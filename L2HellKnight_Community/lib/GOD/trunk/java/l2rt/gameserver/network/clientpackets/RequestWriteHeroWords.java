package l2rt.gameserver.network.clientpackets;

/**
 * Format chS
 * c (id) 0xD0
 * h (subid) 0x05
 * S the hero's words :)
 */
public class RequestWriteHeroWords extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String _heroWords;

	@Override
	public void readImpl()
	{
		_heroWords = readS();
	}

	@Override
	public void runImpl()
	{}
}