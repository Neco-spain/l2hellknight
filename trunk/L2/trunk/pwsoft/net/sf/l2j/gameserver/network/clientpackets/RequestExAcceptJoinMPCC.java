package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player != null)
    {
      L2PcInstance requestor = player.getTransactionRequester();

      player.setTransactionRequester(null);

      if (requestor == null) {
        return;
      }
      requestor.setTransactionRequester(null);

      if ((player.getTransactionType() != L2PcInstance.TransactionType.CHANNEL) || (player.getTransactionType() != requestor.getTransactionType())) {
        return;
      }
      if ((!requestor.isInParty()) || (!player.isInParty()) || ((requestor.getParty().isInCommandChannel()) && (!requestor.getParty().getCommandChannel().getChannelLeader().equals(requestor))))
      {
        requestor.sendMessage("\u041D\u0438\u043A\u0430\u043A\u043E\u0439 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044C \u043D\u0435 \u0431\u044B\u043B \u043F\u0440\u0438\u0433\u043B\u0430\u0448\u0435\u043D \u0432 \u043A\u0430\u043D\u0430\u043B \u043A\u043E\u043C\u0430\u043D\u0434\u044B");
        player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u0440\u0438\u0441\u043E\u0435\u0434\u0438\u043D\u0438\u0442\u044C\u0441\u044F \u043A \u041A\u043E\u043C\u0430\u043D\u0434\u043D\u043E\u043C\u0443 \u041A\u0430\u043D\u0430\u043B\u0443");
        return;
      }

      if (_response == 1)
      {
        if (player.isTeleporting())
        {
          player.sendMessage("\u0412\u044B \u043D\u0435 \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u0440\u0438\u0441\u043E\u0435\u0434\u0438\u043D\u0438\u0442\u044C\u0441\u044F \u043A \u041A\u043E\u043C\u0430\u043D\u0434\u043D\u043E\u043C\u0443 \u041A\u0430\u043D\u0430\u043B\u0443 \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u0443\u044F\u0441\u044C");
          requestor.sendMessage("\u041D\u0438\u043A\u0430\u043A\u043E\u0439 \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044C \u043D\u0435 \u0431\u044B\u043B \u043F\u0440\u0438\u0433\u043B\u0430\u0448\u0435\u043D \u0432 \u043A\u0430\u043D\u0430\u043B \u043A\u043E\u043C\u0430\u043D\u0434\u044B");
          return;
        }

        if (!requestor.getParty().isInCommandChannel())
        {
          new L2CommandChannel(requestor);
        }
        requestor.getParty().getCommandChannel().addParty(player.getParty());
      }
      else {
        requestor.sendMessage(player.getName() + " \u043E\u0442\u043A\u043B\u043E\u043D\u0438\u043B \u043F\u0440\u0438\u0433\u043B\u0430\u0448\u0435\u043D\u0438\u0435 \u0432 \u043A\u0430\u043D\u0430\u043B.");
      }
    }
  }
}