package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2p.commons.math.SafeMath;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.model.items.Warehouse.WarehouseType;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class SendWareHouseDepositList extends L2GameClientPacket
{
  private static final long _WAREHOUSE_FEE = 30L;
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
      return;
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
    if ((whkeeper == null) || (!activeChar.isInRangeZ(whkeeper, 200L)))
    {
      activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
      return;
    }

    PcInventory inventory = activeChar.getInventory();
    boolean privatewh = activeChar.getUsingWarehouseType() != Warehouse.WarehouseType.CLAN;
    Warehouse warehouse;
    Warehouse warehouse;
    if (privatewh)
      warehouse = activeChar.getWarehouse();
    else {
      warehouse = activeChar.getClan().getWarehouse();
    }
    inventory.writeLock();
    warehouse.writeLock();
    try
    {
      int slotsleft = 0;
      long adenaDeposit = 0L;

      if (privatewh)
        slotsleft = activeChar.getWarehouseLimit() - warehouse.getSize();
      else {
        slotsleft = activeChar.getClan().getWhBonus() + Config.WAREHOUSE_SLOTS_CLAN - warehouse.getSize();
      }
      int items = 0;

      for (int i = 0; i < _count; i++)
      {
        ItemInstance item = inventory.getItemByObjectId(_items[i]);
        if ((item == null) || (item.getCount() < _itemQ[i]) || (!item.canBeStored(activeChar, privatewh)))
        {
          _items[i] = 0;
          _itemQ[i] = 0L;
        }
        else if ((!item.isStackable()) || (warehouse.getItemByItemId(item.getItemId()) == null))
        {
          if (slotsleft <= 0)
          {
            _items[i] = 0;
            _itemQ[i] = 0L;
          }
          else {
            slotsleft--;
          }
        } else {
          if (item.getItemId() == 57) {
            adenaDeposit = _itemQ[i];
          }
          items++;
        }
      }

      if (slotsleft <= 0) {
        activeChar.sendPacket(Msg.YOUR_WAREHOUSE_IS_FULL);
      }
      if (items == 0)
      {
        activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        return;
      }
      long fee = SafeMath.mulAndCheck(items, 30L);

      if (fee + adenaDeposit > activeChar.getAdena()) {
        activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
        return;
      }
      if (!activeChar.reduceAdena(fee, true)) {
        sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }
      for (int i = 0; i < _count; i++)
      {
        if (_items[i] == 0)
          continue;
        ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
        Log.LogItem(activeChar, privatewh ? "WarehouseDeposit" : "ClanWarehouseDeposit", item);
        warehouse.addItem(item);
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