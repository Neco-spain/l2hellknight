package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.skills.Env;

public class ConditionPlayerMinHp extends Condition
{
	private final float _hp;

	public ConditionPlayerMinHp(int hp)
	{
		_hp = hp;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentHp() > _hp;
	}
}