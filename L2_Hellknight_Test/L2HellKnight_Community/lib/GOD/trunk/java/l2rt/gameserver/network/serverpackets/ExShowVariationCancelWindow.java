package l2rt.gameserver.network.serverpackets;

public class ExShowVariationCancelWindow extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x52);
	}
}