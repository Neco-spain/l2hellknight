package l2rt.gameserver.model.entity;

import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2PathfinderInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class KamalokaNightmare extends Reflection
{
	public static final int TIME_LIMIT = 1200000;
	public static final int COLLAPSE_TIME = 1800000;

	public static final int KAMALOKA_ESSENCE = 13002;

	private int _player;
	private Timer _pathfinderTimer;
	private TimerTask _pathfinderTimerTask;
	private HashMap<L2NpcTemplate, Integer> counter = new HashMap<L2NpcTemplate, Integer>();

	public KamalokaNightmare(L2Player player)
	{
		super("Kamaloka Nightmare");
		_player = player.getObjectId();
		startPathfinderTimer(TIME_LIMIT);
	}

	@Override
	public void collapse()
	{
		ReflectionTable.getInstance().removeSoloKamaloka(_player);
		stopPathfinderTimer();
		super.collapse();
	}

	@Override
	public void removeObject(L2Object o)
	{
		synchronized (_objects_lock)
		{
			_objects.remove(o.getStoredId());
		}
	}

	@Override
	public void addObject(L2Object o)
	{
		synchronized (_objects_lock)
		{
			_objects.add(o.getStoredId());
		}
	}

	public void registerKilled(L2NpcTemplate t)
	{
		Integer current = counter.get(t);
		if(current == null)
			current = 0;
		counter.put(t, ++current);
	}

	public HashMap<L2NpcTemplate, Integer> getCounter()
	{
		return counter;
	}

	public void startPathfinderTimer(long time)
	{
		if(_pathfinderTimerTask != null)
		{
			_pathfinderTimerTask.cancel();
			_pathfinderTimerTask = null;
		}

		if(_pathfinderTimer != null)
		{
			_pathfinderTimer.cancel();
			_pathfinderTimer = null;
		}

		_pathfinderTimer = new Timer();
		_pathfinderTimerTask = new TimerTask(){
			@Override
			public void run()
			{
				try
				{
					GArray<L2MonsterInstance> delete_list = new GArray<L2MonsterInstance>();
					for(L2Spawn s : KamalokaNightmare.this.getSpawns().toArray(new L2Spawn[0]))
						if(s != null)
							s.despawnAll();

					KamalokaNightmare.this.getSpawns().clear();

					_objects_lock.lock();
					for(Long storedId : _objects)
					{
						L2Object o = L2ObjectsStorage.get(storedId);
						if(o != null && o instanceof L2MonsterInstance)
							delete_list.add((L2MonsterInstance) o);
					}
					for(L2MonsterInstance o : delete_list)
						o.deleteMe();
					_objects_lock.unlock();

					L2Player p = (L2Player) L2ObjectsStorage.findObject(_player);
					if(p != null)
					{
						p.getPlayer().sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber((COLLAPSE_TIME - TIME_LIMIT) / 60000));

						L2PathfinderInstance npc = new L2PathfinderInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(32485));
						npc.setSpawnedLoc(KamalokaNightmare.this.getTeleportLoc());
						npc.setReflection(KamalokaNightmare.this.getId());
						npc.onSpawn();
						npc.spawnMe(npc.getSpawnedLoc());
					}
					else
						collapse();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};

		_pathfinderTimer.schedule(_pathfinderTimerTask, time);
	}

	public void stopPathfinderTimer()
	{
		if(_pathfinderTimerTask != null)
			_pathfinderTimerTask.cancel();
		_pathfinderTimerTask = null;

		if(_pathfinderTimer != null)
			_pathfinderTimer.cancel();
		_pathfinderTimer = null;
	}

	public int getPlayerId()
	{
		return _player;
	}

	@Override
	public boolean canChampions()
	{
		return false;
	}
}