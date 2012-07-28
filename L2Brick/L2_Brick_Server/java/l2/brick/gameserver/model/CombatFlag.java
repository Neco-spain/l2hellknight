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
package l2.brick.gameserver.model;

import l2.brick.Config;
import l2.brick.gameserver.datatables.ItemTable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.InventoryUpdate;
import l2.brick.gameserver.network.serverpackets.ItemList;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

@SuppressWarnings("unused")
public class CombatFlag
{
	// private static final Logger _log = Logger.getLogger(CombatFlag.class.getName());
	
	private L2PcInstance _player = null;
	private int _playerId = 0;
	private L2ItemInstance _item = null;
	private L2ItemInstance _itemInstance;
	private final Location _location;
	private final int _itemId;
	private final int _heading;
	private final int _fortId;
	
	public CombatFlag(int fort_id, int x, int y, int z, int heading, int item_id)
	{
		_fortId = fort_id;
		_location = new Location(x, y, z, heading);
		_heading = heading;
		_itemId = item_id;
	}
	
	public synchronized void spawnMe()
	{
		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		_itemInstance = ItemTable.getInstance().createItem("Combat", _itemId, 1, null, null);
		_itemInstance.dropMe(null, _location.getX(), _location.getY(), _location.getZ());
	}
	
	public synchronized void unSpawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		if (_itemInstance != null)
		{
			_itemInstance.decayMe();
		}
	}
	
	public boolean activate(L2PcInstance player, L2ItemInstance item)
	{
		if (player.isMounted())
		{
			player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			return false;
		}
		
		// Player holding it data
		_player = player;
		_playerId = _player.getObjectId();
		_itemInstance = null;
		
		// Equip with the weapon
		_item = item;
		_player.getInventory().equipItem(_item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(_item);
		_player.sendPacket(sm);
		
		// Refresh inventory
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendPacket(iu);
		}
		else
		{
			_player.sendPacket(new ItemList(_player, false));
		}
		// Refresh player stats
		_player.broadcastUserInfo();
		_player.setCombatFlagEquipped(true);
		return true;
	}
	
	public void dropIt()
	{
		// Reset player stats
		_player.setCombatFlagEquipped(false);
		int slot = _player.getInventory().getSlotFromItem(_item);
		_player.getInventory().unEquipItemInBodySlot(slot);
		_player.destroyItem("CombatFlag", _item, null, true);
		_item = null;
		_player.broadcastUserInfo();
		_player = null;
		_playerId = 0;
	}
	
	public int getPlayerObjectId()
	{
		return _playerId;
	}
	
	public L2ItemInstance getCombatFlagInstance()
	{
		return _itemInstance;
	}
}
