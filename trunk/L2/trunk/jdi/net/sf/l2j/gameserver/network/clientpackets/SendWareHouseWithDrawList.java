package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ClanWarehouse;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.FloodProtector;

public final class SendWareHouseWithDrawList extends L2GameClientPacket
{
  private static final String _C__32_SENDWAREHOUSEWITHDRAWLIST = "[C] 32 SendWareHouseWithDrawList";
  private static Logger _log = Logger.getLogger(SendWareHouseWithDrawList.class.getName());
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _count = readD();
    if ((_count < 0) || (_count * 8 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
    {
      _count = 0;
      _items = null;
      return;
    }
    _items = new int[_count * 2];
    for (int i = 0; i < _count; i++)
    {
      int objectId = readD();
      _items[(i * 2 + 0)] = objectId;
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt < 0L))
      {
        _count = 0; _items = null;
        return;
      }
      _items[(i * 2 + 1)] = (int)cnt;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;
    ItemContainer warehouse = player.getActiveWarehouse();
    if (warehouse == null) return;
    L2FolkInstance manager = player.getLastFolkNPC();
    if (((manager == null) || (!player.isInsideRadius(manager, 150, false, false))) && (!player.isGM())) return;

    if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), 14))
    {
      return;
    }
    if (((warehouse instanceof ClanWarehouse)) && (Config.GM_DISABLE_TRANSACTION) && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      player.sendMessage("Transactions are disable for your Access Level");
      return;
    }
    if ((warehouse instanceof ClanWarehouse))
    {
      L2PcInstance tmp_own = ((ClanWarehouse)warehouse).getOwner();

      if (tmp_own == null) {
        return;
      }
      if (tmp_own.getClanId() != player.getClanId())
      {
        player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u0437\u0430\u0431\u0440\u0430\u0442\u044C \u0432\u0435\u0449\u0438, \u0442.\u043A. \u044D\u0442\u043E \u0432\u0435\u0449\u0438 \u0432\u0430\u0448\u0435\u0433\u043E \u043F\u0440\u0435\u0434\u044B\u0434\u0443\u0449\u0435\u0433\u043E \u043A\u043B\u0430\u043D\u0430");
        return;
      }
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE) && (player.getKarma() > 0)) return;

    if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
    {
      if (((warehouse instanceof ClanWarehouse)) && ((player.getClanPrivileges() & 0x8) != 8))
      {
        return;
      }

    }
    else if (((warehouse instanceof ClanWarehouse)) && (!player.isClanLeader()))
    {
      player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE));
      return;
    }

    int weight = 0;
    int slots = 0;

    for (int i = 0; i < _count; i++)
    {
      int objectId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];

      L2ItemInstance item = warehouse.getItemByObjectId(objectId);
      if (item != null) {
        weight += weight * item.getItem().getWeight();
        if (!item.isStackable()) { slots += count; } else {
          if (player.getInventory().getItemByItemId(item.getItemId()) != null) continue; slots++;
        }
      }
    }
    if (!player.getInventory().validateCapacity(slots))
    {
      sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
      return;
    }

    if (!player.getInventory().validateWeight(weight))
    {
      sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
      return;
    }

    InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
    for (int i = 0; i < _count; i++)
    {
      int objectId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];

      L2ItemInstance oldItem = warehouse.getItemByObjectId(objectId);
      if ((oldItem == null) || (oldItem.getCount() < count))
        player.sendMessage(new StringBuilder().append("Can't withdraw requested item").append(count > 1 ? "s" : "").toString());
      L2ItemInstance newItem = warehouse.transferItem("Warehouse", objectId, count, player.getInventory(), player, player.getLastFolkNPC());
      if (newItem == null)
      {
        _log.warning(new StringBuilder().append("Error withdrawing a warehouse object for char ").append(player.getName()).toString());
      }
      else
      {
        if (playerIU == null)
          continue;
        if (newItem.getCount() > count) playerIU.addModifiedItem(newItem); else {
          playerIU.addNewItem(newItem);
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
    return "[C] 32 SendWareHouseWithDrawList";
  }
}