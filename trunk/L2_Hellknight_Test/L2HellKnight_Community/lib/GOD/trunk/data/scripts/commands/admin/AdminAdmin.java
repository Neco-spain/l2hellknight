package commands.admin;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.PlaySound;
import l2rt.util.Util;

public class AdminAdmin implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_admin,
		admin_play_sounds,
		admin_play_sound,
		admin_silence,
		admin_tradeoff,
		admin_cfg,
		admin_config,
		admin_show_html
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(activeChar.getPlayerAccess().Menu)
		{
			switch(command)
			{
				case admin_admin:
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/admin.htm"));
					break;
				case admin_play_sounds:
					if(wordList.length == 1)
						activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/songs/songs.htm"));
					else
						try
						{
							activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/songs/songs" + wordList[1] + ".htm"));
						}
						catch(StringIndexOutOfBoundsException e)
						{}
					break;
				case admin_play_sound:
					try
					{
						playAdminSound(activeChar, wordList[1]);
					}
					catch(StringIndexOutOfBoundsException e)
					{}
					break;
				case admin_silence:
					if(activeChar.getMessageRefusal()) // already in message refusal
					// mode
					{
						activeChar.unsetVar("gm_silence");
						activeChar.setMessageRefusal(false);
						activeChar.sendPacket(Msg.MESSAGE_ACCEPTANCE_MODE);
					}
					else
					{
						if(Config.SAVE_GM_EFFECTS)
							activeChar.setVar("gm_silence", "true");
						activeChar.setMessageRefusal(true);
						activeChar.sendPacket(Msg.MESSAGE_REFUSAL_MODE);
					}
					break;
				case admin_tradeoff:
					try
					{
						if(wordList[1].equalsIgnoreCase("on"))
						{
							activeChar.setTradeRefusal(true);
							activeChar.sendMessage("tradeoff enabled");
						}
						else if(wordList[1].equalsIgnoreCase("off"))
						{
							activeChar.setTradeRefusal(false);
							activeChar.sendMessage("tradeoff disabled");
						}
					}
					catch(Exception ex)
					{
						if(activeChar.getTradeRefusal())
							activeChar.sendMessage("tradeoff currently enabled");
						else
							activeChar.sendMessage("tradeoff currently disabled");
					}
					break;
				case admin_cfg:
				case admin_config:
					if(wordList.length < 2)
					{
						activeChar.sendMessage("USAGE: //config parameter[=value]");
						return false;
					}
					activeChar.sendMessage(Config.HandleConfig(activeChar, Util.joinStrings(" ", wordList, 1)));
					break;
				case admin_show_html:
					String html = wordList[1];
					try
					{
						if(html != null)
							activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/" + html));
						else
							activeChar.sendMessage("Html page not found");
					}
					catch(Exception npe)
					{
						activeChar.sendMessage("Html page not found");
					}
					break;
			}
			return true;
		}

		if(activeChar.getPlayerAccess().CanTeleport)
		{
			switch(command)
			{
				case admin_show_html:
					String html = wordList[1];
					try
					{
						if(html != null)
							if(html.startsWith("tele"))
								activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/" + html));
							else
								activeChar.sendMessage("Access denied");
						else
							activeChar.sendMessage("Html page not found");
					}
					catch(Exception npe)
					{
						activeChar.sendMessage("Html page not found");
					}
					break;
			}
			return true;
		}

		return false;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void playAdminSound(L2Player activeChar, String sound)
	{
		activeChar.broadcastPacket(new PlaySound(sound));
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/admin.htm"));
		activeChar.sendMessage("Playing " + sound + ".");
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