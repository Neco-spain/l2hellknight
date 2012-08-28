package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.entity.SpawnTerritory;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.log.AbstractLogger;

public class L2Spawn
{
  protected static final Logger _log = AbstractLogger.getLogger(L2Spawn.class.getName());
  private L2NpcTemplate _template;
  private int _id;
  private int _location;
  private int _maximumCount;
  private int _currentCount;
  protected int _scheduledCount;
  private int _locX;
  private int _locY;
  private int _locZ;
  private int _heading;
  private int _respawnDelay;
  private int _respawnMinDelay;
  private int _respawnMaxDelay;
  private Constructor<?> _constructor;
  private boolean _doRespawn;
  private L2NpcInstance _lastSpawn;
  private static ConcurrentLinkedQueue<SpawnListener> _spawnListeners = new ConcurrentLinkedQueue();

  private long _lastKill = 0L;
  private boolean _isFree = false;
  private SpawnTerritory _spawnTerr = null;

  public L2Spawn(L2NpcTemplate mobTemplate)
    throws SecurityException, ClassNotFoundException, NoSuchMethodException
  {
    _template = mobTemplate;

    if (_template == null) {
      return;
    }

    String implementationName = _template.type;

    if (mobTemplate.npcId == 30995) {
      implementationName = "L2RaceManager";
    }

    if ((mobTemplate.npcId >= 31046) && (mobTemplate.npcId <= 31053)) {
      implementationName = "L2SymbolMaker";
    }

    try
    {
      Class[] parameters = { Integer.TYPE, Class.forName("net.sf.l2j.gameserver.templates.L2NpcTemplate") };
      _constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + implementationName + "Instance").getConstructor(parameters);
    } catch (ClassNotFoundException ex) {
      try {
        Class[] parameters = { Integer.TYPE, Class.forName("net.sf.l2j.gameserver.templates.L2NpcTemplate") };
        _constructor = Class.forName("scripts.ai." + implementationName).getConstructor(parameters);
      } catch (ClassNotFoundException ex2) {
        _log.log(Level.WARNING, "Class not found", ex);
        _log.log(Level.WARNING, "Class not found", ex2);
      }
    }
  }

  public int getAmount()
  {
    return _maximumCount;
  }

  public int getId()
  {
    return _id;
  }

  public int getLocation()
  {
    return _location;
  }

  public int getLocx()
  {
    return _locX;
  }

  public int getLocy()
  {
    return _locY;
  }

  public int getLocz()
  {
    return _locZ;
  }

  public int getNpcid()
  {
    return _template.npcId;
  }

  public int getHeading()
  {
    return _heading;
  }

  public int getRespawnDelay()
  {
    return _respawnDelay;
  }

  public int getRespawnMinDelay()
  {
    return _respawnMinDelay;
  }

  public int getRespawnMaxDelay()
  {
    return _respawnMaxDelay;
  }

  public void setAmount(int amount)
  {
    _maximumCount = amount;
  }

  public void setId(int id)
  {
    _id = id;
  }

  public void setLocation(int location)
  {
    _location = location;
  }

  public void setRespawnMinDelay(int date)
  {
    _respawnMinDelay = date;
  }

  public void setRespawnMaxDelay(int date)
  {
    _respawnMaxDelay = date;
  }

  public void setLocx(int locx)
  {
    _locX = locx;
  }

  public void setLocy(int locy)
  {
    _locY = locy;
  }

  public void setLocz(int locz)
  {
    _locZ = locz;
  }

  public void setHeading(int heading)
  {
    _heading = heading;
  }

  public void decreaseCount(L2NpcInstance oldNpc)
  {
    _currentCount -= 1;

    if ((_doRespawn) && (_scheduledCount + _currentCount < _maximumCount))
    {
      _scheduledCount += 1;

      ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(oldNpc), _respawnDelay);
    }
  }

  public int init()
  {
    while (_currentCount < _maximumCount) {
      doSpawn();
    }
    _doRespawn = true;

    return _currentCount;
  }

  public L2NpcInstance spawnOne()
  {
    return doSpawn();
  }

  public void stopRespawn()
  {
    _doRespawn = false;
  }

  public void startRespawn()
  {
    _doRespawn = true;
  }

  public L2NpcInstance doSpawn()
  {
    L2NpcInstance mob = null;
    try
    {
      if ((_template.type.equalsIgnoreCase("L2Pet")) || (_template.type.equalsIgnoreCase("L2Minion"))) {
        _currentCount += 1;

        return mob;
      }

      Object[] parameters = { Integer.valueOf(IdFactory.getInstance().getNextId()), _template };

      Object tmp = _constructor.newInstance(parameters);

      if (!(tmp instanceof L2NpcInstance)) {
        return mob;
      }
      mob = (L2NpcInstance)tmp;
      return intializeNpcInstance(mob);
    } catch (Exception e) {
      _log.log(Level.WARNING, "NPC " + _template.npcId + " class not found", e);
    }
    return mob;
  }

  private L2NpcInstance intializeNpcInstance(L2NpcInstance mob)
  {
    mob.stopAllEffects();

    mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());

    mob.setHeading(getHeading());

    mob.setDecayed(false);

    mob.setSpawn(this);

    mob.spawnMe(getLocx(), getLocy(), mob.isL2Npc() ? getLocz() : GeoData.getInstance().getHeight(getLocx(), getLocy(), getLocz()));

    _lastSpawn = mob;

    _currentCount += 1;
    return mob;
  }

  public static void addSpawnListener(SpawnListener listener) {
    _spawnListeners.add(listener);
  }

  public static void removeSpawnListener(SpawnListener listener) {
    _spawnListeners.remove(listener);
  }

  public static void notifyNpcSpawned(L2NpcInstance npc) {
    for (SpawnListener listener : _spawnListeners)
      listener.npcSpawned(npc);
  }

  public void setRespawnDelay(int i)
  {
    if (i < 0) {
      _log.warning("respawn delay is negative for spawnId:" + _id);
    }

    if (i < 10) {
      i = 10;
    }

    _respawnDelay = (i * 1000);
  }

  public L2NpcInstance getLastSpawn() {
    return _lastSpawn;
  }

  public void respawnNpc(L2NpcInstance oldNpc)
  {
    oldNpc.refreshID();
    L2NpcInstance instance = intializeNpcInstance(oldNpc);
    instance.setRunning();
  }

  public L2NpcTemplate getTemplate() {
    return _template;
  }

  public void setLastKill(long last)
  {
    _lastKill = last;
  }

  public long getLastKill() {
    return _lastKill;
  }

  public void setTerritory(SpawnTerritory terr) {
    _spawnTerr = terr;
  }

  public SpawnTerritory getTerritory() {
    return _spawnTerr;
  }

  public void setFree() {
    _isFree = true;
  }

  public boolean isFree() {
    return _isFree;
  }

  class SpawnTask
    implements Runnable
  {
    private L2NpcInstance npc;

    public SpawnTask(L2NpcInstance npc)
    {
      this.npc = npc;
    }

    public void run()
    {
      try
      {
        respawnNpc(npc);
      } catch (Exception e) {
        L2Spawn._log.log(Level.WARNING, "", e);
      }

      _scheduledCount -= 1;
    }
  }
}