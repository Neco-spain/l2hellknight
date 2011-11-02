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
package com.l2js.gameserver.handler.itemhandlers;

import com.l2js.Config;
import com.l2js.gameserver.model.L2ItemInstance;
import com.l2js.gameserver.model.actor.L2Playable;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.serverpackets.SystemMessage;

public class ManaPotion extends ItemSkills
{
	/**
	 * 
	 * @see com.l2js.gameserver.handler.IItemHandler#useItem(com.l2js.gameserver.model.actor.L2Playable, com.l2js.gameserver.model.L2ItemInstance, boolean)
	 */
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!Config.ALLOW_MANA_POTIONS)
		{
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
			return;
		}
		super.useItem(playable, item, forceUse);
	}
}