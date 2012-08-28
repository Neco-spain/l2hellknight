package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class AnswerJoinPartyRoom extends L2GameClientPacket
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
        player.sendPacket(Static.TARGET_IS_NOT_FOUND_IN_THE_GAME);
      }
      player.setTransactionRequester(null);
      player.setTransactionType(L2PcInstance.TransactionType.NONE);
      return;
    }

    PartyWaitingRoomManager.WaitingRoom rRoom = partner.getPartyRoom();
    if (rRoom == null) {
      return;
    }
    if (!rRoom.owner.equals(partner)) {
      return;
    }
    if ((player.getTransactionType() != L2PcInstance.TransactionType.ROOM) || (player.getTransactionType() != partner.getTransactionType())) {
      return;
    }
    if (_response == 1)
      PartyWaitingRoomManager.getInstance().joinRoom(player, rRoom.id);
    else {
      partner.sendPacket(Static.PLAYER_DECLINED);
    }
    partner.setTransactionRequester(null);
    player.setTransactionRequester(null);
    partner.setTransactionType(L2PcInstance.TransactionType.NONE);
    player.setTransactionType(L2PcInstance.TransactionType.NONE);
  }
}