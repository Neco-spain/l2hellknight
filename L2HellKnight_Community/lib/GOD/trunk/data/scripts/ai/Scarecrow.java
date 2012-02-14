package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class Scarecrow extends DefaultAI
{
	public Scarecrow(L2Character actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		return true;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{}
}