package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.skills.Env;

public class ConditionTargetClan extends Condition
{
	private final boolean _test;

	public ConditionTargetClan(String param)
	{
		_test = Boolean.valueOf(param);
	}

	@Override
	protected boolean testImpl(Env env)
	{
		L2Character Char = env.character;
		L2Character target = env.target;
		return Char.getPlayer() != null && target.getPlayer() != null && (Char.getPlayer().getClanId() != 0 && Char.getPlayer().getClanId() == target.getPlayer().getClanId() == _test || Char.getPlayer().getParty() != null && Char.getPlayer().getParty() == target.getPlayer().getParty());
	}
}