package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Object;

public class AttackableKnownList extends NpcKnownList
{
  public AttackableKnownList(L2Attackable activeChar)
  {
    super(activeChar);
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) {
      return false;
    }

    if ((object != null) && (object.isL2Character())) {
      getActiveChar().getAggroList().remove(object);
    }
    Collection known = getKnownPlayers().values();

    L2CharacterAI ai = getActiveChar().getAI();
    if ((ai != null) && ((known == null) || (known.isEmpty())))
    {
      ai.setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }

    return true;
  }

  public L2Attackable getActiveChar()
  {
    return (L2Attackable)super.getActiveChar();
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    if ((getActiveChar().getAggroList() != null) && 
      (getActiveChar().getAggroList().get(object) != null)) return 3000;
    return Math.min(2200, 2 * getDistanceToWatchObject(object));
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    if ((object.isL2Folk()) || (!object.isL2Character())) {
      return 0;
    }
    if (object.isL2Playable()) {
      return 1500;
    }
    if (getActiveChar().getAggroRange() > getActiveChar().getFactionRange()) {
      return getActiveChar().getAggroRange();
    }
    if (getActiveChar().getFactionRange() > 200) {
      return getActiveChar().getFactionRange();
    }
    return 200;
  }
}