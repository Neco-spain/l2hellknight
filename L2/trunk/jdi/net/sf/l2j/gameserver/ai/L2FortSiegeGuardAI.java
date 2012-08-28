package net.sf.l2j.gameserver.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2CommanderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class L2FortSiegeGuardAI extends L2CharacterAI
  implements Runnable
{
  protected static final Logger _log1 = Logger.getLogger(L2FortSiegeGuardAI.class.getName());

  public List<L2Skill> pdamSkills = new FastList();
  public List<L2Skill> mdamSkills = new FastList();
  public List<L2Skill> healSkills = new FastList();
  public List<L2Skill> rootSkills = new FastList();

  public boolean hasPDam = false;
  public boolean hasMDam = false;
  public boolean hasHeal = false;
  public boolean hasRoot = false;
  private static final int MAX_ATTACK_TIMEOUT = 300;
  private Future<?> _aiTask;
  private int _attackTimeout;
  private int _globalAggro;
  private boolean _thinking;
  private int _attackRange;

  public L2FortSiegeGuardAI(L2Character.AIAccessor accessor)
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
    if ((target == null) || ((target instanceof L2FortSiegeGuardInstance)) || ((target instanceof L2FolkInstance)) || ((target instanceof L2DoorInstance)) || (target.isAlikeDead()) || ((target instanceof L2CommanderInstance)) || ((target instanceof L2PlayableInstance)))
    {
      L2PcInstance player = null;
      if ((target instanceof L2PcInstance))
      {
        player = (L2PcInstance)target;
      }
      else if ((target instanceof L2Summon))
      {
        player = ((L2Summon)target).getOwner();
      }
      if ((player == null) || ((player != null) && (player.getClan() != null) && (player.getClan().getHasFort() == ((L2NpcInstance)_actor).getFort().getFortId()))) {
        return false;
      }
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
      if (_actor.isInsideRadius(owner, 1000, true, false))
      {
        target = owner;
      }

    }

    if ((target instanceof L2PcInstance))
    {
      if ((((L2PcInstance)target).isSilentMoving()) && (!_actor.isInsideRadius(target, 250, false, false))) {
        return false;
      }
    }
    return (_actor.isAutoAttackable(target)) && (GeoData.getInstance().canSeeTarget(_actor, target));
  }

  synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    if (Config.DEBUG)
    {
      _log1.info("L2SiegeAI.changeIntention(" + intention + ", " + arg0 + ", " + arg1 + ")");
    }

    if (intention == CtrlIntention.AI_INTENTION_IDLE)
    {
      if (!_actor.isAlikeDead())
      {
        L2Attackable npc = (L2Attackable)_actor;

        if (npc.getKnownList().getKnownPlayers().size() > 0)
        {
          intention = CtrlIntention.AI_INTENTION_ACTIVE;
        }
        else
        {
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
      if (_globalAggro < 0)
      {
        _globalAggro += 1;
      }
      else
      {
        _globalAggro -= 1;
      }

    }

    if (_globalAggro >= 0)
    {
      for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(_attackRange))
      {
        if (target == null)
        {
          continue;
        }
        if (autoAttackCondition(target))
        {
          int hating = npc.getHating(target);

          if (hating == 0)
          {
            npc.addDamageHate(target, 0, 1);
          }
        }
      }
      L2Character hated;
      L2Character hated;
      if (_actor.isConfused())
      {
        hated = getAttackTarget();
      }
      else
      {
        hated = npc.getMostHated();
      }

      if (hated != null)
      {
        int aggro = npc.getHating(hated);

        if (aggro + _globalAggro > 0)
        {
          if (!_actor.isRunning())
          {
            _actor.setRunning();
          }

          setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
        }

        return;
      }

    }

    if (_actor.getWalkSpeed() >= 0)
    {
      if ((_actor instanceof L2FortSiegeGuardInstance))
      {
        ((L2FortSiegeGuardInstance)_actor).returnHome();
      }
      else
      {
        ((L2CommanderInstance)_actor).returnHome();
      }
    }
  }

  private void thinkAttack()
  {
    if (Config.DEBUG)
    {
      _log1.info("L2FortSiegeGuardAI.thinkAttack(); timeout=" + (_attackTimeout - GameTimeController.getGameTicks()));
    }

    if (_attackTimeout < GameTimeController.getGameTicks())
    {
      if (_actor.isRunning())
      {
        _actor.setWalking();

        _attackTimeout = (300 + GameTimeController.getGameTicks());
      }
    }

    L2Character attackTarget = getAttackTarget();

    if ((attackTarget == null) || (attackTarget.isAlikeDead()) || (_attackTimeout < GameTimeController.getGameTicks()))
    {
      if (attackTarget != null)
      {
        L2Attackable npc = (L2Attackable)_actor;
        npc.stopHating(attackTarget);
      }

      _attackTimeout = 2147483647;
      setAttackTarget(null);

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

    if ((((L2NpcInstance)_actor).getFactionId() == null) || (target == null)) {
      return;
    }
    if (target.isInvul()) {
      return;
    }
    if (Rnd.get(10) > 4) {
      return;
    }
    String faction_id = ((L2NpcInstance)_actor).getFactionId();

    for (L2Skill sk : _actor.getAllSkills())
    {
      if (sk.isPassive())
      {
        continue;
      }

      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillType[sk.getSkillType().ordinal()])
      {
      case 1:
        rootSkills.add(sk);
        hasPDam = true;
        break;
      case 2:
        rootSkills.add(sk);
        hasMDam = true;
        break;
      case 3:
        healSkills.add(sk);
        hasHeal = true;
        break;
      case 4:
        rootSkills.add(sk);
        hasRoot = true;
      }

    }

    for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(1000L))
    {
      if (cha == null)
      {
        continue;
      }

      if (!(cha instanceof L2NpcInstance))
      {
        if ((!(cha instanceof L2PcInstance)) || (!((L2NpcInstance)_actor).getFort().getSiege().checkIsDefender(((L2PcInstance)cha).getClan())) || 
          (_actor.isAttackingDisabled()) || (cha.getCurrentHp() >= cha.getMaxHp() * 0.6D) || (_actor.getCurrentHp() <= _actor.getMaxHp() / 2) || (_actor.getCurrentMp() <= _actor.getMaxMp() / 2) || (!cha.isInCombat()))
          continue;
        for (L2Skill sk : healSkills)
        {
          if ((_actor.getCurrentMp() < sk.getMpConsume()) || 
            (_actor.isSkillDisabled(sk.getId())) || 
            (!Util.checkIfInRange(sk.getCastRange(), _actor, cha, true)))
          {
            continue;
          }

          int chance = 5;
          if (chance >= Rnd.get(100))
          {
            continue;
          }
          if (!GeoData.getInstance().canSeeTarget(_actor, cha))
          {
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
      {
        continue;
      }

      if (npc.getAI() != null)
      {
        if ((!npc.isDead()) && (Math.abs(target.getZ() - npc.getZ()) < 600) && ((npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)) && (target.isInsideRadius(npc, 1500, true, false)) && (GeoData.getInstance().canSeeTarget(npc, target)))
        {
          npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), Integer.valueOf(1));
        }

        if ((!_actor.isAttackingDisabled()) && (npc.getCurrentHp() < npc.getMaxHp() * 0.6D) && (_actor.getCurrentHp() > _actor.getMaxHp() / 2) && (_actor.getCurrentMp() > _actor.getMaxMp() / 2) && (npc.isInCombat()))
        {
          for (L2Skill sk : healSkills)
          {
            if ((_actor.getCurrentMp() < sk.getMpConsume()) || 
              (_actor.isSkillDisabled(sk.getId())) || 
              (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true)))
            {
              continue;
            }

            int chance = 4;
            if (chance >= Rnd.get(100))
            {
              continue;
            }
            if (!GeoData.getInstance().canSeeTarget(_actor, npc))
            {
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

    L2FortSiegeGuardInstance sGuard = (L2FortSiegeGuardInstance)_actor;
    L2Character attackTarget = getAttackTarget();
    try
    {
      _actor.setTarget(attackTarget);
      skills = _actor.getAllSkills();
      dist_2 = _actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY());
      range = _actor.getPhysicalAttackRange() + _actor.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius;
      if (attackTarget.isMoving())
      {
        range += 50;
      }
    }
    catch (NullPointerException e)
    {
      e.printStackTrace();

      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if (((attackTarget instanceof L2PcInstance)) && (sGuard.getFort().getSiege().checkIsDefender(((L2PcInstance)attackTarget).getClan())))
    {
      sGuard.stopHating(attackTarget);
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if (!GeoData.getInstance().canSeeTarget(_actor, attackTarget))
    {
      sGuard.stopHating(attackTarget);
      _actor.setTarget(null);
      setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      return;
    }

    if ((!_actor.isMuted()) && (dist_2 > range * range))
    {
      for (L2Skill sk : skills)
      {
        int castRange = sk.getCastRange();

        if ((dist_2 > castRange * castRange) || (castRange <= 70) || (_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (sk.isPassive())) {
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

          if (useSkillSelf)
          {
            _actor.setTarget(_actor);
          }
        }

        clientStopMoving(null);
        _accessor.doCast(sk);
        _actor.setTarget(OldTarget);
        return;
      }

      if ((!_actor.isAttackingNow()) && (_actor.getRunSpeed() == 0) && (_actor.getKnownList().knowsObject(attackTarget)))
      {
        _actor.getKnownList().removeKnownObject(attackTarget);
        _actor.setTarget(null);
        setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
      }
      else
      {
        double dx = _actor.getX() - attackTarget.getX();
        double dy = _actor.getY() - attackTarget.getY();
        double dz = _actor.getZ() - attackTarget.getZ();
        double homeX = attackTarget.getX() - sGuard.getSpawn().getLocx();
        double homeY = attackTarget.getY() - sGuard.getSpawn().getLocy();

        if ((dx * dx + dy * dy > 10000.0D) && (homeX * homeX + homeY * homeY > 3240000.0D) && (_actor.getKnownList().knowsObject(attackTarget)))
        {
          _actor.getKnownList().removeKnownObject(attackTarget);
          _actor.setTarget(null);
          setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
        }
        else if (dz * dz < 28900.0D)
        {
          if (_actor.getWalkSpeed() <= 0)
            return;
          if (attackTarget.isMoving())
          {
            moveToPawn(attackTarget, range - 70);
          }
          else
          {
            moveToPawn(attackTarget, range);
          }
        }

      }

      return;
    }

    if ((_actor.isMuted()) && (dist_2 > range * range))
    {
      double dz = _actor.getZ() - attackTarget.getZ();
      if (dz * dz < 28900.0D)
      {
        if (_actor.getWalkSpeed() <= 0)
          return;
        if (attackTarget.isMoving())
        {
          moveToPawn(attackTarget, range - 70);
        }
        else
        {
          moveToPawn(attackTarget, range);
        }
      }
      return;
    }

    if (dist_2 <= range * range)
    {
      L2Character hated = null;
      if (_actor.isConfused())
      {
        hated = attackTarget;
      }
      else
      {
        hated = ((L2Attackable)_actor).getMostHated();
      }

      if (hated == null)
      {
        setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        return;
      }
      if (hated != attackTarget)
      {
        attackTarget = hated;
      }

      _attackTimeout = (300 + GameTimeController.getGameTicks());

      if ((!_actor.isMuted()) && (Rnd.nextInt(100) <= 5))
      {
        for (L2Skill sk : skills)
        {
          int castRange = sk.getCastRange();

          if ((castRange * castRange < dist_2) || (sk.isPassive()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (_actor.isSkillDisabled(sk.getId())))
            continue;
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

            if (useSkillSelf)
            {
              _actor.setTarget(_actor);
            }
          }

          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(OldTarget);
          return;
        }

      }

      _accessor.doAttack(attackTarget);
    }
  }

  protected void onEvtThink()
  {
    if ((_thinking) || (_actor.isCastingNow()) || (_actor.isAllSkillsDisabled())) {
      return;
    }

    _thinking = true;
    try
    {
      if (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
      {
        thinkActive();
      }
      else if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
      {
        thinkAttack();
      }

    }
    finally
    {
      _thinking = false;
    }
  }

  protected void onEvtAttacked(L2Character attacker)
  {
    _attackTimeout = (300 + GameTimeController.getGameTicks());

    if (_globalAggro < 0)
    {
      _globalAggro = 0;
    }

    ((L2Attackable)_actor).addDamageHate(attacker, 0, 1);

    if (!_actor.isRunning())
    {
      _actor.setRunning();
    }

    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
    {
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
    }

    super.onEvtAttacked(attacker);
  }

  protected void onEvtAggression(L2Character target, int aggro)
  {
    if (_actor == null)
      return;
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
        if (!_actor.isRunning())
        {
          _actor.setRunning();
        }

        L2FortSiegeGuardInstance sGuard = (L2FortSiegeGuardInstance)_actor;
        double homeX = target.getX() - sGuard.getSpawn().getLocx();
        double homeY = target.getY() - sGuard.getSpawn().getLocy();

        if (homeX * homeX + homeY * homeY < 3240000.0D)
        {
          setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
        }
      }

    }
    else
    {
      if (aggro >= 0) {
        return;
      }
      L2Character mostHated = me.getMostHated();
      if (mostHated == null)
      {
        _globalAggro = -25;
        return;
      }

      for (L2Character aggroed : me.getAggroListRP().keySet())
      {
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