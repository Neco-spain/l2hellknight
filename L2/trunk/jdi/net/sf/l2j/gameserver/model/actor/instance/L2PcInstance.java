package net.sf.l2j.gameserver.model.actor.instance;

import java.io.PrintStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2PlayerAI;
import net.sf.l2j.gameserver.ai.special.Sailren;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.cache.WarehouseCacheManager;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.FishTable;
import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.datatables.HennaTreeTable;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.NobleSkillTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.skillhandlers.SiegeFlag;
import net.sf.l2j.gameserver.handler.skillhandlers.StrSiegeAssault;
import net.sf.l2j.gameserver.handler.skillhandlers.TakeCastle;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.instancemanager.DuelManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.ChanceSkillList;
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
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Radar;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2Request;
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
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassLevel;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.quest.State;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CameraMode;
import net.sf.l2j.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExFishingStart;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMode;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadUserInfoSpectator;
import net.sf.l2j.gameserver.network.serverpackets.ExPCCafePointInfo;
import net.sf.l2j.gameserver.network.serverpackets.ExSetCompassZoneCode;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ObservationMode;
import net.sf.l2j.gameserver.network.serverpackets.ObservationReturn;
import net.sf.l2j.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PetInventoryUpdate;
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
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.SkillList;
import net.sf.l2j.gameserver.network.serverpackets.Snoop;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.TitleUpdate;
import net.sf.l2j.gameserver.network.serverpackets.TradePressOtherOk;
import net.sf.l2j.gameserver.network.serverpackets.TradePressOwnOk;
import net.sf.l2j.gameserver.network.serverpackets.TradeStart;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
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
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;
import net.sf.protection.nProtect;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOConnection;

