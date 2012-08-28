package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

public final class TaskRestart extends Task
{
  public static final String NAME = "restart";

  public String getName()
  {
    return "restart";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    Shutdown handler = new Shutdown(Integer.valueOf(task.getParams()[2]).intValue(), true);
    handler.start();
  }
}