package net.sf.l2j.gameserver.model;

import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
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
import net.sf.l2j.gameserver.model.zone.L2ZoneManager;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.type.L2PeaceZone;
import net.sf.l2j.gameserver.taskmanager.KnownListUpdateTaskManager;
import net.sf.l2j.util.L2ObjectSet;

public final class L2WorldRegion
{
  private static Logger _log;
  private L2ObjectSet<L2PlayableInstance> _allPlayable;
  private L2ObjectSet<L2Object> _visibleObjects;
  private FastList<L2WorldRegion> _surroundingRegions;
  private int _tileX;
  private int _tileY;
  private Boolean _active = Boolean.valueOf(false);
  private ScheduledFuture<?> _neighborsTask = null;
  private L2ZoneManager _zoneManager;

  public L2WorldRegion(int pTileX, int pTileY)
  {
    _allPlayable = L2ObjectSet.createL2PlayerSet();
    _visibleObjects = L2ObjectSet.createL2ObjectSet();
    _surroundingRegions = new FastList();

    _tileX = pTileX;
    _tileY = pTileY;

    if (Config.GRIDS_ALWAYS_ON)
      _active = Boolean.valueOf(true);
    else
      _active = Boolean.valueOf(false);
  }

  public void addZone(L2ZoneType zone)
  {
    if (_zoneManager == null)
    {
      _zoneManager = new L2ZoneManager();
    }
    _zoneManager.registerNewZone(zone);
  }

  public void removeZone(L2ZoneType zone)
  {
    if (_zoneManager == null)
      return;
    _zoneManager.unregisterZone(zone);
  }

  public void revalidateZones(L2Character character)
  {
    if (_zoneManager == null) return;

    if (_zoneManager != null)
    {
      _zoneManager.revalidateZones(character);
    }
  }

  public void removeFromZones(L2Character character)
  {
    if (_zoneManager == null) return;

    if (_zoneManager != null)
    {
      _zoneManager.removeCharacter(character);
    }
  }

  public boolean checkEffectRangeInsidePeaceZone(L2Skill skill, int x, int y, int z)
  {
    if (_zoneManager != null)
    {
      int range = skill.getEffectRange();
      int up = y + range;
      int down = y - range;
      int left = x + range;
      int right = x - range;

      for (L2ZoneType e : _zoneManager.getZones())
      {
        if ((e instanceof L2PeaceZone))
        {
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
          if (e.isInsideZone(x, y, z))
            return false;
        }
      }
      return true;
    }
    return true;
  }

  public void onDeath(L2Character character)
  {
    if (_zoneManager == null) return;

    if (_zoneManager != null)
    {
      _zoneManager.onDeath(character);
    }
  }

  public void onRevive(L2Character character)
  {
    if (_zoneManager == null) return;

    if (_zoneManager != null)
    {
      _zoneManager.onRevive(character);
    }
  }

  private void switchAI(Boolean isOn)
  {
    int c = 0;
    if (!isOn.booleanValue())
    {
      for (L2Object o : _visibleObjects)
      {
        if ((o instanceof L2Attackable))
        {
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
    }
    else
    {
      for (L2Object o : _visibleObjects)
      {
        if ((o instanceof L2Attackable))
        {
          c++;

          ((L2Attackable)o).getStatus().startHpMpRegeneration();
        }
        else if ((o instanceof L2NpcInstance))
        {
          ((L2NpcInstance)o).startRandomAnimationTimer();
        }
      }
      KnownListUpdateTaskManager.getInstance().updateRegion(this, true, false);
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

    if (_neighborsTask != null)
    {
      _neighborsTask.cancel(true);
      _neighborsTask = null;
    }

    _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
  }

  private void startDeactivation()
  {
    if (_neighborsTask != null)
    {
      _neighborsTask.cancel(true);
      _neighborsTask = null;
    }

    _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000 * Config.GRID_NEIGHBOR_TURNOFF_TIME);
  }

  public void addVisibleObject(L2Object object)
  {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (object.getWorldRegion() != this)) throw new AssertionError();

    if (object == null) return;
    _visibleObjects.put(object);

    if ((object instanceof L2PlayableInstance))
    {
      _allPlayable.put((L2PlayableInstance)object);

      if ((_allPlayable.size() == 1) && (!Config.GRIDS_ALWAYS_ON))
        startActivation();
    }
  }

  public void removeVisibleObject(L2Object object)
  {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (object.getWorldRegion() != this) && (object.getWorldRegion() != null)) throw new AssertionError();

    if (object == null) return;
    _visibleObjects.remove(object);

    if ((object instanceof L2PlayableInstance))
    {
      _allPlayable.remove((L2PlayableInstance)object);

      if ((_allPlayable.size() == 0) && (!Config.GRIDS_ALWAYS_ON))
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

  public Iterator<L2PlayableInstance> iterateAllPlayers()
  {
    return _allPlayable.iterator();
  }

  public L2ObjectSet<L2Object> getVisibleObjects()
  {
    return _visibleObjects;
  }

  public L2ObjectSet<L2PlayableInstance> getVisiblePlayable()
  {
    return _allPlayable;
  }

  public String getName()
  {
    return "(" + _tileX + ", " + _tileY + ")";
  }

  public synchronized void deleteVisibleNpcSpawns()
  {
    _log.fine("Deleting all visible NPC's in Region: " + getName());
    for (L2Object obj : _visibleObjects)
    {
      if ((obj instanceof L2NpcInstance))
      {
        L2NpcInstance target = (L2NpcInstance)obj;
        target.deleteMe();
        L2Spawn spawn = target.getSpawn();
        if (spawn != null)
        {
          spawn.stopRespawn();
          SpawnTable.getInstance().deleteSpawn(spawn, false);
        }
        _log.finest("Removed NPC " + target.getObjectId());
      }
    }
    _log.info("All visible NPC's deleted in Region: " + getName());
  }

  static
  {
    _log = Logger.getLogger(L2WorldRegion.class.getName());
  }

  public class NeighborsTask
    implements Runnable
  {
    private boolean _isActivating;

    public NeighborsTask(boolean isActivating)
    {
      _isActivating = isActivating;
    }

    public void run()
    {
      if (_isActivating)
      {
        for (L2WorldRegion neighbor : getSurroundingRegions())
          neighbor.setActive(true);
      }
      else
      {
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