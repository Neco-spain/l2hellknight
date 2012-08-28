package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public class NpcStatus extends CharStatus
{
  public NpcStatus(L2NpcInstance activeChar)
  {
    super(activeChar);
  }

  public final void reduceHp(double value, L2Character attacker)
  {
    reduceHp(value, attacker, true);
  }

  public final void reduceHp(double value, L2Character attacker, boolean awake)
  {
    if (getActiveChar().isDead()) return;

    if (attacker != null) getActiveChar().addAttackerToAttackByList(attacker);

    super.reduceHp(value, attacker, awake);
  }

  public L2NpcInstance getActiveChar()
  {
    return (L2NpcInstance)super.getActiveChar();
  }
}