package l2r.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.stats.conditions.ConditionTargetRelation;
import l2r.gameserver.templates.StatsSet;

public class ChainHeal extends Skill
{
	private final int[] _healPercents;
	private final int _healRadius;
	private final int _maxTargets;

	public ChainHeal(StatsSet set)
	{
		super(set);
		_healRadius = set.getInteger("healRadius", 350);
		String[] params = set.getString("healPercents", "").split(";");
		_maxTargets = params.length;
		_healPercents = new int[params.length];
		for(int i = 0; i < params.length; i++)
			_healPercents[i] = Integer.parseInt(params[i]);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int curTarget = 0;
		for(Creature target : targets)
		{
			if(target == null)
				continue;

			getEffects(activeChar, target, getActivateRate() > 0, false);

			double hp = _healPercents[curTarget] * target.getMaxHp() / 100.;
			double addToHp = Math.max(0, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));

			if(addToHp > 0)
				target.setCurrentHp(addToHp + target.getCurrentHp(), false);

			if(target.isPlayer())
				if(activeChar != target)
					target.sendPacket(new SystemMessage(SystemMessage.XS2S_HP_HAS_BEEN_RESTORED_BY_S1).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToHp)));

			curTarget++;
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	@Override
	public List<Creature> getTargets(Creature activeChar, Creature aimingTarget, boolean forceUse)
	{
		List<Creature> result = new ArrayList<Creature>();
		List<Creature> targets = aimingTarget.getAroundCharacters(_healRadius, _healRadius);
		if(targets == null)
			return result;

		List<HealTarget> healTargets = new ArrayList<HealTarget>();
		healTargets.add(new HealTarget(-100.0D, aimingTarget));
		for(Creature target : targets)
		{
			if(target == null || target.isHealBlocked() || target.isCursedWeaponEquipped() || target.isCurrentHpFull() || ConditionTargetRelation.getRelation(activeChar, target) != ConditionTargetRelation.Relation.Friend)
				continue;

			double hpPercent = target.getCurrentHp() / target.getMaxHp();
			healTargets.add(new HealTarget(hpPercent, target));
		}
		
		HealTarget[] healTargetsArr = new HealTarget[healTargets.size()];
		healTargets.toArray(healTargetsArr);
		Arrays.sort(healTargetsArr, new Comparator<HealTarget>(){
			@Override
			public int compare(HealTarget o1, HealTarget o2)
			{
				if(o1 == null || o2 == null)
					return 0;
				if(o1.getHpPercent() < o2.getHpPercent())
					return -1;
				if(o1.getHpPercent() > o2.getHpPercent())
					return 1;
				return 0;
			}
		});

		int targetsCount = 0;
		for(HealTarget ht : healTargetsArr)
		{
			result.add(ht.getTarget());
			targetsCount++;
			if(targetsCount >= _maxTargets)
				break;
		}
		return result;
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(activeChar.isPlayable() && target.isMonster())
			return false;
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	private static class HealTarget
	{
		private final double hpPercent;
		private final Creature target;

		public HealTarget(double hpPercent, Creature target)
		{
			this.hpPercent = hpPercent;
			this.target = target;
		}

		public double getHpPercent()
		{
			return hpPercent;
		}

		public Creature getTarget()
		{
			return target;
		}
	}
}