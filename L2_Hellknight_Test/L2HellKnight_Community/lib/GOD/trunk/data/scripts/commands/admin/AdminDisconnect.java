package commands.admin;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;

public class AdminDisconnect implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_disconnect,
		admin_kick
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanKick)
			return false;

		switch(command)
		{
			case admin_disconnect:
			case admin_kick:
				final L2Player player;
				if(wordList.length == 1)
				{
					// Обработка по таргету
					L2Object target = activeChar.getTarget();
					if(target == null)
					{
						activeChar.sendMessage("Select character or specify player name.");
						break;
					}
					if(!target.isPlayer())
					{
						activeChar.sendPacket(Msg.INVALID_TARGET);
						break;
					}
					player = (L2Player) target;
				}
				else
				{
					// Обработка по нику
					player = L2World.getPlayer(wordList[1]);
					if(player == null)
					{
						activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
						break;
					}
				}

				activeChar.sendMessage("Character " + player.getName() + " disconnected from server.");

				if(player.isInOfflineMode())
				{
					player.setOfflineMode(false);
					player.logout(false, false, true, true);
					if(player.getNetConnection() != null)
						player.getNetConnection().disconnectOffline();
					return true;
				}

				player.sendMessage(new CustomMessage("scripts.commands.admin.AdminDisconnect.YoureKickedByGM", player));
				player.sendPacket(Msg.YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN);
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
					public void run()
					{
						player.logout(false, false, true, true);
					}
				}, 500);
				break;
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