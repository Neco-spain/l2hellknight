package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import l2p.gameserver.instancemanager.MatchingRoomManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.matching.MatchingRoom;

public class ListPartyWaiting extends L2GameServerPacket
{
  private Collection<MatchingRoom> _rooms;
  private int _fullSize;

  public ListPartyWaiting(int region, boolean allLevels, int page, Player activeChar)
  {
    int first = (page - 1) * 64;
    int firstNot = page * 64;
    _rooms = new ArrayList();

    int i = 0;
    List temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, allLevels, activeChar);
    _fullSize = temp.size();
    for (MatchingRoom room : temp)
    {
      if ((i < first) || (i >= firstNot))
        continue;
      _rooms.add(room);
      i++;
    }
  }

  protected final void writeImpl()
  {
    writeC(156);
    writeD(_fullSize);
    writeD(_rooms.size());

    for (MatchingRoom room : _rooms)
    {
      writeD(room.getId());
      writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
      writeD(room.getLocationId());
      writeD(room.getMinLevel());
      writeD(room.getMaxLevel());
      writeD(room.getMaxMembersSize());
      writeS(room.getTopic());

      Collection players = room.getPlayers();
      writeD(players.size());
      for (Player player : players)
      {
        writeD(player.getClassId().getId());
        writeS(player.getName());
      }
    }
  }
}