package net.sf.l2j.gameserver.network.serverpackets;

public class SunSet extends L2GameServerPacket
{
  private static final String _S__29_SUNSET = "[S] 1d SunSet";

  protected final void writeImpl()
  {
    writeC(29);
  }

  public String getType()
  {
    return "[S] 1d SunSet";
  }
}