/**
 * 
 */
package handlers.voicedcommandhandlers;

import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.LMEvent;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class LMVoicedInfo implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = {
		"lminfo",
		"lmjoin",
		"lmleave"
		};
	
	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "data/html/mods/LMEvent/Status.htm");

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("lminfo"))
		{
			if (LMEvent.isStarting() || LMEvent.isStarted())
			{
				String htmContent = (USE_STATIC_HTML && !HTML.isEmpty()) ? HTML : 
					HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/LMEvent/Status.htm");
				
				try
				{
					//String[] firstPositions = LMEvent.getFirstPosition(Config.DM_REWARD_FIRST_PLAYERS);
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					
					String htmltext = "";
					/*htmltext += "<table width=\"260\">";
		    		htmltext += "<tr><td width=\"50\">Pos</td><td width=\"130\">Name</td><td width=\"80\" align=\"center\">Points</td></tr>";
		    		if (firstPositions != null)
		    			for (int i = 0; i < firstPositions.length; i++)
		    			{
		    				String[] row = firstPositions[i].split("\\,");
		    				htmltext += "<tr><td>" + (i + 1) + "</td><td>" + row[0] + "</td><td align=\"center\">" + row[1] + "</td></tr>";
		    			}
		    		htmltext += "</table>";
		    		*/
					npcHtmlMessage.setHtml(htmContent);
		    		//npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					npcHtmlMessage.replace("%positions%", htmltext);
					activeChar.sendPacket(npcHtmlMessage);
					
				}
				catch (Exception e)
				{
					_log.warning("wrong LM voiced: " + e);
				}

			}
			else
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		else if (command.equalsIgnoreCase("lmjoin"))
		{
			LMEvent.onBypass("lm_event_participation", activeChar);
		}
		else if (command.equalsIgnoreCase("lmleave"))
		{
			LMEvent.onBypass("lm_event_remove_participation", activeChar);
		}
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
