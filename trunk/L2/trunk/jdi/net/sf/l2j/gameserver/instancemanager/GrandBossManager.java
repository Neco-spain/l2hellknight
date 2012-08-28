package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.L2FastList;

public class GrandBossManager
{
  private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
  private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
  private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";
  private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ?, respawn_time = ? where boss_id = ?";
  private static Logger _log = Logger.getLogger(GrandBossManager.class.getName());
  private static GrandBossManager _instance;
  protected static Map<Integer, L2GrandBossInstance> _bosses;
  protected static Map<Integer, StatsSet> _storedInfo;
  private Map<Integer, Integer> _bossStatus;
  private L2FastList<L2BossZone> _zones;

  public static final GrandBossManager getInstance()
  {
    if (_instance == null)
    {
      _log.info("Initializing GrandBossManager");
      _instance = new GrandBossManager();
    }
    return _instance;
  }

  public GrandBossManager()
  {
    init();
  }

  private void init()
  {
    _zones = new L2FastList();

    _bosses = new FastMap();
    _storedInfo = new FastMap();
    _bossStatus = new FastMap();
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_data ORDER BY boss_id");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        StatsSet info = new StatsSet();
        int bossId = rset.getInt("boss_id");
        info.set("loc_x", rset.getInt("loc_x"));
        info.set("loc_y", rset.getInt("loc_y"));
        info.set("loc_z", rset.getInt("loc_z"));
        info.set("heading", rset.getInt("heading"));
        info.set("respawn_time", rset.getLong("respawn_time"));
        double HP = rset.getDouble("currentHP");
        int true_HP = (int)HP;
        info.set("currentHP", true_HP);
        double MP = rset.getDouble("currentMP");
        int true_MP = (int)MP;
        info.set("currentMP", true_MP);
        _bossStatus.put(Integer.valueOf(bossId), Integer.valueOf(rset.getInt("status")));

        _storedInfo.put(Integer.valueOf(bossId), info);
        info = null;
      }

      _log.info("GrandBossManager: Loaded " + _storedInfo.size() + " Instances");

      rset.close();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("GrandBossManager: Could not load grandboss_data table");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public void initZones()
  {
    Connection con = null;

    FastMap zones = new FastMap();

    if (_zones == null)
    {
      _log.warning("GrandBossManager: Could not read Grand Boss zone data");
      return;
    }

    for (L2BossZone zone : _zones)
    {
      if (zone == null)
        continue;
      zones.put(Integer.valueOf(zone.getId()), new L2FastList());
    }

    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_list ORDER BY player_id");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("player_id");
        int zone_id = rset.getInt("zone");
        ((L2FastList)zones.get(Integer.valueOf(zone_id))).add(Integer.valueOf(id));
      }

      rset.close();
      statement.close();

