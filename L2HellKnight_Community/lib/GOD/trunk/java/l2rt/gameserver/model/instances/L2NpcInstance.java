package l2rt.gameserver.model.instances;

import javolution.text.TextBuilder;
import javolution.util.FastMap;
import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Events;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.Scripts;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.network.clientpackets.RequestExRemoveItemAttribute;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.*;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2ObjectTasks.NotifyFactionTask;
import l2rt.gameserver.model.L2ObjectTasks.RandomAnimationTask;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.residence.ClanHall;
import l2rt.gameserver.model.entity.residence.Fortress;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestEventType;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.network.serverpackets.ExEnchantSkillList.EnchantSkillType;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.tables.*;
import l2rt.gameserver.tables.TeleportTable.TeleportLocation;
import l2rt.gameserver.taskmanager.DecayTaskManager;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

public class L2NpcInstance extends L2Character
{
	private long _lastFactionNotifyTime = 0;
	public int minFactionNotifyInterval = 10000;
	public boolean hasChatWindow = true;
	protected long _dieTime = 0;
	private int _personalAggroRange = -1;
	private byte _level = 0;
	private int _weaponEnchant = -1;
	private boolean _isHideName = false;
	private int _currentLHandId;
	private int _currentRHandId;

	private double _currentCollisionRadius;
	private double _currentCollisionHeight;

	/**
	 * Нужно для отображения анимации спауна, используется в пакете NpcInfo:
	 * 0=false, 1=true, 2=summoned (only works if model has a summon animation)
	 **/
	private int _showSpawnAnimation = 2;

	public long pathfindCount;
	public long pathfindTime;
    private int npcState = 0;

    public void callFriends(L2Character attacker)
	{
		callFriends(attacker, 0);
	}

	public void callFriends(L2Character attacker, int damage)
	{
		if(System.currentTimeMillis() - _lastFactionNotifyTime > minFactionNotifyInterval)
		{
			if(isMonster())
				if(isMinion())
				{
					// Call master
					L2MonsterInstance master = ((L2MinionInstance) this).getLeader();
					if(master != null)
					{
						if(!master.isInCombat() && !master.isDead())
							master.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, new Object[] { attacker, Rnd.get(1, 100) });
						MinionList list = master.getMinionList();
						if(list != null)
							for(L2MinionInstance m : list.getSpawnedMinions())
								if(m != this)
									m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, new Object[] { attacker, Rnd.get(1, 100) });
					}
				}
				else
					// Call minions
					callMinionsToAssist(attacker);

			if(getFactionId() != null && !getFactionId().isEmpty())
				// call friend's
				ThreadPoolManager.getInstance().scheduleAi(new NotifyFactionTask(this, attacker, damage), 100, false);

