package l2rt.gameserver.network.serverpackets;

public class ExOlympiadMatchEnd extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x2D);
	}
}