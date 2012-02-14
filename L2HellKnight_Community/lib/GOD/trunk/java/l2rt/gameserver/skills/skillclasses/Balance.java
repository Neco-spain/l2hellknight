package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class Balance extends L2Skill
{
	public Balance(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		double summaryCurrentHp = 0;
		int summaryMaximumHp = 0;

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked(false))
					continue;
				summaryCurrentHp += target.getCurrentHp();
				summaryMaximumHp += target.getMaxHp();
			}

		double percent = summaryCurrentHp / summaryMaximumHp;

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked(false))
					continue;
				double newHp = Math.max(0, Math.min(target.getMaxHp() * percent, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.));
				target.setCurrentHp(newHp, false);
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
