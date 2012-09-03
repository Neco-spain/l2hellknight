package l2rt.gameserver.network.serverpackets;

/**
 * Seven Signs Record Update
 *
 * packet type id 0xf5
 * format:
 *
 * c cc	(Page Num = 1 -> 4, period)
 *
 * 1: [ddd cc dd ddd c ddd c]
 * 2: [hc [cd (dc (S))]
 * 3: [ccc (cccc)]
 * 4: [(cchh)]
 */
public class SSQStatus extends L2GameServerPacket
{

	public void SSQStatus()
	{
		// Больше не используется
	}
	
	@Override
	protected final void writeImpl()
	{
		//writeC(0xfb);
	}
}