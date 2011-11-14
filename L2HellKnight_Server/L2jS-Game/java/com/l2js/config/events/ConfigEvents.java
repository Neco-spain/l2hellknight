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
package com.l2js.config.events;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0tn
 */
public class ConfigEvents extends Config
{
	private final static String path = L2JS_EVENTS_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ENABLE_BLOCK_CHECKER_EVENT = getBoolean(properties, "EnableBlockCheckerEvent", true);
			MIN_BLOCK_CHECKER_TEAM_MEMBERS = getInt(properties, "BlockCheckerMinTeamMembers", 2);
			if (MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1)
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
			else if (MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6)
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
			HBCE_FAIR_PLAY = getBoolean(properties, "HBCEFairPlay", true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
