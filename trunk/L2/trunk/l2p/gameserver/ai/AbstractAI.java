package l2p.gameserver.ai;

import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.actor.listener.CharListenerList;
import l2p.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAI extends RunnableImpl
{
  protected static final Logger _log = LoggerFactory.getLogger(AbstractAI.class);
  protected final Creature _actor;
  private HardReference<? extends Creature> _attackTarget = HardReferences.emptyRef();

  private CtrlIntention _intention = CtrlIntention.AI_INTENTION_IDLE;

  protected AbstractAI(Creature actor)
  {
    _actor = actor;
  }

  public void runImpl() throws Exception
  {
  }

  public void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
    _intention = intention;
    if ((intention != CtrlIntention.AI_INTENTION_CAST) && (intention != CtrlIntention.AI_INTENTION_ATTACK))
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
    if ((intention != CtrlIntention.AI_INTENTION_CAST) && (intention != CtrlIntention.AI_INTENTION_ATTACK)) {
      setAttackTarget(null);
    }
    Creature actor = getActor();

    if (!actor.isVisible())
    {
      if (_intention == CtrlIntention.AI_INTENTION_IDLE) {
        return;
      }
      intention = CtrlIntention.AI_INTENTION_IDLE;
    }

    actor.getListeners().onAiIntention(intention, arg0, arg1);

    switch (1.$SwitchMap$l2p$gameserver$ai$CtrlIntention[intention.ordinal()])
    {
    case 1:
      onIntentionIdle();
      break;
    case 2:
      onIntentionActive();
      break;
    case 3:
      onIntentionRest();
      break;
    case 4:
      onIntentionAttack((Creature)arg0);
      break;
    case 5:
      onIntentionCast((Skill)arg0, (Creature)arg1);
      break;
    case 6:
      onIntentionPickUp((GameObject)arg0);
      break;
    case 7:
      onIntentionInteract((GameObject)arg0);
      break;
    case 8:
      onIntentionFollow((Creature)arg0, (Integer)arg1);
      break;
    case 9:
      onIntentionCoupleAction((Player)arg0, (Integer)arg1);
    }
  }

  public final void notifyEvent(CtrlEvent evt)
  {
    notifyEvent(evt, new Object[0]);
  }

  public final void notifyEvent(CtrlEvent evt, Object arg0)
  {
    notifyEvent(evt, new Object[] { arg0 });
  }

  public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
  {
    notifyEvent(evt, new Object[] { arg0, arg1 });
  }

  public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1, Object arg2)
  {
    notifyEvent(evt, new Object[] { arg0, arg1, arg2 });
  }

  public void notifyEvent(CtrlEvent evt, Object[] args)
  {
    Creature actor = getActor();
    if ((actor == null) || (!actor.isVisible())) {
      return;
    }
    actor.getListeners().onAiEvent(evt, args);

    switch (1.$SwitchMap$l2p$gameserver$ai$CtrlEvent[evt.ordinal()])
    {
    case 1:
      onEvtThink();
      break;
    case 2:
      onEvtAttacked((Creature)args[0], ((Number)args[1]).intValue());
      break;
    case 3:
      onEvtClanAttacked((Creature)args[0], (Creature)args[1], ((Number)args[2]).intValue());
      break;
    case 4:
      onEvtAggression((Creature)args[0], ((Number)args[1]).intValue());
      break;
    case 5:
      onEvtReadyToAct();
      break;
    case 6:
      onEvtArrived();
      break;
    case 7:
      onEvtArrivedTarget();
      break;
    case 8:
      onEvtArrivedBlocked((Location)args[0]);
      break;
    case 9:
      onEvtForgetObject((GameObject)args[0]);
      break;
    case 10:
      onEvtDead((Creature)args[0]);
      break;
    case 11:
      onEvtFakeDeath();
      break;
    case 12:
      onEvtFinishCasting();
      break;
    case 13:
      onEvtSeeSpell((Skill)args[0], (Creature)args[1]);
      break;
    case 14:
      onEvtSpawn();
      break;
    case 15:
      onEvtDeSpawn();
      break;
    case 16:
      onEvtTimer(((Number)args[0]).intValue(), args[1], args[2]);
    }
  }

  protected void clientActionFailed()
  {
    Creature actor = getActor();
    if ((actor != null) && (actor.isPlayer()))
      actor.sendActionFailed();
  }

  public void clientStopMoving(boolean validate)
  {
    Creature actor = getActor();
    actor.stopMove(validate);
  }

  public void clientStopMoving()
  {
    Creature actor = getActor();
    actor.stopMove();
  }

  public Creature getActor()
  {
    return _actor;
  }

  public CtrlIntention getIntention()
  {
    return _intention;
  }

  public void setAttackTarget(Creature target)
  {
    _attackTarget = (target == null ? HardReferences.emptyRef() : target.getRef());
  }

  public Creature getAttackTarget()
  {
    return (Creature)_attackTarget.get();
  }

  public boolean isGlobalAI()
  {
    return false;
  }

  public String toString()
  {
    return getClass().getSimpleName() + " for " + getActor();
  }

  protected abstract void onIntentionIdle();

  protected abstract void onIntentionActive();

  protected abstract void onIntentionRest();

  protected abstract void onIntentionAttack(Creature paramCreature);

  protected abstract void onIntentionCast(Skill paramSkill, Creature paramCreature);

  protected abstract void onIntentionPickUp(GameObject paramGameObject);

  protected abstract void onIntentionInteract(GameObject paramGameObject);

  protected abstract void onIntentionCoupleAction(Player paramPlayer, Integer paramInteger);

  protected abstract void onEvtThink();

  protected abstract void onEvtAttacked(Creature paramCreature, int paramInt);

  protected abstract void onEvtClanAttacked(Creature paramCreature1, Creature paramCreature2, int paramInt);

  protected abstract void onEvtAggression(Creature paramCreature, int paramInt);

  protected abstract void onEvtReadyToAct();

  protected abstract void onEvtArrived();

  protected abstract void onEvtArrivedTarget();

  protected abstract void onEvtArrivedBlocked(Location paramLocation);

  protected abstract void onEvtForgetObject(GameObject paramGameObject);

  protected abstract void onEvtDead(Creature paramCreature);

  protected abstract void onEvtFakeDeath();

  protected abstract void onEvtFinishCasting();

  protected abstract void onEvtSeeSpell(Skill paramSkill, Creature paramCreature);

  protected abstract void onEvtSpawn();

  public abstract void onEvtDeSpawn();

  protected abstract void onIntentionFollow(Creature paramCreature, Integer paramInteger);

  protected abstract void onEvtTimer(int paramInt, Object paramObject1, Object paramObject2);
}