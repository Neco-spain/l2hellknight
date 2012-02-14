package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.templates.L2Armor.ArmorType;

public class ConditionUsingArmor extends Condition
{
	private final ArmorType _armor;

	public ConditionUsingArmor(ArmorType armor)
	{
		_armor = armor;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(env.character.isPlayer() && ((L2Player) env.character).isWearingArmor(_armor))
			return true;

		return false;
	}
}
