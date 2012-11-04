package l2r.gameserver.network.serverpackets;

public class ExEventMatchFirecracker extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x05);
		// TODO d
	}
}