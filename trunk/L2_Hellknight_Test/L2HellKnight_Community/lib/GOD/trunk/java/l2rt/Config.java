package l2rt;

import l2rt.gameserver.geodata.PathFindBuffers;
import l2rt.gameserver.loginservercon.AdvIP;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.base.PlayerAccess;
import l2rt.util.GArray;
import l2rt.util.NetList;
import l2rt.util.Strings;
import l2rt.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javolution.util.FastList;

public class Config
{
	protected static Logger _log = Logger.getLogger(Config.class.getName());

	// Включение дебага: java -DenableDebugGsLs
	public static boolean DEBUG_GS_LS = System.getProperty("enableDebugGsLs") != null;

	private static String lastKey;

	public static boolean LOGIN_DEBUG;
	public static boolean COMBO_MODE;
	public static int LOGIN_WATCHDOG_TIMEOUT;
	
	/** Login Protect */
	public static FastList<String> blackIPs = new FastList<String>();
	public static FastList<String> whiteIPs = new FastList<String>();
	public static boolean LOAD_FIREWALL;
	public static boolean AllowCMD;
	public static String CMDLOGIN;
	public static long BAN_CLEAR;
	public static long GARBAGE_CLEAR;
	public static long BAN_TIME;
	public static long INTERVAL;
	public static long BRUTE_BAN;
	public static int MAX_BRUTE;
	
	
	
	/** GS Packets Logger */
	public static boolean LOG_CLIENT_PACKETS;
	public static boolean LOG_SERVER_PACKETS;
	public static int PACKETLOGGER_FLUSH_SIZE;
	public static NetList PACKETLOGGER_IPS;
	public static GArray<String> PACKETLOGGER_ACCOUNTS;
	public static GArray<String> PACKETLOGGER_CHARACTERS;

	/** Game/Login Server ports */
	public static int[] PORTS_GAME;
	public static int PORT_LOGIN;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static String GAMESERVER_HOSTNAME;
	public static boolean ADVIPSYSTEM;
	public static GArray<AdvIP> GAMEIPS = new GArray<AdvIP>();
	public static int IP_UPDATE_TIME;
	public static boolean LOGIN_PING;
	public static int LOGIN_PING_TIME;

	/** AntiFlood for Game/Login */
	public static boolean ANTIFLOOD_ENABLE;
	public static int MAX_UNHANDLED_SOCKETS_PER_IP;
	public static int UNHANDLED_SOCKET_MIN_TTL;

	public static int WEB_SERVER_DELAY;
	public static String WEB_SERVER_ROOT;

	// Database additional options
	public static boolean LAZY_ITEM_UPDATE;
	public static boolean LAZY_ITEM_UPDATE_ALL;
	public static int LAZY_ITEM_UPDATE_TIME;
	public static int LAZY_ITEM_UPDATE_ALL_TIME;
	public static int DELAYED_ITEMS_UPDATE_INTERVAL;
	public static int USER_INFO_INTERVAL;
	public static boolean BROADCAST_STATS_INTERVAL;
	public static int BROADCAST_CHAR_INFO_INTERVAL;
	public static int SAVE_GAME_TIME_INTERVAL;

	public static int MAXIMUM_ONLINE_USERS;
	public static boolean AUTO_CREATE_ACCOUNTS;

	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static boolean SERVER_LIST_TESTSERVER;
	public static boolean SERVER_GMONLY;

	public static boolean CHECK_LANG_FILES_MODIFY;
	public static boolean USE_FILE_CACHE;

	public static int LINEAR_TERRITORY_CELL_SIZE;
	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;

	/** ChatBan */
	public static int CHAT_MESSAGE_MAX_LEN;
	public static int CHAT_MAX_LINES;
	public static int CHAT_LINE_LENGTH;
	public static boolean MAT_BANCHAT;
	public static int[] BAN_CHANNEL_LIST = new int[18];
	public static int MAT_BAN_COUNT_CHANNELS;
	public static boolean MAT_REPLACE;
	public static String MAT_REPLACE_STRING;
	public static int UNCHATBANTIME;
	public static Pattern[] MAT_LIST = {};
	public static boolean MAT_ANNOUNCE;
	public static boolean MAT_ANNOUNCE_FOR_ALL_WORLD;
	public static boolean MAT_ANNOUNCE_NICK;

	public static int ALT_ADD_RECIPES;
	public static boolean ALT_100_RECIPES_B;
	public static boolean ALT_100_RECIPES_A;
	public static boolean ALT_100_RECIPES_S;
	public static boolean ALT_100_RECIPES_S80;

	public static int ALT_MAX_ALLY_SIZE;

	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static float[] ALT_PARTY_BONUS;

	public static boolean ALT_VITALITY_ENABLED;
	public static float ALT_VITALITY_POWER_MULT;
	public static float ALT_VITALITY_CONSUMPTION;
	public static int ALT_VITALITY_RAID_BONUS;

	/** Count kills? */
	public static boolean KILL_COUNTER;

	/** Count dropped? */
	public static boolean DROP_COUNTER;

	/** Count crafted? */
	public static boolean CRAFT_COUNTER;
	public static double CRAFT_MASTERWORK_CHANCE;
	public static double CRAFT_MASTERWORK_LEVEL_MOD;
	public static boolean CRAFT_MASTERWORK_CHEST;

	/** Thread pools size */
	public static int THREAD_P_GENERAL;
	public static int THREAD_P_MOVE;
	public static int THREAD_P_PATHFIND;
	public static int NPC_AI_MAX_THREAD;
	public static int PLAYER_AI_MAX_THREAD;
	public static int INTEREST_MAX_THREAD;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int URGENT_PACKET_THREAD_CORE_SIZE;
	public static int SELECTOR_SLEEP_TIME;
	public static boolean INTEREST_ALT;
	public static boolean MULTI_THREADED_IDFACTORY_EXTRACTOR;
	public static boolean MULTI_THREADED_IDFACTORY_CLEANER;

	public static int DEADLOCKCHECK_INTERVAL;
	public static int GARBAGE_COLLECTOR_INTERVAL;

	public static boolean AUTOSAVE;

	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_INDIVIDUAL;
	public static boolean AUTO_LOOT_FROM_RAIDS;

	/** Auto-loot for/from players with karma also? */
	public static boolean AUTO_LOOT_PK;

	/** Auto-loot for/from players with PA? */
	public static boolean AUTO_LOOT_PA;
	
	/** Account name template */
	public static String ANAME_TEMPLATE;

	/** Account password template */
	public static String APASSWD_TEMPLATE;

	/** Character name template */
	public static String CNAME_TEMPLATE;

	public static int CNAME_MAXLEN = 32;

	/** Clan name template */
	public static String CLAN_NAME_TEMPLATE;

	/** Clan title template */
	public static String CLAN_TITLE_TEMPLATE;

	/** Ally name template */
	public static String ALLY_NAME_TEMPLATE;

	/** Global chat state */
	public static int GLOBAL_CHAT;
	public static int GLOBAL_TRADE_CHAT;
	public static int SHOUT_CHAT_MODE;
	public static int TRADE_CHAT_MODE;

	public static Pattern[] TRADE_WORDS;
	public static boolean TRADE_CHATS_REPLACE_FROM_ALL;
	public static boolean TRADE_CHATS_REPLACE_FROM_SHOUT;

	/** For test servers - evrybody has admin rights */
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean ALLOW_SPECIAL_COMMANDS;

	/** For small servers */
	public static boolean ALT_GAME_MATHERIALSDROP;

	/** Все мобы не являющиеся рейдами спавнятся в двойном количестве */
	public static boolean ALT_DOUBLE_SPAWN;

	public static float ALT_RAID_RESPAWN_MULTIPLIER;

	public static boolean ALT_ALLOW_AUGMENT_ALL;
	public static boolean ALT_ALLOW_DROP_AUGMENTED;

	/** Give exp and sp for craft */
	public static boolean ALT_GAME_EXP_FOR_CRAFT;

	public static boolean ALT_GAME_UNREGISTER_RECIPE;

	/** Delay for announce SS period (in minutes) */
	public static int SS_ANNOUNCE_PERIOD;

	/** Show mob stats/droplist to players? */
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALT_GAME_GEN_DROPLIST_ON_DEMAND;
	public static boolean ALT_FULL_NPC_STATS_PAGE;
	public static boolean ALLOW_NPC_SHIFTCLICK;

	/** Hardcore configs */
	public static boolean ALT_ALLOW_DROP_COMMON;
	public static boolean ALT_ALLOW_SELL_COMMON;
	public static boolean ALT_ALLOW_SHADOW_WEAPONS;
	public static int[] ALT_DISABLED_MULTISELL;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_UNALLOWED_ITEMS;

	/* Show html window at login */
	public static boolean SHOW_HTML_WELCOME;

	/** Титул при создании чара */
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;

	/** Таймаут на использование social action */
	public static boolean ALT_SOCIAL_ACTION_REUSE;

	/** Отключение книг для изучения скилов */
	public static boolean ALT_DISABLE_SPELLBOOKS;

	public static int RELATION;

	/** Разрешать ли на арене бои за опыт */
	public static boolean ALT_ARENA_EXP;

	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_MAX_LEVEL;
	public static int ALT_MAX_SUB_LEVEL;
	public static byte SUBCLASS_INIT_LEVEL;
	public static int ALT_GAME_SUB_ADD;
	public static boolean ALT_NO_LASTHIT;
	public static String ALT_KAMALOKA_LIMITS;
	public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
	public static boolean ALT_KAMALOKA_NIGHTMARE_REENTER;
	public static boolean ALT_KAMALOKA_ABYSS_REENTER;
	public static boolean ALT_KAMALOKA_LAB_REENTER;
	public static boolean ALT_DONT_ALLOW_PETS_ON_SIEGE;
	public static boolean ALT_PET_HEAL_BATTLE_ONLY;

	public static boolean ALT_SIMPLE_SIGNS;
	public static boolean ALT_TELE_TO_CATACOMBS;
	public static boolean ALT_BS_CRYSTALLIZE;
	public static int ALT_MAMMON_EXCHANGE;
	public static int ALT_MAMMON_UPGRADE;
	public static boolean ALT_ALLOW_TATTOO;

	public static int MULTISELL_SIZE;

	public static int SERVICES_CHANGE_NICK_PRICE;
	public static int SERVICES_CHANGE_NICK_ITEM;

	public static boolean SERVICES_CHANGE_CLAN_NAME_ENABLED;
	public static int SERVICES_CHANGE_CLAN_NAME_PRICE;
	public static int SERVICES_CHANGE_CLAN_NAME_ITEM;

	public static boolean SERVICES_CHANGE_PET_NAME_ENABLED;
	public static int SERVICES_CHANGE_PET_NAME_PRICE;
	public static int SERVICES_CHANGE_PET_NAME_ITEM;

	public static boolean SERVICES_EXCHANGE_BABY_PET_ENABLED;
	public static int SERVICES_EXCHANGE_BABY_PET_PRICE;
	public static int SERVICES_EXCHANGE_BABY_PET_ITEM;

	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;

	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;

	public static int SERVICES_SEPARATE_SUB_PRICE;
	public static int SERVICES_SEPARATE_SUB_ITEM;

	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;

	public static boolean SERVICES_BASH_ENABLED;
	public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
	public static int SERVICES_BASH_RELOAD_TIME;

	public static boolean SERVICES_RATE_BONUS_ENABLED;
	public static boolean SERVICES_BONUS_APPLY_RATES_THEN_SERVICE_DISABLED;
	public static int[] SERVICES_RATE_BONUS_PRICE;
	public static int[] SERVICES_RATE_BONUS_ITEM;
	public static float[] SERVICES_RATE_BONUS_VALUE;
	public static int[] SERVICES_RATE_BONUS_DAYS;
	public static float SERVICES_RATE_BONUS_LUCK_EFFECT;

	public static boolean SERVICES_NOBLESS_TW_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;

	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static Integer SERVICES_EXPAND_INVENTORY_MAX;

	public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
	public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
	public static int SERVICES_EXPAND_WAREHOUSE_ITEM;

	public static boolean SERVICES_EXPAND_CWH_ENABLED;
	public static int SERVICES_EXPAND_CWH_PRICE;
	public static int SERVICES_EXPAND_CWH_ITEM;

	public static boolean SERVICES_WINDOW_ENABLED;
	public static int SERVICES_WINDOW_PRICE;
	public static int SERVICES_WINDOW_ITEM;
	public static int SERVICES_WINDOW_DAYS;
	public static int SERVICES_WINDOW_MAX;

	public static boolean SERVICES_HOW_TO_GET_COL;

	public static String SERVICES_SELLPETS;

	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static boolean SERVICES_OFFLINE_TRADE_KICK_NOT_TRADING;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
	public static boolean SERVICES_PARNASSUS_ENABLED;
	public static boolean SERVICES_PARNASSUS_NOTAX;
	public static long SERVICES_PARNASSUS_PRICE;
	public static boolean SERVICES_LOCK_ACCOUNT_IP;
	public static boolean SERVICES_CHANGE_PASSWORD;

	public static boolean SERVICES_ALLOW_LOTTERY;
	public static int SERVICES_LOTTERY_PRIZE;
	public static int SERVICES_ALT_LOTTERY_PRICE;
	public static int SERVICES_LOTTERY_TICKET_PRICE;
	public static float SERVICES_LOTTERY_5_NUMBER_RATE;
	public static float SERVICES_LOTTERY_4_NUMBER_RATE;
	public static float SERVICES_LOTTERY_3_NUMBER_RATE;
	public static int SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;

	public static boolean SERVICES_ALLOW_ROULETTE;
	public static long SERVICES_ROULETTE_MIN_BET;
	public static long SERVICES_ROULETTE_MAX_BET;

	public static boolean SERVICES_REFERRAL_ENABLED;
	public static int SERVICES_REFERRAL_ITEM_1;
	public static long SERVICES_REFERRAL_COUNT_1;
	public static int SERVICES_REFERRAL_ITEM_2;
	public static long SERVICES_REFERRAL_COUNT_2;
	
	public static int ITEM_MOLL_ID_1;
	public static int ITEM_MOLL_KOL_1;
	public static int ITEM_MOLL_ID_2;
	public static int ITEM_MOLL_KOL_2;
	public static int ITEM_MOLL_ID_3;
	public static int ITEM_MOLL_KOL_3;
	
	public static boolean ALT_SIEGE_MOD;
	
	public static String[] DT_OF_SIEGE_ADEN;
	public static String[] DT_OF_SIEGE_RUNE;
	public static String[] DT_OF_SIEGE_SCHUTTGART;
	public static String[] DT_OF_SIEGE_GODDARD;
	public static String[] DT_OF_SIEGE_GIRAN;
	public static String[] DT_OF_SIEGE_OREN;
	public static String[] DT_OF_SIEGE_GLUDIO;
	public static String[] DT_OF_SIEGE_DION;
	public static String[] DT_OF_SIEGE_INNADRIL;

	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_ALLOW_ADENA_DAWN;

	/** Olympiad Compitition Starting time */
	public static int ALT_OLY_START_TIME;
	/** Olympiad Compition Min */
	public static int ALT_OLY_MIN;
	/** Olympaid Comptetition Period */
	public static long ALT_OLY_CPERIOD;
	/** Olympaid Weekly Period */
	public static long ALT_OLY_WPERIOD;
	/** Olympaid Validation Period */
	public static long ALT_OLY_VPERIOD;

	public static boolean ENABLE_OLYMPIAD;
	public static boolean ENABLE_OLYMPIAD_SPECTATING;
	public static int CLASS_GAME_MIN;
	public static int NONCLASS_GAME_MIN;
	public static int RANDOM_TEAM_GAME_MIN;
	public static int TEAM_GAME_MIN;

	public static int ALT_OLY_REG_DISPLAY;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_RANDOM_TEAM_RITEM_C;
	public static int ALT_OLY_TEAM_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;

	public static int ALT_TRUE_CHESTS;

	public static long NONOWNER_ITEM_PICKUP_DELAY;

	/** Logging Chat Window */
	public static boolean LOG_CHAT;
	public static String LOG_CHAT_DB;
	public static boolean LOG_KILLS;
	public static boolean LOG_TELNET;
	public static boolean SQL_LOG;

	public static HashMap<Integer, PlayerAccess> gmlist = new HashMap<Integer, PlayerAccess>();

	/** Rate control */
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_QUESTS_REWARD;
	public static float RATE_QUESTS_DROP;
	public static float RATE_QUESTS_DROP_PROF;
	public static boolean RATE_QUESTS_OCCUPATION_CHANGE;
	public static float RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static float RATE_DROP_ADENA;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_COMMON_ITEMS;
	public static float RATE_DROP_RAIDBOSS;
	public static float RATE_DROP_SPOIL;
	public static float RATE_DROP_ADENA_STATIC_MOD;
	public static float RATE_DROP_ADENA_MULT_MOD;
	public static int RATE_BREAKPOINT;
	public static int MAX_DROP_ITERATIONS;
	public static double RATE_MANOR;
	public static float RATE_FISH_DROP_COUNT;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static boolean RATE_PARTY_MIN;

	/** Player Drop Rate control */
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_NEEDED_TO_DROP;

	public static int KARMA_DROP_ITEM_LIMIT;

	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;

	public static float KARMA_DROPCHANCE_BASE;
	public static float KARMA_DROPCHANCE_MOD;
	public static float NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;

	public static int AUTODESTROY_ITEM_AFTER;
	public static int AUTODESTROY_PLAYER_ITEM_AFTER;

	public static int DELETE_DAYS;

	public static int PURGE_BYPASS_TASK_FREQUENCY;

	/** Datapack root directory */
	public static File DATAPACK_ROOT;

	public static String AttributeBonusFile;

	public static boolean WEAR_TEST_ENABLED;

