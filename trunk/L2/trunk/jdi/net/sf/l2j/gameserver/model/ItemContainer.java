package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public abstract class ItemContainer
{
  protected static final Logger _log = Logger.getLogger(ItemContainer.class.getName());
  protected final List<L2ItemInstance> _items;

  protected ItemContainer()
  {
    _items = new FastList();
  }

  protected abstract L2Character getOwner();

  protected abstract L2ItemInstance.ItemLocation getBaseLocation();

  public int getOwnerId()
  {
    return getOwner() == null ? 0 : getOwner().getObjectId();
  }

  public int getSize()
  {
    return _items.size();
  }

  public L2ItemInstance[] getItems()
  {
    return (L2ItemInstance[])_items.toArray(new L2ItemInstance[_items.size()]);
  }

  public L2ItemInstance getItemByItemId(int itemId)
  {
    for (L2ItemInstance item : _items) {
      if ((item != null) && (item.getItemId() == itemId)) return item;
    }
    return null;
  }

  public L2ItemInstance getItemByItemId(int itemId, L2ItemInstance itemToIgnore)
  {
    for (L2ItemInstance item : _items)
      if ((item != null) && (item.getItemId() == itemId) && (!item.equals(itemToIgnore)))
      {
        return item;
      }
    return null;
  }

  public L2ItemInstance getItemByObjectId(int objectId)
  {
    for (L2ItemInstance item : _items) {
      if (item.getObjectId() == objectId) return item;
    }
    return null;
  }

  public static L2ItemInstance getItemByObjectId(int objectId, ItemContainer _object)
  {
    return _object.getItemByObjectId(objectId);
  }

  public int getInventoryItemCount(int itemId, int enchantLevel)
  {
    int count = 0;

    for (L2ItemInstance item : _items)
      if ((item.getItemId() == itemId) && ((item.getEnchantLevel() == enchantLevel) || (enchantLevel < 0)))
      {
        if (item.isStackable())
          count = item.getCount();
        else
          count++;
      }
    return count;
  }

  public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance olditem = getItemByItemId(item.getItemId());

    if ((olditem != null) && (olditem.isStackable()))
    {
      int count = item.getCount();
      olditem.changeCount(process, count, actor, reference);
      olditem.setLastChange(2);

      ItemTable.getInstance().destroyItem(process, item, actor, reference);
      item.updateDatabase();
      item = olditem;

      if ((item.getItemId() == 57) && (count < 10000.0F * Config.RATE_DROP_ADENA))
      {
        if (GameTimeController.getGameTicks() % 5 == 0)
          item.updateDatabase();
      }
      else {
        item.updateDatabase();
      }
    }
    else
    {
      item.setOwnerId(process, getOwnerId(), actor, reference);
      item.setLocation(getBaseLocation());
      item.setLastChange(1);

      addItem(item);

      item.updateDatabase();
    }

    refreshWeight();
    return item;
  }

  public L2ItemInstance addItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = getItemByItemId(itemId);

    if ((item != null) && (item.isStackable()))
    {
      item.changeCount(process, count, actor, reference);
      item.setLastChange(2);

      if ((itemId == 57) && (count < 10000.0F * Config.RATE_DROP_ADENA))
      {
        if (GameTimeController.getGameTicks() % 5 == 0)
          item.updateDatabase();
      }
      else {
        item.updateDatabase();
      }
    }
    else
    {
      for (int i = 0; i < count; i++)
      {
        L2Item template = ItemTable.getInstance().getTemplate(itemId);
        if (template == null)
        {
          _log.log(Level.WARNING, new StringBuilder().append(actor != null ? new StringBuilder().append("[").append(actor.getName()).append("] ").toString() : "").append("Invalid ItemId requested: ").toString(), Integer.valueOf(itemId));
          return null;
        }

        item = ItemTable.getInstance().createItem(process, itemId, template.isStackable() ? count : 1, actor, reference);
        item.setOwnerId(getOwnerId());
        item.setLocation(getBaseLocation());
        item.setLastChange(1);

        addItem(item);

        item.updateDatabase();

        if ((template.isStackable()) || (!Config.MULTIPLE_ITEM_DROP))
          break;
      }
    }
    refreshWeight();
    return item;
  }

  public L2ItemInstance addWearItem(String process, int itemId, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = getItemByItemId(itemId);

    if (item != null) return item;

    item = ItemTable.getInstance().createItem(process, itemId, 1, actor, reference);

    item.setWear(true);
    item.setOwnerId(getOwnerId());
    item.setLocation(getBaseLocation());
    item.setLastChange(1);

    addItem(item);

    refreshWeight();

    return item;
  }

  public L2ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, L2PcInstance actor, L2Object reference)
  {
    if (target == null)
    {
      return null;
    }

    L2ItemInstance sourceitem = getItemByObjectId(objectId);
    if (sourceitem == null)
    {
      return null;
    }
    L2ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;

    synchronized (sourceitem)
    {
      if (getItemByObjectId(objectId) != sourceitem)
      {
        return null;
      }

      if (count > sourceitem.getCount()) count = sourceitem.getCount();

      if ((sourceitem.getCount() == count) && (targetitem == null))
      {
        removeItem(sourceitem);
        target.addItem(process, sourceitem, actor, reference);
        targetitem = sourceitem;
      }
      else
      {
        if (sourceitem.getCount() > count)
        {
          sourceitem.changeCount(process, -count, actor, reference);
        }
        else
        {
          removeItem(sourceitem);
          ItemTable.getInstance().destroyItem(process, sourceitem, actor, reference);
        }

        if (targetitem != null)
        {
          targetitem.changeCount(process, count, actor, reference);
        }
        else
        {
          targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
        }

      }

      sourceitem.updateDatabase();
      if ((targetitem != sourceitem) && (targetitem != null)) targetitem.updateDatabase();
      if (sourceitem.isAugmented())
        sourceitem.getAugmentation().removeBoni(actor);
      refreshWeight();
    }
    return targetitem;
  }

  public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
  {
    synchronized (item)
    {
      if (!_items.contains(item))
      {
        return null;
      }
      if (item.isAugmented())
        item.getAugmentation().removeBoni(actor);
      removeItem(item);
      ItemTable.getInstance().destroyItem(process, item, actor, reference);

      item.updateDatabase();
      refreshWeight();
    }
    return item;
  }

  public L2ItemInstance destroyItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = getItemByObjectId(objectId);
    if (item == null) return null;

    if (item.getCount() > count)
    {
      synchronized (item)
      {
        item.changeCount(process, -count, actor, reference);
        item.setLastChange(2);

        item.updateDatabase();
        refreshWeight();
      }
      return item;
    }

    return destroyItem(process, item, actor, reference);
  }

  public L2ItemInstance destroyItemByItemId(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = getItemByItemId(itemId);
    if (item == null) return null;

    synchronized (item)
    {
      if (item.getCount() > count)
      {
        item.changeCount(process, -count, actor, reference);
        item.setLastChange(2);
      }
      else {
        return destroyItem(process, item, actor, reference);
      }
      item.updateDatabase();
      refreshWeight();
    }
    return item;
  }

  public synchronized void destroyAllItems(String process, L2PcInstance actor, L2Object reference)
  {
    for (L2ItemInstance item : _items)
      destroyItem(process, item, actor, reference);
  }

  public int getAdena()
  {
    int count = 0;

    for (L2ItemInstance item : _items) {
      if (item.getItemId() == 57)
      {
        count = item.getCount();
        return count;
      }
    }
    return count;
  }

  protected void addItem(L2ItemInstance item)
  {
    _items.add(item);
  }

  protected void removeItem(L2ItemInstance item)
  {
    _items.remove(item);
  }

  protected void refreshWeight()
  {
  }

  public void deleteMe()
  {
    try
    {
      updateDatabase(); } catch (Throwable t) { _log.log(Level.SEVERE, "deletedMe()", t); }
    List items = new FastList(_items);

    _items.clear();

    L2World.getInstance().removeObjects(items);
  }

  public void updateDatabase()
  {
    if (getOwner() != null)
    {
      for (L2ItemInstance item : _items)
      {
        if (item != null)
          item.updateDatabase();
      }
    }
  }

  public void restore()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=?) ORDER BY object_id DESC");

      statement.setInt(1, getOwnerId());
      statement.setString(2, getBaseLocation().name());
      ResultSet inv = statement.executeQuery();

      while (inv.next())
      {
        int objectId = inv.getInt(1);
        L2ItemInstance item = L2ItemInstance.restoreFromDb(objectId);
        if (item == null)
          continue;
        L2World.getInstance().storeObject(item);

        if ((item.isStackable()) && (getItemByItemId(item.getItemId()) != null))
          addItem("Restore", item, null, getOwner());
        else addItem(item);
      }

      inv.close();
      statement.close();
      refreshWeight();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not restore container:", e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public boolean validateCapacity(int slots) {
    return true;
  }
  public boolean validateWeight(int weight) { return true;
  }
}