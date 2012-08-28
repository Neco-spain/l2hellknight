package net.sf.l2j.gameserver;

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
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Server;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ArmorSetsTable;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.EventDroplist;
import net.sf.l2j.gameserver.datatables.ExtractableItemsData;
import net.sf.l2j.gameserver.datatables.FakePlayersTablePlus;
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
import net.sf.l2j.gameserver.datatables.WorldRegionTable;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorListener;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.entity.FightClub;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.TvTManager;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GamePacketHandler;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.util.MemoryAgent;
import net.sf.l2j.gameserver.util.Online;
import net.sf.l2j.gameserver.util.PcCafe;
import net.sf.l2j.gameserver.util.QueuedItems;
import net.sf.l2j.gameserver.util.WebStat;
import net.sf.l2j.gameserver.util.protection.GameGuard;
import net.sf.l2j.gameserver.util.vote.L2TopRU;
import net.sf.l2j.gameserver.util.vote.MmotopRU;
import net.sf.l2j.status.Status;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.log.AbstractLogger;
import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;
import scripts.clanhalls.BanditStronghold;
import scripts.commands.AdminCommandHandler;
import scripts.commands.UserCommandHandler;
import scripts.commands.VoicedCommandHandler;
import scripts.communitybbs.Manager.AuctionBBSManager;
import scripts.communitybbs.Manager.AugmentBBSManager;
import scripts.communitybbs.Manager.CustomBBSManager;
import scripts.communitybbs.Manager.MailBBSManager;
import scripts.communitybbs.Manager.MenuBBSManager;
import scripts.items.ItemHandler;
import scripts.script.faenor.FaenorScriptEngine;
import scripts.scripting.CompiledScriptCache;
import scripts.scripting.L2ScriptEngineManager;
import scripts.skills.SkillHandler;

public class GameServer
{
  private static Logger _log;
  private final SelectorThread<L2GameClient> _selectorThread;
  private final SkillTable _st;
  private final ItemTable _it;
  private final NpcTable _nt;
  private final HennaTable _ht;
  private final IdFactory _if;
  public static GameServer gameServer;
  private static ClanHallManager _cm;
  private final Shutdown _sh;
  private final DoorTable _dt;
  private final SevenSigns _ss;
  private final AutoChatHandler _ach;
  private final AutoSpawnHandler _ash;
  private LoginServerThread _loginThread;
  private final HelperBuffTable _hbt;
  private static Status _statusServer;
  private final ThreadPoolManager _threadpools;
  public static final Calendar dateTimeServerStarted = Calendar.getInstance();

  public long getUsedMemoryMB() {
    return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
  }

  public SelectorThread<L2GameClient> getSelectorThread() {
    return _selectorThread;
  }

  public ClanHallManager getCHManager() {
    return _cm;
  }

