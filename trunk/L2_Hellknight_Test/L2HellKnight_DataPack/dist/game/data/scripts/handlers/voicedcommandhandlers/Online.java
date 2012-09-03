package handlers.voicedcommandhandlers;

import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class Online implements IVoicedCommandHandler
{
	public static final String[] VOICED_COMMANDS = { "onlineplayers" };

	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML_LINK = HtmCache.getInstance().getHtm(null, "data/html/mods/online/OnlineStatus.htm");
	
	public boolean useVoicedCommand(String command, L2PcInstance player, String params)
	{
		String htmContent = (USE_STATIC_HTML && !HTML_LINK.isEmpty()) ? HTML_LINK :
		HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/online/OnlineStatus.htm");
		
		if(command.equalsIgnoreCase("onlineplayers"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%playernumber%", String.valueOf(L2World.getInstance().getAllPlayers().size()));
			player.sendPacket(npcHtmlMessage);
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		return true;
	}
	
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}