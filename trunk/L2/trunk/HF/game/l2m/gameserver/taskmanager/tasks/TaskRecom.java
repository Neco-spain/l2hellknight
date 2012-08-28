package l2m.gameserver.taskmanager.tasks;

import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.taskmanager.Task;
import l2m.gameserver.taskmanager.TaskManager;
import l2m.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2m.gameserver.taskmanager.TaskTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRecom extends Task
{
  private static final Logger _log = LoggerFactory.getLogger(TaskRecom.class);
  private static final String NAME = "sp_recommendations";

  public String getName()
  {
    return "sp_recommendations";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    _log.info("Recommendation Global Task: launched.");
    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
      player.restartRecom();
    _log.info("Recommendation Global Task: completed.");
  }

  public void initializate()
  {
    TaskManager.addUniqueTask("sp_recommendations", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
  }
}