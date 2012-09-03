package l2rt.gameserver.model;

import gnu.trove.map.hash.TIntByteHashMap;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.config.ConfigSystem;
import l2rt.extensions.listeners.MethodCollection;
import l2rt.extensions.listeners.PropertyCollection;
import l2rt.extensions.listeners.StatsChangeListener;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Scripts;
import l2rt.extensions.scripts.Scripts.ScriptClassAndMethod;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.ai.L2PlayableAI.nextAction;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.geodata.GeoMove;
import l2rt.gameserver.instancemanager.TownManager;
import l2rt.gameserver.model.L2ObjectTasks.*;
import l2rt.gameserver.model.L2Skill.SkillTargetType;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.model.L2Skill.TriggerActionType;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.Duel;
import l2rt.gameserver.model.entity.Duel.DuelState;
import l2rt.gameserver.model.entity.Town;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2TrapInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.QuestEventType;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2rt.gameserver.skills.*;
import l2rt.gameserver.skills.Calculator;
import l2rt.gameserver.skills.Formulas.AttackInfo;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.tables.MapRegion;
import l2rt.gameserver.taskmanager.RegenTaskManager;
import l2rt.gameserver.templates.L2CharTemplate;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static l2rt.gameserver.ai.CtrlEvent.EVT_FORGET_OBJECT;
import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

public abstract class L2Character extends L2Object
{
	public enum TargetDirection
	{
		NONE,
		FRONT,
		SIDE,
		BEHIND
	}

	public L2Character getFollowTarget()
	{
		return L2ObjectsStorage.getAsCharacter(followTargetStoreId);
	}

