package net.sf.l2j;

import gnu.trove.TIntIntHashMap;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Utils;
import net.sf.l2j.gameserver.util.StringUtil;
import net.sf.l2j.util.Util;

public final class Config
{
    protected static final Logger _log = Logger.getLogger(Config.class.getName());
    public static boolean REMOVE_BUFFS_AFTER_DEATH;
	public static boolean OLY_SAME_IP;
	// CTF EVENT OPEN
	public static String CTF_EVEN_TEAMS;
	public static boolean CTF_ALLOW_INTERFERENCE;
	public static boolean CTF_ALLOW_SUMMON;
	public static boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean CTF_ON_START_UNSUMMON_PET;
	public static boolean CTF_REVIVE_RECOVERY;
	public static int CTF_RND_SPAWNXMIN;
	public static int CTF_RND_SPAWNXMAX;
	public static int CTF_RND_SPAWNYMIN;
	public static int CTF_RND_SPAWNYMAX;
	public static int CTF_SPAWN_Z;
	//CTF EVENT CLOSE
	public static int CHANCE_WIN;
	public static int STAWKAID_COIN;
	public static int STAWKA_COIN_AMOUNT;
	public static int STAWKAID_ADENA;
	public static int STAWKA_ADENA_AMOUNT;
	public static int WIN_COIN;
	public static int WIN_COIN_AMOUNT;
	public static int WIN_ADENA;
	public static int WIN_ADENA_AMOUNT;
	
	//champion
	public static boolean CHAMPION_ENABLE;
	public static int CHAMPION_FREQUENCY;
	public static int CHAMPION_MIN_LVL;
	public static int CHAMPION_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static int CHAMPION_XPSP;
	public static int CHAMPION_ADENAS_REWARDS;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	public static String CHAMPION_TITLE;
	public static boolean CHAMPION_AURA;
	public static boolean SHOW_NPC_CREST;
	public static boolean USE_TRADE_ZONE;
 	// ITEM BUFF SKILLS  START
 	public static int IB_ITEM_ID1;
 	public static int IB_ITEM_ID2;
 	public static int IB_ITEM_ID3;
 	public static int IB_ITEM_ID4;
 	public static int IB_ITEM_ID5;
	public static int IB_SKILL_ID1;
	public static int IB_SKILL_LVL1;
	public static int IB_SKILL_ID2;
	public static int IB_SKILL_LVL2;
	public static int IB_SKILL_ID3;
	public static int IB_SKILL_LVL3;
	public static int IB_SKILL_ID4;
	public static int IB_SKILL_LVL4;
	public static int IB_SKILL_ID5;
	public static int IB_SKILL_LVL5;
	//IB END
	// Skill Duration - OPEN
	public static boolean	ENABLE_MODIFY_SKILL_DURATION;
	public static			Map<Integer, Integer> SKILL_DURATION_LIST;
	// Skill Duration - CLOSE
	public static boolean ENABLE_NO_AUTOLEARN_LIST;
	public static FastList<Integer> NO_AUTOLEARN_LIST;
	
	public static boolean RB_HEAL;
	public static boolean GB_LOADER;
	public static int ANTHARAS_WAIT_TIME;
	public static int ANTHARAS_INTERVAL_SPAWN;
	public static int ANTHARAS_RANDOM_SPAWN;
	
	//WS
	public static boolean WEB_SERVER_ENABLE;
	public static int WEB_SERVER_PORT;
	public static String WEB_SERVER_ROOT;
	public static boolean USE_FILE_CACHE;
	//RRD
	public static boolean RRD_ENABLED;
	public static boolean RRD_EXTENDED;
	public static boolean RRD_GRAPH_AA;
	public static String RRD_PATH;
	public static String RRD_EXT_PATH;
	public static String RRD_GRAPH_PATH;
	public static String RRD_GRAPH_FORMAT;
	public static long RRD_REDRAW_TIME;
	public static long RRD_UPDATE_TIME;
	public static int RRD_GRAPH_HEIGHT;
	public static int RRD_GRAPH_WIDTH;
	
	public static int DUEL_SPAWN_X; 
	public static int DUEL_SPAWN_Y; 
	public static int DUEL_SPAWN_Z;
	public static boolean MOVE_BASED_KNOWNLIST;
	public static int CLAN_MEMBER_LVLUP6;
	public static int CLAN_MEMBER_LVLUP7;
	public static int CLAN_MEMBER_LVLUP8;
	
	public static boolean DUEL_ALLOW;
	public static int REWARD_KILL_WAR;
	public static int BONUS_CLAN_SCORE_SIEGE;
	public static int CLAN_SCORE_SIEGE;
	public static int DAY_TO_SIEGE;
	public static boolean KICK_L2WALKER;
	public static CorrectSpawnsZ GEO_CORRECT_Z;
	 public static boolean FORCE_GEODATA;
	  public static boolean GEODATA;
	  public static boolean GEO_DOORS;
	  public static boolean GEO_CHECK_LOS;
	  public static boolean GEO_MOVE_PC;
	  public static boolean GEO_MOVE_NPC;
	  public static boolean GEO_PATH_FINDING;
	  public static boolean ADVANCED_DIAGONAL_STRATEGY;
	  public static boolean DEBUG_PATH;
	  public static float HIGH_WEIGHT;
	  public static float LOW_WEIGHT;
	  public static float MEDIUM_WEIGHT;
	  public static float DIAGONAL_WEIGHT;
	  public static String PATHFIND_BUFFERS;
	  public static int MAX_POSTFILTER_PASSES;
	
    public static boolean ACCEPT_GEOEDITOR_CONN;
	public static boolean SHOW_NPC_LVL;
	public static int ENCHANT_FAIL;
	public static boolean ENCHANT_STACKABLE;
	public static boolean ENABLE_NEWCHAR_TITLE;
	public static String NEW_CHAR_TITLE;
	public static enum CorrectSpawnsZ
	{
		TOWN, MONSTER, ALL, NONE
	}
	//Add file
	public static int   COL_TITLECOLOR;
	public static int   COL_NICKCOLOR;
	public static int   COL_CHANGENAME;
	public static int   COL_CHANGECLANNAME;
	public static int   COL_6LVL_CLAN;
	public static int   COL_7LVL_CLAN;
	public static int   COL_8LVL_CLAN;
	public static int   COL_NOBLESSE;
	public static int   COL_PREM1;
	public static int   COL_PREM2;
	public static int   COL_PREM3;
	public static int   COL_SEX;
	public static int   COL_PK;
	public static int   COL_HERO;
	public static int   COL_CRP;
	public static int   CRP_COUNT;
	public static int   DON_ITEM_ID;	
	public static int   CRP_ITEM_ID;
	//end
	public static List<Integer> COMMUN_MULT_LIST = new ArrayList<Integer>();
	
	//Elite Clan Hall
	public static int DEVASTATED_DAY;
    public static int DEVASTATED_HOUR;
	public static int DEVASTATED_MINUTES;
	public static int PARTISAN_DAY;
	public static int PARTISAN_HOUR;
	public static int PARTISAN_MINUTES;
	//end
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
	public static int TIMELIMITOFINVADE;
	public static boolean FWS_ENABLESINGLEPLAYER;
    
	
	public static int Valakas_Wait_Time;
	public static int Interval_Of_Valakas_Spawn;
	public static int Random_Of_Valakas_Spawn;
	public static int Interval_Of_Baium_Spawn;
	public static int Random_Of_Baium_Spawn;
	
	public static int FWS_FIXINTERVALOFSAILRENSPAWN;
	public static int FWS_RANDOMINTERVALOFSAILRENSPAWN;
	public static int FWS_INTERVALOFNEXTMONSTER;
	public static int FWS_ACTIVITYTIMEOFMOBS;

	public static int HPH_FIXINTERVALOFHALTER;
	public static int HPH_RANDOMINTERVALOFHALTER;
	public static int HPH_APPTIMEOFHALTER;
	public static int HPH_ACTIVITYTIMEOFHALTER;
	public static int HPH_FIGHTTIMEOFHALTER;
	public static int HPH_CALLROYALGUARDHELPERCOUNT;
	public static int HPH_CALLROYALGUARDHELPERINTERVAL;
	public static int HPH_INTERVALOFDOOROFALTER;
	public static int HPH_TIMEOFLOCKUPDOOROFALTAR;
	public static int LIT_REGISTRATION_MODE;
	public static int LIT_REGISTRATION_TIME;
	public static int LIT_MIN_PARTY_CNT;
	public static int LIT_MAX_PARTY_CNT;
	public static int LIT_MIN_PLAYER_CNT;
	public static int LIT_MAX_PLAYER_CNT;
	public static int LIT_TIME_LIMIT;
	public static int LIT_FIXINTERVALOFFRINTEZZA;
	public static int LIT_RANDOMINTERVALOFFRINTEZZA;
	public static int LIT_APPTIMEOFFRINTEZZA;
	public static int LIT_ACTIVITYTIMEOFFRINTEZZA;
	
	
	public static boolean ALT_DIFF_MAGIC;
	public static int MAX_MULTISELL;
	public static int MAX_SUBCLASS;
    public static boolean RWHO_LOG;
	public static String RWHO_CLIENT;
    public static int RWHO_FORCE_INC;
    public static int RWHO_KEEP_STAT;
    public static int RWHO_MAX_ONLINE;
    public static boolean RWHO_SEND_TRASH;
    public static int RWHO_ONLINE_INCREMENT;
    public static float RWHO_PRIV_STORE_FACTOR;
    public static int RWHO_ARRAY[] = new int[13];
    public static boolean ENABLE_MENU;
	public static boolean EXPERTISE_PENALTY;
	// Tanks cant use Bows - open
	public static boolean BOWTANK_PENALTY;
	// Tanks cant use Bows - close
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static byte LEVEL_ON_ENTER;
	public static int SP_ON_ENTER;
	public static float ALT_CRIT_DAMAGE;
	public static float BLOW_DAMAGE_HEAVY;
	public static float BLOW_DAMAGE_LIGHT;
	public static float BLOW_DAMAGE_ROBE;
	public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
	public static int SUBCLASS_PROTECT;
	public static boolean NOT_CONSUME_SHOTS;
	public static boolean NOT_CONSUME_ARROWS;
	public static int SHOUT_FLOOD_TIME;
	public static int WH_FLOOD_TIME;
	public static int ENCHANT_FLOOD_TIME;
	public static int TRADE_FLOOD_TIME;
	public static int MAX_MESSAGE_LENGHT;
	public static int ATTACK_STANCE_TASKS;
	public static int ALT_PCRITICAL_CAP;
	public static int ALT_MCRITICAL_CAP;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	//TVT
	  public static boolean TVT_EVENT_ENABLED;
	  public static String[] TVT_EVENT_INTERVAL;
	  public static int TVT_EVENT_PARTICIPATION_TIME;
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
	  public static List<Integer> TVT_DOORS_IDS;
	  public static boolean TVT_REWARD_TEAM_TIE;
	  public static byte TVT_EVENT_MIN_LVL;
	  public static byte TVT_EVENT_MAX_LVL;
	  public static int TVT_EVENT_EFFECTS_REMOVAL;
	  public static boolean TVT_ALLOW_VOICED_COMMAND;
	  public static boolean TVT_ALLOW_REGISTER_VOICED_COMMAND;
	  public static boolean TVT_SAME_IP;
	  public static boolean TVT_RESTORE_PLAYER_POS;
	  public static List<Integer> LIST_TVT_RESTRICTED_ITEMS = new ArrayList<Integer>();
	  public static boolean TVT_REWARD_ONLY_KILLERS;
	  public static boolean TVT_EVENT_ALLOW_PEACE_ATTACK;
	  public static boolean TVT_EVENT_ALLOW_FLAG;
	  public static boolean TVT_EVENT_RESTORE_CPHPMP;
	  
	  public static boolean USE_SAY_FILTER;
	  public static boolean USE_SAY_FILTER_EXCEPTIONS;
	  public static boolean BAN_FOR_BAD_WORDS;
	  public static boolean KARMA_FOR_BAD_WORDS;
	  public static boolean HARD_FILTERING;
	  public static int TIME_AUTO_CHAT_BAN;
	  public static int KARMA_FOR_BAD_WORD_MIN;
	  public static int KARMA_FOR_BAD_WORD_MAX;
	  public static String SAY_FILTER_REPLACEMENT_STRING;
	  public static String CHAT_BAN_REASON;
	
	//CHAT LVL'S
	public static int TELL_CHAT_LVL;
	public static int SHOUT_CHAT_LVL;
	public static int TRADE_CHAT_LVL;
	
	
	//NICKNAMES
	public static String NOT_ALLOWED_NICKS;
	public static List<String> LIST_NOT_ALLOWED_NICKS = new FastList<String>();
	
	//--------------------------------------------------
    // Premium start
    //--------------------------------------------------
    public static boolean USE_PREMIUMSERVICE;
    public static float PREMIUM_RATE_XP;
    public static float PREMIUM_RATE_SP;
	public static float PREMIUM_RATE_DROP_ADENA;
    public static float PREMIUM_RATE_DROP_SPOIL;
    public static float PREMIUM_RATE_DROP_ITEMS;
    public static float PREMIUM_RATE_DROP_QUEST;
    public static float PREMIUM_RATE_DROP_ITEMS_BY_RAID;
	public static int PREMIUM_NICK_COLOR;
	public static int PREMIUM_TITLE_COLOR;
	//end
	public static String NON_CHECK_SKILLS;
	public static List<Integer> LIST_NON_CHECK_SKILLS = new FastList<Integer>();
	public static boolean CHECK_NOBLE_SKILLS;
	public static boolean CHECK_HERO_SKILLS;
	public static boolean NPC_ATTACKABLE;
	public static boolean pccafe_event;
	public static int pccafe_min_lvl;
	public static int pccafe_score_min;
	public static int pccafe_score_max;
	public static int pccafe_score_double;
	public static int pccafe_interval;
	public static List<Integer> INVUL_NPC_LIST;
	public static int[] BUFFS_LIST;
	public static int[] BUFFER_TABLE_DIALOG;
	public static int BUFF_MAGE_1;
	public static int BUFF_MAGE_2;
	public static int BUFF_MAGE_3;
	public static int BUFF_FIGHTER_3;
	public static int BUFF_FIGHTER_2;
	public static int BUFF_FIGHTER_1;
	public static int BUFF_OTHER;
	public static int BUFF_REBUF;
	public static int BUFF_ITEM_ID;
	public static boolean ON_ENTER_BUFFS;
	public static int ON_ENTER_BUFFS_LVL;
	public static TIntIntHashMap ON_ENTER_F_BUFFS;
	public static TIntIntHashMap ON_ENTER_M_BUFFS;
	
	public static TIntIntHashMap ITEMS_ON_CREATE_CHAR;
	
	public static boolean CHECK_SKILLS_ON_ENTER;
	public static int PVPPK_PROTECT;
	public static int PVP_ITEM_ID;
	public static int PVP_ITEM_COUNT;
	public static long PVP_EXP_COUNT;
	public static int PVP_SP_COUNT;
	public static int PVP_LEVEL_DIFFERENCE;
	public static int PK_ITEM_ID;
	public static int PK_ITEM_COUNT;
	public static long PK_EXP_COUNT;
	public static int PK_SP_COUNT;
	public static int PK_LEVEL_DIFFERENCE;
	public static boolean PVP_STRICT_IP;
	public static boolean PK_STRICT_IP;
    public static boolean DEBUG;
	public static String CLASS_MASTER_SETTINGS_LINE;
	public static boolean CLASS_MASTER_STRIDER_UPDATE;
	public static int STRIDER_LEVEL_FOR_UP;
	public static int PRICE_FOR_STRIDER;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;	
	public static boolean CLASSMASTER_MSG;
	public static boolean VIEW_SKILL_CHANCE;
	public static int ALT_MINIMUM_FALL_HEIGHT;
	public static int ANONS_PVP_PK;
	public static int FRONT_CHANCE;
	public static int SIDE_CHANCE;
	public static int BEHIND_CHANCE;
	public static boolean REGION_PVP_PK;
	public static boolean LIMITED_ENTRY_CATACOMB;
	public static boolean LIMITED_ENTRY_NECRO;
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
    public static int   MAXIMUM_ONLINE_USERS;
    public static boolean SERVER_LIST_BRACKET;
    public static boolean SERVER_LIST_CLOCK;
    public static boolean SERVER_LIST_TESTSERVER;
    public static boolean SERVER_GMONLY;
    public static int THREAD_P_EFFECTS;
    public static int THREAD_P_GENERAL;
    public static int GENERAL_PACKET_THREAD_CORE_SIZE;
    public static int IO_PACKET_THREAD_CORE_SIZE;
    public static int GENERAL_THREAD_CORE_SIZE;
    public static int AI_MAX_THREAD;
    public static boolean AUTO_LOOT;
	public static boolean BOSS_AUTO_LOOT;
    public static boolean AUTO_LOOT_HERBS;
    public static String CNAME_TEMPLATE;
    public static String PET_NAME_TEMPLATE;
    public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
    public static String  DEFAULT_GLOBAL_CHAT;
    public static String  DEFAULT_TRADE_CHAT;
    public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
    public static boolean ALT_GAME_CREATION;
    public static double ALT_GAME_CREATION_SPEED;
    public static double ALT_GAME_CREATION_XP_RATE;
    public static double ALT_GAME_CREATION_SP_RATE;
    public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static boolean REMOVE_CASTLE_CIRCLETS;
    public static double ALT_WEIGHT_LIMIT;
    public static boolean ALT_GAME_SKILL_LEARN;
    public static boolean AUTO_LEARN_SKILLS;
    public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
    public static boolean ALT_GAME_CANCEL_BOW;
    public static boolean ALT_GAME_CANCEL_CAST;
    public static boolean ALT_GAME_TIREDNESS;
    public static int ALT_PARTY_RANGE;
    public static int ALT_PARTY_RANGE2;
    public static int MALARIA_CHANCE;
    public static int FLU_CHANCE;
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
    public static boolean ALT_GAME_FREE_TELEPORT;
    public static boolean ALT_RECOMMEND;
    public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
    public static boolean SUBCLASS_WITH_ITEM_AND_NO_QUEST;
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
    public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
    public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
    public static boolean LIFE_CRYSTAL_NEEDED;
    public static boolean SP_BOOK_NEEDED;
    public static boolean ES_SP_BOOK_NEEDED;
    public static boolean LOG_CHAT;
    public static boolean LOG_ITEMS;
    public static boolean ALT_PRIVILEGES_ADMIN;
    public static boolean ALT_PRIVILEGES_SECURE_CHECK;
    public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
    public static boolean ALT_OLY_WEEK;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_BWAIT;
	public static long ALT_OLY_IWAIT;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_MIN_POINT_FOR_EXCH;
	public static int ALT_OLY_HERO_POINTS;
	public static String ALT_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_MAX_ENCHANT;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS = new FastList<Integer>();
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
    private static String lastKey;
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
    public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
    public static int     GM_ACCESSLEVEL;
    public static int     GM_MIN;
    public static int     GM_ALTG_MIN_LEVEL;
    public static int     GM_ANNOUNCE;
    public static int     GM_BAN;
    public static int     GM_BAN_CHAT;
    public static int     GM_CREATE_ITEM;
    public static int     GM_DELETE;
    public static int     GM_KICK;
    public static int     GM_MENU;
    public static int     GM_GODMODE;
    public static int     GM_CHAR_EDIT;
    public static int     GM_CHAR_EDIT_OTHER;
    public static int     GM_CHAR_VIEW;
    public static int     GM_NPC_EDIT;
    public static int     GM_NPC_VIEW;
    public static int     GM_TELEPORT;
    public static int     GM_TELEPORT_OTHER;
    public static int     GM_RESTART;
    public static int     GM_MONSTERRACE;
    public static int     GM_RIDER;
    public static int     GM_ESCAPE;
    public static int     GM_FIXED;
    public static int     GM_CREATE_NODES;
    public static int     GM_ENCHANT;
    public static int     GM_DOOR;
    public static int     GM_RES;
    public static int     GM_PEACEATTACK;
    public static int     GM_HEAL;
    public static int     GM_UNBLOCK;
    public static int     GM_CACHE;
    public static int     GM_TALK_BLOCK;
    public static int     GM_TEST;
    public static boolean GM_DISABLE_TRANSACTION;
    public static int     GM_TRANSACTION_MIN;
    public static int     GM_TRANSACTION_MAX;
    public static int     GM_CAN_GIVE_DAMAGE;
    public static int     GM_DONT_TAKE_EXPSP;
    public static int     GM_DONT_TAKE_AGGRO;
    public static int GM_REPAIR = 75;
    public static float   RATE_XP;
    public static float   RATE_SP;
    public static float   RATE_PARTY_XP;
    public static float   RATE_PARTY_SP;
    public static float   RATE_QUESTS_REWARD;
    public static float   RATE_DROP_ADENA;
    public static float   RATE_CONSUMABLE_COST;
    public static float   RATE_DROP_ITEMS;
	public static float RATE_DROP_STONE;
	public static float RATE_DROP_ITEMS_BY_RAID;
	public static float RATE_DROP_ITEMS_BY_GRAND;
    public static float   RATE_DROP_SPOIL;
    public static int   RATE_DROP_MANOR;
    public static float   RATE_DROP_QUEST;
    public static float   RATE_KARMA_EXP_LOST;
    public static float   RATE_SIEGE_GUARDS_PRICE;
    public static float   ALT_GAME_EXPONENT_XP;
    public static float   ALT_GAME_EXPONENT_SP;
    public static float   RATE_DROP_COMMON_HERBS;
    public static float   RATE_DROP_MP_HP_HERBS;
    public static float   RATE_DROP_GREATER_HERBS;
    public static float   RATE_DROP_SUPERIOR_HERBS;
    public static float   RATE_DROP_SPECIAL_HERBS;
    public static int   PLAYER_DROP_LIMIT;
    public static int   PLAYER_RATE_DROP;
    public static int   PLAYER_RATE_DROP_ITEM;
    public static int   PLAYER_RATE_DROP_EQUIP;
    public static int   PLAYER_RATE_DROP_EQUIP_WEAPON;
    public static float         PET_XP_RATE;
    public static int           PET_FOOD_RATE;
    public static float         SINEATER_XP_RATE;
    public static int   KARMA_DROP_LIMIT;
    public static int   KARMA_RATE_DROP;
    public static int   KARMA_RATE_DROP_ITEM;
    public static int   KARMA_RATE_DROP_EQUIP;
    public static int   KARMA_RATE_DROP_EQUIP_WEAPON;
    public static int     AUTODESTROY_ITEM_AFTER;
    public static int     HERB_AUTO_DESTROY_TIME;
    public static String  PROTECTED_ITEMS;
    public static List<Integer> LIST_PROTECTED_ITEMS = new FastList<Integer>();
    public static boolean     DESTROY_DROPPED_PLAYER_ITEM;
    public static boolean     DESTROY_EQUIPABLE_PLAYER_ITEM;
    public static boolean     SAVE_DROPPED_ITEM;
    public static boolean     EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
    public static int         SAVE_DROPPED_ITEM_INTERVAL;
    public static boolean     CLEAR_DROPPED_ITEM_TABLE;
    public static boolean PRECISE_DROP_CALCULATION;
    public static boolean MULTIPLE_ITEM_DROP;
    public static int     COORD_SYNCHRONIZE;
    public static int     DELETE_DAYS;
    public static File    DATAPACK_ROOT;
    public static int MAX_DRIFT_RANGE;
    public static boolean ALLOWFISHING;
    public static boolean ALLOW_MANOR;
    public static boolean JAIL_IS_PVP;
    public static boolean JAIL_DISABLE_CHAT;
    public static enum L2WalkerAllowed
    {
        True,
        False,
        GM
    }
    
    //Announces
    public static boolean ONLINE_ANNOUNE;
    public static int ONLINE_ANNOUNCE_DELAY;
    public static boolean SAVE_MAXONLINE_IN_DB;
    public static boolean ONLINE_SHOW_MAXONLINE;
    public static boolean ONLINE_SHOW_MAXONLINE_DATE;
    public static boolean ONLINE_SHOW_OFFLINE;
    public static boolean ONLINE_LOGIN_ONLINE;
    public static boolean ONLINE_LOGIN_MAX;
    public static boolean ONLINE_LOGIN_DATE;
    public static boolean ONLINE_LOGIN_OFFLINE;
    public static boolean AUTO_ANNOUNCE_ALLOW;
    public static int AUTO_ANNOUNCE_DELAY;
    