	public static float MAXLOAD_MODIFIER;
	public static float GATEKEEPER_MODIFIER;
	public static int ALT_BUFF_MIN_LEVEL;
	public static int ALT_BUFF_MAX_LEVEL;
	public static boolean ALT_IMPROVED_PETS_LIMITED_USE;
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;
	public static int ALT_CATACOMB_MODIFIER_HP;
	public static float ALT_CATACOMB_RESPAWN;
	public static boolean ALT_SAVE_SPAWN;
	public static double ALT_CHAMPION_CHANCE1;
	public static double ALT_CHAMPION_CHANCE2;
	public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
	public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;

	public static int L2WALKER_PUNISHMENT;
	public static int BUGUSER_PUNISH;

	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_FREIGHT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean DROP_CURSED_WEAPONS_ON_KICK;
	public static boolean ALLOW_NOBLE_TP_TO_ALL;

	/** Pets */
	public static int SWIMING_SPEED;

	/** protocol revision */
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;

	/** random animation interval */
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;

	public static String DEFAULT_LANG;
	public static boolean USE_CLIENT_LANG;
	public static boolean ENG_QUEST_NAMES;

	/** Время регулярного рестарта (через определенное время) */
	public static int RESTART_TIME;
	/** Время запланированного на определенное время суток рестарта */
	public static int RESTART_AT_TIME;
	public static int LRESTART_TIME;

	/** Configuration files */
	public static final String L2WT_FILE = "./config/L2-WT.ini";
	public static final String OTHER_CONFIG_FILE = "./config/other.ini";
	public static final String COMMUNITY_CONFIG_FILE = "./config/communityboard.ini";
	public static final String RESIDENCE_CONFIG_FILE = "./config/residence.ini";
	public static final String SPOIL_CONFIG_FILE = "./config/spoil.ini";
	public static final String ALT_SETTINGS_FILE = "./config/altsettings.ini";
	public static final String PVP_CONFIG_FILE = "./config/pvp.ini";
	public static final String GM_PERSONAL_ACCESS_FILE = "./config/GMAccess.xml";
	public static final String GM_ACCESS_FILES_DIR = "./config/GMAccess.d/";
	public static final String TELNET_FILE = "./config/telnet.ini";
	public static final String LOGIN_TELNET_FILE = "./config/login_telnet.ini";
	public static final String CONFIGURATION_FILE = "./config/server.ini";
	public static final String LOGIN_CONFIGURATION_FILE = "./config/loginserver.ini";
	public static final String LOGIN_PROTECT_FILE = "./config/protection_login.ini";
	public static final String BLACKIP_FILE = "./config/ips_black.txt";
	public static final String WHITEIP_FILE = "./config/ips_white.txt";
	public static final String VERSION_FILE = "l2rt-version.ini";
	public static final String SIEGE_CASTLE_CONFIGURATION_FILE = "./config/siege_castle.ini";
	public static final String SIEGE_FORTRESS_CONFIGURATION_FILE = "./config/siege_fortress.ini";
	public static final String SIEGE_CLANHALL_CONFIGURATION_FILE = "./config/siege_clanhall.ini";
	public static final String SIEGE_TERRITORY_CONFIGURATION_FILE = "./config/siege_territory.ini";
	public static final String BANNED_IP_XML = "./config/banned.xml";
	public static final String HEXID_FILE = "./config/hexid.txt";
	public static final String MAT_CONFIG_FILE = "./config/mats.cfg";
	public static final String ADV_IP_FILE = "./config/advipsystem.ini";
	public static final String AI_CONFIG_FILE = "./config/ai.ini";
	public static final String GEODATA_CONFIG_FILE = "./config/geodata.ini";
	public static final String EVENTS = "./config/events.ini";
	public static final String FAKE_PLAYERS_LIST = "./config/fake_players.list";
	public static final String SERVICES_FILE = "./config/services.ini";
	public static final String PROTECT_FILE = "./config/protection.ini";
	public static final String OLYMPIAD = "./config/olympiad.ini";
	public static final String EVENT_PC_BANG_POINT_FILE = "./config/pcBang.ini";

	/** DRiN's Protection config */
	public static boolean PROTECT_ENABLE;
	public static NetList PROTECT_UNPROTECTED_IPS;
	public static boolean PROTECT_GS_STORE_HWID;
	public static boolean PROTECT_GS_LOG_HWID;
	public static String PROTECT_GS_LOG_HWID_QUERY;
	public static boolean PROTECT_GS_ENABLE_HWID_BANS;
	public static boolean PROTECT_GS_ENABLE_HWID_BONUS;
	public static int PROTECT_GS_MAX_SAME_HWIDs, PROTECT_COMPRESSION, PROTECT_COMPRESSION_MINSIZE,
			PROTECT_COMPRESSION_SPLITSIZE, PROTECT_COMPRESSION_WRITEDELAY;
	public static String HWID_BANS_TABLE;

	public static String LOGIN_HOST;
	public static int GAME_SERVER_LOGIN_PORT;
	public static boolean GAME_SERVER_LOGIN_CRYPT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;

	public static String DEFAULT_PASSWORD_ENCODING;
	public static String LEGACY_PASSWORD_ENCODING;
	public static String DOUBLE_WHIRPOOL_SALT;
	public static int LOGIN_BLOWFISH_KEYS;
	public static int LOGIN_RSA_KEYPAIRS;

	public static boolean SERVER_SIDE_NPC_NAME;
	public static boolean SERVER_SIDE_NPC_TITLE;
	public static boolean SERVER_SIDE_NPC_TITLE_WITH_LVL;
	public static String CLASS_MASTERS_PRICE;
	public static int CLASS_MASTERS_PRICE_ITEM;
	public static GArray<Integer> ALLOW_CLASS_MASTERS_LIST = new GArray<Integer>();
	public static int[] CLASS_MASTERS_PRICE_LIST = new int[4];
	public static boolean ALLOW_SHORT_2ND_PROF_QUEST;

	public static String SERVER_VERSION;
	public static String SERVER_BUILD_DATE;
	public final static String SERVER_VERSION_UNSUPPORTED = "Unknown Version";

	/** Inventory slots limits */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;

	/** Warehouse slots limits */
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;

	/** Spoil Rates */
	public static float BASE_SPOIL_RATE;
	public static float MINIMUM_SPOIL_RATE;
	public static boolean ALT_SPOIL_FORMULA;

	/** Manor Config */
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static double MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static double MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;

	/** Karma System Variables */
	public static int KARMA_MIN_KARMA;
	public static int KARMA_SP_DIVIDER;
	public static int KARMA_LOST_BASE;

	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static boolean DROP_ITEMS_AUGMENTED;

	public static String KARMA_NONDROPPABLE_ITEMS;
	public static GArray<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new GArray<Integer>();

	public static int PVP_TIME;

	/** Karma Punishment */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;

	public static boolean HARD_DB_CLEANUP_ON_START;

	/** Chance that an item will succesfully be enchanted */
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_ACCESSORY;
	public static int ENCHANT_CHANCE_CRYSTAL_WEAPON;
	public static int ENCHANT_CHANCE_CRYSTAL_ARMOR;
	public static int ENCHANT_CHANCE_CRYSTAL_ACCESSORY;
	public static byte ENCHANT_MAX;
	public static int ENCHANT_ATTRIBUTE_STONE_CHANCE;
	public static int ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE;
	public static int ARMOR_OVERENCHANT_HPBONUS_LIMIT;

	public static boolean REGEN_SIT_WAIT;

	public static long TIMEOUT_CHECKER_CLIENT;

	public static float RATE_RAID_REGEN;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean PARALIZE_ON_RAID_DIFF;

	public static int STARTING_ADENA;

	/** Deep Blue Mobs' Drop Rules Enabled */
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;

	/** telnet enabled */
	public static boolean IS_TELNET_ENABLED;
	public static boolean IS_LOGIN_TELNET_ENABLED;
	public static String TELNET_DEFAULT_ENCODING;

	/** Percent CP is restore on respawn */
	public static double RESPAWN_RESTORE_CP;
	/** Percent HP is restore on respawn */
	public static double RESPAWN_RESTORE_HP;
	/** Percent MP is restore on respawn */
	public static double RESPAWN_RESTORE_MP;

	/** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	/** Maximum number of available slots for pvt stores (sell/buy) - Others */
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static int MAX_PVTCRAFT_SLOTS;

	public static boolean SENDSTATUS_TRADE_JUST_OFFLINE;

	public static boolean ALLOW_CH_DOOR_OPEN_ON_CLICK;
	public static boolean ALT_CH_ALL_BUFFS;
	public static boolean ALT_CH_ALLOW_1H_BUFFS;
	public static boolean ALT_CH_SIMPLE_DIALOG;

	public static int CH_BID_GRADE1_MINCLANLEVEL;
	public static int CH_BID_GRADE1_MINCLANMEMBERS;
	public static int CH_BID_GRADE1_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE2_MINCLANLEVEL;
	public static int CH_BID_GRADE2_MINCLANMEMBERS;
	public static int CH_BID_GRADE2_MINCLANMEMBERSLEVEL;
	public static int CH_BID_GRADE3_MINCLANLEVEL;
	public static int CH_BID_GRADE3_MINCLANMEMBERS;
	public static int CH_BID_GRADE3_MINCLANMEMBERSLEVEL;
	public static double RESIDENCE_LEASE_FUNC_MULTIPLIER;
	public static double RESIDENCE_LEASE_MULTIPLIER;

	/** Show licence or not just after login (if false, will directly go to the Server List */
	public static boolean SHOW_LICENCE;

	/** Deafult punishment for illegal actions */
	public static int DEFAULT_PUNISH;

	public static boolean ACCEPT_NEW_GAMESERVER;
	public static byte[] HEX_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;

	public static boolean ANNOUNCE_MAMMON_SPAWN;

	public static int GM_NAME_COLOUR;
	public static boolean GM_HERO_AURA;
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;

	/** AI */
	public static int AI_TASK_DELAY;
	public static int AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean SAY_CASTING_SKILL_NAME;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;

	public static int AGGRO_CHECK_INTERVAL;

	/** Maximum range mobs can randomly go from spawn point */
	public static int MAX_DRIFT_RANGE;

	/** Maximum range mobs can pursue agressor from spawn point */
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;

	public static boolean ALT_AI_KELTIRS;
	public static boolean ALT_TELEPORTING_TOMA;

	/** It's EVIL :) */
	public static boolean MOBSLOOTERS;

	public static int MOBS_WEAPON_ENCHANT_MIN;
	public static int MOBS_WEAPON_ENCHANT_MAX;
	public static int MOBS_WEAPON_ENCHANT_CHANCE;

	public static boolean RRD_ENABLED;
	public static boolean RRD_EXTENDED;
	public static String RRD_PATH;
	public static String RRD_EXT_PATH;
	public static String RRD_GRAPH_PATH;
	public static String RRD_AREA_COLOR;
	public static String RRD_LINE_COLOR;
	public static long RRD_UPDATE_TIME;
	public static int RRD_GRAPH_HEIGHT;
	public static int RRD_GRAPH_WIDTH;
	public static float RRD_LINE_WIDTH;

	public static boolean HIDE_GM_STATUS;
	public static boolean SHOW_GM_LOGIN;
	public static boolean SAVE_GM_EFFECTS; //Silence, gmspeed, etc...

	/** security options */
	public static GArray<Integer> DISABLE_CREATION_ID_LIST = new GArray<Integer>();
	public static GArray<Integer> LOG_MULTISELL_ID_LIST = new GArray<Integer>();

	public static boolean ENABLE_FISHING_CHECK_WATER;

	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;

	public static boolean DAMAGE_FROM_FALLING;

	/** Community Board */
	public static boolean ALLOW_COMMUNITYBOARD;
	public static String BBS_DEFAULT;
	public static String COMMUNITYBOARD_HTML_ROOT;
	public static boolean COMMUNITYBOARD_SORTPLAYERSLIST;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static long COMMUNITYBOARD_PLAYERSLIST_CACHE;
	public static String ALLOW_COMMUNITYBOARD_PLAYERSLIST;

	/** Wedding Options */
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_INTERVAL;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;

	public static boolean FORCE_STATUSUPDATE;

	public static long PLAYER_DISCONNECT_INGAME_TIME;
	public static long PLAYER_LOGOUT_INGAME_TIME;

	/** Castle siege options **/
	public static boolean SIEGE_OPERATE_DOORS;
	public static boolean SIEGE_OPERATE_DOORS_LORD_ONLY;

	/** Augmentations **/
	public static int AUGMENTATION_NG_SKILL_CHANCE; // Chance to get a skill while using a NoGrade Life Stone
	public static int AUGMENTATION_NG_GLOW_CHANCE; // Chance to get a Glow effect while using a NoGrade Life Stone(only if you get a skill)
	public static int AUGMENTATION_MID_SKILL_CHANCE; // Chance to get a skill while using a MidGrade Life Stone
	public static int AUGMENTATION_MID_GLOW_CHANCE; // Chance to get a Glow effect while using a MidGrade Life Stone(only if you get a skill)
	public static int AUGMENTATION_HIGH_SKILL_CHANCE; // Chance to get a skill while using a HighGrade Life Stone
	public static int AUGMENTATION_HIGH_GLOW_CHANCE; // Chance to get a Glow effect while using a HighGrade Life Stone
	public static int AUGMENTATION_TOP_SKILL_CHANCE; // Chance to get a skill while using a TopGrade Life Stone
	public static int AUGMENTATION_TOP_GLOW_CHANCE; // Chance to get a Glow effect while using a TopGrade Life Stone
	public static int AUGMENTATION_BASESTAT_CHANCE; // Chance to get a BaseStatModifier in the augmentation process
	public static int AUGMENTATION_ACC_SKILL_CHANCE;

	/** Custom Westeros settings **/	
	public static boolean REVIVAL_POINT_IN_GIRAN;

	public static int FOLLOW_RANGE;

	/** Enchant Config **/
	public static int SAFE_ENCHANT_COMMON;
	public static int SAFE_ENCHANT_FULL_BODY;

	public static int FESTIVAL_MIN_PARTY_SIZE;
	public static float FESTIVAL_RATE_PRICE;

	/** Dimensional Rift Config **/
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY; // Time in ms the party has to wait until the mobs spawn
	public static int RIFT_AUTO_JUMPS_TIME;
	public static int RIFT_AUTO_JUMPS_TIME_RAND;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;

	/** Some more LS Settings */
	public static GArray<String> INTERNAL_IP = null;
	/** Продвинутый список локальных сетей / ip-адресов */
	public static NetList INTERNAL_NETLIST = null;

	public static boolean ALLOW_TALK_WHILE_SITTING;

	public static boolean ALLOW_FAKE_PLAYERS;
	public static int FAKE_PLAYERS_PERCENT;

	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;

	public static boolean LOGIN_GG_CHECK;

	/** Конфиг на включение защиты от брута паролей. **/
	public static boolean FAKE_LOGIN;

	public static boolean GG_CHECK;

	/** Allow Manor system */
	public static boolean ALLOW_MANOR;

	/** Manor Refresh Starting time */
	public static int MANOR_REFRESH_TIME;

	/** Manor Refresh Min */
	public static int MANOR_REFRESH_MIN;

	/** Manor Next Period Approve Starting time */
	public static int MANOR_APPROVE_TIME;

	/** Manor Next Period Approve Min */
	public static int MANOR_APPROVE_MIN;

	/** Manor Maintenance Time */
	public static int MANOR_MAINTENANCE_PERIOD;

	/** Manor Save All Actions */
	public static boolean MANOR_SAVE_ALL_ACTIONS;

	/** Manor Save Period Rate */
	public static int MANOR_SAVE_PERIOD_RATE;

	public static String SNAPSHOTS_DIRECTORY;
	public static boolean DUMP_MEMORY_ON_SHUTDOWN;

	/** NPC Buffer config */
	public static int SERVICES_BUFFER_MIN_LVL;
	public static int SERVICES_BUFFER_MAX_LVL;
	public static int SERVICES_BUFFER_PRICE;
	//public static boolean SERVICES_BUFFER_SIEGE;
	public static boolean SERVICES_BUFFER_ENABLED;
	public static boolean SERVICES_BUFFER_PET_ENABLED;

	public static float EVENT_CofferOfShadowsPriceRate;
	public static float EVENT_CofferOfShadowsRewardRate;

	public static float EVENT_RabbitsToRichesRewardRate;
	public static float EVENT_TREASURE_SACK_CHANCE;

	/** Bonus event **/
	public static int SERVICES_RATE_SPECIAL_ITEM_ID;
	public static int SERVICES_RATE_SPECIAL_ITEM_COUNT;
	public static int SERVICES_RATE_SPECIAL_RATE;
	public static int SERVICES_RATE_SPECIAL_DAYS;
	public static boolean SERVICES_RATE_SPECIAL_ENABLED;
	public static int  SERVICES_RATE_CREATE_CHARACTER_PA;
	public static int  SERVICES_RATE_CREATE_CHARACTER_PA_VALUE;
	
	public static int EVENT_LastHeroItemID;
	public static float EVENT_LastHeroItemCOUNT;
	public static int EVENT_LastHeroTime;
	public static boolean EVENT_LastHeroRate;
	public static float EVENT_LastHeroItemCOUNTFinal;
	public static boolean EVENT_LastHeroRateFinal;
	public static int EVENT_LastHeroChanceToStart;

	public static int EVENT_TvTItemID;
	public static float EVENT_TvTItemCOUNT;
	public static int EVENT_TvTTime;
	public static boolean EVENT_TvT_rate;
	public static int EVENT_TvTChanceToStart;

	public static int EVENT_CtFItemID;
	public static float EVENT_CtFItemCOUNT;
	public static int EVENT_CtFTime;
	public static boolean EVENT_CtF_rate;
	public static int EVENT_CtFChanceToStart;

