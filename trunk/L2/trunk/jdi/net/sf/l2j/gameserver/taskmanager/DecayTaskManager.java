package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;

public class DecayTaskManager
{
  protected static final Logger _log = Logger.getLogger(DecayTaskManager.class.getName());
  protected Map<L2Character, Long> _decayTasks = new FastMap().setShared(true);
  private static DecayTaskManager _instance;

  public DecayTaskManager()
  {
    ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000L, 5000L);
  }

  public static DecayTaskManager getInstance()
  {
    if (_instance == null) {
      _instance = new DecayTaskManager();
    }
    return _instance;
  }

  public void addDecayTask(L2Character actor)
  {
    _decayTasks.put(actor, Long.valueOf(System.currentTimeMillis()));
  }

  public void addDecayTask(L2Character actor, int interval)
  {
    _decayTasks.put(actor, Long.valueOf(System.currentTimeMillis() + interval));
  }

  public void cancelDecayTask(L2Character actor)
  {
    try
    {
      _decayTasks.remove(actor);
    }
    catch (NoSuchElementException e)
    {
    }
  }

  public String toString()
  {
    String ret = "============= DecayTask Manager Report ============\r\n";
    ret = ret + "Tasks count: " + _decayTasks.size() + "\r\n";
    ret = ret + "Tasks dump:\r\n";

    Long current = Long.valueOf(System.currentTimeMillis());
    for (L2Character actor : _decayTasks.keySet())
    {
      ret = ret + "Class/Name: " + actor.getClass().getSimpleName() + "/" + actor.getName() + " decay timer: " + (current.longValue() - ((Long)_decayTasks.get(actor)).longValue()) + "\r\n";
    }

    return ret;
  }

  private class DecayScheduler
    implements Runnable
  {
    protected DecayScheduler()
    {
    }

    public void run()
    {
      Long current = Long.valueOf(System.currentTimeMillis());
      try
      {
        if (_decayTasks != null)
          for (L2Character actor : _decayTasks.keySet())
          {
            int delay;
            int delay;
            if (((actor instanceof L2RaidBossInstance)) && (!actor.isRaidMinion())) delay = 30000; else
              delay = 8500;
            if (current.longValue() - ((Long)_decayTasks.get(actor)).longValue() > delay)
            {
              actor.onDecay();
              _decayTasks.remove(actor);
            }
          }
      }
      catch (Throwable e) {
        DecayTaskManager._log.warning(e.toString());
      }
    }
  }
}