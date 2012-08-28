package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import l2p.commons.lang.ArrayUtils;
import l2m.gameserver.instancemanager.MatchingRoomManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.entity.Reflection;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
  private List<PartyMatchingWaitingInfo> _waitingList = Collections.emptyList();
  private final int _fullSize;

  public ExListPartyMatchingWaitingRoom(Player searcher, int minLevel, int maxLevel, int page, int[] classes)
  {
    int first = (page - 1) * 64;
    int firstNot = page * 64;
    int i = 0;

    List temp = MatchingRoomManager.getInstance().getWaitingList(minLevel, maxLevel, classes);
    _fullSize = temp.size();

    _waitingList = new ArrayList(_fullSize);
    for (Player pc : temp)
    {
      if ((i < first) || (i >= firstNot))
        continue;
      _waitingList.add(new PartyMatchingWaitingInfo(pc));
      i++;
    }
  }

  protected void writeImpl()
  {
    writeEx(54);

    writeD(_fullSize);
    writeD(_waitingList.size());
    for (PartyMatchingWaitingInfo waiting_info : _waitingList)
    {
      writeS(waiting_info.name);
      writeD(waiting_info.classId);
      writeD(waiting_info.level);
      writeD(waiting_info.currentInstance);
      writeD(waiting_info.instanceReuses.length);
      for (int i : waiting_info.instanceReuses)
        writeD(i); 
    }
  }
  static class PartyMatchingWaitingInfo { public final int classId;
    public final int level;
    public final int currentInstance;
    public final String name;
    public final int[] instanceReuses;

    public PartyMatchingWaitingInfo(Player member) { name = member.getName();
      classId = member.getClassId().getId();
      level = member.getLevel();
      Reflection ref = member.getReflection();
      currentInstance = (ref == null ? 0 : ref.getInstancedZoneId());
      instanceReuses = ArrayUtils.toArray(member.getInstanceReuses().keySet());
    }
  }
}