	public static float EVENT_TFH_POLLEN_CHANCE;
	public static float EVENT_GLITTMEDAL_NORMAL_CHANCE;
	public static float EVENT_GLITTMEDAL_GLIT_CHANCE;
	public static float EVENT_L2DAY_LETTER_CHANCE;
	public static float EVENT_CHANGE_OF_HEART_CHANCE;

	public static float EVENT_MARCH8_DROP_CHANCE;
	public static float EVENT_MARCH8_PRICE_RATE;

	public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;

	public static long EVENT_SAVING_SNOWMAN_LOTERY_PRICE;
	public static int EVENT_SAVING_SNOWMAN_REWARDER_CHANCE;

	/** Master Of Enchanting **/
	public static float ENCHANT_MASTER_DROP_CHANCE;
	public static boolean EVENT_MASTEROFENCHANTING_USE_RATES;
	public static long ENCHANT_MASTER_STAFF_PRICE;
	public static long ENCHANT_MASTER_24SCROLL_PRICE;
	public static long ENCHANT_MASTER_1SCROLL_PRICE;
	public static int ENCHANT_MASTER_PRICE_ID;

	/** Trick Of Transmutation **/
	public static float EVENT_TRICK_OF_TRANS_CHANCE;

	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	public static float SERVICES_TRADE_TAX;
	public static float SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;

	public static int SHUTDOWN_MSG_TYPE;

	/** Geodata config */

	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static boolean GEODATA_DEBUG;
	public static boolean PATHFIND_DEBUG;
	public static String GEOFILES_PATTERN;
	public static boolean ALLOW_DOORS;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static boolean LOAD_MULTITHREADED;
	public static int CLIENT_Z_SHIFT;

	public static int VIEW_OFFSET;
	public static int DIV_BY;
	public static int DIV_BY_FOR_Z;
	public static String VERTICAL_SPLIT_REGIONS;

	/** Geodata (Pathfind) config */

	public static boolean SIMPLE_PATHFIND_FOR_MOBS;
	public static int PATHFIND_BOOST;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	public static double WEIGHT0;
	public static double WEIGHT1;
	public static double WEIGHT2;

	public static boolean DELAYED_SPAWN;
	
	/** Settings of PcBang Points */ 
	public static boolean BANG_POINT_ENABLE; 
	public static int BANG_POINT_MIN_LEVEL; 
	public static int BANG_POINT_MIN_COUNT; 
	public static int BANG_POINT_MAX_COUNT; 
	public static int BANG_POINT_DUAL_CHANCE; 
	public static int BANG_POINT_TIME_STAMP; 
	public static float BANG_POINT_RATE; 
	public static boolean BANG_POINT_DOUBLE_ENABLE; 
	public static boolean BANG_RANDOM_POINT_ENABLE;

	// L2-WT
	public static boolean ANNOUNCE_BAN_CHAT;
	public static boolean SELL_FREE_ADENA;
	
	public static boolean SECOND_AUTH_ENABLED;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static int SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;
	
	private static void initProtection(boolean gs)
	{
		File fp = new File(PROTECT_FILE);
		PROTECT_ENABLE = fp.exists();
		if(PROTECT_ENABLE)
			try
			{
				Properties protectSettings = loadPropertiesFile(fp);

				PROTECT_UNPROTECTED_IPS = new NetList();

				String _ips = getProperty(protectSettings, "UpProtectedIPs", "");
				if(_ips.equals(""))
					PROTECT_UNPROTECTED_IPS.LoadFromString(_ips, ",");

				if(gs)
				{
					PROTECT_GS_STORE_HWID = getBooleanProperty(protectSettings, "StoreHWID", false);
					PROTECT_GS_LOG_HWID = getBooleanProperty(protectSettings, "LogHWIDs", false);
					int LogHWIDsID = getIntProperty(protectSettings, "LogHWIDsID", -1);
					PROTECT_GS_LOG_HWID_QUERY = "INSERT INTO " + getProperty(protectSettings, "LogHWIDsPath", "hwids_log");
					if(LogHWIDsID == -1)
						PROTECT_GS_LOG_HWID_QUERY += " (Account,IP,HWID) VALUES (?,?,?);";
					else
						PROTECT_GS_LOG_HWID_QUERY += " (Account,IP,HWID,ServerID) VALUES (?,?,?," + LogHWIDsID + ");";
					HWID_BANS_TABLE = getProperty(protectSettings, "BanHWIDsPath", "hwid_bans");
					PROTECT_GS_ENABLE_HWID_BANS = getBooleanProperty(protectSettings, "EnableHWIDBans", false);
					PROTECT_GS_ENABLE_HWID_BONUS = getBooleanProperty(protectSettings, "EnableHWIDBonus", false);
					PROTECT_GS_MAX_SAME_HWIDs = getIntProperty(protectSettings, "MaxSameHWIDs", 0);
					PROTECT_COMPRESSION = getIntProperty(protectSettings, "Compression", 0);
					PROTECT_COMPRESSION_MINSIZE = getIntProperty(protectSettings, "CompressionMinSize", 512);
					PROTECT_COMPRESSION_SPLITSIZE = getIntProperty(protectSettings, "CompressionSplitSize", 0x4000);
					PROTECT_COMPRESSION_WRITEDELAY = getIntProperty(protectSettings, "CompressionWriteDelay", 2);
				}

			}
			catch(Exception e)
			{}
	}

	private static void loadAntiFlood(Properties _settings)
	{
		try
		{
			ANTIFLOOD_ENABLE = getBooleanProperty(_settings, "AntiFloodEnable", false);
			if(!ANTIFLOOD_ENABLE)
				return;
			MAX_UNHANDLED_SOCKETS_PER_IP = getIntProperty(_settings, "MaxUnhandledSocketsPerIP", 5);
			UNHANDLED_SOCKET_MIN_TTL = getIntProperty(_settings, "UnhandledSocketsMinTTL", 5000);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load AntiFlood Properties. [" + lastKey + "]");
		}
	}

	public static void reloadPacketLoggerConfig()
	{
		try
		{
			Properties serverSettings = loadPropertiesFile(CONFIGURATION_FILE);

			LOG_CLIENT_PACKETS = getBooleanProperty(serverSettings, "LogClientPackets", false);
			LOG_SERVER_PACKETS = getBooleanProperty(serverSettings, "LogServerPackets", false);
			PACKETLOGGER_FLUSH_SIZE = getIntProperty(serverSettings, "LogPacketsFlushSize", 8192);

			String temp = getProperty(serverSettings, "LogPacketsFromIPs", "").trim();
			if(temp.isEmpty())
				PACKETLOGGER_IPS = null;
			else
			{
				PACKETLOGGER_IPS = new NetList();
				PACKETLOGGER_IPS.LoadFromString(temp, ",");
			}

			temp = getProperty(serverSettings, "LogPacketsFromAccounts", "").trim();
			if(temp.isEmpty())
				PACKETLOGGER_ACCOUNTS = null;
			else
			{
				PACKETLOGGER_ACCOUNTS = new GArray<String>();
				for(String s : temp.split(","))
					PACKETLOGGER_ACCOUNTS.add(s);
			}

			temp = getProperty(serverSettings, "LogPacketsFromChars", "").trim();
			if(temp.isEmpty())
				PACKETLOGGER_CHARACTERS = null;
			else
			{
				PACKETLOGGER_CHARACTERS = new GArray<String>();
				for(String s : temp.split(","))
					PACKETLOGGER_CHARACTERS.add(s);
			}

			temp = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load PacketLogger Config. [" + lastKey + "]");
		}
	}

