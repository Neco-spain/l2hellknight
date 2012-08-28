package l2p.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2p.gameserver.Config;
import l2p.gameserver.GameTimeController;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.data.xml.holder.SpawnHolder;
import l2p.gameserver.data.xml.parser.SpawnParser;
import l2p.gameserver.listener.game.OnDayNightChangeListener;
import l2p.gameserver.listener.game.OnSSPeriodListener;
import l2p.gameserver.model.HardSpawner;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.templates.spawn.PeriodOfDay;
import l2p.gameserver.templates.spawn.SpawnTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnManager
{
  private static final Logger _log = LoggerFactory.getLogger(SpawnManager.class);

  private static SpawnManager _instance = new SpawnManager();
  private static final String DAWN_GROUP = "dawn_spawn";
  private static final String DUSK_GROUP = "dusk_spawn";
  private Map<String, List<Spawner>> _spawns = new ConcurrentHashMap();
  private Listeners _listeners = new Listeners(null);

  public static SpawnManager getInstance()
  {
    return _instance;
  }

  private SpawnManager()
  {
    for (Map.Entry entry : SpawnHolder.getInstance().getSpawns().entrySet()) {
      fillSpawn((String)entry.getKey(), (List)entry.getValue());
    }
    GameTimeController.getInstance().addListener(_listeners);
    SevenSigns.getInstance().addListener(_listeners);
  }

  public List<Spawner> fillSpawn(String group, List<SpawnTemplate> templateList)
  {
    if (Config.DONTLOADSPAWN) {
      return Collections.emptyList();
    }
    List spawnerList = (List)_spawns.get(group);
    if (spawnerList == null) {
      _spawns.put(group, spawnerList = new ArrayList(templateList.size()));
    }
    for (SpawnTemplate template : templateList)
    {
      HardSpawner spawner = new HardSpawner(template);
      spawnerList.add(spawner);

      NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(spawner.getCurrentNpcId());

      if ((Config.RATE_MOB_SPAWN > 1) && (npcTemplate.getInstanceClass() == MonsterInstance.class) && (npcTemplate.level >= Config.RATE_MOB_SPAWN_MIN_LEVEL) && (npcTemplate.level <= Config.RATE_MOB_SPAWN_MAX_LEVEL))
        spawner.setAmount(template.getCount() * Config.RATE_MOB_SPAWN);
      else {
        spawner.setAmount(template.getCount());
      }
      spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
      spawner.setReflection(ReflectionManager.DEFAULT);
      spawner.setRespawnTime(0);

      if ((npcTemplate.isRaid) && (group.equals(PeriodOfDay.NONE.name()))) {
        RaidBossSpawnManager.getInstance().addNewSpawn(npcTemplate.getNpcId(), spawner);
      }
    }
    return spawnerList;
  }

  public void spawnAll()
  {
    spawn(PeriodOfDay.NONE.name());
    if (Config.ALLOW_EVENT_GATEKEEPER)
      spawn("event_gatekeeper");
    if (!Config.ALLOW_CLASS_MASTERS_LIST.isEmpty())
      spawn("class_master");
  }

  public void spawn(String group)
  {
    List spawnerList = (List)_spawns.get(group);
    if (spawnerList == null) {
      return;
    }
    int npcSpawnCount = 0;

    for (Spawner spawner : spawnerList)
    {
      npcSpawnCount += spawner.init();

      if ((npcSpawnCount % 1000 == 0) && (npcSpawnCount != 0))
        _log.info("SpawnManager: spawned " + npcSpawnCount + " npc for group: " + group);
    }
    _log.info("SpawnManager: spawned " + npcSpawnCount + " npc; spawns: " + spawnerList.size() + "; group: " + group);
  }

  public void despawn(String group)
  {
    List spawnerList = (List)_spawns.get(group);
    if (spawnerList == null) {
      return;
    }
    for (Spawner spawner : spawnerList)
      spawner.deleteAll();
  }

  public List<Spawner> getSpawners(String group)
  {
    List list = (List)_spawns.get(group);
    return list == null ? Collections.emptyList() : list;
  }

  public void reloadSpawnInfo()
  {
    SpawnHolder.getInstance().clear();
    SpawnParser.getInstance().load();

    _spawns.clear();

    for (Map.Entry entry : SpawnHolder.getInstance().getSpawns().entrySet())
      fillSpawn((String)entry.getKey(), (List)entry.getValue());
  }

  public void reloadAll()
  {
    RaidBossSpawnManager.getInstance().cleanUp();
    for (List spawnerList : _spawns.values()) {
      for (Spawner spawner : spawnerList)
        spawner.deleteAll();
    }
    reloadSpawnInfo();

    RaidBossSpawnManager.getInstance().reloadBosses();

    spawnAll();

    int mode = 0;
    if (SevenSigns.getInstance().getCurrentPeriod() == 3) {
      mode = SevenSigns.getInstance().getCabalHighestScore();
    }
    _listeners.onPeriodChange(mode);

    if (GameTimeController.getInstance().isNowNight())
      _listeners.onNight();
    else
      _listeners.onDay();
  }

  private class Listeners
    implements OnDayNightChangeListener, OnSSPeriodListener
  {
    private Listeners()
    {
    }

    public void onDay()
    {
      despawn(PeriodOfDay.NIGHT.name());
      spawn(PeriodOfDay.DAY.name());
    }

    public void onNight()
    {
      despawn(PeriodOfDay.DAY.name());
      spawn(PeriodOfDay.NIGHT.name());
    }

    public void onPeriodChange(int mode)
    {
      switch (mode)
      {
      case 0:
        despawn("dawn_spawn");
        despawn("dusk_spawn");
        spawn("dawn_spawn");
        spawn("dusk_spawn");
        break;
      case 1:
        despawn("dawn_spawn");
        despawn("dusk_spawn");
        spawn("dusk_spawn");
        break;
      case 2:
        despawn("dawn_spawn");
        despawn("dusk_spawn");
        spawn("dawn_spawn");
      }
    }
  }
}