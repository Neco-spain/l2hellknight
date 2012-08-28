package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class ExStopMoveAirShip extends L2GameServerPacket
{
  private int boat_id;
  private Location _loc;

  public ExStopMoveAirShip(Boat boat)
  {
    boat_id = boat.getObjectId();
    _loc = boat.getLoc();
  }

  protected final void writeImpl()
  {
    writeEx(102);
    writeD(boat_id);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
  }
}