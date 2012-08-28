package l2p.gameserver.model;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.ai.SummonAI;
import l2p.gameserver.dao.EffectsDAO;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.actor.recorder.SummonStatsChangeRecorder;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.entity.events.impl.DuelEvent;
import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PetInventory;
import l2p.gameserver.scripts.Events;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.AutoAttackStart;
import l2p.gameserver.serverpackets.ExPartyPetWindowAdd;
import l2p.gameserver.serverpackets.ExPartyPetWindowDelete;
import l2p.gameserver.serverpackets.ExPartyPetWindowUpdate;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.NpcInfo;
import l2p.gameserver.serverpackets.PartySpelled;
import l2p.gameserver.serverpackets.PetDelete;
import l2p.gameserver.serverpackets.PetInfo;
import l2p.gameserver.serverpackets.PetItemList;
import l2p.gameserver.serverpackets.PetStatusShow;
import l2p.gameserver.serverpackets.PetStatusUpdate;
import l2p.gameserver.serverpackets.RelationChanged;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.taskmanager.DecayTaskManager;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

public abstract class Summon extends Playable
{
  private static final int SUMMON_DISAPPEAR_RANGE = 2500;
  private final Player _owner;
  private int _spawnAnimation = 2;
  protected long _exp = 0L;
  protected int _sp = 0;
  private int _maxLoad;
  private int _spsCharged;
  private boolean _follow = true; private boolean _depressed = false; private boolean _ssCharged = false;
  private Future<?> _decayTask;
  private Future<?> _updateEffectIconsTask;
  private ScheduledFuture<?> _broadcastCharInfoTask;
  private Future<?> _petInfoTask;

  public Summon(int objectId, NpcTemplate template, Player owner)
  {
    super(objectId, template);
    _owner = owner;
    TIntObjectIterator iterator;
    if (template.getSkills().size() > 0) {
      for (iterator = template.getSkills().iterator(); iterator.hasNext(); )
      {
        iterator.advance();
        addSkill((Skill)iterator.value());
      }
    }
    setXYZ(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
  }

  protected void onSpawn()
  {
    super.onSpawn();

    _spawnAnimation = 0;

    Player owner = getPlayer();
    Party party = owner.getParty();
    if (party != null)
      party.broadcastToPartyMembers(owner, new ExPartyPetWindowAdd(this));
    getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
  }

  public SummonAI getAI()
  {
    if (_ai == null) {
      synchronized (this)
      {
        if (_ai == null)
          _ai = new SummonAI(this);
      }
    }
    return (SummonAI)_ai;
  }

  public NpcTemplate getTemplate()
  {
    return (NpcTemplate)_template;
  }

  public boolean isUndead()
  {
    return getTemplate().isUndead();
  }

  public abstract int getSummonType();

  public abstract int getEffectIdentifier();

  public boolean isMountable()
  {
    return false;
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

    Player owner = getPlayer();

    if (player.getTarget() != this)
    {
      player.setTarget(this);
      if (player.getTarget() == this)
      {
        player.sendPacket(new IStaticPacket[] { new MyTargetSelected(getObjectId(), 0), makeStatusUpdate(new int[] { 9, 10, 11, 12 }) });
      }
      else
        player.sendPacket(ActionFail.STATIC);
    }
    else if (player == owner)
    {
      player.sendPacket(new PetInfo(this).update());

      if (!player.isActionsDisabled()) {
        player.sendPacket(new PetStatusShow(this));
      }
      player.sendPacket(ActionFail.STATIC);
    }
    else if (isAutoAttackable(player)) {
      player.getAI().Attack(this, false, shift);
    }
    else if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
    {
      if (!shift)
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Integer.valueOf(Config.FOLLOW_RANGE));
      else
        player.sendActionFailed();
    }
    else {
      player.sendActionFailed();
    }
  }

  public long getExpForThisLevel()
  {
    return Experience.getExpForLevel(getLevel());
  }

  public long getExpForNextLevel()
  {
    return Experience.getExpForLevel(getLevel() + 1);
  }

  public int getNpcId()
  {
    return getTemplate().npcId;
  }

  public final long getExp()
  {
    return _exp;
  }

  public final void setExp(long exp)
  {
    _exp = exp;
  }

  public final int getSp()
  {
    return _sp;
  }

  public void setSp(int sp)
  {
    _sp = sp;
  }

  public int getMaxLoad()
  {
    return _maxLoad;
  }

  public void setMaxLoad(int maxLoad)
  {
    _maxLoad = maxLoad;
  }

  public int getBuffLimit()
  {
    Player owner = getPlayer();
    return (int)calcStat(Stats.BUFF_LIMIT, owner.getBuffLimit(), null, null);
  }

  public int getSongLimit()
  {
    Player owner = getPlayer();
    return (int)calcStat(Stats.SONG_LIMIT, owner.getSongLimit(), null, null);
  }

  public abstract int getCurrentFed();

