package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.L2ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
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
      L2Clan.SubPledge sp = _member.getClan().getSubPledge(_member.getPledgeType());
      if (sp != null)
        writeS(sp.getName());
    } else {
      writeS(_member.getClan().getName());
    }
    writeS(_member.getApprenticeOrSponsorName());
  }
}