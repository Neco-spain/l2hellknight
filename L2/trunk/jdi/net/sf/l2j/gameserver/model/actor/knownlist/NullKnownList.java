package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;

public class NullKnownList extends ObjectKnownList
{
  public NullKnownList(L2Object activeObject)
  {
    super(activeObject);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper)
  {
    return false;
  }

  public boolean addKnownObject(L2Object object)
  {
    return false;
  }

  public L2Object getActiveObject()
  {
    return super.getActiveObject();
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    return 0;
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    return 0;
  }

  public void removeAllKnownObjects()
  {
  }

  public boolean removeKnownObject(L2Object object)
  {
    return false;
  }
}