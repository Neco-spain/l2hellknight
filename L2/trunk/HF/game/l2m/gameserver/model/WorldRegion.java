package l2m.gameserver.model;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.commons.lang.ArrayUtils;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.instances.NpcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WorldRegion
  implements Iterable<GameObject>
{
  public static final WorldRegion[] EMPTY_L2WORLDREGION_ARRAY = new WorldRegion[0];

  private static final Logger _log = LoggerFactory.getLogger(WorldRegion.class);
  private final int tileX;
  private final int tileY;
  private final int tileZ;
  private volatile GameObject[] _objects = GameObject.EMPTY_L2OBJECT_ARRAY;

  private int _objectsCount = 0;

  private volatile Zone[] _zones = Zone.EMPTY_L2ZONE_ARRAY;

  private int _playersCount = 0;

  private final AtomicBoolean _isActive = new AtomicBoolean();
  private Future<?> _activateTask;
  private final Lock lock = new ReentrantLock();

  WorldRegion(int x, int y, int z)
  {
    tileX = x;
    tileY = y;
    tileZ = z;
  }

  int getX()
  {
    return tileX;
  }

  int getY()
  {
    return tileY;
  }

  int getZ()
  {
    return tileZ;
  }

  void setActive(boolean activate)
  {
    if (!_isActive.compareAndSet(!activate, activate)) {
      return;
    }

    for (GameObject obj : this)
    {
      if (!obj.isNpc())
        continue;
      NpcInstance npc = (NpcInstance)obj;
      if (npc.getAI().isActive() != isActive())
        if (isActive())
        {
          npc.getAI().startAITask();
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
          npc.startRandomAnimation();
        }
        else if (!npc.getAI().isGlobalAI())
        {
          npc.getAI().stopAITask();
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
          npc.stopRandomAnimation();
        }
    }
  }

  void addToPlayers(GameObject object, Creature dropper)
  {
    if (object == null) {
      return;
    }
    Player player = null;
    if (object.isPlayer()) {
      player = (Player)object;
    }
    int oid = object.getObjectId();
    int rid = object.getReflectionId();

    for (GameObject obj : this)
    {
      if ((obj.getObjectId() == oid) || (obj.getReflectionId() != rid)) {
        continue;
      }
      if (player != null) {
        player.sendPacket(player.addVisibleObject(obj, null));
      }

      if (obj.isPlayer())
      {
        Player p = (Player)obj;
        p.sendPacket(p.addVisibleObject(object, dropper));
      }
    }
  }

  void removeFromPlayers(GameObject object)
  {
    if (object == null) {
      return;
    }
    Player player = null;
    if (object.isPlayer()) {
      player = (Player)object;
    }
    int oid = object.getObjectId();
    Reflection rid = object.getReflection();

    List d = null;

    for (GameObject obj : this)
    {
      if ((obj.getObjectId() == oid) || (obj.getReflection() != rid))
      {
        continue;
      }
      if (player != null) {
        player.sendPacket(player.removeVisibleObject(obj, null));
      }

      if (obj.isPlayer())
      {
        Player p = (Player)obj;
        p.sendPacket(p.removeVisibleObject(object, d == null ? (d = object.deletePacketList()) : d));
      }
    }
  }

  public void addObject(GameObject obj)
  {
    if (obj == null) {
      return;
    }
    lock.lock();
    try
    {
      GameObject[] objects = _objects;

      GameObject[] resizedObjects = new GameObject[_objectsCount + 1];
      System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);
      objects = resizedObjects;
      objects[(_objectsCount++)] = obj;

      _objects = resizedObjects;

      if ((obj.isPlayer()) && 
        (_playersCount++ == 0))
      {
        if (_activateTask != null) {
          _activateTask.cancel(false);
        }
        _activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(true), 1000L);
      }
    }
    finally
    {
      lock.unlock();
    }
  }

  public void removeObject(GameObject obj)
  {
    if (obj == null) {
      return;
    }
    lock.lock();
    try
    {
      GameObject[] objects = _objects;

      int index = -1;

      for (int i = 0; i < _objectsCount; i++)
      {
        if (objects[i] != obj)
          continue;
        index = i;
        break;
      }

      if (index == -1)
        return;
      _objectsCount -= 1;

      GameObject[] resizedObjects = new GameObject[_objectsCount];
      objects[index] = objects[_objectsCount];
      System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);

      _objects = resizedObjects;

      if ((obj.isPlayer()) && 
        (--_playersCount == 0))
      {
        if (_activateTask != null) {
          _activateTask.cancel(false);
        }
        _activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(false), 60000L);
      }
    }
    finally
    {
      lock.unlock();
    }
  }

  public int getObjectsSize()
  {
    return _objectsCount;
  }

  public int getPlayersCount()
  {
    return _playersCount;
  }

  public boolean isEmpty()
  {
    return _playersCount == 0;
  }

  public boolean isActive()
  {
    return _isActive.get();
  }

  void addZone(Zone zone)
  {
    lock.lock();
    try
    {
      _zones = ((Zone[])ArrayUtils.add(_zones, zone));
    }
    finally
    {
      lock.unlock();
    }
  }

  void removeZone(Zone zone)
  {
    lock.lock();
    try
    {
      _zones = ((Zone[])ArrayUtils.remove(_zones, zone));
    }
    finally
    {
      lock.unlock();
    }
  }

  Zone[] getZones()
  {
    return _zones;
  }

  public String toString()
  {
    return "[" + tileX + ", " + tileY + ", " + tileZ + "]";
  }

  public Iterator<GameObject> iterator()
  {
    return new InternalIterator(_objects);
  }

  private class InternalIterator
    implements Iterator<GameObject>
  {
    final GameObject[] objects;
    int cursor = 0;

    public InternalIterator(GameObject[] objects)
    {
      this.objects = objects;
    }

    public boolean hasNext()
    {
      if (cursor < objects.length)
        return objects[cursor] != null;
      return false;
    }

    public GameObject next()
    {
      return objects[(cursor++)];
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

  public class ActivateTask extends RunnableImpl
  {
    private boolean _isActivating;

    public ActivateTask(boolean isActivating)
    {
      _isActivating = isActivating;
    }

    public void runImpl()
      throws Exception
    {
      if (_isActivating)
        World.activate(WorldRegion.this);
      else
        World.deactivate(WorldRegion.this);
    }
  }
}