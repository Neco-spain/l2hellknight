package net.sf.l2j.gameserver.network.serverpackets;

public class ExAskJoinMPCC extends L2GameServerPacket
{
  private static final String _S__FE_27_EXASKJOINMPCC = "[S] FE:27 ExAskJoinMPCC";
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

  public String getType()
  {
    return "[S] FE:27 ExAskJoinMPCC";
  }
}