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
package com.l2js.config.main;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigSiege extends Config
{
	private final static String path = SIEGE_CONFIGURATION_FILE;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			GLUDIO_MAX_MERCENARIES = getInt(properties, "GludioMaxMercenaries", 100);
			DION_MAX_MERCENARIES = getInt(properties, "DionMaxMercenaries", 150);
			GIRAN_MAX_MERCENARIES = getInt(properties, "GiranMaxMercenaries", 200);
			OREN_MAX_MERCENARIES = getInt(properties, "OrenMaxMercenaries", 300);
			ADEN_MAX_MERCENARIES = getInt(properties, "AdenMaxMercenaries", 400);
			INNADRIL_MAX_MERCENARIES = getInt(properties, "InnadrilMaxMercenaries", 400);
			GODDARD_MAX_MERCENARIES = getInt(properties, "GoddardMaxMercenaries", 400);
			RUNE_MAX_MERCENARIES = getInt(properties, "RuneMaxMercenaries", 400);
			SCHUTTGART_MAX_MERCENARIES = getInt(properties, "SchuttgartMaxMercenaries", 400);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
