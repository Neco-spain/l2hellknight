package l2rt.gameserver.network.serverpackets;

public class ExEventMatchUserInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x02);
		// TODO dSdddddddd
	}
}