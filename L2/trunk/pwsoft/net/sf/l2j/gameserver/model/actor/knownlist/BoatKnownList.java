package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;

public class BoatKnownList extends CharKnownList
{
  public BoatKnownList(L2Character activeChar)
  {
    super(activeChar);
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    if (!object.isPlayer())
      return 0;
    return 8000;
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    if (!object.isPlayer())
      return 0;
    return 4000;
  }
}