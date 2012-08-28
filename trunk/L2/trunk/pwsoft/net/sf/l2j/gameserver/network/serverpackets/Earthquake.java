package net.sf.l2j.gameserver.network.serverpackets;

public class Earthquake extends L2GameServerPacket
{
  private int _x;
  private int _y;
  private int _z;
  private int _intensity;
  private int _duration;

  public Earthquake(int x, int y, int z, int intensity, int duration)
  {
    _x = x;
    _y = y;
    _z = z;
    _intensity = intensity;
    _duration = duration;
  }

  protected final void writeImpl()
  {
    writeC(196);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_intensity);
    writeD(_duration);
    writeD(0);
  }
}