package l2.hellknight;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntFloatHashMap;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2.hellknight.gameserver.util.FloodProtectorConfig;
import l2.hellknight.util.L2Properties;
import l2.hellknight.util.StringUtil;

public final class Config
{
	protected static final Logger _log = Logger.getLogger(Config.class.getName());
	
	//	GrandBoss/
	public static final String ANTHARAS_CONFIG_FILE = "./config/GrandBoss/antharas.properties";
	public static final String VALAKAS_CONFIG_FILE = "./config/GrandBoss/valakas.properties";
	public static final String BAIUM_CONFIG_FILE = "./config/GrandBoss/baium.properties";
	public static final String BELETH_CONFIG_FILE = "./config/GrandBoss/beleth.properties";
	public static final String CORE_CONFIG_FILE = "./config/GrandBoss/core.properties";
	public static final String ORFEN_CONFIG_FILE = "./config/GrandBoss/orfen.properties";
	public static final String QUEENANT_CONFIG_FILE = "./config/GrandBoss/queenant.properties";
	public static final String ZAKEN_CONFIG_FILE = "./config/GrandBoss/zaken.properties";
	public static final String FRINTEZZA_CONFIG_FILE = "./config/GrandBoss/frintezza.properties";
	public static final String SAILREN_CONFIG_FILE = "./config/GrandBoss/sailren.properties";
	public static final String FREYA_CONFIG_FILE = "./config/GrandBoss/freya.properties";
	public static final String LINDVIOR_CONFIG_FILE = "./config/GrandBoss/lindvior.properties";
	
	//	InGame/
	public static final String CHARACTER_CONFIG_FILE = "./config/InGame/character.properties";
	public static final String PREMIUM_CONFIG_FILE = "./config/InGame/premium.properties";
	public static final String GENERAL_CONFIG_FILE = "./config/InGame/general.properties";
	public static final String NPC_CONFIG_FILE = "./config/InGame/npc.properties";
	public static final String PVP_CONFIG_FILE = "./config/InGame/playervsplayer.properties";
	public static final String Enchant_FILE = "./config/InGame/enchant.properties";
	public static final String Augmentation_FILE = "./config/InGame/augmentation.properties";
	public static final String GameMaster_FILE = "./config/InGame/gamemaster.properties";
	public static final String OfflineMod_FILE = "./config/InGame/offlinemod.properties";
	public static final String GAMEFEATURE_CONFIG_FILE = "./config/InGame/gamefeature.properties";
	public static final String LOTTERY_CONFIG_FILE = "./config/InGame/lottery.properties";
	public static final String MANOR_CONFIG_FILE = "./config/InGame/manor.properties";
	public static final String MISC_CONFIG_FILE = "./config/InGame/misc.properties";
	private static final String AIO_CONFIG_FILE = "./config/InGame/aioconfig.properties";
	
	//	InGameEvents/
	public static final String LeaderBoards_FILE = "./config/InGameEvents/leaderboards.properties";
	public static final String Champions_FILE = "./config/InGameEvents/champions.properties";
	public static final String TeamVsTeam_FILE = "./config/InGameEvents/teamvsteam.properties";
	public static final String TeamVsTeamFragReward_FILE = "./config/InGameEvents/teamvsteamfragreward.properties";
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/InGameEvents/fortsiege.properties";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/InGameEvents/siege.properties";
	public static final String TW_CONFIGURATION_FILE = "./config/InGameEvents/territorywar.properties";
	public static final String FEATURE_CONFIG_FILE = "./config/InGameEvents/feature.properties";
	public static final String ExtraConfig_CONFIG_FILE = "./config/InGameEvents/extraconfig.properties";
	public static final String OLYMPIAD_CONFIG_FILE = "./config/InGameEvents/olympiadgames.properties";
	public static final String PCBANG_CONFIG_FILE = "./config/InGameEvents/pccafe.properties";
	public static final String CH_SIEGE_FILE = "./config/InGameEvents/conquerablehallsiege.properties";
	public static final String HideAndSeek_FILE = "./config/InGameEvents/hideandseek.properties";
	public static final String DIMENSIONALRIFT_CONFIG_FILE = "./config/InGameEvents/dimensionrift.properties";
	public static final String HANDYSBLOCKCHECKER_CONFIG_FILE = "./config/InGameEvents/handysblockchecker.properties";
	public static final String TEAMVSTEAMROUNDS_CONFIG_FILE = "./config/InGameEvents/teamvsteamrounds.properties";
	public static final String RIMKAMALOKA_CONFIG_FILE = "./config/InGameEvents/rimkamaloka.properties";
	public static final String UNDERGROUNDCOLI_CONFIG_FILE = "./config/InGameEvents/undergroundcoliseum.properties";
	public static final String DM_CONFIG_FILE = "./config/InGameEvents/deathmatch.properties";
	public static final String CTF_CONFIG_FILE = "./config/InGameEvents/ctf.properties";
	public static final String LM_CONFIG_FILE = "./config/InGameEvents/lastman.properties";
	public static final String EVENTMODS_CONFIG_FILE = "./config/InGameEvents/eventmods.properties";
	
	//	ServerSettings/
	public static final String RATES_CONFIG_FILE = "./config/ServerSettings/rates.properties";
	public static final String AntiDualBox_FILE = "./config/ServerSettings/antidualbox.properties";
	public static final String HEXID_FILE = "./config/ServerSettings/hexid.txt";
	public static final String ID_CONFIG_FILE = "./config/ServerSettings/idfactory.properties";
	public static final String SERVER_VERSION_FILE = "./config/ServerSettings/l2hellknight-version.properties";
	public static final String DATAPACK_VERSION_FILE = "./config/ServerSettings/l2hellknightdp-version.properties";
	public static final String CONFIGURATION_FILE = "./config/ServerSettings/server.properties";
	public static final String IP_CONFIG_FILE = "./config/ServerSettings/ipconfig.xml";
	public static final String FLOOD_PROTECTOR_FILE = "./config/ServerSettings/floodprotector.properties";
	public static final String ServerSecurity_FILE = "./config/ServerSettings/serversecurity.properties";
	public static final String Optimization_FILE = "./config/ServerSettings/optimization.properties";
	public static final String Geodata_FILE = "./config/ServerSettings/geodata.properties";
	public static final String CLIENTSPACKET_CONFIG_FILE = "./config/ServerSettings/clientspacket.properties";
	public static final String CUSTOMTABLES_CONFIG_FILE = "./config/ServerSettings/customtables.properties";
	public static final String VOTEREWARD_CONFIG_FILE = "./config/ServerSettings/votereward.properties";
	public static final String NPCINFO_FILE = "./config/ServerSettings/npcserverinfo.properties";

	// Scripts/
	public static final String GRACIASEEDS_CONFIG_FILE = "./config/Scripts/graciaseeds.properties";
	public static final String HOS_CONFIG_FILE = "./config/Scripts/hallofsuffering.properties";
	public static final String HELLBOUND_CONFIG_FILE = "./config/Scripts/hellbound.properties";
	public static final String PAILAKA_CONFIG_FILE = "./config/Scripts/pailaka.properties";
	
	//Other = no folder created/
	public static final String TELNET_FILE = "./config/telnet.properties";
	public static final String MMO_CONFIG_FILE = "./config/mmo.properties";
	public static final String CHAT_FILTER_FILE = "./config/chatfilter.txt";
	public static final String LOGIN_CONFIGURATION_FILE = "./config/loginserver.properties";
	public static final String COMMUNITY_CONFIGURATION_FILE = "./config/communityserver.properties";

	//MODS
	public static boolean ENABLE_ELPY;
	public static int EVENT_INTERVAL_ELPIES;
	public static int EVENT_TIME_ELPIES;
	public static int EVENT_NUMBER_OF_SPAWNED_ELPIES;
	
	public static boolean ENABLE_RABBITS;
	public static int EVENT_INTERVAL_RABBITS;
	public static int EVENT_TIME_RABBITS;
	public static int EVENT_NUMBER_OF_SPAWNED_CHESTS;
	
	public static boolean ENABLE_RACE;
	public static int EVENT_INTERVAL_RACE;
	public static int EVENT_REG_TIME_RACE;
	public static int EVENT_RUNNING_TIME_RACE;

	//Auto Loot Comand
	public static boolean L2JMOD_AUTO_LOOT_INDIVIDUAL;
	
	//Underground Coliseum
	public static ArrayList<Integer> UC_WARDAYS = new ArrayList<Integer>(7);
	public static int UC_START_HOUR;
	public static int UC_END_HOUR;
	public static int UC_ROUND_TIME;
	public static int UC_PARTY_LIMIT;
	
	//AIO
	public static boolean AIOITEM_ENABLEME;
	public static boolean AIOITEM_ENABLESHOP;
	public static boolean AIOITEM_ENABLEGK;
	public static boolean AIOITEM_ENABLEWH;
	public static boolean AIOITEM_ENABLEBUFF;
	public static boolean AIOITEM_ENABLESCHEMEBUFF;
	public static boolean AIOITEM_ENABLESERVICES;
	public static boolean AIOITEM_ENABLESUBCLASS;
	public static boolean AIOITEM_ENABLETOPLIST;
	public static int AIOITEM_GK_COIN;
	public static int AIOITEM_GK_PRICE;
	public static int AIOITEM_BUFF_COIN;
	public static int AIOITEM_BUFF_PRICE;
	public static int AIOITEM_SCHEME_COIN;
	public static int AIOITEM_SCHEME_PRICE;
	public static int AIOITEM_SCHEME_PROFILE_PRICE;
	public static int AIOITEM_SCHEME_MAX_PROFILES;
	public static int AIOITEM_SCHEME_MAX_PROFILE_BUFFS;
	
	//LM - Last Man
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
	public static byte LM_EVENT_MIN_LVL;
	public static byte LM_EVENT_MAX_LVL;
	public static int LM_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap LM_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap LM_EVENT_MAGE_BUFFS;
	public static boolean LM_ALLOW_VOICED_COMMAND;
	
	//DM
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
	public static byte DM_EVENT_MIN_LVL;
	public static byte DM_EVENT_MAX_LVL;
	public static int DM_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap DM_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap DM_EVENT_MAGE_BUFFS;
	public static boolean DM_ALLOW_VOICED_COMMAND;
	
	//antimulti
	public static int MAX_PLAYERS_FROM_ONE_PC;
	public static boolean ALLOW_MAX_PLAYERS_FROM_ONE_PC;

	//CTF
	public static boolean CTF_EVENT_ENABLED;
	public static String[] CTF_EVENT_INTERVAL;
	public static String CTF_EVEN_TEAMS;
	public static boolean CTF_ALLOW_VOICE_COMMAND;
	public static boolean CTF_ALLOW_INTERFERENCE;
	public static boolean CTF_ALLOW_POTIONS;
	public static boolean CTF_ALLOW_SUMMON;
	public static boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean CTF_ON_START_UNSUMMON_PET;
	public static boolean CTF_ANNOUNCE_TEAM_STATS;
	public static boolean CTF_ANNOUNCE_REWARD;
	public static boolean CTF_JOIN_CURSED;
	public static boolean CTF_REVIVE_RECOVERY;
	public static long CTF_REVIVE_DELAY;
	public static boolean CTF_BASE_TELEPORT_FIRST;
	
	//Vote Reward
	    public static String 		VOTE_HTML_PATCH;
	    public static int 			VOTE_REWARD1_ID;
	    public static int 			VOTE_REWARD2_ID;
	    public static int 			VOTE_REWARD1_COUNT;
	    public static int 			VOTE_REWARD2_COUNT;
	    public static int 			VOTES_FOR_REWARD;
	    public static boolean 		VOTE_REWARD_ENABLE;
	    public static String 		SERVER_NAME_FOR_VOTES;
	   public static int 			MAX_REWARD_COUNT_FOR_STACK_ITEM1;
	    public static int 			MAX_REWARD_COUNT_FOR_STACK_ITEM2;
	    public static int 			DELAY_FOR_NEXT_REWARD;
		//Online Command
	    public static boolean       ONLINE_PLAYERS;
	    // Custom Title
	    public static boolean CHAR_TITLE;
	    public static String ADD_CHAR_TITLE;
	    // Flag Player Can't Use GK
	    public static boolean 	ALT_GAME_FLAGED_PLAYER_CAN_USE_GK;
	    public static boolean           ALT_OLY_SAME_IP;
			
	//pets
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static boolean RESTORE_SERVITOR_ON_RECONNECT;
	public static boolean RESTORE_PET_ON_RECONNECT;
	
	//Pailaka
	public static int PSOIAF_MIN_LVL;
	public static int PSOIAF_MAX_LVL;
	public static int PSOIAF_EXIT_TIME;
	public static int PDL_MIN_LVL;
	public static int PDL_MAX_LVL;
	public static int PDL_EXIT_TIME;
	public static int PID_MIN_LVL;
	public static int PID_MAX_LVL;
	public static int PID_MAX_LVL_SUMMON;
	public static int PID_EXIT_TIME;

	//Hall Of Suffering
	public static boolean HOS_DEBUG;
	public static int HOS_INS_PENALTY;
	public static int HOS_MIN_LEVEL;

	//hellbound
	public static boolean ANNOUNCE_TO_ALL_GAINED_TRUST;
	public static boolean MOD_HELLBOUND_STATUS;
	public static float RATE_HB_TRUST_INCREASE;
	public static float RATE_HB_TRUST_DECREASE;
	public static boolean ENTER_HELLBOUND_WITHOUT_QUEST;
	public static boolean L2JMOD_HELLBOUND_STATUS;
	
	//captcha system
	public static int MIN_KILLS_FOR_CAPTCHA;
	public static int CAPTCHA_SYSTEM;
	
	//Rim Kamaloka
	public static int RESET_HOUR;
	public static int RESET_MIN;
	public static int LOCK_TIME;
	public static int DURATION;
	public static int EMPTY_DESTROY_TIME;
	public static int EXIT_TIME;
	public static int MAX_LEVEL_DIFFERENCE;
	public static int RESPAWN_DELAY;
	public static int DESPAWN_DELAY;
	
	//tvt round
	public static boolean TVT_ROUND_EVENT_ENABLED;
	public static boolean TVT_ROUND_EVENT_IN_INSTANCE;
	public static String TVT_ROUND_EVENT_INSTANCE_FILE;
	public static String[] TVT_ROUND_EVENT_INTERVAL;
	public static int TVT_ROUND_EVENT_PARTICIPATION_TIME;
	public static int TVT_ROUND_EVENT_FIRST_FIGHT_RUNNING_TIME;
	public static int TVT_ROUND_EVENT_SECOND_FIGHT_RUNNING_TIME;
	public static int TVT_ROUND_EVENT_THIRD_FIGHT_RUNNING_TIME;
	public static int TVT_ROUND_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_ROUND_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static boolean TVT_ROUND_EVENT_ON_DIE;
	public static int TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY;
	public static String TVT_ROUND_EVENT_TEAM_1_NAME;
	public static int[] TVT_ROUND_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String TVT_ROUND_EVENT_TEAM_2_NAME;
	public static int[] TVT_ROUND_EVENT_TEAM_2_COORDINATES = new int[3];
	public static List<int[]> TVT_ROUND_EVENT_REWARDS;
	public static boolean TVT_ROUND_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_ROUND_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_ROUND_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_ROUND_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> TVT_ROUND_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_ROUND_DOORS_IDS_TO_CLOSE;
	public static List<Integer> TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE;
	public static int TVT_ROUND_EVENT_WAIT_OPEN_ANTEROOM_DOORS;
	public static int TVT_ROUND_EVENT_WAIT_CLOSE_ANTEROOM_DOORS;
	public static boolean TVT_ROUND_EVENT_STOP_ON_TIE;
	public static int TVT_ROUND_EVENT_MINIMUM_TIE;
	public static boolean TVT_ROUND_GIVE_POINT_TEAM_TIE;
	public static boolean TVT_ROUND_REWARD_TEAM_TIE;
	public static boolean TVT_ROUND_EVENT_REWARD_ON_SECOND_FIGHT_END;
	public static byte TVT_ROUND_EVENT_MIN_LVL;
	public static byte TVT_ROUND_EVENT_MAX_LVL;
	public static int TVT_ROUND_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap TVT_ROUND_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap TVT_ROUND_EVENT_MAGE_BUFFS;
	public static int TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP;
	public static boolean TVT_ROUND_ALLOW_VOICED_COMMAND;
	
	//PC Bang Settings
	public static boolean PC_BANG_ENABLED;
	public static int MAX_PC_BANG_POINTS;
	public static boolean ENABLE_DOUBLE_PC_BANG_POINTS;
	public static int DOUBLE_PC_BANG_POINTS_CHANCE;
	public static double PC_BANG_POINT_RATE;
	public static boolean RANDOM_PC_BANG_POINT;
	
	//enchant skill
	public static boolean ENABLE_SKILL_ENCHANT;
	public static boolean ENABLE_SKILL_MAX_ENCHANT_LIMIT;
	public static int SKILL_MAX_ENCHANT_LIMIT_LEVEL;
	
	//server npc info
	public static int SERVERINFO_NPC_ID;
	public static String[] SERVERINFO_NPC_DISABLE_PAGE;
	
	//Hide and Seek
	public static boolean ALT_HAS_ENABLE;
	public static String[] ALT_HAS_TIME;
	public static int ALT_HAS_TIME_REG;
	public static int ALT_HAS_TIME_EVENT;
	public static int ALT_HAS_NPC;
	public static int[] ALT_HAS_LOCNPC = new int[3];
	public static boolean ALT_HAS_PKJOIN;
	public static boolean ALT_HAS_SECUENTIAL;
	public static int ALT_HAS_MINLEVEL;
	public static int ALT_HAS_MAXLEVEL;
	public static int ALT_HAS_MINPLAYERS;
	public static int ALT_HAS_MAXPLAYERS;
	
	public static TIntIntHashMap MINIONS_RESPAWN_TIME;
	
	public static int TVT_FRAGS_MIN_FOR_EXTRA_REWARD;
	public static boolean TVT_EXTRA_REWARD_FOR_FRAGS;
	public static List<int[]> TVT_EXTRA_REWARDS = new FastList<int[]>();;
	public static int TVT_FRAGS_FOR_REWARD_STEP;
	public static boolean TVT_FRAGS_COUNTER;
	public static int TVT_FRAGS_MIN_FOR_REWARD;
	public static boolean LIMIT_SUMMONS_PAILAKA;
	public static boolean RANK_ARENA_ENABLED;
	public static int RANK_ARENA_INTERVAL;
	public static int RANK_ARENA_REWARD_ID;
	public static int RANK_ARENA_REWARD_COUNT;
	public static boolean RANK_FISHERMAN_ENABLED;
	public static int RANK_FISHERMAN_INTERVAL;
	public static int RANK_FISHERMAN_REWARD_ID;
	public static int RANK_FISHERMAN_REWARD_COUNT;
	public static boolean RANK_CRAFT_ENABLED;
	public static int RANK_CRAFT_INTERVAL;
	public static int RANK_CRAFT_REWARD_ID;
	public static int RANK_CRAFT_REWARD_COUNT;
	public static boolean RANK_TVT_ENABLED;
	public static int RANK_TVT_INTERVAL;
	public static int RANK_TVT_REWARD_ID;
	public static int RANK_TVT_REWARD_COUNT;
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
	public static boolean AUTO_LOOT_HERBS;
	public static byte BUFFS_MAX_AMOUNT;
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
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALLOW_CLASS_MASTERS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ALTERNATE_CLASS_MASTER;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean SP_BOOK_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION;
	public static byte MAX_SUBCLASS;
	public static byte MAX_SUBCLASS_LEVEL;
	public static byte START_SUBCLASS_LEVEL;
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
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
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
	public static boolean GM_CRITANNOUNCER_NAME;
	public static boolean GM_GIVE_SPECIAL_SKILLS;
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
	public static boolean DEBUG;
	public static boolean PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	public static boolean ACCEPT_GEOEDITOR_CONN;
	public static boolean TEST_SERVER;
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
	public static boolean DATABASE_CLEAN_UP;
	public static long CONNECTION_CLOSE_TIME;
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
	public static boolean COMMUNITY_COLOR_LEGEND;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int[] ALT_OLY_END_DATE;
	public static int[] ALT_OLY_END_HOUR = new int[3];
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
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static TIntArrayList LIST_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
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
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE;
	public static boolean CUSTOM_NPC_SKILLS_TABLE;
	public static boolean CUSTOM_ARMORSETS_TABLE;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_DROPLIST_TABLE;
	public static boolean CUSTOM_MERCHANT_TABLES;
	public static boolean CUSTOM_NPCBUFFER_TABLES;
	public static boolean ENABLE_BLOCK_CHECKER_EVENT;
	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean HBCE_FAIR_PLAY;
	public static int PLAYER_MOVEMENT_BLOCK_TIME;
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
	public static boolean L2JMOD_CHAMPION_ENABLE;
	public static boolean L2JMOD_CHAMPION_PASSIVE;
	public static int L2JMOD_CHAMPION_FREQUENCY;
	public static String L2JMOD_CHAMP_TITLE;
	public static int L2JMOD_CHAMP_MIN_LVL;
	public static int L2JMOD_CHAMP_MAX_LVL;
	public static int L2JMOD_CHAMPION_HP;
	public static int L2JMOD_CHAMPION_REWARDS;
	public static float L2JMOD_CHAMPION_ADENAS_REWARDS;
	public static float L2JMOD_CHAMPION_HP_REGEN;
	public static float L2JMOD_CHAMPION_ATK;
	public static float L2JMOD_CHAMPION_SPD_ATK;
	public static int L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int L2JMOD_CHAMPION_REWARD_ID;
	public static int L2JMOD_CHAMPION_REWARD_QTY;
	public static boolean	L2JMOD_CHAMPION_ENABLE_VITALITY;
	public static int L2JMOD_CHAMPION_ENABLE_AURA;
	public static boolean L2JMOD_CHAMPION_ENABLE_IN_INSTANCES;
	public static boolean TVT_EVENT_ENABLED;
	public static boolean TVT_EVENT_IN_INSTANCE;
	public static String TVT_EVENT_INSTANCE_FILE;
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
	public static List<Integer> TVT_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_DOORS_IDS_TO_CLOSE;
	public static boolean TVT_REWARD_TEAM_TIE;
	public static byte TVT_EVENT_MIN_LVL;
	public static byte TVT_EVENT_MAX_LVL;
	public static int TVT_EVENT_EFFECTS_REMOVAL;
	public static TIntIntHashMap TVT_EVENT_FIGHTER_BUFFS;
	public static TIntIntHashMap TVT_EVENT_MAGE_BUFFS;
	public static int TVT_EVENT_MAX_PARTICIPANTS_PER_IP;
	public static boolean TVT_ALLOW_VOICED_COMMAND;
	public static List<int[]> TVT_EVENT_REWARDS_KILL;
	public static boolean L2JMOD_ALLOW_WEDDING;
	public static int L2JMOD_WEDDING_PRICE;
	public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
	public static boolean L2JMOD_WEDDING_TELEPORT;
	public static int L2JMOD_WEDDING_TELEPORT_PRICE;
	public static int L2JMOD_WEDDING_TELEPORT_DURATION;
	public static boolean L2JMOD_WEDDING_SAMESEX;
	public static boolean L2JMOD_WEDDING_FORMALWEAR;
	public static int L2JMOD_WEDDING_DIVORCE_COSTS;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean OFFLINE_SUPER_MODE_ENABLE;
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	public static boolean L2JMOD_ENABLE_MANA_POTIONS_SUPPORT;
	public static boolean L2JMOD_DISPLAY_SERVER_TIME;
	public static boolean L2JMOD_VOTE_ENGINE_ENABLE;
	public static boolean L2JMOD_VOTE_ENGINE_SAVE;
	
	//Top pvp/pk
	public static boolean TOPDEBUG;
	public static int TOPID;
	
	//Welcome Message
	public static boolean WELCOME_MESSAGE_ENABLED;
	public static String WELCOME_MESSAGE_TEXT;
	public static int WELCOME_MESSAGE_TIME;
	
	//System Message advance configuration
	public static boolean SHOW_DAMAGE_MESSAGE_ON_CENTER_TOP_SCREEN;
	public static int FONT_SIZE_CRITICAL;
	public static int SCREEN_POSITION;
	
	//bind account ip
	public static boolean ALLOW_BIND_ACCOUNT_IP;
	
	//Welcome message - PM
    public static boolean SHOW_WELCOME_PM;
    public static String PM_FROM;
    public static String PM_TEXT;
	
	//Welcome Message - advanced
	public static boolean WELCOME_ADVANCE_MESSAGE_ENABLED;
	public static String WELCOME_ADVANCE_HUMAN_MESSAGE_TEXT;
	public static String WELCOME_ADVANCE_DARK_ELF_MESSAGE_TEXT;
	public static String WELCOME_ADVANCE_DWARF_MESSAGE_TEXT;
	public static String WELCOME_ADVANCE_ELF_MESSAGE_TEXT;
	public static String WELCOME_ADVANCE_KAMAEL_MESSAGE_TEXT;
	public static String WELCOME_ADVANCE_ORC_MESSAGE_TEXT;
	public static int WELCOME_ADVANCE_MESSAGE_TIME;
	
	public static boolean L2JMOD_ANTIFEED_ENABLE;
	public static boolean L2JMOD_ANTIFEED_DUALBOX;
	public static boolean L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int L2JMOD_ANTIFEED_INTERVAL;
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	public static boolean L2JMOD_CHAT_ADMIN;
	public static boolean L2JMOD_MULTILANG_ENABLE;
	public static List<String> L2JMOD_MULTILANG_ALLOWED = new ArrayList<String>();
	public static String L2JMOD_MULTILANG_DEFAULT;
	public static boolean L2JMOD_MULTILANG_VOICED_ALLOW;
	public static boolean L2JMOD_MULTILANG_SM_ENABLE;
	public static List<String> L2JMOD_MULTILANG_SM_ALLOWED = new ArrayList<String>();
	public static boolean L2WALKER_PROTECTION;
	public static boolean L2JMOD_DEBUG_VOICE_COMMAND;
	public static int L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP;
	public static int L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP;
	public static TIntIntHashMap L2JMOD_DUALBOX_CHECK_WHITELIST;
	public static boolean AUTO_ACTIVATE_SHOTS;
	public static int AUTO_ACTIVATE_SHOTS_MIN;
	public static boolean ALT_HERO_COLOR_ENABLED;
	public static int ALT_HERO_COLOR;
	public static boolean ALT_NOBLE_COLOR_ENABLED;
	public static int ALT_NOBLE_COLOR;
	public static boolean CLAN_LEADER_COLOR_ENABLED;
	public static int CLAN_LEADER_COLOR;
	public static int CLAN_LEADER_COLOR_CLAN_LEVEL;
	public static boolean ALT_KEEP_BUFF_AFTER_DEATH;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static boolean ALT_GAME_VIEWPLAYER;
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
	
	//Premium
	public static boolean USE_PREMIUMSERVICE;
	public static float PREMIUM_RATE_XP;
	public static float PREMIUM_RATE_SP;
	public static float PREMIUM_RATE_DROP_ITEMS;
	public static float PREMIUM_RATE_DROP_SPOIL;
	public static TIntFloatHashMap PREMIUM_RATE_DROP_ITEMS_ID;
	
	
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
	public static boolean ENABLE_BOTREPORT;
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
	public static int DATABASE_MAX_IDLE_TIME;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static File DATAPACK_ROOT;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static TIntArrayList PROTOCOL_LIST;
	public static boolean LOG_LOGIN_CONTROLLER;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	public static boolean	ENABLE_COMMUNITY_BOARD;
	public static String	COMMUNITY_SERVER_ADDRESS;
	public static int		COMMUNITY_SERVER_PORT;
	public static byte[]	COMMUNITY_SERVER_HEX_ID;
	public static int		COMMUNITY_SERVER_SQL_DP_ID;
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	public static boolean ENABLE_VITALITY;
	public static boolean RECOVER_VITALITY_ON_RECONNECT;
	public static boolean ENABLE_DROP_VITALITY_HERBS;
	public static float RATE_VITALITY_LEVEL_1;
	public static float RATE_VITALITY_LEVEL_2;
	public static float RATE_VITALITY_LEVEL_3;
	public static float RATE_VITALITY_LEVEL_4;
	public static float RATE_DROP_VITALITY_HERBS;
	public static float RATE_RECOVERY_VITALITY_PEACE_ZONE;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_RECOVERY_ON_RECONNECT;
	public static int STARTING_VITALITY_POINTS;
	public static int MAX_ITEM_IN_PACKET;
	public static boolean CHECK_KNOWN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String[] GAME_SERVER_SUBNETS;
	public static String[] GAME_SERVER_HOSTS;
	public static int NEW_NODE_ID;
	public static int SELECTED_NODE_ID;
	public static int LINKED_NODE_ID;
	public static String NEW_NODE_TYPE;
	public static String SERVER_VERSION;
	public static String SERVER_BUILD_DATE;
	public static String DATAPACK_VERSION;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
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
	public static ObjectMapType MAP_TYPE;
	public static ObjectSetType SET_TYPE;
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
	public static int CRYSTAL_ENCHANT_CHANCE_WEAPON_WARRIOR;
	public static int CRYSTAL_ENCHANT_CHANCE_WEAPON_MAGE;
	public static int CRYSTAL_ENCHANT_CHANCE_ARMOR;
	public static int CRYSTAL_ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static boolean ENCHANT_STEP_ENABLED;
	public static String ENCHANT_STEP_MODE;
	public static int ENCHANT_STEP_STATIC;
	public static double ENCHANT_STEP_DYNAMIC;
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
	public static boolean IS_TELNET_ENABLED;
	public static boolean SHOW_LICENCE;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static int Antharas_Wait_Time;
	public static int Valakas_Wait_Time;
	public static int Interval_Of_Antharas_Spawn;
	public static int Random_Of_Antharas_Spawn;
	public static int Interval_Of_Valakas_Spawn;
	public static int Random_Of_Valakas_Spawn;
	public static int Interval_Of_Baium_Spawn;
	public static int Random_Of_Baium_Spawn;
    public static int BELETH_MIN_PLAYERS; 
    public static int INTERVAL_OF_BELETH_SPAWN; 
    public static int RANDOM_OF_BELETH_SPAWN; 
	public static int Interval_Of_Core_Spawn;
	public static int Random_Of_Core_Spawn;
	public static int Interval_Of_Orfen_Spawn;
	public static int Random_Of_Orfen_Spawn;
	public static int Interval_Of_QueenAnt_Spawn;
	public static int Random_Of_QueenAnt_Spawn;
	public static int Interval_Of_Frintezza_Spawn;
	public static int Random_Of_Frintezza_Spawn;
	public static int MIN_FRINTEZZA_PARTIES;
	public static int MAX_FRINTEZZA_PARTIES;
	public static int Interval_Of_Sailren_Spawn;
	public static int Random_Of_Sailren_Spawn;
	public static int PRVNI_SCENA_PO_STARTU_SERVERU;
	public static int INTERVAL_MEZI_SCENAMA;
    public static int MIN_DAYTIME_ZAKEN_PLAYERS;
    public static int MIN_NIGHTIME_ZAKEN_PLAYERS;
    public static int MIN_TOP_DAYTIME_ZAKEN_PLAYERS;
    public static int MIN_FREYA_PLAYERS;
    public static int MAX_FREYA_PLAYERS;
    public static int MIN_LEVEL_PLAYERS;
    public static int MIN_FREYA_HC_PLAYERS;
    public static int MAX_FREYA_HC_PLAYERS;
    public static int MIN_LEVEL_HC_PLAYERS;
    
