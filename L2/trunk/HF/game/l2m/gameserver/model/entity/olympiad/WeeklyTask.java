package l2m.gameserver.model.entity.olympiad;

import java.util.Calendar;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeeklyTask extends RunnableImpl
{
  private static final Logger _log = LoggerFactory.getLogger(WeeklyTask.class);

  public void runImpl()
    throws Exception
  {
    Olympiad.doWeekTasks();
    _log.info("Olympiad System: Added weekly points to nobles");

    Calendar nextChange = Calendar.getInstance();
    Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
  }
}