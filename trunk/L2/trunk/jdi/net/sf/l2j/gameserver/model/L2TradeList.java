package net.sf.l2j.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public class L2TradeList
{
  private static Logger _log = Logger.getLogger(L2TradeList.class.getName());
  private List<L2ItemInstance> _items;
  private int _listId;
  private boolean _confirmed;
  private String _buystorename;
  private String _sellstorename;
  private String _npcId;

  public L2TradeList(int listId)
  {
    _items = new FastList();
    _listId = listId;
    _confirmed = false;
  }

  public void setNpcId(String id)
  {
    _npcId = id;
  }

  public String getNpcId()
  {
    return _npcId;
  }

  public void addItem(L2ItemInstance item)
  {
    _items.add(item);
  }

  public void replaceItem(int itemID, int price)
  {
    for (int i = 0; i < _items.size(); i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if (item.getItemId() != itemID)
        continue;
      item.setPriceToSell(price);
    }
  }

  public boolean decreaseCount(int itemID, int count)
  {
    for (int i = 0; i < _items.size(); i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if (item.getItemId() != itemID)
        continue;
      int newCount = item.getCount() - count;
      if (newCount < 0) {
        continue;
      }
      item.setCount(newCount);
      return true;
    }

    return false;
  }

  public void restoreCount(int time)
  {
    for (int i = 0; i < _items.size(); i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if ((!item.getCountDecrease()) || (item.getTime() != time))
        continue;
      item.restoreInitCount();
    }
  }

  public void removeItem(int itemID)
  {
    for (int i = 0; i < _items.size(); i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if (item.getItemId() != itemID)
        continue;
      _items.remove(i);
    }
  }

  public int getListId()
  {
    return _listId;
  }

  public void setSellStoreName(String name) {
    _sellstorename = name;
  }

  public String getSellStoreName() {
    return _sellstorename;
  }

  public void setBuyStoreName(String name) {
    _buystorename = name;
  }

  public String getBuyStoreName() {
    return _buystorename;
  }

  public List<L2ItemInstance> getItems()
  {
    return _items;
  }

  public List<L2ItemInstance> getItems(int start, int end)
  {
    return _items.subList(start, end);
  }

  public int getPriceForItemId(int itemId)
  {
    for (int i = 0; i < _items.size(); i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if (item.getItemId() == itemId)
      {
        return item.getPriceToSell();
      }
    }
    return -1;
  }

  public boolean countDecrease(int itemId) {
    for (int i = 0; i < _items.size(); i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if (item.getItemId() == itemId)
      {
        return item.getCountDecrease();
      }
    }
    return false;
  }

  public boolean containsItemId(int itemId) {
    for (L2ItemInstance item : _items)
    {
      if (item.getItemId() == itemId) {
        return true;
      }
    }
    return false;
  }

  public L2ItemInstance getItem(int ObjectId) {
    for (int i = 0; i < _items.size(); i++)
    {
      L2ItemInstance item = (L2ItemInstance)_items.get(i);
      if (item.getObjectId() == ObjectId)
      {
        return item;
      }
    }
    return null;
  }

  public synchronized void setConfirmedTrade(boolean x) {
    _confirmed = x;
  }

  public synchronized boolean hasConfirmed() {
    return _confirmed;
  }

  public void removeItem(int objId, int count)
  {
    for (int y = 0; y < _items.size(); y++)
    {
      L2ItemInstance temp = (L2ItemInstance)_items.get(y);
      if (temp.getObjectId() != objId)
        continue;
      if (count != temp.getCount())
        break;
      _items.remove(temp); break;
    }
  }

  public boolean contains(int objId)
  {
    boolean bool = false;

    for (int y = 0; y < _items.size(); y++)
    {
      L2ItemInstance temp = (L2ItemInstance)_items.get(y);
      if (temp.getObjectId() != objId)
        continue;
      bool = true;
      break;
    }

    return bool;
  }

  public boolean validateTrade(L2PcInstance player)
  {
    Inventory playersInv = player.getInventory();

    for (int y = 0; y < _items.size(); y++)
    {
      L2ItemInstance temp = (L2ItemInstance)_items.get(y);
      L2ItemInstance playerItem = playersInv.getItemByObjectId(temp.getObjectId());
      if ((playerItem == null) || (playerItem.getCount() < temp.getCount()))
        return false;
    }
    return true;
  }

  public void tradeItems(L2PcInstance player, L2PcInstance reciever)
  {
    Inventory playersInv = player.getInventory();
    Inventory recieverInv = reciever.getInventory();

    InventoryUpdate update = new InventoryUpdate();
    ItemTable itemTable = ItemTable.getInstance();

    for (int y = 0; y < _items.size(); y++)
    {
      L2ItemInstance temp = (L2ItemInstance)_items.get(y);
      L2ItemInstance playerItem = playersInv.getItemByObjectId(temp.getObjectId());

      if (playerItem == null)
        continue;
      L2ItemInstance newitem = itemTable.createItem("L2TradeList", playerItem.getItemId(), playerItem.getCount(), player);
      newitem.setEnchantLevel(temp.getEnchantLevel());

      changePetItemObjectId(playerItem.getObjectId(), newitem.getObjectId());

      if ((reciever.isGM()) || (player.isGM()))
      {
        L2PcInstance target;
        L2PcInstance gm;
        L2PcInstance target;
        if (reciever.isGM()) {
          L2PcInstance gm = reciever;
          target = player;
        } else {
          gm = player;
          target = reciever;
        }
        GMAudit.auditGMAction(gm.getName(), "trade", target.getName(), newitem.getItem().getName() + " - " + newitem.getItemId());
      }
      playerItem = playersInv.destroyItem("!L2TradeList!", playerItem.getObjectId(), temp.getCount(), null, null);
      L2ItemInstance recieverItem = recieverInv.addItem("!L2TradeList!", newitem, null, null);

      if (playerItem == null)
      {
        _log.warning("L2TradeList: PlayersInv.destroyItem returned NULL!");
      }
      else
      {
        if (playerItem.getLastChange() == 2)
        {
          update.addModifiedItem(playerItem);
        }
        else
        {
          L2World world = L2World.getInstance();
          world.removeObject(playerItem);
          update.addRemovedItem(playerItem);
        }

        player.sendPacket(update);

        update = new InventoryUpdate();
        if (recieverItem.getLastChange() == 2)
        {
          update.addModifiedItem(recieverItem);
        }
        else
        {
          update.addNewItem(recieverItem);
        }

        reciever.sendPacket(update);
      }
    }

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);

    su = new StatusUpdate(reciever.getObjectId());
    su.addAttribute(14, reciever.getCurrentLoad());
    reciever.sendPacket(su);
  }

  private void changePetItemObjectId(int oldObjectId, int newObjectId)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("UPDATE pets SET item_obj_id = ? WHERE item_obj_id = ?");
      statement.setInt(1, newObjectId);
      statement.setInt(2, oldObjectId);
      statement.executeUpdate();
      statement.close();
    } catch (Exception e) {
      _log.warning("could not change pet item object id: " + e); } finally {
      try { con.close();
      } catch (Exception e)
      {
      }
    }
  }

  public void updateBuyList(L2PcInstance player, List<TradeItem> list) {
    Inventory playersInv = player.getInventory();

    int count = 0;

    while (count != list.size())
    {
      TradeItem temp = (TradeItem)list.get(count);
      L2ItemInstance temp2 = playersInv.getItemByItemId(temp.getItemId());
      if (temp2 == null)
      {
        list.remove(count);
        count -= 1;
      }
      else if (temp.getCount() == 0)
      {
        list.remove(count);
        count -= 1;
      }

      count++;
    }
  }

  public void updateSellList(L2PcInstance player, List<TradeItem> list)
  {
    Inventory playersInv = player.getInventory();

    int count = 0;
    while (count != list.size())
    {
      TradeItem temp = (TradeItem)list.get(count);
      L2ItemInstance temp2 = playersInv.getItemByObjectId(temp.getObjectId());
      if (temp2 == null)
      {
        list.remove(count);
        count -= 1;
      }
      else if (temp2.getCount() < temp.getCount())
      {
        temp.setCount(temp2.getCount());
      }

      count++;
    }
  }

  public synchronized void buySellItems(L2PcInstance buyer, List<TradeItem> buyerslist, L2PcInstance seller, List<TradeItem> sellerslist)
  {
    Inventory sellerInv = seller.getInventory();
    Inventory buyerInv = buyer.getInventory();

    TradeItem temp2 = null;

    L2ItemInstance sellerItem = null;
    L2ItemInstance temp = null;
    L2ItemInstance newitem = null;
    L2ItemInstance adena = null;
    int enchantLevel = 0;

    InventoryUpdate buyerupdate = new InventoryUpdate();
    InventoryUpdate sellerupdate = new InventoryUpdate();

    ItemTable itemTable = ItemTable.getInstance();

    int amount = 0;
    int x = 0;
    int y = 0;

    List sysmsgs = new FastList();
    SystemMessage msg = null;

    for (TradeItem buyerItem : buyerslist)
    {
      for (x = 0; x < sellerslist.size(); x++)
      {
        temp2 = (TradeItem)sellerslist.get(x);
        if (temp2.getItemId() != buyerItem.getItemId())
          continue;
        sellerItem = sellerInv.getItemByItemId(buyerItem.getItemId());
        break;
      }

      if (sellerItem != null)
      {
        if (buyerItem.getCount() > temp2.getCount())
        {
          amount = temp2.getCount();
        }
        if (buyerItem.getCount() > sellerItem.getCount())
        {
          amount = sellerItem.getCount();
        }
        else
        {
          amount = buyerItem.getCount();
        }
        if (buyerItem.getCount() > 2147483647 / buyerItem.getOwnersPrice())
        {
          _log.warning("Integer Overflow on Cost. Possible Exploit attempt between " + buyer.getName() + " and " + seller.getName() + ".");
          return;
        }

        enchantLevel = sellerItem.getEnchantLevel();
        sellerItem = sellerInv.destroyItem("", sellerItem.getObjectId(), amount, null, null);

        newitem = itemTable.createItem("L2TradeList", sellerItem.getItemId(), amount, buyer, seller);
        newitem.setEnchantLevel(enchantLevel);
        temp = buyerInv.addItem("", newitem, null, null);
        if (amount == 1)
        {
          msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S2);
          msg.addString(buyer.getName());
          msg.addItemName(sellerItem.getItemId());
          sysmsgs.add(msg);
          msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S2);
          msg.addString("You");
          msg.addItemName(sellerItem.getItemId());
          sysmsgs.add(msg);
        }
        else
        {
          msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
          msg.addString(buyer.getName());
          msg.addItemName(sellerItem.getItemId());
          msg.addNumber(amount);
          sysmsgs.add(msg);
          msg = new SystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
          msg.addString("You");
          msg.addItemName(sellerItem.getItemId());
          msg.addNumber(amount);
          sysmsgs.add(msg);
        }
        if (temp2.getCount() == buyerItem.getCount())
        {
          sellerslist.remove(temp2);
          buyerItem.setCount(0);
        }
        else if (buyerItem.getCount() < temp2.getCount())
        {
          temp2.setCount(temp2.getCount() - buyerItem.getCount());
        }
        else
        {
          buyerItem.setCount(buyerItem.getCount() - temp2.getCount());
        }

        if (sellerItem.getLastChange() == 2)
        {
          sellerupdate.addModifiedItem(sellerItem);
        }
        else
        {
          L2World world = L2World.getInstance();
          world.removeObject(sellerItem);
          sellerupdate.addRemovedItem(sellerItem);
        }

        if (temp.getLastChange() == 2)
        {
          buyerupdate.addModifiedItem(temp);
        }
        else
        {
          buyerupdate.addNewItem(temp);
        }

        sellerItem = null;
      }
    }
    if (newitem != null)
    {
      adena = seller.getInventory().getAdenaInstance();
      adena.setLastChange(2);
      sellerupdate.addModifiedItem(adena);
      adena = buyer.getInventory().getAdenaInstance();
      adena.setLastChange(2);
      buyerupdate.addModifiedItem(adena);

      seller.sendPacket(sellerupdate);
      buyer.sendPacket(buyerupdate);
      y = 0;

      for (x = 0; x < sysmsgs.size(); x++)
      {
        if (y == 0)
        {
          seller.sendPacket((L2GameServerPacket)sysmsgs.get(x));
          y = 1;
        }
        else
        {
          buyer.sendPacket((L2GameServerPacket)sysmsgs.get(x));
          y = 0;
        }
      }
    }
  }
}