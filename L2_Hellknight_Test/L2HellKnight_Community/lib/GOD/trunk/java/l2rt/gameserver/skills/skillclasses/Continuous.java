package l2rt.gameserver.skills.skillclasses;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class Continuous extends L2Skill
{
	private final int _lethal1;
	private final int _lethal2;

	public Continuous(StatsSet set)
	{
		super(set);
		_lethal1 = set.getInteger("lethal1", 0);
		_lethal2 = set.getInteger("lethal2", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				// Player holding a cursed weapon can't be buffed and can't buff
				if(getSkillType() == L2Skill.SkillType.BUFF && target != activeChar)
					if(target.isCursedWeaponEquipped() || activeChar.isCursedWeaponEquipped())
						continue;

				if(target.checkReflectSkill(activeChar, this))
					target = activeChar;

				double mult = 0.01 * target.calcStat(Stats.DEATH_RECEPTIVE, activeChar, this);
				double lethal1 = _lethal1 * mult;
				double lethal2 = _lethal2 * mult;

				if(lethal1 > 0 && Rnd.chance(lethal1))
				{
					if(target.isPlayer())
					{
						target.reduceCurrentHp(target.getCurrentCp(), activeChar, this, true, true, false, true);
						target.sendPacket(Msg.LETHAL_STRIKE);
						activeChar.sendPacket(Msg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					else if(target.isNpc() && !target.isLethalImmune())
					{
						target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar, this, true, true, false, true);
						activeChar.sendPacket(Msg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
				}
				else if(lethal2 > 0 && Rnd.chance(lethal2))
					if(target.isPlayer())
					{
						target.reduceCurrentHp(target.getCurrentHp() + target.getCurrentCp() - 1, activeChar, this, true, true, false, true);
						target.sendPacket(Msg.LETHAL_STRIKE);
						activeChar.sendPacket(Msg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}
					else if(target.isNpc() && !target.isLethalImmune())
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar, this, true, true, false, true);
						activeChar.sendPacket(Msg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
					}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			if(!(ConfigSystem.getBoolean("SavingSpS") && _skillType == SkillType.BUFF))
				activeChar.unChargeShots(isMagic());
	}
}