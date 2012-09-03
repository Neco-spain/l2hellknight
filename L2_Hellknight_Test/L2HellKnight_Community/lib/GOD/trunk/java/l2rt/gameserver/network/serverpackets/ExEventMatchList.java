package l2rt.gameserver.network.serverpackets;

public class ExEventMatchList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x0D);
		// TODO пока не реализован даже в коиенте
	}
}