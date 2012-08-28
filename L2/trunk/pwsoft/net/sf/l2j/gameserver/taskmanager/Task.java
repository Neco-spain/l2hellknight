package net.sf.l2j.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import net.sf.l2j.util.log.AbstractLogger;

public abstract class Task
{
  private static Logger _log = AbstractLogger.getLogger(Task.class.getName());

  public void initializate()
  {
  }

  public ScheduledFuture<?> launchSpecial(TaskManager.ExecutedTask instance)
  {
    return null;
  }

  public abstract String getName();

  public abstract void onTimeElapsed(TaskManager.ExecutedTask paramExecutedTask);

  public void onDestroy()
  {
  }
}