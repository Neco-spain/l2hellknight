package net.sf.l2j.gameserver;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.templates.L2EtcItemType;

public class ItemsAutoDestroy
{
  protected static final Logger _log = Logger.getLogger("ItemsAutoDestroy");
  private static ItemsAutoDestroy _instance;
  protected List<L2ItemInstance> _items = null;
  protected static long _sleep;

  private ItemsAutoDestroy()
  {
    _items = new FastList();
    _sleep = Config.AUTODESTROY_ITEM_AFTER * 1000;
    if (_sleep == 0L)
      _sleep = 3600000L;
    ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(), 5000L, 5000L);
  }

  public static ItemsAutoDestroy getInstance()
  {
    if (_instance == null)
    {
      System.out.println("Initializing ItemsAutoDestroy.");
      _instance = new ItemsAutoDestroy();
    }
    return _instance;
  }

  public synchronized void addItem(L2ItemInstance item)
  {
    item.setDropTime(System.currentTimeMillis());
    _items.add(item);
  }

  public synchronized void removeItems()
  {
    if (Config.DEBUG) {
      _log.info("[ItemsAutoDestroy] : " + _items.size() + " items to check.");
    }
    if (_items.isEmpty()) return;

    long curtime = System.currentTimeMillis();
    for (L2ItemInstance item : _items)
    {
      if ((item == null) || (item.getDropTime() == 0L) || (item.getLocation() != L2ItemInstance.ItemLocation.VOID)) {
        _items.remove(item);
      }
      else if (item.getItemType() == L2EtcItemType.HERB)
      {
        if (curtime - item.getDropTime() > Config.HERB_AUTO_DESTROY_TIME)
        {
          L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
          L2World.getInstance().removeObject(item);
          _items.remove(item);
          if (Config.SAVE_DROPPED_ITEM)
            ItemsOnGroundManager.getInstance().removeObject(item);
        }
      }
      else if (curtime - item.getDropTime() > _sleep)
      {
        L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
        L2World.getInstance().removeObject(item);
        _items.remove(item);
        if (Config.SAVE_DROPPED_ITEM) {
          ItemsOnGroundManager.getInstance().removeObject(item);
        }
      }
    }

    if (Config.DEBUG)
      _log.info("[ItemsAutoDestroy] : " + _items.size() + " items remaining.");
  }

  protected class CheckItemsForDestroy extends Thread {
    protected CheckItemsForDestroy() {
    }

    public void run() {
      removeItems();
    }
  }
}