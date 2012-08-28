package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
  private L2Clan _clan;

  public PledgeShowInfoUpdate(L2Clan clan)
  {
    _clan = clan;
  }

  protected final void writeImpl()
  {
    writeC(136);

    writeD(_clan.getClanId());
    writeD(0);
    writeD(_clan.getLevel());
    writeD(_clan.getHasCastle());
    writeD(_clan.getHasHideout());
    writeD(0);
    writeD(_clan.getReputationScore());
    writeD(0);
    writeD(0);

    writeD(0);
    writeS("bili");
    writeD(0);
    writeD(0);
  }

  public String getType()
  {
    return "S.PledgeShowInfoUpdate";
  }
}