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

import java.util.StringTokenizer;

import l2.brick.Config;
import l2.brick.gameserver.handler.IVoicedCommandHandler;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.util.StringUtil;

public class Lang implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"lang"
	};
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, l2.brick.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (!Config.L2JMOD_MULTILANG_ENABLE
				|| !Config.L2JMOD_MULTILANG_VOICED_ALLOW)
			return false;
		
		NpcHtmlMessage msg = new NpcHtmlMessage(1);
		
		if (params == null)
		{
			final StringBuilder html = StringUtil.startAppend(100);
			for (String lang : Config.L2JMOD_MULTILANG_ALLOWED)
			{
				StringUtil.append(html,
						"<button value=\"",
						lang.toUpperCase(),
						"\" action=\"bypass -h voice .lang ",
						lang,
						"\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br>"
				);
			}
			
			msg.setFile(activeChar.getHtmlPrefix(), "data/html/mods/Lang/LanguageSelect.htm");
			msg.replace("%list%", html.toString());
			activeChar.sendPacket(msg);
			return true;
		}
		
		StringTokenizer st = new StringTokenizer(params);
		if (st.hasMoreTokens())
		{
			final String lang = st.nextToken().trim();
			if (activeChar.setLang(lang))
			{
				msg.setFile(activeChar.getHtmlPrefix(), "data/html/mods/Lang/Ok.htm");
				activeChar.sendPacket(msg);
				return true;
			}
			else
			{
				msg.setFile(activeChar.getHtmlPrefix(), "data/html/mods/Lang/Error.htm");
				activeChar.sendPacket(msg);
			}
		}
		
		return false;
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