/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import l2.hellknight.ExternalConfig;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.TvTRoundEvent;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * TvT Round info.
 * 
 * @author SolidSnake
 */
public class TvTRoundVoicedInfo implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = { "tvtround" };
	
	/**
	 * Set this to false and recompile script if you dont want to use string cache.
	 * This will decrease performance but will be more consistent against possible html editions during runtime
	 * Recompiling the script will get the new html would be enough too [DrHouse]
	 */
	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "data/html/mods/TvTRoundEvent/Status.htm");
	
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("tvtround"))
		{
			if (TvTRoundEvent.isStarting() || TvTRoundEvent.isStarted())
			{
				String htmContent = (USE_STATIC_HTML && !HTML.isEmpty()) ? HTML :
					HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/TvTRoundEvent/Status.htm");
				
				try
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
					
					npcHtmlMessage.setHtml(htmContent);
					// npcHtmlMessage.replace("%objectId%",
					// String.valueOf(getObjectId()));
					npcHtmlMessage.replace("%roundteam1name%", ExternalConfig.TVT_ROUND_EVENT_TEAM_1_NAME);
					npcHtmlMessage.replace("%roundteam1playercount%", String.valueOf(TvTRoundEvent.getTeamsPlayerCounts()[0]));
					npcHtmlMessage.replace("%roundteam1points%", String.valueOf(TvTRoundEvent.getTeamsPoints()[0]));
					npcHtmlMessage.replace("%roundteam2name%", ExternalConfig.TVT_ROUND_EVENT_TEAM_2_NAME);
					npcHtmlMessage.replace("%roundteam2playercount%", String.valueOf(TvTRoundEvent.getTeamsPlayerCounts()[1]));
					npcHtmlMessage.replace("%roundteam2points%", String.valueOf(TvTRoundEvent.getTeamsPoints()[1]));
					activeChar.sendPacket(npcHtmlMessage);
				}
				catch (Exception e)
				{
					_log.warning("wrong TvT Round voiced: " + e);
				}
				
			}
			else
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		return true;
	}
	
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
