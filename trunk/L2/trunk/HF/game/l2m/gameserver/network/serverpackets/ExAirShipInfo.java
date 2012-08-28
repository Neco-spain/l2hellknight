package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.boat.AirShip;
import l2m.gameserver.model.entity.boat.ClanAirShip;
import l2m.gameserver.utils.Location;

public class ExAirShipInfo extends L2GameServerPacket
{
  private int _objId;
  private int _speed1;
  private int _speed2;
  private int _fuel;
  private int _maxFuel;
  private int _driverObjId;
  private int _controlKey;
  private Location _loc;

  public ExAirShipInfo(AirShip ship)
  {
    _objId = ship.getObjectId();
    _loc = ship.getLoc();
    _speed1 = ship.getRunSpeed();
    _speed2 = ship.getRotationSpeed();
    if (ship.isClanAirShip())
    {
      _fuel = ((ClanAirShip)ship).getCurrentFuel();
      _maxFuel = ((ClanAirShip)ship).getMaxFuel();
      Player driver = ((ClanAirShip)ship).getDriver();
      _driverObjId = (driver == null ? 0 : driver.getObjectId());
      _controlKey = ((ClanAirShip)ship).getControlKey().getObjectId();
    }
  }

  protected final void writeImpl()
  {
    writeEx(96);

    writeD(_objId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_loc.h);
    writeD(_driverObjId);
    writeD(_speed1);
    writeD(_speed2);
    writeD(_controlKey);

    if (_controlKey != 0)
    {
      writeD(366);
      writeD(0);
      writeD(107);
      writeD(348);
      writeD(0);
      writeD(105);
    }
    else
    {
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
    }

    writeD(_fuel);
    writeD(_maxFuel);
  }
}