package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.StaticObjectInstance;

/**
 * format: d
 */
public class ChairSit extends L2GameServerPacket
{
	private int _objectId;
	private int _staticObjectId;

	public ChairSit(Player player, StaticObjectInstance throne)
	{
		_objectId = player.getObjectId();
		_staticObjectId = throne.getUId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xed);
		writeD(_objectId);
		writeD(_staticObjectId);
	}
}