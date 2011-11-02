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

import java.util.ArrayList;

import com.l2js.Config;
import com.l2js.util.L2Properties;
import com.l2js.util.StringUtil;

/**
 * @author L0ngh0rn
 */
public class ConfigFeature extends Config
{
	private final static String path = FEATURE_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			CH_TELE_FEE_RATIO = getLong(properties, "ClanHallTeleportFunctionFeeRatio", 604800000);
			CH_TELE1_FEE = getInt(properties, "ClanHallTeleportFunctionFeeLvl1", 7000);
			CH_TELE2_FEE = getInt(properties, "ClanHallTeleportFunctionFeeLvl2", 14000);
			CH_SUPPORT_FEE_RATIO = getLong(properties, "ClanHallSupportFunctionFeeRatio", 86400000);
			CH_SUPPORT1_FEE = getInt(properties, "ClanHallSupportFeeLvl1", 2500);
			CH_SUPPORT2_FEE = getInt(properties, "ClanHallSupportFeeLvl2", 5000);
			CH_SUPPORT3_FEE = getInt(properties, "ClanHallSupportFeeLvl3", 7000);
			CH_SUPPORT4_FEE = getInt(properties, "ClanHallSupportFeeLvl4", 11000);
			CH_SUPPORT5_FEE = getInt(properties, "ClanHallSupportFeeLvl5", 21000);
			CH_SUPPORT6_FEE = getInt(properties, "ClanHallSupportFeeLvl6", 36000);
			CH_SUPPORT7_FEE = getInt(properties, "ClanHallSupportFeeLvl7", 37000);
			CH_SUPPORT8_FEE = getInt(properties, "ClanHallSupportFeeLvl8", 52000);
			CH_MPREG_FEE_RATIO = getLong(properties, "ClanHallMpRegenerationFunctionFeeRatio", 86400000);
			CH_MPREG1_FEE = getInt(properties, "ClanHallMpRegenerationFeeLvl1", 2000);
			CH_MPREG2_FEE = getInt(properties, "ClanHallMpRegenerationFeeLvl2", 3750);
			CH_MPREG3_FEE = getInt(properties, "ClanHallMpRegenerationFeeLvl3", 6500);
			CH_MPREG4_FEE = getInt(properties, "ClanHallMpRegenerationFeeLvl4", 13750);
			CH_MPREG5_FEE = getInt(properties, "ClanHallMpRegenerationFeeLvl5", 20000);
			CH_HPREG_FEE_RATIO = getLong(properties, "ClanHallHpRegenerationFunctionFeeRatio", 86400000);
			CH_HPREG1_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl1", 700);
			CH_HPREG2_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl2", 800);
			CH_HPREG3_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl3", 1000);
			CH_HPREG4_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl4", 1166);
			CH_HPREG5_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl5", 1500);
			CH_HPREG6_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl6", 1750);
			CH_HPREG7_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl7", 2000);
			CH_HPREG8_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl8", 2250);
			CH_HPREG9_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl9", 2500);
			CH_HPREG10_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl10", 3250);
			CH_HPREG11_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl11", 3270);
			CH_HPREG12_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl12", 4250);
			CH_HPREG13_FEE = getInt(properties, "ClanHallHpRegenerationFeeLvl13", 5166);
			CH_EXPREG_FEE_RATIO = Config.getLong(properties, "ClanHallExpRegenerationFunctionFeeRatio", 86400000);
			CH_EXPREG1_FEE = getInt(properties, "ClanHallExpRegenerationFeeLvl1", 3000);
			CH_EXPREG2_FEE = getInt(properties, "ClanHallExpRegenerationFeeLvl2", 6000);
			CH_EXPREG3_FEE = getInt(properties, "ClanHallExpRegenerationFeeLvl3", 9000);
			CH_EXPREG4_FEE = getInt(properties, "ClanHallExpRegenerationFeeLvl4", 15000);
			CH_EXPREG5_FEE = getInt(properties, "ClanHallExpRegenerationFeeLvl5", 21000);
			CH_EXPREG6_FEE = getInt(properties, "ClanHallExpRegenerationFeeLvl6", 23330);
			CH_EXPREG7_FEE = getInt(properties, "ClanHallExpRegenerationFeeLvl7", 30000);
			CH_ITEM_FEE_RATIO = getLong(properties, "ClanHallItemCreationFunctionFeeRatio", 86400000);
			CH_ITEM1_FEE = getInt(properties, "ClanHallItemCreationFunctionFeeLvl1", 30000);
			CH_ITEM2_FEE = getInt(properties, "ClanHallItemCreationFunctionFeeLvl2", 70000);
			CH_ITEM3_FEE = getInt(properties, "ClanHallItemCreationFunctionFeeLvl3", 140000);
			CH_CURTAIN_FEE_RATIO = getLong(properties, "ClanHallCurtainFunctionFeeRatio", 604800000);
			CH_CURTAIN1_FEE = getInt(properties, "ClanHallCurtainFunctionFeeLvl1", 2000);
			CH_CURTAIN2_FEE = getInt(properties, "ClanHallCurtainFunctionFeeLvl2", 2500);
			CH_FRONT_FEE_RATIO = getLong(properties, "ClanHallFrontPlatformFunctionFeeRatio", 259200000);
			CH_FRONT1_FEE = getInt(properties, "ClanHallFrontPlatformFunctionFeeLvl1", 1300);
			CH_FRONT2_FEE = getInt(properties, "ClanHallFrontPlatformFunctionFeeLvl2", 4000);
			CH_BUFF_FREE = getBoolean(properties, "AltClanHallMpBuffFree", false);

