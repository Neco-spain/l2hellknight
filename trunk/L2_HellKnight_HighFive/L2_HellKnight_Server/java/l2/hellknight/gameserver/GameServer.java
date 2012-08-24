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

package l2.hellknight.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import l2.hellknight.Config;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.Server;
import l2.hellknight.gameserver.cache.CrestCache;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.datatables.AccessLevels;
import l2.hellknight.gameserver.datatables.AdminCommandAccessRights;
import l2.hellknight.gameserver.datatables.ArmorSetsTable;
import l2.hellknight.gameserver.datatables.AugmentationData;
import l2.hellknight.gameserver.datatables.CharNameTable;
import l2.hellknight.gameserver.datatables.CharSummonTable;
import l2.hellknight.gameserver.datatables.CharTemplateTable;
import l2.hellknight.gameserver.datatables.ClanTable;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.EnchantGroupsTable;
import l2.hellknight.gameserver.datatables.EnchantHPBonusData;
import l2.hellknight.gameserver.datatables.EventDroplist;
import l2.hellknight.gameserver.datatables.ExperienceTable;
import l2.hellknight.gameserver.datatables.FishTable;
import l2.hellknight.gameserver.datatables.GMSkillTable;
import l2.hellknight.gameserver.datatables.HelperBuffTable;
import l2.hellknight.gameserver.datatables.HennaTable;
import l2.hellknight.gameserver.datatables.HennaTreeTable;
import l2.hellknight.gameserver.datatables.HerbDropTable;
import l2.hellknight.gameserver.datatables.HeroSkillTable;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.LevelUpData;
import l2.hellknight.gameserver.datatables.MerchantPriceConfigTable;
import l2.hellknight.gameserver.datatables.MultiSell;
import l2.hellknight.gameserver.datatables.NobleSkillTable;
import l2.hellknight.gameserver.datatables.NpcBufferTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.NpcWalkerRoutesTable;
import l2.hellknight.gameserver.datatables.OfflineTradersTable;
import l2.hellknight.gameserver.datatables.PetDataTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SkillTreesData;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.datatables.StaticObjects;
import l2.hellknight.gameserver.datatables.SummonItemsData;
import l2.hellknight.gameserver.datatables.SummonSkillsTable;
import l2.hellknight.gameserver.datatables.TeleportLocationTable;
import l2.hellknight.gameserver.datatables.UITable;
import l2.hellknight.gameserver.geoeditorcon.GeoEditorListener;
import l2.hellknight.gameserver.handler.EffectHandler;
import l2.hellknight.gameserver.idfactory.IdFactory;
import l2.hellknight.gameserver.instancemanager.AirShipManager;
import l2.hellknight.gameserver.instancemanager.AntiFeedManager;
import l2.hellknight.gameserver.instancemanager.AuctionManager;
import l2.hellknight.gameserver.instancemanager.AutoVoteRewardManager;
import l2.hellknight.gameserver.instancemanager.BoatManager;
import l2.hellknight.gameserver.instancemanager.CHSiegeManager;
import l2.hellknight.gameserver.instancemanager.CastleManager;
import l2.hellknight.gameserver.instancemanager.CastleManorManager;
import l2.hellknight.gameserver.instancemanager.ClanHallManager;
import l2.hellknight.gameserver.instancemanager.CoupleManager;
import l2.hellknight.gameserver.instancemanager.CursedWeaponsManager;
import l2.hellknight.gameserver.instancemanager.DayNightSpawnManager;
import l2.hellknight.gameserver.instancemanager.DimensionalRiftManager;
import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.instancemanager.FortSiegeManager;
import l2.hellknight.gameserver.instancemanager.FourSepulchersManager;
import l2.hellknight.gameserver.instancemanager.GlobalVariablesManager;
import l2.hellknight.gameserver.instancemanager.GraciaSeedsManager;
import l2.hellknight.gameserver.instancemanager.GrandBossManager;
import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.ItemAuctionManager;
import l2.hellknight.gameserver.instancemanager.ItemsOnGroundManager;
import l2.hellknight.gameserver.instancemanager.MailManager;
import l2.hellknight.gameserver.instancemanager.MapRegionManager;
import l2.hellknight.gameserver.instancemanager.MercTicketManager;
import l2.hellknight.gameserver.instancemanager.PcCafePointsManager;
import l2.hellknight.gameserver.instancemanager.PetitionManager;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.instancemanager.RaidBossPointsManager;
import l2.hellknight.gameserver.instancemanager.RaidBossSpawnManager;
import l2.hellknight.gameserver.instancemanager.SiegeManager;
import l2.hellknight.gameserver.instancemanager.TerritoryWarManager;
import l2.hellknight.gameserver.instancemanager.TransformationManager;
import l2.hellknight.gameserver.instancemanager.WalkingManager;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.instancemanager.leaderboards.ArenaLeaderboard;
import l2.hellknight.gameserver.instancemanager.leaderboards.CraftLeaderboard;
import l2.hellknight.gameserver.instancemanager.leaderboards.FishermanLeaderboard;
import l2.hellknight.gameserver.instancemanager.leaderboards.TvTLeaderboard;
import l2.hellknight.gameserver.model.AutoChatHandler;
import l2.hellknight.gameserver.model.AutoSpawnHandler;
import l2.hellknight.gameserver.model.L2Manor;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.PartyMatchRoomList;
import l2.hellknight.gameserver.model.PartyMatchWaitingList;
import l2.hellknight.gameserver.model.entity.Hero;
import l2.hellknight.gameserver.model.entity.TvTManager;
import l2.hellknight.gameserver.model.entity.TvTRoundManager;
import l2.hellknight.gameserver.model.olympiad.Olympiad;
import l2.hellknight.gameserver.network.L2GameClient;
import l2.hellknight.gameserver.network.L2GamePacketHandler;
import l2.hellknight.gameserver.network.communityserver.CommunityServerThread;
import l2.hellknight.gameserver.pathfinding.PathFinding;
import l2.hellknight.gameserver.script.faenor.FaenorScriptEngine;
import l2.hellknight.gameserver.scripting.CompiledScriptCache;
import l2.hellknight.gameserver.scripting.L2ScriptEngineManager;
import l2.hellknight.gameserver.taskmanager.AutoAnnounceTaskManager;
import l2.hellknight.gameserver.taskmanager.KnownListUpdateTaskManager;
import l2.hellknight.gameserver.taskmanager.TaskManager;
import l2.hellknight.gameserver.util.L2HellKnight;
import l2.hellknight.status.Status;
import l2.hellknight.util.DeadLockDetector;
import l2.hellknight.util.IPv4Filter;

