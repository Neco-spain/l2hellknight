package l2m.gameserver.skills.conditions;

import l2m.gameserver.skills.Env;

public abstract class ConditionInventory extends Condition
{
  protected final int _slot;

  public ConditionInventory(int slot)
  {
    _slot = slot;
  }

  protected abstract boolean testImpl(Env paramEnv);
}