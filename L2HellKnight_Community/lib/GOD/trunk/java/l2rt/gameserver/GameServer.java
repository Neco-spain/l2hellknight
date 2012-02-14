package l2rt.gameserver;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.Server;
import l2rt.common.ThreadPoolManager;
import l2rt.common.tasks.GarbageCollector;
import l2rt.database.*;
import l2rt.extensions.Stat;
import l2rt.extensions.network.MMOConnection;
import l2rt.extensions.network.MMOSocket;
import l2rt.extensions.network.SelectorConfig;
import l2rt.extensions.network.SelectorThread;
import l2rt.extensions.scripts.Events;
import l2rt.extensions.scripts.ScriptObject;
import l2rt.gameserver.cache.CrestCache;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.handler.UserCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.*;
import l2rt.gameserver.itemmall.ItemMall;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.model.AutoChatHandler;
import l2rt.gameserver.model.AutoSpawnHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.MonsterRace;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.entity.vehicle.L2VehicleManager;
import l2rt.gameserver.model.items.MailParcelController;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.L2GamePacketHandler;
import l2rt.gameserver.tables.*;
import l2rt.gameserver.taskmanager.ItemsAutoDestroy;
import l2rt.gameserver.taskmanager.MemoryWatchDog;
import l2rt.gameserver.taskmanager.TaskManager;
import l2rt.gameserver.webserver.WebServer;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.gameserver.xml.loader.XmlArmorsetLoader;
import l2rt.status.Status;
import l2rt.util.*;

import java.io.File;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

