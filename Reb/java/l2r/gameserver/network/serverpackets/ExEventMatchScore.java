package l2r.gameserver.network.serverpackets;

public class ExEventMatchScore extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x10);
		// TODO ddd
	}
}