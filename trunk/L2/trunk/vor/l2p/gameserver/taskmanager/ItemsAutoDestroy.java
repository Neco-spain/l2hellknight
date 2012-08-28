package l2p.gameserver.taskmanager;

import java.util.concurrent.ConcurrentLinkedQueue;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.ItemInstance.ItemLocation;

public class ItemsAutoDestroy
{
  private static ItemsAutoDestroy _instance;
  private ConcurrentLinkedQueue<ItemInstance> _items = null;
  private ConcurrentLinkedQueue<ItemInstance> _herbs = null;

  private ItemsAutoDestroy()
  {
    _herbs = new ConcurrentLinkedQueue();
    if (Config.AUTODESTROY_ITEM_AFTER > 0)
    {
      _items = new ConcurrentLinkedQueue();
      ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckItemsForDestroy(), 60000L, 60000L);
    }
    ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckHerbsForDestroy(), 1000L, 1000L);
  }

  public static ItemsAutoDestroy getInstance()
  {
    if (_instance == null)
      _instance = new ItemsAutoDestroy();
    return _instance;
  }

  public void addItem(ItemInstance item)
  {
    item.setDropTime(System.currentTimeMillis());
    _items.add(item);
  }

  public void addHerb(ItemInstance herb)
  {
    herb.setDropTime(System.currentTimeMillis());
    _herbs.add(herb);
  }

  public class CheckHerbsForDestroy extends RunnableImpl
  {
    static final long _sleep = 60000L;

    public CheckHerbsForDestroy()
    {
    }

    public void runImpl()
      throws Exception
    {
      long curtime = System.currentTimeMillis();
      for (ItemInstance item : _herbs)
        if ((item == null) || (item.getLastDropTime() == 0L) || (item.getLocation() != ItemInstance.ItemLocation.VOID)) {
          _herbs.remove(item);
        } else if (item.getLastDropTime() + 60000L < curtime)
        {
          item.deleteMe();
          _herbs.remove(item);
        }
    }
  }

  public class CheckItemsForDestroy extends RunnableImpl
  {
    public CheckItemsForDestroy()
    {
    }

    public void runImpl()
      throws Exception
    {
      long _sleep = Config.AUTODESTROY_ITEM_AFTER * 1000L;
      long curtime = System.currentTimeMillis();
      for (ItemInstance item : _items)
        if ((item == null) || (item.getLastDropTime() == 0L) || (item.getLocation() != ItemInstance.ItemLocation.VOID)) {
          _items.remove(item);
        } else if (item.getLastDropTime() + _sleep < curtime)
        {
          item.deleteMe();
          _items.remove(item);
        }
    }
  }
}