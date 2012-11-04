package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Playable;
import l2r.gameserver.stats.Env;

public final class ConditionUsingItemType extends Condition
{
	private final long _mask;

	public ConditionUsingItemType(long mask)
	{
		_mask = mask;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayable())
			return false;
		return (_mask & ((Playable) env.character).getWearedMask()) != 0;
	}
}
