package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public abstract class ItemContainer
{
  protected static final Logger _log = Logger.getLogger(ItemContainer.class.getName());
  protected final ConcurrentLinkedQueue<L2ItemInstance> _items;

  protected ItemContainer()
  {
    _items = new ConcurrentLinkedQueue();
  }

  protected abstract L2Character getOwner();

  protected abstract L2ItemInstance.ItemLocation getBaseLocation();

  public int getOwnerId() {
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

  public ConcurrentLinkedQueue<L2ItemInstance> getAllItems()
  {
    return _items;
  }

  public FastTable<L2ItemInstance> getAllItemsEnch()
  {
    if (getSize() == 0) {
      return null;
    }
    FastTable enchs = new FastTable();
    for (L2ItemInstance item : getAllItems())
    {
      if ((item == null) || 
        (!item.canBeEnchanted())) {
        continue;
      }
      if (item.getEnchantLevel() > 1)
        enchs.add(item);
    }
    return enchs;
  }

  public FastTable<L2ItemInstance> getAllItemsAug()
  {
    if (getSize() == 0) {
      return null;
    }
    FastTable enchs = new FastTable();
    for (L2ItemInstance item : getAllItems())
    {
      if (item == null) {
        continue;
      }
      if ((item.isAugmented()) && (item.getAugmentation().getAugmentSkill() != null))
        enchs.add(item);
    }
    return enchs;
  }

  public FastTable<L2ItemInstance> getAllItemsNext(int exc, int service)
  {
    if (getSize() == 0) {
      return null;
    }
    int itemType = 0;
    FastTable enchs = new FastTable();
    for (L2ItemInstance item : getAllItems())
    {
      if ((item == null) || 
        (!item.canBeEnchanted()) || 
        (exc == item.getObjectId())) {
        continue;
      }
      itemType = item.getItem().getType2();
      switch (service)
      {
      case 1:
        if ((itemType != 0) || (item.isAugmented()) || (item.isWear()) || (item.isEquipped()) || (item.isHeroItem()) || (!item.isDestroyable())) break;
        enchs.add(item); break;
      case 2:
        if ((item.getItem().getCrystalType() <= 2) || ((itemType != 0) && (itemType != 1) && (itemType != 2))) break;
        enchs.add(item);
      }
    }

    return enchs;
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
    if (target == null) {
      return null;
    }
    L2ItemInstance sourceitem = getItemByObjectId(objectId);
    if (sourceitem == null) {
      return null;
    }
    L2ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;

    synchronized (sourceitem)
    {
      if (getItemByObjectId(objectId) != sourceitem) {
        return null;
      }

      if (count > sourceitem.getCount()) {
        count = sourceitem.getCount();
      }

      if ((sourceitem.getCount() == count) && (targetitem == null))
      {
        removeItem(sourceitem);
        target.addItem(process, sourceitem, actor, reference);
        targetitem = sourceitem;
      }
      else
      {
        if (sourceitem.getCount() > count) {
          sourceitem.changeCount(process, -count, actor, reference);
        }
        else {
          removeItem(sourceitem);
          ItemTable.getInstance().destroyItem(process, sourceitem, actor, reference);
        }

        if (targetitem != null)
          targetitem.changeCount(process, count, actor, reference);
        else {
          targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
        }
      }

      sourceitem.updateDatabase(true);
      if ((targetitem != sourceitem) && (targetitem != null))
        targetitem.updateDatabase(true);
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
    L2ItemInstance adena = findItemId(57, 2);
    if (adena == null) {
      return 0;
    }
    return adena.getCount();
  }

  public int getItemCount(int itemId)
  {
    L2ItemInstance adena = findItemId(itemId, 2);
    if (adena == null) {
      return 0;
    }
    return adena.getCount();
  }

  protected void addItem(L2ItemInstance item)
  {
    _items.add(item);
  }

  protected void removeItem(L2ItemInstance item)
  {
    item.updateDatabase(true);

    _items.remove(item);
  }

  public void updateDatabase(boolean commit)
  {
    updateDatabase(_items, commit);
  }

  private void updateDatabase(ConcurrentLinkedQueue<L2ItemInstance> items, boolean commit)
  {
    if (getOwner() != null)
      for (L2ItemInstance inst : items)
      {
        inst.updateDatabase(commit);
      }
  }

  protected void refreshWeight()
  {
  }

  public void deleteMe()
  {
    for (L2ItemInstance inst : getAllItems())
    {
      inst.updateDatabase(true);
      inst.removeFromWorld();
    }
    getAllItems().clear();
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
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet inv = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=?) ORDER BY object_id DESC");
      statement.setInt(1, getOwnerId());
      statement.setString(2, getBaseLocation().name());
      inv = statement.executeQuery();

      while (inv.next())
      {
        int objectId = inv.getInt(1);
        L2ItemInstance item = L2ItemInstance.restoreFromDb(objectId);
        if (item == null) {
          continue;
        }
        L2World.getInstance().storeObject(item);

        if ((item.isStackable()) && (getItemByItemId(item.getItemId()) != null))
          addItem("Restore", item, null, getOwner());
        else
          addItem(item);
      }
      refreshWeight();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not restore container:", e);
    }
    finally
    {
      Close.CSR(con, statement, inv);
    }
  }

  public boolean validateCapacity(int slots) {
    return true;
  }
  public boolean validateWeight(int weight) { return true; }

  public FastTable<L2ItemInstance> listItems(int type)
  {
    FastTable items = new FastTable();
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      String loc = "";
      switch (type)
      {
      case 1:
        loc = "WAREHOUSE";
        break;
      case 2:
      case 3:
        loc = "CLANWH";
        break;
      case 4:
        loc = "FREIGHT";
      }

      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=?) ORDER BY object_id DESC");
      statement.setInt(1, getOwnerId());
      statement.setString(2, loc);
      rset = statement.executeQuery();

      while (rset.next())
      {
        int objectId = rset.getInt(1);
        L2ItemInstance item = L2ItemInstance.restoreFromDb(objectId);
        if (item == null)
          continue;
        items.add(item);
      }
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "could not restore warehouse:", e);
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }
    return items;
  }

  public synchronized void addItem(L2ItemInstance newItem, int whType) {
    L2ItemInstance oldItem = findItemId(newItem.getItemId(), whType);

    if ((!newItem.isStackable()) || (oldItem == null))
    {
      newItem.setOwnerId(getOwnerId());
      if (whType == 1)
        newItem.setLocation(L2ItemInstance.ItemLocation.WAREHOUSE);
      else if (whType == 4)
        newItem.setLocation(L2ItemInstance.ItemLocation.FREIGHT);
      else
        newItem.setLocation(L2ItemInstance.ItemLocation.CLANWH);
      newItem.updateDatabase();
      return;
    }
    int newCount = oldItem.getCount() + newItem.getCount();
    long cur = oldItem.getCount() + newItem.getCount();
    if ((oldItem.getItemId() == 57) && (cur >= 2147483647L))
    {
      newCount = 2147483647;
    }
    oldItem.setCount(newCount);
    oldItem.updateDatabase(true);
    newItem.deleteMe();
  }

  public synchronized void addAdena(int id, int count)
  {
    L2ItemInstance oldItem = findItemId(id, 2);

    if (oldItem == null)
    {
      L2ItemInstance newItem = ItemTable.getInstance().createItem("addAdena", id, count, null, null);
      newItem.setOwnerId(getOwnerId());
      newItem.setLocation(L2ItemInstance.ItemLocation.CLANWH);
      newItem.updateDatabase();
      return;
    }
    int newCount = oldItem.getCount() + count;
    long cur = oldItem.getCount() + count;
    if ((oldItem.getItemId() == 57) && (cur >= 2147483647L))
    {
      newCount = 2147483647;
    }
    oldItem.setCount(newCount);
    oldItem.updateDatabase();
  }

  public L2ItemInstance reduceAdena(int itemId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = findItemId(itemId, 2);
    if (item == null) return null;

    synchronized (item)
    {
      if (item.getCount() > count)
      {
        item.changeCount("reduceAdena", -count, actor, reference);
        item.setLastChange(2);
      }
      else {
        return destroyItem("reduceAdena", item, actor, reference);
      }
      item.updateDatabase(true);
      refreshWeight();
    }
    return item;
  }

  public L2ItemInstance findItemId(int itemId, int whType)
  {
    L2ItemInstance foundItem = null;
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      String loc = "";
      switch (whType)
      {
      case 1:
        loc = "WAREHOUSE";
        break;
      case 2:
      case 3:
        loc = "CLANWH";
        break;
      case 4:
        loc = "FREIGHT";
      }

      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND loc=? AND item_id=?");
      statement.setInt(1, getOwnerId());
      statement.setString(2, loc);
      statement.setInt(3, itemId);
      rset = statement.executeQuery();

      if (rset.next())
        foundItem = L2ItemInstance.restoreFromDb(rset.getInt(1));
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not list warehouse: ", e);
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }
    return foundItem;
  }

  public synchronized L2ItemInstance getItemByObj(int objectId, int count, L2PcInstance player)
  {
    L2ItemInstance item = L2ItemInstance.restoreFromDb(objectId);
    if (item == null)
    {
      _log.fine(new StringBuilder().append("Warehouse.destroyItem: can't destroy objectId: ").append(objectId).append(", count: ").append(count).toString());
      return null;
    }

    if ((item.getLocation() != L2ItemInstance.ItemLocation.CLANWH) && (item.getLocation() != L2ItemInstance.ItemLocation.WAREHOUSE) && (item.getLocation() != L2ItemInstance.ItemLocation.FREIGHT))
    {
      _log.warning(new StringBuilder().append("WARNING get item not in WAREHOUSE via WAREHOUSE: item objid=").append(item.getObjectId()).append(" ownerid=").append(item.getOwnerId()).toString());
      return null;
    }

    if (!item.isStackable()) {
      item.setWhFlag(true);
    }

    if ((item.getCount() <= 0) || (count <= 0))
    {
      return null;
    }

    if (item.getCount() <= count)
    {
      item.setLocation(L2ItemInstance.ItemLocation.VOID);
      item.updateDatabase(true);
      return item;
    }

    item.setCount(item.getCount() - count);
    item.updateDatabase(true);

    L2ItemInstance Newitem = ItemTable.getInstance().createItem("withdrawwh", item.getItem().getItemId(), count, player, player.getLastFolkNPC());
    Newitem.setWhFlag(true);

    return Newitem;
  }
}