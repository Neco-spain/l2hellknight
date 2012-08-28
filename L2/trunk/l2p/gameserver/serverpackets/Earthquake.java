package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class Earthquake extends L2GameServerPacket
{
  private Location _loc;
  private int _intensity;
  private int _duration;

  public Earthquake(Location loc, int intensity, int duration)
  {
    _loc = loc;
    _intensity = intensity;
    _duration = duration;
  }

  protected final void writeImpl()
  {
    writeC(211);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_intensity);
    writeD(_duration);
    writeD(0);
  }
}