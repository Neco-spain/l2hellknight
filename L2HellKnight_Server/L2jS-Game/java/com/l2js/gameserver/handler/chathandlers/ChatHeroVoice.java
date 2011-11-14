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
package com.l2js.gameserver.handler.chathandlers;

import com.l2js.Config;
import com.l2js.gameserver.handler.IChatHandler;
import com.l2js.gameserver.model.BlockList;
import com.l2js.gameserver.model.L2World;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.serverpackets.CharInfo;
import com.l2js.gameserver.network.serverpackets.CreatureSay;
import com.l2js.gameserver.network.serverpackets.SystemMessage;
import com.l2js.gameserver.network.serverpackets.UserInfo;
import com.l2js.gameserver.util.Util;

/**
 * Hero chat handler.
 * 
 * @author durgus
 */
public class ChatHeroVoice implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		17
	};

	/**
	 * Handle chat type 'hero voice'
	 * 
	 * @see com.l2js.gameserver.handler.IChatHandler#handleChat(int,
	 *      com.l2js.gameserver.model.actor.instance.L2PcInstance,
	 *      java.lang.String)
	 */
	@Override
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if ((isValid(activeChar) && activeChar.isHero()) || activeChar.isGM())
		{
			if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type))
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED));
				return;
			}
			
			if (!activeChar.getFloodProtectors().getHeroVoice().tryPerformAction("hero voice"))
			{
				activeChar
						.sendMessage("Action failed. Heroes are only able to speak in the global channel once every 10 seconds.");
				return;
			}
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);

			for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
			{
				if (player != null && !BlockList.isBlocked(player, activeChar))
					player.sendPacket(cs);
			}

			if (!isValidPunished(activeChar) && !activeChar.isGM())
			{
				activeChar.sendMessage("You were punished for using this chat. (-"
						+ String.valueOf(Config.PUNISHED_AMOUNT_PVP_CHAT_HERO) + ")");
				activeChar.setPvpKills(activeChar.getPvpKills() - Config.PUNISHED_AMOUNT_PVP_CHAT_HERO);
				activeChar.store();
				activeChar.broadcastPacket(new CharInfo(activeChar));
				activeChar.sendPacket(new UserInfo(activeChar));
			}
		}
		else
			activeChar.sendMessage("You do not have PvP Kills enough to use this Chat.");
	}

	public Boolean isValid(L2PcInstance activeChar)
	{
		if (Config.ENABLE_PVP_CHAT_HERO_BLOCK && activeChar.getPvpKills() < Config.AMOUNT_PVP_CHAT_HERO)
			return false;
		return true;

	}

	public Boolean isValidPunished(L2PcInstance activeChar)
	{
		if (Config.ENABLE_PVP_CHAT_HERO_BLOCK && Config.PUNISHED_AMOUNT_PVP_CHAT_HERO > 0
				&& Config.AMOUNT_PVP_CHAT_HERO > Config.PUNISHED_AMOUNT_PVP_CHAT_HERO)
			return false;
		return true;
	}

	/**
	 * Returns the chat types registered to this handler
	 * 
	 * @see com.l2js.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
