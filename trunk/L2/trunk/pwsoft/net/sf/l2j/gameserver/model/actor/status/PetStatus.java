package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class PetStatus extends SummonStatus
{
  private int _currentFed = 0;

  public PetStatus(L2PetInstance activeChar)
  {
    super(activeChar);
  }

  public final void reduceHp(double value, L2Character attacker)
  {
    reduceHp(value, attacker, true);
  }

  public final void reduceHp(double value, L2Character attacker, boolean awake) {
    if (getActiveChar().isDead()) return;

    super.reduceHp(value, attacker, awake);

    if (attacker != null)
    {
      SystemMessage sm = SystemMessage.id(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1);
      if (attacker.isL2Npc())
        sm.addNpcName(((L2NpcInstance)attacker).getTemplate().idTemplate);
      else
        sm.addString(attacker.getName());
      sm.addNumber((int)value);
      getActiveChar().getOwner().sendPacket(sm);
      sm = null;

      getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
      if (getActiveChar().getTarget() == null)
      {
        int posX = getActiveChar().getOwner().getX();
        int posY = getActiveChar().getOwner().getY();
        int posZ = getActiveChar().getOwner().getZ();

        int side = Rnd.get(1, 6);

        switch (side)
        {
        case 1:
          posX += 43;
          posY += 70;
          break;
        case 2:
          posX += 10;
          posY += 80;
          break;
        case 3:
          posX += 60;
          posY += 30;
          break;
        case 4:
          posX += 40;
          posY -= 40;
          break;
        case 5:
          posX -= 40;
          posY -= 60;
          break;
        case 6:
          posX -= 50;
          posY += 10;
        }

        getActiveChar().setRunning();
        getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
      }
    }
  }

  public L2PetInstance getActiveChar()
  {
    return (L2PetInstance)super.getActiveChar();
  }
  public int getCurrentFed() { return _currentFed; } 
  public void setCurrentFed(int value) { _currentFed = value;
  }
}