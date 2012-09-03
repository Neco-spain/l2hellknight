package l2rt.gameserver.network.serverpackets;

public class ExNotifyPremiumItem extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x85);
		// just trigger
	}
}