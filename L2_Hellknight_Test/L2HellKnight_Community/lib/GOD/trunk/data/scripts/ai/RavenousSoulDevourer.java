package ai;

import java.util.Iterator;
import l2rt.gameserver.ai.*;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.instances.L2NpcInstance;

public class RavenousSoulDevourer extends Fighter
{

    public RavenousSoulDevourer(L2Character actor)
    {
        super(actor);
        destroyedTumor = 32531;
        AI_TASK_DELAY = 1000;
        AI_TASK_ACTIVE_DELAY = 1000;
    }

    @SuppressWarnings("unchecked")
	protected void onEvtSpawn()
    {
        L2NpcInstance actor = getActor();
        if(actor == null)
            return;
        Iterator i$ = L2ObjectsStorage.getAllByNpcId(destroyedTumor, true).iterator();
        do
        {
            if(!i$.hasNext())
                break;
            L2NpcInstance npc = (L2NpcInstance)i$.next();
            if(npc.getReflectionId() == actor.getReflectionId())
                actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, Integer.valueOf(5000));
        } while(true);
        super.onEvtSpawn();
    }

    protected void onEvtAttacked(L2Character attacker, int damage)
    {
        L2NpcInstance actor = getActor();
        if(actor == null)
            return;
        if(attacker.getNpcId() == destroyedTumor)
        {
            actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(5000));
            attacker.addDamageHate(actor, 0, 2);
            startRunningTask(2000);
            setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
        }
        super.onEvtAttacked(attacker, damage);
    }

    private int destroyedTumor;
}