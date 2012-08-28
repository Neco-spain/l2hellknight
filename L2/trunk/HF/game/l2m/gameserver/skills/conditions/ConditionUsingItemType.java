package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Playable;
import l2m.gameserver.skills.Env;

public final class ConditionUsingItemType extends Condition
{
  private final long _mask;

  public ConditionUsingItemType(long mask)
  {
    _mask = mask;
  }

  protected boolean testImpl(Env env)
  {
    if (!env.character.isPlayable())
      return false;
    return (_mask & ((Playable)env.character).getWearedMask()) != 0L;
  }
}