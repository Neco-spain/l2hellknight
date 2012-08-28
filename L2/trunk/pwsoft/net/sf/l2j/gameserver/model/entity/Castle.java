package net.sf.l2j.gameserver.model.entity;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.CastleUpdater;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.zone.L2ZoneType;
import scripts.zone.type.L2CastleZone;
import scripts.zone.type.L2SiegeWaitZone;

public class Castle
{
  protected static final Logger _log = AbstractLogger.getLogger(Castle.class.getName());

  private FastList<CastleManorManager.CropProcure> _procure = new FastList();
  private FastList<CastleManorManager.SeedProduction> _production = new FastList();
  private FastList<CastleManorManager.CropProcure> _procureNext = new FastList();
  private FastList<CastleManorManager.SeedProduction> _productionNext = new FastList();
  private boolean _isNextPeriodApproved = false;
  private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
  private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
  private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
  private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
  private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
  private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
  private int _castleId = 0;
  private List<L2DoorInstance> _doors = new FastList();
  private List<String> _doorDefault = new FastList();
  private String _name = "";
  private int _ownerId = 0;
  private Siege _siege = null;
  private Calendar _siegeDate;
  private int _siegeDayOfWeek = 7;
  private int _siegeHourOfDay = 20;
  private int _taxPercent = 0;
  private double _taxRate = 0.0D;
  private int _treasury = 0;
  private L2CastleZone _zone;
  private L2SiegeWaitZone _zoneWait;
  private L2Clan _formerOwner = null;
  private int _nbArtifact = 1;
  private Map<Integer, Integer> _engrave = new FastMap();

  public Castle(int castleId)
  {
    _castleId = castleId;
    if ((_castleId == 7) || (castleId == 9))
    {
      _nbArtifact = 2;
    }
    load();
    loadDoor();
  }

  public void Engrave(L2Clan clan, int objId)
  {
    _engrave.put(Integer.valueOf(objId), Integer.valueOf(clan.getClanId()));
    if (_engrave.size() == _nbArtifact) {
      boolean rst = true;
      for (Iterator i$ = _engrave.values().iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();
        if (id != clan.getClanId()) {
          rst = false;
        }
      }
      if (rst) {
        _engrave.clear();
        setOwner(clan);
      } else {
        getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
      }
    } else {
      getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
    }
  }

  public void addToTreasury(int amount)
  {
    if (getOwnerId() <= 0) {
      return;
    }

    if ((_name.equalsIgnoreCase("Schuttgart")) || (_name.equalsIgnoreCase("Goddard"))) {
      Castle rune = CastleManager.getInstance().getCastle("rune");
      if (rune != null) {
        int runeTax = (int)(amount * rune.getTaxRate());
        if (rune.getOwnerId() > 0) {
          rune.addToTreasury(runeTax);
        }
        amount -= runeTax;
      }
    }
    if ((!_name.equalsIgnoreCase("aden")) && (!_name.equalsIgnoreCase("Rune")) && (!_name.equalsIgnoreCase("Schuttgart")) && (!_name.equalsIgnoreCase("Goddard")))
    {
      Castle aden = CastleManager.getInstance().getCastle("aden");
      if (aden != null) {
        int adenTax = (int)(amount * aden.getTaxRate());
        if (aden.getOwnerId() > 0) {
          aden.addToTreasury(adenTax);
        }
        amount -= adenTax;
      }
    }

    addToTreasuryNoTax(amount);
  }

