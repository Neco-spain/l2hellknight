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
package com.l2js;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntFloatHashMap;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2js.config.events.ConfigDM;
import com.l2js.config.events.ConfigEvents;
import com.l2js.config.events.ConfigHitman;
import com.l2js.config.events.ConfigLM;
import com.l2js.config.events.ConfigTvT;
import com.l2js.config.main.*;
import com.l2js.config.mods.ConfigBanking;
import com.l2js.config.mods.ConfigChampion;
import com.l2js.config.mods.ConfigChars;
import com.l2js.config.mods.ConfigChat;
import com.l2js.config.mods.ConfigClasses;
import com.l2js.config.mods.ConfigCustom;
import com.l2js.config.mods.ConfigGraciaSeeds;
import com.l2js.config.mods.ConfigL2jMods;
import com.l2js.config.mods.ConfigMessage;
import com.l2js.config.mods.ConfigOfflineTrade;
import com.l2js.config.mods.ConfigWedding;
import com.l2js.config.network.ConfigCommunityServer;
import com.l2js.config.network.ConfigGameServer;
import com.l2js.config.network.ConfigHexid;
import com.l2js.config.network.ConfigIPConfig;
import com.l2js.config.network.ConfigLoginServer;
import com.l2js.config.network.ConfigTelnet;
import com.l2js.config.scripts.ConfigBufferNpc;
import com.l2js.config.scripts.ConfigRankNpc;
import com.l2js.config.security.ConfigProtectionAdmin;
import com.l2js.config.security.ConfigProtectionBot;
import com.l2js.config.security.ConfigProtectionBox;
import com.l2js.gameserver.util.FloodProtectorConfig;
import com.l2js.util.L2Properties;
import com.l2js.util.StringUtil;
import com.l2js.util.Tools;

public class Config
{
	protected static final Logger _log = Logger.getLogger(Config.class.getName());
	
	public static final String PROJECT_NAME = "L2jS Project";
	
	/*
	 * L2J Property File Definitions
	 */
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/main/FortSiege.ini";
	public static final String TW_CONFIGURATION_FILE = "./config/main/TerritoryWar.ini";
	
