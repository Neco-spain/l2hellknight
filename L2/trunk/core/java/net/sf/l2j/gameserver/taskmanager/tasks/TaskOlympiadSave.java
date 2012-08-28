//L2E
package net.sf.l2j.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskTypes;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;

public class TaskOlympiadSave extends Task
{
    private static final Logger _log = Logger.getLogger(TaskOlympiadSave.class.getName());
    public static final String NAME = "OlympiadSave";

    @Override
	public String getName()
    {
        return NAME;
    }

    @Override
	public void onTimeElapsed(ExecutedTask task)
    {
        try {
            Olympiad.getInstance().save();
            _log.info("Olympiad System: Data updated successfully.");
        }
        catch (Exception e) {
            _log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
        }
    }

    @Override
	public void initializate()
    {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
    }
}
