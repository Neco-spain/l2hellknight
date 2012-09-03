package npc.model;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class Kama43BossInstance extends L2ReflectionBossInstance
{
    public static class MinionSpawner
        implements Runnable
    {

        public void run()
        {
            try
            {
                if(!_boss.isDead() && _boss.getMinionList().countSpawnedMinions() < 8)
                    _boss.getMinionList().spawnSingleMinionSync(_minionId);
            }
            catch(Throwable e)
            {
                e.printStackTrace();
            }
        }

        private Kama43BossInstance _boss;
        private int _minionId;

        public MinionSpawner(Kama43BossInstance boss, int minionId)
        {
            _boss = boss;
            _minionId = minionId;
        }
    }


    public Kama43BossInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        MINION_ID = 18563;
    }

    public void decayMe()
    {
        if(_spawner != null)
            _spawner.cancel(true);
        super.decayMe();
    }

    public void notifyMinionDied(L2MinionInstance minion)
    {
        _minionList.removeSpawnedMinion(minion);
    }

    public void doDie(L2Character killer)
    {
        if(_spawner != null)
            _spawner.cancel(true);
        super.doDie(killer);
    }

    public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean standUp, boolean directHp)
    {
        if(getCurrentHp() < (double)getMaxHp() * 0.59999999999999998D && _spawner == null)
            _spawner = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new MinionSpawner(this, MINION_ID), 15000L, 30000L, directHp);
        super.reduceCurrentHp(i, attacker, null, awake, standUp, directHp, directHp);
    }

    @SuppressWarnings("unchecked")
	private ScheduledFuture _spawner;
    private int MINION_ID;
}
