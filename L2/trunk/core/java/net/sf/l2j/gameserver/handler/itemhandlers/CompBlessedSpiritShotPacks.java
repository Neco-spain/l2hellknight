/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:30:07 $
 */

public class CompBlessedSpiritShotPacks implements IItemHandler
{
	private static final int[] ITEM_IDS = { 5146, 5147, 5148, 5149, 5150, 5151, 5262, 5263, 5264, 5265, 5266, 5267 };

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;

	    int itemId = item.getItemId();
	    int itemToCreateId;
	    int amount;

	    if (itemId < 5200){ // Normal Compressed Package of SpiritShots
    		itemToCreateId = itemId - 1199; // Gives id of matching item for this pack
    		amount = 300;
	    }else{  // Greater Compressed Package of Spirithots
     		itemToCreateId = itemId - 1315; // Gives id of matching item for this pack
	    	amount = 1000;
	    }

	    activeChar.getInventory().destroyItem("Extract", item, activeChar, null);
	    activeChar.getInventory().addItem("Extract", itemToCreateId, amount, activeChar, item);

	    SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
	    sm.addItemName(itemToCreateId);
	    sm.addNumber(amount);
        activeChar.sendPacket(sm);

        ItemList playerUI = new ItemList(activeChar, false);
		activeChar.sendPacket(playerUI);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
