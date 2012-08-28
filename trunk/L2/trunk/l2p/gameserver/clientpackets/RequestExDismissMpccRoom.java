package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.network.GameClient;

public class RequestExDismissMpccRoom extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    MatchingRoom room = player.getMatchingRoom();
    if ((room == null) || (room.getType() != MatchingRoom.CC_MATCHING)) {
      return;
    }
    if (room.getLeader() != player) {
      return;
    }
    room.disband();
  }
}