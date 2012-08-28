package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

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
      SystemMessage sm = new SystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1);
      if ((attacker instanceof L2NpcInstance))
        sm.addNpcName(((L2NpcInstance)attacker).getTemplate().idTemplate);
      else
        sm.addString(attacker.getName());
      sm.addNumber((int)value);
      getActiveChar().getOwner().sendPacket(sm);

      getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
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