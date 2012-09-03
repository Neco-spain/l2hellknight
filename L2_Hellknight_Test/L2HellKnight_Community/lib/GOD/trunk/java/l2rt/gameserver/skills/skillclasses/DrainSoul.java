package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class DrainSoul extends L2Skill
{
	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!target.isMonster())
		{
			activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	public DrainSoul(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		if(!activeChar.isPlayer())
			return;

		// This is just a dummy skill for the soul crystal skill condition,
		// since the Soul Crystal item handler already does everything.
	}
}
