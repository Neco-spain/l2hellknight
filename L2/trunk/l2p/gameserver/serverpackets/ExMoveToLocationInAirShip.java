package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class ExMoveToLocationInAirShip extends L2GameServerPacket
{
  private int char_id;
  private int boat_id;
  private Location _origin;
  private Location _destination;

  public ExMoveToLocationInAirShip(Player cha, Boat boat, Location origin, Location destination)
  {
    char_id = cha.getObjectId();
    boat_id = boat.getObjectId();
    _origin = origin;
    _destination = destination;
  }

  protected final void writeImpl()
  {
    writeEx(109);
    writeD(char_id);
    writeD(boat_id);

    writeD(_destination.x);
    writeD(_destination.y);
    writeD(_destination.z);
    writeD(_origin.x);
    writeD(_origin.y);
    writeD(_origin.z);
  }
}