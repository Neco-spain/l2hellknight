package l2rt.gameserver.network.serverpackets;

public class ExEventMatchLockResult extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x0B);
		// TODO пока не реализован даже в клиенте
	}
}