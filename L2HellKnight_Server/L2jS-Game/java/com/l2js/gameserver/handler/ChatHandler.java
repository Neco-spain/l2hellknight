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
package com.l2js.gameserver.handler;

import gnu.trove.TIntObjectHashMap;

import java.util.logging.Logger;

import com.l2js.Config;
import com.l2js.gameserver.handler.chathandlers.*;

/**
 * This class handles all chat handlers
 * 
 * @author durgus
 */
public class ChatHandler
{
	private static Logger _log = Logger.getLogger(ChatHandler.class.getName());

	private TIntObjectHashMap<IChatHandler> _datatable;

	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Singleton constructor
	 */
	private ChatHandler()
	{
		_datatable = new TIntObjectHashMap<IChatHandler>();
		registerChatHandler(new ChatAll());
		registerChatHandler(new ChatAlliance());
		registerChatHandler(new ChatBattlefield());
		registerChatHandler(new ChatClan());
		registerChatHandler(new ChatHeroVoice());
		registerChatHandler(new ChatParty());
		registerChatHandler(new ChatPartyMatchRoom());
		registerChatHandler(new ChatPartyRoomAll());
		registerChatHandler(new ChatPartyRoomCommander());
		registerChatHandler(new ChatPetition());
		registerChatHandler(new ChatShout());
		registerChatHandler(new ChatTell());
		registerChatHandler(new ChatTrade());
		_log.info("Loaded " + size() + "  ChatHandlers");
	}

	/**
	 * Register a new chat handler
	 * 
	 * @param handler
	 */
	public void registerChatHandler(IChatHandler handler)
	{
		int[] ids = handler.getChatTypeList();
		for (int i = 0; i < ids.length; i++)
		{
			if (Config.DEBUG)
				_log.fine("Adding handler for chat type " + ids[i]);
			_datatable.put(ids[i], handler);
		}
	}

	/**
	 * Get the chat handler for the given chat type
	 * 
	 * @param chatType
	 * @return
	 */
	public IChatHandler getChatHandler(int chatType)
	{
		return _datatable.get(chatType);
	}

	/**
	 * Returns the size
	 * 
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}