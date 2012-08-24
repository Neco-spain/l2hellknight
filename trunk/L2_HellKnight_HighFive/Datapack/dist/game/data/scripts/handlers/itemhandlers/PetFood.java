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

import java.util.List;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.PetDataTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.handler.IItemHandler;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.holders.SkillHolder;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillUse;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Kerberos, Zoey76
 */
public class PetFood implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (playable.isPet() && !((L2PetInstance) playable).canEatFoodId(item.getItemId()))
		{
			playable.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return false;
		}
		
		final SkillHolder[] skills = item.getItem().getSkills();
		if (skills != null)
		{
			for (SkillHolder sk : skills)
			{
				useFood(playable, sk.getSkillId(), sk.getSkillLvl(), item);
			}
		}
		return true;
	}
	
	public boolean useFood(L2Playable activeChar, int skillId, int skillLevel, L2ItemInstance item)
	{
		final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if (skill != null)
		{
			if (activeChar.isPet())
			{
				final L2PetInstance pet = (L2PetInstance) activeChar;
				if (pet.destroyItem("Consume", item.getObjectId(), 1, null, false))
				{
					pet.broadcastPacket(new MagicSkillUse(pet, pet, skillId, skillLevel, 0, 0));
					pet.setCurrentFed(pet.getCurrentFed() + (skill.getFeed() * Config.PET_FOOD_RATE));
					pet.broadcastStatusUpdate();
					if (pet.getCurrentFed() < ((pet.getPetData().getHungryLimit() / 100f) * pet.getPetLevelData().getPetMaxFeed()))
					{
						pet.sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
					}
					return true;
				}
			}
			else if (activeChar.isPlayer())
			{
				final L2PcInstance player = activeChar.getActingPlayer();
				if (player.isMounted())
				{
					final List<Integer> foodIds = PetDataTable.getInstance().getPetData(player.getMountNpcId()).getFood();
					if (foodIds.contains(Integer.valueOf(item.getItemId())))
					{
						if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
						{
							player.broadcastPacket(new MagicSkillUse(player, player, skillId, skillLevel, 0, 0));
							player.setCurrentFeed(player.getCurrentFeed() + skill.getFeed());
							return true;
						}
					}
				}
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addItemName(item);
				player.sendPacket(sm);
			}
		}
		return false;
	}
}