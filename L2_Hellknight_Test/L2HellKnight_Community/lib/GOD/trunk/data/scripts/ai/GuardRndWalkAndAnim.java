package ai;

import l2rt.gameserver.ai.Guard;
import l2rt.gameserver.model.L2Character;

public class GuardRndWalkAndAnim extends Guard
{
	public GuardRndWalkAndAnim(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;

		if(randomAnimation())
			return true;

		if(randomWalk())
			return true;

		return false;
	}
}