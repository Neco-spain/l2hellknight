package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.tables.GmListTable;

public class AdminGmChat implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_gmchat,
		admin_snoop
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
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
					Say2 cs = new Say2(0, ChatType.ALLIANCE, activeChar.getName(), text);
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

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}