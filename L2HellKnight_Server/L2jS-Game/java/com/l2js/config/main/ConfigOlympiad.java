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
public class ConfigOlympiad extends Config
{
	private final static String path = OLYMPIAD_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			ALT_OLY_START_TIME = getInt(properties, "AltOlyStartTime", 18);
			ALT_OLY_MIN = getInt(properties, "AltOlyMin", 0);
			ALT_OLY_CPERIOD = getLong(properties, "AltOlyCPeriod", 21600000);
			ALT_OLY_BATTLE = getLong(properties, "AltOlyBattle", 360000);
			ALT_OLY_WPERIOD = getLong(properties, "AltOlyWPeriod", 604800000);
			ALT_OLY_VPERIOD = getLong(properties, "AltOlyVPeriod", 86400000);
			ALT_OLY_START_POINTS = getInt(properties, "AltOlyStartPoints", 10);
			ALT_OLY_WEEKLY_POINTS = getInt(properties, "AltOlyWeeklyPoints", 10);
			ALT_OLY_CLASSED = getInt(properties, "AltOlyClassedParticipants", 11);
			ALT_OLY_NONCLASSED = getInt(properties, "AltOlyNonClassedParticipants", 11);
			ALT_OLY_TEAMS = getInt(properties, "AltOlyTeamsParticipants", 6);
			ALT_OLY_REG_DISPLAY = getInt(properties, "AltOlyRegistrationDisplayNumber", 100);
			ALT_OLY_CLASSED_REWARD = parseItemsList(getString(properties, "AltOlyClassedReward", "13722,50"));
			ALT_OLY_NONCLASSED_REWARD = parseItemsList(getString(properties, "AltOlyNonClassedReward", "13722,40"));
			ALT_OLY_TEAM_REWARD = parseItemsList(getString(properties, "AltOlyTeamReward", "13722,85"));
			ALT_OLY_COMP_RITEM = getInt(properties, "AltOlyCompRewItem", 13722);
			ALT_OLY_MIN_MATCHES = getInt(properties, "AltOlyMinMatchesForPoints", 15);
			ALT_OLY_GP_PER_POINT = getInt(properties, "AltOlyGPPerPoint", 1000);
			ALT_OLY_HERO_POINTS = getInt(properties, "AltOlyHeroPoints", 200);
			ALT_OLY_RANK1_POINTS = getInt(properties, "AltOlyRank1Points", 100);
			ALT_OLY_RANK2_POINTS = getInt(properties, "AltOlyRank2Points", 75);
			ALT_OLY_RANK3_POINTS = getInt(properties, "AltOlyRank3Points", 55);
			ALT_OLY_RANK4_POINTS = getInt(properties, "AltOlyRank4Points", 40);
			ALT_OLY_RANK5_POINTS = getInt(properties, "AltOlyRank5Points", 30);
			ALT_OLY_MAX_POINTS = getInt(properties, "AltOlyMaxPoints", 10);
			ALT_OLY_DIVIDER_CLASSED = getInt(properties, "AltOlyDividerClassed", 5);
			ALT_OLY_DIVIDER_NON_CLASSED = getInt(properties, "AltOlyDividerNonClassed", 5);
			ALT_OLY_LOG_FIGHTS = getBoolean(properties, "AltOlyLogFights", false);
			ALT_OLY_SHOW_MONTHLY_WINNERS = getBoolean(properties, "AltOlyShowMonthlyWinners", true);
			ALT_OLY_ANNOUNCE_GAMES = getBoolean(properties, "AltOlyAnnounceGames", true);
			String[] split = getStringArray(properties, "AltOlyRestrictedItems", new String[]
			{
					"6611", "6612", "6613", "6614", "6615", "6616", "6617", "6618", "6619", "6620", "6621", "9388",
					"9389", "9390", "17049", "17050", "17051", "17052", "17053", "17054", "17055", "17056", "17057",
					"17058", "17059", "17060", "17061", "20759", "20775", "20776", "20777", "20778", "14774"
			}, ",");
			LIST_OLY_RESTRICTED_ITEMS = new TIntArrayList(split.length);
			for (String id : split)
				LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			ALT_OLY_ENCHANT_LIMIT = getInt(properties, "AltOlyEnchantLimit", -1);
			ALT_OLY_WAIT_TIME = getInt(properties, "AltOlyWaitTime", 120);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
