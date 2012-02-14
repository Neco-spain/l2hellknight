package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;

public class ssq4m extends Fighter
{

    public ssq4m(L2Character actor)
    {
        super(actor);
    }

    public boolean isGlobalAI()
    {
        return true;
    }

    protected void onEvtSpawn()
    {
    }

    protected void onEvtDead(L2Character l2character)
    {
    }

    protected void onEvtAggression(L2Character attacker, int aggro)
    {
        l2rt.gameserver.model.instances.L2NpcInstance actor = getActor();
        if(actor != null && attacker != null)
        {
            attacker.teleToLocation(0xfffed72c, 0x341a6, -7119);
            super.onEvtAggression(attacker, aggro);
        }
    }
}