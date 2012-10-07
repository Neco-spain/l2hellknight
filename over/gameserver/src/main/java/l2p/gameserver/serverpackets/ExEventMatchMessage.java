package l2p.gameserver.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x0F);
		// TODO cS
	}
}