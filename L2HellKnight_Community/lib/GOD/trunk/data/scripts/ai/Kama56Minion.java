package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;

public class Kama56Minion extends Fighter
{
	public Kama56Minion(L2Character actor)
	{
		super(actor);
		actor.setIsInvul(true);
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{
		if(aggro < 10000000)
			return;
		super.onEvtAggression(attacker, aggro);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{}
}