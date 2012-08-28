package net.sf.l2j.gameserver.model.entity;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Auction
{
  protected static final Logger _log = Logger.getLogger(Auction.class.getName());
  private int _id = 0;
  private int _adenaId = 57;
  private long _endDate;
  private int _highestBidderId = 0;
  private String _highestBidderName = "";
  private int _highestBidderMaxBid = 0;
  private int _itemId = 0;
  private String _itemName = "";
  private int _itemObjectId = 0;
  private int _itemQuantity = 0;
  private String _itemType = "";
  private int _sellerId = 0;
  private String _sellerClanName = "";
  private String _sellerName = "";
  private int _currentBid = 0;
  private int _startingBid = 0;

  private Map<Integer, Bidder> _bidders = new FastMap();
  private static final String[] ItemTypeName = { "ClanHall" };

  public Auction(int auctionId)
  {
    _id = auctionId;
    load();
    startAutoTask();
  }

  public Auction(int itemId, L2Clan Clan, long delay, int bid, String name) {
    _id = itemId;
    _endDate = (System.currentTimeMillis() + delay);
    _itemId = itemId;
    _itemName = name;
    _itemType = "ClanHall";
    _sellerId = Clan.getLeaderId();
    _sellerName = Clan.getLeaderName();
    _sellerClanName = Clan.getName();
    _startingBid = bid;
  }

  private void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("Select * from auction where id = ?");
      statement.setInt(1, getId());
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        _currentBid = rs.getInt("currentBid");
        _endDate = rs.getLong("endDate");
        _itemId = rs.getInt("itemId");
        _itemName = rs.getString("itemName");
        _itemObjectId = rs.getInt("itemObjectId");
        _itemType = rs.getString("itemType");
        _sellerId = rs.getInt("sellerId");
        _sellerClanName = rs.getString("sellerClanName");
        _sellerName = rs.getString("sellerName");
        _startingBid = rs.getInt("startingBid");
      }
      statement.close();
      loadBid();
    }
    catch (Exception e)
    {
      System.out.println("Exception: Auction.load(): " + e.getMessage());
      e.printStackTrace(); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void loadBid() {
    _highestBidderId = 0;
    _highestBidderName = "";
    _highestBidderMaxBid = 0;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC");
      statement.setInt(1, getId());
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        if (rs.isFirst())
        {
          _highestBidderId = rs.getInt("bidderId");
          _highestBidderName = rs.getString("bidderName");
          _highestBidderMaxBid = rs.getInt("maxBid");
        }
        _bidders.put(Integer.valueOf(rs.getInt("bidderId")), new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid")));
      }

      statement.close();
    }
    catch (Exception e)
    {
      System.out.println("Exception: Auction.loadBid(): " + e.getMessage());
      e.printStackTrace(); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void startAutoTask() {
    long currentTime = System.currentTimeMillis();
    long taskDelay = 0L;
    if (_endDate <= currentTime) {
      _endDate = (currentTime + 604800000L);
      saveAuctionDate();
    } else {
      taskDelay = _endDate - currentTime;
    }ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), taskDelay);
  }

  public static String getItemTypeName(ItemTypeEnum value) {
    return ItemTypeName[value.ordinal()];
  }

  private void saveAuctionDate()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Update auction set endDate = ? where id = ?");
      statement.setLong(1, _endDate);
      statement.setInt(2, _id);
      statement.execute();

      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + e.getMessage(), e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public synchronized void setBid(L2PcInstance bidder, int bid) {
    int requiredAdena = bid;
    if (getHighestBidderName().equals(bidder.getClan().getLeaderName()))
      requiredAdena = bid - getHighestBidderMaxBid();
    if (((getHighestBidderId() > 0) && (bid > getHighestBidderMaxBid())) || ((getHighestBidderId() == 0) && (bid >= getStartingBid())))
    {
      if (takeItem(bidder, 57, requiredAdena))
      {
        updateInDB(bidder, bid);
        bidder.getClan().setAuctionBiddedAt(_id, true);
        return;
      }
    }
    if ((bid < getStartingBid()) || (bid < getHighestBidderMaxBid()))
      bidder.sendMessage("Invalid bid! Bid price must be higher");
  }

  private void returnItem(String Clan, int itemId, int quantity, boolean penalty)
  {
    if (penalty)
      quantity = (int)(quantity * 0.9D);
    ClanTable.getInstance().getClanByName(Clan).getWarehouse().addItem("Outbidded", _adenaId, quantity, null, null);
  }

  private boolean takeItem(L2PcInstance bidder, int itemId, int quantity)
  {
    if ((bidder.getClan() != null) && (bidder.getClan().getWarehouse().getAdena() >= quantity))
    {
      bidder.getClan().getWarehouse().destroyItemByItemId("Buy", _adenaId, quantity, bidder, bidder);
      return true;
    }
    bidder.sendMessage("You do not have enough adena");
    return false;
  }

  private void updateInDB(L2PcInstance bidder, int bid)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      if (getBidders().get(Integer.valueOf(bidder.getClanId())) != null)
      {
        PreparedStatement statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?");
        statement.setInt(1, bidder.getClanId());
        statement.setString(2, bidder.getClan().getLeaderName());
        statement.setInt(3, bid);
        statement.setLong(4, System.currentTimeMillis());
        statement.setInt(5, getId());
        statement.setInt(6, bidder.getClanId());
        statement.execute();
        statement.close();
      }
      else
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)");
        statement.setInt(1, IdFactory.getInstance().getNextId());
        statement.setInt(2, getId());
        statement.setInt(3, bidder.getClanId());
        statement.setString(4, bidder.getName());
        statement.setInt(5, bid);
        statement.setString(6, bidder.getClan().getName());
        statement.setLong(7, System.currentTimeMillis());
        statement.execute();
        statement.close();
        if (L2World.getInstance().getPlayer(_highestBidderName) != null)
          L2World.getInstance().getPlayer(_highestBidderName).sendMessage("You have been out bidded");
      }
      _highestBidderId = bidder.getClanId();
      _highestBidderMaxBid = bid;
      _highestBidderName = bidder.getClan().getLeaderName();
      if (_bidders.get(Integer.valueOf(_highestBidderId)) == null) {
        _bidders.put(Integer.valueOf(_highestBidderId), new Bidder(_highestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis()));
      }
      else {
        ((Bidder)_bidders.get(Integer.valueOf(_highestBidderId))).setBid(bid);
        ((Bidder)_bidders.get(Integer.valueOf(_highestBidderId))).setTimeBid(Calendar.getInstance().getTimeInMillis());
      }
      bidder.sendMessage("You have bidded successfully");
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: Auction.updateInDB(L2PcInstance bidder, int bid): " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void removeBids() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?");
      statement.setInt(1, getId());
      statement.execute();

      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    for (Bidder b : _bidders.values())
    {
      if (ClanTable.getInstance().getClanByName(b.getClanName()).getHasHideout() == 0) {
        returnItem(b.getClanName(), 57, b.getBid(), true);
      }
      else if (L2World.getInstance().getPlayer(b.getName()) != null) {
        L2World.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
      }
      ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
    }
    _bidders.clear();
  }

  public void deleteAuctionFromDB()
  {
    AuctionManager.getInstance().getAuctions().remove(this);
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?");
      statement.setInt(1, _itemId);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void endAuction() {
    if ((GameServer.gameServer.getCHManager() != null) && (GameServer.gameServer.getCHManager().loaded())) {
      if ((_highestBidderId == 0) && (_sellerId == 0))
      {
        startAutoTask();
        return;
      }
      if ((_highestBidderId == 0) && (_sellerId > 0))
      {
        int aucId = AuctionManager.getInstance().getAuctionIndex(_id);
        AuctionManager.getInstance().getAuctions().remove(aucId);
        return;
      }
      if (_sellerId > 0)
      {
        returnItem(_sellerClanName, 57, _highestBidderMaxBid, true);
        returnItem(_sellerClanName, 57, ClanHallManager.getInstance().getClanHallById(_itemId).getLease(), false);
      }
      deleteAuctionFromDB();
      L2Clan Clan = ClanTable.getInstance().getClanByName(((Bidder)_bidders.get(Integer.valueOf(_highestBidderId))).getClanName());
      _bidders.remove(Integer.valueOf(_highestBidderId));
      Clan.setAuctionBiddedAt(0, true);
      removeBids();
      ClanHallManager.getInstance().setOwner(_itemId, Clan);
    }
    else {
      ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), 3000L);
    }
  }

  public synchronized void cancelBid(int bidder)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?");
      statement.setInt(1, getId());
      statement.setInt(2, bidder);
      statement.execute();

      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: Auction.cancelBid(String bidder): " + e.getMessage(), e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    returnItem(((Bidder)_bidders.get(Integer.valueOf(bidder))).getClanName(), 57, ((Bidder)_bidders.get(Integer.valueOf(bidder))).getBid(), true);
    ClanTable.getInstance().getClanByName(((Bidder)_bidders.get(Integer.valueOf(bidder))).getClanName()).setAuctionBiddedAt(0, true);
    _bidders.clear();
    loadBid();
  }

  public void cancelAuction()
  {
    deleteAuctionFromDB();
    removeBids();
  }

  public void confirmAuction()
  {
    AuctionManager.getInstance().getAuctions().add(this);
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemName, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, getId());
      statement.setInt(2, _sellerId);
      statement.setString(3, _sellerName);
      statement.setString(4, _sellerClanName);
      statement.setString(5, _itemType);
      statement.setInt(6, _itemId);
      statement.setInt(7, _itemObjectId);
      statement.setString(8, _itemName);
      statement.setInt(9, _itemQuantity);
      statement.setInt(10, _startingBid);
      statement.setInt(11, _currentBid);
      statement.setLong(12, _endDate);
      statement.execute();
      statement.close();
      loadBid();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: Auction.load(): " + e.getMessage(), e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }
  public final int getId() {
    return _id; } 
  public final int getCurrentBid() { return _currentBid; } 
  public final long getEndDate() { return _endDate; } 
  public final int getHighestBidderId() { return _highestBidderId; } 
  public final String getHighestBidderName() { return _highestBidderName; } 
  public final int getHighestBidderMaxBid() { return _highestBidderMaxBid; } 
  public final int getItemId() { return _itemId; } 
  public final String getItemName() { return _itemName; } 
  public final int getItemObjectId() { return _itemObjectId; } 
  public final int getItemQuantity() { return _itemQuantity; } 
  public final String getItemType() { return _itemType; } 
  public final int getSellerId() { return _sellerId; } 
  public final String getSellerName() { return _sellerName; } 
  public final String getSellerClanName() { return _sellerClanName; } 
  public final int getStartingBid() { return _startingBid; } 
  public final Map<Integer, Bidder> getBidders() { return _bidders;
  }

  public class AutoEndTask
    implements Runnable
  {
    public AutoEndTask()
    {
    }

    public void run()
    {
      try
      {
        endAuction();
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class Bidder
  {
    private String _name;
    private String _clanName;
    private int _bid;
    private Calendar _timeBid;

    public Bidder(String name, String clanName, int bid, long timeBid)
    {
      _name = name;
      _clanName = clanName;
      _bid = bid;
      _timeBid = Calendar.getInstance();
      _timeBid.setTimeInMillis(timeBid);
    }

    public String getName() {
      return _name;
    }

    public String getClanName() {
      return _clanName;
    }

    public int getBid() {
      return _bid;
    }

    public Calendar getTimeBid() {
      return _timeBid;
    }

    public void setTimeBid(long timeBid) {
      _timeBid.setTimeInMillis(timeBid);
    }

    public void setBid(int bid) {
      _bid = bid;
    }
  }

  public static enum ItemTypeEnum
  {
    ClanHall;
  }
}