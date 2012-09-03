package l2rt.gameserver.taskmanager;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.items.L2ItemInstance;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ItemsAutoDestroy
{
	private static ItemsAutoDestroy _instance;
	private ConcurrentLinkedQueue<L2ItemInstance> _items = null;
	private ConcurrentLinkedQueue<L2ItemInstance> _herbs = null;

	private ItemsAutoDestroy()
	{
		_herbs = new ConcurrentLinkedQueue<L2ItemInstance>();
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			_items = new ConcurrentLinkedQueue<L2ItemInstance>();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(), 60000, 60000);
		}
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckHerbsForDestroy(), 1000, 1000);
	}

	public static ItemsAutoDestroy getInstance()
	{
		if(_instance == null)
			_instance = new ItemsAutoDestroy();
		return _instance;
	}

	public void addItem(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}

	public void addHerb(L2ItemInstance herb)
	{
		herb.setDropTime(System.currentTimeMillis());
		_herbs.add(herb);
	}

	public class CheckItemsForDestroy implements Runnable
	{
		@Override
		public void run()
		{
			long _sleep = Config.AUTODESTROY_ITEM_AFTER * 1000L;
			try
			{
				long curtime = System.currentTimeMillis();
				for(L2ItemInstance item : _items)
					if(item == null || item.getDropTime() == 0 || item.getLocation() != L2ItemInstance.ItemLocation.VOID)
						_items.remove(item);
					else if(item.getDropTime() + _sleep < curtime)
					{
						item.deleteMe();
						_items.remove(item);
					}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public class CheckHerbsForDestroy implements Runnable
	{
		static final long _sleep = 60000;

		@Override
		public void run()
		{
			try
			{
				long curtime = System.currentTimeMillis();
				for(L2ItemInstance item : _herbs)
					if(item == null || item.getDropTime() == 0 || item.getLocation() != L2ItemInstance.ItemLocation.VOID)
						_herbs.remove(item);
					else if(item.getDropTime() + _sleep < curtime)
					{
						item.deleteMe();
						_herbs.remove(item);
					}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}