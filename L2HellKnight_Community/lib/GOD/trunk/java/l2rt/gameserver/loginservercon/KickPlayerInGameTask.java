package l2rt.gameserver.loginservercon;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.L2GameClient;

/**
 * @Author: Death
 * @Date: 13/11/2007
 * @Time: 20:46:51
 */
public class KickPlayerInGameTask implements Runnable
{
	private final L2GameClient client;

	public KickPlayerInGameTask(L2GameClient client)
	{
		this.client = client;
	}

	public void run()
	{
		L2Player activeChar = client.getActiveChar();

		if(activeChar != null)
			activeChar.logout(false, false, true, false);
		else
		{
			client.sendPacket(Msg.ServerClose);
			client.closeNow(false);
		}
	}
}
