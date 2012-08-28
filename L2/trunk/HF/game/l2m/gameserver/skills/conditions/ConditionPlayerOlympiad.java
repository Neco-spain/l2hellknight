package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionPlayerOlympiad extends Condition
{
  private final boolean _value;

  public ConditionPlayerOlympiad(boolean v)
  {
    _value = v;
  }

  protected boolean testImpl(Env env)
  {
    return env.character.isInOlympiadMode() == _value;
  }
}