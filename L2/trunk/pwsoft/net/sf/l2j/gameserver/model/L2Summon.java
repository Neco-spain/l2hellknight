package net.sf.l2j.gameserver.model;

import java.util.concurrent.ScheduledFuture;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.SummonKnownList;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.model.actor.status.SummonStatus;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.PetDelete;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusShow;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.PeaceZone;

public abstract class L2Summon extends L2PlayableInstance
{
  protected int _pkKills;
  private byte _pvpFlag;
  private L2PcInstance _owner;
  private int _karma = 0;
  private int _attackRange = 36;
  private boolean _follow = true;
  private boolean _previousFollowStatus = true;
  private int _maxLoad;
  private int _chargedSoulShot;
  private int _chargedSpiritShot;
  private int _soulShotsPerHit = 1;
  private int _spiritShotsPerHit = 1;
  protected boolean _showSumAnim;
  private Config.EventReward _healData;
  private ScheduledFuture<?> _healTask;

  public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
  {
    super(objectId, template);
    init(owner);
  }

  private void init(L2PcInstance owner) {
    getKnownList();
    getStat();
    getStatus();

    _showSumAnim = true;
    _owner = owner;
    _ai = new L2SummonAI(new AIAccessor());
    setXYZInvisible(owner.getX() + 50, owner.getY() + 100, owner.getZ() + 100);
    if (Config.HEALING_SUMMONS.containsKey(Integer.valueOf(getNpcId()))) {
      _healData = ((Config.EventReward)Config.HEALING_SUMMONS.get(Integer.valueOf(getNpcId())));
      startHealTask();
    }
  }

