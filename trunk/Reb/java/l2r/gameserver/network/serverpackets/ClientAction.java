package l2r.gameserver.network.serverpackets;

public class ClientAction extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0x8F);
		//TODO d
	}
}