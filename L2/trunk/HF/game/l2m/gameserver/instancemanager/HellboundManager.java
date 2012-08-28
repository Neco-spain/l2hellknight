package l2m.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2p.commons.geometry.Polygon;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.listener.actor.OnDeathListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.SimpleSpawner;
import l2m.gameserver.model.Territory;
import l2m.gameserver.model.instances.DoorInstance;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;
import l2m.gameserver.utils.ReflectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HellboundManager
{
  private static final Logger _log = LoggerFactory.getLogger(HellboundManager.class);
  private static ArrayList<HellboundSpawn> _list;
  private static List<SimpleSpawner> _spawnList;
  private static HellboundManager _instance;
  private static int _initialStage;
  private static final long _taskDelay = 120000L;
  DeathListener _deathListener = new DeathListener(null);

  public static HellboundManager getInstance()
  {
    if (_instance == null)
      _instance = new HellboundManager();
    return _instance;
  }

  public HellboundManager()
  {
    getHellboundSpawn();
    spawnHellbound();
    doorHandler();
    _initialStage = getHellboundLevel();
    ThreadPoolManager.getInstance().scheduleAtFixedRate(new StageCheckTask(null), 120000L, 120000L);
    _log.info("Hellbound Manager: Loaded");
  }

  public static long getConfidence()
  {
    return ServerVariables.getLong("HellboundConfidence", 0L);
  }

  public static void addConfidence(long value)
  {
    ServerVariables.set("HellboundConfidence", Math.round(getConfidence() + value * Config.RATE_HELLBOUND_CONFIDENCE));
  }

  public static void reduceConfidence(long value)
  {
    long i = getConfidence() - value;
    if (i < 1L)
      i = 1L;
    ServerVariables.set("HellboundConfidence", i);
  }

  public static void setConfidence(long value)
  {
    ServerVariables.set("HellboundConfidence", value);
  }

  public static int getHellboundLevel()
  {
    if (Config.HELLBOUND_LEVEL <= getRHellboundLevel())
    {
      return getRHellboundLevel();
    }
    return Config.HELLBOUND_LEVEL;
  }

  public static int getRHellboundLevel()
  {
    long confidence = ServerVariables.getLong("HellboundConfidence", 0L);
    boolean judesBoxes = ServerVariables.getBool("HB_judesBoxes", false);
    boolean bernardBoxes = ServerVariables.getBool("HB_bernardBoxes", false);
    boolean derekKilled = ServerVariables.getBool("HB_derekKilled", false);
    boolean captainKilled = ServerVariables.getBool("HB_captainKilled", false);

    if (confidence < 1L)
      return 0;
    if ((confidence >= 1L) && (confidence < 300000L))
      return 1;
    if ((confidence >= 300000L) && (confidence < 600000L))
      return 2;
    if ((confidence >= 600000L) && (confidence < 1000000L))
      return 3;
    if ((confidence >= 1000000L) && (confidence < 1200000L))
    {
      if ((derekKilled) && (judesBoxes) && (bernardBoxes))
        return 5;
      if ((!derekKilled) && (judesBoxes) && (bernardBoxes))
        return 4;
      if ((!derekKilled) && ((!judesBoxes) || (!bernardBoxes)))
        return 3;
    } else {
      if ((confidence >= 1200000L) && (confidence < 1500000L))
        return 6;
      if ((confidence >= 1500000L) && (confidence < 1800000L))
        return 7;
      if ((confidence >= 1800000L) && (confidence < 2100000L))
      {
        if (captainKilled) {
          return 9;
        }
        return 8;
      }
      if ((confidence >= 2100000L) && (confidence < 2200000L))
        return 10;
      if (confidence >= 2200000L)
        return 11;
    }
    return 0;
  }

  private void spawnHellbound()
  {
    for (HellboundSpawn hbsi : _list)
      if (ArrayUtils.contains(hbsi.getStages(), getHellboundLevel()))
        try
        {
          NpcTemplate template = NpcHolder.getInstance().getTemplate(hbsi.getNpcId());
          for (int i = 0; i < hbsi.getAmount(); i++)
          {
            SimpleSpawner spawnDat = new SimpleSpawner(template);
            spawnDat.setAmount(1);
            if (hbsi.getLoc() != null)
              spawnDat.setLoc(hbsi.getLoc());
            if (hbsi.getSpawnTerritory() != null)
              spawnDat.setTerritory(hbsi.getSpawnTerritory());
            spawnDat.setReflection(ReflectionManager.DEFAULT);
            spawnDat.setRespawnDelay(hbsi.getRespawn(), hbsi.getRespawnRnd());
            spawnDat.setRespawnTime(0);
            spawnDat.doSpawn(true);
            spawnDat.getLastSpawn().addListener(_deathListener);
            spawnDat.startRespawn();
            _spawnList.add(spawnDat);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
    _log.info("HellboundManager: Spawned " + _spawnList.size() + " mobs and NPCs according to the current Hellbound stage");
  }

  private void getHellboundSpawn()
  {
    _list = new ArrayList();
    _spawnList = new ArrayList();
    try
    {
      File file = new File(Config.DATAPACK_ROOT + "/data/hellbound_spawnlist.xml");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc1 = factory.newDocumentBuilder().parse(file);

      int counter = 0;
      for (Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
        if ("list".equalsIgnoreCase(n1.getNodeName()))
          for (Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
            if (!"data".equalsIgnoreCase(d1.getNodeName()))
              continue;
            counter++;
            int npcId = Integer.parseInt(d1.getAttributes().getNamedItem("npc_id").getNodeValue());
            Location spawnLoc = null;
            if (d1.getAttributes().getNamedItem("loc") != null)
              spawnLoc = Location.parseLoc(d1.getAttributes().getNamedItem("loc").getNodeValue());
            int count = 1;
            if (d1.getAttributes().getNamedItem("count") != null)
              count = Integer.parseInt(d1.getAttributes().getNamedItem("count").getNodeValue());
            int respawn = 60;
            if (d1.getAttributes().getNamedItem("respawn") != null)
              respawn = Integer.parseInt(d1.getAttributes().getNamedItem("respawn").getNodeValue());
            int respawnRnd = 0;
            if (d1.getAttributes().getNamedItem("respawn_rnd") != null) {
              respawnRnd = Integer.parseInt(d1.getAttributes().getNamedItem("respawn_rnd").getNodeValue());
            }
            Node att = d1.getAttributes().getNamedItem("stage");
            StringTokenizer st = new StringTokenizer(att.getNodeValue(), ";");
            int tokenCount = st.countTokens();
            int[] stages = new int[tokenCount];
            for (int i = 0; i < tokenCount; i++)
            {
              Integer value = Integer.decode(st.nextToken().trim());
              stages[i] = value.intValue();
            }

            Territory territory = null;
            for (Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
              if (!"territory".equalsIgnoreCase(s1.getNodeName())) {
                continue;
              }
              Polygon poly = new Polygon();
              for (Node s2 = s1.getFirstChild(); s2 != null; s2 = s2.getNextSibling()) {
                if (!"add".equalsIgnoreCase(s2.getNodeName()))
                  continue;
                int x = Integer.parseInt(s2.getAttributes().getNamedItem("x").getNodeValue());
                int y = Integer.parseInt(s2.getAttributes().getNamedItem("y").getNodeValue());
                int minZ = Integer.parseInt(s2.getAttributes().getNamedItem("zmin").getNodeValue());
                int maxZ = Integer.parseInt(s2.getAttributes().getNamedItem("zmax").getNodeValue());
                poly.add(x, y).setZmin(minZ).setZmax(maxZ);
              }

              territory = new Territory().add(poly);

              if (poly.validate())
                continue;
              _log.error("HellboundManager: Invalid spawn territory : " + poly + "!");
            }

            if ((spawnLoc == null) && (territory == null))
            {
              _log.error("HellboundManager: no spawn data for npc id : " + npcId + "!");
            }
            else
            {
              HellboundSpawn hbs = new HellboundSpawn(npcId, spawnLoc, count, territory, respawn, respawnRnd, stages);
              _list.add(hbs);
            }
          }
      _log.info("HellboundManager: Loaded " + counter + " spawn entries.");
    }
    catch (Exception e)
    {
      _log.warn("HellboundManager: Spawn table could not be initialized.");
      e.printStackTrace();
    }
  }

  private void despawnHellbound()
  {
    for (SimpleSpawner spawnToDelete : _spawnList) {
      spawnToDelete.deleteAll();
    }
    _spawnList.clear();
  }

  private static void doorHandler()
  {
    int NativeHell_native0131 = 19250001;
    int NativeHell_native0132 = 19250002;
    int NativeHell_native0133 = 19250003;
    int NativeHell_native0134 = 19250004;

    int sdoor_trans_mesh00 = 20250002;
    int Hell_gate_door = 20250001;

    int[] _doors = { 19250001, 19250002, 19250003, 19250004, 20250002, 20250001 };

    for (int i = 0; i < _doors.length; i++) {
      ReflectionUtils.getDoor(_doors[i]).closeMe();
    }
    switch (getHellboundLevel())
    {
    case 0:
      break;
    case 1:
      break;
    case 2:
      break;
    case 3:
      break;
    case 4:
      break;
    case 5:
      ReflectionUtils.getDoor(19250001).openMe();
      ReflectionUtils.getDoor(19250002).openMe();
      break;
    case 6:
      ReflectionUtils.getDoor(19250001).openMe();
      ReflectionUtils.getDoor(19250002).openMe();
      break;
    case 7:
      ReflectionUtils.getDoor(19250001).openMe();
      ReflectionUtils.getDoor(19250002).openMe();
      ReflectionUtils.getDoor(20250002).openMe();
      break;
    case 8:
      ReflectionUtils.getDoor(19250001).openMe();
      ReflectionUtils.getDoor(19250002).openMe();
      ReflectionUtils.getDoor(20250002).openMe();
      break;
    case 9:
      ReflectionUtils.getDoor(19250001).openMe();
      ReflectionUtils.getDoor(19250002).openMe();
      ReflectionUtils.getDoor(20250002).openMe();
      ReflectionUtils.getDoor(20250001).openMe();
      break;
    case 10:
      ReflectionUtils.getDoor(19250001).openMe();
      ReflectionUtils.getDoor(19250002).openMe();
      ReflectionUtils.getDoor(20250002).openMe();
      ReflectionUtils.getDoor(20250001).openMe();
      break;
    case 11:
      ReflectionUtils.getDoor(19250001).openMe();
      ReflectionUtils.getDoor(19250002).openMe();
      ReflectionUtils.getDoor(20250002).openMe();
      ReflectionUtils.getDoor(20250001).openMe();
      break;
    }
  }

  public class HellboundSpawn
  {
    private final int _npcId;
    private final Location _loc;
    private final int _amount;
    private final Territory _spawnTerritory;
    private final int _respawn;
    private final int _respawnRnd;
    private final int[] _stages;

    public HellboundSpawn(int npcId, Location loc, int amount, Territory spawnTerritory, int respawn, int respawnRnd, int[] stages)
    {
      _npcId = npcId;
      _loc = loc;
      _amount = amount;
      _spawnTerritory = spawnTerritory;
      _respawn = respawn;
      _respawnRnd = respawnRnd;
      _stages = stages;
    }

    public int getNpcId()
    {
      return _npcId;
    }

    public Location getLoc()
    {
      return _loc;
    }

    public int getAmount()
    {
      return _amount;
    }

    public Territory getSpawnTerritory()
    {
      return _spawnTerritory;
    }

    public int getRespawn()
    {
      return _respawn;
    }

    public int getRespawnRnd()
    {
      return _respawnRnd;
    }

    public int[] getStages()
    {
      return _stages;
    }
  }

  private class StageCheckTask extends RunnableImpl
  {
    private StageCheckTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      if (HellboundManager._initialStage != HellboundManager.getHellboundLevel())
      {
        HellboundManager.this.despawnHellbound();
        HellboundManager.this.spawnHellbound();
        HellboundManager.access$500();
        HellboundManager.access$202(HellboundManager.getHellboundLevel());
      }
    }
  }

  private class DeathListener
    implements OnDeathListener
  {
    private DeathListener()
    {
    }

    public void onDeath(Creature cha, Creature killer)
    {
      if ((killer == null) || (!cha.isMonster()) || (!killer.isPlayable())) {
        return;
      }
      switch (HellboundManager.getHellboundLevel())
      {
      case 0:
        break;
      case 1:
        switch (cha.getNpcId())
        {
        case 22320:
        case 22321:
        case 22324:
        case 22325:
          HellboundManager.addConfidence(1L);
          break;
        case 22327:
        case 22328:
        case 22329:
          HellboundManager.addConfidence(3L);
          break;
        case 22322:
        case 22323:
        case 32299:
          HellboundManager.reduceConfidence(10L);
        }

        break;
      case 2:
        switch (cha.getNpcId())
        {
        case 18463:
        case 18464:
          HellboundManager.addConfidence(5L);
          break;
        case 22322:
        case 22323:
        case 32299:
          HellboundManager.reduceConfidence(10L);
        }

        break;
      case 3:
        switch (cha.getNpcId())
        {
        case 22342:
        case 22343:
          HellboundManager.addConfidence(3L);
          break;
        case 22341:
          HellboundManager.addConfidence(100L);
          break;
        case 22322:
        case 22323:
        case 32299:
          HellboundManager.reduceConfidence(10L);
        }

        break;
      case 4:
        switch (cha.getNpcId())
        {
        case 18465:
          HellboundManager.addConfidence(10000L);
          ServerVariables.set("HB_derekKilled", true);
          break;
        case 22322:
        case 22323:
        case 32299:
          HellboundManager.reduceConfidence(10L);
        }

        break;
      case 5:
        switch (cha.getNpcId())
        {
        case 22448:
          HellboundManager.reduceConfidence(50L);
        }

        break;
      case 6:
        switch (cha.getNpcId())
        {
        case 22326:
          HellboundManager.addConfidence(500L);
          break;
        case 18484:
          HellboundManager.addConfidence(5L);
        }

        break;
      case 8:
        switch (cha.getNpcId())
        {
        case 18466:
          HellboundManager.addConfidence(10000L);
          ServerVariables.set("HB_captainKilled", true);
        }

        break;
      case 7:
      }
    }
  }
}