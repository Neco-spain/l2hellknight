package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;

public class PledgeStatusChanged extends L2GameServerPacket
{
  private static final String _S__CD_PLEDGESTATUS_CHANGED = "[S] CD PledgeStatusChanged";
  private L2Clan _clan;

  public PledgeStatusChanged(L2Clan clan)
  {
    _clan = clan;
  }

  protected final void writeImpl()
  {
    writeC(205);
    writeD(_clan.getLeaderId());
    writeD(_clan.getClanId());
    writeD(0);
    writeD(_clan.getLevel());
    writeD(0);
    writeD(0);
    writeD(0);
  }

  public String getType()
  {
    return "[S] CD PledgeStatusChanged";
  }
}