package net.sf.l2j.webserver;

import java.util.logging.Logger;

import net.sf.l2j.Config;

public class WebServer extends Thread
{
	protected static Logger _log = Logger.getLogger(WebServer.class.getName());
	WebServerThread wst = null;

	@Override
	public void run()
	{
		_log.fine("Starting WebServer at port " + Config.WEB_SERVER_PORT);
		try
		{
			if(wst == null)
				wst = new WebServerThread(Config.WEB_SERVER_PORT, Config.WEB_SERVER_ROOT);
			else
				Thread.dumpStack();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
