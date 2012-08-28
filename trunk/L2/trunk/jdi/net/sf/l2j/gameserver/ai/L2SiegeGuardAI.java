package net.sf.l2j.gameserver.ai;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class L2SiegeGuardAI extends L2CharacterAI
  implements Runnable
{
  private static final int MAX_ATTACK_TIMEOUT = 300;
  private Future<?> _aiTask;
  private L2CharacterAI.SelfAnalysis _selfAnalysis = new L2CharacterAI.SelfAnalysis(this);
  private int _attackTimeout;
  private int _globalAggro;
  private boolean _thinking;
  private int _attackRange;

  public L2SiegeGuardAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
    _selfAnalysis.init();
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
    if ((target == null) || ((target instanceof L2SiegeGuardInstance)) || ((target instanceof L2FolkInstance)) || ((target instanceof L2DoorInstance)) || (target.isAlikeDead()))
    {
      return false;
    }

    if (target.isInvul())
    {
      if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isGM()))
        return false;
      if (((target instanceof L2Summon)) && (((L2Summon)target).getOwner().isGM())) {
        return false;
      }
    }

    if ((target instanceof L2Summon))
    {
      L2PcInstance owner = ((L2Summon)target).getOwner();
      if (_actor.isInsideRadius(owner, 1000, true, false)) {
        target = owner;
      }
    }

    if ((target instanceof L2PlayableInstance))
    {
      if ((((L2PlayableInstance)target).isSilentMoving()) && (!_actor.isInsideRadius(target, 250, false, false))) {
        return false;
      }
    }
    return (_actor.isAutoAttackable(target)) && (GeoData.getInstance().canSeeTarget(_actor, target));
  }

  synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    if (Config.DEBUG) {
      _log.info("L2SiegeAI.changeIntention(" + intention + ", " + arg0 + ", " + arg1 + ")");
    }
    if (intention == CtrlIntention.AI_INTENTION_IDLE)
    {
      if (!_actor.isAlikeDead())
      {
        L2Attackable npc = (L2Attackable)_actor;

        if (npc.getKnownList().getKnownPlayers().size() > 0) intention = CtrlIntention.AI_INTENTION_ACTIVE; else {
          intention = CtrlIntention.AI_INTENTION_IDLE;
        }
      }
      if (intention == CtrlIntention.AI_INTENTION_IDLE)
      {
        super.changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

        if (_aiTask != null)
        {
          _aiTask.cancel(true);
          _aiTask = null;
        }

        _accessor.detachAI();

        return;
      }

    }

    super.changeIntention(intention, arg0, arg1);

    if (_aiTask == null)
    {
      _aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000L, 1000L);
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

    if (_globalAggro != 0)
    {
      if (_globalAggro < 0) _globalAggro += 1; else {
        _globalAggro -= 1;
      }

    }

    if (_globalAggro >= 0)
    {
      for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(_attackRange))
      {
        if (target != null)
          if (autoAttackCondition(target))
          {
            int hating = npc.getHating(target);

            if (hating == 0) npc.addDamageHate(target, 0, 1);
          }
      }
      L2Character hated;
      L2Character hated;
      if (_actor.isConfused()) hated = _attackTarget; else {
        hated = npc.getMostHated();
      }

      if (hated != null)
      {
        int aggro = npc.getHating(hated);

        if (aggro + _globalAggro > 0)
        {
          if (!_actor.isRunning()) _actor.setRunning();

          setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
        }

        return;
      }

    }

    ((L2SiegeGuardInstance)_actor).returnHome();
  }

  private void thinkAttack()
  {
    if (Config.DEBUG) {
      _log.info("L2SiegeGuardAI.thinkAttack(); timeout=" + (_attackTimeout - GameTimeController.getGameTicks()));
    }

    if (_attackTimeout < GameTimeController.getGameTicks())
    {
      if (_actor.isRunning())
      {
        _actor.setWalking();

        _attackTimeout = (300 + GameTimeController.getGameTicks());
      }

    }

    if ((_attackTarget == null) || (_attackTarget.isAlikeDead()) || (_attackTimeout < GameTimeController.getGameTicks()))
    {
      if (_attackTarget != null)
      {
        L2Attackable npc = (L2Attackable)_actor;
        npc.stopHating(_attackTarget);
      }

      _attackTimeout = 2147483647;
      _attackTarget = null;

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

      _actor.setWalking();
      return;
    }

    factionNotifyAndSupport();
    attackPrepare();
  }

  private final void factionNotifyAndSupport()
  {
    L2Character target = getAttackTarget();

    if ((((L2NpcInstance)_actor).getFactionId() == null) || (target == null) || (_actor == null)) {
      return;
    }
    if (target.isInvul()) return;

    String faction_id = ((L2NpcInstance)_actor).getFactionId();

    for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(1000L))
    {
      if (cha == null)
        continue;
      if (!(cha instanceof L2NpcInstance))
      {
        if ((!_selfAnalysis.hasHealOrResurrect) || (!(cha instanceof L2PcInstance)) || (!((L2NpcInstance)_actor).getCastle().getSiege().checkIsDefender(((L2PcInstance)cha).getClan())) || 
          (_actor.isAttackingDisabled()) || (cha.getCurrentHp() >= cha.getMaxHp() * 0.6D) || (_actor.getCurrentHp() <= _actor.getMaxHp() / 2) || (_actor.getCurrentMp() <= _actor.getMaxMp() / 2) || (!cha.isInCombat()))
        {
          continue;
        }

        for (L2Skill sk : _selfAnalysis.healSkills)
        {
          if ((_actor.getCurrentMp() < sk.getMpConsume()) || 
            (_actor.isSkillDisabled(sk.getId())) || 
            (!Util.checkIfInRange(sk.getCastRange(), _actor, cha, true))) {
            continue;
          }
          int chance = 5;
          if (chance >= Rnd.get(100))
            continue;
          if (!GeoData.getInstance().canSeeTarget(_actor, cha)) {
            break;
          }
          L2Object OldTarget = _actor.getTarget();
          _actor.setTarget(cha);
          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(OldTarget);
          return; } continue;
      }

      npc = (L2NpcInstance)cha;

      if (faction_id != npc.getFactionId())
        continue;
      if (npc.getAI() != null)
      {
        if ((!npc.isDead()) && (Math.abs(target.getZ() - npc.getZ()) < 600) && ((npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)) && (target.isInsideRadius(npc, 1500, true, false)) && (GeoData.getInstance().canSeeTarget(npc, target)))
        {
          npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), Integer.valueOf(1));
        }

        if ((_selfAnalysis.hasHealOrResurrect) && (!_actor.isAttackingDisabled()) && (npc.getCurrentHp() < npc.getMaxHp() * 0.6D) && (_actor.getCurrentHp() > _actor.getMaxHp() / 2) && (_actor.getCurrentMp() > _actor.getMaxMp() / 2) && (npc.isInCombat()))
        {
          for (L2Skill sk : _selfAnalysis.healSkills)
          {
            if ((_actor.getCurrentMp() < sk.getMpConsume()) || 
              (_actor.isSkillDisabled(sk.getId())) || 
              (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))) {
              continue;
            }
            int chance = 4;
            if (chance >= Rnd.get(100))
              continue;
            if (!GeoData.getInstance().canSeeTarget(_actor, npc)) {
              break;
            }
            L2Object OldTarget = _actor.getTarget();
            _actor.setTarget(npc);
            clientStopMoving(null);
            _accessor.doCast(sk);
            _actor.setTarget(OldTarget);
            return;
          }
        }
      }
    }
    L2NpcInstance npc;
  }

  private void attackPrepare() {
    L2Skill[] skills = null;
    double dist_2 = 0.0D;
    int range = 0;
    L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance)_actor;
    try
    {
      _actor.setTarget(_attackTarget);
      skills = _actor.getAllSkills();
      dist_2 = _actor.getPlanDistanceSq(_attackTarget.getX(), _attackTarget.getY());
      range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + _attackTarget.getTemplate().collisionRadius;
      if (_attackTarget.isMoving()) range += 50;

    }
    catch (NullPointerException e)
    {
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if (((_attackTarget instanceof L2PcInstance)) && (sGuard.getCastle().getSiege().checkIsDefender(((L2PcInstance)_attackTarget).getClan())))
    {
      sGuard.stopHating(_attackTarget);
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if (!GeoData.getInstance().canSeeTarget(_actor, _attackTarget))
    {
      sGuard.stopHating(_attackTarget);
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if ((!_actor.isMuted()) && (dist_2 > range * range))
    {
      for (L2Skill sk : skills)
      {
        int castRange = sk.getCastRange();

        if ((dist_2 > castRange * castRange) || (castRange <= 70) || (_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (sk.isPassive()))
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
          if (sk.getSkillType() == L2Skill.SkillType.BUFF)
          {
            L2Effect[] effects = _actor.getAllEffects();
            for (int i = 0; (effects != null) && (i < effects.length); i++)
            {
              L2Effect effect = effects[i];
              if (effect.getSkill() != sk)
                continue;
              useSkillSelf = false;
              break;
            }
          }

          if (useSkillSelf) _actor.setTarget(_actor);
        }

        clientStopMoving(null);
        _accessor.doCast(sk);
        _actor.setTarget(OldTarget);
        return;
      }

      if ((!_actor.isAttackingNow()) && (_actor.getRunSpeed() == 0) && (_actor.getKnownList().knowsObject(_attackTarget)))
      {
        _actor.getKnownList().removeKnownObject(_attackTarget);
        _actor.setTarget(null);
        setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      }
      else
      {
        double dx = _actor.getX() - _attackTarget.getX();
        double dy = _actor.getY() - _attackTarget.getY();
        double dz = _actor.getZ() - _attackTarget.getZ();
        double homeX = _attackTarget.getX() - sGuard.getSpawn().getLocx();
        double homeY = _attackTarget.getY() - sGuard.getSpawn().getLocy();

        if ((dx * dx + dy * dy > 10000.0D) && (homeX * homeX + homeY * homeY > 3240000.0D) && (_actor.getKnownList().knowsObject(_attackTarget)))
        {
          _actor.getKnownList().removeKnownObject(_attackTarget);
          _actor.setTarget(null);
          setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
        }
        else if (dz * dz < 28900.0D)
        {
          if (_selfAnalysis.isMage) range = _selfAnalysis.maxCastRange - 50;
          if (_attackTarget.isMoving())
            moveToPawn(_attackTarget, range - 70);
          else {
            moveToPawn(_attackTarget, range);
          }
        }
      }

      return;
    }

    if ((_actor.isMuted()) && (dist_2 > range * range))
    {
      double dz = _actor.getZ() - _attackTarget.getZ();
      if (dz * dz < 28900.0D)
      {
        if (_selfAnalysis.isMage) range = _selfAnalysis.maxCastRange - 50;
        if (_attackTarget.isMoving())
          moveToPawn(_attackTarget, range - 70);
        else
          moveToPawn(_attackTarget, range);
      }
      return;
    }

    if (dist_2 <= range * range)
    {
      L2Character hated = null;
      if (_actor.isConfused()) hated = _attackTarget; else {
        hated = ((L2Attackable)_actor).getMostHated();
      }
      if (hated == null)
      {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        return;
      }
      if (hated != _attackTarget) _attackTarget = hated;

      _attackTimeout = (300 + GameTimeController.getGameTicks());

      if ((!_actor.isMuted()) && (Rnd.nextInt(100) <= 5))
      {
        for (L2Skill sk : skills)
        {
          int castRange = sk.getCastRange();

          if ((castRange * castRange < dist_2) || (sk.isPassive()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (_actor.isSkillDisabled(sk.getId())))
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
            if (sk.getSkillType() == L2Skill.SkillType.BUFF)
            {
              L2Effect[] effects = _actor.getAllEffects();
              for (int i = 0; (effects != null) && (i < effects.length); i++)
              {
                L2Effect effect = effects[i];
                if (effect.getSkill() != sk)
                  continue;
                useSkillSelf = false;
                break;
              }
            }

            if (useSkillSelf) _actor.setTarget(_actor);
          }

          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(OldTarget);
          return;
        }

      }

      _accessor.doAttack(_attackTarget);
    }
  }

  protected void onEvtThink()
  {
    if ((_thinking) || (_actor.isAllSkillsDisabled())) return;

    _thinking = true;
    try
    {
      if (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) thinkActive();
      else if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK) thinkAttack();

    }
    finally
    {
      _thinking = false;
    }
  }

  protected void onEvtAttacked(L2Character attacker)
  {
    _attackTimeout = (300 + GameTimeController.getGameTicks());

    if (_globalAggro < 0) _globalAggro = 0;

    ((L2Attackable)_actor).addDamageHate(attacker, 0, 1);

    if (!_actor.isRunning()) _actor.setRunning();

    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
    {
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
    }

    super.onEvtAttacked(attacker);
  }

  protected void onEvtAggression(L2Character target, int aggro)
  {
    if (_actor == null) return;
    L2Attackable me = (L2Attackable)_actor;

    if (target != null)
    {
      me.addDamageHate(target, 0, aggro);

      aggro = me.getHating(target);

      if (aggro <= 0)
      {
        if (me.getMostHated() == null)
        {
          _globalAggro = -25;
          me.clearAggroList();
          setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
        }
        return;
      }

      if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
      {
        if (!_actor.isRunning()) _actor.setRunning();

        L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance)_actor;
        double homeX = target.getX() - sGuard.getSpawn().getLocx();
        double homeY = target.getY() - sGuard.getSpawn().getLocy();

        if (homeX * homeX + homeY * homeY < 3240000.0D) {
          setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
        }
      }
    }
    else
    {
      if (aggro >= 0) return;

      L2Character mostHated = me.getMostHated();
      if (mostHated == null)
      {
        _globalAggro = -25;
        return;
      }

      for (L2Character aggroed : me.getAggroListRP().keySet()) {
        me.addDamageHate(aggroed, 0, aggro);
      }
      aggro = me.getHating(mostHated);
      if (aggro <= 0)
      {
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

  public void stopAITask()
  {
    if (_aiTask != null)
    {
      _aiTask.cancel(false);
      _aiTask = null;
    }
    _accessor.detachAI();
  }
}