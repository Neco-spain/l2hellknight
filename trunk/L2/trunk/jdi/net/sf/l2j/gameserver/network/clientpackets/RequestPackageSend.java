package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcFreight;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2EtcItemType;

public final class RequestPackageSend extends L2GameClientPacket
{
  private static final String _C_9F_REQUESTPACKAGESEND = "[C] 9F RequestPackageSend";
  private static Logger _log = Logger.getLogger(RequestPackageSend.class.getName());
  private List<Item> _items;
  private int _objectID;
  private int _count;

  public RequestPackageSend()
  {
    _items = new FastList();
  }

  protected void readImpl()
  {
    _objectID = readD();
    _count = readD();
    if ((_count < 0) || (_count > 500))
    {
      _count = -1;
      return;
    }
    for (int i = 0; i < _count; i++)
    {
      int id = readD();
      int count = readD();
      _items.add(new Item(id, count));
    }
  }

  protected void runImpl()
  {
    if ((_count == -1) || (_items == null)) {
      return;
    }
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if (player.getObjectId() == _objectID) {
      return;
    }
    if (L2World.getInstance().getPlayer(_objectID) != null) {
      return;
    }
    if (player.getAccountChars().size() < 1)
      return;
    if (!player.getAccountChars().containsKey(Integer.valueOf(_objectID))) {
      return;
    }
    L2PcInstance target = L2PcInstance.load(_objectID);

    PcFreight freight = target.getFreight();

    ((L2GameClient)getClient()).getActiveChar().setActiveWarehouse(freight);

    target.deleteMe();

    ItemContainer warehouse = player.getActiveWarehouse();

    if (warehouse == null) {
      return;
    }
    L2FolkInstance manager = player.getLastFolkNPC();

    if (((manager == null) || (!player.isInsideRadius(manager, 150, false, false))) && (!player.isGM())) return;

    if (((warehouse instanceof PcFreight)) && (Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE) && (player.getKarma() > 0)) return;

    int fee = _count * Config.ALT_GAME_FREIGHT_PRICE;
    int currentAdena = player.getAdena();
    int slots = 0;

    for (Item i : _items)
    {
      int objectId = i.id;
      int count = i.count;

      L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
      if (item == null)
      {
        _log.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
        i.id = 0;
        i.count = 0;
        continue;
      }

      if ((!item.isTradeable()) || (item.getItemType() == L2EtcItemType.QUEST)) return;

      if (item.getItemId() == 57) currentAdena -= count;
      if (!item.isStackable()) slots += count;
      else if (warehouse.getItemByItemId(item.getItemId()) == null) slots++;

    }

    if (!warehouse.validateCapacity(slots))
    {
      sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
      return;
    }

    if ((currentAdena < fee) || (!player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false)))
    {
      sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
      return;
    }

    InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
    for (Item i : _items)
    {
      int objectId = i.id;
      int count = i.count;

      if ((objectId == 0) && (count == 0))
        continue;
      L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
      if (oldItem == null)
      {
        _log.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
        continue;
      }

      int itemId = oldItem.getItemId();

      if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842)) {
        continue;
      }
      L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
      if (newItem == null)
      {
        _log.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
        continue;
      }

      if (playerIU != null)
      {
        if ((oldItem.getCount() > 0) && (oldItem != newItem)) playerIU.addModifiedItem(oldItem); else {
          playerIU.addRemovedItem(oldItem);
        }
      }
    }

    if (playerIU != null) player.sendPacket(playerIU); else {
      player.sendPacket(new ItemList(player, false));
    }

    StatusUpdate su = new StatusUpdate(player.getObjectId());
    su.addAttribute(14, player.getCurrentLoad());
    player.sendPacket(su);
  }

  public String getType()
  {
    return "[C] 9F RequestPackageSend";
  }

  private class Item {
    public int id;
    public int count;

    public Item(int i, int c) {
      id = i;
      count = c;
    }
  }
}