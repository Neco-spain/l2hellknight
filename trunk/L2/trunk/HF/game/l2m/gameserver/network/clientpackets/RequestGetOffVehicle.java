package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.BoatHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.utils.Location;

public class RequestGetOffVehicle extends L2GameClientPacket
{
  private int _objectId;
  private Location _location = new Location();

  protected void readImpl()
  {
    _objectId = readD();
    _location.x = readD();
    _location.y = readD();
    _location.z = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Boat boat = BoatHolder.getInstance().getBoat(_objectId);
    if ((boat == null) || (boat.isMoving))
    {
      player.sendActionFailed();
      return;
    }

    boat.oustPlayer(player, _location, false);
  }
}