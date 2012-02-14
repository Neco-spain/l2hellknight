package ai;

import l2rt.gameserver.ai.Mystic;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.FuncMul;
import l2rt.util.Rnd;

/**
 * При спавне саммонят случайную охрану.
 * Защита прямо пропорциональна количеству охранников.
 * 
 * @author Diamond
 */
public class GraveRobberSummoner extends Mystic
{
	private static final int[] Servitors = { 22683, 22684, 22685, 22686 };

	private int _lastMinionCount = 0;

	public GraveRobberSummoner(L2Character actor)
	{
		super(actor);
	}

	@Override
	public void startAITask()
	{
		if(_aiTask == null)
		{
			L2MonsterInstance actor = (L2MonsterInstance) getActor();
			if(actor != null)
			{
				actor.removeMinions();
				actor.setNewMinionList();
				actor.getMinionList().spawnSingleMinionSync(Servitors[Rnd.get(Servitors.length)]);
				if(Rnd.chance(50))
					actor.getMinionList().spawnSingleMinionSync(Servitors[Rnd.get(Servitors.length)]);
				_lastMinionCount = actor.getMinionList().countSpawnedMinions();
				reapplyFunc(actor, _lastMinionCount);
			}
		}
		super.startAITask();
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2MonsterInstance actor = (L2MonsterInstance) getActor();
		if(actor == null)
			return;
		int minionCount = actor.getMinionList() == null ? 0 : actor.getMinionList().countSpawnedMinions();
		if(minionCount != _lastMinionCount)
		{
			_lastMinionCount = minionCount;
			reapplyFunc(actor, _lastMinionCount);
		}
		super.onEvtAttacked(attacker, damage);
	}

	private void reapplyFunc(L2NpcInstance actor, int minionCount)
	{
		actor.removeStatsOwner(this);
		if(minionCount > 0)
		{
			actor.addStatFunc(new FuncMul(Stats.MAGIC_DEFENCE, 0x30, this, minionCount));
			actor.addStatFunc(new FuncMul(Stats.POWER_DEFENCE, 0x30, this, minionCount));
		}
	}
}