    public static L2WalkerAllowed ALLOW_L2WALKER_CLIENT;
    public static boolean         AUTOBAN_L2WALKER_ACC;
    public static int             L2WALKER_REVISION;
    public static int              FLOODPROTECTOR_INITIALSIZE;
    public static boolean         ALLOW_DISCARDITEM;
    public static boolean         ALLOW_FREIGHT;
    public static boolean         ALLOW_WAREHOUSE;
    public static boolean         WAREHOUSE_CACHE;
    public static int             WAREHOUSE_CACHE_TIME;
    public static boolean           ALLOW_WEAR;
    public static int               WEAR_DELAY;
    public static int               WEAR_PRICE;
    public static boolean           ALLOW_LOTTERY;
    public static boolean           ALLOW_RACE;
    public static boolean           ALLOW_WATER;
    public static boolean         ALLOW_RENTPET;
    public static boolean           ALLOW_BOAT;
    public static boolean           ALLOW_CURSED_WEAPONS;
    public static int WYVERN_SPEED;
    public static int STRIDER_SPEED;
    public static int MIN_PROTOCOL_REVISION;
    public static int MAX_PROTOCOL_REVISION;
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
    public static int     NAME_PAGE_SIZE_COMMUNITYBOARD;
    public static int     NAME_PER_ROW_COMMUNITYBOARD;
    public static int MAX_ITEM_IN_PACKET;
    public static boolean CHECK_KNOWN;
    public static int        GAME_SERVER_LOGIN_PORT;
    public static String     GAME_SERVER_LOGIN_HOST;
    public static String     INTERNAL_HOSTNAME;
    public static String     EXTERNAL_HOSTNAME;
    public static int        NEW_NODE_ID;
    public static int        SELECTED_NODE_ID;
    public static int        LINKED_NODE_ID;
    public static String     NEW_NODE_TYPE;
    public static String     VERSION_SERV;
    public static boolean SERVER_NEWS;
    public static boolean    FORCE_INVENTORY_UPDATE;
    public static boolean    ALLOW_GUARDS;
    public static boolean    ALLOW_CLASS_MASTERS;
    public static int        IP_UPDATE_TIME;
    public static int ZONE_TOWN;
    public static boolean IS_CRAFTING_ENABLED;
    public static int INVENTORY_MAXIMUM_NO_DWARF;
    public static int INVENTORY_MAXIMUM_DWARF;
    public static int INVENTORY_MAXIMUM_GM;
    public static int WAREHOUSE_SLOTS_NO_DWARF;
    public static int WAREHOUSE_SLOTS_DWARF;
    public static int WAREHOUSE_SLOTS_CLAN;
    public static int FREIGHT_SLOTS;
    public static int     KARMA_MIN_KARMA;
    public static int     KARMA_MAX_KARMA;
    public static int     KARMA_XP_DIVIDER;
    public static int     KARMA_LOST_BASE;
    public static boolean KARMA_DROP_GM;
    public static boolean KARMA_AWARD_PK_KILL;
    public static int     KARMA_PK_LIMIT;
    public static String        KARMA_NONDROPPABLE_PET_ITEMS;
    public static String        KARMA_NONDROPPABLE_ITEMS;
    public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS   = new FastList<Integer>();
    public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();
    public static String        NONDROPPABLE_ITEMS;
    public static List<Integer> LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();
    public static String        PET_RENT_NPC;
    public static List<Integer> LIST_PET_RENT_NPC   = new FastList<Integer>();
    public static int PVP_NORMAL_TIME;
    public static int PVP_PVP_TIME;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE; 
    public static boolean L2JMOD_ALLOW_WEDDING;
    public static int L2JMOD_WEDDING_PRICE;
    public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
    public static boolean L2JMOD_WEDDING_TELEPORT;
    public static int L2JMOD_WEDDING_TELEPORT_PRICE;
    public static int L2JMOD_WEDDING_TELEPORT_DURATION;
    public static boolean L2JMOD_WEDDING_SAMESEX;
    public static boolean L2JMOD_WEDDING_FORMALWEAR;
    public static int L2JMOD_WEDDING_DIVORCE_COSTS;
	public static boolean COLOR_WEDDING_NAME;
	public static int COLOR_WEDDING_NAMES;
	public static int COLOR_WEDDING_NAMES_GEY;
	public static int COLOR_WEDDING_NAMES_LIZ;
    public static String     CHECKIP = Config.EXTERNAL_HOSTNAME;
    public static enum IdFactoryType
    {
        Compaction,
        BitSet,
        Stack
    }
    public static IdFactoryType IDFACTORY_TYPE;
    public static boolean BAD_ID_CHECKING;
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
    //Offline system start;
    public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	
	//TODO Restore offliners
	 public static boolean ENCHANT_HERO_WEAPONS;
	public static FastMap<Integer,Integer> NORMAL_FULL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_FULL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_WEAPON_MAGE_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_WEAPON_MAGE_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_WEAPON_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_WEAPON_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_WEAPON_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_ARMOR_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer>NORMAL_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_FULL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_WEAPON_MAGE_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_WEAPON_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_WEAPON_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_WEAPON_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> NORMAL_JEWELRY_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> BLESS_JEWELRY_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static FastMap<Integer,Integer> CRYSTAL_JEWELRY_ENCHANT_LEVEL_DONATE = new FastMap<Integer,Integer>();
	  public static int ENCHANT_MAX_WEAPON_DONATE;
	  public static int ENCHANT_MAX_ARMOR_DONATE;
	  public static int ENCHANT_MAX_JEWELRY_DONATE;
	  public static String MAGE_WEAPON_ID;
	  public static List<Integer> MAGE_WEAPON_ID_LIST = new FastList<Integer>();
	  public static boolean ENABLE_DWARF_ENCHANT_BONUS;
	  public static int DWARF_ENCHANT_MIN_LEVEL;
	  public static int DWARF_ENCHANT_BONUS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	//Offline system end;
    public static ObjectMapType   MAP_TYPE;
    public static ObjectSetType   SET_TYPE;
    public static boolean EFFECT_CANCELING;
    public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean ENABLE_MODIFY_ENCHANT_CHANCE_WEAPON;
	public static Map<Integer, Integer> ENCHANT_CHANCE_LIST_WEAPON;
	public static boolean ENABLE_MODIFY_ENCHANT_CHANCE_ARMOR;
	public static Map<Integer, Integer> ENCHANT_CHANCE_LIST_ARMOR;
	public static boolean ENABLE_MODIFY_ENCHANT_CHANCE_JEWELRY;
	public static Map<Integer, Integer> ENCHANT_CHANCE_LIST_JEWELRY;
	public static boolean ENABLE_MODIFY_ENCHANT_MULTISELL;
	public static Map<Integer, Integer> ENCHANT_MULTISELL_LIST;
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_CHANCE_WEAPON_1015;
	public static int ENCHANT_CHANCE_ARMOR_1015;
	public static int ENCHANT_CHANCE_JEWELRY_1015;
	public static int ENCHANT_CHANCE_WEAPON_16;
	public static int ENCHANT_CHANCE_ARMOR_16;
	public static int ENCHANT_CHANCE_JEWELRY_16;
	public static int BLESSED_CHANCE_WEAPON;
	public static int BLESSED_CHANCE_ARMOR;
	public static int BLESSED_CHANCE_JEWELRY;
	public static int BLESSED_CHANCE_WEAPON_1015;
	public static int BLESSED_CHANCE_ARMOR_1015;
	public static int BLESSED_CHANCE_JEWELRY_1015;
	public static int BLESSED_CHANCE_WEAPON_16;
	public static int BLESSED_CHANCE_ARMOR_16;
	public static int BLESSED_CHANCE_JEWELRY_16;
	public static int CRYSTAL_CHANCE_WEAPON;
	public static int CRYSTAL_CHANCE_ARMOR;
	public static int CRYSTAL_CHANCE_JEWELRY;
	public static int CRYSTAL_CHANCE_WEAPON_1015;
	public static int CRYSTAL_CHANCE_ARMOR_1015;
	public static int CRYSTAL_CHANCE_JEWELRY_1015;
	public static int CRYSTAL_CHANCE_WEAPON_16;
	public static int CRYSTAL_CHANCE_ARMOR_16;
	public static int CRYSTAL_CHANCE_JEWELRY_16;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
    public static double  HP_REGEN_MULTIPLIER;
    public static double  MP_REGEN_MULTIPLIER;
    public static double  CP_REGEN_MULTIPLIER;
    public static double   RAID_HP_REGEN_MULTIPLIER;
    public static double   RAID_MP_REGEN_MULTIPLIER;
    public static double   RAID_DEFENCE_MULTIPLIER;
    public static double   RAID_MINION_RESPAWN_TIMER;
    public static float   RAID_MIN_RESPAWN_MULTIPLIER;
    public static float   RAID_MAX_RESPAWN_MULTIPLIER;
    public static int STARTING_ADENA;
    public static boolean NUM_JVM1;
    public static boolean DEEPBLUE_DROP_RULES;
    public static int     UNSTUCK_INTERVAL;
    public static boolean IS_TELNET_ENABLED;
    public static int   DEATH_PENALTY_CHANCE;
    public static int   PLAYER_SPAWN_PROTECTION;
    public static int   PLAYER_FAKEDEATH_UP_PROTECTION;
    public static String  PARTY_XP_CUTOFF_METHOD;
    public static int PARTY_XP_CUTOFF_LEVEL;
    public static double  PARTY_XP_CUTOFF_PERCENT;
    public static double  RESPAWN_RESTORE_CP;
    public static double  RESPAWN_RESTORE_HP;
    public static double  RESPAWN_RESTORE_MP;
    public static boolean RESPAWN_RANDOM_ENABLED;
    public static int RESPAWN_RANDOM_MAX_OFFSET;
    public static int  MAX_PVTSTORE_SLOTS_DWARF;
    public static int  MAX_PVTSTORE_SLOTS_OTHER;
    public static boolean STORE_SKILL_COOLTIME;
    public static boolean SHOW_LICENCE;
    public static int DEFAULT_PUNISH;
    public static int DEFAULT_PUNISH_PARAM;
    public static boolean ACCEPT_NEW_GAMESERVER;
    public static int SERVER_ID;
    public static byte[] HEX_ID;
    public static boolean ACCEPT_ALTERNATE_ID;
    public static int REQUEST_ID;
    public static boolean RESERVE_HOST_ON_LOGIN = false;
    public static boolean ANNOUNCE_MAMMON_SPAWN;
    public static boolean LAZY_CACHE;
    public static boolean GM_HERO_AURA;
    public static boolean GM_STARTUP_INVULNERABLE;
    public static boolean GM_STARTUP_INVISIBLE;
    public static boolean GM_STARTUP_SILENCE;
    public static boolean GM_STARTUP_AUTO_LIST;
    public static String GM_ADMIN_MENU_STYLE;
    public static boolean PETITIONING_ALLOWED;
    public static int     MAX_PETITIONS_PER_PLAYER;
    public static int     MAX_PETITIONS_PENDING;
    public static boolean BYPASS_VALIDATION;
    public static boolean ONLY_GM_ITEMS_FREE;
    public static boolean GMAUDIT;
    public static boolean AUTO_CREATE_ACCOUNTS;
    
    public static int BRUT_AVG_TIME;
    public static int BRUT_LOGON_ATTEMPTS;
    public static int BRUT_BAN_IP_TIME;
    
    public static boolean ENABLE_DDOS_PROTECTION_SYSTEM;
    public static boolean ENABLE_DEBUG_DDOS_PROTECTION_SYSTEM;
    public static String IPTABLES_COMMAND;
    
	public static boolean ENABLE_PACKET_PROTECTION;
	public static int MAX_UNKNOWN_PACKETS;
	public static int UNKNOWN_PACKETS_PUNISHMENT;
    public static boolean FLOOD_PROTECTION;
    public static int     FAST_CONNECTION_LIMIT;
    public static int     NORMAL_CONNECTION_TIME;
    public static int     FAST_CONNECTION_TIME;
    public static int     MAX_CONNECTION_PER_IP;
    public static boolean GAMEGUARD_ENFORCE;
    public static boolean GAMEGUARD_PROHIBITACTION;
    public static int DWARF_RECIPE_LIMIT;
    public static int COMMON_RECIPE_LIMIT;
    public static boolean GRIDS_ALWAYS_ON;
    public static int GRID_NEIGHBOR_TURNON_TIME;
    public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static int CH_RATE;
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
    public static byte BUFFS_MAX_AMOUNT;
    public static byte DEBUFFS_MAX_AMOUNT;
	public static boolean PATH_SMOOTHING;
    public static byte PATH_SMOOTHING_FACTOR;
    
    public static int CHANCE_LS_SKILL;

	public static boolean SAVE_BUFF_PROFILES;

	public static boolean NOBLESSE_BY_KILLING_RB;

	public static int NAKRUTKA_ONLINE;

	public static boolean L2TOP_ENABLED; 
 	public static int L2TOP_POLLINTERVAL; 
 	public static boolean L2TOP_IGNOREFIRST; 
 	public static int L2TOP_MIN; 
 	public static int L2TOP_MAX; 
 	public static int L2TOP_ITEM; 
 	public static String L2TOP_MESSAGE; 
 	public static String L2TOP_URL; 
 	public static String L2TOP_PREFIX; 

    public static byte NEW_SUBCLASS_LVL;
    public static byte MAX_CHAR_LEVEL;
    public static boolean BANKING_SYSTEM_ENABLED;
    public static int BANKING_SYSTEM_1ITEMID;
    public static int BANKING_SYSTEM_1ITEMCOUNT;
    public static int BANKING_SYSTEM_2ITEMID;
    public static int BANKING_SYSTEM_2ITEMCOUNT;
    public static String BANKING_SYSTEM_1ITEMNAME;
    public static String BANKING_SYSTEM_2ITEMNAME;
    
    public static double M_CRIT_DAMAGE;
	public static double M_CRIT_CHANCE;
    
	public static int AQ_RESP_TIME;
	public static int AQ_RND_RESP_TIME;
	public static int AQ_ROYAL_RESP_TIME;
	public static int AQ_NURSE_RESP_TIME;
	
	public static int ZAKEN_RESP_TIME;
	public static int ZAKEN_RND_RESP_TIME;
	
	public static boolean SHOW_WELCOME_PM;
	public static int MP_RESTORE;
	// buff amount
	public static float SPIRIT_TIME_MULTIPLIER;
	public static float BUFF_TIME_MULTIPLIER;   
	public static float DANCE_TIME_MULTIPLIER;
	public static float HDAMAGE_FIGHTER;

	public static float HDAMAGE_WARRIOR;
	public static float HDAMAGE_KNIGHT;	
	public static float HDAMAGE_ROGUE;	
	
	public static float HDAMAGE_GLADIATOR;
	public static float HDAMAGE_WARLORD;
	public static float HDAMAGE_PALADIN;
	public static float HDAMAGE_DARK_AVENGER;
	public static float HDAMAGE_TREASURE_HUNTER;
	public static float HDAMAGE_HAWKEYE;

	public static float HDAMAGE_DUELIST;
	public static float HDAMAGE_DREADNOUGHT;
	public static float HDAMAGE_PHOENIX_KNIGHT;
	public static float HDAMAGE_HELL_KNIGHT;
	public static float HDAMAGE_SAGITTARIUS;
	public static float HDAMAGE_ADVENTURER;

	public static float HDAMAGE_MAGE;

	public static float HDAMAGE_WIZARD;
	public static float HDAMAGE_CLERIC;

	public static float HDAMAGE_SORCEROR;
	public static float HDAMAGE_NECROMANCER;
	public static float HDAMAGE_WARLOCK;
	public static float HDAMAGE_BISHOP;
	public static float HDAMAGE_PROPHET;

	public static float HDAMAGE_ARCHMAGE;
	public static float HDAMAGE_SOULTLAKER;
	public static float HDAMAGE_ARKANALORD;
	public static float HDAMAGE_CARDINAL;
	public static float HDAMAGE_HIEROPHANT;
	

	public static float EDamage_Fighter;
	public static float EDamage_Knight;
	public static float EDamage_Scout;
	public static float EDamage_TempleKnight;
	public static float EDamage_SwordSinger;
	public static float EDamage_PlainsWalker;
	public static float EDamage_SilverRanger;

	public static float EDAMAGE_EVATEMPLAR;
	public static float EDAMAGE_SWORDMUSE;
	public static float EDAMAGE_WINDRIDER;
	public static float EDAMAGE_MOONLIGHT_SENTINEL;
	
	
	public static float EDamage_Mage;
	public static float EDamage_Wizard;
	public static float EDamage_SpellSinger;
	public static float EDamage_ElementalSummoner;
	public static float EDamage_Oracle;
	public static float EDamage_Elder;
	
	public static float EDAMAGE_MYSTIC_MUSE;
	public static float EDAMAGE_ELEMENTAL_MASTER;
	public static float EDAMAGE_EVA_SEINT;
	

	
	public static float DEDamage_Fighter;
	public static float DEDamage_PaulusKnight;
	public static float DEDamage_ShillienKnight;
	public static float DEDamage_BladeDancer;
	public static float DEDamage_Assassin;
	public static float DEDamage_AbyssWalker;
	public static float DEDamage_PhantomRanger;
	
	
	public static float DEDAMAGE_SHILLEN_TEMPLAR;
	public static float DEDAMAGE_SPECTRAL_DANCER;
	public static float DEDAMAGE_GHOST_HUNTER;
	public static float DEDAMAGE_GHOSTSENTINEL;
	
	public static float DEDamage_Mage;
	public static float DEDamage_DarkWizard;
	public static float DEDamage_Spellhowler;
	public static float DEDamage_PhantomSummoner;
	public static float DEDamage_ShillienOracle;
	public static float DEDamage_ShillienElder;
	
	public static float DEDAMAGE_STORMSCREAMER;
	public static float DEDAMAGE_SPECTRALMASTER;
	public static float DEDAMAGE_SHILLENSEINT;
	
	public static float ODamage_Fighter;
	public static float ODamage_Raider;
	public static float ODamage_Destroyer;
	public static float ODamage_Monk;
	public static float ODamage_Tyrant;
	
	
	public static float ODAMAGE_TITAN;
	public static float ODAMAGE_GRAND_KHAUATARI;
	
	public static float ODamage_Mage;
	public static float ODamage_Shaman;
	public static float ODamage_Overlord;
	public static float ODamage_Warcryer;
	
	public static float ODAMAGE_DOMINATOR;
	public static float ODAMAGE_DOOMCRAYER;

	public static float DDamage_Fighter;
	public static float DDamage_Scavenger;
	public static float DDamage_BountyHunter;
	public static float DDamage_Artisan;
	public static float DDamage_Warsmith;
	
	public static float DDAMAGE_FORTUNESEEKER;
	public static float DDAMAGE_MAESTRO;
	
	public static final String BALANCE_FILE = 									 		"./config/balance.ini";
	public static final String CONFIGURATION_FILE = 									"./config/gameserver.ini";
	public static final String OPTIONS_FILE = 											"./config/options.ini";
	public static final String BOSSES_FILE = 											"./config/bosses.ini";
	public static final String LOGIN_CONFIGURATION_FILE = 								"./config/loginserver.ini";
	public static final String ID_CONFIG_FILE = 										"./config/idfactory.ini";
	public static final String OTHER_CONFIG_FILE = 										"./config/other.ini";
	public static final String RATES_CONFIG_FILE = 										"./config/rates.ini";
	public static final String ALT_SETTINGS_FILE = 										"./config/altsettings.ini";
	public static final String PVP_CONFIG_FILE = 										"./config/pvp.ini";
	public static final String GM_ACCESS_FILE = 										"./config/GMAccess.ini";
	public static final String TELNET_FILE = 											"./config/telnet.ini";
	public static final String SIEGE_CONFIGURATION_FILE = 								"./config/siege.ini";
	public static final String BANNED_IP_XML = 											"./config/banned.xml";
	public static final String HEXID_FILE = 											"./config/hexid.txt";
	public static final String COMMAND_PRIVILEGES_FILE = 								"./config/command-privileges.ini";
	public static final String CLANHALL_CONFIG_FILE = 									"./config/clanhall.ini";
	public static final String PHYSICS_CONFIG_FILE = 									"./config/physics.ini";
	public static final String DEVELOPER_CONFIG_FILE = 									"./config/developer.ini";
	public static final String ADDITIONS_CONFIG_FILE = 									"./config/additions.ini";
	public static final String EON_CONFIG_FILE = 										"./config/custom.ini";
	public static final String GEODATA_CONFIG_FILE = 									"./config/geodata.ini";
	public static final String CHAT_CONFIG_FILE = 										"./config/chat.ini";
	public static final String ENCHANT_CONFIG_FILE = 									"./config/enchant.ini";
	public static final String PROTECT_CONFIG_FILE = 									"./config/protection.ini";
	public static final String CASTLE_CONFIG_FILE = 									"./config/castle.ini";
	public static final String OFFLINE_FILE = 											"./config/offline.ini";
	public static final String PREM_FILE = 												"./config/premium.ini";
	public static final String DON_FILE = 												"./config/donate.ini";
	public static final String WEB_FILE = 												"./config/webserver.ini";
	public static final String ELITE_CH = 												"./config/eliteclanhall.ini";
	public static final String L2TOP_FILE = 											"./config/l2top.ini";

	public static int BACKSTAB_CHANCE;

	public static String GM_MASTERS;
	public static List<Integer> LIST_GM_MASTERS = new FastList<Integer>();

	public static int PTS_EMULATION_LOAD_NPC_SERVER_TIME;

	public static int REBIRTH_ITEM;
	public static String FORTSIEGE_CONFIGURATION_FILE = 								"./config/fort.ini";
	public static String ANNOUNCE_FILE = 												"./config/announce.ini";
	public static String BUFF_FILE = 												    "./config/buff.ini";
	public static String VERSION = 														"./config/l2j-version.ini";

	public static String TEST_CONFIG_FILE = 											"./config/test.ini";
	public static boolean ENTER_WORLD_FIX;
	

	
    public static void load()
    {
        if(Server.serverMode == Server.MODE_GAMESERVER)
        {
        	_log.info("./config/gameserver.ini");
        	_log.info("./config/options.ini");
        	_log.info("./config/bosses.ini");
        	_log.info("./config/loginserver.ini");
        	_log.info("./config/idfactory.ini");
        	_log.info(OTHER_CONFIG_FILE);
        	_log.info(RATES_CONFIG_FILE);
        	_log.info(ALT_SETTINGS_FILE);
        	_log.info(PVP_CONFIG_FILE);
        	_log.info(GM_ACCESS_FILE);
        	_log.info(TELNET_FILE);
        	_log.info(SIEGE_CONFIGURATION_FILE);
        	_log.info(BANNED_IP_XML);
        	_log.info(HEXID_FILE);
        	_log.info(COMMAND_PRIVILEGES_FILE);
        	_log.info(CLANHALL_CONFIG_FILE);
        	_log.info(PHYSICS_CONFIG_FILE);
        	_log.info(DEVELOPER_CONFIG_FILE);
        	_log.info(ADDITIONS_CONFIG_FILE);
        	_log.info(EON_CONFIG_FILE);
        	_log.info(GEODATA_CONFIG_FILE);
        	_log.info(CHAT_CONFIG_FILE);
        	_log.info(ENCHANT_CONFIG_FILE);
        	_log.info(PROTECT_CONFIG_FILE);
        	_log.info(CASTLE_CONFIG_FILE);
        	_log.info(OFFLINE_FILE);
        	_log.info(PREM_FILE);
        	_log.info(DON_FILE);
        	_log.info(WEB_FILE);
        	_log.info(ELITE_CH);
        	_log.info(L2TOP_FILE);
        	_log.info(FORTSIEGE_CONFIGURATION_FILE);
        	_log.info(ANNOUNCE_FILE);
        	_log.info(BUFF_FILE);
        	_log.info(BALANCE_FILE);
        	_log.info("./config/chatfilter/chatfilter.txt");
        	_log.info("./config/chatfilter/chatfilter-ex.txt");
        	_log.info(TEST_CONFIG_FILE);
        	
        	try 
        	{
        		Properties testSettings    = new Properties();
        		InputStream is             = new FileInputStream(new File(TEST_CONFIG_FILE));
        		testSettings.load(is);
        		is.close();
        		ENTER_WORLD_FIX = Boolean.valueOf(testSettings.getProperty("enterWorldFix", "false"));
        		
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        		throw new Error("Failed to Load "+TEST_CONFIG_FILE+" File.");	
        	}
			try {
                Properties physicsSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(PHYSICS_CONFIG_FILE));
                physicsSettings.load(is);
				is.close();
				ALT_DIFF_MAGIC = Boolean.valueOf(physicsSettings.getProperty("AltDiffMagic", "false"));
				ALT_CRIT_DAMAGE = Float.parseFloat(physicsSettings.getProperty("FighterCritDamage", "1.0"));
				M_CRIT_DAMAGE = Double.parseDouble(physicsSettings.getProperty("MagicCritDamageMultiple", "4.0"));
				M_CRIT_CHANCE = Float.parseFloat(physicsSettings.getProperty("MagicCritChanceMultiple", "1"));
				BLOW_DAMAGE_HEAVY = Float.parseFloat(physicsSettings.getProperty("BlowDamageHeavy", "1.5"));
				BLOW_DAMAGE_LIGHT = Float.parseFloat(physicsSettings.getProperty("BlowDamageLight", "1.2"));
				BLOW_DAMAGE_ROBE = Float.parseFloat(physicsSettings.getProperty("BlowDamageRobe", "1.0"));
				ALT_MAGES_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltPDamageMages", "1.00"));
				ALT_MAGES_MAGICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltMDamageMages", "1.00"));
				ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltPDamageFighters", "1.00"));
				ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltMDamageFighters", "1.00"));
				ALT_PETS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltPDamagePets", "1.00"));
				ALT_PETS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltMDamagePets", "1.00"));
				ALT_NPC_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltPDamageNpc", "1.00"));
				ALT_NPC_MAGICAL_DAMAGE_MULTI = Float.parseFloat(physicsSettings.getProperty("AltMDamageNpc", "1.00"));
				ALT_PCRITICAL_CAP = Integer.parseInt(physicsSettings.getProperty("AltPCriticalCap", "500"));
		    	ALT_MCRITICAL_CAP = Integer.parseInt(physicsSettings.getProperty("AltMCriticalCap", "300"));
			    MAX_PATK_SPEED = Integer.parseInt(physicsSettings.getProperty("MaxPAtkSpeed", "0"));
			    MAX_MATK_SPEED = Integer.parseInt(physicsSettings.getProperty("MaxMAtkSpeed", "0"));
				FRONT_CHANCE = Integer.parseInt(physicsSettings.getProperty("FrontSuccessChance", "15"));
			 	SIDE_CHANCE = Integer.parseInt(physicsSettings.getProperty("SideSuccessChance", "45"));
			    BEHIND_CHANCE = Integer.parseInt(physicsSettings.getProperty("BehindSuccessChance", "85"));
			    BACKSTAB_CHANCE = Integer.parseInt(physicsSettings.getProperty("BackstabChance", "70"));
				ALT_MINIMUM_FALL_HEIGHT = Integer.parseInt(physicsSettings.getProperty("MinimumFallHeight", "400"));
				HP_REGEN_MULTIPLIER = Double.parseDouble(physicsSettings.getProperty("HpRegenMultiplier", "100")) /100;
                MP_REGEN_MULTIPLIER = Double.parseDouble(physicsSettings.getProperty("MpRegenMultiplier", "100")) /100;
                CP_REGEN_MULTIPLIER = Double.parseDouble(physicsSettings.getProperty("CpRegenMultiplier", "100")) /100;

			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+PHYSICS_CONFIG_FILE+" File.");
            }
			try {
                Properties BalanceSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(BALANCE_FILE));
                BalanceSettings.load(is);
				is.close();
				

