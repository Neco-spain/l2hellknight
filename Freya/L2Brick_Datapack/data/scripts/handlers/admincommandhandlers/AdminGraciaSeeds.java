package handlers.admincommandhandlers;

import java.util.Calendar;
import java.util.StringTokenizer;

import l2.brick.gameserver.handler.IAdminCommandHandler;
import l2.brick.gameserver.instancemanager.GraciaSeedManager;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminGraciaSeeds implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gracia_seeds",
		"admin_kill_tiat",
		"admin_set_sodstate"
	};
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, l2.brick.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("admin_kill_tiat"))
			GraciaSeedManager.getInstance().increaseSoDTiatKilled();
		else if (actualCommand.equalsIgnoreCase("admin_set_sodstate"))
			GraciaSeedManager.getInstance().setSoDState(Integer.parseInt(val), true);
		
		showMenu(activeChar);
		return true;
	}
	
	private void showMenu(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/graciaseeds.htm");
		html.replace("%sodstate%", String.valueOf(GraciaSeedManager.getInstance().getSoDState()));
		html.replace("%sodtiatkill%", String.valueOf(GraciaSeedManager.getInstance().getSoDTiatKilled()));
		if (GraciaSeedManager.getInstance().getSoDTimeForNextStateChange() > 0)
		{
			Calendar nextChangeDate = Calendar.getInstance();
			nextChangeDate.setTimeInMillis(System.currentTimeMillis() + GraciaSeedManager.getInstance().getSoDTimeForNextStateChange());
			html.replace("%sodtime%", nextChangeDate.getTime().toString());
		}
		else
			html.replace("%sodtime%", "-1");
		activeChar.sendPacket(html);
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
