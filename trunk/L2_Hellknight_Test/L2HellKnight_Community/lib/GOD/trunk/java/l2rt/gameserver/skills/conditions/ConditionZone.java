package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.skills.Env;

public class ConditionZone extends Condition
{
	private final ZoneType _zoneType;

	public ConditionZone(String zoneType)
	{
		_zoneType = ZoneType.valueOf(zoneType);
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		return env.character.isInZone(_zoneType);
	}
}