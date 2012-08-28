package l2m.gameserver.network.serverpackets;

public class ExAskJoinMPCC extends L2GameServerPacket
{
  private String _requestorName;

  public ExAskJoinMPCC(String requestorName)
  {
    _requestorName = requestorName;
  }

  protected void writeImpl()
  {
    writeEx(26);
    writeS(_requestorName);
    writeD(0);
  }
}