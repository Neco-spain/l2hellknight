package commands.admin;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.Say2;
import l2rt.gameserver.tables.GmListTable;

public class AdminGmChat implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_gmchat,
		admin_snoop
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanAnnounce)
			return false;

		switch(command)
		{
			case admin_gmchat:
				try
				{
					String text = fullString.replaceFirst(Commands.admin_gmchat.name(), "");
					Say2 cs = new Say2(0, 9, activeChar.getName(), text);
					GmListTable.broadcastToGMs(cs);
				}
				catch(StringIndexOutOfBoundsException e)
				{}
				break;
			case admin_snoop:
			{
				/**
				L2Object target = activeChar.getTarget();
				if(target == null)
				{
					activeChar.sendMessage("You must select a target.");
					return false;
				}
				if(!target.isPlayer)
				{
					activeChar.sendMessage("Target must be a player.");
					return false;
				}
				L2Player player = (L2Player) target;
				player.addSnooper(activeChar);
				activeChar.addSnooped(player);
				*/
			}
		}
		return true;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}