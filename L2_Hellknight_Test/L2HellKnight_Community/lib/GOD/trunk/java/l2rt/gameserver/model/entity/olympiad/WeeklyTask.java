package l2rt.gameserver.model.entity.olympiad;

import l2rt.Config;

import java.util.Calendar;

public class WeeklyTask implements Runnable
{
	@Override
	public void run()
	{
		Olympiad.addWeeklyPoints();
		Olympiad._log.info("Olympiad System: Added weekly points to nobles");

		Calendar nextChange = Calendar.getInstance();
		Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
	}
}