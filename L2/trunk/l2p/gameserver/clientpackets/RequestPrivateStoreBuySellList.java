package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.commons.math.SafeMath;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.TradeHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPrivateStoreBuySellList extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(RequestPrivateStoreBuySellList.class);
  private int _buyerId;
  private int _count;
  private int[] _items;
  private long[] _itemQ;
  private long[] _itemP;

  protected void readImpl()
  {
    _buyerId = readD();
    _count = readD();

    if ((_count * 28 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }

    _items = new int[_count];
    _itemQ = new long[_count];
    _itemP = new long[_count];

    for (int i = 0; i < _count; i++)
    {
      _items[i] = readD();
      readD();
      readH();
      readH();
      _itemQ[i] = readQ();
      _itemP[i] = readQ();

      if ((_itemQ[i] >= 1L) && (_itemP[i] >= 1L) && (ArrayUtils.indexOf(_items, _items[i]) >= i))
        continue;
      _count = 0;
      break;
    }
  }

  protected void runImpl()
  {
    Player seller = ((GameClient)getClient()).getActiveChar();
    if ((seller == null) || (_count == 0)) {
      return;
    }
    if (seller.isActionsDisabled())
    {
      seller.sendActionFailed();
      return;
    }

    if (seller.isInStoreMode())
    {
      seller.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (seller.isInTrade())
    {
      seller.sendActionFailed();
      return;
    }

    if (seller.isFishing())
    {
      seller.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
      return;
    }

    if (!seller.getPlayerAccess().UseTrade)
    {
      seller.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
      return;
    }

    Player buyer = (Player)seller.getVisibleObject(_buyerId);
    if ((buyer == null) || (buyer.getPrivateStoreType() != 3) || (!seller.isInRangeZ(buyer, 200L)))
    {
      seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
      seller.sendActionFailed();
      return;
    }

    List buyList = buyer.getBuyList();
    if (buyList.isEmpty())
    {
      seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
      seller.sendActionFailed();
      return;
    }

    List sellList = new ArrayList();

    long totalCost = 0L;
    int slots = 0;
    long weight = 0L;

    buyer.getInventory().writeLock();
    seller.getInventory().writeLock();
    try
    {
      int objectId;
      long count;
      long price;
      ItemInstance item;
      TradeItem si;
      for (int i = 0; i < _count; i++)
      {
        objectId = _items[i];
        count = _itemQ[i];
        price = _itemP[i];

        item = seller.getInventory().getItemByObjectId(objectId);
        if ((item == null) || (item.getCount() < count) || (!item.canBeTraded(seller))) {
          break;
        }
        si = null;

        for (TradeItem bi : buyList)
          if ((bi.getItemId() == item.getItemId()) && 
            (bi.getOwnersPrice() == price))
          {
            if (count > bi.getCount())
              break label472;
            totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
            weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
            if ((!item.isStackable()) || (buyer.getInventory().getItemByItemId(item.getItemId()) == null)) {
              slots++;
            }
            si = new TradeItem();
            si.setObjectId(objectId);
            si.setItemId(item.getItemId());
            si.setCount(count);
            si.setOwnersPrice(price);

            sellList.add(si);
            break; }   }  } catch (ArithmeticException ae) { label472: Iterator i$;
      TradeItem si;
      ItemInstance item;
      Iterator i$;
      TradeItem bi;
      long tax;
      sellList.clear();
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      Iterator i$;
      TradeItem si;
      ItemInstance item;
      Iterator i$;
      TradeItem bi;
      long tax;
      return; } finally { try { if (sellList.size() != _count) {
          seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
          seller.sendActionFailed();
          return;
        }if (!buyer.getInventory().validateWeight(weight)) { buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
          seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
          seller.sendActionFailed();
          return; }
        if (!buyer.getInventory().validateCapacity(slots)) { buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
          seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
          seller.sendActionFailed();
          return; }
        if (!buyer.reduceAdena(totalCost)) {
          buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
          seller.sendPacket(Msg.THE_ATTEMPT_TO_SELL_HAS_FAILED);
          seller.sendActionFailed();
          return;
        }for (TradeItem si : sellList)
        {
          ItemInstance item = seller.getInventory().removeItemByObjectId(si.getObjectId(), si.getCount());
          for (TradeItem bi : buyList) {
            if ((bi.getItemId() == si.getItemId()) && 
              (bi.getOwnersPrice() == si.getOwnersPrice()))
            {
              bi.setCount(bi.getCount() - si.getCount());
              if (bi.getCount() >= 1L) break;
              buyList.remove(bi); break;
            }
          }
          Log.LogItem(seller, "PrivateStoreSell", item);
          Log.LogItem(buyer, "PrivateStoreBuy", item);
          buyer.getInventory().addItem(item);
          TradeHelper.purchaseItem(buyer, seller, si);
        }

        long tax = TradeHelper.getTax(seller, totalCost);
        if (tax > 0L)
        {
          totalCost -= tax;
          seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller, new Object[0]).addNumber(tax));
        }

        seller.addAdena(totalCost);
        buyer.saveTradeList();
      }
      finally
      {
        seller.getInventory().writeUnlock();
        buyer.getInventory().writeUnlock();
      }
    }

    if (buyList.isEmpty()) {
      TradeHelper.cancelStore(buyer);
    }
    seller.sendChanges();
    buyer.sendChanges();

    seller.sendActionFailed();
  }
}