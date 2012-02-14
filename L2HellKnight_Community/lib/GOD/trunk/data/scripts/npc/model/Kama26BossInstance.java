package npc.model;

import java.util.concurrent.ScheduledFuture;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class Kama26BossInstance extends L2ReflectionBossInstance
{
	private ScheduledFuture<?> _spawner;

	public Kama26BossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void notifyMinionDied(L2MinionInstance minion)
	{
		if(_minionList != null)
			_minionList.removeSpawnedMinion(minion);
		_spawner = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new MinionSpawner(), 60000, 60000, false);
	}

	@Override
	public void onSpawn()
	{
		setNewMinionList();
		_minionList.spawnSingleMinionSync(18556);
		super.onSpawn();
	}

	@Override
	public void doDie(L2Character killer)
	{
		if(_spawner != null)
			_spawner.cancel(true);
		super.doDie(killer);
	}

	public class MinionSpawner implements Runnable
	{
		public void run()
		{
			try
			{
				if(!Kama26BossInstance.this.isDead() && Kama26BossInstance.this.getTotalSpawnedMinionsInstances() == 0)
				{
					if(Kama26BossInstance.this.getMinionList() == null)
						Kama26BossInstance.this.new MinionMaintainTask().run();
					Kama26BossInstance.this.getMinionList().spawnSingleMinionSync(18556);
					Functions.npcSayCustomMessage(Kama26BossInstance.this, "Kama26Boss.helpme");
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
}