package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;

public class SiegeGuardKnownList extends AttackableKnownList
{
  public SiegeGuardKnownList(L2SiegeGuardInstance activeChar)
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

    if ((getActiveChar().getCastle() != null) && (getActiveChar().getCastle().getSiege().getIsInProgress()))
    {
      L2PcInstance player = object.getPlayer();

      if ((player != null) && ((player.getClan() == null) || (getActiveChar().getCastle().getSiege().getAttackerClan(player.getClan()) != null)))
      {
        if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
          getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
        }
      }
    }

    return true;
  }

  public final L2SiegeGuardInstance getActiveChar()
  {
    return (L2SiegeGuardInstance)super.getActiveChar();
  }
}