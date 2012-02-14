package npc.model;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class Kama36BossInstance extends L2ReflectionBossInstance
{
    public static class MinionSpawner
        implements Runnable
    {

        public void run()
        {
            try
            {
                if(!_boss.isDead())
                    _boss.getMinionList().spawnSingleMinionSync(_minionId);
            }
            catch(Throwable e)
            {
                e.printStackTrace();
            }
        }

        private Kama36BossInstance _boss;
        private int _minionId;

        public MinionSpawner(Kama36BossInstance boss, int minionId)
        {
            _boss = boss;
            _minionId = minionId;
        }
    }


    public Kama36BossInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        MINION_ID = 18560;
    }

    public void onSpawn()
    {
        _spawner = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new MinionSpawner(this, MINION_ID), 0x493e0L, 30000L, isDead());
        super.onSpawn();
    }

    public void notifyMinionDied(L2MinionInstance minion)
    {
        _minionList.removeSpawnedMinion(minion);
    }

    public void decayMe()
    {
        if(_spawner != null)
            _spawner.cancel(true);
        super.decayMe();
    }

    public void doDie(L2Character killer)
    {
        if(_spawner != null)
            _spawner.cancel(true);
        super.doDie(killer);
    }

    private ScheduledFuture _spawner;
    private int MINION_ID;
}
