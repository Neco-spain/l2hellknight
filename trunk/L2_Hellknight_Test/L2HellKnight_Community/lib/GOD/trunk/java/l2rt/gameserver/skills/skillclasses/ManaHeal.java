package l2rt.gameserver.skills.skillclasses;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class ManaHeal extends L2Skill
{
	public ManaHeal(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!isHandler() && oneTarget() && !checkTarget(target, activeChar))
		{
			activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		double mp = _power;

		int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		if(sps > 0 && ConfigSystem.getBoolean("ManahealSpSBonus"))
			mp *= sps == 2 ? 1.5 : 1.3;

		for(L2Character target : targets)
			if(checkTarget(target, activeChar))
			{
				if(target.isDead() || target.isHealBlocked(true))
					continue;

				double maxNewMp = activeChar == target ? mp : Math.min(mp * 1.7, target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, mp, activeChar, this));

				// Обработка разницы в левелах при речардже. Учитывыется разница уровня скилла и уровня цели.
				// 1013 = id скилла recharge. Для сервиторов не проверено убавление маны, пока оставлено так как есть.
				if(getMagicLevel() > 0 && activeChar != target)
				{
					int diff = target.getLevel() - getMagicLevel();
					if(diff > 5)
						if(diff < 20)
							maxNewMp = maxNewMp / 100 * (100 - diff * 5);
						else
							maxNewMp = 0;
				}

				if(maxNewMp == 0)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_FAILED).addSkillName(_id, getDisplayLevel()));
					getEffects(activeChar, target, getActivateRate() > 0, false);
					continue;
				}

				double addToMp = Math.max(0, Math.min(maxNewMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100. - target.getCurrentMp()));

				if(addToMp > 0)
					target.setCurrentMp(addToMp + target.getCurrentMp());
				if(target.isPlayer())
					if(activeChar != target)
						target.sendPacket(new SystemMessage(SystemMessage.XS2S_MP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
					else
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToMp)));
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	/**
	 * Нельзя речарджить речарджеров и улучшенных куриц.
	 */
	private final boolean checkTarget(L2Character target, L2Character activeChar)
	{
		if(target == null || target.isDead() || target.isHealBlocked(true))
			return false;

		// бутылки, хербы, и массовый речардж действует на всех
		if(getTargetType() == SkillTargetType.TARGET_SELF || !oneTarget())
			return true;

		// петы и саммоны могут речарджить всех, ограничения только для игроков
		if(target.isPlayer() && activeChar.isPlayer() && target.getPlayer().getSkillLevel(SKILL_RECHARGE) > 0)
			return false;

		// речардж кукабурр - большая халява
		if(target.getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID)
			return false;

		return true;
	}
}