package l2rt.gameserver.model.entity.olympiad;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class OlympiadEndTask implements Runnable
{
	@Override
	public void run()
	{
		if(Olympiad._inCompPeriod) // Если бои еще не закончились, откладываем окончание олимпиады на минуту
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), 60000);
			return;
		}

		Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_ENDED).addNumber(Olympiad._currentCycle));
		Announcements.getInstance().announceToAll("Olympiad Validation Period has began");

		Olympiad._isOlympiadEnd = true;
		if(Olympiad._scheduledManagerTask != null)
			Olympiad._scheduledManagerTask.cancel(true);
		if(Olympiad._scheduledWeeklyTask != null)
			Olympiad._scheduledWeeklyTask.cancel(true);

		Olympiad._validationEnd = Olympiad._olympiadEnd + Config.ALT_OLY_VPERIOD;

		OlympiadDatabase.saveNobleData();
		Olympiad._period = 1;
		Hero.getInstance().clearHeroes();

		try
		{
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			Olympiad._log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
		}

		Olympiad._log.warning("Olympiad System: Starting Validation period. Time to end validation:" + Olympiad.getMillisToValidationEnd() / (60 * 1000));

		if(Olympiad._scheduledValdationTask != null)
			Olympiad._scheduledValdationTask.cancel(true);
		Olympiad._scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationTask(), Olympiad.getMillisToValidationEnd());
	}
}