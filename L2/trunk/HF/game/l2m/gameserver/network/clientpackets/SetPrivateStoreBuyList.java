package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2p.commons.math.SafeMath;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.TradeItem;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import l2m.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.TradeHelper;

public class SetPrivateStoreBuyList extends L2GameClientPacket
{
  private int _count;
  private int[] _items;
  private long[] _itemQ;
  private long[] _itemP;

  protected void readImpl()
  {
    _count = readD();
    if ((_count * 40 > _buf.remaining()) || (_count > 32767) || (_count < 1))
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

      readH();
      readH();

      _itemQ[i] = readQ();
      _itemP[i] = readQ();

      if ((_itemQ[i] < 1L) || (_itemP[i] < 1L))
      {
        _count = 0;
        break;
      }

      readC();
      readD();
      readD();

      readC();
      readC();
      readC();
      readC();
      readC();
      readC();
      readC();
    }
  }

  protected void runImpl()
  {
    Player buyer = ((GameClient)getClient()).getActiveChar();
    if ((buyer == null) || (_count == 0)) {
      return;
    }
    if (!TradeHelper.checksIfCanOpenStore(buyer, 3))
    {
      buyer.sendActionFailed();
      return;
    }

    List buyList = new CopyOnWriteArrayList();
    long totalCost = 0L;
    try
    {
      label298: for (int i = 0; i < _count; i++)
      {
        int itemId = _items[i];
        long count = _itemQ[i];
        long price = _itemP[i];

        ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);

        if ((item == null) || (itemId == 57)) {
          continue;
        }
        if (item.getReferencePrice() / 2 > price)
        {
          buyer.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.SetPrivateStoreBuyList.TooLowPrice", buyer, new Object[0]).addItemName(item).addNumber(item.getReferencePrice() / 2));
        }
        else
        {
          if (item.isStackable()) {
            for (TradeItem bi : buyList)
              if (bi.getItemId() == itemId)
              {
                bi.setOwnersPrice(price);
                bi.setCount(bi.getCount() + count);
                totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
                break label298;
              }
          }
          TradeItem bi = new TradeItem();
          bi.setItemId(itemId);
          bi.setCount(count);
          bi.setOwnersPrice(price);
          totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
          buyList.add(bi);
        }
      }
    }
    catch (ArithmeticException ae)
    {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    if (buyList.size() > buyer.getTradeLimit())
    {
      buyer.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
      return;
    }

    if (totalCost > buyer.getAdena())
    {
      buyer.sendPacket(Msg.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
      buyer.sendPacket(new PrivateStoreManageListBuy(buyer));
      return;
    }

    if (!buyList.isEmpty())
    {
      buyer.setBuyList(buyList);
      buyer.saveTradeList();
      buyer.setPrivateStoreType(3);
      buyer.broadcastPacket(new L2GameServerPacket[] { new PrivateStoreMsgBuy(buyer) });
      buyer.sitDown(null);
      buyer.broadcastCharInfo();
    }

    buyer.sendActionFailed();
  }
}