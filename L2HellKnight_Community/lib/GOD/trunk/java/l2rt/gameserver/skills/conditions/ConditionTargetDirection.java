package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.model.L2Character.TargetDirection;
import l2rt.gameserver.skills.Env;

public class ConditionTargetDirection extends Condition
{
	private final TargetDirection _dir;

	public ConditionTargetDirection(TargetDirection direction)
	{
		_dir = direction;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getDirectionTo(env.target, true).equals(_dir);
	}
}
