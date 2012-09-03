package l2rt.gameserver.ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.instancemanager.TowerOfNaiaManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.taskmanager.SpawnTaskManager;

public class NaiaSpore extends Fighter
{
	public NaiaSpore(L2Character actor)
	{
		super(actor);
	}

	public void notifyEpidosIndexReached()
	{
		L2MonsterInstance actor = (L2MonsterInstance) getActor();
		if(actor.isDead())
			SpawnTaskManager.getInstance().cancelSpawnTask(actor);
		getActor().moveToLocation(TowerOfNaiaManager.CENTRAL_COLUMN, 0, false);
		ThreadPoolManager.getInstance().scheduleGeneral(new DespawnTask(null), 3000);
	}

	protected void onEvtDead(L2Character killer)
	{
		TowerOfNaiaManager.handleEpidosIndex((L2MonsterInstance) getActor());
		super.onEvtDead(killer);
	}

	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		L2MonsterInstance actor = (L2MonsterInstance) getActor();
		if(TowerOfNaiaManager.isEpidosSpawned())
			actor.decayMe();
		else
			TowerOfNaiaManager.addSpore(actor);
	}

	private class DespawnTask implements Runnable
	{
		private DespawnTask(Object o)
		{}

		public void run()
		{
			getActor().decayMe();
		}
	}
}