	//Gracia Seeds
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	public static int MINIMUM_SOD_PLAYERS;
	public static int MAXIMUM_SOD_PLAYERS;
	public static int MIN_LEVEL_PLAYER_FOR_SOD;
	public static int PREVENT_TO_MUCH_SPAWN_MOBS;
	public static boolean TEST_SEED_OF_DESTRUCTION;
	public static int EXIT_TIME_SOD;
	
	public static ArrayList<String>	FILTER_LIST;
	
	public static boolean SHOW_ONLINE_PLAYERS;
	
	//Conquerable Halls Settings
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;

	public static boolean ENABLE_BLOCK_EXP;
	public static boolean ENABLE_LV_UP_MSG;
	
	// PvP/PK Color System
	public static boolean PVP_COLOR_SYSTEM_ENABLED;
	public static int PVP_AMOUNT1;
	public static int PVP_AMOUNT2;
	public static int PVP_AMOUNT3;
	public static int PVP_AMOUNT4;
	public static int PVP_AMOUNT5;
	public static int NAME_COLOR_FOR_PVP_AMOUNT1;
	public static int NAME_COLOR_FOR_PVP_AMOUNT2;
	public static int NAME_COLOR_FOR_PVP_AMOUNT3;
	public static int NAME_COLOR_FOR_PVP_AMOUNT4;
	public static int NAME_COLOR_FOR_PVP_AMOUNT5;
	public static boolean PK_COLOR_SYSTEM_ENABLED;
	public static int PK_AMOUNT1;
	public static int PK_AMOUNT2;
	public static int PK_AMOUNT3;
	public static int PK_AMOUNT4;
	public static int PK_AMOUNT5;
	public static int TITLE_COLOR_FOR_PK_AMOUNT1;
	public static int TITLE_COLOR_FOR_PK_AMOUNT2;
	public static int TITLE_COLOR_FOR_PK_AMOUNT3;
	public static int TITLE_COLOR_FOR_PK_AMOUNT4;
	public static int TITLE_COLOR_FOR_PK_AMOUNT5;
	
	//server loading info
	public static boolean ENABLE_LOADING_INFO_FOR_SCRIPTS;
	public static boolean ALLOW_KEYBOARD_MOVEMENT;
	
	
	public static void load()
	{
		if(Server.serverMode == Server.MODE_GAMESERVER)
		{
			FLOOD_PROTECTOR_USE_ITEM =
				new FloodProtectorConfig("UseItemFloodProtector");
			FLOOD_PROTECTOR_ROLL_DICE =
				new FloodProtectorConfig("RollDiceFloodProtector");
			FLOOD_PROTECTOR_FIREWORK =
				new FloodProtectorConfig("FireworkFloodProtector");
			FLOOD_PROTECTOR_ITEM_PET_SUMMON =
				new FloodProtectorConfig("ItemPetSummonFloodProtector");
			FLOOD_PROTECTOR_HERO_VOICE =
				new FloodProtectorConfig("HeroVoiceFloodProtector");
			FLOOD_PROTECTOR_GLOBAL_CHAT =
				new FloodProtectorConfig("GlobalChatFloodProtector");
			FLOOD_PROTECTOR_SUBCLASS =
				new FloodProtectorConfig("SubclassFloodProtector");
			FLOOD_PROTECTOR_DROP_ITEM =
				new FloodProtectorConfig("DropItemFloodProtector");
			FLOOD_PROTECTOR_SERVER_BYPASS =
				new FloodProtectorConfig("ServerBypassFloodProtector");
			FLOOD_PROTECTOR_MULTISELL =
				new FloodProtectorConfig("MultiSellFloodProtector");
			FLOOD_PROTECTOR_TRANSACTION =
				new FloodProtectorConfig("TransactionFloodProtector");
			FLOOD_PROTECTOR_MANUFACTURE =
				new FloodProtectorConfig("ManufactureFloodProtector");
			FLOOD_PROTECTOR_MANOR =
				new FloodProtectorConfig("ManorFloodProtector");
			FLOOD_PROTECTOR_SENDMAIL =
				new FloodProtectorConfig("SendMailFloodProtector");
			FLOOD_PROTECTOR_CHARACTER_SELECT =
				new FloodProtectorConfig("CharacterSelectFloodProtector");
			FLOOD_PROTECTOR_ITEM_AUCTION =
				new FloodProtectorConfig("ItemAuctionFloodProtector");
			
			_log.info("Loading GameServer Configuration Files...");
			InputStream is = null;
			try
			{
				try
				{
					L2Properties serverSettings = new L2Properties();
					is = new FileInputStream(new File(CONFIGURATION_FILE));
					serverSettings.load(is);
					
					GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
					PORT_GAME = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
					
					GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort","9014"));
					GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost","127.0.0.1");
					
					REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID","0"));
					ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID","True"));
					
					DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
					DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
					DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
					DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
					DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
					DATABASE_MAX_IDLE_TIME = Integer.parseInt(serverSettings.getProperty("MaximumDbIdleTime", "0"));
					
					DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
					
					CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
					PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
					
					MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "0"));
					MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
					
					String[] protocols = serverSettings.getProperty("AllowedProtocolRevisions", "146;152").split(";");
					PROTOCOL_LIST = new TIntArrayList(protocols.length);
					for (String protocol : protocols)
					{
						try
						{
							PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
						}
						catch(NumberFormatException e)
						{
							_log.info("Wrong config protocol version: "+protocol+". Skipped.");
						}
					}
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
				}
				
				File file = new File(IP_CONFIG_FILE);
				Document doc = null;
				ArrayList <String> subnets = new ArrayList<String>(5);
				ArrayList <String> hosts = new ArrayList<String>(5);
				try
				{
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating(false);
					factory.setIgnoringComments(true);
					doc = factory.newDocumentBuilder().parse(file);
					
					for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
					{
						NamedNodeMap attrs;
						Node att;
						
						if ("gameserver".equalsIgnoreCase(n.getNodeName()))
						{
							for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
							{
								if ("define".equalsIgnoreCase(d.getNodeName()))
								{
									attrs = d.getAttributes();
									
									att = attrs.getNamedItem("subnet");
									if (att == null)
										continue;
									
									subnets.add(att.getNodeValue());
									
									att = attrs.getNamedItem("address");
									if (att == null)
										continue;
									
									hosts.add(att.getNodeValue());
									
									if (hosts.size() != subnets.size())
										throw new Error("Failed to Load "+IP_CONFIG_FILE+" File - subnets does not match server addresses.");
								}
							}
							
							attrs = n.getAttributes();
							
							att = attrs.getNamedItem("address");
							if (att == null)
								throw new Error("Failed to Load "+IP_CONFIG_FILE+" File - default server address is missing.");
							
							subnets.add("0.0.0.0/0");
							hosts.add(att.getNodeValue());
						}
					}
					GAME_SERVER_SUBNETS = subnets.toArray(new String[subnets.size()]);
					GAME_SERVER_HOSTS = hosts.toArray(new String[hosts.size()]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+IP_CONFIG_FILE+" File.");
				}
				
				// Load Community Properties file (if exists)
				try
				{
					L2Properties communityServerSettings	= new L2Properties();
					is = new FileInputStream(new File(COMMUNITY_CONFIGURATION_FILE));
					communityServerSettings.load(is);
					ENABLE_COMMUNITY_BOARD = Boolean.parseBoolean(communityServerSettings.getProperty("EnableCommunityBoard", "False"));
					COMMUNITY_SERVER_ADDRESS = communityServerSettings.getProperty("CommunityServerHostname", "localhost");
					COMMUNITY_SERVER_PORT = Integer.parseInt(communityServerSettings.getProperty("CommunityServerPort", "9013"));
					COMMUNITY_SERVER_HEX_ID = new BigInteger(communityServerSettings.getProperty("CommunityServerHexId"), 16).toByteArray();
					COMMUNITY_SERVER_SQL_DP_ID = Integer.parseInt(communityServerSettings.getProperty("CommunityServerSqlDpId", "200"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+COMMUNITY_CONFIGURATION_FILE+" File.");
				}
				
				//GrandBoss
				// Load Antharas L2Properties file (if exists)
				try
				{
					L2Properties antharas = new L2Properties();
					is = new FileInputStream(new File(ANTHARAS_CONFIG_FILE));
					antharas.load(is);
					Antharas_Wait_Time = Integer.parseInt(antharas.getProperty("AntharasWaitTime", "30"));
					if (Antharas_Wait_Time < 3 || Antharas_Wait_Time > 60)
						Antharas_Wait_Time = 30;
					Antharas_Wait_Time = Antharas_Wait_Time * 60000;
					
					Interval_Of_Antharas_Spawn = Integer.parseInt(antharas.getProperty("IntervalOfAntharasSpawn", "264"));
					if (Interval_Of_Antharas_Spawn < 1 || Interval_Of_Antharas_Spawn > 480)
						Interval_Of_Antharas_Spawn = 192;
					Interval_Of_Antharas_Spawn = Interval_Of_Antharas_Spawn * 3600000;

					Random_Of_Antharas_Spawn = Integer.parseInt(antharas.getProperty("RandomOfAntharasSpawn", "72"));
					if (Random_Of_Antharas_Spawn < 1 || Random_Of_Antharas_Spawn > 192)
						Random_Of_Antharas_Spawn = 145;
					Random_Of_Antharas_Spawn = Random_Of_Antharas_Spawn * 3600000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+ANTHARAS_CONFIG_FILE+" File.");
					}
				
				// Load Valakas L2Properties file (if exists)
				try
				{
					L2Properties valakas = new L2Properties();
					is = new FileInputStream(new File(VALAKAS_CONFIG_FILE));
					valakas.load(is);
					Valakas_Wait_Time = Integer.parseInt(valakas.getProperty("ValakasWaitTime", "30"));
					if (Valakas_Wait_Time < 3 || Valakas_Wait_Time > 60)
						Valakas_Wait_Time = 30;
					Valakas_Wait_Time = Valakas_Wait_Time * 60000;
					
					Interval_Of_Valakas_Spawn = Integer.parseInt(valakas.getProperty("IntervalOfValakasSpawn", "264"));
					if (Interval_Of_Valakas_Spawn < 1 || Interval_Of_Valakas_Spawn > 480)
						Interval_Of_Valakas_Spawn = 192;
					Interval_Of_Valakas_Spawn = Interval_Of_Valakas_Spawn * 3600000;
					
					Random_Of_Valakas_Spawn = Integer.parseInt(valakas.getProperty("RandomOfValakasSpawn", "72"));
					if (Random_Of_Valakas_Spawn < 1 || Random_Of_Valakas_Spawn > 192)
						Random_Of_Valakas_Spawn = 145;
					Random_Of_Valakas_Spawn = Random_Of_Valakas_Spawn * 3600000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+VALAKAS_CONFIG_FILE+" File.");
					}
				
