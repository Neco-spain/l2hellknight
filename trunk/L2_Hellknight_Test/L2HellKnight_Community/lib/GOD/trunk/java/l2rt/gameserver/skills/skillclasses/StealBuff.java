package l2rt.gameserver.skills.skillclasses;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

import java.util.concurrent.ConcurrentLinkedQueue;

public class StealBuff extends L2Skill
{
	private final int _stealCount;

	public StealBuff(StatsSet set)
	{
		super(set);
		_stealCount = set.getInteger("stealCount", 1);
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isPlayer())
		{
			activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(getActivateRate() > 0 && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
					continue;
				}

				if(!target.isPlayer())
					continue;

				if(target.checkReflectSkill(activeChar, this))
					target = activeChar;

				ConcurrentLinkedQueue<L2Effect> eff = target.getEffectList().getAllEffects();
				if(eff.size() == 0)
					continue;

				int counter = 0;
				int maxCount = _stealCount;
				boolean update = false;
				L2Effect[] a = eff.toArray(new L2Effect[eff.size()]);

				// Сначало крадем песни/танцы
				for(int i = 0; counter < maxCount && i < a.length; i++)
				{
					L2Effect e = a[a.length - i - 1];
					if(e != null && e.getSkill().isMusic() && e.getSkill().isCancelable() && !e.getSkill().isToggle() && !e.getSkill().isPassive() && (!e.getSkill().isOffensive() || e.getSkill().getId() == 368) && e.getEffectType() != EffectType.Vitality && (!e._template._applyOnCaster || e.getSkill().getId() == 368))
					{
						L2Effect stealedEffect = cloneEffect(activeChar, e);
						e.exit();
						if(stealedEffect != null)
						{
							activeChar.getEffectList().addEffect(stealedEffect);
							update = true;
						}
						counter++;
					}
				}

				// Потом остальное
				for(int i = 0; counter < maxCount && i < a.length; i++)
				{
					L2Effect e = a[a.length - i - 1];
					if(e != null && !e.getSkill().isMusic() && e.getSkill().isCancelable() && !e.getSkill().isToggle() && !e.getSkill().isPassive() && (!e.getSkill().isOffensive() || e.getSkill().getId() == 368) && e.getEffectType() != EffectType.Vitality && (!e._template._applyOnCaster || e.getSkill().getId() == 368))
					{
						L2Effect stealedEffect = cloneEffect(activeChar, e);
						e.exit();
						if(stealedEffect != null)
						{
							activeChar.getEffectList().addEffect(stealedEffect);
							update = true;
						}
						counter++;
					}
				}

				if(update)
				{
					activeChar.sendMessage(new CustomMessage("l2rt.gameserver.skills.skillclasses.StealBuff.Success", activeChar).addNumber(counter));
					activeChar.sendChanges();
					activeChar.updateEffectIcons();
				}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private L2Effect cloneEffect(L2Character cha, L2Effect eff)
	{
		L2Skill skill = eff.getSkill();

		for(EffectTemplate et : skill.getEffectTemplates())
		{
			L2Effect effect = et.getEffect(new Env(cha, cha, skill));
			if(effect != null)
			{
				effect.setCount(eff.getCount());
				if(eff.getCount() == 1)
					effect.setPeriod(eff.getPeriod() - eff.getTime());
				else
					effect.setPeriod(eff.getPeriod());
				return effect;
			}
		}
		return null;
	}
}