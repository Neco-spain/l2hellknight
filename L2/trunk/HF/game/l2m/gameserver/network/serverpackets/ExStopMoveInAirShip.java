package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.utils.Location;

public class ExStopMoveInAirShip extends L2GameServerPacket
{
  private int char_id;
  private int boat_id;
  private int char_heading;
  private Location _loc;

  public ExStopMoveInAirShip(Player cha)
  {
    char_id = cha.getObjectId();
    boat_id = cha.getBoat().getObjectId();
    _loc = cha.getInBoatPosition();
    char_heading = cha.getHeading();
  }

  protected final void writeImpl()
  {
    writeEx(110);

    writeD(char_id);
    writeD(boat_id);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(char_heading);
  }
}