package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.skills.Env;

public class ConditionPlayerClassId extends Condition
{
	private final int _class;

	public ConditionPlayerClassId(int id)
	{
		_class = id;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		return ((L2Player) env.character).getActiveClassId() == _class;
	}
}