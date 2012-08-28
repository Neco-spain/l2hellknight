package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2CabaleBufferInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalGuideInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public class NpcKnownList extends CharKnownList
{
  public NpcKnownList(L2NpcInstance activeChar)
  {
    super(activeChar);
  }

  public L2NpcInstance getActiveChar()
  {
    return (L2NpcInstance)super.getActiveChar();
  }
  public int getDistanceToForgetObject(L2Object object) {
    return 2 * getDistanceToWatchObject(object);
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    if ((object instanceof L2FestivalGuideInstance)) {
      return 10000;
    }
    if ((object.isL2Folk()) || (!object.isL2Character())) {
      return 0;
    }
    if ((object instanceof L2CabaleBufferInstance)) {
      return 900;
    }
    if (object.isL2Playable()) {
      return 1500;
    }
    return 500;
  }
}