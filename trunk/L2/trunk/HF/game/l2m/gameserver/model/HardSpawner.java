package l2m.gameserver.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2p.commons.collections.MultiValueSet;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.templates.spawn.SpawnNpcInfo;
import l2m.gameserver.templates.spawn.SpawnRange;
import l2m.gameserver.templates.spawn.SpawnTemplate;

public class HardSpawner extends Spawner
{
  public static final long serialVersionUID = 1L;
  private final SpawnTemplate _template;
  private int _pointIndex;
  private int _npcIndex;
  private List<NpcInstance> _reSpawned = new CopyOnWriteArrayList();

  public HardSpawner(SpawnTemplate template)
  {
    _template = template;
    _spawned = new CopyOnWriteArrayList();
  }

  public void decreaseCount(NpcInstance oldNpc)
  {
    oldNpc.setSpawn(null);
    oldNpc.deleteMe();

    _spawned.remove(oldNpc);

    SpawnNpcInfo npcInfo = getNextNpcInfo();

    NpcInstance npc = npcInfo.getTemplate().getNewInstance();
    npc.setSpawn(this);

    _reSpawned.add(npc);

    decreaseCount0(npcInfo.getTemplate(), npc, oldNpc.getDeadTime());
  }

  public NpcInstance doSpawn(boolean spawn)
  {
    SpawnNpcInfo npcInfo = getNextNpcInfo();

    return doSpawn0(npcInfo.getTemplate(), spawn, npcInfo.getParameters());
  }

  protected NpcInstance initNpc(NpcInstance mob, boolean spawn, MultiValueSet<String> set)
  {
    _reSpawned.remove(mob);

    SpawnRange range = _template.getSpawnRange(getNextRangeId());
    mob.setSpawnRange(range);
    return initNpc0(mob, range.getRandomLoc(getReflection().getGeoIndex()), spawn, set);
  }

  public int getCurrentNpcId()
  {
    SpawnNpcInfo npcInfo = _template.getNpcId(_npcIndex);
    return npcInfo.getTemplate().npcId;
  }

  public SpawnRange getCurrentSpawnRange()
  {
    return _template.getSpawnRange(_pointIndex);
  }

  public void respawnNpc(NpcInstance oldNpc)
  {
    initNpc(oldNpc, true, StatsSet.EMPTY);
  }

  public void deleteAll()
  {
    super.deleteAll();

    for (NpcInstance npc : _reSpawned)
    {
      npc.setSpawn(null);
      npc.deleteMe();
    }

    _reSpawned.clear();
  }

  private synchronized SpawnNpcInfo getNextNpcInfo()
  {
    int old = _npcIndex++;
    if (_npcIndex >= _template.getNpcSize()) {
      _npcIndex = 0;
    }
    SpawnNpcInfo npcInfo = _template.getNpcId(old);
    if (npcInfo.getMax() > 0)
    {
      int count = 0;
      for (NpcInstance npc : _spawned) {
        if (npc.getNpcId() == npcInfo.getTemplate().getNpcId())
          count++;
      }
      if (count >= npcInfo.getMax())
        return getNextNpcInfo();
    }
    return npcInfo;
  }

  private synchronized int getNextRangeId()
  {
    int old = _pointIndex++;
    if (_pointIndex >= _template.getSpawnRangeSize())
      _pointIndex = 0;
    return old;
  }

  public HardSpawner clone()
  {
    HardSpawner spawnDat = new HardSpawner(_template);
    spawnDat.setAmount(_maximumCount);
    spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
    spawnDat.setRespawnTime(0);
    return spawnDat;
  }
}