				// Load Baium L2Properties file (if exists)
				try
				{
					L2Properties baium = new L2Properties();
					is = new FileInputStream(new File(BAIUM_CONFIG_FILE));
					baium.load(is);
					Interval_Of_Baium_Spawn = Integer.parseInt(baium.getProperty("IntervalOfBaiumSpawn", "168"));
					if (Interval_Of_Baium_Spawn < 1 || Interval_Of_Baium_Spawn > 480)
						Interval_Of_Baium_Spawn = 121;
					Interval_Of_Baium_Spawn = Interval_Of_Baium_Spawn * 3600000;
					
					Random_Of_Baium_Spawn = Integer.parseInt(baium.getProperty("RandomOfBaiumSpawn", "48"));
					if (Random_Of_Baium_Spawn < 1 || Random_Of_Baium_Spawn > 192)
						Random_Of_Baium_Spawn = 8;
					Random_Of_Baium_Spawn = Random_Of_Baium_Spawn * 3600000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+BAIUM_CONFIG_FILE+" File.");
					}
				
				// Load Beleth L2Properties file (if exists)
				try
				{
					L2Properties beleth = new L2Properties();
					is = new FileInputStream(new File(BELETH_CONFIG_FILE));
					beleth.load(is);
					INTERVAL_OF_BELETH_SPAWN = Integer.parseInt(beleth.getProperty("IntervalOfBelethSpawn", "192"));
					if (INTERVAL_OF_BELETH_SPAWN < 1 || INTERVAL_OF_BELETH_SPAWN > 480)
						INTERVAL_OF_BELETH_SPAWN = 192;
					INTERVAL_OF_BELETH_SPAWN *= 3600000;

					RANDOM_OF_BELETH_SPAWN = Integer.parseInt(beleth.getProperty("RandomOfBelethSpawn", "148"));
					if (RANDOM_OF_BELETH_SPAWN < 1 || RANDOM_OF_BELETH_SPAWN > 192)
						RANDOM_OF_BELETH_SPAWN = 148;
					RANDOM_OF_BELETH_SPAWN *= 3600000;

					BELETH_MIN_PLAYERS = Integer.parseInt(beleth.getProperty("BelethMinPlayers", "36"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+BELETH_CONFIG_FILE+" File.");
					}
				
				// Load Core L2Properties file (if exists)
				try
				{
					L2Properties core = new L2Properties();
					is = new FileInputStream(new File(CORE_CONFIG_FILE));
					core.load(is);
					Interval_Of_Core_Spawn = Integer.parseInt(core.getProperty("IntervalOfCoreSpawn", "60"));
					if (Interval_Of_Core_Spawn < 1 || Interval_Of_Core_Spawn > 480)
						Interval_Of_Core_Spawn = 27;
					Interval_Of_Core_Spawn = Interval_Of_Core_Spawn * 3600000;
					
					Random_Of_Core_Spawn = Integer.parseInt(core.getProperty("RandomOfCoreSpawn", "24"));
					if (Random_Of_Core_Spawn < 1 || Random_Of_Core_Spawn > 192)
						Random_Of_Core_Spawn = 47;
					Random_Of_Core_Spawn = Random_Of_Core_Spawn * 3600000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+CORE_CONFIG_FILE+" File.");
					}
				
				// Load Orfen L2Properties file (if exists)
				try
				{
					L2Properties orfen = new L2Properties();
					is = new FileInputStream(new File(ORFEN_CONFIG_FILE));
					orfen.load(is);
					Interval_Of_Orfen_Spawn = Integer.parseInt(orfen.getProperty("IntervalOfOrfenSpawn", "48"));
					if (Interval_Of_Orfen_Spawn < 1 || Interval_Of_Orfen_Spawn > 480)
						Interval_Of_Orfen_Spawn = 28;
					Interval_Of_Orfen_Spawn = Interval_Of_Orfen_Spawn * 3600000;
					
					Random_Of_Orfen_Spawn = Integer.parseInt(orfen.getProperty("RandomOfOrfenSpawn", "20"));
					if (Random_Of_Orfen_Spawn < 1 || Random_Of_Orfen_Spawn > 192)
						Random_Of_Orfen_Spawn = 41;
					Random_Of_Orfen_Spawn = Random_Of_Orfen_Spawn * 3600000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+ORFEN_CONFIG_FILE+" File.");
					}
				
				// Load Queen Ant L2Properties file (if exists)
				try
				{
					L2Properties queenant = new L2Properties();
					is = new FileInputStream(new File(QUEENANT_CONFIG_FILE));
					queenant.load(is);
					Interval_Of_QueenAnt_Spawn = Integer.parseInt(queenant.getProperty("IntervalOfQueenAntSpawn", "36"));
					if (Interval_Of_QueenAnt_Spawn < 1 || Interval_Of_QueenAnt_Spawn > 480)
						Interval_Of_QueenAnt_Spawn = 19;
					Interval_Of_QueenAnt_Spawn = Interval_Of_QueenAnt_Spawn * 3600000;
					
					Random_Of_QueenAnt_Spawn = Integer.parseInt(queenant.getProperty("RandomOfQueenAntSpawn", "17"));
					if (Random_Of_QueenAnt_Spawn < 1 || Random_Of_QueenAnt_Spawn > 192)
						Random_Of_QueenAnt_Spawn = 35;
					Random_Of_QueenAnt_Spawn = Random_Of_QueenAnt_Spawn * 3600000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+QUEENANT_CONFIG_FILE+" File.");
					}
				
				// Load Frintezza L2Properties file (if exists)
				try
				{
					L2Properties frintezza = new L2Properties();
					is = new FileInputStream(new File(FRINTEZZA_CONFIG_FILE));
					frintezza.load(is);
					Interval_Of_Frintezza_Spawn = Integer.parseInt(frintezza.getProperty("IntervalOfFrintezzaSpawn", "48"));
					if (Interval_Of_Frintezza_Spawn < 1 || Interval_Of_Frintezza_Spawn > 480)
						Interval_Of_Frintezza_Spawn = 121;
					Interval_Of_Frintezza_Spawn = Interval_Of_Frintezza_Spawn * 3600000;
					
					Random_Of_Frintezza_Spawn = Integer.parseInt(frintezza.getProperty("RandomOfFrintezzaSpawn", "8"));
					if (Random_Of_Frintezza_Spawn < 1 || Random_Of_Frintezza_Spawn > 192)
						Random_Of_Frintezza_Spawn = 8;
					Random_Of_Frintezza_Spawn = Random_Of_Frintezza_Spawn * 3600000;
					
					MIN_FRINTEZZA_PARTIES = Integer.parseInt(frintezza.getProperty("MinFrintezzaParties", "4"));
					MAX_FRINTEZZA_PARTIES = Integer.parseInt(frintezza.getProperty("MaxFrintezzaParties", "5"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+FRINTEZZA_CONFIG_FILE+" File.");
					}
				
				// Load Zaken L2Properties file (if exists)
				try
				{
					L2Properties zaken = new L2Properties();
					is = new FileInputStream(new File(ZAKEN_CONFIG_FILE));
					zaken.load(is);
					MIN_DAYTIME_ZAKEN_PLAYERS = Integer.parseInt(zaken.getProperty("MinDaytimeZakenPlayers", "9"));
					MIN_NIGHTIME_ZAKEN_PLAYERS = Integer.parseInt(zaken.getProperty("MinNightimeZakenPlayers", "72"));
					MIN_TOP_DAYTIME_ZAKEN_PLAYERS = Integer.parseInt(zaken.getProperty("MinTopDaytimeZakenPlayers", "9"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+ZAKEN_CONFIG_FILE+" File.");
					}
				
				// Load Sailren L2Properties file (if exists)
				try
				{
					L2Properties sailren = new L2Properties();
					is = new FileInputStream(new File(SAILREN_CONFIG_FILE));
					sailren.load(is);
					Interval_Of_Sailren_Spawn = Integer.parseInt(sailren.getProperty("IntervalOfSailrenSpawn", "12"));
					if (Interval_Of_Sailren_Spawn < 1 || Interval_Of_Sailren_Spawn > 192)
						Interval_Of_Sailren_Spawn = 12;
					Interval_Of_Sailren_Spawn = Interval_Of_Sailren_Spawn * 3600000;
					
					Random_Of_Sailren_Spawn = Integer.parseInt(sailren.getProperty("RandomOfSailrenSpawn", "24"));
					if (Random_Of_Sailren_Spawn < 1 || Random_Of_Sailren_Spawn > 192)
						Random_Of_Sailren_Spawn = 24;
					Random_Of_Sailren_Spawn = Random_Of_Sailren_Spawn * 3600000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+SAILREN_CONFIG_FILE+" File.");
					}
				
				// Load Freya L2Properties file (if exists)
				try
				{
					L2Properties freya = new L2Properties();
					is = new FileInputStream(new File(FREYA_CONFIG_FILE));
					freya.load(is);
					MIN_FREYA_PLAYERS = Integer.parseInt(freya.getProperty("MinFreyaPlayers", "18"));
					MAX_FREYA_PLAYERS = Integer.parseInt(freya.getProperty("MaxFreyaPlayers", "27"));
					MIN_LEVEL_PLAYERS = Integer.parseInt(freya.getProperty("MinLevelPlayers", "82"));
					MIN_FREYA_HC_PLAYERS = Integer.parseInt(freya.getProperty("MinFreyaHcPlayers", "36"));
					MAX_FREYA_HC_PLAYERS = Integer.parseInt(freya.getProperty("MaxFreyaHcPlayers", "45"));
					MIN_LEVEL_HC_PLAYERS = Integer.parseInt(freya.getProperty("MinLevelHcPlayers", "82"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+FREYA_CONFIG_FILE+" File.");
					}
				
				// Load Lindvior L2Properties file (if exists)
				try
				{
					L2Properties lindvior = new L2Properties();
					is = new FileInputStream(new File(LINDVIOR_CONFIG_FILE));
					lindvior.load(is);
					PRVNI_SCENA_PO_STARTU_SERVERU = Integer.parseInt(lindvior.getProperty("PrvniScenaPoStartuServeru", "3600"));
					if (PRVNI_SCENA_PO_STARTU_SERVERU < 1 || PRVNI_SCENA_PO_STARTU_SERVERU > 86400)
						PRVNI_SCENA_PO_STARTU_SERVERU = 3600;
					PRVNI_SCENA_PO_STARTU_SERVERU = PRVNI_SCENA_PO_STARTU_SERVERU * 60000;
					
					INTERVAL_MEZI_SCENAMA = Integer.parseInt(lindvior.getProperty("IntervalMeziScenama", "21600"));
					if (INTERVAL_MEZI_SCENAMA < 1 || INTERVAL_MEZI_SCENAMA > 86400)
						INTERVAL_MEZI_SCENAMA = 21600;
					INTERVAL_MEZI_SCENAMA = INTERVAL_MEZI_SCENAMA * 60000;
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+LINDVIOR_CONFIG_FILE+" File.");
					}
				
				// Load MODS L2Properties file (if exists)
				try
				{
					L2Properties eventmods = new L2Properties();
					is = new FileInputStream(new File(EVENTMODS_CONFIG_FILE));
					eventmods.load(is);
					ENABLE_ELPY = Boolean.parseBoolean(eventmods.getProperty("EnableElpyEvent", "False"));
					EVENT_INTERVAL_ELPIES = Integer.parseInt(eventmods.getProperty("EventIntervalElpies", "180"));
					EVENT_TIME_ELPIES = Integer.parseInt(eventmods.getProperty("EventTimeElpies", "2"));
					EVENT_NUMBER_OF_SPAWNED_ELPIES = Integer.parseInt(eventmods.getProperty("EventNumberOfSpawnedElpies", "100"));
					ENABLE_RABBITS = Boolean.parseBoolean(eventmods.getProperty("EnableRabbitsEvent", "False"));
					EVENT_INTERVAL_RABBITS = Integer.parseInt(eventmods.getProperty("EventIntervalRabbits", "240"));
					EVENT_TIME_RABBITS = Integer.parseInt(eventmods.getProperty("EventTimeRabbits", "10"));
					EVENT_NUMBER_OF_SPAWNED_CHESTS = Integer.parseInt(eventmods.getProperty("EventNumberOfSpawnedChest", "100"));
					ENABLE_RACE = Boolean.parseBoolean(eventmods.getProperty("EnableRaceEvent", "False"));
					EVENT_INTERVAL_RACE = Integer.parseInt(eventmods.getProperty("EventIntervalRace", "300"));
					EVENT_REG_TIME_RACE = Integer.parseInt(eventmods.getProperty("EventRegTimeRace", "5"));
					EVENT_RUNNING_TIME_RACE = Integer.parseInt(eventmods.getProperty("EventRunningTimeRace", "10"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+EVENTMODS_CONFIG_FILE+" File.");
				}
				
				// Load VOTEREWARD L2Properties file (if exists)
				try
				{
					L2Properties votereward = new L2Properties();
					is = new FileInputStream(new File(VOTEREWARD_CONFIG_FILE));
					votereward.load(is);					
										VOTE_HTML_PATCH = votereward.getProperty("VoteHtmlPatch", "Null");
										VOTE_REWARD1_COUNT = Integer.parseInt(votereward.getProperty("VoteReward1Count", "2"));
										VOTE_REWARD2_COUNT = Integer.parseInt(votereward.getProperty("VoteReward2Count", "5"));
										VOTE_REWARD1_ID = Integer.parseInt(votereward.getProperty("VoteReward1Id", "33340"));
										VOTE_REWARD2_ID = Integer.parseInt(votereward.getProperty("VoteReward2Id", "33399"));
										VOTES_FOR_REWARD = Integer.parseInt(votereward.getProperty("VotesForReward", "10"));
										VOTE_REWARD_ENABLE = Boolean.parseBoolean(votereward.getProperty("EnableVoteReward", "False"));
										SERVER_NAME_FOR_VOTES                     = votereward.getProperty("ServerNameForVotes", "L2NeXtGeN");
										MAX_REWARD_COUNT_FOR_STACK_ITEM1 = Integer.parseInt(votereward.getProperty("MaxRewardCountForStackItem1", "2000000000"));
										MAX_REWARD_COUNT_FOR_STACK_ITEM2 = Integer.parseInt(votereward.getProperty("MaxRewardCountForStackItem2", "2000000000"));
										DELAY_FOR_NEXT_REWARD = Integer.parseInt(votereward.getProperty("DelayForNextReward", "600"));
										ONLINE_PLAYERS = Boolean.parseBoolean(votereward.getProperty("OnlinePLayers", "True"));
										CHAR_TITLE              = Boolean.parseBoolean(votereward.getProperty("CharTitle", "true"));
										ADD_CHAR_TITLE          = votereward.getProperty("CharAddTitle", "* Vote For Us *");
										ALT_GAME_FLAGED_PLAYER_CAN_USE_GK	= Boolean.parseBoolean(votereward.getProperty("AltFlagedPlayerCanUseGK", "false"));
										ALT_OLY_SAME_IP                                     = Boolean.parseBoolean(votereward.getProperty("AltOlySameIp", "true"));
										
					L2JMOD_VOTE_ENGINE_ENABLE = Boolean.parseBoolean(votereward.getProperty("EnableAutoVoteEngine", "false"));
					L2JMOD_VOTE_ENGINE_SAVE = Boolean.parseBoolean(votereward.getProperty("AutoVoteEngineSaveLoad", "false"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+VOTEREWARD_CONFIG_FILE+" File.");
				}
				
				// Load DM L2Properties file (if exists)
				try
				{
					L2Properties dmSettings = new L2Properties();
					is = new FileInputStream(new File(DM_CONFIG_FILE));
					dmSettings.load(is);
					Long time = 0L;					
					DM_EVENT_ENABLED = Boolean.parseBoolean(dmSettings.getProperty("DMEventEnabled", "False"));
					DM_EVENT_IN_INSTANCE = Boolean.parseBoolean(dmSettings.getProperty("DMEventInInstance", "False"));
					DM_EVENT_INSTANCE_FILE = dmSettings.getProperty("DMEventInstanceFile", "coliseum.xml");
					DM_EVENT_INTERVAL = dmSettings.getProperty("DMEventInterval", "8:00,14:00,20:00,2:00").split(",");
					String[] timeParticipation = dmSettings.getProperty("DMEventParticipationTime", "01:00:00").split(":");
					time = 0L;
					time += Long.parseLong(timeParticipation[0]) * 3600L;
					time += Long.parseLong(timeParticipation[1]) * 60L;
					time += Long.parseLong(timeParticipation[2]);
					DM_EVENT_PARTICIPATION_TIME = time * 1000L;
					DM_EVENT_RUNNING_TIME = Integer.parseInt(dmSettings.getProperty("DMEventRunningTime", "1800"));
					DM_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(dmSettings.getProperty("DMEventParticipationNpcId", "0"));
					DM_SHOW_TOP_RANK = Boolean.parseBoolean(dmSettings.getProperty("DMShowTopRank", "False"));
					DM_TOP_RANK = Integer.parseInt(dmSettings.getProperty("DMTopRank", "10"));
					if (DM_EVENT_PARTICIPATION_NPC_ID == 0)
					{
						DM_EVENT_ENABLED = false;
						_log.warning("DMEventEngine[Config.load()]: invalid config property -> DMEventParticipationNpcId");
					}
					else
					{
						String[] propertySplit = dmSettings.getProperty("DMEventParticipationNpcCoordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							DM_EVENT_ENABLED = false;
							_log.warning("DMEventEngine[Config.load()]: invalid config property -> DMEventParticipationNpcCoordinates");
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
								
								if (propertySplit.length == 4) DM_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
								DM_EVENT_MIN_PLAYERS = Integer.parseInt(dmSettings.getProperty("DMEventMinPlayers", "1"));
								DM_EVENT_MAX_PLAYERS = Integer.parseInt(dmSettings.getProperty("DMEventMaxPlayers", "20"));
								DM_EVENT_MIN_LVL = (byte) Integer.parseInt(dmSettings.getProperty("DMEventMinPlayerLevel", "1"));
								DM_EVENT_MAX_LVL = (byte) Integer.parseInt(dmSettings.getProperty("DMEventMaxPlayerLevel", "80"));
								DM_EVENT_RESPAWN_TELEPORT_DELAY = Integer.parseInt(dmSettings.getProperty("DMEventRespawnTeleportDelay", "20"));
								DM_EVENT_START_LEAVE_TELEPORT_DELAY = Integer.parseInt(dmSettings.getProperty("DMEventStartLeaveTeleportDelay", "20"));
								DM_EVENT_EFFECTS_REMOVAL = Integer.parseInt(dmSettings.getProperty("DMEventEffectsRemoval", "0"));
								DM_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(dmSettings.getProperty("DMAllowVoicedInfoCommand", "True"));
								
								propertySplit = dmSettings.getProperty("DMEventParticipationFee", "0,0").split(",");
								try
								{
									DM_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
									DM_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
								}
								catch (NumberFormatException nfe)
								{
									if (propertySplit.length > 0) _log.warning("DMEventEngine[Config.load()]: invalid config property -> DMEventParticipationFee");
								}
								
								DM_REWARD_FIRST_PLAYERS = Integer.parseInt(dmSettings.getProperty("DMRewardFirstPlayers", "3"));
								
								propertySplit = dmSettings.getProperty("DMEventReward", "57,100000;5575,5000|57,50000|57,25000").split("\\|");
								int i = 1;
								if (DM_REWARD_FIRST_PLAYERS < propertySplit.length) _log.warning("DMEventEngine[Config.load()]: invalid config property -> DMRewardFirstPlayers < DMEventReward");
								else
								{
									for (String pos : propertySplit)
									{
										List<int[]> value = new ArrayList<int[]>();
										String[] rewardSplit = pos.split("\\;");
										for (String rewards : rewardSplit)
										{
											String[] reward = rewards.split("\\,");
											if (reward.length != 2) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventReward \"", pos, "\""));
											else
											{
												try
												{
													value.add(new int[] { Integer.parseInt(reward[0]), Integer.parseInt(reward[1]) });
												}
												catch (NumberFormatException nfe)
												{
													_log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventReward \"", pos, "\""));
												}
											}
											
											try
											{
												if (value.isEmpty()) DM_EVENT_REWARDS.put(i, DM_EVENT_REWARDS.get(i - 1));
												else DM_EVENT_REWARDS.put(i, value);
											}
											catch (Exception e)
											{
												_log.warning("DMEventEngine[Config.load()]: invalid config property -> DMEventReward array index out of bounds (1)");
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
												_log.warning("DMEventEngine[Config.load()]: invalid config property -> DMEventReward array index out of bounds (2)");
												e.printStackTrace();
											}
										}
									}
								}
								
								propertySplit = dmSettings.getProperty("DMEventPlayerCoordinates", "0,0,0").split(";");
								for (String coordPlayer : propertySplit)
								{
									String[] coordSplit = coordPlayer.split(",");
									if (coordSplit.length != 3) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventPlayerCoordinates \"", coordPlayer, "\""));
									else
									{
										try
										{
											DM_EVENT_PLAYER_COORDINATES.add(new int[] { Integer.parseInt(coordSplit[0]), Integer.parseInt(coordSplit[1]), Integer.parseInt(coordSplit[2]) });
										}
										catch (NumberFormatException nfe)
										{
											if (!coordPlayer.isEmpty()) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventPlayerCoordinates \"", coordPlayer, "\""));
										}
									}
								}
								
								DM_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(dmSettings.getProperty("DMEventScrollsAllowed", "False"));
								DM_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(dmSettings.getProperty("DMEventPotionsAllowed", "False"));
								DM_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(dmSettings.getProperty("DMEventSummonByItemAllowed", "False"));
								DM_REWARD_PLAYERS_TIE = Boolean.parseBoolean(dmSettings.getProperty("DMRewardPlayersTie", "False"));
								
								propertySplit = dmSettings.getProperty("DMDoorsToOpen", "").split(";");
								for (String door : propertySplit)
								{
									try
									{
										DM_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
									}
									catch (NumberFormatException nfe)
									{
										if (!door.isEmpty()) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMDoorsToOpen \"", door, "\""));
									}
								}
								
								propertySplit = dmSettings.getProperty("DMDoorsToClose", "").split(";");
								for (String door : propertySplit)
								{
									try
									{
										DM_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
									}
									catch (NumberFormatException nfe)
									{
										if (!door.isEmpty()) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMDoorsToClose \"", door, "\""));
									}
								}
								
								propertySplit = dmSettings.getProperty("DMEventFighterBuffs", "").split(";");
								if (!propertySplit[0].isEmpty())
								{
									DM_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(propertySplit.length);
									for (String skill : propertySplit)
									{
										String[] skillSplit = skill.split(",");
										if (skillSplit.length != 2) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventFighterBuffs \"", skill, "\""));
										else
										{
											try
											{
												DM_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
											}
											catch (NumberFormatException nfe)
											{
												if (!skill.isEmpty()) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventFighterBuffs \"", skill, "\""));
											}
										}
									}
								}
								
								propertySplit = dmSettings.getProperty("DMEventMageBuffs", "").split(";");
								if (!propertySplit[0].isEmpty())
								{
									DM_EVENT_MAGE_BUFFS = new TIntIntHashMap(propertySplit.length);
									for (String skill : propertySplit)
									{
										String[] skillSplit = skill.split(",");
										if (skillSplit.length != 2) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventMageBuffs \"", skill, "\""));
										else
										{
											try
											{
												DM_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
											}
											catch (NumberFormatException nfe)
											{
												if (!skill.isEmpty()) _log.warning(StringUtil.concat("DMEventEngine[Config.load()]: invalid config property -> DMEventMageBuffs \"", skill, "\""));
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
					throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
				}

				// Load CTF L2Properties file (if exists)
				try
				{
					L2Properties ctfSettings = new L2Properties();
					is = new FileInputStream(new File(CTF_CONFIG_FILE));
					ctfSettings.load(is);
					CTF_EVENT_ENABLED = Boolean.parseBoolean(ctfSettings.getProperty("CTFEventEnabled", "false"));
					CTF_EVENT_INTERVAL = ctfSettings.getProperty("CTFEventInterval", "20:00").split(",");
					CTF_EVEN_TEAMS = ctfSettings.getProperty("CTFEvenTeams", "BALANCE");
					CTF_ALLOW_VOICE_COMMAND = Boolean.parseBoolean(ctfSettings.getProperty("CTFAllowVoiceCommand", "false"));
					CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(ctfSettings.getProperty("CTFAllowInterference", "false"));
					CTF_ALLOW_POTIONS = Boolean.parseBoolean(ctfSettings.getProperty("CTFAllowPotions", "false"));
					CTF_ALLOW_SUMMON = Boolean.parseBoolean(ctfSettings.getProperty("CTFAllowSummon", "false"));
					CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(ctfSettings.getProperty("CTFOnStartRemoveAllEffects", "true"));
					CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(ctfSettings.getProperty("CTFOnStartUnsummonPet", "true"));
					CTF_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(ctfSettings.getProperty("CTFAnnounceTeamStats", "false"));
					CTF_ANNOUNCE_REWARD = Boolean.parseBoolean(ctfSettings.getProperty("CTFAnnounceReward", "false"));
					CTF_JOIN_CURSED = Boolean.parseBoolean(ctfSettings.getProperty("CTFJoinWithCursedWeapon", "true"));
					CTF_REVIVE_RECOVERY = Boolean.parseBoolean(ctfSettings.getProperty("CTFReviveRecovery", "false"));
					CTF_REVIVE_DELAY = Long.parseLong(ctfSettings.getProperty("CTFReviveDelay", "20000"));
					CTF_BASE_TELEPORT_FIRST = Boolean.parseBoolean(ctfSettings.getProperty("CTFTeleportToBaseFirst", "false"));
					
					if (CTF_REVIVE_DELAY < 1000)
						CTF_REVIVE_DELAY = 1000; //can't be set less then 1 second	
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CTF_CONFIG_FILE + " File.");
				}
				
				// Load LM L2Properties file (if exists)
				try
				{
					L2Properties LmSettings = new L2Properties();
					is = new FileInputStream(new File(LM_CONFIG_FILE));
					LmSettings.load(is);
					Long time = 0L;
					LM_EVENT_ENABLED = Boolean.parseBoolean(LmSettings.getProperty("LMEventEnabled", "False"));
					LM_EVENT_IN_INSTANCE = Boolean.parseBoolean(LmSettings.getProperty("LMEventInInstance", "False"));
					LM_EVENT_INSTANCE_FILE = LmSettings.getProperty("LMEventInstanceFile", "coliseum.xml");
					LM_EVENT_INTERVAL = LmSettings.getProperty("LMEventInterval", "8:00,14:00,20:00,2:00").split(",");
					String[] timeParticipation = LmSettings.getProperty("LMEventParticipationTime", "01:00:00").split(":");
					time = 0L;
					time += Long.parseLong(timeParticipation[0]) * 3600L;
					time += Long.parseLong(timeParticipation[1]) * 60L;
					time += Long.parseLong(timeParticipation[2]);
					LM_EVENT_PARTICIPATION_TIME = time * 1000L;
					LM_EVENT_RUNNING_TIME = Integer.parseInt(LmSettings.getProperty("LMEventRunningTime", "1800"));
					LM_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(LmSettings.getProperty("LMEventParticipationNpcId", "0"));
					short credits = Short.parseShort(LmSettings.getProperty("LMEventPlayerCredits", "1"));
					LM_EVENT_PLAYER_CREDITS = (credits > 0 ? credits : 1);
					if (LM_EVENT_PARTICIPATION_NPC_ID == 0)
					{
						LM_EVENT_ENABLED = false;
						_log.warning("LMEventEngine[Config.load()]: invalid config property -> LMEventParticipationNpcId");
					}
					else
					{
						String[] propertySplit = LmSettings.getProperty("LMEventParticipationNpcCoordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							LM_EVENT_ENABLED = false;
							_log.warning("LMEventEngine[Config.load()]: invalid config property -> LMEventParticipationNpcCoordinates");
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
								
								if (propertySplit.length == 4) LM_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
								LM_EVENT_MIN_PLAYERS = Integer.parseInt(LmSettings.getProperty("LMEventMinPlayers", "1"));
								LM_EVENT_MAX_PLAYERS = Integer.parseInt(LmSettings.getProperty("LMEventMaxPlayers", "20"));
								LM_EVENT_MIN_LVL = (byte) Integer.parseInt(LmSettings.getProperty("LMEventMinPlayerLevel", "1"));
								LM_EVENT_MAX_LVL = (byte) Integer.parseInt(LmSettings.getProperty("LMEventMaxPlayerLevel", "80"));
								LM_EVENT_RESPAWN_TELEPORT_DELAY = Integer.parseInt(LmSettings.getProperty("LMEventRespawnTeleportDelay", "20"));
								LM_EVENT_START_LEAVE_TELEPORT_DELAY = Integer.parseInt(LmSettings.getProperty("LMEventStartLeaveTeleportDelay", "20"));
								LM_EVENT_EFFECTS_REMOVAL = Integer.parseInt(LmSettings.getProperty("LMEventEffectsRemoval", "0"));
								LM_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(LmSettings.getProperty("LMAllowVoicedInfoCommand", "True"));
								
								propertySplit = LmSettings.getProperty("LMEventParticipationFee", "0,0").split(",");
								try
								{
									LM_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
									LM_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
								}
								catch (NumberFormatException nfe)
								{
									if (propertySplit.length > 0) _log.warning("LMEventEngine[Config.load()]: invalid config property -> LMEventParticipationFee");
								}
								
								propertySplit = LmSettings.getProperty("LMEventReward", "57,100000;5575,5000").split("\\;");
								for (String reward : propertySplit)
								{
									String[] rewardSplit = reward.split("\\,");
									try
									{
										LM_EVENT_REWARDS.add(new int[] { Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1]) });
									}
									catch (NumberFormatException nfe)
									{
										_log.warning("LMEventEngine[Config.load()]: invalid config property -> LM_EVENT_REWARDS");
									}
								}
								
								propertySplit = LmSettings.getProperty("LMEventPlayerCoordinates", "0,0,0").split(";");
								for (String coordPlayer : propertySplit)
								{
									String[] coordSplit = coordPlayer.split(",");
									if (coordSplit.length != 3) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMEventPlayerCoordinates \"", coordPlayer, "\""));
									else
									{
										try
										{
											LM_EVENT_PLAYER_COORDINATES.add(new int[] { Integer.parseInt(coordSplit[0]), Integer.parseInt(coordSplit[1]), Integer.parseInt(coordSplit[2]) });
										}
										catch (NumberFormatException nfe)
										{
											if (!coordPlayer.isEmpty()) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMEventPlayerCoordinates \"", coordPlayer, "\""));
										}
									}
								}
								
								LM_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(LmSettings.getProperty("LMEventScrollsAllowed", "False"));
								LM_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(LmSettings.getProperty("LMEventPotionsAllowed", "False"));
								LM_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(LmSettings.getProperty("LMEventSummonByItemAllowed", "False"));
								LM_REWARD_PLAYERS_TIE = Boolean.parseBoolean(LmSettings.getProperty("LMRewardPlayersTie", "False"));

								propertySplit = LmSettings.getProperty("LMDoorsToOpen", "").split(";");
								for (String door : propertySplit)
								{
									try
									{
										LM_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
									}
									catch (NumberFormatException nfe)
									{
										if (!door.isEmpty()) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMDoorsToOpen \"", door, "\""));
									}
								}
								
								propertySplit = LmSettings.getProperty("LMDoorsToClose", "").split(";");
								for (String door : propertySplit)
								{
									try
									{
										LM_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
									}
									catch (NumberFormatException nfe)
									{
										if (!door.isEmpty()) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMDoorsToClose \"", door, "\""));
									}
								}
								
								propertySplit = LmSettings.getProperty("LMEventFighterBuffs", "").split(";");
								if (!propertySplit[0].isEmpty())
								{
									LM_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(propertySplit.length);
									for (String skill : propertySplit)
									{
										String[] skillSplit = skill.split(",");
										if (skillSplit.length != 2) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMEventFighterBuffs \"", skill, "\""));
										else
										{
											try
											{
												LM_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
											}
											catch (NumberFormatException nfe)
											{
												if (!skill.isEmpty()) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMEventFighterBuffs \"", skill, "\""));
											}
										}
									}
								}
								
								propertySplit = LmSettings.getProperty("LMEventMageBuffs", "").split(";");
								if (!propertySplit[0].isEmpty())
								{
									LM_EVENT_MAGE_BUFFS = new TIntIntHashMap(propertySplit.length);
									for (String skill : propertySplit)
									{
										String[] skillSplit = skill.split(",");
										if (skillSplit.length != 2) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMEventMageBuffs \"", skill, "\""));
										else
										{
											try
											{
												LM_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
											}
											catch (NumberFormatException nfe)
											{
												if (!skill.isEmpty()) _log.warning(StringUtil.concat("LMEventEngine[Config.load()]: invalid config property -> LMEventMageBuffs \"", skill, "\""));
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
					throw new Error("Failed to Load " + CTF_CONFIG_FILE + " File.");
				}
				
				// Load underground coliusem L2Properties file (if exists)
				try
				{
					L2Properties undergroundcoliseum = new L2Properties();
					is = new FileInputStream(new File(UNDERGROUNDCOLI_CONFIG_FILE));
					undergroundcoliseum.load(is);
					UC_START_HOUR = Integer.parseInt(undergroundcoliseum.getProperty("StartHour", "21"));
					UC_END_HOUR = Integer.parseInt(undergroundcoliseum.getProperty("EndHour", "23"));
					UC_ROUND_TIME = Integer.parseInt(undergroundcoliseum.getProperty("RoundTime", "10"));
					UC_PARTY_LIMIT = Integer.parseInt(undergroundcoliseum.getProperty("PartyLimit", "7"));
				}		
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+UNDERGROUNDCOLI_CONFIG_FILE+" File.");
					}
				
				// Load Feature L2Properties file (if exists)
				try
				{
					L2Properties Feature = new L2Properties();
					is = new FileInputStream(new File(FEATURE_CONFIG_FILE));
					Feature.load(is);
					
					CH_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallTeleportFunctionFeeRatio", "604800000"));
					CH_TELE1_FEE = Integer.parseInt(Feature.getProperty("ClanHallTeleportFunctionFeeLvl1", "7000"));
					CH_TELE2_FEE = Integer.parseInt(Feature.getProperty("ClanHallTeleportFunctionFeeLvl2", "14000"));
					CH_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallSupportFunctionFeeRatio", "86400000"));
					CH_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl1", "2500"));
					CH_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl2", "5000"));
					CH_SUPPORT3_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl3", "7000"));
					CH_SUPPORT4_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl4", "11000"));
					CH_SUPPORT5_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl5", "21000"));
					CH_SUPPORT6_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl6", "36000"));
					CH_SUPPORT7_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl7", "37000"));
					CH_SUPPORT8_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl8", "52000"));
					CH_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallMpRegenerationFunctionFeeRatio", "86400000"));
					CH_MPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl1", "2000"));
					CH_MPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl2", "3750"));
					CH_MPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl3", "6500"));
					CH_MPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl4", "13750"));
					CH_MPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl5", "20000"));
					CH_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallHpRegenerationFunctionFeeRatio", "86400000"));
					CH_HPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl1", "700"));
					CH_HPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl2", "800"));
					CH_HPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl3", "1000"));
					CH_HPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl4", "1166"));
					CH_HPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl5", "1500"));
					CH_HPREG6_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl6", "1750"));
					CH_HPREG7_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl7", "2000"));
					CH_HPREG8_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl8", "2250"));
					CH_HPREG9_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl9", "2500"));
					CH_HPREG10_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl10", "3250"));
					CH_HPREG11_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl11", "3270"));
					CH_HPREG12_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl12", "4250"));
					CH_HPREG13_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl13", "5166"));
					CH_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallExpRegenerationFunctionFeeRatio", "86400000"));
					CH_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl1", "3000"));
					CH_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl2", "6000"));
					CH_EXPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl3", "9000"));
					CH_EXPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl4", "15000"));
					CH_EXPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl5", "21000"));
					CH_EXPREG6_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl6", "23330"));
					CH_EXPREG7_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl7", "30000"));
					CH_ITEM_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallItemCreationFunctionFeeRatio", "86400000"));
					CH_ITEM1_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl1", "30000"));
					CH_ITEM2_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl2", "70000"));
					CH_ITEM3_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl3", "140000"));
					CH_CURTAIN_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallCurtainFunctionFeeRatio", "604800000"));
					CH_CURTAIN1_FEE = Integer.parseInt(Feature.getProperty("ClanHallCurtainFunctionFeeLvl1", "2000"));
					CH_CURTAIN2_FEE = Integer.parseInt(Feature.getProperty("ClanHallCurtainFunctionFeeLvl2", "2500"));
					CH_FRONT_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallFrontPlatformFunctionFeeRatio", "259200000"));
					CH_FRONT1_FEE = Integer.parseInt(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "1300"));
					CH_FRONT2_FEE = Integer.parseInt(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "4000"));
					CH_BUFF_FREE = Boolean.parseBoolean(Feature.getProperty("AltClanHallMpBuffFree", "False"));
					
					CL_SET_SIEGE_TIME_LIST = new ArrayList<String>();
					SIEGE_HOUR_LIST_MORNING = new ArrayList<Integer>();
					SIEGE_HOUR_LIST_AFTERNOON = new ArrayList<Integer>();
					String[] sstl = Feature.getProperty("CLSetSiegeTimeList", "").split(",");
					if (sstl.length != 0)
					{
						boolean isHour = false;
						for (String st : sstl)
						{
							if (st.equalsIgnoreCase("day") || st.equalsIgnoreCase("hour") || st.equalsIgnoreCase("minute"))
							{
								if (st.equalsIgnoreCase("hour")) isHour = true;
								CL_SET_SIEGE_TIME_LIST.add(st.toLowerCase());
							}
							else
							{
								_log.warning(StringUtil.concat("[CLSetSiegeTimeList]: invalid config property -> CLSetSiegeTimeList \"", st, "\""));
							}
						}
						if (isHour)
						{
							String[] shl = Feature.getProperty("SiegeHourList", "").split(",");
							for (String st : shl)
							{
								if (!st.equalsIgnoreCase(""))
								{
									int val = Integer.parseInt(st);
									if (val > 23 || val < 0)
										_log.warning(StringUtil.concat("[SiegeHourList]: invalid config property -> SiegeHourList \"", st, "\""));
									else if (val < 12)
										SIEGE_HOUR_LIST_MORNING.add(val);
									else
									{
										val -= 12;
										SIEGE_HOUR_LIST_AFTERNOON.add(val);
									}
								}
							}
							if (Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty() && Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
							{
								_log.warning("[SiegeHourList]: invalid config property -> SiegeHourList is empty");
								CL_SET_SIEGE_TIME_LIST.remove("hour");
							}
						}
					}
					CS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleTeleportFunctionFeeRatio", "604800000"));
					CS_TELE1_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl1", "7000"));
					CS_TELE2_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl2", "14000"));
					CS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleSupportFunctionFeeRatio", "86400000"));
					CS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl1", "7000"));
					CS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl2", "21000"));
					CS_SUPPORT3_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl3", "37000"));
					CS_SUPPORT4_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl4", "52000"));
					CS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleMpRegenerationFunctionFeeRatio", "86400000"));
					CS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl1", "2000"));
					CS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl2", "6500"));
					CS_MPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl3", "13750"));
					CS_MPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl4", "20000"));
					CS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleHpRegenerationFunctionFeeRatio", "86400000"));
					CS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl1", "1000"));
					CS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl2", "1500"));
					CS_HPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl3", "2250"));
					CS_HPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl4", "3270"));
					CS_HPREG5_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl5", "5166"));
					CS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleExpRegenerationFunctionFeeRatio", "86400000"));
					CS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl1", "9000"));
					CS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl2", "15000"));
					CS_EXPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl3", "21000"));
					CS_EXPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl4", "30000"));
					
					FS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressTeleportFunctionFeeRatio", "604800000"));
					FS_TELE1_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl1", "1000"));
					FS_TELE2_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl2", "10000"));
					FS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressSupportFunctionFeeRatio", "86400000"));
					FS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl1", "7000"));
					FS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl2", "17000"));
					FS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressMpRegenerationFunctionFeeRatio", "86400000"));
					FS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl1", "6500"));
					FS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl2", "9300"));
					FS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressHpRegenerationFunctionFeeRatio", "86400000"));
					FS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl1", "2000"));
					FS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl2", "3500"));
					FS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressExpRegenerationFunctionFeeRatio", "86400000"));
					FS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl1", "9000"));
					FS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl2", "10000"));
					FS_UPDATE_FRQ = Integer.parseInt(Feature.getProperty("FortressPeriodicUpdateFrequency", "360"));
					FS_BLOOD_OATH_COUNT = Integer.parseInt(Feature.getProperty("FortressBloodOathCount", "1"));
					FS_MAX_SUPPLY_LEVEL = Integer.parseInt(Feature.getProperty("FortressMaxSupplyLevel", "6"));
					FS_FEE_FOR_CASTLE = Integer.parseInt(Feature.getProperty("FortressFeeForCastle", "25000"));
					FS_MAX_OWN_TIME = Integer.parseInt(Feature.getProperty("FortressMaximumOwnTime", "168"));
					
					ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(Feature.getProperty("AltCastleForDawn", "True"));
					ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(Feature.getProperty("AltCastleForDusk", "True"));
					ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(Feature.getProperty("AltRequireClanCastle", "False"));
					ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(Feature.getProperty("AltFestivalMinPlayer", "5"));
					ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(Feature.getProperty("AltMaxPlayerContrib", "1000000"));
					ALT_FESTIVAL_MANAGER_START = Long.parseLong(Feature.getProperty("AltFestivalManagerStart", "120000"));
					ALT_FESTIVAL_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalLength", "1080000"));
					ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalCycleLength", "2280000"));
					ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalFirstSpawn", "120000"));
					ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(Feature.getProperty("AltFestivalFirstSwarm", "300000"));
					ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalSecondSpawn", "540000"));
					ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(Feature.getProperty("AltFestivalSecondSwarm", "720000"));
					ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalChestSpawn", "900000"));
					ALT_SIEGE_DAWN_GATES_PDEF_MULT = Double.parseDouble(Feature.getProperty("AltDawnGatesPdefMult", "1.1"));
					ALT_SIEGE_DUSK_GATES_PDEF_MULT = Double.parseDouble(Feature.getProperty("AltDuskGatesPdefMult", "0.8"));
					ALT_SIEGE_DAWN_GATES_MDEF_MULT = Double.parseDouble(Feature.getProperty("AltDawnGatesMdefMult", "1.1"));
					ALT_SIEGE_DUSK_GATES_MDEF_MULT = Double.parseDouble(Feature.getProperty("AltDuskGatesMdefMult", "0.8"));
					ALT_STRICT_SEVENSIGNS = Boolean.parseBoolean(Feature.getProperty("StrictSevenSigns", "True"));
					ALT_SEVENSIGNS_LAZY_UPDATE = Boolean.parseBoolean(Feature.getProperty("AltSevenSignsLazyUpdate", "True"));
					ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(Feature.getProperty("AnnounceMammonSpawn", "False"));
					
					TAKE_FORT_POINTS = Integer.parseInt(Feature.getProperty("TakeFortPoints", "200"));
					LOOSE_FORT_POINTS = Integer.parseInt(Feature.getProperty("LooseFortPoints", "0"));
					TAKE_CASTLE_POINTS = Integer.parseInt(Feature.getProperty("TakeCastlePoints", "1500"));
					LOOSE_CASTLE_POINTS = Integer.parseInt(Feature.getProperty("LooseCastlePoints", "3000"));
					CASTLE_DEFENDED_POINTS = Integer.parseInt(Feature.getProperty("CastleDefendedPoints", "750"));
					FESTIVAL_WIN_POINTS = Integer.parseInt(Feature.getProperty("FestivalOfDarknessWin", "200"));
					HERO_POINTS = Integer.parseInt(Feature.getProperty("HeroPoints", "1000"));
					ROYAL_GUARD_COST = Integer.parseInt(Feature.getProperty("CreateRoyalGuardCost", "5000"));
					KNIGHT_UNIT_COST = Integer.parseInt(Feature.getProperty("CreateKnightUnitCost", "10000"));
					KNIGHT_REINFORCE_COST = Integer.parseInt(Feature.getProperty("ReinforceKnightUnitCost", "5000"));
					BALLISTA_POINTS = Integer.parseInt(Feature.getProperty("KillBallistaPoints", "30"));
					BLOODALLIANCE_POINTS = Integer.parseInt(Feature.getProperty("BloodAlliancePoints", "500"));
					BLOODOATH_POINTS = Integer.parseInt(Feature.getProperty("BloodOathPoints", "200"));
					KNIGHTSEPAULETTE_POINTS = Integer.parseInt(Feature.getProperty("KnightsEpaulettePoints", "20"));
					REPUTATION_SCORE_PER_KILL = Integer.parseInt(Feature.getProperty("ReputationScorePerKill", "1"));
					JOIN_ACADEMY_MIN_REP_SCORE = Integer.parseInt(Feature.getProperty("CompleteAcademyMinPoints", "190"));
					JOIN_ACADEMY_MAX_REP_SCORE = Integer.parseInt(Feature.getProperty("CompleteAcademyMaxPoints", "650"));
					RAID_RANKING_1ST = Integer.parseInt(Feature.getProperty("1stRaidRankingPoints", "1250"));
					RAID_RANKING_2ND = Integer.parseInt(Feature.getProperty("2ndRaidRankingPoints", "900"));
					RAID_RANKING_3RD = Integer.parseInt(Feature.getProperty("3rdRaidRankingPoints", "700"));
					RAID_RANKING_4TH = Integer.parseInt(Feature.getProperty("4thRaidRankingPoints", "600"));
					RAID_RANKING_5TH = Integer.parseInt(Feature.getProperty("5thRaidRankingPoints", "450"));
					RAID_RANKING_6TH = Integer.parseInt(Feature.getProperty("6thRaidRankingPoints", "350"));
					RAID_RANKING_7TH = Integer.parseInt(Feature.getProperty("7thRaidRankingPoints", "300"));
					RAID_RANKING_8TH = Integer.parseInt(Feature.getProperty("8thRaidRankingPoints", "200"));
					RAID_RANKING_9TH = Integer.parseInt(Feature.getProperty("9thRaidRankingPoints", "150"));
					RAID_RANKING_10TH = Integer.parseInt(Feature.getProperty("10thRaidRankingPoints", "100"));
					RAID_RANKING_UP_TO_50TH = Integer.parseInt(Feature.getProperty("UpTo50thRaidRankingPoints", "25"));
					RAID_RANKING_UP_TO_100TH = Integer.parseInt(Feature.getProperty("UpTo100thRaidRankingPoints", "12"));
					CLAN_LEVEL_6_COST = Integer.parseInt(Feature.getProperty("ClanLevel6Cost", "10000"));
					CLAN_LEVEL_7_COST = Integer.parseInt(Feature.getProperty("ClanLevel7Cost", "20000"));
					CLAN_LEVEL_8_COST = Integer.parseInt(Feature.getProperty("ClanLevel8Cost", "40000"));
					CLAN_LEVEL_9_COST = Integer.parseInt(Feature.getProperty("ClanLevel9Cost", "40000"));
					CLAN_LEVEL_10_COST = Integer.parseInt(Feature.getProperty("ClanLevel10Cost", "40000"));
					CLAN_LEVEL_11_COST = Integer.parseInt(Feature.getProperty("ClanLevel11Cost", "75000"));
					CLAN_LEVEL_6_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel6Requirement", "30"));
					CLAN_LEVEL_7_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel7Requirement", "80"));
					CLAN_LEVEL_8_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel8Requirement", "120"));
					CLAN_LEVEL_9_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel9Requirement", "120"));
					CLAN_LEVEL_10_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel10Requirement", "140"));
					CLAN_LEVEL_11_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel11Requirement", "170"));
					ALLOW_WYVERN_DURING_SIEGE = Boolean.parseBoolean(Feature.getProperty("AllowRideWyvernDuringSiege", "True"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+FEATURE_CONFIG_FILE+" File.");
				}
				
				// Load Character L2Properties file (if exists)
				try
				{
					L2Properties Character = new L2Properties();
					is = new FileInputStream(new File(CHARACTER_CONFIG_FILE));
					Character.load(is);
					
					MASTERACCESS_LEVEL = Integer.parseInt(Character.getProperty("MasterAccessLevel", "127"));
					MASTERACCESS_NAME_COLOR = Integer.decode(StringUtil.concat("0x", Character.getProperty("MasterNameColor", "00FF00")));
					MASTERACCESS_TITLE_COLOR = Integer.decode(StringUtil.concat("0x", Character.getProperty("MasterTitleColor", "00FF00")));
					ALT_GAME_DELEVEL = Boolean.parseBoolean(Character.getProperty("Delevel", "true"));
					DECREASE_SKILL_LEVEL = Boolean.parseBoolean(Character.getProperty("DecreaseSkillOnDelevel", "true"));
					ALT_WEIGHT_LIMIT = Double.parseDouble(Character.getProperty("AltWeightLimit", "1"));
					RUN_SPD_BOOST = Integer.parseInt(Character.getProperty("RunSpeedBoost", "0"));
					DEATH_PENALTY_CHANCE = Integer.parseInt(Character.getProperty("DeathPenaltyChance", "20"));
					RESPAWN_RESTORE_CP = Double.parseDouble(Character.getProperty("RespawnRestoreCP", "0")) / 100;
					RESPAWN_RESTORE_HP = Double.parseDouble(Character.getProperty("RespawnRestoreHP", "70")) / 100;
					RESPAWN_RESTORE_MP = Double.parseDouble(Character.getProperty("RespawnRestoreMP", "70")) / 100;
					HP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("HpRegenMultiplier", "100")) /100;
					MP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("MpRegenMultiplier", "100")) /100;
					CP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("CpRegenMultiplier", "100")) /100;
					ALT_GAME_TIREDNESS = Boolean.parseBoolean(Character.getProperty("AltGameTiredness", "false"));
					ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(Character.getProperty("EnableModifySkillDuration", "false"));
					ALT_GAME_VIEWPLAYER = Boolean.parseBoolean(Character.getProperty("ShowPlayerStats", "false"));
					ENABLE_BLOCK_EXP = Boolean.parseBoolean(Character.getProperty("EnableBlockExp", "False"));
					ENABLE_LV_UP_MSG = Boolean.parseBoolean(Character.getProperty("EnableLvUpMsg", "False"));
					
					// PvP/PK Color System
					PVP_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(Character.getProperty("EnablePvPColorSystem", "false"));
					PVP_AMOUNT1 = Integer.parseInt(Character.getProperty("PvpAmount1", "500"));
					PVP_AMOUNT2 = Integer.parseInt(Character.getProperty("PvpAmount2", "1000"));
					PVP_AMOUNT3 = Integer.parseInt(Character.getProperty("PvpAmount3", "1500"));
					PVP_AMOUNT4 = Integer.parseInt(Character.getProperty("PvpAmount4", "2500"));
					PVP_AMOUNT5 = Integer.parseInt(Character.getProperty("PvpAmount5", "5000"));
					NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + Character.getProperty("ColorForAmount1", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + Character.getProperty("ColorForAmount2", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + Character.getProperty("ColorForAmount3", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + Character.getProperty("ColorForAmount4", "00FF00"));
					NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + Character.getProperty("ColorForAmount4", "00FF00"));	                            
					PK_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(Character.getProperty("EnablePkColorSystem", "false"));
					PK_AMOUNT1 = Integer.parseInt(Character.getProperty("PkAmount1", "500"));
					PK_AMOUNT2 = Integer.parseInt(Character.getProperty("PkAmount2", "1000"));
					PK_AMOUNT3 = Integer.parseInt(Character.getProperty("PkAmount3", "1500"));
					PK_AMOUNT4 = Integer.parseInt(Character.getProperty("PkAmount4", "2500"));
					PK_AMOUNT5 = Integer.parseInt(Character.getProperty("PkAmount5", "5000"));
					TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + Character.getProperty("TitleForAmount1", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + Character.getProperty("TitleForAmount2", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + Character.getProperty("TitleForAmount3", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + Character.getProperty("TitleForAmount4", "00FF00"));
					TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + Character.getProperty("TitleForAmount5", "00FF00"));
					
					
					// Create Map only if enabled
					if (ENABLE_MODIFY_SKILL_DURATION)
					{
						String[] propertySplit = Character.getProperty("SkillDurationList", "").split(";");
						SKILL_DURATION_LIST = new TIntIntHashMap(propertySplit.length);
						for (String skill : propertySplit)
						{
							String[] skillSplit = skill.split(",");
							if (skillSplit.length != 2)
								_log.warning(StringUtil.concat("[SkillDurationList]: invalid config property -> SkillDurationList \"", skill, "\""));
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
										_log.warning(StringUtil.concat("[SkillDurationList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
									}
								}
							}
						}
					}
					ENABLE_MODIFY_SKILL_REUSE = Boolean.parseBoolean(Character.getProperty("EnableModifySkillReuse", "false"));
					// Create Map only if enabled
					if (ENABLE_MODIFY_SKILL_REUSE)
					{
						String[] propertySplit = Character.getProperty("SkillReuseList", "").split(";");
						SKILL_REUSE_LIST = new TIntIntHashMap(propertySplit.length);
						for (String skill : propertySplit)
						{
							String[] skillSplit = skill.split(",");
							if (skillSplit.length != 2)
								_log.warning(StringUtil.concat("[SkillReuseList]: invalid config property -> SkillReuseList \"", skill, "\""));
							else
							{
								try
								{
									SKILL_REUSE_LIST.put(Integer.valueOf(skillSplit[0]), Integer.valueOf(skillSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!skill.isEmpty())
										_log.warning(StringUtil.concat("[SkillReuseList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
								}
							}
						}
					}
					
					AUTO_LEARN_SKILLS = Boolean.parseBoolean(Character.getProperty("AutoLearnSkills", "false"));
					AUTO_LOOT_HERBS = Boolean.parseBoolean(Character.getProperty("AutoLootHerbs", "false"));
					BUFFS_MAX_AMOUNT = Byte.parseByte(Character.getProperty("maxbuffamount","20"));
					DANCES_MAX_AMOUNT = Byte.parseByte(Character.getProperty("maxdanceamount","12"));
					DANCE_CANCEL_BUFF = Boolean.parseBoolean(Character.getProperty("DanceCancelBuff", "false"));
					DANCE_CONSUME_ADDITIONAL_MP = Boolean.parseBoolean(Character.getProperty("DanceConsumeAdditionalMP", "true"));
					AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(Character.getProperty("AutoLearnDivineInspiration", "false"));
					ALT_GAME_CANCEL_BOW = Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
					ALT_GAME_CANCEL_CAST = Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
					EFFECT_CANCELING = Boolean.parseBoolean(Character.getProperty("CancelLesserEffect", "True"));
					ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(Character.getProperty("MagicFailures", "true"));
					PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(Character.getProperty("PlayerFakeDeathUpProtection", "0"));
					STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("StoreSkillCooltime", "true"));
					SUBCLASS_STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("SubclassStoreSkillCooltime", "false"));
					SUMMON_STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("SummonStoreSkillCooltime", "True"));
					ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(Character.getProperty("AltShieldBlocks", "false"));
					ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(Character.getProperty("AltPerfectShieldBlockRate", "10"));
					ALLOW_CLASS_MASTERS = Boolean.parseBoolean(Character.getProperty("AllowClassMasters", "False"));
					ALLOW_ENTIRE_TREE = Boolean.parseBoolean(Character.getProperty("AllowEntireTree", "False"));
					ALTERNATE_CLASS_MASTER = Boolean.parseBoolean(Character.getProperty("AlternateClassMaster", "False"));
					if (ALLOW_CLASS_MASTERS || ALTERNATE_CLASS_MASTER)
						CLASS_MASTER_SETTINGS = new ClassMasterSettings(Character.getProperty("ConfigClassMaster"));
					LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(Character.getProperty("LifeCrystalNeeded", "true"));
					SP_BOOK_NEEDED = Boolean.parseBoolean(Character.getProperty("SpBookNeeded", "false"));
					ES_SP_BOOK_NEEDED = Boolean.parseBoolean(Character.getProperty("EnchantSkillSpBookNeeded","true"));
					DIVINE_SP_BOOK_NEEDED = Boolean.parseBoolean(Character.getProperty("DivineInspirationSpBookNeeded", "true"));
					ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(Character.getProperty("AltGameSkillLearn", "false"));
					ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(Character.getProperty("AltSubClassWithoutQuests", "False"));
					ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(Character.getProperty("AltSubclassEverywhere", "False"));
					RESTORE_SERVITOR_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RestoreServitorOnReconnect", "true"));
					RESTORE_PET_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RestorePetOnReconnect", "False"));
					ENABLE_VITALITY = Boolean.parseBoolean(Character.getProperty("EnableVitality", "True"));
					RECOVER_VITALITY_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RecoverVitalityOnReconnect", "True"));
					STARTING_VITALITY_POINTS = Integer.parseInt(Character.getProperty("StartingVitalityPoints", "20000"));
					MAX_RUN_SPEED = Integer.parseInt(Character.getProperty("MaxRunSpeed", "250"));
					MAX_PCRIT_RATE = Integer.parseInt(Character.getProperty("MaxPCritRate", "500"));
					MAX_MCRIT_RATE = Integer.parseInt(Character.getProperty("MaxMCritRate", "200"));
					MAX_PATK_SPEED = Integer.parseInt(Character.getProperty("MaxPAtkSpeed", "1500"));
					MAX_MATK_SPEED = Integer.parseInt(Character.getProperty("MaxMAtkSpeed", "1999"));
					MAX_EVASION = Integer.parseInt(Character.getProperty("MaxEvasion", "250"));
					MAX_SUBCLASS = Byte.parseByte(Character.getProperty("MaxSubclass", "3"));
					MAX_SUBCLASS_LEVEL = Byte.parseByte(Character.getProperty("MaxSubclassLevel", "80"));
					MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(Character.getProperty("MaxPvtStoreSellSlotsDwarf", "4"));
					MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(Character.getProperty("MaxPvtStoreSellSlotsOther", "3"));
					MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(Character.getProperty("MaxPvtStoreBuySlotsDwarf", "5"));
					MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(Character.getProperty("MaxPvtStoreBuySlotsOther", "4"));
					INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(Character.getProperty("MaximumSlotsForNoDwarf", "80"));
					INVENTORY_MAXIMUM_DWARF = Integer.parseInt(Character.getProperty("MaximumSlotsForDwarf", "100"));
					INVENTORY_MAXIMUM_GM = Integer.parseInt(Character.getProperty("MaximumSlotsForGMPlayer", "250"));
					INVENTORY_MAXIMUM_QUEST_ITEMS = Integer.parseInt(Character.getProperty("MaximumSlotsForQuestItems", "100"));
					MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
					WAREHOUSE_SLOTS_DWARF = Integer.parseInt(Character.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
					WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(Character.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
					WAREHOUSE_SLOTS_CLAN = Integer.parseInt(Character.getProperty("MaximumWarehouseSlotsForClan", "150"));
					
					ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
					ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanShop", "true"));
					ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanTeleport", "true"));
					ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanUseGK", "false"));
					ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanTrade", "true"));
					ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
					MAX_PERSONAL_FAME_POINTS = Integer.parseInt(Character.getProperty("MaxPersonalFamePoints","65535"));
					FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(Character.getProperty("FortressZoneFameTaskFrequency","300"));
					FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(Character.getProperty("FortressZoneFameAquirePoints","31"));
					CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(Character.getProperty("CastleZoneFameTaskFrequency","300"));
					CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(Character.getProperty("CastleZoneFameAquirePoints","125"));
					FAME_FOR_DEAD_PLAYERS = Boolean.parseBoolean(Character.getProperty("FameForDeadPlayers", "true"));
					IS_CRAFTING_ENABLED = Boolean.parseBoolean(Character.getProperty("CraftingEnabled", "true"));
					CRAFT_MASTERWORK = Boolean.parseBoolean(Character.getProperty("CraftMasterwork", "True"));
					DWARF_RECIPE_LIMIT = Integer.parseInt(Character.getProperty("DwarfRecipeLimit","50"));
					COMMON_RECIPE_LIMIT = Integer.parseInt(Character.getProperty("CommonRecipeLimit","50"));
					ALT_GAME_CREATION = Boolean.parseBoolean(Character.getProperty("AltGameCreation", "false"));
					ALT_GAME_CREATION_SPEED = Double.parseDouble(Character.getProperty("AltGameCreationSpeed", "1"));
					ALT_GAME_CREATION_XP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationXpRate", "1"));
					ALT_GAME_CREATION_SP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationSpRate", "1"));
					ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationRareXpSpRate", "2"));
					ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(Character.getProperty("AltBlacksmithUseRecipes", "true"));
					ALT_CLAN_JOIN_DAYS = Integer.parseInt(Character.getProperty("DaysBeforeJoinAClan", "1"));
					ALT_CLAN_CREATE_DAYS = Integer.parseInt(Character.getProperty("DaysBeforeCreateAClan", "10"));
					ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(Character.getProperty("DaysToPassToDissolveAClan", "7"));
					ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(Character.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
					ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(Character.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
					ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(Character.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
					ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(Character.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "1"));
					ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(Character.getProperty("AltMaxNumOfClansInAlly", "3"));
					ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(Character.getProperty("AltClanMembersForWar", "15"));
					ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH= Boolean.parseBoolean(Character.getProperty("AltMembersCanWithdrawFromClanWH", "false"));
					REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(Character.getProperty("RemoveCastleCirclets", "true"));
					ALT_PARTY_RANGE = Integer.parseInt(Character.getProperty("AltPartyRange", "1600"));
					ALT_PARTY_RANGE2 = Integer.parseInt(Character.getProperty("AltPartyRange2", "1400"));
					STARTING_ADENA = Long.parseLong(Character.getProperty("StartingAdena", "0"));
					STARTING_LEVEL = Byte.parseByte(Character.getProperty("StartingLevel", "1"));
					STARTING_SP = Integer.parseInt(Character.getProperty("StartingSP", "0"));
					AUTO_LOOT = Boolean.parseBoolean(Character.getProperty("AutoLoot", "false"));
					AUTO_LOOT_RAIDS = Boolean.parseBoolean(Character.getProperty("AutoLootRaids", "false"));
					LOOT_RAIDS_PRIVILEGE_INTERVAL = Integer.parseInt(Character.getProperty("RaidLootRightsInterval", "900")) * 1000;
					LOOT_RAIDS_PRIVILEGE_CC_SIZE = Integer.parseInt(Character.getProperty("RaidLootRightsCCSize", "45"));
					UNSTUCK_INTERVAL = Integer.parseInt(Character.getProperty("UnstuckInterval", "300"));
					TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(Character.getProperty("TeleportWatchdogTimeout", "0"));
					PLAYER_SPAWN_PROTECTION = Integer.parseInt(Character.getProperty("PlayerSpawnProtection", "0"));
					String[] items = Character.getProperty("PlayerSpawnProtectionAllowedItems", "0").split(",");
					SPAWN_PROTECTION_ALLOWED_ITEMS = new ArrayList<Integer>(items.length);
					for(String item : items)
					{
						Integer itm = 0;
						try { itm = Integer.parseInt(item); }
						catch(NumberFormatException nfe)
						{
							_log.warning("Player Spawn Protection: Wrong ItemId passed: "+item);
							_log.warning(nfe.getMessage());
						}
						if(itm != 0)
							SPAWN_PROTECTION_ALLOWED_ITEMS.add(itm);
					}
					SPAWN_PROTECTION_ALLOWED_ITEMS.trimToSize();
					PLAYER_TELEPORT_PROTECTION = Integer.parseInt(Character.getProperty("PlayerTeleportProtection", "0"));
					RANDOM_RESPAWN_IN_TOWN_ENABLED = Boolean.parseBoolean(Character.getProperty("RandomRespawnInTownEnabled", "True"));
					OFFSET_ON_TELEPORT_ENABLED = Boolean.parseBoolean(Character.getProperty("OffsetOnTeleportEnabled", "True"));
					MAX_OFFSET_ON_TELEPORT = Integer.parseInt(Character.getProperty("MaxOffsetOnTeleport", "50"));
					RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(Character.getProperty("RestorePlayerInstance", "False"));
					ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(Character.getProperty("AllowSummonToInstance", "True"));
					PETITIONING_ALLOWED = Boolean.parseBoolean(Character.getProperty("PetitioningAllowed", "True"));
					MAX_PETITIONS_PER_PLAYER = Integer.parseInt(Character.getProperty("MaxPetitionsPerPlayer", "5"));
					MAX_PETITIONS_PENDING = Integer.parseInt(Character.getProperty("MaxPetitionsPending", "25"));
					ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(Character.getProperty("AltFreeTeleporting", "False"));
					DELETE_DAYS = Integer.parseInt(Character.getProperty("DeleteCharAfterDays", "7"));
					ALT_GAME_EXPONENT_XP = Float.parseFloat(Character.getProperty("AltGameExponentXp", "0."));
					ALT_GAME_EXPONENT_SP = Float.parseFloat(Character.getProperty("AltGameExponentSp", "0."));
					PARTY_XP_CUTOFF_METHOD = Character.getProperty("PartyXpCutoffMethod", "level");
					PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(Character.getProperty("PartyXpCutoffPercent", "3."));
					PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(Character.getProperty("PartyXpCutoffLevel", "20"));
					DISABLE_TUTORIAL = Boolean.parseBoolean(Character.getProperty("DisableTutorial", "False"));
					EXPERTISE_PENALTY = Boolean.parseBoolean(Character.getProperty("ExpertisePenalty", "True"));
					STORE_RECIPE_SHOPLIST = Boolean.parseBoolean(Character.getProperty("StoreRecipeShopList", "False"));
					STORE_UI_SETTINGS = Boolean.parseBoolean(Character.getProperty("StoreCharUiSettings", "False"));
					FORBIDDEN_NAMES = Character.getProperty("ForbiddenNames", "").split(",");
					PLAYER_MOVEMENT_BLOCK_TIME = Integer.parseInt(Character.getProperty("NpcTalkBlockingTime", "0")) * 1000;
					}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+CHARACTER_CONFIG_FILE+" file.");
				}
				
				// Load Premium L2Properties file (if exists)
				try
				{
					L2Properties premium = new L2Properties();
					is = new FileInputStream(new File(PREMIUM_CONFIG_FILE));
					premium.load(is);
					
					USE_PREMIUMSERVICE = Boolean.parseBoolean(premium.getProperty("EnablePremium", "false"));
					//premium
					PREMIUM_RATE_XP = Float.parseFloat(premium.getProperty("PremiumRateXp", "2."));
					PREMIUM_RATE_SP = Float.parseFloat(premium.getProperty("PremiumRateSp", "2."));
					PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(premium.getProperty("PremiumRateDropItems", "2."));
					PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(premium.getProperty("PremiumRateDropSpoil", "2."));
					
					String[] propertySplit = premium.getProperty("PremiumRateDropItemsById", "").split(";");
					PREMIUM_RATE_DROP_ITEMS_ID = new TIntFloatHashMap(propertySplit.length);
					if (!propertySplit[0].isEmpty())
					{
						for (String item : propertySplit)
						{
							String[] itemSplit = item.split(",");
							if (itemSplit.length != 2)
								_log.warning(StringUtil.concat("Config.load(): invalid config property -> PremiumRateDropItemsById \"", item, "\""));
							else
							{
								try
								{
									PREMIUM_RATE_DROP_ITEMS_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!item.isEmpty())
										_log.warning(StringUtil.concat("Config.load(): invalid config property -> PremiumRateDropItemsById \"", item, "\""));
								}
							}
						}
					}
					if (PREMIUM_RATE_DROP_ITEMS_ID.get(57) == 0f)
					{
						PREMIUM_RATE_DROP_ITEMS_ID.put(57, PREMIUM_RATE_DROP_ITEMS); //for Adena rate if not defined
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+PREMIUM_CONFIG_FILE+" file.");
				}
				
				// Load L2J Server Version L2Properties file (if exists)
				try
				{
					L2Properties serverVersion = new L2Properties();
					is = new FileInputStream(new File(SERVER_VERSION_FILE));
					serverVersion.load(is);
					
					SERVER_VERSION = serverVersion.getProperty("version", "Unsupported Custom Version.");
					SERVER_BUILD_DATE = serverVersion.getProperty("builddate", "Undefined Date.");
				}
				catch (Exception e)
				{
					//Ignore L2Properties file if it doesnt exist
					SERVER_VERSION = "Unsupported Custom Version.";
					SERVER_BUILD_DATE = "Undefined Date.";
				}
				
				// Load L2J Datapack Version L2Properties file (if exists)
				try
				{
					L2Properties serverVersion = new L2Properties();
					is = new FileInputStream(new File(DATAPACK_VERSION_FILE));
					serverVersion.load(is);
					
					DATAPACK_VERSION = serverVersion.getProperty("version", "Unsupported Custom Version.");
				}
				catch (Exception e)
				{
					//Ignore L2Properties file if it doesnt exist
					DATAPACK_VERSION = "Unsupported Custom Version.";
				}
				
				// Load Telnet L2Properties file (if exists)
				try
				{
					L2Properties telnetSettings = new L2Properties();
					is = new FileInputStream(new File(TELNET_FILE));
					telnetSettings.load(is);
					
					IS_TELNET_ENABLED = Boolean.parseBoolean(telnetSettings.getProperty("EnableTelnet", "false"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+TELNET_FILE+" File.");
				}
				
				
				// Load AntiDualBox L2Properties file (if exists)
				try
				{
					L2Properties AntiDualBox = new L2Properties();
					is = new FileInputStream(new File(AntiDualBox_FILE));
					AntiDualBox.load(is);
					
					L2JMOD_ANTIFEED_ENABLE = Boolean.parseBoolean(AntiDualBox.getProperty("AntiFeedEnable", "false"));
					L2JMOD_ANTIFEED_DUALBOX = Boolean.parseBoolean(AntiDualBox.getProperty("AntiFeedDualbox", "true"));
					L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX = Boolean.parseBoolean(AntiDualBox.getProperty("AntiFeedDisconnectedAsDualbox", "true"));
					L2JMOD_ANTIFEED_INTERVAL = 1000*Integer.parseInt(AntiDualBox.getProperty("AntiFeedInterval", "120"));
					ALLOW_MAX_PLAYERS_FROM_ONE_PC = Boolean.parseBoolean(AntiDualBox.getProperty("AllowMultiboxesPerPC", "true"));
					MAX_PLAYERS_FROM_ONE_PC = Integer.parseInt(AntiDualBox.getProperty("MultiboxesPerPC", "2"));
					L2JMOD_DUALBOX_CHECK_MAX_PLAYERS_PER_IP = Integer.parseInt(AntiDualBox.getProperty("DualboxCheckMaxPlayersPerIP", "0"));
					L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP = Integer.parseInt(AntiDualBox.getProperty("DualboxCheckMaxOlympiadParticipantsPerIP", "0"));
					String[] propertySplit = AntiDualBox.getProperty("DualboxCheckWhitelist", "127.0.0.1,0").split(";");
					L2JMOD_DUALBOX_CHECK_WHITELIST = new TIntIntHashMap(propertySplit.length);
					for (String entry : propertySplit)
					{
						String[] entrySplit = entry.split(",");
						if (entrySplit.length != 2)
							_log.warning(StringUtil.concat("DualboxCheck[Config.load()]: invalid config property -> DualboxCheckWhitelist \"", entry, "\""));
						else
						{
							try
							{
								int num = Integer.parseInt(entrySplit[1]);
								num = num == 0 ? -1 : num;
								L2JMOD_DUALBOX_CHECK_WHITELIST.put(InetAddress.getByName(entrySplit[0]).hashCode(), num);
							}
							catch (UnknownHostException e)
							{
								_log.warning(StringUtil.concat("DualboxCheck[Config.load()]: invalid address -> DualboxCheckWhitelist \"", entrySplit[0], "\""));
							}
							catch (NumberFormatException e)
							{
								_log.warning(StringUtil.concat("DualboxCheck[Config.load()]: invalid number -> DualboxCheckWhitelist \"", entrySplit[1], "\""));
							}
						}
					}
				}
				
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+AntiDualBox_FILE+" File.");
				}
				
				
				// Load TeamVsTeamFragReward L2Properties file (if exists)
				try
				{
					L2Properties TeamVsTeamFragReward = new L2Properties();
					is = new FileInputStream(new File(TeamVsTeamFragReward_FILE));
					TeamVsTeamFragReward.load(is);
					
                    TVT_FRAGS_COUNTER = Boolean.parseBoolean(TeamVsTeamFragReward.getProperty("TvTfragCountSystem", "false"));
                    TVT_FRAGS_MIN_FOR_REWARD = Integer.parseInt(TeamVsTeamFragReward.getProperty("TvTminFrags", "1"));
                    TVT_EXTRA_REWARD_FOR_FRAGS = Boolean.parseBoolean(TeamVsTeamFragReward.getProperty("TvTfragsExtraReward", "false"));
                    TVT_EXTRA_REWARDS = new FastList<int[]>();
                    TVT_FRAGS_MIN_FOR_EXTRA_REWARD = Integer.parseInt(TeamVsTeamFragReward.getProperty("TvTminFragsExtra", "5"));
                    TVT_FRAGS_FOR_REWARD_STEP =  Integer.parseInt(TeamVsTeamFragReward.getProperty("TvTfragsExtraStep", "5"));
				}
				
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+TeamVsTeamFragReward_FILE+" File.");
				}
				
				// Load LeaderBoards L2Properties file (if exists)
				try
				{
					L2Properties LeaderBoards = new L2Properties();
					is = new FileInputStream(new File(LeaderBoards_FILE));
					LeaderBoards.load(is);
					
					RANK_ARENA_ENABLED = Boolean.parseBoolean(LeaderBoards.getProperty("RankArenaEnabled", "False"));
					RANK_ARENA_INTERVAL = Integer.parseInt(LeaderBoards.getProperty("RankArenaInterval", "120"));
					RANK_ARENA_REWARD_ID = Integer.parseInt(LeaderBoards.getProperty("RankArenaRewardId", "57"));
					RANK_ARENA_REWARD_COUNT = Integer.parseInt(LeaderBoards.getProperty("RankArenaRewardCount", "100"));
					RANK_FISHERMAN_ENABLED = Boolean.parseBoolean(LeaderBoards.getProperty("RankFishermanEnabled", "False"));
					RANK_FISHERMAN_INTERVAL = Integer.parseInt(LeaderBoards.getProperty("RankFishermanInterval", "120"));
					RANK_FISHERMAN_REWARD_ID = Integer.parseInt(LeaderBoards.getProperty("RankFishermanRewardId", "57"));
					RANK_FISHERMAN_REWARD_COUNT = Integer.parseInt(LeaderBoards.getProperty("RankFishermanRewardCount", "100"));	
					RANK_CRAFT_ENABLED = Boolean.parseBoolean(LeaderBoards.getProperty("RankCraftEnabled", "False"));
					RANK_CRAFT_INTERVAL = Integer.parseInt(LeaderBoards.getProperty("RankCraftInterval", "120"));
					RANK_CRAFT_REWARD_ID = Integer.parseInt(LeaderBoards.getProperty("RankCraftRewardId", "57"));
					RANK_CRAFT_REWARD_COUNT = Integer.parseInt(LeaderBoards.getProperty("RankCraftRewardCount", "100"));	
					RANK_TVT_ENABLED = Boolean.parseBoolean(LeaderBoards.getProperty("RankTvTEnabled", "False"));
					RANK_TVT_INTERVAL = Integer.parseInt(LeaderBoards.getProperty("RankTvTInterval", "120"));
					RANK_TVT_REWARD_ID = Integer.parseInt(LeaderBoards.getProperty("RankTvTRewardId", "57"));
					RANK_TVT_REWARD_COUNT = Integer.parseInt(LeaderBoards.getProperty("RankTvTRewardCount", "100"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+LeaderBoards_FILE+" File.");
				}
				
				// Load Geodata L2Properties file (if exists)
				try
				{
					L2Properties geodata = new L2Properties();
					is = new FileInputStream(new File(Geodata_FILE));
					geodata.load(is);
					
					GEODATA = Integer.parseInt(geodata.getProperty("GeoData", "0"));
					GEODATA_CELLFINDING = Boolean.parseBoolean(geodata.getProperty("CellPathFinding", "False"));
					PATHFIND_BUFFERS = geodata.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
					LOW_WEIGHT = Float.parseFloat(geodata.getProperty("LowWeight", "0.5"));
					MEDIUM_WEIGHT = Float.parseFloat(geodata.getProperty("MediumWeight", "2"));
					HIGH_WEIGHT = Float.parseFloat(geodata.getProperty("HighWeight", "3"));
					ADVANCED_DIAGONAL_STRATEGY = Boolean.parseBoolean(geodata.getProperty("AdvancedDiagonalStrategy", "True"));
					DIAGONAL_WEIGHT = Float.parseFloat(geodata.getProperty("DiagonalWeight", "0.707"));
					MAX_POSTFILTER_PASSES = Integer.parseInt(geodata.getProperty("MaxPostfilterPasses", "3"));
					DEBUG_PATH = Boolean.parseBoolean(geodata.getProperty("DebugPath", "False"));
					FORCE_GEODATA = Boolean.parseBoolean(geodata.getProperty("ForceGeodata", "True"));
					COORD_SYNCHRONIZE = Integer.parseInt(geodata.getProperty("CoordSynchronize", "-1"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+Geodata_FILE+" File.");
				}
				
				// Load RimKamaloka L2Properties file (if exists)
				try
				{
					L2Properties rimkamaloka = new L2Properties();
					is = new FileInputStream(new File(RIMKAMALOKA_CONFIG_FILE));
					rimkamaloka.load(is);
					RESET_HOUR = Integer.parseInt(rimkamaloka.getProperty("RESET_HOUR", "6"));
					RESET_MIN = Integer.parseInt(rimkamaloka.getProperty("RESET_MIN", "30"));
					LOCK_TIME = Integer.parseInt(rimkamaloka.getProperty("LOCK_TIME", "10"));
					DURATION = Integer.parseInt(rimkamaloka.getProperty("DURATION", "20"));
					EMPTY_DESTROY_TIME = Integer.parseInt(rimkamaloka.getProperty("EMPTY_DESTROY_TIME", "5"));
					EXIT_TIME = Integer.parseInt(rimkamaloka.getProperty("EXIT_TIME", "10"));
					MAX_LEVEL_DIFFERENCE = Integer.parseInt(rimkamaloka.getProperty("MAX_LEVEL_DIFFERENCE", "5"));
					RESPAWN_DELAY = Integer.parseInt(rimkamaloka.getProperty("RESPAWN_DELAY", "30"));
					DESPAWN_DELAY = Integer.parseInt(rimkamaloka.getProperty("DESPAWN_DELAY", "10000"));

				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+RIMKAMALOKA_CONFIG_FILE+" File.");
				}
				
				// Load serversecurity L2Properties file (if exists)
				try
				{
					L2Properties serversecurity = new L2Properties();
					is = new FileInputStream(new File(ServerSecurity_FILE));
					serversecurity.load(is);
					BYPASS_VALIDATION = Boolean.parseBoolean(serversecurity.getProperty("BypassValidation", "True"));
					GAMEGUARD_ENFORCE = Boolean.parseBoolean(serversecurity.getProperty("GameGuardEnforce", "False"));
					GAMEGUARD_PROHIBITACTION = Boolean.parseBoolean(serversecurity.getProperty("GameGuardProhibitAction", "False"));
					LOG_CHAT = Boolean.parseBoolean(serversecurity.getProperty("LogChat", "false"));
					LOG_ITEMS = Boolean.parseBoolean(serversecurity.getProperty("LogItems", "false"));
					LOG_ITEMS_SMALL_LOG = Boolean.parseBoolean(serversecurity.getProperty("LogItemsSmallLog", "false"));
					LOG_ITEM_ENCHANTS = Boolean.parseBoolean(serversecurity.getProperty("LogItemEnchants", "false"));
					LOG_SKILL_ENCHANTS = Boolean.parseBoolean(serversecurity.getProperty("LogSkillEnchants", "false"));
					GMAUDIT = Boolean.parseBoolean(serversecurity.getProperty("GMAudit", "False"));
					LOG_GAME_DAMAGE = Boolean.parseBoolean(serversecurity.getProperty("LogGameDamage", "False"));
					LOG_GAME_DAMAGE_THRESHOLD = Integer.parseInt(serversecurity.getProperty("LogGameDamageThreshold", "5000"));
					SKILL_CHECK_ENABLE = Boolean.parseBoolean(serversecurity.getProperty("SkillCheckEnable", "False"));
					SKILL_CHECK_REMOVE = Boolean.parseBoolean(serversecurity.getProperty("SkillCheckRemove", "False"));
					SKILL_CHECK_GM = Boolean.parseBoolean(serversecurity.getProperty("SkillCheckGM", "True"));
					MIN_KILLS_FOR_CAPTCHA = Integer.parseInt(serversecurity.getProperty("MinKillsForCaptcha", "20"));
					CAPTCHA_SYSTEM = Integer.parseInt(serversecurity.getProperty("CaptchaSystem", "0"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+ServerSecurity_FILE+" File.");
				}
				
				// Load optimization L2Properties file (if exists)
				try
				{
					L2Properties optimization = new L2Properties();
					is = new FileInputStream(new File(Optimization_FILE));
					optimization.load(is);
					
					ALLOW_DISCARDITEM = Boolean.parseBoolean(optimization.getProperty("AllowDiscardItem", "True"));
					AUTODESTROY_ITEM_AFTER = Integer.parseInt(optimization.getProperty("AutoDestroyDroppedItemAfter", "600"));
					HERB_AUTO_DESTROY_TIME = Integer.parseInt(optimization.getProperty("AutoDestroyHerbTime","60"))*1000;
					String[] split = optimization.getProperty("ListOfProtectedItems", "0").split(",");
					LIST_PROTECTED_ITEMS = new TIntArrayList(split.length);
					for (String id : split)
					{
						LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
					}
					DATABASE_CLEAN_UP = Boolean.parseBoolean(optimization.getProperty("DatabaseCleanUp", "true"));
					CONNECTION_CLOSE_TIME = Long.parseLong(optimization.getProperty("ConnectionCloseTime", "60000"));
					CHAR_STORE_INTERVAL = Integer.parseInt(optimization.getProperty("CharacterDataStoreInterval", "15"));
					LAZY_ITEMS_UPDATE = Boolean.parseBoolean(optimization.getProperty("LazyItemsUpdate", "false"));
					UPDATE_ITEMS_ON_CHAR_STORE = Boolean.parseBoolean(optimization.getProperty("UpdateItemsOnCharStore", "false"));
					DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(optimization.getProperty("DestroyPlayerDroppedItem", "false"));
					DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(optimization.getProperty("DestroyEquipableItem", "false"));
					SAVE_DROPPED_ITEM = Boolean.parseBoolean(optimization.getProperty("SaveDroppedItem", "false"));
					EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(optimization.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
					SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(optimization.getProperty("SaveDroppedItemInterval", "60"))*60000;
					CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(optimization.getProperty("ClearDroppedItemTable", "false"));
					AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(optimization.getProperty("AutoDeleteInvalidQuestData", "False"));
					PRECISE_DROP_CALCULATION = Boolean.parseBoolean(optimization.getProperty("PreciseDropCalculation", "True"));
					MULTIPLE_ITEM_DROP = Boolean.parseBoolean(optimization.getProperty("MultipleItemDrop", "True"));
					FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(optimization.getProperty("ForceInventoryUpdate", "False"));
					LAZY_CACHE = Boolean.parseBoolean(optimization.getProperty("LazyCache", "True"));
					CACHE_CHAR_NAMES = Boolean.parseBoolean(optimization.getProperty("CacheCharNames", "True"));
					MIN_NPC_ANIMATION = Integer.parseInt(optimization.getProperty("MinNPCAnimation", "10"));
					MAX_NPC_ANIMATION = Integer.parseInt(optimization.getProperty("MaxNPCAnimation", "20"));
					MIN_MONSTER_ANIMATION = Integer.parseInt(optimization.getProperty("MinMonsterAnimation", "5"));
					MAX_MONSTER_ANIMATION = Integer.parseInt(optimization.getProperty("MaxMonsterAnimation", "20"));
					MOVE_BASED_KNOWNLIST = Boolean.parseBoolean(optimization.getProperty("MoveBasedKnownlist", "False"));
					KNOWNLIST_UPDATE_INTERVAL = Long.parseLong(optimization.getProperty("KnownListUpdateInterval", "1250"));
					GRIDS_ALWAYS_ON = Boolean.parseBoolean(optimization.getProperty("GridsAlwaysOn", "False"));
					GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(optimization.getProperty("GridNeighborTurnOnTime", "1"));
					GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(optimization.getProperty("GridNeighborTurnOffTime", "90"));
					ENABLE_LOADING_INFO_FOR_SCRIPTS = Boolean.parseBoolean(optimization.getProperty("ShowLoadingScriptsLog", "True"));
					ALLOW_KEYBOARD_MOVEMENT = Boolean.parseBoolean(optimization.getProperty("AllowKeyboardMovement", "false"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+Optimization_FILE+" File.");
				}
				
				// Load AIO L2Properties file (if exists)
				try
				{
					L2Properties aioSettings = new L2Properties();
					is = new FileInputStream(new File(AIO_CONFIG_FILE));
					aioSettings.load(is);
					
					AIOITEM_ENABLEME = Boolean.parseBoolean(aioSettings.getProperty("EnableAIOItem", "false"));
					AIOITEM_ENABLESHOP = Boolean.parseBoolean(aioSettings.getProperty("EnableGMShop", "false"));
					AIOITEM_ENABLEGK = Boolean.parseBoolean(aioSettings.getProperty("EnableGk", "false"));
					AIOITEM_ENABLEWH = Boolean.parseBoolean(aioSettings.getProperty("EnableWh", "false"));
					AIOITEM_ENABLEBUFF = Boolean.parseBoolean(aioSettings.getProperty("EnableBuffer", "false"));
					AIOITEM_ENABLESCHEMEBUFF = Boolean.parseBoolean(aioSettings.getProperty("EnableSchemeBuffer", "false"));
					AIOITEM_ENABLESERVICES = Boolean.parseBoolean(aioSettings.getProperty("EnableServices", "false"));
					AIOITEM_ENABLESUBCLASS = Boolean.parseBoolean(aioSettings.getProperty("EnableSubclassManager", "false"));
					AIOITEM_ENABLETOPLIST = Boolean.parseBoolean(aioSettings.getProperty("EnableTopListManager", "false"));
					AIOITEM_GK_COIN = Integer.parseInt(aioSettings.getProperty("GkCoin", "57"));
					AIOITEM_GK_PRICE = Integer.parseInt(aioSettings.getProperty("GkPrice", "100"));
					AIOITEM_BUFF_COIN = Integer.parseInt(aioSettings.getProperty("BufferCoin", "57"));
					AIOITEM_BUFF_PRICE = Integer.parseInt(aioSettings.getProperty("BufferPrice", "100"));
					AIOITEM_SCHEME_COIN = Integer.parseInt(aioSettings.getProperty("SchemeCoin", "57"));
					AIOITEM_SCHEME_PRICE = Integer.parseInt(aioSettings.getProperty("SchemePrice", "100"));
					AIOITEM_SCHEME_PROFILE_PRICE = Integer.parseInt(aioSettings.getProperty("SchemeProfileCreationPrice", "1000"));
					AIOITEM_SCHEME_MAX_PROFILES = Integer.parseInt(aioSettings.getProperty("SchemeMaxProfiles", "4"));
					AIOITEM_SCHEME_MAX_PROFILE_BUFFS = Integer.parseInt(aioSettings.getProperty("SchemeMaxProfileBuffs", "24"));
				}
				catch(Exception e)
				{
					_log.warning("CustomConfig.load(): Couldn't load AIO Item settings. Reason:");
					e.printStackTrace();
				}
				
				// Load Geodata L2Properties file (if exists)
				try
				{
					L2Properties geodata = new L2Properties();
					is = new FileInputStream(new File(Geodata_FILE));
					geodata.load(is);
					
					GEODATA = Integer.parseInt(geodata.getProperty("GeoData", "0"));
					GEODATA_CELLFINDING = Boolean.parseBoolean(geodata.getProperty("CellPathFinding", "False"));
					PATHFIND_BUFFERS = geodata.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
					LOW_WEIGHT = Float.parseFloat(geodata.getProperty("LowWeight", "0.5"));
					MEDIUM_WEIGHT = Float.parseFloat(geodata.getProperty("MediumWeight", "2"));
					HIGH_WEIGHT = Float.parseFloat(geodata.getProperty("HighWeight", "3"));
					ADVANCED_DIAGONAL_STRATEGY = Boolean.parseBoolean(geodata.getProperty("AdvancedDiagonalStrategy", "True"));
					DIAGONAL_WEIGHT = Float.parseFloat(geodata.getProperty("DiagonalWeight", "0.707"));
					MAX_POSTFILTER_PASSES = Integer.parseInt(geodata.getProperty("MaxPostfilterPasses", "3"));
					DEBUG_PATH = Boolean.parseBoolean(geodata.getProperty("DebugPath", "False"));
					FORCE_GEODATA = Boolean.parseBoolean(geodata.getProperty("ForceGeodata", "True"));
					COORD_SYNCHRONIZE = Integer.parseInt(geodata.getProperty("CoordSynchronize", "-1"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+Geodata_FILE+" File.");
				}
				
				// Load Enchant L2Properties file (if exists)
				try
				{
					L2Properties Enchant = new L2Properties();
					is = new FileInputStream(new File(Enchant_FILE));
					Enchant.load(is);
					
					ENCHANT_CHANCE_WEAPON = Integer.parseInt(Enchant.getProperty("EnchantChanceWeapon", "66"));
					ENCHANT_CHANCE_ARMOR = Integer.parseInt(Enchant.getProperty("EnchantChanceArmor", "66"));
					ENCHANT_CHANCE_JEWELRY = Integer.parseInt(Enchant.getProperty("EnchantChanceJewelry", "66"));
					ENCHANT_CHANCE_ELEMENT_STONE = Integer.parseInt(Enchant.getProperty("EnchantChanceElementStone", "50"));
					ENCHANT_CHANCE_ELEMENT_CRYSTAL = Integer.parseInt(Enchant.getProperty("EnchantChanceElementCrystal", "30"));
					ENCHANT_CHANCE_ELEMENT_JEWEL = Integer.parseInt(Enchant.getProperty("EnchantChanceElementJewel", "20"));
					ENCHANT_CHANCE_ELEMENT_ENERGY = Integer.parseInt(Enchant.getProperty("EnchantChanceElementEnergy", "10"));
					BLESSED_ENCHANT_CHANCE_WEAPON = Integer.parseInt(Enchant.getProperty("BlessedEnchantChanceWeapon", "66"));
					BLESSED_ENCHANT_CHANCE_ARMOR = Integer.parseInt(Enchant.getProperty("BlessedEnchantChanceArmor", "66"));
					BLESSED_ENCHANT_CHANCE_JEWELRY = Integer.parseInt(Enchant.getProperty("BlessedEnchantChanceJewelry", "66"));
					CRYSTAL_ENCHANT_CHANCE_WEAPON_WARRIOR = Integer.parseInt(Enchant.getProperty("CrystalEnchantChanceWeaponWarrior", "70"));
					CRYSTAL_ENCHANT_CHANCE_WEAPON_MAGE = Integer.parseInt(Enchant.getProperty("CrystalEnchantChanceWeaponMage", "40"));
					CRYSTAL_ENCHANT_CHANCE_ARMOR = Integer.parseInt(Enchant.getProperty("CrystalEnchantChanceArmor", "66"));
					CRYSTAL_ENCHANT_CHANCE_JEWELRY = Integer.parseInt(Enchant.getProperty("CrystalEnchantChanceJewelry", "66"));
					ENCHANT_MAX_WEAPON = Integer.parseInt(Enchant.getProperty("EnchantMaxWeapon", "0"));
					ENCHANT_MAX_ARMOR = Integer.parseInt(Enchant.getProperty("EnchantMaxArmor", "0"));
					ENCHANT_MAX_JEWELRY = Integer.parseInt(Enchant.getProperty("EnchantMaxJewelry", "0"));
					ENCHANT_SAFE_MAX = Integer.parseInt(Enchant.getProperty("EnchantSafeMax", "3"));
					ENCHANT_SAFE_MAX_FULL = Integer.parseInt(Enchant.getProperty("EnchantSafeMaxFull", "4"));
					ENCHANT_STEP_ENABLED = Boolean.parseBoolean(Enchant.getProperty("EnchantStepEnabled", "False"));
					ENCHANT_STEP_MODE = Enchant.getProperty("EnchantStepMode", "static");
					ENCHANT_STEP_STATIC = Integer.parseInt(Enchant.getProperty("EnchantStepStatic", "3"));					ENCHANT_STEP_DYNAMIC = Double.parseDouble(Enchant.getProperty("EnchantStepDynamic", "0.95"));

					String[] notenchantable = Enchant.getProperty("EnchantBlackList","7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296").split(",");
					ENCHANT_BLACKLIST = new int[notenchantable.length];					
					for (int i = 0; i < notenchantable.length; i++)
						ENCHANT_BLACKLIST[i] = Integer.parseInt(notenchantable[i]);					
					Arrays.sort(ENCHANT_BLACKLIST);
					ENABLE_SKILL_ENCHANT = Boolean.parseBoolean(Enchant.getProperty("EnableSkillEnchant", "True"));
					ENABLE_SKILL_MAX_ENCHANT_LIMIT = Boolean.parseBoolean(Enchant.getProperty("EnableSkillMaxEnchantLimit", "False"));
					SKILL_MAX_ENCHANT_LIMIT_LEVEL = Integer.parseInt(Enchant.getProperty("SkillMaxEnchantLimitLevel", "30"));

				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+Enchant_FILE+" File.");
				}
				
				// MMO
				try
				{
					//_log.info("Loading " + MMO_CONFIG_FILE.replaceAll("./config/", ""));
					L2Properties mmoSettings = new L2Properties();
					is = new FileInputStream(new File(MMO_CONFIG_FILE));
					mmoSettings.load(is);
					MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(mmoSettings.getProperty("SleepTime", "20"));
					MMO_MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "12"));
					MMO_MAX_READ_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxReadPerPass", "12"));
					MMO_HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("HelperBufferCount", "20"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
				}
				
				// Load IdFactory L2Properties file (if exists)
				try
				{
					L2Properties idSettings = new L2Properties();
					is = new FileInputStream(new File(ID_CONFIG_FILE));
					idSettings.load(is);
					
					MAP_TYPE = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
					SET_TYPE = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
					IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
					BAD_ID_CHECKING = Boolean.parseBoolean(idSettings.getProperty("BadIdChecking", "True"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+ID_CONFIG_FILE+" file.");
				}
				
				// Load General L2Properties file (if exists)
				try
				{
					L2Properties General = new L2Properties();
					is = new FileInputStream(new File(GENERAL_CONFIG_FILE));
					General.load(is);
					
					DISPLAY_SERVER_VERSION = Boolean.parseBoolean(General.getProperty("DisplayServerRevision","True"));
					SERVER_LIST_BRACKET = Boolean.parseBoolean(General.getProperty("ServerListBrackets", "false"));
					SERVER_LIST_TYPE = getServerTypeId(General.getProperty("ServerListType", "Normal").split(","));
					SERVER_LIST_AGE = Integer.parseInt(General.getProperty("ServerListAge", "0"));
					DEBUG = Boolean.parseBoolean(General.getProperty("Debug", "false"));
					PACKET_HANDLER_DEBUG = Boolean.parseBoolean(General.getProperty("PacketHandlerDebug", "false"));
					DEVELOPER = Boolean.parseBoolean(General.getProperty("Developer", "false"));
					ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(General.getProperty("AcceptGeoeditorConn", "false"));
					TEST_SERVER = Boolean.parseBoolean(General.getProperty("TestServer", "false"));
					ALT_DEV_NO_HANDLERS = Boolean.parseBoolean(General.getProperty("AltDevNoHandlers", "False"));
					ALT_DEV_NO_QUESTS = Boolean.parseBoolean(General.getProperty("AltDevNoQuests", "False"));
					ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(General.getProperty("AltDevNoSpawns", "False"));
					THREAD_P_EFFECTS = Integer.parseInt(General.getProperty("ThreadPoolSizeEffects", "10"));
					THREAD_P_GENERAL = Integer.parseInt(General.getProperty("ThreadPoolSizeGeneral", "13"));
					IO_PACKET_THREAD_CORE_SIZE = Integer.parseInt(General.getProperty("UrgentPacketThreadCoreSize", "2"));
					GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(General.getProperty("GeneralPacketThreadCoreSize", "4"));
					GENERAL_THREAD_CORE_SIZE = Integer.parseInt(General.getProperty("GeneralThreadCoreSize", "4"));
					AI_MAX_THREAD = Integer.parseInt(General.getProperty("AiMaxThread", "6"));
					DEADLOCK_DETECTOR = Boolean.parseBoolean(General.getProperty("DeadLockDetector", "False"));
					DEADLOCK_CHECK_INTERVAL = Integer.parseInt(General.getProperty("DeadLockCheckInterval", "20"));
					RESTART_ON_DEADLOCK = Boolean.parseBoolean(General.getProperty("RestartOnDeadlock", "False"));
					
					WORLD_X_MIN = Integer.parseInt(General.getProperty("WorldXMin", "10"));
					WORLD_X_MAX = Integer.parseInt(General.getProperty("WorldXMax", "26"));
					WORLD_Y_MIN = Integer.parseInt(General.getProperty("WorldYMin", "10"));
					WORLD_Y_MAX = Integer.parseInt(General.getProperty("WorldYMax", "26"));
					
					String str = General.getProperty("EnableFallingDamage", "auto");
					ENABLE_FALLING_DAMAGE = "auto".equalsIgnoreCase(str) ? GEODATA > 0 : Boolean.parseBoolean(str);
					
					ALT_ITEM_AUCTION_ENABLED = Boolean.valueOf(General.getProperty("AltItemAuctionEnabled", "False"));
					ALT_ITEM_AUCTION_EXPIRED_AFTER = Integer.valueOf(General.getProperty("AltItemAuctionExpiredAfter", "14"));
					ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = 1000 * (long)Integer.valueOf(General.getProperty("AltItemAuctionTimeExtendsOnBid", "0"));
					FS_TIME_ATTACK = Integer.parseInt(General.getProperty("TimeOfAttack", "50"));
					FS_TIME_COOLDOWN = Integer.parseInt(General.getProperty("TimeOfCoolDown", "5"));
					FS_TIME_ENTRY = Integer.parseInt(General.getProperty("TimeOfEntry", "3"));
					FS_TIME_WARMUP = Integer.parseInt(General.getProperty("TimeOfWarmUp", "2"));
					FS_PARTY_MEMBER_COUNT = Integer.parseInt(General.getProperty("NumberOfNecessaryPartyMembers", "4"));
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
					DEFAULT_PUNISH = Integer.parseInt(General.getProperty("DefaultPunish", "2"));
					DEFAULT_PUNISH_PARAM = Integer.parseInt(General.getProperty("DefaultPunishParam", "0"));
					ONLY_GM_ITEMS_FREE = Boolean.parseBoolean(General.getProperty("OnlyGMItemsFree", "True"));
					JAIL_IS_PVP = Boolean.parseBoolean(General.getProperty("JailIsPvp", "False"));
					JAIL_DISABLE_CHAT = Boolean.parseBoolean(General.getProperty("JailDisableChat", "True"));
					JAIL_DISABLE_TRANSACTION = Boolean.parseBoolean(General.getProperty("JailDisableTransaction", "False"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+GENERAL_CONFIG_FILE+" File.");
				}
				
				// Load FloodProtector L2Properties file
				try
				{
					L2Properties security = new L2Properties();
					is = new FileInputStream(new File(FLOOD_PROTECTOR_FILE));
					security.load(is);
					
					loadFloodProtectorConfigs(security);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+FLOOD_PROTECTOR_FILE);
				}
				
				// Load NPC L2Properties file (if exists)
				try
				{
					L2Properties NPC = new L2Properties();
					is = new FileInputStream(new File(NPC_CONFIG_FILE));
					NPC.load(is);
					
					ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(NPC.getProperty("AltMobAgroInPeaceZone", "True"));
					ALT_ATTACKABLE_NPCS = Boolean.parseBoolean(NPC.getProperty("AltAttackableNpcs", "True"));
					ALT_GAME_VIEWNPC = Boolean.parseBoolean(NPC.getProperty("AltGameViewNpc", "False"));
					MAX_DRIFT_RANGE = Integer.parseInt(NPC.getProperty("MaxDriftRange", "300"));
					DEEPBLUE_DROP_RULES = Boolean.parseBoolean(NPC.getProperty("UseDeepBlueDropRules", "True"));
					DEEPBLUE_DROP_RULES_RAID = Boolean.parseBoolean(NPC.getProperty("UseDeepBlueDropRulesRaid", "True"));
					SHOW_NPC_LVL = Boolean.parseBoolean(NPC.getProperty("ShowNpcLevel", "False"));
					SHOW_CREST_WITHOUT_QUEST = Boolean.parseBoolean(NPC.getProperty("ShowCrestWithoutQuest", "False"));
					ENABLE_RANDOM_ENCHANT_EFFECT = Boolean.parseBoolean(NPC.getProperty("EnableRandomEnchantEffect", "False"));
					MIN_NPC_LVL_DMG_PENALTY = Integer.parseInt(NPC.getProperty("MinNPCLevelForDmgPenalty", "78"));
					NPC_DMG_PENALTY = parseConfigLine(NPC.getProperty("DmgPenaltyForLvLDifferences", "0.7, 0.6, 0.6, 0.55"));
					NPC_CRIT_DMG_PENALTY = parseConfigLine(NPC.getProperty("CritDmgPenaltyForLvLDifferences", "0.75, 0.65, 0.6, 0.58"));
					NPC_SKILL_DMG_PENALTY = parseConfigLine(NPC.getProperty("SkillDmgPenaltyForLvLDifferences", "0.8, 0.7, 0.65, 0.62"));
					MIN_NPC_LVL_MAGIC_PENALTY = Integer.parseInt(NPC.getProperty("MinNPCLevelForMagicPenalty", "78"));
					NPC_SKILL_CHANCE_PENALTY = parseConfigLine(NPC.getProperty("SkillChancePenaltyForLvLDifferences", "2.5, 3.0, 3.25, 3.5"));
					ENABLE_DROP_VITALITY_HERBS = Boolean.parseBoolean(NPC.getProperty("EnableVitalityHerbs", "True"));
					GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(NPC.getProperty("GuardAttackAggroMob", "False"));
					ALLOW_WYVERN_UPGRADER = Boolean.parseBoolean(NPC.getProperty("AllowWyvernUpgrader", "False"));
					String[] split = NPC.getProperty("ListPetRentNpc", "30827").split(",");
					LIST_PET_RENT_NPC = new TIntArrayList(split.length);
					for (String id : split)
					{
						LIST_PET_RENT_NPC.add(Integer.parseInt(id));
					}
					RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidHpRegenMultiplier", "100")) /100;
					RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMpRegenMultiplier", "100")) /100;
					RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidPDefenceMultiplier", "100")) /100;
					RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMDefenceMultiplier", "100")) /100;
					RAID_PATTACK_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidPAttackMultiplier", "100")) /100;
					RAID_MATTACK_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMAttackMultiplier", "100")) /100;
					RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(NPC.getProperty("RaidMinRespawnMultiplier", "1.0"));
					RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(NPC.getProperty("RaidMaxRespawnMultiplier", "1.0"));
					RAID_MINION_RESPAWN_TIMER = Integer.parseInt(NPC.getProperty("RaidMinionRespawnTime", "300000"));
					String[] propertySplit = NPC.getProperty("CustomMinionsRespawnTime", "").split(";"); 
					MINIONS_RESPAWN_TIME = new TIntIntHashMap(propertySplit.length); 
					for (String prop : propertySplit) 
					{ 
						String[] propSplit = prop.split(","); 
						if (propSplit.length != 2) 
							_log.warning(StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", prop, "\"")); 
						else 
						{ 
							try 
							{ 
								MINIONS_RESPAWN_TIME.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1])); 
							} 
							catch (NumberFormatException nfe) 
								{ 
								if (!prop.isEmpty()) 
									_log.warning(StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", propSplit[0], "\"", propSplit[1])); 
								} 
						} 
					} 
					RAID_DISABLE_CURSE = Boolean.parseBoolean(NPC.getProperty("DisableRaidCurse", "False"));
					RAID_CHAOS_TIME = Integer.parseInt(NPC.getProperty("RaidChaosTime", "10"));
					GRAND_CHAOS_TIME = Integer.parseInt(NPC.getProperty("GrandChaosTime", "10"));
					MINION_CHAOS_TIME = Integer.parseInt(NPC.getProperty("MinionChaosTime", "10"));
					INVENTORY_MAXIMUM_PET = Integer.parseInt(NPC.getProperty("MaximumSlotsForPet", "12"));
					PET_HP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("PetHpRegenMultiplier", "100")) /100;
					PET_MP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("PetMpRegenMultiplier", "100")) /100;
					
					split = NPC.getProperty("NonTalkingNpcs", "18684,18685,18686,18687,18688,18689,18690,19691,18692,31557,31606,31671,31672,31673,31674,32026,32030,32031,32032,32306,32619,32620,32621").split(",");
					NON_TALKING_NPCS = new TIntArrayList(split.length);
					for (String npcId : split)
					{
						try
						{
							NON_TALKING_NPCS.add(Integer.parseInt(npcId));
						}
						catch (NumberFormatException nfe)
						{
							if (!npcId.isEmpty())
							{
								_log.warning("Could not parse " + npcId + " id for NonTalkingNpcs. Please check that all values are digits and coma separated.");
							}
						}
					}
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+NPC_CONFIG_FILE+" File.");
				}
				
				// Load Rates L2Properties file (if exists)
				try
				{
					L2Properties ratesSettings = new L2Properties();
					is = new FileInputStream(new File(RATES_CONFIG_FILE));
					ratesSettings.load(is);
					
					RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
					RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
					RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
					RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
					RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
					RATE_EXTR_FISH = Float.parseFloat(ratesSettings.getProperty("RateExtractFish", "1."));
					RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
					RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(ratesSettings.getProperty("RateRaidDropItems", "1."));
					RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
					RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
					RATE_QUEST_DROP = Float.parseFloat(ratesSettings.getProperty("RateQuestDrop", "1."));
					RATE_QUEST_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestReward", "1."));
					RATE_QUEST_REWARD_XP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardXP", "1."));
					RATE_QUEST_REWARD_SP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardSP", "1."));
					RATE_QUEST_REWARD_ADENA = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardAdena", "1."));
					RATE_QUEST_REWARD_USE_MULTIPLIERS = Boolean.parseBoolean(ratesSettings.getProperty("UseQuestRewardMultipliers", "False"));
					RATE_QUEST_REWARD_POTION = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardPotion", "1."));
					RATE_QUEST_REWARD_SCROLL = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardScroll", "1."));
					RATE_QUEST_REWARD_RECIPE = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardRecipe", "1."));
					RATE_QUEST_REWARD_MATERIAL = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardMaterial", "1."));
					
					RATE_VITALITY_LEVEL_1 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel1", "1.5"));
					RATE_VITALITY_LEVEL_2 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel2", "2."));
					RATE_VITALITY_LEVEL_3 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel3", "2.5"));
					RATE_VITALITY_LEVEL_4 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel4", "3."));
					RATE_RECOVERY_VITALITY_PEACE_ZONE = Float.parseFloat(ratesSettings.getProperty("RateRecoveryPeaceZone", "1."));
					RATE_VITALITY_LOST = Float.parseFloat(ratesSettings.getProperty("RateVitalityLost", "1."));
					RATE_VITALITY_GAIN = Float.parseFloat(ratesSettings.getProperty("RateVitalityGain", "1."));
					RATE_RECOVERY_ON_RECONNECT = Float.parseFloat(ratesSettings.getProperty("RateRecoveryOnReconnect", "4."));
					RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
					RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));
					RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "1."));
					RATE_DROP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpHerbs", "1."));
					RATE_DROP_MP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateMpHerbs", "1."));
					RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "1."));
					RATE_DROP_VITALITY_HERBS = Float.parseFloat(ratesSettings.getProperty("RateVitalityHerbs", "1."));
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
					
					// Initializing table
					PLAYER_XP_PERCENT_LOST = new double[Byte.MAX_VALUE+1];
					
					// Default value
					for (int i = 0; i <= Byte.MAX_VALUE; i++)
						PLAYER_XP_PERCENT_LOST[i] = 1.;
					
					// Now loading into table parsed values
					try
					{
						String[] values = ratesSettings.getProperty("PlayerXPPercentLost", "0,39-7.0;40,75-4.0;76,76-2.5;77,77-2.0;78,78-1.5").split(";");
						
						for (String s : values)
						{
							int min;
							int max;
							double val;
							
							String[] vals = s.split("-");
							String[] mM = vals[0].split(",");
							
							min = Integer.parseInt(mM[0]);
							max = Integer.parseInt(mM[1]);
							val = Double.parseDouble(vals[1]);
							
							for (int i = min; i <= max; i++)
								PLAYER_XP_PERCENT_LOST[i] = val;
						}
					}
					catch (Exception e)
					{
						_log.warning("Error while loading Player XP percent lost");
						e.printStackTrace();
					}
					
					String[] propertySplit = ratesSettings.getProperty("RateDropItemsById", "").split(";");
					RATE_DROP_ITEMS_ID = new TIntFloatHashMap(propertySplit.length);
					if (!propertySplit[0].isEmpty())
					{
						for (String item : propertySplit)
						{
							String[] itemSplit = item.split(",");
							if (itemSplit.length != 2)
								_log.warning(StringUtil.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
							else
							{
								try
								{
									RATE_DROP_ITEMS_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!item.isEmpty())
										_log.warning(StringUtil.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
								}
							}
						}
					}
					if (RATE_DROP_ITEMS_ID.get(57) == 0f)
					{
						RATE_DROP_ITEMS_ID.put(57, RATE_DROP_ITEMS); //for Adena rate if not defined
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+RATES_CONFIG_FILE+" File.");
				}
				
				// Load Augmentation L2Properties file (if exists)
				try
				{
					L2Properties Augmentation = new L2Properties();
					is = new FileInputStream(new File(Augmentation_FILE));
					Augmentation.load(is);
					
					AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationNGSkillChance", "15"));
					AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationNGGlowChance", "0"));
					AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationMidSkillChance", "30"));
					AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationMidGlowChance", "40"));
					AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationHighSkillChance", "45"));
					AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationHighGlowChance", "70"));
					AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationTopSkillChance", "60"));
					AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationTopGlowChance", "100"));
					AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationBaseStatChance", "1"));
					AUGMENTATION_ACC_SKILL_CHANCE = Integer.parseInt(Augmentation.getProperty("AugmentationAccSkillChance", "0"));
					
					String[] array = Augmentation.getProperty("AugmentationBlackList", "6656,6657,6658,6659,6660,6661,6662,8191,10170,10314").split(",");
					AUGMENTATION_BLACKLIST = new int[array.length];
					
					for (int i = 0; i < array.length; i++)
						AUGMENTATION_BLACKLIST[i] = Integer.parseInt(array[i]);
					
					Arrays.sort(AUGMENTATION_BLACKLIST);
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+Augmentation_FILE+" File.");
					}	
				
				// Load GameMaster L2Properties file (if exists)
				try
				{
					L2Properties GameMaster = new L2Properties();
					is = new FileInputStream(new File(GameMaster_FILE));
					GameMaster.load(is);
					
					EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(GameMaster.getProperty("EverybodyHasAdminRights", "false"));
					SERVER_GMONLY = Boolean.parseBoolean(GameMaster.getProperty("ServerGMOnly", "false"));
					GM_HERO_AURA = Boolean.parseBoolean(GameMaster.getProperty("GMHeroAura", "False"));
					GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(GameMaster.getProperty("GMStartupInvulnerable", "False"));
					GM_STARTUP_INVISIBLE = Boolean.parseBoolean(GameMaster.getProperty("GMStartupInvisible", "False"));
					GM_STARTUP_SILENCE = Boolean.parseBoolean(GameMaster.getProperty("GMStartupSilence", "False"));
					GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(GameMaster.getProperty("GMStartupAutoList", "False"));
					GM_STARTUP_DIET_MODE = Boolean.parseBoolean(GameMaster.getProperty("GMStartupDietMode", "False"));
					GM_ADMIN_MENU_STYLE = GameMaster.getProperty("GMAdminMenuStyle", "modern");
					GM_ITEM_RESTRICTION = Boolean.parseBoolean(GameMaster.getProperty("GMItemRestriction", "True"));
					GM_SKILL_RESTRICTION = Boolean.parseBoolean(GameMaster.getProperty("GMSkillRestriction", "True"));
					GM_TRADE_RESTRICTED_ITEMS = Boolean.parseBoolean(GameMaster.getProperty("GMTradeRestrictedItems", "False"));
					GM_RESTART_FIGHTING = Boolean.parseBoolean(GameMaster.getProperty("GMRestartFighting", "True"));
					GM_ANNOUNCER_NAME = Boolean.parseBoolean(GameMaster.getProperty("GMShowAnnouncerName", "False"));
					GM_CRITANNOUNCER_NAME = Boolean.parseBoolean(GameMaster.getProperty("GMShowCritAnnouncerName", "False"));
					GM_GIVE_SPECIAL_SKILLS = Boolean.parseBoolean(GameMaster.getProperty("GMGiveSpecialSkills", "False"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+GameMaster_FILE+" File.");
					}
				
				// Load OfflineMod L2Properties file (if exists)
				try
				{
					L2Properties OfflineMod = new L2Properties();
					is = new FileInputStream(new File(OfflineMod_FILE));
					OfflineMod.load(is);

					OFFLINE_TRADE_ENABLE = Boolean.parseBoolean(OfflineMod.getProperty("OfflineTradeEnable", "false"));
					OFFLINE_CRAFT_ENABLE = Boolean.parseBoolean(OfflineMod.getProperty("OfflineCraftEnable", "false"));
					OFFLINE_SUPER_MODE_ENABLE = Boolean.parseBoolean(OfflineMod.getProperty("OfflineSuperModeEnable", "false"));
					OFFLINE_SET_NAME_COLOR = Boolean.parseBoolean(OfflineMod.getProperty("OfflineSetNameColor", "false"));
					OFFLINE_NAME_COLOR = Integer.decode("0x" + OfflineMod.getProperty("OfflineNameColor", "808080"));
					OFFLINE_FAME = Boolean.parseBoolean(OfflineMod.getProperty("OfflineFame", "true"));
					RESTORE_OFFLINERS = Boolean.parseBoolean(OfflineMod.getProperty("RestoreOffliners", "false"));
					OFFLINE_MAX_DAYS = Integer.parseInt(OfflineMod.getProperty("OfflineMaxDays", "10"));
					OFFLINE_DISCONNECT_FINISHED = Boolean.parseBoolean(OfflineMod.getProperty("OfflineDisconnectFinished", "true"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+OfflineMod_FILE+" File.");
					}
				
				// Load GAMEFEATURE L2Properties file (if exists)
				try
				{
					L2Properties gamefeature = new L2Properties();
					is = new FileInputStream(new File(GAMEFEATURE_CONFIG_FILE));
					gamefeature.load(is);
					ZONE_TOWN = Integer.parseInt(gamefeature.getProperty("ZoneTown", "0"));
					DEFAULT_GLOBAL_CHAT = gamefeature.getProperty("GlobalChat", "ON");
					DEFAULT_TRADE_CHAT = gamefeature.getProperty("TradeChat", "ON");
					ALLOW_WAREHOUSE = Boolean.parseBoolean(gamefeature.getProperty("AllowWarehouse", "True"));
					WAREHOUSE_CACHE = Boolean.parseBoolean(gamefeature.getProperty("WarehouseCache", "False"));
					WAREHOUSE_CACHE_TIME = Integer.parseInt(gamefeature.getProperty("WarehouseCacheTime", "15"));
					ALLOW_REFUND = Boolean.parseBoolean(gamefeature.getProperty("AllowRefund", "True"));
					ALLOW_MAIL = Boolean.parseBoolean(gamefeature.getProperty("AllowMail", "True"));
					ALLOW_ATTACHMENTS = Boolean.parseBoolean(gamefeature.getProperty("AllowAttachments", "True"));
					ALLOW_WEAR = Boolean.parseBoolean(gamefeature.getProperty("AllowWear", "True"));
					WEAR_DELAY = Integer.parseInt(gamefeature.getProperty("WearDelay", "5"));
					WEAR_PRICE = Integer.parseInt(gamefeature.getProperty("WearPrice", "10"));

				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+GAMEFEATURE_CONFIG_FILE+" File.");
					}
				
				// Load LOTTERY L2Properties file (if exists)
				try
				{
					L2Properties lottery = new L2Properties();
					is = new FileInputStream(new File(LOTTERY_CONFIG_FILE));
					lottery.load(is);
					ALLOW_LOTTERY = Boolean.parseBoolean(lottery.getProperty("AllowLottery", "True"));
					ALT_LOTTERY_PRIZE = Long.parseLong(lottery.getProperty("AltLotteryPrize","50000"));
					ALT_LOTTERY_TICKET_PRICE = Long.parseLong(lottery.getProperty("AltLotteryTicketPrice","2000"));
					ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(lottery.getProperty("AltLottery5NumberRate","0.6"));
					ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(lottery.getProperty("AltLottery4NumberRate","0.2"));
					ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(lottery.getProperty("AltLottery3NumberRate","0.2"));
					ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Long.parseLong(lottery.getProperty("AltLottery2and1NumberPrize","200"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+LOTTERY_CONFIG_FILE+" File.");
					}
				
				// Load MANOR L2Properties file (if exists)
				try
				{
					L2Properties manor = new L2Properties();
					is = new FileInputStream(new File(MANOR_CONFIG_FILE));
					manor.load(is);
					ALLOW_MANOR = Boolean.parseBoolean(manor.getProperty("AllowManor", "True"));
					ALT_MANOR_REFRESH_TIME = Integer.parseInt(manor.getProperty("AltManorRefreshTime","20"));
					ALT_MANOR_REFRESH_MIN = Integer.parseInt(manor.getProperty("AltManorRefreshMin","00"));
					ALT_MANOR_APPROVE_TIME = Integer.parseInt(manor.getProperty("AltManorApproveTime","6"));
					ALT_MANOR_APPROVE_MIN = Integer.parseInt(manor.getProperty("AltManorApproveMin","00"));
					ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(manor.getProperty("AltManorMaintenancePeriod","360000"));
					ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(manor.getProperty("AltManorSaveAllActions","false"));
					ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(manor.getProperty("AltManorSavePeriodRate","2"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+MANOR_CONFIG_FILE+" File.");
					}
				
				// Load MISC L2Properties file (if exists)
				try
				{
					L2Properties misc = new L2Properties();
					is = new FileInputStream(new File(MISC_CONFIG_FILE));
					misc.load(is);
					ALLOW_RACE = Boolean.parseBoolean(misc.getProperty("AllowRace", "True"));
					ALLOW_WATER = Boolean.parseBoolean(misc.getProperty("AllowWater", "True"));
					ALLOW_RENTPET = Boolean.parseBoolean(misc.getProperty("AllowRentPet", "False"));
					ALLOWFISHING = Boolean.parseBoolean(misc.getProperty("AllowFishing", "True"));
					ALLOW_BOAT = Boolean.parseBoolean(misc.getProperty("AllowBoat", "True"));
					BOAT_BROADCAST_RADIUS = Integer.parseInt(misc.getProperty("BoatBroadcastRadius", "20000"));
					ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(misc.getProperty("AllowCursedWeapons", "True"));
					ALLOW_NPC_WALKERS = Boolean.parseBoolean(misc.getProperty("AllowNpcWalkers", "true"));
					ALLOW_PET_WALKERS = Boolean.parseBoolean(misc.getProperty("AllowPetWalkers", "True"));
					SERVER_NEWS = Boolean.parseBoolean(misc.getProperty("ShowServerNews", "False"));
					COMMUNITY_TYPE = Integer.parseInt(misc.getProperty("CommunityType", "1"));
					BBS_SHOW_PLAYERLIST = Boolean.parseBoolean(misc.getProperty("BBSShowPlayerList", "false"));
					COMMUNITY_COLOR_LEGEND = Boolean.parseBoolean(misc.getProperty("ShowCommunityColorLegend", "false"));
					BBS_DEFAULT = misc.getProperty("BBSDefault", "_bbshome");
					SHOW_LEVEL_COMMUNITYBOARD = Boolean.parseBoolean(misc.getProperty("ShowLevelOnCommunityBoard", "False"));
					SHOW_STATUS_COMMUNITYBOARD = Boolean.parseBoolean(misc.getProperty("ShowStatusOnCommunityBoard", "True"));
					NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(misc.getProperty("NamePageSizeOnCommunityBoard", "50"));
					NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(misc.getProperty("NamePerRowOnCommunityBoard", "5"));
					USE_SAY_FILTER = Boolean.parseBoolean(misc.getProperty("UseChatFilter", "false"));
					CHAT_FILTER_CHARS = misc.getProperty("ChatFilterChars", "^_^");
					SHOW_DAMAGE_MESSAGE_ON_CENTER_TOP_SCREEN = Boolean.parseBoolean(misc.getProperty("EnableAdvanceSystemMessage", "false"));
					FONT_SIZE_CRITICAL = Integer.parseInt(misc.getProperty("FontSizeCriticalMessage", "1"));
					SCREEN_POSITION = Integer.parseInt(misc.getProperty("ScreenPositionMessage", "2"));
					ALLOW_BIND_ACCOUNT_IP = Boolean.parseBoolean(misc.getProperty("AllowBindAccountIP", "True"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+MISC_CONFIG_FILE+" File.");
					}
				
				// Load DIMENSIONALRIFT L2Properties file (if exists)
				try
				{
					L2Properties dimensionalrift = new L2Properties();
					is = new FileInputStream(new File(DIMENSIONALRIFT_CONFIG_FILE));
					dimensionalrift.load(is);
					RIFT_MIN_PARTY_SIZE = Integer.parseInt(dimensionalrift.getProperty("RiftMinPartySize", "5"));
					RIFT_MAX_JUMPS = Integer.parseInt(dimensionalrift.getProperty("MaxRiftJumps", "4"));
					RIFT_SPAWN_DELAY = Integer.parseInt(dimensionalrift.getProperty("RiftSpawnDelay", "10000"));
					RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(dimensionalrift.getProperty("AutoJumpsDelayMin", "480"));
					RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(dimensionalrift.getProperty("AutoJumpsDelayMax", "600"));
					RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(dimensionalrift.getProperty("BossRoomTimeMultiply", "1.5"));
					RIFT_ENTER_COST_RECRUIT = Integer.parseInt(dimensionalrift.getProperty("RecruitCost", "18"));
					RIFT_ENTER_COST_SOLDIER = Integer.parseInt(dimensionalrift.getProperty("SoldierCost", "21"));
					RIFT_ENTER_COST_OFFICER = Integer.parseInt(dimensionalrift.getProperty("OfficerCost", "24"));
					RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(dimensionalrift.getProperty("CaptainCost", "27"));
					RIFT_ENTER_COST_COMMANDER = Integer.parseInt(dimensionalrift.getProperty("CommanderCost", "30"));
					RIFT_ENTER_COST_HERO = Integer.parseInt(dimensionalrift.getProperty("HeroCost", "33"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+DIMENSIONALRIFT_CONFIG_FILE+" File.");
					}
				
				// Load HANDYSBLOCKCHECKER L2Properties file (if exists)
				try
				{
					L2Properties handysblockchecker = new L2Properties();
					is = new FileInputStream(new File(HANDYSBLOCKCHECKER_CONFIG_FILE));
					handysblockchecker.load(is);
					ENABLE_BLOCK_CHECKER_EVENT = Boolean.valueOf(handysblockchecker.getProperty("EnableBlockCheckerEvent", "false"));
					MIN_BLOCK_CHECKER_TEAM_MEMBERS = Integer.valueOf(handysblockchecker.getProperty("BlockCheckerMinTeamMembers", "2"));
					if(MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1)
						MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
					else if(MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6)
						MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
					HBCE_FAIR_PLAY = Boolean.parseBoolean(handysblockchecker.getProperty("HBCEFairPlay", "false"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+HANDYSBLOCKCHECKER_CONFIG_FILE+" File.");
					}
				
				// Load CLIENTSPACKET L2Properties file (if exists)
				try
				{
					L2Properties clientspacket = new L2Properties();
					is = new FileInputStream(new File(CLIENTSPACKET_CONFIG_FILE));
					clientspacket.load(is);
					CLIENT_PACKET_QUEUE_SIZE = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueSize", "0"));
					if (CLIENT_PACKET_QUEUE_SIZE == 0)
						CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2;
					CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMaxBurstSize", "0"));
					if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
						CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1;
					CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMaxPacketsPerSecond", "80"));
					CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMeasureInterval", "5"));
					CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMaxAveragePacketsPerSecond", "40"));
					CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMaxFloodsPerMin", "2"));
					CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMaxOverflowsPerMin", "1"));
					CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMaxUnderflowsPerMin", "1"));
					CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = Integer.parseInt(clientspacket.getProperty("ClientPacketQueueMaxUnknownPerMin", "5"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+CLIENTSPACKET_CONFIG_FILE+" File.");
					}
				
				// Load CUSTOMTABLES L2Properties file (if exists)
				try
				{
					L2Properties customtables = new L2Properties();
					is = new FileInputStream(new File(CUSTOMTABLES_CONFIG_FILE));
					customtables.load(is);
					CUSTOM_SPAWNLIST_TABLE = Boolean.valueOf(customtables.getProperty("CustomSpawnlistTable", "false"));
					SAVE_GMSPAWN_ON_CUSTOM = Boolean.valueOf(customtables.getProperty("SaveGmSpawnOnCustom", "false"));
					CUSTOM_NPC_TABLE = Boolean.valueOf(customtables.getProperty("CustomNpcTable", "false"));
					CUSTOM_NPC_SKILLS_TABLE = Boolean.valueOf(customtables.getProperty("CustomNpcSkillsTable", "false"));
					CUSTOM_ARMORSETS_TABLE = Boolean.valueOf(customtables.getProperty("CustomArmorSetsTable", "false"));
					CUSTOM_TELEPORT_TABLE = Boolean.valueOf(customtables.getProperty("CustomTeleportTable", "false"));
					CUSTOM_DROPLIST_TABLE = Boolean.valueOf(customtables.getProperty("CustomDroplistTable", "false"));
					CUSTOM_MERCHANT_TABLES = Boolean.valueOf(customtables.getProperty("CustomMerchantTables", "false"));
					CUSTOM_NPCBUFFER_TABLES = Boolean.valueOf(customtables.getProperty("CustomNpcBufferTables", "false"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+CUSTOMTABLES_CONFIG_FILE+" File.");
					}
				
				// Load TvT Rounds L2Properties file (if exists)
				try
				{
					L2Properties teamvsteamtrounds = new L2Properties();
					is = new FileInputStream(new File(TEAMVSTEAMROUNDS_CONFIG_FILE));
					teamvsteamtrounds.load(is);
					TVT_ROUND_EVENT_ENABLED = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventEnabled", "false"));
					TVT_ROUND_EVENT_IN_INSTANCE = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventInInstance", "false"));
					TVT_ROUND_EVENT_INSTANCE_FILE = teamvsteamtrounds.getProperty("TvTRoundEventInstanceFile", "coliseum.xml");
					TVT_ROUND_EVENT_INTERVAL = teamvsteamtrounds.getProperty("TvTRoundEventInterval", "20:00").split(",");
					TVT_ROUND_EVENT_PARTICIPATION_TIME = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventParticipationTime", "3600"));
					TVT_ROUND_EVENT_FIRST_FIGHT_RUNNING_TIME = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventFirstFightRunningTime", "1800"));
					TVT_ROUND_EVENT_SECOND_FIGHT_RUNNING_TIME = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventSecondFightRunningTime", "1800"));
					TVT_ROUND_EVENT_THIRD_FIGHT_RUNNING_TIME = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventThirdFightRunningTime", "1800"));
					TVT_ROUND_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventParticipationNpcId", "0"));
					TVT_ROUND_EVENT_ON_DIE = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventOnDie", "true"));
					
					if (TVT_ROUND_EVENT_PARTICIPATION_NPC_ID == 0)
					{
						TVT_ROUND_EVENT_ENABLED = false;
						_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationNpcId");
					}
					else
					{
						String[] propertySplit = teamvsteamtrounds.getProperty("TvTRoundEventParticipationNpcCoordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							TVT_ROUND_EVENT_ENABLED = false;
							_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationNpcCoordinates");
						}
						else
						{
							TVT_ROUND_EVENT_REWARDS = new ArrayList<int[]>();
							TVT_ROUND_DOORS_IDS_TO_OPEN = new ArrayList<Integer>();
							TVT_ROUND_DOORS_IDS_TO_CLOSE = new ArrayList<Integer>();
							TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE = new ArrayList<Integer>();
							TVT_ROUND_EVENT_WAIT_OPEN_ANTEROOM_DOORS = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventWaitOpenAnteroomDoors", "30"));
							TVT_ROUND_EVENT_WAIT_CLOSE_ANTEROOM_DOORS = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventWaitCloseAnteroomDoors", "15"));
							TVT_ROUND_EVENT_STOP_ON_TIE = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventStopOnTie", "false"));
							TVT_ROUND_EVENT_MINIMUM_TIE = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventMinimumTie", "1"));
							if (TVT_ROUND_EVENT_MINIMUM_TIE != 1 && TVT_ROUND_EVENT_MINIMUM_TIE != 2 && TVT_ROUND_EVENT_MINIMUM_TIE != 3) TVT_ROUND_EVENT_MINIMUM_TIE = 1;
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
							TVT_ROUND_EVENT_TEAM_1_COORDINATES = new int[3];
							TVT_ROUND_EVENT_TEAM_2_COORDINATES = new int[3];
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
							if (propertySplit.length == 4)
								TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
							TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventMinPlayersInTeams", "1"));
							TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventMaxPlayersInTeams", "20"));
							TVT_ROUND_EVENT_MIN_LVL = (byte)Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventMinPlayerLevel", "1"));
							TVT_ROUND_EVENT_MAX_LVL = (byte)Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventMaxPlayerLevel", "80"));
							TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventStartRespawnLeaveTeleportDelay", "10"));
							TVT_ROUND_EVENT_EFFECTS_REMOVAL = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventEffectsRemoval", "0"));
							TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP = Integer.parseInt(teamvsteamtrounds.getProperty("TvTRoundEventMaxParticipantsPerIP", "0"));
							TVT_ROUND_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundAllowVoicedInfoCommand", "false"));
							TVT_ROUND_EVENT_TEAM_1_NAME = teamvsteamtrounds.getProperty("TvTRoundEventTeam1Name", "Team1");
							propertySplit = teamvsteamtrounds.getProperty("TvTRoundEventTeam1Coordinates", "0,0,0").split(",");
							if (propertySplit.length < 3)
							{
								TVT_ROUND_EVENT_ENABLED = false;
								_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventTeam1Coordinates");
							}
							else
							{
								TVT_ROUND_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
								TVT_ROUND_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
								TVT_ROUND_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
								TVT_ROUND_EVENT_TEAM_2_NAME = teamvsteamtrounds.getProperty("TvTRoundEventTeam2Name", "Team2");
								propertySplit = teamvsteamtrounds.getProperty("TvTRoundEventTeam2Coordinates", "0,0,0").split(",");
								if (propertySplit.length < 3)
								{
									TVT_ROUND_EVENT_ENABLED= false;
									_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventTeam2Coordinates");
								}
								else
								{
									TVT_ROUND_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
									TVT_ROUND_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
									TVT_ROUND_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
									propertySplit = teamvsteamtrounds.getProperty("TvTRoundEventParticipationFee", "0,0").split(",");
									try
									{
										TVT_ROUND_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
										TVT_ROUND_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
									}
									catch (NumberFormatException nfe)
									{
										if (propertySplit.length > 0)
											_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationFee");
									}
									propertySplit = teamvsteamtrounds.getProperty("TvTRoundEventReward", "57,100000").split(";");
									for (String reward : propertySplit)
									{
										String[] rewardSplit = reward.split(",");
										if (rewardSplit.length != 2)
											_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventReward \"", reward, "\""));
										else
										{
											try
											{
												TVT_ROUND_EVENT_REWARDS.add(new int[]{Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])});
											}
											catch (NumberFormatException nfe)
											{
												if (!reward.isEmpty())
													_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventReward \"", reward, "\""));
											}
										}
									}
									
									TVT_ROUND_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventTargetTeamMembersAllowed", "true"));
									TVT_ROUND_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventScrollsAllowed", "false"));
									TVT_ROUND_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventPotionsAllowed", "false"));
									TVT_ROUND_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventSummonByItemAllowed", "false"));
									TVT_ROUND_GIVE_POINT_TEAM_TIE = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundGivePointTeamTie", "false"));
									TVT_ROUND_REWARD_TEAM_TIE = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundRewardTeamTie", "false"));
									TVT_ROUND_EVENT_REWARD_ON_SECOND_FIGHT_END = Boolean.parseBoolean(teamvsteamtrounds.getProperty("TvTRoundEventRewardOnSecondFightEnd", "false"));
									propertySplit = teamvsteamtrounds.getProperty("TvTRoundDoorsToOpen", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_ROUND_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundDoorsToOpen \"", door, "\""));
										}
									}
									
									propertySplit = teamvsteamtrounds.getProperty("TvTRoundDoorsToClose", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_ROUND_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundDoorsToClose \"", door, "\""));
										}
									}
									
									propertySplit = teamvsteamtrounds.getProperty("TvTRoundAnteroomDoorsToOpenClose", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundAnteroomDoorsToOpenClose \"", door, "\""));
										}
									}
									
									propertySplit = teamvsteamtrounds.getProperty("TvTRoundEventFighterBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_ROUND_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventFighterBuffs \"", skill, "\""));
											else
											{
												try
												{
													TVT_ROUND_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
														_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventFighterBuffs \"", skill, "\""));
												}
											}
										}
									}
									
									propertySplit = teamvsteamtrounds.getProperty("TvTRoundEventMageBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_ROUND_EVENT_MAGE_BUFFS = new TIntIntHashMap(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventMageBuffs \"", skill, "\""));
											else
											{
												try
												{
													TVT_ROUND_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
														_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventMageBuffs \"", skill, "\""));
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
						throw new Error("Failed to Load "+TEAMVSTEAMROUNDS_CONFIG_FILE+" File.");
					}
				
				// Load pailaka L2Properties file (if exists)
				try
				{
					L2Properties pailaka = new L2Properties();
					is = new FileInputStream(new File(PAILAKA_CONFIG_FILE));
					pailaka.load(is);
					PSOIAF_MIN_LVL = Integer.parseInt(pailaka.getProperty("IceAndFireMinLevel", "36"));
					PSOIAF_MAX_LVL = Integer.parseInt(pailaka.getProperty("IceAndFireMaxLevel", "42"));
					PSOIAF_EXIT_TIME = Integer.parseInt(pailaka.getProperty("IceAndFireExitTime", "5"));
					PDL_MIN_LVL = Integer.parseInt(pailaka.getProperty("DevilsLegacyMinLevel", "61"));
					PDL_MAX_LVL = Integer.parseInt(pailaka.getProperty("DevilsLegacyMaxLevel", "67"));
					PDL_EXIT_TIME = Integer.parseInt(pailaka.getProperty("DevilsLegacyExitTime", "5"));
					PID_MIN_LVL = Integer.parseInt(pailaka.getProperty("InjuredDragonMinLevel", "73"));
					PID_MAX_LVL = Integer.parseInt(pailaka.getProperty("InjuredDragonMaxLevel", "77"));
					PID_MAX_LVL_SUMMON = Integer.parseInt(pailaka.getProperty("InjuredDragonMaxLevelSummon", "80"));
					PID_EXIT_TIME = Integer.parseInt(pailaka.getProperty("InjuredDragonExitTime", "5"));

				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+PAILAKA_CONFIG_FILE+" File.");
					}
				
				// Load hallofsuffering L2Properties file (if exists)
				try
				{
					L2Properties hallofsuffering = new L2Properties();
					is = new FileInputStream(new File(HOS_CONFIG_FILE));
					hallofsuffering.load(is);
					HOS_DEBUG = Boolean.parseBoolean(hallofsuffering.getProperty("EnableDebug", "false"));
					HOS_INS_PENALTY = Integer.parseInt(hallofsuffering.getProperty("InstancePenalty", "24"));
					HOS_MIN_LEVEL = Integer.parseInt(hallofsuffering.getProperty("MinimalLevel", "75"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+HOS_CONFIG_FILE+" File.");
					}
				
				// Load Hellbound L2Properties file (if exists)
				try
				{
					L2Properties hellbound = new L2Properties();
					is = new FileInputStream(new File(HELLBOUND_CONFIG_FILE));
					hellbound.load(is);
					ANNOUNCE_TO_ALL_GAINED_TRUST = Boolean.parseBoolean(hellbound.getProperty("AnnounceToAllGainedTrust", "false"));
					L2JMOD_HELLBOUND_STATUS = Boolean.parseBoolean(hellbound.getProperty("HellboundStatus", "false"));
					RATE_HB_TRUST_INCREASE = Float.parseFloat(hellbound.getProperty("RateHellboundTrustIncrease", "1."));
					RATE_HB_TRUST_DECREASE = Float.parseFloat(hellbound.getProperty("RateHellboundTrustDecrease", "1."));
					MOD_HELLBOUND_STATUS = Boolean.parseBoolean(hellbound.getProperty("HellboundStatus", "false"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+HELLBOUND_CONFIG_FILE+" File.");
					}
				
				// Load Champion L2Properties file (if exists)
				try
				{
					L2Properties Champions = new L2Properties();
					is = new FileInputStream(new File(Champions_FILE));
					Champions.load(is);
					L2JMOD_CHAMPION_ENABLE = Boolean.parseBoolean(Champions.getProperty("ChampionEnable", "false"));
					L2JMOD_CHAMPION_PASSIVE = Boolean.parseBoolean(Champions.getProperty("ChampionPassive", "false"));
					L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(Champions.getProperty("ChampionFrequency", "0"));
					L2JMOD_CHAMP_TITLE = Champions.getProperty("ChampionTitle", "Champion");
					L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(Champions.getProperty("ChampionMinLevel", "20"));
					L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(Champions.getProperty("ChampionMaxLevel", "60"));
					L2JMOD_CHAMPION_HP = Integer.parseInt(Champions.getProperty("ChampionHp", "7"));
					L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(Champions.getProperty("ChampionHpRegen", "1."));
					L2JMOD_CHAMPION_REWARDS = Integer.parseInt(Champions.getProperty("ChampionRewards", "8"));
					L2JMOD_CHAMPION_ADENAS_REWARDS = Float.parseFloat(Champions.getProperty("ChampionAdenasRewards", "1"));
					L2JMOD_CHAMPION_ATK = Float.parseFloat(Champions.getProperty("ChampionAtk", "1."));
					L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(Champions.getProperty("ChampionSpdAtk", "1."));
					L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = Integer.parseInt(Champions.getProperty("ChampionRewardLowerLvlItemChance", "0"));
					L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = Integer.parseInt(Champions.getProperty("ChampionRewardHigherLvlItemChance", "0"));
					L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(Champions.getProperty("ChampionRewardItemID", "6393"));
					L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(Champions.getProperty("ChampionRewardItemQty", "1"));
					L2JMOD_CHAMPION_ENABLE_VITALITY = Boolean.parseBoolean(Champions.getProperty("ChampionEnableVitality", "False"));
					L2JMOD_CHAMPION_ENABLE_AURA = Integer.parseInt(Champions.getProperty("ChampionEnableAura", "0"));
					if (L2JMOD_CHAMPION_ENABLE_AURA != 0 && L2JMOD_CHAMPION_ENABLE_AURA != 1 && L2JMOD_CHAMPION_ENABLE_AURA != 2) L2JMOD_CHAMPION_ENABLE_AURA = 0;
					L2JMOD_CHAMPION_ENABLE_IN_INSTANCES = Boolean.parseBoolean(Champions.getProperty("ChampionEnableInInstances", "False"));
				}
				
				catch (Exception e)
					{
						e.printStackTrace();
						throw new Error("Failed to Load "+Champions_FILE+" File.");
					}
					
				// Load TeamVsTeam L2Properties file (if exists)
				try
				{
					L2Properties TeamVsTeam = new L2Properties();
					is = new FileInputStream(new File(TeamVsTeam_FILE));
					TeamVsTeam.load(is);
					
					TVT_EVENT_ENABLED = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTEventEnabled", "false"));
					TVT_EVENT_IN_INSTANCE = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTEventInInstance", "false"));
					TVT_EVENT_INSTANCE_FILE = TeamVsTeam.getProperty("TvTEventInstanceFile", "coliseum.xml");
					TVT_EVENT_INTERVAL = TeamVsTeam.getProperty("TvTEventInterval", "20:00").split(",");
					TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(TeamVsTeam.getProperty("TvTEventParticipationTime", "3600"));
					TVT_EVENT_RUNNING_TIME = Integer.parseInt(TeamVsTeam.getProperty("TvTEventRunningTime", "1800"));
					TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(TeamVsTeam.getProperty("TvTEventParticipationNpcId", "0"));
					
					if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
					{
						TVT_EVENT_ENABLED = false;
						_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcId");
					}
					else
					{
						String[] propertySplit = TeamVsTeam.getProperty("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							TVT_EVENT_ENABLED = false;
							_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
						}
						else
						{
							TVT_EVENT_REWARDS = new ArrayList<int[]>();
							TVT_EVENT_REWARDS_KILL = new ArrayList<int[]>();
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
							TVT_EVENT_MIN_PLAYERS_IN_TEAMS = Integer.parseInt(TeamVsTeam.getProperty("TvTEventMinPlayersInTeams", "1"));
							TVT_EVENT_MAX_PLAYERS_IN_TEAMS = Integer.parseInt(TeamVsTeam.getProperty("TvTEventMaxPlayersInTeams", "20"));
							TVT_EVENT_MIN_LVL = (byte)Integer.parseInt(TeamVsTeam.getProperty("TvTEventMinPlayerLevel", "1"));
							TVT_EVENT_MAX_LVL = (byte)Integer.parseInt(TeamVsTeam.getProperty("TvTEventMaxPlayerLevel", "80"));
							TVT_EVENT_RESPAWN_TELEPORT_DELAY = Integer.parseInt(TeamVsTeam.getProperty("TvTEventRespawnTeleportDelay", "20"));
							TVT_EVENT_START_LEAVE_TELEPORT_DELAY = Integer.parseInt(TeamVsTeam.getProperty("TvTEventStartLeaveTeleportDelay", "20"));
							TVT_EVENT_EFFECTS_REMOVAL = Integer.parseInt(TeamVsTeam.getProperty("TvTEventEffectsRemoval", "0"));
							TVT_EVENT_MAX_PARTICIPANTS_PER_IP = Integer.parseInt(TeamVsTeam.getProperty("TvTEventMaxParticipantsPerIP", "0"));
							TVT_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTAllowVoicedInfoCommand", "false"));
							TVT_EVENT_TEAM_1_NAME = TeamVsTeam.getProperty("TvTEventTeam1Name", "Team1");
							propertySplit = TeamVsTeam.getProperty("TvTEventTeam1Coordinates", "0,0,0").split(",");
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
								TVT_EVENT_TEAM_2_NAME = TeamVsTeam.getProperty("TvTEventTeam2Name", "Team2");
								propertySplit = TeamVsTeam.getProperty("TvTEventTeam2Coordinates", "0,0,0").split(",");
								if (propertySplit.length < 3)
								{
									TVT_EVENT_ENABLED= false;
									_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam2Coordinates");
								}
								else
								{
									TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
									TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
									TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
									propertySplit = TeamVsTeam.getProperty("TvTEventParticipationFee", "0,0").split(",");
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
									propertySplit = TeamVsTeam.getProperty("TvTEventReward", "57,100000").split(";");
									for (String reward : propertySplit)
									{
										String[] rewardSplit = reward.split(",");
										if (rewardSplit.length != 2)
											_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\""));
										else
										{
											try
											{
												TVT_EVENT_REWARDS.add(new int[]{Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])});
											}
											catch (NumberFormatException nfe)
											{
												if (!reward.isEmpty())
													_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\""));
											}
										}
									}
									
									propertySplit = TeamVsTeam.getProperty("TvTEventRewardKill", "57,2").split(";");
									for (String reward : propertySplit)
									{
										String[] rewardSplit = reward.split(",");
										if (rewardSplit.length != 2)
											_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventRewardKill \"", reward, "\""));
										else
										{
											try
											{
												TVT_EVENT_REWARDS_KILL.add(new int[]{Integer.parseInt(rewardSplit[0]), Integer.parseInt(rewardSplit[1])});
											}
											catch (NumberFormatException nfe)
											{
												if (!reward.isEmpty())
													_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventRewardKill \"", reward, "\""));
											}
										}
									}
									TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTEventTargetTeamMembersAllowed", "true"));
									TVT_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTEventScrollsAllowed", "false"));
									TVT_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTEventPotionsAllowed", "false"));
									TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTEventSummonByItemAllowed", "false"));
									TVT_REWARD_TEAM_TIE = Boolean.parseBoolean(TeamVsTeam.getProperty("TvTRewardTeamTie", "false"));
									propertySplit = TeamVsTeam.getProperty("TvTDoorsToOpen", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToOpen \"", door, "\""));
										}
									}
									
									propertySplit = TeamVsTeam.getProperty("TvTDoorsToClose", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToClose \"", door, "\""));
										}
									}
									
									propertySplit = TeamVsTeam.getProperty("TvTEventFighterBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_EVENT_FIGHTER_BUFFS = new TIntIntHashMap(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
											else
											{
												try
												{
													TVT_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
														_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
												}
											}
										}
									}
									
									propertySplit = TeamVsTeam.getProperty("TvTEventMageBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_EVENT_MAGE_BUFFS = new TIntIntHashMap(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
											else
											{
												try
												{
													TVT_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
														_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
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
						throw new Error("Failed to Load "+TeamVsTeam_FILE+" File.");
					}
					
				// Load L2JMod L2Properties file (if exists)
				try
				{
					L2Properties ExtraConfig = new L2Properties();
					is = new FileInputStream(new File(ExtraConfig_CONFIG_FILE));
					ExtraConfig.load(is);
					TOPDEBUG = Boolean.valueOf(ExtraConfig.getProperty("TopNpcDebug", "false"));
					TOPID = Integer.parseInt(ExtraConfig.getProperty("TopNpcID", "1"));
					SHOW_WELCOME_PM = Boolean.valueOf(ExtraConfig.getProperty("ShowWelcomePM", "false"));
					PM_FROM = ExtraConfig.getProperty("PMFrom", "L2JHellKnight");
					PM_TEXT = ExtraConfig.getProperty("PMText", "Remember Vote For L2JHellKnight");
					START_SUBCLASS_LEVEL = Byte.parseByte(ExtraConfig.getProperty("StartSubclassLevel", "40"));
					SHOW_ONLINE_PLAYERS = Boolean.parseBoolean(ExtraConfig.getProperty("ShowOnlinePlayers","False"));
					ENABLE_BOTREPORT = Boolean.valueOf(ExtraConfig.getProperty("EnableBotReport", "false"));
					AUTO_ACTIVATE_SHOTS = Boolean.parseBoolean(ExtraConfig.getProperty("AutoActivateShotsEnabled", "False"));;
					AUTO_ACTIVATE_SHOTS_MIN = Integer.parseInt(ExtraConfig.getProperty("AutoActivateShotsMin", "200"));
					L2JMOD_ALLOW_WEDDING = Boolean.parseBoolean(ExtraConfig.getProperty("AllowWedding", "False"));
					L2JMOD_WEDDING_PRICE = Integer.parseInt(ExtraConfig.getProperty("WeddingPrice", "250000000"));
					L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(ExtraConfig.getProperty("WeddingPunishInfidelity", "True"));
					L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(ExtraConfig.getProperty("WeddingTeleport", "True"));
					L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(ExtraConfig.getProperty("WeddingTeleportPrice", "50000"));
					L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(ExtraConfig.getProperty("WeddingTeleportDuration", "60"));
					L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(ExtraConfig.getProperty("WeddingAllowSameSex", "False"));
					L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(ExtraConfig.getProperty("WeddingFormalWear", "True"));
					L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(ExtraConfig.getProperty("WeddingDivorceCosts", "20"));
					L2JMOD_ENABLE_WAREHOUSESORTING_CLAN = Boolean.valueOf(ExtraConfig.getProperty("EnableWarehouseSortingClan", "False"));
					L2JMOD_AUTO_LOOT_INDIVIDUAL = Boolean.parseBoolean(ExtraConfig.getProperty("AutoLootIndividual", "False"));
					L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.valueOf(ExtraConfig.getProperty("EnableWarehouseSortingPrivate", "False"));
					ENTER_HELLBOUND_WITHOUT_QUEST = Boolean.parseBoolean(ExtraConfig.getProperty("EnterHellBoundWithoutQuest", "false"));
					LIMIT_SUMMONS_PAILAKA = Boolean.parseBoolean(ExtraConfig.getProperty("LimitSummonsPailaka", "False"));
					BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(ExtraConfig.getProperty("BankingEnabled", "false"));
					BANKING_SYSTEM_GOLDBARS = Integer.parseInt(ExtraConfig.getProperty("BankingGoldbarCount", "1"));
					BANKING_SYSTEM_ADENA = Integer.parseInt(ExtraConfig.getProperty("BankingAdenaCount", "500000000"));
					L2JMOD_DISPLAY_SERVER_TIME = Boolean.parseBoolean(ExtraConfig.getProperty("DisplayServerTime", "false"));
					//Welcme message
					WELCOME_MESSAGE_ENABLED = Boolean.parseBoolean(ExtraConfig.getProperty("ScreenWelcomeMessageEnable", "false"));
					WELCOME_MESSAGE_TEXT = ExtraConfig.getProperty("ScreenWelcomeMessageText", "Welcome to L2JHellKnight server!");
					WELCOME_MESSAGE_TIME = Integer.parseInt(ExtraConfig.getProperty("ScreenWelcomeMessageTime", "10")) * 1000;
					//welcome message - advance
					WELCOME_ADVANCE_MESSAGE_ENABLED = Boolean.parseBoolean(ExtraConfig.getProperty("ScreenWelcomeAdvanceMessageEnable", "false"));
					WELCOME_ADVANCE_HUMAN_MESSAGE_TEXT = ExtraConfig.getProperty("ScreenWelcomeAdvanceHumanMessageText", "Jsem clovek");
					WELCOME_ADVANCE_DARK_ELF_MESSAGE_TEXT = ExtraConfig.getProperty("ScreenWelcomeAdvanceDarkElfMessageText", "Jsem temny elf");
					WELCOME_ADVANCE_DWARF_MESSAGE_TEXT = ExtraConfig.getProperty("ScreenWelcomeAdvanceDwarfMessageText", "Jsem trpaslik");
					WELCOME_ADVANCE_ELF_MESSAGE_TEXT = ExtraConfig.getProperty("ScreenWelcomeAdvanceElfMessageText", "Jsem elf");
					WELCOME_ADVANCE_KAMAEL_MESSAGE_TEXT = ExtraConfig.getProperty("ScreenWelcomeAdvanceKamaelMessageText", "Jsem kamael");
					WELCOME_ADVANCE_ORC_MESSAGE_TEXT = ExtraConfig.getProperty("ScreenWelcomeAdvanceOrcMessageText", "Jsem ork");
					WELCOME_ADVANCE_MESSAGE_TIME = Integer.parseInt(ExtraConfig.getProperty("ScreenWelcomeAdvanceMessageTime", "10")) * 1000;
					
					ANNOUNCE_PK_PVP = Boolean.parseBoolean(ExtraConfig.getProperty("AnnouncePkPvP", "False"));
					ANNOUNCE_PK_PVP_NORMAL_MESSAGE = Boolean.parseBoolean(ExtraConfig.getProperty("AnnouncePkPvPNormalMessage", "True"));
					ANNOUNCE_PK_MSG = ExtraConfig.getProperty("AnnouncePkMsg", "$killer has slaughtered $target");
					ANNOUNCE_PVP_MSG = ExtraConfig.getProperty("AnnouncePvpMsg", "$killer has defeated $target");
					L2JMOD_ENABLE_MANA_POTIONS_SUPPORT = Boolean.parseBoolean(ExtraConfig.getProperty("EnableManaPotionSupport", "false"));
					ALT_KEEP_BUFF_AFTER_DEATH = Boolean.parseBoolean(ExtraConfig.getProperty("AltKeepBuffAfterDeath", "False"));                 			
					ALT_HERO_COLOR_ENABLED = Boolean.parseBoolean(ExtraConfig.getProperty("HeroNameColorEnabled", "False"));
                   	ALT_HERO_COLOR = Integer.decode((new StringBuilder()).append("0x").append(ExtraConfig.getProperty("HeroColor", "EE0000")).toString()).intValue();
   		    		ALT_NOBLE_COLOR_ENABLED = Boolean.parseBoolean(ExtraConfig.getProperty("NobleNameColorEnabled", "False"));
   		    		ALT_NOBLE_COLOR = Integer.decode((new StringBuilder()).append("0x").append(ExtraConfig.getProperty("NobleColor", "EE0000")).toString()).intValue();
   		    		CLAN_LEADER_COLOR_ENABLED = Boolean.parseBoolean(ExtraConfig.getProperty("ClanLeaderNameColorEnabled", "False"));
   		    		CLAN_LEADER_COLOR = Integer.decode((new StringBuilder()).append("0x").append(ExtraConfig.getProperty("ClanLeaderColor", "00FFFF")).toString()).intValue();
   		    		CLAN_LEADER_COLOR_CLAN_LEVEL = Integer.parseInt(ExtraConfig.getProperty("ClanLeaderColorAtClanLevel", "1"));
   		    		
					L2JMOD_CHAT_ADMIN = Boolean.parseBoolean(ExtraConfig.getProperty("ChatAdmin", "false"));
					
					L2JMOD_MULTILANG_ENABLE = Boolean.parseBoolean(ExtraConfig.getProperty("MultiLangEnable", "false"));
					String[] allowed = ExtraConfig.getProperty("MultiLangAllowed", "en").split(";");
					L2JMOD_MULTILANG_ALLOWED = new ArrayList<String>(allowed.length);
					for (String lang : allowed)
						L2JMOD_MULTILANG_ALLOWED.add(lang);
					L2JMOD_MULTILANG_DEFAULT = ExtraConfig.getProperty("MultiLangDefault", "en");
					if (!L2JMOD_MULTILANG_ALLOWED.contains(L2JMOD_MULTILANG_DEFAULT))
						_log.warning("MultiLang[Config.load()]: default language: " + L2JMOD_MULTILANG_DEFAULT + " is not in allowed list !");
					L2JMOD_MULTILANG_VOICED_ALLOW = Boolean.parseBoolean(ExtraConfig.getProperty("MultiLangVoiceCommand", "True"));
					L2JMOD_MULTILANG_SM_ENABLE = Boolean.parseBoolean(ExtraConfig.getProperty("MultiLangSystemMessageEnable", "false"));
					allowed = ExtraConfig.getProperty("MultiLangSystemMessageAllowed", "").split(";");
					L2JMOD_MULTILANG_SM_ALLOWED = new ArrayList<String>(allowed.length);
					for (String lang : allowed)
					{
						if (!lang.isEmpty())
							L2JMOD_MULTILANG_SM_ALLOWED.add(lang);
					}
					
					L2WALKER_PROTECTION = Boolean.parseBoolean(ExtraConfig.getProperty("L2WalkerProtection", "False"));
					L2JMOD_DEBUG_VOICE_COMMAND = Boolean.parseBoolean(ExtraConfig.getProperty("DebugVoiceCommand", "False"));

				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+ExtraConfig_CONFIG_FILE+" File.");
				}
				
				// Load PvP L2Properties file (if exists)
				try
				{
					L2Properties pvpSettings = new L2Properties();
					is = new FileInputStream(new File(PVP_CONFIG_FILE));
					pvpSettings.load(is);
					
					
					KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
					KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
					KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
					KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
					KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
					KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));
					KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
					KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
					KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390");
					
					String[] array = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
					KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[array.length];
					
					for (int i = 0; i < array.length; i++)
						KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i]);
					
					array = KARMA_NONDROPPABLE_ITEMS.split(",");
					KARMA_LIST_NONDROPPABLE_ITEMS = new int[array.length];
					
					for (int i = 0; i < array.length; i++)
						KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i]);
					
					// sorting so binarySearch can be used later
					Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
					Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
					
					PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "120000"));
					PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "60000"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+PVP_CONFIG_FILE+" File.");
				}
				
				// Load NPC INFO L2Properties file (if exists)
				try
				{
					L2Properties npcinfo = new L2Properties();
					is = new FileInputStream(new File(NPCINFO_FILE));
					npcinfo.load(is);
					
					SERVERINFO_NPC_ID = Integer.parseInt(npcinfo.getProperty("ServerInfoNpcID", "50026"));
					SERVERINFO_NPC_DISABLE_PAGE = npcinfo.getProperty("ServerInfoNpcDisablePage", "0").split("\\;");
				}
				
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+NPCINFO_FILE+" File.");
				}
				
				// Load HideAndSeek L2Properties file (if exists)
				try
				{
					L2Properties HideAndSeek = new L2Properties();
					is = new FileInputStream(new File(HideAndSeek_FILE));
					HideAndSeek.load(is);
					
                    ALT_HAS_ENABLE = Boolean.parseBoolean(HideAndSeek.getProperty("HaSEventEnabled", "false"));
                    ALT_HAS_TIME = HideAndSeek.getProperty("HaSEventInterval", "9:00").split(",");
                    ALT_HAS_TIME_REG = Integer.parseInt(HideAndSeek.getProperty("HaSEventParticipationTime", "5"));
                    ALT_HAS_TIME_EVENT = Integer.parseInt(HideAndSeek.getProperty("HaSEventRunningTime", "10"));
                    ALT_HAS_NPC = Integer.parseInt(HideAndSeek.getProperty("HaSEventParticipationNpcId", "90000"));
                    ALT_HAS_PKJOIN = Boolean.parseBoolean(HideAndSeek.getProperty("HaSEventCanPkJoin", "false"));
                    ALT_HAS_SECUENTIAL = Boolean.parseBoolean(HideAndSeek.getProperty("HaSEventSecuential", "false"));
                    ALT_HAS_MINLEVEL = Integer.parseInt(HideAndSeek.getProperty("HaSEventMinimumLevel", "1"));
                    ALT_HAS_MAXLEVEL = Integer.parseInt(HideAndSeek.getProperty("HaSEventMaximumLevel", "85"));
                    ALT_HAS_MINPLAYERS = Integer.parseInt(HideAndSeek.getProperty("HaSEventMinimumPlayers", "2"));
                    ALT_HAS_MAXPLAYERS = Integer.parseInt(HideAndSeek.getProperty("HaSEventMaximumPlayers", "20"));
        			{
        				String[] temp = HideAndSeek.getProperty("HaSEventParticipationNpcCoordinates", "0,0,0").split(",");
        				ALT_HAS_LOCNPC[0] = Integer.valueOf(temp[0]);
        				ALT_HAS_LOCNPC[1] = Integer.valueOf(temp[1]);
        				ALT_HAS_LOCNPC[2] = Integer.valueOf(temp[2]);
        			}
				}
				
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+HideAndSeek_FILE+" File.");
				}
				
				// Load Olympiad L2Properties file (if exists)
				try
				{
					L2Properties olympiad = new L2Properties();
					is = new FileInputStream(new File(OLYMPIAD_CONFIG_FILE));
					olympiad.load(is);
					
					ALT_OLY_START_TIME = Integer.parseInt(olympiad.getProperty("AltOlyStartTime", "18"));
					ALT_OLY_MIN = Integer.parseInt(olympiad.getProperty("AltOlyMin","00"));
					ALT_OLY_CPERIOD = Long.parseLong(olympiad.getProperty("AltOlyCPeriod","21600000"));
					ALT_OLY_BATTLE = Long.parseLong(olympiad.getProperty("AltOlyBattle","360000"));
					ALT_OLY_WPERIOD = Long.parseLong(olympiad.getProperty("AltOlyWPeriod","604800000"));
					ALT_OLY_VPERIOD = Long.parseLong(olympiad.getProperty("AltOlyVPeriod","86400000"));
					String[] propertySplit = olympiad.getProperty("AltOlyEndDate","1").split(",");
					ALT_OLY_END_DATE = new int[propertySplit.length];
					for (int i = 0; i < propertySplit.length; i++)
					{
					   ALT_OLY_END_DATE[i] = Integer.parseInt(propertySplit[i]);
					}
					propertySplit = olympiad.getProperty("AltOlyEndHour","12:00:00").split(":");
					for (int i = 0; i < 3; i++)
					{
					   ALT_OLY_END_HOUR[i] = Integer.parseInt(propertySplit[i]);
					}
					ALT_OLY_START_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyStartPoints","18"));
					ALT_OLY_WEEKLY_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyWeeklyPoints","3"));
					ALT_OLY_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyClassedParticipants","5"));
					ALT_OLY_NONCLASSED = Integer.parseInt(olympiad.getProperty("AltOlyNonClassedParticipants","9"));
					ALT_OLY_TEAMS = Integer.parseInt(olympiad.getProperty("AltOlyTeamsParticipants","9"));
					ALT_OLY_REG_DISPLAY = Integer.parseInt(olympiad.getProperty("AltOlyRegistrationDisplayNumber","100"));
					ALT_OLY_CLASSED_REWARD = parseItemsList(olympiad.getProperty("AltOlyClassedReward","13722,50"));
					ALT_OLY_NONCLASSED_REWARD = parseItemsList(olympiad.getProperty("AltOlyNonClassedReward","13722,40"));
					ALT_OLY_TEAM_REWARD = parseItemsList(olympiad.getProperty("AltOlyTeamReward","13722,85"));
					ALT_OLY_COMP_RITEM = Integer.parseInt(olympiad.getProperty("AltOlyCompRewItem","13722"));
					ALT_OLY_MIN_MATCHES = Integer.parseInt(olympiad.getProperty("AltOlyMinMatchesForPoints","9"));
					ALT_OLY_GP_PER_POINT = Integer.parseInt(olympiad.getProperty("AltOlyGPPerPoint","1000"));
					ALT_OLY_HERO_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyHeroPoints","180"));
					ALT_OLY_RANK1_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank1Points","120"));
					ALT_OLY_RANK2_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank2Points","80"));
					ALT_OLY_RANK3_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank3Points","55"));
					ALT_OLY_RANK4_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank4Points","35"));
					ALT_OLY_RANK5_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank5Points","20"));
					ALT_OLY_MAX_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyMaxPoints","10"));
					ALT_OLY_LOG_FIGHTS = Boolean.parseBoolean(olympiad.getProperty("AltOlyLogFights","false"));
					ALT_OLY_SHOW_MONTHLY_WINNERS = Boolean.parseBoolean(olympiad.getProperty("AltOlyShowMonthlyWinners","true"));
					ALT_OLY_ANNOUNCE_GAMES = Boolean.parseBoolean(olympiad.getProperty("AltOlyAnnounceGames","true"));
					String[] split = olympiad.getProperty("AltOlyRestrictedItems","6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390,17049,17050,17051,17052,17053,17054,17055,17056,17057,17058,17059,17060,17061,20759,20775,20776,20777,20778,14774").split(",");
					LIST_OLY_RESTRICTED_ITEMS = new TIntArrayList(split.length);
					for (String id : split)
					{
						LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
					}
					ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(olympiad.getProperty("AltOlyEnchantLimit","-1"));
					ALT_OLY_WAIT_TIME = Integer.parseInt(olympiad.getProperty("AltOlyWaitTime","120"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+OLYMPIAD_CONFIG_FILE+" File.");
				}
				
				// PC Bang
 				try
 				{
					L2Properties pccaffeSettings = new L2Properties();
					is = new FileInputStream(new File(PCBANG_CONFIG_FILE));
					pccaffeSettings.load(is);

					PC_BANG_ENABLED = Boolean.parseBoolean(pccaffeSettings.getProperty("Enabled", "false"));
					MAX_PC_BANG_POINTS = Integer.parseInt(pccaffeSettings.getProperty("MaxPcBangPoints", "200000"));
					if(MAX_PC_BANG_POINTS<0)
						MAX_PC_BANG_POINTS=0;
					ENABLE_DOUBLE_PC_BANG_POINTS = Boolean.parseBoolean(pccaffeSettings.getProperty("DoublingAcquisitionPoints", "false"));
					DOUBLE_PC_BANG_POINTS_CHANCE = Integer.parseInt(pccaffeSettings.getProperty("DoublingAcquisitionPointsChance", "1"));
					if(DOUBLE_PC_BANG_POINTS_CHANCE<0 || DOUBLE_PC_BANG_POINTS_CHANCE>100)
						DOUBLE_PC_BANG_POINTS_CHANCE=1;
					PC_BANG_POINT_RATE = Double.parseDouble(pccaffeSettings.getProperty("AcquisitionPointsRate", "1.0"));
					if(PC_BANG_POINT_RATE<0)
						PC_BANG_POINT_RATE=1;
					RANDOM_PC_BANG_POINT = Boolean.parseBoolean(pccaffeSettings.getProperty("AcquisitionPointsRandom", "false"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + PCBANG_CONFIG_FILE + " File.");
				}
				
				try
				{
					L2Properties Settings = new L2Properties();
					is = new FileInputStream(HEXID_FILE);
					Settings.load(is);
					SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
					HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
				}
				catch (Exception e)
				{
					_log.warning("Could not load HexID file ("+HEXID_FILE+"). Hopefully login will give us one.");
				}
				
				// Gracia Seeds
				try
				{
					L2Properties graciaseedsSettings = new L2Properties();
					is = new FileInputStream(new File(GRACIASEEDS_CONFIG_FILE));
					graciaseedsSettings.load(is);

					// Seed of Destruction
					TEST_SEED_OF_DESTRUCTION = Boolean.parseBoolean(graciaseedsSettings.getProperty("EnableDebug","False"));
					SOD_TIAT_KILL_COUNT = Integer.parseInt(graciaseedsSettings.getProperty("TiatKillCountForNextState", "10"));
					SOD_STAGE_2_LENGTH = Long.parseLong(graciaseedsSettings.getProperty("Stage2Length", "720")) * 60000;
					MINIMUM_SOD_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MinimumSodPlayers", "36"));
					MAXIMUM_SOD_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MaximumSodPlayers", "46"));
					MIN_LEVEL_PLAYER_FOR_SOD = Integer.parseInt(graciaseedsSettings.getProperty("MinimumLevelForSOD", "75"));
					PREVENT_TO_MUCH_SPAWN_MOBS = Integer.parseInt(graciaseedsSettings.getProperty("PreventToMuchSpawnMobs", "40"));
					EXIT_TIME_SOD = Integer.parseInt(graciaseedsSettings.getProperty("ExitTime", "5"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + GRACIASEEDS_CONFIG_FILE + " File.");
				}
				
				try
				{
					FILTER_LIST = new ArrayList<String>();
					LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(CHAT_FILTER_FILE))));
					String line = null;
					while ((line = lnr.readLine()) != null)
					{
						if (line.trim().isEmpty() || line.startsWith("#"))
							continue;
						
						FILTER_LIST.add(line.trim());
					}
					_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + CHAT_FILTER_FILE + " File.");
				}
				try
				{
					L2Properties chSiege = new L2Properties();
					is = new FileInputStream(new File(CH_SIEGE_FILE));
					chSiege.load(is);
					
					CHS_MAX_ATTACKERS = Integer.parseInt(chSiege.getProperty("MaxAttackers", "500"));
					CHS_CLAN_MINLEVEL = Integer.parseInt(chSiege.getProperty("MinClanLevel", "4"));
					CHS_MAX_FLAGS_PER_CLAN = Integer.parseInt(chSiege.getProperty("MaxFlagsPerClan", "1"));
					CHS_ENABLE_FAME = Boolean.parseBoolean(chSiege.getProperty("EnableFame", "false"));
					CHS_FAME_AMOUNT = Integer.parseInt(chSiege.getProperty("FameAmount", "0"));
					CHS_FAME_FREQUENCY = Integer.parseInt(chSiege.getProperty("FameFrequency", "0"));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			finally
			{
				try
				{
					is.close();
				}
				catch (Exception e)
				{}
			}
		}
		else if(Server.serverMode == Server.MODE_LOGINSERVER)
		{
			_log.info("loading login config");
			InputStream is = null;
			try
			{
				try
				{
					L2Properties serverSettings = new L2Properties();
					is = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE));
					serverSettings.load(is);
					
					GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHostname","*");
					GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort","9013"));
					
					LOGIN_BIND_ADDRESS = serverSettings.getProperty("LoginserverHostname", "*");
					PORT_LOGIN = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
					
					DEBUG = Boolean.parseBoolean(serverSettings.getProperty("Debug", "false"));
					
					ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer","True"));
					
					LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
					LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
					
					LOG_LOGIN_CONTROLLER = Boolean.parseBoolean(serverSettings.getProperty("LogLoginController", "true"));

					LOGIN_SERVER_SCHEDULE_RESTART = Boolean.parseBoolean(serverSettings.getProperty("LoginRestartSchedule", "False"));
					LOGIN_SERVER_SCHEDULE_RESTART_TIME = Long.parseLong(serverSettings.getProperty("LoginRestartTime", "24"));
					
					LOGIN_SERVER_SCHEDULE_RESTART = Boolean.parseBoolean(serverSettings.getProperty("LoginRestartSchedule", "False"));
					
					LOGIN_SERVER_SCHEDULE_RESTART_TIME = Long.parseLong(serverSettings.getProperty("LoginRestartTime", "24"));
					
					DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
					DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
					DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
					DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
					DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
					DATABASE_MAX_IDLE_TIME = Integer.parseInt(serverSettings.getProperty("MaximumDbIdleTime", "0"));
					
					SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
					
					AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts","True"));
					
					FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection","True"));
					FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit","15"));
					NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime","700"));
					FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime","350"));
					MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP","50"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + LOGIN_CONFIGURATION_FILE + " File.");
				}
				// MMO
				try
				{
					_log.info("Loading " + MMO_CONFIG_FILE.replaceAll("./config/", ""));
					L2Properties mmoSettings = new L2Properties();
					is = new FileInputStream(new File(MMO_CONFIG_FILE));
					mmoSettings.load(is);
					MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(mmoSettings.getProperty("SleepTime", "20"));
					MMO_MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "12"));
					MMO_MAX_READ_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxReadPerPass", "12"));
					MMO_HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("HelperBufferCount", "20"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
				}
				
				// Load Telnet L2Properties file (if exists)
				try
				{
					L2Properties telnetSettings = new L2Properties();
					is = new FileInputStream(new File(TELNET_FILE));
					telnetSettings.load(is);
					
					IS_TELNET_ENABLED = Boolean.parseBoolean(telnetSettings.getProperty("EnableTelnet", "false"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load "+TELNET_FILE+" File.");
				}
			}
			finally
			{
				try
				{
					is.close();
				}
				catch(Exception e) { }
			}
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set");
		}
	}
	
	/**
	 * Set a new value to a game parameter from the admin console.
	 * @param pName (String) : name of the parameter to change
	 * @param pValue (String) : new value of the parameter
	 * @return boolean : true if modification has been made
	 * @link useAdminCommand
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		//PREMIUM
		if (pName.equalsIgnoreCase("PremiumRateXp")) PREMIUM_RATE_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateSp")) PREMIUM_RATE_SP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateDropItems")) PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateDropSpoil")) PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateDropAdena")) PREMIUM_RATE_DROP_ITEMS_ID.put(57, Float.parseFloat(pValue));
		// rates.properties
		else if (pName.equalsIgnoreCase("RateXp")) RATE_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateSp")) RATE_SP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RatePartyXp")) RATE_PARTY_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RatePartySp")) RATE_PARTY_SP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateConsumableCost")) RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateExtractFish")) RATE_EXTR_FISH = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropItems")) RATE_DROP_ITEMS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropAdena")) RATE_DROP_ITEMS_ID.put(57, Float.parseFloat(pValue));
		else if (pName.equalsIgnoreCase("RateRaidDropItems")) RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropSpoil")) RATE_DROP_SPOIL = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropManor")) RATE_DROP_MANOR = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("RateQuestDrop")) RATE_QUEST_DROP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestReward")) RATE_QUEST_REWARD = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestRewardXP")) RATE_QUEST_REWARD_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestRewardSP")) RATE_QUEST_REWARD_SP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestRewardAdena")) RATE_QUEST_REWARD_ADENA = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("UseQuestRewardMultipliers")) RATE_QUEST_REWARD_USE_MULTIPLIERS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("RateQuestRewardPotion")) RATE_QUEST_REWARD_POTION = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestRewardScroll")) RATE_QUEST_REWARD_SCROLL = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestRewardRecipe")) RATE_QUEST_REWARD_RECIPE = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestRewardMaterial")) RATE_QUEST_REWARD_MATERIAL = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateHellboundTrustIncrease")) RATE_HB_TRUST_INCREASE = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateHellboundTrustDecrease")) RATE_HB_TRUST_DECREASE = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateVitalityLevel1")) RATE_VITALITY_LEVEL_1 = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateVitalityLevel2")) RATE_VITALITY_LEVEL_2 = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateVitalityLevel3")) RATE_VITALITY_LEVEL_3 = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateVitalityLevel4")) RATE_VITALITY_LEVEL_4 = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateRecoveryPeaceZone")) RATE_RECOVERY_VITALITY_PEACE_ZONE = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateVitalityLost")) RATE_VITALITY_LOST = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateVitalityGain")) RATE_VITALITY_GAIN = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateRecoveryOnReconnect")) RATE_RECOVERY_ON_RECONNECT = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateKarmaExpLost")) RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice")) RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateCommonHerbs")) RATE_DROP_COMMON_HERBS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateHpHerbs")) RATE_DROP_HP_HERBS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateMpHerbs")) RATE_DROP_MP_HERBS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateSpecialHerbs")) RATE_DROP_SPECIAL_HERBS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateVitalityHerbs")) RATE_DROP_VITALITY_HERBS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PlayerDropLimit")) PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDrop")) PLAYER_RATE_DROP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDropItem")) PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDropEquip")) PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon")) PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PetXpRate")) PET_XP_RATE = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PetFoodRate")) PET_FOOD_RATE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("SinEaterXpRate")) SINEATER_XP_RATE = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("KarmaDropLimit")) KARMA_DROP_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDrop")) KARMA_RATE_DROP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDropItem")) KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDropEquip")) KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon")) KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("CTFEvenTeams")) CTF_EVEN_TEAMS = pValue;
		else if (pName.equalsIgnoreCase("CTFAllowVoiceCommand")) CTF_ALLOW_VOICE_COMMAND = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CTFAllowInterference")) CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CTFAllowPotions")) CTF_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CTFAllowSummon")) CTF_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CTFOnStartRemoveAllEffects")) CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CTFOnStartUnsummonPet")) CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CTFReviveDelay")) CTF_REVIVE_DELAY = Long.parseLong(pValue);
		else if (pName.equalsIgnoreCase("CTFEventEnabled")) CTF_EVENT_ENABLED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CTFEventInterval")) CTF_EVENT_INTERVAL = pValue.split(",");
		
		// general.properties
		else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter")) AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("DestroyPlayerDroppedItem")) DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("DestroyEquipableItem")) DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("SaveDroppedItem")) SAVE_DROPPED_ITEM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("EmptyDroppedItemTableAfterLoad")) EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("SaveDroppedItemInterval")) SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ClearDroppedItemTable")) CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("PreciseDropCalculation")) PRECISE_DROP_CALCULATION = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("MultipleItemDrop")) MULTIPLE_ITEM_DROP = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("LowWeight")) LOW_WEIGHT = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("MediumWeight")) MEDIUM_WEIGHT = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("HighWeight")) HIGH_WEIGHT = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("AdvancedDiagonalStrategy")) ADVANCED_DIAGONAL_STRATEGY= Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("DiagonalWeight")) DIAGONAL_WEIGHT = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("MaxPostfilterPasses")) MAX_POSTFILTER_PASSES = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CoordSynchronize")) COORD_SYNCHRONIZE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("DeleteCharAfterDays")) DELETE_DAYS = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("ClientPacketQueueSize"))
		{
			CLIENT_PACKET_QUEUE_SIZE = Integer.parseInt(pValue);
			if (CLIENT_PACKET_QUEUE_SIZE == 0)
				CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 1;
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxBurstSize"))
		{
			CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = Integer.parseInt(pValue);
			if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
				CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS;
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxPacketsPerSecond")) CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ClientPacketQueueMeasureInterval")) CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxAveragePacketsPerSecond")) CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxFloodsPerMin")) CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxOverflowsPerMin")) CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxUnderflowsPerMin")) CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxUnknownPerMin")) CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("AllowDiscardItem")) ALLOW_DISCARDITEM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowRefund")) ALLOW_REFUND = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowWarehouse")) ALLOW_WAREHOUSE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowWear")) ALLOW_WEAR = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("WearDelay")) WEAR_DELAY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("WearPrice")) WEAR_PRICE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AllowWater")) ALLOW_WATER = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowRentPet")) ALLOW_RENTPET = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("BoatBroadcastRadius")) BOAT_BROADCAST_RADIUS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AllowCursedWeapons")) ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowManor")) ALLOW_MANOR = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowNpcWalkers")) ALLOW_NPC_WALKERS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowPetWalkers")) ALLOW_PET_WALKERS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("BypassValidation")) BYPASS_VALIDATION = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("CommunityType")) COMMUNITY_TYPE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("BBSShowPlayerList")) BBS_SHOW_PLAYERLIST = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("BBSDefault")) BBS_DEFAULT = pValue;
		else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard")) SHOW_LEVEL_COMMUNITYBOARD = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard")) SHOW_STATUS_COMMUNITYBOARD = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard")) NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard")) NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("ShowServerNews")) SERVER_NEWS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ShowNpcLevel")) SHOW_NPC_LVL = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ShowCrestWithoutQuest")) SHOW_CREST_WITHOUT_QUEST = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("ForceInventoryUpdate")) FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData")) AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("MaximumOnlineUsers")) MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("ZoneTown")) ZONE_TOWN = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("CheckKnownList")) CHECK_KNOWN = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("MaxDriftRange")) MAX_DRIFT_RANGE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRules")) DEEPBLUE_DROP_RULES = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRulesRaid")) DEEPBLUE_DROP_RULES_RAID = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("GuardAttackAggroMob")) GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CancelLesserEffect")) EFFECT_CANCELING = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf")) INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf")) INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer")) INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumSlotsForQuestItems")) INVENTORY_MAXIMUM_QUEST_ITEMS = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf")) WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf")) WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan")) WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("EnchantChanceWeapon")) ENCHANT_CHANCE_WEAPON = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantChanceArmor")) ENCHANT_CHANCE_ARMOR = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantChanceJewelry")) ENCHANT_CHANCE_JEWELRY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantChanceElementStone")) ENCHANT_CHANCE_ELEMENT_STONE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantChanceElementCrystal")) ENCHANT_CHANCE_ELEMENT_CRYSTAL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantChanceElementJewel")) ENCHANT_CHANCE_ELEMENT_JEWEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantChanceElementEnergy")) ENCHANT_CHANCE_ELEMENT_ENERGY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantMaxWeapon")) ENCHANT_MAX_WEAPON = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantMaxArmor")) ENCHANT_MAX_ARMOR = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantMaxJewelry")) ENCHANT_MAX_JEWELRY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantSafeMax")) ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantSafeMaxFull")) ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantStepEnabled")) ENCHANT_STEP_ENABLED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("EnchantStepMode")) ENCHANT_STEP_MODE = pValue;
		else if (pName.equalsIgnoreCase("EnchantStepStatic")) ENCHANT_STEP_STATIC = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantStepDynamic")) ENCHANT_STEP_DYNAMIC = Double.parseDouble(pValue);
		

		else if (pName.equalsIgnoreCase("AugmentationNGSkillChance")) AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationNGGlowChance")) AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationMidSkillChance")) AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationMidGlowChance")) AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationHighSkillChance")) AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationHighGlowChance")) AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationTopSkillChance")) AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationTopGlowChance")) AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationBaseStatChance")) AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("HpRegenMultiplier")) HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("MpRegenMultiplier")) MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("CpRegenMultiplier")) CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		
		else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier")) RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier")) RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("RaidPDefenceMultiplier")) RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(pValue) /100;
		else if (pName.equalsIgnoreCase("RaidMDefenceMultiplier")) RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(pValue) /100;
		else if (pName.equalsIgnoreCase("RaidPAttackMultiplier")) RAID_PATTACK_MULTIPLIER = Double.parseDouble(pValue) /100;
		else if (pName.equalsIgnoreCase("RaidMAttackMultiplier")) RAID_MATTACK_MULTIPLIER = Double.parseDouble(pValue) /100;
		else if (pName.equalsIgnoreCase("RaidMinionRespawnTime")) RAID_MINION_RESPAWN_TIMER =Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("RaidChaosTime")) RAID_CHAOS_TIME =Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("GrandChaosTime")) GRAND_CHAOS_TIME =Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MinionChaosTime")) MINION_CHAOS_TIME =Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("StartingAdena")) STARTING_ADENA = Long.parseLong(pValue);
		else if (pName.equalsIgnoreCase("StartingLevel")) STARTING_LEVEL = Byte.parseByte(pValue);
		else if (pName.equalsIgnoreCase("StartingSP")) STARTING_SP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("UnstuckInterval")) UNSTUCK_INTERVAL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("TeleportWatchdogTimeout")) TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("PlayerSpawnProtection")) PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerFakeDeathUpProtection")) PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("RestorePlayerInstance")) RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowSummonToInstance")) ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("PartyXpCutoffMethod")) PARTY_XP_CUTOFF_METHOD = pValue;
		else if (pName.equalsIgnoreCase("PartyXpCutoffPercent")) PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("PartyXpCutoffLevel")) PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("RespawnRestoreCP")) RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
		else if (pName.equalsIgnoreCase("RespawnRestoreHP")) RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
		else if (pName.equalsIgnoreCase("RespawnRestoreMP")) RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
		
		else if (pName.equalsIgnoreCase("MaxPvtStoreSellSlotsDwarf")) MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxPvtStoreSellSlotsOther")) MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxPvtStoreBuySlotsDwarf")) MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxPvtStoreBuySlotsOther")) MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("StoreSkillCooltime")) STORE_SKILL_COOLTIME = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("SubclassStoreSkillCooltime")) SUBCLASS_STORE_SKILL_COOLTIME = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AnnounceMammonSpawn")) ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("AltGameTiredness")) ALT_GAME_TIREDNESS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("EnableFallingDamage")) ENABLE_FALLING_DAMAGE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreation")) ALT_GAME_CREATION = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationSpeed")) ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationXpRate")) ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationRareXpSpRate")) ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationSpRate")) ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltWeightLimit")) ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltBlacksmithUseRecipes")) ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltGameSkillLearn")) ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("RemoveCastleCirclets")) REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ReputationScorePerKill")) REPUTATION_SCORE_PER_KILL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
		{
			ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
		}
		
		else if (pName.equalsIgnoreCase("AltShieldBlocks")) ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate")) ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("Delevel")) ALT_GAME_DELEVEL = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("MagicFailures")) ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltMobAgroInPeaceZone")) ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("AltGameExponentXp")) ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("AltGameExponentSp")) ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
		
		else if (pName.equalsIgnoreCase("AllowClassMasters")) ALLOW_CLASS_MASTERS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowEntireTree")) ALLOW_ENTIRE_TREE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AlternateClassMaster")) ALTERNATE_CLASS_MASTER = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltPartyRange")) ALT_PARTY_RANGE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltPartyRange2")) ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("CraftingEnabled")) IS_CRAFTING_ENABLED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CraftMasterwork")) CRAFT_MASTERWORK = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("LifeCrystalNeeded")) LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("SpBookNeeded")) SP_BOOK_NEEDED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLoot")) AUTO_LOOT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLootRaids")) AUTO_LOOT_RAIDS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLootHerbs")) AUTO_LOOT_HERBS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLootIndividual")) L2JMOD_AUTO_LOOT_INDIVIDUAL = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone")) ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop")) ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK")) ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport")) ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade")) ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse")) ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("MaxPersonalFamePoints")) MAX_PERSONAL_FAME_POINTS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("FortressZoneFameTaskFrequency")) FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("FortressZoneFameAquirePoints")) FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CastleZoneFameTaskFrequency")) CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CastleZoneFameAquirePoints")) CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltCastleForDawn")) ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltCastleForDusk")) ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltRequireClanCastle")) ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltFreeTeleporting")) ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests")) ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltSubclassEverywhere")) ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltMembersCanWithdrawFromClanWH")) ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("DwarfRecipeLimit")) DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CommonRecipeLimit")) COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("EnableAutoVoteEngine")) L2JMOD_VOTE_ENGINE_ENABLE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoVoteEngineSaveLoad")) L2JMOD_VOTE_ENGINE_SAVE = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("ChampionEnable")) L2JMOD_CHAMPION_ENABLE =	Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ChampionFrequency")) L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionMinLevel")) L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionMaxLevel")) L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionHp")) L2JMOD_CHAMPION_HP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionHpRegen")) L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewards")) L2JMOD_CHAMPION_REWARDS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionAdenasRewards")) L2JMOD_CHAMPION_ADENAS_REWARDS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionAtk")) L2JMOD_CHAMPION_ATK = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionSpdAtk")) L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewardLowerLvlItemChance")) L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewardHigherLvlItemChance")) L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewardItemID")) L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewardItemQty")) L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionEnableInInstances")) L2JMOD_CHAMPION_ENABLE_IN_INSTANCES =	Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("AllowWedding")) L2JMOD_ALLOW_WEDDING = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("WeddingPrice")) L2JMOD_WEDDING_PRICE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("WeddingPunishInfidelity")) L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("WeddingTeleport")) L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("WeddingTeleportPrice")) L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("WeddingTeleportDuration")) L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("WeddingAllowSameSex")) L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("WeddingFormalWear")) L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("WeddingDivorceCosts")) L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("TvTEventEnabled")) TVT_EVENT_ENABLED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("TvTEventInterval")) TVT_EVENT_INTERVAL = pValue.split(",");
		else if (pName.equalsIgnoreCase("TvTEventParticipationTime")) TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("TvTEventRunningTime")) TVT_EVENT_RUNNING_TIME = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("TvTEventParticipationNpcId")) TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("EnableWarehouseSortingClan")) L2JMOD_ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("EnableWarehouseSortingPrivate")) L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("EnableManaPotionSupport")) L2JMOD_ENABLE_MANA_POTIONS_SUPPORT = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("DisplayServerTime")) L2JMOD_DISPLAY_SERVER_TIME = Boolean.parseBoolean(pValue);
		
		else if (pName.equalsIgnoreCase("AntiFeedEnable")) L2JMOD_ANTIFEED_ENABLE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AntiFeedDualbox")) L2JMOD_ANTIFEED_DUALBOX = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AntiFeedDisconnectedAsDualbox")) L2JMOD_ANTIFEED_DISCONNECTED_AS_DUALBOX = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AntiFeedInterval")) L2JMOD_ANTIFEED_INTERVAL = 1000*Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MultiBoxesPerPC")) MAX_PLAYERS_FROM_ONE_PC = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MinKarma")) KARMA_MIN_KARMA = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxKarma")) KARMA_MAX_KARMA = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("XPDivider")) KARMA_XP_DIVIDER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("BaseKarmaLost")) KARMA_LOST_BASE = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("CanGMDropEquipment")) KARMA_DROP_GM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint")) KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop")) KARMA_PK_LIMIT = Integer.parseInt(pValue);
		
		else if (pName.equalsIgnoreCase("PvPVsNormalTime")) PVP_NORMAL_TIME = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PvPVsPvPTime")) PVP_PVP_TIME = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("GlobalChat")) DEFAULT_GLOBAL_CHAT = pValue;
		else if (pName.equalsIgnoreCase("TradeChat")) DEFAULT_TRADE_CHAT = pValue;
		else if (pName.equalsIgnoreCase("GMAdminMenuStyle")) GM_ADMIN_MENU_STYLE = pValue;
		else if (pName.equalsIgnoreCase("EnableBotReport")) ENABLE_BOTREPORT = Boolean.parseBoolean(pValue);
		else
		{
			try
			{
				//TODO: stupid GB configs...
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
	
	private Config() { }
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.
	 * @param string (String) : hexadecimal ID of the server to store
	 * @see HEXID_FILE
	 * @see saveHexid(String string, String fileName)
	 * @link LoginServerThread
	 */
	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, HEXID_FILE);
	}
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.
	 * @param hexId (String) : hexadecimal ID of the server to store
	 * @param fileName (String) : name of the L2Properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			L2Properties hexSetting = new L2Properties();
			File file = new File(fileName);
			//Create a new empty file only if it doesn't exist
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			hexSetting.setProperty("ServerID",String.valueOf(serverId));
			hexSetting.setProperty("HexID",hexId);
			hexSetting.store(out,"the hexID to auth into login");
			out.close();
		}
		catch (Exception e)
		{
			_log.warning(StringUtil.concat("Failed to save hex id to ", fileName, " File."));
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads flood protector configurations.
	 */
	private static void loadFloodProtectorConfigs(final L2Properties properties)
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
	
	/**
	 * Loads single flood protector configuration.
	 * 
	 * @param properties
	 *            L2Properties file reader
	 * @param config
	 *            flood protector configuration instance
	 * @param configString
	 *            flood protector configuration string that determines for which flood protector
	 *            configuration should be read
	 * @param defaultInterval
	 *            default flood protector interval
	 */
	private static void loadFloodProtectorConfig(final L2Properties properties, final FloodProtectorConfig config, final String configString, final String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "False"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE = properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
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
	
	public static class ClassMasterSettings
	{
		private TIntObjectHashMap<TIntIntHashMap> _claimItems;
		private TIntObjectHashMap<TIntIntHashMap> _rewardItems;
		private TIntObjectHashMap<Boolean> _allowedClassChange;
		
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
	
	private static TIntFloatHashMap parseConfigLine(String line)
	{
		String[] propertySplit = line.split(",");
		TIntFloatHashMap ret = new TIntFloatHashMap(propertySplit.length);
		int i = 1;
		for (String value : propertySplit)
			ret.put(i++, Float.parseFloat(value));
		return ret;
	}

	/**
	 * itemId1,itemNumber1;itemId2,itemNumber2...
	 * to the int[n][2] = [itemId1][itemNumber1],[itemId2][itemNumber2]...
	 */
	private static int[][] parseItemsList(String line)
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
}