  public final SummonKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof SummonKnownList))) {
      setKnownList(new SummonKnownList(this));
    }
    return (SummonKnownList)super.getKnownList();
  }

  public SummonStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof SummonStat))) {
      setStat(new SummonStat(this));
    }
    return (SummonStat)super.getStat();
  }

  public SummonStatus getStatus()
  {
    if ((super.getStatus() == null) || (!(super.getStatus() instanceof SummonStatus))) {
      setStatus(new SummonStatus(this));
    }
    return (SummonStatus)super.getStatus();
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null) {
      _ai = new L2SummonAI(new AIAccessor());
    }

    return _ai;
  }

  public L2NpcTemplate getTemplate()
  {
    return (L2NpcTemplate)super.getTemplate();
  }

  public abstract int getSummonType();

  public void updateAbnormalEffect()
  {
    FastList players = getKnownList().getListKnownPlayers();
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      pc.sendPacket(new NpcInfo(this, pc, 1));
    }
    players.clear();
    players = null;
    pc = null;
  }

  public boolean isMountable()
  {
    return false;
  }

  public void onAction(L2PcInstance player)
  {
    if (getNpcId() == Config.SOB_NPC) {
      player.sendActionFailed();
      return;
    }

    if ((player == _owner) && (player.getTarget() == this)) {
      player.sendPacket(new PetStatusShow(this));
      player.sendActionFailed();
    } else if (player.getTarget() != this) {
      player.setTarget(this);
      player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

      StatusUpdate su = new StatusUpdate(getObjectId());
      su.addAttribute(9, (int)getCurrentHp());
      su.addAttribute(10, getMaxHp());

      player.sendPacket(su);
    } else if (player.getTarget() == this) {
      if ((isAutoAttackable(player)) || (player.isOlympiadStart())) {
        if (canSeeTarget(player)) {
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
          player.onActionRequest();
        }
      }
      else {
        player.sendActionFailed();
        if (canSeeTarget(player))
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
      }
    }
  }

  public long getExpForThisLevel()
  {
    if (getLevel() >= Experience.LEVEL.length) {
      return 0L;
    }
    return Experience.LEVEL[getLevel()];
  }

  public long getExpForNextLevel() {
    if (getLevel() >= Experience.LEVEL.length - 1) {
      return 0L;
    }
    return Experience.LEVEL[(getLevel() + 1)];
  }

  public final int getKarma()
  {
    return _karma;
  }

  public void setKarma(int karma) {
    _karma = karma;
  }

  public final L2PcInstance getOwner()
  {
    return _owner;
  }

  public final int getNpcId()
  {
    return getTemplate().npcId;
  }

  public void setPvpFlag(byte pvpFlag) {
    _pvpFlag = pvpFlag;
  }

  public byte getPvpFlag()
  {
    return _pvpFlag;
  }

  public void setPkKills(int pkKills) {
    _pkKills = pkKills;
  }

  public final int getPkKills() {
    return _pkKills;
  }

  public final int getMaxLoad() {
    return _maxLoad;
  }

  public final int getSoulShotsPerHit() {
    return _soulShotsPerHit;
  }

  public final int getSpiritShotsPerHit() {
    return _spiritShotsPerHit;
  }

  public void setMaxLoad(int maxLoad) {
    _maxLoad = maxLoad;
  }

  public void setChargedSoulShot(int shotType)
  {
    _chargedSoulShot = shotType;
  }

  public void setChargedSpiritShot(int shotType)
  {
    _chargedSpiritShot = shotType;
  }

  public void followOwner() {
    setFollowStatus(true);
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }
    DecayTaskManager.getInstance().addDecayTask(this);
    return true;
  }

  public boolean doDie(L2Character killer, boolean decayed) {
    if (!super.doDie(killer)) {
      return false;
    }
    if (!decayed) {
      DecayTaskManager.getInstance().addDecayTask(this);
    }
    return true;
  }

  public void stopDecay() {
    DecayTaskManager.getInstance().cancelDecayTask(this);
  }

  public void onDecay()
  {
    deleteMe(_owner);
  }

  public void updateAndBroadcastStatus(int val)
  {
    if (getNpcId() != Config.SOB_NPC) {
      getOwner().sendPacket(new PetInfo(this, val));
      getOwner().sendPacket(new PetStatusUpdate(this));
    }

    if (isVisible()) {
      broadcastNpcInfo(val);
    }

    updateEffectIcons(true);
  }

  public void broadcastNpcInfo(int val) {
    FastList players = getKnownList().getListKnownPlayers();
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if ((pc == null) || 
        (pc == getOwner())) {
        continue;
      }
      pc.sendPacket(new NpcInfo(this, pc, val));
    }
    players.clear();
    players = null;
    pc = null;
  }

  public void broadcastStatusUpdate()
  {
    super.broadcastStatusUpdate();

    if ((getOwner() != null) && (isVisible()))
      getOwner().sendPacket(new PetStatusUpdate(this));
  }

  public void updateEffectIcons(boolean partyOnly)
  {
    PartySpelled ps = new PartySpelled(this);

    L2Effect[] effects = getAllEffects();
    if ((effects != null) && (effects.length > 0)) {
      for (L2Effect effect : effects) {
        if (effect == null)
        {
          continue;
        }
        if (effect.getInUse()) {
          effect.addPartySpelledIcon(ps);
        }
      }
    }

    getOwner().sendPacket(ps);
  }

  public void deleteMe(L2PcInstance owner) {
    getAI().stopFollow();
    owner.sendPacket(new PetDelete(getObjectId(), 2));

    giveAllToOwner();
    decayMe();
    stopHealTask();
    getKnownList().removeAllKnownObjects();
    owner.setPet(null);
  }

  public synchronized void unSummon(L2PcInstance owner) {
    if ((isVisible()) && (!isDead())) {
      getAI().stopFollow();
      owner.sendPacket(new PetDelete(getObjectId(), 2));
      if (getWorldRegion() != null) {
        getWorldRegion().removeFromZones(this);
      }
      store();

      giveAllToOwner();
      decayMe();
      stopHealTask();
      getKnownList().removeAllKnownObjects();
      owner.setPet(null);
      setTarget(null);
    }
  }

  public int getAttackRange() {
    return _attackRange;
  }

  public void setAttackRange(int range) {
    if (range < 36) {
      range = 36;
    }
    _attackRange = range;
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

  public boolean isAutoAttackable(L2Character attacker)
  {
    return _owner.isAutoAttackable(attacker);
  }

  public int getChargedSoulShot()
  {
    return _chargedSoulShot;
  }

  public int getChargedSpiritShot()
  {
    return _chargedSpiritShot;
  }

  public int getControlItemId() {
    return 0;
  }

  public L2Weapon getActiveWeapon() {
    return null;
  }

  public PetInventory getInventory() {
    return null;
  }

  protected void doPickupItem(L2Object object)
  {
  }

  public void giveAllToOwner()
  {
  }

  public void store()
  {
  }

  public L2ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public L2Weapon getActiveWeaponItem()
  {
    return null;
  }

  public L2ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public L2Weapon getSecondaryWeaponItem()
  {
    return null;
  }

  public L2Party getParty()
  {
    if (_owner == null) {
      return null;
    }

    return _owner.getParty();
  }

  public boolean isInParty()
  {
    if (_owner == null) {
      return false;
    }

    return _owner.getParty() != null;
  }

  public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
  {
    if ((skill == null) || (isDead())) {
      return;
    }

    if (skill.isPassive()) {
      return;
    }

    if (isCastingNow()) {
      return;
    }

    L2Object target = null;

    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[skill.getTargetType().ordinal()])
    {
    case 1:
      target = getOwner();
      break;
    case 2:
    case 3:
    case 4:
      target = this;
      break;
    default:
      target = skill.getFirstOfTargetList(this);
    }

    if (target == null) {
      if (getOwner() == null) {
        return;
      }
      target = getOwner().getTarget();
      if (target == null) {
        getOwner().sendPacket(Static.TARGET_CANT_FOUND);
        return;
      }

    }

    if ((isSkillDisabled(skill.getId())) && (getOwner() != null)) {
      getOwner().sendPacket(SystemMessage.id(SystemMessageId.SKILL_NOT_AVAILABLE).addString(skill.getName()));
      return;
    }

    if ((isAllSkillsDisabled()) && (getOwner() != null)) {
      return;
    }

    if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
    {
      if (getOwner() != null) {
        getOwner().sendPacket(Static.NOT_ENOUGH_MP);
      }
      return;
    }

    if (getCurrentHp() <= skill.getHpConsume())
    {
      if (getOwner() != null) {
        getOwner().sendPacket(Static.NOT_ENOUGH_HP);
      }
      return;
    }

    if (skill.isOffensive()) {
      if (getOwner() != null) {
        if (PeaceZone.getInstance().inPeace(getOwner(), target))
        {
          getOwner().sendActionFailed();
          return;
        }

        if ((getOwner().isInOlympiadMode()) && (!getOwner().isOlympiadCompStart())) {
          getOwner().sendActionFailed();
          return;
        }

      }

      if ((!target.isAutoAttackable(this)) && (!forceUse) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_CLAN) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_ALLY) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_PARTY) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF))
      {
        return;
      }

    }

    getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
  }

  public void setIsImobilised(boolean value)
  {
    super.setIsImobilised(value);

    if (value) {
      _previousFollowStatus = getFollowStatus();

      if (_previousFollowStatus)
        setFollowStatus(false);
    }
    else
    {
      setFollowStatus(_previousFollowStatus);
    }
  }

  public void setOwner(L2PcInstance newOwner) {
    _owner = newOwner;
  }

  public boolean isShowSummonAnimation()
  {
    return _showSumAnim;
  }

  public void setShowSummonAnimation(boolean showSummonAnimation)
  {
    _showSumAnim = showSummonAnimation;
  }

  public int getWeapon() {
    return 0;
  }

  public int getArmor() {
    return 0;
  }

  public void doCast(L2Skill skill)
  {
    int petLevel = getLevel();
    int skillLevel = petLevel / 10;
    if (petLevel >= 70) {
      skillLevel += (petLevel - 65) / 10;
    }

    if (skillLevel < 1) {
      skillLevel = 1;
    }

    L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);

    if (skillToCast != null)
      super.doCast(skillToCast);
    else
      super.doCast(skill);
  }

  public int getPetSpeed()
  {
    return getTemplate().baseRunSpd;
  }

  public void broadcastPetInfo()
  {
    updateEffectIcons();
  }

  public void broadcastUserInfo()
  {
    broadcastPetInfo();
  }

  public boolean isEnemyForMob(L2Attackable mob)
  {
    if (_owner.isGM()) {
      return false;
    }

    return (_owner.isEnemyForMob(mob)) || (mob.isAggressive());
  }

  public boolean isInsidePvpZone()
  {
    if (ZoneManager.getInstance().inPvpZone(this)) {
      return true;
    }

    return super.isInsidePvpZone();
  }

  public boolean replaceFirstBuff()
  {
    return getBuffCount() >= Config.BUFFS_PET_MAX_AMOUNT;
  }

  public void rechargeAutoSoulShot(boolean a, boolean b, boolean c)
  {
    if (_owner == null) {
      return;
    }

    _owner.rechargeAutoSoulShot(a, b, c);
  }

  public boolean isL2Summon()
  {
    return true;
  }

  public double calcAtkAccuracy(double value)
  {
    value += (getLevel() < 60 ? 4.0D : 5.0D);
    return value;
  }

  public double calcAtkCritical(double value, double dex)
  {
    return 40.0D;
  }

  public double calcMAtkCritical(double value, double wit)
  {
    return 8.0D;
  }

  public L2PcInstance getPlayer()
  {
    return _owner;
  }

  public L2Summon getL2Summon()
  {
    return this;
  }

  public void startHealTask()
  {
    if (_healTask != null) {
      return;
    }
    _healTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new HealTask(), Config.HEALSUM_DELAY, Config.HEALSUM_DELAY);
  }

  public void stopHealTask() {
    if (_healTask == null) {
      return;
    }
    _healTask.cancel(false);
    _healTask = null;
  }

  class HealTask
    implements Runnable
  {
    HealTask()
    {
    }

    public void run()
    {
      if ((_owner == null) || (_owner.isDead())) {
        stopHealTask();
        return;
      }

      _owner.setCurrentHpMp(_owner.getCurrentHp() + _healData.id, _owner.getCurrentMp() + _healData.count);
      _owner.setCurrentCp(_owner.getCurrentCp() + _healData.chance);
      if (Config.HEALSUM_ANIM > 0) {
        broadcastPacket(new MagicSkillUser(L2Summon.this, _owner, Config.HEALSUM_ANIM, 1, 1000, 0));
      }

      _owner.sendMessage("\u0421\u0430\u043C\u043C\u043E\u043D \u0432\u043E\u0441\u0442\u0430\u043D\u0430\u0432\u043B\u0438\u0432\u0430\u0435\u0442 \u0432\u0430\u043C \u0425\u041F/\u041C\u041F/\u0426\u041F.");
    }
  }

  public class AIAccessor extends L2Character.AIAccessor
  {
    protected AIAccessor()
    {
      super();
    }

    public L2Summon getSummon() {
      return L2Summon.this;
    }

    public boolean isAutoFollow() {
      return getFollowStatus();
    }

    public void doPickupItem(L2Object object) {
      L2Summon.this.doPickupItem(object);
    }
  }
}