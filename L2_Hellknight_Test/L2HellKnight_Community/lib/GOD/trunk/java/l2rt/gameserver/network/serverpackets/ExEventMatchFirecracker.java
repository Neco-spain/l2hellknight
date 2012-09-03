package l2rt.gameserver.network.serverpackets;

public class ExEventMatchFirecracker extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x05);
		// TODO d
	}
}