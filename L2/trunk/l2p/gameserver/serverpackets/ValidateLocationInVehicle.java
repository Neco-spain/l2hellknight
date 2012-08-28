package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

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