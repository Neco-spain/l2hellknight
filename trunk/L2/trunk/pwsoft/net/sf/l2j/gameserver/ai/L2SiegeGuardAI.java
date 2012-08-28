package net.sf.l2j.gameserver.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.util.Rnd;

public class L2SiegeGuardAI extends L2CharacterAI
  implements Runnable
{
  private static final int MAX_ATTACK_TIMEOUT = 300;
  private Future<?> _aiTask;
  private int _attackTimeout;
  private int _globalAggro;
  private boolean _thinking;
  private int _attackRange;

  public L2SiegeGuardAI(L2Character.AIAccessor accessor)
  {
    super(accessor);

    _attackTimeout = 2147483647;
    _globalAggro = -10;

    _attackRange = ((L2Attackable)_actor).getPhysicalAttackRange();
  }

  public void run()
  {
    onEvtThink();
  }

  private boolean autoAttackCondition(L2Character target)
  {
    if ((target == null) || (target.isL2SiegeGuard()) || (target.isL2Folk()) || (target.isL2Door()) || (target.isAlikeDead()) || (target.isInvul()))
    {
      return false;
    }

    if (target.isL2Summon()) {
      L2PcInstance owner = target.getOwner();
      if (_actor.isInsideRadius(owner, 1000, true, false)) {
        target = owner;
      }

    }

    if (target.isPlayer())
    {
      if ((target.isSilentMoving()) && (!_actor.isInsideRadius(target, 250, false, false))) {
        return false;
      }
    }

    return (_actor.isAutoAttackable(target)) && (_actor.canSeeTarget(target));
  }

  void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    ((L2Attackable)_actor).setisReturningToSpawnPoint(false);

    if (intention == CtrlIntention.AI_INTENTION_IDLE)
    {
      if (!_actor.isAlikeDead()) {
        L2Attackable npc = (L2Attackable)_actor;

        if (npc.getKnownList().getKnownPlayers().size() > 0)
          intention = CtrlIntention.AI_INTENTION_ACTIVE;
        else {
          intention = CtrlIntention.AI_INTENTION_IDLE;
        }
      }

      if (intention == CtrlIntention.AI_INTENTION_IDLE)
      {
        super.changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

        Lock shed = new ReentrantLock();
        shed.lock();
        try
        {
          if (_aiTask != null) {
            _aiTask.cancel(true);
            _aiTask = null;
          }
        } finally {
          shed.unlock();
        }

        _accessor.detachAI();

        return;
      }

    }

    super.changeIntention(intention, arg0, arg1);

    Lock shed = new ReentrantLock();
    shed.lock();
    try
    {
      if (_aiTask == null)
        _aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000L, 1000L);
    }
    finally {
      shed.unlock();
    }
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

    if (_globalAggro >= 0) {
      for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(_attackRange)) {
        if (target == null) {
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

          setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
        }

        return;
      }

    }

    ((L2SiegeGuardInstance)_actor).returnHome();
  }

  private void attackPrepare()
  {
    L2Character target = getAttackTarget();
    if (target == null) {
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    L2Skill[] skills = null;
    double dist_2 = 0.0D;
    int range = 0;
    L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance)_actor;
    try {
      _actor.setTarget(target);
      skills = _actor.getAllSkills();
      dist_2 = _actor.getPlanDistanceSq(target.getX(), target.getY());
      range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + target.getTemplate().collisionRadius;
    } catch (NullPointerException e) {
      e.printStackTrace();

      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if ((target.isPlayer()) && (sGuard.getCastle().getSiege().checkIsDefender(target.getPlayer().getClan())))
    {
      sGuard.stopHating(target);
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if (!_actor.canSeeTarget(target))
    {
      sGuard.stopHating(target);
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if ((!_actor.isMuted()) && (dist_2 > (range + 20) * (range + 20)))
    {
      if ((!Config.ALT_GAME_MOB_ATTACK_AI) || ((_actor.isL2Monster()) && (Rnd.nextInt(100) <= 5))) {
        for (L2Skill sk : skills) {
          int castRange = sk.getCastRange();

          if (((sk.getSkillType() != L2Skill.SkillType.BUFF) && (sk.getSkillType() != L2Skill.SkillType.HEAL) && ((dist_2 < castRange * castRange / 9.0D) || (dist_2 > castRange * castRange) || (castRange <= 70))) || (_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (sk.isPassive()))
          {
            continue;
          }
          L2Object OldTarget = _actor.getTarget();
          if ((sk.getSkillType() == L2Skill.SkillType.BUFF) || (sk.getSkillType() == L2Skill.SkillType.HEAL))
          {
            boolean useSkillSelf = true;
            if ((sk.getSkillType() == L2Skill.SkillType.HEAL) && (_actor.getCurrentHp() > (int)(_actor.getMaxHp() / 1.5D)))
            {
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

      if ((!_actor.isAttackingNow()) && (_actor.getRunSpeed() == 0) && (_actor.getKnownList().knowsObject(target)))
      {
        _actor.getKnownList().removeKnownObject(target);
        _actor.setTarget(null);
        setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      } else {
        double dx = _actor.getX() - target.getX();
        double dy = _actor.getY() - target.getY();
        double dz = _actor.getZ() - target.getZ();
        double homeX = target.getX() - sGuard.getHomeX();
        double homeY = target.getY() - sGuard.getHomeY();

        if ((dx * dx + dy * dy > 10000.0D) && (homeX * homeX + homeY * homeY > 3240000.0D) && (_actor.getKnownList().knowsObject(target)))
        {
          _actor.getKnownList().removeKnownObject(target);
          _actor.setTarget(null);
          setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
        }
        else if (dz * dz < 28900.0D)
        {
          moveToPawn(target, range);
        }

      }

      return;
    }

    if ((_actor.isMuted()) && (dist_2 > (range + 20) * (range + 20)))
    {
      double dz = _actor.getZ() - target.getZ();
      if (dz * dz < 28900.0D)
      {
        moveToPawn(target, range);
      }
      return;
    }
    if (dist_2 <= (range + 20) * (range + 20))
    {
      L2Character hated = null;
      if (_actor.isConfused())
        hated = target;
      else {
        hated = ((L2Attackable)_actor).getMostHated();
      }

      if (hated == null) {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        return;
      }
      if (hated != target) {
        setAttackTarget(hated);
      }

      _attackTimeout = (300 + GameTimeController.getGameTicks());

      if ((!_actor.isMuted()) && (Rnd.nextInt(100) <= 5)) {
        for (L2Skill sk : skills) {
          int castRange = sk.getCastRange();

          if ((castRange * castRange < dist_2) || (castRange > 70) || (sk.isPassive()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (_actor.isSkillDisabled(sk.getId()))) {
            continue;
          }
          L2Object OldTarget = _actor.getTarget();
          if ((sk.getSkillType() == L2Skill.SkillType.BUFF) || (sk.getSkillType() == L2Skill.SkillType.HEAL))
          {
            boolean useSkillSelf = true;
            if ((sk.getSkillType() == L2Skill.SkillType.HEAL) && (_actor.getCurrentHp() > (int)(_actor.getMaxHp() / 1.5D)))
            {
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

      _accessor.doAttack(target);
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

    if ((getAttackTarget() == null) || (getAttackTarget().isAlikeDead()) || (_attackTimeout < GameTimeController.getGameTicks()))
    {
      if (getAttackTarget() != null) {
        L2Attackable npc = (L2Attackable)_actor;
        npc.stopHating(getAttackTarget());
      }

      _attackTimeout = 2147483647;
      setAttackTarget(null);

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

      _actor.setWalking();
      return;
    }

    attackPrepare();
    factionNotify();
  }

  private void factionNotify()
  {
    if ((_actor == null) || (((L2NpcInstance)_actor).getFactionId() == null)) {
      return;
    }

    if ((getAttackTarget() == null) || (getAttackTarget().isInvul())) {
      return;
    }

    String faction_id = ((L2NpcInstance)_actor).getFactionId();

    for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(1000)) {
      if ((cha == null) || 
        (!cha.isL2Npc()))
      {
        continue;
      }
      L2NpcInstance npc = (L2NpcInstance)cha;

      if (!faction_id.equals(npc.getFactionId()))
      {
        continue;
      }

      if (((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)) && (_actor.isInsideRadius(npc, npc.getFactionRange(), false, true)) && (npc.getTarget() == null) && (getAttackTarget().isInsideRadius(npc, npc.getFactionRange(), false, true)))
      {
        if (npc.canSeeTarget(getAttackTarget()))
        {
          npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), Integer.valueOf(1));
        }
      }
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

    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
    }

    super.onEvtAttacked(attacker);
  }

  protected void onEvtAggression(L2Character target, int aggro)
  {
    if (_actor == null) {
      return;
    }
    L2Attackable me = (L2Attackable)_actor;

    if (target != null)
    {
      me.addDamageHate(target, 0, aggro);

      aggro = me.getHating(target);

      if (aggro <= 0) {
        if (me.getMostHated() == null) {
          _globalAggro = -25;
          me.clearAggroList();
          setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
        }
        return;
      }

      if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
      {
        if (!_actor.isRunning()) {
          _actor.setRunning();
        }

        L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance)_actor;
        double homeX = target.getX() - sGuard.getHomeX();
        double homeY = target.getY() - sGuard.getHomeY();

        if (homeX * homeX + homeY * homeY < 3240000.0D)
        {
          setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
        }
      }
    }
    else {
      if (aggro >= 0) {
        return;
      }

      L2Character mostHated = me.getMostHated();
      if (mostHated == null) {
        _globalAggro = -25;
        return;
      }
      for (L2Character aggroed : me.getAggroList().keySet()) {
        me.addDamageHate(aggroed, 0, aggro);
      }

      aggro = me.getHating(mostHated);
      if (aggro <= 0) {
        _globalAggro = -25;
        me.clearAggroList();
        setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      }
    }
  }

  protected void onEvtDead()
  {
    stopAITask();
    super.onEvtDead();
  }

  public void stopAITask() {
    if (_aiTask != null) {
      _aiTask.cancel(false);
      _aiTask = null;
    }
    _accessor.detachAI();
  }
}