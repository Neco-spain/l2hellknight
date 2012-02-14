package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.skills.Env;

public class ConditionPlayerRace extends Condition
{
	private final Race _race;

	public ConditionPlayerRace(String race)
	{
		_race = Race.valueOf(race.toLowerCase());
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		return ((L2Player) env.character).getRace() == _race;
	}
}