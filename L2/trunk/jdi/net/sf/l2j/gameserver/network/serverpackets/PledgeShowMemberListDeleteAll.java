package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
  private static final String _S__9B_PLEDGESHOWMEMBERLISTDELETEALL = "[S] 82 PledgeShowMemberListDeleteAll";

  protected final void writeImpl()
  {
    writeC(130);
  }

  public String getType()
  {
    return "[S] 82 PledgeShowMemberListDeleteAll";
  }
}