package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
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
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAdmin;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBBS;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBan;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminBanChat;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCTFEngine;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCache;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCreateItem;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDelete;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminDoorControl;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditNpc;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEffects;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEnchant;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEventEngine;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminExpSp;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminFortSiege;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGeoEditor;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGm;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminGmChat;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHeal;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHelpPage;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminHwid;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminInvul;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKick;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminKill;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminLevel;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminLogin;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMammon;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminManor;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMenu;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMobGroup;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPForge;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPetition;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPledge;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPolymorph;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPremium;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminQuest;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRepairChar;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRes;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSetNoble;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminShop;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminShutdown;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSiege;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSkill;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminSpawn;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTarget;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTeleport;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTest;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTvTEvent;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminZone;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSoulShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSpice;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BlessedSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.Book;
import net.sf.l2j.gameserver.handler.itemhandlers.CastPotions;
import net.sf.l2j.gameserver.handler.itemhandlers.CharChangePotions;
import net.sf.l2j.gameserver.handler.itemhandlers.CrystalCarol;
import net.sf.l2j.gameserver.handler.itemhandlers.EnchantScrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.EnergyStone;
import net.sf.l2j.gameserver.handler.itemhandlers.ExtractableItems;
import net.sf.l2j.gameserver.handler.itemhandlers.Firework;
import net.sf.l2j.gameserver.handler.itemhandlers.FishShots;
import net.sf.l2j.gameserver.handler.itemhandlers.Harvester;
import net.sf.l2j.gameserver.handler.itemhandlers.ItemBuff;
import net.sf.l2j.gameserver.handler.itemhandlers.Key;
import net.sf.l2j.gameserver.handler.itemhandlers.Maps;
import net.sf.l2j.gameserver.handler.itemhandlers.MercTicket;
import net.sf.l2j.gameserver.handler.itemhandlers.MysteryPotion;
import net.sf.l2j.gameserver.handler.itemhandlers.PaganKeys;
import net.sf.l2j.gameserver.handler.itemhandlers.Potions;
import net.sf.l2j.gameserver.handler.itemhandlers.Recipes;
import net.sf.l2j.gameserver.handler.itemhandlers.Remedy;
import net.sf.l2j.gameserver.handler.itemhandlers.RollingDice;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfEscape;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfResurrection;
import net.sf.l2j.gameserver.handler.itemhandlers.Scrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.Seed;
import net.sf.l2j.gameserver.handler.itemhandlers.SevenSignsRecord;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulCrystals;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulShots;
import net.sf.l2j.gameserver.handler.itemhandlers.SpecialXMas;
import net.sf.l2j.gameserver.handler.itemhandlers.SpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.SummonItems;
import net.sf.l2j.gameserver.handler.skillhandlers.BalanceLife;
import net.sf.l2j.gameserver.handler.skillhandlers.BeastFeed;
import net.sf.l2j.gameserver.handler.skillhandlers.Blow;
import net.sf.l2j.gameserver.handler.skillhandlers.Charge;
import net.sf.l2j.gameserver.handler.skillhandlers.CombatPointHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Continuous;
import net.sf.l2j.gameserver.handler.skillhandlers.CpDam;
import net.sf.l2j.gameserver.handler.skillhandlers.Craft;
import net.sf.l2j.gameserver.handler.skillhandlers.DeluxeKey;
import net.sf.l2j.gameserver.handler.skillhandlers.Disablers;
import net.sf.l2j.gameserver.handler.skillhandlers.DrainSoul;
import net.sf.l2j.gameserver.handler.skillhandlers.Fishing;
import net.sf.l2j.gameserver.handler.skillhandlers.FishingSkill;
import net.sf.l2j.gameserver.handler.skillhandlers.GetPlayer;
import net.sf.l2j.gameserver.handler.skillhandlers.Harvest;
import net.sf.l2j.gameserver.handler.skillhandlers.Heal;
import net.sf.l2j.gameserver.handler.skillhandlers.ManaHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Manadam;
import net.sf.l2j.gameserver.handler.skillhandlers.Mdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Pdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Recall;
import net.sf.l2j.gameserver.handler.skillhandlers.Resurrect;
import net.sf.l2j.gameserver.handler.skillhandlers.SiegeFlag;
import net.sf.l2j.gameserver.handler.skillhandlers.Sow;
import net.sf.l2j.gameserver.handler.skillhandlers.Spoil;
import net.sf.l2j.gameserver.handler.skillhandlers.StrSiegeAssault;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonFriend;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonPet;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonTreasureKey;
import net.sf.l2j.gameserver.handler.skillhandlers.Sweep;
import net.sf.l2j.gameserver.handler.skillhandlers.TakeCastle;
import net.sf.l2j.gameserver.handler.skillhandlers.Unlock;
import net.sf.l2j.gameserver.handler.usercommandhandlers.ChannelDelete;
import net.sf.l2j.gameserver.handler.usercommandhandlers.ChannelLeave;
import net.sf.l2j.gameserver.handler.usercommandhandlers.ChannelListUpdate;
import net.sf.l2j.gameserver.handler.usercommandhandlers.ClanPenalty;
import net.sf.l2j.gameserver.handler.usercommandhandlers.ClanWarsList;
import net.sf.l2j.gameserver.handler.usercommandhandlers.DisMount;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Escape;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Loc;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Mount;
import net.sf.l2j.gameserver.handler.usercommandhandlers.OlympiadStat;
import net.sf.l2j.gameserver.handler.usercommandhandlers.PartyInfo;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Time;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Banking;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Wedding;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.menu;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
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
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Top;
import net.sf.l2j.gameserver.model.entity.SayFilter;
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
import net.sf.l2j.util.Util;
import net.sf.protection.nProtect;
import org.mmocore.network.MMOConnection;
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
  private final HennaTreeTable _hennaTreeTable;
  private final IdFactory _idFactory;
  public static GameServer gameServer;
  private static ClanHallManager _cHManager;
  private final ItemHandler _itemHandler;
  private final SkillHandler _skillHandler;
  private final AdminCommandHandler _adminCommandHandler;
  private final Shutdown _shutdownHandler;
  private final UserCommandHandler _userCommandHandler;
  private final VoicedCommandHandler _voicedCommandHandler;
  private final DoorTable _doorTable;
  private final SevenSigns _sevenSignsEngine;
  private final AutoChatHandler _autoChatHandler;
  private final AutoSpawnHandler _autoSpawnHandler;
  private LoginServerThread _loginThread;
  private final HelperBuffTable _helperBuffTable;
  private static Status _statusServer;
  private static int _serverStarted;
  public static final Calendar dateTimeServerStarted = Calendar.getInstance();

  public long getUsedMemoryMB()
  {
    return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
  }

  public SelectorThread<L2GameClient> getSelectorThread()
  {
    return _selectorThread;
  }

  public ClanHallManager getCHManager() {
    return _cHManager;
  }

  public GameServer() throws Exception
  {
    L2JSoftware.info();

    gameServer = this;
    _log.finest("Used memory: " + getUsedMemoryMB() + "mb");
    _idFactory = IdFactory.getInstance();
    if (!_idFactory.isInitialized())
    {
      _log.severe("Could not read object IDs from DB. Please Check Your Data.");
      throw new Exception("Could not initialize the ID factory");
    }

    ThreadPoolManager.getInstance();

    new DeadlockDetector().start();

    new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
    new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
    GameTimeController.getInstance();

    CharNameTable.getInstance();

    _itemTable = ItemTable.getInstance();
    if (!_itemTable.isInitialized())
    {
      _log.severe("Could not find the extraced files. Please Check Your Data.");
      throw new Exception("Could not initialize the item table");
    }

    ExtractableItemsData.getInstance();
    SummonItemsData.getInstance();

    nProtect.getInstance();

    TradeController.getInstance();
    _skillTable = SkillTable.getInstance();
    if (!_skillTable.isInitialized())
    {
      _log.severe("Could not find the extraced files. Please Check Your Data.");
      throw new Exception("Could not initialize the skill table");
    }
    NpcWalkerRoutesTable.getInstance().load();

    RecipeController.getInstance();
    Util.printSection("Skills");
    SkillTreeTable.getInstance();
    ArmorSetsTable.getInstance();
    FishTable.getInstance();
    SkillSpellbookTable.getInstance();
    CharTemplateTable.getInstance();
    NobleSkillTable.getInstance();

    HeroSkillTable.getInstance();
    HtmCache.getInstance();
    CrestCache.getInstance();
    ClanTable.getInstance();

    _npcTable = NpcTable.getInstance();

    if (!_npcTable.isInitialized())
    {
      _log.severe("Could not find the extraced files. Please Check Your Data.");
      throw new Exception("Could not initialize the npc table");
    }

    _hennaTable = HennaTable.getInstance();

    if (!_hennaTable.isInitialized())
    {
      throw new Exception("Could not initialize the Henna Table");
    }

    _hennaTreeTable = HennaTreeTable.getInstance();

    if (!_hennaTreeTable.isInitialized())
    {
      throw new Exception("Could not initialize the Henna Tree Table");
    }

    _helperBuffTable = HelperBuffTable.getInstance();

    if (!_helperBuffTable.isInitialized())
    {
      throw new Exception("Could not initialize the Helper Buff Table");
    }

    if (Config.GEODATA)
    {
      GeoData.getInstance();
    }
    if (Config.GEO_PATH_FINDING)
    {
      GeoPathFinding.getInstance();
    }
    _log.severe("#######################################################");
    _cHManager = ClanHallManager.getInstance();
    BanditStrongholdSiege.getInstance();
    DevastatedCastle.getInstance();
    FortressOfResistance.getInstance();
    WildBeastFarmSiege.getInstance();
    FortressOfTheDeadManager.getInstance();
    _log.severe("#######################################################");
    CastleManager.getInstance();
    SiegeManager.getInstance();
    FortManager.getInstance();
    FortSiegeManager.getInstance();

    TeleportLocationTable.getInstance();
    LevelUpData.getInstance();
    L2World.getInstance();
    ZoneData.getInstance();
    SpawnTable.getInstance();
    Util.printSection("Managers");
    RaidBossSpawnManager.getInstance();
    RaidBossPointsManager.init();
    DayNightSpawnManager.getInstance().notifyChangeMode();
    DimensionalRiftManager.getInstance();
    Announcements.getInstance();
    MapRegionTable.getInstance();
    EventDroplist.getInstance();
    L2Manor.getInstance();

    AuctionManager.getInstance();
    BoatManager.getInstance();
    KnownListUpdateTaskManager.getInstance();
    CastleManorManager.getInstance();
    MercTicketManager.getInstance();
    PetitionManager.getInstance();
    _doorTable = DoorTable.getInstance();
    _doorTable.parseData();
    GrandBossManager.getInstance();
    FourSepulchersManager.getInstance().init();
    VanHalterManager.getInstance().init();

    Sailren.getInstance().init();
    QuestManager.getInstance();
    try
    {
      _log.info("Loading Server Scripts");
      File scripts = new File("./config/scripts.ini");
      L2ScriptEngineManager.getInstance().executeScriptList(scripts);
    }
    catch (IOException ioe)
    {
      _log.severe("Failed loading scripts.ini, no script going to be loaded");
    }
    _log.info(" ---------------");
    _log.info("AILoader started");
    AILoader.init();
    QuestManager.getInstance().report();
    AugmentationData.getInstance();
    if (Config.SAVE_DROPPED_ITEM) {
      ItemsOnGroundManager.getInstance();
    }
    if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0)) {
      ItemsAutoDestroy.getInstance();
    }
    MonsterRace.getInstance();
    StaticObjects.getInstance();

    _sevenSignsEngine = SevenSigns.getInstance();
    SevenSignsFestival.getInstance();
    _autoSpawnHandler = AutoSpawnHandler.getInstance();
    _autoChatHandler = AutoChatHandler.getInstance();
    _sevenSignsEngine.spawnSevenSignsNPC();

    Olympiad.getInstance();
    Hero.getInstance();
    FaenorScriptEngine.getInstance();
    CursedWeaponsManager.getInstance();

    _log.config("*** AutoChat: " + _autoChatHandler.size() + " loaded in total.");
    _log.config("*** AutoSpawn: " + _autoSpawnHandler.size() + " loaded in total.");

    _itemHandler = ItemHandler.getInstance();
    _itemHandler.registerItemHandler(new ScrollOfEscape());
    _itemHandler.registerItemHandler(new ScrollOfResurrection());
    _itemHandler.registerItemHandler(new SoulShots());
    _itemHandler.registerItemHandler(new SpiritShot());
    _itemHandler.registerItemHandler(new BlessedSpiritShot());
    _itemHandler.registerItemHandler(new BeastSoulShot());
    _itemHandler.registerItemHandler(new BeastSpiritShot());
    _itemHandler.registerItemHandler(new Key());
    _itemHandler.registerItemHandler(new PaganKeys());
    _itemHandler.registerItemHandler(new Maps());
    _itemHandler.registerItemHandler(new CastPotions());
    _itemHandler.registerItemHandler(new Potions());
    _itemHandler.registerItemHandler(new Recipes());
    _itemHandler.registerItemHandler(new RollingDice());
    _itemHandler.registerItemHandler(new MysteryPotion());
    _itemHandler.registerItemHandler(new EnchantScrolls());
    _itemHandler.registerItemHandler(new EnergyStone());
    _itemHandler.registerItemHandler(new Book());
    _itemHandler.registerItemHandler(new Remedy());
    _itemHandler.registerItemHandler(new Scrolls());
    _itemHandler.registerItemHandler(new CrystalCarol());
    _itemHandler.registerItemHandler(new SoulCrystals());
    _itemHandler.registerItemHandler(new SevenSignsRecord());
    _itemHandler.registerItemHandler(new CharChangePotions());
    _itemHandler.registerItemHandler(new Firework());
    _itemHandler.registerItemHandler(new Seed());
    _itemHandler.registerItemHandler(new Harvester());
    _itemHandler.registerItemHandler(new MercTicket());
    _itemHandler.registerItemHandler(new FishShots());
    _itemHandler.registerItemHandler(new ExtractableItems());
    _itemHandler.registerItemHandler(new SpecialXMas());
    _itemHandler.registerItemHandler(new SummonItems());
    _itemHandler.registerItemHandler(new BeastSpice());
    _itemHandler.registerItemHandler(new ItemBuff());
    _log.config("*** Items: " + _itemHandler.size() + " loaded.");

    _skillHandler = SkillHandler.getInstance();
    _skillHandler.registerSkillHandler(new SummonPet());
    _skillHandler.registerSkillHandler(new Blow());
    _skillHandler.registerSkillHandler(new Pdam());
    _skillHandler.registerSkillHandler(new Mdam());
    _skillHandler.registerSkillHandler(new CpDam());
    _skillHandler.registerSkillHandler(new Manadam());
    _skillHandler.registerSkillHandler(new Heal());
    _skillHandler.registerSkillHandler(new CombatPointHeal());
    _skillHandler.registerSkillHandler(new ManaHeal());
    _skillHandler.registerSkillHandler(new BalanceLife());
    _skillHandler.registerSkillHandler(new Charge());
    _skillHandler.registerSkillHandler(new Continuous());
    _skillHandler.registerSkillHandler(new Resurrect());
    _skillHandler.registerSkillHandler(new Spoil());
    _skillHandler.registerSkillHandler(new Sweep());
    _skillHandler.registerSkillHandler(new StrSiegeAssault());
    _skillHandler.registerSkillHandler(new SummonFriend());
    _skillHandler.registerSkillHandler(new SummonTreasureKey());
    _skillHandler.registerSkillHandler(new Disablers());
    _skillHandler.registerSkillHandler(new Recall());
    _skillHandler.registerSkillHandler(new SiegeFlag());
    _skillHandler.registerSkillHandler(new TakeCastle());
    _skillHandler.registerSkillHandler(new Unlock());
    _skillHandler.registerSkillHandler(new DrainSoul());
    _skillHandler.registerSkillHandler(new Craft());
    _skillHandler.registerSkillHandler(new Fishing());
    _skillHandler.registerSkillHandler(new FishingSkill());
    _skillHandler.registerSkillHandler(new BeastFeed());
    _skillHandler.registerSkillHandler(new DeluxeKey());
    _skillHandler.registerSkillHandler(new Sow());
    _skillHandler.registerSkillHandler(new Harvest());
    _skillHandler.registerSkillHandler(new GetPlayer());
    _log.config("*** Skills: " + _skillHandler.size() + " loaded.");

    _adminCommandHandler = AdminCommandHandler.getInstance();
    _adminCommandHandler.registerAdminCommandHandler(new AdminAdmin());
    _adminCommandHandler.registerAdminCommandHandler(new AdminHwid());
    _adminCommandHandler.registerAdminCommandHandler(new AdminInvul());
    _adminCommandHandler.registerAdminCommandHandler(new AdminDelete());
    _adminCommandHandler.registerAdminCommandHandler(new AdminKill());
    _adminCommandHandler.registerAdminCommandHandler(new AdminTarget());
    _adminCommandHandler.registerAdminCommandHandler(new AdminShop());
    _adminCommandHandler.registerAdminCommandHandler(new AdminAnnouncements());
    _adminCommandHandler.registerAdminCommandHandler(new AdminCreateItem());
    _adminCommandHandler.registerAdminCommandHandler(new AdminHeal());
    _adminCommandHandler.registerAdminCommandHandler(new AdminHelpPage());
    _adminCommandHandler.registerAdminCommandHandler(new AdminShutdown());
    _adminCommandHandler.registerAdminCommandHandler(new AdminSpawn());
    _adminCommandHandler.registerAdminCommandHandler(new AdminSkill());
    _adminCommandHandler.registerAdminCommandHandler(new AdminExpSp());
    _adminCommandHandler.registerAdminCommandHandler(new AdminEventEngine());
    _adminCommandHandler.registerAdminCommandHandler(new AdminGmChat());
    _adminCommandHandler.registerAdminCommandHandler(new AdminEditChar());
    _adminCommandHandler.registerAdminCommandHandler(new AdminGm());
    _adminCommandHandler.registerAdminCommandHandler(new AdminTeleport());
    _adminCommandHandler.registerAdminCommandHandler(new AdminRepairChar());
    _adminCommandHandler.registerAdminCommandHandler(new AdminChangeAccessLevel());
    _adminCommandHandler.registerAdminCommandHandler(new AdminCTFEngine());
    _adminCommandHandler.registerAdminCommandHandler(new AdminBan());
    _adminCommandHandler.registerAdminCommandHandler(new AdminPolymorph());
    _adminCommandHandler.registerAdminCommandHandler(new AdminBanChat());
    _adminCommandHandler.registerAdminCommandHandler(new AdminKick());
    _adminCommandHandler.registerAdminCommandHandler(new AdminMonsterRace());
    _adminCommandHandler.registerAdminCommandHandler(new AdminEditNpc());
    _adminCommandHandler.registerAdminCommandHandler(new AdminFightCalculator());
    _adminCommandHandler.registerAdminCommandHandler(new AdminMenu());
    _adminCommandHandler.registerAdminCommandHandler(new AdminSiege());
    _adminCommandHandler.registerAdminCommandHandler(new AdminPetition());
    _adminCommandHandler.registerAdminCommandHandler(new AdminPForge());
    _adminCommandHandler.registerAdminCommandHandler(new AdminBBS());
    _adminCommandHandler.registerAdminCommandHandler(new AdminEffects());
    _adminCommandHandler.registerAdminCommandHandler(new AdminDoorControl());
    _adminCommandHandler.registerAdminCommandHandler(new AdminTest());
    _adminCommandHandler.registerAdminCommandHandler(new AdminEnchant());
    _adminCommandHandler.registerAdminCommandHandler(new AdminMobGroup());
    _adminCommandHandler.registerAdminCommandHandler(new AdminRes());
    _adminCommandHandler.registerAdminCommandHandler(new AdminMammon());
    _adminCommandHandler.registerAdminCommandHandler(new AdminUnblockIp());
    _adminCommandHandler.registerAdminCommandHandler(new AdminPledge());
    _adminCommandHandler.registerAdminCommandHandler(new AdminRideWyvern());
    _adminCommandHandler.registerAdminCommandHandler(new AdminLogin());
    _adminCommandHandler.registerAdminCommandHandler(new AdminCache());
    _adminCommandHandler.registerAdminCommandHandler(new AdminLevel());
    _adminCommandHandler.registerAdminCommandHandler(new AdminQuest());
    _adminCommandHandler.registerAdminCommandHandler(new AdminZone());
    _adminCommandHandler.registerAdminCommandHandler(new AdminCursedWeapons());
    _adminCommandHandler.registerAdminCommandHandler(new AdminGeoEditor());
    _adminCommandHandler.registerAdminCommandHandler(new AdminManor());
    _adminCommandHandler.registerAdminCommandHandler(new AdminTvTEvent());
    _adminCommandHandler.registerAdminCommandHandler(new AdminFortSiege());
    _adminCommandHandler.registerAdminCommandHandler(new AdminPremium());
    _adminCommandHandler.registerAdminCommandHandler(new AdminSetNoble());
    _log.config("*** AdminCommand: " + _adminCommandHandler.size() + " loaded.");

    _userCommandHandler = UserCommandHandler.getInstance();
    _userCommandHandler.registerUserCommandHandler(new ClanPenalty());
    _userCommandHandler.registerUserCommandHandler(new ClanWarsList());
    _userCommandHandler.registerUserCommandHandler(new DisMount());
    _userCommandHandler.registerUserCommandHandler(new Escape());
    _userCommandHandler.registerUserCommandHandler(new Loc());
    _userCommandHandler.registerUserCommandHandler(new Mount());
    _userCommandHandler.registerUserCommandHandler(new PartyInfo());
    _userCommandHandler.registerUserCommandHandler(new Time());
    _userCommandHandler.registerUserCommandHandler(new OlympiadStat());
    _userCommandHandler.registerUserCommandHandler(new ChannelLeave());
    _userCommandHandler.registerUserCommandHandler(new ChannelDelete());
    _userCommandHandler.registerUserCommandHandler(new ChannelListUpdate());

    _log.config("*** UserCommand: " + _userCommandHandler.size() + " loaded.");

    _voicedCommandHandler = VoicedCommandHandler.getInstance();
    if (Config.ENABLE_MENU)
      _voicedCommandHandler.registerVoicedCommandHandler(new menu());
    if (Config.L2JMOD_ALLOW_WEDDING)
      _voicedCommandHandler.registerVoicedCommandHandler(new Wedding());
    if (Config.BANKING_SYSTEM_ENABLED) {
      _voicedCommandHandler.registerVoicedCommandHandler(new Banking());
    }

    _log.config("*** VoicedCommand: " + _voicedCommandHandler.size() + " loaded.");

    if (Config.L2JMOD_ALLOW_WEDDING) {
      CoupleManager.getInstance();
    }
    TaskManager.getInstance();

    GmListTable.getInstance();

    L2PetDataTable.getInstance().loadPetsData();

    Universe.getInstance();

    L2Multisell.getInstance();

    if (Config.ACCEPT_GEOEDITOR_CONN) {
      GeoEditorListener.getInstance();
    }
    _shutdownHandler = Shutdown.getInstance();
    Runtime.getRuntime().addShutdownHook(_shutdownHandler);
    try
    {
      _doorTable.getDoor(Integer.valueOf(24190001)).openMe();
      _doorTable.getDoor(Integer.valueOf(24190002)).openMe();
      _doorTable.getDoor(Integer.valueOf(24190003)).openMe();
      _doorTable.getDoor(Integer.valueOf(24190004)).openMe();
      _doorTable.getDoor(Integer.valueOf(23180001)).openMe();
      _doorTable.getDoor(Integer.valueOf(23180002)).openMe();
      _doorTable.getDoor(Integer.valueOf(23180003)).openMe();
      _doorTable.getDoor(Integer.valueOf(23180004)).openMe();
      _doorTable.getDoor(Integer.valueOf(23180005)).openMe();
      _doorTable.getDoor(Integer.valueOf(23180006)).openMe();

      _doorTable.getDoor(Integer.valueOf(23140001)).closeMe();
      _doorTable.getDoor(Integer.valueOf(23140002)).closeMe();

      _doorTable.checkAutoOpen();
    }
    catch (NullPointerException e)
    {
      _log.warning("There is errors in your Door.csv file. Update door.csv");
    }
    ForumsBBSManager.getInstance();
    _log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
    try {
      DynamicExtension.getInstance();
    } catch (Exception ex) {
      _log.log(Level.WARNING, "DynamicExtension could not be loaded and initialized", ex);
    }

    FloodProtector.getInstance();

    TvTManager.getInstance();

    Heroes.getInstance().engineInit();

    if (Config.USE_SAY_FILTER)
    {
      SayFilter.getInstance().load();
    }

    if (Config.L2TOP_ENABLED)
    {
      L2Top.getInstance();
    }
    if (Config.AUTO_ANNOUNCE_ALLOW) {
      AutoAnnounce.load();
    }
    AnnouncementsOnline.getInstance().loadMaxOnline();

    if (Config.pccafe_event) PcCafe.getInstance();

    System.gc();
    long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576L;
    long totalMem = Runtime.getRuntime().maxMemory() / 1048576L;
    _log.info("========================================================================");
    _log.info("Game Server started, free memory " + freeMem + " Mb of " + totalMem + " Mb");

    L2GamePacketHandler gph = new L2GamePacketHandler();
    SelectorConfig sc = new SelectorConfig(null, null, gph, gph);
    sc.setMaxSendPerPass(12);
    sc.setSelectorSleepTime(20);

    _selectorThread = new SelectorThread(sc, gph, gph, null);

    InetAddress bindAddress = null;
    if (!Config.GAMESERVER_HOSTNAME.equals("*"))
    {
      try
      {
        bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
      }
      catch (UnknownHostException e1)
      {
        _log.severe("WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage());
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
      _log.severe("FATAL: Failed to open server socket. Reason: " + e.getMessage());
      if (Config.DEVELOPER)
      {
        e.printStackTrace();
      }
      System.exit(1);
    }
    _selectorThread.start();
    _log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

    if ((Config.OFFLINE_TRADE_ENABLE) && (Config.RESTORE_OFFLINERS))
    {
      new Thread(new Runnable()
      {
        public void run() {
          Connection con = null;
          PreparedStatement statement = null;
          ResultSet result = null;
          int i = 0;
          try
          {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * from offline_traders;");
            result = statement.executeQuery();

            while (result.next())
            {
              i++;
              L2GameClient client = new L2GameClient(new MMOConnection(_selectorThread));
              client.setConnection(null);
              client.setCharSelection(Integer.valueOf(result.getInt("charId")));
              L2PcInstance p = client.loadCharFromDisk(0);
              if ((p == null) || (p.isDead()))
                continue;
              client.setAccountName(p.getAccountName());
              p.setClient(client);
              client.setActiveChar(p);
              LoginServerThread.getInstance().addAccount(client);

              client.stopAutoSave();

              p.setOnlineStatus(true);
              p.setOffline(true);
              p.spawnMe();
              p.restoreEffects();
              p.startAbnormalEffect(128);

              p.setPrivateStoreType(result.getInt("tradetype"));
              p.setOfflineStartTime(result.getLong("start_time"));

              TradeList tradeList = null;
              if (p.getPrivateStoreType() == 3)
              {
                tradeList = p.getBuyList();
              }
              else
              {
                tradeList = p.getSellList();
                tradeList.setPackaged(result.getInt("pakagesale") == 1);
              }
              tradeList.clear();
              if (!result.getString("msg").equals("null")) {
                tradeList.setTitle(result.getString("msg"));
              }
              PreparedStatement statement2 = con.prepareStatement("SELECT * FROM offline_traders_lists WHERE charId=" + result.getInt("charId"));
              ResultSet result2 = statement2.executeQuery();

              while (result2.next())
              {
                if (p.getPrivateStoreType() == 3)
                {
                  tradeList.addItemByItemId(result2.getInt("itemId"), result2.getInt("count"), result2.getInt("price"), result2.getInt("enchant")); continue;
                }

                tradeList.addItem(result2.getInt("objectId"), result2.getInt("count"), result2.getInt("price"));
              }

              result2.close();
              statement2.close();

              p.sitDown();
              RegionBBSManager.getInstance().changeCommunityBoard();
            }

            result.close();
            statement.close();

            statement = con.prepareStatement("Truncate table offline_traders;");
            statement.executeUpdate();
            statement.close();
            statement = con.prepareStatement("Truncate table offline_traders_lists;");
            statement.executeUpdate();
            statement.close();
          }
          catch (Exception e)
          {
            GameServer._log.warning("Could not restore offline_traders:" + e);
            e.printStackTrace();
          }
          finally {
            try {
              con.close(); } catch (Exception e) { e.printStackTrace(); }
          }
          GameServer._log.info("####### !!!!!!! Restored offline traders : ----> " + i);
          GameServer.access$202(GameServer.this, LoginServerThread.getInstance());
          _loginThread.start();
        }
      }).start();
    }
    else
    {
      _loginThread = LoginServerThread.getInstance();
      _loginThread.start();
    }
  }

  public static void main(String[] args)
    throws Exception
  {
    if (System.getProperty("os.name").startsWith("Windows"))
    {
      PrintStream ps = new PrintStream(System.out, true, "Cp866");
      System.setOut(ps);
      System.setErr(ps);
    }
    Server.serverMode = 1;
    String LOG_FOLDER = "logs";
    String LOG_NAME = "./config/log.cfg";
    File logFolder = new File(Config.DATAPACK_ROOT, "logs");
    logFolder.mkdir();
    InputStream is = new FileInputStream(new File("./config/log.cfg"));
    LogManager.getLogManager().readConfiguration(is);
    is.close();
    Config.load();
    L2DatabaseFactory.getInstance();
    gameServer = new GameServer();

    if (Config.IS_TELNET_ENABLED) {
      _statusServer = new Status(Server.serverMode);
      _statusServer.start();
    }
    else {
      System.out.println("Telnet server is currently disabled.");
    }
  }

  public static final int time() {
    return (int)(System.currentTimeMillis() / 1000L);
  }

  public static final int uptime() {
    return time() - _serverStarted;
  }
}