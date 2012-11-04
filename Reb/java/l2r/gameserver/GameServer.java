package l2r.gameserver;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;

import l2r.commons.lang.StatsUtils;
import l2r.commons.listener.Listener;
import l2r.commons.listener.ListenerList;
import l2r.commons.net.nio.impl.SelectorThread;
import l2r.commons.versioning.Version;
import l2r.gameserver.cache.CrestCache;
import l2r.gameserver.dao.CharacterDAO;
import l2r.gameserver.dao.ItemsDAO;
import l2r.gameserver.data.BoatHolder;
import l2r.gameserver.data.xml.Parsers;
import l2r.gameserver.data.xml.holder.EventHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.data.xml.holder.StaticObjectHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.items.ItemHandler;
import l2r.gameserver.handler.usercommands.UserCommandHandler;
import l2r.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.AutoAnnounce;
import l2r.gameserver.instancemanager.AutoHuntingManager;
import l2r.gameserver.instancemanager.AutoSpawnManager;
import l2r.gameserver.instancemanager.BloodAltarManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.instancemanager.CoupleManager;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.instancemanager.L2TopManager;
import l2r.gameserver.instancemanager.MMOTopManager;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.instancemanager.PlayerMessageStack;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.instancemanager.SMSWayToPay;
import l2r.gameserver.instancemanager.SoDManager;
import l2r.gameserver.instancemanager.SoIManager;
import l2r.gameserver.instancemanager.SpawnManager;
import l2r.gameserver.instancemanager.games.FishingChampionShipManager;
import l2r.gameserver.instancemanager.games.LotteryManager;
import l2r.gameserver.instancemanager.games.MiniGameScoreManager;
import l2r.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2r.gameserver.instancemanager.naia.NaiaCoreManager;
import l2r.gameserver.instancemanager.naia.NaiaTowerManager;
import l2r.gameserver.listener.GameListener;
import l2r.gameserver.listener.game.OnShutdownListener;
import l2r.gameserver.listener.game.OnStartListener;
import l2r.gameserver.model.World;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.MonsterRace;
import l2r.gameserver.model.entity.SevenSigns;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.GamePacketHandler;
import l2r.gameserver.network.loginservercon.AuthServerCommunication;
import l2r.gameserver.network.telnet.TelnetServer;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.tables.AugmentationData;
import l2r.gameserver.tables.ClanTable;
import l2r.gameserver.tables.EnchantHPBonusTable;
import l2r.gameserver.tables.FishTable;
import l2r.gameserver.tables.LevelUpTable;
import l2r.gameserver.tables.PetSkillsTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.taskmanager.ItemsAutoDestroy;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import l2r.gameserver.utils.RebellionTeam;
import l2r.gameserver.utils.Strings;
import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer
{
	public static final int AUTH_SERVER_PROTOCOL = 2;
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnStartListener.class.isInstance(listener))
					((OnStartListener) listener).onStart();
		}

		public void onShutdown()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown();
		}
	}

	public static GameServer _instance;

	private final SelectorThread<GameClient> _selectorThreads[];
	private TelnetServer statusServer;
	private Version version;
	private final GameServerListenerList _listeners;

	private int _serverStarted;

	public SelectorThread<GameClient>[] getSelectorThreads()
	{
		return _selectorThreads;
	}

	public int time()
	{
		return (int) (System.currentTimeMillis() / 1000);
	}

	public int uptime()
	{
		return time() - _serverStarted;
	}
	private void showLogo() 
	{
		System.out.println("                                                                                ");
		System.out.println("================================================================================");
		System.out.println("           ######                                                               ");
		System.out.println("           #     # ###### #####  ###### #      #      #  ####  #    #           ");
		System.out.println("           #     # #      #    # #      #      #      # #    # ##   #           ");
		System.out.println("           ######  #####  #####  #####  #      #      # #    # # #  #           ");
		System.out.println("           #   #   #      #    # #      #      #      # #    # #  # #           ");
		System.out.println("           #    #  #      #    # #      #      #      # #    # #   ##           ");
		System.out.println("           #     # ###### #####  ###### ###### ###### #  ####  #    #           ");
		System.out.println("                                                                                ");
		System.out.println("================================================================================");
		System.out.println("                                                                                ");
	}
	@SuppressWarnings("unchecked")
	public GameServer() throws Exception
	{
		_instance = this;
		_serverStarted = time();
		_listeners = new GameServerListenerList();

		new File("./log/").mkdir();

		version = new Version(GameServer.class);

		_log.info("=================================================");
		_log.info("Copyright: ............... " + "Rebellion-team");
		_log.info("Chronicle: ............... " + "High Five");
		_log.info("Revision: ................ " + version.getRevisionNumber());
		_log.info("Build date: .............. " + version.getBuildDate());
//		_log.info("Builder name: ............ " + version.getBuilderName());
//		_log.info("Compiler version: ........ " + Version.getBuildJdk());
		_log.info("=================================================");
		showLogo();
		// Initialize config
		Config.load();
		// Check binding address
		checkFreePorts();
		// Initialize database
		Class.forName(Config.DATABASE_DRIVER).newInstance();
		DatabaseFactory.getInstance().getConnection().close();

		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		CacheManager.getInstance();

		ThreadPoolManager.getInstance();
		Scripts.getInstance();
		GeoEngine.load();
		Strings.reload();
		GameTimeController.getInstance();
		World.init();
		Parsers.parseAll();
		ItemsDAO.getInstance();
		CrestCache.getInstance();
		CharacterDAO.getInstance();
		ClanTable.getInstance();
		FishTable.getInstance();
		SkillTreeTable.getInstance();
		AugmentationData.getInstance();
		EnchantHPBonusTable.getInstance();
		LevelUpTable.getInstance();
		PetSkillsTable.getInstance();
		ItemAuctionManager.getInstance();
		Scripts.getInstance().init();
		SpawnManager.getInstance().spawnAll();
		BoatHolder.getInstance().spawnAll();
		StaticObjectHolder.getInstance().spawnAll();
		RaidBossSpawnManager.getInstance();
		DimensionalRiftManager.getInstance();
		Announcements.getInstance();
		LotteryManager.getInstance();
		PlayerMessageStack.getInstance();
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();
		MonsterRace.getInstance();
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		SevenSigns.getInstance().updateFestivalScore();
		AutoSpawnManager.getInstance();
		SevenSigns.getInstance().spawnSevenSignsNPC();
		if(Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		if(!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.info("CoupleManager initialized");
		}
		ItemHandler.getInstance();
		AdminCommandHandler.getInstance().log();
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();
		TaskManager.getInstance();
		AutoHuntingManager.getInstance();
		_log.info("===================[Events]=======================");
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		_log.info("==================================================");

		CastleManorManager.getInstance();

		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		CoupleManager.getInstance();

		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionShipManager.getInstance();

		HellboundManager.getInstance();

		NaiaTowerManager.getInstance();
		NaiaCoreManager.getInstance();

		SoDManager.getInstance();
		SoIManager.getInstance();
		BloodAltarManager.getInstance();

		MiniGameScoreManager.getInstance();

		L2TopManager.getInstance();

		MMOTopManager.getInstance();

		SMSWayToPay.getInstance();
		
		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.RESTART);
		_log.info("GameServer Started");
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

		GamePacketHandler gph = new GamePacketHandler();

		InetAddress serverAddr = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.GAMESERVER_HOSTNAME);

		_selectorThreads = new SelectorThread[Config.PORTS_GAME.length];
		for(int i = 0; i < Config.PORTS_GAME.length; i++)
		{
			_selectorThreads[i] = new SelectorThread<GameClient>(Config.SELECTOR_CONFIG, gph, gph, gph, null);
			_selectorThreads[i].openServerSocket(serverAddr, Config.PORTS_GAME[i]);
			_selectorThreads[i].start();
		}
		AuthServerCommunication.getInstance().start();

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
			ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 30000L);
		
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoAnnounce(), 60000, 60000);

		getListeners().onStart();

		if(Config.IS_TELNET_ENABLED)
			statusServer = new TelnetServer();
		else
			_log.info("Telnet server is currently disabled.");

		_log.info("=================================================");
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for(String line : memUsage.split("\n"))
			_log.info(line);
		_log.info("=================================================");
		RebellionTeam.showLogo();
	}

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}

	public static GameServer getInstance()
	{
		return _instance;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}

	public static void checkFreePorts()
	{
		boolean binded = false;
		while(!binded)
			for(int PORT_GAME : Config.PORTS_GAME)
				try
				{
					ServerSocket ss;
					if(Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*"))
						ss = new ServerSocket(PORT_GAME);
					else
						ss = new ServerSocket(PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
					ss.close();
					binded = true;
				}
				catch(Exception e)
				{
					_log.warn("Port " + PORT_GAME + " is allready binded. Please free it and restart server.");
					binded = false;
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException e2)
					{}
				}
	}

	public static void main(String[] args) throws Exception
	{
		new GameServer();
	}

	public Version getVersion()
	{
		return version;
	}

	public TelnetServer getStatusServer()
	{
		return statusServer;
	}
}