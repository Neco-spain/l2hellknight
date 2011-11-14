package com.l2js.gameserver.handler.voicedcommandhandlers;

import com.l2js.Config;
import com.l2js.gameserver.cache.HtmCache;
import com.l2js.gameserver.handler.IVoicedCommandHandler;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.entity.event.DMEvent;
import com.l2js.gameserver.network.serverpackets.ActionFailed;
import com.l2js.gameserver.network.serverpackets.NpcHtmlMessage;

public class DMVoicedInfo implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = { "dminfo", "dmjoin", "dmleave" };
	
	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "data/html/mods/DMEvent/Status.htm");
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("dminfo"))
		{
			if (DMEvent.isStarting() || DMEvent.isStarted())
			{
				String htmContent = (USE_STATIC_HTML && !HTML.isEmpty()) ? HTML : HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/DMEvent/Status.htm");
				
				try
				{
					String[] firstPositions = DMEvent.getFirstPosition(Config.DM_REWARD_FIRST_PLAYERS);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					
					String htmltext = "";
					Boolean c = true;
					String c1 = "D9CC46";
					String c2 = "FFFFFF";
					if (firstPositions != null)
						for (int i = 0; i < firstPositions.length; i++)
						{
							String[] row = firstPositions[i].split("\\,");
							String color = (c ? c1 : c2);
							htmltext += "<tr>";
							htmltext += "<td width=\"35\" align=\"center\"><font color=\"" + color + "\">" + String.valueOf(i + 1) + "</font></td>";
							htmltext += "<td width=\"100\" align=\"left\"><font color=\"" + color + "\">" + row[0] + "</font></td>";
							htmltext += "<td width=\"125\" align=\"right\"><font color=\"" + color + "\">" + row[1] + "</font></td>";
							htmltext += "</tr>";
							c = !c;
						}
					
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%toprank%", htmltext);
					activeChar.sendPacket(npcHtmlMessage);
					
				}
				catch (Exception e)
				{
					_log.warning("wrong DM voiced: " + e);
				}
				
			}
			else
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		else if (command.equalsIgnoreCase("dmjoin"))
		{
			DMEvent.onBypass("dm_event_participation", activeChar);
		}
		else if (command.equalsIgnoreCase("dmleave"))
		{
			DMEvent.onBypass("dm_event_remove_participation", activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
