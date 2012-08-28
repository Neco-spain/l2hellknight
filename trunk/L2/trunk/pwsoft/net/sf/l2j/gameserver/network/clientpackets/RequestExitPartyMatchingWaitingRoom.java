package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    PartyWaitingRoomManager.WaitingRoom room = player.getPartyRoom();
    if (room != null) {
      PartyWaitingRoomManager.getInstance().exitRoom(player, room);
    }
    PartyWaitingRoomManager.getInstance().delPlayer(player);
    player.setLFP(false);
    player.broadcastUserInfo();
  }
}