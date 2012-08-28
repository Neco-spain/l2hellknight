//L2DDT
package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;



public class FuncBaseMul extends Func {
	private final Lambda _lambda;
	public FuncBaseMul(Stats pStat, int pOrder, Object owner, Lambda lambda) {
		super(pStat, pOrder, owner);
		_lambda = lambda;
	}
	@Override
	public void calc(Env env)
	{
		if (cond == null || cond.test(env))
			env.value += env.baseValue * _lambda.calc(env);
	}
}