  public GameServer() throws Exception {
    gameServer = this;

    ThreadPoolManager.init();
    _threadpools = ThreadPoolManager.getInstance();

    AbstractLogger.startRefresTask();

    _if = IdFactory.getInstance();
    if (!_if.isInitialized()) {
      _log.severe("GameServer [ERROR]: Could not read object IDs from DB. Please Check Your Data.");
      throw new Exception("GameServer [ERROR]: Could not initialize the ID factory");
    }
    Formulas.init();
    ItemTable.init();

    if (Config.DEADLOCKCHECK_INTERVAL > 0) {
      DeadlockDetector.init();
    }
    new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();

    new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();

    L2ScriptEngineManager.getInstance();

    GameTimeController.init();

    CharNameTable.getInstance();

    ClanTable.init();
    _it = ItemTable.getInstance();
    if (!_it.isInitialized()) {
      _log.severe("GameServer [ERROR]: Could not find the extraced files. Please Check Your Data.");
      throw new Exception("GameServer [ERROR]: Could not initialize the item table");
    }

    ExtractableItemsData.getInstance();
    CustomServerData.init();
    SummonItemsData.getInstance();
    WorldRegionTable.init();

    GrandBossManager.init();
    ClanHallManager.init();
    _cm = ClanHallManager.getInstance();
    CastleManager.init();
    ZoneManager.init();
    SiegeManager.init();

    TradeController.init();
    _st = SkillTable.getInstance();
    if (!_st.isInitialized()) {
      _log.severe("GameServer [ERROR]: Could not find the extraced files. Please Check Your Data.");
      throw new Exception("GameServer [ERROR]: Could not initialize the skill table");
    }

    NpcWalkerRoutesTable.getInstance().load();

    GrandBossManager.getInstance().loadManagers();

    RecipeController.init();
    NobleSkillTable.init();
    HeroSkillTable.init();

    SkillTreeTable.getInstance();
    ArmorSetsTable.getInstance();
    FishTable.getInstance();
    SkillSpellbookTable.getInstance();
    CharTemplateTable.getInstance();

    HtmCache.getInstance();
    CrestCache.init();
    Static.init();
    _nt = NpcTable.getInstance();

    if (!_nt.isInitialized()) {
      _log.severe("GameServer [ERROR]: Could not find the extraced files. Please Check Your Data.");
      throw new Exception("GameServer [ERROR]: Could not initialize the npc table");
    }

    _ht = HennaTable.getInstance();

    if (!_ht.isInitialized()) {
      throw new Exception("GameServer [ERROR]: Could not initialize the Henna Table");
    }

    HennaTreeTable.getInstance();

    if (!_ht.isInitialized()) {
      throw new Exception("GameServer [ERROR]: Could not initialize the Henna Tree Table");
    }

    _hbt = HelperBuffTable.getInstance();

    if (!_hbt.isInitialized()) {
      throw new Exception("GameServer [ERROR]: Could not initialize the Helper Buff Table");
    }

    GeoData.init();

    AttackStanceTaskManager.init();
    DecayTaskManager.init();

    TeleportLocationTable.init();
    LevelUpData.getInstance();
    L2World.getInstance();

    SpawnTable.getInstance();
    RaidBossSpawnManager.init();

    RaidBossPointsManager.init();

    DimensionalRiftManager.getInstance();
    Announcements.init();
    MapRegionTable.getInstance();
    EventDroplist.getInstance();

    L2Manor.init();
    L2Multisell.getInstance();

    AuctionManager.getInstance();
    BoatManager.init();
    CastleManorManager.pinit();
    MercTicketManager.init();

    PetitionManager.getInstance();
    QuestManager.init();
    EventManager.init();
    BanditStronghold.init();

    AugmentationData.getInstance();
    if (Config.SAVE_DROPPED_ITEM) {
      ItemsOnGroundManager.init();
    }

    if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0)) {
      ItemsAutoDestroy.init();
    }

    MonsterRace.getInstance();

    ItemHandler.getInstance();
    SkillHandler.getInstance();
    UserCommandHandler.getInstance();
    VoicedCommandHandler.getInstance();

    _dt = DoorTable.getInstance();
    _dt.parseData();
    StaticObjects.getInstance();
    try
    {
      _log.info("GameServer: Loading Server Scripts");
      File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
      L2ScriptEngineManager.getInstance().executeScriptList(scripts);
    } catch (IOException ioe) {
      _log.severe("GameServer [ERROR]: Failed loading scripts.cfg, no script going to be loaded");
    }
    try {
      CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
      if (compiledScriptCache == null) {
        _log.info("GameServer: Compiled Scripts Cache is disabled.");
      } else {
        compiledScriptCache.purge();

        if (compiledScriptCache.isModified()) {
          compiledScriptCache.save();
          _log.info("GameServer: Compiled Scripts Cache was saved.");
        } else {
          _log.info("GameServer: Compiled Scripts Cache is up-to-date.");
        }
      }
    }
    catch (IOException e) {
      _log.log(Level.SEVERE, "GameServer [ERROR]: Failed to store Compiled Scripts Cache.", e);
    }

    SevenSigns.init();
    _ss = SevenSigns.getInstance();
    SevenSignsFestival.init();
    FourSepulchersManager.init();
    AutoChatHandler.init();
    AutoSpawnHandler.init();
    _ash = AutoSpawnHandler.getInstance();
    _ach = AutoChatHandler.getInstance();

    _ss.spawnSevenSignsNPC();
    AdminCommandHandler.getInstance();

    Olympiad.load();

    Hero.getInstance();

    FaenorScriptEngine.getInstance();
    PartyWaitingRoomManager.init();

    CursedWeaponsManager.init();
    CrownManager.init();
    TownManager.init();

    _log.config("AutoChatHandler: Loaded " + _ach.size() + " handlers in total.");
    _log.config("AutoSpawnHandler: Loaded " + _ash.size() + " handlers in total.");

    if (Config.L2JMOD_ALLOW_WEDDING) {
      CoupleManager.init();
    }

    TaskManager.getInstance();

    GmListTable.getInstance();

    L2PetDataTable.getInstance().loadPetsData();

    if (Config.ACCEPT_GEOEDITOR_CONN) {
      GeoEditorListener.init();
    }

    _sh = Shutdown.getInstance();
    Runtime.getRuntime().addShutdownHook(_sh);
    try
    {
      _dt.getDoor(Integer.valueOf(24190001)).openMe();
      _dt.getDoor(Integer.valueOf(24190002)).openMe();
      _dt.getDoor(Integer.valueOf(24190003)).openMe();
      _dt.getDoor(Integer.valueOf(24190004)).openMe();
      _dt.getDoor(Integer.valueOf(23180001)).openMe();
      _dt.getDoor(Integer.valueOf(23180002)).openMe();
      _dt.getDoor(Integer.valueOf(23180003)).openMe();
      _dt.getDoor(Integer.valueOf(23180004)).openMe();
      _dt.getDoor(Integer.valueOf(23180005)).openMe();
      _dt.getDoor(Integer.valueOf(23180006)).openMe();
      _dt.getDoor(Integer.valueOf(19160001)).openMe();
      _dt.getDoor(Integer.valueOf(19160010)).openMe();
      _dt.getDoor(Integer.valueOf(19160011)).openMe();
      _dt.getDoor(Integer.valueOf(23150003)).openMe();
      _dt.getDoor(Integer.valueOf(23150004)).openMe();

      _dt.checkAutoOpen();
    } catch (NullPointerException e) {
      e.printStackTrace();
      _log.warning("GameServer [ERROR]: There is errors in your Door.csv file. Update door.csv");
    }

    if (Config.COMMUNITY_TYPE.equals("pw")) {
      AugmentBBSManager.init();
      AuctionBBSManager.init();
      CustomBBSManager.init();
      MailBBSManager.init();
      MenuBBSManager.init();
    }

    _log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

    TvTManager.getInstance();
    FightClub.init();

    Runtime r = Runtime.getRuntime();

    long freeMem = (r.maxMemory() - r.totalMemory() + r.freeMemory()) / 1048576L;
    long totalMem = r.maxMemory() / 1048576L;
    _log.info("GameServer: Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");

    LoginServerThread.init();
    _loginThread = LoginServerThread.getInstance();
    _loginThread.start();

    L2GamePacketHandler gph = new L2GamePacketHandler();

    SelectorConfig sc = new SelectorConfig(null, null, gph, gph);
    sc.setMaxSendPerPass(12);
    sc.setSelectorSleepTime(20);

    _selectorThread = new SelectorThread(sc, gph, gph, null);

    InetAddress bindAddress = null;
    if (!Config.GAMESERVER_HOSTNAME.equals("*")) {
      try {
        bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
      } catch (UnknownHostException e1) {
        _log.severe("GameServer [ERROR]:  The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage());

        if (Config.DEVELOPER) {
          e1.printStackTrace();
        }
      }
    }
    try
    {
      _selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
    } catch (IOException e) {
      _log.severe("GameServer [ERROR]: Failed to open server socket. Reason: " + e.getMessage());
      if (Config.DEVELOPER) {
        e.printStackTrace();
      }
      System.exit(1);
    }

    if (Config.IS_TELNET_ENABLED) {
      _statusServer = new Status(Server.serverMode);
      _statusServer.start();
    } else {
      _log.info("GameServer: Telnet server is currently disabled.");
    }

    _log.config("GameServer: Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

    _selectorThread.start();

    Online.getInstance().loadMaxOnline();
    MemoryAgent.init();

    FakePlayersTablePlus.init();

    if (Config.CACHED_SERVER_STAT) {
      CustomServerData.getInstance().cacheStat();
    }

    if (Config.RESTART_HOUR > 0)
      AutoRestart.init();
    else {
      _log.info("Auto Restart: disabled.");
    }

    if (Config.L2TOP_ENABLE) {
      L2TopRU.init();
    }

    if (Config.MMOTOP_ENABLE) {
      MmotopRU.init();
    }

    if (Config.PC_CAFE_ENABLED) {
      PcCafe.init();
    }

    if (Config.QUED_ITEMS_ENABLE) {
      QueuedItems.init();
    }

    if (Config.WEBSTAT_ENABLE) {
      WebStat.init();
    }

    GameGuard.getInstance().startCheckTask();

    AbstractLogger.setLoaded();
  }

  public static void main(String[] args) throws Exception {
    Server.serverMode = 1;
    try
    {
      Config.load(false);
    } catch (Exception e) {
    }
    AbstractLogger.init();
    _log = AbstractLogger.getLogger(GameServer.class.getName());
    _log.info(TimeLogger.getLogTime() + "Welcome to pwServer.");

    GameGuard.init();

    new File(Config.DATAPACK_ROOT, "log").mkdir();

    InputStream is = new FileInputStream(new File("./config/log.cfg"));
    LogManager.getLogManager().readConfiguration(is);
    is.close();

    Log.init();

    L2DatabaseFactory.init();
    gameServer = new GameServer();
  }
}