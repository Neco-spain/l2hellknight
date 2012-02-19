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
package handlers.chathandlers;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2.brick.Config;
import l2.brick.gameserver.handler.IChatHandler;
import l2.brick.gameserver.handler.IVoicedCommandHandler;
import l2.brick.gameserver.handler.VoicedCommandHandler;
import l2.brick.gameserver.model.BlockList;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.CreatureSay;


/**
 * A chat handler
 *
 * @author  durgus
 */
public class ChatAll implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	private static Logger _log = Logger.getLogger(ChatAll.class.getName());
	
	/**
	 * Handle chat type 'all'
	 * @see l2.brick.gameserver.handler.IChatHandler#handleChat(int, l2.brick.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public void handleChat(int type, L2PcInstance activeChar, String params, String text)
	{
		boolean vcd_used = false;
		if (text.startsWith("."))
		{
			StringTokenizer st = new StringTokenizer(text);
			IVoicedCommandHandler vch;
			String command = "";
			
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				params = text.substring(command.length() + 2);
				vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			else
			{
				command = text.substring(1);
				if (Config.DEBUG)
					_log.info("Command: " + command);
				vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			if (vch != null)
			{
				vch.useVoicedCommand(command, activeChar, params);
				vcd_used = true;
			}
			else
			{
				if (Config.DEBUG)
					_log.warning("No handler registered for bypass '" + command + "'");
				vcd_used = false;
			}
		}
		if (!vcd_used)
		{
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);
			
			Collection<L2PcInstance> plrs = activeChar.getKnownList().getKnownPlayers().values();
			//synchronized (activeChar.getKnownList().getKnownPlayers())
			{
				for (L2PcInstance player : plrs)
				{
					if (player != null && activeChar.isInsideRadius(player, 1250, false, true) && !BlockList.isBlocked(player, activeChar))
						player.sendPacket(cs);
				}
			}
			
			activeChar.sendPacket(cs);
		}
	}
	
	/**
	 * Returns the chat types registered to this handler
	 * @see l2.brick.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}