package l2p.gameserver.taskmanager.tasks;

import l2p.gameserver.Config;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExVitalityPointInfo;
import l2p.gameserver.taskmanager.Task;
import l2p.gameserver.taskmanager.TaskManager;
import l2p.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2p.gameserver.taskmanager.TaskTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class TaskVitalitySystem extends Task {
    private static final Logger _log = LoggerFactory.getLogger(TaskVitalitySystem.class);
    private static final String NAME = "sp_vitalitysystem";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onTimeElapsed(ExecutedTask task) {
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            _log.info("Vitality System Global Task: launched.");
            for (Player player : GameObjectsStorage.getAllPlayersForIterate()) {
                player.setVitality(Config.MAX_VITALITY);
                player.sendPacket(new ExVitalityPointInfo(player.getVitality()));
            }
            _log.info("Vitality System Task: completed.");
        }
    }

    @Override
    public void initializate() {
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
    }
}
