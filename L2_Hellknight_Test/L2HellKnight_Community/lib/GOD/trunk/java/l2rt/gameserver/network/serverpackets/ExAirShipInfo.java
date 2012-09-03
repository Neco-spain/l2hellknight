package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.util.Location;

public class ExAirShipInfo extends L2GameServerPacket
{
	private int _objId, _speed1, _speed2, _fuel, _driverObjId, _ownerObjId;
	private Location _loc;

	public ExAirShipInfo(L2AirShip ship)
	{
		_objId = ship.getObjectId();
		_loc = ship.getLoc();
		_speed1 = ship.getRunSpeed();
		_speed2 = ship.getRotationSpeed();
		_fuel = ship.getFuel();
		_driverObjId = ship.getDriver() == null ? 0 : ship.getDriver().getObjectId();
		_ownerObjId = ship.getOwner() == null ? 0 : ship.getOwner().getClanId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x60);

		writeD(_objId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(_driverObjId); // object id of player who control ship
		writeD(_speed1);
		writeD(_speed2);

		// clan airship related info
		writeD(_ownerObjId); // clan-owner object id?
		writeD(366); // 366
		writeD(0x00); // 0
		writeD(0x6b); // 107
		writeD(348); // 348
		writeD(0x00); // 0
		writeD(0x69); // 105
		writeD(_fuel); // current fuel
		writeD(L2AirShip.MAX_FUEL); // max fuel
	}
}