  public boolean addToTreasuryNoTax(int amount)
  {
    if (getOwnerId() <= 0) {
      return false;
    }

    long cur = _treasury + amount;
    if (cur < 0L)
      _treasury = 0;
    else if (cur >= 2147483647L)
      _treasury = 2147483647;
    else {
      _treasury = (int)cur;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE castle SET treasury = ? WHERE id = ?");
      statement.setInt(1, _treasury);
      statement.setInt(2, _castleId);
      statement.execute();
    } catch (Exception e) {
      System.out.println("Exception: addToTreasuryNoTax: " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
    return true;
  }

  public void banishForeigners()
  {
    getZone().banishForeigners(getOwnerId());
  }

  public boolean checkIfInZone(int x, int y, int z)
  {
    return getZone().isInsideZone(x, y, z);
  }

  public void setZone(L2CastleZone zone)
  {
    _zone = zone;
  }

  public L2CastleZone getZone() {
    if (_zone == null) {
      for (L2ZoneType zone : ZoneManager.getInstance().getAllZones()) {
        if (((zone instanceof L2CastleZone)) && (((L2CastleZone)zone).getCastleId() == getCastleId())) {
          _zone = ((L2CastleZone)zone);
          break;
        }
      }
    }
    return _zone;
  }

  public void setWaitZone(L2SiegeWaitZone zone) {
    _zoneWait = zone;
  }

  public L2SiegeWaitZone getWaitZone() {
    if (_zoneWait == null) {
      for (L2ZoneType zone : ZoneManager.getInstance().getAllZones()) {
        if (((zone instanceof L2SiegeWaitZone)) && (((L2SiegeWaitZone)zone).getCastleId() == getCastleId())) {
          _zoneWait = ((L2SiegeWaitZone)zone);
          break;
        }
      }
    }
    return _zoneWait;
  }

  public double getDistance(L2Object obj)
  {
    return getZone().getDistanceToZone(obj);
  }

  public void closeDoor(L2PcInstance activeChar, int doorId) {
    openCloseDoor(activeChar, doorId, false);
  }

  public void openDoor(L2PcInstance activeChar, int doorId) {
    openCloseDoor(activeChar, doorId, true);
  }

  public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open) {
    if (activeChar.getClanId() != getOwnerId()) {
      return;
    }

    L2DoorInstance door = getDoor(doorId);
    if (door != null)
      if (open)
        door.openMe();
      else
        door.closeMe();
  }

  public void removeUpgrade()
  {
    removeDoorUpgrade();
  }

  public void setOwner(L2Clan clan)
  {
    if ((getOwnerId() > 0) && ((clan == null) || (clan.getClanId() != getOwnerId()))) {
      L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());
      if (oldOwner != null) {
        if (_formerOwner == null) {
          _formerOwner = oldOwner;
          if (Config.REMOVE_CASTLE_CIRCLETS) {
            CastleManager.getInstance().removeCirclet(_formerOwner, getCastleId());
          }
        }
        oldOwner.setHasCastle(0);
        oldOwner.removeBonusEffects();
        new Announcements().announceToAll(oldOwner.getName() + " has lost " + getName() + " castle!");
      }
    }

    updateOwnerInDB(clan);

    if (getSiege().getIsInProgress())
    {
      getSiege().midVictory();
    }
    updateClansReputation();
  }

  public void removeOwner(L2Clan clan) {
    if (clan != null) {
      _formerOwner = clan;
      if (Config.REMOVE_CASTLE_CIRCLETS) {
        CastleManager.getInstance().removeCirclet(_formerOwner, getCastleId());
      }
      clan.setHasCastle(0);
      new Announcements().announceToAll(clan.getName() + " has lost " + getName() + " castle");
      clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
    }

    updateOwnerInDB(null);
    if (getSiege().getIsInProgress()) {
      getSiege().midVictory();
    }

    updateClansReputation();
  }

  public void setTaxPercent(L2PcInstance activeChar, int taxPercent)
  {
    int maxTax;
    switch (SevenSigns.getInstance().getSealOwner(3)) {
    case 2:
      maxTax = 25;
      break;
    case 1:
      maxTax = 5;
      break;
    default:
      maxTax = 15;
    }

    if ((taxPercent < 0) || (taxPercent > maxTax)) {
      activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
      return;
    }

    setTaxPercent(taxPercent);
    activeChar.sendMessage(getName() + " castle tax changed to " + taxPercent + "%.");
  }

