package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.commons.math.SafeMath;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.BuyListHolder;
import l2p.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExBuySellList.SellRefundList;
import l2p.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBuyItem extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(RequestBuyItem.class);
  private int _listId;
  private int _count;
  private int[] _items;
  private long[] _itemQ;

  protected void readImpl()
  {
    _listId = readD();
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
      if (_itemQ[i] >= 1L)
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

    if (activeChar.getBuyListId() != _listId)
    {
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
    if ((!activeChar.isGM()) && ((merchant == null) || (!isValidMerchant) || (!activeChar.isInRange(merchant, 200L))))
    {
      activeChar.sendActionFailed();
      return;
    }

    BuyListHolder.NpcTradeList list = BuyListHolder.getInstance().getBuyList(_listId);
    if (list == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    int slots = 0;
    long weight = 0L;
    long totalPrice = 0L;
    long tax = 0L;
    double taxRate = 0.0D;

    Castle castle = null;
    if (merchant != null)
    {
      castle = merchant.getCastle(activeChar);
      if (castle != null) {
        taxRate = castle.getTaxRate();
      }
    }
    List buyList = new ArrayList(_count);
    List tradeList = list.getItems();
    try
    {
      for (int i = 0; i < _count; i++)
      {
        int itemId = _items[i];
        long count = _itemQ[i];
        long price = 0L;

        Iterator i$ = tradeList.iterator();
        while (true) if (i$.hasNext()) { TradeItem ti = (TradeItem)i$.next();
            if (ti.getItemId() == itemId)
            {
              if ((ti.isCountLimited()) && (ti.getCurrentValue() < count))
                break;
              price = ti.getOwnersPrice(); } else { continue; }
          } else
          {
            if ((price == 0L) && ((!activeChar.isGM()) || (!activeChar.getPlayerAccess().UseGMShop)))
            {
              activeChar.sendActionFailed();
              return;
            }

            totalPrice = SafeMath.addAndCheck(totalPrice, SafeMath.mulAndCheck(count, price));

            TradeItem ti = new TradeItem();
            ti.setItemId(itemId);
            ti.setCount(count);
            ti.setOwnersPrice(price);

            weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, ti.getItem().getWeight()));
            if ((!ti.getItem().isStackable()) || (activeChar.getInventory().getItemByItemId(itemId) == null)) {
              slots++;
            }
            buyList.add(ti);
          }
      }
      tax = ()(totalPrice * taxRate);

      totalPrice = SafeMath.addAndCheck(totalPrice, tax);

      if (!activeChar.getInventory().validateWeight(weight))
      {
        sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
        return;
      }

      if (!activeChar.getInventory().validateCapacity(slots))
      {
        sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
        return;
      }

      if (!activeChar.reduceAdena(totalPrice))
      {
        activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }

      for (TradeItem ti : buyList) {
        activeChar.getInventory().addItem(ti.getItemId(), ti.getCount());
      }

      list.updateItems(buyList);

      if ((castle != null) && 
        (tax > 0L) && (castle.getOwnerId() > 0) && (activeChar.getReflection() == ReflectionManager.DEFAULT)) {
        castle.addToTreasury(tax, true, false);
      }
    }
    catch (ArithmeticException ae)
    {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    sendPacket(new ExBuySellList.SellRefundList(activeChar, true));
    activeChar.sendChanges();
  }
}