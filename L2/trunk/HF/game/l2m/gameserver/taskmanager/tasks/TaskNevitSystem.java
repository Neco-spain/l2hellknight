package l2m.gameserver.taskmanager.tasks;

import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.NevitSystem;
import l2m.gameserver.taskmanager.Task;
import l2m.gameserver.taskmanager.TaskManager;
import l2m.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2m.gameserver.taskmanager.TaskTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskNevitSystem extends Task
{
  private static final Logger _log = LoggerFactory.getLogger(TaskNevitSystem.class);
  private static final String NAME = "sp_navitsystem";

  public String getName()
  {
    return "sp_navitsystem";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    _log.info("Navit System Global Task: launched.");
    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
      player.getNevitSystem().restartSystem();
    _log.info("Navit System Task: completed.");
  }

  public void initializate()
  {
    TaskManager.addUniqueTask("sp_navitsystem", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
  }
}