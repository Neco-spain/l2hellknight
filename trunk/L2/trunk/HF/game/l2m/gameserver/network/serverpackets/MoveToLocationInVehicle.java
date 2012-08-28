package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
  private int _playerObjectId;
  private int _boatObjectId;
  private Location _origin;
  private Location _destination;

  public MoveToLocationInVehicle(Player cha, Boat boat, Location origin, Location destination)
  {
    _playerObjectId = cha.getObjectId();
    _boatObjectId = boat.getObjectId();
    _origin = origin;
    _destination = destination;
  }

  protected final void writeImpl()
  {
    writeC(126);
    writeD(_playerObjectId);
    writeD(_boatObjectId);
    writeD(_destination.x);
    writeD(_destination.y);
    writeD(_destination.z);
    writeD(_origin.x);
    writeD(_origin.y);
    writeD(_origin.z);
  }
}