				 HDAMAGE_FIGHTER 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_fighter", "1.00"));
					 
				 HDAMAGE_WARRIOR 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_warrior", "1.00"));
				 HDAMAGE_KNIGHT 				= Float.parseFloat(BalanceSettings.getProperty("HDamage_knight", "1.00"));			 
				 HDAMAGE_ROGUE 				= Float.parseFloat(BalanceSettings.getProperty("HDamage_rogue", "1.00"));			 
				 
				 HDAMAGE_GLADIATOR 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_gladiator", "1.00"));
				 HDAMAGE_WARLORD 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_warlord", "1.00"));
				 HDAMAGE_PALADIN 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_paladin", "1.00"));
				 HDAMAGE_DARK_AVENGER 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_darkAvenger", "1.00"));
				 HDAMAGE_TREASURE_HUNTER 	= Float.parseFloat(BalanceSettings.getProperty("HDamage_treasureHunter", "1.00"));
				 HDAMAGE_HAWKEYE 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_hawkeye", "1.00"));
				
				 HDAMAGE_DUELIST 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_Duelist", "1.00"));
				 HDAMAGE_DREADNOUGHT 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_Dreadnought", "1.00"));
				 HDAMAGE_PHOENIX_KNIGHT 	= Float.parseFloat(BalanceSettings.getProperty("HDamage_PhoenixKnight", "1.00"));
				 HDAMAGE_HELL_KNIGHT 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_HellKnight", "1.00"));
				 HDAMAGE_SAGITTARIUS 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_Sagittarius", "1.00"));
				 HDAMAGE_ADVENTURER 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_Adventurer", "1.00"));
				 //======================================================================================================
				 
				
				 HDAMAGE_MAGE				= Float.parseFloat(BalanceSettings.getProperty("HDamage_mage", "1.00"));
				 	
				 HDAMAGE_WIZARD				= Float.parseFloat(BalanceSettings.getProperty("HDamage_wizard", "1.00"));
				 HDAMAGE_CLERIC			    = Float.parseFloat(BalanceSettings.getProperty("HDamage_cleric", "1.00"));
					
				 HDAMAGE_SORCEROR			= Float.parseFloat(BalanceSettings.getProperty("HDamage_sorceror", "1.00"));
				 HDAMAGE_NECROMANCER		= Float.parseFloat(BalanceSettings.getProperty("HDamage_necromancer", "1.00"));
				 HDAMAGE_WARLOCK			= Float.parseFloat(BalanceSettings.getProperty("HDamage_warlock", "1.00"));
				 HDAMAGE_BISHOP				= Float.parseFloat(BalanceSettings.getProperty("HDamage_bishop", "1.00"));
				 HDAMAGE_PROPHET			= Float.parseFloat(BalanceSettings.getProperty("HDamage_prophet", "1.00"));
				
				 HDAMAGE_ARCHMAGE 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_Archmage", "1.00"));
				 HDAMAGE_SOULTLAKER 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_Soultaker", "1.00"));
				 HDAMAGE_ARKANALORD 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_ArcanaLord", "1.00"));
				 HDAMAGE_CARDINAL 			= Float.parseFloat(BalanceSettings.getProperty("HDamage_Cardinal", "1.00"));
				 HDAMAGE_HIEROPHANT 		= Float.parseFloat(BalanceSettings.getProperty("HDamage_Hierophant", "1.00"));
				 
				 
				 
				 EDamage_Fighter			= Float.parseFloat(BalanceSettings.getProperty("EDamage_Fighter", "1.00"));
				 EDamage_Knight				= Float.parseFloat(BalanceSettings.getProperty("EDamage_Knight", "1.00"));
				 EDamage_Scout				= Float.parseFloat(BalanceSettings.getProperty("EDamage_Scout", "1.00"));
				 EDamage_TempleKnight		= Float.parseFloat(BalanceSettings.getProperty("EDamage_TempleKnight", "1.00"));
				 EDamage_SwordSinger		= Float.parseFloat(BalanceSettings.getProperty("EDamage_SwordSinger", "1.00"));
				 EDamage_PlainsWalker		= Float.parseFloat(BalanceSettings.getProperty("EDamage_PlainsWalker", "1.00"));
				 EDamage_SilverRanger		= Float.parseFloat(BalanceSettings.getProperty("EDamage_SilverRanger", "1.00"));
				 
				 
				 EDAMAGE_EVATEMPLAR 		= Float.parseFloat(BalanceSettings.getProperty("EDamage_EvaTemplar", "1.00"));
				 EDAMAGE_SWORDMUSE 			= Float.parseFloat(BalanceSettings.getProperty("EDamage_SwordMuse", "1.00"));
				 EDAMAGE_WINDRIDER 			= Float.parseFloat(BalanceSettings.getProperty("EDamage_WindRider", "1.00"));
				 EDAMAGE_MOONLIGHT_SENTINEL = Float.parseFloat(BalanceSettings.getProperty("EDamage_MoonlightSentinel", "1.00"));
				 
				 EDamage_Mage				= Float.parseFloat(BalanceSettings.getProperty("EDamage_Mage", "1.00"));
				 EDamage_Wizard				= Float.parseFloat(BalanceSettings.getProperty("EDamage_Wizard", "1.00"));
				 EDamage_SpellSinger		= Float.parseFloat(BalanceSettings.getProperty("EDamage_SpellSinger", "1.00"));
				 EDamage_ElementalSummoner	= Float.parseFloat(BalanceSettings.getProperty("EDamage_ElementalSummoner", "1.00"));
				 EDamage_Oracle				= Float.parseFloat(BalanceSettings.getProperty("EDamage_Oracle", "1.00"));
				 EDamage_Elder				= Float.parseFloat(BalanceSettings.getProperty("EDamage_Elder", "1.00"));
				 
				 
				 EDAMAGE_MYSTIC_MUSE 		= Float.parseFloat(BalanceSettings.getProperty("EDamage_MysticMuse", "1.00"));
				 EDAMAGE_ELEMENTAL_MASTER 	= Float.parseFloat(BalanceSettings.getProperty("EDamage_ElementalMaster", "1.00"));
				 EDAMAGE_EVA_SEINT 			= Float.parseFloat(BalanceSettings.getProperty("EDamage_EvaSaint", "1.00"));
				 
				 
				 DEDamage_Fighter			= Float.parseFloat(BalanceSettings.getProperty("DEDamage_Fighter", "1.00"));
				 DEDamage_PaulusKnight		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_PaulusKnight", "1.00"));
				 DEDamage_ShillienKnight	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_ShillienKnight", "1.00"));
				 DEDamage_BladeDancer		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_BladeDancer", "1.00"));
				 DEDamage_Assassin			= Float.parseFloat(BalanceSettings.getProperty("DEDamage_Assassin", "1.00"));
				 DEDamage_AbyssWalker		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_AbyssWalker", "1.00"));
				 DEDamage_PhantomRanger		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_PhantomRanger", "1.00"));
				 
				 DEDAMAGE_SHILLEN_TEMPLAR 	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_ShillienTemplar", "1.00"));
				 DEDAMAGE_SPECTRAL_DANCER 	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_SpectralDancer", "1.00"));
				 DEDAMAGE_GHOST_HUNTER 		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_GhostHunter", "1.00"));
				 DEDAMAGE_GHOSTSENTINEL 	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_GhostSentinel", "1.00"));
				 
				 
				 DEDamage_Mage				= Float.parseFloat(BalanceSettings.getProperty("DEDamage_Mage", "1.00"));
				 DEDamage_DarkWizard		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_DarkWizard", "1.00"));
				 DEDamage_Spellhowler		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_Spellhowler", "1.00"));
				 DEDamage_PhantomSummoner	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_PhantomSummoner", "1.00"));
				 DEDamage_ShillienOracle	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_ShillienOracle", "1.00"));
				 DEDamage_ShillienElder		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_ShillienElder", "1.00"));
				 
				 DEDAMAGE_STORMSCREAMER 	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_StormScreamer", "1.00"));
				 DEDAMAGE_SPECTRALMASTER 	= Float.parseFloat(BalanceSettings.getProperty("DEDamage_SpectralMaster", "1.00"));
				 DEDAMAGE_SHILLENSEINT 		= Float.parseFloat(BalanceSettings.getProperty("DEDamage_ShillienSaint", "1.00"));
				 
					
				 ODamage_Fighter			= Float.parseFloat(BalanceSettings.getProperty("ODamage_Fighter", "1.00"));
				 ODamage_Raider				= Float.parseFloat(BalanceSettings.getProperty("ODamage_Raider", "1.00"));
				 ODamage_Destroyer			= Float.parseFloat(BalanceSettings.getProperty("ODamage_Destroyer", "1.00"));
				 ODamage_Monk				= Float.parseFloat(BalanceSettings.getProperty("ODamage_Monk", "1.00"));
				 ODamage_Tyrant				= Float.parseFloat(BalanceSettings.getProperty("ODamage_Tyrant", "1.00"));
				 ODAMAGE_TITAN 				= Float.parseFloat(BalanceSettings.getProperty("ODamage_Titan", "1.00"));
				 ODAMAGE_GRAND_KHAUATARI 	= Float.parseFloat(BalanceSettings.getProperty("ODamage_GrandKhauatari", "1.00"));
				 
				 
				 ODamage_Mage				= Float.parseFloat(BalanceSettings.getProperty("ODamage_Mage", "1.00"));
				 ODamage_Shaman				= Float.parseFloat(BalanceSettings.getProperty("ODamage_Shaman", "1.00"));
				 ODamage_Overlord			= Float.parseFloat(BalanceSettings.getProperty("ODamage_Overlord", "1.00"));
				 ODamage_Warcryer			= Float.parseFloat(BalanceSettings.getProperty("ODamage_Warcryer", "1.00"));
				 
				 ODAMAGE_DOMINATOR 			= Float.parseFloat(BalanceSettings.getProperty("ODamage_Dominator", "1.00"));
				 ODAMAGE_DOOMCRAYER 		= Float.parseFloat(BalanceSettings.getProperty("ODamage_Doomcryer", "1.00"));
				 
					
				 DDamage_Fighter			= Float.parseFloat(BalanceSettings.getProperty("DDamage_Fighter", "1.00"));
				 DDamage_Scavenger			= Float.parseFloat(BalanceSettings.getProperty("DDamage_Scavenger", "1.00"));
				 DDamage_BountyHunter		= Float.parseFloat(BalanceSettings.getProperty("DDamage_BountyHunter", "1.00"));
				 DDamage_Artisan			= Float.parseFloat(BalanceSettings.getProperty("DDamage_Artisan", "1.00"));
				 DDamage_Warsmith			= Float.parseFloat(BalanceSettings.getProperty("DDamage_Warsmith", "1.00"));
				 
