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
 * @since 19/09/2011
 */
public class ConfigConquerableHallSiege extends Config
{
	private final static String path = CH_SIEGE_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			
			CHS_MAX_ATTACKERS = getInt(properties, "MaxAttackers", 500);
			CHS_CLAN_MINLEVEL = getInt(properties, "MinClanLevel", 4);
			CHS_MAX_FLAGS_PER_CLAN = getInt(properties, "MaxFlagsPerClan", 1);
			CHS_ENABLE_FAME = getBoolean(properties, "EnableFame", false);
			CHS_FAME_AMOUNT = getInt(properties, "FameAmount", 0);
			CHS_FAME_FREQUENCY = getInt(properties, "FameFrequency", 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
