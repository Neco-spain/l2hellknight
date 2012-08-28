package net.sf.l2j.gameserver.instancemanager;

import java.util.logging.Logger;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.util.log.AbstractLogger;

public class DayNightSpawnManager
{
  private static Logger _log = AbstractLogger.getLogger(DayNightSpawnManager.class.getName());
  private static DayNightSpawnManager _instance;
  private static FastMap<L2Spawn, L2NpcInstance> _dayCreatures;
  private static FastMap<L2Spawn, L2NpcInstance> _nightCreatures;
  private static FastMap<L2Spawn, L2RaidBossInstance> _bosses;

  public static DayNightSpawnManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new DayNightSpawnManager();
    _instance.load(false);
  }

  public void load(boolean reload)
  {
    if (reload)
    {
      _dayCreatures.clear();
      _nightCreatures.clear();
      _bosses.clear();
    }
    else
    {
      _dayCreatures = new FastMap().shared("DayNightSpawnManager._dayCreatures");
      _nightCreatures = new FastMap().shared("DayNightSpawnManager._nightCreatures");
      _bosses = new FastMap().shared("DayNightSpawnManager._bosses");
    }
  }

  public void addDayCreature(L2Spawn spawnDat)
  {
    if (!_dayCreatures.containsKey(spawnDat))
      _dayCreatures.put(spawnDat, null);
  }

  public void addNightCreature(L2Spawn spawnDat)
  {
    if (!_nightCreatures.containsKey(spawnDat))
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

  private void spawnCreatures(FastMap<L2Spawn, L2NpcInstance> toDelete, FastMap<L2Spawn, L2NpcInstance> toSpawn, String UnspawnLogInfo, String SpawnLogInfo)
  {
    new Thread(new Runnable(toDelete, toSpawn)
    {
      public void run() {
        FastMap.Entry e = val$toDelete.head(); for (FastMap.Entry end = val$toDelete.tail(); (e = e.getNext()) != end; )
        {
          L2Spawn key = (L2Spawn)e.getKey();
          L2NpcInstance value = (L2NpcInstance)e.getValue();
          if ((key == null) || (value == null)) {
            continue;
          }
          value.getSpawn().stopRespawn();
          value.decayMe();
          value.deleteMe();
        }

        L2NpcInstance creature = null;
        FastMap.Entry e = val$toSpawn.head(); for (FastMap.Entry end = val$toSpawn.tail(); (e = e.getNext()) != end; )
        {
          L2Spawn key = (L2Spawn)e.getKey();
          L2NpcInstance value = (L2NpcInstance)e.getValue();
          if (key == null) {
            continue;
          }
          if (value == null)
          {
            value = key.doSpawn();
            if (value == null) {
              continue;
            }
            val$toSpawn.put(key, value);
            value.setCurrentHp(value.getMaxHp());
            value.setCurrentMp(value.getMaxMp());
            value.getSpawn().startRespawn();
            if (value.isDecayed())
              value.setDecayed(false);
            if (value.isDead())
              value.doRevive();
          }
          else
          {
            value.getSpawn().startRespawn();
            if (value.isDecayed())
              value.setDecayed(false);
            if (value.isDead())
              value.doRevive();
            value.setCurrentHp(value.getMaxHp());
            value.setCurrentMp(value.getMaxMp());
            value.spawnMe();
          }
        }
      }
    }).start();
  }

  private void changeMode(int mode)
  {
    if ((_nightCreatures.isEmpty()) && (_dayCreatures.isEmpty())) {
      return;
    }
    switch (mode)
    {
    case 0:
      spawnDayCreatures();
      specialNightBoss(0);
      break;
    case 1:
      spawnNightCreatures();
      specialNightBoss(1);
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
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void cleanUp()
  {
    _nightCreatures.clear();
    _dayCreatures.clear();
    _bosses.clear();
  }

  private void specialNightBoss(int mode)
  {
    try
    {
      boss = null;
      for (L2Spawn spawn : _bosses.keySet())
      {
        if (spawn == null) {
          continue;
        }
        boss = (L2RaidBossInstance)_bosses.get(spawn);

        if ((boss == null) && (mode == 1))
        {
          boss = (L2RaidBossInstance)spawn.doSpawn();
          RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
          _bosses.remove(spawn);
          _bosses.put(spawn, boss);
          continue;
        }

        if (((boss == null) && (mode == 0)) || 
          (boss == null)) {
          continue;
        }
        if ((boss.getNpcId() == 25328) && (boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE)))
          handleHellmans(boss, mode);
        return;
      }
    }
    catch (Exception e)
    {
      L2RaidBossInstance boss;
      e.printStackTrace();
    }
  }

  private void handleHellmans(L2RaidBossInstance boss, int mode)
  {
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
    if (_bosses.containsKey(spawnDat)) {
      return (L2RaidBossInstance)_bosses.get(spawnDat);
    }
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