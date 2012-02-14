package l2rt.gameserver.taskmanager;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;

import java.util.Collection;
import java.util.HashMap;

public class VitalityManager
{
	private PlayerContainer[] _tasks = new PlayerContainer[2];
	private int _currentCell = 0;

	private static VitalityManager _instance;

	private VitalityManager()
	{
		if(Config.ALT_VITALITY_ENABLED)
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new VitalityScheduler(), 120000, 120000);
	}

	public static VitalityManager getInstance()
	{
		if(_instance == null)
			_instance = new VitalityManager();

		return _instance;
	}

	public PlayerContainer addRegenTask(L2Player p)
	{
		// Вообще-то этот кошмар можно нахрен убрать, оставив только одну ячейку для следующего тика, но раз работает...
		int cell = _currentCell + 1;
		if(_tasks.length <= cell)
			cell -= _tasks.length;
		if(_tasks[cell] == null)
			_tasks[cell] = new PlayerContainer();
		_tasks[cell].addPlayer(p);
		return _tasks[cell];
	}

	private class VitalityScheduler implements Runnable
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
							if(player != null && !player.isDeleting() && (player.isConnected() || player.isInOfflineMode()) && player.isInPeaceZone())
							{
								player.setVitality(player.getVitality() + 1); // одно очко в 2 минуты, поскольку у нас одно очко исторически вдвое больше чем на оффе
								addRegenTask(player);
							}
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