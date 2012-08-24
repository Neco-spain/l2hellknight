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
package l2.hellknight;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.util.L2Properties;
import l2.hellknight.util.StringUtil;

public final class ExternalConfig
{
	protected static final Logger _log = Logger.getLogger(ExternalConfig.class.getName());
	
	//--------------------------------------------------\\
	//					Custom							\\
	//--------------------------------------------------\\
	public static final String PCBANG_CONFIG_FILE = "./config/pccafe.properties";
	public static final String CUSTOM_CONFIG_FILE = "./config/Custom.properties";
	public static final String CUSTOM_NPC_CONFIG_FILE = "./config/CustomNpc.properties";
	public static final String TVT_ROUND_CONFIG_FILE = "./config/events/TvTRoundsEvent.properties";
	public static final String VIP_CONFIG_FILE = "./config/vip/VipSettings.properties";
	public static final String NPCBUFFER_CONFIG_FILE = "./config/npcbuffer.properties";
	public static final String PCSTATS_CONFIG_FILE = "./config/Character/PcStats.properties";

	
	//--------------------------------------------------
	// Custom Settings
	//--------------------------------------------------
	public static boolean RANK_ARENA_ACCEPT_SAME_IP;
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
	public static boolean CLAN_LEADER_COLOR_ENABLED;
	public static int   CLAN_LEADER_COLOR;
	public static boolean NOXPGAIN_ENABLED;
    public static boolean CHAR_TITLE;
    public static String ADD_CHAR_TITLE;
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	
	public static int GAME_POINT_ITEM_ID;
	//vote reward //
	public static String SERVER_WEBSITE;
	public static boolean VOTESYSTEMENABLE;
	public static int REQUIREDVOTES;
	public static String WEBSITE_SERVER_LINK;
	public static int ITEM_ID;
	public static int ITEM_COUNT;
	public static String VOTE_GS_DATABASE;
	public static String VOTE_LS_DATABASE;

    public static String VOTE_LINK_HOPZONE;
    public static String VOTE_LINK_TOPZONE;
    public static int VOTE_REWARD_ID1;
    public static int VOTE_REWARD_ID2;
    public static int VOTE_REWARD_ID3;
    public static int VOTE_REWARD_AMOUNT1;
    public static int VOTE_REWARD_AMOUNT2;
    public static int VOTE_REWARD_AMOUNT3;

	// Shop Distance
	public static int SHOP_MIN_RANGE_FROM_NPC;
	public static int SHOP_MIN_RANGE_FROM_PLAYER;
	public static boolean AUTO_ACTIVATE_SHOTS;
	public static int AUTO_ACTIVATE_SHOTS_MIN;
	//community custom
	public static boolean ENABLE_COMMUNITY_BUFFER;
	public static boolean ENABLE_COMMUNITY_CLASMASTER;
	public static boolean ENABLE_COMMUNITY_SHOP;
	public static boolean cbInOlympiadMode;
	public static boolean cbInFlying;	
	public static boolean cbInObserveMode;	
	public static boolean cbInDead;	
	public static boolean cbInKarma;
	public static boolean cbInSiege;	
	public static boolean cbInCombat;	
	public static boolean cbInCast;	
	public static boolean cbInAttack;	
	public static boolean cbInTransform;	
	public static boolean cbInDuel;	
	public static boolean cbInFishing;	
	public static boolean cbInVechile;	
	public static boolean cbInStore;	
	public static boolean cbInTvT;
	public static boolean cbInTvTRound;
	public static String ALLOW_CLASS_MASTERSCB;
	public static String CLASS_MASTERS_PRICECB;
	public static int CLASS_MASTERS_PRICE_ITEMCB;
	public static int[] CLASS_MASTERS_PRICE_LISTCB = new int[4];
	public static ArrayList<Integer> ALLOW_CLASS_MASTERS_LISTCB = new ArrayList<>();

	//pc stats
	public static boolean ENABLE_RUNE_BONUS;
	
	//Npc Buffer
	public static boolean NpcBuffer_Reload;
	public static boolean NpcBuffer_SmartWindow;
	public static boolean NpcBuffer_VIP;
	public static int NpcBuffer_VIP_ALV;
	public static boolean NpcBuffer_EnableBuff;
	public static boolean NpcBuffer_EnableScheme;
	public static boolean NpcBuffer_EnableHeal;
	public static boolean NpcBuffer_EnableBuffs;
	public static boolean NpcBuffer_EnableResist;
	public static boolean NpcBuffer_EnableSong;
	public static boolean NpcBuffer_EnableDance;
	public static boolean NpcBuffer_EnableChant;
	public static boolean NpcBuffer_EnableOther;
	public static boolean NpcBuffer_EnableSpecial;
	public static boolean NpcBuffer_EnableCubic;
	public static boolean NpcBuffer_EnableCancel;
	public static boolean NpcBuffer_EnableBuffSet;
	public static boolean NpcBuffer_EnableBuffPK;
	public static boolean NpcBuffer_EnableFreeBuffs;
	public static boolean NpcBuffer_EnableTimeOut;
	public static int NpcBuffer_TimeOutTime;
	public static int NpcBuffer_MinLevel;
	public static int NpcBuffer_PriceCancel;
	public static int NpcBuffer_PriceHeal;
	public static int NpcBuffer_PriceBuffs;
	public static int NpcBuffer_PriceResist;
	public static int NpcBuffer_PriceSong;
	public static int NpcBuffer_PriceDance;
	public static int NpcBuffer_PriceChant;
	public static int NpcBuffer_PriceOther;
	public static int NpcBuffer_PriceSpecial;
	public static int NpcBuffer_PriceCubic;
	public static int NpcBuffer_PriceSet;
	public static int NpcBuffer_PriceScheme;
	public static int NpcBuffer_MaxScheme;
	public static int NpcBuffer_consumableID;
	
