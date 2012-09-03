package l2rt.gameserver.ai;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Character.HateInfo;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.model.instances.L2NpcInstance.AggroInfo;
import l2rt.gameserver.model.quest.QuestEventType;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.StatusUpdate;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.TerritoryTable;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.*;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class DefaultAI extends L2CharacterAI implements Runnable
{
	protected static Logger _log = Logger.getLogger(DefaultAI.class.getName());

	public static enum TaskType
	{
		MOVE,
		ATTACK,
		CAST,
		BUFF
	}

	public static class Task
	{
		public TaskType type;
		public L2Skill skill;
		public long targetStoreId;
		public Location loc;
		public boolean pathfind;
		public int weight = TaskDefaultWeight;
	}

	public void addTaskCast(L2Character target, L2Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.CAST;
		task.targetStoreId = target == null ? 0 : target.getStoredId();
		task.skill = skill;
		_task_list.add(task);
		_def_think = true;
	}

	public void addTaskBuff(L2Character target, L2Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.BUFF;
		task.targetStoreId = target == null ? 0 : target.getStoredId();
		task.skill = skill;
		_task_list.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskAttack(L2Character target)
	{
		Task task = new Task();
		task.type = TaskType.ATTACK;
		task.targetStoreId = target == null ? 0 : target.getStoredId();
		_task_list.add(task);
		_def_think = true;
	}

	@Override
	public void addTaskMove(Location loc, boolean pathfind)
	{
		Task task = new Task();
		task.type = TaskType.MOVE;
		task.loc = loc;
		_task_list.add(task);
		_def_think = true;
	}

	public void addTaskMove(int locX, int locY, int locZ, boolean pathfind)
	{
		addTaskMove(new Location(locX, locY, locZ), pathfind);
	}

	private static class TaskComparator implements Comparator<Task>
	{
		public int compare(Task o1, Task o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o2.weight - o1.weight;
		}
	}

	private static final int TaskDefaultWeight = 10000;
	private static final TaskComparator task_comparator = new TaskComparator();

	public class Teleport implements Runnable
	{
		Location _destination;

		public Teleport(Location destination)
		{
			_destination = destination;
		}

		@Override
		public void run()
		{
			L2NpcInstance actor = getActor();
			if(actor != null)
				actor.teleToLocation(_destination);
		}
	}

	public class RunningTask implements Runnable
	{
		@Override
		public void run()
		{
			L2NpcInstance actor = getActor();
			if(actor != null)
				actor.setRunning();
			_runningTask = null;
		}
	}

	public class MadnessTask implements Runnable
	{
		@Override
		public void run()
		{
			L2NpcInstance actor = getActor();
			if(actor != null)
				actor.stopConfused();
			_madnessTask = null;
		}
	}

	protected StatsSet this_params = null;
	protected int AI_TASK_DELAY = Config.AI_TASK_DELAY;
	protected int AI_TASK_ACTIVE_DELAY = Config.AI_TASK_ACTIVE_DELAY;
	protected int MAX_PURSUE_RANGE;
	protected int MAX_Z_AGGRO_RANGE = 200;

	/** The L2NpcInstance AI task executed every 1s (call onEvtThink method)*/
	protected Future<?> _aiTask;

	protected ScheduledFuture<?> _runningTask;
	protected ScheduledFuture<?> _madnessTask;

	/** The flag used to indicate that a thinking action is in progress */
	private boolean _thinking = false;

	/** The L2NpcInstance aggro counter */
	protected long _globalAggro;

	protected long _randomAnimationEnd;
	protected int _pathfind_fails;

	/** Список заданий */
	protected ConcurrentSkipListSet<Task> _task_list = new ConcurrentSkipListSet<Task>(task_comparator);

	/** Показывает, есть ли задания */
	protected boolean _def_think = false;

	public final L2Skill[] _dam_skills, _dot_skills, _debuff_skills, _heal, _buff, _stun;

	private long _lastActiveCheck;

	public DefaultAI(L2Character actor)
	{
		super(actor);

		setGlobalAggro(System.currentTimeMillis() + 10000); // 10 seconds timeout of ATTACK after respawn
		L2NpcInstance thisActor = (L2NpcInstance) actor;
		thisActor.setAttackTimeout(Long.MAX_VALUE);

		_dam_skills = thisActor.getTemplate().getDamageSkills();
		_dot_skills = thisActor.getTemplate().getDotSkills();
		_debuff_skills = thisActor.getTemplate().getDebuffSkills();
		_buff = thisActor.getTemplate().getBuffSkills();
		_stun = thisActor.getTemplate().getStunSkills();
		_heal = thisActor.getTemplate().getHealSkills();

		// Preload some AI params
		setMaxPursueRange(getInt("MaxPursueRange", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : thisActor.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : Config.MAX_PURSUE_RANGE));
		thisActor.minFactionNotifyInterval = getInt("FactionNotifyInterval", thisActor.minFactionNotifyInterval);
	}

	@Override
	public void run()
	{
		if(_aiTask == null)
			return;
		if(!isGlobalAI() && System.currentTimeMillis() - _lastActiveCheck > 60000)
		{
			_lastActiveCheck = System.currentTimeMillis();
			L2NpcInstance actor = getActor();
			L2WorldRegion region = actor == null ? null : actor.getCurrentRegion();
			if(region == null || region.areNeighborsEmpty())
			{
				if(_aiTask == null)
					return;
				_aiTask.cancel(true);
				return;
			}
		}
		onEvtThink();
	}

	@Override
	public void startAITask()
	{
		if(_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, Rnd.get(AI_TASK_ACTIVE_DELAY), AI_TASK_ACTIVE_DELAY, false);
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public void stopAITask()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
			setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	public void setAiDelay(int delay)
	{
		AI_TASK_DELAY = delay;
	}

	/**
	 * Определяет, может ли этот тип АИ видеть персонажей в режиме Silent Move.
	 * @param target L2Playable цель
	 * @return true если цель видна в режиме Silent Move
	 */
	@Override
	public boolean canSeeInSilentMove(L2Playable target)
	{
		if(getBool("canSeeInSilentMove", false))
			return true;
		return !target.isSilentMoving();
	}


	@Override
	public void checkAggression(L2Character target)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
			return;
		if(!actor.isAggressive() || actor.getDistance(target) > actor.getAggroRange() || Math.abs(actor.getZ() - target.getZ()) > MAX_Z_AGGRO_RANGE)
			return;
		if(target.isPlayable() && !canSeeInSilentMove((L2Playable) target))
			return;
		if(actor.getFactionId().equalsIgnoreCase("varka_silenos_clan") && target.getPlayer() != null && target.getPlayer().getVarka() > 0)
			return;
		if(actor.getFactionId().equalsIgnoreCase("ketra_orc_clan") && target.getPlayer() != null && target.getPlayer().getKetra() > 0)
			return;
		if(target.isInZonePeace())
			return;
		if(target.isFollow && !target.isPlayer() && target.getFollowTarget() != null && target.getFollowTarget().isPlayer())
			return;
		if(target.isPlayer() && ((L2Player) target).isInvisible())
			return;

		if(!actor.canAttackCharacter(target))
			return;
		if(!GeoEngine.canSeeTarget(actor, target, false))
			return;

		target.addDamageHate(actor, 0, 2);

		if((target.isSummon() || target.isPet()) && target.getPlayer() != null)
			target.getPlayer().addDamageHate(actor, 0, 1);

		startRunningTask(2000);
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	public void setIsInRandomAnimation(long time)
	{
		_randomAnimationEnd = System.currentTimeMillis() + time;
	}

	protected boolean randomAnimation()
	{
		L2NpcInstance actor = getActor();
		if(actor != null && !actor.isMoving && actor.hasRandomAnimation() && Rnd.chance(Config.RND_ANIMATION_RATE))
		{
			setIsInRandomAnimation(3000);
			actor.onRandomAnimation();
			return true;
		}
		return false;
	}

	protected boolean randomWalk()
	{
		if(getBool("noRandomWalk", false))
			return false;
		L2Character actor = getActor();
		return actor != null && !actor.isMoving && maybeMoveToHome();
	}

	/**
	 * @return true если действие выполнено, false если нет
	 */
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		if(actor.isBlocked() || _randomAnimationEnd > System.currentTimeMillis())
			return true;

		if(_def_think)
		{
			if(doTask())
				clearTasks();
			return true;
		}

		// Аггрится даже на неподвижных игроков
		if(actor.isAggressive() && Rnd.chance(getInt("SelfAggressive", 1)))
			for(L2Playable obj : L2World.getAroundPlayables(actor))
				if(obj != null && !obj.isAlikeDead() && !obj.isInvul() && obj.isVisible())
					checkAggression(obj);

		// If this is a festival monster or chest, then it remains in the same location
		if(actor instanceof L2ChestInstance || actor instanceof L2TamedBeastInstance)
			return false;

		if(actor.isMinion())
		{
			L2MonsterInstance leader = ((L2MinionInstance) actor).getLeader();
			if(leader == null)
				return false;
			double distance = actor.getDistance(leader.getX(), leader.getY());
			if(distance > 1000)
				actor.teleToLocation(leader.getMinionPosition());
			else if(distance > 200)
				addTaskMove(leader.getMinionPosition(), false);
			return false;
		}

		if(randomAnimation())
			return true;

		if(randomWalk())
			return true;

		return false;
	}

	@Override
	protected void onIntentionActive()
	{
		L2NpcInstance actor = getActor();
		if(actor != null)
			actor.setAttackTimeout(Long.MAX_VALUE);

		clientStopMoving();

		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			if(_aiTask != null)
				_aiTask.cancel(false);
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 0, AI_TASK_ACTIVE_DELAY, false);
		}
	}

	protected boolean checkTarget(L2Character target, boolean canSelf, int range)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || target == null || target == actor && !canSelf || target.isAlikeDead() || !actor.isInRange(target, range))
			return true;

		if(actor.isConfused() || target instanceof L2DecoyInstance)
			return false;

		if(!canSelf && target.getHateList().get(actor) == null)
			return true;

		/**
		if(actor.getAttackTimeout() < System.currentTimeMillis())
		{
			if(actor.isRunning() && actor.getAggroListSize() == 1)
			{
				actor.setWalking();
				actor.setAttackTimeout(MAX_ATTACK_TIMEOUT / 4 + System.currentTimeMillis());
				return false;
			}
			target.removeFromHatelist(actor);
			return true;
		}
		 */

		return false;
	}

	protected void thinkAttack()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		Location loc = actor.getSpawnedLoc();
		if(loc.x != 0 && loc.y != 0 && !actor.isInRange(loc, getMaxPursueRange()))
		{
			teleportHome(true);
			return;
		}

		if(doTask() && !actor.isAttackingNow() && !actor.isCastingNow())
			createNewTask();
	}

	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrived()
	{
		onEvtThink();
	}

	protected boolean tryMoveToTarget(L2Character target)
	{
		return tryMoveToTarget(target, 0);
	}

	protected boolean tryMoveToTarget(L2Character target, int range)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return false;
		range = range == 0 ? actor.getPhysicalAttackRange() : Math.max(0, range);
		if(!actor.followToCharacter(target, actor.getPhysicalAttackRange(), true))
			_pathfind_fails++;

		if(_pathfind_fails >= getMaxPathfindFails() && System.currentTimeMillis() - (actor.getAttackTimeout() - getMaxAttackTimeout()) < getTeleportTimeout() && actor.isInRange(target, 2000))
		{
			_pathfind_fails = 0;
			HateInfo hate = target.getHateList().get(actor);
			if(hate == null || hate.damage < 100 && hate.hate < 100)
			{
				returnHome(true);
				return false;
			}
			Location loc = GeoEngine.moveCheckForAI(target.getLoc(), actor.getLoc(), actor.getReflection().getGeoIndex());
			if(!GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getReflection().getGeoIndex())) // Для подстраховки
				loc = target.getLoc();
			actor.teleToLocation(loc);
			//actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 600000));
			//ThreadPoolManager.getInstance().scheduleAi(new Teleport(GeoEngine.moveCheckForAI(target.getLoc(), actor.getLoc(), actor.getReflection().getGeoIndex())), 500, false);
			return false;
		}

		return true;
	}

	protected boolean maybeNextTask(Task currentTask)
	{
		// Следующее задание
		_task_list.remove(currentTask);
		// Если заданий больше нет - определить новое
		if(_task_list.size() == 0)
			return true;
		return false;
	}

	protected boolean doTask()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return false;

		if(_task_list.size() == 0)
		{
			clearTasks();
			return true;
		}

		Task currentTask = null;
		try
		{
			currentTask = _task_list.first();
		}
		catch(Exception e)
		{}

		if(currentTask == null)
			clearTasks();

		if(!_def_think)
			return true;

		assert currentTask != null;
		L2Character temp_attack_target = L2ObjectsStorage.getAsCharacter(currentTask.targetStoreId);

		if(actor.isAttackingNow() || actor.isCastingNow())
			return false;

		switch(currentTask.type)
		{
			// Задание "прибежать в заданные координаты"
			case MOVE:
			{
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				if(actor.isInRange(currentTask.loc, 100))
					return maybeNextTask(currentTask);

				if(actor.isMoving)
					return false;

				if(!actor.moveToLocation(currentTask.loc, 0, currentTask.pathfind))
				{
					clientStopMoving();
					_pathfind_fails = 0;
					actor.teleToLocation(currentTask.loc);
					//actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 600000));
					//ThreadPoolManager.getInstance().scheduleAi(new Teleport(currentTask.loc), 500, false);
					return maybeNextTask(currentTask);
				}
			}
				break;
			// Задание "добежать - ударить"
			case ATTACK:
			{
				if(checkTarget(temp_attack_target, false, 2000))
					return true;

				setAttackTarget(temp_attack_target);

				if(actor.isMoving)
					return Rnd.chance(25);

				if(actor.getRealDistance(temp_attack_target) <= actor.getPhysicalAttackRange() + 40 && GeoEngine.canSeeTarget(actor, temp_attack_target, false))
				{
					clientStopMoving();
					_pathfind_fails = 0;
					actor.setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doAttack(temp_attack_target);
					return maybeNextTask(currentTask);
				}

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(temp_attack_target);
			}
				break;
			// Задание "добежать - атаковать скиллом"
			case CAST:
			{
				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill.getId()))
					return true;

				boolean isAoE = currentTask.skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA;

				if(checkTarget(temp_attack_target, false, 3000))
					return true;

				setAttackTarget(temp_attack_target);

				int castRange = currentTask.skill.getAOECastRange();

				if(actor.getRealDistance(temp_attack_target) <= castRange + 60 && GeoEngine.canSeeTarget(actor, temp_attack_target, false))
				{
					clientStopMoving();
					_pathfind_fails = 0;
					actor.setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doCast(currentTask.skill, isAoE ? actor : temp_attack_target, !temp_attack_target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if(actor.isMoving)
					return Rnd.chance(10);

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(temp_attack_target, castRange);
			}
				break;
			// Задание "добежать - применить скилл"
			case BUFF:
			{
				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill.getId()))
					return true;

				if(temp_attack_target == null || temp_attack_target.isAlikeDead() || !actor.isInRange(temp_attack_target, 2000))
					return true;

				boolean isAoE = currentTask.skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA;
				int castRange = currentTask.skill.getAOECastRange();

				if(actor.isMoving)
					return Rnd.chance(10);

				if(actor.getRealDistance(temp_attack_target) <= castRange + 60 && GeoEngine.canSeeTarget(actor, temp_attack_target, false))
				{
					clientStopMoving();
					_pathfind_fails = 0;
					actor.doCast(currentTask.skill, isAoE ? actor : temp_attack_target, !temp_attack_target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if(actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(temp_attack_target);
			}
				break;
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

		L2NpcInstance actor = getActor();
		L2Character target;
		if(actor == null || (target = prepareTarget()) == null)
			return false;

		double distance = actor.getDistance(target);
		return chooseTaskAndTargets(null, target, distance);
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// Удаляем все задания
		clearTasks();

		setAttackTarget(target);
		clientStopMoving();

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			if(_aiTask != null)
				_aiTask.cancel(false);
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 0, AI_TASK_DELAY, false);
		}
	}

	@Override
	protected void onEvtThink()
	{
		L2NpcInstance actor = getActor();
		if(_thinking || actor == null || actor.isActionsDisabled() || actor.isAfraid() || actor.isDead())
			return;

		if(actor.isBlocked() || _randomAnimationEnd > System.currentTimeMillis())
			return;

		if(actor.isRaid() && (actor.isInZonePeace() || actor.isInZone(ZoneType.battle_zone) || actor.isInZone(ZoneType.Siege)))
			teleportHome(true);

		_thinking = true;
		try
		{
			if(!Config.BLOCK_ACTIVE_TASKS && (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || getIntention() == CtrlIntention.AI_INTENTION_IDLE))
				thinkActive();
			else if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				thinkAttack();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			_thinking = false;
		}
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();

		int transformer = getInt("transformOnDead", 0);
		int chance = getInt("transformChance", 100);
		if(transformer > 0 && Rnd.chance(chance))
		{
			try
			{
				if(actor != null)
				{
					Reflection r = actor.getReflection();
					L2MonsterInstance npc = (L2MonsterInstance) NpcTable.getTemplate(transformer).getNewInstance();
					npc.setSpawnedLoc(actor.getLoc());
					npc.setReflection(r);
					npc.onSpawn();
					npc.spawnMe(npc.getSpawnedLoc());
					if(r.getId() > 0)
						r.addSpawn(npc.getSpawn());
					if(killer != null && killer.isPlayable())
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
						killer.setTarget(npc);
						killer.sendPacket(npc.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		int dead = getInt("stakatoOnDead", 0);
        int chance1 = getInt("stakatoChance", 100);
		int count1 = getInt("stakatoCount", 1);
        if(dead > 0 && Rnd.chance(chance1))
		{
			for(int i = 0; i < count1; i++)
				try
				{
					if(actor != null)
					{
						Reflection r = actor.getReflection();
						L2MonsterInstance npc = (L2MonsterInstance)NpcTable.getTemplate(dead).getNewInstance();
						Location pos = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
						npc.setSpawnedLoc(pos);
						npc.setReflection(r);
						npc.onSpawn();
						npc.spawnMe(npc.getSpawnedLoc());
						if(r.getId() > 0)
							r.addSpawn(npc.getSpawn());
						if(killer != null && killer.isPlayable())
						{
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
							killer.setTarget(npc);
							killer.sendPacket(npc.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
						}
					}
				}
            catch(Exception e)
            {
                e.printStackTrace();
            }
		}

		stopAITask();

		if(actor != null)
			actor.clearAggroList(false);

		if(actor != null)
			actor.setAttackTimeout(Long.MAX_VALUE);

		// Удаляем все задания
		clearTasks();

		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtClanAttacked(L2Character attacked_member, L2Character attacker, int damage)
	{
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
			return;
		L2NpcInstance actor = getActor();
		if(actor == null || !actor.isInRange(attacked_member, actor.getFactionRange()))
			return;
		if(Math.abs(attacker.getZ() - actor.getZ()) > MAX_Z_AGGRO_RANGE)
			return;

		if(GeoEngine.canSeeTarget(actor, attacked_member, false))
			notifyEvent(CtrlEvent.EVT_AGGRESSION, new Object[] { attacker, attacker.isSummon() ? damage : 3 });
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(attacker == null || actor == null)
			return;

		if(!actor.isDead())
		{
			int transformer = getInt("transformOnUnderAttack", 0);
			if(transformer > 0)
				try
				{
					int chance = getInt("transformChance", 5);
					if(chance == 100 || ((L2MonsterInstance) actor).getChampion() == 0 && actor.getCurrentHpPercents() > 50 && Rnd.chance(chance))
					{
						Reflection r = actor.getReflection();
						L2MonsterInstance npc = (L2MonsterInstance) NpcTable.getTemplate(transformer).getNewInstance();
						npc.setSpawnedLoc(actor.getLoc());
						npc.setReflection(r);
						npc.onSpawn();
						npc.setChampion(((L2MonsterInstance) actor).getChampion());
						npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
						npc.spawnMe(npc.getSpawnedLoc());
						if(r.getId() > 0)
							r.addSpawn(npc.getSpawn());
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
						actor.decayMe();
						actor.doDie(actor);
						attacker.setTarget(npc);
						attacker.sendPacket(npc.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
						return;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}

		if(!actor.canAttackCharacter(attacker))
			return;

		L2Player player = attacker.getPlayer();
		if(player != null)
		{
			GArray<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.MOBGOTATTACKED);
			if(quests != null)
				for(QuestState qs : quests)
					qs.getQuest().notifyAttack(actor, qs);
		}

		actor.callFriends(attacker, damage);
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{
		if(attacker == null || attacker.getPlayer() == null)
			return;

		L2NpcInstance actor = getActor();
		if(actor == null || !actor.canAttackCharacter(attacker))
			return;

		L2Player player = attacker.getPlayer();

		actor.setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
		setGlobalAggro(0);

		attacker.addDamageHate(actor, 0, aggro);

		// Обычно 1 хейт добавляется хозяину суммона, чтобы после смерти суммона моб накинулся на хозяина.
		if(attacker.getPlayer() != null && aggro > 0 && (attacker.isSummon() || attacker.isPet()))
			attacker.getPlayer().addDamageHate(actor, 0, getBool("searchingMaster", false) ? aggro : 1);

		if(!actor.isRunning())
			startRunningTask(1000);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			// Показываем анимацию зарядки шотов, если есть таковые.
			switch(actor.getTemplate().shots)
			{
				case SOUL:
					actor.unChargeShots(false);
					break;
				case SPIRIT:
				case BSPIRIT:
					actor.unChargeShots(true);
					break;
				case SOUL_SPIRIT:
				case SOUL_BSPIRIT:
					actor.unChargeShots(false);
					actor.unChargeShots(true);
					break;
			}

			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
	}

	protected boolean maybeMoveToHome()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return false;

		boolean randomWalk = actor.hasRandomWalk();
		Location sloc = actor.getSpawnedLoc();
		if(sloc == null)
			return false;

		// Моб попал на другой этаж
		if(Math.abs(sloc.z - actor.getZ()) > 128 && !isGlobalAI())
		{
			teleportHome(true);
			return true;
		}

		// Random walk or not?
		if(randomWalk && (!Config.RND_WALK || !Rnd.chance(Config.RND_WALK_RATE)))
			return false;

		boolean isInRange = actor.isInRangeZ(sloc, Config.MAX_DRIFT_RANGE);

		if(!randomWalk && isInRange)
			return false;

		int x = sloc.x + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		int y = sloc.y + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		int z = GeoEngine.getHeight(x, y, sloc.z, actor.getReflection().getGeoIndex());

		if(Math.abs(sloc.z - z) > 64)
			return false;

		L2Spawn spawn = actor.getSpawn();
		if(spawn != null && spawn.getLocation() != 0 && !TerritoryTable.getInstance().getLocation(spawn.getLocation()).isInside(x, y))
			return false;

		actor.setWalking();

		// Телепортируемся домой, только если далеко от дома
		if(!actor.moveToLocation(x, y, z, 0, false) && !isInRange)
			teleportHome(true);

		return true;
	}

	public void returnHome(boolean clearAggro)
	{
		if(Config.ALWAYS_TELEPORT_HOME)
		{
			teleportHome(clearAggro);
			return;
		}

		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(clearAggro)
			actor.clearAggroList(true);

		setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		// Удаляем все задания
		clearTasks();

		Location sloc = actor.getSpawnedLoc();
		if(sloc == null)
			return;

		if(!clearAggro)
			actor.setRunning();

		addTaskMove(sloc, false);
	}

	@Override
	public void teleportHome(boolean clearAggro)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(clearAggro)
			actor.clearAggroList(true);

		setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		// Удаляем все задания
		clearTasks();

		Location sloc = actor.getSpawnedLoc();
		if(sloc == null)
			return;

	//	actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0));
	//	actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getHeight(sloc, actor.getReflection().getGeoIndex()));
	}

	protected L2Character prepareTarget()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return null;

		// Новая цель исходя из агрессивности
		L2Character hated = actor.isConfused() && getAttackTarget() != actor ? getAttackTarget() : actor.getMostHated();

		// Для "двинутых" боссов, иногда, выбираем случайную цель
		if(!actor.isConfused() && Rnd.chance(getInt("isMadness", 1)))
		{
			L2Character randomHated = actor.getRandomHated();
			if(randomHated != null && randomHated != hated && randomHated != actor)
			{
				setAttackTarget(randomHated);
				if(_madnessTask == null && !actor.isConfused())
				{
					actor.startConfused();
					_madnessTask = ThreadPoolManager.getInstance().scheduleAi(new MadnessTask(), 10000, false);
				}
				return randomHated;
			}
		}

		if(hated != null && hated != actor)
		{
			setAttackTarget(hated);
			return hated;
		}

		returnHome(false);
		return null;
	}

	protected boolean canUseSkill(L2Skill sk, L2Character target, double distance)
	{
		L2NpcInstance actor = getActor();

		if(actor == null || sk == null || sk.isNotUsedByAI() || actor.isSkillDisabled(sk.getId()))
			return false;

		double mpConsume2 = sk.getMpConsume2();
		if(sk.isMagic())
			mpConsume2 = actor.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, sk);
		else
			mpConsume2 = actor.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, sk);
		if(actor.getCurrentMp() < mpConsume2)
			return false;
		if(actor.isMuted(sk))
			return false;
		SkillType st = sk.getSkillType();
		if(st == SkillType.CANCEL && !Rnd.chance(target.getEffectList().getAllCancelableEffectsCount(-1) * 20))
			return false;
		int castRange = sk.getAOECastRange();
		/*
				if(distance > 0)
				{
					if(castRange > 200)
					{
						if(distance <= 200 && !actor.isRaid())
							return false;
					}
					else if(distance > 200)
						return false;
				}
		*/
		if(castRange <= 200 && distance > 200)
			return false;
		if(target.getEffectList().getEffectsBySkill(sk) != null)
			return false;
		return true;
	}

	protected boolean canUseSkill(L2Skill sk, L2Character target)
	{
		return canUseSkill(sk, target, 0);
	}

	protected L2Skill[] selectUsableSkills(L2Character target, double distance, L2Skill... skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return null;

		L2Skill[] ret = null;
		int usable = 0;

		for(L2Skill skill : skills)
			if(canUseSkill(skill, target, distance))
			{
				if(ret == null)
					ret = new L2Skill[skills.length];
				ret[usable++] = skill;
			}

		if(ret == null || usable == skills.length)
			return ret;

		L2Skill[] ret_resized = new L2Skill[usable];
		System.arraycopy(ret, 0, ret_resized, 0, usable);
		return ret_resized;
	}

	protected static L2Skill selectTopSkillByDamage(L2Character actor, L2Character target, double distance, L2Skill... skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		RndSelector<L2Skill> rnd = new RndSelector<L2Skill>(skills.length);
		double weight;
		for(L2Skill skill : skills)
		{
			weight = skill.getSimpleDamage(actor, target) * skill.getAOECastRange() / distance;
			if(weight <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected static L2Skill selectTopSkillByDebuff(L2Character actor, L2Character target, double distance, L2Skill... skills) //FIXME
	{
		if(skills == null || skills.length == 0)
			return null;

		RndSelector<L2Skill> rnd = new RndSelector<L2Skill>(skills.length);
		double weight;
		for(L2Skill skill : skills)
		{
			if(skill.getSameByStackType(target) != null)
				continue;
			if((weight = 100f * skill.getAOECastRange() / distance) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected static L2Skill selectTopSkillByBuff(L2Character target, L2Skill... skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		RndSelector<L2Skill> rnd = new RndSelector<L2Skill>(skills.length);
		double weight;
		for(L2Skill skill : skills)
		{
			if(skill.getSameByStackType(target) != null)
				continue;
			if((weight = skill.getPower()) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected static L2Skill selectTopSkillByHeal(L2Character target, L2Skill... skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		double hp_reduced = target.getMaxHp() - target.getCurrentHp();
		if(hp_reduced < 1)
			return null;

		RndSelector<L2Skill> rnd = new RndSelector<L2Skill>(skills.length);
		double weight;
		for(L2Skill skill : skills)
		{
			if((weight = Math.abs(skill.getPower() - hp_reduced)) <= 0)
				weight = 1;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected void addDesiredSkill(Map<L2Skill, Integer> skill_list, L2Character target, double distance, L2Skill[] skills_for_use)
	{
		if(skills_for_use == null || skills_for_use.length == 0 || target == null)
			return;
		for(L2Skill sk : skills_for_use)
			addDesiredSkill(skill_list, target, distance, sk);
	}

	protected void addDesiredSkill(Map<L2Skill, Integer> skill_list, L2Character target, double distance, L2Skill skill_for_use)
	{
		if(skill_for_use == null || target == null || !canUseSkill(skill_for_use, target))
			return;
		int weight = (int) -Math.abs(skill_for_use.getAOECastRange() - distance);
		if(skill_for_use.getAOECastRange() >= distance)
			weight += 1000000;
		else if(skill_for_use.isNotTargetAoE() && skill_for_use.getTargets(getActor(), target, false).size() == 0)
			return;
		skill_list.put(skill_for_use, weight);
	}

	protected void addDesiredHeal(Map<L2Skill, Integer> skill_list, L2Skill[] skills_for_use)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || skills_for_use == null || skills_for_use.length == 0)
			return;
		double hp_reduced = actor.getMaxHp() - actor.getCurrentHp();
		double hp_precent = actor.getCurrentHpPercents();
		if(hp_reduced < 1)
			return;
		int weight;
		for(L2Skill sk : skills_for_use)
			if(canUseSkill(sk, actor) && sk.getPower() <= hp_reduced)
			{
				weight = (int) sk.getPower();
				if(hp_precent < 50)
					weight += 1000000;
				skill_list.put(sk, weight);
			}
	}

	protected void addDesiredBuff(Map<L2Skill, Integer> skill_list, L2Skill[] skills_for_use)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || skills_for_use == null || skills_for_use.length == 0)
			return;
		for(L2Skill sk : skills_for_use)
			if(canUseSkill(sk, actor))
				skill_list.put(sk, 1000000);
	}

	protected L2Skill selectTopSkill(Map<L2Skill, Integer> skill_list)
	{
		if(skill_list == null || skill_list.isEmpty())
			return null;
		int next_weight, top_weight = Integer.MIN_VALUE;
		for(L2Skill next : skill_list.keySet())
			if((next_weight = skill_list.get(next)) > top_weight)
				top_weight = next_weight;
		if(top_weight == Integer.MIN_VALUE)
			return null;
		for(L2Skill next : skill_list.keySet())
			if(skill_list.get(next) < top_weight)
				skill_list.remove(next);
		next_weight = skill_list.size();
		return skill_list.keySet().toArray(new L2Skill[next_weight])[Rnd.get(next_weight)];
	}

	protected boolean chooseTaskAndTargets(L2Skill r_skill, L2Character target, double distance)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return false;
		// Использовать скилл если можно, иначе атаковать
		if(r_skill != null && !actor.isMuted(r_skill))
		{
			// Проверка цели, и смена если необходимо
			if(r_skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
				target = actor;
			//else if(r_skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA)
			//	target = actor;
			else if(actor.isMovementDisabled() && r_skill.isOffensive() && distance > r_skill.getAOECastRange() + 60)
			{
				GArray<L2Playable> targets = new GArray<L2Playable>();
				for(AggroInfo ai : actor.getAggroList())
					if(ai.attacker != null && actor.getDistance(ai.attacker) <= r_skill.getAOECastRange() + 60)
						targets.add(ai.attacker);
				if(targets.size() > 0)
					target = targets.get(Rnd.get(targets.size()));
			}

			if(actor.isAMuted())
				return false;

			// Добавить новое задание
			Task task = new Task();
			task.type = r_skill.isOffensive() ? TaskType.CAST : TaskType.BUFF;
			task.targetStoreId = target == null ? 0 : target.getStoredId();
			task.skill = r_skill;
			_task_list.add(task);
			_def_think = true;
			return true;
		}

		// Смена цели, если необходимо
		if(actor.isMovementDisabled() && distance > actor.getPhysicalAttackRange() + 40)
		{
			GArray<L2Playable> targets = new GArray<L2Playable>();
			for(AggroInfo ai : actor.getAggroList())
				if(ai.attacker != null && actor.getDistance(ai.attacker) <= actor.getPhysicalAttackRange() + 40)
					targets.add(ai.attacker);
			if(targets.size() > 0)
				target = targets.get(Rnd.get(targets.size()));
		}

		// Добавить новое задание
		addTaskAttack(target);
		return true;
	}

	@Override
	public boolean isActive()
	{
		return _aiTask != null;
	}

	@Override
	public void clearTasks()
	{
		_def_think = false;
		_task_list.clear();
	}

	/** переход в режим бега через определенный интервал времени */
	public void startRunningTask(int interval)
	{
		L2NpcInstance actor = getActor();
		if(actor != null && _runningTask == null && !actor.isRunning())
			_runningTask = ThreadPoolManager.getInstance().scheduleAi(new RunningTask(), interval, false);
	}

	@Override
	public boolean isGlobalAggro()
	{
		if(_globalAggro == 0)
			return true;
		if(_globalAggro < System.currentTimeMillis())
		{
			_globalAggro = 0;
			return true;
		}
		return false;
	}

	@Override
	public void setGlobalAggro(long value)
	{
		_globalAggro = value;
	}

	public StatsSet getAIParams()
	{
		if(this_params != null)
			return this_params;
		L2NpcInstance actor = getActor();
		if(actor != null)
			return actor.getTemplate().getAIParams();
		this_params = new StatsSet();
		return this_params;
	}

	private StatsSet getThisAIParams()
	{
		if(this_params == null)
		{
			L2NpcInstance actor = getActor();
			this_params = actor == null ? new StatsSet() : actor.getTemplate().getAIParams().clone();
		}
		return this_params;
	}

	public void AddUseSkillDesire(L2Character target, L2Skill skill, int weight)
	{
		Task task = new Task();
		task.type = skill.isOffensive() ? TaskType.CAST : TaskType.BUFF;
		task.targetStoreId = target == null ? 0 : target.getStoredId();
		task.skill = skill;
		task.weight = weight;
		_task_list.add(task);
		_def_think = true;
	}

	public static void DebugTask(Task task)
	{
		if(task == null)
			System.out.println("NULL");
		else
		{
			System.out.print("Weight=" + task.weight);
			System.out.print("; Type=" + task.type);
			System.out.print("; Skill=" + task.skill);
			System.out.print("; Target=" + L2ObjectsStorage.get(task.targetStoreId));
			System.out.print("; Loc=" + task.loc);
			System.out.println();
		}
	}

	public void DebugTasks()
	{
		if(_task_list.size() == 0)
		{
			System.out.println("No Tasks");
			return;
		}

		int i = 0;
		for(Task task : _task_list)
		{
			System.out.print("Task [" + i + "]: ");
			DebugTask(task);
			i++;
		}
	}

	public boolean getBool(String name)
	{
		return getAIParams().getBool(name);
	}

	public boolean getBool(String name, boolean defult)
	{
		return getAIParams().getBool(name, defult);
	}

	public int getInt(String name)
	{
		return getAIParams().getInteger(name);
	}

	public int getInt(String name, int defult)
	{
		return getAIParams().getInteger(name, defult);
	}

	public long getLong(String name)
	{
		return getAIParams().getLong(name);
	}

	public long getLong(String name, long defult)
	{
		return getAIParams().getLong(name, defult);
	}

	public float getFloat(String name)
	{
		return getAIParams().getFloat(name);
	}

	public float getFloat(String name, float defult)
	{
		return getAIParams().getFloat(name, defult);
	}

	public String getString(String name)
	{
		return getAIParams().getString(name);
	}

	public String getString(String name, String defult)
	{
		return getAIParams().getString(name, defult);
	}

	public void set(String name, boolean value)
	{
		getThisAIParams().set(name, value);
	}

	public void set(String name, int value)
	{
		getThisAIParams().set(name, value);
	}

	public void set(String name, long value)
	{
		getThisAIParams().set(name, value);
	}

	public void set(String name, double value)
	{
		getThisAIParams().set(name, value);
	}

	public void set(String name, String value)
	{
		getThisAIParams().set(name, value);
	}

	@Override
	public L2NpcInstance getActor()
	{
		return (L2NpcInstance) super.getActor();
	}

	protected boolean defaultThinkBuff(int rateSelf)
	{
		return defaultThinkBuff(rateSelf, 0);
	}

	protected boolean defaultThinkBuff(int rateSelf, int rateFriends)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		//TODO сделать более разумный выбор баффа, сначала выбирать подходящие а потом уже рандомно 1 из них
		if(_buff.length > 0)
		{
			if(Rnd.chance(rateSelf))
			{
				L2Skill r_skill = _buff[Rnd.get(_buff.length)];
				if(actor.getEffectList().getEffectsBySkill(r_skill) == null)
				{
					addTaskBuff(actor, r_skill);
					return true;
				}
			}

			if(Rnd.chance(rateFriends))
			{
				L2Skill r_skill = _buff[Rnd.get(_buff.length)];
				double bestDistance = 1000000;
				L2NpcInstance target = null;
				for(L2NpcInstance npc : actor.ActiveFriendTargets(true, true))
					if(npc != null && npc.getEffectList().getEffectsBySkill(r_skill) == null)
					{
						double distance = actor.getDistance(npc);
						if(target == null || bestDistance > distance)
						{
							target = npc;
							bestDistance = distance;
						}
					}

				if(target != null)
				{
					addTaskBuff(target, r_skill);
					return true;
				}
			}
		}

		return false;
	}

	protected boolean defaultFightTask()
	{
		clearTasks();
		L2Character target;
		if((target = prepareTarget()) == null)
			return false;

		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;

		double distance = actor.getDistance(target);

		if(!actor.isAMuted() && Rnd.chance(getRatePHYS()))
			return chooseTaskAndTargets(null, target, distance);

		double target_hp_precent = target.getCurrentHpPercents();
		double actor_hp_precent = actor.getCurrentHpPercents();

		// Изначально все дружественные цели живые
		double frendly_target_hp_precent = 100;
		L2MonsterInstance targetToHeal = null;
		if(actor.isMinion())
		{
			// Ищем самую дохлую дружественную цель
			L2MonsterInstance master = ((L2MinionInstance) actor).getLeader();
			if(master != null && !master.isDead() && master.isInCombat())
			{
				if(frendly_target_hp_precent > master.getCurrentHpPercents())
				{
					targetToHeal = master;
					frendly_target_hp_precent = master.getCurrentHpPercents();
				}

				MinionList list = master.getMinionList();
				if(list != null)
					for(L2MinionInstance m : list.getSpawnedMinions())
						if(m != actor && frendly_target_hp_precent > m.getCurrentHpPercents())
						{
							targetToHeal = m;
							frendly_target_hp_precent = m.getCurrentHpPercents();
						}
			}
		}

		L2Skill[] dam_skills = getRateDAM() > 0 ? selectUsableSkills(target, distance, _dam_skills) : null;
		L2Skill[] dot_skills = getRateDOT() > 0 && target_hp_precent > 10 ? selectUsableSkills(target, distance, _dot_skills) : null;
		L2Skill[] debuff_skills = getRateDEBUFF() > 0 && target_hp_precent > 10 ? selectUsableSkills(target, distance, _debuff_skills) : null;
		L2Skill[] stun = getRateSTUN() > 0 ? selectUsableSkills(target, distance, _stun) : null;
		L2Skill[] heal = getRateHEAL() > 0 && (actor_hp_precent < 85 || frendly_target_hp_precent < 95) ? selectUsableSkills(actor, 0, _heal) : null;
		L2Skill[] buff = getRateBUFF() > 0 ? selectUsableSkills(actor, 0, _buff) : null;

		RndSelector<L2Skill[]> rnd = new RndSelector<L2Skill[]>();
		rnd.add(dam_skills, getRateDAM());
		rnd.add(dot_skills, getRateDOT());
		rnd.add(debuff_skills, getRateDEBUFF());
		rnd.add(heal, getRateHEAL());
		rnd.add(buff, getRateBUFF());
		rnd.add(stun, getRateSTUN());

		L2Skill[] selected_skills = rnd.select();
		rnd.clear();

		if(selected_skills == dam_skills || selected_skills == dot_skills)
			return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected_skills), target, distance);

		if(selected_skills == debuff_skills || selected_skills == stun)
			return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected_skills), target, distance);

		// TODO сделать баф дружественных целей
		if(selected_skills == buff)
			return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected_skills), actor, distance);

		// TODO сделать хил дружественный целей для обычных мобов
		if(selected_skills == heal)
		{
			if(actor_hp_precent < frendly_target_hp_precent)
				return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected_skills), actor, distance);
			else
			{
				distance = actor.getDistance(targetToHeal);
				return chooseTaskAndTargets(selectTopSkillByHeal(targetToHeal, selected_skills), targetToHeal, distance);
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
		return true;
	}

	protected int getMaxPursueRange()
	{
		return MAX_PURSUE_RANGE;
	}

	@Override
	public void setMaxPursueRange(int range)
	{
		MAX_PURSUE_RANGE = range;
	}

	public int getMaxPathfindFails()
	{
		return 3;
	}

	public int getMaxAttackTimeout()
	{
		return 20000;
	}

	public int getTeleportTimeout()
	{
		return 10000;
	}
}