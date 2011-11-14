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
public class ConfigLM extends Config
{
	private final static String path = L2JS_LM_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			LM_EVENT_ENABLED = getBoolean(properties, "LMEventEnabled", false);
			LM_EVENT_IN_INSTANCE = getBoolean(properties, "LMEventInInstance", false);
			LM_EVENT_INSTANCE_FILE = getString(properties, "LMEventInstanceFile", "coliseum.xml");
			LM_EVENT_INTERVAL = getString(properties, "LMEventInterval", "8:00,14:00,20:00,2:00").split(",");
			String[] timeParticipation = getString(properties, "LMEventParticipationTime", "01:00:00").split(":");
			Long time = 0L;
			time += Long.parseLong(timeParticipation[0]) * 3600L;
			time += Long.parseLong(timeParticipation[1]) * 60L;
			time += Long.parseLong(timeParticipation[2]);
			LM_EVENT_PARTICIPATION_TIME = time * 1000L;
			LM_EVENT_RUNNING_TIME = getInt(properties, "LMEventRunningTime", 1800);
			LM_EVENT_PARTICIPATION_NPC_ID = getInt(properties, "LMEventParticipationNpcId", 0);
			short credits = getShort(properties, "LMEventPlayerCredits", Short.parseShort("1"));
			LM_EVENT_PLAYER_CREDITS = (credits > 0 ? credits : 1);
			if (LM_EVENT_PARTICIPATION_NPC_ID == 0)
			{
				LM_EVENT_ENABLED = false;
				_log.warning("LMEventEngine[load()]: invalid config property -> LMEventParticipationNpcId");
			}
			else
			{
				String[] propertySplit = getString(properties, "LMEventParticipationNpcCoordinates", "0,0,0")
						.split(",");
				if (propertySplit.length < 3)
				{
					LM_EVENT_ENABLED = false;
					_log.warning("LMEventEngine[load()]: invalid config property -> LMEventParticipationNpcCoordinates");
				}
				else
				{
					if (LM_EVENT_ENABLED)
					{
						LM_EVENT_REWARDS = new ArrayList<int[]>();
						LM_DOORS_IDS_TO_OPEN = new ArrayList<Integer>();
						LM_DOORS_IDS_TO_CLOSE = new ArrayList<Integer>();
						LM_EVENT_PLAYER_COORDINATES = new ArrayList<int[]>();

						LM_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
						LM_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
						LM_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
						LM_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);

						if (propertySplit.length == 4)
							LM_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
						LM_EVENT_MIN_PLAYERS = getInt(properties, "LMEventMinPlayers", 1);
						LM_EVENT_MAX_PLAYERS = getInt(properties, "LMEventMaxPlayers", 20);
						LM_EVENT_MIN_LVL = (byte) getInt(properties, "LMEventMinPlayerLevel", 1);
						LM_EVENT_MAX_LVL = (byte) getInt(properties, "LMEventMaxPlayerLevel", 80);
						LM_EVENT_RESPAWN_TELEPORT_DELAY = getInt(properties, "LMEventRespawnTeleportDelay", 20);
						LM_EVENT_START_LEAVE_TELEPORT_DELAY = getInt(properties, "LMEventStartLeaveTeleportDelay", 20);
						LM_EVENT_EFFECTS_REMOVAL = getInt(properties, "LMEventEffectsRemoval", 0);
						LM_ALLOW_VOICED_COMMAND = Config.getBoolean(properties, "LMAllowVoicedInfoCommand", true);
						LM_EVENT_MULTIBOX_PROTECTION_ENABLE = getBoolean(properties, "LMEventMultiBoxEnable", false);
						LM_EVENT_NUMBER_BOX_REGISTER = getInt(properties, "LMEventNumberBoxRegister", 1);
						propertySplit = getString(properties, "LMEventParticipationFee", "0,0").split(",");
						try
						{
							LM_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
							LM_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
						}
						catch (NumberFormatException nfe)
						{
							if (propertySplit.length > 0)
								_log.warning("LMEventEngine[load()]: invalid config property -> LMEventParticipationFee");
						}

						propertySplit = getString(properties, "LMEventReward", "57,100000;5575,5000").split("\\;");
						for (String reward : propertySplit)
						{
							String[] rewardSplit = reward.split("\\,");
							try
							{
								LM_EVENT_REWARDS.add(new int[]
								{
										Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])
								});
							}
							catch (NumberFormatException nfe)
							{
								_log.warning("LMEventEngine[load()]: invalid config property -> LM_EVENT_REWARDS");
							}
						}

