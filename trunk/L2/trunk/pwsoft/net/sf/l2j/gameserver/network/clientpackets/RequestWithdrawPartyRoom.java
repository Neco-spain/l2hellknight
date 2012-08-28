package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager;
import net.sf.l2j.gameserver.instancemanager.PartyWaitingRoomManager.WaitingRoom;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestWithdrawPartyRoom extends L2GameClientPacket
{
  private int _roomId;
  private int _data2;

  protected void readImpl()
  {
    _roomId = readD();
    _data2 = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    PartyWaitingRoomManager.WaitingRoom room = player.getPartyRoom();
    if ((room != null) && (room.id == _roomId)) {
      PartyWaitingRoomManager.getInstance().exitRoom(player, room);
    }
    player.setLFP(false);
    player.broadcastUserInfo();
  }
}