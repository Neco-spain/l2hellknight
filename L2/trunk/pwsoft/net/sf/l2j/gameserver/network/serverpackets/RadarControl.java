package net.sf.l2j.gameserver.network.serverpackets;

public class RadarControl extends L2GameServerPacket
{
  private int _showRadar;
  private int _type;
  private int _x;
  private int _y;
  private int _z;

  public RadarControl(int showRadar, int type, int x, int y, int z)
  {
    _showRadar = showRadar;
    _type = type;
    _x = x;
    _y = y;
    _z = z;
  }

  protected final void writeImpl()
  {
    writeC(235);
    writeD(_showRadar);
    writeD(_type);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "S.RadarControl";
  }
}