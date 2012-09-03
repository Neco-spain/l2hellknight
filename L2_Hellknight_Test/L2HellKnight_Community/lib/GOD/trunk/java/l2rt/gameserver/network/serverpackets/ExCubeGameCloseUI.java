package l2rt.gameserver.network.serverpackets;

/**
 * Format: (chd)
 */
public class ExCubeGameCloseUI extends L2GameServerPacket
{
	int _seconds;

	public ExCubeGameCloseUI(int seconds)
	{
		_seconds = seconds;
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x97);
		writeD(0xffffffff);
	}
}