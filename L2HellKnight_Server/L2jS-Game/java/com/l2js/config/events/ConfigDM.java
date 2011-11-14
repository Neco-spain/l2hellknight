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
import java.util.HashMap;
import java.util.List;

import com.l2js.Config;
import com.l2js.util.L2Properties;
import com.l2js.util.StringUtil;

/**
 * @author L0ngh0rn
 */
public class ConfigDM extends Config
{
	private final static String path = L2JS_DM_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			DM_EVENT_ENABLED = getBoolean(properties, "DMEventEnabled", false);
			DM_EVENT_IN_INSTANCE = getBoolean(properties, "DMEventInInstance", false);
			DM_EVENT_INSTANCE_FILE = getString(properties, "DMEventInstanceFile", "coliseum.xml");
			DM_EVENT_INTERVAL = getString(properties, "DMEventInterval", "8:00,14:00,20:00,2:00").split(",");
			String[] timeParticipation = getString(properties, "DMEventParticipationTime", "01:00:00").split(":");
			Long time = 0L;
			time += Long.parseLong(timeParticipation[0]) * 3600L;
			time += Long.parseLong(timeParticipation[1]) * 60L;
			time += Long.parseLong(timeParticipation[2]);
			DM_EVENT_PARTICIPATION_TIME = time * 1000L;
			DM_EVENT_RUNNING_TIME = getInt(properties, "DMEventRunningTime", 1800);
			DM_EVENT_PARTICIPATION_NPC_ID = getInt(properties, "DMEventParticipationNpcId", 0);
			DM_SHOW_TOP_RANK = getBoolean(properties, "DMShowTopRank", false);
			DM_TOP_RANK = getInt(properties, "DMTopRank", 10);
			if (DM_EVENT_PARTICIPATION_NPC_ID == 0)
			{
				DM_EVENT_ENABLED = false;
				_log.warning("DMEventEngine[load()]: invalid config property -> DMEventParticipationNpcId");
			}
			else
			{
				String[] propertySplit = getString(properties, "DMEventParticipationNpcCoordinates", "0,0,0")
						.split(",");
				if (propertySplit.length < 3)
				{
					DM_EVENT_ENABLED = false;
					_log.warning("DMEventEngine[load()]: invalid config property -> DMEventParticipationNpcCoordinates");
				}
				else
				{
					if (DM_EVENT_ENABLED)
					{
						DM_EVENT_REWARDS = new HashMap<Integer, List<int[]>>();
						DM_DOORS_IDS_TO_OPEN = new ArrayList<Integer>();
						DM_DOORS_IDS_TO_CLOSE = new ArrayList<Integer>();
						DM_EVENT_PLAYER_COORDINATES = new ArrayList<int[]>();

						DM_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
						DM_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
						DM_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
						DM_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);

						if (propertySplit.length == 4)
							DM_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
						DM_EVENT_MIN_PLAYERS = getInt(properties, "DMEventMinPlayers", 1);
						DM_EVENT_MAX_PLAYERS = getInt(properties, "DMEventMaxPlayers", 20);
						DM_EVENT_MIN_LVL = (byte) getInt(properties, "DMEventMinPlayerLevel", 1);
						DM_EVENT_MAX_LVL = (byte) getInt(properties, "DMEventMaxPlayerLevel", 80);
						DM_EVENT_RESPAWN_TELEPORT_DELAY = getInt(properties, "DMEventRespawnTeleportDelay", 20);
						DM_EVENT_START_LEAVE_TELEPORT_DELAY = getInt(properties, "DMEventStartLeaveTeleportDelay", 20);
						DM_EVENT_EFFECTS_REMOVAL = getInt(properties, "DMEventEffectsRemoval", 0);
						DM_ALLOW_VOICED_COMMAND = Config.getBoolean(properties, "DMAllowVoicedInfoCommand", true);
						DM_EVENT_MULTIBOX_PROTECTION_ENABLE = getBoolean(properties, "DMEventMultiBoxEnable", false);
						DM_EVENT_NUMBER_BOX_REGISTER = getInt(properties, "DMEventNumberBoxRegister", 1);
						propertySplit = getString(properties, "DMEventParticipationFee", "0,0").split(",");
						try
						{
							DM_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
							DM_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
						}
						catch (NumberFormatException nfe)
						{
							if (propertySplit.length > 0)
								_log.warning("DMEventEngine[load()]: invalid config property -> DMEventParticipationFee");
						}

						DM_REWARD_FIRST_PLAYERS = getInt(properties, "DMRewardFirstPlayers", 3);

						propertySplit = getString(properties, "DMEventReward", "57,100000;5575,5000|57,50000|57,25000")
								.split("\\|");
						int i = 1;
						if (DM_REWARD_FIRST_PLAYERS < propertySplit.length)
							_log.warning("DMEventEngine[load()]: invalid config property -> DMRewardFirstPlayers < DMEventReward");
						else
						{
							for (String pos : propertySplit)
							{
								List<int[]> value = new ArrayList<int[]>();
								String[] rewardSplit = pos.split("\\;");
								for (String rewards : rewardSplit)
								{
									String[] reward = rewards.split("\\,");
									if (reward.length != 2)
										_log.warning(StringUtil.concat(
												"DMEventEngine[load()]: invalid config property -> DMEventReward \"",
												pos, "\""));
									else
									{
										try
										{
											value.add(new int[]
											{
													Integer.parseInt(reward[0]), Integer.parseInt(reward[1])
											});
										}
										catch (NumberFormatException nfe)
										{
											_log.warning(StringUtil
													.concat("DMEventEngine[load()]: invalid config property -> DMEventReward \"",
															pos, "\""));
										}
									}

									try
									{
										if (value.isEmpty())
											DM_EVENT_REWARDS.put(i, DM_EVENT_REWARDS.get(i - 1));
										else
											DM_EVENT_REWARDS.put(i, value);
									}
									catch (Exception e)
									{
										_log.warning("DMEventEngine[load()]: invalid config property -> DMEventReward array index out of bounds (1)");
										e.printStackTrace();
									}
									i++;
								}
							}

							int countPosRewards = DM_EVENT_REWARDS.size();
							if (countPosRewards < DM_REWARD_FIRST_PLAYERS)
							{
								for (i = countPosRewards + 1; i <= DM_REWARD_FIRST_PLAYERS; i++)
								{
									try
									{
										DM_EVENT_REWARDS.put(i, DM_EVENT_REWARDS.get(i - 1));
									}
									catch (Exception e)
									{
										_log.warning("DMEventEngine[load()]: invalid config property -> DMEventReward array index out of bounds (2)");
										e.printStackTrace();
									}
								}
							}
						}

						propertySplit = getString(properties, "DMEventPlayerCoordinates", "0,0,0").split(";");
						for (String coordPlayer : propertySplit)
						{
							String[] coordSplit = coordPlayer.split(",");
							if (coordSplit.length != 3)
								_log.warning(StringUtil
										.concat("DMEventEngine[load()]: invalid config property -> DMEventPlayerCoordinates \"",
												coordPlayer, "\""));
							else
							{
								try
								{
									DM_EVENT_PLAYER_COORDINATES.add(new int[]
									{
											Integer.parseInt(coordSplit[0]), Integer.parseInt(coordSplit[1]),
											Integer.parseInt(coordSplit[2])
									});
								}
								catch (NumberFormatException nfe)
								{
									if (!coordPlayer.isEmpty())
										_log.warning(StringUtil
												.concat("DMEventEngine[load()]: invalid config property -> DMEventPlayerCoordinates \"",
														coordPlayer, "\""));
								}
							}
						}

						DM_EVENT_SCROLL_ALLOWED = getBoolean(properties, "DMEventScrollsAllowed", false);
						DM_EVENT_POTIONS_ALLOWED = getBoolean(properties, "DMEventPotionsAllowed", false);
						DM_EVENT_SUMMON_BY_ITEM_ALLOWED = getBoolean(properties, "DMEventSummonByItemAllowed", false);
						DM_REWARD_PLAYERS_TIE = getBoolean(properties, "DMRewardPlayersTie", false);
						DM_EVENT_HIDE_NAME = getBoolean(properties, "DMEventHideName", true);
						DM_COLOR_TITLE = getIntDecode(properties, "DMColorTitle", "50D6FF");
						DM_COLOR_NAME = getIntDecode(properties, "DMColorName", "1509FF");

						propertySplit = getString(properties, "DMDoorsToOpen", "").split(";");
						for (String door : propertySplit)
						{
							try
							{
								DM_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
							}
							catch (NumberFormatException nfe)
							{
								if (!door.isEmpty())
									_log.warning(StringUtil.concat(
											"DMEventEngine[load()]: invalid config property -> DMDoorsToOpen \"", door,
											"\""));
							}
						}

						propertySplit = getString(properties, "DMDoorsToClose", "").split(";");
						for (String door : propertySplit)
						{
							try
							{
								DM_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
							}
							catch (NumberFormatException nfe)
							{
								if (!door.isEmpty())
									_log.warning(StringUtil.concat(
											"DMEventEngine[load()]: invalid config property -> DMDoorsToClose \"",
											door, "\""));
							}
						}

						propertySplit = getString(properties, "DMEventFighterBuffs", "").split(";");
						if (!propertySplit[0].isEmpty())
						{
							DM_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(propertySplit.length);
							for (String skill : propertySplit)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
									_log.warning(StringUtil.concat(
											"DMEventEngine[load()]: invalid config property -> DMEventFighterBuffs \"",
											skill, "\""));
								else
								{
									try
									{
										DM_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]),
												Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
											_log.warning(StringUtil
													.concat("DMEventEngine[load()]: invalid config property -> DMEventFighterBuffs \"",
															skill, "\""));
									}
								}
							}
						}

						propertySplit = getString(properties, "DMEventMageBuffs", "").split(";");
						if (!propertySplit[0].isEmpty())
						{
							DM_EVENT_MAGE_BUFFS = new TIntIntHashMap(propertySplit.length);
							for (String skill : propertySplit)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
									_log.warning(StringUtil.concat(
											"DMEventEngine[load()]: invalid config property -> DMEventMageBuffs \"",
											skill, "\""));
								else
								{
									try
									{
										DM_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]),
												Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
											_log.warning(StringUtil
													.concat("DMEventEngine[load()]: invalid config property -> DMEventMageBuffs \"",
															skill, "\""));
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
