package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class MDam extends L2Skill
{
	public MDam(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		int sps = isSSPossible() ? (isMagic() ? activeChar.getChargedSpiritShot() : (activeChar.getChargedSoulShot() ? 2 : 0)) : 0;

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				if(getPower() > 0) // Если == 0 значит скилл "отключен"
					if(target.checkReflectSkill(activeChar, this))
						target = activeChar;

				double damage = Formulas.calcMagicDam(activeChar, target, this, sps);

				if(damage >= 1)
					target.reduceCurrentHp(damage, activeChar, this, true, true, false, true);

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSuicideAttack())
		{
			activeChar.doDie(null);
			activeChar.onDecay();
		}
		else if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}