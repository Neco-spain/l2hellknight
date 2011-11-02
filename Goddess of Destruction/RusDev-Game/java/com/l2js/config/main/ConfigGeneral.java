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
package com.l2js.config.main;

import gnu.trove.TIntArrayList;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigGeneral extends Config
{
	private final static String path = GENERAL_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			EVERYBODY_HAS_ADMIN_RIGHTS = getBoolean(properties, "EverybodyHasAdminRights", false);
			DISPLAY_SERVER_DEV = getBoolean(properties, "DisplayServerDev", true);
			DISPLAY_SERVER_VERSION = getBoolean(properties, "DisplayServerRevision", true);
			SERVER_LIST_BRACKET = getBoolean(properties, "ServerListBrackets", false);
			SERVER_LIST_TYPE = getServerTypeId(getString(properties, "ServerListType", "Normal").split(","));
			SERVER_LIST_AGE = getInt(properties, "ServerListAge", 0);
			SERVER_GMONLY = getBoolean(properties, "ServerGMOnly", false);
			GM_HERO_AURA = getBoolean(properties, "GMHeroAura", false);
			GM_STARTUP_INVULNERABLE = getBoolean(properties, "GMStartupInvulnerable", false);
			GM_STARTUP_INVISIBLE = getBoolean(properties, "GMStartupInvisible", false);
			GM_STARTUP_SILENCE = getBoolean(properties, "GMStartupSilence", false);
			GM_STARTUP_AUTO_LIST = getBoolean(properties, "GMStartupAutoList", false);
			GM_STARTUP_DIET_MODE = getBoolean(properties, "GMStartupDietMode", false);
			GM_ADMIN_MENU_STYLE = getString(properties, "GMAdminMenuStyle", "modern");
			GM_ITEM_RESTRICTION = getBoolean(properties, "GMItemRestriction", true);
			GM_SKILL_RESTRICTION = getBoolean(properties, "GMSkillRestriction", true);
			GM_TRADE_RESTRICTED_ITEMS = getBoolean(properties, "GMTradeRestrictedItems", false);
			GM_RESTART_FIGHTING = getBoolean(properties, "GMRestartFighting", true);
			GM_ANNOUNCER_NAME = getBoolean(properties, "GMShowAnnouncerName", false);
			GM_GIVE_SPECIAL_SKILLS = getBoolean(properties, "GMGiveSpecialSkills", false);
			BYPASS_VALIDATION = getBoolean(properties, "BypassValidation", true);
			GAMEGUARD_ENFORCE = getBoolean(properties, "GameGuardEnforce", false);
			GAMEGUARD_PROHIBITACTION = getBoolean(properties, "GameGuardProhibitAction", false);
			LOG_CHAT = getBoolean(properties, "LogChat", false);
			LOG_ITEMS = getBoolean(properties, "LogItems", false);
			LOG_ITEMS_SMALL_LOG = getBoolean(properties, "LogItemsSmallLog", false);
			LOG_ITEM_ENCHANTS = getBoolean(properties, "LogItemEnchants", false);
			LOG_SKILL_ENCHANTS = getBoolean(properties, "LogSkillEnchants", false);
			GMAUDIT = getBoolean(properties, "GMAudit", false);
			LOG_GAME_DAMAGE = getBoolean(properties, "LogGameDamage", false);
			LOG_GAME_DAMAGE_THRESHOLD = getInt(properties, "LogGameDamageThreshold", 5000);
			SKILL_CHECK_ENABLE = getBoolean(properties, "SkillCheckEnable", false);
			SKILL_CHECK_REMOVE = getBoolean(properties, "SkillCheckRemove", false);
			SKILL_CHECK_GM = getBoolean(properties, "SkillCheckGM", true);
			DEBUG = getBoolean(properties, "Debug", false);
			PACKET_HANDLER_DEBUG = getBoolean(properties, "PacketHandlerDebug", false);
			DEVELOPER = getBoolean(properties, "Developer", false);
			ACCEPT_GEOEDITOR_CONN = getBoolean(properties, "AcceptGeoeditorConn", false);
			ALT_DEV_NO_HANDLERS = getBoolean(properties, "AltDevNoHandlers", false);
			ALT_DEV_NO_QUESTS = getBoolean(properties, "AltDevNoQuests", false);
			ALT_DEV_NO_SPAWNS = getBoolean(properties, "AltDevNoSpawns", false);
			THREAD_P_EFFECTS = getInt(properties, "ThreadPoolSizeEffects", 10);
			THREAD_P_GENERAL = getInt(properties, "ThreadPoolSizeGeneral", 13);
			IO_PACKET_THREAD_CORE_SIZE = getInt(properties, "UrgentPacketThreadCoreSize", 2);
			GENERAL_PACKET_THREAD_CORE_SIZE = getInt(properties, "GeneralPacketThreadCoreSize", 4);
			GENERAL_THREAD_CORE_SIZE = getInt(properties, "GeneralThreadCoreSize", 4);
			AI_MAX_THREAD = getInt(properties, "AiMaxThread", 6);
			CLIENT_PACKET_QUEUE_SIZE = getInt(properties, "ClientPacketQueueSize", 0);
			if (CLIENT_PACKET_QUEUE_SIZE == 0)
				CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2;
			CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = getInt(properties, "ClientPacketQueueMaxBurstSize", 0);
			if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
				CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1;
			CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = getInt(properties, "ClientPacketQueueMaxPacketsPerSecond", 80);
			CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = getInt(properties, "ClientPacketQueueMeasureInterval", 5);
			CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = getInt(properties,
					"ClientPacketQueueMaxAveragePacketsPerSecond", 40);
			CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = getInt(properties, "ClientPacketQueueMaxFloodsPerMin", 2);
			CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = getInt(properties, "ClientPacketQueueMaxOverflowsPerMin", 1);
			CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = getInt(properties, "ClientPacketQueueMaxUnderflowsPerMin", 1);
			CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = getInt(properties, "ClientPacketQueueMaxUnknownPerMin", 5);
			DEADLOCK_DETECTOR = getBoolean(properties, "DeadLockDetector", false);
			DEADLOCK_CHECK_INTERVAL = getInt(properties, "DeadLockCheckInterval", 20);
			RESTART_ON_DEADLOCK = getBoolean(properties, "RestartOnDeadlock", false);
			ALLOW_DISCARDITEM = getBoolean(properties, "AllowDiscardItem", true);
			AUTODESTROY_ITEM_AFTER = getInt(properties, "AutoDestroyDroppedItemAfter", 600);
			HERB_AUTO_DESTROY_TIME = getInt(properties, "AutoDestroyHerbTime", 60) * 1000;
			String[] split = getString(properties, "ListOfProtectedItems", "0").split(",");
			LIST_PROTECTED_ITEMS = new TIntArrayList(split.length);
			for (String id : split)
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
			CHAR_STORE_INTERVAL = getInt(properties, "CharacterDataStoreInterval", 15);
			LAZY_ITEMS_UPDATE = getBoolean(properties, "LazyItemsUpdate", false);
			UPDATE_ITEMS_ON_CHAR_STORE = getBoolean(properties, "UpdateItemsOnCharStore", false);
			DESTROY_DROPPED_PLAYER_ITEM = getBoolean(properties, "DestroyPlayerDroppedItem", false);
			DESTROY_EQUIPABLE_PLAYER_ITEM = getBoolean(properties, "DestroyEquipableItem", false);
			SAVE_DROPPED_ITEM = getBoolean(properties, "SaveDroppedItem", false);
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = getBoolean(properties, "EmptyDroppedItemTableAfterLoad", false);
			SAVE_DROPPED_ITEM_INTERVAL = getInt(properties, "SaveDroppedItemInterval", 60) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = getBoolean(properties, "ClearDroppedItemTable", false);
			AUTODELETE_INVALID_QUEST_DATA = getBoolean(properties, "AutoDeleteInvalidQuestData", false);
			PRECISE_DROP_CALCULATION = getBoolean(properties, "PreciseDropCalculation", true);
			MULTIPLE_ITEM_DROP = getBoolean(properties, "MultipleItemDrop", true);
			FORCE_INVENTORY_UPDATE = getBoolean(properties, "ForceInventoryUpdate", false);
			LAZY_CACHE = getBoolean(properties, "LazyCache", true);
			CACHE_CHAR_NAMES = getBoolean(properties, "CacheCharNames", true);
			MIN_NPC_ANIMATION = getInt(properties, "MinNPCAnimation", 10);
			MAX_NPC_ANIMATION = getInt(properties, "MaxNPCAnimation", 20);
			MIN_MONSTER_ANIMATION = getInt(properties, "MinMonsterAnimation", 5);
			MAX_MONSTER_ANIMATION = getInt(properties, "MaxMonsterAnimation", 20);
			MOVE_BASED_KNOWNLIST = getBoolean(properties, "MoveBasedKnownlist", false);
			KNOWNLIST_UPDATE_INTERVAL = getLong(properties, "KnownListUpdateInterval", 1250);
			GRIDS_ALWAYS_ON = getBoolean(properties, "GridsAlwaysOn", false);
			GRID_NEIGHBOR_TURNON_TIME = getInt(properties, "GridNeighborTurnOnTime", 1);
			GRID_NEIGHBOR_TURNOFF_TIME = getInt(properties, "GridNeighborTurnOffTime", 90);
			WORLD_X_MIN = getInt(properties, "WorldXMin", 10);
			WORLD_X_MAX = getInt(properties, "WorldXMax", 26);
			WORLD_Y_MIN = getInt(properties, "WorldYMin", 10);
			WORLD_Y_MAX = getInt(properties, "WorldYMax", 26);
			GEODATA = getInt(properties, "GeoData", 0);
			GEODATA_CELLFINDING = getBoolean(properties, "CellPathFinding", false);
			PATHFIND_BUFFERS = getString(properties, "PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
			LOW_WEIGHT = getFloat(properties, "LowWeight", 0.5F);
			MEDIUM_WEIGHT = getFloat(properties, "MediumWeight", 2F);
			HIGH_WEIGHT = getFloat(properties, "HighWeight", 3F);
			ADVANCED_DIAGONAL_STRATEGY = getBoolean(properties, "AdvancedDiagonalStrategy", true);
			DIAGONAL_WEIGHT = getFloat(properties, "DiagonalWeight", 0.707F);
			MAX_POSTFILTER_PASSES = getInt(properties, "MaxPostfilterPasses", 3);
			DEBUG_PATH = getBoolean(properties, "DebugPath", false);
			FORCE_GEODATA = getBoolean(properties, "ForceGeodata", true);
			COORD_SYNCHRONIZE = getInt(properties, "CoordSynchronize", -1);
			String str = getString(properties, "EnableFallingDamage", "auto");
			ENABLE_FALLING_DAMAGE = "auto".equalsIgnoreCase(str) ? GEODATA > 0 : Boolean.parseBoolean(str);
			ZONE_TOWN = getInt(properties, "ZoneTown", 0);
			DEFAULT_GLOBAL_CHAT = getString(properties, "GlobalChat", "ON");
			DEFAULT_TRADE_CHAT = getString(properties, "TradeChat", "ON");
			ALLOW_WAREHOUSE = getBoolean(properties, "AllowWarehouse", true);
			WAREHOUSE_CACHE = getBoolean(properties, "WarehouseCache", false);
			WAREHOUSE_CACHE_TIME = getInt(properties, "WarehouseCacheTime", 15);
			ALLOW_REFUND = getBoolean(properties, "AllowRefund", true);
			ALLOW_MAIL = getBoolean(properties, "AllowMail", true);
			ALLOW_ATTACHMENTS = getBoolean(properties, "AllowAttachments", true);
			ALLOW_WEAR = getBoolean(properties, "AllowWear", true);
			WEAR_DELAY = getInt(properties, "WearDelay", 5);
			WEAR_PRICE = getInt(properties, "WearPrice", 10);
			ALLOW_LOTTERY = getBoolean(properties, "AllowLottery", true);
			ALLOW_RACE = getBoolean(properties, "AllowRace", true);
			ALLOW_WATER = getBoolean(properties, "AllowWater", true);
			ALLOW_RENTPET = getBoolean(properties, "AllowRentPet", false);
			ALLOWFISHING = getBoolean(properties, "AllowFishing", true);
			ALLOW_MANOR = getBoolean(properties, "AllowManor", true);
			ALLOW_BOAT = getBoolean(properties, "AllowBoat", true);
			BOAT_BROADCAST_RADIUS = getInt(properties, "BoatBroadcastRadius", 20000);
			ALLOW_CURSED_WEAPONS = getBoolean(properties, "AllowCursedWeapons", true);
			ALLOW_NPC_WALKERS = getBoolean(properties, "AllowNpcWalkers", true);
			ALLOW_PET_WALKERS = getBoolean(properties, "AllowPetWalkers", true);
			SERVER_NEWS = getBoolean(properties, "ShowServerNews", false);
			COMMUNITY_TYPE = getInt(properties, "CommunityType", 1);
			BBS_SHOW_PLAYERLIST = getBoolean(properties, "BBSShowPlayerList", false);
			BBS_DEFAULT = getString(properties, "BBSDefault", "_bbshome");
			SHOW_LEVEL_COMMUNITYBOARD = getBoolean(properties, "ShowLevelOnCommunityBoard", false);
			SHOW_STATUS_COMMUNITYBOARD = getBoolean(properties, "ShowStatusOnCommunityBoard", true);
			NAME_PAGE_SIZE_COMMUNITYBOARD = getInt(properties, "NamePageSizeOnCommunityBoard", 50);
			NAME_PER_ROW_COMMUNITYBOARD = getInt(properties, "NamePerRowOnCommunityBoard", 5);
			USE_SAY_FILTER = getBoolean(properties, "UseChatFilter", false);
			CHAT_FILTER_CHARS = getString(properties, "ChatFilterChars", "^_^");
			BAN_CHAT_CHANNELS = getIntArray(properties, "BanChatChannels", new int[]{0, 1, 8, 17}, ";");
			ALT_MANOR_REFRESH_TIME = getInt(properties, "AltManorRefreshTime", 20);
			ALT_MANOR_REFRESH_MIN = getInt(properties, "AltManorRefreshMin", 0);
			ALT_MANOR_APPROVE_TIME = getInt(properties, "AltManorApproveTime", 6);
			ALT_MANOR_APPROVE_MIN = getInt(properties, "AltManorApproveMin", 0);
			ALT_MANOR_MAINTENANCE_PERIOD = getInt(properties, "AltManorMaintenancePeriod", 360000);
			ALT_MANOR_SAVE_ALL_ACTIONS = getBoolean(properties, "AltManorSaveAllActions", false);
			ALT_MANOR_SAVE_PERIOD_RATE = getInt(properties, "AltManorSavePeriodRate", 2);
			ALT_LOTTERY_PRIZE = getLong(properties, "AltLotteryPrize", 50000);
			ALT_LOTTERY_TICKET_PRICE = getLong(properties, "AltLotteryTicketPrice", 2000);
			ALT_LOTTERY_5_NUMBER_RATE = getFloat(properties, "AltLottery5NumberRate", 0.6F);
			ALT_LOTTERY_4_NUMBER_RATE = getFloat(properties, "AltLottery4NumberRate", 0.2F);
			ALT_LOTTERY_3_NUMBER_RATE = getFloat(properties, "AltLottery3NumberRate", 0.2F);
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = getLong(properties, "AltLottery2and1NumberPrize", 200);
			ALT_ITEM_AUCTION_ENABLED = getBoolean(properties, "AltItemAuctionEnabled", false);
			ALT_ITEM_AUCTION_EXPIRED_AFTER = getInt(properties, "AltItemAuctionExpiredAfter", 14);
			ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = 1000 * getLong(properties, "AltItemAuctionTimeExtendsOnBid", 0);
			FS_TIME_ATTACK = getInt(properties, "TimeOfAttack", 50);
			FS_TIME_COOLDOWN = getInt(properties, "TimeOfCoolDown", 5);
			FS_TIME_ENTRY = getInt(properties, "TimeOfEntry", 3);
			FS_TIME_WARMUP = getInt(properties, "TimeOfWarmUp", 2);
			FS_PARTY_MEMBER_COUNT = getInt(properties, "NumberOfNecessaryPartyMembers", 4);
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
			RIFT_MIN_PARTY_SIZE = getInt(properties, "RiftMinPartySize", 5);
			RIFT_MAX_JUMPS = getInt(properties, "MaxRiftJumps", 4);
			RIFT_SPAWN_DELAY = getInt(properties, "RiftSpawnDelay", 10000);
			RIFT_AUTO_JUMPS_TIME_MIN = getInt(properties, "AutoJumpsDelayMin", 480);
			RIFT_AUTO_JUMPS_TIME_MAX = getInt(properties, "AutoJumpsDelayMax", 600);
			RIFT_BOSS_ROOM_TIME_MUTIPLY = getFloat(properties, "BossRoomTimeMultiply", 1.5F);
			RIFT_ENTER_COST_RECRUIT = getInt(properties, "RecruitCost", 18);
			RIFT_ENTER_COST_SOLDIER = getInt(properties, "SoldierCost", 21);
			RIFT_ENTER_COST_OFFICER = getInt(properties, "OfficerCost", 24);
			RIFT_ENTER_COST_CAPTAIN = getInt(properties, "CaptainCost", 27);
			RIFT_ENTER_COST_COMMANDER = getInt(properties, "CommanderCost", 30);
			RIFT_ENTER_COST_HERO = getInt(properties, "HeroCost", 33);
			DEFAULT_PUNISH = getInt(properties, "DefaultPunish", 2);
			DEFAULT_PUNISH_PARAM = getInt(properties, "DefaultPunishParam", 0);
			ONLY_GM_ITEMS_FREE = getBoolean(properties, "OnlyGMItemsFree", true);
			JAIL_IS_PVP = getBoolean(properties, "JailIsPvp", false);
			JAIL_DISABLE_CHAT = getBoolean(properties, "JailDisableChat", true);
			JAIL_DISABLE_TRANSACTION = getBoolean(properties, "JailDisableTransaction", false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
