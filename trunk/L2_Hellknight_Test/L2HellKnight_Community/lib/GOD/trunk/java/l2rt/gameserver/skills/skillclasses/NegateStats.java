package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;

import java.util.concurrent.ConcurrentLinkedQueue;

public class NegateStats extends L2Skill
{
	private final GArray<Stats> _negateStats;
	private final boolean _negateOffensive;
	private final int _negateCount;

	public NegateStats(StatsSet set)
	{
		super(set);

		String[] negateStats = set.getString("negateStats", "").split(" ");
		_negateStats = new GArray<Stats>(negateStats.length);
		for(String stat : negateStats)
			if(!stat.isEmpty())
				_negateStats.add(Stats.valueOfXml(stat));

		_negateOffensive = set.getBool("negateDebuffs", false);
		_negateCount = set.getInteger("negateCount", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(!_negateOffensive && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
					continue;
				}

				int count = 0;
				ConcurrentLinkedQueue<L2Effect> effects = target.getEffectList().getAllEffects();
				for(Stats stat : _negateStats)
					for(L2Effect e : effects)
					{
						L2Skill skill = e.getSkill();
						// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
						if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
						{
							count++;
							continue;
						}
						if(skill.isOffensive() == _negateOffensive && e.containsStat(stat) && skill.isCancelable())
						{
							target.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
							e.exit();
							count++;
						}
						if(_negateCount > 0 && count >= _negateCount)
							break;
					}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	@Override
	public boolean isOffensive()
	{
		return !_negateOffensive;
	}

	public GArray<Stats> getNegateStats()
	{
		return _negateStats;
	}
}