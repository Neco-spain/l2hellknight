package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class RequestExManageMpccRoom extends L2GameClientPacket
{
  private int _id;
  private int _memberSize;
  private int _minLevel;
  private int _maxLevel;
  private String _topic;

  protected void readImpl()
  {
    _id = readD();
    _memberSize = readD();
    _minLevel = readD();
    _maxLevel = readD();
    readD();
    _topic = readS();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    MatchingRoom room = player.getMatchingRoom();
    if ((room == null) || (room.getId() != _id) || (room.getType() != MatchingRoom.CC_MATCHING)) {
      return;
    }
    if (room.getLeader() != player) {
      return;
    }
    room.setTopic(_topic);
    room.setMaxMemberSize(_memberSize);
    room.setMinLevel(_minLevel);
    room.setMaxLevel(_maxLevel);
    room.broadCast(new IStaticPacket[] { room.infoRoomPacket() });

    player.sendPacket(SystemMsg.THE_COMMAND_CHANNEL_MATCHING_ROOM_INFORMATION_WAS_EDITED);
  }
}