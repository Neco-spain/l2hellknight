package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public class PcInventory extends Inventory
{
  public static final int ADENA_ID = 57;
  public static final int ANCIENT_ADENA_ID = 5575;
  private final L2PcInstance _owner;
  private L2ItemInstance _adena;
  private L2ItemInstance _ancientAdena;

  public PcInventory(L2PcInstance owner)
  {
    _owner = owner;
  }

  public L2PcInstance getOwner() {
    return _owner;
  }
  protected L2ItemInstance.ItemLocation getBaseLocation() { return L2ItemInstance.ItemLocation.INVENTORY; } 
  protected L2ItemInstance.ItemLocation getEquipLocation() {
    return L2ItemInstance.ItemLocation.PAPERDOLL;
  }
  public L2ItemInstance getAdenaInstance() { return _adena; } 
  public int getAdena() {
    return _adena != null ? _adena.getCount() : 0;
  }

  public L2ItemInstance getAncientAdenaInstance() {
    return _ancientAdena;
  }

  public int getAncientAdena()
  {
    return _ancientAdena != null ? _ancientAdena.getCount() : 0;
  }

  public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena)
  {
    return getUniqueItems(allowAdena, allowAncientAdena, true);
  }

  public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
  {
    List list = new FastList();
    for (L2ItemInstance item : _items)
    {
      if (((!allowAdena) && (item.getItemId() == 57)) || (
        (!allowAncientAdena) && (item.getItemId() == 5575))) {
        continue;
      }
      boolean isDuplicate = false;
      for (L2ItemInstance litem : list)
      {
        if (litem.getItemId() == item.getItemId())
        {
          isDuplicate = true;
          break;
        }
      }
      if ((!isDuplicate) && ((!onlyAvailable) || ((item.getItem().isSellable()) && (item.isAvailable(getOwner(), false))))) list.add(item);
    }

    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena)
  {
    return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
  }

  public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
  {
    List list = new FastList();
    for (L2ItemInstance item : _items)
    {
      if (((!allowAdena) && (item.getItemId() == 57)) || (
        (!allowAncientAdena) && (item.getItemId() == 5575))) {
        continue;
      }
      boolean isDuplicate = false;
      for (L2ItemInstance litem : list)
        if ((litem.getItemId() == item.getItemId()) && (litem.getEnchantLevel() == item.getEnchantLevel()))
        {
          isDuplicate = true;
          break;
        }
      if ((!isDuplicate) && ((!onlyAvailable) || ((item.getItem().isSellable()) && (item.isAvailable(getOwner(), false))))) list.add(item);
    }

    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public L2ItemInstance[] getAllItemsByItemId(int itemId)
  {
    List list = new FastList();
    for (L2ItemInstance item : _items)
    {
      if (item.getItemId() == itemId) {
        list.add(item);
      }
    }
    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment)
  {
    List list = new FastList();
    for (L2ItemInstance item : _items)
    {
      if ((item.getItemId() == itemId) && (item.getEnchantLevel() == enchantment)) {
        list.add(item);
      }
    }
    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public L2ItemInstance[] getAvailableItems(boolean allowAdena)
  {
    List list = new FastList();
    for (L2ItemInstance item : _items) {
      if ((item != null) && (item.isAvailable(getOwner(), allowAdena))) list.add(item);
    }
    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public L2ItemInstance[] getAugmentedItems()
  {
    List list = new FastList();
    for (L2ItemInstance item : _items) {
      if ((item != null) && (item.isAugmented())) list.add(item);
    }
    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public FastList<TradeList.TradeItem> getAvailableItems(TradeList tradeList)
  {
    FastList list = new FastList();
    for (L2ItemInstance item : _items) {
      if (item.isAvailable(getOwner(), false))
      {
        TradeList.TradeItem adjItem = tradeList.adjustAvailableItem(item);
        if (adjItem != null)
          list.add(adjItem);
      }
    }
    return list;
  }

  public void adjustAvailableItem(TradeList.TradeItem item)
  {
    for (L2ItemInstance adjItem : _items)
    {
      if ((adjItem.getItemId() == item.getItem().getItemId()) && (adjItem.getEnchantLevel() == item.getEnchant()))
      {
        item.setObjectId(adjItem.getObjectId());

        if (adjItem.getCount() < item.getCount()) {
          item.setCount(adjItem.getCount());
        }
        return;
      }
    }

    item.setCount(0);
  }

  public void addAdena(String process, int count, L2PcInstance actor, L2Object reference)
  {
    if (count > 0)
      addItem(process, 57, count, actor, reference);
  }

  public void reduceAdena(String process, int count, L2PcInstance actor, L2Object reference)
  {
    if (count > 0)
      destroyItemByItemId(process, 57, count, actor, reference);
  }

  public void addAncientAdena(String process, int count, L2PcInstance actor, L2Object reference)
  {
    if (count > 0)
      addItem(process, 5575, count, actor, reference);
  }

  public void reduceAncientAdena(String process, int count, L2PcInstance actor, L2Object reference)
  {
    if (count > 0)
      destroyItemByItemId(process, 5575, count, actor, reference);
  }

  public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
  {
    item = super.addItem(process, item, actor, reference);

    if ((item != null) && (item.getItemId() == 57) && (!item.equals(_adena))) {
      _adena = item;
    }
    if ((item != null) && (item.getItemId() == 5575) && (!item.equals(_ancientAdena))) {
      _ancientAdena = item;
    }
    return item;
  }

  public L2ItemInstance addItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = super.addItem(process, itemId, count, actor, reference);

    if ((item != null) && (item.getItemId() == 57) && (!item.equals(_adena))) {
      _adena = item;
    }
    if ((item != null) && (item.getItemId() == 5575) && (!item.equals(_ancientAdena))) {
      _ancientAdena = item;
    }
    return item;
  }

  public L2ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);

    if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId()))) {
      _adena = null;
    }
    if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId()))) {
      _ancientAdena = null;
    }
    return item;
  }

  public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
  {
    item = super.destroyItem(process, item, actor, reference);

    if ((_adena != null) && (_adena.getCount() <= 0)) {
      _adena = null;
    }
    if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0)) {
      _ancientAdena = null;
    }
    return item;
  }

  public L2ItemInstance destroyItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = super.destroyItem(process, objectId, count, actor, reference);

    if ((_adena != null) && (_adena.getCount() <= 0)) {
      _adena = null;
    }
    if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0)) {
      _ancientAdena = null;
    }
    return item;
  }

  public L2ItemInstance destroyItemByItemId(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = super.destroyItemByItemId(process, itemId, count, actor, reference);

    if ((_adena != null) && (_adena.getCount() <= 0)) {
      _adena = null;
    }
    if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0)) {
      _ancientAdena = null;
    }
    return item;
  }

  public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
  {
    item = super.dropItem(process, item, actor, reference);

    if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId()))) {
      _adena = null;
    }
    if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId()))) {
      _ancientAdena = null;
    }
    return item;
  }

  public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = super.dropItem(process, objectId, count, actor, reference);

    if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId()))) {
      _adena = null;
    }
    if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId()))) {
      _ancientAdena = null;
    }
    return item;
  }

  protected void removeItem(L2ItemInstance item)
  {
    getOwner().removeItemFromShortCut(item.getObjectId());

    if (item.equals(getOwner().getActiveEnchantItem())) {
      getOwner().setActiveEnchantItem(null);
    }
    if (item.getItemId() == 57)
      _adena = null;
    else if (item.getItemId() == 5575) {
      _ancientAdena = null;
    }
    super.removeItem(item);
  }

  public void refreshWeight()
  {
    super.refreshWeight();
    getOwner().refreshOverloaded();
  }

  public void restore()
  {
    super.restore();
    _adena = getItemByItemId(57);
    _ancientAdena = getItemByItemId(5575);
  }

  public static int[][] restoreVisibleInventory(int objectId)
  {
    int[][] paperdoll = new int[18][3];
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
      st.setInt(1, objectId);
      rs = st.executeQuery();

      while (rs.next())
      {
        int slot = rs.getInt("loc_data");
        paperdoll[slot][0] = rs.getInt("object_id");
        paperdoll[slot][1] = rs.getInt("item_id");
        paperdoll[slot][2] = rs.getInt("enchant_level");
      }
    }
    catch (SQLException e)
    {
      _log.log(Level.WARNING, "could not restore inventory:", e);
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
    return paperdoll;
  }

  public boolean validateCapacity(L2ItemInstance item)
  {
    int slots = 0;

    if (((!item.isStackable()) || (getItemByItemId(item.getItemId()) == null)) && (item.getItemType() != L2EtcItemType.HERB)) {
      slots++;
    }
    return validateCapacity(slots);
  }

  public boolean validateCapacity(List<L2ItemInstance> items)
  {
    int slots = 0;

    for (L2ItemInstance item : items) {
      if ((!item.isStackable()) || (getItemByItemId(item.getItemId()) == null))
        slots++;
    }
    return validateCapacity(slots);
  }

  public boolean validateCapacityByItemId(int ItemId)
  {
    int slots = 0;

    L2ItemInstance invItem = getItemByItemId(ItemId);
    if ((invItem == null) || (!invItem.isStackable())) {
      slots++;
    }
    return validateCapacity(slots);
  }

  public boolean validateCapacity(int slots)
  {
    return _items.size() + slots <= _owner.getInventoryLimit();
  }

  public boolean validateWeight(int weight)
  {
    return _totalWeight + weight <= _owner.getMaxLoad();
  }
}