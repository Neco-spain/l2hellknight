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
package l2.hellknight.gameserver.model;

import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.model.items.L2Item;

public class L2ProductItemComponent
{
	private final int _itemId;
	private final int _count;

	private final int _weight;
	private final boolean _dropable;

	public L2ProductItemComponent(int item_id, int count)
	{
	 	_itemId = item_id;
		_count = count;

		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if(item != null)
		{
			_weight = item.getWeight();
			_dropable = item.isDropable();
		}
		else
		{
			_weight = 0;
			_dropable = true;
		}
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getCount()
	{
		return _count;
	}

	public int getWeight()
	{
		return _weight;
	}

	public boolean isDropable()
	{
		return _dropable;
	}
}