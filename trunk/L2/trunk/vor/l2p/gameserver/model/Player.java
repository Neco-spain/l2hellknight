package l2p.gameserver.model;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.GameTimeController;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.PlayableAI.nextAction;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.dao.AccountBonusDAO;
import l2p.gameserver.dao.CharacterDAO;
import l2p.gameserver.dao.CharacterGroupReuseDAO;
import l2p.gameserver.dao.CharacterPostFriendDAO;
import l2p.gameserver.dao.EffectsDAO;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.data.xml.holder.HennaHolder;
import l2p.gameserver.data.xml.holder.InstantZoneHolder;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.database.mysql;
import l2p.gameserver.handler.bbs.CommunityBoardManager;
import l2p.gameserver.handler.bbs.ICommunityBoardHandler;
import l2p.gameserver.handler.items.IItemHandler;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.BypassManager;
import l2p.gameserver.instancemanager.BypassManager.BypassType;
import l2p.gameserver.instancemanager.BypassManager.DecodedBypass;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.instancemanager.DimensionalRiftManager;
import l2p.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import l2p.gameserver.instancemanager.MatchingRoomManager;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.instancemanager.games.HandysBlockCheckerManager;
import l2p.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2p.gameserver.listener.actor.player.OnAnswerListener;
import l2p.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2p.gameserver.listener.actor.player.impl.ScriptAnswerListener;
import l2p.gameserver.listener.actor.player.impl.SummonAnswerListener;
import l2p.gameserver.model.actor.instances.player.Bonus;
import l2p.gameserver.model.actor.instances.player.BookMarkList;
import l2p.gameserver.model.actor.instances.player.FriendList;
import l2p.gameserver.model.actor.instances.player.Macro;
import l2p.gameserver.model.actor.instances.player.MacroList;
import l2p.gameserver.model.actor.instances.player.NevitSystem;
import l2p.gameserver.model.actor.instances.player.RecomBonus;
import l2p.gameserver.model.actor.instances.player.ShortCut;
import l2p.gameserver.model.actor.instances.player.ShortCutList;
import l2p.gameserver.model.actor.listener.PlayerListenerList;
import l2p.gameserver.model.actor.recorder.PlayerStatsChangeRecorder;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.base.InvisibleType;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.DimensionalRift;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.model.entity.boat.ClanAirShip;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2p.gameserver.model.entity.events.impl.DuelEvent;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.olympiad.CompType;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.entity.olympiad.OlympiadGame;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.entity.residence.Fortress;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.instances.DecoyInstance;
import l2p.gameserver.model.instances.FestivalMonsterInstance;
import l2p.gameserver.model.instances.GuardInstance;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.instances.PetBabyInstance;
import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.model.instances.ReflectionBossInstance;
import l2p.gameserver.model.instances.StaticObjectInstance;
import l2p.gameserver.model.instances.TamedBeastInstance;
import l2p.gameserver.model.instances.TrapInstance;
import l2p.gameserver.model.items.ItemContainer;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.LockType;
import l2p.gameserver.model.items.ManufactureItem;
import l2p.gameserver.model.items.PcFreight;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.PcRefund;
import l2p.gameserver.model.items.PcWarehouse;
import l2p.gameserver.model.items.PetInventory;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.model.items.Warehouse.WarehouseType;
import l2p.gameserver.model.items.attachment.FlagItemAttachment;
import l2p.gameserver.model.items.attachment.PickableAttachment;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.model.petition.PetitionMainGroup;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.Privilege;
import l2p.gameserver.model.pledge.RankPrivs;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestEventType;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.scripts.Events;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.AbnormalStatusUpdate;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.AutoAttackStart;
import l2p.gameserver.serverpackets.CameraMode;
import l2p.gameserver.serverpackets.ChairSit;
import l2p.gameserver.serverpackets.ChangeWaitType;
import l2p.gameserver.serverpackets.CharInfo;
import l2p.gameserver.serverpackets.ConfirmDlg;
import l2p.gameserver.serverpackets.EtcStatusUpdate;
import l2p.gameserver.serverpackets.ExAutoSoulShot;
import l2p.gameserver.serverpackets.ExBR_AgathionEnergyInfo;
import l2p.gameserver.serverpackets.ExBR_ExtraUserInfo;
import l2p.gameserver.serverpackets.ExBasicActionList;
import l2p.gameserver.serverpackets.ExDominionWarStart;
import l2p.gameserver.serverpackets.ExDuelUpdateUserInfo;
import l2p.gameserver.serverpackets.ExOlympiadMatchEnd;
import l2p.gameserver.serverpackets.ExOlympiadMode;
import l2p.gameserver.serverpackets.ExOlympiadSpelledInfo;
import l2p.gameserver.serverpackets.ExPCCafePointInfo;
import l2p.gameserver.serverpackets.ExQuestItemList;
import l2p.gameserver.serverpackets.ExSetCompassZoneCode;
import l2p.gameserver.serverpackets.ExStartScenePlayer;
import l2p.gameserver.serverpackets.ExStorageMaxCount;
import l2p.gameserver.serverpackets.ExVitalityPointInfo;
import l2p.gameserver.serverpackets.ExVoteSystemInfo;
import l2p.gameserver.serverpackets.GetItem;
import l2p.gameserver.serverpackets.HennaInfo;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.ItemList;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.LeaveWorld;
import l2p.gameserver.serverpackets.MagicSkillLaunched;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.NpcInfoPoly;
import l2p.gameserver.serverpackets.ObserverEnd;
import l2p.gameserver.serverpackets.ObserverStart;
import l2p.gameserver.serverpackets.PartySmallWindowUpdate;
import l2p.gameserver.serverpackets.PartySpelled;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.PledgeShowMemberListDelete;
import l2p.gameserver.serverpackets.PledgeShowMemberListDeleteAll;
import l2p.gameserver.serverpackets.PledgeShowMemberListUpdate;
import l2p.gameserver.serverpackets.PrivateStoreListBuy;
import l2p.gameserver.serverpackets.PrivateStoreListSell;
import l2p.gameserver.serverpackets.PrivateStoreMsgBuy;
import l2p.gameserver.serverpackets.PrivateStoreMsgSell;
import l2p.gameserver.serverpackets.QuestList;
import l2p.gameserver.serverpackets.RadarControl;
import l2p.gameserver.serverpackets.RecipeShopMsg;
import l2p.gameserver.serverpackets.RecipeShopSellList;
import l2p.gameserver.serverpackets.RelationChanged;
import l2p.gameserver.serverpackets.Ride;
import l2p.gameserver.serverpackets.SendTradeDone;
import l2p.gameserver.serverpackets.ServerClose;
import l2p.gameserver.serverpackets.SetupGauge;
import l2p.gameserver.serverpackets.ShortBuffStatusUpdate;
import l2p.gameserver.serverpackets.ShortCutInit;
import l2p.gameserver.serverpackets.ShortCutRegister;
import l2p.gameserver.serverpackets.SkillCoolTime;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.SpawnEmitter;
import l2p.gameserver.serverpackets.SpecialCamera;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.TargetSelected;
import l2p.gameserver.serverpackets.TargetUnselected;
import l2p.gameserver.serverpackets.TeleportToLocation;
import l2p.gameserver.serverpackets.UserInfo;
import l2p.gameserver.serverpackets.ValidateLocation;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SceneMovie;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.skills.effects.EffectCubic;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.skills.skillclasses.Transformation;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.stats.funcs.FuncTemplate;
import l2p.gameserver.tables.CharTemplateTable;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTreeTable;
import l2p.gameserver.taskmanager.AutoSaveManager;
import l2p.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.templates.FishTemplate;
import l2p.gameserver.templates.Henna;
import l2p.gameserver.templates.InstantZone;
import l2p.gameserver.templates.InstantZoneEntryType;
import l2p.gameserver.templates.PlayerTemplate;
import l2p.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.AntiFlood;
import l2p.gameserver.utils.EffectsComparator;
import l2p.gameserver.utils.GameStats;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Language;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.SiegeUtils;
import l2p.gameserver.utils.SqlBatch;
import l2p.gameserver.utils.Strings;
import l2p.gameserver.utils.TeleportUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.IntObjectMap.Entry;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Player extends Playable
  implements PlayerGroup
{
  public static final int DEFAULT_TITLE_COLOR = 16777079;
  public static final int MAX_POST_FRIEND_SIZE = 100;
  public static final int MAX_FRIEND_SIZE = 128;
  private static final Logger _log = LoggerFactory.getLogger(Player.class);
  public static final String NO_TRADERS_VAR = "notraders";
  public static final String NO_ANIMATION_OF_CAST_VAR = "notShowBuffAnim";
  public static final String MY_BIRTHDAY_RECEIVE_YEAR = "MyBirthdayReceiveYear";
  private static final String NOT_CONNECTED = "<not connected>";
  public Map<Integer, SubClass> _classlist = new HashMap(4);
  public static final int OBSERVER_NONE = 0;
  public static final int OBSERVER_STARTING = 1;
  public static final int OBSERVER_STARTED = 3;
  public static final int OBSERVER_LEAVING = 2;
  public static final int STORE_PRIVATE_NONE = 0;
  public static final int STORE_PRIVATE_SELL = 1;
  public static final int STORE_PRIVATE_BUY = 3;
  public static final int STORE_PRIVATE_MANUFACTURE = 5;
  public static final int STORE_OBSERVING_GAMES = 7;
  public static final int STORE_PRIVATE_SELL_PACKAGE = 8;
  public static final int RANK_VAGABOND = 0;
  public static final int RANK_VASSAL = 1;
  public static final int RANK_HEIR = 2;
  public static final int RANK_KNIGHT = 3;
  public static final int RANK_WISEMAN = 4;
  public static final int RANK_BARON = 5;
  public static final int RANK_VISCOUNT = 6;
  public static final int RANK_COUNT = 7;
  public static final int RANK_MARQUIS = 8;
  public static final int RANK_DUKE = 9;
  public static final int RANK_GRAND_DUKE = 10;
  public static final int RANK_DISTINGUISHED_KING = 11;
  public static final int RANK_EMPEROR = 12;
  public static final int LANG_ENG = 0;
  public static final int LANG_RUS = 1;
  public static final int LANG_UNK = -1;
  public static final int[] EXPERTISE_LEVELS = { 0, 20, 40, 52, 61, 76, 80, 84, 2147483647 };
  private GameClient _connection;
  private String _login;
  private int _karma;
  private int _pkKills;
  private int _pvpKills;
  private int _face;
  private int _hairStyle;
  private int _hairColor;
  private int _recomHave;
  private int _recomLeftToday;
  private int _fame;
  private int _recomLeft = 20;
  private int _recomBonusTime = 3600;
  private boolean _isHourglassEffected;
  private boolean _isRecomTimerActive;
  private boolean _isUndying = false;
  private int _deleteTimer;
  private long _createTime;
  private long _onlineTime;
  private long _onlineBeginTime;
  private long _leaveClanTime;
  private long _deleteClanTime;
  private long _NoChannel;
  private long _NoChannelBegin;
  private long _uptime;
  private long _lastAccess;
  private int _nameColor;
  private int _titlecolor;
  private int _vitalityLevel = -1;
  private double _vitality = Config.VITALITY_LEVELS[4];
  private boolean _overloaded;
  boolean sittingTaskLaunched;
  private int _waitTimeWhenSit;
  private boolean _autoLoot = Config.AUTO_LOOT; private boolean AutoLootHerbs = Config.AUTO_LOOT_HERBS;

  private final PcInventory _inventory = new PcInventory(this);
  private final Warehouse _warehouse = new PcWarehouse(this);
  private final ItemContainer _refund = new PcRefund(this);
  private final PcFreight _freight = new PcFreight(this);

  public final BookMarkList bookmarks = new BookMarkList(this, 0);

  public final AntiFlood antiFlood = new AntiFlood();

  private final Map<Integer, Recipe> _recipebook = new TreeMap();
  private final Map<Integer, Recipe> _commonrecipebook = new TreeMap();

  private Map<Integer, PremiumItem> _premiumItems = new TreeMap();

  private final Map<String, QuestState> _quests = new HashMap();

  private final ShortCutList _shortCuts = new ShortCutList(this);

  private final MacroList _macroses = new MacroList(this);
  private int _privatestore;
  private String _manufactureName;
  private List<ManufactureItem> _createList = Collections.emptyList();
  private String _sellStoreName;
  private List<TradeItem> _sellList = Collections.emptyList();
  private List<TradeItem> _packageSellList = Collections.emptyList();
  private String _buyStoreName;
  private List<TradeItem> _buyList = Collections.emptyList();

  private List<TradeItem> _tradeList = Collections.emptyList();

  private final Henna[] _henna = new Henna[3];
  private int _hennaSTR;
  private int _hennaINT;
  private int _hennaDEX;
  private int _hennaMEN;
  private int _hennaWIT;
  private int _hennaCON;
  private Party _party;
  private Location _lastPartyPosition;
  private Clan _clan;
  private int _pledgeClass = 0; private int _pledgeType = -128; private int _powerGrade = 0; private int _lvlJoinedAcademy = 0; private int _apprentice = 0;
  private int _accessLevel;
  private PlayerAccess _playerAccess = new PlayerAccess();

  private boolean _messageRefusal = false; private boolean _tradeRefusal = false; private boolean _blockAll = false;

  private Summon _summon = null;
  private boolean _riding;
  private DecoyInstance _decoy = null;

  private Map<Integer, EffectCubic> _cubics = null;
  private int _agathionId = 0;
  private Request _request;
  private ItemInstance _arrowItem;
  private WeaponTemplate _fistsWeaponItem;
  private Map<Integer, String> _chars = new HashMap(8);

  public int expertiseIndex = 0;

  private ItemInstance _enchantScroll = null;
  private Warehouse.WarehouseType _usingWHType;
  private boolean _isOnline = false;

  private AtomicBoolean _isLogout = new AtomicBoolean();

  private HardReference<NpcInstance> _lastNpc = HardReferences.emptyRef();

  private MultiSellHolder.MultiSellListContainer _multisell = null;

  private Set<Integer> _activeSoulShots = new CopyOnWriteArraySet();
  private WorldRegion _observerRegion;
  private AtomicInteger _observerMode = new AtomicInteger(0);

  public int _telemode = 0;

  private int _handysBlockCheckerEventArena = -1;

  public boolean entering = true;

  public Location _stablePoint = null;

  public int[] _loto = new int[5];

  public int[] _race = new int[2];

  private final Map<Integer, String> _blockList = new ConcurrentSkipListMap();
  private final FriendList _friendList = new FriendList(this);

  private boolean _hero = false;
  private Boat _boat;
  private Location _inBoatPosition;
  protected int _baseClass = -1;
  protected SubClass _activeClass = null;

  private Bonus _bonus = new Bonus();
  private Future<?> _bonusExpiration;
  private boolean _isSitting;
  private StaticObjectInstance _sittingObject;
  private boolean _noble = false;
  private boolean _inOlympiadMode;
  private OlympiadGame _olympiadGame;
  private OlympiadGame _olympiadObserveGame;
  private int _olympiadSide = -1;

  private int _varka = 0;
  private int _ketra = 0;
  private int _ram = 0;

  private byte[] _keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;

  private int _cursedWeaponEquippedId = 0;

  private final Fishing _fishing = new Fishing(this);
  private boolean _isFishing;
  private Future<?> _taskWater;
  private Future<?> _autoSaveTask;
  private Future<?> _kickTask;
  private Future<?> _vitalityTask;
  private Future<?> _pcCafePointsTask;
  private Future<?> _unjailTask;
  private final Lock _storeLock = new ReentrantLock();
  private int _zoneMask;
  private boolean _offline = false;
  private int _transformationId;
  private int _transformationTemplate;
  private String _transformationName;
  private int _pcBangPoints;
  Map<Integer, Skill> _transformationSkills = new HashMap();

  private int _expandInventory = 0;
  private int _expandWarehouse = 0;
  private int _battlefieldChatId;
  private int _lectureMark;
  private InvisibleType _invisibleType = InvisibleType.NONE;

  private List<String> bypasses = null; private List<String> bypasses_bbs = null;
  private IntObjectMap<String> _postFriends = Containers.emptyIntObjectMap();

  private List<String> _blockedActions = new ArrayList();

  private boolean _notShowBuffAnim = false;
  private boolean _notShowTraders = false;
  private boolean _debug = false;
  private long _dropDisabled;
  private long _lastItemAuctionInfoRequest;
  private IntObjectMap<TimeStamp> _sharedGroupReuses = new CHashIntObjectMap();
  private Pair<Integer, OnAnswerListener> _askDialog = null;

  private NevitSystem _nevitSystem = new NevitSystem(this);
  private MatchingRoom _matchingRoom;
  private PetitionMainGroup _petitionGroup;
  private final Map<Integer, Long> _instancesReuses = new ConcurrentHashMap();

  private String _htmlPrefix = null;
  private ScheduledFuture<?> _recomBonusTask;
  private Future<?> _updateEffectIconsTask;
  private ScheduledFuture<?> _broadcastCharInfoTask;
  private int _polyNpcId;
  private Future<?> _userInfoTask;
  private int _mountNpcId;
  private int _mountObjId;
  private int _mountLevel;
  private final Map<String, String> user_variables = new ConcurrentHashMap();

  private boolean _maried = false;
  private int _partnerId = 0;
  private int _coupleId = 0;
  private boolean _maryrequest = false;
  private boolean _maryaccepted = false;

  private boolean _charmOfCourage = false;

  private int _increasedForce = 0;
  private int _consumedSouls = 0;
  private long _lastFalling;
  private Location _lastClientPosition;
  private Location _lastServerPosition;
  private int _useSeed = 0;
  protected int _pvpFlag;
  private Future<?> _PvPRegTask;
  private long _lastPvpAttack;
  private Map<Integer, TamedBeastInstance> _tamedBeasts = new ConcurrentHashMap();

  private long _lastAttackPacket = 0L;

  private long _lastMovePacket = 0L;
  private Location _groundSkillLoc;
  private int _buyListId;
  private final int _incorrectValidateCount = 0;

  private int _movieId = 0;
  private boolean _isInMovie;
  private ItemInstance _petControlItem = null;

  private AtomicBoolean isActive = new AtomicBoolean();
  private Map<Integer, Long> _traps;
  private Future<?> _hourlyTask;
  private int _hoursInGame = 0;

  private boolean _agathionResAvailable = false;
  private Map<String, String> _userSession;

  public String getHtmlPrefix()
  {
    return _htmlPrefix;
  }

  public Player(int objectId, PlayerTemplate template, String accountName)
  {
    super(objectId, template);

    _login = accountName;
    _nameColor = 16777215;
    _titlecolor = 16777079;
    _baseClass = getClassId().getId();
  }

  private Player(int objectId, PlayerTemplate template)
  {
    this(objectId, template, null);

    _ai = new PlayerAI(this);

    if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
      setPlayerAccess((PlayerAccess)Config.gmlist.get(Integer.valueOf(objectId)));
    else
      setPlayerAccess((PlayerAccess)Config.gmlist.get(Integer.valueOf(0)));
  }

  public HardReference<Player> getRef()
  {
    return super.getRef();
  }

  public String getAccountName()
  {
    if (_connection == null)
      return _login;
    return _connection.getLogin();
  }

  public String getIP()
  {
    if (_connection == null)
      return "<not connected>";
    return _connection.getIpAddr();
  }

  public Map<Integer, String> getAccountChars()
  {
    return _chars;
  }

  public final PlayerTemplate getTemplate()
  {
    return (PlayerTemplate)_template;
  }

  public PlayerTemplate getBaseTemplate()
  {
    return (PlayerTemplate)_baseTemplate;
  }

  public void changeSex()
  {
    boolean male = true;
    if (getSex() == 1)
      male = false;
    _template = CharTemplateTable.getInstance().getTemplate(getClassId(), !male);
  }

  public PlayerAI getAI()
  {
    return (PlayerAI)_ai;
  }

  public void doCast(Skill skill, Creature target, boolean forceUse)
  {
    if (skill == null) {
      return;
    }
    super.doCast(skill, target, forceUse);
  }

  public void sendReuseMessage(Skill skill)
  {
    if (isCastingNow())
      return;
    TimeStamp sts = getSkillReuse(skill);
    if ((sts == null) || (!sts.hasNotPassed()))
      return;
    long timeleft = sts.getReuseCurrent();
    if (((!Config.ALT_SHOW_REUSE_MSG) && (timeleft < 10000L)) || (timeleft < 500L))
      return;
    long hours = timeleft / 3600000L;
    long minutes = (timeleft - hours * 3600000L) / 60000L;
    long seconds = ()Math.ceil((timeleft - hours * 3600000L - minutes * 60000L) / 1000.0D);
    if (hours > 0L)
      sendPacket(new SystemMessage(2305).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
    else if (minutes > 0L)
      sendPacket(new SystemMessage(2304).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
    else
      sendPacket(new SystemMessage(2303).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));
  }

  public final int getLevel()
  {
    return _activeClass == null ? 1 : _activeClass.getLevel();
  }

  public int getSex()
  {
    return getTemplate().isMale ? 0 : 1;
  }

  public int getFace()
  {
    return _face;
  }

  public void setFace(int face)
  {
    _face = face;
  }

  public int getHairColor()
  {
    return _hairColor;
  }

  public void setHairColor(int hairColor)
  {
    _hairColor = hairColor;
  }

  public int getHairStyle()
  {
    return _hairStyle;
  }

  public void setHairStyle(int hairStyle)
  {
    _hairStyle = hairStyle;
  }

  public void offline()
  {
    if (_connection != null)
    {
      _connection.setActiveChar(null);
      _connection.close(ServerClose.STATIC);
      setNetConnection(null);
    }

    setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
    setOfflineMode(true);

    setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L), -1L);

    if (Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L) {
      startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);
    }
    Party party = getParty();
    if (party != null)
    {
      if (isFestivalParticipant())
        party.broadcastMessageToPartyMembers(new StringBuilder().append(getName()).append(" has been removed from the upcoming festival.").toString());
      leaveParty();
    }

    if (getPet() != null) {
      getPet().unSummon();
    }
    CursedWeaponsManager.getInstance().doLogout(this);

    if (isInOlympiadMode()) {
      Olympiad.logoutPlayer(this);
    }
    broadcastCharInfo();
    stopWaterTask();
    stopBonusTask();
    stopHourlyTask();
    stopVitalityTask();
    stopPcBangPointsTask();
    stopAutoSaveTask();
    stopRecomBonusTask(true);
    stopQuestTimers();
    getNevitSystem().stopTasksOnLogout();
    try
    {
      getInventory().store();
    }
    catch (Throwable t)
    {
      _log.error("", t);
    }

    try
    {
      store(false);
    }
    catch (Throwable t)
    {
      _log.error("", t);
    }
  }

  public void kick()
  {
    if (_connection != null)
    {
      _connection.close(LeaveWorld.STATIC);
      setNetConnection(null);
    }
    prepareToLogout();
    deleteMe();
  }

  public void restart()
  {
    if (_connection != null)
    {
      _connection.setActiveChar(null);
      setNetConnection(null);
    }
    prepareToLogout();
    deleteMe();
  }

  public void logout()
  {
    if (_connection != null)
    {
      _connection.close(ServerClose.STATIC);
      setNetConnection(null);
    }
    prepareToLogout();
    deleteMe();
  }

  private void prepareToLogout()
  {
    if (_isLogout.getAndSet(true)) {
      return;
    }
    setNetConnection(null);
    setIsOnline(false);

    getListeners().onExit();

    if ((isFlying()) && (!checkLandingState())) {
      _stablePoint = TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE);
    }
    if (isCastingNow()) {
      abortCast(true, true);
    }
    Party party = getParty();
    if (party != null)
    {
      if (isFestivalParticipant())
        party.broadcastMessageToPartyMembers(new StringBuilder().append(getName()).append(" has been removed from the upcoming festival.").toString());
      leaveParty();
    }

    CursedWeaponsManager.getInstance().doLogout(this);

    if (_olympiadObserveGame != null) {
      _olympiadObserveGame.removeSpectator(this);
    }
    if (isInOlympiadMode()) {
      Olympiad.logoutPlayer(this);
    }
    stopFishing();

    if (_stablePoint != null) {
      teleToLocation(_stablePoint);
    }
    Summon pet = getPet();
    if (pet != null)
    {
      pet.saveEffects();
      pet.unSummon();
    }

    _friendList.notifyFriends(false);

    if (isProcessingRequest()) {
      getRequest().cancel();
    }
    stopAllTimers();

    if (isInBoat()) {
      getBoat().removePlayer(this);
    }
    SubUnit unit = getSubUnit();
    UnitMember member = unit == null ? null : unit.getUnitMember(getObjectId());
    if (member != null)
    {
      int sponsor = member.getSponsor();
      int apprentice = getApprentice();
      PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
      for (Player clanMember : _clan.getOnlineMembers(getObjectId()))
      {
        clanMember.sendPacket(memberUpdate);
        if (clanMember.getObjectId() == sponsor)
          clanMember.sendPacket(new SystemMessage(1757).addString(_name));
        else if (clanMember.getObjectId() == apprentice)
          clanMember.sendPacket(new SystemMessage(1759).addString(_name));
      }
      member.setPlayerInstance(this, true);
    }

    FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
    if (attachment != null) {
      attachment.onLogout(this);
    }
    if (CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null) {
      CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);
    }
    MatchingRoom room = getMatchingRoom();
    if (room != null)
    {
      if (room.getLeader() == this)
        room.disband();
      else
        room.removeMember(this, false);
    }
    setMatchingRoom(null);

    MatchingRoomManager.getInstance().removeFromWaitingList(this);

    destroyAllTraps();

    if (_decoy != null)
    {
      _decoy.unSummon();
      _decoy = null;
    }

    stopPvPFlag();

    Reflection ref = getReflection();

    if (ref != ReflectionManager.DEFAULT)
    {
      if (ref.getReturnLoc() != null) {
        _stablePoint = ref.getReturnLoc();
      }
      ref.removeObject(this);
    }

    try
    {
      getInventory().store();
      getRefund().clear();
    }
    catch (Throwable t)
    {
      _log.error("", t);
    }

    try
    {
      store(false);
    }
    catch (Throwable t)
    {
      _log.error("", t);
    }
  }

  public Collection<Recipe> getDwarvenRecipeBook()
  {
    return _recipebook.values();
  }

  public Collection<Recipe> getCommonRecipeBook()
  {
    return _commonrecipebook.values();
  }

  public int recipesCount()
  {
    return _commonrecipebook.size() + _recipebook.size();
  }

  public boolean hasRecipe(Recipe id)
  {
    return (_recipebook.containsValue(id)) || (_commonrecipebook.containsValue(id));
  }

  public boolean findRecipe(int id)
  {
    return (_recipebook.containsKey(Integer.valueOf(id))) || (_commonrecipebook.containsKey(Integer.valueOf(id)));
  }

  public void registerRecipe(Recipe recipe, boolean saveDB)
  {
    if (recipe == null)
      return;
    if (recipe.isDwarvenRecipe())
      _recipebook.put(Integer.valueOf(recipe.getId()), recipe);
    else
      _commonrecipebook.put(Integer.valueOf(recipe.getId()), recipe);
    if (saveDB)
      mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", new Object[] { Integer.valueOf(getObjectId()), Integer.valueOf(recipe.getId()) });
  }

  public void unregisterRecipe(int RecipeID)
  {
    if (_recipebook.containsKey(Integer.valueOf(RecipeID)))
    {
      mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", new Object[] { Integer.valueOf(getObjectId()), Integer.valueOf(RecipeID) });
      _recipebook.remove(Integer.valueOf(RecipeID));
    }
    else if (_commonrecipebook.containsKey(Integer.valueOf(RecipeID)))
    {
      mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", new Object[] { Integer.valueOf(getObjectId()), Integer.valueOf(RecipeID) });
      _commonrecipebook.remove(Integer.valueOf(RecipeID));
    }
    else {
      _log.warn(new StringBuilder().append("Attempted to remove unknown RecipeList").append(RecipeID).toString());
    }
  }

  public QuestState getQuestState(String quest)
  {
    questRead.lock();
    try
    {
      QuestState localQuestState = (QuestState)_quests.get(quest);
      return localQuestState; } finally { questRead.unlock(); } throw localObject;
  }

  public QuestState getQuestState(Class<?> quest)
  {
    return getQuestState(quest.getSimpleName());
  }

  public boolean isQuestCompleted(String quest)
  {
    QuestState q = getQuestState(quest);
    return (q != null) && (q.isCompleted());
  }

  public boolean isQuestCompleted(Class<?> quest)
  {
    QuestState q = getQuestState(quest);
    return (q != null) && (q.isCompleted());
  }

  public void setQuestState(QuestState qs)
  {
    questWrite.lock();
    try
    {
      _quests.put(qs.getQuest().getName(), qs);
    }
    finally
    {
      questWrite.unlock();
    }
  }

  public void removeQuestState(String quest)
  {
    questWrite.lock();
    try
    {
      _quests.remove(quest);
    }
    finally
    {
      questWrite.unlock();
    }
  }

  public Quest[] getAllActiveQuests()
  {
    List quests = new ArrayList(_quests.size());
    questRead.lock();
    try
    {
      for (QuestState qs : _quests.values())
        if (qs.isStarted())
          quests.add(qs.getQuest());
    }
    finally
    {
      questRead.unlock();
    }
    return (Quest[])quests.toArray(new Quest[quests.size()]);
  }

  public QuestState[] getAllQuestsStates()
  {
    questRead.lock();
    try
    {
      QuestState[] arrayOfQuestState = (QuestState[])_quests.values().toArray(new QuestState[_quests.size()]);
      return arrayOfQuestState; } finally { questRead.unlock(); } throw localObject;
  }

  public List<QuestState> getQuestsForEvent(NpcInstance npc, QuestEventType event)
  {
    List states = new ArrayList();
    Quest[] quests = npc.getTemplate().getEventQuests(event);

    if (quests != null)
      for (Quest quest : quests)
      {
        QuestState qs = getQuestState(quest.getName());
        if ((qs != null) && (!qs.isCompleted()))
          states.add(getQuestState(quest.getName()));
      }
    return states;
  }

  public void processQuestEvent(String quest, String event, NpcInstance npc)
  {
    if (event == null)
      event = "";
    QuestState qs = getQuestState(quest);
    if (qs == null)
    {
      Quest q = QuestManager.getQuest(quest);
      if (q == null)
      {
        _log.warn(new StringBuilder().append("Quest ").append(quest).append(" not found!").toString());
        return;
      }
      qs = q.newQuestState(this, 1);
    }
    if ((qs == null) || (qs.isCompleted()))
      return;
    qs.getQuest().notifyEvent(event, qs, npc);
    sendPacket(new QuestList(this));
  }

  public boolean isQuestContinuationPossible(boolean msg)
  {
    if ((getWeightPenalty() >= 3) || (getInventoryLimit() * 0.9D < getInventory().getSize()) || (Config.QUEST_INVENTORY_MAXIMUM * 0.9D < getInventory().getQuestSize()))
    {
      if (msg)
        sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
      return false;
    }
    return true;
  }

  public void stopQuestTimers()
  {
    for (QuestState qs : getAllQuestsStates())
      if (qs.isStarted())
        qs.pauseQuestTimers();
      else
        qs.stopQuestTimers();
  }

  public void resumeQuestTimers()
  {
    for (QuestState qs : getAllQuestsStates())
      qs.resumeQuestTimers();
  }

  public Collection<ShortCut> getAllShortCuts()
  {
    return _shortCuts.getAllShortCuts();
  }

  public ShortCut getShortCut(int slot, int page)
  {
    return _shortCuts.getShortCut(slot, page);
  }

  public void registerShortCut(ShortCut shortcut)
  {
    _shortCuts.registerShortCut(shortcut);
  }

  public void deleteShortCut(int slot, int page)
  {
    _shortCuts.deleteShortCut(slot, page);
  }

  public void registerMacro(Macro macro)
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

  public boolean isCastleLord(int castleId)
  {
    return (_clan != null) && (isClanLeader()) && (_clan.getCastle() == castleId);
  }

  public boolean isFortressLord(int fortressId)
  {
    return (_clan != null) && (isClanLeader()) && (_clan.getHasFortress() == fortressId);
  }

  public int getPkKills()
  {
    return _pkKills;
  }

  public void setPkKills(int pkKills)
  {
    _pkKills = pkKills;
  }

  public long getCreateTime()
  {
    return _createTime;
  }

  public void setCreateTime(long createTime)
  {
    _createTime = createTime;
  }

  public int getDeleteTimer()
  {
    return _deleteTimer;
  }

  public void setDeleteTimer(int deleteTimer)
  {
    _deleteTimer = deleteTimer;
  }

  public int getCurrentLoad()
  {
    return getInventory().getTotalWeight();
  }

  public long getLastAccess()
  {
    return _lastAccess;
  }

  public void setLastAccess(long value)
  {
    _lastAccess = value;
  }

  public int getRecomHave()
  {
    return _recomHave;
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

  public int getRecomBonusTime()
  {
    if (_recomBonusTask != null)
      return (int)Math.max(0L, _recomBonusTask.getDelay(TimeUnit.SECONDS));
    return _recomBonusTime;
  }

  public void setRecomBonusTime(int val)
  {
    _recomBonusTime = val;
  }

  public int getRecomLeft()
  {
    return _recomLeft;
  }

  public void setRecomLeft(int value)
  {
    _recomLeft = value;
  }

  public boolean isHourglassEffected()
  {
    return _isHourglassEffected;
  }

  public void setHourlassEffected(boolean val)
  {
    _isHourglassEffected = val;
  }

  public void startHourglassEffect()
  {
    setHourlassEffected(true);
    stopRecomBonusTask(true);
    sendVoteSystemInfo();
  }

  public void stopHourglassEffect()
  {
    setHourlassEffected(false);
    startRecomBonusTask();
    sendVoteSystemInfo();
  }

  public int addRecomLeft()
  {
    int recoms = 0;
    if (getRecomLeftToday() < 20)
      recoms = 10;
    else
      recoms = 1;
    setRecomLeft(getRecomLeft() + recoms);
    setRecomLeftToday(getRecomLeftToday() + recoms);
    sendUserInfo(true);
    return recoms;
  }

  public int getRecomLeftToday()
  {
    return _recomLeftToday;
  }

  public void setRecomLeftToday(int value)
  {
    _recomLeftToday = value;
    setVar("recLeftToday", String.valueOf(_recomLeftToday), -1L);
  }

  public void giveRecom(Player target)
  {
    int targetRecom = target.getRecomHave();
    if (targetRecom < 255)
      target.addRecomHave(1);
    if (getRecomLeft() > 0) {
      setRecomLeft(getRecomLeft() - 1);
    }
    sendUserInfo(true);
  }

  public void addRecomHave(int val)
  {
    setRecomHave(getRecomHave() + val);
    broadcastUserInfo(true);
    sendVoteSystemInfo();
  }

  public int getRecomBonus()
  {
    if ((getRecomBonusTime() > 0) || (isHourglassEffected()))
      return RecomBonus.getRecoBonus(this);
    return 0;
  }

  public double getRecomBonusMul()
  {
    if ((getRecomBonusTime() > 0) || (isHourglassEffected()))
      return RecomBonus.getRecoMultiplier(this);
    return 1.0D;
  }

  public void sendVoteSystemInfo()
  {
    sendPacket(new ExVoteSystemInfo(this));
  }

  public boolean isRecomTimerActive()
  {
    return _isRecomTimerActive;
  }

  public void setRecomTimerActive(boolean val)
  {
    if (_isRecomTimerActive == val) {
      return;
    }
    _isRecomTimerActive = val;

    if (val)
      startRecomBonusTask();
    else {
      stopRecomBonusTask(true);
    }
    sendVoteSystemInfo();
  }

  public void startRecomBonusTask()
  {
    if ((_recomBonusTask == null) && (getRecomBonusTime() > 0) && (isRecomTimerActive()) && (!isHourglassEffected()))
      _recomBonusTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.RecomBonusTask(this), getRecomBonusTime() * 1000);
  }

  public void stopRecomBonusTask(boolean saveTime)
  {
    if (_recomBonusTask != null)
    {
      if (saveTime)
        setRecomBonusTime((int)Math.max(0L, _recomBonusTask.getDelay(TimeUnit.SECONDS)));
      _recomBonusTask.cancel(false);
      _recomBonusTask = null;
    }
  }

  public int getKarma()
  {
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
    _karma = karma;

    sendChanges();

    if (getPet() != null)
      getPet().broadcastCharInfo();
  }

  public int getMaxLoad()
  {
    int con = getCON();
    if (con < 1)
      return (int)(31000.0D * Config.MAXLOAD_MODIFIER);
    if (con > 59) {
      return (int)(176000.0D * Config.MAXLOAD_MODIFIER);
    }
    return (int)calcStat(Stats.MAX_LOAD, Math.pow(1.029993928D, con) * 30495.627366000001D * Config.MAXLOAD_MODIFIER, this, null);
  }

  public void updateEffectIcons()
  {
    if ((entering) || (isLogoutStarted())) {
      return;
    }
    if (Config.USER_INFO_INTERVAL == 0L)
    {
      if (_updateEffectIconsTask != null)
      {
        _updateEffectIconsTask.cancel(false);
        _updateEffectIconsTask = null;
      }
      updateEffectIconsImpl();
      return;
    }

    if (_updateEffectIconsTask != null) {
      return;
    }
    _updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(null), Config.USER_INFO_INTERVAL);
  }

  private void updateEffectIconsImpl()
  {
    Effect[] effects = getEffectList().getAllFirstEffects();
    Arrays.sort(effects, EffectsComparator.getInstance());

    PartySpelled ps = new PartySpelled(this, false);
    AbnormalStatusUpdate mi = new AbnormalStatusUpdate();

    for (Effect effect : effects) {
      if (!effect.isInUse())
        continue;
      if (effect.getStackType().equals(EffectTemplate.HP_RECOVER_CAST))
        sendPacket(new ShortBuffStatusUpdate(effect));
      else
        effect.addIcon(mi);
      if (_party != null) {
        effect.addPartySpelledIcon(ps);
      }
    }
    sendPacket(mi);
    if (_party != null)
      _party.broadCast(new IStaticPacket[] { ps });
    ExOlympiadSpelledInfo olympiadSpelledInfo;
    if ((isInOlympiadMode()) && (isOlympiadCompStart()))
    {
      OlympiadGame olymp_game = _olympiadGame;
      if (olymp_game != null)
      {
        olympiadSpelledInfo = new ExOlympiadSpelledInfo();

        for (Effect effect : effects) {
          if ((effect != null) && (effect.isInUse()))
            effect.addOlympiadSpelledIcon(this, olympiadSpelledInfo);
        }
        if ((olymp_game.getType() == CompType.CLASSED) || (olymp_game.getType() == CompType.NON_CLASSED)) {
          for (Player member : olymp_game.getTeamMembers(this))
            member.sendPacket(olympiadSpelledInfo);
        }
        for (Player member : olymp_game.getSpectators())
          member.sendPacket(olympiadSpelledInfo);
      }
    }
  }

  public int getWeightPenalty()
  {
    return getSkillLevel(Integer.valueOf(4270), 0);
  }

  public void refreshOverloaded()
  {
    if ((isLogoutStarted()) || (getMaxLoad() <= 0)) {
      return;
    }
    setOverloaded(getCurrentLoad() > getMaxLoad());
    double weightproc = 100.0D * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0.0D, this, null)) / getMaxLoad();
    int newWeightPenalty = 0;

    if (weightproc < 50.0D)
      newWeightPenalty = 0;
    else if (weightproc < 66.599999999999994D)
      newWeightPenalty = 1;
    else if (weightproc < 80.0D)
      newWeightPenalty = 2;
    else if (weightproc < 100.0D)
      newWeightPenalty = 3;
    else {
      newWeightPenalty = 4;
    }
    int current = getWeightPenalty();
    if (current == newWeightPenalty) {
      return;
    }
    if (newWeightPenalty > 0)
      super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
    else {
      super.removeSkill(getKnownSkill(4270));
    }
    sendPacket(new SkillList(this));
    sendEtcStatusUpdate();
    updateStats();
  }

  public int getArmorsExpertisePenalty()
  {
    return getSkillLevel(Integer.valueOf(6213), 0);
  }

  public int getWeaponsExpertisePenalty()
  {
    return getSkillLevel(Integer.valueOf(6209), 0);
  }

  public int getExpertisePenalty(ItemInstance item)
  {
    if (item.getTemplate().getType2() == 0)
      return getWeaponsExpertisePenalty();
    if ((item.getTemplate().getType2() == 1) || (item.getTemplate().getType2() == 2))
      return getArmorsExpertisePenalty();
    return 0;
  }

  public void refreshExpertisePenalty()
  {
    if (isLogoutStarted()) {
      return;
    }

    int level = (int)calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
    int i = 0;
    for (i = 0; (i < EXPERTISE_LEVELS.length) && 
      (level >= EXPERTISE_LEVELS[(i + 1)]); i++);
    boolean skillUpdate = false;

    if (expertiseIndex != i)
    {
      expertiseIndex = i;
      if ((expertiseIndex > 0) && (Config.ADD_EXPERTISE_PENALTY))
      {
        addSkill(SkillTable.getInstance().getInfo(239, expertiseIndex), false);
        skillUpdate = true;
      }
    }

    int newWeaponPenalty = 0;
    int newArmorPenalty = 0;
    ItemInstance[] items = getInventory().getPaperdollItems();
    for (ItemInstance item : items) {
      if (item == null)
        continue;
      int crystaltype = item.getTemplate().getCrystalType().ordinal();
      if (item.getTemplate().getType2() == 0)
      {
        if (crystaltype > newWeaponPenalty)
          newWeaponPenalty = crystaltype;
      } else {
        if (((item.getTemplate().getType2() != 1) && (item.getTemplate().getType2() != 2)) || 
          (crystaltype <= newArmorPenalty)) continue;
        newArmorPenalty = crystaltype;
      }
    }
    newWeaponPenalty -= expertiseIndex;
    if (newWeaponPenalty <= 0)
      newWeaponPenalty = 0;
    else if (newWeaponPenalty >= 4) {
      newWeaponPenalty = 4;
    }
    newArmorPenalty -= expertiseIndex;
    if (newArmorPenalty <= 0)
      newArmorPenalty = 0;
    else if (newArmorPenalty >= 4) {
      newArmorPenalty = 4;
    }
    int weaponExpertise = getWeaponsExpertisePenalty();
    int armorExpertise = getArmorsExpertisePenalty();

    if (weaponExpertise != newWeaponPenalty)
    {
      weaponExpertise = newWeaponPenalty;
      if ((newWeaponPenalty > 0) && (Config.ADD_EXPERTISE_PENALTY))
        super.addSkill(SkillTable.getInstance().getInfo(6209, weaponExpertise));
      else
        super.removeSkill(getKnownSkill(6209));
      skillUpdate = true;
    }
    if (armorExpertise != newArmorPenalty)
    {
      armorExpertise = newArmorPenalty;
      if ((newArmorPenalty > 0) && (Config.ADD_EXPERTISE_PENALTY))
        super.addSkill(SkillTable.getInstance().getInfo(6213, armorExpertise));
      else
        super.removeSkill(getKnownSkill(6213));
      if (Config.ADD_EXPERTISE_PENALTY)
        skillUpdate = true;
      else {
        skillUpdate = false;
      }
    }
    if (skillUpdate)
    {
      getInventory().validateItemsSkills();

      sendPacket(new SkillList(this));
      sendEtcStatusUpdate();
      updateStats();
    }
  }

  public int getPvpKills()
  {
    return _pvpKills;
  }

  public void setPvpKills(int pvpKills)
  {
    _pvpKills = pvpKills;
  }

  public ClassId getClassId()
  {
    return getTemplate().classId;
  }

  public void addClanPointsOnProfession(int id)
  {
    if ((getLvlJoinedAcademy() != 0) && (_clan != null) && (_clan.getLevel() >= 5) && (ClassId.VALUES[id].getLevel() == 2)) {
      _clan.incReputation(100, true, "Academy");
    } else if ((getLvlJoinedAcademy() != 0) && (_clan != null) && (_clan.getLevel() >= 5) && (ClassId.VALUES[id].getLevel() == 3))
    {
      int earnedPoints = 0;
      if (getLvlJoinedAcademy() <= 16)
        earnedPoints = Config.ADD_MAX_ACADEM_POINT;
      else if (getLvlJoinedAcademy() >= 39)
        earnedPoints = Config.ADD_MIN_ACADEM_POINT;
      else {
        earnedPoints = Config.ADD_MAX_ACADEM_POINT - (getLvlJoinedAcademy() - 16) * 20;
      }
      _clan.removeClanMember(getObjectId());

      SystemMessage sm = new SystemMessage(1748);
      sm.addString(getName());
      sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
      _clan.broadcastToOnlineMembers(new L2GameServerPacket[] { sm });
      _clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);

      setClan(null);
      setTitle("");
      sendPacket(Msg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
      setLeaveClanTime(0L);

      broadcastCharInfo();

      sendPacket(PledgeShowMemberListDeleteAll.STATIC);

      ItemFunctions.addItem(this, 8181, 1L, true);
    }
  }

  public synchronized void setClassId(int id, boolean noban, boolean fromQuest)
  {
    if ((!noban) && (!ClassId.VALUES[id].equalsOrChildOf(ClassId.VALUES[getActiveClassId()])) && (!getPlayerAccess().CanChangeClass) && (!Config.EVERYBODY_HAS_ADMIN_RIGHTS))
    {
      Thread.dumpStack();
      return;
    }

    if (!getSubClasses().containsKey(Integer.valueOf(id)))
    {
      SubClass cclass = getActiveClass();
      getSubClasses().remove(Integer.valueOf(getActiveClassId()));
      changeClassInDb(cclass.getClassId(), id);
      if (cclass.isBase())
      {
        setBaseClass(id);
        addClanPointsOnProfession(id);
        ItemInstance coupons = null;
        if (ClassId.VALUES[id].getLevel() == 2)
        {
          if ((fromQuest) && (Config.ALT_ALLOW_SHADOW_WEAPONS))
            coupons = ItemFunctions.createItem(8869);
          unsetVar("newbieweapon");
          unsetVar("p1q2");
          unsetVar("p1q3");
          unsetVar("p1q4");
          unsetVar("prof1");
          unsetVar("ng1");
          unsetVar("ng2");
          unsetVar("ng3");
          unsetVar("ng4");
        }
        else if (ClassId.VALUES[id].getLevel() == 3)
        {
          if ((fromQuest) && (Config.ALT_ALLOW_SHADOW_WEAPONS))
            coupons = ItemFunctions.createItem(8870);
          unsetVar("newbiearmor");
          unsetVar("dd1");
          unsetVar("dd2");
          unsetVar("dd3");
          unsetVar("prof2.1");
          unsetVar("prof2.2");
          unsetVar("prof2.3");
        }

        if (coupons != null)
        {
          coupons.setCount(15L);
          sendPacket(SystemMessage2.obtainItems(coupons));
          getInventory().addItem(coupons);
        }

      }

      switch (3.$SwitchMap$l2p$gameserver$model$base$ClassId[ClassId.VALUES[id].ordinal()])
      {
      case 1:
        ItemFunctions.addItem(this, 15307, 1L, true);
        break;
      case 2:
        ItemFunctions.addItem(this, 15308, 1L, true);
        break;
      case 3:
        ItemFunctions.addItem(this, 15309, 4L, true);
      }

      cclass.setClassId(id);
      getSubClasses().put(Integer.valueOf(id), cclass);
      rewardSkills(true);
      storeCharSubClasses();

      if (fromQuest)
      {
        broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(this, this, 5103, 1, 1000, 0L) });
        sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
      }
      broadcastCharInfo();
    }

    PlayerTemplate t = CharTemplateTable.getInstance().getTemplate(id, getSex() == 1);
    if (t == null)
    {
      _log.error(new StringBuilder().append("Missing template for classId: ").append(id).toString());

      return;
    }

    _template = t;

    if (isInParty())
      getParty().broadCast(new IStaticPacket[] { new PartySmallWindowUpdate(this) });
    if (getClan() != null)
      getClan().broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowMemberListUpdate(this) });
    if (_matchingRoom != null) {
      _matchingRoom.broadcastPlayerUpdate(this);
    }

    if (Config.ENABLE_PROF_SOCIAL_ACTION)
      broadcastPacket(new L2GameServerPacket[] { new SocialAction(getObjectId(), Config.PROF_SOCIAL_ACTION_ID) });
  }

  public long getExp()
  {
    return _activeClass == null ? 0L : _activeClass.getExp();
  }

  public long getMaxExp()
  {
    return _activeClass == null ? l2p.gameserver.model.base.Experience.LEVEL[(l2p.gameserver.model.base.Experience.getMaxLevel() + 1)] : _activeClass.getMaxExp();
  }

  public void setEnchantScroll(ItemInstance scroll)
  {
    _enchantScroll = scroll;
  }

  public ItemInstance getEnchantScroll()
  {
    return _enchantScroll;
  }

  public void setFistsWeaponItem(WeaponTemplate weaponItem)
  {
    _fistsWeaponItem = weaponItem;
  }

  public WeaponTemplate getFistsWeaponItem()
  {
    return _fistsWeaponItem;
  }

  public WeaponTemplate findFistsWeaponItem(int classId)
  {
    if ((classId >= 0) && (classId <= 9)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(246);
    }

    if ((classId >= 10) && (classId <= 17)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(251);
    }

    if ((classId >= 18) && (classId <= 24)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(244);
    }

    if ((classId >= 25) && (classId <= 30)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(249);
    }

    if ((classId >= 31) && (classId <= 37)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(245);
    }

    if ((classId >= 38) && (classId <= 43)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(250);
    }

    if ((classId >= 44) && (classId <= 48)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(248);
    }

    if ((classId >= 49) && (classId <= 52)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(252);
    }

    if ((classId >= 53) && (classId <= 57)) {
      return (WeaponTemplate)ItemHolder.getInstance().getTemplate(247);
    }
    return null;
  }

  public void addExpAndCheckBonus(MonsterInstance mob, double noRateExp, double noRateSp, double partyVitalityMod)
  {
    if (_activeClass == null) {
      return;
    }

    double neededExp = calcStat(Stats.SOULS_CONSUME_EXP, 0.0D, mob, null);
    if ((neededExp > 0.0D) && (noRateExp > neededExp))
    {
      mob.broadcastPacket(new L2GameServerPacket[] { new SpawnEmitter(mob, this) });
      ThreadPoolManager.getInstance().schedule(new GameObjectTasks.SoulConsumeTask(this), 1000L);
    }

    double vitalityBonus = 0.0D;
    int npcLevel = mob.getLevel();
    if (Config.ALT_VITALITY_ENABLED)
    {
      boolean blessActive = getNevitSystem().isBlessingActive();
      vitalityBonus = mob.isRaid() ? 0.0D : getVitalityLevel(blessActive) / 2.0D;
      vitalityBonus *= Config.ALT_VITALITY_RATE;
      if (noRateExp > 0.0D)
      {
        if (!mob.isRaid())
        {
          if ((!blessActive) && ((!getVarB("NoExp")) || (getExp() != l2p.gameserver.model.base.Experience.LEVEL[(getLevel() + 1)] - 1L)))
          {
            double points = noRateExp / (npcLevel * npcLevel) * 100.0D / 9.0D;
            points *= Config.ALT_VITALITY_CONSUME_RATE;

            if (getEffectList().getEffectByType(EffectType.Vitality) != null) {
              points *= -1.0D;
            }
            setVitality(getVitality() - points * partyVitalityMod);
          }
        }
        else {
          setVitality(getVitality() + Config.ALT_VITALITY_RAID_BONUS);
        }
      }
    }

    if (!isInPeaceZone())
    {
      setRecomTimerActive(true);
      getNevitSystem().startAdventTask();
      if (getLevel() - npcLevel <= 9)
      {
        int nevitPoints = (int)Math.round(noRateExp / (npcLevel * npcLevel) * 100.0D / 20.0D);
        getNevitSystem().addPoints(nevitPoints);
      }
    }

    long normalExp = ()(noRateExp * ((Config.RATE_XP * getRateExp() + vitalityBonus) * getRecomBonusMul()));
    long normalSp = ()(noRateSp * (Config.RATE_SP * getRateSp() + vitalityBonus));

    long expWithoutBonus = ()(noRateExp * Config.RATE_XP * getRateExp());
    long spWithoutBonus = ()(noRateSp * Config.RATE_SP * getRateSp());

    addExpAndSp(normalExp, normalSp, normalExp - expWithoutBonus, normalSp - spWithoutBonus, false, true);
  }

  public void addExpAndSp(long exp, long sp)
  {
    addExpAndSp(exp, sp, 0L, 0L, false, false);
  }

  public void addExpAndSp(long addToExp, long addToSp, long bonusAddExp, long bonusAddSp, boolean applyRate, boolean applyToPet)
  {
    if (_activeClass == null) {
      return;
    }
    if (applyRate)
    {
      addToExp = ()(addToExp * (Config.RATE_XP * getRateExp()));
      addToSp = ()(addToSp * (Config.RATE_SP * getRateSp()));
    }

    Summon pet = getPet();
    if (addToExp > 0L)
    {
      if (applyToPet)
      {
        if ((pet != null) && (!pet.isDead()) && (!PetDataTable.isVitaminPet(pet.getNpcId())))
        {
          if (pet.getNpcId() == 12564)
          {
            pet.addExpAndSp(addToExp, 0L);
            addToExp = 0L;
          }
          else if ((pet.isPet()) && (pet.getExpPenalty() > 0.0D)) {
            if ((pet.getLevel() > getLevel() - 20) && (pet.getLevel() < getLevel() + 5))
            {
              pet.addExpAndSp(()(addToExp * pet.getExpPenalty()), 0L);
              addToExp = ()(addToExp * (1.0D - pet.getExpPenalty()));
            }
            else
            {
              pet.addExpAndSp(()(addToExp * pet.getExpPenalty() / 5.0D), 0L);
              addToExp = ()(addToExp * (1.0D - pet.getExpPenalty() / 5.0D));
            }
          } else if (pet.isSummon()) {
            addToExp = ()(addToExp * (1.0D - pet.getExpPenalty()));
          }
        }
      }

      if ((!isCursedWeaponEquipped()) && (addToSp > 0L) && (_karma > 0)) {
        _karma = (int)(_karma - addToSp / (Config.KARMA_SP_DIVIDER * Config.RATE_SP));
      }
      if (_karma < 0) {
        _karma = 0;
      }
      long max_xp = getVarB("NoExp") ? l2p.gameserver.model.base.Experience.LEVEL[(getLevel() + 1)] - 1L : getMaxExp();
      addToExp = Math.min(addToExp, max_xp - getExp());
    }

    int oldLvl = _activeClass.getLevel();

    _activeClass.addExp(addToExp);
    _activeClass.addSp(addToSp);

    if ((addToExp > 0L) && (addToSp > 0L) && ((bonusAddExp > 0L) || (bonusAddSp > 0L)))
      sendPacket(((SystemMessage2)((SystemMessage2)((SystemMessage2)new SystemMessage2(SystemMsg.YOU_HAVE_ACQUIRED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4).addLong(addToExp)).addLong(bonusAddExp)).addInteger(addToSp)).addInteger((int)bonusAddSp));
    else if ((addToSp > 0L) && (addToExp == 0L))
      sendPacket(new SystemMessage(331).addNumber(addToSp));
    else if ((addToSp > 0L) && (addToExp > 0L))
      sendPacket(new SystemMessage(95).addNumber(addToExp).addNumber(addToSp));
    else if ((addToSp == 0L) && (addToExp > 0L)) {
      sendPacket(new SystemMessage(45).addNumber(addToExp));
    }
    int level = _activeClass.getLevel();
    if (level != oldLvl)
    {
      int levels = level - oldLvl;
      if (levels > 0)
        getNevitSystem().addPoints(1950);
      levelSet(levels);
    }

    if ((pet != null) && (pet.isPet()) && (PetDataTable.isVitaminPet(pet.getNpcId())))
    {
      PetInstance _pet = (PetInstance)pet;
      _pet.setLevel(getLevel());
      _pet.setExp(_pet.getExpForNextLevel());
      _pet.broadcastStatusUpdate();
    }

    updateStats();
  }

  private void rewardSkills(boolean send)
  {
    boolean update = false;
    if (Config.AUTO_LEARN_SKILLS)
    {
      int unLearnable = 0;
      Collection skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
      while (skills.size() > unLearnable)
      {
        unLearnable = 0;
        for (SkillLearn s : skills)
        {
          Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
          if ((sk == null) || (!sk.getCanLearn(getClassId())) || ((!Config.AUTO_LEARN_FORGOTTEN_SKILLS) && (s.isClicked())))
          {
            unLearnable++;
            continue;
          }
          addSkill(sk, true);
        }
        skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
      }
      update = true;
    }
    else
    {
      for (SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL))
        if ((skill.getCost() == 0) && (skill.getItemId() == 0))
        {
          Skill sk = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
          addSkill(sk, true);
          if ((getAllShortCuts().size() > 0) && (sk.getLevel() > 1))
            for (ShortCut sc : getAllShortCuts())
              if ((sc.getId() == sk.getId()) && (sc.getType() == 2))
              {
                ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sk.getLevel(), 1);
                sendPacket(new ShortCutRegister(this, newsc));
                registerShortCut(newsc);
              }
          update = true;
        }
    }
    if ((send) && (update)) {
      sendPacket(new SkillList(this));
    }
    updateStats();
  }

  public Race getRace()
  {
    return getBaseTemplate().race;
  }

  public int getIntSp()
  {
    return (int)getSp();
  }

  public long getSp()
  {
    return _activeClass == null ? 0L : _activeClass.getSp();
  }

  public void setSp(long sp)
  {
    if (_activeClass != null)
      _activeClass.setSp(sp);
  }

  public int getClanId()
  {
    return _clan == null ? 0 : _clan.getClanId();
  }

  public long getLeaveClanTime()
  {
    return _leaveClanTime;
  }

  public long getDeleteClanTime()
  {
    return _deleteClanTime;
  }

  public void setLeaveClanTime(long time)
  {
    _leaveClanTime = time;
  }

  public void setDeleteClanTime(long time)
  {
    _deleteClanTime = time;
  }

  public void setOnlineTime(long time)
  {
    _onlineTime = time;
    _onlineBeginTime = System.currentTimeMillis();
  }

  public void setNoChannel(long time)
  {
    _NoChannel = time;
    if ((_NoChannel > 2145909600000L) || (_NoChannel < 0L)) {
      _NoChannel = -1L;
    }
    if (_NoChannel > 0L)
      _NoChannelBegin = System.currentTimeMillis();
    else
      _NoChannelBegin = 0L;
  }

  public long getNoChannel()
  {
    return _NoChannel;
  }

  public long getNoChannelRemained()
  {
    if (_NoChannel == 0L)
      return 0L;
    if (_NoChannel < 0L) {
      return -1L;
    }

    long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
    if (remained < 0L) {
      return 0L;
    }
    return remained;
  }

  public void setLeaveClanCurTime()
  {
    _leaveClanTime = System.currentTimeMillis();
  }

  public void setDeleteClanCurTime()
  {
    _deleteClanTime = System.currentTimeMillis();
  }

  public boolean canJoinClan()
  {
    if (_leaveClanTime == 0L)
      return true;
    if (System.currentTimeMillis() - _leaveClanTime >= 86400000L)
    {
      _leaveClanTime = 0L;
      return true;
    }
    return false;
  }

  public boolean canCreateClan()
  {
    if (_deleteClanTime == 0L)
      return true;
    if (System.currentTimeMillis() - _deleteClanTime >= 864000000L)
    {
      _deleteClanTime = 0L;
      return true;
    }
    return false;
  }

  public IStaticPacket canJoinParty(Player inviter)
  {
    Request request = getRequest();
    if ((request != null) && (request.isInProgress()) && (request.getOtherPlayer(this) != inviter))
      return SystemMsg.WAITING_FOR_ANOTHER_REPLY.packet(inviter);
    if ((isBlockAll()) || (getMessageRefusal()))
      return SystemMsg.THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE.packet(inviter);
    if (isInParty())
      return new SystemMessage2(SystemMsg.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addName(this);
    if ((inviter.getReflection() != getReflection()) && 
      (inviter.getReflection() != ReflectionManager.DEFAULT) && (getReflection() != ReflectionManager.DEFAULT))
      return SystemMsg.INVALID_TARGET.packet(inviter);
    if ((isCursedWeaponEquipped()) || (inviter.isCursedWeaponEquipped()))
      return SystemMsg.INVALID_TARGET.packet(inviter);
    if ((inviter.isInOlympiadMode()) || (isInOlympiadMode()))
      return SystemMsg.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS.packet(inviter);
    if ((!inviter.getPlayerAccess().CanJoinParty) || (!getPlayerAccess().CanJoinParty))
      return SystemMsg.INVALID_TARGET.packet(inviter);
    if (getTeam() != TeamType.NONE)
      return SystemMsg.INVALID_TARGET.packet(inviter);
    return null;
  }

  public PcInventory getInventory()
  {
    return _inventory;
  }

  public long getWearedMask()
  {
    return _inventory.getWearedMask();
  }

  public PcFreight getFreight()
  {
    return _freight;
  }

  public void removeItemFromShortCut(int objectId)
  {
    _shortCuts.deleteShortCutByObjectId(objectId);
  }

  public void removeSkillFromShortCut(int skillId)
  {
    _shortCuts.deleteShortCutBySkillId(skillId);
  }

  public boolean isSitting()
  {
    return _isSitting;
  }

  public void setSitting(boolean val)
  {
    _isSitting = val;
  }

  public boolean getSittingTask()
  {
    return sittingTaskLaunched;
  }

  public void sitDown(StaticObjectInstance throne)
  {
    if ((isSitting()) || (sittingTaskLaunched) || (isAlikeDead())) {
      return;
    }
    if ((isStunned()) || (isSleeping()) || (isParalyzed()) || (isAttackingNow()) || (isCastingNow()) || (isMoving))
    {
      getAI().setNextAction(PlayableAI.nextAction.REST, null, null, false, false);
      return;
    }

    resetWaitSitTime();
    getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);

    if (throne == null)
      broadcastPacket(new L2GameServerPacket[] { new ChangeWaitType(this, 0) });
    else {
      broadcastPacket(new L2GameServerPacket[] { new ChairSit(this, throne) });
    }
    _sittingObject = throne;
    setSitting(true);
    sittingTaskLaunched = true;
    ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndSitDownTask(this), 2500L);
  }

  public void standUp()
  {
    if ((!isSitting()) || (sittingTaskLaunched) || (isInStoreMode()) || (isAlikeDead())) {
      return;
    }

    getEffectList().stopAllSkillEffects(EffectType.Relax);

    getAI().clearNextAction();
    broadcastPacket(new L2GameServerPacket[] { new ChangeWaitType(this, 1) });

    _sittingObject = null;
    setSitting(false);
    sittingTaskLaunched = true;
    ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndStandUpTask(this), 2500L);
  }

  public void updateWaitSitTime()
  {
    if (_waitTimeWhenSit < 200)
      _waitTimeWhenSit += 2;
  }

  public int getWaitSitTime()
  {
    return _waitTimeWhenSit;
  }

  public void resetWaitSitTime()
  {
    _waitTimeWhenSit = 0;
  }

  public Warehouse getWarehouse()
  {
    return _warehouse;
  }

  public ItemContainer getRefund()
  {
    return _refund;
  }

  public long getAdena()
  {
    return getInventory().getAdena();
  }

  public boolean reduceAdena(long adena)
  {
    return reduceAdena(adena, false);
  }

  public boolean reduceAdena(long adena, boolean notify)
  {
    if (adena < 0L)
      return false;
    if (adena == 0L)
      return true;
    boolean result = getInventory().reduceAdena(adena);
    if ((notify) && (result))
      sendPacket(SystemMessage2.removeItems(57, adena));
    return result;
  }

  public ItemInstance addAdena(long adena)
  {
    return addAdena(adena, false);
  }

  public ItemInstance addAdena(long adena, boolean notify)
  {
    if (adena < 1L)
      return null;
    ItemInstance item = getInventory().addAdena(adena);
    if ((item != null) && (notify))
      sendPacket(SystemMessage2.obtainItems(57, adena, 0));
    return item;
  }

  public GameClient getNetConnection()
  {
    return _connection;
  }

  public int getRevision()
  {
    return _connection == null ? 0 : _connection.getRevision();
  }

  public void setNetConnection(GameClient connection)
  {
    _connection = connection;
  }

  public boolean isConnected()
  {
    return (_connection != null) && (_connection.isConnected());
  }

  public void onAction(Player player, boolean shift)
  {
    if (isFrozen())
    {
      player.sendPacket(ActionFail.STATIC);
      return;
    }

    if (Events.onAction(player, this, shift))
    {
      player.sendPacket(ActionFail.STATIC);
      return;
    }

    if (player.getTarget() != this)
    {
      player.setTarget(this);
      if (player.getTarget() == this)
        player.sendPacket(new MyTargetSelected(getObjectId(), 0));
      else
        player.sendPacket(ActionFail.STATIC);
    }
    else if (getPrivateStoreType() != 0)
    {
      if ((getDistance(player) > 200.0D) && (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT))
      {
        if (!shift)
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
        else
          player.sendPacket(ActionFail.STATIC);
      }
      else
        player.doInteract(this);
    }
    else if (isAutoAttackable(player)) {
      player.getAI().Attack(this, false, shift);
    } else if (player != this)
    {
      if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
      {
        if (!shift)
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Integer.valueOf(Config.FOLLOW_RANGE));
        else
          player.sendPacket(ActionFail.STATIC);
      }
      else
        player.sendPacket(ActionFail.STATIC);
    }
    else {
      player.sendPacket(ActionFail.STATIC);
    }
  }

  public void broadcastStatusUpdate()
  {
    if (!needStatusUpdate()) {
      return;
    }
    StatusUpdate su = makeStatusUpdate(new int[] { 10, 12, 34, 9, 11, 33 });
    sendPacket(su);

    if (isInParty())
    {
      getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
    }
    DuelEvent duelEvent = (DuelEvent)getEvent(DuelEvent.class);
    if (duelEvent != null) {
      duelEvent.sendPacket(new ExDuelUpdateUserInfo(this), new String[] { getTeam().revert().name() });
    }
    if ((isInOlympiadMode()) && (isOlympiadCompStart()))
    {
      if (_olympiadGame != null)
        _olympiadGame.broadcastInfo(this, null, false);
    }
  }

  public void broadcastCharInfo()
  {
    broadcastUserInfo(false);
  }

  public void broadcastUserInfo(boolean force)
  {
    sendUserInfo(force);

    if ((!isVisible()) || (isInvisible())) {
      return;
    }
    if (Config.BROADCAST_CHAR_INFO_INTERVAL == 0L) {
      force = true;
    }
    if (force)
    {
      if (_broadcastCharInfoTask != null)
      {
        _broadcastCharInfoTask.cancel(false);
        _broadcastCharInfoTask = null;
      }
      broadcastCharInfoImpl();
      return;
    }

    if (_broadcastCharInfoTask != null) {
      return;
    }
    _broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
  }

  public void setPolyId(int polyid)
  {
    _polyNpcId = polyid;

    teleToLocation(getLoc());
    broadcastUserInfo(true);
  }

  public boolean isPolymorphed()
  {
    return _polyNpcId != 0;
  }

  public int getPolyId()
  {
    return _polyNpcId;
  }

  private void broadcastCharInfoImpl()
  {
    if ((!isVisible()) || (isInvisible())) {
      return;
    }
    L2GameServerPacket ci = isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this);
    L2GameServerPacket exCi = new ExBR_ExtraUserInfo(this);
    L2GameServerPacket dominion = getEvent(DominionSiegeEvent.class) != null ? new ExDominionWarStart(this) : null;
    for (Player player : World.getAroundPlayers(this))
    {
      player.sendPacket(new IStaticPacket[] { ci, exCi });
      player.sendPacket(RelationChanged.update(player, this, player));
      if (dominion != null)
        player.sendPacket(dominion);
    }
  }

  public void broadcastRelationChanged()
  {
    if ((!isVisible()) || (isInvisible())) {
      return;
    }
    for (Player player : World.getAroundPlayers(this))
      player.sendPacket(RelationChanged.update(player, this, player));
  }

  public void sendEtcStatusUpdate()
  {
    if (!isVisible()) {
      return;
    }
    sendPacket(new EtcStatusUpdate(this));
  }

  private void sendUserInfoImpl()
  {
    sendPacket(new IStaticPacket[] { new UserInfo(this), new ExBR_ExtraUserInfo(this) });
    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)getEvent(DominionSiegeEvent.class);
    if (siegeEvent != null)
      sendPacket(new ExDominionWarStart(this));
  }

  public void sendUserInfo()
  {
    sendUserInfo(false);
  }

  public void sendUserInfo(boolean force)
  {
    if ((!isVisible()) || (entering) || (isLogoutStarted())) {
      return;
    }
    if ((Config.USER_INFO_INTERVAL == 0L) || (force))
    {
      if (_userInfoTask != null)
      {
        _userInfoTask.cancel(false);
        _userInfoTask = null;
      }
      sendUserInfoImpl();
      return;
    }

    if (_userInfoTask != null) {
      return;
    }
    _userInfoTask = ThreadPoolManager.getInstance().schedule(new UserInfoTask(null), Config.USER_INFO_INTERVAL);
  }

  public StatusUpdate makeStatusUpdate(int[] fields)
  {
    StatusUpdate su = new StatusUpdate(getObjectId());
    for (int field : fields)
      switch (field)
      {
      case 9:
        su.addAttribute(field, (int)getCurrentHp());
        break;
      case 10:
        su.addAttribute(field, getMaxHp());
        break;
      case 11:
        su.addAttribute(field, (int)getCurrentMp());
        break;
      case 12:
        su.addAttribute(field, getMaxMp());
        break;
      case 14:
        su.addAttribute(field, getCurrentLoad());
        break;
      case 15:
        su.addAttribute(field, getMaxLoad());
        break;
      case 26:
        su.addAttribute(field, _pvpFlag);
        break;
      case 27:
        su.addAttribute(field, getKarma());
        break;
      case 33:
        su.addAttribute(field, (int)getCurrentCp());
        break;
      case 34:
        su.addAttribute(field, getMaxCp());
      case 13:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 28:
      case 29:
      case 30:
      case 31:
      case 32: }  return su;
  }

  public void sendStatusUpdate(boolean broadCast, boolean withPet, int[] fields)
  {
    if ((fields.length == 0) || ((entering) && (!broadCast))) {
      return;
    }
    StatusUpdate su = makeStatusUpdate(fields);
    if (!su.hasAttributes()) {
      return;
    }
    List packets = new ArrayList(withPet ? 2 : 1);
    if ((withPet) && (getPet() != null)) {
      packets.add(getPet().makeStatusUpdate(fields));
    }
    packets.add(su);

    if (!broadCast)
      sendPacket(packets);
    else if (entering)
      broadcastPacketToOthers(packets);
    else
      broadcastPacket(packets);
  }

  public int getAllyId()
  {
    return _clan == null ? 0 : _clan.getAllyId();
  }

  public void sendPacket(IStaticPacket p)
  {
    if (!isConnected()) {
      return;
    }
    if (isPacketIgnored(p.packet(this))) {
      return;
    }
    _connection.sendPacket(p.packet(this));
  }

  public void sendPacket(IStaticPacket[] packets)
  {
    if (!isConnected()) {
      return;
    }
    for (IStaticPacket p : packets)
    {
      if (isPacketIgnored(p)) {
        continue;
      }
      _connection.sendPacket(p.packet(this));
    }
  }

  private boolean isPacketIgnored(IStaticPacket p)
  {
    if (p == null) {
      return true;
    }
    return (_notShowBuffAnim) && ((p.getClass() == MagicSkillUse.class) || (p.getClass() == MagicSkillLaunched.class));
  }

  public void sendPacket(List<? extends IStaticPacket> packets)
  {
    if (!isConnected()) {
      return;
    }
    for (IStaticPacket p : packets)
      _connection.sendPacket(p.packet(this));
  }

  public void doInteract(GameObject target)
  {
    if ((target == null) || (isActionsDisabled()))
    {
      sendActionFailed();
      return;
    }
    if (target.isPlayer())
    {
      if (target.getDistance(this) <= 200.0D)
      {
        Player temp = (Player)target;

        if ((temp.getPrivateStoreType() == 1) || (temp.getPrivateStoreType() == 8))
        {
          sendPacket(new PrivateStoreListSell(this, temp));
          sendActionFailed();
        }
        else if (temp.getPrivateStoreType() == 3)
        {
          sendPacket(new PrivateStoreListBuy(this, temp));
          sendActionFailed();
        }
        else if (temp.getPrivateStoreType() == 5)
        {
          sendPacket(new RecipeShopSellList(this, temp));
          sendActionFailed();
        }
        sendActionFailed();
      }
      else if (getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT) {
        getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
      }
    }
    else target.onAction(this, false);
  }

  public void doAutoLootOrDrop(ItemInstance item, NpcInstance fromNpc)
  {
    boolean forceAutoloot = (fromNpc.isFlying()) || (getReflection().isAutolootForced());

    if (((fromNpc.isRaid()) || ((fromNpc instanceof ReflectionBossInstance))) && (!Config.AUTO_LOOT_FROM_RAIDS) && (!item.isHerb()) && (!forceAutoloot))
    {
      item.dropToTheGround(this, fromNpc);
      return;
    }

    if (item.isHerb())
    {
      if ((!AutoLootHerbs) && (!forceAutoloot))
      {
        item.dropToTheGround(this, fromNpc);
        return;
      }
      Skill[] skills = item.getTemplate().getAttachedSkills();
      if (skills.length > 0)
        for (Skill skill : skills)
        {
          altUseSkill(skill, this);
          if ((getPet() != null) && (getPet().isSummon()) && (!getPet().isDead()))
            getPet().altUseSkill(skill, getPet());
        }
      item.deleteMe();
      return;
    }

    if ((!_autoLoot) && (!forceAutoloot))
    {
      item.dropToTheGround(this, fromNpc);
      return;
    }

    if (!isInParty())
    {
      if (!pickupItem(item, "Pickup"))
      {
        item.dropToTheGround(this, fromNpc);
        return;
      }
    }
    else {
      getParty().distributeItem(this, item, fromNpc);
    }
    broadcastPickUpMsg(item);
  }

  public void doPickupItem(GameObject object)
  {
    if (!object.isItem())
    {
      _log.warn(new StringBuilder().append("trying to pickup wrong target.").append(getTarget()).toString());
      return;
    }

    sendActionFailed();
    stopMove();

    ItemInstance item = (ItemInstance)object;

    synchronized (item)
    {
      if (!item.isVisible()) {
        return;
      }

      if (!ItemFunctions.checkIfCanPickup(this, item))
      {
        SystemMessage sm;
        if (item.getItemId() == 57)
        {
          SystemMessage sm = new SystemMessage(55);
          sm.addNumber(item.getCount());
        }
        else
        {
          sm = new SystemMessage(56);
          sm.addItemName(item.getItemId());
        }
        sendPacket(sm);
        return;
      }

      if (item.isHerb())
      {
        Skill[] skills = item.getTemplate().getAttachedSkills();
        if (skills.length > 0) {
          for (Skill skill : skills)
            altUseSkill(skill, this);
        }
        broadcastPacket(new L2GameServerPacket[] { new GetItem(item, getObjectId()) });
        item.deleteMe();
        return;
      }

      FlagItemAttachment attachment = (item.getAttachment() instanceof FlagItemAttachment) ? (FlagItemAttachment)item.getAttachment() : null;

      if ((!isInParty()) || (attachment != null))
      {
        if (pickupItem(item, "Pickup"))
        {
          broadcastPacket(new L2GameServerPacket[] { new GetItem(item, getObjectId()) });
          broadcastPickUpMsg(item);
          item.pickupMe();
        }
      }
      else
        getParty().distributeItem(this, item, null);
    }
  }

  public boolean pickupItem(ItemInstance item, String log)
  {
    PickableAttachment attachment = (item.getAttachment() instanceof PickableAttachment) ? (PickableAttachment)item.getAttachment() : null;

    if (!ItemFunctions.canAddItem(this, item)) {
      return false;
    }
    if ((item.getItemId() == 57) || (item.getItemId() == 6353))
    {
      Quest q = QuestManager.getQuest(255);
      if (q != null) {
        processQuestEvent(q.getName(), new StringBuilder().append("CE").append(item.getItemId()).toString(), null);
      }
    }
    Log.LogItem(this, log, item);
    sendPacket(SystemMessage2.obtainItems(item));
    getInventory().addItem(item);

    if (attachment != null) {
      attachment.pickUp(this);
    }
    sendChanges();
    return true;
  }

  public void setObjectTarget(GameObject target)
  {
    setTarget(target);
    if (target == null) {
      return;
    }
    if (target == getTarget())
    {
      if (target.isNpc())
      {
        NpcInstance npc = (NpcInstance)target;
        sendPacket(new MyTargetSelected(npc.getObjectId(), getLevel() - npc.getLevel()));
        sendPacket(npc.makeStatusUpdate(new int[] { 9, 10 }));
        sendPacket(new IStaticPacket[] { new ValidateLocation(npc), ActionFail.STATIC });
      }
      else {
        sendPacket(new MyTargetSelected(target.getObjectId(), 0));
      }
    }
  }

  public void setTarget(GameObject newTarget)
  {
    if ((newTarget != null) && (!newTarget.isVisible())) {
      newTarget = null;
    }

    if (((newTarget instanceof FestivalMonsterInstance)) && (!isFestivalParticipant())) {
      newTarget = null;
    }
    Party party = getParty();

    if ((party != null) && (party.isInDimensionalRift()))
    {
      int riftType = party.getDimensionalRift().getType();
      int riftRoom = party.getDimensionalRift().getCurrentRoom();
      if ((newTarget != null) && (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))) {
        newTarget = null;
      }
    }
    GameObject oldTarget = getTarget();

    if (oldTarget != null)
    {
      if (oldTarget.equals(newTarget)) {
        return;
      }

      if (oldTarget.isCreature()) {
        ((Creature)oldTarget).removeStatusListener(this);
      }
      broadcastPacket(new L2GameServerPacket[] { new TargetUnselected(this) });
    }

    if (newTarget != null)
    {
      if (newTarget.isCreature()) {
        ((Creature)newTarget).addStatusListener(this);
      }
      broadcastPacket(new L2GameServerPacket[] { new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()) });
    }

    super.setTarget(newTarget);
  }

  public ItemInstance getActiveWeaponInstance()
  {
    return getInventory().getPaperdollItem(7);
  }

  public WeaponTemplate getActiveWeaponItem()
  {
    ItemInstance weapon = getActiveWeaponInstance();

    if (weapon == null) {
      return getFistsWeaponItem();
    }
    return (WeaponTemplate)weapon.getTemplate();
  }

  public ItemInstance getSecondaryWeaponInstance()
  {
    return getInventory().getPaperdollItem(8);
  }

  public WeaponTemplate getSecondaryWeaponItem()
  {
    ItemInstance weapon = getSecondaryWeaponInstance();

    if (weapon == null) {
      return getFistsWeaponItem();
    }
    ItemTemplate item = weapon.getTemplate();

    if ((item instanceof WeaponTemplate)) {
      return (WeaponTemplate)item;
    }
    return null;
  }

  public boolean isWearingArmor(ArmorTemplate.ArmorType armorType)
  {
    ItemInstance chest = getInventory().getPaperdollItem(10);

    if (chest == null) {
      return armorType == ArmorTemplate.ArmorType.NONE;
    }
    if (chest.getItemType() != armorType) {
      return false;
    }
    if (chest.getBodyPart() == 32768) {
      return true;
    }
    ItemInstance legs = getInventory().getPaperdollItem(11);

    return armorType == ArmorTemplate.ArmorType.NONE;
  }

  public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
  {
    if ((attacker == null) || (isDead()) || ((attacker.isDead()) && (!isDot))) {
      return;
    }

    if ((attacker.isPlayer()) && (Math.abs(attacker.getLevel() - getLevel()) > 10))
    {
      if ((attacker.getKarma() > 0) && (getEffectList().getEffectsBySkillId(5182) != null) && (!isInZone(Zone.ZoneType.SIEGE))) {
        return;
      }
      if ((getKarma() > 0) && (attacker.getEffectList().getEffectsBySkillId(5182) != null) && (!attacker.isInZone(Zone.ZoneType.SIEGE))) {
        return;
      }
    }

    super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
  }

  protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
  {
    if (standUp)
    {
      standUp();
      if (isFakeDeath()) {
        breakFakeDeath();
      }
    }
    if (attacker.isPlayable())
    {
      if ((!directHp) && (getCurrentCp() > 0.0D))
      {
        double cp = getCurrentCp();
        if (cp >= damage)
        {
          cp -= damage;
          damage = 0.0D;
        }
        else
        {
          damage -= cp;
          cp = 0.0D;
        }

        setCurrentCp(cp);
      }
    }

    double hp = getCurrentHp();

    DuelEvent duelEvent = (DuelEvent)getEvent(DuelEvent.class);
    if ((duelEvent != null) && 
      (hp - damage <= 1.0D))
    {
      setCurrentHp(1.0D, false);
      duelEvent.onDie(this);
      return;
    }

    if (isInOlympiadMode())
    {
      OlympiadGame game = _olympiadGame;
      if ((this != attacker) && ((skill == null) || (skill.isOffensive()))) {
        game.addDamage(this, Math.min(hp, damage));
      }
      if (hp - damage <= 1.0D) {
        if (game.getType() != CompType.TEAM)
        {
          game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
          game.endGame(20000L, false);
          setCurrentHp(1.0D, false);
          attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
          attacker.sendActionFailed();
          return;
        }
        if (game.doDie(this))
        {
          game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
          game.endGame(20000L, false);
        }
      }
    }
    super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
  }

  private void altDeathPenalty(Creature killer)
  {
    if (!Config.ALT_GAME_DELEVEL)
      return;
    if (isInZoneBattle())
      return;
    if (getNevitSystem().isBlessingActive())
      return;
    deathPenalty(killer);
  }

  public final boolean atWarWith(Player player)
  {
    return (_clan != null) && (player.getClan() != null) && (getPledgeType() != -1) && (player.getPledgeType() != -1) && (_clan.isAtWarWith(player.getClan().getClanId()));
  }

  public boolean atMutualWarWith(Player player)
  {
    return (_clan != null) && (player.getClan() != null) && (getPledgeType() != -1) && (player.getPledgeType() != -1) && (_clan.isAtWarWith(player.getClan().getClanId())) && (player.getClan().isAtWarWith(_clan.getClanId()));
  }

  public final void doPurePk(Player killer)
  {
    int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);

    killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
    killer.setPkKills(killer.getPkKills() + 1);
  }

  public final void doKillInPeace(Player killer)
  {
    if (_karma <= 0)
      doPurePk(killer);
    else
      killer.setPvpKills(killer.getPvpKills() + 1);
  }

  public void checkAddItemToDrop(List<ItemInstance> array, List<ItemInstance> items, int maxCount)
  {
    for (int i = 0; (i < maxCount) && (!items.isEmpty()); i++)
      array.add(items.remove(Rnd.get(items.size())));
  }

  public FlagItemAttachment getActiveWeaponFlagAttachment()
  {
    ItemInstance item = getActiveWeaponInstance();
    if ((item == null) || (!(item.getAttachment() instanceof FlagItemAttachment)))
      return null;
    return (FlagItemAttachment)item.getAttachment();
  }

  protected void doPKPVPManage(Creature killer)
  {
    FlagItemAttachment attachment = getActiveWeaponFlagAttachment();
    if (attachment != null) {
      attachment.onDeath(this, killer);
    }

    if ((killer == null) || (killer == _summon) || (killer == this)) {
      return;
    }
    if (((isInZoneBattle()) || (killer.isInZoneBattle())) && (!Config.ADD_ZONE_PVP_COUNT)) {
      return;
    }
    if (((killer instanceof Summon)) && ((killer = killer.getPlayer()) == null)) {
      return;
    }

    if (killer.isPlayer())
    {
      Player pk = (Player)killer;
      int repValue = (getLevel() - pk.getLevel() >= 20 ? 2 : 1) * Config.CLAN_WAR_REP;

      boolean war = atMutualWarWith(pk);

      if ((war) && 
        (pk.getClan().getReputationScore() > 0) && (_clan.getLevel() >= 5) && (_clan.getReputationScore() > 0) && (pk.getClan().getLevel() >= 5))
      {
        _clan.broadcastToOtherOnlineMembers(new SystemMessage(1782).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
        pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1783).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
      }

      if ((isOnSiegeField()) && (!Config.ADD_SIEGE_PVP_COUNT)) {
        return;
      }
      if ((_pvpFlag > 0) || (war) || ((Config.ADD_SIEGE_PVP_COUNT) && (isOnSiegeField())) || ((Config.ADD_ZONE_PVP_COUNT) && (isInCombatZone())))
      {
        pk.setPvpKills(pk.getPvpKills() + 1);
      }

      if (Config.HONOR_SYSTEM_ENABLE)
      {
        if ((war) || (isOnSiegeField()) || ((Config.HONOR_SYSTEM__IN_PVP_ZONE) && (isInCombatZone())))
        {
          Functions.addItem(killer.getTarget().getPlayer(), Config.HONOR_SYSTEM_LOSE_ITEM_ID, Config.HONOR_SYSTEM_LOSE_ITEM_COUNT);
          Functions.addItem(killer.getPlayer(), Config.HONOR_SYSTEM_WON_ITEM_ID, Config.HONOR_SYSTEM_WON_ITEM_COUNT);
        }
        else if (_pvpFlag > 0)
        {
          Functions.addItem(killer.getPlayer(), Config.HONOR_SYSTEM_PVP_ITEM_ID, Config.HONOR_SYSTEM__PVP_ITEM_COUNT);
        }

      }
      else
      {
        doKillInPeace(pk);
      }
      pk.sendChanges();
    }

    int karma = _karma;
    decreaseKarma(Config.KARMA_LOST_BASE);

    boolean isPvP = (killer.isPlayable()) || ((killer instanceof GuardInstance));

    if (((killer.isMonster()) && (!Config.DROP_ITEMS_ON_DIE)) || ((isPvP) && ((_pkKills < Config.MIN_PK_TO_ITEMS_DROP) || ((karma == 0) && (Config.KARMA_NEEDED_TO_DROP)))) || (isFestivalParticipant()) || ((!killer.isMonster()) && (!isPvP)))
    {
      return;
    }

    if ((!Config.KARMA_DROP_GM) && (isGM())) {
      return;
    }
    int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;
    double dropRate;
    double dropRate;
    if (isPvP)
      dropRate = _pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE;
    else {
      dropRate = Config.NORMAL_DROPCHANCE_BASE;
    }
    int dropEquipCount = 0; int dropWeaponCount = 0; int dropItemCount = 0;

    for (int i = 0; (i < Math.ceil(dropRate / 100.0D)) && (i < max_drop_count); i++) {
      if (!Rnd.chance(dropRate))
        continue;
      int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
      if (rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
        dropItemCount++;
      else if (rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
        dropEquipCount++;
      else {
        dropWeaponCount++;
      }
    }
    List drop = new LazyArrayList();
    List dropItem = new LazyArrayList(); List dropEquip = new LazyArrayList(); List dropWeapon = new LazyArrayList();

    getInventory().writeLock();
    try
    {
      for (ItemInstance item : getInventory().getItems())
      {
        if ((!item.canBeDropped(this, true)) || (Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(Integer.valueOf(item.getItemId())))) {
          continue;
        }
        if (item.getTemplate().getType2() == 0)
          dropWeapon.add(item);
        else if ((item.getTemplate().getType2() == 1) || (item.getTemplate().getType2() == 2))
          dropEquip.add(item);
        else if (item.getTemplate().getType2() == 5) {
          dropItem.add(item);
        }
      }
      checkAddItemToDrop(drop, dropWeapon, dropWeaponCount);
      checkAddItemToDrop(drop, dropEquip, dropEquipCount);
      checkAddItemToDrop(drop, dropItem, dropItemCount);

      if (drop.isEmpty())
        return;
      for (ItemInstance item : drop)
      {
        if ((item.isAugmented()) && (!Config.ALT_ALLOW_DROP_AUGMENTED)) {
          item.setAugmentationId(0);
        }
        item = getInventory().removeItem(item);
        Log.LogItem(this, "PvPDrop", item);

        if (item.getEnchantLevel() > 0)
          sendPacket(new SystemMessage(375).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
        else {
          sendPacket(new SystemMessage(298).addItemName(item.getItemId()));
        }
        if ((killer.isPlayable()) && (((Config.AUTO_LOOT) && (Config.AUTO_LOOT_PK)) || (isInFlyingTransform())))
        {
          killer.getPlayer().getInventory().addItem(item);
          Log.LogItem(this, "Pickup", item);

          killer.getPlayer().sendPacket(SystemMessage2.obtainItems(item));
        }
        else {
          item.dropToTheGround(this, Location.findAroundPosition(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT));
        }
      }
    }
    finally {
      getInventory().writeUnlock();
    }
  }

  protected void onDeath(Creature killer)
  {
    getDeathPenalty().checkCharmOfLuck();

    if (isInStoreMode())
      setPrivateStoreType(0);
    if (isProcessingRequest())
    {
      Request request = getRequest();
      if (isInTrade())
      {
        Player parthner = request.getOtherPlayer(this);
        sendPacket(SendTradeDone.FAIL);
        parthner.sendPacket(SendTradeDone.FAIL);
      }
      request.cancel();
    }

    setAgathion(0);

    boolean checkPvp = true;
    if (Config.ALLOW_CURSED_WEAPONS)
    {
      if (isCursedWeaponEquipped())
      {
        CursedWeaponsManager.getInstance().dropPlayer(this);
        checkPvp = false;
      }
      else if ((killer != null) && (killer.isPlayer()) && (killer.isCursedWeaponEquipped()))
      {
        CursedWeaponsManager.getInstance().increaseKills(((Player)killer).getCursedWeaponEquippedId());
        checkPvp = false;
      }
    }

    if (checkPvp)
    {
      doPKPVPManage(killer);

      altDeathPenalty(killer);
    }

    getDeathPenalty().notifyDead(killer);

    setIncreasedForce(0);

    if ((isInParty()) && (getParty().isInReflection()) && ((getParty().getReflection() instanceof DimensionalRift))) {
      ((DimensionalRift)getParty().getReflection()).memberDead(this);
    }
    stopWaterTask();

    if ((!isSalvation()) && (isOnSiegeField()) && (isCharmOfCourage()))
    {
      ask(new ConfirmDlg(SystemMsg.YOUR_CHARM_OF_COURAGE_IS_TRYING_TO_RESURRECT_YOU, 60000), new ReviveAnswerListener(this, 100.0D, false));
      setCharmOfCourage(false);
    }

    if (getLevel() < 6)
    {
      Quest q = QuestManager.getQuest(255);
      if (q != null) {
        processQuestEvent(q.getName(), "CE30", null);
      }
    }
    super.onDeath(killer);
  }

  public void restoreExp()
  {
    restoreExp(100.0D);
  }

  public void restoreExp(double percent)
  {
    if (percent == 0.0D) {
      return;
    }
    int lostexp = 0;

    String lostexps = getVar("lostexp");
    if (lostexps != null)
    {
      lostexp = Integer.parseInt(lostexps);
      unsetVar("lostexp");
    }

    if (lostexp != 0)
      addExpAndSp(()(lostexp * percent / 100.0D), 0L);
  }

  public void deathPenalty(Creature killer)
  {
    if (killer == null)
      return;
    boolean atwar = (killer.getPlayer() != null) && (atWarWith(killer.getPlayer()));

    double deathPenaltyBonus = getDeathPenalty().getLevel() * Config.ALT_DEATH_PENALTY_C5_EXPERIENCE_PENALTY;
    if (deathPenaltyBonus < 2.0D)
      deathPenaltyBonus = 1.0D;
    else {
      deathPenaltyBonus /= 2.0D;
    }

    double percentLost = 8.0D;

    int level = getLevel();
    if (level >= 79)
      percentLost = 1.0D;
    else if (level >= 78)
      percentLost = 1.5D;
    else if (level >= 76)
      percentLost = 2.0D;
    else if (level >= 40) {
      percentLost = 4.0D;
    }
    if (Config.ALT_DEATH_PENALTY) {
      percentLost = percentLost * Config.RATE_XP + _pkKills * Config.ALT_PK_DEATH_RATE;
    }
    if ((isFestivalParticipant()) || (atwar)) {
      percentLost /= 4.0D;
    }

    int lostexp = (int)Math.round((l2p.gameserver.model.base.Experience.LEVEL[(level + 1)] - l2p.gameserver.model.base.Experience.LEVEL[level]) * percentLost / 100.0D);
    lostexp = (int)(lostexp * deathPenaltyBonus);

    lostexp = (int)calcStat(Stats.EXP_LOST, lostexp, killer, null);

    if (isOnSiegeField())
    {
      SiegeEvent siegeEvent = (SiegeEvent)getEvent(SiegeEvent.class);
      if (siegeEvent != null) {
        lostexp = 0;
      }
      if (siegeEvent != null)
      {
        List effect = getEffectList().getEffectsBySkillId(5660);
        if (effect != null)
        {
          int syndromeLvl = ((Effect)effect.get(0)).getSkill().getLevel();
          if (syndromeLvl < 5)
          {
            getEffectList().stopEffect(5660);
            Skill skill = SkillTable.getInstance().getInfo(5660, syndromeLvl + 1);
            skill.getEffects(this, this, false, false);
          }
          else if (syndromeLvl == 5)
          {
            getEffectList().stopEffect(5660);
            Skill skill = SkillTable.getInstance().getInfo(5660, 5);
            skill.getEffects(this, this, false, false);
          }
        }
        else
        {
          Skill skill = SkillTable.getInstance().getInfo(5660, 1);
          if (skill != null) {
            skill.getEffects(this, this, false, false);
          }
        }
      }
    }
    long before = getExp();
    addExpAndSp(-lostexp, 0L);
    long lost = before - getExp();

    if (lost > 0L)
      setVar("lostexp", String.valueOf(lost), -1L);
  }

  public void setRequest(Request transaction)
  {
    _request = transaction;
  }

  public Request getRequest()
  {
    return _request;
  }

  public boolean isBusy()
  {
    return (isProcessingRequest()) || (isOutOfControl()) || (isInOlympiadMode()) || (getTeam() != TeamType.NONE) || (isInStoreMode()) || (isInDuel()) || (getMessageRefusal()) || (isBlockAll()) || (isInvisible());
  }

  public boolean isProcessingRequest()
  {
    if (_request == null) {
      return false;
    }
    return _request.isInProgress();
  }

  public boolean isInTrade()
  {
    return (isProcessingRequest()) && (getRequest().isTypeOf(Request.L2RequestType.TRADE));
  }

  public List<L2GameServerPacket> addVisibleObject(GameObject object, Creature dropper)
  {
    if ((isLogoutStarted()) || (object == null) || (object.getObjectId() == getObjectId()) || (!object.isVisible())) {
      return Collections.emptyList();
    }
    return object.addPacketList(this, dropper);
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    if ((isInvisible()) && (forPlayer.getObjectId() != getObjectId())) {
      return Collections.emptyList();
    }
    if ((getPrivateStoreType() != 0) && (forPlayer.getVarB("notraders"))) {
      return Collections.emptyList();
    }

    if ((isInObserverMode()) && (getCurrentRegion() != getObserverRegion()) && (getObserverRegion() == forPlayer.getCurrentRegion())) {
      return Collections.emptyList();
    }
    List list = new ArrayList();
    if (forPlayer.getObjectId() != getObjectId()) {
      list.add(isPolymorphed() ? new NpcInfoPoly(this) : new CharInfo(this));
    }
    list.add(new ExBR_ExtraUserInfo(this));

    if ((isSitting()) && (_sittingObject != null)) {
      list.add(new ChairSit(this, _sittingObject));
    }
    if (getPrivateStoreType() != 0)
    {
      if (getPrivateStoreType() == 3)
        list.add(new PrivateStoreMsgBuy(this));
      else if ((getPrivateStoreType() == 1) || (getPrivateStoreType() == 8))
        list.add(new PrivateStoreMsgSell(this));
      else if (getPrivateStoreType() == 5)
        list.add(new RecipeShopMsg(this));
      if (forPlayer.isInZonePeace()) {
        return list;
      }
    }
    if (isCastingNow())
    {
      Creature castingTarget = getCastingTarget();
      Skill castingSkill = getCastingSkill();
      long animationEndTime = getAnimationEndTime();
      if ((castingSkill != null) && (castingTarget != null) && (castingTarget.isCreature()) && (getAnimationEndTime() > 0L)) {
        list.add(new MagicSkillUse(this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int)(animationEndTime - System.currentTimeMillis()), 0L));
      }
    }
    if (isInCombat()) {
      list.add(new AutoAttackStart(getObjectId()));
    }
    list.add(RelationChanged.update(forPlayer, this, forPlayer));
    DominionSiegeEvent dominionSiegeEvent = (DominionSiegeEvent)getEvent(DominionSiegeEvent.class);
    if (dominionSiegeEvent != null) {
      list.add(new ExDominionWarStart(this));
    }
    if (isInBoat()) {
      list.add(getBoat().getOnPacket(this, getInBoatPosition()));
    }
    else if ((isMoving) || (isFollow)) {
      list.add(movePacket());
    }
    return list;
  }

  public List<L2GameServerPacket> removeVisibleObject(GameObject object, List<L2GameServerPacket> list)
  {
    if ((isLogoutStarted()) || (object == null) || (object.getObjectId() == getObjectId())) {
      return null;
    }
    List result = list == null ? object.deletePacketList() : list;

    getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
    return result;
  }

  private void levelSet(int levels)
  {
    if (levels > 0)
    {
      sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
      broadcastPacket(new L2GameServerPacket[] { new SocialAction(getObjectId(), 2122) });

      setCurrentHpMp(getMaxHp(), getMaxMp());
      setCurrentCp(getMaxCp());

      Quest q = QuestManager.getQuest(255);
      if (q != null)
        processQuestEvent(q.getName(), "CE40", null);
    }
    else if ((levels < 0) && 
      (Config.ALT_REMOVE_SKILLS_ON_DELEVEL)) {
      checkSkills();
    }

    if (isInParty()) {
      getParty().recalculatePartyData();
    }
    if (_clan != null) {
      _clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new PledgeShowMemberListUpdate(this) });
    }
    if (_matchingRoom != null) {
      _matchingRoom.broadcastPlayerUpdate(this);
    }

    rewardSkills(true);
  }

  public void checkSkills()
  {
    for (Skill sk : getAllSkillsArray())
      SkillTreeTable.checkSkill(this, sk);
  }

  public void startTimers()
  {
    startAutoSaveTask();
    startPcBangPointsTask();
    startBonusTask();
    getInventory().startTimers();
    resumeQuestTimers();
  }

  public void stopAllTimers()
  {
    setAgathion(0);
    stopWaterTask();
    stopBonusTask();
    stopHourlyTask();
    stopKickTask();
    stopVitalityTask();
    stopPcBangPointsTask();
    stopAutoSaveTask();
    stopRecomBonusTask(true);
    getInventory().stopAllTimers();
    stopQuestTimers();
    getNevitSystem().stopTasksOnLogout();
  }

  public Summon getPet()
  {
    return _summon;
  }

  public void setPet(Summon summon)
  {
    boolean isPet = false;
    if ((_summon != null) && (_summon.isPet()))
      isPet = true;
    unsetVar("pet");
    _summon = summon;
    autoShot();
    if (summon == null)
    {
      if (isPet)
      {
        if ((isLogoutStarted()) && 
          (getPetControlItem() != null))
          setVar("pet", String.valueOf(getPetControlItem().getObjectId()), -1L);
        setPetControlItem(null);
      }
      getEffectList().stopEffect(4140);
    }
  }

  public void scheduleDelete()
  {
    long time = 0L;

    if (Config.SERVICES_ENABLE_NO_CARRIER) {
      time = NumberUtils.toInt(getVar("noCarrier"), Config.SERVICES_NO_CARRIER_DEFAULT_TIME);
    }
    scheduleDelete(time * 1000L);
  }

  public void scheduleDelete(long time)
  {
    if ((isLogoutStarted()) || (isInOfflineMode())) {
      return;
    }
    broadcastCharInfo();

    ThreadPoolManager.getInstance().schedule(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        if (!isConnected())
        {
          Player.this.prepareToLogout();
          deleteMe();
        }
      }
    }
    , time);
  }

  protected void onDelete()
  {
    super.onDelete();

    WorldRegion observerRegion = getObserverRegion();
    if (observerRegion != null) {
      observerRegion.removeObject(this);
    }

    _friendList.notifyFriends(false);

    bookmarks.clear();

    _inventory.clear();
    _warehouse.clear();
    _summon = null;
    _arrowItem = null;
    _fistsWeaponItem = null;
    _chars = null;
    _enchantScroll = null;
    _lastNpc = HardReferences.emptyRef();
    _observerRegion = null;
  }

  public void setTradeList(List<TradeItem> list)
  {
    _tradeList = list;
  }

  public List<TradeItem> getTradeList()
  {
    return _tradeList;
  }

  public String getSellStoreName()
  {
    return _sellStoreName;
  }

  public void setSellStoreName(String name)
  {
    _sellStoreName = Strings.stripToSingleLine(name);
  }

  public void setSellList(boolean packageSell, List<TradeItem> list)
  {
    if (packageSell)
      _packageSellList = list;
    else
      _sellList = list;
  }

  public List<TradeItem> getSellList()
  {
    return getSellList(_privatestore == 8);
  }

  public List<TradeItem> getSellList(boolean packageSell)
  {
    return packageSell ? _packageSellList : _sellList;
  }

  public String getBuyStoreName()
  {
    return _buyStoreName;
  }

  public void setBuyStoreName(String name)
  {
    _buyStoreName = Strings.stripToSingleLine(name);
  }

  public void setBuyList(List<TradeItem> list)
  {
    _buyList = list;
  }

  public List<TradeItem> getBuyList()
  {
    return _buyList;
  }

  public void setManufactureName(String name)
  {
    _manufactureName = Strings.stripToSingleLine(name);
  }

  public String getManufactureName()
  {
    return _manufactureName;
  }

  public List<ManufactureItem> getCreateList()
  {
    return _createList;
  }

  public void setCreateList(List<ManufactureItem> list)
  {
    _createList = list;
  }

  public void setPrivateStoreType(int type)
  {
    _privatestore = type;
    if (type != 0)
      setVar("storemode", String.valueOf(type), -1L);
    else
      unsetVar("storemode");
  }

  public boolean isInStoreMode()
  {
    return _privatestore != 0;
  }

  public int getPrivateStoreType()
  {
    return _privatestore;
  }

  public void setClan(Clan clan)
  {
    if ((_clan != clan) && (_clan != null)) {
      unsetVar("canWhWithdraw");
    }
    Clan oldClan = _clan;
    if ((oldClan != null) && (clan == null)) {
      for (Skill skill : oldClan.getAllSkills())
        removeSkill(skill, false);
    }
    _clan = clan;

    if (clan == null)
    {
      _pledgeType = -128;
      _pledgeClass = 0;
      _powerGrade = 0;
      _apprentice = 0;
      getInventory().validateItems();
      return;
    }

    if (!clan.isAnyMember(getObjectId()))
    {
      setClan(null);
      if (!isNoble())
        setTitle("");
    }
  }

  public Clan getClan()
  {
    return _clan;
  }

  public SubUnit getSubUnit()
  {
    return _clan == null ? null : _clan.getSubUnit(_pledgeType);
  }

  public ClanHall getClanHall()
  {
    int id = _clan != null ? _clan.getHasHideout() : 0;
    return (ClanHall)ResidenceHolder.getInstance().getResidence(ClanHall.class, id);
  }

  public Castle getCastle()
  {
    int id = _clan != null ? _clan.getCastle() : 0;
    return (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, id);
  }

  public Fortress getFortress()
  {
    int id = _clan != null ? _clan.getHasFortress() : 0;
    return (Fortress)ResidenceHolder.getInstance().getResidence(Fortress.class, id);
  }

  public Alliance getAlliance()
  {
    return _clan == null ? null : _clan.getAlliance();
  }

  public boolean isClanLeader()
  {
    return (_clan != null) && (getObjectId() == _clan.getLeaderId());
  }

  public boolean isAllyLeader()
  {
    return (getAlliance() != null) && (getAlliance().getLeader().getLeaderId() == getObjectId());
  }

  public void reduceArrowCount()
  {
    sendPacket(SystemMsg.YOU_CAREFULLY_NOCK_AN_ARROW);

    if (Config.ADD_REDUCE_ARROW)
    {
      if (!getInventory().destroyItemByObjectId(getInventory().getPaperdollObjectId(8), 1L))
      {
        getInventory().setPaperdollItem(8, null);
        _arrowItem = null;
      }
    }
  }

  protected boolean checkAndEquipArrows()
  {
    if (getInventory().getPaperdollItem(8) == null)
    {
      ItemInstance activeWeapon = getActiveWeaponInstance();
      if (activeWeapon != null)
      {
        if (activeWeapon.getItemType() == WeaponTemplate.WeaponType.BOW)
          _arrowItem = getInventory().findArrowForBow(activeWeapon.getTemplate());
        else if (activeWeapon.getItemType() == WeaponTemplate.WeaponType.CROSSBOW) {
          getInventory().findArrowForCrossbow(activeWeapon.getTemplate());
        }
      }

      if (_arrowItem != null)
        getInventory().setPaperdollItem(8, _arrowItem);
    }
    else
    {
      _arrowItem = getInventory().getPaperdollItem(8);
    }
    return _arrowItem != null;
  }

  public void setUptime(long time)
  {
    _uptime = time;
  }

  public long getUptime()
  {
    return System.currentTimeMillis() - _uptime;
  }

  public boolean isInParty()
  {
    return _party != null;
  }

  public void setParty(Party party)
  {
    _party = party;
  }

  public void joinParty(Party party)
  {
    if (party != null)
      party.addPartyMember(this);
  }

  public void leaveParty()
  {
    if (isInParty())
      _party.removePartyMember(this, false);
  }

  public Party getParty()
  {
    return _party;
  }

  public void setLastPartyPosition(Location loc)
  {
    _lastPartyPosition = loc;
  }

  public Location getLastPartyPosition()
  {
    return _lastPartyPosition;
  }

  public boolean isGM()
  {
    return _playerAccess == null ? false : _playerAccess.IsGM;
  }

  public void setAccessLevel(int level)
  {
    _accessLevel = level;
  }

  public int getAccessLevel()
  {
    return _accessLevel;
  }

  public void setPlayerAccess(PlayerAccess pa)
  {
    if (pa != null)
      _playerAccess = pa;
    else {
      _playerAccess = new PlayerAccess();
    }
    setAccessLevel((isGM()) || (_playerAccess.Menu) ? 100 : 0);
  }

  public PlayerAccess getPlayerAccess()
  {
    return _playerAccess;
  }

  public double getLevelMod()
  {
    return (89.0D + getLevel()) / 100.0D;
  }

  public void updateStats()
  {
    if ((entering) || (isLogoutStarted())) {
      return;
    }
    refreshOverloaded();
    if (!Config.ADD_EXPERTISE_PENALTY)
    {
      refreshExpertisePenalty();
    }
    super.updateStats();
  }

  public void sendChanges()
  {
    if ((entering) || (isLogoutStarted()))
      return;
    super.sendChanges();
  }

  public void updateKarma(boolean flagChanged)
  {
    sendStatusUpdate(true, true, new int[] { 27 });
    if (flagChanged)
      broadcastRelationChanged();
  }

  public boolean isOnline()
  {
    return _isOnline;
  }

  public void setIsOnline(boolean isOnline)
  {
    _isOnline = isOnline;
  }

  public void setOnlineStatus(boolean isOnline)
  {
    _isOnline = isOnline;
    updateOnlineStatus();
  }

  private void updateOnlineStatus()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
      statement.setInt(1, (isOnline()) && (!isInOfflineMode()) ? 1 : 0);
      statement.setLong(2, System.currentTimeMillis() / 1000L);
      statement.setInt(3, getObjectId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void increaseKarma(long add_karma)
  {
    boolean flagChanged = _karma == 0;
    long new_karma = _karma + add_karma;

    if (new_karma > 2147483647L) {
      new_karma = 2147483647L;
    }
    if ((_karma == 0) && (new_karma > 0L))
    {
      if (_pvpFlag > 0)
      {
        _pvpFlag = 0;
        if (_PvPRegTask != null)
        {
          _PvPRegTask.cancel(true);
          _PvPRegTask = null;
        }
        sendStatusUpdate(true, true, new int[] { 26 });
      }

      _karma = (int)new_karma;
    }
    else {
      _karma = (int)new_karma;
    }
    updateKarma(flagChanged);
  }

  public void decreaseKarma(int i)
  {
    boolean flagChanged = _karma > 0;
    _karma -= i;
    if (_karma <= 0)
    {
      _karma = 0;
      updateKarma(flagChanged);
    }
    else {
      updateKarma(false);
    }
  }

  public static Player create(int classId, int sex, String accountName, String name, int hairStyle, int hairColor, int face)
  {
    PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, sex != 0);

    Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName);

    player.setName(name);
    player.setTitle("");
    player.setHairStyle(hairStyle);
    player.setHairColor(hairColor);
    player.setFace(face);
    player.setCreateTime(System.currentTimeMillis());

    if (!CharacterDAO.getInstance().insert(player)) {
      return null;
    }
    return player;
  }

  public static Player restore(int objectId)
  {
    Player player = null;
    Connection con = null;
    Statement statement = null;
    Statement statement2 = null;
    PreparedStatement statement3 = null;
    ResultSet rset = null;
    ResultSet rset2 = null;
    ResultSet rset3 = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.createStatement();
      statement2 = con.createStatement();
      rset = statement.executeQuery(new StringBuilder().append("SELECT * FROM `characters` WHERE `obj_Id`=").append(objectId).append(" LIMIT 1").toString());
      rset2 = statement2.executeQuery(new StringBuilder().append("SELECT `class_id` FROM `character_subclasses` WHERE `char_obj_id`=").append(objectId).append(" AND `isBase`=1 LIMIT 1").toString());

      if ((rset.next()) && (rset2.next()))
      {
        int classId = rset2.getInt("class_id");
        boolean female = rset.getInt("sex") == 1;
        PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, female);

        player = new Player(objectId, template);

        player.loadVariables();
        player.loadInstanceReuses();
        player.loadPremiumItemList();
        player.bookmarks.setCapacity(rset.getInt("bookmarks"));
        player.bookmarks.restore();
        player._friendList.restore();
        player._postFriends = CharacterPostFriendDAO.getInstance().select(player);
        CharacterGroupReuseDAO.getInstance().select(player);

        player.setBaseClass(classId);
        player._login = rset.getString("account_name");
        player.setName(rset.getString("char_name"));

        player.setFace(rset.getInt("face"));
        player.setHairStyle(rset.getInt("hairStyle"));
        player.setHairColor(rset.getInt("hairColor"));
        player.setHeading(0);

        player.setKarma(rset.getInt("karma"));
        player.setPvpKills(rset.getInt("pvpkills"));
        player.setPkKills(rset.getInt("pkkills"));
        player.setLeaveClanTime(rset.getLong("leaveclan") * 1000L);
        if ((player.getLeaveClanTime() > 0L) && (player.canJoinClan()))
          player.setLeaveClanTime(0L);
        player.setDeleteClanTime(rset.getLong("deleteclan") * 1000L);
        if ((player.getDeleteClanTime() > 0L) && (player.canCreateClan())) {
          player.setDeleteClanTime(0L);
        }
        player.setNoChannel(rset.getLong("nochannel") * 1000L);
        if ((player.getNoChannel() > 0L) && (player.getNoChannelRemained() < 0L)) {
          player.setNoChannel(0L);
        }
        player.setOnlineTime(rset.getLong("onlinetime") * 1000L);

        int clanId = rset.getInt("clanid");
        if (clanId > 0)
        {
          player.setClan(ClanTable.getInstance().getClan(clanId));
          player.setPledgeType(rset.getInt("pledge_type"));
          player.setPowerGrade(rset.getInt("pledge_rank"));
          player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
          player.setApprentice(rset.getInt("apprentice"));
        }

        player.setCreateTime(rset.getLong("createtime") * 1000L);
        player.setDeleteTimer(rset.getInt("deletetime"));

        player.setTitle(rset.getString("title"));

        if (player.getVar("titlecolor") != null) {
          player.setTitleColor(Integer.decode(new StringBuilder().append("0x").append(player.getVar("titlecolor")).toString()).intValue());
        }
        if (player.getVar("namecolor") == null) {
          if (player.isGM())
            player.setNameColor(Config.GM_NAME_COLOUR);
          else if ((player.getClan() != null) && (player.getClan().getLeaderId() == player.getObjectId()))
            player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
          else
            player.setNameColor(Config.NORMAL_NAME_COLOUR);
        }
        else player.setNameColor(Integer.decode(new StringBuilder().append("0x").append(player.getVar("namecolor")).toString()).intValue());

        if (Config.AUTO_LOOT_INDIVIDUAL)
        {
          player._autoLoot = player.getVarB("AutoLoot", Config.AUTO_LOOT);
          player.AutoLootHerbs = player.getVarB("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
        }

        player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
        player.setUptime(System.currentTimeMillis());
        player.setLastAccess(rset.getLong("lastAccess"));

        player.setRecomHave(rset.getInt("rec_have"));
        player.setRecomLeft(rset.getInt("rec_left"));
        player.setRecomBonusTime(rset.getInt("rec_bonus_time"));

        if (player.getVar("recLeftToday") != null)
          player.setRecomLeftToday(Integer.parseInt(player.getVar("recLeftToday")));
        else {
          player.setRecomLeftToday(0);
        }
        player.getNevitSystem().setPoints(rset.getInt("hunt_points"), rset.getInt("hunt_time"));

        player.setKeyBindings(rset.getBytes("key_bindings"));
        player.setPcBangPoints(rset.getInt("pcBangPoints"));

        player.setFame(rset.getInt("fame"), null);

        player.restoreRecipeBook();

        if (Config.ENABLE_OLYMPIAD)
        {
          player.setHero(Hero.getInstance().isHero(player.getObjectId()));
          player.setNoble(Olympiad.isNoble(player.getObjectId()));
        }

        player.updatePledgeClass();

        int reflection = 0;

        if ((player.getVar("jailed") != null) && (System.currentTimeMillis() / 1000L < Integer.parseInt(player.getVar("jailed")) + 60))
        {
          player.setXYZ(-114648, -249384, -2984);

          player.sitDown(null);
          player.block();
          player._unjailTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.UnJailTask(player), Integer.parseInt(player.getVar("jailed")) * 1000L);
        }
        else
        {
          player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
          String ref = player.getVar("reflection");
          if (ref != null)
          {
            reflection = Integer.parseInt(ref);
            if (reflection > 0)
            {
              String back = player.getVar("backCoords");
              if (back != null)
              {
                player.setLoc(Location.parseLoc(back));
                player.unsetVar("backCoords");
              }
              reflection = 0;
            }
          }
        }

        player.setReflection(reflection);

        EventHolder.getInstance().findEvent(player);

        Quest.restoreQuestStates(player);

        player.getInventory().restore();

        restoreCharSubClasses(player);

        player.setVitality(rset.getInt("vitality") + (int)((System.currentTimeMillis() / 1000L - rset.getLong("lastAccess")) / 15.0D));
        try
        {
          String var = player.getVar("ExpandInventory");
          if (var != null)
            player.setExpandInventory(Integer.parseInt(var));
        }
        catch (Exception e)
        {
          _log.error("", e);
        }

        try
        {
          String var = player.getVar("ExpandWarehouse");
          if (var != null)
            player.setExpandWarehouse(Integer.parseInt(var));
        }
        catch (Exception e)
        {
          _log.error("", e);
        }

        try
        {
          String var = player.getVar("notShowBuffAnim");
          if (var != null)
            player.setNotShowBuffAnim(Boolean.parseBoolean(var));
        }
        catch (Exception e)
        {
          _log.error("", e);
        }

        try
        {
          String var = player.getVar("notraders");
          if (var != null)
            player.setNotShowTraders(Boolean.parseBoolean(var));
        }
        catch (Exception e)
        {
          _log.error("", e);
        }

        try
        {
          String var = player.getVar("pet");
          if (var != null)
            player.setPetControlItem(Integer.parseInt(var));
        }
        catch (Exception e)
        {
          _log.error("", e);
        }

        statement3 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
        statement3.setString(1, player._login);
        statement3.setInt(2, objectId);
        rset3 = statement3.executeQuery();
        while (rset3.next())
        {
          Integer charId = Integer.valueOf(rset3.getInt("obj_Id"));
          String charName = rset3.getString("char_name");
          player._chars.put(charId, charName);
        }

        DbUtils.close(statement3, rset3);

        LazyArrayList zones = LazyArrayList.newInstance();

        World.getZones(zones, player.getLoc(), player.getReflection());

        if (!zones.isEmpty()) {
          for (Zone zone : zones)
            if (zone.getType() == Zone.ZoneType.no_restart)
            {
              if (System.currentTimeMillis() / 1000L - player.getLastAccess() > zone.getRestartTime())
              {
                player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.EnterWorld.TeleportedReasonNoRestart", player, new Object[0]));
                player.setLoc(TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE));
              }
            }
            else if (zone.getType() == Zone.ZoneType.SIEGE)
            {
              SiegeEvent siegeEvent = (SiegeEvent)player.getEvent(SiegeEvent.class);
              if (siegeEvent != null) {
                player.setLoc(siegeEvent.getEnterLoc(player));
              }
              else {
                Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
                player.setLoc(r.getNotOwnerRestartPoint(player));
              }
            }
        }
        LazyArrayList.recycle(zones);

        if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getLoc(), false)) {
          player.setLoc(DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords());
        }

        player.restoreBlockList();
        player._macroses.restore();

        player.refreshExpertisePenalty();
        player.refreshOverloaded();

        player.getWarehouse().restore();
        player.getFreight().restore();

        player.restoreTradeList();
        if (player.getVar("storemode") != null)
        {
          player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
          player.setSitting(true);
        }

        player.updateKetraVarka();
        player.updateRam();
        player.checkRecom();
      }
    }
    catch (Exception e)
    {
      _log.error("Could not restore char data!", e);
    }
    finally
    {
      DbUtils.closeQuietly(statement2, rset2);
      DbUtils.closeQuietly(statement3, rset3);
      DbUtils.closeQuietly(con, statement, rset);
    }
    return player;
  }

  private void loadPremiumItemList()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
      statement.setInt(1, getObjectId());
      rs = statement.executeQuery();
      while (rs.next())
      {
        int itemNum = rs.getInt("itemNum");
        int itemId = rs.getInt("itemId");
        long itemCount = rs.getLong("itemCount");
        String itemSender = rs.getString("itemSender");
        PremiumItem item = new PremiumItem(itemId, itemCount, itemSender);
        _premiumItems.put(Integer.valueOf(itemNum), item);
      }
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rs);
    }
  }

  public void updatePremiumItem(int itemNum, long newcount)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=?");
      statement.setLong(1, newcount);
      statement.setInt(2, getObjectId());
      statement.setInt(3, itemNum);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void deletePremiumItem(int itemNum)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, itemNum);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public Map<Integer, PremiumItem> getPremiumItemList()
  {
    return _premiumItems;
  }

  public void store(boolean fast)
  {
    if (!_storeLock.tryLock()) {
      return;
    }
    try
    {
      Connection con = null;
      PreparedStatement statement = null;
      try
      {
        con = DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("UPDATE characters SET face=?,hairStyle=?,hairColor=?,x=?,y=?,z=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunt_points=?,hunt_time=?,clanid=?,deletetime=?,title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?,onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,vitality=?,fame=?,bookmarks=? WHERE obj_Id=? LIMIT 1");

        statement.setInt(1, getFace());
        statement.setInt(2, getHairStyle());
        statement.setInt(3, getHairColor());
        if (_stablePoint == null)
        {
          statement.setInt(4, getX());
          statement.setInt(5, getY());
          statement.setInt(6, getZ());
        }
        else
        {
          statement.setInt(4, _stablePoint.x);
          statement.setInt(5, _stablePoint.y);
          statement.setInt(6, _stablePoint.z);
        }
        statement.setInt(7, getKarma());
        statement.setInt(8, getPvpKills());
        statement.setInt(9, getPkKills());
        statement.setInt(10, getRecomHave());
        statement.setInt(11, getRecomLeft());
        statement.setInt(12, getRecomBonusTime());
        statement.setInt(13, getNevitSystem().getPoints());
        statement.setInt(14, getNevitSystem().getTime());
        statement.setInt(15, getClanId());
        statement.setInt(16, getDeleteTimer());
        statement.setString(17, _title);
        statement.setInt(18, _accessLevel);
        statement.setInt(19, (isOnline()) && (!isInOfflineMode()) ? 1 : 0);
        statement.setLong(20, getLeaveClanTime() / 1000L);
        statement.setLong(21, getDeleteClanTime() / 1000L);
        statement.setLong(22, _NoChannel > 0L ? getNoChannelRemained() / 1000L : _NoChannel);
        statement.setInt(23, (int)(_onlineBeginTime > 0L ? (_onlineTime + System.currentTimeMillis() - _onlineBeginTime) / 1000L : _onlineTime / 1000L));
        statement.setInt(24, getPledgeType());
        statement.setInt(25, getPowerGrade());
        statement.setInt(26, getLvlJoinedAcademy());
        statement.setInt(27, getApprentice());
        statement.setBytes(28, getKeyBindings());
        statement.setInt(29, getPcBangPoints());
        statement.setString(30, getName());
        statement.setInt(31, (int)getVitality());
        statement.setInt(32, getFame());
        statement.setInt(33, bookmarks.getCapacity());
        statement.setInt(34, getObjectId());

        statement.executeUpdate();
        GameStats.increaseUpdatePlayerBase();

        if (!fast)
        {
          EffectsDAO.getInstance().insert(this);
          CharacterGroupReuseDAO.getInstance().insert(this);
          storeDisableSkills();
          storeBlockList();
        }

        storeCharSubClasses();
        bookmarks.store();
      }
      catch (Exception e)
      {
        _log.error(new StringBuilder().append("Could not store char data: ").append(this).append("!").toString(), e);
      }
      finally
      {
        DbUtils.closeQuietly(con, statement);
      }
    }
    finally
    {
      _storeLock.unlock();
    }
  }

  public Skill addSkill(Skill newSkill, boolean store)
  {
    if (newSkill == null) {
      return null;
    }

    Skill oldSkill = super.addSkill(newSkill);

    if (newSkill.equals(oldSkill)) {
      return oldSkill;
    }

    if (store) {
      storeSkill(newSkill, oldSkill);
    }
    return oldSkill;
  }

  public Skill removeSkill(Skill skill, boolean fromDB)
  {
    if (skill == null)
      return null;
    return removeSkill(skill.getId(), fromDB);
  }

  public Skill removeSkill(int id, boolean fromDB)
  {
    Skill oldSkill = super.removeSkillById(Integer.valueOf(id));

    if (!fromDB) {
      return oldSkill;
    }
    if (oldSkill != null)
    {
      Connection con = null;
      PreparedStatement statement = null;
      try
      {
        con = DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
        statement.setInt(1, oldSkill.getId());
        statement.setInt(2, getObjectId());
        statement.setInt(3, getActiveClassId());
        statement.execute();
      }
      catch (Exception e)
      {
        _log.error("Could not delete skill!", e);
      }
      finally
      {
        DbUtils.closeQuietly(con, statement);
      }
    }

    return oldSkill;
  }

  private void storeSkill(Skill newSkill, Skill oldSkill)
  {
    if (newSkill == null)
    {
      _log.warn("could not store new skill. its NULL");
      return;
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newSkill.getId());
      statement.setInt(3, newSkill.getLevel());
      statement.setInt(4, getActiveClassId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("Error could not store skills!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  private void restoreSkills()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getActiveClassId());
      rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("skill_id");
        int level = rset.getInt("skill_level");

        Skill skill = SkillTable.getInstance().getInfo(id, level);

        if (skill == null)
        {
          continue;
        }
        if ((!isGM()) && (!SkillAcquireHolder.getInstance().isSkillPossible(this, skill)))
        {
          removeSkill(skill, true);
          removeSkillFromShortCut(skill.getId());

          continue;
        }

        super.addSkill(skill);
      }

      if (isNoble()) {
        updateNobleSkills();
      }

      if ((_hero) && (getBaseClassId() == getActiveClassId())) {
        Hero.addSkills(this);
      }

      if (_clan != null)
      {
        _clan.addSkillsQuietly(this);

        if ((_clan.getLeaderId() == getObjectId()) && (_clan.getLevel() >= 5)) {
          SiegeUtils.addSiegeSkills(this);
        }
      }

      if (((getActiveClassId() >= 53) && (getActiveClassId() <= 57)) || (getActiveClassId() == 117) || (getActiveClassId() == 118)) {
        super.addSkill(SkillTable.getInstance().getInfo(1321, 1));
      }
      super.addSkill(SkillTable.getInstance().getInfo(1322, 1));

      if ((Config.UNSTUCK_SKILL) && (getSkillLevel(Integer.valueOf(1050)) < 0))
        super.addSkill(SkillTable.getInstance().getInfo(2099, 1));
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Could not restore skills for player objId: ").append(getObjectId()).toString());
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public void storeDisableSkills()
  {
    Connection con = null;
    Statement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.createStatement();
      statement.executeUpdate(new StringBuilder().append("DELETE FROM character_skills_save WHERE char_obj_id = ").append(getObjectId()).append(" AND class_index=").append(getActiveClassId()).append(" AND `end_time` < ").append(System.currentTimeMillis()).toString());

      if (_skillReuses.isEmpty())
        return;
      SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`skill_level`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
      synchronized (_skillReuses)
      {
        for (TimeStamp timeStamp : _skillReuses.values())
        {
          if (timeStamp.hasNotPassed())
          {
            StringBuilder sb = new StringBuilder("(");
            sb.append(getObjectId()).append(",");
            sb.append(timeStamp.getId()).append(",");
            sb.append(timeStamp.getLevel()).append(",");
            sb.append(getActiveClassId()).append(",");
            sb.append(timeStamp.getEndTime()).append(",");
            sb.append(timeStamp.getReuseBasic()).append(")");
            b.write(sb.toString());
          }
        }
      }
      if (!b.isEmpty())
        statement.executeUpdate(b.close());
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Could not store disable skills data: ").append(e).toString());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void restoreDisableSkills()
  {
    _skillReuses.clear();

    Connection con = null;
    Statement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.createStatement();
      rset = statement.executeQuery(new StringBuilder().append("SELECT skill_id,skill_level,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=").append(getObjectId()).append(" AND class_index=").append(getActiveClassId()).toString());
      while (rset.next())
      {
        int skillId = rset.getInt("skill_id");
        int skillLevel = rset.getInt("skill_level");
        long endTime = rset.getLong("end_time");
        long rDelayOrg = rset.getLong("reuse_delay_org");
        long curTime = System.currentTimeMillis();

        Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

        if ((skill != null) && (endTime - curTime > 500L))
          _skillReuses.put(skill.hashCode(), new TimeStamp(skill, endTime, rDelayOrg));
      }
      DbUtils.close(statement);

      statement = con.createStatement();
      statement.executeUpdate(new StringBuilder().append("DELETE FROM character_skills_save WHERE char_obj_id = ").append(getObjectId()).append(" AND class_index=").append(getActiveClassId()).append(" AND `end_time` < ").append(System.currentTimeMillis()).toString());
    }
    catch (Exception e)
    {
      _log.error("Could not restore active skills data!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  private void restoreHenna()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getActiveClassId());
      rset = statement.executeQuery();

      for (int i = 0; i < 3; i++) {
        _henna[i] = null;
      }
      while (rset.next())
      {
        int slot = rset.getInt("slot");
        if ((slot < 1) || (slot > 3)) {
          continue;
        }
        int symbol_id = rset.getInt("symbol_id");

        if (symbol_id != 0)
        {
          Henna tpl = HennaHolder.getInstance().getHenna(symbol_id);
          if (tpl != null)
          {
            _henna[(slot - 1)] = tpl;
          }
        }
      }
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("could not restore henna: ").append(e).toString());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    recalcHennaStats();
  }

  public int getHennaEmptySlots()
  {
    int totalSlots = 1 + getClassId().level();
    for (int i = 0; i < 3; i++) {
      if (_henna[i] != null)
        totalSlots--;
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
    Henna henna = _henna[slot];
    int dyeID = henna.getDyeId();

    _henna[slot] = null;

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, slot + 1);
      statement.setInt(3, getActiveClassId());
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("could not remove char henna: ").append(e).toString(), e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    recalcHennaStats();

    sendPacket(new HennaInfo(this));

    sendUserInfo(true);

    ItemFunctions.addItem(this, dyeID, henna.getDrawCount() / 2L, true);

    return true;
  }

  public boolean addHenna(Henna henna)
  {
    if (getHennaEmptySlots() == 0)
    {
      sendPacket(SystemMsg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
      return false;
    }

    for (int i = 0; i < 3; i++) {
      if (_henna[i] != null)
        continue;
      _henna[i] = henna;

      recalcHennaStats();

      Connection con = null;
      PreparedStatement statement = null;
      try
      {
        con = DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
        statement.setInt(1, getObjectId());
        statement.setInt(2, henna.getSymbolId());
        statement.setInt(3, i + 1);
        statement.setInt(4, getActiveClassId());
        statement.execute();
      }
      catch (Exception e)
      {
        _log.warn(new StringBuilder().append("could not save char henna: ").append(e).toString());
      }
      finally
      {
        DbUtils.closeQuietly(con, statement);
      }

      sendPacket(new HennaInfo(this));
      sendUserInfo(true);

      return true;
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

    for (int i = 0; i < 3; i++)
    {
      Henna henna = _henna[i];
      if (henna == null)
        continue;
      if (!henna.isForThisClass(this)) {
        continue;
      }
      _hennaINT += henna.getStatINT();
      _hennaSTR += henna.getStatSTR();
      _hennaMEN += henna.getStatMEN();
      _hennaCON += henna.getStatCON();
      _hennaWIT += henna.getStatWIT();
      _hennaDEX += henna.getStatDEX();
    }

    if (_hennaINT > 5)
      _hennaINT = 5;
    if (_hennaSTR > 5)
      _hennaSTR = 5;
    if (_hennaMEN > 5)
      _hennaMEN = 5;
    if (_hennaCON > 5)
      _hennaCON = 5;
    if (_hennaWIT > 5)
      _hennaWIT = 5;
    if (_hennaDEX > 5)
      _hennaDEX = 5;
  }

  public Henna getHenna(int slot)
  {
    if ((slot < 1) || (slot > 3))
      return null;
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

  public boolean consumeItem(int itemConsumeId, long itemCount)
  {
    if (Config.ADD_CONSAMBLE_LIST.length > 0)
    {
      boolean destroy = true;
      for (int i = 0; i < Config.ADD_CONSAMBLE_LIST.length; i++)
      {
        if (itemConsumeId != Config.ADD_CONSAMBLE_LIST[i])
          continue;
        destroy = false;
        break;
      }

      if (destroy)
      {
        if (getInventory().destroyItemByItemId(itemConsumeId, itemCount))
        {
          sendPacket(SystemMessage2.removeItems(itemConsumeId, itemCount));
          return true;
        }
      }
    }

    if (getInventory().destroyItemByItemId(itemConsumeId, itemCount))
    {
      sendPacket(SystemMessage2.removeItems(itemConsumeId, itemCount));
      return true;
    }

    return false;
  }

  public boolean consumeItemMp(int itemId, int mp)
  {
    for (ItemInstance item : getInventory().getPaperdollItems()) {
      if ((item == null) || (item.getItemId() != itemId))
        continue;
      int newMp = item.getLifeTime() - mp;
      if (newMp < 0)
        break;
      item.setLifeTime(newMp);
      sendPacket(new InventoryUpdate().addModifiedItem(item));
      return true;
    }

    return false;
  }

  public boolean isMageClass()
  {
    return _template.baseMAtk > 3;
  }

  public boolean isMounted()
  {
    return _mountNpcId > 0;
  }

  public final boolean isRiding()
  {
    return _riding;
  }

  public final void setRiding(boolean mode)
  {
    _riding = mode;
  }

  public boolean checkLandingState()
  {
    if (isInZone(Zone.ZoneType.no_landing)) {
      return false;
    }
    SiegeEvent siege = (SiegeEvent)getEvent(SiegeEvent.class);
    if (siege != null)
    {
      Residence unit = siege.getResidence();

      return (unit != null) && (getClan() != null) && (isClanLeader()) && ((getClan().getCastle() == unit.getId()) || (getClan().getHasFortress() == unit.getId()));
    }

    return true;
  }

  public void setMount(int npcId, int obj_id, int level)
  {
    if (isCursedWeaponEquipped()) {
      return;
    }
    switch (npcId)
    {
    case 0:
      setFlying(false);
      setRiding(false);
      if (getTransformation() > 0)
        setTransformation(0);
      removeSkillById(Integer.valueOf(325));
      removeSkillById(Integer.valueOf(4289));
      getEffectList().stopEffect(4258);
      break;
    case 12526:
    case 12527:
    case 12528:
    case 16038:
    case 16039:
    case 16040:
    case 16068:
      setRiding(true);
      if (!isNoble()) break;
      addSkill(SkillTable.getInstance().getInfo(325, 1), false); break;
    case 12621:
      setFlying(true);
      setLoc(getLoc().changeZ(32));
      addSkill(SkillTable.getInstance().getInfo(4289, 1), false);
      break;
    case 16037:
    case 16041:
    case 16042:
      setRiding(true);
    }

    if (npcId > 0) {
      unEquipWeapon();
    }
    _mountNpcId = npcId;
    _mountObjId = obj_id;
    _mountLevel = level;

    broadcastUserInfo(true);
    broadcastPacket(new L2GameServerPacket[] { new Ride(this) });
    broadcastUserInfo(true);

    sendPacket(new SkillList(this));
  }

  public void unEquipWeapon()
  {
    ItemInstance wpn = getSecondaryWeaponInstance();
    if (wpn != null)
    {
      sendDisarmMessage(wpn);
      getInventory().unEquipItem(wpn);
    }

    wpn = getActiveWeaponInstance();
    if (wpn != null)
    {
      sendDisarmMessage(wpn);
      getInventory().unEquipItem(wpn);
    }

    abortAttack(true, true);
    abortCast(true, true);
  }

  public int getSpeed(int baseSpeed)
  {
    if (isMounted())
    {
      PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
      int speed = 187;
      if (petData != null)
        speed = petData.getSpeed();
      double mod = 1.0D;
      int level = getLevel();
      if ((_mountLevel > level) && (level - _mountLevel > 10))
        mod = 0.5D;
      baseSpeed = (int)(mod * speed);
    }
    return super.getSpeed(baseSpeed);
  }

  public int getMountNpcId()
  {
    return _mountNpcId;
  }

  public int getMountObjId()
  {
    return _mountObjId;
  }

  public int getMountLevel()
  {
    return _mountLevel;
  }

  public void sendDisarmMessage(ItemInstance wpn)
  {
    if (wpn.getEnchantLevel() > 0)
    {
      SystemMessage sm = new SystemMessage(1064);
      sm.addNumber(wpn.getEnchantLevel());
      sm.addItemName(wpn.getItemId());
      sendPacket(sm);
    }
    else
    {
      SystemMessage sm = new SystemMessage(417);
      sm.addItemName(wpn.getItemId());
      sendPacket(sm);
    }
  }

  public void setUsingWarehouseType(Warehouse.WarehouseType type)
  {
    _usingWHType = type;
  }

  public Warehouse.WarehouseType getUsingWarehouseType()
  {
    return _usingWHType;
  }

  public Collection<EffectCubic> getCubics()
  {
    return _cubics == null ? Collections.emptyList() : _cubics.values();
  }

  public void addCubic(EffectCubic cubic)
  {
    if (_cubics == null)
      _cubics = new ConcurrentHashMap(3);
    _cubics.put(Integer.valueOf(cubic.getId()), cubic);
  }

  public void removeCubic(int id)
  {
    if (_cubics != null)
      _cubics.remove(Integer.valueOf(id));
  }

  public EffectCubic getCubic(int id)
  {
    return _cubics == null ? null : (EffectCubic)_cubics.get(Integer.valueOf(id));
  }

  public String toString()
  {
    return new StringBuilder().append(getName()).append("[").append(getObjectId()).append("]").toString();
  }

  public int getEnchantEffect()
  {
    ItemInstance wpn = getActiveWeaponInstance();

    if (wpn == null) {
      return 0;
    }
    return Math.min(127, wpn.getEnchantLevel());
  }

  public void setLastNpc(NpcInstance npc)
  {
    if (npc == null)
      _lastNpc = HardReferences.emptyRef();
    else
      _lastNpc = npc.getRef();
  }

  public NpcInstance getLastNpc()
  {
    return (NpcInstance)_lastNpc.get();
  }

  public void setMultisell(MultiSellHolder.MultiSellListContainer multisell)
  {
    _multisell = multisell;
  }

  public MultiSellHolder.MultiSellListContainer getMultisell()
  {
    return _multisell;
  }

  public boolean isFestivalParticipant()
  {
    return getReflection() instanceof DarknessFestival;
  }

  public boolean unChargeShots(boolean spirit)
  {
    ItemInstance weapon = getActiveWeaponInstance();
    if (weapon == null) {
      return false;
    }
    if (spirit)
      weapon.setChargedSpiritshot(0);
    else {
      weapon.setChargedSoulshot(0);
    }
    autoShot();
    return true;
  }

  public boolean unChargeFishShot()
  {
    ItemInstance weapon = getActiveWeaponInstance();
    if (weapon == null)
      return false;
    weapon.setChargedFishshot(false);
    autoShot();
    return true;
  }

  public void autoShot()
  {
    for (Integer shotId : _activeSoulShots)
    {
      ItemInstance item = getInventory().getItemByItemId(shotId.intValue());
      if (item == null)
      {
        removeAutoSoulShot(shotId);
        continue;
      }
      IItemHandler handler = item.getTemplate().getHandler();
      if (handler == null)
        continue;
      handler.useItem(this, item, false);
    }
  }

  public boolean getChargedFishShot()
  {
    ItemInstance weapon = getActiveWeaponInstance();
    return (weapon != null) && (weapon.getChargedFishshot());
  }

  public boolean getChargedSoulShot()
  {
    ItemInstance weapon = getActiveWeaponInstance();
    return (weapon != null) && (weapon.getChargedSoulshot() == 1);
  }

  public int getChargedSpiritShot()
  {
    ItemInstance weapon = getActiveWeaponInstance();
    if (weapon == null)
      return 0;
    return weapon.getChargedSpiritshot();
  }

  public void addAutoSoulShot(Integer itemId)
  {
    _activeSoulShots.add(itemId);
  }

  public void removeAutoSoulShot(Integer itemId)
  {
    _activeSoulShots.remove(itemId);
  }

  public Set<Integer> getAutoSoulShot()
  {
    return _activeSoulShots;
  }

  public void setInvisibleType(InvisibleType vis)
  {
    _invisibleType = vis;
  }

  public InvisibleType getInvisibleType()
  {
    return _invisibleType;
  }

  public int getClanPrivileges()
  {
    if (_clan == null)
      return 0;
    if (isClanLeader())
      return 16777214;
    if ((_powerGrade < 1) || (_powerGrade > 9))
      return 0;
    RankPrivs privs = _clan.getRankPrivs(_powerGrade);
    if (privs != null)
      return privs.getPrivs();
    return 0;
  }

  public void teleToClosestTown()
  {
    teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_VILLAGE), ReflectionManager.DEFAULT);
  }

  public void teleToCastle()
  {
    teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CASTLE), ReflectionManager.DEFAULT);
  }

  public void teleToFortress()
  {
    teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_FORTRESS), ReflectionManager.DEFAULT);
  }

  public void teleToClanhall()
  {
    teleToLocation(TeleportUtils.getRestartLocation(this, RestartType.TO_CLANHALL), ReflectionManager.DEFAULT);
  }

  public void sendMessage(CustomMessage message)
  {
    sendMessage(message.toString());
  }

  public void teleToLocation(int x, int y, int z, int refId)
  {
    if (isDeleted()) {
      return;
    }
    super.teleToLocation(x, y, z, refId);
  }

  public boolean onTeleported()
  {
    if (!super.onTeleported()) {
      return false;
    }
    if (isFakeDeath()) {
      breakFakeDeath();
    }
    if (isInBoat()) {
      setLoc(getBoat().getLoc());
    }

    setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);

    spawnMe();

    setLastClientPosition(getLoc());
    setLastServerPosition(getLoc());

    if (isPendingRevive()) {
      doRevive();
    }
    sendActionFailed();

    getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);

    if ((isLockedTarget()) && (getTarget() != null)) {
      sendPacket(new MyTargetSelected(getTarget().getObjectId(), 0));
    }
    sendUserInfo(true);
    if (getPet() != null) {
      getPet().teleportToOwner();
    }
    return true;
  }

  public boolean enterObserverMode(Location loc)
  {
    WorldRegion observerRegion = World.getRegion(loc);
    if (observerRegion == null)
      return false;
    if (!_observerMode.compareAndSet(0, 1)) {
      return false;
    }
    setTarget(null);
    stopMove();
    sitDown(null);
    setFlying(true);

    World.removeObjectsFromPlayer(this);

    setObserverRegion(observerRegion);

    broadcastCharInfo();

    sendPacket(new ObserverStart(loc));

    return true;
  }

  public void appearObserverMode()
  {
    if (!_observerMode.compareAndSet(1, 3)) {
      return;
    }
    WorldRegion currentRegion = getCurrentRegion();
    WorldRegion observerRegion = getObserverRegion();

    if (!observerRegion.equals(currentRegion)) {
      observerRegion.addObject(this);
    }
    World.showObjectsToPlayer(this);

    OlympiadGame game = getOlympiadObserveGame();
    if (game != null)
    {
      game.addSpectator(this);
      game.broadcastInfo(null, this, true);
    }
  }

  public void leaveObserverMode()
  {
    if (!_observerMode.compareAndSet(3, 2)) {
      return;
    }
    WorldRegion currentRegion = getCurrentRegion();
    WorldRegion observerRegion = getObserverRegion();

    if (!observerRegion.equals(currentRegion)) {
      observerRegion.removeObject(this);
    }

    World.removeObjectsFromPlayer(this);

    setObserverRegion(null);

    setTarget(null);
    stopMove();

    sendPacket(new ObserverEnd(getLoc()));
  }

  public void returnFromObserverMode()
  {
    if (!_observerMode.compareAndSet(2, 0)) {
      return;
    }

    setLastClientPosition(null);
    setLastServerPosition(null);

    unblock();
    standUp();
    setFlying(false);

    broadcastCharInfo();

    World.showObjectsToPlayer(this);
  }

  public void enterOlympiadObserverMode(Location loc, OlympiadGame game, Reflection reflect)
  {
    WorldRegion observerRegion = World.getRegion(loc);
    if (observerRegion == null) {
      return;
    }
    OlympiadGame oldGame = getOlympiadObserveGame();
    if (!_observerMode.compareAndSet(oldGame != null ? 3 : 0, 1)) {
      return;
    }
    setTarget(null);
    stopMove();

    World.removeObjectsFromPlayer(this);
    setObserverRegion(observerRegion);

    if (oldGame != null)
    {
      oldGame.removeSpectator(this);
      sendPacket(ExOlympiadMatchEnd.STATIC);
    }
    else
    {
      block();

      broadcastCharInfo();

      sendPacket(new ExOlympiadMode(3));
    }

    setOlympiadObserveGame(game);

    setReflection(reflect);
    sendPacket(new TeleportToLocation(this, loc));
  }

  public void leaveOlympiadObserverMode(boolean removeFromGame)
  {
    OlympiadGame game = getOlympiadObserveGame();
    if (game == null)
      return;
    if (!_observerMode.compareAndSet(3, 2)) {
      return;
    }
    if (removeFromGame)
      game.removeSpectator(this);
    setOlympiadObserveGame(null);

    WorldRegion currentRegion = getCurrentRegion();
    WorldRegion observerRegion = getObserverRegion();

    if ((observerRegion != null) && (currentRegion != null) && (!observerRegion.equals(currentRegion))) {
      observerRegion.removeObject(this);
    }

    World.removeObjectsFromPlayer(this);

    setObserverRegion(null);

    setTarget(null);
    stopMove();

    sendPacket(new ExOlympiadMode(0));
    sendPacket(ExOlympiadMatchEnd.STATIC);

    setReflection(ReflectionManager.DEFAULT);

    sendPacket(new TeleportToLocation(this, getLoc()));
  }

  public void setOlympiadSide(int i)
  {
    _olympiadSide = i;
  }

  public int getOlympiadSide()
  {
    return _olympiadSide;
  }

  public boolean isInObserverMode()
  {
    return _observerMode.get() > 0;
  }

  public int getObserverMode()
  {
    return _observerMode.get();
  }

  public WorldRegion getObserverRegion()
  {
    return _observerRegion;
  }

  public void setObserverRegion(WorldRegion region)
  {
    _observerRegion = region;
  }

  public int getTeleMode()
  {
    return _telemode;
  }

  public void setTeleMode(int mode)
  {
    _telemode = mode;
  }

  public void setLoto(int i, int val)
  {
    _loto[i] = val;
  }

  public int getLoto(int i)
  {
    return _loto[i];
  }

  public void setRace(int i, int val)
  {
    _race[i] = val;
  }

  public int getRace(int i)
  {
    return _race[i];
  }

  public boolean getMessageRefusal()
  {
    return _messageRefusal;
  }

  public void setMessageRefusal(boolean mode)
  {
    _messageRefusal = mode;
  }

  public void setTradeRefusal(boolean mode)
  {
    _tradeRefusal = mode;
  }

  public boolean getTradeRefusal()
  {
    return _tradeRefusal;
  }

  public void addToBlockList(String charName)
  {
    if ((charName == null) || (charName.equalsIgnoreCase(getName())) || (isInBlockList(charName)))
    {
      sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
      return;
    }

    Player block_target = World.getPlayer(charName);

    if (block_target != null)
    {
      if (block_target.isGM())
      {
        sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
        return;
      }
      _blockList.put(Integer.valueOf(block_target.getObjectId()), block_target.getName());
      sendPacket(new SystemMessage(617).addString(block_target.getName()));
      block_target.sendPacket(new SystemMessage(619).addString(getName()));
      return;
    }

    int charId = CharacterDAO.getInstance().getObjectIdByName(charName);

    if (charId == 0)
    {
      sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
      return;
    }

    if ((Config.gmlist.containsKey(Integer.valueOf(charId))) && (((PlayerAccess)Config.gmlist.get(Integer.valueOf(charId))).IsGM))
    {
      sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
      return;
    }
    _blockList.put(Integer.valueOf(charId), charName);
    sendPacket(new SystemMessage(617).addString(charName));
  }

  public void removeFromBlockList(String charName)
  {
    int charId = 0;
    for (Iterator i$ = _blockList.keySet().iterator(); i$.hasNext(); ) { int blockId = ((Integer)i$.next()).intValue();
      if (charName.equalsIgnoreCase((String)_blockList.get(Integer.valueOf(blockId))))
      {
        charId = blockId;
        break;
      } }
    if (charId == 0)
    {
      sendPacket(Msg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_IGNORE_LIST);
      return;
    }
    sendPacket(new SystemMessage(618).addString((String)_blockList.remove(Integer.valueOf(charId))));
    Player block_target = GameObjectsStorage.getPlayer(charId);
    if (block_target != null)
      block_target.sendMessage(new StringBuilder().append(getName()).append(" has removed you from his/her Ignore List.").toString());
  }

  public boolean isInBlockList(Player player)
  {
    return isInBlockList(player.getObjectId());
  }

  public boolean isInBlockList(int charId)
  {
    return (_blockList != null) && (_blockList.containsKey(Integer.valueOf(charId)));
  }

  public boolean isInBlockList(String charName)
  {
    for (Iterator i$ = _blockList.keySet().iterator(); i$.hasNext(); ) { int blockId = ((Integer)i$.next()).intValue();
      if (charName.equalsIgnoreCase((String)_blockList.get(Integer.valueOf(blockId))))
        return true; }
    return false;
  }

  private void restoreBlockList()
  {
    _blockList.clear();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT target_Id, char_name FROM character_blocklist LEFT JOIN characters ON ( character_blocklist.target_Id = characters.obj_Id ) WHERE character_blocklist.obj_Id = ?");
      statement.setInt(1, getObjectId());
      rs = statement.executeQuery();
      while (rs.next())
      {
        int targetId = rs.getInt("target_Id");
        String name = rs.getString("char_name");
        if (name == null)
          continue;
        _blockList.put(Integer.valueOf(targetId), name);
      }
    }
    catch (SQLException e)
    {
      _log.warn(new StringBuilder().append("Can't restore player blocklist ").append(e).toString(), e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rs);
    }
  }

  private void storeBlockList()
  {
    Connection con = null;
    Statement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.createStatement();
      statement.executeUpdate(new StringBuilder().append("DELETE FROM character_blocklist WHERE obj_Id=").append(getObjectId()).toString());

      if (_blockList.isEmpty())
        return;
      SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`) VALUES");

      synchronized (_blockList)
      {
        for (Map.Entry e : _blockList.entrySet())
        {
          StringBuilder sb = new StringBuilder("(");
          sb.append(getObjectId()).append(",");
          sb.append(e.getKey()).append(")");
          b.write(sb.toString());
        }
      }
      if (!b.isEmpty())
        statement.executeUpdate(b.close());
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Can't store player blocklist ").append(e).toString());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public boolean isBlockAll()
  {
    return _blockAll;
  }

  public void setBlockAll(boolean state)
  {
    _blockAll = state;
  }

  public Collection<String> getBlockList()
  {
    return _blockList.values();
  }

  public Map<Integer, String> getBlockListMap()
  {
    return _blockList;
  }

  public void setHero(boolean hero)
  {
    _hero = hero;
  }

  public boolean isHero()
  {
    return _hero;
  }

  public void setIsInOlympiadMode(boolean b)
  {
    _inOlympiadMode = b;
  }

  public boolean isInOlympiadMode()
  {
    return _inOlympiadMode;
  }

  public boolean isOlympiadGameStart()
  {
    return (_olympiadGame != null) && (_olympiadGame.getState() == 1);
  }

  public boolean isOlympiadCompStart()
  {
    return (_olympiadGame != null) && (_olympiadGame.getState() == 2);
  }

  public void updateNobleSkills()
  {
    if (isNoble())
    {
      if ((isClanLeader()) && (getClan().getCastle() > 0))
        super.addSkill(SkillTable.getInstance().getInfo(327, 1));
      super.addSkill(SkillTable.getInstance().getInfo(1323, 1));
      super.addSkill(SkillTable.getInstance().getInfo(1324, 1));
      super.addSkill(SkillTable.getInstance().getInfo(1325, 1));
      super.addSkill(SkillTable.getInstance().getInfo(1326, 1));
      super.addSkill(SkillTable.getInstance().getInfo(1327, 1));
    }
    else
    {
      super.removeSkillById(Integer.valueOf(327));
      super.removeSkillById(Integer.valueOf(1323));
      super.removeSkillById(Integer.valueOf(1324));
      super.removeSkillById(Integer.valueOf(1325));
      super.removeSkillById(Integer.valueOf(1326));
      super.removeSkillById(Integer.valueOf(1327));
    }
  }

  public void setNoble(boolean noble)
  {
    if (noble)
      broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(this, this, 6673, 1, 1000, 0L) });
    _noble = noble;

    broadcastPacket(new L2GameServerPacket[] { new SocialAction(getObjectId(), 3) });
  }

  public boolean isNoble()
  {
    return _noble;
  }

  public int getSubLevel()
  {
    return isSubClassActive() ? getLevel() : 0;
  }

  public void updateKetraVarka()
  {
    if (ItemFunctions.getItemCount(this, 7215) > 0L) {
      _ketra = 5;
    } else if (ItemFunctions.getItemCount(this, 7214) > 0L) {
      _ketra = 4;
    } else if (ItemFunctions.getItemCount(this, 7213) > 0L) {
      _ketra = 3;
    } else if (ItemFunctions.getItemCount(this, 7212) > 0L) {
      _ketra = 2;
    } else if (ItemFunctions.getItemCount(this, 7211) > 0L) {
      _ketra = 1;
    } else if (ItemFunctions.getItemCount(this, 7225) > 0L) {
      _varka = 5;
    } else if (ItemFunctions.getItemCount(this, 7224) > 0L) {
      _varka = 4;
    } else if (ItemFunctions.getItemCount(this, 7223) > 0L) {
      _varka = 3;
    } else if (ItemFunctions.getItemCount(this, 7222) > 0L) {
      _varka = 2;
    } else if (ItemFunctions.getItemCount(this, 7221) > 0L) {
      _varka = 1;
    }
    else {
      _varka = 0;
      _ketra = 0;
    }
  }

  public int getVarka()
  {
    return _varka;
  }

  public int getKetra()
  {
    return _ketra;
  }

  public void updateRam()
  {
    if (ItemFunctions.getItemCount(this, 7247) > 0L)
      _ram = 2;
    else if (ItemFunctions.getItemCount(this, 7246) > 0L)
      _ram = 1;
    else
      _ram = 0;
  }

  public int getRam()
  {
    return _ram;
  }

  public void setPledgeType(int typeId)
  {
    _pledgeType = typeId;
  }

  public int getPledgeType()
  {
    return _pledgeType;
  }

  public void setLvlJoinedAcademy(int lvl)
  {
    _lvlJoinedAcademy = lvl;
  }

  public int getLvlJoinedAcademy()
  {
    return _lvlJoinedAcademy;
  }

  public int getPledgeClass()
  {
    return _pledgeClass;
  }

  public void updatePledgeClass()
  {
    int CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
    boolean IN_ACADEMY = (_clan != null) && (Clan.isAcademy(_pledgeType));
    boolean IS_GUARD = (_clan != null) && (Clan.isRoyalGuard(_pledgeType));
    boolean IS_KNIGHT = (_clan != null) && (Clan.isOrderOfKnights(_pledgeType));

    boolean IS_GUARD_CAPTAIN = false; boolean IS_KNIGHT_COMMANDER = false; boolean IS_LEADER = false;

    SubUnit unit = getSubUnit();
    if (unit != null)
    {
      UnitMember unitMember = unit.getUnitMember(getObjectId());
      if (unitMember == null)
      {
        _log.warn(new StringBuilder().append("Player: unitMember null, clan: ").append(_clan.getClanId()).append("; pledgeType: ").append(unit.getType()).toString());
        return;
      }
      IS_GUARD_CAPTAIN = Clan.isRoyalGuard(unitMember.getLeaderOf());
      IS_KNIGHT_COMMANDER = Clan.isOrderOfKnights(unitMember.getLeaderOf());
      IS_LEADER = unitMember.getLeaderOf() == 0;
    }

    switch (CLAN_LEVEL)
    {
    case -1:
      _pledgeClass = 0;
      break;
    case 0:
    case 1:
    case 2:
    case 3:
      if (IS_LEADER)
        _pledgeClass = 2;
      else
        _pledgeClass = 1;
      break;
    case 4:
      if (IS_LEADER)
        _pledgeClass = 3;
      else
        _pledgeClass = 2;
      break;
    case 5:
      if (IS_LEADER)
        _pledgeClass = 4;
      else if (IN_ACADEMY)
        _pledgeClass = 1;
      else
        _pledgeClass = 2;
      break;
    case 6:
      if (IS_LEADER)
        _pledgeClass = 5;
      else if (IN_ACADEMY)
        _pledgeClass = 1;
      else if (IS_GUARD_CAPTAIN)
        _pledgeClass = 4;
      else if (IS_GUARD)
        _pledgeClass = 2;
      else
        _pledgeClass = 3;
      break;
    case 7:
      if (IS_LEADER)
        _pledgeClass = 7;
      else if (IN_ACADEMY)
        _pledgeClass = 1;
      else if (IS_GUARD_CAPTAIN)
        _pledgeClass = 6;
      else if (IS_GUARD)
        _pledgeClass = 3;
      else if (IS_KNIGHT_COMMANDER)
        _pledgeClass = 5;
      else if (IS_KNIGHT)
        _pledgeClass = 2;
      else
        _pledgeClass = 4;
      break;
    case 8:
      if (IS_LEADER)
        _pledgeClass = 8;
      else if (IN_ACADEMY)
        _pledgeClass = 1;
      else if (IS_GUARD_CAPTAIN)
        _pledgeClass = 7;
      else if (IS_GUARD)
        _pledgeClass = 4;
      else if (IS_KNIGHT_COMMANDER)
        _pledgeClass = 6;
      else if (IS_KNIGHT)
        _pledgeClass = 3;
      else
        _pledgeClass = 5;
      break;
    case 9:
      if (IS_LEADER)
        _pledgeClass = 9;
      else if (IN_ACADEMY)
        _pledgeClass = 1;
      else if (IS_GUARD_CAPTAIN)
        _pledgeClass = 8;
      else if (IS_GUARD)
        _pledgeClass = 5;
      else if (IS_KNIGHT_COMMANDER)
        _pledgeClass = 7;
      else if (IS_KNIGHT)
        _pledgeClass = 4;
      else
        _pledgeClass = 6;
      break;
    case 10:
      if (IS_LEADER)
        _pledgeClass = 10;
      else if (IN_ACADEMY)
        _pledgeClass = 1;
      else if (IS_GUARD)
        _pledgeClass = 6;
      else if (IS_KNIGHT)
        _pledgeClass = 5;
      else if (IS_GUARD_CAPTAIN)
        _pledgeClass = 9;
      else if (IS_KNIGHT_COMMANDER)
        _pledgeClass = 8;
      else
        _pledgeClass = 7;
      break;
    case 11:
      if (IS_LEADER)
        _pledgeClass = 11;
      else if (IN_ACADEMY)
        _pledgeClass = 1;
      else if (IS_GUARD)
        _pledgeClass = 7;
      else if (IS_KNIGHT)
        _pledgeClass = 6;
      else if (IS_GUARD_CAPTAIN)
        _pledgeClass = 10;
      else if (IS_KNIGHT_COMMANDER)
        _pledgeClass = 9;
      else {
        _pledgeClass = 8;
      }
    }

    if ((_hero) && (_pledgeClass < 8))
      _pledgeClass = 8;
    else if ((_noble) && (_pledgeClass < 5))
      _pledgeClass = 5;
  }

  public void setPowerGrade(int grade)
  {
    _powerGrade = grade;
  }

  public int getPowerGrade()
  {
    return _powerGrade;
  }

  public void setApprentice(int apprentice)
  {
    _apprentice = apprentice;
  }

  public int getApprentice()
  {
    return _apprentice;
  }

  public int getSponsor()
  {
    return _clan == null ? 0 : _clan.getAnyMember(getObjectId()).getSponsor();
  }

  public int getNameColor()
  {
    if (isInObserverMode()) {
      return Color.black.getRGB();
    }
    return _nameColor;
  }

  public void setNameColor(int nameColor)
  {
    if ((nameColor != Config.NORMAL_NAME_COLOUR) && (nameColor != Config.CLANLEADER_NAME_COLOUR) && (nameColor != Config.GM_NAME_COLOUR) && (nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR))
      setVar("namecolor", Integer.toHexString(nameColor), -1L);
    else if (nameColor == Config.NORMAL_NAME_COLOUR)
      unsetVar("namecolor");
    _nameColor = nameColor;
  }

  public void setNameColor(int red, int green, int blue)
  {
    _nameColor = ((red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16));
    if ((_nameColor != Config.NORMAL_NAME_COLOUR) && (_nameColor != Config.CLANLEADER_NAME_COLOUR) && (_nameColor != Config.GM_NAME_COLOUR) && (_nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR))
      setVar("namecolor", Integer.toHexString(_nameColor), -1L);
    else
      unsetVar("namecolor");
  }

  public void setVar(String name, String value, long expirationTime)
  {
    user_variables.put(name, value);
    mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,?)", new Object[] { Integer.valueOf(getObjectId()), name, value, Long.valueOf(expirationTime) });
  }

  public void setVar(String name, int value, long expirationTime)
  {
    setVar(name, String.valueOf(value), expirationTime);
  }

  public void setVar(String name, long value, long expirationTime)
  {
    setVar(name, String.valueOf(value), expirationTime);
  }

  public void unsetVar(String name)
  {
    if (name == null) {
      return;
    }
    if (user_variables.remove(name) != null)
      mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", new Object[] { Integer.valueOf(getObjectId()), name });
  }

  public String getVar(String name)
  {
    return (String)user_variables.get(name);
  }

  public boolean getVarB(String name, boolean defaultVal)
  {
    String var = (String)user_variables.get(name);
    if (var == null)
      return defaultVal;
    return (!var.equals("0")) && (!var.equalsIgnoreCase("false"));
  }

  public boolean getVarB(String name)
  {
    String var = (String)user_variables.get(name);
    return (var != null) && (!var.equals("0")) && (!var.equalsIgnoreCase("false"));
  }

  public long getVarLong(String name)
  {
    return getVarLong(name, 0L);
  }

  public long getVarLong(String name, long defaultVal)
  {
    long result = defaultVal;
    String var = getVar(name);
    if (var != null)
      result = Long.parseLong(var);
    return result;
  }

  public int getVarInt(String name)
  {
    return getVarInt(name, 0);
  }

  public int getVarInt(String name, int defaultVal)
  {
    int result = defaultVal;
    String var = getVar(name);
    if (var != null)
      result = Integer.parseInt(var);
    return result;
  }

  public Map<String, String> getVars()
  {
    return user_variables;
  }

  private void loadVariables()
  {
    Connection con = null;
    PreparedStatement offline = null;
    ResultSet rs = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      offline = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?");
      offline.setInt(1, getObjectId());
      rs = offline.executeQuery();
      while (rs.next())
      {
        String name = rs.getString("name");
        String value = Strings.stripSlashes(rs.getString("value"));
        user_variables.put(name, value);
      }

      if (getVar("lang@") == null)
        setVar("lang@", Config.DEFAULT_LANG, -1L);
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, offline, rs);
    }
  }

  public static String getVarFromPlayer(int objId, String var)
  {
    String value = null;
    Connection con = null;
    PreparedStatement offline = null;
    ResultSet rs = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      offline = con.prepareStatement("SELECT value FROM character_variables WHERE obj_id = ? AND name = ?");
      offline.setInt(1, objId);
      offline.setString(2, var);
      rs = offline.executeQuery();
      if (rs.next())
        value = Strings.stripSlashes(rs.getString("value"));
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, offline, rs);
    }
    return value;
  }

  public String getLang()
  {
    return getVar("lang@");
  }

  public int getLangId()
  {
    String lang = getLang();
    if ((lang.equalsIgnoreCase("en")) || (lang.equalsIgnoreCase("e")) || (lang.equalsIgnoreCase("eng")))
      return 0;
    if ((lang.equalsIgnoreCase("ru")) || (lang.equalsIgnoreCase("r")) || (lang.equalsIgnoreCase("rus")))
      return 1;
    return -1;
  }

  public Language getLanguage()
  {
    String lang = getLang();
    if ((lang == null) || (lang.equalsIgnoreCase("en")) || (lang.equalsIgnoreCase("e")) || (lang.equalsIgnoreCase("eng")))
      return Language.ENGLISH;
    if ((lang.equalsIgnoreCase("ru")) || (lang.equalsIgnoreCase("r")) || (lang.equalsIgnoreCase("rus")))
      return Language.RUSSIAN;
    return Language.ENGLISH;
  }

  public boolean isLangRus()
  {
    return getLangId() == 1;
  }

  public int isAtWarWith(Integer id)
  {
    return (_clan == null) || (!_clan.isAtWarWith(id.intValue())) ? 0 : 1;
  }

  public int isAtWar()
  {
    return (_clan == null) || (_clan.isAtWarOrUnderAttack() <= 0) ? 0 : 1;
  }

  public void stopWaterTask()
  {
    if (_taskWater != null)
    {
      _taskWater.cancel(false);
      _taskWater = null;
      sendPacket(new SetupGauge(this, 2, 0));
      sendChanges();
    }
  }

  public void startWaterTask()
  {
    if (isDead()) {
      stopWaterTask();
    } else if ((Config.ALLOW_WATER) && (_taskWater == null))
    {
      int timeinwater = (int)(calcStat(Stats.BREATH, 86.0D, null, null) * 1000.0D);
      sendPacket(new SetupGauge(this, 2, timeinwater));
      if ((getTransformation() > 0) && (getTransformationTemplate() > 0) && (!isCursedWeaponEquipped()))
        setTransformation(0);
      _taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.WaterTask(this), timeinwater, 1000L);
      sendChanges();
    }
  }

  public void doRevive(double percent)
  {
    restoreExp(percent);
    doRevive();
  }

  public void doRevive()
  {
    super.doRevive();
    setAgathionRes(false);
    unsetVar("lostexp");
    updateEffectIcons();
    autoShot();
  }

  public void reviveRequest(Player reviver, double percent, boolean pet)
  {
    ReviveAnswerListener reviveAsk = (_askDialog != null) && ((_askDialog.getValue() instanceof ReviveAnswerListener)) ? (ReviveAnswerListener)_askDialog.getValue() : null;
    if (reviveAsk != null)
    {
      if ((reviveAsk.isForPet() == pet) && (reviveAsk.getPower() >= percent))
      {
        reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
        return;
      }
      if ((pet) && (!reviveAsk.isForPet()))
      {
        reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
        return;
      }
      if ((pet) && (isDead()))
      {
        reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
        return;
      }
    }

    if (((pet) && (getPet() != null) && (getPet().isDead())) || ((!pet) && (isDead())))
    {
      ConfirmDlg pkt = new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0);
      ((ConfirmDlg)pkt.addName(reviver)).addString(new StringBuilder().append(Math.round(percent)).append(" percent").toString());

      ask(pkt, new ReviveAnswerListener(this, percent, pet));
    }
  }

  public void summonCharacterRequest(Creature summoner, Location loc, int summonConsumeCrystal)
  {
    ConfirmDlg cd = new ConfirmDlg(SystemMsg.C1_WISHES_TO_SUMMON_YOU_FROM_S2, 60000);
    ((ConfirmDlg)cd.addName(summoner)).addZoneName(loc);

    ask(cd, new SummonAnswerListener(this, loc, summonConsumeCrystal));
  }

  public void scriptRequest(String text, String scriptName, Object[] args)
  {
    ask((ConfirmDlg)new ConfirmDlg(SystemMsg.S1, 30000).addString(text), new ScriptAnswerListener(this, scriptName, args));
  }

  public void updateNoChannel(long time)
  {
    setNoChannel(time);

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
      statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=?");
      statement.setLong(1, _NoChannel > 0L ? _NoChannel / 1000L : _NoChannel);
      statement.setInt(2, getObjectId());
      statement.executeUpdate();
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Could not activate nochannel:").append(e).toString());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    sendPacket(new EtcStatusUpdate(this));
  }

  private void checkRecom()
  {
    Calendar temp = Calendar.getInstance();
    temp.set(11, 6);
    temp.set(12, 30);
    temp.set(13, 0);
    temp.set(14, 0);
    long count = Math.round((float)((System.currentTimeMillis() / 1000L - _lastAccess) / 86400L));
    if ((count == 0L) && (_lastAccess < temp.getTimeInMillis() / 1000L) && (System.currentTimeMillis() > temp.getTimeInMillis())) {
      count += 1L;
    }
    for (int i = 1; i < count; i++) {
      setRecomHave(getRecomHave() - 20);
    }
    if (count > 0L)
      restartRecom();
  }

  public void restartRecom()
  {
    setRecomBonusTime(3600);
    setRecomLeftToday(0);
    setRecomLeft(20);
    setRecomHave(getRecomHave() - 20);
    stopRecomBonusTask(false);
    startRecomBonusTask();
    sendUserInfo(true);
    sendVoteSystemInfo();
  }

  public boolean isInBoat()
  {
    return _boat != null;
  }

  public Boat getBoat()
  {
    return _boat;
  }

  public void setBoat(Boat boat)
  {
    _boat = boat;
  }

  public Location getInBoatPosition()
  {
    return _inBoatPosition;
  }

  public void setInBoatPosition(Location loc)
  {
    _inBoatPosition = loc;
  }

  public Map<Integer, SubClass> getSubClasses()
  {
    return _classlist;
  }

  public void setBaseClass(int baseClass)
  {
    _baseClass = baseClass;
  }

  public int getBaseClassId()
  {
    return _baseClass;
  }

  public void setActiveClass(SubClass activeClass)
  {
    _activeClass = activeClass;
  }

  public SubClass getActiveClass()
  {
    return _activeClass;
  }

  public int getActiveClassId()
  {
    return getActiveClass().getClassId();
  }

  public synchronized void changeClassInDb(int oldclass, int newclass)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
      statement.setInt(1, newclass);
      statement.setInt(2, getObjectId());
      statement.setInt(3, oldclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, newclass);
      statement.setInt(2, getObjectId());
      statement.setInt(3, oldclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE object_id=? AND class_index=?");
      statement.setInt(1, newclass);
      statement.setInt(2, getObjectId());
      statement.setInt(3, oldclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, newclass);
      statement.setInt(2, getObjectId());
      statement.setInt(3, oldclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE character_effects_save SET id=? WHERE object_id=? AND id=?");
      statement.setInt(1, newclass);
      statement.setInt(2, getObjectId());
      statement.setInt(3, oldclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newclass);
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, newclass);
      statement.setInt(2, getObjectId());
      statement.setInt(3, oldclass);
      statement.executeUpdate();
      DbUtils.close(statement);
    }
    catch (SQLException e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void storeCharSubClasses()
  {
    SubClass main = getActiveClass();
    if (main != null)
    {
      main.setCp(getCurrentCp());

      main.setHp(getCurrentHp());
      main.setMp(getCurrentMp());
      main.setActive(true);
      getSubClasses().put(Integer.valueOf(getActiveClassId()), main);
    }
    else {
      _log.warn(new StringBuilder().append("Could not store char sub data, main class ").append(getActiveClassId()).append(" not found for ").append(this).toString());
    }
    Connection con = null;
    Statement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.createStatement();

      for (SubClass subClass : getSubClasses().values())
      {
        StringBuilder sb = new StringBuilder("UPDATE character_subclasses SET ");
        sb.append("exp=").append(subClass.getExp()).append(",");
        sb.append("sp=").append(subClass.getSp()).append(",");
        sb.append("curHp=").append(subClass.getHp()).append(",");
        sb.append("curMp=").append(subClass.getMp()).append(",");
        sb.append("curCp=").append(subClass.getCp()).append(",");
        sb.append("level=").append(subClass.getLevel()).append(",");
        sb.append("active=").append(subClass.isActive() ? 1 : 0).append(",");
        sb.append("isBase=").append(subClass.isBase() ? 1 : 0).append(",");
        sb.append("death_penalty=").append(subClass.getDeathPenalty(this).getLevelOnSaveDB()).append(",");
        sb.append("certification='").append(subClass.getCertification()).append("'");
        sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND class_id=").append(subClass.getClassId()).append(" LIMIT 1");
        statement.executeUpdate(sb.toString());
      }

      StringBuilder sb = new StringBuilder("UPDATE character_subclasses SET ");
      sb.append("maxHp=").append(getMaxHp()).append(",");
      sb.append("maxMp=").append(getMaxMp()).append(",");
      sb.append("maxCp=").append(getMaxCp());
      sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND active=1 LIMIT 1");
      statement.executeUpdate(sb.toString());
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Could not store char sub data: ").append(e).toString());
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public static void restoreCharSubClasses(Player player)
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT class_id,exp,sp,curHp,curCp,curMp,active,isBase,death_penalty,certification FROM character_subclasses WHERE char_obj_id=?");
      statement.setInt(1, player.getObjectId());
      rset = statement.executeQuery();

      SubClass activeSubclass = null;
      while (rset.next())
      {
        SubClass subClass = new SubClass();
        subClass.setBase(rset.getInt("isBase") != 0);
        subClass.setClassId(rset.getInt("class_id"));
        subClass.setExp(rset.getLong("exp"));
        subClass.setSp(rset.getInt("sp"));
        subClass.setHp(rset.getDouble("curHp"));
        subClass.setMp(rset.getDouble("curMp"));
        subClass.setCp(rset.getDouble("curCp"));
        subClass.setDeathPenalty(new DeathPenalty(player, rset.getInt("death_penalty")));
        subClass.setCertification(rset.getInt("certification"));

        boolean active = rset.getInt("active") != 0;
        if (active)
          activeSubclass = subClass;
        player.getSubClasses().put(Integer.valueOf(subClass.getClassId()), subClass);
      }

      if (player.getSubClasses().size() == 0) {
        throw new Exception(new StringBuilder().append("There are no one subclass for player: ").append(player).toString());
      }
      int BaseClassId = player.getBaseClassId();
      if (BaseClassId == -1) {
        throw new Exception(new StringBuilder().append("There are no base subclass for player: ").append(player).toString());
      }
      if (activeSubclass != null) {
        player.setActiveSubClass(activeSubclass.getClassId(), false);
      }
      if (player.getActiveClass() == null)
      {
        SubClass subClass = (SubClass)player.getSubClasses().get(Integer.valueOf(BaseClassId));
        subClass.setActive(true);
        player.setActiveSubClass(subClass.getClassId(), false);
      }
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Could not restore char sub-classes: ").append(e).toString());
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public boolean addSubClass(int classId, boolean storeOld, int certification)
  {
    if (_classlist.size() >= 4) {
      return false;
    }
    ClassId newId = ClassId.VALUES[classId];

    SubClass newClass = new SubClass();
    newClass.setBase(false);
    if (newId.getRace() == null) {
      return false;
    }
    newClass.setClassId(classId);
    newClass.setCertification(certification);

    _classlist.put(Integer.valueOf(classId), newClass);

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newClass.getClassId());
      statement.setLong(3, l2p.gameserver.model.base.Experience.LEVEL[40]);
      statement.setInt(4, 0);
      statement.setDouble(5, getCurrentHp());
      statement.setDouble(6, getCurrentMp());
      statement.setDouble(7, getCurrentCp());
      statement.setDouble(8, getCurrentHp());
      statement.setDouble(9, getCurrentMp());
      statement.setDouble(10, getCurrentCp());
      statement.setInt(11, 40);
      statement.setInt(12, 0);
      statement.setInt(13, 0);
      statement.setInt(14, 0);
      statement.setInt(15, certification);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Could not add character sub-class: ").append(e).toString(), e);
      int i = 0;
      return i; } finally { DbUtils.closeQuietly(con, statement);
    }

    setActiveSubClass(classId, storeOld);

    boolean countUnlearnable = true;
    int unLearnable = 0;

    Collection skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
    while (skills.size() > unLearnable)
    {
      for (SkillLearn s : skills)
      {
        Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
        if ((sk == null) || (!sk.getCanLearn(newId)))
        {
          if (countUnlearnable) {
            unLearnable++; continue;
          }
        }
        addSkill(sk, true);
      }
      countUnlearnable = false;
      skills = SkillAcquireHolder.getInstance().getAvailableSkills(this, AcquireType.NORMAL);
    }

    sendPacket(new SkillList(this));
    setCurrentHpMp(getMaxHp(), getMaxMp(), true);
    setCurrentCp(getMaxCp());
    return true;
  }

  public boolean modifySubClass(int oldClassId, int newClassId)
  {
    SubClass originalClass = (SubClass)_classlist.get(Integer.valueOf(oldClassId));
    if ((originalClass == null) || (originalClass.isBase())) {
      return false;
    }
    int certification = originalClass.getCertification();

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
      statement.setInt(1, getObjectId());
      statement.setInt(2, oldClassId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
      statement.setInt(1, getObjectId());
      statement.setInt(2, oldClassId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
      statement.setInt(1, getObjectId());
      statement.setInt(2, oldClassId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_effects_save WHERE object_id=? AND id=? ");
      statement.setInt(1, getObjectId());
      statement.setInt(2, oldClassId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
      statement.setInt(1, getObjectId());
      statement.setInt(2, oldClassId);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND class_index=? ");
      statement.setInt(1, getObjectId());
      statement.setInt(2, oldClassId);
      statement.execute();
      DbUtils.close(statement);
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("Could not delete char sub-class: ").append(e).toString());
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
    _classlist.remove(Integer.valueOf(oldClassId));

    return (newClassId <= 0) || (addSubClass(newClassId, false, certification));
  }

  public void setActiveSubClass(int subId, boolean store)
  {
    SubClass sub = (SubClass)getSubClasses().get(Integer.valueOf(subId));
    if (sub == null) {
      return;
    }
    if (getActiveClass() != null)
    {
      EffectsDAO.getInstance().insert(this);
      storeDisableSkills();

      if (QuestManager.getQuest(422) != null)
      {
        String qn = QuestManager.getQuest(422).getName();
        if (qn != null)
        {
          QuestState qs = getQuestState(qn);
          if (qs != null) {
            qs.exitCurrentQuest(true);
          }
        }
      }
    }
    if (store)
    {
      SubClass oldsub = getActiveClass();
      oldsub.setCp(getCurrentCp());

      oldsub.setHp(getCurrentHp());
      oldsub.setMp(getCurrentMp());
      oldsub.setActive(false);
      getSubClasses().put(Integer.valueOf(getActiveClassId()), oldsub);
    }

    sub.setActive(true);
    setActiveClass(sub);
    getSubClasses().put(Integer.valueOf(getActiveClassId()), sub);

    setClassId(subId, false, false);

    removeAllSkills();

    getEffectList().stopAllEffects();

    if ((getPet() != null) && ((getPet().isSummon()) || ((Config.ALT_IMPROVED_PETS_LIMITED_USE) && (((getPet().getNpcId() == 16035) && (!isMageClass())) || ((getPet().getNpcId() == 16034) && (isMageClass())))))) {
      getPet().unSummon();
    }
    setAgathion(0);

    restoreSkills();
    rewardSkills(false);
    checkSkills();
    sendPacket(new ExStorageMaxCount(this));

    refreshExpertisePenalty();

    sendPacket(new SkillList(this));

    getInventory().refreshEquip();
    getInventory().validateItems();

    for (int i = 0; i < 3; i++) {
      _henna[i] = null;
    }
    restoreHenna();
    sendPacket(new HennaInfo(this));

    EffectsDAO.getInstance().restoreEffects(this);
    restoreDisableSkills();

    setCurrentHpMp(sub.getHp(), sub.getMp());
    setCurrentCp(sub.getCp());

    _shortCuts.restore();
    sendPacket(new ShortCutInit(this));
    for (Iterator i$ = getAutoSoulShot().iterator(); i$.hasNext(); ) { int shotId = ((Integer)i$.next()).intValue();
      sendPacket(new ExAutoSoulShot(shotId, true)); }
    sendPacket(new SkillCoolTime(this));

    broadcastPacket(new L2GameServerPacket[] { new SocialAction(getObjectId(), 2122) });

    getDeathPenalty().restore(this);

    setIncreasedForce(0);

    startHourlyTask();

    broadcastCharInfo();
    updateEffectIcons();
    updateStats();
  }

  public void startKickTask(long delayMillis)
  {
    stopKickTask();
    _kickTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.KickTask(this), delayMillis);
  }

  public void stopKickTask()
  {
    if (_kickTask != null)
    {
      _kickTask.cancel(false);
      _kickTask = null;
    }
  }

  public void startBonusTask()
  {
    if (Config.SERVICES_RATE_TYPE != 0)
    {
      int bonusExpire = getNetConnection().getBonusExpire();
      double bonus = getNetConnection().getBonus();
      if (bonusExpire > System.currentTimeMillis() / 1000L)
      {
        getBonus().setRateXp(bonus);
        getBonus().setRateSp(bonus);
        getBonus().setDropAdena(bonus);
        getBonus().setDropItems(bonus);
        getBonus().setDropSpoil(bonus);

        getBonus().setBonusExpire(bonusExpire);

        if (_bonusExpiration == null)
          _bonusExpiration = LazyPrecisionTaskManager.getInstance().startBonusExpirationTask(this);
      }
      else if ((bonus > 0.0D) && (Config.SERVICES_RATE_TYPE == 2)) {
        AccountBonusDAO.getInstance().delete(getAccountName());
      }
    }
  }

  public void stopBonusTask() {
    if (_bonusExpiration != null)
    {
      _bonusExpiration.cancel(false);
      _bonusExpiration = null;
    }
  }

  public int getInventoryLimit()
  {
    return (int)calcStat(Stats.INVENTORY_LIMIT, 0.0D, null, null);
  }

  public int getWarehouseLimit()
  {
    return (int)calcStat(Stats.STORAGE_LIMIT, 0.0D, null, null);
  }

  public int getTradeLimit()
  {
    return (int)calcStat(Stats.TRADE_LIMIT, 0.0D, null, null);
  }

  public int getDwarvenRecipeLimit()
  {
    return (int)calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50.0D, null, null) + Config.ALT_ADD_RECIPES;
  }

  public int getCommonRecipeLimit()
  {
    return (int)calcStat(Stats.COMMON_RECIPE_LIMIT, 50.0D, null, null) + Config.ALT_ADD_RECIPES;
  }

  public Element getAttackElement()
  {
    return Formulas.getAttackElement(this, null);
  }

  public int getAttack(Element element)
  {
    if (element == Element.NONE)
      return 0;
    return (int)calcStat(element.getAttack(), 0.0D, null, null);
  }

  public int getDefence(Element element)
  {
    if (element == Element.NONE)
      return 0;
    return (int)calcStat(element.getDefence(), 0.0D, null, null);
  }

  public boolean getAndSetLastItemAuctionRequest()
  {
    if (_lastItemAuctionInfoRequest + 2000L < System.currentTimeMillis())
    {
      _lastItemAuctionInfoRequest = System.currentTimeMillis();
      return true;
    }

    _lastItemAuctionInfoRequest = System.currentTimeMillis();
    return false;
  }

  public int getNpcId()
  {
    return -2;
  }

  public GameObject getVisibleObject(int id)
  {
    if (getObjectId() == id) {
      return this;
    }
    GameObject target = null;

    if (getTargetId() == id) {
      target = getTarget();
    }
    if ((target == null) && (_party != null)) {
      for (Player p : _party.getPartyMembers())
        if ((p != null) && (p.getObjectId() == id))
        {
          target = p;
          break;
        }
    }
    if (target == null) {
      target = World.getAroundObjectById(this, id);
    }
    return (target == null) || (target.isInvisible()) ? null : target;
  }

  public int getPAtk(Creature target)
  {
    double init = getActiveWeaponInstance() == null ? isMageClass() ? 3 : 4 : 0.0D;
    return (int)calcStat(Stats.POWER_ATTACK, init, target, null);
  }

  public int getPDef(Creature target)
  {
    double init = 4.0D;

    ItemInstance chest = getInventory().getPaperdollItem(10);
    if (chest == null)
      init += (isMageClass() ? 15.0D : 31.0D);
    if ((getInventory().getPaperdollItem(11) == null) && ((chest == null) || (chest.getBodyPart() != 32768))) {
      init += (isMageClass() ? 8.0D : 18.0D);
    }
    if (getInventory().getPaperdollItem(6) == null)
      init += 12.0D;
    if (getInventory().getPaperdollItem(9) == null)
      init += 8.0D;
    if (getInventory().getPaperdollItem(12) == null) {
      init += 7.0D;
    }
    return (int)calcStat(Stats.POWER_DEFENCE, init, target, null);
  }

  public int getMDef(Creature target, Skill skill)
  {
    double init = 0.0D;

    if (getInventory().getPaperdollItem(2) == null)
      init += 9.0D;
    if (getInventory().getPaperdollItem(1) == null)
      init += 9.0D;
    if (getInventory().getPaperdollItem(3) == null)
      init += 13.0D;
    if (getInventory().getPaperdollItem(5) == null)
      init += 5.0D;
    if (getInventory().getPaperdollItem(4) == null) {
      init += 5.0D;
    }
    return (int)calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
  }

  public boolean isSubClassActive()
  {
    return getBaseClassId() != getActiveClassId();
  }

  public String getTitle()
  {
    return super.getTitle();
  }

  public int getTitleColor()
  {
    return _titlecolor;
  }

  public void setTitleColor(int titlecolor)
  {
    if (titlecolor != 16777079)
      setVar("titlecolor", Integer.toHexString(titlecolor), -1L);
    else
      unsetVar("titlecolor");
    _titlecolor = titlecolor;
  }

  public boolean isCursedWeaponEquipped()
  {
    return _cursedWeaponEquippedId != 0;
  }

  public void setCursedWeaponEquippedId(int value)
  {
    _cursedWeaponEquippedId = value;
  }

  public int getCursedWeaponEquippedId()
  {
    return _cursedWeaponEquippedId;
  }

  public boolean isImmobilized()
  {
    return (super.isImmobilized()) || (isOverloaded()) || (isSitting()) || (isFishing());
  }

  public boolean isBlocked()
  {
    return (super.isBlocked()) || (isInMovie()) || (isInObserverMode()) || (isTeleporting()) || (isLogoutStarted());
  }

  public boolean isInvul()
  {
    return (super.isInvul()) || (isInMovie());
  }

  public void setOverloaded(boolean overloaded)
  {
    _overloaded = overloaded;
  }

  public boolean isOverloaded()
  {
    return _overloaded;
  }

  public boolean isFishing()
  {
    return _isFishing;
  }

  public Fishing getFishing()
  {
    return _fishing;
  }

  public void setFishing(boolean value)
  {
    _isFishing = value;
  }

  public void startFishing(FishTemplate fish, int lureId)
  {
    _fishing.setFish(fish);
    _fishing.setLureId(lureId);
    _fishing.startFishing();
  }

  public void stopFishing()
  {
    _fishing.stopFishing();
  }

  public Location getFishLoc()
  {
    return _fishing.getFishLoc();
  }

  public Bonus getBonus()
  {
    return _bonus;
  }

  public boolean hasBonus()
  {
    return _bonus.getBonusExpire() > System.currentTimeMillis() / 1000L;
  }

  public double getRateAdena()
  {
    return _party == null ? _bonus.getDropAdena() : _party._rateAdena;
  }

  public double getRateItems()
  {
    return _party == null ? _bonus.getDropItems() : _party._rateDrop;
  }

  public double getRateExp()
  {
    return calcStat(Stats.EXP, _party == null ? _bonus.getRateXp() : _party._rateExp, null, null);
  }

  public double getRateSp()
  {
    return calcStat(Stats.SP, _party == null ? _bonus.getRateSp() : _party._rateSp, null, null);
  }

  public double getRateSpoil()
  {
    return _party == null ? _bonus.getDropSpoil() : _party._rateSpoil;
  }

  public boolean isMaried()
  {
    return _maried;
  }

  public void setMaried(boolean state)
  {
    _maried = state;
  }

  public void setMaryRequest(boolean state)
  {
    _maryrequest = state;
  }

  public boolean isMaryRequest()
  {
    return _maryrequest;
  }

  public void setMaryAccepted(boolean state)
  {
    _maryaccepted = state;
  }

  public boolean isMaryAccepted()
  {
    return _maryaccepted;
  }

  public int getPartnerId()
  {
    return _partnerId;
  }

  public void setPartnerId(int partnerid)
  {
    _partnerId = partnerid;
  }

  public int getCoupleId()
  {
    return _coupleId;
  }

  public void setCoupleId(int coupleId)
  {
    _coupleId = coupleId;
  }

  public void setUndying(boolean val)
  {
    if (!isGM())
      return;
    _isUndying = val;
  }

  public boolean isUndying()
  {
    return _isUndying;
  }

  public void resetReuse()
  {
    _skillReuses.clear();
    _sharedGroupReuses.clear();
  }

  public DeathPenalty getDeathPenalty()
  {
    return _activeClass == null ? null : _activeClass.getDeathPenalty(this);
  }

  public boolean isCharmOfCourage()
  {
    return _charmOfCourage;
  }

  public void setCharmOfCourage(boolean val)
  {
    _charmOfCourage = val;

    if (!val) {
      getEffectList().stopEffect(5041);
    }
    sendEtcStatusUpdate();
  }

  public int getIncreasedForce()
  {
    return _increasedForce;
  }

  public int getConsumedSouls()
  {
    return _consumedSouls;
  }

  public void setConsumedSouls(int i, NpcInstance monster)
  {
    if (i == _consumedSouls) {
      return;
    }
    int max = (int)calcStat(Stats.SOULS_LIMIT, 0.0D, monster, null);

    if (i > max) {
      i = max;
    }
    if (i <= 0)
    {
      _consumedSouls = 0;
      sendEtcStatusUpdate();
      return;
    }

    if (_consumedSouls != i)
    {
      int diff = i - _consumedSouls;
      if (diff > 0)
      {
        SystemMessage sm = new SystemMessage(2162);
        sm.addNumber(diff);
        sm.addNumber(i);
        sendPacket(sm);
      }
    }
    else if (max == i)
    {
      sendPacket(Msg.SOUL_CANNOT_BE_ABSORBED_ANY_MORE);
      return;
    }

    _consumedSouls = i;
    sendPacket(new EtcStatusUpdate(this));
  }

  public void setIncreasedForce(int i)
  {
    i = Math.min(i, 8);
    i = Math.max(i, 0);

    if ((i != 0) && (i > _increasedForce)) {
      sendPacket(new SystemMessage(323).addNumber(i));
    }
    _increasedForce = i;
    sendEtcStatusUpdate();
  }

  public boolean isFalling()
  {
    return System.currentTimeMillis() - _lastFalling < 5000L;
  }

  public void falling(int height)
  {
    if ((!Config.DAMAGE_FROM_FALLING) || (isDead()) || (isFlying()) || (isInWater()) || (isInBoat()))
      return;
    _lastFalling = System.currentTimeMillis();
    int damage = (int)calcStat(Stats.FALL, getMaxHp() / 2000 * height, null, null);
    if (damage > 0)
    {
      int curHp = (int)getCurrentHp();
      if (curHp - damage < 1)
        setCurrentHp(1.0D, false);
      else
        setCurrentHp(curHp - damage, false);
      sendPacket(new SystemMessage(296).addNumber(damage));
    }
  }

  public void checkHpMessages(double curHp, double newHp)
  {
    int[] _hp = { 30, 30 };

    int[] skills = { 290, 291 };

    int[] _effects_skills_id = { 139, 176, 292, 292, 420 };

    int[] _effects_hp = { 30, 30, 30, 60, 30 };

    double percent = getMaxHp() / 100;
    double _curHpPercent = curHp / percent;
    double _newHpPercent = newHp / percent;
    boolean needsUpdate = false;
    int level;
    for (int i = 0; i < skills.length; i++)
    {
      level = getSkillLevel(Integer.valueOf(skills[i]));
      if (level > 0)
        if ((_curHpPercent > _hp[i]) && (_newHpPercent <= _hp[i]))
        {
          sendPacket(new SystemMessage(1133).addSkillName(skills[i], level));
          needsUpdate = true;
        } else {
          if ((_curHpPercent > _hp[i]) || (_newHpPercent <= _hp[i]))
            continue;
          sendPacket(new SystemMessage(1134).addSkillName(skills[i], level));
          needsUpdate = true;
        }
    }
    Integer localInteger1;
    for (Integer i = Integer.valueOf(0); i.intValue() < _effects_skills_id.length; localInteger1 = i = Integer.valueOf(i.intValue() + 1)) {
      if (getEffectList().getEffectsBySkillId(_effects_skills_id[i.intValue()]) != null)
        if ((_curHpPercent > _effects_hp[i.intValue()]) && (_newHpPercent <= _effects_hp[i.intValue()]))
        {
          sendPacket(new SystemMessage(1133).addSkillName(_effects_skills_id[i.intValue()], 1));
          needsUpdate = true;
        }
        else if ((_curHpPercent <= _effects_hp[i.intValue()]) && (_newHpPercent > _effects_hp[i.intValue()]))
        {
          sendPacket(new SystemMessage(1134).addSkillName(_effects_skills_id[i.intValue()], 1));
          needsUpdate = true;
        }
      level = i;
    }

    if (needsUpdate)
      sendChanges();
  }

  public void checkDayNightMessages()
  {
    int level = getSkillLevel(Integer.valueOf(294));
    if (level > 0)
      if (GameTimeController.getInstance().isNowNight())
        sendPacket(new SystemMessage(1131).addSkillName(294, level));
      else
        sendPacket(new SystemMessage(1132).addSkillName(294, level));
    sendChanges();
  }

  public int getZoneMask()
  {
    return _zoneMask;
  }

  protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
  {
    super.onUpdateZones(leaving, entering);

    if (((leaving == null) || (leaving.isEmpty())) && ((entering == null) || (entering.isEmpty()))) {
      return;
    }
    boolean lastInCombatZone = (_zoneMask & 0x4000) == 16384;
    boolean lastInDangerArea = (_zoneMask & 0x100) == 256;
    boolean lastOnSiegeField = (_zoneMask & 0x800) == 2048;
    boolean lastInPeaceZone = (_zoneMask & 0x1000) == 4096;

    boolean isInCombatZone = isInCombatZone();
    boolean isInDangerArea = isInDangerArea();
    boolean isOnSiegeField = isOnSiegeField();
    boolean isInPeaceZone = isInPeaceZone();
    boolean isInSSQZone = isInSSQZone();

    int lastZoneMask = _zoneMask;
    _zoneMask = 0;

    if (isInCombatZone)
      _zoneMask |= 16384;
    if (isInDangerArea)
      _zoneMask |= 256;
    if (isOnSiegeField)
      _zoneMask |= 2048;
    if (isInPeaceZone)
      _zoneMask |= 4096;
    if (isInSSQZone) {
      _zoneMask |= 8192;
    }
    if (lastZoneMask != _zoneMask) {
      sendPacket(new ExSetCompassZoneCode(this));
    }
    if (lastInCombatZone != isInCombatZone) {
      broadcastRelationChanged();
    }
    if (lastInDangerArea != isInDangerArea) {
      sendPacket(new EtcStatusUpdate(this));
    }
    if (lastOnSiegeField != isOnSiegeField)
    {
      broadcastRelationChanged();
      if (isOnSiegeField) {
        sendPacket(Msg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
      }
      else {
        sendPacket(Msg.YOU_HAVE_LEFT_A_COMBAT_ZONE);
        if ((!isTeleporting()) && (getPvpFlag() == 0)) {
          startPvPFlag(null);
        }
      }
    }
    if (lastInPeaceZone != isInPeaceZone)
      if (isInPeaceZone)
      {
        setRecomTimerActive(false);
        if (getNevitSystem().isActive())
          getNevitSystem().stopAdventTask(true);
        startVitalityTask();
      }
      else {
        stopVitalityTask();
      }
    if (isInWater())
      startWaterTask();
    else
      stopWaterTask();
  }

  public void startAutoSaveTask()
  {
    if (!Config.AUTOSAVE)
      return;
    if (_autoSaveTask == null)
      _autoSaveTask = AutoSaveManager.getInstance().addAutoSaveTask(this);
  }

  public void stopAutoSaveTask()
  {
    if (_autoSaveTask != null)
      _autoSaveTask.cancel(false);
    _autoSaveTask = null;
  }

  public void startVitalityTask()
  {
    if (!Config.ALT_VITALITY_ENABLED)
      return;
    if (_vitalityTask == null)
      _vitalityTask = LazyPrecisionTaskManager.getInstance().addVitalityRegenTask(this);
  }

  public void stopVitalityTask()
  {
    if (_vitalityTask != null)
      _vitalityTask.cancel(false);
    _vitalityTask = null;
  }

  public void startPcBangPointsTask()
  {
    if ((!Config.ALT_PCBANG_POINTS_ENABLED) || (Config.ALT_PCBANG_POINTS_DELAY <= 0))
      return;
    if (_pcCafePointsTask == null)
      _pcCafePointsTask = LazyPrecisionTaskManager.getInstance().addPCCafePointsTask(this);
  }

  public void stopPcBangPointsTask()
  {
    if (_pcCafePointsTask != null)
      _pcCafePointsTask.cancel(false);
    _pcCafePointsTask = null;
  }

  public void startUnjailTask(Player player, int time)
  {
    if (_unjailTask != null)
      _unjailTask.cancel(false);
    _unjailTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.UnJailTask(player), time * 60000);
  }

  public void stopUnjailTask()
  {
    if (_unjailTask != null)
      _unjailTask.cancel(false);
    _unjailTask = null;
  }

  public void sendMessage(String message)
  {
    sendPacket(new SystemMessage(message));
  }

  public void setLastClientPosition(Location position)
  {
    _lastClientPosition = position;
  }

  public Location getLastClientPosition()
  {
    return _lastClientPosition;
  }

  public void setLastServerPosition(Location position)
  {
    _lastServerPosition = position;
  }

  public Location getLastServerPosition()
  {
    return _lastServerPosition;
  }

  public void setUseSeed(int id)
  {
    _useSeed = id;
  }

  public int getUseSeed()
  {
    return _useSeed;
  }

  public int getRelation(Player target)
  {
    int result = 0;

    if (getClan() != null)
    {
      result |= 64;
      if (getClan() == target.getClan())
        result |= 256;
      if (getClan().getAllyId() != 0) {
        result |= 65536;
      }
    }
    if (isClanLeader()) {
      result |= 128;
    }
    Party party = getParty();
    if ((party != null) && (party == target.getParty()))
    {
      result |= 32;

      switch (party.getPartyMembers().indexOf(this))
      {
      case 0:
        result |= 16;
        break;
      case 1:
        result |= 8;
        break;
      case 2:
        result |= 7;
        break;
      case 3:
        result |= 6;
        break;
      case 4:
        result |= 5;
        break;
      case 5:
        result |= 4;
        break;
      case 6:
        result |= 3;
        break;
      case 7:
        result |= 2;
        break;
      case 8:
        result |= 1;
      }

    }

    Clan clan1 = getClan();
    Clan clan2 = target.getClan();
    if ((clan1 != null) && (clan2 != null))
    {
      if ((target.getPledgeType() != -1) && (getPledgeType() != -1) && 
        (clan2.isAtWarWith(clan1.getClanId())))
      {
        result |= 32768;
        if (clan1.isAtWarWith(clan2.getClanId()))
          result |= 16384;
      }
      if (getBlockCheckerArena() != -1)
      {
        result |= 512;
        HandysBlockCheckerManager.ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
        if (holder.getPlayerTeam(this) == 0)
          result |= 4096;
        else
          result |= 2048;
        result |= 1024;
      }
    }

    for (GlobalEvent e : getEvents()) {
      result = e.getRelation(this, target, result);
    }
    return result;
  }

  public long getlastPvpAttack()
  {
    return _lastPvpAttack;
  }

  public void startPvPFlag(Creature target)
  {
    if (_karma > 0) {
      return;
    }
    long startTime = System.currentTimeMillis();
    if ((target != null) && (target.getPvpFlag() != 0))
      startTime -= Config.PVP_TIME / 2;
    if ((_pvpFlag != 0) && (_lastPvpAttack > startTime)) {
      return;
    }
    _lastPvpAttack = startTime;

    updatePvPFlag(1);

    if (_PvPRegTask == null)
      _PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.PvPFlagTask(this), 1000L, 1000L);
  }

  public void stopPvPFlag()
  {
    if (_PvPRegTask != null)
    {
      _PvPRegTask.cancel(false);
      _PvPRegTask = null;
    }
    updatePvPFlag(0);
  }

  public void updatePvPFlag(int value)
  {
    if (_handysBlockCheckerEventArena != -1)
      return;
    if (_pvpFlag == value) {
      return;
    }
    setPvpFlag(value);

    sendStatusUpdate(true, true, new int[] { 26 });

    broadcastRelationChanged();
  }

  public void setPvpFlag(int pvpFlag)
  {
    _pvpFlag = pvpFlag;
  }

  public int getPvpFlag()
  {
    return _pvpFlag;
  }

  public boolean isInDuel()
  {
    return getEvent(DuelEvent.class) != null;
  }

  public Map<Integer, TamedBeastInstance> getTrainedBeasts()
  {
    return _tamedBeasts;
  }

  public void addTrainedBeast(TamedBeastInstance tamedBeast)
  {
    _tamedBeasts.put(Integer.valueOf(tamedBeast.getObjectId()), tamedBeast);
  }

  public void removeTrainedBeast(int npcId)
  {
    _tamedBeasts.remove(Integer.valueOf(npcId));
  }

  public long getLastAttackPacket()
  {
    return _lastAttackPacket;
  }

  public void setLastAttackPacket()
  {
    _lastAttackPacket = System.currentTimeMillis();
  }

  public long getLastMovePacket()
  {
    return _lastMovePacket;
  }

  public void setLastMovePacket()
  {
    _lastMovePacket = System.currentTimeMillis();
  }

  public byte[] getKeyBindings()
  {
    return _keyBindings;
  }

  public void setKeyBindings(byte[] keyBindings)
  {
    if (keyBindings == null)
      keyBindings = ArrayUtils.EMPTY_BYTE_ARRAY;
    _keyBindings = keyBindings;
  }

  public void setTransformation(int transformationId)
  {
    if ((transformationId == _transformationId) || ((_transformationId != 0) && (transformationId != 0))) {
      return;
    }

    if (transformationId == 0)
    {
      for (Effect effect : getEffectList().getAllEffects()) {
        if ((effect != null) && (effect.getEffectType() == EffectType.Transformation))
        {
          if (effect.calc() == 0.0D)
            continue;
          effect.exit();
          preparateToTransform(effect.getSkill());
          break;
        }
      }

      if (!_transformationSkills.isEmpty())
      {
        for (Skill s : _transformationSkills.values())
          if ((!s.isCommon()) && (!SkillAcquireHolder.getInstance().isSkillPossible(this, s)) && (!s.isHeroic()))
            super.removeSkill(s);
        _transformationSkills.clear();
      }
    }
    else
    {
      if (!isCursedWeaponEquipped())
      {
        for (Effect effect : getEffectList().getAllEffects())
          if ((effect != null) && (effect.getEffectType() == EffectType.Transformation))
          {
            if (((effect.getSkill() instanceof Transformation)) && (((Transformation)effect.getSkill()).isDisguise))
            {
              for (Skill s : getAllSkills())
                if ((s != null) && ((s.isActive()) || (s.isToggle())))
                  _transformationSkills.put(Integer.valueOf(s.getId()), s);
            }
            else
              for (Skill.AddedSkill s : effect.getSkill().getAddedSkills())
                if (s.level == 0)
                {
                  int s2 = getSkillLevel(Integer.valueOf(s.id));
                  if (s2 > 0)
                    _transformationSkills.put(Integer.valueOf(s.id), SkillTable.getInstance().getInfo(s.id, s2));
                }
                else if (s.level == -2)
                {
                  int learnLevel = Math.max(effect.getSkill().getMagicLevel(), 40);
                  int maxLevel = SkillTable.getInstance().getBaseLevel(s.id);
                  int curSkillLevel = 1;
                  if (maxLevel > 3)
                    curSkillLevel += getLevel() - learnLevel;
                  else
                    curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel);
                  curSkillLevel = Math.min(Math.max(curSkillLevel, 1), maxLevel);
                  _transformationSkills.put(Integer.valueOf(s.id), SkillTable.getInstance().getInfo(s.id, curSkillLevel));
                }
                else {
                  _transformationSkills.put(Integer.valueOf(s.id), s.getSkill());
                }
            preparateToTransform(effect.getSkill());
            break;
          }
      }
      else {
        preparateToTransform(null);
      }
      if ((!isInOlympiadMode()) && (!isCursedWeaponEquipped()) && (_hero) && (getBaseClassId() == getActiveClassId()))
      {
        _transformationSkills.put(Integer.valueOf(395), SkillTable.getInstance().getInfo(395, 1));
        _transformationSkills.put(Integer.valueOf(396), SkillTable.getInstance().getInfo(396, 1));
        _transformationSkills.put(Integer.valueOf(1374), SkillTable.getInstance().getInfo(1374, 1));
        _transformationSkills.put(Integer.valueOf(1375), SkillTable.getInstance().getInfo(1375, 1));
        _transformationSkills.put(Integer.valueOf(1376), SkillTable.getInstance().getInfo(1376, 1));
      }

      for (Skill s : _transformationSkills.values()) {
        addSkill(s, false);
      }
    }
    _transformationId = transformationId;

    sendPacket(new ExBasicActionList(this));
    sendPacket(new SkillList(this));
    sendPacket(new ShortCutInit(this));
    for (Iterator i$ = getAutoSoulShot().iterator(); i$.hasNext(); ) { int shotId = ((Integer)i$.next()).intValue();
      sendPacket(new ExAutoSoulShot(shotId, true)); }
    broadcastUserInfo(true);
  }

  private void preparateToTransform(Skill transSkill)
  {
    if ((transSkill == null) || (!transSkill.isBaseTransformation()))
    {
      for (Effect effect : getEffectList().getAllEffects())
        if ((effect != null) && (effect.getSkill().isToggle()))
          effect.exit();
    }
  }

  public boolean isInFlyingTransform()
  {
    return (_transformationId == 8) || (_transformationId == 9) || (_transformationId == 260);
  }

  public boolean isInMountTransform()
  {
    return (_transformationId == 106) || (_transformationId == 109) || (_transformationId == 110) || (_transformationId == 20001);
  }

  public int getTransformation()
  {
    return _transformationId;
  }

  public String getTransformationName()
  {
    return _transformationName;
  }

  public void setTransformationName(String name)
  {
    _transformationName = name;
  }

  public void setTransformationTemplate(int template)
  {
    _transformationTemplate = template;
  }

  public int getTransformationTemplate()
  {
    return _transformationTemplate;
  }

  public final Collection<Skill> getAllSkills()
  {
    if (_transformationId == 0) {
      return super.getAllSkills();
    }

    Map tempSkills = new HashMap();
    for (Skill s : super.getAllSkills())
      if ((s != null) && (!s.isActive()) && (!s.isToggle()))
        tempSkills.put(Integer.valueOf(s.getId()), s);
    tempSkills.putAll(_transformationSkills);
    return tempSkills.values();
  }

  public void setAgathion(int id)
  {
    if (_agathionId == id) {
      return;
    }
    _agathionId = id;
    broadcastCharInfo();
  }

  public int getAgathionId()
  {
    return _agathionId;
  }

  public int getPcBangPoints()
  {
    return _pcBangPoints;
  }

  public void setPcBangPoints(int val)
  {
    _pcBangPoints = val;
  }

  public void addPcBangPoints(int count, boolean doublePoints)
  {
    if (doublePoints) {
      count *= 2;
    }
    _pcBangPoints += count;

    sendPacket(new SystemMessage(doublePoints ? 1708 : 1707).addNumber(count));
    sendPacket(new ExPCCafePointInfo(this, count, 1, 2, 12));
  }

  public boolean reducePcBangPoints(int count)
  {
    if (_pcBangPoints < count) {
      return false;
    }
    _pcBangPoints -= count;
    sendPacket(new SystemMessage(1709).addNumber(count));
    sendPacket(new ExPCCafePointInfo(this, 0, 1, 2, 12));
    return true;
  }

  public void setGroundSkillLoc(Location location)
  {
    _groundSkillLoc = location;
  }

  public Location getGroundSkillLoc()
  {
    return _groundSkillLoc;
  }

  public boolean isLogoutStarted()
  {
    return _isLogout.get();
  }

  public void setOfflineMode(boolean val)
  {
    if (!val)
      unsetVar("offline");
    _offline = val;
  }

  public boolean isInOfflineMode()
  {
    return _offline;
  }

  public void saveTradeList()
  {
    String val = "";

    if ((_sellList == null) || (_sellList.isEmpty())) {
      unsetVar("selllist");
    }
    else {
      for (TradeItem i : _sellList)
        val = new StringBuilder().append(val).append(i.getObjectId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":").toString();
      setVar("selllist", val, -1L);
      val = "";
      if ((_tradeList != null) && (getSellStoreName() != null)) {
        setVar("sellstorename", getSellStoreName(), -1L);
      }
    }
    if ((_packageSellList == null) || (_packageSellList.isEmpty())) {
      unsetVar("packageselllist");
    }
    else {
      for (TradeItem i : _packageSellList)
        val = new StringBuilder().append(val).append(i.getObjectId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":").toString();
      setVar("packageselllist", val, -1L);
      val = "";
      if ((_tradeList != null) && (getSellStoreName() != null)) {
        setVar("sellstorename", getSellStoreName(), -1L);
      }
    }
    if ((_buyList == null) || (_buyList.isEmpty())) {
      unsetVar("buylist");
    }
    else {
      for (TradeItem i : _buyList)
        val = new StringBuilder().append(val).append(i.getItemId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":").toString();
      setVar("buylist", val, -1L);
      val = "";
      if ((_tradeList != null) && (getBuyStoreName() != null)) {
        setVar("buystorename", getBuyStoreName(), -1L);
      }
    }
    if ((_createList == null) || (_createList.isEmpty())) {
      unsetVar("createlist");
    }
    else {
      for (ManufactureItem i : _createList)
        val = new StringBuilder().append(val).append(i.getRecipeId()).append(";").append(i.getCost()).append(":").toString();
      setVar("createlist", val, -1L);
      if (getManufactureName() != null)
        setVar("manufacturename", getManufactureName(), -1L);
    }
  }

  public void restoreTradeList()
  {
    String var = getVar("selllist");
    if (var != null)
    {
      _sellList = new CopyOnWriteArrayList();
      String[] items = var.split(":");
      for (String item : items)
      {
        if (item.equals(""))
          continue;
        String[] values = item.split(";");
        if (values.length < 3) {
          continue;
        }
        int oId = Integer.parseInt(values[0]);
        long count = Long.parseLong(values[1]);
        long price = Long.parseLong(values[2]);

        ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

        if ((count < 1L) || (itemToSell == null)) {
          continue;
        }
        if (count > itemToSell.getCount()) {
          count = itemToSell.getCount();
        }
        TradeItem i = new TradeItem(itemToSell);
        i.setCount(count);
        i.setOwnersPrice(price);

        _sellList.add(i);
      }
      var = getVar("sellstorename");
      if (var != null)
        setSellStoreName(var);
    }
    var = getVar("packageselllist");
    if (var != null)
    {
      _packageSellList = new CopyOnWriteArrayList();
      String[] items = var.split(":");
      for (String item : items)
      {
        if (item.equals(""))
          continue;
        String[] values = item.split(";");
        if (values.length < 3) {
          continue;
        }
        int oId = Integer.parseInt(values[0]);
        long count = Long.parseLong(values[1]);
        long price = Long.parseLong(values[2]);

        ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

        if ((count < 1L) || (itemToSell == null)) {
          continue;
        }
        if (count > itemToSell.getCount()) {
          count = itemToSell.getCount();
        }
        TradeItem i = new TradeItem(itemToSell);
        i.setCount(count);
        i.setOwnersPrice(price);

        _packageSellList.add(i);
      }
      var = getVar("sellstorename");
      if (var != null)
        setSellStoreName(var);
    }
    var = getVar("buylist");
    if (var != null)
    {
      _buyList = new CopyOnWriteArrayList();
      String[] items = var.split(":");
      for (String item : items)
      {
        if (item.equals(""))
          continue;
        String[] values = item.split(";");
        if (values.length < 3)
          continue;
        TradeItem i = new TradeItem();
        i.setItemId(Integer.parseInt(values[0]));
        i.setCount(Long.parseLong(values[1]));
        i.setOwnersPrice(Long.parseLong(values[2]));
        _buyList.add(i);
      }
      var = getVar("buystorename");
      if (var != null)
        setBuyStoreName(var);
    }
    var = getVar("createlist");
    if (var != null)
    {
      _createList = new CopyOnWriteArrayList();
      String[] items = var.split(":");
      for (String item : items)
      {
        if (item.equals(""))
          continue;
        String[] values = item.split(";");
        if (values.length < 2)
          continue;
        int recId = Integer.parseInt(values[0]);
        long price = Long.parseLong(values[1]);
        if (findRecipe(recId))
          _createList.add(new ManufactureItem(recId, price));
      }
      var = getVar("manufacturename");
      if (var != null)
        setManufactureName(var);
    }
  }

  public void restoreRecipeBook()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
      statement.setInt(1, getObjectId());
      rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("id");
        Recipe recipe = RecipeHolder.getInstance().getRecipeByRecipeId(id);
        registerRecipe(recipe, false);
      }
    }
    catch (Exception e)
    {
      _log.warn(new StringBuilder().append("count not recipe skills:").append(e).toString());
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public DecoyInstance getDecoy()
  {
    return _decoy;
  }

  public void setDecoy(DecoyInstance decoy)
  {
    _decoy = decoy;
  }

  public int getMountType()
  {
    switch (getMountNpcId())
    {
    case 12526:
    case 12527:
    case 12528:
    case 16038:
    case 16039:
    case 16040:
    case 16068:
      return 1;
    case 12621:
      return 2;
    case 16037:
    case 16041:
    case 16042:
      return 3;
    }
    return 0;
  }

  public double getColRadius()
  {
    if (getTransformation() != 0)
    {
      int template = getTransformationTemplate();
      if (template != 0)
      {
        NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
        if (npcTemplate != null)
          return npcTemplate.collisionRadius;
      }
    }
    else if (isMounted())
    {
      int mountTemplate = getMountNpcId();
      if (mountTemplate != 0)
      {
        NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
        if (mountNpcTemplate != null)
          return mountNpcTemplate.collisionRadius;
      }
    }
    return getBaseTemplate().collisionRadius;
  }

  public double getColHeight()
  {
    if (getTransformation() != 0)
    {
      int template = getTransformationTemplate();
      if (template != 0)
      {
        NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(template);
        if (npcTemplate != null)
          return npcTemplate.collisionHeight;
      }
    }
    else if (isMounted())
    {
      int mountTemplate = getMountNpcId();
      if (mountTemplate != 0)
      {
        NpcTemplate mountNpcTemplate = NpcHolder.getInstance().getTemplate(mountTemplate);
        if (mountNpcTemplate != null)
          return mountNpcTemplate.collisionHeight;
      }
    }
    return getBaseTemplate().collisionHeight;
  }

  public void setReflection(Reflection reflection)
  {
    if (getReflection() == reflection) {
      return;
    }
    super.setReflection(reflection);

    if ((_summon != null) && (!_summon.isDead())) {
      _summon.setReflection(reflection);
    }
    if (reflection != ReflectionManager.DEFAULT)
    {
      String var = getVar("reflection");
      if ((var == null) || (!var.equals(String.valueOf(reflection.getId()))))
        setVar("reflection", String.valueOf(reflection.getId()), -1L);
    }
    else {
      unsetVar("reflection");
    }
    if (getActiveClass() != null)
    {
      getInventory().validateItems();

      if ((getPet() != null) && ((getPet().getNpcId() == 14916) || (getPet().getNpcId() == 14917)))
        getPet().unSummon();
    }
  }

  public boolean isTerritoryFlagEquipped()
  {
    ItemInstance weapon = getActiveWeaponInstance();
    return (weapon != null) && (weapon.getTemplate().isTerritoryFlag());
  }

  public void setBuyListId(int listId)
  {
    _buyListId = listId;
  }

  public int getBuyListId()
  {
    return _buyListId;
  }

  public int getFame()
  {
    return _fame;
  }

  public void setFame(int fame, String log)
  {
    fame = Math.min(Config.LIM_FAME, fame);
    if ((log != null) && (!log.isEmpty()))
      Log.add(new StringBuilder().append(_name).append("|").append(fame - _fame).append("|").append(fame).append("|").append(log).toString(), "fame");
    if (fame > _fame)
      sendPacket(new SystemMessage(2319).addNumber(fame - _fame));
    _fame = fame;
    sendChanges();
  }

  public int getVitalityLevel(boolean blessActive)
  {
    return Config.ALT_VITALITY_ENABLED ? _vitalityLevel : blessActive ? 4 : 0;
  }

  public double getVitality()
  {
    return Config.ALT_VITALITY_ENABLED ? _vitality : 0.0D;
  }

  public void addVitality(double val)
  {
    setVitality(getVitality() + val);
  }

  public void setVitality(double newVitality)
  {
    if (!Config.ALT_VITALITY_ENABLED) {
      return;
    }
    newVitality = Math.max(Math.min(newVitality, Config.VITALITY_LEVELS[4]), 0.0D);

    if ((newVitality >= _vitality) || (getLevel() >= 10))
    {
      if (newVitality != _vitality) {
        if (newVitality == 0.0D)
          sendPacket(Msg.VITALITY_IS_FULLY_EXHAUSTED);
        else if (newVitality == Config.VITALITY_LEVELS[4])
          sendPacket(Msg.YOUR_VITALITY_IS_AT_MAXIMUM);
      }
      _vitality = newVitality;
    }

    int newLevel = 0;
    if (_vitality >= Config.VITALITY_LEVELS[3])
      newLevel = 4;
    else if (_vitality >= Config.VITALITY_LEVELS[2])
      newLevel = 3;
    else if (_vitality >= Config.VITALITY_LEVELS[1])
      newLevel = 2;
    else if (_vitality >= Config.VITALITY_LEVELS[0]) {
      newLevel = 1;
    }
    if (_vitalityLevel > newLevel) {
      getNevitSystem().addPoints(1200);
    }
    if (_vitalityLevel != newLevel)
    {
      if (_vitalityLevel != -1)
        sendPacket(newLevel < _vitalityLevel ? Msg.VITALITY_HAS_DECREASED : Msg.VITALITY_HAS_INCREASED);
      _vitalityLevel = newLevel;
    }

    sendPacket(new ExVitalityPointInfo((int)_vitality));
  }

  public int getIncorrectValidateCount()
  {
    return 0;
  }

  public int setIncorrectValidateCount(int count)
  {
    return 0;
  }

  public int getExpandInventory()
  {
    return _expandInventory;
  }

  public void setExpandInventory(int inventory)
  {
    _expandInventory = inventory;
  }

  public int getExpandWarehouse()
  {
    return _expandWarehouse;
  }

  public void setExpandWarehouse(int warehouse)
  {
    _expandWarehouse = warehouse;
  }

  public boolean isNotShowBuffAnim()
  {
    return _notShowBuffAnim;
  }

  public void setNotShowBuffAnim(boolean value)
  {
    _notShowBuffAnim = value;
  }

  public void enterMovieMode()
  {
    if (isInMovie()) {
      return;
    }
    setTarget(null);
    stopMove();
    setIsInMovie(true);
    sendPacket(new CameraMode(1));
  }

  public void leaveMovieMode()
  {
    setIsInMovie(false);
    sendPacket(new CameraMode(0));
    broadcastCharInfo();
  }

  public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration)
  {
    sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
  }

  public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk)
  {
    sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk));
  }

  public void setMovieId(int id)
  {
    _movieId = id;
  }

  public int getMovieId()
  {
    return _movieId;
  }

  public boolean isInMovie()
  {
    return _isInMovie;
  }

  public void setIsInMovie(boolean state)
  {
    _isInMovie = state;
  }

  public void showQuestMovie(SceneMovie movie)
  {
    if (isInMovie()) {
      return;
    }
    sendActionFailed();
    setTarget(null);
    stopMove();
    setMovieId(movie.getId());
    setIsInMovie(true);
    sendPacket(movie.packet(this));
  }

  public void showQuestMovie(int movieId)
  {
    if (isInMovie()) {
      return;
    }
    sendActionFailed();
    setTarget(null);
    stopMove();
    setMovieId(movieId);
    setIsInMovie(true);
    sendPacket(new ExStartScenePlayer(movieId));
  }

  public void setAutoLoot(boolean enable)
  {
    if (Config.AUTO_LOOT_INDIVIDUAL)
    {
      _autoLoot = enable;
      setVar("AutoLoot", String.valueOf(enable), -1L);
    }
  }

  public void setAutoLootHerbs(boolean enable)
  {
    if (Config.AUTO_LOOT_INDIVIDUAL)
    {
      AutoLootHerbs = enable;
      setVar("AutoLootHerbs", String.valueOf(enable), -1L);
    }
  }

  public boolean isAutoLootEnabled()
  {
    return _autoLoot;
  }

  public boolean isAutoLootHerbsEnabled()
  {
    return AutoLootHerbs;
  }

  public final void reName(String name, boolean saveToDB)
  {
    setName(name);
    if (saveToDB)
      saveNameToDB();
    broadcastCharInfo();
  }

  public final void reName(String name)
  {
    reName(name, false);
  }

  public final void saveNameToDB()
  {
    Connection con = null;
    PreparedStatement st = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
      st.setString(1, getName());
      st.setInt(2, getObjectId());
      st.executeUpdate();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, st);
    }
  }

  public Player getPlayer()
  {
    return this;
  }

  private List<String> getStoredBypasses(boolean bbs)
  {
    if (bbs)
    {
      if (bypasses_bbs == null)
        bypasses_bbs = new LazyArrayList();
      return bypasses_bbs;
    }
    if (bypasses == null)
      bypasses = new LazyArrayList();
    return bypasses;
  }

  public void cleanBypasses(boolean bbs)
  {
    List bypassStorage = getStoredBypasses(bbs);
    synchronized (bypassStorage)
    {
      bypassStorage.clear();
    }
  }

  public String encodeBypasses(String htmlCode, boolean bbs)
  {
    List bypassStorage = getStoredBypasses(bbs);
    synchronized (bypassStorage)
    {
      return BypassManager.encode(htmlCode, bypassStorage, bbs);
    }
  }

  public BypassManager.DecodedBypass decodeBypass(String bypass)
  {
    BypassManager.BypassType bpType = BypassManager.getBypassType(bypass);
    boolean bbs = (bpType == BypassManager.BypassType.ENCODED_BBS) || (bpType == BypassManager.BypassType.SIMPLE_BBS);
    List bypassStorage = getStoredBypasses(bbs);
    if ((bpType == BypassManager.BypassType.ENCODED) || (bpType == BypassManager.BypassType.ENCODED_BBS))
      return BypassManager.decode(bypass, bypassStorage, bbs, this);
    if (bpType == BypassManager.BypassType.SIMPLE)
      return new BypassManager.DecodedBypass(bypass, false).trim();
    if ((bpType == BypassManager.BypassType.SIMPLE_BBS) && (!bypass.startsWith("_bbsscripts")))
      return new BypassManager.DecodedBypass(bypass, true).trim();
    ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(bypass);
    if (handler != null)
      return new BypassManager.DecodedBypass(bypass, handler).trim();
    _log.warn(new StringBuilder().append("Direct access to bypass: ").append(bypass).append(" / Player: ").append(getName()).toString());
    return null;
  }

  public int getTalismanCount()
  {
    return (int)calcStat(Stats.TALISMANS_LIMIT, 0.0D, null, null);
  }

  public boolean getOpenCloak()
  {
    if (Config.ALT_OPEN_CLOAK_SLOT)
      return true;
    return (int)calcStat(Stats.CLOAK_SLOT, 0.0D, null, null) > 0;
  }

  public final void disableDrop(int time)
  {
    _dropDisabled = (System.currentTimeMillis() + time);
  }

  public final boolean isDropDisabled()
  {
    return _dropDisabled > System.currentTimeMillis();
  }

  public void setPetControlItem(int itemObjId)
  {
    setPetControlItem(getInventory().getItemByObjectId(itemObjId));
  }

  public void setPetControlItem(ItemInstance item)
  {
    _petControlItem = item;
  }

  public ItemInstance getPetControlItem()
  {
    return _petControlItem;
  }

  public boolean isActive()
  {
    return isActive.get();
  }

  public void setActive()
  {
    setNonAggroTime(0L);

    if (isActive.getAndSet(true)) {
      return;
    }
    onActive();
  }

  private void onActive()
  {
    setNonAggroTime(0L);
    sendPacket(Msg.YOU_ARE_PROTECTED_AGGRESSIVE_MONSTERS);
    if (getPetControlItem() != null)
      ThreadPoolManager.getInstance().execute(new RunnableImpl()
      {
        public void runImpl()
        {
          if (getPetControlItem() != null)
            summonPet();
        }
      });
  }

  public void summonPet()
  {
    if (getPet() != null) {
      return;
    }
    ItemInstance controlItem = getPetControlItem();
    if (controlItem == null) {
      return;
    }
    int npcId = PetDataTable.getSummonId(controlItem);
    if (npcId == 0) {
      return;
    }
    NpcTemplate petTemplate = NpcHolder.getInstance().getTemplate(npcId);
    if (petTemplate == null) {
      return;
    }
    PetInstance pet = PetInstance.restore(controlItem, petTemplate, this);
    if (pet == null) {
      return;
    }
    setPet(pet);
    pet.setTitle(getName());

    if (!pet.isRespawned())
    {
      pet.setCurrentHp(pet.getMaxHp(), false);
      pet.setCurrentMp(pet.getMaxMp());
      pet.setCurrentFed(pet.getMaxFed());
      pet.updateControlItem();
      pet.store();
    }

    pet.getInventory().restore();

    pet.setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
    pet.setReflection(getReflection());
    pet.spawnMe(Location.findPointToStay(this, 50, 70));
    pet.setRunning();
    pet.setFollowMode(true);
    pet.getInventory().validateItems();

    if ((pet instanceof PetBabyInstance))
      ((PetBabyInstance)pet).startBuffTask();
  }

  public Collection<TrapInstance> getTraps()
  {
    if (_traps == null)
      return null;
    Collection result = new ArrayList(getTrapsCount());

    for (Integer trapId : _traps.keySet())
    {
      TrapInstance trap;
      if ((trap = (TrapInstance)GameObjectsStorage.get((Long)_traps.get(trapId))) != null)
        result.add(trap);
      else
        _traps.remove(trapId); 
    }
    return result;
  }

  public int getTrapsCount()
  {
    return _traps == null ? 0 : _traps.size();
  }

  public void addTrap(TrapInstance trap)
  {
    if (_traps == null)
      _traps = new HashMap();
    _traps.put(Integer.valueOf(trap.getObjectId()), trap.getStoredId());
  }

  public void removeTrap(TrapInstance trap)
  {
    Map traps = _traps;
    if ((traps == null) || (traps.isEmpty()))
      return;
    traps.remove(Integer.valueOf(trap.getObjectId()));
  }

  public void destroyFirstTrap()
  {
    Map traps = _traps;
    if ((traps == null) || (traps.isEmpty())) {
      return;
    }
    Iterator i$ = traps.keySet().iterator(); if (i$.hasNext()) { Integer trapId = (Integer)i$.next();
      TrapInstance trap;
      if ((trap = (TrapInstance)GameObjectsStorage.get((Long)traps.get(trapId))) != null)
      {
        trap.deleteMe();
        return;
      }
      return;
    }
  }

  public void destroyAllTraps()
  {
    Map traps = _traps;
    if ((traps == null) || (traps.isEmpty()))
      return;
    List toRemove = new ArrayList();
    for (Integer trapId : traps.keySet())
      toRemove.add((TrapInstance)GameObjectsStorage.get((Long)traps.get(trapId)));
    for (TrapInstance t : toRemove)
      if (t != null)
        t.deleteMe();
  }

  public void setBlockCheckerArena(byte arena)
  {
    _handysBlockCheckerEventArena = arena;
  }

  public int getBlockCheckerArena()
  {
    return _handysBlockCheckerEventArena;
  }

  public PlayerListenerList getListeners()
  {
    if (listeners == null)
      synchronized (this)
      {
        if (listeners == null)
          listeners = new PlayerListenerList(this);
      }
    return (PlayerListenerList)listeners;
  }

  public PlayerStatsChangeRecorder getStatsRecorder()
  {
    if (_statsRecorder == null)
      synchronized (this)
      {
        if (_statsRecorder == null)
          _statsRecorder = new PlayerStatsChangeRecorder(this);
      }
    return (PlayerStatsChangeRecorder)_statsRecorder;
  }

  public int getHoursInGame()
  {
    _hoursInGame += 1;
    return _hoursInGame;
  }

  public void startHourlyTask()
  {
    _hourlyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GameObjectTasks.HourlyTask(this), 3600000L, 3600000L);
  }

  public void stopHourlyTask()
  {
    if (_hourlyTask != null)
    {
      _hourlyTask.cancel(false);
      _hourlyTask = null;
    }
  }

  public long getPremiumPoints()
  {
    if (Config.GAME_POINT_ITEM_ID > 0)
      return ItemFunctions.getItemCount(this, Config.GAME_POINT_ITEM_ID);
    return 0L;
  }

  public void reducePremiumPoints(int val)
  {
    if (Config.GAME_POINT_ITEM_ID > 0)
      ItemFunctions.removeItem(this, Config.GAME_POINT_ITEM_ID, val, true);
  }

  public boolean isAgathionResAvailable()
  {
    return _agathionResAvailable;
  }

  public void setAgathionRes(boolean val)
  {
    _agathionResAvailable = val;
  }

  public boolean isClanAirShipDriver()
  {
    return (isInBoat()) && (getBoat().isClanAirShip()) && (((ClanAirShip)getBoat()).getDriver() == this);
  }

  public String getSessionVar(String key)
  {
    if (_userSession == null)
      return null;
    return (String)_userSession.get(key);
  }

  public void setSessionVar(String key, String val)
  {
    if (_userSession == null) {
      _userSession = new ConcurrentHashMap();
    }
    if ((val == null) || (val.isEmpty()))
      _userSession.remove(key);
    else
      _userSession.put(key, val);
  }

  public FriendList getFriendList()
  {
    return _friendList;
  }

  public boolean isNotShowTraders()
  {
    return _notShowTraders;
  }

  public void setNotShowTraders(boolean notShowTraders)
  {
    _notShowTraders = notShowTraders;
  }

  public boolean isDebug()
  {
    return _debug;
  }

  public void setDebug(boolean b)
  {
    _debug = b;
  }

  public void sendItemList(boolean show)
  {
    ItemInstance[] items = getInventory().getItems();
    LockType lockType = getInventory().getLockType();
    int[] lockItems = getInventory().getLockItems();

    int allSize = items.length;
    int questItemsSize = 0;
    int agathionItemsSize = 0;
    for (ItemInstance item : items)
    {
      if (item.getTemplate().isQuest())
        questItemsSize++;
      if (item.getTemplate().getAgathionEnergy() > 0) {
        agathionItemsSize++;
      }
    }
    sendPacket(new ItemList(allSize - questItemsSize, items, show, lockType, lockItems));
    if (questItemsSize > 0)
      sendPacket(new ExQuestItemList(questItemsSize, items, lockType, lockItems));
    if (agathionItemsSize > 0)
      sendPacket(new ExBR_AgathionEnergyInfo(agathionItemsSize, items));
  }

  public int getBeltInventoryIncrease()
  {
    ItemInstance item = getInventory().getPaperdollItem(25);
    if ((item != null) && (item.getTemplate().getAttachedSkills() != null))
      for (Skill skill : item.getTemplate().getAttachedSkills())
        for (FuncTemplate func : skill.getAttachedFuncs())
          if (func._stat == Stats.INVENTORY_LIMIT)
            return (int)func._value;
    return 0;
  }

  public boolean isPlayer()
  {
    return true;
  }

  public boolean checkCoupleAction(Player target)
  {
    if (target.getPrivateStoreType() != 0)
    {
      sendPacket(new SystemMessage(3123).addName(target));
      return false;
    }
    if (target.isFishing())
    {
      sendPacket(new SystemMessage(3124).addName(target));
      return false;
    }
    if (target.isInCombat())
    {
      sendPacket(new SystemMessage(3125).addName(target));
      return false;
    }
    if (target.isCursedWeaponEquipped())
    {
      sendPacket(new SystemMessage(3127).addName(target));
      return false;
    }
    if (target.isInOlympiadMode())
    {
      sendPacket(new SystemMessage(3128).addName(target));
      return false;
    }
    if (target.isOnSiegeField())
    {
      sendPacket(new SystemMessage(3130).addName(target));
      return false;
    }
    if ((target.isInBoat()) || (target.getMountNpcId() != 0))
    {
      sendPacket(new SystemMessage(3131).addName(target));
      return false;
    }
    if (target.isTeleporting())
    {
      sendPacket(new SystemMessage(3132).addName(target));
      return false;
    }
    if (target.getTransformation() != 0)
    {
      sendPacket(new SystemMessage(3133).addName(target));
      return false;
    }
    if (target.isDead())
    {
      sendPacket(new SystemMessage(3139).addName(target));
      return false;
    }
    return true;
  }

  public void startAttackStanceTask()
  {
    startAttackStanceTask0();
    Summon summon = getPet();
    if (summon != null)
      summon.startAttackStanceTask0();
  }

  public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
  {
    super.displayGiveDamageMessage(target, damage, crit, miss, shld, magic);
    if (crit) {
      if (magic)
        sendPacket(new SystemMessage(1280).addName(this));
      else
        sendPacket(new SystemMessage(2266).addName(this));
    }
    if (miss)
      sendPacket(new SystemMessage(2265).addName(this));
    else if (!target.isDamageBlocked()) {
      sendPacket(new SystemMessage(2261).addName(this).addName(target).addNumber(damage));
    }
    if (target.isPlayer())
    {
      if ((shld) && (damage > 1))
        target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
      else if ((shld) && (damage == 1))
        target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
    }
  }

  public void displayReceiveDamageMessage(Creature attacker, int damage)
  {
    if (attacker != this)
      sendPacket(new SystemMessage(2262).addName(this).addName(attacker).addNumber(damage));
  }

  public IntObjectMap<String> getPostFriends()
  {
    return _postFriends;
  }

  public boolean isSharedGroupDisabled(int groupId)
  {
    TimeStamp sts = (TimeStamp)_sharedGroupReuses.get(groupId);
    if (sts == null)
      return false;
    if (sts.hasNotPassed())
      return true;
    _sharedGroupReuses.remove(groupId);
    return false;
  }

  public TimeStamp getSharedGroupReuse(int groupId)
  {
    return (TimeStamp)_sharedGroupReuses.get(groupId);
  }

  public void addSharedGroupReuse(int group, TimeStamp stamp)
  {
    _sharedGroupReuses.put(group, stamp);
  }

  public Collection<IntObjectMap.Entry<TimeStamp>> getSharedGroupReuses()
  {
    return _sharedGroupReuses.entrySet();
  }

  public void sendReuseMessage(ItemInstance item)
  {
    TimeStamp sts = getSharedGroupReuse(item.getTemplate().getReuseGroup());
    if ((sts == null) || (!sts.hasNotPassed())) {
      return;
    }
    long timeleft = sts.getReuseCurrent();
    long hours = timeleft / 3600000L;
    long minutes = (timeleft - hours * 3600000L) / 60000L;
    long seconds = ()Math.ceil((timeleft - hours * 3600000L - minutes * 60000L) / 1000.0D);

    if (hours > 0L)
      sendPacket(((SystemMessage2)((SystemMessage2)((SystemMessage2)new SystemMessage2(item.getTemplate().getReuseType().getMessages()[2]).addItemName(item.getTemplate().getItemId())).addInteger(hours)).addInteger(minutes)).addInteger(seconds));
    else if (minutes > 0L)
      sendPacket(((SystemMessage2)((SystemMessage2)new SystemMessage2(item.getTemplate().getReuseType().getMessages()[1]).addItemName(item.getTemplate().getItemId())).addInteger(minutes)).addInteger(seconds));
    else
      sendPacket(((SystemMessage2)new SystemMessage2(item.getTemplate().getReuseType().getMessages()[0]).addItemName(item.getTemplate().getItemId())).addInteger(seconds));
  }

  public NevitSystem getNevitSystem()
  {
    return _nevitSystem;
  }

  public void ask(ConfirmDlg dlg, OnAnswerListener listener)
  {
    if (_askDialog != null)
      return;
    int rnd = Rnd.nextInt();
    _askDialog = new ImmutablePair(Integer.valueOf(rnd), listener);
    dlg.setRequestId(rnd);
    sendPacket(dlg);
  }

  public Pair<Integer, OnAnswerListener> getAskListener(boolean clear)
  {
    if (!clear) {
      return _askDialog;
    }

    Pair ask = _askDialog;
    _askDialog = null;
    return ask;
  }

  public boolean isDead()
  {
    return (isInOlympiadMode()) || (isInDuel()) ? false : getCurrentHp() <= 1.0D ? true : super.isDead();
  }

  public int getAgathionEnergy()
  {
    ItemInstance item = getInventory().getPaperdollItem(18);
    return item == null ? 0 : item.getAgathionEnergy();
  }

  public void setAgathionEnergy(int val)
  {
    ItemInstance item = getInventory().getPaperdollItem(18);
    if (item == null)
      return;
    item.setAgathionEnergy(val);
    item.setJdbcState(JdbcEntityState.UPDATED);

    sendPacket(new ExBR_AgathionEnergyInfo(1, new ItemInstance[] { item }));
  }

  public boolean hasPrivilege(Privilege privilege)
  {
    return (_clan != null) && ((getClanPrivileges() & privilege.mask()) == privilege.mask());
  }

  public MatchingRoom getMatchingRoom()
  {
    return _matchingRoom;
  }

  public void setMatchingRoom(MatchingRoom matchingRoom)
  {
    _matchingRoom = matchingRoom;
  }

  public void dispelBuffs()
  {
    for (Effect e : getEffectList().getAllEffects())
      if ((!e.getSkill().isOffensive()) && (!e.getSkill().isNewbie()) && (e.isCancelable()) && (!e.getSkill().isPreservedOnDeath()))
      {
        sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
        e.exit();
      }
    if (getPet() != null)
      for (Effect e : getPet().getEffectList().getAllEffects())
        if ((!e.getSkill().isOffensive()) && (!e.getSkill().isNewbie()) && (e.isCancelable()) && (!e.getSkill().isPreservedOnDeath()))
          e.exit();
  }

  public void setInstanceReuse(int id, long time)
  {
    SystemMessage msg = new SystemMessage(2720).addString(getName());
    sendPacket(msg);
    _instancesReuses.put(Integer.valueOf(id), Long.valueOf(time));
    mysql.set("REPLACE INTO character_instances (obj_id, id, reuse) VALUES (?,?,?)", new Object[] { Integer.valueOf(getObjectId()), Integer.valueOf(id), Long.valueOf(time) });
  }

  public void removeInstanceReuse(int id)
  {
    if (_instancesReuses.remove(Integer.valueOf(id)) != null)
      mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=? AND `id`=? LIMIT 1", new Object[] { Integer.valueOf(getObjectId()), Integer.valueOf(id) });
  }

  public void removeAllInstanceReuses()
  {
    _instancesReuses.clear();
    mysql.set("DELETE FROM `character_instances` WHERE `obj_id`=?", new Object[] { Integer.valueOf(getObjectId()) });
  }

  public void removeInstanceReusesByGroupId(int groupId)
  {
    for (Iterator i$ = InstantZoneHolder.getInstance().getSharedReuseInstanceIdsByGroup(groupId).iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
      if (getInstanceReuse(i) != null)
        removeInstanceReuse(i); }
  }

  public Long getInstanceReuse(int id)
  {
    return (Long)_instancesReuses.get(Integer.valueOf(id));
  }

  public Map<Integer, Long> getInstanceReuses()
  {
    return _instancesReuses;
  }

  private void loadInstanceReuses()
  {
    Connection con = null;
    PreparedStatement offline = null;
    ResultSet rs = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      offline = con.prepareStatement("SELECT * FROM character_instances WHERE obj_id = ?");
      offline.setInt(1, getObjectId());
      rs = offline.executeQuery();
      while (rs.next())
      {
        int id = rs.getInt("id");
        long reuse = rs.getLong("reuse");
        _instancesReuses.put(Integer.valueOf(id), Long.valueOf(reuse));
      }
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, offline, rs);
    }
  }

  public Reflection getActiveReflection()
  {
    for (Reflection r : ReflectionManager.getInstance().getAll())
      if ((r != null) && (ArrayUtils.contains(r.getVisitors(), getObjectId())))
        return r;
    return null;
  }

  public boolean canEnterInstance(int instancedZoneId)
  {
    InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);

    if (isDead()) {
      return false;
    }
    if (ReflectionManager.getInstance().size() > Config.MAX_REFLECTIONS_COUNT)
    {
      sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
      return false;
    }

    if (iz == null)
    {
      sendPacket(SystemMsg.SYSTEM_ERROR);
      return false;
    }

    if (ReflectionManager.getInstance().getCountByIzId(instancedZoneId) >= iz.getMaxChannels())
    {
      sendPacket(SystemMsg.THE_MAXIMUM_NUMBER_OF_INSTANCE_ZONES_HAS_BEEN_EXCEEDED);
      return false;
    }

    return iz.getEntryType().canEnter(this, iz);
  }

  public boolean canReenterInstance(int instancedZoneId)
  {
    InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
    if ((getActiveReflection() != null) && (getActiveReflection().getInstancedZoneId() != instancedZoneId))
    {
      sendPacket(SystemMsg.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
      return false;
    }
    if (iz.isDispelBuffs())
      dispelBuffs();
    return iz.getEntryType().canReEnter(this, iz);
  }

  public int getBattlefieldChatId()
  {
    return _battlefieldChatId;
  }

  public void setBattlefieldChatId(int battlefieldChatId)
  {
    _battlefieldChatId = battlefieldChatId;
  }

  public void broadCast(IStaticPacket[] packet)
  {
    sendPacket(packet);
  }

  public Iterator<Player> iterator()
  {
    return Collections.singleton(this).iterator();
  }

  public PlayerGroup getPlayerGroup()
  {
    if (getParty() != null)
    {
      if (getParty().getCommandChannel() != null) {
        return getParty().getCommandChannel();
      }
      return getParty();
    }

    return this;
  }

  public boolean isActionBlocked(String action)
  {
    return _blockedActions.contains(action);
  }

  public void blockActions(String[] actions)
  {
    Collections.addAll(_blockedActions, actions);
  }

  public void unblockActions(String[] actions)
  {
    for (String action : actions)
      _blockedActions.remove(action);
  }

  public OlympiadGame getOlympiadGame()
  {
    return _olympiadGame;
  }

  public void setOlympiadGame(OlympiadGame olympiadGame)
  {
    _olympiadGame = olympiadGame;
  }

  public OlympiadGame getOlympiadObserveGame()
  {
    return _olympiadObserveGame;
  }

  public void setOlympiadObserveGame(OlympiadGame olympiadObserveGame)
  {
    _olympiadObserveGame = olympiadObserveGame;
  }

  public void addRadar(int x, int y, int z)
  {
    sendPacket(new RadarControl(0, 1, x, y, z));
  }

  public void addRadarWithMap(int x, int y, int z)
  {
    sendPacket(new RadarControl(0, 2, x, y, z));
  }

  public PetitionMainGroup getPetitionGroup()
  {
    return _petitionGroup;
  }

  public void setPetitionGroup(PetitionMainGroup petitionGroup)
  {
    _petitionGroup = petitionGroup;
  }

  public int getLectureMark()
  {
    return _lectureMark;
  }

  public void setLectureMark(int lectureMark)
  {
    _lectureMark = lectureMark;
  }

  private class UserInfoTask extends RunnableImpl
  {
    private UserInfoTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      Player.this.sendUserInfoImpl();
      Player.access$602(Player.this, null);
    }
  }

  public class BroadcastCharInfoTask extends RunnableImpl
  {
    public BroadcastCharInfoTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      Player.this.broadcastCharInfoImpl();
      Player.access$402(Player.this, null);
    }
  }

  private class UpdateEffectIcons extends RunnableImpl
  {
    private UpdateEffectIcons()
    {
    }

    public void runImpl()
      throws Exception
    {
      Player.this.updateEffectIconsImpl();
      Player.access$102(Player.this, null);
    }
  }
}