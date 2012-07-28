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

import l2.brick.Config;
import l2.brick.gameserver.datatables.PetDataTable;
import l2.brick.gameserver.datatables.SkillTable;
import l2.brick.gameserver.handler.IItemHandler;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.instance.L2PetInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.MagicSkillUse;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.Util;

/**
 * @author Kerberos, Zoey76
 */
public class PetFood implements IItemHandler
{
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		int itemId = item.getItemId();
		switch (itemId)
		{
			case 2515: //Food For Wolves
				useFood(playable, 2048, item);
				break;
			case 4038: //Food For Hatchling
				useFood(playable, 2063, item);
				break;
			case 5168: //Food for Strider
				useFood(playable, 2101, item);
				break;
			case 5169: //Deluxe Food for Strider
				useFood(playable, 2102, item);
				break;
			case 6316: //Food for Wyvern
				useFood(playable, 2180, item);
				break;
			case 7582: //Baby Spice
				useFood(playable, 2048, item);
				break;
			case 9668: //Great Wolf Food
				useFood(playable, 2361, item);
				break;
			case 10425: //Improved Baby Pet Food
				useFood(playable, 2361, item);
				break;
			case 14818: //Enriched Pet Food for Wolves
				useFood(playable, 2916, item);
				break;
			default:
				_log.warning("Pet Food Id: " + itemId + " without handler!");
				break;
		}
	}
	
	public boolean useFood(L2Playable activeChar, int magicId, L2ItemInstance item)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, 1);
		if (skill != null)
		{
			if (activeChar instanceof L2PetInstance)
			{
				if (((L2PetInstance) activeChar).destroyItem("Consume", item.getObjectId(), 1, null, false))
				{
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0));
					((L2PetInstance) activeChar).setCurrentFed(((L2PetInstance) activeChar).getCurrentFed() + (skill.getFeed() * Config.PET_FOOD_RATE));
					((L2PetInstance) activeChar).broadcastStatusUpdate();
					if (((L2PetInstance) activeChar).getCurrentFed() < (((L2PetInstance) activeChar).getPetData().getHungry_limit() / 100f * ((L2PetInstance) activeChar).getPetLevelData().getPetMaxFeed()))
					{
						activeChar.sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
					}
					return true;
				}
			}
			else if (activeChar instanceof L2PcInstance)
			{
				L2PcInstance player = ((L2PcInstance) activeChar);
				int itemId = item.getItemId();
				if (player.isMounted())
				{
					int food[] = PetDataTable.getInstance().getPetData(player.getMountNpcId()).getFood();
					if (Util.contains(food, itemId))
					{
						if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
						{
							player.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0));
							player.setCurrentFeed(player.getCurrentFeed() + skill.getFeed());
						}
						return true;
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
						sm.addItemName(item);
						activeChar.sendPacket(sm);
						return false;
					}
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
					sm.addItemName(item);
					activeChar.sendPacket(sm);
				}
				return false;
			}
		}
		return false;
	}
}