package l2r.gameserver.network.serverpackets;

public class ExBR_BuffEventState extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0xDB);
		// TODO dddd
	}
}