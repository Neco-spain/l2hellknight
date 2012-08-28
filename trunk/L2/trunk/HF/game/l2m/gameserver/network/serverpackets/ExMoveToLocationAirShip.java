package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class ExMoveToLocationAirShip extends L2GameServerPacket
{
  private int _objectId;
  private Location _origin;
  private Location _destination;

  public ExMoveToLocationAirShip(Boat boat)
  {
    _objectId = boat.getObjectId();
    _origin = boat.getLoc();
    _destination = boat.getDestination();
  }

  protected final void writeImpl()
  {
    writeEx(101);
    writeD(_objectId);

    writeD(_destination.x);
    writeD(_destination.y);
    writeD(_destination.z);
    writeD(_origin.x);
    writeD(_origin.y);
    writeD(_origin.z);
  }
}