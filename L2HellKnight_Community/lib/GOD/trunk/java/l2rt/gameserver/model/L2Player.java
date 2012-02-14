package l2rt.gameserver.model;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.config.ConfigSystem;
import l2rt.database.*;
import l2rt.extensions.Bonus;
import l2rt.extensions.Stat;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.network.SendablePacket;
import l2rt.extensions.scripts.Events;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.Scripts;
import l2rt.extensions.scripts.Scripts.ScriptClassAndMethod;
import l2rt.gameserver.Constants;
import l2rt.gameserver.GameTimeController;
import l2rt.gameserver.RecipeController;
import l2rt.gameserver.ai.*;
import l2rt.gameserver.ai.L2PlayableAI.nextAction;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.network.clientpackets.EnterWorld;
import l2rt.gameserver.communitybbs.BB.Forum;
import l2rt.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.*;
import l2rt.gameserver.loginservercon.LSConnection;
import l2rt.gameserver.loginservercon.gspackets.ChangeAccessLevel;
import l2rt.gameserver.model.BypassManager.BypassType;
import l2rt.gameserver.model.BypassManager.DecodedBypass;
import l2rt.gameserver.model.L2Clan.RankPrivs;
import l2rt.gameserver.model.L2Multisell.MultiSellListContainer;
import l2rt.gameserver.model.L2ObjectTasks.*;
import l2rt.gameserver.model.L2Skill.AddedSkill;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.base.*;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.model.entity.Duel;
import l2rt.gameserver.model.entity.Duel.DuelState;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.olympiad.CompType;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.olympiad.OlympiadGame;
import l2rt.gameserver.model.entity.residence.*;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.gameserver.model.entity.vehicle.L2Vehicle;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.instances.L2CubicInstance.CubicType;
import l2rt.gameserver.model.items.*;
import l2rt.gameserver.model.items.MailParcelController.Letter;
import l2rt.gameserver.model.items.Warehouse.WarehouseType;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestEventType;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.SkillTimeStamp;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.skills.skillclasses.Charge;
import l2rt.gameserver.skills.skillclasses.Transformation;
import l2rt.gameserver.tables.*;
import l2rt.gameserver.taskmanager.AutoSaveManager;
import l2rt.gameserver.taskmanager.BreakWarnManager;
import l2rt.gameserver.templates.*;
import l2rt.gameserver.templates.L2Armor.ArmorType;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.loginserver.Lock.HWIDLockComparator;
import l2rt.util.*;
import l2rt.util.HWID.HWIDComparator;
import l2rt.util.HWID.HardwareID;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import gnu.trove.map.hash.TIntObjectHashMap; 

import static l2rt.gameserver.model.L2Zone.ZoneType.*;

public final class L2Player extends L2Playable
{
	static final Logger _log = Logger.getLogger(L2Player.class.getName());

	public HashMap<Integer, L2SubClass> _classlist = new HashMap<Integer, L2SubClass>(4);

