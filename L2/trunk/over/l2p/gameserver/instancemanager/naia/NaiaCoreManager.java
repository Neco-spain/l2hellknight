package l2p.gameserver.instancemanager.naia;

import l2p.commons.geometry.Polygon;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.Territory;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NaiaCoreManager
{
  private static final Logger _log = LoggerFactory.getLogger(NaiaTowerManager.class);
  private static final NaiaCoreManager _instance = new NaiaCoreManager();
  private static Zone _zone;
  private static boolean _active = false;
  private static boolean _bossSpawned = false;
  private static final Territory _coreTerritory = new Territory().add(new Polygon().add(-44789, 246305).add(-44130, 247452).add(-46092, 248606).add(-46790, 247414).add(-46139, 246304).setZmin(-14220).setZmax(-13800));
  private static final int fireSpore = 25605;
  private static final int waterSpore = 25606;
  private static final int windSpore = 25607;
  private static final int earthSpore = 25608;
  private static final int fireEpidos = 25609;
  private static final int waterEpidos = 25610;
  private static final int windEpidos = 25611;
  private static final int earthEpidos = 25612;
  private static final int teleCube = 32376;
  private static final int respawnDelay = 120;
  private static final long coreClearTime = 14400000L;
  private static final Location spawnLoc = new Location(-45496, 246744, -14209);

  public static final NaiaCoreManager getInstance()
  {
    return _instance;
  }

  public NaiaCoreManager()
  {
    _zone = ReflectionUtils.getZone("[naia_core_poison]");
    _log.info("Naia Core Manager: Loaded");
  }

  public static void launchNaiaCore()
  {
    if (isActive()) {
      return;
    }
    _active = true;
    ReflectionUtils.getDoor(18250025).closeMe();
    _zone.setActive(true);
    spawnSpores();
    ThreadPoolManager.getInstance().schedule(new ClearCore(null), 14400000L);
  }

  private static boolean isActive()
  {
    return _active;
  }

  public static void setZoneActive(boolean value)
  {
    _zone.setActive(value);
  }

  private static void spawnSpores()
  {
    spawnToRoom(25605, 10, _coreTerritory);
    spawnToRoom(25606, 10, _coreTerritory);
    spawnToRoom(25607, 10, _coreTerritory);
    spawnToRoom(25608, 10, _coreTerritory);
  }

  public static void spawnEpidos(int index)
  {
    if (!isActive())
      return;
    int epidostospawn = 0;
    switch (index)
    {
    case 1:
      epidostospawn = 25609;
      break;
    case 2:
      epidostospawn = 25610;
      break;
    case 3:
      epidostospawn = 25611;
      break;
    case 4:
      epidostospawn = 25612;
      break;
    }

    try
    {
      SimpleSpawner sp = new SimpleSpawner(epidostospawn);
      sp.setLoc(spawnLoc);
      sp.doSpawn(true);
      sp.stopRespawn();
      _bossSpawned = true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static boolean isBossSpawned()
  {
    return _bossSpawned;
  }

  public static void removeSporesAndSpawnCube()
  {
    int[] spores = { 25605, 25606, 25607, 25608 };

    for (NpcInstance spore : GameObjectsStorage.getAllByNpcId(spores, false))
      spore.deleteMe();
    try
    {
      SimpleSpawner sp = new SimpleSpawner(32376);
      sp.setLoc(spawnLoc);
      sp.doSpawn(true);
      sp.stopRespawn();
      Functions.npcShout(sp.getLastSpawn(), "Teleportation to Beleth Throne Room is available for 2 minutes");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private static void spawnToRoom(int mobId, int count, Territory territory)
  {
    for (int i = 0; i < count; i++)
    {
      try
      {
        SimpleSpawner sp = new SimpleSpawner(mobId);
        sp.setLoc(Territory.getRandomLoc(territory).setH(Rnd.get(65535)));
        sp.setRespawnDelay(120, 30);
        sp.setAmount(1);
        sp.doSpawn(true);
        sp.startRespawn();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  private static class ClearCore extends RunnableImpl
  {
    public void runImpl()
      throws Exception
    {
      int[] spores = { 25605, 25606, 25607, 25608 };

      int[] epidoses = { 25609, 25610, 25611, 25612 };

      for (NpcInstance spore : GameObjectsStorage.getAllByNpcId(spores, false))
        spore.deleteMe();
      for (NpcInstance epidos : GameObjectsStorage.getAllByNpcId(epidoses, false)) {
        epidos.deleteMe();
      }
      NaiaCoreManager.access$102(false);
      ReflectionUtils.getDoor(18250025).openMe();
      NaiaCoreManager._zone.setActive(false);
    }
  }
}