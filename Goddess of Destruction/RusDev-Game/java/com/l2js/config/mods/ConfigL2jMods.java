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

import java.util.ArrayList;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigL2jMods extends Config
{
	private final static String path = L2JS_L2J_MODS_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ENABLE_WAREHOUSESORTING_CLAN = getBoolean(properties, "EnableWarehouseSortingClan", false);
			ENABLE_WAREHOUSESORTING_PRIVATE = getBoolean(properties, "EnableWarehouseSortingPrivate", false);
			DISPLAY_SERVER_TIME = getBoolean(properties, "DisplayServerTime", false);
			MULTILANG_ENABLE = getBoolean(properties, "MultiLangEnable", false);
			String[] allowed = getString(properties, "MultiLangAllowed", "en").split(";");
			MULTILANG_ALLOWED = new ArrayList<String>(allowed.length);
			for (String lang : allowed)
				MULTILANG_ALLOWED.add(lang);
			MULTILANG_DEFAULT = getString(properties, "MultiLangDefault", "en");
			if (!MULTILANG_ALLOWED.contains(MULTILANG_DEFAULT))
				_log.warning("MultiLang[load()]: default language: " + MULTILANG_DEFAULT + " is not in allowed list !");
			MULTILANG_VOICED_ALLOW = getBoolean(properties, "MultiLangVoiceCommand", true);
			MULTILANG_SM_ENABLE = getBoolean(properties, "MultiLangSystemMessageEnable", false);
			allowed = properties.getProperty("MultiLangSystemMessageAllowed", "").split(";");
			MULTILANG_SM_ALLOWED = new ArrayList<String>(allowed.length);
			for (String lang : allowed)
			{
				if (!lang.isEmpty())
					MULTILANG_SM_ALLOWED.add(lang);
			}
			DEBUG_VOICE_COMMAND = getBoolean(properties, "DebugVoiceCommand", false);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
