package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.MatchingRoomManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.network.GameClient;

public class RequestPartyMatchDetail extends L2GameClientPacket
{
  private int _roomId;
  private int _locations;
  private int _level;

  protected void readImpl()
  {
    _roomId = readD();
    _locations = readD();
    _level = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.getMatchingRoom() != null) {
      return;
    }
    if (_roomId > 0)
    {
      MatchingRoom room = MatchingRoomManager.getInstance().getMatchingRoom(MatchingRoom.PARTY_MATCHING, _roomId);
      if (room == null) {
        return;
      }
      room.addMember(player);
    }
    else
    {
      for (MatchingRoom room : MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, _locations, _level == 1, player))
        if (room.addMember(player))
          break;
    }
  }
}