package l2.hellknight.gameserver.taskmanager.tasks;

import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.taskmanager.Task;
import l2.hellknight.gameserver.taskmanager.TaskManager;
import l2.hellknight.gameserver.taskmanager.TaskTypes;
import l2.hellknight.gameserver.taskmanager.TaskManager.ExecutedTask;

public final class TaskHellboundSave extends Task
{
	public static final String NAME = "hellbound_save";

	/**
	 * 
	 * @see l2.hellknight.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}

	/**
	 * 
	 * @see l2.hellknight.gameserver.taskmanager.Task#onTimeElapsed(l2.hellknight.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		HellboundManager.getInstance().saveVars();
	}

	/**
	 * 
	 * @see l2.hellknight.gameserver.taskmanager.Task#initializate()
	 */
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "500000", "1800000", "");
	}
}