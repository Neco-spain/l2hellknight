package l2p.gameserver.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.collections.CollectionUtils;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.math.random.RndSelector;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.AggroList.AggroInfo;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.MinionList;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillTargetType;
import l2p.gameserver.model.World;
import l2p.gameserver.model.WorldRegion;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.instances.MinionInstance;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestEventType;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.taskmanager.AiTaskManager;
import l2p.gameserver.templates.npc.Faction;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAI extends CharacterAI
{
  protected static final Logger _log = LoggerFactory.getLogger(DefaultAI.class);
  public static final int TaskDefaultWeight = 10000;
  protected long AI_TASK_ATTACK_DELAY = Config.AI_TASK_ATTACK_DELAY;
  protected long AI_TASK_ACTIVE_DELAY = Config.AI_TASK_ACTIVE_DELAY;
  protected long AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
  protected int MAX_PURSUE_RANGE;
  protected ScheduledFuture<?> _aiTask;
  protected ScheduledFuture<?> _runningTask;
  protected ScheduledFuture<?> _madnessTask;
  private boolean _thinking = false;

  protected boolean _def_think = false;
  protected long _globalAggro;
  protected long _randomAnimationEnd;
  protected int _pathfindFails;
  protected final NavigableSet<Task> _tasks = new ConcurrentSkipListSet(TaskComparator.getInstance());
  protected final Skill[] _damSkills;
  protected final Skill[] _dotSkills;
  protected final Skill[] _debuffSkills;
  protected final Skill[] _healSkills;
  protected final Skill[] _buffSkills;
  protected final Skill[] _stunSkills;
  protected long _lastActiveCheck;
  protected long _checkAggroTimestamp = 0L;
  protected long _attackTimeout;
  protected long _lastFactionNotifyTime = 0L;
  protected long _minFactionNotifyInterval = 10000L;
  protected final Comparator<Creature> _nearestTargetComparator;

  public void addTaskCast(Creature target, Skill skill)
  {
    Task task = new Task();
    task.type = TaskType.CAST;
    task.target = target.getRef();
    task.skill = skill;
    _tasks.add(task);
    _def_think = true;
  }

  public void addTaskBuff(Creature target, Skill skill)
  {
    Task task = new Task();
    task.type = TaskType.BUFF;
    task.target = target.getRef();
    task.skill = skill;
    _tasks.add(task);
    _def_think = true;
  }

  public void addTaskAttack(Creature target)
  {
    Task task = new Task();
    task.type = TaskType.ATTACK;
    task.target = target.getRef();
    _tasks.add(task);
    _def_think = true;
  }

  public void addTaskAttack(Creature target, Skill skill, int weight)
  {
    Task task = new Task();
    task.type = (skill.isOffensive() ? TaskType.CAST : TaskType.BUFF);
    task.target = target.getRef();
    task.skill = skill;
    task.weight = weight;
    _tasks.add(task);
    _def_think = true;
  }

  public void addTaskMove(Location loc, boolean pathfind)
  {
    Task task = new Task();
    task.type = TaskType.MOVE;
    task.loc = loc;
    task.pathfind = pathfind;
    _tasks.add(task);
    _def_think = true;
  }

  protected void addTaskMove(int locX, int locY, int locZ, boolean pathfind)
  {
    addTaskMove(new Location(locX, locY, locZ), pathfind);
  }

  public DefaultAI(NpcInstance actor)
  {
    super(actor);

    setAttackTimeout(9223372036854775807L);

    NpcInstance npc = getActor();
    _damSkills = npc.getTemplate().getDamageSkills();
    _dotSkills = npc.getTemplate().getDotSkills();
    _debuffSkills = npc.getTemplate().getDebuffSkills();
    _buffSkills = npc.getTemplate().getBuffSkills();
    _stunSkills = npc.getTemplate().getStunSkills();
    _healSkills = npc.getTemplate().getHealSkills();

    _nearestTargetComparator = new NearestTargetComparator(actor);

    MAX_PURSUE_RANGE = actor.getParameter("MaxPursueRange", npc.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : Config.MAX_PURSUE_RANGE);
    _minFactionNotifyInterval = actor.getParameter("FactionNotifyInterval", 10000);
  }

  public void runImpl()
    throws Exception
  {
    if (_aiTask == null) {
      return;
    }
    if ((!isGlobalAI()) && (System.currentTimeMillis() - _lastActiveCheck > 60000L))
    {
      _lastActiveCheck = System.currentTimeMillis();
      NpcInstance actor = getActor();
      WorldRegion region = actor == null ? null : actor.getCurrentRegion();
      if ((region == null) || (!region.isActive()))
      {
        stopAITask();
        return;
      }
    }
    onEvtThink();
  }

  public synchronized void startAITask()
  {
    if (_aiTask == null)
    {
      AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
      _aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
    }
  }

  protected synchronized void switchAITask(long NEW_DELAY)
  {
    if (_aiTask == null) {
      return;
    }
    if (AI_TASK_DELAY_CURRENT != NEW_DELAY)
    {
      _aiTask.cancel(false);
      AI_TASK_DELAY_CURRENT = NEW_DELAY;
      _aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
    }
  }

  public final synchronized void stopAITask()
  {
    if (_aiTask != null)
    {
      _aiTask.cancel(false);
      _aiTask = null;
    }
  }

  protected boolean canSeeInSilentMove(Playable target)
  {
    if (getActor().getParameter("canSeeInSilentMove", false))
      return true;
    return !target.isSilentMoving();
  }

  protected boolean canSeeInHide(Playable target)
  {
    if (getActor().getParameter("canSeeInHide", false)) {
      return true;
    }
    return !target.isInvisible();
  }

  protected boolean checkAggression(Creature target)
  {
    NpcInstance actor = getActor();
    if ((getIntention() != CtrlIntention.AI_INTENTION_ACTIVE) || (!isGlobalAggro()))
      return false;
    if (target.isAlikeDead()) {
      return false;
    }
    if ((target.isNpc()) && (target.isInvul())) {
      return false;
    }
    if (target.isPlayable())
    {
      if (!canSeeInSilentMove((Playable)target))
        return false;
      if (!canSeeInHide((Playable)target))
        return false;
      if ((actor.getFaction().getName().equalsIgnoreCase("varka_silenos_clan")) && (target.getPlayer().getVarka() > 0))
        return false;
      if ((actor.getFaction().getName().equalsIgnoreCase("ketra_orc_clan")) && (target.getPlayer().getKetra() > 0)) {
        return false;
      }

      if ((target.isPlayer()) && (((Player)target).isGM()) && (target.isInvisible()))
        return false;
      if (((Playable)target).getNonAggroTime() > System.currentTimeMillis())
        return false;
      if ((target.isPlayer()) && (!target.getPlayer().isActive()))
        return false;
      if ((actor.isMonster()) && (target.isInZonePeace())) {
        return false;
      }
    }
    AggroList.AggroInfo ai = actor.getAggroList().get(target);
    if ((ai != null) && (ai.hate > 0))
    {
      if (!target.isInRangeZ(actor.getSpawnedLoc(), MAX_PURSUE_RANGE))
        return false;
    }
    else if ((!actor.isAggressive()) || (!target.isInRangeZ(actor.getSpawnedLoc(), actor.getAggroRange()))) {
      return false;
    }
    if (!canAttackCharacter(target))
      return false;
    if (!GeoEngine.canSeeTarget(actor, target, false)) {
      return false;
    }
    actor.getAggroList().addDamageHate(target, 0, 2);

    if ((target.isSummon()) || (target.isPet())) {
      actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
    }
    startRunningTask(AI_TASK_ATTACK_DELAY);
    setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

    return true;
  }

  protected void setIsInRandomAnimation(long time)
  {
    _randomAnimationEnd = (System.currentTimeMillis() + time);
  }

  protected boolean randomAnimation()
  {
    NpcInstance actor = getActor();

    if (actor.getParameter("noRandomAnimation", false)) {
      return false;
    }
    if ((actor.hasRandomAnimation()) && (!actor.isActionsDisabled()) && (!actor.isMoving) && (!actor.isInCombat()) && (Rnd.chance(Config.RND_ANIMATION_RATE)))
    {
      setIsInRandomAnimation(3000L);
      actor.onRandomAnimation();
      return true;
    }
    return false;
  }

  protected boolean randomWalk()
  {
    NpcInstance actor = getActor();

    if (actor.getParameter("noRandomWalk", false)) {
      return false;
    }
    return (!actor.isMoving) && (maybeMoveToHome());
  }

  protected boolean thinkActive()
  {
    NpcInstance actor = getActor();
    if (actor.isActionsDisabled()) {
      return true;
    }
    if (_randomAnimationEnd > System.currentTimeMillis()) {
      return true;
    }
    if (_def_think)
    {
      if (doTask())
        clearTasks();
      return true;
    }

    long now = System.currentTimeMillis();
    boolean aggressive;
    if (now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL)
    {
      _checkAggroTimestamp = now;

      aggressive = Rnd.chance(actor.getParameter("SelfAggressive", actor.isAggressive() ? 100 : 0));
      if ((!actor.getAggroList().isEmpty()) || (aggressive))
      {
        List chars = World.getAroundCharacters(actor);
        CollectionUtils.eqSort(chars, _nearestTargetComparator);
        for (Creature cha : chars)
        {
          if (((aggressive) || (actor.getAggroList().get(cha) != null)) && 
            (checkAggression(cha))) {
            return true;
          }
        }
      }
    }
    if (actor.isMinion())
    {
      MonsterInstance leader = ((MinionInstance)actor).getLeader();
      if (leader != null)
      {
        double distance = actor.getDistance(leader.getX(), leader.getY());
        if (distance > 1000.0D)
          actor.teleToLocation(leader.getMinionPosition());
        else if (distance > 200.0D)
          addTaskMove(leader.getMinionPosition(), false);
        return true;
      }
    }

    if (randomAnimation()) {
      return true;
    }

    return randomWalk();
  }

  protected void onIntentionIdle()
  {
    NpcInstance actor = getActor();

    clearTasks();

    actor.stopMove();
    actor.getAggroList().clear(true);
    setAttackTimeout(9223372036854775807L);
    setAttackTarget(null);

    changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
  }

  protected void onIntentionActive()
  {
    NpcInstance actor = getActor();

    actor.stopMove();
    setAttackTimeout(9223372036854775807L);

    if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
    {
      switchAITask(AI_TASK_ACTIVE_DELAY);
      changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
    }

    onEvtThink();
  }

  protected void onIntentionAttack(Creature target)
  {
    NpcInstance actor = getActor();

    clearTasks();

    actor.stopMove();
    setAttackTarget(target);
    setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
    setGlobalAggro(0L);

    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
    {
      changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
      switchAITask(AI_TASK_ATTACK_DELAY);
    }

    onEvtThink();
  }

  protected boolean canAttackCharacter(Creature target)
  {
    return target.isPlayable();
  }

  protected boolean checkTarget(Creature target, int range)
  {
    NpcInstance actor = getActor();
    if ((target == null) || (target.isAlikeDead()) || (!actor.isInRangeZ(target, range))) {
      return false;
    }

    boolean hided = (target.isPlayable()) && (!canSeeInHide((Playable)target));

    if ((!hided) && (actor.isConfused())) {
      return true;
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
    {
      AggroList.AggroInfo ai = actor.getAggroList().get(target);
      if (ai != null)
      {
        if (hided)
        {
          ai.hate = 0;
          return false;
        }
        return ai.hate > 0;
      }
      return false;
    }

    return canAttackCharacter(target);
  }

  public void setAttackTimeout(long time)
  {
    _attackTimeout = time;
  }

  protected long getAttackTimeout()
  {
    return _attackTimeout;
  }

  protected void thinkAttack()
  {
    NpcInstance actor = getActor();
    if (actor.isDead()) {
      return;
    }
    Location loc = actor.getSpawnedLoc();
    if (!actor.isInRange(loc, MAX_PURSUE_RANGE))
    {
      teleportHome();
      return;
    }

    if ((doTask()) && (!actor.isAttackingNow()) && (!actor.isCastingNow()))
    {
      if (!createNewTask())
      {
        if (System.currentTimeMillis() > getAttackTimeout())
          returnHome();
      }
    }
  }

  protected void onEvtSpawn()
  {
    setGlobalAggro(System.currentTimeMillis() + getActor().getParameter("globalAggro", 10000L));

    setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
  }

  protected void onEvtReadyToAct()
  {
    onEvtThink();
  }

  protected void onEvtArrivedTarget()
  {
    onEvtThink();
  }

  protected void onEvtArrived()
  {
    onEvtThink();
  }

  protected boolean tryMoveToTarget(Creature target)
  {
    return tryMoveToTarget(target, 0);
  }

  protected boolean tryMoveToTarget(Creature target, int range)
  {
    NpcInstance actor = getActor();

    if (!actor.followToCharacter(target, actor.getPhysicalAttackRange(), true)) {
      _pathfindFails += 1;
    }
    if ((_pathfindFails >= getMaxPathfindFails()) && (System.currentTimeMillis() > getAttackTimeout() - getMaxAttackTimeout() + getTeleportTimeout()) && (actor.isInRange(target, MAX_PURSUE_RANGE)))
    {
      _pathfindFails = 0;

      if (target.isPlayable())
      {
        AggroList.AggroInfo hate = actor.getAggroList().get(target);
        if ((hate == null) || (hate.hate < 100))
        {
          returnHome();
          return false;
        }
      }
      Location loc = GeoEngine.moveCheckForAI(target.getLoc(), actor.getLoc(), actor.getGeoIndex());
      if (!GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getGeoIndex()))
        loc = target.getLoc();
      actor.teleToLocation(loc);
    }

    return true;
  }

  protected boolean maybeNextTask(Task currentTask)
  {
    _tasks.remove(currentTask);

    return _tasks.size() == 0;
  }

  protected boolean doTask()
  {
    NpcInstance actor = getActor();

    if (!_def_think) {
      return true;
    }
    Task currentTask = (Task)_tasks.pollFirst();
    if (currentTask == null)
    {
      clearTasks();
      return true;
    }

    if ((actor.isDead()) || (actor.isAttackingNow()) || (actor.isCastingNow())) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$ai$DefaultAI$TaskType[currentTask.type.ordinal()])
    {
    case 1:
      if ((actor.isMovementDisabled()) || (!getIsMobile())) {
        return true;
      }
      if (actor.isInRange(currentTask.loc, 100L)) {
        return maybeNextTask(currentTask);
      }
      if (actor.isMoving) {
        return false;
      }
      if (actor.moveToLocation(currentTask.loc, 0, currentTask.pathfind))
        break;
      clientStopMoving();
      _pathfindFails = 0;
      actor.teleToLocation(currentTask.loc);

      return maybeNextTask(currentTask);
    case 2:
      Creature target = (Creature)currentTask.target.get();

      if (!checkTarget(target, MAX_PURSUE_RANGE)) {
        return true;
      }
      setAttackTarget(target);

      if (actor.isMoving) {
        return Rnd.chance(25);
      }
      if ((actor.getRealDistance3D(target) <= actor.getPhysicalAttackRange() + 40) && (GeoEngine.canSeeTarget(actor, target, false)))
      {
        clientStopMoving();
        _pathfindFails = 0;
        setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
        actor.doAttack(target);
        return maybeNextTask(currentTask);
      }

      if ((actor.isMovementDisabled()) || (!getIsMobile())) {
        return true;
      }
      tryMoveToTarget(target);

      break;
    case 3:
      Creature target = (Creature)currentTask.target.get();

      if ((actor.isMuted(currentTask.skill)) || (actor.isSkillDisabled(currentTask.skill)) || (actor.isUnActiveSkill(currentTask.skill.getId()))) {
        return true;
      }
      boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
      int castRange = currentTask.skill.getAOECastRange();

      if (!checkTarget(target, MAX_PURSUE_RANGE + castRange)) {
        return true;
      }
      setAttackTarget(target);

      if ((actor.getRealDistance3D(target) <= castRange + 60) && (GeoEngine.canSeeTarget(actor, target, false)))
      {
        clientStopMoving();
        _pathfindFails = 0;
        setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
        actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
        return maybeNextTask(currentTask);
      }

      if (actor.isMoving) {
        return Rnd.chance(10);
      }
      if ((actor.isMovementDisabled()) || (!getIsMobile())) {
        return true;
      }
      tryMoveToTarget(target, castRange);

      break;
    case 4:
      Creature target = (Creature)currentTask.target.get();

      if ((actor.isMuted(currentTask.skill)) || (actor.isSkillDisabled(currentTask.skill)) || (actor.isUnActiveSkill(currentTask.skill.getId()))) {
        return true;
      }
      if ((target == null) || (target.isAlikeDead()) || (!actor.isInRange(target, 2000L))) {
        return true;
      }
      boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
      int castRange = currentTask.skill.getAOECastRange();

      if (actor.isMoving) {
        return Rnd.chance(10);
      }
      if ((actor.getRealDistance3D(target) <= castRange + 60) && (GeoEngine.canSeeTarget(actor, target, false)))
      {
        clientStopMoving();
        _pathfindFails = 0;
        actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
        return maybeNextTask(currentTask);
      }

      if ((actor.isMovementDisabled()) || (!getIsMobile())) {
        return true;
      }
      tryMoveToTarget(target);
    }

    return false;
  }

  protected boolean createNewTask()
  {
    return false;
  }

  protected boolean defaultNewTask()
  {
    clearTasks();

    NpcInstance actor = getActor();
    Creature target;
    if ((actor == null) || ((target = prepareTarget()) == null))
      return false;
    Creature target;
    double distance = actor.getDistance(target);
    return chooseTaskAndTargets(null, target, distance);
  }

  protected void onEvtThink()
  {
    NpcInstance actor = getActor();
    if ((_thinking) || (actor == null) || (actor.isActionsDisabled()) || (actor.isAfraid())) {
      return;
    }
    if (_randomAnimationEnd > System.currentTimeMillis()) {
      return;
    }
    if ((actor.isRaid()) && ((actor.isInZonePeace()) || (actor.isInZoneBattle()) || (actor.isInZone(Zone.ZoneType.SIEGE))))
    {
      teleportHome();
      return;
    }

    _thinking = true;
    try
    {
      if ((!Config.BLOCK_ACTIVE_TASKS) && (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE))
        thinkActive();
      else if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
        thinkAttack();
    }
    finally
    {
      _thinking = false;
    }
  }

  protected void onEvtDead(Creature killer)
  {
    NpcInstance actor = getActor();

    int transformer = actor.getParameter("transformOnDead", 0);
    int chance = actor.getParameter("transformChance", 100);
    if ((transformer > 0) && (Rnd.chance(chance)))
    {
      NpcInstance npc = NpcUtils.spawnSingle(transformer, actor.getLoc(), actor.getReflection());

      if ((killer != null) && (killer.isPlayable()))
      {
        npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(100));
        killer.setTarget(npc);
        killer.sendPacket(npc.makeStatusUpdate(new int[] { 9, 10 }));
      }
    }

    super.onEvtDead(killer);
  }

  protected void onEvtClanAttacked(Creature attacked, Creature attacker, int damage)
  {
    if ((getIntention() != CtrlIntention.AI_INTENTION_ACTIVE) || (!isGlobalAggro())) {
      return;
    }
    notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(2));
  }

  protected void onEvtAttacked(Creature attacker, int damage)
  {
    NpcInstance actor = getActor();
    if ((attacker == null) || (actor.isDead())) {
      return;
    }
    int transformer = actor.getParameter("transformOnUnderAttack", 0);
    if (transformer > 0)
    {
      int chance = actor.getParameter("transformChance", 5);
      if ((chance == 100) || ((((MonsterInstance)actor).getChampion() == 0) && (actor.getCurrentHpPercents() > 50.0D) && (Rnd.chance(chance))))
      {
        MonsterInstance npc = (MonsterInstance)NpcHolder.getInstance().getTemplate(transformer).getNewInstance();
        npc.setSpawnedLoc(actor.getLoc());
        npc.setReflection(actor.getReflection());
        npc.setChampion(((MonsterInstance)actor).getChampion());
        npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
        npc.spawnMe(npc.getSpawnedLoc());
        npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
        actor.doDie(actor);
        actor.decayMe();
        attacker.setTarget(npc);
        attacker.sendPacket(npc.makeStatusUpdate(new int[] { 9, 10 }));
        return;
      }
    }

    Player player = attacker.getPlayer();

    if (player != null)
    {
      if (((SevenSigns.getInstance().isSealValidationPeriod()) || (SevenSigns.getInstance().isCompResultsPeriod())) && (actor.isSevenSignsMonster()))
      {
        int pcabal = SevenSigns.getInstance().getPlayerCabal(player);
        int wcabal = SevenSigns.getInstance().getCabalHighestScore();
        if ((pcabal != wcabal) && (wcabal != 0))
        {
          player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
          player.teleToClosestTown();
          return;
        }
      }

      List quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
      if (quests != null) {
        for (QuestState qs : quests) {
          qs.getQuest().notifyAttack(actor, qs);
        }
      }
    }
    actor.getAggroList().addDamageHate(attacker, 0, damage);

    if ((damage > 0) && ((attacker.isSummon()) || (attacker.isPet()))) {
      actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? damage : 1);
    }
    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
    {
      if (!actor.isRunning())
        startRunningTask(AI_TASK_ATTACK_DELAY);
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    }

    notifyFriends(attacker, damage);
  }

  protected void onEvtAggression(Creature attacker, int aggro)
  {
    NpcInstance actor = getActor();
    if ((attacker == null) || (actor.isDead())) {
      return;
    }
    actor.getAggroList().addDamageHate(attacker, 0, aggro);

    if ((aggro > 0) && ((attacker.isSummon()) || (attacker.isPet()))) {
      actor.getAggroList().addDamageHate(attacker.getPlayer(), 0, actor.getParameter("searchingMaster", false) ? aggro : 1);
    }
    if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
    {
      if (!actor.isRunning())
        startRunningTask(AI_TASK_ATTACK_DELAY);
      setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    }
  }

  protected boolean maybeMoveToHome()
  {
    NpcInstance actor = getActor();
    if (actor.isDead()) {
      return false;
    }
    boolean randomWalk = actor.hasRandomWalk();
    Location sloc = actor.getSpawnedLoc();

    if ((randomWalk) && ((!Config.RND_WALK) || (!Rnd.chance(Config.RND_WALK_RATE)))) {
      return false;
    }
    boolean isInRange = actor.isInRangeZ(sloc, Config.MAX_DRIFT_RANGE);

    if ((!randomWalk) && (isInRange)) {
      return false;
    }
    Location pos = Location.findPointToStay(actor, sloc, 0, Config.MAX_DRIFT_RANGE);

    actor.setWalking();

    if ((!actor.moveToLocation(pos.x, pos.y, pos.z, 0, true)) && (!isInRange)) {
      teleportHome();
    }
    return true;
  }

  protected void returnHome()
  {
    returnHome(true, Config.ALWAYS_TELEPORT_HOME);
  }

  protected void teleportHome()
  {
    returnHome(true, true);
  }

  protected void returnHome(boolean clearAggro, boolean teleport)
  {
    NpcInstance actor = getActor();
    Location sloc = actor.getSpawnedLoc();

    clearTasks();
    actor.stopMove();

    if (clearAggro) {
      actor.getAggroList().clear(true);
    }
    setAttackTimeout(9223372036854775807L);
    setAttackTarget(null);

    changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

    if (teleport)
    {
      actor.broadcastPacketToOthers(new L2GameServerPacket[] { new MagicSkillUse(actor, actor, 2036, 1, 500, 0L) });
      actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getHeight(sloc, actor.getGeoIndex()));
    }
    else
    {
      if (!clearAggro)
        actor.setRunning();
      else {
        actor.setWalking();
      }
      addTaskMove(sloc, false);
    }
  }

  protected Creature prepareTarget()
  {
    NpcInstance actor = getActor();

    if (actor.isConfused()) {
      return getAttackTarget();
    }

    if (Rnd.chance(actor.getParameter("isMadness", 0)))
    {
      Creature randomHated = actor.getAggroList().getRandomHated();
      if (randomHated != null)
      {
        setAttackTarget(randomHated);
        if ((_madnessTask == null) && (!actor.isConfused()))
        {
          actor.startConfused();
          _madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 10000L);
        }
        return randomHated;
      }

    }

    List hateList = actor.getAggroList().getHateList();
    Creature hated = null;
    for (Creature cha : hateList)
    {
      if (!checkTarget(cha, MAX_PURSUE_RANGE))
      {
        actor.getAggroList().remove(cha, true);
        continue;
      }
      hated = cha;
    }

    if (hated != null)
    {
      setAttackTarget(hated);
      return hated;
    }

    return null;
  }

  protected boolean canUseSkill(Skill skill, Creature target, double distance)
  {
    NpcInstance actor = getActor();
    if ((skill == null) || (skill.isNotUsedByAI())) {
      return false;
    }
    if ((skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF) && (target != actor)) {
      return false;
    }
    int castRange = skill.getAOECastRange();
    if ((castRange <= 200) && (distance > 200.0D)) {
      return false;
    }
    if ((actor.isSkillDisabled(skill)) || (actor.isMuted(skill)) || (actor.isUnActiveSkill(skill.getId()))) {
      return false;
    }
    double mpConsume2 = skill.getMpConsume2();
    if (skill.isMagic())
      mpConsume2 = actor.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, skill);
    else
      mpConsume2 = actor.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, skill);
    if (actor.getCurrentMp() < mpConsume2) {
      return false;
    }

    return target.getEffectList().getEffectsCountForSkill(skill.getId()) == 0;
  }

  protected boolean canUseSkill(Skill sk, Creature target)
  {
    return canUseSkill(sk, target, 0.0D);
  }

  protected Skill[] selectUsableSkills(Creature target, double distance, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0) || (target == null)) {
      return null;
    }
    Skill[] ret = null;
    int usable = 0;

    for (Skill skill : skills) {
      if (!canUseSkill(skill, target, distance))
        continue;
      if (ret == null)
        ret = new Skill[skills.length];
      ret[(usable++)] = skill;
    }

    if ((ret == null) || (usable == skills.length)) {
      return ret;
    }
    if (usable == 0) {
      return null;
    }
    ret = (Skill[])Arrays.copyOf(ret, usable);
    return ret;
  }

  protected static Skill selectTopSkillByDamage(Creature actor, Creature target, double distance, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0)) {
      return null;
    }
    if (skills.length == 1) {
      return skills[0];
    }
    RndSelector rnd = new RndSelector(skills.length);

    for (Skill skill : skills)
    {
      double weight = skill.getSimpleDamage(actor, target) * skill.getAOECastRange() / distance;
      if (weight < 1.0D)
        weight = 1.0D;
      rnd.add(skill, (int)weight);
    }
    return (Skill)rnd.select();
  }

  protected static Skill selectTopSkillByDebuff(Creature actor, Creature target, double distance, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0)) {
      return null;
    }
    if (skills.length == 1) {
      return skills[0];
    }
    RndSelector rnd = new RndSelector(skills.length);

    for (Skill skill : skills)
    {
      if (skill.getSameByStackType(target) != null)
        continue;
      double weight;
      if ((weight = 100.0D * skill.getAOECastRange() / distance) <= 0.0D)
        weight = 1.0D;
      rnd.add(skill, (int)weight);
    }
    return (Skill)rnd.select();
  }

  protected static Skill selectTopSkillByBuff(Creature target, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0)) {
      return null;
    }
    if (skills.length == 1) {
      return skills[0];
    }
    RndSelector rnd = new RndSelector(skills.length);

    for (Skill skill : skills)
    {
      if (skill.getSameByStackType(target) != null)
        continue;
      double weight;
      if ((weight = skill.getPower()) <= 0.0D)
        weight = 1.0D;
      rnd.add(skill, (int)weight);
    }
    return (Skill)rnd.select();
  }

  protected static Skill selectTopSkillByHeal(Creature target, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0)) {
      return null;
    }
    double hpReduced = target.getMaxHp() - target.getCurrentHp();
    if (hpReduced < 1.0D) {
      return null;
    }
    if (skills.length == 1) {
      return skills[0];
    }
    RndSelector rnd = new RndSelector(skills.length);

    for (Skill skill : skills)
    {
      double weight;
      if ((weight = Math.abs(skill.getPower() - hpReduced)) <= 0.0D)
        weight = 1.0D;
      rnd.add(skill, (int)weight);
    }
    return (Skill)rnd.select();
  }

  protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0) || (target == null))
      return;
    for (Skill sk : skills)
      addDesiredSkill(skillMap, target, distance, sk);
  }

  protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill skill)
  {
    if ((skill == null) || (target == null) || (!canUseSkill(skill, target)))
      return;
    int weight = (int)(-Math.abs(skill.getAOECastRange() - distance));
    if (skill.getAOECastRange() >= distance)
      weight += 1000000;
    else if ((skill.isNotTargetAoE()) && (skill.getTargets(getActor(), target, false).size() == 0))
      return;
    skillMap.put(skill, Integer.valueOf(weight));
  }

  protected void addDesiredHeal(Map<Skill, Integer> skillMap, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0))
      return;
    NpcInstance actor = getActor();
    double hpReduced = actor.getMaxHp() - actor.getCurrentHp();
    double hpPercent = actor.getCurrentHpPercents();
    if (hpReduced < 1.0D) {
      return;
    }
    for (Skill sk : skills) {
      if ((!canUseSkill(sk, actor)) || (sk.getPower() > hpReduced))
        continue;
      int weight = (int)sk.getPower();
      if (hpPercent < 50.0D)
        weight += 1000000;
      skillMap.put(sk, Integer.valueOf(weight));
    }
  }

  protected void addDesiredBuff(Map<Skill, Integer> skillMap, Skill[] skills)
  {
    if ((skills == null) || (skills.length == 0))
      return;
    NpcInstance actor = getActor();
    for (Skill sk : skills)
      if (canUseSkill(sk, actor))
        skillMap.put(sk, Integer.valueOf(1000000));
  }

  protected Skill selectTopSkill(Map<Skill, Integer> skillMap)
  {
    if ((skillMap == null) || (skillMap.isEmpty()))
      return null;
    int topWeight = -2147483648;
    for (Skill next : skillMap.keySet())
    {
      int nWeight;
      if ((nWeight = ((Integer)skillMap.get(next)).intValue()) > topWeight)
        topWeight = nWeight; 
    }
    if (topWeight == -2147483648) {
      return null;
    }
    Skill[] skills = new Skill[skillMap.size()];
    int nWeight = 0;
    for (Map.Entry e : skillMap.entrySet())
    {
      if (((Integer)e.getValue()).intValue() < topWeight)
        continue;
      skills[(nWeight++)] = ((Skill)e.getKey());
    }
    return skills[Rnd.get(nWeight)];
  }

  protected boolean chooseTaskAndTargets(Skill skill, Creature target, double distance)
  {
    NpcInstance actor = getActor();

    if (skill != null)
    {
      if ((actor.isMovementDisabled()) && (distance > skill.getAOECastRange() + 60))
      {
        target = null;
        if (skill.isOffensive())
        {
          LazyArrayList targets = LazyArrayList.newInstance();
          for (Creature cha : actor.getAggroList().getHateList())
          {
            if ((!checkTarget(cha, skill.getAOECastRange() + 60)) || (!canUseSkill(skill, cha)))
              continue;
            targets.add(cha);
          }
          if (!targets.isEmpty())
            target = (Creature)targets.get(Rnd.get(targets.size()));
          LazyArrayList.recycle(targets);
        }
      }

      if (target == null) {
        return false;
      }

      if (skill.isOffensive())
        addTaskCast(target, skill);
      else
        addTaskBuff(target, skill);
      return true;
    }

    if ((actor.isMovementDisabled()) && (distance > actor.getPhysicalAttackRange() + 40))
    {
      target = null;
      LazyArrayList targets = LazyArrayList.newInstance();
      for (Creature cha : actor.getAggroList().getHateList())
      {
        if (!checkTarget(cha, actor.getPhysicalAttackRange() + 40))
          continue;
        targets.add(cha);
      }
      if (!targets.isEmpty())
        target = (Creature)targets.get(Rnd.get(targets.size()));
      LazyArrayList.recycle(targets);
    }

    if (target == null) {
      return false;
    }

    addTaskAttack(target);
    return true;
  }

  public boolean isActive()
  {
    return _aiTask != null;
  }

  protected void clearTasks()
  {
    _def_think = false;
    _tasks.clear();
  }

  protected void startRunningTask(long interval)
  {
    NpcInstance actor = getActor();
    if ((actor != null) && (_runningTask == null) && (!actor.isRunning()))
      _runningTask = ThreadPoolManager.getInstance().schedule(new RunningTask(), interval);
  }

  protected boolean isGlobalAggro()
  {
    if (_globalAggro == 0L)
      return true;
    if (_globalAggro <= System.currentTimeMillis())
    {
      _globalAggro = 0L;
      return true;
    }
    return false;
  }

  public void setGlobalAggro(long value)
  {
    _globalAggro = value;
  }

  public NpcInstance getActor()
  {
    return (NpcInstance)super.getActor();
  }

  protected boolean defaultThinkBuff(int rateSelf)
  {
    return defaultThinkBuff(rateSelf, 0);
  }

  protected void notifyFriends(Creature attacker, int damage)
  {
    NpcInstance actor = getActor();
    if (System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
    {
      _lastFactionNotifyTime = System.currentTimeMillis();
      if (actor.isMinion())
      {
        MonsterInstance master = ((MinionInstance)actor).getLeader();
        if (master != null)
        {
          if ((!master.isDead()) && (master.isVisible())) {
            master.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(damage));
          }

          MinionList minionList = master.getMinionList();
          if (minionList != null) {
            for (MinionInstance minion : minionList.getAliveMinions()) {
              if (minion != actor)
                minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(damage));
            }
          }
        }
      }
      MinionList minionList = actor.getMinionList();
      if ((minionList != null) && (minionList.hasAliveMinions())) {
        for (MinionInstance minion : minionList.getAliveMinions()) {
          minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(damage));
        }
      }
      for (NpcInstance npc : activeFactionTargets())
        npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, new Object[] { actor, attacker, Integer.valueOf(damage) });
    }
  }

  protected List<NpcInstance> activeFactionTargets()
  {
    NpcInstance actor = getActor();
    if (actor.getFaction().isNone())
      return Collections.emptyList();
    List npcFriends = new LazyArrayList();
    for (NpcInstance npc : World.getAroundNpc(actor))
      if ((!npc.isDead()) && 
        (npc.isInFaction(actor)) && 
        (npc.isInRangeZ(actor, npc.getFaction().getRange())) && 
        (GeoEngine.canSeeTarget(npc, actor, false)))
        npcFriends.add(npc);
    return npcFriends;
  }

  protected boolean defaultThinkBuff(int rateSelf, int rateFriends)
  {
    NpcInstance actor = getActor();
    if (actor.isDead()) {
      return true;
    }

    if (Rnd.chance(rateSelf))
    {
      double actorHp = actor.getCurrentHpPercents();

      Skill[] skills = actorHp < 50.0D ? selectUsableSkills(actor, 0.0D, _healSkills) : selectUsableSkills(actor, 0.0D, _buffSkills);
      if ((skills == null) || (skills.length == 0)) {
        return false;
      }
      Skill skill = skills[Rnd.get(skills.length)];
      addTaskBuff(actor, skill);
      return true;
    }

    if (Rnd.chance(rateFriends))
    {
      for (NpcInstance npc : activeFactionTargets())
      {
        double targetHp = npc.getCurrentHpPercents();

        Skill[] skills = targetHp < 50.0D ? selectUsableSkills(actor, 0.0D, _healSkills) : selectUsableSkills(actor, 0.0D, _buffSkills);
        if ((skills == null) || (skills.length == 0)) {
          continue;
        }
        Skill skill = skills[Rnd.get(skills.length)];
        addTaskBuff(actor, skill);
        return true;
      }
    }

    return false;
  }

  protected boolean defaultFightTask()
  {
    clearTasks();

    NpcInstance actor = getActor();
    if ((actor.isDead()) || (actor.isAMuted()))
      return false;
    Creature target;
    if ((target = prepareTarget()) == null) {
      return false;
    }
    double distance = actor.getDistance(target);
    double targetHp = target.getCurrentHpPercents();
    double actorHp = actor.getCurrentHpPercents();

    Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
    Skill[] dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
    Skill[] debuff = targetHp > 10.0D ? null : Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null;
    Skill[] stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
    Skill[] heal = actorHp < 50.0D ? null : Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0.0D, _healSkills) : null;
    Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0.0D, _buffSkills) : null;

    RndSelector rnd = new RndSelector();
    if (!actor.isAMuted())
      rnd.add(null, getRatePHYS());
    rnd.add(dam, getRateDAM());
    rnd.add(dot, getRateDOT());
    rnd.add(debuff, getRateDEBUFF());
    rnd.add(heal, getRateHEAL());
    rnd.add(buff, getRateBUFF());
    rnd.add(stun, getRateSTUN());

    Skill[] selected = (Skill[])rnd.select();
    if (selected != null)
    {
      if ((selected == dam) || (selected == dot)) {
        return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);
      }
      if ((selected == debuff) || (selected == stun)) {
        return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);
      }
      if (selected == buff) {
        return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);
      }
      if (selected == heal) {
        return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
      }

    }

    return chooseTaskAndTargets(null, target, distance);
  }

  public int getRatePHYS()
  {
    return 100;
  }

  public int getRateDOT()
  {
    return 0;
  }

  public int getRateDEBUFF()
  {
    return 0;
  }

  public int getRateDAM()
  {
    return 0;
  }

  public int getRateSTUN()
  {
    return 0;
  }

  public int getRateBUFF()
  {
    return 0;
  }

  public int getRateHEAL()
  {
    return 0;
  }

  public boolean getIsMobile()
  {
    return !getActor().getParameter("isImmobilized", false);
  }

  public int getMaxPathfindFails()
  {
    return 3;
  }

  public int getMaxAttackTimeout()
  {
    return 15000;
  }

  public int getTeleportTimeout()
  {
    return 10000;
  }

  protected class NearestTargetComparator
    implements Comparator<Creature>
  {
    private final Creature actor;

    public NearestTargetComparator(Creature actor)
    {
      this.actor = actor;
    }

    public int compare(Creature o1, Creature o2)
    {
      double diff = actor.getDistance3D(o1) - actor.getDistance3D(o2);
      if (diff < 0.0D)
        return -1;
      return diff > 0.0D ? 1 : 0;
    }
  }

  protected class MadnessTask extends RunnableImpl
  {
    protected MadnessTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      NpcInstance actor = getActor();
      if (actor != null)
        actor.stopConfused();
      _madnessTask = null;
    }
  }

  protected class RunningTask extends RunnableImpl
  {
    protected RunningTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      NpcInstance actor = getActor();
      if (actor != null)
        actor.setRunning();
      _runningTask = null;
    }
  }

  protected class Teleport extends RunnableImpl
  {
    Location _destination;

    public Teleport(Location destination)
    {
      _destination = destination;
    }

    public void runImpl()
      throws Exception
    {
      NpcInstance actor = getActor();
      if (actor != null)
        actor.teleToLocation(_destination);
    }
  }

  private static class TaskComparator
    implements Comparator<DefaultAI.Task>
  {
    private static final Comparator<DefaultAI.Task> instance = new TaskComparator();

    public static final Comparator<DefaultAI.Task> getInstance()
    {
      return instance;
    }

    public int compare(DefaultAI.Task o1, DefaultAI.Task o2)
    {
      if ((o1 == null) || (o2 == null))
        return 0;
      return o2.weight - o1.weight;
    }
  }

  public static class Task
  {
    public DefaultAI.TaskType type;
    public Skill skill;
    public HardReference<? extends Creature> target;
    public Location loc;
    public boolean pathfind;
    public int weight = 10000;
  }

  public static enum TaskType
  {
    MOVE, 
    ATTACK, 
    CAST, 
    BUFF;
  }
}