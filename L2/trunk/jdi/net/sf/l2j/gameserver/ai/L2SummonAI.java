package net.sf.l2j.gameserver.ai;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Summon.AIAccessor;

public class L2SummonAI extends L2CharacterAI
{
  private boolean _thinking;
  private boolean _startFollow = ((L2Summon)_actor).getFollowStatus();

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
    L2Summon summon = (L2Summon)_actor;
    if (_startFollow)
      setIntention(CtrlIntention.AI_INTENTION_FOLLOW, summon.getOwner());
    else
      super.onIntentionActive();
  }

  private void thinkAttack()
  {
    if (checkTargetLostOrDead(getAttackTarget()))
    {
      setAttackTarget(null);
      return;
    }
    if (maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange()))
      return;
    clientStopMoving(null);
    _accessor.doAttack(getAttackTarget());
  }

  private void thinkCast()
  {
    L2Summon summon = (L2Summon)_actor;
    if (checkTargetLost(getCastTarget()))
    {
      setCastTarget(null);
      return;
    }
    boolean val = _startFollow;
    if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
      return;
    clientStopMoving(null);
    summon.setFollowStatus(false);
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    _startFollow = val;
    _accessor.doCast(_skill);
  }

  private void thinkPickUp()
  {
    if (_actor.isAllSkillsDisabled())
      return;
    if (checkTargetLost(getTarget()))
      return;
    if (maybeMoveToPawn(getTarget(), 36))
      return;
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
    ((L2Summon.AIAccessor)_accessor).doPickupItem(getTarget());
  }

  private void thinkInteract()
  {
    if (_actor.isAllSkillsDisabled())
      return;
    if (checkTargetLost(getTarget()))
      return;
    if (maybeMoveToPawn(getTarget(), 36))
      return;
    setIntention(CtrlIntention.AI_INTENTION_IDLE);
  }

  protected void onEvtThink()
  {
    if ((_thinking) || (_actor.isAllSkillsDisabled()))
      return;
    _thinking = true;
    try
    {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlIntention[getIntention().ordinal()])
      {
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
    finally
    {
      _thinking = false;
    }
  }

  protected void onEvtFinishCasting()
  {
    if (_actor.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
      ((L2Summon)_actor).setFollowStatus(_startFollow);
  }

  public void notifyFollowStatusChange()
  {
    _startFollow = (!_startFollow);
    switch (1.$SwitchMap$net$sf$l2j$gameserver$ai$CtrlIntention[getIntention().ordinal()])
    {
    case 5:
    case 6:
    case 7:
      ((L2Summon)_actor).setFollowStatus(_startFollow);
    }
  }

  public void setStartFollowController(boolean val)
  {
    _startFollow = val;
  }
}