	public static final short STORE_PRIVATE_NONE = 0;
	public static final short STORE_PRIVATE_SELL = 1;
	public static final short STORE_PRIVATE_BUY = 3;
	public static final short STORE_PRIVATE_MANUFACTURE = 5;
	public static final short STORE_OBSERVING_GAMES = 7;
	public static final short STORE_PRIVATE_SELL_PACKAGE = 8;

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
	public static final int RANK_EMPEROR = 12; // unused
	public FastList<UsablePacketItem> CrystallizationProducts;
	public static final int LANG_ENG = 0;
	public static final int LANG_RUS = 1;
	public static final int LANG_UNK = -1;

	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S, S80, S84)*/
	public static final int[] EXPERTISE_LEVELS = {
			//
			0, //NONE
			20, //D
			40, //C
			52, //B
			61, //A
			76, //S
			80, //S80
			84, //S84
			90, //R
			95, //R95
			99, //R99
			Integer.MAX_VALUE, // затычка
	};

	private ClassId _skillLearningClassId;

	private L2GameClient _connection;
	private String _accountName;
	public int _bookmarkslot;

	private int _karma, _pkKills, _pvpKills;
	private int _face, _hairStyle, _hairColor;
	private int _recomHave, _recomLeft, _fame;
	private int _deleteTimer;
	private int _partyMatchingLevels, _partyMatchingRegion;
	private Integer _partyRoom = 0;

	private long _createTime, _onlineTime, _onlineBeginTime, _leaveClanTime, _deleteClanTime, _NoChannel,
			_NoChannelBegin;

	/** The Color of players name / title (white is 0xFFFFFF) */
	private int _nameColor, _titlecolor;

	private double _vitality = 140000;
	private int _curWeightPenalty = 0;

	private boolean _relax;

	boolean sittingTaskLaunched;

	/** Time counter when L2Player is sitting */
	private int _waitTimeWhenSit;

	private boolean AutoLoot = Config.AUTO_LOOT, AutoLootHerbs = Config.AUTO_LOOT_HERBS;

	private final PcInventory _inventory = new PcInventory(this);
	private PcWarehouse _warehouse = new PcWarehouse(this);
	private PcFreight _freight = new PcFreight(this);
	public final BookMarkList bookmarks = new BookMarkList(this, 0);

	/** The table containing all L2RecipeList of the L2Player */
	private final Map<Integer, L2Recipe> _recipebook = new TreeMap<Integer, L2Recipe>();
	private final Map<Integer, L2Recipe> _commonrecipebook = new TreeMap<Integer, L2Recipe>();

	/** The table containing all Quests began by the L2Player */
	private final HashMap<String, QuestState> _quests = new HashMap<String, QuestState>();

	/** The list containing all shortCuts of this L2Player */
	private final ShortCuts _shortCuts = new ShortCuts(this);

	/** The list containing all macroses of this L2Player */
	private final MacroList _macroses = new MacroList(this);

	private final StatsChangeRecorder _statsChangeRecorder = new StatsChangeRecorder(this);

	public L2Radar radar;

	private L2TradeList _tradeList;
	private L2ManufactureList _createList;
	private ConcurrentLinkedQueue<TradeItem> _sellList, _buyList;

	// hennas
	private final L2HennaInstance[] _henna = new L2HennaInstance[3];
	private short _hennaSTR, _hennaINT, _hennaDEX, _hennaMEN, _hennaWIT, _hennaCON;
	private byte[] _hennaElement = { 0, 0, 0, 0, 0, 0 };
	
	private L2Party _party;
	private L2Clan _clan;
	private int _pledgeClass = 0, _pledgeType = 0, _powerGrade = 0, _lvlJoinedAcademy = 0, _apprentice = 0;

	//GM Stuff
	private int _accessLevel;
	private PlayerAccess _playerAccess = new PlayerAccess();
	private boolean _messageRefusal = false, _tradeRefusal = false, _exchangeRefusal = false, _invisible = false,
			_blockAll = false;

	/** The Private Store type of the L2Player (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5) */
	private short _privatestore;

	/** The L2Summon of the L2Player */
	private L2Summon _summon = null;

	private L2DecoyInstance _decoy = null;

	private GCSArray<L2CubicInstance> _cubics = null;
	private L2AgathionInstance _agathion = null;

	private Transaction _transaction;

	private L2ItemInstance _arrowItem;

	/** The fists L2Weapon of the L2Player (used when no weapon is equipped) */
	private L2Weapon _fistsWeaponItem;

	private long _uptime;

	private HashMap<Integer, String> _chars = new HashMap<Integer, String>(8);

	public byte updateKnownCounter = 0;

	/** The current higher Expertise of the L2Player (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)*/
	public int expertiseIndex = 0;
    int armorExpertisePenalty = 0;
    int weaponExpertisePenalty = 0;

	private L2ItemInstance _enchantScroll = null;

	private WarehouseType _usingWHType;
	private boolean _isOnline = false;
	private boolean _isDeleting = false;

	protected boolean _inventoryDisable = false;

	/** The L2NpcInstance corresponding to the last Folk which one the player talked. */
	private L2NpcInstance _lastNpc = null;
	private String _lastBBS_script_operation = null;

	/** тут храним мультиселл с которым работаем, полезно... */
	private MultiSellListContainer _multisell = null;

	protected ConcurrentSkipListSet<Integer> _activeSoulShots = new ConcurrentSkipListSet<Integer>();

	/** Location before entering Observer Mode */
	private Location _obsLoc = new Location();
	private L2WorldRegion _observNeighbor;
	private byte _observerMode = 0;

	public int _telemode = 0;

	public Location _stablePoint = null;

	/** new loto ticket **/
	public int _loto[] = new int[5];
	/** new race ticket **/
	public int _race[] = new int[2];

	private final FastMap<Integer, String> _blockList = new FastMap<Integer, String>().setShared(true); // characters blocked with '/block <charname>' cmd

	private boolean _isConnected = true;

	private boolean _hero = false;
	private int _team = 0;
	private boolean _checksForTeam = false;

	// time on login in game
	private long _lastAccess;

	/** True if the L2Player is in a boat */
	private L2Vehicle _vehicle;
	private Location _inVehiclePosition;

	protected int _baseClass = -1;
	protected L2SubClass _activeClass = null;

	private Bonus _bonus;
	private Future<?> _bonusExpiration;

	public boolean _isSitting = false;

	private boolean _noble = true;
	private boolean _inOlympiadMode = false;
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;
	private int _olympiadObserveId = -1;
	
	public boolean InKrateisCube = false;

	/** ally with ketra or varka related wars*/
	private int _varka = 0;
	private int _ketra = 0;
	private int _ram = 0;

	/** The Siege state */
	private int _siegeState = 0;

	private byte[] _keyBindings;

	public ScheduledFuture<?> _taskWater;

	protected HashMap<Integer, Long> _StatKills;
	protected HashMap<Integer, Long> _StatDrop;
	protected HashMap<Integer, Long> _StatCraft;

	private Forum _forumMemo;
	private ReentrantLock _forumMemoLock = new ReentrantLock();

	private int _cursedWeaponEquippedId = 0;

	private L2Fishing _fishCombat;
	private boolean _fishing = false;
	private Location _fishLoc = new Location();
	private L2ItemInstance _lure = null;
	public ScheduledFuture<?> _taskforfish;
    public ScheduledFuture<?> recVoteTask;
    private long recSupportTime = 0;
    private int recomTimeLeft = 0;
    private long lastCheckBonusTime = 0;
    private boolean isRecSupportTime = false;
	private Future<?> _kickTask;

	private boolean _isInCombatZone;
	private boolean _isOnSiegeField;
	private boolean _isInPeaceZone;
	private boolean _isInSSZone;

	private boolean _offline = false;

	private int pcBangPoints;

	/** Коллекция для временного хранения скилов данной трансформации */
	HashMap<Integer, L2Skill> _transformationSkills = new HashMap<Integer, L2Skill>();

	private int _expandInventory = 0;
	private int _expandWarehouse = 0;

	private boolean _notShowBuffAnim = false;
	private GArray<String> bypasses = null, bypasses_bbs = null;
	private static final String NOT_CONNECTED = "<not connected>";
	
	/**
	 * Конструктор для L2Player. Напрямую не вызывается, для создания игрока используется PlayerManager.create
	 *
	 */
	public L2Player(final int objectId, final L2PlayerTemplate template, final String accountName)
	{
		super(objectId, template);

		_accountName = accountName;
		_nameColor = 0xFFFFFF;
		_titlecolor = 0xFFFF77;
		_baseClass = getClassId().getId();
	}

	/**
	 * Constructor<?> of L2Player (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2Player </li>
	 * <li>Create a L2Radar object</li>
	 * <li>Retrieve from the database all items of this L2Player and add them to _inventory </li>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SET the account name of the L2Player</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PlayerTemplate to apply to the L2Player
	 *
	 */
	private L2Player(final int objectId, final L2PlayerTemplate template)
	{
		this(objectId, template, null);

		getInventory().restore(); 
		
		// Create an AI
		setAI(new L2PlayerAI(this));

		// Create a L2Radar object
		radar = new L2Radar(this);

		if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			setPlayerAccess(Config.gmlist.get(objectId));
		else
			setPlayerAccess(Config.gmlist.get(new Integer(0)));

		// Retrieve from the database all macroses of this L2Player and add them to _macroses
		_macroses.restore();
	}

	public String getAccountName()
	{
		if(_connection == null)
			return _accountName;
		return _connection.getLoginName();
	}

	public String getIP()
	{
		if(_connection == null)
			return NOT_CONNECTED;
		return _connection.getIpAddr();
	}
	
	public boolean getPA()
	{
		if (_bonusExpiration != null)
			return true;
		else
			return false;
	}

	/**
	 * Возвращает список персонажей на аккаунте, за исключением текущего
	 * @return Список персонажей
	 */
	public HashMap<Integer, String> getAccountChars()
	{
		return _chars;
	}

	@Override
	public final L2PlayerTemplate getTemplate()
	{
		return (L2PlayerTemplate) _template;
	}

	@Override
	public L2PlayerTemplate getBaseTemplate()
	{
		return (L2PlayerTemplate) _baseTemplate;
	}

	public void changeSex()
	{
		boolean male = true;
		if(getSex() == 1)
			male = false;
		_template = CharTemplateTable.getInstance().getTemplate(getClassId(), !male);
	}

	@Override
	public L2PlayableAI getAI()
	{
		if(_ai == null)
			_ai = new L2PlayerAI(this);
		return (L2PlayableAI) _ai;
	}

	@Override
	public void doAttack(final L2Character target)
	{
		super.doAttack(target);

		if(_cubics != null)
			for(L2CubicInstance cubic : _cubics)
				if(cubic.getType() != CubicType.LIFE_CUBIC)
					cubic.doAction(target);
		if(_agathion != null)
			_agathion.doAction(target);
	}

	@Override
	public void doCast(final L2Skill skill, final L2Character target, boolean forceUse)
	{
		if(skill == null)
			return;

		super.doCast(skill, target, forceUse);

		if(getUseSeed() != 0 && skill.getSkillType() == SkillType.SOWING)
			sendPacket(new ExUseSharedGroupItem(getUseSeed(), getUseSeed(), 5000, 5000));

		if(skill.isOffensive() && target != null)
		{
			if(_cubics != null)
				for(L2CubicInstance cubic : _cubics)
					if(cubic.getType() != CubicType.LIFE_CUBIC)
						cubic.doAction(target);
			if(_agathion != null)
				_agathion.doAction(target);
		}
	}

	public void refreshSavedStats()
	{
		_statsChangeRecorder.refreshSaves();
	}

	@Override
	public void sendChanges()
	{
		_statsChangeRecorder.sendChanges();
	}

	@Override
	public final byte getLevel()
	{
		return _activeClass == null ? 1 : _activeClass.getLevel();
	}

	public final boolean setLevel(final int lvl)
	{
		if(_activeClass != null)
			_activeClass.setLevel((byte) lvl);
		return lvl == getLevel();
	}

	public byte getSex()
	{
		return getTemplate().isMale ? (byte) 0 : (byte) 1;
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

	public boolean isInStoreMode()
	{
		return _privatestore != STORE_PRIVATE_NONE && _privatestore != STORE_OBSERVING_GAMES;
	}

	public void offline()
	{
		setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
		setOfflineMode(true);
		clearHateList(false);
		setVar("offline", String.valueOf(System.currentTimeMillis() / 1000));
		if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0)
			startKickTask(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK * 1000L);

		if(getParty() != null)
			getParty().oustPartyMember(this);

		if(getPet() != null && getPet().getNpcId() != PetDataTable.IMPROVED_BABY_KOOKABURRA_ID && getPet().getNpcId() != PetDataTable.IMPROVED_BABY_COUGAR_ID)
			getPet().unSummon();

		CursedWeaponsManager.getInstance().doLogout(this);

		if(isInOlympiadMode() || getOlympiadGameId() > -1)
			Olympiad.logoutPlayer(this);

		sendPacket(Msg.LeaveWorld);
		setConnected(false);
		setOnlineStatus(false);
		//LSConnection.getInstance().removeAccount(getNetConnection());
		//LSConnection.getInstance().sendPacket(new PlayerLogout(getNetConnection().getLoginName()));
		broadcastUserInfo(true);

		store(false);
		_connection.OnOfflineTrade();
		//TODO освобождать кучу других объектов связанных с игроком не нужных в оффлайне
	}

	public void logout(boolean shutdown, boolean restart, boolean kicked, boolean instant)
	{
		if(isLogoutStarted())
			return;

		Log.LogChar(this, Log.Logout, "");

		// Msg.ExRestartClient - 2 таблички появляется (вторая GG Fail), нажатие ок приводит к закрытию клиента
		// Msg.ServerClose - табличка появляется, после нажатия ок переходит к диалогу ввода логина/пароля
		// Msg.LeaveWorld - молча закрывает клиент (используется при выходе из игры)

		if(kicked && Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK)
			if(isCursedWeaponEquipped())
			{
				_pvpFlag = 0;
				CursedWeaponsManager.getInstance().dropPlayer(this);
			}

		if(restart)
		{
			// При рестарте просто обнуляем коннект
			if(instant || Config.PLAYER_LOGOUT_INGAME_TIME == 0)
				deleteMe();
			else
				scheduleDelete(Config.PLAYER_LOGOUT_INGAME_TIME);
			if(_connection != null)
				_connection.setActiveChar(null);
		}
		else
		{
			L2GameServerPacket sp = shutdown || kicked ? Msg.ServerClose : Msg.LeaveWorld;
			sendPacket(sp);
			if(_connection != null && _connection.getConnection() != null)
				_connection.getConnection().close(sp);
			if(instant || Config.PLAYER_LOGOUT_INGAME_TIME == 0)
				deleteMe();
			else
				scheduleDelete(Config.PLAYER_LOGOUT_INGAME_TIME);
		}

		_connection = null;
		setConnected(false);
		broadcastUserInfo(false);
	}

	public void prepareToLogout()
	{
		if(isFlying() && !checkLandingState())
			setLoc(MapRegion.getTeleToClosestTown(this));

		if(isCastingNow())
			abortCast(true);

		// При логауте автоматом проигрывается дуэль.
		if(getDuel() != null)
			getDuel().onPlayerDefeat(this);

		CursedWeaponsManager.getInstance().doLogout(this);

		if(inObserverMode())
			if(getOlympiadObserveId() == -1)
				leaveObserverMode();
			else
				leaveOlympiadObserverMode();

		if(isInOlympiadMode() || getOlympiadGameId() > -1)
			Olympiad.logoutPlayer(this);

		// Вызов всех хэндлеров, определенных в скриптах
		Object[] script_args = new Object[] { this };
		for(ScriptClassAndMethod handler : Scripts.onPlayerExit)
			callScripts(handler.scriptClass, handler.method, script_args);

		if(_stablePoint != null)
		{
			teleToLocation(_stablePoint);
			addAdena(_stablePoint.h);
			unsetVar("wyvern_moneyback");
		}

		if(getPet() != null)
			try
			{
				getPet().unSummon();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				_log.log(Level.WARNING, "prepareToLogout()", t);
			}

		if(isInParty())
			try
			{
				leaveParty();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				_log.log(Level.WARNING, "prepareToLogout()", t);
			}
	}

	private boolean _logoutStarted = false;

	public boolean isLogoutStarted()
	{
		return _logoutStarted;
	}

	public void setLogoutStarted(boolean logoutStarted)
	{
		_logoutStarted = logoutStarted;
	}

	/**
	 * @return a table containing all L2RecipeList of the L2Player.<BR><BR>
	 */
	public Collection<L2Recipe> getDwarvenRecipeBook()
	{
		return _recipebook.values();
	}

	public Collection<L2Recipe> getCommonRecipeBook()
	{
		return _commonrecipebook.values();
	}

	public int recipesCount()
	{
		return _commonrecipebook.size() + _recipebook.size();
	}

	public boolean hasRecipe(final L2Recipe id)
	{
		return _recipebook.containsValue(id) || _commonrecipebook.containsValue(id);
	}

	public boolean findRecipe(final int id)
	{
		return _recipebook.containsKey(id) || _commonrecipebook.containsKey(id);
	}

	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void registerRecipe(final L2Recipe recipe, boolean saveDB)
	{
		if(recipe.isDwarvenRecipe())
			_recipebook.put(recipe.getId(), recipe);
		else
			_commonrecipebook.put(recipe.getId(), recipe);
		if(saveDB)
			mysql.set("REPLACE INTO character_recipebook (char_id, id) VALUES(?,?)", getObjectId(), recipe.getId());
	}

	/**
	 * Remove a L2RecipList from the table _recipebook containing all L2RecipeList of the L2Player
	 */
	public void unregisterRecipe(final int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_recipebook.remove(RecipeID);
		}
		else if(_commonrecipebook.containsKey(RecipeID))
		{
			mysql.set("DELETE FROM `character_recipebook` WHERE `char_id`=? AND `id`=? LIMIT 1", getObjectId(), RecipeID);
			_commonrecipebook.remove(RecipeID);
		}
		else
			_log.warning("Attempted to remove unknown RecipeList" + RecipeID);
	}

	// ------------------- Quest Engine ----------------------

	public QuestState getQuestState(String quest)
	{
		return _quests != null ? _quests.get(quest) : null;
	}

	public QuestState getQuestState(Class<?> quest)
	{
		return getQuestState(quest.getSimpleName());
	}

	public boolean isQuestCompleted(String quest)
	{
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public boolean isQuestCompleted(Class<?> quest)
	{
		QuestState q = getQuestState(quest);
		return q != null && q.isCompleted();
	}

	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuest().getName(), qs);
	}

	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}

	public Quest[] getAllActiveQuests()
	{
		GArray<Quest> quests = new GArray<Quest>();
		for(final QuestState qs : _quests.values())
			if(qs != null && qs.isStarted())
				quests.add(qs.getQuest());
		return quests.toArray(new Quest[quests.size()]);
	}

	public QuestState[] getAllQuestsStates()
	{
		return _quests.values().toArray(new QuestState[_quests.size()]);
	}

	public GArray<QuestState> getQuestsForEvent(L2NpcInstance npc, QuestEventType event)
	{
		GArray<QuestState> states = new GArray<QuestState>();
		Quest[] quests = npc.getTemplate().getEventQuests(event);
		if(quests != null)
			for(Quest quest : quests)
				if(getQuestState(quest.getName()) != null && !getQuestState(quest.getName()).isCompleted())
					states.add(getQuestState(quest.getName()));
		return states;
	}

	public void processQuestEvent(String quest, String event, L2NpcInstance npc)
	{
		if(event == null)
			event = "";
		QuestState qs = getQuestState(quest);
		if(qs == null)
		{
			Quest q = QuestManager.getQuest(quest);
			if(q == null)
			{
				System.out.println("Quest " + quest + " not found!!!");
				return;
			}
			qs = q.newQuestState(this, Quest.CREATED);
		}
		if(qs == null || qs.isCompleted())
			return;
		qs.getQuest().notifyEvent(event, qs, npc);
		sendPacket(new QuestList(this));
	}

	/**
	 * Проверка на переполнение инвентаря и перебор в весе для квестов и эвентов
	 * @return true если ве проверки прошли успешно
	 */
	public boolean isQuestContinuationPossible(boolean msg)
	{
		if(getWeightPenalty() >= 3 || getInventoryLimit() * 0.8 < getInventory().getSize())
		{
			if(msg)
				sendPacket(Msg.PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			return false;
		}
		return true;
	}

	// ----------------- End of Quest Engine -------------------

	public Collection<L2ShortCut> getAllShortCuts()
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

	/**
	 * Возвращает состояние осады L2Player.<BR>
	 * 1 = attacker, 2 = defender, 0 = не учавствует
	 * @return состояние осады
	 */
	public int getSiegeState()
	{
		return _siegeState;
	}

	/**
	* Устанавливает состояние осады L2Player.<BR>
	* 1 = attacker, 2 = defender, 0 = не учавствует
	*/
	public void setSiegeState(int siegeState)
	{
		_siegeState = siegeState;
		broadcastRelationChanged();
	}

	public boolean isCastleLord(int castleId)
	{
		return _clan != null && isClanLeader() && _clan.getHasCastle() == castleId;
	}

	/**
	 * Проверяет является ли этот персонаж владельцем крепости
	 * @param fortressId
	 * @return true если владелец
	 */
	public boolean isFortressLord(int fortressId)
	{
		return _clan != null && isClanLeader() && _clan.getHasFortress() == fortressId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(final int pkKills)
	{
		_pkKills = pkKills;
	}

	public long getCreateTime()
	{
		return _createTime;
	}

	public void setCreateTime(final long createTime)
	{
		_createTime = createTime;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(final int deleteTimer)
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

	public int getRecomLeft()
	{
		return _recomLeft;
	}

	public void setRecomLeft(final int value)
	{
		_recomLeft = value;
	}

	@Override
	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma)
	{
		if(_karma == karma)
			return;

		_karma = karma;

		if(karma < 0)
			for(final L2Character object : L2World.getAroundCharacters(this))
				if(object instanceof L2GuardInstance && object.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					object.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

		sendChanges();

		if(getPet() != null)
			getPet().broadcastPetInfo();
	}

	public int getMaxLoad()
	{
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://l2rt.bravehost.com/weightlimit.html (May 2007)
		// Fitted exponential curve to the data
		int con = getCON();
		if(con < 1)
			return (int) (31000 * Config.MAXLOAD_MODIFIER);
		else if(con > 59)
			return (int) (176000 * Config.MAXLOAD_MODIFIER);
		else
			return (int) calcStat(Stats.MAX_LOAD, Math.pow(1.029993928, con) * 30495.627366 * Config.MAXLOAD_MODIFIER, this, null);
	}

    public int getArmorExpertisePenalty()
	{
        return armorExpertisePenalty;
    }

    public int getWeaponsExpertisePenalty()
	{
        return weaponExpertisePenalty;
    }

	public int getWeightPenalty()
	{
		return _curWeightPenalty;
	}

	@Override
	public void updateEffectIcons()
	{
		if(isMassUpdating())
			return;

		L2Effect[] effects = getEffectList().getAllFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());

		PartySpelled ps = new PartySpelled(this, false);
		AbnormalStatusUpdate mi = new AbnormalStatusUpdate();

		for(L2Effect effect : effects)
			if(effect != null && effect.isInUse())
			{
				if(effect.getStackType().equalsIgnoreCase("HpRecoverCast"))
					sendPacket(new ShortBuffStatusUpdate(effect));
				else
					effect.addIcon(mi);
				if(_party != null)
					effect.addPartySpelledIcon(ps);
			}

		sendPacket(mi);
		if(_party != null)
			_party.broadcastToPartyMembers(ps);

		if(Config.ENABLE_OLYMPIAD && isInOlympiadMode() && isOlympiadCompStart())
		{
			OlympiadGame olymp_game = Olympiad.getOlympiadGame(getOlympiadGameId());
			if(olymp_game != null)
			{
				ExOlympiadSpelledInfo OlympiadSpelledInfo = new ExOlympiadSpelledInfo();

				for(L2Effect effect : effects)
					if(effect != null && effect.isInUse())
						effect.addOlympiadSpelledIcon(this, OlympiadSpelledInfo);

				if(olymp_game.getType() == CompType.CLASSED || olymp_game.getType() == CompType.NON_CLASSED)
					for(L2Player member : olymp_game.getTeamMembers(this))
						member.sendPacket(OlympiadSpelledInfo);

				for(L2Player member : olymp_game.getSpectators())
					member.sendPacket(OlympiadSpelledInfo);
			}
		}
	}

	public void refreshOverloaded()
	{
		if(isMassUpdating() || getMaxLoad() <= 0)
			return;

		setOverloaded(getCurrentLoad() > getMaxLoad());
		double weightproc = 100. * (getCurrentLoad() - calcStat(Stats.MAX_NO_PENALTY_LOAD, 0, this, null)) / getMaxLoad();
		int newWeightPenalty = 0;

		if(weightproc < 50)
			newWeightPenalty = 0;
		else if(weightproc < 66.6)
			newWeightPenalty = 1;
		else if(weightproc < 80)
			newWeightPenalty = 2;
		else if(weightproc < 100)
			newWeightPenalty = 3;
		else
			newWeightPenalty = 4;

		if(_curWeightPenalty == newWeightPenalty)
			return;

		_curWeightPenalty = newWeightPenalty;
		if(_curWeightPenalty > 0)
			super.addSkill(SkillTable.getInstance().getInfo(4270, _curWeightPenalty));
		else
			super.removeSkill(getKnownSkill(4270));

		sendPacket(new EtcStatusUpdate(this));
	}

    public void checkGradeExpertiseUpdate()
	{
		if(isMassUpdating())
			return;

        int level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
        int current = expertiseIndex, changeTo = -1;

        for(short i = 0; i < EXPERTISE_LEVELS.length; i++)
            if(level >= EXPERTISE_LEVELS[i])
                changeTo = i;

        if(changeTo == -1)
            return;

        if(current == changeTo) // nothing to change
            return;

        super.removeSkill(getKnownSkill(239));
        if(changeTo > 0)
            super.addSkill(SkillTable.getInstance().getInfo(239, changeTo));

        sendPacket(new SkillList(this));
        expertiseIndex = changeTo;
    }

    public void validateItemExpertisePenalties(boolean grade, boolean armor, boolean weapon)
	{
        if(grade)
            checkGradeExpertiseUpdate();
        if(armor)
            checkArmorPenalty();
        if(weapon)
            checkWeaponPenalty();
    }

    private void checkArmorPenalty()
	{
        int current = -1;
        L2ItemInstance[] f = getInventory().getItems();
        for(L2ItemInstance item : f) {
            if(item.isEquipped())
			{
                int itemType2 = item.getItem().getType2();
                if(itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
				{
                    int crystaltype = item.getItem().getCrystalType().ordinal();
                    if(current < crystaltype)
                        current = crystaltype;
                }
            }
        }

        if (current <= expertiseIndex)
		{
            armorExpertisePenalty = 0;
            sendPacket(new EtcStatusUpdate(this));

            L2Skill s = getKnownSkill(6213);
            if(s != null)
			{
                this.removeSkillById(s.getId());
                sendPacket(new SkillList(this));
            }
        }
		else
		{
            int penalty = current - expertiseIndex;

            if(penalty > 4)
                penalty = 4;

            armorExpertisePenalty = penalty;

            super.removeSkill(getKnownSkill(6213));
            if(penalty > 0)
                super.addSkill(SkillTable.getInstance().getInfo(6213, penalty));

            sendPacket(new SkillList(this));
            sendPacket(new EtcStatusUpdate(this));
        }
    }

    private void checkWeaponPenalty()
	{
        int current = -1;
        L2ItemInstance[] f = getInventory().getItems();
        for(L2ItemInstance item : f)
		{
            if(item.isEquipped())
			{
                int itemType2 = item.getItem().getType2();
                if(itemType2 == L2Item.TYPE2_WEAPON)
				{
                    int crystaltype = item.getItem().getCrystalType().ordinal();
                    if(current < crystaltype)
                        current = crystaltype;
                }
            }
        }

        if(current <= expertiseIndex)
		{
            weaponExpertisePenalty = 0;
            sendPacket(new EtcStatusUpdate(this));

            L2Skill s = getKnownSkill(6209);
            if(s != null)
			{
                this.removeSkillById(s.getId());
                sendPacket(new SkillList(this));
            }
        }
		else
		{
            int penalty = current - expertiseIndex;

            if(penalty > 4)
                penalty = 4;

            weaponExpertisePenalty = penalty;

            super.removeSkill(getKnownSkill(6209));
            if(penalty > 0)
                super.addSkill(SkillTable.getInstance().getInfo(6209, penalty));

            sendPacket(new SkillList(this));
            sendPacket(new EtcStatusUpdate(this));
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

	public void addClanPointsOnProfession(final int id)
	{
		if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.values()[id].getLevel() == 2)
			_clan.incReputation(100, true, "Academy");
		else if(getLvlJoinedAcademy() != 0 && _clan != null && _clan.getLevel() >= 5 && ClassId.values()[id].getLevel() == 3)
		{
			int earnedPoints = 0;
			if(getLvlJoinedAcademy() <= 16)
				earnedPoints = 2000;
			else if(getLvlJoinedAcademy() >= 39)
				earnedPoints = 290;
			else
				earnedPoints = 200 - (getLvlJoinedAcademy() - 16) * 20;

			_clan.removeClanMember(getObjectId());
			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS);
			sm.addString(getName());
			sm.addNumber(_clan.incReputation(earnedPoints, true, "Academy"));
			_clan.broadcastToOnlineMembers(sm);
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListDelete(getName()), this);

			setClan(null);
			setTitle("");
			sendPacket(Msg.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
			setLeaveClanTime(0);

			broadcastUserInfo(true);
			broadcastRelationChanged();

			sendPacket(Msg.PledgeShowMemberListDeleteAll);
		}
	}

	/**
	 * Set the template of the L2Player.
	 * @param id The Identifier of the L2PlayerTemplate to set to the L2Player
	 */
	public synchronized void setClassId(final int id, boolean noban)
	{
		if(!noban && !(ClassId.values()[id].equalsOrChildOf(ClassId.values()[getActiveClassId()]) || getPlayerAccess().CanChangeClass || Config.EVERYBODY_HAS_ADMIN_RIGHTS))
		{
			Thread.dumpStack();
			Util.handleIllegalPlayerAction(this, "L2Player[1535]", "tried to change class " + getActiveClassId() + " to " + id, 1);
			return;
		}

		//Если новый ID не принадлежит имеющимся классам значит это новая профа
		if(!getSubClasses().containsKey(id))
		{
			final L2SubClass cclass = getActiveClass();
			getSubClasses().remove(getActiveClassId());
			changeClassInDb(cclass.getClassId(), id);
			if(cclass.isBase())
			{
				setBaseClass(id);
				//addClanPointsOnProfession(id);
				L2ItemInstance coupons = null;
				if(ClassId.values()[id].getLevel() == 2)
				{
					if(Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemTemplates.getInstance().createItem(8869);
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
				else if(ClassId.values()[id].getLevel() == 3)
				{
					addClanPointsOnProfession(id);
					if(Config.ALT_ALLOW_SHADOW_WEAPONS)
						coupons = ItemTemplates.getInstance().createItem(8870);
					unsetVar("newbiearmor");
					unsetVar("dd1"); // удаляем отметки о выдаче дименшен даймондов
					unsetVar("dd2");
					unsetVar("dd3");
					unsetVar("prof2.1");
					unsetVar("prof2.2");
					unsetVar("prof2.3");
					checkReferralBonus(1);
				}
				else if(ClassId.values()[id].getLevel() == 4)
					checkReferralBonus(2);

				if(coupons != null)
				{
					coupons.setCount(15);
					getInventory().addItem(coupons);
					sendPacket(SystemMessage.obtainItems(coupons));
				}
			}

			// Выдача Holy Pomander
			switch(ClassId.values()[id])
			{
				case cardinal:
					Functions.addItem(this, 15307, 7);
					break;
				case evaSaint:
					Functions.addItem(this, 15308, 7);
					break;
				case shillienSaint:
					Functions.addItem(this, 15309, 7);
					break;
			}

			cclass.setClassId(id);
			getSubClasses().put(id, cclass);
			rewardSkills();
			storeCharSubClasses();

			// Социалка при получении профы
			broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
			//broadcastPacket(new SocialAction(getObjectId(), 16));
			sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
			broadcastUserInfo(true);
		}

		L2PlayerTemplate t = CharTemplateTable.getInstance().getTemplate(id, getSex() == 1);
		if(t == null)
		{
			_log.severe("Missing template for classId: " + id);
			// do not throw error - only print error
			return;
		}

		// Set the template of the L2Player
		setTemplate(t);

		// Update class icon in party and clan
		if(isInParty())
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));
		if(getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
	}

	public void setClassId(final int id)
	{
		setClassId(id, false);
	}

	public long getExp()
	{
		return _activeClass == null ? 0 : _activeClass.getExp();
	}

	public long getMaxExp()
	{
		return _activeClass == null ? Experience.LEVEL[Experience.getMaxLevel() + 1] : _activeClass.getMaxExp();
	}

	public void addExp(long val)
	{
		if(_activeClass != null)
			_activeClass.addExp(val);
	}

	public void setEnchantScroll(final L2ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public L2ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}

	public void setFistsWeaponItem(final L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	public L2Weapon findFistsWeaponItem(final int classId)
	{
		//human fighter fists
		if(classId >= 0x00 && classId <= 0x09)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(246);

		//human mage fists
		if(classId >= 0x0a && classId <= 0x11)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(251);

		//elven fighter fists
		if(classId >= 0x12 && classId <= 0x18)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(244);

		//elven mage fists
		if(classId >= 0x19 && classId <= 0x1e)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(249);

		//dark elven fighter fists
		if(classId >= 0x1f && classId <= 0x25)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(245);

		//dark elven mage fists
		if(classId >= 0x26 && classId <= 0x2b)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(250);

		//orc fighter fists
		if(classId >= 0x2c && classId <= 0x30)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(248);

		//orc mage fists
		if(classId >= 0x31 && classId <= 0x34)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(252);

		//dwarven fists
		if(classId >= 0x35 && classId <= 0x39)
			return (L2Weapon) ItemTemplates.getInstance().getTemplate(247);

		return null;
	}

	/**
	 * Добавляет чару опыт и/или сп с учетом личного бонуса
	 */
	@Override
	public void addExpAndSp(long addToExp, long addToSp)
	{
		addExpAndSp(addToExp, addToSp, true, true);
	}

	/**
	 * Добавляет чару опыт и/или сп, с учетом личного бонуса или нет
	 */
	@Override
	public void addExpAndSp(long addToExp, long addToSp, boolean applyBonus, boolean appyToPet)
	{
		if(applyBonus)
		{
			addToExp *= Config.RATE_XP * getRateExp();
			addToSp *= Config.RATE_SP * getRateSp();
		}

		if(addToExp > 0)
		{
			if(appyToPet)
			{
				L2Summon pet = getPet();
				if(pet != null && !pet.isDead())
					// Sin Eater забирает всю экспу у персонажа
					if(pet.getNpcId() == PetDataTable.SIN_EATER_ID)
					{
						pet.addExpAndSp(addToExp, 0);
						addToExp = 0;
					}
					else if(pet.isPet() && pet.getExpPenalty() > 0f)
						if(pet.getLevel() > getLevel() - 20 && pet.getLevel() < getLevel() + 5)
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty()), 0);
							addToExp *= 1f - pet.getExpPenalty();
						}
						else
						{
							pet.addExpAndSp((long) (addToExp * pet.getExpPenalty() / 5f), 0);
							addToExp *= 1f - pet.getExpPenalty() / 5f;
						}
					else if(pet.isSummon())
						addToExp *= 1f - pet.getExpPenalty();
			}

			// Remove Karma when the player kills L2MonsterInstance
			if(!isCursedWeaponEquipped() && addToSp > 0 && _karma < 0) {
				_karma += addToSp / (Config.KARMA_SP_DIVIDER * Config.RATE_SP);
				if (_karma > 0)
					_karma = 0;
			}

			long max_xp = getVarB("NoExp") ? Experience.LEVEL[getLevel() + 1] - 1 : getMaxExp();
			addToExp = Math.min(addToExp, max_xp - getExp());
		}

		if (_karma >= 0) {
			addExp(addToExp);
			addSp(addToSp);
		}

		if(addToSp > 0 && addToExp == 0 && _karma >= 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_SP).addNumber(addToSp));
		else if(addToSp > 0 || addToExp > 0 && _karma >= 0)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP).addNumber(addToExp).addNumber(addToSp));

		long exp = getExp();
		int level = getLevel();

		int _MaxLevel = 99;
			
		while(level < _MaxLevel && exp >= Experience.LEVEL[level + 1] && increaseLevel())
			level = getLevel();

		while(exp < Experience.LEVEL[level] && decreaseLevel())
			level = getLevel();
		sendChanges();
	}

	/**
	 * Give Expertise skill of this level.<BR><BR>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2Player </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2Player</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 */
	private void rewardSkills()
	{
		boolean update = false;
		if(ConfigSystem.getBoolean("AutoLearnSkills"))
		{
			int unLearnable = 0;
			GArray<L2SkillLearn> skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
			while(skills.size() > unLearnable)
			{
				unLearnable = 0;
				for(L2SkillLearn s : skills)
				{
					L2Skill sk = SkillTable.getInstance().getInfo(s.id, s.skillLevel);
					if(sk == null || !sk.getCanLearn(getClassId()) || s.getMinLevel() > ConfigSystem.getInt("AutoLearnSkillsMaxLevel"))
					{
						unLearnable++;
						continue;
					}
					addSkill(sk, true);
				}
				skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
			}
			update = true;
		}
		else
		{
			for(L2SkillLearn skill : SkillTreeTable.getInstance().getAvailableSkills(this, getClassId()))
			{
				if(skill._repCost == 0 && skill._spCost == 0 && skill.itemCount == 0)
				{
					L2Skill sk = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
					addSkill(sk, true);
					if(!getAllShortCuts().isEmpty() && sk.getLevel() > 1)
					{
						for(L2ShortCut sc : getAllShortCuts())
						{
							if(sc.id == sk.getId() && sc.type == L2ShortCut.TYPE_SKILL)
							{
								L2ShortCut newsc = new L2ShortCut(sc.slot, sc.page, sc.type, sc.id, sk.getLevel());
								sendPacket(new ShortCutRegister(newsc));
								registerShortCut(newsc);
							}
						}
					}
					update = true;
				}
			}
		}

		if(update)
			sendPacket(new SkillList(this));

		// This function gets called on login, so not such a bad place to check weight
		// Update the overloaded status of the L2Player
		refreshOverloaded();
        checkGradeExpertiseUpdate();
        checkWeaponPenalty();
        checkArmorPenalty();
		AwakingManager.getInstance().ChekRelationSkill(this);
	}

	public Race getRace()
	{
		return getBaseTemplate().race;
	}

	public int getIntSp()
	{
		return (int) getSp();
	}

	public long getSp()
	{
		return _activeClass == null ? 0 : _activeClass.getSp();
	}

	public void setSp(long sp)
	{
		if(_activeClass != null)
			_activeClass.setSp(sp);
	}

	public void addSp(long val)
	{
		if(_activeClass != null)
			_activeClass.addSp(val);
	}

	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	@Override
	public int getClanCrestId()
	{
		return _clan == null ? 0 : _clan.getCrestId();
	}

	@Override
	public int getClanCrestLargeId()
	{
		return _clan == null ? 0 : _clan.getCrestLargeId();
	}

	public long getLeaveClanTime()
	{
		return _leaveClanTime;
	}

	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}

	public void setLeaveClanTime(final long time)
	{
		_leaveClanTime = time;
	}

	public void setDeleteClanTime(final long time)
	{
		_deleteClanTime = time;
	}

	public void setOnlineTime(final long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

    public long getOnlineTime() {
        return _onlineTime;
    }

	public void setNoChannel(final long time)
	{
		_NoChannel = time;
		if(_NoChannel > 2145909600000L || _NoChannel < 0)
			_NoChannel = -1;

		if(_NoChannel > 0)
			_NoChannelBegin = System.currentTimeMillis();
		else
			_NoChannelBegin = 0;

		sendPacket(new EtcStatusUpdate(this));
	}

	public long getNoChannel()
	{
		return _NoChannel;
	}

	public long getNoChannelRemained()
	{
		if(_NoChannel == 0)
			return 0;
		else if(_NoChannel < 0)
			return -1;
		else
		{
			long remained = _NoChannel - System.currentTimeMillis() + _NoChannelBegin;
			if(remained < 0)
				return 0;

			return remained;
		}
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
		if(_leaveClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _leaveClanTime >= 24 * 60 * 60 * 1000L)
		{
			_leaveClanTime = 0;
			return true;
		}
		return false;
	}

	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if(System.currentTimeMillis() - _deleteClanTime >= 10 * 24 * 60 * 60 * 1000L)
		{
			_deleteClanTime = 0;
			return true;
		}
		return false;
	}

	public SystemMessage canJoinParty(L2Player inviter)
	{
		Transaction transaction = getTransaction();
		if(transaction != null && transaction.isInProgress() && transaction.getOtherPlayer(this) != inviter)
			return Msg.WAITING_FOR_ANOTHER_REPLY; // занят
		if(isBlockAll() || getMessageRefusal()) // всех нафиг
			return Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE;
		if(isInParty()) // уже
			return new SystemMessage(SystemMessage.S1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addString(getName());
		if(ReflectionTable.getInstance().findSoloKamaloka(getObjectId()) != null) // в соло каме
			return Msg.INVALID_TARGET;
		if(isCursedWeaponEquipped() || inviter.isCursedWeaponEquipped()) // зарич
			return Msg.INVALID_TARGET;
		if(inviter.isInOlympiadMode() || isInOlympiadMode()) // олимпиада
			return Msg.INVALID_TARGET;
		if(!inviter.getPlayerAccess().CanJoinParty || !getPlayerAccess().CanJoinParty) // низя
			return Msg.INVALID_TARGET;
		if(getTeam() != 0) // участник пвп эвента или дуэли
			return Msg.INVALID_TARGET;
		return null;
	}

	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}

	public void removeItemFromShortCut(final int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}

	public void removeSkillFromShortCut(final int skillId)
	{
		_shortCuts.deleteShortCutBySkillId(skillId);
	}

	@Override
	public boolean isSitting()
	{
		return inObserverMode() || _isSitting;
	}

	public void setSitting(boolean val)
	{
		_isSitting = val;
	}

	public boolean getSittingTask()
	{
		return sittingTaskLaunched;
	}

	@Override
	public void sitDown()
	{
		if(isSitting() || sittingTaskLaunched || isAlikeDead())
			return;

		if(isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isMoving)
		{
			getAI().setNextAction(nextAction.REST, null, null, false, false);
			return;
		}

		resetWaitSitTime();
		getAI().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
		sittingTaskLaunched = true;
		_isSitting = true;
		ThreadPoolManager.getInstance().scheduleAi(new EndSitDownTask(this), 2500, true);
	}

	@Override
	public void standUp()
	{
		if(_isSitting && !sittingTaskLaunched && !isInStoreMode() && !isAlikeDead())
		{
			if(_relax)
			{
				setRelax(false);
				getEffectList().stopAllSkillEffects(EffectType.Relax);
			}
			getAI().clearNextAction();
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			sittingTaskLaunched = true;
			_isSitting = true;
			ThreadPoolManager.getInstance().scheduleAi(new EndStandUpTask(this), 2500, true);
		}
	}

	public void setRelax(final boolean val)
	{
		_relax = val;
	}

	public void updateWaitSitTime()
	{
		if(_waitTimeWhenSit < 200)
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

	public Warehouse getFreight()
	{
		return _freight;
	}

	public long getAdena()
	{
		return getInventory().getAdena();
	}

	/**
	 * Забирает адену у игрока.<BR><BR>
	 * @param adena - сколько адены забрать
	 * @param notify - отображать системное сообщение
	 * @return L2ItemInstance - остаток адены
	 */
	public L2ItemInstance reduceAdena(long adena, boolean notify)
	{
		if(notify && adena > 0)
			sendPacket(new SystemMessage(SystemMessage.S1_ADENA_DISAPPEARED).addNumber(adena));
		return getInventory().reduceAdena(adena);
	}

	/**
	 * Добавляет адену игроку.<BR><BR>
	 * @param adena - сколько адены дать
	 * @return L2ItemInstance - новое количество адены
	 * TODO добавить параметр update как в reduceAdena
	 */
	public L2ItemInstance addAdena(final long adena)
	{
		return getInventory().addAdena(adena);
	}

	public L2GameClient getNetConnection()
	{
		return _connection;
	}

	public int getRevision()
	{
		return _connection == null ? 0 : _connection.getRevision();
	}

	public void setNetConnection(final L2GameClient connection)
	{
		_connection = connection;
		_isConnected = connection != null && connection.isConnected();
	}

	public void closeNetConnection()
	{
		if(_connection != null)
			_connection.closeNow(false);
	}

	@Override
	public void onAction(final L2Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;

		// Check if the other player already target this L2Player
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() != this)
				sendActionFailed();
		}
		else if(getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			if(getDistance(player) > INTERACTION_DISTANCE && getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
			{
				if(!shift)
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			}
			else
				player.doInteract(this);
		}
		else if(isAutoAttackable(player))
		{
			// Player with lvl < 21 can't attack a cursed weapon holder, and a cursed weapon holder can't attack players with lvl < 21 or Cursed player is in Peace zone.
			if(isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && getLevel() < 21 || player.isCursedWeaponEquipped() && isInZonePeace())
				player.sendActionFailed();
			else
				player.getAI().Attack(this, false, shift);
		}
		else if(player != this && !shift)
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
		else
			sendActionFailed();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		// Send the Server->Client packet StatusUpdate with current HP and MP to all L2Player that must be informed of HP/MP updates of this L2Player
		if(Config.FORCE_STATUSUPDATE)
			super.broadcastStatusUpdate();
		else if(!needStatusUpdate()) //По идее еше должно срезать траффик. Будут глюки с отображением - убрать это условие.
			return;

		sendStatusUpdate(false, StatusUpdate.CUR_HP, StatusUpdate.CUR_MP, StatusUpdate.CUR_CP);

		// Check if a party is in progress
		if(isInParty())
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2Player of the Party
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));

		if(getDuel() != null)
			getDuel().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));

		if(isInOlympiadMode() && isOlympiadCompStart())
		{
			OlympiadGame game = Olympiad.getOlympiadGame(getOlympiadGameId());
			if(game != null)
				game.broadcastInfo(this, null, false);
		}
	}

	public Future<?> _broadcastCharInfoTask;

	@Override
	public void broadcastUserInfo(boolean force)
	{
		sendUserInfo(force);

		if(isInvisible())
			return;

		if(Config.BROADCAST_CHAR_INFO_INTERVAL == 0)
			force = true;

		if(force)
		{
			broadcastCharInfo();
			if(_broadcastCharInfoTask != null)
			{
				_broadcastCharInfoTask.cancel(false);
				_broadcastCharInfoTask = null;
			}
			return;
		}

		if(_broadcastCharInfoTask != null)
			return;

		_broadcastCharInfoTask = ThreadPoolManager.getInstance().scheduleAi(new BroadcastCharInfoTask(this), Config.BROADCAST_CHAR_INFO_INTERVAL, true);
	}

	public L2GameServerPacket newCharInfo()
	{
		if(!isPolymorphed())
			return new CharInfo(this);
		else if(getPolytype() == L2Object.POLY_NPC)
			return new NpcInfoPoly(this);
		else
			return new SpawnItemPoly(this);
	}

	public void broadcastCharInfo()
	{
		if(isInvisible())
			return;

		L2GameServerPacket ci = newCharInfo();
		for(L2Player player : L2World.getAroundPlayers(this))
			if(player != null && player != this)
				player.sendPacket(ci);
	}

	public void broadcastRelationChanged()
	{
		if(isInvisible() || isInOfflineMode())
			return;

		for(L2Player player : L2World.getAroundPlayers(this))
			if(player != null && _objectId != player.getObjectId())
				player.sendPackets(RelationChanged.update(player, this, player));
	}

	public Future<?> _userInfoTask;
	public boolean entering = true;

	public void sendUserInfo(boolean force)
	{
		if(entering || isLogoutStarted())
			return;

		if(Config.USER_INFO_INTERVAL == 0 || force)
		{
			sendPacket(new UserInfo(this), new ExBrExtraUserInfo(this));
			if(_userInfoTask != null)
			{
				_userInfoTask.cancel(false);
				_userInfoTask = null;
			}
			return;
		}

		if(_userInfoTask != null)
			return;

		_userInfoTask = ThreadPoolManager.getInstance().scheduleAi(new UserInfoTask(this), Config.USER_INFO_INTERVAL, true);
	}

	@Override
	public StatusUpdate makeStatusUpdate(int... fields)
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		for(int field : fields)
			switch(field)
			{
				case StatusUpdate.CUR_HP:
					su.addAttribute(field, (int) getCurrentHp());
					break;
				case StatusUpdate.MAX_HP:
					su.addAttribute(field, getMaxHp());
					break;
				case StatusUpdate.CUR_MP:
					su.addAttribute(field, (int) getCurrentMp());
					break;
				case StatusUpdate.MAX_MP:
					su.addAttribute(field, getMaxMp());
					break;
				case StatusUpdate.CUR_LOAD:
					su.addAttribute(field, getCurrentLoad());
					break;
				case StatusUpdate.MAX_LOAD:
					su.addAttribute(field, getMaxLoad());
					break;
				case StatusUpdate.PVP_FLAG:
					su.addAttribute(field, _pvpFlag);
					break;
				case StatusUpdate.KARMA:
					su.addAttribute(field, getKarma());
					break;
				case StatusUpdate.CUR_CP:
					su.addAttribute(field, (int) getCurrentCp());
					break;
				case StatusUpdate.MAX_CP:
					su.addAttribute(field, getMaxCp());
					break;
				default:
					System.out.println("unknown StatusUpdate field: " + field);
					Thread.dumpStack();
					break;
			}
		return su;
	}

	public void sendStatusUpdate(boolean broadCast, int... fields)
	{
		if(fields.length == 0 || entering && !broadCast)
			return;

		StatusUpdate su = makeStatusUpdate(fields);
		if(!su.hasAttributes())
			return;

		if(!broadCast)
			sendPacket(su);
		else if(entering)
			broadcastPacketToOthers(su);
		else
			broadcastPacket(su);
	}

	/**
	 * @return the Alliance Identifier of the L2Player.<BR><BR>
	 */
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}

	@Override
	public int getAllyCrestId()
	{
		return getAlliance() == null ? 0 : getAlliance().getAllyCrestId();
	}

	public HashMap<String, Integer> packetsStat = null;
	public boolean packetsCount = false;
	

	protected synchronized void sendPacketStatsUpdate(final L2GameServerPacket... packets)
	{
		if(packetsStat == null)
			packetsStat = new HashMap<String, Integer>();

		String className;
		Integer count;
		for(L2GameServerPacket packet : packets)
		{
			className = packet.getClass().getSimpleName();
			count = packetsStat.get(className);
			if(count == null)
				count = 1;
			else
				count++;
			packetsStat.put(className, count);
		}
	}

	protected synchronized void sendPacketsStatsUpdate(final Collection<L2GameServerPacket> packets)
	{
		if(packetsStat == null)
			packetsStat = new HashMap<String, Integer>();

		String className;
		Integer count;
		for(SendablePacket<?> packet : packets)
		{
			className = packet.getClass().getSimpleName();
			count = packetsStat.get(className);
			if(count == null)
				count = 1;
			else
				count++;
			packetsStat.put(className, count);
		}
	}

	/**
	 * Send a Server->Client packet StatusUpdate to the L2Player.<BR><BR>
	 */
	@Override
	public void sendPacket(final L2GameServerPacket... packets)
	{
		if(_isConnected && packets.length != 0)
			try
			{
				if(_connection != null)
					_connection.sendPacket(packets);

				if(packetsCount && isGM())
					sendPacketStatsUpdate(packets);
			}
			catch(final Exception e)
			{
				_log.log(Level.INFO, "", e);
				e.printStackTrace();
			}
	}

	public void sendPackets(final Collection<L2GameServerPacket> packets)
	{
		if(_isConnected && packets != null && packets.size() > 0)
			try
			{
				if(_connection != null)
					_connection.sendPackets(packets);

				if(packetsCount && isGM())
					sendPacketsStatsUpdate(packets);
			}
			catch(final Exception e)
			{
				_log.log(Level.INFO, "", e);
				e.printStackTrace();
			}
	}

	public void doInteract(L2Object target)
	{
		if(target == null || isOutOfControl())
		{
			sendActionFailed();
			return;
		}
		if(target.isPlayer())
		{
			if(target.getDistance(this) <= INTERACTION_DISTANCE)
			{
				L2Player temp = (L2Player) target;

				if(temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
				{
					sendPacket(new PrivateStoreListSell(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				{
					sendPacket(new PrivateStoreListBuy(this, temp));
					sendActionFailed();
				}
				else if(temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				{
					sendPacket(new RecipeShopSellList(this, temp));
					sendActionFailed();
				}
				sendActionFailed();
			}
			else if(getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
		}
		else
			target.onAction(this, false);
	}

	public void doAutoLootOrDrop(L2ItemInstance item, L2NpcInstance fromNpc)
	{
		boolean forceAutoloot = fromNpc.isFlying() || getReflection().isAutolootForced();

		if (item.getItem().isArrow() || item.getName().equalsIgnoreCase("Arrow"))
			return; //TODO удалить из базы и ето снести
		
		if((fromNpc.isRaid() || fromNpc instanceof L2ReflectionBossInstance) && !Config.AUTO_LOOT_FROM_RAIDS && !item.isHerb() && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}

		// Herbs
		if(item.isHerb())
		{
			if(!AutoLootHerbs && !forceAutoloot)
			{
				item.dropToTheGround(this, fromNpc);
				return;
			}
			L2Skill[] skills = item.getItem().getAttachedSkills();
			if(skills != null && skills.length > 0)
				for(L2Skill skill : skills)
				{
					altUseSkill(skill, this);
					if(getPet() != null && getPet().isSummon() && !getPet().isDead() && (item.getItemId() <= 8605 || item.getItemId() == 8614))
						getPet().altUseSkill(skill, getPet());
				}
			item.deleteMe();
			broadcastPacket(new GetItem(item, getObjectId()));
			return;
		}

		if(!AutoLoot && !forceAutoloot)
		{
			item.dropToTheGround(this, fromNpc);
			return;
		}
		
		// Проверка наличия ПА для автолута
		if (Config.AUTO_LOOT_PA)
		{
			if (!(_bonusExpiration !=null)) //не правильная проверка для ПА TODO
			{
				item.dropToTheGround(this, fromNpc);
				sendMessage("Need bay Premium Account");
				return;
			}
		}
		// Check if the L2Player is in a Party
		if(!isInParty())
		{
			if(!getInventory().validateWeight(item))
			{
				sendActionFailed();
				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				item.dropToTheGround(this, fromNpc);
				return;
			}

			if(!getInventory().validateCapacity(item))
			{
				sendActionFailed();
				sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				item.dropToTheGround(this, fromNpc);
				return;
			}

			// Send a System Message to the L2Player
			sendPacket(SystemMessage.obtainItems(item));

			// Add the Item to the L2Player inventory
			L2ItemInstance target2 = getInventory().addItem(item);
			Log.LogItem(this, fromNpc, Log.GetItemByAutoLoot, target2);

			sendChanges();
		}
		else if(item.getItemId() == 57)
			// Distribute Adena between Party members
			getParty().distributeAdena(item, fromNpc, this);
		else
			// Distribute Item between Party members
			getParty().distributeItem(this, item, fromNpc);

		broadcastPickUpMsg(item);
	}

	@Override
	public void doPickupItem(final L2Object object)
	{
		// Check if the L2Object to pick up is a L2ItemInstance
		if(!object.isItem())
		{
			_log.warning("trying to pickup wrong target." + getTarget());
			return;
		}

		sendActionFailed();
		stopMove();

		L2ItemInstance item = (L2ItemInstance) object;

		if(item.getItem().isCombatFlag() && !FortressSiegeManager.checkIfCanPickup(this))
			return;

		synchronized (item)
		{
			// Check if me not owner of item and, if in party, not in owner party and nonowner pickup delay still active
			if(item.getDropTimeOwner() != 0 && item.getItemDropOwner() != null && item.getDropTimeOwner() > System.currentTimeMillis() && this != item.getItemDropOwner() && (!isInParty() || isInParty() && item.getItemDropOwner().isInParty() && getParty() != item.getItemDropOwner().getParty()))
			{
				SystemMessage sm;
				if(item.getItemId() == 57)
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				return;
			}

			if(!item.isVisible())
				return;

			// Herbs
			if(item.isHerb())
			{
				L2Skill[] skills = item.getItem().getAttachedSkills();
				if(skills != null && skills.length > 0)
					for(L2Skill skill : skills)
					{
						altUseSkill(skill, this);
						if(getPet() != null && getPet().isSummon() && !getPet().isDead() && (item.getItemId() <= 8605 || item.getItemId() == 8614))
							getPet().altUseSkill(skill, getPet());
					}
				item.deleteMe();
				broadcastPacket(new GetItem(item, getObjectId()));
				return;
			}

			boolean equip = (item.getCustomFlags() & L2ItemInstance.FLAG_EQUIP_ON_PICKUP) == L2ItemInstance.FLAG_EQUIP_ON_PICKUP;

			if(!isInParty() || equip)
			{
				if(!getInventory().validateWeight(item))
				{
					sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
					return;
				}

				if(!getInventory().validateCapacity(item))
				{
					sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
					return;
				}

				if(!item.pickupMe(this))
					return;

				sendPacket(SystemMessage.obtainItems(item));

				Log.LogItem(this, Log.PickupItem, getInventory().addItem(item));

				if(equip)
					getInventory().equipItem(item, true);

				sendChanges();
			}
			else if(item.getItemId() == 57)
			{
				if(!item.pickupMe(this))
					return;
				getParty().distributeAdena(item, this);
			}
			else
			{
				// Нужно обязательно сначало удалить предмет с земли.
				if(!item.pickupMe(null))
					return;
				getParty().distributeItem(this, item);
			}

			broadcastPacket(new GetItem(item, getObjectId()));
			broadcastPickUpMsg(item);
		}
	}

	@Override
	public void setTarget(L2Object newTarget)
	{
		// Check if the new target is visible
		if(newTarget != null && !newTarget.isVisible())
			newTarget = null;

		L2Party party = getParty();

		L2Object oldTarget = getTarget();

		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
				return;

			// Remove the L2Player from the _statusListener of the old target if it was a L2Character
			if(oldTarget.isCharacter())
				((L2Character) oldTarget).removeStatusListener(this);

			broadcastPacket(new TargetUnselected(this));
		}

		if(newTarget != null)
		{
			// Add the L2Player to the _statusListener of the new target if it's a L2Character
			if(newTarget.isCharacter())
			{
				((L2Character) newTarget).addStatusListener(this);				
			}			
			
			sendPacket(new MyTargetSelected(newTarget.getObjectId(), 0));			
			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getLoc()));
			if(newTarget.isCharacter())
				sendPacket(new ExAbnormalStatusUpdateFromTargetPacket((L2Character)newTarget));
		}

		super.setTarget(newTarget);
	}

	/**
	 * @return the active weapon instance (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * @return the active weapon item (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		final L2ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		return (L2Weapon) weapon.getItem();
	}

	/**
	 * @return the secondary weapon instance (always equipped in the left hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * @return the secondary weapon item (always equipped in the left hand) or the fists weapon.<BR><BR>
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		final L2ItemInstance weapon = getSecondaryWeaponInstance();

		if(weapon == null)
			return getFistsWeaponItem();

		final L2Item item = weapon.getItem();

		if(item instanceof L2Weapon)
			return (L2Weapon) item;

		return null;
	}

	public boolean isWearingArmor(final ArmorType armorType)
	{
		final L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);

		if(chest == null)
			return armorType == ArmorType.NONE;

		if(chest.getItemType() != armorType)
			return false;

		if(chest.getBodyPart() == L2Item.SLOT_FULL_ARMOR)
			return true;

		final L2ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);

		return legs == null ? armorType == ArmorType.NONE : legs.getItemType() == armorType;
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		if(attacker == null || isDead() || attacker.isDead())
			return;

		if(isInvul() && attacker != this)
		{
			attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		if(isInOfflineMode() && attacker.getPlayer() != null && !attacker.getPlayer().isGM())
		{
			attacker.sendPacket(Msg.INVALID_TARGET);
			return;
		}

		if(this != attacker && isInOlympiadMode() && !isOlympiadCompStart())
		{
			attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
			return;
		}

		if(attacker.isPlayable() && isInZoneBattle() != attacker.isInZoneBattle())
		{
			attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
			return;
		}

		double trans = calcStat(Stats.TRANSFER_PET_DAMAGE_PERCENT, 0, attacker, skill);
		if(trans >= 1)
			if(_summon == null || _summon.isDead())
			{
				getEffectList().stopEffect(L2Skill.SKILL_TRANSFER_PAIN);
				getEffectList().stopEffect(711); // Divine Summoner Transfer Pain
				getEffectList().stopEffect(3667); // Yellow Talisman - Damage Transition
			}
			else if(_summon.isInRange(this, 1200))
			{
				trans *= i * .01;
				i -= trans;
				_summon.reduceCurrentHp(trans, attacker, null, false, false, false, false);
			}

		if(this != attacker && canReflect)
		{
			L2Effect transferDam = getEffectList().getEffectByType(EffectType.TransferDam);
			if(transferDam != null)
			{
				L2Character effector = transferDam.getEffector();
				if(effector == null || effector.isDead())
					getEffectList().stopEffects(EffectType.TransferDam);
				else if(effector.isInRange(this, 1200))
				{
					trans = transferDam.calc();
					trans *= i * .01;
					i -= trans;
					effector.reduceCurrentHp(trans, attacker, null, false, false, false, false);
				}
			}
		}

		if(attacker != this)
			sendPacket(new SystemMessage(SystemMessage.C1_HAS_RECEIVED_DAMAGE_OF_S3_FROM_C2).addName(this).addName(attacker).addNumber((long) i));

		double hp = directHp ? getCurrentHp() : getCurrentHp() + getCurrentCp();

		if(getDuel() != null)
			if(getDuel() != attacker.getDuel())
				getDuel().setDuelState(getStoredId(), DuelState.Interrupted);
			else if(getDuel().getDuelState(getStoredId()) == DuelState.Interrupted)
			{
				attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
				return;
			}
			else if(i >= hp)
			{
				setCurrentHp(1, false);
				getDuel().onPlayerDefeat(this);
				getDuel().stopFighting(attacker.getPlayer());
				return;
			}

		if(isInOlympiadMode())
		{
			OlympiadGame olymp_game = Olympiad.getOlympiadGame(getOlympiadGameId());
			if(olymp_game != null)
			{
				if(olymp_game.getState() <= 0)
				{
					attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
					return;
				}

				if(this != attacker)
					olymp_game.addDamage(this, Math.min(hp, i));

				if(i >= hp)
					if(olymp_game.getType() != CompType.TEAM && olymp_game.getType() != CompType.TEAM_RANDOM)
					{
						olymp_game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
						olymp_game.endGame(20000, false);
						setCurrentHp(1, false);
						attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						attacker.sendActionFailed();
						return;
					}
					else if(olymp_game.doDie(this)) // Все умерли
					{
						olymp_game.setWinner(getOlympiadSide() == 1 ? 2 : 1);
						olymp_game.endGame(20000, false);
					}
			}
			else
			{
				_log.warning("OlympiadGame id = " + getOlympiadGameId() + " is null");
				Thread.dumpStack();
			}
		}

		// Reduce the current HP of the L2Player
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect);

		//TODO: переделать на листенер
		if(getLevel() < 6 && getCurrentHpPercents() < 25)
		{
			Quest q = QuestManager.getQuest(255);
			if(q != null)
				processQuestEvent(q.getName(), "CE45", null);
		}
	}

	public final boolean atWarWith(final L2Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId());
	}

	public boolean atMutualWarWith(L2Player player)
	{
		return _clan != null && player.getClan() != null && getPledgeType() != -1 && player.getPledgeType() != -1 && _clan.isAtWarWith(player.getClan().getClanId()) && player.getClan().isAtWarWith(_clan.getClanId());
	}

	public final void doPurePk(final L2Player killer)
	{
		// Check if the attacker has a PK counter greater than 0
		final int pkCountMulti = Math.max(killer.getPkKills() / 2, 1);

		// Calculate the level difference Multiplier between attacker and killed L2Player
		//final int lvlDiffMulti = Math.max(killer.getLevel() / _level, 1);

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		// Add karma to attacker and increase its PK counter
		if (killer.getPkKills() != 0)
			killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti); // * lvlDiffMulti);
		else
			killer.increaseKarma(720);
		killer.setPkKills(killer.getPkKills() + 1);
	}

	public final void doKillInPeace(final L2Player killer) // Check if the L2Player killed haven't Karma
	{
		if(_karma >= 0)
			doPurePk(killer);
		else
			killer.setPvpKills(killer.getPvpKills() + 1);
	}

	public void checkAddItemToDrop(GArray<L2ItemInstance> array, GArray<L2ItemInstance> items, int maxCount)
	{
		for(int i = 0; i < maxCount && !items.isEmpty(); i++)
			array.add(items.remove(Rnd.get(items.size())));
	}

	protected void doPKPVPManage(L2Character killer)
	{
		if(isCombatFlagEquipped())
		{
			L2ItemInstance flag = getActiveWeaponInstance();
			if(flag != null)
			{
				int customFlags = flag.getCustomFlags();
				flag.setCustomFlags(0, false);
				flag = getInventory().dropItem(flag, flag.getCount(), true);
				flag.setCustomFlags(customFlags, false);
				flag.dropMe(this, getLoc().rnd(0, 100, false));
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(flag.getItemId()));
			}
		}

		if(isTerritoryFlagEquipped())
		{
			L2ItemInstance flag = getActiveWeaponInstance();
			if(flag != null && flag.getCustomType1() != 77) // 77 это эвентовый флаг
			{
				L2TerritoryFlagInstance flagNpc = TerritorySiege.getNpcFlagByItemId(flag.getItemId());
				flagNpc.drop(this);

				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(flag.getItemId()));
				String terrName = CastleManager.getInstance().getCastleByIndex(flagNpc.getBaseTerritoryId()).getName();
				TerritorySiege.announceToPlayer(new SystemMessage(SystemMessage.THE_CHARACTER_THAT_ACQUIRED_S1_WARD_HAS_BEEN_KILLED).addString(terrName), true);
			}
		}

		for(L2ItemInstance item : getInventory().getItemsList())
			if((item.getCustomFlags() & L2ItemInstance.FLAG_ALWAYS_DROP_ON_DIE) == L2ItemInstance.FLAG_ALWAYS_DROP_ON_DIE)
			{
				item = getInventory().dropItem(item, item.getCount(), false);
				item.dropMe(this, getLoc().rnd(0, 100, false));
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));
			}

		if(killer == null || killer == _summon)
			return;

		if(killer.getObjectId() == _objectId)
			return;

		if(isInZoneBattle() || killer.isInZoneBattle())
			return;

		if(killer instanceof L2Summon && (killer = killer.getPlayer()) == null)
			return;

		// Processing Karma/PKCount/PvPCount for killer
		if(killer.isPlayer())
		{
			final L2Player pk = (L2Player) killer;
			final int repValue = getLevel() - pk.getLevel() >= 20 ? 2 : 1;
			boolean war = atMutualWarWith(pk);

			if(getLevel() > 4 && _clan != null && pk.getClan() != null)
				if(war || _clan.getSiege() != null && _clan.getSiege() == pk.getClan().getSiege() && (_clan.isDefender() && pk.getClan().isAttacker() || _clan.isAttacker() && pk.getClan().isDefender()))
					if(pk.getClan().getReputationScore() > 0 && _clan.getLevel() >= 5 && _clan.getReputationScore() > 0 && pk.getClan().getLevel() >= 5)
					{
						_clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENT_CLAN_REPUTATION_SCORE).addString(getName()).addNumber(-_clan.incReputation(-repValue, true, "ClanWar")), this);
						pk.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE).addNumber(pk.getClan().incReputation(repValue, true, "ClanWar")), pk);
					}

			if(isInZone(Siege))
			{
				if(pk.getTerritorySiege() > -1 && getTerritorySiege() > -1 && pk.getTerritorySiege() != getTerritorySiege() && pk.getLevel() - getLevel() < 10 && pk.getLevel() > 61 && getLevel() > 61)
				{
					if(getClanId() > 0 && getClanId() != pk.getClanId() || getAllyId() > 0 && getAllyId() != pk.getAllyId())
						return;
					String var = pk.getVar("badges" + pk.getTerritorySiege());
					int badges = var == null ? 0 : Integer.parseInt(var);
					if(!isInParty())
						badges += Rnd.get(0, 1);
					else if(getParty() != pk.getParty())
						badges += Rnd.get(0, 2);
					pk.setVar("badges" + pk.getTerritorySiege(), "" + badges);
				}
				return;
			}

			if(_pvpFlag > 0 || war)
				pk.setPvpKills(pk.getPvpKills() + 1);
			else
				doKillInPeace(pk);

			// Send a Server->Client UserInfo packet to attacker with its PvP Kills Counter
			pk.sendUserInfo(false);
		}

		int karma = _karma;
		if (_karma < 0) {
			decreaseKarma(Config.KARMA_LOST_BASE);
			if (_karma > 0)
				_karma = 0;
		}

		// в нормальных условиях вещи теряются только при смерти от гварда или игрока
		// кроме того, альт на потерю вещей при сметри позволяет терять вещи при смтери от монстра
		boolean isPvP = killer.isPlayable() || killer instanceof L2GuardInstance;

		
		if (killer.isMonster() && !Config.DROP_ITEMS_ON_DIE || isPvP && (_pkKills < 31 && _karma >= 0) || !killer.isMonster() && !isPvP)
				return;

		// No drop from GM's
		if(!Config.KARMA_DROP_GM && isGM())
			return;

		// нечего терять
		if(getInventory().getItemsList().isEmpty())
			return;
		
		if (_karma >= 0)
			return;

		final int max_drop_count = isPvP ? Config.KARMA_DROP_ITEM_LIMIT : 1;

		double dropRate; // базовый шанс в процентах
		if(isPvP)
			dropRate = (_pkKills * Config.KARMA_DROPCHANCE_MOD + Config.KARMA_DROPCHANCE_BASE);
		else
			dropRate = Config.NORMAL_DROPCHANCE_BASE;

		int dropEquipCount = 0, dropWeaponCount = 0, dropItemCount = 0;

		for(int i = 0; i < Math.ceil(dropRate / 100) && i < max_drop_count; i++)
			if(Rnd.chance(dropRate))
			{
				int rand = Rnd.get(Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT + Config.DROPCHANCE_ITEM) + 1;
				if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON + Config.DROPCHANCE_EQUIPMENT)
					dropItemCount++;
				else if(rand > Config.DROPCHANCE_EQUIPPED_WEAPON)
					dropEquipCount++;
				else
					dropWeaponCount++;
			}

		GArray<L2ItemInstance> dropped_items = new GArray<L2ItemInstance>(), // общий массив с результатами выбора 
		dropItem = new GArray<L2ItemInstance>(), dropEquip = new GArray<L2ItemInstance>(), dropWeapon = new GArray<L2ItemInstance>(); // временные

		for(L2ItemInstance item : getInventory().getItems())
		{
			if(!item.canBeDropped(this, true) || Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
				continue;

			if(item.getItem().getType2() == L2Item.TYPE2_WEAPON)
				dropWeapon.add(item);
			else if(item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)
				dropEquip.add(item);
			else if(item.getItem().getType2() == L2Item.TYPE2_OTHER)
				dropItem.add(item);
		}

		checkAddItemToDrop(dropped_items, dropWeapon, dropWeaponCount);
		checkAddItemToDrop(dropped_items, dropEquip, dropEquipCount);
		checkAddItemToDrop(dropped_items, dropItem, dropItemCount);

		// Dropping items, if present
		if(dropped_items.isEmpty())
			return;

		for(L2ItemInstance item : dropped_items)
		{
			if(item.isEquipped())
				getInventory().unEquipItemInSlot(item.getEquipSlot());

			if(item.isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
				item.removeAugmentation();

			item = getInventory().dropItem(item, item.getCount(), false);

			if(Config.MOBSLOOTERS && killer.isMonster() && !item.isCursed() && _reflection <= 0)
				if(killer.isMinion() && !((L2MinionInstance) killer).getLeader().isDead())
					((L2MinionInstance) killer).getLeader().giveItem(item, true);
				else
					((L2MonsterInstance) killer).giveItem(item, true);
			else if(killer.isPlayer() && Config.AUTO_LOOT && Config.AUTO_LOOT_PK)
				((L2Player) killer).getInventory().addItem(item);
			else if(killer.isSummon() && Config.AUTO_LOOT && Config.AUTO_LOOT_PK)
				killer.getPlayer().getInventory().addItem(item);
			else
			{
				item = getInventory().dropItem(item, item.getCount(), false);
				item.dropMe(this, getLoc().rnd(0, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT, false));
			}

			if(item.getEnchantLevel() > 0)
				sendPacket(new SystemMessage(SystemMessage.DROPPED__S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
			else
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_DROPPED_S1).addItemName(item.getItemId()));
		}
		refreshOverloaded();		
	}

	@Override
	public void doDie(L2Character killer)
	{
		dieLock.lock();
		try
		{
			if(_killedAlreadyPlayer)
				return;
			_killedAlreadyPlayer = true;
		}
		finally
		{
			dieLock.unlock();
		}

		//Check for active charm of luck for death penalty
		getDeathPenalty().checkCharmOfLuck();

		L2TradeList tl = getTradeList();
		if(tl != null)
		{
			tl.removeAll();
			setTradeList(null);
		}

		if(isInTransaction())
		{
			if(getTransaction().isTypeOf(TransactionType.TRADE))
				sendPacket(new SendTradeDone(0));
			getTransaction().cancel();
		}

		setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);

		// Kill the L2Player
		super.doDie(killer);

		// Dont unsummon a summon, it can kill few enemies. But pet must returned back into its item
		// Unsummon siege summons
		if(_summon != null && (_summon.isPet() || _summon.isSiegeWeapon()))
			_summon.unSummon();

		// Unsummon Cubics and agathion
		if(!isBlessedByNoblesse() && !isSalvation())
			if(_cubics != null)
				for(L2CubicInstance cubic : _cubics)
					cubic.deleteMe(false);

		setAgathion(0);

		if(Config.LOG_KILLS)
		{
			String coords = " at (" + getX() + "," + getY() + "," + getZ() + ")";
			if(killer.isNpc())
				Log.add("" + this + " karma " + _karma + " killed by mob " + killer.getNpcId() + coords, "kills");
			else if(killer instanceof L2Summon && killer.getPlayer() != null)
				Log.add("" + this + " karma " + _karma + " killed by summon of " + killer.getPlayer() + coords, "kills");
			else
				Log.add("" + this + " karma " + _karma + " killed by " + killer + coords, "kills");
		}

		if(Config.ALLOW_CURSED_WEAPONS)
			if(isCursedWeaponEquipped())
			{
				_pvpFlag = 0;

				CursedWeaponsManager.getInstance().dropPlayer(this);
				return;
			}
			else if(killer.isPlayer() && killer.isCursedWeaponEquipped())
			{
				_pvpFlag = 0;

				//noinspection ConstantConditions
				CursedWeaponsManager.getInstance().increaseKills(((L2Player) killer).getCursedWeaponEquippedId());
				return;
			}
		
		if (getKarma() < 0)
			if(killer.isPlayer())
			{
				final L2Player pk = (L2Player) killer;
				pk.decreaseKarma(360);
			}
			

		doPKPVPManage(killer);

		// Set the PvP Flag of the L2Player
		_pvpFlag = 0;

		//And in the end of process notify death penalty that owner died :)
		getDeathPenalty().notifyDead(killer);

		setIncreasedForce(0);

		stopWaterTask();

		if(!isSalvation() && isInZone(Siege) && isCharmOfCourage())
		{
			_reviveRequested = true;
			_revivePower = 100;
			sendPacket(new ConfirmDlg(SystemMessage.RESURRECTION_IS_POSSIBLE_BECAUSE_OF_THE_COURAGE_CHARM_S_EFFECT_WOULD_YOU_LIKE_TO_RESURRECT_NOW, 60000, 2));
			setCharmOfCourage(false);
		}

		if(getLevel() < 6)
		{
			Quest q = QuestManager.getQuest(255);
			if(q != null)
				processQuestEvent(q.getName(), "CE30", null);
		}
	}

	public void restoreExp()
	{
		restoreExp(100.);
	}

	public void restoreExp(double percent)
	{
		if(percent == 0)
			return;

		int lostexp = 0;

		String lostexps = getVar("lostexp");
		if(lostexps != null)
		{
			lostexp = Integer.parseInt(lostexps);
			unsetVar("lostexp");
		}

		if(lostexp != 0)
			addExpAndSp((long) (lostexp * percent / 100), 0, false, false);
	}

	public void deathPenalty(L2Character killer)
	{
		final boolean atwar = killer.getPlayer() != null ? atWarWith(killer.getPlayer()) : false;

		double deathPenaltyBonus = getDeathPenalty().getLevel() * ConfigSystem.getInt("DeathPenaltyC5RateExpPenalty");
		if(deathPenaltyBonus < 2)
			deathPenaltyBonus = 1;
		else
			deathPenaltyBonus = deathPenaltyBonus / 2;

		// The death steal you some Exp: 10-40 lvl 8% loose
		double percentLost = 8.0;

		byte level = getLevel();
		if(level >= 79)
			percentLost = 1.0;
		else if(level >= 78)
			percentLost = 1.5;
		else if(level >= 76)
			percentLost = 2.0;
		else if(level >= 40)
			percentLost = 4.0;

		if(atwar)
			percentLost = percentLost / 4.0;

		// Calculate the Experience loss
		int lostexp = (int) Math.round((Experience.LEVEL[level + 1] - Experience.LEVEL[level]) * percentLost / 100);
		lostexp *= deathPenaltyBonus;

		lostexp = (int) calcStat(Stats.EXP_LOST, lostexp, killer, null);

		// На зарегистрированной осаде нет потери опыта, на чужой осаде - как при обычной смерти от *моба*
		if(isInZone(Siege))
		{
			Siege siege = SiegeManager.getSiege(this, true);
			if(siege != null && siege.isParticipant(this))
				lostexp = 0;

			if(getTerritorySiege() > -1 && TerritorySiege.checkIfInZone(this))
				lostexp = 0;

			// Battlefield Death Syndrome
			GArray<L2Effect> effect = getEffectList().getEffectsBySkillId(L2Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
			if(effect != null)
			{
				int syndromeLvl = effect.get(0).getSkill().getLevel();
				if(syndromeLvl < 5)
				{
					getEffectList().stopEffect(L2Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
					L2Skill skill = SkillTable.getInstance().getInfo(L2Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, syndromeLvl + 1);
					skill.getEffects(this, this, false, false);
				}
				else if(syndromeLvl == 5)
				{
					getEffectList().stopEffect(L2Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
					L2Skill skill = SkillTable.getInstance().getInfo(L2Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 5);
					skill.getEffects(this, this, false, false);
				}
			}
			else
			{
				L2Skill skill = SkillTable.getInstance().getInfo(L2Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME, 1);
				if(skill != null)
					skill.getEffects(this, this, false, false);
			}
		}
		
		if (killer.getKarma() < 0 && lostexp > 0)
			lostexp = lostexp * 10;
		
		if (getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR) == 8181) 
			lostexp = lostexp - (lostexp / 100) * 50;;
			
		_log.fine(_name + "is dead, so exp to remove:" + lostexp);

		long before = getExp();
		addExpAndSp(-lostexp, 0, false, false);
		long lost = before - getExp();

		if(lost > 0)
			setVar("lostexp", String.valueOf(lost));
	}

	public void setPartyMatchingLevels(int levels)
	{
		_partyMatchingLevels = levels;
	}

	public int getPartyMatchingLevels()
	{
		return _partyMatchingLevels;
	}

	public void setPartyMatchingRegion(int region)
	{
		_partyMatchingRegion = region;
	}

	public int getPartyMatchingRegion()
	{
		return _partyMatchingRegion;
	}

	public Integer getPartyRoom()
	{
		return _partyRoom;
	}

	public void setPartyRoom(Integer partyRoom)
	{
		_partyRoom = partyRoom;
	}

	public void setTransaction(Transaction transaction)
	{
		_transaction = transaction;
	}

	public Transaction getTransaction()
	{
		return _transaction;
	}

	public boolean isInTransaction()
	{
		if(_transaction == null)
			return false;
		if(!_transaction.isInProgress())
			return false;
		return true;
	}

	public GArray<L2GameServerPacket> addVisibleObject(L2Object object, L2Character dropper)
	{
		GArray<L2GameServerPacket> result = new GArray<L2GameServerPacket>();

		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId() || !object.isVisible())
			return result;

		if(object.isTrap() && !((L2TrapInstance) object).isDetected() && ((L2TrapInstance) object).getOwner() != this)
			return result;

		if(object.isPolymorphed())
			switch(object.getPolytype())
			{
				case L2Object.POLY_ITEM:
					result.add(new SpawnItemPoly(object));
					showMoves(result, object);
					return result;
				case L2Object.POLY_NPC:
					result.add(new NpcInfoPoly(object));
					showMoves(result, object);
					return result;
			}

		if(object.isItem())
		{
			if(dropper != null)
				result.add(new DropItem((L2ItemInstance) object, dropper.getObjectId()));
			else
				result.add(new SpawnItem((L2ItemInstance) object));
			return result;
		}

		if(object.isDoor())
		{
			result.add(new StaticObject((L2DoorInstance) object));
			return result;
		}

		if(object instanceof L2StaticObjectInstance)
		{
			result.add(new StaticObject((L2StaticObjectInstance) object));
			return result;
		}

		if(object instanceof L2ClanHallManagerInstance)
			((L2ClanHallManagerInstance) object).sendDecoInfo(this);

		if(object.isNpc())
		{
            L2NpcInstance npc = (L2NpcInstance) object;
			result.add(new NpcInfo(npc, this));
            result.add(new ExChangeNpcState(npc.getObjectId(), npc.getNpcState()));
            showMoves(result, object);

			if(object.getAI() instanceof DefaultAI && !object.getAI().isActive())
				object.getAI().startAITask();

			return result;
		}

		if(object instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) object;
			L2Player owner = summon.getPlayer();

			if(owner == this)
			{
				result.add(new PetInfo(summon, 2));
				result.add(new PartySpelled(summon, true));

				if(summon.isPet())
					result.add(new PetItemList((L2PetInstance) summon));
			}
			else
			{
				L2Party party = getParty();
				if(getReflectionId() == -2 && (owner == null || party == null || party != owner.getParty())) // Чужие петы в GH не показываются для уменьшения лагов.
					return result;
				result.add(new NpcInfo(summon, this, 2));
				if(owner != null && party != null && party == owner.getParty())
					result.add(new PartySpelled(summon, true));
				result.addAll(RelationChanged.update(this, owner, this));
			}

			showMoves(result, object);
			return result;
		}

		if(object.isPlayer())
		{
			final L2Player otherPlayer = (L2Player) object;
			if(otherPlayer.isInvisible() && getObjectId() != otherPlayer.getObjectId())
				return result;

			if(otherPlayer.getPrivateStoreType() != STORE_PRIVATE_NONE && getVarB("notraders"))
				return result;

			if(getObjectId() != otherPlayer.getObjectId())
				result.add(otherPlayer.newCharInfo());

			if(otherPlayer.getPrivateStoreType() != STORE_PRIVATE_NONE)
			{
				if(otherPlayer.getPrivateStoreType() == STORE_PRIVATE_BUY)
					result.add(new PrivateStoreMsgBuy(otherPlayer));
				else if(otherPlayer.getPrivateStoreType() == STORE_PRIVATE_SELL || otherPlayer.getPrivateStoreType() == STORE_PRIVATE_SELL_PACKAGE)
					result.add(new PrivateStoreMsgSell(otherPlayer));
				else if(otherPlayer.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
					result.add(new RecipeShopMsg(otherPlayer));
				if(isInZonePeace()) // Мирным торговцам не нужно посылать больше пакетов, для экономии траффика
					return result;
			}

			if(otherPlayer.isCastingNow())
			{
				L2Character castingTarget = otherPlayer.getCastingTarget();
				L2Skill castingSkill = otherPlayer.getCastingSkill();
				long animationEndTime = otherPlayer.getAnimationEndTime();
				if(castingSkill != null && castingTarget != null && castingTarget.isCharacter() && otherPlayer.getAnimationEndTime() > 0)
					result.add(new MagicSkillUse(otherPlayer, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
			}

			result.addAll(RelationChanged.update(this, otherPlayer, this));

			if(otherPlayer.isInVehicle())
				if(otherPlayer.getVehicle().isAirShip())
					result.add(new ExGetOnAirShip(otherPlayer, (L2AirShip) otherPlayer.getVehicle(), otherPlayer.getInVehiclePosition()));
				else
					result.add(new GetOnVehicle(otherPlayer, (L2Ship) otherPlayer.getVehicle(), otherPlayer.getInVehiclePosition()));
			else
				showMoves(result, object);
			return result;
		}

		if(object.isAirShip())
		{
			L2AirShip boat = (L2AirShip) object;
			result.add(new ExAirShipInfo(boat));
			if(isInVehicle() && getVehicle() == boat)
				result.add(new ExGetOnAirShip(this, boat, getInVehiclePosition()));
			if(boat.isMoving)
				result.add(new ExMoveToLocationAirShip(boat, boat.getLoc(), boat.getDestination()));
		}
		else if(object.isShip())
		{
			L2Ship boat = (L2Ship) object;
			result.add(new VehicleInfo(boat));
			if(isInVehicle() && getVehicle() == boat)
				result.add(new GetOnVehicle(this, boat, getInVehiclePosition()));
			if(boat.isMoving)
				result.add(new VehicleDeparture(boat));
		}

		return result;
	}

	public L2GameServerPacket removeVisibleObject(L2Object object, DeleteObject packet, boolean deactivateAI)
	{
		if(isLogoutStarted() || object == null || object.getObjectId() == getObjectId()) // FIXME  || isTeleporting()
			return null;
		if(isInVehicle() && getVehicle() == object)
			return null;

		if(deactivateAI && object.isNpc())
		{
			L2NpcInstance npc = (L2NpcInstance) object;
			L2WorldRegion region = npc.getCurrentRegion();
			L2CharacterAI ai = npc.getAI();
			if(ai instanceof DefaultAI && ai.isActive() && !ai.isGlobalAI() && (region == null || region.areNeighborsEmpty()))
			{
				npc.setTarget(null);
				npc.stopMove();
				npc.getAI().stopAITask();
			}
		}

		L2GameServerPacket result = (packet == null ? new DeleteObject(object) : packet);

		//if(object.isNpc)
		//	removeFromHatelist((L2NpcInstance) object);
		getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		return result;
	}

	private void showMoves(GArray<L2GameServerPacket> result, L2Object object)
	{
		if(object != null && object.isCharacter())
		{
			L2Character obj = (L2Character) object;
			if(obj.isMoving || obj.isFollow)
				result.add(new CharMoveToLocation(obj));
		}
	}

	private boolean increaseLevel()
	{
		if(_activeClass == null || !_activeClass.incLevel())
			return false;

		sendPacket(Msg.YOU_HAVE_INCREASED_YOUR_LEVEL);
		broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

		setCurrentHpMp(getMaxHp(), getMaxMp());
		setCurrentCp(getMaxCp());

		// Recalculate the party level
		if(isInParty())
			getParty().recalculatePartyData();

		if(_clan != null)
		{
			PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for(L2Player clanMember : _clan.getOnlineMembers(0))
				clanMember.sendPacket(memberUpdate);
		}

		// Give Expertise skill of this level
		rewardSkills();

		Quest q = QuestManager.getQuest(255);
		if(q != null)
			processQuestEvent(q.getName(), "CE40", null);		
		
		if (getLevel() > 84 && !isAwaking())
			AwakingManager.getInstance().SendReqToStartQuest(this);
		
		SkillTreeTable.getNewSkills(this);
		return true;
	}

	private boolean decreaseLevel()
	{
		if(_activeClass == null || !_activeClass.decLevel())
			return false;

		// Recalculate the party level
		if(isInParty())
			getParty().recalculatePartyData();

		if(_clan != null)
		{
			PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
			for(L2Player clanMember : _clan.getOnlineMembers(getObjectId()))
				if(!clanMember.equals(this))
					clanMember.sendPacket(memberUpdate);
		}

		if(ConfigSystem.getBoolean("AltRemoveSkillsOnDelevel"))
			checkSkills(10);
		// Give Expertise skill of this level
		rewardSkills();
		return true;
	}

	/**
	 * Удаляет все скиллы, которые учатся на уровне большем, чем текущий+maxDiff
	 */
	public void checkSkills(int maxDiff)
	{
		for(L2Skill sk : getAllSkills())
			if(SkillTreeTable.getMinSkillLevel(sk.getId(), getClassId(), sk.getLevel()) > getLevel() + maxDiff)
			{
				int id = sk.getId();
				int level = sk.getLevel();
				removeSkill(sk, true);
				if(level > 1)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(id, level - 1);
					addSkill(skill, true);
				}
			}
	}

	public void stopAllTimers()
	{
		if(_cubics != null)
			for(L2CubicInstance cubic : _cubics)
				cubic.deleteMe(false);
		setAgathion(0);
		stopWaterTask();
		stopBonusTask();
		stopKickTask();
	}

	@Override
	public L2Summon getPet()
	{
		return _summon;
	}

	public void setPet(L2Summon summon)
	{
		_summon = summon;
		AutoShot();
		if(summon == null)
			getEffectList().stopEffect(4140);
	}

	/**
	 * Удалит персонажа из мира через указанное время, если на момент истечения времени он не будет присоединен.
	 * 
	 * TODO: через минуту делать его неуязвимым.
	 * TODO: сделать привязку времени к контексту, для зон с лимитом времени оставлять в игре на все время в зоне.
	 */
	public void scheduleDelete(long time)
	{
		if(time == 0)
		{
			deleteMe();
			return;
		}

		synchronized (_storeLock)
		{
			PlayerManager.saveCharToDisk(this); // получаем лишнее сохранение при логауте, но так надежнее
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
			public void run()
			{
				if(getNetConnection() == null || !getNetConnection().isConnected())
					deleteMe();
			}
		}, time);
	}

	@Override
	public void deleteMe()
	{
		if(isLogoutStarted())
			return;
		setLogoutStarted(true);

		prepareToLogout();

		synchronized (_storeLock)
		{
			PlayerManager.saveCharToDisk(this);
		}

		// Останавливаем и запоминаем все квестовые таймеры
		Quest.pauseQuestTimes(this);

		super.deleteMe();

		_isDeleting = true;

		getEffectList().stopAllEffects();

		setMassUpdating(true);

		//Send friendlists to friends that this player has logged off
		EnterWorld.notifyFriends(this, false);

		if(isInTransaction())
			getTransaction().cancel();

		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try
		{
			setOnlineStatus(false);
		}
		catch(Throwable t)
		{
			_log.log(Level.WARNING, "deletedMe()", t);
		}

		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try
		{
			stopAllTimers();
		}
		catch(Throwable t)
		{
			_log.log(Level.WARNING, "deletedMe()", t);
		}

		// Cancel Attak or Cast
		try
		{
			setTarget(null);
		}
		catch(Throwable t)
		{
			_log.log(Level.WARNING, "deletedMe()", t);
		}

		try
		{
			if(getClanId() > 0 && _clan != null && _clan.getClanMember(getObjectId()) != null)
			{
				int sponsor = _clan.getClanMember(getObjectId()).getSponsor();
				int apprentice = getApprentice();
				PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(this);
				for(L2Player clanMember : _clan.getOnlineMembers(getObjectId()))
				{
					if(clanMember.getObjectId() == getObjectId())
						continue;
					clanMember.sendPacket(memberUpdate);
					if(clanMember.getObjectId() == sponsor)
						clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_OUT).addString(_name));
					else if(clanMember.getObjectId() == apprentice)
						clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_OUT).addString(_name));
				}
				_clan.getClanMember(getObjectId()).setPlayerInstance(null);
			}
		}
		catch(final Throwable t)
		{
			_log.log(Level.SEVERE, "deletedMe()", t);
		}

		try
		{
			if(isCombatFlagEquipped())
			{
				L2ItemInstance flag = getActiveWeaponInstance();
				if(flag != null)
				{
					int customFlags = flag.getCustomFlags();
					flag.setCustomFlags(0, false);
					flag = getInventory().dropItem(flag, flag.getCount(), true);
					flag.setCustomFlags(customFlags, false);
					flag.dropMe(this, getLoc().rnd(0, 100, false));
				}
			}

			if(isTerritoryFlagEquipped())
			{
				L2ItemInstance flag = getActiveWeaponInstance();
				if(flag != null && flag.getCustomType1() != 77) // 77 это эвентовый флаг
				{
					L2TerritoryFlagInstance flagNpc = TerritorySiege.getNpcFlagByItemId(flag.getItemId());
					flagNpc.drop(this);
				}
			}

			/* TODO
			for(L2ItemInstance item : getInventory().getItemsList())
				if((item.getCustomFlags() & L2ItemInstance.FLAG_ALWAYS_DROP_ON_DIE) == L2ItemInstance.FLAG_DROP_ON_DISCONNECT)
				{
					item = getInventory().dropItem(item, item.getCount());
					item.dropMe(this, getLoc().rnd(0, 100, false));
				}
			*/
		}
		catch(Throwable t)
		{
			_log.log(Level.WARNING, "deletedMe()", t);
		}

		if(CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()) != null)
			CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).setPlayer(null);

		if(getPartyRoom() > 0)
		{
			PartyRoom room = PartyRoomManager.getInstance().getRooms().get(getPartyRoom());
			if(room != null)
				if(room.getLeader() == null || room.getLeader().equals(this))
					PartyRoomManager.getInstance().removeRoom(room.getId());
				else
					room.removeMember(this, false);
		}

		setPartyRoom(0);

		setEffectList(null);

		// Update database with items in its inventory and remove them from the world
		try
		{
			getInventory().deleteMe();
		}
		catch(Throwable t)
		{
			_log.log(Level.WARNING, "deletedMe()", t);
		}

		destroyAllTraps();

		if(_decoy != null)
			_decoy.unSummon();

		stopPvPFlag();

		bookmarks.clear();
		_warehouse = null;
		_freight = null;
		_ai = null;
		_summon = null;
		_arrowItem = null;
		_fistsWeaponItem = null;
		_chars = null;
		_enchantScroll = null;
		_agathion = null;
		_lastNpc = null;
		_obsLoc = null;
		_observNeighbor = null;
	}

	public void setTradeList(final L2TradeList x)
	{
		_tradeList = x;
	}

	public L2TradeList getTradeList()
	{
		return _tradeList;
	}

	public void setSellList(final ConcurrentLinkedQueue<TradeItem> x)
	{
		_sellList = x;
		saveTradeList();
	}

	public ConcurrentLinkedQueue<TradeItem> getSellList()
	{
		return _sellList != null ? _sellList : new ConcurrentLinkedQueue<TradeItem>();
	}

	public L2ManufactureList getCreateList()
	{
		return _createList;
	}

	public void setCreateList(final L2ManufactureList x)
	{
		_createList = x;
		saveTradeList();
	}

	public void setBuyList(final ConcurrentLinkedQueue<TradeItem> x)
	{
		_buyList = x;
		saveTradeList();
	}

	public ConcurrentLinkedQueue<TradeItem> getBuyList()
	{
		return _buyList != null ? _buyList : new ConcurrentLinkedQueue<TradeItem>();
	}

	public void setPrivateStoreType(final short type)
	{
		_privatestore = type;
		if(type != STORE_PRIVATE_NONE && type != STORE_OBSERVING_GAMES)
			setVar("storemode", String.valueOf(type));
		else
			unsetVar("storemode");
	}

	public short getPrivateStoreType()
	{
		if(inObserverMode())
			return STORE_OBSERVING_GAMES;

		return _privatestore;
	}

	public void setSkillLearningClassId(final ClassId classId)
	{
		_skillLearningClassId = classId;
	}

	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2Player.<BR><BR>
	 * @param clan the clat to set
	 */
	public void setClan(L2Clan clan)
	{
		if(_clan != clan && _clan != null)
			unsetVar("canWhWithdraw");

		L2Clan oldClan = _clan;
		if(oldClan != null && clan == null)
		{
			for(L2Skill skill : oldClan.getAllSkills())
				removeSkill(skill, false);
			for(int pledgeId : oldClan.getSquadSkills().keySet())
				if(pledgeId == _pledgeType)
				{
					FastMap<Integer, L2Skill> skills = oldClan.getSquadSkills().get(pledgeId);
					for(L2Skill s : skills.values())
						removeSkill(s, false);
				}
		}
		_clan = clan;

		if(clan == null)
		{
			_pledgeType = 0;
			_pledgeClass = 0;
			_powerGrade = 0;
			_apprentice = 0;
			getInventory().checkAllConditions();
			return;
		}

		if(!clan.isMember(getObjectId()))
		{
			// char has been kicked from clan
			_log.fine("Char " + _name + " is kicked from clan: " + clan.getName());
			setClan(null);
			setTitle("");
			return;
		}

		setTitle("");
	}

	public L2Clan getClan()
	{
		return _clan;
	}

	public ClanHall getClanHall()
	{
		return ClanHallManager.getInstance().getClanHallByOwner(_clan);
	}

	public Castle getCastle()
	{
		return CastleManager.getInstance().getCastleByOwner(_clan);
	}

	public Fortress getFortress()
	{
		return FortressManager.getInstance().getFortressByOwner(_clan);
	}

	public L2Alliance getAlliance()
	{
		return _clan == null ? null : _clan.getAlliance();
	}

	public boolean isClanLeader()
	{
		return _clan != null && _objectId == _clan.getLeaderId();
	}

	public boolean isAllyLeader()
	{
		return getAlliance() != null && getAlliance().getLeader().getLeaderId() == getObjectId();
	}

	@Override
	public void reduceArrowCount()
	{
		sendPacket(Msg.YOU_CAREFULLY_NOCK_AN_ARROW);
		if(!ConfigSystem.getBoolean("InfinityArrow"))
		{			
			L2ItemInstance arrows = getInventory().destroyItem(getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, false);
			if(arrows == null || arrows.getCount() == 0)
			{
				getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
				_arrowItem = null;
			}
		}
	}

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2Player then return True.
	 */
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			if(getActiveWeaponItem().getItemType() == WeaponType.BOW)
				_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			else if(getActiveWeaponItem().getItemType() == WeaponType.CROSSBOW)
				getInventory().findArrowForCrossbow(getActiveWeaponItem());

			// Equip arrows needed in left hand
			if(_arrowItem != null)
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
		}
		else
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		return _arrowItem != null;
	}

	public void setUptime(final long time)
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

	public void setParty(final L2Party party)
	{
		_party = party;
	}

	public void joinParty(final L2Party party)
	{
		if(party != null)
		{
			_party = party;
			party.addPartyMember(this);
		}
	}

	public void leaveParty()
	{
		if(isInParty())
		{
			_party.oustPartyMember(this);
			_party = null;
		}
	}

	public L2Party getParty()
	{
		return _party;
	}

	public boolean isGM()
	{
		return _playerAccess == null ? false : _playerAccess.IsGM;
	}

	/**
	 * Нигде не используется, но может пригодиться для БД
	 */
	public void setAccessLevel(final int level)
	{
		_accessLevel = level;
	}

	/**
	 * Нигде не используется, но может пригодиться для БД
	 */
	@Override
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public void setPlayerAccess(final PlayerAccess pa)
	{
		if(pa != null)
			_playerAccess = pa;
		else
			_playerAccess = new PlayerAccess();

		setAccessLevel(isGM() || _playerAccess.Menu ? 100 : 0);
	}

	public PlayerAccess getPlayerAccess()
	{
		return _playerAccess;
	}

	public void setAccountAccesslevel(final int level, final String comments, int banTime)
	{
		LSConnection.getInstance().sendPacket(new ChangeAccessLevel(getAccountName(), level, comments, banTime));
	}

	@Override
	public double getLevelMod()
	{
		return (89. + getLevel()) / 100.0;
	}

	/**
	 * Update Stats of the L2Player client side by sending Server->Client packet UserInfo/StatusUpdate to this L2Player and CharInfo/StatusUpdate to all L2Player in its _KnownPlayers (broadcast).<BR><BR>
	 */
	@Override
	public void updateStats()
	{
		refreshOverloaded();
        checkGradeExpertiseUpdate();
        checkArmorPenalty();
        checkWeaponPenalty();
		sendChanges();
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2Player and all L2Player to inform (broadcast).
	 */
	public void updateKarma(boolean flagChanged)
	{
		sendStatusUpdate(true, StatusUpdate.KARMA);
		if(flagChanged)
			broadcastRelationChanged();
	}

	public void setOnlineStatus(final boolean isOnline)
	{
		_isOnline = isOnline;
		updateOnlineStatus();
	}

	public void storeHWID(String HWID)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET LastHWID=? WHERE obj_id=?");
			statement.setString(1, HWID);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warning("could not store characters HWID:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void updateOnlineStatus()
	{
		boolean online = isOnline();
		if(isInOfflineMode())
			online = false;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, online ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis() / 1000);
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warning("could not set char online status:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void increaseKarma(final long add_karma)
	{
		boolean flagChanged = _karma == 0;
		long new_karma = _karma - add_karma;

		if(new_karma < Integer.MIN_VALUE)
			new_karma = Integer.MIN_VALUE;

		if(new_karma < 0)
		{
			_karma = (int) new_karma;
			for(final L2Character cha : L2World.getAroundCharacters(this))
				if(cha instanceof L2GuardInstance && cha.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					cha.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
		else
			_karma = (int) new_karma;

		updateKarma(flagChanged);
	}

	/**
	 * Decrease Karma of the L2Player and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).
	 */
	public void decreaseKarma(final int i)
	{
		boolean flagChanged = _karma > 0;
		_karma += i;
		
		if (getPkKills() > 30 && _karma > 0)
			_karma = 0;

			updateKarma(flagChanged);
	}

	/**
	 * Create a new L2Player and add it in the characters table of the database.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2Player with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and	the Face type of the L2Player</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param accountName The name of the L2Player
	 * @param name The name of the L2Player
	 * @param hairStyle The hair style Identifier of the L2Player
	 * @param hairColor The hair color Identifier of the L2Player
	 * @param face The face type Identifier of the L2Player
	 *
	 * @return The L2Player added to the database or null
	 */
	public static L2Player create(int classId, byte sex, String accountName, final String name, final byte hairStyle, final byte hairColor, final byte face)
	{
		L2PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, sex != 0);

		// Create a new L2Player with an account name
		L2Player player = new L2Player(IdFactory.getInstance().getNextId(), template, accountName);

		player.setName(name);
		player.setTitle("");
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		player.setCreateTime(System.currentTimeMillis());

		// Add the player in the characters table of the database
		if(!PlayerManager.createDb(player))
			return null;

		return player;
	}

	/**
	 * Retrieve a L2Player from the characters table of the database and add it in _allObjects of the L2World
	 * @return The L2Player loaded from the database
	 */
	public static L2Player restore(final int objectId)
	{
		L2Player player = null;
		ThreadConnection con = null;
		FiltredStatement statement = null;
		FiltredStatement statement2 = null;
		ResultSet pl_rset = null;
		ResultSet ps_rset = null;
		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.createStatement();
			statement2 = con.createStatement();
			pl_rset = statement.executeQuery("SELECT * FROM `characters` WHERE `obj_Id`=" + objectId + " LIMIT 1");
			ps_rset = statement2.executeQuery("SELECT `class_id`, `AwakingId` FROM `character_subclasses` WHERE `char_obj_id`=" + objectId + " AND `isBase`=1 LIMIT 1");

			if(pl_rset.next() && ps_rset.next())
			{
				final int classId = ps_rset.getInt("class_id");
				final boolean female = pl_rset.getInt("sex") == 1;
				final L2PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(classId, female);

				player = new L2Player(objectId, template);

				player.loadVariables();
				player.bookmarks.setCapacity(pl_rset.getInt("bookmarks"));

				player.setBaseClass(classId);

				player._accountName = pl_rset.getString("account_name");
				player.setName(pl_rset.getString("char_name"));
				player.setFace(pl_rset.getByte("face"));
				player.setHairStyle(pl_rset.getByte("hairStyle"));
				player.setHairColor(pl_rset.getByte("hairColor"));
				player.setHeading(pl_rset.getInt("heading"));

				player.setKarma(pl_rset.getInt("karma"));
				player.setPvpKills(pl_rset.getInt("pvpkills"));
				player.setPkKills(pl_rset.getInt("pkkills"));
				player.setLeaveClanTime(pl_rset.getLong("leaveclan") * 1000L);
				if(player.getLeaveClanTime() > 0 && player.canJoinClan())
					player.setLeaveClanTime(0);
				player.setDeleteClanTime(pl_rset.getLong("deleteclan") * 1000L);
				if(player.getDeleteClanTime() > 0 && player.canCreateClan())
					player.setDeleteClanTime(0);

				player.setNoChannel(pl_rset.getLong("nochannel") * 1000L);
				if(player.getNoChannel() > 0 && player.getNoChannelRemained() < 0)
					player.updateNoChannel(0);

				player.setOnlineTime(pl_rset.getLong("onlinetime") * 1000L);

				final int clanId = pl_rset.getInt("clanid");
				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
					player.setPledgeType(pl_rset.getInt("pledge_type"));
					player.setPowerGrade(pl_rset.getInt("pledge_rank"));
					player.setLvlJoinedAcademy(pl_rset.getInt("lvl_joined_academy"));
					player.setApprentice(pl_rset.getInt("apprentice"));
				}

				player.setCreateTime(pl_rset.getLong("createtime") * 1000L);
				player.setDeleteTimer(pl_rset.getInt("deletetime"));
                player.setRecomHave(pl_rset.getInt("rec_have"));
                player.setRecomLeft(pl_rset.getInt("rec_left"));
                player.setRecomTimeLeft(pl_rset.getInt("rec_timeleft"));
				player.setTitle(pl_rset.getString("title"));

				if(player.getVar("namecolor") == null)
					if(player.isGM())
						player.setNameColor(Config.GM_NAME_COLOUR);
					else if(player.getClan() != null && player.getClan().getLeaderId() == player.getObjectId())
						player.setNameColor(Config.CLANLEADER_NAME_COLOUR);
					else
						player.setNameColor(Config.NORMAL_NAME_COLOUR);
				else
					player.setNameColor(Integer.decode("0x" + player.getVar("namecolor")));

				if(Config.AUTO_LOOT_INDIVIDUAL)
				{
					player.AutoLoot = player.getVarB("AutoLoot", Config.AUTO_LOOT);
					player.AutoLootHerbs = player.getVarB("AutoLootHerbs", Config.AUTO_LOOT_HERBS);
				}
				player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
				player.setUptime(System.currentTimeMillis());
				player.setLastAccess(pl_rset.getLong("lastAccess"));

				player.setRecomHave(pl_rset.getInt("rec_have"));
				player.setRecomLeft(pl_rset.getInt("rec_left"));

				player.setKeyBindings(pl_rset.getBytes("key_bindings"));
				player.setPcBangPoints(pl_rset.getInt("pcBangPoints"));
				player.setBookMarkSlot(pl_rset.getInt("Bookmarks"));

				player.setFame(pl_rset.getInt("fame"), null);

				player.restoreRecipeBook();

				if(Config.ENABLE_OLYMPIAD)
				{
					player.setHero(Hero.getInstance().isHero(player.getObjectId()));
					player.setNoble(Olympiad.isNoble(player.getObjectId()));
				}

				player.updatePledgeClass();

				player.updateKetraVarka();
				player.updateRam();

				// для сервиса виверн - возврат денег если сервер упал во время полета
				String wm = player.getVar("wyvern_moneyback");
				if(wm != null && Integer.parseInt(wm) > 0)
					player.addAdena(Integer.parseInt(wm));
				player.unsetVar("wyvern_moneyback");

				long reflection = 0;

				// Set the x,y,z position of the L2Player and make it invisible
				if(player.getVar("jailed") != null && System.currentTimeMillis() / 1000 < Integer.parseInt(player.getVar("jailed")) + 60)
				{
					player.setXYZInvisible(-114648, -249384, -2984);
					String[] re = player.getVar("jailedFrom").split(";");
					Location loc = new Location(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
					reflection = -3;

					player._unjailTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player, loc, re.length > 3 ? Integer.parseInt(re[3]) : 0), Integer.parseInt(player.getVar("jailed")) * 1000L);
				}
				else
				{
					player.setXYZInvisible(pl_rset.getInt("x"), pl_rset.getInt("y"), pl_rset.getInt("z"));
					wm = player.getVar("reflection");
					if(wm != null)
					{
						reflection = Long.parseLong(wm);
						if(reflection > 0)
						{
							String back = player.getVar("backCoords");
							if(back != null)
							{
								player.setXYZInvisible(new Location(back));
								player.unsetVar("backCoords");
							}
							reflection = 0;
						}
					}
				}

				player.setReflection(reflection);

				player.restoreTradeList();
				if(player.getVar("storemode") != null)
					if(player.getVar("offline") != null) // оффтрейдеры выбивают других, онтрейдеры нет
					{
						if(Config.SERVICES_TRADE_ONLY_FAR)
						{
							L2WorldRegion currentRegion = L2World.getRegion(player.getLoc(), player.getReflectionId());
							if(currentRegion != null)
							{
								GArray<L2WorldRegion> neighbors = currentRegion.getNeighbors();
								int size = 0;
								for(L2WorldRegion region : neighbors)
									size += region.getPlayersSize();
								GArray<L2Player> result = new GArray<L2Player>(size);
								for(L2WorldRegion region : neighbors)
									region.getPlayersList(result, 0, player.getReflection(), player.getX(), player.getY(), player.getZ(), Config.SERVICES_TRADE_RADIUS * Config.SERVICES_TRADE_RADIUS, 200);

								for(L2Player p : result)
									if(p.isInStoreMode())
										if(p.isInOfflineMode())
											L2TradeList.cancelStore(p);
										else
										{
											p.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
											p.broadcastUserInfo(true);
										}
							}
						}

						player.setPrivateStoreType(Short.parseShort(player.getVar("storemode")));
						player.setSitting(true);
					}
					else
					{
						short type = Short.parseShort(player.getVar("storemode"));
						if(player.checksForShop(type == STORE_PRIVATE_MANUFACTURE))
						{
							player.setPrivateStoreType(type);
							player.setSitting(true);
						}
						else
							player.unsetVar("storemode");
					}

				if(TerritorySiege.isInProgress())
					player.setTerritorySiege(TerritorySiege.getTerritoryForPlayer(objectId));

				Quest.playerEnter(player);
				
				player._hidden = true;
				restoreCharSubClasses(player);
				player._hidden = false;
				
				player.restoreVitality();
				
				// 15 секунд после входа в игру на персонажа не агрятся мобы
				player.setNonAggroTime(System.currentTimeMillis() + 15000);

				try
				{
					String var = player.getVar("ExpandInventory");
					if(var != null)
						player.setExpandInventory(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					String var = player.getVar("ExpandWarehouse");
					if(var != null)
						player.setExpandWarehouse(Integer.parseInt(var));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					String var = player.getVar("notShowBuffAnim");
					if(var != null)
						player.setNotShowBuffAnim(Boolean.parseBoolean(var));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

				FiltredPreparedStatement stmt = null;
				ResultSet chars = null;
				try
				{
					stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id!=?");
					stmt.setString(1, player._accountName);
					stmt.setInt(2, objectId);
					chars = stmt.executeQuery();
					while(chars.next())
					{
						final Integer charId = chars.getInt("obj_Id");
						final String charName = chars.getString("char_name");
						player._chars.put(charId, charName);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					DatabaseUtils.closeDatabaseSR(stmt, chars);
				}

				if(Config.KILL_COUNTER)
				{
					// Restore kills stat
					FiltredStatement stt = null;
					ResultSet rstkills = null;
					try
					{
						stt = con.createStatement();
						rstkills = stt.executeQuery("SELECT `npc_id`, `count` FROM `killcount` WHERE `char_id`=" + objectId);
						player._StatKills = new HashMap<Integer, Long>(128);
						while(rstkills.next())
							player._StatKills.put(rstkills.getInt("npc_id"), rstkills.getLong("count"));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						DatabaseUtils.closeDatabaseSR(stt, rstkills);
					}
				}

				//Restore craft stat
				if(Config.CRAFT_COUNTER)
				{
					FiltredStatement stcraft = null;
					ResultSet rstcraft = null;
					try
					{
						stcraft = con.createStatement();
						rstcraft = stcraft.executeQuery("SELECT `item_id`, `count` FROM `craftcount` WHERE `char_id`=" + objectId);
						player._StatCraft = new HashMap<Integer, Long>(32);
						while(rstcraft.next())
							player._StatCraft.put(rstcraft.getInt("item_id"), rstcraft.getLong("count"));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						DatabaseUtils.closeDatabaseSR(stcraft, rstcraft);
					}
				}

				if(Config.DROP_COUNTER)
				{
					//Restore drop stat
					FiltredStatement stdrop = null;
					ResultSet rstdrop = null;
					try
					{
						stdrop = con.createStatement();
						rstdrop = stdrop.executeQuery("SELECT `item_id`, `count` FROM `dropcount` WHERE `char_id`=" + objectId);
						player._StatDrop = new HashMap<Integer, Long>(128);
						while(rstdrop.next())
							player._StatDrop.put(rstdrop.getInt("item_id"), rstdrop.getLong("count"));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						DatabaseUtils.closeDatabaseSR(stdrop, rstdrop);
					}
				}

				if(!L2World.validCoords(player.getX(), player.getY()) || player.getX() == 0 && player.getY() == 0)
					player.setXYZInvisible(MapRegion.getTeleToClosestTown(player));

				// Перед началом работы с территориями, выполним их обновление
				player.updateTerritories();

				if(!player.isGM())
				{
					if(Config.ENABLE_OLYMPIAD && player.isInZone(ZoneType.OlympiadStadia))
					{
						player.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.EnterWorld.TeleportedReasonOlympiad", player));
						player.setXYZInvisible(MapRegion.getTeleToClosestTown(player));
					}

					L2Zone noRestartZone = ZoneManager.getInstance().getZoneByTypeAndObject(no_restart, player);
					if(noRestartZone != null && System.currentTimeMillis() / 1000 - player.getLastAccess() > noRestartZone.getRestartTime())
					{
						player.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.EnterWorld.TeleportedReasonNoRestart", player));
						player.setXYZInvisible(MapRegion.getTeleToClosestTown(player));
					}

					if(player.isInZone(Siege))
					{
						Siege siege = SiegeManager.getSiege(player, true);
						if(siege != null && !siege.checkIsDefender(player.getClan()))
							if(siege.getHeadquarter(player.getClan()) == null)
								player.setXYZInvisible(MapRegion.getTeleToClosestTown(player));
							else
								player.setXYZInvisible(MapRegion.getTeleToHeadquarter(player));
						if(TerritorySiege.checkIfInZone(player))
							if(TerritorySiege.getHeadquarter(player.getClan()) == null)
								player.setXYZInvisible(MapRegion.getTeleToClosestTown(player));
							else
								player.setXYZInvisible(MapRegion.getTeleToHeadquarter(player));
					}

				}

				player.getInventory().validateItems();
				player.revalidatePenalties();
				player.restoreBlockList();
				BreakWarnManager.getInstance().addWarnTask(player);
				AutoSaveManager.getInstance().addPlayerTask(player);
			}
		}
		catch(final Exception e)
		{
			_log.log(Level.WARNING, "restore: could not restore char data:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseSR(statement2, ps_rset);
			DatabaseUtils.closeDatabaseCSR(con, statement, pl_rset);
		}
		return player;
	}

	public Future<?> _unjailTask;

	public void incrementKillsCounter(final Integer Id)
	{
		final Long tmp = _StatKills.containsKey(Id) ? _StatKills.get(Id) + 1 : 1;
		_StatKills.put(Id, tmp);
		sendMessage(new CustomMessage("l2rt.gameserver.model.L2Player.KillsCounter", this).addString(tmp.toString()));
	}

	public void incrementDropCounter(final Integer Id, final Long qty)
	{
		_StatDrop.put(Id, _StatDrop.containsKey(Id) ? _StatDrop.get(Id) + qty : qty);
	}

	public void incrementCraftCounter(final Integer Id, final int qty)
	{
		final Long tmp = _StatCraft.containsKey(Id) ? _StatCraft.get(Id) + qty : qty;
		_StatCraft.put(Id, tmp);
		sendMessage(new CustomMessage("l2rt.gameserver.model.L2Player.CraftCounter", this).addString(tmp.toString()));
	}

	private final Object _storeLock = new Object();

	/**
	 * Update L2Player stats in the characters table of the database.
	 */
	public void store(boolean fast)
	{
		synchronized (_storeLock)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			FiltredStatement fs = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(//
				"UPDATE characters SET face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?" + //
				",karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,deletetime=?," + //
				"title=?,accesslevel=?,online=?,leaveclan=?,deleteclan=?,nochannel=?," + //
				"onlinetime=?,pledge_type=?,pledge_rank=?,lvl_joined_academy=?,apprentice=?,key_bindings=?,pcBangPoints=?,char_name=?,fame=?,bookmarks=?,rec_timeleft=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, getFace());
				statement.setInt(2, getHairStyle());
				statement.setInt(3, getHairColor());
				statement.setInt(4, getHeading() & 0xFFFF);
				if(_stablePoint == null) // если игрок находится в точке в которой его сохранять не стоит (например на виверне) то сохраняются последние координаты
				{
					statement.setInt(5, getX());
					statement.setInt(6, getY());
					statement.setInt(7, getZ());
				}
				else
				{
					statement.setInt(5, _stablePoint.x);
					statement.setInt(6, _stablePoint.y);
					statement.setInt(7, _stablePoint.z);
				}
				statement.setInt(8, getKarma());
				statement.setInt(9, getPvpKills());
				statement.setInt(10, getPkKills());
				statement.setInt(11, getRecomHave());
				statement.setInt(12, getRecomLeft());
				statement.setInt(13, getClanId());
				statement.setInt(14, getDeleteTimer());
				statement.setString(15, _title);
				statement.setInt(16, _accessLevel);
				statement.setInt(17, isOnline() ? 1 : 0);
				statement.setLong(18, getLeaveClanTime() / 1000);
				statement.setLong(19, getDeleteClanTime() / 1000);
				statement.setLong(20, _NoChannel > 0 ? getNoChannelRemained() / 1000 : _NoChannel);
				statement.setLong(21, _onlineBeginTime > 0 ? (_onlineTime + System.currentTimeMillis() - _onlineBeginTime) / 1000 : _onlineTime / 1000);
				statement.setInt(22, getPledgeType());
				statement.setInt(23, getPowerGrade());
				statement.setInt(24, getLvlJoinedAcademy());
				statement.setInt(25, getApprentice());
				statement.setBytes(26, getKeyBindings());
				statement.setInt(27, getPcBangPoints());
				statement.setString(28, getName());
				statement.setInt(29, getFame());
				statement.setInt(30, bookmarks.getCapacity());
				statement.setInt(31, getRecomTimeLeft());
				statement.setInt(32, getObjectId());

				statement.executeUpdate();
				Stat.increaseUpdatePlayerBase();

				try
				{
					if(!fast && Config.KILL_COUNTER && _StatKills != null)
					{
						TextBuilder sb = TextBuilder.newInstance();
						fs = con.createStatement();
						for(Entry<Integer, Long> tmp : _StatKills.entrySet())
						{
							fs.addBatch(sb.append("REPLACE DELAYED INTO `killcount` SET `npc_id`=").append(tmp.getKey()).append(", `count`=").append(tmp.getValue()).append(", `char_id`=").append(_objectId).toString());
							sb.clear();
						}
						TextBuilder.recycle(sb);
						fs.executeBatch();
						DatabaseUtils.closeStatement(fs);
					}

					if(!fast && Config.CRAFT_COUNTER && _StatCraft != null)
					{
						TextBuilder sb = TextBuilder.newInstance();
						fs = con.createStatement();
						for(Entry<Integer, Long> tmp : _StatCraft.entrySet())
						{
							fs.addBatch(sb.append("REPLACE DELAYED INTO `craftcount` SET `item_id`=").append(tmp.getKey()).append(", `count`=").append(tmp.getValue()).append(", `char_id`=").append(_objectId).toString());
							sb.clear();
						}
						TextBuilder.recycle(sb);
						fs.executeBatch();
						DatabaseUtils.closeStatement(fs);
					}

					if(!fast && Config.DROP_COUNTER && _StatDrop != null)
					{
						TextBuilder sb = TextBuilder.newInstance();
						fs = con.createStatement();
						for(Entry<Integer, Long> tmp : _StatDrop.entrySet())
						{
							fs.addBatch(sb.append("REPLACE DELAYED INTO `craftcount` SET `item_id`=").append(tmp.getKey()).append(", `count`=").append(tmp.getValue()).append(", `char_id`=").append(_objectId).toString());
							sb.clear();
						}
						TextBuilder.recycle(sb);
						fs.executeBatch();
						DatabaseUtils.closeStatement(fs);
					}
				}
				catch(ConcurrentModificationException e)
				{}

				if(!fast)
				{
					storeEffects();
					storeDisableSkills();
					storeBlockList();
				}

				storeCharSubClasses();
				bookmarks.store();
				storeVitality();
			}
			catch(Exception e)
			{
				_log.warning("store: could not store char data: " + e);
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	/**
	 * Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player and save update in the character_skills table of the database.
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public L2Skill addSkill(final L2Skill newSkill, final boolean store)
	{
		if(newSkill == null)
			return null;

		
		if (isAwaking())
		{
			AwakingManager.getInstance().onAddSkill(this, newSkill.getId());
		}
			
		// Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player
		L2Skill oldSkill = super.addSkill(newSkill);

		if(newSkill.equals(oldSkill))
			return oldSkill;

		// Add or update a L2Player skill in the character_skills table of the database
		if(store)
			storeSkill(newSkill, oldSkill);

		return oldSkill;
	}

	public L2Skill removeSkill(L2Skill skill, boolean fromDB)
	{
		if(skill == null)
			return null;
		return removeSkill(skill.getId(), fromDB);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.
	 * @return The L2Skill removed
	 */
	public L2Skill removeSkill(int id, boolean fromDB)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		L2Skill oldSkill = super.removeSkillById(id);

		if(!fromDB)
			return oldSkill;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			// Remove or update a L2Player skill from the character_skills table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			if(oldSkill != null)
			{
				statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?");
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getActiveClassId());
				statement.execute();
			}
		}
		catch(final Exception e)
		{
			_log.log(Level.WARNING, "Error could not delete Skill:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		return oldSkill;
	}

	/**
	 * Add or update a L2Player skill in the character_skills table of the database.
	 */
	private void storeSkill(final L2Skill newSkill, final L2Skill oldSkill)
	{
		if(newSkill == null) // вообще-то невозможно
		{
			_log.warning("could not store new skill. its NULL");
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("REPLACE INTO character_skills (char_obj_id,skill_id,skill_level,class_index) values(?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newSkill.getId());
			statement.setInt(3, newSkill.getLevel());
			statement.setInt(4, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.log(Level.WARNING, "Error could not store Skills:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Retrieve from the database all skills of this L2Player and add them to _skills.
	 */
	private void restoreSkills()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2Player from the database
			// Send the SQL query : SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? to the database
			con = L2DatabaseFactory.getInstance().getConnection(); 
			if(ConfigSystem.getBoolean("MultiProfa") && getActiveClass().isBase())
			{ 
				statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? "); 
				statement.setInt(1, getObjectId()); 
				rset = statement.executeQuery(); 
			} 
			else 
			{ 
				statement = con.prepareStatement("SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?"); 
				statement.setInt(1, getObjectId()); 
				statement.setInt(2, getActiveClassId()); 
				rset = statement.executeQuery(); 
			}

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				final int id = rset.getInt("skill_id");
				final int level = rset.getInt("skill_level");

				if(id > 9000)
					continue; // fake skills for base stats

				// Create a L2Skill object for each record
				final L2Skill skill = SkillTable.getInstance().getInfo(id, level);

				if(skill == null)
					continue;

				// Remove skill if not possible
				if(!ConfigSystem.getBoolean("OldSkillDelete"))
				{ 
					if(!_playerAccess.IsGM && !skill.isCommon() && !SkillTreeTable.getInstance().isSkillPossible(this, skill.getId(), skill.getLevel())) 
					{ 
						int ReturnSP = SkillTreeTable.getInstance().getSkillCost(this, skill); 
						if(ReturnSP == Integer.MAX_VALUE || ReturnSP < 0) 
							ReturnSP = 0; 
						removeSkill(skill, true); 
						removeSkillFromShortCut(skill.getId()); 
						if(ReturnSP > 0) 
							setSp(getSp() + ReturnSP); 
						illegalAction("has skill " + skill.getName() + " / ReturnSP: " + ReturnSP, 0); 
						continue; 
					} 
				}

				// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
				super.addSkill(skill);
			}

			// Restore noble skills
			if(isNoble())
				updateNobleSkills();

			// Restore Hero skills at main class only
			if(_hero && getBaseClassId() == getActiveClassId())
				Hero.addSkills(this);

			if(_clan != null)
			{
				// Restore clan leader siege skills
				if(_clan.getLeaderId() == getObjectId() && _clan.getLevel() >= CastleSiegeManager.getSiegeClanMinLevel())
					SiegeManager.addSiegeSkills(this);

				// Restore clan skills
				_clan.addAndShowSkillsToPlayer(this);
			}

			// Give dwarven craft skill
			if(getActiveClassId() >= 53 && getActiveClassId() <= 57 || getActiveClassId() == 117 || getActiveClassId() == 118)
				super.addSkill(SkillTable.getInstance().getInfo(1321, 1));
			super.addSkill(SkillTable.getInstance().getInfo(1322, 1));

			if(ConfigSystem.getBoolean("UnstuckSkill") && getSkillLevel(1050) < 0)
				super.addSkill(SkillTable.getInstance().getInfo(2099, 1));
		}
		catch(final Exception e)
		{
			_log.warning("Could not restore skills for player objId: " + getObjectId());
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void deleteSubclassSkills()
	{
		try
		{
			for(L2SubClass subClass : getSubClasses().values())
				if(!subClass.getSkills().isEmpty())
					for(String i : subClass.getSkills().split(";"))
						super.removeSkillById(Integer.parseInt(i));
		}
		catch(final Exception e)
		{
			_log.warning("Could not delete subclass skills for player objId: " + getObjectId());
			e.printStackTrace();
		}
	}

	public void restoreSubclassSkills()
	{
		if(!getActiveClass().isBase())
			return;
		try
		{
			for(L2SubClass subClass : getSubClasses().values())
				if(!subClass.getSkills().isEmpty())
					for(String i : subClass.getSkills().split(";"))
					{
						int id = Integer.parseInt(i);
						int level = Math.max(1, getSkillLevel(id) + 1);
						L2Skill skill = SkillTable.getInstance().getInfo(id, level);
						if(skill != null)
							super.addSkill(skill);
						else
							System.out.println("Not found skill id: " + id + ", level: " + level);
					}
		}
		catch(final Exception e)
		{
			_log.warning("Could not restore subclass skills for player objId: " + getObjectId());
			e.printStackTrace();
		}
 	}
	
	public void storeDisableSkills()
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());

			if(skillReuseTimeStamps.isEmpty())
				return;

			SqlBatch b = new SqlBatch("REPLACE INTO `character_skills_save` (`char_obj_id`,`skill_id`,`class_index`,`end_time`,`reuse_delay_org`) VALUES");
			synchronized (skillReuseTimeStamps)
			{
				StringBuilder sb;
				for(Entry<Integer, SkillTimeStamp> tmp : getSkillReuseTimeStamps().entrySet())
					if(tmp.getValue().hasNotPassed())
					{
						sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(tmp.getKey()).append(",");
						sb.append(getActiveClassId()).append(",");
						sb.append(tmp.getValue().getEndTime()).append(",");
						sb.append(tmp.getValue().getReuseBasic()).append(")");
						b.write(sb.toString());
					}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(final Exception e)
		{
			_log.warning("Could not store disable skills data: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void storeEffects()
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_effects_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId());

			if(_effectList == null || _effectList.isEmpty())
				return;

			int order = 0;
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_effects_save` (`char_obj_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`,`class_index`) VALUES");

			synchronized (getEffectList())
			{
				StringBuilder sb;
				for(L2Effect effect : getEffectList().getAllEffects())
					if(effect != null && effect.isInUse() && !effect.getSkill().isToggle() && effect.getEffectType() != EffectType.HealOverTime && effect.getEffectType() != EffectType.CombatPointHealOverTime)
					{
						if(effect.isSaveable())
						{
							sb = new StringBuilder("(");
							sb.append(getObjectId()).append(",");
							sb.append(effect.getSkill().getId()).append(",");
							sb.append(effect.getSkill().getLevel()).append(",");
							sb.append(effect.getCount()).append(",");
							sb.append(effect.getTime()).append(",");
							sb.append(effect.getPeriod()).append(",");
							sb.append(order).append(",");
							sb.append(getActiveClassId()).append(")");
							b.write(sb.toString());
						}
						while((effect = effect.getNext()) != null && effect.isSaveable())
						{
							sb = new StringBuilder("(");
							sb.append(getObjectId()).append(",");
							sb.append(effect.getSkill().getId()).append(",");
							sb.append(effect.getSkill().getLevel()).append(",");
							sb.append(effect.getCount()).append(",");
							sb.append(effect.getTime()).append(",");
							sb.append(effect.getPeriod()).append(",");
							sb.append(order).append(",");
							sb.append(getActiveClassId()).append(")");
							b.write(sb.toString());
						}
						order++;
					}
				if(ConfigSystem.getBoolean("AltSaveUnsaveable") && _cubics != null)
					for(L2CubicInstance cubic : _cubics)
					{
						sb = new StringBuilder("(");
						sb.append(getObjectId()).append(",");
						sb.append(cubic.getId() + L2CubicInstance.CUBIC_STORE_OFFSET).append(",");
						sb.append(cubic.getLevel()).append(",1,");
						sb.append(cubic.lifeLeft()).append(",1,");
						sb.append(order++).append(",");
						sb.append(getActiveClassId()).append(")");
						b.write(sb.toString());
					}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(final Exception e)
		{
			_log.warning("Could not store active effects data: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void restoreEffects()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `character_effects_save` WHERE `char_obj_id`=? AND `class_index`=? ORDER BY `order` ASC");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());

			rset = statement.executeQuery();
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				long effectCurTime = rset.getLong("effect_cur_time");
				long duration = rset.getLong("duration");

				if(skillId >= L2CubicInstance.CUBIC_STORE_OFFSET) // cubic
				{
					skillId -= L2CubicInstance.CUBIC_STORE_OFFSET;
					addCubic(skillId, skillLvl, (int) effectCurTime, true);
					continue;
				}

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);

				if(skill == null)
				{
					_log.warning("Can't restore Effect\tskill: " + skillId + ":" + skillLvl + " " + toFullString());
					Thread.dumpStack();
					continue;
				}

				if(skill.getEffectTemplates() == null)
				{
					_log.warning("Can't restore Effect, EffectTemplates is NULL\tskill: " + skillId + ":" + skillLvl + " " + toFullString());
					Thread.dumpStack();
					continue;
				}

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et == null)
						continue;
					Env env = new Env(this, this, skill);
					L2Effect effect = et.getEffect(env);
					if(effect == null)
						continue;
					if(effectCount == 1)
					{
						effect.setCount(effectCount);
						effect.setPeriod(duration - effectCurTime);
					}
					else
					{
						effect.setPeriod(duration);
						effect.setCount(effectCount);
					}
					getEffectList().addEffect(effect);
				}
			}
		}
		catch(final Exception e)
		{
			_log.warning("Could not restore active effects data [charId: " + getObjectId() + "; ActiveClassId: " + getActiveClassId() + "]: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		updateEffectIcons();
		broadcastUserInfo(true);
	}

	public void restoreDisableSkills()
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.createStatement();
			rset = statement.executeQuery("SELECT skill_id,end_time,reuse_delay_org FROM character_skills_save WHERE char_obj_id=" + getObjectId() + " AND class_index=" + getActiveClassId());

			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLevel = Math.max(getSkillLevel(skillId), 1);
				long endTime = rset.getLong("end_time");
				long rDelayOrg = rset.getLong("reuse_delay_org");
				long curTime = System.currentTimeMillis();

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				if(skill != null && endTime - curTime > 500)
				{
					getSkillReuseTimeStamps().put(skillId, new SkillTimeStamp(skillId, endTime, rDelayOrg));
					disableItem(skill, rDelayOrg, endTime - curTime);
				}
			}
			DatabaseUtils.closeStatement(statement);

			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_skills_save WHERE char_obj_id = " + getObjectId() + " AND class_index=" + getActiveClassId() + " AND `end_time` < " + System.currentTimeMillis());
		}
		catch(Exception e)
		{
			_log.warning("Could not restore active skills data for " + getObjectId() + "/" + getActiveClassId());
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			updateEffectIcons();
		}
	}

	@Override
	public void disableItem(L2Skill handler, long timeTotal, long timeLeft)
	{
		if(handler.isHandler() && timeLeft > 1000)
			if(handler.getReuseGroupId() > 0)
			{
				GArray<Integer> disabled = new GArray<Integer>();
				for(Integer skill_id : handler.getReuseGroup())
				{
					// TODO: хранить отключенные группы отдельно от скиллов во избежание коллизий и для отсылки при изменении ярлыков
					  for(L2Skill sk : SkillTable.getInstance().getAllLevels(skill_id))
						if(sk != null && sk._itemConsumeId[0] != 0 && !disabled.contains(sk._itemConsumeId[0]))
						{
							sendPacket(new ExUseSharedGroupItem(sk._itemConsumeId[0], sk.getReuseGroupId(), (int) timeLeft, (int) timeTotal));
							disabled.add(sk._itemConsumeId[0]);
						}
					if(!isSkillDisabled(skill_id))
						disableSkill(skill_id, timeLeft);
				}
			}
			else
				sendPacket(new ExUseSharedGroupItem(handler._itemConsumeId[0], handler._itemConsumeId[0], (int) timeTotal, (int) timeLeft));
	}

	/**
	 * Retrieve from the database all Henna of this L2Player, add them to _henna and calculate stats of the L2Player.<BR><BR>
	 */
	private void restoreHenna()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, getActiveClassId());
			rset = statement.executeQuery();

			for(int i = 0; i < 3; i++)
				_henna[i] = null;

			while(rset.next())
			{
				final int slot = rset.getInt("slot");
				if(slot < 1 || slot > 3)
					continue;

				final int symbol_id = rset.getInt("symbol_id");

				L2HennaInstance sym;

				if(symbol_id != 0)
				{
					final L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);
					if(tpl != null)
					{
						sym = new L2HennaInstance(tpl);
						_henna[slot - 1] = sym;
					}
				}
			}
		}
		catch(final Exception e)
		{
			_log.warning("could not restore henna: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

	}

	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassId().level();
		for(int i = 0; i < 3; i++)
			if(_henna[i] != null)
				totalSlots--;

		if(totalSlots <= 0)
			return 0;

		return totalSlots;

	}

	/**
	 * Remove a Henna of the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if(slot < 1 || slot > 3)
			return false;

		slot--;

		if(_henna[slot] == null)
			return false;

		final L2HennaInstance henna = _henna[slot];
		final int dyeID = henna.getItemIdDye();

		// Added by Tempy - 10 Aug 05
		// Gives amount equal to half of the dyes needed for the henna back.
		final L2ItemInstance hennaDyes = ItemTemplates.getInstance().createItem(dyeID);
		hennaDyes.setCount(henna.getAmountDyeRequire() / 2);

		_henna[slot] = null;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=? and class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getActiveClassId());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warning("could not remove char henna: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		// Calculate Henna modifiers of this L2Player
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2Player
		sendPacket(new HennaInfo(this));

		// Send Server->Client UserInfo packet to this L2Player
		sendUserInfo(false);

		// Add the recovered dyes to the player's inventory and notify them.
		getInventory().addItem(hennaDyes);
		sendPacket(SystemMessage.obtainItems(henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, 0));

		return true;
	}

	/**
	 * Add a Henna to the L2Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2Player.<BR><BR>
	 * @param henna L2HennaInstance для добавления
	 */
	public boolean addHenna(L2HennaInstance henna)
	{
		if(getHennaEmptySlots() == 0)
		{
			sendPacket(Msg.NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL);
			return false;
		}

		// int slot = 0;
		for(int i = 0; i < 3; i++)
			if(_henna[i] == null)
			{
				_henna[i] = henna;

				// Calculate Henna modifiers of this L2Player
				recalcHennaStats();

				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO `character_hennas` (char_obj_id, symbol_id, slot, class_index) VALUES (?,?,?,?)");
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getActiveClassId());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.warning("could not save char henna: " + e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}

				sendPacket(new HennaInfo(this));
				sendUserInfo(true);

				return true;
			}

		return false;
	}

	/**
	 * Calculate Henna modifiers of this L2Player.
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for(int i = 0; i < 3; i++)
		{
			if(_henna[i] == null)
				continue;
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEN();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}

		if(_hennaINT > 5)
			_hennaINT = 5;
		if(_hennaSTR > 5)
			_hennaSTR = 5;
		if(_hennaMEN > 5)
			_hennaMEN = 5;
		if(_hennaCON > 5)
			_hennaCON = 5;
		if(_hennaWIT > 5)
			_hennaWIT = 5;
		if(_hennaDEX > 5)
			_hennaDEX = 5;
	}

	/**
	 * @param slot id слота у перса
	 * @return the Henna of this L2Player corresponding to the selected slot.<BR><BR>
	 */
	public L2HennaInstance getHenna(final int slot)
	{
		if(slot < 1 || slot > 3)
			return null;
		return _henna[slot - 1];
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

	@Override
	public boolean consumeItem(final int itemConsumeId, final int itemCount)
	{
		L2ItemInstance item = getInventory().getItemByItemId(itemConsumeId);
		if(item == null || item.getCount() < itemCount)
			return false;
		if(getInventory().destroyItem(item, itemCount, false) != null)
		{
			sendPacket(SystemMessage.removeItems(itemConsumeId, itemCount));
			return true;
		}
		return false;
	}

	/**
	 * @return True if the L2Player is a Mage.<BR><BR>
	 */
	@Override
	public boolean isMageClass()
	{
		return _template.baseMAtk > 3;
	}

	public boolean isMounted()
	{
		return _mountNpcId > 0;
	}

	/**
	 * Проверяет, можно ли приземлиться в этой зоне.
	 * @return можно ли приземлится
	 */
	public boolean checkLandingState()
	{
		if(isInZone(no_landing))
			return false;

		Siege siege = SiegeManager.getSiege(this, false);
		if(siege != null)
		{
			Residence unit = siege.getSiegeUnit();
			if(unit != null && getClan() != null && isClanLeader() && (getClan().getHasCastle() == unit.getId() || getClan().getHasFortress() == unit.getId()))
				return true;
			return false;
		}

		return true;
	}

	public void setMount(int npcId, int obj_id, int level)
	{
		if(isCursedWeaponEquipped())
			return;

		switch(npcId)
		{
			case 0: // Dismount
				setFlying(false);
				setRiding(false);
				if(getTransformation() > 0)
					setTransformation(0);
				removeSkillById(L2Skill.SKILL_STRIDER_ASSAULT);
				removeSkillById(L2Skill.SKILL_WYVERN_BREATH);
				getEffectList().stopEffect(L2Skill.SKILL_HINDER_STRIDER);
				break;
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
				setRiding(true);
				if(isNoble())
					addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_STRIDER_ASSAULT, 1), false);
				break;
			case PetDataTable.WYVERN_ID:
				setFlying(true);
				setLoc(getLoc().changeZ(32));
				addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_WYVERN_BREATH, 1), false);
				break;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
			case PetDataTable.LIGHT_PURPLE_MANED_HORSE_ID:
				setRiding(true);
				break;
			case PetDataTable.KNIGHT_HORSE_ID:
			case PetDataTable.WARRIOR_HORSE_ID:
			case PetDataTable.RUSTY_STEEL_HORSE_ID:
			case PetDataTable.ARCHER_HORSE_ID:
			case PetDataTable.PHANTOM_HORSE_ID:
			case PetDataTable.COBALT_HORSE_ID:
			case PetDataTable.ENCHANTER_HORSE_ID:
			case PetDataTable.HEALER_HORSE_ID:
				setRiding(true);
				break;
			case PetDataTable.ANT_PRINCESS_ID:
			case PetDataTable.HALLOWEEM_FLYING_BROOM_ID:
			case PetDataTable.TAWNY_MANED_LION_ID:
				setRiding(true);
				break;
			case PetDataTable.STEAM_BEATLE_ID:
				setRiding(true);
				break;
			case PetDataTable.AURA_BIRD_FALCON_ID:
				setLoc(getLoc().changeZ(32));
				setFlying(true);
				setTransformation(8);
				break;
			case PetDataTable.AURA_BIRD_OWL_ID:
				setLoc(getLoc().changeZ(32));
				setFlying(true);
				setTransformation(9);
				break;
		}

		if(npcId > 0)
			unEquipWeapon();

		_mountNpcId = npcId;
		_mountObjId = obj_id;
		_mountLevel = level;

		broadcastUserInfo(true); // нужно послать пакет перед Ride для корректного снятия оружия с заточкой
		broadcastPacket(new Ride(this));
		broadcastUserInfo(true); // нужно послать пакет после Ride для корректного отображения скорости

		sendPacket(new SkillList(this));
	}

	public void unEquipWeapon()
	{
		L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(wpn != null)
			sendDisarmMessage(wpn);
		getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);

		wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if(wpn != null)
			sendDisarmMessage(wpn);
		getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_RHAND);

		checkGradeExpertiseUpdate();
		abortAttack(true, true);
		abortCast(true);
	}

	/*
	@Override
	public float getMovementSpeedMultiplier()
	{
		int template_speed = _template.baseRunSpd;
		if(isMounted())
		{
			L2PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
			if(petData != null)
				template_speed = petData.getSpeed();
		}
		return getRunSpeed() * 1f / template_speed;
	}
	*/

	@Override
	public int getSpeed(int baseSpeed)
	{
		if(isMounted())
		{
			L2PetData petData = PetDataTable.getInstance().getInfo(_mountNpcId, _mountLevel);
			int speed = 187;
			if(petData != null)
				speed = petData.getSpeed();
			double mod = 1.;
			int level = getLevel();
			if(_mountLevel > level && level - _mountLevel > 10)
				mod = 0.5; // Штраф на разницу уровней между игроком и петом
			baseSpeed = (int) (mod * speed);
		}
		return super.getSpeed(baseSpeed);
	}

	private int _mountNpcId;
	private int _mountObjId;
	private int _mountLevel;

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

	public void sendDisarmMessage(L2ItemInstance wpn)
	{
		if(wpn.getEnchantLevel() > 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.EQUIPMENT_OF__S1_S2_HAS_BEEN_REMOVED);
			sm.addNumber(wpn.getEnchantLevel());
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1__HAS_BEEN_DISARMED);
			sm.addItemName(wpn.getItemId());
			sendPacket(sm);
		}
	}

	/**
	 * Send a Server->Client packet UserInfo to this L2Player and CharInfo to all L2Player in its _KnownPlayers.
	 */
	@Override
	public void updateAbnormalEffect()
	{
		sendChanges();
	}

	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.
	 */
	public void tempInventoryDisable()
	{
		_inventoryDisable = true;
		ThreadPoolManager.getInstance().scheduleAi(new InventoryEnableTask(this), 1500, true);
	}

	/**
	 * @return True if the Inventory is disabled.<BR><BR>
	 */
	public boolean isInventoryDisabled()
	{
		return _inventoryDisable;
	}

	/**
	 * Устанавливает тип используемого склада.
	 * @param type тип склада:<BR>
	 * <ul>
	 * <li>WarehouseType.PRIVATE
	 * <li>WarehouseType.CLAN
	 * <li>WarehouseType.CASTLE
	 * <li>WarehouseType.FREIGHT
	 * </ul>
	 */
	public void setUsingWarehouseType(final WarehouseType type)
	{
		_usingWHType = type;
	}

	/**
	 * Возвращает тип используемого склада.
	 * @return null или тип склада:<br>
	 * <ul>
	 * <li>WarehouseType.PRIVATE
	 * <li>WarehouseType.CLAN
	 * <li>WarehouseType.CASTLE
	 * <li>WarehouseType.FREIGHT
	 * </ul>
	 */
	public WarehouseType getUsingWarehouseType()
	{
		return _usingWHType;
	}

	public GCSArray<L2CubicInstance> getCubics()
	{
		return _cubics == null ? new GCSArray<L2CubicInstance>(0) : _cubics;
	}

	public void addCubic(int id, int level, int lifetime, boolean givenByOther)
	{
		if(_cubics != null)
			for(L2CubicInstance old : _cubics)
				if(old.getId() == id)
					old.deleteMe(false);
		if(_cubics == null)
			_cubics = new GCSArray<L2CubicInstance>(4);
		int mastery = Math.max(0, getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY));
		if(_cubics.size() > mastery)
		{
			sendPacket(Msg.CUBIC_SUMMONING_FAILED);
			return;
		}
		_cubics.add(new L2CubicInstance(this, id, level, lifetime, givenByOther));
	}

	public void delCubic(L2CubicInstance cubic)
	{
		if(_cubics != null)
			_cubics.remove(cubic);
	}

	public L2CubicInstance getCubic(int id)
	{
		if(_cubics != null)
			for(L2CubicInstance cubic : _cubics)
				if(cubic.getId() == id)
					return cubic;
		return null;
	}

	@Override
	public String toString()
	{
		return "player '" + getName() + "'";
	}

	/**
	 * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		final L2ItemInstance wpn = getActiveWeaponInstance();

		if(wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	/**
	 * Set the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public void setLastNpc(final L2NpcInstance npc)
	{
		_lastNpc = npc;
	}

	/**
	 * @return the _lastFolkNpc of the L2Player corresponding to the last Folk witch one the player talked.<BR><BR>
	 */
	public L2NpcInstance getLastNpc()
	{
		return _lastNpc;
	}

	public void setLastBbsOperaion(final String operaion)
	{
		_lastBBS_script_operation = operaion;
	}

	public String getLastBbsOperaion()
	{
		return _lastBBS_script_operation;
	}

	public void setMultisell(MultiSellListContainer multisell)
	{
		_multisell = multisell;
	}

	public MultiSellListContainer getMultisell()
	{
		return _multisell;
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(spirit)
			weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
		else
			weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

		AutoShot();
		return true;
	}

	public boolean unChargeFishShot()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
			return false;
		weapon.setChargedFishshot(false);
		AutoShot();
		return true;
	}

	public void AutoShot()
	{
		synchronized (_activeSoulShots)
		{
			for(Integer e : _activeSoulShots)
			{
				if(e == null)
					continue;
				L2ItemInstance item = getInventory().getItemByItemId(e);
				if(item == null)
				{
					_activeSoulShots.remove(e);
					continue;
				}
				IItemHandler handler = ItemHandler.getInstance().getItemHandler(e);
				if(handler == null)
					continue;
				handler.useItem(this, item, false);
			}
		}
	}

	public boolean getChargedFishShot()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedFishshot();
	}

	@Override
	public boolean getChargedSoulShot()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT;
	}

	@Override
	public int getChargedSpiritShot()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if(weapon == null)
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

	public ConcurrentSkipListSet<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	public void setInvisible(boolean vis)
	{
		_invisible = vis;
	}

	@Override
	public boolean isInvisible()
	{
		return _invisible;
	}

	public int getClanPrivileges()
	{
		if(_clan == null)
			return 0;
		if(isClanLeader())
			return L2Clan.CP_ALL;
		if(_powerGrade < 1 || _powerGrade > 9)
			return 0;
		RankPrivs privs = _clan.getRankPrivs(_powerGrade);
		if(privs != null)
			return privs.getPrivs();
		return 0;
	}

	public boolean enterObserverMode(Location loc)
	{
		_observNeighbor = L2World.getRegion(loc, 0);
		if(_observNeighbor == null)
			return false;

		setTarget(null);
		stopMove();
		sitDown();
		block();

		_observerMode = 1;

		// Отображаем надпись над головой
		broadcastCharInfo();

		// Переходим в режим обсервинга
		sendPacket(new ObserverStart(loc));

		return true;
	}

	public void appearObserverMode()
	{
		L2WorldRegion observNeighbor = _observNeighbor;
		L2WorldRegion currentRegion = getCurrentRegion();
		if(observNeighbor == null || currentRegion == null)
		{
			if(getOlympiadObserveId() == -1)
				leaveObserverMode();
			else
				leaveOlympiadObserverMode();
			return;
		}

		_observerMode = 3;

		// Очищаем все видимые обьекты
		for(L2WorldRegion neighbor : currentRegion.getNeighbors())
			neighbor.removeObjectsFromPlayer(this);

		// Добавляем фэйк в точку наблюдения
		if(!_observNeighbor.equals(currentRegion))
			_observNeighbor.addObject(this);

		// Показываем чару все обьекты, что находятся в точке наблюдения и соседних регионах
		for(L2WorldRegion neighbor : _observNeighbor.getNeighbors())
			neighbor.showObjectsToPlayer(this);

		if(getOlympiadObserveId() > -1)
		{
			OlympiadGame game = Olympiad.getOlympiadGame(getOlympiadObserveId());
			if(game != null)
				game.broadcastInfo(null, this, true);
		}
	}

	public void returnFromObserverMode()
	{
		_observerMode = 0;
		_observNeighbor = null;
		_olympiadObserveId = -1;
		setIsInvul(false);  
		setInvisible(false);  
		sendUserInfo(true); 
		

		L2WorldRegion currentRegion = getCurrentRegion();

		// Показываем чару все обьекты, что находятся в точке воврата и соседних регионах
		if(currentRegion != null)
			for(L2WorldRegion neighbor : currentRegion.getNeighbors())
				neighbor.showObjectsToPlayer(this);

		broadcastUserInfo(true);
	}

	public void leaveObserverMode()
	{
		L2WorldRegion observNeighbor = _observNeighbor;

		// Удаляем фэйк из точки наблюдения и удаляем у чара все обьекты, что там находятся
		if(observNeighbor != null)
			for(L2WorldRegion neighbor : observNeighbor.getNeighbors())
			{
				neighbor.removeObjectsFromPlayer(this);
				neighbor.removeObject(this, false);
			}

		// Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
		setLastClientPosition(null);
		setLastServerPosition(null);

		_observNeighbor = null;
		_observerMode = 2;

		setTarget(null);
		setIsInvul(false);  
		setInvisible(false);  
		sendUserInfo(true);       	
		unblock();
		standUp();

		// Выходим из режима обсервинга
		sendPacket(new ObserverEnd(this));
	}

	public void enterOlympiadObserverMode(Location loc, int id)
	{
		_observNeighbor = L2World.getRegion(loc, 0);
		if(_observNeighbor == null)
			return;

		setTarget(null);
		setIsInvul(true);  
		setInvisible(true);  
		if (getCurrentRegion() != null)  
			for (L2WorldRegion neighbor : getCurrentRegion().getNeighbors())  
				neighbor.removePlayerFromOtherPlayers(this);              
		sendUserInfo(true);  
		block();

		_olympiadObserveId = id;
		_observerMode = 1;

		// Отображаем надпись над головой
		//broadcastCharInfo();

		// Меняем интерфейс
		sendPacket(new ExOlympiadMode(3));

		// Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
		setLastClientPosition(null);
		setLastServerPosition(null);

		// "Телепортируемся"
		sendPacket(new TeleportToLocation(this, loc));
	}

	public void switchOlympiadObserverArena(int id)
	{
		L2WorldRegion observNeighbor = _observNeighbor;

		// Удаляем фэйк из точки наблюдения и удаляем у чара все обьекты, что там находятся
		if(observNeighbor != null)
			for(L2WorldRegion neighbor : observNeighbor.getNeighbors())
			{
				neighbor.removeObjectsFromPlayer(this);
				neighbor.removeObject(this, false);
			}

		int oldId = _olympiadObserveId;

		_observNeighbor = null;
		_observerMode = 0;
		_olympiadObserveId = -1;

		setTarget(null);
		//stopMove();
		setIsInvul(false);
		setInvisible(false);
		sendUserInfo(true);
		unblock();

		// Меняем интерфейс
		sendPacket(new ExOlympiadMode(0));
		sendPacket(new ExOlympiadMatchEnd());

		Olympiad.removeSpectator(oldId, this);
		Olympiad.addSpectator(id, this);
	}

	public void leaveOlympiadObserverMode()
	{
		L2WorldRegion observNeighbor = _observNeighbor;

		// Удаляем фэйк из точки наблюдения и удаляем у чара все обьекты, что там находятся
		if(observNeighbor != null)
			for(L2WorldRegion neighbor : observNeighbor.getNeighbors())
			{
				neighbor.removeObjectsFromPlayer(this);
				neighbor.removeObject(this, false);
			}

		_observNeighbor = null;
		_observerMode = 2;
		_olympiadGameId = -1;		

		setTarget(null);
		setIsInvul(false);  
		setInvisible(false);  
		sendUserInfo(true);        
		
		unblock();

		Olympiad.removeSpectator(_olympiadObserveId, this);
		_olympiadObserveId = -1;

		// Меняем интерфейс
		sendPacket(new ExOlympiadMode(0));
		sendPacket(new ExOlympiadMatchEnd());

		// Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
		setLastClientPosition(null);
		setLastServerPosition(null);

		// "Телепортируемся"
		sendPacket(new TeleportToLocation(this, getLoc()));
	}

	public void setOlympiadSide(final int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	public void setOlympiadGameId(final int id)
	{
		_olympiadGameId = id;
	}

	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}

	public int getOlympiadObserveId()
	{
		return _olympiadObserveId;
	}

	public Location getObsLoc()
	{
		return _obsLoc;
	}

	@Override
	public boolean inObserverMode()
	{
		return _observerMode > 0;
	}

	public byte getObserverMode()
	{
		return _observerMode;
	}

	public void setObserverMode(byte mode)
	{
		_observerMode = mode;
	}

	public L2WorldRegion getObservNeighbor()
	{
		return _observNeighbor;
	}

	public void setObservNeighbor(L2WorldRegion region)
	{
		_observNeighbor = region;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(final int mode)
	{
		_telemode = mode;
	}

	public void setLoto(final int i, final int val)
	{
		_loto[i] = val;
	}

	public int getLoto(final int i)
	{
		return _loto[i];
	}

	public void setRace(final int i, final int val)
	{
		_race[i] = val;
	}

	public int getRace(final int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(final boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}

	public void setTradeRefusal(final boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void setExchangeRefusal(final boolean mode)
	{
		_exchangeRefusal = mode;
	}

	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}

	public void addToBlockList(final String charName)
	{
		if(charName == null || charName.equalsIgnoreCase(getName()) || isInBlockList(charName))
		{
			// уже в списке
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		L2Player block_target = L2World.getPlayer(charName);

		if(block_target != null)
		{
			if(block_target.isGM())
			{
				sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
				return;
			}
			_blockList.put(block_target.getObjectId(), block_target.getName());
			sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(block_target.getName()));
			block_target.sendPacket(new SystemMessage(SystemMessage.S1__HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST).addString(getName()));
			return;
		}

		// чар не в игре
		int charId = Util.GetCharIDbyName(charName);

		if(charId == 0)
		{
			// чар не существует
			sendPacket(Msg.YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST);
			return;
		}

		if(Config.gmlist.containsKey(charId) && Config.gmlist.get(charId).IsGM)
		{
			sendPacket(Msg.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM);
			return;
		}
		_blockList.put(charId, charName);
		sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST).addString(charName));
	}

	public void removeFromBlockList(final String charName)
	{
		int charId = 0;
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
			{
				charId = blockId;
				break;
			}
		if(charId == 0)
		{
			sendPacket(Msg.YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_FROM_IGNORE_LIST);
			return;
		}
		sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST).addString(_blockList.remove(charId)));
		L2Player block_target = L2ObjectsStorage.getPlayer(charId);
		if(block_target != null)
			block_target.sendMessage(getName() + " has removed you from his/her Ignore List."); //В системных(619 == 620) мессагах ошибка ;)
	}

	public boolean isInBlockList(final L2Player player)
	{
		return isInBlockList(player.getObjectId());
	}

	public boolean isInBlockList(final int charId)
	{
		return _blockList != null && _blockList.containsKey(charId);
	}

	public boolean isInBlockList(final String charName)
	{
		for(int blockId : _blockList.keySet())
			if(charName.equalsIgnoreCase(_blockList.get(blockId)))
				return true;
		return false;
	}

	private void restoreBlockList()
	{
		_blockList.clear();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT target_Id, char_name FROM character_blocklist LEFT JOIN characters ON ( character_blocklist.target_Id = characters.obj_Id ) WHERE character_blocklist.obj_Id = ?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
				_blockList.put(rs.getInt("target_Id"), rs.getString("char_name"));
		}
		catch(SQLException e)
		{
			_log.warning("Can't restore player blocklist " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private void storeBlockList()
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM character_blocklist WHERE obj_Id=" + getObjectId());

			if(_blockList.isEmpty())
				return;

			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `character_blocklist` (`obj_Id`,`target_Id`) VALUES");

			synchronized (_blockList)
			{
				StringBuilder sb;
				for(Entry<Integer, String> e : _blockList.entrySet())
				{
					sb = new StringBuilder("(");
					sb.append(getObjectId()).append(",");
					sb.append(e.getKey()).append(")");
					b.write(sb.toString());
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.warning("Can't store player blocklist " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(final boolean state)
	{
		_blockAll = state;
		sendPacket(new EtcStatusUpdate(this));
	}

	public Collection<String> getBlockList()
	{
		return _blockList.values();
	}

	public void setConnected(boolean connected)
	{
		_isConnected = connected;
	}

	public boolean isConnected()
	{
		return _isConnected;
	}

	public void setHero(final boolean hero)
	{
		_hero = hero;
	}

	@Override
	public boolean isHero()
	{
		return _hero;
	}

	public void setIsInOlympiadMode(final boolean b)
	{
		_inOlympiadMode = b;
	}

	@Override
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public boolean isOlympiadGameStart()
	{
		int id = _olympiadGameId;
		if(id < 0)
			return false;
		OlympiadGame og = Olympiad.getOlympiadGame(id);
		return og != null && og.getState() == 1;
	}

	public boolean isOlympiadCompStart()
	{
		int id = _olympiadGameId;
		if(id < 0)
			return false;
		OlympiadGame og = Olympiad.getOlympiadGame(id);
		return og != null && og.getState() == 2;
	}

	public void updateNobleSkills()
	{
		if(isNoble())
		{
			if(isClanLeader() && getClan().getHasCastle() > 0)
				super.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_WYVERN_AEGIS, 1));
			super.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_NOBLESSE_BLESSING, 1));
			super.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_SUMMON_CP_POTION, 1));
			super.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_FORTUNE_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_HARMONY_OF_NOBLESSE, 1));
			super.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_SYMPHONY_OF_NOBLESSE, 1));
		}
		else
		{
			super.removeSkillById(L2Skill.SKILL_WYVERN_AEGIS);
			super.removeSkillById(L2Skill.SKILL_NOBLESSE_BLESSING);
			super.removeSkillById(L2Skill.SKILL_SUMMON_CP_POTION);
			super.removeSkillById(L2Skill.SKILL_FORTUNE_OF_NOBLESSE);
			super.removeSkillById(L2Skill.SKILL_HARMONY_OF_NOBLESSE);
			super.removeSkillById(L2Skill.SKILL_SYMPHONY_OF_NOBLESSE);
		}
	}

	public void setNoble(boolean noble)
	{
		_noble = noble;
	}

	public boolean isNoble()
	{
		return true;
	}

	public int getSubLevel()
	{
		return isSubClassActive() ? getLevel() : 0;
	}

	/* varka silenos and ketra orc quests related functions */
	public void updateKetraVarka()
	{
		if(Functions.getItemCount(this, 7215) > 0)
			_ketra = 5;
		else if(Functions.getItemCount(this, 7214) > 0)
			_ketra = 4;
		else if(Functions.getItemCount(this, 7213) > 0)
			_ketra = 3;
		else if(Functions.getItemCount(this, 7212) > 0)
			_ketra = 2;
		else if(Functions.getItemCount(this, 7211) > 0)
			_ketra = 1;
		else if(Functions.getItemCount(this, 7225) > 0)
			_varka = 5;
		else if(Functions.getItemCount(this, 7224) > 0)
			_varka = 4;
		else if(Functions.getItemCount(this, 7223) > 0)
			_varka = 3;
		else if(Functions.getItemCount(this, 7222) > 0)
			_varka = 2;
		else if(Functions.getItemCount(this, 7221) > 0)
			_varka = 1;
		else
		{
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
		if(Functions.getItemCount(this, 7247) > 0)
			_ram = 2;
		else if(Functions.getItemCount(this, 7246) > 0)
			_ram = 1;
		else
			_ram = 0;
	}

	public int getRam()
	{
		return _ram;
	}

	public void setPledgeType(final int typeId)
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

	public void setPledgeClass(final int classId)
	{
		_pledgeClass = classId;
	}

	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public void updatePledgeClass()
	{
		byte CLAN_LEVEL = _clan == null ? -1 : _clan.getLevel();
		boolean IN_ACADEMY = _clan != null && _clan.isAcademy(_pledgeType);
		boolean IS_GUARD = _clan != null && _clan.isRoyalGuard(_pledgeType);
		boolean IS_KNIGHT = _clan != null && _clan.isOrderOfKnights(_pledgeType);
		boolean IS_GUARD_CAPTAIN = false;
		boolean IS_KNIGHT_COMMANDER = false;
		if(_clan != null && _pledgeType == 0)
		{
			int leaderOf = _clan.getClanMember(_objectId).isSubLeader();
			if(_clan.isRoyalGuard(leaderOf))
				IS_GUARD_CAPTAIN = true;
			else if(_clan.isOrderOfKnights(leaderOf))
				IS_KNIGHT_COMMANDER = true;
		}

		switch(CLAN_LEVEL)
		{
			case -1:
				_pledgeClass = RANK_VAGABOND;
				break;
			case 0:
			case 1:
			case 2:
			case 3:
				if(isClanLeader())
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_VASSAL;
				break;
			case 4:
				if(isClanLeader())
					_pledgeClass = RANK_KNIGHT;
				else
					_pledgeClass = RANK_HEIR;
				break;
			case 5:
				if(isClanLeader())
					_pledgeClass = RANK_WISEMAN;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else
					_pledgeClass = RANK_HEIR;
				break;
			case 6:
				if(isClanLeader())
					_pledgeClass = RANK_BARON;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_WISEMAN;
				else if(IS_GUARD)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_KNIGHT;
				break;
			case 7:
				if(isClanLeader())
					_pledgeClass = RANK_COUNT;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_GUARD)
					_pledgeClass = RANK_KNIGHT;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_BARON;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_HEIR;
				else
					_pledgeClass = RANK_WISEMAN;
				break;
			case 8:
				if(isClanLeader())
					_pledgeClass = RANK_MARQUIS;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_COUNT;
				else if(IS_GUARD)
					_pledgeClass = RANK_WISEMAN;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_KNIGHT;
				else
					_pledgeClass = RANK_BARON;
				break;
			case 9:
				if(isClanLeader())
					_pledgeClass = RANK_DUKE;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_MARQUIS;
				else if(IS_GUARD)
					_pledgeClass = RANK_BARON;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_COUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_WISEMAN;
				else
					_pledgeClass = RANK_VISCOUNT;
				break;
			case 10:
				if(isClanLeader())
					_pledgeClass = RANK_GRAND_DUKE;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_BARON;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_DUKE;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_MARQUIS;
				else
					_pledgeClass = RANK_COUNT;
				break;
			case 11:
				if(isClanLeader())
					_pledgeClass = RANK_DISTINGUISHED_KING;
				else if(IN_ACADEMY)
					_pledgeClass = RANK_VASSAL;
				else if(IS_GUARD)
					_pledgeClass = RANK_COUNT;
				else if(IS_KNIGHT)
					_pledgeClass = RANK_VISCOUNT;
				else if(IS_GUARD_CAPTAIN)
					_pledgeClass = RANK_GRAND_DUKE;
				else if(IS_KNIGHT_COMMANDER)
					_pledgeClass = RANK_DUKE;
				else
					_pledgeClass = RANK_MARQUIS;
				break;
		}

		if(_hero && _pledgeClass < RANK_MARQUIS)
			_pledgeClass = RANK_MARQUIS;
		else if(_noble && _pledgeClass < RANK_BARON)
			_pledgeClass = RANK_BARON;
	}

	public void setPowerGrade(final int grade)
	{
		_powerGrade = grade;
	}

	public int getPowerGrade()
	{
		return _powerGrade;
	}

	public void setApprentice(final int apprentice)
	{
		_apprentice = apprentice;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public int getSponsor()
	{
		return _clan == null ? 0 : _clan.getClanMember(getObjectId()).getSponsor();
	}

	public void setTeam(final int team, boolean checksForTeam)
	{
		_checksForTeam = checksForTeam;
		if(_team != team)
		{
			_team = team;

			broadcastUserInfo(true);
			if(getPet() != null)
				getPet().broadcastPetInfo();
		}
	}

	@Override
	public int getTeam()
	{
		return _team;
	}

	public boolean isChecksForTeam()
	{
		return _checksForTeam;
	}

	public int getNameColor()
	{
		if(inObserverMode())
			return Color.black.getRGB();

		return _nameColor;
	}

	public void setNameColor(final int nameColor)
	{
		if(nameColor != Config.NORMAL_NAME_COLOUR && nameColor != Config.CLANLEADER_NAME_COLOUR && nameColor != Config.GM_NAME_COLOUR && nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(nameColor));
		else if(nameColor == Config.NORMAL_NAME_COLOUR)
			unsetVar("namecolor");
		_nameColor = nameColor;
	}

	public void setNameColor(final int red, final int green, final int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
		if(_nameColor != Config.NORMAL_NAME_COLOUR && _nameColor != Config.CLANLEADER_NAME_COLOUR && _nameColor != Config.GM_NAME_COLOUR && _nameColor != Config.SERVICES_OFFLINE_TRADE_NAME_COLOR)
			setVar("namecolor", Integer.toHexString(_nameColor));
		else
			unsetVar("namecolor");
	}

	public final void illegalAction(final String msg, final Integer jail_items)
	{
		Log.IllegalPlayerAction(this, msg, jail_items);
	}

	public final String toFullString()
	{
		final StringBuffer sb = new StringBuffer(160);

		sb.append("Player '").append(getName()).append("' [oid=").append(_objectId).append(", account='").append(getAccountName()).append(", ip=").append(getIP()).append("']");
		return sb.toString();
	}

	private final FastMap<String, String> user_variables = new FastMap<String, String>().setShared(true);

	public void setVar(String name, String value)
	{
		user_variables.put(name, value);
		mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (?,'user-var',?,?,-1)", _objectId, name, value);
	}

	public void unsetVar(String name)
	{
		if(name == null)
			return;

		if(user_variables.remove(name) != null)
			mysql.set("DELETE FROM `character_variables` WHERE `obj_id`=? AND `type`='user-var' AND `name`=? LIMIT 1", _objectId, name);
	}

	public String getVar(String name)
	{
		return user_variables.get(name);
	}

	public boolean getVarB(String name, boolean defaultVal)
	{
		String var = user_variables.get(name);
		if(var == null)
			return defaultVal;
		return !(var.equals("0") || var.equalsIgnoreCase("false"));
	}

	public boolean getVarB(String name)
	{
		String var = user_variables.get(name);
		return !(var == null || var.equals("0") || var.equalsIgnoreCase("false"));
	}

	public FastMap<String, String> getVars()
	{
		return user_variables;
	}

	private void loadVariables()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?");
			offline.setInt(1, _objectId);
			rs = offline.executeQuery();
			while(rs.next())
			{
				String name = rs.getString("name");
				String value = Strings.stripSlashes(rs.getString("value"));
				user_variables.put(name, value);
			}

			// TODO Здесь обязятельно выставлять все стандартные параметры, иначе будут NPE
			if(getVar("lang@") == null)
				setVar("lang@", Config.DEFAULT_LANG);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, offline, rs);
		}
	}

	public String getLang()
	{
		return getVar("lang@");
	}

	public int getLangId()
	{
		String lang = getLang();
		if(lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("e") || lang.equalsIgnoreCase("eng"))
			return LANG_ENG;
		if(lang.equalsIgnoreCase("ru") || lang.equalsIgnoreCase("r") || lang.equalsIgnoreCase("rus"))
			return LANG_RUS;
		return LANG_UNK;
	}

	public boolean isLangRus()
	{
		return getLangId() == LANG_RUS;
	}

	public int isAtWarWith(final Integer id)
	{
		return _clan == null || !_clan.isAtWarWith(id) ? 0 : 1;
	}

	public int isAtWar()
	{
		return _clan == null || _clan.isAtWarOrUnderAttack() <= 0 ? 0 : 1;
	}

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(2, 0));
			sendChanges();
		}
	}

	public void startWaterTask()
	{
		if(isDead())
			stopWaterTask();
		else if(Config.ALLOW_WATER && _taskWater == null)
		{
			int timeinwater = (int) (calcStat(Stats.BREATH, 86, null, null) * 1000L);
			sendPacket(new SetupGauge(2, timeinwater));
			if(getTransformation() > 0 && getTransformationTemplate() > 0 && !isCursedWeaponEquipped())
				setTransformation(0);
			_taskWater = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new WaterTask(this), timeinwater, 1000L, true);
			sendChanges();
		}
	}

	public void checkWaterState()
	{
		if(isInZoneWater())
			startWaterTask();
		else
			stopWaterTask();
	}

	private boolean _reviveRequested = false;
	private double _revivePower = 0;
	private boolean _revivePet = false;

	public void doRevive(double percent)
	{
		restoreExp(percent);
		doRevive();
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		unsetVar("lostexp");
		updateEffectIcons();
		AutoShot();
		_reviveRequested = false;
		_revivePower = 0;
	}

	public void reviveRequest(L2Player Reviver, double percent, boolean Pet)
	{
		if(_reviveRequested)
		{
			if(_revivePet == Pet && _revivePower >= percent)
			{
				Reviver.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
				return;
			}
			if(Pet && !_revivePet)
			{
				Reviver.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
				return;
			}
			if(Pet && isDead())
			{
				Reviver.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER);
				return;
			}
		}
		if(Pet && getPet() != null && getPet().isDead() || !Pet && isDead())
		{
			_reviveRequested = true;
			_revivePower = percent;
			_revivePet = Pet;
			ConfirmDlg pkt = new ConfirmDlg(SystemMessage.S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_WITH_$S2_EXPERIENCE_POINTS_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION, 0, 2);
			pkt.addString(Reviver.getName()).addString(Math.round(_revivePower) + " percent");
			sendPacket(pkt);
		}
	}

	public void reviveAnswer(int answer)
	{
		if(!_reviveRequested || !isDead() && !_revivePet || _revivePet && getPet() != null && !getPet().isDead())
			return;
		if(answer == 1)
			if(!_revivePet)
				doRevive(_revivePower);
			else if(getPet() != null)
				((L2PetInstance) getPet()).doRevive(_revivePower);
		_reviveRequested = false;
		_revivePower = 0;
	}


	private Location _SummonCharacterCoords;

	private int _SummonConsumeCrystall = 0;

	public void summonCharacterAnswer(int answer)
	{
		int summoningCrystallId = 8615;
		if(answer == 1 && _SummonCharacterCoords != null)
		{
			abortAttack(true, true);
			abortCast(true);
			stopMove();
			if(_SummonConsumeCrystall > 0)
			{
				L2ItemInstance ConsumedItem = getInventory().getItemByItemId(summoningCrystallId);
				if(ConsumedItem != null && ConsumedItem.getCount() >= _SummonConsumeCrystall)
				{
					getInventory().destroyItemByItemId(summoningCrystallId, _SummonConsumeCrystall, false);
					sendPacket(SystemMessage.removeItems(summoningCrystallId, _SummonConsumeCrystall));
					teleToLocation(_SummonCharacterCoords);
				}
				else
					sendPacket(Msg.INCORRECT_ITEM_COUNT);
			}
			else
				teleToLocation(_SummonCharacterCoords);
		}
		_SummonCharacterCoords = null;
	}

	public void summonCharacterRequest(String SummonerName, Location loc, int SummonConsumeCrystall)
	{
		if(_SummonCharacterCoords == null)
		{
			_SummonConsumeCrystall = SummonConsumeCrystall;
			_SummonCharacterCoords = loc;
			ConfirmDlg cd = new ConfirmDlg(SystemMessage.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT, 60000, 1);
			cd.addString(SummonerName).addZoneName(_SummonCharacterCoords);
			sendPacket(cd);
		}
	}

	String _scriptName = "";
	Object[] _scriptArgs = new Object[0];

	public void scriptAnswer(int answer)
	{
		if(answer == 1 && !_scriptName.equals(""))
			callScripts(_scriptName.split(":")[0], _scriptName.split(":")[1], _scriptArgs);
		_scriptName = "";
	}

	public void scriptRequest(String text, String scriptName, Object[] args)
	{
		if(_scriptName.equals(""))
		{
			_scriptName = scriptName;
			_scriptArgs = args;
			sendPacket(new ConfirmDlg(SystemMessage.S1, 30000, 3).addString(text));
		}
	}

	public boolean isReviveRequested()
	{
		return _reviveRequested;
	}

	public boolean isRevivingPet()
	{
		return _revivePet;
	}

	public void updateNoChannel(final long time)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		setNoChannel(time);

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			final String stmt = "UPDATE characters SET nochannel = ? WHERE obj_Id=?";
			statement = con.prepareStatement(stmt);
			statement.setLong(1, _NoChannel > 0 ? _NoChannel / 1000 : _NoChannel);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.warning("Could not activate nochannel:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

    public void checkRecom() {
        Calendar temp = Calendar.getInstance();
        temp.set(Calendar.HOUR_OF_DAY, 6);
        temp.set(Calendar.MINUTE, 30);
        temp.set(Calendar.SECOND, 0);
        long count = Math.round((System.currentTimeMillis() / 1000 - _lastAccess) / 86400);
        if (count == 0 && _lastAccess < temp.getTimeInMillis() / 1000 && System.currentTimeMillis() > temp.getTimeInMillis())
            count++;
        if(count != 0) {
            setRecomLeft(20);
            setRecomTimeLeft(3600);
            int have = getRecomHave();
            for (int i = 1; i < count; i++)
                have -= 20;
            if(have < 0)
                have = 0;
            setRecomHave(have);
        }
        updateVoteInfo();
    }

	public void restartRecom()
	{
		try
		{
			if(getLevel() < 20)
				_recomLeft = 3;
			else if(getLevel() < 40)
				_recomLeft = 6;
			else
				_recomLeft = 9;

			if(_recomHave < 200)
				_recomHave -= 2;
			else
				_recomHave -= 3;

			if(_recomHave < 0)
				_recomHave = 0;
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
	}
    private void setRecomTimeLeft(int rec_timeleft) {
        recomTimeLeft = rec_timeleft;
    }

    /**
     * Расчитывает и сохраняет время до конца бонуса
     * @return оставшееся время
     */
    public int getRecomTimeLeft() {
        long currTime = System.currentTimeMillis();
        if(!isRecomSupportTime() && recSupportTime > 0) { // образуется тогда, когда мы отменяем эффект от часов
            int t = recomTimeLeft - (int) (currTime - lastCheckBonusTime - (currTime - recSupportTime)) / 1000;
            setRecomSupportTime(0, false);
            setRecomTimeLeft(t);
            lastCheckBonusTime = currTime;
            return t >= 0 ? t : 0;
        }
        if(lastCheckBonusTime == 0)
            lastCheckBonusTime = currTime;
        recomTimeLeft = recomTimeLeft - (int) (currTime - lastCheckBonusTime)/1000;
        lastCheckBonusTime = currTime;
        return recomTimeLeft >= 0 ? recomTimeLeft : 0;
    }

    public int getRecomExpBonus() {
        if (getRecomTimeLeft() <= 0 && !isRecomSupportTime())
            return 0;
        if (getLevel() < 1)
            return 0;
        if (getRecomHave() < 1)
            return 0;
        if (getRecomHave() >= 100)
            return 50;
        int arg1 = (int) Math.floor(getLevel() / 10);
        int arg2 = (int) Math.floor(getRecomHave() / 10);
        return Constants.REC_BONUS[arg1][arg2];
    }

    public void startRecomendationTask() {
        if(getOnlineTime() <=0)
            recVoteTask = ThreadPoolManager.getInstance().scheduleGeneral(new RecVoteTask(true, 10), 3600*1000);
        else
            recVoteTask = ThreadPoolManager.getInstance().scheduleGeneral(new RecVoteTask(false, 1), 3600*1000);
    }

    public class RecVoteTask implements Runnable {
        boolean created;
        int rec2Add;

        public RecVoteTask(boolean b, int i) {
            created = b;
            rec2Add = i;
        }

        @Override
        public void run() {
            addRecomLeft(rec2Add);
            if (created)
                ThreadPoolManager.getInstance().scheduleGeneral(new RecVoteTask(false, 10), 3600 * 1000);
            else
                ThreadPoolManager.getInstance().scheduleGeneral(new RecVoteTask(false, 1), 3600 * 1000);
        }
    }
        public int getRecomHave() {
        return _recomHave;
    }

    public void setRecomHave(int value) {
        if (value > 255)
            _recomHave = 255;
        else if (value < 0)
            _recomHave = 0;
        else
            _recomHave = value;
    }

    public void addRecomLeft(int recLeft) {
        setRecomLeft(getRecomLeft() + recLeft);
        updateVoteInfo();
    }

    public void updateVoteInfo() {
        sendPacket(new ExVoteSystemInfo(this));
    }

    public void giveRecom(final L2Player target) {
        int targetRecom = target.getRecomHave();
        if (targetRecom < 255)
            target.setRecomHave(targetRecom + 1);
        if (_recomLeft > 0)
            _recomLeft--;
    }

    public boolean isRecomSupportTime() {
        return isRecSupportTime;
    }

    public void setRecomSupportTime(long startTime, boolean isSuppTime) {
        isRecSupportTime = isSuppTime;
        if(startTime != -1)
             recSupportTime = startTime;
        updateVoteInfo();
    }

	@Override
	public boolean isInVehicle()
	{
		return _vehicle != null;
	}

	public L2Vehicle getVehicle()
	{
		return _vehicle;
	}

	public void setVehicle(L2Vehicle boat)
	{
		_vehicle = boat;
	}

	public Location getInVehiclePosition()
	{
		return _inVehiclePosition;
	}

	public void setInVehiclePosition(Location loc)
	{
		_inVehiclePosition = loc;
	}

	public HashMap<Integer, L2SubClass> getSubClasses()
	{
		return _classlist;
	}

	public void setBaseClass(final int baseClass)
	{
		_baseClass = baseClass;
	}

	public int getBaseClassId()
	{
		return _baseClass;
	}

	public void setActiveClass(L2SubClass activeClass)
	{
		if(activeClass == null)
		{
			System.out.print("WARNING! setActiveClass(null);");
			Thread.dumpStack();
		}
		_activeClass = activeClass;
	}

	public L2SubClass getActiveClass()
	{
		return _activeClass;
	}

	public int getActiveClassId()
	{
		return getActiveClass().getClassId();
	}

	/**
	 * Changing index of class in DB, used for changing class when finished professional quests
	 * @param oldclass
	 * @param newclass
	 */
	public synchronized void changeClassInDb(final int oldclass, final int newclass)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("UPDATE character_subclasses SET class_id=? WHERE char_obj_id=? AND class_id=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("UPDATE character_hennas SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("UPDATE character_shortcuts SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("UPDATE character_skills SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("UPDATE character_effects_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("UPDATE character_skills_save SET class_index=? WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, newclass);
			statement.setInt(2, getObjectId());
			statement.setInt(3, oldclass);
			statement.executeUpdate();
			DatabaseUtils.closeStatement(statement);
		}
		catch(final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Сохраняет информацию о классах в БД
	 */
	public void storeCharSubClasses()
	{
		L2SubClass main = getActiveClass();
		if(main != null)
		{
			main.setCp(getCurrentCp());
			//main.setExp(getExp());
			//main.setLevel(getLevel());
			//main.setSp(getSp());
			main.setHp(getCurrentHp());
			main.setMp(getCurrentMp());
			main.setActive(true);
			getSubClasses().put(getActiveClassId(), main);
		}
		else
			_log.warning("Could not store char sub data, main class " + getActiveClassId() + " not found for " + this);

		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();

			StringBuilder sb;
			for(L2SubClass subClass : getSubClasses().values())
			{
				sb = new StringBuilder("UPDATE character_subclasses SET ");
				sb.append("exp=").append(subClass.getExp()).append(",");
				sb.append("sp=").append(subClass.getSp()).append(",");
				sb.append("curHp=").append(subClass.getHp()).append(",");
				sb.append("curMp=").append(subClass.getMp()).append(",");
				sb.append("curCp=").append(subClass.getCp()).append(",");
				sb.append("level=").append(subClass.getLevel()).append(",");
				sb.append("active=").append(subClass.isActive() ? 1 : 0).append(",");
				sb.append("isBase=").append(subClass.isBase() ? 1 : 0).append(",");
				sb.append("death_penalty=").append(subClass.getDeathPenalty().getLevelOnSaveDB()).append(",");
				sb.append("skills='").append(subClass.getSkills()).append("',");
				sb.append("AwakingId=").append(subClass.getAwakingId()).append(",");
				sb.append("dualClass=").append(subClass.getDualClass());
				sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND class_id=").append(subClass.getClassId()).append(" LIMIT 1");
				statement.executeUpdate(sb.toString());
			}

			sb = new StringBuilder("UPDATE LOW_PRIORITY character_subclasses SET ");
			sb.append("maxHp=").append(getMaxHp()).append(",");
			sb.append("maxMp=").append(getMaxMp()).append(",");
			sb.append("maxCp=").append(getMaxCp());
			sb.append(" WHERE char_obj_id=").append(getObjectId()).append(" AND active=1 LIMIT 1");
			statement.executeUpdate(sb.toString());
		}
		catch(final Exception e)
		{
			_log.warning("Could not store char sub data: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Restore list of character professions and set up active proof
	 * Used when character is loading
	 */
	public static void restoreCharSubClasses(final L2Player player)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT class_id,exp,sp,level,curHp,curCp,curMp,active,isBase,death_penalty,skills,AwakingId,dualClass FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final L2SubClass subClass = new L2SubClass();
				subClass.setBase(rset.getInt("isBase") != 0);
				subClass.setClassId(rset.getShort("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setHp(rset.getDouble("curHp"));
				subClass.setMp(rset.getDouble("curMp"));
				subClass.setCp(rset.getDouble("curCp"));
				subClass.setActive(rset.getInt("active") != 0);
				subClass.setDeathPenalty(new DeathPenalty(player, rset.getByte("death_penalty")));
				subClass.setSkills(rset.getString("skills"));
				subClass.setAwakingId(rset.getInt("AwakingId"));
				subClass.setDualClass(rset.getInt("dualClass"));
				subClass.setPlayer(player);

				player.getSubClasses().put(subClass.getClassId(), subClass);
			}

			if(player.getSubClasses().size() == 0)
				throw new Exception("There are no one subclass for player: " + player);

			int BaseClassId = player.getBaseClassId();
			if(BaseClassId == -1)
				throw new Exception("There are no base subclass for player: " + player);

			for(L2SubClass subClass : player.getSubClasses().values())
				if(subClass.isActive())
				{
					player.setActiveSubClass(subClass.getClassId(), false);
					break;
				}

			if(player.getActiveClass() == null)
			{
				//если из-за какого-либо сбоя ни один из сабкласов не отмечен как активный помечаем базовый как активный
				final L2SubClass subClass = player.getSubClasses().get(BaseClassId);
				subClass.setActive(true);
				player.setActiveSubClass(subClass.getClassId(), false);
			}
		}
		catch(final Exception e)
		{
			_log.warning("Could not restore char sub-classes: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Добавить класс, используется только для сабклассов
	 * @param storeOld
	 */
	public boolean addSubClass(final int classId, boolean storeOld)
	{
		if(_classlist.size() >= (4+ Config.ALT_GAME_SUB_ADD))
			return false;

		final ClassId newId = ClassId.values()[classId];

		final L2SubClass newClass = new L2SubClass();
		if(newId.getRace() == null)
			return false;

		newClass.setClassId(classId);
		newClass.setPlayer(this);

		_classlist.put(classId, newClass);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			// Store the basic info about this new sub-class.
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, skills, AwakingId,dualClass) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, Experience.LEVEL[40]);
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
			statement.setString(15, "");
			statement.setInt(16, 0);
			statement.setInt(17, 0);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warning("Could not add character sub-class: " + e);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		setActiveSubClass(classId, storeOld);
		
		// Add all the necessary skills up to level 40 for this new class.
		boolean countUnlearnable = true;
		int unLearnable = 0;
		int numSkillsAdded = 0;
		GArray<L2SkillLearn> skills = SkillTreeTable.getInstance().getAvailableSkills(this, newId);
		while(skills.size() > unLearnable)
		{
			for(final L2SkillLearn s : skills)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || !sk.getCanLearn(newId))
				{
					if(countUnlearnable)
						unLearnable++;
					continue;
				}
				addSkill(sk, true);
				numSkillsAdded++;
			}
			countUnlearnable = false;
			skills = SkillTreeTable.getInstance().getAvailableSkills(this, newId);
		}

		restoreSkills();
		rewardSkills();
		sendPacket(new SkillList(this));
		setCurrentHpMp(getMaxHp(), getMaxMp(), true);
		setCurrentCp(getMaxCp());
		return true;
	}

	/**
	 * Удаляет всю информацию о классе и добавляет новую, только для сабклассов
	 */
	public boolean modifySubClass(final int oldClassId, final int newClassId)
	{
		final L2SubClass originalClass = _classlist.get(oldClassId);
		if(originalClass.isBase())
			return false;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=? AND isBase = 0");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			// Remove all saved skills info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			// Remove all saved effects stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_effects_save WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, oldClassId);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.warning("Could not delete char sub-class: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		_classlist.remove(oldClassId);

		return newClassId > 0 ? addSubClass(newClassId, false) : true;
	}

	/**
	 * Устанавливает активный сабкласс
	 *
	 * <li>Retrieve from the database all skills of this L2Player and add them to _skills </li>
	 * <li>Retrieve from the database all macroses of this L2Player and add them to _macroses</li>
	 * <li>Retrieve from the database all shortCuts of this L2Player and add them to _shortCuts</li><BR><BR>
	 */
	public void setActiveSubClass(final int subId, final boolean store)
	{
		final L2SubClass sub = getSubClasses().get(subId);
		if(sub == null)
		{
			System.out.print("WARNING! setActiveSubClass<?> :: sub == null :: subId == " + subId);
			Thread.dumpStack();
			return;
		}

		if(getActiveClass() != null)
		{
			storeEffects();
			storeDisableSkills();

			if(QuestManager.getQuest(422) != null)
			{
				String qn = QuestManager.getQuest(422).getName();
				if(qn != null)
				{
					QuestState qs = getQuestState(qn);
					if(qs != null)
						qs.exitCurrentQuest(true);
				}
			}
		}

		if(store)
		{
			final L2SubClass oldsub = getActiveClass();
			oldsub.setCp(getCurrentCp());
			//oldsub.setExp(getExp());
			//oldsub.setLevel(getLevel());
			//oldsub.setSp(getSp());
			oldsub.setHp(getCurrentHp());
			oldsub.setMp(getCurrentMp());
			oldsub.setActive(false);
			getSubClasses().put(getActiveClassId(), oldsub);
		}

		sub.setActive(true);
		setActiveClass(sub);
		getSubClasses().put(getActiveClassId(), sub);

		setClassId(subId, false);

		removeAllSkills();

		getEffectList().stopAllEffects();

		if(getPet() != null && (getPet().isSummon() || Config.ALT_IMPROVED_PETS_LIMITED_USE && (getPet().getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID && !isMageClass() || getPet().getNpcId() == PetDataTable.IMPROVED_BABY_BUFFALO_ID && isMageClass())))
			getPet().unSummon();

		if(_cubics != null)
			for(L2CubicInstance cubic : _cubics)
				cubic.deleteMe(false);

		setAgathion(0);

		checkRecom();
		restoreSkills();
		restoreSubclassSkills();
		rewardSkills();
		sendPacket(new ExStorageMaxCount(this));
		sendPacket(new SkillList(this));

		getInventory().refreshListeners();
		getInventory().checkAllConditions();

		for(int i = 0; i < 3; i++)
			_henna[i] = null;

		restoreHenna();
		sendPacket(new HennaInfo(this));

		restoreEffects();
		if(isVisible()) // костыль для загрузки чара
			restoreDisableSkills();

		setCurrentHpMp(sub.getHp(), sub.getMp());
		setCurrentCp(sub.getCp());
		broadcastUserInfo(true);
		updateStats();

		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		sendPacket(new SkillCoolTime(this));

		broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));

		getInventory().restoreCursedWeapon();
		// Хранится в БД как эффект
		//getDeathPenalty().restore();

		setIncreasedForce(0);
	}

	/**
	 * Через delay миллисекунд выбросит игрока из игры
	 */
	public void startKickTask(long delay)
	{
		if(_kickTask != null)
			stopKickTask();
		_kickTask = ThreadPoolManager.getInstance().scheduleAi(new KickTask(this), delay, true);
	}

	public void stopKickTask()
	{
		if(_kickTask != null)
		{
			_kickTask.cancel(false);
			_kickTask = null;
		}
	}

	public void startBonusTask(long time)
	{
		time *= 1000;
		time -= System.currentTimeMillis();
		if(_bonusExpiration != null)
			stopBonusTask();
		_bonusExpiration = ThreadPoolManager.getInstance().scheduleAi(new BonusTask(this), time, true);
	}

	public void stopBonusTask()
	{
		if(_bonusExpiration != null)
		{
			_bonusExpiration.cancel(true);
			_bonusExpiration = null;
		}
	}

	public int getInventoryLimit()
	{
		return (int) calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
	}

	public int getWarehouseLimit()
	{
		return (int) calcStat(Stats.STORAGE_LIMIT, 0, null, null);
	}

	public int getFreightLimit()
	{
		// FIXME Не учитывается количество предметов, уже имеющееся на складе
		return getWarehouseLimit();
	}

	public int getTradeLimit()
	{
		return (int) calcStat(Stats.TRADE_LIMIT, 0, null, null);
	}

	public int getDwarvenRecipeLimit()
	{
		return (int) calcStat(Stats.DWARVEN_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	public int getCommonRecipeLimit()
	{
		return (int) calcStat(Stats.COMMON_RECIPE_LIMIT, 50, null, null) + Config.ALT_ADD_RECIPES;
	}

	@Override
	public int getNpcId()
	{
		return -2;
	}

	public L2Object getVisibleObject(int id)
	{
		if(getObjectId() == id)
			return this;

		if(getTargetId() == id)
			return getTarget();

		if(_party != null)
			for(L2Player p : _party.getPartyMembers())
				if(p != null && p.getObjectId() == id)
					return p;

		L2Object obj = L2World.getAroundObjectById(this, id);

		// Руль кланового летающего корабля
		if(obj == null && isInVehicle() && getVehicle().isClanAirShip() && ClanTable.getInstance().getClan(id) != null)
			obj = ((L2AirShip) getVehicle()).getControlKey();

		return obj == null || obj.isInvisible() ? null : obj;
	}

	@Override
	public int getPAtk(final L2Character target)
	{
		double init = getActiveWeaponInstance() == null ? (isMageClass() ? 3 : 4) : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getPDef(final L2Character target)
	{
		double init = 4; //empty cloak and underwear slots

		final L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest == null)
			init += isMageClass() ? L2Armor.EMPTY_BODY_MYSTIC : L2Armor.EMPTY_BODY_FIGHTER;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null && (chest == null || chest.getBodyPart() != L2Item.SLOT_FULL_ARMOR))
			init += isMageClass() ? L2Armor.EMPTY_LEGS_MYSTIC : L2Armor.EMPTY_LEGS_FIGHTER;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null)
			init += L2Armor.EMPTY_HELMET;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null)
			init += L2Armor.EMPTY_GLOVES;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) == null)
			init += L2Armor.EMPTY_BOOTS;

		return (int) calcStat(Stats.POWER_DEFENCE, init, target, null);
	}

	@Override
	public int getMDef(final L2Character target, final L2Skill skill)
	{
		double init = 0;

		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) == null)
			init += L2Armor.EMPTY_EARRING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) == null)
			init += L2Armor.EMPTY_EARRING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) == null)
			init += L2Armor.EMPTY_NECKLACE;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) == null)
			init += L2Armor.EMPTY_RING;
		if(getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) == null)
			init += L2Armor.EMPTY_RING;

		return (int) calcStat(Stats.MAGIC_DEFENCE, init, target, skill);
	}

	public boolean isSubClassActive()
	{
		return getBaseClassId() != getActiveClassId();
	}

	@Override
	public String getTitle()
	{
		return super.getTitle();
	}

	public int getTitleColor()
	{
		return _titlecolor;
	}

	public void setTitleColor(final int color)
	{
		_titlecolor = color;
	}

	public void setTitleColor(final int red, final int green, final int blue)
	{
		_titlecolor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

    public Forum getMemo() {
        if (_forumMemo == null) {
            if (ForumsBBSManager.getInstance().getForumByName("MemoRoot") == null)
                return null;
            if (ForumsBBSManager.getInstance().getForumByName("MemoRoot").GetChildByName(_accountName) == null)
                ForumsBBSManager.getInstance().CreateNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
            setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").GetChildByName(_accountName));
        }
        return _forumMemo;
    }

    /**
     * @param forum
     */
    public void setMemo(final Forum forum) {
        _forumMemo = forum;
    }

	@Override
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

	private FishData _fish;

	public void setFish(FishData fish)
	{
		_fish = fish;
	}

	public void stopLookingForFishTask()
	{
		if(_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}

	public void startLookingForFishTask()
	{
		if(!isDead() && _taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;

			if(_lure != null)
			{
				int lureid = _lure.getItemId();
				isNoob = _fish.getGroup() == 0;
				isUpperGrade = _fish.getGroup() == 2;
				if(lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511) //low grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.33));
				else if(lureid == 6520 || lureid == 6523 || lureid == 6526 || lureid >= 8505 && lureid <= 8513 || lureid >= 7610 && lureid <= 7613 || lureid >= 7807 && lureid <= 7809 || lureid >= 8484 && lureid <= 8486) //medium grade, beginner, prize-winning & quest special bait
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.00));
				else if(lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513) //high grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 0.66));
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new LookingForFishTask(this, _fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob, isUpperGrade), 10000, checkDelay, false);
		}
	}

	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}

	public void endFishing(boolean win)
	{
		ExFishingEnd efe = new ExFishingEnd(win, this);
		broadcastPacket(efe);
		_fishing = false;
		_fishLoc = new Location();
		broadcastUserInfo(true);
		if(_fishCombat == null)
			sendPacket(Msg.BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY);
		_fishCombat = null;
		_lure = null;
		//Ends fishing
		sendPacket(Msg.ENDS_FISHING);
		setImobilised(false);
		stopLookingForFishTask();
	}

	public L2Fishing getFishCombat()
	{
		return _fishCombat;
	}

	public void setFishLoc(Location loc)
	{
		_fishLoc = loc;
	}

	public Location getFishLoc()
	{
		return _fishLoc;
	}

	public void setLure(L2ItemInstance lure)
	{
		_lure = lure;
	}

	public L2ItemInstance getLure()
	{
		return _lure;
	}

	public boolean isFishing()
	{
		return _fishing;
	}

	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}

	public Bonus getBonus()
	{
		return _bonus;
	}

	public void restoreBonus()
	{
		_bonus = new Bonus(this);
	}

	@Override
	public float getRateAdena()
	{
		return _party == null ? _bonus.RATE_DROP_ADENA : _party._rateAdena;
	}

	@Override
	public float getRateItems()
	{
		return _party == null ? _bonus.RATE_DROP_ITEMS : _party._rateDrop;
	}

	@Override
	public double getRateExp()
	{
		return calcStat(Stats.EXP, (_party == null ? _bonus.RATE_XP : _party._rateExp), null, null);
	}

	@Override
	public double getRateSp()
	{
		return calcStat(Stats.SP, (_party == null ? _bonus.RATE_SP : _party._rateSp), null, null);
	}

	@Override
	public float getRateSpoil()
	{
		return _party == null ? _bonus.RATE_DROP_SPOIL : _party._rateSpoil;
	}

	private boolean _maried = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _engagerequest = false;
	private int _engageid = 0;
	private boolean _maryrequest = false;
	private boolean _maryaccepted = false;

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
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

	public void engageAnswer(int answer)
	{
		if(!_engagerequest || _engageid == 0)
			return;

		L2Player ptarget = L2ObjectsStorage.getPlayer(_engageid);
		setEngageRequest(false, 0);
		if(ptarget != null)
			if(answer == 1)
			{
				CoupleManager.getInstance().createCouple(ptarget, this);
				ptarget.sendMessage(new CustomMessage("l2rt.gameserver.model.L2Player.EngageAnswerYes", this));
			}
			else
				ptarget.sendMessage(new CustomMessage("l2rt.gameserver.model.L2Player.EngageAnswerNo", this));
	}

	/**
	private List<L2Player> _snoopListener = new GArray<L2Player>();
	private List<L2Player> _snoopedPlayer = new GArray<L2Player>();

	public void broadcastSnoop(int type, String name, String _text)
	{
		if(_snoopListener.size() > 0)
		{
			Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);
			for(L2Player pci : _snoopListener)
				if(pci != null)
					pci.sendPacket(sn);
		}
	}

	public void addSnooper(L2Player pci)
	{
		if(!_snoopListener.contains(pci))
			_snoopListener.add(pci);
	}

	public void removeSnooper(L2Player pci)
	{
		_snoopListener.remove(pci);
	}

	public void addSnooped(L2Player pci)
	{
		if(!_snoopedPlayer.contains(pci))
			_snoopedPlayer.add(pci);
	}

	public void removeSnooped(L2Player pci)
	{
		_snoopedPlayer.remove(pci);
	}
	*/

	public final FastMap<Integer, SkillTimeStamp> skillReuseTimeStamps = new FastMap<Integer, SkillTimeStamp>().setShared(true);

	public FastMap<Integer, SkillTimeStamp> getSkillReuseTimeStamps()
	{
		return skillReuseTimeStamps;
	}

	private void addSkillTimeStamp(Integer skillId, long reuseDelay)
	{
		synchronized (skillReuseTimeStamps)
		{
			skillReuseTimeStamps.put(skillId, new SkillTimeStamp(skillId, System.currentTimeMillis() + reuseDelay, reuseDelay));
		}
	}

	private void removeSkillTimeStamp(Integer skillId)
	{
		synchronized (skillReuseTimeStamps)
		{
			skillReuseTimeStamps.remove(skillId);
		}
	}

	@Override
	public boolean isSkillDisabled(Integer skillId)
	{
		synchronized (skillReuseTimeStamps)
		{
			SkillTimeStamp sts = skillReuseTimeStamps.get(skillId);
			if(sts == null)
				return false;
			if(sts.hasNotPassed())
				return true;
			skillReuseTimeStamps.remove(skillId);
			return false;
		}
	}

	@Override
	public void disableSkill(int skillId, long delay)
	{
		addSkillTimeStamp(skillId, delay);
	}

	@Override
	public void enableSkill(Integer skillId)
	{
		removeSkillTimeStamp(skillId);
	}

	public ScheduledFuture<?> getWaterTask()
	{
		return _taskWater;
	}

	public DeathPenalty getDeathPenalty()
	{
		return getActiveClass().getDeathPenalty();
	}

	public void setDeathPeanalty(DeathPenalty dp)
	{
		getActiveClass().setDeathPenalty(dp);
	}

	//fast fix for dice spam
	public long lastDiceThrown = 0;

	private boolean _charmOfCourage = false;

	public boolean isCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;

		if(!val)
			getEffectList().stopEffect(L2Skill.SKILL_CHARM_OF_COURAGE);

		sendPacket(new EtcStatusUpdate(this));
	}

    private void revalidatePenalties()
	{
        _curWeightPenalty = 0;
        armorExpertisePenalty = 0;
        weaponExpertisePenalty = 0;
        refreshOverloaded();
        validateItemExpertisePenalties(true, true, true);
    }

	private int _increasedForce = 0;
	private int _consumedSouls = 0;

	@Override
	public int getIncreasedForce()
	{
		return _increasedForce;
	}

	@Override
	public int getConsumedSouls()
	{
		return _consumedSouls;
	}

	@Override
	public void setConsumedSouls(int i, L2NpcInstance monster)
	{
		if(i == _consumedSouls)
			return;

		int max = (int) calcStat(Stats.SOULS_LIMIT, 0, monster, null);

		if(i > max)
			i = max;

		if(i <= 0)
		{
			_consumedSouls = 0;
			sendPacket(new EtcStatusUpdate(this));
			return;
		}

		if(_consumedSouls != i)
		{
			int diff = i - _consumedSouls;
			if(diff > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
				sm.addNumber(diff);
				sm.addNumber(i);
				sendPacket(sm);
			}
		}
		else if(max == i)
		{
			sendPacket(Msg.SOUL_CANNOT_BE_ABSORBED_ANY_MORE);
			return;
		}

		_consumedSouls = i;
		sendPacket(new EtcStatusUpdate(this));
	}

	@Override
	public void setIncreasedForce(int i)
	{
		i = Math.min(i, Charge.MAX_CHARGE);
		i = Math.max(i, 0);

		if(i != 0 && i > _increasedForce)
			sendPacket(new SystemMessage(SystemMessage.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL).addNumber(i));

		_increasedForce = i;
		sendPacket(new EtcStatusUpdate(this));
	}

	private long _lastFalling;

	public boolean isFalling()
	{
		return System.currentTimeMillis() - _lastFalling < 5000;
	}

	public void falling(int height)
	{
		if(!Config.DAMAGE_FROM_FALLING || isDead() || isFlying() || isSwimming() || isInVehicle())
			return;
		_lastFalling = System.currentTimeMillis();
		int damage = (int) calcStat(Stats.FALL, getMaxHp() / 2000 * height, null, null);
		if(damage > 0)
		{
			int curHp = (int) getCurrentHp();
			if(curHp - damage < 1)
				setCurrentHp(1, false);
			else
				setCurrentHp(curHp - damage, false);
			sendPacket(new SystemMessage(SystemMessage.YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL).addNumber(damage));
		}
	}

	/**
	 * Системные сообщения о текущем состоянии хп
	 */
	@Override
	public void checkHpMessages(double curHp, double newHp)
	{
		//сюда пасивные скиллы
		byte[] _hp = { 30, 30 };
		int[] skills = { 290, 291 };

		//сюда активные эффекты
		int[] _effects_skills_id = { 139, 176, 292, 292, 420 };
		byte[] _effects_hp = { 30, 30, 30, 60, 30 };

		double percent = getMaxHp() / 100;
		double _curHpPercent = curHp / percent;
		double _newHpPercent = newHp / percent;
		boolean needsUpdate = false;

		//check for passive skills
		for(int i = 0; i < skills.length; i++)
		{
			short level = getSkillLevel(skills[i]);
			if(level > 0)
				if(_curHpPercent > _hp[i] && _newHpPercent <= _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(skills[i], level));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _hp[i] && _newHpPercent > _hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(skills[i], level));
					needsUpdate = true;
				}
		}

		//check for active effects
		for(Integer i = 0; i < _effects_skills_id.length; i++)
			if(getEffectList().getEffectsBySkillId(_effects_skills_id[i]) != null)
				if(_curHpPercent > _effects_hp[i] && _newHpPercent <= _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}
				else if(_curHpPercent <= _effects_hp[i] && _newHpPercent > _effects_hp[i])
				{
					sendPacket(new SystemMessage(SystemMessage.SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR).addSkillName(_effects_skills_id[i], 1));
					needsUpdate = true;
				}

		if(needsUpdate)
			sendChanges();
	}

	/**
	 * Системные сообщения для темных эльфов о вкл/выкл ShadowSence (skill id = 294)
	 */
	public void checkDayNightMessages()
	{
		short level = getSkillLevel(294);
		if(level > 0)
			if(GameTimeController.getInstance().isNowNight())
				sendPacket(new SystemMessage(SystemMessage.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT).addSkillName(294, level));
			else
				sendPacket(new SystemMessage(SystemMessage.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR).addSkillName(294, level));
		sendChanges();
	}

	private boolean _isInDangerArea;

	public boolean isInDangerArea()
	{
		return _isInDangerArea;
	}

	public void setInDangerArea(boolean value)
	{
		_isInDangerArea = value;
	}

	public void setInCombatZone(boolean flag)
	{
		_isInCombatZone = flag;
	}

	public void setOnSiegeField(boolean flag)
	{
		_isOnSiegeField = flag;
	}

	public boolean isInPeaceZone()
	{
		return _isInPeaceZone;
	}

	public void setInPeaceZone(boolean b)
	{
		_isInPeaceZone = b;
	}

	public boolean isInSSZone()
	{
		return _isInSSZone;
	}

	public void setInSSZone(boolean b)
	{
		_isInSSZone = b;
	}

	public boolean isInCombatZone()
	{
		return _isInCombatZone;
	}

	public boolean isOnSiegeField()
	{
		return _isOnSiegeField;
	}

	public void doZoneCheck(int messageNumber)
	{
		boolean oldIsInDangerArea = isInDangerArea();
		boolean oldIsInCombatZone = isInCombatZone();
		boolean oldIsOnSiegeField = isOnSiegeField();
		boolean oldIsInPeaceZone = isInPeaceZone();
		boolean oldSSQZone = isInSSZone();

		setInDangerArea(isInZone(poison) || isInZone(instant_skill) || isInZone(swamp) || isInZone(damage));
		setInCombatZone(isInZoneBattle());
		setOnSiegeField(isInZone(Siege));
		setInPeaceZone(isInZone(peace_zone));
		setInSSZone(isInZone(ssq_zone));

		if(oldIsInDangerArea != isInDangerArea() || oldIsInCombatZone != isInCombatZone() || oldIsOnSiegeField != isOnSiegeField() || oldIsInPeaceZone != isInPeaceZone() || oldSSQZone != isInSSZone())
		{
			sendPacket(new ExSetCompassZoneCode(this));
			sendPacket(new EtcStatusUpdate(this));
			if(messageNumber != 0)
				sendPacket(new SystemMessage(messageNumber));
		}

		if(oldIsOnSiegeField != isOnSiegeField())
			if(isOnSiegeField())
				sendPacket(Msg.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			else
				sendPacket(Msg.YOU_HAVE_LEFT_A_COMBAT_ZONE);

		if(oldIsOnSiegeField != isOnSiegeField() && !isOnSiegeField() && !isTeleporting() && getPvpFlag() == 0)
			startPvPFlag(null);

		revalidateInResidence();
	}

	private Future<?> _returnTerritoryFlagTask = null;

	public void checkTerritoryFlag()
	{
		if(isTerritoryFlagEquipped())
		{
			L2Zone siegeZone = ZoneManager.getInstance().getZoneByType(ZoneType.Siege, getX(), getY(), true);
			if(siegeZone == null && (_returnTerritoryFlagTask == null || _returnTerritoryFlagTask.isDone()))
			{
				_returnTerritoryFlagTask = ThreadPoolManager.getInstance().scheduleGeneral(new ReturnTerritoryFlagTask(this), 600000);
				sendMessage("У вас есть 10 минут, чтобы вернуться в осадную зону, иначе флаг вернется в замок. Вы можете использовать форты как помежуточные точки, для сброса таймера.");
			}
			if(siegeZone != null && _returnTerritoryFlagTask != null)
			{
				_returnTerritoryFlagTask.cancel(true);
				_returnTerritoryFlagTask = null;
			}
		}
	}

	private ResidenceType _inResidence = ResidenceType.None;

	public void revalidateInResidence()
	{
		L2Clan clan = _clan;
		if(clan == null)
			return;
		int clanHallIndex = clan.getHasHideout();
		if(clanHallIndex != 0)
		{
			ClanHall clansHall = ClanHallManager.getInstance().getClanHall(clanHallIndex);
			if(clansHall != null && clansHall.checkIfInZone(getX(), getY()))
			{
				setInResidence(ResidenceType.Clanhall);
				return;
			}
		}
		int castleIndex = clan.getHasCastle();
		if(castleIndex != 0)
		{
			Castle castle = CastleManager.getInstance().getCastleByIndex(castleIndex);
			if(castle != null && castle.checkIfInZone(getX(), getY()))
			{
				setInResidence(ResidenceType.Castle);
				return;
			}
		}
		int fortressIndex = clan.getHasFortress();
		if(fortressIndex != 0)
		{
			Fortress fort = FortressManager.getInstance().getFortressByIndex(fortressIndex);
			if(fort != null && fort.checkIfInZone(getX(), getY()))
			{
				setInResidence(ResidenceType.Fortress);
				return;
			}
		}
		setInResidence(ResidenceType.None);
	}

	public ResidenceType getInResidence()
	{
		return _inResidence;
	}

	public void setInResidence(ResidenceType inResidence)
	{
		_inResidence = inResidence;
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(new SystemMessage(message));
	}

	private Location _lastClientPosition;
	private Location _lastServerPosition;

	@Override
	public void setLastClientPosition(Location position)
	{
		_lastClientPosition = position;
	}

	public Location getLastClientPosition()
	{
		return _lastClientPosition;
	}

	@Override
	public void setLastServerPosition(Location position)
	{
		_lastServerPosition = position;
	}

	public Location getLastServerPosition()
	{
		return _lastServerPosition;
	}

	private int _useSeed = 0;

	public void setUseSeed(int id)
	{
		_useSeed = id;
	}

	public int getUseSeed()
	{
		return _useSeed;
	}

	public int getRelation(L2Player target)
	{
		int result = 0;

		if(getClan() != null)
			result |= RelationChanged.RELATION_CLAN_MEMBER;

		if(isClanLeader())
			result |= RelationChanged.RELATION_LEADER;

		L2Party party = getParty();
		if(party != null && party == target.getParty())
		{
			result |= RelationChanged.RELATION_HAS_PARTY;

			switch(party.getPartyMembers().indexOf(this))
			{
				case 0:
					result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChanged.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChanged.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChanged.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChanged.RELATION_PARTY1; // 0x1
					break;
			}
		}

		L2Clan clan1 = getClan();
		L2Clan clan2 = target.getClan();
		if(clan1 != null && clan2 != null)
		{
			Siege siege1 = clan1.getSiege();
			Siege siege2 = clan2.getSiege();

			int state1 = getSiegeState();
			int state2 = target.getSiegeState();

			if(siege1 != null && siege2 != null && siege1 == siege2 && siege1.isInProgress() && state1 != 0 && state2 != 0)
			{
				result |= RelationChanged.RELATION_INSIEGE;
				if(state1 != state2 || siege1.isMidVictory() && state1 == 1 && state2 == 1)
					result |= RelationChanged.RELATION_ENEMY;
				else
					result |= RelationChanged.RELATION_ALLY;
				if(state1 == 1)
					result |= RelationChanged.RELATION_ATTACKER;
			}

			if(target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY && getPledgeType() != L2Clan.SUBUNIT_ACADEMY)
				if(clan2.isAtWarWith(clan1.getClanId()))
				{
					result |= RelationChanged.RELATION_1SIDED_WAR;
					if(clan1.isAtWarWith(clan2.getClanId()))
						result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
		}

		int territorySiege1 = getTerritorySiege();
		int territorySiege2 = target.getTerritorySiege();

		if(territorySiege1 > -1 && territorySiege2 > -1)
			/* TODO возможно, что-то из этого тоже нужно отсылать
			result |= RelationChanged.RELATION_INSIEGE;
			if(territorySiege1 != territorySiege2)
				result |= RelationChanged.RELATION_ENEMY;
			else
				result |= RelationChanged.RELATION_ALLY;
			*/
			result |= RelationChanged.RELATION_ISINTERRITORYWARS;

		return result;
	}

	/** 0=White, 1=Purple, 2=PurpleBlink  */
	protected int _pvpFlag;

	private Future<?> _PvPRegTask;
	private long _lastPvpAttack;

	public void setlastPvpAttack(long time)
	{
		_lastPvpAttack = time;
	}

	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}

	@Override
	public void startPvPFlag(L2Character target)
	{
		long startTime = System.currentTimeMillis();
		if(target != null && target.getPvpFlag() != 0)
			startTime -= Config.PVP_TIME / 2;
		if(_pvpFlag != 0 && _lastPvpAttack > startTime)
			return;
		_lastPvpAttack = startTime;

		updatePvPFlag(1);

		if(_PvPRegTask == null)
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new PvPFlagTask(this), 1000, 1000, true);
	}

	public void stopPvPFlag()
	{
		if(_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
			_PvPRegTask = null;
		}
		updatePvPFlag(0);
	}

	public void updatePvPFlag(int value)
	{
		if(_pvpFlag == value)
			return;

		setPvpFlag(value);

		if(_karma > 0)
		{
			sendStatusUpdate(true, StatusUpdate.PVP_FLAG);
			if(getPet() != null)
				getPet().broadcastPetInfo();
		}

		broadcastRelationChanged();
	}

	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	@Override
	public int getPvpFlag()
	{
		return _pvpFlag;
	}

	private Duel _duel;

	public void setDuel(Duel duel)
	{
		_duel = duel;
		broadcastCharInfo();
	}

	@Override
	public Duel getDuel()
	{
		return _duel;
	}

	public boolean isInDuel()
	{
		return _duel != null;
	}

	private L2TamedBeastInstance _tamedBeast = null;

	public L2TamedBeastInstance getTrainedBeast()
	{
		return _tamedBeast;
	}

	public void setTrainedBeast(L2TamedBeastInstance tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}

	private long _lastAttackPacket = 0;

	public long getLastAttackPacket()
	{
		return _lastAttackPacket;
	}

	public void setLastAttackPacket()
	{
		_lastAttackPacket = System.currentTimeMillis();
	}

	private long _lastMovePacket = 0;

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
		if(keyBindings == null)
			keyBindings = new byte[0];
		_keyBindings = keyBindings;
	}
	public void setTransformation(int transformationId)
	{
		if((transformationId == getTransformation() )|| (getTransformation() != 0 && transformationId != 0))
			return;

		// Для каждой трансформации свой набор скилов
		if(transformationId == 0) // Обычная форма
		{
			// Останавливаем текущий эффект трансформации
			for(L2Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getEffectType() == EffectType.Transformation)
				{
					effect.exit();
					break;
				}
			// Удаляем скилы трансформации
			if(!_transformationSkills.isEmpty())
			{
				for(L2Skill s : _transformationSkills.values())
					if(!s.isCommon() && !SkillTreeTable.getInstance().isSkillPossible(this, s.getId(), s.getLevel()))
						removeSkill(s);
				removeSkillById(619);
				_transformationSkills.clear();
			}
		}
		else
		{
			// Добавляем скилы трансформации
			for(L2Effect effect : getEffectList().getAllEffects())
				if(effect != null && effect.getEffectType() == EffectType.Transformation)
				{
					if(effect.getSkill() instanceof Transformation && ((Transformation) effect.getSkill()).isDisguise)
					{
						for(L2Skill s : getAllSkills())
							if(s != null && (s.isActive() || s.isToggle()))
								_transformationSkills.put(s.getId(), s);
					}
					else
						for(AddedSkill s : effect.getSkill().getAddedSkills())
							if(s.level == 0) // трансформация позволяет пользоваться обычным скиллом
							{
								int s2 = getSkillLevel(s.id);
								if(s2 > 0)
									_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, s2));
							}
							else if(s.level == -2) // XXX: дикий изжоп для скиллов зависящих от уровня игрока
							{
								int learnLevel = Math.max(effect.getSkill().getMagicLevel(), 40);
								int maxLevel = SkillTable.getInstance().getBaseLevel(s.id);
								int curSkillLevel = 1;
								if(maxLevel > 3)
									curSkillLevel += getLevel() - learnLevel;
								else
									curSkillLevel += (getLevel() - learnLevel) / ((76 - learnLevel) / maxLevel); // не спрашивайте меня что это такое
								curSkillLevel = Math.min(Math.max(curSkillLevel, 1), maxLevel);
								_transformationSkills.put(s.id, SkillTable.getInstance().getInfo(s.id, curSkillLevel));
							}
							else
								_transformationSkills.put(s.id, s.getSkill());
					break;
				}
			// Для все трансформаций кроме проклятых добавляем скилы:
			// - обратной трансформации (619)
			// - Decrease Bow/Crossbow Attack Speed (5491)
			if(!isCursedWeaponEquipped())
			{
				_transformationSkills.put(L2Skill.SKILL_TRANSFOR_DISPELL, SkillTable.getInstance().getInfo(L2Skill.SKILL_TRANSFOR_DISPELL, 1));
				_transformationSkills.put(5491, SkillTable.getInstance().getInfo(5491, 1));
			}

			for(L2Skill s : _transformationSkills.values())
				addSkill(s, false);
		}

		setTransformationId(transformationId);
		
		sendPacket(new ExBasicActionList());
		sendPacket(new SkillList(this));
		sendPacket(new ShortCutInit(this));
		for(int shotId : getAutoSoulShot())
			sendPacket(new ExAutoSoulShot(shotId, true));
		broadcastUserInfo(true);
	}	 

	/**
	 * Возвращает коллекцию скиллов, с учетом текущей трансформации
	 */
	@Override
	public final L2Skill[] getAllSkills()
	{
		// Трансформация неактивна
		if(getTransformation() == 0)
			return super.getAllSkills();

		// Трансформация активна
		L2TIntObjectHashMap<L2Skill> tempSkills = new L2TIntObjectHashMap<L2Skill>();
		for(L2Skill s : super.getAllSkills())
			if(s != null && !s.isActive() && !s.isToggle())
				tempSkills.put(s.getId(), s);
		tempSkills.putAll(_transformationSkills); // Добавляем к пассивкам скилы текущей трансформации
		
		if (_skills == null)
			return new L2Skill[0];
		
		return _skills.values(new L2Skill[0]);
	}

	public boolean getSkillFromAll(L2Skill skill)
	{
		for (L2Skill s : getAllSkills())
			if( s == skill)
				return true;
		return false;
	}
	
	public void setAgathion(int id)
	{
		if(id == 0)
		{
			if(_agathion != null)
				_agathion.deleteMe();
			_agathion = null;
		}
		else
			_agathion = new L2AgathionInstance(this, id);

		broadcastUserInfo(true);
		sendPacket(new SkillList(this));
	}

	public L2AgathionInstance getAgathion()
	{
		return _agathion;
	}

	/**
	 * Возвращает количество PcBangPoint'ов даного игрока
	 * @return количество PcCafe Bang Points
	 */
	public int getPcBangPoints()
	{
		return pcBangPoints;
	}

	/**
	 * Устанавливает количество Pc Cafe Bang Points для даного игрока
	 * @param pcBangPoints новое количество PcCafeBangPoints
	 */
	public void setPcBangPoints(int pcBangPoints)
	{
		this.pcBangPoints = pcBangPoints;
	}

	private Location _groundSkillLoc;

	public void setGroundSkillLoc(Location location)
	{
		_groundSkillLoc = location;
	}

	public Location getGroundSkillLoc()
	{
		return _groundSkillLoc;
	}

	public boolean isDeleting()
	{
		return _isDeleting;
	}

	public void setOfflineMode(boolean val)
	{
		if(!val)
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

		if(_sellList == null || _sellList.isEmpty())
			unsetVar("selllist");
		else
		{
			for(TradeItem i : _sellList)
				val += i.getObjectId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("selllist", val);
			val = "";
			if(_tradeList != null && _tradeList.getSellStoreName() != null)
				setVar("sellstorename", _tradeList.getSellStoreName());
		}

		if(_buyList == null || _buyList.isEmpty())
			unsetVar("buylist");
		else
		{
			for(TradeItem i : _buyList)
				val += i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
			setVar("buylist", val);
			val = "";
			if(_tradeList != null && _tradeList.getBuyStoreName() != null)
				setVar("buystorename", _tradeList.getBuyStoreName());
		}

		if(_createList == null || _createList.getList().isEmpty())
			unsetVar("createlist");
		else
		{
			for(L2ManufactureItem i : _createList.getList())
				val += i.getRecipeId() + ";" + i.getCost() + ":";
			setVar("createlist", val);
			if(_createList.getStoreName() != null)
				setVar("manufacturename", _createList.getStoreName());
		}
	}

	public void restoreTradeList()
	{
		if(getVar("selllist") != null)
		{
			_sellList = new ConcurrentLinkedQueue<TradeItem>();
			String[] items = getVar("selllist").split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;
				TradeItem i = new TradeItem();
				int oId = Integer.parseInt(values[0]);
				long count = Long.parseLong(values[1]);
				long price = Long.parseLong(values[2]);
				i.setObjectId(oId);

				L2ItemInstance itemToSell = getInventory().getItemByObjectId(oId);

				if(count < 1 || itemToSell == null)
					continue;

				if(count > itemToSell.getCount())
					count = itemToSell.getCount();

				i.setCount(count);
				i.setOwnersPrice(price);
				i.setItemId(itemToSell.getItemId());
				i.setEnchantLevel(itemToSell.getEnchantLevel());
				i.setAttackElement(itemToSell.getAttackElementAndValue());
				i.setDefenceFire(itemToSell.getDefenceFire());
				i.setDefenceWater(itemToSell.getDefenceWater());
				i.setDefenceWind(itemToSell.getDefenceWind());
				i.setDefenceEarth(itemToSell.getDefenceEarth());
				i.setDefenceHoly(itemToSell.getDefenceHoly());
				i.setDefenceUnholy(itemToSell.getDefenceUnholy());

				_sellList.add(i);
			}
			if(_tradeList == null)
				_tradeList = new L2TradeList();
			if(getVar("sellstorename") != null)
				_tradeList.setSellStoreName(getVar("sellstorename"));
		}
		if(getVar("buylist") != null)
		{
			_buyList = new ConcurrentLinkedQueue<TradeItem>();
			String[] items = getVar("buylist").split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 3)
					continue;
				TradeItem i = new TradeItem();
				i.setItemId(Integer.parseInt(values[0]));
				i.setCount(Long.parseLong(values[1]));
				i.setOwnersPrice(Long.parseLong(values[2]));
				_buyList.add(i);
			}
			if(_tradeList == null)
				_tradeList = new L2TradeList();
			if(getVar("buystorename") != null)
				_tradeList.setBuyStoreName(getVar("buystorename"));
		}
		if(getVar("createlist") != null)
		{
			_createList = new L2ManufactureList();
			String[] items = getVar("createlist").split(":");
			for(String item : items)
			{
				if(item.equals(""))
					continue;
				String[] values = item.split(";");
				if(values.length < 2)
					continue;
				_createList.add(new L2ManufactureItem(Integer.parseInt(values[0]), Long.parseLong(values[1])));
			}
			if(getVar("manufacturename") != null)
				_createList.setStoreName(getVar("manufacturename"));
		}
	}

	public void restoreRecipeBook()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("id");
				L2Recipe recipe = RecipeController.getInstance().getRecipeByRecipeId(id);
				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			_log.warning("count not recipe skills:" + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public L2DecoyInstance getDecoy()
	{
		return _decoy;
	}

	public void setDecoy(L2DecoyInstance decoy)
	{
		_decoy = decoy;
	}

	public int getMountType()
	{
		switch(getMountNpcId())
		{
			case PetDataTable.STRIDER_WIND_ID:
			case PetDataTable.STRIDER_STAR_ID:
			case PetDataTable.STRIDER_TWILIGHT_ID:
			case PetDataTable.RED_STRIDER_WIND_ID:
			case PetDataTable.RED_STRIDER_STAR_ID:
			case PetDataTable.RED_STRIDER_TWILIGHT_ID:
				return 1;
			case PetDataTable.WYVERN_ID:
				return 2;
			case PetDataTable.WGREAT_WOLF_ID:
			case PetDataTable.FENRIR_WOLF_ID:
			case PetDataTable.WFENRIR_WOLF_ID:
				return 3;
			case PetDataTable.LIGHT_PURPLE_MANED_HORSE_ID:
			case PetDataTable.TAWNY_MANED_LION_ID:
			case PetDataTable.STEAM_BEATLE_ID:
                        case PetDataTable.ANT_PRINCESS_ID:
                        case PetDataTable.HALLOWEEM_FLYING_BROOM_ID:
                        case PetDataTable.KNIGHT_HORSE_ID:
                        case PetDataTable.WARRIOR_HORSE_ID:
                        case PetDataTable.RUSTY_STEEL_HORSE_ID:
                        case PetDataTable.ARCHER_HORSE_ID:
                        case PetDataTable.PHANTOM_HORSE_ID:
                        case PetDataTable.COBALT_HORSE_ID:
                        case PetDataTable.ENCHANTER_HORSE_ID:
                        case PetDataTable.HEALER_HORSE_ID:
				return 4;
		}
		return 0;
	}

	@Override
	public float getColRadius()
	{
		if(getTransformation() != 0 && getTransformationTemplate() != 0 && NpcTable.getTemplate(getTransformationTemplate()) != null)
			return NpcTable.getTemplate(getTransformationTemplate()).collisionRadius;
		else if(isMounted() && NpcTable.getTemplate(getMountNpcId()) != null)
			return NpcTable.getTemplate(getMountNpcId()).collisionRadius;
		else
			return getBaseTemplate().collisionRadius;
	}

	@Override
	public float getColHeight()
	{
		if(getTransformation() != 0 && getTransformationTemplate() != 0 && NpcTable.getTemplate(getTransformationTemplate()) != null)
			return NpcTable.getTemplate(getTransformationTemplate()).collisionHeight;
		else if(isMounted() && NpcTable.getTemplate(getMountNpcId()) != null)
			return NpcTable.getTemplate(getMountNpcId()).collisionHeight + getBaseTemplate().collisionHeight;
		else
			return getBaseTemplate().collisionHeight;
	}

	@Override
	public void setReflection(long i)
	{
		if(_reflection == i)
			return;
		super.setReflection(i);
		if(_summon != null && !_summon.isDead())
			_summon.setReflection(i);
		if(i != 0)
		{
			String var = getVar("reflection");
			if(var == null || !var.equals(String.valueOf(i)))
				setVar("reflection", String.valueOf(i));
		}
		else
			unsetVar("reflection");
		if(getActiveClass() != null)
		{
			getInventory().checkAllConditions();
			// Для квеста _129_PailakaDevilsLegacy
			if(getPet() != null && (getPet().getNpcId() == 14916 || getPet().getNpcId() == 14917))
				getPet().unSummon();
		}
	}

	public boolean isCombatFlagEquipped()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getItem().isCombatFlag();
	}

	public boolean isTerritoryFlagEquipped()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		return weapon != null && weapon.getItem().isTerritoryFlag();
	}

	private int _buyListId;

	public void setBuyListId(int listId)
	{
		_buyListId = listId;
	}

	public int getBuyListId()
	{
		return _buyListId;
	}

	public boolean checksForShop(boolean RequestManufacture)
	{
		if(!getPlayerAccess().UseTrade)
		{
			sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
			return false;
		}

		String tradeBan = getVar("tradeBan");
		if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			sendMessage("Your trade is banned! Expires: " + (tradeBan.equals("-1") ? "never" : Util.formatTime((Long.parseLong(tradeBan) - System.currentTimeMillis()) / 1000)) + ".");
			return false;
		}

		if(hasHWID())
		{
			int ban;
			if((ban = HWID.getBonus(getHWID(), "tradeBan")) != 0)
				if(ban > System.currentTimeMillis() / 1000)
					sendMessage("Your trade is totally banned! Expires: " + (ban < 0 ? "never" : Util.formatTime(ban - System.currentTimeMillis() / 1000)) + ".");
				else if(ban > 0)
					HWID.unsetBonus(getHWID(), "tradeBan");
		}

		String BLOCK_ZONE = RequestManufacture ? L2Zone.BLOCKED_ACTION_PRIVATE_WORKSHOP : L2Zone.BLOCKED_ACTION_PRIVATE_STORE;
		if(isActionBlocked(BLOCK_ZONE) && !isInStoreMode())
			if(!Config.SERVICES_NO_TRADE_ONLY_OFFLINE || Config.SERVICES_NO_TRADE_ONLY_OFFLINE && isInOfflineMode())
			{
				sendPacket(RequestManufacture ? new SystemMessage(SystemMessage.A_PRIVATE_WORKSHOP_MAY_NOT_BE_OPENED_IN_THIS_AREA) : Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_IN_THIS_AREA);
				return false;
			}

		if(isCastingNow())
		{
			sendPacket(Msg.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL);
			return false;
		}

		if(isInCombat())
		{
			sendPacket(Msg.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}

		if(isOutOfControl() || isActionsDisabled() || isMounted() || isInOlympiadMode() || getDuel() != null)
			return false;

		if(Config.SERVICES_TRADE_ONLY_FAR && !isInStoreMode())
		{
			boolean tradenear = false;
			for(L2Player player : L2World.getAroundPlayers(this, Config.SERVICES_TRADE_RADIUS, 200))
				if(player.isInStoreMode())
				{
					tradenear = true;
					break;
				}

			if(L2World.getAroundNpc(this, Config.SERVICES_TRADE_RADIUS + 100, 200).size() > 0)
				tradenear = true;

			if(tradenear)
			{
				sendMessage(new CustomMessage("trade.OtherTradersNear", this));
				return false;
			}
		}

		return true;
	}

	public int getFame()
	{
		return _fame;
	}

	public void setFame(int fame, String log)
	{
		fame = Math.min(ConfigSystem.getInt("LimitFame"), fame);
		if(log != null && !log.isEmpty())
			Log.add(_name + "|" + (fame - _fame) + "|" + fame + "|" + log, "fame");
		if(fame > _fame)
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S1_REPUTATION_SCORE).addNumber(fame - _fame));
		_fame = fame;
		sendChanges();
	}

	// ТЕперь виталка хранится на аккаунте, а не на чаре
	private void storeVitality()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO vitality VALUES (?,?)");
			statement.setString(1, getAccountName());
			statement.setInt(2, (int) getVitality());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error while storeVitality.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void restoreVitality()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT vitality_points FROM vitality WHERE account_name = ?");
			statement.setString(1, getAccountName());
			rset = statement.executeQuery();
			while (rset.next())
			{
				setVitality(rset.getInt("vitality_points"));
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error while restoreVitality.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}
	
	public double getVitality()
	{
		return Config.ALT_VITALITY_ENABLED ? _vitality : 0;
	}

	public void setVitality(double newVitality)
	{
		if(!Config.ALT_VITALITY_ENABLED)
			return;

		// Походу это надо удалить
		newVitality = Math.max(Math.min(newVitality, 140000), 0);

		if(newVitality >= _vitality || getLevel() >= 10)
		{
			if(newVitality != _vitality)
				if(newVitality == 0)
					sendPacket(Msg.VITALITY_IS_FULLY_EXHAUSTED);
				else if(newVitality == 140000)
					sendPacket(Msg.YOUR_VITALITY_IS_AT_MAXIMUM);

			_vitality = newVitality;
		}
	}

	public float getVitalityBonus()
	{
		if (getPA())
			return 3;
		else 
			return 2;
	}

	public double[] applyVitality(L2MonsterInstance monster, double xp, double sp, double partyVitalityMod)
	{
		float vitalitybonus = (monster.isRaid() ? 0 : getVitalityBonus());
		if(xp > 0)
			xp *= Config.RATE_XP * getRateExp() + vitalitybonus;
		if(sp > 0)
			sp *= Config.RATE_SP * getRateSp() + vitalitybonus;
		if(xp > 0)
				if(!(getVarB("NoExp") && getExp() == Experience.LEVEL[getLevel() + 1] - 1))
				{
					double mod = Experience.baseVitalityMod(getLevel(), getLevel(), monster.getExpReward());
					if(getEffectList().getEffectByType(EffectType.Vitality) != null)
						mod *= -1;
					setVitality(getVitality() - mod * partyVitalityMod);
				}
		return new double[] { xp, sp };
	}

	private final int _incorrectValidateCount = 0;

	public int getIncorrectValidateCount()
	{
		return _incorrectValidateCount;
	}

	public int setIncorrectValidateCount(int count)
	{
		return _incorrectValidateCount;
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
		setTarget(null);
		stopMove();
		setIsInvul(true);
		setImobilised(true);
		sendPacket(new CameraMode(1));
	}

	public void leaveMovieMode()
	{
		if(!isGM())
			setIsInvul(false);
		setImobilised(false);
		sendPacket(new CameraMode(0));
		broadcastUserInfo(true);
	}

	public void specialCamera(L2Object target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	private int _movieId = 0;

	public void setMovieId(int id)
	{
		_movieId = id;
	}

	public int getMovieId()
	{
		return _movieId;
	}

	public void showQuestMovie(int id)
	{
		if(_movieId > 0) //already in movie
			return;
		sendActionFailed();
		setTarget(null);
		stopMove();
		_movieId = id;
		sendPacket(new ExStartScenePlayer(id));
	}

	public void setAutoLoot(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			AutoLoot = enable;
			setVar("AutoLoot", String.valueOf(enable));
		}
	}
	
	public void setAutoLootHerbs(boolean enable)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			AutoLootHerbs = enable;
			setVar("AutoLootHerbs", String.valueOf(enable));
		}
	}

	public boolean isAutoLootEnabled()
	{	
		return AutoLoot;
	}

	public boolean isAutoLootHerbsEnabled()
	{
		return AutoLootHerbs;
	}

	public final void reName(String name, boolean saveToDB)
	{
		setName(name);
		if(saveToDB)
			saveNameToDB();
		Olympiad.changeNobleName(getObjectId(), name);
		broadcastUserInfo(true);
	}

	public final void reName(String name)
	{
		reName(name, false);
	}

	public final void saveNameToDB()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement st = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("UPDATE characters SET char_name = ? WHERE obj_Id = ?");
			st.setString(1, getName());
			st.setInt(2, getObjectId());
			st.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, st);
		}
	}

	@Override
	public L2Player getPlayer()
	{
		return this;
	}

	private GArray<String> getStoredBypasses(boolean bbs)
	{
		if(bbs)
		{
			if(bypasses_bbs == null)
				bypasses_bbs = new GArray<String>();
			return bypasses_bbs;
		}
		if(bypasses == null)
			bypasses = new GArray<String>();
		return bypasses;
	}

	public void cleanBypasses(boolean bbs)
	{
		GArray<String> bypassStorage = getStoredBypasses(bbs);
		synchronized (bypassStorage)
		{
			bypassStorage.clearSize();
		}
	}

	public String encodeBypasses(String htmlCode, boolean bbs)
	{
		GArray<String> bypassStorage = getStoredBypasses(bbs);
		synchronized (bypassStorage)
		{
			return BypassManager.encode(htmlCode, bypassStorage, bbs);
		}
	}

	public DecodedBypass decodeBypass(String bypass)
	{
		BypassType bpType = BypassManager.getBypassType(bypass);
		boolean bbs = bpType == BypassType.ENCODED_BBS || bpType == BypassType.SIMPLE_BBS;
		GArray<String> bypassStorage = getStoredBypasses(bbs);
		if(bpType == BypassType.ENCODED || bpType == BypassType.ENCODED_BBS)
			return BypassManager.decode(bypass, bypassStorage, bbs, this);
		if(bpType == BypassType.SIMPLE)
			return new DecodedBypass(bypass, false).trim();
		if(bpType == BypassType.SIMPLE_BBS && !bypass.startsWith("_bbsscripts"))
			return new DecodedBypass(bypass, true).trim();
		_log.warning("Direct access to bypass: " + bypass + " / Player: " + getName());
		return null;
	}

	/**
	 * Сброс реюза всех скилов персонажа.
	 */
	public void resetSkillsReuse()
	{
		getSkillReuseTimeStamps().clear();
		sendPacket(new SkillCoolTime(this));
	}

	public boolean hasHWID()
	{
		return _connection != null && _connection.hasHWID();
	}

	public HardwareID getHWID()
	{
		/*if(_connection == null)
			return NOT_CONNECTED;*/
		return _connection.HWID;
	}

	private int _territorySide = -1;


	public void setTerritorySiege(int side)
	{
		_territorySide = side;
	}

	public int getTerritorySiege()
	{
		L2Clan clan = getClan();
		if(clan != null && clan.getTerritorySiege() > -1)
			return clan.getTerritorySiege();
		if(_territorySide > -1)
			return _territorySide;
		return -1;
	}

	private static final HWIDLockComparator ReferralsComparator = new HWIDLockComparator(3);
	
	public boolean isInKrateisCube()
	{
		return InKrateisCube;
	}

	public void setInKrateisCube(boolean par)
	{
		InKrateisCube = par;
	}

	@SuppressWarnings("unchecked")
	private void checkReferralBonus(int id)
	{
		if(!Config.SERVICES_REFERRAL_ENABLED)
			return;

		try
		{
			GArray<Object> referral = mysql.get_array(L2DatabaseFactory.getInstanceLogin(), "SELECT * FROM `referrals` WHERE `login`='" + getAccountName() + "'");
			if(referral.isEmpty())
				return; // нету
			Map<String, Object> row = (Map<String, Object>) referral.get(0);

			if(!row.get("bonus" + id).toString().isEmpty())
				return; // бонус уже получен

			int server = Integer.parseInt(row.get("server").toString());
			if(server != Config.REQUEST_ID)
				return; // другой сервер

			int char_id = Integer.parseInt(row.get("char").toString());
			if(char_id == 0)
				return; // WTF?

			if(Config.PROTECT_ENABLE)
			{
				Object otherId = mysql.get("SELECT `LastHWID` FROM `characters` WHERE `obj_Id`=" + char_id);
				if(otherId == null || otherId.toString().length() != 32)
					return; // other char not exist or hwid invalid

				if(ReferralsComparator.compare(new HardwareID(otherId.toString()), getHWID()) == HWIDComparator.EQUALS)
					return; // other char is twink
			}

			Letter letter = new Letter();
			letter.receiverId = getObjectId();
			letter.receiverName = "";
			letter.senderId = 1;
			letter.senderName = "";
			letter.topic = "Referral reward";
			letter.body = "Congratulations!";
			letter.price = 0;
			letter.unread = 1;
			letter.system = 1;
			letter.validtime = 1296000 + (int) (System.currentTimeMillis() / 1000); // 14 days
			L2ItemInstance reward1 = ItemTemplates.getInstance().createItem(id == 1 ? Config.SERVICES_REFERRAL_ITEM_1 : Config.SERVICES_REFERRAL_ITEM_2);
			reward1.setCount(id == 1 ? Config.SERVICES_REFERRAL_COUNT_1 : Config.SERVICES_REFERRAL_COUNT_2);
			MailParcelController.getInstance().attach(letter, reward1);
			MailParcelController.getInstance().sendLetter(letter);
			sendPacket(new ExNoticePostArrived(1));

			Letter letter2 = new Letter();
			letter2.receiverId = char_id;
			letter2.receiverName = "";
			letter2.senderId = 1;
			letter2.senderName = "";
			letter2.topic = "Referral reward";
			letter2.body = "Congratulations! Your friend " + getName() + " acquired profession " + getClassId().toString() + "!";
			letter2.price = 0;
			letter2.unread = 1;
			letter2.system = 1;
			letter2.validtime = 1296000 + (int) (System.currentTimeMillis() / 1000); // 14 days
			L2ItemInstance reward2 = ItemTemplates.getInstance().createItem(id == 1 ? Config.SERVICES_REFERRAL_ITEM_1 : Config.SERVICES_REFERRAL_ITEM_2);
			reward2.setCount(id == 1 ? Config.SERVICES_REFERRAL_COUNT_1 : Config.SERVICES_REFERRAL_COUNT_2);
			MailParcelController.getInstance().attach(letter2, reward2);
			MailParcelController.getInstance().sendLetter(letter2);
			L2Player other = L2ObjectsStorage.getPlayer(char_id);
			if(other != null)
				other.sendPacket(new ExNoticePostArrived(1));

			Log.add("Bonus #" + id + " for char " + getObjectId() + " referred by " + char_id, "referral");
			mysql.setEx(L2DatabaseFactory.getInstanceLogin(), "UPDATE `referrals` SET `bonus" + id + "`=? WHERE `login`=? LIMIT 1", String.valueOf(getHWID()), getAccountName());
		}
		catch(SQLException e)
		{
			_log.warning("Unable to process referrals for player " + this);
			e.printStackTrace();
		}
	}
    public int getBookMarkSlot()
    {
        return _bookmarkslot;
    }
	public void setBookMarkSlot(int slot)
    {
        _bookmarkslot = slot;
        sendPacket(new L2GameServerPacket[] {
            new ExGetBookMarkInfo(this)
        });
    }
	
	public void setAwakingId(int _id)
	{
		getActiveClass().setAwakingId(_id);
		return;
	}
	
	public int getAwakingId()
	{
		return getActiveClass().getAwakingId();
	}
	
	public ClassId getAwakingClass()
	{
		return ClassId.values()[getAwakingId()];
	}

	public boolean isAwaking()
	{
		if (getActiveClass() != null)
			if (getActiveClass().getAwakingId() > 100)
				return true;
		return false;
	}
}