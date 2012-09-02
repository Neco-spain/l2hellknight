package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.boat.Boat;
import l2p.gameserver.utils.Location;

public class ExStopMoveAirShip extends L2GameServerPacket
{
	private int boat_id;
	private Location _loc;

	public ExStopMoveAirShip(Boat boat)
	{
		boat_id = boat.getObjectId();
		_loc = boat.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x66);
		writeD(boat_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}