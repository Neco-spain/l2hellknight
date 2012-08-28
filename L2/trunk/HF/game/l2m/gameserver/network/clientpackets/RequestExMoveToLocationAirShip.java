package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.Boat;
import l2m.gameserver.model.entity.boat.ClanAirShip;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.utils.Location;

public class RequestExMoveToLocationAirShip extends L2GameClientPacket
{
  private int _moveType;
  private int _param1;
  private int _param2;

  protected void readImpl()
  {
    _moveType = readD();
    switch (_moveType)
    {
    case 4:
      _param1 = (readD() + 1);
      break;
    case 0:
      _param1 = readD();
      _param2 = readD();
      break;
    case 2:
      readD();
      readD();
      break;
    case 3:
      readD();
      readD();
    case 1:
    }
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getBoat() == null) || (!player.getBoat().isClanAirShip())) {
      return;
    }
    ClanAirShip airship = (ClanAirShip)player.getBoat();
    if (airship.getDriver() == player)
      switch (_moveType)
      {
      case 4:
        airship.addTeleportPoint(player, _param1);
        break;
      case 0:
        if (!airship.isCustomMove())
          break;
        airship.moveToLocation(airship.getLoc().setX(_param1).setY(_param2), 0, false);
        break;
      case 2:
        if (!airship.isCustomMove())
          break;
        airship.moveToLocation(airship.getLoc().changeZ(100), 0, false);
        break;
      case 3:
        if (!airship.isCustomMove())
          break;
        airship.moveToLocation(airship.getLoc().changeZ(-100), 0, false);
      case 1:
      }
  }
}