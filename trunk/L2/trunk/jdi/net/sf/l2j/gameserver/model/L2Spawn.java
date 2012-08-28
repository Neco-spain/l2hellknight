package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Territory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2Spawn
{
  protected static final Logger _log = Logger.getLogger(L2Spawn.class.getName());
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
  private Constructor _constructor;
  protected boolean _doRespawn;
  private L2NpcInstance _lastSpawn;
  private static List<SpawnListener> _spawnListeners = new FastList();

  public boolean isRespawnable()
  {
    return _doRespawn;
  }

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

    Class[] parameters = { Integer.TYPE, Class.forName("net.sf.l2j.gameserver.templates.L2NpcTemplate") };
    _constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + implementationName + "Instance").getConstructor(parameters);
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

    if ((isRespawnable()) && (_doRespawn) && (_scheduledCount + _currentCount < _maximumCount))
    {
      _scheduledCount += 1;

      ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(oldNpc), _respawnDelay);
    }
  }

  public int init()
  {
    return init(false);
  }

  public int init(boolean firstspawn)
  {
    while (_currentCount < _maximumCount)
    {
      doSpawn(false, firstspawn);
    }
    _doRespawn = true;

    return _currentCount;
  }

  public L2NpcInstance spawnOne(boolean val)
  {
    return doSpawn(val);
  }

  public L2NpcInstance doSpawn(boolean isSummonSpawn)
  {
    return doSpawn(isSummonSpawn, false);
  }

  public L2NpcInstance doSpawn()
  {
    return doSpawn(false, false);
  }

  public void stopRespawn()
  {
    _doRespawn = false;
  }

  public void startRespawn()
  {
    _doRespawn = true;
  }

  public L2NpcInstance doSpawn(boolean isSummonSpawn, boolean firstspawn)
  {
    L2NpcInstance mob = null;
    try
    {
      if ((_template.type.equalsIgnoreCase("L2Pet")) || (_template.type.equalsIgnoreCase("L2Minion")) || (_template.type.equalsIgnoreCase("L2EffectPoint")))
      {
        _currentCount += 1;
        return mob;
      }

      Object[] parameters = { Integer.valueOf(IdFactory.getInstance().getNextId()), _template };

      L2Object tmp = (L2Object)_constructor.newInstance(parameters);

      if ((isSummonSpawn) && ((tmp instanceof L2Character))) {
        ((L2Character)tmp).setShowSummonAnimation(isSummonSpawn);
      }
      if (!(tmp instanceof L2NpcInstance))
        return mob;
      mob = (L2NpcInstance)tmp;
      return intializeNpcInstance(mob, firstspawn);
    }
    catch (Exception e)
    {
      _currentCount += 1;
      if (Config.DEBUG)
      {
        _log.warning("NPC " + _template.npcId + " class not found: " + _template.type);
      }
    }
    return mob;
  }

  private L2NpcInstance intializeNpcInstance(L2NpcInstance mob, boolean firstspawn)
  {
    int newlocz;
    int newlocx;
    int newlocy;
    int newlocz;
    if ((getLocx() == 0) && (getLocy() == 0))
    {
      if (getLocation() == 0) {
        return mob;
      }
      int[] p = Territory.getInstance().getRandomPoint(getLocation());

      int newlocx = p[0];
      int newlocy = p[1];
      newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, p[2], p[3], _id);
    }
    else
    {
      newlocx = getLocx();
      newlocy = getLocy();
      int newlocz;
      if (Config.GEODATA)
        newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, getLocz(), getLocz(), _id);
      else
        newlocz = getLocz();
    }
    L2Effect[] effects = mob.getAllEffects();
    for (L2Effect f : effects)
    {
      if (f != null)
        mob.removeEffect(f);
    }
    mob.setDecayed(false);
    mob.getStatus().setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
    if (getHeading() == -1)
    {
      mob.setHeading(Rnd.nextInt(61794));
    }
    else
    {
      mob.setHeading(getHeading());
    }

    if ((mob instanceof L2Attackable)) {
      ((L2Attackable)mob).setChampion(false);
    }
    if (Config.CHAMPION_ENABLE)
    {
      if (((mob instanceof L2MonsterInstance)) && (!(mob instanceof L2RaidBossInstance)) && (!(mob instanceof L2GrandBossInstance)) && (!getTemplate().isQuestMonster) && (!mob.isRaid()) && (!mob.isRaidMinion()) && (Config.CHAMPION_FREQUENCY > 0) && (mob.getLevel() >= Config.CHAMPION_MIN_LVL) && (mob.getLevel() <= Config.CHAMPION_MAX_LVL))
      {
        int random = Rnd.get(100);

        if (random < Config.CHAMPION_FREQUENCY)
          ((L2Attackable)mob).setChampion(true);
      }
    }
    mob.setSpawn(this);
    mob.spawnMe(newlocx, newlocy, newlocz, firstspawn);
    notifyNpcSpawned(mob);
    _lastSpawn = mob;
    _currentCount += 1;
    return mob;
  }

  public static void addSpawnListener(SpawnListener listener)
  {
    synchronized (_spawnListeners)
    {
      _spawnListeners.add(listener);
    }
  }

  public static void removeSpawnListener(SpawnListener listener)
  {
    synchronized (_spawnListeners)
    {
      _spawnListeners.remove(listener);
    }
  }

  public static void notifyNpcSpawned(L2NpcInstance npc)
  {
    synchronized (_spawnListeners)
    {
      for (SpawnListener listener : _spawnListeners)
      {
        listener.npcSpawned(npc);
      }
    }
  }

  public void setRespawnDelay(int i)
  {
    if (i < 0) {
      _log.warning("respawn delay is negative for spawnId:" + _id);
    }
    if (i < 60) {
      i = 60;
    }
    _respawnDelay = (i * 1000);
  }

  public L2NpcInstance getLastSpawn()
  {
    return _lastSpawn;
  }

  public void respawnNpc(L2NpcInstance oldNpc)
  {
    oldNpc.refreshID();
    intializeNpcInstance(oldNpc, false);
  }

  public L2NpcTemplate getTemplate()
  {
    return _template;
  }

  class SpawnTask
    implements Runnable
  {
    private L2NpcInstance _oldNpc;

    public SpawnTask(L2NpcInstance pOldNpc)
    {
      _oldNpc = pOldNpc;
    }

    public void run()
    {
      try
      {
        if (_doRespawn)
          respawnNpc(_oldNpc);
      }
      catch (Exception e)
      {
        L2Spawn._log.warning(e.getMessage());
      }

      _scheduledCount -= 1;
    }
  }
}