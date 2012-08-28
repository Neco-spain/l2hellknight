package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

public final class TaskCleanUp extends Task
{
  public static final String NAME = "CleanUp";

  public String getName()
  {
    return "CleanUp";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    System.runFinalization();
    System.gc();
  }
}