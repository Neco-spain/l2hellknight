package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;
import org.python.util.PythonInterpreter;

public class TaskJython extends Task
{
  public static final String NAME = "jython";
  private final PythonInterpreter _python = new PythonInterpreter();

  public String getName()
  {
    return "jython";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    _python.cleanup();
    _python.exec("import sys");
    _python.execfile("data/jscript/cron/" + task.getParams()[2]);
  }
}