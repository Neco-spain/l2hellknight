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

package handlers.bypasshandlers;

import l2.hellknight.Config;
import l2.hellknight.gameserver.handler.IBypassHandler;
import l2.hellknight.gameserver.instancemanager.leaderboards.ArenaLeaderboard;
import l2.hellknight.gameserver.instancemanager.leaderboards.CraftLeaderboard;
import l2.hellknight.gameserver.instancemanager.leaderboards.FishermanLeaderboard;
import l2.hellknight.gameserver.instancemanager.leaderboards.TvTLeaderboard;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class Leaderboard implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"rank_arena_info",
		"rank_fisherman_info",
		"rank_craft_info",
		"rank_tvt_info"
	};

	/**
	 * @author Gnacik
	 */

	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		NpcHtmlMessage html = new NpcHtmlMessage(((L2Npc)target).getObjectId());
		
		if (command.toLowerCase().startsWith(COMMANDS[0]) && Config.RANK_ARENA_ENABLED)
		{
			html.setHtml(ArenaLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		else if (command.toLowerCase().startsWith(COMMANDS[1]) && Config.RANK_FISHERMAN_ENABLED)
		{
			html.setHtml(FishermanLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		else if (command.toLowerCase().startsWith(COMMANDS[2]) && Config.RANK_CRAFT_ENABLED)
		{
			html.setHtml(CraftLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		else if (command.toLowerCase().startsWith(COMMANDS[3]) && Config.RANK_TVT_ENABLED)
		{
			html.setHtml(TvTLeaderboard.getInstance().showHtm(activeChar.getObjectId()));
			activeChar.sendPacket(html);
		}
		return false;
	}

	public String[] getBypassList()
	{
		return COMMANDS;
	}
}