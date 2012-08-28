package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.BoatHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.model.entity.events.impl.BoatWayEvent;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.utils.Location;

public class RequestGetOnVehicle extends L2GameClientPacket
{
  private int _objectId;
  private Location _loc = new Location();

  protected void readImpl()
  {
    _objectId = readD();
    _loc.x = readD();
    _loc.y = readD();
    _loc.z = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Boat boat = BoatHolder.getInstance().getBoat(_objectId);
    if (boat == null) {
      return;
    }
    player._stablePoint = boat.getCurrentWay().getReturnLoc();
    boat.addPlayer(player, _loc);
  }
}