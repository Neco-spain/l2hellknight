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
public class ConfigChampion extends Config
{
	private final static String path = L2JS_CHAMPION_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			CHAMPION_ENABLE = getBoolean(properties, "ChampionEnable", false);
			CHAMPION_PASSIVE = getBoolean(properties, "ChampionPassive", false);
			CHAMPION_FREQUENCY = getInt(properties, "ChampionFrequency", 0);
			CHAMP_TITLE = getString(properties, "ChampionTitle", "Champion");
			CHAMP_MIN_LVL = getInt(properties, "ChampionMinLevel", 20);
			CHAMP_MAX_LVL = getInt(properties, "ChampionMaxLevel", 60);
			CHAMPION_HP = getInt(properties, "ChampionHp", 7);
			CHAMPION_HP_REGEN = getFloat(properties, "ChampionHpRegen", 1F);
			CHAMPION_REWARDS = getInt(properties, "ChampionRewards", 8);
			CHAMPION_ADENAS_REWARDS = getFloat(properties, "ChampionAdenasRewards", 1F);
			CHAMPION_ATK = getFloat(properties, "ChampionAtk", 1F);
			CHAMPION_SPD_ATK = getFloat(properties, "ChampionSpdAtk", 1F);
			CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = getInt(properties, "ChampionRewardLowerLvlItemChance", 0);
			CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = getInt(properties, "ChampionRewardHigherLvlItemChance", 0);
			CHAMPION_REWARD_ID = getInt(properties, "ChampionRewardItemID", 6393);
			CHAMPION_REWARD_QTY = getInt(properties, "ChampionRewardItemQty", 1);
			CHAMPION_ENABLE_VITALITY = getBoolean(properties, "ChampionEnableVitality", false);
			CHAMPION_ENABLE_IN_INSTANCES = getBoolean(properties, "ChampionEnableInInstances", false);
			CHAMPION_ENABLE_AURA = getInt(properties, "ChampionEnableAura", 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
