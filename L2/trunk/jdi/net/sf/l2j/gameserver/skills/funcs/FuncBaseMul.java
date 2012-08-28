package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;

public class FuncBaseMul extends Func
{
  private final Lambda _lambda;

  public FuncBaseMul(Stats pStat, int pOrder, Object owner, Lambda lambda)
  {
    super(pStat, pOrder, owner);
    _lambda = lambda;
  }

  public void calc(Env env)
  {
    if ((cond == null) || (cond.test(env)))
      env.value += env.baseValue * _lambda.calc(env);
  }
}