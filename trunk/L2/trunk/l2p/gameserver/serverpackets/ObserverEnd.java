package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class ObserverEnd extends L2GameServerPacket
{
  private Location _loc;

  public ObserverEnd(Location loc)
  {
    _loc = loc;
  }

  protected final void writeImpl()
  {
    writeC(236);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
  }
}