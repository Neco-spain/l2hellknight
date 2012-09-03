package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.util.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int char_id, boat_id;
	private Location _origin, _destination;

	public MoveToLocationInVehicle(L2Player cha, L2Ship boat, Location origin, Location destination)
	{
		char_id = cha.getObjectId();
		boat_id = boat.getObjectId();
		_origin = origin;
		_destination = destination;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x7e);
		writeD(char_id);
		writeD(boat_id);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}
}