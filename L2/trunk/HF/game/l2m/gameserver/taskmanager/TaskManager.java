package l2m.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.taskmanager.tasks.SoIStageUpdater;
import l2m.gameserver.taskmanager.tasks.TaskNevitSystem;
import l2m.gameserver.taskmanager.tasks.TaskRecom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskManager
{
  private static final Logger _log = LoggerFactory.getLogger(TaskManager.class);
  private static TaskManager _instance;
  static final String[] SQL_STATEMENTS = { "SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks", "UPDATE global_tasks SET last_activation=? WHERE id=?", "SELECT id FROM global_tasks WHERE task=?", "INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)" };

  private final Map<Integer, Task> _tasks = new ConcurrentHashMap();
  final List<ExecutedTask> _currentTasks = new ArrayList();

  public static TaskManager getInstance()
  {
    if (_instance == null)
      _instance = new TaskManager();
    return _instance;
  }

  public TaskManager()
  {
    initializate();
    startAllTasks();
  }

  private void initializate()
  {
    registerTask(new TaskRecom());
    registerTask(new TaskNevitSystem());
    registerTask(new SoIStageUpdater());
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
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement(SQL_STATEMENTS[0]);
      rset = statement.executeQuery();
      while (rset.next())
      {
        Task task = (Task)_tasks.get(Integer.valueOf(rset.getString("task").trim().toLowerCase().hashCode()));

        if (task == null) {
          continue;
        }
        TaskTypes type = TaskTypes.valueOf(rset.getString("type"));

        if (type != TaskTypes.TYPE_NONE)
        {
          ExecutedTask current = new ExecutedTask(task, type, rset);
          if (launchTask(current)) {
            _currentTasks.add(current);
          }
        }
      }
    }
    catch (Exception e)
    {
      _log.error("error while loading Global Task table " + e);
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
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
      task._scheduled = scheduler.schedule(task, delay);
      return true;
    }
    if (type == TaskTypes.TYPE_FIXED_SHEDULED)
    {
      long delay = Long.valueOf(task.getParams()[0]).longValue();
      long interval = Long.valueOf(task.getParams()[1]).longValue();

      task._scheduled = scheduler.scheduleAtFixedRate(task, delay, interval);
      return true;
    }
    if (type == TaskTypes.TYPE_TIME)
    {
      try {
        Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
        long diff = desired.getTime() - System.currentTimeMillis();
        if (diff >= 0L)
        {
          task._scheduled = scheduler.schedule(task, diff);
          return true;
        }
        _log.info("Task " + task.getId() + " is obsoleted.");
      } catch (Exception e) {
      }
    }
    else if (type == TaskTypes.TYPE_SPECIAL)
    {
      ScheduledFuture result = task.getTask().launchSpecial(task);
      if (result != null)
      {
        task._scheduled = result;
        return true;
      }
    }
    else if (type == TaskTypes.TYPE_GLOBAL_TASK)
    {
      long interval = Long.valueOf(task.getParams()[0]).longValue() * 86400000L;
      String[] hour = task.getParams()[1].split(":");

      if (hour.length != 3)
      {
        _log.warn("Task " + task.getId() + " has incorrect parameters");
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
        _log.warn("Bad parameter on task " + task.getId() + ": " + e.getMessage());
        return false;
      }

      long delay = min.getTimeInMillis() - System.currentTimeMillis();

      if ((check.after(min)) || (delay < 0L)) {
        delay += interval;
      }
      task._scheduled = scheduler.scheduleAtFixedRate(task, delay, interval);

      return true;
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
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement(SQL_STATEMENTS[2]);
      statement.setString(1, task);
      rset = statement.executeQuery();
      boolean exists = rset.next();
      DbUtils.close(statement, rset);
      if (!exists)
      {
        statement = con.prepareStatement(SQL_STATEMENTS[3]);
        statement.setString(1, task);
        statement.setString(2, type.toString());
        statement.setLong(3, lastActivation / 1000L);
        statement.setString(4, param1);
        statement.setString(5, param2);
        statement.setString(6, param3);
        statement.execute();
      }
      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warn("cannot add the unique task: " + e.getMessage());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
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
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement(SQL_STATEMENTS[3]);
      statement.setString(1, task);
      statement.setString(2, type.toString());
      statement.setLong(3, lastActivation / 1000L);
      statement.setString(4, param1);
      statement.setString(5, param2);
      statement.setString(6, param3);
      statement.execute();
      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warn("cannot add the task:\t" + e.getMessage());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    return false;
  }

  public class ExecutedTask extends RunnableImpl
  {
    int _id;
    long _lastActivation;
    Task _task;
    TaskTypes _type;
    String[] _params;
    ScheduledFuture<?> _scheduled;

    public ExecutedTask(Task task, TaskTypes type, ResultSet rset)
      throws SQLException
    {
      _task = task;
      _type = type;
      _id = rset.getInt("id");
      _lastActivation = (rset.getLong("last_activation") * 1000L);
      _params = new String[] { rset.getString("param1"), rset.getString("param2"), rset.getString("param3") };
    }

    public void runImpl()
      throws Exception
    {
      _task.onTimeElapsed(this);

      _lastActivation = System.currentTimeMillis();

      Connection con = null;
      PreparedStatement statement = null;
      try
      {
        con = DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[1]);
        statement.setLong(1, _lastActivation / 1000L);
        statement.setInt(2, _id);
        statement.executeUpdate();
      }
      catch (SQLException e)
      {
        _log.warn("cannot updated the Global Task " + _id + ": " + e.getMessage());
      }
      finally
      {
        DbUtils.closeQuietly(con, statement);
      }

      if ((_type == TaskTypes.TYPE_SHEDULED) || (_type == TaskTypes.TYPE_TIME))
        stopTask();
    }

    public boolean equals(Object object)
    {
      return _id == ((ExecutedTask)object)._id;
    }

    public Task getTask()
    {
      return _task;
    }

    public TaskTypes getType()
    {
      return _type;
    }

    public int getId()
    {
      return _id;
    }

    public String[] getParams()
    {
      return _params;
    }

    public long getLastActivation()
    {
      return _lastActivation;
    }

    public void stopTask()
    {
      _task.onDestroy();

      if (_scheduled != null) {
        _scheduled.cancel(false);
      }
      _currentTasks.remove(this);
    }
  }
}