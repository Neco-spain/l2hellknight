package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class Drain extends L2Skill
{
	private float _absorbAbs;

	public Drain(StatsSet set)
	{
		super(set);
		_absorbAbs = set.getFloat("absorbAbs", 0.f);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		boolean ss = isSSPossible() && activeChar.getChargedSoulShot();

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.checkReflectSkill(activeChar, this))
					target = activeChar;

				if(getPower() > 0 || _absorbAbs > 0) // Если == 0 значит скилл "отключен"
				{
					if(target.isDead() && _targetType != SkillTargetType.TARGET_CORPSE)
						continue;

					double hp = 0.;
					double targetHp = target.getCurrentHp();

					if(_targetType != SkillTargetType.TARGET_CORPSE)
					{
						if(target.checkReflectSkill(activeChar, this))
							target = activeChar;

						double damage = isMagic() ? Formulas.calcMagicDam(activeChar, target, this, sps) : Formulas.calcPhysDam(activeChar, target, this, false, false, ss, false).damage;
						double targetCP = target.getCurrentCp();

						// Нельзя восстанавливать HP из CP
						if(damage > targetCP || !target.isPlayer())
							hp = (damage - targetCP) * _absorbPart;

						target.reduceCurrentHp(damage, activeChar, this, true, true, false, true);
					}

					if(_absorbAbs == 0 && _absorbPart == 0)
						continue;

					hp += _absorbAbs;

					// Нельзя восстановить больше hp, чем есть у цели.
					if(hp > targetHp && _targetType != SkillTargetType.TARGET_CORPSE)
						hp = targetHp;

					double addToHp = Math.max(0, Math.min(hp, activeChar.calcStat(Stats.HP_LIMIT, null, null) * activeChar.getMaxHp() / 100. - activeChar.getCurrentHp()));

					if(addToHp > 0 && !target.isDoor() && !activeChar.isHealBlocked(true))
						activeChar.setCurrentHp(activeChar.getCurrentHp() + addToHp, false);

					if(target.isDead() && _targetType == SkillTargetType.TARGET_CORPSE && target.isNpc())
					{
						activeChar.getAI().setAttackTarget(null);
						((L2NpcInstance) target).endDecayTask();
					}
				}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isMagic() ? sps != 0 : ss)
			activeChar.unChargeShots(isMagic());
	}
}