package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import l2m.gameserver.instancemanager.MatchingRoomManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.matching.MatchingRoom;

public class ExListMpccWaiting extends L2GameServerPacket
{
  private static final int PAGE_SIZE = 10;
  private int _fullSize;
  private List<MatchingRoom> _list;

  public ExListMpccWaiting(Player player, int page, int location, boolean allLevels)
  {
    int first = (page - 1) * 10;
    int firstNot = page * 10;
    int i = 0;
    Collection all = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.CC_MATCHING, location, allLevels, player);
    _fullSize = all.size();
    _list = new ArrayList(10);
    for (MatchingRoom c : all)
    {
      if ((i < first) || (i >= firstNot)) {
        continue;
      }
      _list.add(c);
      i++;
    }
  }

  public void writeImpl()
  {
    writeEx(156);
    writeD(_fullSize);
    writeD(_list.size());
    for (MatchingRoom room : _list)
    {
      writeD(room.getId());
      writeS(room.getTopic());
      writeD(room.getPlayers().size());
      writeD(room.getMinLevel());
      writeD(room.getMaxLevel());
      writeD(1);
      writeD(room.getMaxMembersSize());
      Player leader = room.getLeader();
      writeS(leader == null ? "" : leader.getName());
    }
  }
}