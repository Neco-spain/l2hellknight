package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class GetOnVehicle extends L2GameServerPacket
{
  private int _playerObjectId;
  private int _boatObjectId;
  private Location _loc;

  public GetOnVehicle(Player activeChar, Boat boat, Location loc)
  {
    _loc = loc;
    _playerObjectId = activeChar.getObjectId();
    _boatObjectId = boat.getObjectId();
  }

  protected final void writeImpl()
  {
    writeC(110);
    writeD(_playerObjectId);
    writeD(_boatObjectId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
  }
}