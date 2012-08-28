package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;

public class PlayableStatus extends CharStatus
{
  public PlayableStatus(L2PlayableInstance activeChar)
  {
    super(activeChar);
  }

  public void reduceHp(double value, L2Character attacker)
  {
    reduceHp(value, attacker, true);
  }

  public void reduceHp(double value, L2Character attacker, boolean awake) {
    if (getActiveChar().isDead()) return;

    super.reduceHp(value, attacker, awake);
  }

  public L2PlayableInstance getActiveChar()
  {
    return (L2PlayableInstance)super.getActiveChar();
  }
}