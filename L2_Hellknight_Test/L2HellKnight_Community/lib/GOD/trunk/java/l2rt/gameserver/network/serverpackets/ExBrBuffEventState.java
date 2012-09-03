package l2rt.gameserver.network.serverpackets;

public class ExBrBuffEventState extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xBF);
		// TODO dddd
	}
}