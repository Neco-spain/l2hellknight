package l2rt.gameserver.network.serverpackets;

/**
 * Format: ch
 * Протокол 828: при отправке пакета клиенту ничего не происходит.
 */
public class ExPlayScene extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x5c);
		writeD(0x00); //Kamael
	}
}