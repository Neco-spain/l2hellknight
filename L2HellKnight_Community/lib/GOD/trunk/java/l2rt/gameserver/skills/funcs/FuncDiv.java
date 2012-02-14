package l2rt.gameserver.skills.funcs;

import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public class FuncDiv extends Func
{
	public FuncDiv(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public void calc(Env env)
	{
		env.value /= _value;
	}
}
