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

import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2AirShipInstance;
import l2.hellknight.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class EnergyStarStone extends ItemSkills
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final L2AirShipInstance ship = playable.getActingPlayer().getAirShip();
		if (ship == null || !(ship instanceof L2ControllableAirShipInstance) || ship.getFuel() >= ship.getMaxFuel())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			playable.sendPacket(sm);
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		return super.useItem(playable, item, forceUse);
	}
}