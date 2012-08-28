package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public abstract class ConditionInventory extends Condition
  implements ConditionListener
{
  protected final int _slot;

  public ConditionInventory(int slot)
  {
    _slot = slot;
  }

  public abstract boolean testImpl(Env paramEnv);
}