package l2p.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;

public abstract class Task
{
  public abstract void initializate();

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