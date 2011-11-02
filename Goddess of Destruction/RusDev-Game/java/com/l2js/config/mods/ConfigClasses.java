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
public class ConfigClasses extends Config
{
	private final static String path = L2JS_CLASSES_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ALT_MAGES_PHYSICAL_DAMAGE_MULTI = getFloat(properties, "AltPDamageMages", 1F);
			ALT_MAGES_MAGICAL_DAMAGE_MULTI = getFloat(properties, "AltMDamageMages", 1F);
			ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = getFloat(properties, "AltPDamageFighters", 1F);
			ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = getFloat(properties, "AltMDamageFighters", 1F);
			ALT_PETS_PHYSICAL_DAMAGE_MULTI = getFloat(properties, "AltPDamagePets", 1F);
			ALT_PETS_MAGICAL_DAMAGE_MULTI = getFloat(properties, "AltMDamagePets", 1F);
			ALT_NPC_PHYSICAL_DAMAGE_MULTI = getFloat(properties, "AltPDamageNpc", 1F);
			ALT_NPC_MAGICAL_DAMAGE_MULTI = getFloat(properties, "AltMDamageNpc", 1F);
			ENABLE_CLASS_BALANCE_SYSTEM = getBoolean(properties, "EnableClassBalanceSystem", false);
			ENABLE_CLASS_VS_CLASS_SYSTEM = getBoolean(properties, "EnableClassVsClassSystem", false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
