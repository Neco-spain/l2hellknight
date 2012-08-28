package l2m.gameserver.network.serverpackets;

import java.util.Collections;
import java.util.List;
import l2m.gameserver.model.entity.boat.ClanAirShip;
import l2m.gameserver.model.entity.events.objects.BoatPoint;
import l2m.gameserver.templates.AirshipDock;

public class ExAirShipTeleportList extends L2GameServerPacket
{
  private int _fuel;
  private List<BoatPoint> _airports = Collections.emptyList();

  public ExAirShipTeleportList(ClanAirShip ship)
  {
    _fuel = ship.getCurrentFuel();
    _airports = ship.getDock().getTeleportList();
  }

  protected void writeImpl()
  {
    writeEx(154);
    writeD(_fuel);
    writeD(_airports.size());

    for (int i = 0; i < _airports.size(); i++)
    {
      BoatPoint point = (BoatPoint)_airports.get(i);
      writeD(i - 1);
      writeD(point.getFuel());
      writeD(point.x);
      writeD(point.y);
      writeD(point.z);
    }
  }
}