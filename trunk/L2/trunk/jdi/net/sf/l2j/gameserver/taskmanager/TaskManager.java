package net.sf.l2j.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskCleanUp;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskJython;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskOlympiadSave;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskRecom;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskRestart;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskSevenSignsUpdate;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskShutdown;

public final class TaskManager
{
  protected static final Logger _log = Logger.getLogger(TaskManager.class.getName());
  private static TaskManager _instance;
  protected static final String[] SQL_STATEMENTS = { "SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks", "UPDATE global_tasks SET last_activation=? WHERE id=?", "SELECT id FROM global_tasks WHERE task=?", "INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)" };

  private final FastMap<Integer, Task> _tasks = new FastMap();
  protected final FastList<ExecutedTask> _currentTasks = new FastList();

  public static TaskManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new TaskManager();
    }
    return _instance;
  }

  public TaskManager()
  {
    initializate();
    startAllTasks();
  }

  private void initializate()
  {
    registerTask(new TaskCleanUp());
    registerTask(new TaskJython());
    registerTask(new TaskOlympiadSave());
    registerTask(new TaskRecom());
    registerTask(new TaskRestart());
    registerTask(new TaskSevenSignsUpdate());
    registerTask(new TaskShutdown());
  }

  public void registerTask(Task task)
  {
    int key = task.getName().hashCode();
    if (!_tasks.containsKey(Integer.valueOf(key)))
    {
      _tasks.put(Integer.valueOf(key), task);
      task.initializate();
    }
  }

  private void startAllTasks()
  {
    Connection con = null;
    try
    {
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[0]);
        ResultSet rset = statement.executeQuery();

        while (rset.next())
        {
          Task task = (Task)_tasks.get(Integer.valueOf(rset.getString("task").trim().toLowerCase().hashCode()));

          if (task == null)
            continue;
          TaskTypes type = TaskTypes.valueOf(rset.getString("type"));

          if (type != TaskTypes.TYPE_NONE)
          {
            ExecutedTask current = new ExecutedTask(task, type, rset);
            if (launchTask(current)) _currentTasks.add(current);
          }

        }

        rset.close();
        statement.close();
      }
      catch (Exception e)
      {
        _log.severe("error while loading Global Task table " + e);
        e.printStackTrace();
      }

    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  private boolean launchTask(ExecutedTask task)
  {
    ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
    TaskTypes type = task.getType();

    if (type == TaskTypes.TYPE_STARTUP)
    {
      task.run();
      return false;
    }
    if (type == TaskTypes.TYPE_SHEDULED)
    {
      long delay = Long.valueOf(task.getParams()[0]).longValue();
      task.scheduled = scheduler.scheduleGeneral(task, delay);
      return true;
    }
    if (type == TaskTypes.TYPE_FIXED_SHEDULED)
    {
      long delay = Long.valueOf(task.getParams()[0]).longValue();
      long interval = Long.valueOf(task.getParams()[1]).longValue();

      task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
      return true;
    }
    if (type == TaskTypes.TYPE_TIME)
    {
      try
      {
        Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
        long diff = desired.getTime() - System.currentTimeMillis();
        if (diff >= 0L)
        {
          task.scheduled = scheduler.scheduleGeneral(task, diff);
          return true;
        }
        _log.info("Task " + task.getId() + " is obsoleted.");
      }
      catch (Exception e)
      {
      }
    }
    else if (type == TaskTypes.TYPE_SPECIAL)
    {
      ScheduledFuture result = task.getTask().launchSpecial(task);
      if (result != null)
      {
        task.scheduled = result;
        return true;
      }
    } else {
      if (type == TaskTypes.TYPE_GLOBAL_TASK)
      {
        long interval = Long.valueOf(task.getParams()[0]).longValue() * 86400000L;
        String[] hour = task.getParams()[1].split(":");

        if (hour.length != 3)
        {
          _log.warning("Task " + task.getId() + " has incorrect parameters");
          return false;
        }

        Calendar check = Calendar.getInstance();
        check.setTimeInMillis(task.getLastActivation() + interval);

        Calendar min = Calendar.getInstance();
        try
        {
          min.set(11, Integer.valueOf(hour[0]).intValue());
          min.set(12, Integer.valueOf(hour[1]).intValue());
          min.set(13, Integer.valueOf(hour[2]).intValue());
        }
        catch (Exception e)
        {
          _log.warning("Bad parameter on task " + task.getId() + ": " + e.getMessage());
          return false;
        }

        long delay = min.getTimeInMillis() - System.currentTimeMillis();

        if ((check.after(min)) || (delay < 0L))
        {
          delay += interval;
        }

        task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);

        return true;
      }
      if (type == TaskTypes.TYPE_EVENT)
      {
        long l3 = Long.valueOf(task.getParams()[0]).longValue() * 86400000L;
        String[] as1 = task.getParams()[1].split(":");
        if (as1.length != 3)
        {
          _log.warning("Task " + task.getId() + " has incorrect parameters");
          return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(task.getLastActivation() + l3);
        Calendar calendar3 = Calendar.getInstance();
        try
        {
          calendar3.set(11, Integer.valueOf(as1[0]).intValue());
          calendar3.set(12, Integer.valueOf(as1[1]).intValue());
          calendar3.set(13, Integer.valueOf(as1[2]).intValue());
        }
        catch (Exception exception2)
        {
          _log.warning("Bad parameter on task " + task.getId() + ": " + exception2.getMessage());
          return false;
        }
        long l7 = calendar3.getTimeInMillis() - System.currentTimeMillis();
        if (l7 < 0L)
          l7 += l3;
        task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, l7, l3);
        return true;
      }
    }
    return false;
  }

  public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3)
  {
    return addUniqueTask(task, type, param1, param2, param3, 0L);
  }

  public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[2]);
      statement.setString(1, task);
      ResultSet rset = statement.executeQuery();

      if (!rset.next())
      {
        statement = con.prepareStatement(SQL_STATEMENTS[3]);
        statement.setString(1, task);
        statement.setString(2, type.toString());
        statement.setLong(3, lastActivation);
        statement.setString(4, param1);
        statement.setString(5, param2);
        statement.setString(6, param3);
        statement.execute();
      }

      rset.close();
      statement.close();

      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warning("cannot add the unique task: " + e.getMessage());
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }

    return false;
  }

  public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3)
  {
    return addTask(task, type, param1, param2, param3, 0L);
  }

  public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[3]);
      statement.setString(1, task);
      statement.setString(2, type.toString());
      statement.setLong(3, lastActivation);
      statement.setString(4, param1);
      statement.setString(5, param2);
      statement.setString(6, param3);
      statement.execute();

      statement.close();
      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warning("cannot add the task:  " + e.getMessage());
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }

    return false;
  }

  public class ExecutedTask
    implements Runnable
  {
    int id;
    long lastActivation;
    Task task;
    TaskTypes type;
    String[] params;
    ScheduledFuture scheduled;

    public ExecutedTask(Task ptask, TaskTypes ptype, ResultSet rset)
      throws SQLException
    {
      task = ptask;
      type = ptype;
      id = rset.getInt("id");
      lastActivation = rset.getLong("last_activation");
      params = new String[] { rset.getString("param1"), rset.getString("param2"), rset.getString("param3") };
    }

    public void run()
    {
      task.onTimeElapsed(this);

      lastActivation = System.currentTimeMillis();

      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[1]);
        statement.setLong(1, lastActivation);
        statement.setInt(2, id);
        statement.executeUpdate();
        statement.close();
      }
      catch (SQLException e)
      {
        TaskManager._log.warning("cannot updated the Global Task " + id + ": " + e.getMessage());
      }
      finally
      {
        try
        {
          con.close();
        }
        catch (Exception e)
        {
        }
      }

      if ((type == TaskTypes.TYPE_SHEDULED) || (type == TaskTypes.TYPE_TIME))
      {
        stopTask();
      }
    }

    public boolean equals(Object object)
    {
      return id == ((ExecutedTask)object).id;
    }

    public Task getTask()
    {
      return task;
    }

    public TaskTypes getType()
    {
      return type;
    }

    public int getId()
    {
      return id;
    }

    public String[] getParams()
    {
      return params;
    }

    public long getLastActivation()
    {
      return lastActivation;
    }

    public void stopTask()
    {
      task.onDestroy();

      if (scheduled != null) scheduled.cancel(true);

      _currentTasks.remove(this);
    }
  }
}