package l2rt.gameserver.skills.skillclasses;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class DeathPenalty extends L2Skill
{
	public DeathPenalty(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(final L2Character activeChar, final L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		// Chaotic characters can't use scrolls of recovery
		if(activeChar.getKarma() < 0 && !ConfigSystem.getBoolean("ChaoticCanUseScrollOfRecovery"))
		{
			activeChar.sendActionFailed();
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(!target.isPlayer())
					continue;
				((L2Player) target).getDeathPenalty().reduceLevel();
			}
	}
}