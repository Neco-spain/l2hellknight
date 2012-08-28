package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcFreight;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Log;

public final class RequestPackageSend extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(RequestPackageSend.class.getName());
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
    if ((_count < 0) || (_count > 500)) {
      _count = -1;
      return;
    }
    for (int i = 0; i < _count; i++) {
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

    if (System.currentTimeMillis() - player.gCPBE() < 200L) {
      player.sendActionFailed();
      return;
    }

    player.sCPBE();

    if (player.getObjectId() == _objectID)
    {
      player.sendActionFailed();
      return;
    }

    L2PcInstance tg = L2World.getInstance().getPlayer(_objectID);
    if (tg != null)
    {
      player.sendActionFailed();
      return;
    }

    L2PcInstance target = L2PcInstance.load(_objectID);
    PcFreight freight = target.getFreight();
    player.setActiveWarehouse(freight);
    target.deleteMe();

    ItemContainer warehouse = player.getActiveWarehouse();
    if (warehouse == null) {
      player.sendActionFailed();
      return;
    }

    if (player.isTransactionInProgress()) {
      player.sendActionFailed();
      return;
    }

    if (!player.tradeLeft()) {
      player.sendActionFailed();
      return;
    }

    if (player.getActiveTradeList() != null) {
      player.cancelActiveTrade();
      player.sendActionFailed();
      return;
    }

    if (player.getPrivateStoreType() != 0) {
      player.sendActionFailed();
      return;
    }

    L2FolkInstance manager = player.getLastFolkNPC();
    if (((manager == null) || (!player.isInsideRadius(manager, 150, false, false))) && (!player.isGM())) {
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE) && (player.getKarma() > 0)) {
      return;
    }

    int fee = _count * Config.ALT_GAME_FREIGHT_PRICE;
    int currentAdena = player.getAdena();
    int slots = 0;

    for (Item i : _items) {
      int objectId = i.id;
      int count = i.count;

      L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
      if (item == null) {
        _log.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
        i.id = 0;
        i.count = 0;
        continue;
      }

      if ((!item.isTradeable()) || (item.getItemType() == L2EtcItemType.QUEST)) {
        return;
      }

      if (item.getItemId() == 57) {
        currentAdena -= count;
      }
      if (!item.isStackable())
        slots += count;
      else if (warehouse.getItemByItemId(item.getItemId()) == null) {
        slots++;
      }

    }

    if (!warehouse.validateCapacity(slots)) {
      sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    if ((currentAdena < fee) || (!player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))) {
      sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
      return;
    }

    String date = "";
    TextBuilder tb = null;
    if (Config.LOG_ITEMS) {
      date = Log.getTime();
      tb = new TextBuilder();
    }
    InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
    for (Item i : _items) {
      int objectId = i.id;
      int count = i.count;

      if ((objectId == 0) && (count == 0))
      {
        continue;
      }
      L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
      if (oldItem == null) {
        _log.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
        continue;
      }

      int itemId = oldItem.getItemId();

      if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842))
      {
        continue;
      }

      L2ItemInstance newItem = player.getInventory().dropItem("depositwh", objectId, count, player, player.getLastFolkNPC(), true);
      warehouse.addItem(newItem, 4);

      if (playerIU != null) {
        if ((oldItem.getCount() > 0) && (oldItem != newItem))
          playerIU.addModifiedItem(oldItem);
        else {
          playerIU.addRemovedItem(oldItem);
        }
      }
      if ((Config.LOG_ITEMS) && (newItem != null)) {
        String act = "FREIGHT " + newItem.getItemName() + "(" + count + ")(+" + newItem.getEnchantLevel() + ")(" + objectId + ")(npc:" + manager.getTemplate().npcId + ") #(player " + player.getName() + ", account: " + player.getAccountName() + ", ip: " + player.getIP() + ", hwid: " + player.getHWID() + ")";
        tb.append(date + act + "\n");
      }
    }
    if ((Config.LOG_ITEMS) && (tb != null)) {
      Log.item(tb.toString(), 2);
      tb.clear();
      tb = null;
    }

    if (playerIU != null)
      player.sendPacket(playerIU);
    else {
      player.sendItems(false);
    }

    player.sendChanges();
    _items.clear();
    _items = null;
  }
  private static class Item {
    public int id;
    public int count;

    public Item(int i, int c) {
      id = i;
      count = c;
    }
  }
}