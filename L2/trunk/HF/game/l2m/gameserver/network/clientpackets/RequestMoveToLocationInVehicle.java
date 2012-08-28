package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.BoatHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.utils.Location;

public class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
  private Location _pos = new Location();
  private Location _originPos = new Location();
  private int _boatObjectId;

  protected void readImpl()
  {
    _boatObjectId = readD();
    _pos.x = readD();
    _pos.y = readD();
    _pos.z = readD();
    _originPos.x = readD();
    _originPos.y = readD();
    _originPos.z = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Boat boat = BoatHolder.getInstance().getBoat(_boatObjectId);
    if (boat == null)
    {
      player.sendActionFailed();
      return;
    }

    boat.moveInBoat(player, _originPos, _pos);
  }
}