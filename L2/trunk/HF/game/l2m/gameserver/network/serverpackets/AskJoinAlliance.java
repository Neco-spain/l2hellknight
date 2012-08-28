package l2m.gameserver.network.serverpackets;

public class AskJoinAlliance extends L2GameServerPacket
{
  private String _requestorName;
  private String _requestorAllyName;
  private int _requestorId;

  public AskJoinAlliance(int requestorId, String requestorName, String requestorAllyName)
  {
    _requestorName = requestorName;
    _requestorAllyName = requestorAllyName;
    _requestorId = requestorId;
  }

  protected final void writeImpl()
  {
    writeC(187);
    writeD(_requestorId);
    writeS(_requestorName);
    writeS("");
    writeS(_requestorAllyName);
  }
}