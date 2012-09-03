package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.SSQStatus;

/**
 * Seven Signs Record Update Request
 * packet type id 0xc8
 * format: cc
 */
public class RequestSSQStatus extends L2GameClientPacket
{
	//private int _page;

	@Override
	public void readImpl()
	{
		/*_page =*/ readC();
		
	}

	@Override
	public void runImpl()
	{
		// Больше не используется
		//L2Player activeChar = getClient().getActiveChar();
		//if(activeChar == null)
			return;

		//sendPacket(new SSQStatus());
	}
}