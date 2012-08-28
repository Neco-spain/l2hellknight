package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class RaidBossSpawnManager
{
  private static Logger _log = Logger.getLogger(RaidBossSpawnManager.class.getName());
  private static RaidBossSpawnManager _instance;
  protected static Map<Integer, L2RaidBossInstance> _bosses;
  protected static Map<Integer, L2Spawn> _spawns;
  protected static Map<Integer, StatsSet> _storedInfo;
  protected static Map<Integer, ScheduledFuture> _schedules;

  public RaidBossSpawnManager()
  {
    init();
  }

  public static RaidBossSpawnManager getInstance()
  {
    if (_instance == null) {
      _instance = new RaidBossSpawnManager();
    }
    return _instance;
  }

  private void init()
  {
    _bosses = new FastMap();
    _schedules = new FastMap();
    _storedInfo = new FastMap();
    _spawns = new FastMap();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT * from raidboss_spawnlist ORDER BY boss_id");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template = getValidTemplate(rset.getInt("boss_id"));
        if (template != null)
        {
          L2Spawn spawnDat = new L2Spawn(template);
          spawnDat.setLocx(rset.getInt("loc_x"));
          spawnDat.setLocy(rset.getInt("loc_y"));
          spawnDat.setLocz(rset.getInt("loc_z"));
          spawnDat.setAmount(rset.getInt("amount"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnMinDelay(rset.getInt("respawn_min_delay"));
          spawnDat.setRespawnMaxDelay(rset.getInt("respawn_max_delay"));
          long respawnTime = rset.getLong("respawn_time");

          addNewSpawn(spawnDat, respawnTime, rset.getDouble("currentHP"), rset.getDouble("currentMP"), false); continue;
        }

        _log.warning("RaidBossSpawnManager: Could not load raidboss #" + rset.getInt("boss_id") + " from DB");
      }

      _log.info("RaidBossSpawnManager: Loaded " + _bosses.size() + " Instances");
      _log.info("RaidBossSpawnManager: Scheduled " + _schedules.size() + " Instances");

      rset.close();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("RaidBossSpawnManager: Couldnt load raidboss_spawnlist table");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        con.close(); } catch (Exception e) { e.printStackTrace();
      }
    }
  }

  public void updateStatus(L2RaidBossInstance boss, boolean isBossDead)
  {
    if (!_storedInfo.containsKey(Integer.valueOf(boss.getNpcId()))) {
      return;
    }
    StatsSet info = (StatsSet)_storedInfo.get(Integer.valueOf(boss.getNpcId()));

    if (isBossDead)
    {
      boss.setRaidStatus(StatusEnum.DEAD);

      int RespawnMinDelay = boss.getSpawn().getRespawnMinDelay();
      int RespawnMaxDelay = boss.getSpawn().getRespawnMaxDelay();
      long respawn_delay = Rnd.get((int)(RespawnMinDelay * 1000 * Config.RAID_MIN_RESPAWN_MULTIPLIER), (int)(RespawnMaxDelay * 1000 * Config.RAID_MAX_RESPAWN_MULTIPLIER));
      long respawnTime = Calendar.getInstance().getTimeInMillis() + respawn_delay;

      info.set("currentHP", boss.getMaxHp());
      info.set("currentMP", boss.getMaxMp());
      info.set("respawnTime", respawnTime);

      _log.info("RaidBossSpawnManager: Updated " + boss.getName() + " respawn time to " + respawnTime);

      ScheduledFuture futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new spawnSchedule(boss.getNpcId()), respawn_delay);

      _schedules.put(Integer.valueOf(boss.getNpcId()), futureSpawn);
    }
    else
    {
      boss.setRaidStatus(StatusEnum.ALIVE);

      info.set("currentHP", boss.getCurrentHp());
      info.set("currentMP", boss.getCurrentMp());
      info.set("respawnTime", 0L);
    }

    _storedInfo.remove(Integer.valueOf(boss.getNpcId()));
    _storedInfo.put(Integer.valueOf(boss.getNpcId()), info);
  }

  public void addNewSpawn(L2Spawn spawnDat, long respawnTime, double currentHP, double currentMP, boolean storeInDb)
  {
    if (spawnDat == null) return;
    if (_spawns.containsKey(Integer.valueOf(spawnDat.getNpcid()))) return;

    int bossId = spawnDat.getNpcid();
    long time = Calendar.getInstance().getTimeInMillis();

    SpawnTable.getInstance().addNewSpawn(spawnDat, false);

    if ((respawnTime == 0L) || (time > respawnTime))
    {
      L2RaidBossInstance raidboss = null;

      if (bossId == 25328)
        raidboss = DayNightSpawnManager.getInstance().handleBoss(spawnDat);
      else {
        raidboss = (L2RaidBossInstance)spawnDat.doSpawn();
      }
      if (raidboss != null)
      {
        raidboss.setCurrentHp(currentHP);
        raidboss.setCurrentMp(currentMP);
        raidboss.setRaidStatus(StatusEnum.ALIVE);

        _bosses.put(Integer.valueOf(bossId), raidboss);

        StatsSet info = new StatsSet();
        info.set("currentHP", currentHP);
        info.set("currentMP", currentMP);
        info.set("respawnTime", 0L);

        _storedInfo.put(Integer.valueOf(bossId), info);
      }

    }
    else
    {
      long spawnTime = respawnTime - Calendar.getInstance().getTimeInMillis();

      ScheduledFuture futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new spawnSchedule(bossId), spawnTime);

      _schedules.put(Integer.valueOf(bossId), futureSpawn);
    }

    _spawns.put(Integer.valueOf(bossId), spawnDat);

    if (storeInDb)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("INSERT INTO raidboss_spawnlist (boss_id,amount,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) values(?,?,?,?,?,?,?,?,?)");
        statement.setInt(1, spawnDat.getNpcid());
        statement.setInt(2, spawnDat.getAmount());
        statement.setInt(3, spawnDat.getLocx());
        statement.setInt(4, spawnDat.getLocy());
        statement.setInt(5, spawnDat.getLocz());
        statement.setInt(6, spawnDat.getHeading());
        statement.setLong(7, respawnTime);
        statement.setDouble(8, currentHP);
        statement.setDouble(9, currentMP);
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("RaidBossSpawnManager: Could not store raidboss #" + bossId + " in the DB:" + e);
      }
      finally {
        try {
          con.close(); } catch (Exception e) {
        }
      }
    }
  }

  public void deleteSpawn(L2Spawn spawnDat, boolean updateDb) {
    if (spawnDat == null) return;
    if (!_spawns.containsKey(Integer.valueOf(spawnDat.getNpcid()))) return;

    int bossId = spawnDat.getNpcid();

    SpawnTable.getInstance().deleteSpawn(spawnDat, false);
    _spawns.remove(Integer.valueOf(bossId));

    if (_bosses.containsKey(Integer.valueOf(bossId))) {
      _bosses.remove(Integer.valueOf(bossId));
    }
    if (_schedules.containsKey(Integer.valueOf(bossId)))
    {
      ScheduledFuture f = (ScheduledFuture)_schedules.get(Integer.valueOf(bossId));
      f.cancel(true);
      _schedules.remove(Integer.valueOf(bossId));
    }

    if (_storedInfo.containsKey(Integer.valueOf(bossId))) {
      _storedInfo.remove(Integer.valueOf(bossId));
    }
    if (updateDb)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?");
        statement.setInt(1, bossId);
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("RaidBossSpawnManager: Could not remove raidboss #" + bossId + " from DB: " + e);
      }
      finally {
        try {
          con.close(); } catch (Exception e) {
        }
      }
    }
  }

  private void updateDb() {
    for (Integer bossId : _storedInfo.keySet())
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();

        L2RaidBossInstance boss = (L2RaidBossInstance)_bosses.get(bossId);

        if (boss == null)
        {
          try
          {
            con.close(); } catch (Exception e) {  }
          e.printStackTrace(); continue;
        }
        if (boss.getRaidStatus().equals(StatusEnum.ALIVE)) {
          updateStatus(boss, false);
        }
        StatsSet info = (StatsSet)_storedInfo.get(bossId);

        if (info == null)
        {
          try
          {
            con.close(); } catch (Exception e) {  }
          e.printStackTrace(); continue;
        }
        PreparedStatement statement = con.prepareStatement("UPDATE raidboss_spawnlist set respawn_time = ?, currentHP = ?, currentMP = ? where boss_id = ?");
        statement.setLong(1, info.getLong("respawnTime"));
        statement.setDouble(2, info.getDouble("currentHP"));
        statement.setDouble(3, info.getDouble("currentMP"));
        statement.setInt(4, bossId.intValue());
        statement.execute();

        statement.close();
      } catch (SQLException e) {
        _log.warning("RaidBossSpawnManager: Couldnt update raidboss_spawnlist table");
      } finally {
        try {
          con.close(); } catch (Exception e) { e.printStackTrace(); }
      }
    }
  }

  public String[] getAllRaidBossStatus()
  {
    String[] msg = new String[_bosses == null ? 0 : _bosses.size()];

    if (_bosses == null)
    {
      msg[0] = "None";
      return msg;
    }

    int index = 0;

    for (Iterator i$ = _bosses.keySet().iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      L2RaidBossInstance boss = (L2RaidBossInstance)_bosses.get(Integer.valueOf(i));

      msg[index] = (boss.getName() + ": " + boss.getRaidStatus().name());
      index++;
    }

    return msg;
  }

  public String getRaidBossStatus(int bossId)
  {
    String msg = "RaidBoss Status....\n";

    if (_bosses == null)
    {
      msg = msg + "None";
      return msg;
    }

    if (_bosses.containsKey(Integer.valueOf(bossId)))
    {
      L2RaidBossInstance boss = (L2RaidBossInstance)_bosses.get(Integer.valueOf(bossId));

      msg = msg + boss.getName() + ": " + boss.getRaidStatus().name();
    }

    return msg;
  }

  public StatusEnum getRaidBossStatusId(int bossId)
  {
    if (_bosses.containsKey(Integer.valueOf(bossId))) {
      return ((L2RaidBossInstance)_bosses.get(Integer.valueOf(bossId))).getRaidStatus();
    }
    if (_schedules.containsKey(Integer.valueOf(bossId))) {
      return StatusEnum.DEAD;
    }
    return StatusEnum.UNDEFINED;
  }

  public L2NpcTemplate getValidTemplate(int bossId)
  {
    L2NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
    if (template == null) return null;
    if (!template.type.equalsIgnoreCase("L2RaidBoss")) return null;
    return template;
  }

  public void notifySpawnNightBoss(L2RaidBossInstance raidboss)
  {
    StatsSet info = new StatsSet();
    info.set("currentHP", raidboss.getCurrentHp());
    info.set("currentMP", raidboss.getCurrentMp());
    info.set("respawnTime", 0L);

    raidboss.setRaidStatus(StatusEnum.ALIVE);

    _storedInfo.put(Integer.valueOf(raidboss.getNpcId()), info);

    GmListTable.broadcastMessageToGMs("Spawning Raid Boss " + raidboss.getName());

    _bosses.put(Integer.valueOf(raidboss.getNpcId()), raidboss);
  }

  public boolean isDefined(int bossId)
  {
    return _spawns.containsKey(Integer.valueOf(bossId));
  }

  public Map<Integer, L2RaidBossInstance> getBosses()
  {
    return _bosses;
  }

  public Map<Integer, L2Spawn> getSpawns()
  {
    return _spawns;
  }

  public void reloadBosses()
  {
    init();
  }

  public void cleanUp()
  {
    updateDb();

    _bosses.clear();

    if (_schedules != null)
    {
      for (Integer bossId : _schedules.keySet())
      {
        ScheduledFuture f = (ScheduledFuture)_schedules.get(bossId);
        f.cancel(true);
      }
    }

    _schedules.clear();
    _storedInfo.clear();
    _spawns.clear();
  }

  private class spawnSchedule
    implements Runnable
  {
    private int bossId;

    public spawnSchedule(int npcId)
    {
      bossId = npcId;
    }

    public void run()
    {
      L2RaidBossInstance raidboss = null;

      if (bossId == 25328)
        raidboss = DayNightSpawnManager.getInstance().handleBoss((L2Spawn)RaidBossSpawnManager._spawns.get(Integer.valueOf(bossId)));
      else {
        raidboss = (L2RaidBossInstance)((L2Spawn)RaidBossSpawnManager._spawns.get(Integer.valueOf(bossId))).doSpawn();
      }
      if (raidboss != null)
      {
        raidboss.setRaidStatus(RaidBossSpawnManager.StatusEnum.ALIVE);

        StatsSet info = new StatsSet();
        info.set("currentHP", raidboss.getCurrentHp());
        info.set("currentMP", raidboss.getCurrentMp());
        info.set("respawnTime", 0L);

        RaidBossSpawnManager._storedInfo.put(Integer.valueOf(bossId), info);

        GmListTable.broadcastMessageToGMs("Spawning Raid Boss " + raidboss.getName());

        RaidBossSpawnManager._bosses.put(Integer.valueOf(bossId), raidboss);
      }

      RaidBossSpawnManager._schedules.remove(Integer.valueOf(bossId));
    }
  }

  public static enum StatusEnum
  {
    ALIVE, 
    DEAD, 
    UNDEFINED;
  }
}