	public void setFollowTarget(L2Character target)
	{
		followTargetStoreId = target == null ? 0 : target.getStoredId();
	}

	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());

	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	public static final int INTERACTION_DISTANCE = 200;

	/** Array containing all clients that need to be notified about hp/mp updates of the L2Character */
	private CopyOnWriteArraySet<L2Character> _statusListener;

	public ScheduledFuture<?> _skillScheduledTask;
	public int _scheduledCastCount;
	public int _scheduledCastInterval;

	public Future<?> _skillTask;
	public Future<?> _skillLaunchedTask;
	public Future<?> _stanceTask;

	private long _stanceInited;

	private double _lastHpUpdate = -99999999;

	protected double _currentCp = 0;
	protected double _currentHp = 1;
	protected double _currentMp = 1;

	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;

	/** HashMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final L2TIntObjectHashMap<L2Skill> _skills = new L2TIntObjectHashMap<L2Skill>();
	protected ConcurrentHashMap<TriggerActionType, ConcurrentLinkedQueue<L2Skill>> _skillsOnAction;

	private L2Skill _castingSkill;

	private long _castInterruptTime;
	private long _animationEndTime;

	/** Table containing all skillId that are disabled */
	protected GCSArray<Integer> _disabledSkills;

	protected EffectList _effectList;

	private boolean _massUpdating;

	private GArray<Stats> _blockedStats;

	/** Map 32 bits (0x00000000) containing all abnormal effect in progress */
	private int _abnormalEffects;
	private int _abnormalEffects2;

	private boolean _flying;
	private boolean _riding;

	private boolean _fakeDeath;
	private boolean _fishing;

	protected boolean _isInvul;
	protected boolean _isPendingRevive;
	protected boolean _isTeleporting;
	protected boolean _overloaded;
	protected boolean _killedAlready;
	protected boolean _killedAlreadyPlayer;
	protected boolean _killedAlreadyPet;

	private long _dropDisabled;

	private byte _isBlessedByNoblesse; // Восстанавливает все бафы после смерти
	private byte _isSalvation; // Восстанавливает все бафы после смерти и полностью CP, MP, HP
	private byte _buffImmunity; // Иммунитет к бафам/дебафам
	private TIntByteHashMap _skillMastery;

	private boolean _afraid;
	private boolean _meditated;
	private boolean _muted;
	private boolean _pmuted;
	private boolean _amuted;
	private boolean _paralyzed;
	private boolean _rooted;
	private boolean _sleeping;
	private boolean _stunned;
	private boolean _imobilised;
	private boolean _confused;
	private boolean _blocked;
	private boolean _healBlocked;

	private boolean _running;

	public Future<?> _moveTask;
	public final MoveNextTask _moveTaskRunnable;
	public boolean isMoving;
	public boolean isFollow;
	protected ArrayList<Location> moveList = new ArrayList<Location>();
	protected Location destination = null;

	/**
	 * при moveToLocation используется для хранения геокоординат в которые мы двигаемся для того что бы избежать повторного построения одного и того же пути
	 * при followToCharacter используется для хранения мировых координат в которых находилась последний раз преследуемая цель для отслеживания необходимости перестраивания пути
	 */
	protected final Location movingDestTempPos = new Location();
	public int _offset;

	protected boolean _forestalling;

	protected long castingTargetStoreId, followTargetStoreId, targetStoreId;

	protected final ArrayList<ArrayList<Location>> _targetRecorder = new ArrayList<ArrayList<Location>>();

	protected long _followTimestamp, _startMoveTime, _arriveTime;
	protected double _previousSpeed = -1;

	private int _heading;

	private final Calculator[] _calculators;

	protected L2CharTemplate _template;
	protected L2CharTemplate _baseTemplate;
	protected L2CharacterAI _ai;

	private static final String EMPTY_STRING = new String();
	protected String _name;
	protected String _title;

	protected HashMap<Integer, Long> _traps;
	protected final ReentrantLock dieLock = new ReentrantLock(), statusListenerLock = new ReentrantLock(),
			regenLock = new ReentrantLock();

	
	/** Трансформация */
	private int _transformationId;
	private int _transformationTemplate;
	private String _transformationName;
	
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);

		// Set its template to the new L2Character
		_template = template;
		_baseTemplate = template;

		_calculators = new Calculator[Stats.NUM_STATS];
		if(isPlayer())
			for(Stats stat : Stats.values())
				_calculators[stat.ordinal()] = new Calculator(stat, this);

		if(template != null && (isNpc() || this instanceof L2Summon))
			if(((L2NpcTemplate) template).getSkills().size() > 0)
				for(L2Skill skill : ((L2NpcTemplate) template).getSkills().values())
					addSkill(skill);

		_moveTaskRunnable = new MoveNextTask(this); //FIXME check hasAI???
		Formulas.addFuncsToNewCharacter(this);
	}

	public final void abortAttack(boolean force, boolean message)
	{
		if(isAttackingNow())
		{
			if(force)
				_isAttackAborted = true;
			getAI().setIntention(AI_INTENTION_ACTIVE);
			if(isPlayer())
			{
				sendActionFailed();
				sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_FAILED).addName(this));
			}
		}
	}

	public final void abortCast(boolean force)
	{
		if(isCastingNow() && (force || canAbortCast()))
		{
			_castInterruptTime = 0;

			L2Skill castingSkill = _castingSkill;
			if(castingSkill != null)
			{
				if(castingSkill.isUsingWhileCasting())
				{
					L2Character target = getAI().getAttackTarget();
					if(target != null)
						target.getEffectList().stopEffect(castingSkill.getId());
				}
				TIntByteHashMap skillMastery = _skillMastery;
				if(skillMastery != null)
					skillMastery.remove(castingSkill.getId());
				_castingSkill = null;
				_flyLoc = null;
			}

			if(_skillTask != null)
			{
				_skillTask.cancel(false); // cancels the skill hit scheduled task
				_skillTask = null;
			}

			if(_skillLaunchedTask != null)
			{
				_skillLaunchedTask.cancel(false); // cancels the skill hit scheduled task
				_skillLaunchedTask = null;
			}

			broadcastPacket(new MagicSkillCanceled(_objectId)); // broadcast packet to stop animations client-side
			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer())
				sendPacket(Msg.CASTING_HAS_BEEN_INTERRUPTED);
		}
	}

	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}

	public boolean absorbAndReflect(L2Character target, L2Skill skill, double damage)
	{
		if(target.isDead())
			return false;

		boolean bow = getActiveWeaponItem() != null && (getActiveWeaponItem().getItemType() == WeaponType.BOW || getActiveWeaponItem().getItemType() == WeaponType.CROSSBOW);

		double value = 0;

		if(skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if(skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0, this, skill);
		else if(skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0, this, null);

		if(value > 0)
		{
			applyReflectDamage(target, damage, 100.);
			return true;
		}

		if(skill != null && skill.isMagic())
			value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0, this, skill);
		else if(skill != null && skill.getCastRange() <= 200)
			value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0, this, skill);
		else if (skill != null && skill.getCastRange() > 200)
			value = target.calcStat(Stats.REFLECT_DAMAGE_DAMAGE_PERCENT, 0, this, skill);
		else if(skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, this, null);

		if(value > 0)
			applyReflectDamage(target, damage, value);

		if(skill != null || bow)
			return false;

		// вампирик
		damage = (int) (damage - target.getCurrentCp());

		if(damage <= 0)
			return false;

		double absorb = calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, target, null);
		if(absorb > 0 && !target.isDoor())
			setCurrentHp(_currentHp + damage * absorb * ConfigSystem.getFloat("AbsorbDamageModifier") / 100, false);

		absorb = calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0, target, null);
		if(absorb > 0 && !target.isHealBlocked(true))
			setCurrentMp(_currentMp + damage * absorb * ConfigSystem.getFloat("AbsorbDamageModifier") / 100);

		return false;
	}

	public void applyReflectDamage(L2Character target, double damage, double reflect)
	{
		double rdmg = damage * reflect / 100.;
		rdmg = Math.min(rdmg, target.getCurrentHp());
		if(isPlayable() && !target.isNpc())
			reduceCurrentHp(rdmg, this, null, true, true, false, false);
		else
			reduceCurrentHp(rdmg, target, null, true, true, false, false);
		if(target.isPlayer() && rdmg >= 1.)
			target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(target).addName(this).addNumber((long) rdmg));
	}

	public void addBlockStats(GArray<Stats> stats)
	{
		if(_blockedStats == null)
			_blockedStats = new GArray<Stats>();
		_blockedStats.addAll(stats);
	}

	public L2Skill addSkill(L2Skill newSkill)
	{
		if(newSkill == null)
			return null;

		L2Skill oldSkill = _skills.get(newSkill.getId());

		if(oldSkill != null && oldSkill.getLevel() == newSkill.getLevel())
			return newSkill;

		// Replace oldSkill by newSkill or Add the newSkill
		_skills.put(newSkill.getId(), newSkill);
		if(newSkill.isOnAction())
			addTriggerableSkill(newSkill);

		// If an old skill has been replaced, remove all its Func objects
		if(oldSkill != null)
			removeStatsOwner(oldSkill);

		// Add Func objects of newSkill to the calculator set of the L2Character
		addStatFuncs(newSkill.getStatFuncs());

		return oldSkill;
	}

	public final synchronized void addStatFunc(Func f)
	{
		if(f == null)
			return;
		int stat = f._stat.ordinal();
		if(_calculators[stat] == null)
			_calculators[stat] = new Calculator(f._stat, this);
		_calculators[stat].addFunc(f);
	}

	public final synchronized void addStatListener(StatsChangeListener l)
	{
		if(l == null || l._stat == null)
			return;
		int stat = l._stat.ordinal();
		if(_calculators[stat] == null)
			_calculators[stat] = new Calculator(l._stat, this);
		_calculators[l._stat.ordinal()].addListener(l);
	}

	public final synchronized void addStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			addStatFunc(f);
	}

	public void altOnMagicUseTimer(L2Character aimingTarget, L2Skill skill)
	{
		if(isAlikeDead())
			return;
		int magicId = skill.getDisplayId();
		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
		GArray<L2Character> targets = skill.getTargets(this, aimingTarget, true);
		broadcastPacket(new MagicSkillLaunched(_objectId, magicId, level, targets, skill.isOffensive()));
		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			if(_currentMp < mpConsume2)
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				return;
			}
			if(skill.isMagic())
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
			else
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
		}
		callSkill(skill, targets, false);
	}

	public void altUseSkill(L2Skill skill, L2Character target)
	{
		if(skill == null)
			return;
		int magicId = skill.getId();
		if(isSkillDisabled(magicId))
		{
			sendReuseMessage(skill);
			return;
		}
		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if(target == null)
				return;
		}

		int itemConsume[] = skill.getItemConsume();

		if(itemConsume[0] > 0)
			for(int i = 0; i < itemConsume.length; i++)
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(Msg.INCORRECT_ITEM_COUNT);
					sendChanges();
					return;
				}

		if(skill.getSoulsConsume() > getConsumedSouls())
		{
			sendPacket(Msg.THERE_IS_NOT_ENOUGHT_SOUL);
			return;
		}

		fireMethodInvoked(MethodCollection.onStartAltCast, new Object[] { skill, target });

		if(skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);

		int level = Math.max(1, getSkillDisplayLevel(magicId));
		Formulas.calcSkillMastery(skill, this);
		long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);
		if(!skill.isToggle())
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay));
		// Не показывать сообщение для хербов и кубиков
		if(!(skill.getId() >= 4049 && skill.getId() <= 4055 || skill.getId() >= 4164 && skill.getId() <= 4166 || skill.getId() >= 2278 && skill.getId() <= 2285 || skill.getId() >= 2512 && skill.getId() <= 2514 || skill.getId() == 5115 || skill.getId() == 5116 || skill.getId() == 2580))
			if(!skill.isHandler())
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));
		// Skill reuse check
		if(reuseDelay > 10)
		{
			disableItem(skill, reuseDelay, reuseDelay);
			disableSkill(skill.getId(), reuseDelay);
		}
		ThreadPoolManager.getInstance().scheduleAi(new AltMagicUseTask(this, target, skill), skill.getHitTime(), isPlayable());
	}

	public void sendReuseMessage(L2Skill skill)
	{
		if(isPet() || isSummon())
		{
			L2Player player = getPlayer();
			if(player != null && isSkillDisabled(skill.getId()))
				player.sendPacket(new SystemMessage(SystemMessage.THAT_PET_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING));
			return;
		}
		if(!isPlayer() || isCastingNow())
			return;
		SkillTimeStamp sts = ((L2Player) this).getSkillReuseTimeStamps().get(skill.getId());
		if(sts == null || !sts.hasNotPassed())
			return;
		long timeleft = sts.getReuseCurrent();
		if(!ConfigSystem.getBoolean("AltShowSkillReuseMessage") && timeleft < 10000 || timeleft < 500)
			return;
		long hours = timeleft / 3600000;
		long minutes = (timeleft - hours * 3600000) / 60000;
		long seconds = (long) Math.ceil((timeleft - hours * 3600000 - minutes * 60000) / 1000.);
		if(hours > 0)
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(hours).addNumber(minutes).addNumber(seconds));
		else if(minutes > 0)
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(minutes).addNumber(seconds));
		else
			sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addSkillName(skill.getId(), skill.getDisplayLevel()).addNumber(seconds));
	}

	public void broadcastPacket(L2GameServerPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacketToOthers(L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		GArray<L2GameServerPacket> packetsNoBuffs = null;
		for(L2GameServerPacket packet : packets)
			if(notShowPacket(packet))
			{
				packetsNoBuffs = new GArray<L2GameServerPacket>(packets.length);
				break;
			}

		if(packetsNoBuffs != null)
			for(L2GameServerPacket packet : packets)
				if(notShowPacket(packet))
					packetsNoBuffs.add(packet);

		for(L2Player target : L2World.getAroundPlayers(this))
			if(target != null && _objectId != target.getObjectId())
				if(packetsNoBuffs != null && target.isNotShowBuffAnim())
					target.sendPackets(packetsNoBuffs);
				else
					target.sendPacket(packets);
	}

	private boolean notShowPacket(L2GameServerPacket packet)
	{
		if(packet instanceof MagicSkillLaunched)
			return !((MagicSkillLaunched) packet).isOffensive();
		if(packet instanceof MagicSkillUse)
		{
			int id = ((MagicSkillUse) packet).getSkillId();
			// Соулшоты (вероятно, можно добавить и другие скиллы)
			return id == 2061 || id == 2160 || id == 2161 || id == 2162 || id == 2163 || id == 2164 || id == 2033 || id == 2008 || id == 2009 || id == 2039 || id == 2150 || id == 2151 || id == 2152 || id == 2153 || id == 2154;
		}
		return false;
	}

	public void addStatusListener(L2Character object)
	{
		if(object == this)
			return;
		statusListenerLock.lock();
		try
		{
			if(_statusListener == null)
				_statusListener = new CopyOnWriteArraySet<L2Character>();
			_statusListener.add(object);
		}
		finally
		{
			statusListenerLock.unlock();
		}
	}

	public void removeStatusListener(L2Character object)
	{
		statusListenerLock.lock();
		try
		{
			if(_statusListener == null)
				return;
			_statusListener.remove(object);
			if(_statusListener.isEmpty())
				_statusListener = null;
		}
		finally
		{
			statusListenerLock.unlock();
		}
	}

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

	public void broadcastStatusUpdate()
	{
		CopyOnWriteArraySet<L2Character> list = _statusListener;

		if(list == null || list.isEmpty())
			return;

		if(!needStatusUpdate())
			return;

		StatusUpdate su = makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.CUR_MP, StatusUpdate.CUR_CP);

		for(L2Character temp : list)
			if(!Config.FORCE_STATUSUPDATE)
			{
				if(temp.getTarget() == this)
					temp.sendPacket(su);
			}
			else
				temp.sendPacket(su);
	}

	public int calcHeading(Location dest)
	{
		if(dest == null)
			return 0;
		if(Math.abs(getX() - dest.x) == 0 && Math.abs(getY() - dest.y) == 0)
			return _heading;
		return calcHeading(dest.x, dest.y);
	}

	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}

	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}

	public final double calcStat(Stats stat, double init, L2Character object, L2Skill skill)
	{
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c == null)
			return init;
		Env env = new Env();
		env.character = this;
		env.target = object;
		env.skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}

	public final double calcStat(Stats stat, L2Character object, L2Skill skill)
	{
		Env env = new Env(this, object, skill);
		stat.getInit().calc(env);
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c != null)
			c.calc(env);
		return env.value;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}

	public void callSkill(L2Skill skill, GArray<L2Character> targets, boolean useActionSkills)
	{
		try
		{
			if(useActionSkills && !skill.isUsingWhileCasting() && _skillsOnAction != null)
			{
				if(skill.isOffensive())
				{
					if(skill.isMagic())
					{
						ConcurrentLinkedQueue<L2Skill> SkillsOnMagicAttack = getTriggerableSkills().get(TriggerActionType.OFFENSIVE_MAGICAL_SKILL_USE);
						if(SkillsOnMagicAttack != null)
							for(L2Skill sk : SkillsOnMagicAttack)
								if(Rnd.chance(sk.getChanceForAction(TriggerActionType.OFFENSIVE_MAGICAL_SKILL_USE)) && sk.checkCondition(this, sk.getAimingTarget(this, this.getTarget()), false, false, true))
								{
									L2Skill.broadcastUseAnimation(sk, this, targets);
									Formulas.calcSkillMastery(sk, this);
									callSkill(sk, targets, false);
								}
					}
					else
					{
						ConcurrentLinkedQueue<L2Skill> SkillsOnSkillAttack = getTriggerableSkills().get(TriggerActionType.OFFENSIVE_PHYSICAL_SKILL_USE);
						if(SkillsOnSkillAttack != null)
							for(L2Skill sk : SkillsOnSkillAttack)
								if(Rnd.chance(sk.getChanceForAction(TriggerActionType.OFFENSIVE_PHYSICAL_SKILL_USE)) && sk.checkCondition(this, sk.getAimingTarget(this, this.getTarget()), false, false, true))
								{
									L2Skill.broadcastUseAnimation(sk, this, targets);
									Formulas.calcSkillMastery(sk, this);
									callSkill(sk, targets, false);
								}
					}

					for(L2Character target : targets)
						useActionSkill(skill, target, this, TriggerActionType.UNDER_SKILL_ATTACK);
				}
				else if(skill.isMagic())
				{
					ConcurrentLinkedQueue<L2Skill> SkillsOnMagicSupport = getTriggerableSkills().get(TriggerActionType.SUPPORT_MAGICAL_SKILL_USE);
					if(SkillsOnMagicSupport != null)
						for(L2Skill sk : SkillsOnMagicSupport)
							if(Rnd.chance(sk.getChanceForAction(TriggerActionType.SUPPORT_MAGICAL_SKILL_USE)) && sk.checkCondition(this, sk.getAimingTarget(this, this.getTarget()), false, false, true))
							{
								L2Skill.broadcastUseAnimation(sk, this, targets);
								Formulas.calcSkillMastery(sk, this);
								callSkill(sk, targets, false);
							}
				}

				if(isPlayer())
				{
					L2Player pl = (L2Player) this;
					for(L2Character target : targets)
						if(target != null && target.isNpc())
						{
							L2NpcInstance npc = (L2NpcInstance) target;
							GArray<QuestState> ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
							if(ql != null)
								for(QuestState qs : ql)
									qs.getQuest().notifySkillUse(npc, skill, qs);
						}
				}
			}

			if(skill.getNegateSkill() > 0)
				for(L2Character target : targets)
					for(L2Effect e : target.getEffectList().getAllEffects())
					{
						L2Skill efs = e.getSkill();
						if(efs.getId() == skill.getNegateSkill() && efs.isCancelable() && (skill.getNegatePower() <= 0 || efs.getPower() <= skill.getNegatePower()))
							e.exit();
					}

			if(skill.getCancelTarget() > 0)
				for(L2Character target : targets)
					if(Rnd.chance(skill.getCancelTarget()))
					{
						if(target.getCastingSkill() != null && (target.getCastingSkill().getSkillType() == SkillType.TAKECASTLE || target.getCastingSkill().getSkillType() == SkillType.TAKEFORTRESS || target.getCastingSkill().getSkillType() == SkillType.TAKEFLAG))
							continue;
						if(!target.isRaid())
						{
							target.abortAttack(true, true);
							target.abortCast(true);
							target.setTarget(null);
						}
					}

			if(skill.isSkillInterrupt())
				for(L2Character target : targets)
					if(!target.isRaid())
					{
						if(target.getCastingSkill() != null && !target.getCastingSkill().isMagic())
							target.abortCast(false);
						target.abortAttack(true, true);
					}

			if(skill.isOffensive())
				startAttackStanceTask();

			skill.getEffects(this, this, false, true);
			skill.useSkill(this, targets);
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}

	public boolean checkBlockedStat(Stats stat)
	{
		return _blockedStats != null && _blockedStats.contains(stat);
	}

	public boolean checkReflectSkill(L2Character attacker, L2Skill skill)
	{
		if(isInvul() || attacker.isInvul() || !skill.isOffensive()) // Не отражаем, если есть неуязвимость, иначе она может отмениться
			return false;
		// Из магических скилов отражаются только скилы наносящие урон по ХП.
		if(skill.isMagic() && (skill.getSkillType() != SkillType.MDAM && skill.getSkillType() != SkillType.DRAIN)) // Дрейн так же не отражается. Однако абсолютно не уверен, что это правильно...
			return false;
		if(Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0, attacker, skill)))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_COUNTERED_C1S_ATTACK).addName(attacker));
			attacker.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(this));
			return true;
		}
		return false;
	}

	public void doCounterAttack(L2Skill skill, L2Character attacker)
	{
		if(isInvul() || attacker.isInvul()) // Не отражаем, если есть неуязвимость, иначе она может отмениться
			return;
		if(skill == null || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200)
			return;
		if(Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0, attacker, skill)))
		{
			double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
			attacker.sendPacket(new SystemMessage(SystemMessage.C1S_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			sendPacket(new SystemMessage(SystemMessage.C1S_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(attacker).addNumber((long) damage));
			attacker.reduceCurrentHp(damage, this, skill, true, true, false, false);
		}
	}

	public final void disableDrop(int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param skillId
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(int skillId, long delay)
	{
		if(delay > 10)
		{
			if(_disabledSkills == null)
				_disabledSkills = new GCSArray<Integer>();
			_disabledSkills.add(skillId);
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkillTask(this, skillId), delay, isPlayable());
		}
	}

	public void doAttack(L2Character target)
	{
		if(target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isAlikeDead() || !isInRange(target, 2000))
			return;

		fireMethodInvoked(MethodCollection.onStartAttack, new Object[] { this, target });

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int sAtk = Math.max(calculateAttackDelay(), 333);
		int ssGrade = 0;

		L2Weapon weaponItem = getActiveWeaponItem();
		if(weaponItem != null)
		{
			if(isPlayer() && weaponItem.getAttackReuseDelay() > 0)
			{
				int reuse = (int) (weaponItem.getAttackReuseDelay() * getReuseModifier(target) * 666 * calcStat(Stats.ATK_BASE, 0, target, null) / 293. / getPAtkSpd());
				if(reuse > 0)
				{
					sendPacket(new SetupGauge(SetupGauge.RED, reuse));
					_attackReuseEndTime = reuse + System.currentTimeMillis() - 75;
					if(reuse > sAtk)
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT, null, null), reuse, isPlayable());
				}
			}

			ssGrade = weaponItem.getCrystalType().externalOrdinal;
		}

		_attackEndTime = sAtk + System.currentTimeMillis() + 10000;
		_isAttackAborted = false;

		Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);

		setHeading(target, true);

		// Select the type of attack to start
		if(weaponItem == null)
			doAttackHitSimple(attack, target, 1., !isPlayer(), sAtk, true);
		else
			switch(weaponItem.getItemType())
			{
				case BOW:
				case CROSSBOW:
					doAttackHitByBow(attack, target, sAtk);
					break;
				case POLE:
					doAttackHitByPole(attack, target, sAtk);
					break;
				case DUAL:
				case DUALFIST:
				case DUALDAGGER:
					doAttackHitByDual(attack, target, sAtk);
					break;
				default:
					doAttackHitSimple(attack, target, 1., true, sAtk, true);
			}

		if(attack.hasHits())
			broadcastPacket(attack);
	}

	private void doAttackHitSimple(Attack attack, L2Character target, double multiplier, boolean unchargeSS, int sAtk, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) (info.damage * multiplier);
			shld1 = info.shld;
			crit1 = info.crit;
		}
		else if(target.isPlayer() && !target.isInvul())
			target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(this));

		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify), sAtk, isPlayable());

		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByBow(Attack attack, L2Character target, int sAtk)
	{
		L2Weapon activeWeapon = getActiveWeaponItem();
		if(activeWeapon == null)
			return;

		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);

		reduceArrowCount();

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;

			int range = activeWeapon.getAttackRange();
			damage1 *= Math.min(range, getDistance(target)) / range * .4 + 0.8; // разброс 20% в обе стороны
		}

		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true), sAtk, isPlayable());

		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;

		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);

		if(!miss1)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage1 = (int) info.damage;
			shld1 = info.shld;
			crit1 = info.crit;
		}

		if(!miss2)
		{
			AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			damage2 = (int) info.damage;
			shld2 = info.shld;
			crit2 = info.crit;
		}

		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false), sAtk / 2, isPlayable());
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true), sAtk, isPlayable());

		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}

	private void doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, 90, target, null);
		int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, target, null);

		// Используем Math.round т.к. обычный кастинг обрезает к меньшему
		// double d = 2.95. int i = (int)d, выйдет что i = 2
		// если 1% угла или 1 дистанции не играет огромной роли, то для
		// количества целей это критично
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGERT_COUNT, 3, target, null));

		if(isBoss())
			attackcountmax += 27;
		else if(isRaid())
			attackcountmax += 12;
		else if(isMonster() && getLevel() > 0)
			attackcountmax += getLevel() / 7.5;

		double mult = 1;
		int attackcount = 1;

		for(L2Character t : getAroundCharacters(range, 200))
			if(attackcount <= attackcountmax)
			{
				if(t != null && !t.isDead() && t.isAutoAttackable(this))
				{
					if(t == getAI().getAttackTarget() || !isInFront(t, angle))
						continue;
					doAttackHitSimple(attack, t, mult, attackcount == 0, sAtk, false);
					mult *= 0.85;
					attackcount++;
				}
			}
			else
				break;

		doAttackHitSimple(attack, target, 1., true, sAtk, true);
	}

	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}

	public void doCast(L2Skill skill, L2Character target, boolean forceUse)
	{
		// Прерывать дуэли если цель не дуэлянт
		if(getDuel() != null)
			if(target.getDuel() != getDuel())
				getDuel().setDuelState(getPlayer().getStoredId(), DuelState.Interrupted);
			else if(isPlayer() && getDuel().getDuelState(getStoredId()) == DuelState.Interrupted)
			{
				sendPacket(Msg.INVALID_TARGET);
				return;
			}

		if(skill == null)
		{
			sendActionFailed();
			return;
		}

		int itemConsume[] = skill.getItemConsume();

		if(itemConsume[0] > 0)
			for(int i = 0; i < itemConsume.length; i++)
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(Msg.INCORRECT_ITEM_COUNT);
					sendChanges();
					return;
				}

		int magicId = skill.getId();

		if(target == null)
			target = skill.getAimingTarget(this, getTarget());
		if(target == null)
			return;

		fireMethodInvoked(MethodCollection.onStartCast, new Object[] { skill, target, forceUse });

		setHeading(target, true);

		int level = Math.max(1, getSkillDisplayLevel(magicId));

		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
		int skillInterruptTime = skill.isMagic() ? Formulas.calcMAtkSpd(this, skill, skill.getSkillInterruptTime()) : 0;

		int minCastTime = Math.min(ConfigSystem.getInt("SkillsCastTimeMin"), skill.getHitTime());
		if(skillTime < minCastTime)
		{
			skillTime = minCastTime;
			skillInterruptTime = 0;
		}

		_animationEndTime = System.currentTimeMillis() + skillTime;

		if(skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritShot() > 0)
		{
			skillTime = (int) (0.70 * skillTime);
			skillInterruptTime = (int) (0.70 * skillInterruptTime);
		}

		Formulas.calcSkillMastery(skill, this); // Calculate skill mastery for current cast
		long reuseDelay = Math.max(500, Formulas.calcSkillReuseDelay(this, skill));

		broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay));

		disableItem(skill, reuseDelay, reuseDelay);
		disableSkill(skill.getId(), reuseDelay);

		if(isPlayer())
			if(!skill.isHandler())
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addSkillName(magicId, level));
			else
				sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addItemName(skill.getItemConsumeId()[0]));

		if(skill.getTargetType() == SkillTargetType.TARGET_HOLY)
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);

		double mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : skill.getMpConsume1();
		if(mpConsume1 > 0)
			if(skill.isMusic())
			{
				double inc = mpConsume1 / 2;
				double add = 0;
				for(L2Effect e : getEffectList().getAllEffects())
					if(e.getSkill().getId() != skill.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30000)
						add += inc;
				mpConsume1 += add;
				mpConsume1 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume1, target, skill);
			}
			else if(skill.isMagic())
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume1, target, skill), null);
			else
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume1, target, skill), null);

		_flyLoc = null;
		if(skill.getFlyType() == FlyType.DUMMY || skill.getFlyType() == FlyType.CHARGE)
		{
			Location flyLoc = getFlyLocation(target, skill);
			if(flyLoc != null)
			{
				_flyLoc = flyLoc;
				broadcastPacket(new FlyToLocation(this, flyLoc, skill.getFlyType()));
			}
			else
			{
				sendPacket(Msg.CANNOT_SEE_TARGET);
				return;
			}
		}

		_castingSkill = skill;
		_castInterruptTime = System.currentTimeMillis() + skillInterruptTime;
		setCastingTarget(target);

		if(skill.isUsingWhileCasting())
			callSkill(skill, skill.getTargets(this, target, forceUse), true);

		if(isPlayer())
			sendPacket(new SetupGauge(SetupGauge.BLUE, skillTime));

		_scheduledCastCount = skill.getCastCount();
		_scheduledCastInterval = skill.getCastCount() > 0 ? skillTime / _scheduledCastCount : skillTime;

		// Create a task MagicUseTask with Medium priority to launch the MagicSkill at the end of the casting time
		_skillLaunchedTask = ThreadPoolManager.getInstance().scheduleAi(new MagicLaunchedTask(this, forceUse), skillInterruptTime, isPlayable());
		_skillTask = ThreadPoolManager.getInstance().scheduleAi(new MagicUseTask(this, forceUse), skill.getCastCount() > 0 ? skillTime / skill.getCastCount() : skillTime, isPlayable());
	}

	private Location _flyLoc;

	public Location getFlyLocation(L2Object target, L2Skill skill)
	{
		if(target != null && target != this)
		{
			Location loc = target.getLoc();

			double radian = Util.convertHeadingToRadian(target.getHeading());
			if(skill.isFlyToBack())
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 40), target.getY() - (int) (Math.cos(radian) * 40), target.getZ());
			else
				loc = new Location(target.getX() - (int) (Math.sin(radian) * 40), target.getY() + (int) (Math.cos(radian) * 40), target.getZ());

			if(isFlying())
			{
				if(isPlayer() && ((L2Player) this).isInFlyingTransform() && (loc.z <= 0 || loc.z >= 6000))
					return null;
				if(GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getReflection().getGeoIndex()) == null)
					return null;
			}
			else
			{
				loc.correctGeoZ();
				if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getReflection().getGeoIndex()))
				{
					loc = target.getLoc(); // Если не получается встать рядом с объектом, пробуем встать прямо в него
					if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getReflection().getGeoIndex()))
						return null;
				}
			}

			return loc;
		}

		double radian = Util.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian) * skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian) * skill.getFlyRadius());

		if(isFlying())
			return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getColRadius(), getReflection().getGeoIndex());
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getReflection().getGeoIndex());
	}

	public void doDie(L2Character killer)
	{
		// killing is only possible one time
		dieLock.lock();
		try
		{
			if(_killedAlready)
				return;
			_killedAlready = true;
		}
		finally
		{
			dieLock.unlock();
		}

		fireMethodInvoked(MethodCollection.doDie, new Object[] { killer });

		if(killer != null)
		{
			killer.fireMethodInvoked(MethodCollection.onKill, new Object[] { this });
			if(isPlayer() && killer.isPlayable())
				_currentCp = 0;
		}

		setTarget(null);
		stopMove();

		_currentHp = 0;

		// Stop all active skills effects in progress on the L2Character
		setMassUpdating(true);
		if(isBlessedByNoblesse() || isSalvation())
		{
			if(isSalvation() && !getPlayer().isInOlympiadMode())
				getPlayer().reviveRequest(getPlayer(), 100, false);
			for(L2Effect e : getEffectList().getAllEffects())
				// Noblesse Blessing Buff/debuff effects are retained after
				// death. However, Noblesse Blessing and Lucky Charm are lost as normal.
				if(e.getEffectType() == EffectType.BlessNoblesse || e.getSkill().getId() == L2Skill.SKILL_FORTUNE_OF_NOBLESSE || e.getSkill().getId() == L2Skill.SKILL_RAID_BLESSING)
					e.exit();
		}
		else
			for(L2Effect e : getEffectList().getAllEffects())
				// Трансформы и Battlefield Death Syndrome при смерти не слетают.
				// Charm of Courage тоже, он удаляется позже
				if(e.getEffectType() != EffectType.Transformation && e.getSkill().getId() != L2Skill.SKILL_CHARM_OF_COURAGE && e.getSkill().getId() != L2Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME)
					e.exit();

		setMassUpdating(false);
		sendChanges();
		updateEffectIcons();

		broadcastStatusUpdate();

		ThreadPoolManager.getInstance().executeGeneral(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null));

		Object[] script_args = new Object[] { this, killer };
		for(ScriptClassAndMethod handler : Scripts.onDie)
			callScripts(handler.scriptClass, handler.method, script_args);
	}

	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		if(!isTeleporting())
		{
			setIsPendingRevive(false);

			if(isSalvation())
			{
				for(L2Effect e : getEffectList().getAllEffects())
					if(e.getEffectType() == EffectType.Salvation)
					{
						e.exit();
						break;
					}
				setCurrentCp(getMaxCp());
				setCurrentHp(getMaxHp(), true);
				setCurrentMp(getMaxMp());
			}
			else
			{
				if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0)
					setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);

				setCurrentHp(Math.max(1, getMaxHp() * Config.RESPAWN_RESTORE_HP), true);

				if(Config.RESPAWN_RESTORE_MP >= 0)
					setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			}

			broadcastPacket(new Revive(this));
		}
		else
			setIsPendingRevive(true);
	}

	public void enableSkill(Integer skillId)
	{
		if(_disabledSkills == null)
			return;
		_disabledSkills.remove(skillId);
	}

	/**
	 * Return a map of 32 bits (0x00000000) containing all abnormal effects
	 */
	public int getAbnormalEffect()
	{
		return _abnormalEffects;
	}

	/**
	 * Return a map of 32 bits (0x00000000) containing all special effects
	 */
	public int getAbnormalEffect2()
	{
		return _abnormalEffects2;
	}

	public int getAccuracy()
	{
		return (int) (calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / getWeaponExpertisePenalty());
	}

	public int getMAccuracy()
	{
		return (int) (calcStat(Stats.MACCURACY_COMBAT, 0, null, null) / getWeaponExpertisePenalty());
	}

	/**
	 * Возвращает тип атакующего элемента и его силу.
	 * @return массив, в котором:
	 * <li>[0]: тип элемента,
	 * <li>[1]: его сила
	 */
	public int[] getAttackElement()
	{
		return Formulas.calcAttackElement(this);
	}

	/**
	 * Возвращает защиту от элемента: огонь.
	 * @return значение защиты
	 */
	public int getDefenceFire()
	{
		return (int) -calcStat(Stats.FIRE_RECEPTIVE, 0, null, null);
	}

	/**
	 * Возвращает защиту от элемента: вода.
	 * @return значение защиты
	 */
	public int getDefenceWater()
	{
		return (int) -calcStat(Stats.WATER_RECEPTIVE, 0, null, null);
	}

	/**
	 * Возвращает защиту от элемента: воздух.
	 * @return значение защиты
	 */
	public int getDefenceWind()
	{
		return (int) -calcStat(Stats.WIND_RECEPTIVE, 0, null, null);
	}

	/**
	 * Возвращает защиту от элемента: земля.
	 * @return значение защиты
	 */
	public int getDefenceEarth()
	{
		return (int) -calcStat(Stats.EARTH_RECEPTIVE, 0, null, null);
	}

	/**
	 * Возвращает защиту от элемента: свет.
	 * @return значение защиты
	 */
	public int getDefenceHoly()
	{
		return (int) -calcStat(Stats.SACRED_RECEPTIVE, 0, null, null);
	}

	/**
	 * Возвращает защиту от элемента: тьма.
	 * @return значение защиты
	 */
	public int getDefenceUnholy()
	{
		return (int) -calcStat(Stats.UNHOLY_RECEPTIVE, 0, null, null);
	}

	/**
	 * Возвращает коллекцию скиллов для быстрого перебора
	 */
	public L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];
		
		return _skills.values(new L2Skill[0]);
	}

	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}

	public final float getAttackSpeedMultiplier()
	{
		return (float) (1.1 * getPAtkSpd() / getTemplate().basePAtkSpd);
	}

	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, ConfigSystem.getInt("BuffLimit"), null, null);
	}
	
	public int getSongLimit()
	{
		return (int) calcStat(Stats.SONG_LIMIT, ConfigSystem.getInt("SongLimit"), null, null);
	}

	public L2Skill getCastingSkill()
	{
		return _castingSkill;
	}

	public final L2Character getCharTarget()
	{
		L2Object target = getTarget();
		if(target == null || !target.isCharacter())
			return null;
		return (L2Character) target;
	}

	public byte getCON()
	{
		return (byte) calcStat(Stats.STAT_CON, _template.baseCON, null, null);
	}

	/**
	 * Возвращает шанс физического крита (1000 == 100%)
	 */
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return (int) calcStat(Stats.CRITICAL_BASE, _template.baseCritRate, target, skill);
	}

	/**
	 * Возвращает шанс магического крита в процентах
	 */
	public double getMagicCriticalRate(L2Character target, L2Skill skill)
	{
		return calcStat(Stats.MCRITICAL_RATE, target, skill);
	}

	/**
	 * Return the current CP of the L2Character.
	 * 
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}

	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}

	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100f;
	}

	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}

	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1;
	}

	public final double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}

	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100f;
	}

	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}

	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1;
	}

	public final double getCurrentMp()
	{
		return _currentMp;
	}

	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}

	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100f;
	}

	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}

	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1;
	}

	public Location getDestination()
	{
		return destination;
	}

	public byte getDEX()
	{
		return (byte) calcStat(Stats.STAT_DEX, _template.baseDEX, null, null);
	}

	public int getEvasionRate(L2Character target)
	{
		return (int) (calcStat(Stats.EVASION_RATE, 0, target, null) / getArmourExpertisePenalty());
	}	
	
	public int getMEvasionRate(L2Character target)
	{
		return (int) (calcStat(Stats.MEVASION_RATE, 0, target, null) / getArmourExpertisePenalty());
	}

	/**
	 * If <b>boolean toChar is true heading calcs this->target, else target->this.
	 */
	public int getHeadingTo(L2Object target, boolean toChar)
	{
		if(target == null || target == this)
			return -1;

		int dx = target.getX() - getX();
		int dy = target.getY() - getY();
		int heading = (int) (Math.atan2(-dy, -dx) * HEADINGS_IN_PI + 32768);

		heading = toChar ? target.getHeading() - heading : getHeading() - heading;

		if(heading < 0)
			heading = heading + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(heading > 0xFFFF)
			heading &= 0xFFFF;

		return heading;
	}

	public TargetDirection getDirectionTo(L2Object target, boolean toChar)
	{
		int targeth = getHeadingTo(target, toChar);
		if(targeth == -1)
			return TargetDirection.NONE;
		if(targeth <= 10923 || targeth >= 54613)
			return TargetDirection.BEHIND;
		if(targeth >= 21845 && targeth <= 43691)
			return TargetDirection.FRONT;
		return TargetDirection.SIDE;
	}

	public byte getINT()
	{
		return (byte) calcStat(Stats.STAT_INT, _template.baseINT, null, null);
	}

	public GArray<L2Character> getAroundCharacters(int radius, int height)
	{
		if(!isVisible())
			return new GArray<L2Character>(0);
		return L2World.getAroundCharacters(this, radius, height);
	}

	public GArray<L2NpcInstance> getAroundNpc(int range, int height)
	{
		if(!isVisible())
			return new GArray<L2NpcInstance>(0);
		return L2World.getAroundNpc(this, range, height);
	}

	public boolean knowsObject(L2Object obj)
	{
		return L2World.getAroundObjectById(this, obj.getObjectId()) != null;
	}

	public final L2Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}

	public final int getMagicalAttackRange(L2Skill skill)
	{
		if(skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getTemplate().baseAtkRange;
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		return (int) calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
	}

	public int getMAtkSpd()
	{
		return (int) (calcStat(Stats.MAGIC_ATTACK_SPEED, _template.baseMAtkSpd, null, null) / getArmourExpertisePenalty());
	}

	public final int getMaxCp()
	{
		return (int) calcStat(Stats.MAX_CP, _template.baseCpMax, null, null);
	}

	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _template.baseHpMax, null, null);
	}

	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _template.baseMpMax, null, null);
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, _template.baseMDef, target, skill), 1);
	}

	public byte getMEN()
	{
		return (byte) calcStat(Stats.STAT_MEN, _template.baseMEN, null, null);
	}

	public float getMinDistance(L2Object obj)
	{
		float distance = getTemplate().collisionRadius;

		if(obj != null && obj.isCharacter())
			distance += ((L2Character) obj).getTemplate().collisionRadius;

		return distance;
	}

	public float getMovementSpeedMultiplier()
	{
		return getRunSpeed() * 1f / _template.baseRunSpd;
	}

	@Override
	public float getMoveSpeed()
	{
		if(isRunning())
			return getRunSpeed();

		return getWalkSpeed();
	}

	@Override
	public String getName()
	{
		return _name == null ? EMPTY_STRING : _name;
	}

	public int getPAtk(L2Character target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _template.basePAtk, target, null);
	}

	public int getPAtkSpd()
	{
		return (int) (calcStat(Stats.POWER_ATTACK_SPEED, _template.basePAtkSpd, null, null) / getArmourExpertisePenalty());
	}

	public int getPDef(L2Character target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _template.basePDef, target, null);
	}

	public final int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
	}

	public final int getRandomDamage()
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		if(weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());
		return weaponItem.getRandomDamage();
	}

	public double getReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}

	public int getRunSpeed()
	{
		return getSpeed(_template.baseRunSpd);
	}

	public final int getShldDef()
	{
		if(isPlayer())
			return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
		return (int) calcStat(Stats.SHIELD_DEFENCE, _template.baseShldDef, null, null);
	}

	public final short getSkillDisplayLevel(Integer skillId)
	{
		L2Skill skill = _skills.get(skillId);
		if(skill == null)
			return -1;
		return skill.getDisplayLevel();
	}

	public final short getSkillLevel(Integer skillId)
	{
		L2Skill skill = _skills.get(skillId);
		if(skill == null)
			return -1;
		return skill.getLevel();
	}

	public byte getSkillMastery(Integer skillId)
	{
		if(_skillMastery == null)
			return 0;
		Byte val = _skillMastery.get(skillId);
		return val == null ? 0 : val;
	}

	public void removeSkillMastery(Integer skillId)
	{
		if(_skillMastery != null)
			_skillMastery.remove(skillId);
	}

	public final GArray<L2Skill> getSkillsByType(SkillType type)
	{
		GArray<L2Skill> result = new GArray<L2Skill>();
		for(L2Skill sk : _skills.values())
			if(sk.getSkillType() == type)
				result.add(sk);
		return result;
	}

	public int getSpeed(int baseSpeed)
	{
		if(isInWater())
			return getSwimSpeed();
		return (int) (calcStat(Stats.RUN_SPEED, baseSpeed, null, null) / getArmourExpertisePenalty() + 0.5);
	}

	public byte getSTR()
	{
		return (byte) calcStat(Stats.STAT_STR, _template.baseSTR, null, null);
	}

	public int getSwimSpeed()
	{
		return (int) calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
	}

	public L2Object getTarget()
	{
		return L2ObjectsStorage.get(targetStoreId);
	}

	public final int getTargetId()
	{
		L2Object target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}

	public L2CharTemplate getTemplate()
	{
		return _template;
	}

	public L2CharTemplate getBaseTemplate()
	{
		return _baseTemplate;
	}

	public String getTitle()
	{
		if(_title != null && _title.length() > 16)
			return _title.substring(0, 16);
		return _title;
	}

	public final int getWalkSpeed()
	{
		if(isInWater())
			return getSwimSpeed();
		return getSpeed(_template.baseWalkSpd);
	}

	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}

	public byte getWIT()
	{
		return (byte) calcStat(Stats.STAT_WIT, _template.baseWIT, null, null);
	}

	public double headingToRadians(int heading)
	{
		return (heading - 32768) / HEADINGS_IN_PI;
	}

	public final boolean isAlikeDead()
	{
		return _fakeDeath || _currentHp < 0.5;
	}

	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}

	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}

	public boolean isBehindTarget()
	{
		if(getTarget() != null && getTarget().isCharacter())
		{
			int head = getHeadingTo(getTarget(), true);
			return head != -1 && (head <= 10430 || head >= 55105);
		}
		return false;
	}

	public boolean isToSideOfTarget()
	{
		if(getTarget() != null && getTarget().isCharacter())
		{
			int head = getHeadingTo(getTarget(), true);
			return head != -1 && (head <= 22337 || head >= 43197);
		}
		return false;
	}

	public boolean isToSideOfTarget(L2Object target)
	{
		if(target != null && target.isCharacter())
		{
			int head = getHeadingTo(target, true);
			return head != -1 && (head <= 22337 || head >= 43197);
		}
		return false;
	}

	public boolean isBehindTarget(L2Object target)
	{
		if(target != null && target.isCharacter())
		{
			int head = getHeadingTo(target, true);
			return head != -1 && (head <= 10430 || head >= 55105);
		}
		return false;
	}

	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse > 0;
	}

	public final boolean isSalvation()
	{
		return _isSalvation > 0;
	}

	public final boolean isEffectImmune()
	{
		return _buffImmunity > 0;
	}

	public boolean isDead()
	{
		return _currentHp < 0.5;
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	@Override
	public final boolean isFlying()
	{
		return _flying;
	}

	public final boolean isInCombat()
	{
		return _stanceTask != null;
	}

	/**
	 * Return True if the target is front L2Character and can be seen.
	 * degrees = 0..180, front->sides->back
	 */
	public boolean isInFront(L2Object target, int degrees)
	{
		int head = getHeadingTo(target, false);
		return head <= 32768 * degrees / 180 || head >= 65536 - 32768 * degrees / 180;
	}

	public boolean isInvul()
	{
		return _isInvul;
	}

	/** Отображение значка клана у НПЦ */
	public boolean isCrestEnable()
	{
		return true;
	}

	public boolean isMageClass()
	{
		return getTemplate().baseMAtk > 3;
	}

	public final boolean isRiding()
	{
		return _riding;
	}

	public final boolean isRunning()
	{
		return _running;
	}

	public boolean isSkillDisabled(Integer skillId)
	{
		return _disabledSkills != null && _disabledSkills.contains(skillId);
	}

	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}

	/**
	 * Возвращает позицию цели, в которой она будет через пол секунды.
	 */
	public Location getIntersectionPoint(L2Character target)
	{
		if(!isInFront(target, 90))
			return new Location(target.getX(), target.getY(), target.getZ());
		double angle = Util.convertHeadingToDegree(target.getHeading()); // угол в градусах
		double radian = Math.toRadians(angle - 90); // угол в радианах
		double range = target.getMoveSpeed() / 2; // расстояние, пройденное за 1 секунду, равно скорости. Берем половину.
		return new Location((int) (target.getX() - range * Math.sin(radian)), (int) (target.getY() + range * Math.cos(radian)), target.getZ());
	}

	public Location applyOffset(Location point, int offset)
	{
		if(offset <= 0)
			return point;

		long dx = point.x - getX();
		long dy = point.y - getY();
		long dz = point.z - getZ();

		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if(distance <= offset)
		{
			point.set(getX(), getY(), getZ());
			return point;
		}

		if(distance >= 1)
		{
			double cut = offset / distance;
			point.x -= (int) (dx * cut + 0.5f);
			point.y -= (int) (dy * cut + 0.5f);
			point.z -= (int) (dz * cut + 0.5f);

			if(!isFlying() && !isInVehicle() && !isSwimming() && !isVehicle())
				point.correctGeoZ();
		}

		return point;
	}

	public ArrayList<Location> applyOffset(ArrayList<Location> points, int offset)
	{
		offset = offset >> 4;
		if(offset <= 0)
			return points;

		long dx = points.get(points.size() - 1).x - points.get(0).x;
		long dy = points.get(points.size() - 1).y - points.get(0).y;
		long dz = points.get(points.size() - 1).z - points.get(0).z;

		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(distance <= offset)
		{
			Location point = points.get(0);
			points.clear();
			points.add(point);
			return points;
		}

		if(distance >= 1)
		{
			double cut = offset / distance;
			int num = (int) (points.size() * cut + 0.5f);
			for(int i = 1; i <= num && points.size() > 0; i++)
				points.remove(points.size() - 1);
		}

		return points;
	}

	public boolean setSimplePath(Location dest)
	{
		ArrayList<Location> moveList = GeoMove.constructMoveList(getLoc(), dest);
		if(moveList.isEmpty())
			return false;
		_targetRecorder.clear();
		_targetRecorder.add(moveList);
		return true;
	}

	public boolean buildPathTo(int dest_x, int dest_y, int dest_z, int offset, boolean pathFind, boolean _follow)
	{
		int ref = getReflection().getGeoIndex();

		Location dest;

		if(_forestalling && isFollow && getFollowTarget() != null && getFollowTarget().isMoving)
			dest = getIntersectionPoint(getFollowTarget());
		else
			dest = new Location(dest_x, dest_y, dest_z);

		if(isInVehicle() || isVehicle())
		{
			applyOffset(dest, offset);
			return setSimplePath(dest);
		}

		if(isFlying() || isSwimming() || isInWater() || L2World.isWater(dest))
		{
			applyOffset(dest, offset);

			if(GeoEngine.canSeeCoord(this, dest.x, dest.y, dest.z, isFlying()))
				return setSimplePath(dest);

			Location nextloc;

			if(isFlying())
				nextloc = GeoEngine.moveCheckInAir(getX(), getY(), getZ(), dest.x, dest.y, dest.z, getColRadius(), ref);
			else
				nextloc = GeoEngine.moveInWaterCheck(getX(), getY(), getZ(), dest.x, dest.y, dest.z, ref);

			if(nextloc != null && !nextloc.equals(getX(), getY(), getZ()))
				return setSimplePath(nextloc);

			return false;
		}

		ArrayList<Location> moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, ref, true); // onlyFullPath = true - проверяем весь путь до конца
		if(moveList != null) // null - до конца пути дойти нельзя
		{
			if(moveList.isEmpty()) // уже стоим на нужной клетке
				return false;
			applyOffset(moveList, offset);
			if(moveList.isEmpty()) // уже стоим на нужной клетке
				return false;
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return true;
		}
		if(!ConfigSystem.getBoolean("GeodataEnabled"))
        {
			applyOffset(dest, offset);
			setSimplePath(dest);
            return true;
		}
		if(pathFind)
		{
            ArrayList<ArrayList<Location>> targets = GeoMove.findMovePath(getX(), getY(), getZ(), dest.clone(), this, true, ref);
			if(!targets.isEmpty())
			{
				moveList = targets.remove(targets.size() - 1);
				applyOffset(moveList, offset);
				if(!moveList.isEmpty())
					targets.add(moveList);
				if(!targets.isEmpty())
				{
					_targetRecorder.clear();
					_targetRecorder.addAll(targets);
					return true;
				}
			}
		}

		if(_follow)
			return false;

		applyOffset(dest, offset);

		moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, ref, false); // onlyFullPath = false - идем до куда можем
		if(moveList != null && !moveList.isEmpty()) // null - нет геодаты, empty - уже стоим на нужной клетке
		{
			_targetRecorder.clear();
			_targetRecorder.add(moveList);
			return true;
		}

		return false;
	}

	public boolean followToCharacter(L2Character target, int offset, boolean forestalling)
	{
		synchronized (_targetRecorder)
		{
			offset = Math.max(offset, 10);
			if(isFollow && target == getFollowTarget() && offset == _offset)
				return true;

			getAI().clearNextAction();

			if(isMovementDisabled() || target == null || isInVehicle() || isSwimming())
			{
				stopMove();
				return false;
			}

			if(Math.abs(getZ() - target.getZ()) > 1000 && !isFlying())
			{
				stopMove();
				sendPacket(Msg.CANNOT_SEE_TARGET);
				return false;
			}

			if(_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}

			//TODO сравнить с ним и без 
			//broadcastPacket(new StopMove(this));

			isFollow = true;
			setFollowTarget(target);
			_forestalling = forestalling;

			if(buildPathTo(target.getX(), target.getY(), target.getZ(), offset, true, !target.isDoor()))
				movingDestTempPos.set(target.getX(), target.getY(), target.getZ());
			else
			{
				isFollow = false;
				return false;
			}

			_offset = offset;
			moveNext(true);
			return true;
		}
	}

	public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
	}

	public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding)
	{
		synchronized (_targetRecorder)
		{
			offset = Math.max(offset, 0);
			Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
			if(isMoving && !isFollow && movingDestTempPos.equals(dst_geoloc))
			{
				sendActionFailed();
				return true;
			}

			getAI().clearNextAction();

			if(isMovementDisabled())
			{
				getAI().setNextAction(nextAction.MOVE, new Location(x_dest, y_dest, z_dest), offset, pathfinding, false);
				sendActionFailed();
				return false;
			}

			isFollow = false;

			if(_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}

			//TODO сравнить с ним и без 
			//broadcastPacket(new StopMove(this));

			if(isPlayer())
				getAI().changeIntention(AI_INTENTION_ACTIVE, null, null);

			if(buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding, false))
				movingDestTempPos.set(dst_geoloc);
			else
			{
				isMoving = false;
				sendActionFailed();
				return false;
			}
		}

		moveNext(true);

		return true;
	}

	/**
	 * должно вызыватся только из synchronized(_targetRecorder)
	 * @param firstMove
	 */
	public void moveNext(boolean firstMove)
	{
		_previousSpeed = getMoveSpeed();
		if(_previousSpeed <= 0)
		{
			stopMove();
			return;
		}

		if(!firstMove)
		{
			Location dest = destination;
			if(dest != null)
				setLoc(dest, true);
		}

		double distance;

		synchronized (_targetRecorder)
		{
			if(_targetRecorder.isEmpty())
			{
				isMoving = false;
				destination = null;
				if(isFollow)
				{
					isFollow = false;
					ThreadPoolManager.getInstance().executeAi(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED_TARGET, null, null), isPlayable());
				}
				else
					ThreadPoolManager.getInstance().executeAi(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED, null, null), isPlayable());

				validateLocation(isPlayer() ? 2 : 1);
				return;
			}

			moveList = _targetRecorder.remove(0);
			Location begin = moveList.get(0).clone().geo2world();
			Location end = moveList.get(moveList.size() - 1).clone().geo2world();
			destination = end;
			distance = begin.distance3D(end);

			isMoving = true;
		}

		broadcastMove();
		setHeading(calcHeading(destination));
		_startMoveTime = _followTimestamp = System.currentTimeMillis();
		_moveTask = ThreadPoolManager.getInstance().scheduleMove(_moveTaskRunnable.setDist(distance), getMoveTickInterval());
	}

	public int getMoveTickInterval()
	{
		return (int) ((isPlayer() ? 16000 : 32000) / getMoveSpeed());
	}

	private void broadcastMove()
	{
		if(isAirShip())
			broadcastPacket(new ExMoveToLocationAirShip((L2AirShip) this, getLoc(), getDestination()));
		else if(isShip())
			broadcastPacket(new VehicleDeparture((L2Ship) this));
		else
		{
			validateLocation(isPlayer() ? 2 : 1);
			broadcastPacket(new CharMoveToLocation(this));
		}
	}

	/**
	 * Останавливает движение и рассылает ValidateLocation
	 */
	public void stopMove()
	{
		stopMove(true);
	}

	/**
	 * Останавливает движение
	 * @param validate - рассылать ли ValidateLocation
	 */
	public void stopMove(boolean validate)
	{
		if(isMoving)
		{
			synchronized (_targetRecorder)
			{
				isMoving = false;
				destination = null;
				if(_moveTask != null)
				{
					_moveTask.cancel(false);
					_moveTask = null;
				}
				_targetRecorder.clear();
			}

			broadcastPacket(new StopMove(this));
			if(validate)
				validateLocation(1);
		}

		isFollow = false;
	}

	protected boolean needStatusUpdate()
	{
		if(Config.FORCE_STATUSUPDATE)
			return true;

		if(!isNpc())
			return true;

		double _intervalHpUpdate = getMaxHp() / 352;

		if(_lastHpUpdate == -99999999)
		{
			_lastHpUpdate = -9999999;
			return true;
		}

		if(getCurrentHp() <= 0 || getMaxHp() < 352)
			return true;

		if(_lastHpUpdate + _intervalHpUpdate < getCurrentHp() && getCurrentHp() > _lastHpUpdate)
		{
			_lastHpUpdate = getCurrentHp();
			return true;
		}

		if(_lastHpUpdate - _intervalHpUpdate > getCurrentHp() && getCurrentHp() < _lastHpUpdate)
		{
			_lastHpUpdate = getCurrentHp();
			return true;
		}
		return false;
	}

	public void onDecay()
	{
		decayMe();
		fireMethodInvoked(MethodCollection.onDecay, null);
	}

	@Override
	public void onForcedAttack(L2Player player, boolean shift)
	{
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

		if(!isAttackable(player) || player.isConfused() || player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}

		player.getAI().Attack(this, true, shift);
	}

	public void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}

		if(target.isDead() || !isInRange(target, 2000))
		{
			sendActionFailed();
			return;
		}

		if(isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle())
		{
			L2Player player = getPlayer();
			if(player != null)
			{
				player.sendPacket(Msg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}

		fireMethodInvoked(MethodCollection.OnAttacked, new Object[] { this, target, damage, crit, miss, soulshot, shld,
				unchargeSS });

		// if hitted by a cursed weapon, Cp is reduced to 0, if a cursed weapon is hitted by a Hero, Cp is reduced to 0
		if(!miss && target.isPlayer() && (isCursedWeaponEquipped() || getActiveWeaponInstance() != null && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped()))
			target.setCurrentCp(0);

		if(target.isStunned() && Formulas.calcStunBreak(crit))
		{
			target.getEffectList().stopEffects(EffectType.Stun);
			target.getEffectList().stopEffects(EffectType.Turner); // stun from bluff
		}

		if(isPlayer())
		{
			if(crit)
				sendPacket(new SystemMessage(SystemMessage.C1_HAD_A_CRITICAL_HIT).addName(this));
			if(miss)
				sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(this));
			else if(!target.isInvul())
				sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(target).addNumber(damage));
		}
		else if(this instanceof L2Summon)
			((L2Summon) this).displayHitMessage(target, damage, crit, miss);

		if(target.isPlayer())
		{
			L2Player enemy = (L2Player) target;

			if(shld && damage > 1)
				enemy.sendPacket(Msg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			else if(shld && damage == 1)
				enemy.sendPacket(Msg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
		}

		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		if(!miss && damage > 0)
		{
			target.reduceCurrentHp(damage, this, null, true, true, false, true);
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, damage);
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 0);

			// Скиллы, кастуемые при физ атаке
			if(!target.isDead())
			{
				if(crit)
					useActionSkill(null, this, target, TriggerActionType.CRIT);
				useActionSkill(null, this, target, TriggerActionType.ATTACK);
				useActionSkill(null, target, this, TriggerActionType.UNDER_ATTACK);

				// Проверка на мираж
				if(getTarget() != null && isPlayer())
					if(Rnd.chance(target.calcStat(Stats.CANCEL_TARGET, 0, this, null)))
						setTarget(null);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if(Formulas.calcCastBreak(target, crit))
					target.abortCast(false);
			}

			if(soulshot && unchargeSS)
				unChargeShots(false);
		}

		if(miss)
			useActionSkill(null, target, this, TriggerActionType.UNDER_MISSED_ATTACK);

		startAttackStanceTask();

		if(checkPvP(target, null))
			startPvPFlag(target);
	}

	private void useActionSkill(L2Skill exclude, L2Character actor, L2Character target, TriggerActionType type)
	{
		if(actor.getTriggerableSkills() == null)
			return;
		ConcurrentLinkedQueue<L2Skill> triggerableSkills = actor.getTriggerableSkills().get(type);
		if(triggerableSkills != null)
			for(L2Skill skill : triggerableSkills)
				if(skill != exclude && Rnd.chance(skill.getChanceForAction(type)))
					useActionSkill(skill, actor, target);
	}

	private static void useActionSkill(L2Skill skill, L2Character actor, L2Character target)
	{
		L2Character aimingTarget = skill.getAimingTarget(actor, target);
		if(skill.checkCondition(actor, aimingTarget, false, false, true))
		{
			GArray<L2Character> targets = skill.getTargets(actor, aimingTarget, false);
			L2Skill.broadcastUseAnimation(skill, actor, targets);
			Formulas.calcSkillMastery(skill, actor);
			actor.callSkill(skill, targets, false);
		}
	}

	public void onMagicUseTimer(L2Character aimingTarget, L2Skill skill, boolean forceUse)
	{
		if(skill == null)
		{
			onCastEndTime();
			sendPacket(Msg.ActionFail);
			return;
		}

		_castInterruptTime = 0;

		if(skill.isUsingWhileCasting())
		{
			aimingTarget.getEffectList().stopEffect(skill.getId());
			onCastEndTime();
			return;
		}

		if(!skill.isOffensive() && getAggressionTarget() != null)
			forceUse = true;

		if(!skill.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			onCastEndTime();
			return;
		}

		if(skill.getCastRange() < 32767 && skill.getSkillType() != SkillType.TAKECASTLE && skill.getSkillType() != SkillType.TAKEFORTRESS && !GeoEngine.canSeeTarget(this, aimingTarget, isFlying()))
		{
			sendPacket(Msg.CANNOT_SEE_TARGET);
			broadcastPacket(new MagicSkillCanceled(_objectId));
			onCastEndTime();
			return;
		}

		int level = getSkillDisplayLevel(skill.getId());
		if(level < 1)
			level = 1;

		GArray<L2Character> targets = skill.getTargets(this, aimingTarget, forceUse);

		int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
			setCurrentHp(Math.max(0, _currentHp - hpConsume), false);

		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			if(skill.isMusic())
			{
				double inc = mpConsume2 / 2;
				double add = 0;
				for(L2Effect e : getEffectList().getAllEffects())
					if(e.getSkill().getId() != skill.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30000)
						add += inc;
				mpConsume2 += add;
				mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			else if(skill.isMagic())
				mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);

			if(_currentMp < mpConsume2 && isPlayable())
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				onCastEndTime();
				return;
			}
			reduceCurrentMp(mpConsume2, null);
		}

		callSkill(skill, targets, true);

		if(skill.getNumCharges() > 0)
			setIncreasedForce(getIncreasedForce() - skill.getNumCharges());

		if(skill.isSoulBoost())
			setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
		else if(skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);

		Location flyLoc;
		switch(skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
				for(L2Character target : targets)
				{
					target.setHeading(this, false);
					flyLoc = getFlyLocation(null, skill);
					target.setLoc(flyLoc);
					broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType()));
				}
				break;
			case DUMMY:
			case CHARGE:
				flyLoc = _flyLoc;
				_flyLoc = null;
				if(flyLoc != null)
				{
					setLoc(flyLoc);
					validateLocation(1);
				}
				break;
		}

		if(isPlayer() && getTarget() != null && skill.isOffensive())
			for(L2Character target : targets)
				if(Rnd.chance(target.calcStat(Stats.CANCEL_TARGET, 0, aimingTarget, skill)))
				{
					clearCastVars();
					getAI().notifyEvent(EVT_FORGET_OBJECT, target);
					return;
				}

		if(_scheduledCastCount > 0)
		{
			_scheduledCastCount--;
			_skillLaunchedTask = ThreadPoolManager.getInstance().scheduleAi(new MagicLaunchedTask(this, forceUse), _scheduledCastInterval, isPlayable());
			_skillTask = ThreadPoolManager.getInstance().scheduleAi(new MagicUseTask(this, forceUse), _scheduledCastInterval, isPlayable());
			return;
		}

		int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		if(skillCoolTime > 0)
			ThreadPoolManager.getInstance().scheduleAi(new CastEndTimeTask(this), skillCoolTime, isPlayable());
		else
			onCastEndTime();
	}

	public void onCastEndTime()
	{
		clearCastVars();
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, null, null);
	}

	public void clearCastVars()
	{
		_castingSkill = null;
		_skillTask = null;
		_skillLaunchedTask = null;
		_flyLoc = null;
	}

	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		fireMethodInvoked(MethodCollection.ReduceCurrentHp, new Object[] { i, attacker, skill, awake, standUp, directHp, });

		if(attacker == null || isDead() || attacker.isDead())
			return;

		if(isInvul() && attacker != this) // Если я атакую, то меня...
		{
			attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			if(Rnd.get(100)>50)
			attacker.teleToLocation(10468, -24569, -3650, 0);
			else
			attacker.teleToLocation(11656, -23928, -3668, 0);
			return;
		}

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.getKarma() < 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.Siege))
				return;
			// чар с блессингом не может нанести урон ПК
			if(getKarma() < 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.Siege))
				return;
		}

		if(awake && isSleeping())
			getEffectList().stopEffects(EffectType.Sleep);

		if(isMeditated() && attacker != this)
		{
			L2Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
			if(effect != null)
			{
				GArray<L2Effect> effects = getEffectList().getEffectsBySkill(effect.getSkill());
				if(effects != null)
					for(L2Effect ef : effects)
						if(ef.getEffectType() != EffectType.Debuff)
							ef.exit();
			}
		}

		if(standUp && isPlayer())
		{
			standUp();
			if(isFakeDeath())
			{
				L2Effect fakeDeath = getEffectList().getEffectByType(EffectType.FakeDeath);
				if(fakeDeath == null)
					stopFakeDeath();
				else if(fakeDeath.getTime() > 2000)
					getEffectList().stopEffects(EffectType.FakeDeath);
			}
		}

		if(attacker != this)
		{
			startAttackStanceTask();
			if(isInvisible() && getEffectList().getEffectByType(EffectType.Invisible) != null)
				getEffectList().stopEffects(EffectType.Invisible);
		}

		if(canReflect && attacker.absorbAndReflect(this, skill, i))
			return;

		if(attacker.isPlayable())
		{
			L2Playable pAttacker = (L2Playable) attacker;

			// Flag the attacker if it's a L2Player outside a PvP area
			if(!isDead() && pAttacker.checkPvP(this, null))
				pAttacker.startPvPFlag(this);

			if(isMonster() && skill != null && skill.isOverhit())
			{
				// Calculate the over-hit damage
				// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
				double overhitDmg = (_currentHp - i) * -1;
				if(overhitDmg <= 0)
				{
					setOverhitDamage(0);
					setOverhitAttacker(null);
				}
				else
				{
					setOverhitDamage(overhitDmg);
					setOverhitAttacker(attacker);
				}
			}

			double ii;
			if(!directHp && _currentCp > 0)
			{
				i = _currentCp - i;
				ii = i;

				if(ii < 0)
					ii *= -1;

				if(i < 0)
					i = 0;

				setCurrentCp(i);
			}
			else
				ii = i;

			if(_currentCp == 0 || directHp)
			{
				ii = _currentHp - ii;

				if(ii < 0)
					ii = 0;

				if(isNpc())
					pAttacker.addDamage((L2NpcInstance) this, (int) (_currentHp - ii));

				if(!onDieTrigger && ii < 0.5 && _skillsOnAction != null)
				{
					onDieTrigger = true;
					ConcurrentLinkedQueue<L2Skill> SkillsOnDie = _skillsOnAction.get(TriggerActionType.DIE);
					if(SkillsOnDie != null)
						for(L2Skill sk : SkillsOnDie)
							if(Rnd.chance(sk.getChanceForAction(TriggerActionType.DIE)))
								useActionSkill(sk, this, attacker);
					onDieTrigger = false;
				}

				setCurrentHp(ii, false);
			}
		}
		else
		{
			if(_currentHp - i < 0.5)
				useActionSkill(null, this, this, TriggerActionType.DIE);

			setCurrentHp(Math.max(_currentHp - i, 0), false);
		}

		if(isDead())
			doDie(attacker);
	}

	private boolean onDieTrigger = false;

	public void reduceCurrentMp(double i, L2Character attacker)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
				getEffectList().stopEffects(EffectType.Sleep);
			if(isMeditated())
			{
				L2Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
				if(effect != null)
				{
					GArray<L2Effect> effects = getEffectList().getEffectsBySkill(effect.getSkill());
					if(effects != null)
						for(L2Effect ef : effects)
							if(ef.getEffectType() != EffectType.Debuff)
								ef.exit();
				}
			}
		}

		if(isInvul() && attacker != null && attacker != this)
		{
			attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.getKarma() < 0 && getEffectList().getEffectsBySkillId(5182) != null && !isInZone(ZoneType.Siege))
				return;
			// чар с блессингом не может нанести урон ПК
			if(getKarma() < 0 && attacker.getEffectList().getEffectsBySkillId(5182) != null && !attacker.isInZone(ZoneType.Siege))
				return;
		}

		i = _currentMp - i;

		if(i < 0)
			i = 0;

		setCurrentMp(i);

		if(attacker != null && attacker != this)
			startAttackStanceTask();
	}

	public double relativeSpeed(L2Object target)
	{
		return getMoveSpeed() - target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
	}

	public void removeAllSkills()
	{
		for(L2Skill s : getAllSkills())
			removeSkill(s);
	}

	public void removeBlockStats(GArray<Stats> stats)
	{
		if(_blockedStats != null)
		{
			_blockedStats.removeAll(stats);
			if(_blockedStats.isEmpty())
				_blockedStats = null;
		}
	}

	public L2Skill removeSkill(L2Skill skill)
	{
		if(skill == null)
			return null;
		return removeSkillById(skill.getId());
	}

	public L2Skill removeSkillById(Integer id)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(id);

		removeTriggerableSkill(id);

		// Remove all its Func objects from the L2Character calculator set
		if(oldSkill != null)
		{
			removeStatsOwner(oldSkill);
			if(ConfigSystem.getBoolean("AltDeleteSABuffs") && (oldSkill.isItemSkill() || oldSkill.isHandler()))
			{
				// Завершаем все эффекты, принадлежащие старому скиллу
				GArray<L2Effect> effects = getEffectList().getEffectsBySkill(oldSkill);
				if(effects != null)
					for(L2Effect effect : effects)
						effect.exit();
				// И с петов тоже
				L2Summon pet = getPet();
				if(pet != null)
				{
					effects = pet.getEffectList().getEffectsBySkill(oldSkill);
					if(effects != null)
						for(L2Effect effect : effects)
							effect.exit();
				}
			}
		}

		return oldSkill;
	}

	public ConcurrentHashMap<TriggerActionType, ConcurrentLinkedQueue<L2Skill>> getTriggerableSkills()
	{
		return _skillsOnAction;
	}

	public void addTriggerableSkill(L2Skill newSkill)
	{
		for(TriggerActionType e : newSkill.getTriggerActions().keySet())
		{
			if(_skillsOnAction == null)
				_skillsOnAction = new ConcurrentHashMap<TriggerActionType, ConcurrentLinkedQueue<L2Skill>>();
			ConcurrentLinkedQueue<L2Skill> hs = _skillsOnAction.get(e);
			if(hs == null)
			{
				hs = new ConcurrentLinkedQueue<L2Skill>();
				_skillsOnAction.put(e, hs);
			}
			hs.add(newSkill);

			if(e == TriggerActionType.ADD)
				if(Rnd.chance(newSkill.getChanceForAction(TriggerActionType.ADD)))
					useActionSkill(newSkill, this, this);
		}
	}

	public void removeTriggerableSkill(int id)
	{
		if(_skillsOnAction != null)
			for(ConcurrentLinkedQueue<L2Skill> s : _skillsOnAction.values())
				for(L2Skill sk : s)
					if(sk != null && sk.getId() == id)
						s.remove(sk);
	}

	public final synchronized void removeStatFunc(Func f)
	{
		if(f == null)
			return;

		int stat = f._stat.ordinal();
		if(_calculators.length > stat && _calculators[stat] != null)
			_calculators[stat].removeFunc(f);
	}

	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			removeStatFunc(f);
	}

	public final void removeStatsOwner(Object owner)
	{
		for(int i = 0; i < _calculators.length; i++)
			if(_calculators[i] != null)
				_calculators[i].removeOwner(owner);
	}

	public void sendActionFailed()
	{
		sendPacket(Msg.ActionFail);
	}

	@Override
	public boolean hasAI()
	{
		return _ai != null;
	}

	@Override
	public L2CharacterAI getAI()
	{
		if(_ai == null)
			_ai = new L2CharacterAI(this);
		return _ai;
	}

	public L2CharacterAI setAI(L2CharacterAI new_ai)
	{
		if(new_ai == null)
			return _ai = null;
		if(_ai != null)
			_ai.stopAITask();
		_ai = new_ai;
		return _ai;
	}

	public final void setCurrentHp(double newHp, boolean canRessurect)
	{
		newHp = Math.min(getMaxHp(), Math.max(0, newHp));

		if(_currentHp == newHp)
			return;

		if(newHp >= 0.5 && isDead() && !canRessurect)
			return;

		double hpStart = _currentHp;

		dieLock.lock();

		_currentHp = newHp;
		if(!isDead())
		{
			_killedAlready = false;
			_killedAlreadyPlayer = false;
			_killedAlreadyPet = false;
		}

		dieLock.unlock();

		startRegeneration();

		firePropertyChanged(PropertyCollection.HitPoints, hpStart, _currentHp);

		checkHpMessages(hpStart, newHp);
		broadcastStatusUpdate();
	}

	public final void setCurrentMp(double newMp)
	{
		newMp = Math.min(getMaxMp(), Math.max(0, newMp));

		if(_currentMp == newMp)
			return;

		_currentMp = newMp;

		startRegeneration();

		broadcastStatusUpdate();
	}

	public final void setCurrentCp(double newCp)
	{
		if(!isPlayer())
			return;

		newCp = Math.min(getMaxCp(), Math.max(0, newCp));

		if(_currentCp == newCp)
			return;

		_currentCp = newCp;

		startRegeneration();
		broadcastStatusUpdate();
	}

	public void setCurrentHpMp(double newHp, double newMp, boolean canRessurect)
	{
		newHp = Math.min(getMaxHp(), Math.max(0, newHp));
		newMp = Math.min(getMaxMp(), Math.max(0, newMp));

		if(_currentHp == newHp && _currentMp == newMp)
			return;

		if(newHp >= 0.5 && isDead() && !canRessurect)
			return;

		double hpStart = _currentHp;

		dieLock.lock();

		_currentHp = newHp;
		_currentMp = newMp;

		if(!isDead())
			_killedAlready = false;

		dieLock.unlock();

		startRegeneration();
		firePropertyChanged(PropertyCollection.HitPoints, hpStart, _currentHp);
		checkHpMessages(hpStart, newHp);
		broadcastStatusUpdate();
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHpMp(newHp, newMp, false);
	}

	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}

	@Override
	public final int getHeading()
	{
		if(isAttackingNow() || isCastingNow())
		{
			L2CharacterAI ai = getAI();
			if(ai != null)
			{
				L2Character target = ai.getAttackTarget();
				if(target != null)
					setHeading(target, true);
			}
		}
		return _heading;
	}

	@Override
	public void setHeading(int heading)
	{
		if(heading < 0)
			heading = heading + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(heading > 0xFFFF)
			heading &= 0xFFFF;
		_heading = heading;
	}

	public final void setHeading(L2Character target, boolean toChar)
	{
		if(target == null || target == this)
			return;
		setHeading(new Location(target.getX(), target.getY(), target.getZ()), toChar); // не менять на getLoc() иначе будет цикл из за getHeading() внутри getLoc()
	}

	public final void setHeading(Location target, boolean toChar)
	{
		setHeading((int) (Math.atan2(getY() - target.y, getX() - target.x) * HEADINGS_IN_PI) + (toChar ? 32768 : 0));
	}

	public final void setIsBlessedByNoblesse(boolean value)
	{
		if(value)
			_isBlessedByNoblesse++;
		else
			_isBlessedByNoblesse--;
	}

	public final void setIsSalvation(boolean value)
	{
		if(value)
			_isSalvation++;
		else
			_isSalvation--;
	}

	public final void setBuffImmunity(boolean value)
	{
		if(value)
			_buffImmunity++;
		else
			_buffImmunity--;
	}

	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}

	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public final void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}

	public final void setName(String name)
	{
		_name = name;
	}

	public L2Character getCastingTarget()
	{
		return L2ObjectsStorage.getAsCharacter(castingTargetStoreId);
	}

	public void setCastingTarget(L2Character target)
	{
		castingTargetStoreId = target == null ? 0 : target.getStoredId();
	}

	public final void setRiding(boolean mode)
	{
		_riding = mode;
	}

	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	public void setSkillMastery(Integer skill, byte mastery)
	{
		if(_skillMastery == null)
			_skillMastery = new TIntByteHashMap();
		_skillMastery.put(skill, mastery);
	}

	private L2Character _aggressionTarget = null;

	public void setAggressionTarget(L2Character target)
	{
		_aggressionTarget = target;
	}

	public L2Character getAggressionTarget()
	{
		return _aggressionTarget;
	}

	public void setTarget(L2Object object)
	{
		if(object != null && !object.isVisible())
			object = null;
		if(object == null)
		{
			if(isAttackingNow() && getAI().getAttackTarget() == getTarget())
				abortAttack(false, true);
			if(isCastingNow() && getAI().getAttackTarget() == getTarget())
				abortCast(false);
		}
		targetStoreId = object == null ? 0 : object.getStoredId();
	}

	protected void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public void setWalking()
	{
		if(_running)
		{
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	public void startAbnormalEffect(AbnormalEffect ae)
	{
		if(ae == AbnormalEffect.NULL)
		{
			_abnormalEffects = AbnormalEffect.NULL.getMask();
			_abnormalEffects2 = AbnormalEffect.NULL.getMask();
		}
		else if(ae.isSpecial())
			_abnormalEffects2 |= ae.getMask();
		else
			_abnormalEffects |= ae.getMask();
		updateAbnormalEffect();
	}

	@Override
	public void startAttackStanceTask()
	{
		if(System.currentTimeMillis() < _stanceInited + 10000)
			return;

		_stanceInited = System.currentTimeMillis();

		// Бесконечной рекурсии не будет, потому что выше проверка на _stanceInited
		if(this instanceof L2Summon && getPlayer() != null)
			getPlayer().startAttackStanceTask();
		else if(isPlayer() && getPet() != null)
			getPet().startAttackStanceTask();

		if(_stanceTask != null)
			_stanceTask.cancel(false);
		else
			broadcastPacket(new AutoAttackStart(getObjectId()));

		_stanceTask = ThreadPoolManager.getInstance().scheduleAi(new CancelAttackStanceTask(this), 15000, isPlayable());
	}

	public void stopAttackStanceTask()
	{
		broadcastPacket(new AutoAttackStop(getObjectId()));
		_stanceTask.cancel(false);
		_stanceTask = null;
	}

	public void startRegeneration()
	{
		if(!isDead() && (_currentHp < getMaxHp() || _currentMp < getMaxMp() || _currentCp < getMaxCp()))
		{
			regenLock.lock();
			try
			{
				long tick = RegenTaskManager.getInstance().getTick();
				if(_regenTick >= tick)
					return;
				_regenTick = tick;
			}
			finally
			{
				regenLock.unlock();
			}
			RegenTaskManager.getInstance().addRegenTask(this);
		}
	}

	public long _regenTick;

	public void doRegen()
	{
		if(isDead() || isHealBlocked(false))
			return;

		try
		{
			double addHp = 0;
			double addMp = 0;

			int maxHp = getMaxHp();
			int maxMp = getMaxMp();

			if(_currentHp < maxHp)
				addHp += Formulas.calcHpRegen(this);

			if(_currentMp < maxMp)
				addMp += Formulas.calcMpRegen(this);

			// Added regen bonus when character is sitting
			if(isPlayer() && Config.REGEN_SIT_WAIT)
			{
				L2Player pl = (L2Player) this;
				if(pl.isSitting())
				{
					pl.updateWaitSitTime();
					if(pl.getWaitSitTime() > 5)
					{
						addHp += pl.getWaitSitTime();
						addMp += pl.getWaitSitTime();
					}
				}
			}
			else if(isRaid())
			{
				addHp *= Config.RATE_RAID_REGEN;
				addMp *= Config.RATE_RAID_REGEN;
			}

			double hpStart = _currentHp;

			_currentHp += Math.max(0, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * maxHp / 100. - _currentHp));
			_currentMp += Math.max(0, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * maxMp / 100. - _currentMp));

			_currentHp = Math.min(maxHp, _currentHp);
			_currentMp = Math.min(maxMp, _currentMp);

			if(isPlayer())
			{
				int maxCp = getMaxCp();
				_currentCp += Math.max(0, Math.min(Formulas.calcCpRegen(L2Character.this), calcStat(Stats.CP_LIMIT, null, null) * maxCp / 100. - _currentCp));
				_currentCp = Math.min(maxCp, _currentCp);
			}

			firePropertyChanged(PropertyCollection.HitPoints, hpStart, _currentHp);
			checkHpMessages(hpStart, _currentHp);

			broadcastStatusUpdate();
			startRegeneration();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public void stopAbnormalEffect(AbnormalEffect ae)
	{
		if(ae.isSpecial())
			_abnormalEffects2 &= ~ae.getMask();
		else
			_abnormalEffects &= ~ae.getMask();
		updateAbnormalEffect();
	}

	public void block()
	{
		_blocked = true;
	}

	public void unblock()
	{
		_blocked = false;
	}

	public void startConfused()
	{
		if(!_confused)
		{
			_confused = true;
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopConfused()
	{
		if(_confused)
		{
			_confused = false;
			updateAbnormalEffect();

			abortAttack(true, true);
			abortCast(true);
			stopMove();
			getAI().setAttackTarget(null);
		}
	}

	public void startFakeDeath()
	{
		if(!_fakeDeath)
		{
			if(isPlayer())
				((L2Player) this).clearHateList(true);
			_fakeDeath = true;
			getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
			updateAbnormalEffect();
		}
	}

	public void stopFakeDeath()
	{
		if(_fakeDeath)
		{
			_fakeDeath = false;
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH));
			updateAbnormalEffect();
		}
	}

	public void breakFakeDeath()
	{
		getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
		stopFakeDeath();
	}

	public void startFear()
	{
		if(!_afraid)
		{
			_afraid = true;
			abortAttack(true, true);
			abortCast(true);
			sendActionFailed();
			stopMove();
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopFear()
	{
		if(_afraid)
		{
			_afraid = false;
			updateAbnormalEffect();
		}
	}

	public void startMuted()
	{
		if(!_muted)
		{
			_muted = true;
			if(getCastingSkill() != null && getCastingSkill().isMagic())
				abortCast(true);
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopMuted()
	{
		if(_muted)
		{
			_muted = false;
			updateAbnormalEffect();
		}
	}

	public void startPMuted()
	{
		if(!_pmuted)
		{
			_pmuted = true;
			if(getCastingSkill() != null && !getCastingSkill().isMagic())
				abortCast(true);
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopPMuted()
	{
		if(_pmuted)
		{
			_pmuted = false;
			updateAbnormalEffect();
		}
	}

	public void startAMuted()
	{
		if(!_amuted)
		{
			_amuted = true;
			abortCast(true);
			abortAttack(true, true);
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopAMuted()
	{
		if(_amuted)
		{
			_amuted = false;
			updateAbnormalEffect();
		}
	}

	public void startRooted()
	{
		if(!_rooted)
		{
			_rooted = true;
			stopMove();
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopRooting()
	{
		if(_rooted)
		{
			_rooted = false;
			updateAbnormalEffect();
		}
	}

	public void startSleeping()
	{
		if(!_sleeping)
		{
			_sleeping = true;
			abortAttack(true, true);
			abortCast(true);
			sendActionFailed();
			stopMove();
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopSleeping()
	{
		if(_sleeping)
		{
			_sleeping = false;
			updateAbnormalEffect();
		}
	}

	public void startStunning()
	{
		if(!_stunned)
		{
			_stunned = true;
			abortAttack(true, true);
			abortCast(true);
			sendActionFailed();
			stopMove();
			startAttackStanceTask();
			updateAbnormalEffect();
		}
	}

	public void stopStunning()
	{
		if(_stunned)
		{
			_stunned = false;
			updateAbnormalEffect();
		}
	}

	public void setMeditated(boolean meditated)
	{
		_meditated = meditated;
	}

	public void setParalyzed(boolean paralyzed)
	{
		if(_paralyzed != paralyzed)
		{
			_paralyzed = paralyzed;
			if(paralyzed)
			{
				abortAttack(true, true);
				abortCast(true);
				sendActionFailed();
				stopMove();
			}
		}
	}

	public void setImobilised(boolean imobilised)
	{
		if(_imobilised != imobilised)
		{
			_imobilised = imobilised;
			if(imobilised)
				stopMove();
			updateAbnormalEffect();
		}
	}

	public void setHealBlocked(boolean value)
	{
		_healBlocked = value;
	}

	/**
	 * if True, the L2Player can't take more item
	 */
	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public boolean isConfused()
	{
		return _confused;
	}

	public boolean isFakeDeath()
	{
		return _fakeDeath;
	}

	public boolean isAfraid()
	{
		return _afraid;
	}

	public boolean isBlocked()
	{
		return _blocked;
	}

	public boolean isMuted(L2Skill skill)
	{
		if(skill == null || skill.isNotAffectedByMute())
			return false;
		return _muted && skill.isMagic() || _pmuted && !skill.isMagic();
	}

	public boolean isPMuted()
	{
		return _pmuted;
	}

	public boolean isMMuted()
	{
		return _muted;
	}

	public boolean isAMuted()
	{
		return _amuted;
	}

	public boolean isRooted()
	{
		return _rooted;
	}

	public boolean isSleeping()
	{
		return _sleeping;
	}

	public boolean isStunned()
	{
		return _stunned;
	}

	public boolean isMeditated()
	{
		return _meditated;
	}

	public boolean isParalyzed()
	{
		return _paralyzed;
	}

	public boolean isImobilised()
	{
		return _imobilised || getRunSpeed() < 1;
	}

	public boolean isHealBlocked(boolean checkInvul)
	{
		return _healBlocked || checkInvul && _isInvul;
	}

	public boolean isCastingNow()
	{
		return _skillTask != null;
	}

	public boolean isMovementDisabled()
	{
		return isSitting() || isStunned() || isRooted() || isSleeping() || isParalyzed() || isImobilised() || isAlikeDead() || isAttackingNow() || isCastingNow() || _overloaded || _fishing || isPlayer() && (isTeleporting() || ((L2Player) this).isLogoutStarted());
	}

	public boolean isActionsDisabled()
	{
		return isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isAlikeDead() || isPlayer() && (isTeleporting() || ((L2Player) this).isLogoutStarted());
	}

	public boolean isPotionsDisabled()
	{
		return isStunned() || isSleeping() || isParalyzed() || isAlikeDead() || isPlayer() && (isTeleporting() || ((L2Player) this).isLogoutStarted());
	}

	public boolean isToggleDisabled()
	{
		return isStunned() || isSleeping() || isParalyzed() || isPlayer() && (isTeleporting() || ((L2Player) this).isLogoutStarted());
	}

	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}

	public boolean isOutOfControl()
	{
		return isConfused() || isAfraid() || isBlocked() || isPlayer() && (isTeleporting() || ((L2Player) this).isLogoutStarted());
	}

	public void teleToLocation(Location loc)
	{
		teleToLocation(loc.x, loc.y, loc.z, getReflection().getId());
	}

	public void teleToLocation(Location loc, long ref)
	{
		teleToLocation(loc.x, loc.y, loc.z, ref);
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection().getId());
	}

	public void teleToLocation(int x, int y, int z, long ref)
	{
		if(isFakeDeath())
			breakFakeDeath();

		if(isTeleporting())
			return;

		abortCast(true);

		if(isPlayable())
			clearHateList(true);

		if(!isVehicle() && !isFlying() && !L2World.isWater(new Location(x, y, z)))
			z = GeoEngine.getHeight(x, y, z, getReflection().getGeoIndex());

		setTarget(null);

		if(isPlayer())
		{
			L2Player player = (L2Player) this;
			if(player.isLogoutStarted())
				return;

			setIsTeleporting(true);

			decayMe();
			setXYZInvisible(x, y, z);
			if(ref != getReflection().getId())
				setReflection(ref);

			// Нужно при телепорте с более высокой точки на более низкую, иначе наносится вред от "падения"
			setLastClientPosition(null);
			setLastServerPosition(null);

			player.sendPacket(new TeleportToLocation(player, x, y, z));
		}
		else
		{
			setXYZ(x, y, z);
			broadcastPacket(new TeleportToLocation(this, x, y, z));
		}
	}

	public void onTeleported()
	{
		L2Player activeChar = (L2Player) this;

		if(activeChar.isFakeDeath())
			activeChar.breakFakeDeath();

		if(activeChar.isInVehicle())
			activeChar.setXYZInvisible(activeChar.getVehicle().getLoc());

		// 15 секунд после телепорта на персонажа не агрятся мобы
		activeChar.setNonAggroTime(System.currentTimeMillis() + 15000);

		spawnMe();

		setLastClientPosition(getLoc());
		setLastServerPosition(getLoc());

		setIsTeleporting(false);

		if(_isPendingRevive)
			doRevive();

		if(activeChar.getTrainedBeast() != null)
			activeChar.getTrainedBeast().setXYZ(getX() + Rnd.get(-100, 100), getY() + Rnd.get(-100, 100), getZ());

		activeChar.checkWaterState();

		sendActionFailed();

		getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
		activeChar.sendUserInfo(true);
		if(activeChar.getPet() != null)
			activeChar.getPet().teleportToOwner();
	}

	public void teleToClosestTown()
	{
		teleToLocation(MapRegion.getTeleToClosestTown(this), 0);
	}

	public void teleToSecondClosestTown()
	{
		teleToLocation(MapRegion.getTeleToSecondClosestTown(this), 0);
	}

	public void teleToCastle()
	{
		teleToLocation(MapRegion.getTeleToCastle(this), 0);
	}

	public void teleToFortress()
	{
		teleToLocation(MapRegion.getTeleToFortress(this), 0);
	}

	public void teleToClanhall()
	{
		teleToLocation(MapRegion.getTeleToClanHall(this), 0);
	}

	public void teleToHeadquarter()
	{
		teleToLocation(MapRegion.getTeleToHeadquarter(this), 0);
	}

	public void sendMessage(CustomMessage message)
	{
		sendMessage(message.toString());
	}

	private long _nonAggroTime;

	public long getNonAggroTime()
	{
		return _nonAggroTime;
	}

	public void setNonAggroTime(long time)
	{
		_nonAggroTime = time;
	}

	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}

	@Override
	public float getColRadius()
	{
		return getTemplate().collisionRadius;
	}

	@Override
	public float getColHeight()
	{
		return getTemplate().collisionHeight;
	}

	public boolean canAttackCharacter(L2Character target)
	{
		return target.getPlayer() != null;
	}

	public class HateInfo
	{
		public L2NpcInstance npc;
		public int hate;
		public int damage;

		HateInfo(L2NpcInstance attacker)
		{
			npc = attacker;
		}
	}

	private ConcurrentHashMap<L2NpcInstance, HateInfo> _hateList = null;

	public void addDamage(L2NpcInstance npc, int damage)
	{
		addDamageHate(npc, damage, damage);

		// Добавляем хейта к хозяину саммона
		if(npc.hasAI() && npc.getAI() instanceof DefaultAI && (isSummon() || isPet()) && getPlayer() != null)
			getPlayer().addDamageHate(npc, damage, npc.getTemplate().getAIParams().getBool("searchingMaster", false) ? damage : 1);
	}

	public void addDamageHate(L2NpcInstance npc, int damage, int aggro)
	{
		if(npc == null)
			return;

		if(damage <= 0 && aggro <= 0)
			return;

		if(damage > 0 && aggro <= 0)
			aggro = damage;

		if(_hateList == null)
			_hateList = new ConcurrentHashMap<L2NpcInstance, HateInfo>();

		HateInfo ai = _hateList.get(npc);

		if(ai != null)
		{
			ai.damage += damage;
			ai.hate += aggro;
			ai.hate = Math.max(ai.hate, 0);
		}
		else if(aggro > 0)
		{
			ai = new HateInfo(npc);
			ai.damage = damage;
			ai.hate = aggro;
			_hateList.put(npc, ai);
		}
	}

	public ConcurrentHashMap<L2NpcInstance, HateInfo> getHateList()
	{
		if(_hateList == null)
			return new ConcurrentHashMap<L2NpcInstance, HateInfo>();
		return _hateList;
	}

	public void removeFromHatelist(L2NpcInstance npc, boolean onlyHate)
	{
		if(npc != null && _hateList != null)
			if(onlyHate)
			{
				HateInfo i = _hateList.get(npc);
				if(i != null)
					i.hate = 0;
			}
			else
				_hateList.remove(npc);
	}

	public void clearHateList(boolean onlyHate)
	{
		if(_hateList != null)
			if(onlyHate)
				for(HateInfo i : _hateList.values())
					i.hate = 0;
			else
				_hateList = null;
	}

	public EffectList getEffectList()
	{
		if(_effectList == null)
			_effectList = new EffectList(this);
		return _effectList;
	}

	public void setEffectList(EffectList el)
	{
		_effectList = el;
	}

	public boolean isMassUpdating()
	{
		return _massUpdating;
	}

	public void setMassUpdating(boolean updating)
	{
		_massUpdating = updating;
	}

	public Collection<L2TrapInstance> getTraps()
	{
		if(_traps == null)
			return null;
		Collection<L2TrapInstance> result = new GArray<L2TrapInstance>(getTrapsCount());
		L2TrapInstance trap;
		for(Integer trapId : _traps.keySet())
			if((trap = (L2TrapInstance) L2ObjectsStorage.get(_traps.get(trapId))) != null)
				result.add(trap);
			else
				_traps.remove(trapId);
		return result;
	}

	public int getTrapsCount()
	{
		return _traps == null ? 0 : _traps.size();
	}

	public void addTrap(L2TrapInstance trap)
	{
		if(_traps == null)
			_traps = new HashMap<Integer, Long>();
		_traps.put(trap.getObjectId(), trap.getStoredId());
	}

	public void removeTrap(L2TrapInstance trap)
	{
		HashMap<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		traps.remove(trap.getObjectId());
	}

	public void destroyFirstTrap()
	{
		HashMap<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		L2TrapInstance trap;
		for(Integer trapId : traps.keySet())
		{
			if((trap = (L2TrapInstance) L2ObjectsStorage.get(traps.get(trapId))) != null)
			{
				trap.destroy();
				return;
			}
			traps.remove(trapId);
			return;
		}
	}

	public void destroyAllTraps()
	{
		HashMap<Integer, Long> traps = _traps;
		if(traps == null || traps.isEmpty())
			return;
		GArray<L2TrapInstance> toRemove = new GArray<L2TrapInstance>();
		for(Integer trapId : traps.keySet())
			toRemove.add((L2TrapInstance) L2ObjectsStorage.get(traps.get(trapId)));
		for(L2TrapInstance t : toRemove)
			if(t != null)
				t.destroy();
	}

	public boolean paralizeOnAttack(L2Character attacker)
	{
		// Mystic Immunity Makes a target temporarily immune to raid curce
		if(attacker.getEffectList().getEffectsBySkillId(L2Skill.SKILL_MYSTIC_IMMUNITY) != null)
			return false;

		int max_attacker_level = 0xFFFF;

		L2MonsterInstance leader;
		if(isRaid() || (isMinion() && (leader = ((L2MinionInstance) this).getLeader()) != null && leader.isRaid()))
			max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
		else if(getAI() instanceof DefaultAI)
		{
			int max_level_diff = ((DefaultAI) getAI()).getInt("ParalizeOnAttack", -1000);
			if(max_level_diff != -1000)
				max_attacker_level = getLevel() + max_level_diff;
		}

		if(attacker.getLevel() > max_attacker_level)
		{
			if(max_attacker_level > 0)
				attacker.sendMessage(new CustomMessage("l2rt.gameserver.model.L2Character.ParalizeOnAttack", attacker).addCharName(this).addNumber(max_attacker_level));
			return true;
		}

		return false;
	}

	public Calculator[] getCalculators()
	{
		return _calculators;
	}

	@Override
	public void deleteMe()
	{
		setTarget(null);
		stopMove();
		super.deleteMe();
	}

	// ---------------------------- Not Implemented -------------------------------

	public void addExpAndSp(long addToExp, long addToSp)
	{}

	public void addExpAndSp(long addToExp, long addToSp, boolean applyBonus, boolean appyToPet)
	{}

	public void broadcastUserInfo(boolean force)
	{}

	public void checkHpMessages(double currentHp, double newHp)
	{}

	public boolean checkPvP(L2Character target, L2Skill skill)
	{
		return false;
	}

	public boolean consumeItem(int itemConsumeId, int itemCount)
	{
		return true;
	}

	public void doPickupItem(L2Object object)
	{}

	public boolean isFearImmune()
	{
		return false;
	}

	public boolean isLethalImmune()
	{
		if(ConfigSystem.getInt("LethalImmuneHp") > 0)
			return getMaxHp() >= ConfigSystem.getInt("LethalImmuneHp");
		else
			return false;
	}

	public boolean getChargedSoulShot()
	{
		return false;
	}

	public int getChargedSpiritShot()
	{
		return 0;
	}

	public Duel getDuel()
	{
		return null;
	}

	public int getIncreasedForce()
	{
		return 0;
	}

	public int getConsumedSouls()
	{
		return 0;
	}

	public int getKarma()
	{
		return 0;
	}

	public double getLevelMod()
	{
		return 1;
	}

	public int getNpcId()
	{
		return 0;
	}

	public L2Summon getPet()
	{
		return null;
	}

	public int getPvpFlag()
	{
		return 0;
	}

	public int getTeam()
	{
		return 0;
	}

	public boolean isSitting()
	{
		return false;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isUsingDualWeapon()
	{
		return false;
	}

	public boolean isParalyzeImmune()
	{
		return false;
	}

	public void reduceArrowCount()
	{}

	public void sendChanges()
	{}

	public void sendMessage(String message)
	{}

	public void sendPacket(L2GameServerPacket... mov)
	{}

	public void setIncreasedForce(int i)
	{}

	public void setConsumedSouls(int i, L2NpcInstance monster)
	{}

	public void sitDown()
	{}

	public void standUp()
	{}

	public void startPvPFlag(L2Character target)
	{}

	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}

	public void updateEffectIcons()
	{}

	public void updateStats()
	{}

	public void callMinionsToAssist(L2Character attacker)
	{}

	public void setOverhitAttacker(L2Character attacker)
	{}

	public void setOverhitDamage(double damage)
	{}

	public boolean hasMinions()
	{
		return false;
	}

	public boolean isCursedWeaponEquipped()
	{
		return false;
	}

	public boolean isHero()
	{
		return false;
	}

	public int getAccessLevel()
	{
		return 0;
	}

	public void spawnWayPoints(Vector<Location> recorder)
	{}

	public void setFollowStatus(boolean state, boolean changeIntention)
	{}

	public void setLastClientPosition(Location charPosition)
	{}

	public void setLastServerPosition(Location charPosition)
	{}

	public boolean hasRandomAnimation()
	{
		return true;
	}

	public boolean hasRandomWalk()
	{
		return true;
	}

	public int getClanCrestId()
	{
		Integer result = 0;
		if(isCrestEnable())
		{
			Town town = TownManager.getInstance().getClosestTown(this);
			if(town != null && town.getCastle() != null && town.getCastle().getOwner() != null)
				result = town.getCastle().getOwner().getCrestId();
		}
		return result;
	}

	public int getClanCrestLargeId()
	{
		Integer result = 0;
		if(isCrestEnable())
		{
			Town town = TownManager.getInstance().getClosestTown(this);
			if(town != null && town.getCastle() != null && town.getCastle().getOwner() != null)
				result = town.getCastle().getOwner().getCrestLargeId();
		}
		return result;
	}

	public int getAllyCrestId()
	{
		Integer result = 0;
		L2Alliance ally;
		if(isCrestEnable())
		{
			Town town = TownManager.getInstance().getClosestTown(this);
			if(town != null && town.getCastle() != null && town.getCastle().getOwner() != null)
			{
				ally = town.getCastle().getOwner().getAlliance();
				if(ally != null)
					result = town.getCastle().getOwner().getAlliance().getAllyCrestId();
			}
		}
		return result;
	}

	public void disableItem(L2Skill handler, long timeTotal, long timeLeft)
	{}

	public float getRateAdena()
	{
		return 1.0f;
	}

	public float getRateItems()
	{
		return 1.0f;
	}

	public double getRateExp()
	{
		return 1.;
	}

	public double getRateSp()
	{
		return 1.;
	}

	public float getRateSpoil()
	{
		return 1.0f;
	}

	@Override
	public void setXYZInvisible(int x, int y, int z)
	{
		stopMove();
		super.setXYZInvisible(x, y, z);
	}

	@Override
	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if(!MoveTask)
			stopMove();
		super.setXYZ(x, y, z, MoveTask);
	}

	public void validateLocation(int broadcast)
	{
		if(isVehicle() || isInVehicle()) // FIXME для кораблей что-то иное
			return;
		L2GameServerPacket sp = new ValidateLocation(this);
		if(broadcast == 0)
			sendPacket(sp);
		else if(broadcast == 1)
			broadcastPacket(sp);
		else
			broadcastPacketToOthers(sp);
	}

	// --------------------------- End Of Not Implemented ------------------------------

	// --------------------------------- Abstract --------------------------------------

	public abstract byte getLevel();

	public abstract void updateAbnormalEffect();

	public abstract L2ItemInstance getActiveWeaponInstance();

	public abstract L2Weapon getActiveWeaponItem();

	public abstract L2ItemInstance getSecondaryWeaponInstance();

	public abstract L2Weapon getSecondaryWeaponItem();

	// ----------------------------- End Of Abstract -----------------------------------
	
	public void setTransformationId(int transformationId)
	{
		if((transformationId == _transformationId )|| (_transformationId != 0 && transformationId != 0))
			return;
		_transformationId = transformationId;
	}
	
	public boolean isTransformed()  
	{  
		return _transformationId != 0;  
	} 
	
	public boolean isInFlyingTransform()
	{
		return _transformationId == 8 || _transformationId == 9 || _transformationId == 260;
	}

	/**
	 * Возвращает режим трансформации
	 * @return ID режима трансформации
	 */
	public int getTransformation()
	{
		return _transformationId;
	}

	/**
	 * Возвращает имя трансформации
	 * @return String
	 */
	public String getTransformationName()
	{
		return _transformationName;
	}

	/**
	 * Устанавливает имя трансформаии
	 * @param name имя трансформации
	 */
	public void setTransformationName(String name)
	{
		_transformationName = name;
	}

	/**
	 * Устанавливает шаблон трансформации, используется для определения коллизий
	 * @param template ID шаблона
	 */
	public void setTransformationTemplate(int template)
	{
		_transformationTemplate = template;
	}

	/**
	 * Возвращает шаблон трансформации, используется для определения коллизий
	 * @return NPC ID
	 */
	public int getTransformationTemplate()
	{
		return _transformationTemplate;
	}
}