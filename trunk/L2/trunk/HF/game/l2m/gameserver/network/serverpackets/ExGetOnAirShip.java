package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class ExGetOnAirShip extends L2GameServerPacket
{
  private int _playerObjectId;
  private int _boatObjectId;
  private Location _loc;

  public ExGetOnAirShip(Player cha, Boat boat, Location loc)
  {
    _playerObjectId = cha.getObjectId();
    _boatObjectId = boat.getObjectId();
    _loc = loc;
  }

  protected final void writeImpl()
  {
    writeEx(99);
    writeD(_playerObjectId);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
  }
}