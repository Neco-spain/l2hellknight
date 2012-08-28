package l2p.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2p.commons.math.SafeMath;
import l2p.gameserver.dao.ItemsDAO;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ItemContainer
{
  private static final Logger _log = LoggerFactory.getLogger(ItemContainer.class);
  protected static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();

  protected final List<ItemInstance> _items = new ArrayList();

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();
  protected final Lock readLock = lock.readLock();
  protected final Lock writeLock = lock.writeLock();

  public int getSize()
  {
    return _items.size();
  }

  public ItemInstance[] getItems()
  {
    readLock();
    try
    {
      ItemInstance[] arrayOfItemInstance = (ItemInstance[])_items.toArray(new ItemInstance[_items.size()]);
      return arrayOfItemInstance; } finally { readUnlock(); } throw localObject;
  }

  public void clear()
  {
    writeLock();
    try
    {
      _items.clear();
    }
    finally
    {
      writeUnlock();
    }
  }

  public final void writeLock()
  {
    writeLock.lock();
  }

  public final void writeUnlock()
  {
    writeLock.unlock();
  }

  public final void readLock()
  {
    readLock.lock();
  }

  public final void readUnlock()
  {
    readLock.unlock();
  }

  public ItemInstance getItemByObjectId(int objectId)
  {
    readLock();
    try
    {
      for (int i = 0; i < _items.size(); i++)
      {
        ItemInstance item = (ItemInstance)_items.get(i);
        if (item.getObjectId() == objectId) {
          ItemInstance localItemInstance1 = item;
          return localItemInstance1;
        }
      } } finally { readUnlock();
    }

    return null;
  }

  public ItemInstance getItemByItemId(int itemId)
  {
    readLock();
    try
    {
      for (int i = 0; i < _items.size(); i++)
      {
        ItemInstance item = (ItemInstance)_items.get(i);
        if (item.getItemId() == itemId) {
          ItemInstance localItemInstance1 = item;
          return localItemInstance1;
        }
      } } finally { readUnlock();
    }

    return null;
  }

  public List<ItemInstance> getItemsByItemId(int itemId)
  {
    List result = new ArrayList();

    readLock();
    try
    {
      for (int i = 0; i < _items.size(); i++)
      {
        ItemInstance item = (ItemInstance)_items.get(i);
        if (item.getItemId() == itemId)
          result.add(item);
      }
    }
    finally
    {
      readUnlock();
    }

    return result;
  }

  public long getCountOf(int itemId)
  {
    long count = 0L;
    readLock();
    try
    {
      for (int i = 0; i < _items.size(); i++)
      {
        ItemInstance item = (ItemInstance)_items.get(i);
        if (item.getItemId() == itemId)
          count = SafeMath.addAndLimit(count, item.getCount());
      }
    }
    finally
    {
      readUnlock();
    }
    return count;
  }

  public ItemInstance addItem(int itemId, long count)
  {
    if (count < 1L) {
      return null;
    }
writeLock();
    ItemInstance item;
    try {
      item = getItemByItemId(itemId);

      if ((item != null) && (item.isStackable())) {
        synchronized (item)
        {
          item.setCount(SafeMath.addAndLimit(item.getCount(), count));
          onModifyItem(item);
        }
      }
      else {
        item = ItemFunctions.createItem(itemId);
        item.setCount(count);

        _items.add(item);
        onAddItem(item);
      }
    }
    finally
    {
      writeUnlock();
    }

    return item;
  }

  public ItemInstance addItem(ItemInstance item)
  {
    if (item == null) {
      return null;
    }
    if (item.getCount() < 1L) {
      return null;
    }
    ItemInstance result = null;

    writeLock();
    try
    {
      if (getItemByObjectId(item.getObjectId()) != null) {
        Object localObject1 = null;
        return localObject1;
      }
      if (item.isStackable())
      {
        int itemId = item.getItemId();
        result = getItemByItemId(itemId);
        if (result != null) {
          synchronized (result)
          {
            result.setCount(SafeMath.addAndLimit(item.getCount(), result.getCount()));
            onModifyItem(result);
            onDestroyItem(item);
          }
        }
      }
      if (result == null)
      {
        _items.add(item);
        result = item;

        onAddItem(result);
      }
    }
    finally
    {
      writeUnlock();
    }

    return result;
  }

  public ItemInstance removeItemByObjectId(int objectId, long count)
  {
    if (count < 1L) {
      return null;
    }

    writeLock();
    ItemInstance result;
    try
    {
      ItemInstance item;
      if ((item = getItemByObjectId(objectId)) == null) {
        Object localObject1 = null;
        return localObject1;
      }
      synchronized (item)
      {
        result = removeItem(item, count);
      }
    }
    finally
    {
      writeUnlock();
    }

    return (ItemInstance)result;
  }

  public ItemInstance removeItemByItemId(int itemId, long count)
  {
    if (count < 1L) {
      return null;
    }

    writeLock();
    ItemInstance result;
    try
    {
      ItemInstance item;
      if ((item = getItemByItemId(itemId)) == null) {
        Object localObject1 = null;
        return localObject1;
      }
      synchronized (item)
      {
        result = removeItem(item, count);
      }
    }
    finally
    {
      writeUnlock();
    }

    return (ItemInstance)result;
  }

  public ItemInstance removeItem(ItemInstance item, long count)
  {
    if (item == null) {
      return null;
    }
    if (count < 1L) {
      return null;
    }
    if (item.getCount() < count) {
      return null;
    }
    writeLock();
    try
    {
      if (!_items.contains(item)) {
        Object localObject1 = null;
        return localObject1;
      }
      if (item.getCount() > count)
      {
        item.setCount(item.getCount() - count);
        onModifyItem(item);

        newItem = new ItemInstance(IdFactory.getInstance().getNextId(), item.getItemId());
        newItem.setCount(count);

        ItemInstance localItemInstance1 = newItem;
        return localItemInstance1;
      }
      ItemInstance newItem = removeItem(item);
      return newItem; } finally { writeUnlock(); } throw localObject2;
  }

  public ItemInstance removeItem(ItemInstance item)
  {
    if (item == null) {
      return null;
    }
    writeLock();
    try
    {
      if (!_items.remove(item)) {
        localItemInstance = null;
        return localItemInstance;
      }
      onRemoveItem(item);

      ItemInstance localItemInstance = item;
      return localItemInstance; } finally { writeUnlock(); } throw localObject;
  }

  public boolean destroyItemByObjectId(int objectId, long count)
  {
    writeLock();
    try
    {
      ItemInstance item;
      if ((item = getItemByObjectId(objectId)) == null) {
        int i = 0;
        return i;
      }
      synchronized (item)
      {
        boolean bool = destroyItem(item, count);

        writeUnlock(); return bool;
      } } finally { writeUnlock(); } throw localObject2;
  }

  public boolean destroyItemByItemId(int itemId, long count)
  {
    writeLock();
    try
    {
      ItemInstance item;
      if ((item = getItemByItemId(itemId)) == null) {
        int i = 0;
        return i;
      }
      synchronized (item)
      {
        boolean bool = destroyItem(item, count);

        writeUnlock(); return bool;
      } } finally { writeUnlock(); } throw localObject2;
  }

  public boolean destroyItem(ItemInstance item, long count)
  {
    if (item == null) {
      return false;
    }
    if (count < 1L) {
      return false;
    }
    if (item.getCount() < count) {
      return false;
    }
    writeLock();
    try
    {
      if (!_items.contains(item)) {
        bool = false;
        return bool;
      }
      if (item.getCount() > count)
      {
        item.setCount(item.getCount() - count);
        onModifyItem(item);

        bool = true;
        return bool;
      }
      boolean bool = destroyItem(item);
      return bool; } finally { writeUnlock(); } throw localObject;
  }

  public boolean destroyItem(ItemInstance item)
  {
    if (item == null) {
      return false;
    }
    writeLock();
    try
    {
      if (!_items.remove(item)) {
        i = 0;
        return i;
      }
      onRemoveItem(item);
      onDestroyItem(item);

      int i = 1;
      return i; } finally { writeUnlock(); } throw localObject;
  }

  protected abstract void onAddItem(ItemInstance paramItemInstance);

  protected abstract void onModifyItem(ItemInstance paramItemInstance);

  protected abstract void onRemoveItem(ItemInstance paramItemInstance);

  protected abstract void onDestroyItem(ItemInstance paramItemInstance);
}