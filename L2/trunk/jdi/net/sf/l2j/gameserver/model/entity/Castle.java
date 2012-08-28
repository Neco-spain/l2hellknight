package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
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
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.type.L2CastleTeleportZone;
import net.sf.l2j.gameserver.model.zone.type.L2CastleZone;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;

public class Castle
{
  protected static final Logger _log = Logger.getLogger(Castle.class.getName());

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
  private L2CastleTeleportZone _teleZone;
  private L2Clan _formerOwner = null;
  private int _nbArtifact = 1;
  private Map<Integer, Integer> _engrave = new FastMap();
  private Map<Integer, CastleFunction> _function;
  public static final int FUNC_TELEPORT = 1;
  public static final int FUNC_RESTORE_HP = 2;
  public static final int FUNC_RESTORE_MP = 3;
  public static final int FUNC_RESTORE_EXP = 4;
  public static final int FUNC_SUPPORT = 5;

  public Castle(int castleId)
  {
    _castleId = castleId;
    if ((_castleId == 7) || (castleId == 9))
      _nbArtifact = 2;
    load();
    loadDoor();
    _function = new FastMap();
    if (getOwnerId() != 0)
    {
      loadFunctions();
    }
  }

  public CastleFunction getFunction(int type)
  {
    if (_function.get(Integer.valueOf(type)) != null)
      return (CastleFunction)_function.get(Integer.valueOf(type));
    return null;
  }

  public void Engrave(L2Clan clan, int objId)
  {
    _engrave.put(Integer.valueOf(objId), Integer.valueOf(clan.getClanId()));
    if (_engrave.size() == _nbArtifact)
    {
      boolean rst = true;
      for (Iterator i$ = _engrave.values().iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();

        if (id != clan.getClanId())
          rst = false;
      }
      if (rst)
      {
        _engrave.clear();
        setOwner(clan);
      }
      else {
        getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
      }
    } else {
      getSiege().announceToPlayer("Clan " + clan.getName() + " has finished to engrave one of the rulers.", true);
    }
  }

  public void addToTreasury(int amount)
  {
    if (getOwnerId() <= 0)
    {
      return;
    }

    if ((_name.equalsIgnoreCase("Schuttgart")) || (_name.equalsIgnoreCase("Goddard")))
    {
      Castle rune = CastleManager.getInstance().getCastle("rune");
      if (rune != null)
      {
        int runeTax = (int)(amount * rune.getTaxRate());
        if (rune.getOwnerId() > 0) rune.addToTreasury(runeTax);
        amount -= runeTax;
      }
    }
    if ((!_name.equalsIgnoreCase("aden")) && (!_name.equalsIgnoreCase("Rune")) && (!_name.equalsIgnoreCase("Schuttgart")) && (!_name.equalsIgnoreCase("Goddard")))
    {
      Castle aden = CastleManager.getInstance().getCastle("aden");
      if (aden != null)
      {
        int adenTax = (int)(amount * aden.getTaxRate());
        if (aden.getOwnerId() > 0) aden.addToTreasury(adenTax);

        amount -= adenTax;
      }
    }

    addToTreasuryNoTax(amount);
  }

