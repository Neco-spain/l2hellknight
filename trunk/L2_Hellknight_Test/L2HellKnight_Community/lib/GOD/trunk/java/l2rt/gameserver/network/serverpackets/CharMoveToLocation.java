package l2rt.gameserver.network.serverpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Character;
import l2rt.util.Location;

/**
 * 0000: 01  7a 73 10 4c  b2 0b 00 00  a3 fc 00 00  e8 f1 ff    .zs.L...........
 * 0010: ff  bd 0b 00 00  b3 fc 00 00  e8 f1 ff ff             .............
 *
 * ddddddd
 */
public class CharMoveToLocation extends L2GameServerPacket
{
	private int _objectId, _client_z_shift;
	private Location _current;
	private Location _destination;

	public CharMoveToLocation(L2Character cha)
	{
		_objectId = cha.getObjectId();
		_current = cha.getLoc();
		_destination = cha.getDestination();
		_client_z_shift = cha.isFlying() || cha.isInWater() ? 0 : Config.CLIENT_Z_SHIFT;
	}

	public CharMoveToLocation(int objectId, Location from, Location to)
	{
		_objectId = objectId;
		_current = from;
		_destination = to;
	}

	@Override
	protected final void writeImpl()
	{
		if(_destination == null)
			return;

		writeC(0x2f);

		writeD(_objectId);

		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z + _client_z_shift);

		writeD(_current.x);
		writeD(_current.y);
		writeD(_current.z + _client_z_shift);
	}
}