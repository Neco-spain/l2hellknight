package l2m.gameserver.skills.conditions;

import l2m.gameserver.skills.Env;

public class ConditionLogicAnd extends Condition
{
  private static final Condition[] emptyConditions = new Condition[0];

  public Condition[] _conditions = emptyConditions;

  public void add(Condition condition)
  {
    if (condition == null) {
      return;
    }
    int len = _conditions.length;
    Condition[] tmp = new Condition[len + 1];
    System.arraycopy(_conditions, 0, tmp, 0, len);
    tmp[len] = condition;
    _conditions = tmp;
  }

  protected boolean testImpl(Env env)
  {
    for (Condition c : _conditions)
      if (!c.test(env))
        return false;
    return true;
  }
}