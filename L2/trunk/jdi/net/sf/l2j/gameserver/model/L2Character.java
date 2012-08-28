package net.sf.l2j.gameserver.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.geodata.pathfind.AbstractNodeLoc;
import net.sf.l2j.gameserver.geodata.pathfind.PathFinding;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcWalkerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.actor.status.CharStatus;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.Attack;
import net.sf.l2j.gameserver.network.serverpackets.BeginRotation;
import net.sf.l2j.gameserver.network.serverpackets.ChangeMoveType;
import net.sf.l2j.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.CharMoveToLocation;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TargetUnselected;
import net.sf.l2j.gameserver.network.serverpackets.TeleportToLocation;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;

public abstract class L2Character extends L2Object
{
  protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
  private volatile List<L2Character> _attackByList;
  private boolean _MyIsAttacking = false;
  private boolean _MyIsMoveCast = false;
  private L2Skill _lastSkillCast;
  private boolean _isAfraid = false;
  private boolean _isRaid = false;
  private boolean _isConfused = false;
  private boolean _isFakeDeath = false;
  private boolean _isFlying = false;
  private boolean _isFallsdown = false;
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
  private boolean _isSleeping = false;
  private boolean _isMeditation = false;
  private boolean _isStunned = false;
  private boolean _isAttackDisable = false;
  private boolean _isBetrayed = false;
  protected boolean _showSummonAnimation = false;
  protected boolean _isTeleporting = false;
  private L2Character _lastBuffer = null;
  protected boolean _isInvul = false;
  protected byte _zoneValidateCounter = 4;
  private int _lastHealAmount = 0;
  private int[] lastPosition = { 0, 0, 0 };
  private CharStat _stat;
  private CharStatus _status;
  private L2CharTemplate _template;
  private String _title;
  private String _aiClass = "default";
  private double _hpUpdateIncCheck = 0.0D;
  private double _hpUpdateDecCheck = 0.0D;
  private double _hpUpdateInterval = 0.0D;
  private boolean _isBuffBlocked = false;
  private Calculator[] _calculators;
  protected final Map<Integer, L2Skill> _skills;
  protected volatile ChanceSkillList _chanceSkills;
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
  public static final int ZONE_SWAMP = 2048;
  public static final int ZONE_NOSUMMONFRIEND = 4096;
  public static final int ZONE_OLY = 8192;
  public static final int ZONE_BOSS = 16384;
  public static final int ZONE_TRADE = 32768;
  private boolean _canCastAA;
  private int _currentZones = 0;
  private int _AbnormalEffects;
  private FastTable<L2Effect> _effects;
  protected Map<String, List<L2Effect>> _stackedEffects;
  private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
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
  protected List<Integer> _disabledSkills;
  private boolean _allSkillsDisabled;
  protected MoveData _move;
  private int _heading;
  private L2Object _target;
  private int _castEndTime;
  private int _castInterruptTime;
  private boolean _isCastNow = false;
  private int _attackEndTime;
  private int _attacking;
  private int _disableBowAttackEndTime;
  private L2NpcTemplate _lastPetNT = null;
  private L2ItemInstance _lastPetItem = null;

  private static final Calculator[] NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();
  protected L2CharacterAI _ai;
  protected Future _skillCast;
  private int _clientX;
  private int _clientY;
  private int _clientZ;
  private int _clientHeading;
  private List<QuestState> _NotifyQuestOfDeathList = new FastList();
  private Future _PvPRegTask;
  private long _pvpFlagLasts;
  private boolean _isMinion = false;

  private boolean _champion = false;
  private int _premiumsystem;

  public void setIsMoveCast(boolean value)
  {
    _MyIsMoveCast = value;
  }

  public boolean isInsideZone(int zone)
  {
    return (_currentZones & zone) != 0;
  }

  public void setInsideZone(int zone, boolean state) {
    if (state)
      _currentZones |= zone;
    else if (isInsideZone(zone))
      _currentZones ^= zone;
  }

  public L2Character(int objectId, L2CharTemplate template)
  {
    super(objectId);
    getKnownList();

    _template = template;

    if ((template != null) && ((this instanceof L2NpcInstance)))
    {
      _calculators = NPC_STD_CALCULATOR;

      _skills = ((L2NpcTemplate)template).getSkills();
      if (_skills != null)
      {
        for (Map.Entry skill : _skills.entrySet())
          addStatFuncs(((L2Skill)skill.getValue()).getStatFuncs(null, this));
      }
    }
    else
    {
      _skills = new FastMap().setShared(true);

      _calculators = new Calculator[Stats.NUM_STATS];
      Formulas.getInstance().addFuncsToNewCharacter(this);
    }
  }

  protected void initCharStatusUpdateValues()
  {
    _hpUpdateInterval = (getMaxHp() / 352.0D);
    _hpUpdateIncCheck = getMaxHp();
    _hpUpdateDecCheck = (getMaxHp() - _hpUpdateInterval);
  }

  public void onDecay()
  {
    L2WorldRegion reg = getWorldRegion();
    if (reg != null) reg.removeFromZones(this);
    decayMe();
  }

  public void onSpawn()
  {
    super.onSpawn();
    revalidateZone(true);
  }

  public void onTeleported()
  {
    if (!isTeleporting()) {
      return;
    }
    spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());

    lastPosition[0] = getPosition().getX();
    lastPosition[1] = getPosition().getY();
    lastPosition[2] = getPosition().getZ();

    setIsTeleporting(false);

    if (_isPendingRevive)
    {
      doRevive(false);
    }

