package npc.model;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Rnd;

public class Kama63MinionInstance extends L2MinionInstance
{
    private class HealBoss
        implements Runnable
    {

        private static final boolean HealBoss = false;

		public void run()
        {
            if(Rnd.chance(50) && getLeader() != null && !getLeader().isDead() && getLeader().getCurrentHp() < (double)getLeader().getMaxHp())
                getLeader().setCurrentHp(getLeader().getMaxHp(), HealBoss);
            doDie(null);
        }

        final Kama63MinionInstance this$0;

        private HealBoss()
        {
            this$0 = Kama63MinionInstance.this;
        }

    }


    public Kama63MinionInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template, null);
    }

    public void onSpawn()
    {
        super.onSpawn();
        _healBossTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new HealBoss(), 30000L, 30000L, isDead());
    }

    public void decayMe()
    {
        if(_healBossTask != null)
            _healBossTask.cancel(true);
        super.decayMe();
    }

    public void doDie(L2Character killer)
    {
        if(_healBossTask != null)
            _healBossTask.cancel(true);
        if(killer != null)
            getLeader().setCurrentHp(getLeader().getCurrentHp() - 45000D, isDead());
        super.doDie(killer);
    }

    @SuppressWarnings("unchecked")
	private ScheduledFuture _healBossTask;

}
