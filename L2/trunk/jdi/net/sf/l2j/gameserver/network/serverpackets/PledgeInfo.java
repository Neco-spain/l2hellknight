package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;

public class PledgeInfo extends L2GameServerPacket
{
  private static final String _S__9C_PLEDGEINFO = "[S] 9C PledgeInfo";
  private L2Clan _clan;

  public PledgeInfo(L2Clan clan)
  {
    _clan = clan;
  }

  protected final void writeImpl()
  {
    writeC(131);
    writeD(_clan.getClanId());
    writeS(_clan.getName());
    writeS(_clan.getAllyName());
  }

  public String getType()
  {
    return "[S] 9C PledgeInfo";
  }
}