package net.sf.l2j.gameserver.model;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.SummonKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.model.actor.status.SummonStatus;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetDelete;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusShow;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

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

  public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
  {
    super(objectId, template);
    getKnownList();
    getStat();
    getStatus();

    _showSummonAnimation = true;
    _owner = owner;
    _ai = new L2SummonAI(new AIAccessor());

    getPosition().setXYZInvisible(owner.getX() + 50, owner.getY() + 100, owner.getZ() + 100);
  }

  public final SummonKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof SummonKnownList)))
      setKnownList(new SummonKnownList(this));
    return (SummonKnownList)super.getKnownList();
  }

  public SummonStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof SummonStat)))
      setStat(new SummonStat(this));
    return (SummonStat)super.getStat();
  }

  public SummonStatus getStatus()
  {
    if ((super.getStatus() == null) || (!(super.getStatus() instanceof SummonStatus)))
      setStatus(new SummonStatus(this));
    return (SummonStatus)super.getStatus();
  }

  public L2CharacterAI getAI()
  {
    if (_ai == null)
    {
      synchronized (this)
      {
        if (_ai == null) {
          _ai = new L2SummonAI(new AIAccessor());
        }
      }
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
    for (L2PcInstance player : getKnownList().getKnownPlayers().values())
      player.sendPacket(new NpcInfo(this, player));
  }

  public boolean isMountable()
  {
    return false;
  }

  public void onAction(L2PcInstance player)
  {
    if ((player == _owner) && (player.getTarget() == this))
    {
      player.sendPacket(new PetStatusShow(this));
      player.sendPacket(new ActionFailed());
    }
    else if (player.getTarget() != this)
    {
      if (Config.DEBUG) _log.fine("new target selected:" + getObjectId());
      player.setTarget(this);
      MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
      player.sendPacket(my);

      StatusUpdate su = new StatusUpdate(getObjectId());
      su.addAttribute(9, (int)getCurrentHp());
      su.addAttribute(10, getMaxHp());
      player.sendPacket(su);
    }
    else if (player.getTarget() == this)
    {
      if (isAutoAttackable(player))
      {
        if (Config.GEODATA)
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
      else
      {
        player.sendPacket(new ActionFailed());
        if (Config.GEODATA)
        {
          if (GeoData.getInstance().canSeeTarget(player, this))
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
        }
        else
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
      }
    }
  }

  public long getExpForThisLevel()
  {
    if (getLevel() >= Experience.LEVEL.length)
    {
      return 0L;
    }
    return Experience.LEVEL[getLevel()];
  }

  public long getExpForNextLevel()
  {
    if (getLevel() >= Experience.LEVEL.length - 1)
    {
      return 0L;
    }
    return Experience.LEVEL[(getLevel() + 1)];
  }

  public final int getKarma()
  {
    return _karma;
  }

  public void setKarma(int karma)
  {
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

  public void setPvpFlag(byte pvpFlag)
  {
    _pvpFlag = pvpFlag;
  }

  public byte getPvpFlag()
  {
    return getOwner().getPvpFlag();
  }

  public void setPkKills(int pkKills)
  {
    _pkKills = pkKills;
  }

  public final int getPkKills()
  {
    return _pkKills;
  }

  public final int getMaxLoad()
  {
    return _maxLoad;
  }

  public final int getSoulShotsPerHit()
  {
    return _soulShotsPerHit;
  }

  public final int getSpiritShotsPerHit()
  {
    return _spiritShotsPerHit;
  }

  public void setMaxLoad(int maxLoad)
  {
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

  public void followOwner()
  {
    setFollowStatus(true);
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer))
      return false;
    DecayTaskManager.getInstance().addDecayTask(this);
    return true;
  }

  public boolean doDie(L2Character killer, boolean decayed)
  {
    if (!super.doDie(killer))
      return false;
    if (!decayed)
    {
      DecayTaskManager.getInstance().addDecayTask(this);
    }
    return true;
  }

  public void stopDecay()
  {
    DecayTaskManager.getInstance().cancelDecayTask(this);
  }

  public void onDecay()
  {
    deleteMe(_owner);
  }

  public void broadcastStatusUpdate()
  {
    super.broadcastStatusUpdate();

    if ((getOwner() != null) && (isVisible()))
      getOwner().sendPacket(new PetStatusUpdate(this));
  }

  public void deleteMe(L2PcInstance owner)
  {
    getAI().stopFollow();
    owner.sendPacket(new PetDelete(getObjectId(), 2));

    giveAllToOwner();
    decayMe();
    getKnownList().removeAllKnownObjects();
    owner.setPet(null);
  }

  public synchronized void unSummon(L2PcInstance owner)
  {
    if ((isVisible()) && (!isDead()))
    {
      getAI().stopFollow();
      owner.sendPacket(new PetDelete(getObjectId(), 2));
      if (getWorldRegion() != null) getWorldRegion().removeFromZones(this);
      store();
      giveAllToOwner();
      decayMe();
      getKnownList().removeAllKnownObjects();
      owner.setPet(null);
      setTarget(null);
    }
  }

  public int getAttackRange()
  {
    return _attackRange;
  }

  public void setAttackRange(int range)
  {
    if (range < 36)
      range = 36;
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

  public int getControlItemId()
  {
    return 0;
  }

  public L2Weapon getActiveWeapon()
  {
    return null;
  }

  public PetInventory getInventory()
  {
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

    if (skill.isPassive())
    {
      return;
    }

    if (isCastingNow())
    {
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

    if (target == null)
    {
      if (getOwner() != null)
        getOwner().sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
      return;
    }

    if (target.getObjectId() == getOwner().getObjectId())
    {
      return;
    }

    if ((isSkillDisabled(skill.getId())) && (getOwner() != null) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE);
      sm.addString(skill.getName());
      getOwner().sendPacket(sm);
      return;
    }

    if ((isAllSkillsDisabled()) && (getOwner() != null) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
    {
      return;
    }

    if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
    {
      if (getOwner() != null)
        getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
      return;
    }

    if (getCurrentHp() <= skill.getHpConsume())
    {
      if (getOwner() != null)
        getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_HP));
      return;
    }

    if (skill.isOffensive())
    {
      if ((isInsidePeaceZone(this, target)) && (getOwner() != null) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
      {
        if ((!isInFunEvent()) || (!target.isInFunEvent()))
        {
          sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
          return;
        }
      }

      if ((getOwner() != null) && (getOwner().isInOlympiadMode()) && (!getOwner().isOlympiadStart()))
      {
        sendPacket(new ActionFailed());
        return;
      }
      if (target.getObjectId() == getOwner().getObjectId())
      {
        sendPacket(new ActionFailed());
        return;
      }

      if ((target instanceof L2DoorInstance))
      {
        if (!((L2DoorInstance)target).isAttackable(getOwner()))
          return;
      }
      else
      {
        if ((!target.isAttackable()) && (getOwner() != null) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
        {
          return;
        }

        if ((!target.isAutoAttackable(this)) && (!forceUse) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_CLAN) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_ALLY) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_PARTY) && (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF))
        {
          return;
        }
      }

    }

    getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
  }

  public void setIsImobilised(boolean value)
  {
    super.setIsImobilised(value);

    if (value)
    {
      _previousFollowStatus = getFollowStatus();

      if (_previousFollowStatus) {
        setFollowStatus(false);
      }
    }
    else
    {
      setFollowStatus(_previousFollowStatus);
    }
  }

  public void setOwner(L2PcInstance newOwner)
  {
    _owner = newOwner;
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

  public class AIAccessor extends L2Character.AIAccessor
  {
    protected AIAccessor()
    {
      super(); } 
    public L2Summon getSummon() { return L2Summon.this; } 
    public boolean isAutoFollow() {
      return getFollowStatus();
    }
    public void doPickupItem(L2Object object) {
      L2Summon.this.doPickupItem(object);
    }
  }
}