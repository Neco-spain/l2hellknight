package l2p.gameserver.serverpackets;

public class RestartResponse extends L2GameServerPacket
{
  public static final RestartResponse OK = new RestartResponse(1); public static final RestartResponse FAIL = new RestartResponse(0);
  private String _message;
  private int _param;

  public RestartResponse(int param)
  {
    _message = "bye";
    _param = param;
  }

  protected final void writeImpl()
  {
    writeC(113);
    writeD(_param);
    writeS(_message);
  }
}