package l2p.gameserver.serverpackets;

public class ExEventMatchGMTest extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x07);
		// just trigger
	}
}