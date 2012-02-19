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

import java.util.Iterator;

import l2.brick.gameserver.handler.IVoicedCommandHandler;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.util.StringUtil;


/**
 *
 *
 */
public class stats implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"stats"
	};
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, l2.brick.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("stats"))
		{
			L2PcInstance pc = L2World.getInstance().getPlayer(params);
			if (pc != null)
			{
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				final StringBuilder replyMSG = StringUtil.startAppend(
						300 + pc.kills.size() * 50,
						"<html><body>" +
						"<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br>" +
						"<br>Statistics for player <font color=\"LEVEL\">",
						pc.getName(),
						"</font><br>" +
						"Total kills <font color=\"FF0000\">",
						String.valueOf(pc.kills.size()),
						"</font><br>" +
						"<br>Detailed list: <br>"
				);
				
				Iterator<String> it = pc.kills.iterator();
				
				while (it.hasNext()) {
					StringUtil.append(replyMSG,
							"<font color=\"FF0000\">",
							it.next(),
					"</font><br>");
				}
				
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			
		}
		return true;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}
