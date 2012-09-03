package l2rt.gameserver.taskmanager;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;

import java.util.Collection;
import java.util.HashMap;

public class BreakWarnManager
{
	private PlayerContainer[] _tasks = new PlayerContainer[360];
	private int _currentCell = 0;

	private static BreakWarnManager _instance;

	private BreakWarnManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DispelScheduler(), 60000, 60000);
	}

	public static BreakWarnManager getInstance()
	{
		if(_instance == null)
			_instance = new BreakWarnManager();

		return _instance;
	}

	public PlayerContainer addWarnTask(L2Player p)
	{
		int cell = _currentCell + 120;
		if(_tasks.length <= cell)
			cell -= _tasks.length;
		if(_tasks[cell] == null)
			_tasks[cell] = new PlayerContainer();
		_tasks[cell].addPlayer(p);
		return _tasks[cell];
	}

	private class DispelScheduler implements Runnable
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
							L2Player player = L2ObjectsStorage.getAsPlayer(storeId);
							if(player == null || !player.isConnected() || player.isDeleting())
								continue;
							player.sendPacket(Msg.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK);
							addWarnTask(player);
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