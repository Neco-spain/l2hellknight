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

import gnu.trove.TIntArrayList;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigNPC extends Config
{
	private final static String	path	= NPC_CONFIG;

	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ANNOUNCE_MAMMON_SPAWN = getBoolean(properties, "AnnounceMammonSpawn", false);
			ALT_MOB_AGRO_IN_PEACEZONE = getBoolean(properties, "AltMobAgroInPeaceZone", true);
			ALT_ATTACKABLE_NPCS = getBoolean(properties, "AltAttackableNpcs", true);
			ALT_GAME_VIEWNPC = getBoolean(properties, "AltGameViewNpc", false);
			MAX_DRIFT_RANGE = getInt(properties, "MaxDriftRange", 300);
			DEEPBLUE_DROP_RULES = getBoolean(properties, "UseDeepBlueDropRules", true);
			DEEPBLUE_DROP_RULES_RAID = getBoolean(properties, "UseDeepBlueDropRulesRaid", true);
			SHOW_NPC_LVL = getBoolean(properties, "ShowNpcLevel", false);
			SHOW_CREST_WITHOUT_QUEST = getBoolean(properties, "ShowCrestWithoutQuest", false);
			ENABLE_RANDOM_ENCHANT_EFFECT = getBoolean(properties, "EnableRandomEnchantEffect", false);
			MIN_NPC_LVL_DMG_PENALTY = getInt(properties, "MinNPCLevelForDmgPenalty", 78);
			NPC_DMG_PENALTY = parseConfigLine(getString(properties, "DmgPenaltyForLvLDifferences",
					"0.7, 0.6, 0.6, 0.55"));
			NPC_CRIT_DMG_PENALTY = parseConfigLine(getString(properties, "CritDmgPenaltyForLvLDifferences",
					"0.75, 0.65, 0.6, 0.58"));
			NPC_SKILL_DMG_PENALTY = parseConfigLine(getString(properties, "SkillDmgPenaltyForLvLDifferences",
					"0.8, 0.7, 0.65, 0.62"));
			MIN_NPC_LVL_MAGIC_PENALTY = getInt(properties, "MinNPCLevelForMagicPenalty", 78);
			NPC_SKILL_CHANCE_PENALTY = parseConfigLine(getString(properties, "SkillChancePenaltyForLvLDifferences",
					"2.5, 3.0, 3.25, 3.5"));
			ENABLE_DROP_VITALITY_HERBS = getBoolean(properties, "EnableVitalityHerbs", true);
			GUARD_ATTACK_AGGRO_MOB = getBoolean(properties, "GuardAttackAggroMob", false);
			ALLOW_WYVERN_UPGRADER = getBoolean(properties, "AllowWyvernUpgrader", false);
			String[] split = getStringArray(properties, "ListPetRentNpc", new String[] {
				"30827"
			}, ",");
			LIST_PET_RENT_NPC = new TIntArrayList(split.length);
			for (String id : split)
				LIST_PET_RENT_NPC.add(Integer.parseInt(id));
			RAID_HP_REGEN_MULTIPLIER = getDouble(properties, "RaidHpRegenMultiplier", 100D) / 100;
			RAID_MP_REGEN_MULTIPLIER = getDouble(properties, "RaidMpRegenMultiplier", 100D) / 100;
			RAID_PDEFENCE_MULTIPLIER = getDouble(properties, "RaidPDefenceMultiplier", 100D) / 100;
			RAID_MDEFENCE_MULTIPLIER = getDouble(properties, "RaidMDefenceMultiplier", 100D) / 100;
			RAID_PATTACK_MULTIPLIER = getDouble(properties, "RaidPAttackMultiplier", 100D) / 100;
			RAID_MATTACK_MULTIPLIER = getDouble(properties, "RaidMAttackMultiplier", 100D) / 100;
			RAID_MIN_RESPAWN_MULTIPLIER = getFloat(properties, "RaidMinRespawnMultiplier", 1F);
			RAID_MAX_RESPAWN_MULTIPLIER = getFloat(properties, "RaidMaxRespawnMultiplier", 1F);
			RAID_MINION_RESPAWN_TIMER = getInt(properties, "RaidMinionRespawnTime", 300000);
			RAID_DISABLE_CURSE = getBoolean(properties, "DisableRaidCurse", false);
			RAID_CHAOS_TIME = getInt(properties, "RaidChaosTime", 10);
			GRAND_CHAOS_TIME = getInt(properties, "GrandChaosTime", 10);
			MINION_CHAOS_TIME = getInt(properties, "MinionChaosTime", 10);
			INVENTORY_MAXIMUM_PET = getInt(properties, "MaximumSlotsForPet", 12);
			PET_HP_REGEN_MULTIPLIER = getDouble(properties, "PetHpRegenMultiplier", 100D) / 100;
			PET_MP_REGEN_MULTIPLIER = getDouble(properties, "PetMpRegenMultiplier", 100D) / 100;
			split = getStringArray(properties, "NonTalkingNpcs", new String[] {
					"18684", "18685", "18686", "18687", "18688", "18689", "18690", "19691", "18692", "31557", "31606",
					"31671", "31672", "31673", "31674", "32026", "32030", "32031", "32032", "32306", "32619", "32620",
					"32621"
			}, ",");
			NON_TALKING_NPCS = new TIntArrayList(split.length);
			for (String npcId : split)
			{
				try
				{
					NON_TALKING_NPCS.add(Integer.parseInt(npcId));
				}
				catch (NumberFormatException nfe)
				{
					if (!npcId.isEmpty())
					{
						_log.warning("Could not parse " + npcId
								+ " id for NonTalkingNpcs. Please check that all values are digits and coma separated.");
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
