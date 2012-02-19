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
package l2.brick.gameserver.model.actor.instance;

import l2.brick.gameserver.cache.HtmCache;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.entity.LMEvent;
import l2.brick.gameserver.network.serverpackets.ActionFailed;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.templates.L2NpcTemplate;

/**
 * @author L0ngh0rn
 *
 */
public class L2LMEventNpcInstance extends L2Npc
{
	private static final String htmlPath = "data/html/mods/LMEvent/";

	/**
	 * @param objectId
	 * @param template
	 */
	public L2LMEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2LMEventNpcInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		LMEvent.onBypass(command, playerInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance activeChar, int val)
	{
		if (activeChar == null)
			return;

		if (LMEvent.isParticipating())
		{
			final boolean isParticipant = LMEvent.isPlayerParticipant(activeChar.getObjectId()); 
			final String htmContent;

			if (!isParticipant)
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Participation.htm");
			else
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "RemoveParticipation.htm");

	    	if (htmContent != null)
	    	{
	    		int PlayerCounts = LMEvent.getPlayerCounts();
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
	    		npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%playercount%", String.valueOf(PlayerCounts));
				if (!isParticipant)
					npcHtmlMessage.replace("%fee%", LMEvent.getParticipationFee());

				activeChar.sendPacket(npcHtmlMessage);
	    	}
		}
		else if (LMEvent.isStarting() || LMEvent.isStarted())
		{
			final String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Status.htm");

	    	if (htmContent != null)
	    	{
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
	    		String htmltext = "";
	    		htmltext = String.valueOf(LMEvent.getPlayerCounts());
				npcHtmlMessage.setHtml(htmContent);
				npcHtmlMessage.replace("%countplayer%", htmltext);
				activeChar.sendPacket(npcHtmlMessage);
	    	}
		}

		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
