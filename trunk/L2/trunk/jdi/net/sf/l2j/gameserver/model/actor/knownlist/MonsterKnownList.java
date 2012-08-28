package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Map;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class MonsterKnownList extends AttackableKnownList
{
  public MonsterKnownList(L2MonsterInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper) {
    if (!super.addKnownObject(object, dropper)) return false;

    if (((object instanceof L2PcInstance)) && (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
      getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
    return true;
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) return false;

    if (!(object instanceof L2Character)) return true;

    if (getActiveChar().hasAI())
    {
      getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
    }

    if ((getActiveChar().isVisible()) && (getKnownPlayers().isEmpty()))
    {
      getActiveChar().clearAggroList();
    }

    return true;
  }

  public final L2MonsterInstance getActiveChar()
  {
    return (L2MonsterInstance)super.getActiveChar();
  }
}