package l2m.gameserver.taskmanager.actionrunner.tasks;

import l2m.gameserver.model.entity.olympiad.OlympiadDatabase;
import org.apache.log4j.Logger;

public class OlympiadSaveTask extends AutomaticTask
{
  private static final Logger _log = Logger.getLogger(OlympiadSaveTask.class);

  public void doTask()
    throws Exception
  {
    long t = System.currentTimeMillis();

    OlympiadDatabase.save();
  }

  public long reCalcTime(boolean start)
  {
    return System.currentTimeMillis() + 600000L;
  }
}