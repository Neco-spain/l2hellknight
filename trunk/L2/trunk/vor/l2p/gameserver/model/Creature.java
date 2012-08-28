package l2p.gameserver.model;

import gnu.trove.TIntHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.commons.listener.Listener;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.commons.util.concurrent.atomic.AtomicState;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.PlayableAI.nextAction;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.geodata.GeoMove;
import l2p.gameserver.instancemanager.DimensionalRiftManager;
import l2p.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.actor.listener.CharListenerList;
import l2p.gameserver.model.actor.listener.PlayerListenerList;
import l2p.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2p.gameserver.model.base.InvisibleType;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.DimensionalRift;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.MinionInstance;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestEventType;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.model.reference.L2Reference;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.Attack;
import l2p.gameserver.serverpackets.AutoAttackStart;
import l2p.gameserver.serverpackets.AutoAttackStop;
import l2p.gameserver.serverpackets.ChangeMoveType;
import l2p.gameserver.serverpackets.CharMoveToLocation;
import l2p.gameserver.serverpackets.FlyToLocation;
import l2p.gameserver.serverpackets.FlyToLocation.FlyType;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MagicSkillCanceled;
import l2p.gameserver.serverpackets.MagicSkillLaunched;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.SetupGauge;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.serverpackets.StopMove;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.TeleportToLocation;
import l2p.gameserver.serverpackets.ValidateLocation;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.stats.Calculator;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.stats.Formulas.AttackInfo;
import l2p.gameserver.stats.StatFunctions;
import l2p.gameserver.stats.StatTemplate;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.stats.funcs.Func;
import l2p.gameserver.stats.triggers.TriggerInfo;
import l2p.gameserver.stats.triggers.TriggerType;
import l2p.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2p.gameserver.taskmanager.RegenTaskManager;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.PositionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Creature extends GameObject
{
  private static final Logger _log = LoggerFactory.getLogger(Creature.class);
  public static final double HEADINGS_IN_PI = 10430.378350470453D;
  public static final int INTERACTION_DISTANCE = 200;
  private Skill _castingSkill;
  private long _castInterruptTime;
  private long _animationEndTime;
  public int _scheduledCastCount;
  public int _scheduledCastInterval;
  public Future<?> _skillTask;
  public Future<?> _skillLaunchedTask;
  private Future<?> _stanceTask;
  private Runnable _stanceTaskRunnable;
  private long _stanceEndTime;
  public static final int CLIENT_BAR_SIZE = 352;
  private int _lastCpBarUpdate = -1;
  private int _lastHpBarUpdate = -1;
  private int _lastMpBarUpdate = -1;

  protected double _currentCp = 0.0D;
  protected double _currentHp = 1.0D;
  protected double _currentMp = 1.0D;
  protected boolean _isAttackAborted;
  protected long _attackEndTime;
  protected long _attackReuseEndTime;
  private int _poleAttackCount = 0;
  private static final double[] POLE_VAMPIRIC_MOD = { 1.0D, 0.9D, 0.0D, 7.0D, 0.2D, 0.01D };

  protected final Map<Integer, Skill> _skills = new ConcurrentSkipListMap();
  protected Map<TriggerType, Set<TriggerInfo>> _triggers;
  protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap();
  protected volatile EffectList _effectList;
  protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;
  private List<Stats> _blockedStats;
  private int _abnormalEffects;
  private int _abnormalEffects2;
  private int _abnormalEffects3;
  protected AtomicBoolean isDead = new AtomicBoolean();
  protected AtomicBoolean isTeleporting = new AtomicBoolean();
  private Map<Integer, Integer> _skillMastery;
  protected boolean _isInvul;
  private boolean _fakeDeath;
  private boolean _isBlessedByNoblesse;
  private boolean _isSalvation;
  private boolean _meditated;
  private boolean _lockedTarget;
  private boolean _blocked;
  private AtomicState _afraid = new AtomicState();
  private AtomicState _muted = new AtomicState();
  private AtomicState _pmuted = new AtomicState();
  private AtomicState _amuted = new AtomicState();
  private AtomicState _paralyzed = new AtomicState();
  private AtomicState _rooted = new AtomicState();
  private AtomicState _sleeping = new AtomicState();
  private AtomicState _stunned = new AtomicState();
  private AtomicState _immobilized = new AtomicState();
  private AtomicState _confused = new AtomicState();
  private AtomicState _frozen = new AtomicState();

  private AtomicState _healBlocked = new AtomicState();
  private AtomicState _damageBlocked = new AtomicState();
  private AtomicState _buffImmunity = new AtomicState();
  private AtomicState _debuffImmunity = new AtomicState();
  private AtomicState _effectImmunity = new AtomicState();

  private AtomicState _weaponEquipBlocked = new AtomicState();
  private boolean _flying;
  private boolean _running;
  public boolean isMoving;
  public boolean isFollow;
  private final Lock moveLock = new ReentrantLock();
  private Future<?> _moveTask;
  private MoveNextTask _moveTaskRunnable;
  private List<Location> moveList;
  private Location destination;
  private final Location movingDestTempPos = new Location();
  private int _offset;
  private boolean _forestalling;
  private volatile HardReference<? extends GameObject> target = HardReferences.emptyRef();
  private volatile HardReference<? extends Creature> castingTarget = HardReferences.emptyRef();
  private volatile HardReference<? extends Creature> followTarget = HardReferences.emptyRef();
  private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();

  private final List<List<Location>> _targetRecorder = new ArrayList();
  private long _followTimestamp;
  private long _startMoveTime;
  private int _previousSpeed = 0;
  private int _heading;
  private final Calculator[] _calculators;
  protected CharTemplate _template;
  protected CharTemplate _baseTemplate;
  protected volatile CharacterAI _ai;
  protected String _name;
  protected String _title;
  protected TeamType _team = TeamType.NONE;
  private boolean _isRegenerating;
  private final Lock regenLock = new ReentrantLock();
  private Future<?> _regenTask;
  private Runnable _regenTaskRunnable;
  private List<Zone> _zones = new LazyArrayList();

  private final ReadWriteLock zonesLock = new ReentrantReadWriteLock();
  private final Lock zonesRead = zonesLock.readLock();
  private final Lock zonesWrite = zonesLock.writeLock();
  protected volatile CharListenerList listeners;
  private List<Player> _statusListeners;
  private final Lock statusListenersLock = new ReentrantLock();
  protected Long _storedId;
  protected HardReference<? extends Creature> reference;
  private Location _flyLoc;
  private TIntHashSet _unActiveSkills = new TIntHashSet();

  public final Long getStoredId()
  {
    return _storedId;
  }

  public Creature(int objectId, CharTemplate template)
  {
    super(objectId);

    _template = template;
    _baseTemplate = template;

    _calculators = new Calculator[Stats.NUM_STATS];

    StatFunctions.addPredefinedFuncs(this);

    reference = new L2Reference(this);

    _storedId = Long.valueOf(GameObjectsStorage.put(this));
  }

  public HardReference<? extends Creature> getRef()
  {
    return reference;
  }

  public boolean isAttackAborted()
  {
    return _isAttackAborted;
  }

  public final void abortAttack(boolean force, boolean message)
  {
    if (isAttackingNow())
    {
      _attackEndTime = 0L;
      if (force) {
        _isAttackAborted = true;
      }
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

      if ((isPlayer()) && (message))
      {
        sendActionFailed();
        sendPacket(new SystemMessage(2268).addName(this));
      }
    }
  }

  public final void abortCast(boolean force, boolean message)
  {
    if ((isCastingNow()) && ((force) || (canAbortCast())))
    {
      Skill castingSkill = _castingSkill;
      Future skillTask = _skillTask;
      Future skillLaunchedTask = _skillLaunchedTask;

      finishFly();
      clearCastVars();

      if (skillTask != null) {
        skillTask.cancel(false);
      }
      if (skillLaunchedTask != null) {
        skillLaunchedTask.cancel(false);
      }
      if (castingSkill != null)
      {
        if (castingSkill.isUsingWhileCasting())
        {
          Creature target = getAI().getAttackTarget();
          if (target != null) {
            target.getEffectList().stopEffect(castingSkill.getId());
          }
        }
        removeSkillMastery(Integer.valueOf(castingSkill.getId()));
      }

      broadcastPacket(new L2GameServerPacket[] { new MagicSkillCanceled(getObjectId()) });

      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

      if ((isPlayer()) && (message))
        sendPacket(Msg.CASTING_HAS_BEEN_INTERRUPTED);
    }
  }

  public final boolean canAbortCast()
  {
    return _castInterruptTime > System.currentTimeMillis();
  }

  public boolean absorbAndReflect(Creature target, Skill skill, double damage)
  {
    if (target.isDead()) {
      return false;
    }
    boolean bow = (getActiveWeaponItem() != null) && ((getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW) || (getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.CROSSBOW));

    double value = 0.0D;

    if ((skill != null) && (skill.isMagic()))
      value = target.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0.0D, this, skill);
    else if ((skill != null) && (skill.getCastRange() <= 200))
      value = target.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0.0D, this, skill);
    else if ((skill == null) && (!bow)) {
      value = target.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0.0D, this, null);
    }

    if ((value > 0.0D) && (Rnd.chance(value)))
    {
      reduceCurrentHp(damage, target, null, true, true, false, false, false, false, true);
      return true;
    }

    if ((skill != null) && (skill.isMagic()))
      value = target.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0.0D, this, skill);
    else if ((skill != null) && (skill.getCastRange() <= 200))
      value = target.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0.0D, this, skill);
    else if ((skill == null) && (!bow)) {
      value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0D, this, null);
    }
    if (value > 0.0D)
    {
      if (target.getCurrentHp() + target.getCurrentCp() > damage) {
        reduceCurrentHp(value / 100.0D * damage, target, null, true, true, false, false, false, false, true);
      }
    }
    if ((skill != null) || (bow)) {
      return false;
    }

    damage = (int)(damage - target.getCurrentCp());

    if (damage <= 0.0D) {
      return false;
    }
    double poleMod = _poleAttackCount < POLE_VAMPIRIC_MOD.length ? POLE_VAMPIRIC_MOD[_poleAttackCount] : 0.0D;
    double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0D, target, null);

    if ((absorb > 0.0D) && (!target.isDamageBlocked()))
    {
      double limit = calcStat(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.0D;
      if (getCurrentHp() < limit) {
        setCurrentHp(Math.min(_currentHp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100.0D, limit), false);
      }
    }
    absorb = poleMod * calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0.0D, target, null);
    if ((absorb > 0.0D) && (!target.isDamageBlocked()))
    {
      double limit = calcStat(Stats.MP_LIMIT, null, null) * getMaxMp() / 100.0D;
      if (getCurrentMp() < limit) {
        setCurrentMp(Math.min(_currentMp + damage * absorb * Config.ALT_ABSORB_DAMAGE_MODIFIER / 100.0D, limit));
      }
    }
    return false;
  }

  public double absorbToEffector(Creature attacker, double damage)
  {
    double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.0D);
    if (transferToEffectorDam > 0.0D)
    {
      Effect effect = getEffectList().getEffectByType(EffectType.AbsorbDamageToEffector);
      if (effect == null) {
        return damage;
      }
      Creature effector = effect.getEffector();

      if ((effector == this) || (effector.isDead()) || (!isInRange(effector, 1200L))) {
        return damage;
      }
      Player thisPlayer = getPlayer();
      Player effectorPlayer = effector.getPlayer();
      if ((thisPlayer != null) && (effectorPlayer != null))
      {
        if ((thisPlayer != effectorPlayer) && ((!thisPlayer.isOnline()) || (!thisPlayer.isInParty()) || (thisPlayer.getParty() != effectorPlayer.getParty())))
          return damage;
      }
      else {
        return damage;
      }
      double transferDamage = damage * transferToEffectorDam * 0.01D;
      damage -= transferDamage;

      effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
    }
    return damage;
  }

  public double absorbToMp(Creature attacker, double damage)
  {
    double transferToMpDamPercent = calcStat(Stats.TRANSFER_TO_MP_DAMAGE_PERCENT, 0.0D);
    if (transferToMpDamPercent > 0.0D)
    {
      double transferDamage = damage * transferToMpDamPercent * 0.01D;

      double currentMp = getCurrentMp();
      if (currentMp > transferDamage)
      {
        setCurrentMp(getCurrentMp() - transferDamage);
        return 0.0D;
      }

      if (currentMp > 0.0D)
      {
        damage -= currentMp;
        setCurrentMp(0.0D);
        sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
      }
      getEffectList().stopEffects(EffectType.AbsorbDamageToMp);

      return damage;
    }
    return damage;
  }

  public double absorbToSummon(Creature attacker, double damage)
  {
    double transferToSummonDam = calcStat(Stats.TRANSFER_TO_SUMMON_DAMAGE_PERCENT, 0.0D);
    if (transferToSummonDam > 0.0D)
    {
      Summon summon = getPet();
      double transferDamage = damage * transferToSummonDam * 0.01D;
      if ((summon == null) || (summon.isDead()) || (summon.getCurrentHp() < transferDamage)) {
        getEffectList().stopEffects(EffectType.AbsorbDamageToSummon);
      } else if ((summon.isSummon()) && (summon.isInRangeZ(this, 1200L)))
      {
        damage -= transferDamage;

        summon.reduceCurrentHp(transferDamage, summon, null, false, false, false, false, true, false, true);
      }
    }
    return damage;
  }

  public void addBlockStats(List<Stats> stats)
  {
    if (_blockedStats == null)
      _blockedStats = new ArrayList();
    _blockedStats.addAll(stats);
  }

  public Skill addSkill(Skill newSkill)
  {
    if (newSkill == null) {
      return null;
    }
    Skill oldSkill = (Skill)_skills.get(Integer.valueOf(newSkill.getId()));

    if ((oldSkill != null) && (oldSkill.getLevel() == newSkill.getLevel())) {
      return newSkill;
    }

    _skills.put(Integer.valueOf(newSkill.getId()), newSkill);

    if (oldSkill != null)
    {
      removeStatsOwner(oldSkill);
      removeTriggers(oldSkill);
    }

    addTriggers(newSkill);

    addStatFuncs(newSkill.getStatFuncs());

    return oldSkill;
  }

  public Calculator[] getCalculators()
  {
    return _calculators;
  }

  public final void addStatFunc(Func f)
  {
    if (f == null)
      return;
    int stat = f.stat.ordinal();
    synchronized (_calculators)
    {
      if (_calculators[stat] == null)
        _calculators[stat] = new Calculator(f.stat, this);
      _calculators[stat].addFunc(f);
    }
  }

  public final void addStatFuncs(Func[] funcs)
  {
    for (Func f : funcs)
      addStatFunc(f);
  }

  public final void removeStatFunc(Func f)
  {
    if (f == null)
      return;
    int stat = f.stat.ordinal();
    synchronized (_calculators)
    {
      if (_calculators[stat] != null)
        _calculators[stat].removeFunc(f);
    }
  }

  public final void removeStatFuncs(Func[] funcs)
  {
    for (Func f : funcs)
      removeStatFunc(f);
  }

  public final void removeStatsOwner(Object owner)
  {
    synchronized (_calculators)
    {
      for (int i = 0; i < _calculators.length; i++)
        if (_calculators[i] != null)
          _calculators[i].removeOwner(owner);
    }
  }

  public void altOnMagicUseTimer(Creature aimingTarget, Skill skill)
  {
    if (isAlikeDead())
      return;
    int magicId = skill.getDisplayId();
    int level = Math.max(1, getSkillDisplayLevel(Integer.valueOf(skill.getId())));
    List targets = skill.getTargets(this, aimingTarget, true);
    broadcastPacket(new L2GameServerPacket[] { new MagicSkillLaunched(getObjectId(), magicId, level, targets) });
    double mpConsume2 = skill.getMpConsume2();
    if (mpConsume2 > 0.0D)
    {
      if (_currentMp < mpConsume2)
      {
        sendPacket(Msg.NOT_ENOUGH_MP);
        return;
      }
      if (skill.isMagic())
        reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
      else
        reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
    }
    callSkill(skill, targets, false);
  }

  public void altUseSkill(Skill skill, Creature target)
  {
    if (skill == null)
      return;
    int magicId = skill.getId();
    if (isUnActiveSkill(magicId))
      return;
    if (isSkillDisabled(skill))
    {
      sendReuseMessage(skill);
      return;
    }
    if (target == null)
    {
      target = skill.getAimingTarget(this, getTarget());
      if (target == null) {
        return;
      }
    }
    getListeners().onMagicUse(skill, target, true);

    int[] itemConsume = skill.getItemConsume();

    if (itemConsume[0] > 0) {
      for (int i = 0; i < itemConsume.length; i++) {
        if (consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
          continue;
        sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
        return;
      }
    }
    if ((skill.getReferenceItemId() > 0) && 
      (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))) {
      return;
    }
    if (skill.getSoulsConsume() > getConsumedSouls())
    {
      sendPacket(Msg.THERE_IS_NOT_ENOUGHT_SOUL);
      return;
    }

    if (skill.getEnergyConsume() > getAgathionEnergy())
    {
      sendPacket(SystemMsg.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
      return;
    }

    if (skill.getSoulsConsume() > 0)
      setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
    if (skill.getEnergyConsume() > 0) {
      setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());
    }
    int level = Math.max(1, getSkillDisplayLevel(Integer.valueOf(magicId)));
    Formulas.calcSkillMastery(skill, this);
    long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);
    if (!skill.isToggle()) {
      broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay) });
    }
    if (!skill.isHideUseMessage()) {
      if (skill.getSkillType() == Skill.SkillType.PET_SUMMON)
        sendPacket(new SystemMessage(547));
      else if (!skill.isHandler())
        sendPacket(new SystemMessage(46).addSkillName(magicId, level));
      else
        sendPacket(new SystemMessage(46).addItemName(skill.getItemConsumeId()[0]));
    }
    if (!skill.isHandler()) {
      disableSkill(skill, reuseDelay);
    }
    ThreadPoolManager.getInstance().schedule(new GameObjectTasks.AltMagicUseTask(this, target, skill), skill.getHitTime());
  }

  public void sendReuseMessage(Skill skill)
  {
  }

  public void broadcastPacket(L2GameServerPacket[] packets)
  {
    sendPacket(packets);
    broadcastPacketToOthers(packets);
  }

  public void broadcastPacket(List<L2GameServerPacket> packets)
  {
    sendPacket(packets);
    broadcastPacketToOthers(packets);
  }

  public void broadcastPacketToOthers(L2GameServerPacket[] packets)
  {
    if ((!isVisible()) || (packets.length == 0)) {
      return;
    }
    List players = World.getAroundPlayers(this);

    for (int i = 0; i < players.size(); i++)
    {
      Player target = (Player)players.get(i);
      target.sendPacket(packets);
    }
  }

  public void broadcastPacketToOthers(List<L2GameServerPacket> packets)
  {
    if ((!isVisible()) || (packets.isEmpty())) {
      return;
    }
    List players = World.getAroundPlayers(this);

    for (int i = 0; i < players.size(); i++)
    {
      Player target = (Player)players.get(i);
      target.sendPacket(packets);
    }
  }

  public void broadcastToStatusListeners(L2GameServerPacket[] packets)
  {
    if ((!isVisible()) || (packets.length == 0)) {
      return;
    }
    statusListenersLock.lock();
    try
    {
      if ((_statusListeners == null) || (_statusListeners.isEmpty())) {
        return;
      }
      for (int i = 0; i < _statusListeners.size(); i++)
      {
        Player player = (Player)_statusListeners.get(i);
        player.sendPacket(packets);
      }
    }
    finally
    {
      statusListenersLock.unlock();
    }
  }

  public void addStatusListener(Player cha)
  {
    if (cha == this) {
      return;
    }
    statusListenersLock.lock();
    try
    {
      if (_statusListeners == null)
        _statusListeners = new LazyArrayList();
      if (!_statusListeners.contains(cha))
        _statusListeners.add(cha);
    }
    finally
    {
      statusListenersLock.unlock();
    }
  }

  public void removeStatusListener(Creature cha)
  {
    statusListenersLock.lock();
    try
    {
      if (_statusListeners == null) return;
      _statusListeners.remove(cha);
    }
    finally
    {
      statusListenersLock.unlock();
    }
  }

  public void clearStatusListeners()
  {
    statusListenersLock.lock();
    try
    {
      if (_statusListeners == null) return;
      _statusListeners.clear();
    }
    finally
    {
      statusListenersLock.unlock();
    }
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
      case 27:
        su.addAttribute(field, getKarma());
        break;
      case 33:
        su.addAttribute(field, (int)getCurrentCp());
        break;
      case 34:
        su.addAttribute(field, getMaxCp());
        break;
      case 26:
        su.addAttribute(field, getPvpFlag());
      case 13:
      case 14:
      case 15:
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

  public void broadcastStatusUpdate()
  {
    if (!needStatusUpdate()) {
      return;
    }
    StatusUpdate su = makeStatusUpdate(new int[] { 10, 12, 9, 11 });
    broadcastToStatusListeners(new L2GameServerPacket[] { su });
  }

  public int calcHeading(int x_dest, int y_dest)
  {
    return (int)(Math.atan2(getY() - y_dest, getX() - x_dest) * 10430.378350470453D) + 32768;
  }

  public final double calcStat(Stats stat, double init)
  {
    return calcStat(stat, init, null, null);
  }

  public final double calcStat(Stats stat, double init, Creature target, Skill skill)
  {
    int id = stat.ordinal();
    Calculator c = _calculators[id];
    if (c == null)
      return init;
    Env env = new Env();
    env.character = this;
    env.target = target;
    env.skill = skill;
    env.value = init;
    c.calc(env);
    return env.value;
  }

  public final double calcStat(Stats stat, Creature target, Skill skill)
  {
    Env env = new Env(this, target, skill);
    env.value = stat.getInit();
    int id = stat.ordinal();
    Calculator c = _calculators[id];
    if (c != null)
      c.calc(env);
    return env.value;
  }

  public int calculateAttackDelay()
  {
    return Formulas.calcPAtkSpd(getPAtkSpd());
  }

  public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
  {
    try
    {
      if ((useActionSkills) && (!skill.isUsingWhileCasting()) && (_triggers != null)) {
        if (skill.isOffensive())
        {
          if (skill.isMagic())
            useTriggers(getTarget(), TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0.0D);
          else
            useTriggers(getTarget(), TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0.0D);
        }
        else if (skill.isMagic())
        {
          boolean targetSelf = (skill.isAoE()) || (skill.isNotTargetAoE()) || (skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF);
          useTriggers(targetSelf ? this : getTarget(), TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0.0D);
        }
      }
      Player pl = getPlayer();

      Iterator itr = targets.iterator();
      while (itr.hasNext())
      {
        Creature target = (Creature)itr.next();

        if ((skill.isOffensive()) && (target.isInvul()))
        {
          Player pcTarget = target.getPlayer();
          if (((!skill.isIgnoreInvul()) || ((pcTarget != null) && (pcTarget.isGM()))) && (!target.isArtefact()))
          {
            itr.remove();
            continue;
          }
        }

        Effect ie = target.getEffectList().getEffectByType(EffectType.IgnoreSkill);
        if ((ie != null) && 
          (ArrayUtils.contains(ie.getTemplate().getParam().getIntegerArray("skillId"), skill.getId())))
        {
          itr.remove();
          continue;
        }

        target.getListeners().onMagicHit(skill, this);
        NpcInstance npc;
        if ((pl != null) && 
          (target != null) && (target.isNpc()))
        {
          npc = (NpcInstance)target;
          List ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
          if (ql != null) {
            for (QuestState qs : ql)
              qs.getQuest().notifySkillUse(npc, skill, qs);
          }
        }
        if (skill.getNegateSkill() > 0) {
          for (Effect e : target.getEffectList().getAllEffects())
          {
            Skill efs = e.getSkill();
            if ((efs.getId() == skill.getNegateSkill()) && (e.isCancelable()) && ((skill.getNegatePower() <= 0) || (efs.getPower() <= skill.getNegatePower())))
              e.exit();
          }
        }
        if ((skill.getCancelTarget() > 0) && 
          (Rnd.chance(skill.getCancelTarget())) && 
          ((target.getCastingSkill() == null) || ((target.getCastingSkill().getSkillType() != Skill.SkillType.TAKECASTLE) && (target.getCastingSkill().getSkillType() != Skill.SkillType.TAKEFORTRESS) && (target.getCastingSkill().getSkillType() != Skill.SkillType.TAKEFLAG))) && (!target.isRaid()))
        {
          target.abortAttack(true, true);
          target.abortCast(true, true);
          target.setTarget(null);
        }
      }

      if (skill.isOffensive()) {
        startAttackStanceTask();
      }

      if ((!skill.isNotTargetAoE()) || (!skill.isOffensive()) || (targets.size() != 0)) {
        skill.getEffects(this, this, false, true);
      }
      skill.useSkill(this, targets);
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
  }

  public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
  {
    if (_triggers == null) {
      return;
    }
    Set SkillsOnSkillAttack = (Set)_triggers.get(type);
    if (SkillsOnSkillAttack != null)
      for (TriggerInfo t : SkillsOnSkillAttack)
        if (t.getSkill() != ex)
          useTriggerSkill(target == null ? getTarget() : target, null, t, owner, damage);
  }

  public void useTriggerSkill(GameObject target, List<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
  {
    Skill skill = trigger.getSkill();
    if ((skill.getReuseDelay() > 0L) && (isSkillDisabled(skill))) {
      return;
    }
    Creature aimTarget = skill.getAimingTarget(this, target);

    Creature realTarget = (target != null) && (target.isCreature()) ? (Creature)target : null;
    if ((Rnd.chance(trigger.getChance())) && (trigger.checkCondition(this, realTarget, aimTarget, owner, damage)) && (skill.checkCondition(this, aimTarget, false, true, true)))
    {
      if (targets == null) {
        targets = skill.getTargets(this, aimTarget, false);
      }
      int displayId = 0; int displayLevel = 0;

      if (skill.hasEffects())
      {
        displayId = skill.getEffectTemplates()[0]._displayId;
        displayLevel = skill.getEffectTemplates()[0]._displayLevel;
      }

      if (displayId == 0)
        displayId = skill.getDisplayId();
      if (displayLevel == 0) {
        displayLevel = skill.getDisplayLevel();
      }
      if (trigger.getType() != TriggerType.SUPPORT_MAGICAL_SKILL_USE) {
        for (Creature cha : targets)
          broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(this, cha, displayId, displayLevel, 0, 0L) });
      }
      Formulas.calcSkillMastery(skill, this);
      callSkill(skill, targets, false);
      disableSkill(skill, skill.getReuseDelay());
    }
  }

  public boolean checkBlockedStat(Stats stat)
  {
    return (_blockedStats != null) && (_blockedStats.contains(stat));
  }

  public boolean checkReflectSkill(Creature attacker, Skill skill)
  {
    if (!skill.isReflectable()) {
      return false;
    }
    if ((isInvul()) || (attacker.isInvul()) || (!skill.isOffensive())) {
      return false;
    }
    if ((skill.isMagic()) && (skill.getSkillType() != Skill.SkillType.MDAM))
      return false;
    if (Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0.0D, attacker, skill)))
    {
      sendPacket(new SystemMessage(1998).addName(attacker));
      attacker.sendPacket(new SystemMessage(1999).addName(this));
      return true;
    }
    return false;
  }

  public void doCounterAttack(Skill skill, Creature attacker, boolean blow)
  {
    if (isDead())
      return;
    if ((isDamageBlocked()) || (attacker.isDamageBlocked()))
      return;
    if ((skill == null) || (skill.hasEffects()) || (skill.isMagic()) || (!skill.isOffensive()) || (skill.getCastRange() > 200))
      return;
    if (Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0.0D, attacker, skill)))
    {
      double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
      attacker.sendPacket(new SystemMessage(1997).addName(this));
      if (blow)
      {
        sendPacket(new SystemMessage(1997).addName(this));
        sendPacket(new SystemMessage(2261).addName(this).addName(attacker).addNumber(()damage));
        attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
      }
      else {
        sendPacket(new SystemMessage(1997).addName(this));
      }sendPacket(new SystemMessage(2261).addName(this).addName(attacker).addNumber(()damage));
      attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
    }
  }

  public void disableSkill(Skill skill, long delay)
  {
    _skillReuses.put(skill.hashCode(), new TimeStamp(skill, delay));
  }

  public abstract boolean isAutoAttackable(Creature paramCreature);

  public void doAttack(Creature target) {
    if ((target == null) || (isAMuted()) || (isAttackingNow()) || (isAlikeDead()) || (target.isAlikeDead()) || (!isInRange(target, 2000L)) || ((isPlayer()) && (getPlayer().isInMountTransform()))) {
      return;
    }
    getListeners().onAttack(target);

    int sAtk = Math.max(calculateAttackDelay(), 333);
    int ssGrade = 0;

    WeaponTemplate weaponItem = getActiveWeaponItem();
    if (weaponItem != null)
    {
      if ((isPlayer()) && (weaponItem.getAttackReuseDelay() > 0))
      {
        int reuse = (int)(weaponItem.getAttackReuseDelay() * getReuseModifier(target) * 666.0D * calcStat(Stats.ATK_BASE, 0.0D, target, null) / 293.0D / getPAtkSpd());
        if (reuse > 0)
        {
          sendPacket(new SetupGauge(this, 1, reuse));
          _attackReuseEndTime = (reuse + System.currentTimeMillis() - 75L);
          if (reuse > sAtk) {
            ThreadPoolManager.getInstance().schedule(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT, null, null), reuse);
          }
        }
      }
      ssGrade = weaponItem.getCrystalType().externalOrdinal;
    }

    _attackEndTime = (sAtk + System.currentTimeMillis() - 10L);
    _isAttackAborted = false;

    Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);

    setHeading(PositionUtils.calculateHeadingFrom(this, target));

    if (weaponItem == null)
      doAttackHitSimple(attack, target, 1.0D, !isPlayer(), sAtk, true);
    else {
      switch (1.$SwitchMap$l2p$gameserver$templates$item$WeaponTemplate$WeaponType[weaponItem.getItemType().ordinal()])
      {
      case 1:
      case 2:
        doAttackHitByBow(attack, target, sAtk);
        break;
      case 3:
        doAttackHitByPole(attack, target, sAtk);
        break;
      case 4:
      case 5:
      case 6:
        doAttackHitByDual(attack, target, sAtk);
        break;
      default:
        doAttackHitSimple(attack, target, 1.0D, true, sAtk, true);
      }
    }
    if (attack.hasHits())
      broadcastPacket(new L2GameServerPacket[] { attack });
  }

  private void doAttackHitSimple(Attack attack, Creature target, double multiplier, boolean unchargeSS, int sAtk, boolean notify)
  {
    int damage1 = 0;
    boolean shld1 = false;
    boolean crit1 = false;
    boolean miss1 = Formulas.calcHitMiss(this, target);

    if (!miss1)
    {
      Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
      damage1 = (int)(info.damage * multiplier);
      shld1 = info.shld;
      crit1 = info.crit;
    }

    ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify), sAtk);

    attack.addHit(target, damage1, miss1, crit1, shld1);
  }

  private void doAttackHitByBow(Attack attack, Creature target, int sAtk)
  {
    WeaponTemplate activeWeapon = getActiveWeaponItem();
    if (activeWeapon == null) {
      return;
    }
    int damage1 = 0;
    boolean shld1 = false;
    boolean crit1 = false;

    boolean miss1 = Formulas.calcHitMiss(this, target);

    reduceArrowCount();

    if (!miss1)
    {
      Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
      damage1 = (int)info.damage;
      shld1 = info.shld;
      crit1 = info.crit;

      int range = activeWeapon.getAttackRange();
      damage1 = (int)(damage1 * (Math.min(range, getDistance(target)) / range * 0.4D + 0.8D));
    }

    ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true), sAtk);

    attack.addHit(target, damage1, miss1, crit1, shld1);
  }

  private void doAttackHitByDual(Attack attack, Creature target, int sAtk)
  {
    int damage1 = 0;
    int damage2 = 0;
    boolean shld1 = false;
    boolean shld2 = false;
    boolean crit1 = false;
    boolean crit2 = false;

    boolean miss1 = Formulas.calcHitMiss(this, target);
    boolean miss2 = Formulas.calcHitMiss(this, target);

    if (!miss1)
    {
      Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
      damage1 = (int)info.damage;
      shld1 = info.shld;
      crit1 = info.crit;
    }

    if (!miss2)
    {
      Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
      damage2 = (int)info.damage;
      shld2 = info.shld;
      crit2 = info.crit;
    }

    ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false), sAtk / 2);
    ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true), sAtk);

    attack.addHit(target, damage1, miss1, crit1, shld1);
    attack.addHit(target, damage2, miss2, crit2, shld2);
  }

  private void doAttackHitByPole(Attack attack, Creature target, int sAtk)
  {
    int angle = (int)calcStat(Stats.POLE_ATTACK_ANGLE, 90.0D, target, null);
    int range = (int)calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, target, null);

    int attackcountmax = (int)Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0.0D, target, null));

    if (isBoss())
      attackcountmax += 27;
    else if (isRaid())
      attackcountmax += 12;
    else if ((isMonster()) && (getLevel() > 0)) {
      attackcountmax = (int)(attackcountmax + getLevel() / 7.5D);
    }
    double mult = 1.0D;
    _poleAttackCount = 1;

    if (!isInZonePeace()) {
      for (Creature t : getAroundCharacters(range, 200)) {
        if (_poleAttackCount > attackcountmax)
          break;
        if ((t == target) || (t.isDead()) || (!PositionUtils.isFacing(this, t, angle))) {
          continue;
        }
        if (t.isAutoAttackable(this))
        {
          doAttackHitSimple(attack, t, mult, false, sAtk, false);
          mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
          _poleAttackCount += 1;
        }
      }

    }

    _poleAttackCount = 0;
    doAttackHitSimple(attack, target, 1.0D, true, sAtk, true);
  }

  public long getAnimationEndTime()
  {
    return _animationEndTime;
  }

  public void doCast(Skill skill, Creature target, boolean forceUse)
  {
    if (skill == null) {
      return;
    }
    int[] itemConsume = skill.getItemConsume();

    if (itemConsume[0] > 0) {
      for (int i = 0; i < itemConsume.length; i++) {
        if (consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
          continue;
        sendPacket(skill.isHandler() ? SystemMsg.INCORRECT_ITEM_COUNT : SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
        return;
      }
    }
    if ((skill.getReferenceItemId() > 0) && 
      (!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))) {
      return;
    }
    int magicId = skill.getId();

    if (target == null)
      target = skill.getAimingTarget(this, getTarget());
    if (target == null) {
      return;
    }
    getListeners().onMagicUse(skill, target, false);

    if (this != target) {
      setHeading(PositionUtils.calculateHeadingFrom(this, target));
    }
    int level = Math.max(1, getSkillDisplayLevel(Integer.valueOf(magicId)));

    int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
    int skillInterruptTime = skill.isMagic() ? Formulas.calcMAtkSpd(this, skill, skill.getSkillInterruptTime()) : 0;

    int minCastTime = Math.min(Config.SKILLS_CAST_TIME_MIN, skill.getHitTime());
    if (skillTime < minCastTime)
    {
      skillTime = minCastTime;
      skillInterruptTime = 0;
    }

    _animationEndTime = (System.currentTimeMillis() + skillTime);

    if ((skill.isMagic()) && (!skill.isSkillTimePermanent()) && (getChargedSpiritShot() > 0))
    {
      skillTime = (int)(0.7D * skillTime);
      skillInterruptTime = (int)(0.7D * skillInterruptTime);
    }

    Formulas.calcSkillMastery(skill, this);
    long reuseDelay = Math.max(0L, Formulas.calcSkillReuseDelay(this, skill));

    broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay) });

    if (!skill.isHandler()) {
      disableSkill(skill, reuseDelay);
    }
    if (isPlayer()) {
      if (skill.getSkillType() == Skill.SkillType.PET_SUMMON)
        sendPacket(Msg.SUMMON_A_PET);
      else if (!skill.isHandler())
        sendPacket(new SystemMessage(46).addSkillName(magicId, level));
      else
        sendPacket(new SystemMessage(46).addItemName(skill.getItemConsumeId()[0]));
    }
    if (skill.getTargetType() == Skill.SkillTargetType.TARGET_HOLY) {
      target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, Integer.valueOf(1));
    }
    double mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : skill.getMpConsume1();
    if (mpConsume1 > 0.0D)
    {
      if (_currentMp < mpConsume1)
      {
        sendPacket(Msg.NOT_ENOUGH_MP);
        onCastEndTime();
        return;
      }
      reduceCurrentMp(mpConsume1, null);
    }

    _flyLoc = null;
    switch (skill.getFlyType())
    {
    case DUMMY:
    case CHARGE:
      Location flyLoc = getFlyLocation(target, skill);
      if (flyLoc != null)
      {
        _flyLoc = flyLoc;
        broadcastPacket(new L2GameServerPacket[] { new FlyToLocation(this, flyLoc, skill.getFlyType()) });
      }
      else
      {
        sendPacket(SystemMsg.CANNOT_SEE_TARGET);
        return;
      }
    }

    _castingSkill = skill;
    _castInterruptTime = (System.currentTimeMillis() + skillInterruptTime);
    setCastingTarget(target);

    if (skill.isUsingWhileCasting()) {
      callSkill(skill, skill.getTargets(this, target, forceUse), true);
    }
    if (isPlayer()) {
      sendPacket(new SetupGauge(this, 0, skillTime));
    }
    _scheduledCastCount = skill.getCastCount();
    _scheduledCastInterval = (skill.getCastCount() > 0 ? skillTime / _scheduledCastCount : skillTime);

    _skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicLaunchedTask(this, forceUse), skillInterruptTime);
    _skillTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicUseTask(this, forceUse), skillTime);
  }

  private Location getFlyLocation(GameObject target, Skill skill)
  {
    if ((target != null) && (target != this))
    {
      double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
      Location loc;
      Location loc;
      if (skill.isFlyToBack())
        loc = new Location(target.getX() + (int)(Math.sin(radian) * 40.0D), target.getY() - (int)(Math.cos(radian) * 40.0D), target.getZ());
      else {
        loc = new Location(target.getX() - (int)(Math.sin(radian) * 40.0D), target.getY() + (int)(Math.cos(radian) * 40.0D), target.getZ());
      }
      if (isFlying())
      {
        if ((isPlayer()) && (((Player)this).isInFlyingTransform()) && ((loc.z <= 0) || (loc.z >= 6000)))
          return null;
        if (GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getGeoIndex()) == null)
          return null;
      }
      else
      {
        loc.correctGeoZ();

        if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
        {
          loc = target.getLoc();
          if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex())) {
            return null;
          }
        }
      }
      return loc;
    }

    double radian = PositionUtils.convertHeadingToRadian(getHeading());
    int x1 = -(int)(Math.sin(radian) * skill.getFlyRadius());
    int y1 = (int)(Math.cos(radian) * skill.getFlyRadius());

    if (isFlying())
      return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getColRadius(), getGeoIndex());
    return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
  }

  public final void doDie(Creature killer)
  {
    if (!isDead.compareAndSet(false, true)) {
      return;
    }
    onDeath(killer);
  }

  protected void onDeath(Creature killer)
  {
    if (killer != null)
    {
      Player killerPlayer = killer.getPlayer();
      if (killerPlayer != null) {
        killerPlayer.getListeners().onKillIgnorePetOrSummon(this);
      }
      killer.getListeners().onKill(this);

      if ((isPlayer()) && (killer.isPlayable())) {
        _currentCp = 0.0D;
      }
    }
    setTarget(null);
    stopMove();
    stopAttackStanceTask();
    stopRegeneration();

    _currentHp = 0.0D;

    if ((isBlessedByNoblesse()) || (isSalvation()))
    {
      if ((isSalvation()) && (isPlayer()) && (!getPlayer().isInOlympiadMode()))
        getPlayer().reviveRequest(getPlayer(), 100.0D, false);
      for (Effect e : getEffectList().getAllEffects())
      {
        if ((e.getEffectType() == EffectType.BlessNoblesse) || (e.getSkill().getId() == 1325) || (e.getSkill().getId() == 2168)) {
          e.exit();
        } else if (e.getEffectType() == EffectType.AgathionResurrect)
        {
          if (isPlayer())
            getPlayer().setAgathionRes(true);
          e.exit();
        }
      }
    } else {
      for (Effect e : getEffectList().getAllEffects())
      {
        if ((e.getEffectType() != EffectType.Transformation) && (!e.getSkill().isPreservedOnDeath()))
          e.exit(); 
      }
    }
    ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null));

    getListeners().onDeath(killer);

    updateEffectIcons();
    updateStats();
    broadcastStatusUpdate();
  }

  protected void onRevive()
  {
  }

  public void enableSkill(Skill skill)
  {
    _skillReuses.remove(skill.hashCode());
  }

  public int getAbnormalEffect()
  {
    return _abnormalEffects;
  }

  public int getAbnormalEffect2()
  {
    return _abnormalEffects2;
  }

  public int getAbnormalEffect3()
  {
    return _abnormalEffects3;
  }

  public int getAccuracy()
  {
    return (int)calcStat(Stats.ACCURACY_COMBAT, 0.0D, null, null);
  }

  public Collection<Skill> getAllSkills()
  {
    return _skills.values();
  }

  public final Skill[] getAllSkillsArray()
  {
    Collection vals = _skills.values();
    return (Skill[])vals.toArray(new Skill[vals.size()]);
  }

  public final double getAttackSpeedMultiplier()
  {
    return 1.1D * getPAtkSpd() / getTemplate().basePAtkSpd;
  }

  public int getBuffLimit()
  {
    return (int)calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
  }

  public Skill getCastingSkill()
  {
    return _castingSkill;
  }

  public int getCON()
  {
    return (int)calcStat(Stats.STAT_CON, _template.baseCON, null, null);
  }

  public int getCriticalHit(Creature target, Skill skill)
  {
    return (int)calcStat(Stats.CRITICAL_BASE, _template.baseCritRate, target, skill);
  }

  public double getMagicCriticalRate(Creature target, Skill skill)
  {
    return calcStat(Stats.MCRITICAL_RATE, target, skill);
  }

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
    return getCurrentCpRatio() * 100.0D;
  }

  public final boolean isCurrentCpFull()
  {
    return getCurrentCp() >= getMaxCp();
  }

  public final boolean isCurrentCpZero()
  {
    return getCurrentCp() < 1.0D;
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
    return getCurrentHpRatio() * 100.0D;
  }

  public final boolean isCurrentHpFull()
  {
    return getCurrentHp() >= getMaxHp();
  }

  public final boolean isCurrentHpZero()
  {
    return getCurrentHp() < 1.0D;
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
    return getCurrentMpRatio() * 100.0D;
  }

  public final boolean isCurrentMpFull()
  {
    return getCurrentMp() >= getMaxMp();
  }

  public final boolean isCurrentMpZero()
  {
    return getCurrentMp() < 1.0D;
  }

  public Location getDestination()
  {
    return destination;
  }

  public int getDEX()
  {
    return (int)calcStat(Stats.STAT_DEX, _template.baseDEX, null, null);
  }

  public int getEvasionRate(Creature target)
  {
    return (int)calcStat(Stats.EVASION_RATE, 0.0D, target, null);
  }

  public int getINT()
  {
    return (int)calcStat(Stats.STAT_INT, _template.baseINT, null, null);
  }

  public List<Creature> getAroundCharacters(int radius, int height)
  {
    if (!isVisible())
      return Collections.emptyList();
    return World.getAroundCharacters(this, radius, height);
  }

  public List<NpcInstance> getAroundNpc(int range, int height)
  {
    if (!isVisible())
      return Collections.emptyList();
    return World.getAroundNpc(this, range, height);
  }

  public boolean knowsObject(GameObject obj)
  {
    return World.getAroundObjectById(this, obj.getObjectId()) != null;
  }

  public final Skill getKnownSkill(int skillId)
  {
    return (Skill)_skills.get(Integer.valueOf(skillId));
  }

  public final int getMagicalAttackRange(Skill skill)
  {
    if (skill != null)
      return (int)calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
    return getTemplate().baseAtkRange;
  }

  public int getMAtk(Creature target, Skill skill)
  {
    if ((skill != null) && (skill.getMatak() > 0))
      return skill.getMatak();
    return (int)calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
  }

  public int getMAtkSpd()
  {
    return (int)calcStat(Stats.MAGIC_ATTACK_SPEED, _template.baseMAtkSpd, null, null);
  }

  public final int getMaxCp()
  {
    return (int)calcStat(Stats.MAX_CP, _template.baseCpMax, null, null);
  }

  public int getMaxHp()
  {
    return (int)calcStat(Stats.MAX_HP, _template.baseHpMax, null, null);
  }

  public int getMaxMp()
  {
    return (int)calcStat(Stats.MAX_MP, _template.baseMpMax, null, null);
  }

  public int getMDef(Creature target, Skill skill)
  {
    return Math.max((int)calcStat(Stats.MAGIC_DEFENCE, _template.baseMDef, target, skill), 1);
  }

  public int getMEN()
  {
    return (int)calcStat(Stats.STAT_MEN, _template.baseMEN, null, null);
  }

  public double getMinDistance(GameObject obj)
  {
    double distance = getTemplate().collisionRadius;

    if ((obj != null) && (obj.isCreature())) {
      distance += ((Creature)obj).getTemplate().collisionRadius;
    }
    return distance;
  }

  public double getMovementSpeedMultiplier()
  {
    return getRunSpeed() * 1.0D / _template.baseRunSpd;
  }

  public int getMoveSpeed()
  {
    if (isRunning()) {
      return getRunSpeed();
    }
    return getWalkSpeed();
  }

  public String getName()
  {
    return StringUtils.defaultString(_name);
  }

  public int getPAtk(Creature target)
  {
    return (int)calcStat(Stats.POWER_ATTACK, _template.basePAtk, target, null);
  }

  public int getPAtkSpd()
  {
    return (int)calcStat(Stats.POWER_ATTACK_SPEED, _template.basePAtkSpd, null, null);
  }

  public int getPDef(Creature target)
  {
    return (int)calcStat(Stats.POWER_DEFENCE, _template.basePDef, target, null);
  }

  public final int getPhysicalAttackRange()
  {
    return (int)calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
  }

  public final int getRandomDamage()
  {
    WeaponTemplate weaponItem = getActiveWeaponItem();
    if (weaponItem == null)
      return 5 + (int)Math.sqrt(getLevel());
    return weaponItem.getRandomDamage();
  }

  public double getReuseModifier(Creature target)
  {
    return calcStat(Stats.ATK_REUSE, 1.0D, target, null);
  }

  public int getRunSpeed()
  {
    return getSpeed(_template.baseRunSpd);
  }

  public final int getShldDef()
  {
    if (isPlayer())
      return (int)calcStat(Stats.SHIELD_DEFENCE, 0.0D, null, null);
    return (int)calcStat(Stats.SHIELD_DEFENCE, _template.baseShldDef, null, null);
  }

  public final int getSkillDisplayLevel(Integer skillId)
  {
    Skill skill = (Skill)_skills.get(skillId);
    if (skill == null)
      return -1;
    return skill.getDisplayLevel();
  }

  public final int getSkillLevel(Integer skillId)
  {
    return getSkillLevel(skillId, -1);
  }

  public final int getSkillLevel(Integer skillId, int def)
  {
    Skill skill = (Skill)_skills.get(skillId);
    if (skill == null)
      return def;
    return skill.getLevel();
  }

  public int getSkillMastery(Integer skillId)
  {
    if (_skillMastery == null)
      return 0;
    Integer val = (Integer)_skillMastery.get(skillId);
    return val == null ? 0 : val.intValue();
  }

  public void removeSkillMastery(Integer skillId)
  {
    if (_skillMastery != null)
      _skillMastery.remove(skillId);
  }

  public int getSpeed(int baseSpeed)
  {
    if (isInWater())
      return getSwimSpeed();
    return (int)calcStat(Stats.RUN_SPEED, baseSpeed, null, null);
  }

  public int getSTR()
  {
    return (int)calcStat(Stats.STAT_STR, _template.baseSTR, null, null);
  }

  public int getSwimSpeed()
  {
    return (int)calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
  }

  public GameObject getTarget()
  {
    return (GameObject)target.get();
  }

  public final int getTargetId()
  {
    GameObject target = getTarget();
    return target == null ? -1 : target.getObjectId();
  }

  public CharTemplate getTemplate()
  {
    return _template;
  }

  public CharTemplate getBaseTemplate()
  {
    return _baseTemplate;
  }

  public String getTitle()
  {
    return StringUtils.defaultString(_title);
  }

  public final int getWalkSpeed()
  {
    if (isInWater())
      return getSwimSpeed();
    return getSpeed(_template.baseWalkSpd);
  }

  public int getWIT()
  {
    return (int)calcStat(Stats.STAT_WIT, _template.baseWIT, null, null);
  }

  public double headingToRadians(int heading)
  {
    return (heading - 32768) / 10430.378350470453D;
  }

  public boolean isAlikeDead()
  {
    return (_fakeDeath) || (isDead());
  }

  public final boolean isAttackingNow()
  {
    return _attackEndTime > System.currentTimeMillis();
  }

  public final boolean isBlessedByNoblesse()
  {
    return _isBlessedByNoblesse;
  }

  public final boolean isSalvation()
  {
    return _isSalvation;
  }

  public boolean isEffectImmune()
  {
    return _effectImmunity.get();
  }

  public boolean isBuffImmune()
  {
    return _buffImmunity.get();
  }

  public boolean isDebuffImmune()
  {
    return _debuffImmunity.get();
  }

  public boolean isDead()
  {
    return (_currentHp < 0.5D) || (isDead.get());
  }

  public final boolean isFlying()
  {
    return _flying;
  }

  public final boolean isInCombat()
  {
    return System.currentTimeMillis() < _stanceEndTime;
  }

  public boolean isInvul()
  {
    return _isInvul;
  }

  public boolean isMageClass()
  {
    return getTemplate().baseMAtk > 3;
  }

  public final boolean isRunning()
  {
    return _running;
  }

  public boolean isSkillDisabled(Skill skill)
  {
    TimeStamp sts = (TimeStamp)_skillReuses.get(skill.hashCode());
    if (sts == null)
      return false;
    if (sts.hasNotPassed())
      return true;
    _skillReuses.remove(skill.hashCode());
    return false;
  }

  public final boolean isTeleporting()
  {
    return isTeleporting.get();
  }

  public Location getIntersectionPoint(Creature target)
  {
    if (!PositionUtils.isFacing(this, target, 90))
      return new Location(target.getX(), target.getY(), target.getZ());
    double angle = PositionUtils.convertHeadingToDegree(target.getHeading());
    double radian = Math.toRadians(angle - 90.0D);
    double range = target.getMoveSpeed() / 2;
    return new Location((int)(target.getX() - range * Math.sin(radian)), (int)(target.getY() + range * Math.cos(radian)), target.getZ());
  }

  public Location applyOffset(Location point, int offset)
  {
    if (offset <= 0) {
      return point;
    }
    long dx = point.x - getX();
    long dy = point.y - getY();
    long dz = point.z - getZ();

    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

    if (distance <= offset)
    {
      point.set(getX(), getY(), getZ());
      return point;
    }

    if (distance >= 1.0D)
    {
      double cut = offset / distance;
      point.x -= (int)(dx * cut + 0.5D);
      point.y -= (int)(dy * cut + 0.5D);
      point.z -= (int)(dz * cut + 0.5D);

      if ((!isFlying()) && (!isInBoat()) && (!isInWater()) && (!isBoat())) {
        point.correctGeoZ();
      }
    }
    return point;
  }

  public List<Location> applyOffset(List<Location> points, int offset)
  {
    offset >>= 4;
    if (offset <= 0) {
      return points;
    }
    long dx = ((Location)points.get(points.size() - 1)).x - ((Location)points.get(0)).x;
    long dy = ((Location)points.get(points.size() - 1)).y - ((Location)points.get(0)).y;
    long dz = ((Location)points.get(points.size() - 1)).z - ((Location)points.get(0)).z;

    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (distance <= offset)
    {
      Location point = (Location)points.get(0);
      points.clear();
      points.add(point);
      return points;
    }

    if (distance >= 1.0D)
    {
      double cut = offset / distance;
      int num = (int)(points.size() * cut + 0.5D);
      for (int i = 1; (i <= num) && (points.size() > 0); i++) {
        points.remove(points.size() - 1);
      }
    }
    return points;
  }

  private boolean setSimplePath(Location dest)
  {
    List moveList = GeoMove.constructMoveList(getLoc(), dest);
    if (moveList.isEmpty())
      return false;
    _targetRecorder.clear();
    _targetRecorder.add(moveList);
    return true;
  }

  private boolean buildPathTo(int x, int y, int z, int offset, boolean pathFind)
  {
    return buildPathTo(x, y, z, offset, null, false, pathFind);
  }

  private boolean buildPathTo(int x, int y, int z, int offset, Creature follow, boolean forestalling, boolean pathFind)
  {
    int geoIndex = getGeoIndex();
    Location dest;
    Location dest;
    if ((forestalling) && (follow != null) && (follow.isMoving))
      dest = getIntersectionPoint(follow);
    else {
      dest = new Location(x, y, z);
    }
    if ((isInBoat()) || (isBoat()) || (!Config.ALLOW_GEODATA))
    {
      applyOffset(dest, offset);
      return setSimplePath(dest);
    }

    if ((isFlying()) || (isInWater()))
    {
      applyOffset(dest, offset);

      if (isFlying())
      {
        if (GeoEngine.canSeeCoord(this, dest.x, dest.y, dest.z, true)) {
          return setSimplePath(dest);
        }
        Location nextloc = GeoEngine.moveCheckInAir(getX(), getY(), getZ(), dest.x, dest.y, dest.z, getColRadius(), geoIndex);
        if ((nextloc != null) && (!nextloc.equals(getX(), getY(), getZ())))
          return setSimplePath(nextloc);
      }
      else
      {
        int waterZ = getWaterZ();
        Location nextloc = GeoEngine.moveInWaterCheck(getX(), getY(), getZ(), dest.x, dest.y, dest.z, waterZ, geoIndex);
        if (nextloc == null) {
          return false;
        }
        List moveList = GeoMove.constructMoveList(getLoc(), nextloc.clone());
        _targetRecorder.clear();
        if (!moveList.isEmpty()) {
          _targetRecorder.add(moveList);
        }
        int dz = dest.z - nextloc.z;

        if ((dz > 0) && (dz < 128))
        {
          moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, geoIndex, false);
          if (moveList != null)
          {
            if (!moveList.isEmpty()) {
              _targetRecorder.add(moveList);
            }
          }
        }
        return !_targetRecorder.isEmpty();
      }
      return false;
    }

    List moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, true);
    if (moveList != null)
    {
      if (moveList.isEmpty())
        return false;
      applyOffset(moveList, offset);
      if (moveList.isEmpty())
        return false;
      _targetRecorder.clear();
      _targetRecorder.add(moveList);
      return true;
    }

    if (pathFind)
    {
      List targets = GeoMove.findMovePath(getX(), getY(), getZ(), dest.clone(), this, true, geoIndex);
      if (!targets.isEmpty())
      {
        moveList = (List)targets.remove(targets.size() - 1);
        applyOffset(moveList, offset);
        if (!moveList.isEmpty())
          targets.add(moveList);
        if (!targets.isEmpty())
        {
          _targetRecorder.clear();
          _targetRecorder.addAll(targets);
          return true;
        }
      }
    }

    if (follow != null) {
      return false;
    }
    applyOffset(dest, offset);

    moveList = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, geoIndex, false);
    if ((moveList != null) && (!moveList.isEmpty()))
    {
      _targetRecorder.clear();
      _targetRecorder.add(moveList);
      return true;
    }

    return false;
  }

  public Creature getFollowTarget()
  {
    return (Creature)followTarget.get();
  }

  public void setFollowTarget(Creature target)
  {
    followTarget = (target == null ? HardReferences.emptyRef() : target.getRef());
  }

  public boolean followToCharacter(Creature target, int offset, boolean forestalling)
  {
    return followToCharacter(target.getLoc(), target, offset, forestalling);
  }

  public boolean followToCharacter(Location loc, Creature target, int offset, boolean forestalling)
  {
    moveLock.lock();
    try
    {
      if ((isMovementDisabled()) || (target == null) || (isInBoat()) || (isInWater())) {
        i = 0;
        return i;
      }
      offset = Math.max(offset, 10);
      if ((isFollow) && (target == getFollowTarget()) && (offset == _offset)) {
        i = 1;
        return i;
      }
      if ((Math.abs(getZ() - target.getZ()) > 1000) && (!isFlying()))
      {
        sendPacket(SystemMsg.CANNOT_SEE_TARGET);
        i = 0;
        return i;
      }
      getAI().clearNextAction();

      stopMove(false, false);

      if (buildPathTo(loc.x, loc.y, loc.z, offset, target, forestalling, !target.isDoor())) {
        movingDestTempPos.set(loc.x, loc.y, loc.z);
      } else {
        i = 0;
        return i;
      }
      isMoving = true;
      isFollow = true;
      _forestalling = forestalling;
      _offset = offset;
      setFollowTarget(target);

      moveNext(true);

      int i = 1;
      return i; } finally { moveLock.unlock(); } throw localObject;
  }

  public boolean moveToLocation(Location loc, int offset, boolean pathfinding)
  {
    return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
  }

  public boolean moveToLocation(int x_dest, int y_dest, int z_dest, int offset, boolean pathfinding)
  {
    moveLock.lock();
    try
    {
      offset = Math.max(offset, 0);
      Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
      if ((isMoving) && (!isFollow) && (movingDestTempPos.equals(dst_geoloc)))
      {
        sendActionFailed();
        i = 1;
        return i;
      }
      if (isMovementDisabled())
      {
        getAI().setNextAction(PlayableAI.nextAction.MOVE, new Location(x_dest, y_dest, z_dest), Integer.valueOf(offset), pathfinding, false);
        sendActionFailed();
        i = 0;
        return i;
      }
      getAI().clearNextAction();

      if (isPlayer()) {
        getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
      }
      stopMove(false, false);

      if (buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding)) {
        movingDestTempPos.set(dst_geoloc);
      }
      else {
        sendActionFailed();
        i = 0;
        return i;
      }
      isMoving = true;

      moveNext(true);

      int i = 1;
      return i; } finally { moveLock.unlock(); } throw localObject;
  }

  private void moveNext(boolean firstMove)
  {
    if ((!isMoving) || (isMovementDisabled()))
    {
      stopMove();
      return;
    }

    _previousSpeed = getMoveSpeed();
    if (_previousSpeed <= 0)
    {
      stopMove();
      return;
    }

    if (!firstMove)
    {
      Location dest = destination;
      if (dest != null) {
        setLoc(dest, true);
      }
    }
    if (_targetRecorder.isEmpty())
    {
      CtrlEvent ctrlEvent = isFollow ? CtrlEvent.EVT_ARRIVED_TARGET : CtrlEvent.EVT_ARRIVED;
      stopMove(false, true);
      ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, ctrlEvent));
      return;
    }

    moveList = ((List)_targetRecorder.remove(0));
    Location begin = ((Location)moveList.get(0)).clone().geo2world();
    Location end = ((Location)moveList.get(moveList.size() - 1)).clone().geo2world();
    destination = end;
    double distance = (isFlying()) || (isInWater()) ? begin.distance3D(end) : begin.distance(end);

    if (distance != 0.0D) {
      setHeading(PositionUtils.calculateHeadingFrom(getX(), getY(), destination.x, destination.y));
    }
    broadcastMove();

    _startMoveTime = (this._followTimestamp = System.currentTimeMillis());
    if (_moveTaskRunnable == null)
      _moveTaskRunnable = new MoveNextTask();
    _moveTask = ThreadPoolManager.getInstance().schedule(_moveTaskRunnable.setDist(distance), getMoveTickInterval());
  }

  public int getMoveTickInterval()
  {
    return (isPlayer() ? 16000 : 32000) / Math.max(getMoveSpeed(), 1);
  }

  private void broadcastMove()
  {
    validateLocation(isPlayer() ? 2 : 1);
    broadcastPacket(new L2GameServerPacket[] { movePacket() });
  }

  public void stopMove()
  {
    stopMove(true, true);
  }

  public void stopMove(boolean validate)
  {
    stopMove(true, validate);
  }

  public void stopMove(boolean stop, boolean validate)
  {
    if (!isMoving) {
      return;
    }
    moveLock.lock();
    try
    {
      if (!isMoving)
        return;
      isMoving = false;
      isFollow = false;

      if (_moveTask != null)
      {
        _moveTask.cancel(false);
        _moveTask = null;
      }

      destination = null;
      moveList = null;

      _targetRecorder.clear();

      if (validate)
        validateLocation(isPlayer() ? 2 : 1);
      if (stop)
        broadcastPacket(new L2GameServerPacket[] { stopMovePacket() });
    }
    finally
    {
      moveLock.unlock();
    }
  }

  public int getWaterZ()
  {
    if (!isInWater()) {
      return -2147483648;
    }
    int waterZ = -2147483648;
    zonesRead.lock();
    try
    {
      for (int i = 0; i < _zones.size(); i++)
      {
        Zone zone = (Zone)_zones.get(i);
        if ((zone.getType() != Zone.ZoneType.water) || (
          (waterZ != -2147483648) && (waterZ >= zone.getTerritory().getZmax()))) continue;
        waterZ = zone.getTerritory().getZmax();
      }
    }
    finally
    {
      zonesRead.unlock();
    }

    return waterZ;
  }

  protected L2GameServerPacket stopMovePacket()
  {
    return new StopMove(this);
  }

  public L2GameServerPacket movePacket()
  {
    return new CharMoveToLocation(this);
  }

  public void updateZones()
  {
    if (isInObserverMode()) {
      return;
    }
    Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;

    LazyArrayList entering = null;
    LazyArrayList leaving = null;

    zonesWrite.lock();
    try
    {
      if (!_zones.isEmpty())
      {
        leaving = LazyArrayList.newInstance();
        for (int i = 0; i < _zones.size(); i++)
        {
          Zone zone = (Zone)_zones.get(i);

          if ((!ArrayUtils.contains(zones, zone)) || (!zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))) {
            leaving.add(zone);
          }
        }

        if (!leaving.isEmpty())
        {
          for (int i = 0; i < leaving.size(); i++)
          {
            Zone zone = (Zone)leaving.get(i);
            _zones.remove(zone);
          }
        }
      }

      if (zones.length > 0)
      {
        entering = LazyArrayList.newInstance();
        for (int i = 0; i < zones.length; i++)
        {
          Zone zone = zones[i];

          if ((!_zones.contains(zone)) && (zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))) {
            entering.add(zone);
          }
        }

        if (!entering.isEmpty())
        {
          for (int i = 0; i < entering.size(); i++)
          {
            Zone zone = (Zone)entering.get(i);
            _zones.add(zone);
          }
        }
      }
    }
    finally
    {
      zonesWrite.unlock();
    }

    onUpdateZones(leaving, entering);

    if (leaving != null) {
      LazyArrayList.recycle(leaving);
    }
    if (entering != null)
      LazyArrayList.recycle(entering);
  }

  protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
  {
    if ((leaving != null) && (!leaving.isEmpty()))
    {
      for (int i = 0; i < leaving.size(); i++)
      {
        Zone zone = (Zone)leaving.get(i);
        zone.doLeave(this);
      }
    }

    if ((entering != null) && (!entering.isEmpty()))
    {
      for (int i = 0; i < entering.size(); i++)
      {
        Zone zone = (Zone)entering.get(i);
        zone.doEnter(this);
      }
    }
  }

  public boolean isInZonePeace()
  {
    return (isInZone(Zone.ZoneType.peace_zone)) && (!isInZoneBattle());
  }

  public boolean isInZoneBattle()
  {
    return isInZone(Zone.ZoneType.battle_zone);
  }

  public boolean isInWater()
  {
    return (isInZone(Zone.ZoneType.water)) && (!isInBoat()) && (!isBoat()) && (!isFlying());
  }

  public boolean isInZone(Zone.ZoneType type)
  {
    zonesRead.lock();
    try
    {
      for (int i = 0; i < _zones.size(); i++)
      {
        Zone zone = (Zone)_zones.get(i);
        if (zone.getType() == type) {
          int i = 1;
          return i;
        }
      } } finally { zonesRead.unlock();
    }

    return false;
  }

  public boolean isInZone(String name)
  {
    zonesRead.lock();
    try
    {
      for (int i = 0; i < _zones.size(); i++)
      {
        Zone zone = (Zone)_zones.get(i);
        if (zone.getName().equals(name)) {
          int i = 1;
          return i;
        }
      } } finally { zonesRead.unlock();
    }

    return false;
  }

  public boolean isInZone(Zone zone)
  {
    zonesRead.lock();
    try
    {
      boolean bool = _zones.contains(zone);
      return bool; } finally { zonesRead.unlock(); } throw localObject;
  }

  public Zone getZone(Zone.ZoneType type)
  {
    zonesRead.lock();
    try
    {
      for (int i = 0; i < _zones.size(); i++)
      {
        Zone zone = (Zone)_zones.get(i);
        if (zone.getType() == type) {
          Zone localZone1 = zone;
          return localZone1;
        }
      } } finally { zonesRead.unlock();
    }
    return null;
  }

  public Location getRestartPoint()
  {
    zonesRead.lock();
    try
    {
      for (int i = 0; i < _zones.size(); i++)
      {
        Zone zone = (Zone)_zones.get(i);
        if (zone.getRestartPoints() == null)
          continue;
        Zone.ZoneType type = zone.getType();
        if ((type == Zone.ZoneType.battle_zone) || (type == Zone.ZoneType.peace_zone) || (type == Zone.ZoneType.offshore) || (type == Zone.ZoneType.dummy)) {
          Location localLocation = zone.getSpawn();
          return localLocation;
        }
      } } finally { zonesRead.unlock();
    }

    return null;
  }

  public Location getPKRestartPoint()
  {
    zonesRead.lock();
    try
    {
      for (int i = 0; i < _zones.size(); i++)
      {
        Zone zone = (Zone)_zones.get(i);
        if (zone.getRestartPoints() == null)
          continue;
        Zone.ZoneType type = zone.getType();
        if ((type == Zone.ZoneType.battle_zone) || (type == Zone.ZoneType.peace_zone) || (type == Zone.ZoneType.offshore) || (type == Zone.ZoneType.dummy)) {
          Location localLocation = zone.getPKSpawn();
          return localLocation;
        }
      } } finally { zonesRead.unlock();
    }

    return null;
  }

  public int getGeoZ(Location loc)
  {
    if ((isFlying()) || (isInWater()) || (isInBoat()) || (isBoat()) || (isDoor())) {
      return loc.z;
    }
    return super.getGeoZ(loc);
  }

  protected boolean needStatusUpdate()
  {
    if (!isVisible()) {
      return false;
    }
    boolean result = false;

    int bar = (int)(getCurrentHp() * 352.0D / getMaxHp());
    if ((bar == 0) || (bar != _lastHpBarUpdate))
    {
      _lastHpBarUpdate = bar;
      result = true;
    }

    bar = (int)(getCurrentMp() * 352.0D / getMaxMp());
    if ((bar == 0) || (bar != _lastMpBarUpdate))
    {
      _lastMpBarUpdate = bar;
      result = true;
    }

    if (isPlayer())
    {
      bar = (int)(getCurrentCp() * 352.0D / getMaxCp());
      if ((bar == 0) || (bar != _lastCpBarUpdate))
      {
        _lastCpBarUpdate = bar;
        result = true;
      }
    }

    return result;
  }

  public void onForcedAttack(Player player, boolean shift)
  {
    player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

    if ((!isAttackable(player)) || (player.isConfused()) || (player.isBlocked()))
    {
      player.sendActionFailed();
      return;
    }

    player.getAI().Attack(this, true, shift);
  }

  public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
  {
    if (isAlikeDead())
    {
      sendActionFailed();
      return;
    }

    if ((target.isDead()) || (!isInRange(target, 2000L)))
    {
      sendActionFailed();
      return;
    }

    if ((isPlayable()) && (target.isPlayable()) && (isInZoneBattle() != target.isInZoneBattle()))
    {
      Player player = getPlayer();
      if (player != null)
      {
        player.sendPacket(Msg.INVALID_TARGET);
        player.sendActionFailed();
      }
      return;
    }

    target.getListeners().onAttackHit(this);

    if ((!miss) && (target.isPlayer()) && ((isCursedWeaponEquipped()) || ((getActiveWeaponInstance() != null) && (getActiveWeaponInstance().isHeroWeapon()) && (target.isCursedWeaponEquipped())))) {
      target.setCurrentCp(0.0D);
    }
    if ((target.isStunned()) && (Formulas.calcStunBreak(crit))) {
      target.getEffectList().stopEffects(EffectType.Stun);
    }
    displayGiveDamageMessage(target, damage, crit, miss, shld, false);

    ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, Integer.valueOf(damage)));

    boolean checkPvP = checkPvP(target, null);

    if ((!miss) && (damage > 0))
    {
      target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true);

      if (!target.isDead())
      {
        if (crit) {
          useTriggers(target, TriggerType.CRIT, null, null, damage);
        }
        useTriggers(target, TriggerType.ATTACK, null, null, damage);

        if (Formulas.calcCastBreak(target, crit)) {
          target.abortCast(false, true);
        }
      }
      if ((soulshot) && (unchargeSS)) {
        unChargeShots(false);
      }
    }
    if (miss) {
      target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);
    }
    startAttackStanceTask();

    if (checkPvP)
      startPvPFlag(target);
  }

  public void onMagicUseTimer(Creature aimingTarget, Skill skill, boolean forceUse)
  {
    _castInterruptTime = 0L;

    if (skill.isUsingWhileCasting())
    {
      aimingTarget.getEffectList().stopEffect(skill.getId());
      onCastEndTime();
      return;
    }

    if ((!skill.isOffensive()) && (getAggressionTarget() != null)) {
      forceUse = true;
    }
    if (!skill.checkCondition(this, aimingTarget, forceUse, false, false))
    {
      if ((skill.getSkillType() == Skill.SkillType.PET_SUMMON) && (isPlayer()))
        getPlayer().setPetControlItem(null);
      onCastEndTime();
      return;
    }

    if ((skill.getCastRange() < 32767) && (skill.getSkillType() != Skill.SkillType.TAKECASTLE) && (skill.getSkillType() != Skill.SkillType.TAKEFORTRESS) && (!GeoEngine.canSeeTarget(this, aimingTarget, isFlying())))
    {
      sendPacket(SystemMsg.CANNOT_SEE_TARGET);
      broadcastPacket(new L2GameServerPacket[] { new MagicSkillCanceled(getObjectId()) });
      onCastEndTime();
      return;
    }

    List targets = skill.getTargets(this, aimingTarget, forceUse);

    int hpConsume = skill.getHpConsume();
    if (hpConsume > 0) {
      setCurrentHp(Math.max(0.0D, _currentHp - hpConsume), false);
    }
    double mpConsume2 = skill.getMpConsume2();
    if (mpConsume2 > 0.0D)
    {
      if (skill.isMusic())
      {
        double inc = mpConsume2 / 2.0D;
        double add = 0.0D;
        for (Effect e : getEffectList().getAllEffects())
          if ((e.getSkill().getId() != skill.getId()) && (e.getSkill().isMusic()) && (e.getTimeLeft() > 30))
            add += inc;
        mpConsume2 += add;
        mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
      }
      else if (skill.isMagic()) {
        mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
      } else {
        mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
      }
      if ((_currentMp < mpConsume2) && (isPlayable()))
      {
        sendPacket(Msg.NOT_ENOUGH_MP);
        onCastEndTime();
        return;
      }
      reduceCurrentMp(mpConsume2, null);
    }

    callSkill(skill, targets, true);

    if (skill.getNumCharges() > 0) {
      setIncreasedForce(getIncreasedForce() - skill.getNumCharges());
    }
    if (skill.isSoulBoost())
      setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
    else if (skill.getSoulsConsume() > 0) {
      setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
    }
    switch (skill.getFlyType())
    {
    case THROW_UP:
    case THROW_HORIZONTAL:
      for (Creature target : targets)
      {
        Location flyLoc = getFlyLocation(null, skill);
        target.setLoc(flyLoc);
        broadcastPacket(new L2GameServerPacket[] { new FlyToLocation(target, flyLoc, skill.getFlyType()) });
      }

    }

    if (_scheduledCastCount > 0)
    {
      _scheduledCastCount -= 1;
      _skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicLaunchedTask(this, forceUse), _scheduledCastInterval);
      _skillTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicUseTask(this, forceUse), _scheduledCastInterval);
      return;
    }

    int skillCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
    if (skillCoolTime > 0)
      ThreadPoolManager.getInstance().schedule(new GameObjectTasks.CastEndTimeTask(this), skillCoolTime);
    else
      onCastEndTime();
  }

  public void onCastEndTime()
  {
    finishFly();
    clearCastVars();
    getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, null, null);
  }

  public void clearCastVars()
  {
    _animationEndTime = 0L;
    _castInterruptTime = 0L;
    _scheduledCastCount = 0;
    _castingSkill = null;
    _skillTask = null;
    _skillLaunchedTask = null;
    _flyLoc = null;
  }

  private void finishFly()
  {
    Location flyLoc = _flyLoc;
    _flyLoc = null;
    if (flyLoc != null)
    {
      setLoc(flyLoc);
      validateLocation(1);
    }
  }

  public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
  {
    if ((attacker == null) || (isDead()) || ((attacker.isDead()) && (!isDot))) {
      return;
    }
    if ((isDamageBlocked()) && (transferDamage)) {
      return;
    }
    if ((isDamageBlocked()) && (attacker != this))
    {
      if (sendMessage)
        attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
      return;
    }

    if (canReflect)
    {
      if (attacker.absorbAndReflect(this, skill, damage)) {
        return;
      }
      damage = absorbToEffector(attacker, damage);
      damage = absorbToMp(attacker, damage);
      damage = absorbToSummon(attacker, damage);
    }

    getListeners().onCurrentHpDamage(damage, attacker, skill);

    if (attacker != this)
    {
      if (sendMessage) {
        displayReceiveDamageMessage(attacker, (int)damage);
      }
      if (!isDot) {
        useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
      }
    }
    onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
  }

  protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
  {
    if ((awake) && (isSleeping())) {
      getEffectList().stopEffects(EffectType.Sleep);
    }
    if ((attacker != this) || ((skill != null) && (skill.isOffensive())))
    {
      if (isMeditated())
      {
        Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
        if (effect != null) {
          getEffectList().stopEffect(effect.getSkill());
        }
      }
      startAttackStanceTask();
      checkAndRemoveInvisible();

      if (getCurrentHp() - damage < 0.5D) {
        useTriggers(attacker, TriggerType.DIE, null, null, damage);
      }
    }

    if ((isPlayer()) && (getPlayer().isGM()) && (getPlayer().isUndying()) && (damage + 0.5D >= getCurrentHp())) {
      return;
    }
    setCurrentHp(Math.max(getCurrentHp() - damage, 0.0D), false);

    if (getCurrentHp() < 0.5D)
      doDie(attacker);
  }

  public void reduceCurrentMp(double i, Creature attacker)
  {
    if ((attacker != null) && (attacker != this))
    {
      if (isSleeping()) {
        getEffectList().stopEffects(EffectType.Sleep);
      }
      if (isMeditated())
      {
        Effect effect = getEffectList().getEffectByType(EffectType.Meditation);
        if (effect != null) {
          getEffectList().stopEffect(effect.getSkill());
        }
      }
    }
    if ((isDamageBlocked()) && (attacker != null) && (attacker != this))
    {
      attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
      return;
    }

    if ((attacker != null) && (attacker.isPlayer()) && (Math.abs(attacker.getLevel() - getLevel()) > 10))
    {
      if ((attacker.getKarma() > 0) && (getEffectList().getEffectsBySkillId(5182) != null) && (!isInZone(Zone.ZoneType.SIEGE))) {
        return;
      }
      if ((getKarma() > 0) && (attacker.getEffectList().getEffectsBySkillId(5182) != null) && (!attacker.isInZone(Zone.ZoneType.SIEGE))) {
        return;
      }
    }
    i = _currentMp - i;

    if (i < 0.0D) {
      i = 0.0D;
    }
    setCurrentMp(i);

    if ((attacker != null) && (attacker != this))
      startAttackStanceTask();
  }

  public double relativeSpeed(GameObject target)
  {
    return getMoveSpeed() - target.getMoveSpeed() * Math.cos(headingToRadians(getHeading()) - headingToRadians(target.getHeading()));
  }

  public void removeAllSkills()
  {
    for (Skill s : getAllSkillsArray())
      removeSkill(s);
  }

  public void removeBlockStats(List<Stats> stats)
  {
    if (_blockedStats != null)
    {
      _blockedStats.removeAll(stats);
      if (_blockedStats.isEmpty())
        _blockedStats = null;
    }
  }

  public Skill removeSkill(Skill skill)
  {
    if (skill == null)
      return null;
    return removeSkillById(Integer.valueOf(skill.getId()));
  }

  public Skill removeSkillById(Integer id)
  {
    Skill oldSkill = (Skill)_skills.remove(id);

    if (oldSkill != null)
    {
      removeTriggers(oldSkill);
      removeStatsOwner(oldSkill);
      if ((Config.ALT_DELETE_SA_BUFFS) && ((oldSkill.isItemSkill()) || (oldSkill.isHandler())))
      {
        List effects = getEffectList().getEffectsBySkill(oldSkill);
        if (effects != null) {
          for (Effect effect : effects)
            effect.exit();
        }
        Summon pet = getPet();
        if (pet != null)
        {
          effects = pet.getEffectList().getEffectsBySkill(oldSkill);
          if (effects != null) {
            for (Effect effect : effects)
              effect.exit();
          }
        }
      }
    }
    return oldSkill;
  }

  public void addTriggers(StatTemplate f)
  {
    if (f.getTriggerList().isEmpty()) {
      return;
    }
    for (TriggerInfo t : f.getTriggerList())
    {
      addTrigger(t);
    }
  }

  public void addTrigger(TriggerInfo t)
  {
    if (_triggers == null) {
      _triggers = new ConcurrentHashMap();
    }
    Set hs = (Set)_triggers.get(t.getType());
    if (hs == null)
    {
      hs = new CopyOnWriteArraySet();
      _triggers.put(t.getType(), hs);
    }

    hs.add(t);

    if (t.getType() == TriggerType.ADD)
      useTriggerSkill(this, null, t, null, 0.0D);
  }

  public void removeTriggers(StatTemplate f)
  {
    if ((_triggers == null) || (f.getTriggerList().isEmpty())) {
      return;
    }
    for (TriggerInfo t : f.getTriggerList())
      removeTrigger(t);
  }

  public void removeTrigger(TriggerInfo t)
  {
    if (_triggers == null)
      return;
    Set hs = (Set)_triggers.get(t.getType());
    if (hs == null)
      return;
    hs.remove(t);
  }

  public void sendActionFailed()
  {
    sendPacket(ActionFail.STATIC);
  }

  public boolean hasAI()
  {
    return _ai != null;
  }

  public CharacterAI getAI()
  {
    if (_ai == null) {
      synchronized (this)
      {
        if (_ai == null)
          _ai = new CharacterAI(this);
      }
    }
    return _ai;
  }

  public void setAI(CharacterAI newAI)
  {
    if (newAI == null) {
      return;
    }
    CharacterAI oldAI = _ai;

    synchronized (this)
    {
      _ai = newAI;
    }

    if (oldAI != null)
    {
      if (oldAI.isActive())
      {
        oldAI.stopAITask();
        newAI.startAITask();
        newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      }
    }
  }

  public final void setCurrentHp(double newHp, boolean canRessurect, boolean sendInfo)
  {
    int maxHp = getMaxHp();

    newHp = Math.min(maxHp, Math.max(0.0D, newHp));

    if (_currentHp == newHp) {
      return;
    }
    if ((newHp >= 0.5D) && (isDead()) && (!canRessurect)) {
      return;
    }
    double hpStart = _currentHp;

    _currentHp = newHp;

    if (isDead.compareAndSet(true, false)) {
      onRevive();
    }
    checkHpMessages(hpStart, _currentHp);

    if (sendInfo)
    {
      broadcastStatusUpdate();
      sendChanges();
    }

    if (_currentHp < maxHp)
      startRegeneration();
  }

  public final void setCurrentHp(double newHp, boolean canRessurect)
  {
    setCurrentHp(newHp, canRessurect, true);
  }

  public final void setCurrentMp(double newMp, boolean sendInfo)
  {
    int maxMp = getMaxMp();

    newMp = Math.min(maxMp, Math.max(0.0D, newMp));

    if (_currentMp == newMp) {
      return;
    }
    if ((newMp >= 0.5D) && (isDead())) {
      return;
    }
    _currentMp = newMp;

    if (sendInfo)
    {
      broadcastStatusUpdate();
      sendChanges();
    }

    if (_currentMp < maxMp)
      startRegeneration();
  }

  public final void setCurrentMp(double newMp)
  {
    setCurrentMp(newMp, true);
  }

  public final void setCurrentCp(double newCp, boolean sendInfo)
  {
    if (!isPlayer()) {
      return;
    }
    int maxCp = getMaxCp();
    newCp = Math.min(maxCp, Math.max(0.0D, newCp));

    if (_currentCp == newCp) {
      return;
    }
    if ((newCp >= 0.5D) && (isDead())) {
      return;
    }
    _currentCp = newCp;

    if (sendInfo)
    {
      broadcastStatusUpdate();
      sendChanges();
    }

    if (_currentCp < maxCp)
      startRegeneration();
  }

  public final void setCurrentCp(double newCp)
  {
    setCurrentCp(newCp, true);
  }

  public void setCurrentHpMp(double newHp, double newMp, boolean canRessurect)
  {
    int maxHp = getMaxHp();
    int maxMp = getMaxMp();

    newHp = Math.min(maxHp, Math.max(0.0D, newHp));
    newMp = Math.min(maxMp, Math.max(0.0D, newMp));

    if ((_currentHp == newHp) && (_currentMp == newMp)) {
      return;
    }
    if ((newHp >= 0.5D) && (isDead()) && (!canRessurect)) {
      return;
    }
    double hpStart = _currentHp;

    _currentHp = newHp;
    _currentMp = newMp;

    if (isDead.compareAndSet(true, false)) {
      onRevive();
    }
    checkHpMessages(hpStart, _currentHp);

    broadcastStatusUpdate();
    sendChanges();

    if ((_currentHp < maxHp) || (_currentMp < maxMp))
      startRegeneration();
  }

  public void setCurrentHpMp(double newHp, double newMp)
  {
    setCurrentHpMp(newHp, newMp, false);
  }

  public final void setFlying(boolean mode)
  {
    _flying = mode;
  }

  public final int getHeading()
  {
    return _heading;
  }

  public void setHeading(int heading)
  {
    _heading = heading;
  }

  public final void setIsTeleporting(boolean value)
  {
    isTeleporting.compareAndSet(!value, value);
  }

  public final void setName(String name)
  {
    _name = name;
  }

  public Creature getCastingTarget()
  {
    return (Creature)castingTarget.get();
  }

  public void setCastingTarget(Creature target)
  {
    if (target == null)
      castingTarget = HardReferences.emptyRef();
    else
      castingTarget = target.getRef();
  }

  public final void setRunning()
  {
    if (!_running)
    {
      _running = true;
      broadcastPacket(new L2GameServerPacket[] { new ChangeMoveType(this) });
    }
  }

  public void setSkillMastery(Integer skill, int mastery)
  {
    if (_skillMastery == null)
      _skillMastery = new HashMap();
    _skillMastery.put(skill, Integer.valueOf(mastery));
  }

  public void setAggressionTarget(Creature target)
  {
    if (target == null)
      _aggressionTarget = HardReferences.emptyRef();
    else
      _aggressionTarget = target.getRef();
  }

  public Creature getAggressionTarget()
  {
    return (Creature)_aggressionTarget.get();
  }

  public void setTarget(GameObject object)
  {
    if ((object != null) && (!object.isVisible())) {
      object = null;
    }

    if (object == null)
      target = HardReferences.emptyRef();
    else
      target = object.getRef();
  }

  public void setTitle(String title)
  {
    _title = title;
  }

  public void setWalking()
  {
    if (_running)
    {
      _running = false;
      broadcastPacket(new L2GameServerPacket[] { new ChangeMoveType(this) });
    }
  }

  public void startAbnormalEffect(AbnormalEffect ae)
  {
    if (ae == AbnormalEffect.NULL)
    {
      _abnormalEffects = AbnormalEffect.NULL.getMask();
      _abnormalEffects2 = AbnormalEffect.NULL.getMask();
      _abnormalEffects3 = AbnormalEffect.NULL.getMask();
    }
    else if (ae.isSpecial()) {
      _abnormalEffects2 |= ae.getMask();
    } else if (ae.isEvent()) {
      _abnormalEffects3 |= ae.getMask();
    } else {
      _abnormalEffects |= ae.getMask();
    }sendChanges();
  }

  public void startAttackStanceTask()
  {
    startAttackStanceTask0();
  }

  protected void startAttackStanceTask0()
  {
    if (isInCombat())
    {
      _stanceEndTime = (System.currentTimeMillis() + 15000L);
      return;
    }

    _stanceEndTime = (System.currentTimeMillis() + 15000L);

    broadcastPacket(new L2GameServerPacket[] { new AutoAttackStart(getObjectId()) });

    Future task = _stanceTask;
    if (task != null) {
      task.cancel(false);
    }

    _stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? (this._stanceTaskRunnable = new AttackStanceTask(null)) : _stanceTaskRunnable, 1000L, 1000L);
  }

  public void stopAttackStanceTask()
  {
    _stanceEndTime = 0L;

    Future task = _stanceTask;
    if (task != null)
    {
      task.cancel(false);
      _stanceTask = null;

      broadcastPacket(new L2GameServerPacket[] { new AutoAttackStop(getObjectId()) });
    }
  }

  protected void stopRegeneration()
  {
    regenLock.lock();
    try
    {
      if (_isRegenerating)
      {
        _isRegenerating = false;

        if (_regenTask != null)
        {
          _regenTask.cancel(false);
          _regenTask = null;
        }
      }
    }
    finally
    {
      regenLock.unlock();
    }
  }

  protected void startRegeneration()
  {
    if ((!isVisible()) || (isDead()) || (getRegenTick() == 0L)) {
      return;
    }
    if (_isRegenerating) {
      return;
    }
    regenLock.lock();
    try
    {
      if (!_isRegenerating)
      {
        _isRegenerating = true;
        _regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? (this._regenTaskRunnable = new RegenTask(null)) : _regenTaskRunnable, 0L, getRegenTick());
      }
    }
    finally
    {
      regenLock.unlock();
    }
  }

  public long getRegenTick()
  {
    return 3000L;
  }

  public void stopAbnormalEffect(AbnormalEffect ae)
  {
    if (ae.isSpecial())
      _abnormalEffects2 &= (ae.getMask() ^ 0xFFFFFFFF);
    if (ae.isEvent())
      _abnormalEffects3 &= (ae.getMask() ^ 0xFFFFFFFF);
    else
      _abnormalEffects &= (ae.getMask() ^ 0xFFFFFFFF);
    sendChanges();
  }

  public void block()
  {
    _blocked = true;
  }

  public void unblock()
  {
    _blocked = false;
  }

  public boolean startConfused()
  {
    return _confused.getAndSet(true);
  }

  public boolean stopConfused()
  {
    return _confused.setAndGet(false);
  }

  public boolean startFear()
  {
    return _afraid.getAndSet(true);
  }

  public boolean stopFear()
  {
    return _afraid.setAndGet(false);
  }

  public boolean startMuted()
  {
    return _muted.getAndSet(true);
  }

  public boolean stopMuted()
  {
    return _muted.setAndGet(false);
  }

  public boolean startPMuted()
  {
    return _pmuted.getAndSet(true);
  }

  public boolean stopPMuted()
  {
    return _pmuted.setAndGet(false);
  }

  public boolean startAMuted()
  {
    return _amuted.getAndSet(true);
  }

  public boolean stopAMuted()
  {
    return _amuted.setAndGet(false);
  }

  public boolean startRooted()
  {
    return _rooted.getAndSet(true);
  }

  public boolean stopRooted()
  {
    return _rooted.setAndGet(false);
  }

  public boolean startSleeping()
  {
    return _sleeping.getAndSet(true);
  }

  public boolean stopSleeping()
  {
    return _sleeping.setAndGet(false);
  }

  public boolean startStunning()
  {
    return _stunned.getAndSet(true);
  }

  public boolean stopStunning()
  {
    return _stunned.setAndGet(false);
  }

  public boolean startParalyzed()
  {
    return _paralyzed.getAndSet(true);
  }

  public boolean stopParalyzed()
  {
    return _paralyzed.setAndGet(false);
  }

  public boolean startImmobilized()
  {
    return _immobilized.getAndSet(true);
  }

  public boolean stopImmobilized()
  {
    return _immobilized.setAndGet(false);
  }

  public boolean startHealBlocked()
  {
    return _healBlocked.getAndSet(true);
  }

  public boolean stopHealBlocked()
  {
    return _healBlocked.setAndGet(false);
  }

  public boolean startDamageBlocked()
  {
    return _damageBlocked.getAndSet(true);
  }

  public boolean stopDamageBlocked()
  {
    return _damageBlocked.setAndGet(false);
  }

  public boolean startBuffImmunity()
  {
    return _buffImmunity.getAndSet(true);
  }

  public boolean stopBuffImmunity()
  {
    return _buffImmunity.setAndGet(false);
  }

  public boolean startDebuffImmunity()
  {
    return _debuffImmunity.getAndSet(true);
  }

  public boolean stopDebuffImmunity()
  {
    return _debuffImmunity.setAndGet(false);
  }

  public boolean startEffectImmunity()
  {
    return _effectImmunity.getAndSet(true);
  }

  public boolean stopEffectImmunity()
  {
    return _effectImmunity.setAndGet(false);
  }

  public boolean startWeaponEquipBlocked()
  {
    return _weaponEquipBlocked.getAndSet(true);
  }

  public boolean stopWeaponEquipBlocked()
  {
    return _weaponEquipBlocked.getAndSet(false);
  }

  public boolean startFrozen()
  {
    return _frozen.getAndSet(true);
  }

  public boolean stopFrozen()
  {
    return _frozen.setAndGet(false);
  }

  public void setFakeDeath(boolean value)
  {
    _fakeDeath = value;
  }

  public void breakFakeDeath()
  {
    getEffectList().stopAllSkillEffects(EffectType.FakeDeath);
  }

  public void setMeditated(boolean value)
  {
    _meditated = value;
  }

  public final void setIsBlessedByNoblesse(boolean value)
  {
    _isBlessedByNoblesse = value;
  }

  public final void setIsSalvation(boolean value)
  {
    _isSalvation = value;
  }

  public void setIsInvul(boolean value)
  {
    _isInvul = value;
  }

  public void setLockedTarget(boolean value)
  {
    _lockedTarget = value;
  }

  public boolean isConfused()
  {
    return _confused.get();
  }

  public boolean isFakeDeath()
  {
    return _fakeDeath;
  }

  public boolean isAfraid()
  {
    return _afraid.get();
  }

  public boolean isBlocked()
  {
    return _blocked;
  }

  public boolean isMuted(Skill skill)
  {
    if ((skill == null) || (skill.isNotAffectedByMute()))
      return false;
    return ((isMMuted()) && (skill.isMagic())) || ((isPMuted()) && (!skill.isMagic()));
  }

  public boolean isPMuted()
  {
    return _pmuted.get();
  }

  public boolean isMMuted()
  {
    return _muted.get();
  }

  public boolean isAMuted()
  {
    return _amuted.get();
  }

  public boolean isRooted()
  {
    return _rooted.get();
  }

  public boolean isSleeping()
  {
    return _sleeping.get();
  }

  public boolean isStunned()
  {
    return _stunned.get();
  }

  public boolean isMeditated()
  {
    return _meditated;
  }

  public boolean isWeaponEquipBlocked()
  {
    return _weaponEquipBlocked.get();
  }

  public boolean isParalyzed()
  {
    return _paralyzed.get();
  }

  public boolean isFrozen()
  {
    return _frozen.get();
  }

  public boolean isImmobilized()
  {
    return (_immobilized.get()) || (getRunSpeed() < 1);
  }

  public boolean isHealBlocked()
  {
    return (isAlikeDead()) || (_healBlocked.get());
  }

  public boolean isDamageBlocked()
  {
    return (isInvul()) || (_damageBlocked.get());
  }

  public boolean isCastingNow()
  {
    return _skillTask != null;
  }

  public boolean isLockedTarget()
  {
    return _lockedTarget;
  }

  public boolean isMovementDisabled()
  {
    return (isBlocked()) || (isRooted()) || (isImmobilized()) || (isAlikeDead()) || (isStunned()) || (isSleeping()) || (isParalyzed()) || (isAttackingNow()) || (isCastingNow()) || (isFrozen());
  }

  public boolean isActionsDisabled()
  {
    return (isBlocked()) || (isAlikeDead()) || (isStunned()) || (isSleeping()) || (isParalyzed()) || (isAttackingNow()) || (isCastingNow()) || (isFrozen());
  }

  public final boolean isAttackingDisabled()
  {
    return _attackReuseEndTime > System.currentTimeMillis();
  }

  public boolean isOutOfControl()
  {
    return (isBlocked()) || (isConfused()) || (isAfraid());
  }

  public void teleToLocation(Location loc)
  {
    teleToLocation(loc.x, loc.y, loc.z, getReflection());
  }

  public void teleToLocation(Location loc, int refId)
  {
    teleToLocation(loc.x, loc.y, loc.z, refId);
  }

  public void teleToLocation(Location loc, Reflection r)
  {
    teleToLocation(loc.x, loc.y, loc.z, r);
  }

  public void teleToLocation(int x, int y, int z)
  {
    teleToLocation(x, y, z, getReflection());
  }

  public void checkAndRemoveInvisible()
  {
    InvisibleType invisibleType = getInvisibleType();
    if (invisibleType == InvisibleType.EFFECT)
      getEffectList().stopEffects(EffectType.Invisible);
  }

  public void teleToLocation(int x, int y, int z, int refId)
  {
    Reflection r = ReflectionManager.getInstance().get(refId);
    if (r == null)
      return;
    teleToLocation(x, y, z, r);
  }

  public void teleToLocation(int x, int y, int z, Reflection r)
  {
    if (!isTeleporting.compareAndSet(false, true)) {
      return;
    }
    if (isFakeDeath()) {
      breakFakeDeath();
    }
    abortCast(true, false);
    if (!isLockedTarget())
      setTarget(null);
    stopMove();

    if ((!isBoat()) && (!isFlying()) && (!World.isWater(new Location(x, y, z), r))) {
      z = GeoEngine.getHeight(x, y, z, r.getGeoIndex());
    }

    if ((isPlayer()) && (DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true)))
    {
      Player player = (Player)this;
      if ((player.isInParty()) && (player.getParty().isInDimensionalRift()))
      {
        Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
        x = newCoords.x;
        y = newCoords.y;
        z = newCoords.z;
        player.getParty().getDimensionalRift().usedTeleport(player);
      }
    }

    if (isPlayer())
    {
      Player player = (Player)this;

      player.getListeners().onTeleport(x, y, z, r);

      decayMe();

      setXYZ(x, y, z);

      setReflection(r);

      player.setLastClientPosition(null);
      player.setLastServerPosition(null);

      player.sendPacket(new TeleportToLocation(player, x, y, z));
    }
    else
    {
      setXYZ(x, y, z);

      setReflection(r);

      broadcastPacket(new L2GameServerPacket[] { new TeleportToLocation(this, x, y, z) });
      onTeleported();
    }
  }

  public boolean onTeleported()
  {
    return isTeleporting.compareAndSet(true, false);
  }

  public void sendMessage(CustomMessage message)
  {
  }

  public String toString()
  {
    return getClass().getSimpleName() + "[" + getObjectId() + "]";
  }

  public double getColRadius()
  {
    return getTemplate().collisionRadius;
  }

  public double getColHeight()
  {
    return getTemplate().collisionHeight;
  }

  public EffectList getEffectList()
  {
    if (_effectList == null) {
      synchronized (this)
      {
        if (_effectList == null)
          _effectList = new EffectList(this);
      }
    }
    return _effectList;
  }

  public boolean paralizeOnAttack(Creature attacker)
  {
    int max_attacker_level = 65535;
    MonsterInstance leader;
    if ((isRaid()) || ((isMinion()) && ((leader = ((MinionInstance)this).getLeader()) != null) && (leader.isRaid()))) {
      max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
    } else if (isNpc())
    {
      int max_level_diff = ((NpcInstance)this).getParameter("ParalizeOnAttack", -1000);
      if (max_level_diff != -1000) {
        max_attacker_level = getLevel() + max_level_diff;
      }
    }

    return attacker.getLevel() > max_attacker_level;
  }

  protected void onDelete()
  {
    GameObjectsStorage.remove(_storedId.longValue());

    getEffectList().stopAllEffects();

    super.onDelete();
  }

  public void addExpAndSp(long exp, long sp)
  {
  }

  public void broadcastCharInfo()
  {
  }

  public void checkHpMessages(double currentHp, double newHp)
  {
  }

  public boolean checkPvP(Creature target, Skill skill) {
    return false;
  }

  public boolean consumeItem(int itemConsumeId, long itemCount)
  {
    return true;
  }

  public boolean consumeItemMp(int itemId, int mp)
  {
    return true;
  }

  public boolean isFearImmune()
  {
    return false;
  }

  public boolean isLethalImmune()
  {
    return getMaxHp() >= 50000;
  }

  public boolean getChargedSoulShot()
  {
    return false;
  }

  public int getChargedSpiritShot()
  {
    return 0;
  }

  public int getIncreasedForce()
  {
    return 0;
  }

  public int getConsumedSouls()
  {
    return 0;
  }

  public int getAgathionEnergy()
  {
    return 0;
  }

  public void setAgathionEnergy(int val)
  {
  }

  public int getKarma()
  {
    return 0;
  }

  public double getLevelMod()
  {
    return 1.0D;
  }

  public int getNpcId()
  {
    return 0;
  }

  public Summon getPet()
  {
    return null;
  }

  public int getPvpFlag()
  {
    return 0;
  }

  public void setTeam(TeamType t)
  {
    _team = t;
    sendChanges();
  }

  public TeamType getTeam()
  {
    return _team;
  }

  public boolean isUndead()
  {
    return false;
  }

  public boolean isParalyzeImmune()
  {
    return false;
  }

  public void reduceArrowCount()
  {
  }

  public void sendChanges() {
    getStatsRecorder().sendChanges();
  }

  public void sendMessage(String message)
  {
  }

  public void sendPacket(IStaticPacket mov) {
  }

  public void sendPacket(IStaticPacket[] mov) {
  }

  public void sendPacket(List<? extends IStaticPacket> mov) {
  }

  public void setIncreasedForce(int i) {
  }

  public void setConsumedSouls(int i, NpcInstance monster) {
  }

  public void startPvPFlag(Creature target) {
  }

  public boolean unChargeShots(boolean spirit) {
    return false;
  }

  public void updateEffectIcons()
  {
  }

  protected void refreshHpMpCp()
  {
    int maxHp = getMaxHp();
    int maxMp = getMaxMp();
    int maxCp = isPlayer() ? getMaxCp() : 0;

    if (_currentHp > maxHp)
      setCurrentHp(maxHp, false);
    if (_currentMp > maxMp)
      setCurrentMp(maxMp, false);
    if (_currentCp > maxCp) {
      setCurrentCp(maxCp, false);
    }
    if ((_currentHp < maxHp) || (_currentMp < maxMp) || (_currentCp < maxCp))
      startRegeneration();
  }

  public void updateStats()
  {
    refreshHpMpCp();
    sendChanges();
  }

  public void setOverhitAttacker(Creature attacker)
  {
  }

  public void setOverhitDamage(double damage) {
  }

  public boolean isCursedWeaponEquipped() {
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

  public Clan getClan()
  {
    return null;
  }

  public double getRateAdena()
  {
    return 1.0D;
  }

  public double getRateItems()
  {
    return 1.0D;
  }

  public double getRateExp()
  {
    return 1.0D;
  }

  public double getRateSp()
  {
    return 1.0D;
  }

  public double getRateSpoil()
  {
    return 1.0D;
  }

  public int getFormId()
  {
    return 0;
  }

  public boolean isNameAbove()
  {
    return true;
  }

  public void setLoc(Location loc)
  {
    setXYZ(loc.x, loc.y, loc.z);
  }

  public void setLoc(Location loc, boolean MoveTask)
  {
    setXYZ(loc.x, loc.y, loc.z, MoveTask);
  }

  public void setXYZ(int x, int y, int z)
  {
    setXYZ(x, y, z, false);
  }

  public void setXYZ(int x, int y, int z, boolean MoveTask)
  {
    if (!MoveTask) {
      stopMove();
    }
    moveLock.lock();
    try
    {
      super.setXYZ(x, y, z);
    }
    finally
    {
      moveLock.unlock();
    }

    updateZones();
  }

  protected void onSpawn()
  {
    super.onSpawn();

    updateStats();
    updateZones();
  }

  public void spawnMe(Location loc)
  {
    if (loc.h > 0)
      setHeading(loc.h);
    super.spawnMe(loc);
  }

  protected void onDespawn()
  {
    if (!isLockedTarget())
      setTarget(null);
    stopMove();
    stopAttackStanceTask();
    stopRegeneration();

    updateZones();
    clearStatusListeners();

    super.onDespawn();
  }

  public final void doDecay()
  {
    if (!isDead()) {
      return;
    }
    onDecay();
  }

  protected void onDecay()
  {
    decayMe();
  }

  public void validateLocation(int broadcast)
  {
    L2GameServerPacket sp = new ValidateLocation(this);
    if (broadcast == 0)
      sendPacket(sp);
    else if (broadcast == 1)
      broadcastPacket(new L2GameServerPacket[] { sp });
    else
      broadcastPacketToOthers(new L2GameServerPacket[] { sp });
  }

  public void addUnActiveSkill(Skill skill)
  {
    if ((skill == null) || (isUnActiveSkill(skill.getId()))) {
      return;
    }
    removeStatsOwner(skill);
    removeTriggers(skill);

    _unActiveSkills.add(skill.getId());
  }

  public void removeUnActiveSkill(Skill skill)
  {
    if ((skill == null) || (!isUnActiveSkill(skill.getId()))) {
      return;
    }
    addStatFuncs(skill.getStatFuncs());
    addTriggers(skill);

    _unActiveSkills.remove(skill.getId());
  }

  public boolean isUnActiveSkill(int id)
  {
    return _unActiveSkills.contains(id); } 
  public abstract int getLevel();

  public abstract ItemInstance getActiveWeaponInstance();

  public abstract WeaponTemplate getActiveWeaponItem();

  public abstract ItemInstance getSecondaryWeaponInstance();

  public abstract WeaponTemplate getSecondaryWeaponItem();

  public CharListenerList getListeners() { if (listeners == null)
      synchronized (this)
      {
        if (listeners == null)
          listeners = new CharListenerList(this);
      }
    return listeners;
  }

  public <T extends Listener<Creature>> boolean addListener(T listener)
  {
    return getListeners().add(listener);
  }

  public <T extends Listener<Creature>> boolean removeListener(T listener)
  {
    return getListeners().remove(listener);
  }

  public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
  {
    if (_statsRecorder == null) {
      synchronized (this)
      {
        if (_statsRecorder == null)
          _statsRecorder = new CharStatsChangeRecorder(this);
      }
    }
    return _statsRecorder;
  }

  public boolean isCreature()
  {
    return true;
  }

  public void displayGiveDamageMessage(Creature target, int damage, boolean crit, boolean miss, boolean shld, boolean magic)
  {
    if ((miss) && (target.isPlayer()) && (!target.isDamageBlocked()))
      target.sendPacket(new SystemMessage(2264).addName(target).addName(this));
  }

  public void displayReceiveDamageMessage(Creature attacker, int damage)
  {
  }

  public Collection<TimeStamp> getSkillReuses()
  {
    return _skillReuses.values();
  }

  public TimeStamp getSkillReuse(Skill skill)
  {
    return (TimeStamp)_skillReuses.get(skill.hashCode());
  }

  private class RegenTask
    implements Runnable
  {
    private RegenTask()
    {
    }

    public void run()
    {
      if ((isAlikeDead()) || (getRegenTick() == 0L)) {
        return;
      }
      double hpStart = _currentHp;

      int maxHp = getMaxHp();
      int maxMp = getMaxMp();
      int maxCp = isPlayer() ? getMaxCp() : 0;

      double addHp = 0.0D;
      double addMp = 0.0D;

      regenLock.lock();
      try
      {
        if (_currentHp < maxHp) {
          addHp += Formulas.calcHpRegen(Creature.this);
        }
        if (_currentMp < maxMp) {
          addMp += Formulas.calcMpRegen(Creature.this);
        }

        if ((isPlayer()) && (Config.REGEN_SIT_WAIT))
        {
          Player pl = (Player)Creature.this;
          if (pl.isSitting())
          {
            pl.updateWaitSitTime();
            if (pl.getWaitSitTime() > 5)
            {
              addHp += pl.getWaitSitTime();
              addMp += pl.getWaitSitTime();
            }
          }
        }
        else if (isRaid())
        {
          addHp *= Config.RATE_RAID_REGEN;
          addMp *= Config.RATE_RAID_REGEN;
        }

        _currentHp += Math.max(0.0D, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * maxHp / 100.0D - _currentHp));
        _currentMp += Math.max(0.0D, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * maxMp / 100.0D - _currentMp));

        _currentHp = Math.min(maxHp, _currentHp);
        _currentMp = Math.min(maxMp, _currentMp);

        if (isPlayer())
        {
          _currentCp += Math.max(0.0D, Math.min(Formulas.calcCpRegen(Creature.this), calcStat(Stats.CP_LIMIT, null, null) * maxCp / 100.0D - _currentCp));
          _currentCp = Math.min(maxCp, _currentCp);
        }

        if ((_currentHp == maxHp) && (_currentMp == maxMp) && (_currentCp == maxCp))
          stopRegeneration();
      }
      finally
      {
        regenLock.unlock();
      }

      broadcastStatusUpdate();
      sendChanges();

      checkHpMessages(hpStart, _currentHp);
    }
  }

  private class AttackStanceTask extends RunnableImpl
  {
    private AttackStanceTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      if (!isInCombat())
        stopAttackStanceTask();
    }
  }

  public class MoveNextTask extends RunnableImpl
  {
    private double alldist;
    private double donedist;

    public MoveNextTask()
    {
    }

    public MoveNextTask setDist(double dist)
    {
      alldist = dist;
      donedist = 0.0D;
      return this;
    }

    public void runImpl()
      throws Exception
    {
      if (!isMoving) {
        return;
      }
      moveLock.lock();
      try
      {
        if (!isMoving)
          return;
        if (isMovementDisabled()) {
          stopMove();
          return;
        }
        Creature follow = null;
        int speed = getMoveSpeed();
        if (speed <= 0) {
          stopMove();
          return;
        }long now = System.currentTimeMillis();

        if (isFollow)
        {
          follow = getFollowTarget();
          if (follow == null) {
            stopMove();
            return;
          }if ((isInRangeZ(follow, _offset)) && (GeoEngine.canSeeTarget(Creature.this, follow, false))) {
            stopMove();
            ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(Creature.this, CtrlEvent.EVT_ARRIVED_TARGET));
            return;
          }
        }
        if (alldist <= 0.0D) {
          Creature.this.moveNext(false);
          return;
        }
        donedist += (now - _startMoveTime) * _previousSpeed / 1000.0D;
        double done = donedist / alldist;

        if (done < 0.0D)
          done = 0.0D;
        if (done >= 1.0D) {
          Creature.this.moveNext(false);
          return;
        }
        if (isMovementDisabled()) {
          stopMove();
          return;
        }
        Location loc = null;

        int index = (int)(moveList.size() * done);
        if (index >= moveList.size())
          index = moveList.size() - 1;
        if (index < 0) {
          index = 0;
        }
        loc = ((Location)moveList.get(index)).clone().geo2world();

        if ((!isFlying()) && (!isInBoat()) && (!isInWater()) && (!isBoat()) && 
          (loc.z - getZ() > 256)) {
          String bug_text = "geo bug 1 at: " + getLoc() + " => " + loc.x + "," + loc.y + "," + loc.z + "\tAll path: " + moveList.get(0) + " => " + moveList.get(moveList.size() - 1);
          Log.add(bug_text, "geo");
          stopMove();
          return;
        }if ((loc == null) || (isMovementDisabled())) {
          stopMove();
          return;
        }
        setLoc(loc, true);

        if (isMovementDisabled()) {
          stopMove();
          return;
        }
        if (isFollow) if ((now - _followTimestamp > (_forestalling ? 500 : 1000)) && (follow != null) && (!follow.isInRange(movingDestTempPos, Math.max(100, _offset))))
          {
            if ((Math.abs(getZ() - loc.z) > 1000) && (!isFlying())) { sendPacket(SystemMsg.CANNOT_SEE_TARGET);
              stopMove();
              return; }
            if (Creature.this.buildPathTo(follow.getX(), follow.getY(), follow.getZ(), _offset, follow, true, true)) {
              movingDestTempPos.set(follow.getX(), follow.getY(), follow.getZ());
            } else { stopMove();
              return; }
            Creature.this.moveNext(true);
            return;
          }
        Creature.access$402(Creature.this, speed);
        Creature.access$302(Creature.this, now);
        Creature.access$1002(Creature.this, ThreadPoolManager.getInstance().schedule(this, getMoveTickInterval()));
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
      finally
      {
        moveLock.unlock();
      }
    }
  }
}