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

import l2.hellknight.gameserver.handler.IItemHandler;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.Dice;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.Broadcast;
import l2.hellknight.util.Rnd;

public class RollingDice implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		L2PcInstance activeChar = playable.getActingPlayer();
		int itemId = item.getItemId();
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		
		int number = rollDice(activeChar);
		if (number == 0)
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
			return false;
		}
		
		Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), itemId, number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ROLLED_S2);
		sm.addString(activeChar.getName());
		sm.addNumber(number);
		
		activeChar.sendPacket(sm);
		if (activeChar.isInsideZone(L2Character.ZONE_PEACE))
		{
			Broadcast.toKnownPlayers(activeChar, sm);
		}
		else if (activeChar.isInParty()) // TODO: Verify this!
		{
			activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
		}
		return true;
		
	}
	
	/**
	 * @param player
	 * @return
	 */
	private int rollDice(L2PcInstance player)
	{
		// Check if the dice is ready
		if (!player.getFloodProtectors().getRollDice().tryPerformAction("roll dice"))
		{
			return 0;
		}
		return Rnd.get(1, 6);
	}
}