	/** ************************************************** **/
    /**  Database config.                            **/
	/** ************************************************** **/
    public static boolean DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP;
    public static boolean DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN;
    public static String DATABASE_BACKUP_DATABASE_NAME;
    public static String DATABASE_BACKUP_SAVE_PATH;
    public static boolean DATABASE_BACKUP_COMPRESSION;
    public static String DATABASE_BACKUP_MYSQLDUMP_PATH;
    
    //level protection
    public static int L2JMOD_DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER;
    public static int L2JMOD_PUNISH_PK_PLAYER_IF_PKS_OVER;
    public static long L2JMOD_PK_MONITOR_PERIOD;
    public static String L2JMOD_PK_PUNISHMENT_TYPE;
    public static long L2JMOD_PK_PUNISHMENT_PERIOD;

	public static int MIN_FREYA_PLAYERS;
	public static int MAX_FREYA_PLAYERS;
	public static int MIN_LEVEL_PLAYERS;
	public static int MIN_FREYA_HC_PLAYERS;
	public static int MAX_FREYA_HC_PLAYERS;
	public static int MIN_LEVEL_HC_PLAYERS;
    public static int Interval_Of_Sailren_Spawn;
    public static int Random_Of_Sailren_Spawn;
	//--------------------------------------------------
	// PC bang points
	//--------------------------------------------------
	public static boolean PC_BANG_ENABLED;
	public static int MAX_PC_BANG_POINTS;
	public static boolean ENABLE_DOUBLE_PC_BANG_POINTS;
	public static int DOUBLE_PC_BANG_POINTS_CHANCE;
	public static double PC_BANG_POINT_RATE;
	public static boolean RANDOM_PC_BANG_POINT;

	public static TIntArrayList CUSTOM_GM_SHOP;
	public static TIntArrayList CUSTOM_TELEPORT_ID;
	public static boolean ENABLE_RANDOM_SPAWN_MOB;
	public static boolean USE_RANDOMXY_FROM_DATABASE;
	public static int MIN_SPAWN_MOB;
	public static int MAX_SPAWN_MOB;

	// TvT Round Event
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

	//--------------------------------------------------
	// Premium Settings
	//--------------------------------------------------
	public static boolean USE_PREMIUMSERVICE; 
	public static float PREMIUM_RATE_XP; 
	public static float PREMIUM_RATE_SP; 
	public static TIntFloatHashMap PR_RATE_DROP_ITEMS_ID; 
	public static float PREMIUM_RATE_DROP_SPOIL; 
	public static float PREMIUM_RATE_DROP_ITEMS; 
	public static float PREMIUM_RATE_DROP_QUEST;
	public static float PREMIUM_RATE_DROP_ITEMS_BY_RAID;
	public static boolean PR_ENABLE_MODIFY_SKILL_DURATION;
	public static TIntIntHashMap PR_SKILL_DURATION_LIST;
	public static int PREMIUM_COIN;

	public static boolean ENTER_HELLBOUND_WITHOUT_QUEST;



	/**
	 * This class initializes all global variables for configuration.<br>
	 * If the key doesn't appear in properties file, a default value is set by this class.
	 */
	public static void loadconfig()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			_log.info("Loading Custom GameServer Configuration Files...");
			// Load Custom L2Properties file (if exists)
			final File CustomProperties = new File(CUSTOM_CONFIG_FILE);
			try (InputStream is = new FileInputStream(CustomProperties))
			{
				L2Properties CustomPropertiesSettings = new L2Properties();
				CustomPropertiesSettings.load(is);
			
				RANK_ARENA_ACCEPT_SAME_IP = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("ArenaAcceptSameIP", "true"));
				
