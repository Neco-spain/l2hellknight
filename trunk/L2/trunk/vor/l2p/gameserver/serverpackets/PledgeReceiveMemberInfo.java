package l2p.gameserver.serverpackets;

import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.model.pledge.UnitMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
  private UnitMember _member;

  public PledgeReceiveMemberInfo(UnitMember member)
  {
    _member = member;
  }

  protected final void writeImpl()
  {
    writeEx(62);

    writeD(_member.getPledgeType());
    writeS(_member.getName());
    writeS(_member.getTitle());
    writeD(_member.getPowerGrade());
    writeS(_member.getSubUnit().getName());
    writeS(_member.getRelatedName());
  }
}