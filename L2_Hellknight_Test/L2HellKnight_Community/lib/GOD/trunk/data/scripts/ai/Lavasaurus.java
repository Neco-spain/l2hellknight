package ai;

import l2rt.gameserver.ai.Mystic;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;

public class Lavasaurus extends Mystic
{

    public Lavasaurus(L2Character actor)
    {
        super(actor);
        actor.setImobilised(true);
        AI_TASK_DELAY = 1000;
        AI_TASK_ACTIVE_DELAY = 1000;
    }

    protected boolean checkTarget(L2Character target, boolean canSelf, int range)
    {
        L2NpcInstance actor = getActor();
        if(actor != null && target != null && !actor.isInRange(target, actor.getAggroRange()))
        {
            target.removeFromHatelist(actor, true);
            return false;
        } else
        {
            return super.checkTarget(target, canSelf, range);
        }
    }

    protected boolean randomWalk()
    {
        return false;
    }
}