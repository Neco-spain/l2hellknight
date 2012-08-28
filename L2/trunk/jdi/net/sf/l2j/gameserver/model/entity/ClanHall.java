package net.sf.l2j.gameserver.model.entity;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ClanHall
{
  protected static final Logger _log = Logger.getLogger(ClanHall.class.getName());
  private int _clanHallId;
  private List<L2DoorInstance> _doors;
  private List<String> _doorDefault;
  private String _name;
  private int _ownerId;
  private int _lease;
  private String _desc;
  private String _location;
  protected long _paidUntil;
  private L2ClanHallZone _zone;
  private int _grade;
  protected final int _chRate = Config.CH_RATE;
  protected boolean _isFree = true;
  private Map<Integer, ClanHallFunction> _functions;
  protected boolean _paid;
  public static final int FUNC_TELEPORT = 1;
  public static final int FUNC_ITEM_CREATE = 2;
  public static final int FUNC_RESTORE_HP = 3;
  public static final int FUNC_RESTORE_MP = 4;
  public static final int FUNC_RESTORE_EXP = 5;
  public static final int FUNC_SUPPORT = 6;
  public static final int FUNC_DECO_FRONTPLATEFORM = 7;
  public static final int FUNC_DECO_CURTAINS = 8;

  public ClanHall(int clanHallId, String name, int ownerId, int lease, String desc, String location, long paidUntil, int Grade, boolean paid)
  {
    _clanHallId = clanHallId;
    _name = name;
    _ownerId = ownerId;
    if (Config.DEBUG)
      _log.warning("Init Owner : " + _ownerId);
    _lease = lease;
    _desc = desc;
    _location = location;
    _paidUntil = paidUntil;
    _grade = Grade;
    _paid = paid;
    _doorDefault = new FastList();
    _functions = new FastMap();

    if (ownerId != 0)
    {
      _isFree = false;
      initialyzeTask(false);
      loadFunctions();
    }
  }

  public final boolean getPaid()
  {
    return _paid;
  }

  public final int getId()
  {
    return _clanHallId;
  }

  public final String getName()
  {
    return _name;
  }

  public final int getOwnerId()
  {
    return _ownerId;
  }

  public final int getLease()
  {
    return _lease;
  }

  public final String getDesc()
  {
    return _desc;
  }

  public final String getLocation()
  {
    return _location;
  }

  public final long getPaidUntil()
  {
    return _paidUntil;
  }

  public final int getGrade()
  {
    return _grade;
  }

  public final List<L2DoorInstance> getDoors()
  {
    if (_doors == null) _doors = new FastList();
    return _doors;
  }

  public final L2DoorInstance getDoor(int doorId)
  {
    if (doorId <= 0) return null;
    for (int i = 0; i < getDoors().size(); i++)
    {
      L2DoorInstance door = (L2DoorInstance)getDoors().get(i);
      if (door.getDoorId() == doorId) return door;
    }
    return null;
  }

  public ClanHallFunction getFunction(int type)
  {
    if (_functions.get(Integer.valueOf(type)) != null)
      return (ClanHallFunction)_functions.get(Integer.valueOf(type));
    return null;
  }

  public void setZone(L2ClanHallZone zone)
  {
    _zone = zone;
  }

  public L2ClanHallZone getZone()
  {
    return _zone;
  }

  public void free()
  {
    _ownerId = 0;
    _isFree = true;
    for (Map.Entry fc : _functions.entrySet())
      removeFunction(((Integer)fc.getKey()).intValue());
    _functions.clear();
    _paidUntil = 0L;
    _paid = false;
    updateDb();
  }

  public void setOwner(L2Clan clan)
  {
    if ((_ownerId > 0) || (clan == null))
      return;
    _ownerId = clan.getClanId();
    _isFree = false;
    _paidUntil = System.currentTimeMillis();
    initialyzeTask(true);

    clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
    updateDb();
  }

  public void spawnDoor()
  {
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
        if (isDoorWeak) door.setCurrentHp(door.getMaxHp() / 2);
        door.spawnMe(door.getX(), door.getY(), door.getZ());
        getDoors().set(i, door);
      }
      else if (!door.getOpen()) {
        door.closeMe();
      }
    }
  }

  public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
  {
    if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
      openCloseDoor(doorId, open);
  }

  public void openCloseDoor(int doorId, boolean open)
  {
    openCloseDoor(getDoor(doorId), open);
  }

  public void openCloseDoor(L2DoorInstance door, boolean open)
  {
    if (door != null)
      if (open) door.openMe(); else
        door.closeMe();
  }

  public void openCloseDoors(L2PcInstance activeChar, boolean open)
  {
    if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
      openCloseDoors(open);
  }

  public void openCloseDoors(boolean open)
  {
    for (L2DoorInstance door : getDoors())
    {
      if (door != null)
        if (open) door.openMe(); else
          door.closeMe();
    }
  }

  public void banishForeigners()
  {
    _zone.banishForeigners(getOwnerId());
  }

  private void loadFunctions()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("Select * from clanhall_functions where hall_id = ?");
      statement.setInt(1, getId());
      ResultSet rs = statement.executeQuery();
      while (rs.next())
      {
        _functions.put(Integer.valueOf(rs.getInt("type")), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime")));
      }
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: ClanHall.loadFunctions(): " + e.getMessage(), e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void removeFunction(int functionType) {
    _functions.remove(Integer.valueOf(functionType));
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
      statement.setInt(1, getId());
      statement.setInt(2, functionType);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public boolean updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) {
    if (Config.DEBUG)
      _log.warning("Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());
    if (addNew)
    {
      if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() < lease)
        return false;
      _functions.put(Integer.valueOf(type), new ClanHallFunction(type, lvl, lease, 0, rate, 0L));
    }
    else if ((lvl == 0) && (lease == 0)) {
      removeFunction(type);
    }
    else {
      int diffLease = lease - ((ClanHallFunction)_functions.get(Integer.valueOf(type))).getLease();
      if (Config.DEBUG)
        _log.warning("Called ClanHall.updateFunctions diffLease : " + diffLease);
      if (diffLease > 0)
      {
        if (ClanTable.getInstance().getClan(_ownerId).getWarehouse().getAdena() < diffLease)
          return false;
        _functions.remove(Integer.valueOf(type));
        _functions.put(Integer.valueOf(type), new ClanHallFunction(type, lvl, lease, diffLease, rate, -1L));
      } else {
        ((ClanHallFunction)_functions.get(Integer.valueOf(type))).setLease(lease);
        ((ClanHallFunction)_functions.get(Integer.valueOf(type))).setLvl(lvl);
        ((ClanHallFunction)_functions.get(Integer.valueOf(type))).dbSave(false);
      }
    }

    return true;
  }

  public void updateDb()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
      statement.setInt(1, _ownerId);
      statement.setLong(2, _paidUntil);
      statement.setInt(3, _paid ? 1 : 0);
      statement.setInt(4, _clanHallId);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      System.out.println("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  private void initialyzeTask(boolean forced) {
    long currentTime = System.currentTimeMillis();
    if (_paidUntil > currentTime)
      ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - currentTime);
    else if ((!_paid) && (!forced)) {
      if (System.currentTimeMillis() + 86400000L <= _paidUntil + _chRate)
        ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), System.currentTimeMillis() + 86400000L);
      else
        ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil + _chRate - System.currentTimeMillis());
    }
    else ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0L); 
  }

  private class FeeTask implements Runnable
  {
    public FeeTask()
    {
    }

    public void run()
    {
      try {
        if (_isFree)
          return;
        L2Clan Clan = ClanTable.getInstance().getClan(getOwnerId());
        if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
        {
          if (_paidUntil != 0L) {
            while (_paidUntil < System.currentTimeMillis()) _paidUntil += _chRate;
          }
          _paidUntil = (System.currentTimeMillis() + _chRate);
          ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
          if (Config.DEBUG)
            ClanHall._log.warning("deducted " + getLease() + " adena from " + getName() + " owner's cwh for ClanHall _paidUntil" + _paidUntil);
          ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(ClanHall.this), _paidUntil - System.currentTimeMillis());
          _paid = true;
          updateDb();
        }
        else
        {
          _paid = false;
          if (System.currentTimeMillis() > _paidUntil + _chRate) {
            if ((GameServer.gameServer.getCHManager() != null) && (GameServer.gameServer.getCHManager().loaded())) {
              AuctionManager.getInstance().initNPC(getId());
              ClanHallManager.getInstance().setFree(getId());
              Clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
            } else {
              ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(ClanHall.this), 3000L);
            }
          } else {
            updateDb();
            SystemMessage sm = new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
            sm.addNumber(getLease());
            Clan.broadcastToOnlineMembers(sm);
            if (System.currentTimeMillis() + 86400000L <= _paidUntil + _chRate)
              ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(ClanHall.this), System.currentTimeMillis() + 86400000L);
            else
              ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(ClanHall.this), _paidUntil + _chRate - System.currentTimeMillis());
          }
        }
      }
      catch (Throwable t)
      {
      }
    }
  }

  public class ClanHallFunction
  {
    private int _type;
    private int _lvl;
    protected int _fee;
    protected int _tempFee;
    private long _rate;
    private long _endDate;
    protected boolean _inDebt;

    public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time)
    {
      _type = type;
      _lvl = lvl;
      _fee = lease;
      _tempFee = tempLease;
      _rate = rate;
      _endDate = time;
      initializeTask();
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

    private void initializeTask()
    {
      if (_isFree)
        return;
      long currentTime = System.currentTimeMillis();
      if (_endDate > currentTime)
        ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), _endDate - currentTime);
      else
        ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), 0L);
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
          PreparedStatement statement = con.prepareStatement("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
          statement.setInt(1, getId());
          statement.setInt(2, getType());
          statement.setInt(3, getLvl());
          statement.setInt(4, getLease());
          statement.setLong(5, getRate());
          statement.setLong(6, getEndTime());
        }
        else
        {
          statement = con.prepareStatement("UPDATE clanhall_functions SET lvl=?, lease=?, endTime=? WHERE hall_id=? AND type=?");
          statement.setInt(1, getLvl());
          statement.setInt(2, getLease());
          statement.setLong(3, getEndTime());
          statement.setInt(4, getId());
          statement.setInt(5, getType());
        }
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        ClanHall._log.log(Level.SEVERE, "Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e); } finally {
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
      public FunctionTask()
      {
      }

      public void run()
      {
        try
        {
          if (_isFree)
            return;
          if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee)
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
            ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, fee, null, null);
            if (Config.DEBUG)
              ClanHall._log.warning("deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
            ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(ClanHall.ClanHallFunction.this), getRate());
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