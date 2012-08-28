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

public class RequestPrivateStoreBuy extends L2GameClientPacket
{
  private int _sellerId;
  private int _count;
  private int[] _items;
  private long[] _itemQ;
  private long[] _itemP;

  protected void readImpl()
  {
    _sellerId = readD();
    _count = readD();
    if ((_count * 20 > _buf.remaining()) || (_count > 32767) || (_count < 1))
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
    Player buyer = ((GameClient)getClient()).getActiveChar();
    if ((buyer == null) || (_count == 0)) {
      return;
    }
    if (buyer.isActionsDisabled())
    {
      buyer.sendActionFailed();
      return;
    }

    if (buyer.isInStoreMode())
    {
      buyer.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (buyer.isInTrade())
    {
      buyer.sendActionFailed();
      return;
    }

    if (buyer.isFishing())
    {
      buyer.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
      return;
    }

    if (!buyer.getPlayerAccess().UseTrade)
    {
      buyer.sendPacket(Msg.THIS_ACCOUNT_CANOT_USE_PRIVATE_STORES);
      return;
    }

    Player seller = (Player)buyer.getVisibleObject(_sellerId);
    if ((seller == null) || ((seller.getPrivateStoreType() != 1) && (seller.getPrivateStoreType() != 8)) || (!seller.isInRangeZ(buyer, 200L)))
    {
      buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
      buyer.sendActionFailed();
      return;
    }

    List sellList = seller.getSellList();
    if (sellList.isEmpty())
    {
      buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
      buyer.sendActionFailed();
      return;
    }

    List buyList = new ArrayList();

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
      TradeItem bi;
      for (int i = 0; i < _count; i++)
      {
        objectId = _items[i];
        count = _itemQ[i];
        price = _itemP[i];

        bi = null;

        for (TradeItem si : sellList)
          if ((si.getObjectId() == objectId) && 
            (si.getOwnersPrice() == price))
          {
            if (count > si.getCount())
              break label478;
            ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
            if ((item == null) || (item.getCount() < count) || (!item.canBeTraded(seller)))
              break label478;
            totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
            weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
            if ((!item.isStackable()) || (buyer.getInventory().getItemByItemId(item.getItemId()) == null)) {
              slots++;
            }
            bi = new TradeItem();
            bi.setObjectId(objectId);
            bi.setItemId(item.getItemId());
            bi.setCount(count);
            bi.setOwnersPrice(price);

            buyList.add(bi);
            break; }   }  } catch (ArithmeticException ae) { label478: Iterator i$;
      TradeItem bi;
      ItemInstance item;
      Iterator i$;
      TradeItem si;
      long tax;
      buyList.clear();
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      Iterator i$;
      TradeItem bi;
      ItemInstance item;
      Iterator i$;
      TradeItem si;
      long tax;
      return; } finally { try { if ((buyList.size() != _count) || ((seller.getPrivateStoreType() == 8) && (buyList.size() != sellList.size()))) {
          buyer.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED);
          buyer.sendActionFailed();
          return;
        }if (!buyer.getInventory().validateWeight(weight)) {
          buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
          buyer.sendActionFailed();
          return;
        }if (!buyer.getInventory().validateCapacity(slots)) {
          buyer.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
          buyer.sendActionFailed();
          return;
        }if (!buyer.reduceAdena(totalCost)) {
          buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
          buyer.sendActionFailed();
          return;
        }
        for (TradeItem bi : buyList)
        {
          ItemInstance item = seller.getInventory().removeItemByObjectId(bi.getObjectId(), bi.getCount());
          for (TradeItem si : sellList) {
            if (si.getObjectId() == bi.getObjectId())
            {
              si.setCount(si.getCount() - bi.getCount());
              if (si.getCount() >= 1L) break;
              sellList.remove(si); break;
            }
          }
          Log.LogItem(seller, "PrivateStoreSell", item);
          Log.LogItem(buyer, "PrivateStoreBuy", item);
          buyer.getInventory().addItem(item);
          TradeHelper.purchaseItem(buyer, seller, bi);
        }

        long tax = TradeHelper.getTax(seller, totalCost);
        if (tax > 0L)
        {
          totalCost -= tax;
          seller.sendMessage(new CustomMessage("trade.HavePaidTax", seller, new Object[0]).addNumber(tax));
        }

        seller.addAdena(totalCost);
        seller.saveTradeList();
      }
      finally
      {
        seller.getInventory().writeUnlock();
        buyer.getInventory().writeUnlock();
      }
    }

    if (sellList.isEmpty()) {
      TradeHelper.cancelStore(seller);
    }
    seller.sendChanges();
    buyer.sendChanges();

    buyer.sendActionFailed();
  }
}