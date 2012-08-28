package l2m.gameserver.model;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.util.Rnd;
import l2p.commons.util.concurrent.atomic.AtomicState;
import l2m.gameserver.Config;
import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.geodata.GeoEngine;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.entity.events.impl.DuelEvent;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.instances.StaticObjectInstance;
import l2m.gameserver.model.items.Inventory;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.Revive;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.EffectType;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.CharTemplate;
import l2m.gameserver.templates.item.EtcItemTemplate;
import l2m.gameserver.templates.item.WeaponTemplate;
import l2m.gameserver.templates.item.WeaponTemplate.WeaponType;

public abstract class Playable extends Creature
{
  public static final long serialVersionUID = 1L;
  private AtomicState _isSilentMoving = new AtomicState();
  private boolean _isPendingRevive;
  protected final ReadWriteLock questLock = new ReentrantReadWriteLock();
  protected final Lock questRead = questLock.readLock();
  protected final Lock questWrite = questLock.writeLock();
  private long _nonAggroTime;

  public Playable(int objectId, CharTemplate template)
  {
    super(objectId, template);
  }

  public HardReference<? extends Playable> getRef()
  {
    return super.getRef();
  }

  public abstract Inventory getInventory();

  public abstract long getWearedMask();

  public boolean checkPvP(Creature target, Skill skill)
  {
    Player player = getPlayer();

    if ((isDead()) || (target == null) || (player == null) || (target == this) || (target == player) || (target == player.getPet()) || (player.getKarma() > 0)) {
      return false;
    }
    if (skill != null)
    {
      if (skill.altUse())
        return false;
      if (skill.getTargetType() == Skill.SkillTargetType.TARGET_FEEDABLE_BEAST)
        return false;
      if (skill.getTargetType() == Skill.SkillTargetType.TARGET_UNLOCKABLE)
        return false;
      if (skill.getTargetType() == Skill.SkillTargetType.TARGET_CHEST) {
        return false;
      }
    }

    DuelEvent duelEvent = (DuelEvent)getEvent(DuelEvent.class);
    if ((duelEvent != null) && (duelEvent == target.getEvent(DuelEvent.class))) {
      return false;
    }
    if ((isInZonePeace()) && (target.isInZonePeace()))
      return false;
    if ((isInZoneBattle()) && (target.isInZoneBattle()))
      return false;
    if ((isInZone(Zone.ZoneType.SIEGE)) && (target.isInZone(Zone.ZoneType.SIEGE))) {
      return false;
    }
    if ((skill == null) || (skill.isOffensive()))
    {
      if (target.getKarma() > 0)
        return false;
      if (target.isPlayable())
        return true;
    }
    else if ((target.getPvpFlag() > 0) || (target.getKarma() > 0) || (target.isMonster())) {
      return true;
    }
    return false;
  }

  public boolean checkTarget(Creature target)
  {
    Player player = getPlayer();
    if (player == null) {
      return false;
    }
    if ((target == null) || (target.isDead()))
    {
      player.sendPacket(Msg.INVALID_TARGET);
      return false;
    }

    if (!isInRange(target, 2000L))
    {
      player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
      return false;
    }

    if ((target.isDoor()) && (!target.isAttackable(this)))
    {
      player.sendPacket(Msg.INVALID_TARGET);
      return false;
    }

    if (target.paralizeOnAttack(this))
    {
      if (Config.PARALIZE_ON_RAID_DIFF)
        paralizeMe(target);
      return false;
    }

    if ((target.isInvisible()) || (getReflection() != target.getReflection()) || (!GeoEngine.canSeeTarget(this, target, false)))
    {
      player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
      return false;
    }

    if (player.isInZone(Zone.ZoneType.epic) != target.isInZone(Zone.ZoneType.epic))
    {
      player.sendPacket(Msg.INVALID_TARGET);
      return false;
    }

    if (target.isPlayable())
    {
      if (isInZoneBattle() != target.isInZoneBattle())
      {
        player.sendPacket(Msg.INVALID_TARGET);
        return false;
      }

      if ((isInZonePeace()) || (target.isInZonePeace()))
      {
        player.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
        return false;
      }
      if ((player.isInOlympiadMode()) && (!player.isOlympiadCompStart())) {
        return false;
      }
    }
    return true;
  }

