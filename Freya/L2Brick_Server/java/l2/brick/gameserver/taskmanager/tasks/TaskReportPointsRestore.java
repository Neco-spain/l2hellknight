package l2.brick.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.taskmanager.Task;
import l2.brick.gameserver.taskmanager.TaskManager;
import l2.brick.gameserver.taskmanager.TaskTypes;
import l2.brick.gameserver.taskmanager.TaskManager.ExecutedTask;

public class TaskReportPointsRestore extends Task
{
	private static final String NAME = "report_points_restore";
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}

	/* (non-Javadoc)
	 * @see l2.brick.gameserver.taskmanager.Task#onTimeElapsed(l2.brick.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement update = con.prepareStatement("UPDATE characters SET bot_report_points = 7");
			update.execute();
			update.close();
			System.out.println("Sucessfully restored Bot Report Points for all Characters!");
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch(Exception e) { e.printStackTrace(); }
		}
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.taskmanager.Task#initializate()
	 */
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:00:00", "");
	}
}