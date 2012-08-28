package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class ExGetOffAirShip extends L2GameServerPacket
{
  private int _playerObjectId;
  private int _boatObjectId;
  private Location _loc;

  public ExGetOffAirShip(Player cha, Boat boat, Location loc)
  {
    _playerObjectId = cha.getObjectId();
    _boatObjectId = boat.getObjectId();
    _loc = loc;
  }

  protected final void writeImpl()
  {
    writeEx(100);
    writeD(_playerObjectId);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
  }
}