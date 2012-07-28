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
package handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;

import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.cache.HtmCache;
import l2.brick.gameserver.handler.IAdminCommandHandler;
import l2.brick.gameserver.instancemanager.CastleManager;
import l2.brick.gameserver.instancemanager.ClanHallManager;
import l2.brick.gameserver.instancemanager.FortManager;
import l2.brick.gameserver.instancemanager.SiegeManager;
import l2.brick.gameserver.model.L2Clan;
import l2.brick.gameserver.model.L2ClanMember;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.communityserver.CommunityServerThread;
import l2.brick.gameserver.network.communityserver.writepackets.WorldInfo;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.util.Util;

/**
 * @author UnAfraid, Zoey76
 */
public class AdminClan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_clan_info", "admin_clan_changeleader"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String cmd = st.nextToken();
		if (cmd.startsWith("admin_clan_info"))
		{
			String val;
			L2PcInstance player = null;
			if (st.hasMoreTokens())
			{
				val = st.nextToken();
				// From the HTML we receive player's object Id.
				if (Util.isDigit(val))
				{
					player = L2World.getInstance().getPlayer(Integer.parseInt(val));
					if (player == null)
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
						return false;
					}
				}
				else
				{
					player = L2World.getInstance().getPlayer(val);
					if (player == null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
				}
			}
			else
			{
				L2Object targetObj = activeChar.getTarget();
				if (targetObj instanceof L2PcInstance)
				{
					player = targetObj.getActingPlayer();
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
			}
			
			final L2Clan clan = player.getClan();
			if (clan == null)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
				return false;
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			final String htm = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/admin/claninfo.htm");
			html.setHtml(htm.toString());
			html.replace("%clan_name%", clan.getName());
			html.replace("%clan_leader%", clan.getLeaderName());
			html.replace("%clan_level%", String.valueOf(clan.getLevel()));
			html.replace("%clan_has_castle%", clan.getHasCastle() > 0 ? CastleManager.getInstance().getCastleById(clan.getHasCastle()).getName() : "No");
			html.replace("%clan_has_clanhall%", clan.getHasHideout() > 0 ? ClanHallManager.getInstance().getClanHallById(clan.getHasHideout()).getName() : "No");
			html.replace("%clan_has_fortress%", clan.getHasFort() > 0 ? FortManager.getInstance().getFortById(clan.getHasFort()).getName() : "No");
			html.replace("%clan_points%", String.valueOf(clan.getReputationScore()));
			html.replace("%clan_players_count%", String.valueOf(clan.getMembersCount()));
			html.replace("%clan_ally%", clan.getAllyId() > 0 ? clan.getAllyName() : "Not in ally");
			html.replace("%current_player_objectId%", String.valueOf(player.getObjectId()));
			html.replace("%current_player_name%", player.getName());
			activeChar.sendPacket(html);
		}
		else if (cmd.startsWith("admin_clan_changeleader"))
		{
			String val;
			L2PcInstance player = null;
			if (st.hasMoreTokens())
			{
				val = st.nextToken();
				// From the HTML we receive player's object Id.
				if (Util.isDigit(val))
				{
					player = L2World.getInstance().getPlayer(Integer.parseInt(val));
					if (player == null)
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
						return false;
					}
				}
				else
				{
					player = L2World.getInstance().getPlayer(val);
					if (player == null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
				}
			}
			else
			{
				L2Object targetObj = activeChar.getTarget();
				if (targetObj instanceof L2PcInstance)
				{
					player = targetObj.getActingPlayer();
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
			}
			
			final L2Clan clan = player.getClan();
			if (clan == null)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
				return false;
			}
			
			final L2ClanMember member = clan.getClanMember(player.getObjectId());
			if (member != null)
			{
				if ((clan.getLeader() != null) && (clan.getLeader().getPlayerInstance() != null))
				{
					final L2PcInstance exLeader = clan.getLeader().getPlayerInstance();
					SiegeManager.getInstance().removeSiegeSkills(exLeader);
					exLeader.setClan(clan);
					exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
					exLeader.broadcastUserInfo();
					exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
					exLeader.broadcastUserInfo();
					exLeader.checkItemRestriction();
				}
				else if (clan.getLeaderId() > 0)
				{
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement = con.prepareStatement("UPDATE characters SET clan_privs = ? WHERE charId = ?");
						statement.setInt(1, L2Clan.CP_NOTHING);
						statement.setInt(2, clan.getLeaderId());
						statement.execute();
						
						if (statement.getUpdateCount() == 0)
						{
							activeChar.sendPacket(SystemMessageId.ID_NOT_EXIST);
						}
						statement.close();
					}
					catch (Exception e)
					{
						activeChar.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
					}
					finally
					{
						L2DatabaseFactory.close(con);
					}
				}
				
				clan.setLeader(member);
				clan.updateClanInDB();
				
				player.setClan(clan);
				player.setPledgeClass(member.calculatePledgeClass(player));
				player.setClanPrivileges(L2Clan.CP_ALL);
				
				if (clan.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
				{
					SiegeManager.getInstance().addSiegeSkills(player);
				}
				
				player.broadcastUserInfo();
				clan.broadcastClanStatus();
				
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1);
				sm.addString(player.getName());
				clan.broadcastToOnlineMembers(sm);
				activeChar.sendPacket(sm);
				CommunityServerThread.getInstance().sendPacket(new WorldInfo(null, clan, WorldInfo.TYPE_UPDATE_CLAN_DATA));
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
