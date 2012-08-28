package net.sf.l2j.gameserver.ai;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;

public class L2PlayerAI extends L2CharacterAI
{
  private boolean _thinking;
  L2CharacterAI.IntentionCommand _nextIntention = null;

  public L2PlayerAI(L2Character.AIAccessor accessor)
  {
    super(accessor);
  }

  void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    _nextIntention = new L2CharacterAI.IntentionCommand(this, intention, arg0, arg1);
  }

  public L2CharacterAI.IntentionCommand getNextIntention()
  {
    return _nextIntention;
  }

  synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
  {
    if ((intention != CtrlIntention.AI_INTENTION_CAST) || ((arg0 != null) && (((L2Skill)arg0).isOffensive())))
    {
      _nextIntention = null;
      super.changeIntention(intention, arg0, arg1);
      return;
    }

    if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1))
    {
      super.changeIntention(intention, arg0, arg1);
      return;
    }

    saveNextIntention(_intention, _intentionArg0, _intentionArg1);
    super.changeIntention(intention, arg0, arg1);
  }

  protected void onEvtReadyToAct()
  {
    if (_nextIntention != null)
    {
      setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
      _nextIntention = null;
    }
    super.onEvtReadyToAct();
  }

  protected void onEvtCancel()
  {
    _nextIntention = null;
    super.onEvtCancel();
  }

  protected void onEvtFinishCasting()
  {
    if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
    {
      if (_nextIntention != null)
      {
        if (_nextIntention._crtlIntention != CtrlIntention.AI_INTENTION_CAST)
        {
          setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
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
    if (getIntention() != CtrlIntention.AI_INTENTION_REST)
    {
      changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
      setTarget(null);
      if (getAttackTarget() != null)
      {
        setAttackTarget(null);
      }
      clientStopMoving(null);
    }
  }

  protected void onIntentionActive()
  {
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onIntentionMoveTo(L2CharPosition pos)
  {
    if (getIntention() == CtrlIntention.AI_INTENTION_REST)
    {
      clientActionFailed();
      return;
    }

    if ((_actor.isAllSkillsDisabled()) || (_actor.isAttackingNow()))
    {
      clientActionFailed();
      saveNextIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos, null);
      return;
    }

    changeIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos, null);

    clientStopAutoAttack();

    _actor.abortAttack();

    moveTo(pos.x, pos.y, pos.z);
  }

  protected void clientNotifyDead()
  {
    _clientMovingToPawnOffset = 0;
    _clientMoving = false;

    super.clientNotifyDead();
  }

  private void thinkAttack()
  {
    L2Character target = getAttackTarget();
    if (target == null) return;
    if (checkTargetLostOrDead(target))
    {
      if (target != null)
      {
        setAttackTarget(null);
      }
      return;
    }
    if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange())) return;

    _accessor.doAttack(target);
  }

  private void thinkCast()
  {
    L2Character target = getCastTarget();
    if (Config.DEBUG) {
      _log.warning("L2PlayerAI: thinkCast -> Start");
    }
    if ((_skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND) && ((_actor instanceof L2PcInstance)))
    {
      if (maybeMoveToPosition(((L2PcInstance)_actor).getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
        return;
    }
    else
    {
      if (checkTargetLost(target))
      {
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
    if (_skill.getHitTime() > 50) clientStopMoving(null);

    L2Object oldTarget = _actor.getTarget();
    if (oldTarget != null)
    {
      if ((target != null) && (oldTarget != target)) _actor.setTarget(getCastTarget());

      _accessor.doCast(_skill);

      if ((target != null) && (oldTarget != target)) _actor.setTarget(oldTarget); 
    }
    else {
      _accessor.doCast(_skill);
    }
  }

  private void thinkPickUp()
  {
    if (_actor.isAllSkillsDisabled()) return;
    L2Object target = getTarget();
    if (checkTargetLost(target)) return;
    if (maybeMoveToPawn(target, 36)) return;
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    ((L2PcInstance.AIAccessor)_accessor).doPickupItem(target);
  }

  private void thinkInteract()
  {
    if (_actor.isAllSkillsDisabled()) return;
    L2Object target = getTarget();
    if (checkTargetLost(target)) return;
    if (maybeMoveToPawn(target, 36)) return;
    if (!(target instanceof L2StaticObjectInstance)) ((L2PcInstance.AIAccessor)_accessor).doInteract((L2Character)target);
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onEvtThink()
  {
    if ((_thinking) || (_actor.isAllSkillsDisabled())) return;

    _thinking = true;
    try
    {
      if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK) thinkAttack();
      else if (getIntention() == CtrlIntention.AI_INTENTION_CAST) thinkCast();
      else if (getIntention() == CtrlIntention.AI_INTENTION_PICK_UP) thinkPickUp();
      else if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT) thinkInteract();
    }
    finally
    {
      _thinking = false;
    }
  }

  protected void onEvtArrivedRevalidate()
  {
    if (Config.MOVE_BASED_KNOWNLIST) getActor().getKnownList().findObjects();
    super.onEvtArrivedRevalidate();
  }
}