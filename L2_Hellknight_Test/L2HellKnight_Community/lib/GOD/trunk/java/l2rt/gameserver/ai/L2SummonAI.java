package l2rt.gameserver.ai;

import l2rt.Config;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Summon;

public class L2SummonAI extends L2PlayableAI
{
	public L2SummonAI(L2Summon actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionActive()
	{
		L2Summon actor = getActor();
		if(actor == null || !actor.isVisible())
			return;

		clearNextAction();

		if(actor.isPosessed())
		{
			actor.setRunning();
			if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
			return;
		}

		L2Player owner = actor.getPlayer();
		if(owner == null || owner.isAlikeDead() || actor.getDistance(owner) > 4000 || !owner.isConnected())
		{
			super.onIntentionActive();
			return;
		}

		if(actor.isFollow())
			setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
		else
			super.onIntentionActive();
	}

	@Override
	protected void thinkAttack(boolean checkRange)
	{
		L2Summon actor = getActor();
		if(actor == null)
			return;

		if(actor.isPosessed())
			setAttackTarget(actor.getPlayer());

		super.thinkAttack(checkRange);
	}

	@Override
	protected void onAttackFail()
	{
		L2Summon actor = getActor();
		if(actor != null)
		{
			actor.setFollowTarget(actor.getPlayer());
			actor.setFollowStatus(actor.isFollow(), true);
		}
	}

	@Override
	protected void onEvtThink()
	{
		L2Summon actor = getActor();
		if(actor == null)
			return;

		if(actor.isPosessed())
		{
			setAttackTarget(actor.getPlayer());
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
		}

		super.onEvtThink();
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2Summon actor = getActor();
		if(actor == null)
			return;
		if(attacker != null && actor.getPlayer().isDead() && !actor.isPosessed())
			Attack(attacker, false, false);
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	public L2Summon getActor()
	{
		return (L2Summon) super.getActor();
	}
}