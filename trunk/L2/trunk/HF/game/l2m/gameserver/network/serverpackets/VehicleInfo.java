package l2m.gameserver.serverpackets;

import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class VehicleInfo extends L2GameServerPacket
{
  private int _boatObjectId;
  private Location _loc;

  public VehicleInfo(Boat boat)
  {
    _boatObjectId = boat.getObjectId();
    _loc = boat.getLoc();
  }

  protected final void writeImpl()
  {
    writeC(96);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
  }
}