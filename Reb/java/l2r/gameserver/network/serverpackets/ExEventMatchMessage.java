package l2r.gameserver.network.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x0F);
		// TODO cS
	}
}