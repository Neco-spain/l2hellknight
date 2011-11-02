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
public class ConfigWedding extends Config
{
	private final static String path = L2JS_WEDDING_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ALLOW_WEDDING = getBoolean(properties, "AllowWedding", false);
			WEDDING_PRICE = getInt(properties, "WeddingPrice", 250000000);
			WEDDING_PUNISH_INFIDELITY = getBoolean(properties, "WeddingPunishInfidelity", true);
			WEDDING_TELEPORT = getBoolean(properties, "WeddingTeleport", true);
			WEDDING_TELEPORT_PRICE = getInt(properties, "WeddingTeleportPrice", 50000);
			WEDDING_TELEPORT_DURATION = getInt(properties, "WeddingTeleportDuration", 60);
			WEDDING_SAMESEX = getBoolean(properties, "WeddingAllowSameSex", false);
			WEDDING_FORMALWEAR = getBoolean(properties, "WeddingFormalWear", true);
			WEDDING_DIVORCE_COSTS = getInt(properties, "WeddingDivorceCosts", 30);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
