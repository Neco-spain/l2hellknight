package net.sf.l2j.gameserver.model;

import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;

public class TradeList
{
  private static Logger _log = Logger.getLogger(TradeList.class.getName());
  private L2PcInstance _owner;
  private L2PcInstance _partner;
  private List<TradeItem> _items;
  private String _title;
  private boolean _packaged;
  private boolean _confirmed = false;
  private boolean _locked = false;

  public TradeList(L2PcInstance owner)
  {
    _items = new FastList();
    _owner = owner;
  }

  public L2PcInstance getOwner()
  {
    return _owner;
  }

  public void setPartner(L2PcInstance partner)
  {
    _partner = partner;
  }

  public L2PcInstance getPartner()
  {
    return _partner;
  }

  public void setTitle(String title)
  {
    _title = title;
  }

  public String getTitle()
  {
    return _title;
  }

  public boolean isLocked()
  {
    return _locked;
  }

  public boolean isConfirmed()
  {
    return _confirmed;
  }

  public boolean isPackaged()
  {
    return _packaged;
  }

  public void setPackaged(boolean value)
  {
    _packaged = value;
  }

  public TradeItem[] getItems()
  {
    return (TradeItem[])_items.toArray(new TradeItem[_items.size()]);
  }

  public TradeItem[] getAvailableItems(PcInventory inventory)
  {
    List list = new FastList();
    for (TradeItem item : _items)
    {
      item = new TradeItem(item, item.getCount(), item.getPrice());
      inventory.adjustAvailableItem(item);
      list.add(item);
    }

    return (TradeItem[])list.toArray(new TradeItem[list.size()]);
  }

  public int getItemCount()
  {
    return _items.size();
  }

  public TradeItem adjustAvailableItem(L2ItemInstance item)
  {
    if (item.isStackable())
      for (TradeItem exclItem : _items)
        if (exclItem.getItem().getItemId() == item.getItemId()) {
          if (item.getCount() <= exclItem.getCount()) return null;
          return new TradeItem(item, item.getCount() - exclItem.getCount(), item.getReferencePrice());
        }
    return new TradeItem(item, item.getCount(), item.getReferencePrice());
  }

  public void adjustItemRequest(ItemRequest item)
  {
    for (TradeItem filtItem : _items)
      if (filtItem.getObjectId() == item.getObjectId())
      {
        if (filtItem.getCount() < item.getCount()) item.setCount(filtItem.getCount());
        return;
      }
    item.setCount(0);
  }

  public void adjustItemRequestByItemId(ItemRequest item)
  {
    for (TradeItem filtItem : _items)
      if (filtItem.getItem().getItemId() == item.getItemId())
      {
        if (filtItem.getCount() < item.getCount()) item.setCount(filtItem.getCount());
        return;
      }
    item.setCount(0);
  }

  public synchronized TradeItem addItem(int objectId, int count)
  {
    return addItem(objectId, count, 0);
  }

