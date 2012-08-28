package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.ClanWarehouse;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Log;

public final class SendWareHouseDepositList extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());
  private HashMap<Integer, Integer> _items;
  private FastTable<L2ItemInstance> _itemsOnWarehouse;

  protected void readImpl()
  {
    int itemsCount = readD();
    if ((itemsCount * 8 > _buf.remaining()) || (itemsCount > Config.MAX_ITEM_IN_PACKET) || (itemsCount <= 0)) {
      _items = null;
      return;
    }

    _items = new HashMap(itemsCount + 1, 0.999F);
    for (int i = 0; i < itemsCount; i++) {
      int obj_id = readD();
      int itemQuantity = readD();
      if (itemQuantity <= 0) {
        _items = null;
        return;
      }
      _items.put(Integer.valueOf(obj_id), Integer.valueOf(itemQuantity));
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (_items == null)) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPAY() < 100L) {
      player.sendActionFailed();
      return;
    }

    player.sCPAY();

    ItemContainer warehouse = player.getActiveWarehouse();
    if (warehouse == null) {
      return;
    }

    L2FolkInstance manager = player.getLastFolkNPC();
    if (((manager == null) || (!player.isInsideRadius(manager, 150, false, false))) && (!player.isGM())) {
      return;
    }

    if (player.getActiveEnchantItem() != null)
    {
      player.setActiveEnchantItem(null);
      player.sendPacket(new EnchantResult(0, true));
      player.sendActionFailed();
      return;
    }

    if (player.getActiveTradeList() != null) {
      player.cancelActiveTrade();
      player.sendActionFailed();
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE) && (player.getKarma() > 0)) {
      return;
    }

    PcInventory inventory = player.getInventory();
    int slotsleft = 0;
    int adenaDeposit = 0;
    int whType = 0;

    _itemsOnWarehouse = new FastTable();
    if ((warehouse instanceof ClanWarehouse)) {
      _itemsOnWarehouse.addAll(warehouse.listItems(2));
      whType = 2;
      slotsleft = 211 - _itemsOnWarehouse.size();
    } else {
      _itemsOnWarehouse.addAll(warehouse.listItems(1));
      whType = 1;
      slotsleft = player.getWareHouseLimit() - _itemsOnWarehouse.size();
    }

    FastTable stackableList = new FastTable();
    int i = 0; for (int n = _itemsOnWarehouse.size(); i < n; i++) {
      L2ItemInstance itm = (L2ItemInstance)_itemsOnWarehouse.get(i);
      if (itm.isStackable()) {
        stackableList.add(Integer.valueOf(itm.getItemId()));
      }

    }

    FastTable itemsToStoreList = new FastTable();
    for (Integer itemObjectId : _items.keySet()) {
      L2ItemInstance item = inventory.getItemByObjectId(itemObjectId.intValue());
      if ((item == null) || (item.isEquipped()) || (
        ((warehouse instanceof ClanWarehouse)) && (!item.isDropable())))
      {
        continue;
      }
      if ((!item.isStackable()) || (!stackableList.contains(Integer.valueOf(item.getItemId()))))
      {
        if (slotsleft == 0)
        {
          continue;
        }
        slotsleft--;
      }
      if (item.getItemId() == 57) {
        adenaDeposit = ((Integer)_items.get(itemObjectId)).intValue();
      }
      itemsToStoreList.add(item);
    }
    stackableList.clear();
    stackableList = null;

    int fee = itemsToStoreList.size() * 30;
    if (fee + adenaDeposit > player.getAdena()) {
      sendPacket(Static.YOU_NOT_ENOUGH_ADENA);
      return;
    }

    if (slotsleft == 0) {
      sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    String date = "";
    TextBuilder tb = null;
    if (Config.LOG_ITEMS) {
      date = Log.getTime();
      tb = new TextBuilder();
    }
    int i = 0; for (int n = itemsToStoreList.size(); i < n; i++) {
      L2ItemInstance itemToStore = (L2ItemInstance)itemsToStoreList.get(i);
      L2ItemInstance itemDropped = inventory.dropItem("depositwh", itemToStore.getObjectId(), ((Integer)_items.get(Integer.valueOf(itemToStore.getObjectId()))).intValue(), player, player.getLastFolkNPC(), true);
      warehouse.addItem(itemDropped, whType);
      if ((Config.LOG_ITEMS) && (itemDropped != null)) {
        String act = "DEPOSIT " + itemDropped.getItemName() + "(" + itemDropped.getCount() + ")(+" + itemDropped.getEnchantLevel() + ")(" + itemDropped.getObjectId() + ")(npc:" + manager.getTemplate().npcId + ") #(player " + player.getName() + ", account: " + player.getAccountName() + ", ip: " + player.getIP() + ", hwid: " + player.getHWID() + ")";
        tb.append(date + act + "\n");
      }
    }
    if ((Config.LOG_ITEMS) && (tb != null)) {
      Log.item(tb.toString(), 2);
      tb.clear();
      tb = null;
    }
    player.sendItems(true);

    player.sendChanges();
    _items.clear();
    _items = null;
    itemsToStoreList.clear();
    itemsToStoreList = null;
  }

  public String getType()
  {
    return "[C] SendWareHouseDepositList";
  }
}