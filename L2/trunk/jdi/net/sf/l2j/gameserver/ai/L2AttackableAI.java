package net.sf.l2j.gameserver.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FriendlyMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
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
  private L2CharacterAI.SelfAnalysis _selfAnalysis = new L2CharacterAI.SelfAnalysis(this);
  private L2CharacterAI.TargetAnalysis _mostHatedAnalysis = new L2CharacterAI.TargetAnalysis(this);
  private L2CharacterAI.TargetAnalysis _secondMostHatedAnalysis = new L2CharacterAI.TargetAnalysis(this);

  public L2AttackableAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
    _selfAnalysis.init();
    _attackTimeout = 2147483647;
    _globalAggro = -10;
  }

  public void run()
  {
    onEvtThink();
  }

  private boolean autoAttackCondition(L2Character target)
  {
    if ((target == null) || (!(_actor instanceof L2Attackable))) return false;
    L2Attackable me = (L2Attackable)_actor;

    if (target.isInvul())
    {
      if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).isGM()))
        return false;
      if (((target instanceof L2Summon)) && (((L2Summon)target).getOwner().isGM())) {
        return false;
      }
    }

    if (((target instanceof L2FolkInstance)) || ((target instanceof L2DoorInstance))) return false;

    if ((target.isAlikeDead()) || (!me.isInsideRadius(target, me.getAggroRange(), false, false)) || (Math.abs(_actor.getZ() - target.getZ()) > 300))
    {
      return false;
    }
    if ((_selfAnalysis.cannotMoveOnLand) && (!target.isInsideZone(128))) {
      return false;
    }

    if ((target instanceof L2PcInstance))
    {
      if ((((L2PcInstance)target).isGM()) && (((L2PcInstance)target).getAccessLevel() <= Config.GM_DONT_TAKE_AGGRO)) {
        return false;
      }

      if ((!(me instanceof L2RaidBossInstance)) && (((L2PcInstance)target).isSilentMoving())) {
        return false;
      }

      if ((me.getFactionId() == "varka") && (((L2PcInstance)target).isAlliedWithVarka()))
        return false;
      if ((me.getFactionId() == "ketra") && (((L2PcInstance)target).isAlliedWithKetra())) {
        return false;
      }
      if (((L2PcInstance)target).isRecentFakeDeath()) {
        return false;
      }
      if ((target.isInParty()) && (target.getParty().isInDimensionalRift()))
      {
        byte riftType = target.getParty().getDimensionalRift().getType();
        byte riftRoom = target.getParty().getDimensionalRift().getCurrentRoom();

        if (((me instanceof L2RiftInvaderInstance)) && (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ())))
        {
          return false;
        }
      }
    }
    if ((target instanceof L2Summon))
    {
      L2PcInstance owner = ((L2Summon)target).getOwner();
      if (owner != null)
      {
        if ((owner.isGM()) && ((owner.isInvul()) || (owner.getAccessLevel() <= Config.GM_DONT_TAKE_AGGRO))) {
          return false;
        }
        if ((me.getFactionId() == "varka") && (owner.isAlliedWithVarka()))
          return false;
        if ((me.getFactionId() == "ketra") && (owner.isAlliedWithKetra())) {
          return false;
        }
      }
    }
    if ((_actor instanceof L2GuardInstance))
    {
      if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).getKarma() > 0))
      {
        return GeoData.getInstance().canSeeTarget(me, target);
      }

      if ((target instanceof L2MonsterInstance)) {
        return (((L2MonsterInstance)target).isAggressive()) && (GeoData.getInstance().canSeeTarget(me, target));
      }
      return false;
    }
    if ((_actor instanceof L2FriendlyMobInstance))
    {
      if ((target instanceof L2NpcInstance)) return false;

      if (((target instanceof L2PcInstance)) && (((L2PcInstance)target).getKarma() > 0))
      {
        return GeoData.getInstance().canSeeTarget(me, target);
      }
      return false;
    }

    if ((target instanceof L2NpcInstance)) return false;

    if ((!Config.ALT_MOB_AGRO_IN_PEACEZONE) && (target.isInsideZone(2))) {
      return false;
    }

    return (me.isAggressive()) && (GeoData.getInstance().canSeeTarget(me, target));
  }

  public void startAITask()
  {
    if (_aiTask == null)
    {
      _aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000L, 1000L);
    }
  }

  public void stopAITask()
  {
    if (_aiTask != null)
    {
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
      if (!_actor.isAlikeDead())
      {
        L2Attackable npc = (L2Attackable)_actor;

        if (npc.getKnownList().getKnownPlayers().size() > 0) intention = CtrlIntention.AI_INTENTION_ACTIVE;
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

    startAITask();
  }

  protected void onIntentionAttack(L2Character target)
  {
    _attackTimeout = (300 + GameTimeController.getGameTicks());

    if (_selfAnalysis.lastBuffTick + 100 < GameTimeController.getGameTicks())
    {
      for (L2Skill sk : _selfAnalysis.buffSkills)
      {
        if (_actor.getFirstEffect(sk.getId()) == null)
        {
          if ((_actor.getCurrentMp() < sk.getMpConsume()) || 
            (_actor.isSkillDisabled(sk.getId())) || 
            (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN))
            continue;
          L2Object OldTarget = _actor.getTarget();
          _actor.setTarget(_actor);
          clientStopMoving(null);
          _accessor.doCast(sk);

          _selfAnalysis.lastBuffTick = GameTimeController.getGameTicks();
          _actor.setTarget(OldTarget);
        }
      }
    }

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
      for (L2Object obj : npc.getKnownList().getKnownObjects().values())
      {
        if ((obj instanceof L2Character)) {
          L2Character target = (L2Character)obj;

          if (((_actor instanceof L2FestivalMonsterInstance)) && ((obj instanceof L2PcInstance)))
          {
            L2PcInstance targetPlayer = (L2PcInstance)obj;

            if (!targetPlayer.isFestivalParticipant())
            {
              continue;
            }

          }

          if (autoAttackCondition(target))
          {
            int hating = npc.getHating(target);

            if (hating == 0) npc.addDamageHate(target, 0, 1);
          }
        }
      }
      L2Character hated;
      L2Character hated;
      if (_actor.isConfused()) hated = getAttackTarget(); else {
        hated = npc.getMostHated();
      }

      if (hated != null)
      {
        int aggro = npc.getHating(hated);

        if (aggro + _globalAggro > 0)
        {
          if (!_actor.isRunning()) _actor.setRunning();

          setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
        }

        return;
      }

    }

    if ((_actor instanceof L2GuardInstance))
    {
      ((L2GuardInstance)_actor).returnHome();
    }

    if ((_actor instanceof L2FestivalMonsterInstance)) return;

    if (!npc.canReturnToSpawnPoint()) return;

    if (((_actor instanceof L2MinionInstance)) && (((L2MinionInstance)_actor).getLeader() != null))
    {
      int offset;
      int offset;
      if (_actor.isRaidMinion()) offset = 500; else {
        offset = 200;
      }
      if (((L2MinionInstance)_actor).getLeader().isRunning()) _actor.setRunning(); else {
        _actor.setWalking();
      }
      if (_actor.getPlanDistanceSq(((L2MinionInstance)_actor).getLeader()) > offset * offset)
      {
        int x1 = ((L2MinionInstance)_actor).getLeader().getX() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
        int y1 = ((L2MinionInstance)_actor).getLeader().getY() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
        int z1 = ((L2MinionInstance)_actor).getLeader().getZ();

        moveTo(x1, y1, z1);
        return;
      }
      if (Rnd.nextInt(30) == 0)
      {
        for (L2Skill sk : _selfAnalysis.buffSkills)
        {
          if (_actor.getFirstEffect(sk.getId()) == null)
          {
            if (((sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) && (Rnd.nextInt(2) != 0)) || 
              (_actor.getCurrentMp() < sk.getMpConsume()) || 
              (_actor.isSkillDisabled(sk.getId())))
              continue;
            L2Object OldTarget = _actor.getTarget();
            _actor.setTarget(_actor);
            clientStopMoving(null);
            _accessor.doCast(sk);
            _actor.setTarget(OldTarget);
            return;
          }
        }
      }

    }
    else if ((npc.getSpawn() != null) && (Rnd.nextInt(30) == 0) && (!(_actor instanceof L2RaidBossInstance)) && (!(_actor instanceof L2MinionInstance)))
    {
      int range = Config.MAX_DRIFT_RANGE;

      for (L2Skill sk : _selfAnalysis.buffSkills)
      {
        if (_actor.getFirstEffect(sk.getId()) == null)
        {
          if (((sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) && (Rnd.nextInt(2) != 0)) || 
            (_actor.getCurrentMp() < sk.getMpConsume()) || 
            (_actor.isSkillDisabled(sk.getId())))
            continue;
          L2Object OldTarget = _actor.getTarget();
          _actor.setTarget(_actor);
          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(OldTarget);
          return;
        }
      }

      int x1 = npc.getSpawn().getLocx() + Rnd.nextInt(range * 2) - range;
      int y1 = npc.getSpawn().getLocy() + Rnd.nextInt(range * 2) - range;
      int z1 = npc.getZ();

      if (_actor.getPlanDistanceSq(x1, y1) > range * range)
      {
        npc.setisReturningToSpawnPoint(true);
      }

      moveTo(x1, y1, z1);

      if ((_actor instanceof L2MonsterInstance))
      {
        L2MonsterInstance boss = (L2MonsterInstance)_actor;
        if (boss.hasMinions())
        {
          boss.callMinions();
        }
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

    L2Character originalAttackTarget = getAttackTarget();

    if ((originalAttackTarget == null) || (originalAttackTarget.isAlikeDead()) || (_attackTimeout < GameTimeController.getGameTicks()))
    {
      if (originalAttackTarget != null) {
        ((L2Attackable)_actor).stopHating(originalAttackTarget);
      }

      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

      _actor.setWalking();
      return;
    }
    String faction_id;
    if (((L2NpcInstance)_actor).getFactionId() != null)
    {
      faction_id = ((L2NpcInstance)_actor).getFactionId();

      for (L2Object obj : _actor.getKnownList().getKnownObjects().values())
      {
        if ((obj instanceof L2NpcInstance))
        {
          npc = (L2NpcInstance)obj;

          if (faction_id != npc.getFactionId())
          {
            continue;
          }
          if ((_actor.isInsideRadius(npc, npc.getFactionRange() + npc.getTemplate().collisionRadius, true, false)) && (npc != null) && (_actor != null) && (npc.getAI() != null))
          {
            if ((Math.abs(originalAttackTarget.getZ() - npc.getZ()) < 600) && (_actor.getAttackByList().contains(originalAttackTarget)) && ((npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)) && (GeoData.getInstance().canSeeTarget(_actor, npc)))
            {
              if (((originalAttackTarget instanceof L2PcInstance)) && (originalAttackTarget.isInParty()) && (originalAttackTarget.getParty().isInDimensionalRift()))
              {
                byte riftType = originalAttackTarget.getParty().getDimensionalRift().getType();
                byte riftRoom = originalAttackTarget.getParty().getDimensionalRift().getCurrentRoom();

                if (((_actor instanceof L2RiftInvaderInstance)) && (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ())))
                {
                  continue;
                }

              }

              npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, Integer.valueOf(1));
              if (((originalAttackTarget instanceof L2PcInstance)) || ((originalAttackTarget instanceof L2Summon)))
              {
                if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL) != null)
                {
                  L2PcInstance player = (originalAttackTarget instanceof L2PcInstance) ? (L2PcInstance)originalAttackTarget : ((L2Summon)originalAttackTarget).getOwner();

                  for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL)) {
                    quest.notifyFactionCall(npc, (L2NpcInstance)_actor, player, originalAttackTarget instanceof L2Summon);
                  }
                }
              }
            }
            if ((_selfAnalysis.hasHealOrResurrect) && (!_actor.isAttackingDisabled()) && (npc.getCurrentHp() < npc.getMaxHp() * 0.6D) && (_actor.getCurrentHp() > _actor.getMaxHp() / 2) && (_actor.getCurrentMp() > _actor.getMaxMp() / 2))
            {
              if ((npc.isDead()) && ((_actor instanceof L2MinionInstance)))
              {
                if (((L2MinionInstance)_actor).getLeader() == npc)
                {
                  for (L2Skill sk : _selfAnalysis.resurrectSkills)
                  {
                    if ((_actor.getCurrentMp() < sk.getMpConsume()) || 
                      (_actor.isSkillDisabled(sk.getId())) || 
                      (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true)) || 
                      (10 >= Rnd.get(100)))
                      continue;
                    if (!GeoData.getInstance().canSeeTarget(_actor, npc)) {
                      break;
                    }
                    L2Object OldTarget = _actor.getTarget();
                    _actor.setTarget(npc);

                    DecayTaskManager.getInstance().cancelDecayTask(npc);
                    DecayTaskManager.getInstance().addDecayTask(npc);
                    clientStopMoving(null);
                    _accessor.doCast(sk);
                    _actor.setTarget(OldTarget);
                    return;
                  }
                }
              }
              else if (npc.isInCombat())
              {
                for (L2Skill sk : _selfAnalysis.healSkills)
                {
                  if ((_actor.getCurrentMp() < sk.getMpConsume()) || 
                    (_actor.isSkillDisabled(sk.getId())) || 
                    (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))) {
                    continue;
                  }
                  int chance = 4;
                  if ((_actor instanceof L2MinionInstance))
                  {
                    if (((L2MinionInstance)_actor).getLeader() == npc)
                      chance = 6;
                    else chance = 3;
                  }
                  if ((npc instanceof L2GrandBossInstance))
                    chance = 6;
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
        }
      }
    }
    L2NpcInstance npc;
    if (_actor.isAttackingDisabled()) return;

    List hated = ((L2Attackable)_actor).get2MostHated();
    if (_actor.isConfused())
    {
      if (hated != null) {
        hated.set(0, originalAttackTarget);
      }
      else {
        hated = new FastList();
        hated.add(originalAttackTarget);
        hated.add(null);
      }
    }

    if ((hated == null) || (hated.get(0) == null))
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return;
    }
    if (hated.get(0) != originalAttackTarget)
    {
      setAttackTarget((L2Character)hated.get(0));
    }
    _mostHatedAnalysis.update((L2Character)hated.get(0));
    _secondMostHatedAnalysis.update((L2Character)hated.get(1));

    _actor.setTarget(_mostHatedAnalysis.character);
    double dist2 = _actor.getPlanDistanceSq(_mostHatedAnalysis.character.getX(), _mostHatedAnalysis.character.getY());
    int combinedCollision = _actor.getTemplate().collisionRadius + _mostHatedAnalysis.character.getTemplate().collisionRadius;
    int range = _actor.getPhysicalAttackRange() + combinedCollision;

    if ((!_actor.isMuted()) && (_attackTimeout - 160 < GameTimeController.getGameTicks()) && (_secondMostHatedAnalysis.character != null))
    {
      if (Util.checkIfInRange(900, _actor, (L2Object)hated.get(1), true))
      {
        ((L2Attackable)_actor).reduceHate((L2Character)hated.get(0), 2 * (((L2Attackable)_actor).getHating((L2Character)hated.get(0)) - ((L2Attackable)_actor).getHating((L2Character)hated.get(1))));

        _attackTimeout = (300 + GameTimeController.getGameTicks());
      }

    }

    if ((_actor.isRooted()) && (_secondMostHatedAnalysis.character != null))
    {
      if ((_selfAnalysis.isMage) && (dist2 > _selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange) && (_actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < _selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange))
      {
        ((L2Attackable)_actor).reduceHate((L2Character)hated.get(0), 1 + (((L2Attackable)_actor).getHating((L2Character)hated.get(0)) - ((L2Attackable)_actor).getHating((L2Character)hated.get(1))));
      }
      else if ((dist2 > range * range) && (_actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < range * range))
      {
        ((L2Attackable)_actor).reduceHate((L2Character)hated.get(0), 1 + (((L2Attackable)_actor).getHating((L2Character)hated.get(0)) - ((L2Attackable)_actor).getHating((L2Character)hated.get(1))));
      }

    }

    if ((dist2 < 10000 + combinedCollision * combinedCollision) && (!_selfAnalysis.isFighter) && (!_selfAnalysis.isBalanced) && ((_selfAnalysis.hasLongRangeSkills) || (_selfAnalysis.isArcher)) && ((_mostHatedAnalysis.isBalanced) || (_mostHatedAnalysis.isFighter)) && ((_mostHatedAnalysis.character.isRooted()) || (_mostHatedAnalysis.isSlower))) if ((Config.GEO_PATH_FINDING ? 20 : 12) >= Rnd.get(100))
      {
        int posX = _actor.getX();
        int posY = _actor.getY();
        int posZ = _actor.getZ();
        double distance = Math.sqrt(dist2);

        int signx = -1;
        int signy = -1;
        if (_actor.getX() > _mostHatedAnalysis.character.getX())
          signx = 1;
        if (_actor.getY() > _mostHatedAnalysis.character.getY())
          signy = 1;
        posX += Math.round((float)(signx * (range / 2 + Rnd.get(range)) - distance));
        posY += Math.round((float)(signy * (range / 2 + Rnd.get(range)) - distance));
        setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
        return;
      }


    if ((dist2 > 96100 + combinedCollision * combinedCollision) && (_selfAnalysis.hasLongRangeSkills) && (!GeoData.getInstance().canSeeTarget(_actor, _mostHatedAnalysis.character)))
    {
      if ((!_selfAnalysis.isMage) || (!_actor.isMuted()))
      {
        moveToPawn(_mostHatedAnalysis.character, 300);
        return;
      }
    }

    if (_mostHatedAnalysis.character.isMoving()) range += 50;

    if (dist2 > range * range)
    {
      int castingChance;
      if ((!_actor.isMuted()) && ((_selfAnalysis.hasLongRangeSkills) || (!_selfAnalysis.healSkills.isEmpty())))
      {
        if (!_mostHatedAnalysis.isCanceled)
        {
          for (L2Skill sk : _selfAnalysis.cancelSkills)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
            {
              continue;
            }

            if (Rnd.nextInt(100) <= 8)
            {
              clientStopMoving(null);
              _accessor.doCast(sk);
              _mostHatedAnalysis.isCanceled = true;
              _attackTimeout = (300 + GameTimeController.getGameTicks());
              return;
            }
          }
        }
        if (_selfAnalysis.lastDebuffTick + 60 < GameTimeController.getGameTicks())
        {
          for (L2Skill sk : _selfAnalysis.debuffSkills)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
            {
              continue;
            }

            int chance = 8;
            if ((_selfAnalysis.isFighter) && (_mostHatedAnalysis.isMage))
              chance = 3;
            if ((_selfAnalysis.isFighter) && (_mostHatedAnalysis.isArcher))
              chance = 12;
            if ((_selfAnalysis.isMage) && (!_mostHatedAnalysis.isMage))
              chance = 10;
            if (_mostHatedAnalysis.isMagicResistant) chance /= 2;

            if (Rnd.nextInt(100) <= chance)
            {
              clientStopMoving(null);
              _accessor.doCast(sk);
              _selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
              _attackTimeout = (300 + GameTimeController.getGameTicks());
              return;
            }
          }
        }
        int chance;
        if (!_mostHatedAnalysis.character.isMuted())
        {
          chance = 8;
          if ((!_mostHatedAnalysis.isMage) && (!_mostHatedAnalysis.isBalanced))
            chance = 3;
          for (L2Skill sk : _selfAnalysis.muteSkills)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
            {
              continue;
            }

            if (Rnd.nextInt(100) <= chance)
            {
              clientStopMoving(null);
              _accessor.doCast(sk);
              _attackTimeout = (300 + GameTimeController.getGameTicks());
              return;
            }
          }
        }
        double secondHatedDist2;
        if ((_secondMostHatedAnalysis.character != null) && (!_secondMostHatedAnalysis.character.isMuted()) && ((_secondMostHatedAnalysis.isMage) || (_secondMostHatedAnalysis.isBalanced)))
        {
          secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
          for (L2Skill sk : _selfAnalysis.muteSkills)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > castRange * castRange))
            {
              continue;
            }

            if (Rnd.nextInt(100) <= 2)
            {
              _actor.setTarget(_secondMostHatedAnalysis.character);
              clientStopMoving(null);
              _accessor.doCast(sk);
              _actor.setTarget(_mostHatedAnalysis.character);
              return;
            }
          }
        }
        if (!_mostHatedAnalysis.character.isSleeping())
        {
          for (L2Skill sk : _selfAnalysis.sleepSkills)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
            {
              continue;
            }

            if (Rnd.nextInt(100) <= 1)
            {
              clientStopMoving(null);
              _accessor.doCast(sk);
              _attackTimeout = (300 + GameTimeController.getGameTicks());
              return;
            }
          }
        }
        double secondHatedDist2;
        if ((_secondMostHatedAnalysis.character != null) && (!_secondMostHatedAnalysis.character.isSleeping()))
        {
          secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
          for (L2Skill sk : _selfAnalysis.sleepSkills)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > castRange * castRange))
            {
              continue;
            }

            if (Rnd.nextInt(100) <= 3)
            {
              _actor.setTarget(_secondMostHatedAnalysis.character);
              clientStopMoving(null);
              _accessor.doCast(sk);
              _actor.setTarget(_mostHatedAnalysis.character);
              return;
            }
          }
        }
        if (!_mostHatedAnalysis.character.isRooted())
        {
          for (L2Skill sk : _selfAnalysis.rootSkills)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
            {
              continue;
            }

            if (Rnd.nextInt(100) <= (_mostHatedAnalysis.isSlower ? 3 : 8))
            {
              clientStopMoving(null);
              _accessor.doCast(sk);
              _attackTimeout = (300 + GameTimeController.getGameTicks());
              return;
            }
          }
        }
        if (!_mostHatedAnalysis.character.isAttackingDisabled())
        {
          for (L2Skill sk : _selfAnalysis.generalDisablers)
          {
            int castRange = sk.getCastRange() + combinedCollision;
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
            {
              continue;
            }

            if (Rnd.nextInt(100) <= ((_selfAnalysis.isFighter) && (_actor.isRooted()) ? 15 : 7))
            {
              clientStopMoving(null);
              _accessor.doCast(sk);
              _attackTimeout = (300 + GameTimeController.getGameTicks());
              return;
            }
          }
        }
        if (_actor.getCurrentHp() < _actor.getMaxHp() * 0.4D)
        {
          for (L2Skill sk : _selfAnalysis.healSkills)
          {
            if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)))
            {
              continue;
            }
            int chance = 7;
            if (_mostHatedAnalysis.character.isAttackingDisabled()) chance += 10;
            if ((_secondMostHatedAnalysis.character == null) || (_secondMostHatedAnalysis.character.isAttackingDisabled())) chance += 10;
            if (Rnd.nextInt(100) <= chance)
            {
              _actor.setTarget(_actor);
              clientStopMoving(null);
              _accessor.doCast(sk);
              _actor.setTarget(_mostHatedAnalysis.character);
              return;
            }
          }

        }

        castingChance = 5;
        if (_selfAnalysis.isMage) castingChance = 50;
        if (_selfAnalysis.isBalanced)
        {
          if (!_mostHatedAnalysis.isFighter)
            castingChance = 15;
          else
            castingChance = 25;
        }
        if (_selfAnalysis.isFighter)
        {
          if (_mostHatedAnalysis.isMage)
            castingChance = 3;
          else
            castingChance = 7;
          if (_actor.isRooted())
            castingChance = 20;
        }
        for (L2Skill sk : _selfAnalysis.generalSkills)
        {
          int castRange = sk.getCastRange() + combinedCollision;
          if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
          {
            continue;
          }

          if (Rnd.nextInt(100) <= castingChance)
          {
            clientStopMoving(null);
            _accessor.doCast(sk);
            _attackTimeout = (300 + GameTimeController.getGameTicks());
            return;
          }
        }

      }

      if (_selfAnalysis.isMage)
      {
        if (_actor.isMuted()) return;
        range = _selfAnalysis.maxCastRange;
      }
      if (_mostHatedAnalysis.character.isMoving()) range -= 100; if (range < 5) range = 5;
      moveToPawn(_mostHatedAnalysis.character, range);
      return;
    }

    if (Rnd.nextInt(100) <= 33)
    {
      for (L2Object nearby : _actor.getKnownList().getKnownCharactersInRadius(10L))
      {
        if (((nearby instanceof L2Attackable)) && (nearby != _mostHatedAnalysis.character))
        {
          int diffx = Rnd.get(combinedCollision, combinedCollision + 40);
          if (Rnd.get(10) < 5) diffx = -diffx;
          int diffy = Rnd.get(combinedCollision, combinedCollision + 40);
          if (Rnd.get(10) < 5) diffy = -diffy;
          moveTo(_mostHatedAnalysis.character.getX() + diffx, _mostHatedAnalysis.character.getY() + diffy, _mostHatedAnalysis.character.getZ());

          return;
        }
      }

    }

    _attackTimeout = (300 + GameTimeController.getGameTicks());

    if (!_mostHatedAnalysis.isCanceled)
    {
      for (L2Skill sk : _selfAnalysis.cancelSkills)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
          continue;
        }
        int castRange = sk.getCastRange() + combinedCollision;
        if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
        {
          continue;
        }
        if (Rnd.nextInt(100) <= 8)
        {
          clientStopMoving(null);
          _accessor.doCast(sk);
          _mostHatedAnalysis.isCanceled = true;
          return;
        }
      }
    }
    if (_selfAnalysis.lastDebuffTick + 60 < GameTimeController.getGameTicks())
    {
      for (L2Skill sk : _selfAnalysis.debuffSkills)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
          continue;
        }
        int castRange = sk.getCastRange() + combinedCollision;
        if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
        {
          continue;
        }
        int chance = 5;
        if ((_selfAnalysis.isFighter) && (_mostHatedAnalysis.isMage))
          chance = 3;
        if ((_selfAnalysis.isFighter) && (_mostHatedAnalysis.isArcher))
          chance = 3;
        if ((_selfAnalysis.isMage) && (!_mostHatedAnalysis.isMage))
          chance = 4;
        if (_mostHatedAnalysis.isMagicResistant) chance /= 2;
        if (sk.getCastRange() < 200) chance += 3;
        if (Rnd.nextInt(100) <= chance)
        {
          clientStopMoving(null);
          _accessor.doCast(sk);
          _selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
          return;
        }
      }
    }
    if ((!_mostHatedAnalysis.character.isMuted()) && ((_mostHatedAnalysis.isMage) || (_mostHatedAnalysis.isBalanced)))
    {
      for (L2Skill sk : _selfAnalysis.muteSkills)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
          continue;
        }
        int castRange = sk.getCastRange() + combinedCollision;
        if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
        {
          continue;
        }
        if (Rnd.nextInt(100) <= 7)
        {
          clientStopMoving(null);
          _accessor.doCast(sk);
          return;
        }
      }
    }
    double secondHatedDist2;
    if ((_secondMostHatedAnalysis.character != null) && (!_secondMostHatedAnalysis.character.isMuted()) && ((_secondMostHatedAnalysis.isMage) || (_secondMostHatedAnalysis.isBalanced)))
    {
      secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
      for (L2Skill sk : _selfAnalysis.muteSkills)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
          continue;
        }
        int castRange = sk.getCastRange() + combinedCollision;
        if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > castRange * castRange))
        {
          continue;
        }
        if (Rnd.nextInt(100) <= 3)
        {
          _actor.setTarget(_secondMostHatedAnalysis.character);
          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(_mostHatedAnalysis.character);
          return;
        }
      }
    }
    double secondHatedDist2;
    if ((_secondMostHatedAnalysis.character != null) && (!_secondMostHatedAnalysis.character.isSleeping()))
    {
      secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
      for (L2Skill sk : _selfAnalysis.sleepSkills)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
          continue;
        }
        int castRange = sk.getCastRange() + combinedCollision;
        if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > castRange * castRange))
        {
          continue;
        }
        if (Rnd.nextInt(100) <= 4)
        {
          _actor.setTarget(_secondMostHatedAnalysis.character);
          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(_mostHatedAnalysis.character);
          return;
        }
      }
    }
    if ((!_mostHatedAnalysis.character.isRooted()) && (_mostHatedAnalysis.isFighter) && (!_selfAnalysis.isFighter))
    {
      for (L2Skill sk : _selfAnalysis.rootSkills)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
          continue;
        }
        int castRange = sk.getCastRange() + combinedCollision;
        if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
        {
          continue;
        }
        if (Rnd.nextInt(100) <= 4)
        {
          clientStopMoving(null);
          _accessor.doCast(sk);
          return;
        }
      }
    }
    if (!_mostHatedAnalysis.character.isAttackingDisabled())
    {
      for (L2Skill sk : _selfAnalysis.generalDisablers)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
          continue;
        }
        int castRange = sk.getCastRange() + combinedCollision;
        if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
        {
          continue;
        }
        if (Rnd.nextInt(100) <= (sk.getCastRange() < 200 ? 10 : 7))
        {
          clientStopMoving(null);
          _accessor.doCast(sk);
          return;
        }
      }
    }
    if (_actor.getCurrentHp() < _actor.getMaxHp() * 0.4D)
    {
      for (L2Skill sk : _selfAnalysis.healSkills)
      {
        if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic())) || 
          (_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk))) {
          continue;
        }
        int chance = 7;
        if (_mostHatedAnalysis.character.isAttackingDisabled()) chance += 10;
        if ((_secondMostHatedAnalysis.character == null) || (_secondMostHatedAnalysis.character.isAttackingDisabled())) chance += 10;
        if (Rnd.nextInt(100) <= chance)
        {
          _actor.setTarget(_actor);
          clientStopMoving(null);
          _accessor.doCast(sk);
          _actor.setTarget(_mostHatedAnalysis.character);
          return;
        }
      }
    }
    for (L2Skill sk : _selfAnalysis.generalSkills)
    {
      if (((_actor.isMuted()) && (sk.isMagic())) || ((_actor.isPsychicalMuted()) && (!sk.isMagic()))) {
        continue;
      }
      int castRange = sk.getCastRange() + combinedCollision;
      if ((_actor.isSkillDisabled(sk.getId())) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > castRange * castRange))
      {
        continue;
      }

      int castingChance = 5;
      if (_selfAnalysis.isMage)
      {
        if (sk.getCastRange() < 200) castingChance = 35; else
          castingChance = 25;
      }
      if (_selfAnalysis.isBalanced)
      {
        if (sk.getCastRange() < 200) castingChance = 12;
        else if (_mostHatedAnalysis.isMage)
          castingChance = 2;
        else {
          castingChance = 5;
        }
      }

      if (_selfAnalysis.isFighter)
      {
        if (sk.getCastRange() < 200) castingChance = 12;
        else if (_mostHatedAnalysis.isMage)
          castingChance = 1;
        else {
          castingChance = 3;
        }
      }
      if (Rnd.nextInt(100) <= castingChance)
      {
        clientStopMoving(null);
        _accessor.doCast(sk);
        return;
      }

    }

    clientStopMoving(null);
    _accessor.doAttack(getAttackTarget());
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
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    }
    else if (((L2Attackable)_actor).getMostHated() != getAttackTarget())
    {
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
        if (!_actor.isRunning()) _actor.setRunning();

        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
      }
    }
  }

  protected void onIntentionActive()
  {
    _attackTimeout = 2147483647;
    super.onIntentionActive();
  }

  public void setGlobalAggro(int value)
  {
    _globalAggro = value;
  }
}