package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.JoinParty;

public final class RequestAnswerJoinParty extends L2GameClientPacket
{
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player != null) {
      if (System.currentTimeMillis() - player.gCPS() < 100L) {
        return;
      }
      player.sCPS();

      L2PcInstance requestor = player.getTransactionRequester();

      player.setTransactionRequester(null);

      if (requestor == null) {
        return;
      }

      requestor.setTransactionRequester(null);

      if (requestor.getParty() == null) {
        return;
      }

      if ((player.isInOlympiadMode()) || (requestor.isInOlympiadMode())) {
        return;
      }

      if ((player.getChannel() == 6) || (requestor.getChannel() == 6)) {
        return;
      }

      if ((player.getTransactionType() != L2PcInstance.TransactionType.PARTY) || (player.getTransactionType() != requestor.getTransactionType())) {
        return;
      }

      requestor.sendPacket(new JoinParty(_response));
      if (_response == 1) {
        if (requestor.getParty().getMemberCount() >= 9) {
          player.sendPacket(Static.PARTY_FULL);
          requestor.sendPacket(Static.PARTY_FULL);
          return;
        }

        player.joinParty(requestor.getParty());
      } else {
        requestor.sendPacket(Static.PLAYER_DECLINED);

        if ((requestor.getParty() != null) && (requestor.getParty().getMemberCount() == 1)) {
          requestor.setParty(null);
        }
      }

      requestor.setTransactionType(L2PcInstance.TransactionType.NONE);
      player.setTransactionType(L2PcInstance.TransactionType.NONE);
    }
  }
}