package net.sf.l2j.gameserver.network.serverpackets;

public class ObservationMode extends L2GameServerPacket
{
  private static final String _S__DF_OBSERVMODE = "[S] DF ObservationMode";
  private int _x;
  private int _y;
  private int _z;

  public ObservationMode(int x, int y, int z)
  {
    _x = x;
    _y = y;
    _z = z;
  }

  protected final void writeImpl()
  {
    writeC(223);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeC(0);
    writeC(192);
    writeC(0);
  }

  public String getType()
  {
    return "[S] DF ObservationMode";
  }
}