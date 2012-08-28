package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionLogicNot extends Condition
{
  private Condition _condition;

  public ConditionLogicNot(Condition condition)
  {
    _condition = condition;
    if (getListener() != null)
      _condition.setListener(this);
  }

  void setListener(ConditionListener listener)
  {
    if (listener != null)
      _condition.setListener(this);
    else
      _condition.setListener(null);
    super.setListener(listener);
  }

  public boolean testImpl(Env env)
  {
    return !_condition.test(env);
  }
}