  public abstract int getMaxFed();

  protected void onDeath(Creature killer) {
    super.onDeath(killer);

    startDecay(8500L);

    Player owner = getPlayer();

    if ((killer == null) || (killer == owner) || (killer == this) || (isInZoneBattle()) || (killer.isInZoneBattle())) {
      return;
    }
    if ((killer instanceof Summon)) {
      killer = killer.getPlayer();
    }
    if (killer == null) {
      return;
    }
    if (killer.isPlayer())
    {
      Player pk = (Player)killer;

      if (isInZone(Zone.ZoneType.SIEGE)) {
        return;
      }
      DuelEvent duelEvent = (DuelEvent)getEvent(DuelEvent.class);
      if ((owner.getPvpFlag() > 0) || (owner.atMutualWarWith(pk))) {
        pk.setPvpKills(pk.getPvpKills() + 1);
      } else if (((duelEvent == null) || (duelEvent != pk.getEvent(DuelEvent.class))) && (getKarma() <= 0))
      {
        int pkCountMulti = Math.max(pk.getPkKills() / 2, 1);
        pk.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
      }

      pk.sendChanges();
    }
  }

  protected void startDecay(long delay)
  {
    stopDecay();
    _decayTask = DecayTaskManager.getInstance().addDecayTask(this, delay);
  }

  protected void stopDecay()
  {
    if (_decayTask != null)
    {
      _decayTask.cancel(false);
      _decayTask = null;
    }
  }

  protected void onDecay()
  {
    deleteMe();
  }

  public void endDecayTask()
  {
    stopDecay();
    doDecay();
  }

  public void broadcastStatusUpdate()
  {
    if (!needStatusUpdate()) {
      return;
    }
    Player owner = getPlayer();

    sendStatusUpdate();

    StatusUpdate su = makeStatusUpdate(new int[] { 10, 9 });
    broadcastToStatusListeners(new L2GameServerPacket[] { su });

    Party party = owner.getParty();
    if (party != null)
      party.broadcastToPartyMembers(owner, new ExPartyPetWindowUpdate(this));
  }

  public void sendStatusUpdate()
  {
    Player owner = getPlayer();
    owner.sendPacket(new PetStatusUpdate(this));
  }

  protected void onDelete()
  {
    Player owner = getPlayer();

    Party party = owner.getParty();
    if (party != null)
      party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
    owner.sendPacket(new PetDelete(getObjectId(), getSummonType()));
    owner.setPet(null);

    stopDecay();
    super.onDelete();
  }

  public void unSummon()
  {
    deleteMe();
  }

  public void saveEffects()
  {
    Player owner = getPlayer();
    if (owner == null) {
      return;
    }
    if (owner.isInOlympiadMode()) {
      getEffectList().stopAllEffects();
    }
    EffectsDAO.getInstance().insert(this);
  }

