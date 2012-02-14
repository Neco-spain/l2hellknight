package l2rt.gameserver.network.serverpackets;

public class ExDissmissMpccRoom extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x9D);
		// just trigger
	}
}