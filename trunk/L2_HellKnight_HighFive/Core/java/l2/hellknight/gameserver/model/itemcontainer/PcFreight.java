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
package l2.hellknight.gameserver.model.itemcontainer;

import java.util.List;

import javolution.util.FastList;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance.ItemLocation;
import l2.hellknight.gameserver.model.stats.Stats;

public class PcFreight extends ItemContainer
{
	private final L2PcInstance _owner;
	private int _ownerId = 0;
	
	public PcFreight(int object_id)
	{
		_owner = null;
		_ownerId = object_id;
		restore();
	}
	
	public PcFreight(L2PcInstance owner)
	{
		_owner = owner;
		_ownerId = owner.getObjectId();
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}
	
	/**
	 * Returns the quantity of items in the inventory
	 * @return int
	 */
	@Override
	public int getSize()
	{
		int size = 0;
		for (L2ItemInstance item : _items)
		{
			if (item.getLocation() == getBaseLocation())
				size++;
		}
		return size;
	}
	
	/**
	 * Returns the list of items in inventory
	 * @return L2ItemInstance : items in inventory
	 */
	@Override
	public L2ItemInstance[] getItems()
	{
		List<L2ItemInstance> list = new FastList<>();
		for (L2ItemInstance item : _items)
		{
			if (item.isFreightable())
				list.add(item);
		}
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	/**
	 * Returns the item from inventory by using its <B>itemId</B>
	 * @param itemId : int designating the ID of the item
	 * @return L2ItemInstance designating the item or null if not found in inventory
	 */
	@Override
	public L2ItemInstance getItemByItemId(int itemId)
	{
		for (L2ItemInstance item : _items)
			if ((item.getItemId() == itemId) && (item.getLocation() == ItemLocation.INVENTORY))
				return item;
		
		return null;
	}

	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	@Override
	public boolean validateCapacity(long slots)
	{
		int curSlots = _owner == null ? Config.ALT_FREIGHT_SLOTS : Config.ALT_FREIGHT_SLOTS + (int)_owner.getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
		return (getSize() + slots <= curSlots);
	}
}