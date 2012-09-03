package l2rt.gameserver.taskmanager;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.util.Rnd;

import java.util.Collection;
import java.util.HashMap;

public class AutoSaveManager
{
	private PlayerContainer[] _tasks = new PlayerContainer[3200];
	private int _currentCell = 0;

	private static AutoSaveManager _instance;

	private AutoSaveManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SaveScheduler(), 1000, 1000);
	}

	public static AutoSaveManager getInstance()
	{
		if(_instance == null)
			_instance = new AutoSaveManager();

		return _instance;
	}

	public void addPlayerTask(L2Player p)
	{
		int cell = _currentCell + Rnd.get(800, 1600);
		if(_tasks.length <= cell)
			cell -= _tasks.length;
		if(_tasks[cell] == null)
			_tasks[cell] = new PlayerContainer();
		_tasks[cell].addPlayer(p);
	}

	private class SaveScheduler implements Runnable
	{
		public void run()
		{
			try
			{
				PlayerContainer currentContainer = _tasks[_currentCell];
				if(currentContainer != null)
					for(Long storeId : currentContainer.getList())
						try
						{
							L2Player p = L2ObjectsStorage.getAsPlayer(storeId);
							if(p == null || !p.isConnected() || p.isLogoutStarted() || p.getNetConnection() == null)
								continue;
							if(Config.AUTOSAVE)
								p.store(true);
							addPlayerTask(p);
						}
						catch(Throwable e)
						{
							e.printStackTrace();
						}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(_tasks[_currentCell] != null)
					_tasks[_currentCell].clear();
				_currentCell++;
				if(_currentCell >= _tasks.length)
					_currentCell = 0;
			}
		}
	}

	private class PlayerContainer
	{
		private HashMap<Integer, Long> list = new HashMap<Integer, Long>();

		public void addPlayer(L2Player e)
		{
			if(!list.containsKey(e.getObjectId()))
				list.put(e.getObjectId(), e.getStoredId());
		}

		public Collection<Long> getList()
		{
			return list.values();
		}

		public void clear()
		{
			synchronized (list)
			{
				list.clear();
			}
		}
	}
}