package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.bosses.AntharasManager;
import net.sf.l2j.gameserver.instancemanager.bosses.BaiumManager;
import net.sf.l2j.gameserver.instancemanager.bosses.QueenAntManager;
import net.sf.l2j.gameserver.instancemanager.bosses.ValakasManager;
import net.sf.l2j.gameserver.instancemanager.bosses.ZakenManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.ai.QueenAnt;
import scripts.zone.type.L2BossZone;

public class GrandBossManager
{
  protected static final Logger _log = AbstractLogger.getLogger(GrandBossManager.class.getName());
  private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
  private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
  private static GrandBossManager _instance;
  protected static FastMap<Integer, BossInfo> _bosses = new FastMap();
  private FastList<L2BossZone> _zones = new FastList();

  L2GrandBossInstance _frinta = null;
  L2GrandBossInstance _halisha = null;

  QueenAnt _aq = null;

  L2MonsterInstance _larva = null;

  public static GrandBossManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new GrandBossManager();
    _instance.load();
  }

  private void load() {
    _zones = new FastList();
    loadBosses();
  }

  public void loadBosses() {
    _bosses.clear();
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT boss_id, spawn_date, status FROM grandboss_data ORDER BY boss_id");
      rs = st.executeQuery();
      rs.setFetchSize(5);
      while (rs.next()) {
        if (rs.getInt("boss_id") != 29045)
        {
          continue;
        }
        _bosses.put(Integer.valueOf(rs.getInt("boss_id")), new BossInfo(0, 0L));
        manageBoss(rs.getInt("boss_id"), rs.getInt("status"), rs.getLong("spawn_date"));
      }
    } catch (SQLException e) {
      _log.warning("GrandBossManager: Could not load grandboss_data table");
      e.getMessage();
    } finally {
      Close.CSR(con, st, rs);
    }
    _log.info("GrandBossManager: loaded " + _bosses.size() + " Grand Bosses");
  }

  private void manageBoss(int boss, int status, long respawn)
  {
    long new_respawn = 0L;
    boolean spawn = false;

    long temp = respawn - System.currentTimeMillis();
    if (temp > 0L) {
      new_respawn = respawn;
    } else {
      spawn = true;
      new_respawn = 0L;
    }

    if (spawn) {
      switch (boss) {
      case 29045:
        temp = 90000L;
      }
    }

    setStatus(boss, 0);
    ((BossInfo)_bosses.get(Integer.valueOf(boss))).respawn = new_respawn;
    ThreadPoolManager.getInstance().scheduleGeneral(new RespawnBoss(boss), temp);
  }

  public void initZones()
  {
    FastMap zones = new FastMap();
    if (_zones == null) {
      _log.warning("GrandBossManager: Could not read Grand Boss zone data");
      return;
    }

    for (L2BossZone zone : _zones) {
      if (zone == null) {
        continue;
      }
      zones.put(Integer.valueOf(zone.getId()), new FastList());
    }

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT * from grandboss_list ORDER BY player_id");
      rset = statement.executeQuery();
      rset.setFetchSize(50);
      while (rset.next()) {
        int id = rset.getInt("player_id");
        int zone_id = rset.getInt("zone");
        ((FastList)zones.get(Integer.valueOf(zone_id))).add(Integer.valueOf(id));
      }
    } catch (SQLException e) {
      _log.warning("GrandBossManager: Could not load grandboss_list table");
      e.getMessage();
    } finally {
      Close.CSR(con, statement, rset);
    }
    _log.info("GrandBossManager: Loaded " + _zones.size() + " Grand Boss Zones");

    zones.clear();
  }

  public void addZone(L2BossZone zone) {
    if (_zones != null)
      _zones.add(zone);
  }

  public final L2BossZone getZone(L2Character character)
  {
    if (_zones != null) {
      for (L2BossZone temp : _zones) {
        if (temp.isCharacterInZone(character)) {
          return temp;
        }
      }
    }
    return null;
  }

  public final L2BossZone getZone(int x, int y, int z) {
    if (_zones != null) {
      for (L2BossZone temp : _zones) {
        if (temp.isInsideZone(x, y, z)) {
          return temp;
        }
      }
    }
    return null;
  }

  public boolean checkIfInZone(String zoneType, L2Object obj) {
    L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
    if (temp == null) {
      return false;
    }
    return temp.getZoneName().equalsIgnoreCase(zoneType);
  }

  public boolean checkIfInZone(L2PcInstance player) {
    if (player == null) {
      return false;
    }
    L2BossZone temp = getZone(player.getX(), player.getY(), player.getZ());

    return temp != null;
  }

  public BossInfo get(int bossId)
  {
    return (BossInfo)_bosses.get(Integer.valueOf(bossId));
  }

  public void setStatus(int bossId, int status) {
    ((BossInfo)_bosses.get(Integer.valueOf(bossId))).status = status;

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `grandboss_data` SET `status`=? WHERE `boss_id`=?");
      statement.setInt(1, status);
      statement.setInt(2, bossId);
      statement.executeUpdate();
    } catch (Exception e) {
      _log.warning("L2GrandBossInstance: could not set " + bossId + " status" + e);
    } finally {
      Close.CS(con, statement);
    }
  }

  public void setRespawn(int bossId, long nextTime) {
    ((BossInfo)_bosses.get(Integer.valueOf(bossId))).respawn = nextTime;

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `grandboss_data` SET `spawn_date`=? WHERE `boss_id`=?");
      statement.setLong(1, nextTime);
      statement.setInt(2, bossId);
      statement.executeUpdate();
    } catch (Exception e) {
      _log.warning("L2GrandBossInstance: could not set " + bossId + " status" + e);
    } finally {
      Close.CS(con, statement);
    }
    if (nextTime > 0L) {
      setStatus(bossId, 0);
      manageBoss(bossId, 0, nextTime);

      if ((Config.ANNOUNCE_EPIC_STATES) && (GameServer.gameServer.getSelectorThread() != null))
        switch (bossId) {
        case 29045:
          EventManager.getInstance().announce(Static.FRINTEZZA_DIED);
        }
    }
  }

  private void storeToDb()
  {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM grandboss_list");
      statement.executeUpdate();
      Close.S(statement);

      con.setAutoCommit(false);

      statement = con.prepareStatement("UPDATE `grandboss_data` SET `spawn_date`=?,`status`=? WHERE `boss_id`=?");
      FastMap.Entry e = _bosses.head(); for (FastMap.Entry end = _bosses.tail(); (e = e.getNext()) != end; ) {
        int boss = ((Integer)e.getKey()).intValue();
        BossInfo bi = (BossInfo)e.getValue();
        statement.setLong(1, bi.respawn);
        if (bi.respawn > 0L)
          statement.setInt(2, 0);
        else {
          statement.setInt(2, 1);
        }
        statement.setInt(3, boss);

        statement.addBatch();
      }

      statement.executeBatch();
      con.commit();
    } catch (SQLException e) {
      _log.warning("GrandBossManager: Couldn't store grandbosses to database:" + e);
    } finally {
      Close.CS(con, statement);
    }
  }

  public void cleanUp()
  {
    storeToDb();

    _bosses.clear();
    _zones.clear();
  }

  public FastList<L2BossZone> getZones() {
    return _zones;
  }

  public int getDBValue(String name, String var)
  {
    int result = 0;
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
      statement.setString(1, name);
      statement.setString(2, var);
      rset = statement.executeQuery();
      if (rset.first())
        result = rset.getInt(1);
    }
    catch (Exception e) {
      _log.warning("L2GrandBossInstance: could not load " + name + "; info" + e);
    } finally {
      Close.CSR(con, statement, rset);
    }
    return result;
  }

  public L2NpcInstance createOnePrivateEx(int npcId, int x, int y, int z, int heading) {
    return createOnePrivateEx(npcId, x, y, z, heading, false);
  }

  public L2NpcInstance createOnePrivateEx(int npcId, int x, int y, int z, int heading, boolean respawn) {
    L2NpcInstance result = null;
    try {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
      L2Spawn spawn = new L2Spawn(template);
      spawn.setHeading(heading);
      spawn.setLocx(x);
      spawn.setLocy(y);
      spawn.setLocz(z + 20);
      if (respawn)
        spawn.startRespawn();
      else {
        spawn.stopRespawn();
      }
      result = spawn.spawnOne();
      return result;
    } catch (Exception e1) {
      _log.warning("L2GrandBossInstance: Could not spawn Npc " + npcId);
    }

    return null;
  }

  public L2Spawn createOneSpawnEx(int npcId, int x, int y, int z, int heading, boolean respawn) {
    try {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
      L2Spawn spawn = new L2Spawn(template);
      spawn.setHeading(heading);
      spawn.setLocx(x);
      spawn.setLocy(y);
      spawn.setLocz(z + 20);
      if (respawn)
        spawn.startRespawn();
      else {
        spawn.stopRespawn();
      }
      return spawn;
    } catch (Exception e1) {
      _log.warning("L2GrandBossInstance: Could not spawn Npc " + npcId);
    }
    return null;
  }

  public L2NpcInstance createOnePrivateEx(int npcId, int x, int y, int z, int heading, int respawnTime) {
    L2NpcInstance result = null;
    try {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
      L2Spawn spawn = new L2Spawn(template);
      spawn.setLocx(x);
      spawn.setLocy(y);
      spawn.setLocz(z + 20);
      spawn.setRespawnDelay(respawnTime);
      spawn.setHeading(heading);
      result = spawn.spawnOne();
      return result;
    } catch (Exception e1) {
      _log.warning("L2GrandBossInstance: Could not spawn Npc " + npcId);
    }

    return null;
  }

  public void setFrintezza(L2GrandBossInstance boss)
  {
    if (boss != null)
      _frinta = boss;
  }

  public L2GrandBossInstance getFrintezza()
  {
    return _frinta;
  }

  public void setHalisha(L2GrandBossInstance boss) {
    if (boss != null)
      _halisha = boss;
  }

  public L2GrandBossInstance getHalisha()
  {
    return _halisha;
  }

  public void setAQ(QueenAnt boss)
  {
    if (boss != null)
      _aq = boss;
  }

  public QueenAnt getAQ()
  {
    return _aq;
  }

  public void setAQLarva(L2MonsterInstance larva)
  {
    if (larva != null)
      _larva = larva;
  }

  public L2MonsterInstance getAQLarva()
  {
    return _larva;
  }

  public boolean getItem(L2PcInstance player, int itemId)
  {
    if (!Config.NOEPIC_QUESTS) {
      return true;
    }

    L2ItemInstance coin = player.getInventory().getItemByItemId(itemId);
    if ((coin == null) || (coin.getCount() < 1)) {
      return false;
    }

    return player.destroyItemByItemId("RaidBossTele", itemId, 1, player, true);
  }

  public void loadManagers()
  {
    AntharasManager.init();
    BaiumManager.init();
    ValakasManager.init();
    QueenAntManager.init();
    ZakenManager.init();
  }

  class RespawnBoss
    implements Runnable
  {
    public int boss;

    RespawnBoss(int boss)
    {
      this.boss = boss;
    }

    public void run() {
      long temp = ((GrandBossManager.BossInfo)GrandBossManager._bosses.get(Integer.valueOf(boss))).respawn - System.currentTimeMillis();
      if (temp > 0L) {
        GrandBossManager.this.manageBoss(boss, 0, ((GrandBossManager.BossInfo)GrandBossManager._bosses.get(Integer.valueOf(boss))).respawn);
        return;
      }
      setStatus(boss, 1);
      setRespawn(boss, 0L);
      switch (boss) {
      case 29045:
        GrandBossManager._log.info(TimeLogger.getTime() + "GrandBossManager: Frintezza, ready for farm.");
        if ((!Config.ANNOUNCE_EPIC_STATES) || (GameServer.gameServer.getSelectorThread() == null)) break;
        EventManager.getInstance().announce(Static.FRINTEZZA_SPAWNED);
      }
    }
  }

  public static class BossInfo
  {
    public int status;
    public long respawn;

    public BossInfo(int status, long respawn)
    {
      this.status = status;
      this.respawn = respawn;
    }
  }
}