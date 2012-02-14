package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.TowerOfNaiaManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class TowerOfNaiaLock extends Fighter
{
	private static int count = 0;
	private static boolean spawn = true;
	private static int[][] locs = { { 16408, 243960, 11595 }, { 16248, 243848, 11595 }, { 16264, 243736, 11595 },
			{ 16280, 243608, 11595 }, { 16200, 243512, 11595 }, { 16424, 243416, 11595 }, { 16536, 243544, 11599 },
			{ 16584, 243656, 11595 }, { 16504, 243704, 11595 }, { 16408, 243640, 11595 }, { 16392, 243560, 11595 },
			{ 16392, 243400, 11595 }, { 16456, 243256, 11595 }, { 16408, 243192, 11595 }, { 16264, 243096, 11595 },
			{ 16072, 243192, 11595 }, { 16104, 243336, 11595 } };

	public TowerOfNaiaLock(L2Character actor)
	{
		super(actor);
		actor.setImobilised(true);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		if(spawn)
		{
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new spawnWard(), 90000, 90000, false);
			spawn = false;
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtSpawn()
	{
		TowerOfNaiaManager.registerRoofLock((L2MonsterInstance) getActor());
		super.onEvtSpawn();
	}

	class spawnWard implements Runnable
	{
		public void run()
		{
			if(count <= 10)
			{
				int i = Rnd.get(locs.length);
				L2MonsterInstance mob = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(18493));
				mob.setSpawnedLoc(new Location(locs[i]));
				mob.onSpawn();
				mob.spawnMe(new Location(locs[i]));
				count++;
			}
		}
	}
}