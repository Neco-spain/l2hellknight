package net.sf.l2j.gameserver.model;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.actor.status.CharStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Attack;
import net.sf.l2j.gameserver.network.serverpackets.ChangeMoveType;
import net.sf.l2j.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.network.serverpackets.CharMoveToLocation;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.network.serverpackets.ServerObjectInfo;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TargetUnselected;
import net.sf.l2j.gameserver.network.serverpackets.TeleportToLocation;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.PathFinding;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.gameserver.util.PeaceZone;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;
import scripts.skills.SkillHandler;

public abstract class L2Character extends L2Object
{
  protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
  private List<L2Character> _attackByList;
  private L2Skill _lastSkillCast;
  private boolean _isAfraid = false;
  private boolean _isConfused = false;
  private boolean _isFakeDeath = false;
  private boolean _isFlying = false;
  private boolean _isMuted = false;
  private boolean _isPsychicalMuted = false;
  private boolean _isKilledAlready = false;
  private boolean _isImobilised = false;
  private boolean _isOverloaded = false;
  private boolean _isParalyzed = false;
  private boolean _isRiding = false;
  private boolean _isPendingRevive = false;
  private boolean _isRooted = false;
  private boolean _isRunning = false;
  private boolean _isImmobileUntilAttacked = false;
  private boolean _isSleeping = false;
  private boolean _isStunned = false;
  private boolean _isBetrayed = false;
  protected boolean _showSummonAnimation = false;
  protected boolean _isTeleporting = false;
  private L2Character _lastBuffer = null;
  protected boolean _isInvul = false;
  private int _lastHealAmount = 0;
  private CharStat _stat;
  private CharStatus _status;
  private L2CharTemplate _template;
  private String _title = "";
  private String _aiClass = "default";
  private double _hpUpdateIncCheck = 0.0D;
  private double _hpUpdateDecCheck = 0.0D;
  private double _hpUpdateInterval = 0.0D;
  private boolean _champion = false;
  private Calculator[] _calculators;
  protected Map<Integer, L2Skill> _skills;
  protected ChanceSkillList _chanceSkills;
  public static final int ZONE_PVP = 1;
  public static final int ZONE_PEACE = 2;
  public static final int ZONE_SIEGE = 4;
  public static final int ZONE_MOTHERTREE = 8;
  public static final int ZONE_CLANHALL = 16;
  public static final int ZONE_UNUSED = 32;
  public static final int ZONE_NOLANDING = 64;
  public static final int ZONE_WATER = 128;
  public static final int ZONE_JAIL = 256;
  public static final int ZONE_MONSTERTRACK = 512;
  public static final int ZONE_BOSS = 1024;
  private int _currentZones = 0;
  private L2CharPosition _nextLoc;
  private long _attackReuseEndTime;
  private int _AbnormalEffects;
  private FastTable<L2Effect> _effects;
  protected Map<String, List<L2Effect>> _stackedEffects;
  private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
  private static final FastTable<L2Effect> EMPTY_EFFECTS_TABLE = new FastTable();
  public static final int ABNORMAL_EFFECT_BLEEDING = 1;
  public static final int ABNORMAL_EFFECT_POISON = 2;
  public static final int ABNORMAL_EFFECT_UNKNOWN_3 = 4;
  public static final int ABNORMAL_EFFECT_UNKNOWN_4 = 8;
  public static final int ABNORMAL_EFFECT_UNKNOWN_5 = 16;
  public static final int ABNORMAL_EFFECT_UNKNOWN_6 = 32;
  public static final int ABNORMAL_EFFECT_STUN = 64;
  public static final int ABNORMAL_EFFECT_SLEEP = 128;
  public static final int ABNORMAL_EFFECT_MUTED = 256;
  public static final int ABNORMAL_EFFECT_ROOT = 512;
  public static final int ABNORMAL_EFFECT_HOLD_1 = 1024;
  public static final int ABNORMAL_EFFECT_HOLD_2 = 2048;
  public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 4096;
  public static final int ABNORMAL_EFFECT_BIG_HEAD = 8192;
  public static final int ABNORMAL_EFFECT_FLAME = 16384;
  public static final int ABNORMAL_EFFECT_UNKNOWN_16 = 32768;
  public static final int ABNORMAL_EFFECT_GROW = 65536;
  public static final int ABNORMAL_EFFECT_FLOATING_ROOT = 131072;
  public static final int ABNORMAL_EFFECT_DANCE_STUNNED = 262144;
  public static final int ABNORMAL_EFFECT_FIREROOT_STUN = 524288;
  public static final int ABNORMAL_EFFECT_STEALTH = 1048576;
  public static final int ABNORMAL_EFFECT_IMPRISIONING_1 = 2097152;
  public static final int ABNORMAL_EFFECT_IMPRISIONING_2 = 4194304;
  public static final int ABNORMAL_EFFECT_MAGIC_CIRCLE = 8388608;
  public static final int ABNORMAL_EFFECT_CONFUSED = 32;
  public static final int ABNORMAL_EFFECT_AFRAID = 16;
  protected ConcurrentLinkedQueue<Integer> _disabledSkills;
  private boolean _allSkillsDisabled;
  protected MoveData _move;
  private int _heading;
  private WeakReference<L2Object> _target;
  private int _castEndTime;
  private int _castInterruptTime;
  private long _attackEndTime;
  private int _attacking;
  private int _disableBowAttackEndTime;
  private static final Calculator[] NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
  protected L2CharacterAI _ai;
  protected Future<?> _skillCast;
  private int _clientX;
  private int _clientY;
  private int _clientZ;
  private int _clientHeading;
  private List<QuestState> _NotifyQuestOfDeathList = new FastList();

  private long _lastTrigger = 0L;
  private Future<?> _PvPRegTask;
  private long _pvpFlagLasts;
  private boolean _vis = true;

  private int _numCharges = 0;
  public static final double HEADINGS_IN_PI = 10430.378350470453D;
  private long _lastRestore = 0L;

  private long _lastStop = 0L;

  private long _lastRebuff = 0L;

  private long _fullRebuff = 0L;

  public boolean isInsideZone(int zone)
  {
    return (_currentZones & zone) != 0;
  }

  public void setInsideZone(int zone, boolean state) {
    if (state)
      _currentZones |= zone;
    else if (isInsideZone(zone))
    {
      _currentZones ^= zone;
    }
  }

  public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
  {
    return true;
  }

  public L2Character(int objectId, L2CharTemplate template)
  {
    super(objectId);
    init(template);
  }

  private void init(L2CharTemplate template) {
    getKnownList();

    _template = template;

    if ((template != null) && ((_template instanceof L2NpcTemplate)) && (isL2Npc()))
    {
      _calculators = NPC_STD_CALCULATOR;

      _skills = template.getSkills();
      if (_skills != null) {
        for (Map.Entry skill : _skills.entrySet()) {
          addStatFuncs(((L2Skill)skill.getValue()).getStatFuncs(null, this));
        }
      }
    }
    else
    {
      _skills = new ConcurrentHashMap();

      _calculators = new Calculator[Stats.NUM_STATS];
      Formulas.addFuncsToNewCharacter(this);
    }
  }

  protected void initCharStatusUpdateValues() {
    _hpUpdateInterval = (getMaxHp() / 352.0D);
    _hpUpdateIncCheck = getMaxHp();
    _hpUpdateDecCheck = (getMaxHp() - _hpUpdateInterval);
  }

  public void onDecay()
  {
    L2WorldRegion reg = getWorldRegion();
    if (reg != null) {
      reg.removeFromZones(this);
    }
    decayMe();
  }

  public void onSpawn()
  {
    super.onSpawn();
    revalidateZone();
  }

  public void onTeleported() {
    if (!isTeleporting()) {
      return;
    }

    spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());

    setIsTeleporting(false);

    if (_isPendingRevive) {
      doRevive();
    }