				RANK_ARENA_ENABLED = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("RankArenaEnabled", "false"));
				RANK_ARENA_INTERVAL = Integer.parseInt(CustomPropertiesSettings.getProperty("RankArenaInterval", "120"));
				RANK_ARENA_REWARD_ID = Integer.parseInt(CustomPropertiesSettings.getProperty("RankArenaRewardId", "57"));
				RANK_ARENA_REWARD_COUNT = Integer.parseInt(CustomPropertiesSettings.getProperty("RankArenaRewardCount", "1000"));
				
				RANK_FISHERMAN_ENABLED = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("RankFishermanEnabled", "false"));
				RANK_FISHERMAN_INTERVAL = Integer.parseInt(CustomPropertiesSettings.getProperty("RankFishermanInterval", "120"));
				RANK_FISHERMAN_REWARD_ID = Integer.parseInt(CustomPropertiesSettings.getProperty("RankFishermanRewardId", "57"));
				RANK_FISHERMAN_REWARD_COUNT = Integer.parseInt(CustomPropertiesSettings.getProperty("RankFishermanRewardCount", "1000"));
			
				RANK_CRAFT_ENABLED = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("RankCraftEnabled", "false"));
				RANK_CRAFT_INTERVAL = Integer.parseInt(CustomPropertiesSettings.getProperty("RankCraftInterval", "120"));
				RANK_CRAFT_REWARD_ID = Integer.parseInt(CustomPropertiesSettings.getProperty("RankCraftRewardId", "57"));
				RANK_CRAFT_REWARD_COUNT = Integer.parseInt(CustomPropertiesSettings.getProperty("RankCraftRewardCount", "1000"));
				
				RANK_TVT_ENABLED = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("RankTvTEnabled", "false"));
				RANK_TVT_INTERVAL = Integer.parseInt(CustomPropertiesSettings.getProperty("RankTvTInterval", "120"));
				RANK_TVT_REWARD_ID = Integer.parseInt(CustomPropertiesSettings.getProperty("RankTvTRewardId", "57"));
				RANK_TVT_REWARD_COUNT = Integer.parseInt(CustomPropertiesSettings.getProperty("RankTvTRewardCount", "1000"));
				CLAN_LEADER_COLOR_ENABLED = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("ClanLeaderColorEnabled", "False"));
				CLAN_LEADER_COLOR = Integer.decode("0x" +CustomPropertiesSettings.getProperty("ClanLeaderColor", "00FF00"));
				NOXPGAIN_ENABLED = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("NoXPGainEnable", "false"));
				CHAR_TITLE = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("CharTitle", "true"));
				ADD_CHAR_TITLE = CustomPropertiesSettings.getProperty("CharAddTitle", "Welcome");
				SPAWN_CHAR = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("CustomSpawn", "false"));
				SPAWN_X = Integer.parseInt(CustomPropertiesSettings.getProperty("SpawnX", ""));
				SPAWN_Y = Integer.parseInt(CustomPropertiesSettings.getProperty("SpawnY", ""));
				SPAWN_Z = Integer.parseInt(CustomPropertiesSettings.getProperty("SpawnZ", ""));
				GAME_POINT_ITEM_ID = Integer.parseInt(CustomPropertiesSettings.getProperty("GamePointItemId", "-1"));
				/**Hopzone vote reward**/
				VOTESYSTEMENABLE = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("EnableHopzoneVoteReard", "false"));
				SERVER_WEBSITE = CustomPropertiesSettings.getProperty("ServerWebsite", "link");
				WEBSITE_SERVER_LINK = CustomPropertiesSettings.getProperty("WebsiteServerLink", "link");
				REQUIREDVOTES = Integer.parseInt(CustomPropertiesSettings.getProperty("RequiredVotesForReward", "100"));
				ITEM_ID = Integer.parseInt(CustomPropertiesSettings.getProperty("ItemID", "20392"));
				ITEM_COUNT = Integer.parseInt(CustomPropertiesSettings.getProperty("ItemCount", "5"));
				//VOTE_GS_DATABASE = CustomPropertiesSettings.getProperty("Gs_Database", "gs");;
				VOTE_LS_DATABASE = CustomPropertiesSettings.getProperty("Ls_Database", "login.accounts");
                VOTE_LINK_HOPZONE = CustomPropertiesSettings.getProperty("VoteLinkHopzone", "Null");
                VOTE_LINK_TOPZONE = CustomPropertiesSettings.getProperty("VoteLinkTopzone", "Null");
                VOTE_REWARD_ID1 = Integer.parseInt(CustomPropertiesSettings.getProperty("VoteRewardId1", "300"));
                VOTE_REWARD_ID2 = Integer.parseInt(CustomPropertiesSettings.getProperty("VoteRewardId2", "300"));
                VOTE_REWARD_ID3 = Integer.parseInt(CustomPropertiesSettings.getProperty("VoteRewardId3", "300"));
                VOTE_REWARD_AMOUNT1 = Integer.parseInt(CustomPropertiesSettings.getProperty("VoteRewardAmount1", "300"));
                VOTE_REWARD_AMOUNT2 = Integer.parseInt(CustomPropertiesSettings.getProperty("VoteRewardAmount2", "300"));
                VOTE_REWARD_AMOUNT3 = Integer.parseInt(CustomPropertiesSettings.getProperty("VoteRewardAmount3", "300"));
				/**backup manager**/
				DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("DatabaseBackupMakeBackupOnStartup", "False"));
				DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("DatabaseBackupMakeBackupOnShutdown", "False"));
				DATABASE_BACKUP_DATABASE_NAME = CustomPropertiesSettings.getProperty("DatabaseBackupDatabaseName", "l2hellknightgs");
				DATABASE_BACKUP_SAVE_PATH = CustomPropertiesSettings.getProperty("DatabaseBackupSavePath", "/backup/database/");
				DATABASE_BACKUP_COMPRESSION = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("DatabaseBackupCompression", "True"));
				DATABASE_BACKUP_MYSQLDUMP_PATH = CustomPropertiesSettings.getProperty("DatabaseBackupMysqldumpPath", ".");
				//low lvl protection
				L2JMOD_DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER = Integer.parseInt(CustomPropertiesSettings.getProperty("DisableAttackIfLvlDifferenceOver", "0"));
				L2JMOD_PUNISH_PK_PLAYER_IF_PKS_OVER = Integer.parseInt(CustomPropertiesSettings.getProperty("PunishPKPlayerIfPKsOver", "0"));
				L2JMOD_PK_MONITOR_PERIOD = Long.parseLong(CustomPropertiesSettings.getProperty("PKMonitorPeriod", "3600"));
				L2JMOD_PK_PUNISHMENT_TYPE = CustomPropertiesSettings.getProperty("PKPunishmentType", "jail");
				L2JMOD_PK_PUNISHMENT_PERIOD = Long.parseLong(CustomPropertiesSettings.getProperty("PKPunishmentPeriod", "3600"));
				Interval_Of_Sailren_Spawn = Integer.parseInt(CustomPropertiesSettings.getProperty("IntervalOfSailrenSpawn", "12")); 
				if (Interval_Of_Sailren_Spawn < 1 || Interval_Of_Sailren_Spawn > 192)
					Interval_Of_Sailren_Spawn = 12; 
				Interval_Of_Sailren_Spawn = Interval_Of_Sailren_Spawn * 3600000;
										  
				Random_Of_Sailren_Spawn = Integer.parseInt(CustomPropertiesSettings.getProperty("RandomOfSailrenSpawn", "24")); 
				if (Random_Of_Sailren_Spawn < 1 || Random_Of_Sailren_Spawn > 192)
					Random_Of_Sailren_Spawn = 24;
				Random_Of_Sailren_Spawn = Random_Of_Sailren_Spawn * 3600000;
				
				MIN_FREYA_PLAYERS = Integer.parseInt(CustomPropertiesSettings.getProperty("MinFreyaPlayers", "18"));
				MAX_FREYA_PLAYERS = Integer.parseInt(CustomPropertiesSettings.getProperty("MaxFreyaPlayers", "27"));
				MIN_LEVEL_PLAYERS = Integer.parseInt(CustomPropertiesSettings.getProperty("MinLevelPlayers", "82"));
				MIN_FREYA_HC_PLAYERS = Integer.parseInt(CustomPropertiesSettings.getProperty("MinFreyaHcPlayers", "36"));
				MAX_FREYA_HC_PLAYERS = Integer.parseInt(CustomPropertiesSettings.getProperty("MaxFreyaHcPlayers", "45"));
				MIN_LEVEL_HC_PLAYERS = Integer.parseInt(CustomPropertiesSettings.getProperty("MinLevelHcPlayers", "82"));
				SHOP_MIN_RANGE_FROM_PLAYER  = Integer.parseInt(CustomPropertiesSettings.getProperty("ShopMinRangeFromPlayer", "0"));
				SHOP_MIN_RANGE_FROM_NPC  = Integer.parseInt(CustomPropertiesSettings.getProperty("ShopMinRangeFromNpc", "0"));

				ENTER_HELLBOUND_WITHOUT_QUEST = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("EnterHellboundWithoutQuest", "False"));
				
				AUTO_ACTIVATE_SHOTS = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AutoActivateShotsEnabled", "False"));
				AUTO_ACTIVATE_SHOTS_MIN = Integer.parseInt(CustomPropertiesSettings.getProperty("AutoActivateShotsMin", "200"));
				
				ENABLE_COMMUNITY_BUFFER = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowBufferInCB", "False"));
				ENABLE_COMMUNITY_SHOP = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowShopInCB", "False"));

			    cbInOlympiadMode = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInOlympiadMode", "False"));
		        cbInFlying = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInFlying", "False"));
		        cbInObserveMode = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInObserveMode", "False"));
		        cbInDead = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInDead", "False"));
		        cbInKarma = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInKarma", "False"));
		        cbInSiege = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInSiege", "False"));
		        cbInCombat = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInCombat", "False"));
		        cbInCast = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInCast", "False"));
		        cbInAttack = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInAttack", "False"));
		        cbInTransform = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInTransform", "False"));
		        cbInDuel = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInDuel", "False"));
		        cbInFishing = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInFishing", "False"));
		        cbInVechile = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInVechile", "False"));
		        cbInStore = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInStore", "False"));
		        cbInTvT = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInTvT", "False"));
		        cbInTvTRound = Boolean.parseBoolean(CustomPropertiesSettings.getProperty("AllowInTvTRound", "False"));
		        
		        ALLOW_CLASS_MASTERSCB = CustomPropertiesSettings.getProperty("AllowClassMastersCB", "0");
		        CLASS_MASTERS_PRICE_ITEMCB = Integer.parseInt(CustomPropertiesSettings.getProperty("ClassMastersPriceItemCB", "57"));
		         if ((ALLOW_CLASS_MASTERSCB.length() != 0) && (!ALLOW_CLASS_MASTERSCB.equals("0")))
		         {
		            for (String id : ALLOW_CLASS_MASTERSCB.split(","))
		            {
		            	ALLOW_CLASS_MASTERS_LISTCB.add(Integer.valueOf(Integer.parseInt(id)));
		            }
		         }
		         CLASS_MASTERS_PRICECB = CustomPropertiesSettings.getProperty("ClassMastersPriceCB", "0,0,0");
		         if (CLASS_MASTERS_PRICECB.length() >= 5)
		         {
		            int level = 0;
		            for (String id : CLASS_MASTERS_PRICECB.split(","))
		            {
		            	CLASS_MASTERS_PRICE_LISTCB[level] = Integer.parseInt(id);
		              	level++;
		            }	
			     }
			}
			catch (Exception e)
			{
				_log.warning("Config: " + e.getMessage());
				throw new Error("Failed to Load "+CUSTOM_CONFIG_FILE+" File.");
			}

			// Load PcStats L2Properties file (if exists)
			final File PcStats = new File(PCSTATS_CONFIG_FILE);
			try (InputStream is = new FileInputStream(PcStats))
			{
				L2Properties PcStatsSettings = new L2Properties();
				PcStatsSettings.load(is);
				ENABLE_RUNE_BONUS = Boolean.parseBoolean(PcStatsSettings.getProperty("EnableBonusManager", "False"));
			}
			
			catch (Exception e)
			{
				_log.warning("Config: " + e.getMessage());
				throw new Error("Failed to Load " + PCSTATS_CONFIG_FILE + " File.");
			}
			// Load npcbuffer L2Properties file (if exists)
			final File npcbuffer = new File(NPCBUFFER_CONFIG_FILE);
			try (InputStream is = new FileInputStream(npcbuffer))
			{
				L2Properties npcbufferSettings = new L2Properties();
				npcbufferSettings.load(is);
				NpcBuffer_Reload = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableReloadScript", "False"));
				NpcBuffer_SmartWindow = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableSmartWindow", "True"));
				NpcBuffer_VIP = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableVIP", "False"));
				NpcBuffer_VIP_ALV= Integer.parseInt(npcbufferSettings.getProperty("VipAccesLevel", "1"));
				NpcBuffer_EnableBuff = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableBuffSection", "True"));
				NpcBuffer_EnableScheme = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableScheme", "True"));
				NpcBuffer_EnableHeal = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableHeal", "True"));
				NpcBuffer_EnableBuffs = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableBuffs", "True"));
				NpcBuffer_EnableResist = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableResist", "True"));
				NpcBuffer_EnableSong = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableSongs", "True"));
				NpcBuffer_EnableDance = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableDances", "True"));
				NpcBuffer_EnableChant = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableChants", "True"));
				NpcBuffer_EnableOther = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableOther", "True"));
				NpcBuffer_EnableSpecial = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableSpecial", "True"));
				NpcBuffer_EnableCubic = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableCubic", "True"));
				NpcBuffer_EnableCancel = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableRemoveBuffs", "True"));
				NpcBuffer_EnableBuffSet = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableBuffSet", "True"));
				NpcBuffer_EnableBuffPK = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableBuffForPK", "False"));
				NpcBuffer_EnableFreeBuffs = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableFreeBuffs", "True"));
				NpcBuffer_EnableTimeOut = Boolean.parseBoolean(npcbufferSettings.getProperty("EnableTimeOut", "True"));
				NpcBuffer_TimeOutTime = Integer.parseInt(npcbufferSettings.getProperty("TimeoutTime", "10"));
				NpcBuffer_MinLevel = Integer.parseInt(npcbufferSettings.getProperty("MinimumLevel", "20"));
				NpcBuffer_PriceCancel = Integer.parseInt(npcbufferSettings.getProperty("RemoveBuffsPrice", "100000"));
				NpcBuffer_PriceHeal = Integer.parseInt(npcbufferSettings.getProperty("HealPrice", "100000"));
				NpcBuffer_PriceBuffs = Integer.parseInt(npcbufferSettings.getProperty("BuffsPrice", "100000"));
				NpcBuffer_PriceResist = Integer.parseInt(npcbufferSettings.getProperty("ResistPrice", "100000"));
				NpcBuffer_PriceSong = Integer.parseInt(npcbufferSettings.getProperty("SongPrice", "100000"));
				NpcBuffer_PriceDance = Integer.parseInt(npcbufferSettings.getProperty("DancePrice", "100000"));
				NpcBuffer_PriceChant = Integer.parseInt(npcbufferSettings.getProperty("ChantsPrice", "100000"));
				NpcBuffer_PriceOther = Integer.parseInt(npcbufferSettings.getProperty("OtherPrice", "100000"));
				NpcBuffer_PriceSpecial = Integer.parseInt(npcbufferSettings.getProperty("SpecialPrice", "100000"));
				NpcBuffer_PriceCubic = Integer.parseInt(npcbufferSettings.getProperty("CubicPrice", "100000"));
				NpcBuffer_PriceSet = Integer.parseInt(npcbufferSettings.getProperty("SetPrice", "10000000"));
				NpcBuffer_PriceScheme = Integer.parseInt(npcbufferSettings.getProperty("SchemePrice", "10000000"));
				NpcBuffer_MaxScheme = Integer.parseInt(npcbufferSettings.getProperty("MaxScheme", "4"));
				NpcBuffer_consumableID = Integer.parseInt(npcbufferSettings.getProperty("ConsumableID", "57"));

			}
			
			catch (Exception e)
			{
				_log.warning("Config: " + e.getMessage());
				throw new Error("Failed to Load " + NPCBUFFER_CONFIG_FILE + " File.");
			}
			
			// Load pcbangpoints L2Properties file (if exists)
			final File pccaffe = new File(PCBANG_CONFIG_FILE);
			try (InputStream is = new FileInputStream(pccaffe))
			{
				L2Properties pccaffeSettings = new L2Properties();
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
			// Load Custom Npc L2Properties file (if exists)
			final File CustomNpc = new File(CUSTOM_NPC_CONFIG_FILE);
			try (InputStream is = new FileInputStream(CustomNpc))
			{
				L2Properties CustomNpcSettings = new L2Properties();
				CustomNpcSettings.load(is);
				String[] split = CustomNpcSettings.getProperty("CustomShopId", "").split(",");
				CUSTOM_GM_SHOP = new TIntArrayList(split.length);
				for (String npcId : split)
				{
					try
					{
						CUSTOM_GM_SHOP.add(Integer.parseInt(npcId));
					}
					catch (NumberFormatException nfe)
					{
						if (!npcId.isEmpty())
						{
							_log.warning("Could not parse " + npcId + " id for Custom Shop Npc Id. Please check that all values are digits and coma separated.");
						}
					}
				}
				split = CustomNpcSettings.getProperty("CustomTeleportId", "").split(",");
				CUSTOM_TELEPORT_ID = new TIntArrayList(split.length);
				for (String npcId : split)
				{
					try
					{
						CUSTOM_TELEPORT_ID.add(Integer.parseInt(npcId));
					}
					catch (NumberFormatException nfe)
					{
						if (!npcId.isEmpty())
						{
							_log.warning("Could not parse " + npcId + " id for Custom Teleport Npc Id. Please check that all values are digits and coma separated.");
						}
					}
				}
				ENABLE_RANDOM_SPAWN_MOB = Boolean.parseBoolean(CustomNpcSettings.getProperty("EnableRandomSpawnMob", "False"));
				USE_RANDOMXY_FROM_DATABASE = Boolean.parseBoolean(CustomNpcSettings.getProperty("EnableRandomXYFromSql", "False"));
				MIN_SPAWN_MOB = Integer.parseInt(CustomNpcSettings.getProperty("MinSpawnMob", "-250"));
				MAX_SPAWN_MOB = Integer.parseInt(CustomNpcSettings.getProperty("MaxSpawnMob", "250"));
			}

			catch (Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + CUSTOM_NPC_CONFIG_FILE + " File.");
			}
			// Load pcbangpoints L2Properties file (if exists)
			final File TvTRound = new File(TVT_ROUND_CONFIG_FILE);
			try (InputStream is = new FileInputStream(TvTRound))
			{
				L2Properties pccaffeSettings = new L2Properties();
				pccaffeSettings.load(is);
				TVT_ROUND_EVENT_ENABLED = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventEnabled", "false"));
				TVT_ROUND_EVENT_IN_INSTANCE = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventInInstance", "false"));
				TVT_ROUND_EVENT_INSTANCE_FILE = pccaffeSettings.getProperty("TvTRoundEventInstanceFile", "coliseum.xml");
				TVT_ROUND_EVENT_INTERVAL = pccaffeSettings.getProperty("TvTRoundEventInterval", "20:00").split(",");
				TVT_ROUND_EVENT_PARTICIPATION_TIME = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventParticipationTime", "3600"));
				TVT_ROUND_EVENT_FIRST_FIGHT_RUNNING_TIME = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventFirstFightRunningTime", "1800"));
				TVT_ROUND_EVENT_SECOND_FIGHT_RUNNING_TIME = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventSecondFightRunningTime", "1800"));
				TVT_ROUND_EVENT_THIRD_FIGHT_RUNNING_TIME = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventThirdFightRunningTime", "1800"));
				TVT_ROUND_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventParticipationNpcId", "0"));
				TVT_ROUND_EVENT_ON_DIE = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventOnDie", "true"));
			
					if (TVT_ROUND_EVENT_PARTICIPATION_NPC_ID == 0)
					{
						TVT_ROUND_EVENT_ENABLED = false;
						_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationNpcId");
					}
					else
					{
						String[] propertySplit = pccaffeSettings.getProperty("TvTRoundEventParticipationNpcCoordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							TVT_ROUND_EVENT_ENABLED = false;
							_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationNpcCoordinates");
						}
						else
						{
							TVT_ROUND_EVENT_REWARDS = new ArrayList<>();
							TVT_ROUND_DOORS_IDS_TO_OPEN = new ArrayList<>();
							TVT_ROUND_DOORS_IDS_TO_CLOSE = new ArrayList<>();
							TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE = new ArrayList<>();
							TVT_ROUND_EVENT_WAIT_OPEN_ANTEROOM_DOORS = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventWaitOpenAnteroomDoors", "30"));
							TVT_ROUND_EVENT_WAIT_CLOSE_ANTEROOM_DOORS = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventWaitCloseAnteroomDoors", "15"));
							TVT_ROUND_EVENT_STOP_ON_TIE = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventStopOnTie", "false"));
							TVT_ROUND_EVENT_MINIMUM_TIE = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventMinimumTie", "1"));
							if (TVT_ROUND_EVENT_MINIMUM_TIE != 1 && TVT_ROUND_EVENT_MINIMUM_TIE != 2 && TVT_ROUND_EVENT_MINIMUM_TIE != 3) TVT_ROUND_EVENT_MINIMUM_TIE = 1;
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
							TVT_ROUND_EVENT_TEAM_1_COORDINATES = new int[3];
							TVT_ROUND_EVENT_TEAM_2_COORDINATES = new int[3];
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
							if (propertySplit.length == 4)
								TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
							TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventMinPlayersInTeams", "1"));
							TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventMaxPlayersInTeams", "20"));
							TVT_ROUND_EVENT_MIN_LVL = (byte)Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventMinPlayerLevel", "1"));
							TVT_ROUND_EVENT_MAX_LVL = (byte)Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventMaxPlayerLevel", "80"));
							TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventStartRespawnLeaveTeleportDelay", "10"));
							TVT_ROUND_EVENT_EFFECTS_REMOVAL = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventEffectsRemoval", "0"));
							TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP = Integer.parseInt(pccaffeSettings.getProperty("TvTRoundEventMaxParticipantsPerIP", "0"));
							TVT_ROUND_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundAllowVoicedInfoCommand", "false"));
							TVT_ROUND_EVENT_TEAM_1_NAME = pccaffeSettings.getProperty("TvTRoundEventTeam1Name", "Team1");
							propertySplit = pccaffeSettings.getProperty("TvTRoundEventTeam1Coordinates", "0,0,0").split(",");
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
								TVT_ROUND_EVENT_TEAM_2_NAME = pccaffeSettings.getProperty("TvTRoundEventTeam2Name", "Team2");
								propertySplit = pccaffeSettings.getProperty("TvTRoundEventTeam2Coordinates", "0,0,0").split(",");
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
									propertySplit = pccaffeSettings.getProperty("TvTRoundEventParticipationFee", "0,0").split(",");
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
									propertySplit = pccaffeSettings.getProperty("TvTRoundEventReward", "57,100000").split(";");
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
									
									TVT_ROUND_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventTargetTeamMembersAllowed", "true"));
									TVT_ROUND_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventScrollsAllowed", "false"));
									TVT_ROUND_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventPotionsAllowed", "false"));
									TVT_ROUND_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventSummonByItemAllowed", "false"));
									TVT_ROUND_GIVE_POINT_TEAM_TIE = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundGivePointTeamTie", "false"));
									TVT_ROUND_REWARD_TEAM_TIE = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundRewardTeamTie", "false"));
									TVT_ROUND_EVENT_REWARD_ON_SECOND_FIGHT_END = Boolean.parseBoolean(pccaffeSettings.getProperty("TvTRoundEventRewardOnSecondFightEnd", "false"));
									propertySplit = pccaffeSettings.getProperty("TvTRoundDoorsToOpen", "").split(";");
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
									
									propertySplit = pccaffeSettings.getProperty("TvTRoundDoorsToClose", "").split(";");
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
									
									propertySplit = pccaffeSettings.getProperty("TvTRoundAnteroomDoorsToOpenClose", "").split(";");
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
									
									propertySplit = pccaffeSettings.getProperty("TvTRoundEventFighterBuffs", "").split(";");
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
									
									propertySplit = pccaffeSettings.getProperty("TvTRoundEventMageBuffs", "").split(";");
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
				throw new Error("Failed to Load " + TVT_ROUND_CONFIG_FILE + " File.");
			}
			// Load pcbangpoints L2Properties file (if exists)
			final File Vip = new File(VIP_CONFIG_FILE);
			try (InputStream is = new FileInputStream(Vip))
			{
				L2Properties VipSettings = new L2Properties();
				VipSettings.load(is);
				USE_PREMIUMSERVICE = Boolean.parseBoolean(VipSettings.getProperty("UsePremiumServices", "False"));
				PREMIUM_COIN = Integer.parseInt(VipSettings.getProperty("PremiumCoin", "4037"));
				PREMIUM_RATE_XP = Float.parseFloat(VipSettings.getProperty("PremiumRateXp", "2"));
				PREMIUM_RATE_SP = Float.parseFloat(VipSettings.getProperty("PremiumRateSp", "2"));
				PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(VipSettings.getProperty("PremiumRateDropSpoil", "2"));
				PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(VipSettings.getProperty("PremiumRateDropItems", "2"));
				PREMIUM_RATE_DROP_QUEST = Float.parseFloat(VipSettings.getProperty("PremiumRateDropQuest", "2"));
				PREMIUM_RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(VipSettings.getProperty("PremiumRateRaidDropItems", "2"));
				// For Premium Service
				String[] propertySplit = VipSettings.getProperty("PrRateDropItemsById", "").split(";");
				PR_RATE_DROP_ITEMS_ID = new TIntFloatHashMap(propertySplit.length);
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
								PR_RATE_DROP_ITEMS_ID.put(Integer.parseInt(itemSplit[0]), Float.parseFloat(itemSplit[1]));
							}
							catch (NumberFormatException nfe)
							{
								if (!item.isEmpty())
								_log.warning(StringUtil.concat("Config.load(): invalid config property -> PremiumRateDropItemsById \"", item, "\""));
							}
						}
					}
				}
				if (PR_RATE_DROP_ITEMS_ID.get(57) == 1)
				{
					PR_RATE_DROP_ITEMS_ID.put(57, PREMIUM_RATE_DROP_ITEMS); //for Adena rate if not defined
				}
			
				PR_ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(VipSettings.getProperty("EnablePrModifySkillDuration", "false"));
				
				// Create Map only if enabled
				if (PR_ENABLE_MODIFY_SKILL_DURATION)
				{
					String[] BuffTimeSplit = VipSettings.getProperty("PrSkillDurationList", "").split(";");
					PR_SKILL_DURATION_LIST = new TIntIntHashMap(BuffTimeSplit.length);
					for (String skill : BuffTimeSplit)
					{
						String[] skillSplit = skill.split(",");
						if (skillSplit.length != 2)
						{
							_log.warning(StringUtil.concat("[SkillDurationList]: invalid config property -> SkillDurationList \"", skill, "\""));
						}
						else
						{
							try
							{
								PR_SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
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
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + VIP_CONFIG_FILE + " File.");
			}
		}
		else
		{
			_log.severe("Could not Load ExternalConfig: server mode was not set");
		}
	}
	
 	public static boolean setParameterValue(String pName, String pValue)
 	{
		switch (pName.trim().toLowerCase())
		{
		// rates.properties
			case "PremiumRateXp":
				PREMIUM_RATE_XP = Float.parseFloat(pValue);
				break;
			case "PremiumRateSp":
				PREMIUM_RATE_SP = Float.parseFloat(pValue);
			break;
			case "PremiumRateDropSpoil":
				PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(pValue);
				break;
			case "PremiumRateDropItems":
				PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(pValue);
				break;
			case "PremiumRateDropQuest":
				PREMIUM_RATE_DROP_QUEST = Float.parseFloat(pValue);
				break;
			case "PremiumRateDropAdena":
				PR_RATE_DROP_ITEMS_ID.put(57, Float.parseFloat(pValue));
				break;
			case "PremiumRateRaidDropItems":
				PREMIUM_RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(pValue);
				break;
			case "AllowInOlympiadMode": 
				cbInOlympiadMode = Boolean.parseBoolean(pValue);
				break;
			case "AllowInFlying": 
				cbInFlying = Boolean.parseBoolean(pValue);
				break;
			case "AllowInObserveMode": 
				cbInObserveMode = Boolean.parseBoolean(pValue);
				break;
			case "AllowInDead": 
				cbInDead = Boolean.parseBoolean(pValue);
				break;
			case "AllowInKarma": 
				cbInKarma = Boolean.parseBoolean(pValue);
				break;
			case "AllowInSiege": 
				cbInSiege = Boolean.parseBoolean(pValue);
				break;
			case "AllowInCombat": 
				cbInCombat = Boolean.parseBoolean(pValue);
				break;
			case "AllowInCast": 
				cbInCast = Boolean.parseBoolean(pValue);
				break;
			case "AllowInAttack": 
				cbInAttack = Boolean.parseBoolean(pValue);
				break;
			case "AllowInTransform": 
				cbInTransform = Boolean.parseBoolean(pValue);
				break;
			case "AllowInDuel": 
				cbInDuel = Boolean.parseBoolean(pValue);
				break;
			case "AllowInFishing": 
				cbInFishing = Boolean.parseBoolean(pValue);
				break;
			case "AllowInVechile": 
				cbInVechile = Boolean.parseBoolean(pValue);
				break;
			case "AllowInStore": 
				cbInStore = Boolean.parseBoolean(pValue);
				break;
			case "AllowInTvT": 
				cbInTvT = Boolean.parseBoolean(pValue);
				break;
			case "AllowInTvTRound": 
				cbInTvTRound = Boolean.parseBoolean(pValue);	
				break;
			case "ClassMastersPriceItemCB": 
				CLASS_MASTERS_PRICE_ITEMCB = Integer.parseInt(pValue);		
				break;
			default:
				try
				{
					// TODO: stupid GB configs...
					if (!pName.startsWith("Interval_") && !pName.startsWith("Random_"))
					{
						pName = pName.toUpperCase();
					}
					Field clazField = Config.class.getField(pName);
					int modifiers = clazField.getModifiers();
					// just in case :)
					if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isFinal(modifiers))
					{
						throw new SecurityException("Cannot modify non public, non static or final config!");
					}
					
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
					{
						return false;
					}
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
	
	private ExternalConfig()
	{
	}
	
}
