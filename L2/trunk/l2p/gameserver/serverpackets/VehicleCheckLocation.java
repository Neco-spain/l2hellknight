package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class VehicleCheckLocation extends L2GameServerPacket
{
  private int _boatObjectId;
  private Location _loc;

  public VehicleCheckLocation(Boat instance)
  {
    _boatObjectId = instance.getObjectId();
    _loc = instance.getLoc();
  }

  protected final void writeImpl()
  {
    writeC(109);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
  }
}