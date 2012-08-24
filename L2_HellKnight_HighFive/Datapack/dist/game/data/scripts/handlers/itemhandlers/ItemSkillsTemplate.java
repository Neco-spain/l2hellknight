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
package handlers.itemhandlers;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.handler.IItemHandler;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2ServitorInstance;
import l2.hellknight.gameserver.model.entity.TvTEvent;
import l2.hellknight.gameserver.model.entity.TvTRoundEvent;
import l2.hellknight.gameserver.model.holders.SkillHolder;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.items.type.L2EtcItemType;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

/**
 * Template for item skills handler.<br>
 * Only minimum of checks.
 */
public class ItemSkillsTemplate implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		final L2PcInstance activeChar = playable.getActingPlayer();
		if (!playable.isPet() && !playable.isPlayer())
		{
			return false;
		}
		
		if (!TvTEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!TvTRoundEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Pets can use items only when they are tradable.
		if (playable.isPet() && !item.isTradeable())
		{
			activeChar.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		// Verify that item is not under reuse.
		if (!checkReuse(activeChar, null, item))
		{
			return false;
		}
		
		int skillId;
		int skillLvl;
		
		final SkillHolder[] skills = item.getEtcItem().getSkills();
		if (skills == null)
		{
			_log.info("Item " + item + " does not have registered any skill for handler.");
			return false;
		}
		
		for (SkillHolder skillInfo : skills)
		{
			if (skillInfo == null)
			{
				continue;
			}
			
			skillId = skillInfo.getSkillId();
			skillLvl = skillInfo.getSkillLvl();
			L2Skill itemSkill = skillInfo.getSkill();
			
			if (itemSkill != null)
			{
				if (!itemSkill.checkCondition(playable, playable.getTarget(), false))
				{
					return false;
				}
				
				if (playable.isSkillDisabled(itemSkill))
				{
					return false;
				}
				
				// Verify that skill is not under reuse.
				if (!checkReuse(activeChar, itemSkill, item))
				{
					return false;
				}
				
				if (!item.isPotion() && !item.isElixir() && playable.isCastingNow())
				{
					return false;
				}
				
				if ((itemSkill.getItemConsumeId() == 0) && (itemSkill.getItemConsume() > 0) && (item.isPotion() || item.isElixir() || itemSkill.isSimultaneousCast()))
				{
					if (!playable.destroyItem("Consume", item.getObjectId(), itemSkill.getItemConsume(), playable, false))
					{
						activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						return false;
					}
				}
				
				// send message to owner
				if (playable.isPet())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
					sm.addString(itemSkill.getName());
					activeChar.sendPacket(sm);
				}
				else
				{
					switch (skillId)
					{
					// short buff icon for healing potions
						case 2031:
						case 2032:
						case 2037:
						case 26025:
						case 26026:
							final int buffId = activeChar._shortBuffTaskSkillId;
							if ((skillId == 2037) || (skillId == 26025))
							{
								activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getBuffDuration() / 1000);
							}
							else if (((skillId == 2032) || (skillId == 26026)) && (buffId != 2037) && (buffId != 26025))
							{
								activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getBuffDuration() / 1000);
							}
							else
							{
								if ((buffId != 2037) && (buffId != 26025) && (buffId != 2032) && (buffId != 26026))
								{
									activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getBuffDuration() / 1000);
								}
							}
							break;
					}
				}
				
				if (item.isPotion() || item.isElixir() || itemSkill.isSimultaneousCast())
				{
					playable.doSimultaneousCast(itemSkill);
					// Summons should be affected by herbs too, self time effect is handled at L2Effect constructor
					if (!playable.isPet() && (item.getItemType() == L2EtcItemType.HERB) && (activeChar.getPet() != null) && (activeChar.getPet() instanceof L2ServitorInstance))
					{
						activeChar.getPet().doSimultaneousCast(itemSkill);
					}
				}
				else
				{
					playable.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (!playable.useMagic(itemSkill, forceUse, false))
					{
						return false;
					}
					
					// Consume.
					if ((itemSkill.getItemConsumeId() == 0) && (itemSkill.getItemConsume() > 0))
					{
						if (!playable.destroyItem("Consume", item.getObjectId(), itemSkill.getItemConsume(), null, false))
						{
							activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
							return false;
						}
					}
				}
				
				if (itemSkill.getReuseDelay() > 0)
				{
					activeChar.addTimeStamp(itemSkill, itemSkill.getReuseDelay());
				}
			}
		}
		return true;
	}
	
	/**
	 * @param player the player using the item or skill
	 * @param skill the skill being used, can be null
	 * @param item the item being used
	 * @return {@code true} if the the item or skill to check is available, {@code false} otherwise
	 */
	private boolean checkReuse(L2PcInstance player, L2Skill skill, L2ItemInstance item)
	{
		SystemMessage sm = null;
		final long remainingTime = (skill != null) ? player.getSkillRemainingReuseTime(skill.getReuseHashCode()) : player.getItemRemainingReuseTime(item.getObjectId());
		final boolean isAvailable = remainingTime <= 0;
		if (!isAvailable)
		{
			final int hours = (int) (remainingTime / 3600000L);
			final int minutes = (int) (remainingTime % 3600000L) / 60000;
			final int seconds = (int) ((remainingTime / 1000) % 60);
			if (hours > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
				if ((skill == null) || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
				sm.addNumber(hours);
				sm.addNumber(minutes);
			}
			else if (minutes > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
				if ((skill == null) || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
				sm.addNumber(minutes);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
				if ((skill == null) || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
			}
			sm.addNumber(seconds);
		}
		else if (skill == null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addItemName(item);
		}
		if (sm != null)
		{
			player.sendPacket(sm);
		}
		return isAvailable;
	}
}