						propertySplit = getString(properties, "LMEventPlayerCoordinates", "0,0,0").split(";");
						for (String coordPlayer : propertySplit)
						{
							String[] coordSplit = coordPlayer.split(",");
							if (coordSplit.length != 3)
								_log.warning(StringUtil
										.concat("LMEventEngine[load()]: invalid config property -> LMEventPlayerCoordinates \"",
												coordPlayer, "\""));
							else
							{
								try
								{
									LM_EVENT_PLAYER_COORDINATES.add(new int[]
									{
											Integer.parseInt(coordSplit[0]), Integer.parseInt(coordSplit[1]),
											Integer.parseInt(coordSplit[2])
									});
								}
								catch (NumberFormatException nfe)
								{
									if (!coordPlayer.isEmpty())
										_log.warning(StringUtil
												.concat("LMEventEngine[load()]: invalid config property -> LMEventPlayerCoordinates \"",
														coordPlayer, "\""));
								}
							}
						}

						LM_EVENT_SCROLL_ALLOWED = getBoolean(properties, "LMEventScrollsAllowed", false);
						LM_EVENT_POTIONS_ALLOWED = getBoolean(properties, "LMEventPotionsAllowed", false);
						LM_EVENT_SUMMON_BY_ITEM_ALLOWED = getBoolean(properties, "LMEventSummonByItemAllowed", false);
						LM_REWARD_PLAYERS_TIE = getBoolean(properties, "LMRewardPlayersTie", false);
						LM_EVENT_HIDE_NAME = getBoolean(properties, "LMEventHideName", true);
						LM_COLOR_TITLE = getIntDecode(properties, "LMColorTitle", "50D6FF");
						LM_COLOR_NAME = getIntDecode(properties, "LMColorName", "1509FF");

						propertySplit = getString(properties, "LMDoorsToOpen", "").split(";");
						for (String door : propertySplit)
						{
							try
							{
								LM_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
							}
							catch (NumberFormatException nfe)
							{
								if (!door.isEmpty())
									_log.warning(StringUtil.concat(
											"LMEventEngine[load()]: invalid config property -> LMDoorsToOpen \"", door,
											"\""));
							}
						}

						propertySplit = getString(properties, "LMDoorsToClose", "").split(";");
						for (String door : propertySplit)
						{
							try
							{
								LM_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
							}
							catch (NumberFormatException nfe)
							{
								if (!door.isEmpty())
									_log.warning(StringUtil.concat(
											"LMEventEngine[load()]: invalid config property -> LMDoorsToClose \"",
											door, "\""));
							}
						}

						propertySplit = getString(properties, "LMEventFighterBuffs", "").split(";");
						if (!propertySplit[0].isEmpty())
						{
							LM_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(propertySplit.length);
							for (String skill : propertySplit)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
									_log.warning(StringUtil.concat(
											"LMEventEngine[load()]: invalid config property -> LMEventFighterBuffs \"",
											skill, "\""));
								else
								{
									try
									{
										LM_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]),
												Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
											_log.warning(StringUtil
													.concat("LMEventEngine[load()]: invalid config property -> LMEventFighterBuffs \"",
															skill, "\""));
									}
								}
							}
						}

						propertySplit = getString(properties, "LMEventMageBuffs", "").split(";");
						if (!propertySplit[0].isEmpty())
						{
							LM_EVENT_MAGE_BUFFS = new TIntIntHashMap(propertySplit.length);
							for (String skill : propertySplit)
							{
								String[] skillSplit = skill.split(",");
								if (skillSplit.length != 2)
									_log.warning(StringUtil.concat(
											"LMEventEngine[load()]: invalid config property -> LMEventMageBuffs \"",
											skill, "\""));
								else
								{
									try
									{
										LM_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]),
												Integer.parseInt(skillSplit[1]));
									}
									catch (NumberFormatException nfe)
									{
										if (!skill.isEmpty())
											_log.warning(StringUtil
													.concat("LMEventEngine[load()]: invalid config property -> LMEventMageBuffs \"",
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
