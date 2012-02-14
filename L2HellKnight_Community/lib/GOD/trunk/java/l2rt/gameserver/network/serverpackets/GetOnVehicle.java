package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.entity.vehicle.L2Ship;
import l2rt.util.Location;

public class GetOnVehicle extends L2GameServerPacket
{
	private int char_obj_id, boat_obj_id;
	private Location _loc;

	public GetOnVehicle(L2Player activeChar, L2Ship boat, Location loc)
	{
		_loc = loc;
		char_obj_id = activeChar.getObjectId();
		boat_obj_id = boat.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x6e);
		writeD(char_obj_id);
		writeD(boat_obj_id);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}