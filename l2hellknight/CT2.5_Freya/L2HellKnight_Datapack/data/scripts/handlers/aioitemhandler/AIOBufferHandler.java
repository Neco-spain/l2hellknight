package handlers.aioitemhandler;

import java.util.logging.Logger;


import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.AIOItemTable;
import l2.hellknight.gameserver.handler.IAIOItemHandler;
import l2.hellknight.gameserver.model.L2ItemInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class AIOBufferHandler implements IAIOItemHandler 
{
	private static final Logger _log = Logger.getLogger(AIOBufferHandler.class.getName());
	private static final String BYPASS = "buffer";
	
	@Override
	public String getBypass() 
	{
		return BYPASS;
	}

	@Override
	public void onBypassUse(L2PcInstance player, String command) 
	{
		String[] subCommands = command.split(" ");
		final String actualCmd = subCommands[0];
		
		if(actualCmd == null || actualCmd.isEmpty())
		{
			_log.severe("Wrong parameters for the AIOItem buffer: "+command);
			return;
		}
		
		/*
		 * Show all buffs categories
		 */
		if(actualCmd.equalsIgnoreCase("main"))
		{			
			final NpcHtmlMessage main = AIOItemTable.getInstance().getBufferMain();
			if(main == null)
			{
				_log.severe("AIOItemTable: The Buffer main htm page does not exist!");
				return;
			}
			
			player.sendPacket(main);
		}
		/*
		 * Show given category buffs
		 */
		else if(actualCmd.equalsIgnoreCase("category"))
		{
			final String secondCmd = subCommands[1];
			final NpcHtmlMessage html = AIOItemTable.getInstance().getBufferCategoryPage(secondCmd);
			if(html == null)
			{
				_log.severe("AIOItemTable: Buffer Category page ["+secondCmd+"] does not exist!");
				return;
			}
			player.sendPacket(html);
		}
		/*
		 * Single buff
		 */
		else if(actualCmd.equalsIgnoreCase("buff"))
		{
			if(!paymentDone(player))
			{
				return;
			}
			
			int buffId = 0;
			try
			{
				buffId = Integer.parseInt(subCommands[2]);
			}
			catch(NumberFormatException nfe)
			{
				nfe.printStackTrace();
			}
			
			AIOItemTable.getInstance().getBuff(subCommands[1], buffId).getEffects(player, player);
			NpcHtmlMessage msg = AIOItemTable.getInstance().getBufferCategoryPage(subCommands[1]);
			player.sendPacket(msg);
		}
		/*
		 * Buffer services
		 */
		else if(actualCmd.equalsIgnoreCase("other"))
		{
			if(!paymentDone(player))
			{
				return;
			}
			
			String secondCmd = subCommands[1];
			
			/*
			 * Heal
			 */
			if(secondCmd.equalsIgnoreCase("heal"))
			{
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentMp(player.getMaxMp());
				player.setCurrentHp(player.getMaxHp());
			}
			/*
			 * Cancel
			 */
			else if(secondCmd.equalsIgnoreCase("cancel"))
			{
				player.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}
	}

	/**
	 * Will reduce the player items required for the buffer
	 * or will return false, in which case, wont buff/serve
	 * him
	 * @param player
	 * @return boolean
	 */
	private boolean paymentDone(L2PcInstance player)
	{
		L2ItemInstance payment = null;
		if((payment = player.getInventory().getItemByItemId(Config.AIOITEM_BUFF_COIN)) != null)
		{
			if(payment.getCount() < Config.AIOITEM_BUFF_PRICE)
			{
				player.sendMessage("Not enough "+payment.getName()+" to buy buffs!");
				return false;
			}
			else
			{
				player.destroyItemByItemId("AIO Item", Config.AIOITEM_BUFF_COIN, Config.AIOITEM_BUFF_PRICE, player, true);
				return true;
			}
		}
		else
		{
			player.sendMessage("You dont have the required items to buy buffs!");
			return false;
		}
	}	
}