package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.log.AbstractLogger;

public class GuardKnownList extends AttackableKnownList
{
  private static Logger _log = AbstractLogger.getLogger(GuardKnownList.class.getName());

  public GuardKnownList(L2GuardInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper) {
    if (!super.addKnownObject(object, dropper)) return false;

    if (getActiveChar().getHomeX() == 0) getActiveChar().getHomeLocation();

    if (object.isPlayer())
    {
      if (object.getPlayer().getKarma() > 0)
      {
        if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
          getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
      }
    }
    else if ((Config.ALLOW_GUARDS) && (object.isL2Monster()))
    {
      L2MonsterInstance mob = (L2MonsterInstance)object;

      if (mob.isAggressive())
      {
        if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
          getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
        }
      }
    }
    return true;
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) return false;

    if (getActiveChar().noTarget())
    {
      removeAllKnownObjects();

      L2CharacterAI ai = getActiveChar().getAI();
      if (ai != null) ai.setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
    }

    return true;
  }

  public final L2GuardInstance getActiveChar()
  {
    return (L2GuardInstance)super.getActiveChar();
  }
}