package l2m.gameserver.serverpackets;

import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.RankPrivs;
import l2m.gameserver.model.pledge.UnitMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
  private int PowerGrade;
  private int privs;
  private String member_name;

  public PledgeReceivePowerInfo(UnitMember member)
  {
    PowerGrade = member.getPowerGrade();
    member_name = member.getName();
    if (member.isClanLeader()) {
      privs = 16777214;
    }
    else {
      RankPrivs temp = member.getClan().getRankPrivs(member.getPowerGrade());
      if (temp != null)
        privs = temp.getPrivs();
      else
        privs = 0;
    }
  }

  protected final void writeImpl()
  {
    writeEx(61);
    writeD(PowerGrade);
    writeS(member_name);
    writeD(privs);
  }
}