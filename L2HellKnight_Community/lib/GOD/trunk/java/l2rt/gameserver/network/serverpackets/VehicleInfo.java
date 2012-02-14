package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.util.Location;

public class VehicleInfo extends L2GameServerPacket
{
	private int _boatObjId;
	private Location _loc;

	public VehicleInfo(L2Ship boat)
	{
		_boatObjId = boat.getObjectId();
		_loc = boat.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x60);
		writeD(_boatObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}