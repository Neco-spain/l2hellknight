/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.skillhandlers;

import java.util.ArrayList;
import java.util.logging.Level;

import l2.hellknight.gameserver.handler.ISkillHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.skills.L2SkillType;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.model.stats.Formulas;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class StealBuffs implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STEAL_BUFF
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isNpc())
		{
			((L2Npc) activeChar)._spiritshotcharged = false;
		}
		
		L2Character target;
		L2Effect effect;
		
		int count = skill.getMaxNegatedEffects();
		for (L2Object obj : targets)
		{
			if (!(obj instanceof L2Character))
			{
				continue;
			}
			target = (L2Character) obj;
			
			if (target.isDead())
			{
				continue;
			}
			
			if (!target.isPlayer())
			{
				continue;
			}
			
			Env env;
			int lastSkillId = 0;
			final L2Effect[] effects = target.getAllEffects();
			final ArrayList<L2Effect> toSteal = new ArrayList<>(count);
			
			for (int i = effects.length; --i >= 0;) // reverse order
			{
				effect = effects[i];
				if (effect == null)
				{
					continue;
				}
				
				if (!effect.canBeStolen()) // remove effect if can't be stolen
				{
					effects[i] = null;
					continue;
				}
				
				// if eff time is smaller than 5 sec, will not be stolen, just to save CPU,
				// avoid synchronization(?) problems and NPEs
				if ((effect.getAbnormalTime() - effect.getTime()) < 5)
				{
					effects[i] = null;
					continue;
				}
				
				// first pass - only dances/songs
				if (!effect.getSkill().isDance())
				{
					continue;
				}
				
				if (effect.getSkill().getId() != lastSkillId)
				{
					lastSkillId = effect.getSkill().getId();
					count--;
				}
				
				toSteal.add(effect);
				if (count == 0)
				{
					break;
				}
			}
			
			if (count > 0) // second pass
			{
				lastSkillId = 0;
				for (int i = effects.length; --i >= 0;)
				{
					effect = effects[i];
					if (effect == null)
					{
						continue;
					}
					
					// second pass - all except dances/songs
					if (effect.getSkill().isDance())
					{
						continue;
					}
					
					if (effect.getSkill().getId() != lastSkillId)
					{
						lastSkillId = effect.getSkill().getId();
						count--;
					}
					
					toSteal.add(effect);
					if (count == 0)
					{
						break;
					}
				}
			}
			
			if (toSteal.size() == 0)
			{
				continue;
			}
			
			// stealing effects
			for (L2Effect eff : toSteal)
			{
				env = new Env();
				env.setCharacter(target);
				env.setTarget(activeChar);
				env.setSkill(eff.getSkill());
				try
				{
					effect = eff.getEffectTemplate().getStolenEffect(env, eff);
					if (effect != null)
					{
						effect.scheduleEffect();
						if (effect.getShowIcon() && activeChar.isPlayer())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(effect);
							activeChar.sendPacket(sm);
						}
					}
					// Finishing stolen effect
					eff.exit();
				}
				catch (RuntimeException e)
				{
					_log.log(Level.WARNING, "Cannot steal effect: " + eff + " Stealer: " + activeChar + " Stolen: " + target, e);
				}
			}
			
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		
		if (skill.hasSelfEffects())
		{
			// Applying self-effects
			effect = activeChar.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				// Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
		
		activeChar.spsUncharge(skill);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
