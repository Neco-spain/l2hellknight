/**
 * 
 */
package com.l2js.gameserver.handler.admincommandhandlers;

import com.l2js.Config;
import com.l2js.gameserver.handler.IAdminCommandHandler;
import com.l2js.gameserver.model.L2Object;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.entity.event.DMEvent;
import com.l2js.gameserver.model.entity.event.DMEventTeleporter;
import com.l2js.gameserver.model.entity.event.DMManager;

/**
 * @author L0ngh0rn
 */
public class AdminDMEvent implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_dm_add", "admin_dm_remove", "admin_dm_advance" };
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;
		
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
		else if (command.equals("admin_dm_advance"))
		{
			DMManager.getInstance().skipDelay();
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void add(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if (DMEvent.isPlayerParticipant(playerInstance))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}
		
		if (!DMEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}
		
		if (DMEvent.isStarted())
			new DMEventTeleporter(playerInstance, true, false);
	}
	
	private void remove(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if (!DMEvent.removeParticipant(playerInstance))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}
		
		new DMEventTeleporter(playerInstance, Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}
