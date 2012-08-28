package net.sf.l2j.gameserver.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class L2AttackableAI extends L2CharacterAI
  implements Runnable
{
  private static final int RANDOM_WALK_RATE = 30;
  private static final int MAX_ATTACK_TIMEOUT = 300;
  private Future<?> _aiTask;
  private int _attackTimeout;
  private int _globalAggro;
  private boolean _thinking;
  private long _lastChk = 0L;

  public L2AttackableAI(L2Character.AIAccessor accessor)
  {
    super(accessor);

    _attackTimeout = 2147483647;
    _globalAggro = -10;
  }

  public void run()
  {
    onEvtThink();
  }

  private boolean autoAttackCondition(L2Character target)
  {
    if (target == null) {
      return false;
    }

    if (!_actor.isL2Attackable()) {
      return false;
    }

    L2Attackable me = (L2Attackable)_actor;

    if ((me.isAlikeDead()) || (target.isAlikeDead()) || (!me.isInsideRadius(target, me.getAggroRange(), false, false)) || (Math.abs(_actor.getZ() - target.getZ()) > 300))
    {
      return false;
    }

    return (target.isEnemyForMob(me)) && (me.canSeeTarget(target));
  }

  public void startAITask()
  {
    if (_aiTask == null)
      _aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 500L, 1000L);
  }

  public void stopAITask()
  {
    if (_aiTask != null) {
      _aiTask.cancel(false);
      _aiTask = null;
    }
  }

  protected void onEvtDead()
  {
    stopAITask();
    super.onEvtDead();
  }

  synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    if ((intention == CtrlIntention.AI_INTENTION_IDLE) || (intention == CtrlIntention.AI_INTENTION_ACTIVE))
    {
      if (!_actor.isAlikeDead()) {
        L2Attackable npc = (L2Attackable)_actor;

        if (npc.getKnownList().getKnownPlayers().size() > 0) {
          intention = CtrlIntention.AI_INTENTION_ACTIVE;
        }
      }

      if (intention == CtrlIntention.AI_INTENTION_IDLE)
      {
        super.changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

        if (_aiTask != null) {
          _aiTask.cancel(true);
          _aiTask = null;
        }

        _accessor.detachAI();

        return;
      }

    }

    super.changeIntention(intention, arg0, arg1);

    startAITask();
  }

  protected void onIntentionAttack(L2Character target)
  {
    _attackTimeout = (300 + GameTimeController.getGameTicks());

    super.onIntentionAttack(target);
  }

  private void thinkActive()
  {
    L2Attackable npc = (L2Attackable)_actor;

    if (_globalAggro != 0) {
      if (_globalAggro < 0)
        _globalAggro += 1;
      else {
        _globalAggro -= 1;
      }

    }

    if (_globalAggro >= 0)
    {
      for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(npc.getAggroRange())) {
        if ((target == null) || (
          (_actor.isL2FestivalMonster()) && (target.isPlayer()) && (target.isFestivalParticipant())))
        {
          continue;
        }

        if (autoAttackCondition(target))
        {
          int hating = npc.getHating(target);

          if (hating == 0)
            npc.addDamageHate(target, 0, 1);
        }
      }
      L2Character hated;
      L2Character hated;
      if (_actor.isConfused())
        hated = getAttackTarget();
      else {
        hated = npc.getMostHated();
      }

      if (hated != null)
      {
        int aggro = npc.getHating(hated);
        if (aggro + _globalAggro > 0)
        {
          if (!_actor.isRunning()) {
            _actor.setRunning();
          }

          setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
        }
        return;
      }

    }

    if (_actor.isL2Guard())
    {
      ((L2GuardInstance)_actor).returnHome();
      return;
    }

    if (_actor.isL2FestivalMonster()) {
      return;
    }

    if (!npc.canReturnToSpawnPoint()) {
      return;
    }

    if ((npc.getNpcId() != 29014) && (npc.getSpawn() != null)) {
      int x1 = npc.getSpawn().getLocx() + Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2) - Config.MAX_DRIFT_RANGE;
      int y1 = npc.getSpawn().getLocy() + Rnd.nextInt(Config.MAX_DRIFT_RANGE * 2) - Config.MAX_DRIFT_RANGE;

      moveTo(x1, y1, npc.getSpawn().getLocz());
    }

    if (((_actor instanceof L2MinionInstance)) && (((L2MinionInstance)_actor).getLeader() != null)) {
      int offset = 200;
      if (_actor.isRaid()) {
        offset = 500;
      }
      if (((L2MinionInstance)_actor).getLeader().isRunning())
        _actor.setRunning();
      else {
        _actor.setWalking();
      }

      if (_actor.getPlanDistanceSq(((L2MinionInstance)_actor).getLeader()) > offset * offset) {
        int x1 = ((L2MinionInstance)_actor).getLeader().getX() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
        int y1 = ((L2MinionInstance)_actor).getLeader().getY() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
        int z1 = ((L2MinionInstance)_actor).getLeader().getZ();

        moveTo(x1, y1, z1);
      }
    }
  }

  private void thinkAttack()
  {
    if (_attackTimeout < GameTimeController.getGameTicks())
    {
      if (_actor.isRunning())
      {
        _actor.setWalking();

        _attackTimeout = (300 + GameTimeController.getGameTicks());
      }
    }

    L2Character attackTarget = getAttackTarget();

    if ((attackTarget == null) || (attackTarget.isAlikeDead()) || (attackTarget.isTeleporting()) || (_attackTimeout < GameTimeController.getGameTicks()) || (attackTarget.getFirstEffect(4515) != null))
    {
      if (attackTarget != null) {
        L2Attackable npc = (L2Attackable)_actor;
        npc.stopHating(attackTarget);
      }

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

      _actor.setWalking();
    } else {
      if ((_actor.checkRange()) && (_lastChk < System.currentTimeMillis())) {
        _lastChk = (System.currentTimeMillis() + 3000L);
        L2Attackable npc = (L2Attackable)_actor;
        if (npc.getSpawn() != null) {
          int x1 = npc.getSpawn().getLocx();
          int y1 = npc.getSpawn().getLocy();
          int z1 = npc.getSpawn().getLocz();
          if ((Util.calculateDistance(attackTarget.getX(), attackTarget.getY(), attackTarget.getZ(), x1, y1, z1, true) > 2500.0D) || (Util.calculateDistance(x1, y1, z1, npc.getX(), npc.getY(), npc.getZ(), true) > 2500.0D))
          {
            if (attackTarget != null) {
              ((L2Attackable)_actor).stopHating(attackTarget);
            }

            setIntention(CtrlIntention.AI_INTENTION_IDLE);

            _actor.setWalking();
            _actor.teleToLocation(x1, y1, z1, false);
            return;
          }
        }
      }
      String faction_id;
      if (((L2NpcInstance)_actor).getFactionId() != null) {
        faction_id = ((L2NpcInstance)_actor).getFactionId();

        for (L2Object obj : _actor.getKnownList().getKnownObjects().values()) {
          if ((obj == null) || (!obj.isL2Npc()))
          {
            continue;
          }
          L2NpcInstance npc = (L2NpcInstance)obj;
          if (npc == null)
          {
            continue;
          }
          Integer fr = Integer.valueOf(npc.getFactionRange());
          if ((fr == null) || (fr.intValue() == 0) || (!faction_id.equals(npc.getFactionId())))
          {
            continue;
          }

          if ((_actor.isInsideRadius(npc, fr.intValue(), true, false)) && (Math.abs(attackTarget.getZ() - npc.getZ()) < 600) && (npc.getAI() != null) && (_actor.getAttackByList().contains(attackTarget)) && ((npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)))
          {
            if ((attackTarget.isPlayer()) && (attackTarget.isInParty()) && (attackTarget.getParty().isInDimensionalRift())) {
              byte riftType = attackTarget.getParty().getDimensionalRift().getType();
              byte riftRoom = attackTarget.getParty().getDimensionalRift().getCurrentRoom();

              if ((_actor.isL2RiftInvader()) && (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ())))
              {
                continue;
              }
            }
            if (_actor.canSeeTarget(npc)) {
              npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attackTarget, Integer.valueOf(1));
            }
          }
        }
      }

      if (_actor.isAttackingDisabled()) {
        return;
      }

      L2Skill[] skills = null;
      double dist2 = 0.0D;
      int range = 0;
      try
      {
        _actor.setTarget(attackTarget);
        skills = _actor.getAllSkills();
        dist2 = _actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY());
        range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius;
      }
      catch (NullPointerException e)
      {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        return;
      }

      L2Weapon weapon = _actor.getActiveWeaponItem();
      if ((weapon != null) && (weapon.getItemType() == L2WeaponType.BOW))
      {
        double distance2 = _actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY());
        if (distance2 <= 10000.0D) {
          int chance = 5;
          if (chance >= Rnd.get(100)) {
            int posX = _actor.getX();
            int posY = _actor.getY();
            int posZ = _actor.getZ();
            double distance = Math.sqrt(distance2);

            int signx = -1;
            int signy = -1;
            if (_actor.getX() > attackTarget.getX()) {
              signx = 1;
            }
            if (_actor.getY() > attackTarget.getY()) {
              signy = 1;
            }
            posX += Math.round((float)(signx * (range / 2 + Rnd.get(range)) - distance));
            posY += Math.round((float)(signy * (range / 2 + Rnd.get(range)) - distance));
            setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, _actor.calcHeading(posX, posY)));
            return;
          }
        }
      }
      L2Character hated;
      L2Character hated;
      if (_actor.isConfused())
        hated = attackTarget;
      else {
        hated = ((L2Attackable)_actor).getMostHated();
      }

      if (hated == null) {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        return;
      }

      if (hated != attackTarget) {
        setAttackTarget(hated);
      }

      dist2 = _actor.getPlanDistanceSq(hated.getX(), hated.getY());

      if (hated.isMoving()) {
        range += 50;
      }

      if (dist2 > range * range)
      {
        if ((!_actor.isMuted()) && (_actor.isL2Monster()) && (Rnd.nextInt(100) <= 5)) {
          for (L2Skill sk : skills) {
            if (sk.isPassive())
            {
              continue;
            }
            int castRange = sk.getCastRange();
            if (((sk.getSkillType() != L2Skill.SkillType.BUFF) && (sk.getSkillType() != L2Skill.SkillType.HEAL) && ((dist2 < castRange * castRange / 9.0D) || (dist2 > castRange * castRange) || (castRange <= 70))) || (_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)))
            {
              continue;
            }

            L2Object OldTarget = _actor.getTarget();
            if ((sk.getSkillType() == L2Skill.SkillType.BUFF) || (sk.getSkillType() == L2Skill.SkillType.HEAL)) {
              boolean useSkillSelf = true;
              if ((sk.getSkillType() == L2Skill.SkillType.HEAL) && (_actor.getCurrentHp() > (int)(_actor.getMaxHp() / 1.5D))) {
                useSkillSelf = false;
                break;
              }
              if (sk.getSkillType() == L2Skill.SkillType.BUFF) {
                L2Effect effect = _actor.getFirstEffect(sk.getId());
                if (effect != null) {
                  useSkillSelf = false;
                }
              }
              if (useSkillSelf) {
                _actor.setTarget(_actor);
              }
            }
            clientStopMoving(null);
            _accessor.doCast(sk);
            _actor.setTarget(OldTarget);
            return;
          }

        }

        if (hated.isMoving()) {
          range -= 100;
        }
        if (range < 5) {
          range = 5;
        }
        moveToPawn(attackTarget, range);
        return;
      }

      _attackTimeout = (300 + GameTimeController.getGameTicks());

      if (!_actor.isMuted()) {
        boolean useSkillSelf = true;
        for (L2Skill sk : skills) {
          if (sk.isPassive()) {
            continue;
          }
          if ((_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (_actor.isSkillDisabled(sk.getId())) || ((Rnd.nextInt(100) > 8) && ((!_actor.isL2Penalty()) || (Rnd.nextInt(100) > 20)))) {
            continue;
          }
          L2Object OldTarget = _actor.getTarget();
          if ((sk.getSkillType() == L2Skill.SkillType.BUFF) || (sk.getSkillType() == L2Skill.SkillType.HEAL)) {
            useSkillSelf = true;
            if ((sk.getSkillType() == L2Skill.SkillType.HEAL) && (_actor.getCurrentHp() > (int)(_actor.getMaxHp() / 1.5D))) {
              useSkillSelf = false;
              break;
            }
            if (sk.getSkillType() == L2Skill.SkillType.BUFF) {
              L2Effect effect = _actor.getFirstEffect(sk.getId());
              if (effect != null) {
                useSkillSelf = false;
              }
            }
            if (useSkillSelf) {
              _actor.setTarget(_actor);
            }
          }

          if ((!useSkillSelf) && (!_actor.canSeeTarget(OldTarget))) {
            return;
          }
          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(OldTarget);
          return;
        }

      }

      clientStopMoving(null);
      _accessor.doAttack(hated);
    }
  }

  protected void onEvtThink()
  {
    if ((_thinking) || (_actor.isAllSkillsDisabled())) {
      return;
    }

    _thinking = true;
    try
    {
      if (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
        thinkActive();
      else if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
        thinkAttack();
    }
    finally
    {
      _thinking = false;
    }
  }

  protected void onEvtAttacked(L2Character attacker)
  {
    _attackTimeout = (300 + GameTimeController.getGameTicks());

    if (_globalAggro < 0) {
      _globalAggro = 0;
    }

    ((L2Attackable)_actor).addDamageHate(attacker, 0, 1);

    if (!_actor.isRunning()) {
      _actor.setRunning();
    }

    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    else if (((L2Attackable)_actor).getMostHated() != getAttackTarget()) {
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    }

    super.onEvtAttacked(attacker);
  }

  protected void onEvtAggression(L2Character target, int aggro)
  {
    L2Attackable me = (L2Attackable)_actor;

    if (target != null)
    {
      me.addDamageHate(target, 0, aggro);

      if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
      {
        if (!_actor.isRunning()) {
          _actor.setRunning();
        }

        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
      }
    }
  }

  protected void onIntentionActive()
  {
    _attackTimeout = 2147483647;
    super.onIntentionActive();
  }

  public void setGlobalAggro(int value) {
    _globalAggro = value;
  }
}