package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;

public abstract class Func
{
  public final Stats stat;
  public final int order;
  public final Object funcOwner;
  public Condition cond;

  public Func(Stats pStat, int pOrder, Object owner)
  {
    stat = pStat;
    order = pOrder;
    funcOwner = owner;
  }

  public void setCondition(Condition pCond)
  {
    cond = pCond;
  }

  public abstract void calc(Env paramEnv);
}