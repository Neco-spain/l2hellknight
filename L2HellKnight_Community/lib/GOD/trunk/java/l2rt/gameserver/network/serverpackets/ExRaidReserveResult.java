package l2rt.gameserver.network.serverpackets;

public class ExRaidReserveResult extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB6);
		// TODO dx[dddd]
	}
}