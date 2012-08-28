package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.ClassId;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
  private List<PartyMemberInfo> members;

  public ExMPCCShowPartyMemberInfo(Party party)
  {
    members = new ArrayList();
    for (Player _member : party.getPartyMembers())
      members.add(new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId()));
  }

  protected final void writeImpl()
  {
    writeEx(75);
    writeD(members.size());

    for (PartyMemberInfo member : members)
    {
      writeS(member.name);
      writeD(member.object_id);
      writeD(member.class_id);
    }

    members.clear();
  }
  static class PartyMemberInfo {
    public String name;
    public int object_id;
    public int class_id;

    public PartyMemberInfo(String _name, int _object_id, int _class_id) { name = _name;
      object_id = _object_id;
      class_id = _class_id;
    }
  }
}