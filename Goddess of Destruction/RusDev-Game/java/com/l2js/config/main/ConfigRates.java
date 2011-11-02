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

import gnu.trove.TIntFloatHashMap;

import com.l2js.Config;
import com.l2js.util.L2Properties;
import com.l2js.util.StringUtil;

/**
 * @author L0ngh0rn
 */
public class ConfigRates extends Config
{
	private final static String path = RATES_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			RATE_XP = getFloat(properties, "RateXp", 1F);
			RATE_SP = getFloat(properties, "RateSp", 1F);
			RATE_PARTY_XP = getFloat(properties, "RatePartyXp", 1F);
			RATE_PARTY_SP = getFloat(properties, "RatePartySp", 1F);
			RATE_CONSUMABLE_COST = getFloat(properties, "RateConsumableCost", 1F);
			RATE_EXTR_FISH = getFloat(properties, "RateExtractFish", 1F);
			RATE_DROP_ITEMS = getFloat(properties, "RateDropItems", 1F);
			RATE_DROP_ITEMS_BY_RAID = getFloat(properties, "RateRaidDropItems", 1F);
			RATE_DROP_SPOIL = getFloat(properties, "RateDropSpoil", 1F);
			RATE_DROP_MANOR = getInt(properties, "RateDropManor", 1);
			RATE_QUEST_DROP = getFloat(properties, "RateQuestDrop", 1F);
			RATE_QUEST_REWARD = getFloat(properties, "RateQuestReward", 1F);
			RATE_QUEST_REWARD_XP = getFloat(properties, "RateQuestRewardXP", 1F);
			RATE_QUEST_REWARD_SP = getFloat(properties, "RateQuestRewardSP", 1F);
			RATE_QUEST_REWARD_ADENA = getFloat(properties, "RateQuestRewardAdena", 1F);
			RATE_QUEST_REWARD_USE_MULTIPLIERS = getBoolean(properties, "UseQuestRewardMultipliers", false);
			RATE_QUEST_REWARD_POTION = getFloat(properties, "RateQuestRewardPotion", 1F);
			RATE_QUEST_REWARD_SCROLL = getFloat(properties, "RateQuestRewardScroll", 1F);
			RATE_QUEST_REWARD_RECIPE = getFloat(properties, "RateQuestRewardRecipe", 1F);
			RATE_QUEST_REWARD_MATERIAL = getFloat(properties, "RateQuestRewardMaterial", 1F);
			RATE_VITALITY_LEVEL_1 = getFloat(properties, "RateVitalityLevel1", 1.5F);
			RATE_VITALITY_LEVEL_2 = getFloat(properties, "RateVitalityLevel2", 2F);
			RATE_VITALITY_LEVEL_3 = getFloat(properties, "RateVitalityLevel3", 2.5F);
			RATE_VITALITY_LEVEL_4 = getFloat(properties, "RateVitalityLevel4", 3F);
			RATE_RECOVERY_VITALITY_PEACE_ZONE = getFloat(properties, "RateRecoveryPeaceZone", 1F);
			RATE_VITALITY_LOST = getFloat(properties, "RateVitalityLost", 1F);
			RATE_VITALITY_GAIN = getFloat(properties, "RateVitalityGain", 1F);
			RATE_RECOVERY_ON_RECONNECT = getFloat(properties, "RateRecoveryOnReconnect", 4F);
			RATE_KARMA_EXP_LOST = getFloat(properties, "RateKarmaExpLost", 1F);
			RATE_SIEGE_GUARDS_PRICE = getFloat(properties, "RateSiegeGuardsPrice", 1F);
			RATE_DROP_COMMON_HERBS = getFloat(properties, "RateCommonHerbs", 15F);
			RATE_DROP_HP_HERBS = getFloat(properties, "RateHpHerbs", 10F);
			RATE_DROP_MP_HERBS = getFloat(properties, "RateMpHerbs", 4F);
			RATE_DROP_SPECIAL_HERBS = getFloat(properties, "RateSpecialHerbs", 0.2F) * 10;
			RATE_DROP_VITALITY_HERBS = getFloat(properties, "RateVitalityHerbs", 2F);
			PLAYER_DROP_LIMIT = getInt(properties, "PlayerDropLimit", 3);
			PLAYER_RATE_DROP = getInt(properties, "PlayerRateDrop", 5);
			PLAYER_RATE_DROP_ITEM = getInt(properties, "PlayerRateDropItem", 70);
			PLAYER_RATE_DROP_EQUIP = getInt(properties, "PlayerRateDropEquip", 25);
			PLAYER_RATE_DROP_EQUIP_WEAPON = getInt(properties, "PlayerRateDropEquipWeapon", 5);
			PET_XP_RATE = getFloat(properties, "PetXpRate", 1F);
			PET_FOOD_RATE = getInt(properties, "PetFoodRate", 1);
			SINEATER_XP_RATE = getFloat(properties, "SinEaterXpRate", 1F);
			KARMA_DROP_LIMIT = getInt(properties, "KarmaDropLimit", 10);
			KARMA_RATE_DROP = getInt(properties, "KarmaRateDrop", 70);
			KARMA_RATE_DROP_ITEM = getInt(properties, "KarmaRateDropItem", 50);
			KARMA_RATE_DROP_EQUIP = getInt(properties, "KarmaRateDropEquip", 40);
			KARMA_RATE_DROP_EQUIP_WEAPON = getInt(properties, "KarmaRateDropEquipWeapon", 10);
			PLAYER_XP_PERCENT_LOST = new double[Byte.MAX_VALUE + 1];
			for (int i = 0; i <= Byte.MAX_VALUE; i++)
				PLAYER_XP_PERCENT_LOST[i] = 1.;

			// Now loading into table parsed values
			try
			{
				String[] values = getStringArray(properties, "PlayerXPPercentLost", new String[]
				{
						"0,39-7.0", "40,75-4.0", "76,76-2.5", "77,77-2.0", "78,78-1.5"
				}, ";");
				for (String s : values)
				{
					int min;
					int max;
					double val;

					String[] vals = s.split("-");
					String[] mM = vals[0].split(",");

					min = Integer.parseInt(mM[0]);
					max = Integer.parseInt(mM[1]);
					val = Double.parseDouble(vals[1]);

					for (int i = min; i <= max; i++)
						PLAYER_XP_PERCENT_LOST[i] = val;
				}
			}
			catch (Exception e)
			{
				_log.warning("Error while loading Player XP percent lost");
				e.printStackTrace();
			}

			String[] propertySplit = getStringArray(properties, "RateDropItemsById", new String[] {}, ";");
			RATE_DROP_ITEMS_ID = new TIntFloatHashMap(propertySplit.length);
			if (!propertySplit[0].isEmpty())
			{
				for (String item : propertySplit)
				{
					String[] itemSplit = item.split(",");
					if (itemSplit.length != 2)
						_log.warning(StringUtil.concat("load(): invalid config property -> RateDropItemsById \"", item,
								"\""));
					else
					{
						try
						{
							RATE_DROP_ITEMS_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!item.isEmpty())
								_log.warning(StringUtil.concat(
										"load(): invalid config property -> RateDropItemsById \"", item, "\""));
						}
					}
				}
			}
			// for Adena rate if not defined
			if (RATE_DROP_ITEMS_ID.get(57) == 0f)
				RATE_DROP_ITEMS_ID.put(57, RATE_DROP_ITEMS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
