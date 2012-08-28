package l2p.gameserver.model.entity.olympiad;

import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.serverpackets.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadEndTask extends RunnableImpl
{
  private static final Logger _log = LoggerFactory.getLogger(OlympiadEndTask.class);

  public void runImpl()
    throws Exception
  {
    if (Olympiad._inCompPeriod)
    {
      ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), 60000L);
      return;
    }

    Announcements.getInstance().announceToAll(new SystemMessage(1640).addNumber(Olympiad._currentCycle));
    Announcements.getInstance().announceToAll("Olympiad Validation Period has began");

    Olympiad._isOlympiadEnd = true;
    if (Olympiad._scheduledManagerTask != null)
      Olympiad._scheduledManagerTask.cancel(false);
    if (Olympiad._scheduledWeeklyTask != null) {
      Olympiad._scheduledWeeklyTask.cancel(false);
    }
    Olympiad._validationEnd = Olympiad._olympiadEnd + Config.ALT_OLY_VPERIOD;

    OlympiadDatabase.saveNobleData();
    Olympiad._period = 1;
    Hero.getInstance().clearHeroes();
    try
    {
      OlympiadDatabase.save();
    }
    catch (Exception e)
    {
      _log.error("Olympiad System: Failed to save Olympiad configuration!", e);
    }

    _log.info("Olympiad System: Starting Validation period. Time to end validation:" + Olympiad.getMillisToValidationEnd() / 60000L);

    if (Olympiad._scheduledValdationTask != null)
      Olympiad._scheduledValdationTask.cancel(false);
    Olympiad._scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), Olympiad.getMillisToValidationEnd());
  }
}