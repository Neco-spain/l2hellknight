package l2rt.loginserver.gameservercon;

import l2rt.loginserver.gameservercon.lspackets.RSAKey;

/**
 * @Author: Death
 * @Date: 12/11/2007
 * @Time: 20:13:42
 */
public class KeyTask extends Thread
{
	private final AttGS gameserver;

	public KeyTask(AttGS gameserver)
	{
		this.gameserver = gameserver;
	}

	@Override
	public void run()
	{
		try
		{
			gameserver.setRSA(new RSACrypt());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		gameserver.sendPacket(new RSAKey(gameserver.getRSAPublicKey()));
	}
}
