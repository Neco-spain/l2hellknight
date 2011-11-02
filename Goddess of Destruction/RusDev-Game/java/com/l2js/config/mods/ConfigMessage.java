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
public class ConfigMessage extends Config
{
	private final static String path = L2JS_MESSAGE_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			SERVER_WELCOME_MESSAGE_ENABLE = getBoolean(properties, "ServerWelcomeMessageEnable", false);
			SERVER_WELCOME_MESSAGE = getString(properties, "ServerWelcomeMessage", "Welcome to my Server!");
			SCREEN_WELCOME_MESSAGE_ENABLE = getBoolean(properties, "ScreenWelcomeMessageEnable", false);
			SCREEN_WELCOME_MESSAGE = getString(properties, "ScreenWelcomeMessage", "Welcome to my Server!");
			SCREEN_WELCOME_MESSAGE_TIME = getInt(properties, "ScreenWelcomeMessageTime", 5) * 1000;
			ONLINE_PLAYERS_AT_STARTUP = getBoolean(properties, "OnlinePlayersAtStartup", true);
			ANNOUNCE_PK_PVP = getBoolean(properties, "AnnouncePkPvP", false);
			ANNOUNCE_PK_PVP_NORMAL_MESSAGE = getBoolean(properties, "AnnouncePkPvPNormalMessage", true);
			ANNOUNCE_PK_MSG = getString(properties, "AnnouncePkMsg", "$killer has slaughtered $target");
			ANNOUNCE_PVP_MSG = getString(properties, "AnnouncePvpMsg", "$killer has defeated $target");
			HERO_ANNOUNCE_LOGIN = getBoolean(properties, "HeroAnnounceLogin", true);
			HERO_MSG_LOGIN = getString(properties, "HeroMsgLogin", "Hero: $player has been logged in.");
			CASTLE_LORDS_ANNOUNCE = getBoolean(properties, "CastleLordsAnnounce", true);
			CASTLE_LORDS_MSG = getString(properties, "CastleLordsMsg",
					"Castle Lord $player Of $castle Castle is Currently Online.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
