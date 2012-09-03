package l2rt.gameserver.network.serverpackets;

public class ExPVPMatchCCRecord extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x89);
		// TODO dd[Sd]
	}
}