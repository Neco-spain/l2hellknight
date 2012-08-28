package l2p.gameserver.stats.funcs;

import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;

public class FuncMul extends Func
{
  public FuncMul(Stats stat, int order, Object owner, double value)
  {
    super(stat, order, owner, value);
  }

  public void calc(Env env)
  {
    env.value *= value;
  }
}