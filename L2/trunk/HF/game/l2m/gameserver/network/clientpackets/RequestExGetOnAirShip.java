package l2m.gameserver.network.clientpackets;

import l2m.gameserver.utils.Location;

public class RequestExGetOnAirShip extends L2GameClientPacket
{
  private int _shipId;
  private Location loc = new Location();

  protected void readImpl()
  {
    loc.x = readD();
    loc.y = readD();
    loc.z = readD();
    _shipId = readD();
  }

  protected void runImpl()
  {
  }
}