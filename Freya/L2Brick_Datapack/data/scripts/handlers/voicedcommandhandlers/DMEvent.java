package handlers.voicedcommandhandlers;

import l2.brick.Config;
import l2.brick.gameserver.cache.HtmCache;
import l2.brick.gameserver.handler.IVoicedCommandHandler;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.DM;
import l2.brick.gameserver.network.serverpackets.ActionFailed;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;

public class DMEvent implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = {
		"dminfo",
		"dmjoin",
		"dmleave"
		};
	
	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "data/html/mods/DMEvent/Status.htm");

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("dminfo"))
		{
			if (DM.isStarting() || DM.isStarted())
			{
				String htmContent = (USE_STATIC_HTML && !HTML.isEmpty()) ? HTML : 
					HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/DMEvent/Status.htm");
				
				try
				{
					String[] firstPositions = DM.getFirstPosition(Config.DM_REWARD_FIRST_PLAYERS);
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
			DM.onBypass("dm_event_participation", activeChar);
		}
		else if (command.equalsIgnoreCase("dmleave"))
		{
			DM.onBypass("dm_event_remove_participation", activeChar);
		}
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
