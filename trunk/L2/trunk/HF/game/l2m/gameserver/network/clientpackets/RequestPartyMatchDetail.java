package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.MatchingRoomManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.matching.MatchingRoom;
import l2m.gameserver.network.GameClient;

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