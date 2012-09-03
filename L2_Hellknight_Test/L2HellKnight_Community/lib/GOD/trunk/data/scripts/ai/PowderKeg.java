package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;

public class PowderKeg extends DefaultAI
{
	public PowderKeg(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
		actor.doCast(SkillTable.getInstance().getInfo(5714, 1), attacker, true);
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}
}