package net.sf.l2j.gameserver.network.serverpackets;

public class ExAskJoinMPCC extends L2GameServerPacket
{
  private String _requestorName;

  public ExAskJoinMPCC(String requestorName)
  {
    _requestorName = requestorName;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(39);
    writeS(_requestorName);
  }
}