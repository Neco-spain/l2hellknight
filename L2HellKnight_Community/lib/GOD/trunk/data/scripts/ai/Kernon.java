package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;

public class Kernon extends Fighter
{

    public Kernon(L2Character actor)
    {
        super(actor);
        AI_TASK_DELAY = 1000;
        AI_TASK_ACTIVE_DELAY = 1000;
    }

    protected void onEvtAttacked(L2Character attacker, int damage)
    {
        L2NpcInstance actor = getActor();
        int z = actor.getZ();
        if(z > 4300 || z < 3900)
        {
            actor.teleToLocation(0x1bb0c, 16424, 3969);
            actor.setCurrentHp(actor.getMaxHp(), false);
        }
        super.onEvtAttacked(attacker, damage);
    }

    @SuppressWarnings("unused")
	private static final int z1 = 3900;
    @SuppressWarnings("unused")
	private static final int z2 = 4300;
}