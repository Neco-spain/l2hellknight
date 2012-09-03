package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.skills.Env;

public class ConditionPlayerPercentCp extends Condition
{
	private final float _cp;

	public ConditionPlayerPercentCp(int cp)
	{
		_cp = cp / 100f;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentCpRatio() <= _cp;
	}
}