public final class L2PcInstance extends L2PlayableInstance
{
  private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
  private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
  private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?";
  private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
  private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
  private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?)";
  private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay FROM character_skills_save WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC";
  private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
  private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,last_recom_date=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,banchat_time=?,pccafe=?,name_color=?,title_color=? WHERE obj_id=?";
  private static final String RESTORE_CHARACTER = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,banchat_time,pccafe,name_color,title_color FROM characters WHERE obj_id=?";
  private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
  private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
  private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
  private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
  private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
  private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
  private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
  private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
  private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
  private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
  private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
  private static final String DELETE_CHAR_RECOMS = "DELETE FROM character_recommends WHERE char_id=?";
  private static final String INSERT_PREMIUMSERVICE = "INSERT INTO account_premium (account_name,premium_service,enddate) values(?,?,?)";
  private static final String RESTORE_PREMIUMSERVICE = "SELECT premium_service,enddate FROM account_premium WHERE account_name=?";
  private static final String UPDATE_PREMIUMSERVICE = "UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?";
  public static final int REQUEST_TIMEOUT = 15;
  public int _pvp_realcount;
  public static final int STORE_PRIVATE_NONE = 0;
  public static final int STORE_PRIVATE_SELL = 1;
  public static final int STORE_PRIVATE_BUY = 3;
  public static final int STORE_PRIVATE_MANUFACTURE = 5;
  public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
  private static final int[] EXPERTISE_LEVELS = { SkillTreeTable.getInstance().getExpertiseLevel(0), SkillTreeTable.getInstance().getExpertiseLevel(1), SkillTreeTable.getInstance().getExpertiseLevel(2), SkillTreeTable.getInstance().getExpertiseLevel(3), SkillTreeTable.getInstance().getExpertiseLevel(4), SkillTreeTable.getInstance().getExpertiseLevel(5) };

  private static final int[] COMMON_CRAFT_LEVELS = { 5, 20, 28, 36, 43, 49, 55, 62 };
  private L2GameClient _client;
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
  private int _pkKills;
  private byte _pvpFlag;
  private byte _siegeState = 0;

  private int _curWeightPenalty = 0;
  private int _lastCompassZone;
  private byte _zoneValidateCounter = 4;

  private boolean _isIn7sDungeon = false;

  private boolean _inJail = false;
  private long _jailTimer = 0L;
  private ScheduledFuture _jailTask;
  private boolean _inOlympiadMode = false;
  private boolean _OlympiadStart = false;
  private int _olympiadGameId = -1;
  private int _olympiadSide = -1;
  public int dmgDealt = 0;
  private boolean _isInDuel = false;
  private int _duelState = 0;
  private int _duelId = 0;
  private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
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
  private boolean _observerMode = false;

  private Point3D _lastClientPosition = new Point3D(0, 0, 0);
  private Point3D _lastServerPosition = new Point3D(0, 0, 0);
  private Point3D _lastPartyPosition = new Point3D(0, 0, 0);
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
  private int _newbie;
  private boolean _noble = false;
  private boolean _hero = false;

  private L2FolkInstance _lastFolkNpc = null;

  private int _questNpcObject = 0;

  private Map<String, QuestState> _quests = new FastMap();

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
  private boolean _partyMatchingAutomaticRegistration;
  private boolean _partyMatchingShowLevel;
  private boolean _partyMatchingShowClass;
  private String _partyMatchingMemo;
  private int _clanId;
  public String _tvtIp;
  private L2Clan _clan;
  private int _apprentice = 0;
  private int _sponsor = 0;
  public boolean _allowTrade = true;
  private long _clanJoinExpiryTime;
  private long _clanCreateExpiryTime;
  private int _powerGrade = 0;
  private int _clanPrivileges = 0;

  private long _chatBanTimer = 0L;
  private ScheduledFuture _chatBanTask = null;

  private int _pledgeClass = 0;
  private int _pledgeType = 0;

  private int _lvlJoinedAcademy = 0;

  private int _wantsPeace = 0;

  private int _deathPenaltyBuffLevel = 0;
  private Point3D _currentSkillWorldPosition;
  private boolean _isGm;
  private int _accessLevel;
  private boolean _chatBanned = false;
  private ScheduledFuture _chatUnbanTask = null;
  private boolean _messageRefusal = false;
  private boolean _dietMode = false;
  private boolean _tradeRefusal = false;
  private boolean _exchangeRefusal = false;
  boolean sittingTaskLaunched;
  private L2Party _party;
  private int pcCafeScore;
  private boolean isInDangerArea;
  private L2PcInstance _activeRequester;
  private long _requestExpireTime = 0L;
  private L2Request _request = new L2Request(this);
  private L2ItemInstance _arrowItem;
  private long _protectEndTime = 0L;

  private long _recentFakeDeathEndTime = 0L;
  private L2Weapon _fistsWeaponItem;
  private final Map<Integer, String> _chars = new FastMap();
  private int _expertiseIndex;
  private int _expertisePenalty = 0;

  private L2ItemInstance _activeEnchantItem = null;

  protected boolean _inventoryDisable = false;

  private boolean _isEnchanting = false;
  protected Map<Integer, L2CubicInstance> _cubics = new FastMap().setShared(true);

  protected Map<Integer, Integer> _activeSoulShots = new FastMap().setShared(true);

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

  private boolean _isOffline = false;

  private boolean _isTradeOff = false;
  private int _isBBMult;
  private long _offlineShopStart = 0L;

  public int _originalNameColorOffline = 0;
  public static double curHp;
  public static double curMp;
  public static double curCp;
  public boolean _useAutoLoot = Config.AUTO_LOOT;
  public String _teamNameCTF;
  public String _teamNameHaveFlagCTF;
  public String _originalTitleCTF;
  public int _originalKarmaCTF;
  public int _countCTFflags;
  public boolean _inEventCTF = false; public boolean _haveFlagCTF = false;

  public Future _posCheckerCTF = null;

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
  private ScheduledFuture _taskRentPet;
  private ScheduledFuture _taskUnmount;
  private ScheduledFuture _taskWater;
  private List<String> _validBypass = new FastList();
  private List<String> _validBypass2 = new FastList();
  private Forum _forumMail;
  private Forum _forumMemo;
  private SkillDat _currentSkill;
  private SkillDat _queuedSkill;
  private boolean _IsWearingFormalWear = false;

  private int _cursedWeaponEquipedId = 0;
  private int _tvtkills = 0;
  private int _reviveRequested = 0;
  private double _revivePower = 0.0D;
  private boolean _revivePet = false;

  private double _cpUpdateIncCheck = 0.0D;
  private double _cpUpdateDecCheck = 0.0D;
  private double _cpUpdateInterval = 0.0D;
  private double _mpUpdateIncCheck = 0.0D;
  private double _mpUpdateDecCheck = 0.0D;
  private double _mpUpdateInterval = 0.0D;
  private int _instanceId = 0;

  private int _herbstask = 0;

  private boolean _married = false;
  private int _partnerId = 0;
  private int _coupleId = 0;
  private boolean _engagerequest = false;
  private int _engageid = 0;
  private boolean _marryrequest = false;
  private boolean _marryaccepted = false;

  private summonRequest _summonRequest = new summonRequest();

  private gatesRequest _gatesRequest = new gatesRequest();
  protected ForceBuff _forceBuff;
  private ScheduledFuture _taskWarnUserTakeBreak;
  public ScheduledFuture _taskforfish;
  private FishData _fish;
  private L2ItemInstance _lure = null;

  private boolean _charmOfCourage = false;

  private FastMap<Integer, TimeStamp> ReuseTimeStamps = new FastMap().setShared(true);

  private boolean _expGainOn = true;
  private boolean _clientkey;
  public boolean _anim = false;

  private HashMap<Integer, Long> confirmDlgRequests = new HashMap();

  public void startForceBuff(L2Character target, L2Skill skill)
  {
    if (!(target instanceof L2PcInstance)) return;

    if (skill.getSkillType() != L2Skill.SkillType.FORCE_BUFF) {
      return;
    }
    if (_forceBuff == null)
      _forceBuff = new ForceBuff(this, (L2PcInstance)target, skill);
  }

  public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, boolean sex)
  {
    PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
    L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);

    player.setName(name);

    player.setBaseClass(player.getClassId());
    player.setNewbie(1);

    boolean ok = player.createDb();

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

  public String getAccountName()
  {
    if (getClient() != null) {
      return getClient().getAccountName();
    }
    return _accountName;
  }

  public Map<Integer, String> getAccountChars()
  {
    return _chars;
  }

  public int getRelation(L2PcInstance target)
  {
    int result = 0;

    if (getPvpFlag() != 0)
      result |= 2;
    if (getKarma() > 0) {
      result |= 4;
    }
    if (isClanLeader()) {
      result |= 128;
    }
    if (getSiegeState() != 0)
    {
      result |= 512;
      if (getSiegeState() != target.getSiegeState())
        result |= 4096;
      else
        result |= 2048;
      if (getSiegeState() == 1) {
        result |= 1024;
      }
    }
    if ((getClan() != null) && (target.getClan() != null))
    {
      if ((target.getPledgeType() != -1) && (target.getClan().isAtWarWith(Integer.valueOf(getClan().getClanId()))))
      {
        result |= 65536;
        if (getClan().isAtWarWith(Integer.valueOf(target.getClan().getClanId())))
          result |= 32768;
      }
    }
    return result;
  }

  public static L2PcInstance load(int objectId)
  {
    return restore(objectId);
  }

  private void initPcStatusUpdateValues()
  {
    _cpUpdateInterval = (getMaxCp() / 352.0D);
    _cpUpdateIncCheck = getMaxCp();
    _cpUpdateDecCheck = (getMaxCp() - _cpUpdateInterval);
    _mpUpdateInterval = (getMaxMp() / 352.0D);
    _mpUpdateIncCheck = getMaxMp();
    _mpUpdateDecCheck = (getMaxMp() - _mpUpdateInterval);
  }

  private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
  {
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
    pcCafeScore = 0;
    isInDangerArea = false;

    getInventory().restore();
    if (!Config.WAREHOUSE_CACHE)
      getWarehouse();
    getFreight().restore();
  }

  private L2PcInstance(int objectId)
  {
    super(objectId, null);

    getKnownList();
    getStat();
    getStatus();
    pcCafeScore = 0;
    isInDangerArea = false;
    super.initCharStatusUpdateValues();
    initPcStatusUpdateValues();
  }

  public final PcKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof PcKnownList)))
      setKnownList(new PcKnownList(this));
    return (PcKnownList)super.getKnownList();
  }

  public final PcStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof PcStat)))
      setStat(new PcStat(this));
    return (PcStat)super.getStat();
  }

  public final PcStatus getStatus()
  {
    if ((super.getStatus() == null) || (!(super.getStatus() instanceof PcStatus)))
      setStatus(new PcStatus(this));
    return (PcStatus)super.getStatus();
  }

  public final PcAppearance getAppearance()
  {
    return _appearance;
  }

  public final L2PcTemplate getBaseTemplate()
  {
    return CharTemplateTable.getInstance().getTemplate(_baseClass);
  }
  public final L2PcTemplate getTemplate() {
    return (L2PcTemplate)super.getTemplate();
  }
  public void setTemplate(ClassId newclass) { super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      synchronized (this)
      {
        if (_ai == null)
          _ai = new L2PlayerAI(new AIAccessor());
      }
    }
    return _ai;
  }

  public void explore()
  {
    if (!_exploring) return;

    if (getMountType() == 2) {
      return;
    }
    int x = getX() + Rnd.nextInt(6000) - 3000;
    int y = getY() + Rnd.nextInt(6000) - 3000;

    if (x > 194327) x = 194327;
    if (x < -127900) x = -127900;
    if (y > 259536) y = 259536;
    if (y < -30000) y = -30000;

    int z = getZ();

    L2CharPosition pos = new L2CharPosition(x, y, z, 0);

    getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
  }

  public final int getLevel() {
    return getStat().getLevel();
  }
  public int isNewbie() {
    return getNewbie();
  }

  public void setNewbie(int newbieRewards)
  {
    _newbie = newbieRewards;
  }

  public int getNewbie()
  {
    return _newbie;
  }

  public void setBaseClass(int baseClass)
  {
    _baseClass = baseClass;
  }

  public void setBaseClass(ClassId classId)
  {
    _baseClass = classId.ordinal();
  }
  public boolean isInStoreMode() {
    return getPrivateStoreType() > 0;
  }
  public boolean isInCraftMode() {
    return _inCraftMode;
  }

  public void isInCraftMode(boolean b)
  {
    _inCraftMode = b;
  }

  public void logout()
  {
    closeNetConnection(true);
    System.out.println(new StringBuilder().append(getName()).append(" offtrade logout").toString());
  }

  public L2RecipeList[] getCommonRecipeBook()
  {
    return (L2RecipeList[])_commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
  }

  public L2RecipeList[] getDwarvenRecipeBook() {
    return (L2RecipeList[])_dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
  }

  public void registerCommonRecipeList(L2RecipeList recipe) {
    _commonRecipeBook.put(Integer.valueOf(recipe.getId()), recipe);
  }

  public void registerDwarvenRecipeList(L2RecipeList recipe) {
    _dwarvenRecipeBook.put(Integer.valueOf(recipe.getId()), recipe);
  }

  public boolean hasRecipeList(int recipeId) {
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
    L2ShortCut[] allShortCuts = getAllShortCuts();

    for (L2ShortCut sc : allShortCuts)
    {
      if ((sc != null) && (sc.getId() == recipeId) && (sc.getType() == 5))
        deleteShortCut(sc.getSlot(), sc.getPage());
    }
  }

  public int getLastQuestNpcObject()
  {
    return _questNpcObject;
  }

  public void setLastQuestNpcObject(int npcId)
  {
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
    for (int i = 0; i < len; i++)
      tmp[i] = questStateArray[i];
    tmp[len] = state;
    return tmp;
  }

  public Quest[] getAllActiveQuests()
  {
    FastList quests = new FastList();

    for (QuestState qs : _quests.values())
    {
      if ((qs.getQuest().getQuestIntId() >= 1999) || 
        ((qs.isCompleted()) && (!Config.DEVELOPER)) || (
        (!qs.isStarted()) && (!Config.DEVELOPER))) {
        continue;
      }
      quests.add(qs.getQuest());
    }

    return (Quest[])quests.toArray(new Quest[quests.size()]);
  }

  public QuestState[] getQuestsForAttacks(L2NpcInstance npc)
  {
    QuestState[] states = null;

    for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
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

    for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
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

    Quest[] quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.ON_TALK);
    if (quests != null)
    {
      for (Quest quest : quests)
      {
        if (quest == null)
          continue;
        if (getQuestState(quest.getName()) == null)
          continue;
        if (states == null)
          states = new QuestState[] { getQuestState(quest.getName()) };
        else {
          states = addToQuestStateArray(states, getQuestState(quest.getName()));
        }
      }

    }

    return states;
  }

  public QuestState processQuestEvent(String quest, String event)
  {
    QuestState retval = null;
    if (event == null)
      event = "";
    if (!_quests.containsKey(quest))
      return retval;
    QuestState qs = getQuestState(quest);
    if ((qs == null) && (event.length() == 0))
      return retval;
    if (qs == null) {
      Quest q = QuestManager.getInstance().getQuest(quest);
      if (q == null)
        return retval;
      qs = q.newQuestState(this);
    }
    if ((qs != null) && 
      (getLastQuestNpcObject() > 0))
    {
      L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
      if (((object instanceof L2NpcInstance)) && (isInsideRadius(object, 150, false, false)))
      {
        L2NpcInstance npc = (L2NpcInstance)object;
        QuestState[] states = getQuestsForTalk(npc.getNpcId());

        if (states != null)
        {
          for (QuestState state : states)
          {
            if ((state.getQuest().getQuestIntId() != qs.getQuest().getQuestIntId()) || (qs.isCompleted()))
              continue;
            if (qs.getQuest().notifyEvent(event, npc, this)) {
              showQuestWindow(quest, State.getStateName(qs.getState()));
            }
            retval = qs;
          }

          sendPacket(new QuestList());
        }
      }

    }

    return retval;
  }

  private void showQuestWindow(String questId, String stateId)
  {
    String path = new StringBuilder().append("data/scripts/quests/").append(questId).append("/").append(stateId).append(".htm").toString();
    String content = HtmCache.getInstance().getHtm(path);

    if (content != null)
    {
      NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
      npcReply.setHtml(content);
      sendPacket(npcReply);
    }

    sendPacket(new ActionFailed());
  }

  public L2ShortCut[] getAllShortCuts()
  {
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
    return _pvpFlag;
  }

  public void revalidateZone(boolean force)
  {
    if (getWorldRegion() == null) return;

    if (force) { _zoneValidateCounter = 4;
    } else
    {
      _zoneValidateCounter = (byte)(_zoneValidateCounter - 1);
      if (_zoneValidateCounter < 0)
        _zoneValidateCounter = 4;
      else return;
    }

    getWorldRegion().revalidateZones(this);

    if (Config.ALLOW_WATER) {
      checkWaterState();
    }
    if (isInsideZone(4))
    {
      if (_lastCompassZone == 11) return;
      _lastCompassZone = 11;
      ExSetCompassZoneCode cz = new ExSetCompassZoneCode(11);
      sendPacket(cz);
    }
    else if (isInsideZone(1))
    {
      if (_lastCompassZone == 14) return;
      _lastCompassZone = 14;
      ExSetCompassZoneCode cz = new ExSetCompassZoneCode(14);
      sendPacket(cz);
    }
    else if (isIn7sDungeon())
    {
      if (_lastCompassZone == 13) return;
      _lastCompassZone = 13;
      ExSetCompassZoneCode cz = new ExSetCompassZoneCode(13);
      sendPacket(cz);
    }
    else if (isInsideZone(2))
    {
      if (_lastCompassZone == 12) return;
      _lastCompassZone = 12;
      ExSetCompassZoneCode cz = new ExSetCompassZoneCode(12);
      sendPacket(cz);
    }
    else if (isInDangerArea())
    {
      if (_lastCompassZone == 8) return;
      _lastCompassZone = 8;
      ExSetCompassZoneCode cz = new ExSetCompassZoneCode(8);
      sendPacket(cz);
    }
    else
    {
      if (_lastCompassZone == 15) return;
      if (_lastCompassZone == 11) updatePvPStatus();
      _lastCompassZone = 15;
      ExSetCompassZoneCode cz = new ExSetCompassZoneCode(15);
      sendPacket(cz);
    }
  }

  public boolean hasDwarvenCraft()
  {
    return getSkillLevel(172) >= 1;
  }

  public int getDwarvenCraft()
  {
    return getSkillLevel(172);
  }

  public boolean hasCommonCraft()
  {
    return getSkillLevel(1320) >= 1;
  }

  public int getCommonCraft()
  {
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
    if (Config.ALT_RECOMMEND)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)");
        statement.setInt(1, getObjectId());
        statement.setInt(2, target.getObjectId());
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning(new StringBuilder().append("could not update char recommendations:").append(e).toString());
      }
      finally {
        try {
          con.close(); } catch (Exception e) {
        }
      }
    }
    target.incRecomHave();
    decRecomLeft();
    _recomChars.add(Integer.valueOf(target.getObjectId()));
  }

  public boolean canRecom(L2PcInstance target)
  {
    return !_recomChars.contains(Integer.valueOf(target.getObjectId()));
  }

  public void setExpBeforeDeath(long exp)
  {
    _expBeforeDeath = exp;
  }

  public long getExpBeforeDeath()
  {
    return _expBeforeDeath;
  }

  public int getKarma()
  {
    return _karma;
  }

  public void setKarma(int karma)
  {
    if (karma < 0) karma = 0;
    if ((_karma == 0) && (karma > 0))
    {
      for (L2Object object : getKnownList().getKnownObjects().values())
      {
        if ((object == null) || (!(object instanceof L2GuardInstance)))
          continue;
        if (((L2GuardInstance)object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
          ((L2GuardInstance)object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
      }
    }
    else if ((_karma > 0) && (karma == 0))
    {
      setKarmaFlag(0);
    }

    _karma = karma;
    broadcastKarma();
  }

  public int getMaxLoad()
  {
    int con = getCON();
    if (con < 1) return 31000;
    if (con > 59) return 176000;
    double baseLoad = Math.pow(1.029993928D, con) * 30495.627366000001D;
    return (int)calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null);
  }

  public int getExpertisePenalty()
  {
    return _expertisePenalty;
  }

  public int getWeightPenalty()
  {
    if (_dietMode)
      return 0;
    return _curWeightPenalty;
  }

  public void refreshOverloaded()
  {
    int maxLoad = getMaxLoad();
    if (maxLoad > 0)
    {
      setIsOverloaded(getCurrentLoad() > maxLoad);
      int weightproc = getCurrentLoad() * 1000 / maxLoad;
      int newWeightPenalty;
      int newWeightPenalty;
      if ((weightproc < 500) || (_dietMode))
      {
        newWeightPenalty = 0;
      }
      else
      {
        int newWeightPenalty;
        if (weightproc < 666)
        {
          newWeightPenalty = 1;
        }
        else
        {
          int newWeightPenalty;
          if (weightproc < 800)
          {
            newWeightPenalty = 2;
          }
          else
          {
            int newWeightPenalty;
            if (weightproc < 1000)
            {
              newWeightPenalty = 3;
            }
            else
            {
              newWeightPenalty = 4;
            }
          }
        }
      }
      if (_curWeightPenalty != newWeightPenalty)
      {
        _curWeightPenalty = newWeightPenalty;
        if ((newWeightPenalty > 0) && (!_dietMode))
        {
          super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
        }
        else
        {
          super.removeSkill(getKnownSkill(4270));
        }

        sendPacket(new EtcStatusUpdate(this));
        Broadcast.toKnownPlayers(this, new CharInfo(this));
      }
    }
  }

  public void refreshExpertisePenalty()
  {
    if (!Config.EXPERTISE_PENALTY) return;
    int newPenalty = 0;

    for (L2ItemInstance item : getInventory().getItems())
    {
      if ((item == null) || (!item.isEquipped()))
        continue;
      int crystaltype = item.getItem().getCrystalType();

      if (crystaltype > newPenalty) {
        newPenalty = crystaltype;
      }
    }

    newPenalty -= getExpertiseIndex();

    if (newPenalty <= 0) {
      newPenalty = 0;
    }
    if (getExpertisePenalty() != newPenalty)
    {
      _expertisePenalty = newPenalty;

      if (newPenalty > 0)
        super.addSkill(SkillTable.getInstance().getInfo(4267, 1));
      else {
        super.removeSkill(getKnownSkill(4267));
      }
      sendPacket(new EtcStatusUpdate(this));
    }
  }

  public void checkIfWeaponIsAllowed()
  {
    if (isGM()) {
      return;
    }
    L2Effect[] effects = getAllEffects();

    for (L2Effect currenteffect : effects)
    {
      L2Skill effectSkill = currenteffect.getSkill();

      if ((effectSkill.isOffensive()) || ((effectSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY) && (effectSkill.getSkillType() == L2Skill.SkillType.BUFF))) {
        continue;
      }
      if (effectSkill.getWeaponDependancy(this))
        continue;
      sendMessage(new StringBuilder().append(effectSkill.getName()).append(" cannot be used with this weapon.").toString());
      currenteffect.exit();
    }
  }

  public void checkSSMatch(L2ItemInstance equipped, L2ItemInstance unequipped)
  {
    if (unequipped == null) {
      return;
    }
    if ((unequipped.getItem().getType2() == 0) && ((equipped == null) || (equipped.getItem().getCrystalType() != unequipped.getItem().getCrystalType())))
    {
      for (L2ItemInstance ss : getInventory().getItems())
      {
        int _itemId = ss.getItemId();

        if (((_itemId != 5789) && (_itemId != 5790)) || (ss.getItem().getCrystalType() != unequipped.getItem().getCrystalType()))
        {
          continue;
        }

        sendPacket(new ExAutoSoulShot(_itemId, 0));

        SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
        sm.addString(ss.getItemName());
        sendPacket(sm);
      }
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

  public void setClassId(int Id)
  {
    if ((getLvlJoinedAcademy() != 0) && (_clan != null) && (PlayerClass.values()[Id].getLevel() == ClassLevel.Third))
    {
      if (getLvlJoinedAcademy() <= 16) _clan.setReputationScore(_clan.getReputationScore() + 400, true);
      else if (getLvlJoinedAcademy() >= 39) _clan.setReputationScore(_clan.getReputationScore() + 170, true); else
        _clan.setReputationScore(_clan.getReputationScore() + (400 - (getLvlJoinedAcademy() - 16) * 10), true);
      _clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
      setLvlJoinedAcademy(0);

      SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
      msg.addString(getName());
      _clan.broadcastToOnlineMembers(msg);
      _clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
      _clan.removeClanMember(getName(), 0L);
      sendPacket(new SystemMessage(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED));

      getInventory().addItem("Gift", 8181, 1, this, null);
      getInventory().updateDatabase();
    }
    if (isSubClassActive())
    {
      ((SubClass)getSubClasses().get(Integer.valueOf(_classIndex))).setClassId(Id);
    }
    doCast(SkillTable.getInstance().getInfo(5103, 1));
    setClassTemplate(Id);
  }

  public long getExp() {
    return getStat().getExp();
  }

  public void setActiveEnchantItem(L2ItemInstance scroll)
  {
    if (scroll == null) {
      setIsEnchanting(false);
    }
    _activeEnchantItem = scroll;
  }

  public L2ItemInstance getActiveEnchantItem()
  {
    return _activeEnchantItem;
  }

  public void setIsEnchanting(boolean val)
  {
    _isEnchanting = val;
  }

  public boolean isEnchanting() {
    return _isEnchanting;
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
    }
    else if ((classId >= 10) && (classId <= 17))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(251);
      weaponItem = (L2Weapon)temp;
    }
    else if ((classId >= 18) && (classId <= 24))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(244);
      weaponItem = (L2Weapon)temp;
    }
    else if ((classId >= 25) && (classId <= 30))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(249);
      weaponItem = (L2Weapon)temp;
    }
    else if ((classId >= 31) && (classId <= 37))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(245);
      weaponItem = (L2Weapon)temp;
    }
    else if ((classId >= 38) && (classId <= 43))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(250);
      weaponItem = (L2Weapon)temp;
    }
    else if ((classId >= 44) && (classId <= 48))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(248);
      weaponItem = (L2Weapon)temp;
    }
    else if ((classId >= 49) && (classId <= 52))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(252);
      weaponItem = (L2Weapon)temp;
    }
    else if ((classId >= 53) && (classId <= 57))
    {
      L2Item temp = ItemTable.getInstance().getTemplate(247);
      weaponItem = (L2Weapon)temp;
    }

    return weaponItem;
  }

  public void rewardSkills()
  {
    int lvl = getLevel();

    if (lvl >= 10)
    {
      L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
      skill = removeSkill(skill);
    }

    for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
    {
      if (lvl >= EXPERTISE_LEVELS[i]) {
        setExpertiseIndex(i);
      }
    }

    if (getExpertiseIndex() > 0)
    {
      L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
      addSkill(skill, true);
    }

    if ((getSkillLevel(1321) < 1) && (getRace() == Race.dwarf))
    {
      L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);
      addSkill(skill, true);
    }

    if (getSkillLevel(1322) < 1)
    {
      L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);
      addSkill(skill, true);
    }

    for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
    {
      if ((lvl < COMMON_CRAFT_LEVELS[i]) || (getSkillLevel(1320) >= i + 1))
        continue;
      L2Skill skill = SkillTable.getInstance().getInfo(1320, i + 1);
      addSkill(skill, true);
    }

    if (Config.AUTO_LEARN_SKILLS)
    {
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

    if ((getClan() != null) && (getClan().getReputationScore() >= 0))
    {
      L2Skill[] skills = getClan().getAllSkills();
      for (L2Skill sk : skills)
      {
        if (sk.getMinPledgeClass() <= getPledgeClass()) {
          addSkill(sk, false);
        }
      }
    }
    if (getClan() != null)
    {
      if ((getClan().getLevel() > 3) && (isClanLeader())) {
        SiegeManager.getInstance().addSiegeSkills(this);
      }
    }

    getInventory().reloadEquippedItems();
  }

  public void giveAvailableSkills()
  {
    int unLearnable = 0;
    int skillCounter = 0;

    L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
    while (skills.length > unLearnable)
    {
      unLearnable = 0;
      for (L2SkillLearn s : skills)
      {
        L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
        if ((sk == null) || (!sk.getCanLearn(getClassId())))
        {
          unLearnable++;
        }
        else
        {
          if (getSkillLevel(sk.getId()) == -1)
          {
            skillCounter++;
          }

          if (sk.isToggle())
          {
            L2Effect toggleEffect = getFirstEffect(sk.getId());
            if (toggleEffect != null)
            {
              toggleEffect.exit();
              sk.getEffects(this, this);
            }
          }

          addSkill(sk, true);
        }
      }

      skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
    }

    sendMessage(new StringBuilder().append("You have learned ").append(skillCounter).append(" new skills.").toString());
    skills = null;
  }

  public void setExp(long exp) {
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

  public L2Radar getRadar()
  {
    return _radar;
  }

  public int getSp() {
    return getStat().getSp();
  }
  public void setSp(int sp) {
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

  public long getClanJoinExpiryTime()
  {
    return _clanJoinExpiryTime;
  }

  public void setClanJoinExpiryTime(long time)
  {
    _clanJoinExpiryTime = time;
  }

  public long getClanCreateExpiryTime()
  {
    return _clanCreateExpiryTime;
  }

  public void setClanCreateExpiryTime(long time)
  {
    _clanCreateExpiryTime = time;
  }

  public void setOnlineTime(long time)
  {
    _onlineTime = time;
    _onlineBeginTime = System.currentTimeMillis();
  }

  public PcInventory getInventory()
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

  public boolean getSittingTask() {
    return sittingTaskLaunched;
  }

  public void setIsSitting(boolean state)
  {
    _waitTypeSitting = state;
  }

  public void sitDown()
  {
    if ((isCastingNow()) && (!_relax))
    {
      sendMessage("Cannot sit while casting");
      return;
    }

    if (sittingTaskLaunched)
    {
      return;
    }

    if ((!_waitTypeSitting) && (!isAttackingDisabled()) && (!isOutOfControl()) && (!isImobilised()))
    {
      breakAttack();
      setIsSitting(true);
      broadcastPacket(new ChangeWaitType(this, 0));

      sittingTaskLaunched = true;

      ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(this), 2500L);
      setIsParalyzed(true);
    }
  }

  public void standUp()
  {
    if ((L2Event.active) && (eventSitForced))
    {
      sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
    }
    else if ((CTF._sitForced) && (_inEventCTF)) {
      sendMessage("The Admin/GM handle if you sit or stand in this match!");
    } else if ((_waitTypeSitting) && (!isInStoreMode()) && (!isAlikeDead()) && (!sittingTaskLaunched))
    {
      if (_relax)
      {
        setRelax(false);
        stopEffects(L2Effect.EffectType.RELAXING);
      }

      broadcastPacket(new ChangeWaitType(this, 1));
      sittingTaskLaunched = true;

      ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2500L);
    }
  }

  public void setRelax(boolean val)
  {
    _relax = val;
  }

  public PcWarehouse getWarehouse()
  {
    if (_warehouse == null)
    {
      _warehouse = new PcWarehouse(this);
      _warehouse.restore();
    }
    if (Config.WAREHOUSE_CACHE)
      WarehouseCacheManager.getInstance().addCacheTask(this);
    return _warehouse;
  }

  public void clearWarehouse()
  {
    if (_warehouse != null)
      _warehouse.deleteMe();
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
    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ADENA);
      sm.addNumber(count);
      sendPacket(sm);
    }

    if (count > 0)
    {
      _inventory.addAdena(process, count, this, reference);

      if (getActiveTradeList() != null)
      {
        cancelActiveTrade();
      }

      if (!Config.FORCE_INVENTORY_UPDATE)
      {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(_inventory.getAdenaInstance());
        sendPacket(iu);
      } else {
        sendPacket(new ItemList(this, false));
      }
    }
  }

  public boolean reduceAdena(String process, int count, L2Object reference, boolean sendMessage)
  {
    if (count > getAdena())
    {
      if (sendMessage) sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
      return false;
    }

    if (count > 0)
    {
      L2ItemInstance adenaItem = _inventory.getAdenaInstance();
      _inventory.reduceAdena(process, count, this, reference);

      if (getActiveTradeList() != null)
      {
        cancelActiveTrade();
      }

      if (!Config.FORCE_INVENTORY_UPDATE)
      {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(adenaItem);
        sendPacket(iu);
      } else {
        sendPacket(new ItemList(this, false));
      }
      if (sendMessage)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ADENA);
        sm.addNumber(count);
        sendPacket(sm);
      }
    }

    return true;
  }

  public void addAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
  {
    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
      sm.addItemName(5575);
      sm.addNumber(count);
      sendPacket(sm);
    }

    if (count > 0)
    {
      _inventory.addAncientAdena(process, count, this, reference);
      if (getActiveTradeList() != null)
      {
        cancelActiveTrade();
      }
      if (!Config.FORCE_INVENTORY_UPDATE)
      {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(_inventory.getAncientAdenaInstance());
        sendPacket(iu);
      } else {
        sendPacket(new ItemList(this, false));
      }
    }
  }

  public boolean reduceAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
  {
    if (count > getAncientAdena())
    {
      if (sendMessage) {
        sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
      }
      return false;
    }

    if (count > 0)
    {
      L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
      _inventory.reduceAncientAdena(process, count, this, reference);
      if (getActiveTradeList() != null)
      {
        cancelActiveTrade();
      }
      if (!Config.FORCE_INVENTORY_UPDATE)
      {
        InventoryUpdate iu = new InventoryUpdate();
        iu.addItem(ancientAdenaItem);
        sendPacket(iu);
      }
      else
      {
        sendPacket(new ItemList(this, false));
      }

      if (sendMessage)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
        sm.addNumber(count);
        sm.addItemName(5575);
        sendPacket(sm);
      }
    }

    return true;
  }

  public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
  {
    if (item.getCount() > 0)
    {
      if (sendMessage)
      {
        if (item.getCount() > 1)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
          sm.addItemName(item.getItemId());
          sm.addNumber(item.getCount());
          sendPacket(sm);
        }
        else if (item.getEnchantLevel() > 0)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
          sm.addNumber(item.getEnchantLevel());
          sm.addItemName(item.getItemId());
          sendPacket(sm);
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
          sm.addItemName(item.getItemId());
          sendPacket(sm);
        }

      }

      L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);
      if (getActiveTradeList() != null)
      {
        cancelActiveTrade();
      }

      if (!Config.FORCE_INVENTORY_UPDATE)
      {
        InventoryUpdate playerIU = new InventoryUpdate();
        playerIU.addItem(newitem);
        sendPacket(playerIU);
      }
      else
      {
        sendPacket(new ItemList(this, false));
      }

      StatusUpdate su = new StatusUpdate(getObjectId());
      su.addAttribute(14, getCurrentLoad());
      sendPacket(su);

      if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
      {
        Ride dismount = new Ride(getObjectId(), 0, 0);
        sendPacket(dismount);
        broadcastPacket(dismount);
        setMountType(dismount.getMountType());
        if ((getInventory().getPaperdollItemByL2ItemId(16384) != null) && (getInventory().getPaperdollItemByL2ItemId(16384).getAugmentation() != null))
        {
          getInventory().getPaperdollItemByL2ItemId(16384).getAugmentation().removeBoni(this);
        }
        CursedWeaponsManager.getInstance().activate(this, newitem);
      }

      if ((!isGM()) && (!_inventory.validateCapacity(0)) && (newitem.isDropable()))
        dropItem("InvDrop", newitem, null, true);
    }
  }

  public void addItem(String process, int itemId, int count, L2Object reference, boolean sendMessage)
  {
    if (count > 0)
    {
      if ((sendMessage) && (((!isCastingNow()) && (ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB)) || (ItemTable.getInstance().createDummyItem(itemId).getItemType() != L2EtcItemType.HERB)))
      {
        if (count > 1)
        {
          if ((process.equalsIgnoreCase("sweep")) || (process.equalsIgnoreCase("Quest")))
          {
            SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
            sm.addItemName(itemId);
            sm.addNumber(count);
            sendPacket(sm);
          }
          else
          {
            SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
            sm.addItemName(itemId);
            sm.addNumber(count);
            sendPacket(sm);
          }

        }
        else if ((process.equalsIgnoreCase("sweep")) || (process.equalsIgnoreCase("Quest")))
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
          sm.addItemName(itemId);
          sendPacket(sm);
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
          sm.addItemName(itemId);
          sendPacket(sm);
        }

      }

      if (ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB)
      {
        if (!isCastingNow()) {
          L2ItemInstance herb = new L2ItemInstance(_charId, itemId);
          IItemHandler handler = ItemHandler.getInstance().getItemHandler(herb.getItemId());
          if (handler != null)
          {
            handler.useItem(this, herb);
            if (_herbstask >= 100) _herbstask -= 100; 
          }
        }
        else {
          _herbstask += 100;
          ThreadPoolManager.getInstance().scheduleAi(new HerbTask(process, itemId, count, reference, sendMessage), _herbstask);
        }

      }
      else
      {
        L2ItemInstance item = _inventory.addItem(process, itemId, count, this, reference);
        if (getActiveTradeList() != null)
        {
          cancelActiveTrade();
        }

        if (!Config.FORCE_INVENTORY_UPDATE)
        {
          InventoryUpdate playerIU = new InventoryUpdate();
          playerIU.addItem(item);
          sendPacket(playerIU);
        }
        else {
          sendPacket(new ItemList(this, false));
        }

        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(14, getCurrentLoad());
        sendPacket(su);

        if (CursedWeaponsManager.getInstance().isCursed(item.getItemId())) {
          CursedWeaponsManager.getInstance().activate(this, item);
        }

        if ((!isGM()) && (!_inventory.validateCapacity(0)) && (item.isDropable()))
          dropItem("InvDrop", item, null, true);
      }
    }
  }

  public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
  {
    int oldCount = item.getCount();
    item = _inventory.destroyItem(process, item, this, reference);

    if (item == null)
    {
      if (sendMessage) {
        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      }
      return false;
    }
    if (getActiveTradeList() != null)
    {
      cancelActiveTrade();
    }

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendPacket(playerIU);
    } else {
      sendPacket(new ItemList(this, false));
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(14, getCurrentLoad());
    sendPacket(su);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addNumber(oldCount);
      sm.addItemName(item.getItemId());
      sendPacket(sm);
    }

    return true;
  }

  public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.getItemByObjectId(objectId);

    if ((item == null) || (item.getCount() < count) || (_inventory.destroyItem(process, objectId, count, this, reference) == null))
    {
      if (sendMessage) {
        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      }
      return false;
    }
    if (getActiveTradeList() != null)
    {
      cancelActiveTrade();
    }

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendPacket(playerIU);
    } else {
      sendPacket(new ItemList(this, false));
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(14, getCurrentLoad());
    sendPacket(su);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addNumber(count);
      sm.addItemName(item.getItemId());
      sendPacket(sm);
    }

    return true;
  }

  public boolean destroyItemWithoutTrace(String process, int objectId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.getItemByObjectId(objectId);

    if ((item == null) || (item.getCount() < count))
    {
      if (sendMessage)
        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      return false;
    }

    if (item.getCount() > count)
    {
      synchronized (item)
      {
        item.changeCountWithoutTrace(process, -count, this, reference);
        item.setLastChange(2);

        if (GameTimeController.getGameTicks() % 10 == 0)
          item.updateDatabase();
        _inventory.refreshWeight();
      }

    }
    else
    {
      _inventory.destroyItem(process, item, this, reference);
    }
    if (getActiveTradeList() != null)
    {
      cancelActiveTrade();
    }

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendPacket(playerIU);
    } else {
      sendPacket(new ItemList(this, false));
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(14, getCurrentLoad());
    sendPacket(su);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addNumber(count);
      sm.addItemName(item.getItemId());
      sendPacket(sm);
    }

    return true;
  }

  public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance item = _inventory.getItemByItemId(itemId);

    if ((item == null) || (item.getCount() < count) || (_inventory.destroyItemByItemId(process, itemId, count, this, reference) == null))
    {
      if (sendMessage) {
        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      }
      return false;
    }
    if (getActiveTradeList() != null)
    {
      cancelActiveTrade();
    }

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendPacket(playerIU);
    } else {
      sendPacket(new ItemList(this, false));
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(14, getCurrentLoad());
    sendPacket(su);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
      sm.addNumber(count);
      sm.addItemName(itemId);
      sendPacket(sm);
    }

    return true;
  }

  public void destroyWearedItems(String process, L2Object reference, boolean sendMessage)
  {
    for (L2ItemInstance item : getInventory().getItems())
    {
      if (!item.isWear())
        continue;
      if (item.isEquipped()) {
        getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
      }
      if (_inventory.destroyItem(process, item, this, reference) == null)
      {
        _log.warning(new StringBuilder().append("Player ").append(getName()).append(" can't destroy weared item: ").append(item.getName()).append("[ ").append(item.getObjectId()).append(" ]").toString());
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISARMED);
        sm.addItemName(item.getItemId());
        sendPacket(sm);
      }

    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(14, getCurrentLoad());
    sendPacket(su);

    ItemList il = new ItemList(getInventory().getItems(), true);
    sendPacket(il);
    if (getActiveTradeList() != null)
    {
      cancelActiveTrade();
    }

    broadcastUserInfo();

    sendMessage("Trying-on mode has ended.");
  }

  public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2Object reference)
  {
    L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
    if (oldItem == null) return null;
    L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
    if (newItem == null) return null;

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate playerIU = new InventoryUpdate();

      if ((oldItem.getCount() > 0) && (oldItem != newItem))
        playerIU.addModifiedItem(oldItem);
      else {
        playerIU.addRemovedItem(oldItem);
      }
      sendPacket(playerIU);
    } else {
      sendPacket(new ItemList(this, false));
    }

    StatusUpdate playerSU = new StatusUpdate(getObjectId());
    playerSU.addAttribute(14, getCurrentLoad());
    sendPacket(playerSU);

    if ((target instanceof PcInventory))
    {
      L2PcInstance targetPlayer = ((PcInventory)target).getOwner();

      if (!Config.FORCE_INVENTORY_UPDATE)
      {
        InventoryUpdate playerIU = new InventoryUpdate();

        if (newItem.getCount() > count)
          playerIU.addModifiedItem(newItem);
        else {
          playerIU.addNewItem(newItem);
        }
        targetPlayer.sendPacket(playerIU);
      } else {
        targetPlayer.sendPacket(new ItemList(targetPlayer, false));
      }

      playerSU = new StatusUpdate(targetPlayer.getObjectId());
      playerSU.addAttribute(14, targetPlayer.getCurrentLoad());
      targetPlayer.sendPacket(playerSU);
    }
    else if ((target instanceof PetInventory))
    {
      PetInventoryUpdate petIU = new PetInventoryUpdate();

      if (newItem.getCount() > count)
        petIU.addModifiedItem(newItem);
      else {
        petIU.addNewItem(newItem);
      }
      ((PetInventory)target).getOwner().getOwner().sendPacket(petIU);
    }

    return newItem;
  }

  public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
  {
    item = _inventory.dropItem(process, item, this, reference);

    if (item == null)
    {
      if (sendMessage) {
        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      }
      return false;
    }

    item.dropMe(this, getClientX() + Rnd.get(50) - 25, getClientY() + Rnd.get(50) - 25, getClientZ());

    if ((Config.AUTODESTROY_ITEM_AFTER > 0) && (Config.DESTROY_DROPPED_PLAYER_ITEM) && (!Config.LIST_PROTECTED_ITEMS.contains(Integer.valueOf(item.getItemId()))))
    {
      if (((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)) || (!item.isEquipable()))
        ItemsAutoDestroy.getInstance().addItem(item);
    }
    if (Config.DESTROY_DROPPED_PLAYER_ITEM) {
      if ((!item.isEquipable()) || ((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)))
        item.setProtected(false);
      else
        item.setProtected(true);
    }
    else
      item.setProtected(true);
    if (getActiveTradeList() != null)
    {
      cancelActiveTrade();
    }

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(item);
      sendPacket(playerIU);
    } else {
      sendPacket(new ItemList(this, false));
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(14, getCurrentLoad());
    sendPacket(su);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
      sm.addItemName(item.getItemId());
      sendPacket(sm);
    }

    return true;
  }

  public L2ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, L2Object reference, boolean sendMessage)
  {
    L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
    L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);

    if (item == null)
    {
      if (sendMessage) {
        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      }
      return null;
    }

    item.dropMe(this, x, y, z);

    if ((Config.AUTODESTROY_ITEM_AFTER > 0) && (Config.DESTROY_DROPPED_PLAYER_ITEM) && (!Config.LIST_PROTECTED_ITEMS.contains(Integer.valueOf(item.getItemId()))))
    {
      if (((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)) || (!item.isEquipable()))
        ItemsAutoDestroy.getInstance().addItem(item);
    }
    if (Config.DESTROY_DROPPED_PLAYER_ITEM) {
      if ((!item.isEquipable()) || ((item.isEquipable()) && (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)))
        item.setProtected(false);
      else
        item.setProtected(true);
    }
    else
      item.setProtected(true);
    if (getActiveTradeList() != null)
    {
      cancelActiveTrade();
    }

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate playerIU = new InventoryUpdate();
      playerIU.addItem(invitem);
      sendPacket(playerIU);
    }
    else
    {
      sendPacket(new ItemList(this, false));
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(14, getCurrentLoad());
    sendPacket(su);

    if (sendMessage)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
      sm.addItemName(item.getItemId());
      sendPacket(sm);
    }

    return item;
  }

  public L2ItemInstance checkItemManipulation(int objectId, int count, String action)
  {
    if (L2World.getInstance().findObject(objectId) == null)
    {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" item not available in L2World").toString());
      return null;
    }

    L2ItemInstance item = getInventory().getItemByObjectId(objectId);

    if ((item == null) || (item.getOwnerId() != getObjectId()))
    {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" item he is not owner of").toString());
      return null;
    }

    if ((count < 0) || ((count > 1) && (!item.isStackable())))
    {
      _log.finest(new StringBuilder().append(getObjectId()).append(": player tried to ").append(action).append(" item with invalid count: ").append(count).toString());
      return null;
    }

    if (count > item.getCount())
    {
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

  public void setProtection(boolean protect)
  {
    if ((Config.DEVELOPER) && ((protect) || (_protectEndTime > 0L))) {
      System.out.println(new StringBuilder().append(getName()).append(": Protection ").append(protect ? new StringBuilder().append("ON ").append(GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * 10).toString() : "OFF").append(" (currently ").append(GameTimeController.getGameTicks()).append(")").toString());
    }
    _protectEndTime = (protect ? GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * 10 : 0L);
  }

  public void setRecentFakeDeath(boolean protect)
  {
    _recentFakeDeathEndTime = (protect ? GameTimeController.getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * 10 : 0L);
  }

  public boolean isRecentFakeDeath()
  {
    return _recentFakeDeathEndTime > GameTimeController.getGameTicks();
  }

  public L2GameClient getClient()
  {
    return _client;
  }

  public void setClient(L2GameClient client)
  {
    if ((client == null) && (_client != null))
    {
      if (_client.getConnection() != null)
        _tvtIp = _client.getConnection().getInetAddress().getHostAddress();
      _client.stopGuardTask();
      nProtect.getInstance().closeSession(_client);
    }
    _client = client;
  }

  public void closeNetConnection(boolean closeClient) {
    if (_client != null)
    {
      if (closeClient)
      {
        _client.close(new LeaveWorld());
      }
      else
      {
        _client.close(new ServerClose());
      }
      setClient(null);
    }
  }

  public Point3D getCurrentSkillWorldPosition()
  {
    return _currentSkillWorldPosition;
  }

  public void setCurrentSkillWorldPosition(Point3D worldPosition)
  {
    _currentSkillWorldPosition = worldPosition;
  }

  public void onAction(L2PcInstance player)
  {
    if (player == null)
      return;
    if (!TvTEvent.onAction(player, getObjectId()))
    {
      player.sendPacket(new ActionFailed());
      return;
    }

    if ((CTF._started) && (!Config.CTF_ALLOW_INTERFERENCE))
    {
      if (((_inEventCTF) && (!player._inEventCTF)) || ((!_inEventCTF) && (player._inEventCTF)))
      {
        player.sendPacket(new ActionFailed());
        return;
      }
    }

    if (player.isOutOfControl())
    {
      player.sendPacket(new ActionFailed());
      return;
    }
    if (player.getTarget() != this)
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));
      player.sendPacket(new MyTargetSelected(getObjectId(), 0));
      if (player != this) player.sendPacket(new ValidateLocation(this));
    }
    else
    {
      if (player != this) player.sendPacket(new ValidateLocation(this));
      if (getPrivateStoreType() != 0)
      {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
      }
      else if ((isAutoAttackable(player)) || ((player._inEventCTF) && (CTF._started)))
      {
        if (((isCursedWeaponEquiped()) && (player.getLevel() < 21)) || ((player.isCursedWeaponEquiped()) && (getLevel() < 21)))
        {
          player.sendPacket(new ActionFailed());
        }
        else if ((Config.GEODATA) || ((Config.GEODATA) && (Config.GEO_PATH_FINDING)))
        {
          if (GeoData.getInstance().canSeeTarget(player, this))
          {
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
            player.onActionRequest();
          }
        }
        else
        {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
          player.onActionRequest();
        }
      }
      else if ((Config.GEODATA) || ((Config.GEODATA) && (Config.GEO_PATH_FINDING)))
      {
        if (GeoData.getInstance().canSeeTarget(player, this))
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
      }
      else
      {
        if (player != this) player.turn(this);
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
      }
    }
  }

  public void onActionShift(L2GameClient client)
  {
    L2PcInstance player = client.getActiveChar();

    if (player == null) return;

    if (!TvTEvent.onAction(player, getObjectId()))
    {
      player.sendPacket(new ActionFailed());
      return;
    }

    if (player.isOutOfControl())
    {
      player.sendPacket(new ActionFailed());
      return;
    }

    player.setTarget(this);
  }

  private boolean needCpUpdate(int barPixels) {
    double currentCp = getCurrentCp();

    if ((currentCp <= 1.0D) || (getMaxCp() < barPixels)) {
      return true;
    }
    if ((currentCp <= _cpUpdateDecCheck) || (currentCp >= _cpUpdateIncCheck))
    {
      if (currentCp == getMaxCp())
      {
        _cpUpdateIncCheck = (currentCp + 1.0D);
        _cpUpdateDecCheck = (currentCp - _cpUpdateInterval);
      }
      else
      {
        double doubleMulti = currentCp / _cpUpdateInterval;
        int intMulti = (int)doubleMulti;

        _cpUpdateDecCheck = (_cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti));
        _cpUpdateIncCheck = (_cpUpdateDecCheck + _cpUpdateInterval);
      }

      return true;
    }

    return false;
  }

  private boolean needMpUpdate(int barPixels) {
    double currentMp = getCurrentMp();

    if ((currentMp <= 1.0D) || (getMaxMp() < barPixels)) {
      return true;
    }
    if ((currentMp <= _mpUpdateDecCheck) || (currentMp >= _mpUpdateIncCheck))
    {
      if (currentMp == getMaxMp())
      {
        _mpUpdateIncCheck = (currentMp + 1.0D);
        _mpUpdateDecCheck = (currentMp - _mpUpdateInterval);
      }
      else
      {
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
    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(9, (int)getCurrentHp());
    su.addAttribute(11, (int)getCurrentMp());
    su.addAttribute(33, (int)getCurrentCp());
    su.addAttribute(34, getMaxCp());
    sendPacket(su);
    if ((isInParty()) && ((needCpUpdate(352)) || (super.needHpUpdate(352)) || (needMpUpdate(352))))
    {
      PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
      getParty().broadcastToPartyMembers(this, update);
    }
    if (isInOlympiadMode())
    {
      Collection plrs = getKnownList().getKnownPlayers().values();

      for (L2PcInstance player : plrs)
      {
        if ((player.getOlympiadGameId() == getOlympiadGameId()) && (player.isOlympiadStart()))
        {
          player.sendPacket(new ExOlympiadUserInfoSpectator(this, 1));
        }
      }

      if ((Olympiad.getInstance().getSpectators(_olympiadGameId) != null) && (isOlympiadStart()))
      {
        for (L2PcInstance spectator : Olympiad.getInstance().getSpectators(_olympiadGameId))
        {
          if (spectator != null)
            spectator.sendPacket(new ExOlympiadUserInfoSpectator(this, getOlympiadSide()));
        }
      }
    }
    if (isInDuel())
    {
      ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
      DuelManager.getInstance().broadcastToOppositTeam(this, update);
    }
  }

  public final void broadcastUserInfo()
  {
    sendPacket(new UserInfo(this));

    Broadcast.toKnownPlayers(this, new CharInfo(this));
  }

  public final void broadcastTitleInfo()
  {
    sendPacket(new UserInfo(this));
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
    if (getClanId() == 0)
    {
      return 0;
    }
    if (getClan().getAllyId() == 0)
    {
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
    if (_client != null)
    {
      _client.sendPacket(packet);
    }
  }

  public void doInteract(L2Character target)
  {
    if ((target instanceof L2PcInstance))
    {
      L2PcInstance temp = (L2PcInstance)target;
      sendPacket(new ActionFailed());

      if ((temp.getPrivateStoreType() == 1) || (temp.getPrivateStoreType() == 8))
        sendPacket(new PrivateStoreListSell(this, temp));
      else if (temp.getPrivateStoreType() == 3)
        sendPacket(new PrivateStoreListBuy(this, temp));
      else if (temp.getPrivateStoreType() == 5) {
        sendPacket(new RecipeShopSellList(this, temp));
      }

    }
    else if (target != null) {
      target.onAction(this);
    }
  }

  public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
  {
    if (isInParty()) getParty().distributeItem(this, item, false, target);
    else if (item.getItemId() == 57) addAdena("Loot", item.getCount(), target, true); else
      addItem("Loot", item.getItemId(), item.getCount(), target, true);
  }

  protected void doPickupItem(L2Object object)
  {
    if ((isAlikeDead()) || (isFakeDeath())) return;

    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    if (!(object instanceof L2ItemInstance))
    {
      _log.warning(new StringBuilder().append("trying to pickup wrong target.").append(getTarget()).toString());
      return;
    }

    L2ItemInstance target = (L2ItemInstance)object;

    sendPacket(new ActionFailed());

    StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
    sendPacket(sm);

    synchronized (target)
    {
      if (!target.isVisible())
      {
        sendPacket(new ActionFailed());
        return;
      }

      if (((isInParty()) && (getParty().getLootDistribution() == 0)) || ((!isInParty()) && (!_inventory.validateCapacity(target))))
      {
        sendPacket(new ActionFailed());
        sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
        return;
      }

      if ((target.getOwnerId() != 0) && (target.getOwnerId() != getObjectId()) && (!isInLooterParty(target.getOwnerId())))
      {
        sendPacket(new ActionFailed());

        if (target.getItemId() == 57)
        {
          SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
          smsg.addNumber(target.getCount());
          sendPacket(smsg);
        }
        else if (target.getCount() > 1)
        {
          SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
          smsg.addItemName(target.getItemId());
          smsg.addNumber(target.getCount());
          sendPacket(smsg);
        }
        else
        {
          SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
          smsg.addItemName(target.getItemId());
          sendPacket(smsg);
        }

        return;
      }
      if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getObjectId()) || (isInLooterParty(target.getOwnerId()))))
      {
        target.resetOwnerTimer();
      }

      target.pickupMe(this);
      if (Config.SAVE_DROPPED_ITEM) {
        ItemsOnGroundManager.getInstance().removeObject(target);
      }

    }

    if (target.getItemType() == L2EtcItemType.HERB)
    {
      IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getItemId());
      if (handler != null)
      {
        handler.useItem(this, target);
      }ItemTable.getInstance().destroyItem("Consume", target, this, null);
    }
    else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
    {
      addItem("Pickup", target, null, true);
    }
    else
    {
      if (((target.getItemType() instanceof L2ArmorType)) || ((target.getItemType() instanceof L2WeaponType)))
      {
        if (target.getEnchantLevel() > 0)
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3);
          msg.addString(getName());
          msg.addNumber(target.getEnchantLevel());
          msg.addItemName(target.getItemId());
          broadcastPacket(msg, 1400);
        }
        else
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2);
          msg.addString(getName());
          msg.addItemName(target.getItemId());
          broadcastPacket(msg, 1400);
        }

      }

      if (isInParty()) { getParty().distributeItem(this, target);
      }
      else if ((target.getItemId() == 57) && (getInventory().getAdenaInstance() != null))
      {
        addAdena("Pickup", target.getCount(), null, true);
        ItemTable.getInstance().destroyItem("Pickup", target, this, null);
      }
      else {
        addItem("Pickup", target, null, true);
      }
    }
  }

  public void setTarget(L2Object newTarget)
  {
    if ((newTarget != null) && (!newTarget.isVisible())) {
      newTarget = null;
    }

    if ((newTarget != null) && (Math.abs(newTarget.getZ() - getZ()) > 1000)) {
      newTarget = null;
    }
    if (!isGM())
    {
      if (((newTarget instanceof L2FestivalMonsterInstance)) && (!isFestivalParticipant())) {
        newTarget = null;
      }
      else if ((isInParty()) && (getParty().isInDimensionalRift()))
      {
        byte riftType = getParty().getDimensionalRift().getType();
        byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();

        if ((newTarget != null) && (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))) {
          newTarget = null;
        }
      }
    }

    L2Object oldTarget = getTarget();

    if (oldTarget != null)
    {
      if (oldTarget.equals(newTarget)) {
        return;
      }

      if ((oldTarget instanceof L2Character)) {
        ((L2Character)oldTarget).removeStatusListener(this);
      }
    }

    if ((newTarget != null) && ((newTarget instanceof L2Character)))
    {
      ((L2Character)newTarget).addStatusListener(this);
      TargetSelected my = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
      broadcastPacket(my);
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

  public L2ItemInstance getChestArmorInstance()
  {
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

  public void setIsWearingFormalWear(boolean value)
  {
    _IsWearingFormalWear = value;
  }

  public boolean isMarried() {
    return _married;
  }

  public void setMarried(boolean state)
  {
    _married = state;
  }

  public boolean isEngageRequest()
  {
    return _engagerequest;
  }

  public void setEngageRequest(boolean state, int playerid)
  {
    _engagerequest = state;
    _engageid = playerid;
  }

  public void setMaryRequest(boolean state)
  {
    _marryrequest = state;
  }

  public boolean isMaryRequest()
  {
    return _marryrequest;
  }

  public void setMarryAccepted(boolean state)
  {
    _marryaccepted = state;
  }

  public boolean isMarryAccepted()
  {
    return _marryaccepted;
  }

  public int getEngageId()
  {
    return _engageid;
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

  public void EngageAnswer(int answer)
  {
    if (!_engagerequest)
      return;
    if (_engageid == 0) {
      return;
    }

    L2PcInstance ptarget = (L2PcInstance)L2World.getInstance().findObject(_engageid);
    setEngageRequest(false, 0);
    if (ptarget != null)
    {
      if (answer == 1)
      {
        CoupleManager.getInstance().createCouple(ptarget, this);
        ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
      }
      else {
        ptarget.sendMessage("Request to Engage has been >DENIED<!");
      }
    }
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
    setCurrentHp(0.0D);

    if (!super.doDie(killer)) {
      return false;
    }
    if (killer != null)
    {
      L2PcInstance pk = null;
      if ((killer instanceof L2PcInstance)) {
        pk = (L2PcInstance)killer;
      }
      TvTEvent.onKill(killer, this);

      if (_inEventCTF)
      {
        if ((CTF._teleport) || (CTF._started))
        {
          sendMessage("You will be revived and teleported to team flag in 20 seconds!");

          if (_haveFlagCTF) {
            removeCTFFlagOnDie();
          }
          ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
          {
            public void run()
            {
              teleToLocation(((Integer)CTF._teamsX.get(CTF._teams.indexOf(_teamNameCTF))).intValue() + Rnd.get(Config.CTF_RND_SPAWNXMIN, Config.CTF_RND_SPAWNXMAX), ((Integer)CTF._teamsY.get(CTF._teams.indexOf(_teamNameCTF))).intValue() + Rnd.get(Config.CTF_RND_SPAWNYMIN, Config.CTF_RND_SPAWNYMAX), ((Integer)CTF._teamsZ.get(CTF._teams.indexOf(_teamNameCTF))).intValue() + Config.CTF_SPAWN_Z, false);
              doRevive(true);
            }
          }
          , 20000L);
        }

      }

      if ((atEvent) && (pk != null))
      {
        pk.kills.add(getName());
      }

      setExpBeforeDeath(0L);

      if (isCursedWeaponEquiped())
      {
        CursedWeaponsManager.getInstance().drop(_cursedWeaponEquipedId, killer);
      }
      else
      {
        if ((pk == null) || (!pk.isCursedWeaponEquiped()))
        {
          onDieDropItem(killer);

          if ((!isInsideZone(1)) || (isInsideZone(4)))
          {
            boolean isKillerPc = killer instanceof L2PcInstance;
            if ((isKillerPc) && (((L2PcInstance)killer).getClan() != null) && (getClan() != null) && (!isAcademyMember()) && (!((L2PcInstance)killer).isAcademyMember()) && (_clan.isAtWarWith(Integer.valueOf(((L2PcInstance)killer).getClanId()))) && (((L2PcInstance)killer).getClan().isAtWarWith(Integer.valueOf(_clan.getClanId()))))
            {
              if (getClan().getReputationScore() > 0)
              {
                ((L2PcInstance)killer).getClan().setReputationScore(((L2PcInstance)killer).getClan().getReputationScore() + Config.REWARD_KILL_WAR, true);
                getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
                ((L2PcInstance)killer).getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(((L2PcInstance)killer).getClan()));
              }

              if (((L2PcInstance)killer).getClan().getReputationScore() > 0)
              {
                _clan.setReputationScore(_clan.getReputationScore() - Config.REWARD_KILL_WAR, true);
                getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
                ((L2PcInstance)killer).getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(((L2PcInstance)killer).getClan()));
              }
            }
            if (Config.ALT_GAME_DELEVEL)
            {
              if ((getSkillLevel(194) < 0) || (getStat().getLevel() > 9))
              {
                if (!isPhoenixBlessed())
                  deathPenalty((pk != null) && (getClan() != null) && (pk.getClan() != null) && (pk.getClan().isAtWarWith(Integer.valueOf(getClanId()))));
              }
            }
            onDieUpdateKarma();
          }
        }
        if ((pk != null) && (!pk.getName().equalsIgnoreCase(getName())) && (Config.ANONS_PVP_PK > 0) && (!isInsideZone(1)))
        {
          String announcetext = "";
          if ((getPvpFlag() == 0) && ((Config.ANONS_PVP_PK == 1) || (Config.ANONS_PVP_PK == 3)))
          {
            announcetext = new StringBuilder().append("Player ").append(pk.getName()).append(" kill").append(getName()).append(" and became PK").toString();
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            if ((Config.REGION_PVP_PK) && (pk != null))
              announcetext = new StringBuilder().append(announcetext).append(" in region ").append(MapRegionTable.getInstance().getClosestTownName(pk)).toString();
            CreatureSay cs = new CreatureSay(0, 10, "", announcetext);
            L2PcInstance players;
            for (Iterator i$ = L2World.getInstance().getAllPlayers().iterator(); i$.hasNext(); players.sendPacket(cs)) {
              players = (L2PcInstance)i$.next();
            }
          }
          else if ((getPvpFlag() != 0) && ((Config.ANONS_PVP_PK == 2) || (Config.ANONS_PVP_PK == 3)))
          {
            announcetext = new StringBuilder().append("Player ").append(pk.getName()).append(" kill in PvP").append(getName()).toString();
            CreatureSay cs = new CreatureSay(0, 10, "", announcetext);
            L2PcInstance players;
            for (Iterator i$ = L2World.getInstance().getAllPlayers().iterator(); i$.hasNext(); players.sendPacket(cs)) {
              players = (L2PcInstance)i$.next();
            }
          }
        }
      }
    }

    setPvpFlag(0);
    if (_cubics.size() > 0)
    {
      for (L2CubicInstance cubic : _cubics.values())
      {
        cubic.stopAction();
        cubic.cancelDisappear();
      }

      _cubics.clear();
    }

    if (_forceBuff != null) {
      _forceBuff.delete();
    }
    for (L2Character character : getKnownList().getKnownCharacters()) {
      if ((character.getForceBuff() != null) && (character.getForceBuff().getTarget() == this))
        character.abortCast();
    }
    if ((isInParty()) && (getParty().isInDimensionalRift())) {
      getParty().getDimensionalRift().getDeadMemberList().add(this);
    }

    calculateDeathPenaltyBuffLevel(killer);

    stopRentPet();
    stopUnmountTask();
    stopWaterTask();

    if (GrandBossManager.getInstance().checkIfInZone("LairofSailren", this))
    {
      Sailren.getInstance().checkAnnihilated(this);
    }

    cancelActiveTrade();
    updateEffectIcons();
    return true;
  }

  public void removeCTFFlagOnDie()
  {
    CTF._flagsTaken.set(CTF._teams.indexOf(_teamNameHaveFlagCTF), Boolean.valueOf(false));
    CTF.spawnFlag(_teamNameHaveFlagCTF);
    CTF.removeFlagFromPlayer(this);
    broadcastUserInfo();
    _haveFlagCTF = false;
    CTF.Announcements(new StringBuilder().append(CTF._eventName).append("(CTF): ").append(_teamNameHaveFlagCTF).append("'s flag returned.").toString());
  }

  private void onDieDropItem(L2Character killer)
  {
    if ((atEvent) || ((CTF._started) && (_inEventCTF)) || (killer == null)) {
      return;
    }
    if ((getKarma() <= 0) && ((killer instanceof L2PcInstance)) && (((L2PcInstance)killer).getClan() != null) && (getClan() != null) && (((L2PcInstance)killer).getClan().isAtWarWith(Integer.valueOf(getClanId()))))
    {
      return;
    }
    if ((!isInsideZone(1)) && ((!isGM()) || (Config.KARMA_DROP_GM)))
    {
      boolean isKarmaDrop = false;
      boolean isKillerNpc = killer instanceof L2NpcInstance;
      int pkLimit = Config.KARMA_PK_LIMIT;

      int dropEquip = 0;
      int dropEquipWeapon = 0;
      int dropItem = 0;
      int dropLimit = 0;
      int dropPercent = 0;

      if ((getKarma() > 0) && (getPkKills() >= pkLimit))
      {
        isKarmaDrop = true;
        dropPercent = Config.KARMA_RATE_DROP;
        dropEquip = Config.KARMA_RATE_DROP_EQUIP;
        dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
        dropItem = Config.KARMA_RATE_DROP_ITEM;
        dropLimit = Config.KARMA_DROP_LIMIT;
      }
      else if ((isKillerNpc) && (getLevel() > 4) && (!isFestivalParticipant()))
      {
        dropPercent = Config.PLAYER_RATE_DROP;
        dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
        dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
        dropItem = Config.PLAYER_RATE_DROP_ITEM;
        dropLimit = Config.PLAYER_DROP_LIMIT;
      }

      int dropCount = 0;
      while ((dropPercent > 0) && (Rnd.get(100) < dropPercent) && (dropCount < dropLimit))
      {
        int itemDropPercent = 0;
        List nonDroppableList = new FastList();
        List nonDroppableListPet = new FastList();

        nonDroppableList = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
        nonDroppableListPet = Config.KARMA_LIST_NONDROPPABLE_ITEMS;

        for (L2ItemInstance itemDrop : getInventory().getItems())
        {
          if ((itemDrop.isAugmented()) || (itemDrop.isShadowItem()) || (!itemDrop.isDropable()) || (itemDrop.getItemId() == 57) || (itemDrop.getItem().getType2() == 3) || (nonDroppableList.contains(Integer.valueOf(itemDrop.getItemId()))) || (nonDroppableListPet.contains(Integer.valueOf(itemDrop.getItemId()))) || ((getPet() != null) && (getPet().getControlItemId() == itemDrop.getItemId())))
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

          if (Rnd.get(100) >= itemDropPercent)
            continue;
          dropItem("DieDrop", itemDrop, killer, true);

          if (isKarmaDrop)
            _log.warning(new StringBuilder().append(getName()).append(" has karma and dropped id = ").append(itemDrop.getItemId()).append(", count = ").append(itemDrop.getCount()).toString());
          else {
            _log.warning(new StringBuilder().append(getName()).append(" dropped id = ").append(itemDrop.getItemId()).append(", count = ").append(itemDrop.getCount()).toString());
          }
          dropCount++;
          break;
        }
      }
    }
  }

  private void onDieUpdateKarma()
  {
    if (getKarma() > 0)
    {
      double karmaLost = Config.KARMA_LOST_BASE;
      karmaLost *= getLevel();
      karmaLost *= getLevel() / 100.0D;
      karmaLost = Math.round(karmaLost);
      if (karmaLost < 0.0D) karmaLost = 1.0D;

      setKarma(getKarma() - (int)karmaLost);
    }
  }

  public void onKillUpdatePvPKarma(L2Character target)
  {
    boolean flag = true;
    if (!FloodProtector.getInstance().tryPerformAction(getObjectId(), 7))
      flag = false;
    if (target == null) return;
    if (!(target instanceof L2PcInstance)) flag = false;
    if (!(target instanceof L2PlayableInstance)) return;
    if (_inEventCTF) return;

    L2PcInstance targetPlayer = null;
    if ((target instanceof L2PcInstance))
      targetPlayer = (L2PcInstance)target;
    else if ((target instanceof L2Summon)) {
      targetPlayer = ((L2Summon)target).getOwner();
    }
    if (targetPlayer == null) return;
    if (targetPlayer == this) return;

    if (isCursedWeaponEquiped())
    {
      CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
      return;
    }

    if ((isInDuel()) && (targetPlayer.isInDuel())) return;

    if ((isInsideZone(1)) || (targetPlayer.isInsideZone(1))) {
      return;
    }
    if (((checkIfPvP(target)) && (targetPlayer.getPvpFlag() != 0)) || ((isInsideZone(1)) && (targetPlayer.isInsideZone(1))))
    {
      increasePvpKills();
      if ((flag) && (Config.PVP_LEVEL_DIFFERENCE > 0) && (Math.abs(getLevel() - targetPlayer.getLevel()) >= Config.PVP_LEVEL_DIFFERENCE))
        flag = false;
      if ((flag) && (Config.PVP_STRICT_IP) && (getClient().getConnection().getSocket().getInetAddress().getHostAddress().equalsIgnoreCase(targetPlayer.getClient().getConnection().getSocket().getInetAddress().getHostAddress())))
        flag = false;
      if ((flag) && (Config.PVP_ITEM_COUNT > 0))
        addItem("PVPKill", Config.PVP_ITEM_ID, Config.PVP_ITEM_COUNT, null, true);
      if ((flag) && ((Config.PVP_EXP_COUNT > 0L) || (Config.PVP_SP_COUNT > 0)))
        addExpAndSp(Config.PVP_EXP_COUNT, Config.PVP_SP_COUNT);
    }
    else
    {
      if ((targetPlayer.getClan() != null) && (getClan() != null))
      {
        if (getClan().isAtWarWith(Integer.valueOf(targetPlayer.getClanId())))
        {
          if (targetPlayer.getClan().isAtWarWith(Integer.valueOf(getClanId())))
          {
            increasePvpKills();
            if ((flag) && (Config.PVP_LEVEL_DIFFERENCE > 0) && (Math.abs(getLevel() - targetPlayer.getLevel()) >= Config.PVP_LEVEL_DIFFERENCE))
              flag = false;
            if ((flag) && (Config.PVP_STRICT_IP) && (getClient().getConnection().getSocket().getInetAddress().getHostAddress().equalsIgnoreCase(targetPlayer.getClient().getConnection().getSocket().getInetAddress().getHostAddress())))
              flag = false;
            if ((flag) && (Config.PVP_ITEM_COUNT > 0))
              addItem("PVPKill", Config.PVP_ITEM_ID, Config.PVP_ITEM_COUNT, null, true);
            if ((flag) && ((Config.PVP_EXP_COUNT > 0L) || (Config.PVP_SP_COUNT > 0)))
              addExpAndSp(Config.PVP_EXP_COUNT, Config.PVP_SP_COUNT);
            return;
          }
        }
      }

      if (targetPlayer.getKarma() > 0)
      {
        if (Config.KARMA_AWARD_PK_KILL)
        {
          increasePvpKills();
          if ((flag) && (Config.PVP_LEVEL_DIFFERENCE > 0) && (Math.abs(getLevel() - targetPlayer.getLevel()) >= Config.PVP_LEVEL_DIFFERENCE))
            flag = false;
          if ((flag) && (Config.PVP_STRICT_IP) && (getClient().getConnection().getSocket().getInetAddress().getHostAddress().equalsIgnoreCase(targetPlayer.getClient().getConnection().getSocket().getInetAddress().getHostAddress())))
            flag = false;
          if ((flag) && (Config.PVP_ITEM_COUNT > 0))
            addItem("PVPKill", Config.PVP_ITEM_ID, Config.PVP_ITEM_COUNT, null, true);
          if ((flag) && ((Config.PVP_EXP_COUNT > 0L) || (Config.PVP_SP_COUNT > 0)))
            addExpAndSp(Config.PVP_EXP_COUNT, Config.PVP_SP_COUNT);
        }
      }
      else if (targetPlayer.getPvpFlag() == 0)
      {
        increasePkKillsAndKarma(targetPlayer.getLevel());
        if ((flag) && (Config.PK_LEVEL_DIFFERENCE > 0) && (Math.abs(getLevel() - targetPlayer.getLevel()) >= Config.PK_LEVEL_DIFFERENCE))
          flag = false;
        if ((flag) && (Config.PK_STRICT_IP) && (getClient().getConnection().getSocket().getInetAddress().getHostAddress().equalsIgnoreCase(targetPlayer.getClient().getConnection().getSocket().getInetAddress().getHostAddress())))
          flag = false;
        if ((flag) && (Config.PK_ITEM_COUNT > 0))
          addItem("PKKill", Config.PK_ITEM_ID, Config.PK_ITEM_COUNT, null, true);
        if ((flag) && ((Config.PK_EXP_COUNT > 0L) || (Config.PK_SP_COUNT > 0)))
          addExpAndSp(Config.PK_EXP_COUNT, Config.PK_SP_COUNT);
      }
    }
  }

  public void increasePvpKills()
  {
    if ((CTF._started) && (_inEventCTF)) {
      return;
    }
    setPvpKills(getPvpKills() + 1);

    sendPacket(new UserInfo(this));
  }

  public void increasePkKillsAndKarma(int targLVL)
  {
    if ((CTF._started) && (_inEventCTF))
      return;
    int baseKarma = Config.KARMA_MIN_KARMA;
    int newKarma = baseKarma;
    int karmaLimit = Config.KARMA_MAX_KARMA;

    int pkLVL = getLevel();
    int pkPKCount = getPkKills();

    int lvlDiffMulti = 0;
    int pkCountMulti = 0;

    if (pkPKCount > 0)
      pkCountMulti = pkPKCount / 2;
    else
      pkCountMulti = 1;
    if (pkCountMulti < 1) pkCountMulti = 1;

    if (pkLVL > targLVL)
      lvlDiffMulti = pkLVL / targLVL;
    else
      lvlDiffMulti = 1;
    if (lvlDiffMulti < 1) lvlDiffMulti = 1;

    newKarma *= pkCountMulti;
    newKarma *= lvlDiffMulti;

    if (newKarma < baseKarma) newKarma = baseKarma;
    if (newKarma > karmaLimit) newKarma = karmaLimit;

    if (getKarma() > 2147483647 - newKarma) {
      newKarma = 2147483647 - getKarma();
    }

    setPkKills(getPkKills() + 1);
    setKarma(getKarma() + newKarma);

    sendPacket(new UserInfo(this));
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
    if (karmaLost < Config.KARMA_LOST_BASE) karmaLost = Config.KARMA_LOST_BASE;
    if (karmaLost > getKarma()) karmaLost = getKarma();

    return karmaLost;
  }

  public void updatePvPStatus()
  {
    if ((CTF._started) && (_inEventCTF))
      return;
    if (isInsideZone(1)) return;
    setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);

    if (getPvpFlag() == 0)
      startPvPFlag();
  }

  public void updatePvPStatus(L2Character target)
  {
    L2PcInstance player_target = null;

    if ((target instanceof L2PcInstance))
      player_target = (L2PcInstance)target;
    else if ((target instanceof L2Summon)) {
      player_target = ((L2Summon)target).getOwner();
    }
    if (player_target == null)
      return;
    if ((CTF._started) && (_inEventCTF)) {
      return;
    }
    if ((isInDuel()) && (player_target.getDuelId() == getDuelId())) return;
    if (((!isInsideZone(1)) || (!player_target.isInsideZone(1))) && (player_target.getKarma() == 0))
    {
      if (checkIfPvP(player_target))
        setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
      else
        setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
      if (getPvpFlag() == 0)
        startPvPFlag();
    }
  }

  public void restoreExp(double restorePercent)
  {
    if (getExpBeforeDeath() > 0L)
    {
      getStat().addExp((int)Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100.0D));
      setExpBeforeDeath(0L);
    }
  }

  public void deathPenalty(boolean atwar)
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
    if ((isFestivalParticipant()) || (atwar) || (isInsideZone(4))) {
      percentLost /= 4.0D;
    }

    long lostExp = 0L;
    if ((!atEvent) && (!_inEventCTF))
    {
      if (lvl < 81)
        lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100.0D);
      else {
        lostExp = Math.round((getStat().getExpForLevel(81) - getStat().getExpForLevel(80)) * percentLost / 100.0D);
      }
    }

    setExpBeforeDeath(getExp());

    if (getCharmOfCourage())
    {
      if ((getSiegeState() > 0) && (isInsideZone(4))) {
        lostExp = 0L;
      }
    }
    setCharmOfCourage(false);
    getStat().addExp(-lostExp);
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

  public boolean isPartyMatchingAutomaticRegistration()
  {
    return _partyMatchingAutomaticRegistration;
  }

  public String getPartyMatchingMemo()
  {
    return _partyMatchingMemo;
  }

  public boolean isPartyMatchingShowClass()
  {
    return _partyMatchingShowClass;
  }

  public boolean isPartyMatchingShowLevel()
  {
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
    stopWaterTask();
    stopRentPet();
    stopPvpRegTask();
    stopJailTask(true);
    stopChatBanTask(true);
  }

  public L2Summon getPet()
  {
    return _summon;
  }

  public void setPet(L2Summon summon)
  {
    _summon = summon;
  }

  public L2TamedBeastInstance getTrainedBeast()
  {
    return _tamedBeast;
  }

  public void setTrainedBeast(L2TamedBeastInstance tamedBeast)
  {
    _tamedBeast = tamedBeast;
  }

  public L2Request getRequest()
  {
    return _request;
  }

  public synchronized void setActiveRequester(L2PcInstance requester)
  {
    _activeRequester = requester;
  }

  public L2PcInstance getActiveRequester()
  {
    return _activeRequester;
  }

  public boolean isProcessingRequest()
  {
    return (_activeRequester != null) || (_requestExpireTime > GameTimeController.getGameTicks());
  }

  public boolean isProcessingTransaction()
  {
    return (_activeRequester != null) || (_activeTradeList != null) || (_requestExpireTime > GameTimeController.getGameTicks());
  }

  public void onTransactionRequest(L2PcInstance partner)
  {
    _requestExpireTime = (GameTimeController.getGameTicks() + 150);
    partner.setActiveRequester(this);
  }

  public boolean isRequestExpired() {
    return _requestExpireTime <= GameTimeController.getGameTicks();
  }

  public void onTransactionResponse()
  {
    _requestExpireTime = 0L;
  }

  public void setActiveWarehouse(ItemContainer warehouse)
  {
    _activeWarehouse = warehouse;
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

  public void onTradeStart(L2PcInstance partner)
  {
    cancelActiveTrade();
    _activeTradeList = new TradeList(this);
    _activeTradeList.setPartner(partner);

    SystemMessage msg = new SystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1);
    msg.addString(partner.getName());
    sendPacket(msg);
    sendPacket(new TradeStart(this));
  }

  public void onTradeConfirm(L2PcInstance partner)
  {
    SystemMessage msg = new SystemMessage(SystemMessageId.S1_CONFIRMED_TRADE);
    msg.addString(partner.getName());
    sendPacket(msg);
    partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
    sendPacket(TradePressOtherOk.STATIC_PACKET);
  }

  public void onTradeCancel(L2PcInstance partner)
  {
    if (_activeTradeList == null) {
      return;
    }
    _activeTradeList.lock();
    _activeTradeList = null;

    sendPacket(new SendTradeDone(0));
    SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANCELED_TRADE);
    msg.addString(partner.getName());
    sendPacket(msg);
  }

  public void onTradeFinish(boolean successfull)
  {
    _activeTradeList = null;
    sendPacket(new SendTradeDone(1));
    if (successfull)
      sendPacket(new SystemMessage(SystemMessageId.TRADE_SUCCESSFUL));
  }

  public void startTrade(L2PcInstance partner)
  {
    onTradeStart(partner);
    partner.onTradeStart(this);
  }

  public void cancelActiveTrade()
  {
    if (_activeTradeList == null) {
      return;
    }
    L2PcInstance partner = _activeTradeList.getPartner();
    if (partner != null) partner.onTradeCancel(this);
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
    if (_sellList == null) _sellList = new TradeList(this);
    return _sellList;
  }

  public TradeList getBuyList()
  {
    if (_buyList == null) _buyList = new TradeList(this);
    return _buyList;
  }

  public void setPrivateStoreType(int type)
  {
    _privatestore = type;

    if ((_privatestore == 0) && ((getClient() == null) || (isOffline())))
    {
      store();
    }
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

    if (clan == null)
    {
      _clanId = 0;
      _clanPrivileges = 0;
      _pledgeType = 0;
      _powerGrade = 0;
      _lvlJoinedAcademy = 0;
      _apprentice = 0;
      _sponsor = 0;
      return;
    }

    if (!clan.isMember(getName()))
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
    if (getClan() == null)
    {
      return false;
    }

    return getObjectId() == getClan().getLeaderId();
  }

  protected void reduceArrowCount()
  {
    L2ItemInstance arrows = getInventory().destroyItem("Consume", getInventory().getPaperdollObjectId(8), 1, this, null);

    if ((arrows == null) || (arrows.getCount() == 0))
    {
      getInventory().unEquipItemInSlot(8);
      _arrowItem = null;
      sendPacket(new ItemList(this, false));
    }
    else if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate iu = new InventoryUpdate();
      iu.addModifiedItem(arrows);
      sendPacket(iu);
    } else {
      sendPacket(new ItemList(this, false));
    }
  }

  protected boolean checkAndEquipArrows()
  {
    if (getInventory().getPaperdollItem(8) == null)
    {
      _arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());

      if (_arrowItem != null)
      {
        getInventory().setPaperdollItem(8, _arrowItem);

        ItemList il = new ItemList(this, false);
        sendPacket(il);
      }

    }
    else
    {
      _arrowItem = getInventory().getPaperdollItem(8);
    }

    return _arrowItem != null;
  }

  public boolean equipArrowsWhenPickUp()
  {
    return checkAndEquipArrows();
  }

  public boolean disarmWeapons()
  {
    if (isCursedWeaponEquiped()) return false;

    L2ItemInstance wpn = getInventory().getPaperdollItem(7);
    if (wpn == null) wpn = getInventory().getPaperdollItem(14);
    if (wpn != null)
    {
      if (wpn.isWear()) {
        return false;
      }

      if (wpn.isAugmented()) {
        wpn.getAugmentation().removeBoni(this);
      }
      L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++)
        iu.addModifiedItem(unequiped[i]);
      sendPacket(iu);

      abortAttack();
      broadcastUserInfo();

      if (unequiped.length > 0)
      {
        SystemMessage sm = null;
        if (unequiped[0].getEnchantLevel() > 0)
        {
          sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
          sm.addNumber(unequiped[0].getEnchantLevel());
          sm.addItemName(unequiped[0].getItemId());
        }
        else
        {
          sm = new SystemMessage(SystemMessageId.S1_DISARMED);
          sm.addItemName(unequiped[0].getItemId());
        }
        sendPacket(sm);
      }

    }

    L2ItemInstance sld = getInventory().getPaperdollItem(8);
    if (sld != null)
    {
      if (sld.isWear()) {
        return false;
      }
      L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++)
        iu.addModifiedItem(unequiped[i]);
      sendPacket(iu);

      abortAttack();
      broadcastUserInfo();

      if (unequiped.length > 0)
      {
        SystemMessage sm = null;
        if (unequiped[0].getEnchantLevel() > 0)
        {
          sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
          sm.addNumber(unequiped[0].getEnchantLevel());
          sm.addItemName(unequiped[0].getItemId());
        }
        else
        {
          sm = new SystemMessage(SystemMessageId.S1_DISARMED);
          sm.addItemName(unequiped[0].getItemId());
        }
        sendPacket(sm);
      }
    }
    return true;
  }

  public void enterMovieMode()
  {
    setTarget(null);
    stopMove(null);
    setIsParalyzed(true);
    setIsInvul(true);
    setIsImobilised(true);
    sendPacket(new CameraMode(1));
  }

  public void leaveMovieMode()
  {
    setTarget(null);
    stopMove(null);
    setIsParalyzed(false);
    setIsInvul(false);
    setIsImobilised(false);
    sendPacket(new CameraMode(0));
  }

  public void specialCamera(L2Object l2object, int i, int j, int k, int l, int i1)
  {
    sendPacket(new SpecialCamera(l2object.getObjectId(), i, j, k, l, i1));
  }

  public boolean isUsingDualWeapon()
  {
    L2Weapon weaponItem = getActiveWeaponItem();
    if (weaponItem == null) return false;

    if (weaponItem.getItemType() == L2WeaponType.DUAL)
      return true;
    if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
      return true;
    if (weaponItem.getItemId() == 248) {
      return true;
    }
    return weaponItem.getItemId() == 252;
  }

  public void setUptime(long time)
  {
    _uptime = time;
  }

  public long getUptime()
  {
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
    }
  }

  public void leaveParty()
  {
    if (isInParty())
    {
      _party.removePartyMember(this);
      _party = null;
    }
  }

  public L2Party getParty()
  {
    return _party;
  }

  public void setIsGM(boolean status)
  {
    _isGm = status;
  }

  public boolean isGM()
  {
    return _isGm;
  }

  public void cancelCastMagic()
  {
    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    enableAllSkills();

    MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());

    Broadcast.toSelfAndKnownPlayersInRadius(this, msc, 810000L);
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

    if (broadcastType == 1) sendPacket(new UserInfo(this));
    if (broadcastType == 2) broadcastUserInfo();
  }

  public void setKarmaFlag(int flag)
  {
    sendPacket(new UserInfo(this));
    for (L2PcInstance player : getKnownList().getKnownPlayers().values())
      player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
  }

  public void broadcastKarma()
  {
    sendPacket(new UserInfo(this));
    for (L2PcInstance player : getKnownList().getKnownPlayers().values())
      player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
  }

  public void setOnlineStatus(boolean isOnline)
  {
    if (_isOnline != isOnline) {
      _isOnline = isOnline;
    }

    updateOnlineStatus();
  }

  public void setIsIn7sDungeon(boolean isIn7sDungeon)
  {
    if (_isIn7sDungeon != isIn7sDungeon) {
      _isIn7sDungeon = isIn7sDungeon;
    }
    updateIsIn7sDungeonStatus();
  }

  public void updateOnlineStatus()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
      statement.setInt(1, isOnline());
      statement.setLong(2, System.currentTimeMillis());
      statement.setInt(3, getObjectId());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("could not set char online status:").append(e).toString());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void updateIsIn7sDungeonStatus() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE characters SET isIn7sDungeon=?, lastAccess=? WHERE obj_id=?");
      statement.setInt(1, isIn7sDungeon() ? 1 : 0);
      statement.setLong(2, System.currentTimeMillis());
      statement.setInt(3, getObjectId());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("could not set char isIn7sDungeon status:").append(e).toString());
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private boolean createDb() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd,str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex,movement_multiplier,attack_speed_multiplier,colRad,colHeight,exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,last_recom_date,banchat_time,pccafe,name_color,title_color) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

      statement.setString(1, _accountName);
      statement.setInt(2, getObjectId());
      statement.setString(3, getName());
      statement.setInt(4, getLevel());
      statement.setInt(5, getMaxHp());
      statement.setDouble(6, getCurrentHp());
      statement.setInt(7, getMaxCp());
      statement.setDouble(8, getCurrentCp());
      statement.setInt(9, getMaxMp());
      statement.setDouble(10, getCurrentMp());
      statement.setInt(11, getAccuracy());
      statement.setInt(12, getCriticalHit(null, null));
      statement.setInt(13, getEvasionRate(null));
      statement.setInt(14, getMAtk(null, null));
      statement.setInt(15, getMDef(null, null));
      statement.setInt(16, getMAtkSpd());
      statement.setInt(17, getPAtk(null));
      statement.setInt(18, getPDef(null));
      statement.setInt(19, getPAtkSpd());
      statement.setInt(20, getRunSpeed());
      statement.setInt(21, getWalkSpeed());
      statement.setInt(22, getSTR());
      statement.setInt(23, getCON());
      statement.setInt(24, getDEX());
      statement.setInt(25, getINT());
      statement.setInt(26, getMEN());
      statement.setInt(27, getWIT());
      statement.setInt(28, getAppearance().getFace());
      statement.setInt(29, getAppearance().getHairStyle());
      statement.setInt(30, getAppearance().getHairColor());
      statement.setInt(31, getAppearance().getSex() ? 1 : 0);
      statement.setDouble(32, 1.0D);
      statement.setDouble(33, 1.0D);
      statement.setDouble(34, getTemplate().collisionRadius);
      statement.setDouble(35, getTemplate().collisionHeight);
      statement.setLong(36, getExp());
      statement.setInt(37, getSp());
      statement.setInt(38, getKarma());
      statement.setInt(39, getPvpKills());
      statement.setInt(40, getPkKills());
      statement.setInt(41, getClanId());
      statement.setInt(42, getMaxLoad());
      statement.setInt(43, getRace().ordinal());
      statement.setInt(44, getClassId().getId());
      statement.setLong(45, getDeleteTimer());
      statement.setInt(46, hasDwarvenCraft() ? 1 : 0);
      statement.setString(47, getTitle());
      statement.setInt(48, getAccessLevel());
      statement.setInt(49, isOnline());
      statement.setInt(50, isIn7sDungeon() ? 1 : 0);
      statement.setInt(51, getClanPrivileges());
      statement.setInt(52, getWantsPeace());
      statement.setInt(53, getBaseClass());
      statement.setInt(54, isNewbie());
      statement.setInt(55, isNoble() ? 1 : 0);
      statement.setLong(56, 0L);
      statement.setLong(57, System.currentTimeMillis());
      statement.setLong(58, getChatBanTimer());
      statement.setInt(59, getPcCafeScore());

      statement.setString(60, addZerosToColor(Integer.toHexString(getAppearance().getNameColor()).toUpperCase()));
      statement.setString(61, addZerosToColor(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.severe(new StringBuilder().append("Could not insert char data: ").append(e).toString());
      int i = 0;
      return i; } finally { try { con.close(); } catch (Exception e) {
      } }
    return true;
  }

  private static L2PcInstance restore(int objectId)
  {
    L2PcInstance player = null;
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,banchat_time,pccafe,name_color,title_color FROM characters WHERE obj_id=?");
      statement.setInt(1, objectId);
      ResultSet rset = statement.executeQuery();

      double currentCp = 0.0D;
      double currentHp = 0.0D;
      double currentMp = 0.0D;

      if (rset.next())
      {
        int activeClassId = rset.getInt("classid");
        boolean female = rset.getInt("sex") != 0;
        L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
        PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);

        player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
        restorePremServiceData(player, rset.getString("account_name"));
        player.setName(rset.getString("char_name"));
        player._lastAccess = rset.getLong("lastAccess");

        player.getStat().setExp(rset.getLong("exp"));
        player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
        player.getStat().setLevel(rset.getByte("level"));
        player.getStat().setSp(rset.getInt("sp"));

        player.setWantsPeace(rset.getInt("wantspeace"));

        player.setHeading(rset.getInt("heading"));

        player.setKarma(rset.getInt("karma"));
        player.setPvpKills(rset.getInt("pvpkills"));
        player.setPkKills(rset.getInt("pkkills"));
        player.setOnlineTime(rset.getLong("onlinetime"));
        player.setNewbie(rset.getInt("newbie"));
        player.setNoble(rset.getInt("nobless") == 1);

        player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
        if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
        {
          player.setClanJoinExpiryTime(0L);
        }
        player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
        if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
        {
          player.setClanCreateExpiryTime(0L);
        }

        int clanId = rset.getInt("clanid");
        player.setPowerGrade((int)rset.getLong("power_grade"));
        player.setPledgeType(rset.getInt("subpledge"));
        player.setLastRecomUpdate(rset.getLong("last_recom_date"));

        if (clanId > 0)
        {
          player.setClan(ClanTable.getInstance().getClan(clanId));
        }

        if (player.getClan() != null)
        {
          if (player.getClan().getLeaderId() != player.getObjectId())
          {
            if (player.getPowerGrade() == 0)
            {
              player.setPowerGrade(5);
            }
            player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
          }
          else
          {
            player.setClanPrivileges(8388606);
            player.setPowerGrade(1);
          }
        }
        else
        {
          player.setClanPrivileges(0);
        }

        player.setDeleteTimer(rset.getLong("deletetime"));

        player.setTitle(rset.getString("title"));
        player.setAccessLevel(rset.getInt("accesslevel"));
        player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
        player.setUptime(System.currentTimeMillis());

        currentHp = rset.getDouble("curHp");
        curHp = currentHp;
        player.setCurrentHp(currentHp);
        currentCp = rset.getDouble("curCp");
        curCp = currentCp;
        player.setCurrentCp(currentCp);
        currentMp = rset.getDouble("curMp");
        curMp = currentMp;
        player.setCurrentMp(currentMp);

        player.checkRecom(rset.getInt("rec_have"), rset.getInt("rec_left"));

        player._classIndex = 0;
        try { player.setBaseClass(rset.getInt("base_class")); } catch (Exception e) {
          player.setBaseClass(activeClassId);
        }
        if (restoreSubClassData(player))
        {
          if (activeClassId != player.getBaseClass())
          {
            for (SubClass subClass : player.getSubClasses().values())
              if (subClass.getClassId() == activeClassId)
                player._classIndex = subClass.getClassIndex();
          }
        }
        if ((player.getClassIndex() == 0) && (activeClassId != player.getBaseClass()))
        {
          player.setClassId(player.getBaseClass());
          _log.warning(new StringBuilder().append("Player ").append(player.getName()).append(" reverted to base class. Possibly has tried a relogin exploit while subclassing.").toString());
        } else {
          player._activeClass = activeClassId;
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

        player.setChatBanTimer(rset.getLong("banchat_time"));
        player.pcCafeScore = rset.getInt("pccafe");

        player.getAppearance().setNameColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("name_color")).toString()).intValue(), false);
        player.getAppearance().setTitleColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("title_color")).toString()).intValue());

        player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));

        PreparedStatement stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?");
        stmt.setString(1, player._accountName);
        stmt.setInt(2, objectId);
        ResultSet chars = stmt.executeQuery();

        while (chars.next())
        {
          Integer charId = Integer.valueOf(chars.getInt("obj_Id"));
          String charName = chars.getString("char_name");
          player._chars.put(charId, charName);
        }

        chars.close();
        stmt.close();
      }

      rset.close();
      statement.close();
      player.restoreCharData();
      player.rewardSkills();
      player.setCurrentCp(currentCp);
      player.setCurrentHp(currentHp);
      player.setCurrentMp(currentMp);
      player.setPet(L2World.getInstance().getPet(player.getObjectId()));
      if (player.getPet() != null) player.getPet().setOwner(player);
      player.refreshOverloaded();
    }
    catch (Exception e)
    {
      _log.severe(new StringBuilder().append("Could not restore char data: ").append(e).toString());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return player;
  }

  public Forum getMail()
  {
    if (_forumMail == null)
    {
      setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));

      if (_forumMail == null)
      {
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
    if (_forumMemo == null)
    {
      setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));

      if (_forumMemo == null)
      {
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
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC");
      statement.setInt(1, player.getObjectId());

      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        SubClass subClass = new SubClass();
        subClass.setClassId(rset.getInt("class_id"));
        subClass.setLevel(rset.getByte("level"));
        subClass.setExp(rset.getLong("exp"));
        subClass.setSp(rset.getInt("sp"));
        subClass.setClassIndex(rset.getInt("class_index"));

        player.getSubClasses().put(Integer.valueOf(subClass.getClassIndex()), subClass);
      }

      statement.close();
    }
    catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not restore classes for ").append(player.getName()).append(": ").append(e).toString());
      e.printStackTrace();
    } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return true;
  }

  private void restoreCharData()
  {
    restoreSkills();

    _macroses.restore();

    _shortCuts.restore();

    restoreHenna();

    if (Config.ALT_RECOMMEND) restoreRecom();

    if (!isSubClassActive())
      restoreRecipeBook();
  }

  private void storeRecipeBook()
  {
    if (isSubClassActive())
      return;
    if ((getCommonRecipeBook().length == 0) && (getDwarvenRecipeBook().length == 0)) {
      return;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
      statement.setInt(1, getObjectId());
      statement.execute();
      statement.close();

      L2RecipeList[] recipes = getCommonRecipeBook();

      for (int count = 0; count < recipes.length; count++)
      {
        statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,0)");
        statement.setInt(1, getObjectId());
        statement.setInt(2, recipes[count].getId());
        statement.execute();
        statement.close();
      }

      recipes = getDwarvenRecipeBook();
      for (int count = 0; count < recipes.length; count++)
      {
        statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,1)");
        statement.setInt(1, getObjectId());
        statement.setInt(2, recipes[count].getId());
        statement.execute();
        statement.close();
      }
    }
    catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not store recipe book data: ").append(e).toString());
    } finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private void restoreRecipeBook() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE char_id=?");
      statement.setInt(1, getObjectId());
      ResultSet rset = statement.executeQuery();

      while (rset.next()) {
        L2RecipeList recipe = RecipeController.getInstance().getRecipeList(rset.getInt("id") - 1);

        if (rset.getInt("type") == 1) {
          registerDwarvenRecipeList(recipe); continue;
        }
        registerCommonRecipeList(recipe);
      }

      rset.close();
      statement.close();
    }
    catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not restore recipe book data:").append(e).toString());
    } finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public synchronized void store()
  {
    storeCharBase();
    storeCharSub();
    storeEffect();
    storeRecipeBook();
  }

  private void storeCharBase()
  {
    Connection con = null;
    try
    {
      int currentClassIndex = getClassIndex();
      _classIndex = 0;
      long exp = getStat().getExp();
      int level = getStat().getLevel();
      int sp = getStat().getSp();
      _classIndex = currentClassIndex;

      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,last_recom_date=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,banchat_time=?,pccafe=?,name_color=?,title_color=? WHERE obj_id=?");
      statement.setInt(1, level);
      statement.setInt(2, getMaxHp());
      statement.setDouble(3, getCurrentHp());
      statement.setInt(4, getMaxCp());
      statement.setDouble(5, getCurrentCp());
      statement.setInt(6, getMaxMp());
      statement.setDouble(7, getCurrentMp());
      statement.setInt(8, getSTR());
      statement.setInt(9, getCON());
      statement.setInt(10, getDEX());
      statement.setInt(11, getINT());
      statement.setInt(12, getMEN());
      statement.setInt(13, getWIT());
      statement.setInt(14, getAppearance().getFace());
      statement.setInt(15, getAppearance().getHairStyle());
      statement.setInt(16, getAppearance().getHairColor());
      statement.setInt(17, getHeading());
      statement.setInt(18, _observerMode ? _obsX : getX());
      statement.setInt(19, _observerMode ? _obsY : getY());
      statement.setInt(20, _observerMode ? _obsZ : getZ());
      statement.setLong(21, exp);
      statement.setLong(22, getExpBeforeDeath());
      statement.setInt(23, sp);
      statement.setInt(24, getKarma());
      statement.setInt(25, getPvpKills());
      statement.setInt(26, getPkKills());
      statement.setInt(27, getRecomHave());
      statement.setInt(28, getRecomLeft());
      statement.setInt(29, getClanId());
      statement.setInt(30, getMaxLoad());
      statement.setInt(31, getRace().ordinal());
      statement.setInt(32, getClassId().getId());
      statement.setLong(33, getDeleteTimer());
      statement.setString(34, getTitle());
      statement.setInt(35, getAccessLevel());
      statement.setInt(36, isOnline());
      statement.setInt(37, isIn7sDungeon() ? 1 : 0);
      statement.setInt(38, getClanPrivileges());
      statement.setInt(39, getWantsPeace());
      statement.setInt(40, getBaseClass());

      long totalOnlineTime = _onlineTime;

      if (_onlineBeginTime > 0L) {
        totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000L;
      }
      statement.setLong(41, totalOnlineTime);
      statement.setInt(42, isInJail() ? 1 : 0);
      statement.setLong(43, getJailTimer());
      statement.setInt(44, isNewbie());
      statement.setInt(45, isNoble() ? 1 : 0);
      statement.setLong(46, getPowerGrade());
      statement.setInt(47, getPledgeType());
      statement.setLong(48, getLastRecomUpdate());
      statement.setInt(49, getLvlJoinedAcademy());
      statement.setLong(50, getApprentice());
      statement.setLong(51, getSponsor());
      statement.setInt(52, getAllianceWithVarkaKetra());
      statement.setLong(53, getClanJoinExpiryTime());
      statement.setLong(54, getClanCreateExpiryTime());
      statement.setString(55, getName());
      statement.setLong(56, getDeathPenaltyBuffLevel());
      statement.setLong(57, getChatBanTimer());
      statement.setInt(58, getPcCafeScore());

      statement.setString(59, addZerosToColor(Integer.toHexString(getAppearance().getNameColorForSave()).toUpperCase()));
      statement.setString(60, addZerosToColor(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
      statement.setInt(61, getObjectId());

      statement.execute();
      statement.close();
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not store char base data: ").append(e).toString()); } finally {
      try { con.close(); } catch (Exception e) {
      }
    }
  }

  private void storeCharSub() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      if (getTotalSubClasses() > 0)
      {
        for (SubClass subClass : getSubClasses().values())
        {
          PreparedStatement statement = con.prepareStatement("UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?");
          statement.setLong(1, subClass.getExp());
          statement.setInt(2, subClass.getSp());
          statement.setInt(3, subClass.getLevel());
          statement.setInt(4, subClass.getClassId());
          statement.setInt(5, getObjectId());
          statement.setInt(6, subClass.getClassIndex());

          statement.execute();
          statement.close();
        }
      }
    }
    catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not store sub class data for ").append(getName()).append(": ").append(e).toString()); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void storeEffect() {
    if (!Config.STORE_SKILL_COOLTIME) return;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getClassIndex());
      statement.execute();
      statement.close();

      int buff_index = 0;

      List storedSkills = new FastList();

      L2Effect[] effects = getAllEffects();
      for (L2Effect effect : effects)
      {
        if ((effect == null) || (!effect.getInUse()) || (effect.getSkill().isToggle()))
          continue;
        int skillId = effect.getSkill().getId();

        if (storedSkills.contains(Integer.valueOf(skillId))) {
          continue;
        }
        storedSkills.add(Integer.valueOf(skillId));

        buff_index++;

        statement = con.prepareStatement("INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?)");
        statement.setInt(1, getObjectId());
        statement.setInt(2, skillId);
        statement.setInt(3, effect.getSkill().getLevel());
        statement.setInt(4, effect.getCount());
        statement.setInt(5, effect.getTime());

        if (ReuseTimeStamps.containsKey(Integer.valueOf(skillId)))
        {
          TimeStamp t = (TimeStamp)ReuseTimeStamps.remove(Integer.valueOf(skillId));
          statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0L);
        }
        else {
          statement.setLong(6, 0L);
        }

        statement.setInt(7, 0);
        statement.setInt(8, getClassIndex());
        statement.setInt(9, buff_index);
        statement.execute();
        statement.close();
      }

      for (TimeStamp t : ReuseTimeStamps.values())
      {
        if (t.hasNotPassed())
        {
          if (storedSkills.contains(Integer.valueOf(t.getSkill()))) {
            continue;
          }
          storedSkills.add(Integer.valueOf(t.getSkill()));

          buff_index++;
          statement = con.prepareStatement("INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?)");
          statement.setInt(1, getObjectId());
          statement.setInt(2, t.getSkill());
          statement.setInt(3, -1);
          statement.setInt(4, -1);
          statement.setInt(5, -1);
          statement.setLong(6, t.getReuse());
          statement.setInt(7, 1);
          statement.setInt(8, getClassIndex());
          statement.setInt(9, buff_index);
          statement.execute();
          statement.close();
        }
      }
      ReuseTimeStamps.clear();
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not store char effect data: ").append(e).toString()); } finally {
      try { con.close();
      } catch (Exception e)
      {
      }
    }
  }

  public int isOnline() {
    return _isOnline ? 1 : 0;
  }

  public boolean isIn7sDungeon()
  {
    return _isIn7sDungeon;
  }

  public L2Skill addSkill(L2Skill newSkill, boolean store)
  {
    L2Skill oldSkill = super.addSkill(newSkill);

    if (store) storeSkill(newSkill, oldSkill, -1);

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

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      if (oldSkill != null)
      {
        PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
        statement.setInt(1, oldSkill.getId());
        statement.setInt(2, getObjectId());
        statement.setInt(3, getClassIndex());
        statement.execute();
        statement.close();
      }
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Error could not delete skill: ").append(e).toString());
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
    L2ShortCut[] allShortCuts = getAllShortCuts();

    for (L2ShortCut sc : allShortCuts)
    {
      if ((sc != null) && (skill != null) && (sc.getId() == skill.getId()) && (sc.getType() == 2)) {
        deleteShortCut(sc.getSlot(), sc.getPage());
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
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      if ((oldSkill != null) && (newSkill != null))
      {
        PreparedStatement statement = con.prepareStatement("UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?");
        statement.setInt(1, newSkill.getLevel());
        statement.setInt(2, oldSkill.getId());
        statement.setInt(3, getObjectId());
        statement.setInt(4, classIndex);
        statement.execute();
        statement.close();
      }
      else if (newSkill != null)
      {
        PreparedStatement statement = con.prepareStatement("INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)");
        statement.setInt(1, getObjectId());
        statement.setInt(2, newSkill.getId());
        statement.setInt(3, newSkill.getLevel());
        statement.setString(4, newSkill.getName());
        statement.setInt(5, classIndex);
        statement.execute();
        statement.close();
      }
      else
      {
        _log.warning("could not store new skill. its NULL");
      }
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Error could not store char skills: ").append(e).toString());
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void checkAllowedSkills() {
    boolean flag = false;
    if (!isGM())
    {
      Collection collection = SkillTreeTable.getInstance().getAllowedSkills(getClassId());
      L2Skill[] al2skill = getAllSkills();
      int i = al2skill.length;
      for (int j = 0; j < i; j++)
      {
        L2Skill l2skill = al2skill[j];
        int k = l2skill.getId();
        boolean flag1 = false;
        Iterator iterator = collection.iterator();

        while (iterator.hasNext())
        {
          L2SkillLearn l2skilllearn = (L2SkillLearn)iterator.next();
          if (l2skilllearn.getId() == k) {
            flag1 = true;
          }
        }
        if (Config.CHECK_NOBLE_SKILLS)
        {
          if ((isNoble()) && (k >= 325) && (k <= 327))
            flag1 = true;
          if ((isNoble()) && (k >= 1323) && (k <= 1327))
            flag1 = true;
        }
        else
        {
          if ((k >= 325) && (k <= 327))
            flag1 = true;
          if ((k >= 1323) && (k <= 1327))
            flag1 = true;
        }
        if (Config.CHECK_HERO_SKILLS)
        {
          if ((isHero()) && (k >= 395) && (k <= 396))
            flag1 = true;
          if ((isHero()) && (k >= 1374) && (k <= 1376))
            flag1 = true;
        }
        else
        {
          if ((k >= 395) && (k <= 396))
            flag1 = true;
          if ((k >= 1374) && (k <= 1376))
            flag1 = true;
        }
        if ((getClan() != null) && (k >= 370) && (k <= 391))
          flag1 = true;
        if ((getClan() != null) && (k >= 246) && (k <= 247) && (getClan().getLeaderId() == getObjectId()))
          flag1 = true;
        if ((k >= 1312) && (k <= 1322))
          flag1 = true;
        if ((k >= 1368) && (k <= 1373))
          flag1 = true;
        if ((k >= 3000) && (k < 7000))
        {
          flag1 = true;
          if ((!isCursedWeaponEquiped()) && ((k == 3603) || (k == 3629)))
            flag1 = false;
        }
        if (Config.LIST_NON_CHECK_SKILLS.contains(Integer.valueOf(k))) {
          flag1 = true;
        }
        if (flag1)
          continue;
        removeSkill(l2skill);
        sendMessage(new StringBuilder().append("Skill ").append(l2skill.getName()).append(" removed and gm informed!").toString());
        _log.warning(new StringBuilder().append("WARNING!!! Character ").append(getName()).append(" Cheater!. Skill ").append(l2skill.getName()).append(" removed. Skill level ").append(l2skill.getLevel()).toString());
        GmListTable.broadcastMessageToGMs(new StringBuilder().append("WARNING!!! Character ").append(getName()).append(" Cheater!. Skill ").append(l2skill.getName()).append(" removed").toString());
      }
    }
  }

  private void restoreSkills()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getClassIndex());
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("skill_id");
        int level = rset.getInt("skill_level");

        if (id > 9000)
        {
          continue;
        }
        L2Skill skill = SkillTable.getInstance().getInfo(id, level);

        super.addSkill(skill);
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Could not restore character skills: ").append(e).toString());
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void restoreEffects() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay FROM character_skills_save WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getClassIndex());
      statement.setInt(3, 0);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int skillId = rset.getInt("skill_id");
        int skillLvl = rset.getInt("skill_level");
        int effectCount = rset.getInt("effect_count");
        int effectCurTime = rset.getInt("effect_cur_time");
        long reuseDelay = rset.getLong("reuse_delay");

        if ((skillId == -1) || (effectCount == -1) || (effectCurTime == -1) || (reuseDelay < 0L))
          continue;
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
        skill.getEffects(this, this);

        if (reuseDelay > 10L)
        {
          disableSkill(skillId, reuseDelay);
          addTimeStamp(new TimeStamp(skillId, reuseDelay));
          sendPacket(new MagicSkillUser(this, skill.getDisplayId(), skill.getLevel(), 300, (int)reuseDelay));
        }
        L2Effect[] effects = getAllEffects();
        for (L2Effect effect : effects)
        {
          if (effect.getSkill().getId() != skillId)
            continue;
          effect.setCount(effectCount);
          effect.setFirstTime(effectCurTime);
        }
      }

      rset.close();
      statement.close();

      statement = con.prepareStatement("SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay FROM character_skills_save WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getClassIndex());
      statement.setInt(3, 1);
      rset = statement.executeQuery();

      while (rset.next())
      {
        int skillId = rset.getInt("skill_id");
        long reuseDelay = rset.getLong("reuse_delay");

        if (reuseDelay <= 0L)
          continue;
        disableSkill(skillId, reuseDelay);
        addTimeStamp(new TimeStamp(skillId, reuseDelay));
        for (L2Skill skill : getAllSkills()) {
          if (skill.getId() != skillId)
            continue;
          sendPacket(new MagicSkillUser(this, skill.getDisplayId(), skill.getLevel(), 300, (int)reuseDelay));
        }
      }
      rset.close();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getClassIndex());
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e) {
      _log.warning(new StringBuilder().append("Could not restore active effect data: ").append(e).toString());
    } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    updateEffectIcons();
  }

  private void restoreHenna()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, getClassIndex());
      ResultSet rset = statement.executeQuery();

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

        L2HennaInstance sym = null;

        if (symbol_id != 0)
        {
          L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);

          if (tpl != null)
          {
            sym = new L2HennaInstance(tpl);
            _henna[(slot - 1)] = sym;
          }
        }
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("could not restore henna: ").append(e).toString());
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
    recalcHennaStats();
  }

  private void restoreRecom()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT char_id,target_id FROM character_recommends WHERE char_id=?");
      statement.setInt(1, getObjectId());
      ResultSet rset = statement.executeQuery();
      while (rset.next())
      {
        _recomChars.add(Integer.valueOf(rset.getInt("target_id")));
      }

      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("could not restore recommendations: ").append(e).toString());
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public int getHennaEmptySlots() {
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
    L2HennaInstance henna = _henna[slot];
    _henna[slot] = null;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, slot + 1);
      statement.setInt(3, getClassIndex());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("could not remove char henna: ").append(e).toString());
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
    recalcHennaStats();

    sendPacket(new HennaInfo(this));

    sendPacket(new UserInfo(this));

    getInventory().addItem("Henna", henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, this, null);

    SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
    sm.addItemName(henna.getItemIdDye());
    sm.addNumber(henna.getAmountDyeRequire() / 2);
    sendPacket(sm);

    return true;
  }

  public boolean addHenna(L2HennaInstance henna)
  {
    if (getHennaEmptySlots() == 0)
    {
      sendMessage("You may not have more than three equipped symbols at a time.");
      return false;
    }

    boolean x = false;
    for (L2HennaInstance henna1 : HennaTreeTable.getInstance().getAvailableHenna(getClassId()))
    {
      if (henna1.getSymbolId() != henna.getSymbolId())
        continue;
      x = true;
      break;
    }

    if (!x) return false;

    for (int i = 0; i < 3; i++)
    {
      if (_henna[i] != null)
        continue;
      _henna[i] = henna;

      recalcHennaStats();

      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)");
        statement.setInt(1, getObjectId());
        statement.setInt(2, henna.getSymbolId());
        statement.setInt(3, i + 1);
        statement.setInt(4, getClassIndex());
        statement.execute();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning(new StringBuilder().append("could not save char henna: ").append(e).toString());
      }
      finally {
        try {
          con.close();
        } catch (Exception e) {
        }
      }
      HennaInfo hi = new HennaInfo(this);
      sendPacket(hi);

      UserInfo ui = new UserInfo(this);
      sendPacket(ui);

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
      if (_henna[i] != null) {
        _hennaINT += _henna[i].getStatINT();
        _hennaSTR += _henna[i].getStatSTR();
        _hennaMEN += _henna[i].getStatMEM();
        _hennaCON += _henna[i].getStatCON();
        _hennaWIT += _henna[i].getStatWIT();
        _hennaDEX += _henna[i].getStatDEX();
      }
    }
    if (_hennaINT > 5) _hennaINT = 5;
    if (_hennaSTR > 5) _hennaSTR = 5;
    if (_hennaMEN > 5) _hennaMEN = 5;
    if (_hennaCON > 5) _hennaCON = 5;
    if (_hennaWIT > 5) _hennaWIT = 5;
    if (_hennaDEX > 5) _hennaDEX = 5;
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

  public boolean isAutoAttackable(L2Character attacker)
  {
    if ((attacker == this) || (attacker == getPet())) {
      return false;
    }

    if ((attacker instanceof L2MonsterInstance)) {
      return true;
    }

    if ((getParty() != null) && (getParty().getPartyMembers().contains(attacker))) {
      return false;
    }

    if (((attacker instanceof L2PcInstance)) && (((L2PcInstance)attacker).isInOlympiadMode()))
    {
      return (isInOlympiadMode()) && (isOlympiadStart()) && (((L2PcInstance)attacker).getOlympiadGameId() == getOlympiadGameId());
    }

    if ((getClan() != null) && (attacker != null) && (getClan().isMember(attacker.getName()))) {
      return false;
    }
    if (((attacker instanceof L2PlayableInstance)) && (isInsideZone(2))) {
      return false;
    }

    if ((getKarma() > 0) || (getPvpFlag() > 0)) {
      return true;
    }

    if ((attacker instanceof L2PcInstance))
    {
      if ((getDuelState() == 1) && (getDuelId() == ((L2PcInstance)attacker).getDuelId()))
      {
        return true;
      }
      if ((isInsideZone(1)) && (((L2PcInstance)attacker).isInsideZone(1))) {
        return true;
      }
      if (getClan() != null)
      {
        Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
        if (siege != null)
        {
          if ((siege.checkIsDefender(((L2PcInstance)attacker).getClan())) && (siege.checkIsDefender(getClan())))
          {
            return false;
          }

          if ((siege.checkIsAttacker(((L2PcInstance)attacker).getClan())) && (siege.checkIsAttacker(getClan())))
          {
            return false;
          }
        }

        if ((getClan() != null) && (((L2PcInstance)attacker).getClan() != null) && (getClan().isAtWarWith(Integer.valueOf(((L2PcInstance)attacker).getClanId()))) && (getWantsPeace() == 0) && (((L2PcInstance)attacker).getWantsPeace() == 0) && (!isAcademyMember()))
        {
          return true;
        }
      }
    } else if ((attacker instanceof L2SiegeGuardInstance))
    {
      if (getClan() != null)
      {
        Siege siege = SiegeManager.getInstance().getSiege(this);
        return (siege != null) && (siege.checkIsAttacker(getClan()));
      }
    }

    return false;
  }

  public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
  {
    if (isDead())
    {
      abortCast();
      sendPacket(new ActionFailed());
      return;
    }

    if ((isWearingFormalWear()) && (!skill.isPotion()))
    {
      sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR));

      sendPacket(new ActionFailed());
      abortCast();
      return;
    }

    if (inObserverMode())
    {
      sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
      abortCast();
      sendPacket(new ActionFailed());
      return;
    }

    if (skill.isToggle())
    {
      L2Effect effect = getFirstEffect(skill);

      if (effect != null)
      {
        effect.exit();

        sendPacket(new ActionFailed());
        return;
      }

    }

    if (skill.isPassive())
    {
      sendPacket(new ActionFailed());
      return;
    }

    if (((skill.getId() == 13) || (skill.getId() == 299) || (skill.getId() == 448)) && (!SiegeManager.getInstance().checkIfOkToSummon(this, false)))
    {
      return;
    }

    if ((getCurrentSkill() != null) && (isCastingNow()))
    {
      if (skill.getId() == getCurrentSkill().getSkillId())
      {
        sendPacket(new ActionFailed());
        return;
      }
      setQueuedSkill(skill, forceUse, dontMove);
      sendPacket(new ActionFailed());
      return;
    }
    if (getQueuedSkill() != null) {
      setQueuedSkill(null, false, false);
    }

    L2Object target = null;
    L2Skill.SkillTargetType sklTargetType = skill.getTargetType();
    L2Skill.SkillType sklType = skill.getSkillType();

    Point3D worldPosition = getCurrentSkillWorldPosition();

    if ((sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND) && (worldPosition == null))
    {
      _log.info(new StringBuilder().append("WorldPosition is null for skill: ").append(skill.getName()).append(", player: ").append(getName()).append(".").toString());
      sendPacket(new ActionFailed());
      return;
    }

    switch (2.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[sklTargetType.ordinal()])
    {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
      target = this;
      break;
    case 7:
      target = getPet();
      break;
    default:
      target = getTarget();
    }

    if (target == null)
    {
      sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
      sendPacket(new ActionFailed());
      return;
    }

    if ((target instanceof L2DoorInstance))
    {
      if ((((L2DoorInstance)target).getCastle() != null) && (((L2DoorInstance)target).getCastle().getCastleId() > 0) && (!((L2DoorInstance)target).getCastle().getSiege().getIsInProgress()))
      {
        sendPacket(new SystemMessage(SystemMessageId.ONLY_DURING_SIEGE));
        sendPacket(new ActionFailed());
        return;
      }

    }

    if (isInDuel())
    {
      if (((!(target instanceof L2PcInstance)) || (((L2PcInstance)target).getDuelId() != getDuelId())) && ((!(target instanceof L2SummonInstance)) || (((L2SummonInstance)target).getOwner().getDuelId() != getDuelId())))
      {
        sendMessage("You cannot do this while duelling.");
        sendPacket(new ActionFailed());
        return;
      }

    }

    if ((isSkillDisabled(skill.getId())) && (getAccessLevel() < Config.GM_PEACEATTACK))
    {
      if ((!isInFunEvent()) || (!target.isInFunEvent()))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE);
        sm.addString(skill.getName());
        sendPacket(sm);

        sendPacket(new ActionFailed());
        return;
      }

    }

    if ((isAllSkillsDisabled()) && (getAccessLevel() < Config.GM_PEACEATTACK))
    {
      if ((!isInFunEvent()) || (!target.isInFunEvent()))
      {
        sendPacket(new ActionFailed());
        return;
      }

    }

    if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
    {
      sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));

      sendPacket(new ActionFailed());
      return;
    }

    if (getCurrentHp() <= skill.getHpConsume())
    {
      sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_HP));

      sendPacket(new ActionFailed());
      return;
    }

    if (skill.getItemConsume() > 0)
    {
      L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());

      if ((requiredItems == null) || (requiredItems.getCount() < skill.getItemConsume()))
      {
        if (sklType == L2Skill.SkillType.SUMMON)
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
          sm.addItemName(skill.getItemConsumeId());
          sm.addNumber(skill.getItemConsume());
          sendPacket(sm);
          return;
        }

        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
        return;
      }

    }

    if (!skill.getWeaponDependancy(this))
    {
      sendPacket(new ActionFailed());
      return;
    }

    if (!skill.checkCondition(this, target, false))
    {
      sendPacket(new ActionFailed());
      return;
    }

    if (isAlikeDead())
    {
      sendPacket(new ActionFailed());
      return;
    }

    if ((isSitting()) && (!skill.isPotion()))
    {
      sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));

      sendPacket(new ActionFailed());
      return;
    }

    if ((isFishing()) && (sklType != L2Skill.SkillType.PUMPING) && (sklType != L2Skill.SkillType.REELING) && (sklType != L2Skill.SkillType.FISHING))
    {
      sendPacket(new SystemMessage(SystemMessageId.ONLY_FISHING_SKILLS_NOW));
      return;
    }

    if (skill.isOffensive())
    {
      if ((isInsidePeaceZone(this, target)) && (getAccessLevel() < Config.GM_PEACEATTACK))
      {
        if ((!isInFunEvent()) || (!target.isInFunEvent()))
        {
          sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
          sendPacket(new ActionFailed());
          return;
        }
      }

      if ((isInOlympiadMode()) && (!isOlympiadStart()))
      {
        sendPacket(new ActionFailed());
        return;
      }

      if ((!target.isAttackable()) && (getAccessLevel() < Config.GM_PEACEATTACK))
      {
        if ((!isInFunEvent()) || (!target.isInFunEvent()))
        {
          sendPacket(new ActionFailed());
          return;
        }

      }

      if ((!target.isAutoAttackable(this)) && (!forceUse) && ((!_inEventCTF) || (!CTF._started)) && (sklTargetType != L2Skill.SkillTargetType.TARGET_AURA) && (sklTargetType != L2Skill.SkillTargetType.TARGET_GROUND) && (sklTargetType != L2Skill.SkillTargetType.TARGET_CLAN) && (sklTargetType != L2Skill.SkillTargetType.TARGET_ALLY) && (sklTargetType != L2Skill.SkillTargetType.TARGET_PARTY) && (sklTargetType != L2Skill.SkillTargetType.TARGET_SELF))
      {
        sendPacket(new ActionFailed());
        return;
      }

      if (dontMove)
      {
        if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND)
        {
          if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + getTemplate().collisionRadius, false, false))
          {
            sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));

            sendPacket(new ActionFailed());
            return;
          }
        }
        else if ((skill.getCastRange() > 0) && (!isInsideRadius(target, skill.getCastRange() + getTemplate().collisionRadius, false, false)))
        {
          sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));

          sendPacket(new ActionFailed());
          return;
        }
      }

    }

    if (!skill.isOffensive())
    {
      if (((target instanceof L2MonsterInstance)) && (!forceUse) && (sklTargetType != L2Skill.SkillTargetType.TARGET_PET) && (sklTargetType != L2Skill.SkillTargetType.TARGET_GROUND) && (sklTargetType != L2Skill.SkillTargetType.TARGET_AURA) && (sklTargetType != L2Skill.SkillTargetType.TARGET_CLAN) && (sklTargetType != L2Skill.SkillTargetType.TARGET_SELF) && (sklTargetType != L2Skill.SkillTargetType.TARGET_PARTY) && (sklTargetType != L2Skill.SkillTargetType.TARGET_ALLY) && (sklTargetType != L2Skill.SkillTargetType.TARGET_CORPSE_MOB) && (sklTargetType != L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB) && (sklType != L2Skill.SkillType.BEAST_FEED) && (sklType != L2Skill.SkillType.DELUXE_KEY_UNLOCK) && (sklType != L2Skill.SkillType.UNLOCK))
      {
        sendPacket(new ActionFailed());
        return;
      }

    }

    if (sklType == L2Skill.SkillType.SPOIL)
    {
      if (!(target instanceof L2MonsterInstance))
      {
        sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));

        sendPacket(new ActionFailed());
        return;
      }

    }

    if ((sklType == L2Skill.SkillType.SWEEP) && ((target instanceof L2Attackable)))
    {
      int spoilerId = ((L2Attackable)target).getIsSpoiledBy();

      if (((L2Attackable)target).isDead()) {
        if (!((L2Attackable)target).isSpoil())
        {
          sendPacket(new SystemMessage(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED));

          sendPacket(new ActionFailed());
          return;
        }

        if ((getObjectId() != spoilerId) && (!isInLooterParty(spoilerId)))
        {
          sendPacket(new SystemMessage(SystemMessageId.SWEEP_NOT_ALLOWED));

          sendPacket(new ActionFailed());
          return;
        }
      }

    }

    if (sklType == L2Skill.SkillType.DRAIN_SOUL)
    {
      if (!(target instanceof L2MonsterInstance))
      {
        sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));

        sendPacket(new ActionFailed());
        return;
      }

    }

    switch (2.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[sklTargetType.ordinal()])
    {
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
      break;
    default:
      if ((checkPvpSkill(target, skill)) || (getAccessLevel() >= Config.GM_PEACEATTACK))
        break;
      if ((isInFunEvent()) && (target.isInFunEvent())) {
        break;
      }
      sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));

      sendPacket(new ActionFailed());
      return;
    }

    if ((sklTargetType == L2Skill.SkillTargetType.TARGET_HOLY) && (!TakeCastle.checkIfOkToCastSealOfRule(this, false)))
    {
      sendPacket(new ActionFailed());
      abortCast();
      return;
    }

    if ((sklType == L2Skill.SkillType.SIEGEFLAG) && (!SiegeFlag.checkIfOkToPlaceFlag(this, false)))
    {
      sendPacket(new ActionFailed());
      abortCast();
      return;
    }
    if ((sklType == L2Skill.SkillType.STRSIEGEASSAULT) && (!StrSiegeAssault.checkIfOkToUseStriderSiegeAssault(this, false)))
    {
      sendPacket(new ActionFailed());
      abortCast();
      return;
    }

    if (skill.getCastRange() > 0)
    {
      if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND)
      {
        if (!GeoData.getInstance().canSeeTarget(this, worldPosition))
        {
          sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
          sendPacket(new ActionFailed());
          return;
        }
      }
      else if (!GeoData.getInstance().canSeeTarget(this, target))
      {
        sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
        sendPacket(new ActionFailed());
        return;
      }

    }

    setCurrentSkill(skill, forceUse, dontMove);

    super.useMagic(skill);
  }

  public boolean isInLooterParty(int LooterId)
  {
    L2PcInstance looter = (L2PcInstance)L2World.getInstance().findObject(LooterId);

    if ((isInParty()) && (getParty().isInCommandChannel()) && (looter != null)) {
      return getParty().getCommandChannel().getMembers().contains(looter);
    }
    if ((isInParty()) && (looter != null)) {
      return getParty().getPartyMembers().contains(looter);
    }
    return false;
  }

  public boolean checkPvpSkill(L2Object target, L2Skill skill)
  {
    if ((_inEventCTF) && (CTF._started)) {
      return true;
    }
    if ((target != null) && (target != this) && ((target instanceof L2PcInstance)) && ((!isInDuel()) || (((L2PcInstance)target).getDuelId() != getDuelId())) && (!isInsideZone(1)) && (!((L2PcInstance)target).isInsideZone(1)))
    {
      if (skill.isPvpSkill())
      {
        if ((getClan() != null) && (((L2PcInstance)target).getClan() != null))
        {
          if (getClan().isAtWarWith(Integer.valueOf(((L2PcInstance)target).getClan().getClanId())))
            return true;
        }
        if ((((L2PcInstance)target).getPvpFlag() == 0) && (((L2PcInstance)target).getKarma() == 0))
        {
          return false;
        }
      } else if ((getCurrentSkill() != null) && (!getCurrentSkill().isCtrlPressed()) && (skill.isOffensive()))
      {
        if ((((L2PcInstance)target).getPvpFlag() == 0) && (((L2PcInstance)target).getKarma() == 0))
        {
          return false;
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
    if ((checkLandingState()) && (mountType == 2)) {
      return false;
    }
    switch (mountType)
    {
    case 0:
      setIsFlying(false);
      setIsRiding(false);
      isFalling(false, 0);
      break;
    case 1:
      setIsRiding(true);
      if (!isNoble())
        break;
      L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
      addSkill(striderAssaultSkill, false);
      break;
    case 2:
      setIsFlying(true);
    }

    _mountType = mountType;

    UserInfo ui = new UserInfo(this);
    sendPacket(ui);
    return true;
  }

  public int getMountType()
  {
    return _mountType;
  }

  public void updateAbnormalEffect()
  {
    broadcastUserInfo();
  }

  public void tempInvetoryDisable()
  {
    _inventoryDisable = true;

    ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500L);
  }

  public boolean isInvetoryDisabled()
  {
    return _inventoryDisable;
  }

  public Map<Integer, L2CubicInstance> getCubics()
  {
    return _cubics;
  }

  public void addCubic(int id, int level, double matk, int activationtime, int activationchance)
  {
    if (Config.DEBUG)
      _log.info(new StringBuilder().append("L2PcInstance(").append(getName()).append("): addCubic(").append(id).append("|").append(level).append("|").append(matk).append(")").toString());
    L2CubicInstance cubic = new L2CubicInstance(this, id, level, (int)matk, activationtime, activationchance);

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
    return new StringBuilder().append("player ").append(getName()).toString();
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

  public void setSilentMoving(boolean flag)
  {
    _isSilentMoving = flag;
  }

  public boolean isSilentMoving()
  {
    return _isSilentMoving;
  }

  public boolean isFestivalParticipant()
  {
    return SevenSignsFestival.getInstance().isParticipant(this);
  }

  public void addAutoSoulShot(int itemId)
  {
    _activeSoulShots.put(Integer.valueOf(itemId), Integer.valueOf(itemId));
  }

  public void removeAutoSoulShot(int itemId)
  {
    _activeSoulShots.remove(Integer.valueOf(itemId));
  }

  public Map<Integer, Integer> getAutoSoulShot()
  {
    return _activeSoulShots;
  }

  public void rechargeAutoSoulShot(boolean physical, boolean magic, boolean summon)
  {
    if ((_activeSoulShots == null) || (_activeSoulShots.size() == 0)) {
      return;
    }
    for (Iterator i$ = _activeSoulShots.values().iterator(); i$.hasNext(); ) { int itemId = ((Integer)i$.next()).intValue();

      L2ItemInstance item = getInventory().getItemByItemId(itemId);

      if (item != null)
      {
        if (magic)
        {
          IItemHandler handler;
          if (!summon)
          {
            switch (itemId) { case 2509:
            case 2510:
            case 2511:
            case 2512:
            case 2513:
            case 2514:
            case 3947:
            case 3948:
            case 3949:
            case 3950:
            case 3951:
            case 3952:
            case 5790:
              handler = ItemHandler.getInstance().getItemHandler(itemId);

              if (handler == null) break;
              handler.useItem(this, item);
            }

          }
          else if ((itemId == 6646) || (itemId == 6647))
          {
            handler = ItemHandler.getInstance().getItemHandler(itemId);

            if (handler != null) {
              handler.useItem(this, item);
            }
          }
        }

        if (physical)
        {
          IItemHandler handler;
          if (!summon)
          {
            switch (itemId) { case 1463:
            case 1464:
            case 1465:
            case 1466:
            case 1467:
            case 1835:
            case 5789:
              handler = ItemHandler.getInstance().getItemHandler(itemId);

              if (handler == null) break;
              handler.useItem(this, item);
            }

          }
          else if (itemId == 6645)
          {
            handler = ItemHandler.getInstance().getItemHandler(itemId);

            if (handler != null) {
              handler.useItem(this, item);
            }
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

  public void setClanPrivileges(int n)
  {
    _clanPrivileges = n;
  }

  public boolean getAllowTrade() {
    return _allowTrade;
  }

  public void setAllowTrade(boolean a)
  {
    _allowTrade = a;
  }

  public void setPledgeClass(int classId)
  {
    _pledgeClass = classId;
  }

  public int getPledgeClass()
  {
    return _pledgeClass;
  }

  public void setPledgeType(int typeId)
  {
    _pledgeType = typeId;
  }

  public int getPledgeType()
  {
    return _pledgeType;
  }

  public int getApprentice()
  {
    return _apprentice;
  }

  public void setApprentice(int apprentice_id)
  {
    _apprentice = apprentice_id;
  }

  public int getSponsor()
  {
    return _sponsor;
  }

  public void setSponsor(int sponsor_id)
  {
    _sponsor = sponsor_id;
  }

  public void sendMessage(String message)
  {
    sendPacket(SystemMessage.sendString(message));
  }

  public void enterObserverMode(int x, int y, int z)
  {
    if (isInOlympiadMode())
      return;
    if ((Olympiad.getInstance().isRegisteredInComp(this)) || (getOlympiadGameId() > 0))
    {
      sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));

      return;
    }
    if (TvTEvent.isPlayerParticipant(getObjectId()))
    {
      sendMessage("You can't join observation mode while participating on TvT Event.");
      return;
    }

    _obsX = getX();
    _obsY = getY();
    _obsZ = getZ();

    abortCast();
    setTarget(null);
    stopMove(null);
    setIsParalyzed(true);
    setIsInvul(true);
    getAppearance().setInvisible();
    sendPacket(new ObservationMode(x, y, z));
    setXYZ(x, y, z);

    _observerMode = true;
    broadcastUserInfo();
  }

  public void enterOlympiadObserverMode(int x, int y, int z, int id)
  {
    if (getPet() != null) {
      getPet().unSummon(this);
    }
    if (getCubics().size() > 0)
    {
      for (L2CubicInstance cubic : getCubics().values())
      {
        cubic.stopAction();
        cubic.cancelDisappear();
      }

      getCubics().clear();
    }

    _olympiadGameId = id;
    _obsX = getX();
    if (isSitting())
      standUp();
    _obsY = getY();
    _obsZ = getZ();
    abortCast();
    setTarget(null);
    setIsInvul(true);
    getAppearance().setInvisible();
    teleToLocation(x, y, z, true);
    sendPacket(new ExOlympiadMode(3));
    _observerMode = true;
    broadcastUserInfo();
  }

  public void leaveObserverMode()
  {
    setTarget(null);
    setXYZ(_obsX, _obsY, _obsZ);
    setIsParalyzed(false);
    getAppearance().setVisible();
    setIsInvul(false);

    if (getAI() != null) {
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }
    _observerMode = false;
    sendPacket(new ObservationReturn(this));
    broadcastUserInfo();
  }

  public void leaveOlympiadObserverMode()
  {
    setTarget(null);
    sendPacket(new ExOlympiadMode(0));
    teleToLocation(_obsX, _obsY, _obsZ, true);
    getAppearance().setVisible();
    setIsInvul(false);
    if (getAI() != null)
    {
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }
    Olympiad.getInstance(); Olympiad.removeSpectator(_olympiadGameId, this);
    _olympiadGameId = -1;
    _observerMode = false;
    broadcastUserInfo();
  }

  public void setOlympiadSide(int i)
  {
    _olympiadSide = i;
  }

  public int getOlympiadSide()
  {
    return _olympiadSide;
  }

  public void setOlympiadGameId(int id)
  {
    _olympiadGameId = id;
  }

  public int getOlympiadGameId()
  {
    return _olympiadGameId;
  }

  public int getObsX()
  {
    return _obsX;
  }

  public int getObsY()
  {
    return _obsY;
  }

  public int getObsZ()
  {
    return _obsZ;
  }

  public boolean inObserverMode()
  {
    return _observerMode;
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

  public void setChatBanned(boolean state, int delayInMinutes)
  {
    _chatBanned = state;
    _chatBanTimer = 0L;
    stopChatBanTask(false);
    if ((_chatBanned) && (delayInMinutes > 0))
    {
      _chatBanTimer = (delayInMinutes * 60000L);
      _chatBanTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChatBanTask(this), _chatBanTimer);
      sendMessage(new StringBuilder().append("Your chat banned for").append(delayInMinutes).append("minutes.").toString());
      sendPacket(new EtcStatusUpdate(this));
    }
    storeCharBase();
  }

  public long getChatBanTimer()
  {
    if (_chatBanned)
    {
      long delay = _chatBanTask.getDelay(TimeUnit.MILLISECONDS);
      if (delay >= 0L)
        _chatBanTimer = delay;
    }
    return _chatBanTimer;
  }

  public boolean isChatBanned()
  {
    return _chatBanned;
  }

  public void setChatBanned(boolean state)
  {
    _chatBanned = state;
  }

  public void setChatUnbanTask(ScheduledFuture task)
  {
    _chatUnbanTask = task;
  }

  public ScheduledFuture getChatUnbanTask() {
    return _chatUnbanTask;
  }

  public void setChatBanTimer(long time)
  {
    _chatBanTimer = time;
  }

  private void updateChatBanState()
  {
    if (_chatBanTimer > 0L)
    {
      _chatBanned = true;
      _chatBanTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChatBanTask(this), _chatBanTimer);
      sendMessage(new StringBuilder().append("Your chat will be placed in a ban for").append(Math.round((float)(_chatBanTimer / 60000L))).append(" minutes.").toString());
      sendPacket(new EtcStatusUpdate(this));
    }
  }

  public void stopChatBanTask(boolean save)
  {
    if (_chatBanTask != null)
    {
      if (save)
      {
        long delay = _chatBanTask.getDelay(TimeUnit.MILLISECONDS);
        if (delay < 0L)
          delay = 0L;
        setChatBanTimer(delay);
      }
      _chatBanTask.cancel(false);
      _chatBanned = false;
      _chatBanTask = null;
      sendPacket(new EtcStatusUpdate(this));
    }
  }

  public boolean getMessageRefusal()
  {
    return _messageRefusal;
  }

  public void setMessageRefusal(boolean mode)
  {
    _messageRefusal = mode;
    sendPacket(new EtcStatusUpdate(this));
  }

  public void setDietMode(boolean mode)
  {
    _dietMode = mode;
  }

  public boolean getDietMode()
  {
    return _dietMode;
  }

  public void setTradeRefusal(boolean mode)
  {
    _tradeRefusal = mode;
  }

  public boolean getTradeRefusal()
  {
    return _tradeRefusal;
  }

  public void setExchangeRefusal(boolean mode)
  {
    _exchangeRefusal = mode;
  }

  public boolean getExchangeRefusal()
  {
    return _exchangeRefusal;
  }

  public BlockList getBlockList()
  {
    return _blockList;
  }

  public void setHero(boolean hero)
  {
    if ((hero) && (_baseClass == _activeClass))
    {
      for (L2Skill s : HeroSkillTable.GetHeroSkills()) {
        addSkill(s, false);
      }
    }
    else {
      for (L2Skill s : HeroSkillTable.GetHeroSkills())
        super.removeSkill(s);
    }
    _hero = hero;

    sendSkillList();
  }

  public void setIsInOlympiadMode(boolean b)
  {
    _inOlympiadMode = b;
  }

  public void setIsOlympiadStart(boolean b)
  {
    _OlympiadStart = b;
  }

  public boolean isOlympiadStart() {
    return _OlympiadStart;
  }

  public boolean isHero()
  {
    return _hero;
  }

  public boolean isInOlympiadMode()
  {
    return _inOlympiadMode;
  }

  public boolean isInDuel()
  {
    return _isInDuel;
  }

  public int getDuelId()
  {
    return _duelId;
  }

  public void setDuelState(int mode)
  {
    _duelState = mode;
  }

  public int getDuelState()
  {
    return _duelState;
  }

  public void setIsInDuel(int duelId)
  {
    if (duelId > 0)
    {
      _isInDuel = true;
      _duelState = 1;
      _duelId = duelId;
    }
    else
    {
      if (_duelState == 2) { enableAllSkills(); getStatus().startHpMpRegeneration(); }
      _isInDuel = false;
      _duelState = 0;
      _duelId = 0;
    }
  }

  public SystemMessage getNoDuelReason()
  {
    SystemMessage sm = new SystemMessage(_noDuelReason);
    sm.addString(getName());
    _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
    return sm;
  }

  public boolean canDuel()
  {
    if ((isInCombat()) || (isInJail())) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE; return false; }
    if ((isDead()) || (isAlikeDead()) || (getCurrentHp() < getMaxHp() / 2) || (getCurrentMp() < getMaxMp() / 2)) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1S_HP_OR_MP_IS_BELOW_50_PERCENT; return false; }
    if (isInDuel()) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL; return false; }
    if (isInOlympiadMode()) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD; return false; }
    if (isCursedWeaponEquiped()) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE; return false; }
    if (getPrivateStoreType() != 0) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE; return false; }
    if ((isMounted()) || (isInBoat())) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER; return false; }
    if (isFishing()) { _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING; return false; }
    if ((isInsideZone(1)) || (isInsideZone(2)) || (isInsideZone(4)))
    {
      _noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
      return false;
    }
    return true;
  }

  public boolean isNoble()
  {
    return _noble;
  }

  public void setNoble(boolean val)
  {
    if (val)
      for (L2Skill s : NobleSkillTable.getInstance().GetNobleSkills())
        addSkill(s, false);
    else
      for (L2Skill s : NobleSkillTable.getInstance().GetNobleSkills())
        super.removeSkill(s);
    _noble = val;

    sendSkillList();
  }

  public void setLvlJoinedAcademy(int lvl)
  {
    _lvlJoinedAcademy = lvl;
  }

  public int getLvlJoinedAcademy()
  {
    return _lvlJoinedAcademy;
  }

  public boolean isAcademyMember()
  {
    return _lvlJoinedAcademy > 0;
  }

  public void setTeam(int team)
  {
    _team = team;
  }

  public int getTeam()
  {
    return _team;
  }

  public void setWantsPeace(int wantsPeace)
  {
    _wantsPeace = wantsPeace;
  }

  public int getWantsPeace()
  {
    return _wantsPeace;
  }

  public boolean isFishing()
  {
    return _fishing;
  }

  public void setFishing(boolean fishing)
  {
    _fishing = fishing;
  }

  public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
  {
    _alliedVarkaKetra = sideAndLvlOfAlliance;
  }

  public int getAllianceWithVarkaKetra()
  {
    return _alliedVarkaKetra;
  }

  public boolean isAlliedWithVarka()
  {
    return _alliedVarkaKetra < 0;
  }

  public boolean isAlliedWithKetra()
  {
    return _alliedVarkaKetra > 0;
  }

  public void sendSkillList() {
    sendSkillList(this);
  }

  public void sendSkillList(L2PcInstance player)
  {
    SkillList sl = new SkillList();
    if (player != null)
    {
      for (L2Skill s : player.getAllSkills())
      {
        if (s == null)
          continue;
        if (s.getId() > 9000)
          continue;
        if (s.isChance())
          sl.addSkill(s.getId(), s.getLevel(), true);
        else {
          sl.addSkill(s.getId(), s.getLevel(), s.isPassive());
        }
      }
    }
    sendPacket(sl);
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

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)");
      statement.setInt(1, getObjectId());
      statement.setInt(2, newClass.getClassId());
      statement.setLong(3, newClass.getExp());
      statement.setInt(4, newClass.getSp());
      statement.setInt(5, newClass.getLevel());
      statement.setInt(6, newClass.getClassIndex());
      statement.execute();
      statement.close();
    }
    catch (Exception e) {
      _log.warning(new StringBuilder().append("WARNING: Could not add character sub class for ").append(getName()).append(": ").append(e).toString());
      int i = 0;
      return i; } finally { try { con.close(); } catch (Exception e) {
      } }
    getSubClasses().put(Integer.valueOf(newClass.getClassIndex()), newClass);

    if (Config.DEBUG) {
      _log.info(new StringBuilder().append(getName()).append(" added class ID ").append(classId).append(" as a sub class at index ").append(classIndex).append(".").toString());
    }
    ClassId subTemplate = ClassId.values()[classId];
    Collection skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);

    if (skillTree == null) {
      return true;
    }
    Map prevSkillList = new FastMap();

    for (L2SkillLearn skillInfo : skillTree)
    {
      if (skillInfo.getMinLevel() <= 40)
      {
        L2Skill prevSkill = (L2Skill)prevSkillList.get(Integer.valueOf(skillInfo.getId()));
        L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());

        if ((prevSkill != null) && (prevSkill.getLevel() > newSkill.getLevel())) {
          continue;
        }
        prevSkillList.put(Integer.valueOf(newSkill.getId()), newSkill);
        storeSkill(newSkill, prevSkill, classIndex);
      }
    }

    if (Config.DEBUG) {
      _log.info(new StringBuilder().append(getName()).append(" was given ").append(getAllSkills().length).append(" skills for their new sub class.").toString());
    }
    return true;
  }

  public boolean modifySubClass(int classIndex, int newClassId)
  {
    int oldClassId = ((SubClass)getSubClasses().get(Integer.valueOf(classIndex))).getClassId();

    if (Config.DEBUG) {
      _log.info(new StringBuilder().append(getName()).append(" has requested to modify sub class index ").append(classIndex).append(" from class ID ").append(oldClassId).append(" to ").append(newClassId).append(".").toString());
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, classIndex);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, classIndex);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, classIndex);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, classIndex);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?");
      statement.setInt(1, getObjectId());
      statement.setInt(2, classIndex);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Could not modify sub class for ").append(getName()).append(" to class index ").append(classIndex).append(": ").append(e).toString());

      getSubClasses().remove(Integer.valueOf(classIndex));
      int i = 0;
      return i; } finally { try { con.close();
      } catch (Exception e) {
      } }
    getSubClasses().remove(Integer.valueOf(classIndex));
    return addSubClass(newClassId, classIndex);
  }

  public boolean isSubClassActive()
  {
    return _classIndex > 0;
  }

  public Map<Integer, SubClass> getSubClasses()
  {
    if (_subClasses == null) {
      _subClasses = new FastMap();
    }
    return _subClasses;
  }

  public int getTotalSubClasses()
  {
    return getSubClasses().size();
  }

  public int getBaseClass()
  {
    return _baseClass;
  }

  public int getActiveClass()
  {
    return _activeClass;
  }

  public int getClassIndex()
  {
    return _classIndex;
  }

  private void setClassTemplate(int classId)
  {
    _activeClass = classId;

    L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId);

    if (t == null)
    {
      _log.severe(new StringBuilder().append("Missing template for classId: ").append(classId).toString());
      throw new Error();
    }

    setTemplate(t);
  }

  public boolean setActiveClass(int classIndex)
  {
    for (L2ItemInstance temp : getInventory().getAugmentedItems()) {
      if ((temp == null) || (!temp.isEquipped())) continue; temp.getAugmentation().removeBoni(this);
    }
    if (_forceBuff != null) {
      abortCast();
    }
    for (L2Character character : getKnownList().getKnownCharacters())
    {
      if ((character.getForceBuff() != null) && (character.getForceBuff().getTarget() == this)) {
        character.abortCast();
      }
    }
    store();

    if (classIndex == 0)
    {
      setClassTemplate(getBaseClass());
    }
    else {
      try
      {
        setClassTemplate(((SubClass)getSubClasses().get(Integer.valueOf(classIndex))).getClassId());
      }
      catch (Exception e) {
        _log.info(new StringBuilder().append("Could not switch ").append(getName()).append("'s sub class to class index ").append(classIndex).append(": ").append(e).toString());
        return false;
      }
    }
    _classIndex = classIndex;

    if (isInParty()) {
      getParty().recalculatePartyLevel();
    }
    if ((getPet() instanceof L2SummonInstance)) {
      getPet().unSummon(this);
    }
    if (getCubics().size() > 0)
    {
      for (L2CubicInstance cubic : getCubics().values())
      {
        cubic.stopAction();
        cubic.cancelDisappear();
      }

      getCubics().clear();
    }

    for (L2Skill oldSkill : getAllSkills()) {
      super.removeSkill(oldSkill);
    }
    if (isCursedWeaponEquiped()) {
      CursedWeaponsManager.getInstance().givePassive(_cursedWeaponEquipedId);
    }
    stopAllEffects();

    if (isSubClassActive())
    {
      _dwarvenRecipeBook.clear();
      _commonRecipeBook.clear();
    }
    else
    {
      restoreRecipeBook();
    }

    restoreDeathPenaltyBuffLevel();

    restoreSkills();
    regiveTemporarySkills();
    rewardSkills();
    restoreEffects();
    sendPacket(new EtcStatusUpdate(this));

    QuestState st = getQuestState("422_RepentYourSins");

    if (st != null)
    {
      st.exitQuest(true);
    }

    for (int i = 0; i < 3; i++) {
      _henna[i] = null;
    }
    restoreHenna();
    sendPacket(new HennaInfo(this));

    if (getCurrentHp() > getMaxHp())
      setCurrentHp(getMaxHp());
    if (getCurrentMp() > getMaxMp())
      setCurrentMp(getMaxMp());
    if (getCurrentCp() > getMaxCp())
      setCurrentCp(getMaxCp());
    broadcastUserInfo();
    refreshOverloaded();
    refreshExpertisePenalty();

    setExpBeforeDeath(0L);

    _shortCuts.restore();
    sendPacket(new ShortCutInit(this));

    broadcastPacket(new SocialAction(getObjectId(), 15));

    return true;
  }

  public void stopWarnUserTakeBreak()
  {
    if (_taskWarnUserTakeBreak != null)
    {
      _taskWarnUserTakeBreak.cancel(true);
      _taskWarnUserTakeBreak = null;
    }
  }

  public void startWarnUserTakeBreak()
  {
    if (_taskWarnUserTakeBreak == null)
      _taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000L, 7200000L);
  }

  public void stopRentPet()
  {
    if (_taskRentPet != null)
    {
      if ((checkLandingState()) && (getMountType() == 2)) {
        teleToLocation(MapRegionTable.TeleportWhereType.Town);
      }
      if (setMountType(0))
      {
        _taskRentPet.cancel(true);
        Ride dismount = new Ride(getObjectId(), 0, 0);
        sendPacket(dismount);
        broadcastPacket(dismount);
        _taskRentPet = null;
      }
    }
  }

  public void startUnmountTask()
  {
    if ((!isDead()) && (_taskUnmount == null))
    {
      int timeonmount = 2400000;
      sendPacket(new SetupGauge(3, timeonmount));
      _taskUnmount = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new UnmountTask(), timeonmount, 1000L);
    }
  }

  public void stopUnmountTask()
  {
    if (_taskUnmount != null)
    {
      _taskUnmount.cancel(false);
      _taskUnmount = null;
      sendPacket(new SetupGauge(3, 0));
    }
  }

  public void sendPacket(SystemMessageId hacking_tool)
  {
    sendMessage("Please try again after closing unnecessary programs!.");
  }

  public void startRentPet(int seconds)
  {
    if (_taskRentPet == null)
      _taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000L);
  }

  public boolean isRentedPet()
  {
    return _taskRentPet != null;
  }

  public void stopWaterTask()
  {
    if (_taskWater != null)
    {
      _taskWater.cancel(false);

      _taskWater = null;
      sendPacket(new SetupGauge(2, 0));
      isFalling(false, 0);
    }
  }

  public void startWaterTask()
  {
    if ((!isDead()) && (_taskWater == null))
    {
      int timeinwater = 86000;

      sendPacket(new SetupGauge(2, timeinwater));
      _taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000L);
    }
  }

  public boolean isInWater()
  {
    return _taskWater != null;
  }

  public void checkWaterState()
  {
    if (isInsideZone(128))
      startWaterTask();
    else
      stopWaterTask();
  }

  public void checkOlyState()
  {
    if (isInsideZone(8192))
    {
      if ((!isInOlympiadMode()) && (Olympiad.getInstance().getSpectators(getOlympiadGameId()) == null) && (!isGM())) teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
  }

  public void onPlayerEnter()
  {
    startWarnUserTakeBreak();

    if ((SevenSigns.getInstance().isSealValidationPeriod()) || (SevenSigns.getInstance().isCompResultsPeriod()))
    {
      if ((!isGM()) && (isIn7sDungeon()) && (SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore()))
      {
        teleToLocation(MapRegionTable.TeleportWhereType.Town);
        setIsIn7sDungeon(false);
        sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
      }

    }
    else if ((!isGM()) && (isIn7sDungeon()) && (SevenSigns.getInstance().getPlayerCabal(this) == 0))
    {
      teleToLocation(MapRegionTable.TeleportWhereType.Town);
      setIsIn7sDungeon(false);
      sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
    }

    updateJailState();
    updateChatBanState();
    if (_isInvul)
      sendMessage("Entering world in Invulnerable mode.");
    if (getAppearance().getInvisible())
      sendMessage("Entering world in Invisible mode.");
    if (getMessageRefusal()) {
      sendMessage("Entering world in Message Refusal mode.");
    }
    revalidateZone(true);

    if ((!isGM()) && (GrandBossManager.getInstance().getZone(this) != null))
    {
      L2BossZone bz = GrandBossManager.getInstance().getZone(this);

      if (System.currentTimeMillis() - getLastAccess() >= bz.getTimeInvade())
      {
        if ((bz.getQuestName() != null) && (getQuestState(bz.getQuestName()) != null))
        {
          getQuestState(bz.getQuestName()).exitQuest(true);
        }

        if (bz.getZoneName().equalsIgnoreCase("FourSepulcher"))
        {
          int driftX = Rnd.get(-80, 80);
          int driftY = Rnd.get(-80, 80);
          teleToLocation(178293 + driftX, -84607 + driftY, -7216);
        }
        else
        {
          teleToLocation(MapRegionTable.TeleportWhereType.Town);
        }
      }
    }
  }

  public long getLastAccess()
  {
    return _lastAccess;
  }

  private void checkRecom(int recsHave, int recsLeft)
  {
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

  public void restartRecom()
  {
    if (Config.ALT_RECOMMEND)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("DELETE FROM character_recommends WHERE char_id=?");
        statement.setInt(1, getObjectId());
        statement.execute();
        statement.close();

        _recomChars.clear();
      }
      catch (Exception e)
      {
        _log.warning(new StringBuilder().append("could not clear char recommendations: ").append(e).toString());
      }
      finally {
        try {
          con.close(); } catch (Exception e) {
        }
      }
    }
    if (getStat().getLevel() < 20)
    {
      _recomLeft = 3;
      _recomHave -= 1;
    }
    else if (getStat().getLevel() < 40)
    {
      _recomLeft = 6;
      _recomHave -= 2;
    }
    else
    {
      _recomLeft = 9;
      _recomHave -= 3;
    }
    if (_recomHave < 0) _recomHave = 0;

    Calendar update = Calendar.getInstance();
    if (update.get(11) < 13) update.add(5, -1);
    update.set(11, 13);
    _lastRecomUpdate = update.getTimeInMillis();
  }

  public void doRevive(boolean broadcastPacketRevive)
  {
    super.doRevive(true);
    updateEffectIcons();
    refreshExpertisePenalty();
    sendPacket(new EtcStatusUpdate(this));
    _reviveRequested = 0;
    _revivePower = 0.0D;

    if ((isInParty()) && (getParty().isInDimensionalRift()))
    {
      if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ())) {
        getParty().getDimensionalRift().memberRessurected(this);
      }
    }
    if ((_inEventCTF) && (CTF._started) && (Config.CTF_REVIVE_RECOVERY))
    {
      getStatus().setCurrentHp(getMaxHp());
      getStatus().setCurrentMp(getMaxMp());
      getStatus().setCurrentCp(getMaxCp());
    }
  }

  public void doRevive(double revivePower)
  {
    restoreExp(revivePower);
    doRevive(true);
  }

  public void doRevive()
  {
    doRevive(true);
  }

  public void reviveRequest(L2PcInstance Reviver, L2Skill skill, boolean Pet)
  {
    if (_reviveRequested == 1)
    {
      if (_revivePet == Pet)
      {
        Reviver.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED));
      }
      else if (Pet)
        Reviver.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_RES));
      else {
        Reviver.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES));
      }
      return;
    }
    if (((Pet) && (getPet() != null) && (getPet().isDead())) || ((!Pet) && (isDead())))
    {
      _reviveRequested = 1;
      int restoreExp = 0;
      if (isPhoenixBlessed())
        _revivePower = 0.0D;
      else {
        _revivePower = Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), Reviver.getStat().getWIT());
      }
      restoreExp = (int)Math.round((getExpBeforeDeath() - getExp()) * _revivePower / 100.0D);
      _revivePet = Pet;

      ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST.getId());

      sendPacket(dlg.addPcName(Reviver).addString(new StringBuilder().append("").append(restoreExp).toString()));
    }
  }

  public void reviveAnswer(int answer)
  {
    if ((_reviveRequested != 1) || ((!isDead()) && (!_revivePet)) || ((_revivePet) && (getPet() != null) && (!getPet().isDead()))) {
      return;
    }
    if ((answer == 0) && (isPhoenixBlessed()))
    {
      stopPhoenixBlessing(null);
      stopAllEffects();
    }

    if (answer == 1)
    {
      if (!_revivePet)
      {
        if (_revivePower != 0.0D)
          doRevive(_revivePower);
        else
          doRevive(true);
      }
      else if (getPet() != null)
      {
        if (_revivePower != 0.0D)
          getPet().doRevive(_revivePower);
        else
          getPet().doRevive(true);
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

  public void removeReviving()
  {
    _reviveRequested = 0;
    _revivePower = 0.0D;
  }

  public void onActionRequest()
  {
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

    if ((Config.PLAYER_SPAWN_PROTECTION > 0) && (!isInOlympiadMode())) {
      setProtection(true);
    }

    if (getTrainedBeast() != null)
    {
      getTrainedBeast().getAI().stopFollow();
      getTrainedBeast().teleToLocation(getPosition().getX() + Rnd.get(-100, 100), getPosition().getY() + Rnd.get(-100, 100), getPosition().getZ(), false);
      getTrainedBeast().getAI().startFollow(this);
    }
  }

  public void setLastClientPosition(int x, int y, int z)
  {
    _lastClientPosition.setXYZ(x, y, z);
  }

  public boolean checkLastClientPosition(int x, int y, int z)
  {
    return _lastClientPosition.equals(x, y, z);
  }

  public int getLastClientDistance(int x, int y, int z)
  {
    double dx = x - _lastClientPosition.getX();
    double dy = y - _lastClientPosition.getY();
    double dz = z - _lastClientPosition.getZ();

    return (int)Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public void setLastPartyPosition(int x, int y, int z)
  {
    _lastPartyPosition.setXYZ(x, y, z);
  }

  public int getLastPartyPositionDistance(int x, int y, int z)
  {
    double dx = x - _lastPartyPosition.getX();
    double dy = y - _lastPartyPosition.getY();
    double dz = z - _lastPartyPosition.getZ();

    return (int)Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public void setLastServerPosition(int x, int y, int z)
  {
    _lastServerPosition.setXYZ(x, y, z);
  }

  public boolean checkLastServerPosition(int x, int y, int z)
  {
    return _lastServerPosition.equals(x, y, z);
  }

  public int getLastServerDistance(int x, int y, int z)
  {
    double dx = x - _lastServerPosition.getX();
    double dy = y - _lastServerPosition.getY();
    double dz = z - _lastServerPosition.getZ();

    return (int)Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public void addExpAndSp(long addToExp, int addToSp)
  {
    if (_expGainOn)
    {
      getStat().addExpAndSp(addToExp, addToSp);
    }
    else
    {
      getStat().addExpAndSp(0L, addToSp);
    }
  }

  public void removeExpAndSp(long removeExp, int removeSp) {
    getStat().removeExpAndSp(removeExp, removeSp);
  }

  public void reduceCurrentHp(double i, L2Character attacker) {
    getStatus().reduceHp(i, attacker);

    if (getTrainedBeast() != null)
      getTrainedBeast().onOwnerGotAttacked(attacker);
  }

  public void reduceCurrentHp(double value, L2Character attacker, boolean awake)
  {
    getStatus().reduceHp(value, attacker, awake);

    if (getTrainedBeast() != null)
      getTrainedBeast().onOwnerGotAttacked(attacker);
  }

  public void broadcastSnoop(int type, String name, String _text)
  {
    Snoop sn;
    if (_snoopListener.size() > 0)
    {
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

  public void addSnooped(L2PcInstance pci)
  {
    if (!_snoopedPlayer.contains(pci))
      _snoopedPlayer.add(pci);
  }

  public void removeSnooped(L2PcInstance pci)
  {
    _snoopedPlayer.remove(pci);
  }

  public synchronized void addBypass(String bypass)
  {
    if (bypass == null) return;
    _validBypass.add(bypass);
  }

  public synchronized void addBypass2(String bypass)
  {
    if (bypass == null) return;
    _validBypass2.add(bypass);
  }

  public synchronized boolean validateBypass(String cmd)
  {
    if (!Config.BYPASS_VALIDATION) {
      return true;
    }
    for (String bp : _validBypass)
    {
      if (bp == null) {
        continue;
      }
      if (bp.equals(cmd)) {
        return true;
      }
    }
    for (String bp : _validBypass2)
    {
      if (bp == null) {
        continue;
      }
      if (cmd.startsWith(bp)) {
        return true;
      }
    }
    _log.warning(new StringBuilder().append("[L2PcInstance] player [").append(getName()).append("] sent invalid bypass '").append(cmd).append("', ban this player!").toString());
    return false;
  }

  public boolean validateItemManipulation(int objectId, String action)
  {
    L2ItemInstance item = getInventory().getItemByObjectId(objectId);

    if ((item == null) || (item.getOwnerId() != getObjectId()))
    {
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

  public void setInBoat(boolean inBoat)
  {
    _inBoat = inBoat;
  }

  public L2BoatInstance getBoat()
  {
    return _boat;
  }

  public void setBoat(L2BoatInstance boat)
  {
    _boat = boat;
  }

  public void setInCrystallize(boolean inCrystallize)
  {
    _inCrystallize = inCrystallize;
  }

  public boolean isInCrystallize()
  {
    return _inCrystallize;
  }

  public Point3D getInBoatPosition()
  {
    return _inBoatPosition;
  }

  public void setInBoatPosition(Point3D pt)
  {
    _inBoatPosition = pt;
  }

  public void deleteMe()
  {
    if (inObserverMode()) {
      setXYZ(_obsX, _obsY, _obsZ);
    }
    try
    {
      setOnlineStatus(false); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t); }
    try
    {
      stopAllTimers(); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t); }
    try
    {
      RecipeController.getInstance().requestMakeItemAbort(this); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t); }
    try
    {
      setTarget(null); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t);
    }
    try
    {
      if (_forceBuff != null)
      {
        _forceBuff.delete();
      }
      for (L2Character character : getKnownList().getKnownCharacters())
        if ((character.getForceBuff() != null) && (character.getForceBuff().getTarget() == this))
          character.abortCast();
    } catch (Throwable t) {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }
    try
    {
      L2Effect[] effects = getAllEffects();
      for (L2Effect effect : effects)
      {
        switch (effect.getEffectType())
        {
        case SIGNET_GROUND:
        case SIGNET_EFFECT:
          effect.exit();
        }
      }

    }
    catch (Throwable t)
    {
      _log.log(Level.SEVERE, "deleteMe()", t);
    }

    try
    {
      TvTEvent.onLogout(this);
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "deleteMe()", e);
    }

    L2WorldRegion oldRegion = getWorldRegion();

    if (isVisible()) try {
        decayMe(); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t);
      }
    if (oldRegion != null) oldRegion.removeFromZones(this);

    if (isInParty()) try { leaveParty(); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t);
      }
    if (getOlympiadGameId() != -1) {
      Olympiad.getInstance().removeDisconnectedCompetitor(this);
    }

    if (getPet() != null) {
      try {
        getPet().unSummon(this); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t);
      }
    }
    if ((getClanId() != 0) && (getClan() != null))
    {
      try
      {
        L2ClanMember clanMember = getClan().getClanMember(getName());
        if (clanMember != null) clanMember.setPlayerInstance(null); 
      } catch (Throwable t) {
        _log.log(Level.SEVERE, "deleteMe()", t);
      }
    }
    if (getActiveRequester() != null)
    {
      setActiveRequester(null);
    }

    if (isGM())
      try {
        GmListTable.getInstance().deleteGm(this); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t);
      }
    try
    {
      getInventory().deleteMe(); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t); }
    try
    {
      clearWarehouse(); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t); }
    if (Config.WAREHOUSE_CACHE)
      WarehouseCacheManager.getInstance().remCacheTask(this);
    try
    {
      getFreight().deleteMe(); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t); }
    try
    {
      getKnownList().removeAllKnownObjects(); } catch (Throwable t) { _log.log(Level.SEVERE, "deleteMe()", t);
    }

    closeNetConnection(true);

    FloodProtector.getInstance().removePlayer(getObjectId());

    if (getClanId() > 0) {
      getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
    }

    for (L2PcInstance player : _snoopedPlayer) {
      player.removeSnooper(this);
    }
    for (L2PcInstance player : _snoopListener) {
      player.removeSnooped(this);
    }
    if (_chanceSkills != null)
    {
      _chanceSkills.setOwner(null);
      _chanceSkills = null;
    }

    L2World.getInstance().removeObject(this);
    L2World.getInstance().removeFromAllPlayers(this);
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

    int lvl = GetRandomFishLvl();
    int group = GetRandomGroup();
    int type = GetRandomFishType(group);
    List fishs = FishTable.getInstance().getfish(lvl, type, group);
    if ((fishs == null) || (fishs.size() == 0))
    {
      sendMessage("Error - Fishes are not definied");
      EndFishing(false);
      return;
    }
    int check = Rnd.get(fishs.size());

    _fish = new FishData((FishData)fishs.get(check));
    fishs.clear();
    fishs = null;
    sendPacket(new SystemMessage(SystemMessageId.CAST_LINE_AND_START_FISHING));
    ExFishingStart efs = null;
    if ((!GameTimeController.getInstance().isNowNight()) && (_lure.isNightLure())) {
      _fish.setType(-1);
    }
    efs = new ExFishingStart(this, _fish.getType(), _x, _y, _z, _lure.isNightLure());
    broadcastPacket(efs);
    StartLookingForFishTask();
  }

  public void stopLookingForFishTask() {
    if (_taskforfish != null)
    {
      _taskforfish.cancel(false);
      _taskforfish = null;
    }
  }

  public void StartLookingForFishTask() {
    if ((!isDead()) && (_taskforfish == null))
    {
      int checkDelay = 0;
      boolean isNoob = false;
      boolean isUpperGrade = false;

      if (_lure != null)
      {
        int lureid = _lure.getItemId();
        isNoob = _fish.getGroup() == 0;
        isUpperGrade = _fish.getGroup() == 2;
        if ((lureid == 6519) || (lureid == 6522) || (lureid == 6525) || (lureid == 8505) || (lureid == 8508) || (lureid == 8511))
          checkDelay = Math.round((float)(_fish.getGutsCheckTime() * 1.33D));
        else if ((lureid == 6520) || (lureid == 6523) || (lureid == 6526) || ((lureid >= 8505) && (lureid <= 8513)) || ((lureid >= 7610) && (lureid <= 7613)) || ((lureid >= 7807) && (lureid <= 7809)) || ((lureid >= 8484) && (lureid <= 8486)))
          checkDelay = Math.round((float)(_fish.getGutsCheckTime() * 1.0D));
        else if ((lureid == 6521) || (lureid == 6524) || (lureid == 6527) || (lureid == 8507) || (lureid == 8510) || (lureid == 8513))
          checkDelay = Math.round((float)(_fish.getGutsCheckTime() * 0.66D));
      }
      _taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(_fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob, isUpperGrade), 10000L, checkDelay);
    }
  }

  private int GetRandomGroup()
  {
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

  private int GetRandomFishType(int group)
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
        else
          type = 6;
        break;
      case 7808:
        if (check <= 54)
          type = 4;
        else if (check <= 77)
          type = 6;
        else
          type = 5;
        break;
      case 7809:
        if (check <= 54)
          type = 6;
        else if (check <= 77)
          type = 5;
        else
          type = 4;
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
        else
          type = 3;
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
        else
          type = 3;
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
        else
          type = 3;
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
        else
          type = 9;
        break;
      case 8509:
        if (check <= 54)
          type = 7;
        else if (check <= 77)
          type = 9;
        else
          type = 8;
        break;
      case 8512:
        if (check <= 54)
          type = 9;
        else if (check <= 77)
          type = 8;
        else
          type = 7;
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

  private int GetRandomFishLvl() {
    L2Effect[] effects = getAllEffects();
    int skilllvl = getSkillLevel(1315);
    for (L2Effect e : effects) {
      if (e.getSkill().getId() == 2274)
        skilllvl = (int)e.getSkill().getPower(this);
    }
    if (skilllvl <= 0) return 1;

    int check = Rnd.get(100);
    int randomlvl;
    int randomlvl;
    if (check <= 50)
    {
      randomlvl = skilllvl;
    }
    else if (check <= 85)
    {
      int randomlvl = skilllvl - 1;
      if (randomlvl <= 0)
      {
        randomlvl = 1;
      }
    }
    else
    {
      randomlvl = skilllvl + 1;
      if (randomlvl > 27) randomlvl = 27;
    }

    return randomlvl;
  }

  public void StartFishCombat(boolean isNoob, boolean isUpperGrade) {
    _fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
  }

  public void EndFishing(boolean win) {
    ExFishingEnd efe = new ExFishingEnd(win, this);
    broadcastPacket(efe);
    _fishing = false;
    _fishx = 0;
    _fishy = 0;
    _fishz = 0;
    broadcastUserInfo();
    if (_fishCombat == null)
      sendPacket(new SystemMessage(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY));
    _fishCombat = null;
    _lure = null;

    sendPacket(new SystemMessage(SystemMessageId.REEL_LINE_AND_STOP_FISHING));
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

  public int GetInventoryLimit()
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
      {
        ivlim = Config.INVENTORY_MAXIMUM_DWARF;
      }
      else
      {
        ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
      }
    }
    ivlim += (int)getStat().calcStat(Stats.INV_LIM, 0.0D, null, null);

    return ivlim;
  }

  public int GetWareHouseLimit()
  {
    int whlim;
    int whlim;
    if (getRace() == Race.dwarf) {
      whlim = Config.WAREHOUSE_SLOTS_DWARF;
    }
    else {
      whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
    }
    whlim += (int)getStat().calcStat(Stats.WH_LIM, 0.0D, null, null);

    return whlim;
  }

  public int GetPrivateSellStoreLimit()
  {
    int pslim;
    int pslim;
    if (getRace() == Race.dwarf) {
      pslim = Config.MAX_PVTSTORE_SLOTS_DWARF;
    }
    else {
      pslim = Config.MAX_PVTSTORE_SLOTS_OTHER;
    }
    pslim += (int)getStat().calcStat(Stats.P_SELL_LIM, 0.0D, null, null);

    return pslim;
  }

  public int GetPrivateBuyStoreLimit()
  {
    int pblim;
    int pblim;
    if (getRace() == Race.dwarf) {
      pblim = Config.MAX_PVTSTORE_SLOTS_DWARF;
    }
    else
    {
      pblim = Config.MAX_PVTSTORE_SLOTS_OTHER;
    }
    pblim += (int)getStat().calcStat(Stats.P_BUY_LIM, 0.0D, null, null);

    return pblim;
  }

  public int GetFreightLimit() {
    return Config.FREIGHT_SLOTS + (int)getStat().calcStat(Stats.FREIGHT_LIM, 0.0D, null, null);
  }

  public int GetDwarfRecipeLimit()
  {
    int recdlim = Config.DWARF_RECIPE_LIMIT;
    recdlim += (int)getStat().calcStat(Stats.REC_D_LIM, 0.0D, null, null);
    return recdlim;
  }

  public int GetCommonRecipeLimit()
  {
    int recclim = Config.COMMON_RECIPE_LIMIT;
    recclim += (int)getStat().calcStat(Stats.REC_C_LIM, 0.0D, null, null);
    return recclim;
  }

  public void setMountObjectID(int newID)
  {
    _mountObjectID = newID;
  }

  public int getMountObjectID()
  {
    return _mountObjectID;
  }

  public SkillDat getCurrentSkill()
  {
    return _currentSkill;
  }

  public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
  {
    if (currentSkill == null)
    {
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

  public void setInJail(boolean state)
  {
    _inJail = state;
  }

  public void setInJail(boolean state, int delayInMinutes)
  {
    _inJail = state;
    _jailTimer = 0L;

    stopJailTask(false);

    if (_inJail)
    {
      if (delayInMinutes > 0)
      {
        _jailTimer = (delayInMinutes * 60000L);

        _jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
        sendMessage(new StringBuilder().append("You are in jail for ").append(delayInMinutes).append(" minutes.").toString());
      }

      NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
      String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
      if (jailInfos != null)
        htmlMsg.setHtml(jailInfos);
      else
        htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
      sendPacket(htmlMsg);

      teleToLocation(-114356, -249645, -2984, true);
    }
    else
    {
      NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
      String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
      if (jailInfos != null)
        htmlMsg.setHtml(jailInfos);
      else
        htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
      sendPacket(htmlMsg);

      teleToLocation(17836, 170178, -3507, true);
    }

    storeCharBase();
  }

  public long getJailTimer()
  {
    return _jailTimer;
  }

  public void setJailTimer(long time)
  {
    _jailTimer = time;
  }

  private void updateJailState()
  {
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

  public void stopJailTask(boolean save)
  {
    if (_jailTask != null)
    {
      if (save)
      {
        long delay = _jailTask.getDelay(TimeUnit.MILLISECONDS);
        if (delay < 0L)
          delay = 0L;
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

  public void setCursedWeaponEquipedId(int value)
  {
    _cursedWeaponEquipedId = value;
  }

  public int getCursedWeaponEquipedId()
  {
    return _cursedWeaponEquipedId;
  }

  public boolean getCharmOfCourage()
  {
    return _charmOfCourage;
  }

  public void setCharmOfCourage(boolean val)
  {
    _charmOfCourage = val;
    sendPacket(new EtcStatusUpdate(this));
  }

  public int getDeathPenaltyBuffLevel()
  {
    return _deathPenaltyBuffLevel;
  }

  public void setDeathPenaltyBuffLevel(int level)
  {
    _deathPenaltyBuffLevel = level;
  }

  public void calculateDeathPenaltyBuffLevel(L2Character killer)
  {
    if ((Rnd.get(100) <= Config.DEATH_PENALTY_CHANCE) && (!(killer instanceof L2PcInstance)) && (!isGM()) && ((!getCharmOfLuck()) || ((!(killer instanceof L2GrandBossInstance)) && (!(killer instanceof L2RaidBossInstance)))))
    {
      increaseDeathPenaltyBuffLevel();
    }
  }

  public void increaseDeathPenaltyBuffLevel() {
    if (getDeathPenaltyBuffLevel() >= 15) {
      return;
    }
    if (getDeathPenaltyBuffLevel() != 0)
    {
      L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

      if (skill != null) {
        removeSkill(skill, true);
      }
    }
    _deathPenaltyBuffLevel += 1;

    addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
    sendPacket(new EtcStatusUpdate(this));
    SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
    sm.addNumber(getDeathPenaltyBuffLevel());
    sendPacket(sm);
  }

  public void reduceDeathPenaltyBuffLevel()
  {
    if (getDeathPenaltyBuffLevel() <= 0) {
      return;
    }
    L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

    if (skill != null) {
      removeSkill(skill, true);
    }
    _deathPenaltyBuffLevel -= 1;

    if (getDeathPenaltyBuffLevel() > 0)
    {
      addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
      sendPacket(new EtcStatusUpdate(this));
      SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
      sm.addNumber(getDeathPenaltyBuffLevel());
      sendPacket(sm);
    }
    else
    {
      sendPacket(new EtcStatusUpdate(this));
      sendPacket(new SystemMessage(SystemMessageId.DEATH_PENALTY_LIFTED));
    }
  }

  public void restoreDeathPenaltyBuffLevel()
  {
    L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

    if (skill != null) {
      removeSkill(skill, true);
    }
    if (getDeathPenaltyBuffLevel() > 0)
    {
      addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
    }
  }

  public void addTimeStamp(int s, int r)
  {
    ReuseTimeStamps.put(Integer.valueOf(s), new TimeStamp(s, r));
  }

  private void addTimeStamp(TimeStamp T)
  {
    ReuseTimeStamps.put(Integer.valueOf(T.getSkill()), T);
  }

  public void removeTimeStamp(int s)
  {
    ReuseTimeStamps.remove(Integer.valueOf(s));
  }

  public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
  {
    if (miss)
    {
      sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
      return;
    }

    if (pcrit)
      sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT));
    if (mcrit) {
      sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_MAGIC));
    }
    if ((isInOlympiadMode()) && ((target instanceof L2PcInstance)) && (((L2PcInstance)target).isInOlympiadMode()) && (((L2PcInstance)target).getOlympiadGameId() == getOlympiadGameId()))
    {
      dmgDealt += damage;
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
    sm.addNumber(damage);
    sendPacket(sm);
  }

  public boolean isInDangerArea()
  {
    return isInDangerArea;
  }

  public void enterDangerArea()
  {
    isInDangerArea = true;
    sendPacket(new EtcStatusUpdate(this));
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    sm.addString("You have entered a danger area");
    sendPacket(sm);
  }

  public void exitDangerArea()
  {
    isInDangerArea = false;
    sendPacket(new EtcStatusUpdate(this));
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    sm.addString("You have left a danger area");
    sendPacket(sm);
  }

  public int getPcCafeScore()
  {
    return pcCafeScore;
  }

  public void reducePcCafeScore(int to)
  {
    pcCafeScore -= to;
    updatePcCafeWnd(to, false, false);
  }

  public void addPcCafeScore(int to)
  {
    pcCafeScore += to;
  }

  public void updatePcCafeWnd(int score, boolean add, boolean duble)
  {
    ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, score, add, 24, duble);
    sendPacket(wnd);
  }

  public void showPcCafeWnd()
  {
    ExPCCafePointInfo wnd = new ExPCCafePointInfo(this, 0, false, 24, false);
    sendPacket(wnd);
  }

  public ForceBuff getForceBuff()
  {
    return _forceBuff;
  }

  public void setForceBuff(ForceBuff fb)
  {
    _forceBuff = fb;
  }

  private String addZerosToColor(String color)
  {
    switch (color.length())
    {
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

  public boolean teleportRequest(L2PcInstance requester, L2Skill skill)
  {
    if ((_summonRequest.getTarget() != null) && (requester != null))
      return false;
    _summonRequest.setTarget(requester, skill);
    return true;
  }

  public void teleportAnswer(int answer, int requesterId)
  {
    if (_summonRequest.getTarget() == null)
      return;
    if ((answer == 1) && (_summonRequest.getTarget().getCharId() == requesterId))
    {
      teleToTarget(this, _summonRequest.getTarget(), _summonRequest.getSkill());
    }
    _summonRequest.setTarget(null, null);
  }

  public void teleToTarget(L2PcInstance targetChar, L2PcInstance summonerChar, L2Skill summonSkill)
  {
    if ((targetChar == null) || (summonerChar == null) || (summonSkill == null)) {
      return;
    }
    if (!checkSummonerStatus(summonerChar))
      return;
    if (!checkTargetStatus(targetChar, summonerChar)) {
      return;
    }
    int itemConsumeId = summonSkill.getTargetConsumeId();
    int itemConsumeCount = summonSkill.getTargetConsume();
    if ((itemConsumeId != 0) && (itemConsumeCount != 0))
    {
      if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
        sm.addItemName(summonSkill.getTargetConsumeId());
        targetChar.sendPacket(sm);
        return;
      }
      targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
      sm.addItemName(summonSkill.getTargetConsumeId());
      targetChar.sendPacket(sm);
    }
    targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
  }

  public static boolean checkSummonerStatus(L2PcInstance summonerChar)
  {
    if (summonerChar == null) {
      return false;
    }
    if (summonerChar.isInOlympiadMode())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
      return false;
    }

    if (summonerChar.inObserverMode())
    {
      return false;
    }

    if (summonerChar.isInsideZone(4096))
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
      return false;
    }
    return true;
  }

  public static boolean checkTargetStatus(L2PcInstance targetChar, L2PcInstance summonerChar)
  {
    if (targetChar == null) {
      return false;
    }
    if (targetChar.isAlikeDead())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
      sm.addPcName(targetChar);
      summonerChar.sendPacket(sm);
      return false;
    }

    if (targetChar.isInStoreMode())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
      sm.addPcName(targetChar);
      summonerChar.sendPacket(sm);
      return false;
    }

    if ((targetChar.isRooted()) || (targetChar.isInCombat()))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
      sm.addPcName(targetChar);
      summonerChar.sendPacket(sm);
      return false;
    }

    if (targetChar.isInOlympiadMode())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
      return false;
    }

    if (targetChar.isFestivalParticipant())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
      return false;
    }

    if (targetChar.inObserverMode())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
      return false;
    }

    if (targetChar.isInsideZone(4096))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA);
      sm.addString(targetChar.getName());
      summonerChar.sendPacket(sm);
      return false;
    }

    if (summonerChar.isIn7sDungeon())
    {
      int targetCabal = SevenSigns.getInstance().getPlayerCabal(targetChar);
      if (SevenSigns.getInstance().isSealValidationPeriod())
      {
        if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
        {
          summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
          return false;
        }

      }
      else if (targetCabal == 0)
      {
        summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
        return false;
      }
    }

    return true;
  }

  public void gatesRequest(L2DoorInstance door)
  {
    _gatesRequest.setTarget(door);
  }

  public void gatesAnswer(int answer, int type)
  {
    if (_gatesRequest.getDoor() == null)
      return;
    if ((answer == 1) && (getTarget() == _gatesRequest.getDoor()) && (type == 1))
    {
      _gatesRequest.getDoor().openMe();
    }
    else if ((answer == 1) && (getTarget() == _gatesRequest.getDoor()) && (type == 0))
    {
      _gatesRequest.getDoor().closeMe();
    }
    _gatesRequest.setTarget(null);
  }

  public boolean isInFunEvent()
  {
    return (atEvent) || ((CTF._started) && (_inEventCTF));
  }

  public boolean isOffline()
  {
    return _isOffline;
  }

  public void setOffline(boolean set)
  {
    _isOffline = set;
  }

  public boolean isTradeDisabled()
  {
    return _isTradeOff;
  }

  public void setTradeDisabled(boolean set)
  {
    _isTradeOff = set;
  }

  public long getOfflineStartTime()
  {
    return _offlineShopStart;
  }

  public void setOfflineStartTime(long time)
  {
    _offlineShopStart = time;
  }

  private void createPSdb()
  {
    Connection con = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("INSERT INTO account_premium (account_name,premium_service,enddate) values(?,?,?)");
      statement.setString(1, _accountName);
      statement.setInt(2, 0);
      statement.setLong(3, 0L);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Could not insert char data: ").append(e).toString());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public static void restorePremServiceData(L2PcInstance player, String account) {
    boolean sucess = false;
    Connection con = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT premium_service,enddate FROM account_premium WHERE account_name=?");
      statement.setString(1, account);
      ResultSet rset = statement.executeQuery();
      while (rset.next()) {
        sucess = true;
        if (Config.USE_PREMIUMSERVICE) {
          if (rset.getLong("enddate") <= System.currentTimeMillis()) {
            PStimeOver(account);
            player.setPremiumService(0); continue;
          }
          player.setPremiumService(rset.getInt("premium_service")); continue;
        }
        player.setPremiumService(0);
      }

      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("PremiumService: Could not restore PremiumService data for:").append(account).append(".").append(e).toString());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    if (!sucess) {
      player.createPSdb();
      player.setPremiumService(0);
    }
  }

  private static void PStimeOver(String account) {
    Connection con = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?");
      statement.setInt(1, 0);
      statement.setLong(2, 0L);
      statement.setString(3, account);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("PremiumService:  Could not increase data");
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void setClientKey(boolean clientkey)
  {
    _clientkey = clientkey;
  }

  public boolean getClientKey()
  {
    return _clientkey;
  }

  public void setExpOn(boolean expOn)
  {
    _expGainOn = expOn;
  }

  public boolean getExpOn()
  {
    return _expGainOn;
  }

  public void setShowAnim(boolean b)
  {
    _anim = b;
  }

  public boolean getShowAnim()
  {
    return _anim;
  }

  public void addConfirmDlgRequestTime(int requestId, int time)
  {
    confirmDlgRequests.put(Integer.valueOf(requestId), Long.valueOf(System.currentTimeMillis() + time + 2000L));
  }

  public Long getConfirmDlgRequestTime(int requestId)
  {
    return (Long)confirmDlgRequests.get(Integer.valueOf(requestId));
  }

  public void removeConfirmDlgRequestTime(int requestId)
  {
    confirmDlgRequests.remove(Integer.valueOf(requestId));
  }

  public int getInstanceId()
  {
    return _instanceId;
  }

  public static void savePlayerSex(L2PcInstance player, int mode) {
    if (player == null) {
      return;
    }
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE characters SET sex=? WHERE obj_Id=?");
      statement.setInt(1, player.getAppearance().getSex() ? 1 : 0);
      statement.setInt(2, player.getObjectId());
      statement.execute();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("SetSex:  Could not store data");
    }
    finally
    {
      try
      {
        if (con != null)
          con.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void setAutoLoot(boolean b)
  {
    _useAutoLoot = b;
  }

  public boolean isAutoLoot() {
    return _useAutoLoot;
  }

  public void increaseTvTKills()
  {
    _tvtkills += 1;
  }

  public int getTvTKills()
  {
    return _tvtkills;
  }

  public void cleanKills()
  {
    _tvtkills = 0;
  }

  private class TimeStamp
  {
    private int skill;
    private long reuse;
    private Date stamp;

    public TimeStamp(int _skill, long _reuse)
    {
      skill = _skill;
      reuse = _reuse;
      stamp = new Date(new Date().getTime() + reuse);
    }
    public int getSkill() {
      return skill; } 
    public long getReuse() { return reuse;
    }

    public boolean hasNotPassed()
    {
      Date d = new Date();
      if (d.before(stamp))
      {
        reuse -= d.getTime() - (stamp.getTime() - reuse);
        return true;
      }
      return false;
    }
  }

  private class ChatBanTask
    implements Runnable
  {
    L2PcInstance _player;
    protected long _startedAt;

    protected ChatBanTask(L2PcInstance player)
    {
      _player = player;
      _startedAt = System.currentTimeMillis();
    }

    public void run()
    {
      _player.setChatBanned(false, 0);
    }
  }

  private class JailTask
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
      if (_fishType == -1)
        return;
      int check = Rnd.get(1000);
      if (_fishGutsCheck > check)
      {
        stopLookingForFishTask();
        StartFishCombat(_isNoob, _isUpperGrade);
      }
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
      SystemMessage sm = new SystemMessage(SystemMessageId.DROWN_DAMAGE_S1);
      sm.addNumber((int)reduceHp);
      sendPacket(sm);
    }
  }

  class UnmountTask
    implements Runnable
  {
    UnmountTask()
    {
    }

    public void run()
    {
      Ride dismount = new Ride(getObjectId(), 0, 0);
      broadcastPacket(dismount);
      sendMessage("\u0412\u0430\u0448\u0435 \u0436\u0438\u0432\u043E\u0442\u043D\u043E\u0435 \u0441\u043B\u0438\u0448\u043A\u043E\u043C \u0433\u043E\u043B\u043E\u0434\u043D\u043E \u0447\u0442\u043E\u0431\u044B \u043E\u0441\u0442\u0430\u0432\u0430\u0442\u044C\u0441\u044F.");
      _taskUnmount.cancel(false);
      L2PcInstance.access$102(L2PcInstance.this, null);
      sendPacket(new SetupGauge(3, 0));
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
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.PLAYING_FOR_LONG_TIME);
        sendPacket(msg);
      }
      else {
        stopWarnUserTakeBreak();
      }
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

  class StandUpTask
    implements Runnable
  {
    L2PcInstance _player;

    StandUpTask(L2PcInstance player)
    {
      _player = player;
    }

    public void run() {
      _player.setIsSitting(false);
      sittingTaskLaunched = false;
      _player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    }
  }

  class SitDownTask
    implements Runnable
  {
    L2PcInstance _player;

    SitDownTask(L2PcInstance player)
    {
      _player = player;
    }

    public void run() {
      setIsSitting(true);
      _player.setIsParalyzed(false);
      sittingTaskLaunched = false;
      _player.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
    }
  }

  public class SkillDat
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

    public boolean isCtrlPressed()
    {
      return _ctrlPressed;
    }

    public boolean isShiftPressed()
    {
      return _shiftPressed;
    }

    public L2Skill getSkill()
    {
      return _skill;
    }

    public int getSkillId()
    {
      return getSkill() != null ? getSkill().getId() : -1;
    }
  }

  public class gatesRequest
  {
    private L2DoorInstance _target = null;

    public gatesRequest() {  }

    public void setTarget(L2DoorInstance door) { _target = door;
    }

    public L2DoorInstance getDoor()
    {
      return _target;
    }
  }

  public class summonRequest
  {
    private L2PcInstance _target = null;
    private L2Skill _skill = null;

    public summonRequest() {
    }
    public void setTarget(L2PcInstance destination, L2Skill skill) { _target = destination;
      _skill = skill;
    }

    public L2PcInstance getTarget()
    {
      return _target;
    }

    public L2Skill getSkill()
    {
      return _skill;
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
      try
      {
        addItem(_process, _itemId, _count, _reference, _sendMessage);
      }
      catch (Throwable t)
      {
        L2PcInstance._log.log(Level.WARNING, "", t);
      }
    }
  }

  public class AIAccessor extends L2Character.AIAccessor
  {
    protected AIAccessor()
    {
      super(); } 
    public L2PcInstance getPlayer() { return L2PcInstance.this; } 
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
    }

    public void doCast(L2Skill skill)
    {
      super.doCast(skill);

      getPlayer().setRecentFakeDeath(false);
    }
  }
}