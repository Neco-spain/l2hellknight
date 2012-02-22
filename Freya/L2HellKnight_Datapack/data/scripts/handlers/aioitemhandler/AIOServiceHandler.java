package handlers.aioitemhandler;

import java.util.logging.Logger;

import l2.hellknight.gameserver.datatables.HennaTreeTable;
import l2.hellknight.gameserver.handler.IAIOItemHandler;
import l2.hellknight.gameserver.model.L2HennaInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2.hellknight.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2.hellknight.gameserver.network.serverpackets.HennaEquipList;
import l2.hellknight.gameserver.network.serverpackets.HennaRemoveList;

public class AIOServiceHandler implements IAIOItemHandler 
{
	private static final Logger _log = Logger.getLogger(AIOServiceHandler.class.getName());
	private static final String BYPASS = "services";
	
	@Override
	public String getBypass() 
	{
		return BYPASS;
	}

	@Override
	public void onBypassUse(L2PcInstance player, String command) 
	{
		String[] subCommands = command.split(" ");	
		if(subCommands.length < 2)
		{
			_log.warning("AIOServiceHandler: Wrong bypass: "+command);
		}
		
		String actualCmd = subCommands[0];
		String secondCmd = subCommands[1];
		
		if(secondCmd == null || secondCmd.isEmpty())
		{
			_log.severe("Wrong sub-bypass for the AIO Item at: "+command);
			return;
		}
		
		/*
		 * Augment
		 */
		if(actualCmd.equalsIgnoreCase("augment"))
		{
			/*
			 * Add an augmentation
			 */
			if(secondCmd.equalsIgnoreCase("add"))
			{
				player.sendPacket(new ExShowVariationMakeWindow());
			}
			/*
			 * Remove an agumentation
			 */
			else if(secondCmd.equalsIgnoreCase("erase"))
			{
				player.sendPacket(new ExShowVariationCancelWindow());
			}
			/*
			 * Wrong bypass
			 */
			else
			{
				_log.severe("Wrong tag for Aioitem_services_augment_: "+secondCmd);
				return;
			}
		}
		/*
		 * Henna draw & erase
		 */
		else if(actualCmd.equalsIgnoreCase("henna"))
		{
			/*
			 * Draw a symbol
			 */
			if(secondCmd.equalsIgnoreCase("add"))
			{
				L2HennaInstance[] tato = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId());
				player.sendPacket(new HennaEquipList(player, tato));
			}
			/*
			 * Erase a symbol
			 */
			else if(secondCmd.equalsIgnoreCase("erase"))
			{
				boolean hasHennas = false;
				
				for (int i = 1; i <= 3; i++)
				{
					L2HennaInstance henna = player.getHenna(i);
				
					if (henna != null)
							hasHennas = true;
				}
				
				if (hasHennas)
				{
					player.sendPacket(new HennaRemoveList(player));
				}
			}
			/*
			 * Wrong bypass
			 */
			else
			{
				_log.severe("Wrong tag for Aioitem_henna_ : "+secondCmd);
				return;
			}
		}
		/*
		 * Wrong bypass
		 */
		else
		{
			_log.severe("Wrong bypass for the AIOItem services tag: "+command);
			return;
		}
	}
}