package l2p.gameserver.serverpackets;

public class ExEventMatchUserInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x02);
		// TODO dSdddddddd
	}
}