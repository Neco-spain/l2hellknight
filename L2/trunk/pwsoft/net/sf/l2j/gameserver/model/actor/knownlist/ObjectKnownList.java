package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.util.Util;

public class ObjectKnownList
{
  private L2Object _activeObject;
  private Map<Integer, L2Object> _knownObjects = new ConcurrentHashMap();

  public ObjectKnownList(L2Object activeObject)
  {
    _activeObject = activeObject;
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }
  public boolean addKnownObject(L2Object object, L2Character dropper) {
    if ((object == null) || (object == getActiveObject())) {
      return false;
    }

    if (knowsObject(object))
    {
      if (!object.isVisible())
        removeKnownObject(object);
      return false;
    }

    if (!Util.checkIfInRange(getDistanceToWatchObject(object), getActiveObject(), object, true)) {
      return false;
    }
    return getKnownObjects().put(Integer.valueOf(object.getObjectId()), object) == null;
  }
  public final boolean knowsObject(L2Object object) {
    return (getActiveObject() == object) || (getKnownObjects().containsKey(Integer.valueOf(object.getObjectId())));
  }
  public void removeAllKnownObjects() {
    getKnownObjects().clear();
  }

  public boolean removeKnownObject(L2Object object) {
    if (object == null) return false;
    return getKnownObjects().remove(Integer.valueOf(object.getObjectId())) != null;
  }

  public final synchronized void updateKnownObjects()
  {
    if (getActiveObject().isL2Character())
    {
      findCloseObjects();
      forgetObjects();
    }
  }

  private final void findCloseObjects()
  {
    boolean isActiveObjectPlayable = getActiveObject() instanceof L2PlayableInstance;

    if (isActiveObjectPlayable)
    {
      Collection objects = L2World.getInstance().getVisibleObjects(getActiveObject());
      if (objects == null) return;

      for (L2Object object : objects)
      {
        if (object == null)
        {
          continue;
        }
        addKnownObject(object);

        if (object.isL2Character())
          object.getKnownList().addKnownObject(getActiveObject());
      }
    }
    else
    {
      Collection playables = L2World.getInstance().getVisiblePlayable(getActiveObject());
      if (playables == null) return;

      for (L2Object playable : playables)
      {
        if (playable == null)
        {
          continue;
        }

        addKnownObject(playable);
      }
    }
  }

  private final void forgetObjects()
  {
    Collection knownObjects = getKnownObjects().values();

    if ((knownObjects == null) || (knownObjects.size() == 0)) return;

    for (L2Object object : knownObjects)
    {
      if (object == null)
      {
        continue;
      }
      if ((!object.isVisible()) || (!Util.checkIfInRange(getDistanceToForgetObject(object), getActiveObject(), object, true)))
      {
        if (((object instanceof L2BoatInstance)) && (getActiveObject().isPlayer()))
        {
          if (((L2BoatInstance)(L2BoatInstance)object).getVehicleDeparture() != null)
          {
            if (((L2PcInstance)getActiveObject()).isInBoat())
            {
              if (((L2PcInstance)getActiveObject()).getBoat() != object)
              {
                removeKnownObject(object);
              }
            }
            else
            {
              removeKnownObject(object);
            }
          }
        }
        else
          removeKnownObject(object);
      }
    }
  }

  public L2Object getActiveObject()
  {
    return _activeObject;
  }
  public int getDistanceToForgetObject(L2Object object) {
    return 0;
  }
  public int getDistanceToWatchObject(L2Object object) { return 0;
  }

  public final Map<Integer, L2Object> getKnownObjects()
  {
    return _knownObjects;
  }

  public final FastList<L2DoorInstance> getKnownDoors()
  {
    FastList result = new FastList();
    for (L2Object obj : getKnownObjects().values())
    {
      if (!obj.isL2Door()) {
        continue;
      }
      result.add((L2DoorInstance)obj);
    }
    return result;
  }

  public final FastList<L2DoorInstance> getKnownDoorsInRadius(int radius)
  {
    FastList result = new FastList();
    for (L2Object obj : getKnownObjects().values())
    {
      if (!obj.isL2Door()) {
        continue;
      }
      if (Util.checkIfInRange(radius, getActiveObject(), obj, true))
        result.add((L2DoorInstance)obj);
    }
    return result;
  }

  public static class KnownListAsynchronousUpdateTask
    implements Runnable
  {
    private L2Object _obj;

    public KnownListAsynchronousUpdateTask(L2Object obj)
    {
      _obj = obj;
    }

    public void run()
    {
      if (_obj != null)
        _obj.getKnownList().updateKnownObjects();
    }
  }
}