	public static void load()
	{
		//protection
		initProtection(Server.SERVER_MODE == Server.MODE_GAMESERVER || Server.SERVER_MODE == Server.MODE_COMBOSERVER);

		/*
		 * Load L2Open Version Properties file (if exists)
		 */
		try
		{
			Properties serverVersion = new Properties();
			InputStream is = Config.class.getResourceAsStream(Config.VERSION_FILE);
			serverVersion.load(is);
			is.close();

			SERVER_VERSION = getProperty(serverVersion, "version", SERVER_VERSION_UNSUPPORTED);
			SERVER_BUILD_DATE = getProperty(serverVersion, "builddate", "Undefined Date.");
			_log.info("Gameserver Version: " + SERVER_VERSION + ", build date: " + SERVER_BUILD_DATE);
		}
		catch(Exception e)
		{
			//Ignore Properties file if it doesn't exist
			SERVER_VERSION = SERVER_VERSION_UNSUPPORTED;
			SERVER_BUILD_DATE = "Undefined Date.";
		}

		if(Server.SERVER_MODE == Server.MODE_GAMESERVER || Server.SERVER_MODE == Server.MODE_COMBOSERVER)
		{
			_log.info("Loading gameserver config.");
			
			// L2-WT
			try
			{
				Properties wtSettings = loadPropertiesFile(L2WT_FILE);
				ANNOUNCE_BAN_CHAT = getBooleanProperty(wtSettings, "ANNOUNCE_BAN_CHAT", true);
				SELL_FREE_ADENA = getBooleanProperty(wtSettings, "SELL_FREE_ADENA", false);
				
				SECOND_AUTH_ENABLED = getBooleanProperty(wtSettings, "SecondAuthEnabled", false);
				SECOND_AUTH_MAX_ATTEMPTS = getIntProperty(wtSettings, "SecondAuthMaxAttempts", 5);
				SECOND_AUTH_BAN_TIME = getIntProperty(wtSettings, "SecondAuthBanTime", 480);
				SECOND_AUTH_REC_LINK = getProperty(wtSettings, "SecondAuthRecoveryLink", "http://la2era.ru/");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + L2WT_FILE + " File. [" + lastKey + "]");
			}
			
			try
			{
				Properties serverSettings = loadPropertiesFile(CONFIGURATION_FILE);

				loadAntiFlood(serverSettings);
				reloadPacketLoggerConfig();
				GAME_SERVER_LOGIN_HOST = getProperty(serverSettings, "LoginHost", "127.0.0.1").trim();
				GAME_SERVER_LOGIN_PORT = getIntProperty(serverSettings, "LoginPort", 9013);
				GAME_SERVER_LOGIN_CRYPT = getBooleanProperty(serverSettings, "LoginUseCrypt", true);
				SERVER_LIST_TESTSERVER = getBooleanProperty(serverSettings, "TestServer", false);
				ADVIPSYSTEM = getBooleanProperty(serverSettings, "AdvIPSystem", false);
				WEB_SERVER_DELAY = getIntProperty(serverSettings, "WebServerDelay", 10) * 1000;
				WEB_SERVER_ROOT = getProperty(serverSettings, "WebServerRoot", "./webserver/");
				HIDE_GM_STATUS = getBooleanProperty(serverSettings, "HideGMStatus", false);
				SHOW_GM_LOGIN = getBooleanProperty(serverSettings, "ShowGMLogin", true);
				SAVE_GM_EFFECTS = getBooleanProperty(serverSettings, "SaveGMEffects", false);

				REQUEST_ID = getIntProperty(serverSettings, "RequestServerID", 0);
				ACCEPT_ALTERNATE_ID = getBooleanProperty(serverSettings, "AcceptAlternateID", true);

				PORTS_GAME = getIntArray(serverSettings, "GameserverPort", new int[] { 7777 });
				PORT_LOGIN = getIntProperty(serverSettings, "LoginserverPort", 2106);
				CNAME_TEMPLATE = getProperty(serverSettings, "CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
				CLAN_NAME_TEMPLATE = getProperty(serverSettings, "ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
				CLAN_TITLE_TEMPLATE = getProperty(serverSettings, "ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
				ALLY_NAME_TEMPLATE = getProperty(serverSettings, "AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
				ANAME_TEMPLATE = getProperty(serverSettings, "AnameTemplate", "[A-Za-z0-9]{3,14}");
				APASSWD_TEMPLATE = getProperty(serverSettings, "ApasswdTemplate", "[A-Za-z0-9]{8,16}");
				LOGIN_TRY_BEFORE_BAN = getIntProperty(serverSettings, "LoginTryBeforeBan", 10);
				GAMESERVER_HOSTNAME = getProperty(serverSettings, "GameserverHostname");

				GLOBAL_CHAT = getIntProperty(serverSettings, "GlobalChat", 0);
				GLOBAL_TRADE_CHAT = getIntProperty(serverSettings, "GlobalTradeChat", 0);
				SHOUT_CHAT_MODE = getIntProperty(serverSettings, "ShoutChatMode", 1);
				TRADE_CHAT_MODE = getIntProperty(serverSettings, "TradeChatMode", 1);
				TRADE_WORDS = getContainsNoCasePatternArray(serverSettings, "TradeWords", new String[] { "продам", "куплю",
						"обменяю", "ВТТ", "ВТС", "ВТБ", "WTB", "WTT", "WTS" }, getBooleanProperty(serverSettings, "TradeChatsReplaceExPattern", false));

				TRADE_CHATS_REPLACE_FROM_ALL = getBooleanProperty(serverSettings, "TradeChatsReplaceFromAll", false);
				TRADE_CHATS_REPLACE_FROM_SHOUT = getBooleanProperty(serverSettings, "TradeChatsReplaceFromShout", false);

				EVERYBODY_HAS_ADMIN_RIGHTS = getBooleanProperty(serverSettings, "EverybodyHasAdminRights", false);
				ALLOW_SPECIAL_COMMANDS = getBooleanProperty(serverSettings, "AllowSpecialCommands", false);
				LOG_CHAT = getBooleanProperty(serverSettings, "LogChat", false);
				LOG_CHAT_DB = getProperty(serverSettings, "LogChatDB", "");
				if(LOG_CHAT_DB.isEmpty())
					LOG_CHAT_DB = null;
				LOG_KILLS = getBooleanProperty(serverSettings, "LogKills", false);
				LOG_TELNET = getBooleanProperty(serverSettings, "LogTelnet", false);
				SQL_LOG = getBooleanProperty(serverSettings, "SqlLog", false);

				RATE_XP = getFloatProperty(serverSettings, "RateXp", 1.);
				RATE_SP = getFloatProperty(serverSettings, "RateSp", 1.);
				RATE_QUESTS_REWARD = getFloatProperty(serverSettings, "RateQuestsReward", 1.);
				RATE_QUESTS_DROP = getFloatProperty(serverSettings, "RateQuestsDrop", RATE_QUESTS_REWARD);
				RATE_QUESTS_DROP_PROF = getFloatProperty(serverSettings, "RateQuestsDropProf", 1.);
				RATE_QUESTS_OCCUPATION_CHANGE = getBooleanProperty(serverSettings, "RateQuestsRewardOccupationChange", true);
				RATE_CLAN_REP_SCORE = getFloatProperty(serverSettings, "RateClanRepScore", 1.);
				RATE_CLAN_REP_SCORE_MAX_AFFECTED = getIntProperty(serverSettings, "RateClanRepScoreMaxAffected", 2);
				RATE_DROP_ADENA = getFloatProperty(serverSettings, "RateDropAdena", 1.);
				RATE_DROP_ADENA_MULT_MOD = getFloatProperty(serverSettings, "RateDropAdenaMultMod", 1.);
				RATE_DROP_ADENA_STATIC_MOD = getFloatProperty(serverSettings, "RateDropAdenaStaticMod", 0.);
				RATE_DROP_ITEMS = getFloatProperty(serverSettings, "RateDropItems", 1.);
				RATE_DROP_COMMON_ITEMS = getFloatProperty(serverSettings, "RateDropCommonItems", 1.);
				RATE_DROP_RAIDBOSS = getFloatProperty(serverSettings, "RateRaidBoss", 1.);
				RATE_DROP_SPOIL = getFloatProperty(serverSettings, "RateDropSpoil", 1.);
				RATE_BREAKPOINT = getIntProperty(serverSettings, "RateBreakpoint", 15);
				MAX_DROP_ITERATIONS = getIntProperty(serverSettings, "RateMaxIterations", 30);
				RATE_MANOR = getDoubleProperty(serverSettings, "RateManor", 1.);
				RATE_FISH_DROP_COUNT = getFloatProperty(serverSettings, "RateFishDropCount", 1.);
				RATE_SIEGE_GUARDS_PRICE = getFloatProperty(serverSettings, "RateSiegeGuardsPrice", 1.);
				RATE_PARTY_MIN = getBooleanProperty(serverSettings, "RatePartyMin", false);
				RATE_RAID_REGEN = getFloatProperty(serverSettings, "RateRaidRegen", 1.);
				RAID_MAX_LEVEL_DIFF = getIntProperty(serverSettings, "RaidMaxLevelDiff", 8);
				PARALIZE_ON_RAID_DIFF = getBooleanProperty(serverSettings, "ParalizeOnRaidLevelDiff", true);

				AUTODESTROY_ITEM_AFTER = getIntProperty(serverSettings, "AutoDestroyDroppedItemAfter", 0);
				AUTODESTROY_PLAYER_ITEM_AFTER = getIntProperty(serverSettings, "AutoDestroyPlayerDroppedItemAfter", 0);
				DELETE_DAYS = getIntProperty(serverSettings, "DeleteCharAfterDays", 7);
				PURGE_BYPASS_TASK_FREQUENCY = getIntProperty(serverSettings, "PurgeTaskFrequency", 60);

				DATAPACK_ROOT = new File(getProperty(serverSettings, "DatapackRoot", ".")).getCanonicalFile();

				L2WALKER_PUNISHMENT = getIntProperty(serverSettings, "L2WalkerPunishment", 1);
				BUGUSER_PUNISH = getIntProperty(serverSettings, "BugUserPunishment", 2);
				DEFAULT_PUNISH = getIntProperty(serverSettings, "IllegalActionPunishment", 1);

				ALLOW_DISCARDITEM = getBooleanProperty(serverSettings, "AllowDiscardItem", true);
				ALLOW_FREIGHT = getBooleanProperty(serverSettings, "AllowFreight", false);
				ALLOW_WAREHOUSE = getBooleanProperty(serverSettings, "AllowWarehouse", true);
				ALLOW_WATER = getBooleanProperty(serverSettings, "AllowWater", true);
				ALLOW_BOAT = getBooleanProperty(serverSettings, "AllowBoat", false);
				ALLOW_CURSED_WEAPONS = getBooleanProperty(serverSettings, "AllowCursedWeapons", false);
				DROP_CURSED_WEAPONS_ON_KICK = getBooleanProperty(serverSettings, "DropCursedWeaponsOnKick", false);

				MIN_PROTOCOL_REVISION = getIntProperty(serverSettings, "MinProtocolRevision", 387);
				MAX_PROTOCOL_REVISION = getIntProperty(serverSettings, "MaxProtocolRevision", 398);

				if(MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
					throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");

				MIN_NPC_ANIMATION = getIntProperty(serverSettings, "MinNPCAnimation", 5);
				MAX_NPC_ANIMATION = getIntProperty(serverSettings, "MaxNPCAnimation", 90);

				INTERNAL_HOSTNAME = getProperty(serverSettings, "InternalHostname", "*");
				EXTERNAL_HOSTNAME = getProperty(serverSettings, "ExternalHostname", "*");

				SERVER_SIDE_NPC_NAME = getBooleanProperty(serverSettings, "ServerSideNpcName", false);
				SERVER_SIDE_NPC_TITLE_WITH_LVL = getBooleanProperty(serverSettings, "ServerSideNpcTitleWithLvl", false);
				SERVER_SIDE_NPC_TITLE = getBooleanProperty(serverSettings, "ServerSideNpcTitle", false);

				HARD_DB_CLEANUP_ON_START = getBooleanProperty(serverSettings, "HardDbCleanUpOnStart", false);

				AUTOSAVE = getBooleanProperty(serverSettings, "Autosave", true);

				MAXIMUM_ONLINE_USERS = getIntProperty(serverSettings, "MaximumOnlineUsers", 100);

				LAZY_ITEM_UPDATE = getBooleanProperty(serverSettings, "LazyItemUpdate", false);
				LAZY_ITEM_UPDATE_ALL = getBooleanProperty(serverSettings, "LazyItemUpdateAll", false);
				LAZY_ITEM_UPDATE_TIME = getIntProperty(serverSettings, "LazyItemUpdateTime", 60000);
				LAZY_ITEM_UPDATE_ALL_TIME = getIntProperty(serverSettings, "LazyItemUpdateAllTime", 60000);
				DELAYED_ITEMS_UPDATE_INTERVAL = getIntProperty(serverSettings, "DelayedItemsUpdateInterval", 10000);
				USER_INFO_INTERVAL = getIntProperty(serverSettings, "UserInfoInterval", 100);
				BROADCAST_STATS_INTERVAL = getBooleanProperty(serverSettings, "BroadcastStatsInterval", true);
				BROADCAST_CHAR_INFO_INTERVAL = getIntProperty(serverSettings, "BroadcastCharInfoInterval", 100);
				SAVE_GAME_TIME_INTERVAL = getIntProperty(serverSettings, "SaveGameTimeInterval", 120);

				SERVER_LIST_BRACKET = getBooleanProperty(serverSettings, "ServerListBrackets", false);
				SERVER_LIST_CLOCK = getBooleanProperty(serverSettings, "ServerListClock", false);
				SERVER_GMONLY = getBooleanProperty(serverSettings, "ServerGMOnly", false);

				THREAD_P_GENERAL = getIntProperty(serverSettings, "ThreadPoolSizeGeneral", 15);
				THREAD_P_MOVE = getIntProperty(serverSettings, "ThreadPoolSizeMove", 25);
				THREAD_P_PATHFIND = getIntProperty(serverSettings, "ThreadPoolSizePathfind", 10);
				NPC_AI_MAX_THREAD = getIntProperty(serverSettings, "NpcAiMaxThread", 10);
				INTEREST_MAX_THREAD = getIntProperty(serverSettings, "InterestMaxThread", 10);
				PLAYER_AI_MAX_THREAD = getIntProperty(serverSettings, "PlayerAiMaxThread", 20);

				GENERAL_PACKET_THREAD_CORE_SIZE = getIntProperty(serverSettings, "GeneralPacketThreadCoreSize", 4);
				URGENT_PACKET_THREAD_CORE_SIZE = getIntProperty(serverSettings, "UrgentPacketThreadCoreSize", 2);

				SELECTOR_SLEEP_TIME = getIntProperty(serverSettings, "SelectorSleepTime", 3);
				INTEREST_ALT = getBooleanProperty(serverSettings, "InterestAlt", true);
				MULTI_THREADED_IDFACTORY_EXTRACTOR = getBooleanProperty(serverSettings, "MultiThreadedIdFactoryExtractor", false);
				MULTI_THREADED_IDFACTORY_CLEANER = getBooleanProperty(serverSettings, "MultiThreadedIdFactoryCleaner", false);

				DEADLOCKCHECK_INTERVAL = getIntProperty(serverSettings, "DeadLockCheck", 10000);
				GARBAGE_COLLECTOR_INTERVAL = getIntProperty(serverSettings, "GarbageCollectorInterval", 30) * 60000;

				CHAT_MESSAGE_MAX_LEN = getIntProperty(serverSettings, "ChatMessageLimit", 1000);
				CHAT_MAX_LINES = getIntProperty(serverSettings, "ChatMaxLines", 5);
				CHAT_LINE_LENGTH = getIntProperty(serverSettings, "ChatLineLength", -1);
				MAT_BANCHAT = getBooleanProperty(serverSettings, "MAT_BANCHAT", false);
				MAT_BAN_COUNT_CHANNELS = 1;
				for(int id : getIntArray(serverSettings, "MAT_BAN_CHANNEL", new int[] { 0 }))
				{
					BAN_CHANNEL_LIST[MAT_BAN_COUNT_CHANNELS] = id;
					MAT_BAN_COUNT_CHANNELS++;
				}
				MAT_REPLACE = getBooleanProperty(serverSettings, "MAT_REPLACE", false);
				MAT_REPLACE_STRING = getProperty(serverSettings, "MAT_REPLACE_STRING", "[censored]");
				MAT_ANNOUNCE = getBooleanProperty(serverSettings, "MAT_ANNOUNCE", true);
				MAT_ANNOUNCE_FOR_ALL_WORLD = getBooleanProperty(serverSettings, "MAT_ANNOUNCE_FOR_ALL_WORLD", true);
				MAT_ANNOUNCE_NICK = getBooleanProperty(serverSettings, "MAT_ANNOUNCE_NICK", true);
				UNCHATBANTIME = getIntProperty(serverSettings, "Timer_to_UnBan", 30);
				DEFAULT_LANG = getProperty(serverSettings, "DefaultLang", "ru");
				USE_CLIENT_LANG = getBooleanProperty(serverSettings, "UseClientLang", true);
				ENG_QUEST_NAMES = getBooleanProperty(serverSettings, "EngQuestNames", true);
				RESTART_TIME = getIntProperty(serverSettings, "AutoRestart", 0);
				RESTART_AT_TIME = getIntProperty(serverSettings, "AutoRestartAt", 5);
				if(RESTART_AT_TIME > 24)
					RESTART_AT_TIME = 24;

				CHECK_LANG_FILES_MODIFY = getBooleanProperty(serverSettings, "checkLangFilesModify", false);

				USE_FILE_CACHE = getBooleanProperty(serverSettings, "useFileCache", true);

				LINEAR_TERRITORY_CELL_SIZE = getIntProperty(serverSettings, "LinearTerritoryCellSize", 32);

				for(int id : getIntArray(serverSettings, "DisableCreateItems", new int[] {}))
					DISABLE_CREATION_ID_LIST.add(id);

				for(int id : getIntArray(serverSettings, "LogMultisellId", new int[] {}))
					LOG_MULTISELL_ID_LIST.add(id);

				MOVE_PACKET_DELAY = getIntProperty(serverSettings, "MovePacketDelay", 100);
				ATTACK_PACKET_DELAY = getIntProperty(serverSettings, "AttackPacketDelay", 500);

				DAMAGE_FROM_FALLING = getBooleanProperty(serverSettings, "DamageFromFalling", true);

				ALLOW_WEDDING = getBooleanProperty(serverSettings, "AllowWedding", false);
				WEDDING_PRICE = getIntProperty(serverSettings, "WeddingPrice", 500000);
				WEDDING_PUNISH_INFIDELITY = getBooleanProperty(serverSettings, "WeddingPunishInfidelity", true);
				WEDDING_TELEPORT = getBooleanProperty(serverSettings, "WeddingTeleport", true);
				WEDDING_TELEPORT_PRICE = getIntProperty(serverSettings, "WeddingTeleportPrice", 500000);
				WEDDING_TELEPORT_INTERVAL = getIntProperty(serverSettings, "WeddingTeleportInterval", 120);
				WEDDING_SAMESEX = getBooleanProperty(serverSettings, "WeddingAllowSameSex", true);
				WEDDING_FORMALWEAR = getBooleanProperty(serverSettings, "WeddingFormalWear", true);
				WEDDING_DIVORCE_COSTS = getIntProperty(serverSettings, "WeddingDivorceCosts", 20);

				FORCE_STATUSUPDATE = getBooleanProperty(serverSettings, "ForceStatusUpdate", false);
				PLAYER_LOGOUT_INGAME_TIME = getLongProperty(serverSettings, "LogoutIngameTime", 60) * 1000;
				PLAYER_DISCONNECT_INGAME_TIME = getLongProperty(serverSettings, "DisconnectedIngameTime", 90) * 1000;
				DONTLOADSPAWN = getBooleanProperty(serverSettings, "StartWhisoutSpawn", false);
				DONTLOADQUEST = getBooleanProperty(serverSettings, "StartWhisoutQuest", false);

				GG_CHECK = getBooleanProperty(serverSettings, "GGCheck", true);

				SNAPSHOTS_DIRECTORY = getProperty(serverSettings, "SnapshotsDirectory", "./log/snapshots");
				DUMP_MEMORY_ON_SHUTDOWN = getBooleanProperty(serverSettings, "MemorySnapshotOnShutdown", false);

				TIMEOUT_CHECKER_CLIENT = getLongProperty(serverSettings, "TimeOutChecker", 2000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + CONFIGURATION_FILE + " File. [" + lastKey + "]");
			}

			// telnet
			try
			{
				Properties telnetSettings = loadPropertiesFile(TELNET_FILE);
				IS_TELNET_ENABLED = getBooleanProperty(telnetSettings, "EnableTelnet", false);
				if(IS_TELNET_ENABLED)
					TELNET_DEFAULT_ENCODING = getProperty(telnetSettings, "DefaultEncoding", "");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + TELNET_FILE + " File. [" + lastKey + "]");
			}

			// community settings
			try
			{
				Properties communityboardSettings = loadPropertiesFile(COMMUNITY_CONFIG_FILE);
				ALLOW_COMMUNITYBOARD = getBooleanProperty(communityboardSettings, "AllowCommunityBoard", true);
				BBS_DEFAULT = getProperty(communityboardSettings, "BBSDefault", "_bbshome");
				COMMUNITYBOARD_HTML_ROOT = getProperty(communityboardSettings, "CommunityBoardHtmlRoot", "data/html/CommunityBoard/");
				COMMUNITYBOARD_SORTPLAYERSLIST = getBooleanProperty(communityboardSettings, "CommunityBoardSortPlayersList", false);
				NAME_PAGE_SIZE_COMMUNITYBOARD = getIntProperty(communityboardSettings, "NamePageSizeOnCommunityBoard", 50);
				NAME_PER_ROW_COMMUNITYBOARD = getIntProperty(communityboardSettings, "NamePerRowOnCommunityBoard", 5);
				COMMUNITYBOARD_PLAYERSLIST_CACHE = getIntProperty(communityboardSettings, "CommunityBoardPlayersListCache", 0) * 1000L;
				ALLOW_COMMUNITYBOARD_PLAYERSLIST = getProperty(communityboardSettings, "AllowCommunityBoardPlayersList", "all");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + COMMUNITY_CONFIG_FILE + " File. [" + lastKey + "]");
			}

			// residence settings
			try
			{
				Properties residenceSettings = loadPropertiesFile(RESIDENCE_CONFIG_FILE);

				CH_BID_GRADE1_MINCLANLEVEL = getIntProperty(residenceSettings, "ClanHallBid_Grade1_MinClanLevel", 2);
				CH_BID_GRADE1_MINCLANMEMBERS = getIntProperty(residenceSettings, "ClanHallBid_Grade1_MinClanMembers", 1);
				CH_BID_GRADE1_MINCLANMEMBERSLEVEL = getIntProperty(residenceSettings, "ClanHallBid_Grade1_MinClanMembersAvgLevel", 1);
				CH_BID_GRADE2_MINCLANLEVEL = getIntProperty(residenceSettings, "ClanHallBid_Grade2_MinClanLevel", 2);
				CH_BID_GRADE2_MINCLANMEMBERS = getIntProperty(residenceSettings, "ClanHallBid_Grade2_MinClanMembers", 1);
				CH_BID_GRADE2_MINCLANMEMBERSLEVEL = getIntProperty(residenceSettings, "ClanHallBid_Grade2_MinClanMembersAvgLevel", 1);
				CH_BID_GRADE3_MINCLANLEVEL = getIntProperty(residenceSettings, "ClanHallBid_Grade3_MinClanLevel", 2);
				CH_BID_GRADE3_MINCLANMEMBERS = getIntProperty(residenceSettings, "ClanHallBid_Grade3_MinClanMembers", 1);
				CH_BID_GRADE3_MINCLANMEMBERSLEVEL = getIntProperty(residenceSettings, "ClanHallBid_Grade3_MinClanMembersAvgLevel", 1);
				RESIDENCE_LEASE_FUNC_MULTIPLIER = getDoubleProperty(residenceSettings, "ResidenceLeaseFuncMultiplier", 1.);
				RESIDENCE_LEASE_MULTIPLIER = getDoubleProperty(residenceSettings, "ResidenceLeaseMultiplier", 1.);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + RESIDENCE_CONFIG_FILE + " File. [" + lastKey + "]");
			}

			// other
			try
			{
				Properties otherSettings = loadPropertiesFile(OTHER_CONFIG_FILE);

				DEEPBLUE_DROP_RULES = getBooleanProperty(otherSettings, "UseDeepBlueDropRules", true);
				DEEPBLUE_DROP_MAXDIFF = getIntProperty(otherSettings, "DeepBlueDropMaxDiff", 8);
				DEEPBLUE_DROP_RAID_MAXDIFF = getIntProperty(otherSettings, "DeepBlueDropRaidMaxDiff", 2);

				SWIMING_SPEED = getIntProperty(otherSettings, "SwimingSpeedTemplate", 50);

				/* Inventory slots limits */
				INVENTORY_MAXIMUM_NO_DWARF = getIntProperty(otherSettings, "MaximumSlotsForNoDwarf", 80);
				INVENTORY_MAXIMUM_DWARF = getIntProperty(otherSettings, "MaximumSlotsForDwarf", 100);
				INVENTORY_MAXIMUM_GM = getIntProperty(otherSettings, "MaximumSlotsForGMPlayer", 250);

				MULTISELL_SIZE = getIntProperty(otherSettings, "MultisellPageSize", 10);

				/* Warehouse slots limits */
				WAREHOUSE_SLOTS_NO_DWARF = getIntProperty(otherSettings, "BaseWarehouseSlotsForNoDwarf", 100);
				WAREHOUSE_SLOTS_DWARF = getIntProperty(otherSettings, "BaseWarehouseSlotsForDwarf", 120);
				WAREHOUSE_SLOTS_CLAN = getIntProperty(otherSettings, "MaximumWarehouseSlotsForClan", 200);

				/* chance to enchant an item over safe level */
				ENCHANT_CHANCE_WEAPON = getIntProperty(otherSettings, "EnchantChance", 66);
				ENCHANT_CHANCE_ARMOR = getIntProperty(otherSettings, "EnchantChanceArmor", ENCHANT_CHANCE_WEAPON);
				ENCHANT_CHANCE_ACCESSORY = getIntProperty(otherSettings, "EnchantChanceAccessory", ENCHANT_CHANCE_ARMOR);
				ENCHANT_CHANCE_CRYSTAL_WEAPON = getIntProperty(otherSettings, "EnchantChanceCrystal", 66);
				ENCHANT_CHANCE_CRYSTAL_ARMOR = getIntProperty(otherSettings, "EnchantChanceCrystalArmor", ENCHANT_CHANCE_CRYSTAL_WEAPON);
				ENCHANT_CHANCE_CRYSTAL_ACCESSORY = getIntProperty(otherSettings, "EnchantChanceCrystalAccessory", ENCHANT_CHANCE_CRYSTAL_ARMOR);
				SAFE_ENCHANT_COMMON = getIntProperty(otherSettings, "SafeEnchantCommon", 3);
				SAFE_ENCHANT_FULL_BODY = getIntProperty(otherSettings, "SafeEnchantFullBody", 4);
				ENCHANT_MAX = getByteProperty(otherSettings, "EnchantMax", 20);
				ARMOR_OVERENCHANT_HPBONUS_LIMIT = getIntProperty(otherSettings, "ArmorOverEnchantHPBonusLimit", 10) - 3;

				ENCHANT_ATTRIBUTE_STONE_CHANCE = getIntProperty(otherSettings, "EnchantAttributeChance", 50);
				ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE = getIntProperty(otherSettings, "EnchantAttributeCrystalChance", 30);

				REGEN_SIT_WAIT = getBooleanProperty(otherSettings, "RegenSitWait", false);

				STARTING_ADENA = getIntProperty(otherSettings, "StartingAdena", 0);

				/* Amount of HP, MP, and CP is restored */
				RESPAWN_RESTORE_CP = getDoubleProperty(otherSettings, "RespawnRestoreCP", -1) / 100;
				RESPAWN_RESTORE_HP = getDoubleProperty(otherSettings, "RespawnRestoreHP", 65) / 100;
				RESPAWN_RESTORE_MP = getDoubleProperty(otherSettings, "RespawnRestoreMP", -1) / 100;

				/* Maximum number of available slots for pvt stores */
				MAX_PVTSTORE_SLOTS_DWARF = getIntProperty(otherSettings, "MaxPvtStoreSlotsDwarf", 5);
				MAX_PVTSTORE_SLOTS_OTHER = getIntProperty(otherSettings, "MaxPvtStoreSlotsOther", 4);
				MAX_PVTCRAFT_SLOTS = getIntProperty(otherSettings, "MaxPvtManufactureSlots", 20);

				SENDSTATUS_TRADE_JUST_OFFLINE = getBooleanProperty(otherSettings, "SendStatusTradeJustOffline", false);

				ANNOUNCE_MAMMON_SPAWN = getBooleanProperty(otherSettings, "AnnounceMammonSpawn", true);

				GM_NAME_COLOUR = getIntHexProperty(otherSettings, "GMNameColour", 0xFFFFFF);
				GM_HERO_AURA = getBooleanProperty(otherSettings, "GMHeroAura", true);
				NORMAL_NAME_COLOUR = getIntHexProperty(otherSettings, "NormalNameColour", 0xFFFFFF);
				CLANLEADER_NAME_COLOUR = getIntHexProperty(otherSettings, "ClanleaderNameColour", 0xFFFFFF);

				RRD_ENABLED = getBooleanProperty(otherSettings, "UseRRD", true);
				RRD_EXTENDED = getBooleanProperty(otherSettings, "UseExtendedRRD", false);
				RRD_PATH = getProperty(otherSettings, "RRDPath", "./config/");
				RRD_GRAPH_PATH = getProperty(otherSettings, "GraphPath", "./webserver/");
				RRD_UPDATE_TIME = getLongProperty(otherSettings, "UpdateDelay", 30);
				RRD_GRAPH_HEIGHT = getIntProperty(otherSettings, "GraphHeight", 378);
				RRD_GRAPH_WIDTH = getIntProperty(otherSettings, "GraphWidth", 580);
				RRD_LINE_WIDTH = getFloatProperty(otherSettings, "LineWidth", 1.0);
				RRD_AREA_COLOR = getProperty(otherSettings, "GraphAreaColor", "ORANGE");
				RRD_LINE_COLOR = getProperty(otherSettings, "GraphLineColor", "RED");
				SHOW_HTML_WELCOME = getBooleanProperty(otherSettings, "ShowHTMLWelcome", false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + OTHER_CONFIG_FILE + " File. [" + lastKey + "]");
			}

			// spoil & manor
			try
			{
				Properties spoilSettings = loadPropertiesFile(SPOIL_CONFIG_FILE);

				BASE_SPOIL_RATE = getFloatProperty(spoilSettings, "BasePercentChanceOfSpoilSuccess", 78.);
				MINIMUM_SPOIL_RATE = getFloatProperty(spoilSettings, "MinimumPercentChanceOfSpoilSuccess", 1.);
				ALT_SPOIL_FORMULA = getBooleanProperty(spoilSettings, "AltFormula", false);
				MANOR_SOWING_BASIC_SUCCESS = getIntProperty(spoilSettings, "BasePercentChanceOfSowingSuccess", 100);
				MANOR_SOWING_ALT_BASIC_SUCCESS = getIntProperty(spoilSettings, "BasePercentChanceOfSowingAltSuccess", 10);
				MANOR_HARVESTING_BASIC_SUCCESS = getIntProperty(spoilSettings, "BasePercentChanceOfHarvestingSuccess", 90);
				MANOR_DIFF_PLAYER_TARGET = getIntProperty(spoilSettings, "MinDiffPlayerMob", 5);
				MANOR_DIFF_PLAYER_TARGET_PENALTY = getIntProperty(spoilSettings, "DiffPlayerMobPenalty", 5);
				MANOR_DIFF_SEED_TARGET = getIntProperty(spoilSettings, "MinDiffSeedMob", 5);
				MANOR_DIFF_SEED_TARGET_PENALTY = getIntProperty(spoilSettings, "DiffSeedMobPenalty", 5);
				ALLOW_MANOR = getBooleanProperty(spoilSettings, "AllowManor", true);
				MANOR_REFRESH_TIME = getIntProperty(spoilSettings, "AltManorRefreshTime", 20);
				MANOR_REFRESH_MIN = getIntProperty(spoilSettings, "AltManorRefreshMin", 00);
				MANOR_APPROVE_TIME = getIntProperty(spoilSettings, "AltManorApproveTime", 6);
				MANOR_APPROVE_MIN = getIntProperty(spoilSettings, "AltManorApproveMin", 00);
				MANOR_MAINTENANCE_PERIOD = getIntProperty(spoilSettings, "AltManorMaintenancePeriod", 360000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + SPOIL_CONFIG_FILE + " File. [" + lastKey + "]");
			}

			// alternative settings
			try
			{
				Properties altSettings = loadPropertiesFile(ALT_SETTINGS_FILE);

				ALT_ARENA_EXP = getBooleanProperty(altSettings, "ArenaExp", true);
				WEAR_TEST_ENABLED = getBooleanProperty(altSettings, "WearTestEnabled", false);
				AUTO_LOOT = getBooleanProperty(altSettings, "AutoLoot", false);
				AUTO_LOOT_HERBS = getBooleanProperty(altSettings, "AutoLootHerbs", false);
				AUTO_LOOT_INDIVIDUAL = getBooleanProperty(altSettings, "AutoLootIndividual", false);
				AUTO_LOOT_FROM_RAIDS = getBooleanProperty(altSettings, "AutoLootFromRaids", false);
				AUTO_LOOT_PK = getBooleanProperty(altSettings, "AutoLootPK", false);
				AUTO_LOOT_PA = getBooleanProperty(altSettings, "AutoLootPA", false);
				ALT_GAME_KARMA_PLAYER_CAN_SHOP = getBooleanProperty(altSettings, "AltKarmaPlayerCanShop", false);
				KILL_COUNTER = getBooleanProperty(altSettings, "KillCounter", true);
				DROP_COUNTER = getBooleanProperty(altSettings, "DropCounter", false);
				CRAFT_COUNTER = getBooleanProperty(altSettings, "CraftCounter", true);
				CRAFT_MASTERWORK_CHANCE = getDoubleProperty(altSettings, "CraftMasterworkChance", 3);
				CRAFT_MASTERWORK_LEVEL_MOD = getDoubleProperty(altSettings, "CraftMasterworkLevelMod", 0.2);
				CRAFT_MASTERWORK_CHEST = getBooleanProperty(altSettings, "CraftMasterworkChest", true);
				ALT_TRUE_CHESTS = getIntProperty(altSettings, "TrueChests", 50);
				ALT_GAME_MATHERIALSDROP = getBooleanProperty(altSettings, "AltMatherialsDrop", false);
				ALT_DOUBLE_SPAWN = getBooleanProperty(altSettings, "DoubleSpawn", false);
				ALT_RAID_RESPAWN_MULTIPLIER = getFloatProperty(altSettings, "AltRaidRespawnMultiplier", 1.0);
				ALT_ALLOW_AUGMENT_ALL = getBooleanProperty(altSettings, "AugmentAll", false);
				ALT_ALLOW_DROP_AUGMENTED = getBooleanProperty(altSettings, "AllowDropAugmented", false);
				ALT_GAME_EXP_FOR_CRAFT = getBooleanProperty(altSettings, "AltExpForCraft", true);
				ALT_GAME_UNREGISTER_RECIPE = getBooleanProperty(altSettings, "AltUnregisterRecipe", true);
				ALT_GAME_SHOW_DROPLIST = getBooleanProperty(altSettings, "AltShowDroplist", true);
				ALT_GAME_GEN_DROPLIST_ON_DEMAND = getBooleanProperty(altSettings, "AltGenerateDroplistOnDemand", false);
				ALLOW_NPC_SHIFTCLICK = getBooleanProperty(altSettings, "AllowShiftClick", true);
				ALT_FULL_NPC_STATS_PAGE = getBooleanProperty(altSettings, "AltFullStatsPage", false);
				ALT_GAME_SUBCLASS_WITHOUT_QUESTS = getBooleanProperty(altSettings, "AltAllowSubClassWithoutQuest", false);
				ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM = getBooleanProperty(altSettings, "AltAllowSubClassWithoutBaium", true);
				ALT_GAME_LEVEL_TO_GET_SUBCLASS = getIntProperty(altSettings, "AltLevelToGetSubclass", 75);
				ALT_GAME_SUB_ADD = getIntProperty(altSettings, "AltSubAdd", 0);
				ALT_MAX_LEVEL = Math.min(getIntProperty(altSettings, "AltMaxLevel", 100), Experience.LEVEL.length - 1);
				ALT_MAX_SUB_LEVEL = Math.min(getIntProperty(altSettings, "AltMaxSubLevel", 100), Experience.LEVEL.length - 1);
				SUBCLASS_INIT_LEVEL = getByteProperty(altSettings, "SublcassInitLevel", 40);
				ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = getBooleanProperty(altSettings, "AltAllowOthersWithdrawFromClanWarehouse", false);
				ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = getBooleanProperty(altSettings, "AltAllowClanCommandOnlyForClanLeader", true);

				ALT_GAME_REQUIRE_CLAN_CASTLE = getBooleanProperty(altSettings, "AltRequireClanCastle", false);
				ALT_GAME_REQUIRE_CASTLE_DAWN = getBooleanProperty(altSettings, "AltRequireCastleDawn", true);
				ALT_GAME_ALLOW_ADENA_DAWN = getBooleanProperty(altSettings, "AltAllowAdenaDawn", true);
				ALT_ADD_RECIPES = getIntProperty(altSettings, "AltAddRecipes", 0);
				ALT_100_RECIPES_B = getBooleanProperty(altSettings, "Alt100PercentRecipesB", false);
				ALT_100_RECIPES_A = getBooleanProperty(altSettings, "Alt100PercentRecipesA", false);
				ALT_100_RECIPES_S = getBooleanProperty(altSettings, "Alt100PercentRecipesS", false);
				ALT_100_RECIPES_S80 = getBooleanProperty(altSettings, "Alt100PercentRecipesS80", false);
				SS_ANNOUNCE_PERIOD = getIntProperty(altSettings, "SSAnnouncePeriod", 0);
				ENABLE_FISHING_CHECK_WATER = getBooleanProperty(altSettings, "EnableFishingWaterCheck", true);
				ALT_SOCIAL_ACTION_REUSE = getBooleanProperty(altSettings, "AltSocialActionReuse", false);
				ALT_DISABLE_SPELLBOOKS = getBooleanProperty(altSettings, "AltDisableSpellbooks", false);
				ALT_SIMPLE_SIGNS = getBooleanProperty(altSettings, "PushkinSignsOptions", false);
				ALT_TELE_TO_CATACOMBS = getBooleanProperty(altSettings, "TeleToCatacombs", false);
				ALT_BS_CRYSTALLIZE = getBooleanProperty(altSettings, "BSCrystallize", false);
				ALT_MAMMON_UPGRADE = getIntProperty(altSettings, "MammonUpgrade", 6680500);
				ALT_MAMMON_EXCHANGE = getIntProperty(altSettings, "MammonExchange", 10091400);
				ALT_ALLOW_TATTOO = getBooleanProperty(altSettings, "AllowTattoo", false);
				NONOWNER_ITEM_PICKUP_DELAY = getLongProperty(altSettings, "NonOwnerItemPickupDelay", 15) * 1000L;
				ALT_NO_LASTHIT = getBooleanProperty(altSettings, "NoLasthitOnRaid", false);
				ALT_KAMALOKA_LIMITS = getProperty(altSettings, "KamalokaLimit", "All");
				ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = getBooleanProperty(altSettings, "KamalokaNightmaresPremiumOnly", false);
				ALT_KAMALOKA_NIGHTMARE_REENTER = getBooleanProperty(altSettings, "SellReenterNightmaresTicket", true);
				ALT_KAMALOKA_ABYSS_REENTER = getBooleanProperty(altSettings, "SellReenterAbyssTicket", true);
				ALT_KAMALOKA_LAB_REENTER = getBooleanProperty(altSettings, "SellReenterLabyrinthTicket", true);
				ALT_DONT_ALLOW_PETS_ON_SIEGE = getBooleanProperty(altSettings, "DontAllowPetsOnSiege", false);
				ALT_PET_HEAL_BATTLE_ONLY = getBooleanProperty(altSettings, "PetsHealOnlyInBattle", true);
				CHAR_TITLE = getBooleanProperty(altSettings, "CharTitle", false);
				ADD_CHAR_TITLE = getProperty(altSettings, "CharAddTitle", "Welcome");

				ALT_ALLOW_SELL_COMMON = getBooleanProperty(altSettings, "AllowSellCommon", true);
				ALT_ALLOW_SHADOW_WEAPONS = getBooleanProperty(altSettings, "AllowShadowWeapons", true);
				ALT_DISABLED_MULTISELL = getIntArray(altSettings, "DisabledMultisells", new int[0]);
				ALT_SHOP_PRICE_LIMITS = getIntArray(altSettings, "ShopPriceLimits", new int[0]);
				ALT_SHOP_UNALLOWED_ITEMS = getIntArray(altSettings, "ShopUnallowedItems", new int[0]);

				FESTIVAL_MIN_PARTY_SIZE = getIntProperty(altSettings, "FestivalMinPartySize", 5);
				FESTIVAL_RATE_PRICE = getFloatProperty(altSettings, "FestivalRatePrice", 1.0);

				RIFT_MIN_PARTY_SIZE = getIntProperty(altSettings, "RiftMinPartySize", 5);
				RIFT_SPAWN_DELAY = getIntProperty(altSettings, "RiftSpawnDelay", 10000);
				RIFT_AUTO_JUMPS_TIME = getIntProperty(altSettings, "AutoJumpsDelay", 8);
				RIFT_AUTO_JUMPS_TIME_RAND = getIntProperty(altSettings, "AutoJumpsDelayRandom", 120000);

				RIFT_ENTER_COST_RECRUIT = getIntProperty(altSettings, "RecruitFC", 18);
				RIFT_ENTER_COST_SOLDIER = getIntProperty(altSettings, "SoldierFC", 21);
				RIFT_ENTER_COST_OFFICER = getIntProperty(altSettings, "OfficerFC", 24);
				RIFT_ENTER_COST_CAPTAIN = getIntProperty(altSettings, "CaptainFC", 27);
				RIFT_ENTER_COST_COMMANDER = getIntProperty(altSettings, "CommanderFC", 30);
				RIFT_ENTER_COST_HERO = getIntProperty(altSettings, "HeroFC", 33);
				PARTY_LEADER_ONLY_CAN_INVITE = getBooleanProperty(altSettings, "PartyLeaderOnlyCanInvite", true);
				ALLOW_TALK_WHILE_SITTING = getBooleanProperty(altSettings, "AllowTalkWhileSitting", true);
				ALLOW_NOBLE_TP_TO_ALL = getBooleanProperty(altSettings, "AllowNobleTPToAll", false);

				ALLOW_FAKE_PLAYERS = getBooleanProperty(altSettings, "AllowFakePlayers", false);
				FAKE_PLAYERS_PERCENT = getIntProperty(altSettings, "FakePlayersPercent", 100);
				MAXLOAD_MODIFIER = getFloatProperty(altSettings, "MaxLoadModifier", 1.0);
				GATEKEEPER_MODIFIER = getFloatProperty(altSettings, "GkCostMultiplier", 1.0);
				GATEKEEPER_FREE = getIntProperty(altSettings, "GkFree", 40);
				CRUMA_GATEKEEPER_LVL = getIntProperty(altSettings, "GkCruma", 65);
				ALT_BUFF_MIN_LEVEL = getIntProperty(altSettings, "BuffMinLevel", 6);
				ALT_BUFF_MAX_LEVEL = getIntProperty(altSettings, "BuffMaxLevel", 75);
				ALT_IMPROVED_PETS_LIMITED_USE = getBooleanProperty(altSettings, "ImprovedPetsLimitedUse", false);
				ALT_CATACOMB_MODIFIER_HP = getIntProperty(altSettings, "AltCatacombMonstersMultHP", 4);
				ALT_CATACOMB_RESPAWN = getFloatProperty(altSettings, "AltCatacombMonstersRespawn", 1.);
				ALT_SAVE_SPAWN = getBooleanProperty(altSettings, "AltSaveSpawn", false);
				ALT_CHAMPION_CHANCE1 = getDoubleProperty(altSettings, "AltChampionChance1", 0.);
				ALT_CHAMPION_CHANCE2 = getDoubleProperty(altSettings, "AltChampionChance2", 0.);
				ALT_CHAMPION_CAN_BE_AGGRO = getBooleanProperty(altSettings, "AltChampionAggro", false);
				ALT_CHAMPION_CAN_BE_SOCIAL = getBooleanProperty(altSettings, "AltChampionSocial", false);
				ALT_VITALITY_ENABLED = getBooleanProperty(altSettings, "AltVitalityEnabled", true);
				ALT_VITALITY_POWER_MULT = getFloatProperty(altSettings, "AltVitalityPower", 1.);
				ALT_VITALITY_CONSUMPTION = getFloatProperty(altSettings, "AltVitalityConsumption", 1.);
				ALT_VITALITY_RAID_BONUS = getIntProperty(altSettings, "AltVitalityRaidBonus", 1000);

				SHUTDOWN_MSG_TYPE = getIntProperty(altSettings, "ShutdownMsgType", 3);
				ALT_MAX_ALLY_SIZE = getIntProperty(altSettings, "AltMaxAllySize", 3);
				ALT_PARTY_DISTRIBUTION_RANGE = getIntProperty(altSettings, "AltPartyDistributionRange", 1500);
				ALT_PARTY_BONUS = getFloatArray(altSettings, "AltPartyBonus", new float[] { 1.00f, 1.30f, 1.39f, 1.50f, 1.54f, 1.58f, 1.63f, 1.67f, 1.71f });

				ALLOW_CH_DOOR_OPEN_ON_CLICK = getBooleanProperty(altSettings, "AllowChDoorOpenOnClick", true);
				ALT_CH_ALL_BUFFS = getBooleanProperty(altSettings, "AltChAllBuffs", false);
				ALT_CH_ALLOW_1H_BUFFS = getBooleanProperty(altSettings, "AltChAllowHourBuff", false);
				ALT_CH_SIMPLE_DIALOG = getBooleanProperty(altSettings, "AltChSimpleDialog", false);
				SIEGE_OPERATE_DOORS = getBooleanProperty(altSettings, "SiegeOperateDoors", true);
				SIEGE_OPERATE_DOORS_LORD_ONLY = getBooleanProperty(altSettings, "SiegeOperateDoorsLordOnly", true);

				AUGMENTATION_NG_SKILL_CHANCE = getIntProperty(altSettings, "AugmentationNGSkillChance", 15);
				AUGMENTATION_NG_GLOW_CHANCE = getIntProperty(altSettings, "AugmentationNGGlowChance", 0);
				AUGMENTATION_MID_SKILL_CHANCE = getIntProperty(altSettings, "AugmentationMidSkillChance", 30);
				AUGMENTATION_MID_GLOW_CHANCE = getIntProperty(altSettings, "AugmentationMidGlowChance", 40);
				AUGMENTATION_HIGH_SKILL_CHANCE = getIntProperty(altSettings, "AugmentationHighSkillChance", 45);
				AUGMENTATION_HIGH_GLOW_CHANCE = getIntProperty(altSettings, "AugmentationHighGlowChance", 70);
				AUGMENTATION_TOP_SKILL_CHANCE = getIntProperty(altSettings, "AugmentationTopSkillChance", 60);
				AUGMENTATION_TOP_GLOW_CHANCE = getIntProperty(altSettings, "AugmentationTopGlowChance", 100);
				AUGMENTATION_BASESTAT_CHANCE = getIntProperty(altSettings, "AugmentationBaseStatChance", 1);
				AUGMENTATION_ACC_SKILL_CHANCE = getIntProperty(altSettings, "AugmentationAccSkillChance", 10);
				REVIVAL_POINT_IN_GIRAN = getBooleanProperty(altSettings, "RevivalPointInGiran", false);
				
				FOLLOW_RANGE = getIntProperty(altSettings, "FollowRange", 100);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + ALT_SETTINGS_FILE + " File. [" + lastKey + "]");
			}

			// services settings
			try
			{
				Properties servicesSettings = loadPropertiesFile(SERVICES_FILE);

				for(int id : getIntArray(servicesSettings, "AllowClassMasters", new int[] {}))
					if(id != 0)
						ALLOW_CLASS_MASTERS_LIST.add(id);

				CLASS_MASTERS_PRICE = getProperty(servicesSettings, "ClassMastersPrice", "0,0,0");
				if(CLASS_MASTERS_PRICE.length() >= 5)
				{
					int level = 1;
					for(String id : CLASS_MASTERS_PRICE.split(","))
					{
						CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id);
						level++;
					}
				}
				CLASS_MASTERS_PRICE_ITEM = getIntProperty(servicesSettings, "ClassMastersPriceItem", 57);
				ALLOW_SHORT_2ND_PROF_QUEST = getBooleanProperty(servicesSettings, "Short2ndProfQuest", true);

				SERVICES_CHANGE_NICK_PRICE = getIntProperty(servicesSettings, "NickChangePrice", 100);
				SERVICES_CHANGE_NICK_ITEM = getIntProperty(servicesSettings, "NickChangeItem", 4037);

				SERVICES_CHANGE_CLAN_NAME_PRICE = getIntProperty(servicesSettings, "ClanNameChangePrice", 100);
				SERVICES_CHANGE_CLAN_NAME_ITEM = getIntProperty(servicesSettings, "ClanNameChangeItem", 4037);

				SERVICES_CHANGE_PET_NAME_ENABLED = getBooleanProperty(servicesSettings, "PetNameChangeEnabled", false);
				SERVICES_CHANGE_PET_NAME_PRICE = getIntProperty(servicesSettings, "PetNameChangePrice", 100);
				SERVICES_CHANGE_PET_NAME_ITEM = getIntProperty(servicesSettings, "PetNameChangeItem", 4037);

				SERVICES_EXCHANGE_BABY_PET_ENABLED = getBooleanProperty(servicesSettings, "BabyPetExchangeEnabled", false);
				SERVICES_EXCHANGE_BABY_PET_PRICE = getIntProperty(servicesSettings, "BabyPetExchangePrice", 100);
				SERVICES_EXCHANGE_BABY_PET_ITEM = getIntProperty(servicesSettings, "BabyPetExchangeItem", 4037);

				SERVICES_CHANGE_SEX_PRICE = getIntProperty(servicesSettings, "SexChangePrice", 100);
				SERVICES_CHANGE_SEX_ITEM = getIntProperty(servicesSettings, "SexChangeItem", 4037);

				SERVICES_CHANGE_BASE_PRICE = getIntProperty(servicesSettings, "BaseChangePrice", 100);
				SERVICES_CHANGE_BASE_ITEM = getIntProperty(servicesSettings, "BaseChangeItem", 4037);

				SERVICES_SEPARATE_SUB_PRICE = getIntProperty(servicesSettings, "SeparateSubPrice", 100);
				SERVICES_SEPARATE_SUB_ITEM = getIntProperty(servicesSettings, "SeparateSubItem", 4037);

				SERVICES_CHANGE_NICK_COLOR_PRICE = getIntProperty(servicesSettings, "NickColorChangePrice", 100);
				SERVICES_CHANGE_NICK_COLOR_ITEM = getIntProperty(servicesSettings, "NickColorChangeItem", 4037);
				SERVICES_CHANGE_NICK_COLOR_LIST = getStringArray(servicesSettings, "NickColorChangeList", new String[] { "00FF00" }, ";");

				SERVICES_BASH_ENABLED = getBooleanProperty(servicesSettings, "BashEnabled", false);
				SERVICES_BASH_SKIP_DOWNLOAD = getBooleanProperty(servicesSettings, "BashSkipDownload", false);
				SERVICES_BASH_RELOAD_TIME = getIntProperty(servicesSettings, "BashReloadTime", 24);

				SERVICES_RATE_BONUS_ENABLED = getBooleanProperty(servicesSettings, "RateBonusEnabled", false);
				SERVICES_BONUS_APPLY_RATES_THEN_SERVICE_DISABLED = getBooleanProperty(servicesSettings, "RateBonusApplyRatesThenServiceDisabled", false);
				SERVICES_RATE_BONUS_PRICE = getIntArray(servicesSettings, "RateBonusPrice", new int[] { 1500 });
				SERVICES_RATE_BONUS_ITEM = getIntArray(servicesSettings, "RateBonusItem", new int[] { 4037 });
				SERVICES_RATE_BONUS_VALUE = getFloatArray(servicesSettings, "RateBonusValue", new float[] { (float) 1.25 });
				SERVICES_RATE_BONUS_DAYS = getIntArray(servicesSettings, "RateBonusTime", new int[] { 30 });
				SERVICES_RATE_BONUS_LUCK_EFFECT = getFloatProperty(servicesSettings, "RateBonusLuckEffect", 1.0);

				SERVICES_RATE_SPECIAL_ITEM_ID = getIntProperty(servicesSettings, "BONUS_ITEM", 4037);
				SERVICES_RATE_SPECIAL_ITEM_COUNT = getIntProperty(servicesSettings, "BONUS_PRICE", 50);
				SERVICES_RATE_SPECIAL_RATE = getIntProperty(servicesSettings, "BONUS_RATE", 50);
				SERVICES_RATE_SPECIAL_DAYS = getIntProperty(servicesSettings, "BONUS_DAYS", 7);
				SERVICES_RATE_SPECIAL_ENABLED = getBooleanProperty(servicesSettings, "BONUS_ENABLED", false);
				SERVICES_RATE_CREATE_CHARACTER_PA = getIntProperty(servicesSettings, "CreateCharPA", 3);
				SERVICES_RATE_CREATE_CHARACTER_PA_VALUE = getIntProperty(servicesSettings, "CreateCharPAValue", 2);
				
				SERVICES_NOBLESS_TW_ENABLED = getBooleanProperty(servicesSettings, "NoblessTWEnabled", true);
				SERVICES_NOBLESS_SELL_PRICE = getIntProperty(servicesSettings, "NoblessSellPrice", 1000);
				SERVICES_NOBLESS_SELL_ITEM = getIntProperty(servicesSettings, "NoblessSellItem", 4037);

				SERVICES_EXPAND_INVENTORY_ENABLED = getBooleanProperty(servicesSettings, "ExpandInventoryEnabled", false);
				SERVICES_EXPAND_INVENTORY_PRICE = getIntProperty(servicesSettings, "ExpandInventoryPrice", 1000);
				SERVICES_EXPAND_INVENTORY_ITEM = getIntProperty(servicesSettings, "ExpandInventoryItem", 4037);
				SERVICES_EXPAND_INVENTORY_MAX = getIntProperty(servicesSettings, "ExpandInventoryMax", 250);

				SERVICES_EXPAND_WAREHOUSE_ENABLED = getBooleanProperty(servicesSettings, "ExpandWarehouseEnabled", false);
				SERVICES_EXPAND_WAREHOUSE_PRICE = getIntProperty(servicesSettings, "ExpandWarehousePrice", 1000);
				SERVICES_EXPAND_WAREHOUSE_ITEM = getIntProperty(servicesSettings, "ExpandWarehouseItem", 4037);

				SERVICES_EXPAND_CWH_ENABLED = getBooleanProperty(servicesSettings, "ExpandCWHEnabled", false);
				SERVICES_EXPAND_CWH_PRICE = getIntProperty(servicesSettings, "ExpandCWHPrice", 1000);
				SERVICES_EXPAND_CWH_ITEM = getIntProperty(servicesSettings, "ExpandCWHItem", 4037);

				SERVICES_WINDOW_ENABLED = getBooleanProperty(servicesSettings, "WindowEnabled", false);
				SERVICES_WINDOW_PRICE = getIntProperty(servicesSettings, "WindowPrice", 1000);
				SERVICES_WINDOW_ITEM = getIntProperty(servicesSettings, "WindowItem", 4037);
				SERVICES_WINDOW_DAYS = getIntProperty(servicesSettings, "WindowDays", 7);
				SERVICES_WINDOW_MAX = getIntProperty(servicesSettings, "WindowMax", 3);

				SERVICES_SELLPETS = getProperty(servicesSettings, "SellPets", "");

				SERVICES_OFFLINE_TRADE_ALLOW = getBooleanProperty(servicesSettings, "AllowOfflineTrade", false);
				SERVICES_OFFLINE_TRADE_MIN_LEVEL = getIntProperty(servicesSettings, "OfflineMinLevel", 0);
				SERVICES_OFFLINE_TRADE_NAME_COLOR = getIntHexProperty(servicesSettings, "OfflineTradeNameColor", 0xB0FFFF);
				SERVICES_OFFLINE_TRADE_KICK_NOT_TRADING = getBooleanProperty(servicesSettings, "KickOfflineNotTrading", true);
				SERVICES_OFFLINE_TRADE_PRICE_ITEM = getIntProperty(servicesSettings, "OfflineTradePriceItem", 0);
				SERVICES_OFFLINE_TRADE_PRICE = getIntProperty(servicesSettings, "OfflineTradePrice", 0);
				SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = getLongProperty(servicesSettings, "OfflineTradeDaysToKick", 14) * 60 * 60 * 24;
				SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = getBooleanProperty(servicesSettings, "OfflineRestoreAfterRestart", true);

				SERVICES_NO_TRADE_ONLY_OFFLINE = getBooleanProperty(servicesSettings, "NoTradeOnlyOffline", false);
				SERVICES_TRADE_TAX = getFloatProperty(servicesSettings, "TradeTax", 0.0);
				SERVICES_OFFSHORE_TRADE_TAX = getFloatProperty(servicesSettings, "OffshoreTradeTax", 0.0);
				SERVICES_TRADE_TAX_ONLY_OFFLINE = getBooleanProperty(servicesSettings, "TradeTaxOnlyOffline", false);
				SERVICES_OFFSHORE_NO_CASTLE_TAX = getBooleanProperty(servicesSettings, "NoCastleTaxInOffshore", false);
				SERVICES_TRADE_RADIUS = getIntProperty(servicesSettings, "TradeRadius", 30);
				SERVICES_TRADE_ONLY_FAR = getBooleanProperty(servicesSettings, "TradeOnlyFar", false);

				SERVICES_GIRAN_HARBOR_ENABLED = getBooleanProperty(servicesSettings, "GiranHarborZone", false);
				SERVICES_PARNASSUS_ENABLED = getBooleanProperty(servicesSettings, "ParnassusZone", false);
				SERVICES_PARNASSUS_NOTAX = getBooleanProperty(servicesSettings, "ParnassusNoTax", false);
				SERVICES_PARNASSUS_PRICE = getLongProperty(servicesSettings, "ParnassusPrice", 500000);

				SERVICES_LOCK_ACCOUNT_IP = getBooleanProperty(servicesSettings, "LockAccountIP", false);
				SERVICES_CHANGE_PASSWORD = getBooleanProperty(servicesSettings, "ChangePassword", false);

				SERVICES_BUFFER_MIN_LVL = getIntProperty(servicesSettings, "BufferMinLvl", 1);
				SERVICES_BUFFER_MAX_LVL = getIntProperty(servicesSettings, "BufferMaxLvl", 90);
				SERVICES_BUFFER_PRICE = getIntProperty(servicesSettings, "BufferPrice", 5000);
				//SERVICES_BUFFER_SIEGE = getBooleanProperty(servicesSettings, "BufferSiege", false);
				SERVICES_BUFFER_ENABLED = getBooleanProperty(servicesSettings, "BufferEnabled", false);
				SERVICES_BUFFER_PET_ENABLED = getBooleanProperty(servicesSettings, "BufferPetEnabled", false);

				SERVICES_ALLOW_LOTTERY = getBooleanProperty(servicesSettings, "AllowLottery", false);
				SERVICES_LOTTERY_PRIZE = getIntProperty(servicesSettings, "LotteryPrize", 50000);
				SERVICES_ALT_LOTTERY_PRICE = getIntProperty(servicesSettings, "AltLotteryPrice", 2000);
				SERVICES_LOTTERY_TICKET_PRICE = getIntProperty(servicesSettings, "LotteryTicketPrice", 2000);
				SERVICES_LOTTERY_5_NUMBER_RATE = getFloatProperty(servicesSettings, "Lottery5NumberRate", 0.6);
				SERVICES_LOTTERY_4_NUMBER_RATE = getFloatProperty(servicesSettings, "Lottery4NumberRate", 0.4);
				SERVICES_LOTTERY_3_NUMBER_RATE = getFloatProperty(servicesSettings, "Lottery3NumberRate", 0.2);
				SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = getIntProperty(servicesSettings, "Lottery2and1NumberPrize", 200);

				SERVICES_ALLOW_ROULETTE = getBooleanProperty(servicesSettings, "AllowRoulette", false);
				SERVICES_ROULETTE_MIN_BET = getLongProperty(servicesSettings, "RouletteMinBet", 1);
				SERVICES_ROULETTE_MAX_BET = getLongProperty(servicesSettings, "RouletteMaxBet", Long.MAX_VALUE);

				SERVICES_REFERRAL_ENABLED = getBooleanProperty(servicesSettings, "AllowReferrals", false);
				SERVICES_REFERRAL_ITEM_1 = getIntProperty(servicesSettings, "ReferralsBonusId1", 57);
				SERVICES_REFERRAL_COUNT_1 = getLongProperty(servicesSettings, "ReferralsBonusCount1", 1000);
				SERVICES_REFERRAL_ITEM_2 = getIntProperty(servicesSettings, "ReferralsBonusId2", 57);
				SERVICES_REFERRAL_COUNT_2 = getLongProperty(servicesSettings, "ReferralsBonusCount2", 1000);
				
				ITEM_MOLL_ID_1 = getIntProperty(servicesSettings, "ItemMollId1", 1162);
				ITEM_MOLL_KOL_1 = getIntProperty(servicesSettings, "ItemMollKol1", 100);
				ITEM_MOLL_ID_2 = getIntProperty(servicesSettings, "ItemMollId2", 6673);
				ITEM_MOLL_KOL_2 = getIntProperty(servicesSettings, "ItemMollKol2", 200);
				ITEM_MOLL_ID_3 = getIntProperty(servicesSettings, "ItemMollId3", 4037);
				ITEM_MOLL_KOL_3 = getIntProperty(servicesSettings, "ItemMollKol3", 500);
				
				ALT_SIEGE_MOD = Boolean.parseBoolean(servicesSettings.getProperty("AltSiegeMode", "false"));
				
				DT_OF_SIEGE_ADEN = servicesSettings.getProperty("AdenSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_RUNE = servicesSettings.getProperty("RuneSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_SCHUTTGART = servicesSettings.getProperty("SchuttgartSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_GODDARD = servicesSettings.getProperty("GoddardSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_GIRAN = servicesSettings.getProperty("GiranSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_OREN = servicesSettings.getProperty("OrenSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_GLUDIO = servicesSettings.getProperty("GludioSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_DION = servicesSettings.getProperty("DionSiegeTime", "7:20:00").split(":");
				DT_OF_SIEGE_INNADRIL = servicesSettings.getProperty("InnadrilSiegeTime", "7:20:00").split(":");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + SERVICES_FILE + " File. [" + lastKey + "]");
			}

			// pvp config
			try
			{
				Properties pvpSettings = loadPropertiesFile(PVP_CONFIG_FILE);

				/* KARMA SYSTEM */
				KARMA_MIN_KARMA = getIntProperty(pvpSettings, "MinKarma", 240);
				KARMA_SP_DIVIDER = getIntProperty(pvpSettings, "SPDivider", 7);
				KARMA_LOST_BASE = getIntProperty(pvpSettings, "BaseKarmaLost", 0);

				KARMA_DROP_GM = getBooleanProperty(pvpSettings, "CanGMDropEquipment", false);
				KARMA_NEEDED_TO_DROP = getBooleanProperty(pvpSettings, "KarmaNeededToDrop", true);
				DROP_ITEMS_ON_DIE = getBooleanProperty(pvpSettings, "DropOnDie", false);
				DROP_ITEMS_AUGMENTED = getBooleanProperty(pvpSettings, "DropAugmented", false);

				KARMA_DROP_ITEM_LIMIT = getIntProperty(pvpSettings, "MaxItemsDroppable", 10);
				MIN_PK_TO_ITEMS_DROP = getIntProperty(pvpSettings, "MinPKToDropItems", 5);

				KARMA_RANDOM_DROP_LOCATION_LIMIT = getIntProperty(pvpSettings, "MaxDropThrowDistance", 70);

				KARMA_DROPCHANCE_BASE = getFloatProperty(pvpSettings, "ChanceOfPKDropBase", 20.);
				KARMA_DROPCHANCE_MOD = getFloatProperty(pvpSettings, "ChanceOfPKsDropMod", 1.);
				NORMAL_DROPCHANCE_BASE = getFloatProperty(pvpSettings, "ChanceOfNormalDropBase", 1.);
				DROPCHANCE_EQUIPPED_WEAPON = getIntProperty(pvpSettings, "ChanceOfDropWeapon", 3);
				DROPCHANCE_EQUIPMENT = getIntProperty(pvpSettings, "ChanceOfDropEquippment", 17);
				DROPCHANCE_ITEM = getIntProperty(pvpSettings, "ChanceOfDropOther", 80);

				KARMA_LIST_NONDROPPABLE_ITEMS = new GArray<Integer>();
				for(int id : getIntArray(pvpSettings, "ListOfNonDroppableItems", new int[] { 57, 1147, 425, 1146, 461, 10,
						2368, 7, 6, 2370, 2369, 3500, 3501, 3502, 4422, 4423, 4424, 2375, 6648, 6649, 6650, 6842, 6834, 6835, 6836,
						6837, 6838, 6839, 6840, 5575, 7694, 6841, 8181 }))
					KARMA_LIST_NONDROPPABLE_ITEMS.add(id);

				PVP_TIME = getIntProperty(pvpSettings, "PvPTime", 40000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + PVP_CONFIG_FILE + " File. [" + lastKey + "]");
			}

			// AI
			try
			{
				Properties aiSettings = loadPropertiesFile(AI_CONFIG_FILE);

				AI_TASK_DELAY = getIntProperty(aiSettings, "AiTaskDelay", 1000);
				AI_TASK_ACTIVE_DELAY = getIntProperty(aiSettings, "AiTaskActiveDelay", 10000);
				BLOCK_ACTIVE_TASKS = getBooleanProperty(aiSettings, "BlockActiveTasks", false);
				SAY_CASTING_SKILL_NAME = getBooleanProperty(aiSettings, "SayCastingSkillName", false);
				ALWAYS_TELEPORT_HOME = getBooleanProperty(aiSettings, "AlwaysTeleportHome", false);

				MOBSLOOTERS = getBooleanProperty(aiSettings, "MonstersLooters", false);

				MOBS_WEAPON_ENCHANT_MIN = getIntProperty(aiSettings, "MonstersWeaponEnchantMin", 0);
				MOBS_WEAPON_ENCHANT_MAX = getIntProperty(aiSettings, "MonstersWeaponEnchantMax", 0);
				MOBS_WEAPON_ENCHANT_CHANCE = getIntProperty(aiSettings, "MonstersWeaponEnchantChance", 0);

				RND_WALK = getBooleanProperty(aiSettings, "RndWalk", true);
				RND_WALK_RATE = getIntProperty(aiSettings, "RndWalkRate", 1);
				RND_ANIMATION_RATE = getIntProperty(aiSettings, "RndAnimationRate", 2);

				AGGRO_CHECK_INTERVAL = getIntProperty(aiSettings, "AggroCheckInterval", 250);

				MAX_DRIFT_RANGE = getIntProperty(aiSettings, "MaxDriftRange", 100);
				MAX_PURSUE_RANGE = getIntProperty(aiSettings, "MaxPursueRange", 4000);
				MAX_PURSUE_UNDERGROUND_RANGE = getIntProperty(aiSettings, "MaxPursueUndergoundRange", 2000);
				MAX_PURSUE_RANGE_RAID = getIntProperty(aiSettings, "MaxPursueRangeRaid", 5000);

				ALT_AI_KELTIRS = getBooleanProperty(aiSettings, "AltAiKeltirs", false);
				ALT_TELEPORTING_TOMA = getBooleanProperty(aiSettings, "AltTeleportingToma", false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + AI_CONFIG_FILE + " File. [" + lastKey + "]");
			}

			// Geodata config
			try
			{
				Properties geodataSettings = loadPropertiesFile(GEODATA_CONFIG_FILE);

				GEO_X_FIRST = getIntProperty(geodataSettings, "GeoFirstX", 11);
				GEO_Y_FIRST = getIntProperty(geodataSettings, "GeoFirstY", 10);
				GEO_X_LAST = getIntProperty(geodataSettings, "GeoLastX", 26);
				GEO_Y_LAST = getIntProperty(geodataSettings, "GeoLastY", 26);

				GEODATA_DEBUG = getBooleanProperty(geodataSettings, "GeodataDebug", false);
				PATHFIND_DEBUG = getBooleanProperty(geodataSettings, "PathfindDebug", false);
				GEOFILES_PATTERN = getProperty(geodataSettings, "GeoFilesPattern", "(\\d{2}_\\d{2})\\.l2j");
				ALLOW_DOORS = getBooleanProperty(geodataSettings, "AllowDoors", false);
				ALLOW_FALL_FROM_WALLS = getBooleanProperty(geodataSettings, "AllowFallFromWalls", false);
				ALLOW_KEYBOARD_MOVE = getBooleanProperty(geodataSettings, "AllowMoveWithKeyboard", true);
				COMPACT_GEO = getBooleanProperty(geodataSettings, "CompactGeoData", false);
				LOAD_MULTITHREADED = getBooleanProperty(geodataSettings, "MultiThreadedLoad", false);
				CLIENT_Z_SHIFT = getIntProperty(geodataSettings, "ClientZShift", 16);
				SIMPLE_PATHFIND_FOR_MOBS = getBooleanProperty(geodataSettings, "SimplePathFindForMobs", true);
				PATHFIND_BOOST = getIntProperty(geodataSettings, "PathFindBoost", 2);
				PATHFIND_DIAGONAL = getBooleanProperty(geodataSettings, "PathFindDiagonal", true);
				PATH_CLEAN = getBooleanProperty(geodataSettings, "PathClean", true);
				PATHFIND_MAX_Z_DIFF = getIntProperty(geodataSettings, "PathFindMaxZDiff", 32);
				MAX_Z_DIFF = getIntProperty(geodataSettings, "MaxZDiff", 64);
				MIN_LAYER_HEIGHT = getIntProperty(geodataSettings, "MinLayerHeight", 64);
				WEIGHT0 = getDoubleProperty(geodataSettings, "Weight0", 0.5);
				WEIGHT1 = getDoubleProperty(geodataSettings, "Weight1", 2.0);
				WEIGHT2 = getDoubleProperty(geodataSettings, "Weight2", 1.0);
				PathFindBuffers.initBuffers(getProperty(geodataSettings, "PathFindBuffers", "8x100;8x128;8x192;4x256;2x320;2x384;1x500"));

				VIEW_OFFSET = getIntProperty(geodataSettings, "ViewOffset", 1);
				DIV_BY = getIntProperty(geodataSettings, "DivBy", 2048);
				DIV_BY_FOR_Z = getIntProperty(geodataSettings, "DivByForZ", 1024);
				VERTICAL_SPLIT_REGIONS = getProperty(geodataSettings, "VerticalSplitRegions", "23_18");

				DELAYED_SPAWN = getBooleanProperty(geodataSettings, "DelayedSpawn", false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + GEODATA_CONFIG_FILE + " File. [" + lastKey + "]");
			}
			
			// residence settings
			try
			{
				Properties PcBangSettings = loadPropertiesFile(EVENT_PC_BANG_POINT_FILE);

				BANG_POINT_ENABLE = getBooleanProperty(PcBangSettings, "BangPointEnable", false); 
				BANG_POINT_MIN_LEVEL = getIntProperty(PcBangSettings, "PcBangPointMinLevel", 20); 
				BANG_POINT_MIN_COUNT = getIntProperty(PcBangSettings, "PcBangPointMinCount", 20); 
				BANG_POINT_MAX_COUNT = getIntProperty(PcBangSettings, "PcBangPointMaxCount", 200000); 
				if(BANG_POINT_MAX_COUNT < 0) 
					BANG_POINT_MAX_COUNT = 0; 
				BANG_POINT_DUAL_CHANCE = getIntProperty(PcBangSettings, "PcBangPointDualChance", 20); 
				if(BANG_POINT_DUAL_CHANCE < 0 || BANG_POINT_DUAL_CHANCE > 100) 
					BANG_POINT_DUAL_CHANCE = 1; 
				BANG_POINT_TIME_STAMP = getIntProperty(PcBangSettings, "PcBangPointTimeStamp", 900); 
				BANG_POINT_RATE = getFloatProperty(PcBangSettings, "PcBangPointsRate", 1.0); 
				if(BANG_POINT_RATE < 0) 
					BANG_POINT_RATE = 1; 
				BANG_POINT_DOUBLE_ENABLE = getBooleanProperty(PcBangSettings, "BangPointDoubleEnable", false); 
				BANG_RANDOM_POINT_ENABLE = getBooleanProperty(PcBangSettings, "BangPointRandomEnable", false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + EVENT_PC_BANG_POINT_FILE + " File. [" + lastKey + "]");
			}

			// hexid
			try
			{
				Properties Settings = loadPropertiesFile(HEXID_FILE);
				HEX_ID = new BigInteger(getProperty(Settings, "HexID"), 16).toByteArray();
			}
			catch(Exception e)
			{}

			// Events config
			try
			{
				Properties EventSettings = loadPropertiesFile(EVENTS);

				EVENT_CofferOfShadowsPriceRate = getFloatProperty(EventSettings, "CofferOfShadowsPriceRate", 1.);
				EVENT_CofferOfShadowsRewardRate = getFloatProperty(EventSettings, "CofferOfShadowsRewardRate", 1.);

				EVENT_RabbitsToRichesRewardRate = getFloatProperty(EventSettings, "RabbitsToRichesRewardRate", 1.);
				EVENT_TREASURE_SACK_CHANCE = getFloatProperty(EventSettings, "TREASURE_SACK_CHANCE", 10.);

				EVENT_LastHeroItemID = getIntProperty(EventSettings, "LastHero_bonus_id", 57);
				EVENT_LastHeroItemCOUNT = getFloatProperty(EventSettings, "LastHero_bonus_count", 5000.);
				EVENT_LastHeroTime = getIntProperty(EventSettings, "LastHero_time", 3);
				EVENT_LastHeroRate = getBooleanProperty(EventSettings, "LastHero_rate", true);
				EVENT_LastHeroChanceToStart = getIntProperty(EventSettings, "LastHero_ChanceToStart", 5);
				EVENT_LastHeroItemCOUNTFinal = getFloatProperty(EventSettings, "LastHero_bonus_count_final", 10000.);
				EVENT_LastHeroRateFinal = getBooleanProperty(EventSettings, "LastHero_rate_final", true);

				EVENT_TvTItemID = getIntProperty(EventSettings, "TvT_bonus_id", 57);
				EVENT_TvTItemCOUNT = getFloatProperty(EventSettings, "TvT_bonus_count", 5000.);
				EVENT_TvTTime = getIntProperty(EventSettings, "TvT_time", 3);
				EVENT_TvT_rate = getBooleanProperty(EventSettings, "TvT_rate", true);
				EVENT_TvTChanceToStart = getIntProperty(EventSettings, "TvT_ChanceToStart", 5);

				EVENT_CtFItemID = getIntProperty(EventSettings, "CtF_bonus_id", 57);
				EVENT_CtFItemCOUNT = getFloatProperty(EventSettings, "CtF_bonus_count", 5000.);
				EVENT_CtFTime = getIntProperty(EventSettings, "CtF_time", 3);
				EVENT_CtF_rate = getBooleanProperty(EventSettings, "CtF_rate", true);
				EVENT_CtFChanceToStart = getIntProperty(EventSettings, "CtF_ChanceToStart", 5);

				EVENT_TFH_POLLEN_CHANCE = getFloatProperty(EventSettings, "TFH_POLLEN_CHANCE", 5.);

				EVENT_GLITTMEDAL_NORMAL_CHANCE = getFloatProperty(EventSettings, "MEDAL_CHANCE", 10.);
				EVENT_GLITTMEDAL_GLIT_CHANCE = getFloatProperty(EventSettings, "GLITTMEDAL_CHANCE", 0.1);

				EVENT_L2DAY_LETTER_CHANCE = getFloatProperty(EventSettings, "L2DAY_LETTER_CHANCE", 1.);
				EVENT_CHANGE_OF_HEART_CHANCE = getFloatProperty(EventSettings, "EVENT_CHANGE_OF_HEART_CHANCE", 5.);

				EVENT_BOUNTY_HUNTERS_ENABLED = getBooleanProperty(EventSettings, "BountyHuntersEnabled", true);

				EVENT_SAVING_SNOWMAN_LOTERY_PRICE = getLongProperty(EventSettings, "SavingSnowmanLoteryPrice", 50000);
				EVENT_SAVING_SNOWMAN_REWARDER_CHANCE = getIntProperty(EventSettings, "SavingSnowmanRewarderChance", 2);

				EVENT_MARCH8_DROP_CHANCE = getFloatProperty(EventSettings, "March8DropChance", 10.);
				EVENT_MARCH8_PRICE_RATE = getFloatProperty(EventSettings, "March8PriceRate", 1.);

				ENCHANT_MASTER_DROP_CHANCE = getFloatProperty(EventSettings, "EnchMasterDropChance", 1.);
				EVENT_MASTEROFENCHANTING_USE_RATES = getBooleanProperty(EventSettings, "EnchMasterUseAdenaRates", true);
				ENCHANT_MASTER_STAFF_PRICE = getLongProperty(EventSettings, "EnchMasterStaffPrice", 1000);
				ENCHANT_MASTER_STAFF_PRICE = (long) (EVENT_MASTEROFENCHANTING_USE_RATES ? ENCHANT_MASTER_STAFF_PRICE * RATE_DROP_ADENA : ENCHANT_MASTER_STAFF_PRICE);
				ENCHANT_MASTER_24SCROLL_PRICE = getLongProperty(EventSettings, "EnchMaster24ScrollPrice", 6000);
				ENCHANT_MASTER_24SCROLL_PRICE = (long) (EVENT_MASTEROFENCHANTING_USE_RATES ? ENCHANT_MASTER_24SCROLL_PRICE * RATE_DROP_ADENA : ENCHANT_MASTER_24SCROLL_PRICE);
				ENCHANT_MASTER_1SCROLL_PRICE = getLongProperty(EventSettings, "EnchMaster1ScrollPrice", 77777);
				ENCHANT_MASTER_1SCROLL_PRICE = (long) (EVENT_MASTEROFENCHANTING_USE_RATES ? ENCHANT_MASTER_1SCROLL_PRICE * RATE_DROP_ADENA : ENCHANT_MASTER_1SCROLL_PRICE);
				ENCHANT_MASTER_PRICE_ID = getIntProperty(EventSettings, "PriceEnchantMasterId", 57);

				EVENT_TRICK_OF_TRANS_CHANCE = getFloatProperty(EventSettings, "TRICK_OF_TRANS_CHANCE", 10.);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + EVENTS + " File. [" + lastKey + "]");
			}

			try
			{
				Properties olympSettings = loadPropertiesFile(OLYMPIAD);

				ENABLE_OLYMPIAD = getBooleanProperty(olympSettings, "EnableOlympiad", true);
				ENABLE_OLYMPIAD_SPECTATING = getBooleanProperty(olympSettings, "EnableOlympiadSpectating", true);
				ALT_OLY_START_TIME = getIntProperty(olympSettings, "AltOlyStartTime", 18);
				ALT_OLY_MIN = getIntProperty(olympSettings, "AltOlyMin", 00);
				ALT_OLY_CPERIOD = getLongProperty(olympSettings, "AltOlyCPeriod", 21600000);
				ALT_OLY_WPERIOD = getLongProperty(olympSettings, "AltOlyWPeriod", 604800000);
				ALT_OLY_VPERIOD = getLongProperty(olympSettings, "AltOlyVPeriod", 43200000);
				CLASS_GAME_MIN = getIntProperty(olympSettings, "ClassGameMin", 5);
				NONCLASS_GAME_MIN = getIntProperty(olympSettings, "NonClassGameMin", 9);
				RANDOM_TEAM_GAME_MIN = getIntProperty(olympSettings, "RandomTeamGameMin", 12);
				TEAM_GAME_MIN = getIntProperty(olympSettings, "TeamGameMin", 4);

				ALT_OLY_REG_DISPLAY = Integer.parseInt(olympSettings.getProperty("AltOlyRegistrationDisplayNumber", "100"));
				ALT_OLY_BATTLE_REWARD_ITEM = Integer.parseInt(olympSettings.getProperty("AltOlyBattleRewItem", "13722"));
				ALT_OLY_CLASSED_RITEM_C = Integer.parseInt(olympSettings.getProperty("AltOlyClassedRewItemCount", "50"));
				ALT_OLY_NONCLASSED_RITEM_C = Integer.parseInt(olympSettings.getProperty("AltOlyNonClassedRewItemCount", "40"));
				ALT_OLY_RANDOM_TEAM_RITEM_C = Integer.parseInt(olympSettings.getProperty("AltOlyRandomTeamRewItemCount", "30"));
				ALT_OLY_TEAM_RITEM_C = Integer.parseInt(olympSettings.getProperty("AltOlyTeamRewItemCount", "50"));
				ALT_OLY_COMP_RITEM = Integer.parseInt(olympSettings.getProperty("AltOlyCompRewItem", "13722"));
				ALT_OLY_GP_PER_POINT = Integer.parseInt(olympSettings.getProperty("AltOlyGPPerPoint", "1000"));
				ALT_OLY_HERO_POINTS = Integer.parseInt(olympSettings.getProperty("AltOlyHeroPoints", "180"));
				ALT_OLY_RANK1_POINTS = Integer.parseInt(olympSettings.getProperty("AltOlyRank1Points", "120"));
				ALT_OLY_RANK2_POINTS = Integer.parseInt(olympSettings.getProperty("AltOlyRank2Points", "80"));
				ALT_OLY_RANK3_POINTS = Integer.parseInt(olympSettings.getProperty("AltOlyRank3Points", "55"));
				ALT_OLY_RANK4_POINTS = Integer.parseInt(olympSettings.getProperty("AltOlyRank4Points", "35"));
				ALT_OLY_RANK5_POINTS = Integer.parseInt(olympSettings.getProperty("AltOlyRank5Points", "20"));
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + EVENTS + " File. [" + lastKey + "]");
			}
			abuseLoad();
			loadGMAccess();
			_log.info("loading xml GMAccess");
			if(ADVIPSYSTEM)
				ipsLoad();
		}
		else if(Server.SERVER_MODE == Server.MODE_LOGINSERVER)
		{
			_log.info("loading login config");
			try
			{
				Properties serverSettings = loadPropertiesFile(LOGIN_CONFIGURATION_FILE);

				loadAntiFlood(serverSettings);
				SELECTOR_SLEEP_TIME = getIntProperty(serverSettings, "SelectorSleepTime", 5);
				LOGIN_HOST = getProperty(serverSettings, "LoginserverHostname", "127.0.0.1");
				GAME_SERVER_LOGIN_PORT = getIntProperty(serverSettings, "LoginPort", 9013);
				GAME_SERVER_LOGIN_HOST = getProperty(serverSettings, "LoginHost", "127.0.0.1");
				PORT_LOGIN = getIntProperty(serverSettings, "LoginserverPort", 2106);
				LOGIN_WATCHDOG_TIMEOUT = getIntProperty(serverSettings, "LoginWatchdogTimeout", 15000);

				DEFAULT_PASSWORD_ENCODING = getProperty(serverSettings, "DefaultPasswordEncoding", "Whirlpool");
				LEGACY_PASSWORD_ENCODING = getProperty(serverSettings, "LegacyPasswordEncoding", "SHA1;DES");
				DOUBLE_WHIRPOOL_SALT = getProperty(serverSettings, "DoubleWhirlpoolSalt", "blablabla");

				LOGIN_BLOWFISH_KEYS = getIntProperty(serverSettings, "BlowFishKeys", 20);
				LOGIN_RSA_KEYPAIRS = getIntProperty(serverSettings, "RSAKeyPairs", 10);

				COMBO_MODE = getBooleanProperty(serverSettings, "ComboMode", false);
				LOGIN_DEBUG = getBooleanProperty(serverSettings, "Debug", false);

				ACCEPT_NEW_GAMESERVER = getBooleanProperty(serverSettings, "AcceptNewGameServer", true);

				LOGIN_TRY_BEFORE_BAN = getIntProperty(serverSettings, "LoginTryBeforeBan", 10);
				LOGIN_PING = getBooleanProperty(serverSettings, "PingServer", true);
				LOGIN_PING_TIME = getIntProperty(serverSettings, "WaitPingTime", 5);

				SHOW_LICENCE = getBooleanProperty(serverSettings, "ShowLicence", true);
				SQL_LOG = getBooleanProperty(serverSettings, "SqlLog", false);
				AUTO_CREATE_ACCOUNTS = getBooleanProperty(serverSettings, "AutoCreateAccounts", true);
				IP_UPDATE_TIME = getIntProperty(serverSettings, "IpUpdateTime", 15);
				ANAME_TEMPLATE = getProperty(serverSettings, "AnameTemplate", "[A-Za-z0-9]{3,14}");
				APASSWD_TEMPLATE = getProperty(serverSettings, "ApasswdTemplate", "[A-Za-z0-9]{5,16}");
				LOGIN_GG_CHECK = getBooleanProperty(serverSettings, "GGCheck", false);
				FAKE_LOGIN = getBooleanProperty(serverSettings, "FakeLogin", true);
				LRESTART_TIME = getIntProperty(serverSettings, "AutoRestart", -1);

				String internalIpList = getProperty(serverSettings, "InternalIpList", "127.0.0.1,192.168.0.0-192.168.255.255,10.0.0.0-10.255.255.255,172.16.0.0-172.16.31.255");
				if(internalIpList.startsWith("NetList@"))
				{
					INTERNAL_NETLIST = new NetList();
					INTERNAL_NETLIST.LoadFromFile(internalIpList.replaceFirst("NetList@", ""));
					_log.info("Loaded " + INTERNAL_NETLIST.NetsCount() + " Internal Nets");
				}
				else
				{
					INTERNAL_IP = new GArray<String>();
					INTERNAL_IP.addAll(Arrays.asList(internalIpList.split(",")));
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + LOGIN_CONFIGURATION_FILE + " File. [" + lastKey + "]");
			}
			
			// Login Protect
			try
			{
				Properties serverSettings = loadPropertiesFile(LOGIN_PROTECT_FILE);
				loadAntiFlood(serverSettings);
				BAN_CLEAR = getLongProperty(serverSettings, "ClearBans", 120000);
				GARBAGE_CLEAR = getLongProperty(serverSettings, "ClearMemory", 600000);
				BAN_TIME = getLongProperty(serverSettings, "FloodBan", 120000);
				BRUTE_BAN = getLongProperty(serverSettings, "BruteBan", 600000);
				MAX_BRUTE = getIntProperty(serverSettings, "MaxFails", 4);
				INTERVAL = getLongProperty(serverSettings, "LoginInterval", 20000);

				LOAD_FIREWALL = getBooleanProperty(serverSettings, "LoadIptablesRules", false);
				AllowCMD = getBooleanProperty(serverSettings, "AllowCMD", false);
				CMDLOGIN = getProperty(serverSettings, "ExecCMD", "None");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + LOGIN_PROTECT_FILE + " File. [" + lastKey + "]");
			}
			
			//	      telnet
			try
			{
				Properties telnetSettings = loadPropertiesFile(LOGIN_TELNET_FILE);
				IS_LOGIN_TELNET_ENABLED = getBooleanProperty(telnetSettings, "EnableTelnet", false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + TELNET_FILE + " File. [" + lastKey + "]");
			}
		}
		else
			_log.severe("Could not Load Config: server mode was not set");
		loadBlackIps();
		loadWhiteIps();
	}

	// it has no instancies
	private Config()
	{}

	public static void saveHexid(String string)
	{
		saveHexid(string, HEXID_FILE);
	}

	
	
	
	private static void loadBlackIps()
	{
		LineNumberReader lnr = null;
		BufferedReader br = null;
		FileReader fr = null;
		try
		{
			File Data = new File("./config/ips_black.txt");
			if (!Data.exists())
				return;
			fr = new FileReader(Data);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			String line;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || (line.startsWith("#"))) 
				{
					continue;
				}
				blackIPs.add(line);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (fr != null)
					fr.close();
				if (br != null)
					br.close();
				if (lnr != null)
					lnr.close();
			}
			catch (Exception e1) 
			{
			}
		}
			_log.severe("Black IPs: loaded.");
	}
	
	private static void loadWhiteIps()
	{
		LineNumberReader lnr = null;
		BufferedReader br = null;
		FileReader fr = null;
		try
		{
			File Data = new File("./config/ips_white.txt");
			if (!Data.exists())
				return;
			fr = new FileReader(Data);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			String line;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || (line.startsWith("#"))) {
					continue;
				}
				whiteIPs.add(line);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (fr != null)
					fr.close();
				if (br != null)
					br.close();
				if (lnr != null)
					lnr.close();
			}
			catch (Exception e1) 
			{
			}
		}
		_log.severe("White IPs: loaded.");
	}
	
	
	
	
	
	public static void saveHexid(String string, String fileName)
	{
		try
		{
			Properties hexSetting = new Properties();
			File file = new File(fileName);
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("HexID", string);
			hexSetting.store(out, "the hexID to auth into login");
			out.close();
		}
		catch(Exception e)
		{
			System.out.println("Failed to save hex id to " + fileName + " File.");
			e.printStackTrace();
		}
	}

	public static void abuseLoad()
	{
		GArray<Pattern> tmp = new GArray<Pattern>();

		LineNumberReader lnr = null;
		try
		{
			String line;

			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(Server.PhoenixHomeDir + MAT_CONFIG_FILE), "UTF-8"));

			while((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}

			MAT_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Abuse: Loaded " + MAT_LIST.length + " abuse words.");
		}
		catch(IOException e1)
		{
			_log.warning("Error reading abuse: " + e1);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e2)
			{
				// nothing
			}
		}
	}

	private static void ipsLoad()
	{
		try
		{
			Properties ipsSettings = loadPropertiesFile(ADV_IP_FILE);

			String NetMask;
			String ip;
			for(int i = 0; i < ipsSettings.size() / 2; i++)
			{
				NetMask = getProperty(ipsSettings, "NetMask" + (i + 1));
				ip = getProperty(ipsSettings, "IPAdress" + (i + 1));
				for(String mask : NetMask.split(","))
				{
					AdvIP advip = new AdvIP();
					advip.ipadress = ip;
					advip.ipmask = mask.split("/")[0];
					advip.bitmask = mask.split("/")[1];
					GAMEIPS.add(advip);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ADV_IP_FILE + " File. [" + lastKey + "]");
		}
	}

	public static void loadGMAccess()
	{
		gmlist.clear();
		loadGMAccess(new File(GM_PERSONAL_ACCESS_FILE));
		File dir = new File(GM_ACCESS_FILES_DIR);
		if(!dir.exists() || !dir.isDirectory())
		{
			_log.config("Dir " + dir.getAbsolutePath() + " not exists.");
			return;
		}
		for(File f : dir.listFiles())
			// hidden файлы НЕ игнорируем
			if(!f.isDirectory() && f.getName().endsWith(".xml"))
				loadGMAccess(f);
	}

	public static void loadGMAccess(File file)
	{
		try
		{
			Field fld;
			//File file = new File(filename);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);

			for(Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
				for(Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if(!n.getNodeName().equalsIgnoreCase("char"))
						continue;

					PlayerAccess pa = new PlayerAccess();
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						Class<?> cls = pa.getClass();
						String node = d.getNodeName();

						if(node.equalsIgnoreCase("#text"))
							continue;
						try
						{
							fld = cls.getField(node);
						}
						catch(NoSuchFieldException e)
						{
							_log.info("Not found desclarate ACCESS name: " + node + " in XML Player access Object");
							continue;
						}

						if(fld.getType().getName().equalsIgnoreCase("boolean"))
							fld.setBoolean(pa, Boolean.parseBoolean(d.getAttributes().getNamedItem("set").getNodeValue()));
						else if(fld.getType().getName().equalsIgnoreCase("int"))
							fld.setInt(pa, Integer.valueOf(d.getAttributes().getNamedItem("set").getNodeValue()));
					}
					gmlist.put(pa.PlayerID, pa);
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + GM_PERSONAL_ACCESS_FILE + " File. [" + file.getPath() + "]");
		}
	}

	private static String getProperty(Properties prop, String name)
	{
		lastKey = name;
		String result = prop.getProperty(name.trim());
		lastKey += " = " + result;
		return result;
	}

	private static String getProperty(Properties prop, String name, String _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : s;
	}

	private static int getIntProperty(Properties prop, String name, int _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Integer.parseInt(s.trim());
	}

	private static int getIntHexProperty(Properties prop, String name, int _default)
	{
		String s = getProperty(prop, name);
		if(s == null)
			return _default;
		s = s.trim();
		if(!s.startsWith("0x"))
			s = "0x" + s;
		return Integer.decode(s);
	}

	private static long getLongProperty(Properties prop, String name, long _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Long.parseLong(s.trim());
	}

	private static byte getByteProperty(Properties prop, String name, byte _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Byte.parseByte(s.trim());
	}

	private static byte getByteProperty(Properties prop, String name, int _default)
	{
		return getByteProperty(prop, name, (byte) _default);
	}

	private static boolean getBooleanProperty(Properties prop, String name, boolean _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Boolean.parseBoolean(s.trim());
	}

	private static float getFloatProperty(Properties prop, String name, float _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Float.parseFloat(s.trim());
	}

	private static float getFloatProperty(Properties prop, String name, double _default)
	{
		return getFloatProperty(prop, name, (float) _default);
	}

	private static double getDoubleProperty(Properties prop, String name, double _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Double.parseDouble(s.trim());
	}

	private static int[] getIntArray(Properties prop, String name, int[] _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Util.parseCommaSeparatedIntegerArray(s.trim());
	}

	private static float[] getFloatArray(Properties prop, String name, float[] _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Util.parseCommaSeparatedFloatArray(s.trim());
	}

	private static String[] getStringArray(Properties prop, String name, String[] _default, String delimiter)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : s.split(delimiter);
	}

	@SuppressWarnings("unused")
	private static String[] getStringArray(Properties prop, String name, String[] _default)
	{
		return getStringArray(prop, name, _default, ",");
	}

	private static Pattern[] getContainsNoCasePatternArray(Properties prop, String name, String[] _default, boolean ExPattern, String delimiter)
	{
		GArray<Pattern> tempPatterns = new GArray<Pattern>();
		for(String s : getStringArray(prop, name, _default, delimiter))
			if(!s.isEmpty())
			{
				if(ExPattern)
					s = Strings.joinStrings("[\\\\_ *@.\\/\\-]*", s.split(""));
				tempPatterns.add(Pattern.compile(".*" + s + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}
		return tempPatterns.toArray(new Pattern[tempPatterns.size()]);
	}

	private static Pattern[] getContainsNoCasePatternArray(Properties prop, String name, String[] _default, boolean ExPattern)
	{
		return getContainsNoCasePatternArray(prop, name, _default, ExPattern, ",");
	}

	public static String HandleConfig(L2Player activeChar, String s)
	{
		String[] parameter = s.split("=");
		String pName = parameter[0].trim();

		try
		{
			Field field = Config.class.getField(pName);
			if(parameter.length < 2)
				return pName + "=" + field.get(null);
			String pValue = parameter[1].trim();
			if(setField(activeChar, field, pValue))
				return "Config field set succesfully: " + pName + "=" + field.get(null);
			return "Config field [" + pName + "] set fail!";
		}
		catch(NoSuchFieldException e)
		{
			return "Parameter " + pName + " not found";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "Exception on HandleConfig";
		}
	}

	private static boolean setField(L2Player activeChar, Field field, String param)
	{
		try
		{
			field.setBoolean(null, Boolean.parseBoolean(param));
		}
		catch(Exception e)
		{
			try
			{
				field.setInt(null, Integer.parseInt(param));
			}
			catch(Exception e1)
			{
				try
				{
					field.setLong(null, Long.parseLong(param));
				}
				catch(Exception e2)
				{
					try
					{
						field.setDouble(null, Double.parseDouble(param));
					}
					catch(Exception e3)
					{
						try
						{
							field.setFloat(null, Float.parseFloat(param));
						}
						catch(Exception e4)
						{
							try
							{
								field.set(null, param);
							}
							catch(Exception e5)
							{
								if(activeChar != null)
									activeChar.sendMessage("Error while set field: " + param + " " + e.getMessage());
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	public static Properties loadPropertiesFile(String pathname) throws IOException
	{
		return loadPropertiesFile(new File(pathname));
	}

	public static Properties loadPropertiesFile(File f) throws IOException
	{
		Properties result = new Properties();
		InputStream is = new FileInputStream(f);
		InputStreamReader reader = new InputStreamReader(is, "UTF-8");
		result.load(reader);
		is.close();
		return result;
	}

	public static boolean containsMat(String s)
	{
		for(Pattern pattern : MAT_LIST)
			if(pattern.matcher(s).matches())
				return true;
		return false;
	}

	public static float getRateAdena(L2Player activeChar)
	{
		return RATE_DROP_ADENA * (activeChar == null ? 1 : activeChar.getRateAdena()) * RATE_DROP_ADENA_MULT_MOD + RATE_DROP_ADENA_STATIC_MOD;
	}
}