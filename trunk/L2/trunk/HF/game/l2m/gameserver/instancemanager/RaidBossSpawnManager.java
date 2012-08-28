package l2m.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.database.mysql;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Spawner;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.instances.RaidBossInstance;
import l2m.gameserver.model.instances.ReflectionBossInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.data.tables.ClanTable;
import l2m.gameserver.data.tables.GmListTable;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaidBossSpawnManager
{
  private static final Logger _log;
  private static RaidBossSpawnManager _instance;
  protected static Map<Integer, Spawner> _spawntable;
  protected static Map<Integer, StatsSet> _storedInfo;
  protected static Map<Integer, Map<Integer, Integer>> _points;
  public static final Integer KEY_RANK;
  public static final Integer KEY_TOTAL_POINTS;
  private Lock pointsLock = new ReentrantLock();

  private RaidBossSpawnManager()
  {
    _instance = this;
    if (!Config.DONTLOADSPAWN)
      reloadBosses();
  }

  public void reloadBosses()
  {
    loadStatus();
    restorePointsTable();
    calculateRanking();
  }

  public void cleanUp()
  {
    updateAllStatusDb();
    updatePointsDb();

    _storedInfo.clear();
    _spawntable.clear();
    _points.clear();
  }

  public static RaidBossSpawnManager getInstance()
  {
    if (_instance == null)
      new RaidBossSpawnManager();
    return _instance;
  }

  private void loadStatus()
  {
    _storedInfo = new ConcurrentHashMap();

    Connection con = null;
    Statement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      rset = con.createStatement().executeQuery("SELECT * FROM `raidboss_status`");
      while (rset.next())
      {
        int id = rset.getInt("id");
        StatsSet info = new StatsSet();
        info.set("current_hp", rset.getDouble("current_hp"));
        info.set("current_mp", rset.getDouble("current_mp"));
        info.set("respawn_delay", rset.getInt("respawn_delay"));
        _storedInfo.put(Integer.valueOf(id), info);
      }
    }
    catch (Exception e)
    {
      _log.warn("RaidBossSpawnManager: Couldnt load raidboss statuses");
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    _log.info("RaidBossSpawnManager: Loaded " + _storedInfo.size() + " Statuses");
  }

  private void updateAllStatusDb()
  {
    for (Iterator i$ = _storedInfo.keySet().iterator(); i$.hasNext(); ) { int id = ((Integer)i$.next()).intValue();
      updateStatusDb(id); }
  }

  private void updateStatusDb(int id)
  {
    Spawner spawner = (Spawner)_spawntable.get(Integer.valueOf(id));
    if (spawner == null) {
      return;
    }
    StatsSet info = (StatsSet)_storedInfo.get(Integer.valueOf(id));
    if (info == null) {
      _storedInfo.put(Integer.valueOf(id), info = new StatsSet());
    }
    NpcInstance raidboss = spawner.getFirstSpawned();
    if ((raidboss instanceof ReflectionBossInstance)) {
      return;
    }
    if (raidboss != null)
    {
      info.set("current_hp", raidboss.getCurrentHp());
      info.set("current_mp", raidboss.getCurrentMp());
      info.set("respawn_delay", 0);
    }
    else
    {
      info.set("current_hp", 0);
      info.set("current_mp", 0);
      info.set("respawn_delay", spawner.getRespawnTime());
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, respawn_delay) VALUES (?,?,?,?)");
      statement.setInt(1, id);
      statement.setDouble(2, info.getDouble("current_hp"));
      statement.setDouble(3, info.getDouble("current_mp"));
      statement.setInt(4, info.getInteger("respawn_delay", 0));
      statement.execute();
    }
    catch (SQLException e)
    {
      _log.warn("RaidBossSpawnManager: Couldnt update raidboss_status table");
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  private void updateStatusRespawnDB(RaidBossInstance npc)
  {
    if ((npc instanceof ReflectionBossInstance))
    {
      return;
    }
    Connection con = null;
    PreparedStatement statement = null;

    if ((npc.getSpawn() == null) || (npc.getSpawn().getRespawnDelayWithRnd() < 1))
    {
      return;
    }

    try
    {
      long delay = ()(Config.ALT_RAID_RESPAWN_MULTIPLIER * npc.getSpawn().getRespawnDelayWithRnd()) * 1000L;
      delay = Math.max(1000L, delay - npc.getDeadTime());

      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, respawn_delay) VALUES (?,?,?,?)");
      statement.setInt(1, npc.getNpcId());
      statement.setDouble(2, 0.0D);
      statement.setDouble(3, 0.0D);
      statement.setInt(4, (int)((System.currentTimeMillis() + delay) / 1000L));
      statement.execute();
    }
    catch (SQLException e)
    {
      _log.warn("RaidBossSpawnManager: Couldnt update raidboss_status table");
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void addNewSpawn(int npcId, Spawner spawnDat)
  {
    if (_spawntable.containsKey(Integer.valueOf(npcId))) {
      return;
    }
    _spawntable.put(Integer.valueOf(npcId), spawnDat);

    StatsSet info = (StatsSet)_storedInfo.get(Integer.valueOf(npcId));
    if (info != null)
      spawnDat.setRespawnTime(info.getInteger("respawn_delay", 0));
  }

  public void onBossSpawned(RaidBossInstance raidboss)
  {
    int bossId = raidboss.getNpcId();
    if (!_spawntable.containsKey(Integer.valueOf(bossId))) {
      return;
    }
    StatsSet info = (StatsSet)_storedInfo.get(Integer.valueOf(bossId));
    if ((info != null) && (info.getDouble("current_hp") > 1.0D))
    {
      raidboss.setCurrentHp(info.getDouble("current_hp"), false);
      raidboss.setCurrentMp(info.getDouble("current_mp"));
    }

    GmListTable.broadcastMessageToGMs("Spawning RaidBoss " + raidboss.getName());
  }

  public void onBossDespawned(RaidBossInstance raidboss)
  {
    updateStatusDb(raidboss.getNpcId());
  }

  public void onBossSaveDespawned(RaidBossInstance raidboss)
  {
    updateStatusRespawnDB(raidboss);
  }

  public Status getRaidBossStatusId(int bossId)
  {
    Spawner spawner = (Spawner)_spawntable.get(Integer.valueOf(bossId));
    if (spawner == null) {
      return Status.UNDEFINED;
    }
    NpcInstance npc = spawner.getFirstSpawned();
    return npc == null ? Status.DEAD : Status.ALIVE;
  }

  public boolean isDefined(int bossId)
  {
    return _spawntable.containsKey(Integer.valueOf(bossId));
  }

  public Map<Integer, Spawner> getSpawnTable()
  {
    return _spawntable;
  }

  private void restorePointsTable()
  {
    pointsLock.lock();
    _points = new ConcurrentHashMap();

    Connection con = null;
    Statement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.createStatement();
      rset = statement.executeQuery("SELECT owner_id, boss_id, points FROM `raidboss_points` ORDER BY owner_id ASC");
      int currentOwner = 0;
      Map score = null;
      while (rset.next())
      {
        if (currentOwner != rset.getInt("owner_id"))
        {
          currentOwner = rset.getInt("owner_id");
          score = new HashMap();
          _points.put(Integer.valueOf(currentOwner), score);
        }

        assert (score != null);
        int bossId = rset.getInt("boss_id");
        NpcTemplate template = NpcHolder.getInstance().getTemplate(bossId);
        if ((bossId != KEY_RANK.intValue()) && (bossId != KEY_TOTAL_POINTS.intValue()) && (template != null) && (template.rewardRp > 0))
          score.put(Integer.valueOf(bossId), Integer.valueOf(rset.getInt("points")));
      }
    }
    catch (Exception e)
    {
      _log.warn("RaidBossSpawnManager: Couldnt load raidboss points");
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
    pointsLock.unlock();
  }

  public void updatePointsDb()
  {
    pointsLock.lock();
    if (!mysql.set("TRUNCATE `raidboss_points`")) {
      _log.warn("RaidBossSpawnManager: Couldnt empty raidboss_points table");
    }
    if (_points.isEmpty())
    {
      pointsLock.unlock();
      return;
    }

    Connection con = null;
    Statement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.createStatement();
      SqlBatch b = new SqlBatch("INSERT INTO `raidboss_points` (owner_id, boss_id, points) VALUES");

      for (Iterator i$ = _points.entrySet().iterator(); i$.hasNext(); ) { pointEntry = (Map.Entry)i$.next();

        Map tmpPoint = (Map)pointEntry.getValue();
        if ((tmpPoint == null) || (tmpPoint.isEmpty())) {
          continue;
        }
        for (Map.Entry pointListEntry : tmpPoint.entrySet())
        {
          if ((KEY_RANK.equals(pointListEntry.getKey())) || (KEY_TOTAL_POINTS.equals(pointListEntry.getKey())) || (pointListEntry.getValue() == null) || (((Integer)pointListEntry.getValue()).intValue() == 0)) {
            continue;
          }
          StringBuilder sb = new StringBuilder("(");
          sb.append(pointEntry.getKey()).append(",");
          sb.append(pointListEntry.getKey()).append(",");
          sb.append(pointListEntry.getValue()).append(")");
          b.write(sb.toString());
        }
      }
      Map.Entry pointEntry;
      if (!b.isEmpty())
        statement.executeUpdate(b.close());
    }
    catch (SQLException e)
    {
      _log.warn("RaidBossSpawnManager: Couldnt update raidboss_points table");
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
    pointsLock.unlock();
  }

  public void addPoints(int ownerId, int bossId, int points)
  {
    if ((points <= 0) || (ownerId <= 0) || (bossId <= 0)) {
      return;
    }
    pointsLock.lock();

    Map pointsTable = (Map)_points.get(Integer.valueOf(ownerId));

    if (pointsTable == null)
    {
      pointsTable = new HashMap();
      _points.put(Integer.valueOf(ownerId), pointsTable);
    }

    if (pointsTable.isEmpty()) {
      pointsTable.put(Integer.valueOf(bossId), Integer.valueOf(points));
    }
    else
    {
      Integer currentPoins = (Integer)pointsTable.get(Integer.valueOf(bossId));
      pointsTable.put(Integer.valueOf(bossId), Integer.valueOf(currentPoins == null ? points : currentPoins.intValue() + points));
    }
    pointsLock.unlock();
  }

  public TreeMap<Integer, Integer> calculateRanking()
  {
    TreeMap tmpRanking = new TreeMap();

    pointsLock.lock();

    for (Map.Entry point : _points.entrySet())
    {
      Map tmpPoint = (Map)point.getValue();

      tmpPoint.remove(KEY_RANK);
      tmpPoint.remove(KEY_TOTAL_POINTS);
      int totalPoints = 0;

      for (Map.Entry e : tmpPoint.entrySet()) {
        totalPoints += ((Integer)e.getValue()).intValue();
      }

      if (totalPoints != 0)
      {
        tmpPoint.put(KEY_TOTAL_POINTS, Integer.valueOf(totalPoints));

        tmpRanking.put(Integer.valueOf(totalPoints), point.getKey());
      }

    }

    int ranking = 1;
    for (Map.Entry entry : tmpRanking.descendingMap().entrySet())
    {
      Map tmpPoint = (Map)_points.get(entry.getValue());

      tmpPoint.put(KEY_RANK, Integer.valueOf(ranking));
      ranking++;
    }

    pointsLock.unlock();

    return tmpRanking;
  }

  public void distributeRewards()
  {
    pointsLock.lock();
    TreeMap ranking = calculateRanking();
    Iterator e = ranking.descendingMap().values().iterator();
    int counter = 1;
    while ((e.hasNext()) && (counter <= 100))
    {
      int reward = 0;
      int playerId = ((Integer)e.next()).intValue();
      if (counter == 1)
        reward = 2500;
      else if (counter == 2)
        reward = 1800;
      else if (counter == 3)
        reward = 1400;
      else if (counter == 4)
        reward = 1200;
      else if (counter == 5)
        reward = 900;
      else if (counter == 6)
        reward = 700;
      else if (counter == 7)
        reward = 600;
      else if (counter == 8)
        reward = 400;
      else if (counter == 9)
        reward = 300;
      else if (counter == 10)
        reward = 200;
      else if (counter <= 50)
        reward = 50;
      else if (counter <= 100)
        reward = 25;
      Player player = GameObjectsStorage.getPlayer(playerId);
      Clan clan = null;
      if (player != null)
        clan = player.getClan();
      else
        clan = ClanTable.getInstance().getClan(mysql.simple_get_int("clanid", "characters", "obj_Id=" + playerId));
      if (clan != null)
        clan.incReputation(reward, true, "RaidPoints");
      counter++;
    }
    _points.clear();
    updatePointsDb();
    pointsLock.unlock();
  }

  public Map<Integer, Map<Integer, Integer>> getPoints()
  {
    return _points;
  }

  public Map<Integer, Integer> getPointsForOwnerId(int ownerId)
  {
    return (Map)_points.get(Integer.valueOf(ownerId));
  }

  static
  {
    _log = LoggerFactory.getLogger(RaidBossSpawnManager.class);

    _spawntable = new ConcurrentHashMap();

    KEY_RANK = new Integer(-1);
    KEY_TOTAL_POINTS = new Integer(0);
  }

  public static enum Status
  {
    ALIVE, 
    DEAD, 
    UNDEFINED;
  }
}