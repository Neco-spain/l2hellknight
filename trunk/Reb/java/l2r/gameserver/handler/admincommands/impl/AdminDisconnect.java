package l2r.gameserver.handler.admincommands.impl;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class AdminDisconnect implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_disconnect,
		admin_kick
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanKick)
			return false;

		switch(command)
		{
			case admin_disconnect:
			case admin_kick:
				final Player player;
				if(wordList.length == 1)
				{
					// Обработка по таргету
					GameObject target = activeChar.getTarget();
					if(target == null)
					{
						activeChar.sendMessage("Select character or specify player name.");
						break;
					}
					if(!target.isPlayer())
					{
						activeChar.sendPacket(SystemMsg.INVALID_TARGET);
						break;
					}
					player = (Player) target;
				}
				else
				{
					// Обработка по нику
					player = World.getPlayer(wordList[1]);
					if(player == null)
					{
						activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
						break;
					}
				}

				if(player.getObjectId() == activeChar.getObjectId())
				{
					activeChar.sendMessage("You can't logout your character.");
					break;
				}

				activeChar.sendMessage("Character " + player.getName() + " disconnected from server.");

				if(player.isInOfflineMode())
				{
					player.setOfflineMode(false);
					player.kick();
					return true;
				}

				player.sendMessage(new CustomMessage("admincommandhandlers.AdminDisconnect.YoureKickedByGM", player));
				player.sendPacket(SystemMsg.YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_);
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					@Override
					public void runImpl() throws Exception
					{
						player.kick();
					}
				}, 500);
				break;
		}
		return true;
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}