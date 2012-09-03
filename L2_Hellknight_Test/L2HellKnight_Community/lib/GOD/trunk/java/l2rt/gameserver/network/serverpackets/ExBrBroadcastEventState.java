package l2rt.gameserver.network.serverpackets;

public class ExBrBroadcastEventState extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0xBD);
		// TODO dddddddSS
	}
}