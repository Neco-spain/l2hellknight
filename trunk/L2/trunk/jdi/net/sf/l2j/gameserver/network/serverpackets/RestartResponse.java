package net.sf.l2j.gameserver.network.serverpackets;

public class RestartResponse extends L2GameServerPacket
{
  private static final String _S__74_RESTARTRESPONSE = "[S] 74 RestartResponse";
  private String _message;

  public RestartResponse()
  {
    _message = "ok merong~ khaha";
  }

  protected final void writeImpl()
  {
    writeC(95);

    writeD(1);
    writeS(_message);
  }

  public String getType()
  {
    return "[S] 74 RestartResponse";
  }
}