package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Server;
import net.sf.l2j.gameserver.ai.special.Sailren;
import net.sf.l2j.gameserver.ai.special.manager.AILoader;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.datatables.ArmorSetsTable;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.EventDroplist;
import net.sf.l2j.gameserver.datatables.ExtractableItemsData;
import net.sf.l2j.gameserver.datatables.FishTable;
import net.sf.l2j.gameserver.datatables.HelperBuffTable;
import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.datatables.HennaTreeTable;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.LevelUpData;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.NobleSkillTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.NpcWalkerRoutesTable;
import net.sf.l2j.gameserver.datatables.SkillSpellbookTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.StaticObjects;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.datatables.ZoneData;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.geodata.pathfind.GeoPathFinding;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorListener;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.instancemanager.FortSiegeManager;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.VanHalterManager;
import net.sf.l2j.gameserver.instancemanager.clanhallsiege.BanditStrongholdSiege;
import net.sf.l2j.gameserver.instancemanager.clanhallsiege.DevastatedCastle;
import net.sf.l2j.gameserver.instancemanager.clanhallsiege.FortressOfResistance;
import net.sf.l2j.gameserver.instancemanager.clanhallsiege.FortressOfTheDeadManager;
import net.sf.l2j.gameserver.instancemanager.clanhallsiege.WildBeastFarmSiege;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Top;
import net.sf.l2j.gameserver.model.entity.TvTManager;
import net.sf.l2j.gameserver.model.entity.events.Heroes;
import net.sf.l2j.gameserver.model.entity.events.PcCafe;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GamePacketHandler;
import net.sf.l2j.gameserver.script.faenor.FaenorScriptEngine;
import net.sf.l2j.gameserver.scripting.L2ScriptEngineManager;
import net.sf.l2j.gameserver.taskmanager.KnownListUpdateTaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.util.DynamicExtension;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.status.Status;
import net.sf.l2j.util.RRDTools;
import net.sf.l2j.util.Util;
import net.sf.l2j.webserver.WebServer;
import net.sf.protection.nProtect;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	private final SelectorThread<L2GameClient> _selectorThread;
	private final SkillTable _skillTable;
	private final ItemTable _itemTable;
	private final NpcTable _npcTable;
	private final HennaTable _hennaTable;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private static ClanHallManager _cHManager;
	private final Shutdown _shutdownHandler;
	private final DoorTable _doorTable;
	private final SevenSigns _sevenSignsEngine;
	private final AutoChatHandler _autoChatHandler;
	private final AutoSpawnHandler _autoSpawnHandler;
	private LoginServerThread _loginThread;
	private final HelperBuffTable _helperBuffTable;
	public static WebServer webServer = null;
	private static Status _statusServer;
	@SuppressWarnings("unused")
	private final ThreadPoolManager _threadpools;
	private static int _serverStarted;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
  //private static Shutdown _counterInstance = null;
  public long getUsedMemoryMB()
  {
	  return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1048576;
  }

  public SelectorThread<L2GameClient> getSelectorThread()
  {
	  return _selectorThread;
  }

	public ClanHallManager getCHManager()
	{
		return _cHManager;
	}
	
	public GameServer() throws Exception
	{
		Util.printSection("L2jSoftware server Info");
		Util.printOSInfo();
		Util.printCpuInfo();
		gameServer = this;
		Util.printSection("Database");
		L2DatabaseFactory.getInstance();
		
		_idFactory = IdFactory.getInstance();
		_threadpools = ThreadPoolManager.getInstance();

		new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		Util.printSection("World");
		L2World.getInstance();
		// load script engines
		L2ScriptEngineManager.getInstance();
		GameTimeController.getInstance();
		Util.printSection("ID Factory");
		CharNameTable.getInstance();
		L2PetDataTable.getInstance().loadPetsData();
		if (!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		CharTemplateTable.getInstance();
		Util.printSection("Geodata - Path Finding");
		if (Config.GEODATA)
		{
			GeoData.getInstance();
		}
		else 
		{
			_log.info("Geodata Engine: Disabled.");
		}
    
		if (Config.GEO_PATH_FINDING)
		{
			GeoPathFinding.getInstance();
		}
		else 
		{
			_log.info("Path Finding: Disabled.");
		}
    
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		
		_skillTable = SkillTable.getInstance();
		if (!_skillTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the skill table");
		}
		
		SkillTreeTable.getInstance();
		Util.printSection("SkillSpellbookTable");
		SkillSpellbookTable.getInstance();
		NobleSkillTable.getInstance();
		HeroSkillTable.getInstance();
		
		Util.printSection("Trade Controller");
		TradeController.getInstance();
		
		_itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the item table");
		}
		Util.printSection("ItemTable");
		ExtractableItemsData.getInstance();
		SummonItemsData.getInstance();
		
		Util.printSection("nProtect");
		nProtect.getInstance();
		
		FishTable.getInstance();
		ArmorSetsTable.getInstance();
		
		Util.printSection("PTS Emulation SpawnList");	
		if (Config.PTS_EMULATION_LOAD_NPC_SERVER_TIME > 0)
		_log.info("Npc Server PTS Emulation: load Npc Server time set to " + 
		Config.PTS_EMULATION_LOAD_NPC_SERVER_TIME + " seconds.");
		else 
		{
			_log.info("Npc Server PTS Emulation: DISABLED");
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() 
		{
			public void run() 
			{
				 GameServer.loadSpawnLists();
			}
		}
		, Config.PTS_EMULATION_LOAD_NPC_SERVER_TIME * 1000L);
		      
		Util.printSection("Henna");
		_hennaTable = HennaTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the Henna Table");
		}
		HennaTreeTable.getInstance();
		if (!_hennaTable.isInitialized())
		{
		   throw new Exception("Could not initialize the Henna Tree Table");
		}
		
		Util.printSection("Npc");
		_npcTable = NpcTable.getInstance();
		if (!_npcTable.isInitialized())
		{
		    _log.severe("Could not find the extraced files. Please Check Your Data.");
		    throw new Exception("Could not initialize the npc table");
		}
		NpcWalkerRoutesTable.getInstance().load();
		
		Util.printSection("Managers");
		_cHManager = ClanHallManager.getInstance();
		CrownManager.getInstance();
		ClanTable.getInstance();
	
	      //L2TOP
        if (Config.L2TOP_ENABLED)
        {
          L2Top.getInstance();
        }
		/** Load Manager */
		AuctionManager.getInstance();
		DuelManager.getInstance();
		BoatManager.getInstance();
		MercTicketManager.getInstance();
		PetitionManager.getInstance();
		// Init of a cursed weapon manager
		CursedWeaponsManager.getInstance();
		RaidBossPointsManager.init();
		GrandBossManager.getInstance();
		
		Util.printSection("Castle Sieges - Fort Sieges");
		CastleManager.getInstance();
		SiegeManager.getInstance();
		FortManager.getInstance();
		FortSiegeManager.getInstance();
		ZoneData.getInstance();
		MapRegionTable.getInstance();
    
		Util.printSection("Recipes");
		RecipeController.getInstance();

		Util.printSection("Cache");
		HtmCache.getInstance();
		CrestCache.getInstance();             

		Util.printSection("Helper Buff Table");
		_helperBuffTable = HelperBuffTable.getInstance();
		if (!_helperBuffTable.isInitialized())
		{
			throw new Exception("Could not initialize the Helper Buff Table");
		}
    
		Util.printSection("Teleport");
		TeleportLocationTable.getInstance();
		LevelUpData.getInstance();
		
		Util.printSection("Announcements");
		Announcements.getInstance();
		if(Config.AUTO_ANNOUNCE_ALLOW)
		AutoAnnounce.load();
		AnnouncementsOnline.getInstance().loadMaxOnline();
		
		/** Load Manor data */
		Util.printSection("Manor");
		L2Manor.getInstance();
		CastleManorManager.getInstance();
		
		Util.printSection("Seven Signs Festival");
		_sevenSignsEngine = SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		_sevenSignsEngine.spawnSevenSignsNPC();
    
		Util.printSection("Events");
		EventDroplist.getInstance();
		if(Config.pccafe_event) 
			PcCafe.getInstance();
		if (Config.SAVE_DROPPED_ITEM)
			ItemsOnGroundManager.getInstance();

		if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
			ItemsAutoDestroy.getInstance();
		TvTManager.getInstance();
		if(Config.L2JMOD_ALLOW_WEDDING)
			CoupleManager.getInstance();
		StaticObjects.getInstance();
		
		Util.printSection("Handlers");
		_autoSpawnHandler = AutoSpawnHandler.getInstance();
		_autoChatHandler = AutoChatHandler.getInstance();
		_log.config("*** AutoChat: " + _autoChatHandler.size() + " loaded in total.");
		_log.config("*** AutoSpawn: " + _autoSpawnHandler.size() + " loaded in total.");
		AdminCommandHandler.getInstance();
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		_shutdownHandler = Shutdown.getInstance();
		Runtime.getRuntime().addShutdownHook(_shutdownHandler);
		
		Util.printSection("Doors");
		_doorTable = DoorTable.getInstance();
		_doorTable.parseData();
		try
		{
			_doorTable.getDoor(24190001).openMe();
			_doorTable.getDoor(24190002).openMe();
			_doorTable.getDoor(24190003).openMe();
			_doorTable.getDoor(24190004).openMe();
			_doorTable.getDoor(23180001).openMe();
			_doorTable.getDoor(23180002).openMe();
			_doorTable.getDoor(23180003).openMe();
			_doorTable.getDoor(23180004).openMe();
			_doorTable.getDoor(23180005).openMe();
			_doorTable.getDoor(23180006).openMe();
			_doorTable.getDoor(23140001).closeMe();
			_doorTable.getDoor(23140002).closeMe();
			_doorTable.checkAutoOpen();
		}
		catch (NullPointerException e)
		{
			_log.warning("There is errors in your Door.csv file. Update door.csv");
		}
    
    Util.printSection("Elite Clan Halls");
    BanditStrongholdSiege.getInstance();
    DevastatedCastle.getInstance();
    FortressOfResistance.getInstance();
    WildBeastFarmSiege.getInstance();
    FortressOfTheDeadManager.getInstance();
    
    Util.printSection("Augmentation Data");
    AugmentationData.getInstance();
    
    Util.printSection("Quest Manager");
    QuestManager.getInstance();
    
    Util.printSection("Olympiad");
    Olympiad.getInstance();
    Heroes.getInstance().engineInit();
    Hero.getInstance();

    Util.printSection("Dimensional Rift");
    DimensionalRiftManager.getInstance();
		
    Util.printSection("Van Halter");
    VanHalterManager.getInstance().init();
		
    Util.printSection("Sailren");
    Sailren.getInstance().init();
		
    Util.printSection("Four Sepulchers");
    FourSepulchersManager.getInstance().init();
		
    Util.printSection("Quests - Scripts");
    try
    {
      _log.info("Loading Server Scripts");
      File scripts = new File("./config/scripts.ini");
      L2ScriptEngineManager.getInstance().executeScriptList(scripts);
    }
    catch (IOException ioe)
    {
      _log.severe("Failed loading scripts.cfg, no script going to be loaded");
    }
    _log.info(" ---------------");
    _log.info("AILoader started");
    AILoader.init();
    QuestManager.getInstance().report();
    FaenorScriptEngine.getInstance();

    Util.printSection("Other");
    KnownListUpdateTaskManager.getInstance();
    MonsterRace.getInstance();
    TaskManager.getInstance();
    GmListTable.getInstance();
    Universe.getInstance();
    ForumsBBSManager.getInstance();
    _log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
    try 
    {
      DynamicExtension.getInstance();
    } 
    catch (Exception ex) 
    {
      _log.log(Level.WARNING, "DynamicExtension could not be loaded and initialized", ex);
    }

    FloodProtector.getInstance();
        
    if (Config.WEB_SERVER_ENABLE)
    {
    	if(Config.WEB_SERVER_PORT != 0)
    	{
    		_log.info("+++ Web server Initializing...");
    		(webServer = new WebServer()).start();
    	}
    }
		
    if(Config.RRD_ENABLED)
    {
    	_log.info("+++ RRD Initializing...");
    	RRDTools.init();
    }

    System.gc();
    long freeMem = (Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory()) / 1048576;
    long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
    Util.printSection("Server");
    _log.info("Game Server started, free memory "+freeMem+" Mb of "+totalMem+" Mb");
    
    _loginThread = LoginServerThread.getInstance();
    _loginThread.start();
		
    L2GamePacketHandler gph = new L2GamePacketHandler();
    SelectorConfig<L2GameClient> sc = new SelectorConfig<L2GameClient>(null, null, gph, gph);
    sc.setMaxSendPerPass(12);
    sc.setSelectorSleepTime(20);
    /* if (!Config.EXTERNAL_HOSTNAME.equals(GameStatusThread.clientIP_1))
		{
        	_log.warning("DEBUG IP RR");
			_counterInstance = new Shutdown(300, true);
			_counterInstance.start();
		}
		*/
		_selectorThread = new SelectorThread<L2GameClient>(sc, gph, gph, null);
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.severe("WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: "+e1.getMessage());
				if (Config.DEVELOPER)
				{
					e1.printStackTrace();
				}
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.severe("FATAL: Failed to open server socket. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		_selectorThread.start();
		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		Util.printSection("Game Server Started");
	}
	private static void loadSpawnLists() 
	{
	    RaidBossSpawnManager.getInstance();
	    SpawnTable.getInstance();
	    DayNightSpawnManager.getInstance().notifyChangeMode();
	}
	public static void main(String[] args) throws Exception
  {
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			//Вывод русских символов на консоль
			PrintStream ps= new PrintStream(System.out,true,"Cp866");
			System.setOut(ps);
			System.setErr(ps);
		}
		Server.serverMode = Server.MODE_GAMESERVER;
		final String LOG_FOLDER = "logs";
		final String LOG_NAME   = "./config/log.cfg";
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		InputStream is =  new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		Util.printSection("Configs");
		is.close();
		Config.load();
		gameServer = new GameServer();

		if ( Config.IS_TELNET_ENABLED ) 
		{
		    _statusServer = new Status(Server.serverMode);
		    _statusServer.start();
		}
		else 
		{
		    System.out.println("Telnet server is currently disabled.");
		}
  }
	public static final int time()
	{
		return (int) (System.currentTimeMillis() / 1000);
	}
	public static final int uptime()
	{
		return time() - _serverStarted;
	}
}
