package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
  private FastList<PartyMemberInfo> members;

  public ExMPCCShowPartyMemberInfo(L2PcInstance partyLeader)
  {
    if (!partyLeader.isInParty()) {
      return;
    }
    L2Party _party = partyLeader.getParty();
    if (_party == null) {
      return;
    }
    if (!_party.isInCommandChannel()) {
      return;
    }
    members = new FastList();
    for (L2PcInstance _member : _party.getPartyMembers())
      members.add(new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId()));
  }

  protected final void writeImpl()
  {
    if (members == null) {
      return;
    }
    writeC(254);
    writeH(74);
    writeD(members.size());
    for (PartyMemberInfo _member : members)
    {
      writeS(_member._name);
      writeD(_member.object_id);
      writeD(_member.class_id);
    }
    members.clear();
  }
  static class PartyMemberInfo {
    public String _name;
    public int object_id;
    public int class_id;

    public PartyMemberInfo(String __name, int _object_id, int _class_id) { _name = __name;
      object_id = _object_id;
      class_id = _class_id;
    }
  }
}