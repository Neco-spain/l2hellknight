package l2rt.gameserver.network.serverpackets;

public class ServerClose extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0x20);
	}
}