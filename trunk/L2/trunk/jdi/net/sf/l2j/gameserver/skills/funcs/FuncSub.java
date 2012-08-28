package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;

public class FuncSub extends Func
{
  private final Lambda _lambda;

  public FuncSub(Stats pStat, int pOrder, Object owner, Lambda lambda)
  {
    super(pStat, pOrder, owner);
    _lambda = lambda;
  }

  public void calc(Env env)
  {
    if ((cond == null) || (cond.test(env)))
      env.value -= _lambda.calc(env);
  }
}