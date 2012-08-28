package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class StopMoveToLocationInVehicle extends L2GameServerPacket
{
  private int _boatObjectId;
  private int _playerObjectId;
  private int _heading;
  private Location _loc;

  public StopMoveToLocationInVehicle(Player player)
  {
    _boatObjectId = player.getBoat().getObjectId();
    _playerObjectId = player.getObjectId();
    _loc = player.getInBoatPosition();
    _heading = player.getHeading();
  }

  protected final void writeImpl()
  {
    writeC(127);
    writeD(_playerObjectId);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_heading);
  }
}