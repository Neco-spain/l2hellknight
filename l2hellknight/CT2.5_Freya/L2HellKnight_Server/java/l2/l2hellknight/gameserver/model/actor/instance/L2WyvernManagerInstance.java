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

import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.entity.ClanHall;
import l2.hellknight.gameserver.model.entity.clanhall.SiegableHall;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2Npc
{
	public L2WyvernManagerInstance (int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2WyvernManagerInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket( ActionFailed.STATIC_PACKET );
		String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
		
		if (isOwnerClan(player))
			filename = "data/html/wyvernmanager/wyvernmanager.htm";      // Owner message window
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	public boolean isOwnerClan(L2PcInstance player)
	{
		L2Clan clan = player.getClan();
		if(clan != null)
		{
			ClanHall hall = getConquerableHall();
			if(hall != null)
				return hall.getOwnerId() == clan.getClanId();
		}
		return false;
	}
	
	public boolean isInSiege()
	{
		SiegableHall hall = getConquerableHall();
		if(hall != null)
			return hall.isInSiege();
		return getCastle().getSiege().getIsInProgress();
	}
}
