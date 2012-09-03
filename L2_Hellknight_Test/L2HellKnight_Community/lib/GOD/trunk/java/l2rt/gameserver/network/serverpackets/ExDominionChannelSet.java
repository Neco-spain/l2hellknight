package l2rt.gameserver.network.serverpackets;

public class ExDominionChannelSet extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x96);
		writeD(0); // unk
	}
}