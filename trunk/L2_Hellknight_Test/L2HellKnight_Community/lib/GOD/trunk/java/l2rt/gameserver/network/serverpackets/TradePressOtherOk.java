package l2rt.gameserver.network.serverpackets;

public class TradePressOtherOk extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0x82);
	}
}