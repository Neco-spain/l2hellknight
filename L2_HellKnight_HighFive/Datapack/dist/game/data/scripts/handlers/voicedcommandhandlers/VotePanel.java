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

import javolution.text.TextBuilder;


import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.handler.IVoicedCommandHandler;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class VotePanel implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
		"getreward"
	};

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if(command.equalsIgnoreCase("getreward"))
		{
			       TextBuilder tb = new TextBuilder();
			       NpcHtmlMessage html = new NpcHtmlMessage(1);
			               
			       tb.append("<html><head><title>User Control Panel</title></head><body>");
			       tb.append("<center>");
			       tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
			       tb.append("<tr>");
			       tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
			       tb.append("<td valign=\"top\"><font color=\"FF6600\">Vote Panel</font>");  
			       tb.append("<br1><font color=\"00FF00\">"+activeChar.getName()+"</font>, use this menu to Vote for our server.</td>");
			       tb.append("</tr>");
			       tb.append("</table>");
			       tb.append("</center>");
			       tb.append("<center>");
			       tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32 align=center><br>");
			       tb.append("</center>");
			       tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
			       tb.append("<tr>");
			       tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><button action=\"bypass -h votehopzone\" width=32 height=32 back=\"L2ui_ch3.mainwndtabicon2_over\" fore=\"L2ui_ch3.mainwndtabicon2\"></td>");
			       tb.append("<td valign=\"top\"><font color=\"FF6600\">Hopzone</font>");
			       tb.append("<br1>Vote for us at Hopzone.</td>");
                   tb.append("</tr>");
			       tb.append("</table>");
			       tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
			       tb.append("<tr>");
			       tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><button action=\"bypass -h votetopzone\" width=32 height=32 back=\"L2ui_ch3.mainwndtabicon2_over\" fore=\"L2ui_ch3.mainwndtabicon2\"></td>");
			       tb.append("<td valign=\"top\"><font color=\"FF6600\">Topzone</font>");
			       tb.append("<br1>Vote for us at Topzone.</td>");
                   tb.append("</tr>");
			       tb.append("</table>");
			       tb.append("<center>");
			       tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32 align=center>");
			       tb.append("<font color=\"FF6600\">By Elfocrash For L2Nexus</font>");  
			       tb.append("</center>");
			       tb.append("</body></html>");
			               
			       html.setHtml(tb.toString());
			       activeChar.sendPacket(html);
			
		}
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
