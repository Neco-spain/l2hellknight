package l2p.gameserver.model;

import java.util.List;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.util.Rnd;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.EventOwner;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.instances.MinionInstance;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.taskmanager.SpawnTaskManager;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.templates.spawn.SpawnRange;
import l2p.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Spawner extends EventOwner
  implements Cloneable
{
  protected static final Logger _log = LoggerFactory.getLogger(Spawner.class);
  protected static final int MIN_RESPAWN_DELAY = 20;
  protected int _maximumCount;
  protected int _referenceCount;
  protected int _currentCount;
  protected int _scheduledCount;
  protected int _respawnDelay;
  protected int _respawnDelayRandom;
  protected int _nativeRespawnDelay;
  protected int _respawnTime;
  protected boolean _doRespawn;
  protected NpcInstance _lastSpawn;
  protected List<NpcInstance> _spawned;
  protected Reflection _reflection = ReflectionManager.DEFAULT;

  public void decreaseScheduledCount()
  {
    if (_scheduledCount > 0)
      _scheduledCount -= 1;
  }

  public boolean isDoRespawn()
  {
    return _doRespawn;
  }

  public Reflection getReflection()
  {
    return _reflection;
  }

  public void setReflection(Reflection reflection)
  {
    _reflection = reflection;
  }

  public int getRespawnDelay()
  {
    return _respawnDelay;
  }

  public int getNativeRespawnDelay()
  {
    return _nativeRespawnDelay;
  }

  public int getRespawnDelayRandom()
  {
    return _respawnDelayRandom;
  }

  public int getRespawnDelayWithRnd()
  {
    return _respawnDelayRandom == 0 ? _respawnDelay : Rnd.get(_respawnDelay - _respawnDelayRandom, _respawnDelay);
  }

  public int getRespawnTime()
  {
    return _respawnTime;
  }

  public NpcInstance getLastSpawn()
  {
    return _lastSpawn;
  }

  public void setAmount(int amount)
  {
    if (_referenceCount == 0)
      _referenceCount = amount;
    _maximumCount = amount;
  }

  public void deleteAll()
  {
    stopRespawn();
    for (NpcInstance npc : _spawned)
      npc.deleteMe();
    _spawned.clear();
    _respawnTime = 0;
    _scheduledCount = 0;
    _currentCount = 0; } 
  public abstract void decreaseCount(NpcInstance paramNpcInstance);

  public abstract NpcInstance doSpawn(boolean paramBoolean);

  public abstract void respawnNpc(NpcInstance paramNpcInstance);

  protected abstract NpcInstance initNpc(NpcInstance paramNpcInstance, boolean paramBoolean, MultiValueSet<String> paramMultiValueSet);

  public abstract int getCurrentNpcId();

  public abstract SpawnRange getCurrentSpawnRange();

  public int init() { while (_currentCount + _scheduledCount < _maximumCount) {
      doSpawn(false);
    }
    _doRespawn = true;

    return _currentCount;
  }

  public NpcInstance spawnOne()
  {
    return doSpawn(false);
  }

  public void stopRespawn()
  {
    _doRespawn = false;
  }

  public void startRespawn()
  {
    _doRespawn = true;
  }

  public List<NpcInstance> getAllSpawned()
  {
    return _spawned;
  }

  public NpcInstance getFirstSpawned()
  {
    List npcs = getAllSpawned();
    return npcs.size() > 0 ? (NpcInstance)npcs.get(0) : null;
  }

  public void setRespawnDelay(int respawnDelay, int respawnDelayRandom)
  {
    if (respawnDelay < 0) {
      _log.warn("respawn delay is negative");
    }
    _nativeRespawnDelay = respawnDelay;
    _respawnDelay = respawnDelay;
    _respawnDelayRandom = respawnDelayRandom;
  }

  public void setRespawnDelay(int respawnDelay)
  {
    setRespawnDelay(respawnDelay, 0);
  }

  public void setRespawnTime(int respawnTime)
  {
    _respawnTime = respawnTime;
  }

  protected NpcInstance doSpawn0(NpcTemplate template, boolean spawn, MultiValueSet<String> set)
  {
    if ((template.isInstanceOf(PetInstance.class)) || (template.isInstanceOf(MinionInstance.class)))
    {
      _currentCount += 1;
      return null;
    }

    NpcInstance tmp = template.getNewInstance();
    if (tmp == null) {
      return null;
    }
    if (!spawn) {
      spawn = _respawnTime <= System.currentTimeMillis() / 1000L + 20L;
    }
    return initNpc(tmp, spawn, set);
  }

  protected NpcInstance initNpc0(NpcInstance mob, Location newLoc, boolean spawn, MultiValueSet<String> set)
  {
    mob.setParameters(set);

    mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);

    mob.setSpawn(this);

    mob.setSpawnedLoc(newLoc);

    mob.setUnderground(GeoEngine.getHeight(newLoc, getReflection().getGeoIndex()) < GeoEngine.getHeight(newLoc.clone().changeZ(5000), getReflection().getGeoIndex()));

    for (GlobalEvent e : getEvents()) {
      mob.addEvent(e);
    }
    if (spawn)
    {
      mob.setReflection(getReflection());

      if (mob.isMonster()) {
        ((MonsterInstance)mob).setChampion();
      }

      mob.spawnMe(newLoc);

      _currentCount += 1;
    }
    else
    {
      mob.setLoc(newLoc);

      _scheduledCount += 1;

      SpawnTaskManager.getInstance().addSpawnTask(mob, _respawnTime * 1000L - System.currentTimeMillis());
    }

    _spawned.add(mob);
    _lastSpawn = mob;
    return mob;
  }

  public void decreaseCount0(NpcTemplate template, NpcInstance spawnedNpc, long deadTime)
  {
    _currentCount -= 1;

    if (_currentCount < 0) {
      _currentCount = 0;
    }
    if (_respawnDelay == 0) {
      return;
    }
    if ((_doRespawn) && (_scheduledCount + _currentCount < _maximumCount))
    {
      _scheduledCount += 1;

      long delay = ()getRespawnDelayWithRnd() * 1000L;
      delay = Math.max(1000L, delay - deadTime);

      _respawnTime = (int)((System.currentTimeMillis() + delay) / 1000L);

      SpawnTaskManager.getInstance().addSpawnTask(spawnedNpc, delay);
    }
  }
}