package l2rt.gameserver.network.serverpackets;

public class ExRaidCharSelected extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xB5);
		// just a trigger
	}
}