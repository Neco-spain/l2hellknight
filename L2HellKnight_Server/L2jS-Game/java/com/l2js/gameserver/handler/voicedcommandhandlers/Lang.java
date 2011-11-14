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
package com.l2js.gameserver.handler.voicedcommandhandlers;

import java.util.StringTokenizer;

import com.l2js.Config;
import com.l2js.gameserver.handler.IVoicedCommandHandler;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2js.util.StringUtil;

public class Lang implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"lang"
	};
	
	/**
	 * 
	 * @see com.l2js.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, com.l2js.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (!Config.MULTILANG_ENABLE
				|| !Config.MULTILANG_VOICED_ALLOW)
			return false;
		
		NpcHtmlMessage msg = new NpcHtmlMessage(1);
		
		if (params == null)
		{
			final StringBuilder html = StringUtil.startAppend(100);
			for (String lang : Config.MULTILANG_ALLOWED)
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
	 * @see com.l2js.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}