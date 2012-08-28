package l2p.gameserver.clientpackets;

import java.util.List;
import l2p.commons.math.SafeMath;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Request;
import l2p.gameserver.model.Request.L2RequestType;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SendTradeDone;
import l2p.gameserver.serverpackets.TradeOtherAdd;
import l2p.gameserver.serverpackets.TradeOwnAdd;
import l2p.gameserver.serverpackets.TradeUpdate;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class AddTradeItem extends L2GameClientPacket
{
  private int _tradeId;
  private int _objectId;
  private long _amount;

  protected void readImpl()
  {
    _tradeId = readD();
    _objectId = readD();
    _amount = readQ();
  }

  protected void runImpl()
  {
    Player parthner1 = ((GameClient)getClient()).getActiveChar();
    if ((parthner1 == null) || (_amount < 1L)) {
      return;
    }
    Request request = parthner1.getRequest();
    if ((request == null) || (!request.isTypeOf(Request.L2RequestType.TRADE)))
    {
      parthner1.sendActionFailed();
      return;
    }

    if (!request.isInProgress())
    {
      request.cancel();
      parthner1.sendPacket(SendTradeDone.FAIL);
      parthner1.sendActionFailed();
      return;
    }

    if (parthner1.isOutOfControl())
    {
      request.cancel();
      parthner1.sendPacket(SendTradeDone.FAIL);
      parthner1.sendActionFailed();
      return;
    }

    Player parthner2 = request.getOtherPlayer(parthner1);
    if (parthner2 == null)
    {
      request.cancel();
      parthner1.sendPacket(SendTradeDone.FAIL);
      parthner1.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
      parthner1.sendActionFailed();
      return;
    }

    if (parthner2.getRequest() != request)
    {
      request.cancel();
      parthner1.sendPacket(SendTradeDone.FAIL);
      parthner1.sendActionFailed();
      return;
    }

    if ((request.isConfirmed(parthner1)) || (request.isConfirmed(parthner2)))
    {
      parthner1.sendPacket(SystemMsg.YOU_MAY_NO_LONGER_ADJUST_ITEMS_IN_THE_TRADE_BECAUSE_THE_TRADE_HAS_BEEN_CONFIRMED);
      parthner1.sendActionFailed();
      return;
    }

    ItemInstance item = parthner1.getInventory().getItemByObjectId(_objectId);
    if ((item == null) || (!item.canBeTraded(parthner1)))
    {
      parthner1.sendPacket(SystemMsg.THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD);
      return;
    }

    long count = Math.min(_amount, item.getCount());

    List tradeList = parthner1.getTradeList();
    TradeItem tradeItem = null;
    try
    {
      for (TradeItem ti : parthner1.getTradeList())
        if (ti.getObjectId() == _objectId)
        {
          count = SafeMath.addAndCheck(count, ti.getCount());
          count = Math.min(count, item.getCount());
          ti.setCount(count);
          tradeItem = ti;
          break;
        }
    }
    catch (ArithmeticException ae)
    {
      parthner1.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
      return;
    }

    if (tradeItem == null)
    {
      tradeItem = new TradeItem(item);
      tradeItem.setCount(count);
      tradeList.add(tradeItem);
    }

    parthner1.sendPacket(new IStaticPacket[] { new TradeOwnAdd(tradeItem, tradeItem.getCount()), new TradeUpdate(tradeItem, item.getCount() - tradeItem.getCount()) });
    parthner2.sendPacket(new TradeOtherAdd(tradeItem, tradeItem.getCount()));
  }
}