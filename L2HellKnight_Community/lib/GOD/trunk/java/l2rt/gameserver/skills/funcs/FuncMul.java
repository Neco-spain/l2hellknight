package l2rt.gameserver.skills.funcs;

import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public class FuncMul extends Func
{
	public FuncMul(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public void calc(Env env)
	{
		env.value *= _value;
	}
}
