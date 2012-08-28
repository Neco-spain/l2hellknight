package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.network.GameClient;

public class RequestWithdrawPartyRoom extends L2GameClientPacket
{
  private int _roomId;

  protected void readImpl()
  {
    _roomId = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    MatchingRoom room = player.getMatchingRoom();
    if ((room.getId() != _roomId) || (room.getType() != MatchingRoom.PARTY_MATCHING)) {
      return;
    }
    if (room.getLeader() == player) {
      return;
    }
    room.removeMember(player, false);
  }
}