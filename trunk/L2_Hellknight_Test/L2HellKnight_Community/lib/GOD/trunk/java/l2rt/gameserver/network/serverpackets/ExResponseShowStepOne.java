package l2rt.gameserver.network.serverpackets;

public class ExResponseShowStepOne extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xAE);
		// TODO dx[cS]
	}
}