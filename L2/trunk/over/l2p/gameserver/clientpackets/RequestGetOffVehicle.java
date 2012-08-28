package l2p.gameserver.clientpackets;

import l2p.gameserver.data.BoatHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.utils.Location;

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