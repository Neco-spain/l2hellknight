package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
  private int _playerObjectId;
  private int _boatObjectId;
  private Location _loc;

  public ValidateLocationInVehicle(Player player)
  {
    _playerObjectId = player.getObjectId();
    _boatObjectId = player.getBoat().getObjectId();
    _loc = player.getInBoatPosition();
  }

  protected final void writeImpl()
  {
    writeC(128);
    writeD(_playerObjectId);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
  }
}