package net.sf.l2j.gameserver.ai;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStart;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.network.serverpackets.CharMoveToLocation;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.MoveToLocationInVehicle;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

abstract class AbstractAI
  implements Ctrl
{
  protected static final Logger _log = Logger.getLogger(AbstractAI.class.getName());
  protected final L2Character _actor;
  protected final L2Character.AIAccessor _accessor;
  protected CtrlIntention _intention = CtrlIntention.AI_INTENTION_IDLE;

  protected Object _intentionArg0 = null;

  protected Object _intentionArg1 = null;
  protected boolean _clientMoving;
  protected boolean _clientAutoAttacking;
  protected int _clientMovingToPawnOffset;
  private L2Object _target;
  private L2Character _castTarget;
  protected L2Character _attackTarget;
  protected L2Character _followTarget;
  L2Skill _skill;
  private int _moveToPawnTimeout;
  protected Future<?> _followTask = null;
  private static final int FOLLOW_INTERVAL = 1000;
  private static final int ATTACK_FOLLOW_INTERVAL = 500;

  protected AbstractAI(L2Character.AIAccessor accessor)
  {
    _accessor = accessor;

    _actor = accessor.getActor();
  }

  public L2Character getActor()
  {
    return _actor;
  }

  public CtrlIntention getIntention()
  {
    return _intention;
  }

  protected synchronized void setCastTarget(L2Character target)
  {
    _castTarget = target;
  }

  public L2Character getCastTarget()
  {
    return _castTarget;
  }

  protected synchronized void setAttackTarget(L2Character target)
  {
    _attackTarget = target;
  }

  public L2Character getAttackTarget()
  {
    return _attackTarget;
  }

  synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    _intention = intention;
    _intentionArg0 = arg0;
    _intentionArg1 = arg1;
  }

  public final void setIntention(CtrlIntention intention)
  {
    setIntention(intention, null, null);
  }

  public final void setIntention(CtrlIntention intention, Object arg0)
  {
    setIntention(intention, arg0, null);
  }

  public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    if ((!_actor.isVisible()) || (!_actor.hasAI())) return;

    if ((intention != CtrlIntention.AI_INTENTION_FOLLOW) && (intention != CtrlIntention.AI_INTENTION_ATTACK)) stopFollow();

    switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlIntention[intention.ordinal()])
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
      onIntentionAttack((L2Character)arg0);
      break;
    case 5:
      onIntentionCast((L2Skill)arg0, (L2Object)arg1);
      break;
    case 6:
      onIntentionMoveTo((L2CharPosition)arg0);
      break;
    case 7:
      onIntentionMoveToInABoat((L2CharPosition)arg0, (L2CharPosition)arg1);
      break;
    case 8:
      onIntentionFollow((L2Character)arg0);
      break;
    case 9:
      onIntentionPickUp((L2Object)arg0);
      break;
    case 10:
      onIntentionInteract((L2Object)arg0);
    }
  }

  public final void notifyEvent(CtrlEvent evt)
  {
    notifyEvent(evt, null, null);
  }

  public final void notifyEvent(CtrlEvent evt, Object arg0)
  {
    notifyEvent(evt, arg0, null);
  }

  public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
  {
    if ((!_actor.isVisible()) || (!_actor.hasAI())) return;

    switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlEvent[evt.ordinal()])
    {
    case 1:
      onEvtThink();
      break;
    case 2:
      onEvtAttacked((L2Character)arg0);
      break;
    case 3:
      onEvtAggression((L2Character)arg0, ((Number)arg1).intValue());
      break;
    case 4:
      onEvtStunned((L2Character)arg0);
      break;
    case 5:
      onEvtSleeping((L2Character)arg0);
      break;
    case 6:
      onEvtRooted((L2Character)arg0);
      break;
    case 7:
      onEvtConfused((L2Character)arg0);
      break;
    case 8:
      onEvtMuted((L2Character)arg0);
      break;
    case 9:
      onEvtReadyToAct();
      break;
    case 10:
      onEvtUserCmd(arg0, arg1);
      break;
    case 11:
      onEvtArrived();
      break;
    case 12:
      onEvtArrivedRevalidate();
      break;
    case 13:
      onEvtArrivedBlocked((L2CharPosition)arg0);
      break;
    case 14:
      onEvtForgetObject((L2Object)arg0);
      break;
    case 15:
      onEvtCancel();
      break;
    case 16:
      onEvtDead();
      break;
    case 17:
      onEvtFakeDeath();
      break;
    case 18:
      onEvtFinishCasting(); }  } 
  protected abstract void onIntentionIdle();

  protected abstract void onIntentionActive();

  protected abstract void onIntentionRest();

  protected abstract void onIntentionAttack(L2Character paramL2Character);

  protected abstract void onIntentionCast(L2Skill paramL2Skill, L2Object paramL2Object);

  protected abstract void onIntentionMoveTo(L2CharPosition paramL2CharPosition);

  protected abstract void onIntentionMoveToInABoat(L2CharPosition paramL2CharPosition1, L2CharPosition paramL2CharPosition2);

  protected abstract void onIntentionFollow(L2Character paramL2Character);

  protected abstract void onIntentionPickUp(L2Object paramL2Object);

  protected abstract void onIntentionInteract(L2Object paramL2Object);

  protected abstract void onEvtThink();

  protected abstract void onEvtAttacked(L2Character paramL2Character);

  protected abstract void onEvtAggression(L2Character paramL2Character, int paramInt);

  protected abstract void onEvtStunned(L2Character paramL2Character);

  protected abstract void onEvtSleeping(L2Character paramL2Character);

  protected abstract void onEvtRooted(L2Character paramL2Character);

  protected abstract void onEvtConfused(L2Character paramL2Character);

  protected abstract void onEvtMuted(L2Character paramL2Character);

  protected abstract void onEvtReadyToAct();

  protected abstract void onEvtUserCmd(Object paramObject1, Object paramObject2);

  protected abstract void onEvtArrived();

  protected abstract void onEvtArrivedRevalidate();

  protected abstract void onEvtArrivedBlocked(L2CharPosition paramL2CharPosition);

  protected abstract void onEvtForgetObject(L2Object paramL2Object);

  protected abstract void onEvtCancel();

  protected abstract void onEvtDead();

  protected abstract void onEvtFakeDeath();

  protected abstract void onEvtFinishCasting();

  protected void clientActionFailed() { if ((_actor instanceof L2PcInstance)) _actor.sendPacket(new ActionFailed());
  }

  protected void moveToPawn(L2Object pawn, int offset)
  {
    if (!_actor.isMovementDisabled())
    {
      if (offset < 10) offset = 10;

      boolean sendPacket = true;
      if ((_clientMoving) && (_target == pawn))
      {
        if (_clientMovingToPawnOffset == offset)
        {
          if (GameTimeController.getGameTicks() < _moveToPawnTimeout) return;
          sendPacket = false;
        }
        else if (_actor.isOnGeodataPath())
        {
          if (GameTimeController.getGameTicks() < _moveToPawnTimeout + 10) return;
        }

      }

      _clientMoving = true;
      _clientMovingToPawnOffset = offset;
      _target = pawn;
      _moveToPawnTimeout = GameTimeController.getGameTicks();
      _moveToPawnTimeout += 10;

      if ((pawn == null) || (_accessor == null)) return;

      _accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);

      if (!_actor.isMoving())
      {
        _actor.sendPacket(new ActionFailed());
        return;
      }

      if ((pawn instanceof L2Character)) {
        if (_actor.isOnGeodataPath())
        {
          _actor.broadcastPacket(new CharMoveToLocation(_actor));
          _clientMovingToPawnOffset = 0;
        }
        else if (sendPacket) {
          _actor.broadcastPacket(new MoveToPawn(_actor, (L2Character)pawn, offset));
        }
      }
      else _actor.broadcastPacket(new CharMoveToLocation(_actor));
    }
    else
    {
      _actor.sendPacket(new ActionFailed());
    }
  }

  protected void moveTo(int x, int y, int z)
  {
    if (!_actor.isMovementDisabled())
    {
      _clientMoving = true;
      _clientMovingToPawnOffset = 0;

      _accessor.moveTo(x, y, z);

      CharMoveToLocation msg = new CharMoveToLocation(_actor);
      _actor.broadcastPacket(msg);
    }
    else
    {
      _actor.sendPacket(new ActionFailed());
    }
  }

  protected void moveToInABoat(L2CharPosition destination, L2CharPosition origin)
  {
    if (!_actor.isMovementDisabled())
    {
      if (((L2PcInstance)_actor).getBoat() != null)
      {
        MoveToLocationInVehicle msg = new MoveToLocationInVehicle(_actor, destination, origin);
        _actor.broadcastPacket(msg);
      }

    }
    else
    {
      _actor.sendPacket(new ActionFailed());
    }
  }

  protected void clientStopMoving(L2CharPosition pos)
  {
    if (_actor.isMoving()) _accessor.stopMove(pos);

    _clientMovingToPawnOffset = 0;

    if ((_clientMoving) || (pos != null))
    {
      _clientMoving = false;

      StopMove msg = new StopMove(_actor);
      _actor.broadcastPacket(msg);

      if (pos != null)
      {
        StopRotation sr = new StopRotation(_actor.getObjectId(), pos.heading, 0);
        _actor.sendPacket(sr);
        _actor.broadcastPacket(sr);
      }
    }
  }

  protected void clientStoppedMoving()
  {
    if (_clientMovingToPawnOffset > 0)
    {
      _clientMovingToPawnOffset = 0;
      StopMove msg = new StopMove(_actor);
      _actor.broadcastPacket(msg);
    }
    _clientMoving = false;
  }

  public boolean isAutoAttacking()
  {
    return _clientAutoAttacking;
  }

  public void setAutoAttacking(boolean isAutoAttacking)
  {
    _clientAutoAttacking = isAutoAttacking;
  }

  public void clientStartAutoAttack()
  {
    if (!isAutoAttacking())
    {
      _actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
      setAutoAttacking(true);
    }
    AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
  }

  public void clientStopAutoAttack()
  {
    if ((_actor instanceof L2PcInstance))
    {
      if ((!AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor)) && (isAutoAttacking()))
        AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
    }
    else if (isAutoAttacking())
    {
      _actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
    }
    setAutoAttacking(false);
  }

  protected void clientNotifyDead()
  {
    Die msg = new Die(_actor);
    _actor.broadcastPacket(msg);

    _intention = CtrlIntention.AI_INTENTION_IDLE;
    _target = null;
    _castTarget = null;
    _attackTarget = null;

    stopFollow();
  }

  public void describeStateToPlayer(L2PcInstance player)
  {
    if (_clientMoving)
    {
      if ((_clientMovingToPawnOffset != 0) && (_followTarget != null))
      {
        MoveToPawn msg = new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset);
        player.sendPacket(msg);
      }
      else
      {
        CharMoveToLocation msg = new CharMoveToLocation(_actor);
        player.sendPacket(msg);
      }
    }
  }

  public synchronized void startFollow(L2Character target)
  {
    if (_followTask != null)
    {
      _followTask.cancel(false);
      _followTask = null;
    }

    _followTarget = target;
    _followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5L, 1000L);
  }

  public synchronized void startFollow(L2Character target, int range)
  {
    if (_followTask != null)
    {
      _followTask.cancel(false);
      _followTask = null;
    }

    _followTarget = target;
    _followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5L, 500L);
  }

  public synchronized void stopFollow()
  {
    if (_followTask != null)
    {
      _followTask.cancel(false);
      _followTask = null;
    }
    _followTarget = null;
  }

  protected L2Character getFollowTarget()
  {
    return _followTarget;
  }

  protected L2Object getTarget()
  {
    return _target;
  }

  protected synchronized void setTarget(L2Object target)
  {
    _target = target;
  }

  class FollowTask
    implements Runnable
  {
    protected int _range = 60;

    public FollowTask()
    {
    }

    public FollowTask(int range)
    {
      _range = range;
    }

    public void run()
    {
      try
      {
        if (_followTask == null) return;

        if (_followTarget == null)
        {
          stopFollow();
          if ((_actor instanceof L2Summon))
            ((L2Summon)_actor).setFollowStatus(false);
          setIntention(CtrlIntention.AI_INTENTION_IDLE);
          return;
        }
        if (!_actor.isInsideRadius(_followTarget, _range, true, false))
        {
          moveToPawn(_followTarget, _range);
        }
        if (_range <= 100)
        {
          _actor.sendPacket(new ActionFailed());
        }

      }
      catch (Throwable t)
      {
        AbstractAI._log.log(Level.WARNING, "", t);
      }
    }
  }
}