package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.Inventory;
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
    if (!(env.player instanceof L2PcInstance))
      return false;
    Inventory inv = ((L2PcInstance)env.player).getInventory();
    return (_mask & inv.getWearedMask()) != 0;
  }
}