package l2rt.gameserver.network.serverpackets;

public class ExEventMatchManage extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x30);
		// TODO dccScScd[ccdSdd]
	}
}