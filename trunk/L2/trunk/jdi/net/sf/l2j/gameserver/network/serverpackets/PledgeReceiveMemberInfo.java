package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.L2ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
  private static final String _S__FE_3D_PLEDGERECEIVEMEMBERINFO = "[S] FE:3D PledgeReceiveMemberInfo";
  private L2ClanMember _member;

  public PledgeReceiveMemberInfo(L2ClanMember member)
  {
    _member = member;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(61);

    writeD(_member.getPledgeType());
    writeS(_member.getName());
    writeS(_member.getTitle());
    writeD(_member.getPowerGrade());

    if (_member.getPledgeType() != 0)
    {
      writeS(_member.getClan().getSubPledge(_member.getPledgeType()).getName());
    }
    else writeS(_member.getClan().getName());

    writeS(_member.getApprenticeOrSponsorName());
  }

  public String getType()
  {
    return "[S] FE:3D PledgeReceiveMemberInfo";
  }
}