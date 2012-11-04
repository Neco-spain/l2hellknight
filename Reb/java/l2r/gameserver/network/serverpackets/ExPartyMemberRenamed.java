package l2r.gameserver.network.serverpackets;

public class ExPartyMemberRenamed extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0xA6);
		// TODO ddd
	}
}