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

import gnu.trove.TIntIntHashMap;

import java.util.ArrayList;

import com.l2js.Config;
import com.l2js.util.L2Properties;
import com.l2js.util.StringUtil;

/**
 * @author L0ngh0rn
 */
public class ConfigTvT extends Config
{
	private final static String path = L2JS_TVT_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			TVT_EVENT_ENABLED = getBoolean(properties, "TvTEventEnabled", false);
			TVT_EVENT_IN_INSTANCE = getBoolean(properties, "TvTEventInInstance", false);
			TVT_EVENT_INSTANCE_FILE = getString(properties, "TvTEventInstanceFile", "coliseum.xml");
			TVT_EVENT_INTERVAL = getString(properties, "TvTEventInterval", "20:00").split(",");
			String[] timeParticipation = getString(properties, "TvTEventParticipationTime", "01:00:00").split(":");
			Long time = 0L;
			time += Long.parseLong(timeParticipation[0]) * 3600L;
			time += Long.parseLong(timeParticipation[1]) * 60L;
			time += Long.parseLong(timeParticipation[2]);
			TVT_EVENT_PARTICIPATION_TIME = time * 1000L;
			TVT_EVENT_RUNNING_TIME = getInt(properties, "TvTEventRunningTime", 20);
			TVT_EVENT_PARTICIPATION_NPC_ID = getInt(properties, "TvTEventParticipationNpcId", 0);
			if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
			{
				TVT_EVENT_ENABLED = false;
				_log.warning("TvTEventEngine[load()]: invalid config property -> TvTEventParticipationNpcId");
			}
			else
			{
				String[] propertySplit = getString(properties, "TvTEventParticipationNpcCoordinates", "0,0,0").split(
						",");
				if (propertySplit.length < 3)
				{
					TVT_EVENT_ENABLED = false;
					_log.warning("TvTEventEngine[load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
				}
				else
				{
					TVT_EVENT_REWARDS = new ArrayList<int[]>();
					TVT_DOORS_IDS_TO_OPEN = new ArrayList<Integer>();
					TVT_DOORS_IDS_TO_CLOSE = new ArrayList<Integer>();
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
					TVT_EVENT_TEAM_1_COORDINATES = new int[3];
					TVT_EVENT_TEAM_2_COORDINATES = new int[3];
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
					TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
					if (propertySplit.length == 4)
						TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
					TVT_EVENT_MIN_PLAYERS_IN_TEAMS = getInt(properties, "TvTEventMinPlayersInTeams", 1);
					TVT_EVENT_MAX_PLAYERS_IN_TEAMS = getInt(properties, "TvTEventMaxPlayersInTeams", 20);
					TVT_EVENT_MIN_LVL = (byte) getInt(properties, "TvTEventMinPlayerLevel", 1);
					TVT_EVENT_MAX_LVL = (byte) getInt(properties, "TvTEventMaxPlayerLevel", 80);
					TVT_EVENT_RESPAWN_TELEPORT_DELAY = getInt(properties, "TvTEventRespawnTeleportDelay", 20);
					TVT_EVENT_START_LEAVE_TELEPORT_DELAY = getInt(properties, "TvTEventStartLeaveTeleportDelay", 20);
					TVT_EVENT_EFFECTS_REMOVAL = getInt(properties, "TvTEventEffectsRemoval", 0);
					TVT_EVENT_TEAM_1_NAME = getString(properties, "TvTEventTeam1Name", "Team1");
					propertySplit = getString(properties, "TvTEventTeam1Coordinates", "0,0,0").split(",");
					if (propertySplit.length < 3)
					{
						TVT_EVENT_ENABLED = false;
						_log.warning("TvTEventEngine[load()]: invalid config property -> TvTEventTeam1Coordinates");
					}
					else
					{
						TVT_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
						TVT_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
						TVT_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
						TVT_EVENT_TEAM_2_NAME = getString(properties, "TvTEventTeam2Name", "Team2");
						propertySplit = getString(properties, "TvTEventTeam2Coordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							TVT_EVENT_ENABLED = false;
							_log.warning("TvTEventEngine[load()]: invalid config property -> TvTEventTeam2Coordinates");
						}
						else
						{
							TVT_ALLOW_VOICED_COMMAND = getBoolean(properties, "TvTAllowVoicedInfoCommand", true);
							TVT_EVENT_MULTIBOX_PROTECTION_ENABLE = getBoolean(properties, "TvTEventMultiBoxEnable",
									false);
							TVT_EVENT_NUMBER_BOX_REGISTER = getInt(properties, "TvTEventNumberBoxRegister", 1);
							TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
							TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
							TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
							propertySplit = getString(properties, "TvTEventParticipationFee", "0,0").split(",");
							try
							{
								TVT_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
								TVT_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
							}
							catch (NumberFormatException nfe)
							{
								if (propertySplit.length > 0)
									_log.warning("TvTEventEngine[load()]: invalid config property -> TvTEventParticipationFee");
							}
							propertySplit = getString(properties, "TvTEventReward", "57,100000").split(";");
							for (String reward : propertySplit)
							{
								String[] rewardSplit = reward.split(",");
								if (rewardSplit.length != 2)
									_log.warning(StringUtil.concat(
											"TvTEventEngine[load()]: invalid config property -> TvTEventReward \"",
											reward, "\""));
								else
								{
									try
									{
										TVT_EVENT_REWARDS.add(new int[]
										{
												Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])
										});
									}
									catch (NumberFormatException nfe)
									{
										if (!reward.isEmpty())
											_log.warning(StringUtil
													.concat("TvTEventEngine[load()]: invalid config property -> TvTEventReward \"",
															reward, "\""));
									}
								}
							}

							TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = getBoolean(properties,
									"TvTEventTargetTeamMembersAllowed", true);
							TVT_EVENT_SCROLL_ALLOWED = getBoolean(properties, "TvTEventScrollsAllowed", false);
							TVT_EVENT_POTIONS_ALLOWED = getBoolean(properties, "TvTEventPotionsAllowed", false);
							TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = getBoolean(properties, "TvTEventSummonByItemAllowed",
									false);
							TVT_REWARD_TEAM_TIE = getBoolean(properties, "TvTRewardTeamTie", false);
							TVT_REWARD_PLAYER = getBoolean(properties, "TvTRewardPlayer", true);
							propertySplit = getString(properties, "TvTDoorsToOpen", "").split(";");
							for (String door : propertySplit)
							{
								try
								{
									TVT_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
								}
								catch (NumberFormatException nfe)
								{
									if (!door.isEmpty())
										_log.warning(StringUtil.concat(
												"TvTEventEngine[load()]: invalid config property -> TvTDoorsToOpen \"",
												door, "\""));
								}
							}

							propertySplit = getString(properties, "TvTDoorsToClose", "").split(";");
							for (String door : propertySplit)
							{
								try
								{
									TVT_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
								}
								catch (NumberFormatException nfe)
								{
									if (!door.isEmpty())
										_log.warning(StringUtil
												.concat("TvTEventEngine[load()]: invalid config property -> TvTDoorsToClose \"",
														door, "\""));
								}
							}

							propertySplit = getString(properties, "TvTEventFighterBuffs", "").split(";");
							if (!propertySplit[0].isEmpty())
							{
								TVT_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(propertySplit.length);
								for (String skill : propertySplit)
								{
									String[] skillSplit = skill.split(",");
									if (skillSplit.length != 2)
										_log.warning(StringUtil
												.concat("TvTEventEngine[load()]: invalid config property -> TvTEventFighterBuffs \"",
														skill, "\""));
									else
									{
										try
										{
											TVT_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]),
													Integer.parseInt(skillSplit[1]));
										}
										catch (NumberFormatException nfe)
										{
											if (!skill.isEmpty())
												_log.warning(StringUtil
														.concat("TvTEventEngine[load()]: invalid config property -> TvTEventFighterBuffs \"",
																skill, "\""));
										}
									}
								}
							}

							propertySplit = getString(properties, "TvTEventMageBuffs", "").split(";");
							if (!propertySplit[0].isEmpty())
							{
								TVT_EVENT_MAGE_BUFFS = new TIntIntHashMap(propertySplit.length);
								for (String skill : propertySplit)
								{
									String[] skillSplit = skill.split(",");
									if (skillSplit.length != 2)
										_log.warning(StringUtil
												.concat("TvTEventEngine[load()]: invalid config property -> TvTEventMageBuffs \"",
														skill, "\""));
									else
									{
										try
										{
											TVT_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]),
													Integer.parseInt(skillSplit[1]));
										}
										catch (NumberFormatException nfe)
										{
											if (!skill.isEmpty())
												_log.warning(StringUtil
														.concat("TvTEventEngine[load()]: invalid config property -> TvTEventMageBuffs \"",
																skill, "\""));
										}
									}
								}
							}
						}
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
