package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.skills.Env;
import l2rt.util.Rnd;

public class ConditionGameChance extends Condition
{
	private final int _chance;

	ConditionGameChance(int chance)
	{
		_chance = chance;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return Rnd.chance(_chance);
	}
}
