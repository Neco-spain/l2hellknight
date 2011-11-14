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
package com.l2js.config.scripts;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigRankNpc extends Config
{
	private final static String path = L2JS_RANK_NPC_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			RANK_NPC_ID = getInt(properties, "RankNpcID", 70027);
			RANK_NPC_MIN_LEVEL = getInt(properties, "RankNpcMinLevel", 40);
			RANK_NPC_DISABLE_PAGE = getStringArray(properties, "RankNpcDisablePage", new String[]
			{
				"0"
			}, "\\,");
			RANK_NPC_LIST_ITEM = getIntArray(properties, "RankNpcListItem", new int[]
			{
					57, 5575, 6673
			}, "\\;");
			RANK_NPC_ITEMS_RECORDS = getInt(properties, "RankNpcItemsRecords", 20);
			RANK_NPC_LIST_CLASS = getIntArray(properties, "RankNpcListClass", new int[]
			{
					88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
					110, 111, 112, 113, 114, 115, 116, 117, 118, 131, 132, 133, 134, 136
			}, "\\;");
			RANK_NPC_OLY_RECORDS = getInt(properties, "RankNpcOlyRecords", 20);
			RANK_NPC_PVP_RECORDS = getInt(properties, "RankNpcPvPRecords", 100);
			RANK_NPC_PK_RECORDS = getInt(properties, "RankNpcPKRecords", 100);
			RANK_NPC_COLOR_A = getString(properties, "RankNpcColorA", "D9CC46");
			RANK_NPC_COLOR_B = getString(properties, "RankNpcColorB", "FFFFFF");
			RANK_NPC_RELOAD = getLong(properties, "RankNpcReload", 15L);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
