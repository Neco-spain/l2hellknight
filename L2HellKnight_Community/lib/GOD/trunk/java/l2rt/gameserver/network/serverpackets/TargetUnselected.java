package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Object;
import l2rt.util.Location;

/**
 * format  ddddd
 */
public class TargetUnselected extends L2GameServerPacket
{
	private int _targetId;
	private Location _loc;

	public TargetUnselected(L2Object obj)
	{
		_targetId = obj.getObjectId();
		_loc = obj.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x24);
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(0x00); // иногда бывает 1
	}
}