    if (getPet() != null)
    {
      getPet().setFollowStatus(false);
      getPet().teleToLocation(getPosition().getX() + Rnd.get(-100, 100), getPosition().getY() + Rnd.get(-100, 100), getPosition().getZ(), false);
      ((L2SummonAI)getPet().getAI()).setStartFollowController(true);
      getPet().setFollowStatus(true);
    }
  }

  public void addAttackerToAttackByList(L2Character player)
  {
    if ((player == null) || (player == this) || (getAttackByList() == null) || (getAttackByList().contains(player))) return;
    getAttackByList().add(player);
  }

  public final void broadcastPacket(L2GameServerPacket mov)
  {
    if (!(mov instanceof CharInfo)) {
      sendPacket(mov);
    }
    for (L2PcInstance player : getKnownList().getKnownPlayers().values())
    {
      try
      {
        player.sendPacket(mov);
        if (((mov instanceof CharInfo)) && ((this instanceof L2PcInstance))) {
          int relation = ((L2PcInstance)this).getRelation(player);
          if ((getKnownList().getKnownRelations().get(Integer.valueOf(player.getObjectId())) != null) && (((Integer)getKnownList().getKnownRelations().get(Integer.valueOf(player.getObjectId()))).intValue() != relation))
            player.sendPacket(new RelationChanged((L2PcInstance)this, relation, player.isAutoAttackable(this)));
        }
      } catch (NullPointerException e) {
      }
    }
  }

  public final void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist) {
    if (!(mov instanceof CharInfo)) {
      sendPacket(mov);
    }
    for (L2PcInstance player : getKnownList().getKnownPlayers().values())
    {
      try
      {
        if (isInsideRadius(player, radiusInKnownlist, false, false)) {
          player.sendPacket(mov);
          if (((mov instanceof CharInfo)) && ((this instanceof L2PcInstance))) {
            int relation = ((L2PcInstance)this).getRelation(player);
            if ((getKnownList().getKnownRelations().get(Integer.valueOf(player.getObjectId())) != null) && (((Integer)getKnownList().getKnownRelations().get(Integer.valueOf(player.getObjectId()))).intValue() != relation))
              player.sendPacket(new RelationChanged((L2PcInstance)this, relation, player.isAutoAttackable(this)));
          }
        }
      }
      catch (NullPointerException e)
      {
      }
    }
  }

  protected boolean needHpUpdate(int barPixels)
  {
    double currentHp = getCurrentHp();

    if ((currentHp <= 1.0D) || (getMaxHp() < barPixels)) {
      return true;
    }
    if ((currentHp <= _hpUpdateDecCheck) || (currentHp >= _hpUpdateIncCheck))
    {
      if (currentHp == getMaxHp())
      {
        _hpUpdateIncCheck = (currentHp + 1.0D);
        _hpUpdateDecCheck = (currentHp - _hpUpdateInterval);
      }
      else
      {
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
    if (getStatus().getStatusListener().isEmpty()) return;

    if (!needHpUpdate(352)) {
      return;
    }

    StatusUpdate su = new StatusUpdate(getObjectId());
    su.addAttribute(9, (int)getCurrentHp());
    su.addAttribute(11, (int)getCurrentMp());

    synchronized (getStatus().getStatusListener())
    {
      for (L2Character temp : getStatus().getStatusListener())
        try {
          temp.sendPacket(su);
        }
        catch (NullPointerException e)
        {
        }
    }
  }

  public void sendPacket(L2GameServerPacket mov)
  {
  }

  public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
  {
    stopMove(null, false);
    abortAttack();
    abortCast();
    isFalling(false, 0);
    setIsTeleporting(true);
    setTarget(null);

    for (L2Character character : getKnownList().getKnownCharacters())
    {
      if (character.getTarget() == this)
      {
        character.stopMove(null, false);
        character.abortAttack();
        character.abortCast();
        character.setTarget(null);
      }

    }

    if (getWorldRegion() != null)
    {
      getWorldRegion().removeFromZones(this);
    }

    getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

    if ((Config.RESPAWN_RANDOM_ENABLED) && (allowRandomOffset))
    {
      x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
      y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
    }

    z += 5;

    broadcastPacket(new TeleportToLocation(this, x, y, z));

    getPosition().setXYZ(x, y, z);

    decayMe();
    isFalling(false, 0);
    if ((!(this instanceof L2PcInstance)) || ((((L2PcInstance)this).getClient() != null) && (((L2PcInstance)this).getClient().isDetached())))
    {
      onTeleported();
    }
  }
  public void teleToLocation(int x, int y, int z) {
    teleToLocation(x, y, z, false);
  }

  public void teleToLocation(Location loc, boolean allowRandomOffset) {
    int x = loc.getX();
    int y = loc.getY();
    int z = loc.getZ();

    if (((this instanceof L2PcInstance)) && (DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)))
    {
      L2PcInstance player = (L2PcInstance)this;
      player.sendMessage("You have been sent to the waiting room.");
      if ((player.isInParty()) && (player.getParty().isInDimensionalRift()))
      {
        player.getParty().getDimensionalRift().usedTeleport(player);
      }
      int[] newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
      x = newCoords[0];
      y = newCoords[1];
      z = newCoords[2];
    }
    teleToLocation(x, y, z, allowRandomOffset);
  }
  public void teleToLocation(MapRegionTable.TeleportWhereType teleportWhere) {
    teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
  }

  public int isFalling(boolean falling, int fallHeight)
  {
    if ((isFallsdown()) && (fallHeight == 0)) {
      return -1;
    }
    if ((!falling) || ((lastPosition[0] == 0) && (lastPosition[1] == 0) && (lastPosition[2] == 0)))
    {
      lastPosition = new int[] { getClientX(), getClientY(), getClientZ() };
      setIsFallsdown(false);
      return -1;
    }

    int moveChangeX = Math.abs(lastPosition[0] - getClientX());
    int moveChangeY = Math.abs(lastPosition[1] - getClientY());
    int moveChangeZ = Math.max(lastPosition[2] - getClientZ(), lastPosition[2] - getZ());

    if ((moveChangeZ > fallSafeHeight()) && (moveChangeY < moveChangeZ) && (moveChangeX < moveChangeZ) && (!isFlying()))
    {
      setIsFallsdown(true);
      fallHeight += moveChangeZ;

      lastPosition = new int[] { getClientX(), getClientY(), getClientZ() };
      getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

      CheckFalling cf = new CheckFalling(fallHeight);
      cf.setTask(ThreadPoolManager.getInstance().scheduleGeneral(cf, Math.min(1200, moveChangeZ)));

      return fallHeight;
    }
    lastPosition = new int[] { getClientX(), getClientY(), getClientZ() };
    getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

    if (fallHeight > fallSafeHeight())
    {
      doFallDamage(fallHeight);
      return fallHeight;
    }

    return -1;
  }

  private int fallSafeHeight()
  {
    int safeFallHeight = Config.ALT_MINIMUM_FALL_HEIGHT;
    try
    {
      if ((this instanceof L2PcInstance))
      {
        safeFallHeight = ((L2PcInstance)this).getTemplate().getBaseFallSafeHeight(((L2PcInstance)this).getAppearance().getSex());
      }

    }
    catch (Throwable t)
    {
      _log.log(Level.SEVERE, "Template Missing : ", t);
    }

    return safeFallHeight;
  }

  private int getFallDamage(int fallHeight)
  {
    int damage = (fallHeight - fallSafeHeight()) * 2;
    damage = (int)(damage / getStat().calcStat(Stats.FALL_VULN, 1.0D, this, null));
    if (damage >= getStatus().getCurrentHp())
    {
      damage = (int)(getStatus().getCurrentHp() - 1.0D);
    }

    disableAllSkills();

    ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
    {
      public void run()
      {
        enableAllSkills();

        setIsFallsdown(false);

        L2Character.access$002(L2Character.this, new int[] { getX(), getY(), getZ() });
        setClientX(lastPosition[0]);
        setClientY(lastPosition[1]);
        setClientZ(lastPosition[2]);
      }
    }
    , 250L);

    return damage;
  }

  private void doFallDamage(int fallHeight)
  {
    isFalling(false, 0);

    if ((this instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)this;

      if ((player.isInvul()) || (player.isInFunEvent()))
      {
        setIsFallsdown(false);
        return;
      }
    }

    int damage = getFallDamage(fallHeight);

    if (damage < 1) {
      return;
    }
    if ((this instanceof L2PcInstance))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.FALL_DAMAGE_S1);
      sm.addNumber(damage);
      sendPacket(sm);
    }

    getStatus().reduceHp(damage, this);
    getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
  }

  protected void doAttack(L2Character target)
  {
    if ((isAlikeDead()) || (target == null) || (((this instanceof L2NpcInstance)) && (target.isAlikeDead())) || (((this instanceof L2PcInstance)) && (target.isDead()) && (!target.isFakeDeath())) || (!getKnownList().knowsObject(target)) || (((this instanceof L2PcInstance)) && (isDead())) || (((target instanceof L2PcInstance)) && (((L2PcInstance)target).getDuelState() == 2)) || (Formulas.getInstance().canCancelAttackerTarget(this, target)))
    {
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

      sendPacket(new ActionFailed());
      return;
    }

    if ((isAttackingDisabled()) || (_MyIsAttacking)) {
      return;
    }

    if ((this instanceof L2PcInstance))
    {
      if (((L2PcInstance)this).inObserverMode())
      {
        sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
        sendPacket(new ActionFailed());
        return;
      }

      if ((target instanceof L2PcInstance))
      {
        if (target.isInsidePeaceZone((L2PcInstance)this))
        {
          sendPacket(new ActionFailed());
          return;
        }

        if (target.getObjectId() == getObjectId())
        {
          sendPacket(new ActionFailed());
          return;
        }

        if ((((L2PcInstance)target).isCursedWeaponEquiped()) && (((L2PcInstance)this).getLevel() <= 20)) {
          ((L2PcInstance)this).sendMessage("Can't attack a cursed player when under level 21.");
          sendPacket(new ActionFailed());
          return;
        }

        if ((((L2PcInstance)this).isCursedWeaponEquiped()) && (((L2PcInstance)target).getLevel() <= 20)) {
          ((L2PcInstance)this).sendMessage("Can't attack a newbie player using a cursed weapon.");
          sendPacket(new ActionFailed());
          return;
        }
      }

    }

    L2ItemInstance weaponInst = getActiveWeaponInstance();

    L2Weapon weaponItem = getActiveWeaponItem();

    if ((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.ROD))
    {
      ((L2PcInstance)this).sendPacket(new SystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
      getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      sendPacket(new ActionFailed());
      return;
    }

    if ((!(target instanceof L2DoorInstance)) && (!GeoData.getInstance().canSeeTarget(this, target)))
    {
      sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      sendPacket(new ActionFailed());
      return;
    }

    if ((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.BOW))
    {
      if ((this instanceof L2PcInstance))
      {
        if (target.isInsidePeaceZone((L2PcInstance)this))
        {
          getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
          sendPacket(new ActionFailed());
          return;
        }

        if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
        {
          int saMpConsume = (int)getStat().calcStat(Stats.MP_CONSUME, 0.0D, null, null);
          int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;

          if (getCurrentMp() < mpConsume)
          {
            ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000L);

            sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
            sendPacket(new ActionFailed());
            return;
          }

          getStatus().reduceMp(mpConsume);

          _disableBowAttackEndTime = (50 + GameTimeController.getGameTicks());
        }
        else
        {
          ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000L);

          sendPacket(new ActionFailed());
          return;
        }

        if (!checkAndEquipArrows())
        {
          getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

          sendPacket(new ActionFailed());
          sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
          return;
        }
      }
      else if ((this instanceof L2NpcInstance))
      {
        if (_disableBowAttackEndTime > GameTimeController.getGameTicks()) {
          return;
        }
      }
    }

    target.getKnownList().addKnownObject(this);

    if (Config.ALT_GAME_TIREDNESS) {
      setCurrentCp(getCurrentCp() - 10.0D);
    }

    if ((this instanceof L2PcInstance))
      ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
    else if ((this instanceof L2Summon))
      ((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);
    boolean wasSSCharged;
    boolean wasSSCharged;
    if ((this instanceof L2NpcInstance)) {
      wasSSCharged = ((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
    }
    else
    {
      boolean wasSSCharged;
      if (((this instanceof L2Summon)) && (!(this instanceof L2PetInstance)))
        wasSSCharged = ((L2Summon)this).getChargedSoulShot() != 0;
      else {
        wasSSCharged = (weaponInst != null) && (weaponInst.getChargedSoulshot() != 0);
      }
    }
    int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);

    int timeToHit = timeAtk / 2;

    _attackEndTime = GameTimeController.getGameTicks();
    _attackEndTime += timeAtk / 100;
    _attackEndTime -= 1;

    int ssGrade = 0;

    if (weaponItem != null)
    {
      ssGrade = weaponItem.getCrystalType();
      if (ssGrade == 6) {
        ssGrade = 5;
      }
    }

    Attack attack = new Attack(this, wasSSCharged, ssGrade);

    setAttackingBodypart();
    setHeading(Util.calculateHeadingFrom(this, target));

    int reuse = calculateReuseTime(target, weaponItem);

    if ((this instanceof L2PcInstance))
      _MyIsAttacking = true;
    boolean hitted;
    boolean hitted;
    if (weaponItem == null) {
      hitted = doAttackHitSimple(attack, target, timeToHit);
    }
    else
    {
      boolean hitted;
      if (weaponItem.getItemType() == L2WeaponType.BOW) {
        hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
      }
      else
      {
        boolean hitted;
        if (weaponItem.getItemType() == L2WeaponType.POLE) {
          hitted = doAttackHitByPole(attack, target, timeToHit);
        }
        else
        {
          boolean hitted;
          if (isUsingDualWeapon())
            hitted = doAttackHitByDual(attack, target, timeToHit);
          else
            hitted = doAttackHitSimple(attack, target, timeToHit);
        }
      }
    }
    L2PcInstance player = null;

    if ((this instanceof L2PcInstance))
      player = (L2PcInstance)this;
    else if ((this instanceof L2Summon)) {
      player = ((L2Summon)this).getOwner();
    }
    if (player != null) {
      player.updatePvPStatus(target);
    }

    if (!hitted)
    {
      abortAttack();
    }
    else
    {
      if (((this instanceof L2Summon)) && (!(this instanceof L2PetInstance))) {
        ((L2Summon)this).setChargedSoulShot(0);
      }
      else if (weaponInst != null) {
        weaponInst.setChargedSoulshot(0);
      }

      if (player != null)
      {
        if (player.isCursedWeaponEquiped())
        {
          if (!target.isInvul())
            target.setCurrentCp(0.0D);
        } else if (player.isHero())
        {
          if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isCursedWeaponEquiped()))
          {
            target.setCurrentCp(0.0D);
          }
        }
      }

    }

    if (attack.hasHits()) {
      broadcastPacket(attack);
    }

    ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
  }

  private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
  {
    int damage1 = 0;
    boolean shld1 = false;
    boolean crit1 = false;

    boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);

    if (!Config.NOT_CONSUME_ARROWS)
    {
      reduceArrowCount();
    }
    _move = null;

    if (!miss1)
    {
      shld1 = Formulas.getInstance().calcShldUse(this, target);

      crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

      damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
    }

    if ((this instanceof L2PcInstance))
    {
      sendPacket(new SystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));

      SetupGauge sg = new SetupGauge(1, sAtk + reuse);
      sendPacket(sg);
    }

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);

    _disableBowAttackEndTime = ((sAtk + reuse) / 100 + GameTimeController.getGameTicks());

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

    boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
    boolean miss2 = Formulas.getInstance().calcHitMiss(this, target);

    if (!miss1)
    {
      shld1 = Formulas.getInstance().calcShldUse(this, target);

      crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

      damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
      damage1 /= 2;
    }

    if (!miss2)
    {
      shld2 = Formulas.getInstance().calcShldUse(this, target);

      crit2 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

      damage2 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
      damage2 /= 2;
    }

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);

    attack.addHit(target, damage1, miss1, crit1, shld1);
    attack.addHit(target, damage2, miss2, crit2, shld2);

    return (!miss1) || (!miss2);
  }

  private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
  {
    int maxRadius = getPhysicalAttackRange();
    int maxAngleDiff = (int)getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120.0D, null, null);
    double angleChar = Util.convertHeadingToDegree(getHeading());
    int attackRandomCountMax = (int)getStat().calcStat(Stats.ATTACK_COUNT_MAX, 3.0D, null, null) - 1;
    int attackcount = 0;

    if (angleChar <= 0.0D)
      angleChar += 360.0D;
    boolean hitted = doAttackHitSimple(attack, target, 100.0D, sAtk);
    double attackpercent = 85.0D;

    L2PcInstance player = null;
    if ((this instanceof L2PcInstance))
      player = (L2PcInstance)this;
    else if ((this instanceof L2Summon)) {
      player = ((L2Summon)this).getOwner();
    }
    for (L2Object obj : getKnownList().getKnownObjects().values())
    {
      if (obj != target)
        if ((obj instanceof L2Character))
        {
          if ((((obj instanceof L2PetInstance)) && ((this instanceof L2PcInstance)) && (((L2PetInstance)obj).getOwner() == (L2PcInstance)this)) || 
            (!Util.checkIfInRange(maxRadius, this, obj, false)) || 
            (Math.abs(obj.getZ() - getZ()) > 650) || 
            (!isFacing(obj, maxAngleDiff)))
            continue;
          L2Character temp = (L2Character)obj;
          if (!temp.isAlikeDead())
          {
            attackcount++;
            if (attackcount <= attackRandomCountMax)
            {
              if ((temp == getAI().getAttackTarget()) || (temp.isAutoAttackable(this)))
              {
                if (player != null)
                {
                  if ((((temp instanceof L2PcInstance)) && (((L2PcInstance)temp).getPvpFlag() > 0)) || (((temp instanceof L2Summon)) && (((L2Summon)temp).getOwner().getPvpFlag() > 0)))
                  {
                    player.updatePvPStatus(temp);
                  }
                }
                hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
                attackpercent /= 1.15D;
              }
            }
          }
        }
    }
    return hitted;
  }

  private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
  {
    return doAttackHitSimple(attack, target, 100.0D, sAtk);
  }

  private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
  {
    int damage1 = 0;
    boolean shld1 = false;
    boolean crit1 = false;

    boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);

    if (!miss1)
    {
      shld1 = Formulas.getInstance().calcShldUse(this, target);

      crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

      damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);

      if (attackpercent != 100.0D) {
        damage1 = (int)(damage1 * attackpercent / 100.0D);
      }
    }

    ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);

    attack.addHit(target, damage1, miss1, crit1, shld1);

    return !miss1;
  }

  public void doCast(L2Skill skill)
  {
    _MyIsMoveCast = false;
    if (skill == null)
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if ((skill.getId() == 3260) || (skill.getId() == 3261) || (skill.getId() == 3262))
    {
      L2Weapon weapon = getActiveWeaponItem();
      if ((weapon.getItemId() != 9140) && (weapon.getItemId() != 9141))
      {
        return;
      }
    }

    if (isSkillDisabled(skill.getId()))
    {
      if ((this instanceof L2PcInstance))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
        sm.addSkillName(skill.getId(), skill.getLevel());
        sendPacket(sm);
      }
      return;
    }

    if ((skill.isMagic()) && (isMuted()) && (!skill.isPotion()))
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }
    if ((!skill.isMagic()) && (isPsychicalMuted()) && (!skill.isPotion()))
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if ((isPsychicalMuted()) && (!skill.isMagic()) && (!skill.isPotion()))
    {
      sendPacket(new ActionFailed());
      return;
    }

    if (((this instanceof L2PcInstance)) && (((L2PcInstance)this).isInOlympiadMode()) && ((skill.isHeroSkill()) || (skill.getSkillType() == L2Skill.SkillType.RESURRECT)))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
      sendPacket(sm);
      return;
    }

    if (skill.getSkillType() == L2Skill.SkillType.CHARGE)
    {
      EffectCharge effect = (EffectCharge)getFirstEffect(skill);
      if ((effect != null) && (effect.getLevel() >= skill.getNumCharges()))
      {
        if ((this instanceof L2PcInstance)) {
          sendPacket(new SystemMessage(SystemMessageId.FORCE_MAXIMUM));
        }
        getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
        return;
      }

    }

    if ((skill.getSkillType() == L2Skill.SkillType.SIGNET) || (skill.getSkillType() == L2Skill.SkillType.SIGNET_CASTTIME))
    {
      L2WorldRegion region = getWorldRegion();
      if (region == null) return;
      boolean canCast = true;
      if ((skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND) && ((this instanceof L2PcInstance)))
      {
        Point3D wp = ((L2PcInstance)this).getCurrentSkillWorldPosition();
        if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
          canCast = false;
      }
      else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ())) {
        canCast = false;
      }if (!canCast)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
        sm.addSkillName(skill.getId());
        sendPacket(sm);
        return;
      }

    }

    if (skill.useSoulShot())
    {
      if ((this instanceof L2NpcInstance))
        ((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
      else if ((this instanceof L2PcInstance))
        ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
      else if ((this instanceof L2Summon))
        ((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);
    }
    else if (skill.useSpiritShot())
    {
      if ((this instanceof L2PcInstance))
        ((L2PcInstance)this).rechargeAutoSoulShot(false, true, false);
      else if ((this instanceof L2Summon)) {
        ((L2Summon)this).getOwner().rechargeAutoSoulShot(false, true, true);
      }
    }
    L2Character target = null;
    L2Object[] targets = skill.getTargetList(this);

    if ((skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND))
    {
      target = this;
    }
    else
    {
      if ((targets == null) || (targets.length == 0))
      {
        getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
        return;
      }

      if ((skill.getSkillType() == L2Skill.SkillType.BUFF) || (skill.getSkillType() == L2Skill.SkillType.HEAL) || (skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL) || (skill.getSkillType() == L2Skill.SkillType.REFLECT) || (skill.getSkillType() == L2Skill.SkillType.SEED) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY))
      {
        target = (L2Character)targets[0];

        if (((this instanceof L2PcInstance)) && ((target instanceof L2PcInstance)) && (target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK))
        {
          if ((skill.getSkillType() == L2Skill.SkillType.BUFF) || (skill.getSkillType() == L2Skill.SkillType.HOT) || (skill.getSkillType() == L2Skill.SkillType.HEAL) || (skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL) || (skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == L2Skill.SkillType.BALANCE_LIFE)) {
            target.setLastBuffer(this);
          }
          if ((((L2PcInstance)this).isInParty()) && (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY))
          {
            for (L2PcInstance member : ((L2PcInstance)this).getParty().getPartyMembers())
              member.setLastBuffer(this);
          }
        }
      } else {
        target = (L2Character)getTarget();
      }
    }
    if (target == null)
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    setLastSkillCast(skill);
    int magicId = skill.getId();
    int displayId = skill.getDisplayId();
    int level = skill.getLevel();

    if (level < 1) {
      level = 1;
    }
    int hitTime = skill.getHitTime();
    int coolTime = skill.getCoolTime();

    boolean forcebuff = (skill.getSkillType() == L2Skill.SkillType.FORCE_BUFF) || (skill.getSkillType() == L2Skill.SkillType.SIGNET_CASTTIME);

    if (!forcebuff)
    {
      hitTime = Formulas.getInstance().calcMAtkSpd(this, skill, hitTime);
      if (coolTime > 0) {
        coolTime = Formulas.getInstance().calcMAtkSpd(this, skill, coolTime);
      }
    }
    L2ItemInstance weaponInst = getActiveWeaponInstance();

    if ((skill.getId() == 1157) || (skill.getId() == 1013) || (skill.getId() == 1335) || (skill.getId() == 1311))
    {
      if (weaponInst != null)
      {
        if ((weaponInst.getChargedSpiritshot() == 2) || (weaponInst.getChargedSpiritshot() == 1))
        {
          hitTime = (int)(0.7D * hitTime);
          coolTime = (int)(0.7D * coolTime);
          weaponInst.setChargedSpiritshot(0);
        }
      }

    }

    if ((weaponInst != null) && (skill.isMagic()) && (!forcebuff) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF))
    {
      if ((weaponInst.getChargedSpiritshot() == 2) || (weaponInst.getChargedSpiritshot() == 1))
      {
        hitTime = (int)(0.7D * hitTime);
        coolTime = (int)(0.7D * coolTime);
        switch (3.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[skill.getSkillType().ordinal()])
        {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
          weaponInst.setChargedSpiritshot(0);
        }
      }

    }
    else if (((this instanceof L2NpcInstance)) && (skill.useSpiritShot()))
    {
      if (((L2NpcInstance)this).rechargeAutoSoulShot(false, true))
      {
        hitTime = (int)(0.7D * hitTime);
        coolTime = (int)(0.7D * coolTime);
      }
    }

    if (skill.isStaticHitTime())
    {
      hitTime = skill.getHitTime();
      coolTime = skill.getCoolTime();
    }

    _castEndTime = (10 + GameTimeController.getGameTicks() + (coolTime + hitTime) / 100);
    _castInterruptTime = (-2 + GameTimeController.getGameTicks() + hitTime / 100);
    int reuseDelay;
    int reuseDelay;
    if (skill.isStaticReuse())
    {
      reuseDelay = skill.getReuseDelay();
    }
    else
    {
      int reuseDelay;
      if (skill.isMagic())
      {
        reuseDelay = (int)(skill.getReuseDelay() * getStat().getMReuseRate(skill));
      }
      else
      {
        reuseDelay = (int)(skill.getReuseDelay() * getStat().getPReuseRate(skill));
      }
      reuseDelay = (int)(reuseDelay * (333.0D / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd())));
    }

    if (reuseDelay > 30000) {
      addTimeStamp(skill.getId(), reuseDelay);
    }

    boolean skillMastery = Formulas.getInstance().calcSkillMastery(this, skill);

    if ((reuseDelay > 30000) && (!skillMastery)) addTimeStamp(skill.getId(), reuseDelay);

    int initmpcons = getStat().getMpInitialConsume(skill);
    if (initmpcons > 0)
    {
      StatusUpdate su = new StatusUpdate(getObjectId());
      if (skill.isDance())
      {
        getStatus().reduceMp(calcStat(Stats.DANCE_MP_CONSUME_RATE, initmpcons, null, null));
      }
      else if (skill.isMagic())
      {
        getStatus().reduceMp(calcStat(Stats.MAGICAL_MP_CONSUME_RATE, initmpcons, null, null));
      }
      else
      {
        getStatus().reduceMp(calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, initmpcons, null, null));
      }
      su.addAttribute(11, (int)getCurrentMp());
      sendPacket(su);
    }

    if ((reuseDelay > 10) && (!skillMastery))
    {
      disableSkill(skill.getId(), reuseDelay);
    }

    if (skillMastery)
    {
      reuseDelay = 0;
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
      sm.addSkillName(skill.getId(), skill.getLevel());
      sendPacket(sm);
    }

    setHeading(Util.calculateHeadingFrom(this, target));

    if (forcebuff)
    {
      if (skill.getItemConsume() > 0) {
        consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
      }
      if (skill.getSkillType() == L2Skill.SkillType.FORCE_BUFF)
        startForceBuff(target, skill);
      else
        callSkill(skill, targets);
    }
    getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    broadcastPacket(new MagicSkillUser(this, target, displayId, level, hitTime, reuseDelay));

    if (((this instanceof L2PcInstance)) && (magicId != 1312))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
      sm.addSkillName(magicId, skill.getLevel());
      sendPacket(sm);
    }

    if (hitTime > 210)
    {
      if (((this instanceof L2PcInstance)) && (!forcebuff))
      {
        SetupGauge sg = new SetupGauge(0, hitTime);
        sendPacket(sg);
      }

      disableAllSkills();

      if (_skillCast != null)
      {
        _skillCast.cancel(true);
        _skillCast = null;
      }

      if (forcebuff)
        _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), hitTime);
      else
        _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1), hitTime - 200);
    }
    else
    {
      onMagicLaunchedTimer(targets, skill, coolTime, true);
    }
    L2Character _character = this;
    L2Object[] _targets = targets;
    L2Skill _skill = skill;
    if ((this instanceof L2NpcInstance)) {
      ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(_targets, _character, _skill) {
        public void run() {
          try {
            if (((L2NpcTemplate)getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null) {
              L2PcInstance player = null;
              if ((val$_targets[0] instanceof L2PcInstance))
                player = (L2PcInstance)val$_targets[0];
              else if ((val$_targets[0] instanceof L2Summon))
                player = ((L2Summon)val$_targets[0]).getOwner();
              for (Quest quest : ((L2NpcTemplate)getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED))
              {
                quest.notifySpellFinished((L2NpcInstance)val$_character, player, val$_skill);
              }
            }
          }
          catch (Throwable e)
          {
          }
        }
      }
      , hitTime + coolTime + 1000);
    }

    stopMove(null);
  }
  public void addTimeStamp(int s, int r) {
  }
  public void removeTimeStamp(int s) {
  }

  public void startForceBuff(L2Character caster, L2Skill skill) {
  }

  public boolean doDie(L2Character killer) {
    synchronized (this)
    {
      if (isKilledAlready()) return false;
      setIsKilledAlready(true);
    }
    setTarget(null);
    stopMove(null);

    getStatus().stopHpMpRegeneration();
    if (((this instanceof L2PlayableInstance)) && (((L2PlayableInstance)this).isPhoenixBlessed()))
    {
      if (((L2PlayableInstance)this).getCharmOfLuck())
        ((L2PlayableInstance)this).stopCharmOfLuck(null);
      if (((L2PlayableInstance)this).isNoblesseBlessed())
        ((L2PlayableInstance)this).stopNoblesseBlessing(null);
    }
    else if (((this instanceof L2PlayableInstance)) && (((L2PlayableInstance)this).isNoblesseBlessed()))
    {
      ((L2PlayableInstance)this).stopNoblesseBlessing(null);
      if (((L2PlayableInstance)this).getCharmOfLuck()) {
        ((L2PlayableInstance)this).stopCharmOfLuck(null);
      }

    }
    else if (Config.REMOVE_BUFFS_AFTER_DEATH) { stopAllEffects();
    }
    if ((!(this instanceof L2GuardInstance)) && (!(this instanceof L2SiegeGuardInstance)) && ((!(this instanceof L2MinionInstance)) || (!((L2MinionInstance)this).getLeader().isRaid())))
    {
      calculateRewards(killer);
    }
    broadcastStatusUpdate();
    getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);

    if (getWorldRegion() != null)
      getWorldRegion().onDeath(this);
    for (QuestState qs : getNotifyQuestOfDeath())
    {
      qs.getQuest().notifyDeath(killer == null ? this : killer, this, qs);
    }
    getNotifyQuestOfDeath().clear();
    if (((this instanceof L2PlayableInstance)) && (((L2PlayableInstance)this).isPhoenixBlessed()))
    {
      if ((this instanceof L2Summon))
      {
        ((L2Summon)this).getOwner().reviveRequest(((L2Summon)this).getOwner(), null, true);
      }
      else
        ((L2PcInstance)this).reviveRequest((L2PcInstance)this, null, false);
    }
    getAttackByList().clear();
    return true;
  }

  protected void calculateRewards(L2Character killer)
  {
  }

  public void doRevive(boolean broadcastPacketRevive)
  {
    if (!isDead())
    {
      return;
    }
    if (!isTeleporting())
    {
      setIsPendingRevive(false);

      if (((this instanceof L2PlayableInstance)) && (((L2PlayableInstance)this).isPhoenixBlessed()))
      {
        ((L2PlayableInstance)this).stopPhoenixBlessing(null);
        setCurrentCp(getMaxCp());
        setCurrentHp(getMaxHp());
        setCurrentMp(getMaxMp());
      }
      else
      {
        if (Config.RESPAWN_RESTORE_CP < 0.0D)
        {
          _status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
        }

        _status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);

        if (Config.RESPAWN_RESTORE_MP < 0.0D) {
          _status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
        }
      }
    }
    if (broadcastPacketRevive)
    {
      broadcastPacket(new Revive(this));
    }

    if (getWorldRegion() != null)
      getWorldRegion().onRevive(this);
    else
      setIsPendingRevive(true);
  }

  public void doRevive(double revivePower)
  {
    doRevive(true);
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

    if (skill.isPassive()) {
      return;
    }
    L2Object target = null;

    switch (3.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[skill.getTargetType().ordinal()])
    {
    case 1:
    case 2:
    case 3:
      target = this;
      break;
    default:
      target = skill.getFirstOfTargetList(this);
    }

    if ((skill.isOffensive()) && ((target instanceof L2Character)) && (Formulas.getInstance().canCancelAttackerTarget(this, (L2Character)target)))
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      synchronized (this)
      {
        if (_ai == null) _ai = new L2CharacterAI(new AIAccessor());
      }
    }

    return _ai;
  }

  public void setAI(L2CharacterAI newAI)
  {
    L2CharacterAI oldAI = getAI();
    if ((oldAI != null) && (oldAI != newAI) && ((oldAI instanceof L2AttackableAI)))
      ((L2AttackableAI)oldAI).stopAITask();
    _ai = newAI;
  }
  public boolean hasAI() {
    return _ai != null;
  }

  public boolean isRaid() {
    return _isRaid;
  }

  public void setIsRaid(boolean isRaid)
  {
    _isRaid = isRaid;
  }

  public final List<L2Character> getAttackByList()
  {
    if (_attackByList == null)
    {
      synchronized (this)
      {
        if (_attackByList == null)
        {
          _attackByList = new FastList();
        }
      }
    }
    return _attackByList;
  }

  public final L2Skill getLastSkillCast()
  {
    return _lastSkillCast; } 
  public void setLastSkillCast(L2Skill skill) { _lastSkillCast = skill; } 
  public final boolean isAfraid() {
    return _isAfraid; } 
  public final void setIsAfraid(boolean value) { _isAfraid = value; }

  public final boolean isAlikeDead() {
    return (isFakeDeath()) || (getCurrentHp() <= 0.5D);
  }
  public final boolean isAllSkillsDisabled() {
    return (_allSkillsDisabled) || (isStunned()) || (isSleeping()) || (isMeditation()) || (isParalyzed());
  }
  public final boolean isPotionsDisabled() { return (isStunned()) || (isSleeping()) || (isMeditation()) || (isParalyzed()); } 
  public boolean isAttackingDisabled() {
    return (isStunned()) || (isAttackDisable()) || (isMeditation()) || (isSleeping()) || (_attackEndTime > GameTimeController.getGameTicks()) || (isFakeDeath()) || (isParalyzed()) || (isFallsdown());
  }
  public final Calculator[] getCalculators() { return _calculators; } 
  public final boolean isConfused() {
    return _isConfused; } 
  public final void setIsConfused(boolean value) { _isConfused = value; }

  public final boolean isDead() {
    return (!isFakeDeath()) && (getCurrentHp() <= 0.5D);
  }
  public final boolean isFakeDeath() { return _isFakeDeath; } 
  public final void setIsFakeDeath(boolean value) { _isFakeDeath = value; } 
  public final boolean isFallsdown() {
    return _isFallsdown; } 
  public final void setIsFallsdown(boolean value) { _isFallsdown = value; } 
  public final boolean isFlying() {
    return _isFlying;
  }
  public final void setIsFlying(boolean mode) { _isFlying = mode; } 
  public boolean isImobilised() {
    return _isImobilised; } 
  public void setIsImobilised(boolean value) { _isImobilised = value; } 
  public final boolean isKilledAlready() {
    return _isKilledAlready; } 
  public final void setIsKilledAlready(boolean value) { _isKilledAlready = value; } 
  public final boolean isMuted() {
    return _isMuted; } 
  public final void setIsMuted(boolean value) { _isMuted = value; } 
  public final boolean isPsychicalMuted() {
    return _isPsychicalMuted; } 
  public final void setIsPsychicalMuted(boolean value) { _isPsychicalMuted = value; }

  public boolean isMovementDisabled() {
    return (isStunned()) || (isRooted()) || (isSleeping()) || (isMeditation()) || (isOverloaded()) || (isParalyzed()) || (isImobilised()) || (isFakeDeath()) || (isFallsdown());
  }
  public final boolean isOutOfControl() {
    return (isConfused()) || (isAfraid());
  }
  public final boolean isOverloaded() { return _isOverloaded; } 
  public final void setIsOverloaded(boolean value) {
    _isOverloaded = value;
  }
  public final boolean isParalyzed() { return _isParalyzed; } 
  public final void setIsParalyzed(boolean value) { _isParalyzed = value; }

  public final boolean isPendingRevive()
  {
    return (isDead()) && (_isPendingRevive);
  }

  public final void setIsPendingRevive(boolean value)
  {
    _isPendingRevive = value;
  }

  public L2Summon getPet()
  {
    return null;
  }
  public final boolean isRiding() {
    return _isRiding;
  }
  public final void setIsRiding(boolean mode) { _isRiding = mode; } 
  public final boolean isRooted() {
    return _isRooted; } 
  public final void setIsRooted(boolean value) { _isRooted = value; }

  public final boolean isRunning() {
    return _isRunning;
  }
  public final void setIsRunning(boolean value) {
    _isRunning = value;
    broadcastPacket(new ChangeMoveType(this));
  }

  public final void setRunning()
  {
    if (!isRunning()) setIsRunning(true); 
  }
  public final boolean isSleeping() {
    return _isSleeping; } 
  public final void setIsSleeping(boolean value) { _isSleeping = value; } 
  public final boolean isMeditation() {
    return _isMeditation; } 
  public final void setIsMeditation(boolean value) { _isMeditation = value; } 
  public final boolean isStunned() {
    return _isStunned; } 
  public final void setIsStunned(boolean value) { _isStunned = value; } 
  public final boolean isAttackDisable() {
    return _isAttackDisable; } 
  public final void setIsAttackDisable(boolean value) { _isAttackDisable = value; } 
  public final boolean isBetrayed() {
    return _isBetrayed; } 
  public final void setIsBetrayed(boolean value) { _isBetrayed = value; } 
  public final boolean isTeleporting() {
    return _isTeleporting; } 
  public final void setIsTeleporting(boolean value) { _isTeleporting = value; } 
  public void setIsInvul(boolean b) { _isInvul = b; } 
  public boolean isInvul() { return (_isInvul) || (_isTeleporting); } 
  public boolean isUndead() { return _template.isUndead;
  }

  public CharKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof CharKnownList)))
      setKnownList(new CharKnownList(this));
    return (CharKnownList)super.getKnownList();
  }

  public CharStat getStat()
  {
    if (_stat == null) _stat = new CharStat(this);
    return _stat;
  }
  public final void setStat(CharStat value) { _stat = value; }

  public CharStatus getStatus()
  {
    if (_status == null) _status = new CharStatus(this);
    return _status;
  }
  public final void setStatus(CharStatus value) { _status = value; } 
  public L2CharTemplate getTemplate() {
    return _template;
  }

  protected final void setTemplate(L2CharTemplate template)
  {
    _template = template;
  }
  public final String getTitle() {
    return _title;
  }
  public final void setTitle(String value) { _title = value; }

  public final void setWalking() {
    if (isRunning()) setIsRunning(false);
  }

  public final void addEffect(L2Effect newEffect)
  {
    if (newEffect == null) return;

    synchronized (this)
    {
      if (_effects == null) {
        _effects = new FastTable();
      }
      if (_stackedEffects == null)
        _stackedEffects = new FastMap();
    }
    synchronized (_effects)
    {
      for (int i = 0; i < _effects.size(); i++)
      {
        if ((((L2Effect)_effects.get(i)).getSkill().getId() != newEffect.getSkill().getId()) || (((L2Effect)_effects.get(i)).getEffectType() != newEffect.getEffectType()) || (((L2Effect)_effects.get(i)).getStackOrder() != newEffect.getStackOrder()))
        {
          continue;
        }
        if ((newEffect.getSkill().getSkillType() == L2Skill.SkillType.BUFF) || (newEffect.getEffectType() == L2Effect.EffectType.BUFF))
        {
          ((L2Effect)_effects.get(i)).exit();
        }
        else
        {
          newEffect.stopEffectTask();
          return;
        }
      }

      L2Skill tempskill = newEffect.getSkill();
      int buffcountmax = -1;
      buffcountmax += Config.BUFFS_MAX_AMOUNT;
      if ((getBuffCount() > buffcountmax) && (!doesStack(tempskill)) && ((tempskill.getSkillType() == L2Skill.SkillType.BUFF) || (tempskill.getSkillType() == L2Skill.SkillType.REFLECT) || (tempskill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT) || (tempskill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT)) && ((tempskill.getId() <= 4360) || (tempskill.getId() >= 4367)) && ((tempskill.getId() <= 4550) || (tempskill.getId() >= 4555)))
      {
        if (newEffect.isHerbEffect())
        {
          newEffect.stopEffectTask();
          return;
        }
        removeFirstBuff(tempskill.getId());
      }

      if ((getDeBuffCount() >= Config.DEBUFFS_MAX_AMOUNT) && (!doesStack(tempskill)) && (tempskill.getSkillType() == L2Skill.SkillType.DEBUFF))
      {
        removeFirstDeBuff(tempskill.getId());
      }

      if (!newEffect.getSkill().isToggle())
      {
        int pos = 0;
        for (int i = 0; i < _effects.size(); i++)
        {
          if (_effects.get(i) == null)
            break;
          int skillid = ((L2Effect)_effects.get(i)).getSkill().getId();
          if ((((L2Effect)_effects.get(i)).getSkill().isToggle()) || ((skillid > 4360) && (skillid < 4367)))
            continue;
          pos++;
        }

        _effects.add(pos, newEffect);
      } else {
        _effects.addLast(newEffect);
      }if (newEffect.getStackType().equals("none"))
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
      L2Effect tempEffect = null;
      if (stackQueue.size() > 0)
      {
        for (int i = 0; i < _effects.size(); i++)
        {
          if (_effects.get(i) != stackQueue.get(0))
            continue;
          tempEffect = (L2Effect)_effects.get(i);
          break;
        }
      }

      stackQueue = effectQueueInsert(newEffect, stackQueue);

      if (stackQueue == null) return;
      _stackedEffects.put(newEffect.getStackType(), stackQueue);
      L2Effect tempEffect2 = null;
      for (int i = 0; i < _effects.size(); i++)
      {
        if (_effects.get(i) != stackQueue.get(0))
          continue;
        tempEffect2 = (L2Effect)_effects.get(i);
        break;
      }

      if (tempEffect != tempEffect2)
      {
        if (tempEffect != null)
        {
          removeStatsOwner(tempEffect);
          tempEffect.setInUse(false);
        }
        if (tempEffect2 != null)
        {
          tempEffect2.setInUse(true);
          addStatFuncs(tempEffect2.getStatFuncs());
        }
      }
    }
    updateEffectIcons();

    updateEffectIcons();
  }

  private List<L2Effect> effectQueueInsert(L2Effect newStackedEffect, List<L2Effect> stackQueue)
  {
    if (_effects == null) {
      return null;
    }
    Iterator queueIterator = stackQueue.iterator();

    int i = 0;
    while (queueIterator.hasNext())
    {
      L2Effect cur = (L2Effect)queueIterator.next();
      if (newStackedEffect.getStackOrder() >= cur.getStackOrder()) break;
      i++;
    }

    stackQueue.add(i, newStackedEffect);

    if ((Config.EFFECT_CANCELING) && (!newStackedEffect.isHerbEffect()) && (stackQueue.size() > 1))
    {
      for (int n = 0; n < _effects.size(); n++)
      {
        if (_effects.get(n) != stackQueue.get(1))
          continue;
        _effects.remove(n);
        break;
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
      if (effect.getStackType() == "none")
      {
        removeStatsOwner(effect);
      }
      else if (effect.getStackType().equalsIgnoreCase("HpRecover"))
      {
        sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
      }
      else
      {
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
              for (int i = 0; i < _effects.size(); i++)
              {
                if (_effects.get(i) != stackQueue.get(0))
                  continue;
                addStatFuncs(((L2Effect)_effects.get(i)).getStatFuncs());
                ((L2Effect)_effects.get(i)).setInUse(true);
                break;
              }
            }

          }

          if (stackQueue.isEmpty())
            _stackedEffects.remove(effect.getStackType());
          else {
            _stackedEffects.put(effect.getStackType(), stackQueue);
          }

        }

      }

      for (int i = 0; i < _effects.size(); i++)
      {
        if (_effects.get(i) != effect)
          continue;
        _effects.remove(i);
        break;
      }

    }

    updateEffectIcons();
  }

  public final void startAbnormalEffect(int mask)
  {
    _AbnormalEffects |= mask;
    updateAbnormalEffect();
  }

  public final void startConfused()
  {
    setIsConfused(true);
    getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
    updateAbnormalEffect();
  }

  public final void startFakeDeath()
  {
    setIsFallsdown(true);
    setIsFakeDeath(true);
    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
    broadcastPacket(new ChangeWaitType(this, 2));

    for (L2Object obj : getKnownList().getKnownObjects().values())
    {
      if ((obj instanceof L2Character))
      {
        if ((obj == this) || (((L2Character)obj).getTarget() != this) || ((obj instanceof L2Summon))) {
          continue;
        }
        ((L2Character)obj).setTarget(null);
      }
    }
  }

  public final void startFear()
  {
    setIsAfraid(true);
    getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
    updateAbnormalEffect();
  }

  public final void startMuted()
  {
    setIsMuted(true);
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_MUTED);
    updateAbnormalEffect();
  }

  public final void startPsychicalMuted()
  {
    setIsPsychicalMuted(true);
    getAI().notifyEvent(CtrlEvent.EVT_MUTED);
    updateAbnormalEffect();
  }

  public final void startRooted()
  {
    setIsRooted(true);
    getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
    updateAbnormalEffect();
  }

  public final void startSleeping()
  {
    setIsSleeping(true);
    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
    updateAbnormalEffect();
  }

  public final void startMeditation()
  {
    setIsMeditation(true);
    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
    updateAbnormalEffect();
  }

  public final void startStunning()
  {
    setIsStunned(true);
    abortAttack();
    abortCast();
    getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
    updateAbnormalEffect();
  }

  public final void startBetray()
  {
    setIsBetrayed(true);
    getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
    updateAbnormalEffect();
  }

  public final void stopBetray()
  {
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
    L2Effect[] effects = getAllEffects();
    if (effects == null) return;
    for (L2Effect e : effects)
    {
      if (e == null)
        continue;
      e.exit(true);
    }

    if ((this instanceof L2PcInstance)) ((L2PcInstance)this).updateAndBroadcastStatus(2);
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

  public final void stopSkillEffects(int skillId)
  {
    L2Effect[] effects = getAllEffects();
    if (effects == null) return;

    for (L2Effect e : effects)
    {
      if (e.getSkill().getId() != skillId) continue; e.exit();
    }
  }

  public final void stopEffects(L2Effect.EffectType type)
  {
    L2Effect[] effects = getAllEffects();

    if (effects == null) return;
    for (L2Effect e : effects)
    {
      if (e.getEffectType() != type) continue; e.exit();
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
    setIsFallsdown(false);
    if ((this instanceof L2PcInstance))
    {
      ((L2PcInstance)this).setRecentFakeDeath(true);
    }

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
    abortAttack();
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

  public final void stopPsychicalMuted(L2Effect effect)
  {
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
    abortAttack();
    setIsSleeping(false);
    getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
    updateAbnormalEffect();
  }

  public final void stopMeditation(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.MEDITATION);
    else {
      removeEffect(effect);
    }
    abortAttack();
    setIsMeditation(false);
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
    abortAttack();
    setIsStunned(false);
    getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
    updateAbnormalEffect();
  }

  public abstract void updateAbnormalEffect();

  public final void updateEffectIcons() {
    updateEffectIcons(false);
  }

  public final void updateEffectIcons(boolean partyOnly)
  {
    L2PcInstance player = null;
    if ((this instanceof L2PcInstance)) {
      player = (L2PcInstance)this;
    }
    L2Summon summon = null;
    if ((this instanceof L2Summon))
    {
      summon = (L2Summon)this;
      player = summon.getOwner();
    }

    MagicEffectIcons mi = null;
    if (!partyOnly) {
      mi = new MagicEffectIcons();
    }
    PartySpelled ps = null;
    if (summon != null)
      ps = new PartySpelled(summon);
    else if ((player != null) && (player.isInParty())) {
      ps = new PartySpelled(player);
    }
    ExOlympiadSpelledInfo os = null;
    if ((player != null) && (player.isInOlympiadMode())) {
      os = new ExOlympiadSpelledInfo(player);
    }
    if ((mi == null) && (ps == null) && (os == null)) {
      return;
    }
    L2Effect[] effects = getAllEffects();
    if ((effects != null) && (effects.length > 0))
    {
      for (int i = 0; i < effects.length; i++)
      {
        L2Effect effect = effects[i];

        if ((effect == null) || (!effect.getShowIcon()))
        {
          continue;
        }

        if ((effect.getEffectType() == L2Effect.EffectType.CHARGE) || ((effect.getEffectType() == L2Effect.EffectType.SIGNET_GROUND) && (player != null)))
        {
          continue;
        }

        if (!effect.getInUse())
          continue;
        if (effect.getStackType().equalsIgnoreCase("HpRecover"))
        {
          sendPacket(new ShortBuffStatusUpdate(effect.getSkill().getId(), effect.getSkill().getLevel(), effect.getSkill().getBuffDuration() / 1000));
        }
        else
        {
          if (mi != null)
            effect.addIcon(mi);
          if (ps != null)
            effect.addPartySpelledIcon(ps);
          if (os != null) {
            effect.addOlympiadSpelledIcon(os);
          }
        }
      }
    }

    if (mi != null)
      sendPacket(mi);
    if ((ps != null) && (player != null))
    {
      if ((player.isInParty()) && (summon == null))
        player.getParty().broadcastToPartyMembers(player, ps);
      else
        player.sendPacket(ps);
    }
    if (os != null)
    {
      if (Olympiad.getInstance().getSpectators(player.getOlympiadGameId()) != null)
      {
        for (L2PcInstance spectator : Olympiad.getInstance().getSpectators(player.getOlympiadGameId()))
        {
          if (spectator != null)
            spectator.sendPacket(os);
        }
      }
    }
  }

  public int getAbnormalEffect()
  {
    int ae = _AbnormalEffects;
    if (isStunned()) ae |= 64;
    if (isRooted()) ae |= 512;
    if (isSleeping()) ae |= 128;
    if (isMeditation()) ae |= 524288;
    if (isConfused()) ae |= 32;
    if (isMuted()) ae |= 256;
    if (isAfraid()) ae |= 16;
    if (isPsychicalMuted()) ae |= 256;
    return ae;
  }

  public final L2Effect[] getAllEffects()
  {
    FastTable effects = _effects;
    if ((effects == null) || (effects.isEmpty())) return EMPTY_EFFECTS;
    int ArraySize = effects.size();
    L2Effect[] effectArray = new L2Effect[ArraySize];
    for (int i = 0; i < ArraySize; i++)
    {
      if ((i >= effects.size()) || (effects.get(i) == null)) break;
      effectArray[i] = ((L2Effect)effects.get(i));
    }
    return effectArray;
  }

  public final L2Effect getFirstEffect(int index)
  {
    FastTable effects = _effects;
    if (effects == null) return null;

    L2Effect eventNotInUse = null;
    for (int i = 0; i < effects.size(); i++)
    {
      L2Effect e = (L2Effect)effects.get(i);
      if (e.getSkill().getId() != index)
        continue;
      if (e.getInUse()) return e;
      eventNotInUse = e;
    }

    return eventNotInUse;
  }

  public final L2Effect getFirstEffect(L2Skill skill)
  {
    FastTable effects = _effects;
    if (effects == null) return null;

    L2Effect eventNotInUse = null;
    for (int i = 0; i < effects.size(); i++)
    {
      L2Effect e = (L2Effect)effects.get(i);
      if (e.getSkill() != skill)
        continue;
      if (e.getInUse()) return e;
      eventNotInUse = e;
    }

    return eventNotInUse;
  }

  public final L2Effect getFirstEffect(L2Effect.EffectType tp)
  {
    FastTable effects = _effects;
    if (effects == null) return null;

    L2Effect eventNotInUse = null;
    for (int i = 0; i < effects.size(); i++)
    {
      L2Effect e = (L2Effect)effects.get(i);
      if (e.getEffectType() != tp)
        continue;
      if (e.getInUse()) return e;
      eventNotInUse = e;
    }

    return eventNotInUse;
  }

  public EffectCharge getChargeEffect()
  {
    L2Effect[] effects = getAllEffects();
    for (L2Effect e : effects)
    {
      if (e.getSkill().getSkillType() == L2Skill.SkillType.CHARGE)
      {
        return (EffectCharge)e;
      }
    }
    return null;
  }

  public void setCanCastAA(boolean v)
  {
    _canCastAA = v;
  }

  public boolean getCanCastAA()
  {
    return _canCastAA;
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

      for (int i = 0; i < Stats.NUM_STATS; i++)
      {
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

    for (Func f : funcs)
    {
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

    if ((this instanceof L2NpcInstance))
    {
      int i = 0;
      for (; i < Stats.NUM_STATS; i++)
      {
        if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i])) {
          break;
        }
      }
      if (i >= Stats.NUM_STATS)
        _calculators = NPC_STD_CALCULATOR;
    }
  }

  public final synchronized void removeStatFuncs(Func[] funcs)
  {
    FastList modifiedStats = new FastList();

    for (Func f : funcs)
    {
      modifiedStats.add(f.stat);
      removeStatFunc(f);
    }

    broadcastModifiedStats(modifiedStats);
  }

  public final synchronized void removeStatsOwner(Object owner)
  {
    FastList modifiedStats = null;

    for (int i = 0; i < _calculators.length; i++)
    {
      if (_calculators[i] == null) {
        continue;
      }
      if (modifiedStats != null)
        modifiedStats.addAll(_calculators[i].removeOwner(owner));
      else {
        modifiedStats = _calculators[i].removeOwner(owner);
      }
      if (_calculators[i].size() == 0) {
        _calculators[i] = null;
      }

    }

    if ((this instanceof L2NpcInstance))
    {
      int i = 0;
      for (; i < Stats.NUM_STATS; i++)
      {
        if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i])) {
          break;
        }
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
    if ((stats == null) || (stats.isEmpty())) return;

    boolean broadcastFull = false;
    boolean otherStats = false;
    StatusUpdate su = null;

    for (Stats stat : stats)
    {
      if (stat == Stats.POWER_ATTACK_SPEED)
      {
        if (su == null) su = new StatusUpdate(getObjectId());
        su.addAttribute(18, getPAtkSpd());
      }
      else if (stat == Stats.MAGIC_ATTACK_SPEED)
      {
        if (su == null) su = new StatusUpdate(getObjectId());
        su.addAttribute(24, getMAtkSpd());
      }
      else if (stat == Stats.MAX_CP)
      {
        if ((this instanceof L2PcInstance))
        {
          if (su == null) su = new StatusUpdate(getObjectId());
          su.addAttribute(34, getMaxCp());
        }

      }
      else if (stat == Stats.RUN_SPEED)
      {
        broadcastFull = true;
      }
      else {
        otherStats = true;
      }
    }
    if ((this instanceof L2PcInstance))
    {
      if (broadcastFull) {
        ((L2PcInstance)this).updateAndBroadcastStatus(2);
      }
      else if (otherStats)
      {
        ((L2PcInstance)this).updateAndBroadcastStatus(1);
        if (su != null)
        {
          for (L2PcInstance player : getKnownList().getKnownPlayers().values())
            try {
              player.sendPacket(su);
            } catch (NullPointerException e) {
            }
        }
      } else if (su != null) { broadcastPacket(su);
      }
    }
    else if ((this instanceof L2NpcInstance))
    {
      if (broadcastFull)
      {
        for (L2PcInstance player : getKnownList().getKnownPlayers().values())
          if (player != null)
            player.sendPacket(new NpcInfo((L2NpcInstance)this, player));
      }
      else if (su != null)
        broadcastPacket(su);
    }
    else if ((this instanceof L2Summon))
    {
      if (broadcastFull)
      {
        for (L2PcInstance player : getKnownList().getKnownPlayers().values())
          if (player != null)
            player.sendPacket(new NpcInfo((L2Summon)this, player));
      }
      else if (su != null)
        broadcastPacket(su);
    }
    else if (su != null)
      broadcastPacket(su);
  }

  public final int getHeading()
  {
    return _heading;
  }

  public final void setHeading(int heading)
  {
    _heading = heading;
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
    if (_move == null) return false;
    try
    {
      if (_move.onGeodataPathIndex == -1) return false;
      if (_move.onGeodataPathIndex == _move.geoPath.size() - 1)
        return false;
    }
    catch (NullPointerException e)
    {
      return false;
    }
    return true;
  }

  public final boolean isCastingNow()
  {
    return (_castEndTime > GameTimeController.getGameTicks()) || (_isCastNow);
  }

  public final void setCastingNow(boolean value)
  {
    _isCastNow = value;
  }

  public final L2NpcTemplate getLastPetNT()
  {
    return _lastPetNT;
  }

  public final void setLastPetNT(L2NpcTemplate value)
  {
    _lastPetNT = value;
  }

  public final L2ItemInstance getLastPetItem()
  {
    return _lastPetItem;
  }

  public final void setLastPetItem(L2ItemInstance value)
  {
    _lastPetItem = value;
  }

  public final boolean canAbortCast()
  {
    return _castInterruptTime > GameTimeController.getGameTicks();
  }

  public final boolean isAttackingNow()
  {
    return _attackEndTime > GameTimeController.getGameTicks();
  }

  public final boolean isAttackAborted()
  {
    return _attacking <= 0;
  }

  public final void abortAttack()
  {
    if (isAttackingNow())
    {
      _attacking = 0;
      sendPacket(new ActionFailed());
    }
  }

  public final int getAttackingBodyPart()
  {
    return _attacking;
  }

  public final void abortCast()
  {
    if (isCastingNow())
    {
      if (_MyIsMoveCast) {
        _MyIsMoveCast = false;
      }
      if (_lastPetNT != null) {
        _lastPetNT = null;
      }
      if (_lastPetItem != null) {
        _lastPetItem = null;
      }
      _castEndTime = 0;
      _castInterruptTime = 0;
      if (_skillCast != null)
      {
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

      enableAllSkills();
      if ((this instanceof L2PcInstance)) getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
      broadcastPacket(new MagicSkillCanceld(getObjectId()));
      sendPacket(new ActionFailed());
    }
  }

  public boolean updatePosition(int gameTicks)
  {
    MoveData m = _move;

    if (m == null) {
      return true;
    }
    if (!isVisible())
    {
      _move = null;
      return true;
    }

    if (m._moveTimestamp == 0)
    {
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
    else
    {
      dx = m._xDestination - m._xAccurate;
      dy = m._yDestination - m._yAccurate;
    }
    double dz;
    if ((Config.GEODATA) && (Config.COORD_SYNCHRONIZE == 2) && (!isFlying()) && (!isInsideZone(128)) && (!m.disregardingGeodata) && (GameTimeController.getGameTicks() % 10 == 0) && (!(this instanceof L2BoatInstance)))
    {
      short geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30, getObjectId());
      double dz = m._zDestination - geoHeight;

      if (((this instanceof L2PcInstance)) && (Math.abs(((L2PcInstance)this).getClientZ() - geoHeight) > 200) && (Math.abs(((L2PcInstance)this).getClientZ() - geoHeight) < 1500))
      {
        dz = m._zDestination - zPrev;
      }
      else if ((isInCombat()) && (Math.abs(dz) > 200.0D) && (dx * dx + dy * dy < 40000.0D))
      {
        dz = m._zDestination - zPrev;
      }
      else
      {
        zPrev = geoHeight;
      }
    }
    else {
      dz = m._zDestination - zPrev;
    }
    double distPassed = getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / 10.0F;
    double distFraction;
    double distFraction;
    if ((dx * dx + dy * dy < 10000.0D) && (dz * dz > 2500.0D))
    {
      distFraction = distPassed / Math.sqrt(dx * dx + dy * dy);
    }
    else {
      distFraction = distPassed / Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    if (distFraction > 1.0D)
    {
      if ((this instanceof L2BoatInstance))
      {
        super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
        ((L2BoatInstance)this).updatePeopleInTheBoat(m._xDestination, m._yDestination, m._zDestination);
      }
      else
      {
        super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
        revalidateZone(false);
      }
    }
    else
    {
      m._xAccurate += dx * distFraction;
      m._yAccurate += dy * distFraction;

      if ((this instanceof L2BoatInstance))
      {
        super.getPosition().setXYZ((int)m._xAccurate, (int)m._yAccurate, zPrev + (int)(dz * distFraction + 0.5D));
        ((L2BoatInstance)this).updatePeopleInTheBoat((int)m._xAccurate, (int)m._yAccurate, zPrev + (int)(dz * distFraction + 0.5D));
      }
      else
      {
        super.getPosition().setXYZ((int)m._xAccurate, (int)m._yAccurate, zPrev + (int)(dz * distFraction + 0.5D));
        revalidateZone(false);
      }

    }

    m._moveTimestamp = gameTicks;

    return distFraction > 1.0D;
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
  }

  public void stopMove(L2CharPosition pos)
  {
    stopMove(pos, false);
  }

  public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
  {
    _move = null;

    if (pos != null)
    {
      getPosition().setXYZ(pos.x, pos.y, pos.z);
      setHeading(pos.heading);
      if ((this instanceof L2PcInstance)) ((L2PcInstance)this).revalidateZone(true);
    }
    broadcastPacket(new StopMove(this));
    if ((Config.MOVE_BASED_KNOWNLIST) && (updateKnownObjects)) getKnownList().findObjects();
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
    if ((object != null) && (object != _target))
    {
      getKnownList().addKnownObject(object);
      object.getKnownList().addKnownObject(this);
    }

    if (object == null)
    {
      if (_target != null)
      {
        broadcastPacket(new TargetUnselected(this));
      }

      if ((isAttackingNow()) && (getAI().getAttackTarget() == _target))
      {
        abortAttack();

        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

        if ((this instanceof L2PcInstance)) {
          sendPacket(new ActionFailed());
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
          sm.addString("Attack is aborted");
          sendPacket(sm);
        }
      }

      if ((isCastingNow()) && (canAbortCast()) && (getAI().getCastTarget() == _target))
      {
        abortCast();
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        if ((this instanceof L2PcInstance)) {
          sendPacket(new ActionFailed());
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
          sm.addString("Casting is aborted");
          sendPacket(sm);
        }
      }
    }

    _target = object;
  }

  public final int getTargetId()
  {
    if (_target != null)
    {
      return _target.getObjectId();
    }

    return -1;
  }

  public final L2Object getTarget()
  {
    return _target;
  }

  protected void moveToLocation(int x, int y, int z, int offset)
  {
    getAI().setSitDownAfterAction(false);

    float speed = getStat().getMoveSpeed();

    if (speed <= 0.0F) {
      return;
    }

    int curX = super.getX();
    int curY = super.getY();
    int curZ = super.getZ();

    double dx = x - curX;
    double dy = y - curY;
    double dz = z - curZ;

    int toX = x;
    int toY = y;
    int toZ = z;

    double distSq = dx * dx + dy * dy;

    double distance = Math.sqrt(distSq);
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
    m.disregardingGeodata = false;

    if ((Config.GEODATA) && (!isFlying()) && ((!isInsideZone(128)) || (isInsideZone(4))) && (!(this instanceof L2NpcWalkerInstance)))
    {
      double originalDistance = distance;
      int originalX = x;
      int originalY = y;
      int originalZ = z;
      int gtx = originalX - -131072 >> 4;
      int gty = originalY - -262144 >> 4;

      if (((Config.GEO_MOVE_PC) && ((this instanceof L2PcInstance))) || ((Config.GEO_MOVE_NPC) && ((this instanceof L2Attackable)) && (!((L2Attackable)this).isReturningToSpawnPoint())) || (((this instanceof L2Summon)) && (getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)) || (isAfraid()) || ((this instanceof L2RiftInvaderInstance)))
      {
        if (isOnGeodataPath())
        {
          if (_move != null) {
            if ((gtx == _move.geoPathGtx) && (gty == _move.geoPathGty)) {
              return;
            }
            _move.onGeodataPathIndex = -1;
          }
        }

        if ((curX < -131072) || (curX > 228608) || (curY < -262144) || (curY > 262144))
        {
          _log.warning("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
          getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
          if ((this instanceof L2PcInstance))
            ((L2PcInstance)this).deleteMe();
          else
            onDecay();
          return;
        }

        Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z);

        x = destiny.getX();
        y = destiny.getY();
        z = destiny.getZ();
        distance = Math.sqrt((x - curX) * (x - curX) + (y - curY) * (y - curY));
      }

      if ((Config.GEO_PATH_FINDING) && (originalDistance - distance > 1.0D) && (distance < 2000.0D) && (!isAfraid()))
      {
        if (((this instanceof L2PlayableInstance)) || (isInCombat()) || ((this instanceof L2MinionInstance)))
        {
          int gx = curX - -131072 >> 4;
          int gy = curY - -262144 >> 4;
          m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, toX, toY, toZ, this instanceof L2PlayableInstance);
          if ((m.geoPath == null) || (m.geoPath.size() < 2))
          {
            if (((this instanceof L2PcInstance)) || ((!(this instanceof L2PlayableInstance)) && (!(this instanceof L2MinionInstance)) && (Math.abs(z - curZ) > 140)) || (((this instanceof L2Summon)) && (!((L2Summon)this).getFollowStatus())))
            {
              getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
              return;
            }

            m.disregardingGeodata = true;
            x = originalX;
            y = originalY;
            z = originalZ;
            distance = originalDistance;
          }
          else
          {
            m.onGeodataPathIndex = 0;
            m.geoPathGtx = gtx;
            m.geoPathGty = gty;
            m.geoPathAccurateTx = originalX;
            m.geoPathAccurateTy = originalY;

            x = ((AbstractNodeLoc)m.geoPath.get(m.onGeodataPathIndex)).getX();
            y = ((AbstractNodeLoc)m.geoPath.get(m.onGeodataPathIndex)).getY();
            z = ((AbstractNodeLoc)m.geoPath.get(m.onGeodataPathIndex)).getZ();

            if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z))
            {
              m.geoPath = null;
              getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
              return;
            }

            dx = x - curX;
            dy = y - curY;
            distance = Math.sqrt(dx * dx + dy * dy);
            sin = dy / distance;
            cos = dx / distance;
          }

        }

      }

    }

    int ticksToMove = 1 + (int)(10.0D * distance / speed);

    m._xDestination = x;
    m._yDestination = y;
    m._zDestination = z;

    m._heading = 0;
    setHeading(Util.calculateHeadingFrom(cos, sin));

    m._moveStartTime = GameTimeController.getGameTicks();

    if (((this instanceof L2PcInstance)) && (((L2PcInstance)this).getActiveEnchantItem() != null))
    {
      L2PcInstance _player = (L2PcInstance)this;
      _player.setActiveEnchantItem(null);
      _player.sendPacket(new EnchantResult(1));
      _player = null;
    }

    _move = m;

    GameTimeController.getInstance().registerMovingObject(this);

    if (ticksToMove * 100 > 3000)
      ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000L);
  }

  public boolean moveToNextRoutePoint()
  {
    if (!isOnGeodataPath())
    {
      _move = null;
      return false;
    }

    float speed = getStat().getMoveSpeed();
    if ((speed <= 0.0F) || (isMovementDisabled()))
    {
      _move = null;
      return false;
    }

    MoveData m = new MoveData();
    MoveData md = _move;
    if (md == null) {
      return false;
    }

    md.onGeodataPathIndex += 1;
    m.geoPath = md.geoPath;
    m.geoPathGtx = md.geoPathGtx;
    m.geoPathGty = md.geoPathGty;
    m.geoPathAccurateTx = md.geoPathAccurateTx;
    m.geoPathAccurateTy = md.geoPathAccurateTy;

    if (md.onGeodataPathIndex == md.geoPath.size() - 2)
    {
      m._xDestination = md.geoPathAccurateTx;
      m._yDestination = md.geoPathAccurateTy;
      m._zDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getZ();
    }
    else
    {
      m._xDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getX();
      m._yDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getY();
      m._zDestination = ((AbstractNodeLoc)md.geoPath.get(m.onGeodataPathIndex)).getZ();
    }
    double dx = m._xDestination - super.getX();
    double dy = m._yDestination - super.getY();

    double distance = Math.sqrt(dx * dx + dy * dy);
    double sin = dy / distance;
    double cos = dx / distance;

    int ticksToMove = 1 + (int)(10.0D * distance / speed);

    int heading = (int)(Math.atan2(-sin, -cos) * 10430.378000000001D);
    heading += 32768;
    setHeading(heading);
    m._heading = 0;

    m._moveStartTime = GameTimeController.getGameTicks();

    _move = m;

    GameTimeController.getInstance().registerMovingObject(this);

    if (ticksToMove * 100 > 3000) {
      ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000L);
    }

    CharMoveToLocation msg = new CharMoveToLocation(this);
    broadcastPacket(msg);

    return true;
  }

  public boolean validateMovementHeading(int heading)
  {
    MoveData md = _move;

    if (md == null) return true;

    boolean result = true;
    if (md._heading != heading)
    {
      result = md._heading == 0;
      md._heading = heading;
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

    if (strictCheck)
    {
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
    _MyIsAttacking = false;

    if ((target == null) || (isAlikeDead()) || (((this instanceof L2NpcInstance)) && (((L2NpcInstance)this).isEventMob)))
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      sendPacket(new ActionFailed());
      return;
    }

    if ((((this instanceof L2NpcInstance)) && (target.isAlikeDead())) || (target.isDead()) || ((!getKnownList().knowsObject(target)) && (!(this instanceof L2DoorInstance))))
    {
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

      sendPacket(new ActionFailed());
      return;
    }

    if (miss)
    {
      if ((target instanceof L2PcInstance))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK);

        if ((this instanceof L2Summon))
        {
          int mobId = ((L2Summon)this).getTemplate().npcId;
          sm.addNpcName(mobId);
        }
        else
        {
          sm.addString(getName());
        }

        ((L2PcInstance)target).sendPacket(sm);
      }

    }

    if (!isAttackAborted())
    {
      if (target.isRaid())
      {
        int level = 0;
        if ((this instanceof L2PcInstance))
          level = getLevel();
        else if ((this instanceof L2Summon)) {
          level = ((L2Summon)this).getOwner().getLevel();
        }
        if (level > target.getLevel() + 8)
        {
          L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);

          if (skill != null)
            skill.getEffects(target, this);
          else {
            _log.warning("Skill 4515 at level 1 is missing in DP.");
          }
          damage = 0;
        }
      }
      if ((target instanceof L2PcInstance))
      {
        if (((L2PcInstance)target).isInOlympiadMode())
        {
          if ((this instanceof L2PcInstance))
          {
            if (!((L2PcInstance)this).isInOlympiadMode())
            {
              damage = 0;
              Util.handleIllegalPlayerAction((L2PcInstance)this, "Warning! Character " + ((L2PcInstance)this).getName() + " of account " + ((L2PcInstance)this).getAccountName() + " using olympiad bugs.", Config.DEFAULT_PUNISH);
            }
          }
          else if ((this instanceof L2Summon))
          {
            if (!((L2Summon)this).getOwner().isInOlympiadMode())
            {
              damage = 0;
              Util.handleIllegalPlayerAction(((L2Summon)this).getOwner(), "Warning! Character " + ((L2Summon)this).getOwner().getName() + " of account " + ((L2Summon)this).getOwner().getAccountName() + " using olympiad bugs.", Config.DEFAULT_PUNISH);
            }
          }
        }
      }
      sendDamageMessage(target, damage, false, crit, miss);

      if ((target instanceof L2PcInstance))
      {
        L2PcInstance enemy = (L2PcInstance)target;

        if (shld) {
          enemy.sendPacket(new SystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
        }

      }
      else if ((target instanceof L2Summon))
      {
        L2Summon activeSummon = (L2Summon)target;

        SystemMessage sm = new SystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1);
        sm.addString(getName());
        sm.addNumber(damage);
        activeSummon.getOwner().sendPacket(sm);
      }

      if ((!miss) && (damage > 0))
      {
        L2Weapon weapon = getActiveWeaponItem();
        boolean isBow = (weapon != null) && (weapon.getItemType().toString().equalsIgnoreCase("Bow"));

        if (!isBow)
        {
          double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0D, null, null);

          if (reflectPercent > 0.0D)
          {
            int reflectedDamage = (int)(reflectPercent / 100.0D * damage);
            damage -= reflectedDamage;

            if (reflectedDamage > target.getMaxHp()) {
              reflectedDamage = target.getMaxHp();
            }
            getStatus().reduceHp(reflectedDamage, target, true);
          }

          if (target.calcStat(Stats.REFLECT_DAMAGE_PHYSIC, 0.0D, null, null) > 0.0D)
          {
            int reflectDmg = 1189 * target.getPAtk(null) / getPDef(null);
            if ((target instanceof L2PcInstance))
            {
              target.sendPacket(new SystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addString(getName()));
            }
            if ((this instanceof L2PcInstance))
            {
              sendPacket(new SystemMessage(SystemMessageId.S1_IS_PERFORMING_A_COUNTER_ATTACK).addString(target.getName()));
            }
            reduceCurrentHp(reflectDmg, target);
            if (Rnd.get(100) < 80) reduceCurrentHp(reflectDmg, target);

          }

          double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0D, null, null);

          if (absorbPercent > 0.0D)
          {
            int maxCanAbsorb = (int)(getMaxHp() - getCurrentHp());
            int absorbDamage = (int)(absorbPercent / 100.0D * damage);

            if (absorbDamage > maxCanAbsorb) {
              absorbDamage = maxCanAbsorb;
            }
            if ((absorbDamage > 0) && (getCurrentHp() > 1.0D))
            {
              setCurrentHp(getCurrentHp() + absorbDamage);
            }
          }

        }

        target.reduceCurrentHp(damage, this);

        target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
        getAI().clientStartAutoAttack();

        if ((!target.isRaid()) && (Formulas.getInstance().calcAtkBreak(target, damage)))
        {
          target.breakAttack();
          target.breakCast();
        }

        if (_chanceSkills != null) {
          _chanceSkills.onHit(target, false, crit);
        }
        if (target.getChanceSkills() != null) {
          target.getChanceSkills().onHit(this, true, crit);
        }
      }

      L2Weapon activeWeapon = getActiveWeaponItem();

      if (activeWeapon != null) {
        activeWeapon.getSkillEffects(this, target, crit);
      }
      return;
    }
    getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
  }

  public void breakAttack()
  {
    if (isAttackingNow())
    {
      abortAttack();

      if ((this instanceof L2PcInstance))
      {
        sendPacket(new ActionFailed());

        sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
      }
    }
  }

  public void breakCast()
  {
    if ((isCastingNow()) && (canAbortCast()) && (getLastSkillCast() != null) && (getLastSkillCast().isMagic()))
    {
      abortCast();

      if ((this instanceof L2PcInstance))
      {
        sendPacket(new SystemMessage(SystemMessageId.CASTING_INTERRUPTED));
      }
    }
  }

  protected void reduceArrowCount()
  {
  }

  public void onForcedAttack(L2PcInstance player)
  {
    if (isInsidePeaceZone(player))
    {
      if ((!player.isInFunEvent()) || (!player.getTarget().isInFunEvent()))
      {
        player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
        player.sendPacket(new ActionFailed());
      }
    }
    else if ((player.isInOlympiadMode()) && (player.getTarget() != null) && ((player.getTarget() instanceof L2PlayableInstance)))
    {
      L2PcInstance target;
      L2PcInstance target;
      if ((player.getTarget() instanceof L2Summon))
        target = ((L2Summon)player.getTarget()).getOwner();
      else {
        target = (L2PcInstance)player.getTarget();
      }
      if ((target.isInOlympiadMode()) && (!player.isOlympiadStart()) && (player.getOlympiadGameId() != target.getOlympiadGameId()))
      {
        player.sendPacket(new ActionFailed());
      }
      else if ((player.isOlympiadStart()) && (player.getOlympiadGameId() == target.getOlympiadGameId()))
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
    }
    else if ((player.getTarget() != null) && (!player.getTarget().isAttackable()) && (player.getAccessLevel() < Config.GM_PEACEATTACK))
    {
      player.sendPacket(new ActionFailed());
    }
    else if (player.isConfused())
    {
      player.sendPacket(new ActionFailed());
    }
    else if ((this instanceof L2ArtefactInstance))
    {
      player.sendPacket(new ActionFailed());
    }
    else
    {
      if (!GeoData.getInstance().canSeeTarget(player, this))
      {
        player.sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
        player.sendPacket(new ActionFailed());
        return;
      }

      player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
    }
  }

  public boolean isInsidePeaceZone(L2PcInstance attacker)
  {
    if ((!isInFunEvent()) || (!attacker.isInFunEvent()))
    {
      return isInsidePeaceZone(attacker, this);
    }
    return false;
  }

  public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
  {
    return (attacker.getAccessLevel() < Config.GM_PEACEATTACK) && (isInsidePeaceZone(attacker, target));
  }

  public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
  {
    if (target == null) return false;
    if ((target instanceof L2MonsterInstance)) return false;
    if ((target instanceof L2NpcInstance)) return false;
    if ((attacker instanceof L2MonsterInstance)) return false;
    if ((attacker instanceof L2NpcInstance)) return false;
    if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
    {
      if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).getKarma() > 0))
        return false;
      if (((target instanceof L2Summon)) && (((L2Summon)target).getOwner().getKarma() > 0))
        return false;
      if (((attacker instanceof L2PcInstance)) && (((L2PcInstance)attacker).getKarma() > 0))
      {
        if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).getPvpFlag() > 0))
          return false;
        if (((target instanceof L2Summon)) && (((L2Summon)target).getOwner().getPvpFlag() > 0))
          return false;
      }
      if (((attacker instanceof L2Summon)) && (((L2Summon)attacker).getOwner().getKarma() > 0))
      {
        if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).getPvpFlag() > 0))
          return false;
        if (((target instanceof L2Summon)) && (((L2Summon)target).getOwner().getPvpFlag() > 0)) {
          return false;
        }
      }

    }

    if (((attacker instanceof L2Character)) && ((target instanceof L2Character)))
    {
      return (((L2Character)target).isInsideZone(2)) || (((L2Character)attacker).isInsideZone(2));
    }
    if ((attacker instanceof L2Character))
    {
      return (TownManager.getInstance().getTown(target.getX(), target.getY(), target.getZ()) != null) || (((L2Character)attacker).isInsideZone(2));
    }

    return (TownManager.getInstance().getTown(target.getX(), target.getY(), target.getZ()) != null) || (TownManager.getInstance().getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null);
  }

  public Boolean isInActiveRegion()
  {
    try
    {
      L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
      return Boolean.valueOf((region != null) && (region.isActive().booleanValue()));
    }
    catch (Exception e)
    {
      if ((this instanceof L2PcInstance))
      {
        _log.warning("Player " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
        ((L2PcInstance)this).sendMessage("Error with your coordinates! Please reboot your game fully!");
        ((L2PcInstance)this).teleToLocation(80753, 145481, -3532, false);
      }
      else
      {
        _log.warning("Object " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
        decayMe();
      }
    }
    return Boolean.valueOf(false);
  }

  public boolean isInParty()
  {
    return false;
  }

  public L2Party getParty()
  {
    return null;
  }

  public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
  {
    double atkSpd = 0.0D;
    if (weapon != null)
    {
      switch (weapon.getItemType())
      {
      case BOW:
        atkSpd = getStat().getPAtkSpd();
        return (int)(517500.0D / atkSpd);
      case DAGGER:
        atkSpd = getStat().getPAtkSpd();

        break;
      default:
        atkSpd = getStat().getPAtkSpd(); break;
      }
    }
    else {
      atkSpd = getPAtkSpd();
    }
    return Formulas.getInstance().calcPAtkSpd(this, target, atkSpd);
  }

  public int calculateReuseTime(L2Character target, L2Weapon weapon)
  {
    if (weapon == null) return 0;

    int reuse = weapon.getAttackReuseDelay();

    if (reuse == 0) return 0;

    reuse = (int)(reuse * getStat().getReuseModifier(target));
    double atkSpd = getStat().getPAtkSpd();
    switch (weapon.getItemType())
    {
    case BOW:
      return (int)(reuse * 345 / atkSpd);
    }
    return (int)(reuse * 312 / atkSpd);
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

      if ((oldSkill != null) && (_chanceSkills != null))
      {
        removeChanceSkill(oldSkill.getId());
      }

      if (newSkill.isChance())
      {
        addChanceSkill(newSkill);
      }
    }

    return oldSkill;
  }

  public L2Skill removeSkill(L2Skill skill)
  {
    if (skill == null) return null;

    L2Skill oldSkill = (L2Skill)_skills.remove(Integer.valueOf(skill.getId()));

    if (oldSkill != null)
    {
      removeStatsOwner(oldSkill);
      stopSkillEffects(oldSkill.getId());
      if ((oldSkill.isChance()) && (_chanceSkills != null))
      {
        removeChanceSkill(oldSkill.getId());
      }
    }

    return oldSkill;
  }

  public void addChanceSkill(L2Skill skill)
  {
    if (_chanceSkills == null)
    {
      synchronized (this)
      {
        if (_chanceSkills == null)
          _chanceSkills = new ChanceSkillList(this);
        _chanceSkills.put(skill, skill.getChanceCondition());
      }
    }
  }

  public void removeChanceSkill(int id)
  {
    if (_chanceSkills == null) {
      return;
    }
    synchronized (this)
    {
      for (L2Skill skill : _chanceSkills.keySet())
      {
        if (skill.getId() == id)
          _chanceSkills.remove(skill);
      }
      if (_chanceSkills.size() == 0)
        _chanceSkills = null;
    }
  }

  public final L2Skill[] getAllSkills()
  {
    if (_skills == null) {
      return new L2Skill[0];
    }
    return (L2Skill[])_skills.values().toArray(new L2Skill[_skills.values().size()]);
  }

  public ChanceSkillList getChanceSkills()
  {
    return _chanceSkills;
  }

  public int getSkillLevel(int skillId)
  {
    if (_skills == null) {
      return -1;
    }
    L2Skill skill = (L2Skill)_skills.get(Integer.valueOf(skillId));

    if (skill == null)
      return -1;
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
    L2Effect[] effects = getAllEffects();
    int numBuffs = 0;
    if (effects != null)
    {
      for (L2Effect e : effects)
      {
        if (e == null)
          continue;
        if (((e.getSkill().getSkillType() != L2Skill.SkillType.BUFF) && (e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF) && (e.getSkill().getSkillType() != L2Skill.SkillType.REFLECT) && (e.getSkill().getSkillType() != L2Skill.SkillType.HEAL_PERCENT) && (e.getSkill().getSkillType() != L2Skill.SkillType.MANAHEAL_PERCENT)) || ((e.getSkill().getId() > 4360) && (e.getSkill().getId() < 4367)))
        {
          continue;
        }

        numBuffs++;
      }

    }

    return numBuffs;
  }

  public void removeFirstBuff(int preferSkill)
  {
    L2Effect[] effects = getAllEffects();
    L2Effect removeMe = null;
    if (effects != null) {
      for (L2Effect e : effects) {
        if ((e == null) || 
          ((e.getSkill().getSkillType() != L2Skill.SkillType.BUFF) && (e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF) && (e.getSkill().getSkillType() != L2Skill.SkillType.REFLECT) && (e.getSkill().getSkillType() != L2Skill.SkillType.HEAL_PERCENT) && (e.getSkill().getSkillType() != L2Skill.SkillType.MANAHEAL_PERCENT)) || (
          (e.getSkill().getId() > 4360) && (e.getSkill().getId() < 4367)))
        {
          continue;
        }

        if (preferSkill == 0) { removeMe = e; break; }
        if (e.getSkill().getId() == preferSkill) { removeMe = e; break; }
        if (removeMe != null) continue; removeMe = e;
      }

    }

    if (removeMe != null) removeMe.exit();
  }

  public int getDanceCount()
  {
    int danceCount = 0;
    L2Effect[] effects = getAllEffects();
    for (L2Effect effect : effects)
    {
      if (effect == null)
        continue;
      if ((effect.getSkill().isDance()) && (effect.getInUse()))
        danceCount++;
    }
    return danceCount;
  }

  public int getDeBuffCount()
  {
    L2Effect[] effects = getAllEffects();
    int numDeBuffs = 0;

    if (effects != null)
    {
      for (L2Effect e : effects)
      {
        if (e == null)
          continue;
        if (e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF)
          continue;
        numDeBuffs++;
      }

    }

    return numDeBuffs;
  }

  public void removeFirstDeBuff(int preferSkill)
  {
    L2Effect[] effects = getAllEffects();

    L2Effect removeMe = null;

    if (effects != null)
    {
      for (L2Effect e : effects)
      {
        if (e == null)
          continue;
        if (e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF)
          continue;
        if (preferSkill == 0)
        {
          removeMe = e;
          break;
        }
        if (e.getSkill().getId() == preferSkill)
        {
          removeMe = e;
          break;
        }
        if (removeMe != null)
          continue;
        removeMe = e;
      }
    }

    if (removeMe != null)
    {
      removeMe.exit();
    }

    removeMe = null;
  }

  public boolean doesStack(L2Skill checkSkill)
  {
    if ((_effects == null) || (_effects.size() < 1) || (checkSkill._effectTemplates == null) || (checkSkill._effectTemplates.length < 1) || (checkSkill._effectTemplates[0].stackType == null))
    {
      return false;
    }String stackType = checkSkill._effectTemplates[0].stackType;
    if (stackType.equals("none")) return false;

    for (int i = 0; i < _effects.size(); i++) {
      if ((((L2Effect)_effects.get(i)).getStackType() != null) && (((L2Effect)_effects.get(i)).getStackType().equals(stackType)))
        return true;
    }
    return false;
  }

  public void onMagicLaunchedTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant)
  {
    if (skill == null)
    {
      _skillCast = null;
      enableAllSkills();
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if (((targets == null) || (targets.length <= 0)) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA))
    {
      _skillCast = null;
      enableAllSkills();
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    int escapeRange = 0;
    if (skill.getEffectRange() > escapeRange) escapeRange = skill.getEffectRange();
    else if ((skill.getCastRange() < 0) && (skill.getSkillRadius() > 80)) escapeRange = skill.getSkillRadius();

    if (escapeRange > 0)
    {
      List targetList = new FastList();
      for (int i = 0; i < targets.length; i++)
      {
        if (!(targets[i] instanceof L2Character))
          continue;
        if ((!Util.checkIfInRange(escapeRange, this, targets[i], true)) || (!GeoData.getInstance().canSeeTarget(this, targets[i])))
          continue;
        if ((skill.isOffensive()) && 
          ((this instanceof L2PcInstance) ? 
          ((L2Character)targets[i]).isInsidePeaceZone((L2PcInstance)this) : 
          ((L2Character)targets[i]).isInsidePeaceZone(this, targets[i])))
        {
          continue;
        }
        targetList.add((L2Character)targets[i]);
      }

      if ((targetList.isEmpty()) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA))
      {
        abortCast();
        return;
      }
      targets = (L2Object[])targetList.toArray(new L2Character[targetList.size()]);
    }

    if ((!isCastingNow()) || ((isAlikeDead()) && (!skill.isPotion())))
    {
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
    if (!skill.isPotion()) broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));

    if (instant)
      onMagicHitTimer(targets, skill, coolTime, true);
    else
      _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), 200L);
  }

  public void onMagicHitTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant)
  {
    if (skill == null)
    {
      _skillCast = null;
      enableAllSkills();
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if (((targets == null) || (targets.length <= 0)) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA))
    {
      _skillCast = null;
      enableAllSkills();
      getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
      return;
    }

    if ((skill.getItemConsume() > 0) && (!(this instanceof L2NpcInstance)))
    {
      L2ItemInstance requiredItems = ((L2PcInstance)this).getInventory().getItemByItemId(skill.getItemConsumeId());
      if ((requiredItems == null) || (requiredItems.getCount() < skill.getItemConsume()))
      {
        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
        _skillCast = null;
        enableAllSkills();
        getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
        return;
      }
    }

    if (getForceBuff() != null)
    {
      _skillCast = null;
      enableAllSkills();

      getForceBuff().delete();
      return;
    }

    L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
    if (mog != null)
    {
      _skillCast = null;
      enableAllSkills();
      mog.exit();
      return;
    }

    try
    {
      for (int i = 0; i < targets.length; i++)
      {
        if (!(targets[i] instanceof L2PlayableInstance))
          continue;
        L2Character target = (L2Character)targets[i];

        if ((skill.getSkillType() == L2Skill.SkillType.BUFF) || (skill.getSkillType() == L2Skill.SkillType.SEED))
        {
          SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
          smsg.addString(skill.getName());
          target.sendPacket(smsg);
        }

        if ((!(this instanceof L2PcInstance)) || (!(target instanceof L2Summon)))
          continue;
        ((L2Summon)target).getOwner().sendPacket(new PetInfo((L2Summon)target));
        sendPacket(new NpcInfo((L2Summon)target, this));

        ((L2Summon)target).updateEffectIcons(true);
      }

      StatusUpdate su = new StatusUpdate(getObjectId());
      boolean isSendStatus = false;

      double mpConsume = getStat().getMpConsume(skill);
      if (mpConsume > 0.0D)
      {
        if (skill.isDance())
        {
          getStatus().reduceMp(calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null));
        }
        else if (skill.isMagic())
        {
          getStatus().reduceMp(calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null));
        }
        else
        {
          getStatus().reduceMp(calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null));
        }

        su.addAttribute(11, (int)getCurrentMp());
        isSendStatus = true;
      }

      if (skill.getHpConsume() > 0)
      {
        double consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
        if (consumeHp + 1.0D >= getCurrentHp()) {
          consumeHp = getCurrentHp() - 1.0D;
        }
        getStatus().reduceHp(consumeHp, this);

        su.addAttribute(9, (int)getCurrentHp());
        isSendStatus = true;
      }

      if (isSendStatus) sendPacket(su);

      if ((skill.getItemConsume() > 0) && (!(this instanceof L2NpcInstance))) {
        consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
      }

      callSkill(skill, targets);
    }
    catch (NullPointerException e) {
    }
    if ((instant) || (coolTime == 0))
      onMagicFinalizer(targets, skill);
    else
      _skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3), coolTime);
  }

  public void onMagicFinalizer(L2Object[] targets, L2Skill skill)
  {
    _skillCast = null;
    _castEndTime = 0;
    _castInterruptTime = 0;
    enableAllSkills();

    if (((getAI().getNextIntention() == null) && (skill.getSkillType() == L2Skill.SkillType.PDAM) && (skill.getCastRange() < 400)) || (skill.getSkillType() == L2Skill.SkillType.BLOW) || (skill.getSkillType() == L2Skill.SkillType.DRAIN_SOUL) || (skill.getSkillType() == L2Skill.SkillType.SOW) || (skill.getSkillType() == L2Skill.SkillType.SPOIL) || (skill.getSkillType() == L2Skill.SkillType.STUN))
    {
      if ((getTarget() != null) && ((getTarget() instanceof L2Character)) && (getTarget() != this) && (!_MyIsMoveCast))
      {
        getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
      }
    }
    if ((skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_MOB) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB))
    {
      if ((getTarget() != null) && ((getTarget() instanceof L2Attackable)) && (getTarget() != this)) {
        getTarget().decayMe();
      }
    }
    if ((skill.isOffensive()) && (skill.getSkillType() != L2Skill.SkillType.UNLOCK) && (skill.getSkillType() != L2Skill.SkillType.DELUXE_KEY_UNLOCK))
    {
      if (getTarget() != this) {
        getAI().clientStartAutoAttack();
      }
    }
    getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

    if (skill.useSoulShot())
    {
      if ((this instanceof L2NpcInstance))
        ((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
      else if ((this instanceof L2PcInstance))
        ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
      else if ((this instanceof L2Summon))
        ((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);
    }
    else if (skill.useSpiritShot())
    {
      if ((this instanceof L2PcInstance))
        ((L2PcInstance)this).rechargeAutoSoulShot(false, true, false);
      else if ((this instanceof L2Summon)) {
        ((L2Summon)this).getOwner().rechargeAutoSoulShot(false, true, true);
      }

    }

    if ((this instanceof L2PcInstance))
    {
      L2PcInstance currPlayer = (L2PcInstance)this;
      L2PcInstance.SkillDat queuedSkill = currPlayer.getQueuedSkill();

      currPlayer.setCurrentSkill(null, false, false);

      if (queuedSkill != null)
      {
        currPlayer.setQueuedSkill(null, false, false);

        ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
      }

      _MyIsMoveCast = false;
    }
  }

  public void consumeItem(int itemConsumeId, int itemCount)
  {
  }

  public void enableSkill(int skillId)
  {
    if (_disabledSkills == null) return;

    _disabledSkills.remove(new Integer(skillId));

    if ((this instanceof L2PcInstance))
      removeTimeStamp(skillId);
  }

  public void disableSkill(int skillId)
  {
    if (_disabledSkills == null) _disabledSkills = Collections.synchronizedList(new FastList());

    _disabledSkills.add(Integer.valueOf(skillId));
  }

  public void disableSkill(int skillId, long delay)
  {
    disableSkill(skillId);
    if (delay > 10L) ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay);
  }

  public boolean isSkillDisabled(L2Skill skill)
  {
    if (_disabledSkills == null) return false;

    return _disabledSkills.contains(Integer.valueOf(skill.getId()));
  }

  public boolean isSkillDisabled(int skillId)
  {
    if (isAllSkillsDisabled()) return true;

    if (_disabledSkills == null) return false;

    return _disabledSkills.contains(Integer.valueOf(skillId));
  }

  public void disableAllSkills()
  {
    _allSkillsDisabled = true;
  }

  public void enableAllSkills()
  {
    _allSkillsDisabled = false;
  }

  public void callSkill(L2Skill skill, L2Object[] targets)
  {
    try
    {
      ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
      L2Weapon activeWeapon = getActiveWeaponItem();

      player = null;
      if ((this instanceof L2PcInstance))
        player = (L2PcInstance)this;
      else if ((this instanceof L2Summon)) {
        player = ((L2Summon)this).getOwner();
      }
      if ((skill.isToggle()) && (getFirstEffect(skill.getId()) != null)) {
        return;
      }
      for (L2Object trg : targets)
      {
        if (!(trg instanceof L2Character))
          continue;
        L2Character target = (L2Character)trg;
        L2Character targetsAttackTarget = target.getAI().getAttackTarget();
        L2Character targetsCastTarget = target.getAI().getCastTarget();
        if (((target.isRaid()) && (getLevel() > target.getLevel() + 8)) || ((!skill.isOffensive()) && (targetsAttackTarget != null) && (targetsAttackTarget.isRaid()) && (targetsAttackTarget.getAttackByList().contains(target)) && (getLevel() > targetsAttackTarget.getLevel() + 8)) || ((!skill.isOffensive()) && (targetsCastTarget != null) && (targetsCastTarget.isRaid()) && (targetsCastTarget.getAttackByList().contains(target)) && (getLevel() > targetsCastTarget.getLevel() + 8)))
        {
          if (skill.isMagic())
          {
            L2Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
            if (tempSkill != null)
              tempSkill.getEffects(target, this);
            else
              _log.warning("Skill 4215 at level 1 is missing in DP.");
          }
          else
          {
            L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
            if (tempSkill != null)
              tempSkill.getEffects(target, this);
            else
              _log.warning("Skill 4515 at level 1 is missing in DP.");
          }
          return;
        }

        if ((target instanceof L2PcInstance))
        {
          if (((L2PcInstance)target).isInOlympiadMode())
          {
            if ((this instanceof L2PcInstance))
            {
              if (!((L2PcInstance)this).isInOlympiadMode())
              {
                Util.handleIllegalPlayerAction((L2PcInstance)this, "Warning! Character " + ((L2PcInstance)this).getName() + " of account " + ((L2PcInstance)this).getAccountName() + " using olympiad bugs.", Config.DEFAULT_PUNISH);
                return;
              }
            }
            else if ((this instanceof L2Summon))
            {
              if (!((L2Summon)this).getOwner().isInOlympiadMode())
              {
                Util.handleIllegalPlayerAction(((L2Summon)this).getOwner(), "Warning! Character " + ((L2Summon)this).getOwner().getName() + " of account " + ((L2Summon)this).getOwner().getAccountName() + " using olympiad bugs.", Config.DEFAULT_PUNISH);
                return;
              }
            }
          }
        }

        if (skill.isOverhit())
        {
          if ((target instanceof L2Attackable)) {
            ((L2Attackable)target).overhitEnabled(true);
          }
        }
        if ((activeWeapon != null) && (!target.isDead()))
        {
          if ((activeWeapon.getSkillEffects(this, target, skill).length > 0) && ((this instanceof L2PcInstance)))
          {
            ((L2PcInstance)this).sendMessage("Target affected by weapon special ability!");
          }
        }

        if (_chanceSkills != null)
          _chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
        if (target.getChanceSkills() != null) {
          target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
        }
      }
      if (handler != null)
        handler.useSkill(this, skill, targets);
      else {
        skill.useSkill(this, targets);
      }
      if (player != null)
      {
        for (L2Object target : targets)
        {
          if (!(target instanceof L2Character))
            continue;
          if (skill.isOffensive())
          {
            if (((target instanceof L2PcInstance)) || ((target instanceof L2Summon)))
            {
              if ((skill.getSkillType() == L2Skill.SkillType.SIGNET) || (skill.getSkillType() == L2Skill.SkillType.SIGNET_CASTTIME))
                continue;
              if ((skill.getSkillType() != L2Skill.SkillType.AGGREDUCE) && (skill.getSkillType() != L2Skill.SkillType.AGGREDUCE_CHAR) && (skill.getSkillType() != L2Skill.SkillType.AGGREMOVE))
              {
                ((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
              }
              player.updatePvPStatus((L2Character)target);
            }
            else {
              if (!(target instanceof L2Attackable))
                continue;
              if ((skill.getSkillType() == L2Skill.SkillType.AGGREDUCE) || (skill.getSkillType() == L2Skill.SkillType.AGGREDUCE_CHAR) || (skill.getSkillType() == L2Skill.SkillType.AGGREMOVE))
              {
                continue;
              }
              ((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
            }

          }
          else if ((target instanceof L2PcInstance))
          {
            if ((target.equals(this)) || ((((L2PcInstance)target).getPvpFlag() <= 0) && (((L2PcInstance)target).getKarma() <= 0)))
              continue;
            player.updatePvPStatus();
          } else {
            if ((!(target instanceof L2Attackable)) || (skill.getSkillType() == L2Skill.SkillType.SUMMON) || (skill.getSkillType() == L2Skill.SkillType.BEAST_FEED) || (skill.getSkillType() == L2Skill.SkillType.UNLOCK) || (skill.getSkillType() == L2Skill.SkillType.DELUXE_KEY_UNLOCK))
            {
              continue;
            }

            player.updatePvPStatus();
          }
        }

        Collection objs = player.getKnownList().getKnownObjects().values();

        for (L2Object spMob : objs)
        {
          if ((spMob instanceof L2NpcInstance))
          {
            L2NpcInstance npcMob = (L2NpcInstance)spMob;

            if ((npcMob.isInsideRadius(player, 1000, true, true)) && (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null))
            {
              for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
                quest.notifySkillSee(npcMob, player, skill, targets, this instanceof L2Summon);
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      L2PcInstance player;
      _log.log(Level.WARNING, "", e);
    }
  }

  public void seeSpell(L2PcInstance caster, L2Object target, L2Skill skill) {
    if ((this instanceof L2Attackable))
      ((L2Attackable)this).addDamageHate(caster, 0, -skill.getAggroPoints());
  }

  public boolean isBehind(L2Object target)
  {
    double maxAngleDiff = 45.0D;

    if (target == null) {
      return false;
    }
    if ((target instanceof L2Character))
    {
      L2Character target1 = (L2Character)target;
      double angleChar = Util.calculateAngleFrom(target1, this);
      double angleTarget = Util.convertHeadingToDegree(target1.getHeading());
      double angleDiff = angleChar - angleTarget;
      if (angleDiff <= -360.0D + maxAngleDiff) angleDiff += 360.0D;
      if (angleDiff >= 360.0D - maxAngleDiff) angleDiff -= 360.0D;
      if (Math.abs(angleDiff) <= maxAngleDiff)
      {
        return true;
      }
    }
    else
    {
      _log.fine("isBehindTarget's target not an L2 Character.");
    }
    return false;
  }

  public boolean isBehindTarget()
  {
    return isBehind(getTarget());
  }

  public boolean isFront(L2Object target)
  {
    double maxAngleDiff = 45.0D;
    if (target == null)
      return false;
    if ((target instanceof L2Character))
    {
      L2Character target1 = (L2Character)target;
      double angleChar = Util.calculateAngleFrom(target1, this);
      double angleTarget = Util.convertHeadingToDegree(target1.getHeading());
      double angleDiff = angleChar - angleTarget;
      if (angleDiff <= -180.0D + maxAngleDiff) angleDiff += 180.0D;
      if (angleDiff >= 180.0D - maxAngleDiff) angleDiff -= 180.0D;
      if (Math.abs(angleDiff) <= maxAngleDiff)
      {
        return true;
      }
    }
    else
    {
      _log.fine("isSideTarget's target not an L2 Character.");
    }
    return false;
  }

  public boolean isFacing(L2Object target, int maxAngle)
  {
    if (target == null)
      return false;
    double maxAngleDiff = maxAngle / 2;
    double angleTarget = Util.calculateAngleFrom(this, target);
    double angleChar = Util.convertHeadingToDegree(getHeading());
    double angleDiff = angleChar - angleTarget;
    if (angleDiff <= -180.0D + maxAngleDiff) angleDiff += 180.0D;
    if (angleDiff >= 180.0D - maxAngleDiff) angleDiff -= 180.0D;

    return Math.abs(angleDiff) <= maxAngleDiff;
  }

  public boolean isFrontTarget()
  {
    return isFront(getTarget());
  }

  public double getLevelMod()
  {
    return 1.0D;
  }

  public final void setSkillCast(Future newSkillCast)
  {
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

  public long getPvpFlagLasts()
  {
    return _pvpFlagLasts;
  }

  public void startPvPFlag()
  {
    updatePvPFlag(1);

    _PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000L, 1000L);
  }

  public void stopPvpRegTask()
  {
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
    if (!(this instanceof L2PcInstance))
      return;
    L2PcInstance player = (L2PcInstance)this;
    if (player.getPvpFlag() == value)
      return;
    player.setPvpFlag(value);
    player.sendPacket(new UserInfo(player));
    if (player.getPet() != null)
    {
      player.sendPacket(new PetInfo(player.getPet()));
    }
    for (L2PcInstance target : getKnownList().getKnownPlayers().values())
    {
      target.sendPacket(new RelationChanged(player, player.getRelation(player), player.isAutoAttackable(target)));
      if (player.getPet() != null)
      {
        target.sendPacket(new RelationChanged(player.getPet(), player.getRelation(player), player.isAutoAttackable(target)));
      }
    }
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

  public int getAttackEndTime()
  {
    return _attackEndTime;
  }

  public abstract int getLevel();

  public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
  {
    return getStat().calcStat(stat, init, target, skill);
  }
  public int getAccuracy() {
    return getStat().getAccuracy(); } 
  public final float getAttackSpeedMultiplier() { return getStat().getAttackSpeedMultiplier(); } 
  public int getCON() { return getStat().getCON(); } 
  public int getDEX() { return getStat().getDEX(); } 
  public final double getCriticalDmg(L2Character target, double init) { return getStat().getCriticalDmg(target, init); } 
  public int getCriticalHit(L2Character target, L2Skill skill) { return getStat().getCriticalHit(target, skill); } 
  public int getEvasionRate(L2Character target) { return getStat().getEvasionRate(target); } 
  public int getINT() { return getStat().getINT(); } 
  public final int getMagicalAttackRange(L2Skill skill) { return getStat().getMagicalAttackRange(skill); } 
  public final int getMaxCp() { return getStat().getMaxCp(); } 
  public int getMAtk(L2Character target, L2Skill skill) { return getStat().getMAtk(target, skill); }

  public int getMAtkSpd()
  {
    if (Config.MAX_MATK_SPEED > 0)
    {
      if (getStat().getMAtkSpd() > Config.MAX_MATK_SPEED)
        return Config.MAX_MATK_SPEED;
    }
    return getStat().getMAtkSpd();
  }
  public int getMaxMp() {
    return getStat().getMaxMp(); } 
  public int getMaxHp() { return getStat().getMaxHp(); } 
  public final int getMCriticalHit(L2Character target, L2Skill skill) { return getStat().getMCriticalHit(target, skill); } 
  public int getMDef(L2Character target, L2Skill skill) { return getStat().getMDef(target, skill); } 
  public int getMEN() { return getStat().getMEN(); } 
  public double getMReuseRate(L2Skill skill) { return getStat().getMReuseRate(skill); } 
  public float getMovementSpeedMultiplier() { return getStat().getMovementSpeedMultiplier(); } 
  public int getPAtk(L2Character target) { return getStat().getPAtk(target); } 
  public double getPAtkAnimals(L2Character target) { return getStat().getPAtkAnimals(target); } 
  public double getPAtkDragons(L2Character target) { return getStat().getPAtkDragons(target); } 
  public double getPAtkInsects(L2Character target) { return getStat().getPAtkInsects(target); } 
  public double getPAtkMonsters(L2Character target) { return getStat().getPAtkMonsters(target); } 
  public double getPAtkPlants(L2Character target) { return getStat().getPAtkPlants(target); }

  public int getPAtkSpd()
  {
    if (Config.MAX_PATK_SPEED > 0)
    {
      if (getStat().getPAtkSpd() > Config.MAX_PATK_SPEED)
        return Config.MAX_PATK_SPEED;
    }
    return getStat().getPAtkSpd();
  }
  public double getPAtkUndead(L2Character target) {
    return getStat().getPAtkUndead(target); } 
  public double getPDefUndead(L2Character target) { return getStat().getPDefUndead(target); } 
  public int getPDef(L2Character target) { return getStat().getPDef(target); } 
  public final int getPhysicalAttackRange() { return getStat().getPhysicalAttackRange(); } 
  public int getRunSpeed() { return getStat().getRunSpeed(); } 
  public final int getShldDef() { return getStat().getShldDef(); } 
  public int getSTR() { return getStat().getSTR(); } 
  public final int getWalkSpeed() { return getStat().getWalkSpeed(); } 
  public int getWIT() { return getStat().getWIT();
  }

  public void addStatusListener(L2Character object)
  {
    getStatus().addStatusListener(object); } 
  public void reduceCurrentHp(double i, L2Character attacker) { reduceCurrentHp(i, attacker, true); }

  public void reduceCurrentHp(double i, L2Character attacker, boolean awake) {
    if (((this instanceof L2NpcInstance)) && (((attacker instanceof L2PcInstance)) || ((attacker instanceof L2Summon))) && 
      (Config.INVUL_NPC_LIST.contains(Integer.valueOf(((L2NpcInstance)this).getNpcId()))))
      return;
    if ((Config.CHAMPION_ENABLE) && (isChampion()) && (Config.CHAMPION_HP != 0))
      getStatus().reduceHp(i / Config.CHAMPION_HP, attacker, awake);
    else
      getStatus().reduceHp(i, attacker, awake); 
  }
  public void reduceCurrentMp(double i) {
    getStatus().reduceMp(i); } 
  public void removeStatusListener(L2Character object) { getStatus().removeStatusListener(object); } 
  protected void stopHpMpRegeneration() { getStatus().stopHpMpRegeneration(); }

  public final double getCurrentCp() {
    return getStatus().getCurrentCp(); } 
  public final void setCurrentCp(Double newCp) { setCurrentCp(newCp.doubleValue()); } 
  public final void setCurrentCp(double newCp) { getStatus().setCurrentCp(newCp); } 
  public final double getCurrentHp() { return getStatus().getCurrentHp(); } 
  public final void setCurrentHp(double newHp) { getStatus().setCurrentHp(newHp); } 
  public final void setCurrentHpMp(double newHp, double newMp) { getStatus().setCurrentHpMp(newHp, newMp); } 
  public final double getCurrentMp() { return getStatus().getCurrentMp(); } 
  public final void setCurrentMp(Double newMp) { setCurrentMp(newMp.doubleValue()); } 
  public final void setCurrentMp(double newMp) { getStatus().setCurrentMp(newMp);
  }

  public void setAiClass(String aiClass)
  {
    _aiClass = aiClass;
  }

  public String getAiClass()
  {
    return _aiClass;
  }

  public L2Character getLastBuffer()
  {
    return _lastBuffer;
  }

  public int getLastHealAmount()
  {
    return _lastHealAmount;
  }

  public void setLastBuffer(L2Character buffer)
  {
    _lastBuffer = buffer;
  }

  public void setLastHealAmount(int hp)
  {
    _lastHealAmount = hp;
  }

  public void setDestination(int x, int y, int z)
  {
    MoveData m = new MoveData();
    m._xDestination = x;
    m._yDestination = y;
    m._zDestination = z;
  }

  public boolean reflectSkill(L2Skill skill)
  {
    double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0.0D, null, null);

    if ((!skill.isMagic()) && (skill.getCastRange() < 100))
    {
      double reflectMeleeSkill = calcStat(Stats.REFLECT_SKILL_MELEE_PHYSIC, 0.0D, null, null);
      reflect = reflectMeleeSkill > reflect ? reflectMeleeSkill : reflect;
    }

    return Rnd.get(100) < reflect;
  }

  public boolean reflectDamageSkill(L2Skill skill)
  {
    double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_DAMAGE_MAGIC : Stats.REFLECT_DAMAGE_PHYSIC, 0.0D, null, null);
    return Rnd.get(100) < reflect;
  }

  public boolean reflectRevengeSkill(L2Skill skill)
  {
    double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_DAMAGE_MAGIC : Stats.REFLECT_REVENGE_SKILL, 0.0D, null, null);
    return Rnd.get(100) < reflect;
  }

  public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
  {
  }

  public ForceBuff getForceBuff()
  {
    return null;
  }

  public void setForceBuff(ForceBuff fb)
  {
  }

  public boolean isRaidMinion()
  {
    return _isMinion;
  }

  public void setIsRaidMinion(boolean val)
  {
    _isRaid = val;
    _isMinion = val;
  }

  public void setChampion(boolean champ)
  {
    _champion = champ;
  }

  public boolean isChampion()
  {
    return _champion;
  }

  public void setPremiumService(int PS)
  {
    _premiumsystem = PS;
  }

  public int getPremiumService()
  {
    return _premiumsystem;
  }

  public final void setIsBuffBlocked(boolean value)
  {
    _isBuffBlocked = value;
  }

  public boolean isBuffBlocked()
  {
    return _isBuffBlocked;
  }

  public void turn(L2Character target)
  {
    if (!Config.TURN_HEADING) return;
    double dx = target.getX() - getX();
    double dy = target.getY() - getY();
    double distance = Math.sqrt(dx * dx + dy * dy);
    double sin = dy / distance;
    double cos = dx / distance;
    int newHeading = Util.calculateHeadingFrom(cos, sin);
    if (getHeading() != newHeading)
    {
      broadcastPacket(new BeginRotation(getObjectId(), newHeading, 1, 0));
      setHeading(newHeading);
      broadcastPacket(new StopRotation(getObjectId(), newHeading, 0));
    }
  }

  public static class MoveData
  {
    public int _moveStartTime;
    public int _moveTimestamp;
    public int _xDestination;
    public int _yDestination;
    public int _zDestination;
    public double _xAccurate;
    public double _yAccurate;
    public double _zAccurate;
    public int _yMoveFrom;
    public int _zMoveFrom;
    public int _heading;
    public boolean disregardingGeodata;
    public int onGeodataPathIndex;
    public List<AbstractNodeLoc> geoPath;
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
      if ((isAttackingNow()) && (skill.isActive()))
      {
        if (!getCanCastAA())
        {
          setCanCastAA(true);
        }
        else
        {
          setCanCastAA(false);
        }
        return;
      }
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
        {
          stopPvPFlag();
        }
        else if (System.currentTimeMillis() > getPvpFlagLasts() - 5000L)
        {
          updatePvPFlag(2);
        }
        else
        {
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

    NotifyAITask(CtrlEvent evt)
    {
      _evt = evt;
    }

    public void run()
    {
      try
      {
        getAI().notifyEvent(_evt, null);
      }
      catch (Throwable t)
      {
        L2Character._log.log(Level.WARNING, "", t);
      }
    }
  }

  class QueuedMagicUseTask
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
      try
      {
        _currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
      }
      catch (Throwable e)
      {
        L2Character._log.log(Level.SEVERE, "", e);
      }
    }
  }

  class MagicUseTask
    implements Runnable
  {
    L2Object[] _targets;
    L2Skill _skill;
    int _coolTime;
    int _phase;

    public MagicUseTask(L2Object[] targets, L2Skill skill, int coolTime, int phase)
    {
      _targets = targets;
      _skill = skill;
      _coolTime = coolTime;
      _phase = phase;
    }

    public void run()
    {
      try
      {
        switch (_phase)
        {
        case 1:
          onMagicLaunchedTimer(_targets, _skill, _coolTime, false);
          break;
        case 2:
          onMagicHitTimer(_targets, _skill, _coolTime, false);
          break;
        case 3:
          onMagicFinalizer(_targets, _skill);
        }

      }
      catch (Throwable e)
      {
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

    public void run()
    {
      try
      {
        onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
      }
      catch (Throwable e)
      {
        L2Character._log.warning(e.toString());
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

    public void run()
    {
      try
      {
        enableSkill(_skillId);
      } catch (Throwable e) {
        L2Character._log.log(Level.SEVERE, "", e);
      }
    }
  }

  public class CheckFalling
    implements Runnable
  {
    private int _fallHeight;
    private Future _task;

    public CheckFalling(int fallHeight)
    {
      _fallHeight = fallHeight;
    }

    public void setTask(Future task)
    {
      _task = task;
    }

    public void run()
    {
      if (_task != null)
      {
        _task.cancel(true);
        _task = null;
      }

      try
      {
        isFalling(true, _fallHeight);
      }
      catch (Throwable e)
      {
        L2Character._log.log(Level.SEVERE, "L2PcInstance.CheckFalling exception ", e);
      }
    }
  }
}