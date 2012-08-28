package net.sf.l2j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.EventTerritory;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.log.AbstractLogger;

public final class Config
{
  protected static final Logger _log = Logger.getLogger(Config.class.getName());
  public static final String CONFIGURATION_FILE = "./config/server.cfg";
  public static final String OPTIONS_FILE = "./config/options.cfg";
  public static final String LOGIN_CONFIGURATION_FILE = "./config/loginserver.cfg";
  public static final String ID_CONFIG_FILE = "./config/idfactory.cfg";
  public static final String OTHER_CONFIG_FILE = "./config/other.cfg";
  public static final String RATES_CONFIG_FILE = "./config/rates.cfg";
  public static final String ALT_SETTINGS_FILE = "./config/altsettings.cfg";
  public static final String PVP_CONFIG_FILE = "./config/pvp.cfg";
  public static final String GM_ACCESS_FILE = "./config/GMAccess.cfg";
  public static final String TELNET_FILE = "./config/telnet.cfg";
  public static final String SIEGE_CONFIGURATION_FILE = "./config/siege.cfg";
  public static final String BANNED_IP_XML = "./config/banned.xml";
  public static final String HEXID_FILE = "./config/hexid.txt";
  public static final String COMMAND_PRIVILEGES_FILE = "./config/command-privileges.cfg";
  public static final String AI_FILE = "./config/ai.cfg";
  public static final String SEVENSIGNS_FILE = "./config/sevensigns.cfg";
  public static final String CLANHALL_CONFIG_FILE = "./config/clanhall.cfg";
  public static final String NPC_CONFIG_FILE = "./config/npc.cfg";
  public static final String CUSTOM_CONFIG_FILE = "./config/custom.cfg";
  public static final String ENCHANT_CONFIG_FILE = "./config/enchants.cfg";
  public static final String CMD_CONFIG_FILE = "./config/commands.cfg";
  public static final String CHAT_FILTER_FILE = "./config/chatfilter.txt";
  public static final String GEO_FILE = "./config/geodata.cfg";
  public static final String FAKE_FILE = "./config/fakeplayers.cfg";
  public static final String GAME_GUARD_FILE = "./config/protection.cfg";
  public static boolean DEBUG;
  public static boolean ASSERT;
  public static boolean DEVELOPER;
  public static boolean TEST_SERVER;
  public static int PORT_GAME;
  public static int PORT_LOGIN;
  public static String LOGIN_BIND_ADDRESS;
  public static int LOGIN_TRY_BEFORE_BAN;
  public static int LOGIN_BLOCK_AFTER_BAN;
  public static String GAMESERVER_HOSTNAME;
  public static String DATABASE_DRIVER;
  public static String DATABASE_URL;
  public static String DATABASE_LOGIN;
  public static String DATABASE_PASSWORD;
  public static int DATABASE_MAX_CONNECTIONS;
  public static int MINCONNECTIONSPERPARTITION;
  public static int MAXCONNECTIONSPERPARTITION;
  public static int PARTITIONCOUNT;
  public static int ACQUIREINCREMENT;
  public static int IDLECONNECTIONTESTPERIOD;
  public static int IDLEMAXAGE;
  public static int RELEASEHELPERTHREADS;
  public static int ACQUIRERETRYDELAY;
  public static int ACQUIRERETRYATTEMPTS;
  public static int QUERYEXECUTETIMELIMIT;
  public static int CONNECTIONTIMEOUT;
  public static boolean LAZYINIT;
  public static boolean TRANSACTIONRECOVERYENABLED;
  public static int MAXIMUM_ONLINE_USERS;
  public static boolean SERVER_LIST_BRACKET;
  public static boolean SERVER_LIST_CLOCK;
  public static boolean SERVER_LIST_TESTSERVER;
  public static boolean SERVER_GMONLY;
  public static int THREAD_P_EFFECTS;
  public static int THREAD_P_GENERAL;
  public static int GENERAL_PACKET_THREAD_CORE_SIZE;
  public static int THREAD_P_PATHFIND;
  public static int IO_PACKET_THREAD_CORE_SIZE;
  public static int GENERAL_THREAD_CORE_SIZE;
  public static int THREADING_MODEL;
  public static int NPC_AI_MAX_THREAD;
  public static int PLAYER_AI_MAX_THREAD;
  public static int THREAD_P_MOVE;
  public static boolean AUTO_LOOT;
  public static boolean AUTO_LOOT_RAID;
  public static boolean AUTO_LOOT_HERBS;
  public static String CNAME_TEMPLATE;
  public static String PET_NAME_TEMPLATE;
  public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
  public static String DEFAULT_GLOBAL_CHAT;
  public static String DEFAULT_TRADE_CHAT;
  public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
  public static boolean ALT_GAME_CREATION;
  public static double ALT_GAME_CREATION_SPEED;
  public static double ALT_GAME_CREATION_XP_RATE;
  public static double ALT_GAME_CREATION_SP_RATE;
  public static boolean ALT_BLACKSMITH_USE_RECIPES;
  public static boolean REMOVE_CASTLE_CIRCLETS;
  public static double ALT_WEIGHT_LIMIT;
  public static double MAGIC_CRIT_EXP;
  public static double MAGIC_DAM_EXP;
  public static double MAGIC_PDEF_EXP;
  public static boolean ALT_GAME_SKILL_LEARN;
  public static boolean AUTO_LEARN_SKILLS;
  public static boolean ALT_GAME_CANCEL_BOW;
  public static boolean ALT_GAME_CANCEL_CAST;
  public static boolean ALT_GAME_TIREDNESS;
  public static int ALT_PARTY_RANGE;
  public static int ALT_PARTY_RANGE2;
  public static boolean ALT_GAME_SHIELD_BLOCKS;
  public static int ALT_PERFECT_SHLD_BLOCK;
  public static boolean ALT_GAME_MOB_ATTACK_AI;
  public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
  public static boolean ALT_GAME_FREIGHTS;
  public static int ALT_GAME_FREIGHT_PRICE;
  public static float ALT_GAME_SKILL_HIT_RATE;
  public static boolean ALT_GAME_DELEVEL;
  public static boolean ALT_GAME_MAGICFAILURES;
  public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
  public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
  public static int ALT_GAME_FREE_TELEPORT;
  public static boolean ALT_RECOMMEND;
  public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
  public static byte MAX_SUBCLASS;
  public static boolean ALT_GAME_VIEWNPC;
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
  public static int ALT_CLAN_MEMBERS_FOR_WAR;
  public static int ALT_CLAN_JOIN_DAYS;
  public static int ALT_CLAN_CREATE_DAYS;
  public static int ALT_CLAN_DISSOLVE_DAYS;
  public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
  public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
  public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
  public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
  public static float ALT_CLAN_REP_MUL;
  public static int ALT_CLAN_CREATE_LEVEL;
  public static int ALT_CLAN_REP_WAR;
  public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
  public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NOBLE;
  public static int ALT_START_LEVEL;
  public static boolean ALLOW_RUPOR;
  public static int RUPOR_ID;
  public static boolean SONLINE_ANNOUNE;
  public static int SONLINE_ANNOUNCE_DELAY;
  public static boolean SONLINE_SHOW_MAXONLINE;
  public static boolean SONLINE_SHOW_MAXONLINE_DATE;
  public static boolean SONLINE_SHOW_OFFLINE;
  public static boolean SONLINE_LOGIN_ONLINE;
  public static boolean SONLINE_LOGIN_MAX;
  public static boolean SONLINE_LOGIN_DATE;
  public static boolean SONLINE_LOGIN_OFFLINE;
  public static int AUTO_ANNOUNCE_DELAY;
  public static boolean AUTO_ANNOUNCE_ALLOW;
  public static boolean ALT_ALLOW_AUGMENT_ON_OLYMP;
  public static boolean ALT_ALLOW_OFFLINE_TRADE;
  public static boolean ALT_ALLOW_AUC;
  public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
  public static boolean CASTLE_SHIELD;
  public static boolean CLANHALL_SHIELD;
  public static boolean APELLA_ARMORS;
  public static boolean OATH_ARMORS;
  public static boolean CASTLE_CROWN;
  public static boolean CASTLE_CIRCLETS;
  public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
  public static boolean LIFE_CRYSTAL_NEEDED;
  public static boolean SP_BOOK_NEEDED;
  public static boolean ES_SP_BOOK_NEEDED;
  public static boolean LOG_CHAT;
  public static boolean LOG_ITEMS;
  public static FastList<Integer> LOG_MULTISELL_ID = new FastList();
  public static boolean ALT_PRIVILEGES_ADMIN;
  public static boolean ALT_PRIVILEGES_SECURE_CHECK;
  public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
  public static int ALT_OLY_START_TIME;
  public static int ALT_OLY_MIN;
  public static long ALT_OLY_CPERIOD;
  public static long ALT_OLY_BATTLE;
  public static long ALT_OLY_BWAIT;
  public static long ALT_OLY_IWAIT;
  public static long ALT_OLY_WPERIOD;
  public static long ALT_OLY_VPERIOD;
  public static boolean ALT_OLY_SAME_IP;
  public static boolean ALT_OLY_SAME_HWID;
  public static int ALT_OLY_ENCHANT_LIMIT;
  public static int ALT_OLY_MINCLASS;
  public static int ALT_OLY_MINNONCLASS;
  public static boolean ALT_OLY_MP_REG;
  public static int ALT_MANOR_REFRESH_TIME;
  public static int ALT_MANOR_REFRESH_MIN;
  public static int ALT_MANOR_APPROVE_TIME;
  public static int ALT_MANOR_APPROVE_MIN;
  public static int ALT_MANOR_MAINTENANCE_PERIOD;
  public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
  public static int ALT_MANOR_SAVE_PERIOD_RATE;
  public static int ALT_LOTTERY_PRIZE;
  public static int ALT_LOTTERY_TICKET_PRICE;
  public static float ALT_LOTTERY_5_NUMBER_RATE;
  public static float ALT_LOTTERY_4_NUMBER_RATE;
  public static float ALT_LOTTERY_3_NUMBER_RATE;
  public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
  public static boolean NOEPIC_QUESTS;
  public static boolean ALT_EPIC_JEWERLY;
  public static boolean ONE_AUGMENT;
  public static boolean JOB_WINDOW;
  public static boolean USE_SOULSHOTS;
  public static boolean USE_ARROWS;
  public static int MAX_PATK_SPEED;
  public static int MAX_MATK_SPEED;
  public static int MAX_MAX_HP;
  public static boolean DISABLE_GRADE_PENALTY;
  public static boolean DISABLE_WEIGHT_PENALTY;
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
  public static int RIFT_ENTER_COST_RECRUIT;
  public static int RIFT_ENTER_COST_SOLDIER;
  public static int RIFT_ENTER_COST_OFFICER;
  public static int RIFT_ENTER_COST_CAPTAIN;
  public static int RIFT_ENTER_COST_COMMANDER;
  public static int RIFT_ENTER_COST_HERO;
  public static int MAX_CHAT_LENGTH;
  public static int CRUMA_TOWER_LEVEL_RESTRICT;
  public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
  public static int GM_ACCESSLEVEL;
  public static int GM_MIN;
  public static int GM_ALTG_MIN_LEVEL;
  public static int GM_ANNOUNCE;
  public static int GM_BAN;
  public static int GM_BAN_CHAT;
  public static int GM_CREATE_ITEM;
  public static int GM_DELETE;
  public static int GM_KICK;
  public static int GM_MENU;
  public static int GM_GODMODE;
  public static int GM_CHAR_EDIT;
  public static int GM_CHAR_EDIT_OTHER;
  public static int GM_CHAR_VIEW;
  public static int GM_NPC_EDIT;
  public static int GM_NPC_VIEW;
  public static int GM_TELEPORT;
  public static int GM_TELEPORT_OTHER;
  public static int GM_RESTART;
  public static int GM_MONSTERRACE;
  public static int GM_RIDER;
  public static int GM_ESCAPE;
  public static int GM_FIXED;
  public static int GM_CREATE_NODES;
  public static int GM_ENCHANT;
  public static int GM_DOOR;
  public static int GM_RES;
  public static int GM_PEACEATTACK;
  public static int GM_HEAL;
  public static int GM_UNBLOCK;
  public static int GM_CACHE;
  public static int GM_TALK_BLOCK;
  public static int GM_TEST;
  public static boolean GM_DISABLE_TRANSACTION;
  public static int GM_TRANSACTION_MIN;
  public static int GM_TRANSACTION_MAX;
  public static int GM_CAN_GIVE_DAMAGE;
  public static int GM_DONT_TAKE_EXPSP;
  public static int GM_DONT_TAKE_AGGRO;
  public static int GM_REPAIR = 75;
  public static float RATE_XP;
  public static float RATE_SP;
  public static float RATE_PARTY_XP;
  public static float RATE_PARTY_SP;
  public static float RATE_QUESTS_REWARD;
  public static float RATE_DROP_ADENA;
  public static float RATE_DROP_ADENAMUL;
  public static float RATE_CONSUMABLE_COST;
  public static float RATE_DROP_ITEMS;
  public static float RATE_DROP_ITEMS_BY_RAID;
  public static float RATE_DROP_ITEMS_BY_GRANDRAID;
  public static float RATE_DROP_SPOIL;
  public static int RATE_DROP_MANOR;
  public static float RATE_DROP_QUEST;
  public static float RATE_KARMA_EXP_LOST;
  public static float RATE_SIEGE_GUARDS_PRICE;
  public static float ALT_GAME_EXPONENT_XP;
  public static float ALT_GAME_EXPONENT_SP;
  public static float RATE_DROP_COMMON_HERBS;
  public static float RATE_DROP_MP_HP_HERBS;
  public static float RATE_DROP_GREATER_HERBS;
  public static float RATE_DROP_SUPERIOR_HERBS;
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
  public static int AUTODESTROY_ITEM_AFTER;
  public static int HERB_AUTO_DESTROY_TIME;
  public static String PROTECTED_ITEMS;
  public static List<Integer> LIST_PROTECTED_ITEMS = new FastList();
  public static boolean DESTROY_DROPPED_PLAYER_ITEM;
  public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
  public static boolean SAVE_DROPPED_ITEM;
  public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
  public static int SAVE_DROPPED_ITEM_INTERVAL;
  public static boolean CLEAR_DROPPED_ITEM_TABLE;
  public static boolean PRECISE_DROP_CALCULATION;
  public static boolean MULTIPLE_ITEM_DROP;
  public static int COORD_SYNCHRONIZE;
  public static int DELETE_DAYS;
  public static File DATAPACK_ROOT;
  public static int MAX_DRIFT_RANGE;
  public static boolean ALLOWFISHING;
  public static boolean ALLOW_MANOR;
  public static boolean JAIL_IS_PVP;
  public static boolean JAIL_DISABLE_CHAT;
  public static L2WalkerAllowed ALLOW_L2WALKER_CLIENT;
  public static boolean AUTOBAN_L2WALKER_ACC;
  public static int L2WALKER_REVISION;
  public static boolean GM_EDIT;
  public static boolean ALLOW_DISCARDITEM;
  public static boolean ALLOW_FREIGHT;
  public static boolean ALLOW_WAREHOUSE;
  public static boolean WAREHOUSE_CACHE;
  public static int WAREHOUSE_CACHE_TIME;
  public static boolean ALLOW_WEAR;
  public static int WEAR_DELAY;
  public static int WEAR_PRICE;
  public static boolean ALLOW_LOTTERY;
  public static boolean ALLOW_RACE;
  public static boolean ALLOW_WATER;
  public static boolean ALLOW_RENTPET;
  public static boolean ALLOW_BOAT;
  public static boolean ALLOW_RAID_BOSS_PUT;
  public static boolean ALLOW_RAID_BOSS_HEAL;
  public static boolean ALLOW_CURSED_WEAPONS;
  public static boolean ALLOW_NPC_WALKERS;
  public static int PACKET_LIFETIME;
  public static int WYVERN_SPEED;
  public static int STRIDER_SPEED;
  public static int WATER_SPEED;
  public static boolean ALLOW_WYVERN_UPGRADER;
  public static int MIN_PROTOCOL_REVISION;
  public static int MAX_PROTOCOL_REVISION;
  public static boolean SHOW_PROTOCOL_VERSIONS;
  public static int MIN_NPC_ANIMATION;
  public static int MAX_NPC_ANIMATION;
  public static int MIN_MONSTER_ANIMATION;
  public static int MAX_MONSTER_ANIMATION;
  public static boolean ACTIVATE_POSITION_RECORDER;
  public static boolean USE_3D_MAP;
  public static String COMMUNITY_TYPE;
  public static String BBS_DEFAULT;
  public static boolean SHOW_LEVEL_COMMUNITYBOARD;
  public static boolean SHOW_STATUS_COMMUNITYBOARD;
  public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
  public static int NAME_PER_ROW_COMMUNITYBOARD;
  public static int MAX_ITEM_IN_PACKET;
  public static boolean CHECK_KNOWN;
  public static int GAME_SERVER_LOGIN_PORT;
  public static String GAME_SERVER_LOGIN_HOST;
  public static String INTERNAL_HOSTNAME;
  public static String EXTERNAL_HOSTNAME;
  public static int PATH_NODE_RADIUS;
  public static int NEW_NODE_ID;
  public static int SELECTED_NODE_ID;
  public static int LINKED_NODE_ID;
  public static String NEW_NODE_TYPE;
  public static boolean SERVER_NEWS;
  public static boolean SHOW_NPC_LVL;
  public static boolean FORCE_INVENTORY_UPDATE;
  public static boolean ALLOW_GUARDS;
  public static boolean ALLOW_CLASS_MASTERS;
  public static final FastMap<Integer, EventReward> CLASS_MASTERS_PRICES = new FastMap().shared("Config.CLASS_MASTERS_PRICES");
  public static boolean ALLOW_CLAN_LEVEL;
  public static boolean REWARD_SHADOW;
  public static int IP_UPDATE_TIME;
  public static String SERVER_VERSION;
  public static String SERVER_BUILD_DATE;
  public static String DATAPACK_VERSION;
  public static int ZONE_TOWN;
  public static boolean IS_CRAFTING_ENABLED;
  public static int INVENTORY_MAXIMUM_NO_DWARF;
  public static int INVENTORY_MAXIMUM_DWARF;
  public static int INVENTORY_MAXIMUM_GM;
  public static int WAREHOUSE_SLOTS_NO_DWARF;
  public static int WAREHOUSE_SLOTS_DWARF;
  public static int WAREHOUSE_SLOTS_CLAN;
  public static int FREIGHT_SLOTS;
  public static int KARMA_MIN_KARMA;
  public static int KARMA_MAX_KARMA;
  public static int KARMA_XP_DIVIDER;
  public static int KARMA_LOST_BASE;
  public static boolean KARMA_DROP_GM;
  public static boolean KARMA_AWARD_PK_KILL;
  public static int KARMA_PK_LIMIT;
  public static String KARMA_NONDROPPABLE_PET_ITEMS;
  public static String KARMA_NONDROPPABLE_ITEMS;
  public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList();

  public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new FastList();
  public static String NONDROPPABLE_ITEMS;
  public static List<Integer> LIST_NONDROPPABLE_ITEMS = new FastList();
  public static String PET_RENT_NPC;
  public static List<Integer> LIST_PET_RENT_NPC = new FastList();
  public static int PVP_NORMAL_TIME;
  public static int PVP_PVP_TIME;
  public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
  public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
  public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
  public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
  public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
  public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
  public static int ALT_PLAYER_PROTECTION_LEVEL;
  public static boolean L2JMOD_CHAMPION_ENABLE;
  public static int L2JMOD_CHAMPION_FREQUENCY;
  public static int L2JMOD_CHAMP_MIN_LVL;
  public static int L2JMOD_CHAMP_MAX_LVL;
  public static int L2JMOD_CHAMPION_HP;
  public static int L2JMOD_CHAMPION_REWARDS;
  public static int L2JMOD_CHAMPION_ADENAS_REWARDS;
  public static float L2JMOD_CHAMPION_HP_REGEN;
  public static float L2JMOD_CHAMPION_ATK;
  public static float L2JMOD_CHAMPION_SPD_ATK;
  public static int L2JMOD_CHAMPION_REWARD;
  public static int L2JMOD_CHAMPION_REWARD_ID;
  public static int L2JMOD_CHAMPION_REWARD_QTY;
  public static boolean L2JMOD_CHAMPION_AURA;
  public static boolean TVT_EVENT_ENABLED;
  public static int TVT_EVENT_INTERVAL;
  public static int TVT_EVENT_PARTICIPATION_TIME;
  public static int TVT_EVENT_RUNNING_TIME;
  public static int TVT_EVENT_PARTICIPATION_NPC_ID;
  public static FastList<Location> TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new FastList();
  public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS;
  public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS;
  public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY;
  public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY;
  public static String TVT_EVENT_TEAM_1_NAME;
  public static int[] TVT_EVENT_TEAM_1_COORDINATES = new int[3];
  public static String TVT_EVENT_TEAM_2_NAME;
  public static int[] TVT_EVENT_TEAM_2_COORDINATES = new int[3];
  public static final List<int[]> TVT_EVENT_REWARDS = new FastList();
  public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
  public static boolean TVT_EVENT_POTIONS_ALLOWED;
  public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;
  public static final List<Integer> TVT_EVENT_DOOR_IDS = new FastList();
  public static byte TVT_EVENT_MIN_LVL;
  public static byte TVT_EVENT_MAX_LVL;
  public static boolean TVT_NO_PASSIVE;
  public static boolean L2JMOD_ALLOW_WEDDING;
  public static int L2JMOD_WEDDING_INTERVAL;
  public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
  public static boolean L2JMOD_WEDDING_TELEPORT;
  public static int L2JMOD_WEDDING_TELEPORT_PRICE;
  public static int L2JMOD_WEDDING_TELEPORT_DURATION;
  public static boolean L2JMOD_WEDDING_SAMESEX;
  public static boolean L2JMOD_WEDDING_FORMALWEAR;
  public static int L2JMOD_WEDDING_COIN;
  public static int L2JMOD_WEDDING_PRICE;
  public static String L2JMOD_WEDDING_COINNAME;
  public static int L2JMOD_WEDDING_DIVORCE_COIN;
  public static int L2JMOD_WEDDING_DIVORCE_PRICE;
  public static String L2JMOD_WEDDING_DIVORCE_COINNAME;
  public static boolean L2JMOD_WEDDING_BOW;
  public static boolean KICK_L2WALKER;
  public static boolean ALLOW_RAID_PVP;
  public static long CP_REUSE_TIME;
  public static long MANA_RESTORE;
  public static int ANTIBUFF_SKILLID;
  public static int BLOW_CHANCE_FRONT;
  public static int BLOW_CHANCE_BEHIND;
  public static int BLOW_CHANCE_SIDE;
  public static double BLOW_DAMAGE_HEAVY;
  public static double BLOW_DAMAGE_LIGHT;
  public static double BLOW_DAMAGE_ROBE;
  public static boolean ALLOW_HERO_SUBSKILL;
  public static byte SUB_START_LVL;
  public static int RUN_SPD_BOOST;
  public static int MAX_RUN_SPEED;
  public static int MAX_PCRIT_RATE;
  public static int MAX_MCRIT_RATE;
  public static boolean ALT_SUBCLASS_SKILLS;
  public static boolean SPAWN_CHAR;
  public static int SPAWN_X;
  public static int SPAWN_Y;
  public static int SPAWN_Z;
  public static int ENCHANT_CHANCE_WEAPON_CRYSTAL;
  public static int ENCHANT_CHANCE_ARMOR_CRYSTAL;
  public static int ENCHANT_CHANCE_JEWELRY_CRYSTAL;
  public static int ENCHANT_CHANCE_NEXT;
  public static int ENCHANT_FAILED_NUM;
  public static float MAGIC_CHANCE_BEFORE_NEXT;
  public static float MAGIC_CHANCE_AFTER_NEXT;
  public static float WEAPON_CHANCE_BEFORE_NEXT;
  public static float WEAPON_CHANCE_AFTER_NEXT;
  public static final List<Float> ARMOR_ENCHANT_TABLE = new FastList();
  public static final List<Float> FULL_ARMOR_ENCHANT_TABLE = new FastList();
  public static boolean USE_CHAT_FILTER;
  public static List<String> FILTER_LIST = new FastList();
  public static String CHAT_FILTER_PUNISHMENT;
  public static int CHAT_FILTER_PUNISHMENT_PARAM1;
  public static int CHAT_FILTER_PUNISHMENT_PARAM2;
  public static boolean COUNT_PACKETS = false;

  public static boolean DUMP_PACKET_COUNTS = false;

