package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.Map;
import l2p.commons.math.SafeMath;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcFreight;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class RequestPackageSend extends L2GameClientPacket
{
  private static final long _FREIGHT_FEE = 1000L;
  private int _objectId;
  private int _count;
  private int[] _items;
  private long[] _itemQ;

  protected void readImpl()
  {
    _objectId = readD();
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
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (_count == 0)) {
      return;
    }
    if (!player.getPlayerAccess().UseWarehouse)
    {
      player.sendActionFailed();
      return;
    }

    if (player.isActionsDisabled())
    {
      player.sendActionFailed();
      return;
    }

    if (player.isInStoreMode())
    {
      player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (player.isInTrade())
    {
      player.sendActionFailed();
      return;
    }

    NpcInstance whkeeper = player.getLastNpc();
    if ((whkeeper == null) || (!player.isInRangeZ(whkeeper, 200L))) {
      return;
    }
    if (!player.getAccountChars().containsKey(Integer.valueOf(_objectId))) {
      return;
    }
    PcInventory inventory = player.getInventory();
    PcFreight freight = new PcFreight(_objectId);
    freight.restore();

    inventory.writeLock();
    freight.writeLock();
    try
    {
      int slotsleft = 0;
      long adenaDeposit = 0L;

      slotsleft = Config.FREIGHT_SLOTS - freight.getSize();

      int items = 0;

      for (int i = 0; i < _count; i++)
      {
        ItemInstance item = inventory.getItemByObjectId(_items[i]);
        if ((item == null) || (item.getCount() < _itemQ[i]) || (!item.getTemplate().isFreightable()))
        {
          _items[i] = 0;
          _itemQ[i] = 0L;
        }
        else if ((!item.isStackable()) || (freight.getItemByItemId(item.getItemId()) == null))
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
        player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      }
      if (items == 0)
      {
        player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
        return;
      }
      long fee = SafeMath.mulAndCheck(items, 1000L);

      if (fee + adenaDeposit > player.getAdena()) {
        player.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
        return;
      }
      if (!player.reduceAdena(fee, true)) {
        player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }
      for (int i = 0; i < _count; i++)
      {
        if (_items[i] == 0)
          continue;
        ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
        Log.LogItem(player, "FreightDeposit", item);
        freight.addItem(item);
      }

    }
    catch (ArithmeticException ae) {
      player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }
    finally {
      freight.writeUnlock();
      inventory.writeUnlock();
    }

    player.sendChanges();
    player.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
  }
}