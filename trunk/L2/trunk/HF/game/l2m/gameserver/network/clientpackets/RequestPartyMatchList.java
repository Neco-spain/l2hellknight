package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.matching.MatchingRoom;
import l2m.gameserver.model.matching.PartyMatchingRoom;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;

public class RequestPartyMatchList extends L2GameClientPacket
{
  private int _lootDist;
  private int _maxMembers;
  private int _minLevel;
  private int _maxLevel;
  private int _roomId;
  private String _roomTitle;

  protected void readImpl()
  {
    _roomId = readD();
    _maxMembers = readD();
    _minLevel = readD();
    _maxLevel = readD();
    _lootDist = readD();
    _roomTitle = readS(64);
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    MatchingRoom room = player.getMatchingRoom();
    if (room == null) {
      new PartyMatchingRoom(player, _minLevel, _maxLevel, _maxMembers, _lootDist, _roomTitle);
    } else if ((room.getId() == _roomId) && (room.getType() == MatchingRoom.PARTY_MATCHING) && (room.getLeader() == player))
    {
      room.setMinLevel(_minLevel);
      room.setMaxLevel(_maxLevel);
      room.setMaxMemberSize(_maxMembers);
      room.setTopic(_roomTitle);
      room.setLootType(_lootDist);
      room.broadCast(new IStaticPacket[] { room.infoRoomPacket() });
    }
  }
}