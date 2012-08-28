package l2p.gameserver.taskmanager.tasks;

import java.util.Calendar;
import l2p.gameserver.instancemanager.SoIManager;
import l2p.gameserver.taskmanager.Task;
import l2p.gameserver.taskmanager.TaskManager;
import l2p.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2p.gameserver.taskmanager.TaskTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoIStageUpdater extends Task
{
  private static final Logger _log = LoggerFactory.getLogger(SoIStageUpdater.class);
  private static final String NAME = "soi_update";

  public void initializate()
  {
    TaskManager.addUniqueTask(getName(), TaskTypes.TYPE_GLOBAL_TASK, "1", "12:00:00", "");
  }

  public String getName()
  {
    return "soi_update";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    if (Calendar.getInstance().get(7) == 2)
    {
      SoIManager.setCurrentStage(1);
      _log.info("Seed of Infinity update Task: Seed updated successfuly.");
    }
  }
}