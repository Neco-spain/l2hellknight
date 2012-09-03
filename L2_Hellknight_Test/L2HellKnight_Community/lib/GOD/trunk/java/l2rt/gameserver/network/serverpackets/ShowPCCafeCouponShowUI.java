package l2rt.gameserver.network.serverpackets;

/**
 * Даный пакет показывает менюшку для ввода серийника. Можно что-то придумать :)
 * Format: (ch)
 */
public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x44);
	}
}