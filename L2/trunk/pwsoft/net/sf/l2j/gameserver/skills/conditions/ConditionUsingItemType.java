package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

public final class ConditionUsingItemType extends Condition
{
  private final int _mask;

  public ConditionUsingItemType(int mask)
  {
    _mask = mask;
  }

  public boolean testImpl(Env env)
  {
    if (!env.cha.isPlayer()) {
      return false;
    }
    return (_mask & env.cha.getPlayer().getInventory().getWearedMask()) != 0;
  }
}