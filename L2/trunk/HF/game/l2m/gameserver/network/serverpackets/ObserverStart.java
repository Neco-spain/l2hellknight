package l2m.gameserver.serverpackets;

import l2m.gameserver.utils.Location;

public class ObserverStart extends L2GameServerPacket
{
  private Location _loc;

  public ObserverStart(Location loc)
  {
    _loc = loc;
  }

  protected final void writeImpl()
  {
    writeC(235);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeC(0);
    writeC(192);
    writeC(0);
  }
}