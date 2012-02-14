package npc.model;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.templates.L2NpcTemplate;

public class OlArioshInstance extends L2ReflectionBossInstance
{
    public class MinionSpawner
        implements Runnable
    {

        public void run()
        {
            try
            {
                if(!isDead() && getTotalSpawnedMinionsInstances() == 0)
                {
                    if(getMinionList() == null)
                    getMinionList().spawnSingleMinionSync(18556);
                    Functions.npcSayCustomMessage(OlArioshInstance.this, "OlAriosh.helpme", new Object[0]);
                }
            }
            catch(Throwable e)
            {
                e.printStackTrace();
            }
        }

        final OlArioshInstance this$0;

        public MinionSpawner()
        {
            this$0 = OlArioshInstance.this;
        }
    }


    public OlArioshInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void notifyMinionDied(L2MinionInstance minion)
    {
        if(_minionList != null)
            _minionList.removeSpawnedMinion(minion);
        _spawner = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new MinionSpawner(), 60000L, 60000L, false);
    }

    public void onSpawn()
    {
        setNewMinionList();
        _minionList.spawnSingleMinionSync(18556);
        super.onSpawn();
    }

    public void doDie(L2Character killer)
    {
        if(_spawner != null)
            _spawner.cancel(true);
        super.doDie(killer);
    }

    private ScheduledFuture _spawner;
}
