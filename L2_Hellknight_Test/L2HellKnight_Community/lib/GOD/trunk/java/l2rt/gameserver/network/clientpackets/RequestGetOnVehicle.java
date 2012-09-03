package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.gameserver.model.entity.vehicle.L2VehicleManager;
import l2rt.gameserver.network.serverpackets.GetOnVehicle;
import l2rt.util.Location;

public class RequestGetOnVehicle extends L2GameClientPacket
{
	private int _id;
	private Location loc = new Location();

	/**
	 * packet type id 0x53
	 * format:      cdddd
	 */
	@Override
	public void readImpl()
	{
		_id = readD();
		loc.x = readD();
		loc.y = readD();
		loc.z = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		L2Ship boat = (L2Ship) L2VehicleManager.getInstance().getBoat(_id);
		if(boat == null)
			return;
		activeChar.stopMove();
		activeChar.setVehicle(boat);
		activeChar.setInVehiclePosition(loc);
		activeChar.setLoc(boat.getLoc());
		activeChar.broadcastPacket(new GetOnVehicle(activeChar, boat, loc));
	}
}