package l2m.gameserver.skills.funcs;

import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;

public class FuncSet extends Func
{
  public FuncSet(Stats stat, int order, Object owner, double value)
  {
    super(stat, order, owner, value);
  }

  public void calc(Env env)
  {
    env.value = value;
  }
}