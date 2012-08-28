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
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.util.FloodProtector;

public final class SendWareHouseDepositList extends L2GameClientPacket
{
  private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";
  private static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _count = readD();

    if ((_count < 0) || (_count * 8 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
    {
      _count = 0;
    }

    _items = new int[_count * 2];
    for (int i = 0; i < _count; i++)
    {
      int objectId = readD();
      _items[(i * 2 + 0)] = objectId;
      long cnt = readD();
      if ((cnt > 2147483647L) || (cnt < 0L))
      {
        _count = 0;
        _items = null;
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
    if (player.isDead())
    {
      player.sendPacket(new ActionFailed());
      player.sendMessage("Cannot use WH while dead!");

      return;
    }
    if (player.getActiveTradeList() != null)
    {
      player.cancelActiveTrade();
    }

    if (player.getActiveEnchantItem() != null)
    {
      player.sendPacket(new ActionFailed());
      player.sendMessage("Close your ecnhant window first!");

      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE) && (player.getKarma() > 0)) return;

    int fee = _count * 30;
    int currentAdena = player.getAdena();
    int slots = 0;

    for (int i = 0; i < _count; i++)
    {
      int objectId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];

      L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
      if (item == null)
      {
        _log.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
        _items[(i * 2 + 0)] = 0;
        _items[(i * 2 + 1)] = 0;
      }
      else
      {
        if (item.isEquipped())
        {
          L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
          InventoryUpdate iu = new InventoryUpdate();
          for (int y = 0; y < unequiped.length; y++)
          {
            iu.addModifiedItem(unequiped[y]);
          }
          player.sendPacket(iu);
          player.broadcastUserInfo();
        }

        if ((((warehouse instanceof ClanWarehouse)) && (!item.isTradeable())) || (item.getItemType() == L2EtcItemType.QUEST)) return;
        if (item.getItemId() == 57) currentAdena -= count;
        if (!item.isStackable()) { slots += count; } else {
          if (warehouse.getItemByItemId(item.getItemId()) != null) continue; slots++;
        }
      }
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
    for (int i = 0; i < _count; i++)
    {
      int objectId = _items[(i * 2 + 0)];
      int count = _items[(i * 2 + 1)];

      if ((objectId == 0) && (count == 0))
        continue;
      L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
      if (oldItem == null)
      {
        _log.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
      }
      else
      {
        int itemId = oldItem.getItemId();

        if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842)) {
          continue;
        }
        L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
        if (newItem == null)
        {
          _log.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
        }
        else
        {
          if (playerIU == null)
            continue;
          if ((oldItem.getCount() > 0) && (oldItem != newItem)) playerIU.addModifiedItem(oldItem); else
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
    return "[C] 31 SendWareHouseDepositList";
  }
}