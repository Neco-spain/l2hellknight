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

import l2.brick.gameserver.handler.ISkillHandler;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.L2Summon;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.skills.Formulas;
import l2.brick.gameserver.templates.skills.L2SkillType;

public class StealBuffs implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STEAL_BUFF
	};
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.ISkillHandler#useSkill(l2.brick.gameserver.model.actor.L2Character, l2.brick.gameserver.model.L2Skill, l2.brick.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		// discharge shots
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;
			
			if (skill.isMagic())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2Npc)
			((L2Npc) activeChar)._spiritshotcharged = false;
		
		L2Character target;
		L2Effect effect;
		
		int count = (int)skill.getPower();
		for (L2Object obj: targets)
		{
			if (!(obj instanceof L2Character))
				continue;
			target = (L2Character)obj;
			
			if (target.isDead())
				continue;
			
			if (!(target instanceof L2PcInstance))
				continue;
			
			Env env;
			int lastSkillId = 0;
			final L2Effect[] effects = target.getAllEffects();
			final ArrayList<L2Effect> toSteal = new ArrayList<L2Effect>(count);
			
			for (int i = effects.length; --i >= 0;) // reverse order
			{
				effect = effects[i];
				if (effect == null)
					continue;
				
				if (!effect.canBeStolen()) // remove effect if can't be stolen
				{
					effects[i] = null;
					continue;
				}
				
				// if eff time is smaller than 5 sec, will not be stolen, just to save CPU,
				// avoid synchronization(?) problems and NPEs
				if (effect.getAbnormalTime() - effect.getTime() < 5)
				{
					effects[i] = null;
					continue;
				}
				
				// first pass - only dances/songs
				if (!effect.getSkill().isDance())
					continue;
				
				if (effect.getSkill().getId() != lastSkillId)
				{
					lastSkillId = effect.getSkill().getId();
					count--;
				}
				
				toSteal.add(effect);
				if (count == 0)
					break;
			}
			
			if (count > 0) // second pass
			{
				lastSkillId = 0;
				for (int i = effects.length; --i >= 0;)
				{
					effect = effects[i];
					if (effect == null)
						continue;
					
					// second pass - all except dances/songs
					if (effect.getSkill().isDance())
						continue;
					
					if (effect.getSkill().getId() != lastSkillId)
					{
						lastSkillId = effect.getSkill().getId();
						count--;
					}
					
					toSteal.add(effect);
					if (count == 0)
						break;
				}
			}
			
			if (toSteal.size() == 0)
				continue;
			
			// stealing effects
			for (L2Effect eff : toSteal)
			{
				env = new Env();
				env.player = target;
				env.target = activeChar;
				env.skill = eff.getSkill();
				try
				{
					effect = eff.getEffectTemplate().getStolenEffect(env, eff);
					if (effect != null)
					{
						effect.scheduleEffect();
						if (effect.getShowIcon() && activeChar instanceof L2PcInstance)
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
			
			//Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		
		if (skill.hasSelfEffects())
		{
			// Applying self-effects
			effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
}