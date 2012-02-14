package l2rt.gameserver.network.serverpackets;

public class ExEventMatchScore extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x10);
		// TODO ddd
	}
}