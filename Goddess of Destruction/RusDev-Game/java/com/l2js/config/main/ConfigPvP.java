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

import java.util.Arrays;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigPvP extends Config
{
	private final static String path = PVP_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			KARMA_MIN_KARMA = getInt(properties, "MinKarma", 240);
			KARMA_MAX_KARMA = getInt(properties, "MaxKarma", 10000);
			KARMA_XP_DIVIDER = getInt(properties, "XPDivider", 260);
			KARMA_LOST_BASE = getInt(properties, "BaseKarmaLost", 0);
			KARMA_DROP_GM = getBoolean(properties, "CanGMDropEquipment", false);
			KARMA_AWARD_PK_KILL = getBoolean(properties, "AwardPKKillPVPPoint", true);
			KARMA_PK_LIMIT = getInt(properties, "MinimumPKRequiredToDrop", 5);
			KARMA_NONDROPPABLE_PET_ITEMS = getString(properties, "ListOfPetItems",
					"2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
			KARMA_NONDROPPABLE_ITEMS = Config
					.getString(
							properties,
							"ListOfNonDroppableItems",
							"57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390");

			String[] array = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[array.length];

			for (int i = 0; i < array.length; i++)
				KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i]);

			array = KARMA_NONDROPPABLE_ITEMS.split(",");
			KARMA_LIST_NONDROPPABLE_ITEMS = new int[array.length];

			for (int i = 0; i < array.length; i++)
				KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i]);

			// sorting so binarySearch can be used later
			Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
			Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);

			PVP_NORMAL_TIME = getInt(properties, "PvPVsNormalTime", 120000);
			PVP_PVP_TIME = getInt(properties, "PvPVsPvPTime", 60000);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
