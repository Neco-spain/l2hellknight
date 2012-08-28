package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class ExMoveToTargetInAirShip extends L2GameServerPacket
{
  private int char_id;
  private int boat_id;
  private int target_id;
  private int _dist;
  private Location _loc;

  public ExMoveToTargetInAirShip(Player cha, Boat boat, int targetId, int dist, Location origin)
  {
    char_id = cha.getObjectId();
    boat_id = boat.getObjectId();
    target_id = targetId;
    _dist = dist;
    _loc = origin;
  }

  protected final void writeImpl()
  {
    writeEx(113);

    writeD(char_id);
    writeD(target_id);
    writeD(_dist);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
    writeD(boat_id);
  }
}