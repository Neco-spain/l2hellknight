package npc.model;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Rnd;

public class Kama73BossInstance extends L2ReflectionBossInstance
{
    public static class MinionSpawner
        implements Runnable
    {

        public void run()
        {
            try
            {
                if(!_boss.isDead() && _boss.getMinionList().countSpawnedMinions() < 10 && Rnd.chance(60))
                    _boss.getMinionList().spawnSingleMinionSync(_minionId);
            }
            catch(Throwable e)
            {
                e.printStackTrace();
            }
        }

        private Kama73BossInstance _boss;
        private int _minionId;

        public MinionSpawner(Kama73BossInstance boss, int minionId)
        {
            _boss = boss;
            _minionId = minionId;
        }
    }


    public Kama73BossInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        MINION_ID = 18578;
    }

    public void onSpawn()
    {
        _spawner = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new MinionSpawner(this, MINION_ID), Rnd.get(60000, 0x2bf20), 60000L, isDead());
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
