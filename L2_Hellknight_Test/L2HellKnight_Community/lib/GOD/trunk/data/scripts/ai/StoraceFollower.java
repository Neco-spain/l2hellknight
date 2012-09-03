package ai;

import java.util.concurrent.ScheduledFuture;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * АИ для камалоки 63 уровня.
 * Каждые 30 секунд босс призывает миньона, который через 25 секунд совершает суицид и восстанавливает здоровье
 * боса.
 *
 * @author SYS
 */
public class StoraceFollower extends Fighter
{
	private static final int BOSS_ID = 18571;
	private static final int MINION_DIE_TIME = 25000;
	private long _wait_timeout = 0;
	private L2NpcInstance _boss;
	private boolean _spawned = false;
	ScheduledFuture<?> _dieTask = null;

	public StoraceFollower(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		_boss = findBoss(BOSS_ID);
	}

	@Override
	protected boolean thinkActive()
	{
		if(_boss == null)
		{
			_boss = findBoss(BOSS_ID);
		}
		else if(!_spawned)
		{
			_spawned = true;
			Functions.npcSayCustomMessage(_boss, "VenomousStorace");
			L2NpcInstance minion = getActor();
			minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _boss.getRandomHated(), Rnd.get(1, 100));
			_dieTask = ThreadPoolManager.getInstance().scheduleAi(new DieScheduleTimerTask(minion, _boss), MINION_DIE_TIME, false);
		}
		return super.thinkActive();
	}

	private L2NpcInstance findBoss(int npcId)
	{
		// Ищем боса не чаще, чем раз в 15 секунд, если по каким-то причинам его нету
		if(System.currentTimeMillis() < _wait_timeout)
		{
			return null;
		}
		_wait_timeout = System.currentTimeMillis() + 15000;
		L2NpcInstance minion = getActor();
		if(minion == null)
		{
			return null;
		}
		for(L2NpcInstance npc : L2World.getAroundNpc(minion))
		{
			if(npc.getNpcId() == npcId)
			{
				return npc;
			}
		}
		return null;
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_spawned = false;
		if(_dieTask != null)
		{
			_dieTask.cancel(true);
		}
		super.onEvtDead(killer);
	}

	public class DieScheduleTimerTask implements Runnable
	{
		L2NpcInstance _minion = null;
		L2NpcInstance _master = null;

		public DieScheduleTimerTask(L2NpcInstance minion, L2NpcInstance master)
		{
			_minion = minion;
			_master = master;
		}

		public void run()
		{
			try
			{
				if(_master != null && _minion != null && !_master.isDead() && !_minion.isDead())
				{
					_master.setCurrentHp(_master.getCurrentHp() + _minion.getCurrentHp() * 5, false);
				}
				Functions.npcSayCustomMessage(_minion, "Kama63Minion");
				_minion.doDie(_minion);
			}
			catch(Throwable t)
			{
			}
		}
	}
}