package l2m.gameserver.network.serverpackets;

public class FriendAddRequest extends L2GameServerPacket
{
  private String _requestorName;

  public FriendAddRequest(String requestorName)
  {
    _requestorName = requestorName;
  }

  protected final void writeImpl()
  {
    writeC(131);
    writeS(_requestorName);
  }
}