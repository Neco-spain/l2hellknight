package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.SpawnTerritory;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SpawnTable
{
  private static Logger _log = AbstractLogger.getLogger(SpawnTable.class.getName());
  private static SpawnTable _instance = new SpawnTable();
  private static FastMap<Integer, L2Spawn> _spawntable;
  private int _npcSpawnCount;
  private int _highestId;
  private static int ST_DELAY = 0;
  private static FastMap<Integer, SpawnTerritory> _territories;

  public static SpawnTable getInstance()
  {
    return _instance;
  }

  private SpawnTable() {
    load(false);
  }

  public final void load(boolean reload)
  {
    if (reload) {
      _spawntable.clear();
      _territories.clear();
      DayNightSpawnManager.getInstance().load(true);
    }
    else {
      DayNightSpawnManager.init();
      _spawntable = new FastMap().shared("SpawnTable._spawntable");
      _territories = new FastMap().shared("SpawnTable._territories");
    }

    switch (Config.NPC_SPAWN_TYPE)
    {
    case 0:
      break;
    case 1:
      loadSpawnData(false);
      break;
    case 2:
      loadSpawnData(true);
      fillSpawnTable();
      break;
    case 3:
      fillSpawnTable();
      loadSpawnData(false);
    }
  }

  public FastMap<Integer, L2Spawn> getSpawnTable()
  {
    return _spawntable;
  }

  private void loadSpawnData(boolean events)
  {
    int locId = 0;
    long start = System.currentTimeMillis();
    try {
      File file = new File(Config.DATAPACK_ROOT, "data/spawnlist.xml");
      if (!file.exists()) {
        _log.config("SpawnTable [ERROR]: data/spawnlist.xml doesn't exist");
        return;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if ("territory".equalsIgnoreCase(d.getNodeName())) {
              NamedNodeMap attrs = d.getAttributes();
              locId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
              if ((events) && (!isEventTerritory(locId)))
              {
                continue;
              }

              SpawnTerritory spawnTerr = new SpawnTerritory(locId);
              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("area".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String srt = attrs.getNamedItem("points").getNodeValue();
                  String[] points = srt.split(";");
                  for (String xyzz : points) {
                    String[] xy = xyzz.split(",");
                    spawnTerr.addPoint(Integer.valueOf(xy[0]).intValue(), Integer.valueOf(xy[1]).intValue());
                    spawnTerr.setZ(Integer.valueOf(xy[2]).intValue(), Integer.valueOf(xy[3]).intValue());
                  }

                }

                if ("npc".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  int npcId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());

                  if (dontSpawn(npcId))
                  {
                    continue;
                  }
                  L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
                  if (npc != null) {
                    if (npc.type.equalsIgnoreCase("L2SiegeGuard"))
                      continue;
                    if (npc.type.equalsIgnoreCase("L2RaidBoss"))
                      continue;
                    if ((npc.type.equalsIgnoreCase("L2GrandBoss")) && (!npc.name.equalsIgnoreCase("Tyrannosaurus")))
                      continue;
                    if ((!Config.ALLOW_CLASS_MASTERS) && (npc.type.equals("L2ClassMaster"))) {
                      continue;
                    }
                    int x = 0;
                    int y = 0;
                    int z = 0;
                    int heading = 0;
                    String pos = attrs.getNamedItem("pos").getNodeValue();
                    int total = Integer.parseInt(attrs.getNamedItem("total").getNodeValue());
                    int respawn = Integer.parseInt(attrs.getNamedItem("respawn").getNodeValue());
                    total--;
                    respawn *= 1000;

                    if (pos.equalsIgnoreCase("anywhere"))
                    {
                      for (int i = 0; i <= total; i++) {
                        L2Spawn spawn = new L2Spawn(npc);
                        spawn.setId(npcId);
                        spawn.setAmount(1);
                        spawn.setLocx(0);
                        spawn.setLocy(0);
                        spawn.setLocz(0);
                        spawn.setHeading(0);
                        spawn.stopRespawn();
                        spawn.setLastKill(1L);
                        spawn.setTerritory(spawnTerr);
                        spawn.setFree();
                        spawnTerr.addSpawn(spawn, respawn);
                      }
                    }
                    else {
                      String[] loc = pos.split(";");
                      for (String xyz : loc) {
                        String[] xyt = xyz.split(",");
                        x = Integer.valueOf(xyt[0]).intValue();
                        y = Integer.valueOf(xyt[1]).intValue();
                        z = Integer.valueOf(xyt[2]).intValue();
                        heading = Integer.valueOf(xyt[3]).intValue();
                      }
                      L2Spawn spawn = new L2Spawn(npc);
                      spawn.setId(npcId);
                      spawn.setAmount(1);
                      spawn.setLocx(x);
                      spawn.setLocy(y);
                      spawn.setLocz(z);
                      spawn.setHeading(heading);
                      spawn.stopRespawn();
                      spawn.setLastKill(1L);
                      spawn.setTerritory(spawnTerr);
                      spawnTerr.addSpawn(spawn, respawn);
                    }
                  }
                  else {
                    _log.warning("SpawnTable [ERROR]: Data missing in NPC table for ID: " + npcId + ".");
                  }
                }
                if ("path".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String waypoints = attrs.getNamedItem("waypoints").getNodeValue();
                  int waypointsdelay = Integer.parseInt(attrs.getNamedItem("waypointsdelay").getNodeValue());

                  String[] waypointsXYZ = waypoints.split(";");
                  for (String wxyz : waypointsXYZ) {
                    String[] wxy = wxyz.split(",");
                    spawnTerr.addWayPoint(Integer.valueOf(wxy[0]).intValue(), Integer.valueOf(wxy[1]).intValue(), Integer.valueOf(wxy[2]).intValue());
                  }
                  spawnTerr.setWayPointDelay(waypointsdelay);
                }
                if ("spawn".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String spawnType = attrs.getNamedItem("type").getNodeValue();
                  if (spawnType.equalsIgnoreCase("manual"))
                    spawnTerr.setManualSpawn();
                  else if (spawnType.equalsIgnoreCase("boss")) {
                    spawnTerr.setBossSpawn();
                  }
                }
              }
              spawnTerr.close();
              _territories.put(Integer.valueOf(locId), spawnTerr);
            }
          }
        }
      }

    }
    catch (Exception e)
    {
      _log.warning("SpawnTable [ERROR]: Error reading '" + locId + "' territory, " + e.toString());
    }
    long time = (System.currentTimeMillis() - start) / 1000L;
    _log.config("Loading SpawnTable... total " + _territories.size() + " locations. Time: " + time + "s.");
    if (Config.NPC_SPAWN_DELAY > 0L) {
      ST_DELAY = 10;
      ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(), Config.NPC_SPAWN_DELAY);
    } else {
      startSpawn();
    }
  }

  private void startSpawn() {
    new Thread(new Runnable()
    {
      public void run() {
        int i = 0;

        FastMap.Entry e = SpawnTable._territories.head(); for (FastMap.Entry end = SpawnTable._territories.tail(); (e = e.getNext()) != end; ) {
          SpawnTerritory st = (SpawnTerritory)e.getValue();
          if (st == null)
          {
            continue;
          }
          if (st.isAutoSpawn()) {
            st.checkSpawns();
          }

          i++;
          if (SpawnTable.ST_DELAY > 0)
            try {
              Thread.sleep(SpawnTable.ST_DELAY);
            }
            catch (InterruptedException ex) {
            }
          if (i % 1000 == 0) {
            SpawnTable._log.config("SpawnTable [XML]: spawned " + i + "/" + SpawnTable._territories.size() + " territories.");
          }
        }
        SpawnTable._log.config("SpawnTable [XML]: spawned " + SpawnTable._territories.size() + " territories.");
      }
    }).start();
  }

  private static boolean dontSpawn(int id)
  {
    switch (id) {
    case 31225:
    case 31226:
      return false;
    case 30990:
    case 30991:
    case 30992:
    case 31093:
    case 31195:
    case 31255:
    case 31713:
    case 31721:
    case 31757:
    case 31760:
    case 31767:
    case 31768:
    case 31774:
    case 31854:
    case 31858:
    case 31860:
    case 32091:
    case 32131:
    case 32132:
      return true;
    case 13006:
    case 13007:
    case 31863:
    case 31864:
      return !Config.ALLOW_XM_SPAWN;
    case 31228:
    case 31229:
    case 31230:
      return !Config.ALLOW_MEDAL_EVENT;
    }

    if ((id >= 13031) && (id <= 13034)) {
      return true;
    }

    if ((id >= 31172) && (id <= 31201)) {
      return true;
    }

    if ((id >= 31212) && (id <= 31254)) {
      return true;
    }

    if ((id >= 31231) && (id <= 31224)) {
      return true;
    }

    if ((id >= 31713) && (id <= 31728)) {
      return true;
    }

    return (id >= 31999) && (id <= 32006);
  }

  private static boolean isEventTerritory(int locId)
  {
    switch (locId) {
    case 5024:
    case 5025:
    case 10612:
    case 10613:
      return true;
    }

    if ((Config.ALLOW_XM_SPAWN) && (locId >= 1856) && (locId <= 1898)) {
      return true;
    }

    return (Config.ALLOW_MEDAL_EVENT) && (locId >= 1753) && (locId <= 1792);
  }

  private void fillSpawnTable()
  {
    long start = System.currentTimeMillis();
    _log.config("SpawnTable: loading spawnlist from database...");

    int i = 0;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist ORDER BY id");
      rs = st.executeQuery();
      rs.setFetchSize(50);

      while (rs.next()) {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rs.getInt("npc_templateid"));
        if (template1 != null) {
          if ((template1.type.equalsIgnoreCase("L2SiegeGuard")) || 
            (template1.type.equalsIgnoreCase("L2RaidBoss")) || (
            (template1.type.equalsIgnoreCase("L2GrandBoss")) && (!template1.name.equalsIgnoreCase("Tyrannosaurus")))) {
            continue;
          }
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setId(rs.getInt("id"));
          spawnDat.setAmount(rs.getInt("count"));
          spawnDat.setLocx(rs.getInt("locx"));
          spawnDat.setLocy(rs.getInt("locy"));
          spawnDat.setLocz(rs.getInt("locz"));
          spawnDat.setHeading(rs.getInt("heading"));
          spawnDat.setRespawnDelay(rs.getInt("respawn_delay"));
          int loc_id = rs.getInt("loc_id");
          spawnDat.setLocation(loc_id);

          switch (rs.getInt("periodOfDay")) {
          case 0:
            _npcSpawnCount += spawnDat.init();
            break;
          case 1:
            _npcSpawnCount += spawnDat.init();
            DayNightSpawnManager.getInstance().addDayCreature(spawnDat);

            break;
          case 2:
            DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
            _npcSpawnCount += 1;
          }

          _spawntable.put(Integer.valueOf(spawnDat.getId()), spawnDat);
          if (spawnDat.getId() > _highestId) {
            _highestId = spawnDat.getId();
          }

          i++;
          if (i % 5000 == 0) {
            _log.config("SpawnTable: spawned " + i + " npc.");
          }
          continue;
        }
        _log.warning("SpawnTable [ERROR]: Data missing in NPC table for ID: " + rs.getInt("npc_templateid") + ".");
      }
    }
    catch (Exception e)
    {
      _log.warning("SpawnTable [ERROR]: Spawn could not be initialized: " + e);
    } finally {
      Close.CSR(con, st, rs);
    }
    long time = System.currentTimeMillis() - start;
    _log.config("SpawnTable: spawned " + i + " npc; done, time " + time + " ms;");
  }

  public L2Spawn getTemplate(int id)
  {
    return (L2Spawn)_spawntable.get(Integer.valueOf(id));
  }

  public void addNewSpawn(L2Spawn spawn, boolean storeInDb) {
    _highestId += 1;
    spawn.setId(_highestId);
    _spawntable.put(Integer.valueOf(_highestId), spawn);

    if (storeInDb) {
      Connect con = null;
      PreparedStatement st = null;
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("REPLACE INTO spawnlist (id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?,?)");
        st.setInt(1, spawn.getId());
        st.setInt(2, spawn.getAmount());
        st.setInt(3, spawn.getNpcid());
        st.setInt(4, spawn.getLocx());
        st.setInt(5, spawn.getLocy());
        st.setInt(6, spawn.getLocz());
        st.setInt(7, spawn.getHeading());
        st.setInt(8, spawn.getRespawnDelay() / 1000);
        st.setInt(9, spawn.getLocation());
        st.execute();
      }
      catch (Exception e) {
        _log.warning("SpawnTable [ERROR]: Could not store spawn in the DB:" + e);
      } finally {
        Close.CS(con, st);
      }
    }
  }

  public void deleteSpawn(L2Spawn spawn, boolean updateDb) {
    if (_spawntable.remove(Integer.valueOf(spawn.getId())) == null) {
      return;
    }

    if (updateDb) {
      Connect con = null;
      PreparedStatement st = null;
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("DELETE FROM spawnlist WHERE id=?");
        st.setInt(1, spawn.getId());
        st.execute();
      }
      catch (Exception e) {
        _log.warning("SpawnTable [ERROR]: Spawn " + spawn.getId() + " could not be removed from DB: " + e);
      } finally {
        Close.CS(con, st);
      }
    }
  }

  public void reloadAll()
  {
    load(true);
  }

  public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex)
  {
    int index = 0;
    for (L2Spawn spawn : _spawntable.values()) {
      if (npcId == spawn.getNpcid()) {
        index++;

        if (teleportIndex > -1) {
          if (teleportIndex == index)
            activeChar.teleToLocation(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), true);
        }
        else {
          activeChar.sendMessage(index + " - " + spawn.getTemplate().name + " (" + spawn.getId() + "): " + spawn.getLocx() + " " + spawn.getLocy() + " " + spawn.getLocz());
        }
      }
    }

    if (index == 0)
      activeChar.sendMessage("No current spawns found.");
  }

  public SpawnTerritory getTerritory(int id)
  {
    return (SpawnTerritory)_territories.get(Integer.valueOf(id));
  }

  class Spawn
    implements Runnable
  {
    Spawn()
    {
    }

    public void run()
    {
      SpawnTable.this.startSpawn();
    }
  }
}