package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;

public class DoorKnownList extends CharKnownList
{
  public DoorKnownList(L2DoorInstance activeChar)
  {
    super(activeChar);
  }

  public final L2DoorInstance getActiveChar()
  {
    return (L2DoorInstance)super.getActiveChar();
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    if (object.isL2SiegeGuard()) return 800;
    if (!object.isPlayer()) {
      return 0;
    }
    return 4000;
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    if (object.isL2SiegeGuard()) return 600;
    if (!object.isPlayer())
      return 0;
    return 2000;
  }
}