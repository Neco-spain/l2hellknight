package l2rt.util;

import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.L2MinionData;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.tables.NpcTable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MinionList
{
	/** List containing the current spawned minions for this L2MonsterInstance */
	private final ConcurrentLinkedQueue<L2MinionInstance> _minionReferences;
	private final L2MonsterInstance _master;

	public MinionList(L2MonsterInstance master)
	{
		_minionReferences = new ConcurrentLinkedQueue<L2MinionInstance>();
		_master = master;
	}

	public int countSpawnedMinions()
	{
		return _minionReferences.size();
	}

	public boolean hasMinions()
	{
		return _minionReferences.size() > 0;
	}

	public ConcurrentLinkedQueue<L2MinionInstance> getSpawnedMinions()
	{
		return _minionReferences;
	}

	public void addSpawnedMinion(L2MinionInstance minion)
	{
		synchronized (_minionReferences)
		{
			_minionReferences.add(minion);
		}
	}

	public void removeSpawnedMinion(L2MinionInstance minion)
	{
		synchronized (_minionReferences)
		{
			_minionReferences.remove(minion);
		}
	}

	/**
	 *  Спавнит всех недостающих миньонов
	 */
	public void maintainMinions()
	{
		GArray<L2MinionData> minions = _master.getTemplate().getMinionData();

		synchronized (_minionReferences)
		{
			byte minionCount;
			int minionId;
			for(L2MinionData minion : minions)
			{
				minionCount = minion.getAmount();
				minionId = minion.getMinionId();

				for(L2MinionInstance m : _minionReferences)
					if(m.getNpcId() == minionId)
						minionCount--;

				for(int i = 0; i < minionCount; i++)
					spawnSingleMinion(minionId);
			}
		}
	}

	/**
	 *	Удаляет тех миньонов, которые еще живы
	 */
	public void maintainLonelyMinions()
	{
		synchronized (_minionReferences)
		{
			for(L2MinionInstance minion : getSpawnedMinions())
				if(!minion.isDead())
				{
					removeSpawnedMinion(minion);
					minion.deleteMe();
				}
		}
	}

	private void spawnSingleMinion(int minionid)
	{
		L2MinionInstance monster = new L2MinionInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(minionid), _master);
		monster.setReflection(_master.getReflection());
		if(_master.getChampion() == 2)
			monster.setChampion(1);
		monster.onSpawn();
		monster.setHeading(_master.getHeading());
		addSpawnedMinion(monster);
		monster.spawnMe(_master.getMinionPosition());
	}

	/**
	 * Same as spawnSingleMinion, but synchronized.<BR><BR>
	 * @param minionid The I2NpcTemplate Identifier of the Minion to spawn
	 */
	public void spawnSingleMinionSync(int minionid)
	{
		synchronized (_minionReferences)
		{
			spawnSingleMinion(minionid);
		}
	}
}