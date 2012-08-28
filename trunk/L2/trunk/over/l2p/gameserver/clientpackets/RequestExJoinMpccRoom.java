package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.MatchingRoomManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.network.GameClient;

public class RequestExJoinMpccRoom extends L2GameClientPacket
{
  private int _roomId;

  protected void readImpl()
    throws Exception
  {
    _roomId = readD();
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.getMatchingRoom() != null) {
      return;
    }
    MatchingRoom room = MatchingRoomManager.getInstance().getMatchingRoom(MatchingRoom.CC_MATCHING, _roomId);
    if (room == null) {
      return;
    }
    room.addMember(player);
  }
}