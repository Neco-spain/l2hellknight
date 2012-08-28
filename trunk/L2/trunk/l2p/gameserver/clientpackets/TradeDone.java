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
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.TradePressOtherOk;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.Log;

public class TradeDone extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    Player parthner1 = ((GameClient)getClient()).getActiveChar();
    if (parthner1 == null) {
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

    if (_response == 0)
    {
      request.cancel();
      parthner1.sendPacket(SendTradeDone.FAIL);
      parthner2.sendPacket(new IStaticPacket[] { SendTradeDone.FAIL, new SystemMessage(124).addString(parthner1.getName()) });
      return;
    }

    if (!parthner1.isInRangeZ(parthner2, 200L))
    {
      parthner1.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
      return;
    }

    request.confirm(parthner1);
    parthner2.sendPacket(new IStaticPacket[] { new SystemMessage(121).addString(parthner1.getName()), TradePressOtherOk.STATIC });

    if (!request.isConfirmed(parthner2))
    {
      parthner1.sendActionFailed();
      return;
    }

    List tradeList1 = parthner1.getTradeList();
    List tradeList2 = parthner2.getTradeList();
    int slots = 0;
    long weight = 0L;
    boolean success = false;

    parthner1.getInventory().writeLock();
    parthner2.getInventory().writeLock();
    try
    {
      slots = 0;
      weight = 0L;

      for (TradeItem ti : tradeList1)
      {
        ItemInstance item = parthner1.getInventory().getItemByObjectId(ti.getObjectId());
        if ((item == null) || (item.getCount() < ti.getCount()) || (!item.canBeTraded(parthner1)))
          return;
        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
        if ((!ti.getItem().isStackable()) || (parthner2.getInventory().getItemByItemId(ti.getItemId()) == null)) {
          slots++;
        }
      }
      if (!parthner2.getInventory().validateWeight(weight)) {
        parthner2.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
        return;
      }
      if (!parthner2.getInventory().validateCapacity(slots)) {
        parthner2.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
        return;
      }
      slots = 0;
      weight = 0L;

      for (TradeItem ti : tradeList2)
      {
        ItemInstance item = parthner2.getInventory().getItemByObjectId(ti.getObjectId());
        if ((item == null) || (item.getCount() < ti.getCount()) || (!item.canBeTraded(parthner2)))
          return;
        weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(ti.getCount(), ti.getItem().getWeight()));
        if ((!ti.getItem().isStackable()) || (parthner1.getInventory().getItemByItemId(ti.getItemId()) == null)) {
          slots++;
        }
      }
      if (!parthner1.getInventory().validateWeight(weight)) {
        parthner1.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
        return;
      }
      if (!parthner1.getInventory().validateCapacity(slots)) {
        parthner1.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
        return;
      }
      for (TradeItem ti : tradeList1)
      {
        ItemInstance item = parthner1.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount());
        Log.LogItem(parthner1, "TradeSell", item);
        Log.LogItem(parthner2, "TradeBuy", item);
        parthner2.getInventory().addItem(item);
      }

      for (TradeItem ti : tradeList2)
      {
        ItemInstance item = parthner2.getInventory().removeItemByObjectId(ti.getObjectId(), ti.getCount());
        Log.LogItem(parthner2, "TradeSell", item);
        Log.LogItem(parthner1, "TradeBuy", item);
        parthner1.getInventory().addItem(item);
      }

      parthner1.sendPacket(Msg.YOUR_TRADE_IS_SUCCESSFUL);
      parthner2.sendPacket(Msg.YOUR_TRADE_IS_SUCCESSFUL);

      success = true;
    }
    finally
    {
      parthner2.getInventory().writeUnlock();
      parthner1.getInventory().writeUnlock();

      request.done();

      parthner1.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
      parthner2.sendPacket(success ? SendTradeDone.SUCCESS : SendTradeDone.FAIL);
    }
  }
}