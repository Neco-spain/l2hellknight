package l2m.gameserver.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.utils.Location;

public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
  private Location _loc = new Location();
  private int _boatid;

  protected void readImpl()
  {
    _boatid = readD();
    _loc.x = readD();
    _loc.y = readD();
    _loc.z = readD();
    _loc.h = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Boat boat = player.getBoat();
    if ((boat != null) && (boat.getObjectId() == _boatid))
    {
      player.setInBoatPosition(_loc);
      player.setHeading(_loc.h);
      player.broadcastPacket(new L2GameServerPacket[] { boat.inStopMovePacket(player) });
    }
  }
}