			_lastFactionNotifyTime = System.currentTimeMillis();
		}
	}

	public GArray<L2NpcInstance> ActiveFriendTargets(boolean active, boolean attack)
	{
		GArray<L2NpcInstance> ActiveFriends = new GArray<L2NpcInstance>();
		for(L2NpcInstance obj : ActiveFriendTargets(true))
			if(attack && obj.getAI().getIntention() == AI_INTENTION_ATTACK || active && obj.getAI().getIntention() == AI_INTENTION_ACTIVE)
				ActiveFriends.add(obj);
		return ActiveFriends;
	}

	public GArray<L2NpcInstance> ActiveFriendTargets(boolean check_canSeeTarget)
	{
		GArray<L2NpcInstance> ActiveFriends = new GArray<L2NpcInstance>();
		L2WorldRegion region = L2World.getRegion(this);
		if(region != null && region.getObjectsSize() > 0)
			for(L2NpcInstance obj : region.getNpcsList(new GArray<L2NpcInstance>(region.getObjectsSize()), getObjectId(), getReflection().getId()))
				if(obj != null && !obj.isDead())
					if(!check_canSeeTarget || GeoEngine.canSeeTarget(this, obj, false))
						ActiveFriends.add(obj);
		return ActiveFriends;
	}

	private static final Logger _log = Logger.getLogger(L2NpcInstance.class.getName());
	private final ClassId[] _classesToTeach;

	/** The delay after witch the attacked is stopped */
	private long _attack_timeout;
	private Location _spawnedLoc = new Location();

	private static FastMap<String, Constructor<?>> _ai_constructors = new FastMap<String, Constructor<?>>().setShared(true);
	private final ReentrantLock getAiLock = new ReentrantLock(), decayLock = new ReentrantLock();

	@Override
	public L2CharacterAI getAI()
	{
		if(_ai != null)
			return _ai;

		getAiLock.lock();
		try
		{
			if(_ai == null)
			{
				Constructor<?> ai_constructor = _ai_constructors.get(getTemplate().ai_type);
				if(ai_constructor != null)
					try
					{
						_ai = (L2CharacterAI) ai_constructor.newInstance(this);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				if(_ai == null)
					_ai = new L2CharacterAI(this);
			}
		}
		finally
		{
			getAiLock.unlock();
		}
		return _ai;
	}

	public void setAttackTimeout(long time)
	{
		_attack_timeout = time;
	}

	public long getAttackTimeout()
	{
		return _attack_timeout;
	}

	/**
	 * Return the position of the spawned point.<BR><BR>
	 * Может возвращать случайную точку, поэтому всегда следует кешировать результат вызова!
	 */
	public Location getSpawnedLoc()
	{
		return _spawnedLoc;
	}

	public void setSpawnedLoc(Location loc)
	{
		_spawnedLoc = loc;
	}

	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		if(template == null)
		{
			_log.warning("No template for Npc. Please check your datapack is setup correctly.");
			throw new IllegalArgumentException();
		}

		_classesToTeach = template.getTeachInfo();
		setName(template.name);
		setTitle(template.title);

		setFlying(template.getAIParams().getBool("isFlying", false));

		String implementationName = template.ai_type;

		Constructor<?> ai_constructor = _ai_constructors.get(implementationName);

		if(ai_constructor == null)
		{
			try
			{
				if(!implementationName.equalsIgnoreCase("npc"))
					ai_constructor = Class.forName("l2rt.gameserver.ai." + implementationName).getConstructors()[0];
			}
			catch(Exception e)
			{
				try
				{
					ai_constructor = Scripts.getInstance().getClasses().get("ai." + implementationName).getRawClass().getConstructors()[0];
				}
				catch(Exception e1)
				{
					_log.warning("AI type " + template.ai_type + " not found!");
					e1.printStackTrace();
				}
			}

			if(ai_constructor != null)
				_ai_constructors.put(implementationName, ai_constructor);
		}

		// Монстров тоже исключаем.
		if(hasRandomAnimation() && ai_constructor == null && !isMonster())
			startRandomAnimation();

		// инициализация параметров оружия
		_currentLHandId = getTemplate().lhand;
		_currentRHandId = getTemplate().rhand;
		_weaponEnchant = _weaponEnchant < 0 && Rnd.chance(Config.MOBS_WEAPON_ENCHANT_CHANCE) ? Rnd.get(Config.MOBS_WEAPON_ENCHANT_MIN, Config.MOBS_WEAPON_ENCHANT_MAX) : 0;

		// инициализация коллизий
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;
	}

	public int getRightHandItem()
	{
		return _currentRHandId;
	}

	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}

	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}
	
	public void setHideName(boolean val)
	{
		_isHideName = val;
	}
	
	public boolean isHideName()
	{
		return _isHideName;
	}

	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public void setCollisionHeight(double offset)
	{
		_currentCollisionHeight = offset;
	}

	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	public void setCollisionRadius(double collisionRadius)
	{
		_currentCollisionRadius = collisionRadius;
	}

	@Override
	public void doDie(L2Character killer)
	{
		_dieTime = System.currentTimeMillis();
		setDecayed(false);

		if(isMonster() && (((L2MonsterInstance) this).isSeeded() || ((L2MonsterInstance) this).isSpoiled()))
			DecayTaskManager.getInstance().addDecayTask(this, 20000);
		else
			DecayTaskManager.getInstance().addDecayTask(this);

		// установка параметров оружия и коллизий по умолчанию
		_currentLHandId = getTemplate().lhand;
		_currentRHandId = getTemplate().rhand;
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;

		clearAggroList(false);

		super.doDie(killer);
	}

	public class AggroInfo
	{
		public L2Playable attacker;
		public int hate;
		public int damage;

		AggroInfo(L2Playable attacker)
		{
			this.attacker = attacker;
		}
	}

	public long getDeadTime()
	{
		if(_dieTime <= 0)
			return 0;
		return System.currentTimeMillis() - _dieTime;
	}

	public HashMap<L2Playable, AggroInfo> getAggroMap()
	{
		HashMap<L2Playable, AggroInfo> temp = new HashMap<L2Playable, AggroInfo>();
		for(L2Playable playable : L2World.getAroundPlayables(this))
			if(playable != null)
			{
				HateInfo hateInfo = playable.getHateList().get(this);
				if(hateInfo != null)
				{
					AggroInfo aggroInfo = new AggroInfo(playable);
					aggroInfo.hate = hateInfo.hate;
					aggroInfo.damage = hateInfo.damage;
					temp.put(playable, aggroInfo);
				}
			}
		return temp;
	}

	public GArray<AggroInfo> getAggroList()
	{
		GArray<AggroInfo> temp = new GArray<AggroInfo>();
		for(L2Playable playable : L2World.getAroundPlayables(this))
			if(playable != null)
			{
				HateInfo hateInfo = playable.getHateList().get(this);
				if(hateInfo != null)
				{
					AggroInfo aggroInfo = new AggroInfo(playable);
					aggroInfo.hate = hateInfo.hate;
					aggroInfo.damage = hateInfo.damage;
					temp.add(aggroInfo);
				}
			}
		return temp;
	}

	public GArray<L2Playable> getAggroListPlayable()
	{
		GArray<L2Playable> temp = new GArray<L2Playable>();
		for(L2Playable playable : L2World.getAroundPlayables(this))
			if(playable != null && playable.getHateList().get(this) != null)
				temp.add(playable);
		return temp;
	}

	public void clearAggroList(boolean onlyHate)
	{
		for(L2Playable playable : L2World.getAroundPlayables(this))
			if(playable != null)
				playable.removeFromHatelist(this, onlyHate);
	}

	public L2Character getMostHated()
	{
		L2Character target = getAI().getAttackTarget();
		if(target != null && target.isNpc() && target.isVisible() && target != this && !target.isDead() && target.isInRange(this, 2000))
			return target;

		GArray<AggroInfo> aggroList = getAggroList();

		GArray<AggroInfo> activeList = new GArray<AggroInfo>();
		GArray<AggroInfo> passiveList = new GArray<AggroInfo>();

		for(AggroInfo ai : aggroList)
			if(ai.hate > 0)
			{
				L2Playable cha = ai.attacker;
				if(cha != null)
					if(!cha.isSummon() && (cha.isStunned() || cha.isSleeping() || cha.isParalyzed() || cha.isAfraid() || cha.isBlocked()))
						passiveList.add(ai);
					else
						activeList.add(ai);
			}

		if(!activeList.isEmpty())
			aggroList = activeList;
		else
			aggroList = passiveList;

		AggroInfo mosthated = null;

		for(AggroInfo ai : aggroList)
			if(mosthated == null)
				mosthated = ai;
			else if(mosthated.hate < ai.hate)
				mosthated = ai;

		return mosthated != null ? mosthated.attacker : null;
	}

	public L2Character getRandomHated()
	{
		GArray<AggroInfo> aggroList = getAggroList();

		GArray<AggroInfo> activeList = new GArray<AggroInfo>();
		GArray<AggroInfo> passiveList = new GArray<AggroInfo>();

		for(AggroInfo ai : aggroList)
			if(ai.hate > 0)
			{
				L2Playable cha = ai.attacker;
				if(cha != null)
					if(cha.isStunned() || cha.isSleeping() || cha.isParalyzed() || cha.isAfraid() || cha.isBlocked() || Math.abs(cha.getZ() - getZ()) > 200)
						passiveList.add(ai);
					else
						activeList.add(ai);
			}

		if(!activeList.isEmpty())
			aggroList = activeList;
		else
			aggroList = passiveList;

		if(!aggroList.isEmpty())
			return aggroList.get(Rnd.get(aggroList.size())).attacker;
		return null;
	}

	public boolean isNoTarget()
	{
		return getAggroList().size() == 0;
	}

	public void dropItem(L2Player lastAttacker, int itemId, long itemCount)
	{
		if(itemCount == 0 || lastAttacker == null)
			return;

		if(Config.DROP_COUNTER)
			lastAttacker.incrementDropCounter(itemId, itemCount);

		L2ItemInstance item;

		for(long i = 0; i < itemCount; i++)
		{
			item = ItemTemplates.getInstance().createItem(itemId);

			// Set the Item quantity dropped if L2ItemInstance is stackable
			if(item.isStackable())
			{
				i = itemCount; // Set so loop won't happent again
				item.setCount(itemCount); // Set item count
			}

			if(isRaid() || this instanceof L2ReflectionBossInstance)
			{
				SystemMessage sm;
				if(itemId == 57)
				{
					sm = new SystemMessage(SystemMessage.S1_DIED_AND_HAS_DROPPED_S2_ADENA);
					sm.addString(getName());
					sm.addNumber(item.getCount());
				}
				else
				{
					sm = new SystemMessage(SystemMessage.S1_DIED_AND_DROPPED_S3_S2);
					sm.addString(getName());
					sm.addItemName(itemId);
					sm.addNumber(item.getCount());
				}
				broadcastPacket(sm);
			}

			lastAttacker.doAutoLootOrDrop(item, this);
		}
	}

	public void dropItem(L2Player lastAttacker, L2ItemInstance item)
	{
		if(item.getCount() == 0)
			return;

		if(isRaid() || this instanceof L2ReflectionBossInstance)
		{
			SystemMessage sm;
			if(item.getItemId() == 57)
			{
				sm = new SystemMessage(SystemMessage.S1_DIED_AND_HAS_DROPPED_S2_ADENA);
				sm.addString(getName());
				sm.addNumber(item.getCount());
			}
			else
			{
				sm = new SystemMessage(SystemMessage.S1_DIED_AND_DROPPED_S3_S2);
				sm.addString(getName());
				sm.addItemName(item.getItemId());
				sm.addNumber(item.getCount());
			}
			broadcastPacket(sm);
		}

		lastAttacker.doAutoLootOrDrop(item, this);
	}

	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}

	@Override
	public boolean isAttackable(L2Character attacker)
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public void onSpawn()
	{
		setDecayed(false);
		_dieTime = 0;
	}

	@Override
	public void spawnMe()
	{
		super.spawnMe();
		getAI().notifyEvent(CtrlEvent.EVT_SPAWN);
	}

	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) _template;
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	protected boolean _unAggred = false;

	public void setUnAggred(boolean state)
	{
		_unAggred = state;
	}

	/**
	 * Return True if the L2NpcInstance is aggressive (ex : L2MonsterInstance in function of aggroRange).<BR><BR>
	 */
	public boolean isAggressive()
	{
		return getAggroRange() > 0;
	}

	public int getAggroRange()
	{
		if(_unAggred)
			return 0;

		if(_personalAggroRange >= 0)
			return _personalAggroRange;

		return getTemplate().aggroRange;
	}

	/**
	 * Устанавливает данному npc новый aggroRange.
	 * Если установленый aggroRange < 0, то будет братся аггрорейндж с темплейта.
	 * @param aggroRange новый agrroRange
	 */
	public void setAggroRange(int aggroRange)
	{
		_personalAggroRange = aggroRange;
	}

	public int getFactionRange()
	{
		return getTemplate().factionRange;
	}

	/**
	 * Возвращает группу социальности или пустой String (не null)
	 */
	public String getFactionId()
	{
		return getTemplate().factionId;
	}

	public long getExpReward()
	{
		return (long) calcStat(Stats.EXP, getTemplate().revardExp, null, null);
	}

	public long getSpReward()
	{
		return (long) calcStat(Stats.SP, getTemplate().revardSp, null, null);
	}

	@Override
	public void deleteMe()
	{
		super.deleteMe();
		if(_spawn != null)
			_spawn.stopRespawn();
		setSpawn(null);
	}

	private L2Spawn _spawn;

	public L2Spawn getSpawn()
	{
		return _spawn;
	}

	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}

	@Override
	public void onDecay()
	{
		decayLock.lock();
		try
		{
			if(isDecayed())
				return;
			setDecayed(true);

			super.onDecay();

			if(_spawn != null)
				_spawn.decreaseCount(this);
			else
				deleteMe(); // Если этот моб заспавнен не через стандартный механизм спавна значит посмертие ему не положено и он умирает насовсем
		}
		finally
		{
			decayLock.unlock();
		}
	}

	private boolean _isDecayed = false;

	public final void setDecayed(boolean mode)
	{
		_isDecayed = mode;
	}

	public final boolean isDecayed()
	{
		return _isDecayed;
	}

	public void endDecayTask()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
		onDecay();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public void setLevel(byte level)
	{
		_level = level;
	}

	@Override
	public byte getLevel()
	{
		return _level == 0 ? getTemplate().level : _level;
	}

	private int _displayId = 0;

	public void setDisplayId(int displayId)
	{
		_displayId = displayId;
	}

	public int getDisplayId()
	{
		return _displayId > 0 ? _displayId : getTemplate().displayId;
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().rhand;

		if(weaponId < 1)
			return null;

		// Get the weapon item equipped in the right hand of the L2NpcInstance
		L2Item item = ItemTemplates.getInstance().getTemplate(getTemplate().rhand);

		if(!(item instanceof L2Weapon))
			return null;

		return (L2Weapon) item;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instances
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().lhand;

		if(weaponId < 1)
			return null;

		// Get the weapon item equipped in the right hand of the L2NpcInstance
		L2Item item = ItemTemplates.getInstance().getTemplate(getTemplate().lhand);

		if(!(item instanceof L2Weapon))
			return null;

		return (L2Weapon) item;
	}

	@Override
	public void updateAbnormalEffect()
	{
		if(isFlying()) // FIXME
			return;
		for(L2Player _cha : L2World.getAroundPlayers(this))
			_cha.sendPacket(new NpcInfo(this, _cha));
	}

	// У NPC всегда 2
	public void onRandomAnimation()
	{
		broadcastPacket(new SocialAction(getObjectId(), 2));
		_lastSocialAction = System.currentTimeMillis();
	}

	public void startRandomAnimation()
	{
		new RandomAnimationTask(this);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		if(Config.MAX_NPC_ANIMATION <= 0)
			return false;
		if(getTemplate().getAIParams().getBool("randomAnimationDisabled", false))
			return false;
		return true;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}

	public Castle getCastle()
	{
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX && (getReflection().getId() != 0 || isInZone(ZoneType.offshore)))
			return null;
		return TownManager.getInstance().getClosestTown(this).getCastle();
	}

	public Castle getCastle(L2Player player)
	{
		return getCastle();
	}

	private int _fortressId = -1;

	public Fortress getFortress()
	{
		if(_fortressId < 0)
			_fortressId = FortressManager.getInstance().findNearestFortressIndex(getX(), getY(), 32768);
		return FortressManager.getInstance().getFortressByIndex(_fortressId);
	}

	private int _clanHallId = -1;

	public ClanHall getClanHall()
	{
		if(_clanHallId < 0)
			_clanHallId = ClanHallManager.getInstance().findNearestClanHallIndex(getX(), getY(), 32768);
		return ClanHallManager.getInstance().getClanHall(_clanHallId);
	}

	private long _lastSocialAction;

	@Override
	public void onAction(L2Player player, boolean shift)
	{
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			if(player.getTarget() == this)
			{
				player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
				if(isAutoAttackable(player))
					player.sendPacket(makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
			}
			player.sendPacket(new ValidateLocation(this), Msg.ActionFail);
			return;
		}

		if(Events.onAction(player, this, shift))
		{
			player.sendActionFailed();
			return;
		}

		if(isAutoAttackable(player))
		{
			player.getAI().Attack(this, false, shift);
			return;
		}

		if(!isInRange(player, INTERACTION_DISTANCE))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() < 0 && !player.isGM() && !(this instanceof L2WarehouseInstance || this instanceof L2ResidenceManager || this instanceof L2ClanHallDoormenInstance || this instanceof L2CastleDoormenInstance || this instanceof L2FortressDoormenInstance))
		{
			player.sendActionFailed();
			return;
		}

		// С NPC нельзя разговаривать мертвым и сидя
		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isAlikeDead())
			return;

		if(System.currentTimeMillis() - _lastSocialAction > 10000 && !getTemplate().getAIParams().getBool("randomAnimationDisabled", false))
		{
			broadcastPacket(new SocialAction(getObjectId(), 2));
			_lastSocialAction = System.currentTimeMillis();
		}

		player.sendActionFailed();
		player.stopMove(false);

		if(_isBusy)
			showBusyWindow(player);
		else if(hasChatWindow)
		{
			boolean flag = false;
			Quest[] qlst = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
			if(qlst != null && qlst.length > 0)
				for(Quest element : qlst)
				{
					QuestState qs = player.getQuestState(element.getName());
					if((qs == null || !qs.isCompleted()) && element.notifyFirstTalk(this, player))
						flag = true;
				}
			if(!flag)
				showChatWindow(player, 0);
		}
	}

	public void showQuestWindow(L2Player player, String questId)
	{
		if(!player.isQuestContinuationPossible(true))
			return;

		int count = 0;
		for(QuestState quest : player.getAllQuestsStates())
			if(quest != null && ((quest.getQuest().getQuestIntId() < 999 || quest.getQuest().getQuestIntId() > 10000) && quest.getQuest().getQuestIntId() != 255) && quest.isStarted() && quest.getCond() > 0)
				count++;

		if(count > 40)
		{
			showChatWindow(player, "data/html/quest-limit.htm");
			return;
		}

		try
		{
			// Get the state of the selected quest
			QuestState qs = player.getQuestState(questId);
			if(qs != null)
			{
				if(qs.isCompleted())
				{
					Functions.show(new CustomMessage("quests.QuestAlreadyCompleted", player), player);
					return;
				}
				if(qs.getQuest().notifyTalk(this, qs))
					return;
			}
			else
			{
				Quest q = QuestManager.getQuest(questId);
				if(q != null)
				{
					// check for start point
					Quest[] qlst = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					if(qlst != null && qlst.length > 0)
						for(Quest element : qlst)
							if(element == q)
							{
								qs = q.newQuestState(player, Quest.CREATED);
								if(qs.getQuest().notifyTalk(this, qs))
									return;
								break;
							}
				}
			}

			showChatWindow(player, "data/html/no-quest.htm");
		}
		catch(Exception e)
		{
			_log.warning("problem with npc text " + e);
			e.printStackTrace();
		}

		player.sendActionFailed();
	}

	public static boolean canBypassCheck(L2Player player, L2NpcInstance npc)
	{
		if(npc == null || player.isActionsDisabled() || !Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || !npc.isInRange(player, INTERACTION_DISTANCE))
		{
			player.sendActionFailed();
			return false;
		}
		return true;
	}

	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		try
		{
			if(command.equalsIgnoreCase("TerritoryStatus"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("data/html/merchant/territorystatus.htm");
				html.replace("%npcname%", getName());

				Castle castle = getCastle(player);
				if(castle != null && castle.getId() > 0)
				{
					html.replace("%castlename%", castle.getName());
					html.replace("%taxpercent%", String.valueOf(castle.getTaxPercent()));

					if(castle.getOwnerId() > 0)
					{
						L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
						if(clan != null)
						{
							html.replace("%clanname%", clan.getName());
							html.replace("%clanleadername%", clan.getLeaderName());
						}
						else
						{
							html.replace("%clanname%", "unexistant clan");
							html.replace("%clanleadername%", "None");
						}
					}
					else
					{
						html.replace("%clanname%", "NPC");
						html.replace("%clanleadername%", "None");
					}
				}
				else
				{
					html.replace("%castlename%", "Open");
					html.replace("%taxpercent%", "0");

					html.replace("%clanname%", "No");
					html.replace("%clanleadername%", getName());
				}

				player.sendPacket(html);
			}
			else if(command.startsWith("Quest"))
			{
				String quest = command.substring(5).trim();
				if(quest.length() == 0)
					showQuestWindow(player);
				else
					showQuestWindow(player, quest);
			}
			else if(command.startsWith("Chat"))
				try
				{
					int val = Integer.parseInt(command.substring(5));
					showChatWindow(player, val);
				}
				catch(NumberFormatException nfe)
				{
					String filename = command.substring(5).trim();
					if(filename.length() == 0)
						showChatWindow(player, "data/html/npcdefault.htm");
					else
						showChatWindow(player, filename);
				}
			else if(command.startsWith("Loto"))
			{
				int val = Integer.parseInt(command.substring(5));
				showLotoWindow(player, val);
			}
			else if(command.startsWith("AttributeCancel"))
				player.sendPacket(new ExShowBaseAttributeCancelWindow(player, RequestExRemoveItemAttribute.UNENCHANT_PRICE));
			else if(command.startsWith("CPRecovery"))
				makeCPRecovery(player);
			else if(command.startsWith("NpcLocationInfo"))
			{
				int val = Integer.parseInt(command.substring(16));
				L2NpcInstance npc = L2ObjectsStorage.getByNpcId(val);
				if(npc != null)
				{
					// Убираем флажок на карте и стрелку на компасе
					player.sendPacket(new RadarControl(2, 2, npc.getLoc()));
					// Ставим флажок на карте и стрелку на компасе
					player.sendPacket(new RadarControl(0, 1, npc.getLoc()));
				}
			}
			else if(command.startsWith("SupportMagic"))
				makeSupportMagic(player);
			else if(command.startsWith("ProtectionBlessing"))
			{
				// Не выдаём блессиг протекшена ПКшникам.
				if(player.getKarma() > 0)
					return;
				if(player.getLevel() > 39 || player.getClassId().getLevel() >= 3)
				{
					String content = "<html><body>Blessing of protection not available for characters whose level more than 39 or completed second class transfer.</body></html>";
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setHtml(content);
					player.sendPacket(html);
					return;
				}
				doCast(SkillTable.getInstance().getInfo(5182, 1), player, true);
			}
			else if(command.startsWith("Multisell") || command.startsWith("multisell"))
			{
				String listId = command.substring(9).trim();
				Castle castle = getCastle(player);
				L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(listId), player, castle != null ? castle.getTaxRate() : 0);
			}
			else if(command.equalsIgnoreCase("SkillList"))
				showSkillList(player);
			else if(command.equalsIgnoreCase("ClanSkillList"))
				showClanSkillList(player);
			else if(command.equalsIgnoreCase("FishingSkillList"))
				showFishingSkillList(player);
			else if(command.equalsIgnoreCase("newPlayerSkillList"))
				showNpcPlayerSkillList(player);
			else if(command.equalsIgnoreCase("newClanSkillList"))
				showNpcClanSkillList(player);
			else if(command.equalsIgnoreCase("TransformationSkillList"))
				showTransformationSkillList(player);
			else if(command.equalsIgnoreCase("EnchantSkillList"))
				showEnchantSkillList(player, false);
			else if(command.startsWith("SafeEnchantSkillList"))
				showEnchantSkillList(player, true);
			else if(command.startsWith("ChangeEnchantSkillList"))
				showEnchantChangeSkillList(player);
			else if(command.startsWith("UntrainEnchantSkillList"))
				showEnchantUntrainSkillList(player);
			else if(command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				if(cmdChoice == 1)
					player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, Msg.ExShowVariationMakeWindow);
				else if(cmdChoice == 2)
					player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, Msg.ExShowVariationCancelWindow);
			}
			else if(command.startsWith("Link"))
				showChatWindow(player, "data/html/" + command.substring(5));
			else if(command.startsWith("Teleport"))
			{
				if(player.getTransformation() == 111 || player.getTransformation() == 112 || player.getTransformation() == 124) 
					return;
				int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				TeleportLocation[] list = TeleportTable.getInstance().getTeleportLocationList(getNpcId(), cmdChoice);
				if(list != null)
					showTeleportList(player, list);
				else
					player.sendMessage("Ссылка неисправна, сообщите администратору.");
			}
			else if(command.startsWith("goto"))
			{
				int val = Integer.parseInt(command.substring(5));
				doTeleport(player, val);				
			}	
			else if(command.startsWith("open_gate"))
			{
				int val = Integer.parseInt(command.substring(10));
				DoorTable.getInstance().getDoor(val).openMe();
				player.sendActionFailed();
			}
			else if(command.equalsIgnoreCase("TransferSkillList"))
				showTransferSkillList(player);
			else if(command.startsWith("RemoveTransferSkill"))
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();

				//int skill_id = Integer.parseInt(st.nextToken());

				ClassId classId = player.getClassId();
				if(classId != null)
				{
					int item_id = 0;
					switch(classId)
					{
						case cardinal:
							item_id = 15307;
							break;
						case evaSaint:
							item_id = 15308;
							break;
						case shillienSaint:
							item_id = 15309;
							break;
					}

					String var = player.getVar("TransferSkills" + item_id);
					if(var == null || var.isEmpty())
						return;

					String[] skills = var.split(";");

					/*
					var = "";
					for(String skill : skills)
						if(Integer.parseInt(skill) != skill_id)
							var += ";" + skill;

					if(!var.isEmpty())
					{
						var = var.substring(1);
						player.setVar("TransferSkills" + item_id, var);
					}
					else
						player.unsetVar("TransferSkills" + item_id);
					*/

					if(player.getAdena() < 10000000)
					{
						player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						return;
					}

					player.unsetVar("TransferSkills" + item_id); // TODO мб вариант выше правильнее, и нужно удалять по одному?

					for(String skill : skills)
						player.removeSkill(Integer.parseInt(skill), true);

					player.reduceAdena(10000000, true);
					Functions.addItem(player, item_id, skills.length);
				}
			}
			else if(command.equalsIgnoreCase("SquadSkillList"))
				showSquadSkillList(player);
		}
		catch(StringIndexOutOfBoundsException sioobe)
		{
			_log.info("Incorrect htm bypass! npcId=" + getTemplate().npcId + " command=[" + command + "]");
		}
		catch(NumberFormatException nfe)
		{
			_log.info("Invalid bypass to Server command parameter! npcId=" + getTemplate().npcId + " command=[" + command + "]");
		}
	}

	private void doTeleport(L2Player player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			Castle castle = TownManager.getInstance().getClosestTown(list.getLocX(), list.getLocY()).getCastle();
			if(castle != null && castle.getSiege().isInProgress())
			{
				// Определяем, в город ли телепортируется чар
				boolean teleToTown = false;
				int townId = 0;
				for(L2Zone town : ZoneManager.getInstance().getZoneByType(ZoneType.Town))
					if(town.checkIfInZone(list.getLocX(), list.getLocY()))
					{
						teleToTown = true;
						townId = town.getIndex();
						break;
					}

				if(teleToTown && townId == castle.getTown())
				{
					player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
					return;
				}
			}
			
				Location pos = GeoEngine.findPointToStay(list.getLocX(), list.getLocY(), list.getLocZ(), 50, 100, player.getReflection().getGeoIndex());
				player.teleToLocation(pos);
		}
		else
			_log.warning("No teleport destination with id:" + val);
	}
	
	
	public void showTeleportList(L2Player player, TeleportLocation[] list)
	{
		StringBuffer sb = new StringBuffer();

		sb.append("Доступно для перемещения").append("<br>\n");

		if(list != null)
		{
			for(TeleportLocation tl : list)
				if(tl._item.getItemId() == 57)
				{
					float pricemod = player.getLevel() <= Config.GATEKEEPER_FREE ? 0f : Config.GATEKEEPER_MODIFIER;
					if(tl._price > 0 && pricemod > 0)
					{
						int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
						int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
						if(day != 1 && day != 7 && (hour <= 8 || hour >= 24))
							pricemod /= 2;
					}
				//	sb.append("[scripts_Util:Gatekeeper ").append(tl._target).append(" ").append((int) (tl._price * pricemod)).append(" @811;").append(tl._name).append("|").append(tl._name);
				//	if(tl._price > 0)
				//		sb.append(" - ").append((int) (tl._price * pricemod)).append(" Adena");
				//	sb.append("]<br1>\n");
					sb.append("[scripts_Util:Gatekeeper ").append(tl._target).append(" ").append((int) (tl._price * pricemod)).append(" @811;").append(tl._name).append("|").append(tl._name);
					if (pricemod != 0)
					{
						if(tl._price > 0)
							sb.append(" - ").append((int) (tl._price * pricemod)).append(" аден");
							sb.append("]<br1>\n");
					}
					else 
					{
					//	sb.append(" - ").append((int) (tl._price * pricemod)).append(" Adena");
						sb.append("]<br1>\n");
					}
				}
				else
					sb.append("[scripts_Util:QuestGatekeeper ").append(tl._target).append(" ").append(tl._price).append(" ").append(tl._item.getItemId()).append(" @811;").append(tl._name).append("|").append(tl._name).append(" - ").append(tl._price).append(" ").append(tl._item.getName()).append("]<br1>\n");
		}
		else
			sb.append("No teleports available.");

		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(Strings.bbParse(sb.toString()));
		player.sendPacket(html);
	}

	public void showQuestWindow(L2Player player)
	{
		// collect awaiting quests and start points
		GArray<Quest> options = new GArray<Quest>();

		GArray<QuestState> awaits = player.getQuestsForEvent(this, QuestEventType.QUEST_TALK);
		Quest[] starts = getTemplate().getEventQuests(QuestEventType.QUEST_START);

		if(awaits != null)
			for(QuestState x : awaits)
				if(!options.contains(x.getQuest()))
					if(x.getQuest().getQuestIntId() > 0)
						options.add(x.getQuest());

		if(starts != null)
			for(Quest x : starts)
				if(!options.contains(x))
					if(x.getQuestIntId() > 0)
						options.add(x);

		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if(options.size() > 1)
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		else if(options.size() == 1)
			showQuestWindow(player, options.get(0).getName());
		else
			showQuestWindow(player, "");
	}

	public void showQuestChooseWindow(L2Player player, Quest[] quests)
	{
		StringBuffer sb = new StringBuffer();

		sb.append("<html><body><br>");

		for(Quest q : quests)
			if(player.getQuestState(q.getName()) == null)
				sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append("]</a><br>");
			else if(player.getQuestState(q.getName()).isCompleted())
				sb.append("<font color=\"808080\"><a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append(" (завершено)").append("]</a></font><br>");
			else
				sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[").append(q.getDescr(player)).append(" (в процессе)").append("]</a><br>");

		sb.append("</body></html>");

		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}

	public void showChatWindow(L2Player player, int val)
	{
		if(getTemplate().getAIParams().getBool("chatWindowDisabled", false))
			return;

		int npcId = getTemplate().npcId;
		String filename;

		switch(npcId)
		{
			case 31688:
				if(player.isNoble() && player.isAwaking())
					filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
				else
					filename = getHtmlPath(npcId, val);
				break;
			case 31690:
			case 31769:
			case 31770: // Monument of Heroes
			case 31771:
			case 31772:
				if(player.isHero() || Hero.getInstance().isInactiveHero(player.getObjectId()))
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				else
					filename = getHtmlPath(npcId, val);
				break;
			default:
				if(npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
					return;
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = getHtmlPath(npcId, val);
				break;
		}

		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	public void showChatWindow(L2Player player, String filename)
	{
		player.sendPacket(new NpcHtmlMessage(player, this, filename, 0));
	}

	public String getHtmlPath(int npcId, int val)
	{
		String pom;
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		String temp = "data/html/default/" + pom + ".htm";
		File mainText = new File(temp);
		if(mainText.exists())
			return temp;

		temp = "data/html/trainer/" + pom + ".htm";
		mainText = new File(temp);
		if(mainText.exists())
			return temp;

		temp = "data/html/lottery/" + pom + ".htm";
		mainText = new File(temp);
		if(mainText.exists())
			return temp;

		temp = "data/html/instance/kamaloka/" + pom + ".htm";
		mainText = new File(temp);
		if(mainText.exists())
			return temp;

		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "data/html/npcdefault.htm";
	}

	/** For Lottery Manager **/
	public void showLotoWindow(L2Player player, int val)
	{
		int npcId = getTemplate().npcId;
		String filename;
		SystemMessage sm;
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);

		// if loto
		if(val == 0)
		{
			filename = getHtmlPath(npcId, 1);
			html.setFile(filename);
		}

		else if(val >= 1 && val <= 21)
		{
			if(!LotteryManager.getInstance().isStarted())
			{
				/** LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD **/
				player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
				return;
			}
			if(!LotteryManager.getInstance().isSellableTickets())
			{
				/** TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE **/
				player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
				return;
			}

			filename = getHtmlPath(npcId, 5);
			html.setFile(filename);

			int count = 0;
			int found = 0;

			// counting buttons and unsetting button if found
			for(int i = 0; i < 5; i++)
				if(player.getLoto(i) == val)
				{
					// unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if(player.getLoto(i) > 0)
					count++;

			// if not rearched limit 5 and not unseted value
			if(count < 5 && found == 0 && val <= 20)
				for(int i = 0; i < 5; i++)
					if(player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}

			//setting pusshed buttons
			count = 0;
			for(int i = 0; i < 5; i++)
				if(player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if(player.getLoto(i) < 10)
						button = "0" + button;
					String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}
			if(count == 5)
			{
				String search = "0\">Return";
				String replace = "22\">The winner selected the numbers above.";
				html.replace(search, replace);
			}
			player.sendPacket(html);
		}

		if(val == 22)
		{
			if(!LotteryManager.getInstance().isStarted())
			{
				/** LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD **/
				player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
				return;
			}
			if(!LotteryManager.getInstance().isSellableTickets())
			{
				/** TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE **/
				player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
				return;
			}

			int price = Config.SERVICES_ALT_LOTTERY_PRICE;
			int lotonumber = LotteryManager.getInstance().getId();
			int enchant = 0;
			int type2 = 0;
			for(int i = 0; i < 5; i++)
			{
				if(player.getLoto(i) == 0)
					return;
				if(player.getLoto(i) < 17)
					enchant += Math.pow(2, player.getLoto(i) - 1);
				else
					type2 += Math.pow(2, player.getLoto(i) - 17);
			}
			if(player.getAdena() < price)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			player.reduceAdena(price, true);
			sm = new SystemMessage(SystemMessage.ACQUIRED__S1_S2);
			sm.addNumber(lotonumber);
			sm.addItemName(4442);
			player.sendPacket(sm);
			L2ItemInstance item = ItemTemplates.getInstance().createItem(4442);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem(item);
			Log.LogItem(player, Log.BuyItem, item);
			html.setHtml("<html><body>Lottery Ticket Seller:<br>Thank you for playing the lottery<br>The winners will be announced at 7:00 pm <br><center><a action=\"bypass -h npc_%objectId%_Chat 0\">Back</a></center></body></html>");
		}
		else if(val == 23) //23 - current lottery jackpot
		{
			filename = getHtmlPath(npcId, 3);
			html.setFile(filename);
		}
		else if(val == 24)
		{
			filename = getHtmlPath(npcId, 4);
			html.setFile(filename);

			int lotonumber = LotteryManager.getInstance().getId();
			String message = "";

			for(L2ItemInstance item : player.getInventory().getItems())
			{
				if(item == null)
					continue;
				if(item.getItemId() == 4442 && item.getCustomType1() < lotonumber)
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					int[] numbers = LotteryManager.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for(int i = 0; i < 5; i++)
						message += numbers[i] + " ";
					int[] check = LotteryManager.getInstance().checkTicket(item);
					if(check[0] > 0)
					{
						switch(check[0])
						{
							case 1:
								message += "- 1st Prize";
								break;
							case 2:
								message += "- 2nd Prize";
								break;
							case 3:
								message += "- 3th Prize";
								break;
							case 4:
								message += "- 4th Prize";
								break;
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}
			if(message == "")
				message += "There is no winning lottery ticket...<br>";
			html.replace("%result%", message);
		}
		else if(val == 25)
		{
			filename = getHtmlPath(npcId, 2);
			html.setFile(filename);
		}
		else if(val > 25)
		{
			int lotonumber = LotteryManager.getInstance().getId();
			L2ItemInstance item = player.getInventory().getItemByObjectId(val);
			if(item == null || item.getItemId() != 4442 || item.getCustomType1() >= lotonumber)
				return;
			int[] check = LotteryManager.getInstance().checkTicket(item);

			player.sendPacket(SystemMessage.removeItems(4442, 1));

			int adena = check[1];
			if(adena > 0)
				player.addAdena(adena);
			player.getInventory().destroyItem(item, 1, true);
			return;
		}

		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%race%", "" + LotteryManager.getInstance().getId());
		html.replace("%adena%", "" + LotteryManager.getInstance().getPrize());
		html.replace("%ticket_price%", "" + Config.SERVICES_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", "" + Config.SERVICES_LOTTERY_5_NUMBER_RATE * 100);
		html.replace("%prize4%", "" + Config.SERVICES_LOTTERY_4_NUMBER_RATE * 100);
		html.replace("%prize3%", "" + Config.SERVICES_LOTTERY_3_NUMBER_RATE * 100);
		html.replace("%prize2%", "" + Config.SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", "" + DateFormat.getDateInstance().format(LotteryManager.getInstance().getEndDate()));

		player.sendPacket(html);
		player.sendActionFailed();
	}

	public void makeCPRecovery(L2Player player)
	{
		if(getNpcId() != 31225 && getNpcId() != 31226)
			return;
		int neededmoney = 100;
		long currentmoney = player.getAdena();
		if(neededmoney > currentmoney)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		player.reduceAdena(neededmoney, true);
		player.setCurrentCp(player.getMaxCp());
		player.sendPacket(new SystemMessage(SystemMessage.S1_CPS_WILL_BE_RESTORED).addString(player.getName()));
	}

	static int[][] _mageBuff = new int[][] {
			// minlevel maxlevel skill skilllevel
			{ 6, 75, 4322, 1 }, // windwalk
			{ 6, 75, 4323, 1 }, // shield
			{ 6, 75, 5637, 1 }, // Magic Barrier 1
			{ 6, 75, 4328, 1 }, // blessthesoul
			{ 6, 75, 4329, 1 }, // acumen
			{ 6, 75, 4330, 1 }, // concentration
			{ 6, 75, 4331, 1 }, // empower
			{ 16, 34, 4338, 1 }, // life cubic
	};

	static int[][] _warrBuff = new int[][] {
			// minlevel maxlevel skill
			{ 6, 75, 4322, 1 }, // windwalk
			{ 6, 75, 4323, 1 }, // shield
			{ 6, 75, 5637, 1 }, // Magic Barrier 1
			{ 6, 75, 4324, 1 }, // btb
			{ 6, 75, 4325, 1 }, // vampirerage
			{ 6, 75, 4326, 1 }, // regeneration
			{ 6, 39, 4327, 1 }, // haste 1
			{ 40, 75, 5632, 1 }, // haste 2
			{ 16, 34, 4338, 1 }, // life cubic
	};

	static int[][] _summonBuff = new int[][] {
			// minlevel maxlevel skill
			{ 6, 75, 4322, 1 }, // windwalk
			{ 6, 75, 4323, 1 }, // shield
			{ 6, 75, 5637, 1 }, // Magic Barrier 1
			{ 6, 75, 4324, 1 }, // btb
			{ 6, 75, 4325, 1 }, // vampirerage
			{ 6, 75, 4326, 1 }, // regeneration
			{ 6, 75, 4328, 1 }, // blessthesoul
			{ 6, 75, 4329, 1 }, // acumen
			{ 6, 75, 4330, 1 }, // concentration
			{ 6, 75, 4331, 1 }, // empower
			{ 6, 39, 4327, 1 }, // haste 1
			{ 40, 75, 5632, 1 }, // haste 2
	};

	public void makeSupportMagic(L2Player player)
	{
		// Prevent a cursed weapon weilder of being buffed
		if(player.isCursedWeaponEquipped())
			return;
		int lvl = player.getLevel();

		if(lvl < Config.ALT_BUFF_MIN_LEVEL)
		{
			player.sendPacket(new NpcHtmlMessage(player, this, "data/html/default/newbie_nosupport6.htm", 0).replace("%minlevel%", String.valueOf(Config.ALT_BUFF_MIN_LEVEL)));
			return;
		}
		if(lvl > Config.ALT_BUFF_MAX_LEVEL)
		{
			player.sendPacket(new NpcHtmlMessage(player, this, "data/html/default/newbie_nosupport62.htm", 0).replace("%maxlevel%", String.valueOf(Config.ALT_BUFF_MAX_LEVEL)));
			return;
		}

		GArray<L2Character> target = new GArray<L2Character>();
		target.add(player);

		if(!player.isMageClass() || player.getTemplate().race == Race.orc)
		{
			for(int[] buff : _warrBuff)
				if(lvl >= buff[0] && lvl <= buff[1])
				{
					broadcastPacket(new MagicSkillUse(this, player, buff[2], buff[3], 0, 0));
					callSkill(SkillTable.getInstance().getInfo(buff[2], buff[3]), target, true);
				}
		}
		else
			for(int[] buff : _mageBuff)
				if(lvl >= buff[0] && lvl <= buff[1])
				{
					broadcastPacket(new MagicSkillUse(this, player, buff[2], buff[3], 0, 0));
					callSkill(SkillTable.getInstance().getInfo(buff[2], buff[3]), target, true);
				}

		if(ConfigSystem.getBoolean("BuffSummon") && player.getPet() != null && !player.getPet().isDead())
		{
			target.clear();
			target = new GArray<L2Character>();
			target.add(player.getPet());

			for(int[] buff : _summonBuff)
				if(lvl >= buff[0] && lvl <= buff[1])
				{
					broadcastPacket(new MagicSkillUse(this, player.getPet(), buff[2], buff[3], 0, 0));
					callSkill(SkillTable.getInstance().getInfo(buff[2], buff[3]), target, true);
				}
		}
	}

	private boolean _isBusy;
	private String _busyMessage = "";

	public final boolean isBusy()
	{
		return _isBusy;
	}

	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}

	public final String getBusyMessage()
	{
		return _busyMessage;
	}

	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}

	public void showBusyWindow(L2Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("data/html/npcbusy.htm");
		html.replace("%npcname%", getName());
		html.replace("%playername%", player.getName());
		html.replace("%busymessage%", _busyMessage);
		player.sendPacket(html);
	}

	public void showSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		ClassId classId = player.getClassId();

		if(classId == null)
			return;

		int npcId = getTemplate().npcId;

		if(_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if(!(getTemplate().canTeach(classId) || getTemplate().canTeach(classId.getParent(player.getSex()))))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.USUAL);
		int counts = 0;

		GArray<L2SkillLearn> skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		for(L2SkillLearn s : skills)
		{
			if(s.getItemCount() == -1)
				continue;
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
				continue;
			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if(counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);

			if(minlevel > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				TextBuilder sb = new TextBuilder();
				sb.append("<html><head><body>");
				sb.append("You've learned all skills for your class.");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public void showTransferSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}

		ClassId classId = player.getClassId();
		if(classId == null)
			return;

		if(player.getLevel() < 76 || classId.getLevel() < 4)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}

		GArray<L2SkillLearn> skills = new GArray<L2SkillLearn>();

		switch(classId)
		{
			case cardinal:
				skills.addAll(SkillTreeTable.getInstance().getAvailableTransferSkills(player, ClassId.evaSaint));
				skills.addAll(SkillTreeTable.getInstance().getAvailableTransferSkills(player, ClassId.shillienSaint));
				break;
			case evaSaint:
				skills.addAll(SkillTreeTable.getInstance().getAvailableTransferSkills(player, ClassId.cardinal));
				skills.addAll(SkillTreeTable.getInstance().getAvailableTransferSkills(player, ClassId.shillienSaint));
				break;
			case shillienSaint:
				skills.addAll(SkillTreeTable.getInstance().getAvailableTransferSkills(player, ClassId.cardinal));
				skills.addAll(SkillTreeTable.getInstance().getAvailableTransferSkills(player, ClassId.evaSaint));
				break;
		}

		if(skills.isEmpty())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("There is no skills for your class.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.TRANSFER);
		for(L2SkillLearn s : skills)
		{
			if(s.getItemCount() == -1)
				continue;
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk != null)
				asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), 0, 0);
		}

		player.sendPacket(asl);
	}

	public void showEnchantSkillList(L2Player player, boolean isSafeEnchant)
	{
		if(!enchantChecks(player))
			return;
		GArray<L2Skill> skills = SkillTreeTable.getInstance().getSkillsToEnchant(player);
		ExEnchantSkillList esl = new ExEnchantSkillList(isSafeEnchant ? EnchantSkillType.SAFE : EnchantSkillType.NORMAL);
		int counts = 0;
		for(L2Skill s : skills)
		{
			counts++;
			esl.addSkill(s.getId(), s.getDisplayLevel());
		}
		if(counts == 0)
			player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		else
			player.sendPacket(esl);
	}

	public void showEnchantChangeSkillList(L2Player player)
	{
		if(!enchantChecks(player))
			return;
		ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.CHANGE_ROUTE);
		int counts = 0;
		for(L2Skill s : player.getAllSkills())
		{
			if(s.getDisplayLevel() < 100)
				continue;
			counts++;
			esl.addSkill(s.getId(), s.getDisplayLevel());
		}
		if(counts == 0)
			player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		else
			player.sendPacket(esl);
	}

	public void showEnchantUntrainSkillList(L2Player player)
	{
		if(!enchantChecks(player))
			return;
		ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.UNTRAIN);
		int counts = 0;
		for(L2Skill s : player.getAllSkills())
		{
			if(s.getDisplayLevel() < 100)
				continue;
			counts++;
			esl.addSkill(s.getId(), s.getDisplayLevel());
		}
		if(counts == 0)
			player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		else
			player.sendPacket(esl);
	}

	public void showSquadSkillList(L2Player player)
	{
		if(player.getClan() == null || !player.isClanLeader())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("Only the castle owning clan leader can add a squad skill!");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2r.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		GArray<L2SkillLearn> skills = new GArray<L2SkillLearn>();
		skills.addAll(SkillTreeTable.getInstance().getAvailableSquadSkills(player.getClan()));
		if(skills.isEmpty())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("This squad has no available skills to learn.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.CLAN_ADDITIONAL);
		for(L2SkillLearn skill : skills)
			if(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()) != null)
				asl.addSkill(skill.getId(), skill.getLevel(), skill.getLevel(), skill.getRepCost(), skill.getItemId());
		player.sendPacket(asl);
	}

	private boolean enchantChecks(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return false;
		}

		int npcId = getTemplate().npcId;

		if(_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return false;
		}

		if(!(getTemplate().canTeach(player.getClassId()) || getTemplate().canTeach(player.getClassId().getParent(player.getSex()))))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return false;
		}

		if(player.getClassId().getLevel() < 4)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("data/html/skillenchant_notfourthclass.htm");
			player.sendPacket(html);
			return false;
		}

		if(player.getLevel() < 76)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("data/html/skillenchant_levelmismatch.htm");
			player.sendPacket(html);
			return false;
		}

		return true;
	}

	
	public void showNpcPlayerSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2open.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.NPCSKILLLEARN);
		int counts = 0;

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableNewPlayerSkills(player);
		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			int cost = 0;
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if(counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("You've learned all skills.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}
	
	public void showNpcClanSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2open.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			player.sendActionFailed();
			return;
		}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.CLANSKILLLEARN);
		int counts = 0;

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableNewClanSkills(player.getClan());
		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			int cost = 0;
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if(counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setHtml("<html><head><body>You've learned all skills.</body></html>");
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}
	
	public void showFishingSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.FISHING);
		int counts = 0;

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableFishingSkills(player);
		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if(counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("You've learned all skills.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public void showTransformationSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if(!ConfigSystem.getBoolean("AllowLearnTransSkillsWOQuest"))
			if(!player.isQuestCompleted("_136_MoreThanMeetsTheEye"))
			{
				showChatWindow(player, "data/html/trainer/" + getNpcId() + "-noquest.htm");
				return;
			}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.TRANSFORMATION);
		int counts = 0;

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableTransformationSkills(player);
		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 1);
		}

		if(counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append("You've learned all skills.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	public void showClanSkillList(L2Player player)
	{
		if(player.getTransformation() != 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			TextBuilder sb = new TextBuilder();
			sb.append("<html><head><body>");
			sb.append(new CustomMessage("l2rt.gameserver.model.instances.L2NpcInstance.CantTeachBecauseTransformation", player));
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			player.sendActionFailed();
			return;
		}

		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.CLAN);
		int counts = 0;

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableClanSkills(player.getClan());
		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			int cost = s.getRepCost();
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if(counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setHtml("<html><head><body>You've learned all skills.</body></html>");
			player.sendPacket(html);
		}
		else
			player.sendPacket(asl);

		player.sendActionFailed();
	}

	/**
	 * Возвращает режим NPC: свежезаспавненный или нормальное состояние
	 * @return true, если NPC свежезаспавненный
	 */
	public int isShowSpawnAnimation()
	{
		return _showSpawnAnimation;
	}

	public void setShowSpawnAnimation(int value)
	{
		_showSpawnAnimation = value;
	}

	@Override
	public boolean getChargedSoulShot()
	{
		switch(getTemplate().shots)
		{
			case SOUL:
			case SOUL_SPIRIT:
			case SOUL_BSPIRIT:
				return true;
			default:
				return false;
		}
	}

	@Override
	public int getChargedSpiritShot()
	{
		switch(getTemplate().shots)
		{
			case SPIRIT:
			case SOUL_SPIRIT:
				return 1;
			case BSPIRIT:
			case SOUL_BSPIRIT:
				return 2;
			default:
				return 0;
		}
	}

	@Override
	public boolean unChargeShots(boolean spirit)
	{
		//broadcastPacket(new MagicSkillUse(this, spirit ? 2061 : 2039, 1, 0, 0)); пакет больше не шлется, из клиента анимация убрана
		return true;
	}

	@Override
	public float getColRadius()
	{
		return (float) getCollisionRadius();
	}

	@Override
	public float getColHeight()
	{
		return (float) getCollisionHeight();
	}

	public L2Character getTopDamager(Collection<AggroInfo> aggroList)
	{
		AggroInfo top = null;
		for(AggroInfo aggro : aggroList)
			if(aggro.attacker != null && (top == null || aggro.damage > top.damage))
				top = aggro;
		return top != null ? top.attacker : null;
	}

	public int calculateLevelDiffForDrop(int charLevel)
	{
		if(!Config.DEEPBLUE_DROP_RULES)
			return 0;

		int mobLevel = getLevel();
		// According to official data (Prima), deep blue mobs are 9 or more levels below players
		int deepblue_maxdiff = isRaid() || this instanceof L2ReflectionBossInstance ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;

		return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
	}

	public void onClanAttacked(L2NpcInstance attacked_member, L2Character attacker, int damage)
	{
		String my_name = getName();
		String attacked_name = attacked_member.getName();

		if(my_name.startsWith("Lilim ") && attacked_name.startsWith("Nephilim "))
			return;
		if(my_name.startsWith("Nephilim ") && attacked_name.startsWith("Lilim "))
			return;
		if(my_name.startsWith("Lith ") && attacked_name.startsWith("Gigant "))
			return;
		if(my_name.startsWith("Gigant ") && attacked_name.startsWith("Lith "))
			return;

		getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[] { attacked_member, attacker, damage });
	}

	public String getTypeName()
	{
		return getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "");
	}

	@Override
	public String toString()
	{
		return "NPC " + getName() + " [" + getNpcId() + "]";
	}

	public void refreshID()
	{
		_objectId = IdFactory.getInstance().getNextId();
		_storedId = L2ObjectsStorage.refreshId(this);
		getEffectList().setOwner(this);
		getAI().refreshActor(this);
		_moveTaskRunnable.updateStoreId(_storedId);
	}

	private boolean _isUnderground = false;

	public void setUnderground(boolean b)
	{
		_isUnderground = b;
	}

	public boolean isUnderground()
	{
		return _isUnderground;
	}

	public void setWeaponEnchant(int val)
	{
		_weaponEnchant = val;
	}

	public int getWeaponEnchant()
	{
		return _weaponEnchant;
	}

    public void setNpcState(int i) 
	{
        if(npcState != i) 
		{
            npcState = i;
            broadcastPacketToOthers(new ExChangeNpcState(getObjectId(), i));
        }
    }

    public int getNpcState() 
	{
        return npcState;
    }
}