package net.sf.l2j.gameserver;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastList;

public final class DeadlockDetector extends Thread
{
  private final Logger _log = Logger.getLogger(DeadlockDetector.class.getName());
  private final ThreadMXBean _mbean = ManagementFactory.getThreadMXBean();

  private static final List<Long> all_locked_ids = new FastList();
  private long[] locked_ids;
  private ThreadInfo[] locked_infos;
  private Thread[] locked_threads;

  public DeadlockDetector()
  {
    setDaemon(true);
    setPriority(1);
  }

  public void run()
  {
    while (true)
    {
      try
      {
        if (!checkForDeadlocks())
          continue;
        Thread.sleep(1000L);
        continue;
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      try
      {
        Thread.sleep(5000L);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }

  private boolean checkForDeadlocks()
  {
    if ((this.locked_ids = _mbean.findDeadlockedThreads()) == null) {
      return false;
    }
    locked_infos = _mbean.getThreadInfo(locked_ids, true, true);
    locked_threads = getThreads(locked_infos);

    int locks_count = logDeadlocks();
    fixDeadlocks();
    return locks_count > 0;
  }

  private void fixDeadlocks()
  {
    if ((locked_threads == null) || (locked_threads.length == 0))
      return;
  }

  private int logDeadlocks()
  {
    int ret = 0;

    String logstr = String.format("Deadlock detected [total %d / %d locked threads]...\r\n", new Object[] { Integer.valueOf(locked_infos.length), Integer.valueOf(locked_ids.length) });

    for (int i = 0; i < locked_infos.length; i++)
    {
      ThreadInfo ti = locked_infos[i];
      if (all_locked_ids.contains(Long.valueOf(ti.getThreadId()))) {
        continue;
      }
      all_locked_ids.add(Long.valueOf(ti.getThreadId()));
      ret++;
      logstr = logstr + "=========== DEADLOCK # " + i + " ============\r\n";
      do
      {
        logstr = logstr + formatLockThreadInfo(ti);
        ti = _mbean.getThreadInfo(new long[] { ti.getLockOwnerId() }, true, true)[0];
      }while (ti.getThreadId() != locked_infos[i].getThreadId());
    }

    _log.warning(logstr);
    return ret;
  }

  private String formatLockThreadInfo(ThreadInfo t)
  {
    String ret = String.format("\t\t%s is waiting to lock %s which is held by %s\r\n", new Object[] { t.getThreadName(), t.getLockInfo().toString(), t.getLockOwnerName() });
    for (StackTraceElement trace : t.getStackTrace())
      ret = ret + String.format("\t\t\t at %s\r\n", new Object[] { trace.toString() });
    return ret;
  }

  private static Thread[] getThreads(ThreadInfo[] infos)
  {
    Thread[] result = new Thread[infos.length];
    Set all = Thread.getAllStackTraces().keySet();
    long id;
    for (int i = 0; i < infos.length; i++)
    {
      result[i] = null;
      id = infos[i].getThreadId();
      for (Thread thread : all)
        if (thread.getId() == id)
        {
          result[i] = thread;
          break;
        }
    }
    return result;
  }
}