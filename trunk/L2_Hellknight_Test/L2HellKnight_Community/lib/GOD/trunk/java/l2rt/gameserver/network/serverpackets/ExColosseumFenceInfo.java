package l2rt.gameserver.network.serverpackets;

public class ExColosseumFenceInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x03);
		// TODO ddddddd
	}
}