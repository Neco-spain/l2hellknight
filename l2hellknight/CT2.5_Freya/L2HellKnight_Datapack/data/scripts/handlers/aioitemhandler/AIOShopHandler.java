package handlers.aioitemhandler;

import java.util.logging.Logger;

import l2.hellknight.gameserver.TradeController;
import l2.hellknight.gameserver.datatables.MultiSell;
import l2.hellknight.gameserver.handler.IAIOItemHandler;
import l2.hellknight.gameserver.model.L2TradeList;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.BuyList;
import l2.hellknight.gameserver.network.serverpackets.ExBuySellListPacket;

public class AIOShopHandler implements IAIOItemHandler 
{
	private static final Logger _log = Logger.getLogger(AIOShopHandler.class.getName());
	private static final String BYPASS = "shop";

	@Override
	public String getBypass() 
	{
		return BYPASS;
	}

	@Override
	public void onBypassUse(L2PcInstance player, String command) 
	{
		String[] subCommand = command.split(" ");		
		/*
		 * Check for null pointers
		 */
		if(subCommand.length < 2)
		{
			_log.severe("Wrong bypass: "+command+" for the AIO Item");
			return;
		}
		
		String actualCmd = subCommand[0];
		
		/*
		 * subCommand parameter npe checked here due common task.
		 * If you are planning to add new cmds and they dont have
		 * to have subCommand parameters necessary, move this
		 * to the buylist, multisell and needed commands 
		 */
		if(subCommand[1] == null || subCommand[1].isEmpty())
		{
			_log.severe("Wrong buylist window/multisell in the AIOItem: "+command);
			return;
		}

		/*
		 * Get the shopId/muiltisell id which gona be used
		 */
		int shopId = 0;
		try
		{
			shopId = Integer.valueOf(subCommand[1]);
		}
		catch(NumberFormatException nfe)
		{
			_log.severe("Wrong shop id passed: "+subCommand[1]);
		}
		if(shopId == 0) return;
				
		/*
		 * Handles the bypasses to open buylists windows
		 */
		if(actualCmd.equalsIgnoreCase("buylist"))
		{						
			player.tempInventoryDisable();
			
			L2TradeList list = TradeController.getInstance().getBuyList(shopId);
			if(list == null)
			{
				_log.severe("The buylist shop id "+shopId+" does not exist!");
				_log.severe("Check the command ["+command+"] in the AIO Item");
				return;
			}
			
			player.setIsUsingAIOItemMultisell(true);
			player.sendPacket(new BuyList(list, player.getAdena(), 0));
			player.sendPacket(new ExBuySellListPacket(player, list, 0, false));
		}
		
		/*
		 * Handles the bypasses to open a multisell window
		 */
		else if(actualCmd.equalsIgnoreCase("multisell"))
		{
			player.setIsUsingAIOItemMultisell(true);
			MultiSell.getInstance().separateAndSend(shopId, player, null, false); //false = multisell, true = exc_multisell
		}
	}
}