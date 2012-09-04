package ai;

import l2rt.gameserver.ai.*;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Rnd;

public class AngerOfSplendor extends Fighter
{

    public AngerOfSplendor(L2Character actor)
    {
        super(actor);
        AI_TASK_DELAY = 1000;
        AI_TASK_ACTIVE_DELAY = 1000;
    }

    protected void onEvtAttacked(L2Character attacker, int damage)
    {
        L2NpcInstance actor = getActor();
        if(actor == null)
            return;
        try
        {
            if(((L2MonsterInstance)actor).getChampion() <= 0 && actor.getCurrentHpPercents() > 50D && Rnd.chance(5))
            {
                L2Spawn spawn = new L2Spawn(NpcTable.getTemplate(21528));
                spawn.setLoc(actor.getLoc());
                L2NpcInstance npc = spawn.doSpawn(true);
                npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
                actor.decayMe();
                actor.doDie(actor);
                return;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        super.onEvtAttacked(attacker, damage);
    }
}