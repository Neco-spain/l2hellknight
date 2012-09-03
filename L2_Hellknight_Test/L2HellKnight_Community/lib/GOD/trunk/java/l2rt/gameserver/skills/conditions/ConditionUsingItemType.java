package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.skills.Env;

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
		if(!env.character.isPlayer())
			return false;
		Inventory inv = ((L2Player) env.character).getInventory();
		return (_mask & inv.getWearedMask()) != 0;
	}
}
