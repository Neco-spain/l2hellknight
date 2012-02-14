package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.StopMoveToLocationInVehicle;
import l2rt.util.Location;

// format: cddddd
public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private Location _loc = new Location();
	private int _boatid;

	@Override
	public void readImpl()
	{
		_boatid = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isInVehicle() && activeChar.getVehicle().getObjectId() == _boatid)
		{
			activeChar.setInVehiclePosition(_loc);
			activeChar.setHeading(_loc.h);
			activeChar.broadcastPacket(new StopMoveToLocationInVehicle(activeChar, _boatid));
		}
	}
}