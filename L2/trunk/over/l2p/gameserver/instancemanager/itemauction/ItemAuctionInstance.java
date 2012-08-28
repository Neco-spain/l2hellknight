package l2p.gameserver.instancemanager.itemauction;

import gnu.trove.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.commons.util.Rnd;
import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.ItemInstance.ItemLocation;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.serverpackets.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemAuctionInstance
{
  private static final Logger _log = LoggerFactory.getLogger(ItemAuctionInstance.class);

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

  private static final long START_TIME_SPACE = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.MINUTES);
  private static final long FINISH_TIME_SPACE = TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);
  private final int _instanceId;
  private final TIntObjectHashMap<ItemAuction> _auctions;
  private final List<AuctionItem> _items;
  private final SchedulingPattern _dateTime;
  private ItemAuction _currentAuction;
  private ItemAuction _nextAuction;
  private ScheduledFuture<?> _stateTask;

  ItemAuctionInstance(int instanceId, SchedulingPattern dateTime, List<AuctionItem> items)
    throws Exception
  {
    _instanceId = instanceId;
    _auctions = new TIntObjectHashMap();
    _items = items;
    _dateTime = dateTime;

    load();

    _log.info("ItemAuction: Loaded " + _items.size() + " item(s) and registered " + _auctions.size() + " auction(s) for instance " + _instanceId + ".");
    checkAndSetCurrentAndNextAuction();
  }

  private void load()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT auctionId FROM item_auction WHERE instanceId=?");
      statement.setInt(1, _instanceId);
      rset = statement.executeQuery();

      while (rset.next())
      {
        int auctionId = rset.getInt(1);
        try
        {
          ItemAuction auction = loadAuction(auctionId);
          if (auction != null)
            _auctions.put(auctionId, auction);
          else
            ItemAuctionManager.getInstance().deleteAuction(auctionId);
        }
        catch (SQLException e)
        {
          _log.warn("ItemAuction: Failed loading auction: " + auctionId, e);
        }
      }
    }
    catch (SQLException e) {
      _log.error("ItemAuction: Failed loading auctions.", e);
      return;
    }
    finally {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public ItemAuction getCurrentAuction()
  {
    return _currentAuction;
  }

  public ItemAuction getNextAuction()
  {
    return _nextAuction;
  }

  public void shutdown()
  {
    ScheduledFuture stateTask = _stateTask;
    if (stateTask != null)
      stateTask.cancel(false);
  }

  private AuctionItem getAuctionItem(int auctionItemId)
  {
    for (int i = _items.size(); i-- > 0; ) {
      try
      {
        AuctionItem item = (AuctionItem)_items.get(i);
        if (item.getAuctionItemId() == auctionItemId)
          return item;
      }
      catch (IndexOutOfBoundsException e)
      {
      }
    }
    return null;
  }

  void checkAndSetCurrentAndNextAuction()
  {
    ItemAuction[] auctions = (ItemAuction[])_auctions.getValues(new ItemAuction[_auctions.size()]);

    ItemAuction currentAuction = null;
    ItemAuction nextAuction = null;

    switch (auctions.length)
    {
    case 0:
      nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
      break;
    case 1:
      switch (2.$SwitchMap$l2p$gameserver$instancemanager$itemauction$ItemAuctionState[auctions[0].getAuctionState().ordinal()])
      {
      case 1:
        if (auctions[0].getStartingTime() < System.currentTimeMillis() + START_TIME_SPACE)
        {
          currentAuction = auctions[0];
          nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
        }
        else {
          nextAuction = auctions[0];
        }break;
      case 2:
        currentAuction = auctions[0];
        nextAuction = createAuction(Math.max(currentAuction.getEndingTime() + FINISH_TIME_SPACE, System.currentTimeMillis() + START_TIME_SPACE));
        break;
      case 3:
        currentAuction = auctions[0];
        nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE);
        break;
      default:
        throw new IllegalArgumentException();
      }

    default:
      Arrays.sort(auctions, new Comparator()
      {
        public int compare(ItemAuction o1, ItemAuction o2)
        {
          if (o2.getStartingTime() > o1.getStartingTime())
            return 1;
          if (o2.getStartingTime() < o1.getStartingTime()) {
            return -1;
          }
          return 0;
        }
      });
      long currentTime = System.currentTimeMillis();

      for (int i = 0; i < auctions.length; i++)
      {
        ItemAuction auction = auctions[i];
        if (auction.getAuctionState() == ItemAuctionState.STARTED)
        {
          currentAuction = auction;
          break;
        }
        if (auction.getStartingTime() > currentTime)
          continue;
        currentAuction = auction;
        break;
      }

      for (int i = 0; i < auctions.length; i++)
      {
        ItemAuction auction = auctions[i];
        if ((auction.getStartingTime() <= currentTime) || (currentAuction == auction))
          continue;
        nextAuction = auction;
        break;
      }

      if (nextAuction != null) break;
      nextAuction = createAuction(System.currentTimeMillis() + START_TIME_SPACE); break;
    }

    _auctions.put(nextAuction.getAuctionId(), nextAuction);

    _currentAuction = currentAuction;
    _nextAuction = nextAuction;

    if ((currentAuction != null) && (currentAuction.getAuctionState() == ItemAuctionState.STARTED))
    {
      setStateTask(ThreadPoolManager.getInstance().schedule(new ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getEndingTime() - System.currentTimeMillis(), 0L)));
      _log.info("ItemAuction: Schedule current auction " + currentAuction.getAuctionId() + " for instance " + _instanceId);
    }
    else
    {
      setStateTask(ThreadPoolManager.getInstance().schedule(new ScheduleAuctionTask(nextAuction), Math.max(nextAuction.getStartingTime() - System.currentTimeMillis(), 0L)));
      _log.info("ItemAuction: Schedule next auction " + nextAuction.getAuctionId() + " on " + DATE_FORMAT.format(new Date(nextAuction.getStartingTime())) + " for instance " + _instanceId);
    }
  }

  public ItemAuction getAuction(int auctionId)
  {
    return (ItemAuction)_auctions.get(auctionId);
  }

  public ItemAuction[] getAuctionsByBidder(int bidderObjId)
  {
    ItemAuction[] auctions = getAuctions();
    List stack = new ArrayList(auctions.length);
    for (ItemAuction auction : getAuctions()) {
      if (auction.getAuctionState() == ItemAuctionState.CREATED)
        continue;
      ItemAuctionBid bid = auction.getBidFor(bidderObjId);
      if (bid != null)
        stack.add(auction);
    }
    return (ItemAuction[])stack.toArray(new ItemAuction[stack.size()]);
  }

  public ItemAuction[] getAuctions()
  {
    synchronized (_auctions)
    {
      return (ItemAuction[])_auctions.getValues(new ItemAuction[_auctions.size()]);
    }
  }

  void onAuctionFinished(ItemAuction auction)
  {
    auction.broadcastToAllBidders(new SystemMessage(2173).addNumber(auction.getAuctionId()));
    ItemAuctionBid bid = auction.getHighestBid();
    if (bid != null)
    {
      ItemInstance item = auction.createNewItemInstance();
      Player player = bid.getPlayer();
      if (player != null)
      {
        player.getWarehouse().addItem(item);
        player.sendPacket(Msg.YOU_HAVE_BID_THE_HIGHEST_PRICE_AND_HAVE_WON_THE_ITEM_THE_ITEM_CAN_BE_FOUND_IN_YOUR_PERSONAL);

        _log.info("ItemAuction: Auction " + auction.getAuctionId() + " has finished. Highest bid by (name) " + player.getName() + " for instance " + _instanceId);
      }
      else
      {
        item.setOwnerId(bid.getCharId());
        item.setLocation(ItemInstance.ItemLocation.WAREHOUSE);
        item.setJdbcState(JdbcEntityState.UPDATED);
        item.update();

        _log.info("ItemAuction: Auction " + auction.getAuctionId() + " has finished. Highest bid by (id) " + bid.getCharId() + " for instance " + _instanceId);
      }
    }
    else {
      _log.info("ItemAuction: Auction " + auction.getAuctionId() + " has finished. There have not been any bid for instance " + _instanceId);
    }
  }

  void setStateTask(ScheduledFuture<?> future) {
    ScheduledFuture stateTask = _stateTask;
    if (stateTask != null) {
      stateTask.cancel(false);
    }
    _stateTask = future;
  }

  private ItemAuction createAuction(long after)
  {
    AuctionItem auctionItem = (AuctionItem)_items.get(Rnd.get(_items.size()));
    long startingTime = _dateTime.next(after);
    long endingTime = startingTime + TimeUnit.MILLISECONDS.convert(auctionItem.getAuctionLength(), TimeUnit.MINUTES);
    int auctionId = ItemAuctionManager.getInstance().getNextId();
    ItemAuction auction = new ItemAuction(auctionId, _instanceId, startingTime, endingTime, auctionItem, ItemAuctionState.CREATED);

    auction.store();

    return auction;
  }

  private ItemAuction loadAuction(int auctionId) throws SQLException
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT auctionItemId,startingTime,endingTime,auctionStateId FROM item_auction WHERE auctionId=?");
      statement.setInt(1, auctionId);
      rset = statement.executeQuery();

      if (!rset.next())
      {
        _log.warn("ItemAuction: Auction data not found for auction: " + auctionId);
        Object localObject1 = null;
        return localObject1;
      }
      int auctionItemId = rset.getInt(1);
      long startingTime = rset.getLong(2);
      long endingTime = rset.getLong(3);
      int auctionStateId = rset.getInt(4);

      DbUtils.close(statement, rset);

      if (startingTime >= endingTime)
      {
        _log.warn("ItemAuction: Invalid starting/ending paramaters for auction: " + auctionId);
        Object localObject2 = null;
        return localObject2;
      }
      AuctionItem auctionItem = getAuctionItem(auctionItemId);
      if (auctionItem == null)
      {
        _log.warn("ItemAuction: AuctionItem: " + auctionItemId + ", not found for auction: " + auctionId);
        Object localObject3 = null;
        return localObject3;
      }
      ItemAuctionState auctionState = ItemAuctionState.stateForStateId(auctionStateId);
      if (auctionState == null)
      {
        _log.warn("ItemAuction: Invalid auctionStateId: " + auctionStateId + ", for auction: " + auctionId);
        Object localObject4 = null;
        return localObject4;
      }
      ItemAuction auction = new ItemAuction(auctionId, _instanceId, startingTime, endingTime, auctionItem, auctionState);

      statement = con.prepareStatement("SELECT playerObjId,playerBid FROM item_auction_bid WHERE auctionId=?");
      statement.setInt(1, auctionId);
      rset = statement.executeQuery();

      while (rset.next())
      {
        charId = rset.getInt(1);
        long playerBid = rset.getLong(2);
        ItemAuctionBid bid = new ItemAuctionBid(charId, playerBid);
        auction.addBid(bid);
      }

      int charId = auction;
      return charId; } finally { DbUtils.closeQuietly(con, statement, rset); } throw localObject5;
  }

  private class ScheduleAuctionTask extends RunnableImpl
  {
    private ItemAuction _auction;

    public ScheduleAuctionTask(ItemAuction auction)
    {
      _auction = auction;
    }

    public void runImpl() throws Exception
    {
      ItemAuctionState state = _auction.getAuctionState();

      switch (ItemAuctionInstance.2.$SwitchMap$l2p$gameserver$instancemanager$itemauction$ItemAuctionState[state.ordinal()])
      {
      case 1:
        if (!_auction.setAuctionState(state, ItemAuctionState.STARTED)) {
          throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.STARTED.toString() + ", expected: " + state.toString());
        }
        _log.info("ItemAuction: Auction " + _auction.getAuctionId() + " has started for instance " + _auction.getInstanceId());
        if (Config.ALT_ITEM_AUCTION_START_ANNOUNCE)
        {
          String[] params = new String[0];
          Announcements.getInstance().announceByCustomMessage("l2p.gameserver.model.instances.L2ItemAuctionBrokerInstance.announce." + _auction.getInstanceId(), params);
        }
        checkAndSetCurrentAndNextAuction();
        break;
      case 2:
        switch (_auction.getAuctionEndingExtendState())
        {
        case 1:
          if (_auction.getScheduledAuctionEndingExtendState() != 0)
            break;
          _auction.setScheduledAuctionEndingExtendState(1);
          setStateTask(ThreadPoolManager.getInstance().schedule(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
          return;
        case 2:
          if (_auction.getScheduledAuctionEndingExtendState() == 2)
            break;
          _auction.setScheduledAuctionEndingExtendState(2);
          setStateTask(ThreadPoolManager.getInstance().schedule(this, Math.max(_auction.getEndingTime() - System.currentTimeMillis(), 0L)));
          return;
        }

        if (!_auction.setAuctionState(state, ItemAuctionState.FINISHED)) {
          throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.FINISHED.toString() + ", expected: " + state.toString());
        }
        onAuctionFinished(_auction);
        checkAndSetCurrentAndNextAuction();
        break;
      default:
        throw new IllegalStateException("Invalid state: " + state);
      }
    }
  }
}