package l2rt.gameserver.network.serverpackets;

import l2rt.util.Location;

public class AirShipMoveToLocation extends L2GameServerPacket
{
	private int _objectId;
	private Location _destination;

	public AirShipMoveToLocation(int objectId, Location to)
	{
		_objectId = objectId;
		_destination = to;
	}

	protected final void writeImpl()
	{
		writeC(47);
		writeD(_objectId);

		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
	}
}