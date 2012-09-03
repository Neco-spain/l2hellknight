package l2rt.gameserver.network.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x0F);
		// TODO cS
	}
}