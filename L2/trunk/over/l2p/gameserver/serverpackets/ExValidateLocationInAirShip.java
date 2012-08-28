package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class ExValidateLocationInAirShip extends L2GameServerPacket
{
  private int _playerObjectId;
  private int _boatObjectId;
  private Location _loc;

  public ExValidateLocationInAirShip(Player cha)
  {
    _playerObjectId = cha.getObjectId();
    _boatObjectId = cha.getBoat().getObjectId();
    _loc = cha.getInBoatPosition();
  }

  protected final void writeImpl()
  {
    writeEx(111);

    writeD(_playerObjectId);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
  }
}