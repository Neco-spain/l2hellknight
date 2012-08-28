package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import l2p.commons.lang.ArrayUtils;
import l2p.gameserver.instancemanager.MatchingRoomManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.matching.MatchingRoom;

public class ExPartyRoomMember extends L2GameServerPacket
{
  private int _type;
  private List<PartyRoomMemberInfo> _members = Collections.emptyList();

  public ExPartyRoomMember(MatchingRoom room, Player activeChar)
  {
    _type = room.getMemberType(activeChar);
    _members = new ArrayList(room.getPlayers().size());
    for (Player $member : room.getPlayers())
      _members.add(new PartyRoomMemberInfo($member, room.getMemberType($member)));
  }

  protected final void writeImpl()
  {
    writeEx(8);
    writeD(_type);
    writeD(_members.size());
    for (PartyRoomMemberInfo member_info : _members)
    {
      writeD(member_info.objectId);
      writeS(member_info.name);
      writeD(member_info.classId);
      writeD(member_info.level);
      writeD(member_info.location);
      writeD(member_info.memberType);
      writeD(member_info.instanceReuses.length);
      for (int i : member_info.instanceReuses)
        writeD(i);  }  } 
  static class PartyRoomMemberInfo { public final int objectId;
    public final int classId;
    public final int level;
    public final int location;
    public final int memberType;
    public final String name;
    public final int[] instanceReuses;

    public PartyRoomMemberInfo(Player member, int type) { objectId = member.getObjectId();
      name = member.getName();
      classId = member.getClassId().ordinal();
      level = member.getLevel();
      location = MatchingRoomManager.getInstance().getLocation(member);
      memberType = type;
      instanceReuses = ArrayUtils.toArray(member.getInstanceReuses().keySet());
    }
  }
}