  public synchronized TradeItem addItem(int objectId, int count, int price)
  {
    if (isLocked())
    {
      _log.warning(_owner.getName() + ": Attempt to modify locked TradeList!");
      return null;
    }
    L2Object o = L2World.getInstance().findObject(objectId);

    if ((o == null) || (!(o instanceof L2ItemInstance)))
    {
      _log.warning(_owner.getName() + ": Attempt to add invalid item to TradeList!");
      return null;
    }

    L2ItemInstance item = (L2ItemInstance)o;

    if ((!item.isTradeable()) || (item.getItemType() == L2EtcItemType.QUEST)) {
      return null;
    }
    if (count > item.getCount()) return null;

    if ((!item.isStackable()) && (count > 1))
    {
      _log.warning(_owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
      return null;
    }
    for (TradeItem checkitem : _items)
    {
      if (checkitem.getObjectId() == objectId) return null;
    }
    TradeItem titem = new TradeItem(item, count, price);
    _items.add(titem);

    invalidateConfirmation();
    return titem;
  }

  public synchronized TradeItem addItemByItemId(int itemId, int count, int price, int enchant)
  {
    if (isLocked())
    {
      _log.warning(_owner.getName() + ": Attempt to modify locked TradeList!");
      return null;
    }

    L2Item item = ItemTable.getInstance().getTemplate(itemId);
    if (item == null)
    {
      _log.warning(_owner.getName() + ": Attempt to add invalid item to TradeList!");
      return null;
    }

    if ((!item.isTradeable()) || (item.getItemType() == L2EtcItemType.QUEST)) return null;

    if ((!item.isStackable()) && (count > 1))
    {
      _log.warning(_owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
      return null;
    }

    TradeItem titem = new TradeItem(item, count, price, enchant);
    _items.add(titem);

    invalidateConfirmation();
    return titem;
  }

  public synchronized TradeItem removeItem(int objectId, int itemId, int count)
  {
    if (isLocked())
    {
      _log.warning(_owner.getName() + ": Attempt to modify locked TradeList!");
      return null;
    }

    for (TradeItem titem : _items)
      if ((titem.getObjectId() == objectId) || (titem.getItem().getItemId() == itemId))
      {
        if (_partner != null)
        {
          TradeList partnerList = _partner.getActiveTradeList();
          if (partnerList == null)
          {
            _log.warning(_partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
            return null;
          }
          partnerList.invalidateConfirmation();
        }

        if ((count != -1) && (titem.getCount() > count)) titem.setCount(titem.getCount() - count); else {
          _items.remove(titem);
        }
        return titem;
      }
    return null;
  }

  public synchronized void updateItems()
  {
    for (TradeItem titem : _items)
    {
      L2ItemInstance item = _owner.getInventory().getItemByObjectId(titem.getObjectId());
      if ((item == null) || (titem.getCount() < 1)) removeItem(titem.getObjectId(), -1, -1);
      else if (item.getCount() < titem.getCount()) titem.setCount(item.getCount());
    }
  }

  public void lock()
  {
    _locked = true;
  }

  public void clear()
  {
    _items.clear();
    _locked = false;
  }

  public boolean confirm()
  {
    if (_confirmed) return true;

    if (_partner != null)
    {
      TradeList partnerList = _partner.getActiveTradeList();
      if (partnerList == null)
      {
        _log.warning(_partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
        return false;
      }
      TradeList sync2;
      TradeList sync1;
      TradeList sync2;
      if (getOwner().getObjectId() > partnerList.getOwner().getObjectId()) {
        TradeList sync1 = partnerList; sync2 = this; } else {
        sync1 = this; sync2 = partnerList;
      }
      synchronized (sync1)
      {
        synchronized (sync2)
        {
          _confirmed = true;
          if (partnerList.isConfirmed())
          {
            partnerList.lock();
            lock();
            if (!partnerList.validate()) return false;
            if (!validate()) return false;

            doExchange(partnerList);
          } else {
            _partner.onTradeConfirm(_owner);
          }
        }
      }
    } else {
      _confirmed = true;
    }
    return _confirmed;
  }

  public void invalidateConfirmation()
  {
    _confirmed = false;
  }

  private boolean validate()
  {
    if ((_owner == null) || (L2World.getInstance().findObject(_owner.getObjectId()) == null))
    {
      _log.warning("Invalid owner of TradeList");
      return false;
    }

    for (TradeItem titem : _items)
    {
      L2ItemInstance item = _owner.checkItemManipulation(titem.getObjectId(), titem.getCount(), "transfer");
      if ((item == null) || (titem.getCount() < 1))
      {
        _log.warning(_owner.getName() + ": Invalid Item in TradeList");
        return false;
      }
    }

    return true;
  }

  private boolean TransferItems(L2PcInstance partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU)
  {
    for (TradeItem titem : _items)
    {
      L2ItemInstance oldItem = _owner.getInventory().getItemByObjectId(titem.getObjectId());
      if (oldItem == null) return false;
      L2ItemInstance newItem = _owner.getInventory().transferItem("Trade", titem.getObjectId(), titem.getCount(), partner.getInventory(), _owner, _partner);
      if (newItem == null) return false;

      if (ownerIU != null)
      {
        if ((oldItem.getCount() > 0) && (oldItem != newItem)) ownerIU.addModifiedItem(oldItem); else {
          ownerIU.addRemovedItem(oldItem);
        }
      }
      if (partnerIU != null)
      {
        if (newItem.getCount() > titem.getCount()) partnerIU.addModifiedItem(newItem); else
          partnerIU.addNewItem(newItem);
      }
    }
    return true;
  }

  public int countItemsSlots(L2PcInstance partner)
  {
    int slots = 0;

    for (TradeItem item : _items)
    {
      if (item != null) {
        L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
        if (template != null)
          if (!template.isStackable()) slots += item.getCount();
          else if (partner.getInventory().getItemByItemId(item.getItem().getItemId()) == null)
            slots++;
      }
    }
    return slots;
  }

  public int calcItemsWeight()
  {
    int weight = 0;

    for (TradeItem item : _items)
    {
      if (item != null) {
        L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
        if (template != null)
          weight += item.getCount() * template.getWeight();
      }
    }
    return weight;
  }

  private void doExchange(TradeList partnerList)
  {
    boolean success = false;

    if ((!getOwner().getInventory().validateWeight(partnerList.calcItemsWeight())) || (!partnerList.getOwner().getInventory().validateWeight(calcItemsWeight())))
    {
      partnerList.getOwner().sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
      getOwner().sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
    }
    else if ((!getOwner().getInventory().validateCapacity(partnerList.countItemsSlots(getOwner()))) || (!partnerList.getOwner().getInventory().validateCapacity(countItemsSlots(partnerList.getOwner()))))
    {
      partnerList.getOwner().sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
      getOwner().sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
    }
    else
    {
      InventoryUpdate ownerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
      InventoryUpdate partnerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

      partnerList.TransferItems(getOwner(), partnerIU, ownerIU);
      TransferItems(partnerList.getOwner(), ownerIU, partnerIU);

      if (ownerIU != null) _owner.sendPacket(ownerIU); else {
        _owner.sendPacket(new ItemList(_owner, false));
      }
      if (partnerIU != null) _partner.sendPacket(partnerIU); else {
        _partner.sendPacket(new ItemList(_partner, false));
      }

      StatusUpdate playerSU = new StatusUpdate(_owner.getObjectId());
      playerSU.addAttribute(14, _owner.getCurrentLoad());
      _owner.sendPacket(playerSU);
      playerSU = new StatusUpdate(_partner.getObjectId());
      playerSU.addAttribute(14, _partner.getCurrentLoad());
      _partner.sendPacket(playerSU);

      success = true;
    }

    partnerList.getOwner().onTradeFinish(success);
    getOwner().onTradeFinish(success);
  }

  public synchronized boolean PrivateStoreBuy(L2PcInstance player, ItemRequest[] items, int price)
  {
    if (_locked) return false;
    if (!validate())
    {
      lock();
      return false;
    }

    int slots = 0;
    int weight = 0;

    for (ItemRequest item : items)
    {
      if (item != null) {
        L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
        if (template != null) {
          weight += item.getCount() * template.getWeight();
          if (!template.isStackable()) { slots += item.getCount(); } else {
            if (player.getInventory().getItemByItemId(item.getItemId()) != null) continue; slots++;
          }
        }
      }
    }
    if (!player.getInventory().validateWeight(weight))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
      return false;
    }

    if (!player.getInventory().validateCapacity(slots))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
      return false;
    }

    PcInventory ownerInventory = _owner.getInventory();
    PcInventory playerInventory = player.getInventory();

    InventoryUpdate ownerIU = new InventoryUpdate();
    InventoryUpdate playerIU = new InventoryUpdate();

    if (price > playerInventory.getAdena())
    {
      lock();
      return false;
    }

    L2ItemInstance adenaItem = playerInventory.getAdenaInstance();
    playerInventory.reduceAdena("PrivateStore", price, player, _owner);
    playerIU.addItem(adenaItem);
    ownerInventory.addAdena("PrivateStore", price, _owner, player);
    ownerIU.addItem(ownerInventory.getAdenaInstance());

    for (ItemRequest item : items)
    {
      adjustItemRequest(item);
      if (item.getCount() == 0) {
        continue;
      }
      L2ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
      if (oldItem == null)
      {
        lock();
        return false;
      }

      L2ItemInstance newItem = ownerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), playerInventory, _owner, player);
      if (newItem == null) return false;
      removeItem(item.getObjectId(), -1, item.getCount());

      if ((oldItem.getCount() > 0) && (oldItem != newItem)) ownerIU.addModifiedItem(oldItem); else
        ownerIU.addRemovedItem(oldItem);
      if (newItem.getCount() > item.getCount()) playerIU.addModifiedItem(newItem); else {
        playerIU.addNewItem(newItem);
      }

      if (newItem.isStackable())
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
        msg.addString(player.getName());
        msg.addItemName(newItem.getItemId());
        msg.addNumber(item.getCount());
        _owner.sendPacket(msg);

        msg = new SystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1);
        msg.addString(_owner.getName());
        msg.addItemName(newItem.getItemId());
        msg.addNumber(item.getCount());
        player.sendPacket(msg);
      }
      else
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S2);
        msg.addString(player.getName());
        msg.addItemName(newItem.getItemId());
        _owner.sendPacket(msg);

        msg = new SystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1);
        msg.addString(_owner.getName());
        msg.addItemName(newItem.getItemId());
        player.sendPacket(msg);
      }

    }

    _owner.sendPacket(ownerIU);
    player.sendPacket(playerIU);

    return true;
  }

  public synchronized boolean PrivateStoreSell(L2PcInstance player, ItemRequest[] items, int price)
  {
    if (_locked) return false;

    PcInventory ownerInventory = _owner.getInventory();
    PcInventory playerInventory = player.getInventory();

    for (ItemRequest item : items)
    {
      L2ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
      if (oldItem == null)
        return false;
      if (oldItem.getAugmentation() != null)
      {
        String msg = "Transaction failed. Augmented items may not be exchanged.";
        _owner.sendMessage("Transaction failed. Augmented items may not be exchanged.");
        player.sendMessage("Transaction failed. Augmented items may not be exchanged.");
        return false;
      }

      boolean isOk = false;
      for (TradeItem ownerItem : _items)
      {
        if (item.getItemId() == ownerItem.getItem().getItemId())
        {
          isOk = true;
          break;
        }
      }
      if (!isOk) {
        return false;
      }

    }

    InventoryUpdate ownerIU = new InventoryUpdate();
    InventoryUpdate playerIU = new InventoryUpdate();

    for (ItemRequest item : items)
    {
      adjustItemRequestByItemId(item);
      if (item.getCount() == 0) {
        continue;
      }
      L2ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
      if (oldItem == null) return false;

      L2ItemInstance newItem = playerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), ownerInventory, player, _owner);
      if (newItem == null) return false;
      removeItem(-1, item.getItemId(), item.getCount());

      if ((oldItem.getCount() > 0) && (oldItem != newItem)) playerIU.addModifiedItem(oldItem); else
        playerIU.addRemovedItem(oldItem);
      if (newItem.getCount() > item.getCount()) ownerIU.addModifiedItem(newItem); else {
        ownerIU.addNewItem(newItem);
      }

      if (newItem.isStackable())
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1);
        msg.addString(player.getName());
        msg.addItemName(newItem.getItemId());
        msg.addNumber(item.getCount());
        _owner.sendPacket(msg);

        msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
        msg.addString(_owner.getName());
        msg.addItemName(newItem.getItemId());
        msg.addNumber(item.getCount());
        player.sendPacket(msg);
      }
      else
      {
        SystemMessage msg = new SystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1);
        msg.addString(player.getName());
        msg.addItemName(newItem.getItemId());
        _owner.sendPacket(msg);

        msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S2);
        msg.addString(_owner.getName());
        msg.addItemName(newItem.getItemId());
        player.sendPacket(msg);
      }

    }

    if (price > ownerInventory.getAdena()) return false;
    L2ItemInstance adenaItem = ownerInventory.getAdenaInstance();
    ownerInventory.reduceAdena("PrivateStore", price, _owner, player);
    ownerIU.addItem(adenaItem);
    playerInventory.addAdena("PrivateStore", price, player, _owner);
    playerIU.addItem(playerInventory.getAdenaInstance());

    _owner.sendPacket(ownerIU);
    player.sendPacket(playerIU);

    return true;
  }

  public TradeItem getItem(int objectId)
  {
    for (TradeItem item : _items)
    {
      if (item.getObjectId() == objectId)
        return item;
    }
    return null;
  }

  public synchronized TradeItem getTradeItem(int objectId)
  {
    L2Object o = L2World.getInstance().findObject(objectId);

    if ((o == null) || (!(o instanceof L2ItemInstance)))
    {
      _log.warning(_owner.getName() + ": Attempt to add invalid item to TradeList!");

      return null;
    }

    L2ItemInstance item = (L2ItemInstance)o;

    if ((!item.isTradeable()) || (item.getItemType() == L2EtcItemType.QUEST)) {
      return null;
    }
    for (TradeItem _item : _items) {
      if (_item.getObjectId() == objectId)
        return _item;
    }
    return null;
  }

  public void addItemByItemId1(int itemId, int count, int price, int enchant)
  {
  }

  public class TradeItem
  {
    private int _objectId;
    private L2Item _item;
    private int _enchant;
    private int _count;
    private int _price;

    public TradeItem(L2ItemInstance item, int count, int price)
    {
      _objectId = item.getObjectId();
      _item = item.getItem();
      _enchant = item.getEnchantLevel();
      _count = count;
      _price = price;
    }

    public TradeItem(L2Item item, int count, int price, int enchant)
    {
      _objectId = 0;
      _item = item;
      _enchant = enchant;
      _count = count;
      _price = price;
    }

    public TradeItem(TradeItem item, int count, int price)
    {
      _objectId = item.getObjectId();
      _item = item.getItem();
      _enchant = item.getEnchant();
      _count = count;
      _price = price;
    }

    public void setObjectId(int objectId)
    {
      _objectId = objectId;
    }

    public int getObjectId()
    {
      return _objectId;
    }

    public L2Item getItem()
    {
      return _item;
    }

    public void setEnchant(int enchant)
    {
      _enchant = enchant;
    }

    public int getEnchant()
    {
      return _enchant;
    }

    public void setCount(int count)
    {
      _count = count;
    }

    public int getCount()
    {
      return _count;
    }

    public void setPrice(int price)
    {
      _price = price;
    }

    public int getPrice()
    {
      return _price;
    }
  }
}