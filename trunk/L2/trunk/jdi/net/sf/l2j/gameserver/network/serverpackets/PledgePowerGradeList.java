package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan.RankPrivs;

public class PledgePowerGradeList extends L2GameServerPacket
{
  private static final String _S__FE_3B_PLEDGEPOWERGRADELIST = "[S] FE:3B PledgePowerGradeList";
  private L2Clan.RankPrivs[] _privs;

  public PledgePowerGradeList(L2Clan.RankPrivs[] privs)
  {
    _privs = privs;
  }

  protected final void writeImpl()
  {
    writeC(254);
    writeH(59);
    writeD(_privs.length);
    for (int i = 0; i < _privs.length; i++)
    {
      writeD(_privs[i].getRank());
      writeD(_privs[i].getParty());
    }
  }

  public String getType()
  {
    return "[S] FE:3B PledgePowerGradeList";
  }
}