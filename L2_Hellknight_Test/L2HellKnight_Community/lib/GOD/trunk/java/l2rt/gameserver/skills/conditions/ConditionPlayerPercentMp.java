package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.skills.Env;

public class ConditionPlayerPercentMp extends Condition
{
	private final float _mp;

	public ConditionPlayerPercentMp(int mp)
	{
		_mp = mp / 100f;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentMpRatio() <= _mp;
	}
}