package l2rt.gameserver.network.serverpackets;

public class ExPVPMatchCCRetire extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x8B);
		// just trigger
	}
}