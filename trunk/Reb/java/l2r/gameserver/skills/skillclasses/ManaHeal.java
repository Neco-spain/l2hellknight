package l2r.gameserver.skills.skillclasses;

import java.util.List;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.StatsSet;

public class ManaHeal extends Skill
{
	private final boolean _ignoreMpEff;

	public ManaHeal(StatsSet set)
	{
		super(set);
		_ignoreMpEff = set.getBool("ignoreMpEff", false);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		double mp = _power;

		int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		if(sps > 0 && Config.MANAHEAL_SPS_BONUS)
			mp *= sps == 2 ? 1.5 : 1.3;

		for(Creature target : targets)
		{
			if(target.isHealBlocked())
				continue;

			double newMp = activeChar == target ? mp : Math.min(mp * 1.7, mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., activeChar, this) : 100.) / 100.);

			// Обработка разницы в левелах при речардже. Учитывыется разница уровня скилла и уровня цели.
			// 1013 = id скилла recharge. Для сервиторов не проверено убавление маны, пока оставлено так как есть.
			if(getMagicLevel() > 0 && activeChar != target)
			{
				int diff = target.getLevel() - getMagicLevel();
				if(diff > 5)
					if(diff < 20)
						newMp = newMp / 100 * (100 - diff * 5);
					else
						newMp = 0;
			}

			if(newMp == 0)
			{
				activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_HAS_FAILED).addSkillName(_id, getDisplayLevel()));
				getEffects(activeChar, target, getActivateRate() > 0, false);
				continue;
			}

			double addToMp = Math.max(0, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100. - target.getCurrentMp()));

			if(addToMp > 0)
				target.setCurrentMp(addToMp + target.getCurrentMp());
			if(target.isPlayer())
				if(activeChar != target)
					target.sendPacket(new SystemMessage2(SystemMsg.S2_MP_HAS_BEEN_RESTORED_BY_C1).addString(activeChar.getName()).addInteger(Math.round(addToMp)));
				else
					activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_MP_HAS_BEEN_RESTORED).addInteger(Math.round(addToMp)));
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}