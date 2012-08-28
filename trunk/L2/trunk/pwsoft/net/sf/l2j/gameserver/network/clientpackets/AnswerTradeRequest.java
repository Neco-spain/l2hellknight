package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class AnswerTradeRequest extends L2GameClientPacket
{
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

    L2PcInstance partner = player.getTransactionRequester();

    if ((partner == null) || (partner.getTransactionRequester() == null))
    {
      if (_response != 0) {
        player.cancelActiveTrade();
        player.sendPacket(Static.TARGET_IS_NOT_FOUND_IN_THE_GAME);
      }
      player.setTradePartner(-1, 0L);
      player.setTransactionRequester(null);
      player.setTransactionType(L2PcInstance.TransactionType.NONE);
      return;
    }

    if (!player.isInsideRadius(partner, 320, false, false)) {
      player.sendPacket(Static.TARGET_TOO_FAR);
      player.sendActionFailed();
      return;
    }

    if ((player.getTransactionType() != L2PcInstance.TransactionType.TRADE) || (player.getTransactionType() != partner.getTransactionType())) {
      clearTrade(player, partner);
      return;
    }

    if ((_response != 1) || (player.getPrivateStoreType() != 0)) {
      partner.sendPacket(SystemMessage.id(SystemMessageId.S1_DENIED_TRADE_REQUEST).addString(player.getName()));
      clearTrade(player, partner);
      if (player.getPrivateStoreType() != 0) {
        player.sendPacket(Static.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
      }
      return;
    }

    if (player.getTradePartner() == -1) {
      player.sendPacket(Static.ANSWER_TIMEOUT);
      clearTrade(player, partner);
      return;
    }

    if (partner.getTradePartner() == -1) {
      player.sendPacket(Static.ANSWER_TIMEOUT);
      clearTrade(player, partner);
      return;
    }

    if ((player.tradeLeft()) || (partner.tradeLeft())) {
      player.sendPacket(Static.ANSWER_TIMEOUT);
      clearTrade(player, partner);
      return;
    }

    if ((_response == 1) && (player.getTradeStart() == partner.getTradeStart())) {
      player.startTrade(partner);
      player.setTransactionType(L2PcInstance.TransactionType.TRADED);
      partner.setTransactionType(L2PcInstance.TransactionType.TRADED);
    } else {
      partner.sendPacket(SystemMessage.id(SystemMessageId.S1_DENIED_TRADE_REQUEST).addString(player.getName()));
      clearTrade(player, partner);
    }

    player.setTransactionRequester(partner);
    partner.setTransactionRequester(player);
  }

  private void clearTrade(L2PcInstance player, L2PcInstance partner) {
    player.setTransactionRequester(null);
    partner.setTransactionRequester(null);
    partner.setTransactionType(L2PcInstance.TransactionType.NONE);
    player.setTransactionType(L2PcInstance.TransactionType.NONE);

    player.setTradePartner(-1, 0L);
    partner.setTradePartner(-1, 0L);
    player.sendActionFailed();
  }
}