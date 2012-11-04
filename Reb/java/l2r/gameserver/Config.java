package l2r.gameserver;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import l2r.commons.configuration.ExProperties;
import l2r.commons.net.AdvIP;
import l2r.commons.net.nio.impl.SelectorConfig;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.model.actor.instances.player.Bonus;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.model.base.PlayerAccess;
import l2r.gameserver.network.loginservercon.ServerType;
import l2r.gameserver.utils.AddonsConfig;
import l2r.gameserver.utils.GArray;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Config
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class);

	public static final int NCPUS = Runtime.getRuntime().availableProcessors();
	/** Configuration files */
	public static final String OTHER_CONFIG_FILE = "config/mod/other.properties";
	public static final String RESIDENCE_CONFIG_FILE = "config/residence.properties";
	public static final String SPOIL_CONFIG_FILE = "config/spoil.properties";
	public static final String ALT_SETTINGS_FILE = "config/altsettings.properties";
	public static final String FORMULAS_CONFIGURATION_FILE = "config/formulas.properties";
	public static final String PVP_CONFIG_FILE = "config/pvp.properties";
	public static final String TELNET_CONFIGURATION_FILE = "config/telnet.properties";
	public static final String CONFIGURATION_FILE = "config/server.properties";
	public static final String AI_CONFIG_FILE = "config/ai.properties";
	public static final String GEODATA_CONFIG_FILE = "config/geodata.properties";
	public static final String EVENTS_CONFIG_FILE = "config/events/events.properties";
	public static final String SERVICES_FILE = "config/services/services.properties";
	public static final String OLYMPIAD = "config/olympiad.properties";
	public static final String DEVELOP_FILE = "config/develop.properties";
	public static final String EXT_FILE = "config/ext.properties";
	public static final String RATES_FILE = "config/rates.properties";
	public static final String WEDDING_FILE = "config/services/Wedding.properties";
	public static final String CHAT_FILE = "config/chat.properties";
	public static final String NPC_FILE = "config/npc.properties";
	public static final String BOSS_FILE = "config/boss.properties";
	public static final String PREMIUM_FILE = "config/services/premium.properties";
	public static final String TOP_FILE = "config/services/tops.properties";
	public static final String EPIC_BOSS_FILE = "config/epic.properties";
	public static final String PAYMENT_FILE = "config/services/payment.properties";
	public static final String ITEM_USE_FILE = "config/UseItems.properties";
	public static final String INSTANCES_FILE = "config/instances.properties";
	public static final String ITEMS_FILE = "config/items.properties";
	public static final String ANUSEWORDS_CONFIG_FILE = "config/Abusewords.txt";
	public static final String ADV_IP_FILE = "config/advipsystem.properties";

	public static final String GM_PERSONAL_ACCESS_FILE = "config/GMAccess.xml";
	public static final String GM_ACCESS_FILES_DIR = "config/GMAccess.d/";
	
	public static final String COMMANDS_CONFIG_FILE = "config/mod/commands.properties";
	public static final String TALKING_GUARD_CONFIG_FILE = "config/mod/TalkingGuard.properties";
	public static final String BUFFER_CONFIG_FILE = "config/services/Buffer.properties";
	public static final String ACC_MOVE_FILE = "config/services/Buffer.properties";
	
	/** 4ipolino */
	/** events */
	public static final String EVENT_APRIL_FOOLS_CONFIG_FILE = "config/events/AprilFools.properties";
	public static final String EVENT_CAPTURE_THE_FLAG_CONFIG_FILE = "config/events/CaptureTheFlag.properties";
	public static final String EVENT_CHANGE_OF_HEART_CONFIG_FILE = "config/events/ChangeOfHeart.properties";
	public static final String EVENT_COFFER_OF_SHADOWS_CONFIG_FILE = "config/events/CofferOfShadows.properties";
    public static final String EVENT_FIGHT_CLUB_FILE = "config/events/FightClub.properties";
	public static final String EVENT_GLITTERING_MEDAL_CONFIG_FILE = "config/events/GlitteringMedal.properties";
	public static final String EVENT_L2_DAY_CONFIG_FILE = "config/events/L2Day.properties";
	public static final String EVENT_LAST_HERO_CONFIG_FILE = "config/events/LastHero.properties";
	public static final String EVENT_MARCH_8_CONFIG_FILE = "config/events/March8.properties";
	public static final String EVENT_MASTER_OF_ENCHANING_CONFIG_FILE = "config/events/MasterOfEnchaning.properties";
	public static final String EVENT_OTHER_EVENTS_CONFIG_FILE = "config/events/OtherEvents.properties";
	public static final String EVENT_SAVING_SNOWMAN_CONFIG_FILE = "config/events/SavingSnowman.properties";
	public static final String EVENT_TEAM_VS_TEAM_CONFIG_FILE = "config/events/TeamVSTeam.properties";
	public static final String EVENT_THE_FALL_HARVEST_CONFIG_FILE = "config/events/TheFallHarvest.properties";
	public static final String EVENT_TRICK_OF_TRANSMUTATION_CONFIG_FILE = "config/events/TrickOfTransmutation.properties";
	public static final String EVENT_WORLD_DROP_CONFIG_FILE = "config/events/WorldDrop.properties";
	public static final String PC_CONFIG_FILE = "config/events/PcBangPoints.properties";
	public static final String DEFENSE_TOWNS_CONFIG_FILE = "config/events/DefenseTowns.properties";
	public static final String VIKTORINA_CONFIG_FILE = "config/events/Victorina.properties";
	public static final String PVP_MOD_CONFIG_FILE = "config/mod/PvPmod.properties";
	
	
	/** Community PvP */
	public static final String BOARD_MANAGER_CONFIG_FILE = "config/CommunityPvP/board_manager.properties";
	public static final String BUFFER_MANAGER_CONFIG_FILE = "config/CommunityPvP/buffer_manager.properties";
	public static final String CLASS_MASTER_CONFIG_FILE = "config/CommunityPvP/class_master.properties";
	public static final String SHOP_MANAGER_CONFIG_FILE = "config/CommunityPvP/shop_manager.properties";
	public static final String TELEPORT_MANAGER_CONFIG_FILE = "config/CommunityPvP/teleport_manager.properties";
	public static final String ENCHANT_CB_CONFIG_FILE = "config/CommunityPvP/EnchantCB.properties";

	public static int HTM_CACHE_MODE;

	public static boolean ALLOW_ADDONS_CONFIG;

	public static boolean ALLOW_IP_LOCK;
	public static boolean ALLOW_HWID_LOCK;
	public static int HWID_LOCK_MASK;
	
	/** GameServer ports */
	public static int[] PORTS_GAME;
	public static String GAMESERVER_HOSTNAME;
	public static boolean ADVIPSYSTEM;
	public static List<AdvIP> GAMEIPS = new ArrayList<AdvIP>();
	public static String DATABASE_DRIVER;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIMEOUT;
	public static int DATABASE_IDLE_TEST_PERIOD;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;

	// Database additional options
	public static boolean AUTOSAVE;

	public static long USER_INFO_INTERVAL;
	public static boolean BROADCAST_STATS_INTERVAL;
	public static long BROADCAST_CHAR_INFO_INTERVAL;

	public static int EFFECT_TASK_MANAGER_COUNT;

	public static int MAXIMUM_ONLINE_USERS;
	public static int ONLINE_PLUS;

	public static boolean DONTLOADSPAWN;
	public static boolean DONTLOADQUEST;
	public static int MAX_REFLECTIONS_COUNT;

	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static int MAP_MIN_Z;
	public static int MAP_MAX_Z;

	/** ChatBan */
	public static int CHAT_MESSAGE_MAX_LEN;
	public static boolean ABUSEWORD_BANCHAT;
	public static int[] BAN_CHANNEL_LIST = new int[18];
	public static boolean ABUSEWORD_REPLACE;
	public static String ABUSEWORD_REPLACE_STRING;
	public static int ABUSEWORD_BANTIME;
	public static Pattern[] ABUSEWORD_LIST = {};
	public static boolean BANCHAT_ANNOUNCE;
	public static boolean BANCHAT_ANNOUNCE_FOR_ALL_WORLD;
	public static boolean BANCHAT_ANNOUNCE_NICK;

	public static int[] CHATFILTER_CHANNELS = new int[18];
	public static int CHATFILTER_MIN_LEVEL = 0;
	public static int CHATFILTER_WORK_TYPE = 1;

	public static boolean SAVING_SPS;
	public static boolean MANAHEAL_SPS_BONUS;

	public static int ALT_ADD_RECIPES;
	public static int ALT_MAX_ALLY_SIZE;

	public static int ALT_PARTY_DISTRIBUTION_RANGE;
	public static double[] ALT_PARTY_BONUS;
	public static double ALT_ABSORB_DAMAGE_MODIFIER;
	public static boolean ALT_ALL_PHYS_SKILLS_OVERHIT;

	public static double ALT_POLE_DAMAGE_MODIFIER;

	public static boolean ALT_REMOVE_SKILLS_ON_DELEVEL;
	public static boolean ALT_USE_BOW_REUSE_MODIFIER;

	public static boolean ALT_VITALITY_ENABLED;
	public static double ALT_VITALITY_RATE;
	public static double ALT_VITALITY_CONSUME_RATE;
	public static int ALT_VITALITY_RAID_BONUS;
	public static final int[] VITALITY_LEVELS = { 240, 2000, 13000, 17000, 20000 };

	public static boolean ALLOW_BBS_WAREHOUSE;
	public static boolean BBS_WAREHOUSE_ALLOW_PK;
	/** Community Board PVP */
	public static boolean BBS_PVP_CB_ENABLED;
	public static boolean BBS_PVP_CB_ABNORMAL;
	public static boolean BBS_PVP_SUB_MANAGER_ALLOW;
	public static boolean BBS_PVP_SUB_MANAGER_PIACE;
	public static boolean BBS_PVP_TELEPORT_ENABLED;
	public static int BBS_PVP_TELEPORT_POINT_PRICE;
	public static int BBS_PVP_TELEPORT_MAX_POINT_COUNT;
	public static boolean BBS_PVP_ALLOW_SELL;
	public static boolean BBS_PVP_ALLOW_BUY;
	public static boolean BBS_PVP_ALLOW_AUGMENT;
	/** Settings of CommunityBoard Buffer */
	public static boolean BBS_PVP_BUFFER_ENABLED;
	public static boolean BBS_PVP_BUFER_ONE_BUFF_PET;
	public static long BBS_PVP_BUFFER_ALT_TIME;
	public static int BBS_PVP_BUFFER_PRICE_ITEM;
	public static int BBS_PVP_BUFFER_PRICE_ONE;
	public static int BBS_PVP_BUFFER_BUFFS_PER_PAGE;
	public static int BBS_PVP_BUFFER_BUFFS_PER_SET;	
	public static int BBS_PVP_BUFFER_TASK_DELAY;
	public static int BBS_PVP_BUFFER_PRICE_MOD_GRP;
	public static int BBS_PVP_BUFFER_MIN_LVL;	
	public static int BBS_PVP_BUFFER_MAX_LVL;
	public static boolean BBS_PVP_BUFFER_ALLOW_SIEGE;
	public static boolean BBS_PVP_BUFFER_ALOWED_INST_BUFF;
	public static boolean BBS_PVP_BUFFER_ALLOW_PVP_FLAG;
	
	
	public static int EVENT_TvTTime;
	public static String[] EVENT_TvTRewards;
	public static boolean EVENT_TvT_rate;
	public static String[] EVENT_TvTStartTime;
	public static boolean EVENT_TvTCategories;
	public static int EVENT_TvTMaxPlayerInTeam;
	public static int EVENT_TvTMinPlayerInTeam;
	public static boolean EVENT_TvTAllowSummons;
	public static boolean EVENT_TvTAllowBuffs;
	public static boolean EVENT_TvTAllowMultiReg;
	public static String EVENT_TvTCheckWindowMethod;
	public static int EVENT_TvTEventRunningTime;
	public static String[] EVENT_TvTFighterBuffs;
	public static String[] EVENT_TvTMageBuffs;
	public static boolean EVENT_TvTBuffPlayers;
	public static boolean EVENT_TvTrate;
	public static int[] EVENT_TvTOpenCloseDoors;
	public static String[] EVENT_TvT_DISALLOWED_SKILLS;
	
	public static int EVENT_CtfTime;
	public static boolean EVENT_CtFrate;
	public static String[] EVENT_CtFStartTime;
	public static boolean EVENT_CtFCategories;
	public static int EVENT_CtFMaxPlayerInTeam;
	public static int EVENT_CtFMinPlayerInTeam;
	public static boolean EVENT_CtFAllowSummons;
	public static boolean EVENT_CtFAllowBuffs;
	public static boolean EVENT_CtFAllowMultiReg;
	public static String EVENT_CtFCheckWindowMethod;
	public static String[] EVENT_CtFFighterBuffs;
	public static String[] EVENT_CtFMageBuffs;
	public static boolean EVENT_CtFBuffPlayers;
	public static String[] EVENT_CtFRewards;
	public static int[] EVENT_CtFOpenCloseDoors;
	public static String[] EVENT_CtF_DISALLOWED_SKILLS;
	
	//TODO
		public static final boolean BBS_COMMISSION_ALLOW = false;
		public static final int[] BBS_COMMISSION_ARMOR_PRICE = null;
		public static final int[] BBS_COMMISSION_WEAPON_PRICE = null;
		public static final int[] BBS_COMMISSION_JEWERLY_PRICE = null;
		public static final int[] BBS_COMMISSION_OTHER_PRICE = null;
		public static final Object[] BBS_COMMISSION_ALLOW_ITEMS = null;
		public static final int BBS_COMMISSION_MAX_ENCHANT = 0;
		public static final Object[] BBS_COMMISSION_NOT_ALLOW_ITEMS = null;
		public static final boolean BBS_COMMISSION_ALLOW_UNDERWEAR = false;
		public static final boolean BBS_COMMISSION_ALLOW_CLOAK = false;
		public static final boolean BBS_COMMISSION_ALLOW_BRACELET = false;
		public static final boolean BBS_COMMISSION_ALLOW_AUGMENTED = false;
		public static final int BBS_COMMISSION_COUNT_TO_PAGE = 0;
		public static final int[] BBS_COMMISSION_MONETS = null;

		public static final boolean ENABLE_AUTO_HUNTING_REPORT = true;

		public static boolean AllowBBSSubManager;

		public static boolean R_GUARD;

		public static int TalkGuardChance;
		public static int TalkNormalChance = 0;
		public static int TalkNormalPeriod = 0;
		public static int TalkAggroPeriod = 0;
		
		public static boolean SERVICES_RIDE_HIRE_ENABLED;

		public static boolean SERVICES_DELEVEL_ENABLED;
		public static int SERVICES_DELEVEL_ITEM;
		public static int SERVICES_DELEVEL_COUNT;
		public static int SERVICES_DELEVEL_MIN_LEVEL;

		// ENCHANT by 4ipolino
		public static boolean ENCHANT_ENABLED;
		public static int ENCHANTER_ITEM_ID;
		public static int MAX_ENCHANT;
		public static int[] ENCHANT_LEVELS;
		public static int[] ENCHANT_PRICE_WPN;
		public static int[] ENCHANT_PRICE_ARM;
		public static int[] ENCHANT_ATTRIBUTE_LEVELS;
		public static int[] ENCHANT_ATTRIBUTE_LEVELS_ARM;
		public static int[] ATTRIBUTE_PRICE_WPN;
		public static int[] ATTRIBUTE_PRICE_ARM;
		public static boolean ENCHANT_ATT_PVP;

		
		
		public static double BBS_BUFF_TIME_MOD_SPECIAL;
		public static double BBS_BUFF_TIME_MOD_MUSIC;
		public static double BBS_BUFF_TIME_MOD;
		public static int BBS_BUFF_TIME;
		public static int BBS_BUFF_TIME_SPECIAL;
		public static int BBS_BUFF_TIME_MUSIC;
		public static int BBS_BUFF_ITEM_ID;
		public static int BUFF_PAGE_ROWS;
		public static int MAX_BUFF_PER_SET;
		public static int BBS_BUFF_FREE_LVL;
		public static int BBS_BUFF_ITEM_COUNT;
		public static int MAX_SETS_PER_CHAR;
		public static boolean BUFF_MANUAL_EDIT_SETS;
		public static boolean BBS_BUFF_ALLOW_HEAL;
		public static boolean BBS_BUFF_ALLOW_CANCEL;
		public static int[] BBS_BUFF_IDs;
		public static boolean BBS_BUFF_CURSED;
		public static boolean BBS_BUFF_PK;
		public static boolean BBS_BUFF_LEADER;
		public static boolean BBS_BUFF_NOBLE;
		public static boolean BBS_BUFF_TERITORY;
		public static boolean BBS_BUFF_PEACEZONE_ONLY;
		public static boolean BBS_BUFF_DUEL;
		public static boolean BBS_BUFF_TEMP_ACTION;
		public static boolean BBS_BUFF_CANT_MOVE;
		public static boolean BBS_BUFF_STORE_MODE;
		public static boolean BBS_BUFF_FISHING;
		public static boolean BBS_BUFF_MOUNTED;
		public static boolean BBS_BUFF_VEICHLE;
		public static boolean BBS_BUFF_FLY;
		public static boolean BBS_BUFF_OLY;
		public static boolean BBS_BUFF_ACTION;
		public static boolean BBS_BUFF_DEATH;
		public static boolean BBS_BUFFER_ENABLED;
		
	public static boolean SERVICES_CHANGE_Title_COLOR_ENABLED;
	public static int SERVICES_CHANGE_Title_COLOR_PRICE;
	public static int SERVICES_CHANGE_Title_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_Title_COLOR_LIST;

	//XXX top
    public static boolean ALLOW_HOPZONE_VOTE_REWARD;
    public static String HOPZONE_SERVER_LINK;
    public static String HOPZONE_FIRST_PAGE_LINK;
    public static int HOPZONE_VOTES_DIFFERENCE;
    public static int HOPZONE_FIRST_PAGE_RANK_NEEDED;
    public static int HOPZONE_REWARD_CHECK_TIME;
	public static int HOPZONE_REWARD_ID;
	public static int HOPZONE_REWARD_COUNT;
	public static int HOPZONE_DUALBOXES_ALLOWED;
    public static boolean ALLOW_HOPZONE_GAME_SERVER_REPORT;
    public static boolean ALLOW_TOPZONE_VOTE_REWARD;
    public static String TOPZONE_SERVER_LINK;
    public static String TOPZONE_FIRST_PAGE_LINK;
    public static int TOPZONE_VOTES_DIFFERENCE;
    public static int TOPZONE_FIRST_PAGE_RANK_NEEDED;
    public static int TOPZONE_REWARD_CHECK_TIME;
	public static int TOPZONE_REWARD_ID;
	public static int TOPZONE_REWARD_COUNT;
	public static int TOPZONE_DUALBOXES_ALLOWED;
    public static boolean ALLOW_TOPZONE_GAME_SERVER_REPORT;

	public static Calendar CASTLE_VALIDATION_DATE;
	public static int[] CASTLE_SELECT_HOURS;

	public static boolean ALT_PCBANG_POINTS_ENABLED;
	public static double ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE;
	public static int ALT_PCBANG_POINTS_BONUS;
	public static int ALT_PCBANG_POINTS_DELAY;
	public static int ALT_PCBANG_POINTS_MIN_LVL;

	public static boolean ALT_DEBUG_ENABLED;
	public static boolean ALT_DEBUG_PVP_ENABLED;
	public static boolean ALT_DEBUG_PVP_DUEL_ONLY;
	public static boolean ALT_DEBUG_PVE_ENABLED;

	public static double CRAFT_MASTERWORK_CHANCE;
	public static double CRAFT_DOUBLECRAFT_CHANCE;

	/** Thread pools size */
	public static int SCHEDULED_THREAD_POOL_SIZE;
	public static int EXECUTOR_THREAD_POOL_SIZE;

	public static boolean ENABLE_RUNNABLE_STATS;

	/** Network settings */
	public static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();

	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_ONLY_ADENA;
	public static boolean AUTO_LOOT_INDIVIDUAL;
	public static boolean AUTO_LOOT_FROM_RAIDS;

	/** Auto-loot for/from players with karma also? */
	public static boolean AUTO_LOOT_PK;

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
	public static boolean GLOBAL_SHOUT;
	public static boolean GLOBAL_TRADE_CHAT;
	public static int CHAT_RANGE;
	public static int SHOUT_OFFSET;	
	
	public static GArray<String> TRADE_WORDS;
	public static boolean TRADE_CHATS_REPLACE;

	/** For test servers - evrybody has admin rights */
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;

	public static double ALT_RAID_RESPAWN_MULTIPLIER;

	public static boolean ALT_ALLOW_AUGMENT_ALL;
	public static boolean ALT_ALLOW_DROP_AUGMENTED;

	public static boolean ALT_GAME_UNREGISTER_RECIPE;

	/** Delay for announce SS period (in minutes) */
	public static int SS_ANNOUNCE_PERIOD;

	/** Petition manager */
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;

	/** Show mob stats/droplist to players? */
	public static boolean ALT_GAME_SHOW_DROPLIST;
	public static boolean ALT_FULL_NPC_STATS_PAGE;
	public static boolean ALLOW_NPC_SHIFTCLICK;

	public static boolean ALT_ALLOW_SELL_COMMON;
	public static boolean ALT_ALLOW_SHADOW_WEAPONS;
	public static int[] ALT_DISABLED_MULTISELL;
	public static int[] ALT_SHOP_PRICE_LIMITS;
	public static int[] ALT_SHOP_UNALLOWED_ITEMS;

	public static int[] ALT_ALLOWED_PET_POTIONS;

	public static double SKILLS_CHANCE_MOD;
	public static double SKILLS_CHANCE_MIN;
	public static double SKILLS_CHANCE_POW;
	public static double SKILLS_CHANCE_CAP;
	public static double SKILLS_MOB_CHANCE;
	public static double SKILLS_DEBUFF_MOB_CHANCE;
	public static boolean SHIELD_SLAM_BLOCK_IS_MUSIC;
	public static boolean ALT_SAVE_UNSAVEABLE;
	public static int ALT_SAVE_EFFECTS_REMAINING_TIME;
	public static boolean ALT_SHOW_REUSE_MSG;
	public static boolean ALT_DELETE_SA_BUFFS;
	public static int SKILLS_CAST_TIME_MIN;
	
	/** Конфигурация использования итемов по умолчанию поушены*/
	public static int[] ITEM_USE_LIST_ID;
	public static boolean ITEM_USE_IS_COMBAT_FLAG;
	public static boolean ITEM_USE_IS_ATTACK;
	public static boolean ITEM_USE_IS_EVENTS;

	/** Настройки для евента Файт Клуб*/
	public static boolean FIGHT_CLUB_ENABLED;
	public static int MINIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_TO_PARRICIPATION;
	public static int MAXIMUM_LEVEL_DIFFERENCE;
	public static String[] ALLOWED_RATE_ITEMS;
	public static int PLAYERS_PER_PAGE;
	public static int ARENA_TELEPORT_DELAY;
	public static boolean CANCEL_BUFF_BEFORE_FIGHT;
	public static boolean UNSUMMON_PETS;
	public static boolean UNSUMMON_SUMMONS;
	public static boolean REMOVE_CLAN_SKILLS;
	public static boolean REMOVE_HERO_SKILLS;
	public static int TIME_TO_PREPARATION;
	public static int FIGHT_TIME;
	public static boolean ALLOW_DRAW;
	public static int TIME_TELEPORT_BACK;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE;
	public static boolean FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN;
	public static boolean FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN;
	
	/** Титул при создании чара */
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;

	/** Таймаут на использование social action */
	public static boolean ALT_SOCIAL_ACTION_REUSE;

	/** Отключение книг для изучения скилов */
	public static boolean ALT_DISABLE_SPELLBOOKS;

	/** Alternative gameing - loss of XP on death */
	public static boolean ALT_GAME_DELEVEL;

	/** Разрешать ли на арене бои за опыт */
	public static boolean ALT_ARENA_EXP;

	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM;
	public static int ALT_GAME_START_LEVEL_TO_SUBCLASS;
	public static int ALT_GAME_LEVEL_TO_GET_SUBCLASS;
	public static int ALT_MAX_LEVEL;
	public static int ALT_MAX_SUB_LEVEL;
	public static int ALT_GAME_SUB_ADD;
	public static boolean ALT_GAME_SUB_BOOK;
	public static boolean ALT_NO_LASTHIT;
	public static boolean ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY;
	public static boolean ALT_KAMALOKA_NIGHTMARE_REENTER;
	public static boolean ALT_KAMALOKA_ABYSS_REENTER;
	public static boolean ALT_KAMALOKA_LAB_REENTER;
	public static boolean ALT_PET_HEAL_BATTLE_ONLY;

	public static boolean ALT_SIMPLE_SIGNS;
	public static boolean ALT_TELE_TO_CATACOMBS;
	public static boolean ALT_BS_CRYSTALLIZE;
	public static int ALT_MAMMON_EXCHANGE;
	public static int ALT_MAMMON_UPGRADE;
	public static boolean ALT_ALLOW_TATTOO;

	public static int ALT_BUFF_LIMIT;

	public static int MULTISELL_SIZE;

	public static boolean SERVICES_CHANGE_NICK_ENABLED;
	public static boolean SERVICES_CHANGE_NICK_ALLOW_SYMBOL;
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

	public static boolean SERVICES_CHANGE_SEX_ENABLED;
	public static int SERVICES_CHANGE_SEX_PRICE;
	public static int SERVICES_CHANGE_SEX_ITEM;

	public static boolean SERVICES_CHANGE_BASE_ENABLED;
	public static int SERVICES_CHANGE_BASE_PRICE;
	public static int SERVICES_CHANGE_BASE_ITEM;

	public static boolean SERVICES_SEPARATE_SUB_ENABLED;
	public static int SERVICES_SEPARATE_SUB_PRICE;
	public static int SERVICES_SEPARATE_SUB_ITEM;

	public static boolean SERVICES_CHANGE_NICK_COLOR_ENABLED;
	public static int SERVICES_CHANGE_NICK_COLOR_PRICE;
	public static int SERVICES_CHANGE_NICK_COLOR_ITEM;
	public static String[] SERVICES_CHANGE_NICK_COLOR_LIST;

	public static boolean SERVICES_BASH_ENABLED;
	public static boolean SERVICES_BASH_SKIP_DOWNLOAD;
	public static int SERVICES_BASH_RELOAD_TIME;

	public static boolean SERVICES_NOBLESS_SELL_ENABLED;
	public static int SERVICES_NOBLESS_SELL_PRICE;
	public static int SERVICES_NOBLESS_SELL_ITEM;
	
	public static boolean SERVICES_HERO_SELL_ENABLED;
	public static int[] SERVICES_HERO_SELL_DAY;
	public static int[] SERVICES_HERO_SELL_PRICE;
	public static int[] SERVICES_HERO_SELL_ITEM;

	public static boolean SERVICES_WASH_PK_ENABLED;
	public static int SERVICES_WASH_PK_ITEM;
	public static int SERVICES_WASH_PK_PRICE;
	
	public static boolean SERVICES_EXPAND_INVENTORY_ENABLED;
	public static int SERVICES_EXPAND_INVENTORY_PRICE;
	public static int SERVICES_EXPAND_INVENTORY_ITEM;
	public static int SERVICES_EXPAND_INVENTORY_MAX;

	public static boolean SERVICES_EXPAND_WAREHOUSE_ENABLED;
	public static int SERVICES_EXPAND_WAREHOUSE_PRICE;
	public static int SERVICES_EXPAND_WAREHOUSE_ITEM;

	public static boolean SERVICES_EXPAND_CWH_ENABLED;
	public static int SERVICES_EXPAND_CWH_PRICE;
	public static int SERVICES_EXPAND_CWH_ITEM;

	public static String SERVICES_SELLPETS;

	public static boolean SERVICES_OFFLINE_TRADE_ALLOW;
	public static boolean SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE;
	public static int SERVICES_OFFLINE_TRADE_MIN_LEVEL;
	public static int SERVICES_OFFLINE_TRADE_NAME_COLOR;
	public static int SERVICES_OFFLINE_TRADE_PRICE;
	public static int SERVICES_OFFLINE_TRADE_PRICE_ITEM;
	public static long SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK;
	public static boolean SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART;
	public static boolean SERVICES_GIRAN_HARBOR_ENABLED;
	public static boolean SERVICES_PARNASSUS_ENABLED;
	public static boolean SERVICES_PARNASSUS_NOTAX;
	public static long SERVICES_PARNASSUS_PRICE;

	public static boolean SERVICES_ALLOW_LOTTERY;
	public static int SERVICES_LOTTERY_PRIZE;
	public static int SERVICES_ALT_LOTTERY_PRICE;
	public static int SERVICES_LOTTERY_TICKET_PRICE;
	public static double SERVICES_LOTTERY_5_NUMBER_RATE;
	public static double SERVICES_LOTTERY_4_NUMBER_RATE;
	public static double SERVICES_LOTTERY_3_NUMBER_RATE;
	public static int SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE;

	public static boolean SERVICES_ALLOW_ROULETTE;
	public static long SERVICES_ROULETTE_MIN_BET;
	public static long SERVICES_ROULETTE_MAX_BET;
	
	public static boolean ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE;
	public static boolean ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_ALLOW_ADENA_DAWN;
	// -------------------------------------------------------------------------------------------------------
