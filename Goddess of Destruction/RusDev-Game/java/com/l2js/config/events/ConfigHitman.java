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

import java.util.ArrayList;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigHitman extends Config
{
	private final static String path = L2JS_HITMAN_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			HITMAN_ENABLE_EVENT = getBoolean(properties, "EnableHitmanEvent", false);
			HITMAN_TAKE_KARMA = getBoolean(properties, "HitmansTakekarma", true);
			HITMAN_ANNOUNCE = getBoolean(properties, "HitmanAnnounce", false);
			HITMAN_MAX_PER_PAGE = getInt(properties, "HitmanMaxPerPage", 20);
			String[] split = getString(properties, "HitmanCurrency", "57,5575,3470").split(",");
			HITMAN_CURRENCY = new ArrayList<Integer>();
			for (String id : split)
			{
				try
				{
					Integer itemId = Integer.parseInt(id);
					HITMAN_CURRENCY.add(itemId);
				}
				catch (Exception e)
				{
					_log.info("Wrong config item id: " + id + ". Skipped.");
				}
			}
			HITMAN_SAME_TEAM = getBoolean(properties, "HitmanSameTeam", false);
			HITMAN_SAVE_TARGET = getInt(properties, "HitmanSaveTarget", 15);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
