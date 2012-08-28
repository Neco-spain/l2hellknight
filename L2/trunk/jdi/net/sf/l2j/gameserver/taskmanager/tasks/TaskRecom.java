package net.sf.l2j.gameserver.taskmanager.tasks;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;
import net.sf.l2j.gameserver.taskmanager.TaskTypes;

public class TaskRecom extends Task
{
  private static final Logger _log = Logger.getLogger(TaskRecom.class.getName());
  private static final String NAME = "sp_recommendations";

  public String getName()
  {
    return "sp_recommendations";
  }

  public void onTimeElapsed(TaskManager.ExecutedTask task)
  {
    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
    {
      player.restartRecom();
      player.sendPacket(new UserInfo(player));
    }
    _log.config("Recommendation Global Task: launched.");
  }

  public void initializate()
  {
    super.initializate();
    TaskManager.addUniqueTask("sp_recommendations", TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
  }
}