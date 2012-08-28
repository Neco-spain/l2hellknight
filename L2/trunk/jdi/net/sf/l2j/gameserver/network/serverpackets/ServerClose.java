package net.sf.l2j.gameserver.network.serverpackets;

public class ServerClose extends L2GameServerPacket
{
  private static final String _S__26_SERVERCLOSE = "[S] 26 ServerClose";

  protected void writeImpl()
  {
    writeC(38);
  }

  public String getType()
  {
    return "[S] 26 ServerClose";
  }
}