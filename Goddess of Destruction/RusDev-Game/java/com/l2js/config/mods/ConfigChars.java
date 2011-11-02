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
public class ConfigChars extends Config
{
	private final static String path = L2JS_CHARS_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			FAKE_PLAYERS = getInt(properties, "FakePlayers", 0);
			ALLOW_NEW_CHARACTER_TITLE = getBoolean(properties, "AllowNewCharacterTitle", false);
			NEW_CHARACTER_TITLE = getString(properties, "NewCharacterTitle", "Newbie");
			CHARACTER_COLOR_NAME = getIntDecode(properties, "CharacterColorName", "FFFFFF");
			CHARACTER_COLOR_TITLE = getIntDecode(properties, "CharacterColorTitle", "FFFF77");
			NO_STORE_ZONES_AROUND_NPCS_RADIUS = getInt(properties, "NoStoreZonesAroundNpcsRadius", 0);
			NO_STORE_ZONES_AROUND_PCS_RADIUS = getInt(properties, "NoStoreZonesAroundPcsRadius", 0);
			DONATOR_SEE_NAME_COLOR = getBoolean(properties, "DonatorSeeColorName", false);
			DONATOR_NAME_COLOR = getIntDecode(properties, "DonatorNameColor", "00CCFF");
			DONATOR_SEE_TITLE_COLOR = getBoolean(properties, "DonatorSeeColorTitle", false);
			DONATOR_TITLE_COLOR = getIntDecode(properties, "DonatorTitleColor", "00CCFF");
			DONATOR_WELCOME_MESSAGE = getString(properties, "DonatorWelcomeMessage",
					"Welcome back to our server! Enjoy your stay!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