    if (getPet() != null)
    {
      ((L2SummonAI)getPet().getAI()).setStartFollowController(true);
      getPet().setFollowStatus(true);
      getPet().updateAndBroadcastStatus(0);
    }
    sendActionFailed();
  }

  public void addAttackerToAttackByList(L2Character player)
  {
    if ((player == null) || (player == this) || (getAttackByList() == null) || (getAttackByList().contains(player))) {
      return;
    }
    getAttackByList().add(player);
  }

  public void broadcastPacket(L2GameServerPacket packet)
  {
    if (!packet.isCharInfo()) {
      sendPacket(packet);
    }

    Broadcast.toKnownPlayers(this, packet);
  }

  public void broadcastSoulShotsPacket(L2GameServerPacket mov) {
    if (showSoulShotsAnim()) {
      sendPacket(mov);
    }

    Broadcast.broadcastSoulShotsPacket(mov, getKnownList().getListKnownPlayers(), null);
  }

  public final void broadcastPacket(L2GameServerPacket packet, int radius)
  {
    if (!packet.isCharInfo()) {
      sendPacket(packet);
    }
    Broadcast.toKnownPlayersInRadius(this, packet, radius, true);
  }

  protected boolean needHpUpdate(int barPixels)
  {
    double currentHp = getCurrentHp();

    if ((currentHp <= 1.0D) || (getMaxHp() < barPixels)) {
      return true;
    }

    if ((currentHp <= _hpUpdateDecCheck) || (currentHp >= _hpUpdateIncCheck)) {
      if (currentHp == getMaxHp()) {
        _hpUpdateIncCheck = (currentHp + 1.0D);
        _hpUpdateDecCheck = (currentHp - _hpUpdateInterval);
      } else {
        double doubleMulti = currentHp / _hpUpdateInterval;
        int intMulti = (int)doubleMulti;

        _hpUpdateDecCheck = (_hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti));
        _hpUpdateIncCheck = (_hpUpdateDecCheck + _hpUpdateInterval);
      }
      return true;
    }
    return false;
  }

  public void broadcastStatusUpdate()
  {
    CopyOnWriteArraySet list = getStatus().getStatusListener();
    if ((list == null) || (list.isEmpty())) {
      return;
    }

    if (!needHpUpdate(352)) {
      return;
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(9, (int)getCurrentHp());
    su.addAttribute(11, (int)getCurrentMp());
    su.addAttribute(33, (int)getCurrentCp());

    L2Object target = null;
    for (L2Character temp : list) {
      if (temp == null)
      {
        continue;
      }
      target = temp.getTarget();
      if (target == null)
      {
        continue;
      }
      if (target.equals(this)) {
        temp.sendPacket(su);
      }
    }
    target = null;
  }

  public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
  {
    if (this != null) {
      if (isInJail()) {
        return;
      }

      stopMove(null, false);
      abortAttack();
      abortCast();

      setIsTeleporting(true);
      setTarget(null);

      if (getPet() != null) {
        getPet().setFollowStatus(false);
        getPet().teleToLocation(x, y, z, false);
      }

      if (getWorldRegion() != null) {
        getWorldRegion().removeFromZones(this);
      }

      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

      z += 5;

      broadcastPacket(new TeleportToLocation(this, x, y, z, true));

      getPosition().setXYZ(x, y, z);

      decayMe();

      if (!isPlayer())
        onTeleported();
    }
  }

  public void teleToLocation(int x, int y, int z)
  {
    teleToLocation(x, y, z, false);
  }

  public void teleToLocation(Location loc, boolean allowRandomOffset) {
    if (this != null) {
      int x = loc.getX();
      int y = loc.getY();
      int z = loc.getZ();

      if ((isPlayer()) && (DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)))
      {
        sendUserPacket(Static.SENT_TO_WAITING_ROOM);
        if ((isInParty()) && (getParty().isInDimensionalRift())) {
          getParty().getDimensionalRift().usedTeleport(getPlayer());
        }

        int[] newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
        x = newCoords[0];
        y = newCoords[1];
        z = newCoords[2];
      }
      teleToLocation(x, y, z, allowRandomOffset);
    }
  }

  public void teleToLocation(MapRegionTable.TeleportWhereType teleportWhere) {
    if (this != null)
      teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
  }

  public void teleToJail()
  {
    if (this != null)
    {
      stopMove(null, false);
      abortAttack();
      abortCast();

      setIsTeleporting(true);
      setTarget(null);

      if (getPet() != null) {
        getPet().setFollowStatus(false);
        getPet().unSummon(getPlayer());
      }

      if (getWorldRegion() != null) {
        getWorldRegion().removeFromZones(this);
      }

      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

      broadcastPacket(new TeleportToLocation(this, -114356, -249645, -2984, true));

      getPosition().setXYZ(-114356, -249645, -2989);

      decayMe();

      if (!isPlayer())
        onTeleported();
    }
  }

  public void setNextLoc(int x, int y, int z)
  {
    _nextLoc = new L2CharPosition(x, y, z, 0);
  }

  public void clearNextLoc() {
    _nextLoc = null;
  }

  public void checkNextLoc() {
    if (_nextLoc == null) {
      return;
    }

    getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _nextLoc);
  }

  protected void doAttack(L2Character target) {
    if (isAttackingNow())
    {
      sendActionFailed();
      return;
    }

    if ((isAlikeDead()) || (target == null) || ((isL2Npc()) && (target.isAlikeDead())) || ((isPlayer()) && (target.isDead()) && (!target.isFakeDeath())) || (!getKnownList().knowsObject(target)) || ((isPlayer()) && (isDead())) || ((target.isPlayer()) && (target.getDuel() != null) && (target.getDuel().getDuelState(target.getPlayer()) == Duel.DuelState.Dead)))
    {
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      sendActionFailed();
      return;
    }

    if ((target.isL2Door()) && (!target.isAutoAttackable(this))) {
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      sendActionFailed();
      return;
    }

    updateLastTeleport(false);

    if (isPlayer()) {
      if (inObserverMode()) {
        sendUserPacket(Static.OBSERVERS_CANNOT_PARTICIPATE);
        sendActionFailed();
        return;
      }

      if ((isHippy()) || (PeaceZone.getInstance().inPeace(this, target))) {
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        sendActionFailed();
        return;
      }

      if (target.isPlayer()) {
        if ((target.isCursedWeaponEquiped()) && (getLevel() <= 20)) {
          sendUserPacket(Static.CW_21);
          sendActionFailed();
          return;
        }

        if ((isCursedWeaponEquiped()) && (target.getLevel() <= 20)) {
          sendUserPacket(Static.CW_20);
          sendActionFailed();
          return;
        }

        if (getObjectId() == target.getObjectId()) {
          sendActionFailed();
          return;
        }
      }

    }

    L2ItemInstance weaponInst = getActiveWeaponInstance();

    L2Weapon weaponItem = getActiveWeaponItem();
    if ((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.ROD))
    {
      sendUserPacket(Static.CANNOT_ATTACK_WITH_FISHING_POLE);
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

      sendActionFailed();
      return;
    }

    if (!canSeeTarget(target)) {
      sendUserPacket(Static.CANT_SEE_TARGET);
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      sendActionFailed();
      return;
    }

    int ssGrade = 0;
    int timeAtk = Config.MIN_ATKSPD_DELAY > 0 ? Math.max(calculateTimeBetweenAttacks(target, weaponItem), Config.MIN_ATKSPD_DELAY) : calculateTimeBetweenAttacks(target, weaponItem);

    if (weaponItem != null) {
      if ((isPlayer()) && (weaponItem.getItemType() == L2WeaponType.BOW))
      {
        if (!target.isAttackable()) {
          sendActionFailed();

          return;
        }

        if (Config.FORBIDDEN_BOW_CLASSES.contains(Integer.valueOf(getClassId().getId()))) {
          sendUserPacket(Static.FORBIDDEN_BOW_CLASS);
          sendActionFailed();
          return;
        }

        int saMpConsume = (int)getStat().calcStat(Stats.MP_CONSUME, 0.0D, null, null);
        int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;
        if (getCurrentMp() < mpConsume) {
          sendUserPacket(Static.NOT_ENOUGH_MP);
          sendActionFailed();
          return;
        }
        getStatus().reduceMp(mpConsume);

        if (!checkAndEquipArrows()) {
          getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

          sendActionFailed();
          sendUserPacket(Static.NOT_ENOUGH_ARROWS);
          return;
        }

        int reuse = (int)(weaponItem.getAttackReuseDelay() * getStat().getWeaponReuseModifier(target) * 666.0D * getTemplate().basePAtkSpd / 293.0D / getPAtkSpd());
        if (reuse > 0) {
          sendUserPacket(new SetupGauge(1, reuse));
          _attackReuseEndTime = (reuse + System.currentTimeMillis() - 75L);
          if (reuse > timeAtk) {
            ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), reuse, (isPlayer()) || (isPet()) || (isSummon()));
          }
        }
      }
      ssGrade = weaponItem.getCrystalType();
    }

    target.getKnownList().addKnownObject(this);
    boolean wasSSCharged;
    boolean wasSSCharged;
    if ((isL2Summon()) && (!isPet()))
      wasSSCharged = getChargedSoulShot() != 0;
    else {
      wasSSCharged = (weaponInst != null) && (weaponInst.getChargedSoulshot() != 0);
    }

    _attackEndTime = (timeAtk + System.currentTimeMillis() - 60L);

    if (isFantome()) {
      wasSSCharged = true;
      ssGrade = 5;
    }

    Attack attack = new Attack(this, wasSSCharged, ssGrade);

    setAttackingBodypart();

    setHeading(Util.calculateHeadingFrom(this, target));
    boolean hitted;
    boolean hitted;
    if ((weaponItem == null) || (isMounted()))
      hitted = doAttackHitSimple(attack, target, timeAtk);
    else {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$templates$L2WeaponType[weaponItem.getItemType().ordinal()]) {
      case 1:
        hitted = doAttackHitByBow(attack, target, timeAtk);
        break;
      case 2:
        hitted = doAttackHitByPole(attack, timeAtk);
        break;
      case 3:
      case 4:
        hitted = doAttackHitByDual(attack, target, timeAtk);
        break;
      default:
        hitted = doAttackHitSimple(attack, target, timeAtk);
      }

    }

    if (((target.isPlayer()) || (target.isL2Summon()) || (target.isPet())) && 
      (getPet() != target)) {
      updatePvPStatus(target);
    }

    if (!hitted)
    {
      abortAttack();
    }
    else
    {
      if ((isL2Summon()) && (!isPet()))
        setChargedSoulShot(0);
      else if (weaponInst != null) {
        weaponInst.setChargedSoulshot(0);
      }

      if (isCursedWeaponEquiped())
      {
        if (!target.isInvul())
          target.setCurrentCp(0.0D);
      }
      else if ((isHero()) && 
        (target.isPlayer()) && (target.isCursedWeaponEquiped()))
      {
        target.setCurrentCp(0.0D);
      }

    }

    if (attack.hasHits())
      broadcastPacket(attack);
  }

  public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
  {
    return Formulas.calcPAtkSpd(this, target, getPAtkSpd());
  }

  public int calculateReuseTime(L2Character target, L2Weapon weapon) {
    if (weapon == null) {
      return 0;
    }

    int reuse = weapon.getAttackReuseDelay();

    if (reuse == 0) {
      return 0;
    }

    reuse = (int)(reuse * getStat().getWeaponReuseModifier(target));
    double atkSpd = getPAtkSpd();
    switch (weapon.getItemType()) {
    case BOW:
      return (int)(reuse * 345 / atkSpd);
    }
    return (int)(reuse * 312 / atkSpd);
  }

  private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk)
  {
    int damage1 = 0;
    boolean shld1 = false;
    boolean crit1 = false;

    boolean miss1 = Formulas.calcHitMiss(this, target);

    reduceArrowCount();

    _move = null;

    if (!miss1)
    {
      shld1 = Formulas.calcShldUse(this, target);

      crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));

      damage1 = (int)Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
    }

    sendUserPacket(Static.GETTING_READY_TO_SHOOT_AN_ARROW);

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk, (isPlayer()) || (isPet()) || (isSummon()));

    attack.addHit(target, damage1, miss1, crit1, shld1);

    return !miss1;
  }

  private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
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
      shld1 = Formulas.calcShldUse(this, target);

      crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));

      damage1 = (int)Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
      damage1 /= 2;
    }

    if (!miss2)
    {
      shld2 = Formulas.calcShldUse(this, target);

      crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null));

      damage2 = (int)Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
      damage2 /= 2;
    }

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2, (isPlayer()) || (isPet()) || (isSummon()));

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk, (isPlayer()) || (isPet()) || (isSummon()));

    attack.addHit(target, damage1, miss1, crit1, shld1);
    attack.addHit(target, damage2, miss2, crit2, shld2);

    return (!miss1) || (!miss2);
  }

  private boolean doAttackHitByPole(Attack attack, int sAtk)
  {
    if (getTarget() == null) {
      return false;
    }

    boolean hitted = false;

    int maxRadius = (int)getStat().calcStat(Stats.POWER_ATTACK_RANGE, 66.0D, null, null);
    int maxAngleDiff = (int)getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120.0D, null, null);

    double angleTarget = Util.calculateAngleFrom(this, getTarget());
    setHeading((int)(angleTarget / 9.0D * 1610.0D));

    double angleChar = Util.convertHeadingToDegree(getHeading());
    double attackpercent = 85.0D;
    int attackcountmax = (int)getStat().calcStat(Stats.ATTACK_COUNT_MAX, 3.0D, null, null);
    int attackcount = 0;

    if (angleChar <= 0.0D) {
      angleChar += 360.0D;
    }

    FastList objs = getKnownList().getKnownCharactersInRadius(maxRadius);
    FastList.Node n = objs.head(); for (FastList.Node end = objs.tail(); (n = n.getNext()) != end; ) {
      L2Character target = (L2Character)n.getValue();
      if ((target == null) || 
        (target.isAlikeDead()) || 
        (Math.abs(target.getZ() - getZ()) > 650) || 
        (PeaceZone.getInstance().inPeace(this, target)) || (
        (target.isPet()) && (isPlayer()) && (target.getOwner() == getPlayer())))
      {
        continue;
      }
      angleTarget = Util.calculateAngleFrom(this, target);
      if (((Math.abs(angleChar - angleTarget) > maxAngleDiff) && (Math.abs(angleChar + 360.0D - angleTarget) > maxAngleDiff) && (Math.abs(angleChar - (angleTarget + 360.0D)) > maxAngleDiff)) || (
        (isL2Guard()) && (target.isFantome())))
      {
        continue;
      }

      attackcount++;
      if ((attackcount > attackcountmax) || (
        (target != getAI().getAttackTarget()) && (!target.isAutoAttackable(this))))
        continue;
      hitted |= doAttackHitSimple(attack, target, attackpercent, sAtk);
      attackpercent /= 1.15D;
    }

    objs.clear();
    objs = null;
    L2Character target = null;

    return hitted;
  }

  private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
  {
    return doAttackHitSimple(attack, target, 100.0D, sAtk);
  }

  private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk) {
    int damage1 = 0;
    boolean shld1 = false;
    boolean crit1 = false;

    boolean miss1 = Formulas.calcHitMiss(this, target);

    if (!miss1)
    {
      shld1 = Formulas.calcShldUse(this, target);

      crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));

      damage1 = (int)Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);

      if (attackpercent != 100.0D) {
        damage1 = (int)(damage1 * attackpercent / 100.0D);
      }

    }

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk, (isPlayer()) || (isPet()) || (isSummon()));

    attack.addHit(target, damage1, miss1, crit1, shld1);

    return !miss1;
  }

  public void doCast(L2Skill skill)
  {
    if ((isHippy()) || (skill == null)) {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if (isSkillDisabled(skill.getId())) {
      sendUserPacket(SystemMessage.id(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill.getId(), skill.getLevel()));
      return;
    }

    if (isPlayer())
    {
      if (((!skill.isCubic()) && (skill.getSkillType() == L2Skill.SkillType.SUMMON) && (getPet() != null)) || (isMounted())) {
        sendUserPacket(Static.YOU_ALREADY_HAVE_A_PET);
        return;
      }

    }

    if ((skill.isMagic()) && (isMuted()) && (!skill.isPotion())) {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if ((!skill.isMagic()) && (isPsychicalMuted()) && (!skill.isPotion())) {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if ((isInOlympiadMode()) && ((skill.isHeroSkill()) || (skill.getSkillType() == L2Skill.SkillType.RESURRECT))) {
      sendUserPacket(Static.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
      return;
    }

    FastList targets = new FastList();

    if (!skill.isSignedTargetType()) {
      targets = skill.getTargetList(this);
      if ((targets == null) || (targets.isEmpty())) {
        getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
        return;
      }

    }

    L2Character target = null;
    if ((skill.isSupportSkill()) && (skill.isSupportTargetType())) {
      target = (L2Character)targets.getFirst();
      if ((isPlayer()) && (target.isPlayer()) && (target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)) {
        target.setLastBuffer(this);

        if ((isInParty()) && (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)) {
          for (L2PcInstance member : getParty().getPartyMembers()) {
            member.setLastBuffer(this);
          }
        }
      }
    }

    if (target == null) {
      target = (L2Character)getTarget();
    }

    if (skill.isAuraSignedTargetType()) {
      target = this;
    }

    if (target == null)
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if ((skill.getCastRange() > 0) && (!canSeeTarget(target))) {
      sendUserPacket(Static.CANT_SEE_TARGET);
      sendActionFailed();
      return;
    }

    setLastSkillCast(skill);

    int magicId = skill.getId();

    int hitTime = skill.getHitTime();
    int coolTime = skill.getCoolTime();

    boolean hasEffectDelay = skill.getInitialEffectDelay() > 0;

    if (!hasEffectDelay) {
      hitTime = Formulas.calcMAtkSpd(this, skill, hitTime);
      if (coolTime > 0) {
        coolTime = Formulas.calcMAtkSpd(this, skill, coolTime);
      }

    }

    L2ItemInstance weaponInst = getActiveWeaponInstance();
    boolean wasSSCharged;
    boolean wasSSCharged;
    if (isL2Summon())
      wasSSCharged = getChargedSpiritShot() != 0;
    else {
      wasSSCharged = (weaponInst != null) && (weaponInst.getChargedSpiritshot() != 0);
    }

    if ((skill.isMagic()) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) && 
      (wasSSCharged))
    {
      hitTime = (int)(0.8D * hitTime);
      coolTime = (int)(0.8D * coolTime);

      if ((skill.isUseSppritShot()) && (!skill.isPotion())) {
        if (isSummon())
          setChargedSpiritShot(0);
        else if (weaponInst != null) {
          weaponInst.setChargedSpiritshot(0);
        }

      }

    }

    _castEndTime = (10 + GameTimeController.getGameTicks() + (coolTime + hitTime) / 100);
    _castInterruptTime = (GameTimeController.getGameTicks() + hitTime / 100);
    int reuseDelay;
    int reuseDelay;
    if (skill.isFixedReuse()) {
      reuseDelay = skill.getReuseDelay();
    }
    else
    {
      int reuseDelay;
      if (skill.isMagic())
        reuseDelay = (int)(skill.getReuseDelay() * getStat().getMReuseRate(skill));
      else {
        reuseDelay = (int)(skill.getReuseDelay() * getStat().getPReuseRate(skill));
      }
      reuseDelay = (int)(reuseDelay * (333.0D / calcAtkSpd(skill.isMagic())));
    }

    boolean skillMastery = Formulas.calcSkillMastery(this);

    broadcastPacket(new MagicSkillUser(this, target, skill.getDisplayId(), Math.max(1, skill.getLevel()), hitTime + skill.getInitialEffectDelay(), reuseDelay));

    if ((isPlayer()) && (magicId != 1312)) {
      sendUserPacket(SystemMessage.id(SystemMessageId.USE_S1).addSkillName(magicId, skill.getLevel()));
    }

    int initmpcons = getStat().getMpInitialConsume(skill);
    if (initmpcons > 0)
    {
      reduceCurrentMp(calcStat(Stats.MP_CONSUME_RATE, initmpcons, null, null));
    }

    if ((reuseDelay > 10) && (!skillMastery)) {
      addTimeStamp(skill.getId(), reuseDelay);
      disableSkill(skill.getId(), reuseDelay);
    }

    if ((skillMastery) && (isPlayer()))
    {
      reuseDelay = 0;
      sendUserPacket(SystemMessage.id(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
    }

    int finalTime = hitTime + skill.getInitialEffectDelay();

    if (finalTime > 210)
    {
      int initialEffectDelay = skill.getInitialEffectDelay();

      if (hasEffectDelay)
        sendUserPacket(new SetupGauge(0, initialEffectDelay));
      else {
        sendUserPacket(new SetupGauge(0, finalTime));
      }

      disableAllSkills();

      if (_skillCast != null) {
        _skillCast.cancel(true);
        _skillCast = null;
      }

      if (hasEffectDelay)
        _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, finalTime), initialEffectDelay);
      else
        _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1, 0), finalTime - 200);
    }
    else {
      onMagicLaunchedTimer(targets, skill, coolTime, true);
    }
  }

  public void setChargedSpiritShot(int shotType)
  {
  }

  private int calcAtkSpd(boolean magic)
  {
    return magic ? Math.min(getMAtkSpd(), Config.MAX_MATKSPD_DELAY) : Math.min(getPAtkSpd(), Config.MAX_PATKSPD_DELAY);
  }

  public void reduceCurrentMp(double i, L2Character attacker)
  {
    if ((attacker != null) && (attacker != this) && 
      (isSleeping())) {
      stopEffects(L2Effect.EffectType.SLEEP);
    }

    i = getCurrentMp() - i;

    if (i < 0.0D) {
      i = 0.0D;
    }

    setCurrentMp(i);
  }

  private L2Character getActingPlayer()
  {
    return null;
  }

  public void addTimeStamp(int s, int r)
  {
  }

  public void removeTimeStamp(int s)
  {
  }

  public void startForceBuff(L2Character caster, L2Skill skill)
  {
  }

  public boolean doDie(L2Character killer)
  {
    synchronized (this) {
      if (isKilledAlready()) {
        return false;
      }
      setIsKilledAlready(true);
    }

    setTarget(null);

    stopMove(null);

    getStatus().stopHpMpRegeneration();

    if (isNoblesseBlessed()) {
      stopNoblesseBlessing(null);
      if (getCharmOfLuck())
      {
        stopCharmOfLuck(null);
      }
    }
    else if ((isL2Playable()) && (isPhoenixBlessed())) {
      if (getCharmOfLuck())
      {
        stopCharmOfLuck(null);
      }
    } else if (Config.CLEAR_BUFF_ONDEATH) {
      stopAllEffects();
    }

    calculateRewards(killer);

    broadcastStatusUpdate();

    getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);

    if (getWorldRegion() != null) {
      getWorldRegion().onDeath(this);
    }

    for (QuestState qs : getNotifyQuestOfDeath()) {
      qs.getQuest().notifyDeath(killer == null ? this : killer, this, qs);
    }
    getNotifyQuestOfDeath().clear();

    getAttackByList().clear();

    return true;
  }

  protected void calculateRewards(L2Character killer)
  {
  }

  public void doRevive() {
    if (!isTeleporting()) {
      setIsPendingRevive(false);

      _status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
      _status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
    }

    broadcastPacket(new Revive(this));
    if (getWorldRegion() != null)
      getWorldRegion().onRevive(this);
    else
      setIsPendingRevive(true);
  }

  public void doRevive(double revivePower)
  {
    doRevive();
  }

  protected void useMagic(L2Skill skill)
  {
    if ((skill == null) || (isDead())) {
      return;
    }

    if (isAllSkillsDisabled())
    {
      return;
    }

    if ((skill.isPassive()) || (skill.isChance())) {
      return;
    }

    L2Object target = null;

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[skill.getTargetType().ordinal()]) {
    case 1:
    case 2:
    case 3:
    case 4:
      target = this;
      break;
    default:
      target = skill.getFirstOfTargetList(this);
    }

    getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null) {
      _ai = new L2CharacterAI(new AIAccessor());
    }

    return _ai;
  }

  public void setAI(L2CharacterAI newAI) {
    L2CharacterAI oldAI = getAI();
    if ((oldAI != null) && (oldAI != newAI) && ((oldAI instanceof L2AttackableAI))) {
      ((L2AttackableAI)oldAI).stopAITask();
    }
    _ai = newAI;
  }

  public boolean hasAI()
  {
    return _ai != null;
  }

  public boolean isRaid()
  {
    return false;
  }

  public boolean isGrandRaid() {
    return false;
  }

  public boolean checkRange()
  {
    return (isMonster()) || (isRaid());
  }

  public boolean isDebuffProtected()
  {
    return false;
  }

  public final List<L2Character> getAttackByList()
  {
    if (_attackByList == null) {
      _attackByList = new FastList();
    }
    return _attackByList;
  }

  public final L2Skill getLastSkillCast() {
    return _lastSkillCast;
  }

  public void setLastSkillCast(L2Skill skill) {
    _lastSkillCast = skill;
  }

  public final boolean isAfraid() {
    return _isAfraid;
  }

  public final void setIsAfraid(boolean value) {
    _isAfraid = value;
  }

  public boolean isAlikeDead()
  {
    return (isFakeDeath()) || (getCurrentHp() < 0.5D);
  }

  public final boolean isAllSkillsDisabled()
  {
    return (_allSkillsDisabled) || (isImmobileUntilAttacked()) || (isStunned()) || (isSleeping()) || (isParalyzed());
  }

  public boolean isAttackingDisabled()
  {
    return (_attackReuseEndTime > System.currentTimeMillis()) || (isImmobileUntilAttacked()) || (isStunned()) || (isSleeping()) || (isFakeDeath()) || (isParalyzed());
  }

  public final Calculator[] getCalculators() {
    return _calculators;
  }

  public final boolean isConfused() {
    return _isConfused;
  }

  public final void setIsConfused(boolean value) {
    _isConfused = value;
  }

  public final boolean isDead()
  {
    return getCurrentHp() < 0.5D;
  }

  public final boolean isFakeDeath() {
    return _isFakeDeath;
  }

  public final void setIsFakeDeath(boolean value) {
    _isFakeDeath = value;
  }

  public final boolean isFlying()
  {
    return _isFlying;
  }

  public final void setIsFlying(boolean mode)
  {
    _isFlying = mode;
  }

  public boolean isImobilised()
  {
    return _isImobilised;
  }

  public void setIsImobilised(boolean value) {
    _isImobilised = value;
  }

  public final boolean isKilledAlready() {
    return _isKilledAlready;
  }

  public final void setIsKilledAlready(boolean value) {
    _isKilledAlready = value;
  }

  public final boolean isMuted() {
    return _isMuted;
  }

  public final void setIsMuted(boolean value) {
    _isMuted = value;
  }

  public final boolean isPsychicalMuted() {
    return _isPsychicalMuted;
  }

  public final void setIsPsychicalMuted(boolean value) {
    _isPsychicalMuted = value;
  }

  public boolean isMovementDisabled()
  {
    return (isTeleporting()) || (isSitting()) || (isStunned()) || (isRooted()) || (isSleeping()) || (isParalyzed()) || (isImobilised()) || (isAlikeDead()) || (isAttackingNow()) || (isCastingNow()) || (isOverloaded()) || (isFishing()) || (getRunSpeed() == 0);
  }

  public boolean isFishing() {
    return false;
  }

  public final boolean isOutOfControl()
  {
    return (isConfused()) || (isAfraid());
  }

  public final boolean isOverloaded() {
    return _isOverloaded;
  }

  public final void setIsOverloaded(boolean value)
  {
    _isOverloaded = value;
  }

  public boolean isParalyzed() {
    return _isParalyzed;
  }

  public final void setIsParalyzed(boolean value) {
    _isParalyzed = value;
  }

  public final boolean isPendingRevive() {
    return (isDead()) && (_isPendingRevive);
  }

  public final void setIsPendingRevive(boolean value) {
    _isPendingRevive = value;
  }

  public L2Summon getPet()
  {
    return null;
  }

  public final boolean isRiding()
  {
    return _isRiding;
  }

  public final void setIsRiding(boolean mode)
  {
    _isRiding = mode;
  }

  public final boolean isRooted() {
    return _isRooted;
  }

  public final void setIsRooted(boolean value) {
    _isRooted = value;
  }

  public final boolean isRunning()
  {
    return _isRunning;
  }

  public final void setIsRunning(boolean value) {
    _isRunning = value;
    if (getRunSpeed() != 0) {
      broadcastPacket(new ChangeMoveType(this));
    }
    if (isPlayer()) {
      broadcastUserInfo();
    } else if (isL2Summon()) {
      broadcastStatusUpdate();
    } else if (isL2Npc()) {
      FastList players = getKnownList().getListKnownPlayers();
      L2PcInstance pc = null;
      FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
        pc = (L2PcInstance)n.getValue();
        if (pc == null)
        {
          continue;
        }
        if (getRunSpeed() == 0) {
          pc.sendPacket(new ServerObjectInfo((L2NpcInstance)this, pc)); continue;
        }
        pc.sendPacket(new NpcInfo((L2NpcInstance)this, pc));
      }

      players.clear();
      players = null;
      pc = null;
    }
  }

  public final void setRunning()
  {
    if (!isRunning())
      setIsRunning(true);
  }

  public final boolean isImmobileUntilAttacked()
  {
    return _isImmobileUntilAttacked;
  }

  public final void setIsImmobileUntilAttacked(boolean value) {
    _isImmobileUntilAttacked = value;
  }

  public final boolean isSleeping() {
    return _isSleeping;
  }

  public final void setIsSleeping(boolean value) {
    _isSleeping = value;
  }

  public final boolean isStunned() {
    return _isStunned;
  }

  public final void setIsStunned(boolean value) {
    _isStunned = value;
  }

  public final boolean isBetrayed() {
    return _isBetrayed;
  }

  public final void setIsBetrayed(boolean value) {
    _isBetrayed = value;
  }

  public final boolean isTeleporting() {
    return _isTeleporting;
  }

  public final void setIsTeleporting(boolean value) {
    _isTeleporting = value;
  }

  public void setIsInvul(boolean b) {
    _isInvul = b;
  }

  public boolean isInvul() {
    return (_isInvul) || (_isTeleporting);
  }

  public boolean isUndead() {
    return _template.isUndead;
  }

  public CharKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof CharKnownList))) {
      setKnownList(new CharKnownList(this));
    }
    return (CharKnownList)super.getKnownList();
  }

  public CharStat getStat() {
    if (_stat == null) {
      _stat = new CharStat(this);
    }
    return _stat;
  }

  public final void setStat(CharStat value) {
    _stat = value;
  }

  public CharStatus getStatus() {
    if (_status == null) {
      _status = new CharStatus(this);
    }
    return _status;
  }

  public final void setStatus(CharStatus value) {
    _status = value;
  }

  public L2CharTemplate getTemplate() {
    return _template;
  }

  protected final void setTemplate(L2CharTemplate template)
  {
    _template = template;
  }

  public String getTitle()
  {
    return _title;
  }

  public final void setTitle(String value)
  {
    _title = value;
  }

  public final void setWalking()
  {
    if (isRunning())
      setIsRunning(false);
  }

  public final void addEffect(L2Effect newEffect)
  {
    if (newEffect == null) {
      return;
    }

    synchronized (this) {
      if (_effects == null) {
        _effects = new FastTable();
      }

      if (_stackedEffects == null) {
        _stackedEffects = new FastMap();
      }
    }
    synchronized (_effects) {
      L2Effect tempEffect = null;

      for (int i = 0; i < _effects.size(); i++) {
        if ((((L2Effect)_effects.get(i)).getSkill().getId() != newEffect.getSkill().getId()) || (((L2Effect)_effects.get(i)).getEffectType() != newEffect.getEffectType())) {
          continue;
        }
        newEffect.stopEffectTask();
        return;
      }

      L2Skill tempskill = newEffect.getSkill();
      switch (tempskill.getSkillType()) {
      case BUFF:
      case REFLECT:
        if ((tempskill.isMalariaBuff()) || (tempskill.isSSBuff()) || (doesStack(tempskill)))
        {
          break;
        }

        if (!replaceFirstBuff()) break;
        removeFirstBuff(tempskill.getId());
      }

      if (newEffect.getSkill().isToggle())
      {
        _effects.addLast(newEffect);
      } else {
        int pos = 0;
        for (int i = 0; (i < _effects.size()) && 
          (_effects.get(i) != null); i++)
        {
          if ((!((L2Effect)_effects.get(i)).getSkill().isToggle()) && (!((L2Effect)_effects.get(i)).getSkill().isMalariaBuff()) && (!((L2Effect)_effects.get(i)).getSkill().isSSBuff())) {
            pos++;
          }

        }

        _effects.add(pos, newEffect);
      }

      if (newEffect.getStackType().equals("none"))
      {
        newEffect.setInUse(true);

        addStatFuncs(newEffect.getStatFuncs());

        updateEffectIcons();
        return;
      }

      List stackQueue = (List)_stackedEffects.get(newEffect.getStackType());

      if (stackQueue == null) {
        stackQueue = new FastList();
      }

      if (stackQueue.size() > 0)
      {
        tempEffect = null;
        for (int i = 0; i < _effects.size(); i++) {
          if (_effects.get(i) == stackQueue.get(0)) {
            tempEffect = (L2Effect)_effects.get(i);
            break;
          }
        }

        if (tempEffect != null)
        {
          removeStatsOwner(tempEffect);

          tempEffect.setInUse(false);
        }

      }

      stackQueue = effectQueueInsert(newEffect, stackQueue);

      if (stackQueue == null) {
        return;
      }

      _stackedEffects.put(newEffect.getStackType(), stackQueue);

      tempEffect = null;
      for (int i = 0; i < _effects.size(); i++) {
        if (_effects.get(i) == stackQueue.get(0)) {
          tempEffect = (L2Effect)_effects.get(i);
          break;
        }
      }

      if (tempEffect != null) {
        tempEffect.setInUse(true);
        addStatFuncs(tempEffect.getStatFuncs());
      }
    }

    updateEffectIcons();
  }

  public boolean replaceFirstBuff()
  {
    return getBuffCount() >= Config.BUFFS_MAX_AMOUNT;
  }

  private List<L2Effect> effectQueueInsert(L2Effect newStackedEffect, List<L2Effect> stackQueue)
  {
    if (_effects == null) {
      return null;
    }

    Iterator queueIterator = stackQueue.iterator();

    int i = 0;
    while (queueIterator.hasNext()) {
      L2Effect cur = (L2Effect)queueIterator.next();
      if (newStackedEffect.getStackOrder() >= cur.getStackOrder()) break;
      i++;
    }

    stackQueue.add(i, newStackedEffect);

    if ((Config.EFFECT_CANCELING) && (!newStackedEffect.isHerbEffect()) && (stackQueue.size() > 1))
    {
      for (int n = 0; n < _effects.size(); n++) {
        if (_effects.get(n) == stackQueue.get(1)) {
          _effects.remove(n);
          break;
        }
      }
      stackQueue.remove(1);
    }

    return stackQueue;
  }

  public final void removeEffect(L2Effect effect)
  {
    if ((effect == null) || (_effects == null)) {
      return;
    }

    synchronized (_effects)
    {
      if ("none".equals(effect.getStackType()))
      {
        removeStatsOwner(effect);
      } else {
        if (_stackedEffects == null) {
          return;
        }

        List stackQueue = (List)_stackedEffects.get(effect.getStackType());

        if ((stackQueue == null) || (stackQueue.size() < 1)) {
          return;
        }

        L2Effect frontEffect = (L2Effect)stackQueue.get(0);

        boolean removed = stackQueue.remove(effect);

        if (removed)
        {
          if (frontEffect == effect)
          {
            removeStatsOwner(effect);

            if (stackQueue.size() > 0)
            {
              for (int i = 0; i < _effects.size(); i++) {
                if (_effects.get(i) != stackQueue.get(0))
                  continue;
                addStatFuncs(((L2Effect)_effects.get(i)).getStatFuncs());

                ((L2Effect)_effects.get(i)).setInUse(true);
                break;
              }
            }
          }

          if (stackQueue.isEmpty()) {
            _stackedEffects.remove(effect.getStackType());
          }
          else {
            _stackedEffects.put(effect.getStackType(), stackQueue);
          }

        }

      }

      for (int i = 0; i < _effects.size(); i++) {
        if (_effects.get(i) == effect) {
          _effects.remove(i);
          break;
        }
      }

    }

    updateEffectIcons();
  }

  public final void startAbnormalEffect(int mask)
  {
    if (isRaid()) {
      return;
    }

    _AbnormalEffects |= mask;
    updateAbnormalEffect();
  }

  public final void startImmobileUntilAttacked()
  {
    setIsImmobileUntilAttacked(true);
    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
    updateAbnormalEffect();
  }

  public final void startConfused()
  {
    if (isRaid()) {
      return;
    }

    setIsConfused(true);
    getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
    updateAbnormalEffect();
  }

  public final void startFakeDeath()
  {
    setIsFakeDeath(true);

    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
    broadcastPacket(new ChangeWaitType(this, 2));
  }

  public final void startFear()
  {
    if (isRaid()) {
      return;
    }

    setIsAfraid(true);
    getAI().notifyEvent(CtrlEvent.EVT_AFFRAID);
    updateAbnormalEffect();
  }

  public final void startMuted()
  {
    if (isRaid()) {
      return;
    }

    setIsMuted(true);

    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_MUTED);
    updateAbnormalEffect();
  }

  public final void startPsychicalMuted()
  {
    if (isRaid()) {
      return;
    }

    setIsPsychicalMuted(true);
    getAI().notifyEvent(CtrlEvent.EVT_MUTED);
    updateAbnormalEffect();
  }

  public final void startRooted()
  {
    if (isRaid()) {
      return;
    }

    setIsRooted(true);
    getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
    updateAbnormalEffect();
  }

  public final void startSleeping()
  {
    if (isRaid()) {
      return;
    }

    setIsSleeping(true);

    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
    updateAbnormalEffect();
  }

  public final void startStunning()
  {
    if (isRaid()) {
      return;
    }

    setIsStunned(true);

    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
    updateAbnormalEffect();
  }

  public final void startBetray() {
    if (isRaid()) {
      return;
    }

    setIsBetrayed(true);
    getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
    updateAbnormalEffect();
  }

  public final void stopBetray() {
    stopEffects(L2Effect.EffectType.BETRAY);
    setIsBetrayed(false);
    updateAbnormalEffect();
  }

  public final void stopAbnormalEffect(int mask)
  {
    _AbnormalEffects &= (mask ^ 0xFFFFFFFF);
    updateAbnormalEffect();
  }

  public final void stopAllEffects()
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return;
    }

    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      e.exit(true);
    }
    updateAndBroadcastStatus(2);
  }

  public final void stopAllDebuffs()
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return;
    }

    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if (e.getSkill().isDebuff()) {
        e.exit(true);
      }
    }
    updateAndBroadcastStatus(2);
  }

  public final void stopImmobileUntilAttacked(L2Effect effect)
  {
    if (effect == null) {
      stopEffects(L2Effect.EffectType.IMMOBILEUNTILATTACKED);
    } else {
      removeEffect(effect);
      stopSkillEffects(effect.getSkill().getNegateId());
    }

    setIsImmobileUntilAttacked(false);
    getAI().notifyEvent(CtrlEvent.EVT_THINK);
    updateAbnormalEffect();
  }

  public final void stopConfused(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.CONFUSION);
    else {
      removeEffect(effect);
    }

    setIsConfused(false);
    getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
    updateAbnormalEffect();
  }

  public void stopSlowEffects()
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return;
    }

    L2Effect e = null;
    int i = 0; for (int n = effects.size(); i < n; i++) {
      e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      switch (e.getSkill().getId()) {
      case 95:
      case 102:
      case 105:
      case 127:
      case 354:
      case 1099:
      case 1160:
      case 1236:
      case 1298:
        e.exit();
      }
    }
  }

  public final void stopSkillEffects(int skillId)
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return;
    }

    L2Effect e = null;
    int i = 0; for (int n = effects.size(); i < n; i++) {
      e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if (e.getSkill().getId() == skillId)
        e.exit();
    }
  }

  public final void stopEffects(L2Effect.EffectType type)
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return;
    }

    L2Effect e = null;

    int i = 0; for (int n = effects.size(); i < n; i++) {
      e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }

      if (e.getEffectType() == type)
        e.exit();
    }
  }

  public final void stopFakeDeath(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.FAKE_DEATH);
    else {
      removeEffect(effect);
    }

    setIsFakeDeath(false);

    setRecentFakeDeath(true);

    ChangeWaitType revive = new ChangeWaitType(this, 3);
    broadcastPacket(revive);

    broadcastPacket(new Revive(this));
    getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
  }

  public final void stopFear(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.FEAR);
    else {
      removeEffect(effect);
    }

    setIsAfraid(false);
    updateAbnormalEffect();
  }

  public final void stopMuted(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.MUTE);
    else {
      removeEffect(effect);
    }

    setIsMuted(false);
    updateAbnormalEffect();
  }

  public final void stopPsychicalMuted(L2Effect effect) {
    if (effect == null)
      stopEffects(L2Effect.EffectType.PSYCHICAL_MUTE);
    else {
      removeEffect(effect);
    }

    setIsPsychicalMuted(false);
    updateAbnormalEffect();
  }

  public final void stopRooting(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.ROOT);
    else {
      removeEffect(effect);
    }

    setIsRooted(false);
    getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
    updateAbnormalEffect();
  }

  public final void stopSleeping(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.SLEEP);
    else {
      removeEffect(effect);
    }

    setIsSleeping(false);
    getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
    updateAbnormalEffect();
  }

  public final void stopStunning(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.STUN);
    else {
      removeEffect(effect);
    }

    setIsStunned(false);
    getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
    updateAbnormalEffect();
  }

  public abstract void updateAbnormalEffect();

  public final void updateEffectIcons()
  {
    updateEffectIcons(false);
  }

  public void updateEffectIcons(boolean partyOnly)
  {
  }

  public int getAbnormalEffect()
  {
    int ae = _AbnormalEffects;
    if (isStunned()) {
      ae |= 64;
    }
    if (isRooted()) {
      ae |= 512;
    }
    if (isSleeping()) {
      ae |= 128;
    }
    if (isConfused()) {
      ae |= 32;
    }
    if (isMuted()) {
      ae |= 256;
    }
    if (isAfraid()) {
      ae |= 16;
    }
    if (isPsychicalMuted()) {
      ae |= 256;
    }
    return ae;
  }

  public final L2Effect[] getAllEffects()
  {
    FastTable effects = _effects;

    if ((effects == null) || (effects.isEmpty())) {
      return EMPTY_EFFECTS;
    }

    int ArraySize = effects.size();
    L2Effect[] effectArray = new L2Effect[ArraySize];
    for (int i = 0; (i < ArraySize) && 
      (i < effects.size()) && (effects.get(i) != null); i++)
    {
      effectArray[i] = ((L2Effect)effects.get(i));
    }
    return effectArray;
  }

  public final FastTable<L2Effect> getAllEffectsTable()
  {
    if ((_effects == null) || (_effects.isEmpty())) {
      return EMPTY_EFFECTS_TABLE;
    }

    FastTable effects = new FastTable();
    effects.addAll(_effects);

    return effects;
  }

  public final L2Effect getFirstEffect(int index)
  {
    FastTable effects = _effects;
    if ((effects == null) || (effects.isEmpty())) {
      return null;
    }

    L2Effect e = null;
    L2Effect eventNotInUse = null;
    for (int i = 0; i < effects.size(); i++) {
      e = (L2Effect)effects.get(i);
      if (e == null) {
        continue;
      }
      if (e.getSkill().getId() == index) {
        if (e.getInUse()) {
          return e;
        }
        eventNotInUse = e;
      }
    }

    return eventNotInUse;
  }

  public final L2Effect getFirstEffect(L2Skill skill)
  {
    FastTable effects = _effects;
    if ((effects == null) || (effects.isEmpty())) {
      return null;
    }

    L2Effect e = null;
    L2Effect eventNotInUse = null;
    for (int i = 0; i < effects.size(); i++) {
      e = (L2Effect)effects.get(i);
      if (e == null) {
        continue;
      }
      if (e.getSkill() == skill) {
        if (e.getInUse()) {
          return e;
        }
        eventNotInUse = e;
      }
    }

    return eventNotInUse;
  }

  public final L2Effect getFirstEffect(L2Effect.EffectType tp)
  {
    FastTable effects = _effects;
    if (effects == null) {
      return null;
    }

    L2Effect e = null;
    L2Effect eventNotInUse = null;
    for (int i = 0; i < effects.size(); i++) {
      e = (L2Effect)effects.get(i);
      if (e == null) {
        continue;
      }
      if (e.getEffectType() == tp) {
        if (e.getInUse()) {
          return e;
        }
        eventNotInUse = e;
      }
    }

    return eventNotInUse;
  }

  public final L2Effect getFirstEffect(L2Skill.SkillType stp) {
    FastTable effects = _effects;
    if (effects == null) {
      return null;
    }

    L2Effect e = null;
    L2Effect eventNotInUse = null;
    for (int i = 0; i < effects.size(); i++) {
      e = (L2Effect)effects.get(i);
      if (e == null) {
        continue;
      }
      if (e.getSkill().getSkillType() == stp) {
        if (e.getInUse()) {
          return e;
        }
        eventNotInUse = e;
      }
    }

    return eventNotInUse;
  }

  public void addNotifyQuestOfDeath(QuestState qs)
  {
    if ((qs == null) || (_NotifyQuestOfDeathList.contains(qs))) {
      return;
    }

    _NotifyQuestOfDeathList.add(qs);
  }

  public final List<QuestState> getNotifyQuestOfDeath()
  {
    if (_NotifyQuestOfDeathList == null) {
      _NotifyQuestOfDeathList = new FastList();
    }

    return _NotifyQuestOfDeathList;
  }

  public final synchronized void addStatFunc(Func f)
  {
    if (f == null) {
      return;
    }

    if (_calculators == NPC_STD_CALCULATOR)
    {
      _calculators = new Calculator[Stats.NUM_STATS];

      for (int i = 0; i < Stats.NUM_STATS; i++) {
        if (NPC_STD_CALCULATOR[i] != null) {
          _calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
        }
      }

    }

    int stat = f.stat.ordinal();

    if (_calculators[stat] == null) {
      _calculators[stat] = new Calculator();
    }

    _calculators[stat].addFunc(f);
  }

  public final synchronized void addStatFuncs(Func[] funcs)
  {
    FastList modifiedStats = new FastList();

    for (Func f : funcs) {
      modifiedStats.add(f.stat);
      addStatFunc(f);
    }
    broadcastModifiedStats(modifiedStats);
  }

  public final synchronized void removeStatFunc(Func f)
  {
    if (f == null) {
      return;
    }

    int stat = f.stat.ordinal();

    if (_calculators[stat] == null) {
      return;
    }

    _calculators[stat].removeFunc(f);

    if (_calculators[stat].size() == 0) {
      _calculators[stat] = null;
    }

    if (isL2Npc()) {
      int i = 0;
      while ((i < Stats.NUM_STATS) && 
        (Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))) {
        i++;
      }

      if (i >= Stats.NUM_STATS)
        _calculators = NPC_STD_CALCULATOR;
    }
  }

  public final synchronized void removeStatFuncs(Func[] funcs)
  {
    FastList modifiedStats = new FastList();

    for (Func f : funcs) {
      modifiedStats.add(f.stat);
      removeStatFunc(f);
    }

    broadcastModifiedStats(modifiedStats);
  }

  public final synchronized void removeStatsOwner(Object owner)
  {
    FastList modifiedStats = null;

    for (int i = 0; i < _calculators.length; i++) {
      if (_calculators[i] == null)
        continue;
      if (modifiedStats != null)
        modifiedStats.addAll(_calculators[i].removeOwner(owner));
      else {
        modifiedStats = _calculators[i].removeOwner(owner);
      }

      if (_calculators[i].size() == 0) {
        _calculators[i] = null;
      }

    }

    if (isL2Npc()) {
      int i = 0;
      while ((i < Stats.NUM_STATS) && 
        (Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))) {
        i++;
      }

      if (i >= Stats.NUM_STATS) {
        _calculators = NPC_STD_CALCULATOR;
      }
    }

    if (((owner instanceof L2Effect)) && (!((L2Effect)owner).preventExitUpdate))
      broadcastModifiedStats(modifiedStats);
  }

  private void broadcastModifiedStats(FastList<Stats> stats)
  {
    if ((stats == null) || (stats.isEmpty())) {
      return;
    }

    boolean broadcastFull = false;
    boolean otherStats = false;
    StatusUpdate su = null;

    for (Stats stat : stats) {
      if (isL2Summon()) {
        updateAndBroadcastStatus(1);
        break;
      }if (stat == Stats.POWER_ATTACK_SPEED) {
        if (su == null) {
          su = new StatusUpdate(getObjectId());
        }
        su.addAttribute(18, getPAtkSpd());
      } else if (stat == Stats.MAGIC_ATTACK_SPEED) {
        if (su == null) {
          su = new StatusUpdate(getObjectId());
        }
        su.addAttribute(24, getMAtkSpd());
      }
      else if (stat == Stats.MAX_CP) {
        if (isPlayer()) {
          if (su == null) {
            su = new StatusUpdate(getObjectId());
          }
          su.addAttribute(34, getMaxCp());
        }

      }
      else if (stat == Stats.RUN_SPEED) {
        broadcastFull = true;
      } else {
        otherStats = true;
      }
    }

    if (isPlayer()) {
      if (broadcastFull) {
        updateAndBroadcastStatus(2);
      }
      else if (otherStats) {
        updateAndBroadcastStatus(1);
        if (su != null) {
          FastList players = getKnownList().getListKnownPlayers();
          L2PcInstance pc = null;
          FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
            pc = (L2PcInstance)n.getValue();
            if (pc == null)
            {
              continue;
            }
            pc.sendPacket(su);
          }
          players.clear();
          players = null;
          pc = null;
        }
      } else if (su != null) {
        broadcastPacket(su);
      }
    }
    else if (isL2Npc()) {
      if (broadcastFull) {
        FastList players = getKnownList().getListKnownPlayers();
        L2PcInstance pc = null;
        FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
          {
            continue;
          }
          if (getRunSpeed() == 0) {
            pc.sendPacket(new ServerObjectInfo((L2NpcInstance)this, pc)); continue;
          }
          pc.sendPacket(new NpcInfo((L2NpcInstance)this, pc));
        }

        players.clear();
        players = null;
        pc = null;
      } else if (su != null) {
        broadcastPacket(su);
      }
    } else if (su != null) {
      broadcastPacket(su);
    }
    su = null;
  }

  public final int getHeading()
  {
    if (isCastingNow()) {
      correctHeadingWhenCast();
    }
    return _heading;
  }

  private void correctHeadingWhenCast() {
    L2Character castingTarget = getAI().getCastTarget();
    if ((castingTarget != null) && (!castingTarget.equals(this)))
      setHeading(castingTarget, true);
  }

  public final void setHeading(int heading)
  {
    _heading = heading;
  }

  public final void setHeading(L2Object target, boolean toChar) {
    if ((target == null) || (target.equals(this))) {
      return;
    }

    _heading = ((int)(Math.atan2(getY() - target.getY(), getX() - target.getX()) * 32768.0D / 3.141592653589793D) + (toChar ? 32768 : 0));
    if (_heading < 0)
      _heading += 65536;
  }

  public final int getClientX()
  {
    return _clientX;
  }

  public final int getClientY() {
    return _clientY;
  }

  public final int getClientZ() {
    return _clientZ;
  }

  public final int getClientHeading() {
    return _clientHeading;
  }

  public final void setClientX(int val) {
    _clientX = val;
  }

  public final void setClientY(int val) {
    _clientY = val;
  }

  public final void setClientZ(int val) {
    _clientZ = val;
  }

  public final void setClientHeading(int val) {
    _clientHeading = val;
  }

  public final int getXdestination() {
    MoveData m = _move;

    if (m != null) {
      return m._xDestination;
    }

    return getX();
  }

  public final int getYdestination()
  {
    MoveData m = _move;

    if (m != null) {
      return m._yDestination;
    }

    return getY();
  }

  public final int getZdestination()
  {
    MoveData m = _move;

    if (m != null) {
      return m._zDestination;
    }

    return getZ();
  }

  public final boolean isInCombat()
  {
    return getAI().getAttackTarget() != null;
  }

  public final boolean isMoving()
  {
    return _move != null;
  }

  public final boolean isOnGeodataPath()
  {
    MoveData m = _move;
    if (m == null) {
      return false;
    }
    if (m.geoPath == null) {
      return false;
    }
    if (m.onGeodataPathIndex == -1) {
      return false;
    }

    return m.onGeodataPathIndex != m.geoPath.size() - 1;
  }

  public final boolean isCastingNow()
  {
    return _castEndTime > GameTimeController.getGameTicks();
  }

  public final boolean canAbortCast()
  {
    return _castInterruptTime > GameTimeController.getGameTicks();
  }

  public final boolean isAttackingNow()
  {
    return _attackEndTime > System.currentTimeMillis();
  }

  public final boolean isAttackAborted()
  {
    return _attacking <= 0;
  }

  public final void abortAttack()
  {
    if (isAttackingNow()) {
      _attacking = 0;
      _attackEndTime = 0L;
      sendActionFailed();
    }
  }

  public final int getAttackingBodyPart()
  {
    return _attacking;
  }

  public final void abortCast()
  {
    if (isCastingNow()) {
      _castEndTime = 0;
      _castInterruptTime = 0;
      if (_skillCast != null) {
        _skillCast.cancel(true);
        _skillCast = null;
      }

      if (getForceBuff() != null) {
        getForceBuff().delete();
      }

      L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
      if (mog != null) {
        mog.exit();
      }

      correctHeadingWhenCast();
      enableAllSkills();
      if (isPlayer()) {
        getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
      }
      broadcastPacket(new MagicSkillCanceld(getObjectId()));
      sendActionFailed();
    }
  }

  public boolean updatePosition(int gameTicks)
  {
    MoveData m = _move;

    if (m == null) {
      return true;
    }

    if (!isVisible()) {
      _move = null;
      return true;
    }

    if (m._moveTimestamp == 0) {
      m._moveTimestamp = m._moveStartTime;
      m._xAccurate = getX();
      m._yAccurate = getY();
    }

    if (m._moveTimestamp == gameTicks) {
      return false;
    }

    int xPrev = getX();
    int yPrev = getY();
    int zPrev = getZ();
    double dy;
    double dx;
    double dy;
    if (Config.COORD_SYNCHRONIZE == 1)
    {
      double dx = m._xDestination - xPrev;
      dy = m._yDestination - yPrev;
    }
    else {
      dx = m._xDestination - m._xAccurate;
      dy = m._yDestination - m._yAccurate;
    }

    boolean isFloating = (isFlying()) || (isInWater());
    double dz = m._zDestination - zPrev;

    double delta = dx * dx + dy * dy;
    if ((delta < 10000.0D) && (dz * dz > 2500.0D) && (!isFloating))
    {
      delta = Math.sqrt(delta);
    }
    else delta = Math.sqrt(delta + dz * dz);

    double distFraction = 1.7976931348623157E+308D;
    if (delta > 1.0D) {
      double distPassed = getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / 10.0F;
      distFraction = distPassed / delta;
    }

    if (distFraction > 1.0D)
    {
      super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
    } else {
      m._xAccurate += dx * distFraction;
      m._yAccurate += dy * distFraction;

      super.getPosition().setXYZ((int)m._xAccurate, (int)m._yAccurate, zPrev + (int)(dz * distFraction + 0.5D));
    }
    revalidateZone();

    m._moveTimestamp = gameTicks;

    return distFraction > 1.0D;
  }

  public void revalidateZone() {
    if (getWorldRegion() == null) {
      return;
    }

    getWorldRegion().revalidateZones(this);
  }

  public void stopMove(L2CharPosition pos)
  {
    stopMove(pos, true);
  }

  public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
  {
    _move = null;

    if (pos != null) {
      getPosition().setXYZ(pos.x, pos.y, pos.z);
      setHeading(pos.heading);
      revalidateZone(true);
    }
    broadcastPacket(new StopMove(this));
    if (updateKnownObjects)
      ThreadPoolManager.getInstance().executeAi(new ObjectKnownList.KnownListAsynchronousUpdateTask(this), (isPlayer()) || (isPet()) || (isSummon()));
  }

  public boolean isShowSummonAnimation()
  {
    return _showSummonAnimation;
  }

  public void setShowSummonAnimation(boolean showSummonAnimation)
  {
    _showSummonAnimation = showSummonAnimation;
  }

  public void setTarget(L2Object object)
  {
    if ((object != null) && (!object.isVisible())) {
      object = null;
    }

    if ((object != null) && (object != getTarget())) {
      getKnownList().addKnownObject(object);
      object.getKnownList().addKnownObject(this);
    }

    if (object == null) {
      if (getTarget() != null) {
        broadcastPacket(new TargetUnselected(this));
      }

      if ((isAttackingNow()) && (getAI().getAttackTarget() == getTarget())) {
        abortAttack();
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      }

      if ((isCastingNow()) && (canAbortCast()) && (getAI().getCastTarget() == getTarget())) {
        abortCast();
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      }
    }
    _target = (object == null ? null : new WeakReference(object));
  }

  public final int getTargetId()
  {
    L2Object target = getTarget();
    if (target == null) {
      return -1;
    }

    return target.getObjectId();
  }

  public final L2Object getTarget()
  {
    if (_target == null) {
      return null;
    }

    L2Object t = (L2Object)_target.get();
    if (t == null) {
      _target = null;
    }
    return t;
  }

  public void moveToLocationm(int x, int y, int z, int offset)
  {
    moveToLocation(x, y, z, offset);
  }

  protected void moveToLocation(int x, int y, int z, int offset) {
    if ((getStat().getMoveSpeed() <= 0.0F) || (isMovementDisabled())) {
      sendActionFailed();
      return;
    }
    clearNextLoc();

    int curX = super.getX();
    int curY = super.getY();
    int curZ = super.getZ();

    if (DoorTable.getInstance().checkIfDoorsBetween(this, curX, curY, curZ, x, y, z)) {
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      return;
    }

    double dx = x - curX;
    double dy = y - curY;
    double dz = z - curZ;
    double distance = Math.sqrt(dx * dx + dy * dy);

    if ((isInWater()) && (distance > 700.0D)) {
      double divider = 700.0D / distance;
      x = curX + (int)(divider * dx);
      y = curY + (int)(divider * dy);
      z = curZ + (int)(divider * dz);
      dx = x - curX;
      dy = y - curY;
      dz = z - curZ;
      distance = Math.sqrt(dx * dx + dy * dy);
    }
    double sin;
    double cos;
    if ((offset > 0) || (distance < 1.0D))
    {
      offset = (int)(offset - Math.abs(dz));
      if (offset < 5) {
        offset = 5;
      }

      if ((distance < 1.0D) || (distance - offset <= 0.0D))
      {
        getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);

        return;
      }

      double sin = dy / distance;
      double cos = dx / distance;

      distance -= offset - 5;

      x = curX + (int)(distance * cos);
      y = curY + (int)(distance * sin);
    }
    else
    {
      sin = dy / distance;
      cos = dx / distance;
    }

    MoveData m = new MoveData();

    m.onGeodataPathIndex = -1;

    if (checkMove()) {
      double originalDistance = distance;
      int originalX = x;
      int originalY = y;
      int originalZ = z;
      int gtx = originalX - L2World.MAP_MIN_X >> 4;
      int gty = originalY - L2World.MAP_MIN_Y >> 4;

      if (checkMoveDestiny()) {
        if (isOnGeodataPath()) {
          if ((gtx == _move.geoPathGtx) && (gty == _move.geoPathGty)) {
            return;
          }
          _move.onGeodataPathIndex = -1;
        }

        if ((curX < L2World.MAP_MIN_X) || (curX > L2World.MAP_MAX_X) || (curY < L2World.MAP_MIN_Y) || (curY > L2World.MAP_MAX_Y))
        {
          getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
          if (isPlayer()) {
            logout(); } else {
            if (isL2Summon()) {
              return;
            }
            onDecay();
          }
          return;
        }
        Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z);

        x = destiny.getX();
        y = destiny.getY();
        z = destiny.getZ();
        dx = x - curX;
        dy = y - curY;
        dz = z - curZ;
        distance = Math.sqrt(dx * dx + dy * dy);
      }

      if ((geoPathfind()) && (originalDistance - distance > 30.0D) && (distance < 2000.0D) && (!isAfraid()))
      {
        if ((isL2Playable()) || (isInCombat()))
        {
          m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ);
          if ((m.geoPath == null) || (m.geoPath.size() < 2))
          {
            if ((isPlayer()) || ((!isL2Playable()) && (Math.abs(z - curZ) > 140)) || ((isL2Summon()) && (!((L2Summon)this).getFollowStatus())))
            {
              getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
              return;
            }
            x = originalX;
            y = originalY;
            z = originalZ;
            distance = originalDistance;
          }
          else {
            m.onGeodataPathIndex = 0;
            m.geoPathGtx = gtx;
            m.geoPathGty = gty;
            m.geoPathAccurateTx = originalX;
            m.geoPathAccurateTy = originalY;

            x = ((AbstractNodeLoc)m.geoPath.get(m.onGeodataPathIndex)).getX();
            y = ((AbstractNodeLoc)m.geoPath.get(m.onGeodataPathIndex)).getY();
            z = ((AbstractNodeLoc)m.geoPath.get(m.onGeodataPathIndex)).getZ();

            if (DoorTable.getInstance().checkIfDoorsBetween(this, curX, curY, curZ, x, y, z)) {
              m.geoPath.clear();
              m.geoPath = null;
              getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
              return;
            }

            dx = x - curX;
            dy = y - curY;
            dz = z - curZ;
            distance = Math.sqrt(dx * dx + dy * dy);
            sin = dy / distance;
            cos = dx / distance;
          }
        }
      }

      if ((distance < 1.0D) && ((geoPathfind()) || (isL2Playable()) || (isAfraid()) || (isL2RiftInvader())))
      {
        setFollowStatus(false);

        getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        return;
      }

    }

    float speed = getStat().getMoveSpeed();

    if ((isFlying()) || (isInWater())) {
      distance = Math.sqrt(distance * distance + dz * dz);
    }

    m._ticksToMove = (1 + (int)(10.0D * distance / speed));

    m._xSpeedTicks = (float)(cos * speed / 10.0D);
    m._ySpeedTicks = (float)(sin * speed / 10.0D);

    int heading = calcHeading(x, y);
    setHeading(heading);
    m._xDestination = x;
    m._yDestination = y;
    m._zDestination = z;
    m._heading = heading;

    m._moveStartTime = GameTimeController.getGameTicks();
    m._xMoveFrom = curX;
    m._yMoveFrom = curY;
    m._zMoveFrom = curZ;

    _move = m;

    GameTimeController.getInstance().registerMovingObject(this);

    if (m._ticksToMove * 100 > 3000)
      ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000L, (isPlayer()) || (isPet()) || (isSummon()));
  }

  private boolean checkMove()
  {
    return (!isFlying()) && (!isInWater());
  }

  private boolean checkMoveDestiny()
  {
    if ((geoPathfind()) && (!isReturningToSpawnPoint())) {
      return true;
    }

    return (isPlayer()) || ((isL2Summon()) && (getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)) || (isAfraid()) || (isL2RiftInvader());
  }

  public boolean moveToNextRoutePoint() {
    if (!isOnGeodataPath())
    {
      _move = null;
      return false;
    }

    if ((getStat().getMoveSpeed() <= 0.0F) || (isMovementDisabled()))
    {
      _move = null;
      return false;
    }

    MoveData md = _move;
    if (md == null) {
      return false;
    }

    MoveData m = new MoveData();

    md.onGeodataPathIndex += 1;
    m.geoPath = md.geoPath;
    m.geoPathGtx = md.geoPathGtx;
    m.geoPathGty = md.geoPathGty;
    m.geoPathAccurateTx = md.geoPathAccurateTx;
    m.geoPathAccurateTy = md.geoPathAccurateTy;

    if (md.onGeodataPathIndex == md.geoPath.size() - 2) {
      m._xDestination = md.geoPathAccurateTx;
      m._yDestination = md.geoPathAccurateTy;
      m._zDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getZ();
    } else {
      m._xDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getX();
      m._yDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getY();
      m._zDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getZ();
    }

    if (DoorTable.getInstance().checkIfDoorsBetween(this, getX(), getY(), getZ(), m._xDestination, m._yDestination, m._zDestination)) {
      _move = null;
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      return false;
    }

    float speed = getStat().getMoveSpeed();
    double dx = m._xDestination - super.getX();
    double dy = m._yDestination - super.getY();
    double distance = Math.sqrt(dx * dx + dy * dy);

    if (distance != 0.0D) {
      setHeading(calcHeading(m._xDestination, m._yDestination));
    }

    int ticksToMove = 1 + (int)(10.0D * distance / speed);

    m._heading = 0;

    m._moveStartTime = GameTimeController.getGameTicks();

    _move = m;

    GameTimeController.getInstance().registerMovingObject(this);

    if (ticksToMove * 100 > 3000) {
      ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000L, (isPlayer()) || (isPet()) || (isSummon()));
    }

    broadcastPacket(new CharMoveToLocation(this));
    return true;
  }

  public boolean validateMovementHeading(int heading) {
    MoveData m = _move;

    if (m == null) {
      return true;
    }

    boolean result = true;
    if (m._heading != heading) {
      result = m._heading == 0;
      m._heading = heading;
    }

    return result;
  }

  @Deprecated
  public final double getDistance(int x, int y)
  {
    double dx = x - getX();
    double dy = y - getY();
    return Math.sqrt(dx * dx + dy * dy);
  }

  @Deprecated
  public final double getDistance(int x, int y, int z)
  {
    double dx = x - getX();
    double dy = y - getY();
    double dz = z - getZ();

    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public final double getDistanceSq(L2Object object)
  {
    return getDistanceSq(object.getX(), object.getY(), object.getZ());
  }

  public final double getDistanceSq(int x, int y, int z)
  {
    double dx = x - getX();
    double dy = y - getY();
    double dz = z - getZ();

    return dx * dx + dy * dy + dz * dz;
  }

  public final double getPlanDistanceSq(L2Object object)
  {
    return getPlanDistanceSq(object.getX(), object.getY());
  }

  public final double getPlanDistanceSq(int x, int y)
  {
    double dx = x - getX();
    double dy = y - getY();

    return dx * dx + dy * dy;
  }

  public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
  {
    return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
  }

  public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
  {
    return isInsideRadius(x, y, 0, radius, false, strictCheck);
  }

  public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
  {
    double dx = x - getX();
    double dy = y - getY();
    double dz = z - getZ();

    if (strictCheck) {
      if (checkZ) {
        return dx * dx + dy * dy + dz * dz < radius * radius;
      }
      return dx * dx + dy * dy < radius * radius;
    }

    if (checkZ) {
      return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
    return dx * dx + dy * dy <= radius * radius;
  }

  public float getWeaponExpertisePenalty()
  {
    return 1.0F;
  }

  public float getArmourExpertisePenalty()
  {
    return 1.0F;
  }

  public void setAttackingBodypart()
  {
    _attacking = 10;
  }

  protected boolean checkAndEquipArrows()
  {
    return true;
  }

  public void addExpAndSp(long addToExp, int addToSp)
  {
  }

  public abstract L2ItemInstance getActiveWeaponInstance();

  public abstract L2Weapon getActiveWeaponItem();

  public abstract L2ItemInstance getSecondaryWeaponInstance();

  public abstract L2Weapon getSecondaryWeaponItem();

  protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
  {
    if ((target == null) || (isAlikeDead()) || (isEventMob())) {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if (target.isInvul()) {
      sendUserPacket(Static.TARGET_IS_INVUL);
      damage = 0;
    }

    if (((isL2Npc()) && (target.isAlikeDead())) || (target.isDead()) || ((!getKnownList().knowsObject(target)) && (!isL2Door())))
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      sendActionFailed();
      return;
    }

    if ((Config.ALLOW_RAID_BOSS_PUT) && (target.isRaid()) && (getLevel() > target.getLevel() + 8)) {
      SkillTable.getInstance().getInfo(4515, 1).getEffects(this, this);
      return;
    }

    if (miss) {
      sendUserPacket(Static.MISSED_TARGET);
      if (target.isPlayer()) {
        SystemMessage sm = SystemMessage.id(SystemMessageId.AVOIDED_S1S_ATTACK);
        if (isL2Summon()) {
          int mobId = ((L2Summon)this).getTemplate().npcId;
          sm.addNpcName(mobId);
        } else {
          sm.addString(getName());
        }
        target.sendUserPacket(sm);
        sm = null;
      }

    }

    if (!isAttackAborted()) {
      sendDamageMessage(target, damage, false, crit, miss);

      if (target.isPlayer())
      {
        if (shld) {
          target.sendUserPacket(Static.SHIELD_DEFENCE_SUCCESSFULL);
        }

      }
      else if (target.isL2Summon()) {
        target.getOwner().sendUserPacket(SystemMessage.id(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1).addString(getName()).addNumber(damage));
      }

      if ((!miss) && (damage > 0)) {
        int reflectedDamage = 0;
        L2Weapon weapon = getActiveWeaponItem();
        boolean isBow = (weapon != null) && (weapon.getItemType().toString().equalsIgnoreCase("Bow"));

        if (!isBow)
        {
          if (getCurrentHp() > 0.0D)
          {
            double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0D, null, null);

            if (reflectPercent > 0.0D) {
              reflectedDamage = (int)(reflectPercent / 100.0D * damage);
              reflectedDamage = Math.min(reflectedDamage, getMaxHp());
              reflectedDamage = Math.max(reflectedDamage, 0);
              damage -= reflectedDamage;
            }

            double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0D, null, null);
            if (absorbPercent > 0.0D) {
              int absorbDamage = (int)(absorbPercent / 100.0D * damage);
              absorbDamage = Math.min(absorbDamage, (int)(getMaxHp() - getCurrentHp()));
              if (absorbDamage > 0) {
                setCurrentHp(getCurrentHp() + absorbDamage);
              }
            }
          }
        }

        target.reduceCurrentHp(damage, this);
        if ((reflectedDamage > 0) && (getCurrentHp() > reflectedDamage)) {
          getStatus().reduceHp(reflectedDamage, target, true);
        }

        target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
        getAI().clientStartAutoAttack();

        if ((!target.isRaid()) && (Formulas.calcAtkBreak(target, damage))) {
          target.breakAttack();
          target.breakCast();
        }

        if (getChanceSkills() != null) {
          if (getSkillLevel(3207) > 0) {
            if (Rnd.get(100) < 5) {
              setCurrentHp(getCurrentHp() + 522.0D);
              broadcastPacket(new MagicSkillUser(this, this, 5123, 1, 0, 0));
            }
          } else if (Rnd.get(100) < 15) {
            getChanceSkills().onHit(target, false, crit);
          }
        }

        if (target.getChanceSkills() != null) {
          if (getSkillLevel(3213) > 0) {
            if (Rnd.get(100) < 5) {
              target.setCurrentCp(target.getCurrentCp() + 473.0D);
              target.broadcastPacket(new MagicSkillUser(this, this, 5123, 1, 0, 0));
            }
          } else if (Rnd.get(100) < 15) {
            target.getChanceSkills().onHit(this, true, crit);
          }

        }

        if (target.isImmobileUntilAttacked()) {
          target.stopImmobileUntilAttacked(null);
        }

        if (isPlayer())
        {
          if ((target.isPlayer()) && (
            (!isInParty()) || ((isInParty()) && (getParty().getPartyMembers().contains(target))))) {
            boolean haveBuff = false;

            if ((target.getFirstEffect(445) != null) && 
              (Rnd.get(100) < Config.MIRAGE_CHANCE)) {
              getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
              setTarget(null);
              abortAttack();
              abortCast();
              haveBuff = true;
            }

            if (Config.ALLOW_APELLA_BONUSES)
            {
              if ((target.getSkillLevel(3608) > 0) && 
                (target.getFirstEffect(4202) == null) && (Rnd.get(100) < 15)) {
                SkillTable.getInstance().getInfo(4202, 12).getEffects(this, this);
                haveBuff = true;
              }

              if ((target.getSkillLevel(3609) > 0) && 
                (target.getFirstEffect(4200) == null) && (Rnd.get(100) < 15)) {
                SkillTable.getInstance().getInfo(4200, 12).getEffects(this, this);
                haveBuff = true;
              }

              if ((target.getSkillLevel(3610) > 0) && 
                (target.getFirstEffect(4203) == null) && (Rnd.get(100) < 15)) {
                SkillTable.getInstance().getInfo(4203, 12).getEffects(this, this);
                haveBuff = true;
              }

            }

            if (haveBuff) {
              broadcastPacket(new MagicSkillUser(this, this, 5144, 1, 0, 0));
            }
          }

        }

      }

      L2Weapon activeWeapon = getActiveWeaponItem();

      if (activeWeapon != null) {
        activeWeapon.getSkillEffects(this, target, crit);
      }

      rechargeAutoSoulShot(true, false, isL2Summon());
      return;
    }

    rechargeAutoSoulShot(true, false, isL2Summon());

    if (!isCastingNow())
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
  }

  public void breakAttack()
  {
    if (isAttackingNow())
    {
      abortAttack();

      sendUserPacket(Static.ATTACK_FAILED);
    }
  }

  public void breakCast()
  {
    if ((isCastingNow()) && (canAbortCast()) && (getLastSkillCast() != null) && (getLastSkillCast().isMagic()))
    {
      abortCast();

      sendUserPacket(Static.CASTING_INTERRUPTED);
    }
  }

  protected void reduceArrowCount()
  {
  }

  public void onForcedAttack(L2PcInstance player)
  {
    if (player.isConfused()) {
      player.sendActionFailed();
      return;
    }

    if (isL2Artefact())
    {
      player.sendActionFailed();
      return;
    }

    if (player.isSitting()) {
      player.sendActionFailed();
      return;
    }

    player.clearNextLoc();
    player.sendUserPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
    player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
  }

  public Boolean isInActiveRegion()
  {
    L2WorldRegion region = getWorldRegion();
    return Boolean.valueOf((region != null) && (region.isActive().booleanValue()));
  }

  public boolean isInParty()
  {
    return false;
  }

  public L2Party getParty()
  {
    return null;
  }

  public boolean isUsingDualWeapon()
  {
    return false;
  }

  public L2Skill addSkill(L2Skill newSkill)
  {
    L2Skill oldSkill = null;

    if (newSkill != null)
    {
      oldSkill = (L2Skill)_skills.put(Integer.valueOf(newSkill.getId()), newSkill);

      if (oldSkill != null) {
        removeStatsOwner(oldSkill);
      }

      addStatFuncs(newSkill.getStatFuncs(null, this));

      if ((oldSkill != null) && (oldSkill.isChance()) && (_chanceSkills != null)) {
        _chanceSkills.remove(oldSkill);
      }
      if (newSkill.isChance()) {
        addChanceSkill(newSkill);
      }
    }

    return oldSkill;
  }

  public L2Skill removeSkill(L2Skill skill)
  {
    if (skill == null) {
      return null;
    }

    L2Skill oldSkill = (L2Skill)_skills.remove(Integer.valueOf(skill.getId()));

    if (oldSkill != null) {
      if ((oldSkill.isChance()) && (_chanceSkills != null))
        removeChanceSkill(oldSkill);
      else {
        removeStatsOwner(oldSkill);
      }
    }
    return oldSkill;
  }

  public final L2Skill[] getAllSkills()
  {
    if (_skills == null) {
      return new L2Skill[0];
    }

    return (L2Skill[])_skills.values().toArray(new L2Skill[_skills.values().size()]);
  }

  public void setLastTrigger()
  {
    _lastTrigger = System.currentTimeMillis();
  }

  public ChanceSkillList getChanceSkills() {
    if (System.currentTimeMillis() - _lastTrigger < 60000L) {
      return null;
    }

    return _chanceSkills;
  }

  public int getSkillLevel(int skillId)
  {
    if (_skills == null) {
      return -1;
    }

    L2Skill skill = (L2Skill)_skills.get(Integer.valueOf(skillId));
    if (skill == null) {
      return -1;
    }

    return skill.getLevel();
  }

  public final L2Skill getKnownSkill(int skillId)
  {
    if (_skills == null) {
      return null;
    }

    return (L2Skill)_skills.get(Integer.valueOf(skillId));
  }

  public int getBuffCount()
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return 0;
    }

    int numBuffs = 0;
    L2Effect e = null;
    int i = 0; for (int n = effects.size(); i < n; i++) {
      e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if ((e.getSkill().isBuff()) && (!e.getSkill().isSSBuff())) {
        numBuffs++;
      }
    }
    return numBuffs;
  }

  public void removeFirstBuff(int preferSkill)
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return;
    }

    L2Effect e = null;
    L2Effect removeMe = null;
    int i = 0; for (int n = effects.size(); i < n; i++) {
      e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if ((e.getSkill().isBuff()) && (!e.getSkill().isSSBuff()) && (!e.getSkill().isMalariaBuff())) {
        if (preferSkill == 0) {
          removeMe = e;
          break;
        }if (e.getSkill().getId() == preferSkill) {
          removeMe = e;
          break;
        }if (removeMe == null) {
          removeMe = e;
        }
      }
    }
    if (removeMe != null)
      removeMe.exit();
  }

  public int getDanceCount()
  {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return 0;
    }

    L2Effect e = null;
    int danceCount = 0;
    int i = 0; for (int n = effects.size(); i < n; i++) {
      e = (L2Effect)effects.get(i);
      if (e == null) {
        continue;
      }
      if ((e.getSkill().isDance()) && (e.getInUse())) {
        danceCount++;
      }
    }
    return danceCount;
  }

  public int getAugmentCount() {
    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      return 0;
    }

    L2Effect e = null;
    int augmentCount = 0;
    int i = 0; for (int n = effects.size(); i < n; i++) {
      e = (L2Effect)effects.get(i);
      if (e == null) {
        continue;
      }
      if ((e.getSkill().isAugment()) && (e.getInUse())) {
        augmentCount++;
      }
    }
    return augmentCount;
  }

  public boolean doesStack(L2Skill checkSkill)
  {
    if ((_effects == null) || (_effects.size() < 1) || (checkSkill._effectTemplates == null) || (checkSkill._effectTemplates.length < 1) || (checkSkill._effectTemplates[0].stackType == null))
    {
      return false;
    }

    String stackType = checkSkill._effectTemplates[0].stackType;
    if (stackType.equals("none")) {
      return false;
    }

    for (int i = 0; i < _effects.size(); i++) {
      if ((((L2Effect)_effects.get(i)).getStackType() != null) && (((L2Effect)_effects.get(i)).getStackType().equals(stackType))) {
        return true;
      }
    }
    return false;
  }

  public void onMagicLaunchedTimer(FastList<L2Object> targets, L2Skill skill, int coolTime, boolean instant)
  {
    if ((skill == null) || (targets == null) || (targets.isEmpty())) {
      _skillCast = null;
      enableAllSkills();
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    int escapeRange = 0;
    if (skill.getEffectRange() > escapeRange)
      escapeRange = skill.getEffectRange();
    else if ((skill.getCastRange() < 0) && (skill.getSkillRadius() > 80)) {
      escapeRange = skill.getSkillRadius();
    }

    if ((escapeRange > 0) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_SIGNET)) {
      L2Object target = null;
      PeaceZone _peace = PeaceZone.getInstance();
      FastList targetList = new FastList();
      FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
        target = (L2Object)n.getValue();
        if ((target == null) || (!target.isL2Character()) || 
          (!Util.checkIfInRange(escapeRange, this, target, true)) || (
          (skill.isOffensive()) && (!target.isMonster()) && (!target.isL2Npc()) && 
          (_peace.inPeace(this, target))))
        {
          continue;
        }

        targetList.add((L2Character)target);
      }

      if (targetList.isEmpty())
      {
        if (skill.getId() != 347) {
          abortCast();
          return;
        }
        targets.clear();
        targets.add(this);
      } else {
        targets.clear();
        targets.addAll(targetList);
      }

    }

    if ((!isCastingNow()) || ((isAlikeDead()) && (!skill.isPotion()))) {
      _skillCast = null;
      enableAllSkills();

      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

      _castEndTime = 0;
      _castInterruptTime = 0;
      return;
    }

    int magicId = skill.getDisplayId();

    int level = getSkillLevel(skill.getId());
    if (level < 1) {
      level = 1;
    }

    if (!skill.isPotion())
    {
      broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
    }

    if (instant)
      onMagicHitTimer(targets, skill, coolTime, true, 0);
    else
      _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, 0), 200L);
  }

  public void onMagicHitTimer(FastList<L2Object> targets, L2Skill skill, int coolTime, boolean instant, int hitTime)
  {
    if ((skill == null) || (targets == null) || (targets.isEmpty())) {
      abortCast();
      return;
    }

    switch (skill.getTargetType()) {
    case TARGET_SIGNET:
    case TARGET_SIGNET_GROUND:
      break;
    default:
      if ((targets != null) && (!targets.isEmpty())) break;
      _skillCast = null;
      enableAllSkills();
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    boolean forceBuff = skill.getSkillType() == L2Skill.SkillType.FORCE_BUFF;

    if (forceBuff) {
      startForceBuff((L2Character)targets.getFirst(), skill);
    }

    L2Object tgt = null;
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
      tgt = (L2Object)n.getValue();
      if (tgt == null)
      {
        continue;
      }
      if (tgt.isL2Playable()) {
        L2Character target = (L2Character)tgt;
        if ((skill.getSkillType() != L2Skill.SkillType.BUFF) || (skill.getSkillType() != L2Skill.SkillType.MANAHEAL) || (skill.getSkillType() != L2Skill.SkillType.RESURRECT) || (skill.getSkillType() != L2Skill.SkillType.RECALL) || (skill.getSkillType() != L2Skill.SkillType.DOT) || ((isInParty()) && (getParty().getPartyMembers().contains(target))) || (getClan() == target.getClan()))
        {
          continue;
        }

        if (skill.getSkillType() == L2Skill.SkillType.BUFF) {
          target.sendUserPacket(SystemMessage.id(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill.getDisplayId()));
        }

        if ((isPlayer()) && (target.isL2Summon())) {
          target.updateAndBroadcastStatus(1);
        }
        if ((isPlayer()) && (target.isPartner())) {
          target.updateAndBroadcastPartnerStatus(1);
        }
      }

      if ((isPlayer()) && (tgt.isL2Monster()) && (skill.getSkillType() != L2Skill.SkillType.SUMMON) && (skill.getSkillType() != L2Skill.SkillType.BEAST_FEED) && (!skill.isOffensive())) {
        updatePvPStatus();
      }

    }

    if (getStat().getMpConsume(skill) > 0) {
      getStatus().reduceMp(calcStat(Stats.MP_CONSUME_RATE, getStat().getMpConsume(skill), null, null));
    }

    if (skill.getHpConsume() > 0) {
      setCurrentHp(getCurrentHp() - skill.getHpConsume());
    }

    if ((skill.getItemConsume() > 0) && 
      (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, false))) {
      sendUserPacket(Static.NOT_ENOUGH_ITEMS);
      abortCast();
      return;
    }

    if (!forceBuff) {
      callSkill(skill, targets);
    }

    if (skill.getInitialEffectDelay() > 0)
      _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, 0), hitTime);
    else if ((instant) || (coolTime == 0))
    {
      onMagicFinalizer(skill, (L2Object)targets.getFirst());
    }
    else
    {
      _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, 0), coolTime);
    }

    if (skill.isNoShot()) {
      return;
    }

    if (skill.useSpiritShot())
      rechargeAutoSoulShot(false, true, isL2Summon());
    else
      rechargeAutoSoulShot(true, false, isL2Summon());
  }

  public void onMagicFinalizer(L2Skill skill, L2Object target)
  {
    _skillCast = null;
    _castEndTime = 0;
    _castInterruptTime = 0;
    enableAllSkills();

    if (getForceBuff() != null) {
      getForceBuff().delete();
    }

    L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
    if (mog != null) {
      mog.exit();
    }

    correctHeadingWhenCast();

    if (continueAttack(skill, getTarget(), target)) {
      getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    }

    if ((skill.isOffensive()) && (skill.isNotUnlock()))
    {
      getAI().clientStartAutoAttack();

      if (getPartner() != null) {
        getPartner().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
      }

    }

    getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

    sheduleQueuedSkill(getQueuedSkill());
  }

  private void sheduleQueuedSkill(L2PcInstance.SkillDat queuedSkill) {
    if (!isPlayer()) {
      return;
    }

    setCurrentSkill(null, false, false);
    if (queuedSkill == null) {
      return;
    }

    setQueuedSkill(null, false, false);
    ThreadPoolManager.getInstance().scheduleAi(new QueuedMagicUseTask(getPlayer(), queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()), 1L, true);
  }

  private boolean continueAttack(L2Skill skill, L2Object target, L2Object current) {
    if (!skill.isContinueAttack()) {
      return false;
    }

    if (target == null) {
      return false;
    }

    if ((!target.isL2Character()) || (target.equals(this))) {
      return false;
    }

    return current.equals(target);
  }

  public void consumeItem(int itemConsumeId, int itemCount)
  {
  }

  public void enableSkill(int skillId)
  {
    if (_disabledSkills == null) {
      return;
    }

    _disabledSkills.remove(Integer.valueOf(skillId));
    removeTimeStamp(skillId);
  }

  public void disableSkill(int skillId)
  {
    if (_disabledSkills == null) {
      _disabledSkills = new ConcurrentLinkedQueue();
    }

    _disabledSkills.add(Integer.valueOf(skillId));
  }

  public void disableSkill(int skillId, long delay)
  {
    disableSkill(skillId);
    if (delay > 10L)
      ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay, (isPlayer()) || (isPet()) || (isSummon()));
  }

  public final boolean isSkillDisabled(int skillId)
  {
    return (isAllSkillsDisabled()) || ((_disabledSkills != null) && (_disabledSkills.contains(Integer.valueOf(skillId))));
  }

  public void disableAllSkills()
  {
    _allSkillsDisabled = true;
  }

  public void enableAllSkills()
  {
    _allSkillsDisabled = false;
  }

  public void callSkill(L2Skill skill, FastList<L2Object> targets)
  {
    if ((skill == null) || (targets == null)) {
      return;
    }

    try
    {
      ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
      L2Weapon activeWeapon = getActiveWeaponItem();

      L2Object trg = null;
      FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
      {
        trg = (L2Object)n.getValue();
        if ((trg == null) || 
          (!trg.isL2Character()))
          continue;
        L2Character target = (L2Character)trg;
        if ((target.isInvul()) && (skill.isSkillTypeOffensive())) {
          sendMessage("\u0426\u0435\u043B\u044C \u043D\u0435 \u0432\u043E\u0441\u043F\u0440\u0435\u0438\u043C\u0447\u0438\u0432\u0430 \u043A \u043F\u043E\u0432\u0440\u0435\u0436\u0434\u0435\u043D\u0438\u044F\u043C");
          continue;
        }

        if ((Config.ALLOW_RAID_BOSS_PUT) && (target.isRaid()) && (skill.isSkillTypeOffensive()) && (getLevel() > target.getLevel() + 8)) {
          if (skill.isMagic()) {
            SkillTable.getInstance().getInfo(4215, 1).getEffects(this, this); continue;
          }
          SkillTable.getInstance().getInfo(4515, 1).getEffects(this, this);

          continue;
        }

        if ((skill.isOverhit()) && 
          (target.isL2Attackable())) {
          ((L2Attackable)target).overhitEnabled(true);
        }

        if ((activeWeapon != null) && (!target.isDead()) && 
          (activeWeapon.getSkillEffects(this, target, skill).length > 0) && (isPlayer())) {
          sendUserPacket(Static.SA_BUFFED_OK);
        }

        if ((getChanceSkills() != null) && 
          (Rnd.get(100) < 15)) {
          getChanceSkills().onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
        }

        if ((target.getChanceSkills() != null) && 
          (Rnd.get(100) < 15)) {
          target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
        }

        if ((target.isPlayer()) && (skill.isSkillTypeOffensive())) {
          boolean haveBuff = false;

          if ((target.getFirstEffect(445) != null) && 
            (Rnd.get(100) < Config.MIRAGE_CHANCE)) {
            getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            setTarget(null);
            abortAttack();
            abortCast();
            haveBuff = true;
          }

          if (target.isImmobileUntilAttacked()) {
            target.stopImmobileUntilAttacked(null);
          }

          if (Config.ALLOW_APELLA_BONUSES)
          {
            if ((target.getSkillLevel(3608) > 0) && 
              (target.getFirstEffect(4202) == null) && (Rnd.get(100) < 15)) {
              SkillTable.getInstance().getInfo(4202, 12).getEffects(this, this);
              haveBuff = true;
            }

            if ((target.getSkillLevel(3609) > 0) && 
              (target.getFirstEffect(4200) == null) && (Rnd.get(100) < 15)) {
              SkillTable.getInstance().getInfo(4200, 12).getEffects(this, this);
              haveBuff = true;
            }

            if ((target.getSkillLevel(3610) > 0) && 
              (target.getFirstEffect(4203) == null) && (Rnd.get(100) < 15)) {
              SkillTable.getInstance().getInfo(4203, 12).getEffects(this, this);
              haveBuff = true;
            }
          }

          if (haveBuff) {
            broadcastPacket(new MagicSkillUser(this, this, 5144, 1, 0, 0));
          }

        }

      }

      if (handler != null)
        handler.useSkill(this, skill, targets);
      else {
        skill.useSkill(this, targets);
      }

      if ((isPlayer()) || (isL2Summon())) {
        player = getPlayer();

        target = null;
        FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
          target = (L2Object)n.getValue();
          if (target == null)
          {
            continue;
          }
          if (target.isL2Character()) {
            if (skill.isOffensive()) {
              if ((target.isPlayer()) || (target.isL2Summon())) {
                if (target == player)
                {
                  continue;
                }
                if ((skill.getSkillType() != L2Skill.SkillType.AGGREDUCE) && (skill.getSkillType() != L2Skill.SkillType.AGGREDUCE_CHAR) && (skill.getSkillType() != L2Skill.SkillType.AGGREMOVE)) {
                  ((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
                }

                if (target.isPlayer())
                  target.getPlayer().getAI().clientStartAutoAttack();
                else if ((target.isL2Summon()) && 
                  (target.getOwner() != null)) {
                  target.getOwner().getAI().clientStartAutoAttack();
                }

                if ((!target.isL2Summon()) || (player.getPet() != target))
                  player.updatePvPStatus((L2Character)target);
              }
              else if ((target.isL2Attackable()) && 
                (skill.getSkillType() != L2Skill.SkillType.AGGREDUCE) && (skill.getSkillType() != L2Skill.SkillType.AGGREDUCE_CHAR) && (skill.getSkillType() != L2Skill.SkillType.AGGREMOVE)) {
                ((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
              }

            }
            else if (target.isPlayer())
            {
              if ((!target.equals(this)) && ((target.getPvpFlag() > 0) || (target.getKarma() > 0)))
                player.updatePvPStatus();
            }
            else if ((target.isL2Attackable()) && (skill.getSkillType() != L2Skill.SkillType.SUMMON) && (skill.getSkillType() != L2Skill.SkillType.BEAST_FEED) && (skill.getSkillType() != L2Skill.SkillType.UNLOCK) && (skill.getSkillType() != L2Skill.SkillType.DELUXE_KEY_UNLOCK) && ((!target.isL2Summon()) || (player.getPet() != target)))
            {
              player.updatePvPStatus((L2Character)target);
            }

            target.notifySkillUse(player, skill); continue;
          }if (target.isL2Npc()) {
            L2NpcInstance npc = (L2NpcInstance)target;
            if (npc.getTemplate().getEventQuests(Quest.QuestEventType.MOB_TARGETED_BY_SKILL) != null) {
              for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.MOB_TARGETED_BY_SKILL)) {
                quest.notifySkillUse(npc, player, skill);
              }
            }
          }
        }
        if (skill.getAggroPoints() > 0)
          for (L2Object spMob : player.getKnownList().getKnownObjects().values())
            if (spMob.isL2Npc()) {
              npcMob = (L2NpcInstance)spMob;
              if ((npcMob.isInsideRadius(player, 1000, true, true)) && (npcMob.hasAI()) && (npcMob.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK))
              {
                npcTarget = npcMob.getTarget();
                n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
                  target = (L2Object)n.getValue();
                  if ((target == null) || (
                    (npcTarget != target) && (npcMob != target))) continue;
                  npcMob.seeSpell(player, target, skill);
                }
              }
            }
      }
    }
    catch (Exception e)
    {
      L2PcInstance player;
      L2Object target;
      L2NpcInstance npcMob;
      L2Object npcTarget;
      FastList.Node n;
      _log.log(Level.WARNING, "callSkill(L2Skill skill, FastList<L2Object> targets)", e);
      e.printStackTrace();
    }
  }

  public void seeSpell(L2PcInstance caster, L2Object target, L2Skill skill)
  {
    addDamageHate(caster, 0, -skill.getAggroPoints());
  }

  public void addDamage(L2Character attacker, int damage)
  {
  }

  public void addDamageHate(L2Character attacker, int damage, int aggro)
  {
  }

  public boolean isBehind(L2Object target)
  {
    double maxAngleDiff = 45.0D;

    if (target == null) {
      return false;
    }

    if (target.isL2Character()) {
      L2Character target1 = (L2Character)target;
      double angleChar = Util.calculateAngleFrom(target1, this);
      double angleTarget = Util.convertHeadingToDegree(target1.getHeading());
      double angleDiff = angleChar - angleTarget;
      if (angleDiff <= -360.0D + maxAngleDiff) {
        angleDiff += 360.0D;
      }
      if (angleDiff >= 360.0D - maxAngleDiff) {
        angleDiff -= 360.0D;
      }
      if (Math.abs(angleDiff) <= maxAngleDiff) {
        return true;
      }
    }
    return false;
  }

  public boolean isBehindTarget() {
    return isBehind(getTarget());
  }

  public boolean isFront(L2Object target)
  {
    double maxAngleDiff = 45.0D;

    if (target == null) {
      return false;
    }

    if (target.isL2Character()) {
      L2Character target1 = (L2Character)target;
      double angleChar = Util.calculateAngleFrom(target1, this);
      double angleTarget = Util.convertHeadingToDegree(target1.getHeading());
      double angleDiff = angleChar - angleTarget;
      if (angleDiff <= -180.0D + maxAngleDiff) {
        angleDiff += 180.0D;
      }
      if (angleDiff >= 180.0D - maxAngleDiff) {
        angleDiff -= 180.0D;
      }
      if (Math.abs(angleDiff) <= maxAngleDiff) {
        return true;
      }
    }
    return false;
  }

  public boolean isFrontTarget() {
    return isFront(getTarget());
  }

  public double getLevelMod()
  {
    return 1.0D;
  }

  public final void setSkillCast(Future<?> newSkillCast) {
    _skillCast = newSkillCast;
  }

  public final void setSkillCastEndTime(int newSkillCastEndTime) {
    _castEndTime = newSkillCastEndTime;

    _castInterruptTime = (newSkillCastEndTime - 12);
  }

  public void setPvpFlagLasts(long time)
  {
    _pvpFlagLasts = time;
  }

  public long getPvpFlagLasts() {
    return _pvpFlagLasts;
  }

  public void startPvPFlag() {
    updatePvPFlag(1);

    _PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000L, 1000L);
  }

  public void stopPvpRegTask() {
    if (_PvPRegTask != null)
      _PvPRegTask.cancel(true);
  }

  public void stopPvPFlag()
  {
    stopPvpRegTask();

    updatePvPFlag(0);

    _PvPRegTask = null;
  }

  public void updatePvPFlag(int value) {
    if (!isPlayer()) {
      return;
    }
    L2PcInstance player = getPlayer();

    player.setPvpFlag(value);
    player.sendUserPacket(new UserInfo(player));

    FastList players = getKnownList().getListKnownPlayers();
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      pc.sendPacket(new RelationChanged(player, player.getRelation(player), player.isAutoAttackable(pc)));
    }
    players.clear();
    players = null;
    pc = null;
  }

  public final int getRandomDamage(L2Character target)
  {
    L2Weapon weaponItem = getActiveWeaponItem();

    if (weaponItem == null) {
      return 5 + (int)Math.sqrt(getLevel());
    }

    return weaponItem.getRandomDamage();
  }

  public String toString()
  {
    return "mob " + getObjectId();
  }

  public long getAttackEndTime() {
    return _attackEndTime;
  }

  public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
  {
    return getStat().calcStat(stat, init, target, skill);
  }

  public int getAccuracy()
  {
    return getStat().getAccuracy();
  }

  public final float getAttackSpeedMultiplier() {
    return getStat().getAttackSpeedMultiplier();
  }

  public int getCON() {
    return getStat().getCON();
  }

  public int getDEX() {
    return getStat().getDEX();
  }

  public final double getCriticalDmg(L2Character target, double init) {
    return getStat().getCriticalDmg(target, init);
  }

  public int getCriticalHit(L2Character target, L2Skill skill) {
    return getStat().getCriticalHit(target, skill);
  }

  public int getEvasionRate(L2Character target) {
    return getStat().getEvasionRate(target);
  }

  public int getINT() {
    return getStat().getINT();
  }

  public final int getMagicalAttackRange(L2Skill skill) {
    return getStat().getMagicalAttackRange(skill);
  }

  public final int getMaxCp() {
    return getStat().getMaxCp();
  }

  public int getMAtk(L2Character target, L2Skill skill) {
    return getStat().getMAtk(target, skill);
  }

  public int getMAtkSpd()
  {
    return Math.min(getStat().getMAtkSpd(), Config.MAX_MATK_SPEED);
  }

  public int getMaxMp() {
    return getStat().getMaxMp();
  }

  public int getMaxHp() {
    return getStat().getMaxHp();
  }

  public final int getMCriticalHit(L2Character target, L2Skill skill) {
    return getStat().getMCriticalHit(target, skill);
  }

  public int getMDef(L2Character target, L2Skill skill) {
    return getStat().getMDef(target, skill);
  }

  public int getMEN() {
    return getStat().getMEN();
  }

  public double getMReuseRate(L2Skill skill) {
    return getStat().getMReuseRate(skill);
  }

  public float getMovementSpeedMultiplier() {
    return getStat().getMovementSpeedMultiplier();
  }

  public int getPAtk(L2Character target) {
    return getStat().getPAtk(target);
  }

  public double getPAtkAnimals(L2Character target) {
    return getStat().getPAtkAnimals(target);
  }

  public double getPAtkDragons(L2Character target) {
    return getStat().getPAtkDragons(target);
  }

  public double getPAtkInsects(L2Character target) {
    return getStat().getPAtkInsects(target);
  }

  public double getPAtkMonsters(L2Character target) {
    return getStat().getPAtkMonsters(target);
  }

  public double getPAtkPlants(L2Character target) {
    return getStat().getPAtkPlants(target);
  }

  public int getPAtkSpd() {
    return Math.min(getStat().getPAtkSpd(), Config.MAX_PATK_SPEED);
  }

  public double getPAtkUndead(L2Character target) {
    return getStat().getPAtkUndead(target);
  }

  public double getPDefUndead(L2Character target) {
    return getStat().getPDefUndead(target);
  }

  public double getPAtkValakas(L2Character target) {
    return getStat().getPAtkValakas(target);
  }

  public double getPDefValakas(L2Character target) {
    return getStat().getPDefValakas(target);
  }

  public int getPDef(L2Character target) {
    return getStat().getPDef(target);
  }

  public final int getPhysicalAttackRange() {
    return getStat().getPhysicalAttackRange();
  }

  public int getRunSpeed() {
    return getStat().getRunSpeed();
  }

  public final int getShldDef() {
    return getStat().getShldDef();
  }

  public int getSTR() {
    return getStat().getSTR();
  }

  public final int getWalkSpeed() {
    return getStat().getWalkSpeed();
  }

  public int getWIT() {
    return getStat().getWIT();
  }

  public double getMAtk() {
    return getStat().getMAtk(null, null);
  }

  public double getMDef() {
    return getStat().getMDef(null, null);
  }

  public void reduceCurrentHp(double i, L2Character attacker)
  {
    reduceCurrentHp(i, attacker, true);
  }

  public void reduceNpcHp(double i, L2Character attacker) {
    getStatus().reduceNpcHp(i, attacker, true);
  }

  public void reduceCurrentHp(double i, L2Character attacker, boolean awake) {
    if ((Config.L2JMOD_CHAMPION_ENABLE) && (isChampion()) && (Config.L2JMOD_CHAMPION_HP != 0))
      getStatus().reduceHp(i / Config.L2JMOD_CHAMPION_HP, attacker, awake);
    else
      getStatus().reduceHp(i, attacker, awake);
  }

  public void reduceCurrentMp(double i)
  {
    getStatus().reduceMp(i);
  }

  public void addStatusListener(L2Character object)
  {
    getStatus().addStatusListener(object);
  }

  public void removeStatusListener(L2Character object)
  {
    getStatus().removeStatusListener(object);
  }

  protected void stopHpMpRegeneration() {
    getStatus().stopHpMpRegeneration();
  }

  public final double getCurrentCp()
  {
    return getStatus().getCurrentCp();
  }

  public final void setCurrentCp(Double newCp) {
    setCurrentCp(newCp.doubleValue());
  }

  public final void setCurrentCp(double newCp) {
    getStatus().setCurrentCp(newCp);
  }

  public final double getCurrentHp() {
    return getStatus().getCurrentHp();
  }

  public final void setCurrentHp(double newHp) {
    getStatus().setCurrentHp(newHp);
  }

  public final void setCurrentHpMp(double newHp, double newMp) {
    getStatus().setCurrentHpMp(newHp, newMp);
  }

  public final double getCurrentMp() {
    return getStatus().getCurrentMp();
  }

  public final void setCurrentMp(Double newMp) {
    setCurrentMp(newMp.doubleValue());
  }

  public final void setCurrentMp(double newMp) {
    getStatus().setCurrentMp(newMp);
  }

  public void setAiClass(String aiClass)
  {
    _aiClass = aiClass;
  }

  public String getAiClass() {
    return _aiClass;
  }

  public L2Character getLastBuffer() {
    return _lastBuffer;
  }

  public void setChampion(boolean champ) {
    _champion = champ;
  }

  public boolean isChampion() {
    return _champion;
  }

  public int getLastHealAmount() {
    return _lastHealAmount;
  }

  public void setLastBuffer(L2Character buffer) {
    _lastBuffer = buffer;
  }

  public void setLastHealAmount(int hp) {
    _lastHealAmount = hp;
  }

  public boolean reflectSkill(L2Skill skill)
  {
    double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0.0D, null, null);
    return Rnd.get(100) < reflect;
  }

  public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
  {
  }

  public ForceBuff getForceBuff()
  {
    return null;
  }

  public final void addChanceSkill(L2Skill skill) {
    if (_chanceSkills == null) {
      _chanceSkills = new ChanceSkillList(this);
    }
    _chanceSkills.put(skill, skill.getChanceCondition());
  }

  public final void removeChanceSkill(L2Skill skill) {
    _chanceSkills.remove(skill);
    if (_chanceSkills.size() == 0)
      _chanceSkills = null;
  }

  public final void setVis(boolean f)
  {
    _vis = f;
    L2PcInstance pc = null;
    FastList players = getKnownList().getKnownPlayersInRadius(1200);
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null) {
        continue;
      }
      pc.sendPacket(new NpcInfo((L2NpcInstance)this, pc));
    }
    players.clear();
    players = null;
    pc = null;
  }

  public boolean isVis() {
    return _vis;
  }

  public void increaseCharges()
  {
    _numCharges += 1;

    if (isPlayer()) {
      sendEtcStatusUpdate();
      if (_numCharges == 7)
        sendUserPacket(Static.FORCE_MAXIMUM);
      else
        sendUserPacket(SystemMessage.id(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_numCharges));
    }
  }

  public void decreaseCharges(int decrease)
  {
    _numCharges -= decrease;
    sendEtcStatusUpdate();
  }

  public void clearCharges() {
    _numCharges = 0;
    sendEtcStatusUpdate();
  }

  public int getCharges() {
    return _numCharges;
  }

  public boolean isDebuffImmun()
  {
    return (isRaid()) || (isInvul()) || (getFirstEffect(1411) != null) || (isCursedWeaponEquiped());
  }

  public boolean isDebuffImmun(L2Skill skill)
  {
    if (getFirstEffect(skill.getId()) != null) {
      return true;
    }

    if ((getFirstEffect(1411) != null) && (skill.hasEffects())) {
      return true;
    }

    return (skill.isDebuff()) && (isDebuffImmun());
  }

  public double calcMENModifier()
  {
    return Formulas.calcMENModifier(this);
  }

  public double calcCONModifier() {
    return Formulas.calcCONModifier(this);
  }

  public double calcSkillResistans(L2Skill skill, L2Skill.SkillType type, L2Skill.SkillType deftype) {
    return Formulas.calcSkillResistans(this, skill, type, deftype);
  }

  public double calcSkillVulnerability(L2Skill skill) {
    return Formulas.calcSkillVulnerability(this, skill);
  }

  public boolean isEnemyForMob(L2Attackable mob)
  {
    return false;
  }

  public boolean isInWater() {
    return isInsideZone(128);
  }

  public boolean isInDerbyTrack() {
    return isInsideZone(512);
  }

  public boolean isInsidePvpZone()
  {
    return isInsideZone(1);
  }

  public boolean isInsideNoLandZone() {
    return isInsideZone(64);
  }

  public int calcHeading(int x_dest, int y_dest)
  {
    return (int)(Math.atan2(getY() - y_dest, getX() - x_dest) * 10430.378350470453D) + 32768;
  }

  public boolean isSitting() {
    return false;
  }

  public boolean isUnlockable() {
    return false;
  }

  public boolean isTyranosurus() {
    return false;
  }

  public boolean isAngel() {
    return false;
  }

  public void fullRestore()
  {
    fullRestore(true);
  }

  public void fullRestore(boolean self) {
    if (self) {
      if (System.currentTimeMillis() - _lastRestore < 5000L) {
        sNotReady();
        return;
      }
      _lastRestore = System.currentTimeMillis();
    }

    broadcastPacket(new MagicSkillUser(this, this, 2241, 1, 1000, 0));
    setCurrentHpMp(getMaxHp(), getMaxMp());
    setCurrentCp(getMaxCp());

    sendUserPacket(Static.FULL_RESTORE);
  }

  public void stopAllEffectsB()
  {
    stopAllEffectsB(true);
  }

  public void stopAllEffectsB(boolean self) {
    if (self) {
      if (System.currentTimeMillis() - _lastStop < 5000L) {
        sNotReady();
        return;
      }
      _lastStop = System.currentTimeMillis();
    }

    broadcastPacket(new MagicSkillUser(this, this, 2243, 1, 1000, 0));
    stopAllEffects();

    sendUserPacket(Static.BUFFS_CANCEL);
  }

  public void doRebuff()
  {
    doRebuff(true);
  }

  public void doRebuff(boolean self) {
    if (self) {
      if (System.currentTimeMillis() - _lastRebuff < 5000L) {
        sNotReady();
        return;
      }
      _lastRebuff = System.currentTimeMillis();
    }

    FastTable effects = getAllEffectsTable();
    if (effects.isEmpty()) {
      sendUserPacket(Static.OOPS_ERROR);
      return;
    }
    stopAllEffects();

    SkillTable _st = SkillTable.getInstance();
    broadcastPacket(new MagicSkillUser(this, this, 2242, 1, 1000, 0));

    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if ((e.getSkill().isForbiddenProfileSkill()) || (e.getSkill().getSkillType() != L2Skill.SkillType.BUFF) || (e.getSkill().isChance()))
      {
        continue;
      }

      _st.getInfo(e.getSkill().getId(), e.getSkill().getLevel()).getEffects(this, this);
    }

    sendUserPacket(Static.BUFFS_UPDATE);
  }

  public void doFullBuff(int type)
  {
    if (System.currentTimeMillis() - _fullRebuff < 5000L) {
      sNotReady();
      return;
    }
    _fullRebuff = System.currentTimeMillis();

    FastMap buffs = null;
    switch (type) {
    case 1:
      if (Config.F_BUFF.isEmpty()) {
        return;
      }
      buffs = Config.F_BUFF;
      break;
    case 2:
      if (Config.M_BUFF.isEmpty()) {
        return;
      }
      buffs = Config.M_BUFF;
    }

    stopAllEffects();
    SkillTable _st = SkillTable.getInstance();
    FastMap.Entry e = buffs.head(); for (FastMap.Entry end = buffs.tail(); (e = e.getNext()) != end; ) {
      Integer id = (Integer)e.getKey();
      Integer lvl = (Integer)e.getValue();
      if ((id == null) || (lvl == null))
      {
        continue;
      }
      _st.getInfo(id.intValue(), lvl.intValue()).getEffects(this, this);
    }
  }

  public void sNotReady()
  {
    sendUserPacket(Static.PLEASE_WAIT);
    sendActionFailed();
  }

  public boolean hasClanWarWith(L2Character cha) {
    if ((getClan() == null) || (cha.getClan() == null)) {
      return false;
    }

    if ((isAcademyMember()) || (cha.isAcademyMember())) {
      return false;
    }

    return (getClan().isAtWarWith(cha.getClan())) && (cha.getClan().isAtWarWith(getClan()));
  }

  public boolean canExp() {
    return !isDead();
  }

  public L2Armor getActiveChestArmorItem()
  {
    return null;
  }

  public boolean isWearingHeavyArmor() {
    return false;
  }

  public boolean isWearingLightArmor() {
    return false;
  }

  public boolean isWearingMagicArmor() {
    return false;
  }

  public boolean isCursedWeaponEquiped() {
    return false;
  }

  public void setPVPArena(boolean f)
  {
  }

  public void startWaterTask(int waterZone)
  {
  }

  public void stopWaterTask(int waterZone)
  {
  }

  public void rechargeAutoSoulShot(boolean a, boolean b, boolean c) {
  }

  public boolean geoPathfind() {
    return Config.GEODATA == 2;
  }

  public boolean getShowSkillChances() {
    return false;
  }

  public void sendMessage(String txt)
  {
  }

  public void setInCastleZone(boolean f)
  {
  }

  public int getClanId() {
    return 0;
  }

  public void setInSiegeFlagArea(boolean f)
  {
  }

  public void setInSiegeRuleArea(boolean f)
  {
  }

  public int getRelation(L2PcInstance target) {
    return 0;
  }

  public boolean isFestivalParticipant() {
    return false;
  }

  public boolean isMounted() {
    return false;
  }

  public ClassId getClassId() {
    return null;
  }

  public void setEventWait(boolean f)
  {
  }

  public void doNpcChat(int type, String name)
  {
  }

  public void setInDino(boolean f)
  {
  }

  public void sendAdmResultMessage(String txt)
  {
  }

  public void setInPvpFarmZone(boolean f)
  {
  }

  public void sendEtcStatusUpdate()
  {
  }

  public void updateAndBroadcastStatus(int broadcastType)
  {
  }

  public void updatePvPStatus()
  {
  }

  public void updatePvPStatus(L2Character target)
  {
  }

  public boolean inObserverMode() {
    return false;
  }

  public void setChargedSoulShot(int shotType)
  {
  }

  public int getChargedSoulShot() {
    return 0;
  }

  public int getChargedSpiritShot() {
    return 0;
  }

  public void broadcastUserInfo()
  {
  }

  public Duel getDuel() {
    return null;
  }

  public void setRecentFakeDeath(boolean f)
  {
  }

  public void revalidateZone(boolean f)
  {
  }

  public void logout()
  {
  }

  public L2Clan getClan() {
    return null;
  }

  public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
  {
  }

  public L2PcInstance.SkillDat getQueuedSkill() {
    return null;
  }

  public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
  {
  }

  public boolean isEventMob() {
    return false;
  }

  public void setChannel(int channel)
  {
  }

  public boolean isMageClass() {
    return false;
  }

  public boolean isReviveRequested() {
    return false;
  }

  public boolean isRevivingPet() {
    return false;
  }

  public boolean isGM() {
    return false;
  }

  public boolean isReturningToSpawnPoint() {
    return false;
  }

  public boolean isAcademyMember() {
    return false;
  }

  public void setFollowStatus(boolean state)
  {
  }

  public double calcMDefMod(double value)
  {
    return value;
  }

  public double calcPDefMod(double value) {
    return value;
  }

  public double calcAtkAccuracy(double value) {
    return value;
  }

  public double calcAtkCritical(double value, double dex) {
    value *= dex * 10.0D;
    return value;
  }

  public double calcMAtkCritical(double value, double wit) {
    return value * wit;
  }

  public double calcBlowDamageMul() {
    return 1.0D;
  }

  public int getRegeneratePeriod()
  {
    return 3000;
  }

  public int getHennaStatINT()
  {
    return 0;
  }

  public int getHennaStatSTR() {
    return 0;
  }

  public int getHennaStatCON() {
    return 0;
  }

  public int getHennaStatMEN() {
    return 0;
  }

  public int getHennaStatWIT() {
    return 0;
  }

  public int getHennaStatDEX() {
    return 0;
  }

  public boolean isOverlord()
  {
    return false;
  }

  public boolean isSilentMoving() {
    return false;
  }

  public L2PcInstance getPlayer()
  {
    return null;
  }

  public boolean isL2Character()
  {
    return true;
  }

  public L2Character getL2Character()
  {
    return this;
  }

  public void onKillUpdatePvPKarma(L2Character target)
  {
  }

  public void updateLastTeleport(boolean f)
  {
  }

  public boolean isNoblesseBlessed() {
    return false;
  }

  public void stopNoblesseBlessing(L2Effect effect)
  {
  }

  public boolean getCharmOfLuck() {
    return false;
  }

  public void stopCharmOfLuck(L2Effect effect)
  {
  }

  public boolean isPhoenixBlessed() {
    return false;
  }

  public boolean isFantome() {
    return false;
  }

  public void rndWalk()
  {
  }

  public boolean rndWalk(L2Character target, boolean fake) {
    return false;
  }

  public void clearRndWalk()
  {
  }

  public void teleToClosestTown()
  {
  }

  public void sayString(String text, int type) {
    broadcastPacket(new CreatureSay(getObjectId(), type, getName(), text));
  }

  public Location getFakeLoc() {
    return null;
  }

  public boolean getFollowStatus() {
    return false;
  }

  public L2Summon getL2Summon() {
    return null;
  }

  public L2PcInstance getPartner() {
    return null;
  }

  public boolean isPartner() {
    return false;
  }

  public void updateAndBroadcastPartnerStatus(int val)
  {
  }

  public int getPartnerClass() {
    return 0;
  }

  public boolean teleToLocation(Location loc) {
    return false;
  }

  public void setInAqZone(boolean f)
  {
  }

  public void setInsideSilenceZone(boolean f)
  {
  }

  public void setInHotZone(boolean f)
  {
  }

  public int isOnline() {
    return 0;
  }

  public void checkHpMessages(double curHp, double newHp)
  {
  }

  public boolean isInDuel() {
    return false;
  }

  public void refreshSavedStats()
  {
  }

  public void sendChanges()
  {
  }

  public void setHippy(boolean hippy)
  {
  }

  public boolean isHippy() {
    return false;
  }

  public boolean isHero() {
    return false;
  }

  public boolean hasItems(FastList<Integer> items) {
    return false;
  }

  public PcInventory getPcInventory() {
    return null;
  }

  public void setFreePvp(boolean f)
  {
  }

  public static class MoveData
  {
    public int _moveTimestamp;
    public int _xDestination;
    public int _yDestination;
    public int _zDestination;
    public int _xMoveFrom;
    public int _yMoveFrom;
    public int _zMoveFrom;
    public double _xAccurate;
    public double _yAccurate;
    public double _zAccurate;
    public int _heading;
    public int _moveStartTime;
    public int _ticksToMove;
    public float _xSpeedTicks;
    public float _ySpeedTicks;
    public int onGeodataPathIndex;
    public FastTable<AbstractNodeLoc> geoPath;
    public int geoPathAccurateTx;
    public int geoPathAccurateTy;
    public int geoPathGtx;
    public int geoPathGty;
  }

  public class AIAccessor
  {
    public AIAccessor()
    {
    }

    public L2Character getActor()
    {
      return L2Character.this;
    }

    public void moveTo(int x, int y, int z, int offset)
    {
      moveToLocation(x, y, z, offset);
    }

    public void moveTo(int x, int y, int z)
    {
      moveToLocation(x, y, z, 0);
    }

    public void stopMove(L2CharPosition pos)
    {
      L2Character.this.stopMove(pos);
    }

    public void doAttack(L2Character target)
    {
      L2Character.this.doAttack(target);
    }

    public void doCast(L2Skill skill)
    {
      L2Character.this.doCast(skill);
    }

    public L2Character.NotifyAITask newNotifyTask(CtrlEvent evt)
    {
      return new L2Character.NotifyAITask(L2Character.this, evt);
    }

    public void detachAI()
    {
      _ai = null;
    }
  }

  class PvPFlag
    implements Runnable
  {
    public PvPFlag()
    {
    }

    public void run()
    {
      try
      {
        if (System.currentTimeMillis() > getPvpFlagLasts())
          stopPvPFlag();
        else if (System.currentTimeMillis() > getPvpFlagLasts() - 20000L)
          updatePvPFlag(2);
        else {
          updatePvPFlag(1);
        }
      }
      catch (Exception e)
      {
        L2Character._log.log(Level.WARNING, "error in pvp flag task:", e);
      }
    }
  }

  public class NotifyAITask
    implements Runnable
  {
    private final CtrlEvent _evt;

    public NotifyAITask(CtrlEvent evt)
    {
      _evt = evt;
    }

    public void run() {
      try {
        getAI().notifyEvent(_evt, null);
      } catch (Throwable t) {
        L2Character._log.log(Level.WARNING, "", t);
      }
    }
  }

  static class QueuedMagicUseTask
    implements Runnable
  {
    L2PcInstance _currPlayer;
    L2Skill _queuedSkill;
    boolean _isCtrlPressed;
    boolean _isShiftPressed;

    public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
    {
      _currPlayer = currPlayer;
      _queuedSkill = queuedSkill;
      _isCtrlPressed = isCtrlPressed;
      _isShiftPressed = isShiftPressed;
    }

    public void run()
    {
      _currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
    }
  }

  class MagicUseTask
    implements Runnable
  {
    FastList<L2Object> _targets = new FastList();
    L2Skill _skill;
    int _hitTime;
    int _coolTime;
    int _phase;

    public MagicUseTask(L2Skill targets, int skill, int coolTime, int phase)
    {
      _hitTime = hitTime;
      _targets.addAll(targets);
      _skill = skill;
      _coolTime = coolTime;
      _phase = phase;
    }

    public void run() {
      try {
        switch (_phase) {
        case 1:
          onMagicLaunchedTimer(_targets, _skill, _coolTime, false);
          break;
        case 2:
          onMagicHitTimer(_targets, _skill, _coolTime, false, _hitTime);

          break;
        case 3:
          onMagicFinalizer(_skill, (L2Object)_targets.getFirst());
        }
      }
      catch (Throwable e) {
        L2Character._log.log(Level.SEVERE, "", e);
        enableAllSkills();
      }
    }
  }

  class HitTask
    implements Runnable
  {
    L2Character _hitTarget;
    int _damage;
    boolean _crit;
    boolean _miss;
    boolean _shld;
    boolean _soulshot;

    public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
    {
      _hitTarget = target;
      _damage = damage;
      _crit = crit;
      _shld = shld;
      _miss = miss;
      _soulshot = soulshot;
    }

    public void run() {
      try {
        onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
        getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
      } catch (Throwable e) {
        L2Character._log.severe(e.toString());
      }
    }
  }

  class EnableSkill
    implements Runnable
  {
    int _skillId;

    public EnableSkill(int skillId)
    {
      _skillId = skillId;
    }

    public void run() {
      try {
        enableSkill(_skillId);
      } catch (Throwable e) {
        L2Character._log.log(Level.SEVERE, "", e);
      }
    }
  }
}