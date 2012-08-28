package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
  private L2ClanMember _member;

  public PledgeReceivePowerInfo(L2ClanMember member)
  {
    _member = member;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(60);

    writeD(_member.getPowerGrade());
    writeS(_member.getName());
    writeD(_member.getClan().getRankPrivs(_member.getPowerGrade()));
  }
}