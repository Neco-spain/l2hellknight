package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class TradeDone extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(TradeDone.class.getName());
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPO() < 500L) {
      return;
    }
    player.sCPO();

    L2PcInstance requestor = player.getTransactionRequester();
    if (requestor == null)
    {
      clearTrade(player, null);
      return;
    }

    if ((requestor == player) || (player == requestor))
    {
      clearTrade(player, requestor);
      return;
    }

    if ((player.getTradePartner() != requestor.getObjectId()) || (player.getTradeStart() != requestor.getTradeStart()))
    {
      clearTrade(player, requestor);
      return;
    }

    TradeList trade = player.getActiveTradeList();
    if ((trade == null) || ((trade.getItemCount() == 0) && (requestor.getActiveTradeList() != null) && (requestor.getActiveTradeList().getItemCount() == 0)))
    {
      clearTrade(player, requestor);

      return;
    }

    if (trade.isLocked()) {
      return;
    }

    if (_response == 1)
    {
      if ((player.getTransactionRequester() == null) || (trade.getPartner() == null) || (L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null))
      {
        player.cancelActiveTrade();
        player.setTransactionRequester(null);
        player.setTransactionType(L2PcInstance.TransactionType.NONE);
        player.setTradePartner(-1, 0L);
        player.sendPacket(Static.TARGET_IS_NOT_FOUND_IN_THE_GAME);
        return;
      }

      if (trade.isConfirmed()) {
        requestor.onTradeConfirm(player);
      }
      trade.confirm();
      return;
    }

    player.cancelActiveTrade();
    player.setTransactionRequester(null);
    requestor.setTransactionRequester(null);

    player.setTradePartner(-1, 0L);
    requestor.setTradePartner(-1, 0L);

    requestor.setTransactionType(L2PcInstance.TransactionType.NONE);
    player.setTransactionType(L2PcInstance.TransactionType.NONE);
  }

  private void clearTrade(L2PcInstance player, L2PcInstance partner)
  {
    player.cancelActiveTrade();
    player.setTransactionRequester(null);
    player.setTransactionType(L2PcInstance.TransactionType.NONE);
    player.setTradePartner(-1, 0L);
    player.sendActionFailed();

    if (partner != null)
    {
      partner.cancelActiveTrade();
      partner.setTransactionRequester(null);
      partner.setTransactionType(L2PcInstance.TransactionType.NONE);
      partner.setTradePartner(-1, 0L);
      partner.sendActionFailed();
    }
  }

  public String getType()
  {
    return "[C] TradeDone";
  }
}