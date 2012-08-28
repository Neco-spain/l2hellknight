package net.sf.l2j.gameserver.model;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.zone.L2ZoneManager;
import scripts.zone.L2ZoneType;
import scripts.zone.type.L2DerbyTrackZone;
import scripts.zone.type.L2PeaceZone;
import scripts.zone.type.L2TownZone;

public final class L2WorldRegion
{
  private static final Logger _log;
  private ConcurrentLinkedQueue<L2Object> _visibleObjects = new ConcurrentLinkedQueue();
  private ConcurrentLinkedQueue<L2PlayableInstance> _allPlayable = new ConcurrentLinkedQueue();
  private FastList<L2WorldRegion> _surroundingRegions;
  private int _tileX;
  private int _tileY;
  private Boolean _active = Boolean.valueOf(false);
  private ScheduledFuture _neighborsTask = null;
  private final FastList<L2ZoneType> _zones;
  private L2ZoneManager _zoneManager;

  public L2WorldRegion(int pTileX, int pTileY)
  {
    _surroundingRegions = new FastList();

    _tileX = pTileX;
    _tileY = pTileY;

    if (Config.GRIDS_ALWAYS_ON)
      _active = Boolean.valueOf(true);
    else {
      _active = Boolean.valueOf(false);
    }
    _zones = new FastList();
  }

  public void addZone(L2ZoneType zone) {
    if (_zoneManager == null) {
      _zoneManager = new L2ZoneManager();
    }
    _zoneManager.registerNewZone(zone);
  }

  public void removeZone(L2ZoneType zone) {
    if (_zoneManager == null) {
      return;
    }
    _zoneManager.unregisterZone(zone);
  }

  public FastList<L2ZoneType> getZones() {
    return _zones;
  }

  public boolean checkEffectRangeInsidePeaceZone(L2Skill skill, int x, int y, int z) {
    int range = skill.getEffectRange();
    int up = y + range;
    int down = y - range;
    int left = x + range;
    int right = x - range;

    for (L2ZoneType e : getZones()) {
      if ((((e instanceof L2TownZone)) && (((L2TownZone)e).isPeaceZone())) || ((e instanceof L2DerbyTrackZone)) || ((e instanceof L2PeaceZone))) {
        if (e.isInsideZone(x, up, z)) {
          return false;
        }

        if (e.isInsideZone(x, down, z)) {
          return false;
        }

        if (e.isInsideZone(left, y, z)) {
          return false;
        }

        if (e.isInsideZone(right, y, z)) {
          return false;
        }

        if (e.isInsideZone(x, y, z)) {
          return false;
        }
      }
    }
    return true;
  }

  public void revalidateZones(L2Character character) {
    if (_zoneManager == null) {
      return;
    }

    if (_zoneManager != null)
      _zoneManager.revalidateZones(character);
  }

  public void removeFromZones(L2Character character)
  {
    if (_zoneManager == null) {
      return;
    }

    if (_zoneManager != null)
      _zoneManager.removeCharacter(character);
  }

  public void onDeath(L2Character character)
  {
    if (_zoneManager == null) {
      return;
    }

    if (_zoneManager != null)
      _zoneManager.onDeath(character);
  }

  public void onRevive(L2Character character)
  {
    if (_zoneManager == null) {
      return;
    }

    if (_zoneManager != null)
      _zoneManager.onRevive(character);
  }

  private void switchAI(Boolean isOn)
  {
    int c = 0;
    if (!isOn.booleanValue()) {
      for (L2Object o : _visibleObjects) {
        if (o.isL2Attackable()) {
          c++;
          L2Attackable mob = (L2Attackable)o;

          mob.setTarget(null);

          mob.stopMove(null);

          mob.stopAllEffects();

          mob.clearAggroList();
          mob.getKnownList().removeAllKnownObjects();

          mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

          ((L2AttackableAI)mob.getAI()).stopAITask();
        }

      }

      _log.fine(c + " mobs were turned off");
    } else {
      for (L2Object o : _visibleObjects) {
        if (o.isL2Attackable()) {
          c++;

          ((L2Attackable)o).getStatus().startHpMpRegeneration();
        }
        else if (o.isL2Npc())
        {
          ((L2NpcInstance)o).startRandomAnimationTimer();
        }
      }
      _log.fine(c + " mobs were turned on");
    }
  }

  public Boolean isActive()
  {
    return _active;
  }

  public Boolean areNeighborsEmpty()
  {
    if ((isActive().booleanValue()) && (_allPlayable.size() > 0)) {
      return Boolean.valueOf(false);
    }

    for (L2WorldRegion neighbor : _surroundingRegions) {
      if ((neighbor.isActive().booleanValue()) && (neighbor._allPlayable.size() > 0)) {
        return Boolean.valueOf(false);
      }

    }

    return Boolean.valueOf(true);
  }