			CL_SET_SIEGE_TIME_LIST = new ArrayList<String>();
			SIEGE_HOUR_LIST_MORNING = new ArrayList<Integer>();
			SIEGE_HOUR_LIST_AFTERNOON = new ArrayList<Integer>();
			String[] sstl = getString(properties, "CLSetSiegeTimeList", "").split(",");
			if (sstl.length != 0)
			{
				boolean isHour = false;
				for (String st : sstl)
				{
					if (st.equalsIgnoreCase("day") || st.equalsIgnoreCase("hour") || st.equalsIgnoreCase("minute"))
					{
						if (st.equalsIgnoreCase("hour) isHour = true"))
							;
						CL_SET_SIEGE_TIME_LIST.add(st.toLowerCase());
					}
					else
						_log.warning(StringUtil.concat(
								"[CLSetSiegeTimeList]: invalid config property -> CLSetSiegeTimeList \"", st, "\""));
				}
				if (isHour)
				{
					String[] shl = getString(properties, "SiegeHourList", "").split(",");
					for (String st : shl)
					{
						if (!st.equalsIgnoreCase(""))
						{
							int val = Integer.parseInt(st);
							if (val > 23 || val < 0)
								_log.warning(StringUtil.concat(
										"[SiegeHourList]: invalid config property -> SiegeHourList \"", st, "\""));
							else if (val < 12)
								SIEGE_HOUR_LIST_MORNING.add(val);
							else
							{
								val -= 12;
								SIEGE_HOUR_LIST_AFTERNOON.add(val);
							}
						}
					}
					if (SIEGE_HOUR_LIST_AFTERNOON.isEmpty() && SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
					{
						_log.warning("[SiegeHourList]: invalid config property -> SiegeHourList is empty");
						CL_SET_SIEGE_TIME_LIST.remove("hour");
					}
				}
			}
			CS_TELE_FEE_RATIO = getLong(properties, "CastleTeleportFunctionFeeRatio", 604800000);
			CS_TELE1_FEE = getInt(properties, "CastleTeleportFunctionFeeLvl1", 7000);
			CS_TELE2_FEE = getInt(properties, "CastleTeleportFunctionFeeLvl2", 14000);
			CS_SUPPORT_FEE_RATIO = getLong(properties, "CastleSupportFunctionFeeRatio", 86400000);
			CS_SUPPORT1_FEE = getInt(properties, "CastleSupportFeeLvl1", 7000);
			CS_SUPPORT2_FEE = getInt(properties, "CastleSupportFeeLvl2", 21000);
			CS_SUPPORT3_FEE = getInt(properties, "CastleSupportFeeLvl3", 37000);
			CS_SUPPORT4_FEE = getInt(properties, "CastleSupportFeeLvl4", 52000);
			CS_MPREG_FEE_RATIO = getLong(properties, "CastleMpRegenerationFunctionFeeRatio", 86400000);
			CS_MPREG1_FEE = getInt(properties, "CastleMpRegenerationFeeLvl1", 2000);
			CS_MPREG2_FEE = getInt(properties, "CastleMpRegenerationFeeLvl2", 6500);
			CS_MPREG3_FEE = getInt(properties, "CastleMpRegenerationFeeLvl3", 13750);
			CS_MPREG4_FEE = getInt(properties, "CastleMpRegenerationFeeLvl4", 20000);
			CS_HPREG_FEE_RATIO = getLong(properties, "CastleHpRegenerationFunctionFeeRatio", 86400000);
			CS_HPREG1_FEE = getInt(properties, "CastleHpRegenerationFeeLvl1", 1000);
			CS_HPREG2_FEE = getInt(properties, "CastleHpRegenerationFeeLvl2", 1500);
			CS_HPREG3_FEE = getInt(properties, "CastleHpRegenerationFeeLvl3", 2250);
			CS_HPREG4_FEE = getInt(properties, "CastleHpRegenerationFeeLvl4", 3270);
			CS_HPREG5_FEE = getInt(properties, "CastleHpRegenerationFeeLvl5", 5166);
			CS_EXPREG_FEE_RATIO = getLong(properties, "CastleExpRegenerationFunctionFeeRatio", 86400000);
			CS_EXPREG1_FEE = getInt(properties, "CastleExpRegenerationFeeLvl1", 9000);
			CS_EXPREG2_FEE = getInt(properties, "CastleExpRegenerationFeeLvl2", 15000);
			CS_EXPREG3_FEE = getInt(properties, "CastleExpRegenerationFeeLvl3", 21000);
			CS_EXPREG4_FEE = getInt(properties, "CastleExpRegenerationFeeLvl4", 30000);

			FS_TELE_FEE_RATIO = getLong(properties, "FortressTeleportFunctionFeeRatio", 604800000);
			FS_TELE1_FEE = getInt(properties, "FortressTeleportFunctionFeeLvl1", 1000);
			FS_TELE2_FEE = getInt(properties, "FortressTeleportFunctionFeeLvl2", 10000);
			FS_SUPPORT_FEE_RATIO = getLong(properties, "FortressSupportFunctionFeeRatio", 86400000);
			FS_SUPPORT1_FEE = getInt(properties, "FortressSupportFeeLvl1", 7000);
			FS_SUPPORT2_FEE = getInt(properties, "FortressSupportFeeLvl2", 17000);
			FS_MPREG_FEE_RATIO = getLong(properties, "FortressMpRegenerationFunctionFeeRatio", 86400000);
			FS_MPREG1_FEE = getInt(properties, "FortressMpRegenerationFeeLvl1", 6500);
			FS_MPREG2_FEE = getInt(properties, "FortressMpRegenerationFeeLvl2", 9300);
			FS_HPREG_FEE_RATIO = getLong(properties, "FortressHpRegenerationFunctionFeeRatio", 86400000);
			FS_HPREG1_FEE = getInt(properties, "FortressHpRegenerationFeeLvl1", 2000);
			FS_HPREG2_FEE = getInt(properties, "FortressHpRegenerationFeeLvl2", 3500);
			FS_EXPREG_FEE_RATIO = Config.getLong(properties, "FortressExpRegenerationFunctionFeeRatio", 86400000);
			FS_EXPREG1_FEE = getInt(properties, "FortressExpRegenerationFeeLvl1", 9000);
			FS_EXPREG2_FEE = getInt(properties, "FortressExpRegenerationFeeLvl2", 10000);
			FS_BLOOD_OATH_COUNT = getInt(properties, "FortressBloodOathCount", 1);
			FS_UPDATE_FRQ = getInt(properties, "FortressPeriodicUpdateFrequency", 360);
			FS_MAX_SUPPLY_LEVEL = getInt(properties, "FortressMaxSupplyLevel", 6);
			FS_FEE_FOR_CASTLE = getInt(properties, "FortressFeeForCastle", 25000);
			FS_MAX_OWN_TIME = getInt(properties, "FortressMaximumOwnTime", 168);

			ALT_GAME_CASTLE_DAWN = getBoolean(properties, "AltCastleForDawn", true);
			ALT_GAME_CASTLE_DUSK = getBoolean(properties, "AltCastleForDusk", true);
			ALT_GAME_REQUIRE_CLAN_CASTLE = getBoolean(properties, "AltRequireClanCastle", false);
			ALT_FESTIVAL_MIN_PLAYER = getInt(properties, "AltFestivalMinPlayer", 5);
			ALT_MAXIMUM_PLAYER_CONTRIB = getInt(properties, "AltMaxPlayerContrib", 1000000);
			ALT_FESTIVAL_MANAGER_START = getLong(properties, "AltFestivalManagerStart", 120000);
			ALT_FESTIVAL_LENGTH = getLong(properties, "AltFestivalLength", 1080000);
			ALT_FESTIVAL_CYCLE_LENGTH = getLong(properties, "AltFestivalCycleLength", 2280000);
			ALT_FESTIVAL_FIRST_SPAWN = getLong(properties, "AltFestivalFirstSpawn", 120000);
			ALT_FESTIVAL_FIRST_SWARM = getLong(properties, "AltFestivalFirstSwarm", 300000);
			ALT_FESTIVAL_SECOND_SPAWN = getLong(properties, "AltFestivalSecondSpawn", 540000);
			ALT_FESTIVAL_SECOND_SWARM = getLong(properties, "AltFestivalSecondSwarm", 720000);
			ALT_FESTIVAL_CHEST_SPAWN = getLong(properties, "AltFestivalChestSpawn", 900000);
			ALT_SIEGE_DAWN_GATES_PDEF_MULT = getDouble(properties, "AltDawnGatesPdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_PDEF_MULT = getDouble(properties, "AltDuskGatesPdefMult", 0.8);
			ALT_SIEGE_DAWN_GATES_MDEF_MULT = getDouble(properties, "AltDawnGatesMdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_MDEF_MULT = getDouble(properties, "AltDuskGatesMdefMult", 0.8);
			ALT_STRICT_SEVENSIGNS = getBoolean(properties, "StrictSevenSigns", true);
			ALT_SEVENSIGNS_LAZY_UPDATE = getBoolean(properties, "AltSevenSignsLazyUpdate", true);

			TAKE_FORT_POINTS = getInt(properties, "TakeFortPoints", 200);
			LOOSE_FORT_POINTS = getInt(properties, "LooseFortPoints", 0);
			TAKE_CASTLE_POINTS = getInt(properties, "TakeCastlePoints", 1500);
			LOOSE_CASTLE_POINTS = getInt(properties, "LooseCastlePoints", 3000);
			CASTLE_DEFENDED_POINTS = getInt(properties, "CastleDefendedPoints", 750);
			FESTIVAL_WIN_POINTS = getInt(properties, "FestivalOfDarknessWin", 200);
			HERO_POINTS = getInt(properties, "HeroPoints", 1000);
			ROYAL_GUARD_COST = getInt(properties, "CreateRoyalGuardCost", 5000);
			KNIGHT_UNIT_COST = getInt(properties, "CreateKnightUnitCost", 10000);
			KNIGHT_REINFORCE_COST = getInt(properties, "ReinforceKnightUnitCost", 5000);
			BALLISTA_POINTS = getInt(properties, "KillBallistaPoints", 30);
			BLOODALLIANCE_POINTS = getInt(properties, "BloodAlliancePoints", 500);
			BLOODOATH_POINTS = getInt(properties, "BloodOathPoints", 200);
			KNIGHTSEPAULETTE_POINTS = getInt(properties, "KnightsEpaulettePoints", 20);
			REPUTATION_SCORE_PER_KILL = getInt(properties, "ReputationScorePerKill", 1);
			JOIN_ACADEMY_MIN_REP_SCORE = getInt(properties, "CompleteAcademyMinPoints", 190);
			JOIN_ACADEMY_MAX_REP_SCORE = getInt(properties, "CompleteAcademyMaxPoints", 650);
			RAID_RANKING_1ST = getInt(properties, "1stRaidRankingPoints", 1250);
			RAID_RANKING_2ND = getInt(properties, "2ndRaidRankingPoints", 900);
			RAID_RANKING_3RD = getInt(properties, "3rdRaidRankingPoints", 700);
			RAID_RANKING_4TH = getInt(properties, "4thRaidRankingPoints", 600);
			RAID_RANKING_5TH = getInt(properties, "5thRaidRankingPoints", 450);
			RAID_RANKING_6TH = getInt(properties, "6thRaidRankingPoints", 350);
			RAID_RANKING_7TH = getInt(properties, "7thRaidRankingPoints", 300);
			RAID_RANKING_8TH = getInt(properties, "8thRaidRankingPoints", 200);
			RAID_RANKING_9TH = getInt(properties, "9thRaidRankingPoints", 150);
			RAID_RANKING_10TH = getInt(properties, "10thRaidRankingPoints", 100);
			RAID_RANKING_UP_TO_50TH = getInt(properties, "UpTo50thRaidRankingPoints", 25);
			RAID_RANKING_UP_TO_100TH = getInt(properties, "UpTo100thRaidRankingPoints", 12);
			CLAN_LEVEL_6_COST = getInt(properties, "ClanLevel6Cost", 5000);
			CLAN_LEVEL_7_COST = getInt(properties, "ClanLevel7Cost", 10000);
			CLAN_LEVEL_8_COST = getInt(properties, "ClanLevel8Cost", 20000);
			CLAN_LEVEL_9_COST = getInt(properties, "ClanLevel9Cost", 40000);
			CLAN_LEVEL_10_COST = getInt(properties, "ClanLevel10Cost", 40000);
			CLAN_LEVEL_11_COST = getInt(properties, "ClanLevel11Cost", 75000);
			CLAN_LEVEL_6_REQUIREMENT = getInt(properties, "ClanLevel6Requirement", 30);
			CLAN_LEVEL_7_REQUIREMENT = getInt(properties, "ClanLevel7Requirement", 50);
			CLAN_LEVEL_8_REQUIREMENT = getInt(properties, "ClanLevel8Requirement", 80);
			CLAN_LEVEL_9_REQUIREMENT = getInt(properties, "ClanLevel9Requirement", 120);
			CLAN_LEVEL_10_REQUIREMENT = getInt(properties, "ClanLevel10Requirement", 140);
			CLAN_LEVEL_11_REQUIREMENT = getInt(properties, "ClanLevel11Requirement", 170);
			ALLOW_WYVERN_DURING_SIEGE = getBoolean(properties, "AllowRideWyvernDuringSiege", true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
