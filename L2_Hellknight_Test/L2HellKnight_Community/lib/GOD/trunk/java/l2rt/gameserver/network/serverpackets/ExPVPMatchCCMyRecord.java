package l2rt.gameserver.network.serverpackets;

public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x8A);
		writeD(0); // unk
	}
}