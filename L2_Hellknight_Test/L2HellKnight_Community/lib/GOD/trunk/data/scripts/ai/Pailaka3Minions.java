package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;

public class Pailaka3Minions extends Fighter
{

    public Pailaka3Minions(L2Character actor)
    {
        super(actor);
        actor.setImobilised(true);
    }

    protected boolean randomAnimation()
    {
        return false;
    }

    protected boolean randomWalk()
    {
        return false;
    }
}