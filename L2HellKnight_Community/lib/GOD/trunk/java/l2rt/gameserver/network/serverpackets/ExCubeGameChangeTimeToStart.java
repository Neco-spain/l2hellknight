package l2rt.gameserver.network.serverpackets;

/**
 * Format: (chd) d
 * d: seconds left
 */
public class ExCubeGameChangeTimeToStart extends L2GameServerPacket
{
	int _seconds;

	public ExCubeGameChangeTimeToStart(int seconds)
	{
		_seconds = seconds;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x97);
		writeD(0x03);

		writeD(_seconds);
	}
}