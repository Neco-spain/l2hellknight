package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.utils.Location;

public class TeleportToLocation extends L2GameServerPacket
{
  private int _targetId;
  private Location _loc;

  public TeleportToLocation(GameObject cha, Location loc)
  {
    _targetId = cha.getObjectId();
    _loc = loc;
  }

  public TeleportToLocation(GameObject cha, int x, int y, int z)
  {
    _targetId = cha.getObjectId();
    _loc = new Location(x, y, z, cha.getHeading());
  }

  protected final void writeImpl()
  {
    writeC(34);
    writeD(_targetId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z + Config.CLIENT_Z_SHIFT);
    writeD(0);
    writeD(_loc.h);
  }
}