@SuppressWarnings({ "nls", "unqualified-field-access", "boxing" })
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());

	private final SelectorThread<L2GameClient> _selectorThreads[];
	public static GameServer gameServer;

	public static Status statusServer;

	public static Events events;

	public static FastMap<String, ScriptObject> scriptsObjects = new FastMap<String, ScriptObject>().setShared(true);

	private static int _serverStarted;

	protected static boolean serverLoaded = false;
	
	protected static final String LOG_FOLDER = "log"; // Name of folder for log file
	protected static final String LOG_NAME = "./config/log.ini"; // Name of log file
	protected static final String LOG_TEXT = "Telnet server is currently disabled.";
	protected static final String Lines = "=================================================";

	public SelectorThread<L2GameClient>[] getSelectorThreads()
	{
		return _selectorThreads;
	}

	public static int time()
	{
		return (int) (System.currentTimeMillis() / 1000);
	}

	public static int uptime()
	{
		return time() - _serverStarted;
	}

	@SuppressWarnings("unchecked")
	public GameServer() throws Exception
	{
		Server.gameServer = this;

		_serverStarted = time();
		_log.finest("used mem:" + MemoryWatchDog.getMemUsedMb());

		Strings.reload();

		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		ThreadPoolManager.getInstance();

		if(Config.DEADLOCKCHECK_INTERVAL > 0)
			new DeadlockDetector().start();

		if(Config.GARBAGE_COLLECTOR_INTERVAL > 0)
        {
            Class.forName(GarbageCollector.class.getName());
        }
		
		CrestCache.load();

		// start game time control early
		GameTimeController.getInstance();

		// keep the references of Singletons to prevent garbage collection
		CharNameTable.getInstance();

		// Базовые статы
		Formulas.GenerateBaseStats();
		
		AuctionManager.getInstance();

		ClanTable.getInstance();

		FakePlayersTable.getInstance();

		SkillTable.getInstance();

		PetSkillsTable.getInstance();

        ItemTemplates.getInstance();

		XmlArmorsetLoader.getInstance().LoadArmorSets();
		
		events = new Events();

		TradeController.getInstance();

		RecipeController.getInstance();

		SkillTreeTable.getInstance();
		SkillSpellbookTable.getInstance();
		CharTemplateTable.getInstance();

		NpcTable.getInstance();
		if(!NpcTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the npc table");
		}

		HennaTable _hennaTable = HennaTable.getInstance();
		if(!_hennaTable.isInitialized())
			throw new Exception("Could not initialize the Henna Table");
		HennaTreeTable.getInstance();
		if(!_hennaTable.isInitialized())
			throw new Exception("Could not initialize the Henna Tree Table");

		LevelUpTable.getInstance();
		
		TeleportLocationTable.getInstance();

		GeoEngine.loadGeo();

		DoorTable.getInstance();

		UnderGroundColliseumManager.getInstance();

		TownManager.getInstance();

		CastleManager.getInstance();
		CastleSiegeManager.load();

		FortressManager.getInstance();
		FortressSiegeManager.load();

		ClanHallManager.getInstance();
		ClanHallSiegeManager.load();

		TerritorySiege.load();

		CastleManorManager.getInstance();

		SpawnTable.getInstance();

		RaidBossSpawnManager.getInstance();

		InstancedZoneManager.getInstance();

		Announcements.getInstance();

		LotteryManager.getInstance();

		MapRegion.getInstance();

		AugmentationData.getInstance();

		PlayerMessageStack.getInstance();

		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();

		MonsterRace.getInstance();

		StaticObjectsTable.getInstance();

		AutoSpawnHandler _autoSpawnHandler = AutoSpawnHandler.getInstance();
		_log.config("AutoSpawnHandler: Loaded " + _autoSpawnHandler.size() + " handlers in total.");

		AutoChatHandler _autoChatHandler = AutoChatHandler.getInstance();
		_log.config("AutoChatHandler: Loaded " + _autoChatHandler.size() + " handlers in total.");

		if(Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}

		CursedWeaponsManager.getInstance();
		
		HellboundManager.getInstance(); 		

		if(!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.config("CoupleManager initialized");
		}

		ItemHandler _itemHandler = ItemHandler.getInstance();
		_log.config("ItemHandler: Loaded " + _itemHandler.size() + " handlers.");

		AdminCommandHandler _adminCommandHandler = AdminCommandHandler.getInstance();
		_log.config("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");

		UserCommandHandler _userCommandHandler = UserCommandHandler.getInstance();
		_log.config("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");

		VoicedCommandHandler _voicedCommandHandler = VoicedCommandHandler.getInstance();
		_log.config("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");

		TaskManager.getInstance();

		MercTicketManager.getInstance();

		L2VehicleManager.getInstance();
		AirShipDocksTable.getInstance();

		Shutdown _shutdownHandler = Shutdown.getInstance();
		Runtime.getRuntime().addShutdownHook(_shutdownHandler);

		try
		{
			// Colosseum doors
			DoorTable.getInstance().getDoor(24190001).openMe();
			DoorTable.getInstance().getDoor(24190002).openMe();
			DoorTable.getInstance().getDoor(24190003).openMe();
			DoorTable.getInstance().getDoor(24190004).openMe();

			// TOI doors
			DoorTable.getInstance().getDoor(23180001).openMe();
			DoorTable.getInstance().getDoor(23180002).openMe();
			DoorTable.getInstance().getDoor(23180003).openMe();
			DoorTable.getInstance().getDoor(23180004).openMe();
			DoorTable.getInstance().getDoor(23180005).openMe();
			DoorTable.getInstance().getDoor(23180006).openMe();

			// Эти двери, похоже выполняют декоративную функцию,
			// находятся во Frozen Labyrinth над мостом по пути к снежной королеве.
			DoorTable.getInstance().getDoor(23140001).openMe();
			DoorTable.getInstance().getDoor(23140002).openMe();

			DoorTable.getInstance().checkAutoOpen();
		}
		catch(NullPointerException e)
		{
			_log.warning("Doors table does not contain the right door info. Update doors.");
			e.printStackTrace();
		}

		_log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		TeleportTable.getInstance();

		PartyRoomManager.getInstance();
        ItemMall.getInstance();

		new File("./log/game").mkdirs();

		int restartTime = 0;
		int restartAt = 0;

		// Время запланированного на определенное время суток рестарта
		if(Config.RESTART_AT_TIME > -1)
		{
			Calendar calendarRestartAt = Calendar.getInstance();
			calendarRestartAt.set(Calendar.HOUR_OF_DAY, Config.RESTART_AT_TIME);
			calendarRestartAt.set(Calendar.MINUTE, 0);

			// Если запланированное время уже прошло, то берем +24 часа
			if(calendarRestartAt.getTimeInMillis() < System.currentTimeMillis())
				calendarRestartAt.add(Calendar.HOUR_OF_DAY, 24);

			restartAt = (int) (calendarRestartAt.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}

		// Время регулярного рестарта (через определенное время)
		restartTime = Config.RESTART_TIME * 60 * 60;

		// Проверяем какой рестарт раньше, регулярный или запланированный
		if(restartTime < restartAt && restartTime > 0 || restartTime > restartAt && restartAt == 0)
			Shutdown.getInstance().setAutoRestart(restartTime);
		else if(restartAt > 0)
			Shutdown.getInstance().setAutoRestart(restartAt);

		MailParcelController.getInstance();

		L2TopManager.getInstance();
		
		AwakingManager.getInstance();
		JumpManager.getInstance().load();

		_log.info("GameServer Started");
		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

		ccpGuard.Protection.Init();		
		Stat.init();

		if(Config.RRD_ENABLED)
			RRDTools.init();

		if(Config.PROTECT_ENABLE && Config.PROTECT_GS_ENABLE_HWID_BANS)
			HWID.reloadBannedHWIDs();

		if(Config.PROTECT_ENABLE && Config.PROTECT_GS_ENABLE_HWID_BONUS)
			HWID.reloadBonusHWIDs();

		MMOSocket.getInstance();
		LSConnection.getInstance().start();

		SelectorThread.setAntiFlood(Config.ANTIFLOOD_ENABLE);
		SelectorThread.setAntiFloodSocketsConf(Config.MAX_UNHANDLED_SOCKETS_PER_IP, Config.UNHANDLED_SOCKET_MIN_TTL);
		L2GamePacketHandler gph = new L2GamePacketHandler();
		SelectorConfig<L2GameClient> sc = new SelectorConfig<L2GameClient>(gph);
		sc.setMaxSendPerPass(30);
		sc.setSelectorSleepTime(1);
		SelectorThread.setGlobalReadLock(Config.PORTS_GAME.length > 1);
		_selectorThreads = new SelectorThread[Config.PORTS_GAME.length];
		for(int i = 0; i < Config.PORTS_GAME.length; i++)
		{
			_selectorThreads[i] = new SelectorThread<L2GameClient>(sc, gph, gph, gph, null);
			_selectorThreads[i].openServerSocket(null, Config.PORTS_GAME[i]);
			_selectorThreads[i].start();
		}

		if(Config.WEB_SERVER_DELAY > 0)
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WebServer(), 5000, Config.WEB_SERVER_DELAY);

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
			// Это довольно тяжелая задача поэтому пусть идет отдельным тридом
			new Thread(new Runnable(){
				@Override
				public void run()
				{
					if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
					{
						int min_offline_restore = (int) (System.currentTimeMillis() / 1000 - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);
						mysql.set("DELETE FROM character_variables WHERE `name` = 'offline' AND `value` < " + min_offline_restore);
					}
					mysql.set("DELETE FROM character_variables WHERE `name` = 'offline' AND `obj_id` IN (SELECT `obj_id` FROM `characters` WHERE `accessLevel` < 0)");

					ThreadConnection con = null;
					FiltredPreparedStatement st = null;
					ResultSet rs = null;

					try
					{
						GArray<Object> logins = mysql.get_array(L2DatabaseFactory.getInstanceLogin(), "SELECT `login` FROM `accounts` WHERE `access_level` < 0");
						if(logins.size() > 0)
						{
							con = L2DatabaseFactory.getInstance().getConnection();
							st = con.prepareStatement("DELETE FROM character_variables WHERE `name` = 'offline' AND `obj_id` IN (SELECT `obj_id` FROM `characters` WHERE `account_name`=?)");
							for(Object login : logins)
							{
								st.setString(1, (String) login);
								st.executeUpdate();
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						DatabaseUtils.closeDatabaseCSR(con, st, rs);
					}

					GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'offline'");
					for(HashMap<String, Object> e : list)
					{
						L2GameClient client = new L2GameClient(new MMOConnection<L2GameClient>(null), true);
						client.setCharSelection((Integer) e.get("obj_id"));
						L2Player p = client.loadCharFromDisk(0);
						if(p == null || p.isDead())
							continue;
						client.setLoginName((String) e.get("account_name") == null ? "OfflineTrader_" + p.getName() : (String) e.get("account_name"));
						client.OnOfflineTrade();
						p.restoreBonus();
						p.spawnMe();
						p.updateTerritories();
						p.setOnlineStatus(true);
						p.setOfflineMode(true);
						p.setConnected(false);
						p.setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
						p.restoreEffects();
						p.restoreDisableSkills();
						p.broadcastUserInfo(true);
						if(p.getClan() != null && p.getClan().getClanMember(p.getObjectId()) != null)
							p.getClan().getClanMember(p.getObjectId()).setPlayerInstance(p);
						if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
							p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + Integer.parseInt(e.get("value").toString())) * 1000L - System.currentTimeMillis());
					}
					_log.info("Restored " + list.size() + " offline traders");
				}
			}).start();
	}

	public static boolean isLoaded()
	{
		return serverLoaded;
	}
}