package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2p.commons.math.SafeMath;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.items.Warehouse;
import l2m.gameserver.model.items.Warehouse.WarehouseType;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);
  private int _count;
  private int[] _items;
  private long[] _itemQ;

  protected void readImpl()
  {
    _count = readD();
    if ((_count * 12 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new int[_count];
    _itemQ = new long[_count];
    for (int i = 0; i < _count; i++)
    {
      _items[i] = readD();
      _itemQ[i] = readQ();
      if ((_itemQ[i] >= 1L) && (ArrayUtils.indexOf(_items, _items[i]) >= i))
        continue;
      _count = 0;
      break;
    }
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_count == 0)) {
      return;
    }
    if (!activeChar.getPlayerAccess().UseWarehouse)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }

    NpcInstance whkeeper = activeChar.getLastNpc();
    if ((whkeeper == null) || (!activeChar.isInRange(whkeeper, 200L)))
    {
      activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
      return;
    }

    Warehouse warehouse = null;
    String logType = null;

    if (activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.PRIVATE)
    {
      warehouse = activeChar.getWarehouse();
      logType = "WarehouseWithdraw";
    }
    else if (activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.CLAN)
    {
      logType = "ClanWarehouseWithdraw";
      boolean canWithdrawCWH = false;
      if ((activeChar.getClan() != null) && 
        ((activeChar.getClanPrivileges() & 0x8) == 8) && (
        (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE) || (activeChar.isClanLeader()) || (activeChar.getVarB("canWhWithdraw"))))
        canWithdrawCWH = true;
      if (!canWithdrawCWH) {
        return;
      }
      warehouse = activeChar.getClan().getWarehouse();
    }
    else if (activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.FREIGHT)
    {
      warehouse = activeChar.getFreight();
      logType = "FreightWithdraw";
    }
    else
    {
      _log.warn("Error retrieving a warehouse object for char " + activeChar.getName() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
      return;
    }

    PcInventory inventory = activeChar.getInventory();

    inventory.writeLock();
    warehouse.writeLock();
    try
    {
      long weight = 0L;
      int slots = 0;

      for (int i = 0; i < _count; i++)
      {
        ItemInstance item = warehouse.getItemByObjectId(_items[i]);
        if ((item == null) || (item.getCount() < _itemQ[i])) {
          activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
          return;
        }
        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getTemplate().getWeight(), _itemQ[i]));
        if ((!item.isStackable()) || (inventory.getItemByItemId(item.getItemId()) == null)) {
          slots++;
        }
      }
      if (!activeChar.getInventory().validateCapacity(slots)) {
        activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
        return;
      }
      if (!activeChar.getInventory().validateWeight(weight)) {
        activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
        return;
      }
      for (int i = 0; i < _count; i++)
      {
        ItemInstance item = warehouse.removeItemByObjectId(_items[i], _itemQ[i]);
        Log.LogItem(activeChar, logType, item);
        activeChar.getInventory().addItem(item);
      }

    }
    catch (ArithmeticException ae) {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }
    finally {
      warehouse.writeUnlock();
      inventory.writeUnlock();
    }

    activeChar.sendChanges();
    activeChar.sendPacket(Msg.THE_TRANSACTION_IS_COMPLETE);
  }
}