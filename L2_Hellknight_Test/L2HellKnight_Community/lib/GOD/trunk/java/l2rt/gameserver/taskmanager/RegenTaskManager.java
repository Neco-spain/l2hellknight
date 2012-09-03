package l2rt.gameserver.taskmanager;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RegenTaskManager
{
	private PlayerContainer[] _tasks = new PlayerContainer[2];
	private int _currentCell = 0;
	private long _currentTick = 0;

	private static RegenTaskManager _instance;

	private RegenTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new RegenScheduler(), 3000);
		_tasks[0] = new PlayerContainer();
		_tasks[1] = new PlayerContainer();
	}

	public static RegenTaskManager getInstance()
	{
		if(_instance == null)
			_instance = new RegenTaskManager();

		return _instance;
	}

	public PlayerContainer addRegenTask(L2Character character)
	{
		int cell = _currentCell == 0 ? 1 : 0;
		_tasks[cell].addPlayer(character);
		return _tasks[cell];
	}

	public long getTick()
	{
		return _currentTick;
	}

	private class RegenScheduler implements Runnable
	{
		public void run()
		{
			long start = System.currentTimeMillis();
			try
			{
				Long storeId;
				L2Character cha;
				while((storeId = _tasks[_currentCell].getList().poll()) != null)
					try
					{
						if((cha = L2ObjectsStorage.getAsCharacter(storeId)) == null)
							continue;
						if(cha.isPlayer())
						{
							L2Player player = (L2Player) cha;
							if(player.isDeleting() || !(player.isConnected() || player.isInOfflineMode()))
								continue;
						}
						cha.doRegen();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				_tasks[_currentCell].clear();
				synchronized (_instance)
				{
					_currentCell = _currentCell == 0 ? 1 : 0;
					_currentTick++;
				}
				long end = System.currentTimeMillis();
				if(end > start + 2500)
					System.out.println("Too long regen task: " + (end - start) + " ms");
				ThreadPoolManager.getInstance().scheduleGeneral(this, Math.max((3000 - (end - start)), 500));
			}
		}
	}

	private class PlayerContainer
	{
		private ConcurrentLinkedQueue<Long> list = new ConcurrentLinkedQueue<Long>();

		public void addPlayer(L2Character e)
		{
			list.add(e.getStoredId());
		}

		public ConcurrentLinkedQueue<Long> getList()
		{
			return list;
		}

		public void clear()
		{
			list.clear();
		}
	}
}