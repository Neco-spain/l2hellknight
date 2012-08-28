package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2CommanderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;

public class CommanderKnownList extends AttackableKnownList
{
  public CommanderKnownList(L2CommanderInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper)
  {
    if (!super.addKnownObject(object, dropper)) {
      return false;
    }
    if (getActiveChar().getHomeX() == 0)
    {
      getActiveChar().getHomeLocation();
    }

    if ((getActiveChar().getFort() != null) && (getActiveChar().getFort().getSiege().getIsInProgress()))
    {
      L2PcInstance player = null;

      if ((object instanceof L2PcInstance))
      {
        player = (L2PcInstance)object;
      }
      else if ((object instanceof L2Summon))
      {
        player = ((L2Summon)object).getOwner();
      }

      if ((player != null) && ((player.getClan() == null) || (getActiveChar().getFort().getSiege().getAttackerClan(player.getClan()) != null)))
      {
        if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
        {
          getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
        }
      }

      player = null;
    }

    return true;
  }

  public final L2CommanderInstance getActiveChar()
  {
    return (L2CommanderInstance)super.getActiveChar();
  }
}