  public void doAttack(Creature target)
  {
    Player player = getPlayer();
    if (player == null) {
      return;
    }
    if ((isAMuted()) || (isAttackingNow()))
    {
      player.sendActionFailed();
      return;
    }

    if (player.isInObserverMode())
    {
      player.sendMessage(new CustomMessage("l2p.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack", player, new Object[0]));
      return;
    }

    if (!checkTarget(target))
    {
      getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
      player.sendActionFailed();
      return;
    }

    DuelEvent duelEvent = (DuelEvent)getEvent(DuelEvent.class);
    if ((duelEvent != null) && (target.getEvent(DuelEvent.class) != duelEvent)) {
      duelEvent.abortDuel(getPlayer());
    }
    WeaponTemplate weaponItem = getActiveWeaponItem();

    if ((weaponItem != null) && ((weaponItem.getItemType() == WeaponTemplate.WeaponType.BOW) || (weaponItem.getItemType() == WeaponTemplate.WeaponType.CROSSBOW)))
    {
      double bowMpConsume = weaponItem.getMpConsume();
      if (bowMpConsume > 0.0D)
      {
        double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0.0D, target, null);
        if ((chance > 0.0D) && (Rnd.chance(chance))) {
          bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);
        }
        if (_currentMp < bowMpConsume)
        {
          getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
          player.sendPacket(Msg.NOT_ENOUGH_MP);
          player.sendActionFailed();
          return;
        }

        reduceCurrentMp(bowMpConsume, null);
      }

