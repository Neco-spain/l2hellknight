package net.sf.l2j.gameserver;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastSet;
import net.sf.l2j.Config;
import net.sf.l2j.util.log.AbstractLogger;

public final class DeadlockDetector extends Thread
{
  private static final Logger _log = AbstractLogger.getLogger(DeadlockDetector.class.getName());

  private final ThreadMXBean _mbean = ManagementFactory.getThreadMXBean();
  private final Set<Long> _logged = new FastSet();
  private static DeadlockDetector _instance;

  public static DeadlockDetector getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new DeadlockDetector();
    _instance.setDaemon(true);
    _instance.setPriority(1);
    _instance.start();
  }

  public void run()
  {
    while (true)
      try
      {
        new Thread()
        {
          public void run()
          {
            try
            {
              DeadlockDetector.this.checkForDeadlocks();
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
          }
        }
        .run();
        try
        {
          Thread.sleep(Config.DEADLOCKCHECK_INTERVAL);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }

        continue;
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
  }

  private void checkForDeadlocks()
  {
    long[] ids = findDeadlockedThreadIDs();
    if (ids == null) {
      return;
    }
    List deadlocked = new FastList();

    for (long id : ids) {
      if (_logged.add(Long.valueOf(id)))
        deadlocked.add(findThreadById(id));
    }
    if (!deadlocked.isEmpty())
    {
      System.out.println("Deadlocked Thread(s)");
      for (Thread thread : deadlocked)
      {
        _log.warning("ERROR:" + thread);
        for (StackTraceElement trace : thread.getStackTrace()) {
          _log.warning("\tat " + trace);
        }
      }
      System.out.println("Kill deadlocked Thread(s)...");
      for (Thread thread : deadlocked)
        thread.interrupt();
      System.out.println("Done.");
    }
  }

  private long[] findDeadlockedThreadIDs()
  {
    if (_mbean.isSynchronizerUsageSupported())
      return _mbean.findDeadlockedThreads();
    return _mbean.findMonitorDeadlockedThreads();
  }

  private Thread findThreadById(long id)
  {
    for (Thread thread : Thread.getAllStackTraces().keySet())
      if (thread.getId() == id)
        return thread;
    throw new IllegalStateException("Deadlocked Thread not found!");
  }
}