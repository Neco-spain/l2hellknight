package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class AIeffects extends L2Skill
{
	public AIeffects(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
				getEffects(activeChar, target, getActivateRate() > 0, false);

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
