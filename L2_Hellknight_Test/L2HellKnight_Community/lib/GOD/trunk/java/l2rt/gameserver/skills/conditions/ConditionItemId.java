package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.skills.Env;

public final class ConditionItemId extends Condition
{
	private final short _itemId;

	public ConditionItemId(short itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(env.item == null)
			return false;
		return env.item.getItemId() == _itemId;
	}
}