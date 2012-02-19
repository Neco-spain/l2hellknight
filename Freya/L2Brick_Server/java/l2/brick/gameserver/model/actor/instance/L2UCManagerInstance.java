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

import java.util.StringTokenizer;

import l2.brick.Config;
import l2.brick.gameserver.instancemanager.UndergroundColiseumManager;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.entity.underground_coliseum.UCArena;
import l2.brick.gameserver.model.entity.underground_coliseum.UCPoint;
import l2.brick.gameserver.model.entity.underground_coliseum.UCTeam;
import l2.brick.gameserver.model.entity.underground_coliseum.UCWaiting;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.templates.L2NpcTemplate;

public class L2UCManagerInstance extends L2Npc
{
	public L2UCManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer token = new StringTokenizer(command, " ");
		String actualCommand = token.nextToken();
		
		if (actualCommand.equalsIgnoreCase("register"))
		{
			try
			{
				if (!player.isInParty())
				{
					showChatWindow(player, "data/html/underground_coliseum/noTeam.htm");
					return;
				}
				
				if (!UndergroundColiseumManager.getInstance().isStarted())
				{
					showChatWindow(player, "data/html/underground_coliseum/notStarted.htm");
					return;
				}
				
				if (!player.getParty().isLeader(player))
				{
					showChatWindow(player, "data/html/underground_coliseum/notPartyLeader.htm");
					return;
				}
				
				if (player.getParty().getUCState() instanceof UCWaiting)
				{
					showChatWindow(player, "data/html/underground_coliseum/alreadyRegistered.htm");
					return;
				}
				
				int val = Integer.parseInt(token.nextToken());
				UCArena arena = UndergroundColiseumManager.getInstance().getArena(val);
				if (arena == null)
				{
					player.sendMessage("This arena is temporarly unavailable.");
					return;
				}
				
				int realCount = 0;
				
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					if (member == null)
						continue;
					
					if (!(member.getLevel() >= arena.getMinLevel() && member.getLevel() <= arena.getMaxLevel()))
					{
						NpcHtmlMessage packet = new NpcHtmlMessage(getObjectId());
						packet.setFile(member.getHtmlPrefix(), "data/html/underground_coliseum/wrongLevel.htm");
						packet.replace("%name%", member.getName());
						player.sendPacket(packet);
						return;
					}
					else
						realCount++;
				}
				
				if (realCount < Config.UC_PARTY_LIMIT)
				{
					showChatWindow(player, "data/html/underground_coliseum/notEnoughMembers.htm");
					return;
				}
				
				if (arena.getWaitingList().size() >= 5)
				{
					showChatWindow(player, "data/html/underground_coliseum/arenaFull.htm");
					return;
				}
				
				UCWaiting waiting = new UCWaiting(player.getParty(), arena);
				arena.getWaitingList().add(waiting);
				waiting.hasRegisterdNow();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (actualCommand.equalsIgnoreCase("cancel"))
		{
			if (player.getParty() == null || (player.getParty() != null && !player.getParty().isLeader(player)))
				return;
			
			if (player.getParty().getUCState() instanceof UCWaiting)
			{
				UCWaiting waiting = (UCWaiting) player.getParty().getUCState();
				
				waiting.setParty(null);
				waiting.clean();
				waiting.getBaseArena().getWaitingList().remove(waiting);
				showChatWindow(player, "data/html/underground_coliseum/registrantionCanceled.htm");
			}
		}
		else if (actualCommand.equalsIgnoreCase("bestTeam"))
		{
			int val = Integer.parseInt(token.nextToken());
			UCArena arena = UndergroundColiseumManager.getInstance().getArena(val);
			int top = 0;
			String name = "None";
			for (UCTeam team : arena.getTeams())
			{
				if (team == null)
					continue;
				
				if (top < team.getConsecutiveWins())
				{
					top = team.getConsecutiveWins();
					name = team.getParty().getLeader().getName();
				}
			}
			if (top > 1)
			{
				NpcHtmlMessage packet = new NpcHtmlMessage(getObjectId());
				packet.setFile(player.getHtmlPrefix(), "data/html/underground_coliseum/bestTeam.htm");
				packet.replace("%name%", name);
				packet.replace("%best%", String.valueOf(top));
				player.sendPacket(packet);
			}
			else
				showChatWindow(player, "data/html/underground_coliseum/view-most-wins.htm");
		}
		else if (actualCommand.equalsIgnoreCase("listTeams"))
		{
			int val = Integer.parseInt(token.nextToken());
			
			UCArena arena = UndergroundColiseumManager.getInstance().getArena(val);
			if (arena == null)
			{
				player.sendMessage("This arena is temporarly unavailable.");
				return;
			}
			
			NpcHtmlMessage packet = new NpcHtmlMessage(getObjectId());
			packet.setFile(player.getHtmlPrefix(), "data/html/underground_coliseum/view-participating-teams.htm");
			
			String list = "";
			int i = 1;
			for (UCPoint point : arena.getPoints())
			{
				if (point.getParty() == null)
					list += i + ". Free <br>";
				else
				{
					String teamList = "";
					for (L2PcInstance m : point.getParty().getPartyMembers())
					{
						if (m != null)
							teamList += m.getName() + ";";
					}
					
					list += i + ". (Participating Team: <font color=00ffff>" + teamList + "</font>)<br>";
				}
				i++;
			}
			
			packet.replace("%list%", list);
			player.sendPacket(packet);
		}
		else
			super.onBypassFeedback(player, command);
	}
}