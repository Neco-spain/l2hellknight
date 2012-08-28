package l2m.gameserver.skills.conditions;

import l2m.gameserver.skills.Env;

public class ConditionFirstEffectSuccess extends Condition
{
  boolean _param;

  public ConditionFirstEffectSuccess(boolean param)
  {
    _param = param;
  }

  protected boolean testImpl(Env env)
  {
    return _param == (env.value == 2147483647.0D);
  }
}