  public static int DUMP_INTERVAL_SECONDS = 60;
  public static IdFactoryType IDFACTORY_TYPE;
  public static boolean BAD_ID_CHECKING;
  public static ObjectMapType MAP_TYPE;
  public static ObjectSetType SET_TYPE;
  public static boolean EFFECT_CANCELING;
  public static boolean AUTODELETE_INVALID_QUEST_DATA;
  public static int ENCHANT_MAX_WEAPON;
  public static int ENCHANT_MAX_ARMOR;
  public static int ENCHANT_MAX_JEWELRY;
  public static int ENCHANT_SAFE_MAX;
  public static int ENCHANT_SAFE_MAX_FULL;
  public static double HP_REGEN_MULTIPLIER;
  public static double MP_REGEN_MULTIPLIER;
  public static double CP_REGEN_MULTIPLIER;
  public static double RAID_HP_REGEN_MULTIPLIER;
  public static double RAID_MP_REGEN_MULTIPLIER;
  public static double RAID_DEFENCE_MULTIPLIER;
  public static double RAID_MINION_RESPAWN_TIMER;
  public static float RAID_MIN_RESPAWN_MULTIPLIER;
  public static float RAID_MAX_RESPAWN_MULTIPLIER;
  public static int STARTING_ADENA;
  public static boolean DEEPBLUE_DROP_RULES;
  public static int UNSTUCK_INTERVAL;
  public static boolean IS_TELNET_ENABLED;
  public static int DEATH_PENALTY_CHANCE;
  public static int AUGMENT_BASESTAT;
  public static int AUGMENT_SKILL_NORM;
  public static int AUGMENT_SKILL_MID;
  public static int AUGMENT_SKILL_HIGH;
  public static int AUGMENT_SKILL_TOP;
  public static boolean AUGMENT_EXCLUDE_NOTDONE;
  public static int PLAYER_SPAWN_PROTECTION;
  public static int PLAYER_FAKEDEATH_UP_PROTECTION;
  public static String PARTY_XP_CUTOFF_METHOD;
  public static int PARTY_XP_CUTOFF_LEVEL;
  public static double PARTY_XP_CUTOFF_PERCENT;
  public static double RESPAWN_RESTORE_CP;
  public static double RESPAWN_RESTORE_HP;
  public static double RESPAWN_RESTORE_MP;
  public static boolean RESPAWN_RANDOM_ENABLED;
  public static int RESPAWN_RANDOM_MAX_OFFSET;
  public static int MAX_PVTSTORE_SLOTS_DWARF;
  public static int MAX_PVTSTORE_SLOTS_OTHER;
  public static boolean STORE_SKILL_COOLTIME;
  public static boolean SHOW_LICENCE;
  public static boolean FORCE_GGAUTH;
  public static int DEFAULT_PUNISH;
  public static int DEFAULT_PUNISH_PARAM;
  public static boolean ACCEPT_NEW_GAMESERVER;
  public static int SERVER_ID;
  public static byte[] HEX_ID;
  public static boolean ACCEPT_ALTERNATE_ID;
  public static int REQUEST_ID;
  public static final boolean RESERVE_HOST_ON_LOGIN = false;
  public static int MINIMUM_UPDATE_DISTANCE;
  public static int KNOWNLIST_FORGET_DELAY;
  public static int MINIMUN_UPDATE_TIME;
  public static boolean ANNOUNCE_MAMMON_SPAWN;
  public static boolean LAZY_CACHE;
  public static boolean GM_HERO_AURA;
  public static boolean GM_STARTUP_INVULNERABLE;
  public static boolean GM_STARTUP_INVISIBLE;
  public static boolean GM_STARTUP_SILENCE;
  public static boolean GM_STARTUP_AUTO_LIST;
  public static String GM_ADMIN_MENU_STYLE;
  public static boolean PETITIONING_ALLOWED;
  public static int MAX_PETITIONS_PER_PLAYER;
  public static int MAX_PETITIONS_PENDING;
  public static boolean BYPASS_VALIDATION;
  public static boolean ONLY_GM_ITEMS_FREE;
  public static boolean GMAUDIT;
  public static boolean AUTO_CREATE_ACCOUNTS;
  public static boolean FLOOD_PROTECTION;
  public static int FAST_CONNECTION_LIMIT;
  public static int NORMAL_CONNECTION_TIME;
  public static int FAST_CONNECTION_TIME;
  public static int MAX_CONNECTION_PER_IP;
  public static boolean GAMEGUARD_ENFORCE;
  public static boolean GAMEGUARD_PROHIBITACTION;
  public static int DWARF_RECIPE_LIMIT;
  public static int COMMON_RECIPE_LIMIT;
  public static boolean GRIDS_ALWAYS_ON;
  public static int GRID_NEIGHBOR_TURNON_TIME;
  public static int GRID_NEIGHBOR_TURNOFF_TIME;
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
  public static int GEODATA;
  public static int GEO_TYPE;
  public static boolean FORCE_GEODATA;
  public static boolean GEO_SHOW_LOAD;
  public static boolean ACCEPT_GEOEDITOR_CONN;
  public static int MAP_MIN_X;
  public static int MAP_MAX_X;
  public static int MAP_MIN_Y;
  public static int MAP_MAX_Y;
  public static String GEO_L2J_PATH;
  public static String GEO_OFF_PATH;
  public static int BUFFS_MAX_AMOUNT;
  public static int BUFFS_PET_MAX_AMOUNT;
  public static boolean ALT_DEV_NO_QUESTS;
  public static boolean ALT_DEV_NO_SPAWNS;
  public static int MASTERACCESS_LEVEL;
  public static final String SERVICE_FILE = "./config/services.cfg";
  public static int STOCK_SERTIFY;
  public static int SERTIFY_PRICE;
  public static int FIRST_BALANCE;
  public static int DONATE_COIN;
  public static String DONATE_COIN_NEMA;
  public static int DONATE_RATE;
  public static int NALOG_NPS;
  public static String VAL_NAME;
  public static int PAGE_LIMIT;
  public static int AUGMENT_COIN;
  public static int ENCHANT_COIN;
  public static String AUGMENT_COIN_NAME;
  public static String ENCHANT_COIN_NAME;
  public static int AUGMENT_PRICE;
  public static int ENCHANT_PRICE;
  public static int CLAN_COIN;
  public static String CLAN_COIN_NAME;
  public static int CLAN_POINTS;
  public static int CLAN_POINTS_PRICE;
  public static int CLAN_SKILLS_PRICE;
  public static int AUGSALE_COIN;
  public static int AUGSALE_PRICE;
  public static String AUGSALE_COIN_NAME;
  public static final FastMap<Integer, Integer> AUGSALE_TABLE = new FastMap().shared("Config.AUGSALE_TABLE");
  public static int SOB_ID;
  public static int SOB_NPC;
  public static int SOB_COIN;
  public static int SOB_PRICE_ONE;
  public static int SOB_PRICE_TWO;
  public static String SOB_COIN_NAME;
  public static boolean ALLOW_DSHOP;
  public static boolean ALLOW_DSKILLS;
  public static boolean ALLOW_CSHOP;
  public static int BBS_AUC_ITEM_COIN;
  public static int BBS_AUC_ITEM_PRICE;
  public static String BBS_AUC_ITEM_NAME;
  public static int BBS_AUC_AUG_COIN;
  public static int BBS_AUC_AUG_PRICE;
  public static String BBS_AUC_AUG_NAME;
  public static int BBS_AUC_SKILL_COIN;
  public static int BBS_AUC_SKILL_PRICE;
  public static String BBS_AUC_SKILL_NAME;
  public static int BBS_AUC_HERO_COIN;
  public static int BBS_AUC_HERO_PRICE;
  public static String BBS_AUC_HERO_NAME;
  public static final FastMap<Integer, String> BBS_AUC_MONEYS = new FastMap().shared("Config.BBS_AUC_MONEYS");
  public static int BBS_AUC_EXPIRE_DAYS;
  public static int BUFFER_ID;
  public static final FastMap<Integer, Integer> M_BUFF = new FastMap().shared("Config.M_BUFF");
  public static final FastMap<Integer, Integer> F_BUFF = new FastMap().shared("Config.F_BUFF");
  public static final FastTable<Integer> F_PROFILE_BUFFS = new FastTable();

  public static final FastTable<Integer> C_ALLOWED_BUFFS = new FastTable();
  public static boolean POST_CHARBRIEF;
  public static String POST_BRIEFAUTHOR;
  public static String POST_BRIEFTHEME;
  public static String POST_BRIEFTEXT;
  public static String POST_NPCNAME;
  public static EventReward POST_BRIEF_ITEM;
  public static String MASTER_NPCNAME;
  public static int MCLAN_COIN;
  public static String MCLAN_COIN_NAME;
  public static int CLAN_LVL6;
  public static int CLAN_LVL7;
  public static int CLAN_LVL8;
  public static final String EVENT_FILE = "./config/events.cfg";
  public static boolean MASS_PVP;
  public static long MPVP_RTIME;
  public static long MPVP_REG;
  public static long MPVP_ANC;
  public static long MPVP_TP;
  public static long MPVP_PR;
  public static long MPVP_WT;
  public static long MPVP_MAX;
  public static long MPVP_NEXT;
  public static int MPVP_MAXC;
  public static int MPVP_NPC;
  public static Location MPVP_NPCLOC;
  public static Location MPVP_TPLOC;
  public static Location MPVP_CLOC;
  public static Location MPVP_WLOC;
  public static int MPVP_CREW;
  public static int MPVP_CREWC;
  public static int MPVP_EREW;
  public static int MPVP_EREWC;
  public static int MPVP_LVL;
  public static int MPVP_MAXP;
  public static boolean MPVP_NOBL;
  public static boolean TVT_NOBL;
  public static boolean ALLOW_SCH;
  public static int SCH_TIMEBOSS;
  public static int SCH_TIME1;
  public static int SCH_TIME2;
  public static int SCH_TIME3;
  public static int SCH_TIME4;
  public static int SCH_TIME5;
  public static int SCH_TIME6;
  public static long SCH_NEXT;
  public static int SCH_RESTART;
  public static int SCH_MOB1;
  public static int SCH_MOB2;
  public static int SCH_MOB3;
  public static int SCH_MOB4;
  public static int SCH_MOB5;
  public static int SCH_MOB6;
  public static int SCH_BOSS;
  public static boolean SCH_ALLOW_SHOP;
  public static int SCH_SHOP;
  public static int SCH_SHOPTIME;
  public static boolean OPEN_SEASON;
  public static int OS_NEXT;
  public static int OS_RESTART;
  public static int OS_BATTLE;
  public static int OS_FAIL_NEXT;
  public static int OS_REGTIME;
  public static int OS_ANNDELAY;
  public static int OS_MINLVL;
  public static int OS_MINPLAYERS;
  public static final FastList<EventReward> OS_REWARDS = new FastList();
  public static boolean ELH_ENABLE;
  public static long ELH_ARTIME;
  public static long ELH_REGTIME;
  public static long ELH_ANNDELAY;
  public static long ELH_TPDELAY;
  public static long ELH_NEXT;
  public static int ELH_MINLVL;
  public static int ELH_MINP;
  public static int ELH_MAXP;
  public static int ELH_NPCID;
  public static Location ELH_NPCLOC;
  public static String ELH_NPCTOWN;
  public static Location ELH_TPLOC;
  public static int ELH_TICKETID;
  public static int ELH_TICKETCOUNT;
  public static final FastList<EventReward> ELH_REWARDS = new FastList();
  public static int ELH_HERO_DAYS;
  public static boolean ELH_HIDE_NAMES;
  public static String ELH_ALT_NAME;
  public static final FastTable<Integer> FC_ALLOWITEMS = new FastTable();
  public static boolean ALLOW_XM_SPAWN;
  public static final FastList<EventReward> XM_DROP = new FastList();
  public static long XM_TREE_LIFE;
  public static boolean ALLOW_MEDAL_EVENT;
  public static final FastList<EventReward> MEDAL_EVENT_DROP = new FastList();
  public static boolean EBC_ENABLE;
  public static long EBC_ARTIME;
  public static long EBC_REGTIME;
  public static long EBC_ANNDELAY;
  public static long EBC_TPDELAY;
  public static long EBC_DEATHLAY;
  public static long EBC_NEXT;
  public static int EBC_MINLVL;
  public static int EBC_MINP;
  public static int EBC_MAXP;
  public static int EBC_NPCID;
  public static Location EBC_NPCLOC;
  public static String EBC_NPCTOWN;
  public static int EBC_BASE1ID;
  public static String EBC_BASE1NAME;
  public static int EBC_BASE2ID;
  public static String EBC_BASE2NAME;
  public static Location EBC_TPLOC1;
  public static Location EBC_TPLOC2;
  public static int EBC_TICKETID;
  public static int EBC_TICKETCOUNT;
  public static final FastList<EventReward> EBC_REWARDS = new FastList();
  public static boolean EENC_ENABLE;
  public static long EENC_ARTIME;
  public static long EENC_REGTIME;
  public static long EENC_ANNDELAY;
  public static long EENC_TPDELAY;
  public static long EENC_FINISH;
  public static long EENC_NEXT;
  public static int EENC_MINLVL;
  public static int EENC_MINP;
  public static int EENC_MAXP;
  public static int EENC_NPCID;
  public static Location EENC_NPCLOC;
  public static String EENC_NPCTOWN;
  public static Location EENC_TPLOC;
  public static int EENC_TICKETID;
  public static int EENC_TICKETCOUNT;
  public static final FastList<EventReward> EENC_REWARDS = new FastList();
  public static final FastMap<Integer, FastList<Location>> EENC_POINTS = new FastMap().shared("Config.EENC_POINTS");
  public static boolean ANARCHY_ENABLE;
  public static int ANARCHY_DAY;
  public static int ANARCHY_HOUR;
  public static long ANARCHY_DELAY;
  public static final FastList<Integer> ANARCHY_TOWNS = new FastList();
  public static boolean FIGHTING_ENABLE;
  public static int FIGHTING_DAY;
  public static int FIGHTING_HOUR;
  public static long FIGHTING_REGTIME;
  public static long FIGHTING_ANNDELAY;
  public static long FIGHTING_TPDELAY;
  public static long FIGHTING_FIGHTDELAY;
  public static int FIGHTING_MINLVL;
  public static int FIGHTING_MINP;
  public static int FIGHTING_MAXP;
  public static int FIGHTING_TICKETID;
  public static int FIGHTING_TICKETCOUNT;
  public static int FIGHTING_NPCID;
  public static Location FIGHTING_NPCLOC;
  public static Location FIGHTING_TPLOC;
  public static boolean EVENTS_SAME_IP;
  public static int DEADLOCKCHECK_INTERVAL;
  public static int RESTART_HOUR;
  public static boolean ALLOW_FAKE_PLAYERS;
  public static int FAKE_PLAYERS_PERCENT;
  public static int FAKE_PLAYERS_DELAY;
  public static final FastTable<Integer> F_OLY_ITEMS = new FastTable();
  public static boolean INVIS_SHOW;
  public static long NPC_SPAWN_DELAY;
  public static int NPC_SPAWN_TYPE;
  public static int MULT_ENCH;
  public static final FastMap<Integer, Integer> MULT_ENCHS = new FastMap().shared("Config.MULT_ENCHS");
  public static long CLAN_CH_CLEAN;
  public static boolean CHECK_SKILLS;
  public static boolean CLEAR_BUFF_ONDEATH;
  public static float ONLINE_PERC;
  public static String SERVER_SERIAL_KEY;
  public static boolean CMD_MENU;
  public static boolean VS_NOEXP;
  public static boolean VS_NOREQ;
  public static boolean VS_VREF;
  public static boolean VS_ONLINE;
  public static boolean VS_AUTORESTAT;
  public static boolean VS_CHATIGNORE;
  public static boolean VS_AUTOLOOT;
  public static boolean VS_TRADERSIGNORE;
  public static boolean VS_PATHFIND;
  public static boolean VS_SKILL_CHANCES;
  public static boolean VS_ANIM_SHOTS;
  public static boolean VS_HWID;
  public static boolean VS_PWD;
  public static boolean VS_EMAIL;
  public static boolean CMD_ADENA_COL;
  public static EventReward CMD_AC_ADENA;
  public static EventReward CMD_AC_COL;
  public static int CMD_AC_ADENA_LIMIT;
  public static int CMD_AC_COL_LIMIT;
  public static boolean CMD_EVENTS;
  public static int MAX_BAN_CHAT;
  public static boolean VS_CKEY;
  public static boolean VS_CKEY_CHARLEVEL;
  public static int PWHERO_COIN;
  public static int PWHERO_PRICE;
  public static int PWHERO_FPRICE;
  public static int PWHERO_MINDAYS;
  public static int PWHERO_TRANPRICE;
  public static String PWHERO_COINNAME;
  public static int PWCSKILLS_COIN;
  public static int PWCSKILLS_PRICE;
  public static String PWCSKILLS_COINNAME;
  public static int PWENCHSKILL_COIN;
  public static int PWENCHSKILL_PRICE;
  public static String PWENCHSKILL_COINNAME;
  public static int PWCNGSKILLS_COIN;
  public static int PWCNGSKILLS_PRICE;
  public static String PWCNGSKILLS_COINNAME;
  public static final FastMap<Integer, Integer> PWCSKILLS = new FastMap().shared("Config.PWCSKILLS");
  public static boolean PWTCOLOR_PAYMENT;
  public static int PWNCOLOR_COIN;
  public static int PWNCOLOR_PRICE;
  public static String PWNCOLOR_COINNAME;
  public static int PWTCOLOR_COIN;
  public static int PWTCOLOR_PRICE;
  public static String PWTCOLOR_COINNAME;
  public static final FastMap<Integer, AltBColor> PWCOLOURS = new FastMap().shared("Config.PWCOLOURS");
  public static int PWCNGCLASS_COIN;
  public static int PWCNGCLASS_PRICE;
  public static String PWCNGCLASS_COINNAME;
  public static int EXPOSTB_COIN;
  public static int EXPOSTB_PRICE;
  public static String EXPOSTB_NAME;
  public static int EXPOSTA_COIN;
  public static int EXPOSTA_PRICE;
  public static String EXPOSTA_NAME;
  public static boolean PREMIUM_ENABLE;
  public static int PREMIUM_COIN;
  public static int PREMIUM_PRICE;
  public static String PREMIUM_COINNAME;
  public static final FastMap<Integer, Integer> PREMIUM_DAY_PRICES = new FastMap().shared("Config.PREMIUM_DAY_PRICES");
  public static double PREMIUM_EXP;
  public static double PREMIUM_SP;
  public static double PREMIUM_ITEMDROP;
  public static double PREMIUM_ITEMDROPMUL;
  public static double PREMIUM_SPOILRATE;
  public static double PREMIUM_ADENAMUL;
  public static double PREMIUM_PCCAFE_MUL;
  public static double PREMIUM_AQURE_SKILL_MUL;
  public static int PREMIUM_AUGMENT_RATE;
  public static int PREMIUM_ENCH_ITEM;
  public static int PREMIUM_ENCH_SKILL;
  public static int PREMIUM_CURSED_RATE;
  public static boolean PREMIUM_ANY_SUBCLASS;
  public static boolean PREMIUM_CHKSKILLS;
  public static boolean PREMIUM_PKDROP_OFF;
  public static boolean PREMIUM_ANOOUNCE;
  public static boolean PREMIUM_ENCHANT_FAIL;
  public static String PREMIUM_ANNOUNCE_PHRASE;
  public static String PREMIUM_NAME_PREFIX;
  public static int PREMIUM_START_DAYS;
  public static final FastList<Integer> PREMIUM_PROTECTED_ITEMS = new FastList();
  public static boolean L2TOP_ENABLE;
  public static int L2TOP_SERV_ID;
  public static String L2TOP_SERV_KEY;
  public static int L2TOP_UPDATE_DELAY;
  public static int L2TOP_OFFLINE_ITEM;
  public static int L2TOP_OFFLINE_COUNT;
  public static String L2TOP_OFFLINE_LOC;
  public static final FastList<EventReward> L2TOP_ONLINE_REWARDS = new FastList();
  public static int L2TOP_LOGTYPE;
  public static boolean MMOTOP_ENABLE;
  public static String MMOTOP_STAT_LINK;
  public static int MMOTOP_UPDATE_DELAY;
  public static int MMOTOP_OFFLINE_ITEM;
  public static int MMOTOP_OFFLINE_COUNT;
  public static String MMOTOP_OFFLINE_LOC;
  public static final FastList<EventReward> MMOTOP_ONLINE_REWARDS = new FastList();
  public static int MMOTOP_LOGTYPE;
  public static boolean RAID_CUSTOM_DROP;
  public static final FastList<EventReward> NPC_RAID_REWARDS = new FastList();
  public static final FastList<EventReward> NPC_EPIC_REWARDS = new FastList();
  public static long ANTARAS_CLOSE_PORT;
  public static long ANTARAS_UPDATE_LAIR;
  public static long ANTARAS_MIN_RESPAWN;
  public static long ANTARAS_MAX_RESPAWN;
  public static long ANTARAS_RESTART_DELAY;
  public static long ANTARAS_SPAWN_DELAY;
  public static long VALAKAS_CLOSE_PORT;
  public static long VALAKAS_UPDATE_LAIR;
  public static long VALAKAS_MIN_RESPAWN;
  public static long VALAKAS_MAX_RESPAWN;
  public static long VALAKAS_RESTART_DELAY;
  public static long VALAKAS_SPAWN_DELAY;
  public static long BAIUM_CLOSE_PORT;
  public static long BAIUM_UPDATE_LAIR;
  public static long BAIUM_MIN_RESPAWN;
  public static long BAIUM_MAX_RESPAWN;
  public static long BAIUM_RESTART_DELAY;
  public static long AQ_MIN_RESPAWN;
  public static long AQ_MAX_RESPAWN;
  public static long AQ_RESTART_DELAY;
  public static long AQ_PLAYER_MAX_LVL;
  public static long AQ_NURSE_RESPAWN;
  public static long ZAKEN_MIN_RESPAWN;
  public static long ZAKEN_MAX_RESPAWN;
  public static long ZAKEN_RESTART_DELAY;
  public static boolean ALLOW_HIT_NPC;
  public static boolean KILL_NPC_ATTACKER;
  public static boolean ANNOUNCE_EPIC_STATES;
  public static boolean ENCHANT_ALT_PACKET;
  public static boolean ENCHANT_PENALTY;
  public static int ENCHANT_ALT_MAGICCAHNCE;
  public static final FastMap<Integer, Integer> ENCHANT_ALT_MAGICSTEPS = new FastMap().shared("Config.ENCHANT_ALT_MAGICSTEPS");
  public static int ENCHANT_ALT_WEAPONCAHNCE;
  public static final FastMap<Integer, Integer> ENCHANT_ALT_WEAPONSTEPS = new FastMap().shared("Config.ENCHANT_ALT_WEAPONSTEPS");
  public static int ENCHANT_ALT_WEAPONFAILBLESS;
  public static int ENCHANT_ALT_WEAPONFAILCRYST;
  public static int ENCHANT_ALT_ARMORCAHNCE;
  public static final FastMap<Integer, Integer> ENCHANT_ALT_ARMORSTEPS = new FastMap().shared("Config.ENCHANT_ALT_ARMORSTEPS");
  public static int ENCHANT_ALT_ARMORFAILBLESS;
  public static int ENCHANT_ALT_ARMORFAILCRYST;
  public static int ENCHANT_ALT_JEWERLYCAHNCE;
  public static final FastMap<Integer, Integer> ENCHANT_ALT_JEWERLYSTEPS = new FastMap().shared("Config.ENCHANT_ALT_JEWERLYSTEPS");
  public static int ENCHANT_ALT_JEWERLYFAILBLESS;
  public static int ENCHANT_ALT_JEWERLYFAILCRYST;
  public static boolean ENCHANT_HERO_WEAPONS;
  public static boolean ENCH_ANTI_CLICK;
  public static int ENCH_ANTI_CLICK_STEP;
  public static boolean ALLOW_PVPPK_REWARD;
  public static int PVPPK_INTERVAL;
  public static PvpColor PVPPK_PENALTY;
  public static boolean PVPPK_IPPENALTY;
  public static final FastList<PvpColor> PVPPK_EXP_SP = new FastList();
  public static final FastList<EventReward> PVPPK_PVPITEMS = new FastList();
  public static final FastList<EventReward> PVPPK_PKITEMS = new FastList();
  public static boolean ALLOW_PVPBONUS_STEPS;
  public static final FastMap<Integer, PvpColor> PVPBONUS_ITEMS = new FastMap().shared("Config.PVPBONUS_ITEMS");
  public static final FastMap<Integer, PvpColor> PVPBONUS_COLORS = new FastMap().shared("Config.PVPBONUS_COLORS");
  public static final FastList<Integer> PVPBONUS_COLORS_NAME = new FastList();
  public static final FastList<Integer> PVPBONUS_COLORS_TITLE = new FastList();
  public static int PVPPK_STEP;
  public static long PVPPK_STEPBAN;
  public static boolean PVPPK_REWARD_ZONE;
  public static final FastMap<Integer, Integer> MULTVIP_CARDS = new FastMap().shared("Config.MULTVIP_CARDS");

  public static final FastMap<Integer, FastList<EventReward>> CASTLE_SIEGE_REWARDS = new FastMap().shared("Config.CASTLE_SIEGE_REWARDS");
  public static boolean ALLOW_APELLA_BONUSES;
  public static boolean BBS_ONLY_PEACE;
  public static final FastList<Integer> TVT_WHITE_POTINS = new FastList();

  public static int ALT_OLY_REG_DISPLAY = 100;
  public static int ALT_OLY_BATTLE_REWARD_ITEM = 13722;
  public static int ALT_OLY_CLASSED_RITEM_C = 50;
  public static int ALT_OLY_NONCLASSED_RITEM_C = 40;
  public static int ALT_OLY_RANDOM_TEAM_RITEM_C = 30;
  public static int ALT_OLY_TEAM_RITEM_C = 50;
  public static int ALT_OLY_COMP_RITEM = 13722;
  public static int ALT_OLY_GP_PER_POINT = 1000;
  public static int ALT_OLY_HERO_POINTS = 180;
  public static int ALT_OLY_RANK1_POINTS = 120;
  public static int ALT_OLY_RANK2_POINTS = 80;
  public static int ALT_OLY_RANK3_POINTS = 55;
  public static int ALT_OLY_RANK4_POINTS = 35;
  public static int ALT_OLY_RANK5_POINTS = 20;

  public static boolean SHOW_ENTER_WARNINGS = false;
  public static boolean PC_CAFE_ENABLED;
  public static int PC_CAFE_INTERVAL;
  public static PvpColor PC_CAFE_BONUS;
  public static int PC_CAFE_DOUBLE_CHANCE;
  public static int WEBSERVER_REFRESH_STATS;
  public static int WEBSERVER_PORT;
  public static String WEBSERVER_FOLDER;
  public static String WEBSERVER_PAGE;
  public static int ALT_BUFF_TIMEMUL;
  public static final FastMap<Integer, Integer> ALT_BUFF_TIME = new FastMap().shared("Config.ALT_BUFF_TIME");

  public static final FastMap<Integer, Integer> ALT_SKILL_CHANSE = new FastMap().shared("Config.ALT_SKILL_CHANSE");
  public static boolean HERO_ITEMS_PENALTY;
  public static final FastList<Integer> ALT_MAGIC_WEAPONS = new FastList();

