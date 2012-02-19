/**
 * 
 */
package handlers.admincommandhandlers;

import l2.brick.Config;
import l2.brick.gameserver.handler.IAdminCommandHandler;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.DM;
import l2.brick.gameserver.model.entity.DMEventTeleporter;
import l2.brick.gameserver.model.entity.DMManager;

public class AdminDMEngine implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_dm_add",
		"admin_dm_remove",
		"admin_dm_advance"
	};
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_dm_add"))
		{
			L2Object target = activeChar.getTarget();
			
			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}
			
			add(activeChar, (L2PcInstance) target);
		}
		else if (command.equals("admin_dm_remove"))
		{
			L2Object target = activeChar.getTarget();
			
			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}
			
			remove(activeChar, (L2PcInstance) target);
		}
		else if ( command.equals( "admin_dm_advance" ) )
		{
			DMManager.getInstance().skipDelay();
		}
		
		return true;
	}
	
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void add(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if (DM.isPlayerParticipant(playerInstance))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}
		
		if (!DM.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}
		
		if (DM.isStarted())
		{
			new DMEventTeleporter(playerInstance, true, false);
		}
	}
	
	private void remove(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if (!DM.removeParticipant(playerInstance))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}
		
		new DMEventTeleporter(playerInstance, Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}
