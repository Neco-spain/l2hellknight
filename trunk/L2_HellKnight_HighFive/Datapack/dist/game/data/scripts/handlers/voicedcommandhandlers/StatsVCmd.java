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

import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.L2Event;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.util.StringUtil;

/**
 * @author Zoey76.
 */
public class StatsVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"stats"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (!command.equals("stats") || (params == null) || params.isEmpty())
		{
			activeChar.sendMessage("Syntax: .stats <player name>");
			return false;
		}
		
		final L2PcInstance pc = L2World.getInstance().getPlayer(params);
		if ((pc == null))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return false;
		}
		
		if (pc.getClient().isDetached())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_OFFLINE);
			sm.addPcName(pc);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (!L2Event.isParticipant(pc) || (pc.getEventStatus() == null))
		{
			activeChar.sendMessage("That player is not an event participant.");
			return false;
		}
		
		final StringBuilder replyMSG = StringUtil.startAppend(300 + (pc.getEventStatus().kills.size() * 50), "<html><body>" + "<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br><br>Statistics for player <font color=\"LEVEL\">", pc.getName(), "</font><br>Total kills <font color=\"FF0000\">", String.valueOf(pc.getEventStatus().kills.size()), "</font><br><br>Detailed list: <br>");
		for (L2PcInstance plr : pc.getEventStatus().kills)
		{
			StringUtil.append(replyMSG, "<font color=\"FF0000\">", plr.getName(), "</font><br>");
		}
		replyMSG.append("</body></html>");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
