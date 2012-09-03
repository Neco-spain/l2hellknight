package l2rt.gameserver.ai;

import l2rt.extensions.listeners.MethodCollection;
import l2rt.extensions.listeners.engine.DefaultListenerEngine;
import l2rt.extensions.listeners.engine.ListenerEngine;
import l2rt.extensions.listeners.events.AbstractAI.AbstractAINotifyEvent;
import l2rt.extensions.listeners.events.AbstractAI.AbstractAISetIntention;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Skill;
import l2rt.util.Location;

import java.util.logging.Logger;

public abstract class AbstractAI
{
	protected static final Logger _log = Logger.getLogger(AbstractAI.class.getName());

	private long _actor, _attack_target;
	private CtrlIntention _intention = CtrlIntention.AI_INTENTION_IDLE;

	protected AbstractAI(L2Character actor)
	{
		refreshActor(actor);
	}

	public void refreshActor(L2Character actor)
	{
		_actor = actor.getStoredId();
	}

	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention = intention;
		if(intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK)
			setAttackTarget(null);
	}

	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}

	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}

	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if(intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK)
			setAttackTarget(null);

		L2Character actor = getActor();
		if(actor == null)
			return;

		if(!actor.isVisible())
		{
			if(_intention == CtrlIntention.AI_INTENTION_IDLE)
				return;

			intention = CtrlIntention.AI_INTENTION_IDLE;
		}

		getListenerEngine().fireMethodInvoked(new AbstractAISetIntention(MethodCollection.AbstractAIsetIntention, this, new Object[] {
				intention, arg0, arg1 }));

		switch(intention)
		{
			case AI_INTENTION_IDLE:
				onIntentionIdle();
				break;
			case AI_INTENTION_ACTIVE:
				onIntentionActive();
				break;
			case AI_INTENTION_REST:
				onIntentionRest();
				break;
			case AI_INTENTION_ATTACK:
				onIntentionAttack((L2Character) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((L2Skill) arg0, (L2Character) arg1);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((L2Character) arg0, (Integer) arg1);
				break;
		}
	}

	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, new Object[] {});
	}

	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, new Object[] { arg0 });
	}

	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		notifyEvent(evt, new Object[] { arg0, arg1 });
	}

	public void notifyEvent(CtrlEvent evt, Object[] args)
	{
		L2Character actor = getActor();
		if(actor == null || !actor.isVisible())
			return;

		getListenerEngine().fireMethodInvoked(new AbstractAINotifyEvent(MethodCollection.AbstractAInotifyEvent, this, new Object[] {
				evt, args }));

		switch(evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) args[0], ((Number) args[1]).intValue());
				break;
			case EVT_CLAN_ATTACKED:
				onEvtClanAttacked((L2Character) args[0], (L2Character) args[1], ((Number) args[2]).intValue());
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) args[0], ((Number) args[1]).intValue());
				break;
			case EVT_READY_TO_ACT:
				onEvtReadyToAct();
				break;
			case EVT_ARRIVED:
				onEvtArrived();
				break;
			case EVT_ARRIVED_TARGET:
				onEvtArrivedTarget();
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((Location) args[0]);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) args[0]);
				break;
			case EVT_DEAD:
				onEvtDead((L2Character) args[0]);
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
			case EVT_SEE_SPELL:
				onEvtSeeSpell((L2Skill) args[0], (L2Character) args[1]);
				break;
			case EVT_SPAWN:
				onEvtSpawn();
				break;
		}
	}

	protected void clientActionFailed()
	{
		L2Character actor = getActor();
		if(actor != null && actor.isPlayer())
			actor.sendActionFailed();
	}

	/**
	 * Останавливает движение
	 * @param validate - рассылать ли ValidateLocation
	 */
	public void clientStopMoving(boolean validate)
	{
		L2Character actor = getActor();
		if(actor == null)
			return;
		actor.stopMove(validate);
	}

	/**
	 * Останавливает движение и рассылает ValidateLocation
	 */
	public void clientStopMoving()
	{
		L2Character actor = getActor();
		if(actor == null)
			return;
		actor.stopMove();
	}

	public L2Character getActor()
	{
		return L2ObjectsStorage.getAsCharacter(_actor);
	}

	public CtrlIntention getIntention()
	{
		return _intention;
	}

	public void setAttackTarget(L2Character target)
	{
		_attack_target = target != null ? target.getStoredId() : 0;
	}

	public L2Character getAttackTarget()
	{
		return L2ObjectsStorage.getAsCharacter(_attack_target);
	}

	/** Означает, что AI всегда включен, независимо от состояния региона */
	public boolean isGlobalAI()
	{
		return false;
	}

	public void setGlobalAggro(long value)
	{}

	public void setMaxPursueRange(int range)
	{}

	@Override
	public String toString()
	{
		return getL2ClassShortName() + " for " + getActor();
	}

	public String getL2ClassShortName()
	{
		return getClass().getName().replaceAll("^.*\\.(.*?)$", "$1");
	}

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionActive();

	protected abstract void onIntentionRest();

	protected abstract void onIntentionAttack(L2Character target);

	protected abstract void onIntentionCast(L2Skill skill, L2Character target);

	protected abstract void onIntentionPickUp(L2Object item);

	protected abstract void onIntentionInteract(L2Object object);

	protected abstract void onEvtThink();

	protected abstract void onEvtAttacked(L2Character attacker, int damage);

	protected abstract void onEvtClanAttacked(L2Character attacked_member, L2Character attacker, int damage);

	protected abstract void onEvtAggression(L2Character target, int aggro);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedTarget();

	protected abstract void onEvtArrivedBlocked(Location blocked_at_pos);

	protected abstract void onEvtForgetObject(L2Object object);

	protected abstract void onEvtDead(L2Character killer);

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting();

	protected abstract void onEvtSeeSpell(L2Skill skill, L2Character caster);

	protected abstract void onEvtSpawn();

	protected abstract void onIntentionFollow(L2Character target, Integer offset);

	private ListenerEngine<AbstractAI> listenerEngine;

	public ListenerEngine<AbstractAI> getListenerEngine()
	{
		if(listenerEngine == null)
			listenerEngine = new DefaultListenerEngine<AbstractAI>(this);
		return listenerEngine;
	}
}