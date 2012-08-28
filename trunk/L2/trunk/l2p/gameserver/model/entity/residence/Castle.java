package l2p.gameserver.model.entity.residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.math.SafeMath;
import l2p.gameserver.dao.CastleDAO;
import l2p.gameserver.dao.CastleHiredGuardDAO;
import l2p.gameserver.dao.ClanDataDAO;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Manor;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.item.support.MerchantGuard;
import l2p.gameserver.templates.manor.CropProcure;
import l2p.gameserver.templates.manor.SeedProduction;
import l2p.gameserver.utils.GameStats;
import l2p.gameserver.utils.Log;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.IntObjectMap.Entry;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Castle extends Residence
{
  public static final long serialVersionUID = 1L;
  private static final Logger _log = LoggerFactory.getLogger(Castle.class);
  private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
  private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
  private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
  private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
  private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
  private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
  private final IntObjectMap<MerchantGuard> _merchantGuards = new HashIntObjectMap();
  private final IntObjectMap<List> _relatedFortresses = new CTreeIntObjectMap();
  private Dominion _dominion;
  private List<CropProcure> _procure;
  private List<SeedProduction> _production;
  private List<CropProcure> _procureNext;
  private List<SeedProduction> _productionNext;
  private boolean _isNextPeriodApproved;
  private int _TaxPercent;
  private double _TaxRate;
  private long _treasury;
  private long _collectedShops;
  private long _collectedSeed;
  private final NpcString _npcStringName;
  private Set<ItemInstance> _spawnMerchantTickets = new CopyOnWriteArraySet();

  public Castle(StatsSet set)
  {
    super(set);
    _npcStringName = NpcString.valueOf(1001000 + _id);
  }

  public void init()
  {
    super.init();

    for (IntObjectMap.Entry entry : _relatedFortresses.entrySet())
    {
      _relatedFortresses.remove(entry.getKey());

      List list = (List)entry.getValue();
      List list2 = new ArrayList(list.size());
      for (Iterator i$ = list.iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

        Fortress fortress = (Fortress)ResidenceHolder.getInstance().getResidence(Fortress.class, i);
        if (fortress == null) {
          continue;
        }
        list2.add(fortress);

        fortress.addRelatedCastle(this);
      }
      _relatedFortresses.put(entry.getKey(), list2);
    }
  }

  public ResidenceType getType()
  {
    return ResidenceType.Castle;
  }

  public void changeOwner(Clan newOwner)
  {
    if (newOwner != null)
    {
      if (newOwner.getHasFortress() != 0)
      {
        Fortress oldFortress = (Fortress)ResidenceHolder.getInstance().getResidence(Fortress.class, newOwner.getHasFortress());
        if (oldFortress != null)
          oldFortress.changeOwner(null);
      }
      if (newOwner.getCastle() != 0)
      {
        Castle oldCastle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, newOwner.getCastle());
        if (oldCastle != null) {
          oldCastle.changeOwner(null);
        }
      }
    }
    Clan oldOwner = null;

    if ((getOwnerId() > 0) && ((newOwner == null) || (newOwner.getClanId() != getOwnerId())))
    {
      removeSkills();
      getDominion().changeOwner(null);
      getDominion().removeSkills();

      setTaxPercent(null, 0);
      cancelCycleTask();

      oldOwner = getOwner();
      if (oldOwner != null)
      {
        long amount = getTreasury();
        if (amount > 0L)
        {
          Warehouse warehouse = oldOwner.getWarehouse();
          if (warehouse != null)
          {
            warehouse.addItem(57, amount);
            addToTreasuryNoTax(-amount, false, false);
            Log.add(getName() + "|" + -amount + "|Castle:changeOwner", "treasury");
          }

        }

        for (Player clanMember : oldOwner.getOnlineMembers(0)) {
          if ((clanMember != null) && (clanMember.getInventory() != null)) {
            clanMember.getInventory().validateItems();
          }
        }
        oldOwner.setHasCastle(0);
      }

    }

    if (newOwner != null) {
      newOwner.setHasCastle(getId());
    }

    updateOwnerInDB(newOwner);

    rewardSkills();

    update();
  }

  protected void loadData()
  {
    _TaxPercent = 0;
    _TaxRate = 0.0D;
    _treasury = 0L;
    _procure = new ArrayList();
    _production = new ArrayList();
    _procureNext = new ArrayList();
    _productionNext = new ArrayList();
    _isNextPeriodApproved = false;

    _owner = ClanDataDAO.getInstance().getOwner(this);
    CastleDAO.getInstance().select(this);
    CastleHiredGuardDAO.getInstance().load(this);
  }

  public void setTaxPercent(int p)
  {
    _TaxPercent = Math.min(Math.max(0, p), 100);
    _TaxRate = (_TaxPercent / 100.0D);
  }

  public void setTreasury(long t)
  {
    _treasury = t;
  }

  private void updateOwnerInDB(Clan clan)
  {
    _owner = clan;

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=? LIMIT 1");
      statement.setInt(1, getId());
      statement.execute();
      DbUtils.close(statement);

      if (clan != null)
      {
        statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=? LIMIT 1");
        statement.setInt(1, getId());
        statement.setInt(2, getOwnerId());
        statement.execute();

        clan.broadcastClanStatus(true, false, false);
      }
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public int getTaxPercent()
  {
    if ((_TaxPercent > 5) && (SevenSigns.getInstance().getSealOwner(3) == 1))
      _TaxPercent = 5;
    return _TaxPercent;
  }

  public int getTaxPercent0()
  {
    return _TaxPercent;
  }

  public long getCollectedShops()
  {
    return _collectedShops;
  }

  public long getCollectedSeed()
  {
    return _collectedSeed;
  }

  public void setCollectedShops(long value)
  {
    _collectedShops = value;
  }

  public void setCollectedSeed(long value)
  {
    _collectedSeed = value;
  }

  public void addToTreasury(long amount, boolean shop, boolean seed)
  {
    if (getOwnerId() <= 0) {
      return;
    }
    if (amount == 0L) {
      return;
    }
    if ((amount > 1L) && (_id != 5) && (_id != 8))
    {
      Castle royal = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _id >= 7 ? 8 : 5);
      if (royal != null)
      {
        long royalTax = ()(amount * royal.getTaxRate());
        if (royal.getOwnerId() > 0)
        {
          royal.addToTreasury(royalTax, shop, seed);
          if (_id == 5)
            Log.add("Aden|" + royalTax + "|Castle:adenTax", "treasury");
          else if (_id == 8) {
            Log.add("Rune|" + royalTax + "|Castle:runeTax", "treasury");
          }
        }
        amount -= royalTax;
      }
    }

    addToTreasuryNoTax(amount, shop, seed);
  }

  public void addToTreasuryNoTax(long amount, boolean shop, boolean seed)
  {
    if (getOwnerId() <= 0) {
      return;
    }
    if (amount == 0L) {
      return;
    }
    GameStats.addAdena(amount);

    _treasury = SafeMath.addAndLimit(_treasury, amount);

    if (shop) {
      _collectedShops += amount;
    }
    if (seed) {
      _collectedSeed += amount;
    }
    setJdbcState(JdbcEntityState.UPDATED);
    update();
  }

  public int getCropRewardType(int crop)
  {
    int rw = 0;
    for (CropProcure cp : _procure)
      if (cp.getId() == crop)
        rw = cp.getReward();
    return rw;
  }

  public void setTaxPercent(Player activeChar, int taxPercent)
  {
    setTaxPercent(taxPercent);

    setJdbcState(JdbcEntityState.UPDATED);
    update();

    if (activeChar != null)
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.model.entity.Castle.OutOfControl.CastleTaxChangetTo", activeChar, new Object[0]).addString(getName()).addNumber(taxPercent));
  }

  public double getTaxRate()
  {
    if ((_TaxRate > 0.05D) && (SevenSigns.getInstance().getSealOwner(3) == 1))
      _TaxRate = 0.05D;
    return _TaxRate;
  }

  public long getTreasury()
  {
    return _treasury;
  }

  public List<SeedProduction> getSeedProduction(int period)
  {
    return period == 0 ? _production : _productionNext;
  }

  public List<CropProcure> getCropProcure(int period)
  {
    return period == 0 ? _procure : _procureNext;
  }

  public void setSeedProduction(List<SeedProduction> seed, int period)
  {
    if (period == 0)
      _production = seed;
    else
      _productionNext = seed;
  }

  public void setCropProcure(List<CropProcure> crop, int period)
  {
    if (period == 0)
      _procure = crop;
    else
      _procureNext = crop;
  }

  public synchronized SeedProduction getSeed(int seedId, int period)
  {
    for (SeedProduction seed : getSeedProduction(period))
      if (seed.getId() == seedId)
        return seed;
    return null;
  }

  public synchronized CropProcure getCrop(int cropId, int period)
  {
    for (CropProcure crop : getCropProcure(period))
      if (crop.getId() == cropId)
        return crop;
    return null;
  }

  public long getManorCost(int period)
  {
    List production;
    List procure;
    List production;
    if (period == 0)
    {
      List procure = _procure;
      production = _production;
    }
    else
    {
      procure = _procureNext;
      production = _productionNext;
    }

    long total = 0L;
    if (production != null)
      for (SeedProduction seed : production)
        total += Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
    if (procure != null)
      for (CropProcure crop : procure)
        total += crop.getPrice() * crop.getStartAmount();
    return total;
  }

  public void saveSeedData()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
      statement.setInt(1, getId());
      statement.execute();
      DbUtils.close(statement);

      if (_production != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_production VALUES ";
        String[] values = new String[_production.size()];
        for (SeedProduction s : _production)
        {
          values[count] = ("(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 0 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
            query = query + "," + values[i];
          statement = con.prepareStatement(query);
          statement.execute();
          DbUtils.close(statement);
        }
      }

      if (_productionNext != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_production VALUES ";
        String[] values = new String[_productionNext.size()];
        for (SeedProduction s : _productionNext)
        {
          values[count] = ("(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 1 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
            query = query + "," + values[i];
          statement = con.prepareStatement(query);
          statement.execute();
          DbUtils.close(statement);
        }
      }
    }
    catch (Exception e)
    {
      _log.error("Error adding seed production data for castle " + getName() + "!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void saveSeedData(int period)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
      statement.setInt(1, getId());
      statement.setInt(2, period);
      statement.execute();
      DbUtils.close(statement);

      List prod = null;
      prod = getSeedProduction(period);

      if (prod != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_production VALUES ";
        String[] values = new String[prod.size()];
        for (SeedProduction s : prod)
        {
          values[count] = ("(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
            query = query + "," + values[i];
          statement = con.prepareStatement(query);
          statement.execute();
          DbUtils.close(statement);
        }
      }
    }
    catch (Exception e)
    {
      _log.error("Error adding seed production data for castle " + getName() + "!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void saveCropData()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
      statement.setInt(1, getId());
      statement.execute();
      DbUtils.close(statement);
      if (_procure != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_procure VALUES ";
        String[] values = new String[_procure.size()];
        for (CropProcure cp : _procure)
        {
          values[count] = ("(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 0 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
            query = query + "," + values[i];
          statement = con.prepareStatement(query);
          statement.execute();
          DbUtils.close(statement);
        }
      }
      if (_procureNext != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_procure VALUES ";
        String[] values = new String[_procureNext.size()];
        for (CropProcure cp : _procureNext)
        {
          values[count] = ("(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 1 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
            query = query + "," + values[i];
          statement = con.prepareStatement(query);
          statement.execute();
          DbUtils.close(statement);
        }
      }
    }
    catch (Exception e)
    {
      _log.error("Error adding crop data for castle " + getName() + "!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void saveCropData(int period)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
      statement.setInt(1, getId());
      statement.setInt(2, period);
      statement.execute();
      DbUtils.close(statement);

      List proc = null;
      proc = getCropProcure(period);

      if (proc != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_procure VALUES ";
        String[] values = new String[proc.size()];

        for (CropProcure cp : proc)
        {
          values[count] = ("(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
            query = query + "," + values[i];
          statement = con.prepareStatement(query);
          statement.execute();
          DbUtils.close(statement);
        }
      }
    }
    catch (Exception e)
    {
      _log.error("Error adding crop data for castle " + getName() + "!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void updateCrop(int cropId, long amount, int period)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
      statement.setLong(1, amount);
      statement.setInt(2, cropId);
      statement.setInt(3, getId());
      statement.setInt(4, period);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("Error adding crop data for castle " + getName() + "!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void updateSeed(int seedId, long amount, int period)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
      statement.setLong(1, amount);
      statement.setInt(2, seedId);
      statement.setInt(3, getId());
      statement.setInt(4, period);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("Error adding seed production data for castle " + getName() + "!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public boolean isNextPeriodApproved()
  {
    return _isNextPeriodApproved;
  }

  public void setNextPeriodApproved(boolean val)
  {
    _isNextPeriodApproved = val;
  }

  public Dominion getDominion()
  {
    return _dominion;
  }

  public void setDominion(Dominion dominion)
  {
    _dominion = dominion;
  }

  public void addRelatedFortress(int type, int fortress)
  {
    List fortresses = (List)_relatedFortresses.get(type);
    if (fortresses == null) {
      _relatedFortresses.put(type, fortresses = new ArrayList());
    }
    fortresses.add(Integer.valueOf(fortress));
  }

  public int getDomainFortressContract()
  {
    List list = (List)_relatedFortresses.get(0);
    if (list == null)
      return 0;
    for (Fortress f : list)
      if ((f.getContractState() == 2) && (f.getCastleId() == getId()))
        return f.getId();
    return 0;
  }

  public void update()
  {
    CastleDAO.getInstance().update(this);
  }

  public NpcString getNpcStringName()
  {
    return _npcStringName;
  }

  public IntObjectMap<List> getRelatedFortresses()
  {
    return _relatedFortresses;
  }

  public void addMerchantGuard(MerchantGuard merchantGuard)
  {
    _merchantGuards.put(merchantGuard.getItemId(), merchantGuard);
  }

  public MerchantGuard getMerchantGuard(int itemId)
  {
    return (MerchantGuard)_merchantGuards.get(itemId);
  }

  public IntObjectMap<MerchantGuard> getMerchantGuards()
  {
    return _merchantGuards;
  }

  public Set<ItemInstance> getSpawnMerchantTickets()
  {
    return _spawnMerchantTickets;
  }

  public void startCycleTask()
  {
  }
}