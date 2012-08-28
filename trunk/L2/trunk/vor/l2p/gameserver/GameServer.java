package l2p.gameserver;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import l2p.commons.lang.StatsUtils;
import l2p.commons.listener.Listener;
import l2p.commons.listener.ListenerList;
import l2p.commons.net.nio.impl.SelectorThread;
import l2p.commons.versioning.Version;
import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.dao.CharacterDAO;
import l2p.gameserver.dao.ItemsDAO;
import l2p.gameserver.data.BoatHolder;
import l2p.gameserver.data.xml.Parsers;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.data.xml.holder.StaticObjectHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.handler.admincommands.AdminCommandHandler;
import l2p.gameserver.handler.items.ItemHandler;
import l2p.gameserver.handler.usercommands.UserCommandHandler;
import l2p.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.AutoSpawnManager;
import l2p.gameserver.instancemanager.BloodAltarManager;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CoupleManager;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.instancemanager.DimensionalRiftManager;
import l2p.gameserver.instancemanager.HellboundManager;
import l2p.gameserver.instancemanager.PetitionManager;
import l2p.gameserver.instancemanager.PlayerMessageStack;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.instancemanager.SoDManager;
import l2p.gameserver.instancemanager.SoIManager;
import l2p.gameserver.instancemanager.SpawnManager;
import l2p.gameserver.instancemanager.games.FishingChampionShipManager;
import l2p.gameserver.instancemanager.games.LotteryManager;
import l2p.gameserver.instancemanager.games.MiniGameScoreManager;
import l2p.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2p.gameserver.instancemanager.naia.NaiaCoreManager;
import l2p.gameserver.instancemanager.naia.NaiaTowerManager;
import l2p.gameserver.listener.GameListener;
import l2p.gameserver.listener.game.OnShutdownListener;
import l2p.gameserver.listener.game.OnStartListener;
import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.model.World;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.MonsterRace;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.network.GamePacketHandler;
import l2p.gameserver.network.telnet.TelnetServer;
import l2p.gameserver.scripts.Scripts;
import l2p.gameserver.tables.AugmentationData;
import l2p.gameserver.tables.CharTemplateTable;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.tables.EnchantHPBonusTable;
import l2p.gameserver.tables.LevelUpTable;
import l2p.gameserver.tables.PetSkillsTable;
import l2p.gameserver.tables.SkillTreeTable;
import l2p.gameserver.taskmanager.ItemsAutoDestroy;
import l2p.gameserver.taskmanager.TaskManager;
import l2p.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import l2p.gameserver.utils.Strings;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer
{
  public static final int LOGIN_SERVER_PROTOCOL = 2;
  private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
  public static GameServer _instance;
  private final SelectorThread<GameClient>[] _selectorThreads;
  private Version version;
  private TelnetServer statusServer;
  private final GameServerListenerList _listeners;
  private int _serverStarted;

  public SelectorThread<GameClient>[] getSelectorThreads()
  {
    return _selectorThreads;
  }

  public int time()
  {
    return (int)(System.currentTimeMillis() / 1000L);
  }

  public int uptime()
  {
    return time() - _serverStarted;
  }

  public GameServer()
    throws Exception
  {
    _instance = this;
    _serverStarted = time();
    _listeners = new GameServerListenerList();

    new File("./log/").mkdir();

    version = new Version(GameServer.class);

    _log.info("=================================================");
    _log.info("Revision: ................ " + version.getRevisionNumber());
    _log.info("Build date: .............. " + version.getBuildDate());
    _log.info("Compiler version: ........ " + version.getBuildJdk());
    _log.info("=================================================");

    Config.load();

    checkFreePorts();

    Class.forName(Config.DATABASE_DRIVER).newInstance();
    DatabaseFactory.getInstance().getConnection().close();

    IdFactory _idFactory = IdFactory.getInstance();
    if (!_idFactory.isInitialized())
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

    SkillTreeTable.getInstance();

    AugmentationData.getInstance();

    CharTemplateTable.getInstance();

    EnchantHPBonusTable.getInstance();

    LevelUpTable.getInstance();

    PetSkillsTable.getInstance();

    ItemAuctionManager.getInstance();

    SpawnManager.getInstance().spawnAll();
    BoatHolder.getInstance().spawnAll();

    StaticObjectHolder.getInstance().spawnAll();

    RaidBossSpawnManager.getInstance();

    Scripts.getInstance().init();

    DimensionalRiftManager.getInstance();

    Announcements.getInstance();

    LotteryManager.getInstance();

    PlayerMessageStack.getInstance();

    if (Config.AUTODESTROY_ITEM_AFTER > 0) {
      ItemsAutoDestroy.getInstance();
    }
    MonsterRace.getInstance();

    SevenSigns.getInstance();
    SevenSignsFestival.getInstance();
    SevenSigns.getInstance().updateFestivalScore();

    AutoSpawnManager.getInstance();

    SevenSigns.getInstance().spawnSevenSignsNPC();

    if (Config.ENABLE_OLYMPIAD)
    {
      Olympiad.load();
      Hero.getInstance();
    }

    PetitionManager.getInstance();

    CursedWeaponsManager.getInstance();

    if (!Config.ALLOW_WEDDING)
    {
      CoupleManager.getInstance();
      _log.info("CoupleManager initialized");
    }

    ItemHandler.getInstance();

    AdminCommandHandler.getInstance().log();
    UserCommandHandler.getInstance().log();
    VoicedCommandHandler.getInstance().log();

    TaskManager.getInstance();

    _log.info("=[Events]=========================================");
    ResidenceHolder.getInstance().callInit();
    EventHolder.getInstance().callInit();
    _log.info("==================================================");

    CastleManorManager.getInstance();

    Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

    _log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

    CoupleManager.getInstance();

    if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
      FishingChampionShipManager.getInstance();
    }
    HellboundManager.getInstance();

    NaiaTowerManager.getInstance();
    NaiaCoreManager.getInstance();

    SoDManager.getInstance();
    SoIManager.getInstance();
    BloodAltarManager.getInstance();

    MiniGameScoreManager.getInstance();

    Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, 2);

    _log.info("GameServer Started");
    _log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

    GamePacketHandler gph = new GamePacketHandler();

    InetAddress serverAddr = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.GAMESERVER_HOSTNAME);

    _selectorThreads = new SelectorThread[Config.PORTS_GAME.length];
    for (int i = 0; i < Config.PORTS_GAME.length; i++)
    {
      _selectorThreads[i] = new SelectorThread(Config.SELECTOR_CONFIG, gph, gph, gph, null);
      _selectorThreads[i].openServerSocket(serverAddr, Config.PORTS_GAME[i]);
      _selectorThreads[i].start();
    }

    LoginServerCommunication.getInstance().start();

    if (Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART) {
      ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 30000L);
    }
    getListeners().onStart();

    if (Config.IS_TELNET_ENABLED)
      statusServer = new TelnetServer();
    else {
      _log.info("Telnet server is currently disabled.");
    }
    _log.info("=================================================");
    String memUsage = StatsUtils.getMemUsage();
    for (String line : memUsage.split("\n"))
      _log.info(line);
    _log.info("=================================================");
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
    while (!binded)
      for (int PORT_GAME : Config.PORTS_GAME)
        try
        {
          ServerSocket ss;
          ServerSocket ss;
          if (Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*"))
            ss = new ServerSocket(PORT_GAME);
          else
            ss = new ServerSocket(PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
          ss.close();
          binded = true;
        }
        catch (Exception e)
        {
          _log.warn("Port " + PORT_GAME + " is allready binded. Please free it and restart server.");
          binded = false;
          try
          {
            Thread.sleep(1000L);
          }
          catch (InterruptedException e2)
          {
          }
        }
  }

  public static void main(String[] args) throws Exception {
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

  public class GameServerListenerList extends ListenerList<GameServer>
  {
    public GameServerListenerList()
    {
    }

    public void onStart()
    {
      for (Listener listener : getListeners())
        if (OnStartListener.class.isInstance(listener))
          ((OnStartListener)listener).onStart();
    }

    public void onShutdown()
    {
      for (Listener listener : getListeners())
        if (OnShutdownListener.class.isInstance(listener))
          ((OnShutdownListener)listener).onShutdown();
    }
  }
}