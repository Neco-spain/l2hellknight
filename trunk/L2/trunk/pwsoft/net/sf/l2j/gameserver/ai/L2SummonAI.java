package net.sf.l2j.gameserver.ai;

import java.util.concurrent.ScheduledFuture;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Summon.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Rnd;

public class L2SummonAI extends L2CharacterAI
{
  private boolean _thinking;
  private boolean _startFollow = _actor.getFollowStatus();
  private RunOnAttacked _runOnAttacked;
  private ScheduledFuture<?> _runOnAttackedTask;

  public L2SummonAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
  }

  protected void onIntentionIdle()
  {
    stopFollow();
    _startFollow = false;
    onIntentionActive();
  }

  protected void onIntentionActive()
  {
    L2Summon summon = _actor.getL2Summon();
    if (_startFollow)
      setIntention(CtrlIntention.AI_INTENTION_FOLLOW, summon.getOwner());
    else
      super.onIntentionActive();
  }

  private void thinkAttack()
  {
    if (checkTargetLostOrDead(getAttackTarget())) {
      setAttackTarget(null);
      return;
    }
    if (maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange())) {
      return;
    }
    clientStopMoving(null);

    if (_actor.getOwner() != null) {
      _actor.getOwner().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getAttackTarget());
    }

    _accessor.doAttack(getAttackTarget());
  }

  private void thinkCast()
  {
    L2Summon summon = _actor.getL2Summon();
    if (checkTargetLost(getCastTarget())) {
      setCastTarget(null);
      return;
    }
    boolean val = _startFollow;
    if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill))) {
      return;
    }
    clientStopMoving(null);
    summon.setFollowStatus(false);
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    _startFollow = val;
    _accessor.doCast(_skill);
  }

  private void thinkPickUp() {
    if (checkTargetLost(getTarget())) {
      return;
    }
    if (maybeMoveToPawn(getTarget(), 36)) {
      return;
    }
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    ((L2Summon.AIAccessor)_accessor).doPickupItem(getTarget());
  }

  private void thinkInteract()
  {
    if (_actor.isAllSkillsDisabled()) {
      return;
    }
    if (checkTargetLost(getTarget())) {
      return;
    }
    if (maybeMoveToPawn(getTarget(), 36)) {
      return;
    }
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onEvtThink()
  {
    if ((_thinking) || (_actor.isAllSkillsDisabled())) {
      return;
    }
    _thinking = true;
    try {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlIntention[getIntention().ordinal()]) {
      case 1:
        thinkAttack();
        break;
      case 2:
        thinkCast();
        break;
      case 3:
        thinkPickUp();
        break;
      case 4:
        thinkInteract();
      }
    }
    finally {
      _thinking = false;
    }
  }

  protected void onEvtFinishCasting()
  {
    if (_actor.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
      _actor.setFollowStatus(_startFollow);
  }

  public void notifyFollowStatusChange()
  {
    _startFollow = (!_startFollow);
    switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlIntention[getIntention().ordinal()]) {
    case 5:
    case 6:
    case 7:
      _actor.setFollowStatus(_startFollow);
    }
  }

  public void setStartFollowController(boolean val) {
    _startFollow = val;
  }

  protected void onEvtAttacked(L2Character attacker)
  {
    L2Summon actor = _actor.getL2Summon();
    if (actor == null) {
      return;
    }

    if (_runOnAttacked != null) {
      _runOnAttacked.setAttacker(attacker);
    }

    if (actor.getOwner() != null) {
      actor.getOwner().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getAttackTarget());
    }

    if ((_runOnAttacked == null) && ((_intention == CtrlIntention.AI_INTENTION_FOLLOW) || (_intention == CtrlIntention.AI_INTENTION_IDLE) || (_intention == CtrlIntention.AI_INTENTION_ACTIVE)) && (!_clientMoving)) {
      if (_runOnAttacked == null) {
        _runOnAttacked = new RunOnAttacked(null);
        _runOnAttacked.setAttacker(attacker);
      }

      if (_runOnAttackedTask == null) {
        _runOnAttackedTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(_runOnAttacked, 0L, 500L);
      }
    }

    super.onEvtAttacked(attacker);
  }
  private class RunOnAttacked implements Runnable {
    private L2Character _attacker;
    private long _lastAttack;

    private RunOnAttacked() {  }

    public void run() { L2Summon actor = _actor.getL2Summon();
      if (actor == null) {
        return;
      }

      if ((_attacker != null) && (actor.getOwner() != null) && (_lastAttack + 20000L > System.currentTimeMillis()) && ((_intention == CtrlIntention.AI_INTENTION_FOLLOW) || (_intention == CtrlIntention.AI_INTENTION_IDLE) || (_intention == CtrlIntention.AI_INTENTION_ACTIVE))) {
        if (!_clientMoving) {
          int posX = actor.getOwner().getX();
          int posY = actor.getOwner().getY();
          int posZ = actor.getOwner().getZ();

          int side = Rnd.get(1, 6);
          switch (side) {
          case 1:
            posX += 30;
            posY += 140;
            break;
          case 2:
            posX += 150;
            posY += 50;
            break;
          case 3:
            posX += 70;
            posY -= 100;
            break;
          case 4:
            posX += 5;
            posY -= 100;
            break;
          case 5:
            posX -= 150;
            posY -= 20;
            break;
          case 6:
            posX -= 100;
            posY += 50;
          }

          actor.setRunning();
          actor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, actor.calcHeading(posX, posY)));
        }
      } else {
        _attacker = null;
        if (_runOnAttackedTask != null) {
          _runOnAttackedTask.cancel(true);
        }

        L2SummonAI.access$102(L2SummonAI.this, null);
        L2SummonAI.access$202(L2SummonAI.this, null);
      } }

    public void setAttacker(L2Character attacker)
    {
      _attacker = attacker;
      _lastAttack = System.currentTimeMillis();
    }
  }
}