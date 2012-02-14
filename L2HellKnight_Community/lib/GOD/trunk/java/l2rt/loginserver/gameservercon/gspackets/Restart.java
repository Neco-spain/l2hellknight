package l2rt.loginserver.gameservercon.gspackets;

import l2rt.Server;
import l2rt.loginserver.gameservercon.AttGS;

public class Restart extends ClientBasePacket
{
	public Restart(byte[] decrypt, AttGS gameserver)
	{
		super(decrypt, gameserver);
	}

	@Override
	public void read()
	{
		System.out.println("Recived full restart command from: " + getGameServer());
		Server.exit(2, "Recived full restart command from: " + getGameServer());
		// Полный рестарт логинсервера.
	}
}