/**
 * This class ...
 * 
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		
		L2HellKnight.info();
		
		_log.finest("used mem:" + getUsedMemoryMB() + "MB");
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("World");
		// start game time control early
		GameTimeController.getInstance();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantGroupsTable.getInstance();
		SkillTable.getInstance();
		SkillTreesData.getInstance();
		NobleSkillTable.getInstance();
		GMSkillTable.getInstance();
		HeroSkillTable.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("Items");
		ItemTable.getInstance();
		SummonItemsData.getInstance();
		EnchantHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		TradeController.getInstance();
		MultiSell.getInstance();
		RecipeController.getInstance();
		ArmorSetsTable.getInstance();
		FishTable.getInstance();
		
		printSection("Characters");
		ExperienceTable.getInstance();
		CharTemplateTable.getInstance();
		CharNameTable.getInstance();
		LevelUpData.getInstance();
		AccessLevels.getInstance();
		AdminCommandAccessRights.getInstance();
		GmListTable.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance().init();
		
		printSection("Clans"); 
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
			PathFinding.getInstance();
		
		printSection("NPCs");
		HerbDropTable.getInstance();
		NpcTable.getInstance();
		NpcWalkerRoutesTable.getInstance();
		WalkingManager.getInstance();
		ZoneManager.getInstance();
		DoorTable.getInstance();
		StaticObjects.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		SpawnTable.getInstance();
		HellboundManager.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		PcCafePointsManager.getInstance();
		L2Manor.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestCache.getInstance();
		TeleportLocationTable.getInstance();
		UITable.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		HennaTable.getInstance();
		HennaTreeTable.getInstance();
		HelperBuffTable.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		
		printSection("Scripts");
		QuestManager.getInstance();
		TransformationManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		try
		{
			_log.info("Loading Server Scripts");
			File scripts = new File(Config.DATAPACK_ROOT, "data/scripts.cfg");
			if(!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
		}
		catch (IOException ioe)
		{
			_log.severe("Failed loading scripts.cfg, no script going to be loaded");
		}
		try
		{
			CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
			if (compiledScriptCache == null)
			{
				_log.info("Compiled Scripts Cache is disabled.");
			}
			else
			{
				compiledScriptCache.purge();
				
				if (compiledScriptCache.isModified())
				{
					compiledScriptCache.save();
					_log.info("Compiled Scripts Cache was saved.");
				}
				else
				{
					_log.info("Compiled Scripts Cache is up-to-date.");
				}
			}
			
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "Failed to store Compiled Scripts Cache.", e);
		}
		QuestManager.getInstance().report();
		TransformationManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
			ItemsOnGroundManager.getInstance();
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
			ItemsAutoDestroy.getInstance();
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		AutoChatHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoChatHandler: Loaded " + AutoChatHandler.getInstance().size() + " handlers in total.");
		_log.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		
		if (Config.L2JMOD_ALLOW_WEDDING)
			CoupleManager.getInstance();
		
		if (Config.RANK_ARENA_ENABLED)
			ArenaLeaderboard.getInstance();
		
		if (Config.RANK_FISHERMAN_ENABLED)
			FishermanLeaderboard.getInstance();
		
		if (Config.RANK_CRAFT_ENABLED)
			CraftLeaderboard.getInstance();
		
		if (Config.RANK_TVT_ENABLED)
			TvTLeaderboard.getInstance();

		if (Config.L2JMOD_VOTE_ENGINE_ENABLE)
		    AutoVoteRewardManager.getInstance();			
		
		TaskManager.getInstance();

		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		MerchantPriceConfigTable.getInstance().updateReferences();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		
		if (Config.ALLOW_MAIL)
			MailManager.getInstance();
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
			GeoEditorListener.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		TvTManager.getInstance();
		TvTRoundManager.getInstance();
		KnownListUpdateTaskManager.getInstance();
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
			OfflineTradersTable.restoreOfflineTraders();
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
			_deadDetectThread = null;
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the
		// allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info("GameServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		CommunityServerThread.initialize();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<L2GameClient>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, "WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server Loaded in " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds");
		
		AutoAnnounceTaskManager.getInstance();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is = new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
		// Initialize config
		Config.load();
		printSection("Database");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
			s = "-" + s;
		_log.info(s);
	}
}