	/*
	 * No classification assigned to the following yet
	 */
	public static boolean CHECK_KNOWN;
	public static int NEW_NODE_ID;
	public static int SELECTED_NODE_ID;
	public static int LINKED_NODE_ID;
	public static String NEW_NODE_TYPE;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	
	/*
	 * Attributes used Globally
	 */
	public static String GAME_SERVER_LOGIN_HOST;
	public static int GAME_SERVER_LOGIN_PORT;
	public static boolean DEBUG;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	
	// Folder Events
	// DM.ini
	public static final String L2JS_DM_CONFIG = "./config/events/DM.ini";
	public static boolean DM_EVENT_ENABLED;
	public static boolean DM_EVENT_IN_INSTANCE;
	public static String DM_EVENT_INSTANCE_FILE;
	public static String[] DM_EVENT_INTERVAL;
	public static Long DM_EVENT_PARTICIPATION_TIME;
	public static int DM_EVENT_RUNNING_TIME;
	public static int DM_EVENT_PARTICIPATION_NPC_ID;
	public static int[] DM_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] DM_EVENT_PARTICIPATION_FEE = new int[2];
	public static int DM_EVENT_MIN_PLAYERS;
	public static int DM_EVENT_MAX_PLAYERS;
	public static int DM_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int DM_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static List<int[]> DM_EVENT_PLAYER_COORDINATES;
	public static Map<Integer, List<int[]>> DM_EVENT_REWARDS;
	public static int DM_REWARD_FIRST_PLAYERS;
	public static boolean DM_SHOW_TOP_RANK;
	public static int DM_TOP_RANK;
	public static boolean DM_EVENT_SCROLL_ALLOWED;
	public static boolean DM_EVENT_POTIONS_ALLOWED;
	public static boolean DM_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> DM_DOORS_IDS_TO_OPEN;
	public static List<Integer> DM_DOORS_IDS_TO_CLOSE;
	public static boolean DM_REWARD_PLAYERS_TIE;
	public static boolean DM_EVENT_HIDE_NAME;
	public static Integer DM_COLOR_TITLE;
	public static Integer DM_COLOR_NAME;
	public static byte DM_EVENT_MIN_LVL;
	public static byte DM_EVENT_MAX_LVL;
	public static int DM_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap DM_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap DM_EVENT_MAGE_BUFFS;
	public static boolean DM_ALLOW_VOICED_COMMAND;
	public static boolean DM_EVENT_MULTIBOX_PROTECTION_ENABLE;
	public static int DM_EVENT_NUMBER_BOX_REGISTER;
	
	// Events.ini
	public static final String L2JS_EVENTS_CONFIG = "./config/events/Events.ini";
	public static boolean ENABLE_BLOCK_CHECKER_EVENT;
	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean HBCE_FAIR_PLAY;
	
	// Hitman.ini
	public static final String L2JS_HITMAN_CONFIG = "./config/events/Hitman.ini";
	public static boolean HITMAN_ENABLE_EVENT;
	public static boolean HITMAN_TAKE_KARMA;
	public static boolean HITMAN_ANNOUNCE;
	public static int HITMAN_MAX_PER_PAGE;
	public static List<Integer> HITMAN_CURRENCY;
	public static boolean HITMAN_SAME_TEAM;
	public static int HITMAN_SAVE_TARGET;
	
	// LM.ini
	public static final String L2JS_LM_CONFIG = "./config/events/LM.ini";
	public static boolean LM_EVENT_ENABLED;
	public static boolean LM_EVENT_IN_INSTANCE;
	public static String LM_EVENT_INSTANCE_FILE;
	public static String[] LM_EVENT_INTERVAL;
	public static Long LM_EVENT_PARTICIPATION_TIME;
	public static int LM_EVENT_RUNNING_TIME;
	public static int LM_EVENT_PARTICIPATION_NPC_ID;
	public static short LM_EVENT_PLAYER_CREDITS;
	public static int[] LM_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] LM_EVENT_PARTICIPATION_FEE = new int[2];
	public static int LM_EVENT_MIN_PLAYERS;
	public static int LM_EVENT_MAX_PLAYERS;
	public static int LM_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int LM_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static List<int[]> LM_EVENT_PLAYER_COORDINATES;
	public static List<int[]> LM_EVENT_REWARDS;
	public static boolean LM_EVENT_SCROLL_ALLOWED;
	public static boolean LM_EVENT_POTIONS_ALLOWED;
	public static boolean LM_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> LM_DOORS_IDS_TO_OPEN;
	public static List<Integer> LM_DOORS_IDS_TO_CLOSE;
	public static boolean LM_REWARD_PLAYERS_TIE;
	public static boolean LM_EVENT_HIDE_NAME;
	public static Integer LM_COLOR_TITLE;
	public static Integer LM_COLOR_NAME;
	public static byte LM_EVENT_MIN_LVL;
	public static byte LM_EVENT_MAX_LVL;
	public static int LM_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap LM_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap LM_EVENT_MAGE_BUFFS;
	public static boolean LM_ALLOW_VOICED_COMMAND;
	public static boolean LM_EVENT_MULTIBOX_PROTECTION_ENABLE;
	public static int LM_EVENT_NUMBER_BOX_REGISTER;
	
	// TvT.ini
	public static final String L2JS_TVT_CONFIG = "./config/events/TvT.ini";
	public static boolean TVT_EVENT_ENABLED;
	public static boolean TVT_EVENT_IN_INSTANCE;
	public static String TVT_EVENT_INSTANCE_FILE;
	public static String[] TVT_EVENT_INTERVAL;
	public static Long TVT_EVENT_PARTICIPATION_TIME;
	public static int TVT_EVENT_RUNNING_TIME;
	public static int TVT_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static String TVT_EVENT_TEAM_1_NAME;
	public static int[] TVT_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String TVT_EVENT_TEAM_2_NAME;
	public static int[] TVT_EVENT_TEAM_2_COORDINATES = new int[3];
	public static List<int[]> TVT_EVENT_REWARDS;
	public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> TVT_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_DOORS_IDS_TO_CLOSE;
	public static boolean TVT_REWARD_TEAM_TIE;
	public static boolean TVT_REWARD_PLAYER;
	public static byte TVT_EVENT_MIN_LVL;
	public static byte TVT_EVENT_MAX_LVL;
	public static int TVT_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap TVT_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap TVT_EVENT_MAGE_BUFFS;
	public static boolean TVT_ALLOW_VOICED_COMMAND;
	public static boolean TVT_EVENT_MULTIBOX_PROTECTION_ENABLE;
	public static int TVT_EVENT_NUMBER_BOX_REGISTER;
	
	// Folder Main
	// Character.ini
	public static final String CHARACTER_CONFIG = "./config/main/Character.ini";
	public static int MASTERACCESS_LEVEL;
	public static int MASTERACCESS_NAME_COLOR;
	public static int MASTERACCESS_TITLE_COLOR;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean DECREASE_SKILL_LEVEL;
	public static double ALT_WEIGHT_LIMIT;
	public static int RUN_SPD_BOOST;
	public static int DEATH_PENALTY_CHANCE;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean ALT_GAME_TIREDNESS;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static TIntIntHashMap SKILL_DURATION_LIST;
	public static boolean ENABLE_MODIFY_SKILL_REUSE;
	public static TIntIntHashMap SKILL_REUSE_LIST;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FS_SKILLS;
	public static boolean AUTO_LOOT_HERBS;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte TRIGGERED_BUFFS_MAX_AMOUNT;
	public static byte DANCES_MAX_AMOUNT;
	public static boolean DANCE_CANCEL_BUFF;
	public static boolean DANCE_CONSUME_ADDITIONAL_MP;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean EFFECT_CANCELING;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALLOW_CLASS_MASTERS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ALTERNATE_CLASS_MASTER;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static boolean ALLOW_TRANSFORM_WITHOUT_QUEST;
	public static boolean RESTORE_SERVITOR_ON_RECONNECT;
	public static boolean RESTORE_PET_ON_RECONNECT;
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION;
	public static int MIN_DEBUFF_CHANCE;
	public static int MAX_DEBUFF_CHANCE;
	public static byte MAX_SUBCLASS;
	public static byte BASE_SUBCLASS_LEVEL;
	public static byte MAX_SUBCLASS_LEVEL;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int ALT_FREIGHT_SLOTS;
	public static int ALT_FREIGHT_PRIECE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static int MAX_PERSONAL_FAME_POINTS;
	public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
	public static boolean FAME_FOR_DEAD_PLAYERS;
	public static boolean IS_CRAFTING_ENABLED;
	public static boolean CRAFT_MASTERWORK;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_LEAVE_PARTY_LEADER;
	public static long STARTING_ADENA;
	public static byte STARTING_LEVEL;
	public static int STARTING_SP;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAIDS;
	public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
	public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	public static int UNSTUCK_INTERVAL;
	public static int TELEPORT_WATCHDOG_TIMEOUT;
	public static int PLAYER_SPAWN_PROTECTION;
	public static ArrayList<Integer> SPAWN_PROTECTION_ALLOWED_ITEMS;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
	public static boolean OFFSET_ON_TELEPORT_ENABLED;
	public static int MAX_OFFSET_ON_TELEPORT;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean ALLOW_SUMMON_TO_INSTANCE;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static int DELETE_DAYS;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static boolean DISABLE_TUTORIAL;
	public static boolean EXPERTISE_PENALTY;
	public static boolean STORE_RECIPE_SHOPLIST;
	public static boolean STORE_UI_SETTINGS;
	public static String[] FORBIDDEN_NAMES;
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_CHANCE_ELEMENT_STONE;
	public static int ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static int ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static int ENCHANT_CHANCE_ELEMENT_ENERGY;
	public static int BLESSED_ENCHANT_CHANCE_WEAPON;
	public static int BLESSED_ENCHANT_CHANCE_ARMOR;
	public static int BLESSED_ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int[] ENCHANT_BLACKLIST;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	public static int[] AUGMENTATION_BLACKLIST;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean ENABLE_VITALITY;
	public static boolean RECOVER_VITALITY_ON_RECONNECT;
	public static int STARTING_VITALITY_POINTS;
	public static int MAX_ITEM_IN_PACKET;
	public static int FEE_DELETE_TRANSFER_SKILLS;
	public static int FEE_DELETE_SUBCLASS_SKILLS;
	
	// ChatFilter.txt
	public static final String CHAT_FILTER_CONFIG = "./config/main/ChatFilter.txt";
	public static ArrayList<String> FILTER_LIST;
	
	// ConquerableHallSiege.ini
	public static final String CH_SIEGE_CONFIG = "./config/main/ConquerableHallSiege.ini";
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;
	
	// EMail.ini
	public static final String EMAIL_CONFIG_FILE = "./config/main/EMail.ini";
	public static String EMAIL_SERVERINFO_NAME;
	public static String EMAIL_SERVERINFO_ADDRESS;
	public static boolean EMAIL_SYS_ENABLED;
	public static String EMAIL_SYS_HOST;
	public static int EMAIL_SYS_PORT;
	public static boolean EMAIL_SYS_SMTP_AUTH;
	public static String EMAIL_SYS_FACTORY;
	public static boolean EMAIL_SYS_FACTORY_CALLBACK;
	public static String EMAIL_SYS_USERNAME;
	public static String EMAIL_SYS_PASSWORD;
	public static String EMAIL_SYS_ADDRESS;
	public static String EMAIL_SYS_SELECTQUERY;
	public static String EMAIL_SYS_DBFIELD;
	
	// Feature.ini
	public static final String FEATURE_CONFIG = "./config/main/Feature.ini";
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	public static boolean CH_BUFF_FREE;
	public static long CS_TELE_FEE_RATIO;
	public static int CS_TELE1_FEE;
	public static int CS_TELE2_FEE;
	public static long CS_MPREG_FEE_RATIO;
	public static int CS_MPREG1_FEE;
	public static int CS_MPREG2_FEE;
	public static int CS_MPREG3_FEE;
	public static int CS_MPREG4_FEE;
	public static long CS_HPREG_FEE_RATIO;
	public static int CS_HPREG1_FEE;
	public static int CS_HPREG2_FEE;
	public static int CS_HPREG3_FEE;
	public static int CS_HPREG4_FEE;
	public static int CS_HPREG5_FEE;
	public static long CS_EXPREG_FEE_RATIO;
	public static int CS_EXPREG1_FEE;
	public static int CS_EXPREG2_FEE;
	public static int CS_EXPREG3_FEE;
	public static int CS_EXPREG4_FEE;
	public static long CS_SUPPORT_FEE_RATIO;
	public static int CS_SUPPORT1_FEE;
	public static int CS_SUPPORT2_FEE;
	public static int CS_SUPPORT3_FEE;
	public static int CS_SUPPORT4_FEE;
	public static List<String> CL_SET_SIEGE_TIME_LIST;
	public static List<Integer> SIEGE_HOUR_LIST_MORNING;
	public static List<Integer> SIEGE_HOUR_LIST_AFTERNOON;
	public static long FS_TELE_FEE_RATIO;
	public static int FS_TELE1_FEE;
	public static int FS_TELE2_FEE;
	public static long FS_MPREG_FEE_RATIO;
	public static int FS_MPREG1_FEE;
	public static int FS_MPREG2_FEE;
	public static long FS_HPREG_FEE_RATIO;
	public static int FS_HPREG1_FEE;
	public static int FS_HPREG2_FEE;
	public static long FS_EXPREG_FEE_RATIO;
	public static int FS_EXPREG1_FEE;
	public static int FS_EXPREG2_FEE;
	public static long FS_SUPPORT_FEE_RATIO;
	public static int FS_SUPPORT1_FEE;
	public static int FS_SUPPORT2_FEE;
	public static int FS_BLOOD_OATH_COUNT;
	public static int FS_UPDATE_FRQ;
	public static int FS_MAX_SUPPLY_LEVEL;
	public static int FS_FEE_FOR_CASTLE;
	public static int FS_MAX_OWN_TIME;
	public static boolean ALT_GAME_CASTLE_DAWN;
	public static boolean ALT_GAME_CASTLE_DUSK;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	public static double ALT_SIEGE_DAWN_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DAWN_GATES_MDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_MDEF_MULT;
	public static boolean ALT_STRICT_SEVENSIGNS;
	public static boolean ALT_SEVENSIGNS_LAZY_UPDATE;
	public static int TAKE_FORT_POINTS;
	public static int LOOSE_FORT_POINTS;
	public static int TAKE_CASTLE_POINTS;
	public static int LOOSE_CASTLE_POINTS;
	public static int CASTLE_DEFENDED_POINTS;
	public static int FESTIVAL_WIN_POINTS;
	public static int HERO_POINTS;
	public static int ROYAL_GUARD_COST;
	public static int KNIGHT_UNIT_COST;
	public static int KNIGHT_REINFORCE_COST;
	public static int BALLISTA_POINTS;
	public static int BLOODALLIANCE_POINTS;
	public static int BLOODOATH_POINTS;
	public static int KNIGHTSEPAULETTE_POINTS;
	public static int REPUTATION_SCORE_PER_KILL;
	public static int JOIN_ACADEMY_MIN_REP_SCORE;
	public static int JOIN_ACADEMY_MAX_REP_SCORE;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	public static int CLAN_LEVEL_6_REQUIREMENT;
	public static int CLAN_LEVEL_7_REQUIREMENT;
	public static int CLAN_LEVEL_8_REQUIREMENT;
	public static int CLAN_LEVEL_9_REQUIREMENT;
	public static int CLAN_LEVEL_10_REQUIREMENT;
	public static int CLAN_LEVEL_11_REQUIREMENT;
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	
	// FloodProtector.ini
	public static final String FLOOD_PROTECTOR_CONFIG = "./config/main/FloodProtector.ini";
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SENDMAIL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_AUCTION;
	
	// General.ini
	public static final String GENERAL_CONFIG = "./config/main/General.ini";
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean DISPLAY_SERVER_DEV;
	public static boolean DISPLAY_SERVER_VERSION;
	public static boolean SERVER_LIST_BRACKET;
	public static int SERVER_LIST_TYPE;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_GMONLY;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean GM_STARTUP_DIET_MODE;
	public static String GM_ADMIN_MENU_STYLE;
	public static boolean GM_ITEM_RESTRICTION;
	public static boolean GM_SKILL_RESTRICTION;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_GIVE_SPECIAL_SKILLS;
	public static boolean GM_GIVE_SPECIAL_AURA_SKILLS;
	public static boolean BYPASS_VALIDATION;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean GAMEGUARD_PROHIBITACTION;
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	public static boolean LOG_ITEMS_SMALL_LOG;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean LOG_GAME_DAMAGE;
	public static int LOG_GAME_DAMAGE_THRESHOLD;
	public static boolean SKILL_CHECK_ENABLE;
	public static boolean SKILL_CHECK_REMOVE;
	public static boolean SKILL_CHECK_GM;
	public static boolean ACCEPT_GEOEDITOR_CONN;
	public static boolean ALT_DEV_NO_HANDLERS;
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	public static int CLIENT_PACKET_QUEUE_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	public static boolean DEADLOCK_DETECTOR;
	public static int DEADLOCK_CHECK_INTERVAL;
	public static boolean RESTART_ON_DEADLOCK;
	public static boolean ALLOW_DISCARDITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static TIntArrayList LIST_PROTECTED_ITEMS;
	public static int CHAR_STORE_INTERVAL;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean LAZY_CACHE;
	public static boolean CACHE_CHAR_NAMES;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static int COORD_SYNCHRONIZE;
	public static boolean ENABLE_FALLING_DAMAGE;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static int WORLD_X_MIN;
	public static int WORLD_X_MAX;
	public static int WORLD_Y_MIN;
	public static int WORLD_Y_MAX;
	public static int GEODATA;
	public static boolean GEODATA_CELLFINDING;
	public static String PATHFIND_BUFFERS;
	public static float LOW_WEIGHT;
	public static float MEDIUM_WEIGHT;
	public static float HIGH_WEIGHT;
	public static boolean ADVANCED_DIAGONAL_STRATEGY;
	public static float DIAGONAL_WEIGHT;
	public static int MAX_POSTFILTER_PASSES;
	public static boolean DEBUG_PATH;
	public static boolean FORCE_GEODATA;
	public static boolean MOVE_BASED_KNOWNLIST;
	public static long KNOWNLIST_UPDATE_INTERVAL;
	public static int ZONE_TOWN;
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_REFUND;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_ATTACHMENTS;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ALLOW_NPC_WALKERS;
	public static boolean ALLOW_PET_WALKERS;
	public static boolean SERVER_NEWS;
	public static int COMMUNITY_TYPE;
	public static boolean BBS_SHOW_PLAYERLIST;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int[] BAN_CHAT_CHANNELS;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static boolean JAIL_DISABLE_TRANSACTION;
	public static int ALT_BIRTHDAY_GIFT;
	public static String ALT_BIRTHDAY_MAIL_SUBJECT;
	public static String ALT_BIRTHDAY_MAIL_TEXT;
	
	// GrandBoss.ini
	public static final String GRAND_BOSS_CONFIG = "./config/main/GrandBoss.ini";
	public static int Antharas_Wait_Time;
	public static int Valakas_Wait_Time;
	public static int Interval_Of_Antharas_Spawn;
	public static int Random_Of_Antharas_Spawn;
	public static int Interval_Of_Valakas_Spawn;
	public static int Random_Of_Valakas_Spawn;
	public static int Interval_Of_Baium_Spawn;
	public static int Random_Of_Baium_Spawn;
	public static int Interval_Of_Core_Spawn;
	public static int Random_Of_Core_Spawn;
	public static int Interval_Of_Orfen_Spawn;
	public static int Random_Of_Orfen_Spawn;
	public static int Interval_Of_QueenAnt_Spawn;
	public static int Random_Of_QueenAnt_Spawn;
	public static int Interval_Of_Zaken_Spawn;
	public static int Random_Of_Zaken_Spawn;
	public static int Interval_Of_Frintezza_Spawn;
	public static int Random_Of_Frintezza_Spawn;
	
	// IDFactory.ini
	public static final String ID_FACTORY_CONFIG = "./config/main/IDFactory.ini";
	public static ObjectMapType MAP_TYPE;
	public static ObjectSetType SET_TYPE;
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	
	// MMO.ini
	public static final String MMO_CONFIG = "./config/main/MMO.ini";
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	
	// NPC.ini
	public static final String NPC_CONFIG = "./config/main/NPC.ini";
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static int MAX_DRIFT_RANGE;
	public static boolean DEEPBLUE_DROP_RULES;
	public static boolean DEEPBLUE_DROP_RULES_RAID;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_CREST_WITHOUT_QUEST;
	public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
	public static int MIN_NPC_LVL_DMG_PENALTY;
	public static TIntFloatHashMap NPC_DMG_PENALTY;
	public static TIntFloatHashMap NPC_CRIT_DMG_PENALTY;
	public static TIntFloatHashMap NPC_SKILL_DMG_PENALTY;
	public static int MIN_NPC_LVL_MAGIC_PENALTY;
	public static TIntFloatHashMap NPC_SKILL_CHANCE_PENALTY;
	public static int DECAY_TIME_TASK;
	public static int NPC_DECAY_TIME;
	public static int RAID_BOSS_DECAY_TIME;
	public static int SPOILED_DECAY_TIME;
	public static boolean ENABLE_DROP_VITALITY_HERBS;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static TIntArrayList LIST_PET_RENT_NPC;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_PDEFENCE_MULTIPLIER;
	public static double RAID_MDEFENCE_MULTIPLIER;
	public static double RAID_PATTACK_MULTIPLIER;
	public static double RAID_MATTACK_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RAID_DISABLE_CURSE;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;
	public static TIntArrayList NON_TALKING_NPCS;
	
	// Olympiad.ini
	public static final String OLYMPIAD_CONFIG = "./config/main/Olympiad.ini";
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_TEAMS;
	public static int ALT_OLY_REG_DISPLAY;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int[][] ALT_OLY_TEAM_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_DIVIDER_CLASSED;
	public static int ALT_OLY_DIVIDER_NON_CLASSED;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static TIntArrayList LIST_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
	
	// PvP.ini
	public static final String PVP_CONFIG = "./config/main/PvP.ini";
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	
	// Rates.ini
	public static final String RATES_CONFIG = "./config/main/Rates.ini";
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_EXTR_FISH;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_ITEMS_BY_RAID;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static float RATE_QUEST_DROP;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static TIntFloatHashMap RATE_DROP_ITEMS_ID;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_HP_HERBS;
	public static float RATE_DROP_MP_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static double[] PLAYER_XP_PERCENT_LOST;
	public static float RATE_VITALITY_LEVEL_1;
	public static float RATE_VITALITY_LEVEL_2;
	public static float RATE_VITALITY_LEVEL_3;
	public static float RATE_VITALITY_LEVEL_4;
	public static float RATE_RECOVERY_VITALITY_PEACE_ZONE;
	public static float RATE_DROP_VITALITY_HERBS;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_RECOVERY_ON_RECONNECT;
	
	// Security.ini
	public static final String SECURITY_CONFIG_FILE = "./config/main/Security.ini";
	public static boolean SECOND_AUTH_ENABLED;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;
	
	// Siege.ini
	public static final String SIEGE_CONFIGURATION_FILE = "./config/main/Siege.ini";
	public static int GLUDIO_MAX_MERCENARIES;
	public static int DION_MAX_MERCENARIES;
	public static int GIRAN_MAX_MERCENARIES;
	public static int OREN_MAX_MERCENARIES;
	public static int ADEN_MAX_MERCENARIES;
	public static int INNADRIL_MAX_MERCENARIES;
	public static int GODDARD_MAX_MERCENARIES;
	public static int RUNE_MAX_MERCENARIES;
	public static int SCHUTTGART_MAX_MERCENARIES;
	
	// Folder Mods
	// Banking.ini
	public static final String L2JS_BANKING_CONFIG = "./config/mods/Banking.ini";
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	
	// Champion.ini
	public static final String L2JS_CHAMPION_CONFIG = "./config/mods/Champion.ini";
	public static boolean CHAMPION_ENABLE;
	public static boolean CHAMPION_PASSIVE;
	public static int CHAMPION_FREQUENCY;
	public static String CHAMP_TITLE;
	public static int CHAMP_MIN_LVL;
	public static int CHAMP_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static float CHAMPION_ADENAS_REWARDS;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	public static boolean CHAMPION_ENABLE_VITALITY;
	public static boolean CHAMPION_ENABLE_IN_INSTANCES;
	public static int CHAMPION_ENABLE_AURA;
	
	// Chars.ini
	public static final String L2JS_CHARS_CONFIG = "./config/mods/Chars.ini";
	public static int FAKE_PLAYERS;
	public static boolean ALLOW_NEW_CHARACTER_TITLE;
	public static String NEW_CHARACTER_TITLE;
	public static int CHARACTER_COLOR_NAME;
	public static int CHARACTER_COLOR_TITLE;
	public static int NO_STORE_ZONES_AROUND_NPCS_RADIUS;
	public static int NO_STORE_ZONES_AROUND_PCS_RADIUS;
	public static boolean DONATOR_SEE_NAME_COLOR;
	public static int DONATOR_NAME_COLOR;
	public static boolean DONATOR_SEE_TITLE_COLOR;
	public static int DONATOR_TITLE_COLOR;
	public static String DONATOR_WELCOME_MESSAGE;
	
	// Chat.ini
	public static final String L2JS_CHAT_CONFIG = "./config/mods/Chat.ini";
	public static boolean CHAT_ADMIN;
	public static boolean ENABLE_PVP_CHAT_SHOUT_BLOCK;
	public static int AMOUNT_PVP_CHAT_SHOUT;
	public static boolean ENABLE_PVP_CHAT_TRADE_BLOCK;
	public static int AMOUNT_PVP_CHAT_TRADE;
	public static boolean ENABLE_PVP_CHAT_HERO_BLOCK;
	public static int AMOUNT_PVP_CHAT_HERO;
	public static int PUNISHED_AMOUNT_PVP_CHAT_SHOUT;
	public static int PUNISHED_AMOUNT_PVP_CHAT_TRADE;
	public static int PUNISHED_AMOUNT_PVP_CHAT_HERO;
	
	// Classes.ini
	public static final String L2JS_CLASSES_CONFIG = "./config/mods/Classes.ini";
	public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
	public static boolean ENABLE_CLASS_BALANCE_SYSTEM;
	public static boolean ENABLE_CLASS_VS_CLASS_SYSTEM;
	
	// Custom.ini
	public static final String L2JS_CUSTOM_CONFIG = "./config/mods/Custom.ini";
	public static String SERVER_NAME;
	public static boolean ALLOW_VALID_ENCHANT;
	public static boolean ALLOW_VALID_EQUIP_ITEM;
	public static boolean DESTROY_ENCHANT_ITEM;
	public static boolean PUNISH_PLAYER;
	public static boolean PVP_ALLOW_REWARD;
	public static String[] PVP_REWARD;
	public static boolean ALLOW_PVP_COLOR_SYSTEM;
	public static boolean ALLOW_PVP_COLOR_NAME;
	public static boolean ALLOW_PVP_COLOR_TITLE;
	public static SystemPvPColor SYSTEM_PVP_COLOR;
	public static boolean AUGMENTATION_WEAPONS_PVP;
	public static boolean ELEMENTAL_ITEM_PVP;
	public static boolean ELEMENTAL_CUSTOM_LEVEL_ENABLE;
	public static int ELEMENTAL_LEVEL_WEAPON;
	public static int ELEMENTAL_LEVEL_ARMOR;
	public static boolean ENTER_HELLBOUND_WITHOUT_QUEST;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE;
	public static boolean CUSTOM_NPC_SKILLS_TABLE;
	public static boolean CUSTOM_ARMORSETS_TABLE;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_DROPLIST_TABLE;
	public static boolean CUSTOM_MERCHANT_TABLES;
	public static boolean CUSTOM_NPCBUFFER_TABLES;
	public static boolean CUSTOM_SKILLS_LOAD;
	public static boolean CUSTOM_ITEMS_LOAD;
	public static boolean CUSTOM_MULTISELL_LOAD;
	public static int SIZE_MESSAGE_HTML_NPC;
	public static int SIZE_MESSAGE_HTML_QUEST;
	public static int SIZE_MESSAGE_SHOW_BOARD;
	public static boolean ALLOW_MANA_POTIONS;
	public static boolean DISABLE_MANA_POTIONS_IN_PVP;
	
	// GraciaSeeds.ini
	public static final String L2JS_GRACIA_SEEDS_CONFIG = "./config/mods/GraciaSeeds.ini";
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	
	// L2jMods.ini
	public static final String L2JS_L2J_MODS_CONFIG = "./config/mods/L2jMods.ini";
	public static boolean ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean DISPLAY_SERVER_TIME;
	public static boolean MULTILANG_ENABLE;
	public static List<String> MULTILANG_ALLOWED = new ArrayList<String>();
	public static String MULTILANG_DEFAULT;
	public static boolean MULTILANG_VOICED_ALLOW;
	public static boolean MULTILANG_SM_ENABLE;
	public static List<String> MULTILANG_SM_ALLOWED = new ArrayList<String>();
	public static boolean L2JMOD_MULTILANG_NS_ENABLE;
	public static List<String> L2JMOD_MULTILANG_NS_ALLOWED = new ArrayList<String>();
	public static boolean DEBUG_VOICE_COMMAND;
	
	// Message.ini
	public static final String L2JS_MESSAGE_CONFIG = "./config/mods/Message.ini";
	public static boolean SERVER_WELCOME_MESSAGE_ENABLE;
	public static String SERVER_WELCOME_MESSAGE;
	public static boolean SCREEN_WELCOME_MESSAGE_ENABLE;
	public static String SCREEN_WELCOME_MESSAGE;
	public static int SCREEN_WELCOME_MESSAGE_TIME;
	public static boolean ONLINE_PLAYERS_AT_STARTUP;
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	public static boolean HERO_ANNOUNCE_LOGIN;
	public static String HERO_MSG_LOGIN;
	public static boolean CASTLE_LORDS_ANNOUNCE;
	public static String CASTLE_LORDS_MSG;
	
	// OfflineTrade.ini
	public static final String L2JS_OFFLINE_TRADE_CONFIG = "./config/mods/OfflineTrade.ini";
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_SET_INVULNERABLE;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	
	// Wedding.ini
	public static final String L2JS_WEDDING_CONFIG = "./config/mods/Wedding.ini";
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_DURATION;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	
	// Folder Network
	// CommunityServer.ini
	public static final String COMMUNITY_SERVER_CONFIG = "./config/network/CommunityServer.ini";
	public static boolean ENABLE_COMMUNITY_BOARD;
	public static String COMMUNITY_SERVER_ADDRESS;
	public static int COMMUNITY_SERVER_PORT;
	public static byte[] COMMUNITY_SERVER_HEX_ID;
	public static int COMMUNITY_SERVER_SQL_DP_ID;
	
	// Hexid.txt
	public static final String HEXID_CONFIG = "./config/network/hexid.txt";
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	
	// GameServer.ini
	public static final String GAME_SERVER_CONFIG = "./config/network/GameServer.ini";
	public static String GAMESERVER_HOSTNAME;
	public static int PORT_GAME;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static int MAXIMUM_ONLINE_USERS;
	public static TIntArrayList PROTOCOL_LIST;
	public static boolean DATABASE_CLEAN_UP;
	public static long CONNECTION_CLOSE_TIME;
	public static boolean PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	public static int REQUEST_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	
	// IPConfig.xml
	public static final String IPCONFIG_CONFIG = "./config/network/IPConfig.xml";
	public static String[] GAME_SERVER_SUBNETS;
	public static String[] GAME_SERVER_HOSTS;
	
	// LoginServer.ini
	public static final String LOGIN_SERVER_CONFIG = "./config/network/LoginServer.ini";
	public static String LOGIN_BIND_ADDRESS;
	public static int PORT_LOGIN;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static boolean LOG_LOGIN_CONTROLLER;
	public static String ROUTER_HOSTNAME;
	public static boolean SHOW_LICENCE;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static File DATAPACK_ROOT;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	
	// Telnet.ini
	public static final String TELNET_CONFIG = "./config/network/Telnet.ini";
	public static boolean IS_TELNET_ENABLED;
	public static int STATUS_PORT;
	public static String STATUS_PW;
	public static String LIST_OF_HOSTS;
	
	// Folder Scripts
	// BufferNpc.ini
	public static final String L2JS_BUFFER_NPC_CONFIG = "./config/scripts/BufferNpc.ini";
	public static int BUFFER_NPC_ID;
	public static int BUFFER_NPC_MIN_LEVEL;
	public static boolean BUFFER_NPC_ENABLE_READY;
	public static boolean BUFFER_NPC_ENABLE_SCHEME;
	public static int BUFFER_NPC_NUMBER_SCHEME;
	public static int BUFFER_NPC_FEE_SCHEME[];
	public static boolean BUFFER_NPC_ENABLE_SELECT;
	public static boolean BUFFER_NPC_ENABLE_PET;
	public static boolean BUFFER_NPC_ENABLE_RECOVER;
	public static boolean BUFFER_NPC_ENABLE_RECOVER_EVENT;
	public static int BUFFER_NPC_FEE_RECOVER[];
	public static boolean BUFFER_NPC_ENABLE_REMOVE;
	public static int BUFFER_NPC_FEE_REMOVE[];
	public static boolean BUFFER_NPC_REMOVE_AMOUNT;
	
	// RankNpc.ini
	public static final String L2JS_RANK_NPC_CONFIG = "./config/scripts/RankNpc.ini";
	public static int RANK_NPC_ID;
	public static int RANK_NPC_MIN_LEVEL;
	public static String[] RANK_NPC_DISABLE_PAGE;
	public static int[] RANK_NPC_LIST_ITEM;
	public static int RANK_NPC_ITEMS_RECORDS;
	public static int[] RANK_NPC_LIST_CLASS;
	public static int RANK_NPC_OLY_RECORDS;
	public static int RANK_NPC_PVP_RECORDS;
	public static int RANK_NPC_PK_RECORDS;
	public static String RANK_NPC_COLOR_A;
	public static String RANK_NPC_COLOR_B;
	public static Long RANK_NPC_RELOAD;
	
	// Folder Security
	// ProtectionAdmin.ini
	public static final String L2JS_PROTECTION_ADMIN = "./config/security/ProtectionAdmin.ini";
	public static boolean ENABLE_SAFE_ADMIN_PROTECTION;
	public static List<String> SAFE_ADMIN_NAMES;
	public static int SAFE_ADMIN_PUNISH;
	public static boolean SAFE_ADMIN_SHOW_ADMIN_ENTER;
	
	// ProtectionBot.ini
	public static final String L2JS_PROTECTION_BOT = "./config/security/ProtectionBot.ini";
	public static boolean BOT_DETECT;
	public static int BOT_PUNISH;
	public static String BLOCK_DATE_FORMAT;
	public static String BLOCK_HOUR_FORMAT;
	
	// ProtectionBox.ini
	public static final String L2JS_PROTECTION_BOX = "./config/security/ProtectionBox.ini";
	public static boolean ANTIFEED_ENABLE;
	public static boolean ANTIFEED_DUALBOX;
	public static boolean ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int ANTIFEED_INTERVAL;
	public static int DUALBOX_CHECK_MAX_PLAYERS_PER_IP;
	public static int DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP;
	public static int DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP;
	public static TIntIntHashMap DUALBOX_CHECK_WHITELIST;
	public static boolean MULTIBOX_PROTECTION_ENABLED;
	public static int MULTIBOX_PROTECTION_CLIENTS_PER_PC;
	public static int MULTIBOX_PROTECTION_PUNISH;
	
	/*
	 * Server Version
	 */
	public static final String SERVER_VERSION_FILE = "./config/version/l2j-version.properties";
	public static String SERVER_VERSION;
	public static String SERVER_BUILD_DATE;
	
	/*
	 * Server Version
	 */
	public static void loadServerVersionConfig()
	{
		_log.info("Loading: " + SERVER_VERSION_FILE);
		try
		{
			L2Properties serverVersion = new L2Properties(SERVER_VERSION_FILE);
			SERVER_VERSION = getString(serverVersion, "version", "Unsupported Custom Version.");
			SERVER_BUILD_DATE = getString(serverVersion, "builddate", "Undefined Date.");
		}
		catch (Exception e)
		{
			_log.warning("Failed to Load " + SERVER_VERSION_FILE + " File.");
			SERVER_VERSION = "Unsupported Custom Version.";
			SERVER_BUILD_DATE = "Undefined Date.";
		}
	}
	
	/*
	 * Datapack Version
	 */
	public static final String DATAPACK_VERSION_FILE = "./config/version/l2jdp-version.properties";
	public static String DATAPACK_VERSION;
	
	/*
	 * Datapack Version
	 */
	public static void loadDatapackVersionConfig()
	{
		_log.info("Loading: " + DATAPACK_VERSION_FILE);
		try
		{
			L2Properties serverVersion = new L2Properties(DATAPACK_VERSION_FILE);
			DATAPACK_VERSION = getString(serverVersion, "version", "Unsupported Custom Version.");
		}
		catch (Exception e)
		{
			_log.warning("Failed to Load " + DATAPACK_VERSION_FILE + " File.");
			DATAPACK_VERSION = "Unsupported Custom Version.";
		}
	}
	
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}
	
	public static enum ObjectMapType
	{
		L2ObjectHashMap,
		WorldObjectMap
	}
	
	public static enum ObjectSetType
	{
		L2ObjectHashSet,
		WorldObjectSet
	}
	
	public static void loadEventsConfigs()
	{
		Tools.printSection("Events");
		ConfigDM.loadConfig();
		ConfigEvents.loadConfig();
		ConfigHitman.loadConfig();
		ConfigLM.loadConfig();
		ConfigTvT.loadConfig();
	}
	
	public static void loadMainConfigs()
	{
		Tools.printSection("Main");
		ConfigFeature.loadConfig();
		ConfigCharacter.loadConfig();
		ConfigMMO.loadConfig();
		ConfigIDFactory.loadConfig();
		ConfigGeneral.loadConfig();
		ConfigFloodProtector.loadConfig();
		ConfigNPC.loadConfig();
		ConfigRates.loadConfig();
		ConfigSiege.loadConfig();
		ConfigPvP.loadConfig();
		ConfigOlympiad.loadConfig();
		ConfigGrandBoss.loadConfig();
		ConfigChatFilter.loadConfig();
		ConfigSecurity.loadConfig();
		ConfigMMO.loadConfig();
		ConfigConquerableHallSiege.loadConfig();
	}
	
	public static void loadModsConfigs()
	{
		Tools.printSection("Mods");
		ConfigBanking.loadConfig();
		ConfigChampion.loadConfig();
		ConfigChars.loadConfig();
		ConfigChat.loadConfig();
		ConfigClasses.loadConfig();
		ConfigCustom.loadConfig();
		ConfigGraciaSeeds.loadConfig();
		ConfigL2jMods.loadConfig();
		ConfigMessage.loadConfig();
		ConfigOfflineTrade.loadConfig();
		ConfigWedding.loadConfig();
	}
	
	public static void loadNetworkConfigs()
	{
		Tools.printSection("Network");
		ConfigCommunityServer.loadConfig();
		ConfigGameServer.loadConfig();
		ConfigHexid.loadConfig();
		ConfigIPConfig.loadConfig();
		ConfigTelnet.loadConfig();
	}
	
	public static void loadScriptConfigs()
	{
		Tools.printSection("Script");
		ConfigBufferNpc.loadConfig();
		ConfigRankNpc.loadConfig();
	}
	
	public static void loadSecurityConfigs()
	{
		Tools.printSection("Security");
		ConfigProtectionAdmin.loadConfig();
		ConfigProtectionBot.loadConfig();
		ConfigProtectionBox.loadConfig();
	}
	
	public static void loadVersionningConfigs()
	{
		Tools.printSection("Versionning");
		loadServerVersionConfig();
		loadDatapackVersionConfig();
	}
	
	public static void loadAll()
	{
		loadNetworkConfigs();
		loadMainConfigs();
		loadModsConfigs();
		loadEventsConfigs();
		loadScriptConfigs();
		loadSecurityConfigs();
		loadVersionningConfigs();
	}
	
	public static void load()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			Tools.printSection("Loading: Game Server");
			loadAll();
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			Tools.printSection("Loading: Login Server");
			Tools.printSection("Network");
			ConfigLoginServer.loadConfig();
			ConfigTelnet.loadConfig();
			Tools.printSection("Main");
			ConfigMMO.loadConfig();
			ConfigEMail.loadConfig();
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set");
		}
	}
	
	public static boolean setParameterValue(String pName, String pValue)
	{
		if (pName.equalsIgnoreCase("RateXp"))
			RATE_XP = Float.parseFloat(pValue);
		else
		{
			try
			{
				// TODO: stupid GB configs...
				if (!pName.startsWith("Interval_") && !pName.startsWith("Random_"))
					pName = pName.toUpperCase();
				Field clazField = Config.class.getField(pName);
				int modifiers = clazField.getModifiers();
				// just in case :)
				if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers))
					throw new SecurityException("Cannot modify non public or non static config!");
				
				if (clazField.getType() == int.class)
				{
					clazField.setInt(clazField, Integer.parseInt(pValue));
				}
				else if (clazField.getType() == short.class)
				{
					clazField.setShort(clazField, Short.parseShort(pValue));
				}
				else if (clazField.getType() == byte.class)
				{
					clazField.setByte(clazField, Byte.parseByte(pValue));
				}
				else if (clazField.getType() == long.class)
				{
					clazField.setLong(clazField, Long.parseLong(pValue));
				}
				else if (clazField.getType() == float.class)
				{
					clazField.setFloat(clazField, Float.parseFloat(pValue));
				}
				else if (clazField.getType() == double.class)
				{
					clazField.setDouble(clazField, Double.parseDouble(pValue));
				}
				else if (clazField.getType() == boolean.class)
				{
					clazField.setBoolean(clazField, Boolean.parseBoolean(pValue));
				}
				else if (clazField.getType() == String.class)
				{
					clazField.set(clazField, pValue);
				}
				else
					return false;
			}
			catch (NoSuchFieldException e)
			{
				return false;
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
				return false;
			}
		}
		return true;
	}
	
	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, HEXID_CONFIG);
	}
	
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			L2Properties hexSetting = new L2Properties();
			File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(out, "the hexID to auth into login");
			out.close();
		}
		catch (Exception e)
		{
			_log.warning(StringUtil.concat("Failed to save hex id to ", fileName, " File."));
			e.printStackTrace();
		}
	}
	
	public static void loadFloodProtectorConfigs(final L2Properties properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", "4");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", "16");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", "3");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SENDMAIL, "SendMail", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_AUCTION, "ItemAuction", "9");
	}
	
	public static int getServerTypeId(String[] serverTypes)
	{
		int tType = 0;
		for (String cType : serverTypes)
		{
			cType = cType.trim();
			if (cType.equalsIgnoreCase("Normal"))
				tType |= 0x01;
			else if (cType.equalsIgnoreCase("Relax"))
				tType |= 0x02;
			else if (cType.equalsIgnoreCase("Test"))
				tType |= 0x04;
			else if (cType.equalsIgnoreCase("NoLabel"))
				tType |= 0x08;
			else if (cType.equalsIgnoreCase("Restricted"))
				tType |= 0x10;
			else if (cType.equalsIgnoreCase("Event"))
				tType |= 0x20;
			else if (cType.equalsIgnoreCase("Free"))
				tType |= 0x40;
		}
		return tType;
	}
	
	public static TIntFloatHashMap parseConfigLine(String line)
	{
		String[] propertySplit = line.split(",");
		TIntFloatHashMap ret = new TIntFloatHashMap(propertySplit.length);
		int i = 1;
		for (String value : propertySplit)
			ret.put(i++, Float.parseFloat(value));
		return ret;
	}
	
	public static boolean getBoolean(final L2Properties properties, final String key, final boolean defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return defaultValue;
		
		return Boolean.parseBoolean(value);
	}
	
	public static long getLong(final L2Properties properties, final String key, final long defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return defaultValue;
		
		return Long.parseLong(value);
	}
	
	public static Short getShort(final L2Properties properties, final String key, final short defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return defaultValue;
		
		return Short.parseShort(value);
	}
	
	public static int getInt(final L2Properties properties, final String key, final int defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return defaultValue;
		
		return Integer.parseInt(value);
	}
	
	public static int getIntDecode(final L2Properties properties, final String key, final String defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return Integer.decode("0x" + defaultValue);
		
		return Integer.decode("0x" + value);
	}
	
	public static byte getByte(final L2Properties properties, final String key, final byte defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return defaultValue;
		
		return Byte.parseByte(value);
	}
	
	public static float getFloat(final L2Properties properties, final String key, final float defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return defaultValue;
		
		return Float.parseFloat(value);
	}
	
	public static double getDouble(final L2Properties properties, final String key, final double defaultValue)
	{
		final String value = getString(properties, key, null);
		
		if (value == null)
			return defaultValue;
		
		return Double.parseDouble(value);
	}
	
	public static String getString(final L2Properties properties, final String key, final String defaultValue)
	{
		String value = null;
		value = properties.getProperty(key);
		
		if (value == null)
			return defaultValue;
		else
			return value;
	}
	
	public static String[] getStringArray(final L2Properties properties, final String key, final String[] defaultValue, final String separator)
	{
		final String string = getString(properties, key, null);
		
		if ((string == null) || string.trim().isEmpty())
			return defaultValue;
		
		final String[] result = string.split(separator);
		
		for (int i = 0; i < result.length; i++)
			result[i] = result[i].trim();
		
		return result;
	}
	
	public static int[] getIntArray(final L2Properties properties, final String key, final int[] defaultValue, final String separator)
	{
		final String string = getString(properties, key, null);
		
		if ((string == null) || string.trim().isEmpty())
			return defaultValue;
		
		final String[] stringArray = string.split(separator);
		final int[] result = new int[stringArray.length];
		
		for (int i = 0; i < stringArray.length; i++)
			result[i] = Integer.parseInt(stringArray[i].trim());
		
		return result;
	}
	
	public static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
			return null;
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber"));
				return null;
			}
			
			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\""));
				return null;
			}
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\""));
				return null;
			}
			i++;
		}
		return result;
	}
	
	private static void loadFloodProtectorConfig(final L2Properties properties, final FloodProtectorConfig config, final String configString, final String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "False"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE = properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
	}
	
	public static class SystemPvPColor
	{
		private TreeMap<Integer, Integer> _colorName;
		private TreeMap<Integer, Integer> _colorTitle;
		
		private static class KeyComparator implements Comparator<Integer>
		{
			@Override
			public int compare(Integer key1, Integer key2)
			{
				return key1 - key2;
			}
		}
		
		public SystemPvPColor(String _configLine)
		{
			_colorName = new TreeMap<Integer, Integer>(new KeyComparator());
			_colorTitle = new TreeMap<Integer, Integer>(new KeyComparator());
			
			if (_configLine != null)
				parseConfigLine(_configLine);
		}
		
		private void parseConfigLine(String _configLine)
		{
			String[] items = _configLine.split("\\;");
			for (String item : items)
			{
				String[] itemSplit = item.split("\\,");
				Integer pvpAmount = Integer.parseInt(itemSplit[0]);
				_colorName.put(pvpAmount, Integer.decode("0x" + itemSplit[1]));
				_colorTitle.put(pvpAmount, Integer.decode("0x" + itemSplit[2]));
			}
		}
		
		public TreeMap<Integer, Integer> getColorName()
		{
			TreeMap<Integer, Integer> color = new TreeMap<Integer, Integer>(new KeyComparator());
			color.putAll(_colorName);
			
			return color;
		}
		
		public TreeMap<Integer, Integer> getColorTitle()
		{
			
			TreeMap<Integer, Integer> color = new TreeMap<Integer, Integer>(new KeyComparator());
			color.putAll(_colorTitle);
			
			return color;
		}
	}
	
	public static class ClassMasterSettings
	{
		private final TIntObjectHashMap<TIntIntHashMap> _claimItems;
		private final TIntObjectHashMap<TIntIntHashMap> _rewardItems;
		private final TIntObjectHashMap<Boolean> _allowedClassChange;
		
		public ClassMasterSettings(String _configLine)
		{
			_claimItems = new TIntObjectHashMap<TIntIntHashMap>(3);
			_rewardItems = new TIntObjectHashMap<TIntIntHashMap>(3);
			_allowedClassChange = new TIntObjectHashMap<Boolean>(3);
			if (_configLine != null)
				parseConfigLine(_configLine.trim());
		}
		
		private void parseConfigLine(String _configLine)
		{
			StringTokenizer st = new StringTokenizer(_configLine, ";");
			
			while (st.hasMoreTokens())
			{
				// get allowed class change
				int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				TIntIntHashMap _items = new TIntIntHashMap();
				// parse items needed for class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_claimItems.put(job, _items);
				
				_items = new TIntIntHashMap();
				// parse gifts after class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_rewardItems.put(job, _items);
			}
		}
		
		public boolean isAllowed(int job)
		{
			if (_allowedClassChange == null)
				return false;
			if (_allowedClassChange.containsKey(job))
				return _allowedClassChange.get(job);
			
			return false;
		}
		
		public TIntIntHashMap getRewardItems(int job)
		{
			if (_rewardItems.containsKey(job))
				return _rewardItems.get(job);
			
			return null;
		}
		
		public TIntIntHashMap getRequireItems(int job)
		{
			if (_claimItems.containsKey(job))
				return _claimItems.get(job);
			
			return null;
		}
	}
}
