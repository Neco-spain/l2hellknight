package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.entity.vehicle.L2VehicleManager;
import l2rt.gameserver.network.serverpackets.ExGetOnAirShip;
import l2rt.util.Location;

public class RequestExGetOnAirShip extends L2GameClientPacket
{
	private int _shipId;
	private Location loc = new Location();

	@Override
	protected void readImpl()
	{
		loc.x = readD();
		loc.y = readD();
		loc.z = readD();
		_shipId = readD();
	}

	@Override
	protected void runImpl()
	{
		System.out.println("[T1:ExGetOnAirShip] loc: " + loc);
		System.out.println("[T1:ExGetOnAirShip] ship ID: " + _shipId);

		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		L2AirShip boat = (L2AirShip) L2VehicleManager.getInstance().getBoat(_shipId);
		if(boat == null)
			return;
		activeChar.stopMove();
		activeChar.setVehicle(boat);
		activeChar.setInVehiclePosition(loc);
		activeChar.setLoc(boat.getLoc());
		activeChar.broadcastPacket(new ExGetOnAirShip(activeChar, boat, loc));
	}
}