package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;

public class L2SummonAI extends L2CharacterAI
{
	
	private boolean _thinking; // to prevent recursive thinking
	private boolean _startFollow = ((L2Summon) _actor).getFollowStatus();
	
	public L2SummonAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		_startFollow = false;
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		L2Summon summon = (L2Summon) _actor;
		if (_startFollow)
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
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
		return;
	}
	
	private void thinkCast()
	{
		L2Summon summon = (L2Summon) _actor;
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
		setIntention(AI_INTENTION_IDLE);
		_startFollow = val;
		_accessor.doCast(_skill);
		return;
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled())
			return;
		if (checkTargetLost(getTarget()))
			return;
		if (maybeMoveToPawn(getTarget(), 36))
			return;
		setIntention(AI_INTENTION_IDLE);
		((L2Summon.AIAccessor) _accessor).doPickupItem(getTarget());
		return;
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled())
			return;
		if (checkTargetLost(getTarget()))
			return;
		if (maybeMoveToPawn(getTarget(), 36))
			return;
		setIntention(AI_INTENTION_IDLE);
		return;
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isAllSkillsDisabled())
			return;
		_thinking = true;
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
				case AI_INTENTION_PICK_UP:
					thinkPickUp();
					break;
				case AI_INTENTION_INTERACT:
					thinkInteract();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (_actor.getAI().getIntention() != AI_INTENTION_ATTACK)
			((L2Summon) _actor).setFollowStatus(_startFollow);
	}
	
	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		switch (getIntention())
		{
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
			case AI_INTENTION_IDLE:
				((L2Summon) _actor).setFollowStatus(_startFollow);
		}
	}
	
	public void setStartFollowController(boolean val)
	{
		_startFollow = val;
	}
}
