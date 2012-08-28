package net.sf.l2j.util;

import net.sf.l2j.gameserver.ThreadPoolManager;

public class RRDUpdater
{
	public class updateTask implements Runnable
	{
		public void run()
		{
			RRDTools.update();
		}
	}

	public class drawTask implements Runnable
	{
		public void run()
		{
			RRDTools.draw(" last hour", "1h", 3600);
			RRDTools.draw(" last day", "1d", 86400);
			RRDTools.draw(" last week", "1w", 604800);
			RRDTools.draw(" last month", "1m", 2419200); // 28 дней
			RRDTools.draw(" last year", "1y", 31536000); //365 дней
			RRDTools._drawtask = ThreadPoolManager.getInstance().scheduleGeneral(new RRDUpdater().new drawTask(), RRDTools.RRD_REDRAW_TIME);
		}
	}
}