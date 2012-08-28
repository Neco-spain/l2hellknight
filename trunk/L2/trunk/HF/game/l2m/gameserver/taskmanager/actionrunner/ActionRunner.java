package l2m.gameserver.taskmanager.actionrunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.commons.logging.LoggerObject;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.taskmanager.actionrunner.tasks.AutomaticTask;
import l2m.gameserver.taskmanager.actionrunner.tasks.DeleteExpiredMailTask;
import l2m.gameserver.taskmanager.actionrunner.tasks.DeleteExpiredVarsTask;
import l2m.gameserver.taskmanager.actionrunner.tasks.OlympiadSaveTask;

public class ActionRunner extends LoggerObject
{
  private static ActionRunner _instance = new ActionRunner();

  private Map<String, List<ActionWrapper>> _futures = new HashMap();
  private final Lock _lock = new ReentrantLock();

  public static ActionRunner getInstance()
  {
    return _instance;
  }

  private ActionRunner()
  {
    if (Config.ENABLE_OLYMPIAD)
      register(new OlympiadSaveTask());
    register(new DeleteExpiredVarsTask());
    register(new DeleteExpiredMailTask());
  }

  public void register(AutomaticTask task)
  {
    register(task.reCalcTime(true), task);
  }

  public void register(long time, ActionWrapper wrapper)
  {
    if (time == 0L)
    {
      info("Try register " + wrapper.getName() + " not defined time.");
      return;
    }

    if (time <= System.currentTimeMillis())
    {
      ThreadPoolManager.getInstance().execute(wrapper);
      return;
    }

    addScheduled(wrapper.getName(), wrapper, time - System.currentTimeMillis());
  }

  protected void addScheduled(String name, ActionWrapper r, long diff)
  {
    _lock.lock();
    try
    {
      String lower = name.toLowerCase();

      List wrapperList = (List)_futures.get(lower);
      if (wrapperList == null) {
        _futures.put(lower, wrapperList = new ArrayList());
      }
      r.schedule(diff);

      wrapperList.add(r);
    }
    finally
    {
      _lock.unlock();
    }
  }

  protected void remove(String name, ActionWrapper f)
  {
    _lock.lock();
    try
    {
      String lower = name.toLowerCase();
      List wrapperList = (List)_futures.get(lower);
      if (wrapperList == null)
        return;
      wrapperList.remove(f);

      if (wrapperList.isEmpty())
        _futures.remove(lower);
    }
    finally
    {
      _lock.unlock();
    }
  }

  public void clear(String name)
  {
    _lock.lock();
    try
    {
      String lower = name.toLowerCase();
      List wrapperList = (List)_futures.remove(lower);
      if (wrapperList == null)
        return;
      for (ActionWrapper f : wrapperList) {
        f.cancel();
      }
      wrapperList.clear();
    }
    finally
    {
      _lock.unlock();
    }
  }

  public void info()
  {
    _lock.lock();
    try
    {
      for (Map.Entry entry : _futures.entrySet())
        info("Name: " + (String)entry.getKey() + "; size: " + ((List)entry.getValue()).size());
    }
    finally
    {
      _lock.unlock();
    }
  }
}