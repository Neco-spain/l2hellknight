package l2rt.loginserver;

import l2rt.Config;
import l2rt.Server;
import l2rt.loginserver.gameservercon.AttGS;
import l2rt.loginserver.gameservercon.GameServerInfo;

import java.util.logging.Logger;

public class Watchdog extends Thread
{
	private static volatile boolean _inited = false;
	private static Logger _log = Logger.getLogger(Watchdog.class.getName());

	public static void init()
	{
		if(!_inited && !Config.COMBO_MODE)
		{
			_inited = true;
			new Watchdog().start();
			_log.info("Login watchdog thread started");
		}
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep(30000); // начинаем не сразу
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}

		AttGS gs;

		while(true)
		{
			for(GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
				if((gs = gsi.getGameServer()) != null && gs.isAuthed() && gs.isPingTimedOut())
					Server.exit(2, "Watchdog: server " + gsi.getId() + " not responding, issuing shutdown...");

			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}