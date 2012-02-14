package l2rt.gameserver.ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Skill.NextAction;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.network.serverpackets.MyTargetSelected;
import l2rt.util.Location;

import java.util.concurrent.ScheduledFuture;

import static l2rt.gameserver.ai.CtrlIntention.*;

public class L2PlayableAI extends L2CharacterAI
{
	private boolean thinking = false; // to prevent recursive thinking

	private Object _intention_arg0 = null, _intention_arg1 = null;
	private L2Skill _skill;

	private nextAction _nextAction;
	private Object _nextAction_arg0;
	private Object _nextAction_arg1;
	private boolean _nextAction_arg2;
	private boolean _nextAction_arg3;

	private boolean _forceUse;
	private boolean _dontMove;

	private ScheduledFuture<?> _followTask;

	public L2PlayableAI(L2Playable actor)
	{
		super(actor);
	}

	public enum nextAction
	{
		ATTACK,
		CAST,
		MOVE,
		REST,
		PICKUP,
		INTERACT
	}

	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		super.changeIntention(intention, arg0, arg1);
		_intention_arg0 = arg0;
		_intention_arg1 = arg1;
	}

	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention_arg0 = null;
		_intention_arg1 = null;
		super.setIntention(intention, arg0, arg1);
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Character target)
	{
		_skill = skill;
		super.onIntentionCast(skill, target);
	}

	@Override
	public void setNextAction(nextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{
		_nextAction = action;
		_nextAction_arg0 = arg0;
		_nextAction_arg1 = arg1;
		_nextAction_arg2 = arg2;
		_nextAction_arg3 = arg3;
	}

	public boolean setNextIntention()
	{
		nextAction nextAction = _nextAction;
		Object nextAction_arg0 = _nextAction_arg0;
		Object nextAction_arg1 = _nextAction_arg1;
		boolean nextAction_arg2 = _nextAction_arg2;
		boolean nextAction_arg3 = _nextAction_arg3;

		L2Playable actor = getActor();
		if(nextAction == null || actor == null)
			return false;

		L2Skill skill;
		L2Character target;
		L2Object object;

		switch(nextAction)
		{
			case ATTACK:
				if(nextAction_arg0 == null)
					return false;
				target = (L2Character) nextAction_arg0;
				_forceUse = nextAction_arg2;
				_dontMove = nextAction_arg3;
				clearNextAction();
				setIntention(AI_INTENTION_ATTACK, target);
				break;
			case CAST:
				if(nextAction_arg0 == null || nextAction_arg1 == null)
					return false;
				skill = (L2Skill) nextAction_arg0;
				target = (L2Character) nextAction_arg1;
				_forceUse = nextAction_arg2;
				_dontMove = nextAction_arg3;
				clearNextAction();
				if(!skill.checkCondition(actor, target, _forceUse, _dontMove, true))
				{
					if(skill.getNextAction() == NextAction.ATTACK && !actor.equals(target))
					{
						setNextAction(l2rt.gameserver.ai.L2PlayableAI.nextAction.ATTACK, target, null, _forceUse, false);
						return setNextIntention();
					}
					return false;
				}
				setIntention(AI_INTENTION_CAST, skill, target);
				break;
			case MOVE:
				if(nextAction_arg0 == null || nextAction_arg1 == null)
					return false;
				Location loc = (Location) nextAction_arg0;
				Integer offset = (Integer) nextAction_arg1;
				clearNextAction();
				actor.moveToLocation(loc, offset, nextAction_arg2);
				break;
			case REST:
				actor.sitDown();
				break;
			case INTERACT:
				if(nextAction_arg0 == null)
					return false;
				object = (L2Object) nextAction_arg0;
				clearNextAction();
				onIntentionInteract(object);
				break;
			case PICKUP:
				if(nextAction_arg0 == null)
					return false;
				object = (L2Object) nextAction_arg0;
				clearNextAction();
				onIntentionPickUp(object);
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public void clearNextAction()
	{
		_nextAction = null;
		_nextAction_arg0 = null;
		_nextAction_arg1 = null;
		_nextAction_arg2 = false;
		_nextAction_arg3 = false;
	}

	@Override
	protected void onEvtFinishCasting()
	{
		if(!setNextIntention())
			setIntention(AI_INTENTION_ACTIVE);
	}

	@Override
	protected void onEvtReadyToAct()
	{
		if(!setNextIntention())
			onEvtThink();
	}

	@Override
	protected void onEvtArrived()
	{
		if(!setNextIntention())
			if(getIntention() == AI_INTENTION_INTERACT || getIntention() == AI_INTENTION_PICK_UP)
				onEvtThink();
			else
				changeIntention(AI_INTENTION_ACTIVE, null, null);
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		switch(getIntention())
		{
			case AI_INTENTION_ATTACK:
				thinkAttack(false);
				break;
			case AI_INTENTION_CAST:
				thinkCast(false);
				break;
			case AI_INTENTION_FOLLOW:
				if(_followTask != null)
					_followTask.cancel(false);
				_followTask = ThreadPoolManager.getInstance().scheduleMove(new ThinkFollow(), 1000);
				break;
		}
	}

	@Override
	protected void onEvtThink()
	{
		L2Playable actor = getActor();
		if(actor == null || thinking || actor.isActionsDisabled())
			return;

		thinking = true;

		try
		{
			switch(getIntention())
			{
				case AI_INTENTION_ATTACK:
					thinkAttack(true);
					break;
				case AI_INTENTION_CAST:
					thinkCast(true);
					break;
				case AI_INTENTION_PICK_UP:
					thinkPickUp();
					break;
				case AI_INTENTION_INTERACT:
					thinkInteract();
					break;
			}
		}
		catch(Exception e)
		{
			_log.warning("Exception onEvtThink(): " + e);
			e.printStackTrace();
		}
		finally
		{
			thinking = false;
		}
	}

	public class ThinkFollow implements Runnable
	{
		public L2Playable getActor()
		{
			return L2PlayableAI.this.getActor();
		}

		@Override
		public void run()
		{
			_followTask = null;
			L2Playable actor = getActor();
			if(actor == null)
				return;
			if(getIntention() != AI_INTENTION_FOLLOW)
			{
				// Если пет прекратил преследование, меняем статус, чтобы не пришлось щелкать на кнопку следования 2 раза.
				if((actor.isPet() || actor.isSummon()) && getIntention() == AI_INTENTION_ACTIVE)
					actor.setFollowStatus(false, false);
				return;
			}
			L2Character target = (L2Character) _intention_arg0;
			Integer offset = (Integer) _intention_arg1;
			if(target == null || target.isAlikeDead() || actor.getDistance(target) > 4000)
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}
			L2Player actor_player = actor.getPlayer();
			if(actor_player == null || !actor_player.isConnected() || (actor.isPet() || actor.isSummon()) && actor_player.getPet() != actor)
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}
			if(!actor.isInRange(target, offset + 20) && (!actor.isFollow || actor.getFollowTarget() != target))
				actor.followToCharacter(target, offset, false);
			_followTask = ThreadPoolManager.getInstance().scheduleMove(this, 1000);
		}
	}

	@Override
	protected void onIntentionInteract(L2Object object)
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;
		if(actor.isActionsDisabled())
		{
			setNextAction(nextAction.INTERACT, object, null, false, false);
			clientActionFailed();
			return;
		}

		clearNextAction();
		changeIntention(AI_INTENTION_INTERACT, object, null);
		onEvtThink();
	}

	protected void thinkInteract()
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;

		L2Object target = (L2Object) _intention_arg0;

		if(target == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		int range = (int) (Math.max(30, actor.getMinDistance(target)) + 20);

		if(actor.isInRangeZ(target, range))
		{
			if(actor.isPlayer())
				((L2Player) actor).doInteract(target);
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
		{
			actor.moveToLocation(target.getLoc(), 40, true);
			setNextAction(nextAction.INTERACT, target, null, false, false);
		}
	}

	@Override
	protected void onIntentionPickUp(L2Object object)
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;

		if(actor.isActionsDisabled())
		{
			setNextAction(nextAction.PICKUP, object, null, false, false);
			clientActionFailed();
			return;
		}

		clearNextAction();
		changeIntention(AI_INTENTION_PICK_UP, object, null);
		onEvtThink();
	}

	protected void thinkPickUp()
	{
		final L2Playable actor = getActor();
		if(actor == null)
			return;

		final L2Object target = (L2Object) _intention_arg0;

		if(target == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		if(actor.isInRange(target, 30) && Math.abs(actor.getZ() - target.getZ()) < 50)
		{
			if(actor.isPlayer() || actor.isPet())
				actor.doPickupItem(target);
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
			ThreadPoolManager.getInstance().executePathfind(new Runnable(){
				public void run()
				{
					actor.moveToLocation(target.getLoc(), 10, true);
					setNextAction(nextAction.PICKUP, target, null, false, false);
				}
			});
	}

	protected void thinkAttack(boolean checkRange)
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;

		L2Player player = actor.getPlayer();
		if(player == null)
		{
			onAttackFail();
			return;
		}

		if(actor.isActionsDisabled() || actor.isAttackingDisabled())
		{
			actor.sendActionFailed();
			return;
		}

		boolean isPosessed = actor instanceof L2Summon && ((L2Summon) actor).isPosessed();

		L2Character attack_target = getAttackTarget();
		if(attack_target == null || attack_target.isDead() || !isPosessed && !(_forceUse ? attack_target.isAttackable(player) : attack_target.isAutoAttackable(player)))
		{
			onAttackFail();
			actor.sendActionFailed();
			return;
		}

		if(!checkRange)
		{
			clientStopMoving();
			actor.doAttack(attack_target);
			return;
		}

		int range = actor.getPhysicalAttackRange();
		if(range < 10)
			range = 10;

		boolean canSee = GeoEngine.canSeeTarget(actor, attack_target, false);

		if(!canSee && (range > 200 || Math.abs(actor.getZ() - attack_target.getZ()) > 200))
		{
			actor.sendPacket(Msg.CANNOT_SEE_TARGET);
			onAttackFail();
			actor.sendActionFailed();
			return;
		}

		range += actor.getMinDistance(attack_target);

		if(actor.isFakeDeath())
			actor.breakFakeDeath();

		if(actor.isInRangeZ(attack_target, range))
		{
			if(!canSee)
			{
				actor.sendPacket(Msg.CANNOT_SEE_TARGET);
				onAttackFail();
				actor.sendActionFailed();
				return;
			}

			clientStopMoving(false);
			actor.doAttack(attack_target);
		}
		else if(!_dontMove)
			ThreadPoolManager.getInstance().executePathfind(new ExecuteFollow(actor, attack_target, range - 20));
		else
			actor.sendActionFailed();
	}

	/**
	 * Нужен для оверрайда саммоном, чтобы он не прекращал фоллов.
	 */
	protected void onAttackFail()
	{
		setIntention(AI_INTENTION_ACTIVE);
	}

	protected void thinkCast(boolean checkRange)
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;

		L2Character attack_target = getAttackTarget();

		if(_skill.getSkillType() == SkillType.CRAFT || _skill.isToggle())
		{
			if(_skill.checkCondition(actor, attack_target, _forceUse, _dontMove, true))
				actor.doCast(_skill, attack_target, _forceUse);
			return;
		}

		if(attack_target == null || attack_target.isDead() != _skill.getCorpse() && !_skill.isNotTargetAoE())
		{
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		if(!checkRange)
		{
			// Если скилл имеет следующее действие, назначим это действие после окончания действия скилла
			if(_skill.getNextAction() == NextAction.ATTACK && !actor.equals(attack_target))
				setNextAction(nextAction.ATTACK, attack_target, null, _forceUse, false);
			else
				clearNextAction();

			clientStopMoving();

			if(_skill.checkCondition(actor, attack_target, _forceUse, _dontMove, true))
				actor.doCast(_skill, attack_target, _forceUse);
			else
			{
				setNextIntention();
				if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
					thinkAttack(true);
			}

			return;
		}

		int range = actor.getMagicalAttackRange(_skill);
		if(range < 10)
			range = 10;

		boolean canSee = _skill.getSkillType() == SkillType.TAKECASTLE || _skill.getSkillType() == SkillType.TAKEFORTRESS || GeoEngine.canSeeTarget(actor, attack_target, actor.isFlying());
		boolean noRangeSkill = _skill.getCastRange() == 32767;

		if(!noRangeSkill && !canSee && (range > 200 || Math.abs(actor.getZ() - attack_target.getZ()) > 200))
		{
			actor.sendPacket(Msg.CANNOT_SEE_TARGET);
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		range += actor.getMinDistance(attack_target);

		if(actor.isFakeDeath())
			actor.breakFakeDeath();

		if(actor.isInRangeZ(attack_target, range) || noRangeSkill)
		{
			if(!noRangeSkill && !canSee)
			{
				actor.sendPacket(Msg.CANNOT_SEE_TARGET);
				setIntention(AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return;
			}

			// Если скилл имеет следующее действие, назначим это действие после окончания действия скилла
			if(_skill.getNextAction() == NextAction.ATTACK && !actor.equals(attack_target))
				setNextAction(nextAction.ATTACK, attack_target, null, _forceUse, false);
			else
				clearNextAction();

			if(_skill.checkCondition(actor, attack_target, _forceUse, _dontMove, true))
			{
				clientStopMoving(false);
				actor.doCast(_skill, attack_target, _forceUse);
			}
			else
			{
				setNextIntention();
				if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
					thinkAttack(true);
			}
		}
		else if(!_dontMove)
			ThreadPoolManager.getInstance().executePathfind(new ExecuteFollow(actor, attack_target, range - 20));
		else
		{
			actor.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			setIntention(AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
		}
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		clearNextAction();
		L2Playable actor = getActor();
		if(actor != null)
			actor.clearHateList(true);
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtFakeDeath()
	{
		clearNextAction();
		super.onEvtFakeDeath();
	}

	public void lockTarget(L2Character target)
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;

		if(target == null)
			actor.setAggressionTarget(null);
		else if(actor.getAggressionTarget() == null)
		{
			L2Object actorStoredTarget = actor.getTarget();
			actor.setAggressionTarget(target);
			actor.setTarget(target);

			clearNextAction();
			switch(getIntention())
			{
				case AI_INTENTION_ATTACK:
					setAttackTarget(target);
					break;
				case AI_INTENTION_CAST:
					L2Skill skill = actor.getCastingSkill();
					if(skill == null)
						skill = _skill;
					if(skill != null && !skill.isUsingWhileCasting())
						switch(skill.getTargetType())
						{
							case TARGET_ONE:
							case TARGET_AREA:
							case TARGET_MULTIFACE:
							case TARGET_TUNNEL:
								setAttackTarget(target);
								actor.setCastingTarget(target);
								break;
						}
					break;
			}

			if(actorStoredTarget != target)
				actor.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
		}
	}

	@Override
	public void Attack(L2Object target, boolean forceUse, boolean dontMove)
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;

		if(target.isCharacter() && (actor.isActionsDisabled() || actor.isAttackingDisabled()))
		{
			// Если не можем атаковать, то атаковать позже
			setNextAction(nextAction.ATTACK, target, null, forceUse, false);
			actor.sendActionFailed();
			return;
		}

		_dontMove = dontMove;
		_forceUse = forceUse;
		clearNextAction();
		setIntention(AI_INTENTION_ATTACK, target);
	}

	@Override
	public void Cast(L2Skill skill, L2Character target, boolean forceUse, boolean dontMove)
	{
		L2Playable actor = getActor();
		if(actor == null)
			return;

		// Если скилл альтернативного типа (например, бутылка на хп),
		// то он может использоваться во время каста других скиллов, или во время атаки, или на бегу.
		// Поэтому пропускаем дополнительные проверки.
		if(skill.altUse() || skill.isToggle())
		{
			if(skill.isToggle() && actor.isToggleDisabled() || skill.isHandler() && actor.isPotionsDisabled())
				clientActionFailed();
			else
				actor.altUseSkill(skill, target);
			return;
		}

		// Если не можем кастовать, то использовать скилл позже
		if(actor.isActionsDisabled())
		{
			//if(!actor.isSkillDisabled(skill.getId()))
			setNextAction(nextAction.CAST, skill, target, forceUse, dontMove);
			clientActionFailed();
			return;
		}

		//_actor.stopMove(null);
		_forceUse = forceUse;
		_dontMove = dontMove;
		clearNextAction();
		setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}

	public static class ExecuteFollow implements Runnable
	{
		private L2Playable _player;
		private L2Character _target;
		private int _range;

		public ExecuteFollow(L2Playable player, L2Character target, int range)
		{
			_player = player;
			_target = target;
			_range = range;
		}

		public void run()
		{
			_player.followToCharacter(_target, _range, true);
		}
	}

	@Override
	public L2Playable getActor()
	{
		return (L2Playable) super.getActor();
	}
}