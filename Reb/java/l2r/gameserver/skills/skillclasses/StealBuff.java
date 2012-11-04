package l2r.gameserver.skills.skillclasses;

import java.util.Collections;
import java.util.List;

import l2r.commons.util.Rnd;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.skills.EffectType;
import l2r.gameserver.skills.effects.EffectTemplate;
import l2r.gameserver.stats.Env;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.utils.EffectsComparator;

public class StealBuff extends Skill
{
	private final int _stealCount;

	public StealBuff(StatsSet set)
	{
		super(set);
		_stealCount = set.getInteger("stealCount", 1);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(calcStealChance(target, activeChar))
				{
					int stealCount = Rnd.get(1, _stealCount); // ToCheck
					int counter = 0;
					if(!target.isPlayer())
						continue;
					List<Effect> effectsList = target.getEffectList().getAllEffects();
					Collections.sort(effectsList, EffectsComparator.getInstance()); // ToFix: Comparator to HF
					Collections.reverse(effectsList);
					for(Effect e : effectsList)
					{
						if(e.getSkill().getId() == 1363)
							continue;
						if(counter < stealCount)
						{
							if(canSteal(e))
							{
								Effect stolenEffect = cloneEffect(activeChar, e);
								if(stolenEffect != null)
								{
									activeChar.getEffectList().addEffect(stolenEffect);
									target.getEffectList().stopEffect(e.getSkill());
								}
								e.exit();
								counter++;
							}
						}
						else
							break;
					}
				}
				else
				{
					activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
					continue;
				}
				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private boolean canSteal(Effect e)
	{
		return e != null && e.isInUse() && e.isCancelable() && !e.getSkill().isToggle() && !e.getSkill().isPassive() && !e.getSkill().isOffensive() && e.getEffectType() != EffectType.Vitality && !e.getTemplate()._applyOnCaster;
	}

	private boolean calcStealChance(Creature effected, Creature effector)
	{
		if (!isIgnoreResists())
		{
			double cancel_res_multiplier = effected.calcStat(Stats.CANCEL_RESIST, 1, null, null);
			int dml = effector.getLevel() - effected.getLevel();   // to check: magicLevel or player level? Since it's magic skill setting player level as default
			double prelimChance = (dml + 50) * (1 - cancel_res_multiplier * .01);   // 50 is random reasonable constant which gives ~50% chance of steal success while else is equal
			return Rnd.chance(prelimChance);
		}
		return true;
	}

	private Effect cloneEffect(Creature cha, Effect eff)
	{
		Skill skill = eff.getSkill();

		for(EffectTemplate et : skill.getEffectTemplates())
		{
			Effect effect = et.getEffect(new Env(cha, cha, skill));
			if(effect != null)
			{
				effect.setCount(eff.getCount());
				effect.setPeriod(eff.getCount() == 1 ? eff.getPeriod() - eff.getTime() : eff.getPeriod());
				return effect;
			}
		}
		return null;
	}
}