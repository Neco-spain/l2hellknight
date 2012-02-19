package handlers.aioitemhandler;

import java.util.logging.Logger;

import l2.brick.Config;
import l2.brick.gameserver.datatables.AIOItemTable;
import l2.brick.gameserver.handler.IAIOItemHandler;
import l2.brick.gameserver.model.L2ItemInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;

public class AIOTeleportHandler implements IAIOItemHandler 
{
	private static final Logger _log = Logger.getLogger(AIOTeleportHandler.class.getName());
	private static final String BYPASS = "teleport";
	
	@Override
	public String getBypass() 
	{
		return BYPASS;
	}

	@Override
	public void onBypassUse(L2PcInstance player, String command) 
	{
		String[] subCommand = command.split(" ");		
		String actualCmd = subCommand[0];

		if(actualCmd.equalsIgnoreCase("main"))
		{
			final NpcHtmlMessage main = AIOItemTable.getInstance().getTeleportMain();
			
			if(main == null)
			{
				_log.severe("AIOItemTable: Teleport main page is null");
				return;
			}
			
			player.sendPacket(main);
		}
		else if(actualCmd.equalsIgnoreCase("categorypage"))
		{
			if(subCommand.length < 2)
			{
				_log.warning("AIOTeleportHandler: Wrong category page bypass: "+command);
				return;
			}
			
			int id = 0;
			try
			{
				id = Integer.parseInt(subCommand[1]);
			}
			catch(NumberFormatException e)
			{
				_log.warning("AIOTeleportHandler: Wrong Teleport category id: "+subCommand[1]);
				return;
			}

			final NpcHtmlMessage categoryPage = AIOItemTable.getInstance().getTeleportCategoryPage(id);
			if(categoryPage == null)
			{
				_log.severe("AIOItemTable: The category page "+id+" does not exist!");
				return;
			}
			
			player.sendPacket(categoryPage);
		}
		else if(actualCmd.equalsIgnoreCase("goto"))
		{
			if(subCommand.length < 3)
			{
				_log.severe("Wrong category/spawn point in the AIOItem: "+command);
				return;
			}
			
			if(!paymentDone(player))
			{
				return;
			}
			
			int categoryId = 0;
			int spawnId = 0;
			try
			{
				categoryId = Integer.parseInt(subCommand[1]);
				spawnId = Integer.parseInt(subCommand[2]);
			}
			catch(NumberFormatException e)
			{
				_log.warning("AIOTeleportTable: Wrong teleport bypass (goto): "+command);
				return;
			}
			
			Integer[] coords = AIOItemTable.getInstance().getTelportCoordinates(categoryId, spawnId);
			if(coords == null)
			{
				_log.warning("AIOItemTable: Teleport spawn point ["+spawnId+"] from category ["+categoryId+"] does not exist!");
				return;
			}
			
			player.teleToLocation(coords[0], coords[1], coords[2]);
		}
	}
	
	/**
	 * Will try to make the player payment. If success paying, will
	 * return true, otherwise, will return false
	 * @param player
	 * @return boolean
	 */
	private boolean paymentDone(L2PcInstance player)
	{
		L2ItemInstance coin = null;
		
		if((coin = player.getInventory().getItemByItemId(Config.AIOITEM_GK_COIN)) != null)
		{
			if(coin.getCount() >= Config.AIOITEM_GK_PRICE)
			{
				player.destroyItemByItemId("AIOItem", coin.getItemId(), Config.AIOITEM_GK_PRICE, player, true);
				return true;
			}
			else
			{
				player.sendMessage("Not enough "+coin.getName()+" to travel!");
				return false;
			}
		}
		else
		{
			player.sendMessage("You dont have the required items to travel!");
			return false;
		}
	}
}