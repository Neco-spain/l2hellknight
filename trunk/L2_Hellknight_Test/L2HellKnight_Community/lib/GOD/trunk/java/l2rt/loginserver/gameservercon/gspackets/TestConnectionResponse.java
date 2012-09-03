package l2rt.loginserver.gameservercon.gspackets;

import l2rt.loginserver.gameservercon.AttGS;

public class TestConnectionResponse extends ClientBasePacket
{
	public TestConnectionResponse(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		//System.out.println("LS: response obtained, time " + (System.currentTimeMillis() - Watchdog.getLastTime()));
		getGameServer().notifyPingResponse();
	}
}