  public void setActive(boolean value)
  {
    if (_active.booleanValue() == value) {
      return;
    }

    _active = Boolean.valueOf(value);

    switchAI(Boolean.valueOf(value));

    if (value)
      _log.fine("Starting Grid " + _tileX + "," + _tileY);
    else
      _log.fine("Stoping Grid " + _tileX + "," + _tileY);
  }

  private void startActivation()
  {
    setActive(true);

    if (_neighborsTask != null) {
      _neighborsTask.cancel(true);
      _neighborsTask = null;
    }

    _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
  }

  private void startDeactivation()
  {
    if (_neighborsTask != null) {
      _neighborsTask.cancel(true);
      _neighborsTask = null;
    }

    _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000 * Config.GRID_NEIGHBOR_TURNOFF_TIME);
  }

  public void addVisibleObject(L2Object object)
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (object.getWorldRegion() != this)) throw new AssertionError();

    if (object == null) {
      return;
    }

    _visibleObjects.add(object);
    if (object.isL2Playable()) {
      _allPlayable.add((L2PlayableInstance)object);

      if ((_allPlayable.size() == 1) && (!Config.GRIDS_ALWAYS_ON))
        startActivation();
    }
  }

  public void removeVisibleObject(L2Object object)
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (object.getWorldRegion() != this) && (object.getWorldRegion() != null)) throw new AssertionError();

    if (object == null) {
      return;
    }
    _visibleObjects.remove(object);

    if (object.isL2Playable()) {
      _allPlayable.remove((L2PlayableInstance)object);

      if ((_allPlayable.isEmpty()) && (!Config.GRIDS_ALWAYS_ON))
        startDeactivation();
    }
  }

  public void addSurroundingRegion(L2WorldRegion region)
  {
    _surroundingRegions.add(region);
  }

  public FastList<L2WorldRegion> getSurroundingRegions()
  {
    return _surroundingRegions;
  }

  public Iterator<L2PlayableInstance> iterateAllPlayers() {
    return _allPlayable.iterator();
  }

  public ConcurrentLinkedQueue<L2Object> getVisibleObjects() {
    return _visibleObjects;
  }

  public String getName() {
    return "(" + _tileX + ", " + _tileY + ")";
  }

  public synchronized void deleteVisibleNpcSpawns()
  {
    for (L2Object obj : _visibleObjects)
      if (obj.isL2Npc()) {
        L2NpcInstance target = (L2NpcInstance)obj;
        target.deleteMe();
        L2Spawn spawn = target.getSpawn();
        if (spawn != null) {
          spawn.stopRespawn();
          SpawnTable.getInstance().deleteSpawn(spawn, false);
        }
      }
  }

  public synchronized void respawnVisibleNpcSpawns(int id)
  {
    L2Spawn spawn = null;
    L2NpcInstance target = null;
    FastList npcs = new FastList();
    for (L2Object obj : _visibleObjects) {
      if (obj == null)
      {
        continue;
      }
      if (obj.isL2Npc()) {
        target = (L2NpcInstance)obj;
        if (target.getNpcId() == id) {
          target.decayMe();
          target.deleteMe();
          spawn = target.getSpawn();
          if (spawn != null)
            npcs.add(spawn);
        }
      }
    }
    FastList.Node n;
    if (!npcs.isEmpty())
    {
      n = npcs.head(); for (FastList.Node end = npcs.tail(); (n = n.getNext()) != end; ) {
        spawn = (L2Spawn)n.getValue();
        if (spawn == null)
        {
          continue;
        }

        target = spawn.spawnOne();
        target.setRunning();
      }
    }
    spawn = null;
    target = null;
    npcs.clear();
    npcs = null;
  }

  static
  {
    _log = AbstractLogger.getLogger(L2WorldRegion.class.getName());
  }

  public class NeighborsTask
    implements Runnable
  {
    private boolean _isActivating;

    public NeighborsTask(boolean isActivating)
    {
      _isActivating = isActivating;
    }

    public void run() {
      if (_isActivating)
      {
        for (L2WorldRegion neighbor : getSurroundingRegions())
          neighbor.setActive(true);
      }
      else {
        if (areNeighborsEmpty().booleanValue()) {
          setActive(false);
        }

        for (L2WorldRegion neighbor : getSurroundingRegions())
          if (neighbor.areNeighborsEmpty().booleanValue())
            neighbor.setActive(false);
      }
    }
  }
}