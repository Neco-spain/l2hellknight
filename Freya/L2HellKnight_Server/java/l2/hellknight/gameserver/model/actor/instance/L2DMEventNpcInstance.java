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
package l2.hellknight.gameserver.model.actor.instance;

import l2.hellknight.Config;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.entity.DM;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.templates.L2NpcTemplate;

public class L2DMEventNpcInstance extends L2Npc
{
	private static final String htmlPath = "data/html/mods/DMEvent/";

	/**
	 * @param objectId
	 * @param template
	 */
	public L2DMEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2DMEventNpcInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		DM.onBypass(command, playerInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance activeChar, int val)
	{
		if (activeChar == null)
			return;

		if (DM.isParticipating())
		{
			final boolean isParticipant = DM.isPlayerParticipant(activeChar.getObjectId()); 
			final String htmContent;

			if (!isParticipant)
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Participation.htm");
			else
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "RemoveParticipation.htm");

	    	if (htmContent != null)
	    	{
	    		int PlayerCounts = DM.getPlayerCounts();
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
	    		npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%playercount%", String.valueOf(PlayerCounts));
				if (!isParticipant)
					npcHtmlMessage.replace("%fee%", DM.getParticipationFee());

				activeChar.sendPacket(npcHtmlMessage);
	    	}
		}
		else if (DM.isStarting() || DM.isStarted())
		{
			final String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Status.htm");

	    	if (htmContent != null)
	    	{
	    		String[] firstPositions = DM.getFirstPosition(Config.DM_REWARD_FIRST_PLAYERS);
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
	    		
	    		String htmltext = "";
	    		htmltext += "<table width=\"250\">";
	    		htmltext += "<tr><td width=\"150\">Name</td><td width=\"100\" align=\"center\">Points</td></tr>";
	    		if (firstPositions != null)
		    		for (int i = 0; i < firstPositions.length; i++)
		    		{
		    			String[] row = firstPositions[i].split("\\,");
		    			htmltext += "<tr><td>" + row[0] + "</td><td align=\"center\">" + row[1] + "</td></tr>";
		    		}
	    		htmltext += "</table>";
	    		
				npcHtmlMessage.setHtml(htmContent);
	    		//npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%positions%", htmltext);
				activeChar.sendPacket(npcHtmlMessage);
	    	}
		}

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
}