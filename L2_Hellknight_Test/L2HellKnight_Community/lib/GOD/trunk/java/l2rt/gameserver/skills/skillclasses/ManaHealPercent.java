package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class ManaHealPercent extends L2Skill
{
	public ManaHealPercent(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked(true))
					continue;

				getEffects(activeChar, target, getActivateRate() > 0, false);

				double addToMp = Math.max(0, Math.min(target.getMaxMp() * _power / 100, target.calcStat(Stats.MP_LIMIT, null, null) * target.getMaxMp() / 100. - target.getCurrentMp()));

				if(addToMp > 0)
					target.setCurrentMp(target.getCurrentMp() + addToMp);
				if(target.isPlayer())
					if(activeChar != target)
						target.sendPacket(new SystemMessage(SystemMessage.XS2S_MP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
					else
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToMp)));
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}