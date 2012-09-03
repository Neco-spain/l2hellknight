package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;

public class AndreasRoyalGuard extends Fighter
{

    public AndreasRoyalGuard(L2Character actor)
    {
        super(actor);
        AI_TASK_DELAY = 1000;
        AI_TASK_ACTIVE_DELAY = 1000;
    }

    protected void onEvtAttacked(L2Character attacker, int damage)
    {
        L2NpcInstance actor = getActor();
        if(actor.getCurrentHpPercents() <= 70D)
        {
            actor.doCast(SkillTable.getInstance().getInfo(4612, 9), attacker, true);
            actor.doDie(actor);
        }
        super.onEvtAttacked(attacker, damage);
    }
}