package net.sf.l2j.gameserver.taskmanager;

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
import net.sf.l2j.gameserver.taskmanager.tasks.TaskJython;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskRecom;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskRestart;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskSevenSignsUpdate;
import net.sf.l2j.gameserver.taskmanager.tasks.TaskShutdown;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public final class TaskManager
{
  protected static final Logger _log = AbstractLogger.getLogger(TaskManager.class.getName());
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
    registerTask(new TaskJython());
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
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement(SQL_STATEMENTS[0]);
        rs = st.executeQuery();
        while (rs.next())
        {
          Task task = (Task)_tasks.get(Integer.valueOf(rs.getString("task").trim().toLowerCase().hashCode()));

          if (task == null)
            continue;
          TaskTypes type = TaskTypes.valueOf(rs.getString("type"));

          if (type != TaskTypes.TYPE_NONE)
          {
            ExecutedTask current = new ExecutedTask(task, type, rs);
            if (launchTask(current)) _currentTasks.add(current);
          }
        }

        Close.SR(st, rs);
      }
      catch (Exception e)
      {
        _log.severe("error while loading Global Task table " + e);
        e.printStackTrace();
      }
    }
    finally
    {
      Close.CSR(con, st, rs);
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
        e.printStackTrace();
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
    }
    else if (type == TaskTypes.TYPE_GLOBAL_TASK)
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
    return false;
  }

  public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3)
  {
    return addUniqueTask(task, type, param1, param2, param3, 0L);
  }

  public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement(SQL_STATEMENTS[2]);
      st.setString(1, task);
      rs = st.executeQuery();

      if (!rs.next())
      {
        st = con.prepareStatement(SQL_STATEMENTS[3]);
        st.setString(1, task);
        st.setString(2, type.toString());
        st.setLong(3, lastActivation);
        st.setString(4, param1);
        st.setString(5, param2);
        st.setString(6, param3);
        st.execute();
      }
      Close.SR(st, rs);
      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warning("cannot add the unique task: " + e.getMessage());
    }
    finally
    {
      Close.CSR(con, st, rs);
    }

    return false;
  }

  public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3)
  {
    return addTask(task, type, param1, param2, param3, 0L);
  }

  public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
  {
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement(SQL_STATEMENTS[3]);
      st.setString(1, task);
      st.setString(2, type.toString());
      st.setLong(3, lastActivation);
      st.setString(4, param1);
      st.setString(5, param2);
      st.setString(6, param3);
      st.execute();
      int i = 1;
      return i;
    }
    catch (SQLException e)
    {
      _log.warning("cannot add the task:  " + e.getMessage());
    }
    finally
    {
      Close.CS(con, st);
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
    ScheduledFuture<?> scheduled;

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

      Connect con = null;
      PreparedStatement st = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        con.setTransactionIsolation(1);
        st = con.prepareStatement(TaskManager.SQL_STATEMENTS[1]);
        st.setLong(1, lastActivation);
        st.setInt(2, id);
        st.executeUpdate();
      }
      catch (SQLException e)
      {
        TaskManager._log.warning("cannot updated the Global Task " + id + ": " + e.getMessage());
      }
      finally
      {
        Close.CS(con, st);
      }

      if ((type == TaskTypes.TYPE_SHEDULED) || (type == TaskTypes.TYPE_TIME))
      {
        stopTask();
      }
    }

    public boolean equals(Object object)
    {
      return this == object;
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