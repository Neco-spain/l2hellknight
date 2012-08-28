package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.skills.Env;

public final class ConditionItemId extends Condition
{
  private final int _itemId;

  public ConditionItemId(int itemId)
  {
    _itemId = itemId;
  }

  public boolean testImpl(Env env)
  {
    if (env.item == null)
      return false;
    return env.item.getItemId() == _itemId;
  }
}