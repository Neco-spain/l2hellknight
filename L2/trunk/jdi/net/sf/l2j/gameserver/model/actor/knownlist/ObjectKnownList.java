package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Map;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.util.Util;

public class ObjectKnownList
{
  private L2Object _activeObject;
  private Map<Integer, L2Object> _knownObjects;

  public ObjectKnownList(L2Object activeObject)
  {
    _activeObject = activeObject;
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }
  public boolean addKnownObject(L2Object object, L2Character dropper) {
    if (object == null) return false;

    if (knowsObject(object)) {
      return false;
    }

    if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true)) return false;

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

  public final void findObjects()
  {
    L2WorldRegion region = getActiveObject().getWorldRegion();
    if (region == null) return;

    if ((getActiveObject() instanceof L2PlayableInstance))
    {
      for (L2WorldRegion regi : region.getSurroundingRegions())
      {
        for (L2Object _object : regi.getVisibleObjects())
        {
          if (_object != getActiveObject())
          {
            addKnownObject(_object);
            if ((_object instanceof L2Character)) _object.getKnownList().addKnownObject(getActiveObject());
          }
        }
      }
    }
    else if ((getActiveObject() instanceof L2Character))
    {
      for (L2WorldRegion regi : region.getSurroundingRegions())
      {
        if (regi.isActive().booleanValue()) for (L2Object _object : regi.getVisiblePlayable())
          {
            if (_object != getActiveObject())
            {
              addKnownObject(_object);
            }
          }
      }
    }
  }

  public final void forgetObjects(boolean fullCheck)
  {
    for (L2Object object : getKnownObjects().values())
    {
      if ((!fullCheck) && (!(object instanceof L2PlayableInstance)))
      {
        continue;
      }

      if ((!object.isVisible()) || (!Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true)))
      {
        if (((object instanceof L2BoatInstance)) && ((getActiveObject() instanceof L2PcInstance)))
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
    if (_knownObjects == null) _knownObjects = new FastMap().setShared(true);
    return _knownObjects;
  }
}