package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.templates.L2Item;

public class FuncEnchantAdd extends Func
{
  private final Lambda _lambda;

  public FuncEnchantAdd(Stats pStat, int pOrder, Object owner, Lambda lambda)
  {
    super(pStat, pOrder, owner);
    _lambda = lambda;
  }

  public void calc(Env env)
  {
    if ((cond != null) && (!cond.test(env)))
      return;
    L2ItemInstance item = (L2ItemInstance)funcOwner;

    if (item.getItem().getCrystalType() == 0) {
      return;
    }
    env.value += _lambda.calc(env) * item.getEnchantLevel();
  }
}