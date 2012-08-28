package net.sf.l2j.gameserver.model.actor.instance;

import java.lang.ref.WeakReference;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.Config.PvpColor;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2PartnerAI;
import net.sf.l2j.gameserver.ai.L2PlayerAI;
import net.sf.l2j.gameserver.ai.L2PlayerFakeAI;
import net.sf.l2j.gameserver.ai.L2PlayerFakeArcherAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.cache.WarehouseCacheManager;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.datatables.CustomServerData.DonateSkill;
import net.sf.l2j.gameserver.datatables.CustomServerData.Riddle;
import net.sf.l2j.gameserver.datatables.FishTable;
import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.NobleSkillTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.instancemanager.EventManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.FishData;
import net.sf.l2j.gameserver.model.ForceBuff;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Attackable.RewardItem;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Fishing;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Macro;
import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Radar;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.MacroList;
import net.sf.l2j.gameserver.model.PcFreight;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.PcWarehouse;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.ShortCuts;
import net.sf.l2j.gameserver.model.StatsChangeRecorder;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassLevel;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.Wedding;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadGame;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CameraMode;
import net.sf.l2j.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingStart;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMode;
import net.sf.l2j.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExSetCompassZoneCode;
import net.sf.l2j.gameserver.network.serverpackets.GMViewCharacterInfo;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ObservationMode;
import net.sf.l2j.gameserver.network.serverpackets.ObservationReturn;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.PetInventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreListBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreListSell;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopSellList;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.network.serverpackets.SkillList;
import net.sf.l2j.gameserver.network.serverpackets.Snoop;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.TitleUpdate;
import net.sf.l2j.gameserver.network.serverpackets.TradeStart;
import net.sf.l2j.gameserver.network.serverpackets.TradeStartOk;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Henna;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.gameserver.util.Moderator;
import net.sf.l2j.gameserver.util.PeaceZone;
import net.sf.l2j.gameserver.util.WebStat;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.Util;
import scripts.autoevents.encounter.Encounter;
import scripts.communitybbs.BB.Forum;
import scripts.communitybbs.Manager.ForumsBBSManager;
import scripts.items.IItemHandler;
import scripts.items.ItemHandler;

public final class L2PcInstance extends L2PlayableInstance
{
  public static final int PS_NONE = 0;
  public static final int PS_SELL = 1;
  public static final int PS_BUY = 3;
  public static final int PS_MANUFACTURE = 5;
  public static final int PS_PACKAGE_SELL = 8;
  private static final int[] GRADES = { SkillTreeTable.getInstance().getExpertiseLevel(0), SkillTreeTable.getInstance().getExpertiseLevel(1), SkillTreeTable.getInstance().getExpertiseLevel(2), SkillTreeTable.getInstance().getExpertiseLevel(3), SkillTreeTable.getInstance().getExpertiseLevel(4), SkillTreeTable.getInstance().getExpertiseLevel(5) };

  private static final int[] CC_LEVELS = { 5, 20, 28, 36, 43, 49, 55, 62 };
  private L2GameClient _client;
  private boolean _isConnected = true;
  private String _accountName;
  private long _deleteTimer;
  private boolean _isOnline = false;
  private long _onlineTime;
  private long _onlineBeginTime;
  private long _lastAccess;
  private long _uptime;
  protected int _baseClass;
  protected int _activeClass;
  protected int _classIndex = 0;
  private Map<Integer, SubClass> _subClasses;
  private PcAppearance _appearance;
  private int _charId = 199546;
  private long _expBeforeDeath;
  private int _karma;
  private int _pvpKills;
  private int _deaths;
  private int _pkKills;
  private byte _pvpFlag;
  private byte _siegeState = 0;
  private int _curWeightPenalty = 0;
  private int _lastCompassZone;
  private byte _zoneValidateCounter = 4;
  private boolean _isIn7sDungeon = false;

  private boolean _isInDangerArea = false;
  private boolean _isInSiegeFlagArea = false;
  private boolean _isInSiegeRuleArea = false;
  private boolean _isInOlumpiadStadium = false;
  private boolean _isInsideCastleWaitZone = false;
  private boolean _isInsideCastleZone = false;
  private boolean _isInsideHotZone = false;
  private boolean _isInsideDismountZone = false;
  private boolean _isInColiseumZone = false;
  private boolean _isInMotherElfZone = false;
  private boolean _isInBlockZone = false;
  private boolean _isInsideAqZone = false;
  private boolean _isInsideSilenceZone = false;
  private boolean _isInZakenZone = false;
  private boolean _InvullBuffs = false;
  private boolean _inJail = false;
  private long _jailTimer = 0L;
  private ScheduledFuture<?> _jailTask;
  private boolean _inOlympiadMode = false;
  private boolean _OlympiadStart = false;
  private boolean _OlympiadCountdown = false;
  private int _olympiadGameId = -1;
  private int _olympiadSide = -1;
  private boolean _inBoat;
  private L2BoatInstance _boat;
  private Point3D _inBoatPosition;
  private int _mountType;
  private int _mountObjectID = 0;
  public int _telemode = 0;
  public boolean _exploring = false;
  private boolean _isSilentMoving = false;
  private boolean _inCrystallize;
  private boolean _inCraftMode;
  private Map<Integer, L2RecipeList> _dwarvenRecipeBook = new FastMap();
  private Map<Integer, L2RecipeList> _commonRecipeBook = new FastMap();
  private boolean _waitTypeSitting;
  private boolean _relax;
  private int _obsX;
  private int _obsY;
  private int _obsZ;
  private int _observerMode = 0;

  private Point3D _lastClientPosition = new Point3D(0, 0, 0);
  private Point3D _lastServerPosition = new Point3D(0, 0, 0);
  private int _recomHave;
  private int _recomLeft;
  private long _lastRecomUpdate;
  private List<Integer> _recomChars = new FastList();

  private PcInventory _inventory = new PcInventory(this);
  private PcWarehouse _warehouse;
  private PcFreight _freight = new PcFreight(this);
  private int _privatestore;
  private TradeList _activeTradeList;
  private ItemContainer _activeWarehouse;
  private L2ManufactureList _createList;
  private TradeList _sellList;
  private TradeList _buyList;
  private boolean _newbie;
  private boolean _noble = false;
  private boolean _hero = false;

  private L2FolkInstance _lastFolkNpc = null;

  private int _questNpcObject = 0;

  private HashMap<String, QuestState> _quests = new HashMap();

  private ShortCuts _shortCuts = new ShortCuts(this);

  private MacroList _macroses = new MacroList(this);
  private List<L2PcInstance> _snoopListener = new FastList();
  private List<L2PcInstance> _snoopedPlayer = new FastList();
  private ClassId _skillLearningClassId;
  private final L2HennaInstance[] _henna = new L2HennaInstance[3];
  private int _hennaSTR;
  private int _hennaINT;
  private int _hennaDEX;
  private int _hennaMEN;
  private int _hennaWIT;
  private int _hennaCON;
  private L2Summon _summon = null;

  private L2TamedBeastInstance _tamedBeast = null;
  private L2Radar _radar;
  private final StatsChangeRecorder _statsChangeRecorder = new StatsChangeRecorder(this);
  private boolean _partyMatchingAutomaticRegistration;
  private boolean _partyMatchingShowLevel;
  private boolean _partyMatchingShowClass;
  private String _partyMatchingMemo;
  private int _clanId;
  private L2Clan _clan;
  private int _apprentice = 0;
  private int _sponsor = 0;
  private long _clanJoinExpiryTime;
  private long _clanCreateExpiryTime;
  private int _powerGrade = 0;
  private int _clanPrivileges = 0;

  private int _pledgeClass = 0;
  private int _pledgeType = 0;

  private int _lvlJoinedAcademy = 0;
  private int _wantsPeace = 0;

  private int _deathPenaltyBuffLevel = 0;
  private Point3D _currentSkillWorldPosition;
  private boolean _isGm;
  private int _accessLevel;
  private boolean _chatBanned = false;
  private long _banchat_timer = 0L;
  private ScheduledFuture _BanChatTask;
  private boolean _messageRefusal = false;
  private boolean _dietMode = false;
  private boolean _tradeRefusal = false;
  private boolean _exchangeRefusal = false;
  private L2Party _party;
  private L2PcInstance _activeRequester;
  private long _requestExpireTime = 0L;
  private L2ItemInstance _arrowItem;
  private L2PcInstance _currentTransactionRequester;
  public long _currentTransactionTimeout;
  private long _protectEndTime = 0L;

  private long _recentFakeDeathEndTime = 0L;
  private L2Weapon _fistsWeaponItem;
  private final HashMap<Integer, String> _chars = new HashMap();
  private int _expertiseIndex;
  private int _expertisePenalty = 0;
  private L2ItemInstance _activeEnchantItem = null;
  protected boolean _inventoryDisable = false;
  protected Map<Integer, L2CubicInstance> _cubics = new FastMap();

  protected Map<Integer, Integer> _activeSoulShots = new ConcurrentHashMap();
  public final ReentrantLock soulShotLock = new ReentrantLock();
  public int eventX;
  public int eventY;
  public int eventZ;
  public int eventkarma;
  public int eventpvpkills;
  public int eventpkkills;
  public String eventTitle;
  public LinkedList<String> kills = new LinkedList();
  public boolean eventSitForced = false;
  public boolean atEvent = false;

  private int[] _loto = new int[5];

  private int[] _race = new int[2];
  private final BlockList _blockList = new BlockList();
  private int _team = 0;

  private int _alliedVarkaKetra = 0;
  private L2Fishing _fishCombat;
  private boolean _fishing = false;
  private int _fishx = 0;
  private int _fishy = 0;
  private int _fishz = 0;
  private ScheduledFuture<?> _taskRentPet;
  private ScheduledFuture<?> _taskWater;
  private List<String> _validBypass = new FastList();
  private List<String> _validBypass2 = new FastList();
  private Forum _forumMail;
  private Forum _forumMemo;
  private SkillDat _currentSkill;
  private SkillDat _queuedSkill;
  private boolean _IsWearingFormalWear = false;
  private int _cursedWeaponEquipedId = 0;
  private int _reviveRequested = 0;
  private double _revivePower = 0.0D;
  private boolean _revivePet = false;
  private double _cpUpdateIncCheck = 0.0D;
  private double _cpUpdateDecCheck = 0.0D;
  private double _cpUpdateInterval = 0.0D;
  private double _mpUpdateIncCheck = 0.0D;
  private double _mpUpdateDecCheck = 0.0D;
  private double _mpUpdateInterval = 0.0D;
  private double _enterWorldCp = 0.0D;
  private double _enterWorldHp = 0.0D;
  private double _enterWorldMp = 0.0D;

  private int _herbstask = 0;

  private boolean _married = false;
  private int _partnerId = 0;
  private int _coupleId = 0;
  private boolean _engagerequest = false;
  private int _engageid = 0;
  private boolean _marryrequest = false;
  private boolean _marryaccepted = false;
  protected ForceBuff _forceBuff;
  private Duel _duel;
  private boolean _classUpdate = false;

  private L2PcInstance _olyEnemy = null;

  private long _engageTime = 0L;

  private boolean _freePvp = false;

  L2Summon fairy = null;

  TransactionType _currentTransactionType = TransactionType.NONE;
  private ScheduledFuture<?> _taskWarnUserTakeBreak;
  public ScheduledFuture<?> _taskforfish;
  private int _olympiadObserveId = -1;

  private int _waterZone = -1;

  private long _reviveTime = 0L;
  private FishData _fish;
  private L2ItemInstance _lure = null;

  private boolean _charmOfCourage = false;

  private Map<Integer, TimeStamp> _reuseTimeStamps = new ConcurrentHashMap();
  private boolean _pvpArena;
  private boolean _dinoIsle;
  private FastMap<Integer, FastMap<Integer, Integer>> _profiles = new FastMap().shared("L2PcInstance._profiles");
  private long _lastBuffProfile = 0L;

  private long _lastReload = 0L;
  public Collection<TimeStamp> _breuseTimeStamps;
  private long _lastPacket = 0L;

  private boolean _isDeleting = false;

  private long _EnterWorld = 0L;

  private long _requestGiveNickName = 0L;
  private int _titleChngedFail = 0;

  private long _cpa = 0L;
  private long _cpb = 0L;
  private long _cpc = 0L;
  private long _cpd = 0L;
  private long _cpe = 0L;
  private long _cpf = 0L;
  private long _cpg = 0L;
  private long _cph = 0L;
  private long _cpj = 0L;
  private long _cpk = 0L;
  private long _cpl = 0L;
  private long _cpm = 0L;
  private long _cpn = 0L;
  private long _cpo = 0L;
  private long _cpp = 0L;
  private long _cpq = 0L;
  private long _cpr = 0L;
  private long _cps = 0L;
  private long _cpt = 0L;
  private long _cpu = 0L;
  private long _cpw = 0L;
  private long _cpx = 0L;
  private long _cpv = 0L;
  private long _cpy = 0L;
  private long _cpz = 0L;
  private long _cpaa = 0L;
  private long _cpab = 0L;
  private long _cpac = 0L;
  private long _cpad = 0L;
  private long _cpae = 0L;
  private long _cpaf = 0L;
  private long _cpag = 0L;
  private long _cpah = 0L;
  private long _cpaj = 0L;
  private long _cpak = 0L;
  private long _cpal = 0L;
  private long _cpam = 0L;
  private long _cpan = 0L;
  private long _cpao = 0L;
  private long _cpap = 0L;
  private long _cpaq = 0L;
  private long _cpar = 0L;
  private long _cpas = 0L;
  private long _cpat = 0L;
  private long _cpau = 0L;
  private long _cpav = 0L;
  private long _cpaw = 0L;
  private long _cpax = 0L;
  private long _cpay = 0L;
  private long _cpaz = 0L;
  private long _cpaaa = 0L;
  private long _cpaab = 0L;
  private long _cpaac = 0L;
  private long _cpaad = 0L;
  private long _cpaae = 0L;
  private long _cpaaf = 0L;
  private long _cpaag = 0L;
  private long _cpaah = 0L;

  private boolean _equiptask = false;

  private boolean _antiWorldChat = false;

  private boolean _moder = false;
  private boolean _cmoder = true;
  private Location _lastOptiClientPosition;
  private Location _lastOptiServerPosition;
  private volatile long _fallingTimestamp = 0L;

  private FastMap<Integer, String> _friends = new FastMap().shared("L2PcInstance._friends");

  private Location _groundSkillLoc = null;

  private long _cpReuseTimeS = 0L;
  private long _cpReuseTimeB = 0L;

  private int _tradePartner = -1;
  private long _tradeStart = 0L;

  private int _destX = 0;
  private int _destY = 0;
  private int _destZ = 0;

  private int _vote1Item = 0;
  private int _vote2Item = 0;

  private int _voteEnch = 0;

  private L2Skill voteAugm = null;

  private int _sellIdStock = 0;
  private int _itemIdStock = 0;
  private int _enchantStock = 0;
  private int _augmentStock = 0;
  private int _auhLeveStock = 0;
  private int _objectIdStockI = 0;
  private int _enchantStockI = 0;

  private int _stockSelf = 0;

  private long _stockTime = 0L;

  private int _augSaleItem = 0;
  private int _augSaleId = 0;
  private int _augSaleLvl = 0;

  private boolean _inGame = false;

  private boolean _noExp = false;
  private boolean _lAlone = false;
  private boolean _autoLoot = false;
  private int _chatIgnore = 0;
  private boolean _tradersIgnore = false;
  private boolean _geoPathFind;
  private boolean _skillChances = false;

  private long _mpvplast = 0L;

  private boolean _eventWait = false;

  private String _voteRef = "no";

  private boolean _augFlag = false;

  private int _aquFlag = 0;

  private boolean _spyPacket = false;

  public Location _fakeLoc = null;

  private int _partnerClass = 0;
  private L2PcInstance _partner;
  private boolean _isPartner = false;
  private L2PcInstance _owner;
  private boolean _follow = true;

  private boolean _fantome = false;

  private boolean _spy = false;

  private boolean _bt = false;

  private int _fcObj = 0;
  private int _fcEnch = 0;
  private int _fcCount = 0;
  private int _fcAugm = 0;

  private boolean _fcBattle = false;

  private boolean _fcWait = false;

  private boolean _inEnch = false;

  private UserKey _userKey = new UserKey("", 0, 0);

  private boolean _antiSummon = false;

  private Lock wpnEquip = new ReentrantLock();
  private ScheduledFuture<?> _euipWeapon;
  private int _osTeam = 0;

  private boolean _havpwcs = false;
  private int _pwskill = 0;

  private boolean _premium = false;
  private long _premiumExpire = 0L;

  private long _heroExpire = 0L;

  private boolean _shdItems = false;

  private boolean _tvtPassive = true;

  private int _bbsMailItem = 0;
  private String _bbsMailSender = "n.a";
  private String _bbsMailTheme = "n.a";

  private long _lastPvPPk = 0L;

  public Map<Integer, Integer> _pvppk_penalties = new ConcurrentHashMap();

  private boolean _isInPpvFarm = false;

  private int _enchClicks = 0;

  private boolean _isInEncounterEvent = false;

  private int _eventColNumber = 0;

  private long quest_last_reward_time = 0L;

  private int _pcPoints = 0;

  private int _nextScroll = 0;

  private boolean _lookingForParty = false;

  private PartyWaitingRoomManager.WaitingRoom _partyRoom = null;

  private int _channel = 1;

  private Location _sfLoc = null;

  private int _sfRequest = 0;
  private long _sfTime = 0L;

  private boolean _offline = false;

  private Map<String, String> _offtrade_items = new ConcurrentHashMap();

  private int _lastSayCount = 0;
  private long _lastSayTime = 0L;
  private String _lastSayString = "";

  private int _activeAug = 0;

  private int _trans1item = 0;
  private int _trans2item = 0;
  private int _transAugId = 0;

  private L2Character _buffTarget = this;

  private boolean _showSoulshotsAnim = true;

  private long _lastTeleport = 0L;

  private int _accKickCount = 0;

  private int _fakeProtect = 0;

  private boolean _isHippy = false;

  public void startForceBuff(L2Character target, L2Skill skill)
  {
    if (!target.isPlayer()) {
      return;
    }

    if (skill.getSkillType() != L2Skill.SkillType.FORCE_BUFF) {
      return;
    }

    int forceId = 0;
    SystemMessage sm = null;

    if (skill.getId() == 426)
      forceId = 5104;
    else {
      forceId = 5105;
    }

    L2Effect force = target.getFirstEffect(forceId);
    if (force != null) {
      int forceLvl = force.getLevel();
      if (forceLvl < 3) {
        int newForceLvl = forceLvl + 1;
        force.exit();
        SkillTable.getInstance().getInfo(forceId, newForceLvl).getEffects(target, target);
        sm = SystemMessage.id(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(newForceLvl);
      } else {
        target.sendUserPacket(Static.YOUR_FORCE_IS_MAX);
      }
    } else {
      SkillTable.getInstance().getInfo(forceId, 1).getEffects(target, target);
      sm = SystemMessage.id(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(1);
    }

    target.sendUserPacket(sm);
    sm = null;
  }

  public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, boolean sex)
  {
    PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
    L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);

    player.setName(name);

    player.setBaseClass(player.getClassId());

    if (Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE) {
      player.setNewbie(true);
    }

    if (Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NOBLE) {
      player.setNoble(true);
    }

    boolean ok = player.createDb();

    if (Config.ALT_START_LEVEL > 0) {
      long pXp = player.getExp();
      long tXp = net.sf.l2j.gameserver.model.base.Experience.LEVEL[Config.ALT_START_LEVEL];
      player.addExpAndSp(tXp - pXp, 166903566);
    }
    FastMap.Entry e;
    if (!Config.CUSTOM_STRT_ITEMS.isEmpty()) {
      e = Config.CUSTOM_STRT_ITEMS.head(); for (FastMap.Entry end = Config.CUSTOM_STRT_ITEMS.tail(); (e = e.getNext()) != end; ) {
        Integer item_id = (Integer)e.getKey();
        Integer item_count = (Integer)e.getValue();
        if ((item_id == null) || (item_count == null))
        {
          continue;
        }
        player.getInventory().addItem("start_items", item_id.intValue(), item_count.intValue(), player, null);
      }
    }

    if (!ok) {
      return null;
    }

    return player;
  }

  public static L2PcInstance createDummyPlayer(int objectId, String name)
  {
    L2PcInstance player = new L2PcInstance(objectId);
    player.setName(name);

    return player;
  }

  public String getName()
  {
    if ((getChannel() == 6) && (Config.ELH_HIDE_NAMES)) {
      return Config.ELH_ALT_NAME;
    }

    if (isPremium()) {
      return new StringBuilder().append(super.getName()).append(Config.PREMIUM_NAME_PREFIX).toString();
    }

    return super.getName();
  }

  public String getRealName() {
    return super.getName();
  }

  public String getAccountName() {
    if (getClient() == null) {
      return "N/A";
    }

    return getClient().getAccountName();
  }

  public HashMap<Integer, String> getAccountChars() {
    return _chars;
  }

  public int getRelation(L2PcInstance target)
  {
    int result = 0;

    if (getPvpFlag() != 0) {
      result |= 2;
    }
    if (getKarma() > 0) {
      result |= 4;
    }

    if (isClanLeader()) {
      result |= 128;
    }

    if (getSiegeState() != 0) {
      result |= 512;
      if (getSiegeState() != target.getSiegeState())
        result |= 4096;
      else {
        result |= 2048;
      }
      if (getSiegeState() == 1) {
        result |= 1024;
      }
    }

    if ((getClan() != null) && (target.getClan() != null) && 
      (target.getPledgeType() != -1) && (target.getClan().isAtWarWith(getClan().getClanId())))
    {
      result |= 65536;
      if (getClan().isAtWarWith(target.getClan().getClanId())) {
        result |= 32768;
      }
    }

    return result;
  }

  public static L2PcInstance load(int objectId)
  {
    return restore(objectId);
  }

  private void initPcStatusUpdateValues() {
    _cpUpdateInterval = (getMaxCp() / 352.0D);
    _cpUpdateIncCheck = getMaxCp();
    _cpUpdateDecCheck = (getMaxCp() - _cpUpdateInterval);
    _mpUpdateInterval = (getMaxMp() / 352.0D);
    _mpUpdateIncCheck = getMaxMp();
    _mpUpdateDecCheck = (getMaxMp() - _mpUpdateInterval);
  }

  public L2PcInstance(int objectId, L2PcTemplate template)
  {
    super(objectId, template);
  }

  private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app) {
    super(objectId, template);
    getKnownList();
    getStat();
    getStatus();
    super.initCharStatusUpdateValues();
    initPcStatusUpdateValues();

    _accountName = accountName;
    _appearance = app;

    _ai = new L2PlayerAI(new AIAccessor());

    _radar = new L2Radar(this);

    getInventory().restore();
    if (!Config.WAREHOUSE_CACHE) {
      getWarehouse();
    }
    getFreight().restore();
  }

  private L2PcInstance(int objectId) {
    super(objectId, null);
    getKnownList();
    getStat();
    getStatus();
    super.initCharStatusUpdateValues();
    initPcStatusUpdateValues();
  }

  public final PcKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof PcKnownList))) {
      setKnownList(new PcKnownList(this));
    }
    return (PcKnownList)super.getKnownList();
  }

  public final PcStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof PcStat))) {
      setStat(new PcStat(this));
    }
    return (PcStat)super.getStat();
  }

  public final PcStatus getStatus()
  {
    if ((super.getStatus() == null) || (!(super.getStatus() instanceof PcStatus))) {
      setStatus(new PcStatus(this));
    }
    return (PcStatus)super.getStatus();
  }

  public final PcAppearance getAppearance() {
    return _appearance;
  }

  public final L2PcTemplate getBaseTemplate()
  {
    return CharTemplateTable.getInstance().getTemplate(_baseClass);
  }

  public final L2PcTemplate getTemplate()
  {
    return (L2PcTemplate)super.getTemplate();
  }

  public void setTemplate(ClassId newclass) {
    super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      if (_fantome) {
        if (_isPartner) {
          _ai = new L2PartnerAI(new AIAccessor());
          return _ai;
        }
        switch (getClassId().getId()) {
        case 92:
        case 102:
        case 109:
          _ai = new L2PlayerFakeArcherAI(new AIAccessor());
          break;
        default:
          _ai = new L2PlayerFakeAI(new AIAccessor());
          break;
        }
      } else {
        _ai = new L2PlayerAI(new AIAccessor());
      }

    }

    return _ai;
  }

  public void explore()
  {
    if (!_exploring) {
      return;
    }

    if (getMountType() == 2) {
      return;
    }

    int x = getX() + Rnd.nextInt(6000) - 3000;
    int y = getY() + Rnd.nextInt(6000) - 3000;

    getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, getZ(), calcHeading(x, y)));
  }

  public final int getLevel()
  {
    return getStat().getLevel();
  }

  public boolean isNewbie()
  {
    return _newbie;
  }

  public void setNewbie(boolean f)
  {
    _newbie = f;
  }

  public void setBaseClass(int baseClass) {
    _baseClass = baseClass;
  }

  public void setBaseClass(ClassId classId) {
    _baseClass = classId.ordinal();
  }

  public boolean isInStoreMode() {
    return getPrivateStoreType() > 0;
  }

  public boolean isInCraftMode()
  {
    return _inCraftMode;
  }

  public void isInCraftMode(boolean b) {
    _inCraftMode = b;
  }

  public void logout()
  {
    closeNetConnection();
  }

  public void kick()
  {
    if (_isDeleting) {
      return;
    }

    setOfflineMode(false);

    sendUserPacket(Static.ServerClose);
    deleteMe();
    _client = null;
    setConnected(false);
    broadcastUserInfo();
  }

  public L2RecipeList[] getCommonRecipeBook()
  {
    if (_commonRecipeBook == null) {
      return new L2RecipeList[0];
    }

    return (L2RecipeList[])_commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
  }

  public L2RecipeList[] getDwarvenRecipeBook()
  {
    return (L2RecipeList[])_dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
  }

  public void registerCommonRecipeList(L2RecipeList recipe)
  {
    _commonRecipeBook.put(Integer.valueOf(recipe.getId()), recipe);
  }

  public void registerDwarvenRecipeList(L2RecipeList recipe)
  {
    _dwarvenRecipeBook.put(Integer.valueOf(recipe.getId()), recipe);
  }

  public boolean hasRecipeList(int recipeId)
  {
    if (_dwarvenRecipeBook.containsKey(Integer.valueOf(recipeId))) {
      return true;
    }
    return _commonRecipeBook.containsKey(Integer.valueOf(recipeId));
  }

  public void unregisterRecipeList(int recipeId)
  {
    if (_dwarvenRecipeBook.containsKey(Integer.valueOf(recipeId)))
      _dwarvenRecipeBook.remove(Integer.valueOf(recipeId));
    else if (_commonRecipeBook.containsKey(Integer.valueOf(recipeId)))
      _commonRecipeBook.remove(Integer.valueOf(recipeId));
    else {
      _log.warning(new StringBuilder().append("Attempted to remove unknown RecipeList: ").append(recipeId).toString());
    }

    for (L2ShortCut sc : getAllShortCuts()) {
      if (sc == null)
      {
        continue;
      }
      if ((sc.getId() == recipeId) && (sc.getType() == 5))
        deleteShortCut(sc.getSlot(), sc.getPage());
    }
  }

  public int getLastQuestNpcObject()
  {
    return _questNpcObject;
  }

  public void setLastQuestNpcObject(int npcId) {
    _questNpcObject = npcId;
  }

  public QuestState getQuestState(String quest)
  {
    return (QuestState)_quests.get(quest);
  }

  public void setQuestState(QuestState qs)
  {
    _quests.put(qs.getQuestName(), qs);
  }

  public void delQuestState(String quest)
  {
    _quests.remove(quest);
  }

  private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state) {
    int len = questStateArray.length;
    QuestState[] tmp = new QuestState[len + 1];
    for (int i = 0; i < len; i++) {
      tmp[i] = questStateArray[i];
    }
    tmp[len] = state;
    return tmp;
  }

  public Quest[] getAllActiveQuests()
  {
    ArrayList quests = new ArrayList();

    for (QuestState qs : _quests.values()) {
      if ((qs.getQuest().getQuestIntId() >= 1999) || 
        ((qs.isCompleted()) && (!Config.DEVELOPER)) || (
        (!qs.isStarted()) && (!Config.DEVELOPER)))
      {
        continue;
      }
      quests.add(qs.getQuest());
    }

    return (Quest[])quests.toArray(new Quest[quests.size()]);
  }

  public QuestState[] getQuestsForAttacks(L2NpcInstance npc)
  {
    QuestState[] states = null;

    for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.MOBGOTATTACKED))
    {
      if (getQuestState(quest.getName()) == null)
        continue;
      if (states == null)
        states = new QuestState[] { getQuestState(quest.getName()) };
      else {
        states = addToQuestStateArray(states, getQuestState(quest.getName()));
      }

    }

    return states;
  }

  public QuestState[] getQuestsForKills(L2NpcInstance npc)
  {
    QuestState[] states = null;

    for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.MOBKILLED))
    {
      if (getQuestState(quest.getName()) == null)
        continue;
      if (states == null)
        states = new QuestState[] { getQuestState(quest.getName()) };
      else {
        states = addToQuestStateArray(states, getQuestState(quest.getName()));
      }

    }

    return states;
  }

  public QuestState[] getQuestsForTalk(int npcId)
  {
    QuestState[] states = null;

    Quest[] quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.QUEST_TALK);
    if (quests != null) {
      for (Quest quest : quests) {
        if (quest == null)
          continue;
        if (getQuestState(quest.getName()) != null) {
          if (states == null)
            states = new QuestState[] { getQuestState(quest.getName()) };
          else {
            states = addToQuestStateArray(states, getQuestState(quest.getName()));
          }
        }

      }

    }

    return states;
  }

  public QuestState processQuestEvent(String quest, String event) {
    QuestState retval = null;
    if (event == null) {
      event = "";
    }
    if (!_quests.containsKey(quest)) {
      return retval;
    }
    QuestState qs = getQuestState(quest);
    if ((qs == null) && (event.length() == 0)) {
      return retval;
    }
    if (qs == null) {
      Quest q = QuestManager.getInstance().getQuest(quest);
      if (q == null) {
        return retval;
      }
      qs = q.newQuestState(this);
    }
    if ((qs != null) && 
      (getLastQuestNpcObject() > 0)) {
      L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
      if (object == null) {
        return retval;
      }

      if ((object.isL2Npc()) && (isInsideRadius(object, 150, false, false))) {
        L2NpcInstance npc = (L2NpcInstance)object;
        QuestState[] states = getQuestsForTalk(npc.getNpcId());

        if (states != null) {
          for (QuestState state : states) {
            if ((state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId()) && (!qs.isCompleted())) {
              if (qs.getQuest().notifyEvent(event, npc, this)) {
                showQuestWindow(quest, qs.getStateId());
              }

              retval = qs;
            }
          }
          sendUserPacket(new QuestList());
        }
      }

    }

    return retval;
  }

  private void showQuestWindow(String questId, String stateId) {
    String content = HtmCache.getInstance().getHtm(new StringBuilder().append("data/jscript/quests/").append(questId).append("/").append(stateId).append(".htm").toString());
    if (content == null) {
      content = HtmCache.getInstance().getHtm(new StringBuilder().append("data/scripts/quests/").append(questId).append("/").append(stateId).append(".htm").toString());
    }
    if (content != null) {
      NpcHtmlMessage npcReply = NpcHtmlMessage.id(5);
      npcReply.setHtml(content);
      sendUserPacket(npcReply);
    }

    sendActionFailed();
  }

  public ShortCuts getShortCuts()
  {
    return _shortCuts;
  }

  public FastTable<L2ShortCut> getAllShortCuts() {
    return _shortCuts.getAllShortCuts();
  }

  public L2ShortCut getShortCut(int slot, int page)
  {
    return _shortCuts.getShortCut(slot, page);
  }

  public void registerShortCut(L2ShortCut shortcut)
  {
    _shortCuts.registerShortCut(shortcut);
  }

  public void deleteShortCut(int slot, int page)
  {
    _shortCuts.deleteShortCut(slot, page);
  }

  public void registerMacro(L2Macro macro)
  {
    _macroses.registerMacro(macro);
  }

  public void deleteMacro(int id)
  {
    _macroses.deleteMacro(id);
  }

  public MacroList getMacroses()
  {
    return _macroses;
  }

  public void setSiegeState(byte siegeState)
  {
    _siegeState = siegeState;
  }

  public byte getSiegeState()
  {
    return _siegeState;
  }

  public void setPvpFlag(int pvpFlag)
  {
    _pvpFlag = (byte)pvpFlag;
  }

  public byte getPvpFlag()
  {
    if (Config.FREE_PVP) {
      return 1;
    }
    return _pvpFlag;
  }

  public void updatePvPFlag(int value)
  {
    if ((Config.FREE_PVP) || (getPvpFlag() == value)) {
      return;
    }
    setPvpFlag(value);

    sendUserPacket(new UserInfo(this));

    if (getPet() != null) {
      sendUserPacket(new RelationChanged(getPet(), getRelation(this), false));
    }

    FastList players = getKnownList().getListKnownPlayers();
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      pc.sendPacket(new RelationChanged(this, getRelation(this), isAutoAttackable(pc)));
      if (getPet() != null) {
        pc.sendPacket(new RelationChanged(getPet(), getRelation(this), isAutoAttackable(pc)));
      }
    }
    players.clear();
    players = null;
    pc = null;
  }

  public void setDuel(Duel duel)
  {
    _duel = duel;
    broadcastPacket(new RelationChanged(this, getRelation(this), false));
  }

  public Duel getDuel()
  {
    return _duel;
  }

  public boolean isInDuel()
  {
    return _duel != null;
  }

  public void revalidateZone(boolean f)
  {
    if (getWorldRegion() == null) {
      return;
    }

    if (f) {
      _zoneValidateCounter = 4;
    } else {
      _zoneValidateCounter = (byte)(_zoneValidateCounter - 1);
      if (_zoneValidateCounter < 0)
        _zoneValidateCounter = 4;
      else {
        return;
      }
    }

    getWorldRegion().revalidateZones(this);

    if (isInsideZone(4)) {
      if (_lastCompassZone == 11) {
        return;
      }
      _lastCompassZone = 11;
      sendUserPacket(new ExSetCompassZoneCode(11));
    } else if (isInsidePvpZone()) {
      if (_lastCompassZone == 14) {
        return;
      }
      _lastCompassZone = 14;
      sendUserPacket(new ExSetCompassZoneCode(14));
    } else if (isIn7sDungeon()) {
      if (_lastCompassZone == 13) {
        return;
      }
      _lastCompassZone = 13;
      sendUserPacket(new ExSetCompassZoneCode(13));
    } else if (isInZonePeace()) {
      if (_lastCompassZone == 12) {
        return;
      }
      _lastCompassZone = 12;
      sendUserPacket(new ExSetCompassZoneCode(12));
    } else {
      if (_lastCompassZone == 15) {
        return;
      }
      if (_lastCompassZone == 11) {
        updatePvPStatus();
      }
      _lastCompassZone = 15;
      sendUserPacket(new ExSetCompassZoneCode(15));
    }
  }

  public boolean hasDwarvenCraft()
  {
    return getSkillLevel(172) >= 1;
  }

  public int getDwarvenCraft() {
    return getSkillLevel(172);
  }

  public boolean hasCommonCraft()
  {
    return getSkillLevel(1320) >= 1;
  }

  public int getCommonCraft() {
    return getSkillLevel(1320);
  }

  public int getPkKills()
  {
    return _pkKills;
  }

  public void setPkKills(int pkKills)
  {
    _pkKills = pkKills;
  }

  public long getDeleteTimer()
  {
    return _deleteTimer;
  }

  public void setDeleteTimer(long deleteTimer)
  {
    _deleteTimer = deleteTimer;
  }

  public int getCurrentLoad()
  {
    return _inventory.getTotalWeight();
  }

  public long getLastRecomUpdate()
  {
    return _lastRecomUpdate;
  }

  public void setLastRecomUpdate(long date) {
    _lastRecomUpdate = date;
  }

  public int getRecomHave()
  {
    return _recomHave;
  }

  protected void incRecomHave()
  {
    if (_recomHave < 255)
      _recomHave += 1;
  }

  public void setRecomHave(int value)
  {
    if (value > 255)
      _recomHave = 255;
    else if (value < 0)
      _recomHave = 0;
    else
      _recomHave = value;
  }

  public int getRecomLeft()
  {
    return _recomLeft;
  }

  protected void decRecomLeft()
  {
    if (_recomLeft > 0)
      _recomLeft -= 1;
  }

  public void giveRecom(L2PcInstance target)
  {
    if (Config.ALT_RECOMMEND) {
      Connect con = null;
      PreparedStatement st = null;
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)");
        st.setInt(1, getObjectId());
        st.setInt(2, target.getObjectId());
        st.execute();
      } catch (Exception e) {
        _log.warning(new StringBuilder().append("could not update char recommendations:").append(e).toString());
      } finally {
        Close.CS(con, st);
      }
    }
    target.incRecomHave();
    decRecomLeft();
    _recomChars.add(Integer.valueOf(target.getObjectId()));
  }

  public boolean canRecom(L2PcInstance target) {
    return !_recomChars.contains(Integer.valueOf(target.getObjectId()));
  }

  public void setExpBeforeDeath(long exp)
  {
    _expBeforeDeath = exp;
  }

  public long getExpBeforeDeath() {
    return _expBeforeDeath;
  }

  public int getKarma()
  {
    if (Config.FREE_PVP) {
      return 0;
    }
    return _karma;
  }

  public void setKarma(int karma)
  {
    if (karma < 0) {
      karma = 0;
    }

    if (_karma == karma) {
      return;
    }

    if (_partner != null) {
      _partner.setKarma(karma);
    }
    if ((_isPartner) && (_owner != null)) {
      _owner.setKarma(karma);
    }

    _karma = karma;

    if ((_karma == 0) && (karma > 0)) {
      for (L2Object object : getKnownList().getKnownObjects().values()) {
        if ((object == null) || (!object.isL2Guard()))
        {
          continue;
        }
        if (((L2GuardInstance)object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
          ((L2GuardInstance)object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
      }
    }
    else if ((_karma > 0) && (karma == 0))
    {
      if (getPvpFlag() != 0) {
        setPvpFlag(0);
      }
      setKarmaFlag(0);
    }

    sendChanges();
    sendUserPacket(SystemMessage.id(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO).addString(String.valueOf(_karma)));

    if (getPet() != null)
      sendUserPacket(new RelationChanged(getPet(), getRelation(this), isAutoAttackable(this)));
  }

  public int getMaxLoad()
  {
    int con = getCON();
    if (con < 1) {
      return 31000;
    }
    if (con > 59) {
      return 176000;
    }
    double baseLoad = Math.pow(1.029993928D, con) * 30495.627366000001D;
    return (int)calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null);
  }

  public int getExpertisePenalty() {
    return _expertisePenalty;
  }

  public int getWeightPenalty() {
    if (_dietMode) {
      return 0;
    }
    return _curWeightPenalty;
  }

  public void refreshOverloaded()
  {
    int maxLoad = getMaxLoad();
    if (maxLoad > 0) {
      setIsOverloaded(getCurrentLoad() > maxLoad);
      int weightproc = getCurrentLoad() * 1000 / maxLoad;
      int newWeightPenalty;
      int newWeightPenalty;
      if ((weightproc < 500) || (_dietMode)) {
        newWeightPenalty = 0;
      }
      else
      {
        int newWeightPenalty;
        if (weightproc < 666) {
          newWeightPenalty = 1;
        }
        else
        {
          int newWeightPenalty;
          if (weightproc < 800) {
            newWeightPenalty = 2;
          }
          else
          {
            int newWeightPenalty;
            if (weightproc < 1000)
              newWeightPenalty = 3;
            else
              newWeightPenalty = 4; 
          }
        }
      }
      if (_curWeightPenalty != newWeightPenalty) {
        _curWeightPenalty = newWeightPenalty;
        if ((newWeightPenalty > 0) && (!_dietMode))
          super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
        else {
          super.removeSkill(getKnownSkill(4270));
        }

        sendEtcStatusUpdate();
      }
    }
  }

  public void setClassUpdate(boolean flag)
  {
    _classUpdate = flag;
  }

  public void refreshExpertisePenalty() {
    if (_classUpdate) {
      return;
    }

    int newPenalty = 0;

    for (L2ItemInstance item : getInventory().getItems()) {
      if ((item != null) && (item.isEquipped())) {
        int crystaltype = item.getItem().getCrystalType();

        if (crystaltype > newPenalty) {
          newPenalty = crystaltype;
        }
      }
    }

    newPenalty -= getExpertiseIndex();

    if (newPenalty <= 0) {
      newPenalty = 0;
    }

    if (getExpertisePenalty() != newPenalty) {
      _expertisePenalty = newPenalty;

      if (newPenalty > 0)
        super.addSkill(SkillTable.getInstance().getInfo(4267, 1));
      else {
        super.removeSkill(getKnownSkill(4267));
      }

      sendEtcStatusUpdate();
    }
  }

  public void checkIfWeaponIsAllowed()
  {
    if (isGM()) {
      return;
    }

    FastTable effects = getAllEffectsTable();
    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect effect = (L2Effect)effects.get(i);
      if (effect == null)
      {
        continue;
      }
      L2Skill effectSkill = effect.getSkill();

      if ((effectSkill.isOffensive()) || ((effectSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY) && (effectSkill.getSkillType() == L2Skill.SkillType.BUFF)))
        continue;
      if (effectSkill.getWeaponDependancy(this))
        continue;
      sendUserPacket(Static.WRONG_WEAPON);

      effect.exit();
    }
  }

  public void useEquippableItem(L2ItemInstance item, boolean abortAttack)
  {
    if (isCastingNow()) {
      return;
    }

    SystemMessage sm = null;
    L2ItemInstance[] items = null;
    int type2 = item.getItem().getType2();
    boolean isEquiped = item.isEquipped();
    L2ItemInstance old = getInventory().getPaperdollItem(14);

    if (old == null) {
      old = getInventory().getPaperdollItem(7);
    }

    if ((old != null) && (old.getItem().getType2() == 0)) {
      old.setChargedSoulshot(0);
      old.setChargedSpiritshot(0);
    }

    if (isEquiped) {
      if (item.getEnchantLevel() > 0)
        sm = SystemMessage.id(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
      else {
        sm = SystemMessage.id(SystemMessageId.S1_DISARMED).addItemName(item.getItemId());
      }
      sendUserPacket(sm);

      if (type2 == 0) {
        item.setChargedSoulshot(0);
        item.setChargedSpiritshot(0);
      }
      int slot = getInventory().getSlotFromItem(item);

      items = getInventory().unEquipItemInBodySlotAndRecord(slot);
    } else {
      int tempBodyPart = item.getItem().getBodyPart();
      L2ItemInstance tempItem = getInventory().getPaperdollItemByL2ItemId(tempBodyPart);

      if ((tempItem != null) && (tempItem.isWear()))
        return;
      if (tempBodyPart == 16384)
      {
        tempItem = getInventory().getPaperdollItem(7);
        if ((tempItem != null) && (tempItem.isWear())) {
          return;
        }

        tempItem = getInventory().getPaperdollItem(8);
        if ((tempItem != null) && (tempItem.isWear()))
          return;
      }
      else if (tempBodyPart == 32768)
      {
        tempItem = getInventory().getPaperdollItem(10);
        if ((tempItem != null) && (tempItem.isWear())) {
          return;
        }

        tempItem = getInventory().getPaperdollItem(11);
        if ((tempItem != null) && (tempItem.isWear())) {
          return;
        }
      }

      if (item.getEnchantLevel() > 0)
        sm = SystemMessage.id(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
      else {
        sm = SystemMessage.id(SystemMessageId.S1_EQUIPPED).addItemName(item.getItemId());
      }
      sendUserPacket(sm);

      items = getInventory().equipItemAndRecord(item);
      if (type2 == 0) {
        rechargeAutoSoulShot(true, false, false);
        rechargeAutoSoulShot(false, true, false);
      }

      if (item.isShadowItem()) {
        sendCritMessage(new StringBuilder().append(item.getItemName()).append(": \u043E\u0441\u0442\u0430\u043B\u043E\u0441\u044C ").append(item.getMana()).append(" \u043C\u0438\u043D\u0443\u0442.").toString());
      }
      item.decreaseMana(true);
    }
    sm = null;

    refreshExpertisePenalty();

    if (type2 == 0) {
      checkIfWeaponIsAllowed();
    }

    InventoryUpdate iu = new InventoryUpdate();
    iu.addItems(Arrays.asList(items));
    sendUserPacket(iu);

    if (abortAttack) {
      abortAttack();
    }

    broadcastUserInfo();
  }

  public int getPvpKills()
  {
    return _pvpKills;
  }

  public int getDeaths() {
    return _deaths;
  }

  public void setInvullBuffs(boolean f) {
    _InvullBuffs = f;
  }

  public boolean isInvullBuffs() {
    return _InvullBuffs;
  }

  public void setPvpKills(int pvpKills)
  {
    _pvpKills = pvpKills;
  }

  public void setDeaths(int deaths) {
    _deaths = deaths;
  }

  public ClassId getClassId()
  {
    return getTemplate().classId;
  }

  public void setClassId(int Id)
  {
    if (Config.ACADEMY_CLASSIC) {
      rewardAcademy(Id);
    }

    if (isSubClassActive()) {
      ((SubClass)getSubClasses().get(Integer.valueOf(_classIndex))).setClassId(Id);
    }

    broadcastPacket(new MagicSkillUser(this, this, 5103, 1, 1000, 0));

    sendUserPacket(new PlaySound("ItemSound.quest_fanfare_2"));

    setClassTemplate(Id);
    refreshExpertisePenalty();
  }

  public void rewardAcademy(int Id)
  {
    if ((getLvlJoinedAcademy() != 0) && (_clan != null) && (!isSubClassActive()) && ((!Config.ACADEMY_CLASSIC) || (PlayerClass.values()[Id].getLevel() == ClassLevel.Third))) {
      if (Config.ACADEMY_CLASSIC) {
        if (getLvlJoinedAcademy() <= 16)
          _clan.addPoints(Config.ACADEMY_POINTS);
        else if (getLvlJoinedAcademy() >= 39)
          _clan.addPoints((int)(Config.ACADEMY_POINTS * 0.4D));
        else
          _clan.addPoints(Config.ACADEMY_POINTS - (getLvlJoinedAcademy() - 16) * 10);
      }
      else {
        _clan.addPoints(Config.ACADEMY_POINTS);
      }

      _clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
      setLvlJoinedAcademy(0);

      _clan.broadcastToOnlineMembers(SystemMessage.id(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(getName()));
      _clan.broadcastToOnlineMembers(SystemMessage.id(SystemMessageId.CLAN_ACQUIRED_CONTESTED_CLAN_HALL_AND_S1_REPUTATION_POINTS).addNumber(Config.ACADEMY_POINTS));
      _clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
      _clan.removeClanMember(getName(), 0L);
      sendUserPacket(Static.ACADEMY_MEMBERSHIP_TERMINATED);

      addItem("Gift", 8181, 1, this, true);
    }
  }

  public long getExp()
  {
    return getStat().getExp();
  }

  public void setActiveEnchantItem(L2ItemInstance scroll) {
    _activeEnchantItem = scroll;
  }

  public L2ItemInstance getActiveEnchantItem() {
    return _activeEnchantItem;
  }

  public void setFistsWeaponItem(L2Weapon weaponItem)
  {
    _fistsWeaponItem = weaponItem;
  }

  public L2Weapon getFistsWeaponItem()
  {
    return _fistsWeaponItem;
  }

  public L2Weapon findFistsWeaponItem(int classId)
  {
    L2Weapon weaponItem = null;
    if ((classId >= 0) && (classId <= 9))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(246);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 10) && (classId <= 17))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(251);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 18) && (classId <= 24))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(244);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 25) && (classId <= 30))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(249);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 31) && (classId <= 37))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(245);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 38) && (classId <= 43))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(250);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 44) && (classId <= 48))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(248);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 49) && (classId <= 52))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(252);
      weaponItem = (L2Weapon)temp;
    } else if ((classId >= 53) && (classId <= 57))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(247);
      weaponItem = (L2Weapon)temp;
    }

    return weaponItem;
  }

  public void rewardSkills()
  {
    if (_fantome) {
      return;
    }

    SkillTable st = SkillTable.getInstance();

    int lvl = getLevel();

    if (lvl == 10) {
      removeSkill(st.getInfo(194, 1));
    }

    if (Config.DISABLE_GRADE_PENALTY) {
      setExpertiseIndex(5);
      addSkill(st.getInfo(239, 5), true);
    }
    else {
      for (int i = 0; i < GRADES.length; i++) {
        if (lvl >= GRADES[i]) {
          setExpertiseIndex(i);
        }
      }

      if (getExpertiseIndex() > 0) {
        addSkill(st.getInfo(239, getExpertiseIndex()), true);
      }

    }

    if ((getSkillLevel(1321) < 1) && (getRace() == Race.dwarf)) {
      addSkill(st.getInfo(1321, 1), true);
    }

    if (getSkillLevel(1322) < 1) {
      addSkill(st.getInfo(1322, 1), true);
    }

    for (int i = 0; i < CC_LEVELS.length; i++) {
      if ((lvl >= CC_LEVELS[i]) && (getSkillLevel(1320) < i + 1)) {
        addSkill(st.getInfo(1320, i + 1), true);
      }

    }

    if (Config.AUTO_LEARN_SKILLS) {
      giveAvailableSkills();
    }

    sendSkillList();

    refreshOverloaded();
    refreshExpertisePenalty();
  }

  private void regiveTemporarySkills()
  {
    if (isNoble()) {
      setNoble(true);
    }

    if (isHero()) {
      setHero(true);
    }

    if (getClan() != null) {
      if (getClan().getReputationScore() >= 0) {
        L2Skill[] skills = getClan().getAllSkills();
        for (L2Skill sk : skills) {
          if (sk.getMinPledgeClass() <= getPledgeClass()) {
            addSkill(sk, false);
          }
        }
      }

      if ((getClan().getLevel() > 3) && (isClanLeader())) {
        SiegeManager.getInstance().addSiegeSkills(this);
      }
    }
    checkDonateSkills();

    L2ItemInstance wpn = getInventory().getPaperdollItem(7);
    if (wpn == null) {
      wpn = getInventory().getPaperdollItem(14);
    }
    if ((wpn != null) && 
      (wpn.isAugmented())) {
      wpn.getAugmentation().applyBoni(this);
    }

    getInventory().reloadEquippedItems();
  }

  private void giveAvailableSkills()
  {
    int unLearnable = 0;
    int skillCounter = 0;

    L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
    while (skills.length > unLearnable) {
      for (int i = 0; i < skills.length; i++) {
        L2SkillLearn s = skills[i];
        L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
        if ((sk == null) || (!sk.getCanLearn(getClassId()))) {
          unLearnable++;
        }
        else
        {
          if (getSkillLevel(sk.getId()) == -1) {
            skillCounter++;
          }

          addSkill(sk, true);
        }
      }

      skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
    }

    sendMessage(new StringBuilder().append("\u041F\u043E\u043B\u0443\u0447\u0435\u043D\u043E ").append(skillCounter).append(" \u043D\u043E\u0432\u044B\u0445 \u0441\u043A\u0438\u043B\u043B\u043E\u0432.").toString());
  }

  public void setExp(long exp)
  {
    getStat().setExp(exp);
  }

  public Race getRace()
  {
    if (!isSubClassActive()) {
      return getTemplate().race;
    }

    L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(_baseClass);
    return charTemp.race;
  }

  public L2Radar getRadar() {
    return _radar;
  }

  public int getSp()
  {
    return getStat().getSp();
  }

  public void setSp(int sp)
  {
    super.getStat().setSp(sp);
  }

  public boolean isCastleLord(int castleId)
  {
    L2Clan clan = getClan();

    if ((clan != null) && (clan.getLeader().getPlayerInstance() == this))
    {
      Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
      if ((castle != null) && (castle == CastleManager.getInstance().getCastleById(castleId))) {
        return true;
      }
    }

    return false;
  }

  public int getClanId()
  {
    if ((getChannel() == 6) && (Config.ELH_HIDE_NAMES)) {
      return 0;
    }

    return _clanId;
  }

  public int getClanCrestId()
  {
    if ((_clan != null) && (_clan.hasCrest())) {
      return _clan.getCrestId();
    }

    return 0;
  }

  public int getClanCrestLargeId()
  {
    if ((_clan != null) && (_clan.hasCrestLarge())) {
      return _clan.getCrestLargeId();
    }

    return 0;
  }

  public long getClanJoinExpiryTime() {
    return _clanJoinExpiryTime;
  }

  public void setClanJoinExpiryTime(long time) {
    _clanJoinExpiryTime = time;
  }

  public long getClanCreateExpiryTime() {
    return _clanCreateExpiryTime;
  }

  public void setClanCreateExpiryTime(long time) {
    _clanCreateExpiryTime = time;
  }

  public void setOnlineTime(long time) {
    _onlineTime = time;
    _onlineBeginTime = System.currentTimeMillis();
  }

  public PcInventory getInventory()
  {
    return _inventory;
  }

  public PcInventory getPcInventory()
  {
    return _inventory;
  }

  public void removeItemFromShortCut(int objectId)
  {
    _shortCuts.deleteShortCutByObjectId(objectId);
  }

  public boolean isSitting()
  {
    return _waitTypeSitting;
  }

  public void setIsSitting(boolean f)
  {
    _waitTypeSitting = f;
  }

  public void sitDown()
  {
    if ((isCastingNow()) && (!_relax)) {
      sendUserPacket(Static.CANT_SET_WHILE_CAST);
      return;
    }

    if ((!_waitTypeSitting) && (!isAttackingDisabled()) && (!isOutOfControl()) && (!isImobilised())) {
      breakAttack();
      setIsSitting(true);
      broadcastPacket(new ChangeWaitType(this, 0));

      ThreadPoolManager.getInstance().scheduleAi(new SitDownTask(this), 2500L, true);
      setIsParalyzed(true);
    }
  }

  public void standUp()
  {
    if ((L2Event.active) && (eventSitForced)) {
      sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
    } else if ((_waitTypeSitting) && (!isInStoreMode()) && (!isAlikeDead())) {
      if (_relax) {
        setRelax(false);
        stopEffects(L2Effect.EffectType.RELAXING);
      }

      broadcastPacket(new ChangeWaitType(this, 1));

      ThreadPoolManager.getInstance().scheduleAi(new StandUpTask(this), 2500L, true);
    }
  }

  public void setRelax(boolean f)
  {
    _relax = f;
  }

  public PcWarehouse getWarehouse()
  {
    if (_warehouse == null) {
      _warehouse = new PcWarehouse(this);
      _warehouse.restore();
    }
    if (Config.WAREHOUSE_CACHE) {
      WarehouseCacheManager.getInstance().addCacheTask(this);
    }
    return _warehouse;
  }

  public void clearWarehouse()
  {
    if (_warehouse != null) {
      _warehouse.deleteMe();
    }
    _warehouse = null;
  }

  public PcFreight getFreight()
  {
    return _freight;
  }

  public int getCharId()
  {
    return _charId;
  }

  public void setCharId(int charId)
  {
    _charId = charId;
  }

  public int getAdena()
  {
    return _inventory.getAdena();
  }

  public int getAncientAdena()
  {
    return _inventory.getAncientAdena();
  }

  public void addAdena(String process, int count, L2Object reference, boolean sendMessage)
  {
    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.EARNED_ADENA).addNumber(count));
    }

    if (count > 0) {
      _inventory.addAdena(process, count, this, reference);

      if (!Config.FORCE_INVENTORY_UPDATE) {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(_inventory.getAdenaInstance());
        sendUserPacket(iu);
      } else {
        sendItems(false);
      }
    }
  }

  public boolean reduceAdena(String process, int count, L2Object reference, boolean sendMessage)
  {
    if (count > getAdena()) {
      if (sendMessage) {
        sendUserPacket(Static.YOU_NOT_ENOUGH_ADENA);
      }
      return false;
    }

    if (count > 0) {
      L2ItemInstance adenaItem = _inventory.getAdenaInstance();
      _inventory.reduceAdena(process, count, this, reference);

      if (!Config.FORCE_INVENTORY_UPDATE) {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(adenaItem);
        sendUserPacket(iu);
      } else {
        sendItems(false);
      }

      if (sendMessage) {
        sendUserPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ADENA).addNumber(count));
      }
    }

    return true;
  }

  public void addAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
  {
    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(5575).addNumber(count));
    }

    if (count > 0) {
      _inventory.addAncientAdena(process, count, this, reference);

      if (!Config.FORCE_INVENTORY_UPDATE) {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(_inventory.getAncientAdenaInstance());
        sendUserPacket(iu);
      } else {
        sendItems(false);
      }
    }
  }

  public boolean reduceAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
  {
    if (count > getAncientAdena()) {
      if (sendMessage) {
        sendUserPacket(Static.YOU_NOT_ENOUGH_ADENA);
      }

      return false;
    }

    if (count > 0) {
      L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
      _inventory.reduceAncientAdena(process, count, this, reference);

      if (!Config.FORCE_INVENTORY_UPDATE) {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(ancientAdenaItem);
        sendUserPacket(iu);
      } else {
        sendItems(false);
      }

      if (sendMessage) {
        sendUserPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addNumber(count).addItemName(5575));
      }
    }

    return true;
  }

  public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
  {
    L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
    if (template == null) {
      return;
    }

    if (item.getCount() > 0)
    {
      if (sendMessage) {
        SystemMessage sm = null;
        if (item.getCount() > 1)
          sm = SystemMessage.id(SystemMessageId.YOU_PICKED_UP_S1_S2).addItemName(item.getItemId()).addNumber(item.getCount());
        else if (item.getEnchantLevel() > 0)
          sm = SystemMessage.id(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
        else {
          sm = SystemMessage.id(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item.getItemId());
        }

        sendUserPacket(sm);
        sm = null;
      }

      L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);

      if (!Config.FORCE_INVENTORY_UPDATE) {
        InventoryUpdate playerIU = new InventoryUpdate();
        playerIU.addItem(newitem);
        sendUserPacket(playerIU);
      } else {
        sendItems(false);
      }

      sendChanges();

      if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId())) {
        CursedWeaponsManager.getInstance().activate(this, newitem);
      }

      if ((!isGM()) && (!_inventory.validateCapacity(0)))
        dropItem("InvDrop", newitem, null, true);
    }
  }

  public void addItem(String process, int itemId, int count, L2Object reference, boolean sendMessage)
  {
    if (ItemTable.getInstance().getTemplate(itemId) == null) {
      return;
    }

    if (count > 0)
    {
      if (sendMessage) {
        SystemMessage sm = null;
        if (count > 1) {
          if ((process.equalsIgnoreCase("sweep")) || (process.equalsIgnoreCase("Quest")))
            sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(count);
          else {
            sm = SystemMessage.id(SystemMessageId.YOU_PICKED_UP_S1_S2).addItemName(itemId).addNumber(count);
          }
        }
        else if ((process.equalsIgnoreCase("sweep")) || (process.equalsIgnoreCase("Quest")))
          sm = SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(itemId);
        else {
          sm = SystemMessage.id(SystemMessageId.YOU_PICKED_UP_S1).addItemName(itemId);
        }

        sendUserPacket(sm);
        sm = null;
      }

      L2ItemInstance item = _inventory.addItem(process, itemId, count, this, reference);

      if (!Config.FORCE_INVENTORY_UPDATE) {
        InventoryUpdate playerIU = new InventoryUpdate();
        playerIU.addItem(item);
        sendUserPacket(playerIU);
      } else {
        sendItems(false);
      }

      sendChanges();

      if (CursedWeaponsManager.getInstance().isCursed(itemId)) {
        CursedWeaponsManager.getInstance().activate(this, item);
      }

      if ((!isGM()) && (!_inventory.validateCapacity(0)))
        dropItem("InvDrop", item, null, true);
    }
  }

  public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
  {
    int oldCount = item.getCount();
    item = _inventory.destroyItem(process, item, this, reference);

    if (item == null) {
      if (sendMessage) {
        sendUserPacket(Static.NOT_ENOUGH_ITEMS);
      }

      return false;
    }

    if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendUserPacket(playerIU);
    } else {
      sendItems(false);
    }

    refreshOverloaded();

    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addNumber(oldCount).addItemName(item.getItemId()));
    }

    return true;
  }

  public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.getItemByObjectId(objectId);

    if ((item == null) || (item.getCount() < count) || (_inventory.destroyItem(process, objectId, count, this, reference) == null)) {
      if (sendMessage) {
        sendUserPacket(Static.NOT_ENOUGH_ITEMS);
      }

      return false;
    }

    if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendUserPacket(playerIU);
    } else {
      sendItems(false);
    }

    refreshOverloaded();

    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addNumber(count).addItemName(item.getItemId()));
    }
    return true;
  }

  public boolean destroyItemWithoutTrace(String process, int objectId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.getItemByObjectId(objectId);

    if ((item == null) || (item.getCount() < count)) {
      if (sendMessage) {
        sendUserPacket(Static.NOT_ENOUGH_ITEMS);
      }
      return false;
    }

    if (item.getCount() > count) {
      synchronized (item) {
        item.changeCountWithoutTrace(process, -count, this, reference);
        item.setLastChange(2);

        if (GameTimeController.getGameTicks() % 10 == 0) {
          item.updateDatabase();
        }
        _inventory.refreshWeight();
      }
    }
    else {
      _inventory.destroyItem(process, item, this, reference);
    }

    if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendUserPacket(playerIU);
    } else {
      sendItems(false);
    }

    sendChanges();

    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addNumber(count).addItemName(item.getItemId()));
    }
    return true;
  }

  public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.getItemByItemId(itemId);

    if ((item == null) || (item.getCount() < count) || (_inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)) {
      if (sendMessage) {
        sendUserPacket(Static.NOT_ENOUGH_ITEMS);
      }

      return false;
    }

    if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendUserPacket(playerIU);
    } else {
      sendItems(false);
    }

    sendChanges();

    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.DISSAPEARED_ITEM).addNumber(count).addItemName(itemId));
    }

    return true;
  }

  public void destroyWearedItems(String process, L2Object reference, boolean sendMessage)
  {
    for (L2ItemInstance item : getInventory().getItems())
    {
      if (item.isWear()) {
        if (item.isEquipped()) {
          getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
        }

        if (_inventory.destroyItem(process, item, this, reference) == null) {
          _log.warning(new StringBuilder().append("Player ").append(getName()).append(" can't destroy weared item: ").append(item.getName()).append("[ ").append(item.getObjectId()).append(" ]").toString());
        }
        else
        {
          sendUserPacket(SystemMessage.id(SystemMessageId.S1_DISARMED).addItemName(item.getItemId()));
        }

      }

    }

    sendChanges();

    sendItems(true);

    broadcastUserInfo();

    sendUserPacket(Static.TRY_ON_ENDED);
  }

  public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2Object reference)
  {
    L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
    if (oldItem == null) {
      return null;
    }
    L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
    if (newItem == null) {
      return null;
    }

    sendItems(false);

    sendChanges();

    if ((target instanceof PcInventory)) {
      L2PcInstance targetPlayer = ((PcInventory)target).getOwner();

      if (!Config.FORCE_INVENTORY_UPDATE) {
        InventoryUpdate playerIU = new InventoryUpdate();

        if (newItem.getCount() > count)
          playerIU.addModifiedItem(newItem);
        else {
          playerIU.addNewItem(newItem);
        }

        targetPlayer.sendUserPacket(playerIU);
      } else {
        targetPlayer.sendItems(false);
      }

      targetPlayer.sendChanges();
    } else if ((target instanceof PetInventory)) {
      PetInventoryUpdate petIU = new PetInventoryUpdate();

      if (newItem.getCount() > count)
        petIU.addModifiedItem(newItem);
      else {
        petIU.addNewItem(newItem);
      }

      ((PetInventory)target).getOwner().getOwner().sendUserPacket(petIU);
    }

    return newItem;
  }

  public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
  {
    item = _inventory.dropItem(process, item, this, reference);

    if (item == null) {
      if (sendMessage) {
        sendUserPacket(Static.NOT_ENOUGH_ITEMS);
      }

      return false;
    }

    item.dropMe(this, getX() + Rnd.get(50) - 25, getY() + Rnd.get(50) - 25, getZ() + 20);

    if ((Config.AUTODESTROY_ITEM_AFTER > 0) && (Config.DESTROY_DROPPED_PLAYER_ITEM) && (!Config.LIST_PROTECTED_ITEMS.contains(Integer.valueOf(item.getItemId()))) && (
      ((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)) || (!item.isEquipable()))) {
      ItemsAutoDestroy.getInstance().addItem(item);
    }

    if (Config.DESTROY_DROPPED_PLAYER_ITEM) {
      if ((!item.isEquipable()) || ((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)))
        item.setProtected(false);
      else
        item.setProtected(true);
    }
    else {
      item.setProtected(true);
    }

    if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendUserPacket(playerIU);
    } else {
      sendItems(false);
    }

    sendChanges();

    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.YOU_DROPPED_S1).addItemName(item.getItemId()));
    }
    return true;
  }

  public L2ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
    L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);

    if (item == null) {
      if (sendMessage) {
        sendUserPacket(Static.NOT_ENOUGH_ITEMS);
      }

      return null;
    }

    item.dropMe(this, x, y, z);

    if ((Config.AUTODESTROY_ITEM_AFTER > 0) && (Config.DESTROY_DROPPED_PLAYER_ITEM) && (!Config.LIST_PROTECTED_ITEMS.contains(Integer.valueOf(item.getItemId()))) && (
      ((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)) || (!item.isEquipable()))) {
      ItemsAutoDestroy.getInstance().addItem(item);
    }

    if (Config.DESTROY_DROPPED_PLAYER_ITEM) {
      if ((!item.isEquipable()) || ((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)))
        item.setProtected(false);
      else
        item.setProtected(true);
    }
    else {
      item.setProtected(true);
    }

    if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(invitem);
      sendUserPacket(playerIU);
    } else {
      sendItems(false);
    }

    sendChanges();

    if (sendMessage) {
      sendUserPacket(SystemMessage.id(SystemMessageId.YOU_DROPPED_S1).addItemName(item.getItemId()));
    }

    return item;
  }

  public L2ItemInstance checkItemManipulation(int objectId, int count, String action)
  {
    if (L2World.getInstance().findObject(objectId) == null) {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" item not available in L2World").toString());
      return null;
    }

    L2ItemInstance item = getInventory().getItemByObjectId(objectId);

    if ((item == null) || (item.getOwnerId() != getObjectId())) {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" item he is not owner of").toString());
      return null;
    }

    if ((count < 0) || ((count > 1) && (!item.isStackable()))) {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" item with invalid count: ").append(count).toString());
      return null;
    }

    if (count > item.getCount()) {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" more items than he owns").toString());
      return null;
    }

    if (((getPet() != null) && (getPet().getControlItemId() == objectId)) || (getMountObjectID() == objectId))
    {
      return null;
    }

    if ((getActiveEnchantItem() != null) && (getActiveEnchantItem().getObjectId() == objectId))
    {
      return null;
    }

    if (item.isWear())
    {
      return null;
    }

    return item;
  }

  public void setProtection(boolean f)
  {
    _protectEndTime = (f ? GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * 10 : 0L);
  }

  public void setRecentFakeDeath(boolean f)
  {
    _recentFakeDeathEndTime = (f ? GameTimeController.getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * 10 : 0L);
  }

  public boolean isRecentFakeDeath() {
    return _recentFakeDeathEndTime > GameTimeController.getGameTicks();
  }

  public L2GameClient getClient()
  {
    return _client;
  }

  public void setClient(L2GameClient client) {
    _client = client;
  }

  public void closeNetConnection()
  {
    if (_client != null)
      _client.close(Static.LeaveWorld);
  }

  public void setConnected(boolean f)
  {
    _isConnected = f;
  }

  public boolean isConnected() {
    return _isConnected;
  }

  public Point3D getCurrentSkillWorldPosition() {
    return _currentSkillWorldPosition;
  }

  public void setCurrentSkillWorldPosition(Point3D worldPosition) {
    _currentSkillWorldPosition = worldPosition;
  }

  public void onAction(L2PcInstance player)
  {
    if (player.isOutOfControl()) {
      player.sendActionFailed();
      return;
    }

    if (player.getTarget() != this) {
      player.setTarget(this);
      if (_isPartner) {
        player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(9, (int)getCurrentHp());
        su.addAttribute(10, getMaxHp());
        player.sendPacket(su);
      } else {
        player.sendUserPacket(new MyTargetSelected(getObjectId(), 0));
      }
      player.sendActionFailed();
      return;
    }

    if (getPrivateStoreType() != 0) {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else {
      if (isAutoAttackable(player))
      {
        if (((isCursedWeaponEquiped()) && (player.getLevel() < 21)) || ((player.isCursedWeaponEquiped()) && (getLevel() < 21))) {
          player.sendActionFailed();
        } else {
          player.clearNextLoc();
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
          player.onActionRequest();
        }
      } else if ((player != this) && 
        (canSeeTarget(player))) {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
      }

      player.sendActionFailed();
    }
  }

  public void onActionShift(L2PcInstance player)
  {
    if (player.isOutOfControl()) {
      player.sendActionFailed();
      return;
    }

    if (player.getTarget() != this) {
      player.setTarget(this);
      player.sendUserPacket(new MyTargetSelected(getObjectId(), 0));
    }
    if ((_isPartner) && 
      (player.equals(_owner))) {
      player.sendPacket(new GMViewCharacterInfo(this));
    }

    player.sendActionFailed();
  }

  private boolean needCpUpdate(int barPixels)
  {
    double currentCp = getCurrentCp();

    if ((currentCp <= 1.0D) || (getMaxCp() < barPixels)) {
      return true;
    }

    if ((currentCp <= _cpUpdateDecCheck) || (currentCp >= _cpUpdateIncCheck)) {
      if (currentCp == getMaxCp()) {
        _cpUpdateIncCheck = (currentCp + 1.0D);
        _cpUpdateDecCheck = (currentCp - _cpUpdateInterval);
      } else {
        double doubleMulti = currentCp / _cpUpdateInterval;
        int intMulti = (int)doubleMulti;

        _cpUpdateDecCheck = (_cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti));
        _cpUpdateIncCheck = (_cpUpdateDecCheck + _cpUpdateInterval);
      }

      return true;
    }

    return false;
  }

  private boolean needMpUpdate(int barPixels)
  {
    double currentMp = getCurrentMp();

    if ((currentMp <= 1.0D) || (getMaxMp() < barPixels)) {
      return true;
    }

    if ((currentMp <= _mpUpdateDecCheck) || (currentMp >= _mpUpdateIncCheck)) {
      if (currentMp == getMaxMp()) {
        _mpUpdateIncCheck = (currentMp + 1.0D);
        _mpUpdateDecCheck = (currentMp - _mpUpdateInterval);
      } else {
        double doubleMulti = currentMp / _mpUpdateInterval;
        int intMulti = (int)doubleMulti;

        _mpUpdateDecCheck = (_mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti));
        _mpUpdateIncCheck = (_mpUpdateDecCheck + _mpUpdateInterval);
      }

      return true;
    }

    return false;
  }

  public void broadcastStatusUpdate()
  {
    if (!needStatusUpdate()) {
      return;
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(9, (int)getCurrentHp());
    su.addAttribute(11, (int)getCurrentMp());
    su.addAttribute(33, (int)getCurrentCp());
    su.addAttribute(34, getMaxCp());
    sendUserPacket(su);

    if (isInParty()) {
      getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
    }

    if (getDuel() != null) {
      getDuel().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
    }

    if ((isInOlympiadMode()) && (isOlympiadCompStart())) {
      OlympiadGame game = Olympiad.getOlympiadGame(getOlympiadGameId());
      if (game != null)
        game.broadcastInfo(this, null, false);
    }
  }

  public void setOlyEnemy(L2PcInstance enemy)
  {
    _olyEnemy = enemy;
  }

  public L2PcInstance getOlyEnemy() {
    return _olyEnemy;
  }

  public boolean needStatusUpdate()
  {
    return (needCpUpdate(352)) || (super.needHpUpdate(352)) || (needMpUpdate(352));
  }

  public final void updateEffectIcons(boolean f)
  {
    MagicEffectIcons mi = null;
    if (!f) {
      mi = new MagicEffectIcons();
    }

    PartySpelled ps = null;
    if (isInParty()) {
      ps = new PartySpelled(this);
    }

    FastTable effects = getAllEffectsTable();
    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect effect = (L2Effect)effects.get(i);
      if (effect == null)
      {
        continue;
      }
      switch (effect.getEffectType()) {
      case CHARGE:
      case SIGNET_GROUND:
        break;
      default:
        if (!effect.getInUse()) continue;
        if (mi != null) {
          effect.addIcon(mi);
        }
        if (ps == null) continue;
        effect.addPartySpelledIcon(ps);
      }

    }

    if (mi != null) {
      sendUserPacket(mi);
    }
    if ((ps != null) && (isInParty()))
    {
      getParty().broadcastToPartyMembers(this, ps);
    }
  }

  public final void broadcastUserInfo()
  {
    sendUserPacket(new UserInfo(this));
    sendEtcStatusUpdate();

    Broadcast.toKnownPlayers(this, new CharInfo(this));
  }

  public final void broadcastTitleInfo()
  {
    sendUserPacket(new UserInfo(this));

    Broadcast.toKnownPlayers(this, new TitleUpdate(this));
  }

  public int getAllyId()
  {
    if (_clan == null) {
      return 0;
    }
    return _clan.getAllyId();
  }

  public int getAllyCrestId()
  {
    if (getClanId() == 0) {
      return 0;
    }
    if (getClan().getAllyId() == 0) {
      return 0;
    }
    return getClan().getAllyCrestId();
  }

  protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
  {
    super.onHitTimer(target, damage, crit, miss, soulshot, shld);
  }

  public void sendPacket(L2GameServerPacket packet)
  {
    if (_fantome) {
      return;
    }

    if (_isConnected)
      try {
        if (_client != null)
          _client.sendPacket(packet);
      }
      catch (Exception e) {
        _log.log(Level.INFO, "", e);
      }
  }

  public void sendUserPacket(L2GameServerPacket packet)
  {
    if (_fantome) {
      return;
    }

    if (_isConnected)
      try {
        if (_client != null)
          _client.sendPacket(packet);
      }
      catch (Exception e) {
        _log.log(Level.INFO, "", e);
      }
      finally
      {
        packet = null;
      }
  }

  public void sendSayPacket(L2GameServerPacket packet, int limit)
  {
    if (getChatIgnore() < limit)
      sendPacket(packet);
  }

  public void doInteract(L2Character target)
  {
    if (target == null) {
      return;
    }

    if (target.isPlayer()) {
      L2PcInstance temp = target.getPlayer();
      sendActionFailed();

      if ((temp.getPrivateStoreType() == 1) || (temp.getPrivateStoreType() == 8))
        sendUserPacket(new PrivateStoreListSell(this, temp));
      else if (temp.getPrivateStoreType() == 3)
        sendUserPacket(new PrivateStoreListBuy(this, temp));
      else if (temp.getPrivateStoreType() == 5) {
        sendUserPacket(new RecipeShopSellList(this, temp));
      }

    }
    else if (target != null) {
      target.onAction(this);
    }
  }

  public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
  {
    if (isInParty()) {
      getParty().distributeItem(this, item, false, target);
    } else if (item.getItemId() == 57) {
      addAdena("Loot", item.getCount(), target, true);
    } else {
      addItem("Loot", item.getItemId(), item.getCount(), target, true);
      if ((Config.LOG_ITEMS) && (item.getItemId() != 9885)) {
        String act = new StringBuilder().append(Log.getTime()).append("PICKUP(AUTOLOOT) itemId: ").append(item.getItemId()).append("(").append(item.getCount()).append(") #(player ").append(getName()).append(", account: ").append(getAccountName()).append(", ip: ").append(getIP()).append(", hwid: ").append(getHWID()).append(")").append("\n").toString();
        Log.item(act, 6);
      }
    }
    sendChanges();
  }

  public void doEpicLoot(L2GrandBossInstance boss, int bossId) {
    int raid_item = 8350;
    String raid_name = "Ooops! \u0421\u043A\u0440\u0438\u043D \u0434\u043B\u044F \u0430\u0434\u043C\u0438\u043D\u0430 \u0441\u0434\u0435\u043B\u0430\u0439.";
    boolean epic = false;

    switch (bossId) {
    case 29001:
      raid_item = 6660;
      raid_name = "\u041A\u043E\u043B\u044C\u0446\u043E Ant Queen \u0443\u0448\u043B\u043E \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
      break;
    case 29028:
      raid_item = 6657;
      raid_name = "\u041E\u0436\u0435\u0440\u0435\u043B\u044C\u0435 Valakas \u0443\u0448\u043B\u043E \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
      break;
    case 29020:
      raid_item = 6658;
      raid_name = "\u041A\u043E\u043B\u044C\u0446\u043E Baium \u0443\u0448\u043B\u043E \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
      break;
    case 29066:
    case 29067:
    case 29068:
      raid_item = 6656;
      raid_name = "\u0421\u0435\u0440\u044C\u0433\u0430 Antharas \u0443\u0448\u043B\u0430 \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
      break;
    case 29006:
      raid_item = 6662;
      raid_name = "\u041A\u043E\u043B\u044C\u0446\u043E Core \u0443\u0448\u043B\u043E \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
      break;
    case 29014:
      raid_item = 6661;
      raid_name = "\u0421\u0435\u0440\u044C\u0433\u0430 Orfen \u0443\u0448\u043B\u0430 \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
      break;
    case 29022:
      raid_item = 6659;
      raid_name = "\u0421\u0435\u0440\u044C\u0433\u0430 Zaken \u0443\u0448\u043B\u0430 \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
      break;
    case 29047:
      raid_item = 8191;
      raid_name = "\u041E\u0436\u0435\u0440\u0435\u043B\u044C\u0435 Frintezza \u0443\u0448\u043B\u043E \u0438\u0433\u0440\u043E\u043A\u0443 ";
      epic = true;
    }

    if (epic) {
      if (Config.ALT_EPIC_JEWERLY) {
        addItem("raidLoot", raid_item, 1, null, true);
      } else {
        boss.dropItem(raid_item, 1, this);
        return;
      }

      CreatureSay gmcs = new CreatureSay(0, 18, getName(), new StringBuilder().append(raid_name).append(" ").append(getName()).toString());
      sendPacket(gmcs);

      FastList players = getKnownList().getKnownPlayersInRadius(1250);
      L2PcInstance pc = null;
      FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
        pc = (L2PcInstance)n.getValue();
        if (pc == null) {
          continue;
        }
        pc.sendPacket(gmcs);
      }
      players.clear();
      players = null;
      pc = null;
      gmcs = null;
      Log.add(new StringBuilder().append(TimeLogger.getTime()).append(raid_name).append(" ").append(getName()).toString(), "epic_loot");
    }
    raid_name = null;
    sendChanges();
  }

  public void sendCritMessage(String text) {
    sendUserPacket(new CreatureSay(0, 18, "", text));
  }

  public void giveItem(int item, int count) {
    addItem("giveItem", item, count, this, true);
    sendChanges();
  }

  protected void doPickupItem(L2Object object)
  {
    if ((isAlikeDead()) || (isFakeDeath())) {
      return;
    }

    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    if (!object.isL2Item())
    {
      _log.warning(new StringBuilder().append("trying to pickup wrong target.").append(getTarget()).toString());
      return;
    }

    L2ItemInstance target = (L2ItemInstance)object;

    sendActionFailed();

    sendUserPacket(new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()));

    synchronized (target)
    {
      if (!target.isVisible())
      {
        sendActionFailed();
        return;
      }

      if (((isInParty()) && (getParty().getLootDistribution() == 0)) || ((!isInParty()) && (!_inventory.validateCapacity(target)))) {
        sendActionFailed();
        sendUserPacket(Static.SLOTS_FULL);
        return;
      }

      if ((isInvul()) && (!isGM())) {
        sendActionFailed();
        sendUserPacket(SystemMessage.id(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
        return;
      }

      if ((target.getOwnerId() != 0) && (target.getOwnerId() != getObjectId()) && (!isInLooterParty(target.getOwnerId()))) {
        sendActionFailed();
        SystemMessage sm = null;
        if (target.getItemId() == 57)
          sm = SystemMessage.id(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(target.getCount());
        else if (target.getCount() > 1)
          sm = SystemMessage.id(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(target.getItemId()).addNumber(target.getCount());
        else {
          sm = SystemMessage.id(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId());
        }

        sendUserPacket(sm);
        sm = null;
        return;
      }
      if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getObjectId()) || (isInLooterParty(target.getOwnerId())))) {
        target.resetOwnerTimer();
      }

      sendChanges();
      target.pickupMe(this);
      if (Config.SAVE_DROPPED_ITEM)
      {
        ItemsOnGroundManager.getInstance().removeObject(target);
      }

    }

    if (target.getItemType() == L2EtcItemType.HERB) {
      IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getItemId());
      if (handler == null)
        _log.fine(new StringBuilder().append("No item handler registered for item ID ").append(target.getItemId()).append(".").toString());
      else {
        handler.useItem(this, target);
      }
      ItemTable.getInstance().destroyItem("Consume", target, this, null);
    }
    else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId())) {
      addItem("Pickup", target, null, true);
    }
    else {
      if (((target.getItemType() instanceof L2ArmorType)) || ((target.getItemType() instanceof L2WeaponType))) {
        SystemMessage msg = null;
        if (target.getEnchantLevel() > 0)
          msg = SystemMessage.id(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addString(getName()).addNumber(target.getEnchantLevel()).addItemName(target.getItemId());
        else {
          msg = SystemMessage.id(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addString(getName()).addItemName(target.getItemId());
        }
        broadcastPacket(msg, 1400);
        msg = null;
      }

      if (isInParty()) {
        getParty().distributeItem(this, target);
      } else if ((target.getItemId() == 57) && (getInventory().getAdenaInstance() != null))
      {
        addAdena("Pickup", target.getCount(), null, true);
        ItemTable.getInstance().destroyItem("Pickup", target, this, null);
      } else {
        addItem("Pickup", target, null, true);
        if (Config.LOG_ITEMS) {
          String act = new StringBuilder().append(Log.getTime()).append("PICKUP ").append(target.getItemName()).append("(").append(target.getCount()).append(")(+").append(target.getEnchantLevel()).append(")(").append(target.getObjectId()).append(") #(player ").append(getName()).append(", account: ").append(getAccountName()).append(", ip: ").append(getIP()).append(", hwid: ").append(getHWID()).append(")").append("\n").toString();
          Log.item(act, 6);
        }
      }
    }
  }

  public void setTarget(L2Object newTarget)
  {
    if ((newTarget != null) && (!newTarget.isVisible())) {
      newTarget = null;
    }

    if ((newTarget != null) && (!isGM()))
    {
      if ((newTarget.isL2FestivalMonster()) && (!isFestivalParticipant())) {
        newTarget = null;
      } else if ((isInParty()) && (getParty().isInDimensionalRift()))
      {
        byte riftType = getParty().getDimensionalRift().getType();
        byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();

        if (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ())) {
          newTarget = null;
        }
      }

    }

    L2Object oldTarget = getTarget();

    if (oldTarget != null) {
      if (oldTarget.equals(newTarget)) {
        return;
      }

      oldTarget.removeStatusListener(this);
    }

    if ((newTarget != null) && (newTarget.isL2Character())) {
      newTarget.addStatusListener(this);
      broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
    }

    super.setTarget(newTarget);
  }

  public L2ItemInstance getActiveWeaponInstance()
  {
    return getInventory().getPaperdollItem(7);
  }

  public L2Weapon getActiveWeaponItem()
  {
    L2ItemInstance weapon = getActiveWeaponInstance();

    if (weapon == null) {
      return getFistsWeaponItem();
    }

    return (L2Weapon)weapon.getItem();
  }

  public L2ItemInstance getChestArmorInstance() {
    return getInventory().getPaperdollItem(10);
  }

  public L2Armor getActiveChestArmorItem()
  {
    L2ItemInstance armor = getChestArmorInstance();

    if (armor == null) {
      return null;
    }

    return (L2Armor)armor.getItem();
  }

  public boolean isWearingHeavyArmor()
  {
    L2ItemInstance armor = getChestArmorInstance();

    return (L2ArmorType)armor.getItemType() == L2ArmorType.HEAVY;
  }

  public boolean isWearingLightArmor()
  {
    L2ItemInstance armor = getChestArmorInstance();

    return (L2ArmorType)armor.getItemType() == L2ArmorType.LIGHT;
  }

  public boolean isWearingMagicArmor()
  {
    L2ItemInstance armor = getChestArmorInstance();

    return (L2ArmorType)armor.getItemType() == L2ArmorType.MAGIC;
  }

  public boolean isWearingFormalWear()
  {
    return _IsWearingFormalWear;
  }

  public void setIsWearingFormalWear(boolean f) {
    _IsWearingFormalWear = f;
  }

  public boolean isMarried() {
    return _married;
  }

  public void setMarried(boolean f) {
    _married = f;
  }

  public boolean isEngageRequest() {
    return _engagerequest;
  }

  public void setEngageRequest(boolean f, int playerid)
  {
    _engagerequest = f;
    _engageid = playerid;
    _engageTime = (System.currentTimeMillis() + Config.WEDDING_ANSWER_TIME);
  }

  public void setMaryRequest(boolean f) {
    _marryrequest = f;
  }

  public boolean isMaryRequest() {
    return _marryrequest;
  }

  public void setMarryAccepted(boolean f) {
    _marryaccepted = f;
  }

  public boolean isMarryAccepted() {
    return _marryaccepted;
  }

  public int getEngageId() {
    return _engageid;
  }

  public int getPartnerId() {
    return _partnerId;
  }

  public void setPartnerId(int partnerid) {
    _partnerId = partnerid;
  }

  public int getCoupleId() {
    return _coupleId;
  }

  public void setCoupleId(int coupleId) {
    _coupleId = coupleId;
  }

  public void engageAnswer(int answer) {
    if (!_engagerequest)
      return;
    if (_engageid == 0) {
      return;
    }
    if ((Config.WEDDING_ANSWER_TIME > 0) && (System.currentTimeMillis() > _engageTime)) {
      setEngageRequest(false, 0);
      sendUserPacket(Static.ANSWER_TIMEOUT);
      return;
    }

    if (answer == 1)
      CoupleManager.getInstance().getWedding(_engageid).sayYes(this);
    else {
      CoupleManager.getInstance().getWedding(_engageid).sayNo(this);
    }

    setEngageRequest(false, 0);
  }

  public L2ItemInstance getSecondaryWeaponInstance()
  {
    return getInventory().getPaperdollItem(8);
  }

  public L2Weapon getSecondaryWeaponItem()
  {
    L2ItemInstance weapon = getSecondaryWeaponInstance();

    if (weapon == null) {
      return getFistsWeaponItem();
    }

    L2Item item = weapon.getItem();

    if ((item instanceof L2Weapon)) {
      return (L2Weapon)item;
    }

    return null;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    if (isPhoenixBlessed()) {
      reviveRequest(this, null, false);
    }

    if (killer != null)
    {
      increaseDeaths();
      TvTEvent.onKill(killer, this);
      EventManager.getInstance().doDie(this, killer);

      setExpBeforeDeath(0L);

      if (isCursedWeaponEquiped()) {
        CursedWeaponsManager.getInstance().drop(_cursedWeaponEquipedId, killer);
      } else {
        if ((killer.isPlayer()) && (!killer.isCursedWeaponEquiped()) && (
          (!isInsidePvpZone()) || (isInsideZone(4)))) {
          if (hasClanWarWith(killer)) {
            if (getClan().getReputationScore() > 0)
            {
              killer.getClan().incWarPoints(Config.ALT_CLAN_REP_WAR);
            }
            if (killer.getClan().getReputationScore() > 0)
            {
              _clan.decWarPoints(Config.ALT_CLAN_REP_WAR);
            }

          }

          if ((getSkillLevel(194) < 0) || (getStat().getLevel() > 9)) {
            deathPenalty(hasClanWarWith(killer));
          }

          if (Config.ALLOW_PVPPK_REWARD) {
            managePvpPkBonus(killer.getPlayer(), true);
          }

          if ((Config.WEBSTAT_ENABLE) && (Config.WEBSTAT_KILLS)) {
            WebStat.getInstance().addKill(killer.getName(), getName());
          }
        }

        onDieDropItem(killer);
      }

      if (killer.isL2Npc()) {
        killer.doNpcChat(3, getName());
      }
    }

    if (getPvpFlag() != 0) {
      setPvpFlag(0);
    }

    if ((_cubics != null) && (_cubics.size() > 0)) {
      for (L2CubicInstance cubic : _cubics.values()) {
        cubic.stopAction();
        cubic.cancelDisappear();
      }
      _cubics.clear();
    }

    if (_forceBuff != null) {
      _forceBuff.delete();
    }

    for (L2Character character : getKnownList().getKnownCharacters()) {
      if ((character.getForceBuff() != null) && (character.getForceBuff().getTarget() == this)) {
        character.abortCast();
      }
    }

    if ((isInParty()) && (getParty().isInDimensionalRift())) {
      getParty().getDimensionalRift().getDeadMemberList().add(this);
    }

    if (_partner != null) {
      try {
        _partner.despawnMe();
      }
      catch (Exception t)
      {
      }
    }
    calculateDeathPenaltyBuffLevel(killer);
    if (getPrivateStoreType() != 0) {
      setPrivateStoreType(0);
    }
    setTransactionRequester(null);

    broadcastUserInfo();

    stopRentPet(false);
    stopWaterTask(-5);
    updateEffectIcons();
    return true;
  }

  private void onDieDropItem(L2Character killer) {
    if ((atEvent) || (killer == null)) {
      return;
    }

    if ((getKarma() == 0) || (getPkKills() < Config.KARMA_PK_LIMIT)) {
      return;
    }

    if ((killer.isL2Npc()) && (!Config.KARMA_PK_NPC_DROP)) {
      return;
    }

    if ((isPremium()) && (Config.PREMIUM_PKDROP_OFF)) {
      return;
    }

    if (!isInsidePvpZone()) {
      onDieUpdateKarma();
      boolean isKillerNpc = killer.isL2Npc();

      int dropEquip = 0;
      int dropEquipWeapon = 0;
      int dropItem = 0;
      int dropLimit = 0;
      int dropPercent = 0;

      if (getPkKills() >= Config.KARMA_PK_LIMIT) {
        dropPercent = Config.KARMA_RATE_DROP;
        dropEquip = Config.KARMA_RATE_DROP_EQUIP;
        dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
        dropItem = Config.KARMA_RATE_DROP_ITEM;
        dropLimit = Config.KARMA_DROP_LIMIT;
      } else if ((isKillerNpc) && (getLevel() > 4) && (!isFestivalParticipant())) {
        dropPercent = Config.PLAYER_RATE_DROP;
        dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
        dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
        dropItem = Config.PLAYER_RATE_DROP_ITEM;
        dropLimit = Config.PLAYER_DROP_LIMIT;
      }

      int dropCount = 0;
      String date = "";
      TextBuilder tb = null;
      if (Config.LOG_ITEMS) {
        date = Log.getTime();
        tb = new TextBuilder();
      }
      while ((dropPercent > 0) && (Rnd.get(100) < dropPercent) && (dropCount < dropLimit)) {
        int itemDropPercent = 0;
        for (L2ItemInstance itemDrop : getInventory().getItems()) {
          if (itemDrop == null)
          {
            continue;
          }

          if ((itemDrop.isAugmented()) || (itemDrop.isShadowItem()) || (itemDrop.getItemId() == 57) || (itemDrop.getItem().getType2() == 3) || (Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(Integer.valueOf(itemDrop.getItemId()))) || (Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(Integer.valueOf(itemDrop.getItemId()))) || ((getPet() != null) && (getPet().getControlItemId() == itemDrop.getItemId())))
          {
            continue;
          }

          if (itemDrop.isEquipped())
          {
            itemDropPercent = itemDrop.getItem().getType2() == 0 ? dropEquipWeapon : dropEquip;
            getInventory().unEquipItemInSlotAndRecord(itemDrop.getEquipSlot());
          } else {
            itemDropPercent = dropItem;
          }

          if (Rnd.get(100) < itemDropPercent) {
            if (Config.LOG_ITEMS) {
              String act = new StringBuilder().append("DIEDROP ").append(itemDrop.getItemName()).append("(").append(itemDrop.getCount()).append(")(+").append(itemDrop.getEnchantLevel()).append(")(").append(itemDrop.getObjectId()).append(") #(player ").append(getName()).append(", account: ").append(getAccountName()).append(", ip: ").append(getIP()).append(")").append(killer.isPlayer() ? new StringBuilder().append("(killer ").append(killer.getName()).append(", account: ").append(killer.getPlayer().getAccountName()).append(", ip: ").append(killer.getPlayer().getIP()).append(", hwid: ").append(killer.getPlayer().getHWID()).append(")").toString() : "(killer MOB)").toString();
              tb.append(new StringBuilder().append(date).append(act).append("\n").toString());
            }
            dropItem("DieDrop", itemDrop, killer, true);
            dropCount++;
            break;
          }
        }
      }
      if ((Config.LOG_ITEMS) && (tb != null)) {
        Log.item(tb.toString(), 7);
        tb.clear();
        tb = null;
      }
    }
  }

  private void onDieUpdateKarma() {
    double karmaLost = Config.KARMA_LOST_BASE;
    karmaLost *= getLevel();
    karmaLost *= getLevel() / 100.0D;
    karmaLost = Math.round(karmaLost);
    if (karmaLost < 0.0D) {
      karmaLost = 1.0D;
    }

    int newKarma = getKarma() - (int)karmaLost;
    setKarma(newKarma);
  }

  public void setFreePvp(boolean f)
  {
    _freePvp = f;
  }

  public void onKillUpdatePvPKarma(L2Character target)
  {
    if (target == null) {
      return;
    }
    if (!target.isL2Playable()) {
      return;
    }

    L2PcInstance targetPlayer = target.getPlayer();
    if (targetPlayer == null) {
      return;
    }
    if (targetPlayer.equals(this)) {
      return;
    }

    if (isCursedWeaponEquiped()) {
      CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
      return;
    }

    if (targetPlayer.getKarma() > 0) {
      return;
    }

    if ((isInDuel()) && (targetPlayer.isInDuel())) {
      return;
    }

    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(getName()))) {
      return;
    }

    if (targetPlayer.getChannel() > 1) {
      return;
    }

    if (_freePvp) {
      increasePvpKills();
      if (Config.ALLOW_PVPPK_REWARD) {
        managePvpPkBonus(getPlayer(), false);
      }

      if ((Config.WEBSTAT_ENABLE) && (Config.WEBSTAT_KILLS)) {
        WebStat.getInstance().addKill(getName(), getName());
      }
      return;
    }

    if ((isInsidePvpZone()) || (targetPlayer.isInsidePvpZone())) {
      return;
    }

    if (target.isL2Summon()) {
      if (targetPlayer.getPvpFlag() == 0) {
        increasePkKillsAndKarma(targetPlayer.getLevel(), false);
      }
      return;
    }

    if ((isPartnerFor(targetPlayer.getPartner())) || (targetPlayer.isPartnerFor(getPartner()))) {
      return;
    }

    if ((checkIfPvP(target)) && (targetPlayer.getPvpFlag() != 0)) {
      increasePvpKills();
      return;
    }

    if ((targetPlayer.getClan() != null) && (getClan() != null) && 
      (getClan().isAtWarWith(targetPlayer.getClanId())) && 
      (targetPlayer.getClan().isAtWarWith(getClanId()))) {
      increasePvpKills();
      return;
    }

    if (targetPlayer.getPvpFlag() == 0)
    {
      increasePkKillsAndKarma(targetPlayer.getLevel());
    }
  }

  public void increasePvpKills()
  {
    setPvpKills(getPvpKills() + 1);

    sendChanges();
  }

  public void increaseDeaths()
  {
    setDeaths(getDeaths() + 1);
  }

  public void increasePkKillsAndKarma(int targLVL)
  {
    increasePkKillsAndKarma(targLVL, true);
  }

  public void increasePkKillsAndKarma(int targLVL, boolean inc) {
    if (Config.FREE_PVP) {
      return;
    }

    int baseKarma = Config.KARMA_MIN_KARMA;
    int newKarma = baseKarma;
    int karmaLimit = Config.KARMA_MAX_KARMA;

    int pkLVL = getLevel();
    int pkPKCount = getPkKills();

    int lvlDiffMulti = 0;
    int pkCountMulti = 0;

    if (pkPKCount > 0)
      pkCountMulti = pkPKCount / 2;
    else {
      pkCountMulti = 1;
    }
    if (pkCountMulti < 1) {
      pkCountMulti = 1;
    }

    if (pkLVL > targLVL)
      lvlDiffMulti = pkLVL / targLVL;
    else {
      lvlDiffMulti = 1;
    }
    if (lvlDiffMulti < 1) {
      lvlDiffMulti = 1;
    }

    newKarma *= pkCountMulti;
    newKarma *= lvlDiffMulti;

    if (newKarma < baseKarma) {
      newKarma = baseKarma;
    }
    if (newKarma > karmaLimit) {
      newKarma = karmaLimit;
    }

    if (getKarma() > 2147483647 - newKarma) {
      newKarma = 2147483647 - getKarma();
    }

    if (inc) {
      setPkKills(getPkKills() + 1);
    }

    setKarma(getKarma() + newKarma);

    sendChanges();

    sendUserPacket(new UserInfo(this));
  }

  public int calculateKarmaLost(long exp)
  {
    long expGained = Math.abs(exp);
    expGained /= Config.KARMA_XP_DIVIDER;

    int karmaLost = 0;
    if (expGained > 2147483647L)
      karmaLost = 2147483647;
    else {
      karmaLost = (int)expGained;
    }

    if (karmaLost < Config.KARMA_LOST_BASE) {
      karmaLost = Config.KARMA_LOST_BASE;
    }
    if (karmaLost > getKarma()) {
      karmaLost = getKarma();
    }

    return karmaLost;
  }

  public void updatePvPStatus()
  {
    if (Config.FREE_PVP) {
      return;
    }

    if (isInsidePvpZone()) {
      return;
    }

    if (isInsideHotZone()) {
      return;
    }

    if ((isInOlympiadMode()) && (isOlympiadStart())) {
      return;
    }

    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(getName()))) {
      return;
    }

    setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);

    if (getPvpFlag() == 0)
      startPvPFlag();
  }

  public void updatePvPStatus(L2Character target)
  {
    if (Config.FREE_PVP) {
      return;
    }

    if (isInsideHotZone()) {
      return;
    }

    if ((isInOlympiadMode()) && (isOlympiadStart())) {
      return;
    }

    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(getName()))) {
      return;
    }

    L2PcInstance player_target = target.getPlayer();
    if (player_target == null) {
      return;
    }

    if ((isInDuel()) && (player_target.getDuel() == getDuel())) {
      return;
    }

    if ((isPartnerFor(player_target.getPartner())) || (player_target.isPartnerFor(getPartner()))) {
      return;
    }

    if (((!isInsidePvpZone()) || (!player_target.isInsidePvpZone())) && (player_target.getKarma() == 0))
    {
      setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
      if (_partner != null) {
        _partner.setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
      }
      if ((_isPartner) && (_owner != null)) {
        _owner.setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
      }
      if (getPvpFlag() == 0) {
        startPvPFlag();
        if (_partner != null) {
          _partner.startPvPFlag();
        }
        if ((_isPartner) && (_owner != null))
          _owner.startPvPFlag();
      }
    }
  }

  public boolean isPartnerFor(L2PcInstance partner)
  {
    if (partner == null) {
      return false;
    }

    return (_isPartner) && (partner.equals(this));
  }

  public void restoreExp(double restorePercent)
  {
    if (getExpBeforeDeath() > 0L)
    {
      getStat().addExp((int)Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100.0D), true);
      setExpBeforeDeath(0L);
    }
  }

  public void deathPenalty(boolean f)
  {
    int lvl = getLevel();

    double percentLost = 7.0D;
    if (getLevel() >= 76)
      percentLost = 2.0D;
    else if (getLevel() >= 40) {
      percentLost = 4.0D;
    }

    if (getKarma() > 0) {
      percentLost *= Config.RATE_KARMA_EXP_LOST;
    }

    if ((isFestivalParticipant()) || (f) || (isInsideZone(4))) {
      percentLost /= 4.0D;
    }

    long lostExp = 0L;
    if (!atEvent) {
      if (lvl < 81)
        lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100.0D);
      else {
        lostExp = Math.round((getStat().getExpForLevel(81) - getStat().getExpForLevel(80)) * percentLost / 100.0D);
      }

    }

    setExpBeforeDeath(getExp());

    if ((getCharmOfCourage()) && 
      (getSiegeState() > 0) && (isInsideZone(4))) {
      lostExp = 0L;
    }

    setCharmOfCourage(false);

    getStat().addExp(-lostExp, true);
  }

  public void setPartyMatchingAutomaticRegistration(boolean b)
  {
    _partyMatchingAutomaticRegistration = b;
  }

  public void setPartyMatchingShowLevel(boolean b)
  {
    _partyMatchingShowLevel = b;
  }

  public void setPartyMatchingShowClass(boolean b)
  {
    _partyMatchingShowClass = b;
  }

  public void setPartyMatchingMemo(String memo)
  {
    _partyMatchingMemo = memo;
  }

  public boolean isPartyMatchingAutomaticRegistration() {
    return _partyMatchingAutomaticRegistration;
  }

  public String getPartyMatchingMemo() {
    return _partyMatchingMemo;
  }

  public boolean isPartyMatchingShowClass() {
    return _partyMatchingShowClass;
  }

  public boolean isPartyMatchingShowLevel() {
    return _partyMatchingShowLevel;
  }

  public void increaseLevel()
  {
    setCurrentHpMp(getMaxHp(), getMaxMp());
    setCurrentCp(getMaxCp());
  }

  public void stopAllTimers()
  {
    stopHpMpRegeneration();
    stopWarnUserTakeBreak();
    stopWaterTask(-5);
    stopRentPet();
    stopPvpRegTask();
    stopJailTask(true);
  }

  public L2Summon getPet()
  {
    return _summon;
  }

  public boolean isPetSummoned() {
    return _summon != null;
  }

  public void setPet(L2Summon summon)
  {
    _summon = summon;
  }

  public L2Summon getFairy()
  {
    return fairy;
  }

  public void setFairy(L2Summon fairy) {
    this.fairy = fairy;
  }

  public L2TamedBeastInstance getTrainedBeast()
  {
    return _tamedBeast;
  }

  public void setTrainedBeast(L2TamedBeastInstance tamedBeast)
  {
    _tamedBeast = tamedBeast;
  }

  public void setTransactionRequester(L2PcInstance requestor)
  {
    _currentTransactionRequester = requestor;
    _currentTransactionTimeout = -1L;
  }

  public void setTransactionRequester(L2PcInstance requestor, long timeout) {
    _currentTransactionRequester = requestor;
    _currentTransactionTimeout = timeout;
  }

  public void setTransactionType(TransactionType type)
  {
    _currentTransactionType = type;
  }

  public TransactionType getTransactionType() {
    return _currentTransactionType;
  }

  public synchronized void setActiveRequester(L2PcInstance requester)
  {
    _currentTransactionRequester = requester;
  }

  public L2PcInstance getTransactionRequester()
  {
    return _currentTransactionRequester;
  }

  public boolean isTransactionInProgress()
  {
    return ((_currentTransactionTimeout < 0L) || (_currentTransactionTimeout > System.currentTimeMillis())) && (_currentTransactionRequester != null);
  }

  public void onTransactionRequest(L2PcInstance partner)
  {
    _requestExpireTime = (GameTimeController.getGameTicks() + 100);
    partner.setTransactionRequester(this);
  }

  public void onTransactionResponse()
  {
    _requestExpireTime = 0L;
  }

  public void setActiveWarehouse(ItemContainer warehouse)
  {
    _activeWarehouse = warehouse;
  }

  public void cancelActiveWarehouse() {
    if (_activeWarehouse == null) {
      return;
    }

    _activeWarehouse = null;

    sendUserPacket(new SendTradeDone(0));
  }

  public ItemContainer getActiveWarehouse()
  {
    return _activeWarehouse;
  }

  public void setActiveTradeList(TradeList tradeList)
  {
    _activeTradeList = tradeList;
  }

  public TradeList getActiveTradeList()
  {
    return _activeTradeList;
  }

  public void onTradeStart(L2PcInstance partner) {
    if (getTradeStart() != partner.getTradeStart())
    {
      cancelActiveTrade();
      return;
    }
    _activeTradeList = new TradeList(this);
    _activeTradeList.setPartner(partner);

    sendUserPacket(SystemMessage.id(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.getName()));
    sendUserPacket(new TradeStart(this));
    sendUserPacket(new TradeStartOk());
  }

  public void onTradeConfirm(L2PcInstance partner) {
    if (_activeTradeList == null)
    {
      cancelActiveTrade();
      return;
    }

    if (getTradeStart() != partner.getTradeStart())
    {
      cancelActiveTrade();
      return;
    }

    sendUserPacket(SystemMessage.id(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.getName()));
    sendUserPacket(Static.TradePressOtherOk);
    partner.sendUserPacket(Static.TradePressOwnOk);
  }

  public void onTradeCancel(L2PcInstance partner) {
    if (_activeTradeList == null) {
      return;
    }

    _activeTradeList.lock();
    _activeTradeList = null;

    sendUserPacket(new SendTradeDone(0));
    sendUserPacket(SystemMessage.id(SystemMessageId.S1_CANCELED_TRADE).addString(partner.getName()));

    setTransactionRequester(null);
    setTransactionType(TransactionType.NONE);
    partner.setTransactionRequester(null);
    partner.setTransactionType(TransactionType.NONE);

    setTradePartner(-1, 0L);
    partner.setTradePartner(-1, 0L);
  }

  public void onTradeFinish(boolean f) {
    _activeTradeList = null;
    sendUserPacket(new SendTradeDone(1));
    if (f) {
      sendUserPacket(Static.TRADE_SUCCESSFUL);
    }

    setTransactionRequester(null);
    setTransactionType(TransactionType.NONE);
    setTradePartner(-1, 0L);
  }

  public void startTrade(L2PcInstance partner) {
    onTradeStart(partner);
    partner.onTradeStart(this);
  }

  public void cancelActiveTrade() {
    if (_activeTradeList == null) {
      return;
    }

    L2PcInstance partner = _activeTradeList.getPartner();
    if (partner != null) {
      partner.onTradeCancel(this);
    }

    onTradeCancel(this);
  }

  public L2ManufactureList getCreateList()
  {
    return _createList;
  }

  public void setCreateList(L2ManufactureList x)
  {
    _createList = x;
  }

  public TradeList getSellList()
  {
    if (_sellList == null) {
      _sellList = new TradeList(this);
    }
    return _sellList;
  }

  public TradeList getBuyList()
  {
    if (_buyList == null) {
      _buyList = new TradeList(this);
    }
    return _buyList;
  }

  public void setPrivateStoreType(int type)
  {
    _privatestore = type;
    if ((type != 0) && (!inObserverMode()))
      setVar("storemode", String.valueOf(type), null);
    else
      unsetVar("storemode", null);
  }

  public int getPrivateStoreType()
  {
    return _privatestore;
  }

  public void setSkillLearningClassId(ClassId classId)
  {
    _skillLearningClassId = classId;
  }

  public ClassId getSkillLearningClassId()
  {
    return _skillLearningClassId;
  }

  public void setClan(L2Clan clan)
  {
    _clan = clan;
    setTitle("");

    if (clan == null) {
      _clanId = 0;
      _clanPrivileges = 0;
      _pledgeType = 0;
      _powerGrade = 0;
      _lvlJoinedAcademy = 0;
      _apprentice = 0;
      _sponsor = 0;
      return;
    }

    if ((!clan.isMember(getObjectId())) && (!isFantome()))
    {
      setClan(null);
      return;
    }

    _clanId = clan.getClanId();
  }

  public L2Clan getClan()
  {
    return _clan;
  }

  public boolean isClanLeader()
  {
    if (getClan() == null) {
      return false;
    }

    return getObjectId() == getClan().getLeaderId();
  }

  protected void reduceArrowCount()
  {
    if ((!Config.USE_ARROWS) || (isFantome())) {
      return;
    }

    L2ItemInstance arrows = getInventory().destroyItem("Consume", getInventory().getPaperdollObjectId(8), 1, this, null);

    if ((arrows == null) || (arrows.getCount() == 0)) {
      getInventory().unEquipItemInSlot(8);
      _arrowItem = null;

      sendItems(false);
    }
    else if (!Config.FORCE_INVENTORY_UPDATE) {
      InventoryUpdate iu = new InventoryUpdate();
      iu.addModifiedItem(arrows);
      sendUserPacket(iu);
    } else {
      sendItems(false);
    }
  }

  protected boolean checkAndEquipArrows()
  {
    if ((!Config.USE_ARROWS) || (isFantome())) {
      return true;
    }

    if (getInventory().getPaperdollItem(8) == null)
    {
      _arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());

      if (_arrowItem != null)
      {
        getInventory().setPaperdollItem(8, _arrowItem);

        sendItems(false);
      }
    }
    else {
      _arrowItem = getInventory().getPaperdollItem(8);
    }

    return _arrowItem != null;
  }

  public boolean disarmWeapons()
  {
    if (isCursedWeaponEquiped()) {
      return false;
    }

    Lock shed = new ReentrantLock();
    shed.lock();
    try {
      if (_euipWeapon != null) {
        int i = 0;
        return i;
      } } finally { shed.unlock();
    }

    L2ItemInstance wpn = getInventory().getPaperdollItem(7);
    if (wpn == null) {
      wpn = getInventory().getPaperdollItem(14);
    }
    if (wpn != null) {
      if (wpn.isWear()) {
        return false;
      }

      L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++) {
        iu.addModifiedItem(unequiped[i]);
      }
      sendUserPacket(iu);

      abortAttack();
      broadcastUserInfo();

      if (unequiped.length > 0) {
        SystemMessage sm = null;
        if (unequiped[0].getEnchantLevel() > 0)
          sm = SystemMessage.id(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequiped[0].getEnchantLevel()).addItemName(unequiped[0].getItemId());
        else {
          sm = SystemMessage.id(SystemMessageId.S1_DISARMED).addItemName(unequiped[0].getItemId());
        }

        sendUserPacket(sm);
        sm = null;
      }

    }

    L2ItemInstance sld = getInventory().getPaperdollItem(8);
    if (sld != null) {
      if (sld.isWear()) {
        return false;
      }

      L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++) {
        iu.addModifiedItem(unequiped[i]);
      }
      sendUserPacket(iu);

      abortAttack();
      broadcastUserInfo();

      if (unequiped.length > 0) {
        SystemMessage sm = null;
        if (unequiped[0].getEnchantLevel() > 0)
          sm = SystemMessage.id(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequiped[0].getEnchantLevel()).addItemName(unequiped[0].getItemId());
        else {
          sm = SystemMessage.id(SystemMessageId.S1_DISARMED).addItemName(unequiped[0].getItemId());
        }

        sendUserPacket(sm);
        sm = null;
      }
    }
    return true;
  }

  public boolean isUsingDualWeapon()
  {
    L2Weapon weaponItem = getActiveWeaponItem();
    if (weaponItem == null) {
      return false;
    }

    if (weaponItem.getItemType() == L2WeaponType.DUAL)
      return true;
    if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
      return true;
    if (weaponItem.getItemId() == 248)
    {
      return true;
    }

    return weaponItem.getItemId() == 252;
  }

  public void setUptime(long time)
  {
    _uptime = time;
  }

  public long getUptime() {
    return System.currentTimeMillis() - _uptime;
  }

  public boolean isInvul()
  {
    return (_isInvul) || (_isTeleporting) || (_protectEndTime > GameTimeController.getGameTicks());
  }

  public boolean isInParty()
  {
    return _party != null;
  }

  public void setParty(L2Party party)
  {
    _party = party;
  }

  public void joinParty(L2Party party)
  {
    if (party != null)
    {
      _party = party;
      party.addPartyMember(this);
      if (_partyRoom != null)
        PartyWaitingRoomManager.getInstance().refreshRoom(_partyRoom);
    }
  }

  public void leaveParty()
  {
    if (isInParty()) {
      _party.removePartyMember(this);
      _party = null;
      if (_partyRoom != null)
        PartyWaitingRoomManager.getInstance().exitRoom(this, _partyRoom);
    }
  }

  public void leaveOffParty()
  {
    if (isInParty()) {
      _party.removePartyMember(this, false);
      _party = null;
      if (_partyRoom != null)
        PartyWaitingRoomManager.getInstance().exitRoom(this, _partyRoom);
    }
  }

  public L2Party getParty()
  {
    return _party;
  }

  public void setIsGM(boolean f)
  {
    _isGm = f;
  }

  public boolean isGM()
  {
    return _isGm;
  }

  public void cancelCastMagic()
  {
    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    enableAllSkills();

    Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillCanceld(getObjectId()), 5000L);
  }

  public void setAccessLevel(int level)
  {
    _accessLevel = level;

    if ((_accessLevel > 0) || (Config.EVERYBODY_HAS_ADMIN_RIGHTS))
      setIsGM(true);
  }

  public void setAccountAccesslevel(int level)
  {
    LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
    if (level < 0)
      kick();
  }

  public int getAccessLevel()
  {
    if ((Config.EVERYBODY_HAS_ADMIN_RIGHTS) && (_accessLevel <= 200)) {
      return 200;
    }

    return _accessLevel;
  }

  public double getLevelMod()
  {
    return (89.0D + getLevel()) / 100.0D;
  }

  public void updateAndBroadcastStatus(int broadcastType)
  {
    refreshOverloaded();
    refreshExpertisePenalty();

    if (broadcastType == 1) {
      sendUserPacket(new UserInfo(this));
    }
    if (broadcastType == 2)
      broadcastUserInfo();
  }

  public void setKarmaFlag(int flag)
  {
    sendUserPacket(new UserInfo(this));

    FastList players = getKnownList().getListKnownPlayers();
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      pc.sendPacket(new RelationChanged(this, getRelation(pc), isAutoAttackable(pc)));
      if (getPet() != null) {
        pc.sendPacket(new RelationChanged(getPet(), getRelation(pc), isAutoAttackable(pc)));
      }
    }
    players.clear();
    players = null;
    pc = null;
  }

  public void broadcastKarma()
  {
    sendChanges();

    FastList players = getKnownList().getListKnownPlayers();
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      pc.sendPacket(new RelationChanged(this, getRelation(pc), isAutoAttackable(pc)));
      if (getPet() != null) {
        pc.sendPacket(new RelationChanged(getPet(), getRelation(pc), isAutoAttackable(pc)));
      }
    }
    players.clear();
    players = null;
    pc = null;
  }

  public void setOnlineStatus(boolean f)
  {
    if (_isOnline != f) {
      _isOnline = f;
    }

    updateOnlineStatus();
  }

  public void setIsIn7sDungeon(boolean f) {
    if (_isIn7sDungeon != f) {
      _isIn7sDungeon = f;
    }

    updateIsIn7sDungeonStatus();
  }

  public void updateOnlineStatus()
  {
    if (isInOfflineMode()) {
      return;
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
      st.setInt(1, isOnline());
      st.setLong(2, System.currentTimeMillis());
      st.setInt(3, getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not set char online status:").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
  }

  public void updateIsIn7sDungeonStatus() {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE characters SET isIn7sDungeon=?, lastAccess=? WHERE obj_id=?");
      st.setInt(1, isIn7sDungeon() ? 1 : 0);
      st.setLong(2, System.currentTimeMillis());
      st.setInt(3, getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not set char isIn7sDungeon status:").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
  }

  private boolean createDb()
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd,str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex,movement_multiplier,attack_speed_multiplier,colRad,colHeight,exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,last_recom_date,premium) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

      st.setString(1, _accountName);
      st.setInt(2, getObjectId());
      st.setString(3, super.getName());
      st.setInt(4, getLevel());
      st.setInt(5, getMaxHp());
      st.setDouble(6, getCurrentHp());
      st.setInt(7, getMaxCp());
      st.setDouble(8, getCurrentCp());
      st.setInt(9, getMaxMp());
      st.setDouble(10, getCurrentMp());
      st.setInt(11, getAccuracy());
      st.setInt(12, getCriticalHit(null, null));
      st.setInt(13, getEvasionRate(null));
      st.setInt(14, getMAtk(null, null));
      st.setInt(15, getMDef(null, null));
      st.setInt(16, getMAtkSpd());
      st.setInt(17, getPAtk(null));
      st.setInt(18, getPDef(null));
      st.setInt(19, getPAtkSpd());
      st.setInt(20, getRunSpeed());
      st.setInt(21, getWalkSpeed());
      st.setInt(22, getSTR());
      st.setInt(23, getCON());
      st.setInt(24, getDEX());
      st.setInt(25, getINT());
      st.setInt(26, getMEN());
      st.setInt(27, getWIT());
      st.setInt(28, getAppearance().getFace());
      st.setInt(29, getAppearance().getHairStyle());
      st.setInt(30, getAppearance().getHairColor());
      st.setInt(31, getAppearance().getSex() ? 1 : 0);
      st.setDouble(32, 1.0D);
      st.setDouble(33, 1.0D);
      st.setDouble(34, getTemplate().collisionRadius);
      st.setDouble(35, getTemplate().collisionHeight);
      st.setLong(36, getExp());
      st.setInt(37, getSp());
      st.setInt(38, getKarma());
      st.setInt(39, getPvpKills());
      st.setInt(40, getPkKills());
      st.setInt(41, getClanId());
      st.setInt(42, getMaxLoad());
      st.setInt(43, getRace().ordinal());
      st.setInt(44, getClassId().getId());
      st.setLong(45, getDeleteTimer());
      st.setInt(46, hasDwarvenCraft() ? 1 : 0);
      st.setString(47, getTitle());
      st.setInt(48, getAccessLevel());
      st.setInt(49, isOnline());
      st.setInt(50, isIn7sDungeon() ? 1 : 0);
      st.setInt(51, getClanPrivileges());
      st.setInt(52, getWantsPeace());
      st.setInt(53, getBaseClass());
      st.setInt(54, isNewbie() ? 1 : 0);
      st.setInt(55, isNoble() ? 1 : 0);
      st.setLong(56, 0L);
      st.setLong(57, System.currentTimeMillis());
      if ((Config.PREMIUM_ENABLE) && (Config.PREMIUM_START_DAYS > 0))
        st.setLong(58, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(Config.PREMIUM_START_DAYS));
      else {
        st.setLong(58, 0L);
      }
      st.executeUpdate();
      Close.S(st);

      setAutoLoot(Config.VS_AUTOLOOT_VAL == 1);
      setGeoPathfind(Config.VS_PATHFIND_VAL == 1);
      setShowSkillChances(Config.VS_SKILL_CHANCES_VAL == 1);
      setSoulShotsAnim(Config.SOULSHOT_ANIM);

      st = con.prepareStatement("INSERT INTO `character_settings` (`char_obj_id`, `autoloot`, `pathfind`, `skillchances`) VALUES (?, ?, ?, ?)");
      st.setInt(1, getObjectId());
      st.setInt(2, Config.VS_AUTOLOOT_VAL);
      st.setInt(3, Config.VS_PATHFIND_VAL);
      st.setInt(4, Config.VS_SKILL_CHANCES_VAL);
      st.execute();
      Close.S(st);

      if (Config.POST_CHARBRIEF) {
        TextBuilder text = new TextBuilder();
        text.append(Config.POST_BRIEFTEXT);
        st = con.prepareStatement("INSERT INTO `z_bbs_mail` (`from`, `to`, `tema`, `text`, `datetime`, `read`, `item_id`, `item_count`, `item_ench`, `aug_hex`, `aug_id`, `aug_lvl`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        st.setInt(1, 555);
        st.setInt(2, getObjectId());
        st.setString(3, Config.POST_BRIEFTHEME);
        st.setString(4, text.toString());
        st.setLong(5, System.currentTimeMillis());
        st.setInt(6, 0);
        st.setInt(7, Config.POST_BRIEF_ITEM.id);
        st.setInt(8, Config.POST_BRIEF_ITEM.count);
        st.setInt(9, 0);
        st.setInt(10, 0);
        st.setInt(11, 0);
        st.setInt(12, 0);
        st.execute();
        text.clear();
        text = null;
        Close.S(st);
      }
    } catch (SQLException e) {
      _log.severe(new StringBuilder().append("Could not insert char data: ").append(e).toString());
      int i = 0;
      return i; } finally { Close.CS(con, st);
    }
    return true;
  }

  private static L2PcInstance restore(int objectId)
  {
    L2PcInstance player = null;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    PreparedStatement st2 = null;
    ResultSet rs2 = null;
    PreparedStatement st3 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      st = con.prepareStatement("SELECT account_name, obj_Id, char_name, name_color, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, title_color, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,hero,premium,chatban_timer,chatban_reason,chat_filter_count,deaths FROM characters WHERE obj_Id=? LIMIT 1");
      st.setInt(1, objectId);
      rset = st.executeQuery();

      double currentCp = 0.0D;
      double currentHp = 0.0D;
      double currentMp = 0.0D;

      if (rset.next()) {
        int activeClassId = rset.getInt("classid");
        boolean female = rset.getInt("sex") != 0;
        L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
        PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);

        player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
        player.setClassUpdate(true);

        player.getAppearance().setNameColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("name_color")).toString()).intValue());
        player._lastAccess = rset.getLong("lastAccess");

        player.loadOfflineTrade(con);

        player.getStat().setExp(rset.getLong("exp"));
        player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
        player.getStat().setLevel(rset.getByte("level"));
        player.getStat().setSp(rset.getInt("sp"));

        player.setWantsPeace(rset.getInt("wantspeace"));

        player.setHeading(rset.getInt("heading"));

        player.setKarma(rset.getInt("karma"));
        player.setPvpKills(rset.getInt("pvpkills"));
        player.setDeaths(rset.getInt("deaths"));
        player.setPkKills(rset.getInt("pkkills"));
        player.setOnlineTime(rset.getLong("onlinetime"));
        player.setNewbie(rset.getInt("newbie") == 1);
        player.setNoble(rset.getInt("nobless") == 1);

        long premiumExpire = rset.getLong("premium");
        if ((Config.PREMIUM_ENABLE) && (premiumExpire > 0L)) {
          if (System.currentTimeMillis() - premiumExpire < 0L) {
            player.setPremium(true);
          } else if (premiumExpire > 1L) {
            premiumExpire = -1L;
            st3 = con.prepareStatement("UPDATE `characters` SET `premium`=? WHERE `obj_Id`=?");
            st3.setInt(1, 0);
            st3.setInt(2, objectId);
            st3.execute();
            Close.S(st3);
          }
        }
        player.setPremiumExpire(premiumExpire);
        player.setName(rset.getString("char_name"));

        player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
        if (player.getClanJoinExpiryTime() < System.currentTimeMillis()) {
          player.setClanJoinExpiryTime(0L);
        }
        player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
        if (player.getClanCreateExpiryTime() < System.currentTimeMillis()) {
          player.setClanCreateExpiryTime(0L);
        }

        int clanId = rset.getInt("clanid");
        player.setPowerGrade((int)rset.getLong("power_grade"));
        player.setPledgeType(rset.getInt("subpledge"));
        player.setLastRecomUpdate(rset.getLong("last_recom_date"));

        if (clanId > 0) {
          player.setClan(ClanTable.getInstance().getClan(clanId));
        }

        if (player.getClan() != null) {
          if (player.getClan().getLeaderId() != player.getObjectId()) {
            if (player.getPowerGrade() == 0) {
              player.setPowerGrade(5);
            }
            player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
          } else {
            player.setClanPrivileges(8388606);
            player.setPowerGrade(1);
          }
        }
        else player.setClanPrivileges(0);

        player.setDeleteTimer(rset.getLong("deletetime"));

        player.setTitle(rset.getString("title"));
        player.getAppearance().setTitleColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("title_color")).toString()).intValue());
        player.setAccessLevel(rset.getInt("accesslevel"));
        player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
        player.setUptime(System.currentTimeMillis());

        currentHp = rset.getDouble("curHp");
        player.setCurrentHp(rset.getDouble("curHp"));
        currentCp = rset.getDouble("curCp");
        player.setCurrentCp(rset.getDouble("curCp"));
        currentMp = rset.getDouble("curMp");
        player.setCurrentMp(rset.getDouble("curMp"));

        player.checkRecom(rset.getInt("rec_have"), rset.getInt("rec_left"));

        player._classIndex = 0;
        try {
          player.setBaseClass(rset.getInt("base_class"));
        } catch (Exception e) {
          player.setBaseClass(activeClassId);
        }

        if ((restoreSubClassData(player)) && 
          (activeClassId != player.getBaseClass())) {
          for (SubClass subClass : player.getSubClasses().values()) {
            if (subClass.getClassId() == activeClassId) {
              player._classIndex = subClass.getClassIndex();
            }
          }
        }

        if ((player.getClassIndex() == 0) && (activeClassId != player.getBaseClass()))
        {
          player.setClassId(player.getBaseClass());
          _log.warning(new StringBuilder().append("Player ").append(player.getName()).append(" reverted to base class. Possibly has tried a relogin exploit while subclassing.").toString());
        } else {
          player._activeClass = activeClassId;
        }

        if ((player.getVar("storemode") != null) && (player.getVar("offline") != null)) {
          player.restoreTradeList();
          player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
          player.sitDown();
        } else {
          player.unsetVar("storemode", null);
        }

        long heroExpire = rset.getLong("hero");
        if (heroExpire > 0L) {
          if ((heroExpire == 1L) || (System.currentTimeMillis() - heroExpire < 0L)) {
            player.setHero(true);
          } else if (heroExpire > 1L) {
            heroExpire = -1L;
            st3 = con.prepareStatement("UPDATE `characters` SET `hero`=? WHERE `obj_Id`=?");
            st3.setInt(1, 0);
            st3.setInt(2, objectId);
            st3.execute();
            Close.S(st3);
            st3 = con.prepareStatement("DELETE FROM `items` WHERE `item_id` IN ('6611', '6612', '6613', '6614', '6615', '6616', '6617', '6618', '6619', '6620', '6621', '6842') AND `owner_id` = ?");
            st3.setInt(1, objectId);
            st3.execute();
            Close.S(st3);
          }
          player.setHeroExpire(heroExpire);
        }

        if (Hero.getInstance().isHero(player.getObjectId())) {
          player.setHero(true);
        }

        player.setApprentice(rset.getInt("apprentice"));
        player.setSponsor(rset.getInt("sponsor"));
        player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
        player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
        player.setInJail(rset.getInt("in_jail") == 1);
        if (player.isInJail())
          player.setJailTimer(rset.getLong("jail_timer"));
        else {
          player.setJailTimer(0L);
        }

        CursedWeaponsManager.getInstance().checkPlayer(player);

        player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));

        player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));

        player.setPcPoints(rset.getInt("chat_filter_count"));

        player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
        try
        {
          st2 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?");
          st2.setString(1, player._accountName);
          st2.setInt(2, objectId);
          rs2 = st2.executeQuery();

          while (rs2.next()) {
            Integer charId = Integer.valueOf(rs2.getInt("obj_Id"));
            String charName = rs2.getString("char_name");
            player._chars.put(charId, charName);
          }
        } catch (SQLException e) {
          _log.severe(new StringBuilder().append("Oops").append(e).toString());
        } finally {
          Close.SR(st2, rs2);
        }

        try
        {
          st2 = con.prepareStatement("SELECT no_exp, no_requests, autoloot, chatblock, charkey, traders, pathfind, skillchances, showshots FROM character_settings WHERE char_obj_id=?");
          st2.setInt(1, objectId);
          rs2 = st2.executeQuery();
          while (rs2.next()) {
            player.setNoExp(rs2.getInt("no_exp") == 1);
            player.setAlone(rs2.getInt("no_requests") == 1);
            player.setAutoLoot(rs2.getInt("autoloot") == 1);
            player.setChatIgnore(rs2.getInt("chatblock"));
            player.checkUserKey(rs2.getString("charkey"));
            player.setTradersIgnore(rs2.getInt("traders") == 1);
            player.setGeoPathfind(rs2.getInt("pathfind") == 1);
            player.setShowSkillChances(rs2.getInt("skillchances") == 1);
            player.setSoulShotsAnim(rs2.getInt("showshots") == 1);
          }
        } catch (SQLException e) {
          _log.severe(new StringBuilder().append("Oops2").append(e).toString());
        } finally {
          Close.SR(st2, rs2);
        }

        player.restoreSkills(con);
        player.getMacroses().restore(con);
        player.getShortCuts().restore(con);
        player.restoreHenna(con);
        if (Config.ALT_RECOMMEND)
        {
          player.restoreRecom(con);
        }
        if (!player.isSubClassActive())
        {
          player.restoreRecipeBook(con);
        }

        player.rewardSkills();
        player.refreshOverloaded();
        player.setClassUpdate(false);

        player.setEnterWorldHp(currentHp);
        player.setEnterWorldMp(currentMp);
        player.setEnterWorldCp(currentCp);

        player.restoreProfileBuffs(con);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      _log.severe(new StringBuilder().append("Could not restore char data: ").append(e.getMessage()).toString());
    } finally {
      Close.S(st3);
      Close.SR(st2, rs2);
      Close.CSR(con, st, rset);
    }
    return player;
  }

  public void setEnterWorldHp(double Hp)
  {
    _enterWorldHp = Hp;
  }

  public double getEnterWorldHp() {
    return _enterWorldHp;
  }

  public void setEnterWorldMp(double Mp) {
    _enterWorldMp = Mp;
  }

  public double getEnterWorldMp() {
    return _enterWorldMp;
  }

  public void setEnterWorldCp(double Cp) {
    _enterWorldCp = Cp;
  }

  public double getEnterWorldCp() {
    return _enterWorldCp;
  }

  public Forum getMail()
  {
    if (_forumMail == null) {
      setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));

      if (_forumMail == null) {
        ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), 4, 3, getObjectId());
        setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
      }
    }

    return _forumMail;
  }

  public void setMail(Forum forum)
  {
    _forumMail = forum;
  }

  public Forum getMemo()
  {
    if (_forumMemo == null) {
      setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));

      if (_forumMemo == null) {
        ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), 3, 3, getObjectId());
        setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
      }
    }

    return _forumMemo;
  }

  public void setMemo(Forum forum)
  {
    _forumMemo = forum;
  }

  private static boolean restoreSubClassData(L2PcInstance player)
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC");
      st.setInt(1, player.getObjectId());

      rset = st.executeQuery();

      while (rset.next()) {
        SubClass subClass = new SubClass();
        subClass.setClassId(rset.getInt("class_id"));
        subClass.setLevel(rset.getByte("level"));
        subClass.setExp(rset.getLong("exp"));
        subClass.setSp(rset.getInt("sp"));
        subClass.setClassIndex(rset.getInt("class_index"));

        player.getSubClasses().put(Integer.valueOf(subClass.getClassIndex()), subClass);
      }
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not restore classes for ").append(player.getName()).append(": ").append(e).toString());
      e.printStackTrace();
    } finally {
      Close.CSR(con, st, rset);
    }

    return true;
  }

  private void restoreRecipeBook(Connect con)
  {
    PreparedStatement st = null;
    ResultSet rset = null;
    try
    {
      st = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE char_id=?");
      st.setInt(1, getObjectId());
      rset = st.executeQuery();

      L2RecipeList recipe = null;
      RecipeController rc = RecipeController.getInstance();
      while (rset.next()) {
        recipe = rc.getRecipeList(rset.getInt("id") - 1);
        if (recipe == null)
        {
          continue;
        }
        if (rset.getInt("type") == 1) {
          registerDwarvenRecipeList(recipe); continue;
        }
        registerCommonRecipeList(recipe);
      }
    }
    catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not restore recipe book data:").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
  }

  public synchronized void store()
  {
    if (_fantome) {
      return;
    }

    setXYZ(getX(), getY(), getZ());

    storeCharBase();
  }

  private void storeCharBase() {
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      int currentClassIndex = getClassIndex();
      _classIndex = 0;
      long exp = getStat().getExp();
      int level = getStat().getLevel();
      int sp = getStat().getSp();
      _classIndex = currentClassIndex;

      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,last_recom_date=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,chat_filter_count=?, name_color=?, title_color=?, sex=?, hero=?, deaths=? WHERE obj_id=?");
      st.setInt(1, level);
      st.setInt(2, getMaxHp());
      st.setDouble(3, getCurrentHp());
      st.setInt(4, getMaxCp());
      st.setDouble(5, getCurrentCp());
      st.setInt(6, getMaxMp());
      st.setDouble(7, getCurrentMp());
      st.setInt(8, getSTR());
      st.setInt(9, getCON());
      st.setInt(10, getDEX());
      st.setInt(11, getINT());
      st.setInt(12, getMEN());
      st.setInt(13, getWIT());
      st.setInt(14, getAppearance().getFace());
      st.setInt(15, getAppearance().getHairStyle());
      st.setInt(16, getAppearance().getHairColor());
      st.setInt(17, getHeading());
      st.setInt(18, getX());
      st.setInt(19, getY());
      st.setInt(20, getZ());
      st.setLong(21, exp);
      st.setLong(22, getExpBeforeDeath());
      st.setInt(23, sp);
      st.setInt(24, getKarma());
      st.setInt(25, getPvpKills());
      st.setInt(26, getPkKills());
      st.setInt(27, getRecomHave());
      st.setInt(28, getRecomLeft());
      st.setInt(29, getClanId());
      st.setInt(30, getMaxLoad());
      st.setInt(31, getRace().ordinal());

      st.setInt(32, getClassId().getId());
      st.setLong(33, getDeleteTimer());
      st.setString(34, getTitle());
      st.setInt(35, getAccessLevel());
      st.setInt(36, isOnline());
      st.setInt(37, isIn7sDungeon() ? 1 : 0);
      st.setInt(38, getClanPrivileges());
      st.setInt(39, getWantsPeace());
      st.setInt(40, getBaseClass());

      long totalOnlineTime = _onlineTime;

      if (_onlineBeginTime > 0L) {
        totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000L;
      }

      st.setLong(41, totalOnlineTime);
      st.setInt(42, isInJail() ? 1 : 0);
      st.setLong(43, getJailTimer());
      st.setInt(44, isNewbie() ? 1 : 0);
      st.setInt(45, isNoble() ? 1 : 0);
      st.setLong(46, getPowerGrade());
      st.setInt(47, getPledgeType());
      st.setLong(48, getLastRecomUpdate());
      st.setInt(49, getLvlJoinedAcademy());
      st.setLong(50, getApprentice());
      st.setLong(51, getSponsor());
      st.setInt(52, getAllianceWithVarkaKetra());
      st.setLong(53, getClanJoinExpiryTime());
      st.setLong(54, getClanCreateExpiryTime());
      st.setString(55, super.getName());
      st.setLong(56, getDeathPenaltyBuffLevel());
      st.setInt(57, getPcPoints());
      st.setString(58, convertColor(Integer.toHexString(getAppearance().getNameColor()).toUpperCase()));
      st.setString(59, convertColor(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
      st.setInt(60, getAppearance().getSex() ? 1 : 0);
      st.setLong(61, _heroExpire);
      st.setLong(62, getDeaths());
      st.setInt(63, getObjectId());
      st.execute();

      storeCharSub(con);
      storeCharSettings(con);
      storeEffect(con);
      storeRecipeBook(con);
      _macroses.store(con);
      _shortCuts.store(con);
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("Could not store char base data: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
  }

  private void storeCharSub(Connect con) {
    if (getTotalSubClasses() > 0) {
      PreparedStatement st = null;
      try {
        con.setAutoCommit(false);
        st = con.prepareStatement("UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index=?");
        for (SubClass subClass : getSubClasses().values()) {
          st.setLong(1, subClass.getExp());
          st.setInt(2, subClass.getSp());
          st.setInt(3, subClass.getLevel());
          st.setInt(4, subClass.getClassId());
          st.setInt(5, getObjectId());
          st.setInt(6, subClass.getClassIndex());
          st.addBatch();
        }
        st.executeBatch();
        con.commit();
        con.setAutoCommit(true);
      }
      catch (SQLException e) {
        _log.warning(new StringBuilder().append("Could not store sub class data for ").append(getName()).append(": ").append(e).toString());
      } finally {
        Close.S(st);
      }
    }
  }

  private void storeCharSettings(Connect con) {
    PreparedStatement st = null;
    try {
      st = con.prepareStatement("UPDATE `character_settings` SET `no_exp`=?, `no_requests`=?, `autoloot`=?, `chatblock`=?, `charkey`=?, `traders`=?, `pathfind`=?, `skillchances`=? WHERE `char_obj_id`=?");
      st.setInt(1, isNoExp() ? 1 : 0);
      st.setInt(2, isAlone() ? 1 : 0);
      st.setInt(3, getAutoLoot() ? 1 : 0);
      st.setInt(4, getChatIgnore());
      st.setString(5, getUserKey().key);
      st.setInt(6, getTradersIgnore() ? 1 : 0);
      st.setInt(7, geoPathfind() ? 1 : 0);
      st.setInt(8, getShowSkillChances() ? 1 : 0);
      st.setInt(9, getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("storeCharSettings() error: ").append(e).toString());
    } finally {
      Close.S(st);
    }
  }

  private void storeEffect(Connect con) {
    if (!Config.STORE_SKILL_COOLTIME) {
      return;
    }

    PreparedStatement st = null;
    try
    {
      st = con.prepareStatement("DELETE FROM character_buffs WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, getClassIndex());
      st.execute();
      Close.S(st);

      int buff_index = 0;

      con.setAutoCommit(false);
      List storedSkills = new FastList();
      st = con.prepareStatement("INSERT INTO character_buffs (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)");

      FastTable effects = getAllEffectsTable();
      int i = 0; for (int n = effects.size(); i < n; i++) {
        L2Effect effect = (L2Effect)effects.get(i);
        if (effect == null)
        {
          continue;
        }
        if (effect.getSkill().isToggle())
        {
          continue;
        }
        int skillId = effect.getSkill().getId();
        if (storedSkills.contains(Integer.valueOf(skillId)))
        {
          continue;
        }
        if ((effect.getSkill().isAugment()) && (_activeAug != skillId))
        {
          continue;
        }
        storedSkills.add(Integer.valueOf(skillId));

        if (effect.getInUse()) {
          buff_index++;

          st.setInt(1, getObjectId());
          st.setInt(2, skillId);
          st.setInt(3, effect.getSkill().getLevel());
          st.setInt(4, effect.getCount());
          st.setInt(5, effect.getTime());

          if (_reuseTimeStamps.containsKey(Integer.valueOf(skillId))) {
            TimeStamp t = (TimeStamp)_reuseTimeStamps.get(Integer.valueOf(skillId));
            st.setLong(6, t.hasNotPassed() ? t.getReuse() : 0L);
            st.setDouble(7, t.hasNotPassed() ? t.getStamp() : 0.0D);
          } else {
            st.setLong(6, 0L);
            st.setDouble(7, 0.0D);
          }

          st.setInt(8, 0);
          st.setInt(9, getClassIndex());
          st.setInt(10, buff_index);
          st.addBatch();
        }
      }
      st.executeBatch();

      for (TimeStamp t : _reuseTimeStamps.values()) {
        if (t.hasNotPassed()) {
          buff_index++;
          int skillId = t.getSkill();
          if (storedSkills.contains(Integer.valueOf(skillId))) {
            continue;
          }
          storedSkills.add(Integer.valueOf(skillId));

          st.setInt(1, getObjectId());
          st.setInt(2, t.getSkill());
          st.setInt(3, -1);
          st.setInt(4, -1);
          st.setInt(5, -1);
          st.setLong(6, t.getReuse());
          st.setDouble(7, t.getStamp());
          st.setInt(8, 1);
          st.setInt(9, getClassIndex());
          st.setInt(10, buff_index);
          st.addBatch();
        }
      }
      st.executeBatch();
      con.commit();
      con.setAutoCommit(true);
    }
    catch (SQLException e)
    {
      _log.warning(new StringBuilder().append("Could not store char effect data: ").append(e).toString());
    } finally {
      Close.S(st);
    }
  }

  private void storeRecipeBook(Connect con)
  {
    if (isSubClassActive()) {
      return;
    }
    if ((getCommonRecipeBook().length == 0) && (getDwarvenRecipeBook().length == 0)) {
      return;
    }

    PreparedStatement st = null;
    try {
      st = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
      st.setInt(1, getObjectId());
      st.execute();
      Close.S(st);

      L2RecipeList[] recipes = getCommonRecipeBook();

      con.setAutoCommit(false);
      st = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,0)");
      for (int count = 0; count < recipes.length; count++) {
        st.setInt(1, getObjectId());
        st.setInt(2, recipes[count].getId());
        st.addBatch();
      }
      st.executeBatch();
      Close.S(st);

      recipes = getDwarvenRecipeBook();
      st = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,1)");
      for (int count = 0; count < recipes.length; count++) {
        st.setInt(1, getObjectId());
        st.setInt(2, recipes[count].getId());
        st.addBatch();
      }
      st.executeBatch();
      con.commit();
      con.setAutoCommit(true);
    }
    catch (SQLException e) {
      _log.warning(new StringBuilder().append("Could not store recipe book data: ").append(e).toString());
    } finally {
      Close.S(st);
    }
  }

  public int isOnline()
  {
    return _isOnline ? 1 : 0;
  }

  public boolean isIn7sDungeon() {
    return _isIn7sDungeon;
  }

  public L2Skill addSkill(L2Skill newSkill, boolean store)
  {
    L2Skill oldSkill = super.addSkill(newSkill);

    if (store) {
      storeSkill(newSkill, oldSkill, -1);
    }
    return oldSkill;
  }

  public L2Skill removeSkill(L2Skill skill, boolean store) {
    if (store) {
      return removeSkill(skill);
    }
    return super.removeSkill(skill);
  }

  public L2Skill removeSkill(L2Skill skill)
  {
    L2Skill oldSkill = super.removeSkill(skill);
    if (oldSkill != null) {
      Connect con = null;
      PreparedStatement st = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();

        st = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
        st.setInt(1, oldSkill.getId());
        st.setInt(2, getObjectId());
        st.setInt(3, getClassIndex());
        st.execute();
      } catch (SQLException e) {
        _log.warning(new StringBuilder().append("Error could not delete skill: ").append(e).toString());
      } finally {
        Close.CS(con, st);
      }
    }

    if (_shortCuts != null)
    {
      for (L2ShortCut sc : getAllShortCuts()) {
        if (sc == null)
        {
          continue;
        }
        if ((skill != null) && (sc.getId() == skill.getId()) && (sc.getType() == 2)) {
          deleteShortCut(sc.getSlot(), sc.getPage());
        }
      }
    }
    return oldSkill;
  }

  private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
  {
    int classIndex = _classIndex;

    if (newClassIndex > -1) {
      classIndex = newClassIndex;
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      if ((oldSkill != null) && (newSkill != null)) {
        st = con.prepareStatement("UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?");
        st.setInt(1, newSkill.getLevel());
        st.setInt(2, oldSkill.getId());
        st.setInt(3, getObjectId());
        st.setInt(4, classIndex);
        st.execute();
        Close.S(st);
      } else if (newSkill != null) {
        st = con.prepareStatement("INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)");
        st.setInt(1, getObjectId());
        st.setInt(2, newSkill.getId());
        st.setInt(3, newSkill.getLevel());
        st.setString(4, newSkill.getName());
        st.setInt(5, classIndex);
        st.execute();
        Close.S(st);
      } else {
        _log.warning("could not store new skill. its NULL");
      }
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("Error could not store char skills: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
  }

  private void restoreSkills(Connect con)
  {
    PreparedStatement st = null;
    ResultSet rset = null;
    try
    {
      st = con.prepareStatement(Config.ALT_SUBCLASS_SKILLS ? "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=?" : "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());

      if (!Config.ALT_SUBCLASS_SKILLS) {
        st.setInt(2, getClassIndex());
      }

      rset = st.executeQuery();

      L2Skill skill = null;
      SkillTable stbl = SkillTable.getInstance();

      while (rset.next()) {
        int id = rset.getInt("skill_id");
        int level = rset.getInt("skill_level");

        if (id > 9000)
        {
          continue;
        }
        skill = stbl.getInfo(id, level);
        if (skill == null)
        {
          continue;
        }

        super.addSkill(skill);
      }
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("Could not restore character skills: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
  }

  public void restoreEffects(Connect con_ex)
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      if (con_ex == null) {
        con = L2DatabaseFactory.getInstance().getConnection();
        con.setTransactionIsolation(1);
      } else {
        con = con_ex;
      }

      st = con.prepareStatement("SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime FROM character_buffs WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC");
      st.setInt(1, getObjectId());
      st.setInt(2, getClassIndex());
      st.setInt(3, 0);
      rset = st.executeQuery();

      L2Skill skill = null;
      SkillTable stbl = SkillTable.getInstance();
      while (rset.next()) {
        int skillId = rset.getInt("skill_id");
        int skillLvl = rset.getInt("skill_level");
        int effectCount = rset.getInt("effect_count");
        int effectCurTime = rset.getInt("effect_cur_time");
        double reuseDelay = rset.getInt("reuse_delay");
        double systime = rset.getDouble("systime");

        double remainingTime = systime - System.currentTimeMillis();

        if ((skillId == -1) || (effectCount == -1) || (effectCurTime == -1) || (reuseDelay < 0.0D))
        {
          continue;
        }
        skill = stbl.getInfo(skillId, skillLvl);
        if (skill == null)
        {
          continue;
        }
        skill.getEffects(this, this);
        if (remainingTime > 10.0D) {
          disableSkill(skillId, ()remainingTime);
          addTimeStamp(new TimeStamp(skillId, ()reuseDelay, ()systime));
        }

        L2Effect effect = null;
        FastTable effects = getAllEffectsTable();
        int i = 0; for (int n = effects.size(); i < n; i++) {
          effect = (L2Effect)effects.get(i);
          if (effect == null)
          {
            continue;
          }
          if (effect.getSkill().getId() == skillId) {
            effect.setCount(effectCount);
            effect.setFirstTime(effectCurTime);
          }
        }
      }
      Close.SR(st, rset);

      st = con.prepareStatement("SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime FROM character_buffs WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC");
      st.setInt(1, getObjectId());
      st.setInt(2, getClassIndex());
      st.setInt(3, 1);
      rset = st.executeQuery();

      while (rset.next()) {
        int skillId = rset.getInt("skill_id");
        double reuseDelay = rset.getDouble("reuse_delay");
        double systime = rset.getDouble("systime");

        double remainingTime = systime - System.currentTimeMillis();

        if (remainingTime < 10.0D)
        {
          continue;
        }
        disableSkill(skillId, ()remainingTime);
        addTimeStamp(new TimeStamp(skillId, ()reuseDelay, ()systime));
      }
      Close.SR(st, rset);

      con.setAutoCommit(false);
      st = con.prepareStatement("DELETE FROM character_buffs WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, getClassIndex());
      st.executeUpdate();
      con.commit();
    }
    catch (SQLException e) {
      _log.warning(new StringBuilder().append("Could not restore active effect data: ").append(e).toString());
    } finally {
      if (con_ex == null)
        Close.CSR(con, st, rset);
      else {
        Close.SR(st, rset);
      }
    }

    updateEffectIcons();
    if (con_ex == null)
    {
      broadcastUserInfo();
    }
  }

  private void restoreHenna(Connect con)
  {
    PreparedStatement st = null;
    ResultSet rset = null;

    L2Henna tpl = null;
    L2HennaInstance sym = null;
    HennaTable ht = HennaTable.getInstance();
    try
    {
      st = con.prepareStatement("SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, getClassIndex());
      rset = st.executeQuery();

      for (int i = 0; i < 3; i++) {
        _henna[i] = null;
      }

      while (rset.next()) {
        int slot = rset.getInt("slot");

        if ((slot < 1) || (slot > 3))
        {
          continue;
        }
        int symbol_id = rset.getInt("symbol_id");
        if (symbol_id != 0) {
          tpl = ht.getTemplate(symbol_id);
          if (tpl != null) {
            sym = new L2HennaInstance(tpl);
            _henna[(slot - 1)] = sym;
          }
        }
      }
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not restore henna: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }

    recalcHennaStats();
  }

  private void restoreRecom(Connect con)
  {
    PreparedStatement st = null;
    ResultSet rset = null;
    try
    {
      st = con.prepareStatement("SELECT char_id,target_id FROM character_recommends WHERE char_id=? LIMIT 1");
      st.setInt(1, getObjectId());
      rset = st.executeQuery();
      while (rset.next())
        _recomChars.add(Integer.valueOf(rset.getInt("target_id")));
    }
    catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not restore recommendations: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
  }

  public int getHennaEmptySlots()
  {
    int totalSlots = 1 + getClassId().level();

    for (int i = 0; i < 3; i++) {
      if (_henna[i] != null) {
        totalSlots--;
      }
    }

    if (totalSlots <= 0) {
      return 0;
    }

    return totalSlots;
  }

  public boolean removeHenna(int slot)
  {
    if ((slot < 1) || (slot > 3)) {
      return false;
    }

    slot--;

    if (_henna[slot] == null) {
      return false;
    }

    L2HennaInstance henna = _henna[slot];
    _henna[slot] = null;

    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, slot + 1);
      st.setInt(3, getClassIndex());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not remove char henna: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }

    recalcHennaStats();

    sendUserPacket(new HennaInfo(this));

    sendUserPacket(new UserInfo(this));

    getInventory().addItem("Henna", henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, this, null);

    sendUserPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(henna.getItemIdDye()).addNumber(henna.getAmountDyeRequire() / 2));
    sendItems(false);
    sendChanges();
    return true;
  }

  public boolean addHenna(L2HennaInstance henna)
  {
    if (getHennaEmptySlots() == 0) {
      sendUserPacket(Static.MAX_3_DYES);
      return false;
    }

    for (int i = 0; i < 3; i++) {
      if (_henna[i] == null) {
        _henna[i] = henna;

        recalcHennaStats();

        Connect con = null;
        PreparedStatement st = null;
        try {
          con = L2DatabaseFactory.getInstance().getConnection();
          st = con.prepareStatement("INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)");
          st.setInt(1, getObjectId());
          st.setInt(2, henna.getSymbolId());
          st.setInt(3, i + 1);
          st.setInt(4, getClassIndex());
          st.execute();
        } catch (SQLException e) {
          _log.warning(new StringBuilder().append("could not save char henna: ").append(e).toString());
        } finally {
          Close.CS(con, st);
        }

        sendUserPacket(new HennaInfo(this));

        sendUserPacket(new UserInfo(this));
        return true;
      }
    }
    return false;
  }

  private void recalcHennaStats()
  {
    _hennaINT = 0;
    _hennaSTR = 0;
    _hennaCON = 0;
    _hennaMEN = 0;
    _hennaWIT = 0;
    _hennaDEX = 0;

    for (int i = 0; i < 3; i++) {
      if (_henna[i] == null) {
        continue;
      }
      _hennaINT += _henna[i].getStatINT();
      _hennaSTR += _henna[i].getStatSTR();
      _hennaMEN += _henna[i].getStatMEM();
      _hennaCON += _henna[i].getStatCON();
      _hennaWIT += _henna[i].getStatWIT();
      _hennaDEX += _henna[i].getStatDEX();
    }

    _hennaINT = Math.min(_hennaINT, Config.MAX_HENNA_BONUS);
    _hennaSTR = Math.min(_hennaSTR, Config.MAX_HENNA_BONUS);
    _hennaMEN = Math.min(_hennaMEN, Config.MAX_HENNA_BONUS);
    _hennaCON = Math.min(_hennaCON, Config.MAX_HENNA_BONUS);
    _hennaWIT = Math.min(_hennaWIT, Config.MAX_HENNA_BONUS);
    _hennaDEX = Math.min(_hennaDEX, Config.MAX_HENNA_BONUS);
  }

  public L2HennaInstance getHenna(int slot)
  {
    if ((slot < 1) || (slot > 3)) {
      return null;
    }

    return _henna[(slot - 1)];
  }

  public int getHennaStatINT()
  {
    return _hennaINT;
  }

  public int getHennaStatSTR()
  {
    return _hennaSTR;
  }

  public int getHennaStatCON()
  {
    return _hennaCON;
  }

  public int getHennaStatMEN()
  {
    return _hennaMEN;
  }

  public int getHennaStatWIT()
  {
    return _hennaWIT;
  }

  public int getHennaStatDEX()
  {
    return _hennaDEX;
  }

  public void setChatBanned(boolean f) {
    _chatBanned = f;

    stopBanChatTask();
    if (isChatBanned()) {
      sendUserPacket(Static.CHAT_BLOCKED);
      if (_banchat_timer > 0L)
        _BanChatTask = ThreadPoolManager.getInstance().scheduleAi(new SchedChatUnban(this), _banchat_timer, true);
    }
    else {
      sendUserPacket(Static.CHAT_UNBLOCKED);
      setBanChatTimer(0L);
    }
    sendUserPacket(new EtcStatusUpdate(this));
  }

  public void setChatBannedForAnnounce(boolean f) {
    _chatBanned = f;

    stopBanChatTask();
    if (isChatBanned()) {
      sendUserPacket(Static.CHAT_BLOCKED);
      _BanChatTask = ThreadPoolManager.getInstance().scheduleAi(new SchedChatUnban(this), _banchat_timer, false);
    } else {
      sendUserPacket(Static.CHAT_UNBLOCKED);
      setBanChatTimer(0L);
    }
    sendEtcStatusUpdate();
  }

  public void setBanChatTimer(long timer) {
    _banchat_timer = timer;
  }

  public long getBanChatTimer() {
    if (_BanChatTask != null) {
      return _BanChatTask.getDelay(TimeUnit.MILLISECONDS);
    }
    return _banchat_timer;
  }

  public void stopBanChatTask() {
    if (_BanChatTask != null) {
      _BanChatTask.cancel(false);
      _BanChatTask = null;
    }
  }

  public boolean isChatBanned()
  {
    return _chatBanned;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    if (attacker == null) {
      return false;
    }

    if ((attacker == this) || (attacker == getPet())) {
      return false;
    }

    if (PeaceZone.getInstance().inPeace(this, attacker)) {
      return false;
    }

    if (attacker.isMonster()) {
      return true;
    }

    if ((getParty() != null) && (getParty().getPartyMembers().contains(attacker))) {
      return false;
    }

    if (isInOlympiadMode()) {
      L2PcInstance enemy = attacker.getPlayer();

      return (enemy.isInOlympiadMode()) && (enemy.getOlympiadGameId() == getOlympiadGameId()) && (isOlympiadCompStart());
    }

    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(attacker.getName()))) {
      return true;
    }

    if ((getClan() != null) && (attacker != null) && (getClan().isMember(attacker.getObjectId()))) {
      return false;
    }

    if ((getKarma() > 0) || (getPvpFlag() > 0)) {
      return true;
    }

    if (attacker.isPlayer())
    {
      if ((getDuel() != null) && (((L2PcInstance)attacker).getDuel() != null) && (getDuel().getDuelState(this) == Duel.DuelState.Fighting) && (getDuel() == ((L2PcInstance)attacker).getDuel()))
      {
        return true;
      }

      if ((isInsidePvpZone()) && (attacker.isInsidePvpZone())) {
        return true;
      }

      if (getClan() != null) {
        Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
        if (siege != null)
        {
          if ((siege.checkIsDefender(attacker.getClan())) && (siege.checkIsDefender(getClan()))) {
            return false;
          }

          if ((siege.checkIsAttacker(attacker.getClan())) && (siege.checkIsAttacker(getClan()))) {
            return false;
          }
        }

        if (Config.FREE_PVP) {
          return true;
        }

        if ((getClan() != null) && (((L2PcInstance)attacker).getClan() != null) && (getClan().isAtWarWith(((L2PcInstance)attacker).getClanId())) && (getWantsPeace() == 0) && (((L2PcInstance)attacker).getWantsPeace() == 0) && (!isAcademyMember()))
        {
          return true;
        }
      }
    } else if ((attacker.isL2SiegeGuard()) && 
      (getClan() != null)) {
      Siege siege = SiegeManager.getInstance().getSiege(this);
      return (siege != null) && (siege.checkIsAttacker(getClan()));
    }

    return false;
  }

  public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
  {
    if ((isSitting()) && (!skill.isPotion())) {
      sendActionFailed();
      sendUserPacket(Static.CANT_MOVE_SITTING);
      return;
    }

    if (inObserverMode()) {
      abortCast();
      sendActionFailed();
      sendUserPacket(Static.OBSERVERS_CANNOT_PARTICIPATE);
      return;
    }

    if ((_disabledSkills != null) && (_disabledSkills.contains(Integer.valueOf(skill.getId())))) {
      sendUserPacket(SystemMessage.id(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill.getId(), skill.getLevel()));
      return;
    }

    if (((skill.getId() == 13) || (skill.getId() == 299) || (skill.getId() == 448)) && (!SiegeManager.getInstance().checkIfOkToSummon(this, false))) {
      return;
    }

    if (isConfused()) {
      sendActionFailed();
      return;
    }

    L2Skill.SkillType sklType = skill.getSkillType();
    if ((sklType == L2Skill.SkillType.CHARGE) && (getCharges() >= 7)) {
      sendUserPacket(Static.FORCE_MAXLEVEL_REACHED);
      sendActionFailed();
      return;
    }

    if ((getCurrentSkill() != null) && (isCastingNow()))
    {
      if (skill.getId() == getCurrentSkill().getSkillId()) {
        sendActionFailed();
        return;
      }

      setQueuedSkill(skill, forceUse, dontMove);
      sendActionFailed();
      return;
    }
    if (getQueuedSkill() != null)
    {
      setQueuedSkill(null, false, false);
    }

    L2Object target = null;
    L2Skill.SkillTargetType sklTargetType = skill.getTargetType();
    Point3D worldPosition = getCurrentSkillWorldPosition();
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[sklTargetType.ordinal()])
    {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
    case 7:
      target = this;
      break;
    case 8:
      target = getPet();
      break;
    default:
      target = getTarget();
    }

    if ((target == null) && (!skill.isOffensive())) {
      target = this;
      setTarget(this);
      sendUserPacket(new MyTargetSelected(getObjectId(), 0));
    }

    if (target == null) {
      sendUserPacket(Static.TARGET_CANT_FOUND);
      sendActionFailed();
      return;
    }

    if ((target.isL2Door()) && (!target.isAttackable())) {
      return;
    }

    if ((isInDuel()) && (
      (!target.isPlayer()) || (((L2PcInstance)target).getDuel() != getDuel()))) {
      sendActionFailed();
      sendUserPacket(Static.CANT_IN_DUEL);
      return;
    }

    if (isSkillDisabled(skill.getId())) {
      sendActionFailed();
      sendUserPacket(SystemMessage.id(SystemMessageId.SKILL_NOT_AVAILABLE).addString(skill.getName()));
      return;
    }

    if (isAllSkillsDisabled()) {
      sendActionFailed();
      return;
    }

    if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)) {
      sendActionFailed();
      sendUserPacket(Static.NOT_ENOUGH_MP);
      return;
    }

    if (getCurrentHp() <= skill.getHpConsume()) {
      sendActionFailed();
      sendUserPacket(Static.NOT_ENOUGH_HP);
      return;
    }

    if (skill.getItemConsume() > 0)
    {
      L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());

      if ((requiredItems == null) || (requiredItems.getCount() < skill.getItemConsume()))
      {
        if (sklType == L2Skill.SkillType.SUMMON) {
          SystemMessage sm = SystemMessage.id(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1).addItemName(skill.getItemConsumeId()).addNumber(skill.getItemConsume());
          sendUserPacket(sm);
          sm = null;
          return;
        }

        sendUserPacket(Static.NOT_ENOUGH_ITEMS);
        return;
      }

    }

    if (!skill.getWeaponDependancy(this)) {
      sendActionFailed();
      return;
    }

    if ((isAlikeDead()) && (!skill.isPotion()) && (skill.getSkillType() != L2Skill.SkillType.FAKE_DEATH)) {
      sendActionFailed();
      return;
    }

    if ((isFishing()) && (sklType != L2Skill.SkillType.PUMPING) && (sklType != L2Skill.SkillType.REELING) && (sklType != L2Skill.SkillType.FISHING)) {
      sendUserPacket(Static.ONLY_FISHING_SKILLS_NOW);
      return;
    }

    if ((sklType == L2Skill.SkillType.SUMMON_NPC) && (skill.getId() != 2137) && (skill.getId() != 2138) && 
      (PeaceZone.getInstance().inPeace(this, target))) {
      sendActionFailed();
      return;
    }

    if (sklType == L2Skill.SkillType.SIEGEFLAG) {
      if (!isInSiegeFlagArea()) {
        sendActionFailed();
        return;
      }

      Castle castle = CastleManager.getInstance().getCastle(this);
      if ((castle == null) || (!castle.getSiege().getIsInProgress())) {
        sendActionFailed();
        return;
      }

    }

    updateLastTeleport(false);

    if (skill.isOffensive())
    {
      if ((target.isMonster()) || ((target.isL2Npc()) && (Config.ALLOW_HIT_NPC) && (forceUse)))
      {
        if ((skill.getCastRange() > 0) && 
          (sklTargetType == L2Skill.SkillTargetType.TARGET_SIGNET_GROUND) && 
          (!GeoData.getInstance().canSeeTarget(this, worldPosition))) {
          sendActionFailed();
          sendUserPacket(Static.CANT_SEE_TARGET);
          return;
        }

        setCurrentSkill(skill, forceUse, dontMove);

        super.useMagic(skill);
        return;
      }

      if ((PeaceZone.getInstance().inPeace(this, target)) && (skill.getId() != 347))
      {
        sendActionFailed();
        return;
      }

      if (!target.isAttackable()) {
        sendActionFailed();
        return;
      }

      if ((isInOlympiadMode()) && (!isOlympiadCompStart()) && (skill.isOffensive()) && (skill.getId() != 347)) {
        sendActionFailed();
        return;
      }

      if ((!target.isAutoAttackable(this)) && (!forceUse) && (sklTargetType != L2Skill.SkillTargetType.TARGET_AURA) && (sklTargetType != L2Skill.SkillTargetType.TARGET_CLAN) && (sklTargetType != L2Skill.SkillTargetType.TARGET_ALLY) && (sklTargetType != L2Skill.SkillTargetType.TARGET_PARTY) && (sklTargetType != L2Skill.SkillTargetType.TARGET_SELF))
      {
        sendActionFailed();
        return;
      }

      if ((!target.isAutoAttackable(this)) && (!forceUse)) {
        switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[sklTargetType.ordinal()]) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          break;
        default:
          sendActionFailed();
          return;
        }

      }

      if (dontMove)
      {
        if ((skill.getCastRange() > 0) && (!isInsideRadius(target, skill.getCastRange() + getTemplate().collisionRadius, false, false))) {
          sendActionFailed();
          sendUserPacket(Static.TARGET_TOO_FAR);
          return;
        }
      }
    }
    else if ((Config.PROTECT_GATE_PVP) && (PeaceZone.getInstance().outGate(this, target, skill.getId()))) {
      sendActionFailed();
      sendUserPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    if ((!skill.isOffensive()) && (target.isMonster()) && (!forceUse))
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[sklTargetType.ordinal()]) {
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
        break;
      default:
        switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[sklType.ordinal()]) {
        case 1:
        case 2:
        case 3:
          break;
        default:
          sendActionFailed();
          return;
        }
      }

    }

    if ((sklType == L2Skill.SkillType.SPOIL) && 
      (!target.isL2Monster())) {
      sendActionFailed();
      sendUserPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    if ((sklType == L2Skill.SkillType.SWEEP) && (target.isL2Attackable())) {
      int spoilerId = ((L2Attackable)target).getIsSpoiledBy();

      if (((L2Attackable)target).isDead()) {
        if (!((L2Attackable)target).isSpoil()) {
          sendActionFailed();
          sendUserPacket(Static.SWEEPER_FAILED_TARGET_NOT_SPOILED);
          return;
        }

        if ((getObjectId() != spoilerId) && (!isInLooterParty(spoilerId))) {
          sendActionFailed();
          sendUserPacket(Static.SWEEP_NOT_ALLOWED);
          return;
        }

      }

    }

    if ((sklType == L2Skill.SkillType.DRAIN_SOUL) && 
      (!target.isL2Monster())) {
      sendActionFailed();
      sendUserPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[sklTargetType.ordinal()])
    {
    case 1:
    case 2:
    case 4:
    case 5:
    case 6:
    case 7:
      break;
    case 3:
    default:
      if (checkPvpSkill(target, skill)) break;
      sendActionFailed();
      sendUserPacket(Static.TARGET_IS_INCORRECT);
      return;
    }

    if ((skill.getCastRange() > 0) && 
      (sklTargetType == L2Skill.SkillTargetType.TARGET_SIGNET_GROUND) && 
      (!GeoData.getInstance().canSeeTarget(this, worldPosition))) {
      sendActionFailed();
      sendUserPacket(Static.CANT_SEE_TARGET);
      return;
    }

    setCurrentSkill(skill, forceUse, dontMove);

    super.useMagic(skill);
  }

  public boolean isInLooterParty(int LooterId) {
    L2PcInstance looter = L2World.getInstance().getPlayer(LooterId);
    if (looter == null) {
      return false;
    }

    if ((isInParty()) && (getParty().isInCommandChannel())) {
      return getParty().getCommandChannel().getMembers().contains(looter);
    }

    if (isInParty()) {
      return getParty().getPartyMembers().contains(looter);
    }

    return false;
  }

  public boolean checkPvpSkill(L2Object target, L2Skill skill)
  {
    if ((target == null) || (skill == null)) {
      return true;
    }

    if ((target.isPlayer()) || (target.isL2Summon()))
    {
      L2PcInstance targetPlayer = null;
      if (target.isL2Summon())
        targetPlayer = target.getOwner();
      else {
        targetPlayer = (L2PcInstance)target;
      }

      if (targetPlayer == null) {
        return true;
      }

      if ((isInOlympiadMode()) && 
        (targetPlayer.isInOlympiadMode()) && (targetPlayer.getOlympiadGameId() == getOlympiadGameId()) && (isOlympiadCompStart())) {
        return true;
      }

      if ((!targetPlayer.equals(this)) && ((!isInDuel()) || (targetPlayer.getDuel() != getDuel()))) {
        if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(getName())) && (TvTEvent.isPlayerParticipant(targetPlayer.getName()))) {
          if ((skill.isPvpSkill()) || (skill.isHeroDebuff()) || (skill.isAOEpvp()))
          {
            if (TvTEvent.getParticipantTeamId(getName()) == TvTEvent.getParticipantTeamId(targetPlayer.getName()))
              return false;
          }
        }
        else if ((isInsideHotZone()) || (targetPlayer.isInsideHotZone())) {
          if ((skill.isPvpSkill()) || (skill.isHeroDebuff()) || (skill.isAOEpvp()))
          {
            if ((getParty() != null) && (getParty().getPartyMembers().contains(targetPlayer))) {
              return false;
            }

            if ((getClan() != null) && (targetPlayer.getClan() != null) && 
              (getClanId() == targetPlayer.getClanId())) {
              return false;
            }

            if ((getAllyId() != 0) && (targetPlayer.getAllyId() != 0) && (getAllyId() == targetPlayer.getAllyId()))
              return false;
          }
        }
        else if ((!isInsidePvpZone()) || (!targetPlayer.isInsidePvpZone())) {
          if ((skill.isPvpSkill()) || (skill.isHeroDebuff()) || (skill.isAOEpvp()))
          {
            if ((getClan() != null) && (targetPlayer.getClan() != null)) {
              if (hasClanWarWith(targetPlayer))
                return true;
              if (getClanId() == targetPlayer.getClanId()) {
                return false;
              }
            }

            if ((getParty() != null) && (getParty().getPartyMembers().contains(targetPlayer))) {
              return false;
            }

            if ((getClan() != null) && (targetPlayer.getClan() != null) && 
              (getClanId() == targetPlayer.getClanId())) {
              return false;
            }

            if ((getAllyId() != 0) && (targetPlayer.getAllyId() != 0) && (getAllyId() == targetPlayer.getAllyId())) {
              return false;
            }

            if ((targetPlayer.getPvpFlag() == 0) && (targetPlayer.getKarma() == 0))
              return false;
          }
          else if ((getCurrentSkill() != null) && (!getCurrentSkill().isCtrlPressed()) && (skill.isOffensive())) {
            if (hasClanWarWith(targetPlayer)) {
              return true;
            }
            if ((getParty() != null) && (getParty().getPartyMembers().contains(targetPlayer))) {
              return false;
            }

            if ((getClan() != null) && (targetPlayer.getClan() != null) && 
              (getClanId() == targetPlayer.getClanId())) {
              return false;
            }

            if ((getAllyId() != 0) && (targetPlayer.getAllyId() != 0) && (getAllyId() == targetPlayer.getAllyId())) {
              return false;
            }

            if ((targetPlayer.getPvpFlag() == 0) && (targetPlayer.getKarma() == 0)) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  public void consumeItem(int itemConsumeId, int itemCount)
  {
    if ((itemConsumeId != 0) && (itemCount != 0))
      destroyItemByItemId("Consume", itemConsumeId, itemCount, null, false);
  }

  public boolean isMageClass()
  {
    return getClassId().isMage();
  }

  public boolean isMounted()
  {
    return _mountType > 0;
  }

  public boolean checkLandingState()
  {
    if (isInsideZone(64)) {
      return true;
    }

    return (isInsideZone(4)) && ((getClan() == null) || (CastleManager.getInstance().getCastle(this) != CastleManager.getInstance().getCastleByOwner(getClan())) || (this != getClan().getLeader().getPlayerInstance()));
  }

  public boolean setMountType(int mountType)
  {
    if ((checkLandingState()) && (mountType == 2))
      return false;
    if ((isInsideCastleZone()) && (mountType == 1)) {
      return false;
    }

    if (_taskRentPet != null) {
      sendUserPacket(new SetupGauge(3, 0));
      _taskRentPet.cancel(true);
    }

    switch (mountType) {
    case 0:
      setIsFlying(false);
      setIsRiding(false);
      break;
    case 1:
      setIsRiding(true);
      if (!isNoble()) break;
      L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
      addSkill(striderAssaultSkill, false);
      break;
    case 2:
      setIsFlying(true);
    }

    _mountType = mountType;

    broadcastUserInfo();
    return true;
  }

  public int getMountType()
  {
    return _mountType;
  }

  public void updateAbnormalEffect()
  {
    sendChanges();
  }

  public void tempInvetoryDisable()
  {
    _inventoryDisable = true;

    ThreadPoolManager.getInstance().scheduleAi(new InventoryEnable(), 1500L, true);
  }

  public boolean isInvetoryDisabled()
  {
    return _inventoryDisable;
  }

  public Map<Integer, L2CubicInstance> getCubics()
  {
    return _cubics;
  }

  public void addCubic(int id, int level)
  {
    L2CubicInstance cubic = new L2CubicInstance(this, id, level);
    _cubics.put(Integer.valueOf(id), cubic);
  }

  public void delCubic(int id)
  {
    _cubics.remove(Integer.valueOf(id));
  }

  public L2CubicInstance getCubic(int id)
  {
    return (L2CubicInstance)_cubics.get(Integer.valueOf(id));
  }

  public String toString()
  {
    return new StringBuilder().append("player ").append(super.getName()).toString();
  }

  public int getEnchantEffect()
  {
    L2ItemInstance wpn = getActiveWeaponInstance();

    if (wpn == null) {
      return 0;
    }

    return Math.min(127, wpn.getEnchantLevel());
  }

  public void setLastFolkNPC(L2FolkInstance folkNpc)
  {
    _lastFolkNpc = folkNpc;
  }

  public L2FolkInstance getLastFolkNPC()
  {
    return _lastFolkNpc;
  }

  public void setSilentMoving(boolean f)
  {
    _isSilentMoving = f;
  }

  public boolean isSilentMoving()
  {
    return _isSilentMoving;
  }

  public boolean isFestivalParticipant()
  {
    return SevenSignsFestival.getInstance().isParticipant(this);
  }

  public void addAutoSoulShot(int itemId) {
    _activeSoulShots.put(Integer.valueOf(itemId), Integer.valueOf(itemId));
  }

  public void removeAutoSoulShot(int itemId) {
    _activeSoulShots.remove(Integer.valueOf(itemId));
  }

  public Map<Integer, Integer> getAutoSoulShot() {
    return _activeSoulShots;
  }

  public void rechargeAutoSoulShot(boolean a, boolean b, boolean c)
  {
    if (_fantome) {
      broadcastSoulShotsPacket(new MagicSkillUser(this, this, 2154, 1, 0, 0));
      return;
    }

    if ((_activeSoulShots == null) || (_activeSoulShots.isEmpty())) {
      return;
    }

    for (Iterator i$ = _activeSoulShots.values().iterator(); i$.hasNext(); ) { int itemId = ((Integer)i$.next()).intValue();
      L2ItemInstance item = getInventory().getItemByItemId(itemId);

      if (item != null) {
        if (b) {
          if (!c) {
            if (item.isMagicShot()) {
              IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);

              if (handler != null) {
                handler.useItem(this, item);
              }
            }
          }
          else if ((itemId == 6646) || (itemId == 6647)) {
            IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);

            if (handler != null) {
              handler.useItem(this, item);
            }
          }

        }

        if (a) {
          if (!c) {
            if (item.isFighterShot()) {
              IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);

              if (handler != null) {
                handler.useItem(this, item);
              }
            }
          }
          else if (itemId == 6645) {
            IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);

            if (handler != null)
              handler.useItem(this, item);
          }
        }
      }
      else
      {
        removeAutoSoulShot(itemId);
      }
    }
  }

  public int getClanPrivileges()
  {
    return _clanPrivileges;
  }

  public void setClanPrivileges(int n) {
    _clanPrivileges = n;
  }

  public void setPledgeClass(int classId)
  {
    _pledgeClass = classId;
  }

  public int getPledgeClass() {
    return _pledgeClass;
  }

  public void setPledgeType(int typeId) {
    _pledgeType = typeId;
  }

  public int getPledgeType() {
    return _pledgeType;
  }

  public int getApprentice() {
    return _apprentice;
  }

  public void setApprentice(int apprentice_id) {
    _apprentice = apprentice_id;
  }

  public int getSponsor() {
    return _sponsor;
  }

  public void setSponsor(int sponsor_id) {
    _sponsor = sponsor_id;
  }

  public void sendMessage(String txt)
  {
    sendUserPacket(SystemMessage.sendString(txt));
  }

  public void sendAdmResultMessage(String txt)
  {
    sendUserPacket(new CreatureSay(0, 0, "SYS", txt));
  }

  public void sendModerResultMessage(String txt) {
    sendUserPacket(new CreatureSay(0, 16, "ModerLog", txt));
  }

  public void sendHtmlMessage(String txt) {
    sendHtmlMessage("\u0423\u0432\u0435\u0434\u043E\u043C\u043B\u0435\u043D\u0438\u0435.", txt);
  }

  public void sendHtmlMessage(String type, String txt) {
    NpcHtmlMessage html = NpcHtmlMessage.id(0);
    html.setHtml(new StringBuilder().append("<html><body> ").append(type).append("<br>").append(txt).append("<br></body></html>").toString());
    sendUserPacket(html);
    html = null;
  }

  public void enterObserverMode(int x, int y, int z) {
    _obsX = getX();
    _obsY = getY();
    _obsZ = getZ();

    setTarget(null);
    stopMove(null);
    setIsParalyzed(true);
    setIsInvul(true);
    setChannel(0);
    sendUserPacket(new ObservationMode(x, y, z));
    setXYZ(x, y, z);

    _observerMode = 1;

    broadcastUserInfo();
  }

  public void enterOlympiadObserverMode(int x, int y, int z, int id, boolean storeCoords)
  {
    if (getPet() != null) {
      getPet().unSummon(this);
    }

    if (getCubics().size() > 0) {
      for (L2CubicInstance cubic : getCubics().values()) {
        cubic.stopAction();
        cubic.cancelDisappear();
      }

      getCubics().clear();
    }

    if (getParty() != null) {
      getParty().removePartyMember(this);
    }

    _olympiadGameId = id;
    _olympiadObserveId = id;
    if (isSitting()) {
      standUp();
    }
    if (storeCoords) {
      _obsX = getX();
      _obsY = getY();
      _obsZ = getZ();
    }
    setTarget(null);
    setIsInvul(true);
    setChannel(0);
    teleToLocation(x, y, z, true);
    sendUserPacket(new ExOlympiadMode(3));
    _observerMode = 1;

    broadcastUserInfo();
    if (getOlympiadObserveId() > -1) {
      OlympiadGame game = Olympiad.getOlympiadGame(getOlympiadObserveId());
      if (game != null)
        game.broadcastInfo(null, this, true);
    }
  }

  public void appearObserverMode()
  {
    _observerMode = 3;

    getKnownList().updateKnownObjects();

    if (getOlympiadObserveId() > -1) {
      OlympiadGame game = Olympiad.getOlympiadGame(getOlympiadObserveId());
      if (game != null)
        game.broadcastInfo(null, this, true);
    }
  }

  public void returnFromObserverMode()
  {
    _observerMode = 0;
    _olympiadObserveId = -1;
  }

  public void leaveObserverMode()
  {
    setTarget(null);
    setXYZ(_obsX, _obsY, _obsZ);
    setIsParalyzed(false);
    setChannel(1);
    setIsInvul(false);

    if (getAI() != null) {
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }

    _observerMode = 2;
    _olympiadObserveId = -1;
    sendUserPacket(new ObservationReturn(this));

    broadcastUserInfo();
  }

  public void leaveOlympiadObserverMode() {
    setTarget(null);
    sendUserPacket(new ExOlympiadMode(0));
    sendUserPacket(new ExOlympiadMatchEnd());
    teleToLocation(_obsX, _obsY, _obsZ, true);
    setChannel(1);
    setIsInvul(false);

    if (getAI() != null) {
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }

    Olympiad.removeSpectator(_olympiadObserveId, this);
    _olympiadGameId = -1;
    _olympiadObserveId = -1;
    _observerMode = 2;

    broadcastUserInfo();
  }

  public int getObserverMode() {
    return _observerMode;
  }

  public void updateNameTitleColor()
  {
    sendChanges();
  }

  public void setOlympiadSide(int i) {
    _olympiadSide = i;
  }

  public int getOlympiadSide() {
    return _olympiadSide;
  }

  public void setOlympiadGameId(int id) {
    _olympiadGameId = id;
  }

  public int getOlympiadGameId()
  {
    return _olympiadGameId;
  }

  public int getObsX() {
    return _obsX;
  }

  public int getObsY() {
    return _obsY;
  }

  public int getObsZ() {
    return _obsZ;
  }

  public boolean inObserverMode()
  {
    return _observerMode > 0;
  }

  public int getTeleMode() {
    return _telemode;
  }

  public void setTeleMode(int mode) {
    _telemode = mode;
  }

  public void setLoto(int i, int val) {
    _loto[i] = val;
  }

  public int getLoto(int i) {
    return _loto[i];
  }

  public void setRace(int i, int val) {
    _race[i] = val;
  }

  public int getRace(int i) {
    return _race[i];
  }

  public boolean getMessageRefusal() {
    return _messageRefusal;
  }

  public void setMessageRefusal(boolean f) {
    _messageRefusal = f;
    sendEtcStatusUpdate();
  }

  public void setDietMode(boolean f) {
    _dietMode = f;
  }

  public boolean getDietMode() {
    return _dietMode;
  }

  public void setTradeRefusal(boolean f) {
    _tradeRefusal = f;
  }

  public boolean getTradeRefusal() {
    return _tradeRefusal;
  }

  public void setExchangeRefusal(boolean f) {
    _exchangeRefusal = f;
  }

  public boolean getExchangeRefusal() {
    return _exchangeRefusal;
  }

  public BlockList getBlockList() {
    return _blockList;
  }

  public int getCount() {
    int count = 0;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT count FROM heroes WHERE char_name=?");
      st.setString(1, getName());
      rset = st.executeQuery();
      if (rset.next())
        count = rset.getInt("count");
    } catch (Exception e) {
    }
    finally {
      Close.CSR(con, st, rset);
    }
    return count;
  }

  public void setHero(boolean f) {
    if ((f) && ((_baseClass == _activeClass) || (Config.ALLOW_HERO_SUBSKILL))) {
      for (L2Skill s : HeroSkillTable.getHeroSkills())
        addSkill(s, false);
    }
    else {
      for (L2Skill s : HeroSkillTable.getHeroSkills()) {
        super.removeSkill(s);
      }
    }
    _hero = f;
    sendSkillList();
  }

  public void setIsInOlympiadMode(boolean b) {
    _inOlympiadMode = b;
  }

  public void setIsOlympiadStart(boolean b) {
    _OlympiadStart = b;
  }

  public boolean isOlympiadStart()
  {
    return _OlympiadStart;
  }

  public boolean isInOlympiadMode()
  {
    return _inOlympiadMode;
  }

  public int getOlympiadObserveId()
  {
    return _olympiadObserveId;
  }

  public boolean isOlympiadGameStart() {
    int id = _olympiadGameId;
    if (id < 0) {
      return false;
    }
    OlympiadGame og = Olympiad.getOlympiadGame(id);
    return (og != null) && (og.getState() == 1);
  }

  public boolean isOlympiadCompStart() {
    int id = _olympiadGameId;
    if (id < 0) {
      return false;
    }
    OlympiadGame og = Olympiad.getOlympiadGame(id);
    return (og != null) && (og.getState() == 2);
  }

  public boolean isOlympiadWait()
  {
    return _OlympiadCountdown;
  }

  public void setIsOlympiadWait(boolean flag) {
    _OlympiadCountdown = flag;
  }

  public boolean isHero()
  {
    return _hero;
  }

  public boolean isNoble() {
    return _noble;
  }

  public void setNoble(boolean f) {
    if (f) {
      for (L2Skill s : NobleSkillTable.getInstance().getNobleSkills())
        addSkill(s, false);
    }
    else {
      for (L2Skill s : NobleSkillTable.getInstance().getNobleSkills()) {
        super.removeSkill(s);
      }
    }
    _noble = f;

    sendSkillList();
  }

  public void setLvlJoinedAcademy(int lvl) {
    _lvlJoinedAcademy = lvl;
  }

  public int getLvlJoinedAcademy() {
    return _lvlJoinedAcademy;
  }

  public boolean isAcademyMember()
  {
    return _lvlJoinedAcademy > 0;
  }

  public void setTeam(int team) {
    _team = team;
    broadcastUserInfo();
    if (getPet() != null)
      getPet().broadcastPetInfo();
  }

  public int getTeam()
  {
    return _team;
  }

  public void setWantsPeace(int wantsPeace) {
    _wantsPeace = wantsPeace;
  }

  public int getWantsPeace() {
    return _wantsPeace;
  }

  public boolean isFishing()
  {
    return _fishing;
  }

  public void setFishing(boolean f) {
    _fishing = f;
  }

  public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
  {
    _alliedVarkaKetra = sideAndLvlOfAlliance;
  }

  public int getAllianceWithVarkaKetra() {
    return _alliedVarkaKetra;
  }

  public boolean isAlliedWithVarka() {
    return _alliedVarkaKetra < 0;
  }

  public boolean isAlliedWithKetra() {
    return _alliedVarkaKetra > 0;
  }

  public void sendSkillList() {
    sendSkillList(this);
  }

  public void sendSkillList(L2PcInstance player) {
    SkillList sl = new SkillList();
    if (player != null) {
      for (L2Skill s : player.getAllSkills()) {
        if (s == null) {
          continue;
        }
        if (s.getId() > 9000) {
          continue;
        }
        sl.addSkill(s.getId(), s.getLevel(), s.isPassive());
      }
    }
    sendUserPacket(sl);
  }

  public boolean addSubClass(int classId, int classIndex)
  {
    if ((getTotalSubClasses() == Config.MAX_SUBCLASS) || (classIndex == 0)) {
      return false;
    }

    if (getSubClasses().containsKey(Integer.valueOf(classIndex))) {
      return false;
    }

    SubClass newClass = new SubClass();
    newClass.setClassId(classId);
    newClass.setClassIndex(classIndex);

    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)");
      st.setInt(1, getObjectId());
      st.setInt(2, newClass.getClassId());
      st.setLong(3, newClass.getExp());
      st.setInt(4, newClass.getSp());
      st.setInt(5, newClass.getLevel());
      st.setInt(6, newClass.getClassIndex());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("WARNING: Could not add character sub class for ").append(getName()).append(": ").append(e).toString());
      int i = 0;
      return i; } finally { Close.CS(con, st);
    }

    getSubClasses().put(Integer.valueOf(newClass.getClassIndex()), newClass);

    ClassId subTemplate = ClassId.values()[classId];
    Collection skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);

    if (skillTree == null) {
      return true;
    }

    Object prevSkillList = new FastMap();

    for (L2SkillLearn skillInfo : skillTree) {
      if (skillInfo.getMinLevel() <= 40) {
        L2Skill prevSkill = (L2Skill)((Map)prevSkillList).get(Integer.valueOf(skillInfo.getId()));
        L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());

        if ((prevSkill != null) && (prevSkill.getLevel() > newSkill.getLevel()))
        {
          continue;
        }
        ((Map)prevSkillList).put(Integer.valueOf(newSkill.getId()), newSkill);
        storeSkill(newSkill, prevSkill, classIndex);
      }

    }

    return true;
  }

  public boolean modifySubClass(int classIndex, int newClassId)
  {
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setAutoCommit(false);

      st = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, classIndex);
      st.execute();
      Close.S(st);

      st = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, classIndex);
      st.execute();
      Close.S(st);

      st = con.prepareStatement("DELETE FROM character_buffs WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, classIndex);
      st.execute();
      Close.S(st);

      st = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, classIndex);
      st.execute();
      Close.S(st);

      st = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?");
      st.setInt(1, getObjectId());
      st.setInt(2, classIndex);
      st.execute();
      con.commit();
    }
    catch (SQLException e) {
      _log.warning(new StringBuilder().append("Could not modify sub class for ").append(getName()).append(" to class index ").append(classIndex).append(": ").append(e).toString());

      getSubClasses().remove(Integer.valueOf(classIndex));
      int i = 0;
      return i; } finally { Close.CS(con, st);
    }

    getSubClasses().remove(Integer.valueOf(classIndex));
    return addSubClass(newClassId, classIndex);
  }

  public boolean isSubClassActive() {
    return _classIndex > 0;
  }

  public Map<Integer, SubClass> getSubClasses() {
    if (_subClasses == null) {
      _subClasses = new FastMap();
    }

    return _subClasses;
  }

  public int getTotalSubClasses() {
    return getSubClasses().size();
  }

  public int getBaseClass() {
    return _baseClass;
  }

  public int getActiveClass() {
    return _activeClass;
  }

  public int getClassIndex() {
    return _classIndex;
  }

  private void setClassTemplate(int classId) {
    _activeClass = classId;

    L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId);

    if (t == null) {
      _log.severe(new StringBuilder().append("Missing template for classId: ").append(classId).toString());
      throw new Error();
    }

    setTemplate(t);
  }

  public boolean setActiveClass(int classIndex)
  {
    _classUpdate = true;

    L2ItemInstance under = getInventory().getPaperdollItem(0);
    if (under != null) {
      L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(under.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (L2ItemInstance element : unequipped) {
        iu.addModifiedItem(element);
      }
      sendUserPacket(iu);
    }

    for (L2ItemInstance temp : getInventory().getAugmentedItems()) {
      if ((temp != null) && (temp.isEquipped())) {
        temp.getAugmentation().removeBoni(this);
      }

    }

    if (_forceBuff != null) {
      abortCast();
    }

    if (getFairy() != null) {
      getFairy().unSummon(this);
    }

    store();
    _reuseTimeStamps.clear();

    if (classIndex == 0)
      setClassTemplate(getBaseClass());
    else {
      try {
        setClassTemplate(((SubClass)getSubClasses().get(Integer.valueOf(classIndex))).getClassId());
      } catch (Exception e) {
        _log.info(new StringBuilder().append("Could not switch ").append(getName()).append("'s sub class to class index ").append(classIndex).append(": ").append(e).toString());
        return false;
      }
    }
    _classIndex = classIndex;

    if (isInParty()) {
      getParty().recalculatePartyLevel();
    }

    if (!getCubics().isEmpty()) {
      for (L2CubicInstance cubic : getCubics().values()) {
        cubic.stopAction();
        cubic.cancelDisappear();
      }

      getCubics().clear();
    }

    if (_forceBuff != null) {
      _forceBuff.delete();
    }

    for (L2Skill oldSkill : getAllSkills()) {
      super.removeSkill(oldSkill);
    }

    if (isCursedWeaponEquiped()) {
      CursedWeaponsManager.getInstance().givePassive(_cursedWeaponEquipedId);
    }

    stopAllEffects();
    clearCharges();

    Connect con = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      if (isSubClassActive()) {
        _dwarvenRecipeBook.clear();
        _commonRecipeBook.clear();
      } else {
        restoreRecipeBook(con);
      }

      restoreSkills(con);

      restoreEffects(con);

      for (int i = 0; i < 3; i++) {
        _henna[i] = null;
      }

      restoreHenna(con);

      _shortCuts.restore(con);
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("setActiveClass [ERROR]: ").append(e).toString());
    } finally {
      Close.C(con);
    }

    restoreDeathPenaltyBuffLevel();

    regiveTemporarySkills();
    if (_clan != null) {
      _clan.addBonusEffects(this, true, false);
    }
    rewardSkills();
    if ((_disabledSkills != null) && (!_disabledSkills.isEmpty())) {
      _disabledSkills.clear();
    }

    sendEtcStatusUpdate();

    QuestState st = getQuestState("422_RepentYourSins");
    if (st != null) {
      st.exitQuest(true);
    }

    sendUserPacket(new HennaInfo(this));

    refreshOverloaded();

    _expertisePenalty = 0;
    _classUpdate = false;
    refreshExpertisePenalty();

    broadcastUserInfo();

    setExpBeforeDeath(0L);

    sendUserPacket(new ShortCutInit(this));

    if ((getKnownSkill(1324) != null) && (Config.SUMMON_CP_PROTECT)) {
      L2Skill pero = getKnownSkill(1324);
      int reuseDelay = (int)(pero.getReuseDelay() * getStat().getMReuseRate(pero));
      reuseDelay = (int)(reuseDelay * (333.0D / getMAtkSpd()));
      addTimeStamp(pero.getId(), reuseDelay);
      disableSkill(pero.getId(), reuseDelay);
    }

    broadcastPacket(new SocialAction(getObjectId(), 15));
    sendUserPacket(new SkillCoolTime(this));
    sendActionFailed();
    return true;
  }

  public void stopWarnUserTakeBreak() {
    if (_taskWarnUserTakeBreak != null) {
      _taskWarnUserTakeBreak.cancel(true);
      _taskWarnUserTakeBreak = null;
    }
  }

  public void startWarnUserTakeBreak() {
    if (_taskWarnUserTakeBreak == null)
      _taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000L, 7200000L);
  }

  public void stopRentPet()
  {
    stopRentPet(true);
  }

  private void stopRentPet(boolean unmount) {
    if (_taskRentPet != null)
    {
      if ((checkLandingState()) && (getMountType() == 2)) {
        teleToLocation(MapRegionTable.TeleportWhereType.Town);
      }

      if ((unmount) && (setMountType(0)))
      {
        if (isFlying()) {
          removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        }

        sendUserPacket(new SetupGauge(3, 0));
        _taskRentPet.cancel(true);
        broadcastPacket(new Ride(getObjectId(), 0, 0));
        setMountObjectID(0);
        broadcastUserInfo();
        _taskRentPet = null;
      }
    }
  }

  public void startRentPet(int seconds) {
    if (_taskRentPet == null) {
      sendUserPacket(new SetupGauge(3, seconds * 1000));
      _taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000L);
    }
  }

  public void startUnmountPet(int seconds) {
    if ((seconds == 0) || ((Config.EENC_ENABLE) && (Encounter.getEvent().isRegged(this)))) {
      return;
    }
    if (_taskRentPet == null) {
      sendUserPacket(new SetupGauge(3, seconds));
      _taskRentPet = ThreadPoolManager.getInstance().scheduleEffect(new RentPetTask(), seconds);
    }
  }

  public boolean isRentedPet() {
    return _taskRentPet != null;
  }

  public void stopWaterTask(int waterZone)
  {
    if ((waterZone != -5) && (_waterZone != waterZone)) {
      return;
    }

    if (_taskWater != null) {
      _taskWater.cancel(false);
      _taskWater = null;
      sendUserPacket(new SetupGauge(2, 0));
      sendChanges();
    }
  }

  public void startWaterTask(int waterZone)
  {
    _waterZone = waterZone;
    if (isDead()) {
      stopWaterTask(waterZone);
    } else if ((Config.ALLOW_WATER) && (_taskWater == null)) {
      int timeinwater = 86000;
      sendUserPacket(new SetupGauge(2, timeinwater));
      _taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000L);
      sendChanges();
    }
  }

  public boolean isInWater()
  {
    return _taskWater != null;
  }

  public void onPlayerEnter() {
    startWarnUserTakeBreak();

    if ((SevenSigns.getInstance().isSealValidationPeriod()) || (SevenSigns.getInstance().isCompResultsPeriod())) {
      if ((!isGM()) && (isIn7sDungeon()) && (SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())) {
        teleToLocation(MapRegionTable.TeleportWhereType.Town);
        setIsIn7sDungeon(false);
        sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
      }
    }
    else if ((!isGM()) && (isIn7sDungeon()) && (SevenSigns.getInstance().getPlayerCabal(this) == 0)) {
      teleToLocation(MapRegionTable.TeleportWhereType.Town);
      setIsIn7sDungeon(false);
      sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
    }

    updateJailState();

    if (_isInvul) {
      sendAdmResultMessage("Immortal mode.");
    }
    if (isInvisible()) {
      sendAdmResultMessage("Invisible mode.");
    }
    if (getMessageRefusal())
      sendAdmResultMessage("Message Refusal mode.");
  }

  public long getLastAccess()
  {
    return _lastAccess;
  }

  private void checkRecom(int recsHave, int recsLeft) {
    Calendar check = Calendar.getInstance();
    check.setTimeInMillis(_lastRecomUpdate);
    check.add(5, 1);

    Calendar min = Calendar.getInstance();

    _recomHave = recsHave;
    _recomLeft = recsLeft;

    if ((getStat().getLevel() < 10) || (check.after(min))) {
      return;
    }

    restartRecom();
  }

  public void restartRecom() {
    if (Config.ALT_RECOMMEND) {
      Connect con = null;
      PreparedStatement st = null;
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("DELETE FROM character_recommends WHERE char_id=?");
        st.setInt(1, getObjectId());
        st.execute();

        _recomChars.clear();
      } catch (Exception e) {
        _log.warning(new StringBuilder().append("could not clear char recommendations: ").append(e).toString());
      } finally {
        Close.CS(con, st);
      }
    }

    if (getStat().getLevel() < 20) {
      _recomLeft = 3;
      _recomHave -= 1;
    } else if (getStat().getLevel() < 40) {
      _recomLeft = 6;
      _recomHave -= 2;
    } else {
      _recomLeft = 9;
      _recomHave -= 3;
    }
    if (_recomHave < 0) {
      _recomHave = 0;
    }

    Calendar update = Calendar.getInstance();
    if (update.get(11) < 13) {
      update.add(5, -1);
    }
    update.set(11, 13);
    _lastRecomUpdate = update.getTimeInMillis();
  }

  public void doRevive()
  {
    super.doRevive();

    if (isPhoenixBlessed()) {
      stopPhoenixBlessing(null);
    }

    updateEffectIcons();
    sendEtcStatusUpdate();
    _reviveRequested = 0;
    _revivePower = 0.0D;

    L2Skill pero = getKnownSkill(1410);
    if ((pero != null) && (isInsidePvpZone())) {
      int reuseDelay = (int)(pero.getReuseDelay() * getStat().getMReuseRate(pero));
      reuseDelay = (int)(reuseDelay * (1380.0D / getMAtkSpd()));
      addTimeStamp(pero.getId(), reuseDelay);
      disableSkill(pero.getId(), reuseDelay);
      sendUserPacket(new SkillCoolTime(this));
    }

    if (getPvpFlag() != 0) {
      setPvpFlag(0);
    }

    if ((isInParty()) && (getParty().isInDimensionalRift()) && 
      (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ())))
      getParty().getDimensionalRift().memberRessurected(this);
  }

  public void doRevive(double revivePower)
  {
    restoreExp(revivePower);
    doRevive();
  }

  public void reviveRequest(L2PcInstance Reviver, L2Skill skill, boolean Pet)
  {
    if (_reviveRequested == 1) {
      if (_revivePet == Pet) {
        Reviver.sendUserPacket(Static.RES_HAS_ALREADY_BEEN_PROPOSED);
      }
      else if (Pet)
        Reviver.sendUserPacket(Static.PET_CANNOT_RES);
      else {
        Reviver.sendUserPacket(Static.MASTER_CANNOT_RES);
      }

      return;
    }
    if (((Pet) && (getPet() != null) && (getPet().isDead())) || ((!Pet) && (isDead()))) {
      _reviveRequested = 1;
      if (isPhoenixBlessed())
        _revivePower = 100.0D;
      else {
        _revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), Reviver.getWIT());
      }

      _reviveTime = (System.currentTimeMillis() + Config.RESURECT_ANSWER_TIME);
      _revivePet = Pet;
      sendUserPacket(new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST.getId(), Reviver.getName()));
    }
  }

  public void reviveAnswer(int answer) {
    if ((_reviveRequested != 1) || ((!isDead()) && (!_revivePet)) || ((_revivePet) && (getPet() != null) && (!getPet().isDead()))) {
      return;
    }

    if ((Config.RESURECT_ANSWER_TIME > 0) && (System.currentTimeMillis() > _reviveTime)) {
      sendUserPacket(Static.ANSWER_TIMEOUT);
      return;
    }

    if ((answer == 0) && (isPhoenixBlessed())) {
      stopPhoenixBlessing(null);
      stopAllEffects();
    }
    if (answer == 1) {
      if (!_revivePet) {
        if (_revivePower != 0.0D)
          doRevive(_revivePower);
        else
          doRevive();
      }
      else if (getPet() != null) {
        if (_revivePower != 0.0D)
          getPet().doRevive(_revivePower);
        else {
          getPet().doRevive();
        }
      }
    }
    _reviveRequested = 0;
    _revivePower = 0.0D;
  }

  public boolean isReviveRequested()
  {
    return _reviveRequested == 1;
  }

  public boolean isRevivingPet()
  {
    return _revivePet;
  }

  public void removeReviving() {
    _reviveRequested = 0;
    _revivePower = 0.0D;
  }

  public void onActionRequest() {
    setProtection(false);
  }

  public void setExpertiseIndex(int expertiseIndex)
  {
    _expertiseIndex = expertiseIndex;
  }

  public int getExpertiseIndex()
  {
    return _expertiseIndex;
  }

  public final void onTeleported()
  {
    super.onTeleported();

    revalidateZone(true);

    if (Config.PLAYER_SPAWN_PROTECTION > 0) {
      setProtection(true);
    }
    updateLastTeleport(true);

    if (getTrainedBeast() != null) {
      getTrainedBeast().getAI().stopFollow();
      getTrainedBeast().teleToLocation(getPosition().getX() + Rnd.get(-100, 100), getPosition().getY() + Rnd.get(-100, 100), getPosition().getZ(), false);
      getTrainedBeast().getAI().startFollow(this);
    }

    if (getPet() != null)
    {
      ((L2SummonAI)getPet().getAI()).setStartFollowController(true);
      getPet().setFollowStatus(true);
      getPet().updateAndBroadcastStatus(0);
    }

    if (getFairy() != null) {
      getFairy().getAI().stopFollow();
      getFairy().teleToLocation(getPosition().getX() + Rnd.get(-50, 50), getPosition().getY() + Rnd.get(-50, 50), getPosition().getZ(), false);

      getFairy().getAI().startFollow(this);
      getFairy().setFollowStatus(true);
      getFairy().updateAndBroadcastStatus(0);
    }

    if (_partner != null) {
      _partner.getAI().stopFollow();
      _partner.teleToLocation(getPosition().getX() + Rnd.get(-50, 50), getPosition().getY() + Rnd.get(-50, 50), getPosition().getZ(), false);

      _partner.getAI().startFollow(this);
      _partner.setFollowStatus(true);
      _partner.updateAndBroadcastPartnerStatus(0);
      _partner.onTeleported();
    }

    if (!_isPartner) {
      sendUserPacket(new UserInfo(this));
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
    }
  }

  public void setLastClientPosition(int x, int y, int z)
  {
    _lastClientPosition.setXYZ(x, y, z);
  }

  public boolean checkLastClientPosition(int x, int y, int z) {
    return _lastClientPosition.equals(x, y, z);
  }

  public int getLastClientDistance(int x, int y, int z) {
    double dx = x - _lastClientPosition.getX();
    double dy = y - _lastClientPosition.getY();
    double dz = z - _lastClientPosition.getZ();

    return (int)Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public void setLastServerPosition(int x, int y, int z) {
    _lastServerPosition.setXYZ(x, y, z);
  }

  public boolean checkLastServerPosition(int x, int y, int z) {
    return _lastServerPosition.equals(x, y, z);
  }

  public int getLastServerDistance(int x, int y, int z) {
    double dx = x - _lastServerPosition.getX();
    double dy = y - _lastServerPosition.getY();
    double dz = z - _lastServerPosition.getZ();

    return (int)Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public void addExpAndSp(long exp, int sp)
  {
    if ((Config.PREMIUM_ENABLE) && (isPremium())) {
      sp = (int)(sp * Config.PREMIUM_SP);
      exp = ()(exp * Config.PREMIUM_EXP);
    }
    getStat().addExpAndSp(exp, sp);
  }

  public void removeExpAndSp(long removeExp, int removeSp) {
    getStat().removeExpAndSp(removeExp, removeSp);
  }

  public void reduceCurrentHp(double i, L2Character attacker)
  {
    getStatus().reduceHp(i, attacker);

    if (getTrainedBeast() != null) {
      getTrainedBeast().onOwnerGotAttacked(attacker);
    }

    if (_isPartner) {
      _owner.sendMessage(new StringBuilder().append("\u041F\u0430\u0440\u0442\u043D\u0435\u0440 \u043F\u043E\u043B\u0443\u0447\u0430\u0435\u0442 ").append((int)i).append(" \u0443\u0440\u043E\u043D\u0430 \u043E\u0442 ").append(attacker.getName()).toString());
    }
    if (_partner != null)
      _partner.getAI().onOwnerGotAttacked(attacker);
  }

  public void reduceCurrentHp(double value, L2Character attacker, boolean awake)
  {
    getStatus().reduceHp(value, attacker, awake);

    if (getTrainedBeast() != null) {
      getTrainedBeast().onOwnerGotAttacked(attacker);
    }
    if (_isPartner) {
      _owner.sendMessage(new StringBuilder().append("\u041F\u0430\u0440\u0442\u043D\u0435\u0440 \u043F\u043E\u043B\u0443\u0447\u0430\u0435\u0442 ").append((int)value).append(" \u0443\u0440\u043E\u043D\u0430 \u043E\u0442 ").append(attacker.getName()).toString());
    }
    if (_partner != null)
      _partner.getAI().onOwnerGotAttacked(attacker);
  }

  public void reduceCurrentHp(double value, L2Character attacker, boolean awake, boolean hp)
  {
    getStatus().reduceHp(value, attacker, awake, true);

    if (getTrainedBeast() != null) {
      getTrainedBeast().onOwnerGotAttacked(attacker);
    }
    if (_isPartner) {
      _owner.sendMessage(new StringBuilder().append("\u041F\u0430\u0440\u0442\u043D\u0435\u0440 \u043F\u043E\u043B\u0443\u0447\u0430\u0435\u0442 ").append((int)value).append(" \u0443\u0440\u043E\u043D\u0430 \u043E\u0442 ").append(attacker.getName()).toString());
    }
    if (_partner != null)
      _partner.getAI().onOwnerGotAttacked(attacker);
  }

  public void broadcastSnoop(int type, String name, String _text)
  {
    Snoop sn;
    if (_snoopListener.size() > 0) {
      sn = new Snoop(getObjectId(), getName(), type, name, _text);
      for (L2PcInstance pci : _snoopListener)
        if (pci != null)
          pci.sendPacket(sn);
    }
  }

  public void addSnooper(L2PcInstance pci)
  {
    if (!_snoopListener.contains(pci))
      _snoopListener.add(pci);
  }

  public void removeSnooper(L2PcInstance pci)
  {
    _snoopListener.remove(pci);
  }

  public void addSnooped(L2PcInstance pci) {
    if (!_snoopedPlayer.contains(pci))
      _snoopedPlayer.add(pci);
  }

  public void removeSnooped(L2PcInstance pci)
  {
    _snoopedPlayer.remove(pci);
  }

  public synchronized void addBypass(String bypass) {
    if (bypass == null) {
      return;
    }
    _validBypass.add(bypass);
  }

  public synchronized void addBypass2(String bypass)
  {
    if (bypass == null) {
      return;
    }
    _validBypass2.add(bypass);
  }

  public synchronized boolean validateBypass(String cmd)
  {
    if (!Config.BYPASS_VALIDATION) {
      return true;
    }

    for (String bp : _validBypass) {
      if (bp == null)
      {
        continue;
      }

      if (bp.equals(cmd)) {
        return true;
      }
    }

    for (String bp : _validBypass2) {
      if (bp == null)
      {
        continue;
      }

      if (cmd.startsWith(bp)) {
        return true;
      }

    }

    return false;
  }

  public boolean validateItemManipulation(int objectId, String action) {
    L2ItemInstance item = getInventory().getItemByObjectId(objectId);

    if ((item == null) || (item.getOwnerId() != getObjectId())) {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" item he is not owner of").toString());
      return false;
    }

    if (((getPet() != null) && (getPet().getControlItemId() == objectId)) || (getMountObjectID() == objectId))
    {
      return false;
    }

    if ((getActiveEnchantItem() != null) && (getActiveEnchantItem().getObjectId() == objectId))
    {
      return false;
    }

    if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
    {
      return false;
    }

    return !item.isWear();
  }

  public synchronized void clearBypass()
  {
    _validBypass.clear();
    _validBypass2.clear();
  }

  public boolean isInBoat()
  {
    return _inBoat;
  }

  public void setInBoat(boolean f)
  {
    _inBoat = f;
  }

  public L2BoatInstance getBoat()
  {
    return _boat;
  }

  public void setBoat(L2BoatInstance boat)
  {
    _boat = boat;
  }

  public void setInCrystallize(boolean f) {
    _inCrystallize = f;
  }

  public boolean isInCrystallize() {
    return _inCrystallize;
  }

  public Point3D getInBoatPosition()
  {
    return _inBoatPosition;
  }

  public void setInBoatPosition(Point3D pt) {
    _inBoatPosition = pt;
  }

  public void deleteMe()
  {
    if ((_fantome) && (isVisible())) {
      try {
        decayMe();
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }
      return;
    }

    _isDeleting = true;
    setInGame(false);

    abortAttack();
    abortCast();

    if (getActiveTradeList() != null) {
      cancelActiveTrade();

      if (getTransactionRequester() != null) {
        getTransactionRequester().setTransactionRequester(null);
      }
      setTransactionRequester(null);
    }

    if (getTransactionRequester() != null) {
      getTransactionRequester().setTransactionRequester(null);
      setTransactionRequester(null);
    }

    if (inObserverMode()) {
      if (getOlympiadObserveId() == -1)
        leaveObserverMode();
      else {
        leaveOlympiadObserverMode();
      }
    }

    if ((Olympiad.isRegisteredInComp(this)) || (isInOlympiadMode()) || (getOlympiadGameId() > -1)) {
      Olympiad.logoutPlayer(this);
    }

    try
    {
      setOnlineStatus(false);
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      EventManager.getInstance().onExit(this);
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      stopAllTimers();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      RecipeController.getInstance().requestMakeItemAbort(this);
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      setTarget(null);
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    if (getWorldRegion() != null) {
      getWorldRegion().removeFromZones(this);
    }

    try
    {
      storeBuffProfiles();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      if (_forceBuff != null) {
        _forceBuff.delete();
      }
      for (L2Character character : getKnownList().getKnownCharacters())
        if ((character.getForceBuff() != null) && (character.getForceBuff().getTarget() == this))
          character.abortCast();
    }
    catch (Throwable t)
    {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      for (L2Effect effect : getAllEffectsTable()) {
        if (effect == null) {
          continue;
        }
        switch (effect.getEffectType()) {
        case SIGNET_GROUND:
        case SIGNET_EFFECT:
          effect.exit();
        }
      }
    }
    catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    if (isVisible()) {
      try {
        decayMe();
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }

    }

    if (isInParty()) {
      try {
        leaveOffParty();
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }

    }

    if (getFairy() != null) {
      try {
        getFairy().unSummon(this);
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }
    }
    if (getPet() != null) {
      try {
        getPet().unSummon(this);
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }
    }
    if (_partner != null)
      try {
        _partner.despawnMe();
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }
    try
    {
      if ((getClanId() > 0) && (getClan() != null)) {
        getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);

        L2ClanMember clanMember = getClan().getClanMember(getName());
        if (clanMember != null)
          clanMember.setPlayerInstance(null);
      }
    }
    catch (Throwable t) {
      _log.log(Level.SEVERE, "deletedMe()", t);
    }

    if (isGM()) {
      try {
        GmListTable.getInstance().deleteGm(this);
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }
    }

    try
    {
      getInventory().deleteMe();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      clearWarehouse();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    if (Config.WAREHOUSE_CACHE) {
      WarehouseCacheManager.getInstance().remCacheTask(this);
    }

    try
    {
      getFreight().deleteMe();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      getKnownList().removeAllKnownObjects();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    closeNetConnection();

    for (L2PcInstance player : _snoopedPlayer) {
      player.removeSnooper(this);
    }

    for (L2PcInstance player : _snoopListener) {
      player.removeSnooped(this);
    }

    if (_partyRoom != null) {
      PartyWaitingRoomManager.getInstance().exitRoom(this, _partyRoom);
    }

    L2World.getInstance().removePlayer(this);
  }

  public void gc()
  {
    _dwarvenRecipeBook = null;
    _commonRecipeBook = null;
    _recomChars = null;
    _warehouse = null;
    _freight = null;
    _activeTradeList = null;
    _activeWarehouse = null;
    _createList = null;
    _sellList = null;
    _buyList = null;
    _lastFolkNpc = null;
    _quests = null;
    _shortCuts = null;
    _macroses = null;
    _snoopListener = null;
    _snoopedPlayer = null;
    _skillLearningClassId = null;
    _summon = null;
    _tamedBeast = null;
    _clan = null;
    _currentSkillWorldPosition = null;
    _BanChatTask = null;
    _party = null;
    _arrowItem = null;
    _currentTransactionRequester = null;
    _fistsWeaponItem = null;
    _activeEnchantItem = null;
    _activeSoulShots = null;
    kills = null;
    _fishCombat = null;
    _taskRentPet = null;
    _taskWater = null;

    _forumMail = null;
    _forumMemo = null;
    _currentSkill = null;
    _queuedSkill = null;
    _forceBuff = null;
    _bbsMailSender = null;
    _bbsMailTheme = null;
    _euipWeapon = null;
    _userKey = null;
    voteAugm = null;
    _friends = null;
    _lastOptiClientPosition = null;
    _lastOptiServerPosition = null;
    _breuseTimeStamps = null;
    _profiles = null;
    _cubics = null;
    clearBypass();
  }

  public void startFishing(int _x, int _y, int _z)
  {
    stopMove(null);
    setIsImobilised(true);
    _fishing = true;
    _fishx = _x;
    _fishy = _y;
    _fishz = _z;
    broadcastUserInfo();

    int lvl = getRandomFishLvl();
    int group = getRandomFishGroup();
    int type = getRandomFishType(group);
    List fishs = FishTable.getInstance().getfish(lvl, type, group);
    if ((fishs == null) || (fishs.size() == 0)) {
      sendMessage("Error - Fishes are not definied");
      EndFishing(false);
      return;
    }
    int check = Rnd.get(fishs.size());

    _fish = new FishData((FishData)fishs.get(check));
    fishs.clear();
    fishs = null;
    sendUserPacket(Static.CAST_LINE_AND_START_FISHING);
    ExFishingStart efs = null;
    if ((!GameTimeController.getInstance().isNowNight()) && (_lure.isNightLure())) {
      _fish.setType(-1);
    }

    efs = new ExFishingStart(this, _fish.getType(), _x, _y, _z, _lure.isNightLure());
    broadcastPacket(efs);
    startLookingForFishTask();
  }

  public void stopLookingForFishTask() {
    if (_taskforfish != null) {
      _taskforfish.cancel(false);
      _taskforfish = null;
    }
  }

  public void startLookingForFishTask() {
    if ((!isDead()) && (_taskforfish == null)) {
      int checkDelay = 0;
      boolean isNoob = false;
      boolean isUpperGrade = false;

      if (_lure != null) {
        int lureid = _lure.getItemId();
        isNoob = _fish.getGroup() == 0;
        isUpperGrade = _fish.getGroup() == 2;
        if ((lureid == 6519) || (lureid == 6522) || (lureid == 6525) || (lureid == 8505) || (lureid == 8508) || (lureid == 8511))
        {
          checkDelay = Math.round((float)(_fish.getGutsCheckTime() * 1.33D));
        } else if ((lureid == 6520) || (lureid == 6523) || (lureid == 6526) || ((lureid >= 8505) && (lureid <= 8513)) || ((lureid >= 7610) && (lureid <= 7613)) || ((lureid >= 7807) && (lureid <= 7809)) || ((lureid >= 8484) && (lureid <= 8486)))
        {
          checkDelay = Math.round((float)(_fish.getGutsCheckTime() * 1.0D));
        } else if ((lureid == 6521) || (lureid == 6524) || (lureid == 6527) || (lureid == 8507) || (lureid == 8510) || (lureid == 8513))
        {
          checkDelay = Math.round((float)(_fish.getGutsCheckTime() * 0.66D));
        }
      }
      _taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(_fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob, isUpperGrade), 10000L, checkDelay);
    }
  }

  private int getRandomFishGroup() {
    switch (_lure.getItemId()) {
    case 7807:
    case 7808:
    case 7809:
    case 8486:
      return 0;
    case 8485:
    case 8506:
    case 8509:
    case 8512:
      return 2;
    }
    return 1;
  }

  private int getRandomFishType(int group)
  {
    int check = Rnd.get(100);
    int type = 1;
    switch (group) {
    case 0:
      switch (_lure.getItemId()) {
      case 7807:
        if (check <= 54)
          type = 5;
        else if (check <= 77)
          type = 4;
        else {
          type = 6;
        }
        break;
      case 7808:
        if (check <= 54)
          type = 4;
        else if (check <= 77)
          type = 6;
        else {
          type = 5;
        }
        break;
      case 7809:
        if (check <= 54)
          type = 6;
        else if (check <= 77)
          type = 5;
        else {
          type = 4;
        }
        break;
      case 8486:
        if (check <= 33)
          type = 4;
        else if (check <= 66)
          type = 5;
        else {
          type = 6;
        }
      }

      break;
    case 1:
      switch (_lure.getItemId()) {
      case 7610:
      case 7611:
      case 7612:
      case 7613:
        type = 3;
        break;
      case 6519:
      case 6520:
      case 6521:
      case 8505:
      case 8507:
        if (check <= 54)
          type = 1;
        else if (check <= 74)
          type = 0;
        else if (check <= 94)
          type = 2;
        else {
          type = 3;
        }
        break;
      case 6522:
      case 6523:
      case 6524:
      case 8508:
      case 8510:
        if (check <= 54)
          type = 0;
        else if (check <= 74)
          type = 1;
        else if (check <= 94)
          type = 2;
        else {
          type = 3;
        }
        break;
      case 6525:
      case 6526:
      case 6527:
      case 8511:
      case 8513:
        if (check <= 55)
          type = 2;
        else if (check <= 74)
          type = 1;
        else if (check <= 94)
          type = 0;
        else {
          type = 3;
        }
        break;
      case 8484:
        if (check <= 33)
          type = 0;
        else if (check <= 66)
          type = 1;
        else {
          type = 2;
        }
      }

      break;
    case 2:
      switch (_lure.getItemId()) {
      case 8506:
        if (check <= 54)
          type = 8;
        else if (check <= 77)
          type = 7;
        else {
          type = 9;
        }
        break;
      case 8509:
        if (check <= 54)
          type = 7;
        else if (check <= 77)
          type = 9;
        else {
          type = 8;
        }
        break;
      case 8512:
        if (check <= 54)
          type = 9;
        else if (check <= 77)
          type = 8;
        else {
          type = 7;
        }
        break;
      case 8485:
        if (check <= 33)
          type = 7;
        else if (check <= 66)
          type = 8;
        else {
          type = 9;
        }
      }
    }

    return type;
  }

  private int getRandomFishLvl() {
    int skilllvl = getSkillLevel(1315);
    FastTable effects = getAllEffectsTable();
    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect effect = (L2Effect)effects.get(i);
      if (effect == null) {
        continue;
      }
      if (effect.getSkill().getId() == 2274) {
        skilllvl = (int)effect.getSkill().getPower(this);
      }
    }
    if (skilllvl <= 0) {
      return 1;
    }

    int check = Rnd.get(100);
    int randomlvl;
    int randomlvl;
    if (check <= 50) {
      randomlvl = skilllvl;
    } else if (check <= 85) {
      int randomlvl = skilllvl - 1;
      if (randomlvl <= 0)
        randomlvl = 1;
    }
    else {
      randomlvl = skilllvl + 1;
      if (randomlvl > 27) {
        randomlvl = 27;
      }
    }
    return randomlvl;
  }

  public void StartFishCombat(boolean a, boolean b) {
    _fishCombat = new L2Fishing(this, _fish, a, b);
  }

  public void EndFishing(boolean f) {
    ExFishingEnd efe = new ExFishingEnd(f, this);
    broadcastPacket(efe);
    _fishing = false;
    _fishx = 0;
    _fishy = 0;
    _fishz = 0;
    broadcastUserInfo();
    if (_fishCombat == null) {
      sendUserPacket(Static.BAIT_LOST_FISH_GOT_AWAY);
    }
    _fishCombat = null;
    _lure = null;

    sendUserPacket(Static.REEL_LINE_AND_STOP_FISHING);
    setIsImobilised(false);
    stopLookingForFishTask();
  }

  public L2Fishing GetFishCombat() {
    return _fishCombat;
  }

  public int GetFishx() {
    return _fishx;
  }

  public int GetFishy() {
    return _fishy;
  }

  public int GetFishz() {
    return _fishz;
  }

  public void SetLure(L2ItemInstance lure) {
    _lure = lure;
  }

  public L2ItemInstance GetLure() {
    return _lure;
  }

  public int getInventoryLimit()
  {
    int ivlim;
    int ivlim;
    if (isGM()) {
      ivlim = Config.INVENTORY_MAXIMUM_GM;
    }
    else
    {
      int ivlim;
      if (getRace() == Race.dwarf)
        ivlim = Config.INVENTORY_MAXIMUM_DWARF;
      else
        ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
    }
    ivlim += (int)getStat().calcStat(Stats.INV_LIM, 0.0D, null, null);

    return ivlim;
  }

  public int getWareHouseLimit()
  {
    int whlim;
    int whlim;
    if (getRace() == Race.dwarf)
      whlim = Config.WAREHOUSE_SLOTS_DWARF;
    else {
      whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
    }
    whlim += (int)getStat().calcStat(Stats.WH_LIM, 0.0D, null, null);

    return whlim;
  }

  public int getPrivateSellStoreLimit()
  {
    int pslim;
    int pslim;
    if (getRace() == Race.dwarf)
      pslim = Config.MAX_PVTSTORE_SLOTS_DWARF;
    else {
      pslim = Config.MAX_PVTSTORE_SLOTS_OTHER;
    }
    pslim += (int)getStat().calcStat(Stats.P_SELL_LIM, 0.0D, null, null);

    return pslim;
  }

  public int getPrivateBuyStoreLimit()
  {
    int pblim;
    int pblim;
    if (getRace() == Race.dwarf)
      pblim = Config.MAX_PVTSTORE_SLOTS_DWARF;
    else {
      pblim = Config.MAX_PVTSTORE_SLOTS_OTHER;
    }
    pblim += (int)getStat().calcStat(Stats.P_BUY_LIM, 0.0D, null, null);

    return pblim;
  }

  public int getFreightLimit() {
    return Config.FREIGHT_SLOTS + (int)getStat().calcStat(Stats.FREIGHT_LIM, 0.0D, null, null);
  }

  public int getDwarfRecipeLimit() {
    int recdlim = Config.DWARF_RECIPE_LIMIT;
    recdlim += (int)getStat().calcStat(Stats.REC_D_LIM, 0.0D, null, null);
    return recdlim;
  }

  public int getCommonRecipeLimit() {
    int recclim = Config.COMMON_RECIPE_LIMIT;
    recclim += (int)getStat().calcStat(Stats.REC_C_LIM, 0.0D, null, null);
    return recclim;
  }

  public void setMountObjectID(int newID) {
    _mountObjectID = newID;
  }

  public int getMountObjectID() {
    return _mountObjectID;
  }

  public SkillDat getCurrentSkill()
  {
    return _currentSkill;
  }

  public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
  {
    if (currentSkill == null) {
      _currentSkill = null;
      return;
    }

    _currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
  }

  public SkillDat getQueuedSkill()
  {
    return _queuedSkill;
  }

  public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
  {
    if (queuedSkill == null)
    {
      _queuedSkill = null;
      return;
    }

    _queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
  }

  public boolean isInJail()
  {
    return _inJail;
  }

  public void setInJail(boolean f) {
    _inJail = f;
  }

  public void setInJail(boolean f, int delayInMinutes) {
    _inJail = f;
    _jailTimer = 0L;

    stopJailTask(false);

    if (_inJail) {
      if (delayInMinutes > 0) {
        _jailTimer = (delayInMinutes * 60000L);

        _jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
        sendMessage(new StringBuilder().append("\u0412\u0430\u0441 \u043F\u043E\u0441\u0430\u0434\u0438\u043B\u0438 \u0432 \u0442\u044E\u0440\u044C\u043C\u0443 \u043D\u0430 ").append(delayInMinutes).append(" \u043C\u0438\u043D\u0443\u0442.").toString());
      }

      if ((!TvTEvent.isInactive()) && (TvTEvent.isPlayerParticipant(getName()))) {
        TvTEvent.removeParticipant(getName());
      }

      NpcHtmlMessage htmlMsg = NpcHtmlMessage.id(5);
      TextBuilder build = new TextBuilder("<html><body>");
      build.append("<html><body>\u041F\u0440\u0430\u0432\u043E \u0437\u0430\u043D\u0438\u043C\u0430\u0442\u044C\u0441\u044F \u0441\u0430\u043C\u043E\u043E\u0431\u0440\u0430\u0437\u043E\u0432\u0430\u043D\u0438\u0435\u043C.<br>");
      build.append("\u041F\u0440\u0430\u0432\u043E \u0442\u0440\u0443\u0434\u0438\u0442\u044C\u0441\u044F.<br>");
      build.append("\u041F\u0440\u0430\u0432\u043E \u043D\u0430 \u043C\u0435\u0434\u0438\u0446\u0438\u043D\u0441\u043A\u043E\u0435 \u043E\u0431\u0435\u0441\u043F\u0435\u0447\u0435\u043D\u0438\u0435.<br>");
      build.append("\u041F\u0440\u0430\u0432\u043E \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u044F\u0442\u044C \u0440\u0435\u043B\u0438\u0433\u0438\u043E\u0437\u043D\u044B\u0435 \u043E\u0431\u0440\u044F\u0434\u044B.<br>");
      build.append("\u041F\u0440\u0430\u0432\u043E \u043E\u0431\u0440\u0430\u0449\u0430\u0442\u044C\u0441\u044F \u0441 \u043F\u0440\u0435\u0434\u043B\u043E\u0436\u0435\u043D\u0438\u044F\u043C\u0438, \u0437\u0430\u044F\u0432\u043B\u0435\u043D\u0438\u044F\u043C\u0438 \u0438 \u0436\u0430\u043B\u043E\u0431\u0430\u043C\u0438.<br>");
      build.append("\u041F\u0440\u0430\u0432\u043E \u043D\u0430 \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u0435 \u0431\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E\u0433\u043E \u043F\u0438\u0442\u0430\u043D\u0438\u044F.<br>");
      build.append("\u041F\u0440\u0430\u0432\u043E \u043D\u0430 \u043F\u0440\u043E\u0433\u0443\u043B\u043A\u0443.<br>");
      build.append("</body></html>");
      htmlMsg.setHtml(build.toString());
      sendUserPacket(htmlMsg);

      teleToJail();
    }
    else {
      NpcHtmlMessage htmlMsg = NpcHtmlMessage.id(0);
      htmlMsg.setHtml("<html><body>\u0412\u0435\u0434\u0438\u0442\u0435 \u0441\u0435\u0431\u044F \u0445\u043E\u0440\u043E\u0448\u043E!</body></html>");
      sendUserPacket(htmlMsg);

      teleToLocation(17836, 170178, -3507, true);
    }

    storeCharBase();
  }

  public long getJailTimer() {
    return _jailTimer;
  }

  public void setJailTimer(long time) {
    _jailTimer = time;
  }

  private void updateJailState() {
    if (isInJail())
    {
      if (_jailTimer > 0L)
      {
        _jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
        sendMessage(new StringBuilder().append("You are still in jail for ").append(Math.round((float)(_jailTimer / 60000L))).append(" minutes.").toString());
      }

      if (!isInsideZone(256))
        teleToLocation(-114356, -249645, -2984, true);
    }
  }

  public void stopJailTask(boolean f)
  {
    if (_jailTask != null) {
      if (f) {
        long delay = _jailTask.getDelay(TimeUnit.MILLISECONDS);
        if (delay < 0L) {
          delay = 0L;
        }
        setJailTimer(delay);
      }
      _jailTask.cancel(false);
      _jailTask = null;
    }
  }

  public int getPowerGrade()
  {
    return _powerGrade;
  }

  public void setPowerGrade(int power)
  {
    _powerGrade = power;
  }

  public boolean isCursedWeaponEquiped()
  {
    return _cursedWeaponEquipedId != 0;
  }

  public void setCursedWeaponEquipedId(int value) {
    _cursedWeaponEquipedId = value;
  }

  public int getCursedWeaponEquipedId() {
    return _cursedWeaponEquipedId;
  }

  public boolean getCharmOfCourage()
  {
    return _charmOfCourage;
  }

  public void setCharmOfCourage(boolean f) {
    _charmOfCourage = f;
    sendEtcStatusUpdate();
  }

  public int getDeathPenaltyBuffLevel() {
    return _deathPenaltyBuffLevel;
  }

  public void setDeathPenaltyBuffLevel(int level) {
    _deathPenaltyBuffLevel = level;
  }

  public void calculateDeathPenaltyBuffLevel(L2Character killer) {
    if ((killer.isPlayer()) || (killer.isL2Summon())) {
      return;
    }

    if ((killer.isRaid()) && (getCharmOfLuck())) {
      return;
    }

    if (Rnd.get(100) > Config.DEATH_PENALTY_CHANCE) {
      return;
    }

    increaseDeathPenaltyBuffLevel();
  }

  public void increaseDeathPenaltyBuffLevel() {
    if (getDeathPenaltyBuffLevel() >= 15)
    {
      return;
    }

    if (getDeathPenaltyBuffLevel() != 0) {
      L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

      if (skill != null) {
        removeSkill(skill, true);
      }
    }

    _deathPenaltyBuffLevel += 1;

    addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
    sendEtcStatusUpdate();
    sendUserPacket(SystemMessage.id(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(getDeathPenaltyBuffLevel()));
  }

  public void reduceDeathPenaltyBuffLevel() {
    if (getDeathPenaltyBuffLevel() <= 0) {
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

    if (skill != null) {
      removeSkill(skill, true);
    }

    _deathPenaltyBuffLevel -= 1;

    if (getDeathPenaltyBuffLevel() > 0) {
      addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
      sendUserPacket(SystemMessage.id(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(getDeathPenaltyBuffLevel()));
    } else {
      sendUserPacket(Static.DEATH_PENALTY_LIFTED);
    }

    sendEtcStatusUpdate();
  }

  public void restoreDeathPenaltyBuffLevel() {
    L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

    if (skill != null) {
      removeSkill(skill, true);
    }

    if (getDeathPenaltyBuffLevel() > 0)
      addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
  }

  public Collection<TimeStamp> getReuseTimeStamps()
  {
    return _reuseTimeStamps.values();
  }

  public Map<Integer, TimeStamp> getReuseTimeStamp() {
    return _reuseTimeStamps;
  }

  public void addTimeStamp(int s, int r)
  {
    _reuseTimeStamps.put(Integer.valueOf(s), new TimeStamp(s, r));
  }

  public void addTimeStamp(TimeStamp T)
  {
    _reuseTimeStamps.put(Integer.valueOf(T.getSkill()), T);
  }

  public void removeTimeStamp(int s)
  {
    _reuseTimeStamps.remove(Integer.valueOf(s));
  }

  public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
  {
    if (miss) {
      sendUserPacket(Static.MISSED_TARGET);
      return;
    }

    if ((isInOlympiadMode()) && ((target.isPlayer()) || (target.isL2Summon()))) {
      L2PcInstance enemy = target.getPlayer();
      if (enemy == null) {
        return;
      }

      if (getOlympiadGameId() != enemy.getOlympiadGameId()) {
        return;
      }

      OlympiadGame olymp_game = Olympiad.getOlympiadGame(getOlympiadGameId());
      if (olymp_game != null) {
        if (olymp_game.getState() <= 0) {
          return;
        }

        if (!equals(enemy)) {
          olymp_game.addDamage(enemy, Math.min(getCurrentHp() + getCurrentCp(), damage));
        }
      }

    }

    if (pcrit) {
      sendUserPacket(Static.CRITICAL_HIT);
    }
    if (mcrit) {
      sendUserPacket(Static.CRITICAL_HIT_MAGIC);
    }

    if (damage > 0) {
      if (!target.isPlayer()) {
        sendUserPacket(SystemMessage.id(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage));
      }

      if (_isPartner)
        _owner.sendMessage(new StringBuilder().append("\u041F\u0430\u0440\u0442\u043D\u0435\u0440 \u043D\u0430\u043D\u043E\u0441\u0438\u0442 ").append(damage).append(" \u0443\u0440\u043E\u043D\u0430 \u043F\u043E ").append(target.getName()).toString());
    }
  }

  public void checkBanChat(boolean f)
  {
    long banLength = 0L;
    String banReason = "";

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT chatban_timer FROM characters WHERE `obj_Id`=? LIMIT 1");
      st.setInt(1, getObjectId());
      rset = st.executeQuery();
      if (rset.next())
        banLength = rset.getLong("chatban_timer");
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Could not select chat ban info:").append(e).toString());
    } finally {
      Close.CSR(con, st, rset);
    }

    Calendar serv_time = Calendar.getInstance();
    long nowTime = serv_time.getTimeInMillis();
    banLength = (banLength - nowTime) / 1000L;

    if (banLength > 0L) {
      _chatBanned = true;
      setChatBanned(true, banLength, banReason);
    } else if ((_chatBanned) && (f)) {
      _chatBanned = false;
      setChatBanned(false, 0L, "");
    }
  }

  public void setChatBanned(boolean f, long banLength, String banReason)
  {
    _chatBanned = f;
    if (f) {
      long banLengthMs = TimeUnit.SECONDS.toMillis(banLength);
      ThreadPoolManager.getInstance().scheduleGeneral(new SchedChatUnban(this), banLengthMs);

      sendMessage(new StringBuilder().append("\u0427\u0430\u0442 \u0437\u0430\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D \u043D\u0430 (").append(banLength / 60L).append(") \u043C\u0438\u043D\u0443\u0442").toString());
      banLength = System.currentTimeMillis() + banLengthMs;
    } else {
      banLength = 0L;
      sendUserPacket(Static.CHAT_UNBLOCKED);
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE `characters` SET `chatban_timer`=? WHERE `obj_Id`=?");
      st.setLong(1, banLength);
      st.setInt(2, getObjectId());
      st.execute();
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not save chat ban info:").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
  }

  public ForceBuff getForceBuff()
  {
    return _forceBuff;
  }

  public void setForceBuff(ForceBuff fb) {
    _forceBuff = fb;
  }

  public boolean isRequestExpired()
  {
    return _requestExpireTime <= GameTimeController.getGameTicks();
  }

  public final boolean isInDangerArea()
  {
    return _isInDangerArea;
  }

  public final void setInDangerArea(boolean f) {
    _isInDangerArea = f;
    sendEtcStatusUpdate();
  }

  public final boolean isInSiegeFlagArea() {
    return _isInSiegeFlagArea;
  }

  public void setInSiegeFlagArea(boolean f)
  {
    _isInSiegeFlagArea = f;
  }

  public final boolean isInSiegeRuleArea() {
    return _isInSiegeRuleArea;
  }

  public void setInSiegeRuleArea(boolean f)
  {
    _isInSiegeRuleArea = f;
  }

  public final boolean isInOlumpiadStadium() {
    return _isInOlumpiadStadium;
  }

  public final void setInOlumpiadStadium(boolean f) {
    _isInOlumpiadStadium = f;
  }

  public void setInCastleWaitZone(boolean f)
  {
    _isInsideCastleWaitZone = f;
  }

  public final boolean isInsideCastleZone() {
    return _isInsideCastleZone;
  }

  public void setInCastleZone(boolean f)
  {
    _isInsideCastleZone = f;
  }

  public final boolean isInsideHotZone() {
    if (isInPVPArena()) {
      return true;
    }

    return _isInsideHotZone;
  }

  public void setInHotZone(boolean f)
  {
    _isInsideHotZone = f;
  }

  public final boolean isInsideDismountZone() {
    return _isInsideDismountZone;
  }

  public final void setInDismountZone(boolean f) {
    _isInsideDismountZone = f;
  }

  public final boolean isInColiseum() {
    return _isInColiseumZone;
  }

  public final void setInColiseum(boolean f) {
    _isInColiseumZone = f;
  }

  public final boolean isInElfTree() {
    return _isInMotherElfZone;
  }

  public final void setInElfTree(boolean f) {
    _isInMotherElfZone = f;
  }

  public final boolean isInBlockZone() {
    return _isInBlockZone;
  }

  public final void setInBlockZone(boolean f) {
    _isInBlockZone = f;
  }

  public boolean isInsideAqZone()
  {
    return _isInsideAqZone;
  }

  public final void setInAqZone(boolean f)
  {
    _isInsideAqZone = f;
  }

  public boolean isInsideSilenceZone()
  {
    return _isInsideSilenceZone;
  }

  public final void setInsideSilenceZone(boolean f)
  {
    _isInsideSilenceZone = f;
  }

  public final boolean isInZakenZone() {
    return _isInZakenZone;
  }

  public final void setInZakenZone(boolean f) {
    _isInZakenZone = f;
  }

  public final boolean isInPVPArena()
  {
    return _pvpArena;
  }

  public void setPVPArena(boolean f)
  {
    _pvpArena = f;
    super.setInsideZone(1, f);
  }

  public boolean isInsidePvpZone()
  {
    if ((_pvpArena) || (ZoneManager.getInstance().inPvpZone(this))) {
      return true;
    }

    return super.isInsidePvpZone();
  }

  public final boolean isInDino()
  {
    return _dinoIsle;
  }

  public void setInDino(boolean f)
  {
    _dinoIsle = f;
  }

  private String convertColor(String color)
  {
    switch (color.length()) {
    case 1:
      color = new StringBuilder().append("00000").append(color).toString();
      break;
    case 2:
      color = new StringBuilder().append("0000").append(color).toString();
      break;
    case 3:
      color = new StringBuilder().append("000").append(color).toString();
      break;
    case 4:
      color = new StringBuilder().append("00").append(color).toString();
      break;
    case 5:
      color = new StringBuilder().append("0").append(color).toString();
    }

    return color;
  }

  public void checkAllowedSkills() {
    if ((getLevel() == 19) || (getLevel() == 39)) {
      return;
    }

    if (isGM()) {
      return;
    }

    if (!Config.CHECK_SKILLS) {
      return;
    }

    if ((Config.PREMIUM_ENABLE) && (isPremium()) && (!Config.PREMIUM_CHKSKILLS)) {
      return;
    }

    Collection skillTree = SkillTreeTable.getInstance().getAllowedSkills(getClassId());

    for (L2Skill skill : getAllSkills()) {
      if (skill == null)
      {
        continue;
      }

      if (skill.isFishingSkill())
      {
        continue;
      }

      if (skill.isDwarvenSkill())
      {
        continue;
      }

      if (skill.isMiscSkill())
      {
        continue;
      }

      if ((isNoble()) && (skill.isNobleSkill()))
      {
        continue;
      }

      if ((isHero()) && (skill.isHerosSkill()))
      {
        continue;
      }

      if (getClan() != null) {
        if (skill.isClanSkill())
        {
          continue;
        }

        if ((isClanLeader()) && (skill.isSiegeSkill()))
        {
          continue;
        }
      }
      int skillid = skill.getId();

      if ((isCursedWeaponEquiped()) && (skillid == CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId).getSkillId()))
      {
        continue;
      }
      if ((skillid == 3603) && (getKarma() >= 900000))
      {
        continue;
      }

      Iterator i$ = skillTree.iterator();
      while (true) if (i$.hasNext()) { L2SkillLearn temp = (L2SkillLearn)i$.next();
          if (temp == null)
          {
            continue;
          }
          if (temp.getId() == skillid) break;
          continue;
        }
        else
        {
          removeSkill(skill);
        }
    }
  }

  public void checkHpMessages(double curHp, double newHp)
  {
    byte[] _hp = { 30, 30 };
    int[] skills = { 290, 291 };

    int[] _effects_skills_id = { 292, 292 };
    byte[] _effects_hp = { 30, 60 };

    double percent = getMaxHp() / 100.0D;
    double _curHpPercent = curHp / percent;
    double _newHpPercent = newHp / percent;
    boolean needsUpdate = false;

    L2Skill skill = null;
    SkillTable st = SkillTable.getInstance();

    for (int i = 0; i < skills.length; i++) {
      int level = getSkillLevel(skills[i]);
      skill = st.getInfo(skills[i], 1);
      if (level > 0) {
        if ((_curHpPercent > _hp[i]) && (_newHpPercent <= _hp[i])) {
          sendMessage(new StringBuilder().append("\u0422\u0430\u043A \u043A\u0430\u043A HP \u0443\u043C\u0435\u043D\u044C\u0448\u0438\u043B\u043E\u0441\u044C, \u0432\u044B \u043E\u0449\u0443\u0449\u0430\u0435\u0442\u0435 \u044D\u0444\u0444\u0435\u043A\u0442 \u043E\u0442 ").append(skill.getName()).toString());
          needsUpdate = true;
        } else if ((_curHpPercent <= _hp[i]) && (_newHpPercent > _hp[i])) {
          sendMessage(new StringBuilder().append("\u0422\u0430\u043A \u043A\u0430\u043A HP \u0443\u0432\u0435\u043B\u0438\u0447\u0438\u043B\u043E\u0441\u044C, \u044D\u0444\u0444\u0435\u043A\u0442 \u043E\u0442 ").append(skill.getName()).append(" \u043F\u0440\u043E\u043F\u0430\u0434\u0430\u0435\u0442").toString());
          needsUpdate = true;
        }
      }

    }

    for (int i = 0; i < _effects_skills_id.length; i++)
      try {
        if (getFirstEffect(_effects_skills_id[i]) != null) {
          skill = st.getInfo(_effects_skills_id[i], 1);
          if ((_curHpPercent > _effects_hp[i]) && (_newHpPercent <= _effects_hp[i])) {
            sendMessage(new StringBuilder().append("\u0422\u0430\u043A \u043A\u0430\u043A HP \u0443\u043C\u0435\u043D\u044C\u0448\u0438\u043B\u043E\u0441\u044C, \u0432\u044B \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u0440\u0438\u043C\u0435\u043D\u0438\u0442\u044C ").append(skill.getName()).toString());
            needsUpdate = true;
          } else if ((_curHpPercent <= _effects_hp[i]) && (_newHpPercent > _effects_hp[i])) {
            sendMessage(new StringBuilder().append("\u0422\u0430\u043A \u043A\u0430\u043A HP \u0443\u0432\u0435\u043B\u0438\u0447\u0438\u043B\u043E\u0441\u044C, \u044D\u0444\u0444\u0435\u043A\u0442 \u043E\u0442 ").append(skill.getName()).append(" \u043F\u0440\u043E\u043F\u0430\u0434\u0430\u0435\u0442").toString());

            needsUpdate = true;
          }
        }
      }
      catch (Exception e)
      {
      }
    if (needsUpdate)
      sendChanges();
  }

  public void checkDayNightMessages()
  {
    if (getSkillLevel(294) == -1) {
      return;
    }

    if (GameTimeController.getInstance().isNowNight())
      sendUserPacket(Static.SHADOW_SENSE_ON);
    else {
      sendUserPacket(Static.SHADOW_SENSE_OFF);
    }

    sendChanges();
  }

  public void refreshSavedStats()
  {
    _statsChangeRecorder.refreshSaves();
  }

  public void sendChanges()
  {
    _statsChangeRecorder.sendChanges();
  }

  public float getColRadius() {
    L2Summon pet = getPet();
    if ((isMounted()) && (pet != null)) {
      return pet.getTemplate().collisionRadius;
    }
    return getBaseTemplate().collisionRadius;
  }

  public float getColHeight()
  {
    L2Summon pet = getPet();
    if ((isMounted()) && (pet != null)) {
      return pet.getTemplate().collisionHeight;
    }
    return getBaseTemplate().collisionHeight;
  }

  public void updateStats()
  {
    refreshOverloaded();
    refreshExpertisePenalty();
    sendChanges();
  }

  public void restoreProfileBuffs(Connect con)
  {
    PreparedStatement st = null;
    ResultSet rset = null;
    try
    {
      st = con.prepareStatement("SELECT profile, buffs FROM `z_buff_profile` WHERE `char_id` = ? ORDER BY `profile`");
      st.setInt(1, getObjectId());
      rset = st.executeQuery();
      while (rset.next()) {
        int profile = rset.getInt("profile");
        _profiles.put(Integer.valueOf(profile), new FastMap());
        String buffs = rset.getString("buffs");
        String[] token = buffs.split(";");
        for (String bf : token) {
          if (bf.equals(""))
          {
            continue;
          }
          String[] buff = bf.split(",");
          Integer id = Integer.valueOf(buff[0]);
          Integer lvl = Integer.valueOf(buff[1]);
          if ((id == null) || (lvl == null))
          {
            continue;
          }
          L2Skill skill = SkillTable.getInstance().getInfo(id.intValue(), 1);
          if (skill == null)
          {
            continue;
          }
          if ((skill.isForbiddenProfileSkill()) || (skill.getSkillType() != L2Skill.SkillType.BUFF) || (skill.isChance()) || (skill.isAugment()))
          {
            continue;
          }
          ((FastMap)_profiles.get(Integer.valueOf(profile))).put(id, lvl);
        }
      }
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("Could not store ").append(getName()).append("'s buff profiles: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
  }

  public void doBuffProfile(int buffprofile) {
    if (System.currentTimeMillis() - _lastBuffProfile < 5000L) {
      sNotReady();
      return;
    }
    _lastBuffProfile = System.currentTimeMillis();

    FastMap profile = (FastMap)_profiles.get(Integer.valueOf(buffprofile));
    if ((profile == null) || (profile.isEmpty())) {
      sendUserPacket(Static.OOPS_ERROR);
      return;
    }

    stopAllEffects();
    SkillTable st = SkillTable.getInstance();
    FastMap.Entry e = profile.head(); for (FastMap.Entry end = profile.tail(); (e = e.getNext()) != end; ) {
      Integer id = (Integer)e.getKey();
      Integer lvl = (Integer)e.getValue();
      if ((id == null) || (lvl == null))
      {
        continue;
      }
      st.getInfo(id.intValue(), lvl.intValue()).getEffects(getBuffTarget(), getBuffTarget());
    }

    broadcastPacket(new MagicSkillUser(getBuffTarget(), getBuffTarget(), 264, 1, 1, 0));
  }

  public void saveBuffProfile(int buffprofile) {
    if (System.currentTimeMillis() - _lastBuffProfile < 5000L) {
      sNotReady();
      return;
    }
    _lastBuffProfile = System.currentTimeMillis();

    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      sendUserPacket(Static.OOPS_ERROR);
      return;
    }

    _profiles.remove(Integer.valueOf(buffprofile));
    _profiles.put(Integer.valueOf(buffprofile), new FastMap());
    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if ((e.getSkill().isForbiddenProfileSkill()) || (e.getSkill().getSkillType() != L2Skill.SkillType.BUFF) || (e.getSkill().isChance()) || (e.getSkill().isAugment()))
      {
        continue;
      }
      int id = e.getSkill().getId();
      int level = e.getSkill().getLevel();

      ((FastMap)_profiles.get(Integer.valueOf(buffprofile))).put(Integer.valueOf(id), Integer.valueOf(level));
    }

    sendUserPacket(Static.PROFILE_SAVED);
  }

  public void storeBuffProfiles() {
    if ((_profiles == null) || (_profiles.isEmpty())) {
      return;
    }

    TextBuilder pf = new TextBuilder();
    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setAutoCommit(false);
      st = con.prepareStatement("REPLACE INTO z_buff_profile (char_id,profile,buffs) VALUES (?,?,?)");
      FastMap.Entry e = _profiles.head(); for (FastMap.Entry end = _profiles.tail(); (e = e.getNext()) != end; ) {
        Integer profile = (Integer)e.getKey();
        FastMap data = (FastMap)e.getValue();
        if ((profile == null) || (data == null) || 
          (data.isEmpty())) {
          continue;
        }
        pf.clear();

        FastMap.Entry j = data.head(); for (FastMap.Entry endj = data.tail(); (j = j.getNext()) != endj; ) {
          Integer id = (Integer)j.getKey();
          Integer lvl = (Integer)j.getValue();
          if ((id == null) || (lvl == null))
          {
            continue;
          }
          pf.append(new StringBuilder().append(id).append(",").append(lvl).append(";").toString());
        }
        st.setInt(1, getObjectId());
        st.setInt(2, profile.intValue());
        st.setString(3, pf.toString());
        st.addBatch();
      }
      st.executeBatch();
      con.commit();
    } catch (SQLException h) {
      _log.warning(new StringBuilder().append("Could not store ").append(getName()).append("'s buff profile").append(h).toString());
    } finally {
      pf.clear();
      Close.CS(con, st);
    }
  }

  public void reloadSkills()
  {
    reloadSkills(true);
  }

  public void reloadSkills(boolean self) {
    if (self) {
      if (System.currentTimeMillis() - _lastReload < 5000L) {
        sNotReady();
        return;
      }
      _lastReload = System.currentTimeMillis();
    }

    _breuseTimeStamps = getReuseTimeStamps();
    for (TimeStamp ts : _breuseTimeStamps) {
      if ((ts.getSkill() == 1410) || (ts.getSkill() == 1324))
      {
        continue;
      }
      enableSkill(ts.getSkill());
    }

    sendUserPacket(new SkillCoolTime(this));
    sendSkillList();

    sendUserPacket(Static.SKILLS_RELOAD);
  }

  public boolean underAttack() {
    return AttackStanceTaskManager.getInstance().getAttackStanceTask(this);
  }

  public long getLastPacket()
  {
    return _lastPacket;
  }

  public void setLastPacket() {
    _lastPacket = System.currentTimeMillis();
  }

  public boolean isDeleting()
  {
    return _isDeleting;
  }

  public long getEnterWorld()
  {
    return _EnterWorld;
  }

  public void setEnterWorld() {
    _EnterWorld = System.currentTimeMillis();
  }

  public long getRequestGiveNickName()
  {
    return _requestGiveNickName;
  }

  public void setRequestGiveNickName() {
    _requestGiveNickName = System.currentTimeMillis();
  }

  public long getTitleChngedFail() {
    return _titleChngedFail;
  }

  public void setTitleChngedFail() {
    _titleChngedFail += 1;
  }

  public void clearTitleChngedFail() {
    _titleChngedFail = 0;
  }

  public long getCPA()
  {
    return _cpaae;
  }

  public void setCPA() {
    _cpaae = System.currentTimeMillis();
  }

  public long getCPB() {
    return _cpaad;
  }

  public void setCPB() {
    _cpaad = System.currentTimeMillis();
  }

  public long getCPC() {
    return _cpa;
  }

  public void setCPC() {
    _cpa = System.currentTimeMillis();
  }

  public long getCPD() {
    return _cpb;
  }

  public void setCPD() {
    _cpb = System.currentTimeMillis();
  }

  public long gCPE() {
    return _cpc;
  }

  public void sCPE() {
    _cpc = System.currentTimeMillis();
  }

  public long gCPF() {
    return _cpd;
  }

  public void sCPF() {
    _cpd = System.currentTimeMillis();
  }

  public long gCPG() {
    return _cpe;
  }

  public void sCPG() {
    _cpe = System.currentTimeMillis();
  }

  public long gCPH() {
    return _cpf;
  }

  public void sCPH() {
    _cpf = System.currentTimeMillis();
  }

  public long gCPJ() {
    return _cpg;
  }

  public void sCPJ() {
    _cpg = System.currentTimeMillis();
  }

  public long gCPK() {
    return _cph;
  }

  public void sCPK() {
    _cph = System.currentTimeMillis();
  }

  public long gCPL() {
    return _cpj;
  }

  public void sCPL() {
    _cpj = System.currentTimeMillis();
  }

  public long gCPM() {
    return _cpk;
  }

  public void sCPM() {
    _cpk = System.currentTimeMillis();
  }

  public long gCPN() {
    return _cpl;
  }

  public void sCPN() {
    _cpl = System.currentTimeMillis();
  }

  public long gCPO() {
    return _cpm;
  }

  public void sCPO() {
    _cpm = System.currentTimeMillis();
  }

  public long gCPP() {
    return _cpn;
  }

  public void sCPP() {
    _cpn = System.currentTimeMillis();
  }

  public long gCPR() {
    return _cpo;
  }

  public void sCPR() {
    _cpo = System.currentTimeMillis();
  }

  public long gCPS() {
    return _cpp;
  }

  public void sCPS() {
    _cpp = System.currentTimeMillis();
  }

  public long gCPT() {
    return _cpq;
  }

  public void sCPT() {
    _cpq = System.currentTimeMillis();
  }

  public long gCPU() {
    return _cpr;
  }

  public void sCPU() {
    _cpr = System.currentTimeMillis();
  }

  public long gCPV() {
    return _cps;
  }

  public void sCPV() {
    _cps = System.currentTimeMillis();
  }

  public long gCPW() {
    return _cpt;
  }

  public void sCPW() {
    _cpt = System.currentTimeMillis();
  }

  public long gCPX() {
    return _cpu;
  }

  public void sCPX() {
    _cpu = System.currentTimeMillis();
  }

  public long gCPY() {
    return _cpw;
  }

  public void sCPY() {
    _cpw = System.currentTimeMillis();
  }

  public long gCPZ() {
    return _cpx;
  }

  public void sCPZ() {
    _cpx = System.currentTimeMillis();
  }

  public long gCPAA() {
    return _cpv;
  }

  public void sCPAA() {
    _cpv = System.currentTimeMillis();
  }

  public long gCPAB() {
    return _cpy;
  }

  public void sCPAB() {
    _cpy = System.currentTimeMillis();
  }

  public long gCPAC() {
    return _cpz;
  }

  public void sCPAC() {
    _cpz = System.currentTimeMillis();
  }

  public long gCPAD() {
    return _cpaa;
  }

  public void sCPAD() {
    _cpaa = System.currentTimeMillis();
  }

  public long gCPAE() {
    return _cpab;
  }

  public void sCPAE() {
    _cpab = System.currentTimeMillis();
  }

  public long gCPAF() {
    return _cpac;
  }

  public void sCPAF() {
    _cpac = System.currentTimeMillis();
  }

  public long gCPAG() {
    return _cpad;
  }

  public void sCPAG() {
    _cpad = System.currentTimeMillis();
  }

  public long gCPAH() {
    return _cpae;
  }

  public void sCPAH() {
    _cpae = System.currentTimeMillis();
  }

  public long gCPAJ() {
    return _cpaf;
  }

  public void sCPAJ() {
    _cpaf = System.currentTimeMillis();
  }

  public long gCPAK() {
    return _cpag;
  }

  public void sCPAK() {
    _cpag = System.currentTimeMillis();
  }

  public long gCPAL() {
    return _cpah;
  }

  public void sCPAL() {
    _cpah = System.currentTimeMillis();
  }

  public long gCPAM() {
    return _cpaj;
  }

  public void sCPAM() {
    _cpaj = System.currentTimeMillis();
  }

  public long gCPAN() {
    return _cpak;
  }

  public void sCPAN() {
    _cpak = System.currentTimeMillis();
  }

  public long gCPAO() {
    return _cpal;
  }

  public void sCPAO() {
    _cpal = System.currentTimeMillis();
  }

  public long gCPAP() {
    return _cpam;
  }

  public void sCPAP() {
    _cpam = System.currentTimeMillis();
  }

  public long gCPAQ() {
    return _cpan;
  }

  public void sCPAQ() {
    _cpan = System.currentTimeMillis();
  }

  public long gCPAR() {
    return _cpao;
  }

  public void sCPAR() {
    _cpao = System.currentTimeMillis();
  }

  public long gCPAS() {
    return _cpap;
  }

  public void sCPAS() {
    _cpap = System.currentTimeMillis();
  }

  public long gCPAT() {
    return _cpaq;
  }

  public void sCPAT() {
    _cpaq = System.currentTimeMillis();
  }

  public long gCPAU() {
    return _cpar;
  }

  public void sCPAU() {
    _cpar = System.currentTimeMillis();
  }

  public long gCPAV() {
    return _cpas;
  }

  public void sCPAV() {
    _cpas = System.currentTimeMillis();
  }

  public long gCPAW() {
    return _cpat;
  }

  public void sCPAW() {
    _cpat = System.currentTimeMillis();
  }

  public long gCPAX() {
    return _cpau;
  }

  public void sCPAX() {
    _cpau = System.currentTimeMillis();
  }

  public long gCPAY() {
    return _cpav;
  }

  public void sCPAY() {
    _cpav = System.currentTimeMillis();
  }

  public long gCPAZ() {
    return _cpaw;
  }

  public void sCPAZ() {
    _cpaw = System.currentTimeMillis();
  }

  public long gCPBA() {
    return _cpax;
  }

  public void sCPBA() {
    _cpax = System.currentTimeMillis();
  }

  public long gCPBB() {
    return _cpay;
  }

  public void sCPBB() {
    _cpay = System.currentTimeMillis();
  }

  public long gCPBC() {
    return _cpaz;
  }

  public void sCPBC() {
    _cpaz = System.currentTimeMillis();
  }

  public long gCPBD() {
    return _cpaaa;
  }

  public void sCPBD() {
    _cpaaa = System.currentTimeMillis();
  }

  public long gCPBE() {
    return _cpaab;
  }

  public void sCPBE() {
    _cpaab = System.currentTimeMillis();
  }

  public long gCPBF() {
    return _cpaac;
  }

  public void sCPBF() {
    _cpaac = System.currentTimeMillis();
  }

  public long gCPBG() {
    return _cpaaf;
  }

  public void sCPBG() {
    _cpaaf = System.currentTimeMillis();
  }

  public long gCPBH() {
    return _cpaag;
  }

  public void sCPBH() {
    _cpaag = System.currentTimeMillis();
  }

  public long gCPBJ() {
    return _cpaah;
  }

  public void sCPBJ() {
    _cpaah = System.currentTimeMillis();
  }

  public void setWaitEquip(boolean f)
  {
    _equiptask = f;
  }

  public boolean isWaitEquip() {
    return _equiptask;
  }

  public int getItemCount(int itemId)
  {
    L2ItemInstance coins = getInventory().getItemByItemId(itemId);
    if (coins == null) {
      return 0;
    }

    return coins.getCount();
  }

  public boolean hasItems(FastList<Integer> items)
  {
    if ((items == null) || (items.isEmpty())) {
      return false;
    }
    Integer id = null;
    FastList.Node n = items.head(); for (FastList.Node end = items.tail(); (n = n.getNext()) != end; ) {
      id = (Integer)n.getValue();
      if ((id == null) || 
        (getInventory().getItemByItemId(id.intValue()) == null)) continue;
      id = null;
      return true;
    }

    id = null;
    return false;
  }

  public void setWorldIgnore(boolean f)
  {
    _antiWorldChat = f;
  }

  public boolean isWorldIgnore() {
    return _antiWorldChat;
  }

  public boolean isModerator()
  {
    if (_cmoder) {
      Connect con = null;
      PreparedStatement st = null;
      ResultSet result = null;
      try {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("SELECT * FROM `z_moderator` WHERE `moder`=? LIMIT 1");
        st.setInt(1, getObjectId());
        result = st.executeQuery();
        if (result.next())
          _moder = true;
      }
      catch (Exception e) {
        _log.warning(new StringBuilder().append("isModerator() error: ").append(e).toString());
      } finally {
        Close.CSR(con, st, result);
      }
      _cmoder = false;
    }
    return _moder;
  }

  public String getForumName() {
    return getName();
  }

  public int getModerRank() {
    return 3;
  }

  public void logModerAction(String Moder, String Action) {
    Moderator.getInstance().logWrite(getName(), Action);
  }

  public void setOptiLastClientPosition(Location position)
  {
    _lastOptiClientPosition = position;
  }

  public Location getOptiLastClientPosition() {
    return _lastOptiClientPosition;
  }

  public void setOptiLastServerPosition(Location position) {
    _lastOptiServerPosition = position;
  }

  public Location getOptiLastServerPosition() {
    return _lastOptiServerPosition;
  }

  public final boolean isFalling(int z)
  {
    if ((isDead()) || (isFlying()) || (isInWater())) {
      return false;
    }

    if (System.currentTimeMillis() < _fallingTimestamp) {
      return true;
    }

    int deltaZ = getZ() - z;
    if (deltaZ <= 400) {
      return false;
    }

    giveDamageFall(deltaZ);
    setFalling();
    return false;
  }

  public final void setFalling() {
    _fallingTimestamp = (System.currentTimeMillis() + 4000L);
  }

  public void giveDamageFall(int damage) {
    int hp = (int)getCurrentHp() - damage;
    if (hp < 1)
      setCurrentHp(1.0D);
    else {
      setCurrentHp(hp);
    }
    sendMessage(new StringBuilder().append("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 ").append(damage).append(" \u0443\u0440\u043E\u043D\u0430 \u043F\u0440\u0438 \u043F\u0430\u0434\u0435\u043D\u0438\u0438 \u0441 \u0432\u044B\u0441\u043E\u0442\u044B.").toString());
  }

  public void storeFriend(int fId, String fName)
  {
    _friends.put(Integer.valueOf(fId), fName);
  }

  public void deleteFriend(int friendId) {
    _friends.remove(Integer.valueOf(friendId));
  }

  public boolean haveFriend(int friendId) {
    return _friends.containsKey(Integer.valueOf(friendId));
  }

  public FastMap<Integer, String> getFriends() {
    return _friends;
  }

  public void setGroundSkillLoc(Location location)
  {
    _groundSkillLoc = location;
  }

  public Location getGroundSkillLoc() {
    return _groundSkillLoc;
  }

  public boolean isUltimate()
  {
    int[] imBuffs = { 313, 110, 368 };

    for (int buff : imBuffs) {
      if (getFirstEffect(buff) != null) {
        return true;
      }
    }

    return false;
  }

  public boolean canPotion() {
    int[] imBuffs = { 2002, 2031, 2032 };

    for (int buff : imBuffs) {
      if (getFirstEffect(buff) != null) {
        return true;
      }
    }

    return false;
  }

  public void clearPotions() {
    int[] imBuffs = { 2002, 2031, 2032 };

    for (int buff : imBuffs)
      if (getFirstEffect(buff) != null)
        stopSkillEffects(buff);
  }

  public void stopMoving()
  {
    stopMove(null);
  }

  public long getCpReuseTime(int itemId)
  {
    long curTime = System.currentTimeMillis();
    long delay = 0L;
    if (itemId == 5592)
      delay = curTime - _cpReuseTimeB;
    else {
      delay = curTime - _cpReuseTimeS;
    }
    return delay;
  }

  public void setCpReuseTime(int itemId) {
    long curTime = System.currentTimeMillis();
    if (itemId == 5592)
      _cpReuseTimeB = curTime;
    else
      _cpReuseTimeS = curTime;
  }

  public void onForcedAttack(L2PcInstance attacker)
  {
    if ((attacker.isInOlympiadMode()) && (
      (!isInOlympiadMode()) || (attacker.getOlympiadGameId() != getOlympiadGameId()) || (!isOlympiadCompStart()))) {
      attacker.sendActionFailed();
      return;
    }

    super.onForcedAttack(attacker);
  }

  public void sendActionFailed()
  {
    sendUserPacket(Static.ActionFailed);
  }

  public int getTradePartner()
  {
    return _tradePartner;
  }

  public long getTradeStart() {
    return _tradeStart;
  }

  public boolean tradeLeft()
  {
    return System.currentTimeMillis() - _tradeStart > 10000L;
  }

  public boolean setTradePartner(int charId, long start)
  {
    if (((_tradePartner > 0) && (charId > 0)) || ((_tradeStart > 0L) && (start > 0L))) {
      _tradePartner = -1;
      _tradeStart = 0L;
      cancelActiveTrade();

      return false;
    }

    _tradePartner = charId;
    _tradeStart = start;
    return true;
  }

  public void setDestination(int x, int y, int z)
  {
    _destX = x;
    _destY = y;
    _destZ = z;
  }

  public int getXdest() {
    return _destX;
  }

  public int getYdest() {
    return _destY;
  }

  public int getZdest() {
    return _destZ;
  }

  public void setVote1Item(int objId)
  {
    _vote1Item = objId;
  }

  public int getVote1Item() {
    return _vote1Item;
  }

  public void setVote2Item(int objId) {
    _vote2Item = objId;
  }

  public int getVote2Item() {
    return _vote2Item;
  }

  public void setVoteEnchant(int enchantLevel)
  {
    _voteEnch = enchantLevel;
  }

  public int getVoteEnchant() {
    return _voteEnch;
  }

  public void setVoteAugment(L2Skill augment)
  {
    voteAugm = augment;
  }

  public L2Skill getVoteAugment() {
    return voteAugm;
  }

  public void setStockItem(int id, int itemId, int enchant, int augment, int auhLevel)
  {
    _sellIdStock = id;
    _itemIdStock = itemId;
    _enchantStock = enchant;
    _augmentStock = augment;
    _auhLeveStock = auhLevel;
  }

  public void setStockInventoryItem(int objectId, int enchantLevel) {
    _objectIdStockI = objectId;
    _enchantStockI = enchantLevel;
  }

  public int getSellIdStock() {
    return _sellIdStock;
  }

  public int getItemIdStock() {
    return _itemIdStock;
  }

  public int getEnchantStock() {
    return _enchantStock;
  }

  public int getAugmentStock() {
    return _augmentStock;
  }

  public int getAuhLeveStock() {
    return _auhLeveStock;
  }

  public int getObjectIdStockI() {
    return _objectIdStockI;
  }

  public int getEnchantStockI() {
    return _enchantStockI;
  }

  public void setStockSelf(int self)
  {
    _stockSelf = self;
  }

  public int getStockSelf() {
    return _stockSelf;
  }

  public void setStockLastAction(long last)
  {
    _stockTime = last;
  }

  public long getStockLastAction() {
    return _stockTime;
  }

  public void setAugSaleItem(int id)
  {
    _augSaleItem = id;
  }

  public int getAugSaleItem() {
    return _augSaleItem;
  }

  public void setAugSale(int id, int lvl) {
    _augSaleId = id;
    _augSaleLvl = lvl;
  }

  public int getAugSaleId() {
    return _augSaleId;
  }

  public int getAugSaleLvl() {
    return _augSaleLvl;
  }

  public void setInGame(boolean f)
  {
    _inGame = f;
    if (f) {
      _client.startSession();

      if (Config.VS_HWID)
        LoginServerThread.getInstance().setLastHwid(getAccountName(), getHWID());
    }
  }

  public boolean isInGame()
  {
    return _inGame;
  }

  public void setNoExp(boolean f)
  {
    _noExp = f;
  }

  public boolean isNoExp() {
    return _noExp;
  }

  public void setAlone(boolean f) {
    _lAlone = f;
  }

  public boolean isAlone() {
    return _lAlone;
  }

  public void setAutoLoot(boolean f) {
    _autoLoot = f;
  }

  public boolean getAutoLoot() {
    return _autoLoot;
  }

  public void setChatIgnore(int f) {
    _chatIgnore = f;
  }

  public int getChatIgnore() {
    return _chatIgnore;
  }

  public void setTradersIgnore(boolean f) {
    _tradersIgnore = f;
  }

  public boolean getTradersIgnore() {
    return _tradersIgnore;
  }

  public void setGeoPathfind(boolean f) {
    _geoPathFind = (Config.GEODATA == 2 ? f : false);
  }

  public boolean geoPathfind()
  {
    if (isPartner()) {
      return true;
    }

    return _geoPathFind;
  }

  public void setShowSkillChances(boolean f) {
    _skillChances = f;
  }

  public boolean getShowSkillChances()
  {
    return _skillChances;
  }

  public void setMPVPLast()
  {
    _mpvplast = System.currentTimeMillis();
  }

  public long getMPVPLast() {
    return _mpvplast;
  }

  public boolean inEvent()
  {
    return EventManager.getInstance().isReg(this);
  }

  public void setEventWait(boolean f)
  {
    _eventWait = f;
  }

  public boolean isEventWait() {
    return _eventWait;
  }

  public void enterMovieMode() {
    setTarget(null);

    sendUserPacket(new CameraMode(1));
  }

  public void enterMovieMode(L2Object target) {
    setTarget(target);

    sendUserPacket(new CameraMode(1));
  }

  public void leaveMovieMode()
  {
    sendUserPacket(new CameraMode(0));
  }

  public void specialCamera(L2Object target, int dist, int yaw, int pitch, int time, int duration) {
    if (Config.DISABLE_BOSS_INTRO) {
      return;
    }

    sendUserPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
  }

  public String voteRef()
  {
    if (!_voteRef.equalsIgnoreCase("no")) {
      return _voteRef;
    }

    String voter = "";

    Connect con = null;
    PreparedStatement st = null;
    ResultSet result = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT * FROM `z_vote_names` WHERE `from`=? LIMIT 1");
      st.setString(1, getName());
      result = st.executeQuery();
      if (result.next())
        voter = result.getString("to");
    }
    catch (SQLException e) {
      _log.warning(new StringBuilder().append("voteRef() error: ").append(e).toString());
    } finally {
      Close.CSR(con, st, result);
    }

    _voteRef = voter;
    return voter;
  }

  public void setVoteRf(String name) {
    if (_voteRef.equalsIgnoreCase(name)) {
      return;
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("REPLACE INTO `z_vote_names` (`from`,`to`) VALUES (?,?)");
      st.setString(1, getName());
      st.setString(2, name);
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("setVoteRf() error: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
    _voteRef = name;
  }

  public void delVoteRef() {
    if (_voteRef.equalsIgnoreCase("no")) {
      return;
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM `z_vote_names` WHERE `from`=?");
      st.setString(1, getName());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("delVoteRef() error: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
    _voteRef = "no";
  }

  public void setAugFlag(boolean f)
  {
    _augFlag = f;
  }

  public boolean getAugFlag() {
    return _augFlag;
  }

  public void setAquFlag(int id)
  {
    _aquFlag = id;
  }

  public int getAquFlag() {
    return _aquFlag;
  }

  public boolean canTrade()
  {
    if ((isParalyzed()) || (!ZoneManager.getInstance().inTradeZone(this))) {
      sendUserPacket(Static.NOT_TRADE_ZONE);
      return false;
    }

    FastList players = getKnownList().getKnownPlayersInRadius(20);
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if ((pc == null) || 
        (pc.getPrivateStoreType() == 0)) continue;
      sendUserPacket(Static.CANT_TRADE_NEAR);
      return false;
    }

    pc = null;
    return true;
  }

  public void setSpyPacket(boolean f)
  {
    _spyPacket = f;
  }

  public boolean isSpyPckt() {
    return _spyPacket;
  }

  public void setFakeLoc(int x, int y, int z)
  {
    _fakeLoc = new Location(x, y, z);
  }

  public Location getFakeLoc()
  {
    return _fakeLoc;
  }

  private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app, boolean aggro) {
    super(objectId, template);
    _accountName = accountName;
    _appearance = app;

    initAggro(aggro);
  }

  private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app, boolean aggro, boolean summon) {
    super(objectId, template);
    _accountName = accountName;
    _appearance = app;

    _ai = new L2PartnerAI(new AIAccessor());
  }

  private void initAggro(boolean aggro)
  {
    if (aggro) {
      switch (getClassId().getId()) {
      case 92:
      case 102:
      case 109:
        _ai = new L2PlayerFakeArcherAI(new AIAccessor());
        stopSkillEffects(99);
        SkillTable.getInstance().getInfo(99, 2).getEffects(this, this);
        break;
      default:
        _ai = new L2PlayerFakeAI(new AIAccessor());
      }

      if (isMageClass())
        doFullBuff(2);
      else
        doFullBuff(1);
    }
    else {
      _ai = new L2PlayerAI(new AIAccessor());
    }
  }

  public static L2PcInstance restoreFake(int objectId) {
    return restoreFake(objectId, 0, false);
  }

  public static L2PcInstance restoreFake(int objectId, int classId, boolean summon) {
    L2PcInstance player = null;

    int activeClassId = Rnd.get(89, 112);
    if (classId > 0) {
      activeClassId = classId;
    }

    boolean female = Rnd.get(0, 1) != 0;

    L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
    byte abc = (byte)Rnd.get(3);
    PcAppearance app = new PcAppearance(abc, abc, abc, female);

    if (summon)
      player = new L2PcInstance(objectId, template, "fake_qwerty", app, false, true);
    else {
      player = new L2PcInstance(objectId, template, "fake_qwerty", app, true);
    }

    player.setFantome(true);

    player._lastAccess = 0L;

    player.getStat().setSp(2147483647);

    long pXp = player.getExp();
    long tXp = net.sf.l2j.gameserver.model.base.Experience.LEVEL[80];
    player.addExpAndSp(tXp - pXp, 0);

    player.setWantsPeace(0);

    player.setHeading(Rnd.get(1, 65535));

    player.setKarma(0);
    player.setPvpKills(0);
    player.setPkKills(0);

    player.setOnlineTime(0L);

    player.setNewbie(false);

    boolean noble = Rnd.get(0, 1) != 0;
    player.setNoble(noble);
    player.setHero(false);

    player.setClanJoinExpiryTime(0L);
    player.setClanJoinExpiryTime(0L);

    player.setClanCreateExpiryTime(0L);
    player.setClanCreateExpiryTime(0L);

    player.setPowerGrade(5);
    player.setPledgeType(0);
    player.setLastRecomUpdate(0L);

    player.setDeleteTimer(0L);

    player.setAccessLevel(0);
    player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
    player.setUptime(System.currentTimeMillis());

    player.setCurrentHp(player.getMaxHp());
    player.setCurrentCp(player.getMaxCp());
    player.setCurrentMp(player.getMaxMp());

    player.checkRecom(5, 1);

    player._classIndex = 0;
    try {
      player.setBaseClass(2);
    } catch (Exception e) {
      player.setBaseClass(activeClassId);
    }

    if ((restoreSubClassData(player)) && 
      (activeClassId != player.getBaseClass())) {
      for (SubClass subClass : player.getSubClasses().values()) {
        if (subClass.getClassId() == activeClassId) {
          player._classIndex = subClass.getClassIndex();
        }
      }

    }

    player._activeClass = activeClassId;

    player.setApprentice(0);
    player.setSponsor(0);
    player.setLvlJoinedAcademy(0);
    player.setIsIn7sDungeon(false);
    player.setInJail(false);
    player.setJailTimer(0L);

    player.setAllianceWithVarkaKetra(0);

    player.setDeathPenaltyBuffLevel(0);

    player.updatePcPoints(0, 0, false);
    try
    {
      player.stopAllTimers();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    return player;
  }

  public void setPartnerClass(int id)
  {
    _partnerClass = id;
  }

  public int getPartnerClass()
  {
    return _partnerClass;
  }

  public void setPartner(L2PcInstance partner)
  {
    _partner = partner;
    if (_partner == null) {
      return;
    }
    _partner.setPartner();
  }

  public L2PcInstance getPartner()
  {
    return _partner;
  }

  public void setPartner()
  {
    _isPartner = true;
  }

  public boolean isPartner()
  {
    return _isPartner;
  }

  public void setOwner(L2PcInstance partner)
  {
    _owner = partner;
  }

  public L2PcInstance getOwner()
  {
    return _owner;
  }

  public void setFollowStatus(boolean state)
  {
    _follow = state;
    if (_follow)
      getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
    else
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
  }

  public boolean getFollowStatus()
  {
    return _follow;
  }

  public void followOwner() {
    setFollowStatus(true);
  }

  public void updateAndBroadcastPartnerStatus(int val)
  {
    _owner.sendPacket(new PetStatusUpdate(this));

    if (isVisible())
      broadcastUserInfo();
  }

  public void setFantome(boolean f)
  {
    _fantome = f;
  }

  public boolean isFantome()
  {
    return _fantome;
  }

  public void setSpy(boolean f)
  {
    _spy = f;
  }

  public boolean isSpy() {
    return _spy;
  }

  public void setBaiTele(boolean f)
  {
    _bt = f;
  }

  public boolean isBaiTele() {
    return _bt;
  }

  public void setFCItem(int objectId, int enchantLevel, int fcAugm, int iCount)
  {
    _fcObj = objectId;
    _fcEnch = enchantLevel;
    _fcAugm = fcAugm;
    _fcCount = iCount;
  }

  public int getFcObj() {
    return _fcObj;
  }

  public int getFcEnch() {
    return _fcEnch;
  }

  public int getFcCount() {
    return _fcCount;
  }

  public int getFcAugm() {
    return _fcAugm;
  }

  public void setFightClub(boolean f)
  {
    _fcBattle = f;
  }

  public boolean inFightClub() {
    return _fcBattle;
  }

  public void setFClub(boolean f)
  {
    _fcWait = f;
  }

  public boolean inFClub() {
    return _fcWait;
  }

  public void setInEnch(boolean f)
  {
    _inEnch = f;
  }

  public boolean inEnch() {
    return _inEnch;
  }

  public void sendItems(boolean f)
  {
    sendPacket(new ItemList(this, f));
  }

  public void sendEtcStatusUpdate()
  {
    sendUserPacket(new EtcStatusUpdate(this));
  }

  public void checkUserKey(String key)
  {
    if (key.length() > 1) {
      _userKey.key = key.trim();
      _userKey.on = 1;
    }
  }

  public void setUserKey(String key)
  {
    if (_userKey.key.equalsIgnoreCase(key)) {
      return;
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE `character_settings` SET `charkey`=? WHERE `char_obj_id`=?");
      st.setString(1, key);
      st.setInt(2, getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("setUserKey() error: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
    _userKey.key = key;
    unsetUserKey();

    sendPacket(Static.SET_KEY_OK.replaceAndGet("%KEY%", key));
  }

  public UserKey getUserKey()
  {
    return _userKey;
  }

  public void unsetUserKey() {
    _userKey.on = 0;
  }

  public void delUserKey()
  {
    if (_userKey.key.equalsIgnoreCase("")) {
      return;
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE `character_settings` SET `charkey`=? WHERE `char_obj_id`=?");
      st.setString(1, "");
      st.setInt(2, getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("delUserKey() error: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
    _userKey.key = "";
  }

  public void setUserKeyOnLevel() {
    if (_userKey.key.length() > 1) {
      return;
    }

    _userKey.on = 1;
    sendUserPacket(Static.SET_CHAR_KEY);
  }

  public boolean isParalyzed()
  {
    return (_userKey.on == 1) || (super.isParalyzed());
  }

  public void setNoSummon(boolean f)
  {
    _antiSummon = f;
  }

  public boolean noSummon() {
    return _antiSummon;
  }

  public void equipWeapon(L2ItemInstance item)
  {
    wpnEquip.lock();
    try {
      if (_euipWeapon != null) { sendActionFailed();
        return; }
    } finally {
      wpnEquip.unlock();
    }

    if ((isAttackingNow()) || (isCastingNow())) {
      _euipWeapon = ThreadPoolManager.getInstance().scheduleAi(new WeaponEquip(item), 700L, true);
      sendActionFailed();
      return;
    }
    _euipWeapon = ThreadPoolManager.getInstance().scheduleAi(new WeaponEquip(item), 200L, true);
  }

  public boolean isEnemyForMob(L2Attackable mob)
  {
    if (isInvisible()) {
      return false;
    }

    if ((isInvul()) && (isGM())) {
      return false;
    }

    if (_isTeleporting) {
      return false;
    }

    if (!canSeeTarget(mob)) {
      return false;
    }

    if ((isSilentMoving()) && (!mob.isRaid())) {
      return false;
    }

    if (isRecentFakeDeath()) {
      return false;
    }

    if (("varka".equals(mob.getFactionId())) && (isAlliedWithVarka())) {
      return false;
    }

    if (("ketra".equals(mob.getFactionId())) && (isAlliedWithKetra())) {
      return false;
    }

    if (mob.fromMonastry()) {
      if (getActiveWeaponItem() == null) {
        return false;
      }
      mob.sayString(new StringBuilder().append("Brother ").append(getName()).append(", move your weapon away!!").toString());
      return true;
    }

    if ((isInParty()) && (getParty().isInDimensionalRift())) {
      byte riftType = getParty().getDimensionalRift().getType();
      byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();

      if ((mob.isL2RiftInvader()) && (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(mob.getX(), mob.getY(), mob.getZ()))) {
        return false;
      }
    }

    if ((mob.isL2Guard()) && (getKarma() > 0)) {
      return true;
    }

    if (((mob.isL2Guard()) || (mob.isL2FriendlyMob())) && (getKarma() == 0)) {
      return false;
    }

    return mob.isAggressive();
  }

  public void setOsTeam(int team)
  {
    _osTeam = team;
    broadcastUserInfo();
  }

  public int getOsTeam()
  {
    return _osTeam;
  }

  public void setHaveCustomSkills()
  {
    _havpwcs = true;
  }

  public boolean haveCustomSkills() {
    return _havpwcs;
  }

  public void setPwSkill(int id) {
    _pwskill = id;
  }

  public int getPwSkill() {
    return _pwskill;
  }

  public void checkDonateSkills() {
    FastTable donSkills = CustomServerData.getInstance().getDonateSkills(getObjectId());
    if ((donSkills == null) || (donSkills.isEmpty())) {
      return;
    }

    int i = 0; for (int n = donSkills.size(); i < n; i++) {
      CustomServerData.DonateSkill di = (CustomServerData.DonateSkill)donSkills.get(i);
      if (di == null)
      {
        continue;
      }
      if ((di.cls > 0) && (di.cls != getClassId().getId()))
      {
        continue;
      }
      if ((di.expire > 0L) && (di.expire < System.currentTimeMillis()))
      {
        continue;
      }
      addSkill(SkillTable.getInstance().getInfo(di.id, di.lvl), false);
      if ((di.id == Config.SOB_ID) || ((di.id >= 7077) && (di.id <= 7080))) {
        if (di.lvl == 2) {
          L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(Config.SOB_NPC);
          L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, this, null);

          summon.setName(" ");
          summon.setTitle(" ");
          summon.setExpPenalty(0.0F);
          summon.getStat().setExp(net.sf.l2j.gameserver.model.base.Experience.LEVEL[(80 % net.sf.l2j.gameserver.model.base.Experience.LEVEL.length)]);
          summon.setCurrentHp(summon.getMaxHp());
          summon.setCurrentMp(summon.getMaxMp());
          summon.setHeading(getHeading());
          summon.setRunning();
          setFairy(summon);

          L2World.getInstance().storeObject(summon);
          summon.spawnMe(getX() + 50, getY() + 100, getZ());
          summon.setFollowStatus(true);
          summon.setShowSummonAnimation(false);
          summon.setIsInvul(true);
          broadcastUserInfo();
        }
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(di.expire));
        sendCritMessage(new StringBuilder().append("Skill of Balance: \u0434\u043E ").append(date).toString());
      }
    }
  }

  public void addDonateSkill(int cls, int id, int lvl, long expire) {
    CustomServerData.getInstance().addDonateSkill(getObjectId(), cls, id, lvl, expire);
  }

  public void setPremium(boolean f)
  {
    _premium = f;
  }

  public void setPremiumExpire(long expire) {
    _premiumExpire = expire;
  }

  public boolean isPremium() {
    return _premium;
  }

  public void setHero(int days)
  {
    setHero(true);
    broadcastPacket(new SocialAction(getObjectId(), 16));
    broadcastUserInfo();

    if (days == 0) {
      _heroExpire = 3L;
      return;
    }
    _heroExpire = (days == -1 ? 1L : System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days));

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE `characters` SET `hero`=? WHERE `obj_Id`=?");
      st.setLong(1, _heroExpire);
      st.setInt(2, getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("setHero(int days) error: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
  }

  public void setHeroExpire(long expire) {
    _heroExpire = expire;
  }

  public void sendTempMessages() {
    revalidateZone(true);
    if (_heroExpire == 1L) {
      sendCritMessage("\u0421\u0442\u0430\u0442\u0443\u0441 \u0413\u0435\u0440\u043E\u044F: \u0431\u0435\u0441\u043A\u043E\u043D\u0435\u0447\u043D\u044B\u0439.");
    } else if (_heroExpire == -1L) {
      sendCritMessage("\u0421\u0442\u0430\u0442\u0443\u0441 \u0413\u0435\u0440\u043E\u044F: \u0438\u0441\u0442\u0435\u043A.");
    } else if (_heroExpire > 1L) {
      String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(_heroExpire));
      sendCritMessage(new StringBuilder().append("\u0421\u0442\u0430\u0442\u0443\u0441 \u0413\u0435\u0440\u043E\u044F: \u0434\u043E ").append(date).toString());
    }

    if (_premiumExpire == -1L) {
      sendCritMessage("\u0421\u0442\u0430\u0442\u0443\u0441 \u041F\u0440\u0435\u043C\u0438\u0443\u043C: \u0438\u0441\u0442\u0435\u043A.");
    } else if (_premiumExpire > 1L) {
      String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(_premiumExpire));
      sendCritMessage(new StringBuilder().append("\u0421\u0442\u0430\u0442\u0443\u0441 \u041F\u0440\u0435\u043C\u0438\u0443\u043C: \u0434\u043E ").append(date).toString());
    }

    if (_userKey.on == 1)
      sendUserPacket(Static.UNBLOCK_CHAR_KEY);
    else if ((Config.VS_CKEY_CHARLEVEL) && (getLevel() >= 40))
      setUserKeyOnLevel();
    else if ((Config.VS_EMAIL) && (!hasEmail()))
      sendUserPacket(Static.CHANGE_EMAIL);
    else if (Config.SERVER_NEWS)
      sendUserPacket(Static.SERVER_WELCOME);
  }

  public void setShadeItems(boolean f)
  {
    _shdItems = f;
  }

  public boolean getShadeItems() {
    return !_shdItems;
  }

  public void setTvtPassive(boolean f)
  {
    _tvtPassive = f;
  }

  public boolean isTvtPassive() {
    return _tvtPassive;
  }

  public int getBriefItem()
  {
    return _bbsMailItem;
  }

  public void setBriefItem(int obj) {
    _bbsMailItem = obj;
  }

  public void setBriefSender(String sender) {
    _bbsMailSender = sender;
  }

  public String getBriefSender() {
    return _bbsMailSender;
  }

  public void setMailTheme(String sender) {
    _bbsMailTheme = sender;
  }

  public String getMailTheme() {
    return _bbsMailTheme;
  }

  public void setSex(boolean f)
  {
    byte abc = (byte)Rnd.get(3);
    _appearance = new PcAppearance(abc, abc, abc, f);
  }

  public long getLastPvpPk()
  {
    return _lastPvPPk;
  }

  public void setLastPvpPk() {
    _lastPvPPk = System.currentTimeMillis();
  }

  public void setLastPvpPkBan() {
    _lastPvPPk = (System.currentTimeMillis() + Config.PVPPK_STEPBAN);
  }

  private boolean canChangeBonusColor(int color, boolean name)
  {
    if (color == 0) {
      return false;
    }

    if (name) {
      if (Config.PVPBONUS_COLORS_NAME.contains(Integer.valueOf(getAppearance().getNameColor()))) {
        getAppearance().setNameColor(color);
        return true;
      }
    }
    else if (Config.PVPBONUS_COLORS_TITLE.contains(Integer.valueOf(getAppearance().getTitleColor()))) {
      getAppearance().setTitleColor(color);
      return true;
    }

    return false;
  }

  private void managePvpPkBonus(L2PcInstance killer, boolean checkPvpZone)
  {
    if (killer.getKarma() == 0) {
      Config.PvpColor color_bonus = (Config.PvpColor)Config.PVPBONUS_COLORS.get(Integer.valueOf(killer.getPvpKills()));
      if (color_bonus != null)
      {
        boolean update = killer.canChangeBonusColor(color_bonus.nick, true);
        if (!update) {
          update = killer.canChangeBonusColor(color_bonus.title, false);
        }

        if (update) {
          killer.broadcastUserInfo();
          killer.store();
        }
      }
    }

    if (getChannel() > 1) {
      return;
    }

    if ((checkPvpZone) && ((isInsidePvpZone()) || (killer.isInsidePvpZone()))) {
      return;
    }

    if ((Config.PVPPK_REWARD_ZONE) && (!isInsidePpvFarmZone())) {
      return;
    }

    if (System.currentTimeMillis() - killer.getLastPvpPk() < Config.PVPPK_INTERVAL) {
      return;
    }

    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(getName()))) {
      return;
    }

    if ((Config.PVPPK_IPPENALTY) && (getIP().equals(killer.getIP()))) {
      return;
    }

    killer.setLastPvpPk();

    if (Config.PVPPK_STEP > 0) {
      Integer last = (Integer)_pvppk_penalties.get(Integer.valueOf(killer.getObjectId()));
      if (last == null) {
        _pvppk_penalties.put(Integer.valueOf(killer.getObjectId()), Integer.valueOf(1));
      } else {
        if (last.intValue() > Config.PVPPK_STEP) {
          killer.setLastPvpPkBan();
          _pvppk_penalties.clear();
          return;
        }
        _pvppk_penalties.put(Integer.valueOf(killer.getObjectId()), Integer.valueOf(last.intValue() + 1));
      }
    }

    Config.PvpColor expsp = null;
    if (killer.getKarma() > 0) {
      if (killer.getLevel() - getLevel() > Config.PVPPK_PENALTY.title) {
        return;
      }

      expsp = (Config.PvpColor)Config.PVPPK_EXP_SP.get(1);

      Config.EventReward reward = null;
      FastList.Node k = Config.PVPPK_PKITEMS.head(); for (FastList.Node endk = Config.PVPPK_PKITEMS.tail(); (k = k.getNext()) != endk; ) {
        reward = (Config.EventReward)k.getValue();
        if ((reward == null) || 
          (Rnd.get(100) >= reward.chance)) continue;
        killer.addItem("pk_bonus", reward.id, reward.count, killer, true);
      }

      reward = null;
    }
    else
    {
      if (killer.getLevel() - getLevel() > Config.PVPPK_PENALTY.nick) {
        return;
      }

      expsp = (Config.PvpColor)Config.PVPPK_EXP_SP.get(0);

      Config.EventReward reward = null;
      FastList.Node k = Config.PVPPK_PVPITEMS.head(); for (FastList.Node endk = Config.PVPPK_PVPITEMS.tail(); (k = k.getNext()) != endk; ) {
        reward = (Config.EventReward)k.getValue();
        if ((reward == null) || 
          (Rnd.get(100) >= reward.chance)) continue;
        killer.addItem("pvp_bonus", reward.id, reward.count, killer, true);
      }

      reward = null;

      Config.PvpColor item_bonus = (Config.PvpColor)Config.PVPBONUS_ITEMS.get(Integer.valueOf(killer.getPvpKills()));
      if (item_bonus != null) {
        killer.addItem("pk_bonus", item_bonus.nick, item_bonus.title, killer, true);
      }
    }

    if ((expsp != null) && ((expsp.nick > 0) || (expsp.title > 0)))
      killer.addExpAndSp(expsp.nick, expsp.title);
  }

  public boolean isInsidePpvFarmZone()
  {
    return _isInPpvFarm;
  }

  public void setInPvpFarmZone(boolean f)
  {
    _isInPpvFarm = f;
  }

  public String getIP()
  {
    if ((_client == null) || (_isDeleting) || (!_isConnected)) {
      return "None";
    }

    return getClient().getIpAddr();
  }

  public boolean canSummon()
  {
    if (underAttack()) {
      sendUserPacket(Static.YOU_CANNOT_SUMMON_IN_COMBAT);
      return false;
    }
    if ((Olympiad.isRegisteredInComp(this)) || (isInOlympiadMode())) {
      sendUserPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
      return false;
    }
    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(getName()))) {
      sendUserPacket(Static.YOU_CANNOT_SUMMON_IN_COMBAT);
      return false;
    }
    if (isInsidePvpZone()) {
      sendUserPacket(Static.YOU_CANNOT_SUMMON_IN_COMBAT);
      return false;
    }
    if (isInsideZone(1024)) {
      sendUserPacket(Static.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
      return false;
    }
    if (isInsideSilenceZone()) {
      sendUserPacket(Static.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
      return false;
    }
    if ((isEventWait()) || (isInEncounterEvent())) {
      sendUserPacket(Static.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
      return false;
    }
    if (getKnownList().existsDoorsInRadius(169)) {
      sendUserPacket(Static.YOU_MAY_NOT_SUMMON_NEAR_DOORS);
      return false;
    }

    FastList objects = L2World.getInstance().getVisibleObjects(this, 2000);
    FastList.Node n;
    if (!objects.isEmpty()) {
      n = objects.head(); for (FastList.Node end = objects.tail(); (n = n.getNext()) != end; ) {
        L2Object object = (L2Object)n.getValue();
        if ((object instanceof L2RaidBossInstance)) {
          sendUserPacket(Static.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
          return false;
        }
      }
    }
    return true;
  }

  public boolean canBeSummoned(L2PcInstance caster) {
    if (isAlikeDead())
    {
      caster.sendUserPacket(SystemMessage.id(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addString(getName()));
      sendUserPacket(Static.CANT_BE_SUMMONED_NOW);
      return false;
    }

    if (isInStoreMode()) {
      caster.sendUserPacket(SystemMessage.id(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addString(getName()));
      sendUserPacket(Static.CANT_BE_SUMMONED_NOW);
      return false;
    }

    if ((isRooted()) || (underAttack())) {
      caster.sendUserPacket(SystemMessage.id(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addString(getName()));
      sendUserPacket(Static.CANT_BE_SUMMONED_NOW);
      return false;
    }

    if ((Olympiad.isRegisteredInComp(this)) || (isInOlympiadMode())) {
      caster.sendUserPacket(Static.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
      sendUserPacket(Static.CANT_BE_SUMMONED);
      return false;
    }

    if (isFestivalParticipant()) {
      caster.sendUserPacket(Static.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
      sendUserPacket(Static.CANT_BE_SUMMONED);
      return false;
    }

    if (isInsidePvpZone()) {
      caster.sendUserPacket(Static.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
      sendUserPacket(Static.CANT_BE_SUMMONED);
      return false;
    }

    if (isInsideSilenceZone()) {
      caster.sendUserPacket(Static.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
      sendUserPacket(Static.CANT_BE_SUMMONED);
      return false;
    }

    if (isInsideZone(1024)) {
      caster.sendUserPacket(Static.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
      sendUserPacket(Static.CANT_BE_SUMMONED);
      return false;
    }

    if ((isEventWait()) || (isInEncounterEvent())) {
      caster.sendUserPacket(Static.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
      sendUserPacket(Static.CANT_BE_SUMMONED);
      return false;
    }

    if (getChannel() != caster.getChannel()) {
      caster.sendUserPacket(Static.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
      sendUserPacket(Static.CANT_BE_SUMMONED);
      return false;
    }

    if (getItemCount(8615) == 0) {
      caster.sendMessage(new StringBuilder().append("\u041F\u0435\u0440\u0441\u043E\u043D\u0430\u0436 ").append(getName()).append(" \u043D\u0435 \u043C\u043E\u0436\u0435\u0442 \u0431\u044B\u0442\u044C \u043F\u0440\u0438\u0437\u0432\u0430\u043D \u0431\u0435\u0437 Summoning Crystal.").toString());
      sendUserPacket(Static.NO_SUMMON_CRY);
      return false;
    }
    return true;
  }

  public void updateEnchClicks()
  {
    _enchClicks += 1;
  }

  public int getEnchClicks() {
    return _enchClicks;
  }

  public void showAntiClickPWD()
  {
    _enchClicks = Rnd.get(99900, 99921);
    TextBuilder tb = new TextBuilder("<br>");
    for (int i = Rnd.get(30); i > 0; i--) {
      tb.append("<br>");
    }
    NpcHtmlMessage html = NpcHtmlMessage.id(0);

    html.setHtml(new StringBuilder().append("<html><body><font color=\"FF6600\">!\u041F\u0440\u0435\u0432\u044B\u0448\u0435\u043D \u043B\u0438\u043C\u0438\u0442 \u0437\u0430\u0442\u043E\u0447\u043A\u0438!</font><br>\u041D\u0430\u0436\u043C\u0438\u0442\u0435 \u043D\u0430 \u043A\u043D\u043E\u043F\u043A\u0443 \u0434\u043B\u044F \u043F\u0440\u043E\u0434\u043E\u043B\u0436\u0435\u043D\u0438\u044F \u0437\u0430\u0442\u043E\u0447\u043A\u0438!<br> <table width=\"").append(Rnd.get(40, 300)).append("\"><tr><td align=\"right\">").append(tb.toString()).append("<button value=\"\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C\" action=\"bypass -h ench_click ").append(_enchClicks).append("\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></body></html>").toString());

    tb.clear();
    tb = null;
    sendUserPacket(html);
  }

  public void showAntiClickOk() {
    if (_enchClicks == 99908)
      sendHtmlMessage("\u041F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u043E, \u043A\u043E\u0440\u043E\u0432\u044B!", "\u041F\u0435\u0439\u0442\u0435, \u0434\u0435\u0442\u0438, \u043C\u043E\u043B\u043E\u043A\u043E -<br1>\u0411\u0443\u0434\u0435\u0442\u0435 \u0437\u0434\u043E\u0440\u043E\u0432\u044B!");
    else {
      sendHtmlMessage("\u0421\u043F\u0430\u0441\u0438\u0431\u043E", "\u043C\u043E\u0436\u0435\u0442\u0435 \u0442\u043E\u0447\u0438\u0442\u044C \u0434\u0430\u043B\u044C\u0448\u0435.");
    }

    _enchClicks = 0;
  }

  public boolean checkEnchClicks(String answer) {
    return answer.equalsIgnoreCase(CustomServerData.getInstance().getRiddle(_enchClicks).answer);
  }

  public void teleToClosestTown()
  {
    teleToLocation(MapRegionTable.TeleportWhereType.Town);
  }

  public void setInEncounterEvent(boolean f)
  {
    _isInEncounterEvent = f;
  }

  public boolean isInEncounterEvent() {
    return _isInEncounterEvent;
  }

  public void setEventColNumber(int rnd)
  {
    _eventColNumber = rnd;
  }

  public int getEventColNumber() {
    return _eventColNumber;
  }

  public void setQuestLastReward()
  {
    quest_last_reward_time = System.currentTimeMillis();
  }

  public long getQuestLastReward() {
    return quest_last_reward_time;
  }

  public int getPcPoints()
  {
    return _pcPoints;
  }

  public void setPcPoints(int points) {
    _pcPoints = points;
  }

  public void updatePcPoints(int points, int type, boolean _double) {
    if (_double) {
      points *= 2;
    }

    switch (type) {
    case 1:
      sendUserPacket(new ExPCCafePointInfo(this, 0, false, false));
      sendMessage(Static.CONSUMED_S1_PCPOINTS.replace("%a%", String.valueOf(points)));
      break;
    case 2:
      if (_premium) {
        points = (int)(points * Config.PREMIUM_PCCAFE_MUL);
      }

      sendUserPacket(new ExPCCafePointInfo(this, points, true, _double));
      sendMessage(Static.EARNED_S1_PCPOINTS.replace("%a%", String.valueOf(points)));
    }

    _pcPoints += points;
  }

  public void setNextScroll(int id)
  {
    _nextScroll = id;
  }

  public int getNextScroll() {
    return _nextScroll;
  }

  public void useNextScroll() {
    if (_nextScroll == 0) {
      return;
    }

    L2ItemInstance item = _inventory.getItemByObjectId(_nextScroll);
    if (item != null) {
      IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
      if (handler != null) {
        handler.useItem(this, item);
      }
    }
    _nextScroll = 0;
  }

  public void setLFP(boolean f)
  {
    _lookingForParty = f;
  }

  public boolean isLFP() {
    return _lookingForParty;
  }

  public void setPartyRoom(PartyWaitingRoomManager.WaitingRoom room)
  {
    _partyRoom = room;
  }

  public PartyWaitingRoomManager.WaitingRoom getPartyRoom() {
    return _partyRoom;
  }

  public void setChannel(int channel)
  {
    _channel = channel;
  }

  public int getChannel()
  {
    return _channel;
  }

  public boolean isInvisible() {
    return _channel == 0;
  }

  public void setSfLoc(Location loc)
  {
    _sfLoc = loc;
  }

  public Location getSfLoc() {
    return _sfLoc;
  }

  public void sendSfRequest(L2PcInstance requester, ConfirmDlg dialog)
  {
    if ((requester == null) || (dialog.getLoc() == null)) {
      return;
    }

    _sfTime = (System.currentTimeMillis() + Config.SUMMON_ANSWER_TIME);
    _sfRequest = 1;

    setSfLoc(dialog.getLoc());
    sendUserPacket(dialog);
  }

  public void sfAnswer(int answer) {
    if ((answer == 0) || (_sfRequest == 0) || (_sfLoc == null)) {
      return;
    }

    if ((Config.SUMMON_ANSWER_TIME > 0) && (System.currentTimeMillis() > _sfTime)) {
      sendUserPacket(Static.ANSWER_TIMEOUT);
      return;
    }

    teleToLocation(_sfLoc.x, _sfLoc.y, _sfLoc.z, false);
    _sfLoc = null;
    _sfTime = 0L;
    _sfRequest = 0;
  }

  public void teleToLocation(int x, int y, int z)
  {
    if (_pvpArena) {
      sendUserPacket(Static.CANT_TELE_ON_EVENT);
      return;
    }

    super.teleToLocation(x, y, z, false);
  }

  public void teleToLocation(MapRegionTable.TeleportWhereType teleportWhere)
  {
    super.teleToLocation(teleportWhere);
  }

  public void teleToLocationEvent(int x, int y, int z) {
    teleToLocationEvent(x, y, z, false);
  }

  public void teleToLocationEvent(int x, int y, int z, boolean f) {
    if ((isMounted()) && 
      (setMountType(0))) {
      if (isFlying()) {
        removeSkill(SkillTable.getInstance().getInfo(4289, 1));
      }
      broadcastPacket(new Ride(getObjectId(), 0, 0));
      setMountObjectID(0);
    }

    super.teleToLocation(x, y, z, f);
  }

  public final String getTitle()
  {
    if ((getChannel() == 6) && (Config.ELH_HIDE_NAMES)) {
      return Config.ELH_ALT_NAME;
    }

    if ((!Config.STARTUP_TITLE.equalsIgnoreCase("off")) && (!isNoble()) && (getClan() == null)) {
      return Config.STARTUP_TITLE;
    }

    return super.getTitle();
  }

  public void setOfflineMode(boolean f)
  {
    if (f) {
      _offline = true;
      if (getParty() != null) {
        getParty().oustPartyMember(this);
      }

      if (getFairy() != null) {
        getFairy().unSummon(this);
      }

      if (getPet() != null) {
        getPet().unSummon(this);
      }

      if ((Olympiad.isRegisteredInComp(this)) || (isInOlympiadMode()) || (getOlympiadGameId() > -1)) {
        Olympiad.logoutPlayer(this);
      }

      if (Config.ALT_RESTORE_OFFLINE_TRADE) {
        setVar("offline", String.valueOf(System.currentTimeMillis() + Config.ALT_OFFLINE_TRADE_LIMIT), null);
      }

      store();
      sendUserPacket(Static.ServerClose);
      setConnected(false);

      broadcastUserInfo();

      if (Config.ALT_OFFLINE_TRADE_ONLINE) {
        Connect con = null;
        PreparedStatement st = null;
        try {
          con = L2DatabaseFactory.getInstance().getConnection();
          st = con.prepareStatement("UPDATE characters SET online=? WHERE obj_id=?");
          st.setInt(1, 2);
          st.setInt(2, getObjectId());
          st.execute();
        } catch (SQLException e) {
          _log.warning(new StringBuilder().append("could not set char offline status:").append(e).toString());
        } finally {
          Close.CS(con, st);
        }
      }
    } else {
      unsetVars();
      setPrivateStoreType(0);
      _offline = false;
    }
  }

  public boolean isInOfflineMode() {
    return _offline;
  }

  public void saveTradeList()
  {
    if (!Config.ALT_RESTORE_OFFLINE_TRADE) {
      return;
    }

    TextBuilder tb = new TextBuilder();
    Connect con = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      if ((_sellList == null) || (_sellList.getItemCount() == 0)) {
        unsetVar("selllist", con);
      } else {
        for (TradeList.TradeItem i : _sellList.getItems()) {
          tb.append(new StringBuilder().append(i.getObjectId()).append(";").append(i.getCount()).append(";").append(i.getPrice()).append(":").toString());
        }
        setVar("selllist", tb.toString(), con);
        if ((_sellList != null) && (_sellList.getTitle() != null)) {
          setVar("sellstorename", _sellList.getTitle(), con);
        }
      }
      tb.clear();

      if ((_buyList == null) || (_buyList.getItemCount() == 0)) {
        unsetVar("buylist", con);
      } else {
        for (TradeList.TradeItem i : _buyList.getItems()) {
          tb.append(new StringBuilder().append(i.getItem().getItemId()).append(";").append(i.getCount()).append(";").append(i.getPrice()).append(":").toString());
        }
        setVar("buylist", tb.toString(), con);
        if ((_buyList != null) && (_buyList.getTitle() != null)) {
          setVar("buystorename", _buyList.getTitle(), con);
        }
      }
      tb.clear();

      if ((_createList == null) || (_createList.getList().isEmpty())) {
        unsetVar("createlist", con);
      } else {
        for (L2ManufactureItem i : _createList.getList()) {
          tb.append(new StringBuilder().append(i.getRecipeId()).append(";").append(i.getCost()).append(":").toString());
        }
        setVar("createlist", tb.toString(), con);
        if (_createList.getStoreName() != null)
          setVar("manufacturename", _createList.getStoreName(), con);
      }
    }
    catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not saveTradeList(): ").append(e).toString());
    } finally {
      tb.clear();
      tb = null;
      Close.C(con);
    }
  }

  public void restoreTradeList() {
    if (!Config.ALT_RESTORE_OFFLINE_TRADE) {
      return;
    }

    if (getVar("selllist") != null) {
      _sellList = new TradeList(this);
      String[] items = getVar("selllist").split(":");
      for (String item : items) {
        if (item.equals("")) {
          continue;
        }
        String[] values = item.split(";");
        if (values.length < 3)
        {
          continue;
        }
        int oId = Integer.parseInt(values[0]);
        int count = Integer.parseInt(values[1]);
        int price = Integer.parseInt(values[2]);

        L2ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

        if ((count < 1) || (itemToSell == null))
        {
          continue;
        }
        if (count > itemToSell.getCount()) {
          count = itemToSell.getCount();
        }

        _sellList.addItem(oId, count, price);
      }

      if (getVar("sellstorename") != null) {
        _sellList.setTitle(getVar("sellstorename"));
      }
    }
    if (getVar("buylist") != null) {
      _buyList = new TradeList(this);
      String[] items = getVar("buylist").split(":");
      for (String item : items) {
        if (item.equals("")) {
          continue;
        }
        String[] values = item.split(";");
        if (values.length < 3)
        {
          continue;
        }
        _buyList.addItem(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
      }

      if (getVar("buystorename") != null) {
        _buyList.setTitle(getVar("buystorename"));
      }
    }
    if (getVar("createlist") != null) {
      _createList = new L2ManufactureList();
      String[] items = getVar("createlist").split(":");
      for (String item : items) {
        if (item.equals("")) {
          continue;
        }
        String[] values = item.split(";");
        if (values.length < 2) {
          continue;
        }
        _createList.add(new L2ManufactureItem(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
      }
      if (getVar("manufacturename") != null)
        _createList.setStoreName(getVar("manufacturename"));
    }
  }

  public void setVar(String name, String value, Connect excon)
  {
    if (!Config.ALT_RESTORE_OFFLINE_TRADE) {
      return;
    }

    _offtrade_items.put(name, value);

    Connect con = null;
    PreparedStatement st = null;
    try {
      if (excon == null)
        con = L2DatabaseFactory.getInstance().getConnection();
      else {
        con = excon;
      }

      st = con.prepareStatement("REPLACE INTO character_offline (obj_id, name, value) VALUES (?,?,?)");
      st.setInt(1, getObjectId());
      st.setString(2, name);
      st.setString(3, value);
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not setVar: ").append(e).toString());
    } finally {
      if (excon == null)
        Close.CS(con, st);
      else
        Close.S(st);
    }
  }

  public void unsetVar(String name, Connect excon)
  {
    if (name == null) {
      return;
    }

    if (_offtrade_items.remove(name) != null) {
      Connect con = null;
      PreparedStatement st = null;
      try {
        if (excon == null)
          con = L2DatabaseFactory.getInstance().getConnection();
        else {
          con = excon;
        }

        st = con.prepareStatement("DELETE FROM `character_offline` WHERE `obj_id`=? AND `name`=? LIMIT 1");
        st.setInt(1, getObjectId());
        st.setString(2, name);
        st.execute();
      } catch (SQLException e) {
        _log.warning(new StringBuilder().append("could not unsetVar: ").append(e).toString());
      } finally {
        if (excon == null)
          Close.CS(con, st);
        else
          Close.S(st);
      }
    }
  }

  public void unsetVars()
  {
    if (!Config.ALT_RESTORE_OFFLINE_TRADE) {
      return;
    }

    _offtrade_items.clear();

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("DELETE FROM `character_offline` WHERE `obj_id`=?");
      st.setInt(1, getObjectId());
      st.execute();
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not unsetVar: ").append(e).toString());
    } finally {
      Close.CS(con, st);
    }
  }

  public String getVar(String name) {
    return (String)_offtrade_items.get(name);
  }

  public Map<String, String> getVars() {
    return _offtrade_items;
  }

  private void loadOfflineTrade(Connect con) {
    if (!Config.ALT_RESTORE_OFFLINE_TRADE) {
      return;
    }

    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT name, value FROM character_offline WHERE obj_id = ?");
      st.setInt(1, getObjectId());
      rs = st.executeQuery();
      while (rs.next()) {
        String name = rs.getString("name");
        String value = Util.htmlSpecialChars(rs.getString("value"));

        _offtrade_items.put(name, value);
      }
    } catch (SQLException e) {
      _log.warning(new StringBuilder().append("could not restore offtrade data: ").append(e).toString());
    } finally {
      Close.SR(st, rs);
    }
  }

  public boolean canSeeTarget(L2Object trg)
  {
    if (trg.isPlayer()) {
      if ((!isGM()) && (getChannel() != trg.getChannel())) {
        return false;
      }

      if (((isInOlympiadMode()) || (trg.isInOlympiadMode())) && (trg.getOlympiadGameId() != getOlympiadGameId())) {
        return false;
      }
    }
    return super.canSeeTarget(trg);
  }

  public boolean canTarget(L2Object trg)
  {
    if (trg.isPlayer()) {
      if ((!isGM()) && (getChannel() != trg.getChannel())) {
        return false;
      }

      if (((isInOlympiadMode()) || (trg.isInOlympiadMode())) && (trg.getOlympiadGameId() != getOlympiadGameId())) {
        return false;
      }
    }
    return true;
  }

  public boolean isInEvent()
  {
    return EventManager.getInstance().onEvent(this);
  }

  public void saveHWID(boolean f)
  {
    LoginServerThread.getInstance().setHwid(getAccountName(), f ? getHWID() : "");
    _client.setMyHWID(f ? getHWID() : "none");
  }

  public String getMyHWID()
  {
    if (_client == null) {
      return "none";
    }

    return _client.getMyHWID();
  }

  public String getHWID()
  {
    if (_client == null) {
      return "none";
    }

    return _client.getHWID();
  }

  public boolean updatePassword(String pass)
  {
    String newpass = Util.getSHA1(pass);
    if (newpass.length() < 5) {
      return false;
    }

    LoginServerThread.getInstance().setNewPassword(getAccountName(), newpass);
    return true;
  }

  public boolean updateEmail(String email)
  {
    LoginServerThread.getInstance().setNewEmail(getAccountName(), email);
    return true;
  }

  public void setLastSay(String text)
  {
    _lastSayString = text;
  }

  public String getLastSay() {
    return _lastSayString;
  }

  public boolean identSay(String text) {
    if (text.startsWith(".")) {
      return false;
    }

    if (text.equalsIgnoreCase(_lastSayString)) {
      if (System.currentTimeMillis() < _lastSayTime) {
        _lastSayCount += 1;
      }

      _lastSayTime = (System.currentTimeMillis() + Config.PROTECT_SAY_INTERVAL);

      if (_lastSayCount >= Config.PROTECT_SAY_COUNT) {
        _lastSayTime = 0L;
        _lastSayCount = 0;
        setChatBanned(true, Config.PROTECT_SAY_BAN, "");
      }
      return true;
    }
    return false;
  }

  public void setActiveAug(int aug)
  {
    _activeAug = aug;
  }

  public int getActiveAug() {
    return _activeAug;
  }

  public void setTrans1Item(int objId)
  {
    _trans1item = objId;
  }

  public void setTrans2Item(int objId) {
    _trans2item = objId;
  }

  public void setTransAugment(int id) {
    _transAugId = id;
  }

  public int getTrans1Item() {
    return _trans1item;
  }

  public int getTrans2Item() {
    return _trans2item;
  }

  public int getTransAugment() {
    return _transAugId;
  }

  public void setBuffTarget(L2Character cha)
  {
    _buffTarget = cha;
  }

  public L2Character getBuffTarget() {
    if (_summon == null) {
      _buffTarget = this;
    }

    return _buffTarget;
  }

  public boolean isPlayer()
  {
    return true;
  }

  public double calcMDefMod(double value)
  {
    if (_inventory.getPaperdollItem(4) != null) {
      value -= 5.0D;
    }
    if (_inventory.getPaperdollItem(5) != null) {
      value -= 5.0D;
    }
    if (_inventory.getPaperdollItem(1) != null) {
      value -= 9.0D;
    }
    if (_inventory.getPaperdollItem(2) != null) {
      value -= 9.0D;
    }
    if (_inventory.getPaperdollItem(3) != null) {
      value -= 13.0D;
    }

    return value;
  }

  public double calcPDefMod(double value)
  {
    if (_inventory.getPaperdollItem(6) != null) {
      value -= 12.0D;
    }
    if (_inventory.getPaperdollItem(10) != null) {
      value -= (getClassId().isMage() ? 15 : 31);
    }
    if (_inventory.getPaperdollItem(11) != null) {
      value -= (getClassId().isMage() ? 8 : 18);
    }
    if (_inventory.getPaperdollItem(9) != null) {
      value -= 8.0D;
    }
    if (_inventory.getPaperdollItem(12) != null) {
      value -= 7.0D;
    }

    return value;
  }

  public double calcAtkCritical(double value, double dex)
  {
    if (getActiveWeaponInstance() == null) {
      return 40.0D;
    }

    value *= dex * 10.0D;
    return value;
  }

  public double calcMAtkCritical(double value, double wit)
  {
    if (getActiveWeaponInstance() == null) {
      return 8.0D;
    }

    return value * wit;
  }

  public double calcBlowDamageMul()
  {
    L2Armor armor = getActiveChestArmorItem();
    if (armor != null) {
      if (isWearingHeavyArmor()) {
        return Config.BLOW_DAMAGE_HEAVY;
      }
      if (isWearingLightArmor()) {
        return Config.BLOW_DAMAGE_LIGHT;
      }
      if (isWearingMagicArmor()) {
        return Config.BLOW_DAMAGE_ROBE;
      }
    }
    return 1.0D;
  }

  public boolean isOverlord()
  {
    return (getClassId().getId() == 51) || (getClassId().getId() == 115);
  }

  public void olympiadClear()
  {
    Olympiad.removeNobleIp(this);
  }

  public void getEffect(int id, int lvl)
  {
    stopSkillEffects(id);
    SkillTable.getInstance().getInfo(id, lvl).getEffects(this, this);
  }

  public boolean canExp()
  {
    if (getLevel() >= Config.MAX_EXP_LEVEL) {
      return false;
    }

    return super.canExp();
  }

  public L2PcInstance getPlayer()
  {
    return this;
  }

  public void setSoulShotsAnim(boolean f)
  {
    _showSoulshotsAnim = f;
  }

  public boolean showSoulShotsAnim()
  {
    return _showSoulshotsAnim;
  }

  public void updateLastTeleport(boolean f)
  {
    if (Config.TELEPORT_PROTECTION == 0L) {
      return;
    }

    if (f) {
      _lastTeleport = (System.currentTimeMillis() + Config.TELEPORT_PROTECTION);
      return;
    }

    if (isProtected()) {
      sendMessage("\u0422\u0435\u043F\u0435\u0440\u044C \u043D\u0430 \u0432\u0430\u0441 \u043C\u043E\u0433\u0443\u0442 \u043D\u0430\u043F\u0430\u0441\u0442\u044C.");
    }

    _lastTeleport = 0L;
  }

  public boolean isProtected() {
    if (Config.TELEPORT_PROTECTION == 0L) {
      return false;
    }

    return System.currentTimeMillis() < _lastTeleport;
  }

  public void incAccKickCount()
  {
    _accKickCount += 1;
  }

  public int getAccKickCount() {
    return _accKickCount;
  }

  public void changeName(String name)
  {
    setName(name);
    store();

    sendAdmResultMessage(new StringBuilder().append("\u0412\u0430\u0448 \u043D\u0438\u043A \u0438\u0437\u043C\u0435\u043D\u0435\u043D \u043D\u0430 ").append(name).toString());

    if (_clan != null) {
      _clan.updateClanMember(this, true);
    }

    if (_party != null) {
      _party.updateMembers();
    }

    if (!isGM()) {
      setChannel(1);
    }

    teleToLocation(getX(), getY(), getZ());
  }

  public boolean hasEmail()
  {
    return getClient().hasEmail();
  }

  public void rndWalk()
  {
    int posX = getX();
    int posY = getY();
    int posZ = getZ();
    switch (Rnd.get(1, 6)) {
    case 1:
      posX += 40;
      posY += 180;
      break;
    case 2:
      posX += 150;
      posY += 50;
      break;
    case 3:
      posX += 69;
      posY -= 100;
      break;
    case 4:
      posX += 10;
      posY -= 100;
      break;
    case 5:
      posX -= 150;
      posY -= 20;
      break;
    case 6:
      posX -= 100;
      posY += 60;
    }

    setRunning();
    getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, calcHeading(posX, posY)));
  }

  public boolean rndWalk(L2Character target, boolean fake)
  {
    rndWalk();
    if (_fakeProtect > 2)
    {
      return false;
    }
    _fakeProtect += 1;
    return true;
  }

  public void clearRndWalk()
  {
  }

  public int getPAtk(L2Character target)
  {
    if (isFantome()) {
      return getAI().getPAtk();
    }
    return super.getPAtk(target);
  }

  public double getMDef()
  {
    return getMDef(null, null);
  }

  public int getMDef(L2Character target, L2Skill skill)
  {
    if (isFantome()) {
      return getAI().getMDef();
    }
    return super.getMDef(target, skill);
  }

  public int getPAtkSpd()
  {
    if (isFantome()) {
      return getAI().getPAtkSpd();
    }
    return super.getPAtkSpd();
  }

  public int getPDef(L2Character target)
  {
    if (isFantome()) {
      return getAI().getPDef();
    }
    return super.getPDef(target);
  }

  public double getMAtk()
  {
    if (isFantome()) {
      return getAI().getMAtk();
    }
    return getStat().getMAtk(null, null);
  }

  public int getMAtkSpd()
  {
    if (isFantome()) {
      return getAI().getMAtkSpd();
    }
    return super.getMAtkSpd();
  }

  public int getMaxHp()
  {
    if (isFantome()) {
      return getAI().getMaxHp();
    }
    return super.getMaxHp();
  }

  public int getRunSpeed()
  {
    if ((_isPartner) && 
      (_owner != null)) {
      return _owner.getRunSpeed();
    }

    return super.getRunSpeed();
  }

  public void despawnMe() {
    if (getWorldRegion() != null) {
      getWorldRegion().removeFromZones(this);
      getWorldRegion().removeVisibleObject(this);
    }

    decayMe();
    getKnownList().removeAllKnownObjects();
    setOnlineStatus(false);
    L2World.getInstance().removeVisibleObject(this, getWorldRegion());
    L2World.getInstance().removePlayer(this);
    if (_owner != null)
      _owner.setPartner(null);
  }

  public boolean teleToLocation(Location loc)
  {
    if ((_pvpArena) || (loc.x == 0)) {
      return false;
    }

    teleToLocation(loc.x, loc.y, loc.z, false);
    return true;
  }

  public boolean ignoreBuffer() {
    if (underAttack()) {
      sendHtmlMessage("\u0411\u0430\u0444\u0444\u0435\u0440", "\u0412\u043E \u0432\u0440\u0435\u043C\u044F \u0431\u043E\u044F \u043D\u0435 \u0431\u0430\u0444\u0444\u0430\u044E.");
      sendActionFailed();
      return true;
    }
    return false;
  }

  public void setHippy(boolean hippy)
  {
    _isHippy = hippy;
  }

  public boolean isHippy()
  {
    return _isHippy;
  }

  public boolean isInEventChannel() {
    return (_channel > 3) && (_channel < 60);
  }

  public boolean isFullyRestored() {
    if (getCurrentCp() < getMaxCp()) {
      return false;
    }

    if (getCurrentHp() < getMaxHp()) {
      return false;
    }

    return getCurrentMp() >= getMaxMp();
  }

  public boolean canMountPet(L2Summon pet)
  {
    if ((pet == null) || (!pet.isMountable())) {
      return false;
    }

    if ((isMounted()) || (isBetrayed())) {
      return false;
    }

    if ((isInEncounterEvent()) || (getKnownList().existsDoorsInRadius(169))) {
      return false;
    }

    if ((isCastingNow()) || (isOutOfControl()) || (isParalyzed())) {
      return false;
    }
    if (isInsideDismountZone()) {
      return false;
    }

    SystemMessage sm = null;
    if (isDead())
      sm = Static.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD;
    else if (pet.isDead())
      sm = Static.DEAD_STRIDER_CANT_BE_RIDDEN;
    else if ((pet.isInCombat()) || (pet.isRooted()))
      sm = Static.STRIDER_IN_BATLLE_CANT_BE_RIDDEN;
    else if (isInCombat())
      sm = Static.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE;
    else if ((isSitting()) || (isMoving()))
      sm = Static.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING;
    else if (isFishing())
      sm = Static.CANNOT_DO_WHILE_FISHING_2;
    else if (isCursedWeaponEquiped()) {
      sm = Static.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE;
    }
    if (sm != null) {
      sendPacket(sm);
      sm = null;
      return false;
    }

    if (!disarmWeapons()) {
      return false;
    }

    return canSeeTarget(pet);
  }

  public void tryMountPet(L2Summon pet)
  {
    if (canMountPet(pet)) {
      broadcastUserInfo();
      Ride mount = new Ride(getObjectId(), 1, pet.getTemplate().npcId);
      broadcastPacket(mount);
      setMountType(mount.getMountType());
      setMountObjectID(pet.getControlItemId());
      pet.unSummon(this);
      startUnmountPet(Config.MOUNT_EXPIRE);
    } else if (isRentedPet()) {
      stopRentPet();
    } else if (isMounted()) {
      tryUnmounPet(pet);
    }
  }

  public void tryUnmounPet(L2Summon pet) {
    if (setMountType(0)) {
      if (isFlying()) {
        removeSkill(SkillTable.getInstance().getInfo(4289, 1));
      }
      broadcastPacket(new Ride(getObjectId(), 0, 0));
      setMountObjectID(0);
      broadcastUserInfo();
    }
  }

  private class WeaponEquip
    implements Runnable
  {
    final WeakReference<L2ItemInstance> wItem;

    public WeaponEquip(L2ItemInstance item)
    {
      wItem = new WeakReference(item);
    }

    public void run()
    {
      abortAttack();
      abortCast();

      if ((isMounted()) || (_isDeleting)) {
        sendActionFailed();
        L2PcInstance.access$202(L2PcInstance.this, null);
        return;
      }

      L2ItemInstance item = (L2ItemInstance)wItem.get();
      if (item == null) {
        L2PcInstance.access$202(L2PcInstance.this, null);
        return;
      }
      int bodyPart = item.getItem().getBodyPart();

      L2ItemInstance[] items = null;

      boolean isEquipped = item.isEquipped();
      L2ItemInstance weapon;
      if (isEquipped) {
        L2ItemInstance weapon = getActiveWeaponInstance();
        items = getInventory().unEquipItemInBodySlotAndRecord(bodyPart);
      } else {
        items = getInventory().equipItemAndRecord(item);
        weapon = getActiveWeaponInstance();
      }

      if (isEquipped != item.isEquipped())
      {
        if (isEquipped)
        {
          SystemMessage sm;
          SystemMessage sm;
          if (item.getEnchantLevel() > 0)
            sm = SystemMessage.id(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
          else {
            sm = SystemMessage.id(SystemMessageId.S1_DISARMED).addItemName(item.getItemId());
          }
          sendUserPacket(sm);

          if ((weapon != null) && (item == weapon)) {
            item.setChargedSoulshot(0);
            item.setChargedSpiritshot(0);

            if ((weapon.getItemType() == L2WeaponType.BOW) && (getFirstEffect(313) != null))
              stopSkillEffects(313);
          }
        }
        else
        {
          SystemMessage sm;
          if (item.getEnchantLevel() > 0)
            sm = SystemMessage.id(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
          else {
            sm = SystemMessage.id(SystemMessageId.S1_EQUIPPED).addItemName(item.getItemId());
          }

          sendUserPacket(sm);
          if ((weapon != null) && (item == weapon)) {
            rechargeAutoSoulShot(true, false, false);
            rechargeAutoSoulShot(false, true, false);

            if ((weapon.getItemType() != L2WeaponType.BOW) && (getFirstEffect(313) != null)) {
              stopSkillEffects(313);
            }
          }

          if (item.isShadowItem()) {
            sendCritMessage(item.getItemName() + ": \u043E\u0441\u0442\u0430\u043B\u043E\u0441\u044C " + item.getMana() + " \u043C\u0438\u043D\u0443\u0442.");
          }

          if (item.getExpire() > 0L) {
            String date = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(item.getExpire()));
            sendCritMessage(item.getItemName() + ": \u0438\u0441\u0442\u0435\u043A\u0430\u0435\u0442 " + date + ".");
          }
          item.decreaseMana(true);
        }
        SystemMessage sm = null;
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItems(Arrays.asList(items));
        sendUserPacket(iu);

        refreshExpertisePenalty();
        broadcastUserInfo();
      }

      L2PcInstance.access$202(L2PcInstance.this, null);
    }
  }

  public static class UserKey
  {
    public String key;
    public int on;
    public int ptr;
    public int error = 0;

    UserKey(String key, int on, int ptr) {
      this.key = key;
      this.on = on;
      this.ptr = ptr;
    }

    public boolean checkLeft() {
      error += 1;
      return error >= 3;
    }
  }

  public static class TimeStamp
  {
    private int skill;
    private long reuse;
    private long stamp;

    public TimeStamp(int _skill, long _reuse)
    {
      skill = _skill;
      reuse = _reuse;
      stamp = (System.currentTimeMillis() + reuse);
    }

    public TimeStamp(int _skill, long _reuse, long _systime) {
      skill = _skill;
      reuse = _reuse;
      stamp = _systime;
    }

    public long getStamp() {
      return stamp;
    }

    public int getSkill() {
      return skill;
    }

    public long getReuse() {
      return reuse;
    }

    public long getRemaining() {
      return Math.max(stamp - System.currentTimeMillis(), 0L);
    }

    public boolean hasNotPassed()
    {
      return System.currentTimeMillis() < stamp;
    }
  }

  private static class JailTask
    implements Runnable
  {
    L2PcInstance _player;
    protected long _startedAt;

    protected JailTask(L2PcInstance player)
    {
      _player = player;
      _startedAt = System.currentTimeMillis();
    }

    public void run()
    {
      _player.setInJail(false, 0);
    }
  }

  class WaterTask
    implements Runnable
  {
    WaterTask()
    {
    }

    public void run()
    {
      double reduceHp = getMaxHp() / 100.0D;

      if (reduceHp < 1.0D) {
        reduceHp = 1.0D;
      }

      reduceCurrentHp(reduceHp, L2PcInstance.this, false);

      sendUserPacket(SystemMessage.id(SystemMessageId.DROWN_DAMAGE_S1).addNumber((int)reduceHp));
    }
  }

  class LookingForFishTask
    implements Runnable
  {
    boolean _isNoob;
    boolean _isUpperGrade;
    int _fishType;
    int _fishGutsCheck;
    int _gutsCheckTime;
    long _endTaskTime;

    protected LookingForFishTask(int fishWaitTime, int fishGutsCheck, int fishType, boolean isNoob, boolean isUpperGrade)
    {
      _fishGutsCheck = fishGutsCheck;
      _endTaskTime = (System.currentTimeMillis() + fishWaitTime + 10000L);
      _fishType = fishType;
      _isNoob = isNoob;
      _isUpperGrade = isUpperGrade;
    }

    public void run()
    {
      if (System.currentTimeMillis() >= _endTaskTime) {
        EndFishing(false);
        return;
      }
      if (_fishType == -1) {
        return;
      }
      int check = Rnd.get(1000);
      if (_fishGutsCheck > check) {
        stopLookingForFishTask();
        StartFishCombat(_isNoob, _isUpperGrade);
      }
    }
  }

  class RentPetTask
    implements Runnable
  {
    RentPetTask()
    {
    }

    public void run()
    {
      stopRentPet();
    }
  }

  class WarnUserTakeBreak
    implements Runnable
  {
    WarnUserTakeBreak()
    {
    }

    public void run()
    {
      if (isOnline() == 1)
        sendUserPacket(Static.PLAYING_FOR_LONG_TIME);
      else
        stopWarnUserTakeBreak();
    }
  }

  class InventoryEnable
    implements Runnable
  {
    InventoryEnable()
    {
    }

    public void run()
    {
      _inventoryDisable = false;
    }
  }

  private static class SchedChatUnban
    implements Runnable
  {
    L2PcInstance _player;
    protected long _startedAt;

    protected SchedChatUnban(L2PcInstance player)
    {
      _player = player;
      _startedAt = System.currentTimeMillis();
    }

    public void run() {
      _player.setChatBanned(false);
    }
  }

  public static enum TransactionType
  {
    NONE, 
    PARTY, 
    CLAN, 
    ALLY, 
    TRADE, 
    FRIEND, 
    CHANNEL, 
    TRADED, 
    ROOM;
  }

  static class StandUpTask
    implements Runnable
  {
    L2PcInstance _player;

    StandUpTask(L2PcInstance player)
    {
      _player = player;
    }

    public void run() {
      _player.setIsSitting(false);
      _player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }
  }

  static class SitDownTask
    implements Runnable
  {
    L2PcInstance _player;

    SitDownTask(L2PcInstance player)
    {
      _player = player;
    }

    public void run() {
      _player.setIsParalyzed(false);
      _player.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
    }
  }

  public static class SkillDat
  {
    private L2Skill _skill;
    private boolean _ctrlPressed;
    private boolean _shiftPressed;

    protected SkillDat(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
    {
      _skill = skill;
      _ctrlPressed = ctrlPressed;
      _shiftPressed = shiftPressed;
    }

    public boolean isCtrlPressed() {
      return _ctrlPressed;
    }

    public boolean isShiftPressed() {
      return _shiftPressed;
    }

    public L2Skill getSkill() {
      return _skill;
    }

    public int getSkillId() {
      return getSkill() != null ? getSkill().getId() : -1;
    }
  }

  public class HerbTask
    implements Runnable
  {
    private String _process;
    private int _itemId;
    private int _count;
    private L2Object _reference;
    private boolean _sendMessage;

    HerbTask(String process, int itemId, int count, L2Object reference, boolean sendMessage)
    {
      _process = process;
      _itemId = itemId;
      _count = count;
      _reference = reference;
      _sendMessage = sendMessage;
    }

    public void run()
    {
      try {
        addItem(_process, _itemId, _count, _reference, _sendMessage);
      } catch (Throwable t) {
        L2PcInstance._log.log(Level.WARNING, "", t);
      }
    }
  }

  public class AIAccessor extends L2Character.AIAccessor
  {
    protected AIAccessor()
    {
      super();
    }

    public L2PcInstance getPlayer() {
      return L2PcInstance.this;
    }

    public void doPickupItem(L2Object object) {
      L2PcInstance.this.doPickupItem(object);
    }

    public void doInteract(L2Character target) {
      L2PcInstance.this.doInteract(target);
    }

    public void doAttack(L2Character target)
    {
      super.doAttack(target);

      getPlayer().setRecentFakeDeath(false);
      for (L2CubicInstance cubic : getCubics().values())
        if (cubic.getId() != 3)
          cubic.doAction(target);
    }

    public void doCast(L2Skill skill)
    {
      super.doCast(skill);

      getPlayer().setRecentFakeDeath(false);
      if (skill == null) {
        return;
      }
      if (!skill.isOffensive()) {
        return;
      }
      switch (L2PcInstance.1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[skill.getTargetType().ordinal()]) {
      case 1:
      case 2:
        return;
      }

      L2Object mainTarget = skill.getFirstOfTargetList(L2PcInstance.this);
      if ((mainTarget == null) || (!mainTarget.isL2Character())) {
        return;
      }
      for (L2CubicInstance cubic : getCubics().values())
        if (cubic.getId() != 3)
          cubic.doAction((L2Character)mainTarget);
    }

    public L2Summon getSummon()
    {
      return getSummon();
    }

    public boolean isAutoFollow() {
      return getFollowStatus();
    }
  }
}