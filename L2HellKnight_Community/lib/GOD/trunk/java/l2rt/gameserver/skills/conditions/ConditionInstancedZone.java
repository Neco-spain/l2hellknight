package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.skills.Env;

public class ConditionInstancedZone extends Condition
{
	private final String _name;

	public ConditionInstancedZone(String name)
	{
		_name = name;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		return env.character.getReflection().getName().equals(_name);
	}
}