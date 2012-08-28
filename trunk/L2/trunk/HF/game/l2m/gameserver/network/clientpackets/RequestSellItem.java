package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2p.commons.math.SafeMath;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.items.ItemContainer;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExBuySellList.SellRefundList;
import l2m.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class RequestSellItem extends L2GameClientPacket
{
  private int _listId;
  private int _count;
  private int[] _items;
  private long[] _itemQ;

  protected void readImpl()
  {
    _listId = readD();
    _count = readD();
    if ((_count * 16 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new int[_count];
    _itemQ = new long[_count];

    for (int i = 0; i < _count; i++)
    {
      _items[i] = readD();
      readD();
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

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
      return;
    }

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP) && (activeChar.getKarma() > 0) && (!activeChar.isGM()))
    {
      activeChar.sendActionFailed();
      return;
    }

    NpcInstance merchant = activeChar.getLastNpc();
    boolean isValidMerchant = (merchant != null) && (merchant.isMerchantNpc());
    if ((!Config.COMMUNITYBOARD_SELL_ENABLED) && (!activeChar.isGM()) && ((merchant == null) || (!isValidMerchant) || (!activeChar.isInRange(merchant, 200L))))
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.getInventory().writeLock();
    try
    {
      for (int i = 0; i < _count; i++)
      {
        int objectId = _items[i];
        long count = _itemQ[i];

        ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
        if ((item == null) || (item.getCount() < count) || (!item.canBeSold(activeChar))) {
          continue;
        }
        long price = SafeMath.mulAndCheck(item.getReferencePrice(), count) / 2L;

        ItemInstance refund = activeChar.getInventory().removeItemByObjectId(objectId, count);

        Log.LogItem(activeChar, "RefundSell", refund);

        activeChar.addAdena(price);
        activeChar.getRefund().addItem(refund);
      }
    }
    catch (ArithmeticException ae) {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }
    finally {
      activeChar.getInventory().writeUnlock();
    }

    activeChar.sendPacket(new ExBuySellList.SellRefundList(activeChar, true));
    activeChar.sendChanges();
  }
}