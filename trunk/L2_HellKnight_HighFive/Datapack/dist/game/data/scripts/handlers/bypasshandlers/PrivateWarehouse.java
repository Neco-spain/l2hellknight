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
package handlers.bypasshandlers;

import java.util.logging.Level;

import l2.hellknight.Config;
import l2.hellknight.gameserver.handler.IBypassHandler;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2.hellknight.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2.hellknight.gameserver.network.serverpackets.WareHouseDepositList;
import l2.hellknight.gameserver.network.serverpackets.WareHouseWithdrawalList;

public class PrivateWarehouse implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"withdrawp",
		"withdrawsortedp",
		"depositp"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
		{
			return false;
		}
		
		if (activeChar.isEnchanting())
		{
			return false;
		}
		
		try
		{
			if (command.toLowerCase().startsWith(COMMANDS[0])) // WithdrawP
			{
				if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE)
				{
					NpcHtmlMessage msg = new NpcHtmlMessage(((L2Npc) target).getObjectId());
					msg.setFile(activeChar.getHtmlPrefix(), "data/html/mods/WhSortedP.htm");
					msg.replace("%objectId%", String.valueOf(((L2Npc) target).getObjectId()));
					activeChar.sendPacket(msg);
				}
				else
				{
					showWithdrawWindow(activeChar, null, (byte) 0);
				}
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1])) // WithdrawSortedP
			{
				final String param[] = command.split(" ");
				
				if (param.length > 2)
				{
					showWithdrawWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
				}
				else if (param.length > 1)
				{
					showWithdrawWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
				}
				else
				{
					showWithdrawWindow(activeChar, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
				}
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[2])) // DepositP
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.setActiveWarehouse(activeChar.getWarehouse());
				activeChar.tempInventoryDisable();
				
				if (Config.DEBUG)
				{
					_log.fine("Source: L2WarehouseInstance.java; Player: " + activeChar.getName() + "; Command: showDepositWindow; Message: Showing items to deposit.");
				}
				
				activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.PRIVATE));
				return true;
			}
			
			return false;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	private static final void showWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
		}
		
		if (Config.DEBUG)
		{
			_log.fine("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindow; Message: Showing stored items.");
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