				 DDAMAGE_FORTUNESEEKER 		= Float.parseFloat(BalanceSettings.getProperty("DDamage_FortuneSeeker", "1.00"));
				 DDAMAGE_MAESTRO 			= Float.parseFloat(BalanceSettings.getProperty("DDamage_Maestro", "1.00"));
			

			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+BALANCE_FILE+" File.");
            }
			try {
                Properties version    = new Properties();
                InputStream is               = new FileInputStream(new File(VERSION));
                version.load(is);
                VERSION_SERV = version.getProperty("version","version");
				

			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+PHYSICS_CONFIG_FILE+" File.");
            }

			try {
                Properties bossesSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(BOSSES_FILE));
                bossesSettings.load(is);
				is.close();
	            
				RB_HEAL = Boolean.parseBoolean(bossesSettings.getProperty("CanHealRB", "true"));
				GB_LOADER = Boolean.parseBoolean(bossesSettings.getProperty("LoadAiGb","True"));
				NOBLESSE_BY_KILLING_RB = Boolean.parseBoolean(bossesSettings.getProperty("NoblessByKillingBarakiel", "false"));
				
				AQ_RESP_TIME = Integer.parseInt(bossesSettings.getProperty("AQRespTime", "20"));
				AQ_RND_RESP_TIME = Integer.parseInt(bossesSettings.getProperty("AQRndRespTime", "8"));
				AQ_ROYAL_RESP_TIME = Integer.parseInt(bossesSettings.getProperty("AQRoyalRespTime", "120"));
				AQ_NURSE_RESP_TIME = Integer.parseInt(bossesSettings.getProperty("AQNurseRespTime", "60"));
						
				ZAKEN_RESP_TIME = Integer.parseInt(bossesSettings.getProperty("ZakenRespTime", "40"));
			    ZAKEN_RND_RESP_TIME = Integer.parseInt(bossesSettings.getProperty("ZakenRndRespTime", "8"));
				
				ANTHARAS_WAIT_TIME = Integer.parseInt(bossesSettings.getProperty("AntharasWaitTime", "30"));
				if (ANTHARAS_WAIT_TIME < 3 || ANTHARAS_WAIT_TIME > 60)
					ANTHARAS_WAIT_TIME = 20;
				ANTHARAS_WAIT_TIME = ANTHARAS_WAIT_TIME * 60000;
				
				ANTHARAS_INTERVAL_SPAWN = Integer.parseInt(bossesSettings.getProperty("IntervalOfAntharasSpawn", "192"));
				if (ANTHARAS_INTERVAL_SPAWN < 1 || ANTHARAS_INTERVAL_SPAWN > 480)
					ANTHARAS_INTERVAL_SPAWN = 192;
				ANTHARAS_INTERVAL_SPAWN = ANTHARAS_INTERVAL_SPAWN * 3600000;
				
				ANTHARAS_RANDOM_SPAWN = Integer.parseInt(bossesSettings.getProperty("RandomOfAntharasSpawn", "72"));
				if (ANTHARAS_RANDOM_SPAWN < 1 || ANTHARAS_RANDOM_SPAWN > 192)
					ANTHARAS_RANDOM_SPAWN = 72;
				ANTHARAS_RANDOM_SPAWN = ANTHARAS_RANDOM_SPAWN * 3600000;

	            TIMELIMITOFINVADE = Integer.parseInt(bossesSettings.getProperty("TimeLimitOfInvade", "1800000"));
				FWS_ENABLESINGLEPLAYER = Boolean.parseBoolean(bossesSettings.getProperty("EnableSinglePlayer", "False"));
                FWS_FIXINTERVALOFSAILRENSPAWN = Integer.parseInt(bossesSettings.getProperty("FixIntervalOfSailrenSpawn", "1440"));
                if(FWS_FIXINTERVALOFSAILRENSPAWN < 5 || FWS_FIXINTERVALOFSAILRENSPAWN > 2880) FWS_FIXINTERVALOFSAILRENSPAWN = 1440;
                FWS_FIXINTERVALOFSAILRENSPAWN = FWS_FIXINTERVALOFSAILRENSPAWN * 60000;
                FWS_RANDOMINTERVALOFSAILRENSPAWN = Integer.parseInt(bossesSettings.getProperty("RandomIntervalOfSailrenSpawn", "1440"));
                if(FWS_RANDOMINTERVALOFSAILRENSPAWN < 5 || FWS_RANDOMINTERVALOFSAILRENSPAWN > 2880) FWS_RANDOMINTERVALOFSAILRENSPAWN = 1440;
                FWS_RANDOMINTERVALOFSAILRENSPAWN = FWS_RANDOMINTERVALOFSAILRENSPAWN * 60000;
                FWS_INTERVALOFNEXTMONSTER = Integer.parseInt(bossesSettings.getProperty("IntervalOfNextMonster", "1"));
                if(FWS_INTERVALOFNEXTMONSTER < 1 || FWS_INTERVALOFNEXTMONSTER > 10) FWS_INTERVALOFNEXTMONSTER = 1;
                FWS_INTERVALOFNEXTMONSTER = FWS_INTERVALOFNEXTMONSTER * 60000;
                FWS_ACTIVITYTIMEOFMOBS = Integer.parseInt(bossesSettings.getProperty("ActivityTimeOfMobs", "120"));
                if(FWS_ACTIVITYTIMEOFMOBS < 1 || FWS_ACTIVITYTIMEOFMOBS > 120) FWS_ACTIVITYTIMEOFMOBS = 120;
                FWS_ACTIVITYTIMEOFMOBS = FWS_ACTIVITYTIMEOFMOBS * 60000;
				
                //TODO BAIUM VALAKAS
                Valakas_Wait_Time = Integer.parseInt(bossesSettings.getProperty("ValakasWaitTime", "30"));
                if ((Valakas_Wait_Time < 3) || (Valakas_Wait_Time > 60))
                  Valakas_Wait_Time = 30;
                Valakas_Wait_Time *= 60000;
                
                Interval_Of_Valakas_Spawn = Integer.parseInt(bossesSettings.getProperty("IntervalOfValakasSpawn", "192"));
                if ((Interval_Of_Valakas_Spawn < 1) || (Interval_Of_Valakas_Spawn > 480))
                  Interval_Of_Valakas_Spawn = 192;
                Interval_Of_Valakas_Spawn *= 3600000;
                
                Random_Of_Valakas_Spawn = Integer.parseInt(bossesSettings.getProperty("RandomOfValakasSpawn", "145"));
                if ((Random_Of_Valakas_Spawn < 1) || (Random_Of_Valakas_Spawn > 192))
                  Random_Of_Valakas_Spawn = 145;
                Random_Of_Valakas_Spawn *= 3600000;

                Interval_Of_Baium_Spawn = Integer.parseInt(bossesSettings.getProperty("IntervalOfBaiumSpawn", "121"));
                if ((Interval_Of_Baium_Spawn < 1) || (Interval_Of_Baium_Spawn > 480))
                  Interval_Of_Baium_Spawn = 121;
                Interval_Of_Baium_Spawn *= 3600000;

                Random_Of_Baium_Spawn = Integer.parseInt(bossesSettings.getProperty("RandomOfBaiumSpawn", "8"));
                if ((Random_Of_Baium_Spawn < 1) || (Random_Of_Baium_Spawn > 192))
                  Random_Of_Baium_Spawn = 8;
                Random_Of_Baium_Spawn *= 3600000;
                
                
                
				HPH_FIXINTERVALOFHALTER = Integer.parseInt(bossesSettings.getProperty("FixIntervalOfHalter", "172800"));
                if (HPH_FIXINTERVALOFHALTER < 300 || HPH_FIXINTERVALOFHALTER > 864000) HPH_FIXINTERVALOFHALTER = 172800;
                HPH_RANDOMINTERVALOFHALTER = Integer.parseInt(bossesSettings.getProperty("RandomIntervalOfHalter", "86400"));
                if (HPH_RANDOMINTERVALOFHALTER < 300 || HPH_RANDOMINTERVALOFHALTER > 864000) HPH_RANDOMINTERVALOFHALTER = 86400;
                HPH_APPTIMEOFHALTER = Integer.parseInt(bossesSettings.getProperty("AppTimeOfHalter", "20"));
                if (HPH_APPTIMEOFHALTER < 5 || HPH_APPTIMEOFHALTER > 60) HPH_APPTIMEOFHALTER = 20;
                HPH_ACTIVITYTIMEOFHALTER = Integer.parseInt(bossesSettings.getProperty("ActivityTimeOfHalter", "21600"));
                if (HPH_ACTIVITYTIMEOFHALTER < 7200 || HPH_ACTIVITYTIMEOFHALTER > 86400) HPH_ACTIVITYTIMEOFHALTER = 21600;
                HPH_FIGHTTIMEOFHALTER = Integer.parseInt(bossesSettings.getProperty("FightTimeOfHalter", "7200"));
                if (HPH_FIGHTTIMEOFHALTER < 7200 || HPH_FIGHTTIMEOFHALTER > 21600) HPH_FIGHTTIMEOFHALTER = 7200;
                HPH_CALLROYALGUARDHELPERCOUNT = Integer.parseInt(bossesSettings.getProperty("CallRoyalGuardHelperCount", "6"));
                if (HPH_CALLROYALGUARDHELPERCOUNT < 1 || HPH_CALLROYALGUARDHELPERCOUNT > 6) HPH_CALLROYALGUARDHELPERCOUNT = 6;
                HPH_CALLROYALGUARDHELPERINTERVAL = Integer.parseInt(bossesSettings.getProperty("CallRoyalGuardHelperInterval", "10"));
                if (HPH_CALLROYALGUARDHELPERINTERVAL < 1 || HPH_CALLROYALGUARDHELPERINTERVAL > 60) HPH_CALLROYALGUARDHELPERINTERVAL = 10;
                HPH_INTERVALOFDOOROFALTER = Integer.parseInt(bossesSettings.getProperty("IntervalOfDoorOfAlter", "5400"));
                if (HPH_INTERVALOFDOOROFALTER < 60 || HPH_INTERVALOFDOOROFALTER > 5400) HPH_INTERVALOFDOOROFALTER = 5400;
                HPH_TIMEOFLOCKUPDOOROFALTAR = Integer.parseInt(bossesSettings.getProperty("TimeOfLockUpDoorOfAltar", "180"));
                if (HPH_TIMEOFLOCKUPDOOROFALTAR < 60 || HPH_TIMEOFLOCKUPDOOROFALTAR > 600) HPH_TIMEOFLOCKUPDOOROFALTAR = 180;
				
               
                LIT_FIXINTERVALOFFRINTEZZA = Integer.parseInt(bossesSettings.getProperty("FixIntervalOfFrintezza", "11520"));
                if ((LIT_FIXINTERVALOFFRINTEZZA < 1) || (LIT_FIXINTERVALOFFRINTEZZA > 480))
                	LIT_FIXINTERVALOFFRINTEZZA = 121;
                LIT_FIXINTERVALOFFRINTEZZA *= 3600000;
                LIT_RANDOMINTERVALOFFRINTEZZA = Integer.parseInt(bossesSettings.getProperty("RandomIntervalOfFrintezza", "8640"));
                if ((LIT_RANDOMINTERVALOFFRINTEZZA < 1) || (LIT_RANDOMINTERVALOFFRINTEZZA > 192))
                	LIT_RANDOMINTERVALOFFRINTEZZA = 8;
                LIT_RANDOMINTERVALOFFRINTEZZA *= 3600000;
                
			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+BOSSES_FILE+" File.");
            }
			try
            {
                Properties OfflineSettings    = new Properties();
                InputStream is              = new FileInputStream(new File(OFFLINE_FILE));
                OfflineSettings.load(is);
                is.close();

                OFFLINE_TRADE_ENABLE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineTradeEnable", "false"));
    			OFFLINE_CRAFT_ENABLE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineCraftEnable", "false"));
    			OFFLINE_SET_NAME_COLOR = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineNameColorEnable", "false"));
    			OFFLINE_NAME_COLOR = Integer.decode("0x" + OfflineSettings.getProperty("OfflineNameColor", "ff00ff"));

    			RESTORE_OFFLINERS = Boolean.parseBoolean(OfflineSettings.getProperty("RestoreOffliners", "false")); 
    			OFFLINE_MAX_DAYS = Integer.parseInt(OfflineSettings.getProperty("OfflineMaxDays", "10"));
    			
    			OFFLINE_DISCONNECT_FINISHED = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineDisconnectFinished", "true"));

            
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new Error("Failed to Load "+OFFLINE_FILE+" File.");
            }
			try
            {
                Properties Buff    = new Properties();
                InputStream is              = new FileInputStream(new File(BUFF_FILE));
                Buff.load(is);
                is.close();

				 DANCE_TIME_MULTIPLIER = Float.parseFloat(Buff.getProperty("DanceTimeMultiplier", "1"));
			     BUFF_TIME_MULTIPLIER = Float.parseFloat(Buff.getProperty("BuffMultiplier", "1"));
			     SPIRIT_TIME_MULTIPLIER = Float.parseFloat(Buff.getProperty("SpiritMultiplier", "1"));
			     REMOVE_BUFFS_AFTER_DEATH = Boolean.parseBoolean(Buff.getProperty("RemoveBuffsAfterDeath", "False")); 
			     
            
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new Error("Failed to Load "+OFFLINE_FILE+" File.");
            }
			try {
                Properties castleSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(CASTLE_CONFIG_FILE));
                castleSettings.load(is);
				is.close();
				CS_TELE_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleTeleportFunctionFeeRatio", "604800000"));
                CS_TELE1_FEE = Integer.parseInt(castleSettings.getProperty("CastleTeleportFunctionFeeLvl1", "7000"));
                CS_TELE2_FEE = Integer.parseInt(castleSettings.getProperty("CastleTeleportFunctionFeeLvl2", "14000"));
                CS_SUPPORT_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleSupportFunctionFeeRatio", "86400000"));
                CS_SUPPORT1_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl1", "7000"));
                CS_SUPPORT2_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl2", "21000"));
                CS_SUPPORT3_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl3", "37000"));
                CS_SUPPORT4_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl4", "52000"));
                CS_MPREG_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleMpRegenerationFunctionFeeRatio", "86400000"));
                CS_MPREG1_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl1", "2000"));
                CS_MPREG2_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl2", "6500"));
                CS_MPREG3_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl3", "13750"));
                CS_MPREG4_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl4", "20000"));
                CS_HPREG_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleHpRegenerationFunctionFeeRatio", "86400000"));
                CS_HPREG1_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl1", "1000"));
                CS_HPREG2_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl2", "1500"));
                CS_HPREG3_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl3", "2250"));
                CS_HPREG4_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl14", "3270"));
                CS_HPREG5_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl15", "5166"));
                CS_EXPREG_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleExpRegenerationFunctionFeeRatio", "86400000"));
                CS_EXPREG1_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl1", "9000"));
                CS_EXPREG2_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl2", "15000"));
                CS_EXPREG3_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl3", "21000"));
                CS_EXPREG4_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl4", "30000"));
			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+CASTLE_CONFIG_FILE+" File.");
            }

			try {
                Properties developerSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(DEVELOPER_CONFIG_FILE));
                developerSettings.load(is);
				is.close();
				ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(developerSettings.getProperty("AcceptGeoeditorConn", "False"));
				USE_3D_MAP = Boolean.valueOf(developerSettings.getProperty("Use3DMap", "False"));
                NEW_NODE_ID = Integer.parseInt(developerSettings.getProperty("NewNodeId", "7952"));
                SELECTED_NODE_ID = Integer.parseInt(developerSettings.getProperty("NewNodeId", "7952"));
                LINKED_NODE_ID = Integer.parseInt(developerSettings.getProperty("NewNodeId", "7952"));
                NEW_NODE_TYPE = developerSettings.getProperty("NewNodeType", "npc");
                CHECK_KNOWN = Boolean.valueOf(developerSettings.getProperty("CheckKnownList", "false"));
			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+DEVELOPER_CONFIG_FILE+" File.");
            }

			try {
                Properties enchantSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(ENCHANT_CONFIG_FILE));
                enchantSettings.load(is);
				is.close();

				ENCHANT_HERO_WEAPONS = Boolean.parseBoolean(enchantSettings.getProperty("EnchantHeroWeapons", "False"));
				
				MAGE_WEAPON_ID = enchantSettings.getProperty("ListOfMageWeapons", "6579,6608,6610,6609");
				MAGE_WEAPON_ID_LIST = new FastList<Integer>();
				for (String id : MAGE_WEAPON_ID.split(","))
				{
					MAGE_WEAPON_ID_LIST.add(Integer.parseInt(id));
				}
					String[] propertySplit;    
					NORMAL_WEAPON_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalWeaponEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalWeaponEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                NORMAL_WEAPON_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalWeaponEnchantLevelDonate");
		              }
		            }
		          }
		          NORMAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalWeaponMageEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalWeaponMageEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                NORMAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalWeaponMageEnchantLevelDonate");
		              }
		            }
		          }
		          CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalWeaponMageEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalWeaponMageEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalWeaponMageEnchantLevelDonate");
		              }
		            }
		          }
		          NORMAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalFullArmorEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalFullArmorEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                NORMAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalFullArmorEnchantLevelDonate");
		              }
		            }
		          }
		          BLESS_FULL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessFullArmorEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessFullArmorEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                BLESS_FULL_ARMOR_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessFullArmorEnchantLevelDonate");
		              }
		            }
		          }
		          CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalFullArmorEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalFullArmorEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalFullArmorEnchantLevelDonate");
		              }
		            }
		          }
		          BLESS_WEAPON_MAGE_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessWeaponMageEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessWeaponMageEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                BLESS_WEAPON_MAGE_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessWeaponMageEnchantLevelDonate");
		              }
		            }
		          }
		          BLESS_WEAPON_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessWeaponEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessWeaponEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                BLESS_WEAPON_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessWeaponEnchantLevelDonate");
		              }
		            }
		          }
		          CRYSTAL_WEAPON_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalWeaponEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalWeaponEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                CRYSTAL_WEAPON_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalWeaponEnchantLevelDonate");
		              }
		            }
		          }
		          NORMAL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalArmorEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalArmorEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                NORMAL_ARMOR_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalArmorEnchantLevelDonate");
		              }
		            }
		          }
		          BLESS_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessArmorEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessArmorEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                BLESS_ARMOR_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessArmorEnchantLevelDonate");
		              }
		            }
		          }
		          CRYSTAL_ARMOR_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalArmorEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalArmorEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                CRYSTAL_ARMOR_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalArmorEnchantLevelDonate");
		              }
		            }
		          }
		          NORMAL_JEWELRY_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalJewelryEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalJewelryEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                NORMAL_JEWELRY_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalJewelryEnchantLevelDonate");
		              }
		            }
		          }
		          BLESS_JEWELRY_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessJewelryEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessJewelryEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                BLESS_JEWELRY_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessJewelryEnchantLevelDonate");
		              }
		            }
		          }
		          CRYSTAL_JEWELRY_ENCHANT_LEVEL_DONATE = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalJewelryEnchantLevelDonate", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalJewelryEnchantLevelDonate");
		            }
		            else {
		              try
		              {
		                CRYSTAL_JEWELRY_ENCHANT_LEVEL_DONATE.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalJewelryEnchantLevelDonate");
		              }
		            }

		          }

		          NORMAL_WEAPON_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalWeaponEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalWeaponEnchantLevel");
		            }
		            else {
		              try
		              {
		                NORMAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalWeaponEnchantLevel");
		              }
		            }
		          }
		          NORMAL_WEAPON_MAGE_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalWeaponMageEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalWeaponMageEnchantLevel");
		            }
		            else {
		              try
		              {
		                NORMAL_WEAPON_MAGE_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalWeaponMageEnchantLevel");
		              }
		            }
		          }
		          CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalWeaponMageEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalWeaponMageEnchantLevel");
		            }
		            else {
		              try
		              {
		                CRYSTAL_WEAPON_MAGE_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalWeaponMageEnchantLevel");
		              }
		            }
		          }
		          BLESS_WEAPON_MAGE_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessWeaponMageEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessWeaponMageEnchantLevel");
		            }
		            else {
		              try
		              {
		                BLESS_WEAPON_MAGE_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessWeaponMageEnchantLevel");
		              }
		            }
		          }
		          BLESS_WEAPON_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessWeaponEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessWeaponEnchantLevel");
		            }
		            else {
		              try
		              {
		                BLESS_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessWeaponEnchantLevel");
		              }
		            }
		          }
		          CRYSTAL_WEAPON_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalWeaponEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalWeaponEnchantLevel");
		            }
		            else {
		              try
		              {
		                CRYSTAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalWeaponEnchantLevel");
		              }
		            }
		          }
		          NORMAL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalArmorEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalArmorEnchantLevel");
		            }
		            else {
		              try
		              {
		                NORMAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalArmorEnchantLevel");
		              }
		            }
		          }
		          NORMAL_FULL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalFullArmorEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalFullArmorEnchantLevel");
		            }
		            else {
		              try
		              {
		                NORMAL_FULL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalFullArmorEnchantLevel");
		              }
		            }
		          }
		          BLESS_FULL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessFullArmorEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessFullArmorEnchantLevel");
		            }
		            else {
		              try
		              {
		                BLESS_FULL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessFullArmorEnchantLevel");
		              }
		            }
		          }
		          CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalFullArmorEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalFullArmorEnchantLevel");
		            }
		            else {
		              try
		              {
		                CRYSTAL_FULL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalFullArmorEnchantLevel");
		              }
		            }
		          }
		          BLESS_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessArmorEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessArmorEnchantLevel");
		            }
		            else {
		              try
		              {
		                BLESS_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessArmorEnchantLevel");
		              }
		            }
		          }
		          CRYSTAL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalArmorEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalArmorEnchantLevel");
		            }
		            else {
		              try
		              {
		                CRYSTAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalArmorEnchantLevel");
		              }
		            }
		          }
		          BLESS_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("NormalJewelryEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property NormalJewelryEnchantLevel");
		            }
		            else {
		              try
		              {
		                NORMAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property NormalJewelryEnchantLevel");
		              }
		            }
		          }
		          BLESS_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("BlessJewelryEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property BlessJewelryEnchantLevel");
		            }
		            else {
		              try
		              {
		                BLESS_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property BlessJewelryEnchantLevel");
		              }
		            }
		          }
		          CRYSTAL_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
		          propertySplit = enchantSettings.getProperty("CrystalJewelryEnchantLevel", "").split(";");
		          for (String enchant : propertySplit)
		          {
		            String[] writeData = enchant.split(",");
		            if (writeData.length != 2)
		            {
		              _log.info("invalid config property CrystalJewelryEnchantLevel");
		            }
		            else {
		              try
		              {
		                CRYSTAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]),Integer.parseInt(writeData[1]));
		              }
		              catch (NumberFormatException nfe)
		              {
		                if (enchant.equals(""))
		                  continue;
		                _log.info("invalid config property CrystalJewelryEnchantLevel");
		              }
		            }
		          }
				
				ENCHANT_FAIL = Integer.parseInt(enchantSettings.getProperty("BlessEnchantFail", "0")); 
				ENCHANT_STACKABLE = Boolean.parseBoolean(enchantSettings.getProperty("EnchantStackable", "False"));
                
				ENCHANT_CHANCE_WEAPON  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceWeaponToTen", "66"));
                ENCHANT_CHANCE_ARMOR  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceArmorToTen", "66"));
                ENCHANT_CHANCE_JEWELRY  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceJewelryToTen", "66"));
				ENCHANT_CHANCE_WEAPON_1015  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceWeaponTenToFifteen", "66"));
                ENCHANT_CHANCE_ARMOR_1015  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceArmorTenToFifteen", "66"));
                ENCHANT_CHANCE_JEWELRY_1015  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceJewelryTenToFifteen", "66"));
				ENCHANT_CHANCE_WEAPON_16  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceWeaponAboveFifteen", "66"));
                ENCHANT_CHANCE_ARMOR_16  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceArmorAboveFifteen", "66"));
                ENCHANT_CHANCE_JEWELRY_16  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceJewelryAboveFifteen", "66"));
				BLESSED_CHANCE_WEAPON  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceWeaponToTen", "66"));
                BLESSED_CHANCE_ARMOR  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceArmorToTen", "66"));
                BLESSED_CHANCE_JEWELRY  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceJewelryToTen", "66"));
				BLESSED_CHANCE_WEAPON_1015  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceWeaponTenToFifteen", "66"));
                BLESSED_CHANCE_ARMOR_1015  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceArmorTenToFifteen", "66"));
                BLESSED_CHANCE_JEWELRY_1015  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceJewelryTenToFifteen", "66"));
				BLESSED_CHANCE_WEAPON_16  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceWeaponAboveFifteen", "66"));
                BLESSED_CHANCE_ARMOR_16  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceArmorAboveFifteen", "66"));
                BLESSED_CHANCE_JEWELRY_16  = Integer.parseInt(enchantSettings.getProperty("BlessedChanceJewelryAboveFifteen", "66"));
				CRYSTAL_CHANCE_WEAPON  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceWeaponToTen", "66"));
                CRYSTAL_CHANCE_ARMOR  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceArmorToTen", "66"));
                CRYSTAL_CHANCE_JEWELRY  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceJewelryToTen", "66"));
				CRYSTAL_CHANCE_WEAPON_1015  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceWeaponTenToFifteen", "66"));
                CRYSTAL_CHANCE_ARMOR_1015  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceArmorTenToFifteen", "66"));
                CRYSTAL_CHANCE_JEWELRY_1015  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceJewelryTenToFifteen", "66"));
				CRYSTAL_CHANCE_WEAPON_16  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceWeaponAboveFifteen", "66"));
                CRYSTAL_CHANCE_ARMOR_16  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceArmorAboveFifteen", "66"));
                CRYSTAL_CHANCE_JEWELRY_16  = Integer.parseInt(enchantSettings.getProperty("CrystalChanceJewelryAboveFifteen", "66"));
                ENCHANT_MAX_WEAPON = Integer.parseInt(enchantSettings.getProperty("EnchantMaxWeapon", "255"));
                ENCHANT_MAX_ARMOR = Integer.parseInt(enchantSettings.getProperty("EnchantMaxArmor", "255"));
                ENCHANT_MAX_JEWELRY = Integer.parseInt(enchantSettings.getProperty("EnchantMaxJewelry", "255"));
                ENCHANT_SAFE_MAX = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMax", "3"));
                ENCHANT_SAFE_MAX_FULL = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMaxFull", "4"));
				ENABLE_MODIFY_ENCHANT_CHANCE_WEAPON = Boolean.parseBoolean(enchantSettings.getProperty("EnableModifyEnchantChanceWeapon", "False"));
                if (ENABLE_MODIFY_ENCHANT_CHANCE_WEAPON)
                {
                    ENCHANT_CHANCE_LIST_WEAPON = new FastMap<Integer, Integer>();
                    propertySplit = enchantSettings.getProperty("EnchantChanceListWeapon", "").split(";");
                    for (String enchant : propertySplit)
                    {
                        String[] enchantSplit = enchant.split(",");
                        if (enchantSplit.length != 2)
                        {
                            System.out.println("[EnchantChanceListWeapon]: invalid config property -> EnchantChanceListWeapon \"" + enchant + "\"");
                        } else
                        {
                            try
                            {
                                ENCHANT_CHANCE_LIST_WEAPON.put(Integer.parseInt(enchantSplit[0]), Integer.parseInt(enchantSplit[1]));
                            } catch (NumberFormatException nfe)
                            {
                                if (!enchant.equals(""))
                                {
                                    System.out.println("[EnchantChanceListWeapon]: invalid config property -> EnchantListWeapon \"" + enchantSplit[0] + "\"" + enchantSplit[1]);
                                }
                            }
                        }
                    }
                }
				ENABLE_MODIFY_ENCHANT_CHANCE_ARMOR = Boolean.parseBoolean(enchantSettings.getProperty("EnableModifyEnchantChanceArmor", "False"));
				if (ENABLE_MODIFY_ENCHANT_CHANCE_ARMOR)
                {
                    ENCHANT_CHANCE_LIST_ARMOR = new FastMap<Integer, Integer>();
                    propertySplit = enchantSettings.getProperty("EnchantChanceListArmor", "").split(";");
                    for (String enchant : propertySplit)
                    {
                        String[] enchantSplit = enchant.split(",");
                        if (enchantSplit.length != 2)
                        {
                            System.out.println("[EnchantChanceListArmor]: invalid config property -> EnchantChanceListArmor \"" + enchant + "\"");
                        } else
                        {
                            try
                            {
                                ENCHANT_CHANCE_LIST_ARMOR.put(Integer.parseInt(enchantSplit[0]), Integer.parseInt(enchantSplit[1]));
                            } catch (NumberFormatException nfe)
                            {
                                if (!enchant.equals(""))
                                {
                                    System.out.println("[EnchantChanceListArmor]: invalid config property -> EnchantListArmor \"" + enchantSplit[0] + "\"" + enchantSplit[1]);
                                }
                            }
                        }
                    }
                }
				ENABLE_MODIFY_ENCHANT_CHANCE_JEWELRY = Boolean.parseBoolean(enchantSettings.getProperty("EnableModifyEnchantChanceJewelry", "False"));
				if (ENABLE_MODIFY_ENCHANT_CHANCE_JEWELRY)
                {
                    ENCHANT_CHANCE_LIST_JEWELRY = new FastMap<Integer, Integer>();
                    
                    propertySplit = enchantSettings.getProperty("EnchantChanceListJewelry", "").split(";");
                    for (String enchant : propertySplit)
                    {
                        String[] enchantSplit = enchant.split(",");
                        if (enchantSplit.length != 2)
                        {
                            System.out.println("[EnchantChanceListJewelry]: invalid config property -> EnchantChanceListJewelry \"" + enchant + "\"");
                        } else
                        {
                            try
                            {
                                ENCHANT_CHANCE_LIST_JEWELRY.put(Integer.parseInt(enchantSplit[0]), Integer.parseInt(enchantSplit[1]));
                            } catch (NumberFormatException nfe)
                            {
                                if (!enchant.equals(""))
                                {
                                    System.out.println("[EnchantChanceListJewelry]: invalid config property -> EnchantListJewelry \"" + enchantSplit[0] + "\"" + enchantSplit[1]);
                                }
                            }
                        }
                    }
                }
				ENABLE_MODIFY_ENCHANT_MULTISELL = Boolean.parseBoolean(enchantSettings.getProperty("EnableModifyEnchantMultisell", "False"));
				if (ENABLE_MODIFY_ENCHANT_MULTISELL)
                {
                    ENCHANT_MULTISELL_LIST = new FastMap<Integer, Integer>();
                   
                    propertySplit = enchantSettings.getProperty("EnchantMultisellList", "").split(";");
                    for (String enchant : propertySplit)
                    {
                        String[] enchantSplit = enchant.split(",");
                        if (enchantSplit.length != 2)
                        {
                            System.out.println("[EnchantMultisellList]: invalid config property -> EnchantMultisellList \"" + enchant + "\"");
                        } else
                        {
                            try
                            {
                                ENCHANT_MULTISELL_LIST.put(Integer.parseInt(enchantSplit[0]), Integer.parseInt(enchantSplit[1]));
                            } catch (NumberFormatException nfe)
                            {
                                if (!enchant.equals(""))
                                {
                                    System.out.println("[EnchantMultisellList]: invalid config property -> EnchantMultisellList \"" + enchantSplit[0] + "\"" + enchantSplit[1]);
                                }
                            }
                        }
                    }
                }
			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+ENCHANT_CONFIG_FILE+" File.");
            }

			try
            {
                Properties donSettings    = new Properties();
                InputStream is              = new FileInputStream(new File(DON_FILE));
                donSettings.load(is);
                is.close();

                COL_TITLECOLOR               = Integer.parseInt(donSettings.getProperty("ColForTitleColor", "7"));
                COL_NICKCOLOR              	 = Integer.parseInt(donSettings.getProperty("ColForNickColor", "7"));
                COL_NOBLESSE                 = Integer.parseInt(donSettings.getProperty("ColForNoblesse", "3"));
                COL_CHANGENAME				 = Integer.parseInt(donSettings.getProperty("ColForNameChange", "3"));
                COL_CHANGECLANNAME		     = Integer.parseInt(donSettings.getProperty("ColForClanNameChange", "3"));
                COL_6LVL_CLAN			     = Integer.parseInt(donSettings.getProperty("ColForClan6lvl", "3"));
                COL_7LVL_CLAN			     = Integer.parseInt(donSettings.getProperty("ColForClan7lvl", "3"));
                COL_8LVL_CLAN			     = Integer.parseInt(donSettings.getProperty("ColForClan8lvl", "3"));
                COL_PREM1			     	 = Integer.parseInt(donSettings.getProperty("ColForPremium1m", "3"));
                COL_PREM2			     	 = Integer.parseInt(donSettings.getProperty("ColForPremium2m", "3"));
                COL_PREM3			     	 = Integer.parseInt(donSettings.getProperty("ColForPremium3m", "3"));
                COL_SEX					 	 = Integer.parseInt(donSettings.getProperty("ColForSex", "3"));
                COL_PK					 	 = Integer.parseInt(donSettings.getProperty("ColForPK", "3"));
                COL_HERO				 	 = Integer.parseInt(donSettings.getProperty("ColForHero", "3"));
                COL_CRP				 		 = Integer.parseInt(donSettings.getProperty("ColForCRP", "3"));
                CRP_COUNT					 = Integer.parseInt(donSettings.getProperty("CRPCount", "1000"));
                CRP_ITEM_ID			    	 = Integer.parseInt(donSettings.getProperty("CRPItemId", "4037"));
                DON_ITEM_ID			     	 = Integer.parseInt(donSettings.getProperty("DonateItemId", "4037"));
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new Error("Failed to Load "+DON_FILE+" File.");
            }
			
			try {
                Properties chatSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(CHAT_CONFIG_FILE));
                chatSettings.load(is);
				is.close();
				DEFAULT_GLOBAL_CHAT = chatSettings.getProperty("GlobalChat", "ON");
                DEFAULT_TRADE_CHAT = chatSettings.getProperty("TradeChat", "ON");
				MAX_MESSAGE_LENGHT = Integer.parseInt(chatSettings.getProperty("MaxChatLenght", "75"));
				SHOUT_FLOOD_TIME = Integer.parseInt(chatSettings.getProperty("ShoutFloodTime", "3"));
				TRADE_FLOOD_TIME = Integer.parseInt(chatSettings.getProperty("TradeFloodTime", "3"));
				TELL_CHAT_LVL = Integer.parseInt(chatSettings.getProperty("TellChatMinLvl", "1"));
				SHOUT_CHAT_LVL = Integer.parseInt(chatSettings.getProperty("ShoutChatMinLvl", "1"));
				TRADE_CHAT_LVL = Integer.parseInt(chatSettings.getProperty("TradeChatMinLvl", "1"));
				
		          USE_SAY_FILTER = Boolean.valueOf(chatSettings.getProperty("UseSayFilter", "true")).booleanValue();
		          USE_SAY_FILTER_EXCEPTIONS = Boolean.valueOf(chatSettings.getProperty("UseSayFilterExceptions", "false")).booleanValue();
		          BAN_FOR_BAD_WORDS = Boolean.valueOf(chatSettings.getProperty("BanForBadWords", "false")).booleanValue();
		          KARMA_FOR_BAD_WORDS = Boolean.valueOf(chatSettings.getProperty("KarmaForBadWords", "false")).booleanValue();
		          HARD_FILTERING = Boolean.valueOf(chatSettings.getProperty("HardFiltering", "false")).booleanValue();
		          TIME_AUTO_CHAT_BAN = Integer.parseInt(chatSettings.getProperty("TimeAutoChatBan", "15"));
		          KARMA_FOR_BAD_WORD_MIN = Integer.parseInt(chatSettings.getProperty("KarmaForBadWordMin", "0"));
		          KARMA_FOR_BAD_WORD_MAX = Integer.parseInt(chatSettings.getProperty("KarmaForBadWordMax", "0"));
		          SAY_FILTER_REPLACEMENT_STRING = chatSettings.getProperty("ReplacementString", " {{CENSORE}} ");
		          CHAT_BAN_REASON = chatSettings.getProperty("ChatBanReason", "You have been banned by chat filter!");
			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+CHAT_CONFIG_FILE+" File.");
            }

            try {
                Properties geodataSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(GEODATA_CONFIG_FILE));
                geodataSettings.load(is);
                is.close();
                
                GEODATA = Boolean.parseBoolean(geodataSettings.getProperty("GeoData", "True"));
                GEO_DOORS = Boolean.parseBoolean(geodataSettings.getProperty("GeoDoors", "False"));
                GEO_CHECK_LOS = (Boolean.parseBoolean(geodataSettings.getProperty("GeoCheckLoS", "False"))) && (GEODATA);
                GEO_MOVE_PC = (Boolean.parseBoolean(geodataSettings.getProperty("GeoCheckMovePlayable", "False"))) && (GEODATA);
                GEO_MOVE_NPC = (Boolean.parseBoolean(geodataSettings.getProperty("GeoCheckMoveNpc", "False"))) && (GEODATA);
                GEO_PATH_FINDING = (Boolean.parseBoolean(geodataSettings.getProperty("GeoPathFinding", "False"))) && (GEODATA);
                FORCE_GEODATA = (Boolean.parseBoolean(geodataSettings.getProperty("ForceGeoData", "True"))) && (GEODATA);
                String correctZ = GEODATA ? geodataSettings.getProperty("GeoCorrectZ", "ALL") : "NONE";
                GEO_CORRECT_Z = CorrectSpawnsZ.valueOf(correctZ.toUpperCase());
                ADVANCED_DIAGONAL_STRATEGY = (Boolean.parseBoolean(geodataSettings.getProperty("AdvancedDiagonalStrategy", "True"))) && (GEODATA);
                DEBUG_PATH = (Boolean.parseBoolean(geodataSettings.getProperty("DebugPath", "False"))) && (GEODATA);
                HIGH_WEIGHT = Float.parseFloat(geodataSettings.getProperty("HighWeight", "3"));
                LOW_WEIGHT = Float.parseFloat(geodataSettings.getProperty("LowWeight", "0.5"));
                MEDIUM_WEIGHT = Float.parseFloat(geodataSettings.getProperty("MediumWeight", "2"));
                DIAGONAL_WEIGHT = Float.parseFloat(geodataSettings.getProperty("DiagonalWeight", "0.707"));
                PATHFIND_BUFFERS = geodataSettings.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
                MAX_POSTFILTER_PASSES = Integer.parseInt(geodataSettings.getProperty("MaxPostfilterPasses", "3"));
                
                
                
            }

			catch (Exception e)
			{
				e.printStackTrace();
                throw new Error("Failed to Load "+GEODATA_CONFIG_FILE+" File.");
			}
			try {
                Properties prem    = new Properties();
                InputStream is 		= new FileInputStream(new File(PREM_FILE));
                prem.load(is);
				is.close();
				
                USE_PREMIUMSERVICE = Boolean.parseBoolean(prem.getProperty("UsePremiumServices", "False"));
                PREMIUM_RATE_XP = Float.parseFloat(prem.getProperty("PremiumRateXp", "2"));
                PREMIUM_RATE_SP = Float.parseFloat(prem.getProperty("PremiumRateSp", "2"));
				PREMIUM_RATE_DROP_ADENA = Float.parseFloat(prem.getProperty("PremiumRateDropAdena", "1.2"));
                PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(prem.getProperty("PremiumRateDropSpoil", "1.2"));
                PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(prem.getProperty("PremiumRateDropItems", "1.2"));
                PREMIUM_RATE_DROP_QUEST = Float.parseFloat(prem.getProperty("PremiumRateDropQuest", "1.2"));
                PREMIUM_RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(prem.getProperty("PremiumRateRaidDropItems", "1"));
				PREMIUM_NICK_COLOR = Integer.decode((new StringBuilder()).append("0x").append(prem.getProperty("PremiumNickColor", "00CCFF")).toString());
				PREMIUM_TITLE_COLOR = Integer.decode((new StringBuilder()).append("0x").append(prem.getProperty("PremiumTitleColor", "00CCFF")).toString());

			}
			catch (Exception e)
			{
                e.printStackTrace();
                throw new Error("Failed to Load "+PREM_FILE+" File.");
            }		
			try {
				Properties protectSettings    = new Properties();
				InputStream is               = new FileInputStream(new File(PROTECT_CONFIG_FILE));
                protectSettings.load(is);
                is.close();
				SUBCLASS_PROTECT = Integer.parseInt(protectSettings.getProperty("SubclassProtect", "20000"));
				CHECK_SKILLS_ON_ENTER = Boolean.valueOf(protectSettings.getProperty("CheckSkillsOnEnter", "False"));
			    CHECK_HERO_SKILLS = Boolean.valueOf(protectSettings.getProperty("CheckHeroSkills", "True"));
			    CHECK_NOBLE_SKILLS = Boolean.valueOf(protectSettings.getProperty("CheckNobleSkills", "True"));
				NON_CHECK_SKILLS = protectSettings.getProperty("NonCheckSkills", "10000");
                LIST_NON_CHECK_SKILLS = new FastList<Integer>();
                for (String id : NON_CHECK_SKILLS.split(",")) {
                    LIST_NON_CHECK_SKILLS.add(Integer.parseInt(id));
                }
				BYPASS_VALIDATION = Boolean.valueOf(protectSettings.getProperty("BypassValidation", "True"));
				ONLY_GM_ITEMS_FREE = Boolean.valueOf(protectSettings.getProperty("OnlyGMItemsFree", "True"));
				GAMEGUARD_ENFORCE = Boolean.valueOf(protectSettings.getProperty("GameGuardEnforce", "False"));
                GAMEGUARD_PROHIBITACTION = Boolean.valueOf(protectSettings.getProperty("GameGuardProhibitAction", "False"));
				FLOOD_PROTECTION = Boolean.parseBoolean(protectSettings.getProperty("EnableFloodProtection","True"));
				FLOODPROTECTOR_INITIALSIZE = Integer.parseInt(protectSettings.getProperty("FloodProtectorInitialSize", "50"));
                FAST_CONNECTION_LIMIT = Integer.parseInt(protectSettings.getProperty("FastConnectionLimit","15"));
                NORMAL_CONNECTION_TIME = Integer.parseInt(protectSettings.getProperty("NormalConnectionTime","700"));
                FAST_CONNECTION_TIME = Integer.parseInt(protectSettings.getProperty("FastConnectionTime","350"));
                MAX_CONNECTION_PER_IP = Integer.parseInt(protectSettings.getProperty("MaxConnectionPerIP","50"));
				ENABLE_PACKET_PROTECTION = Boolean.parseBoolean(protectSettings.getProperty("PacketProtection", "false"));
				MAX_UNKNOWN_PACKETS = Integer.parseInt(protectSettings.getProperty("UnknownPacketsBeforeBan", "50"));
				UNKNOWN_PACKETS_PUNISHMENT = Integer.parseInt(protectSettings.getProperty("UnknownPacketsPunishment", "2"));
				WH_FLOOD_TIME = Integer.parseInt(protectSettings.getProperty("WarehouseFloodTime", "3"));
				ENCHANT_FLOOD_TIME = Integer.parseInt(protectSettings.getProperty("EnchantFloodTime", "3"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
                throw new Error("Failed to Load "+PROTECT_CONFIG_FILE+" File.");
			}
			
			try {
				Properties eonSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(EON_CONFIG_FILE));
                eonSettings.load(is);
                is.close();
                REBIRTH_ITEM = Integer.parseInt(eonSettings.getProperty("RebirthItemId", "0"));
                PTS_EMULATION_LOAD_NPC_SERVER_TIME = Integer.parseInt(eonSettings.getProperty("PTSEmulationLoadNpcServerTime", "120"));
                MAX_CHAR_LEVEL = Byte.parseByte(eonSettings.getProperty("MaxCharLvL", "40"));
                
                OLY_SAME_IP = Boolean.parseBoolean(eonSettings.getProperty("EnableOlySameIp", "True"));
                IB_ITEM_ID1 = Integer.parseInt(eonSettings.getProperty("ItemBuffID1", "0"));
                IB_ITEM_ID2 = Integer.parseInt(eonSettings.getProperty("ItemBuffID2", "0"));
                IB_ITEM_ID3 = Integer.parseInt(eonSettings.getProperty("ItemBuffID3", "0"));
                IB_ITEM_ID4 = Integer.parseInt(eonSettings.getProperty("ItemBuffID4", "0"));
                IB_ITEM_ID5 = Integer.parseInt(eonSettings.getProperty("ItemBuffID5", "0"));
                IB_SKILL_ID1 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillID1", "0"));
                IB_SKILL_LVL1 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillLvL1", "0"));
                IB_SKILL_ID2 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillID2", "0"));
                IB_SKILL_LVL2 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillLvL2", "0"));
                IB_SKILL_ID3 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillID3", "0"));
                IB_SKILL_LVL3 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillLvL3", "0"));
                IB_SKILL_ID4 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillID4", "0"));
                IB_SKILL_LVL4 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillLvL4", "0"));
                IB_SKILL_ID5 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillID5", "0"));
                IB_SKILL_LVL5 = Integer.parseInt(eonSettings.getProperty("ItemBuffSkillLvL5", "0"));
                CLAN_MEMBER_LVLUP6 = Integer.parseInt(eonSettings.getProperty("ClanMemberLvLUp6", "30"));
                CLAN_MEMBER_LVLUP7 = Integer.parseInt(eonSettings.getProperty("ClanMemberLvLUp7", "80"));
                CLAN_MEMBER_LVLUP8 = Integer.parseInt(eonSettings.getProperty("ClanMemberLvLUp8", "120"));
                REWARD_KILL_WAR = Integer.parseInt(eonSettings.getProperty("RewardKillWar", "2"));
                CLAN_SCORE_SIEGE = Integer.parseInt(eonSettings.getProperty("ClanScoreSiege", "500"));
                BONUS_CLAN_SCORE_SIEGE = Integer.parseInt(eonSettings.getProperty("BonusClanScoreSiege", "1000"));
                SHOW_NPC_LVL = Boolean.parseBoolean(eonSettings.getProperty("ShowNpcLevel", "false"));
                
                SHOW_WELCOME_PM = Boolean.parseBoolean(eonSettings.getProperty("ShowPmOnEnter", "false"));
                
                MP_RESTORE = Integer.parseInt(eonSettings.getProperty("MPRestore", "1000"));
                
                DUEL_SPAWN_X = Integer.parseInt(eonSettings.getProperty("PartyDuelSpawnX", "149319")); 
                DUEL_SPAWN_Y = Integer.parseInt(eonSettings.getProperty("PartyDuelSpawnY", "46710")); 
                DUEL_SPAWN_Z = Integer.parseInt(eonSettings.getProperty("PartyDuelSpawnZ", "-3413")); 
                DAY_TO_SIEGE = Integer.parseInt(eonSettings.getProperty("DayToSiege", "14"));
				KICK_L2WALKER = Boolean.parseBoolean(eonSettings.getProperty("L2WalkerProtection","True"));
				
				//HIGH_LVL_RB_FREEZ = Boolean.parseBoolean(eonSettings.getProperty("FreezHighLvlRbAttackers", "True"));
				
				INVUL_NPC_LIST = new FastList<Integer>();
			    String t = eonSettings.getProperty("InvulNpcList", "30001-32132,35092-35103,35142-35146,35176-35187,35218-35232,35261-35278,35308-35319,35352-35367,35382-35407,35417-35427,35433-35469,35497-35513,35544-35587,35600-35617,35623-35628,35638-35640,35644,35645,50007,70010,99999");
			    String as[];
			    int k = (as = t.split(",")).length;
				for(int j = 0; j < k; j++)
				{
					String t2 = as[j];
					if(t2.contains("-"))
					{
						int a1 = Integer.parseInt(t2.split("-")[0]);
						int a2 = Integer.parseInt(t2.split("-")[1]);
						for(int i = a1; i <= a2; i++)
							INVUL_NPC_LIST.add(Integer.valueOf(i));
					} else
					{
						INVUL_NPC_LIST.add(Integer.valueOf(Integer.parseInt(t2)));
					}
			    }
				
				BUFF_ITEM_ID = Integer.parseInt(eonSettings.getProperty("BuffItemID", "57"));
				BUFF_MAGE_3 = Integer.parseInt(eonSettings.getProperty("BuffMage3", "200000"));
				BUFF_MAGE_2 = Integer.parseInt(eonSettings.getProperty("BuffMage2", "100000"));
				BUFF_MAGE_1 = Integer.parseInt(eonSettings.getProperty("BuffMage1", "50000"));
				
				
				BUFF_FIGHTER_3 = Integer.parseInt(eonSettings.getProperty("BuffFighter3", "200000"));
				BUFF_FIGHTER_2 = Integer.parseInt(eonSettings.getProperty("BuffFighter2", "100000"));
				BUFF_FIGHTER_1 = Integer.parseInt(eonSettings.getProperty("BuffFighter1", "50000"));
				
				BUFF_OTHER = Integer.parseInt(eonSettings.getProperty("BuffOther", "25000"));
				BUFF_REBUF = Integer.parseInt(eonSettings.getProperty("ReBuff", "25000"));
				
				BUFFS_LIST = getIntArray(eonSettings, "BuffsList", new int[] { 0 });
				BUFFER_TABLE_DIALOG = getIntArray(eonSettings, "BufferTableDialog", new int[] { 0 });
				
				
				ON_ENTER_BUFFS = Boolean.parseBoolean(eonSettings.getProperty("EnableOnEnterBuffs", "False"));
				ON_ENTER_BUFFS_LVL = Integer.parseInt(eonSettings.getProperty("OnEnterBuffsMaxLvL", "40"));

		          String[] propertySplit = eonSettings.getProperty("OnEnterFighterBuffs", "").split(";");
		          if (!propertySplit[0].isEmpty())
		          {
		        	ON_ENTER_F_BUFFS = new TIntIntHashMap(propertySplit.length);
		            for (String skill : propertySplit)
		            {
		              String[] skillSplit = skill.split(",");
		              if (skillSplit.length != 2) {
		                _log.warning(StringUtil.concat(new String[] { "invalid config property -> OnEnterFighterBuffs \"", skill, "\"" }));
		              }
		              else {
		                try
		                {
		                	ON_ENTER_F_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
		                }
		                catch (NumberFormatException nfe)
		                {
		                  if (!skill.isEmpty()) {
		                    _log.warning(StringUtil.concat(new String[] { "invalid config property -> OnEnterFighterBuffs \"", skill, "\"" }));
		                  }
		                }
		              }
		            }
		          }
		          propertySplit = eonSettings.getProperty("OnEnterMageBuffs", "").split(";");
		          if (!propertySplit[0].isEmpty())
		          {
		        	 ON_ENTER_M_BUFFS = new TIntIntHashMap(propertySplit.length);
		            for (String skill : propertySplit)
		            {
		              String[] skillSplit = skill.split(",");
		              if (skillSplit.length != 2) {
		                _log.warning(StringUtil.concat(new String[] { "invalid config property -> OnEnterMageBuffs \"", skill, "\"" }));
		              }
		              else {
		                try
		                {
		                   ON_ENTER_M_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
		                }
		                catch (NumberFormatException nfe)
		                {
		                  if (!skill.isEmpty()) {
		                    _log.warning(StringUtil.concat(new String[] { "invalid config property -> OnEnterMageBuffs \"", skill, "\"" }));
		                  }
		                }
		              }
		            }
		          }
		          
		          propertySplit = eonSettings.getProperty("ItemsOnCreateChar", "").split(";");
		          if (!propertySplit[0].isEmpty())
		          {
		        	  ITEMS_ON_CREATE_CHAR = new TIntIntHashMap(propertySplit.length);
		            for (String item : propertySplit)
		            {
		              String[] itemSplit = item.split(",");
		              if (itemSplit.length != 2) {
		                _log.warning(StringUtil.concat(new String[] { "invalid config property -> ItemsOnCreateChar \"", item, "\"" }));
		              }
		              else {
		                try
		                {
		                	ITEMS_ON_CREATE_CHAR.put(Integer.parseInt(itemSplit[0]), Integer.parseInt(itemSplit[1]));
		                }
		                catch (NumberFormatException nfe)
		                {
		                  if (!item.isEmpty()) {
		                    _log.warning(StringUtil.concat(new String[] { "invalid config property -> ItemsOnCreateChar \"", item, "\"" }));
		                  }
		                }
		              }
		            }
		          }
		          
		        NEW_SUBCLASS_LVL = Byte.parseByte(eonSettings.getProperty("NewSubClassLvL", "40"));
					  
		        BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(eonSettings.getProperty("BankingSystemEnabled", "False"));
		        BANKING_SYSTEM_1ITEMID = Integer.parseInt(eonSettings.getProperty("BankingSystem1ItemId", "57"));
		        BANKING_SYSTEM_1ITEMCOUNT = Integer.parseInt(eonSettings.getProperty("BankingSystem1ItemCount", "500000000"));
		        BANKING_SYSTEM_1ITEMNAME = eonSettings.getProperty("BankingSystem1ItemName", "Adena");
		        BANKING_SYSTEM_2ITEMID = Integer.parseInt(eonSettings.getProperty("BankingSystem2ItemId", "3470"));
		        BANKING_SYSTEM_2ITEMCOUNT = Integer.parseInt(eonSettings.getProperty("BankingSystem2ItemCount", "1"));
		        BANKING_SYSTEM_2ITEMNAME = eonSettings.getProperty("BankingSystem2ItemName", "GoldBar");

		        
		        SAVE_BUFF_PROFILES = Boolean.valueOf(eonSettings.getProperty("SaveBuffsProfiles", "true"));
				ENABLE_MENU = Boolean.valueOf(eonSettings.getProperty("EnableMenu", "true"));
				NAKRUTKA_ONLINE = Integer.parseInt(eonSettings.getProperty("NakrutkaOnline", "0"));
				MAX_MULTISELL = Integer.parseInt(eonSettings.getProperty("MaxMultisell","5000"));
				CHANCE_LS_SKILL = Integer.parseInt(eonSettings.getProperty("ChanceGetLsSkill","7"));
				MAX_SUBCLASS = Integer.parseInt(eonSettings.getProperty("MaxSubclass","3"));
				EXPERTISE_PENALTY = Boolean.valueOf(eonSettings.getProperty("ExpertisePenalty", "true"));
				BOWTANK_PENALTY = Boolean.valueOf(eonSettings.getProperty("BowPenaltyToTanks", "false"));
				LEVEL_ON_ENTER = Byte.parseByte(eonSettings.getProperty("LevelOnEnter", "0"));
                SP_ON_ENTER = Integer.parseInt(eonSettings.getProperty("SPOnEnter", "0"));
			    NOT_CONSUME_SHOTS = Boolean.valueOf(eonSettings.getProperty("UnlimShots", "false"));
			    NOT_CONSUME_ARROWS = Boolean.parseBoolean(eonSettings.getProperty("UnlimArrows", "false"));
			    NPC_ATTACKABLE = Boolean.valueOf(eonSettings.getProperty("NpcAttackable", "False")); 
			    CLASSMASTER_MSG = Boolean.valueOf(eonSettings.getProperty("ClassmasterMessage", "True"));
			    CLASS_MASTER_STRIDER_UPDATE = Boolean.parseBoolean(eonSettings.getProperty("ClassMasterUpdateStrider", "False")); 
			    if (!eonSettings.getProperty("ConfigClassMaster").trim().equalsIgnoreCase("False"))
				    CLASS_MASTER_SETTINGS_LINE = eonSettings.getProperty("ConfigClassMaster");
                CLASS_MASTER_SETTINGS = new ClassMasterSettings(CLASS_MASTER_SETTINGS_LINE);			 
			    STRIDER_LEVEL_FOR_UP = Integer.parseInt(eonSettings.getProperty("StrideLevelForUp", "55"));
                PRICE_FOR_STRIDER = Integer.parseInt(eonSettings.getProperty("PriceForStrider", "6000000"));
			    VIEW_SKILL_CHANCE = Boolean.parseBoolean(eonSettings.getProperty("SkillChance", "True"));
			    LIMITED_ENTRY_CATACOMB = Boolean.valueOf(eonSettings.getProperty("LimitedEntryCat", "False"));
                LIMITED_ENTRY_NECRO = Boolean.valueOf(eonSettings.getProperty("LimitedEntryNec", "False"));
			    ALLOW_CLASS_MASTERS = Boolean.valueOf(eonSettings.getProperty("ClassMasters", "False"));
                BUFFS_MAX_AMOUNT = Byte.parseByte(eonSettings.getProperty("MaxBuffAmount","24"));
                DEBUFFS_MAX_AMOUNT = Byte.parseByte(eonSettings.getProperty("MaxDeBuffAmount", "6"));
                ALT_WEIGHT_LIMIT = Double.parseDouble(eonSettings.getProperty("WeightLimit", "1"));
                MALARIA_CHANCE = Integer.parseInt(eonSettings.getProperty("GetMalariaChance", "1"));
                FLU_CHANCE = Integer.parseInt(eonSettings.getProperty("GetFluChance", "1"));
                STARTING_ADENA = Integer.parseInt(eonSettings.getProperty("StartingAdena", "100"));
			    DEATH_PENALTY_CHANCE = Integer.parseInt(eonSettings.getProperty("DeathPenaltyChance", "20"));
                MAX_DRIFT_RANGE = Integer.parseInt(eonSettings.getProperty("MaxDriftRange", "300"));
                MIN_NPC_ANIMATION = Integer.parseInt(eonSettings.getProperty("MinNPCAnimation", "10"));
                MAX_NPC_ANIMATION = Integer.parseInt(eonSettings.getProperty("MaxNPCAnimation", "20"));
                MIN_MONSTER_ANIMATION = Integer.parseInt(eonSettings.getProperty("MinMonsterAnimation", "5"));
                MAX_MONSTER_ANIMATION = Integer.parseInt(eonSettings.getProperty("MaxMonsterAnimation", "20"));          
                SERVER_NEWS = Boolean.valueOf(eonSettings.getProperty("ShowServerNews", "False"));
                ENABLE_NEWCHAR_TITLE = Boolean.parseBoolean(eonSettings.getProperty("EnableNewCharTitle", "false"));
    			NEW_CHAR_TITLE = eonSettings.getProperty("NewCharTitle", "NewTitle");
				ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(eonSettings.getProperty("EnableModifySkillDuration", "False"));
				if (ENABLE_MODIFY_SKILL_DURATION)
				{
					SKILL_DURATION_LIST = new FastMap<Integer, Integer>();
					propertySplit = eonSettings.getProperty("SkillDurationList", "").split(";");
					for (String skill : propertySplit)
					{
						String[] skillSplit = skill.split(",");
						if (skillSplit.length != 2)
						{
							System.out.println("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
						} else
						{
							try
							{
								SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
							}
							catch (NumberFormatException nfe)
							{
								if (!skill.isEmpty())
								{
									System.out.println("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
								}
							}
						}
					}
				}
				ENABLE_NO_AUTOLEARN_LIST = Boolean.parseBoolean(eonSettings.getProperty("EnableNoAutoLearnList", "False"));
		        if (ENABLE_NO_AUTOLEARN_LIST)
		        {
		          NO_AUTOLEARN_LIST = new FastList<Integer>();

		          propertySplit = eonSettings.getProperty("NoAutoLearnList", "").split(";");
		          for (String skill : propertySplit)
		          {
		            try
		            {
		              NO_AUTOLEARN_LIST.add(Integer.valueOf(Integer.parseInt(skill)));
		            }
		            catch (NumberFormatException nfe)
		            {
		              if (skill.equals(""))
		                continue;
		              System.out.println("[NoAutoLearnList]: invalid config property -> NoAutoLearnList \"" + skill);
		            }

		          }

		        }
            }
			
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+EON_CONFIG_FILE+" File.");
            }
            try {
                Properties serverSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(CONFIGURATION_FILE));
                serverSettings.load(is);
                is.close();
                GAMESERVER_HOSTNAME     = serverSettings.getProperty("GameserverHostname");
                PORT_GAME               = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
                EXTERNAL_HOSTNAME       = serverSettings.getProperty("ExternalHostname", "*");
                INTERNAL_HOSTNAME       = serverSettings.getProperty("InternalHostname", "*");
                GAME_SERVER_LOGIN_PORT  = Integer.parseInt(serverSettings.getProperty("LoginPort","9014"));
                GAME_SERVER_LOGIN_HOST  = serverSettings.getProperty("LoginHost","127.0.0.1");
                REQUEST_ID              = Integer.parseInt(serverSettings.getProperty("RequestServerID","0"));
                ACCEPT_ALTERNATE_ID     = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID","True"));
                DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
                DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
                DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
                DATABASE_PASSWORD           = serverSettings.getProperty("Password", "");
                DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
                DATAPACK_ROOT           = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
                CNAME_TEMPLATE          = serverSettings.getProperty("CnameTemplate", ".*");
                
                NOT_ALLOWED_NICKS = serverSettings.getProperty("NotAllowedNicks", "admin");
                for (String id : NOT_ALLOWED_NICKS.split(","))
                {
                	LIST_NOT_ALLOWED_NICKS.add(String.valueOf(id));
                }
                
                PET_NAME_TEMPLATE       = serverSettings.getProperty("PetNameTemplate", ".*");
                MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "0"));
                MAXIMUM_ONLINE_USERS        = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
                MIN_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "660"));
                MAX_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "665"));
                Random ppc = new Random();
                int z;
                z = ppc.nextInt(6);
                if(z==0){
                 z+=2;
                }
                for (int x = 0; x<8; x++) {
                 if(x==4)
                 {
                    RWHO_ARRAY[x]=(44);
                 }
                 else { RWHO_ARRAY[x] = 51+ppc.nextInt(z); }
                }
                RWHO_ARRAY[11] = 37265+ppc.nextInt(z*2+3);
                RWHO_ARRAY[8] = (51+ppc.nextInt(z));
                z = 36224+ppc.nextInt(z*2);
                RWHO_ARRAY[9]  = z;
                RWHO_ARRAY[10] = z;
                RWHO_ARRAY[12] = 1;
                RWHO_LOG = Boolean.parseBoolean(serverSettings.getProperty("RemoteWhoLog","False"));
				RWHO_CLIENT = serverSettings.getProperty("RemoteWhoClient", "89.108.93.140");
                RWHO_SEND_TRASH = Boolean.parseBoolean(serverSettings.getProperty("RemoteWhoSendTrash","False"));
                RWHO_MAX_ONLINE = Integer.parseInt(serverSettings.getProperty("RemoteWhoMaxOnline", "0"));
                RWHO_KEEP_STAT = Integer.parseInt(serverSettings.getProperty("RemoteOnlineKeepStat", "5"));
                RWHO_ONLINE_INCREMENT = Integer.parseInt(serverSettings.getProperty("RemoteOnlineIncrement", "0"));
                RWHO_PRIV_STORE_FACTOR = Float.parseFloat(serverSettings.getProperty("RemotePrivStoreFactor", "0"));
                RWHO_FORCE_INC = Integer.parseInt(serverSettings.getProperty("RemoteWhoForceInc", "0"));

                if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
                {
                    throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
            }
            
          //WEB
			try
			{
				Properties webSettings = new Properties();
				InputStream is = new FileInputStream(new File(WEB_FILE));
				webSettings.load(is);
				is.close();
				
				WEB_SERVER_ENABLE = Boolean.valueOf(webSettings.getProperty("EnableWebServer", "true"));
				WEB_SERVER_PORT = Integer.parseInt(webSettings.getProperty("WebServerPort", "7778"));
				WEB_SERVER_ROOT = webSettings.getProperty("WebServerRoot", "./data/webserver");
				USE_FILE_CACHE = Boolean.valueOf(webSettings.getProperty("useFileCache", "true"));
				
				RRD_ENABLED = Boolean.valueOf(webSettings.getProperty("UseRRD", "True"));
				RRD_EXTENDED = Boolean.valueOf(webSettings.getProperty("UseExtendedRRD", "False"));
				RRD_GRAPH_AA = Boolean.valueOf(webSettings.getProperty("GraphAntiAliasing", "False"));
				RRD_PATH = webSettings.getProperty("RRDPath", "./config/");
				RRD_GRAPH_PATH = webSettings.getProperty("GraphPath", "./data/webserver/");
				RRD_REDRAW_TIME = Long.parseLong(webSettings.getProperty("GraphRedrawDelay", "300"));
				RRD_UPDATE_TIME = Long.parseLong(webSettings.getProperty("UpdateDelay", "30"));
				RRD_GRAPH_FORMAT = webSettings.getProperty("GraphFormat", "png");
				RRD_GRAPH_HEIGHT = Integer.parseInt(webSettings.getProperty("GraphHeight", "378"));
				RRD_GRAPH_WIDTH = Integer.parseInt(webSettings.getProperty("GraphWidth", "580"));
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + WEB_FILE + " File.");
			}
			
			
			
            try
            {
                Properties optionsSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(OPTIONS_FILE));
                optionsSettings.load(is);
                is.close();

				MOVE_BASED_KNOWNLIST			= Boolean.parseBoolean(optionsSettings.getProperty("MoveBasedKnownlist", "false"));
                EVERYBODY_HAS_ADMIN_RIGHTS      = Boolean.parseBoolean(optionsSettings.getProperty("EverybodyHasAdminRights", "false"));
                DEBUG                           = Boolean.parseBoolean(optionsSettings.getProperty("Debug", "false"));
                ASSERT                          = Boolean.parseBoolean(optionsSettings.getProperty("Assert", "false"));
                DEVELOPER                       = Boolean.parseBoolean(optionsSettings.getProperty("Developer", "false"));
                TEST_SERVER                     = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
                SERVER_LIST_TESTSERVER          = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
                SERVER_LIST_BRACKET             = Boolean.valueOf(optionsSettings.getProperty("ServerListBrackets", "false"));
                SERVER_LIST_CLOCK               = Boolean.valueOf(optionsSettings.getProperty("ServerListClock", "false"));
                SERVER_GMONLY                   = Boolean.valueOf(optionsSettings.getProperty("ServerGMOnly", "false"));
                AUTODESTROY_ITEM_AFTER          = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
                HERB_AUTO_DESTROY_TIME          = Integer.parseInt(optionsSettings.getProperty("AutoDestroyHerbTime","15"))*1000;
                PROTECTED_ITEMS                    = optionsSettings.getProperty("ListOfProtectedItems");
                LIST_PROTECTED_ITEMS = new FastList<Integer>();
                for (String id : PROTECTED_ITEMS.split(",")) 
                {
                    LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
                }
                DESTROY_DROPPED_PLAYER_ITEM        = Boolean.valueOf(optionsSettings.getProperty("DestroyPlayerDroppedItem", "false"));
                DESTROY_EQUIPABLE_PLAYER_ITEM    = Boolean.valueOf(optionsSettings.getProperty("DestroyEquipableItem", "false"));
                SAVE_DROPPED_ITEM                = Boolean.valueOf(optionsSettings.getProperty("SaveDroppedItem", "false"));
                EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD    = Boolean.valueOf(optionsSettings.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
                SAVE_DROPPED_ITEM_INTERVAL        = Integer.parseInt(optionsSettings.getProperty("SaveDroppedItemInterval", "0"))*60000;
                CLEAR_DROPPED_ITEM_TABLE        = Boolean.valueOf(optionsSettings.getProperty("ClearDroppedItemTable", "false"));
                PRECISE_DROP_CALCULATION        = Boolean.valueOf(optionsSettings.getProperty("PreciseDropCalculation", "True"));
                MULTIPLE_ITEM_DROP              = Boolean.valueOf(optionsSettings.getProperty("MultipleItemDrop", "True"));
                COORD_SYNCHRONIZE               = Integer.parseInt(optionsSettings.getProperty("CoordSynchronize", "-1"));
                ALLOW_WAREHOUSE                 = Boolean.valueOf(optionsSettings.getProperty("AllowWarehouse", "True"));
                WAREHOUSE_CACHE                 = Boolean.valueOf(optionsSettings.getProperty("WarehouseCache", "False"));
                WAREHOUSE_CACHE_TIME            = Integer.parseInt(optionsSettings.getProperty("WarehouseCacheTime", "15"));
                ALLOW_FREIGHT                   = Boolean.valueOf(optionsSettings.getProperty("AllowFreight", "True"));
                ALLOW_WEAR                      = Boolean.valueOf(optionsSettings.getProperty("AllowWear", "False"));
                WEAR_DELAY                      = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
                WEAR_PRICE                      = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
				ALLOW_WATER                     = Boolean.valueOf(optionsSettings.getProperty("Water", "true"));
				DUEL_ALLOW                      = Boolean.parseBoolean(optionsSettings.getProperty("DuelAllow", "true"));
                ALLOW_LOTTERY                   = Boolean.valueOf(optionsSettings.getProperty("AllowLottery", "False"));
                ALLOW_RACE                      = Boolean.valueOf(optionsSettings.getProperty("AllowRace", "False"));
                ALLOW_RENTPET                   = Boolean.valueOf(optionsSettings.getProperty("AllowRentPet", "False"));
                ALLOW_DISCARDITEM               = Boolean.valueOf(optionsSettings.getProperty("AllowDiscardItem", "True"));
                ALLOWFISHING                    = Boolean.valueOf(optionsSettings.getProperty("AllowFishing", "False"));
                ALLOW_BOAT                      = Boolean.valueOf(optionsSettings.getProperty("AllowBoat", "False"));
                ALLOW_CURSED_WEAPONS            = Boolean.valueOf(optionsSettings.getProperty("AllowCursedWeapons", "False"));
                ALLOW_L2WALKER_CLIENT           = L2WalkerAllowed.valueOf(optionsSettings.getProperty("AllowL2Walker", "False"));
                L2WALKER_REVISION               = Integer.parseInt(optionsSettings.getProperty("L2WalkerRevision", "537"));
                AUTOBAN_L2WALKER_ACC            = Boolean.valueOf(optionsSettings.getProperty("AutobanL2WalkerAcc", "False"));
                ACTIVATE_POSITION_RECORDER      = Boolean.valueOf(optionsSettings.getProperty("ActivatePositionRecorder", "False"));
                LOG_CHAT                        = Boolean.valueOf(optionsSettings.getProperty("LogChat", "false"));
                LOG_ITEMS                       = Boolean.valueOf(optionsSettings.getProperty("LogItems", "false"));
                GMAUDIT                         = Boolean.valueOf(optionsSettings.getProperty("GMAudit", "False"));
                COMMUNITY_TYPE                  = optionsSettings.getProperty("CommunityType", "old").toLowerCase();
                BBS_DEFAULT                     = optionsSettings.getProperty("BBSDefault", "_bbshome");
                SHOW_LEVEL_COMMUNITYBOARD       = Boolean.valueOf(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
                SHOW_STATUS_COMMUNITYBOARD      = Boolean.valueOf(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
                NAME_PAGE_SIZE_COMMUNITYBOARD   = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
                NAME_PER_ROW_COMMUNITYBOARD     = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));
                ZONE_TOWN                       = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));
                FORCE_INVENTORY_UPDATE          = Boolean.valueOf(optionsSettings.getProperty("ForceInventoryUpdate", "False"));
                AUTODELETE_INVALID_QUEST_DATA   = Boolean.valueOf(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
                THREAD_P_EFFECTS                = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeEffects", "6"));
                THREAD_P_GENERAL                = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeGeneral", "15"));
                GENERAL_PACKET_THREAD_CORE_SIZE         = Integer.parseInt(optionsSettings.getProperty("GeneralPacketThreadCoreSize", "4"));
                IO_PACKET_THREAD_CORE_SIZE          =Integer.parseInt(optionsSettings.getProperty("UrgentPacketThreadCoreSize", "2"));
                AI_MAX_THREAD                   = Integer.parseInt(optionsSettings.getProperty("AiMaxThread", "10"));
                GENERAL_THREAD_CORE_SIZE        = Integer.parseInt(optionsSettings.getProperty("GeneralThreadCoreSize", "4"));
                DELETE_DAYS                     = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));
                DEFAULT_PUNISH                  = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
                DEFAULT_PUNISH_PARAM            = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));
                LAZY_CACHE                      = Boolean.valueOf(optionsSettings.getProperty("LazyCache", "False"));
                GRIDS_ALWAYS_ON                 = Boolean.parseBoolean(optionsSettings.getProperty("GridsAlwaysOn", "False"));
                GRID_NEIGHBOR_TURNON_TIME       = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOnTime", "30"));
                GRID_NEIGHBOR_TURNOFF_TIME      = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOffTime", "300"));
                String[] split 					= optionsSettings.getProperty("MultisellList", "0").split(",");
                COMMUN_MULT_LIST = new ArrayList<Integer>();
                for (String id : split)
                {
                	COMMUN_MULT_LIST.add(Integer.valueOf(Integer.parseInt(id)));
                }
			}
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+OPTIONS_FILE+" File.");
            }

            try
            {
                Properties telnetSettings   = new Properties();
                InputStream is              = new FileInputStream(new File(TELNET_FILE));
                telnetSettings.load(is);
                is.close();

                IS_TELNET_ENABLED   = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+TELNET_FILE+" File.");
            }

            try
            {
                Properties idSettings   = new Properties();
                InputStream is          = new FileInputStream(new File(ID_CONFIG_FILE));
                idSettings.load(is);
                is.close();

                MAP_TYPE        = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
                SET_TYPE        = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
                IDFACTORY_TYPE  = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
                BAD_ID_CHECKING = Boolean.valueOf(idSettings.getProperty("BadIdChecking", "True"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+ID_CONFIG_FILE+" File.");
            }

			

            try
            {
                Properties otherSettings    = new Properties();
                InputStream is              = new FileInputStream(new File(OTHER_CONFIG_FILE));
                otherSettings.load(is);
                is.close();

                SHOW_NPC_CREST = Boolean.parseBoolean(otherSettings.getProperty("ShowNpcCrest","True"));
                USE_TRADE_ZONE = Boolean.parseBoolean(otherSettings.getProperty("TradeZone", "false"));
                DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
                ALLOW_GUARDS        = Boolean.valueOf(otherSettings.getProperty("AllowGuards", "False"));
                EFFECT_CANCELING    = Boolean.valueOf(otherSettings.getProperty("CancelLesserEffect", "True"));
                WYVERN_SPEED        = Integer.parseInt(otherSettings.getProperty("WyvernSpeed", "100"));
                STRIDER_SPEED       = Integer.parseInt(otherSettings.getProperty("StriderSpeed", "80"));
                INVENTORY_MAXIMUM_NO_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
                INVENTORY_MAXIMUM_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
                INVENTORY_MAXIMUM_GM    = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
                MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
                WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
                WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
                WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
                FREIGHT_SLOTS       = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));
                RAID_HP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "100")) /100;
                RAID_MP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "100")) /100;
                RAID_DEFENCE_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidDefenceMultiplier", "100")) /100;
                RAID_MINION_RESPAWN_TIMER  = Integer.parseInt(otherSettings.getProperty("RaidMinionRespawnTime", "300000"));
                RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
                RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));
                UNSTUCK_INTERVAL    = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));
                PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));
                PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerFakeDeathUpProtection", "0"));
                PARTY_XP_CUTOFF_METHOD  = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
                PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
                PARTY_XP_CUTOFF_LEVEL   = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));
                RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100;
                RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
                RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;
                RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False"));
                RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50"));
                MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
                MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));
                STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "true"));
                PET_RENT_NPC =  otherSettings.getProperty("ListPetRentNpc", "30827");
                LIST_PET_RENT_NPC = new FastList<Integer>();
                for (String id : PET_RENT_NPC.split(",")) {
                    LIST_PET_RENT_NPC.add(Integer.parseInt(id));
                }
                NONDROPPABLE_ITEMS        = otherSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");

                LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
                for (String id : NONDROPPABLE_ITEMS.split(",")) {
                    LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
                }

                ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));

                ALT_PRIVILEGES_ADMIN = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesAdmin", "False"));
                ALT_PRIVILEGES_SECURE_CHECK = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesSecureCheck", "True"));
                ALT_PRIVILEGES_DEFAULT_LEVEL = Integer.parseInt(otherSettings.getProperty("AltPrivilegesDefaultLevel", "100"));
                GM_HERO_AURA = Boolean.parseBoolean(otherSettings.getProperty("GMHeroAura", "True"));
                GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvulnerable", "True"));
                GM_STARTUP_INVISIBLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvisible", "True"));
                GM_STARTUP_SILENCE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupSilence", "True"));
                GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(otherSettings.getProperty("GMStartupAutoList", "True"));
                GM_ADMIN_MENU_STYLE = otherSettings.getProperty("GMAdminMenuStyle", "modern");
                PETITIONING_ALLOWED = Boolean.parseBoolean(otherSettings.getProperty("PetitioningAllowed", "True"));
                MAX_PETITIONS_PER_PLAYER = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPerPlayer", "5"));
                MAX_PETITIONS_PENDING = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPending", "25"));
                JAIL_IS_PVP       = Boolean.valueOf(otherSettings.getProperty("JailIsPvp", "True"));
                JAIL_DISABLE_CHAT = Boolean.valueOf(otherSettings.getProperty("JailDisableChat", "True"));

            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+OTHER_CONFIG_FILE+" File.");
            }

            try
            {
                Properties ratesSettings    = new Properties();
                InputStream is              = new FileInputStream(new File(RATES_CONFIG_FILE));
                ratesSettings.load(is);
                is.close();

                RATE_XP                         = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
                RATE_SP                         = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
                RATE_PARTY_XP                   = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
                RATE_PARTY_SP                   = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
                RATE_QUESTS_REWARD              = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1."));
                RATE_DROP_ADENA                 = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1."));
                RATE_CONSUMABLE_COST            = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
                RATE_DROP_ITEMS                 = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
				RATE_DROP_STONE                 = Float.parseFloat(ratesSettings.getProperty("RateDropStone", "1."));
				RATE_DROP_ITEMS_BY_RAID         = Float.parseFloat(ratesSettings.getProperty("RateDropBossItems", "1."));
				RATE_DROP_ITEMS_BY_GRAND        = Float.parseFloat(ratesSettings.getProperty("RateDropGrandBossItems", "1."));
                RATE_DROP_SPOIL                 = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
                RATE_DROP_MANOR                 = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
                RATE_DROP_QUEST                 = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1."));
                RATE_KARMA_EXP_LOST             = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
                RATE_SIEGE_GUARDS_PRICE         = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));
                RATE_DROP_COMMON_HERBS             = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15."));
                RATE_DROP_MP_HP_HERBS           = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10."));
                RATE_DROP_GREATER_HERBS         = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4."));
                RATE_DROP_SUPERIOR_HERBS        = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.8"))*10;
                RATE_DROP_SPECIAL_HERBS         = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.2"))*10;
                PLAYER_DROP_LIMIT               = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
                PLAYER_RATE_DROP                = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
                PLAYER_RATE_DROP_ITEM           = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
                PLAYER_RATE_DROP_EQUIP          = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
                PLAYER_RATE_DROP_EQUIP_WEAPON   = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));
                PET_XP_RATE                     = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
                PET_FOOD_RATE                   = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
                SINEATER_XP_RATE                = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1."));
                KARMA_DROP_LIMIT                = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
                KARMA_RATE_DROP                 = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
                KARMA_RATE_DROP_ITEM            = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
                KARMA_RATE_DROP_EQUIP           = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
                KARMA_RATE_DROP_EQUIP_WEAPON    = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));

            }
            catch (Exception e) {
                e.printStackTrace();
                throw new Error("Failed to Load "+RATES_CONFIG_FILE+" File.");
            }

            try
            {
                Properties altSettings  = new Properties();
                InputStream is          = new FileInputStream(new File(ALT_SETTINGS_FILE));
                altSettings.load(is);
                is.close();

                ALT_GAME_TIREDNESS      = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "false"));
                ALT_GAME_CREATION       = Boolean.parseBoolean(altSettings.getProperty("AltGameCreation", "false"));
                ALT_GAME_CREATION_SPEED = Double.parseDouble(altSettings.getProperty("AltGameCreationSpeed", "1"));
                ALT_GAME_CREATION_XP_RATE=Double.parseDouble(altSettings.getProperty("AltGameCreationRateXp", "1"));
                ALT_GAME_CREATION_SP_RATE=Double.parseDouble(altSettings.getProperty("AltGameCreationRateSp", "1"));
                ALT_BLACKSMITH_USE_RECIPES=Boolean.parseBoolean(altSettings.getProperty("AltBlacksmithUseRecipes", "true"));
                ALT_GAME_SKILL_LEARN    = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "false"));
                AUTO_LEARN_SKILLS       = Boolean.parseBoolean(altSettings.getProperty("AutoLearnSkills", "false"));
                AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(altSettings.getProperty("AutoLearnDivineInspiration", "False"));
                ALT_GAME_CANCEL_BOW     = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
                ALT_GAME_CANCEL_CAST    = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
                
                ALT_PERFECT_SHLD_BLOCK  = Integer.parseInt(altSettings.getProperty("AltPerfectShieldBlockRate", "10"));
                ALT_GAME_DELEVEL        = Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
                ALT_GAME_MAGICFAILURES  = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "false"));
                ALT_GAME_MOB_ATTACK_AI  = Boolean.parseBoolean(altSettings.getProperty("AltGameMobAttackAI", "false"));
	            ALT_MOB_AGRO_IN_PEACEZONE  = Boolean.parseBoolean(altSettings.getProperty("AltMobAgroInPeaceZone", "true"));
                ALT_GAME_EXPONENT_XP    = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
                ALT_GAME_EXPONENT_SP    = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
                ALT_GAME_FREIGHTS       = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "false"));
                ALT_GAME_FREIGHT_PRICE  = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
                ALT_PARTY_RANGE 		= Integer.parseInt(altSettings.getProperty("AltPartyRange", "1600"));
                ALT_PARTY_RANGE2  		= Integer.parseInt(altSettings.getProperty("AltPartyRange2", "1400"));
                REMOVE_CASTLE_CIRCLETS  = Boolean.parseBoolean(altSettings.getProperty("RemoveCastleCirclets", "true"));
                IS_CRAFTING_ENABLED     = Boolean.parseBoolean(altSettings.getProperty("CraftingEnabled", "true"));
                LIFE_CRYSTAL_NEEDED     = Boolean.parseBoolean(altSettings.getProperty("LifeCrystalNeeded", "true"));
                SP_BOOK_NEEDED          = Boolean.parseBoolean(altSettings.getProperty("SpBookNeeded", "true"));
                ES_SP_BOOK_NEEDED       = Boolean.parseBoolean(altSettings.getProperty("EnchantSkillSpBookNeeded","true"));
                AUTO_LOOT               = altSettings.getProperty("AutoLoot").equalsIgnoreCase("True");
				BOSS_AUTO_LOOT          = altSettings.getProperty("BossAutoLoot").equalsIgnoreCase("True");
                AUTO_LOOT_HERBS         = altSettings.getProperty("AutoLootHerbs").equalsIgnoreCase("True");
                ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE    = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
                ALT_GAME_KARMA_PLAYER_CAN_SHOP                      = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanShop", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_USE_GK                    = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseGK", "false"));
                ALT_GAME_KARMA_PLAYER_CAN_TELEPORT                  = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_TRADE                     = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE             = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
                ALT_GAME_FREE_TELEPORT                              = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
                ALT_RECOMMEND                                       = Boolean.parseBoolean(altSettings.getProperty("AltRecommend", "False"));
                ALT_GAME_SUBCLASS_WITHOUT_QUESTS                    = Boolean.parseBoolean(altSettings.getProperty("AltSubClassWithoutQuests", "False"));
                SUBCLASS_WITH_ITEM_AND_NO_QUEST 					= Boolean.parseBoolean(altSettings.getProperty("SubclassWithItemAndNoQuest", "False"));
                ALT_GAME_VIEWNPC                                    = Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
                ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH                = Boolean.parseBoolean(altSettings.getProperty("AltMembersCanWithdrawFromClanWH", "False"));
                ALT_MAX_NUM_OF_CLANS_IN_ALLY                        = Integer.parseInt(altSettings.getProperty("AltMaxNumOfClansInAlly", "3"));
                DWARF_RECIPE_LIMIT                                  = Integer.parseInt(altSettings.getProperty("DwarfRecipeLimit","50"));
                COMMON_RECIPE_LIMIT                                 = Integer.parseInt(altSettings.getProperty("CommonRecipeLimit","50"));
                ALT_CLAN_MEMBERS_FOR_WAR    = Integer.parseInt(altSettings.getProperty("AltClanMembersForWar", "15"));
                ALT_CLAN_JOIN_DAYS          = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAClan", "5"));
                ALT_CLAN_CREATE_DAYS        = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateAClan", "10"));
                ALT_CLAN_DISSOLVE_DAYS      = Integer.parseInt(altSettings.getProperty("DaysToPassToDissolveAClan", "7"));
                ALT_ALLY_JOIN_DAYS_WHEN_LEAVED       = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
                ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED    = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
                ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED  = Integer.parseInt(altSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
                ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED  = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));

                ALT_LOTTERY_PRIZE                = Integer.parseInt(altSettings.getProperty("AltLotteryPrize","50000"));
                ALT_LOTTERY_TICKET_PRICE         = Integer.parseInt(altSettings.getProperty("AltLotteryTicketPrice","2000"));
                ALT_LOTTERY_5_NUMBER_RATE        = Float.parseFloat(altSettings.getProperty("AltLottery5NumberRate","0.6"));
                ALT_LOTTERY_4_NUMBER_RATE        = Float.parseFloat(altSettings.getProperty("AltLottery4NumberRate","0.2"));
                ALT_LOTTERY_3_NUMBER_RATE        = Float.parseFloat(altSettings.getProperty("AltLottery3NumberRate","0.2"));
                ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(altSettings.getProperty("AltLottery2and1NumberPrize","200"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+ALT_SETTINGS_FILE+" File.");
            }

            try
            {
                Properties clanhallSettings  = new Properties();
                InputStream is          = new FileInputStream(new File(CLANHALL_CONFIG_FILE));
                clanhallSettings.load(is);
                is.close();
				CH_RATE                                             = Integer.valueOf(clanhallSettings.getProperty("ClanHallRate", "604800000"));
                CH_TELE_FEE_RATIO                                   = Long.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
                CH_TELE1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
                CH_TELE2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
                CH_SUPPORT_FEE_RATIO                                = Long.valueOf(clanhallSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
                CH_SUPPORT1_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl1", "86400000"));
                CH_SUPPORT2_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl2", "86400000"));
                CH_SUPPORT3_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl3", "86400000"));
                CH_SUPPORT4_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl4", "86400000"));
                CH_SUPPORT5_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl5", "86400000"));
                CH_SUPPORT6_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl6", "86400000"));
                CH_SUPPORT7_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl7", "86400000"));
                CH_SUPPORT8_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl8", "86400000"));
                CH_MPREG_FEE_RATIO                                  = Long.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
                CH_MPREG1_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
                CH_MPREG2_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
                CH_MPREG3_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
                CH_MPREG4_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000"));
                CH_MPREG5_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000"));
                CH_HPREG_FEE_RATIO                                  = Long.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
                CH_HPREG1_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
                CH_HPREG2_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
                CH_HPREG3_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
                CH_HPREG4_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000"));
                CH_HPREG5_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000"));
                CH_HPREG6_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000"));
                CH_HPREG7_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000"));
                CH_HPREG8_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000"));
                CH_HPREG9_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000"));
                CH_HPREG10_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000"));
                CH_HPREG11_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000"));
                CH_HPREG12_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000"));
                CH_HPREG13_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000"));
                CH_EXPREG_FEE_RATIO                                 = Long.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
                CH_EXPREG1_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
                CH_EXPREG2_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
                CH_EXPREG3_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
                CH_EXPREG4_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000"));
                CH_EXPREG5_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000"));
                CH_EXPREG6_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000"));
                CH_EXPREG7_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000"));
                CH_ITEM_FEE_RATIO                                   = Long.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
                CH_ITEM1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
                CH_ITEM2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
                CH_ITEM3_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
                CH_CURTAIN_FEE_RATIO                                = Long.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeRation", "86400000"));
                CH_CURTAIN1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000"));
                CH_CURTAIN2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000"));
                CH_FRONT_FEE_RATIO                                = Long.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000"));
                CH_FRONT1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000"));
                CH_FRONT2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+CLANHALL_CONFIG_FILE+" File.");
            }
			try
            {
				Properties additionsSettings  = new Properties();
                InputStream is          = new FileInputStream(new File(ADDITIONS_CONFIG_FILE));
                additionsSettings.load(is);
                is.close();
                
               	CTF_EVEN_TEAMS = additionsSettings.getProperty("CTFEvenTeams", "BALANCE");
				CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(additionsSettings.getProperty("CTFAllowInterference", "false"));
				CTF_ALLOW_SUMMON = Boolean.parseBoolean(additionsSettings.getProperty("CTFAllowSummon", "false"));
				CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(additionsSettings.getProperty("CTFOnStartRemoveAllEffects", "true"));
				CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(additionsSettings.getProperty("CTFOnStartUnsummonPet", "true"));
				CTF_REVIVE_RECOVERY = Boolean.parseBoolean(additionsSettings.getProperty("CTFReviveRecovery", "false"));
				CTF_RND_SPAWNXMIN = Integer.parseInt(additionsSettings.getProperty("CTFRndSpawnXMin", "0"));
                CTF_RND_SPAWNXMAX = Integer.parseInt(additionsSettings.getProperty("CTFRndSpawnXMax", "0"));
                CTF_RND_SPAWNYMIN = Integer.parseInt(additionsSettings.getProperty("CTFRndSpawnYMin", "0"));
                CTF_RND_SPAWNYMAX = Integer.parseInt(additionsSettings.getProperty("CTFRndSpawnYMax", "0"));
                CTF_SPAWN_Z = Integer.parseInt(additionsSettings.getProperty("CTFSpawnZ", "0"));
				

				FS_TIME_ATTACK = Integer.parseInt(additionsSettings.getProperty("TimeOfAttack", "50"));
				FS_TIME_COOLDOWN = Integer.parseInt(additionsSettings.getProperty("TimeOfCoolDown", "5"));
				FS_TIME_ENTRY = Integer.parseInt(additionsSettings.getProperty("TimeOfEntry", "3"));
				FS_TIME_WARMUP = Integer.parseInt(additionsSettings.getProperty("TimeOfWarmUp", "2"));
				FS_PARTY_MEMBER_COUNT = Integer.parseInt(additionsSettings.getProperty("NumberOfNecessaryPartyMembers", "4"));
				if (FS_TIME_ATTACK <= 0)
					FS_TIME_ATTACK = 50;
				if (FS_TIME_COOLDOWN <= 0)
					FS_TIME_COOLDOWN = 5;
				if (FS_TIME_ENTRY <= 0)
					FS_TIME_ENTRY = 3;
				if (FS_TIME_ENTRY <= 0)
					FS_TIME_ENTRY = 3;
				if (FS_TIME_ENTRY <= 0)
					FS_TIME_ENTRY = 3;

				pccafe_min_lvl = Integer.parseInt(additionsSettings.getProperty("PcCafeMinLevel", "0"));
			    pccafe_event = Boolean.valueOf(additionsSettings.getProperty("PcCafeEvent", "True")); 
			    pccafe_score_min = Integer.parseInt(additionsSettings.getProperty("PcCafeScoreMin", "45"));
			    pccafe_score_max = Integer.parseInt(additionsSettings.getProperty("PcCafeScoreMax", "150"));
			    pccafe_score_double = Integer.parseInt(additionsSettings.getProperty("PcCafeScoreDouble", "15"));
			    pccafe_interval = Integer.parseInt(additionsSettings.getProperty("PcCafeInterval", "300"));
				ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(additionsSettings.getProperty("AltRequireCastleForDawn", "False"));
                ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(additionsSettings.getProperty("AltRequireClanCastle", "False"));
                ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(additionsSettings.getProperty("AltFestivalMinPlayer", "5"));
                ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(additionsSettings.getProperty("AltMaxPlayerContrib", "1000000"));
                ALT_FESTIVAL_MANAGER_START = Long.parseLong(additionsSettings.getProperty("AltFestivalManagerStart", "120000"));
                ALT_FESTIVAL_LENGTH = Long.parseLong(additionsSettings.getProperty("AltFestivalLength", "1080000"));
                ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(additionsSettings.getProperty("AltFestivalCycleLength", "2280000"));
                ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(additionsSettings.getProperty("AltFestivalFirstSpawn", "120000"));
                ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(additionsSettings.getProperty("AltFestivalFirstSwarm", "300000"));
                ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(additionsSettings.getProperty("AltFestivalSecondSpawn", "540000"));
                ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(additionsSettings.getProperty("AltFestivalSecondSwarm", "720000"));
                ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(additionsSettings.getProperty("AltFestivalChestSpawn", "900000"));
				RIFT_MIN_PARTY_SIZE              = Integer.parseInt(additionsSettings.getProperty("RiftMinPartySize", "5"));
                RIFT_MAX_JUMPS                   = Integer.parseInt(additionsSettings.getProperty("MaxRiftJumps", "4"));
				RIFT_SPAWN_DELAY                 = Integer.parseInt(additionsSettings.getProperty("RiftSpawnDelay", "10000"));
                RIFT_AUTO_JUMPS_TIME_MIN         = Integer.parseInt(additionsSettings.getProperty("AutoJumpsDelayMin", "480"));
                RIFT_AUTO_JUMPS_TIME_MAX         = Integer.parseInt(additionsSettings.getProperty("AutoJumpsDelayMax", "600"));
                RIFT_ENTER_COST_RECRUIT          = Integer.parseInt(additionsSettings.getProperty("RecruitCost", "18"));
                RIFT_ENTER_COST_SOLDIER          = Integer.parseInt(additionsSettings.getProperty("SoldierCost", "21"));
                RIFT_ENTER_COST_OFFICER          = Integer.parseInt(additionsSettings.getProperty("OfficerCost", "24"));
                RIFT_ENTER_COST_CAPTAIN          = Integer.parseInt(additionsSettings.getProperty("CaptainCost", "27"));
                RIFT_ENTER_COST_COMMANDER        = Integer.parseInt(additionsSettings.getProperty("CommanderCost", "30"));
                RIFT_ENTER_COST_HERO             = Integer.parseInt(additionsSettings.getProperty("HeroCost", "33"));
                RIFT_BOSS_ROOM_TIME_MUTIPLY      = Float.parseFloat(additionsSettings.getProperty("BossRoomTimeMultiply", "1.5"));
                ALT_OLY_WEEK 								= Boolean.parseBoolean(additionsSettings.getProperty("OlympiadWeekPeriod", "False"));
                ALT_OLY_START_TIME							= Integer.parseInt(additionsSettings.getProperty("AltOlyStartTime", "18"));
                ALT_OLY_MIN									= Integer.parseInt(additionsSettings.getProperty("AltOlyMin","00"));
                ALT_OLY_CPERIOD								= Long.parseLong(additionsSettings.getProperty("AltOlyCPeriod","21600000"));
                ALT_OLY_BATTLE								= Long.parseLong(additionsSettings.getProperty("AltOlyBattle","360000"));
                ALT_OLY_BWAIT								= Long.parseLong(additionsSettings.getProperty("AltOlyBWait","600000"));
                ALT_OLY_IWAIT								= Long.parseLong(additionsSettings.getProperty("AltOlyIWait","300000"));
                ALT_OLY_WPERIOD								= Long.parseLong(additionsSettings.getProperty("AltOlyWPeriod","604800000"));
                ALT_OLY_VPERIOD								= Long.parseLong(additionsSettings.getProperty("AltOlyVPeriod","86400000"));
                ALT_OLY_CLASSED								= Integer.parseInt(additionsSettings.getProperty("AltOlyClassedParticipants","5"));
                ALT_OLY_NONCLASSED							= Integer.parseInt(additionsSettings.getProperty("AltOlyNonClassedParticipants","9"));
                ALT_OLY_BATTLE_REWARD_ITEM					= Integer.parseInt(additionsSettings.getProperty("AltOlyBattleRewItem","6651"));
                ALT_OLY_CLASSED_RITEM_C						= Integer.parseInt(additionsSettings.getProperty("AltOlyClassedRewItemCount","50"));
                ALT_OLY_NONCLASSED_RITEM_C					= Integer.parseInt(additionsSettings.getProperty("AltOlyNonClassedRewItemCount","30"));
                ALT_OLY_COMP_RITEM							= Integer.parseInt(additionsSettings.getProperty("AltOlyCompRewItem","6651"));
                ALT_OLY_GP_PER_POINT						= Integer.parseInt(additionsSettings.getProperty("AltOlyGPPerPoint","1000"));
                ALT_OLY_MIN_POINT_FOR_EXCH					= Integer.parseInt(additionsSettings.getProperty("AltOlyMinPointForExchange","50"));
                ALT_OLY_HERO_POINTS							= Integer.parseInt(additionsSettings.getProperty("AltOlyHeroPoints","300"));
                ALT_OLY_RESTRICTED_ITEMS					= additionsSettings.getProperty("AltOlyRestrictedItems","0");
                ALT_OLY_MAX_ENCHANT							= Integer.parseInt(additionsSettings.getProperty("AltOlyMaxEnchant","-1"));
                
                LIST_OLY_RESTRICTED_ITEMS					= new FastList<Integer>();
				for (String id : ALT_OLY_RESTRICTED_ITEMS.split(","))
				{
					LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
				}
				ALLOW_MANOR                     = Boolean.parseBoolean(additionsSettings.getProperty("AllowManor", "False"));
				ALT_MANOR_REFRESH_TIME          = Integer.parseInt(additionsSettings.getProperty("AltManorRefreshTime","20"));
    	        ALT_MANOR_REFRESH_MIN           = Integer.parseInt(additionsSettings.getProperty("AltManorRefreshMin","00"));
    	        ALT_MANOR_APPROVE_TIME          = Integer.parseInt(additionsSettings.getProperty("AltManorApproveTime","6"));
    	        ALT_MANOR_APPROVE_MIN           = Integer.parseInt(additionsSettings.getProperty("AltManorApproveMin","00"));
    	        ALT_MANOR_MAINTENANCE_PERIOD    = Integer.parseInt(additionsSettings.getProperty("AltManorMaintenancePeriod","360000"));
    	        ALT_MANOR_SAVE_ALL_ACTIONS      = Boolean.parseBoolean(additionsSettings.getProperty("AltManorSaveAllActions","false"));
    	        ALT_MANOR_SAVE_PERIOD_RATE      = Integer.parseInt(additionsSettings.getProperty("AltManorSavePeriodRate","2"));
                L2JMOD_ALLOW_WEDDING = Boolean.valueOf(additionsSettings.getProperty("AllowWedding", "False"));
                L2JMOD_WEDDING_PRICE = Integer.parseInt(additionsSettings.getProperty("WeddingPrice", "250000000"));
                L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(additionsSettings.getProperty("WeddingPunishInfidelity", "True"));
                L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(additionsSettings.getProperty("WeddingTeleport", "True"));
                L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(additionsSettings.getProperty("WeddingTeleportPrice", "50000"));
                L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(additionsSettings.getProperty("WeddingTeleportDuration", "60"));
                L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(additionsSettings.getProperty("WeddingAllowSameSex", "False"));
                L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(additionsSettings.getProperty("WeddingFormalWear", "True"));
                L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(additionsSettings.getProperty("WeddingDivorceCosts", "20"));
				COLOR_WEDDING_NAME = Boolean.parseBoolean(additionsSettings.getProperty("ColorWeddingName", "True"));
                COLOR_WEDDING_NAMES = Integer.decode((new StringBuilder()).append("0x").append(additionsSettings.getProperty("WeddingNameColor", "FFFF00")).toString()).intValue();
                COLOR_WEDDING_NAMES_GEY = Integer.decode((new StringBuilder()).append("0x").append(additionsSettings.getProperty("WeddingNameGeyColor", "FF0000")).toString()).intValue();
                COLOR_WEDDING_NAMES_LIZ = Integer.decode((new StringBuilder()).append("0x").append(additionsSettings.getProperty("WeddingNameLizColor", "F0F000")).toString()).intValue();
                CHANCE_WIN = Integer.parseInt(additionsSettings.getProperty("ChanceWin","20"));
                STAWKAID_COIN = Integer.parseInt(additionsSettings.getProperty("StawkaCoinId","4037"));
				STAWKA_COIN_AMOUNT = Integer.parseInt(additionsSettings.getProperty("StawkaCoinCount","50"));
				STAWKAID_ADENA = Integer.parseInt(additionsSettings.getProperty("StawkaAdenaId","57"));
				STAWKA_ADENA_AMOUNT = Integer.parseInt(additionsSettings.getProperty("StawkaAdenaCount","100000"));
				WIN_COIN = Integer.parseInt(additionsSettings.getProperty("WinCoinId","4037"));
				WIN_COIN_AMOUNT = Integer.parseInt(additionsSettings.getProperty("WinCoinCount","100"));
				WIN_ADENA = Integer.parseInt(additionsSettings.getProperty("WinAdenaId","57"));
				WIN_ADENA_AMOUNT = Integer.parseInt(additionsSettings.getProperty("WinAdenaCount","100000"));
				CHAMPION_ENABLE = Boolean.parseBoolean(additionsSettings.getProperty("ChampionEnable", "False"));
				CHAMPION_FREQUENCY = Integer.parseInt(additionsSettings.getProperty("ChampionFrequency", "0"));
				CHAMPION_MIN_LVL = Integer.parseInt(additionsSettings.getProperty("ChampionMinLevel", "20"));
				CHAMPION_MAX_LVL = Integer.parseInt(additionsSettings.getProperty("ChampionMaxLevel", "60"));
				CHAMPION_HP = Integer.parseInt(additionsSettings.getProperty("ChampionHp", "7"));
				CHAMPION_HP_REGEN = Float.parseFloat(additionsSettings.getProperty("ChampionHpRegen", "1."));
				CHAMPION_REWARDS = Integer.parseInt(additionsSettings.getProperty("ChampionRewards", "8"));
				CHAMPION_XPSP = Integer.parseInt(additionsSettings.getProperty("ChampionXPSP", "8"));
				CHAMPION_ADENAS_REWARDS = Integer.parseInt(additionsSettings.getProperty("ChampionAdenasRewards", "1"));
				CHAMPION_ATK = Float.parseFloat(additionsSettings.getProperty("ChampionAtk", "1."));
				CHAMPION_SPD_ATK = Float.parseFloat(additionsSettings.getProperty("ChampionSpdAtk", "1."));
				CHAMPION_REWARD = Integer.parseInt(additionsSettings.getProperty("ChampionRewardItem", "0"));
				CHAMPION_REWARD_ID = Integer.parseInt(additionsSettings.getProperty("ChampionRewardItemID", "6393"));
				CHAMPION_REWARD_QTY = Integer.parseInt(additionsSettings.getProperty("ChampionRewardItemQty", "1"));
				CHAMPION_TITLE = additionsSettings.getProperty("ChampionTitle", "Champion");
				CHAMPION_AURA = Boolean.parseBoolean(additionsSettings.getProperty("ChampionAura", "False"));
				
                TVT_EVENT_ENABLED = Boolean.parseBoolean(additionsSettings.getProperty("TvTEventEnabled", "False"));
                TVT_EVENT_INTERVAL = additionsSettings.getProperty("TvTEventInterval", "20:00").split(",");
                TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(additionsSettings.getProperty("TvTEventParticipationTime", "3600"));
                TVT_EVENT_RUNNING_TIME = Integer.parseInt(additionsSettings.getProperty("TvTEventRunningTime", "1800"));
                TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(additionsSettings.getProperty("TvTEventParticipationNpcId", "0"));

                if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
                {
                  TVT_EVENT_ENABLED = false;
                  _log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcId");
                }
                else
                {
                  String[] propertySplit = additionsSettings.getProperty("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");
                  if (propertySplit.length < 3)
                  {
                    TVT_EVENT_ENABLED = false;
                    _log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
                  }
                  else
                  {
                    TVT_EVENT_REWARDS = new ArrayList<int[]>();
                    TVT_DOORS_IDS = new ArrayList<Integer>();
                    TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
                    TVT_EVENT_TEAM_1_COORDINATES = new int[3];
                    TVT_EVENT_TEAM_2_COORDINATES = new int[3];
                    TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
                    TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
                    TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
                    if (propertySplit.length == 4)
                      TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
                    TVT_EVENT_MIN_PLAYERS_IN_TEAMS = Integer.parseInt(additionsSettings.getProperty("TvTEventMinPlayersInTeams", "1"));
                    TVT_EVENT_MAX_PLAYERS_IN_TEAMS = Integer.parseInt(additionsSettings.getProperty("TvTEventMaxPlayersInTeams", "20"));
                    TVT_EVENT_MIN_LVL = (byte)Integer.parseInt(additionsSettings.getProperty("TvTEventMinPlayerLevel", "1"));
                    TVT_EVENT_MAX_LVL = (byte)Integer.parseInt(additionsSettings.getProperty("TvTEventMaxPlayerLevel", "80"));
                    TVT_EVENT_RESPAWN_TELEPORT_DELAY = Integer.parseInt(additionsSettings.getProperty("TvTEventRespawnTeleportDelay", "20"));
                    TVT_EVENT_START_LEAVE_TELEPORT_DELAY = Integer.parseInt(additionsSettings.getProperty("TvTEventStartLeaveTeleportDelay", "20"));
                    TVT_EVENT_EFFECTS_REMOVAL = Integer.parseInt(additionsSettings.getProperty("TvTEventEffectsRemoval", "0"));
                    TVT_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(additionsSettings.getProperty("TvTAllowVoicedInfoCommand", "False"));
                    TVT_ALLOW_REGISTER_VOICED_COMMAND = Boolean.parseBoolean(additionsSettings.getProperty("TvTAllowRegisterVoicedCommand", "false"));
                    TVT_SAME_IP = Boolean.parseBoolean(additionsSettings.getProperty("TvTEnableDualBoxProtection", "False"));
                    TVT_RESTORE_PLAYER_POS = Boolean.parseBoolean(additionsSettings.getProperty("TvTRestorePlayerOldPosition", "False"));
                    String[] split = additionsSettings.getProperty("TvTRestrictedItems", "0").split(",");
                    LIST_TVT_RESTRICTED_ITEMS = new ArrayList<Integer>();
                    for (String id : split)
                    {
                      LIST_TVT_RESTRICTED_ITEMS.add(Integer.valueOf(Integer.parseInt(id)));
                    }
                    TVT_REWARD_ONLY_KILLERS = Boolean.parseBoolean(additionsSettings.getProperty("TvTRewardOnlyKillers", "False"));
                    TVT_EVENT_ALLOW_PEACE_ATTACK = Boolean.parseBoolean(additionsSettings.getProperty("TvTAllowPeaceAttack", "True"));
                    TVT_EVENT_ALLOW_FLAG = Boolean.parseBoolean(additionsSettings.getProperty("TvTAllowFlag", "True"));
                    TVT_EVENT_RESTORE_CPHPMP = Boolean.parseBoolean(additionsSettings.getProperty("TvTRestoreCPHPMP", "False"));
                    TVT_EVENT_TEAM_1_NAME = additionsSettings.getProperty("TvTEventTeam1Name", "Team1");
                    propertySplit = additionsSettings.getProperty("TvTEventTeam1Coordinates", "0,0,0").split(",");
                    if (propertySplit.length < 3)
                    {
                      TVT_EVENT_ENABLED = false;
                      _log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam1Coordinates");
                    }
                    else
                    {
                      TVT_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
                      TVT_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
                      TVT_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
                      TVT_EVENT_TEAM_2_NAME = additionsSettings.getProperty("TvTEventTeam2Name", "Team2");
                      propertySplit = additionsSettings.getProperty("TvTEventTeam2Coordinates", "0,0,0").split(",");
                      if (propertySplit.length < 3)
                      {
                        TVT_EVENT_ENABLED = false;
                        _log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam2Coordinates");
                      }
                      else
                      {
                        TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
                        TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
                        TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
                        propertySplit = additionsSettings.getProperty("TvTEventParticipationFee", "0,0").split(",");
                        try
                        {
                          TVT_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
                          TVT_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
                        }
                        catch (NumberFormatException nfe)
                        {
                          if (propertySplit.length > 0)
                            _log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationFee");
                        }
                        propertySplit = additionsSettings.getProperty("TvTEventReward", "57,100000").split(";");
                        for (String reward : propertySplit)
                        {
                          String[] rewardSplit = reward.split(",");
                          if (rewardSplit.length != 2) {
                            _log.warning(StringUtil.concat(new String[] { "TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\"" }));
                          }
                          else {
                            try
                            {
                              TVT_EVENT_REWARDS.add(new int[] { Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1]) });
                            }
                            catch (NumberFormatException nfe)
                            {
                              if (!reward.isEmpty()) {
                                _log.warning(StringUtil.concat(new String[] { "TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\"" }));
                              }
                            }
                          }
                        }
                        TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = Boolean.parseBoolean(additionsSettings.getProperty("TvTEventTargetTeamMembersAllowed", "True"));
                        TVT_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(additionsSettings.getProperty("TvTEventScrollsAllowed", "False"));
                        TVT_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(additionsSettings.getProperty("TvTEventPotionsAllowed", "False"));
                        TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(additionsSettings.getProperty("TvTEventSummonByItemAllowed", "False"));
                        TVT_REWARD_TEAM_TIE = Boolean.parseBoolean(additionsSettings.getProperty("TvTRewardTeamTie", "False"));
                        
                        propertySplit = additionsSettings.getProperty("TvTDoors", "").split(";");
                        for (String door : propertySplit)
                        {
                          try
                          {
                            TVT_DOORS_IDS.add(Integer.valueOf(Integer.parseInt(door)));
                          }
                          catch (NumberFormatException nfe)
                          {
                            if (!door.isEmpty()) {
                              _log.warning(StringUtil.concat(new String[] { "TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToClose \"", door, "\"" }));
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
                throw new Error("Failed to Load "+ADDITIONS_CONFIG_FILE+" File.");
            }

            try
            {
                Properties pvpSettings      = new Properties();
                InputStream is              = new FileInputStream(new File(PVP_CONFIG_FILE));
                pvpSettings.load(is);
                is.close();
				PVPPK_PROTECT = Integer.parseInt(pvpSettings.getProperty("PvpPkProtect", "30"));
				ATTACK_STANCE_TASKS = Integer.parseInt(pvpSettings.getProperty("AttackStanceTasks", "15000"));
				ANONS_PVP_PK = Integer.parseInt(pvpSettings.getProperty("AnonsPvpKill", "3"));
                REGION_PVP_PK = Boolean.parseBoolean(pvpSettings.getProperty("RegionPvpKill", "True"));
                KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
                KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
                KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
                KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
                KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
                KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));
                KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
                KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
                KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");
                KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
                for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(",")) {
                    KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
                }
                KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
                for (String id : KARMA_NONDROPPABLE_ITEMS.split(",")) {
                    KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
                }
                PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "15000"));
                PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "30000"));
				PVP_ITEM_ID = Integer.parseInt(pvpSettings.getProperty("PvPItemId", "4037"));
                PVP_ITEM_COUNT = Integer.parseInt(pvpSettings.getProperty("PvPItemCount", "0"));
                PVP_EXP_COUNT = Long.parseLong(pvpSettings.getProperty("PvPExpCount", "0"));
                PVP_SP_COUNT = Integer.parseInt(pvpSettings.getProperty("PvPSPCount", "0"));
                PVP_LEVEL_DIFFERENCE = Integer.parseInt(pvpSettings.getProperty("PvPLevelDifference", "10"));
                PVP_STRICT_IP = Boolean.parseBoolean(pvpSettings.getProperty("StrictPvPIp", "True"));
                PK_ITEM_ID = Integer.parseInt(pvpSettings.getProperty("PKItemId", "4037"));
                PK_ITEM_COUNT = Integer.parseInt(pvpSettings.getProperty("PKItemCount", "0"));
                PK_EXP_COUNT = Long.parseLong(pvpSettings.getProperty("PKExpCount", "0"));
                PK_SP_COUNT = Integer.parseInt(pvpSettings.getProperty("PKSPCount", "0"));
                PK_LEVEL_DIFFERENCE = Integer.parseInt(pvpSettings.getProperty("PKLevelDifference", "10"));
                PK_STRICT_IP = Boolean.parseBoolean(pvpSettings.getProperty("StrictPKIp", "True"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+PVP_CONFIG_FILE+" File.");
            }

            try
            {
                Properties gmSettings   = new Properties();
                InputStream is          = new FileInputStream(new File(GM_ACCESS_FILE));
                gmSettings.load(is);
                is.close();
                
                GM_MASTERS = gmSettings.getProperty("ListOfGMs", "0");
                LIST_GM_MASTERS = new FastList<Integer>();
                for (String id : GM_MASTERS.split(","))
                {
                LIST_GM_MASTERS.add(Integer.parseInt(id));
                }
                GM_ACCESSLEVEL  = Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
                GM_MIN          = Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
                GM_ALTG_MIN_LEVEL = Integer.parseInt(gmSettings.getProperty("GMCanAltG", "100"));
                GM_ANNOUNCE     = Integer.parseInt(gmSettings.getProperty("GMCanAnnounce", "100"));
                GM_BAN          = Integer.parseInt(gmSettings.getProperty("GMCanBan", "100"));
                GM_BAN_CHAT     = Integer.parseInt(gmSettings.getProperty("GMCanBanChat", "100"));
                GM_CREATE_ITEM  = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
                GM_DELETE       = Integer.parseInt(gmSettings.getProperty("GMCanDelete", "100"));
                GM_KICK         = Integer.parseInt(gmSettings.getProperty("GMCanKick", "100"));
                GM_MENU         = Integer.parseInt(gmSettings.getProperty("GMMenu", "100"));
                GM_GODMODE      = Integer.parseInt(gmSettings.getProperty("GMGodMode", "100"));
                GM_CHAR_EDIT    = Integer.parseInt(gmSettings.getProperty("GMCanEditChar", "100"));
                GM_CHAR_EDIT_OTHER    = Integer.parseInt(gmSettings.getProperty("GMCanEditCharOther", "100"));
                GM_CHAR_VIEW    = Integer.parseInt(gmSettings.getProperty("GMCanViewChar", "100"));
                GM_NPC_EDIT     = Integer.parseInt(gmSettings.getProperty("GMCanEditNPC", "100"));
                GM_NPC_VIEW     = Integer.parseInt(gmSettings.getProperty("GMCanViewNPC", "100"));
                GM_TELEPORT     = Integer.parseInt(gmSettings.getProperty("GMCanTeleport", "100"));
                GM_TELEPORT_OTHER     = Integer.parseInt(gmSettings.getProperty("GMCanTeleportOther", "100"));
                GM_RESTART      = Integer.parseInt(gmSettings.getProperty("GMCanRestart", "100"));
                GM_MONSTERRACE  = Integer.parseInt(gmSettings.getProperty("GMMonsterRace", "100"));
                GM_RIDER        = Integer.parseInt(gmSettings.getProperty("GMRider", "100"));
                GM_ESCAPE       = Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
                GM_FIXED        = Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
                GM_CREATE_NODES = Integer.parseInt(gmSettings.getProperty("GMCreateNodes", "100"));
                GM_ENCHANT      = Integer.parseInt(gmSettings.getProperty("GMEnchant", "100"));
                GM_DOOR         = Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
                GM_RES          = Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
                GM_PEACEATTACK  = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
                GM_HEAL         = Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
                GM_UNBLOCK      = Integer.parseInt(gmSettings.getProperty("GMUnblock", "100"));
                GM_CACHE        = Integer.parseInt(gmSettings.getProperty("GMCache", "100"));
                GM_TALK_BLOCK   = Integer.parseInt(gmSettings.getProperty("GMTalkBlock", "100"));
                GM_TEST         = Integer.parseInt(gmSettings.getProperty("GMTest", "100"));

                String gmTrans = gmSettings.getProperty("GMDisableTransaction", "False");

                if (!gmTrans.equalsIgnoreCase("false"))
                {
                    String[] params = gmTrans.split(",");
                    GM_DISABLE_TRANSACTION = true;
                    GM_TRANSACTION_MIN = Integer.parseInt(params[0]);
                    GM_TRANSACTION_MAX = Integer.parseInt(params[1]);
                }
                else
                {
                    GM_DISABLE_TRANSACTION = false;
                }
                GM_CAN_GIVE_DAMAGE = Integer.parseInt(gmSettings.getProperty("GMCanGiveDamage", "90"));
                GM_DONT_TAKE_AGGRO = Integer.parseInt(gmSettings.getProperty("GMDontTakeAggro", "90"));
                GM_DONT_TAKE_EXPSP = Integer.parseInt(gmSettings.getProperty("GMDontGiveExpSp", "90"));

            }
            
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+GM_ACCESS_FILE+" File.");
            }

            try
            {
                Properties Settings   = new Properties();
                InputStream is          = new FileInputStream(HEXID_FILE);
                Settings.load(is);
                is.close();
                SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
                HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
            }
            catch (Exception e)
            {
                _log.warning("Could not load HexID file ("+HEXID_FILE+"). Hopefully login will give us one.");
            }
            
            
            try
            {
                Properties eliteChSettings    = new Properties();
                InputStream is              = new FileInputStream(new File(ELITE_CH));
                eliteChSettings.load(is);
                is.close();

                DEVASTATED_DAY = Integer.valueOf(eliteChSettings.getProperty("DevastatedDay", "1")).intValue();
                DEVASTATED_HOUR = Integer.valueOf(eliteChSettings.getProperty("DevastatedHour", "18")).intValue();
                DEVASTATED_MINUTES = Integer.valueOf(eliteChSettings.getProperty("DevastatedMinutes", "0")).intValue();
                PARTISAN_DAY = Integer.valueOf(eliteChSettings.getProperty("PartisanDay", "5")).intValue();
                PARTISAN_HOUR = Integer.valueOf(eliteChSettings.getProperty("PartisanHour", "21")).intValue();
                PARTISAN_MINUTES = Integer.valueOf(eliteChSettings.getProperty("PartisanMinutes", "0")).intValue();
            
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new Error("Failed to Load "+ELITE_CH+" File.");
            }
            
            try
			{
				Properties l2top = new Properties();
				InputStream is = new FileInputStream(new File(L2TOP_FILE));
				l2top.load(is);
				is.close();

				L2TOP_ENABLED = Boolean.parseBoolean(l2top.getProperty("L2TopEnabled","false"));
				L2TOP_URL = l2top.getProperty("L2TopURL","");
				L2TOP_POLLINTERVAL = Integer.parseInt(l2top.getProperty("L2TopPollInterval","5"));
				L2TOP_PREFIX = l2top.getProperty("L2TopPrefix","");
				L2TOP_ITEM = Integer.parseInt(l2top.getProperty("L2TopRewardItem","0"));
				L2TOP_MESSAGE = L2Utils.loadMessage(l2top.getProperty("L2TopMessage",""));
				L2TOP_MIN = Integer.parseInt(l2top.getProperty("L2TopMin","1"));
				L2TOP_MAX = Integer.parseInt(l2top.getProperty("L2TopMax","1"));
				L2TOP_IGNOREFIRST = Boolean.parseBoolean(l2top.getProperty("L2TopDoNotRewardAtFirstTime","false"));
				if(ItemTable.getInstance().getTemplate(L2TOP_ITEM)==null) 
				{
					L2TOP_ENABLED = false;
					System.err.println("Powerpak: Unknown item ("+L2TOP_ITEM+") as vote reward. Vote disabled");
				}	
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + L2TOP_FILE + " File.");
				}
            
            try
			{
				Properties announce = new Properties();
				InputStream is = new FileInputStream(new File(ANNOUNCE_FILE));
				announce.load(is);
				is.close();

				  ONLINE_ANNOUNE = Boolean.parseBoolean(announce.getProperty("AnnounceOnline", "False"));
			      ONLINE_ANNOUNCE_DELAY = Integer.parseInt(announce.getProperty("AnnounceDelay", "10"));
			      SAVE_MAXONLINE_IN_DB = Boolean.parseBoolean(announce.getProperty("SaveMaxOnlineInDB", "False"));
			      ONLINE_SHOW_MAXONLINE = Boolean.parseBoolean(announce.getProperty("ShowMaxOnline", "False"));
			      ONLINE_SHOW_MAXONLINE_DATE = Boolean.parseBoolean(announce.getProperty("ShowMaxOnlineDate", "False"));
			      ONLINE_SHOW_OFFLINE = Boolean.parseBoolean(announce.getProperty("ShowOfflineTraders", "False"));
			      ONLINE_LOGIN_ONLINE = Boolean.parseBoolean(announce.getProperty("AnnounceOnLogin", "False"));
			      ONLINE_LOGIN_MAX = Boolean.parseBoolean(announce.getProperty("ShowLoginMaxOnline", "False"));
			      ONLINE_LOGIN_DATE = Boolean.parseBoolean(announce.getProperty("ShowLoginMaxOnlineDate", "False"));
			      ONLINE_LOGIN_OFFLINE = Boolean.parseBoolean(announce.getProperty("ShowLoginOfflineTraders", "False"));
			      AUTO_ANNOUNCE_ALLOW = Boolean.parseBoolean(announce.getProperty("AllowAutoAnnouncements", "False"));
			      AUTO_ANNOUNCE_DELAY = Integer.parseInt(announce.getProperty("AutoAnnouncementsDelay", "600000"));
 
				
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + L2TOP_FILE + " File.");
				}
            
            
        }
        else if(Server.serverMode == Server.MODE_LOGINSERVER)
        {
            _log.info("loading login config");
            try
            {
                Properties serverSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE));
                serverSettings.load(is);
                is.close();
                GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHostname","*");
                GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort","9013"));
                LOGIN_BIND_ADDRESS     = serverSettings.getProperty("LoginserverHostname", "*");
                PORT_LOGIN            = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
                DEBUG        = Boolean.parseBoolean(serverSettings.getProperty("Debug", "false"));
                DEVELOPER    = Boolean.parseBoolean(serverSettings.getProperty("Developer", "false"));
                ASSERT       = Boolean.parseBoolean(serverSettings.getProperty("Assert", "false"));
                ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer","True"));
                REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID","0"));
                ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID","True"));
                LOGIN_TRY_BEFORE_BAN    = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
                LOGIN_BLOCK_AFTER_BAN   = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
                GM_MIN          = Integer.parseInt(serverSettings.getProperty("GMMinLevel", "100"));
                DATAPACK_ROOT    = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile(); //FIXME: in login?
                INTERNAL_HOSTNAME   = serverSettings.getProperty("InternalHostname", "localhost");
                EXTERNAL_HOSTNAME   = serverSettings.getProperty("ExternalHostname", "localhost");
                DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
                DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
                DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
                DATABASE_PASSWORD           = serverSettings.getProperty("Password", "");
                DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
                SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
                IP_UPDATE_TIME                = Integer.parseInt(serverSettings.getProperty("IpUpdateTime","15"));
                AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts","True"));
                
                BRUT_AVG_TIME = Integer.parseInt(serverSettings.getProperty("BrutAvgTime", "30"));
                BRUT_LOGON_ATTEMPTS = Integer.parseInt(serverSettings.getProperty("BrutLogonAttempts", "10"));
                BRUT_BAN_IP_TIME = Integer.parseInt(serverSettings.getProperty("BrutBanIpTime", "900"));
                
                ENABLE_DDOS_PROTECTION_SYSTEM = Boolean.parseBoolean(serverSettings.getProperty("EnableDdosProtectionSystem", "False"));
                IPTABLES_COMMAND = serverSettings.getProperty("IptablesCommand", "/sbin/iptables -I INPUT -p tcp --dport 7777 -s $ip -j ACCEPT");
                ENABLE_DEBUG_DDOS_PROTECTION_SYSTEM = Boolean.parseBoolean(serverSettings.getProperty("EnableDebug", "false"));
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
            }

            try
            {
                Properties telnetSettings   = new Properties();
                InputStream is              = new FileInputStream(new File(TELNET_FILE));
                telnetSettings.load(is);
                is.close();

                IS_TELNET_ENABLED   = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+TELNET_FILE+" File.");
            }
        }
        else
        {
            _log.severe("Could not Load Config: server mode was not set");
        }

    }

    public static boolean setParameterValue(String pName, String pValue)
    {
        if (pName.equalsIgnoreCase("RateXp")) RATE_XP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateSp")) RATE_SP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RatePartyXp")) RATE_PARTY_XP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RatePartySp")) RATE_PARTY_SP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateQuestsReward")) RATE_QUESTS_REWARD = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropAdena")) RATE_DROP_ADENA = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateConsumableCost")) RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropItems")) RATE_DROP_ITEMS = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropSpoil")) RATE_DROP_SPOIL = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropManor")) RATE_DROP_MANOR = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("RateDropQuest")) RATE_DROP_QUEST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateKarmaExpLost")) RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice")) RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("PlayerDropLimit")) PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDrop")) PLAYER_RATE_DROP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDropItem")) PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDropEquip")) PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon")) PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaDropLimit")) KARMA_DROP_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDrop")) KARMA_RATE_DROP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDropItem")) KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDropEquip")) KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon")) KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter")) AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("DestroyPlayerDroppedItem")) DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("DestroyEquipableItem")) DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("SaveDroppedItem")) SAVE_DROPPED_ITEM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("EmptyDroppedItemTableAfterLoad")) EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("SaveDroppedItemInterval")) SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("ClearDroppedItemTable")) CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("PreciseDropCalculation")) PRECISE_DROP_CALCULATION = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MultipleItemDrop")) MULTIPLE_ITEM_DROP = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CoordSynchronize")) COORD_SYNCHRONIZE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("DeleteCharAfterDays")) DELETE_DAYS = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AllowDiscardItem")) ALLOW_DISCARDITEM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowFreight")) ALLOW_FREIGHT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowWarehouse")) ALLOW_WAREHOUSE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowWear")) ALLOW_WEAR = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("WearDelay")) WEAR_DELAY = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("WearPrice")) WEAR_PRICE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AllowWater")) ALLOW_WATER = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowRentPet")) ALLOW_RENTPET = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowBoat")) ALLOW_BOAT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowCursedWeapons")) ALLOW_CURSED_WEAPONS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowManor")) ALLOW_MANOR = Boolean.valueOf(pValue);        
        else if (pName.equalsIgnoreCase("BypassValidation")) BYPASS_VALIDATION = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CommunityType")) COMMUNITY_TYPE = pValue.toLowerCase();
        else if (pName.equalsIgnoreCase("BBSDefault")) BBS_DEFAULT = pValue;
        else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard")) SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard")) SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard")) NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard")) NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("ShowServerNews")) SERVER_NEWS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("ForceInventoryUpdate")) FORCE_INVENTORY_UPDATE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData")) AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MaximumOnlineUsers")) MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("ZoneTown")) ZONE_TOWN = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("CheckKnownList")) CHECK_KNOWN = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("UseDeepBlueDropRules")) DEEPBLUE_DROP_RULES = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowGuards")) ALLOW_GUARDS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CancelLesserEffect")) EFFECT_CANCELING = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("WyvernSpeed")) WYVERN_SPEED = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("StriderSpeed")) STRIDER_SPEED = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf")) INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf")) INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer")) INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf")) WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf")) WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan")) WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumFreightSlots")) FREIGHT_SLOTS = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("HpRegenMultiplier")) HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("MpRegenMultiplier")) MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("CpRegenMultiplier")) CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier")) RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier")) RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("RaidDefenceMultiplier")) RAID_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) /100;
        else if (pName.equalsIgnoreCase("RaidMinionRespawnTime")) RAID_MINION_RESPAWN_TIMER =Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("StartingAdena")) STARTING_ADENA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("UnstuckInterval")) UNSTUCK_INTERVAL = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerSpawnProtection")) PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerFakeDeathUpProtection")) PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PartyXpCutoffMethod")) PARTY_XP_CUTOFF_METHOD = pValue;
        else if (pName.equalsIgnoreCase("PartyXpCutoffPercent")) PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("PartyXpCutoffLevel")) PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("RespawnRestoreCP")) RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
        else if (pName.equalsIgnoreCase("RespawnRestoreHP")) RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
        else if (pName.equalsIgnoreCase("RespawnRestoreMP")) RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
        else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsDwarf")) MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsOther")) MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("StoreSkillCooltime")) STORE_SKILL_COOLTIME = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AnnounceMammonSpawn")) ANNOUNCE_MAMMON_SPAWN = Boolean.valueOf(pValue);
        // Alternative settings
        else if (pName.equalsIgnoreCase("AltGameTiredness")) ALT_GAME_TIREDNESS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreation")) ALT_GAME_CREATION = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationSpeed")) ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationXpRate")) ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationSpRate")) ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltWeightLimit")) ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltBlacksmithUseRecipes")) ALT_BLACKSMITH_USE_RECIPES = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameSkillLearn")) ALT_GAME_SKILL_LEARN = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("RemoveCastleCirclets")) REMOVE_CASTLE_CIRCLETS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
        {
            ALT_GAME_CANCEL_BOW     = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
            ALT_GAME_CANCEL_CAST    = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
        }
        
        else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate")) ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("Delevel")) ALT_GAME_DELEVEL = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MagicFailures")) ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameMobAttackAI")) ALT_GAME_MOB_ATTACK_AI = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltMobAgroInPeaceZone")) ALT_MOB_AGRO_IN_PEACEZONE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameExponentXp")) ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("AltGameExponentSp")) ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("AllowClassMasters")) ALLOW_CLASS_MASTERS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameFreights")) ALT_GAME_FREIGHTS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameFreightPrice")) ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltPartyRange")) ALT_PARTY_RANGE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltPartyRange2")) ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("CraftingEnabled")) IS_CRAFTING_ENABLED = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("LifeCrystalNeeded")) LIFE_CRYSTAL_NEEDED = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("SpBookNeeded")) SP_BOOK_NEEDED = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AutoLoot")) AUTO_LOOT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AutoLootHerbs")) AUTO_LOOT_HERBS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone")) ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop")) ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK")) ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport")) ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade")) ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse")) ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltRequireCastleForDawn")) ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltRequireClanCastle")) ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltFreeTeleporting")) ALT_GAME_FREE_TELEPORT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests")) ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltMembersCanWithdrawFromClanWH")) ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("DwarfRecipeLimit")) DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("CommonRecipeLimit")) COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AllowWedding")) L2JMOD_ALLOW_WEDDING = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("WeddingPrice")) L2JMOD_WEDDING_PRICE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("WeddingPunishInfidelity")) L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("WeddingTeleport")) L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("WeddingTeleportPrice")) L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("WeddingTeleportDuration")) L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("WeddingAllowSameSex")) L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("WeddingFormalWear")) L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("WeddingDivorceCosts")) L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("TvTEventEnabled")) TVT_EVENT_ENABLED = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("TvTEventParticipationTime")) TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("TvTEventRunningTime")) TVT_EVENT_RUNNING_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("TvTEventParticipationNpcId")) TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MinKarma")) KARMA_MIN_KARMA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaxKarma")) KARMA_MAX_KARMA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("XPDivider")) KARMA_XP_DIVIDER = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("BaseKarmaLost")) KARMA_LOST_BASE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("CanGMDropEquipment")) KARMA_DROP_GM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint")) KARMA_AWARD_PK_KILL = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop")) KARMA_PK_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PvPVsNormalTime")) PVP_NORMAL_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PvPVsPvPTime")) PVP_PVP_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("GlobalChat")) DEFAULT_GLOBAL_CHAT = pValue;
        else if (pName.equalsIgnoreCase("TradeChat"))  DEFAULT_TRADE_CHAT = pValue;
        else if (pName.equalsIgnoreCase("MenuStyle"))  GM_ADMIN_MENU_STYLE = pValue;
		else if (pName.equalsIgnoreCase("CommonRecipeLimit")) COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionEnable")) CHAMPION_ENABLE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ChampionFrequency")) CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionMinLevel")) CHAMPION_MIN_LVL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionMaxLevel")) CHAMPION_MAX_LVL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionHp")) CHAMPION_HP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionHpRegen")) CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewards")) CHAMPION_REWARDS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionAdenasRewards")) CHAMPION_ADENAS_REWARDS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionAtk")) CHAMPION_ATK = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionSpdAtk")) CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewardItem")) CHAMPION_REWARD = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewardItemID")) CHAMPION_REWARD_ID = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewardItemQty")) CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionAura")) CHAMPION_AURA = Boolean.parseBoolean(pValue);
        else return false;
        return true;
    }

	public static class ClassMasterSettings
	{
		private FastMap<Integer, FastMap<Integer, Integer>> _claimItems;
		private FastMap<Integer, FastMap<Integer, Integer>> _rewardItems;
		private FastMap<Integer, Boolean> _allowedClassChange;

		public ClassMasterSettings(String _configLine)
		{
			_claimItems = new FastMap<Integer, FastMap<Integer, Integer>>();
			_rewardItems = new FastMap<Integer, FastMap<Integer, Integer>>();
			_allowedClassChange = new FastMap<Integer, Boolean>();
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

				FastMap<Integer, Integer> _items = new FastMap<Integer, Integer>();
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

				_items = new FastMap<Integer, Integer>();
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
			else
				return false;
		}

		public FastMap<Integer, Integer> getRewardItems(int job)
		{
			if (_rewardItems.containsKey(job))
				return _rewardItems.get(job);
			else
				return null;
		}

		public FastMap<Integer, Integer> getRequireItems(int job)
		{
			if (_claimItems.containsKey(job))
				return _claimItems.get(job);
			else
				return null;
		}

	}

    public static boolean allowL2Walker(L2PcInstance player)
    {
        return (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.True ||
                (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.GM && player != null && player.isGM()));
    }
    private static String getProperty(Properties prop, String name)
    {
      lastKey = name;
      String result = prop.getProperty(name.trim());
      lastKey = lastKey + " = " + result;
      return result;
    }
    private static int[] getIntArray(Properties prop, String name, int[] _default)
    {
      String s = getProperty(prop, name);
      return ((s == null) ? _default : Util.parseCommaSeparatedIntegerArray(s.trim()));
    }
    private Config() {}
    public static void saveHexid(int serverId, String string)
    {
        Config.saveHexid(serverId, string, HEXID_FILE);
    }
    public static void saveHexid(int serverId, String hexId, String fileName)
    {
        try
        {
            Properties hexSetting = new Properties();
            File file = new File(fileName);
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            hexSetting.setProperty("ServerID",String.valueOf(serverId));
            hexSetting.setProperty("HexID",hexId);
            hexSetting.store(out,"the hexID to auth into login");
            out.close();
        }
        catch (Exception e)
        {
            _log.warning("Failed to save hex id to "+fileName+" File.");
            e.printStackTrace();
        }
    }
}