  public void setTaxPercent(int taxPercent) {
    _taxPercent = taxPercent;
    _taxRate = (_taxPercent / 100.0D);

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      ArrayList breakable = new ArrayList();

      statement = con.prepareStatement("UPDATE castle SET taxPercent = ? WHERE id = ?");
      Iterator castles = breakable.iterator();
      while (castles.hasNext()) {
        statement.setInt(1, taxPercent);
        statement.setInt(2, getCastleId());
        statement.addBatch();
      }
      statement.executeBatch();
      statement.clearBatch();
    } catch (Exception e) {
    } finally {
      Close.CS(con, statement);
    }
  }

  public void spawnDoor()
  {
    spawnDoor(false);
  }

  public void spawnDoor(boolean isDoorWeak)
  {
    for (int i = 0; i < getDoors().size(); i++) {
      L2DoorInstance door = (L2DoorInstance)getDoors().get(i);
      if (door.getCurrentHp() <= 0.0D) {
        door.decayMe();
        door = DoorTable.parseList((String)_doorDefault.get(i));
        if (isDoorWeak) {
          door.setCurrentHp(door.getMaxHp() / 2);
        }
        door.spawnMe(door.getX(), door.getY(), door.getZ());
        getDoors().set(i, door);
      } else if (door.getOpen()) {
        door.closeMe();
      }
    }
    loadDoorUpgrade();
  }

  public void upgradeDoor(int doorId, int hp, int pDef, int mDef)
  {
    L2DoorInstance door = getDoor(doorId);
    if (door == null) {
      return;
    }

    if ((door != null) && (door.getDoorId() == doorId)) {
      door.setCurrentHp(door.getMaxHp() + hp);

      saveDoorUpgrade(doorId, hp, pDef, mDef);
      return;
    }
  }

  private void load()
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      statement = con.prepareStatement("Select * from castle where id = ?");
      statement.setInt(1, getCastleId());
      rs = statement.executeQuery();

      while (rs.next()) {
        _name = rs.getString("name");

        _siegeDate = Calendar.getInstance();
        _siegeDate.setTimeInMillis(rs.getLong("siegeDate"));

        _siegeDayOfWeek = rs.getInt("siegeDayOfWeek");
        if ((_siegeDayOfWeek < 1) || (_siegeDayOfWeek > 7)) {
          _siegeDayOfWeek = 7;
        }

        _siegeHourOfDay = rs.getInt("siegeHourOfDay");
        if ((_siegeHourOfDay < 0) || (_siegeHourOfDay > 23)) {
          _siegeHourOfDay = 20;
        }

        _taxPercent = rs.getInt("taxPercent");
        _treasury = rs.getInt("treasury");
      }

      Close.SR(statement, rs);

      _taxRate = (_taxPercent / 100.0D);

      statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?");
      statement.setInt(1, getCastleId());
      rs = statement.executeQuery();

      while (rs.next()) {
        _ownerId = rs.getInt("clan_id");
      }

      if (getOwnerId() > 0) {
        L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());
        ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000L);
      }

      Close.S(statement);
    } catch (Exception e) {
      System.out.println("Exception: loadCastleData(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CSR(con, statement, rs);
    }
    getSiege();
  }

  private void loadDoor()
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("Select * from castle_door where castleId = ?");
      statement.setInt(1, getCastleId());
      rs = statement.executeQuery();

      while (rs.next())
      {
        _doorDefault.add(rs.getString("name") + ";" + rs.getInt("id") + ";" + rs.getInt("x") + ";" + rs.getInt("y") + ";" + rs.getInt("z") + ";" + rs.getInt("range_xmin") + ";" + rs.getInt("range_ymin") + ";" + rs.getInt("range_zmin") + ";" + rs.getInt("range_xmax") + ";" + rs.getInt("range_ymax") + ";" + rs.getInt("range_zmax") + ";" + rs.getInt("hp") + ";" + rs.getInt("pDef") + ";" + rs.getInt("mDef"));

        L2DoorInstance door = DoorTable.parseList((String)_doorDefault.get(_doorDefault.size() - 1));
        door.spawnMe(door.getX(), door.getY(), door.getZ());
        _doors.add(door);
        DoorTable.getInstance().putDoor(door);
      }
    } catch (Exception e) {
      System.out.println("Exception: loadCastleDoor(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CSR(con, statement, rs);
    }
  }

  private void loadDoorUpgrade()
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("Select * from castle_doorupgrade where doorId in (Select Id from castle_door where castleId = ?)");
      statement.setInt(1, getCastleId());
      rs = statement.executeQuery();
      while (rs.next())
        upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
    }
    catch (Exception e) {
      System.out.println("Exception: loadCastleDoorUpgrade(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CSR(con, statement, rs);
    }
  }

  private void removeDoorUpgrade() {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("delete from castle_doorupgrade where doorId in (select id from castle_door where castleId=?)");
      statement.setInt(1, getCastleId());
      statement.execute();
    } catch (Exception e) {
      System.out.println("Exception: removeDoorUpgrade(): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef) {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
      statement.setInt(1, doorId);
      statement.setInt(2, hp);
      statement.setInt(3, pDef);
      statement.setInt(4, mDef);
      statement.execute();
    } catch (Exception e) {
      System.out.println("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  private void updateOwnerInDB(L2Clan clan) {
    if (clan != null)
      _ownerId = clan.getClanId();
    else {
      _ownerId = 0;
    }
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
      statement.setInt(1, getCastleId());
      statement.execute();
      Close.S(statement);

      statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
      statement.setInt(1, getCastleId());
      statement.setInt(2, getOwnerId());
      statement.execute();
      Close.S(statement);

      if (clan != null) {
        clan.setHasCastle(getCastleId());
        new Announcements().announceToAll("\u041A\u043B\u0430\u043D " + clan.getName() + " \u0437\u0430\u0445\u0432\u0430\u0442\u0438\u043B \u0437\u0430\u043C\u043E\u043A " + getName() + "!");
        clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));

        ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000L);
      }
    } catch (Exception e) {
      System.out.println("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  public final int getCastleId()
  {
    return _castleId;
  }

  public final L2DoorInstance getDoor(int doorId) {
    if (doorId <= 0) {
      return null;
    }

    for (int i = 0; i < getDoors().size(); i++) {
      L2DoorInstance door = (L2DoorInstance)getDoors().get(i);
      if (door.getDoorId() == doorId) {
        return door;
      }
    }
    return null;
  }

  public final List<L2DoorInstance> getDoors() {
    return _doors;
  }

  public final String getName() {
    return _name;
  }

  public final int getOwnerId() {
    return _ownerId;
  }

  public final Siege getSiege() {
    if (_siege == null) {
      _siege = new Siege(new Castle[] { this });
    }
    return _siege;
  }

  public final Calendar getSiegeDate() {
    return _siegeDate;
  }

  public final int getSiegeDayOfWeek() {
    return _siegeDayOfWeek;
  }

  public final int getSiegeHourOfDay() {
    return _siegeHourOfDay;
  }

  public final int getTaxPercent() {
    return _taxPercent;
  }

  public final double getTaxRate() {
    return _taxRate;
  }

  public final int getTreasury() {
    return _treasury;
  }

  public FastList<CastleManorManager.SeedProduction> getSeedProduction(int period) {
    return period == 0 ? _production : _productionNext;
  }

  public FastList<CastleManorManager.CropProcure> getCropProcure(int period) {
    return period == 0 ? _procure : _procureNext;
  }

  public void setSeedProduction(FastList<CastleManorManager.SeedProduction> seed, int period) {
    if (period == 0)
      _production = seed;
    else
      _productionNext = seed;
  }

  public void setCropProcure(FastList<CastleManorManager.CropProcure> crop, int period)
  {
    if (period == 0)
      _procure = crop;
    else
      _procureNext = crop;
  }

  public synchronized CastleManorManager.SeedProduction getSeed(int seedId, int period)
  {
    for (CastleManorManager.SeedProduction seed : getSeedProduction(period)) {
      if (seed.getId() == seedId) {
        return seed;
      }
    }
    return null;
  }

  public synchronized CastleManorManager.CropProcure getCrop(int cropId, int period) {
    for (CastleManorManager.CropProcure crop : getCropProcure(period)) {
      if (crop.getId() == cropId) {
        return crop;
      }
    }
    return null;
  }

  public int getManorCost(int period)
  {
    FastList production;
    FastList procure;
    FastList production;
    if (period == 0) {
      FastList procure = _procure;
      production = _production;
    } else {
      procure = _procureNext;
      production = _productionNext;
    }

    int total = 0;
    if (production != null) {
      for (CastleManorManager.SeedProduction seed : production) {
        total += L2Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
      }
    }
    if (procure != null) {
      for (CastleManorManager.CropProcure crop : procure) {
        total += crop.getPrice() * crop.getStartAmount();
      }
    }
    return total;
  }

  public void saveSeedData()
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
      statement.setInt(1, getCastleId());
      statement.execute();

      Close.S(statement);

      TextBuilder query = new TextBuilder("INSERT INTO castle_manor_production VALUES ");
      if (_production != null) {
        int count = 0;
        String[] values = new String[_production.size()];
        for (CastleManorManager.SeedProduction s : _production) {
          values[count] = ("(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 0 + ")");
          count++;
        }
        if (values.length > 0) {
          query.append(values[0]);
          for (int i = 1; i < values.length; i++) {
            query.append("," + values[i]);
          }
          statement = con.prepareStatement(query.toString());
          statement.execute();
          Close.S(statement);
        }
      }
      query.clear();
      query.append("INSERT INTO castle_manor_production VALUES ");
      if (_productionNext != null) {
        int count = 0;
        String[] values = new String[_productionNext.size()];
        for (CastleManorManager.SeedProduction s : _productionNext) {
          values[count] = ("(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 1 + ")");
          count++;
        }
        if (values.length > 0) {
          query.append(values[0]);
          for (int i = 1; i < values.length; i++) {
            query.append("," + values[i]);
          }
          statement = con.prepareStatement(query.toString());
          statement.execute();
          Close.S(statement);
        }
      }
    } catch (Exception e) {
      _log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
    } finally {
      Close.CS(con, statement);
    }
  }

  public void saveSeedData(int period)
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
      statement.setInt(1, getCastleId());
      statement.setInt(2, period);
      statement.execute();
      Close.S(statement);

      FastList prod = null;
      prod = getSeedProduction(period);

      if (prod != null) {
        int count = 0;
        TextBuilder query = new TextBuilder("INSERT INTO castle_manor_production VALUES ");
        String[] values = new String[prod.size()];
        for (CastleManorManager.SeedProduction s : prod) {
          values[count] = ("(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")");
          count++;
        }
        if (values.length > 0) {
          query.append(values[0]);
          for (int i = 1; i < values.length; i++) {
            query.append("," + values[i]);
          }
          statement = con.prepareStatement(query.toString());
          statement.execute();
          Close.S(statement);
        }
      }
    } catch (Exception e) {
      _log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
    } finally {
      Close.CS(con, statement);
    }
  }

  public void saveCropData()
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
      statement.setInt(1, getCastleId());
      statement.execute();
      Close.S(statement);
      TextBuilder query = new TextBuilder("INSERT INTO castle_manor_procure VALUES ");
      if (_procure != null) {
        int count = 0;
        String[] values = new String[_procure.size()];
        for (CastleManorManager.CropProcure cp : _procure) {
          values[count] = ("(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 0 + ")");
          count++;
        }
        if (values.length > 0) {
          query.append(values[0]);
          for (int i = 1; i < values.length; i++) {
            query.append("," + values[i]);
          }
          statement = con.prepareStatement(query.toString());
          statement.execute();
          Close.S(statement);
        }
      }
      query.clear();
      query.append("INSERT INTO castle_manor_procure VALUES ");
      if (_procureNext != null) {
        int count = 0;
        String[] values = new String[_procureNext.size()];
        for (CastleManorManager.CropProcure cp : _procureNext) {
          values[count] = ("(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 1 + ")");
          count++;
        }
        if (values.length > 0) {
          query.append(values[0]);
          for (int i = 1; i < values.length; i++) {
            query.append("," + values[i]);
          }
          statement = con.prepareStatement(query.toString());
          statement.execute();
          Close.S(statement);
        }
      }
      query.clear();
    } catch (Exception e) {
      _log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
    } finally {
      Close.CS(con, statement);
    }
  }

  public void saveCropData(int period)
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
      statement.setInt(1, getCastleId());
      statement.setInt(2, period);
      statement.execute();
      Close.S(statement);

      FastList proc = null;
      proc = getCropProcure(period);

      if (proc != null) {
        int count = 0;
        TextBuilder query = new TextBuilder("INSERT INTO castle_manor_procure VALUES ");
        String[] values = new String[proc.size()];
        for (CastleManorManager.CropProcure cp : proc) {
          values[count] = ("(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")");
          count++;
        }
        if (values.length > 0) {
          query.append(values[0]);
          for (int i = 1; i < values.length; i++) {
            query.append("," + values[i]);
          }
          statement = con.prepareStatement(query.toString());
          statement.execute();
          Close.S(statement);
        }
      }
    } catch (Exception e) {
      _log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
    } finally {
      Close.CS(con, statement);
    }
  }

  public void updateCrop(int cropId, int amount, int period) {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
      statement.setInt(1, amount);
      statement.setInt(2, cropId);
      statement.setInt(3, getCastleId());
      statement.setInt(4, period);
      statement.execute();
    } catch (Exception e) {
      _log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
    } finally {
      Close.CS(con, statement);
    }
  }

  public void updateSeed(int seedId, int amount, int period) {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
      statement.setInt(1, amount);
      statement.setInt(2, seedId);
      statement.setInt(3, getCastleId());
      statement.setInt(4, period);
      statement.execute();
    } catch (Exception e) {
      _log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
    } finally {
      Close.CS(con, statement);
    }
  }

  public boolean isNextPeriodApproved() {
    return _isNextPeriodApproved;
  }

  public void setNextPeriodApproved(boolean val) {
    _isNextPeriodApproved = val;
  }

  public void updateClansReputation() {
    if (_formerOwner != null) {
      if (_formerOwner != ClanTable.getInstance().getClan(getOwnerId())) {
        int maxreward = Math.max(0, _formerOwner.getReputationScore());
        _formerOwner.setReputationScore(_formerOwner.getReputationScore() - 1000, true);
        L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
        if (owner != null) {
          owner.addPoints(Math.min(1000, maxreward));
          owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
        }
      } else {
        _formerOwner.addPoints(500);
      }

      _formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_formerOwner));
    } else {
      L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
      if (owner != null) {
        owner.addPoints(1000);
        owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
      }
    }
  }

  public boolean isClanhall() {
    switch (_castleId) {
    case 21:
    case 34:
    case 35:
    case 64:
      return true;
    }
    return false;
  }

  public void giveOwnerBonus()
  {
    L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());
    if (clan == null) {
      return;
    }

    FastList rewards = (FastList)Config.CASTLE_SIEGE_REWARDS.get(Integer.valueOf(getCastleId()));
    if ((rewards == null) || (rewards.isEmpty())) {
      return;
    }

    L2PcInstance owner = L2World.getInstance().getPlayer(clan.getLeaderId());
    if (owner == null) {
      return;
    }

    FastList.Node k = rewards.head(); for (FastList.Node endk = rewards.tail(); (k = k.getNext()) != endk; ) {
      Config.EventReward reward = (Config.EventReward)k.getValue();
      if (reward == null)
      {
        continue;
      }
      if (Rnd.get(100) < reward.chance)
        owner.addItem("SiegeReward", reward.id, Rnd.get(1, reward.count), owner, true);
    }
  }

  public void giveClanBonus()
  {
    L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());
    if (clan == null) {
      return;
    }

    clan.setBonusSkill();
  }
}