      if (!player.checkAndEquipArrows())
      {
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        player.sendPacket(player.getActiveWeaponInstance().getItemType() == WeaponTemplate.WeaponType.BOW ? Msg.YOU_HAVE_RUN_OUT_OF_ARROWS : Msg.NOT_ENOUGH_BOLTS);
        player.sendActionFailed();
        return;
      }
    }

    super.doAttack(target);
  }

  public void doCast(Skill skill, Creature target, boolean forceUse)
  {
    if (skill == null) {
      return;
    }

    DuelEvent duelEvent = (DuelEvent)getEvent(DuelEvent.class);
    if ((duelEvent != null) && (target.getEvent(DuelEvent.class) != duelEvent)) {
      duelEvent.abortDuel(getPlayer());
    }

    if ((skill.isAoE()) && (isInPeaceZone()))
    {
      getPlayer().sendPacket(Msg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
      return;
    }

    if ((skill.getSkillType() == Skill.SkillType.DEBUFF) && (target.isNpc()) && (target.isInvul()) && (!target.isMonster()))
    {
      getPlayer().sendPacket(Msg.INVALID_TARGET);
      return;
    }

    super.doCast(skill, target, forceUse);
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

    if ((attacker != this) && (attacker.isPlayable()))
    {
      Player player = getPlayer();
      Player pcAttacker = attacker.getPlayer();
      if ((pcAttacker != player) && 
        (player.isInOlympiadMode()) && (!player.isOlympiadCompStart()))
      {
        if (sendMessage)
          pcAttacker.sendPacket(Msg.INVALID_TARGET);
        return;
      }

      if (isInZoneBattle() != attacker.isInZoneBattle())
      {
        if (sendMessage)
          attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
        return;
      }

      DuelEvent duelEvent = (DuelEvent)getEvent(DuelEvent.class);
      if ((duelEvent != null) && (attacker.getEvent(DuelEvent.class) != duelEvent)) {
        duelEvent.abortDuel(player);
      }
    }
    super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
  }

  public int getPAtkSpd()
  {
    return Math.max((int)calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _template.basePAtkSpd, null, null), null, null), 1);
  }

  public int getPAtk(Creature target)
  {
    double init = getActiveWeaponInstance() == null ? _template.basePAtk : 0.0D;
    return (int)calcStat(Stats.POWER_ATTACK, init, target, null);
  }

  public int getMAtk(Creature target, Skill skill)
  {
    if ((skill != null) && (skill.getMatak() > 0))
      return skill.getMatak();
    double init = getActiveWeaponInstance() == null ? _template.baseMAtk : 0.0D;
    return (int)calcStat(Stats.MAGIC_ATTACK, init, target, skill);
  }

  public boolean isAttackable(Creature attacker)
  {
    return isCtrlAttackable(attacker, true, false);
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return isCtrlAttackable(attacker, false, false);
  }

  public boolean isCtrlAttackable(Creature attacker, boolean force, boolean witchCtrl)
  {
    Player player = getPlayer();
    if ((attacker == null) || (player == null) || (attacker == this) || ((attacker == player) && (!force)) || (isAlikeDead()) || (attacker.isAlikeDead())) {
      return false;
    }
    if ((isInvisible()) || (getReflection() != attacker.getReflection())) {
      return false;
    }
    if (isInBoat()) {
      return false;
    }
    for (GlobalEvent e : getEvents()) {
      if (e.checkForAttack(this, attacker, null, force) != null)
        return false;
    }
    for (GlobalEvent e : player.getEvents()) {
      if (e.canAttack(this, attacker, null, force))
        return true;
    }
    Player pcAttacker = attacker.getPlayer();

    if ((pcAttacker != null) && (pcAttacker != player))
    {
      if (pcAttacker.isInBoat()) {
        return false;
      }
      if ((pcAttacker.getBlockCheckerArena() > -1) || (player.getBlockCheckerArena() > -1)) {
        return false;
      }

      if (((pcAttacker.isCursedWeaponEquipped()) && (player.getLevel() < 21)) || ((player.isCursedWeaponEquipped()) && (pcAttacker.getLevel() < 21))) {
        return false;
      }
      if (player.isInZone(Zone.ZoneType.epic) != pcAttacker.isInZone(Zone.ZoneType.epic)) {
        return false;
      }
      if (((player.isInOlympiadMode()) || (pcAttacker.isInOlympiadMode())) && (player.getOlympiadGame() != pcAttacker.getOlympiadGame()))
        return false;
      if ((player.isInOlympiadMode()) && (!player.isOlympiadCompStart()))
        return false;
      if ((player.isInOlympiadMode()) && (player.isOlympiadCompStart()) && (player.getOlympiadSide() == pcAttacker.getOlympiadSide()) && (!force)) {
        return false;
      }

      if (isInZonePeace())
        return false;
      if (isInZoneBattle())
        return true;
      if ((!force) && (player.getParty() != null) && (player.getParty() == pcAttacker.getParty()))
        return false;
      if ((!force) && (player.getClan() != null) && (player.getClan() == pcAttacker.getClan())) {
        return false;
      }
      if (isInZone(Zone.ZoneType.SIEGE)) {
        return true;
      }
      if (pcAttacker.atMutualWarWith(player))
        return true;
      if ((player.getKarma() > 0) || (player.getPvpFlag() != 0))
        return true;
      if ((witchCtrl) && (player.getPvpFlag() > 0)) {
        return true;
      }
      return force;
    }

    return true;
  }

  public int getKarma()
  {
    Player player = getPlayer();
    return player == null ? 0 : player.getKarma();
  }

  public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills)
  {
    Player player = getPlayer();
    if (player == null) {
      return;
    }
    if ((useActionSkills) && (!skill.altUse()) && (!skill.getSkillType().equals(Skill.SkillType.BEAST_FEED))) {
      for (Creature target : targets)
      {
        int aggro;
        if (target.isNpc())
        {
          if (skill.isOffensive())
          {
            if (target.paralizeOnAttack(player))
            {
              if (Config.PARALIZE_ON_RAID_DIFF)
                paralizeMe(target);
              return;
            }
            if (!skill.isAI())
            {
              int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
              target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, Integer.valueOf(damage));
            }
          }
          target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
        }
        else if ((target.isPlayable()) && (target != getPet()) && (((!isSummon()) && (!isPet())) || (target != player)))
        {
          aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int)skill.getPower());

          List npcs = World.getAroundNpc(target);
          for (NpcInstance npc : npcs)
          {
            if ((npc.isDead()) || (!npc.isInRangeZ(this, 2000L))) {
              continue;
            }
            npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);

            AggroList.AggroInfo ai = npc.getAggroList().get(target);

            if (ai == null) {
              continue;
            }
            if ((!skill.isHandler()) && (npc.paralizeOnAttack(player)))
            {
              if (Config.PARALIZE_ON_RAID_DIFF)
                paralizeMe(npc);
              return;
            }

            if (ai.hate < 100) {
              continue;
            }
            if (GeoEngine.canSeeTarget(npc, target, false)) {
              npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, Integer.valueOf(ai.damage == 0 ? aggro / 2 : aggro));
            }
          }
        }

        if (checkPvP(target, skill))
          startPvPFlag(target);
      }
    }
    super.callSkill(skill, targets, useActionSkills);
  }

  public void broadcastPickUpMsg(ItemInstance item)
  {
    Player player = getPlayer();

    if ((item == null) || (player == null) || (player.isInvisible())) {
      return;
    }
    if ((item.isEquipable()) && (!(item.getTemplate() instanceof EtcItemTemplate)))
    {
      SystemMessage msg = null;
      String player_name = player.getName();
      if (item.getEnchantLevel() > 0)
      {
        int msg_id = isPlayer() ? 1534 : 1536;
        msg = new SystemMessage(msg_id).addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
      }
      else
      {
        int msg_id = isPlayer() ? 1533 : 1536;
        msg = new SystemMessage(msg_id).addString(player_name).addItemName(item.getItemId());
      }
      player.broadcastPacket(new L2GameServerPacket[] { msg });
    }
  }

  public void paralizeMe(Creature effector)
  {
    Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
    revengeSkill.getEffects(effector, this, false, false);
  }

  public final void setPendingRevive(boolean value)
  {
    _isPendingRevive = value;
  }

  public boolean isPendingRevive()
  {
    return _isPendingRevive;
  }

  public void doRevive()
  {
    if (!isTeleporting())
    {
      setPendingRevive(false);
      setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);

      if (isSalvation())
      {
        for (Effect e : getEffectList().getAllEffects())
          if (e.getEffectType() == EffectType.Salvation)
          {
            e.exit();
            break;
          }
        setCurrentHp(getMaxHp(), true);
        setCurrentMp(getMaxMp());
        setCurrentCp(getMaxCp());
      }
      else
      {
        if ((isPlayer()) && (Config.RESPAWN_RESTORE_CP >= 0.0D)) {
          setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
        }
        setCurrentHp(Math.max(1.0D, getMaxHp() * Config.RESPAWN_RESTORE_HP), true);

        if (Config.RESPAWN_RESTORE_MP >= 0.0D) {
          setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
        }
      }
      broadcastPacket(new L2GameServerPacket[] { new Revive(this) });
    }
    else {
      setPendingRevive(true);
    }
  }

  public abstract void doPickupItem(GameObject paramGameObject);

  public void sitDown(StaticObjectInstance throne)
  {
  }

  public void standUp() {
  }

  public long getNonAggroTime() {
    return _nonAggroTime;
  }

  public void setNonAggroTime(long time)
  {
    _nonAggroTime = time;
  }

  public boolean startSilentMoving()
  {
    return _isSilentMoving.getAndSet(true);
  }

  public boolean stopSilentMoving()
  {
    return _isSilentMoving.setAndGet(false);
  }

  public boolean isSilentMoving()
  {
    return _isSilentMoving.get();
  }

  public boolean isInCombatZone()
  {
    return isInZoneBattle();
  }

  public boolean isInPeaceZone()
  {
    return isInZonePeace();
  }

  public boolean isInZoneBattle()
  {
    return super.isInZoneBattle();
  }

  public boolean isOnSiegeField()
  {
    return isInZone(Zone.ZoneType.SIEGE);
  }

  public boolean isInSSQZone()
  {
    return isInZone(Zone.ZoneType.ssq_zone);
  }

  public boolean isInDangerArea()
  {
    return (isInZone(Zone.ZoneType.damage)) || (isInZone(Zone.ZoneType.swamp)) || (isInZone(Zone.ZoneType.poison)) || (isInZone(Zone.ZoneType.instant_skill));
  }

  public int getMaxLoad()
  {
    return 0;
  }

  public int getInventoryLimit()
  {
    return 0;
  }

  public boolean isPlayable()
  {
    return true;
  }
}