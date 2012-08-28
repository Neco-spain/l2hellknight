package l2m.gameserver;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import l2p.commons.lang.StatsUtils;
import l2p.commons.listener.Listener;
import l2p.commons.listener.ListenerList;
import l2p.commons.net.nio.impl.SelectorThread;
import l2p.commons.versioning.Version;
import l2m.gameserver.cache.CrestCache;
import l2m.gameserver.data.dao.CharacterDAO;
import l2m.gameserver.data.dao.ItemsDAO;
import l2m.gameserver.data.BoatHolder;
import l2m.gameserver.data.xml.Parsers;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.data.xml.holder.StaticObjectHolder;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.geodata.GeoEngine;
import l2m.gameserver.handler.admincommands.AdminCommandHandler;
import l2m.gameserver.handler.items.ItemHandler;
import l2m.gameserver.handler.usercommands.UserCommandHandler;
import l2m.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2m.gameserver.idfactory.IdFactory;
import l2m.gameserver.instancemanager.AutoSpawnManager;
import l2m.gameserver.instancemanager.BloodAltarManager;
import l2m.gameserver.instancemanager.CastleManorManager;
import l2m.gameserver.instancemanager.CoupleManager;
import l2m.gameserver.instancemanager.CursedWeaponsManager;
import l2m.gameserver.instancemanager.DimensionalRiftManager;
import l2m.gameserver.instancemanager.HellboundManager;
import l2m.gameserver.instancemanager.PetitionManager;
import l2m.gameserver.instancemanager.PlayerMessageStack;
import l2m.gameserver.instancemanager.RaidBossSpawnManager;
import l2m.gameserver.instancemanager.SoDManager;
import l2m.gameserver.instancemanager.SoIManager;
import l2m.gameserver.instancemanager.SpawnManager;
import l2m.gameserver.instancemanager.games.FishingChampionShipManager;
import l2m.gameserver.instancemanager.games.LotteryManager;
import l2m.gameserver.instancemanager.games.MiniGameScoreManager;
import l2m.gameserver.instancemanager.itemauction.ItemAuctionManager;
import l2m.gameserver.instancemanager.naia.NaiaCoreManager;
import l2m.gameserver.instancemanager.naia.NaiaTowerManager;
import l2m.gameserver.listener.GameListener;
import l2m.gameserver.listener.game.OnShutdownListener;
import l2m.gameserver.listener.game.OnStartListener;
import l2m.gameserver.loginservercon.LoginServerCommunication;
import l2m.gameserver.model.World;
import l2m.gameserver.model.entity.Hero;
import l2m.gameserver.model.entity.MonsterRace;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2m.gameserver.model.entity.olympiad.Olympiad;
import l2m.gameserver.model.reward.RewardL2Top;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.GamePacketHandler;
import l2m.gameserver.network.telnet.TelnetServer;
import l2m.gameserver.scripts.Scripts;
import l2m.gameserver.data.tables.AugmentationData;
import l2m.gameserver.data.tables.CharTemplateTable;
import l2m.gameserver.data.tables.ClanTable;
import l2m.gameserver.data.tables.EnchantHPBonusTable;
import l2m.gameserver.data.tables.LevelUpTable;
import l2m.gameserver.data.tables.PetSkillsTable;
import l2m.gameserver.data.tables.SkillTreeTable;
import l2m.gameserver.taskmanager.ItemsAutoDestroy;
import l2m.gameserver.taskmanager.TaskManager;
import l2m.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import l2m.gameserver.utils.Strings;
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
    aConfig.load();

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

    RewardL2Top.getInstance();

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