  public boolean addToTreasuryNoTax(int amount)
  {
    if (getOwnerId() <= 0) return false;

    if (amount < 0) {
      amount *= -1;
      if (_treasury < amount) return false;
      _treasury -= amount;
    }
    else if (_treasury + amount > 2147483647L) { _treasury = 2147483647; } else {
      _treasury += amount;
    }

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Update castle set treasury = ? where id = ?");
      statement.setInt(1, getTreasury());
      statement.setInt(2, getCastleId());
      statement.execute();
      statement.close(); } catch (Exception e) {
    } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }return true;
  }

  public void banishForeigners()
  {
    _zone.banishForeigners(getOwnerId());
  }

  public boolean checkIfInZone(int x, int y, int z)
  {
    return _zone.isInsideZone(x, y, z);
  }

  public void setZone(L2CastleZone zone)
  {
    _zone = zone;
  }

  public L2CastleZone getZone()
  {
    return _zone;
  }

  public void setTeleZone(L2CastleTeleportZone zone)
  {
    _teleZone = zone;
  }

  public L2CastleTeleportZone getTeleZone()
  {
    return _teleZone;
  }

  public void oustAllPlayers()
  {
    _teleZone.oustAllPlayers();
  }

  public double getDistance(L2Object obj)
  {
    return _zone.getDistanceToZone(obj);
  }

  public void closeDoor(L2PcInstance activeChar, int doorId)
  {
    openCloseDoor(activeChar, doorId, false);
  }

  public void openDoor(L2PcInstance activeChar, int doorId)
  {
    openCloseDoor(activeChar, doorId, true);
  }

  public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
  {
    if (activeChar.getClanId() != getOwnerId()) {
      return;
    }
    L2DoorInstance door = getDoor(doorId);
    if (door != null)
    {
      if (open)
        door.openMe();
      else
        door.closeMe();
    }
  }

  public void removeUpgrade()
  {
    removeDoorUpgrade();
    for (Map.Entry fc : _function.entrySet())
      removeFunction(((Integer)fc.getKey()).intValue());
    _function.clear();
  }

  public void setOwner(L2Clan clan)
  {
    if ((getOwnerId() > 0) && ((clan == null) || (clan.getClanId() != getOwnerId())))
    {
      L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());
      if (oldOwner != null)
      {
        if (_formerOwner == null)
        {
          _formerOwner = oldOwner;
          if (Config.REMOVE_CASTLE_CIRCLETS)
          {
            CastleManager.getInstance().removeCirclet(_formerOwner, getCastleId());
          }
        }
        oldOwner.setHasCastle(0);
        Announcements.getInstance().announceToAll(oldOwner.getName() + " has lost " + getName() + " castle!");
        CrownManager.getInstance().checkCrowns(oldOwner);
      }
    }

    updateOwnerInDB(clan);

    if (getSiege().getIsInProgress()) {
      getSiege().midVictory();
    }
    updateClansReputation();
  }

  public void removeOwner(L2Clan clan)
  {
    if (clan != null)
    {
      _formerOwner = clan;
      if (Config.REMOVE_CASTLE_CIRCLETS)
      {
        CastleManager.getInstance().removeCirclet(_formerOwner, getCastleId());
      }
      clan.setHasCastle(0);
      Announcements.getInstance().announceToAll(clan.getName() + " has lost " + getName() + " castle");
      clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
    }

    updateOwnerInDB(null);
    if (getSiege().getIsInProgress()) {
      getSiege().midVictory();
    }
    updateClansReputation();
    for (Map.Entry fc : _function.entrySet())
      removeFunction(((Integer)fc.getKey()).intValue());
    _function.clear();
  }

  public void setTaxPercent(L2PcInstance activeChar, int taxPercent)
  {
    int maxTax;
    switch (SevenSigns.getInstance().getSealOwner(3))
    {
    case 2:
      maxTax = 25;
      break;
    case 1:
      maxTax = 5;
      break;
    default:
      maxTax = 15;
    }

    if ((taxPercent < 0) || (taxPercent > maxTax))
    {
      activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
      return;
    }

    setTaxPercent(taxPercent);
    activeChar.sendMessage(getName() + " castle tax changed to " + taxPercent + "%.");
  }

  public void setTaxPercent(int taxPercent)
  {
    _taxPercent = taxPercent;
    _taxRate = (_taxPercent / 100.0D);

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Update castle set taxPercent = ? where id = ?");
      statement.setInt(1, taxPercent);
      statement.setInt(2, getCastleId());
      statement.execute();
      statement.close(); } catch (Exception e) {
    } finally {
      try {
        con.close();
      }
      catch (Exception e) {
      }
    }
  }

  public void spawnDoor() {
    spawnDoor(false);
  }

  public void spawnDoor(boolean isDoorWeak)
  {
    for (int i = 0; i < getDoors().size(); i++)
    {
      L2DoorInstance door = (L2DoorInstance)getDoors().get(i);
      if (door.getCurrentHp() <= 0.0D)
      {
        door.decayMe();
        door = DoorTable.parseList((String)_doorDefault.get(i));
        if (isDoorWeak)
          door.setCurrentHp(door.getMaxHp() / 2);
        door.spawnMe(door.getX(), door.getY(), door.getZ());
        getDoors().set(i, door);
      }
      else if (!door.getOpen()) {
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
    if ((door != null) && (door.getDoorId() == doorId))
    {
      door.setCurrentHp(door.getMaxHp() + hp);

      saveDoorUpgrade(doorId, hp, pDef, mDef);
      return;
    }
  }

  private void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("Select * from castle where id = ?");
      statement.setInt(1, getCastleId());
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
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

      statement.close();

      _taxRate = (_taxPercent / 100.0D);

      statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?");
      statement.setInt(1, getCastleId());
      rs = statement.executeQuery();

      while (rs.next())
      {
        _ownerId = rs.getInt("clan_id");
      }

      if (getOwnerId() > 0)
      {
        L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());
        ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000L);
      }

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadCastleData(): " + e.getMessage());
      e.printStackTrace(); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void loadFunctions() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Select * from castle_functions where castle_id = ?");
      statement.setInt(1, getCastleId());
      ResultSet rs = statement.executeQuery();
      while (rs.next())
      {
        _function.put(Integer.valueOf(rs.getInt("type")), new CastleFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
      }
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: Castle.loadFunctions(): " + e.getMessage(), e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void removeFunction(int functionType) {
    _function.remove(Integer.valueOf(functionType));
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM castle_functions WHERE castle_id=? AND type=?");
      statement.setInt(1, getCastleId());
      statement.setInt(2, functionType);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: Castle.removeFunctions(int functionType): " + e.getMessage(), e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public boolean updateFunctions(L2PcInstance player, int type, int lvl, int lease, long rate, boolean addNew) {
    if (player == null)
      return false;
    if (Config.DEBUG)
      _log.warning("Called Castle.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());
    if ((lease > 0) && 
      (!player.destroyItemByItemId("Consume", 57, lease, null, true)))
      return false;
    if (addNew)
    {
      _function.put(Integer.valueOf(type), new CastleFunction(type, lvl, lease, 0, rate, 0L, false));
    }
    else if ((lvl == 0) && (lease == 0)) {
      removeFunction(type);
    }
    else {
      int diffLease = lease - ((CastleFunction)_function.get(Integer.valueOf(type))).getLease();
      if (Config.DEBUG)
        _log.warning("Called Castle.updateFunctions diffLease : " + diffLease);
      if (diffLease > 0)
      {
        _function.remove(Integer.valueOf(type));
        _function.put(Integer.valueOf(type), new CastleFunction(type, lvl, lease, 0, rate, -1L, false));
      }
      else
      {
        ((CastleFunction)_function.get(Integer.valueOf(type))).setLease(lease);
        ((CastleFunction)_function.get(Integer.valueOf(type))).setLvl(lvl);
        ((CastleFunction)_function.get(Integer.valueOf(type))).dbSave(false);
      }
    }

    return true;
  }

  private void loadDoor()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Select * from castle_door where castleId = ?");
      statement.setInt(1, getCastleId());
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        _doorDefault.add(rs.getString("name") + ";" + rs.getInt("id") + ";" + rs.getInt("x") + ";" + rs.getInt("y") + ";" + rs.getInt("z") + ";" + rs.getInt("range_xmin") + ";" + rs.getInt("range_ymin") + ";" + rs.getInt("range_zmin") + ";" + rs.getInt("range_xmax") + ";" + rs.getInt("range_ymax") + ";" + rs.getInt("range_zmax") + ";" + rs.getInt("hp") + ";" + rs.getInt("pDef") + ";" + rs.getInt("mDef"));

        L2DoorInstance door = DoorTable.parseList((String)_doorDefault.get(_doorDefault.size() - 1));
        door.spawnMe(door.getX(), door.getY(), door.getZ());
        _doors.add(door);
        DoorTable.getInstance().putDoor(door);
      }

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadCastleDoor(): " + e.getMessage());
      e.printStackTrace(); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void loadDoorUpgrade() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Select * from castle_doorupgrade where doorId in (Select Id from castle_door where castleId = ?)");
      statement.setInt(1, getCastleId());
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
      }

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadCastleDoorUpgrade(): " + e.getMessage());
      e.printStackTrace(); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void removeDoorUpgrade() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("delete from castle_doorupgrade where doorId in (select id from castle_door where castleId=?)");
      statement.setInt(1, getCastleId());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Exception: removeDoorUpgrade(): " + e.getMessage());
      e.printStackTrace(); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
      statement.setInt(1, doorId);
      statement.setInt(2, hp);
      statement.setInt(3, pDef);
      statement.setInt(4, mDef);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void updateOwnerInDB(L2Clan clan) {
    if (clan != null)
      _ownerId = clan.getClanId();
    else {
      _ownerId = 0;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
      statement.setInt(1, getCastleId());
      statement.execute();
      statement.close();

      statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
      statement.setInt(1, getCastleId());
      statement.setInt(2, getOwnerId());
      statement.execute();
      statement.close();

      if (clan != null)
      {
        clan.setHasCastle(getCastleId());
        Announcements.getInstance().announceToAll(clan.getName() + " has taken " + getName() + " castle!");
        clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
        clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
        CrownManager.getInstance().checkCrowns(clan);
        ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000L);
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e) {
      }
    }
  }

  public final int getCastleId() {
    return _castleId;
  }

  public final L2DoorInstance getDoor(int doorId)
  {
    if (doorId <= 0) {
      return null;
    }
    for (int i = 0; i < getDoors().size(); i++)
    {
      L2DoorInstance door = (L2DoorInstance)getDoors().get(i);
      if (door.getDoorId() == doorId)
        return door;
    }
    return null;
  }

  public final List<L2DoorInstance> getDoors()
  {
    return _doors;
  }

  public final String getName()
  {
    return _name;
  }

  public final int getOwnerId()
  {
    return _ownerId;
  }

  public final Siege getSiege()
  {
    if (_siege == null) _siege = new Siege(new Castle[] { this });
    return _siege;
  }
  public final Calendar getSiegeDate() {
    return _siegeDate;
  }
  public final int getSiegeDayOfWeek() { return _siegeDayOfWeek; } 
  public final int getSiegeHourOfDay() {
    return _siegeHourOfDay;
  }

  public final int getTaxPercent() {
    return _taxPercent;
  }

  public final double getTaxRate()
  {
    return _taxRate;
  }

  public final int getTreasury()
  {
    return _treasury;
  }

  public FastList<CastleManorManager.SeedProduction> getSeedProduction(int period)
  {
    return period == 0 ? _production : _productionNext;
  }

  public FastList<CastleManorManager.CropProcure> getCropProcure(int period)
  {
    return period == 0 ? _procure : _procureNext;
  }

  public void setSeedProduction(FastList<CastleManorManager.SeedProduction> seed, int period)
  {
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
    for (CastleManorManager.SeedProduction seed : getSeedProduction(period))
    {
      if (seed.getId() == seedId)
      {
        return seed;
      }
    }
    return null;
  }

  public synchronized CastleManorManager.CropProcure getCrop(int cropId, int period)
  {
    for (CastleManorManager.CropProcure crop : getCropProcure(period))
    {
      if (crop.getId() == cropId)
      {
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
    if (period == 0)
    {
      FastList procure = _procure;
      production = _production;
    } else {
      procure = _procureNext;
      production = _productionNext;
    }

    int total = 0;
    if (production != null)
    {
      for (CastleManorManager.SeedProduction seed : production)
      {
        total += L2Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
      }
    }
    if (procure != null)
    {
      for (CastleManorManager.CropProcure crop : procure)
      {
        total += crop.getPrice() * crop.getStartAmount();
      }
    }
    return total;
  }

  public void saveSeedData()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
      statement.setInt(1, getCastleId());

      statement.execute();
      statement.close();

      if (_production != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_production VALUES ";
        String[] values = new String[_production.size()];
        for (CastleManorManager.SeedProduction s : _production)
        {
          values[count] = ("(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 0 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
          {
            query = query + "," + values[i];
          }
          statement = con.prepareStatement(query);
          statement.execute();
          statement.close();
        }
      }

      if (_productionNext != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_production VALUES ";
        String[] values = new String[_productionNext.size()];
        for (CastleManorManager.SeedProduction s : _productionNext)
        {
          values[count] = ("(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 1 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
          {
            query = query + "," + values[i];
          }
          statement = con.prepareStatement(query);
          statement.execute();
          statement.close();
        }
      }
    }
    catch (Exception e) {
      _log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage()); } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void saveSeedData(int period) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
      statement.setInt(1, getCastleId());
      statement.setInt(2, period);
      statement.execute();
      statement.close();

      FastList prod = null;
      prod = getSeedProduction(period);

      if (prod != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_production VALUES ";
        String[] values = new String[prod.size()];
        for (CastleManorManager.SeedProduction s : prod)
        {
          values[count] = ("(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
          {
            query = query + "," + values[i];
          }
          statement = con.prepareStatement(query);
          statement.execute();
          statement.close();
        }
      }
    }
    catch (Exception e) {
      _log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage()); } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void saveCropData() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
      statement.setInt(1, getCastleId());
      statement.execute();
      statement.close();
      if (_procure != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_procure VALUES ";
        String[] values = new String[_procure.size()];
        for (CastleManorManager.CropProcure cp : _procure)
        {
          values[count] = ("(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 0 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
          {
            query = query + "," + values[i];
          }
          statement = con.prepareStatement(query);
          statement.execute();
          statement.close();
        }
      }
      if (_procureNext != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_procure VALUES ";
        String[] values = new String[_procureNext.size()];
        for (CastleManorManager.CropProcure cp : _procureNext)
        {
          values[count] = ("(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 1 + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
          {
            query = query + "," + values[i];
          }
          statement = con.prepareStatement(query);
          statement.execute();
          statement.close();
        }
      }
    } catch (Exception e) {
      _log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
    } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void saveCropData(int period) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
      statement.setInt(1, getCastleId());
      statement.setInt(2, period);
      statement.execute();
      statement.close();

      FastList proc = null;
      proc = getCropProcure(period);

      if (proc != null)
      {
        int count = 0;
        String query = "INSERT INTO castle_manor_procure VALUES ";
        String[] values = new String[proc.size()];

        for (CastleManorManager.CropProcure cp : proc)
        {
          values[count] = ("(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")");
          count++;
        }
        if (values.length > 0)
        {
          query = query + values[0];
          for (int i = 1; i < values.length; i++)
          {
            query = query + "," + values[i];
          }
          statement = con.prepareStatement(query);
          statement.execute();
          statement.close();
        }
      }
    } catch (Exception e) {
      _log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
    } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void updateCrop(int cropId, int amount, int period) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
      statement.setInt(1, amount);
      statement.setInt(2, cropId);
      statement.setInt(3, getCastleId());
      statement.setInt(4, period);
      statement.execute();
      statement.close();
    } catch (Exception e) {
      _log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
    } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void updateSeed(int seedId, int amount, int period) {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
      statement.setInt(1, amount);
      statement.setInt(2, seedId);
      statement.setInt(3, getCastleId());
      statement.setInt(4, period);
      statement.execute();
      statement.close();
    } catch (Exception e) {
      _log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
    } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public boolean isNextPeriodApproved() {
    return _isNextPeriodApproved;
  }

  public void setNextPeriodApproved(boolean val)
  {
    _isNextPeriodApproved = val;
  }

  public void updateClansReputation()
  {
    if (_formerOwner != null)
    {
      if (_formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
      {
        int maxreward = Math.max(0, _formerOwner.getReputationScore());
        _formerOwner.setReputationScore(_formerOwner.getReputationScore() - Config.BONUS_CLAN_SCORE_SIEGE, true);
        L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
        if (owner != null)
        {
          owner.setReputationScore(owner.getReputationScore() + Math.min(Config.BONUS_CLAN_SCORE_SIEGE, maxreward), true);
          owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
        }
      }
      else {
        _formerOwner.setReputationScore(_formerOwner.getReputationScore() + Config.CLAN_SCORE_SIEGE, true);
      }
      _formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_formerOwner));
    }
    else
    {
      L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
      if (owner != null)
      {
        owner.setReputationScore(owner.getReputationScore() + Config.BONUS_CLAN_SCORE_SIEGE, true);
        owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
      }
    }
  }

  public class CastleFunction
  {
    private int _type;
    private int _lvl;
    protected int _fee;
    protected int _tempFee;
    private long _rate;
    private long _endDate;
    protected boolean _inDebt;
    public boolean _cwh;

    public CastleFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
    {
      _type = type;
      _lvl = lvl;
      _fee = lease;
      _tempFee = tempLease;
      _rate = rate;
      _endDate = time;
      initializeTask(cwh);
    }
    public int getType() {
      return _type; } 
    public int getLvl() { return _lvl; } 
    public int getLease() { return _fee; } 
    public long getRate() { return _rate; } 
    public long getEndTime() { return _endDate; } 
    public void setLvl(int lvl) { _lvl = lvl; } 
    public void setLease(int lease) { _fee = lease; } 
    public void setEndTime(long time) { _endDate = time; }

    private void initializeTask(boolean cwh)
    {
      if (getOwnerId() <= 0)
        return;
      long currentTime = System.currentTimeMillis();
      if (_endDate > currentTime)
        ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), _endDate - currentTime);
      else
        ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0L);
    }

    public void dbSave(boolean newFunction)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement;
        if (newFunction)
        {
          PreparedStatement statement = con.prepareStatement("INSERT INTO castle_functions (castle_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
          statement.setInt(1, getCastleId());
          statement.setInt(2, getType());
          statement.setInt(3, getLvl());
          statement.setInt(4, getLease());
          statement.setLong(5, getRate());
          statement.setLong(6, getEndTime());
        }
        else
        {
          statement = con.prepareStatement("UPDATE castle_functions SET lvl=?, lease=?, endTime=? WHERE castle_id=? AND type=?");
          statement.setInt(1, getLvl());
          statement.setInt(2, getLease());
          statement.setLong(3, getEndTime());
          statement.setInt(4, getCastleId());
          statement.setInt(5, getType());
        }
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        Castle._log.log(Level.SEVERE, "Exception: Castle.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e); } finally {
        try {
          con.close();
        }
        catch (Exception e)
        {
        }
      }
    }

    private class FunctionTask
      implements Runnable
    {
      public FunctionTask(boolean cwh)
      {
        _cwh = cwh;
      }

      public void run()
      {
        try {
          if (getOwnerId() <= 0)
            return;
          if ((ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee) || (!_cwh))
          {
            int fee = _fee;
            boolean newfc = true;
            if ((getEndTime() == 0L) || (getEndTime() == -1L))
            {
              if (getEndTime() == -1L)
              {
                newfc = false;
                fee = _tempFee;
              }
            }
            else newfc = false;
            setEndTime(System.currentTimeMillis() + getRate());
            dbSave(newfc);
            if (_cwh)
            {
              ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CS_function_fee", 57, fee, null, null);
              if (Config.DEBUG)
                Castle._log.warning("deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
            }
            ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(Castle.CastleFunction.this, true), getRate());
          } else {
            removeFunction(getType());
          }
        }
        catch (Throwable t)
        {
        }
      }
    }
  }
}