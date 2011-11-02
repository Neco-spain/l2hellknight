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
package com.l2js.config.mods;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigChat extends Config
{
	private final static String path = L2JS_CHAT_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			CHAT_ADMIN = getBoolean(properties, "ChatAdmin", false);
			ENABLE_PVP_CHAT_SHOUT_BLOCK = getBoolean(properties, "EnablePvPChatShoutBlock", false);
			AMOUNT_PVP_CHAT_SHOUT = getInt(properties, "AmountPvPChatShout", 100);
			PUNISHED_AMOUNT_PVP_CHAT_SHOUT = getInt(properties, "PunishedAmountPvPChatShout", 0);
			ENABLE_PVP_CHAT_TRADE_BLOCK = getBoolean(properties, "EnablePvPChatTradeBlock", false);
			AMOUNT_PVP_CHAT_TRADE = getInt(properties, "AmountPvPChatTrade", 100);
			PUNISHED_AMOUNT_PVP_CHAT_TRADE = getInt(properties, "PunishedAmountPvPChatTrade", 0);
			ENABLE_PVP_CHAT_HERO_BLOCK = getBoolean(properties, "EnablePvPChatHeroBlock", false);
			AMOUNT_PVP_CHAT_HERO = getInt(properties, "AmountPvPChatHero", 100);
			PUNISHED_AMOUNT_PVP_CHAT_HERO = getInt(properties, "PunishedAmountPvPChatHero", 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