      _log.info("GrandBossManager: Initialized " + _zones.size() + " Grand Boss Zones");
    }
    catch (SQLException e)
    {
      _log.warning("GrandBossManager: Could not load grandboss_list table");
      e.getMessage();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }

    for (L2BossZone zone : _zones)
    {
      if (zone == null)
        continue;
      zone.setAllowedPlayers((L2FastList)zones.get(Integer.valueOf(zone.getId())));
    }

    zones.clear();
  }

  public void addZone(L2BossZone zone)
  {
    if (_zones != null)
    {
      _zones.add(zone);
    }
  }

  public final L2BossZone getZone(L2Character character)
  {
    if (_zones != null)
      for (L2BossZone temp : _zones)
      {
        if (temp.isCharacterInZone(character))
        {
          return temp;
        }
      }
    return null;
  }

  public final L2BossZone getZone(int x, int y, int z)
  {
    if (_zones != null)
      for (L2BossZone temp : _zones)
      {
        if (temp.isInsideZone(x, y, z))
        {
          return temp;
        }
      }
    return null;
  }

  public boolean checkIfInZone(String zoneType, L2Object obj)
  {
    L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
    if (temp == null)
    {
      return false;
    }
    return temp.getZoneName().equalsIgnoreCase(zoneType);
  }

  public int getBossStatus(int bossId)
  {
    return ((Integer)_bossStatus.get(Integer.valueOf(bossId))).intValue();
  }

  public void setBossStatus(int bossId, int status)
  {
    _bossStatus.remove(Integer.valueOf(bossId));
    _bossStatus.put(Integer.valueOf(bossId), Integer.valueOf(status));
    updateDb(bossId, true);
  }

  public void addBoss(L2GrandBossInstance boss)
  {
    if (boss != null)
    {
      if (_bosses.containsKey(Integer.valueOf(boss.getNpcId())))
        _bosses.remove(Integer.valueOf(boss.getNpcId()));
      _bosses.put(Integer.valueOf(boss.getNpcId()), boss);
    }
  }

  public L2GrandBossInstance getBoss(int bossId)
  {
    return (L2GrandBossInstance)_bosses.get(Integer.valueOf(bossId));
  }

  public StatsSet getStatsSet(int bossId)
  {
    return (StatsSet)_storedInfo.get(Integer.valueOf(bossId));
  }

  public void setStatsSet(int bossId, StatsSet info)
  {
    if (_storedInfo.containsKey(Integer.valueOf(bossId)))
      _storedInfo.remove(Integer.valueOf(bossId));
    _storedInfo.put(Integer.valueOf(bossId), info);
    updateDb(bossId, false);
  }

  private void storeToDb()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM grandboss_list");
      statement.executeUpdate();
      statement.close();

      statement = con.prepareStatement("INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)");
      for (L2BossZone zone : _zones)
      {
        if (zone == null)
          continue;
        id = Integer.valueOf(zone.getId());
        L2FastList list = zone.getAllowedPlayers();
        if ((list == null) || (list.isEmpty()))
          continue;
        for (Integer player : list)
        {
          statement.setInt(1, player.intValue());
          statement.setInt(2, id.intValue());
          statement.executeUpdate();
          statement.clearParameters();
        }
      }
      Integer id;
      statement.close();

      for (Integer bossId : _storedInfo.keySet())
      {
        L2GrandBossInstance boss = (L2GrandBossInstance)_bosses.get(bossId);
        StatsSet info = (StatsSet)_storedInfo.get(bossId);
        if ((boss == null) || (info == null))
        {
          statement = con.prepareStatement("UPDATE grandboss_data set status = ?, respawn_time = ? where boss_id = ?");
          statement.setInt(1, ((Integer)_bossStatus.get(bossId)).intValue());
          statement.setLong(2, info.getLong("respawn_time"));
          statement.setInt(3, bossId.intValue());
        }
        else
        {
          statement = con.prepareStatement("UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?");
          statement.setInt(1, boss.getX());
          statement.setInt(2, boss.getY());
          statement.setInt(3, boss.getZ());
          statement.setInt(4, boss.getHeading());
          statement.setLong(5, info.getLong("respawn_time"));
          double hp = boss.getCurrentHp();
          double mp = boss.getCurrentMp();
          if (boss.isDead())
          {
            hp = boss.getMaxHp();
            mp = boss.getMaxMp();
          }
          statement.setDouble(6, hp);
          statement.setDouble(7, mp);
          statement.setInt(8, ((Integer)_bossStatus.get(bossId)).intValue());
          statement.setInt(9, bossId.intValue());
        }
        statement.executeUpdate();
        statement.close();
      }
    }
    catch (SQLException e)
    {
      _log.warning("GrandBossManager: Couldn't store grandbosses to database:" + e);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  private void updateDb(int bossId, boolean statusOnly)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = null;
      L2GrandBossInstance boss = (L2GrandBossInstance)_bosses.get(Integer.valueOf(bossId));
      StatsSet info = (StatsSet)_storedInfo.get(Integer.valueOf(bossId));

      if ((statusOnly) || (boss == null) || (info == null))
      {
        statement = con.prepareStatement("UPDATE grandboss_data set status = ?, respawn_time = ? where boss_id = ?");
        statement.setInt(1, ((Integer)_bossStatus.get(Integer.valueOf(bossId))).intValue());
        statement.setLong(2, info.getLong("respawn_time"));
        statement.setInt(3, bossId);
      }
      else
      {
        statement = con.prepareStatement("UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?");
        statement.setInt(1, boss.getX());
        statement.setInt(2, boss.getY());
        statement.setInt(3, boss.getZ());
        statement.setInt(4, boss.getHeading());
        statement.setLong(5, info.getLong("respawn_time"));
        double hp = boss.getCurrentHp();
        double mp = boss.getCurrentMp();
        if (boss.isDead())
        {
          hp = boss.getMaxHp();
          mp = boss.getMaxMp();
        }
        statement.setDouble(6, hp);
        statement.setDouble(7, mp);
        statement.setInt(8, ((Integer)_bossStatus.get(Integer.valueOf(bossId))).intValue());
        statement.setInt(9, bossId);
      }
      statement.executeUpdate();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("GrandBossManager: Couldn't update grandbosses to database: " + e);
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public void cleanUp()
  {
    storeToDb();

    _bosses.clear();
    _storedInfo.clear();
    _bossStatus.clear();
    _zones.clear();
  }

  public synchronized void save()
  {
    storeToDb();
  }

  public long getInterval(int bossId)
  {
    long interval = getStatsSet(bossId).getLong("respawn_time") - Calendar.getInstance().getTimeInMillis();

    if (interval < 0L) {
      return 0L;
    }
    return interval;
  }
}