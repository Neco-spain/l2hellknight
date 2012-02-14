package l2rt.gameserver.taskmanager.tasks;

import l2rt.Config;
import l2rt.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2rt.gameserver.taskmanager.Task;
import l2rt.gameserver.taskmanager.TaskManager;
import l2rt.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2rt.gameserver.taskmanager.TaskTypes;

import java.util.logging.Logger;

/**
 * Updates all data of Olympiad nobles in db
 *
 * @author godson
 */
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
		if(!Config.ENABLE_OLYMPIAD)
			return;
		try
		{
			OlympiadDatabase.save();
			_log.info("Olympiad System: Data updated successfully.");
		}
		catch(Exception e)
		{
			_log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
		}
	}

	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "0", "600000", "");
	}
}
