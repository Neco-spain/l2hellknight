package npc.model;

import java.util.concurrent.ScheduledFuture;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Location;

public class LematanInstance extends L2MonsterInstance
{
    public static class MinionSpawner
        implements Runnable
    {

        public void run()
        {
            try
            {
                if(!_boss.isDead() && _boss.getMinionList().countSpawnedMinions() <= 8)
                    _boss.getMinionList().spawnSingleMinionSync(_minionId);
                if(_boss.getMinionList().countSpawnedMinions() > 8 && LematanInstance._spawner != null)
                    LematanInstance._spawner.cancel(true);
            }
            catch(Throwable e)
            {
                e.printStackTrace();
            }
        }

        private LematanInstance _boss;
        private int _minionId;

        public MinionSpawner(LematanInstance boss, int minionId)
        {
            _boss = boss;
            _minionId = minionId;
        }
    }


    public LematanInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        isTeleported = false;
        teleportLoc = new Location(0x14c08, 0xfffcd0e8, -3367);
    }

    public boolean isFearImmune()
    {
        return true;
    }

    public void doDie(L2Character killer)
    {
        if(_spawner != null)
            _spawner.cancel(true);
        removeMinions();
        super.doDie(killer);
    }

    public void notifyMinionDied(L2MinionInstance minion)
    {
        _minionList.removeSpawnedMinion(minion);
    }

    public void decayMe()
    {
        if(_spawner != null)
            _spawner.cancel(true);
        removeMinions();
        super.decayMe();
    }

    public void setTeleported(boolean flag)
    {
        isTeleported = flag;
        clearAggroList(true);
        setSpawnedLoc(teleportLoc);
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        teleToLocation(teleportLoc);
        _spawner = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new MinionSpawner(this, 18634), 500L, 500L, flag);
    }

    public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean standUp, boolean directHp)
    {
        super.reduceCurrentHp(damage, attacker, null, awake, standUp, directHp, directHp);
        if(!isTeleported && getCurrentHp() <= (double)(getMaxHp() / 2))
            setTeleported(true);
    }

    private boolean isTeleported;
    @SuppressWarnings("unused")
	private static final int Lematan_Follower = 18634;
    @SuppressWarnings("unchecked")
	private static ScheduledFuture _spawner;
    private Location teleportLoc;

}
