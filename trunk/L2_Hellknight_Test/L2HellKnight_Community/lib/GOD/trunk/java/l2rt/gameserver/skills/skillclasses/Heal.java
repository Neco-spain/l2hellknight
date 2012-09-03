package l2rt.gameserver.skills.skillclasses;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2SiegeHeadquarterInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class Heal extends L2Skill
{
	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || target.isDoor() || target instanceof L2SiegeHeadquarterInstance)
			return false;

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	public Heal(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		// Надо уточнить формулу.
		double hp = _power;
		if(!isHandler())
			hp += 0.1 * _power * Math.sqrt(activeChar.getMAtk(null, this) / 333);

		int sps = isSSPossible() && getHpConsume() == 0 ? activeChar.getChargedSpiritShot() : 0;

		if(sps == 2)
			hp *= 1.5;
		else if(sps == 1)
			hp *= 1.3;

		if(activeChar.getSkillMastery(getId()) == 3)
		{
			activeChar.removeSkillMastery(getId());
			hp *= 3;
		}

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked(true))
					continue;

				// Player holding a cursed weapon can't be healed and can't heal
				if(target != activeChar)
					if(target.isPlayer() && target.isCursedWeaponEquipped())
						continue;
					else if(activeChar.isPlayer() && activeChar.isCursedWeaponEquipped())
						continue;

				double maxNewHp = hp * target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, activeChar, this) / 100;
				maxNewHp = activeChar.calcStat(Stats.HEAL_POWER, maxNewHp, target, this);
				double addToHp = Math.max(0, Math.min(maxNewHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));

				if(addToHp > 0)
					target.setCurrentHp(addToHp + target.getCurrentHp(), false);
				if(getId() == 4051)
					target.sendPacket(Msg.REJUVENATING_HP);
				else if(target.isPlayer())
					if(activeChar == target)
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToHp)));
					else
						// FIXME показывать ли лекарю, сколько он восстановил HP цели?
						target.sendPacket(new SystemMessage(SystemMessage.XS2S_HP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				else if(target.isSummon() || target.isPet())
				{
					L2Player owner = target.getPlayer();
					if(owner != null)
					{
						if(activeChar == target) // Пет лечит сам себя
							owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner).addNumber(Math.round(addToHp)));
						else if(owner == activeChar) // Хозяин лечит пета
							owner.sendMessage(new CustomMessage("YOU_HAVE_RESTORED_S1_HP_OF_YOUR_PET", owner).addNumber(Math.round(addToHp)));
						else
							// Пета лечит кто-то другой
							owner.sendMessage(new CustomMessage("S1_HAS_BEEN_RESTORED_S2_HP_OF_YOUR_PET", owner).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
					}
				}
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}