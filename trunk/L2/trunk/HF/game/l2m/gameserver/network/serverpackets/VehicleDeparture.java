package l2m.gameserver.serverpackets;

import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class VehicleDeparture extends L2GameServerPacket
{
  private int _moveSpeed;
  private int _rotationSpeed;
  private int _boatObjId;
  private Location _loc;

  public VehicleDeparture(Boat boat)
  {
    _boatObjId = boat.getObjectId();
    _moveSpeed = boat.getMoveSpeed();
    _rotationSpeed = boat.getRotationSpeed();
    _loc = boat.getDestination();
  }

  protected final void writeImpl()
  {
    writeC(108);
    writeD(_boatObjId);
    writeD(_moveSpeed);
    writeD(_rotationSpeed);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
  }
}