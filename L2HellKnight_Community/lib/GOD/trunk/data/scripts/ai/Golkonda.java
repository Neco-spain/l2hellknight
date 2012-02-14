package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;

public class Golkonda extends Fighter
{
	private static final int z1 = 6900;
	private static final int z2 = 7500;
	public Golkonda(L2Character actor)
	{
		super(actor);
		this.AI_TASK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 1000;
	}
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		int z = actor.getZ();
		if ((z > 7500) || (z < 6900))
		{
			actor.teleToLocation(116313, 15896, 6999);
			actor.setCurrentHp(actor.getMaxHp(), false);
		}
		super.onEvtAttacked(attacker, damage);
	}
}