  public void setFollowMode(boolean state)
  {
    Player owner = getPlayer();

    _follow = state;

    if (_follow)
    {
      if (getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
        getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Integer.valueOf(Config.FOLLOW_RANGE));
    }
    else if (getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
  }

  public boolean isFollowMode()
  {
    return _follow;
  }

  public void updateEffectIcons()
  {
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

  public void updateEffectIconsImpl()
  {
    Player owner = getPlayer();
    PartySpelled ps = new PartySpelled(this, true);
    Party party = owner.getParty();
    if (party != null)
      party.broadCast(new IStaticPacket[] { ps });
    else
      owner.sendPacket(ps);
  }

  public int getControlItemObjId()
  {
    return 0;
  }

  public PetInventory getInventory()
  {
    return null;
  }

  public void doPickupItem(GameObject object)
  {
  }

  public void doRevive()
  {
    super.doRevive();
    setRunning();
    getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    setFollowMode(true);
  }

  public ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getActiveWeaponItem()
  {
    return null;
  }

  public ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getSecondaryWeaponItem()
  {
    return null;
  }

  public abstract void displayGiveDamageMessage(Creature paramCreature, int paramInt, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4);

  public abstract void displayReceiveDamageMessage(Creature paramCreature, int paramInt);

  public boolean unChargeShots(boolean spirit)
  {
    Player owner = getPlayer();

    if (spirit)
    {
      if (_spsCharged != 0)
      {
        _spsCharged = 0;
        owner.autoShot();
        return true;
      }
    }
    else if (_ssCharged)
    {
      _ssCharged = false;
      owner.autoShot();
      return true;
    }

    return false;
  }

  public boolean getChargedSoulShot()
  {
    return _ssCharged;
  }

  public int getChargedSpiritShot()
  {
    return _spsCharged;
  }

  public void chargeSoulShot()
  {
    _ssCharged = true;
  }

  public void chargeSpiritShot(int state)
  {
    _spsCharged = state;
  }

  public int getSoulshotConsumeCount()
  {
    return getLevel() / 27 + 1;
  }

  public int getSpiritshotConsumeCount()
  {
    return getLevel() / 58 + 1;
  }

  public boolean isDepressed()
  {
    return _depressed;
  }

  public void setDepressed(boolean depressed)
  {
    _depressed = depressed;
  }

  public boolean isInRange()
  {
    Player owner = getPlayer();
    return getDistance(owner) < 2500.0D;
  }

  public void teleportToOwner()
  {
    Player owner = getPlayer();

    setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
    if (owner.isInOlympiadMode())
      teleToLocation(owner.getLoc(), owner.getReflection());
    else {
      teleToLocation(Location.findPointToStay(owner, 50, 150), owner.getReflection());
    }
    if ((!isDead()) && (_follow))
      getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Integer.valueOf(Config.FOLLOW_RANGE));
  }

  public void broadcastCharInfo()
  {
    if (_broadcastCharInfoTask != null) {
      return;
    }
    _broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
  }

  public void broadcastCharInfoImpl()
  {
    Player owner = getPlayer();

    for (Player player : World.getAroundPlayers(this))
      if (player == owner)
        player.sendPacket(new PetInfo(this).update());
      else
        player.sendPacket(new NpcInfo(this, player).update());
  }

  private void sendPetInfoImpl()
  {
    Player owner = getPlayer();
    owner.sendPacket(new PetInfo(this).update());
  }

  public void sendPetInfo()
  {
    if (Config.USER_INFO_INTERVAL == 0L)
    {
      if (_petInfoTask != null)
      {
        _petInfoTask.cancel(false);
        _petInfoTask = null;
      }
      sendPetInfoImpl();
      return;
    }

    if (_petInfoTask != null) {
      return;
    }
    _petInfoTask = ThreadPoolManager.getInstance().schedule(new PetInfoTask(null), Config.USER_INFO_INTERVAL);
  }

  public int getSpawnAnimation()
  {
    return _spawnAnimation;
  }

  public void startPvPFlag(Creature target)
  {
    Player owner = getPlayer();
    owner.startPvPFlag(target);
  }

  public int getPvpFlag()
  {
    Player owner = getPlayer();
    return owner.getPvpFlag();
  }

  public int getKarma()
  {
    Player owner = getPlayer();
    return owner.getKarma();
  }

  public TeamType getTeam()
  {
    Player owner = getPlayer();
    return owner.getTeam();
  }

  public Player getPlayer()
  {
    return _owner;
  }

  public abstract double getExpPenalty();

  public SummonStatsChangeRecorder getStatsRecorder()
  {
    if (_statsRecorder == null) {
      synchronized (this)
      {
        if (_statsRecorder == null)
          _statsRecorder = new SummonStatsChangeRecorder(this);
      }
    }
    return (SummonStatsChangeRecorder)_statsRecorder;
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    List list = new ArrayList();
    Player owner = getPlayer();

    if (owner == forPlayer)
    {
      list.add(new PetInfo(this));
      list.add(new PartySpelled(this, true));

      if (isPet())
        list.add(new PetItemList((PetInstance)this));
    }
    else
    {
      Party party = forPlayer.getParty();
      if ((getReflection() == ReflectionManager.GIRAN_HARBOR) && ((owner == null) || (party == null) || (party != owner.getParty())))
        return list;
      list.add(new NpcInfo(this, forPlayer));
      if ((owner != null) && (party != null) && (party == owner.getParty()))
        list.add(new PartySpelled(this, true));
      list.add(RelationChanged.update(forPlayer, this, forPlayer));
    }

    if (isInCombat()) {
      list.add(new AutoAttackStart(getObjectId()));
    }
    if ((isMoving) || (isFollow))
      list.add(movePacket());
    return list;
  }

  public void startAttackStanceTask()
  {
    startAttackStanceTask0();
    Player player = getPlayer();
    if (player != null)
      player.startAttackStanceTask0();
  }

  public <E extends GlobalEvent> E getEvent(Class<E> eventClass)
  {
    Player player = getPlayer();
    if (player != null) {
      return player.getEvent(eventClass);
    }
    return super.getEvent(eventClass);
  }

  public Set<GlobalEvent> getEvents()
  {
    Player player = getPlayer();
    if (player != null) {
      return player.getEvents();
    }
    return super.getEvents();
  }

  public void sendReuseMessage(Skill skill)
  {
    Player player = getPlayer();
    if ((player != null) && (isSkillDisabled(skill)))
      player.sendPacket(SystemMsg.THAT_PET_SERVITOR_SKILL_CANNOT_BE_USED_BECAUSE_IT_IS_RECHARGING);
  }

  private class PetInfoTask extends RunnableImpl
  {
    private PetInfoTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      Summon.this.sendPetInfoImpl();
      Summon.access$402(Summon.this, null);
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
      broadcastCharInfoImpl();
      Summon.access$202(Summon.this, null);
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
      updateEffectIconsImpl();
      Summon.access$002(Summon.this, null);
    }
  }
}