package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;

public class ItemHandler
{
  private static ItemHandler _instance;
  private Map<Integer, IItemHandler> _datatable;

  public static ItemHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new ItemHandler();
    }
    return _instance;
  }

  public int size()
  {
    return _datatable.size();
  }

  private ItemHandler()
  {
    _datatable = new TreeMap();
  }

  public void registerItemHandler(IItemHandler handler)
  {
    int[] ids = handler.getItemIds();
    for (int i = 0; i < ids.length; i++)
    {
      _datatable.put(new Integer(ids[i]), handler);
    }
  }

  public IItemHandler getItemHandler(int itemId)
  {
    return (IItemHandler)_datatable.get(new Integer(itemId));
  }
}