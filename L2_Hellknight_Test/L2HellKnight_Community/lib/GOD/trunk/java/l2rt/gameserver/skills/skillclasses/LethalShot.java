package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class LethalShot extends L2Skill
{
	public LethalShot(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss)
			activeChar.unChargeShots(false);

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				if(target.checkReflectSkill(activeChar, this))
					target = activeChar;

				if(getPower() > 0) // Если == 0 значит скилл "отключен"
				{
					double damage = Formulas.calcPhysDam(activeChar, target, this, false, false, ss, false).damage;
					target.reduceCurrentHp(damage, activeChar, this, true, true, false, true);
				}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}
	}
}
