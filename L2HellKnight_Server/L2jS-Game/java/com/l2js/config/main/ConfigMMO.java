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
public class ConfigMMO extends Config
{
	private final static String path = MMO_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			MMO_SELECTOR_SLEEP_TIME = getInt(properties, "SleepTime", 20);
			MMO_MAX_SEND_PER_PASS = getInt(properties, "MaxSendPerPass", 12);
			MMO_MAX_READ_PER_PASS = getInt(properties, "MaxReadPerPass", 12);
			MMO_HELPER_BUFFER_COUNT = getInt(properties, "HelperBufferCount", 20);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
