package l2m.gameserver.skills.conditions;

import l2m.gameserver.skills.Env;

public class ConditionLogicNot extends Condition
{
  private final Condition _condition;

  public ConditionLogicNot(Condition condition)
  {
    _condition = condition;
  }

  protected boolean testImpl(Env env)
  {
    return !_condition.test(env);
  }
}