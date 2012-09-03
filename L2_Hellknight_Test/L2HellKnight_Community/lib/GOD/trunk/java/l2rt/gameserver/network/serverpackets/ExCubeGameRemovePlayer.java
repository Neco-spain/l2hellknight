package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

/**
 * Format: (chd) ddd
 * d: always -1
 * d: player team
 * d: player object id
 */
public class ExCubeGameRemovePlayer extends L2GameServerPacket
{
	L2Player _player;
	boolean _isRedTeam;

	public ExCubeGameRemovePlayer(L2Player player, boolean isRedTeam)
	{
		_player = player;
		_isRedTeam = isRedTeam;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x97);
		writeD(0x02);

		writeD(0xffffffff);

		writeD(_isRedTeam ? 0x01 : 0x00);
		writeD(_player.getObjectId());
	}
}