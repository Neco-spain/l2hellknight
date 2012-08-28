package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(130);
  }

  public String getType()
  {
    return "S.PledgeShowMemberListDeleteAll";
  }
}