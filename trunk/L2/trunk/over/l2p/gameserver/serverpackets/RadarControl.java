package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class RadarControl extends L2GameServerPacket
{
  private int _x;
  private int _y;
  private int _z;
  private int _type;
  private int _showRadar;

  public RadarControl(int showRadar, int type, Location loc)
  {
    this(showRadar, type, loc.x, loc.y, loc.z);
  }

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
    writeC(241);
    writeD(_showRadar);
    writeD(_type);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }
}