  public static final FastMap<Integer, Integer> CUSTOM_STRT_ITEMS = new FastMap().shared("Config.CUSTOM_STRT_ITEMS");
  public static boolean ACADEMY_CLASSIC;
  public static int ACADEMY_POINTS;
  public static boolean DISABLE_FORCES;
  public static int MAX_TRADE_ENCHANT;
  public static int ALT_OLYMPIAD_PERIOD;
  public static boolean ALLOW_CURSED_QUESTS;
  public static boolean BBS_CURSED_SHOP;
  public static boolean BBS_CURSED_TELEPORT;
  public static boolean BBS_CURSED_BUFF;
  public static String CHAT_FILTER_STRING;
  public static final FastTable<String> CHAT_FILTER_STRINGS = new FastTable();
  public static int ALT_SIEGE_INTERVAL;
  public static final FastMap<Integer, Integer> ENCHANT_LIMITS = new FastMap().shared("Config.ENCHANT_LIMITS");
  public static boolean SOULSHOT_ANIM;
  public static boolean PROTECT_GATE_PVP;
  public static boolean PROTECT_OLY_SOB;
  public static int WEDDING_ANSWER_TIME;
  public static int RESURECT_ANSWER_TIME;
  public static int SUMMON_ANSWER_TIME;
  public static int MAX_HENNA_BONUS;
  public static final FastList<Integer> ALT_FIXED_REUSES = new FastList();
  public static int MAX_AUGMENTS_BUFFS;
  public static boolean ALT_ANY_SUBCLASS;
  public static boolean ALT_ANY_SUBCLASS_OVERCRAF;
  public static EventTerritory TVT_POLY;
  public static EventTerritory LASTHERO_POLY;
  public static EventTerritory MASSPVP_POLY;
  public static EventTerritory BASECAPTURE_POLY;
  public static boolean ALT_AUGMENT_HERO;
  public static final FastList<Integer> PROTECTED_BUFFS = new FastList();
  public static double SKILLS_CHANCE_MIN;
  public static double SKILLS_CHANCE_MAX;
  public static String STARTUP_TITLE;
  public static long PICKUP_PENALTY;
  public static boolean DISABLE_BOSS_INTRO;
  public static boolean DEATH_REFLECT;
  public static boolean ALT_RESTORE_OFFLINE_TRADE;
  public static long ALT_OFFLINE_TRADE_LIMIT;
  public static boolean ALLOW_FAKE_PLAYERS_PLUS;
  public static int FAKE_PLAYERS_PLUS_COUNT;
  public static long FAKE_PLAYERS_PLUS_DELAY_SPAWN;
  public static int FAKE_PLAYERS_PLUS_COUNT_FIRST;
  public static long FAKE_PLAYERS_PLUS_DELAY_FIRST;
  public static int FAKE_PLAYERS_PLUS_DELAY_SPAWN_FIRST;
  public static int FAKE_PLAYERS_PLUS_DELAY_DESPAWN_FIRST;
  public static long FAKE_PLAYERS_PLUS_DESPAWN_FIRST;
  public static int FAKE_PLAYERS_PLUS_COUNT_NEXT;
  public static long FAKE_PLAYERS_PLUS_DELAY_NEXT;
  public static int FAKE_PLAYERS_PLUS_DELAY_SPAWN_NEXT;
  public static int FAKE_PLAYERS_PLUS_DELAY_DESPAWN_NEXT;
  public static long FAKE_PLAYERS_PLUS_DESPAWN_NEXT;
  public static PvpColor FAKE_PLAYERS_ENCHANT;
  public static final FastList<Integer> FAKE_PLAYERS_NAME_CLOLORS = new FastList();
  public static final FastList<Integer> FAKE_PLAYERS_TITLE_CLOLORS = new FastList();
  public static boolean ALT_OFFLINE_TRADE_ONLINE;
  public static boolean PROTECT_SAY;
  public static long PROTECT_SAY_BAN;
  public static int PROTECT_SAY_COUNT;
  public static long PROTECT_SAY_INTERVAL;
  public static boolean CACHED_SERVER_STAT;
  public static boolean ALLOW_FALL;
  public static boolean KARMA_PK_NPC_DROP;
  public static int ENCH_NPC_CAHNCE;
  public static PvpColor ENCH_NPC_MINMAX;
  public static int ENCH_MONSTER_CAHNCE;
  public static PvpColor ENCH_MONSTER_MINMAX;
  public static int ENCH_GUARD_CAHNCE;
  public static PvpColor ENCH_GUARD_MINMAX;
  public static boolean ENCH_STACK_SCROLLS;
  public static int CLANHALL_PAYMENT;
  public static int MIRAGE_CHANCE;
  public static boolean SUMMON_CP_PROTECT;
  public static final FastList<Integer> FORBIDDEN_BOW_CLASSES = new FastList();
  public static boolean ALLOW_NPC_CHAT;
  public static int MNPC_CHAT_CHANCE;
  public static int FRINTA_MMIN_PARTIES;
  public static int FRINTA_MMIN_PLAYERS;
  public static boolean FORBIDDEN_EVENT_ITMES;
  public static int VS_AUTOLOOT_VAL;
  public static int VS_PATHFIND_VAL;
  public static int VS_SKILL_CHANCES_VAL;
  public static PvpColor WEDDING_COLORS;
  public static final FastMap<Integer, Integer> OLY_MAGE_BUFFS = new FastMap().shared("Config.OLY_MAGE_BUFFS");
  public static final FastMap<Integer, Integer> OLY_FIGHTER_BUFFS = new FastMap().shared("Config.OLY_FIGHTER_BUFFS");
  public static boolean MULTISSELL_PROTECT;
  public static boolean MULTISSELL_ERRORS;
  public static int CHEST_CHANCE;
  public static boolean WEBSTAT_ENABLE;
  public static int WEBSTAT_INTERVAL;
  public static int WEBSTAT_INTERVAL2;
  public static int WEBSTAT_INTERVAL3;
  public static boolean WEBSTAT_KILLS;
  public static boolean WEBSTAT_CHEATS;
  public static int WEBSTAT_ENCHANT;
  public static boolean WEBSTAT_EPICLOOT;
  public static boolean ALT_OLY_RELOAD_SKILLS;
  public static int MOB_DEBUFF_CHANCE;
  public static boolean QUED_ITEMS_ENABLE;
  public static int QUED_ITEMS_INTERVAL;
  public static int QUED_ITEMS_LOGTYPE;
  public static float RATE_MUL_SEAL_STONE;
  public static float RATE_DROP_SEAL_STONE;
  public static boolean EVENT_SPECIAL_DROP;
  public static float RATE_DROP_ITEMSRAIDMUL;
  public static float RATE_DROP_ITEMSGRANDMUL;
  public static boolean FC_INSERT_INVENTORY;
  public static int OLY_MAX_WEAPON_ENCH;
  public static int OLY_MAX_ARMOT_ENCH;
  public static final FastList<Integer> NPC_HIT_PROTECTET = new FastList();
  public static boolean CONSOLE_ADVANCED;
  public static boolean BARAKIEL_NOBLESS;
  public static boolean GAMEGUARD_ENABLED;
  public static int GAMEGUARD_KEY = -1;
  public static int GAMEGUARD_INTERVAL;
  public static int GAMEGUARD_PUNISH;
  public static boolean GAMEGUARD_LOG;
  public static boolean NOBLES_ENABLE;
  public static int SNOBLE_COIN;
  public static int SNOBLE_PRICE;
  public static String SNOBLE_COIN_NAME;
  public static int MAX_EXP_LEVEL;
  public static boolean FREE_PVP;
  public static boolean PROTECT_GRADE_PVP;
  public static boolean CLEAR_OLY_BAN;
  public static boolean GIVE_ITEM_PET;
  public static boolean DISABLE_PET_FEED;
  public static boolean ENCHANT_ALT_FORMULA;
  public static boolean SIEGE_GUARDS_SPAWN;
  public static long TELEPORT_PROTECTION;
  public static int MAX_MATKSPD_DELAY;
  public static int MAX_PATKSPD_DELAY;
  public static int MAX_MATK_CALC;
  public static int MAX_MDEF_CALC;
  public static int MIN_ATKSPD_DELAY;
  public static final FastMap<Integer, EventReward> CASTLE_SIEGE_SKILLS = new FastMap().shared("Config.CASTLE_SIEGE_SKILLS");
  public static int FAKE_MAX_PATK_BOW;
  public static int FAKE_MAX_MDEF_BOW;
  public static int FAKE_MAX_PSPD_BOW;
  public static int FAKE_MAX_PDEF_BOW;
  public static int FAKE_MAX_MATK_BOW;
  public static int FAKE_MAX_MSPD_BOW;
  public static int FAKE_MAX_HP_BOW;
  public static int FAKE_MAX_PATK_MAG;
  public static int FAKE_MAX_MDEF_MAG;
  public static int FAKE_MAX_PSPD_MAG;
  public static int FAKE_MAX_PDEF_MAG;
  public static int FAKE_MAX_MATK_MAG;
  public static int FAKE_MAX_MSPD_MAG;
  public static int FAKE_MAX_HP_MAG;
  public static int FAKE_MAX_PATK_HEAL;
  public static int FAKE_MAX_MDEF_HEAL;
  public static int FAKE_MAX_PSPD_HEAL;
  public static int FAKE_MAX_PDEF_HEAL;
  public static int FAKE_MAX_MATK_HEAL;
  public static int FAKE_MAX_MSPD_HEAL;
  public static int FAKE_MAX_HP_HEAL;
  public static int ENCHANT_ALT_STEP;
  public static Location NPC_HIT_LOCATION;
  public static boolean KICK_USED_ACCOUNT;
  public static int RAID_CLANPOINTS_REWARD;
  public static int EPIC_CLANPOINTS_REWARD;
  public static boolean DISABLE_CLAN_REQUREMENTS;
  public static Location ZAKEN_SPAWN_LOC;
  public static int BBS_CNAME_COIN;
  public static int BBS_CNAME_PRICE;
  public static String BBS_CNAME_VAL;
  public static final FastList<Integer> HIPPY_ITEMS = new FastList();
  public static boolean PROTECT_MOBS_ITEMS;
  public static int BOSS_ZONE_MAX_ENCH;
  public static int MOUNT_EXPIRE;
  public static final FastList<Integer> BOSS_ITEMS = new FastList();

  public static final FastList<Integer> FORB_CURSED_SKILLS = new FastList();
  public static int HEALSUM_ANIM;
  public static long HEALSUM_DELAY;
  public static final FastMap<Integer, EventReward> HEALING_SUMMONS = new FastMap().shared("Config.HEALING_SUMMONS");

  public static void loadServerCfg()
  {
    AbstractLogger.init();
    try {
      Properties serverSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/server.cfg"));
      serverSettings.load(is);
      is.close();

      GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
      PORT_GAME = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));

      EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "*");
      INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");

      GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
      GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");

      REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
      ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));

      DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
      DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
      DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
      DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
      DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));

      MINCONNECTIONSPERPARTITION = Integer.parseInt(serverSettings.getProperty("MinConnectionsPerPartition", "10"));
      MAXCONNECTIONSPERPARTITION = Integer.parseInt(serverSettings.getProperty("MaxConnectionsPerPartition", "30"));
      PARTITIONCOUNT = Integer.parseInt(serverSettings.getProperty("PartitionCount", "5"));
      ACQUIREINCREMENT = Integer.parseInt(serverSettings.getProperty("AcquireIncrement", "5"));
      IDLECONNECTIONTESTPERIOD = Integer.parseInt(serverSettings.getProperty("IdleConnectionTestPeriod", "10"));
      IDLEMAXAGE = Integer.parseInt(serverSettings.getProperty("IdleMaxAge", "10"));
      RELEASEHELPERTHREADS = Integer.parseInt(serverSettings.getProperty("ReleaseHelperThreads", "5"));
      ACQUIRERETRYDELAY = Integer.parseInt(serverSettings.getProperty("AcquireRetryDelay", "7000"));
      ACQUIRERETRYATTEMPTS = Integer.parseInt(serverSettings.getProperty("AcquireRetryAttempts", "5"));
      QUERYEXECUTETIMELIMIT = Integer.parseInt(serverSettings.getProperty("QueryExecuteTimeLimit", "0"));
      CONNECTIONTIMEOUT = Integer.parseInt(serverSettings.getProperty("ConnectionTimeout", "0"));

      LAZYINIT = Boolean.parseBoolean(serverSettings.getProperty("LazyInit", "False"));
      TRANSACTIONRECOVERYENABLED = Boolean.parseBoolean(serverSettings.getProperty("TransactionRecoveryEnabled", "False"));

      DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();

      CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
      PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");

      MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "0"));
      MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));

      MIN_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "660"));
      MAX_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "665"));
      SHOW_PROTOCOL_VERSIONS = Boolean.parseBoolean(serverSettings.getProperty("ShowProtocolsInConsole", "False"));
      if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION) {
        throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
      }
      DEADLOCKCHECK_INTERVAL = Integer.parseInt(serverSettings.getProperty("DeadLockCheck", "10000"));
      RESTART_HOUR = Integer.parseInt(serverSettings.getProperty("AutoRestartHour", "0"));
      SERVER_SERIAL_KEY = serverSettings.getProperty("SerialKey", "None");
      if ((SERVER_SERIAL_KEY.equals("None")) || (SERVER_SERIAL_KEY.length() < 40))
      {
        SERVER_SERIAL_KEY = "wq34t43gt34t4g4ge4g";
      }

      WEBSERVER_REFRESH_STATS = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serverSettings.getProperty("WebServerRefreshTime", "15")));
      WEBSERVER_PORT = Integer.parseInt(serverSettings.getProperty("WebServerPort", "0"));
      WEBSERVER_FOLDER = serverSettings.getProperty("WebServerFolder", "data/webserver/");
      WEBSERVER_PAGE = serverSettings.getProperty("WebServerIndex", "index.html");

      WEBSTAT_ENABLE = Boolean.parseBoolean(serverSettings.getProperty("WebStatEnable", "False"));
      WEBSTAT_INTERVAL = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serverSettings.getProperty("WebStatRefreshTime", "5")));
      WEBSTAT_INTERVAL2 = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serverSettings.getProperty("WebStatRefreshTimeEx", "30")));

      WEBSTAT_INTERVAL3 = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serverSettings.getProperty("WebStatRefreshTimeOa", "5")));
      WEBSTAT_ENCHANT = Integer.parseInt(serverSettings.getProperty("WebStatEnchant", "-1"));
      WEBSTAT_KILLS = Boolean.parseBoolean(serverSettings.getProperty("WebStatKills", "False"));
      WEBSTAT_CHEATS = Boolean.parseBoolean(serverSettings.getProperty("WebStatCheats", "False"));
      WEBSTAT_EPICLOOT = Boolean.parseBoolean(serverSettings.getProperty("WebStatEpicLoot", "False"));

      CONSOLE_ADVANCED = Boolean.parseBoolean(serverSettings.getProperty("WindowsAdvancedConsole", "False"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/server.cfg File.");
    }
  }

  public static void loadOptionsCfg() {
    LIST_PROTECTED_ITEMS.clear();
    LOG_MULTISELL_ID.clear();
    try
    {
      Properties optionsSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/options.cfg"));
      optionsSettings.load(is);
      is.close();

      EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(optionsSettings.getProperty("EverybodyHasAdminRights", "false"));

      DEBUG = Boolean.parseBoolean(optionsSettings.getProperty("Debug", "false"));
      ASSERT = Boolean.parseBoolean(optionsSettings.getProperty("Assert", "false"));
      DEVELOPER = Boolean.parseBoolean(optionsSettings.getProperty("Developer", "false"));
      TEST_SERVER = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
      SERVER_LIST_TESTSERVER = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));

      SERVER_LIST_BRACKET = Boolean.valueOf(optionsSettings.getProperty("ServerListBrackets", "false")).booleanValue();
      SERVER_LIST_CLOCK = Boolean.valueOf(optionsSettings.getProperty("ServerListClock", "false")).booleanValue();
      SERVER_GMONLY = Boolean.valueOf(optionsSettings.getProperty("ServerGMOnly", "false")).booleanValue();

      AUTODESTROY_ITEM_AFTER = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
      HERB_AUTO_DESTROY_TIME = Integer.parseInt(optionsSettings.getProperty("AutoDestroyHerbTime", "15")) * 1000;
      PROTECTED_ITEMS = optionsSettings.getProperty("ListOfProtectedItems");
      LIST_PROTECTED_ITEMS = new FastList();
      for (String id : PROTECTED_ITEMS.split(",")) {
        LIST_PROTECTED_ITEMS.add(Integer.valueOf(Integer.parseInt(id)));
      }
      DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyPlayerDroppedItem", "false")).booleanValue();
      DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyEquipableItem", "false")).booleanValue();
      SAVE_DROPPED_ITEM = Boolean.valueOf(optionsSettings.getProperty("SaveDroppedItem", "false")).booleanValue();
      EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(optionsSettings.getProperty("EmptyDroppedItemTableAfterLoad", "false")).booleanValue();
      SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(optionsSettings.getProperty("SaveDroppedItemInterval", "0")) * 60000;
      CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(optionsSettings.getProperty("ClearDroppedItemTable", "false")).booleanValue();

      PRECISE_DROP_CALCULATION = Boolean.valueOf(optionsSettings.getProperty("PreciseDropCalculation", "True")).booleanValue();
      MULTIPLE_ITEM_DROP = Boolean.valueOf(optionsSettings.getProperty("MultipleItemDrop", "True")).booleanValue();

      COORD_SYNCHRONIZE = Integer.parseInt(optionsSettings.getProperty("CoordSynchronize", "-1"));

      ONLY_GM_ITEMS_FREE = Boolean.valueOf(optionsSettings.getProperty("OnlyGMItemsFree", "True")).booleanValue();

      ALLOW_WAREHOUSE = Boolean.valueOf(optionsSettings.getProperty("AllowWarehouse", "True")).booleanValue();
      WAREHOUSE_CACHE = Boolean.valueOf(optionsSettings.getProperty("WarehouseCache", "False")).booleanValue();
      WAREHOUSE_CACHE_TIME = Integer.parseInt(optionsSettings.getProperty("WarehouseCacheTime", "15"));
      ALLOW_FREIGHT = Boolean.valueOf(optionsSettings.getProperty("AllowFreight", "True")).booleanValue();
      ALLOW_WEAR = Boolean.valueOf(optionsSettings.getProperty("AllowWear", "False")).booleanValue();
      WEAR_DELAY = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
      WEAR_PRICE = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
      ALLOW_LOTTERY = Boolean.valueOf(optionsSettings.getProperty("AllowLottery", "False")).booleanValue();
      ALLOW_RACE = Boolean.valueOf(optionsSettings.getProperty("AllowRace", "False")).booleanValue();

      ALLOW_WATER = Boolean.valueOf(optionsSettings.getProperty("AllowWater", "True")).booleanValue();

      ALLOW_FALL = false;

      ALLOW_RENTPET = Boolean.valueOf(optionsSettings.getProperty("AllowRentPet", "False")).booleanValue();
      ALLOW_DISCARDITEM = Boolean.valueOf(optionsSettings.getProperty("AllowDiscardItem", "True")).booleanValue();
      ALLOWFISHING = Boolean.valueOf(optionsSettings.getProperty("AllowFishing", "False")).booleanValue();
      ALLOW_MANOR = Boolean.parseBoolean(optionsSettings.getProperty("AllowManor", "False"));
      ALLOW_BOAT = Boolean.valueOf(optionsSettings.getProperty("AllowBoat", "False")).booleanValue();
      ALLOW_NPC_WALKERS = Boolean.valueOf(optionsSettings.getProperty("AllowNpcWalkers", "true")).booleanValue();
      ALLOW_CURSED_WEAPONS = Boolean.valueOf(optionsSettings.getProperty("AllowCursedWeapons", "False")).booleanValue();

      ALLOW_L2WALKER_CLIENT = L2WalkerAllowed.valueOf(optionsSettings.getProperty("AllowL2Walker", "False"));
      L2WALKER_REVISION = Integer.parseInt(optionsSettings.getProperty("L2WalkerRevision", "537"));
      AUTOBAN_L2WALKER_ACC = Boolean.valueOf(optionsSettings.getProperty("AutobanL2WalkerAcc", "False")).booleanValue();
      GM_EDIT = Boolean.valueOf(optionsSettings.getProperty("GMEdit", "False")).booleanValue();

      ACTIVATE_POSITION_RECORDER = Boolean.valueOf(optionsSettings.getProperty("ActivatePositionRecorder", "False")).booleanValue();

      DEFAULT_GLOBAL_CHAT = optionsSettings.getProperty("GlobalChat", "ON");
      DEFAULT_TRADE_CHAT = optionsSettings.getProperty("TradeChat", "ON");

      LOG_CHAT = Boolean.valueOf(optionsSettings.getProperty("LogChat", "false")).booleanValue();
      LOG_ITEMS = Boolean.valueOf(optionsSettings.getProperty("LogItems", "false")).booleanValue();
      String[] propertySplit = optionsSettings.getProperty("LogMultisell", "").split(",");
      for (String item : propertySplit) {
        try {
          LOG_MULTISELL_ID.add(Integer.valueOf(item));
        } catch (NumberFormatException nfe) {
          if (!item.equals("")) {
            System.out.println("options.cfg: LogMultisell error: " + item);
          }
        }
      }

      GMAUDIT = Boolean.valueOf(optionsSettings.getProperty("GMAudit", "False")).booleanValue();

      COMMUNITY_TYPE = optionsSettings.getProperty("CommunityType", "old").toLowerCase();
      BBS_DEFAULT = optionsSettings.getProperty("BBSDefault", "_bbshome");
      SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False")).booleanValue();
      SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True")).booleanValue();
      NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
      NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));

      BBS_CURSED_SHOP = Boolean.valueOf(optionsSettings.getProperty("BbsCursedShop", "True")).booleanValue();
      BBS_CURSED_TELEPORT = Boolean.valueOf(optionsSettings.getProperty("BbsCursedTeleport", "True")).booleanValue();
      BBS_CURSED_BUFF = Boolean.valueOf(optionsSettings.getProperty("BbsCursedBuff", "True")).booleanValue();

      ZONE_TOWN = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));

      MAX_DRIFT_RANGE = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));

      MIN_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "10"));
      MAX_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "20"));
      MIN_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinMonsterAnimation", "5"));
      MAX_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxMonsterAnimation", "20"));

      SERVER_NEWS = Boolean.valueOf(optionsSettings.getProperty("ShowServerNews", "False")).booleanValue();
      SHOW_NPC_LVL = Boolean.valueOf(optionsSettings.getProperty("ShowNpcLevel", "False")).booleanValue();

      FORCE_INVENTORY_UPDATE = Boolean.valueOf(optionsSettings.getProperty("ForceInventoryUpdate", "False")).booleanValue();

      AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False")).booleanValue();

      THREADING_MODEL = Integer.parseInt(optionsSettings.getProperty("ThreadingModel", "1"));

      THREAD_P_MOVE = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeMove", "50"));
      THREAD_P_EFFECTS = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeEffects", "20"));
      THREAD_P_GENERAL = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeGeneral", "26"));
      THREAD_P_PATHFIND = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizePathfind", "10"));

      IO_PACKET_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("UrgentPacketThreadCoreSize", "2"));

      GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("GeneralPacketThreadCoreSize", "8"));
      GENERAL_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("GeneralThreadCoreSize", "8"));

      NPC_AI_MAX_THREAD = Integer.parseInt(optionsSettings.getProperty("NpcAiMaxThread", "10"));
      PLAYER_AI_MAX_THREAD = Integer.parseInt(optionsSettings.getProperty("PlayerAiMaxThread", "20"));

      DELETE_DAYS = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));

      DEFAULT_PUNISH = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
      DEFAULT_PUNISH_PARAM = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));

      LAZY_CACHE = Boolean.valueOf(optionsSettings.getProperty("LazyCache", "False")).booleanValue();

      PACKET_LIFETIME = Integer.parseInt(optionsSettings.getProperty("PacketLifeTime", "0"));

      BYPASS_VALIDATION = Boolean.valueOf(optionsSettings.getProperty("BypassValidation", "True")).booleanValue();

      GAMEGUARD_ENFORCE = Boolean.valueOf(optionsSettings.getProperty("GameGuardEnforce", "False")).booleanValue();
      GAMEGUARD_PROHIBITACTION = Boolean.valueOf(optionsSettings.getProperty("GameGuardProhibitAction", "False")).booleanValue();

      GRIDS_ALWAYS_ON = Boolean.parseBoolean(optionsSettings.getProperty("GridsAlwaysOn", "False"));
      GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOnTime", "30"));
      GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOffTime", "300"));

      USE_3D_MAP = Boolean.valueOf(optionsSettings.getProperty("Use3DMap", "False")).booleanValue();

      PATH_NODE_RADIUS = Integer.parseInt(optionsSettings.getProperty("PathNodeRadius", "50"));
      NEW_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
      SELECTED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
      LINKED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
      NEW_NODE_TYPE = optionsSettings.getProperty("NewNodeType", "npc");

      COUNT_PACKETS = Boolean.valueOf(optionsSettings.getProperty("CountPacket", "false")).booleanValue();
      DUMP_PACKET_COUNTS = Boolean.valueOf(optionsSettings.getProperty("DumpPacketCounts", "false")).booleanValue();
      DUMP_INTERVAL_SECONDS = Integer.parseInt(optionsSettings.getProperty("PacketDumpInterval", "60"));

      MINIMUM_UPDATE_DISTANCE = Integer.parseInt(optionsSettings.getProperty("MaximumUpdateDistance", "50"));
      MINIMUN_UPDATE_TIME = Integer.parseInt(optionsSettings.getProperty("MinimumUpdateTime", "500"));
      CHECK_KNOWN = Boolean.valueOf(optionsSettings.getProperty("CheckKnownList", "false")).booleanValue();
      KNOWNLIST_FORGET_DELAY = Integer.parseInt(optionsSettings.getProperty("KnownListForgetDelay", "10000"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/options.cfg File.");
    }
  }

  public static void loadTelnetCfg() {
    try {
      Properties telnetSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/telnet.cfg"));
      telnetSettings.load(is);
      is.close();

      IS_TELNET_ENABLED = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false")).booleanValue();
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/telnet.cfg File.");
    }
  }

  public static void loadIdFactoryCfg() {
    try {
      Properties idSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/idfactory.cfg"));
      idSettings.load(is);
      is.close();

      MAP_TYPE = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
      SET_TYPE = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
      IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
      BAD_ID_CHECKING = Boolean.valueOf(idSettings.getProperty("BadIdChecking", "True")).booleanValue();
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/idfactory.cfg File.");
    }
  }

  public static void loadOtherCfg() {
    LIST_PET_RENT_NPC.clear();
    LIST_NONDROPPABLE_ITEMS.clear();
    try
    {
      Properties otherSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/other.cfg"));
      otherSettings.load(is);
      is.close();

      DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
      ALLOW_GUARDS = Boolean.valueOf(otherSettings.getProperty("AllowGuards", "False")).booleanValue();
      EFFECT_CANCELING = Boolean.valueOf(otherSettings.getProperty("CancelLesserEffect", "True")).booleanValue();
      ALLOW_WYVERN_UPGRADER = Boolean.valueOf(otherSettings.getProperty("AllowWyvernUpgrader", "False")).booleanValue();

      INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
      INVENTORY_MAXIMUM_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
      INVENTORY_MAXIMUM_GM = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
      MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));

      WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
      WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
      WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
      FREIGHT_SLOTS = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));

      AUGMENT_EXCLUDE_NOTDONE = Boolean.parseBoolean(otherSettings.getProperty("AugmentExcludeNotdone", "false"));

      HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100")) / 100.0D;
      MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100")) / 100.0D;
      CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100")) / 100.0D;

      RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "100")) / 100.0D;
      RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "100")) / 100.0D;
      RAID_DEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidDefenceMultiplier", "100")) / 100.0D;
      RAID_MINION_RESPAWN_TIMER = Integer.parseInt(otherSettings.getProperty("RaidMinionRespawnTime", "300000"));
      RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
      RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));

      UNSTUCK_INTERVAL = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));

      PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));

      PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerFakeDeathUpProtection", "0"));

      PARTY_XP_CUTOFF_METHOD = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
      PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
      PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));

      RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100.0D;
      RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100.0D;
      RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100.0D;

      RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False"));
      RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50"));

      MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
      MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));

      STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "true"));

      PET_RENT_NPC = otherSettings.getProperty("ListPetRentNpc", "30827");
      LIST_PET_RENT_NPC = new FastList();
      for (String id : PET_RENT_NPC.split(",")) {
        LIST_PET_RENT_NPC.add(Integer.valueOf(Integer.parseInt(id)));
      }
      NONDROPPABLE_ITEMS = otherSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");

      LIST_NONDROPPABLE_ITEMS = new FastList();
      for (String id : NONDROPPABLE_ITEMS.split(",")) {
        LIST_NONDROPPABLE_ITEMS.add(Integer.valueOf(Integer.parseInt(id)));
      }

      ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));

      ALT_PRIVILEGES_ADMIN = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesAdmin", "False"));
      ALT_PRIVILEGES_SECURE_CHECK = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesSecureCheck", "True"));
      ALT_PRIVILEGES_DEFAULT_LEVEL = Integer.parseInt(otherSettings.getProperty("AltPrivilegesDefaultLevel", "100"));

      MASTERACCESS_LEVEL = Integer.parseInt(otherSettings.getProperty("MasterAccessLevel", "127"));
      GM_HERO_AURA = Boolean.parseBoolean(otherSettings.getProperty("GMHeroAura", "True"));
      GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvulnerable", "True"));
      GM_STARTUP_INVISIBLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvisible", "True"));
      GM_STARTUP_SILENCE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupSilence", "True"));
      GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(otherSettings.getProperty("GMStartupAutoList", "True"));
      GM_ADMIN_MENU_STYLE = otherSettings.getProperty("GMAdminMenuStyle", "modern");

      PETITIONING_ALLOWED = Boolean.parseBoolean(otherSettings.getProperty("PetitioningAllowed", "True"));
      MAX_PETITIONS_PER_PLAYER = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPerPlayer", "5"));
      MAX_PETITIONS_PENDING = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPending", "25"));

      JAIL_IS_PVP = Boolean.valueOf(otherSettings.getProperty("JailIsPvp", "True")).booleanValue();
      JAIL_DISABLE_CHAT = Boolean.valueOf(otherSettings.getProperty("JailDisableChat", "True")).booleanValue();

      DEATH_PENALTY_CHANCE = Integer.parseInt(otherSettings.getProperty("DeathPenaltyChance", "20"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/other.cfg File.");
    }
  }

  public static void loadEnchantCfg() {
    ARMOR_ENCHANT_TABLE.clear();
    FULL_ARMOR_ENCHANT_TABLE.clear();
    MULT_ENCHS.clear();
    ENCHANT_ALT_MAGICSTEPS.clear();
    ENCHANT_ALT_WEAPONSTEPS.clear();
    ENCHANT_ALT_ARMORSTEPS.clear();
    ENCHANT_ALT_JEWERLYSTEPS.clear();
    ENCHANT_LIMITS.clear();
    try
    {
      Properties enchSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/enchants.cfg"));
      enchSettings.load(is);
      is.close();

      ENCHANT_MAX_WEAPON = Integer.parseInt(enchSettings.getProperty("EnchantMaxWeapon", "16"));
      ENCHANT_MAX_ARMOR = Integer.parseInt(enchSettings.getProperty("EnchantMaxArmor", "16"));
      ENCHANT_MAX_JEWELRY = Integer.parseInt(enchSettings.getProperty("EnchantMaxJewelry", "16"));

      ENCHANT_SAFE_MAX = Integer.parseInt(enchSettings.getProperty("EnchantSafeMax", "3"));

      ENCHANT_SAFE_MAX_FULL = Integer.parseInt(enchSettings.getProperty("EnchantSafeMaxFull", "4"));

      ENCHANT_CHANCE_WEAPON_CRYSTAL = Integer.parseInt(enchSettings.getProperty("EnchantChanceWeaponCrystal", "100"));
      ENCHANT_CHANCE_ARMOR_CRYSTAL = Integer.parseInt(enchSettings.getProperty("EnchantChanceArmorCrystal", "100"));
      ENCHANT_CHANCE_JEWELRY_CRYSTAL = Integer.parseInt(enchSettings.getProperty("EnchantChanceJewelryCrystal", "100"));

      ENCHANT_CHANCE_NEXT = Integer.parseInt(enchSettings.getProperty("EnchantXX", "15"));
      ENCHANT_FAILED_NUM = Integer.parseInt(enchSettings.getProperty("EnchantFailed", "0"));
      MAGIC_CHANCE_BEFORE_NEXT = Float.parseFloat(enchSettings.getProperty("MagicEnchantSuccesRateBeforeXX", "25.0"));
      MAGIC_CHANCE_AFTER_NEXT = Float.parseFloat(enchSettings.getProperty("MagicEnchantSuccesRateAfterXX", "35.0"));
      WEAPON_CHANCE_BEFORE_NEXT = Float.parseFloat(enchSettings.getProperty("WeaponEnchantSuccesRateBeforeXX", "30.0"));
      WEAPON_CHANCE_AFTER_NEXT = Float.parseFloat(enchSettings.getProperty("WeaponEnchantSuccesRateAfterXX", "30.0"));

      String[] ArmEncTable = enchSettings.getProperty("ArmorEnchantTable", "").split(";");
      for (String aet : ArmEncTable) {
        try {
          ARMOR_ENCHANT_TABLE.add(Float.valueOf(aet));
        } catch (NumberFormatException nfe) {
          if (!aet.equals("")) {
            System.out.println("invalid config property -> ArmorEnchantTable \"" + aet + "\"");
          }
        }
      }

      String[] FullArmEncTable = enchSettings.getProperty("FullArmorEnchantTable", "").split(";");
      for (String faet : FullArmEncTable) {
        try {
          FULL_ARMOR_ENCHANT_TABLE.add(Float.valueOf(faet));
        } catch (NumberFormatException nfe) {
          if (!faet.equals("")) {
            System.out.println("invalid config property -> ArmorEnchantTable \"" + faet + "\"");
          }
        }
      }

      MULT_ENCH = Integer.parseInt(enchSettings.getProperty("EnchMultisell", "0"));
      String[] propertySplit = enchSettings.getProperty("EnchMultisellLists", "0,0").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          MULT_ENCHS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("enchant.cfg: EnchMultisellLists error: " + aug[0]);
          }
        }
      }
      ENCHANT_PENALTY = Boolean.valueOf(enchSettings.getProperty("EnchantPenalty", "True")).booleanValue();
      ENCHANT_HERO_WEAPONS = Boolean.valueOf(enchSettings.getProperty("EnchHeroWeapons", "False")).booleanValue();

      ENCHANT_ALT_PACKET = true;
      if (ENCHANT_ALT_PACKET) {
        ENCHANT_ALT_MAGICCAHNCE = Integer.parseInt(enchSettings.getProperty("EnchantAltMagicChance", "65"));
        propertySplit = enchSettings.getProperty("EnchantAltMagicSteps", "1,100;2,100").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            ENCHANT_ALT_MAGICSTEPS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
          } catch (NumberFormatException nfe) {
            if (aug.length > 0) {
              System.out.println("enchant.cfg: EnchantAltMagicSteps error: " + aug[0]);
            }
          }
        }

        ENCHANT_ALT_WEAPONCAHNCE = Integer.parseInt(enchSettings.getProperty("EnchantAltWeaponChance", "75"));

        ENCHANT_ALT_WEAPONFAILBLESS = Integer.parseInt(enchSettings.getProperty("EnchantAltWeaponBlessFail", "0"));
        ENCHANT_ALT_WEAPONFAILCRYST = Integer.parseInt(enchSettings.getProperty("EnchantAltWeaponCrystallFail", "0"));
        propertySplit = enchSettings.getProperty("EnchantAltWeaponSteps", "1,100;2,100").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            ENCHANT_ALT_WEAPONSTEPS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
          } catch (NumberFormatException nfe) {
            if (aug.length > 0) {
              System.out.println("enchant.cfg: EnchantAltWeaponSteps error: " + aug[0]);
            }
          }
        }

        ENCHANT_ALT_ARMORCAHNCE = Integer.parseInt(enchSettings.getProperty("EnchantAltArmorChance", "75"));

        ENCHANT_ALT_ARMORFAILBLESS = Integer.parseInt(enchSettings.getProperty("EnchantAltArmorBlessFail", "0"));
        ENCHANT_ALT_ARMORFAILCRYST = Integer.parseInt(enchSettings.getProperty("EnchantAltArmorCrystallFail", "0"));
        propertySplit = enchSettings.getProperty("EnchantAltArmorSteps", "1,100;2,100").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            ENCHANT_ALT_ARMORSTEPS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
          } catch (NumberFormatException nfe) {
            if (aug.length > 0) {
              System.out.println("enchant.cfg: EnchantAltArmorSteps error: " + aug[0]);
            }
          }
        }

        ENCHANT_ALT_JEWERLYCAHNCE = Integer.parseInt(enchSettings.getProperty("EnchantAltJewerlyChance", "75"));

        ENCHANT_ALT_JEWERLYFAILBLESS = Integer.parseInt(enchSettings.getProperty("EnchantAltJewerlyBlessFail", "0"));
        ENCHANT_ALT_JEWERLYFAILCRYST = Integer.parseInt(enchSettings.getProperty("EnchantAltJewerlyCrystallFail", "0"));
        propertySplit = enchSettings.getProperty("EnchantAltJewerlySteps", "1,100;2,100").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            ENCHANT_ALT_JEWERLYSTEPS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
          } catch (NumberFormatException nfe) {
            if (aug.length > 0) {
              System.out.println("enchant.cfg: EnchantAltJewerlySteps error: " + aug[0]);
            }
          }
        }
      }
      ENCHANT_ALT_FORMULA = Boolean.valueOf(enchSettings.getProperty("AltEnchantFormula", "False")).booleanValue();

      ENCH_ANTI_CLICK = Boolean.parseBoolean(enchSettings.getProperty("AntiClick", "False"));
      ENCH_ANTI_CLICK_STEP = Integer.parseInt(enchSettings.getProperty("AntiClickStep", "10"));
      ENCH_ANTI_CLICK_STEP *= 2;

      propertySplit = enchSettings.getProperty("EnchantLimits", "0,0").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          ENCHANT_LIMITS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("enchant.cfg: EnchantLimits error: " + aug[0]);
          }
        }

      }

      ENCH_NPC_CAHNCE = Integer.parseInt(enchSettings.getProperty("NpcEnchantChance", "0"));
      propertySplit = enchSettings.getProperty("NpcEnchantMinMax", "0,14").split(",");
      ENCH_NPC_MINMAX = new PvpColor(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]));

      ENCH_MONSTER_CAHNCE = Integer.parseInt(enchSettings.getProperty("MonsterEnchantChance", "0"));
      propertySplit = enchSettings.getProperty("MonsterEnchantMinMax", "0,14").split(",");
      ENCH_MONSTER_MINMAX = new PvpColor(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]));

      ENCH_GUARD_CAHNCE = Integer.parseInt(enchSettings.getProperty("GuardEnchantChance", "0"));
      propertySplit = enchSettings.getProperty("GuardEnchantMinMax", "0,14").split(",");
      ENCH_GUARD_MINMAX = new PvpColor(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]));

      ENCH_STACK_SCROLLS = Boolean.parseBoolean(enchSettings.getProperty("StackableScrolls", "False"));

      ENCHANT_ALT_STEP = Integer.parseInt(enchSettings.getProperty("AltEnchantStep", "1"));
      propertySplit = null;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/enchants.cfg File.");
    }
  }

  public static void loadServicesCfg() {
    AUGSALE_TABLE.clear();
    M_BUFF.clear();
    F_BUFF.clear();
    F_PROFILE_BUFFS.clear();
    C_ALLOWED_BUFFS.clear();
    CLASS_MASTERS_PRICES.clear();
    BBS_AUC_MONEYS.clear();
    PWCSKILLS.clear();
    PWCOLOURS.clear();
    PREMIUM_DAY_PRICES.clear();
    PREMIUM_PROTECTED_ITEMS.clear();
    L2TOP_ONLINE_REWARDS.clear();
    try {
      Properties serviseSet = new Properties();
      InputStream is = new FileInputStream(new File("./config/services.cfg"));
      serviseSet.load(is);
      is.close();

      STOCK_SERTIFY = Integer.parseInt(serviseSet.getProperty("Sertify", "3435"));
      SERTIFY_PRICE = Integer.parseInt(serviseSet.getProperty("SertifyPrice", "10"));
      FIRST_BALANCE = Integer.parseInt(serviseSet.getProperty("StartBalance", "0"));
      DONATE_COIN = Integer.parseInt(serviseSet.getProperty("StockCoin", "5962"));
      DONATE_COIN_NEMA = serviseSet.getProperty("StockCoinName", "Gold Golem");
      DONATE_RATE = Integer.parseInt(serviseSet.getProperty("CoinConvert", "10"));
      NALOG_NPS = Integer.parseInt(serviseSet.getProperty("StockTax", "10"));
      VAL_NAME = serviseSet.getProperty("CoinConvertName", "P.");
      PAGE_LIMIT = Integer.parseInt(serviseSet.getProperty("PageLimit", "10"));
      AUGMENT_COIN = Integer.parseInt(serviseSet.getProperty("AugmentCoin", "4355"));
      ENCHANT_COIN = Integer.parseInt(serviseSet.getProperty("EnchantCoin", "4356"));
      AUGMENT_COIN_NAME = serviseSet.getProperty("AugmentCoinName", "Blue Eva");
      ENCHANT_COIN_NAME = serviseSet.getProperty("EnchantCoinName", "Gold Einhasad");
      AUGMENT_PRICE = Integer.parseInt(serviseSet.getProperty("AugmentPrice", "5"));
      ENCHANT_PRICE = Integer.parseInt(serviseSet.getProperty("EnchantPrice", "3"));
      AUGSALE_COIN = Integer.parseInt(serviseSet.getProperty("AugsaleCoin", "5962"));
      AUGSALE_PRICE = Integer.parseInt(serviseSet.getProperty("AugsalePrice", "20"));
      AUGSALE_COIN_NAME = serviseSet.getProperty("AugsaleCoinName", "Gold Golem");

      String[] propertySplit = serviseSet.getProperty("Augsales", "3250,10").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          AUGSALE_TABLE.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
        }
        catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("services.cfg: magicbuff error: " + aug[0]);
          }
        }
      }
      propertySplit = null;

      SOB_ID = Integer.parseInt(serviseSet.getProperty("SobSkill", "0"));
      SOB_NPC = Integer.parseInt(serviseSet.getProperty("SobNpc", "0"));
      SOB_COIN = Integer.parseInt(serviseSet.getProperty("SobCoin", "5962"));
      SOB_PRICE_ONE = Integer.parseInt(serviseSet.getProperty("SobPriceOne", "5"));
      SOB_PRICE_TWO = Integer.parseInt(serviseSet.getProperty("SobPriceTwo", "10"));
      SOB_COIN_NAME = serviseSet.getProperty("SobCoinName", "Gold Golem");
      PROTECT_OLY_SOB = Boolean.parseBoolean(serviseSet.getProperty("ProtectOlySoB", "False"));

      BUFFER_ID = Integer.parseInt(serviseSet.getProperty("Buffer", "40001"));
      propertySplit = serviseSet.getProperty("Magical", "1204,2").split(";");
      for (String buffs : propertySplit) {
        String[] pbuff = buffs.split(",");
        try {
          M_BUFF.put(Integer.valueOf(pbuff[0]), Integer.valueOf(pbuff[1]));
        } catch (NumberFormatException nfe) {
          if (!pbuff[0].equals("")) {
            System.out.println("services.cfg: magicbuff error: " + pbuff[0]);
          }
        }
      }
      propertySplit = null;

      propertySplit = serviseSet.getProperty("Fighter", "1204,2").split(";");
      for (String buffs : propertySplit) {
        String[] pbuff = buffs.split(",");
        try {
          F_BUFF.put(Integer.valueOf(pbuff[0]), Integer.valueOf(pbuff[1]));
        } catch (NumberFormatException nfe) {
          if (!pbuff[0].equals("")) {
            System.out.println("services.cfg: fightbuff error: " + pbuff[0]);
          }
        }
      }

      propertySplit = serviseSet.getProperty("ForbiddenProfileBuffs", "4,72,76,77,78,82,83,86,91,94,99,109,110,111,112,121,123,130,131,139,176,222,282,287,292,297,298,313,317,334,350,351,355,356,357,359,360,396,406,410,411,413,414,415,416,417,420,421,423,424,425,438,439,442,443,445,446,447,1001,1374,1410,1418,1427,3158,3142,3132,3133,3134,3135,3136,3199,3200,3201,3202,3203,3633,5104,5105").split(",");
      for (String buff : propertySplit) {
        try {
          F_PROFILE_BUFFS.add(Integer.valueOf(buff));
        } catch (NumberFormatException nfe) {
          if (!buff.equals("")) {
            System.out.println("services.cfg: ForbiddenProfileBuffs error: " + buff);
          }
        }
      }

      propertySplit = serviseSet.getProperty("AdditionBuffs", "8888,7777").split(",");
      for (String buff : propertySplit) {
        try {
          C_ALLOWED_BUFFS.add(Integer.valueOf(buff));
        } catch (NumberFormatException nfe) {
          if (!buff.equals("")) {
            System.out.println("services.cfg: AdditionBuffs error: " + buff);
          }
        }
      }

      POST_CHARBRIEF = Boolean.valueOf(serviseSet.getProperty("NewbeiBrief", "False")).booleanValue();
      POST_BRIEFAUTHOR = serviseSet.getProperty("BriefAuthor", ":0");
      POST_BRIEFTHEME = serviseSet.getProperty("BriefTheme", ":)");
      POST_BRIEFTEXT = serviseSet.getProperty("BriefText", ":)");
      POST_NPCNAME = serviseSet.getProperty("BriefNpc", "Ahosey");
      propertySplit = serviseSet.getProperty("BriefItem", "0,0").split(",");
      POST_BRIEF_ITEM = new EventReward(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), 0);

      ALLOW_CLASS_MASTERS = Boolean.valueOf(serviseSet.getProperty("AllowClassMasters", "False")).booleanValue();
      propertySplit = serviseSet.getProperty("ClassMasterPrices", "1,57,50000;2,57,500000;3,57,5000000").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          CLASS_MASTERS_PRICES.put(Integer.valueOf(Integer.parseInt(aug[0])), new EventReward(Integer.parseInt(aug[1]), Integer.parseInt(aug[2]), 0));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("services.cfg: ClassMasterPrices error: " + aug[0]);
          }
        }
      }
      REWARD_SHADOW = Boolean.valueOf(serviseSet.getProperty("AllowShadowReward", "False")).booleanValue();
      MASTER_NPCNAME = serviseSet.getProperty("MasterNpc", "PvP Server");
      ALLOW_CLAN_LEVEL = Boolean.valueOf(serviseSet.getProperty("AllowClanLevel", "False")).booleanValue();
      MCLAN_COIN = Integer.parseInt(serviseSet.getProperty("MClanCoin", "5962"));
      MCLAN_COIN_NAME = serviseSet.getProperty("MClanCoinName", "Gold Golem");
      CLAN_LVL6 = Integer.parseInt(serviseSet.getProperty("Level6", "10"));
      CLAN_LVL7 = Integer.parseInt(serviseSet.getProperty("Level7", "20"));
      CLAN_LVL8 = Integer.parseInt(serviseSet.getProperty("Level8", "30"));

      CLAN_COIN = Integer.parseInt(serviseSet.getProperty("ClanCoin", "5962"));
      CLAN_COIN_NAME = serviseSet.getProperty("ClanCoinName", "Gold Golem");
      CLAN_POINTS = Integer.parseInt(serviseSet.getProperty("ClanPoints", "1000"));
      CLAN_POINTS_PRICE = Integer.parseInt(serviseSet.getProperty("ClanPointsPrice", "5"));
      CLAN_SKILLS_PRICE = Integer.parseInt(serviseSet.getProperty("ClanSkillsPrice", "5"));

      ALLOW_DSHOP = Boolean.valueOf(serviseSet.getProperty("AllowDonateShop", "False")).booleanValue();
      ALLOW_DSKILLS = Boolean.valueOf(serviseSet.getProperty("AllowDonateSkillsShop", "False")).booleanValue();
      ALLOW_CSHOP = Boolean.valueOf(serviseSet.getProperty("AllowChinaShop", "False")).booleanValue();

      PWHERO_COIN = Integer.parseInt(serviseSet.getProperty("BBSHeroCoin", "5962"));
      PWHERO_PRICE = Integer.parseInt(serviseSet.getProperty("BBSHeroCoinDayPrice", "999"));
      PWHERO_FPRICE = Integer.parseInt(serviseSet.getProperty("BBSHeroCoinForeverPrice", "999"));
      PWHERO_MINDAYS = Integer.parseInt(serviseSet.getProperty("BBSHeroMinDays", "1"));
      PWHERO_TRANPRICE = Integer.parseInt(serviseSet.getProperty("BBSHeroCoinTransferPrice", "999"));
      PWCSKILLS_COIN = Integer.parseInt(serviseSet.getProperty("BBSCustomSkillCoin", "5962"));
      PWCSKILLS_PRICE = Integer.parseInt(serviseSet.getProperty("BBSCustomSkillPrice", "999"));
      PWENCHSKILL_COIN = Integer.parseInt(serviseSet.getProperty("BBSEnchantSkillCoin", "5962"));
      PWENCHSKILL_PRICE = Integer.parseInt(serviseSet.getProperty("BBSEnchantSkillPrice", "999"));
      PWCNGSKILLS_COIN = Integer.parseInt(serviseSet.getProperty("BBSTransferSkillCoin", "0"));
      PWCNGSKILLS_PRICE = Integer.parseInt(serviseSet.getProperty("BBSTransferSkillPrice", "999"));

      BBS_AUC_ITEM_COIN = Integer.parseInt(serviseSet.getProperty("AucItemCoin", "4037"));
      BBS_AUC_ITEM_PRICE = Integer.parseInt(serviseSet.getProperty("AucItemPrice", "1"));
      BBS_AUC_ITEM_NAME = serviseSet.getProperty("AucItemName", "Coin Of Luck");
      BBS_AUC_AUG_COIN = Integer.parseInt(serviseSet.getProperty("AucAugCoin", "4037"));
      BBS_AUC_AUG_PRICE = Integer.parseInt(serviseSet.getProperty("AucAugPrice", "1"));
      BBS_AUC_AUG_NAME = serviseSet.getProperty("AucAugName", "Coin Of Luck");
      BBS_AUC_SKILL_COIN = Integer.parseInt(serviseSet.getProperty("AucSkillCoin", "4037"));
      BBS_AUC_SKILL_PRICE = Integer.parseInt(serviseSet.getProperty("AucSkillPrice", "1"));
      BBS_AUC_SKILL_NAME = serviseSet.getProperty("AucSkillName", "Coin Of Luck");
      BBS_AUC_HERO_COIN = Integer.parseInt(serviseSet.getProperty("AucHeroCoin", "4037"));
      BBS_AUC_HERO_PRICE = Integer.parseInt(serviseSet.getProperty("AucHeroPrice", "1"));
      BBS_AUC_HERO_NAME = serviseSet.getProperty("AucHeroName", "Coin Of Luck");
      BBS_AUC_EXPIRE_DAYS = Integer.parseInt(serviseSet.getProperty("AucItemsExpireDays", "7"));

      propertySplit = serviseSet.getProperty("AucMoney", "57,Adena;4037,Coin Of Luck").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          BBS_AUC_MONEYS.put(Integer.valueOf(Integer.parseInt(aug[0])), aug[1]);
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("services.cfg: AucMoney error: " + aug[0]);
          }
        }
      }

      propertySplit = serviseSet.getProperty("BBSCustomSkills", "9999,9").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          PWCSKILLS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("services.cfg: BBSCustomSkills error: " + aug[0]);
          }
        }
      }

      PWNCOLOR_COIN = Integer.parseInt(serviseSet.getProperty("BBSColorNameCoin", "5962"));
      PWNCOLOR_PRICE = Integer.parseInt(serviseSet.getProperty("BBSColorNamePrice", "999"));
      PWNCOLOR_COINNAME = serviseSet.getProperty("BBSColorNameCoinName", "Gold Golem");
      PWTCOLOR_COIN = Integer.parseInt(serviseSet.getProperty("BBSColorTitleCoin", "5962"));
      PWTCOLOR_PRICE = Integer.parseInt(serviseSet.getProperty("BBSColorTitlePrice", "999"));
      PWTCOLOR_COINNAME = serviseSet.getProperty("BBSColorTitleCoinName", "Gold Golem");
      PWTCOLOR_PAYMENT = Boolean.valueOf(serviseSet.getProperty("BBSColorNextChangeFree", "True")).booleanValue();

      BBS_CNAME_COIN = Integer.parseInt(serviseSet.getProperty("BBSChangeNameCoin", "5962"));
      BBS_CNAME_PRICE = Integer.parseInt(serviseSet.getProperty("BBSChangeNamePrice", "999"));
      BBS_CNAME_VAL = serviseSet.getProperty("BBSChangeNameCoinName", "Gold Golem");

      PWCNGCLASS_COIN = Integer.parseInt(serviseSet.getProperty("BBSChangeClassCoin", "5962"));
      PWCNGCLASS_PRICE = Integer.parseInt(serviseSet.getProperty("BBSChangeClassPrice", "999"));

      int count = 0;
      propertySplit = serviseSet.getProperty("BBSPaintColors", "00FF00,00FF00").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          PWCOLOURS.put(Integer.valueOf(count), new AltBColor(Integer.decode("0x" + aug[0]).intValue(), aug[1]));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("services.cfg: BBSPaintColors error: " + aug[0]);
          }
        }
        count++;
      }
      PWHERO_COINNAME = serviseSet.getProperty("BBSHeroCoinName", "Gold Golem");
      PWCSKILLS_COINNAME = serviseSet.getProperty("BBSCustomSkillCoinName", "Gold Golem");
      PWENCHSKILL_COINNAME = serviseSet.getProperty("BBSEnchantSkillCoinName", "Gold Golem");

      PREMIUM_ENABLE = Boolean.parseBoolean(serviseSet.getProperty("PremiumEnable", "False"));
      PREMIUM_COIN = Integer.parseInt(serviseSet.getProperty("PremiumCoin", "5962"));
      PREMIUM_PRICE = Integer.parseInt(serviseSet.getProperty("PremiumPrice", "5962"));
      PREMIUM_COINNAME = serviseSet.getProperty("PremiumCoinName", "Gold Golem");
      propertySplit = serviseSet.getProperty("PremiumDayPrice", "99,2220").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          PREMIUM_DAY_PRICES.put(Integer.valueOf(Integer.parseInt(aug[0])), Integer.valueOf(Integer.parseInt(aug[1])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("services.cfg: PremiumDayPrice error: " + aug[0]);
          }
        }
      }

      NOBLES_ENABLE = Boolean.parseBoolean(serviseSet.getProperty("NobleEnable", "False"));
      SNOBLE_COIN = Integer.parseInt(serviseSet.getProperty("NobleCoin", "5962"));
      SNOBLE_PRICE = Integer.parseInt(serviseSet.getProperty("NoblePrice", "5962"));
      SNOBLE_COIN_NAME = serviseSet.getProperty("NobleCoinName", "Gold Golem");

      PREMIUM_EXP = Double.parseDouble(serviseSet.getProperty("PremiumExp", "1.3"));
      PREMIUM_SP = Double.parseDouble(serviseSet.getProperty("PremiumSp", "1.3"));
      PREMIUM_ITEMDROP = Double.parseDouble(serviseSet.getProperty("PremiumDropItem", "1.3"));
      PREMIUM_ITEMDROPMUL = Double.parseDouble(serviseSet.getProperty("PremiumDropMul", "1.3"));
      PREMIUM_SPOILRATE = Double.parseDouble(serviseSet.getProperty("PremiumDropSpoil", "1.3"));

      PREMIUM_ADENAMUL = Double.parseDouble(serviseSet.getProperty("PremiumAdenaMul", "1.3"));
      PREMIUM_PCCAFE_MUL = Double.parseDouble(serviseSet.getProperty("PremiumPcCafeMul", "1.3"));
      PREMIUM_AQURE_SKILL_MUL = Double.parseDouble(serviseSet.getProperty("PremiumClanSkillsMul", "0.7"));
      PREMIUM_AUGMENT_RATE = Integer.parseInt(serviseSet.getProperty("PremiumAugmentRate", "0"));
      PREMIUM_ENCH_ITEM = Integer.parseInt(serviseSet.getProperty("PremiumEnchRate", "0"));
      PREMIUM_ENCH_SKILL = Integer.parseInt(serviseSet.getProperty("PremiumEnchSkillRate", "0"));
      PREMIUM_CURSED_RATE = Integer.parseInt(serviseSet.getProperty("PremiumCursedRate", "0")) * 1000;

      PREMIUM_ANY_SUBCLASS = Boolean.parseBoolean(serviseSet.getProperty("PremiumAnySubclass", "False"));
      PREMIUM_CHKSKILLS = Boolean.parseBoolean(serviseSet.getProperty("PremiumCheckSkills", "True"));
      PREMIUM_PKDROP_OFF = Boolean.parseBoolean(serviseSet.getProperty("PremiumDisablePkDrop", "False"));

      PREMIUM_ANOOUNCE = Boolean.parseBoolean(serviseSet.getProperty("PremiumAnnounceEnter", "False"));
      PREMIUM_ANNOUNCE_PHRASE = serviseSet.getProperty("PremiumAnnouncePhrase", "\u0418\u0433\u0440\u043E\u043A %player% \u0432\u043E\u0448\u0435\u043B \u0432 \u0438\u0433\u0440\u0443.");

      PREMIUM_ENCHANT_FAIL = Boolean.parseBoolean(serviseSet.getProperty("PremiumAltEnchantFail", "False"));

      PREMIUM_NAME_PREFIX = serviseSet.getProperty("PremiumNamePrefix", "");

      PREMIUM_START_DAYS = Integer.parseInt(serviseSet.getProperty("PremiumNewCharDays", "0"));

      propertySplit = serviseSet.getProperty("ProtectedPremiumItems", "").split(",");
      for (String item : propertySplit) {
        try {
          PREMIUM_PROTECTED_ITEMS.add(Integer.valueOf(item));
        } catch (NumberFormatException nfe) {
          if (!item.equals("")) {
            System.out.println("services.cfg: PremiumDayPrice error: " + item);
          }
        }
      }

      BBS_ONLY_PEACE = Boolean.parseBoolean(serviseSet.getProperty("BbsPeace", "True"));

      L2JMOD_ALLOW_WEDDING = Boolean.valueOf(serviseSet.getProperty("AllowWedding", "False")).booleanValue();
      L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(serviseSet.getProperty("WeddingPunishInfidelity", "True"));
      L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(serviseSet.getProperty("WeddingTeleport", "True"));
      L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(serviseSet.getProperty("WeddingTeleportPrice", "50000"));
      L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(serviseSet.getProperty("WeddingTeleportDuration", "60"));
      L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(serviseSet.getProperty("WeddingAllowSameSex", "False"));
      L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(serviseSet.getProperty("WeddingFormalWear", "True"));

      L2JMOD_WEDDING_COIN = Integer.parseInt(serviseSet.getProperty("WeddingCoin", "4037"));
      L2JMOD_WEDDING_PRICE = Integer.parseInt(serviseSet.getProperty("WeddingPrice", "5"));
      L2JMOD_WEDDING_COINNAME = serviseSet.getProperty("WeddingCoinName", "Coin Of Luck");

      L2JMOD_WEDDING_DIVORCE_COIN = Integer.parseInt(serviseSet.getProperty("WeddingDivorceCoin", "4037"));
      L2JMOD_WEDDING_DIVORCE_PRICE = Integer.parseInt(serviseSet.getProperty("WeddingDivorcePrice", "5"));
      L2JMOD_WEDDING_DIVORCE_COINNAME = serviseSet.getProperty("WeddingDivorceCoinName", "Coin Of Luck");

      L2JMOD_WEDDING_INTERVAL = (int)TimeUnit.SECONDS.toMillis(Integer.parseInt(serviseSet.getProperty("WeddingInterval", "90")));

      propertySplit = serviseSet.getProperty("WeddingColors", "FFFFFF,FFFFFF").split(",");
      WEDDING_COLORS = new PvpColor(Integer.decode("0x" + new TextBuilder(propertySplit[0]).reverse().toString()).intValue(), Integer.decode("0x" + new TextBuilder(propertySplit[1]).reverse().toString()).intValue());

      L2JMOD_WEDDING_BOW = Boolean.valueOf(serviseSet.getProperty("WeddingCupidBow", "False")).booleanValue();

      L2TOP_ENABLE = Boolean.parseBoolean(serviseSet.getProperty("L2TopEnable", "False"));
      L2TOP_SERV_ID = Integer.parseInt(serviseSet.getProperty("L2TopServerId", "0"));
      L2TOP_SERV_KEY = serviseSet.getProperty("L2TopServerKey", "0");
      L2TOP_UPDATE_DELAY = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serviseSet.getProperty("L2TopUpdateDelay", "5")));

      L2TOP_OFFLINE_ITEM = Integer.parseInt(serviseSet.getProperty("L2TopOfflineId", "0"));
      L2TOP_OFFLINE_COUNT = Integer.parseInt(serviseSet.getProperty("L2TopOfflineCount", "0"));
      L2TOP_OFFLINE_LOC = serviseSet.getProperty("L2TopOfflineLoc", "INVENTORY");
      propertySplit = serviseSet.getProperty("L2TopOnlineRewards", "57,13,100;57,13,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          L2TOP_ONLINE_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("services.cfg: L2TopOnlineRewards error: " + aug[0]);
          }
        }
      }
      L2TOP_LOGTYPE = Integer.parseInt(serviseSet.getProperty("L2TopLog", "1"));

      MMOTOP_ENABLE = Boolean.parseBoolean(serviseSet.getProperty("MmotopEnable", "False"));
      MMOTOP_STAT_LINK = serviseSet.getProperty("MmotopStatLink", "0");
      MMOTOP_UPDATE_DELAY = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serviseSet.getProperty("MmotopUpdateDelay", "5")));

      MMOTOP_OFFLINE_ITEM = Integer.parseInt(serviseSet.getProperty("MmotopOfflineId", "0"));
      MMOTOP_OFFLINE_COUNT = Integer.parseInt(serviseSet.getProperty("MmotopOfflineCount", "0"));
      MMOTOP_OFFLINE_LOC = serviseSet.getProperty("MmotopOfflineLoc", "INVENTORY");
      propertySplit = serviseSet.getProperty("MmotopOnlineRewards", "57,13,100;57,13,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          MMOTOP_ONLINE_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("services.cfg: MmotopOnlineRewards error: " + aug[0]);
          }
        }
      }
      MMOTOP_LOGTYPE = Integer.parseInt(serviseSet.getProperty("MmotopLog", "1"));

      EXPOSTB_COIN = Integer.parseInt(serviseSet.getProperty("EpBriefCoin", "4037"));
      EXPOSTB_PRICE = Integer.parseInt(serviseSet.getProperty("EpBriefPrice", "1"));
      EXPOSTB_NAME = serviseSet.getProperty("EpBriefCoinName", "Coin Of Luck");
      EXPOSTA_COIN = Integer.parseInt(serviseSet.getProperty("EpItemCoin", "4037"));
      EXPOSTA_PRICE = Integer.parseInt(serviseSet.getProperty("EpItemPrice", "5"));
      EXPOSTA_NAME = serviseSet.getProperty("EpItemCoinName", "Coin Of Luck");

      PC_CAFE_ENABLED = Boolean.parseBoolean(serviseSet.getProperty("PcCafeEnable", "False"));
      PC_CAFE_INTERVAL = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serviseSet.getProperty("PcCafeUpdateDelay", "60")));

      PC_CAFE_DOUBLE_CHANCE = Integer.parseInt(serviseSet.getProperty("PcCafeUpdateDoubleChance", "60"));

      propertySplit = serviseSet.getProperty("PcCafeUpdateBonus", "30,60").split(",");
      PC_CAFE_BONUS = new PvpColor(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]));

      CACHED_SERVER_STAT = Boolean.parseBoolean(serviseSet.getProperty("ServerStat", "False"));

      QUED_ITEMS_ENABLE = Boolean.parseBoolean(serviseSet.getProperty("QuedItems", "False"));
      QUED_ITEMS_INTERVAL = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(serviseSet.getProperty("QuedItemsInterval", "5")));
      QUED_ITEMS_LOGTYPE = Integer.parseInt(serviseSet.getProperty("QuedItemsLog", "0"));

      propertySplit = null;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/services.cfg File.");
    }
  }

  public static void loadEventsCfg() {
    TVT_EVENT_REWARDS.clear();
    TVT_EVENT_DOOR_IDS.clear();
    TVT_WHITE_POTINS.clear();
    OS_REWARDS.clear();
    ELH_REWARDS.clear();
    FC_ALLOWITEMS.clear();
    XM_DROP.clear();
    MEDAL_EVENT_DROP.clear();
    EBC_REWARDS.clear();
    EENC_REWARDS.clear();
    EENC_POINTS.clear();
    ANARCHY_TOWNS.clear();
    try {
      Properties eventsSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/events.cfg"));
      eventsSettings.load(is);
      is.close();

      MASS_PVP = Boolean.valueOf(eventsSettings.getProperty("AllowMassPvP", "False")).booleanValue();
      MPVP_RTIME = Long.parseLong(eventsSettings.getProperty("AfterServetStart", "60"));
      MPVP_REG = Long.parseLong(eventsSettings.getProperty("Registration", "15"));
      MPVP_ANC = Long.parseLong(eventsSettings.getProperty("AnnouncePeriod", "2"));
      MPVP_TP = Long.parseLong(eventsSettings.getProperty("Teleport", "5"));
      MPVP_PR = Long.parseLong(eventsSettings.getProperty("Buff", "60"));

      MPVP_MAX = Long.parseLong(eventsSettings.getProperty("Battle", "60"));
      MPVP_NEXT = Long.parseLong(eventsSettings.getProperty("Next", "24"));
      MPVP_MAXC = Integer.parseInt(eventsSettings.getProperty("Rounds", "5"));
      MPVP_NPC = Integer.parseInt(eventsSettings.getProperty("RegNpc", "5"));
      MPVP_CREW = Integer.parseInt(eventsSettings.getProperty("RoundReward", "4355"));
      MPVP_CREWC = Integer.parseInt(eventsSettings.getProperty("RoundCount", "3"));
      MPVP_EREW = Integer.parseInt(eventsSettings.getProperty("FinalReward", "4355"));
      MPVP_EREWC = Integer.parseInt(eventsSettings.getProperty("FinalCount", "30"));
      MPVP_LVL = Integer.parseInt(eventsSettings.getProperty("MinLelev", "76"));
      MPVP_MAXP = Integer.parseInt(eventsSettings.getProperty("MaxPlayers", "60"));
      MPVP_NOBL = Boolean.valueOf(eventsSettings.getProperty("OnlyNobless", "True")).booleanValue();
      TVT_NOBL = Boolean.valueOf(eventsSettings.getProperty("TvTOnlyNobless", "True")).booleanValue();

      String[] propertySplit = eventsSettings.getProperty("Npc", "116530,76141,-2730").split(",");
      MPVP_NPCLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      propertySplit = eventsSettings.getProperty("Back", "116530,76141,-2730").split(",");
      MPVP_TPLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      propertySplit = eventsSettings.getProperty("Cycle", "-92939,-251113,-3331").split(",");
      MPVP_CLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      propertySplit = eventsSettings.getProperty("Final", "-92939,-251113,-3331").split(",");
      MPVP_WLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      TVT_EVENT_ENABLED = Boolean.parseBoolean(eventsSettings.getProperty("TvTEventEnabled", "false"));
      TVT_EVENT_INTERVAL = Integer.parseInt(eventsSettings.getProperty("TvTEventInterval", "18000"));
      TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(eventsSettings.getProperty("TvTEventParticipationTime", "3600"));
      TVT_EVENT_RUNNING_TIME = Integer.parseInt(eventsSettings.getProperty("TvTEventRunningTime", "1800"));
      TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(eventsSettings.getProperty("TvTEventParticipationNpcId", "0"));

      if (TVT_EVENT_PARTICIPATION_NPC_ID == 0) {
        TVT_EVENT_ENABLED = false;
        System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcId");
      } else {
        propertySplit = eventsSettings.getProperty("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");

        if (propertySplit.length < 3) {
          TVT_EVENT_ENABLED = false;
          System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
        } else {
          propertySplit = eventsSettings.getProperty("TvTEventParticipationNpcCoordinates", "83425,148585,-3406;83465,148485,-3406").split(";");
          for (String augs : propertySplit) {
            String[] aug = augs.split(",");
            try {
              TVT_EVENT_PARTICIPATION_NPC_COORDINATES.add(new Location(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
            } catch (NumberFormatException nfe) {
              if (!aug[0].equals("")) {
                System.out.println("events.cfg: TvTEventParticipationNpcCoordinates error: " + aug[0]);
              }
            }
          }

          TVT_EVENT_MIN_PLAYERS_IN_TEAMS = Integer.parseInt(eventsSettings.getProperty("TvTEventMinPlayersInTeams", "1"));
          TVT_EVENT_MAX_PLAYERS_IN_TEAMS = Integer.parseInt(eventsSettings.getProperty("TvTEventMaxPlayersInTeams", "20"));
          TVT_EVENT_MIN_LVL = (byte)Integer.parseInt(eventsSettings.getProperty("TvTEventMinPlayerLevel", "1"));
          TVT_EVENT_MAX_LVL = (byte)Integer.parseInt(eventsSettings.getProperty("TvTEventMaxPlayerLevel", "80"));
          TVT_EVENT_RESPAWN_TELEPORT_DELAY = Integer.parseInt(eventsSettings.getProperty("TvTEventRespawnTeleportDelay", "20"));
          TVT_EVENT_START_LEAVE_TELEPORT_DELAY = Integer.parseInt(eventsSettings.getProperty("TvTEventStartLeaveTeleportDelay", "20"));

          TVT_EVENT_TEAM_1_NAME = eventsSettings.getProperty("TvTEventTeam1Name", "Team1");
          propertySplit = eventsSettings.getProperty("TvTEventTeam1Coordinates", "0,0,0").split(",");

          if (propertySplit.length < 3) {
            TVT_EVENT_ENABLED = false;
            System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam1Coordinates");
          } else {
            TVT_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
            TVT_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
            TVT_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(propertySplit[2]);

            TVT_EVENT_TEAM_2_NAME = eventsSettings.getProperty("TvTEventTeam2Name", "Team2");
            propertySplit = eventsSettings.getProperty("TvTEventTeam2Coordinates", "0,0,0").split(",");

            if (propertySplit.length < 3) {
              TVT_EVENT_ENABLED = false;
              System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam2Coordinates");
            } else {
              TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
              TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
              TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
              propertySplit = eventsSettings.getProperty("TvTEventReward", "57,100000").split(";");

              for (String reward : propertySplit) {
                String[] rewardSplit = reward.split(",");

                if (rewardSplit.length != 2)
                  System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"" + reward + "\"");
                else {
                  try {
                    TVT_EVENT_REWARDS.add(new int[] { Integer.valueOf(rewardSplit[0]).intValue(), Integer.valueOf(rewardSplit[1]).intValue() });
                  } catch (NumberFormatException nfe) {
                    if (!reward.equals("")) {
                      System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"" + reward + "\"");
                    }
                  }
                }
              }

              TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = Boolean.parseBoolean(eventsSettings.getProperty("TvTEventTargetTeamMembersAllowed", "true"));
              TVT_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(eventsSettings.getProperty("TvTEventPotionsAllowed", "false"));
              TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(eventsSettings.getProperty("TvTEventSummonByItemAllowed", "false"));
              propertySplit = eventsSettings.getProperty("TvTEventDoorsCloseOpenOnStartEnd", "").split(";");
              for (String door : propertySplit) {
                try {
                  TVT_EVENT_DOOR_IDS.add(Integer.valueOf(door));
                } catch (NumberFormatException nfe) {
                  if (!door.equals("")) {
                    System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventDoorsCloseOpenOnStartEnd \"" + door + "\"");
                  }
                }
              }

              propertySplit = eventsSettings.getProperty("TvTEventAllowedPotionsList", "").split(",");
              for (String potion : propertySplit) {
                try {
                  TVT_WHITE_POTINS.add(Integer.valueOf(potion));
                } catch (NumberFormatException nfe) {
                  if (!potion.equals("")) {
                    System.out.println("TvTEventEngine[Config.load()]: invalid config property -> TvTEventAllowedPotionsList \"" + potion + "\"");
                  }
                }
              }
            }
          }
        }
      }
      TVT_NO_PASSIVE = Boolean.parseBoolean(eventsSettings.getProperty("TvTNoPassive", "False"));

      ALLOW_SCH = Boolean.valueOf(eventsSettings.getProperty("AllowSchuttgart", "False")).booleanValue();
      SCH_NEXT = Integer.parseInt(eventsSettings.getProperty("SchNext", "24"));
      SCH_RESTART = Integer.parseInt(eventsSettings.getProperty("SchRestart", "60"));

      SCH_TIME1 = Integer.parseInt(eventsSettings.getProperty("SchWave1", "120000"));
      SCH_TIME2 = Integer.parseInt(eventsSettings.getProperty("SchWave2", "30000"));
      SCH_TIME3 = Integer.parseInt(eventsSettings.getProperty("SchWave3", "15000"));
      SCH_TIME4 = Integer.parseInt(eventsSettings.getProperty("SchWave4", "15000"));
      SCH_TIME5 = Integer.parseInt(eventsSettings.getProperty("SchWave5", "15000"));
      SCH_TIME6 = Integer.parseInt(eventsSettings.getProperty("SchWave6", "15000"));
      SCH_TIMEBOSS = Integer.parseInt(eventsSettings.getProperty("SchWaveBoss", "400000"));

      SCH_MOB1 = Integer.parseInt(eventsSettings.getProperty("SchMob1", "80100"));
      SCH_MOB2 = Integer.parseInt(eventsSettings.getProperty("SchMob2", "80101"));
      SCH_MOB3 = Integer.parseInt(eventsSettings.getProperty("SchMob3", "80101"));
      SCH_MOB4 = Integer.parseInt(eventsSettings.getProperty("SchMob4", "80100"));
      SCH_MOB5 = Integer.parseInt(eventsSettings.getProperty("SchMob5", "80102"));
      SCH_MOB6 = Integer.parseInt(eventsSettings.getProperty("SchMob6", "80103"));
      SCH_BOSS = Integer.parseInt(eventsSettings.getProperty("SchBoss", "80104"));

      SCH_ALLOW_SHOP = Boolean.valueOf(eventsSettings.getProperty("SchAllowShop", "False")).booleanValue();
      SCH_SHOP = Integer.parseInt(eventsSettings.getProperty("SchShopId", "80105"));
      SCH_SHOPTIME = Integer.parseInt(eventsSettings.getProperty("SchShopTimeout", "20"));

      OPEN_SEASON = Boolean.valueOf(eventsSettings.getProperty("AllowOpenSeason", "False")).booleanValue();
      OS_NEXT = Integer.parseInt(eventsSettings.getProperty("OsNext", "600"));
      OS_RESTART = Integer.parseInt(eventsSettings.getProperty("OsAfterRestart", "60"));
      OS_BATTLE = Integer.parseInt(eventsSettings.getProperty("OsBattlePeriod", "2"));
      OS_FAIL_NEXT = Integer.parseInt(eventsSettings.getProperty("OsFailNext", "120"));
      OS_REGTIME = Integer.parseInt(eventsSettings.getProperty("OsRegTime", "300"));
      OS_ANNDELAY = Integer.parseInt(eventsSettings.getProperty("OsAnnounceDelay", "15"));
      OS_MINLVL = Integer.parseInt(eventsSettings.getProperty("OsMinLevel", "76"));
      OS_MINPLAYERS = Integer.parseInt(eventsSettings.getProperty("OsMinPlayers", "10"));

      propertySplit = eventsSettings.getProperty("OsRewards", "4037,500,100;4355,15,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          OS_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("events.cfg: OsRewards error: " + aug[0]);
          }
        }
      }

      ELH_ENABLE = Boolean.parseBoolean(eventsSettings.getProperty("LastHeroEnable", "False"));
      ELH_ARTIME = Long.parseLong(eventsSettings.getProperty("LhAfterRestart", "30"));
      ELH_REGTIME = Long.parseLong(eventsSettings.getProperty("LhRegPeriod", "30"));
      ELH_ANNDELAY = Long.parseLong(eventsSettings.getProperty("LhAnounceDelay", "30"));
      ELH_TPDELAY = Long.parseLong(eventsSettings.getProperty("LhTeleportDelay", "30"));
      ELH_NEXT = Long.parseLong(eventsSettings.getProperty("LhNextStart", "30"));

      ELH_MINLVL = Integer.parseInt(eventsSettings.getProperty("LhPlayerMinLvl", "76"));
      ELH_MINP = Integer.parseInt(eventsSettings.getProperty("LhMinPlayers", "76"));
      ELH_MAXP = Integer.parseInt(eventsSettings.getProperty("LhMaxPlayers", "76"));

      ELH_NPCID = Integer.parseInt(eventsSettings.getProperty("LhRegNpcId", "55558"));

      propertySplit = eventsSettings.getProperty("LhRegNpcLoc", "83101,148396,-3407").split(",");
      ELH_NPCLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));
      ELH_NPCTOWN = eventsSettings.getProperty("LhRegNpcTown", "\u0413\u0438\u0440\u0430\u043D\u0435");

      propertySplit = eventsSettings.getProperty("LhBattleLoc", "83101,148396,-3407").split(",");
      ELH_TPLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      ELH_TICKETID = Integer.parseInt(eventsSettings.getProperty("LhTicketId", "0"));
      ELH_TICKETCOUNT = Integer.parseInt(eventsSettings.getProperty("LhTicketCount", "0"));

      propertySplit = eventsSettings.getProperty("LhRewards", "9901,500,100;57,1,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          ELH_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("events.cfg: LhRewards error: " + aug[0]);
          }
        }
      }
      ELH_HERO_DAYS = Integer.parseInt(eventsSettings.getProperty("LhRewardDays", "1"));

      ELH_HIDE_NAMES = Boolean.parseBoolean(eventsSettings.getProperty("LhHideNames", "False"));
      ELH_ALT_NAME = eventsSettings.getProperty("LhAltName", "");

      FC_INSERT_INVENTORY = Boolean.parseBoolean(eventsSettings.getProperty("FightClubInsertInventory", "False"));
      propertySplit = eventsSettings.getProperty("FightClubItems", "1234").split(",");
      for (String id : propertySplit) {
        FC_ALLOWITEMS.add(Integer.valueOf(Integer.parseInt(id)));
      }

      ALLOW_XM_SPAWN = Boolean.parseBoolean(eventsSettings.getProperty("AllowChristmassEvent", "False"));
      propertySplit = eventsSettings.getProperty("ChristmassDrop", "5556,3,10;5557,3,10;5558,3,10;5559,3,10").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          XM_DROP.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("events.cfg: ChristmassDrop error: " + aug[0]);
          }
        }
      }
      XM_TREE_LIFE = TimeUnit.MINUTES.toMillis(Integer.parseInt(eventsSettings.getProperty("ChristmassTreeLife", "0")));

      ALLOW_MEDAL_EVENT = Boolean.parseBoolean(eventsSettings.getProperty("AllowMedalsEvent", "False"));
      propertySplit = eventsSettings.getProperty("MedalsDrop", "6392,1,7;6393,1,2").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          MEDAL_EVENT_DROP.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("events.cfg: MedalsDrop error: " + aug[0]);
          }
        }

      }

      EBC_ENABLE = Boolean.parseBoolean(eventsSettings.getProperty("CaptureBaseEnable", "False"));
      EBC_ARTIME = Long.parseLong(eventsSettings.getProperty("CbAfterRestart", "30"));
      EBC_REGTIME = Long.parseLong(eventsSettings.getProperty("CbRegPeriod", "5"));
      EBC_ANNDELAY = Long.parseLong(eventsSettings.getProperty("CbAnounceDelay", "1"));
      EBC_TPDELAY = Long.parseLong(eventsSettings.getProperty("CbTeleportDelay", "1"));
      EBC_DEATHLAY = Long.parseLong(eventsSettings.getProperty("CbRespawnDelay", "5000"));
      EBC_NEXT = Long.parseLong(eventsSettings.getProperty("CbNextStart", "30"));

      EBC_MINLVL = Integer.parseInt(eventsSettings.getProperty("CbPlayerMinLvl", "76"));
      EBC_MINP = Integer.parseInt(eventsSettings.getProperty("CbMinPlayers", "5"));
      EBC_MAXP = Integer.parseInt(eventsSettings.getProperty("CbMaxPlayers", "100"));
      EBC_NPCID = Integer.parseInt(eventsSettings.getProperty("CbRegNpcId", "55558"));

      propertySplit = eventsSettings.getProperty("CbRegNpcLoc", "83101,148396,-3407").split(",");
      EBC_NPCLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));
      EBC_NPCTOWN = eventsSettings.getProperty("CbRegNpcTown", "\u0413\u0438\u0440\u0430\u043D\u0435");

      EBC_BASE1ID = Integer.parseInt(eventsSettings.getProperty("CbBase1Id", "80050"));
      propertySplit = eventsSettings.getProperty("CbBase1Loc", "175732,-87983,-5107").split(",");
      EBC_TPLOC1 = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      EBC_BASE2ID = Integer.parseInt(eventsSettings.getProperty("CbBase2Id", "80051"));
      propertySplit = eventsSettings.getProperty("CbBase2Loc", "172713,-87983,-5107").split(",");
      EBC_TPLOC2 = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      EBC_BASE1NAME = eventsSettings.getProperty("CbTeam1Name", "White");
      EBC_BASE2NAME = eventsSettings.getProperty("CbTeam2Name", "Black");

      EBC_TICKETID = Integer.parseInt(eventsSettings.getProperty("CbTicketId", "0"));
      EBC_TICKETCOUNT = Integer.parseInt(eventsSettings.getProperty("CbTicketCount", "0"));

      propertySplit = eventsSettings.getProperty("CbRewards", "9901,500,100;57,1,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          EBC_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("additions.cfg: CbRewards error: " + aug[0]);
          }
        }
      }

      EENC_ENABLE = Boolean.parseBoolean(eventsSettings.getProperty("EncounterEnable", "False"));
      EENC_ARTIME = Long.parseLong(eventsSettings.getProperty("EncAfterRestart", "30"));
      EENC_REGTIME = Long.parseLong(eventsSettings.getProperty("EncRegPeriod", "30"));
      EENC_ANNDELAY = Long.parseLong(eventsSettings.getProperty("EncAnounceDelay", "30"));
      EENC_TPDELAY = Long.parseLong(eventsSettings.getProperty("EncTeleportDelay", "30"));
      EENC_FINISH = Long.parseLong(eventsSettings.getProperty("EncFinishTask", "1"));
      EENC_NEXT = Long.parseLong(eventsSettings.getProperty("EncNextStart", "30"));

      EENC_MINLVL = Integer.parseInt(eventsSettings.getProperty("EncPlayerMinLvl", "76"));
      EENC_MINP = Integer.parseInt(eventsSettings.getProperty("EncMinPlayers", "76"));
      EENC_MAXP = Integer.parseInt(eventsSettings.getProperty("EncMaxPlayers", "76"));

      EENC_NPCID = Integer.parseInt(eventsSettings.getProperty("EncRegNpcId", "55558"));

      propertySplit = eventsSettings.getProperty("EncRegNpcLoc", "83101,148396,-3407").split(",");
      EENC_NPCLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));
      EENC_NPCTOWN = eventsSettings.getProperty("EncRegNpcTown", "\u0413\u0438\u0440\u0430\u043D\u0435");

      propertySplit = eventsSettings.getProperty("EncBattleLoc", "83101,148396,-3407").split(",");
      EENC_TPLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      EENC_TICKETID = Integer.parseInt(eventsSettings.getProperty("EncTicketId", "0"));
      EENC_TICKETCOUNT = Integer.parseInt(eventsSettings.getProperty("EncTicketCount", "0"));

      propertySplit = eventsSettings.getProperty("EncRewards", "9901,500,100;57,1,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          EENC_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("events.cfg: EncRewards error: " + aug[0]);
          }
        }
      }

      propertySplit = eventsSettings.getProperty("EncItemPoints", "3433#83101,148396,-3407:83201,148296,-3407:83301,148196,-3407;4355#83101,148396,-3407:83201,148296,-3407:83301,148196,-3407").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split("#");
        int itemId = Integer.parseInt(aug[0]);
        String[] locs = aug[1].split(":");
        FastList locfl = new FastList();
        for (String points : locs) {
          String[] loc = points.split(",");
          try {
            locfl.add(new Location(Integer.parseInt(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2])));
          } catch (NumberFormatException nfe) {
            if (!loc.equals("")) {
              System.out.println("events.cfg: EncItemPoints error: " + aug[0]);
            }
          }
        }
        EENC_POINTS.put(Integer.valueOf(itemId), locfl);
      }

      ANARCHY_ENABLE = Boolean.parseBoolean(eventsSettings.getProperty("AnarchyEnable", "False"));
      ANARCHY_DAY = Integer.parseInt(eventsSettings.getProperty("AnarchyDay", "3")) + 1;
      ANARCHY_HOUR = Integer.parseInt(eventsSettings.getProperty("AnarchyHour", "12"));
      ANARCHY_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(eventsSettings.getProperty("AnarchyDuration", "60")));
      String anarchy_towns = eventsSettings.getProperty("AnarchyProtectedTowns", "-1,-2");
      anarchy_towns = anarchy_towns + ",0";
      propertySplit = anarchy_towns.split(",");
      for (String id : propertySplit) {
        ANARCHY_TOWNS.add(Integer.valueOf(Integer.parseInt(id)));
      }

      FIGHTING_ENABLE = Boolean.parseBoolean(eventsSettings.getProperty("FightingEnable", "False"));
      FIGHTING_DAY = Integer.parseInt(eventsSettings.getProperty("FightingDay", "3")) + 1;
      FIGHTING_HOUR = Integer.parseInt(eventsSettings.getProperty("FightingHour", "3"));
      FIGHTING_REGTIME = Integer.parseInt(eventsSettings.getProperty("FightingRegTime", "3"));
      FIGHTING_ANNDELAY = Integer.parseInt(eventsSettings.getProperty("AnarchyAnounceDelay", "3"));
      FIGHTING_TPDELAY = Integer.parseInt(eventsSettings.getProperty("AnarchyBattleDelay", "3"));

      FIGHTING_MINLVL = Integer.parseInt(eventsSettings.getProperty("FightingMinLevel", "70"));
      FIGHTING_MINP = Integer.parseInt(eventsSettings.getProperty("FightingMinPlayers", "10"));
      FIGHTING_MAXP = Integer.parseInt(eventsSettings.getProperty("FightingMaxPlayers", "100"));

      FIGHTING_TICKETID = Integer.parseInt(eventsSettings.getProperty("FightingTicket", "4037"));
      FIGHTING_TICKETCOUNT = Integer.parseInt(eventsSettings.getProperty("FightingTicketCount", "10"));

      FIGHTING_NPCID = Integer.parseInt(eventsSettings.getProperty("FightingNpcId", "4037"));

      propertySplit = eventsSettings.getProperty("FightingNpcLoc", "83101,148396,-3407").split(",");
      FIGHTING_NPCLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      propertySplit = eventsSettings.getProperty("FightingBattleLoc", "83101,148396,-3407").split(",");
      FIGHTING_TPLOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      EVENTS_SAME_IP = Boolean.valueOf(eventsSettings.getProperty("AllowEventsSameIp", "True")).booleanValue();

      if (TVT_EVENT_ENABLED) {
        TVT_POLY = new EventTerritory(1);
        propertySplit = eventsSettings.getProperty("TvtBorder", "9901,500;57,1").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            TVT_POLY.addPoint(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]));
          } catch (NumberFormatException nfe) {
            if (!aug[0].equals("")) {
              System.out.println("events.cfg: TvtBorder error: " + aug[0]);
            }
          }
        }
        propertySplit = eventsSettings.getProperty("TvtBorderZ", "-1292,-3407").split(",");
        int z1 = Integer.parseInt(propertySplit[0]);
        int z2 = Integer.parseInt(propertySplit[1]);
        if (z1 == z2) {
          z1 -= 400;
          z2 += 400;
        }
        TVT_POLY.setZ(z1, z2);
      }
      if (ELH_ENABLE) {
        LASTHERO_POLY = new EventTerritory(2);
        propertySplit = eventsSettings.getProperty("LastHeroBorder", "9901,500;57,1").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            LASTHERO_POLY.addPoint(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]));
          } catch (NumberFormatException nfe) {
            if (!aug[0].equals("")) {
              System.out.println("events.cfg: LastHeroBorder error: " + aug[0]);
            }
          }
        }
        propertySplit = eventsSettings.getProperty("LastHeroBorderZ", "-1292,-3407").split(",");
        int z1 = Integer.parseInt(propertySplit[0]);
        int z2 = Integer.parseInt(propertySplit[1]);
        if (z1 == z2) {
          z1 -= 400;
          z2 += 400;
        }
        LASTHERO_POLY.setZ(z1, z2);
      }
      if (MASS_PVP) {
        MASSPVP_POLY = new EventTerritory(3);
        propertySplit = eventsSettings.getProperty("MassPvpBorder", "9901,500;57,1").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            MASSPVP_POLY.addPoint(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]));
          } catch (NumberFormatException nfe) {
            if (!aug[0].equals("")) {
              System.out.println("events.cfg: MassPvpBorder error: " + aug[0]);
            }
          }
        }
        propertySplit = eventsSettings.getProperty("MassPvpBorderZ", "-1292,-3407").split(",");
        int z1 = Integer.parseInt(propertySplit[0]);
        int z2 = Integer.parseInt(propertySplit[1]);
        if (z1 == z2) {
          z1 -= 400;
          z2 += 400;
        }
        MASSPVP_POLY.setZ(z1, z2);
      }
      if (EBC_ENABLE) {
        BASECAPTURE_POLY = new EventTerritory(4);
        propertySplit = eventsSettings.getProperty("BaseCaptureBorder", "9901,500;57,1").split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            BASECAPTURE_POLY.addPoint(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]));
          } catch (NumberFormatException nfe) {
            if (!aug[0].equals("")) {
              System.out.println("events.cfg: BaseCaptureBorder error: " + aug[0]);
            }
          }
        }
        propertySplit = eventsSettings.getProperty("BaseCaptureBorderZ", "-1292,-3407").split(",");
        int z1 = Integer.parseInt(propertySplit[0]);
        int z2 = Integer.parseInt(propertySplit[1]);
        if (z1 == z2) {
          z1 -= 400;
          z2 += 400;
        }
        BASECAPTURE_POLY.setZ(z1, z2);
      }

      EVENT_SPECIAL_DROP = Boolean.parseBoolean(eventsSettings.getProperty("SpecialEventDrop", "False"));

      propertySplit = null;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/events.cfg File.");
    }
  }

  public static void loadRatesCfg() {
    try {
      Properties ratesSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/rates.cfg"));
      ratesSettings.load(is);
      is.close();

      RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
      RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
      RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
      RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
      RATE_QUESTS_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1."));
      RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
      RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
      RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(ratesSettings.getProperty("RateRaidDropItems", "1."));
      RATE_DROP_ITEMS_BY_GRANDRAID = Float.parseFloat(ratesSettings.getProperty("RateGrandRaidDropItems", "1."));
      RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
      RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
      RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1."));
      RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
      RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));
      RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15."));
      RATE_DROP_MP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10."));
      RATE_DROP_GREATER_HERBS = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4."));
      RATE_DROP_SUPERIOR_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.8")) * 10.0F;
      RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.2")) * 10.0F;

      RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1."));
      RATE_DROP_ADENAMUL = Float.parseFloat(ratesSettings.getProperty("RateDropAdenaMul", "1."));
      RATE_DROP_SEAL_STONE = Float.parseFloat(ratesSettings.getProperty("RateDropSealStone", "1."));
      RATE_MUL_SEAL_STONE = Float.parseFloat(ratesSettings.getProperty("RateDropSealStoneMul", "1."));
      RATE_DROP_ITEMSRAIDMUL = Float.parseFloat(ratesSettings.getProperty("RateDropRaidMul", "1."));
      RATE_DROP_ITEMSGRANDMUL = Float.parseFloat(ratesSettings.getProperty("RateDropGrandMul", "1."));

      PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
      PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
      PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
      PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
      PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));

      PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
      PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
      SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1."));

      KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
      KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
      KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
      KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
      KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/rates.cfg File.");
    }
  }

  public static void loadAltSettingCfg() {
    try {
      Properties altSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/altsettings.cfg"));
      altSettings.load(is);
      is.close();

      ALT_GAME_TIREDNESS = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "false"));
      ALT_GAME_CREATION = Boolean.parseBoolean(altSettings.getProperty("AltGameCreation", "false"));
      ALT_GAME_CREATION_SPEED = Double.parseDouble(altSettings.getProperty("AltGameCreationSpeed", "1"));
      ALT_GAME_CREATION_XP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateXp", "1"));
      ALT_GAME_CREATION_SP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateSp", "1"));
      ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(altSettings.getProperty("AltBlacksmithUseRecipes", "true"));
      ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "false"));
      ALT_GAME_CANCEL_BOW = (altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow")) || (altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all"));
      ALT_GAME_CANCEL_CAST = (altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast")) || (altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all"));
      ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(altSettings.getProperty("AltShieldBlocks", "false"));
      ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(altSettings.getProperty("AltPerfectShieldBlockRate", "10"));
      ALT_GAME_DELEVEL = Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
      ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "false"));
      ALT_GAME_MOB_ATTACK_AI = Boolean.parseBoolean(altSettings.getProperty("AltGameMobAttackAI", "false"));
      ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(altSettings.getProperty("AltMobAgroInPeaceZone", "true"));
      ALT_GAME_EXPONENT_XP = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
      ALT_GAME_EXPONENT_SP = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
      ALT_GAME_FREIGHTS = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "false"));
      ALT_GAME_FREIGHT_PRICE = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
      ALT_PARTY_RANGE = Integer.parseInt(altSettings.getProperty("AltPartyRange", "1600"));
      ALT_PARTY_RANGE2 = Integer.parseInt(altSettings.getProperty("AltPartyRange2", "1400"));
      REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(altSettings.getProperty("RemoveCastleCirclets", "true"));
      IS_CRAFTING_ENABLED = Boolean.parseBoolean(altSettings.getProperty("CraftingEnabled", "true"));
      LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(altSettings.getProperty("LifeCrystalNeeded", "true"));
      SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("SpBookNeeded", "true"));
      ES_SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("EnchantSkillSpBookNeeded", "true"));
      AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs").equalsIgnoreCase("True");
      ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false")).booleanValue();
      ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanShop", "true")).booleanValue();
      ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseGK", "false")).booleanValue();
      ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTeleport", "true")).booleanValue();
      ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTrade", "true")).booleanValue();
      ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true")).booleanValue();
      ALT_GAME_FREE_TELEPORT = Integer.parseInt(altSettings.getProperty("AltFreeTeleporting", "0"));
      ALT_RECOMMEND = Boolean.parseBoolean(altSettings.getProperty("AltRecommend", "False"));
      ALT_PLAYER_PROTECTION_LEVEL = Integer.parseInt(altSettings.getProperty("AltPlayerProtectionLevel", "0"));
      ALT_GAME_VIEWNPC = Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
      ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(altSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
      ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(altSettings.getProperty("AltMembersCanWithdrawFromClanWH", "False"));
      ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(altSettings.getProperty("AltMaxNumOfClansInAlly", "3"));
      DWARF_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("DwarfRecipeLimit", "50"));
      COMMON_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("CommonRecipeLimit", "50"));

      ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(altSettings.getProperty("AltClanMembersForWar", "15"));
      ALT_CLAN_JOIN_DAYS = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAClan", "5"));
      ALT_CLAN_CREATE_DAYS = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateAClan", "10"));
      ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(altSettings.getProperty("DaysToPassToDissolveAClan", "7"));
      ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
      ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
      ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(altSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
      ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));
      ALT_CLAN_REP_MUL = Float.parseFloat(altSettings.getProperty("AltClanReputationRate", "1.0"));
      ALT_CLAN_CREATE_LEVEL = Integer.parseInt(altSettings.getProperty("AltClanCreateLevel", "1"));
      ALT_CLAN_REP_WAR = Integer.parseInt(altSettings.getProperty("AltClanReputationKillBonus", "2"));

      CASTLE_SHIELD = Boolean.parseBoolean(altSettings.getProperty("CastleShieldRestriction", "True"));
      CLANHALL_SHIELD = Boolean.parseBoolean(altSettings.getProperty("ClanHallShieldRestriction", "True"));
      APELLA_ARMORS = Boolean.parseBoolean(altSettings.getProperty("ApellaArmorsRestriction", "True"));
      OATH_ARMORS = Boolean.parseBoolean(altSettings.getProperty("OathArmorsRestriction", "True"));
      CASTLE_CROWN = Boolean.parseBoolean(altSettings.getProperty("CastleLordsCrownRestriction", "True"));
      CASTLE_CIRCLETS = Boolean.parseBoolean(altSettings.getProperty("CastleCircletsRestriction", "True"));

      ALT_OLY_START_TIME = Integer.parseInt(altSettings.getProperty("AltOlyStartTime", "18"));
      ALT_OLY_MIN = Integer.parseInt(altSettings.getProperty("AltOlyMin", "00"));
      ALT_OLY_BATTLE = Long.parseLong(altSettings.getProperty("AltOlyBattle", "360000"));
      ALT_OLY_BWAIT = Long.parseLong(altSettings.getProperty("AltOlyBWait", "600000"));
      ALT_OLY_IWAIT = Long.parseLong(altSettings.getProperty("AltOlyIWait", "300000"));
      ALT_OLY_CPERIOD = Long.parseLong(altSettings.getProperty("AltOlyCPeriod", "21600000"));
      ALT_OLY_WPERIOD = Long.parseLong(altSettings.getProperty("AltOlyWPeriod", "604800000"));
      ALT_OLY_VPERIOD = Long.parseLong(altSettings.getProperty("AltOlyVPeriod", "43200000"));
      ALT_OLY_SAME_IP = Boolean.parseBoolean(altSettings.getProperty("AltOlySameIp", "True"));
      ALT_OLY_SAME_HWID = Boolean.parseBoolean(altSettings.getProperty("AltOlySameHWID", "True"));
      ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(altSettings.getProperty("AltOlyMaxEnchant", "65535"));
      ALT_OLY_MP_REG = Boolean.parseBoolean(altSettings.getProperty("AltOlyRestoreMP", "False"));
      ALT_OLY_MINCLASS = Integer.parseInt(altSettings.getProperty("AltOlyMinClass", "6"));
      ALT_OLY_MINNONCLASS = Integer.parseInt(altSettings.getProperty("AltOlyMinNonClass", "4"));
      ALT_OLYMPIAD_PERIOD = Integer.parseInt(altSettings.getProperty("AltOlyPeriod", "0"));
      ALT_OLY_RELOAD_SKILLS = Boolean.parseBoolean(altSettings.getProperty("AltOlyReloadSkills", "True"));
      OLY_MAX_WEAPON_ENCH = Integer.parseInt(altSettings.getProperty("AltOlyMaxWeaponEnchant", "65535"));
      OLY_MAX_ARMOT_ENCH = Integer.parseInt(altSettings.getProperty("AltOlyMaxArmorEnchant", "65535"));
      if (OLY_MAX_WEAPON_ENCH == -1) {
        OLY_MAX_WEAPON_ENCH = 65535;
      }
      if (OLY_MAX_ARMOT_ENCH == -1) {
        OLY_MAX_ARMOT_ENCH = 65535;
      }

      String[] propertySplit = altSettings.getProperty("AltOlyFighterBuff", "1204,2;1086,1").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          OLY_FIGHTER_BUFFS.put(Integer.valueOf(Integer.parseInt(aug[0])), Integer.valueOf(Integer.parseInt(aug[1])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("altsettings.cfg: AltOlyFighterBuff error: " + aug[0]);
          }
        }
      }
      propertySplit = altSettings.getProperty("AltOlyMageBuff", "1204,2;1085,1").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          OLY_MAGE_BUFFS.put(Integer.valueOf(Integer.parseInt(aug[0])), Integer.valueOf(Integer.parseInt(aug[1])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("altsettings.cfg: AltOlyMageBuff error: " + aug[0]);
          }
        }
      }
      propertySplit = null;

      ALT_MANOR_REFRESH_TIME = Integer.parseInt(altSettings.getProperty("AltManorRefreshTime", "20"));
      ALT_MANOR_REFRESH_MIN = Integer.parseInt(altSettings.getProperty("AltManorRefreshMin", "00"));
      ALT_MANOR_APPROVE_TIME = Integer.parseInt(altSettings.getProperty("AltManorApproveTime", "6"));
      ALT_MANOR_APPROVE_MIN = Integer.parseInt(altSettings.getProperty("AltManorApproveMin", "00"));
      ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(altSettings.getProperty("AltManorMaintenancePeriod", "360000"));
      ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(altSettings.getProperty("AltManorSaveAllActions", "false"));
      ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(altSettings.getProperty("AltManorSavePeriodRate", "2"));

      ALT_LOTTERY_PRIZE = Integer.parseInt(altSettings.getProperty("AltLotteryPrize", "50000"));
      ALT_LOTTERY_TICKET_PRICE = Integer.parseInt(altSettings.getProperty("AltLotteryTicketPrice", "2000"));
      ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery5NumberRate", "0.6"));
      ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery4NumberRate", "0.2"));
      ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery3NumberRate", "0.2"));
      ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(altSettings.getProperty("AltLottery2and1NumberPrize", "200"));

      DISABLE_GRADE_PENALTY = Boolean.parseBoolean(altSettings.getProperty("DisableGradePenalty", "false"));
      DISABLE_WEIGHT_PENALTY = Boolean.parseBoolean(altSettings.getProperty("DisableWeightPenalty", "false"));
      ALT_DEV_NO_QUESTS = Boolean.parseBoolean(altSettings.getProperty("AltDevNoQuests", "False"));
      ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(altSettings.getProperty("AltDevNoSpawns", "False"));

      FS_TIME_ATTACK = Integer.parseInt(altSettings.getProperty("TimeOfAttack", "50"));
      FS_TIME_COOLDOWN = Integer.parseInt(altSettings.getProperty("TimeOfCoolDown", "5"));
      FS_TIME_ENTRY = Integer.parseInt(altSettings.getProperty("TimeOfEntry", "3"));
      FS_TIME_WARMUP = Integer.parseInt(altSettings.getProperty("TimeOfWarmUp", "2"));
      FS_PARTY_MEMBER_COUNT = Integer.parseInt(altSettings.getProperty("NumberOfNecessaryPartyMembers", "4"));
      if (FS_TIME_ATTACK <= 0) {
        FS_TIME_ATTACK = 50;
      }
      if (FS_TIME_COOLDOWN <= 0) {
        FS_TIME_COOLDOWN = 5;
      }
      if (FS_TIME_ENTRY <= 0) {
        FS_TIME_ENTRY = 3;
      }
      if (FS_TIME_ENTRY <= 0) {
        FS_TIME_ENTRY = 3;
      }
      if (FS_TIME_ENTRY <= 0) {
        FS_TIME_ENTRY = 3;
      }

      RIFT_MIN_PARTY_SIZE = Integer.parseInt(altSettings.getProperty("RiftMinPartySize", "5"));
      RIFT_MAX_JUMPS = Integer.parseInt(altSettings.getProperty("MaxRiftJumps", "4"));
      RIFT_SPAWN_DELAY = Integer.parseInt(altSettings.getProperty("RiftSpawnDelay", "10000"));
      RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMin", "480"));
      RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMax", "600"));
      RIFT_ENTER_COST_RECRUIT = Integer.parseInt(altSettings.getProperty("RecruitCost", "18"));
      RIFT_ENTER_COST_SOLDIER = Integer.parseInt(altSettings.getProperty("SoldierCost", "21"));
      RIFT_ENTER_COST_OFFICER = Integer.parseInt(altSettings.getProperty("OfficerCost", "24"));
      RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(altSettings.getProperty("CaptainCost", "27"));
      RIFT_ENTER_COST_COMMANDER = Integer.parseInt(altSettings.getProperty("CommanderCost", "30"));
      RIFT_ENTER_COST_HERO = Integer.parseInt(altSettings.getProperty("HeroCost", "33"));

      MAX_CHAT_LENGTH = Integer.parseInt(altSettings.getProperty("MaxChatLength", "100"));
      CRUMA_TOWER_LEVEL_RESTRICT = Integer.parseInt(altSettings.getProperty("CrumaTowerLevelRestrict", "56"));
      RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(altSettings.getProperty("BossRoomTimeMultiply", "1.5"));
      NOEPIC_QUESTS = Boolean.parseBoolean(altSettings.getProperty("EpicQuests", "True"));
      ONE_AUGMENT = Boolean.parseBoolean(altSettings.getProperty("OneAugmentEffect", "True"));
      JOB_WINDOW = Boolean.parseBoolean(altSettings.getProperty("JobWindow", "False"));
      USE_SOULSHOTS = Boolean.parseBoolean(altSettings.getProperty("UseSoulShots", "True"));
      USE_ARROWS = Boolean.parseBoolean(altSettings.getProperty("ConsumeArrows", "True"));
      CLEAR_BUFF_ONDEATH = Boolean.parseBoolean(altSettings.getProperty("ClearBuffOnDeath", "True"));
      PROTECT_GATE_PVP = Boolean.parseBoolean(altSettings.getProperty("ProtectGatePvp", "False"));
      MAX_HENNA_BONUS = Integer.parseInt(altSettings.getProperty("MaxHennaBonus", "5"));
      ALT_ANY_SUBCLASS = Boolean.parseBoolean(altSettings.getProperty("AltAnySubClass", "False"));
      ALT_ANY_SUBCLASS_OVERCRAF = Boolean.parseBoolean(altSettings.getProperty("AltAnySubClassOverCraft", "False"));
      ALT_AUGMENT_HERO = Boolean.parseBoolean(altSettings.getProperty("AltAugmentHeroWeapons", "False"));

      WEDDING_ANSWER_TIME = Integer.parseInt(altSettings.getProperty("WeddingAnswerTime", "0"));
      RESURECT_ANSWER_TIME = Integer.parseInt(altSettings.getProperty("ResurrectAnswerTime", "0"));
      SUMMON_ANSWER_TIME = Integer.parseInt(altSettings.getProperty("SummonAnswerTime", "30000"));

      MAX_EXP_LEVEL = Integer.parseInt(altSettings.getProperty("MexLevelExp", "100"));
      FREE_PVP = Boolean.parseBoolean(altSettings.getProperty("FreePvp", "False"));
      PROTECT_GRADE_PVP = Boolean.parseBoolean(altSettings.getProperty("ProtectGradePvp", "False"));

      CLEAR_OLY_BAN = Boolean.parseBoolean(altSettings.getProperty("ClearOlympiadPointsBan", "False"));

      GIVE_ITEM_PET = Boolean.parseBoolean(altSettings.getProperty("GiveItemToPet", "True"));

      DISABLE_PET_FEED = Boolean.parseBoolean(altSettings.getProperty("DisablePetFeed", "False"));

      SIEGE_GUARDS_SPAWN = Boolean.parseBoolean(altSettings.getProperty("SpawnSiegeGuards", "False"));

      TELEPORT_PROTECTION = TimeUnit.SECONDS.toMillis(Integer.parseInt(altSettings.getProperty("TeleportProtection", "0")));

      MAX_MATKSPD_DELAY = Integer.parseInt(altSettings.getProperty("MaxMAtkDelay", "655350"));
      MAX_PATKSPD_DELAY = Integer.parseInt(altSettings.getProperty("MaxPAtkDelay", "655350"));

      MAX_MATK_CALC = Integer.parseInt(altSettings.getProperty("MaxMAtkCalc", "655350"));
      MAX_MDEF_CALC = Integer.parseInt(altSettings.getProperty("MaxMDefCalc", "655350"));

      MIN_ATKSPD_DELAY = Integer.parseInt(altSettings.getProperty("MaxAtkSpdDelay", "333"));

      KICK_USED_ACCOUNT = Boolean.parseBoolean(altSettings.getProperty("KickUsedAccount", "True"));

      DISABLE_CLAN_REQUREMENTS = Boolean.parseBoolean(altSettings.getProperty("FreeClanLevelUp5", "False"));

      MOUNT_EXPIRE = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(altSettings.getProperty("MountTime", "10")));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/altsettings.cfg File.");
    }
  }

  public static void loadSevenSignsCfg() {
    try {
      Properties SevenSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/sevensigns.cfg"));
      SevenSettings.load(is);
      is.close();

      ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireCastleForDawn", "False"));
      ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireClanCastle", "False"));
      ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
      ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
      ALT_FESTIVAL_MANAGER_START = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
      ALT_FESTIVAL_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
      ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
      ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
      ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
      ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
      ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
      ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/sevensigns.cfg File.");
    }
  }

  public static void loadClanHallCfg() {
    try {
      Properties clanhallSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/clanhall.cfg"));
      clanhallSettings.load(is);
      is.close();
      CH_TELE_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000")).longValue();
      CH_TELE1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000")).intValue();
      CH_TELE2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000")).intValue();
      CH_SUPPORT_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000")).longValue();
      CH_SUPPORT1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl1", "86400000")).intValue();
      CH_SUPPORT2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl2", "86400000")).intValue();
      CH_SUPPORT3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl3", "86400000")).intValue();
      CH_SUPPORT4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl4", "86400000")).intValue();
      CH_SUPPORT5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl5", "86400000")).intValue();
      CH_SUPPORT6_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl6", "86400000")).intValue();
      CH_SUPPORT7_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl7", "86400000")).intValue();
      CH_SUPPORT8_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl8", "86400000")).intValue();
      CH_MPREG_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000")).longValue();
      CH_MPREG1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000")).intValue();
      CH_MPREG2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000")).intValue();
      CH_MPREG3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000")).intValue();
      CH_MPREG4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000")).intValue();
      CH_MPREG5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000")).intValue();
      CH_HPREG_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000")).longValue();
      CH_HPREG1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000")).intValue();
      CH_HPREG2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000")).intValue();
      CH_HPREG3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000")).intValue();
      CH_HPREG4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000")).intValue();
      CH_HPREG5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000")).intValue();
      CH_HPREG6_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000")).intValue();
      CH_HPREG7_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000")).intValue();
      CH_HPREG8_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000")).intValue();
      CH_HPREG9_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000")).intValue();
      CH_HPREG10_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000")).intValue();
      CH_HPREG11_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000")).intValue();
      CH_HPREG12_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000")).intValue();
      CH_HPREG13_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000")).intValue();
      CH_EXPREG_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000")).longValue();
      CH_EXPREG1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000")).intValue();
      CH_EXPREG2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000")).intValue();
      CH_EXPREG3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000")).intValue();
      CH_EXPREG4_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000")).intValue();
      CH_EXPREG5_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000")).intValue();
      CH_EXPREG6_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000")).intValue();
      CH_EXPREG7_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000")).intValue();
      CH_ITEM_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000")).longValue();
      CH_ITEM1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000")).intValue();
      CH_ITEM2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000")).intValue();
      CH_ITEM3_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000")).intValue();
      CH_CURTAIN_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeRation", "86400000")).longValue();
      CH_CURTAIN1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000")).intValue();
      CH_CURTAIN2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000")).intValue();
      CH_FRONT_FEE_RATIO = Long.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000")).longValue();
      CH_FRONT1_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000")).intValue();
      CH_FRONT2_FEE = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000")).intValue();

      CLANHALL_PAYMENT = Integer.valueOf(clanhallSettings.getProperty("ClanHallPaymentId", "57")).intValue();
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/clanhall.cfg File.");
    }
  }

  public static void loadNpcCfg() {
    NPC_RAID_REWARDS.clear();
    NPC_EPIC_REWARDS.clear();
    NPC_HIT_PROTECTET.clear();
    BOSS_ITEMS.clear();
    HEALING_SUMMONS.clear();
    try {
      Properties npc_conf = new Properties();
      InputStream is = new FileInputStream(new File("./config/npc.cfg"));
      npc_conf.load(is);
      is.close();

      L2JMOD_CHAMPION_ENABLE = Boolean.parseBoolean(npc_conf.getProperty("ChampionEnable", "false"));
      L2JMOD_CHAMPION_AURA = Boolean.parseBoolean(npc_conf.getProperty("ChampionAura", "false"));
      L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(npc_conf.getProperty("ChampionFrequency", "0"));
      L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(npc_conf.getProperty("ChampionMinLevel", "20"));
      L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(npc_conf.getProperty("ChampionMaxLevel", "60"));
      L2JMOD_CHAMPION_HP = Integer.parseInt(npc_conf.getProperty("ChampionHp", "7"));
      L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(npc_conf.getProperty("ChampionHpRegen", "1."));
      L2JMOD_CHAMPION_REWARDS = Integer.parseInt(npc_conf.getProperty("ChampionRewards", "8"));
      L2JMOD_CHAMPION_ADENAS_REWARDS = Integer.parseInt(npc_conf.getProperty("ChampionAdenasRewards", "1"));
      L2JMOD_CHAMPION_ATK = Float.parseFloat(npc_conf.getProperty("ChampionAtk", "1."));
      L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(npc_conf.getProperty("ChampionSpdAtk", "1."));
      L2JMOD_CHAMPION_REWARD = Integer.parseInt(npc_conf.getProperty("ChampionRewardItem", "0"));
      L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(npc_conf.getProperty("ChampionRewardItemID", "6393"));
      L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(npc_conf.getProperty("ChampionRewardItemQty", "1"));

      RAID_CUSTOM_DROP = Boolean.parseBoolean(npc_conf.getProperty("AllowCustomRaidDrop", "False"));
      String[] propertySplit = npc_conf.getProperty("CustomRaidDrop", "57,1,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          NPC_RAID_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("npc.cfg: CustomRaidDrop error: " + aug[0]);
          }
        }
      }

      propertySplit = npc_conf.getProperty("CustomEpicShadowDrop", "57,1,100").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          NPC_EPIC_REWARDS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("npc.cfg: CustomEpicShadowDrop error: " + aug[0]);
          }
        }
      }

      RAID_CLANPOINTS_REWARD = Integer.parseInt(npc_conf.getProperty("BossClanPointsReward", "0"));
      EPIC_CLANPOINTS_REWARD = Integer.parseInt(npc_conf.getProperty("EpicClanPointsReward", "0"));

      ANTARAS_CLOSE_PORT = TimeUnit.SECONDS.toMillis(Integer.parseInt(npc_conf.getProperty("AntharasClosePort", "30")));
      ANTARAS_UPDATE_LAIR = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("AntharasUpdateLair", "10")));
      ANTARAS_MIN_RESPAWN = convertTime(npc_conf.getProperty("AntharasMinRespawn", "160"));
      ANTARAS_MAX_RESPAWN = convertTime(npc_conf.getProperty("AntharasMaxRespawn", "180"));
      ANTARAS_RESTART_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("AntharasRestartDelay", "5")));
      ANTARAS_SPAWN_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("AntharasSpawnDelay", "10")));

      VALAKAS_CLOSE_PORT = TimeUnit.SECONDS.toMillis(Integer.parseInt(npc_conf.getProperty("ValakasClosePort", "30")));
      VALAKAS_UPDATE_LAIR = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("ValakasUpdateLair", "10")));
      VALAKAS_MIN_RESPAWN = convertTime(npc_conf.getProperty("ValakasMinRespawn", "160"));
      VALAKAS_MAX_RESPAWN = convertTime(npc_conf.getProperty("ValakasMaxRespawn", "180"));
      VALAKAS_RESTART_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("ValakasRestartDelay", "5")));
      VALAKAS_SPAWN_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("ValakasSpawnDelay", "10")));

      BAIUM_CLOSE_PORT = TimeUnit.SECONDS.toMillis(Integer.parseInt(npc_conf.getProperty("BaiumClosePort", "30")));
      BAIUM_UPDATE_LAIR = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("BaiumUpdateLair", "2")));
      BAIUM_MIN_RESPAWN = convertTime(npc_conf.getProperty("BaiumMinRespawn", "110"));
      BAIUM_MAX_RESPAWN = convertTime(npc_conf.getProperty("BaiumMaxRespawn", "130"));
      BAIUM_RESTART_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("BaiumRestartDelay", "5")));

      AQ_MIN_RESPAWN = convertTime(npc_conf.getProperty("AqMinRespawn", "22"));
      AQ_MAX_RESPAWN = convertTime(npc_conf.getProperty("AqMaxRespawn", "26"));
      AQ_RESTART_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("AqRestartDelay", "5")));
      AQ_PLAYER_MAX_LVL = Integer.parseInt(npc_conf.getProperty("AqMaxPlayerLvl", "80"));
      AQ_NURSE_RESPAWN = TimeUnit.SECONDS.toMillis(Integer.parseInt(npc_conf.getProperty("AqNurseRespawn", "15")));

      ZAKEN_MIN_RESPAWN = convertTime(npc_conf.getProperty("ZakenMinRespawn", "22"));
      ZAKEN_MAX_RESPAWN = convertTime(npc_conf.getProperty("ZakenMaxRespawn", "26"));
      ZAKEN_RESTART_DELAY = TimeUnit.MINUTES.toMillis(Integer.parseInt(npc_conf.getProperty("ZakenRestartDelay", "5")));

      FRINTA_MMIN_PARTIES = Integer.parseInt(npc_conf.getProperty("FrintezzaMinPartys", "2"));
      FRINTA_MMIN_PLAYERS = Integer.parseInt(npc_conf.getProperty("FrintezzaMinPlayersInParty", "9"));

      ALLOW_HIT_NPC = Boolean.parseBoolean(npc_conf.getProperty("AllowHitNpc", "True"));
      KILL_NPC_ATTACKER = Boolean.parseBoolean(npc_conf.getProperty("KillNpcAttacker", "False"));

      NPC_SPAWN_DELAY = Long.parseLong(npc_conf.getProperty("NpcSpawnDelay", "0"));
      NPC_SPAWN_TYPE = Integer.parseInt(npc_conf.getProperty("NpcSpawnType", "3"));

      ANNOUNCE_EPIC_STATES = Boolean.parseBoolean(npc_conf.getProperty("AnnounceEpicStates", "False"));

      propertySplit = npc_conf.getProperty("ProtectedHitNpc", "57,1").split(",");
      for (String npc_id : propertySplit) {
        NPC_HIT_PROTECTET.add(Integer.valueOf(Integer.parseInt(npc_id)));
      }
      propertySplit = npc_conf.getProperty("HitNpcLocation", "0,0,0").split(",");
      NPC_HIT_LOCATION = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      BARAKIEL_NOBLESS = Boolean.parseBoolean(npc_conf.getProperty("BarakielDeathNobless", "False"));

      ALLOW_RAID_BOSS_PUT = Boolean.parseBoolean(npc_conf.getProperty("AllowRaidBossPetrified", "True"));

      ALLOW_RAID_BOSS_HEAL = Boolean.parseBoolean(npc_conf.getProperty("AllowRaidBossHeal", "True"));

      propertySplit = npc_conf.getProperty("ZakenSpawnLoc", "55256,219114,-3224").split(",");
      ZAKEN_SPAWN_LOC = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));

      PROTECT_MOBS_ITEMS = Boolean.parseBoolean(npc_conf.getProperty("PlayerPenaltyItems", "False"));

      BOSS_ZONE_MAX_ENCH = Integer.parseInt(npc_conf.getProperty("MaxEnchBossZone", "0"));

      propertySplit = npc_conf.getProperty("ForbiddenItemsBossZone", "").trim().split(",");
      for (String itemid : propertySplit) {
        try {
          BOSS_ITEMS.add(Integer.valueOf(itemid));
        } catch (NumberFormatException nfe) {
          if (!itemid.equals("")) {
            System.out.println("npc.cfg: ForbiddenItemsBossZone error: " + itemid);
          }
        }
      }

      propertySplit = npc_conf.getProperty("HealingSummons", "").trim().split(";");
      for (String heal : propertySplit) {
        if (heal.isEmpty())
        {
          continue;
        }
        String[] heal_data = heal.trim().split(",");
        HEALING_SUMMONS.put(Integer.valueOf(Integer.parseInt(heal_data[0])), new EventReward(Integer.parseInt(heal_data[1]), Integer.parseInt(heal_data[2]), Integer.parseInt(heal_data[3])));
      }
      if (!HEALING_SUMMONS.isEmpty()) {
        propertySplit = npc_conf.getProperty("HsRestore", "").trim().split(",");
        HEALSUM_ANIM = Integer.parseInt(npc_conf.getProperty("HsAnimation", "0"));
        HEALSUM_DELAY = TimeUnit.SECONDS.toMillis(Integer.parseInt(npc_conf.getProperty("HsDelay", "5")));
      }

      propertySplit = null;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/npc.cfg File.");
    }
  }

  private static long convertTime(String time) {
    if (time.endsWith("m")) {
      return TimeUnit.MINUTES.toMillis(Integer.parseInt(time));
    }
    if (time.endsWith("s")) {
      return TimeUnit.SECONDS.toMillis(Integer.parseInt(time));
    }

    return TimeUnit.HOURS.toMillis(Integer.parseInt(time));
  }

  public static void loadCustomCfg() {
    CHAT_FILTER_STRINGS.clear();
    F_OLY_ITEMS.clear();
    MULTVIP_CARDS.clear();
    CASTLE_SIEGE_REWARDS.clear();
    ALT_BUFF_TIME.clear();
    ALT_SKILL_CHANSE.clear();
    ALT_MAGIC_WEAPONS.clear();
    CUSTOM_STRT_ITEMS.clear();
    ALT_FIXED_REUSES.clear();
    PROTECTED_BUFFS.clear();
    CASTLE_SIEGE_SKILLS.clear();
    HIPPY_ITEMS.clear();
    FORB_CURSED_SKILLS.clear();
    FORBIDDEN_BOW_CLASSES.clear();
    try {
      Properties customSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/custom.cfg"));
      customSettings.load(is);
      is.close();

      ALLOW_HERO_SUBSKILL = Boolean.parseBoolean(customSettings.getProperty("CustomHeroSubSkill", "False"));
      SUB_START_LVL = Byte.parseByte(customSettings.getProperty("SubStartLvl", "40"));

      RUN_SPD_BOOST = Integer.parseInt(customSettings.getProperty("RunSpeedBoost", "0"));
      MAX_RUN_SPEED = Integer.parseInt(customSettings.getProperty("MaxRunSpeed", "250"));
      MAX_PCRIT_RATE = Integer.parseInt(customSettings.getProperty("MaxPCritRate", "500"));
      MAX_MCRIT_RATE = Integer.parseInt(customSettings.getProperty("MaxMCritRate", "300"));
      MAX_PATK_SPEED = Integer.parseInt(customSettings.getProperty("MaxPAtkSpeed", "1500"));
      MAX_MATK_SPEED = Integer.parseInt(customSettings.getProperty("MaxMAtkSpeed", "1900"));
      MAX_MAX_HP = Integer.parseInt(customSettings.getProperty("MaxMaxHp", "30000"));

      BUFFS_MAX_AMOUNT = Integer.parseInt(customSettings.getProperty("MaxBuffAmount", "24"));
      BUFFS_PET_MAX_AMOUNT = Integer.parseInt(customSettings.getProperty("MaxPetBuffAmount", "20"));

      MAX_SUBCLASS = Byte.parseByte(customSettings.getProperty("MaxSubClasses", "3"));

      AUTO_LOOT = Boolean.parseBoolean(customSettings.getProperty("AutoLoot", "False"));
      AUTO_LOOT_RAID = Boolean.parseBoolean(customSettings.getProperty("AutoLootRaid", "False"));
      ALT_EPIC_JEWERLY = Boolean.parseBoolean(customSettings.getProperty("AutoLootEpicJewerly", "False"));

      USE_CHAT_FILTER = Boolean.parseBoolean(customSettings.getProperty("UseChatFilter", "True"));
      CHAT_FILTER_STRING = customSettings.getProperty("ChatFilterString", "-_-");
      if (USE_CHAT_FILTER) {
        loadChatFilter();
      }

      ALT_GAME_NEW_CHAR_ALWAYS_IS_NOBLE = Boolean.parseBoolean(customSettings.getProperty("AltNewCharAlwaysIsNoble", "False"));
      ALT_START_LEVEL = Integer.parseInt(customSettings.getProperty("AltStartedLevel", "0"));
      ALT_ALLOW_AUGMENT_ON_OLYMP = Boolean.parseBoolean(customSettings.getProperty("AllowAugmnetOlympiad", "True"));
      ALT_ALLOW_AUC = Boolean.parseBoolean(customSettings.getProperty("AllowAuction", "True"));

      ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(customSettings.getProperty("AltSubClassWithoutQuests", "False"));

      AUTO_LEARN_SKILLS = Boolean.parseBoolean(customSettings.getProperty("AutoLearnSkills", "false"));

      ALT_WEIGHT_LIMIT = Double.parseDouble(customSettings.getProperty("AltWeightLimit", "1"));

      STARTING_ADENA = Integer.parseInt(customSettings.getProperty("StartingAdena", "100"));

      WYVERN_SPEED = Integer.parseInt(customSettings.getProperty("WyvernSpeed", "100"));
      STRIDER_SPEED = Integer.parseInt(customSettings.getProperty("StriderSpeed", "80"));
      WATER_SPEED = Integer.parseInt(customSettings.getProperty("WaterSpeed", "60"));

      AUGMENT_BASESTAT = Integer.parseInt(customSettings.getProperty("AugmentBasestat", "1"));
      AUGMENT_SKILL_NORM = Integer.parseInt(customSettings.getProperty("AugmentSkillNormal", "11"));
      AUGMENT_SKILL_MID = Integer.parseInt(customSettings.getProperty("AugmentSkillMid", "11"));
      AUGMENT_SKILL_HIGH = Integer.parseInt(customSettings.getProperty("AugmentSkillHigh", "11"));
      AUGMENT_SKILL_TOP = Integer.parseInt(customSettings.getProperty("AugmentSkillTop", "11"));

      ALLOW_RUPOR = Boolean.parseBoolean(customSettings.getProperty("AllowRupor", "False"));
      RUPOR_ID = Integer.parseInt(customSettings.getProperty("RuporId", "50002"));

      KICK_L2WALKER = Boolean.parseBoolean(customSettings.getProperty("L2WalkerProtection", "True"));

      ALLOW_RAID_PVP = Boolean.parseBoolean(customSettings.getProperty("AllowRaidPvpZones", "True"));
      CP_REUSE_TIME = Long.parseLong(customSettings.getProperty("CpReuseTime", "200"));
      MANA_RESTORE = Long.parseLong(customSettings.getProperty("ManaRestore", "800"));
      ANTIBUFF_SKILLID = Integer.parseInt(customSettings.getProperty("AntiBuffSkillId", "2276"));
      MAGIC_CRIT_EXP = Double.parseDouble(customSettings.getProperty("MagicCritExp", "4"));
      MAGIC_DAM_EXP = Double.parseDouble(customSettings.getProperty("MagicDamExp", "1"));
      MAGIC_PDEF_EXP = Double.parseDouble(customSettings.getProperty("MagicPdefExp", "1"));

      BLOW_CHANCE_FRONT = Integer.parseInt(customSettings.getProperty("BlowChanceFront", "50"));
      BLOW_CHANCE_BEHIND = Integer.parseInt(customSettings.getProperty("BlowChanceBehind", "70"));
      BLOW_CHANCE_SIDE = Integer.parseInt(customSettings.getProperty("BlowChanceSide", "60"));

      BLOW_DAMAGE_HEAVY = Double.parseDouble(customSettings.getProperty("BlowDamageHeavy", "1"));
      BLOW_DAMAGE_LIGHT = Double.parseDouble(customSettings.getProperty("BlowDamageLight", "1"));
      BLOW_DAMAGE_ROBE = Double.parseDouble(customSettings.getProperty("BlowDamageRobe", "1"));

      SONLINE_ANNOUNE = Boolean.parseBoolean(customSettings.getProperty("AnnounceOnline", "False"));
      SONLINE_ANNOUNCE_DELAY = (int)TimeUnit.MINUTES.toMillis(Integer.parseInt(customSettings.getProperty("AnnounceDelay", "10")));
      SONLINE_SHOW_MAXONLINE = Boolean.parseBoolean(customSettings.getProperty("ShowMaxOnline", "False"));
      SONLINE_SHOW_MAXONLINE_DATE = Boolean.parseBoolean(customSettings.getProperty("ShowMaxOnlineDate", "False"));
      SONLINE_SHOW_OFFLINE = Boolean.parseBoolean(customSettings.getProperty("ShowOfflineTraders", "False"));
      SONLINE_LOGIN_ONLINE = Boolean.parseBoolean(customSettings.getProperty("AnnounceOnLogin", "False"));
      SONLINE_LOGIN_MAX = Boolean.parseBoolean(customSettings.getProperty("ShowLoginMaxOnline", "False"));
      SONLINE_LOGIN_DATE = Boolean.parseBoolean(customSettings.getProperty("ShowLoginMaxOnlineDate", "False"));
      SONLINE_LOGIN_OFFLINE = Boolean.parseBoolean(customSettings.getProperty("ShowLoginOfflineTraders", "False"));

      AUTO_ANNOUNCE_ALLOW = Boolean.parseBoolean(customSettings.getProperty("AutoAnnouncementsAllow", "False"));
      AUTO_ANNOUNCE_DELAY = Integer.parseInt(customSettings.getProperty("AutoAnnouncementsDelay", "600000"));

      String[] propertySplit = customSettings.getProperty("ForbiddenOlympItems", "1234").split(",");
      for (String id : propertySplit) {
        F_OLY_ITEMS.add(Integer.valueOf(Integer.parseInt(id)));
      }

      FORBIDDEN_EVENT_ITMES = Boolean.parseBoolean(customSettings.getProperty("ForbiddenOnEvents", "False"));

      INVIS_SHOW = Boolean.parseBoolean(customSettings.getProperty("BroadcastInvis", "True"));
      CLAN_CH_CLEAN = TimeUnit.DAYS.toMillis(Integer.parseInt(customSettings.getProperty("ClanHallFreeAfter", "7")));
      CHECK_SKILLS = Boolean.parseBoolean(customSettings.getProperty("CheckSkills", "True"));

      propertySplit = customSettings.getProperty("MultisellTickets", "502,6399;503,6400;504,6401;505,6402").split(";");
      for (String augs : propertySplit) {
        String[] aug = augs.split(",");
        try {
          MULTVIP_CARDS.put(Integer.valueOf(aug[0]), Integer.valueOf(aug[1]));
        } catch (NumberFormatException nfe) {
          if (aug.length > 0) {
            System.out.println("custom.cfg: MultisellTickets error: " + aug[0]);
          }
        }
      }

      ALLOW_APELLA_BONUSES = Boolean.parseBoolean(customSettings.getProperty("AllowApellaPassives", "True"));
      SHOW_ENTER_WARNINGS = Boolean.parseBoolean(customSettings.getProperty("ShowLoginWarnings", "False"));

      propertySplit = customSettings.getProperty("CastleSiegeRewards", "").split(";");
      for (String augs : propertySplit) {
        if (augs.equals(""))
        {
          continue;
        }
        String[] aug = augs.split("#");
        int castleId = Integer.parseInt(aug[0]);
        String[] locs = aug[1].split(":");
        FastList locfl = new FastList();
        for (String points : locs) {
          String[] loc = points.split(",");
          try {
            locfl.add(new EventReward(Integer.parseInt(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2])));
          } catch (NumberFormatException nfe) {
            if (!loc.equals("")) {
              System.out.println("custom.cfg: CastleSiegeRewards error: " + aug[0]);
            }
          }
        }
        CASTLE_SIEGE_REWARDS.put(Integer.valueOf(castleId), locfl);
      }

      propertySplit = customSettings.getProperty("CastleSiegeSkillRewards", "").split(";");
      for (String augs : propertySplit) {
        if (augs.equals(""))
        {
          continue;
        }
        String[] aug = augs.split("#");
        int castleId = Integer.parseInt(aug[0]);
        String[] locs = aug[1].split(",");
        try {
          CASTLE_SIEGE_SKILLS.put(Integer.valueOf(castleId), new EventReward(Integer.parseInt(locs[0]), Integer.parseInt(locs[1]), 0));
        } catch (NumberFormatException nfe) {
          if (!locs.equals("")) {
            System.out.println("custom.cfg: CastleSiegeSkillRewards error: " + aug[0]);
          }
        }

      }

      ALT_BUFF_TIMEMUL = Integer.parseInt(customSettings.getProperty("BuffTimeMul", "0"));
      propertySplit = customSettings.getProperty("BuffTimeTable", "").split(";");
      for (String augs : propertySplit) {
        if (augs.equals(""))
        {
          continue;
        }
        String[] aug = augs.split("#");
        int time = Integer.parseInt(aug[0]);
        String[] buffs = aug[1].split(",");
        for (String bufftime : buffs) {
          try {
            ALT_BUFF_TIME.put(Integer.valueOf(Integer.parseInt(bufftime)), Integer.valueOf(time));
          } catch (NumberFormatException nfe) {
            if (!bufftime.equals("")) {
              System.out.println("custom.cfg: BuffTimeTable error: " + bufftime);
            }
          }
        }
      }

      propertySplit = customSettings.getProperty("CustomMagicWeapons", "").split(",");
      for (String itemid : propertySplit) {
        try {
          ALT_MAGIC_WEAPONS.add(Integer.valueOf(itemid));
        } catch (NumberFormatException nfe) {
          if (!itemid.equals("")) {
            System.out.println("custom.cfg: CustomMagicWeapons error: " + itemid);
          }
        }
      }

      propertySplit = customSettings.getProperty("StartUpItems", "").split(";");
      for (String augs : propertySplit) {
        if (augs.equals(""))
        {
          break;
        }
        String[] aug = augs.split(",");
        try {
          CUSTOM_STRT_ITEMS.put(Integer.valueOf(Integer.parseInt(aug[0])), Integer.valueOf(Integer.parseInt(aug[1])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("custom.cfg: StartUpItems error: " + aug[0]);
          }
        }
      }

      propertySplit = customSettings.getProperty("ForbiddenBowClasses", "").trim().split(",");
      for (String itemid : propertySplit) {
        try {
          FORBIDDEN_BOW_CLASSES.add(Integer.valueOf(itemid));
        } catch (NumberFormatException nfe) {
          if (!itemid.equals("")) {
            System.out.println("custom.cfg: ForbiddenBowClasses error: " + itemid);
          }
        }
      }

      propertySplit = customSettings.getProperty("HippyItems", "").trim().split(",");
      for (String itemid : propertySplit) {
        try {
          HIPPY_ITEMS.add(Integer.valueOf(itemid));
        } catch (NumberFormatException nfe) {
          if (!itemid.equals("")) {
            System.out.println("custom.cfg: HippyItems error: " + itemid);
          }
        }
      }

      propertySplit = customSettings.getProperty("ForbiddenCursedSkills", "").trim().split(",");
      for (String itemid : propertySplit) {
        try {
          FORB_CURSED_SKILLS.add(Integer.valueOf(itemid));
        } catch (NumberFormatException nfe) {
          if (!itemid.equals("")) {
            System.out.println("custom.cfg: ForbiddenCursedSkills error: " + itemid);
          }
        }
      }

      propertySplit = customSettings.getProperty("FixedReuseSkills", "").trim().split(",");
      for (String itemid : propertySplit) {
        try {
          ALT_FIXED_REUSES.add(Integer.valueOf(itemid));
        } catch (NumberFormatException nfe) {
          if (!itemid.equals("")) {
            System.out.println("custom.cfg: FixedReuseSkills error: " + itemid);
          }
        }
      }
      propertySplit = null;

      propertySplit = customSettings.getProperty("CancelProtectedBuffs", "").trim().split(",");
      for (String itemid : propertySplit) {
        try {
          PROTECTED_BUFFS.add(Integer.valueOf(itemid));
        } catch (NumberFormatException nfe) {
          if (!itemid.equals("")) {
            System.out.println("custom.cfg: CancelProtectedBuffs error: " + itemid);
          }
        }
      }
      propertySplit = null;

      HERO_ITEMS_PENALTY = Boolean.parseBoolean(customSettings.getProperty("HeroItemsPenalty", "True"));

      ACADEMY_CLASSIC = Boolean.parseBoolean(customSettings.getProperty("ClanAcademyClassic", "True"));
      ACADEMY_POINTS = Integer.parseInt(customSettings.getProperty("ClanAcademyPoints", "400"));

      DISABLE_FORCES = Boolean.parseBoolean(customSettings.getProperty("DisableForces", "False"));

      ALLOW_CURSED_QUESTS = Boolean.parseBoolean(customSettings.getProperty("AllowCursedQuestTalk", "True"));

      MAX_TRADE_ENCHANT = Integer.parseInt(customSettings.getProperty("TradeEnchantLimit", "0"));

      ALT_SIEGE_INTERVAL = Integer.parseInt(customSettings.getProperty("SiegeInterval", "14"));

      SOULSHOT_ANIM = Boolean.parseBoolean(customSettings.getProperty("SoulshotAnimation", "True"));

      MAX_AUGMENTS_BUFFS = Integer.parseInt(customSettings.getProperty("MaxAugmentBuffs", "1"));

      STARTUP_TITLE = customSettings.getProperty("StartUpTitle", "off");

      PICKUP_PENALTY = TimeUnit.SECONDS.toMillis(Integer.parseInt(customSettings.getProperty("PickUpPenalty", "15")));

      SKILLS_CHANCE_MIN = Double.parseDouble(customSettings.getProperty("SkillsChanceMin", "5.0d"));
      SKILLS_CHANCE_MAX = Double.parseDouble(customSettings.getProperty("SkillsChanceMax", "95.0d"));

      DISABLE_BOSS_INTRO = Boolean.parseBoolean(customSettings.getProperty("DisableBossIntro", "False"));

      DEATH_REFLECT = Boolean.parseBoolean(customSettings.getProperty("DeathReflect", "False"));

      PROTECT_SAY = Boolean.parseBoolean(customSettings.getProperty("ChatFloodFilter", "False"));
      PROTECT_SAY_COUNT = Integer.parseInt(customSettings.getProperty("ChatFloodFilterCount", "5"));
      PROTECT_SAY_BAN = Integer.parseInt(customSettings.getProperty("ChatFloodFilterBan", "5")) * 60;
      PROTECT_SAY_INTERVAL = TimeUnit.SECONDS.toMillis(Integer.parseInt(customSettings.getProperty("ChatFloodFilterInterval", "10")));

      MIRAGE_CHANCE = Integer.parseInt(customSettings.getProperty("MirageChanse", "50"));

      SUMMON_CP_PROTECT = Boolean.parseBoolean(customSettings.getProperty("ProtectSummonCpFlood", "False"));

      ALLOW_NPC_CHAT = Boolean.parseBoolean(customSettings.getProperty("AllowNpcChat", "False"));
      MNPC_CHAT_CHANCE = Integer.parseInt(customSettings.getProperty("NpcChatChanse", "50"));

      MULTISSELL_PROTECT = Boolean.parseBoolean(customSettings.getProperty("MultisellProtect", "True"));

      MULTISSELL_ERRORS = Boolean.parseBoolean(customSettings.getProperty("MultisellErrors", "True"));

      CHEST_CHANCE = Integer.parseInt(customSettings.getProperty("ChestOpenChance", "80"));

      MOB_DEBUFF_CHANCE = Integer.parseInt(customSettings.getProperty("MonsterDebuffChance", "60"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/custom.cfg File.");
    }
  }

  public static void loadPvpCfg() {
    KARMA_LIST_NONDROPPABLE_PET_ITEMS.clear();
    KARMA_LIST_NONDROPPABLE_ITEMS.clear();
    PVPPK_EXP_SP.clear();
    PVPPK_PVPITEMS.clear();
    PVPPK_PKITEMS.clear();
    PVPBONUS_ITEMS.clear();
    PVPBONUS_COLORS.clear();
    PVPBONUS_COLORS_NAME.clear();
    PVPBONUS_COLORS_TITLE.clear();

    PVPBONUS_COLORS_NAME.add(Integer.valueOf(16777215));
    PVPBONUS_COLORS_TITLE.add(Integer.valueOf(16777079));
    try
    {
      Properties pvpSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/pvp.cfg"));
      pvpSettings.load(is);
      is.close();

      KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
      KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
      KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
      KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));

      KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
      KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));

      KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
      KARMA_PK_NPC_DROP = Boolean.parseBoolean(pvpSettings.getProperty("DropKilledByNpc", "True"));

      KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
      KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");

      KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList();
      for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(",")) {
        KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.valueOf(Integer.parseInt(id)));
      }

      KARMA_LIST_NONDROPPABLE_ITEMS = new FastList();
      for (String id : KARMA_NONDROPPABLE_ITEMS.split(",")) {
        KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.valueOf(Integer.parseInt(id)));
      }

      PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "15000"));
      PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "30000"));

      ALLOW_PVPPK_REWARD = Boolean.parseBoolean(pvpSettings.getProperty("AllowPvpPkReward", "False"));
      PVPPK_INTERVAL = Integer.parseInt(pvpSettings.getProperty("PvpPkInterval", "30000"));
      PVPPK_IPPENALTY = Boolean.parseBoolean(pvpSettings.getProperty("PvpPkIpPenalty", "True"));

      String[] ppp = pvpSettings.getProperty("PvpPkLevelPenalty", "0,0").split(",");
      PVPPK_PENALTY = new PvpColor(Integer.parseInt(ppp[0]), Integer.parseInt(ppp[1]));

      ppp = pvpSettings.getProperty("PvpExpSp", "0,0;0,0").split(";");
      for (String augs : ppp) {
        String[] aug = augs.split(",");
        try {
          PVPPK_EXP_SP.add(new PvpColor(Integer.parseInt(aug[0]), Integer.parseInt(aug[1])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("pvp.cfg: PvpExpSp error: " + aug[0]);
          }
        }
      }

      ppp = pvpSettings.getProperty("PvPRewards", "n,n,n;n,n,n").split(";");
      for (String augs : ppp) {
        if (augs.equals("n,n,n"))
        {
          break;
        }
        String[] aug = augs.split(",");
        try {
          PVPPK_PVPITEMS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("pvp.cfg: PvpExpSp error: " + aug[0]);
          }
        }
      }
      ppp = pvpSettings.getProperty("PkRewards", "n,n,n;n,n,n").split(";");
      for (String augs : ppp) {
        if (augs.equals("n,n,n"))
        {
          break;
        }
        String[] aug = augs.split(",");
        try {
          PVPPK_PKITEMS.add(new EventReward(Integer.parseInt(aug[0]), Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
        } catch (NumberFormatException nfe) {
          if (!aug[0].equals("")) {
            System.out.println("pvp.cfg: PvpExpSp error: " + aug[0]);
          }
        }

      }

      ALLOW_PVPBONUS_STEPS = Boolean.parseBoolean(pvpSettings.getProperty("AllowPvpBonusSteps", "False"));

      String pvpBonusStepsColors = pvpSettings.getProperty("PvpBonusStepsRewards", "None");
      if (!pvpBonusStepsColors.equals("None")) {
        String[] propertySplit = pvpBonusStepsColors.split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            PVPBONUS_ITEMS.put(Integer.valueOf(Integer.parseInt(aug[0])), new PvpColor(Integer.parseInt(aug[1]), Integer.parseInt(aug[2])));
          } catch (NumberFormatException nfe) {
            if (!aug[0].equals("")) {
              System.out.println("pvp.cfg: PvpBonusStepsColors error: " + aug[0]);
            }
          }
        }
        propertySplit = null;
      }
      pvpBonusStepsColors = pvpSettings.getProperty("PvpBonusStepsColors", "None");
      if (!pvpBonusStepsColors.equals("None")) {
        String[] propertySplit = pvpBonusStepsColors.split(";");
        for (String augs : propertySplit) {
          String[] aug = augs.split(",");
          try {
            String nick = new TextBuilder(aug[1]).reverse().toString();
            String title = new TextBuilder(aug[2]).reverse().toString();

            int nick_hex = 0;
            int title_hex = 0;

            if (!nick.equals("llun")) {
              nick_hex = Integer.decode("0x" + nick).intValue();
            }

            if (!title.equals("llun")) {
              title_hex = Integer.decode("0x" + title).intValue();
            }

            PVPBONUS_COLORS_NAME.add(Integer.valueOf(nick_hex));
            PVPBONUS_COLORS_TITLE.add(Integer.valueOf(title_hex));
            PVPBONUS_COLORS.put(Integer.valueOf(Integer.parseInt(aug[0])), new PvpColor(nick_hex, title_hex));
          } catch (NumberFormatException nfe) {
            if (!aug[0].equals("")) {
              System.out.println("pvp.cfg: PvpBonusStepsColors error: " + aug[0]);
            }
          }
        }
        propertySplit = null;
      }
      pvpBonusStepsColors = null;
      PVPPK_STEP = Integer.parseInt(pvpSettings.getProperty("PvpPkStep", "0"));
      PVPPK_STEPBAN = TimeUnit.MINUTES.toMillis(Integer.parseInt(pvpSettings.getProperty("PvpPkStepBan", "10")));
      PVPPK_REWARD_ZONE = Boolean.parseBoolean(pvpSettings.getProperty("PvpPkRewardZone", "False"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/pvp.cfg File.");
    }
  }

  public static void loadAccessLvlCfg() {
    try {
      Properties gmSettings = new Properties();
      InputStream is = new FileInputStream(new File("./config/GMAccess.cfg"));
      gmSettings.load(is);
      is.close();

      GM_ACCESSLEVEL = Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
      GM_MIN = Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
      GM_ALTG_MIN_LEVEL = Integer.parseInt(gmSettings.getProperty("GMCanAltG", "100"));
      GM_ANNOUNCE = Integer.parseInt(gmSettings.getProperty("GMCanAnnounce", "100"));
      GM_BAN = Integer.parseInt(gmSettings.getProperty("GMCanBan", "100"));
      GM_BAN_CHAT = Integer.parseInt(gmSettings.getProperty("GMCanBanChat", "100"));
      GM_CREATE_ITEM = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
      GM_DELETE = Integer.parseInt(gmSettings.getProperty("GMCanDelete", "100"));
      GM_KICK = Integer.parseInt(gmSettings.getProperty("GMCanKick", "100"));
      GM_MENU = Integer.parseInt(gmSettings.getProperty("GMMenu", "100"));
      GM_GODMODE = Integer.parseInt(gmSettings.getProperty("GMGodMode", "100"));
      GM_CHAR_EDIT = Integer.parseInt(gmSettings.getProperty("GMCanEditChar", "100"));
      GM_CHAR_EDIT_OTHER = Integer.parseInt(gmSettings.getProperty("GMCanEditCharOther", "100"));
      GM_CHAR_VIEW = Integer.parseInt(gmSettings.getProperty("GMCanViewChar", "100"));
      GM_NPC_EDIT = Integer.parseInt(gmSettings.getProperty("GMCanEditNPC", "100"));
      GM_NPC_VIEW = Integer.parseInt(gmSettings.getProperty("GMCanViewNPC", "100"));
      GM_TELEPORT = Integer.parseInt(gmSettings.getProperty("GMCanTeleport", "100"));
      GM_TELEPORT_OTHER = Integer.parseInt(gmSettings.getProperty("GMCanTeleportOther", "100"));
      GM_RESTART = Integer.parseInt(gmSettings.getProperty("GMCanRestart", "100"));
      GM_MONSTERRACE = Integer.parseInt(gmSettings.getProperty("GMMonsterRace", "100"));
      GM_RIDER = Integer.parseInt(gmSettings.getProperty("GMRider", "100"));
      GM_ESCAPE = Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
      GM_FIXED = Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
      GM_CREATE_NODES = Integer.parseInt(gmSettings.getProperty("GMCreateNodes", "100"));
      GM_ENCHANT = Integer.parseInt(gmSettings.getProperty("GMEnchant", "100"));
      GM_DOOR = Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
      GM_RES = Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
      GM_PEACEATTACK = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
      GM_HEAL = Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
      GM_UNBLOCK = Integer.parseInt(gmSettings.getProperty("GMUnblock", "100"));
      GM_CACHE = Integer.parseInt(gmSettings.getProperty("GMCache", "100"));
      GM_TALK_BLOCK = Integer.parseInt(gmSettings.getProperty("GMTalkBlock", "100"));
      GM_TEST = Integer.parseInt(gmSettings.getProperty("GMTest", "100"));

      String gmTrans = gmSettings.getProperty("GMDisableTransaction", "False");

      if (!gmTrans.equalsIgnoreCase("false")) {
        String[] params = gmTrans.split(",");
        GM_DISABLE_TRANSACTION = true;
        GM_TRANSACTION_MIN = Integer.parseInt(params[0]);
        GM_TRANSACTION_MAX = Integer.parseInt(params[1]);
      } else {
        GM_DISABLE_TRANSACTION = false;
      }
      GM_CAN_GIVE_DAMAGE = Integer.parseInt(gmSettings.getProperty("GMCanGiveDamage", "90"));
      GM_DONT_TAKE_AGGRO = Integer.parseInt(gmSettings.getProperty("GMDontTakeAggro", "90"));
      GM_DONT_TAKE_EXPSP = Integer.parseInt(gmSettings.getProperty("GMDontGiveExpSp", "90"));
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/GMAccess.cfg File.");
    }
  }

  public static void loadCommandsCfg() {
    try {
      Properties cmd = new Properties();
      InputStream is = new FileInputStream(new File("./config/commands.cfg"));
      cmd.load(is);
      is.close();

      CMD_MENU = Boolean.parseBoolean(cmd.getProperty("AllowMenu", "False"));
      VS_NOEXP = Boolean.parseBoolean(cmd.getProperty("NoExp", "False"));
      VS_NOREQ = Boolean.parseBoolean(cmd.getProperty("NoRequests", "False"));
      VS_VREF = Boolean.parseBoolean(cmd.getProperty("VoteRef", "False"));
      VS_AUTOLOOT = Boolean.parseBoolean(cmd.getProperty("Autoloot", "False"));
      VS_TRADERSIGNORE = Boolean.parseBoolean(cmd.getProperty("TradersIgnore", "False"));
      VS_PATHFIND = Boolean.parseBoolean(cmd.getProperty("GeoPathFinding", "False"));
      VS_CHATIGNORE = Boolean.parseBoolean(cmd.getProperty("ChatIgnore", "False"));
      VS_ONLINE = Boolean.parseBoolean(cmd.getProperty("ShowOnline", "False"));
      VS_AUTORESTAT = Boolean.parseBoolean(cmd.getProperty("ShowRestartTime", "False"));
      VS_SKILL_CHANCES = Boolean.parseBoolean(cmd.getProperty("SkillChances", "False"));
      VS_ANIM_SHOTS = Boolean.parseBoolean(cmd.getProperty("ShotsAnimation", "False"));

      VS_AUTOLOOT_VAL = Integer.parseInt(cmd.getProperty("AutolootDefault", "0"));
      VS_PATHFIND_VAL = Integer.parseInt(cmd.getProperty("GeoPathFindingDefault", "0"));
      VS_SKILL_CHANCES_VAL = Integer.parseInt(cmd.getProperty("SkillChancesDefault", "0"));

      VS_HWID = Boolean.parseBoolean(cmd.getProperty("HWID", "False"));
      VS_PWD = Boolean.parseBoolean(cmd.getProperty("Password", "False"));
      VS_EMAIL = Boolean.parseBoolean(cmd.getProperty("Email", "False"));

      ALT_ALLOW_OFFLINE_TRADE = Boolean.parseBoolean(cmd.getProperty("AllowOfflineTrade", "False"));
      ALT_RESTORE_OFFLINE_TRADE = Boolean.parseBoolean(cmd.getProperty("RestoreOfflineTraders", "False"));
      ALT_OFFLINE_TRADE_LIMIT = TimeUnit.HOURS.toMillis(Integer.parseInt(cmd.getProperty("OfflineLimit", "96")));
      ALT_OFFLINE_TRADE_ONLINE = Boolean.parseBoolean(cmd.getProperty("AltOfflineTraderStatus", "False"));

      CMD_ADENA_COL = Boolean.parseBoolean(cmd.getProperty("CmdAdenaCol", "False"));
      String[] pSplit = cmd.getProperty("AdenaToCol", "4037,1,2000000000").split(",");
      CMD_AC_ADENA = new EventReward(Integer.parseInt(pSplit[0]), Integer.parseInt(pSplit[1]), Integer.parseInt(pSplit[2]));
      pSplit = cmd.getProperty("ColToAdena", "57,1700000000,1").split(",");
      CMD_AC_COL = new EventReward(Integer.parseInt(pSplit[0]), Integer.parseInt(pSplit[1]), Integer.parseInt(pSplit[2]));
      pSplit = null;
      CMD_AC_ADENA_LIMIT = Integer.parseInt(cmd.getProperty("AdenaToColLimit", "0"));
      CMD_AC_COL_LIMIT = Integer.parseInt(cmd.getProperty("ColToAdenaLimit", "0"));

      CMD_EVENTS = Boolean.parseBoolean(cmd.getProperty("CmdEvents", "False"));

      MAX_BAN_CHAT = Integer.parseInt(cmd.getProperty("MaxBanChat", "15"));

      VS_CKEY = Boolean.parseBoolean(cmd.getProperty("CharKey", "False"));
      VS_CKEY_CHARLEVEL = Boolean.parseBoolean(cmd.getProperty("CharKey2ndClass", "False"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/commands.cfg File.");
    }
  }

  public static void loadGeoDataCfg() {
    try {
      Properties geo = new Properties();
      InputStream is = new FileInputStream(new File("./config/geodata.cfg"));
      geo.load(is);
      is.close();

      GEODATA = Integer.parseInt(geo.getProperty("GeoData", "0"));
      GEO_TYPE = Integer.parseInt(geo.getProperty("GeoDataType", "1"));
      GEO_L2J_PATH = geo.getProperty("PathL2J", "./data/geodata/");
      GEO_OFF_PATH = geo.getProperty("PathOFF", "./data/geodataoff/");

      GEO_SHOW_LOAD = Boolean.parseBoolean(geo.getProperty("GeoDataLog", "True"));

      FORCE_GEODATA = Boolean.parseBoolean(geo.getProperty("ForceGeoData", "True"));
      ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(geo.getProperty("AcceptGeoeditorConn", "False"));

      MAP_MIN_X = Integer.parseInt(geo.getProperty("GeoFirstX", "-163840"));
      MAP_MAX_X = Integer.parseInt(geo.getProperty("GeoFirstY", "229375"));
      MAP_MIN_Y = Integer.parseInt(geo.getProperty("GeoLastX", "-262144"));
      MAP_MAX_Y = Integer.parseInt(geo.getProperty("GeoLastY", "294911"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/geodata.cfg File.");
    }
  }

  public static void loadFakeCfg() {
    try {
      Properties fake = new Properties();
      InputStream is = new FileInputStream(new File("./config/fakeplayers.cfg"));
      fake.load(is);
      is.close();

      ALLOW_FAKE_PLAYERS_PLUS = Boolean.parseBoolean(fake.getProperty("AllowFake", "False"));

      FAKE_PLAYERS_PLUS_COUNT_FIRST = Integer.parseInt(fake.getProperty("FirstCount", "50"));
      FAKE_PLAYERS_PLUS_DELAY_FIRST = TimeUnit.MINUTES.toMillis(Integer.parseInt(fake.getProperty("FirstDelay", "5")));
      FAKE_PLAYERS_PLUS_DESPAWN_FIRST = TimeUnit.MINUTES.toMillis(Integer.parseInt(fake.getProperty("FirstDespawn", "60")));
      FAKE_PLAYERS_PLUS_DELAY_SPAWN_FIRST = (int)TimeUnit.SECONDS.toMillis(Integer.parseInt(fake.getProperty("FirstDelaySpawn", "1")));
      FAKE_PLAYERS_PLUS_DELAY_DESPAWN_FIRST = (int)TimeUnit.SECONDS.toMillis(Integer.parseInt(fake.getProperty("FirstDelayDespawn", "20")));

      FAKE_PLAYERS_PLUS_COUNT_NEXT = Integer.parseInt(fake.getProperty("NextCount", "50"));
      FAKE_PLAYERS_PLUS_DELAY_NEXT = TimeUnit.MINUTES.toMillis(Integer.parseInt(fake.getProperty("NextDelay", "15")));
      FAKE_PLAYERS_PLUS_DESPAWN_NEXT = TimeUnit.MINUTES.toMillis(Integer.parseInt(fake.getProperty("NextDespawn", "90")));
      FAKE_PLAYERS_PLUS_DELAY_SPAWN_NEXT = (int)TimeUnit.SECONDS.toMillis(Integer.parseInt(fake.getProperty("NextDelaySpawn", "20")));
      FAKE_PLAYERS_PLUS_DELAY_DESPAWN_NEXT = (int)TimeUnit.SECONDS.toMillis(Integer.parseInt(fake.getProperty("NextDelayDespawn", "30")));

      String[] ppp = fake.getProperty("FakeEnchant", "0,14").split(",");
      FAKE_PLAYERS_ENCHANT = new PvpColor(Integer.parseInt(ppp[0]), Integer.parseInt(ppp[1]));

      ppp = fake.getProperty("FakeNameColors", "FFFFFF,FFFFFF").split(",");
      for (String ncolor : ppp) {
        String nick = new TextBuilder(ncolor).reverse().toString();
        FAKE_PLAYERS_NAME_CLOLORS.add(Integer.decode("0x" + nick));
      }
      ppp = fake.getProperty("FakeTitleColors", "FFFF77,FFFF77").split(",");
      for (String tcolor : ppp) {
        String title = new TextBuilder(tcolor).reverse().toString();
        FAKE_PLAYERS_TITLE_CLOLORS.add(Integer.decode("0x" + title));
      }

      FAKE_MAX_PATK_BOW = Integer.parseInt(fake.getProperty("MaxPatkBow", "50"));
      FAKE_MAX_MDEF_BOW = Integer.parseInt(fake.getProperty("MaxMdefBow", "50"));
      FAKE_MAX_PSPD_BOW = Integer.parseInt(fake.getProperty("MaxPspdBow", "50"));
      FAKE_MAX_PDEF_BOW = Integer.parseInt(fake.getProperty("MaxPdefBow", "50"));
      FAKE_MAX_MATK_BOW = Integer.parseInt(fake.getProperty("MaxMatkBow", "50"));
      FAKE_MAX_MSPD_BOW = Integer.parseInt(fake.getProperty("MaxMspdBow", "50"));
      FAKE_MAX_HP_BOW = Integer.parseInt(fake.getProperty("MaxHpBow", "50"));

      FAKE_MAX_PATK_MAG = Integer.parseInt(fake.getProperty("MaxPatkMage", "50"));
      FAKE_MAX_MDEF_MAG = Integer.parseInt(fake.getProperty("MaxMdefMage", "50"));
      FAKE_MAX_PSPD_MAG = Integer.parseInt(fake.getProperty("MaxPspdMage", "50"));
      FAKE_MAX_PDEF_MAG = Integer.parseInt(fake.getProperty("MaxPdefkMage", "50"));
      FAKE_MAX_MATK_MAG = Integer.parseInt(fake.getProperty("MaxMatkMage", "50"));
      FAKE_MAX_MSPD_MAG = Integer.parseInt(fake.getProperty("MaxMspdMage", "50"));
      FAKE_MAX_HP_MAG = Integer.parseInt(fake.getProperty("MaxHpMage", "50"));

      FAKE_MAX_PATK_HEAL = Integer.parseInt(fake.getProperty("MaxPatkHeal", "50"));
      FAKE_MAX_MDEF_HEAL = Integer.parseInt(fake.getProperty("MaxMdefHeal", "50"));
      FAKE_MAX_PSPD_HEAL = Integer.parseInt(fake.getProperty("MaxPspdHeal", "50"));
      FAKE_MAX_PDEF_HEAL = Integer.parseInt(fake.getProperty("MaxPdefHeal", "50"));
      FAKE_MAX_MATK_HEAL = Integer.parseInt(fake.getProperty("MaxMatkHeal", "50"));
      FAKE_MAX_MSPD_HEAL = Integer.parseInt(fake.getProperty("MaxMspdHeal", "50"));
      FAKE_MAX_HP_HEAL = Integer.parseInt(fake.getProperty("MaxHpHeal", "50"));

      ppp = null;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/fakeplayers.cfg File.");
    }
  }

  public static void loadGameGuardCfg() {
    try {
      Properties guard = new Properties();
      InputStream is = new FileInputStream(new File("./config/protection.cfg"));
      guard.load(is);
      is.close();

      GAMEGUARD_ENABLED = Boolean.parseBoolean(guard.getProperty("GameGuardEnable", "False"));
      GAMEGUARD_INTERVAL = (int)TimeUnit.SECONDS.toMillis(Integer.parseInt(guard.getProperty("GameGuardInterval", "60")));

      GAMEGUARD_LOG = Boolean.parseBoolean(guard.getProperty("GameGuardLog", "False"));
      GAMEGUARD_PUNISH = Integer.parseInt(guard.getProperty("GameGuardPunish", "1"));

      GAMEGUARD_KEY = Integer.parseInt("FFAAFF", 16) << 8;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Load ./config/fakeplayers.cfg File.");
    }
  }

  public static void loadHexidCfg() {
    InputStream is = null;
    try {
      Properties Settings = new Properties();
      is = new FileInputStream("./config/hexid.txt");
      Settings.load(is);
      SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
      HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
    } catch (Exception ignored) {
      _log.warning("Could not load HexID file (./config/hexid.txt). Hopefully login will give us one.");
    } finally {
      try {
        if (is != null)
          is.close();
      }
      catch (Exception ignored)
      {
      }
    }
  }

  public static void load(boolean reload) {
    if (Server.serverMode == 1) {
      loadServerCfg();
      loadOptionsCfg();
      loadTelnetCfg();
      loadIdFactoryCfg();
      loadOtherCfg();
      loadEnchantCfg();
      loadServicesCfg();
      loadEventsCfg();
      loadRatesCfg();
      loadAltSettingCfg();
      loadSevenSignsCfg();
      loadClanHallCfg();
      loadNpcCfg();
      loadCustomCfg();
      loadPvpCfg();
      loadAccessLvlCfg();
      loadCommandsCfg();
      loadGeoDataCfg();
      loadFakeCfg();
      loadGameGuardCfg();

      loadHexidCfg();
    }
    else {
      _log.severe("Could not Load Config: server mode was not set");
    }

    if (reload)
      _log.info(TimeLogger.getLogTime() + "Configs: reloaded.");
    else
      _log.info(TimeLogger.getLogTime() + "Configs: loaded.");
  }

  private static void loadChatFilter()
  {
    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./data/chat_filter.txt");
      if (!Data.exists()) { System.out.println("[ERROR] Config, loadChatFilter() '/data/chat_filter.txt' not founded. ");
        return;
      }
      fr = new FileReader(Data);
      br = new BufferedReader(fr);
      lnr = new LineNumberReader(br);
      String line;
      while ((line = lnr.readLine()) != null) {
        if ((line.trim().length() == 0) || (line.startsWith("#")))
        {
          continue;
        }
        CHAT_FILTER_STRINGS.add(line);
      }
    } catch (Exception e1) {
      System.out.println("[ERROR] Config, loadChatFilter() error: " + e);
    } finally {
      try {
        if (fr != null) {
          fr.close();
        }
        if (br != null) {
          br.close();
        }
        if (lnr != null)
          lnr.close();
      }
      catch (Exception e1)
      {
      }
    }
  }

  public static boolean setParameterValue(String pName, String pValue)
  {
    return false;
  }

  public static boolean allowL2Walker(L2PcInstance player)
  {
    return (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.True) || ((ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.GM) && (player != null) && (player.isGM()));
  }

  public static void saveHexid(int serverId, String string)
  {
    saveHexid(serverId, string, "./config/hexid.txt");
  }

  public static void saveHexid(int serverId, String hexId, String fileName)
  {
    OutputStream out = null;
    try {
      Properties hexSetting = new Properties();
      File file = new File(fileName);
      file.createNewFile();

      out = new FileOutputStream(file);
      hexSetting.setProperty("ServerID", String.valueOf(serverId));
      hexSetting.setProperty("HexID", hexId);
      hexSetting.store(out, "the hexID to auth into login");
    } catch (Exception e) {
      _log.warning("Failed to save hex id to " + fileName + " File.");
      e.printStackTrace();
    } finally {
      try {
        out.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public static class PvpColor
  {
    public int nick;
    public int title;

    PvpColor(int nick, int title)
    {
      this.nick = nick;
      this.title = title;
    }
  }

  public static class AltBColor
  {
    public int hex;
    public String color;

    AltBColor(int hex, String color)
    {
      this.hex = hex;
      this.color = color;
    }
  }

  public static class EventReward
  {
    public int id;
    public int count;
    public int chance;

    public EventReward(int id, int count, int chance)
    {
      this.id = id;
      this.count = count;
      this.chance = chance;
    }
  }

  public static enum ObjectSetType
  {
    L2ObjectHashSet, 
    WorldObjectSet;
  }

  public static enum ObjectMapType
  {
    L2ObjectHashMap, 
    WorldObjectMap;
  }

  public static enum IdFactoryType
  {
    Compaction, 
    BitSet, 
    Stack;
  }

  public static enum L2WalkerAllowed
  {
    True, 
    False, 
    GM;
  }
}