//TODO	// PvP MOD
	// -------------------------------------------------------------------------------------------------------
		public static int ATT_MOD_ARMOR;
		public static int ATT_MOD_WEAPON;
		public static int ATT_MOD_WEAPON1;
		public static int ATT_MOD_MAX_ARMOR;
		public static int ATT_MOD_MAX_WEAPON;
		
		public static boolean SPAWN_CITIES_TREE;
		public static boolean SPAWN_NPC_BUFFER;
		public static int MAX_PARTY_SIZE;
		public static boolean SPAWN_scrubwoman;
		public static boolean ADEPT_ENABLE;
	//By SmokiMo
	public static int HENNA_STATS;
	public static boolean ENEBLE_TITLE_COLOR_MOD;
	public static String TYPE_TITLE_COLOR_MOD;
	public static int COUNT_TITLE_1;
	public static int TITLE_COLOR_1;
	public static int COUNT_TITLE_2;
	public static int TITLE_COLOR_2;
	public static int COUNT_TITLE_3;
	public static int TITLE_COLOR_3;
	public static int COUNT_TITLE_4;
	public static int TITLE_COLOR_4;
	public static int COUNT_TITLE_5;
	public static int TITLE_COLOR_5;
	public static boolean ENEBLE_NAME_COLOR_MOD;
	public static String TYPE_NAME_COLOR_MOD;
	public static int COUNT_NAME_1;
	public static int NAME_COLOR_1;
	public static int COUNT_NAME_2;
	public static int NAME_COLOR_2;
	public static int COUNT_NAME_3;
	public static int NAME_COLOR_3;
	public static int COUNT_NAME_4;
	public static int NAME_COLOR_4;
	public static int COUNT_NAME_5;
	public static int NAME_COLOR_5;
    //add by 4ipolino
	public static boolean NEW_CHAR_IS_NOBLE;
	public static boolean NEW_CHAR_IS_HERO;
	public static boolean ANNOUNCE_SPAWN_RB;       
	// Перенос персонажей между аккаунтами
	public static boolean ACC_MOVE_ENABLED;
	public static int ACC_MOVE_ITEM;
	public static int ACC_MOVE_PRICE;
    //XXX BUFFER 
	public static  boolean BUFFER_ON;
	public static  int ITEM_ID;
	public static  boolean BUFFER_PET_ENABLED;
	public static  int BUFFER_PRICE;
	public static  int BUFFER_MIN_LVL;
	public static  int BUFFER_MAX_LVL;
	public static  boolean BUFFER_ALLOW_IN_INSTANCE;
	//XXX Команды
	/* .km-all-to-me  */ 
	public static boolean ENABLE_KM_ALL_TO_ME;
	/* .res */
	public static boolean COMMAND_RES;
	public static int ITEM_ID_RESS;
	public static int PRICE_RESS;
	/*.pa */ 
	public static boolean COMMAND_PA;
	/*.loot */ 
	public static boolean COMMAND_LOOT;
	/* .lock */
	public static boolean SERVICES_LOCK_ACCOUNT_IP;
	 /* .farm */
	public static boolean COMMAND_FARM;
	public static int FARM_TELEPORT_ITEM_ID;
	public static int PRICE_FARM;
	public static int FARM_X;
	public static int FARM_Y;
	public static int FARM_Z;
	 /* .farm_hard */
	public static boolean COMMAND_FARM_HARD;
	public static int FARM_HARD_TELEPORT_ITEM_ID;
	public static int PRICE_FARM_HARD;
	public static int FARM_HARD_X;
	public static int FARM_HARD_Y;
	public static int FARM_HARD_Z;
	 /* .farm_low */
	public static boolean COMMAND_FARM_LOW;
	public static int FARM_LOW_TELEPORT_ITEM_ID;
	public static int PRICE_FARM_LOW;
	public static int FARM_LOW_X;
	public static int FARM_LOW_Y;
	public static int FARM_LOW_Z;
	 /* .pvp */
	public static boolean COMMAND_PVP;
	public static int PVP_TELEPORT_ITEM_ID;
	public static int PRICE_PVP;
	public static int PVP_X;
	public static int PVP_Y;
	public static int PVP_Z;
	/* .GoToLeader */ 
	public static boolean COMMAND_GoToLeader;
	public static int PRICE_TELEPORT_CL;
	public static int GO_TO_CL_ITEM_ID;
	// noble
	public static boolean NOBLE;
	public static int PRICE_NOBLE;
	public static int ITEM_NOBLE;
	 //CustomSpawnNewChar
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	
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
	public static int[] ALT_OLY_DATE_END;

	public static boolean ENABLE_OLYMPIAD;
	public static boolean ENABLE_OLYMPIAD_SPECTATING;

	public static int CLASS_GAME_MIN;
	public static int NONCLASS_GAME_MIN;
	public static int TEAM_GAME_MIN;

	public static int GAME_MAX_LIMIT;
	public static int GAME_CLASSES_COUNT_LIMIT;
	public static int GAME_NOCLASSES_COUNT_LIMIT;
	public static int GAME_TEAM_COUNT_LIMIT;

	public static int ALT_OLY_REG_DISPLAY;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_TEAM_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int OLYMPIAD_STADIAS_COUNT;
	public static int OLYMPIAD_BATTLES_FOR_REWARD;
	public static int OLYMPIAD_POINTS_DEFAULT;
	public static int OLYMPIAD_POINTS_WEEKLY;
	public static boolean OLYMPIAD_OLDSTYLE_STAT;
	public static boolean OLYMPIAD_PLAYER_IP;
	public static int OLYMPIAD_BEGIN_TIME;
	public static boolean OLYMPIAD_BAD_ENCHANT_ITEMS_ALLOW;
	
	public static boolean OLY_ENCH_LIMIT_ENABLE;
    public static int OLY_ENCHANT_LIMIT_WEAPON;
    public static int OLY_ENCHANT_LIMIT_ARMOR;
    public static int OLY_ENCHANT_LIMIT_JEWEL;

	public static long NONOWNER_ITEM_PICKUP_DELAY;

	/** Logging Chat Window */
	public static boolean LOG_CHAT;

	public static Map<Integer, PlayerAccess> gmlist = new HashMap<Integer, PlayerAccess>();

	/** Rate control */
	public static double RATE_XP;
	public static double RATE_SP;
	public static double RATE_QUESTS_REWARD;
	public static double RATE_QUESTS_DROP;
	public static double RATE_CLAN_REP_SCORE;
	public static int RATE_CLAN_REP_SCORE_MAX_AFFECTED;
	public static double RATE_DROP_ADENA;
	public static double RATE_DROP_CHAMPION;
	public static double RATE_CHAMPION_DROP_ADENA;
	public static double RATE_DROP_SPOIL_CHAMPION;
	public static double RATE_DROP_ITEMS;
	public static double RATE_CHANCE_GROUP_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_HERBS;
	public static double RATE_CHANCE_SPOIL;
	public static double RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_EPOLET;
	public static boolean NO_RATE_ENCHANT_SCROLL;
	public static double RATE_ENCHANT_SCROLL;
	public static boolean CHAMPION_DROP_ONLY_ADENA;
	public static boolean NO_RATE_HERBS;
	public static double RATE_DROP_HERBS;
	public static boolean NO_RATE_ATT;
	public static double RATE_DROP_ATT;
	public static boolean NO_RATE_LIFE_STONE;
	public static boolean NO_RATE_FORGOTTEN_SCROLL;
	public static double RATE_DROP_LIFE_STONE;
	public static boolean NO_RATE_KEY_MATERIAL;
	public static double RATE_DROP_KEY_MATERIAL;
	public static boolean NO_RATE_RECIPES;
	public static double RATE_DROP_RECIPES;
	public static double RATE_DROP_COMMON_ITEMS;
	public static boolean NO_RATE_RAIDBOSS;
	public static double RATE_DROP_RAIDBOSS;
	public static double RATE_DROP_SPOIL;
	public static int[] NO_RATE_ITEMS;
	public static boolean NO_RATE_SIEGE_GUARD;
	public static double RATE_DROP_SIEGE_GUARD;
	public static double RATE_MANOR;
	public static double RATE_FISH_DROP_COUNT;
	public static boolean RATE_PARTY_MIN;
	public static double RATE_HELLBOUND_CONFIDENCE;
	public static boolean NO_RATE_EQUIPMENT;

	public static int RATE_MOB_SPAWN;
	public static int RATE_MOB_SPAWN_MIN_LEVEL;
	public static int RATE_MOB_SPAWN_MAX_LEVEL;

	/** Player Drop Rate control */
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_NEEDED_TO_DROP;

	public static int KARMA_DROP_ITEM_LIMIT;

	public static int KARMA_RANDOM_DROP_LOCATION_LIMIT;

	public static double KARMA_DROPCHANCE_BASE;
	public static double KARMA_DROPCHANCE_MOD;
	public static double NORMAL_DROPCHANCE_BASE;
	public static int DROPCHANCE_EQUIPMENT;
	public static int DROPCHANCE_EQUIPPED_WEAPON;
	public static int DROPCHANCE_ITEM;

	public static int AUTODESTROY_ITEM_AFTER;
	public static int AUTODESTROY_PLAYER_ITEM_AFTER;

	public static int DELETE_DAYS;

	public static int PURGE_BYPASS_TASK_FREQUENCY;

	/** Datapack root directory */
	public static File DATAPACK_ROOT;

	public static double CLANHALL_BUFFTIME_MODIFIER;
	public static double SONGDANCETIME_MODIFIER;

	public static double MAXLOAD_MODIFIER;
	public static double GATEKEEPER_MODIFIER;
	public static boolean ALT_IMPROVED_PETS_LIMITED_USE;
	public static int GATEKEEPER_FREE;
	public static int CRUMA_GATEKEEPER_LVL;

	public static double ALT_CHAMPION_CHANCE1;
	public static double ALT_CHAMPION_CHANCE2;
	public static boolean ALT_CHAMPION_CAN_BE_AGGRO;
	public static boolean ALT_CHAMPION_CAN_BE_SOCIAL;
	public static boolean ALT_CHAMPION_DROP_HERBS;
	public static boolean ALT_SHOW_MONSTERS_LVL;
	public static boolean ALT_SHOW_MONSTERS_AGRESSION;
	public static int ALT_CHAMPION_TOP_LEVEL;
	public static int ALT_CHAMPION_MIN_LEVEL;

	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean DROP_CURSED_WEAPONS_ON_KICK;
	public static boolean ALLOW_NOBLE_TP_TO_ALL;
	
	public static boolean SELL_ALL_ITEMS_FREE;
	/** Pets */
	public static int SWIMING_SPEED;

	/** protocol revision */
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;

	/** random animation interval */
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;

	public static String DEFAULT_LANG;
	public static String DEFAULT_GK_LANG;

	/** Время запланированного на определенное время суток рестарта */
	public static String RESTART_AT_TIME;

	public static int GAME_SERVER_LOGIN_PORT;
	public static boolean GAME_SERVER_LOGIN_CRYPT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;

	public static boolean SECOND_AUTH_ENABLED;
	public static boolean SECOND_AUTH_BAN_ACC;
	public static boolean SECOND_AUTH_STRONG_PASS;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;
	
	public static boolean SERVER_SIDE_NPC_NAME;
	public static boolean SERVER_SIDE_NPC_TITLE;
	public static boolean SERVER_SIDE_NPC_TITLE_ETC;

	public static String CLASS_MASTERS_PRICE;
	public static int CLASS_MASTERS_PRICE_ITEM;
	public static List<Integer> ALLOW_CLASS_MASTERS_LIST = new ArrayList<Integer>();
	public static int[] CLASS_MASTERS_PRICE_LIST = new int[4];
	public static boolean ALLOW_EVENT_GATEKEEPER;

	public static boolean ITEM_BROKER_ITEM_SEARCH;
	public static boolean SERVICES_CHANGE_PASSWORD;

	/** Inventory slots limits */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int QUEST_INVENTORY_MAXIMUM;

	/** Warehouse slots limits */
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;

	public static int FREIGHT_SLOTS;

	/** Spoil Rates */
	public static double BASE_SPOIL_RATE;
	public static double MINIMUM_SPOIL_RATE;
	public static boolean ALT_SPOIL_FORMULA;

	/** Manor Config */
	public static double MANOR_SOWING_BASIC_SUCCESS;
	public static double MANOR_SOWING_ALT_BASIC_SUCCESS;
	public static double MANOR_HARVESTING_BASIC_SUCCESS;
	public static int MANOR_DIFF_PLAYER_TARGET;
	public static double MANOR_DIFF_PLAYER_TARGET_PENALTY;
	public static int MANOR_DIFF_SEED_TARGET;
	public static double MANOR_DIFF_SEED_TARGET_PENALTY;

	/** Karma System Variables */
	public static int KARMA_MIN_KARMA;
	public static int KARMA_SP_DIVIDER;
	public static int KARMA_LOST_BASE;

	public static int MIN_PK_TO_ITEMS_DROP;
	public static boolean DROP_ITEMS_ON_DIE;
	public static boolean DROP_ITEMS_AUGMENTED;

	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();

	public static int PVP_TIME;

	/** Karma Punishment */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;

	/** Chance that an item will succesfully be enchanted */
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_ACCESSORY;
	public static int ENCHANT_CHANCE_CRYSTAL_WEAPON;
	public static int ENCHANT_CHANCE_CRYSTAL_ARMOR;
	public static int ENCHANT_CHANCE_CRYSTAL_ARMOR_OLF;
	public static int ENCHANT_CHANCE_CRYSTAL_ACCESSORY;
	public static int ENCHANT_CHANCE_WEAPON_BLESS;
	public static int ENCHANT_CHANCE_ARMOR_BLESS;
	public static int ENCHANT_CHANCE_ACCESSORY_BLESS;
	
	public static boolean USE_ALT_ENCHANT;
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_CRYSTAL = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_BLESSED = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_CRYSTAL = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_BLESSED = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_CRYSTAL = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_BLESSED = new ArrayList<Integer>();
	public static int ENCHANT_MAX;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_ATTRIBUTE_STONE_CHANCE;
	public static int ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE;
	public static int ARMOR_OVERENCHANT_HPBONUS_LIMIT;
	public static boolean SHOW_ENCHANT_EFFECT_RESULT;

	public static boolean REGEN_SIT_WAIT;

	public static double RATE_RAID_REGEN;
	public static double RATE_RAID_DEFENSE;
	public static double RATE_RAID_ATTACK;
	public static double RATE_EPIC_DEFENSE;
	public static double RATE_EPIC_ATTACK;
	public static int RAID_MAX_LEVEL_DIFF;
	public static boolean PARALIZE_ON_RAID_DIFF;

	public static double ALT_PK_DEATH_RATE;
	public static int STARTING_ADENA;

	public static int STARTING_LVL; 
	/** Deep Blue Mobs' Drop Rules Enabled */
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static boolean UNSTUCK_SKILL;

	/** telnet enabled */
	public static boolean IS_TELNET_ENABLED;
	public static String TELNET_DEFAULT_ENCODING;
	public static String TELNET_PASSWORD;
	public static String TELNET_HOSTNAME;
	public static int TELNET_PORT;

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
	public static double SENDSTATUS_TRADE_MOD;
	public static boolean SHOW_OFFLINE_MODE_IN_ONLINE;

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

	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;

	public static boolean ANNOUNCE_MAMMON_SPAWN;

	public static int GM_NAME_COLOUR;
	public static boolean GM_HERO_AURA;
	public static int NORMAL_NAME_COLOUR;
	public static int CLANLEADER_NAME_COLOUR;

	
	//XXX Викторина
	public static boolean VIKTORINA_ENABLED;// false;
	public static boolean VIKTORINA_REMOVE_QUESTION;//false;;
	public static boolean VIKTORINA_REMOVE_QUESTION_NO_ANSWER;//= false;
	public static int VIKTORINA_START_TIME_HOUR;// 16;
	public static int VIKTORINA_START_TIME_MIN;// 16;
	public static int VIKTORINA_WORK_TIME;//2;
	public static int VIKTORINA_TIME_ANSER;//1;
	public static int VIKTORINA_TIME_PAUSE;//1;
	
	public static void loadVIKTORINAsettings()
	{
		ExProperties VIKTORINASettings = load(VIKTORINA_CONFIG_FILE);
		
		VIKTORINA_ENABLED = VIKTORINASettings.getProperty("Victorina_Enabled", false);
		VIKTORINA_REMOVE_QUESTION = VIKTORINASettings.getProperty("Victorina_Remove_Question", false);
		VIKTORINA_REMOVE_QUESTION_NO_ANSWER = VIKTORINASettings.getProperty("Victorina_Remove_Question_No_Answer", false);
		VIKTORINA_START_TIME_HOUR = VIKTORINASettings.getProperty("Victorina_Start_Time_Hour", 16);
		VIKTORINA_START_TIME_MIN = VIKTORINASettings.getProperty("Victorina_Start_Time_Minute", 16);
		VIKTORINA_WORK_TIME = VIKTORINASettings.getProperty("Victorina_Work_Time", 2);
		VIKTORINA_TIME_ANSER = VIKTORINASettings.getProperty("Victorina_Time_Answer", 1);
		VIKTORINA_TIME_PAUSE = VIKTORINASettings.getProperty("Victorina_Time_Pause", 1);

	}
	
	/** AI */
	public static int AI_TASK_MANAGER_COUNT;
	public static long AI_TASK_ATTACK_DELAY;
	public static long AI_TASK_ACTIVE_DELAY;
	public static boolean BLOCK_ACTIVE_TASKS;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static boolean RND_WALK;
	public static int RND_WALK_RATE;
	public static int RND_ANIMATION_RATE;

	public static int AGGRO_CHECK_INTERVAL;
	public static long NONAGGRO_TIME_ONTELEPORT;

	/** Maximum range mobs can randomly go from spawn point */
	public static int MAX_DRIFT_RANGE;

	/** Maximum range mobs can pursue agressor from spawn point */
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_UNDERGROUND_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;

	public static boolean ALT_DEATH_PENALTY;
	public static boolean ALLOW_DEATH_PENALTY_C5;
	public static int ALT_DEATH_PENALTY_C5_CHANCE;
	public static boolean ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY;
	public static int ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
	public static int ALT_DEATH_PENALTY_C5_KARMA_PENALTY;

	public static boolean HIDE_GM_STATUS;
	public static boolean SHOW_GM_LOGIN;
	public static boolean SAVE_GM_EFFECTS; //Silence, gmspeed, etc...

	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FORGOTTEN_SKILLS;

	public static int MOVE_PACKET_DELAY;
	public static int ATTACK_PACKET_DELAY;

	public static boolean DAMAGE_FROM_FALLING;

	/** Community Board */
	public static boolean USE_BBS_BUFER_IS_COMBAT;
	public static boolean USE_BBS_BUFER_IS_CURSE_WEAPON;
	public static boolean USE_BBS_BUFER_IS_EVENTS;
	public static boolean USE_BBS_TELEPORT_IS_COMBAT;
	public static boolean USE_BBS_TELEPORT_IS_EVENTS;
	public static boolean USE_BBS_PROF_IS_COMBAT;
	public static boolean USE_BBS_PROF_IS_EVENTS;
	public static boolean SAVE_BBS_TELEPORT_IS_EPIC;
	public static boolean SAVE_BBS_TELEPORT_IS_BZ;
	public static boolean COMMUNITYBOARD_ENABLED;
	public static boolean ALLOW_COMMUNITYBOARD_IN_COMBAT;
	public static boolean ALLOW_COMMUNITYBOARD_IS_IN_SIEGE;
	public static boolean COMMUNITYBOARD_BUFFER_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_MAX_LVL_ALLOW;
	public static boolean COMMUNITYBOARD_BUFFER_SIEGE_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_NO_IS_IN_PEACE_ENABLED;
	public static boolean COMMUNITYBOARD_SELL_ENABLED;
	public static boolean COMMUNITYBOARD_SHOP_ENABLED;
	public static boolean COMMUNITYBOARD_SHOP_NO_IS_IN_PEACE_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_PET_ENABLED;
	public static boolean COMMUNITYBOARD_BUFFER_SAVE_ENABLED;
	public static boolean COMMUNITYBOARD_ABNORMAL_ENABLED;
	public static boolean COMMUNITYBOARD_INSTANCE_ENABLED;
	public static boolean COMMUNITYBOARD_EVENTS_ENABLED;
	public static int COMMUNITYBOARD_BUFF_TIME;
	public static int COMMUNITYBOARD_BUFFER_MAX_LVL;
	public static int COMMUNITYBOARD_BUFF_PETS_TIME;
	public static int COMMUNITYBOARD_BUFF_COMBO_TIME;
	public static int COMMUNITYBOARD_BUFF_SONGDANCE_TIME;
	public static int COMMUNITYBOARD_BUFF_PICE;
	public static int COMMUNITYBOARD_BUFF_SAVE_PICE;
	public static List<Integer> COMMUNITYBOARD_BUFF_ALLOW = new ArrayList<Integer>();
	public static List<Integer> COMMUNITI_LIST_MAGE_SUPPORT = new ArrayList<Integer>();
	public static List<Integer> COMMUNITI_LIST_FIGHTER_SUPPORT = new ArrayList<Integer>();
	public static List<String> COMMUNITYBOARD_MULTISELL_ALLOW = new ArrayList<String>();
	public static String BBS_DEFAULT;
	public static String BBS_HOME_DIR;
	public static boolean COMMUNITYBOARD_TELEPORT_ENABLED;
	public static int COMMUNITYBOARD_TELE_PICE;
	public static int COMMUNITYBOARD_SAVE_TELE_PICE;
	public static boolean COMMUNITYBOARD_TELEPORT_SIEGE_ENABLED;

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

	public static int FOLLOW_RANGE;
	
	public static boolean ALT_ENABLE_MULTI_PROFA;

	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static boolean ALT_ITEM_AUCTION_CAN_REBID;
	public static boolean ALT_ITEM_AUCTION_START_ANNOUNCE;
	public static int ALT_ITEM_AUCTION_BID_ITEM_ID;
	public static long ALT_ITEM_AUCTION_MAX_BID;
	public static int ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS;

	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;

	public static boolean ALT_ENABLE_BLOCK_CHECKER_EVENT;
	public static int ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static double ALT_RATE_COINS_REWARD_BLOCK_CHECKER;
	public static boolean ALT_HBCE_FAIR_PLAY;
	public static int ALT_PET_INVENTORY_LIMIT;
	public static int ALT_CLAN_LEVEL_CREATE;

	/**limits of stats **/
	public static int LIM_PATK;
	public static int LIM_MATK;
	public static int LIM_PDEF;
	public static int LIM_MDEF;
	public static int LIM_MATK_SPD;
	public static int LIM_PATK_SPD;
	public static int LIM_CRIT_DAM;
	public static int LIM_CRIT;
	public static int LIM_MCRIT;
	public static int LIM_ACCURACY;
	public static int LIM_EVASION;
	public static int LIM_MOVE;
	public static int GM_LIM_MOVE;
	public static int LIM_FAME;

	public static double ALT_NPC_PATK_MODIFIER;
	public static double ALT_NPC_MATK_MODIFIER;
	public static double ALT_NPC_MAXHP_MODIFIER;
	public static double ALT_NPC_MAXMP_MODIFIER;

	/** Enchant Config **/
	public static int SAFE_ENCHANT_COMMON;
	public static int SAFE_ENCHANT_FULL_BODY;
	public static int SAFE_ENCHANT_LVL;

	public static int FESTIVAL_MIN_PARTY_SIZE;
	public static double FESTIVAL_RATE_PRICE;

	/** DimensionalRift Config **/
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY; // Time in ms the party has to wait until the mobs spawn
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME;
	public static int RIFT_AUTO_JUMPS_TIME_RAND;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;

	public static boolean ALLOW_TALK_WHILE_SITTING;

	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;

	/** Разрешены ли клановые скилы? **/
	public static boolean ALLOW_CLANSKILLS;

	/** Разрешено ли изучение скилов трансформации и саб классов без наличия выполненного квеста */
	public static boolean ALLOW_LEARN_TRANS_SKILLS_WO_QUEST;

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

	public static double EVENT_CofferOfShadowsPriceRate;
	public static double EVENT_CofferOfShadowsRewardRate;

	public static double EVENT_APIL_FOOLS_DROP_CHANCE;

	/** Master Yogi event enchant config */
	public static int ENCHANT_CHANCE_MASTER_YOGI_STAFF;
	public static int ENCHANT_MAX_MASTER_YOGI_STAFF;
	public static int SAFE_ENCHANT_MASTER_YOGI_STAFF;

	public static boolean AllowCustomDropItems;
	public static int[] CDItemsId;
	public static int[] CDItemsCountDropMin;
	public static int[] CDItemsCountDropMax;
	public static double[] CustomDropItemsChance;
	public static boolean CDItemsAllowMinMaxPlayerLvl;
	public static int CDItemsMinPlayerLvl;
	public static int CDItemsMaxPlayerLvl;
	public static boolean CDItemsAllowMinMaxMobLvl;
	public static int CDItemsMinMobLvl;
	public static int CDItemsMaxMobLvl;
	public static boolean CDItemsAllowOnlyRbDrops;
	
	public static boolean EVENT_GvGDisableEffect;

	public static double EVENT_TFH_POLLEN_CHANCE;
	public static double EVENT_GLITTMEDAL_NORMAL_CHANCE;
	public static double EVENT_GLITTMEDAL_GLIT_CHANCE;
	public static double EVENT_L2DAY_LETTER_CHANCE;
	public static double EVENT_CHANGE_OF_HEART_CHANCE;

	public static double EVENT_TRICK_OF_TRANS_CHANCE;

	public static double EVENT_MARCH8_DROP_CHANCE;
	public static double EVENT_MARCH8_PRICE_RATE;

	public static boolean EVENT_BOUNTY_HUNTERS_ENABLED;

	public static long EVENT_SAVING_SNOWMAN_LOTERY_PRICE;
	public static int EVENT_SAVING_SNOWMAN_REWARDER_CHANCE;

	public static boolean SERVICES_NO_TRADE_ONLY_OFFLINE;
	public static boolean SERVICES_NO_TRADE_BLOCK_ZONE;
	public static double SERVICES_TRADE_TAX;
	public static double SERVICES_OFFSHORE_TRADE_TAX;
	public static boolean SERVICES_OFFSHORE_NO_CASTLE_TAX;
	public static boolean SERVICES_TRADE_TAX_ONLY_OFFLINE;
	public static boolean SERVICES_TRADE_ONLY_FAR;
	public static int SERVICES_TRADE_RADIUS;
	public static int SERVICES_TRADE_MIN_LEVEL;

	public static boolean SERVICES_ENABLE_NO_CARRIER;
	public static int SERVICES_NO_CARRIER_DEFAULT_TIME;
	public static int SERVICES_NO_CARRIER_MAX_TIME;
	public static int SERVICES_NO_CARRIER_MIN_TIME;

	public static boolean SERVICES_PK_PVP_KILL_ENABLE;
	public static int SERVICES_PVP_KILL_REWARD_ITEM;
	public static long SERVICES_PVP_KILL_REWARD_COUNT;
	public static int SERVICES_PK_KILL_REWARD_ITEM;
	public static long SERVICES_PK_KILL_REWARD_COUNT;
	public static boolean SERVICES_PK_PVP_TIE_IF_SAME_IP;
	
	public static boolean ALT_OPEN_CLOAK_SLOT;

	public static boolean ALT_SHOW_SERVER_TIME;
	public static boolean CONSUMABLE_SHOT;
	public static boolean CONSUMABLE_ARROW;
	
	/** Geodata config */
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static String GEOFILES_PATTERN;
	public static boolean ALLOW_GEODATA;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static boolean ALLOW_KEYBOARD_MOVE;
	public static boolean COMPACT_GEO;
	public static int CLIENT_Z_SHIFT;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;

	/** Geodata (Pathfind) config */
	public static int PATHFIND_BOOST;
	public static boolean PATHFIND_DIAGONAL;
	public static boolean PATH_CLEAN;
	public static int PATHFIND_MAX_Z_DIFF;
	public static long PATHFIND_MAX_TIME;
	public static String PATHFIND_BUFFERS;

	public static boolean DEBUG;

	/* Item-Mall Configs */
	public static int GAME_POINT_ITEM_ID;

	public static int WEAR_DELAY;

	public static boolean GOODS_INVENTORY_ENABLED = false;
	public static boolean EX_NEW_PETITION_SYSTEM;
	public static boolean EX_JAPAN_MINIGAME;
	public static boolean EX_LECTURE_MARK;

	/* Top's Config */
	public static boolean L2_TOP_MANAGER_ENABLED;
	public static int L2_TOP_MANAGER_INTERVAL;
	public static String L2_TOP_WEB_ADDRESS;
	public static String L2_TOP_SMS_ADDRESS;
	public static String L2_TOP_SERVER_ADDRESS;
	public static int L2_TOP_SAVE_DAYS;
	public static int[] L2_TOP_REWARD;

	public static boolean MMO_TOP_MANAGER_ENABLED;
	public static int MMO_TOP_MANAGER_INTERVAL;
	public static String MMO_TOP_WEB_ADDRESS;
	public static String MMO_TOP_SERVER_ADDRESS;
	public static int MMO_TOP_SAVE_DAYS;
	public static int[] MMO_TOP_REWARD;
	
	public static boolean SMS_PAYMENT_MANAGER_ENABLED;
	public static String SMS_PAYMENT_WEB_ADDRESS;
	public static int SMS_PAYMENT_MANAGER_INTERVAL;
	public static int SMS_PAYMENT_SAVE_DAYS;
	public static String SMS_PAYMENT_SERVER_ADDRESS;
	public static int[] SMS_PAYMENT_REWARD;
	
	public static boolean AUTH_SERVER_GM_ONLY;
	public static boolean AUTH_SERVER_BRACKETS;
	public static boolean AUTH_SERVER_IS_PVP;
	public static int AUTH_SERVER_AGE_LIMIT;
	public static int AUTH_SERVER_SERVER_TYPE;

	/* Version Configs */
	public static String SERVER_VERSION;
	public static String SERVER_BUILD_DATE;

	/*Конфиг для ПА*/
	public static int SERVICES_RATE_TYPE;
	public static int SERVICES_RATE_CREATE_PA;
	public static int[] SERVICES_RATE_BONUS_PRICE;
	public static int[] SERVICES_RATE_BONUS_ITEM;
	public static double[] SERVICES_RATE_BONUS_VALUE;
	public static int[] SERVICES_RATE_BONUS_DAYS;
	public static int ENCHANT_CHANCE_WEAPON_PA;
	public static int ENCHANT_CHANCE_ARMOR_PA;
	public static int ENCHANT_CHANCE_ACCESSORY_PA;
	public static int ENCHANT_CHANCE_WEAPON_BLESS_PA;
	public static int ENCHANT_CHANCE_ARMOR_BLESS_PA;
	public static int ENCHANT_CHANCE_ACCESSORY_BLESS_PA;
	public static int ENCHANT_CHANCE_CRYSTAL_WEAPON_PA;
	public static int ENCHANT_CHANCE_CRYSTAL_ARMOR_PA;
	public static int ENCHANT_CHANCE_CRYSTAL_ACCESSORY_PA;
	
	public static double SERVICES_BONUS_XP;
	public static double SERVICES_BONUS_SP;
	public static double SERVICES_BONUS_ADENA;
	public static double SERVICES_BONUS_ITEMS;
	public static double SERVICES_BONUS_SPOIL;
	
	
	
	public static long MAX_PLAYER_CONTRIBUTION;
	public static boolean AUTO_LOOT_PA;

	/* Конфигурации Епиков */
	public static int FIXINTERVALOFANTHARAS_HOUR;
	public static int FIXINTERVALOFBAIUM_HOUR;
	public static int RANDOMINTERVALOFBAIUM;
	public static int FIXINTERVALOFBAYLORSPAWN_HOUR;
	public static int RANDOMINTERVALOFBAYLORSPAWN;
	public static int FIXINTERVALOFBELETHSPAWN_HOUR;
	public static int FIXINTERVALOFSAILRENSPAWN_HOUR;
	public static int RANDOMINTERVALOFSAILRENSPAWN;
	public static int FIXINTERVALOFVALAKAS;
	
	/* Количество очков репутации необходимое для поднятия уровня клану.*/
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;

	/* Количество человек в клане необходимое для поднятия уровня клану.*/
	public static int CLAN_LEVEL_6_REQUIREMEN;
	public static int CLAN_LEVEL_7_REQUIREMEN;
	public static int CLAN_LEVEL_8_REQUIREMEN;
	public static int CLAN_LEVEL_9_REQUIREMEN;
	public static int CLAN_LEVEL_10_REQUIREMEN;
	public static int CLAN_LEVEL_11_REQUIREMEN;

	public static int BLOOD_OATHS;
	public static int BLOOD_PLEDGES;
	public static int MIN_ACADEM_POINT;
	public static int MAX_ACADEM_POINT;

	public static boolean ZONE_PVP_COUNT;
	public static boolean SIEGE_PVP_COUNT;
	public static boolean EPIC_EXPERTISE_PENALTY;
	public static boolean EXPERTISE_PENALTY;

	public static int ALT_MUSIC_LIMIT;
	public static int ALT_DEBUFF_LIMIT;
	public static int ALT_TRIGGER_LIMIT;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static TIntIntHashMap SKILL_DURATION_LIST;

	public static boolean COMMUNITYBOARD_BOARD_ALT_ENABLED;
	public static int COMMUNITYBOARD_BUFF_PICE_NG;
	public static int COMMUNITYBOARD_BUFF_PICE_D;
	public static int COMMUNITYBOARD_BUFF_PICE_C;
	public static int COMMUNITYBOARD_BUFF_PICE_B;
	public static int COMMUNITYBOARD_BUFF_PICE_A;
	public static int COMMUNITYBOARD_BUFF_PICE_S;
	public static int COMMUNITYBOARD_BUFF_PICE_S80;
	public static int COMMUNITYBOARD_BUFF_PICE_S84;
	public static int COMMUNITYBOARD_BUFF_PICE_NG_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_D_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_C_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_B_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_A_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_S_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_S80_GR;
	public static int COMMUNITYBOARD_BUFF_PICE_S84_GR;
	public static int COMMUNITYBOARD_TELEPORT_PICE_NG;
	public static int COMMUNITYBOARD_TELEPORT_PICE_D;
	public static int COMMUNITYBOARD_TELEPORT_PICE_C;
	public static int COMMUNITYBOARD_TELEPORT_PICE_B;
	public static int COMMUNITYBOARD_TELEPORT_PICE_A;
	public static int COMMUNITYBOARD_TELEPORT_PICE_S;
	public static int COMMUNITYBOARD_TELEPORT_PICE_S80;
	public static int COMMUNITYBOARD_TELEPORT_PICE_S84;

	public static double ALT_VITALITY_NEVIT_UP_POINT;
	public static double ALT_VITALITY_NEVIT_POINT;

	public static boolean SERVICES_LVL_ENABLED;
	public static int SERVICES_LVL_UP_MAX;
	public static int SERVICES_LVL_UP_PRICE;
	public static int SERVICES_LVL_UP_ITEM;
	public static int SERVICES_LVL_DOWN_MAX;
	public static int SERVICES_LVL_DOWN_PRICE;
	public static int SERVICES_LVL_DOWN_ITEM;

	public static boolean ALLOW_INSTANCES_LEVEL_MANUAL;
	public static boolean ALLOW_INSTANCES_PARTY_MANUAL;
	public static int INSTANCES_LEVEL_MIN;
	public static int INSTANCES_LEVEL_MAX;
	public static int INSTANCES_PARTY_MIN;
	public static int INSTANCES_PARTY_MAX;

	// Items setting
	public static boolean CAN_BE_TRADED_NO_TARADEABLE;
	public static boolean CAN_BE_TRADED_NO_SELLABLE;
	public static boolean CAN_BE_TRADED_NO_STOREABLE;
	public static boolean CAN_BE_TRADED_SHADOW_ITEM;
	public static boolean CAN_BE_TRADED_HERO_WEAPON;
	public static boolean CAN_BE_WH_NO_TARADEABLE;
	public static boolean CAN_BE_CWH_NO_TARADEABLE;
	public static boolean CAN_BE_CWH_IS_AUGMENTED;
	public static boolean CAN_BE_WH_IS_AUGMENTED;
	public static boolean ALLOW_SOUL_SPIRIT_SHOT_INFINITELY;
	public static boolean ALLOW_ARROW_INFINITELY;
	
	public static boolean ALLOW_START_ITEMS;
	public static int[] START_ITEMS_MAGE;
	public static int[] START_ITEMS_MAGE_COUNT;
	public static int[] START_ITEMS_FITHER;
	public static int[] START_ITEMS_FITHER_COUNT;
	
	public static int HELLBOUND_LEVEL;
    
    public static boolean COMMUNITYBOARD_ENCHANT_ENABLED;
	public static boolean ALLOW_BBS_ENCHANT_ELEMENTAR;
	public static boolean ALLOW_BBS_ENCHANT_ATT;
    public static int COMMUNITYBOARD_ENCHANT_ITEM;
    public static int COMMUNITYBOARD_MAX_ENCHANT;
    public static int[] COMMUNITYBOARD_ENCHANT_LVL;
    public static int[] COMMUNITYBOARD_ENCHANT_PRICE_WEAPON;
    public static int[] COMMUNITYBOARD_ENCHANT_PRICE_ARMOR;
    public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_WEAPON;
    public static int[] COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_WEAPON;
    public static int[]	COMMUNITYBOARD_ENCHANT_ATRIBUTE_LVL_ARMOR;
    public static int[]	COMMUNITYBOARD_ENCHANT_ATRIBUTE_PRICE_ARMOR;
    public static boolean COMMUNITYBOARD_ENCHANT_ATRIBUTE_PVP;
        
    public static boolean USE_ALT_ENCHANT_PA;
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_BLESSED_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_WEAPON_FIGHT_CRYSTAL_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_CRYSTAL_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_BLESSED_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_CRYSTAL_PA = new ArrayList<Integer>();
	public static ArrayList<Integer> ENCHANT_ARMOR_JEWELRY_BLESSED_PA = new ArrayList<Integer>();

    public static int EVENT_LastHeroItemID;
    public static double EVENT_LastHeroItemCOUNT;
    public static int EVENT_LastHeroTime;
    public static boolean EVENT_LastHeroRate;
    public static double EVENT_LastHeroItemCOUNTFinal;
    public static boolean EVENT_LastHeroRateFinal;
    public static int EVENT_LastHeroChanceToStart;

    public static int EVENT_TvTItemID;
    public static double EVENT_TvTItemCOUNT;
    public static int EVENT_TvTChanceToStart;
	
	public static boolean ALLOW_MULTILANG_GATEKEEPER;

	public static boolean LOAD_CUSTOM_SPAWN;
	public static boolean SAVE_GM_SPAWN;

	public static void loadServerConfig()
	{
		ExProperties serverSettings = load(CONFIGURATION_FILE);

		R_GUARD = serverSettings.getProperty("R_GUARD", true);
		GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
		GAME_SERVER_LOGIN_PORT = serverSettings.getProperty("LoginPort", 9013);
		GAME_SERVER_LOGIN_CRYPT = serverSettings.getProperty("LoginUseCrypt", true);

		AUTH_SERVER_AGE_LIMIT = serverSettings.getProperty("ServerAgeLimit", 0);
		AUTH_SERVER_GM_ONLY = serverSettings.getProperty("ServerGMOnly", false);
		AUTH_SERVER_BRACKETS = serverSettings.getProperty("ServerBrackets", false);
		AUTH_SERVER_IS_PVP = serverSettings.getProperty("PvPServer", false);
		for(String a : serverSettings.getProperty("ServerType", ArrayUtils.EMPTY_STRING_ARRAY))
		{
			if(a.trim().isEmpty())
				continue;

			ServerType t = ServerType.valueOf(a.toUpperCase());
			AUTH_SERVER_SERVER_TYPE |= t.getMask();
		}

		SECOND_AUTH_ENABLED = serverSettings.getProperty("SAEnabled", false);
		SECOND_AUTH_BAN_ACC = serverSettings.getProperty("SABanAccEnabled", false);
		SECOND_AUTH_STRONG_PASS = serverSettings.getProperty("SAStrongPass", false);
		SECOND_AUTH_MAX_ATTEMPTS = serverSettings.getProperty("SAMaxAttemps", 5);
		SECOND_AUTH_BAN_TIME = serverSettings.getProperty("SABanTime", 480);
		SECOND_AUTH_REC_LINK = serverSettings.getProperty("SARecoveryLink", "http://www.my-domain.com/charPassRec.php");

		
		INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");
		EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "*");
		ADVIPSYSTEM = serverSettings.getProperty("AdvIPSystem", false);
		REQUEST_ID = serverSettings.getProperty("RequestServerID", 0);
		ACCEPT_ALTERNATE_ID = serverSettings.getProperty("AcceptAlternateID", true);

		GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
		PORTS_GAME = serverSettings.getProperty("GameserverPort", new int[] { 7777 });

		EVERYBODY_HAS_ADMIN_RIGHTS = serverSettings.getProperty("EverybodyHasAdminRights", false);

		HIDE_GM_STATUS = serverSettings.getProperty("HideGMStatus", false);
		SHOW_GM_LOGIN = serverSettings.getProperty("ShowGMLogin", true);
		SAVE_GM_EFFECTS = serverSettings.getProperty("SaveGMEffects", false);

		CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{2,16}");
		CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");
		CLAN_TITLE_TEMPLATE = serverSettings.getProperty("ClanTitleTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f \\p{Punct}]{1,16}");
		ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", "[A-Za-z0-9\u0410-\u042f\u0430-\u044f]{3,16}");

		PARALIZE_ON_RAID_DIFF = serverSettings.getProperty("ParalizeOnRaidLevelDiff", true);

		AUTODESTROY_ITEM_AFTER = serverSettings.getProperty("AutoDestroyDroppedItemAfter", 0);
		AUTODESTROY_PLAYER_ITEM_AFTER = serverSettings.getProperty("AutoDestroyPlayerDroppedItemAfter", 0);
		DELETE_DAYS = serverSettings.getProperty("DeleteCharAfterDays", 7);
		PURGE_BYPASS_TASK_FREQUENCY = serverSettings.getProperty("PurgeTaskFrequency", 60);

		try
		{
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
		}
		catch(IOException e)
		{
			_log.error("", e);
		}

		ALLOW_DISCARDITEM = serverSettings.getProperty("AllowDiscardItem", true);
		ALLOW_MAIL = serverSettings.getProperty("AllowMail", true);
		ALLOW_WAREHOUSE = serverSettings.getProperty("AllowWarehouse", true);
		ALLOW_WATER = serverSettings.getProperty("AllowWater", true);
		ALLOW_CURSED_WEAPONS = serverSettings.getProperty("AllowCursedWeapons", false);
		DROP_CURSED_WEAPONS_ON_KICK = serverSettings.getProperty("DropCursedWeaponsOnKick", false);

		MIN_PROTOCOL_REVISION = serverSettings.getProperty("MinProtocolRevision", 267);
		MAX_PROTOCOL_REVISION = serverSettings.getProperty("MaxProtocolRevision", 271);

		AUTOSAVE = serverSettings.getProperty("Autosave", true);

		MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 3000);
		ONLINE_PLUS = serverSettings.getProperty("OnlineUsersPlus", 1);

		DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
		DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
		DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 600);
		DATABASE_IDLE_TEST_PERIOD = serverSettings.getProperty("IdleConnectionTestPeriod", 60);

		DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
		DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
		DATABASE_PASSWORD = serverSettings.getProperty("Password", "");

		USER_INFO_INTERVAL = serverSettings.getProperty("UserInfoInterval", 100L);
		BROADCAST_STATS_INTERVAL = serverSettings.getProperty("BroadcastStatsInterval", true);
		BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100L);

		EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);

		SCHEDULED_THREAD_POOL_SIZE = serverSettings.getProperty("ScheduledThreadPoolSize", NCPUS * 4);
		EXECUTOR_THREAD_POOL_SIZE = serverSettings.getProperty("ExecutorThreadPoolSize", NCPUS * 2);

		ENABLE_RUNNABLE_STATS = serverSettings.getProperty("EnableRunnableStats", false);

		SELECTOR_CONFIG.SLEEP_TIME = serverSettings.getProperty("SelectorSleepTime", 10L);
		SELECTOR_CONFIG.INTEREST_DELAY = serverSettings.getProperty("InterestDelay", 30L);
		SELECTOR_CONFIG.MAX_SEND_PER_PASS = serverSettings.getProperty("MaxSendPerPass", 32);
		SELECTOR_CONFIG.READ_BUFFER_SIZE = serverSettings.getProperty("ReadBufferSize", 65536);
		SELECTOR_CONFIG.WRITE_BUFFER_SIZE = serverSettings.getProperty("WriteBufferSize", 131072);
		SELECTOR_CONFIG.HELPER_BUFFER_COUNT = serverSettings.getProperty("BufferPoolSize", 64);

		DEFAULT_LANG = serverSettings.getProperty("DefaultLang", "ru");
		RESTART_AT_TIME = serverSettings.getProperty("AutoRestartAt", "0 5 * * *");
		SHIFT_BY = serverSettings.getProperty("HShift", 12);
		SHIFT_BY_Z = serverSettings.getProperty("VShift", 11);
		MAP_MIN_Z = serverSettings.getProperty("MapMinZ", -32768);
		MAP_MAX_Z = serverSettings.getProperty("MapMaxZ", 32767);

		MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketDelay", 100);
		ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketDelay", 500);

		DAMAGE_FROM_FALLING = serverSettings.getProperty("DamageFromFalling", true);

		MAX_REFLECTIONS_COUNT = serverSettings.getProperty("MaxReflectionsCount", 300);

		WEAR_DELAY = serverSettings.getProperty("WearDelay", 5);

		HTM_CACHE_MODE = serverSettings.getProperty("HtmCacheMode", HtmCache.LAZY);

		ALT_VITALITY_NEVIT_UP_POINT = serverSettings.getProperty("WebServerDelay", 10);
		ALT_VITALITY_NEVIT_POINT = serverSettings.getProperty("WebServerDelay", 10);

		ALLOW_ADDONS_CONFIG = serverSettings.getProperty("AllowAddonsConfig", false);

		ALLOW_IP_LOCK = serverSettings.getProperty("AllowLockIP", false);
		ALLOW_HWID_LOCK = serverSettings.getProperty("AllowLockHwid", false);
		HWID_LOCK_MASK = serverSettings.getProperty("HwidLockMask", 10);
	}

	public static void loadChatConfig()
	{
		ExProperties chatSettings = load(CHAT_FILE);

		GLOBAL_SHOUT = chatSettings.getProperty("GlobalShout", false);
		GLOBAL_TRADE_CHAT = chatSettings.getProperty("GlobalTradeChat", false);
		CHAT_RANGE = chatSettings.getProperty("ChatRange", 1250);
		SHOUT_OFFSET = chatSettings.getProperty("ShoutOffset", 0);
		
		TRADE_WORDS = new GArray<String>();

		String T_WORLD = chatSettings.getProperty("TradeWords", "продаю,проgаю,пр0даю,продам,проgам,пр0дам,покупаю,куплю,кyплю,обменяю,выменяю,ВТТ,ВТС,ВТБ,WTB,WTT,WTS");
		String[] T_WORLDS = T_WORLD.split(",", -1);
		for(String w : T_WORLDS)
			TRADE_WORDS.add(w);
		System.out.println("Loaded " + TRADE_WORDS.size() + " trade words.");
		
		LOG_CHAT = chatSettings.getProperty("LogChat", false);
		CHAT_MESSAGE_MAX_LEN = chatSettings.getProperty("ChatMessageLimit", 1000);
		ABUSEWORD_BANCHAT = chatSettings.getProperty("ABUSEWORD_BANCHAT", false);
		int counter = 0;
		for(int id : chatSettings.getProperty("ABUSEWORD_BAN_CHANNEL", new int[] { 0 }))
		{
			BAN_CHANNEL_LIST[counter] = id;
			counter++;
		}
		ABUSEWORD_REPLACE = chatSettings.getProperty("ABUSEWORD_REPLACE", false);
		ABUSEWORD_REPLACE_STRING = chatSettings.getProperty("ABUSEWORD_REPLACE_STRING", "[censored]");
		BANCHAT_ANNOUNCE = chatSettings.getProperty("BANCHAT_ANNOUNCE", true);
		BANCHAT_ANNOUNCE_FOR_ALL_WORLD = chatSettings.getProperty("BANCHAT_ANNOUNCE_FOR_ALL_WORLD", true);
		BANCHAT_ANNOUNCE_NICK = chatSettings.getProperty("BANCHAT_ANNOUNCE_NICK", true);
		ABUSEWORD_BANTIME = chatSettings.getProperty("ABUSEWORD_UNBAN_TIMER", 30);
		CHATFILTER_MIN_LEVEL = chatSettings.getProperty("ChatFilterMinLevel", 0);
		counter = 0;
		for(int id : chatSettings.getProperty("ChatFilterChannels", new int[] { 1, 8 }))
		{
			CHATFILTER_CHANNELS[counter] = id;
			counter++;
		}
		CHATFILTER_WORK_TYPE = chatSettings.getProperty("ChatFilterWorkType", 1);
	}


	public static void loadTelnetConfig()
	{
		ExProperties telnetSettings = load(TELNET_CONFIGURATION_FILE);

		IS_TELNET_ENABLED = telnetSettings.getProperty("EnableTelnet", false);
		TELNET_DEFAULT_ENCODING = telnetSettings.getProperty("TelnetEncoding", "UTF-8");
		TELNET_PORT = telnetSettings.getProperty("Port", 7000);
		TELNET_HOSTNAME = telnetSettings.getProperty("BindAddress", "127.0.0.1");
		TELNET_PASSWORD = telnetSettings.getProperty("Password", "");
	}
	public static boolean SPAWN_WEDDING;
	public static void loadWeddingConfig()
	{
		ExProperties weddingSettings = load(WEDDING_FILE);

		ALLOW_WEDDING = weddingSettings.getProperty("AllowWedding", false);
		WEDDING_PRICE = weddingSettings.getProperty("WeddingPrice", 500000);
		WEDDING_PUNISH_INFIDELITY = weddingSettings.getProperty("WeddingPunishInfidelity", true);
		WEDDING_TELEPORT = weddingSettings.getProperty("WeddingTeleport", true);
		WEDDING_TELEPORT_PRICE = weddingSettings.getProperty("WeddingTeleportPrice", 500000);
		WEDDING_TELEPORT_INTERVAL = weddingSettings.getProperty("WeddingTeleportInterval", 120);
		WEDDING_SAMESEX = weddingSettings.getProperty("WeddingAllowSameSex", true);
		WEDDING_FORMALWEAR = weddingSettings.getProperty("WeddingFormalWear", true);
		WEDDING_DIVORCE_COSTS = weddingSettings.getProperty("WeddingDivorceCosts", 20);
		SPAWN_WEDDING = weddingSettings.getProperty("SPAWN_WEDDING", false);
	}

	public static void loadResidenceConfig()
	{
		ExProperties residenceSettings = load(RESIDENCE_CONFIG_FILE);

		CH_BID_GRADE1_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanLevel", 2);
		CH_BID_GRADE1_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembers", 1);
		CH_BID_GRADE1_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade1_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE2_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanLevel", 2);
		CH_BID_GRADE2_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembers", 1);
		CH_BID_GRADE2_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade2_MinClanMembersAvgLevel", 1);
		CH_BID_GRADE3_MINCLANLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanLevel", 2);
		CH_BID_GRADE3_MINCLANMEMBERS = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembers", 1);
		CH_BID_GRADE3_MINCLANMEMBERSLEVEL = residenceSettings.getProperty("ClanHallBid_Grade3_MinClanMembersAvgLevel", 1);
		RESIDENCE_LEASE_FUNC_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseFuncMultiplier", 1.);
		RESIDENCE_LEASE_MULTIPLIER = residenceSettings.getProperty("ResidenceLeaseMultiplier", 1.);

		CASTLE_SELECT_HOURS = residenceSettings.getProperty("CastleSelectHours", new int[]{16, 20});
		int[] tempCastleValidatonTime = residenceSettings.getProperty("CastleValidationDate", new int[] {2,4,2003});
		CASTLE_VALIDATION_DATE = Calendar.getInstance();
		CASTLE_VALIDATION_DATE.set(Calendar.DAY_OF_MONTH, tempCastleValidatonTime[0]);
		CASTLE_VALIDATION_DATE.set(Calendar.MONTH, tempCastleValidatonTime[1] - 1);
		CASTLE_VALIDATION_DATE.set(Calendar.YEAR, tempCastleValidatonTime[2]);
		CASTLE_VALIDATION_DATE.set(Calendar.HOUR_OF_DAY, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.MINUTE, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.SECOND, 0);
		CASTLE_VALIDATION_DATE.set(Calendar.MILLISECOND, 0);
	}


	public static void loadItemsUseConfig()
	{
		ExProperties itemsUseSettings = load(ITEM_USE_FILE);
	
		ITEM_USE_LIST_ID = itemsUseSettings.getProperty("ItemUseListId", new int[] {725,726,727,728});
		ITEM_USE_IS_COMBAT_FLAG = itemsUseSettings.getProperty("ItemUseIsCombatFlag", true);
		ITEM_USE_IS_ATTACK = itemsUseSettings.getProperty("ItemUseIsAttack", true);
		ITEM_USE_IS_EVENTS = itemsUseSettings.getProperty("ItemUseIsEvents", true);
	}

	public static void loadFightClubSettings()
	{
		ExProperties eventFightClubSettings = load(EVENT_FIGHT_CLUB_FILE);

		FIGHT_CLUB_ENABLED = eventFightClubSettings.getProperty("FightClubEnabled", false);
		MINIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MinimumLevel", 1);
		MAXIMUM_LEVEL_TO_PARRICIPATION = eventFightClubSettings.getProperty("MaximumLevel", 85);
		MAXIMUM_LEVEL_DIFFERENCE = eventFightClubSettings.getProperty("MaximumLevelDifference", 10);
		ALLOWED_RATE_ITEMS = eventFightClubSettings.getProperty("AllowedItems", "").trim().replaceAll(" ", "").split(",");
		PLAYERS_PER_PAGE = eventFightClubSettings.getProperty("RatesOnPage", 10);
		ARENA_TELEPORT_DELAY = eventFightClubSettings.getProperty("ArenaTeleportDelay", 5);
		CANCEL_BUFF_BEFORE_FIGHT = eventFightClubSettings.getProperty("CancelBuffs", true);
		UNSUMMON_PETS = eventFightClubSettings.getProperty("UnsummonPets", true);
		UNSUMMON_SUMMONS = eventFightClubSettings.getProperty("UnsummonSummons", true);
		REMOVE_CLAN_SKILLS = eventFightClubSettings.getProperty("RemoveClanSkills", false);
		REMOVE_HERO_SKILLS = eventFightClubSettings.getProperty("RemoveHeroSkills", false);
		TIME_TO_PREPARATION = eventFightClubSettings.getProperty("TimeToPreparation", 10);
		FIGHT_TIME = eventFightClubSettings.getProperty("TimeToDraw", 300);
		ALLOW_DRAW = eventFightClubSettings.getProperty("AllowDraw", true);
		TIME_TELEPORT_BACK = eventFightClubSettings.getProperty("TimeToBack", 10);
		FIGHT_CLUB_ANNOUNCE_RATE = eventFightClubSettings.getProperty("AnnounceRate", false);
		FIGHT_CLUB_ANNOUNCE_RATE_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceRateToAllScreen", false);
		FIGHT_CLUB_ANNOUNCE_START_TO_SCREEN = eventFightClubSettings.getProperty("AnnounceStartBatleToAllScreen", false);
	}
	
	public static void loadRatesConfig()
	{
		ExProperties ratesSettings = load(RATES_FILE);

		RATE_XP = ratesSettings.getProperty("RateXp", 1.);
		RATE_SP = ratesSettings.getProperty("RateSp", 1.);
		RATE_QUESTS_REWARD = ratesSettings.getProperty("RateQuestsReward", 1.);
		RATE_QUESTS_DROP = ratesSettings.getProperty("RateQuestsDrop", 1.);
		RATE_DROP_CHAMPION = ratesSettings.getProperty("RateDropChampion", 1.);
		RATE_CLAN_REP_SCORE = ratesSettings.getProperty("RateClanRepScore", 1.);
		RATE_CLAN_REP_SCORE_MAX_AFFECTED = ratesSettings.getProperty("RateClanRepScoreMaxAffected", 2);
		RATE_DROP_ADENA = ratesSettings.getProperty("RateDropAdena", 1.);
		RATE_CHAMPION_DROP_ADENA = ratesSettings.getProperty("RateChampionDropAdena", 1.);
		RATE_DROP_SPOIL_CHAMPION = ratesSettings.getProperty("RateSpoilChampion", 1.);
		RATE_DROP_ITEMS = ratesSettings.getProperty("RateDropItems", 1.);
		RATE_CHANCE_GROUP_DROP_ITEMS = ratesSettings.getProperty("RateChanceGroupDropItems", 1.);
		RATE_CHANCE_DROP_ITEMS = ratesSettings.getProperty("RateChanceDropItems", 1.);
		RATE_CHANCE_DROP_HERBS = ratesSettings.getProperty("RateChanceDropHerbs", 1.);
		RATE_CHANCE_SPOIL = ratesSettings.getProperty("RateChanceSpoil", 1.);
		RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceSpoilWAA", 1.);
		RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceDropWAA", 1.);
		RATE_CHANCE_DROP_EPOLET = ratesSettings.getProperty("RateChanceDropEpolets", 1.);
		NO_RATE_ENCHANT_SCROLL = ratesSettings.getProperty("NoRateEnchantScroll", true);
		CHAMPION_DROP_ONLY_ADENA = ratesSettings.getProperty("ChampionDropOnlyAdena", false);
		RATE_ENCHANT_SCROLL = ratesSettings.getProperty("RateDropEnchantScroll", 1.);
		NO_RATE_HERBS = ratesSettings.getProperty("NoRateHerbs", true);
		RATE_DROP_HERBS = ratesSettings.getProperty("RateDropHerbs", 1.);
		NO_RATE_ATT = ratesSettings.getProperty("NoRateAtt", true);
		RATE_DROP_ATT = ratesSettings.getProperty("RateDropAtt", 1.);
		NO_RATE_LIFE_STONE = ratesSettings.getProperty("NoRateLifeStone", true);
		NO_RATE_FORGOTTEN_SCROLL = ratesSettings.getProperty("NoRateForgottenScroll", true);
		RATE_DROP_LIFE_STONE = ratesSettings.getProperty("RateDropLifeStone", 1.);
		NO_RATE_KEY_MATERIAL = ratesSettings.getProperty("NoRateKeyMaterial", true);
		RATE_DROP_KEY_MATERIAL = ratesSettings.getProperty("RateDropKeyMaterial", 1.);
		NO_RATE_RECIPES= ratesSettings.getProperty("NoRateRecipes", true);
		RATE_DROP_RECIPES = ratesSettings.getProperty("RateDropRecipes", 1.);
		RATE_DROP_COMMON_ITEMS = ratesSettings.getProperty("RateDropCommonItems", 1.);
		NO_RATE_RAIDBOSS = ratesSettings.getProperty("NoRateRaidBoss", false);
		RATE_DROP_RAIDBOSS = ratesSettings.getProperty("RateRaidBoss", 1.);
		RATE_DROP_SPOIL = ratesSettings.getProperty("RateDropSpoil", 1.);
		NO_RATE_ITEMS = ratesSettings.getProperty("NoRateItemIds", new int[] {
				6660,
				6662,
				6661,
				6659,
				6656,
				6658,
				8191,
				6657,
				10170,
				10314,
				16025,
				16026 });
		NO_RATE_EQUIPMENT = ratesSettings.getProperty("NoRateEquipment", true);
		NO_RATE_SIEGE_GUARD = ratesSettings.getProperty("NoRateSiegeGuard", false);
		RATE_DROP_SIEGE_GUARD = ratesSettings.getProperty("RateSiegeGuard", 1.);
		RATE_MANOR = ratesSettings.getProperty("RateManor", 1.);
		RATE_FISH_DROP_COUNT = ratesSettings.getProperty("RateFishDropCount", 1.);
		RATE_PARTY_MIN = ratesSettings.getProperty("RatePartyMin", false);
		RATE_HELLBOUND_CONFIDENCE = ratesSettings.getProperty("RateHellboundConfidence", 1.);

		RATE_MOB_SPAWN = ratesSettings.getProperty("RateMobSpawn", 1);
		RATE_MOB_SPAWN_MIN_LEVEL = ratesSettings.getProperty("RateMobMinLevel", 1);
		RATE_MOB_SPAWN_MAX_LEVEL = ratesSettings.getProperty("RateMobMaxLevel", 100);
	}

	public static void loadBossConfig()
	{
		ExProperties bossSettings = load(BOSS_FILE);

		RATE_RAID_REGEN = bossSettings.getProperty("RateRaidRegen", 1.);
		RATE_RAID_DEFENSE = bossSettings.getProperty("RateRaidDefense", 1.);
		RATE_RAID_ATTACK = bossSettings.getProperty("RateRaidAttack", 1.);
		RATE_EPIC_DEFENSE = bossSettings.getProperty("RateEpicDefense", RATE_RAID_DEFENSE);
		RATE_EPIC_ATTACK = bossSettings.getProperty("RateEpicAttack", RATE_RAID_ATTACK);
		RAID_MAX_LEVEL_DIFF = bossSettings.getProperty("RaidMaxLevelDiff", 8);
	}

	public static void loadNpcConfig()
	{
		ExProperties npcSettings = load(NPC_FILE);

		MIN_NPC_ANIMATION = npcSettings.getProperty("MinNPCAnimation", 5);
		MAX_NPC_ANIMATION = npcSettings.getProperty("MaxNPCAnimation", 90);
		SERVER_SIDE_NPC_NAME = npcSettings.getProperty("ServerSideNpcName", false);
		SERVER_SIDE_NPC_TITLE = npcSettings.getProperty("ServerSideNpcTitle", false);
		SERVER_SIDE_NPC_TITLE_ETC = npcSettings.getProperty("ServerSideNpcTitleEtc", false);
	}

	public static void loadOtherConfig()
	{
		ExProperties otherSettings = load(OTHER_CONFIG_FILE);

		DEEPBLUE_DROP_RULES = otherSettings.getProperty("UseDeepBlueDropRules", true);
		DEEPBLUE_DROP_MAXDIFF = otherSettings.getProperty("DeepBlueDropMaxDiff", 8);
		DEEPBLUE_DROP_RAID_MAXDIFF = otherSettings.getProperty("DeepBlueDropRaidMaxDiff", 2);

		SWIMING_SPEED = otherSettings.getProperty("SwimingSpeedTemplate", 50);
		/* All item price 1 adena */
		SELL_ALL_ITEMS_FREE = otherSettings.getProperty("SellAllItemsFree", false);
		/* Inventory slots limits */
		INVENTORY_MAXIMUM_NO_DWARF = otherSettings.getProperty("MaximumSlotsForNoDwarf", 80);
		INVENTORY_MAXIMUM_DWARF = otherSettings.getProperty("MaximumSlotsForDwarf", 100);
		INVENTORY_MAXIMUM_GM = otherSettings.getProperty("MaximumSlotsForGMPlayer", 250);
		QUEST_INVENTORY_MAXIMUM = otherSettings.getProperty("MaximumSlotsForQuests", 100);

		MULTISELL_SIZE = otherSettings.getProperty("MultisellPageSize", 10);

		/* Warehouse slots limits */
		WAREHOUSE_SLOTS_NO_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForNoDwarf", 100);
		WAREHOUSE_SLOTS_DWARF = otherSettings.getProperty("BaseWarehouseSlotsForDwarf", 120);
		WAREHOUSE_SLOTS_CLAN = otherSettings.getProperty("MaximumWarehouseSlotsForClan", 200);
		FREIGHT_SLOTS = otherSettings.getProperty("MaximumFreightSlots", 10);

		/* chance to enchant an item over safe level */
		ENCHANT_CHANCE_WEAPON = otherSettings.getProperty("EnchantChance", 66);
		ENCHANT_CHANCE_ARMOR = otherSettings.getProperty("EnchantChanceArmor", ENCHANT_CHANCE_WEAPON);
		ENCHANT_CHANCE_ACCESSORY = otherSettings.getProperty("EnchantChanceAccessory", ENCHANT_CHANCE_ARMOR);
		ENCHANT_CHANCE_CRYSTAL_WEAPON = otherSettings.getProperty("EnchantChanceCrystal", 66);
		ENCHANT_CHANCE_CRYSTAL_ARMOR = otherSettings.getProperty("EnchantChanceCrystalArmor", ENCHANT_CHANCE_CRYSTAL_WEAPON);
		ENCHANT_CHANCE_CRYSTAL_ARMOR_OLF = otherSettings.getProperty("EnchantChanceCrystalArmorOlf", 66);
		ENCHANT_CHANCE_CRYSTAL_ACCESSORY = otherSettings.getProperty("EnchantChanceCrystalAccessory", ENCHANT_CHANCE_CRYSTAL_ARMOR);
		SAFE_ENCHANT_COMMON = otherSettings.getProperty("SafeEnchantCommon", 3);
		SAFE_ENCHANT_FULL_BODY = otherSettings.getProperty("SafeEnchantFullBody", 4);
		ENCHANT_MAX = otherSettings.getProperty("EnchantMax", 20);
		SAFE_ENCHANT_LVL = otherSettings.getProperty("SafeEnchant", 0);
		ARMOR_OVERENCHANT_HPBONUS_LIMIT = otherSettings.getProperty("ArmorOverEnchantHPBonusLimit", 10) - 3;
		SHOW_ENCHANT_EFFECT_RESULT = otherSettings.getProperty("ShowEnchantEffectResult", false);

		ENCHANT_CHANCE_WEAPON_BLESS = otherSettings.getProperty("EnchantChanceBless", 66);
		ENCHANT_CHANCE_ARMOR_BLESS = otherSettings.getProperty("EnchantChanceArmorBless", ENCHANT_CHANCE_WEAPON);
		ENCHANT_CHANCE_ACCESSORY_BLESS = otherSettings.getProperty("EnchantChanceAccessoryBless", ENCHANT_CHANCE_ARMOR);
		USE_ALT_ENCHANT = Boolean.parseBoolean(otherSettings.getProperty("UseAltEnchant", "False"));
		for (String prop : otherSettings.getProperty("EnchantWeaponFighter", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantWeaponFighterCrystal", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_BLESSED.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantWeaponFighterBlessed", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_CRYSTAL.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantArmor", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantArmorCrystal", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_CRYSTAL.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantArmorBlessed", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_BLESSED.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantJewelry", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantJewelryCrystal", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_CRYSTAL.add(Integer.parseInt(prop));
		for (String prop : otherSettings.getProperty("EnchantJewelryBlessed", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_BLESSED.add(Integer.parseInt(prop));

		ENCHANT_ATTRIBUTE_STONE_CHANCE = otherSettings.getProperty("EnchantAttributeChance", 50);
		ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE = otherSettings.getProperty("EnchantAttributeCrystalChance", 30);

		REGEN_SIT_WAIT = otherSettings.getProperty("RegenSitWait", false);

		STARTING_ADENA = otherSettings.getProperty("StartingAdena", 0);
		UNSTUCK_SKILL = otherSettings.getProperty("UnstuckSkill", true);

		/* Amount of HP, MP, and CP is restored */
		RESPAWN_RESTORE_CP = otherSettings.getProperty("RespawnRestoreCP", 0.) / 100;
		RESPAWN_RESTORE_HP = otherSettings.getProperty("RespawnRestoreHP", 65.) / 100;
		RESPAWN_RESTORE_MP = otherSettings.getProperty("RespawnRestoreMP", 0.) / 100;

		/* Maximum number of available slots for pvt stores */
		MAX_PVTSTORE_SLOTS_DWARF = otherSettings.getProperty("MaxPvtStoreSlotsDwarf", 5);
		MAX_PVTSTORE_SLOTS_OTHER = otherSettings.getProperty("MaxPvtStoreSlotsOther", 4);
		MAX_PVTCRAFT_SLOTS = otherSettings.getProperty("MaxPvtManufactureSlots", 20);

		SENDSTATUS_TRADE_JUST_OFFLINE = otherSettings.getProperty("SendStatusTradeJustOffline", false);
		SENDSTATUS_TRADE_MOD = otherSettings.getProperty("SendStatusTradeMod", 1.);
		SHOW_OFFLINE_MODE_IN_ONLINE = otherSettings.getProperty("ShowOfflineTradeInOnline", false);

		ANNOUNCE_MAMMON_SPAWN = otherSettings.getProperty("AnnounceMammonSpawn", true);

		GM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("GMNameColour", "FFFFFF"));
		GM_HERO_AURA = otherSettings.getProperty("GMHeroAura", false);
		NORMAL_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("NormalNameColour", "FFFFFF"));
		CLANLEADER_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("ClanleaderNameColour", "FFFFFF"));

		GAME_POINT_ITEM_ID = otherSettings.getProperty("GamePointItemId", -1);
		STARTING_LVL = otherSettings.getProperty("StartingLvL", 0);
		MAX_PLAYER_CONTRIBUTION = otherSettings.getProperty("MaxPlayerContribution", 1000000);
		
		ENCHANT_MAX_WEAPON = otherSettings.getProperty("EnchantMaxWeapon", 20);
		ENCHANT_MAX_ARMOR = otherSettings.getProperty("EnchantMaxArmor", 20);
		ENCHANT_MAX_JEWELRY = otherSettings.getProperty("EnchantMaxJewelry", 20);
	}

	public static void loadSpoilConfig()
	{
		ExProperties spoilSettings = load(SPOIL_CONFIG_FILE);

		BASE_SPOIL_RATE = spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", 78.);
		MINIMUM_SPOIL_RATE = spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", 1.);
		ALT_SPOIL_FORMULA = spoilSettings.getProperty("AltFormula", false);
		MANOR_SOWING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingSuccess", 100.);
		MANOR_SOWING_ALT_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfSowingAltSuccess", 10.);
		MANOR_HARVESTING_BASIC_SUCCESS = spoilSettings.getProperty("BasePercentChanceOfHarvestingSuccess", 90.);
		MANOR_DIFF_PLAYER_TARGET = spoilSettings.getProperty("MinDiffPlayerMob", 5);
		MANOR_DIFF_PLAYER_TARGET_PENALTY = spoilSettings.getProperty("DiffPlayerMobPenalty", 5.);
		MANOR_DIFF_SEED_TARGET = spoilSettings.getProperty("MinDiffSeedMob", 5);
		MANOR_DIFF_SEED_TARGET_PENALTY = spoilSettings.getProperty("DiffSeedMobPenalty", 5.);
		ALLOW_MANOR = spoilSettings.getProperty("AllowManor", true);
		MANOR_REFRESH_TIME = spoilSettings.getProperty("AltManorRefreshTime", 20);
		MANOR_REFRESH_MIN = spoilSettings.getProperty("AltManorRefreshMin", 00);
		MANOR_APPROVE_TIME = spoilSettings.getProperty("AltManorApproveTime", 6);
		MANOR_APPROVE_MIN = spoilSettings.getProperty("AltManorApproveMin", 00);
		MANOR_MAINTENANCE_PERIOD = spoilSettings.getProperty("AltManorMaintenancePeriod", 360000);
	}

	public static void loadInstancesConfig()
	{
		ExProperties instancesSettings = load(INSTANCES_FILE);

		ALLOW_INSTANCES_LEVEL_MANUAL = instancesSettings.getProperty("AllowInstancesLevelManual", false);
		ALLOW_INSTANCES_PARTY_MANUAL = instancesSettings.getProperty("AllowInstancesPartyManual", false);
		INSTANCES_LEVEL_MIN = instancesSettings.getProperty("InstancesLevelMin", 1);
		INSTANCES_LEVEL_MAX = instancesSettings.getProperty("InstancesLevelMax", 85);
		INSTANCES_PARTY_MIN = instancesSettings.getProperty("InstancesPartyMin", 2);
		INSTANCES_PARTY_MAX = instancesSettings.getProperty("InstancesPartyMax", 100);
	}

	public static void loadEpicBossConfig()
	{
		ExProperties epicBossSettings = load(EPIC_BOSS_FILE);

		FIXINTERVALOFANTHARAS_HOUR = epicBossSettings.getProperty("FWA_FIX_INTERVAL_OF_ANTHARAS_HOUR", 264);
		FIXINTERVALOFBAIUM_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_BAIUM_HOUR", 120);
		RANDOMINTERVALOFBAIUM = epicBossSettings.getProperty("RANDOM_INTERVAL_OF_BAIUM", 8);
		FIXINTERVALOFBAYLORSPAWN_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_BAYLOR_SPAWN_HOUR", 24);
		RANDOMINTERVALOFBAYLORSPAWN = epicBossSettings.getProperty("RANDOM_INTERVAL_OF_BAYLOR_SPAWN", 24);
		FIXINTERVALOFBELETHSPAWN_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_BELETH_SPAWN_HOUR", 48);
		FIXINTERVALOFSAILRENSPAWN_HOUR = epicBossSettings.getProperty("FIX_INTERVAL_OF_SAILREN_SPAWN_HOUR", 24);
		RANDOMINTERVALOFSAILRENSPAWN = epicBossSettings.getProperty("RANDOM_INTERVAL_OF_SAILREN_SPAWN", 24);
		FIXINTERVALOFVALAKAS = epicBossSettings.getProperty("FIX_INTERVAL_OF_VALAKAS_HOUR", 264);
	}

	public static void loadFormulasConfig()
	{
		ExProperties formulasSettings = load(FORMULAS_CONFIGURATION_FILE);

		SKILLS_CHANCE_MOD = formulasSettings.getProperty("SkillsChanceMod", 11.);
		SKILLS_CHANCE_POW = formulasSettings.getProperty("SkillsChancePow", 0.5);
		SKILLS_CHANCE_MIN = formulasSettings.getProperty("SkillsChanceMin", 5.);
		SKILLS_CHANCE_CAP = formulasSettings.getProperty("SkillsChanceCap", 95.);
		SKILLS_MOB_CHANCE = formulasSettings.getProperty("SkillsMobChance", 0.5);
		SKILLS_DEBUFF_MOB_CHANCE = formulasSettings.getProperty("SkillsDebuffMobChance", 0.5);
		SKILLS_CAST_TIME_MIN = formulasSettings.getProperty("SkillsCastTimeMin", 333);

		ALT_ABSORB_DAMAGE_MODIFIER = formulasSettings.getProperty("AbsorbDamageModifier", 1.0);

		LIM_PATK = formulasSettings.getProperty("LimitPatk", 20000);
		LIM_MATK = formulasSettings.getProperty("LimitMAtk", 25000);
		LIM_PDEF = formulasSettings.getProperty("LimitPDef", 15000);
		LIM_MDEF = formulasSettings.getProperty("LimitMDef", 15000);
		LIM_PATK_SPD = formulasSettings.getProperty("LimitPatkSpd", 1500);
		LIM_MATK_SPD = formulasSettings.getProperty("LimitMatkSpd", 1999);
		LIM_CRIT_DAM = formulasSettings.getProperty("LimitCriticalDamage", 2000);
		LIM_CRIT = formulasSettings.getProperty("LimitCritical", 500);
		LIM_MCRIT = formulasSettings.getProperty("LimitMCritical", 20);
		LIM_ACCURACY = formulasSettings.getProperty("LimitAccuracy", 200);
		LIM_EVASION = formulasSettings.getProperty("LimitEvasion", 200);
		LIM_MOVE = formulasSettings.getProperty("LimitMove", 250);
		GM_LIM_MOVE = formulasSettings.getProperty("GmLimitMove", 1500);

		LIM_FAME = formulasSettings.getProperty("LimitFame", 50000);

		ALT_NPC_PATK_MODIFIER = formulasSettings.getProperty("NpcPAtkModifier", 1.0);
		ALT_NPC_MATK_MODIFIER = formulasSettings.getProperty("NpcMAtkModifier", 1.0);
		ALT_NPC_MAXHP_MODIFIER = formulasSettings.getProperty("NpcMaxHpModifier", 1.00);
		ALT_NPC_MAXMP_MODIFIER = formulasSettings.getProperty("NpcMapMpModifier", 1.00);

		ALT_POLE_DAMAGE_MODIFIER = formulasSettings.getProperty("PoleDamageModifier", 1.0);
	}

	public static void loadDevelopSettings()
	{
		ExProperties DevelopSettings = load(DEVELOP_FILE);

		ALT_DEBUG_ENABLED = DevelopSettings.getProperty("AltDebugEnabled", false);
		ALT_DEBUG_PVP_ENABLED = DevelopSettings.getProperty("AltDebugPvPEnabled", false);
		ALT_DEBUG_PVP_DUEL_ONLY = DevelopSettings.getProperty("AltDebugPvPDuelOnly", true);
		ALT_DEBUG_PVE_ENABLED = DevelopSettings.getProperty("AltDebugPvEEnabled", false);
		
		DONTLOADSPAWN = DevelopSettings.getProperty("StartWithoutSpawn", false);
		DONTLOADQUEST = DevelopSettings.getProperty("StartWithoutQuest", false);		
		LOAD_CUSTOM_SPAWN = DevelopSettings.getProperty("LoadAddGmSpawn", false);
		SAVE_GM_SPAWN = DevelopSettings.getProperty("SaveGmSpawn", false);
	}

	public static void loadExtSettings()
	{
		ExProperties properties = load(EXT_FILE);

		EX_NEW_PETITION_SYSTEM = properties.getProperty("NewPetitionSystem", false);
		EX_JAPAN_MINIGAME = properties.getProperty("JapanMinigame", false);
		EX_LECTURE_MARK = properties.getProperty("LectureMark", false);
		
		//Emulation OFF Core (packet SendStatus)
        Random ppc = new Random();
        int z = ppc.nextInt(6);
        if(z == 0)
        {
                z += 2;
        }
        for(int x = 0; x < 8; x++)
        {
                if(x == 4)
                {
                        RWHO_ARRAY[x] = 44;
                }
                else
                {
                        RWHO_ARRAY[x] = 51 + ppc.nextInt(z);
                }
        }
        RWHO_ARRAY[11] = 37265 + ppc.nextInt(z * 2 + 3);
        RWHO_ARRAY[8] = 51 + ppc.nextInt(z);
        z = 36224 + ppc.nextInt(z * 2);
        RWHO_ARRAY[9] = z;
        RWHO_ARRAY[10] = z;
        RWHO_ARRAY[12] = 1;
        RWHO_LOG = properties.getProperty("RemoteWhoLog", false);
        RWHO_SEND_TRASH = properties.getProperty("RemoteWhoSendTrash", false);
        RWHO_MAX_ONLINE = properties.getProperty("RemoteWhoMaxOnline", 0);
        RWHO_KEEP_STAT = properties.getProperty("RemoteOnlineKeepStat", 5);
        RWHO_ONLINE_INCREMENT = properties.getProperty("RemoteOnlineIncrement", 0);
        RWHO_PRIV_STORE_FACTOR = properties.getProperty("RemotePrivStoreFactor", 0);
        RWHO_FORCE_INC = properties.getProperty("RemoteWhoForceInc", 0);
	}

	public static void loadItemsSettings()
	{
		ExProperties itemsProperties = load(ITEMS_FILE);

		CAN_BE_TRADED_NO_TARADEABLE = itemsProperties.getProperty("CanBeTradedNoTradeable", false);
		CAN_BE_TRADED_NO_SELLABLE = itemsProperties.getProperty("CanBeTradedNoSellable", false);
		CAN_BE_TRADED_NO_STOREABLE = itemsProperties.getProperty("CanBeTradedNoStoreable", false);
		CAN_BE_TRADED_SHADOW_ITEM = itemsProperties.getProperty("CanBeTradedShadowItem", false);
		CAN_BE_TRADED_HERO_WEAPON = itemsProperties.getProperty("CanBeTradedHeroWeapon", false);
		CAN_BE_WH_NO_TARADEABLE = itemsProperties.getProperty("CanBeWhNoTradeable", false);
		CAN_BE_CWH_NO_TARADEABLE = itemsProperties.getProperty("CanBeCwhNoTradeable", false);
		CAN_BE_CWH_IS_AUGMENTED = itemsProperties.getProperty("CanBeCwhIsAugmented", false);
		CAN_BE_WH_IS_AUGMENTED = itemsProperties.getProperty("CanBeWhIsAugmented", false);
		ALLOW_SOUL_SPIRIT_SHOT_INFINITELY = itemsProperties.getProperty("AllowSoulSpiritShotInfinitely", false);
		ALLOW_ARROW_INFINITELY = itemsProperties.getProperty("AllowArrowInfinitely", false);
		ALLOW_START_ITEMS = itemsProperties.getProperty("AllowStartItems", false);
		START_ITEMS_MAGE = itemsProperties.getProperty("StartItemsMageIds", new int[] { 57 });
		START_ITEMS_MAGE_COUNT = itemsProperties.getProperty("StartItemsMageCount", new int[] { 1 });
		START_ITEMS_FITHER = itemsProperties.getProperty("StartItemsFigtherIds", new int[] { 57 });
		START_ITEMS_FITHER_COUNT = itemsProperties.getProperty("StartItemsFigtherCount", new int[] { 1 });
	}

	public static void loadTopSettings()
	{
		ExProperties topSetting = load(TOP_FILE);

		L2_TOP_MANAGER_ENABLED = topSetting.getProperty("L2TopManagerEnabled", false);
		L2_TOP_MANAGER_INTERVAL = topSetting.getProperty("L2TopManagerInterval", 300000);
		L2_TOP_WEB_ADDRESS = topSetting.getProperty("L2TopWebAddress", "");
		L2_TOP_SMS_ADDRESS = topSetting.getProperty("L2TopSmsAddress", "");
		L2_TOP_SERVER_ADDRESS = topSetting.getProperty("L2TopServerAddress", "first-team.ru");
		L2_TOP_SAVE_DAYS = topSetting.getProperty("L2TopSaveDays", 30);
		L2_TOP_REWARD = topSetting.getProperty("L2TopReward", new int[0]);

		MMO_TOP_MANAGER_ENABLED = topSetting.getProperty("MMOTopEnable", false);
		MMO_TOP_MANAGER_INTERVAL = topSetting.getProperty("MMOTopManagerInterval", 300000);
		MMO_TOP_WEB_ADDRESS = topSetting.getProperty("MMOTopUrl", "");
		MMO_TOP_SERVER_ADDRESS = topSetting.getProperty("MMOTopServerAddress", "first-team.ru");
		MMO_TOP_SAVE_DAYS = topSetting.getProperty("MMOTopSaveDays", 30);
		MMO_TOP_REWARD = topSetting.getProperty("MMOTopReward", new int[0]);
		
		
		ALLOW_HOPZONE_VOTE_REWARD = Boolean.parseBoolean(topSetting.getProperty("AllowHopzoneVoteReward", "false"));
		HOPZONE_SERVER_LINK = topSetting.getProperty("HopzoneServerLink", "http://l2.hopzone.net/lineage2/details/74078/L2World-Servers/");
		HOPZONE_FIRST_PAGE_LINK = topSetting.getProperty("HopzoneFirstPageLink", "http://l2.hopzone.net/lineage2/");
		HOPZONE_VOTES_DIFFERENCE = Integer.parseInt(topSetting.getProperty("HopzoneVotesDifference", "5"));
		HOPZONE_FIRST_PAGE_RANK_NEEDED = Integer.parseInt(topSetting.getProperty("HopzoneFirstPageRankNeeded", "15"));
		HOPZONE_REWARD_CHECK_TIME = Integer.parseInt(topSetting.getProperty("HopzoneRewardCheckTime", "5"));
		HOPZONE_DUALBOXES_ALLOWED = Integer.parseInt(topSetting.getProperty("HopzoneDualboxesAllowed", "1"));
		ALLOW_HOPZONE_GAME_SERVER_REPORT = Boolean.parseBoolean(topSetting.getProperty("AllowHopzoneGameServerReport", "false"));
		ALLOW_TOPZONE_VOTE_REWARD = Boolean.parseBoolean(topSetting.getProperty("AllowTopzoneVoteReward", "false"));
		TOPZONE_SERVER_LINK = topSetting.getProperty("TopzoneServerLink", "http://l2.topzone.net/lineage2/details/74078/L2World-Servers/");
		TOPZONE_FIRST_PAGE_LINK = topSetting.getProperty("TopzoneFirstPageLink", "http://l2.topzone.net/lineage2/");
		TOPZONE_VOTES_DIFFERENCE = Integer.parseInt(topSetting.getProperty("TopzoneVotesDifference", "5"));
		TOPZONE_FIRST_PAGE_RANK_NEEDED = Integer.parseInt(topSetting.getProperty("TopzoneFirstPageRankNeeded", "15"));
		TOPZONE_REWARD_CHECK_TIME = Integer.parseInt(topSetting.getProperty("TopzoneRewardCheckTime", "5"));
		TOPZONE_DUALBOXES_ALLOWED = Integer.parseInt(topSetting.getProperty("TopzoneDualboxesAllowed", "1"));
		ALLOW_TOPZONE_GAME_SERVER_REPORT = Boolean.parseBoolean(topSetting.getProperty("AllowTopzoneGameServerReport", "false"));
		HOPZONE_REWARD_ID = topSetting.getProperty("HopZoneRewardId", 6673);
		HOPZONE_REWARD_COUNT = topSetting.getProperty("HopZoneTopRewardCount", 1);
		TOPZONE_REWARD_ID = topSetting.getProperty("TopZoneRewardId", 6673);
		TOPZONE_REWARD_COUNT = topSetting.getProperty("TopZoneRewardCount", 1);
	}
	
	public static void loadPaymentSettings()
	{
		ExProperties paymentSettings = load(PAYMENT_FILE);

		SMS_PAYMENT_MANAGER_ENABLED = paymentSettings.getProperty("SMSPaymentEnabled", false);
		SMS_PAYMENT_WEB_ADDRESS = paymentSettings.getProperty("SMSPaymentWebAddress", "");
		SMS_PAYMENT_MANAGER_INTERVAL = paymentSettings.getProperty("SMSPaymentManagerInterval", 300000);
		SMS_PAYMENT_SAVE_DAYS = paymentSettings.getProperty("SMSPaymentSaveDays", 30);
		SMS_PAYMENT_SERVER_ADDRESS = paymentSettings.getProperty("SMSPaymentServerAddress", "revolt-team.com");
		SMS_PAYMENT_REWARD = paymentSettings.getProperty("SMSPaymentReward", new int[0]);
	}
	
	public static void loadAltSettings()
	{
		ExProperties altSettings = load(ALT_SETTINGS_FILE);

		ALT_ARENA_EXP = altSettings.getProperty("ArenaExp", true);
		ALT_GAME_DELEVEL = altSettings.getProperty("Delevel", true);
		ALT_SAVE_UNSAVEABLE = altSettings.getProperty("AltSaveUnsaveable", false);
		SHIELD_SLAM_BLOCK_IS_MUSIC = altSettings.getProperty("ShieldSlamBlockIsMusic", false);
		ALT_SAVE_EFFECTS_REMAINING_TIME = altSettings.getProperty("AltSaveEffectsRemainingTime", 5);
		ALT_SHOW_REUSE_MSG = altSettings.getProperty("AltShowSkillReuseMessage", true);
		ALT_DELETE_SA_BUFFS = altSettings.getProperty("AltDeleteSABuffs", false);
		AUTO_LOOT = altSettings.getProperty("AutoLoot", false);
		AUTO_LOOT_ONLY_ADENA = altSettings.getProperty("AutoLootOnlyAdena", false);
		AUTO_LOOT_HERBS = altSettings.getProperty("AutoLootHerbs", false);
		AUTO_LOOT_INDIVIDUAL = altSettings.getProperty("AutoLootIndividual", false);
		AUTO_LOOT_FROM_RAIDS = altSettings.getProperty("AutoLootFromRaids", false);
		AUTO_LOOT_PK = altSettings.getProperty("AutoLootPK", false);
		ALT_GAME_KARMA_PLAYER_CAN_SHOP = altSettings.getProperty("AltKarmaPlayerCanShop", false);
		SAVING_SPS = altSettings.getProperty("SavingSpS", false);
		MANAHEAL_SPS_BONUS = altSettings.getProperty("ManahealSpSBonus", false);
		CRAFT_MASTERWORK_CHANCE = altSettings.getProperty("CraftMasterworkChance", 3.);
		CRAFT_DOUBLECRAFT_CHANCE = altSettings.getProperty("CraftDoubleCraftChance", 3.);
		ALT_RAID_RESPAWN_MULTIPLIER = altSettings.getProperty("AltRaidRespawnMultiplier", 1.0);
		ALT_ALLOW_AUGMENT_ALL = altSettings.getProperty("AugmentAll", false);
		ALT_ALLOW_DROP_AUGMENTED = altSettings.getProperty("AlowDropAugmented", false);
		ALT_GAME_UNREGISTER_RECIPE = altSettings.getProperty("AltUnregisterRecipe", true);
		ALT_GAME_SHOW_DROPLIST = altSettings.getProperty("AltShowDroplist", true);
		ALLOW_NPC_SHIFTCLICK = altSettings.getProperty("AllowShiftClick", true);
		ALT_FULL_NPC_STATS_PAGE = altSettings.getProperty("AltFullStatsPage", false);
		ALT_GAME_SUBCLASS_WITHOUT_QUESTS = altSettings.getProperty("AltAllowSubClassWithoutQuest", false);
		ALT_ALLOW_SUBCLASS_WITHOUT_BAIUM = altSettings.getProperty("AltAllowSubClassWithoutBaium", true);
		ALT_GAME_LEVEL_TO_GET_SUBCLASS = altSettings.getProperty("AltLevelToGetSubclass", 75);
		ALT_GAME_START_LEVEL_TO_SUBCLASS = altSettings.getProperty("AltStartLevelToSubclass", 40);
		ALT_GAME_SUB_ADD = altSettings.getProperty("AltSubAdd", 0);
		ALT_GAME_SUB_BOOK = altSettings.getProperty("AltSubBook", false);
		ALT_MAX_LEVEL = Math.min(altSettings.getProperty("AltMaxLevel", 85), Experience.LEVEL.length - 1);
		ALT_MAX_SUB_LEVEL = Math.min(altSettings.getProperty("AltMaxSubLevel", 80), Experience.LEVEL.length - 1);
		ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE = altSettings.getProperty("AltAllowOthersWithdrawFromClanWarehouse", false);
		ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER = altSettings.getProperty("AltAllowClanCommandOnlyForClanLeader", true);
		
		ALT_GAME_REQUIRE_CLAN_CASTLE = altSettings.getProperty("AltRequireClanCastle", false);
		ALT_GAME_REQUIRE_CASTLE_DAWN = altSettings.getProperty("AltRequireCastleDawn", true);
		ALT_GAME_ALLOW_ADENA_DAWN = altSettings.getProperty("AltAllowAdenaDawn", true);
		ALT_ADD_RECIPES = altSettings.getProperty("AltAddRecipes", 0);
		SS_ANNOUNCE_PERIOD = altSettings.getProperty("SSAnnouncePeriod", 0);
		PETITIONING_ALLOWED = altSettings.getProperty("PetitioningAllowed", true);
		MAX_PETITIONS_PER_PLAYER = altSettings.getProperty("MaxPetitionsPerPlayer", 5);
		MAX_PETITIONS_PENDING = altSettings.getProperty("MaxPetitionsPending", 25);
		AUTO_LEARN_SKILLS = altSettings.getProperty("AutoLearnSkills", false);
		AUTO_LEARN_FORGOTTEN_SKILLS = altSettings.getProperty("AutoLearnForgottenSkills", false);
		ALT_SOCIAL_ACTION_REUSE = altSettings.getProperty("AltSocialActionReuse", false);
		ALT_DISABLE_SPELLBOOKS = altSettings.getProperty("AltDisableSpellbooks", false);
		ALT_SIMPLE_SIGNS = altSettings.getProperty("PushkinSignsOptions", false);
		ALT_TELE_TO_CATACOMBS = altSettings.getProperty("TeleToCatacombs", false);
		ALT_BS_CRYSTALLIZE = altSettings.getProperty("BSCrystallize", false);
		ALT_MAMMON_UPGRADE = altSettings.getProperty("MammonUpgrade", 6680500);
		ALT_MAMMON_EXCHANGE = altSettings.getProperty("MammonExchange", 10091400);
		ALT_ALLOW_TATTOO = altSettings.getProperty("AllowTattoo", false);
		ALT_BUFF_LIMIT = altSettings.getProperty("BuffLimit", 20);
		ALT_DEATH_PENALTY = altSettings.getProperty("EnableAltDeathPenalty", false);
		ALLOW_DEATH_PENALTY_C5 = altSettings.getProperty("EnableDeathPenaltyC5", true);
		ALT_DEATH_PENALTY_C5_CHANCE = altSettings.getProperty("DeathPenaltyC5Chance", 10);
		ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY = altSettings.getProperty("ChaoticCanUseScrollOfRecovery", false);
		ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY = altSettings.getProperty("DeathPenaltyC5RateExpPenalty", 1);
		ALT_DEATH_PENALTY_C5_KARMA_PENALTY = altSettings.getProperty("DeathPenaltyC5RateKarma", 1);
		ALT_PK_DEATH_RATE = altSettings.getProperty("AltPKDeathRate", 0.);
		NONOWNER_ITEM_PICKUP_DELAY = altSettings.getProperty("NonOwnerItemPickupDelay", 15L) * 1000L;
		ALT_NO_LASTHIT = altSettings.getProperty("NoLasthitOnRaid", false);
		ALT_KAMALOKA_NIGHTMARES_PREMIUM_ONLY = altSettings.getProperty("KamalokaNightmaresPremiumOnly", false);
		ALT_KAMALOKA_NIGHTMARE_REENTER = altSettings.getProperty("SellReenterNightmaresTicket", true);
		ALT_KAMALOKA_ABYSS_REENTER = altSettings.getProperty("SellReenterAbyssTicket", true);
		ALT_KAMALOKA_LAB_REENTER = altSettings.getProperty("SellReenterLabyrinthTicket", true);
		ALT_PET_HEAL_BATTLE_ONLY = altSettings.getProperty("PetsHealOnlyInBattle", true);
		CHAR_TITLE = altSettings.getProperty("CharTitle", false);
		ADD_CHAR_TITLE = altSettings.getProperty("CharAddTitle", "");

		ALT_ALLOW_SELL_COMMON = altSettings.getProperty("AllowSellCommon", true);
		ALT_ALLOW_SHADOW_WEAPONS = altSettings.getProperty("AllowShadowWeapons", true);
		ALT_DISABLED_MULTISELL = altSettings.getProperty("DisabledMultisells", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_PRICE_LIMITS = altSettings.getProperty("ShopPriceLimits", ArrayUtils.EMPTY_INT_ARRAY);
		ALT_SHOP_UNALLOWED_ITEMS = altSettings.getProperty("ShopUnallowedItems", ArrayUtils.EMPTY_INT_ARRAY);

		ALT_ALLOWED_PET_POTIONS = altSettings.getProperty("AllowedPetPotions", new int[] { 735, 1060, 1061, 1062, 1374, 1375, 1539, 1540, 6035, 6036 });

		FESTIVAL_MIN_PARTY_SIZE = altSettings.getProperty("FestivalMinPartySize", 5);
		FESTIVAL_RATE_PRICE = altSettings.getProperty("FestivalRatePrice", 1.0);

		RIFT_MIN_PARTY_SIZE = altSettings.getProperty("RiftMinPartySize", 5);
		RIFT_SPAWN_DELAY = altSettings.getProperty("RiftSpawnDelay", 10000);
		RIFT_MAX_JUMPS = altSettings.getProperty("MaxRiftJumps", 4);
		RIFT_AUTO_JUMPS_TIME = altSettings.getProperty("AutoJumpsDelay", 8);
		RIFT_AUTO_JUMPS_TIME_RAND = altSettings.getProperty("AutoJumpsDelayRandom", 120000);

		RIFT_ENTER_COST_RECRUIT = altSettings.getProperty("RecruitFC", 18);
		RIFT_ENTER_COST_SOLDIER = altSettings.getProperty("SoldierFC", 21);
		RIFT_ENTER_COST_OFFICER = altSettings.getProperty("OfficerFC", 24);
		RIFT_ENTER_COST_CAPTAIN = altSettings.getProperty("CaptainFC", 27);
		RIFT_ENTER_COST_COMMANDER = altSettings.getProperty("CommanderFC", 30);
		RIFT_ENTER_COST_HERO = altSettings.getProperty("HeroFC", 33);
		ALLOW_CLANSKILLS = altSettings.getProperty("AllowClanSkills", true);
		ALLOW_LEARN_TRANS_SKILLS_WO_QUEST = altSettings.getProperty("AllowLearnTransSkillsWOQuest", false);
		PARTY_LEADER_ONLY_CAN_INVITE = altSettings.getProperty("PartyLeaderOnlyCanInvite", true);
		ALLOW_TALK_WHILE_SITTING = altSettings.getProperty("AllowTalkWhileSitting", true);
		ALLOW_NOBLE_TP_TO_ALL = altSettings.getProperty("AllowNobleTPToAll", false);

		CLANHALL_BUFFTIME_MODIFIER = altSettings.getProperty("ClanHallBuffTimeModifier", 1.0);
		SONGDANCETIME_MODIFIER = altSettings.getProperty("SongDanceTimeModifier", 1.0);
		MAXLOAD_MODIFIER = altSettings.getProperty("MaxLoadModifier", 1.0);
		GATEKEEPER_MODIFIER = altSettings.getProperty("GkCostMultiplier", 1.0);
		GATEKEEPER_FREE = altSettings.getProperty("GkFree", 40);
		CRUMA_GATEKEEPER_LVL = altSettings.getProperty("GkCruma", 65);
		ALT_IMPROVED_PETS_LIMITED_USE = altSettings.getProperty("ImprovedPetsLimitedUse", false);

		ALT_CHAMPION_CHANCE1 = altSettings.getProperty("AltChampionChance1", 0.);
		ALT_CHAMPION_CHANCE2 = altSettings.getProperty("AltChampionChance2", 0.);
		ALT_CHAMPION_CAN_BE_AGGRO = altSettings.getProperty("AltChampionAggro", false);
		ALT_CHAMPION_CAN_BE_SOCIAL = altSettings.getProperty("AltChampionSocial", false);
		ALT_CHAMPION_DROP_HERBS = altSettings.getProperty("AltChampionDropHerbs", false);
		ALT_SHOW_MONSTERS_AGRESSION = altSettings.getProperty("AltShowMonstersAgression", false);
		ALT_SHOW_MONSTERS_LVL = altSettings.getProperty("AltShowMonstersLvL", false);
		ALT_CHAMPION_TOP_LEVEL = altSettings.getProperty("AltChampionTopLevel", 75);
		ALT_CHAMPION_MIN_LEVEL = altSettings.getProperty("AltChampionMinLevel", 20);

		ALT_VITALITY_ENABLED = altSettings.getProperty("AltVitalityEnabled", true);
		ALT_VITALITY_RATE = altSettings.getProperty("AltVitalityRate", 1.);
		ALT_VITALITY_CONSUME_RATE = altSettings.getProperty("AltVitalityConsumeRate", 1.);
		ALT_VITALITY_RAID_BONUS = altSettings.getProperty("AltVitalityRaidBonus", 2000);

		ALT_PCBANG_POINTS_ENABLED = altSettings.getProperty("AltPcBangPointsEnabled", false);
		ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE = altSettings.getProperty("AltPcBangPointsDoubleChance", 10.);
		ALT_PCBANG_POINTS_BONUS = altSettings.getProperty("AltPcBangPointsBonus", 0);
		ALT_PCBANG_POINTS_DELAY = altSettings.getProperty("AltPcBangPointsDelay", 20);
		ALT_PCBANG_POINTS_MIN_LVL = altSettings.getProperty("AltPcBangPointsMinLvl", 1);

		ALT_MAX_ALLY_SIZE = altSettings.getProperty("AltMaxAllySize", 3);
		ALT_PARTY_DISTRIBUTION_RANGE = altSettings.getProperty("AltPartyDistributionRange", 1500);
		ALT_PARTY_BONUS = altSettings.getProperty("AltPartyBonus", new double[] { 1.00, 1.10, 1.20, 1.30, 1.40, 1.50, 2.00, 2.10, 2.20 });

		ALT_ALL_PHYS_SKILLS_OVERHIT = altSettings.getProperty("AltAllPhysSkillsOverhit", true);
		ALT_REMOVE_SKILLS_ON_DELEVEL = altSettings.getProperty("AltRemoveSkillsOnDelevel", true);
		ALT_USE_BOW_REUSE_MODIFIER = altSettings.getProperty("AltUseBowReuseModifier", true);
		ALLOW_CH_DOOR_OPEN_ON_CLICK = altSettings.getProperty("AllowChDoorOpenOnClick", true);
		ALT_CH_ALL_BUFFS = altSettings.getProperty("AltChAllBuffs", false);
		ALT_CH_ALLOW_1H_BUFFS = altSettings.getProperty("AltChAllowHourBuff", false);
		ALT_CH_SIMPLE_DIALOG = altSettings.getProperty("AltChSimpleDialog", false);

		AUGMENTATION_NG_SKILL_CHANCE = altSettings.getProperty("AugmentationNGSkillChance", 15);
		AUGMENTATION_NG_GLOW_CHANCE = altSettings.getProperty("AugmentationNGGlowChance", 0);
		AUGMENTATION_MID_SKILL_CHANCE = altSettings.getProperty("AugmentationMidSkillChance", 30);
		AUGMENTATION_MID_GLOW_CHANCE = altSettings.getProperty("AugmentationMidGlowChance", 40);
		AUGMENTATION_HIGH_SKILL_CHANCE = altSettings.getProperty("AugmentationHighSkillChance", 45);
		AUGMENTATION_HIGH_GLOW_CHANCE = altSettings.getProperty("AugmentationHighGlowChance", 70);
		AUGMENTATION_TOP_SKILL_CHANCE = altSettings.getProperty("AugmentationTopSkillChance", 60);
		AUGMENTATION_TOP_GLOW_CHANCE = altSettings.getProperty("AugmentationTopGlowChance", 100);
		AUGMENTATION_BASESTAT_CHANCE = altSettings.getProperty("AugmentationBaseStatChance", 1);
		AUGMENTATION_ACC_SKILL_CHANCE = altSettings.getProperty("AugmentationAccSkillChance", 10);

		ALT_OPEN_CLOAK_SLOT = altSettings.getProperty("OpenCloakSlot", false);

		FOLLOW_RANGE = altSettings.getProperty("FollowRange", 100);

		ALT_ENABLE_MULTI_PROFA = altSettings.getProperty("AltEnableMultiProfa", false);
		
		ALT_ITEM_AUCTION_ENABLED = altSettings.getProperty("AltItemAuctionEnabled", true);
		ALT_ITEM_AUCTION_CAN_REBID = altSettings.getProperty("AltItemAuctionCanRebid", false);
		ALT_ITEM_AUCTION_START_ANNOUNCE = altSettings.getProperty("AltItemAuctionAnnounce", true);
		ALT_ITEM_AUCTION_BID_ITEM_ID = altSettings.getProperty("AltItemAuctionBidItemId", 57);
		ALT_ITEM_AUCTION_MAX_BID = altSettings.getProperty("AltItemAuctionMaxBid", 1000000L);
		ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS = altSettings.getProperty("AltItemAuctionMaxCancelTimeInMillis", 604800000);

		ALT_FISH_CHAMPIONSHIP_ENABLED = altSettings.getProperty("AltFishChampionshipEnabled", true);
		ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = altSettings.getProperty("AltFishChampionshipRewardItemId", 57);
		ALT_FISH_CHAMPIONSHIP_REWARD_1 = altSettings.getProperty("AltFishChampionshipReward1", 800000);
		ALT_FISH_CHAMPIONSHIP_REWARD_2 = altSettings.getProperty("AltFishChampionshipReward2", 500000);
		ALT_FISH_CHAMPIONSHIP_REWARD_3 = altSettings.getProperty("AltFishChampionshipReward3", 300000);
		ALT_FISH_CHAMPIONSHIP_REWARD_4 = altSettings.getProperty("AltFishChampionshipReward4", 200000);
		ALT_FISH_CHAMPIONSHIP_REWARD_5 = altSettings.getProperty("AltFishChampionshipReward5", 100000);

		ALT_ENABLE_BLOCK_CHECKER_EVENT = altSettings.getProperty("EnableBlockCheckerEvent", true);
		ALT_MIN_BLOCK_CHECKER_TEAM_MEMBERS = Math.min(Math.max(altSettings.getProperty("BlockCheckerMinTeamMembers", 1), 1), 6);
		ALT_RATE_COINS_REWARD_BLOCK_CHECKER = altSettings.getProperty("BlockCheckerRateCoinReward", 1.);

		ALT_HBCE_FAIR_PLAY = altSettings.getProperty("HBCEFairPlay", false);

		ALT_PET_INVENTORY_LIMIT = altSettings.getProperty("AltPetInventoryLimit", 12);
		ALT_CLAN_LEVEL_CREATE = altSettings.getProperty("ClanLevelCreate", 0);
		CLAN_LEVEL_6_COST = altSettings.getProperty("ClanLevel6Cost", 5000);
		CLAN_LEVEL_7_COST = altSettings.getProperty("ClanLevel7Cost", 10000);
		CLAN_LEVEL_8_COST = altSettings.getProperty("ClanLevel8Cost", 20000);
		CLAN_LEVEL_9_COST = altSettings.getProperty("ClanLevel9Cost", 40000);
		CLAN_LEVEL_10_COST = altSettings.getProperty("ClanLevel10Cost", 40000);
		CLAN_LEVEL_11_COST = altSettings.getProperty("ClanLevel11Cost", 75000);
		CLAN_LEVEL_6_REQUIREMEN = altSettings.getProperty("ClanLevel6Requirement", 30);
		CLAN_LEVEL_7_REQUIREMEN = altSettings.getProperty("ClanLevel7Requirement", 50);
		CLAN_LEVEL_8_REQUIREMEN = altSettings.getProperty("ClanLevel8Requirement", 80);
		CLAN_LEVEL_9_REQUIREMEN = altSettings.getProperty("ClanLevel9Requirement", 120);
		CLAN_LEVEL_10_REQUIREMEN = altSettings.getProperty("ClanLevel10Requirement", 140);
		CLAN_LEVEL_11_REQUIREMEN = altSettings.getProperty("ClanLevel11Requirement", 170);
		BLOOD_OATHS = altSettings.getProperty("BloodOaths", 150);
		BLOOD_PLEDGES = altSettings.getProperty("BloodPledges", 5);
		MIN_ACADEM_POINT = altSettings.getProperty("MinAcademPoint", 190);
		MAX_ACADEM_POINT = altSettings.getProperty("MaxAcademPoint", 650);
		
		HELLBOUND_LEVEL = altSettings.getProperty("HellboundLevel", 0);
                
		SIEGE_PVP_COUNT = altSettings.getProperty("SiegePvpCount", false);
		ZONE_PVP_COUNT = altSettings.getProperty("ZonePvpCount", false);
		EPIC_EXPERTISE_PENALTY = altSettings.getProperty("EpicExpertisePenalty", true);
		EXPERTISE_PENALTY = altSettings.getProperty("ExpertisePenalty", true);
		ALT_MUSIC_LIMIT = altSettings.getProperty("MusicLimit",12);
		ALT_DEBUFF_LIMIT = altSettings.getProperty("DebuffLimit",8);
		ALT_TRIGGER_LIMIT = altSettings.getProperty("TriggerLimit",12);
                ENABLE_MODIFY_SKILL_DURATION = altSettings.getProperty("EnableSkillDuration",false);
                if (ENABLE_MODIFY_SKILL_DURATION)
		{
			String[] propertySplit = altSettings.getProperty("SkillDurationList", "").split(";");
			SKILL_DURATION_LIST = new TIntIntHashMap(propertySplit.length);
			for (String skill : propertySplit)
			{
				String[] skillSplit = skill.split(",");
				if (skillSplit.length != 2)
					_log.warn("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
				else
				{
					try
					{
						SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!skill.isEmpty())
						{
							_log.warn("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
						}
					}
				}
			}
		}
	}
	public static void loadPvPmodConfig()
	{
		ExProperties PvPmodConfig = load(PVP_MOD_CONFIG_FILE);

		// PVP Server system
		CONSUMABLE_SHOT = PvPmodConfig.getProperty("ConsumableShot", true);
		CONSUMABLE_ARROW = PvPmodConfig.getProperty("ConsumableArrow", true);
		
		ATT_MOD_ARMOR = PvPmodConfig.getProperty("att_mod_Armor", 6);
		ATT_MOD_WEAPON = PvPmodConfig.getProperty("att_mod_Weapon", 5);
		ATT_MOD_WEAPON1 = PvPmodConfig.getProperty("att_mod_Weapon1", 20);
		
		ATT_MOD_MAX_ARMOR = PvPmodConfig.getProperty("att_mod_max_armor", 60);
		ATT_MOD_MAX_WEAPON = PvPmodConfig.getProperty("att_mod_max_weapon", 150);
		
		// By SmokiMo
		HENNA_STATS = PvPmodConfig.getProperty("HennaStats", 5);
		ENEBLE_TITLE_COLOR_MOD = PvPmodConfig.getProperty("EnebleTitleColorMod", false);
		TYPE_TITLE_COLOR_MOD = PvPmodConfig.getProperty("TypeTitleColorMod", "PvP");
		COUNT_TITLE_1 = PvPmodConfig.getProperty("CountTitle_1", 50);
		TITLE_COLOR_1 = Integer.decode("0x" + PvPmodConfig.getProperty("TitleColor_1", "FFFFFF"));
		COUNT_TITLE_2 = PvPmodConfig.getProperty("CountTitle_2", 100);
		TITLE_COLOR_2 = Integer.decode("0x" + PvPmodConfig.getProperty("TitleColor_2", "FFFFFF"));
		COUNT_TITLE_3 = PvPmodConfig.getProperty("CountTitle_3", 250);
		TITLE_COLOR_3 = Integer.decode("0x" + PvPmodConfig.getProperty("TitleColor_3", "FFFFFF"));
		COUNT_TITLE_4 = PvPmodConfig.getProperty("CountTitle_4", 500);
		TITLE_COLOR_4 = Integer.decode("0x" + PvPmodConfig.getProperty("TitleColor_4", "FFFFFF"));
		COUNT_TITLE_5 = PvPmodConfig.getProperty("CountTitle_5", 1000);
		TITLE_COLOR_5 = Integer.decode("0x" + PvPmodConfig.getProperty("TitleColor_5", "FFFFFF"));
		ENEBLE_NAME_COLOR_MOD = PvPmodConfig.getProperty("EnebleNameColorMod", false);
		TYPE_NAME_COLOR_MOD = PvPmodConfig.getProperty("TypeNameColorMod", "Pk");
		COUNT_NAME_1 = PvPmodConfig.getProperty("CountName_1", 50);
		NAME_COLOR_1 = Integer.decode("0x" + PvPmodConfig.getProperty("NameColor_1", "FFFFFF"));
		COUNT_NAME_2 = PvPmodConfig.getProperty("CountName_2", 100);
		NAME_COLOR_2 = Integer.decode("0x" + PvPmodConfig.getProperty("NameColor_2", "FFFFFF"));
		COUNT_NAME_3 = PvPmodConfig.getProperty("CountName_3", 250);
		NAME_COLOR_3 = Integer.decode("0x" + PvPmodConfig.getProperty("NameColor_3", "FFFFFF"));
		COUNT_NAME_4 = PvPmodConfig.getProperty("CountName_4", 500);
		NAME_COLOR_4 = Integer.decode("0x" + PvPmodConfig.getProperty("NameColor_4", "FFFFFF"));
		COUNT_NAME_5 = PvPmodConfig.getProperty("CountName_5", 1000);
		NAME_COLOR_5 = Integer.decode("0x" + PvPmodConfig.getProperty("NameColor_5", "FFFFFF"));
		
		
		//by 4ipolino
		NEW_CHAR_IS_NOBLE = Boolean.parseBoolean(PvPmodConfig.getProperty("NewCharIsNoble", "false"));
		NEW_CHAR_IS_HERO = Boolean.valueOf(PvPmodConfig.getProperty("NewCharIsHero", "false")); 
		ANNOUNCE_SPAWN_RB = Boolean.valueOf(PvPmodConfig.getProperty("AnnounceToSpawnRb", "false")); 
	
		SPAWN_CHAR = Boolean.parseBoolean(PvPmodConfig.getProperty("CustomSpawn", "false"));
		SPAWN_X = Integer.parseInt(PvPmodConfig.getProperty("SpawnX", ""));
		SPAWN_Y = Integer.parseInt(PvPmodConfig.getProperty("SpawnY", ""));
		SPAWN_Z = Integer.parseInt(PvPmodConfig.getProperty("SpawnZ", ""));
		
		ADEPT_ENABLE = PvPmodConfig.getProperty("ADEPT_ENABLE", true);
		
		SPAWN_CITIES_TREE = PvPmodConfig.getProperty("SPAWN_CITIES_TREE", true);
		SPAWN_NPC_BUFFER = PvPmodConfig.getProperty("SPAWN_NPC_BUFFER", true);
		SPAWN_scrubwoman = PvPmodConfig.getProperty("SPAWN_scrubwoman", true);
		MAX_PARTY_SIZE = PvPmodConfig.getProperty("MaxPartySize", 9);
		
	}
	public static int _coinID;
	public static boolean ALLOW_UPDATE_ANNOUNCER;
	public static void loadServicesSettings()
	{
		ExProperties servicesSettings = load(SERVICES_FILE);

        _coinID = servicesSettings.getProperty("Id_Item_Mall", 57);
		SERVICES_DELEVEL_ENABLED = servicesSettings.getProperty("AllowDelevel", false);
		SERVICES_DELEVEL_ITEM = servicesSettings.getProperty("DelevelItem", 57);
		SERVICES_DELEVEL_COUNT = servicesSettings.getProperty("DelevelCount", 1000);
		SERVICES_DELEVEL_MIN_LEVEL = servicesSettings.getProperty("DelevelMinLevel", 1);
		
		for(int id : servicesSettings.getProperty("AllowClassMasters", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				ALLOW_CLASS_MASTERS_LIST.add(id);

		CLASS_MASTERS_PRICE = servicesSettings.getProperty("ClassMastersPrice", "0,0,0");
		if(CLASS_MASTERS_PRICE.length() >= 5)
		{
			int level = 1;
			for(String id : CLASS_MASTERS_PRICE.split(","))
			{
				CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id);
				level++;
			}
		}
		SERVICES_RIDE_HIRE_ENABLED = servicesSettings.getProperty("RideHireEnabled", false);
		CLASS_MASTERS_PRICE_ITEM = servicesSettings.getProperty("ClassMastersPriceItem", 57);
		
		SERVICES_CHANGE_NICK_ALLOW_SYMBOL = servicesSettings.getProperty("NickChangeAllowSimbol", false);
		SERVICES_CHANGE_NICK_ENABLED = servicesSettings.getProperty("NickChangeEnabled", false);
		SERVICES_CHANGE_NICK_PRICE = servicesSettings.getProperty("NickChangePrice", 100);
		SERVICES_CHANGE_NICK_ITEM = servicesSettings.getProperty("NickChangeItem", 4037);

		SERVICES_CHANGE_CLAN_NAME_ENABLED = servicesSettings.getProperty("ClanNameChangeEnabled", false);
		SERVICES_CHANGE_CLAN_NAME_PRICE = servicesSettings.getProperty("ClanNameChangePrice", 100);
		SERVICES_CHANGE_CLAN_NAME_ITEM = servicesSettings.getProperty("ClanNameChangeItem", 4037);

		SERVICES_CHANGE_PET_NAME_ENABLED = servicesSettings.getProperty("PetNameChangeEnabled", false);
		SERVICES_CHANGE_PET_NAME_PRICE = servicesSettings.getProperty("PetNameChangePrice", 100);
		SERVICES_CHANGE_PET_NAME_ITEM = servicesSettings.getProperty("PetNameChangeItem", 4037);

		SERVICES_EXCHANGE_BABY_PET_ENABLED = servicesSettings.getProperty("BabyPetExchangeEnabled", false);
		SERVICES_EXCHANGE_BABY_PET_PRICE = servicesSettings.getProperty("BabyPetExchangePrice", 100);
		SERVICES_EXCHANGE_BABY_PET_ITEM = servicesSettings.getProperty("BabyPetExchangeItem", 4037);

		SERVICES_CHANGE_SEX_ENABLED = servicesSettings.getProperty("SexChangeEnabled", false);
		SERVICES_CHANGE_SEX_PRICE = servicesSettings.getProperty("SexChangePrice", 100);
		SERVICES_CHANGE_SEX_ITEM = servicesSettings.getProperty("SexChangeItem", 4037);

		SERVICES_CHANGE_BASE_ENABLED = servicesSettings.getProperty("BaseChangeEnabled", false);
		SERVICES_CHANGE_BASE_PRICE = servicesSettings.getProperty("BaseChangePrice", 100);
		SERVICES_CHANGE_BASE_ITEM = servicesSettings.getProperty("BaseChangeItem", 4037);

		SERVICES_SEPARATE_SUB_ENABLED = servicesSettings.getProperty("SeparateSubEnabled", false);
		SERVICES_SEPARATE_SUB_PRICE = servicesSettings.getProperty("SeparateSubPrice", 100);
		SERVICES_SEPARATE_SUB_ITEM = servicesSettings.getProperty("SeparateSubItem", 4037);

		SERVICES_CHANGE_NICK_COLOR_ENABLED = servicesSettings.getProperty("NickColorChangeEnabled", false);
		SERVICES_CHANGE_NICK_COLOR_PRICE = servicesSettings.getProperty("NickColorChangePrice", 100);
		SERVICES_CHANGE_NICK_COLOR_ITEM = servicesSettings.getProperty("NickColorChangeItem", 4037);
		SERVICES_CHANGE_NICK_COLOR_LIST = servicesSettings.getProperty("NickColorChangeList", new String[] { "00FF00" });

		SERVICES_CHANGE_Title_COLOR_ENABLED = servicesSettings.getProperty("TitleColorChangeEnabled", false);
		SERVICES_CHANGE_Title_COLOR_PRICE = servicesSettings.getProperty("TitleColorChangePrice", 100);
		SERVICES_CHANGE_Title_COLOR_ITEM = servicesSettings.getProperty("TitleColorChangeItem", 4037);
		SERVICES_CHANGE_Title_COLOR_LIST = servicesSettings.getProperty("TitleColorChangeList", new String[] { "00FF00" });
		
		SERVICES_BASH_ENABLED = servicesSettings.getProperty("BashEnabled", false);
		SERVICES_BASH_SKIP_DOWNLOAD = servicesSettings.getProperty("BashSkipDownload", false);
		SERVICES_BASH_RELOAD_TIME = servicesSettings.getProperty("BashReloadTime", 24);

		SERVICES_NOBLESS_SELL_ENABLED = servicesSettings.getProperty("NoblessSellEnabled", false);
		SERVICES_NOBLESS_SELL_PRICE = servicesSettings.getProperty("NoblessSellPrice", 1000);
		SERVICES_NOBLESS_SELL_ITEM = servicesSettings.getProperty("NoblessSellItem", 4037);
		
		SERVICES_HERO_SELL_ENABLED = servicesSettings.getProperty("HeroSellEnabled", false);
		SERVICES_HERO_SELL_DAY = servicesSettings.getProperty("HeroSellDay", new int[] { 30 });
	    SERVICES_HERO_SELL_PRICE = servicesSettings.getProperty("HeroSellPrice", new int[] { 30 });
	    SERVICES_HERO_SELL_ITEM = servicesSettings.getProperty("HeroSellItem", new int[] { 4037 });

	    SERVICES_WASH_PK_ENABLED = servicesSettings.getProperty("WashPkEnabled", false);
	    SERVICES_WASH_PK_ITEM = servicesSettings.getProperty("WashPkItem", 4037);
	    SERVICES_WASH_PK_PRICE = servicesSettings.getProperty("WashPkPrice", 5);
	    
		SERVICES_EXPAND_INVENTORY_ENABLED = servicesSettings.getProperty("ExpandInventoryEnabled", false);
		SERVICES_EXPAND_INVENTORY_PRICE = servicesSettings.getProperty("ExpandInventoryPrice", 1000);
		SERVICES_EXPAND_INVENTORY_ITEM = servicesSettings.getProperty("ExpandInventoryItem", 4037);
		SERVICES_EXPAND_INVENTORY_MAX = servicesSettings.getProperty("ExpandInventoryMax", 250);

		SERVICES_EXPAND_WAREHOUSE_ENABLED = servicesSettings.getProperty("ExpandWarehouseEnabled", false);
		SERVICES_EXPAND_WAREHOUSE_PRICE = servicesSettings.getProperty("ExpandWarehousePrice", 1000);
		SERVICES_EXPAND_WAREHOUSE_ITEM = servicesSettings.getProperty("ExpandWarehouseItem", 4037);

		SERVICES_EXPAND_CWH_ENABLED = servicesSettings.getProperty("ExpandCWHEnabled", false);
		SERVICES_EXPAND_CWH_PRICE = servicesSettings.getProperty("ExpandCWHPrice", 1000);
		SERVICES_EXPAND_CWH_ITEM = servicesSettings.getProperty("ExpandCWHItem", 4037);

		SERVICES_SELLPETS = servicesSettings.getProperty("SellPets", "");

		SERVICES_OFFLINE_TRADE_ALLOW = servicesSettings.getProperty("AllowOfflineTrade", false);
		SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE = servicesSettings.getProperty("AllowOfflineTradeOnlyOffshore", true);
		SERVICES_OFFLINE_TRADE_MIN_LEVEL = servicesSettings.getProperty("OfflineMinLevel", 0);
		SERVICES_OFFLINE_TRADE_NAME_COLOR = Integer.decode("0x" + servicesSettings.getProperty("OfflineTradeNameColor", "B0FFFF"));
		SERVICES_OFFLINE_TRADE_PRICE_ITEM = servicesSettings.getProperty("OfflineTradePriceItem", 0);
		SERVICES_OFFLINE_TRADE_PRICE = servicesSettings.getProperty("OfflineTradePrice", 0);
		SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK = servicesSettings.getProperty("OfflineTradeDaysToKick", 14) * 86400L;
		SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART = servicesSettings.getProperty("OfflineRestoreAfterRestart", true);

		SERVICES_NO_TRADE_ONLY_OFFLINE = servicesSettings.getProperty("NoTradeOnlyOffline", false);
		SERVICES_NO_TRADE_BLOCK_ZONE = servicesSettings.getProperty("NoTradeBlockZone", false);
		SERVICES_TRADE_TAX = servicesSettings.getProperty("TradeTax", 0.0);
		SERVICES_OFFSHORE_TRADE_TAX = servicesSettings.getProperty("OffshoreTradeTax", 0.0);
		SERVICES_TRADE_TAX_ONLY_OFFLINE = servicesSettings.getProperty("TradeTaxOnlyOffline", false);
		SERVICES_OFFSHORE_NO_CASTLE_TAX = servicesSettings.getProperty("NoCastleTaxInOffshore", false);
		SERVICES_TRADE_ONLY_FAR = servicesSettings.getProperty("TradeOnlyFar", false);
		SERVICES_TRADE_MIN_LEVEL = servicesSettings.getProperty("MinLevelForTrade", 0);
		SERVICES_TRADE_RADIUS = servicesSettings.getProperty("TradeRadius", 30);

		SERVICES_GIRAN_HARBOR_ENABLED = servicesSettings.getProperty("GiranHarborZone", false);
		SERVICES_PARNASSUS_ENABLED = servicesSettings.getProperty("ParnassusZone", false);
		SERVICES_PARNASSUS_NOTAX = servicesSettings.getProperty("ParnassusNoTax", false);
		SERVICES_PARNASSUS_PRICE = servicesSettings.getProperty("ParnassusPrice", 500000);

		SERVICES_ALLOW_LOTTERY = servicesSettings.getProperty("AllowLottery", false);
		SERVICES_LOTTERY_PRIZE = servicesSettings.getProperty("LotteryPrize", 50000);
		SERVICES_ALT_LOTTERY_PRICE = servicesSettings.getProperty("AltLotteryPrice", 2000);
		SERVICES_LOTTERY_TICKET_PRICE = servicesSettings.getProperty("LotteryTicketPrice", 2000);
		SERVICES_LOTTERY_5_NUMBER_RATE = servicesSettings.getProperty("Lottery5NumberRate", 0.6);
		SERVICES_LOTTERY_4_NUMBER_RATE = servicesSettings.getProperty("Lottery4NumberRate", 0.4);
		SERVICES_LOTTERY_3_NUMBER_RATE = servicesSettings.getProperty("Lottery3NumberRate", 0.2);
		SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE = servicesSettings.getProperty("Lottery2and1NumberPrize", 200);

		SERVICES_ALLOW_ROULETTE = servicesSettings.getProperty("AllowRoulette", false);
		SERVICES_ROULETTE_MIN_BET = servicesSettings.getProperty("RouletteMinBet", 1L);
		SERVICES_ROULETTE_MAX_BET = servicesSettings.getProperty("RouletteMaxBet", Long.MAX_VALUE);

		SERVICES_ENABLE_NO_CARRIER = servicesSettings.getProperty("EnableNoCarrier", false);
		SERVICES_NO_CARRIER_MIN_TIME = servicesSettings.getProperty("NoCarrierMinTime", 0);
		SERVICES_NO_CARRIER_MAX_TIME = servicesSettings.getProperty("NoCarrierMaxTime", 90);
		SERVICES_NO_CARRIER_DEFAULT_TIME = servicesSettings.getProperty("NoCarrierDefaultTime", 60);
		
		SERVICES_PK_PVP_KILL_ENABLE = servicesSettings.getProperty("PkPvPKillEnable", false);
		SERVICES_PVP_KILL_REWARD_ITEM = servicesSettings.getProperty("PvPkillRewardItem", 4037);
		SERVICES_PVP_KILL_REWARD_COUNT = servicesSettings.getProperty("PvPKillRewardCount", 1L);
		SERVICES_PK_KILL_REWARD_ITEM = servicesSettings.getProperty("PkkillRewardItem", 4037);
		SERVICES_PK_KILL_REWARD_COUNT = servicesSettings.getProperty("PkKillRewardCount", 1L);
		SERVICES_PK_PVP_TIE_IF_SAME_IP = servicesSettings.getProperty("PkPvPTieifSameIP", true);
		
		ITEM_BROKER_ITEM_SEARCH = servicesSettings.getProperty("UseItemBrokerItemSearch", false);

		SERVICES_CHANGE_PASSWORD = servicesSettings.getProperty("ChangePassword", false);
		
		ALLOW_EVENT_GATEKEEPER = servicesSettings.getProperty("AllowEventGatekeeper", false);
		SERVICES_LVL_ENABLED = servicesSettings.getProperty("LevelChangeEnabled", false);
		SERVICES_LVL_UP_MAX = servicesSettings.getProperty("LevelUPChangeMax", 85);
		SERVICES_LVL_UP_PRICE = servicesSettings.getProperty("LevelUPChangePrice", 1000);
		SERVICES_LVL_UP_ITEM = servicesSettings.getProperty("LevelUPChangeItem", 4037);
		SERVICES_LVL_DOWN_MAX = servicesSettings.getProperty("LevelDownChangeMax", 1);
		SERVICES_LVL_DOWN_PRICE = servicesSettings.getProperty("LevelDownChangePrice", 1000);
		SERVICES_LVL_DOWN_ITEM = servicesSettings.getProperty("LevelDownChangeItem", 4037);
		
		ALLOW_MULTILANG_GATEKEEPER = servicesSettings.getProperty("AllowMultiLangGatekeeper", false);
		DEFAULT_GK_LANG = servicesSettings.getProperty("DefaultGKLang", "en");
		ALLOW_UPDATE_ANNOUNCER = servicesSettings.getProperty("AllowUpdateAnnouncer", false);
    }
	
	public static boolean NOT_USE_USER_VOICED;
	public static boolean ALLOW_TOTAL_ONLINE;

	public static boolean show_rates;
	
	public static void loadCommandssettings()
	{
		ExProperties CommandsSettings = load(COMMANDS_CONFIG_FILE);
		
		show_rates = CommandsSettings.getProperty("show_rates", false);
		NOT_USE_USER_VOICED = CommandsSettings.getProperty("NotUsePlayerVoiced", false);
		
		ENABLE_KM_ALL_TO_ME = CommandsSettings.getProperty("EnableKmAllToMe", false);
		
		COMMAND_RES = Boolean.valueOf(CommandsSettings.getProperty("Command_ress", "false")); 
		ITEM_ID_RESS = CommandsSettings.getProperty("Item_Id_ress", 57);
		PRICE_RESS = CommandsSettings.getProperty("price_ress", 57);
	
		SERVICES_LOCK_ACCOUNT_IP = CommandsSettings.getProperty("LockAccountIpService", false);
		ALLOW_TOTAL_ONLINE = CommandsSettings.getProperty("AllowVoiceCommandOnline", false);
		
	    COMMAND_FARM = CommandsSettings.getProperty("COMMAND_FARM", false);
	    FARM_TELEPORT_ITEM_ID = CommandsSettings.getProperty("FARM_TELEPORT_ITEM_ID", 57);
	    PRICE_FARM = CommandsSettings.getProperty("PRICE_FARM", 57);
	    FARM_X = CommandsSettings.getProperty("FARM_X", 57);
	    FARM_Y = CommandsSettings.getProperty("FARM_Y", 57);
	    FARM_Z = CommandsSettings.getProperty("FARM_Z", 57);

	    COMMAND_FARM_HARD = CommandsSettings.getProperty("COMMAND_FARM_HARD", false);
	    FARM_HARD_TELEPORT_ITEM_ID = CommandsSettings.getProperty("FARM_HARD_TELEPORT_ITEM_ID", 57);
	    PRICE_FARM_HARD = CommandsSettings.getProperty("PRICE_FARM_HARD", 57);
	    FARM_HARD_X = CommandsSettings.getProperty("FARM_HARD_X", 57);
	    FARM_HARD_Y = CommandsSettings.getProperty("FARM_HARD_Y", 57);
	    FARM_HARD_Z = CommandsSettings.getProperty("FARM_HARD_Z", 57);
	    
	    COMMAND_FARM_LOW = CommandsSettings.getProperty("COMMAND_FARM_LOW", false);
	    FARM_LOW_TELEPORT_ITEM_ID = CommandsSettings.getProperty("FARM_LOW_TELEPORT_ITEM_ID", 57);
	    PRICE_FARM_LOW = CommandsSettings.getProperty("PRICE_FARM_LOW", 57);
	    FARM_LOW_X = CommandsSettings.getProperty("FARM_LOW_X", 57);
	    FARM_LOW_Y = CommandsSettings.getProperty("FARM_LOW_Y", 57);
	    FARM_LOW_Z = CommandsSettings.getProperty("FARM_LOW_Z", 57);
	   
	    COMMAND_PVP = CommandsSettings.getProperty("COMMAND_PVP", false);
	    PVP_X = Integer.parseInt(CommandsSettings.getProperty("PVP_X", "0"));
	    PVP_Y = Integer.parseInt(CommandsSettings.getProperty("PVP_Y", "0"));
	    PVP_Z = Integer.parseInt(CommandsSettings.getProperty("PVP_Z", "0"));
	    PVP_TELEPORT_ITEM_ID = CommandsSettings.getProperty("PVP_TELEPORT_ITEM_ID", 57);
	    PRICE_PVP = CommandsSettings.getProperty("PRICE_PVP", 57);
	    
	    COMMAND_GoToLeader = CommandsSettings.getProperty("GoToLeader", false);
	    PRICE_TELEPORT_CL = CommandsSettings.getProperty("Price_Teleport", 57);
	    GO_TO_CL_ITEM_ID = CommandsSettings.getProperty("Item_Id_go_to_cl", 57);
	    
	    NOBLE = CommandsSettings.getProperty("NOBLE", false);
	    ITEM_NOBLE = CommandsSettings.getProperty("Item_Noble", 57);
	    PRICE_NOBLE = CommandsSettings.getProperty("Price_noble", 57);
	    
		ALT_SHOW_SERVER_TIME = CommandsSettings.getProperty("ShowServerTime", false);
	}
	
	public static void loadCommunityPvPboardsettings()
	{
		ExProperties CommunityPvPboardSettings = load(BOARD_MANAGER_CONFIG_FILE);
		
		BBS_PVP_CB_ABNORMAL = CommunityPvPboardSettings.getProperty("BBSPVPAllowAbnormal", false);
		BBS_PVP_CB_ENABLED = CommunityPvPboardSettings.getProperty("BBSPVPEnabled", false);
		COMMUNITYBOARD_ENABLED = CommunityPvPboardSettings.getProperty("AllowCommunityBoard", true);
		BBS_DEFAULT = CommunityPvPboardSettings.getProperty("BBSDefault", "_bbshome");
		BBS_HOME_DIR = CommunityPvPboardSettings.getProperty("BBSHomeDir", "scripts/services/community/");
		ALLOW_BBS_WAREHOUSE = CommunityPvPboardSettings.getProperty("AllowBBSWarehouse", true);
		BBS_WAREHOUSE_ALLOW_PK = CommunityPvPboardSettings.getProperty("BBSWarehouseAllowPK", false);

	}

	public static void loadCommunityPvPbuffersettings()
	{
		ExProperties CommunityPvPbufferSettings = load(BUFFER_MANAGER_CONFIG_FILE);
		
		BBS_PVP_BUFFER_ENABLED = CommunityPvPbufferSettings.getProperty("BBSPVPBufferEnabled", false);
		BBS_PVP_BUFFER_ALT_TIME = CommunityPvPbufferSettings.getProperty("BBSPVPBufferTime", 14400) * 1000;
		BBS_PVP_BUFFER_PRICE_ITEM = CommunityPvPbufferSettings.getProperty("BBSPVPBufferPriceItem", 57);
		BBS_PVP_BUFFER_PRICE_ONE = CommunityPvPbufferSettings.getProperty("BBSPVPBufferPriceOne", 1000);
		BBS_PVP_BUFFER_BUFFS_PER_PAGE = CommunityPvPbufferSettings.getProperty("BBSPVPBufferMaxPerPage", 27);
		BBS_PVP_BUFFER_BUFFS_PER_SET = CommunityPvPbufferSettings.getProperty("BBSPVPBufferMaxPerSet", 27);
		BBS_PVP_BUFFER_TASK_DELAY = CommunityPvPbufferSettings.getProperty("BBSPVPBufferTaskDelay", 14400) * 1000;
		BBS_PVP_BUFFER_MIN_LVL = CommunityPvPbufferSettings.getProperty("BBSPVPBufferMinLvl", 1);
		BBS_PVP_BUFFER_MAX_LVL = CommunityPvPbufferSettings.getProperty("BBSPVPBufferMaxLvl", 99);
		BBS_PVP_BUFER_ONE_BUFF_PET = CommunityPvPbufferSettings.getProperty("BBSPVPBufferOneBuffPet", false);
		BBS_PVP_BUFFER_ALLOW_SIEGE = CommunityPvPbufferSettings.getProperty("BBSPVPBufferAllowOnSiege", true);		
		BBS_PVP_BUFFER_ALLOW_PVP_FLAG = CommunityPvPbufferSettings.getProperty("BBSPVPBufferAllowOnPvP", true);
		BBS_PVP_BUFFER_ALOWED_INST_BUFF = CommunityPvPbufferSettings.getProperty("BBSPVPBufferAllowInInstance", true);
		//TODO
		BBS_BUFFER_ENABLED = CommunityPvPbufferSettings.getProperty("AllowBBSBuffer", false);
		BBS_BUFF_DEATH = CommunityPvPbufferSettings.getProperty("AllowWhenDead", false);
		BBS_BUFF_ACTION = CommunityPvPbufferSettings.getProperty("AllowWhenInAction", false);
		BBS_BUFF_OLY = CommunityPvPbufferSettings.getProperty("AllowWhenInOlly", false);
		BBS_BUFF_FLY = CommunityPvPbufferSettings.getProperty("AllowWhenInFly", false);
		BBS_BUFF_VEICHLE = CommunityPvPbufferSettings.getProperty("AllowWhenInVeichle", false);
		BBS_BUFF_MOUNTED = CommunityPvPbufferSettings.getProperty("AllowWhenMounted", false);
		BBS_BUFF_CANT_MOVE = CommunityPvPbufferSettings.getProperty("AllowWhenCantMove", false);
		BBS_BUFF_STORE_MODE = CommunityPvPbufferSettings.getProperty("AllowWhenInTrade", false);
		BBS_BUFF_FISHING = CommunityPvPbufferSettings.getProperty("AllowWhenFishing", false);
		BBS_BUFF_TEMP_ACTION = CommunityPvPbufferSettings.getProperty("AllowWhenInTemp", false);
		BBS_BUFF_DUEL = CommunityPvPbufferSettings.getProperty("AllowWhenInDuel", false);
		BBS_BUFF_CURSED = CommunityPvPbufferSettings.getProperty("AllowWhenUseCursed", false);
		BBS_BUFF_PK = CommunityPvPbufferSettings.getProperty("AllowWhenIsPk", false);
		BBS_BUFF_LEADER = CommunityPvPbufferSettings.getProperty("AllowOnlyToClanLeader", false);
		BBS_BUFF_NOBLE = CommunityPvPbufferSettings.getProperty("AllowOnlyToNoble", false);
		BBS_BUFF_TERITORY = CommunityPvPbufferSettings.getProperty("AllowUseInTWPlayer", false);
		BBS_BUFF_PEACEZONE_ONLY = CommunityPvPbufferSettings.getProperty("AllowUseOnlyInPeace", false);
		BBS_BUFF_IDs = CommunityPvPbufferSettings.getProperty("BuffIDs", ArrayUtils.EMPTY_INT_ARRAY);
		BBS_BUFF_ALLOW_CANCEL = CommunityPvPbufferSettings.getProperty("BuffAllowCancel", false);
		BBS_BUFF_ALLOW_HEAL = CommunityPvPbufferSettings.getProperty("BuffAllowHeal", false);
		BUFF_MANUAL_EDIT_SETS = CommunityPvPbufferSettings.getProperty("BuffManualEditSets", false);
		MAX_SETS_PER_CHAR = CommunityPvPbufferSettings.getProperty("MaximumSetsPerChar", 8);
		BBS_BUFF_ITEM_COUNT = CommunityPvPbufferSettings.getProperty("BuffItemCount", 8);
		BBS_BUFF_FREE_LVL = CommunityPvPbufferSettings.getProperty("FreeBuffLevel", 8);
		MAX_BUFF_PER_SET = CommunityPvPbufferSettings.getProperty("MaxBuffsPerSet", 8);
		BUFF_PAGE_ROWS = CommunityPvPbufferSettings.getProperty("BuffsPageRows", 8);
		BBS_BUFF_ITEM_ID = CommunityPvPbufferSettings.getProperty("BuffItemId", 8);
		BBS_BUFF_TIME_MUSIC = CommunityPvPbufferSettings.getProperty("BuffTimeMusic", 8);
		BBS_BUFF_TIME_SPECIAL = CommunityPvPbufferSettings.getProperty("BuffTimeSpecial", 8);
		BBS_BUFF_TIME = CommunityPvPbufferSettings.getProperty("BuffTime", 8);
		BBS_BUFF_TIME_MOD = CommunityPvPbufferSettings.getProperty("BuffTimeMod", 8);
		BBS_BUFF_TIME_MOD_MUSIC = CommunityPvPbufferSettings.getProperty("BuffTimeModMusic", 8);
		BBS_BUFF_TIME_MOD_SPECIAL = CommunityPvPbufferSettings.getProperty("BuffTimeModSpecial", 8);
		
	}

	public static void loadCommunityPvPclasssettings()
	{
		ExProperties CommunityPvPClassSettings = load(CLASS_MASTER_CONFIG_FILE);
		
				for(int id : CommunityPvPClassSettings.getProperty("AllowClassMasters", ArrayUtils.EMPTY_INT_ARRAY))
			if(id != 0)
				ALLOW_CLASS_MASTERS_LIST.add(id);

		CLASS_MASTERS_PRICE = CommunityPvPClassSettings.getProperty("ClassMastersPrice", "0,0,0");
		if(CLASS_MASTERS_PRICE.length() >= 5)
		{
			int level = 1;
			for(String id : CLASS_MASTERS_PRICE.split(","))
			{
				CLASS_MASTERS_PRICE_LIST[level] = Integer.parseInt(id);
				level++;
			}
		}
		CLASS_MASTERS_PRICE_ITEM = CommunityPvPClassSettings.getProperty("ClassMastersPriceItem", 57);
		
		
		BBS_PVP_SUB_MANAGER_ALLOW = CommunityPvPClassSettings.getProperty("AllowBBSSubManager", false);
		BBS_PVP_SUB_MANAGER_PIACE = CommunityPvPClassSettings.getProperty("AllowBBSSubManagerPiace", false);
	}

	public static void loadCommunityPvPshopsettings()
	{
		ExProperties CommunityPvPshopSettings = load(SHOP_MANAGER_CONFIG_FILE);
		
		BBS_PVP_ALLOW_BUY = CommunityPvPshopSettings.getProperty("CommunityShopEnable", false);
		BBS_PVP_ALLOW_SELL = CommunityPvPshopSettings.getProperty("CommunitySellEnable", false);
		BBS_PVP_ALLOW_AUGMENT = CommunityPvPshopSettings.getProperty("CommunityAugmentEnable", false);
	}

	public static void loadCommunityPvPteleportsettings()
	{
		ExProperties CommunityPvPteleportsettings = load(TELEPORT_MANAGER_CONFIG_FILE);
		
		BBS_PVP_TELEPORT_ENABLED = CommunityPvPteleportsettings.getProperty("BBSPVPTeleportEnabled", false);
		BBS_PVP_TELEPORT_POINT_PRICE = CommunityPvPteleportsettings.getProperty("BBSPVPTeleportPointPrice", 200000);
		BBS_PVP_TELEPORT_MAX_POINT_COUNT  = CommunityPvPteleportsettings.getProperty("BBSPVPTeleportMaxPointCount", 10);
		
	}
	
	public static void loadPvPSettings()
	{
		ExProperties pvpSettings = load(PVP_CONFIG_FILE);

		/* KARMA SYSTEM */
		KARMA_MIN_KARMA = pvpSettings.getProperty("MinKarma", 240);
		KARMA_SP_DIVIDER = pvpSettings.getProperty("SPDivider", 7);
		KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 0);

		KARMA_DROP_GM = pvpSettings.getProperty("CanGMDropEquipment", false);
		KARMA_NEEDED_TO_DROP = pvpSettings.getProperty("KarmaNeededToDrop", true);
		DROP_ITEMS_ON_DIE = pvpSettings.getProperty("DropOnDie", false);
		DROP_ITEMS_AUGMENTED = pvpSettings.getProperty("DropAugmented", false);

		KARMA_DROP_ITEM_LIMIT = pvpSettings.getProperty("MaxItemsDroppable", 10);
		MIN_PK_TO_ITEMS_DROP = pvpSettings.getProperty("MinPKToDropItems", 5);

		KARMA_RANDOM_DROP_LOCATION_LIMIT = pvpSettings.getProperty("MaxDropThrowDistance", 70);

		KARMA_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfPKDropBase", 20.);
		KARMA_DROPCHANCE_MOD = pvpSettings.getProperty("ChanceOfPKsDropMod", 1.);
		NORMAL_DROPCHANCE_BASE = pvpSettings.getProperty("ChanceOfNormalDropBase", 1.);
		DROPCHANCE_EQUIPPED_WEAPON = pvpSettings.getProperty("ChanceOfDropWeapon", 3);
		DROPCHANCE_EQUIPMENT = pvpSettings.getProperty("ChanceOfDropEquippment", 17);
		DROPCHANCE_ITEM = pvpSettings.getProperty("ChanceOfDropOther", 80);

		KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();
		for(int id : pvpSettings.getProperty("ListOfNonDroppableItems", new int[] {
				57,
				1147,
				425,
				1146,
				461,
				10,
				2368,
				7,
				6,
				2370,
				2369,
				3500,
				3501,
				3502,
				4422,
				4423,
				4424,
				2375,
				6648,
				6649,
				6650,
				6842,
				6834,
				6835,
				6836,
				6837,
				6838,
				6839,
				6840,
				5575,
				7694,
				6841,
				8181 }))
			KARMA_LIST_NONDROPPABLE_ITEMS.add(id);

		PVP_TIME = pvpSettings.getProperty("PvPTime", 40000);
	}
	
	public static void loadAISettings()
	{
		ExProperties aiSettings = load(AI_CONFIG_FILE);

		AI_TASK_MANAGER_COUNT = aiSettings.getProperty("AiTaskManagers", 1);
		AI_TASK_ATTACK_DELAY = aiSettings.getProperty("AiTaskDelay", 1000);
		AI_TASK_ACTIVE_DELAY = aiSettings.getProperty("AiTaskActiveDelay", 1000);
		BLOCK_ACTIVE_TASKS = aiSettings.getProperty("BlockActiveTasks", false);
		ALWAYS_TELEPORT_HOME = aiSettings.getProperty("AlwaysTeleportHome", false);

		RND_WALK = aiSettings.getProperty("RndWalk", true);
		RND_WALK_RATE = aiSettings.getProperty("RndWalkRate", 1);
		RND_ANIMATION_RATE = aiSettings.getProperty("RndAnimationRate", 2);

		AGGRO_CHECK_INTERVAL = aiSettings.getProperty("AggroCheckInterval", 250);
		NONAGGRO_TIME_ONTELEPORT = aiSettings.getProperty("NonAggroTimeOnTeleport", 15000);
		MAX_DRIFT_RANGE = aiSettings.getProperty("MaxDriftRange", 100);
		MAX_PURSUE_RANGE = aiSettings.getProperty("MaxPursueRange", 4000);
		MAX_PURSUE_UNDERGROUND_RANGE = aiSettings.getProperty("MaxPursueUndergoundRange", 2000);
		MAX_PURSUE_RANGE_RAID = aiSettings.getProperty("MaxPursueRangeRaid", 5000);
	}

	public static void loadGeodataSettings()
	{
		ExProperties geodataSettings = load(GEODATA_CONFIG_FILE);

		GEO_X_FIRST = geodataSettings.getProperty("GeoFirstX", 11);
		GEO_Y_FIRST = geodataSettings.getProperty("GeoFirstY", 10);
		GEO_X_LAST = geodataSettings.getProperty("GeoLastX", 26);
		GEO_Y_LAST = geodataSettings.getProperty("GeoLastY", 26);

		GEOFILES_PATTERN = geodataSettings.getProperty("GeoFilesPattern", "(\\d{2}_\\d{2})\\.l2j");
		ALLOW_GEODATA = geodataSettings.getProperty("AllowGeodata", true);
		ALLOW_FALL_FROM_WALLS = geodataSettings.getProperty("AllowFallFromWalls", false);
		ALLOW_KEYBOARD_MOVE = geodataSettings.getProperty("AllowMoveWithKeyboard", true);
		COMPACT_GEO = geodataSettings.getProperty("CompactGeoData", false);
		CLIENT_Z_SHIFT = geodataSettings.getProperty("ClientZShift", 16);
		PATHFIND_BOOST = geodataSettings.getProperty("PathFindBoost", 2);
		PATHFIND_DIAGONAL = geodataSettings.getProperty("PathFindDiagonal", true);
		PATH_CLEAN = geodataSettings.getProperty("PathClean", true);
		PATHFIND_MAX_Z_DIFF = geodataSettings.getProperty("PathFindMaxZDiff", 32);
		MAX_Z_DIFF = geodataSettings.getProperty("MaxZDiff", 64);
		MIN_LAYER_HEIGHT = geodataSettings.getProperty("MinLayerHeight", 64);
		PATHFIND_MAX_TIME = geodataSettings.getProperty("PathFindMaxTime", 10000000);
		PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
	}

	public static void loadEventsSettings()
	{
		ExProperties eventSettings = load(EVENTS_CONFIG_FILE);

		EVENT_CofferOfShadowsPriceRate = eventSettings.getProperty("CofferOfShadowsPriceRate", 1.);
		EVENT_CofferOfShadowsRewardRate = eventSettings.getProperty("CofferOfShadowsRewardRate", 1.);

        EVENT_LastHeroItemID = eventSettings.getProperty("LastHero_bonus_id", 57);
        EVENT_LastHeroItemCOUNT = eventSettings.getProperty("LastHero_bonus_count", 5000.);
        EVENT_LastHeroTime = eventSettings.getProperty("LastHero_time", 3);
        EVENT_LastHeroRate = eventSettings.getProperty("LastHero_rate", true);
        EVENT_LastHeroChanceToStart = eventSettings.getProperty("LastHero_ChanceToStart", 5);
        EVENT_LastHeroItemCOUNTFinal = eventSettings.getProperty("LastHero_bonus_count_final", 10000.);
        EVENT_LastHeroRateFinal = eventSettings.getProperty("LastHero_rate_final", true);

        EVENT_TvTItemID = eventSettings.getProperty("TvT_bonus_id", 57);
        EVENT_TvTItemCOUNT = eventSettings.getProperty("TvT_bonus_count", 5000.);
        EVENT_TvTTime = eventSettings.getProperty("TvT_time", 3);
        EVENT_TvT_rate = eventSettings.getProperty("TvT_rate", true);
        EVENT_TvTChanceToStart = eventSettings.getProperty("TvT_ChanceToStart", 5);

		EVENT_GvGDisableEffect = eventSettings.getProperty("GvGDisableEffect", false);
		
		EVENT_TFH_POLLEN_CHANCE = eventSettings.getProperty("TFH_POLLEN_CHANCE", 5.);

		EVENT_GLITTMEDAL_NORMAL_CHANCE = eventSettings.getProperty("MEDAL_CHANCE", 10.);
		EVENT_GLITTMEDAL_GLIT_CHANCE = eventSettings.getProperty("GLITTMEDAL_CHANCE", 0.1);

		EVENT_L2DAY_LETTER_CHANCE = eventSettings.getProperty("L2DAY_LETTER_CHANCE", 1.);
		EVENT_CHANGE_OF_HEART_CHANCE = eventSettings.getProperty("EVENT_CHANGE_OF_HEART_CHANCE", 5.);

		EVENT_APIL_FOOLS_DROP_CHANCE = eventSettings.getProperty("AprilFollsDropChance", 50.);

		EVENT_BOUNTY_HUNTERS_ENABLED = eventSettings.getProperty("BountyHuntersEnabled", true);

		EVENT_SAVING_SNOWMAN_LOTERY_PRICE = eventSettings.getProperty("SavingSnowmanLoteryPrice", 50000);
		EVENT_SAVING_SNOWMAN_REWARDER_CHANCE = eventSettings.getProperty("SavingSnowmanRewarderChance", 2);

		EVENT_TRICK_OF_TRANS_CHANCE = eventSettings.getProperty("TRICK_OF_TRANS_CHANCE", 10.);

		EVENT_MARCH8_DROP_CHANCE = eventSettings.getProperty("March8DropChance", 10.);
		EVENT_MARCH8_PRICE_RATE = eventSettings.getProperty("March8PriceRate", 1.);

		ENCHANT_CHANCE_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiEnchantChance", 66);
		ENCHANT_MAX_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiEnchantMaxWeapon", 28);
		SAFE_ENCHANT_MASTER_YOGI_STAFF = eventSettings.getProperty("MasterYogiSafeEnchant", 3);

		AllowCustomDropItems = eventSettings.getProperty("AllowCustomDropItems", true);
		CDItemsAllowMinMaxPlayerLvl = eventSettings.getProperty("CDItemsAllowMinMaxPlayerLvl", false);
		CDItemsAllowMinMaxMobLvl = eventSettings.getProperty("CDItemsAllowMinMaxMobLvl", false);
		CDItemsAllowOnlyRbDrops = eventSettings.getProperty("CDItemsAllowOnlyRbDrops", false);
		CDItemsId = eventSettings.getProperty("CDItemsId", new int[] { 57 });
		CDItemsCountDropMin = eventSettings.getProperty("CDItemsCountDropMin",  new int[] { 1 });
		CDItemsCountDropMax = eventSettings.getProperty("CDItemsCountDropMax",  new int[] { 1 });
		CustomDropItemsChance = eventSettings.getProperty("CustomDropItemsChance", new double[] { 1. });
		CDItemsMinPlayerLvl = eventSettings.getProperty("CDItemsMinPlayerLvl", 20);
		CDItemsMaxPlayerLvl = eventSettings.getProperty("CDItemsMaxPlayerLvl", 85);
		CDItemsMinMobLvl = eventSettings.getProperty("CDItemsMinMobLvl", 20);
		CDItemsMaxMobLvl = eventSettings.getProperty("CDItemsMaxMobLvl", 80);
	}

	public static void loadOlympiadSettings()
	{
		ExProperties olympSettings = load(OLYMPIAD);

		ENABLE_OLYMPIAD = olympSettings.getProperty("EnableOlympiad", true);
		ENABLE_OLYMPIAD_SPECTATING = olympSettings.getProperty("EnableOlympiadSpectating", true);
		ALT_OLY_START_TIME = olympSettings.getProperty("AltOlyStartTime", 18);
		ALT_OLY_MIN = olympSettings.getProperty("AltOlyMin", 0);
		ALT_OLY_CPERIOD = olympSettings.getProperty("AltOlyCPeriod", 21600000);
		ALT_OLY_WPERIOD = olympSettings.getProperty("AltOlyWPeriod", 604800000);
		ALT_OLY_VPERIOD = olympSettings.getProperty("AltOlyVPeriod", 43200000);
		ALT_OLY_DATE_END = olympSettings.getProperty("AltOlyDateEnd", new int[] { 1 });
		CLASS_GAME_MIN = olympSettings.getProperty("ClassGameMin", 5);
		NONCLASS_GAME_MIN = olympSettings.getProperty("NonClassGameMin", 9);
		TEAM_GAME_MIN = olympSettings.getProperty("TeamGameMin", 4);

		GAME_MAX_LIMIT = olympSettings.getProperty("GameMaxLimit", 70);
		GAME_CLASSES_COUNT_LIMIT = olympSettings.getProperty("GameClassesCountLimit", 30);
		GAME_NOCLASSES_COUNT_LIMIT = olympSettings.getProperty("GameNoClassesCountLimit", 60);
		GAME_TEAM_COUNT_LIMIT = olympSettings.getProperty("GameTeamCountLimit", 10);

		ALT_OLY_REG_DISPLAY = olympSettings.getProperty("AltOlyRegistrationDisplayNumber", 100);
		ALT_OLY_BATTLE_REWARD_ITEM = olympSettings.getProperty("AltOlyBattleRewItem", 13722);
		ALT_OLY_CLASSED_RITEM_C = olympSettings.getProperty("AltOlyClassedRewItemCount", 50);
		ALT_OLY_NONCLASSED_RITEM_C = olympSettings.getProperty("AltOlyNonClassedRewItemCount", 40);
		ALT_OLY_TEAM_RITEM_C = olympSettings.getProperty("AltOlyTeamRewItemCount", 50);
		ALT_OLY_COMP_RITEM = olympSettings.getProperty("AltOlyCompRewItem", 13722);
		ALT_OLY_GP_PER_POINT = olympSettings.getProperty("AltOlyGPPerPoint", 1000);
		ALT_OLY_HERO_POINTS = olympSettings.getProperty("AltOlyHeroPoints", 180);
		ALT_OLY_RANK1_POINTS = olympSettings.getProperty("AltOlyRank1Points", 120);
		ALT_OLY_RANK2_POINTS = olympSettings.getProperty("AltOlyRank2Points", 80);
		ALT_OLY_RANK3_POINTS = olympSettings.getProperty("AltOlyRank3Points", 55);
		ALT_OLY_RANK4_POINTS = olympSettings.getProperty("AltOlyRank4Points", 35);
		ALT_OLY_RANK5_POINTS = olympSettings.getProperty("AltOlyRank5Points", 20);
		OLYMPIAD_STADIAS_COUNT = olympSettings.getProperty("OlympiadStadiasCount", 160);
		OLYMPIAD_BEGIN_TIME = olympSettings.getProperty("OlympiadBeginTime", 120);
		OLYMPIAD_BATTLES_FOR_REWARD = olympSettings.getProperty("OlympiadBattlesForReward", 15);
		OLYMPIAD_POINTS_DEFAULT = olympSettings.getProperty("OlympiadPointsDefault", 50);
		OLYMPIAD_POINTS_WEEKLY = olympSettings.getProperty("OlympiadPointsWeekly", 10);
		OLYMPIAD_OLDSTYLE_STAT = olympSettings.getProperty("OlympiadOldStyleStat", false);
		OLYMPIAD_PLAYER_IP = olympSettings.getProperty("OlympiadPlayerIp", false);
		OLYMPIAD_BAD_ENCHANT_ITEMS_ALLOW = olympSettings.getProperty("OlympiadUnEquipBadEnchantItem", false);
		
		OLY_ENCH_LIMIT_ENABLE = olympSettings.getProperty("OlyEnchantLimit", false);
        OLY_ENCHANT_LIMIT_WEAPON = olympSettings.getProperty("OlyEnchantLimitWeapon", 0);
        OLY_ENCHANT_LIMIT_ARMOR = olympSettings.getProperty("OlyEnchantLimitArmor", 0);
        OLY_ENCHANT_LIMIT_JEWEL = olympSettings.getProperty("OlyEnchantLimitJewel", 0);
	}
	
	public static void loadEnchantCBConfig()
	{
		ExProperties EnchantCBSetting = load(ENCHANT_CB_CONFIG_FILE);
		
		ENCHANT_ENABLED = EnchantCBSetting.getProperty("Enchant_enabled", false);
		ENCHANTER_ITEM_ID = EnchantCBSetting.getProperty("CBEnchantItem", 4037);
		MAX_ENCHANT = EnchantCBSetting.getProperty("CBEnchantItem", 20);
		ENCHANT_LEVELS = EnchantCBSetting.getProperty("CBEnchantLvl", new int[] { 1 });
		ENCHANT_PRICE_WPN = EnchantCBSetting.getProperty("CBEnchantPriceWeapon", new int[] { 1 });
		ENCHANT_PRICE_ARM = EnchantCBSetting.getProperty("CBEnchantPriceArmor", new int[] { 1 });
		ENCHANT_ATTRIBUTE_LEVELS = EnchantCBSetting.getProperty("CBEnchantAtributeLvlWeapon", new int[] { 1 });
		ENCHANT_ATTRIBUTE_LEVELS_ARM = EnchantCBSetting.getProperty("CBEnchantAtributeLvlArmor", new int[] { 1 });
		ATTRIBUTE_PRICE_WPN = EnchantCBSetting.getProperty("CBEnchantAtributePriceWeapon", new int[] { 1 });
		ATTRIBUTE_PRICE_ARM = EnchantCBSetting.getProperty("CBEnchantAtributePriceArmor", new int[] { 1 });
		ENCHANT_ATT_PVP = EnchantCBSetting.getProperty("CBEnchantAtributePvP", false);


	}
	public static void loadPremiumConfig()
	{
		ExProperties premiumConf = load(PREMIUM_FILE);

		SERVICES_RATE_TYPE = premiumConf.getProperty("RateBonusType", Bonus.NO_BONUS);
		SERVICES_RATE_CREATE_PA = premiumConf.getProperty("RateBonusCreateChar", 0);
		SERVICES_RATE_BONUS_PRICE = premiumConf.getProperty("RateBonusPrice", new int[] { 1500 });
		SERVICES_RATE_BONUS_ITEM = premiumConf.getProperty("RateBonusItem", new int[] { 4037 });			
		SERVICES_RATE_BONUS_VALUE = premiumConf.getProperty("RateBonusValue", new double[] { 2. });
		SERVICES_RATE_BONUS_DAYS = premiumConf.getProperty("RateBonusTime", new int[] { 30 });
		AUTO_LOOT_PA = premiumConf.getProperty("AutoLootPA", false);
		ENCHANT_CHANCE_WEAPON_PA = premiumConf.getProperty("EnchantChancePA", 66);
		ENCHANT_CHANCE_ARMOR_PA = premiumConf.getProperty("EnchantChanceArmorPA", ENCHANT_CHANCE_WEAPON);
		ENCHANT_CHANCE_ACCESSORY_PA = premiumConf.getProperty("EnchantChanceAccessoryPA", ENCHANT_CHANCE_ARMOR);
		ENCHANT_CHANCE_WEAPON_BLESS_PA = premiumConf.getProperty("EnchantChanceBlessPA", 66);
		ENCHANT_CHANCE_ARMOR_BLESS_PA = premiumConf.getProperty("EnchantChanceArmorBlessPA", ENCHANT_CHANCE_WEAPON);
		ENCHANT_CHANCE_ACCESSORY_BLESS_PA = premiumConf.getProperty("EnchantChanceAccessoryBlessPA", ENCHANT_CHANCE_ARMOR);
		ENCHANT_CHANCE_CRYSTAL_WEAPON_PA = premiumConf.getProperty("EnchantChanceCrystalPA", 66);
		ENCHANT_CHANCE_CRYSTAL_ARMOR_PA = premiumConf.getProperty("EnchantChanceCrystalArmorPA", ENCHANT_CHANCE_CRYSTAL_WEAPON);
		ENCHANT_CHANCE_CRYSTAL_ACCESSORY_PA = premiumConf.getProperty("EnchantChanceCrystalAccessory", ENCHANT_CHANCE_CRYSTAL_ARMOR);
	
		SERVICES_BONUS_XP = premiumConf.getProperty("RateBonusXp", 1.);
		SERVICES_BONUS_SP = premiumConf.getProperty("RateBonusSp", 1.);
		SERVICES_BONUS_ADENA = premiumConf.getProperty("RateBonusAdena", 1.);
		SERVICES_BONUS_ITEMS = premiumConf.getProperty("RateBonusItems", 1.);
		SERVICES_BONUS_SPOIL = premiumConf.getProperty("RateBonusSpoil", 1.);
                
                USE_ALT_ENCHANT_PA = Boolean.parseBoolean(premiumConf.getProperty("UseAltEnchantPA", "False"));
		for (String prop : premiumConf.getProperty("EnchantWeaponFighterPA", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantWeaponFighterCrystalPA", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_BLESSED_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantWeaponFighterBlessedPA", "100,100,100,70,70,70,70,70,70,70,70,70,70,70,70,35,35,35,35,35").split(","))
			ENCHANT_WEAPON_FIGHT_CRYSTAL_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantArmorPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantArmorCrystalPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_CRYSTAL_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantArmorBlessedPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_BLESSED_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantJewelryPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantJewelryCrystalPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_CRYSTAL_PA.add(Integer.parseInt(prop));
		for (String prop : premiumConf.getProperty("EnchantJewelryBlessedPA", "100,100,100,66,33,25,20,16,14,12,11,10,9,8,8,7,7,6,6,6").split(","))
			ENCHANT_ARMOR_JEWELRY_BLESSED_PA.add(Integer.parseInt(prop));
	}

	public static void loadTalkGuardConfig()
	{
		ExProperties TalkGuardSetting = load(TALKING_GUARD_CONFIG_FILE);
		
		TalkGuardChance = TalkGuardSetting.getProperty("TalkGuardChance", 4037);
		TalkNormalChance = TalkGuardSetting.getProperty("TalkNormalChance", 4037);
		TalkNormalPeriod = TalkGuardSetting.getProperty("TalkNormalPeriod", 4037);
		TalkAggroPeriod = TalkGuardSetting.getProperty("TalkAggroPeriod", 4037);
	}
	
	public static void loadBufferConfig()
	{
		ExProperties BufferConfig = load(BUFFER_CONFIG_FILE);

		BUFFER_ON = BufferConfig.getProperty("Buffer", false);
		ITEM_ID = BufferConfig.getProperty("Item_id", 57);
		BUFFER_PET_ENABLED = BufferConfig.getProperty("Buffer_pet", false);
		BUFFER_PRICE = BufferConfig.getProperty("Buffer_price", 20);
		BUFFER_MIN_LVL = BufferConfig.getProperty("Buffer_min_lvl", 1);
		BUFFER_MAX_LVL = BufferConfig.getProperty("Buffer_max_lvl", 99);
		BUFFER_ALLOW_IN_INSTANCE = BufferConfig.getProperty("BufferAllowInInstance", false);
		
    }
	
	// Перенос персонажей между аккаунтами
	public static void loadAcc_moveConfig()
	{
		ExProperties Acc_moveConfig = load(ACC_MOVE_FILE);

		ACC_MOVE_ENABLED = Acc_moveConfig.getProperty("Acc_move_enabled", false);
		ACC_MOVE_ITEM = Acc_moveConfig.getProperty("Acc_move_item", 57);
		ACC_MOVE_PRICE = Acc_moveConfig.getProperty("Acc_move_price", 57);
		
    }

	
	//RWHO system (off emulation)
	public static boolean RWHO_LOG;
	public static int RWHO_FORCE_INC;
	public static int RWHO_KEEP_STAT;
	public static int RWHO_MAX_ONLINE;
	public static boolean RWHO_SEND_TRASH;
	public static int RWHO_ONLINE_INCREMENT;
	public static float RWHO_PRIV_STORE_FACTOR;
	public static int RWHO_ARRAY[] = new int[13];
	
	
	public static void loadTeamVSTeamSettings()
	{
		ExProperties eventTeamVSTeamSettings = load(EVENT_TEAM_VS_TEAM_CONFIG_FILE);
		
        EVENT_TvTRewards = eventTeamVSTeamSettings.getProperty("TvT_Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTTime = eventTeamVSTeamSettings.getProperty("TvT_time", 3);
		EVENT_TvTStartTime = eventTeamVSTeamSettings.getProperty("TvT_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_TvTCategories = eventTeamVSTeamSettings.getProperty("TvT_Categories", false);
		EVENT_TvTMaxPlayerInTeam = eventTeamVSTeamSettings.getProperty("TvT_MaxPlayerInTeam", 20);
		EVENT_TvTMinPlayerInTeam = eventTeamVSTeamSettings.getProperty("TvT_MinPlayerInTeam", 2);
		EVENT_TvTAllowSummons = eventTeamVSTeamSettings.getProperty("TvT_AllowSummons", false);
		EVENT_TvTAllowBuffs = eventTeamVSTeamSettings.getProperty("TvT_AllowBuffs", false);
		EVENT_TvTAllowMultiReg = eventTeamVSTeamSettings.getProperty("TvT_AllowMultiReg", false);
		EVENT_TvTCheckWindowMethod = eventTeamVSTeamSettings.getProperty("TvT_CheckWindowMethod", "IP");
		EVENT_TvTEventRunningTime = eventTeamVSTeamSettings.getProperty("TvT_EventRunningTime", 20);
		EVENT_TvTFighterBuffs = eventTeamVSTeamSettings.getProperty("TvT_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTMageBuffs = eventTeamVSTeamSettings.getProperty("TvT_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_TvTBuffPlayers = eventTeamVSTeamSettings.getProperty("TvT_BuffPlayers", false);	
		EVENT_TvTrate = eventTeamVSTeamSettings.getProperty("TvT_rate", true);
		EVENT_TvTOpenCloseDoors = eventTeamVSTeamSettings.getProperty("TvT_OpenCloseDoors", new int[] { 24190001, 24190002, 24190003, 24190004 });
        EVENT_TvT_DISALLOWED_SKILLS = eventTeamVSTeamSettings.getProperty("TvT_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
		
	}
	
	public static void loadCaptureTheFlagSettings()
	{
		ExProperties eventCaptureTheFlagSettings = load(EVENT_CAPTURE_THE_FLAG_CONFIG_FILE);

		EVENT_CtFRewards = eventCaptureTheFlagSettings.getProperty("CtF_Rewards", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtfTime = eventCaptureTheFlagSettings.getProperty("CtF_time", 3);
		EVENT_CtFrate = eventCaptureTheFlagSettings.getProperty("CtF_rate", true);
		EVENT_CtFStartTime = eventCaptureTheFlagSettings.getProperty("CtF_StartTime", "20:00").trim().replaceAll(" ", "").split(",");
		EVENT_CtFCategories = eventCaptureTheFlagSettings.getProperty("CtF_Categories", false);
		EVENT_CtFMaxPlayerInTeam = eventCaptureTheFlagSettings.getProperty("CtF_MaxPlayerInTeam", 20);
		EVENT_CtFMinPlayerInTeam = eventCaptureTheFlagSettings.getProperty("CtF_MinPlayerInTeam", 2);
		EVENT_CtFAllowSummons = eventCaptureTheFlagSettings.getProperty("CtF_AllowSummons", false);
		EVENT_CtFAllowBuffs = eventCaptureTheFlagSettings.getProperty("CtF_AllowBuffs", false);
		EVENT_CtFAllowMultiReg = eventCaptureTheFlagSettings.getProperty("CtF_AllowMultiReg", false);
		EVENT_CtFCheckWindowMethod = eventCaptureTheFlagSettings.getProperty("CtF_CheckWindowMethod", "IP");
		EVENT_CtFFighterBuffs = eventCaptureTheFlagSettings.getProperty("CtF_FighterBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtFMageBuffs = eventCaptureTheFlagSettings.getProperty("CtF_MageBuffs", "").trim().replaceAll(" ", "").split(";");
		EVENT_CtFBuffPlayers = eventCaptureTheFlagSettings.getProperty("CtF_BuffPlayers", false);
		EVENT_CtFOpenCloseDoors = eventCaptureTheFlagSettings.getProperty("CtF_OpenCloseDoors", new int[] { 24190001, 24190002, 24190003, 24190004 });
        EVENT_CtF_DISALLOWED_SKILLS = eventCaptureTheFlagSettings.getProperty("CtF_DisallowedSkills", "").trim().replaceAll(" ", "").split(";");
		
	}
	
	
	public static void load()
	{
		loadServerConfig();
		loadTelnetConfig();
		loadResidenceConfig();
		loadOtherConfig();
		loadSpoilConfig();
		loadFormulasConfig();
		loadAltSettings();
		loadServicesSettings();
		loadPvPSettings();
		loadAISettings();
		loadGeodataSettings();
		loadEventsSettings();
		loadOlympiadSettings();
		loadDevelopSettings();
		loadExtSettings();
		loadTopSettings();
		loadRatesConfig();
		loadFightClubSettings();
		loadItemsUseConfig();
		loadChatConfig();
		loadNpcConfig();
		loadBossConfig();
		loadEpicBossConfig();
		loadWeddingConfig();
		loadInstancesConfig();
		loadItemsSettings();
		abuseLoad();
		loadGMAccess();
		loadPremiumConfig();
		loadPvPmodConfig();
		loadVIKTORINAsettings();
		if(ADVIPSYSTEM)
			ipsLoad();
		if(ALLOW_ADDONS_CONFIG)
			AddonsConfig.load();
		// комюнити
		loadCommunityPvPboardsettings();
		loadCommunityPvPbuffersettings();
		loadCommunityPvPclasssettings();
		loadCommunityPvPshopsettings();
		loadCommunityPvPteleportsettings();
		loadEnchantCBConfig();
		loadCommandssettings();
		loadBufferConfig();
		loadTalkGuardConfig();
		loadAcc_moveConfig();
		loadTeamVSTeamSettings();
		loadCaptureTheFlagSettings();
	}

	private Config()
	{}

	public static void abuseLoad()
	{
		List<Pattern> tmp = new ArrayList<Pattern>();

		LineNumberReader lnr = null;
		try
		{
			String line;

			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(ANUSEWORDS_CONFIG_FILE), "UTF-8"));

			while((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
					tmp.add(Pattern.compile(".*" + st.nextToken() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}

			ABUSEWORD_LIST = tmp.toArray(new Pattern[tmp.size()]);
			tmp.clear();
			_log.info("Abuse: Loaded " + ABUSEWORD_LIST.length + " abuse words.");
		}
		catch(IOException e1)
		{
			_log.warn("Error reading abuse: " + e1);
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

	public static void loadGMAccess()
	{
		gmlist.clear();
		loadGMAccess(new File(GM_PERSONAL_ACCESS_FILE));
		File dir = new File(GM_ACCESS_FILES_DIR);
		if(!dir.exists() || !dir.isDirectory())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists.");
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
		}
	}

	public static String getField(String fieldName)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);

		if(field == null)
			return null;

		try
		{
			return String.valueOf(field.get(null));
		}
		catch(IllegalArgumentException e)
		{

		}
		catch(IllegalAccessException e)
		{

		}

		return null;
	}

	public static boolean setField(String fieldName, String value)
	{
		Field field = FieldUtils.getField(Config.class, fieldName);

		if(field == null)
			return false;

		try
		{
			if(field.getType() == boolean.class)
				field.setBoolean(null, BooleanUtils.toBoolean(value));
			else if(field.getType() == int.class)
				field.setInt(null, NumberUtils.toInt(value));
			else if(field.getType() == long.class)
				field.setLong(null, NumberUtils.toLong(value));
			else if(field.getType() == double.class)
				field.setDouble(null, NumberUtils.toDouble(value));
			else if(field.getType() == String.class)
				field.set(null, value);
			else
				return false;
		}
		catch(IllegalArgumentException e)
		{
			return false;
		}
		catch(IllegalAccessException e)
		{
			return false;
		}

		return true;
	}

	public static ExProperties load(String filename)
	{
		return load(new File(filename));
	}

	public static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();

		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			_log.error("Error loading config : " + file.getName() + "!");
		}

		return result;
	}

	public static boolean containsAbuseWord(String s)
	{
		for(Pattern pattern : ABUSEWORD_LIST)
			if(pattern.matcher(s).matches())
				return true;
		return false;
	}

	private static void ipsLoad()
	{
		ExProperties ipsSettings = load(ADV_IP_FILE);

		String NetMask;
		String ip;
		for(int i = 0; i < ipsSettings.size() / 2; i++)
		{
			NetMask = ipsSettings.getProperty("NetMask" + (i + 1));
			ip = ipsSettings.getProperty("IPAdress" + (i + 1));
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
}