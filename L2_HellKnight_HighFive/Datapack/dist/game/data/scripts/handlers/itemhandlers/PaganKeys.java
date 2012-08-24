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

import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.handler.IItemHandler;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;

/**
 * @author chris
 */
public class PaganKeys implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final int itemId = item.getItemId();
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final L2Object target = activeChar.getTarget();
		
		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		L2DoorInstance door = (L2DoorInstance) target;
		
		if (!(activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false)))
		{
			activeChar.sendMessage("Too far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if (activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
		{
			activeChar.sendMessage("You cannot use the key now.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return false;
		
		// TODO: Unhardcode these!
		switch (itemId)
		{
			case 9698:
				if (door.getDoorId() == 24220020)
				{
					if (activeChar.getInstanceId() != door.getInstanceId())
					{
						for (L2DoorInstance instanceDoor : InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoors())
						{
							if (instanceDoor.getDoorId() == door.getDoorId())
							{
								instanceDoor.openMe();
								break;
							}
						}
					}
					else
					{
						door.openMe();
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 9699:
				if (door.getDoorId() == 24220022)
				{
					if (activeChar.getInstanceId() != door.getInstanceId())
					{
						for (L2DoorInstance instanceDoor : InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoors())
						{
							if (instanceDoor.getDoorId() == door.getDoorId())
							{
								instanceDoor.openMe();
								break;
							}
						}
					}
					else
					{
						door.openMe();
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 8056:
				if (door.getDoorId() == 23150004 || door.getDoorId() == 23150003)
				{
					DoorTable.getInstance().getDoor(23150003).openMe();
					DoorTable.getInstance().getDoor(23150004).openMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
		}
		return true;
	}
}
