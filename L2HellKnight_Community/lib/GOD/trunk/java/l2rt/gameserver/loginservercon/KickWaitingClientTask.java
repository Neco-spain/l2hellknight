package l2rt.gameserver.loginservercon;

import l2rt.gameserver.network.L2GameClient;

/**
 * @Author: Death
 * @Date: 13/11/2007
 * @Time: 20:14:14
 */
public class KickWaitingClientTask implements Runnable
{
	private final L2GameClient client;

	public KickWaitingClientTask(L2GameClient client)
	{
		this.client = client;
	}

	public void run()
	{
		client.closeNow(false);
	}
}
