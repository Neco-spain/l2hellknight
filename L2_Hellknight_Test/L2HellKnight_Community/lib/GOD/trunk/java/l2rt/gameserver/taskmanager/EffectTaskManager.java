package l2rt.gameserver.taskmanager;

import javolution.util.ReentrantLock;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Effect;
import l2rt.util.GArray;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ScheduledFuture;

public class EffectTaskManager
{
	private EffectContainer[] _dispelTasks = new EffectContainer[172800];
	private Stack<EffectContainer> _pool = new Stack<EffectContainer>();
	private ReentrantLock lock = new ReentrantLock();
	private int _currentDispelCell = 0;
	@SuppressWarnings("unused")
	private ScheduledFuture<?> _task;

	private static EffectTaskManager _instance;

	private EffectTaskManager()
	{
		_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DispelScheduler(), 1000, 1000);
	}

	public static EffectTaskManager getInstance()
	{
		return _instance != null ? _instance : (_instance = new EffectTaskManager());
	}

	/**
	 * интервал в секундах!
	 */
	public EffectContainer addDispelTask(L2Effect e, int interval)
	{
		try
		{
			lock.lock();

			if(interval < 1)
				interval = 1;
			if(interval > _dispelTasks.length / 2)
			{
				System.out.println("ERROR: Effect " + e.getSkill().getName() + " I " + interval);
				interval = _dispelTasks.length - 1;
			}
			int cell = _currentDispelCell + interval;
			if(_dispelTasks.length <= cell)
				cell -= _dispelTasks.length;
			if(_dispelTasks[cell] == null)
				if(!_pool.isEmpty())
					_dispelTasks[cell] = _pool.pop();
				else
					_dispelTasks[cell] = new EffectContainer();
			_dispelTasks[cell].addEffect(e);

			return _dispelTasks[cell];
		}
		finally
		{
			lock.unlock();
		}
	}

	private class DispelScheduler implements Runnable
	{
		public void run()
		{
			GArray<L2Effect> works = new GArray<L2Effect>();

			lock.lock();
			try
			{
				if(_dispelTasks[_currentDispelCell] != null && !_dispelTasks[_currentDispelCell].getList().isEmpty())
					for(WeakReference<L2Effect> we : _dispelTasks[_currentDispelCell].getList())
						try
						{
							L2Effect eff = we.get();
							if(eff == null || eff.isFinished())
								continue;

							works.add(eff);

							// TODO не баг, но так получается лишний цикл (пустой, т.к. сработает eff.isFinished())
							if(!eff.isEnded())
								addDispelTask(eff, (int) (eff.getPeriod() / 1000));
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
				if(_dispelTasks[_currentDispelCell] != null)
				{
					_dispelTasks[_currentDispelCell].clear();
					_pool.push(_dispelTasks[_currentDispelCell]);
					_dispelTasks[_currentDispelCell] = null;
				}
				_currentDispelCell++;
				if(_currentDispelCell >= _dispelTasks.length)
					_currentDispelCell = 0;

				lock.unlock();
			}

			for(L2Effect work : works)
				try
				{
					work.scheduleEffect();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}
	}

	private class EffectContainer
	{
		private LinkedList<WeakReference<L2Effect>> list = new LinkedList<WeakReference<L2Effect>>();

		public void addEffect(L2Effect e)
		{
			list.add(new WeakReference<L2Effect>(e));
		}

		public LinkedList<WeakReference<L2Effect>> getList()
		{
			return list;
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