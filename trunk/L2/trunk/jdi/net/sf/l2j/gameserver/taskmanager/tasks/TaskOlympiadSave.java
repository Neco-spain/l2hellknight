package net.sf.l2j.gameserver.taskmanager.tasks;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;
import net.sf.l2j.gameserver.taskmanager.TaskTypes;

public class TaskOlympiadSave extends Task
{
  private static final Logger _log = Logger.getLogger(TaskOlympiadSave.class.getName());
  public static final String NAME = "OlympiadSave";

  public String getName()
  {
    return "OlympiadSave";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    try
    {
      Olympiad.getInstance().save();
      _log.info("Olympiad System: Data updated successfully.");
    }
    catch (Exception e) {
      _log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
    }
  }

  public void initializate()
  {
    super.initializate();
    TaskManager.addUniqueTask("OlympiadSave", TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
  }
}