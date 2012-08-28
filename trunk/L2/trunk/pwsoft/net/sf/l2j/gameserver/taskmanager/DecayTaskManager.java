package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.ai.QueenAnt;

public class DecayTaskManager
{
  protected static final Logger _log = AbstractLogger.getLogger(DecayTaskManager.class.getName());
  protected Map<L2Character, Long> _decayTasks = new FastMap().shared("DecayTaskManager._decayTasks");
  private static DecayTaskManager _instance;

  public static void init()
  {
    _instance = new DecayTaskManager();
    _instance.load();
  }

  private void load()
  {
    ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000L, 5000L);
  }

  public static DecayTaskManager getInstance()
  {
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
    StringBuffer ret = new StringBuffer("============= DecayTask Manager Report ============\r\n");
    ret.append("Tasks count: " + _decayTasks.size() + "\r\n");
    ret.append("Tasks dump:\r\n");

    Long current = Long.valueOf(System.currentTimeMillis());
    for (L2Character actor : _decayTasks.keySet())
    {
      ret.append("Class/Name: " + actor.getClass().getSimpleName() + "/" + actor.getName() + " decay timer: " + (current.longValue() - ((Long)_decayTasks.get(actor)).longValue()) + "\r\n");
    }

    return ret.toString();
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
            if ((actor instanceof QueenAnt))
              delay = 300000;
            else
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