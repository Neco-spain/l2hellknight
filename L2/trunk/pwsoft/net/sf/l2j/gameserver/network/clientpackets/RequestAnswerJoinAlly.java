package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestAnswerJoinAlly extends L2GameClientPacket
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
      if (System.currentTimeMillis() - player.gCPAS() < 400L)
        return;
      player.sCPAS();

      L2PcInstance requestor = player.getTransactionRequester();

      player.setTransactionRequester(null);

      if (requestor == null) {
        return;
      }
      requestor.setTransactionRequester(null);

      if ((player.getTransactionType() != L2PcInstance.TransactionType.ALLY) || (player.getTransactionType() != requestor.getTransactionType())) {
        return;
      }
      if (_response == 1)
      {
        L2Clan clan = requestor.getClan();

        if (clan.checkAllyJoinCondition(requestor, player))
        {
          player.sendPacket(Static.FAILED_TO_INVITE_CLAN_IN_ALLIANCE);

          player.getClan().setAllyId(clan.getAllyId());
          player.getClan().setAllyName(clan.getAllyName());
          player.getClan().setAllyPenaltyExpiryTime(0L, 0);
          player.getClan().updateClanInDB();
        }
      }
      else {
        requestor.sendPacket(Static.FAILED_TO_INVITE_CLAN_IN_ALLIANCE);
      }
      requestor.setTransactionType(L2PcInstance.TransactionType.NONE);
      player.setTransactionType(L2PcInstance.TransactionType.NONE);
    }
  }
}