package net.sf.l2j.gameserver.ai;

import java.util.EmptyStackException;
import java.util.Stack;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;

public class L2PlayerAI extends L2CharacterAI
{
  private boolean _thinking;
  private Stack<IntentionCommand> _interuptedIntentions = new Stack();

  public L2PlayerAI(L2Character.AIAccessor accessor) {
    super(accessor);
  }

  synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    if (intention != CtrlIntention.AI_INTENTION_CAST) {
      super.changeIntention(intention, arg0, arg1);
      return;
    }

    if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1)) {
      super.changeIntention(intention, arg0, arg1);
      return;
    }

    _interuptedIntentions.push(new IntentionCommand(_intention, _intentionArg0, _intentionArg1));
    super.changeIntention(intention, arg0, arg1);
  }

  protected void onEvtFinishCasting()
  {
    if ((_skill != null) && (_skill.isOffensive())) {
      _interuptedIntentions.clear();
    }

    if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
    {
      if (!_interuptedIntentions.isEmpty()) {
        IntentionCommand cmd = null;
        try {
          cmd = (IntentionCommand)_interuptedIntentions.pop();
        }
        catch (EmptyStackException ese)
        {
        }

        if ((cmd != null) && (cmd._crtlIntention != CtrlIntention.AI_INTENTION_CAST))
        {
          setIntention(cmd._crtlIntention, cmd._arg0, cmd._arg1);
        }
        else setIntention(CtrlIntention.AI_INTENTION_IDLE);

      }
      else
      {
        setIntention(CtrlIntention.AI_INTENTION_IDLE);
      }
    }
  }

  protected void onIntentionRest()
  {
    if (getIntention() != CtrlIntention.AI_INTENTION_REST) {
      changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
      setTarget(null);
      if (getAttackTarget() != null) {
        setAttackTarget(null);
      }
      clientStopMoving(null);
    }
  }

  protected void onIntentionActive()
  {
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void clientNotifyDead()
  {
    _clientMovingToPawnOffset = 0;
    _clientMoving = false;

    super.clientNotifyDead();
  }

  private void thinkAttack() {
    L2Character target = getAttackTarget();
    if (target == null) {
      return;
    }
    if (checkTargetLostOrDead(target)) {
      if (target != null)
      {
        setAttackTarget(null);
      }
      clientActionFailed();
      return;
    }
    if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange())) {
      clientActionFailed();
      return;
    }
    _actor.checkNextLoc();
    if (_actor.isAttackingDisabled())
    {
      clientActionFailed();
      return;
    }

    _accessor.doAttack(target);
  }

  private void thinkCast()
  {
    L2Character target = getCastTarget();

    if ((_skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SIGNET_GROUND) && (_actor.isPlayer())) {
      if (maybeMoveToPosition(_actor.getPlayer().getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
        return;
    }
    else {
      if (checkTargetLost(target)) {
        if ((_skill.isOffensive()) && (getAttackTarget() != null))
        {
          setCastTarget(null);
        }
        return;
      }

      if ((target != null) && (maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))) {
        return;
      }
    }

    if (_skill.getHitTime() > 50) {
      clientStopMoving(null);
    }

    L2Object oldTarget = _actor.getTarget();
    if (oldTarget != null)
    {
      if ((target != null) && (oldTarget != target)) {
        _actor.setTarget(getCastTarget());
      }

      _accessor.doCast(_skill);

      if ((target != null) && (oldTarget != target))
        _actor.setTarget(oldTarget);
    }
    else {
      _accessor.doCast(_skill);
    }
  }

  private void thinkPickUp()
  {
    if ((_actor.isAllSkillsDisabled()) || (_actor.isMovementDisabled())) {
      return;
    }
    L2Object target = getTarget();
    if (checkTargetLost(target)) {
      return;
    }
    if (maybeMoveToPawn(target, 36)) {
      return;
    }
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    ((L2PcInstance.AIAccessor)_accessor).doPickupItem(target);
  }

  private void thinkInteract()
  {
    if (_actor.isAllSkillsDisabled()) {
      return;
    }
    L2Object target = getTarget();
    if (checkTargetLost(target)) {
      return;
    }
    if (maybeMoveToPawn(target, 36)) {
      return;
    }
    if (!(target instanceof L2StaticObjectInstance)) {
      ((L2PcInstance.AIAccessor)_accessor).doInteract((L2Character)target);
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
      if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
        thinkAttack();
      else if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
        thinkCast();
      else if (getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
        thinkPickUp();
      else if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT)
        thinkInteract();
    }
    finally {
      _thinking = false;
    }
  }

  protected void onEvtArrivedRevalidate()
  {
    ThreadPoolManager.getInstance().executeAi(new ObjectKnownList.KnownListAsynchronousUpdateTask(_actor), true);
    super.onEvtArrivedRevalidate();
  }

  static class IntentionCommand
  {
    protected CtrlIntention _crtlIntention;
    protected Object _arg0;
    protected Object _arg1;

    protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
    {
      _crtlIntention = pIntention;
      _arg0 = pArg0;
      _arg1 = pArg1;
    }
  }
}