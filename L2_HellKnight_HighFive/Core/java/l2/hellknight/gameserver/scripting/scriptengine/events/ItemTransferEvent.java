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
package l2.hellknight.gameserver.scripting.scriptengine.events;

import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.itemcontainer.ItemContainer;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class ItemTransferEvent implements L2Event
{
	private L2ItemInstance item;
	private L2PcInstance player;
	private ItemContainer target;
	
	/**
	 * @return the item
	 */
	public L2ItemInstance getItem()
	{
		return item;
	}
	
	/**
	 * @param item the item to set
	 */
	public void setItem(L2ItemInstance item)
	{
		this.item = item;
	}
	
	/**
	 * @return the player
	 */
	public L2PcInstance getPlayer()
	{
		return player;
	}
	
	/**
	 * @param player the player to set
	 */
	public void setPlayer(L2PcInstance player)
	{
		this.player = player;
	}
	
	/**
	 * @return the target
	 */
	public ItemContainer getTarget()
	{
		return target;
	}
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(ItemContainer target)
	{
		this.target = target;
	}
}
