package net.sf.l2j.gameserver.instancemanager;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;

public class DayNightSpawnManager
{
  private static Logger _log = Logger.getLogger(DayNightSpawnManager.class.getName());
  private static DayNightSpawnManager _instance;
  private static Map<L2Spawn, L2NpcInstance> _dayCreatures;
  private static Map<L2Spawn, L2NpcInstance> _nightCreatures;
  private static Map<L2Spawn, L2RaidBossInstance> _bosses;

  public static DayNightSpawnManager getInstance()
  {
    if (_instance == null)
      _instance = new DayNightSpawnManager();
    return _instance;
  }

  private DayNightSpawnManager()
  {
    _dayCreatures = new FastMap();
    _nightCreatures = new FastMap();
    _bosses = new FastMap();

    _log.info("DayNightSpawnManager: Day/Night handler initialised");
  }

  public void addDayCreature(L2Spawn spawnDat)
  {
    if (_dayCreatures.containsKey(spawnDat))
    {
      _log.warning("DayNightSpawnManager: Spawn already added into day map");
      return;
    }

    _dayCreatures.put(spawnDat, null);
  }

  public void addNightCreature(L2Spawn spawnDat)
  {
    if (_nightCreatures.containsKey(spawnDat))
    {
      _log.warning("DayNightSpawnManager: Spawn already added into night map");
      return;
    }

    _nightCreatures.put(spawnDat, null);
  }

  public void spawnDayCreatures()
  {
    spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
  }

  public void spawnNightCreatures()
  {
    spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
  }

  private void spawnCreatures(Map<L2Spawn, L2NpcInstance> UnSpawnCreatures, Map<L2Spawn, L2NpcInstance> SpawnCreatures, String UnspawnLogInfo, String SpawnLogInfo)
  {
    try
    {
      if (UnSpawnCreatures.size() != 0)
      {
        int i = 0;
        for (L2NpcInstance dayCreature : UnSpawnCreatures.values())
        {
          if (dayCreature == null)
            continue;
          dayCreature.getSpawn().stopRespawn();
          dayCreature.deleteMe();
          i++;
        }
        _log.info("DayNightSpawnManager: Deleted " + i + " " + UnspawnLogInfo + " creatures");
      }

      int i = 0;
      L2NpcInstance creature = null;
      for (L2Spawn spawnDat : SpawnCreatures.keySet())
      {
        if (SpawnCreatures.get(spawnDat) == null)
        {
          creature = spawnDat.doSpawn();
          if (creature == null)
            continue;
          SpawnCreatures.remove(spawnDat);
          SpawnCreatures.put(spawnDat, creature);
          creature.setCurrentHp(creature.getMaxHp());
          creature.setCurrentMp(creature.getMaxMp());
          creature = (L2NpcInstance)SpawnCreatures.get(spawnDat);
          creature.getSpawn().startRespawn();
        }
        else
        {
          creature = (L2NpcInstance)SpawnCreatures.get(spawnDat);
          if (creature == null)
            continue;
          creature.getSpawn().startRespawn();
          creature.setCurrentHp(creature.getMaxHp());
          creature.setCurrentMp(creature.getMaxMp());
          creature.spawnMe();
        }

        i++;
      }

      _log.info("DayNightSpawnManager: Spawning " + i + " " + SpawnLogInfo + " creatures"); } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void changeMode(int mode) {
    if ((_nightCreatures.size() == 0) && (_dayCreatures.size() == 0)) {
      return;
    }
    switch (mode) {
    case 0:
      spawnDayCreatures();
      specialNightBoss(0);
      break;
    case 1:
      spawnNightCreatures();
      specialNightBoss(1);
      break;
    default:
      _log.warning("DayNightSpawnManager: Wrong mode sent");
    }
  }

  public void notifyChangeMode()
  {
    try
    {
      if (GameTimeController.getInstance().isNowNight())
        changeMode(1);
      else
        changeMode(0); 
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void cleanUp() {
    _nightCreatures.clear();
    _dayCreatures.clear();
    _bosses.clear();
  }

  private void specialNightBoss(int mode)
  {
    try {
      for (L2Spawn spawn : _bosses.keySet())
      {
        L2RaidBossInstance boss = (L2RaidBossInstance)_bosses.get(spawn);

        if ((boss == null) && (mode == 1))
        {
          boss = (L2RaidBossInstance)spawn.doSpawn();
          RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
          _bosses.remove(spawn);
          _bosses.put(spawn, boss);
          continue;
        }

        if ((boss == null) && (mode == 0)) {
          continue;
        }
        if ((boss.getNpcId() == 25328) && (boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE)))
        {
          handleHellmans(boss, mode);
        }return;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleHellmans(L2RaidBossInstance boss, int mode) {
    switch (mode)
    {
    case 0:
      boss.deleteMe();
      _log.info("DayNightSpawnManager: Deleting Hellman raidboss");
      break;
    case 1:
      boss.spawnMe();
      _log.info("DayNightSpawnManager: Spawning Hellman raidboss");
    }
  }

  public L2RaidBossInstance handleBoss(L2Spawn spawnDat)
  {
    if (_bosses.containsKey(spawnDat)) return (L2RaidBossInstance)_bosses.get(spawnDat);

    if (GameTimeController.getInstance().isNowNight())
    {
      L2RaidBossInstance raidboss = (L2RaidBossInstance)spawnDat.doSpawn();
      _bosses.put(spawnDat, raidboss);

      return raidboss;
    }

    _bosses.put(spawnDat, null);

    return null;
  }
}