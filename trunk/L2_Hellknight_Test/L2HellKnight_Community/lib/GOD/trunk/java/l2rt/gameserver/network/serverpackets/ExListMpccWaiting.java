package l2rt.gameserver.network.serverpackets;

public class ExListMpccWaiting extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x9C);
		// TODO ...
	}
}