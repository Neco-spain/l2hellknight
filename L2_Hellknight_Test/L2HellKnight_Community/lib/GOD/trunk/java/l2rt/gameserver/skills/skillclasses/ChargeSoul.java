package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class ChargeSoul extends L2Skill
{
	private int _numSouls;

	public ChargeSoul(StatsSet set)
	{
		super(set);
		_numSouls = set.getInteger("numSouls", getLevel());
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		if(!activeChar.isPlayer())
			return;

		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss && getTargetType() != SkillTargetType.TARGET_SELF)
			activeChar.unChargeShots(false);

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				if(target != activeChar && target.checkReflectSkill(activeChar, this))
					target = activeChar;

				if(getPower() > 0) // Если == 0 значит скилл "отключен"
				{
					double damage = Formulas.calcPhysDam(activeChar, target, this, false, false, ss, false).damage;
					target.reduceCurrentHp(damage, activeChar, this, true, true, false, true);
				}

				if(target.isPlayable() || target.isMonster())
					activeChar.setConsumedSouls(activeChar.getConsumedSouls() + _numSouls, null);

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}