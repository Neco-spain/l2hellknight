package l2p.gameserver.serverpackets;

/**
 * Opens the CommandChannel Information window
 */
public class ExMPCCOpen extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExMPCCOpen();

	@Override
	protected void writeImpl()
	{
		writeEx(0x12);
	}
}