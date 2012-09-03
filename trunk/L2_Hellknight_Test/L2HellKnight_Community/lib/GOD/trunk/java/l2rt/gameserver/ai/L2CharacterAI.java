package l2rt.gameserver.ai;

import l2rt.gameserver.ai.L2PlayableAI.nextAction;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.entity.vehicle.L2Vehicle;
import l2rt.gameserver.network.serverpackets.Die;
import l2rt.util.Location;

public class L2CharacterAI extends AbstractAI
{
	public L2CharacterAI(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionIdle()
	{
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionActive()
	{
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		setAttackTarget(target);
		changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Character target)
	{
		setAttackTarget(target);
		changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		onEvtThink();
	}

	@Override
	protected void onIntentionFollow(L2Character target, Integer offset)
	{
		changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, offset);
		L2Character actor = getActor();
		if(actor != null)
			actor.followToCharacter(target, offset, false);
	}

	@Override
	protected void onIntentionInteract(L2Object object)
	{}

	@Override
	protected void onIntentionPickUp(L2Object item)
	{}

	@Override
	protected void onIntentionRest()
	{}

	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		L2Character actor = getActor();
		if(actor != null && actor.isPlayer())
		{
			// Приводит к застреванию в стенах:
			//if(actor.isInRange(blocked_at_pos, 1000))
			//	actor.setLoc(blocked_at_pos, true);
			// Этот способ надежнее:
			Location loc = ((L2Player) actor).getLastServerPosition();
			if(loc != null)
				actor.setLoc(loc, true);
			actor.stopMove();
		}
		onEvtThink();
	}

	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		L2Character actor = getActor();
		if(actor == null || object == null)
			return;

		if(actor.isAttackingNow() && getAttackTarget() == object)
			actor.abortAttack(true, true);

		if(actor.isCastingNow() && getAttackTarget() == object)
			actor.abortCast(true);

		if(getAttackTarget() == object)
			setAttackTarget(null);

		if(actor.getTargetId() == object.getObjectId())
			actor.setTarget(null);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2Character actor = getActor();
		if(actor != null)
		{
			actor.abortAttack(true, true);
			actor.abortCast(true);
			actor.stopMove();
			actor.broadcastPacket(new Die(actor));
		}
		setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtFakeDeath()
	{
		clientStopMoving();
		setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2Character actor = getActor();
		if(actor != null)
			actor.startAttackStanceTask();
		if(attacker != null)
			attacker.startAttackStanceTask();
	}

	@Override
	protected void onEvtClanAttacked(L2Character attacked_member, L2Character attacker, int damage)
	{}

	public void Attack(L2Object target, boolean forceUse, boolean dontMove)
	{
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	public void Cast(L2Skill skill, L2Character target)
	{
		Cast(skill, target, false, false);
	}

	public void Cast(L2Skill skill, L2Character target, boolean forceUse, boolean dontMove)
	{
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	@Override
	protected void onEvtThink()
	{}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{}

	@Override
	protected void onEvtFinishCasting()
	{}

	@Override
	protected void onEvtReadyToAct()
	{}

	@Override
	protected void onEvtArrived()
	{
		L2Character actor = getActor();
		if(actor != null && actor.isVehicle())
			((L2Vehicle) actor).VehicleArrived();
	}

	@Override
	protected void onEvtArrivedTarget()
	{}

	@Override
	protected void onEvtSeeSpell(L2Skill skill, L2Character caster)
	{}

	@Override
	protected void onEvtSpawn()
	{}

	public void stopAITask()
	{}

	public void startAITask()
	{}

	public void setNextAction(nextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{}

	public void clearTasks()
	{}

	public void clearNextAction()
	{}

	public void teleportHome(boolean clearAggro)
	{}

	public void checkAggression(L2Character target)
	{}

	public boolean isActive()
	{
		return true;
	}

	public boolean isGlobalAggro()
	{
		return true;
	}

	public boolean canSeeInSilentMove(L2Playable target)
	{
		return true;
	}

	public void addTaskMove(Location loc, boolean pathfind)
	{}

	public void addTaskAttack(L2Character target)
	{}
}