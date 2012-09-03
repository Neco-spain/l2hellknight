package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;

public class MontagnarFollower extends Fighter
{
	public MontagnarFollower(L2Character actor)
	{
		super(actor);
		actor.setIsInvul(true);
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{
		if(aggro < 10000000)
		{
			return;
		}
		super.onEvtAggression(attacker, aggro);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
	}
}