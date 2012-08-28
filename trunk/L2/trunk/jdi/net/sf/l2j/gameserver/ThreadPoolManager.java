package net.sf.l2j.gameserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;
import org.mmocore.network.ReceivablePacket;

public class ThreadPoolManager
{
  protected static final Logger _log = Logger.getLogger(ThreadPoolManager.class.getName());
  private static ThreadPoolManager _instance;
  public ScheduledThreadPoolExecutor _effectsScheduledThreadPool;
  private ScheduledThreadPoolExecutor _generalScheduledThreadPool;
  private ThreadPoolExecutor _generalPacketsThreadPool;
  private ThreadPoolExecutor _ioPacketsThreadPool;
  private ThreadPoolExecutor _aiThreadPool;
  private ThreadPoolExecutor _generalThreadPool;
  private ScheduledThreadPoolExecutor _aiScheduledThreadPool;
  private static final long MAX_DELAY = 4611686018427L;
  private boolean _shutdown;

  public static ThreadPoolManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new ThreadPoolManager();
    }
    return _instance;
  }

  private ThreadPoolManager()
  {
    _generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GerenalSTPool", 5));
    _generalScheduledThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _generalScheduledThreadPool.allowCoreThreadTimeOut(true);

    _effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS, new PriorityThreadFactory("EffectsSTPool", 1));
    _effectsScheduledThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _effectsScheduledThreadPool.allowCoreThreadTimeOut(true);

    _ioPacketsThreadPool = new ThreadPoolExecutor(Config.IO_PACKET_THREAD_CORE_SIZE, 2147483647, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("I/O Packet Pool", 8));

    _ioPacketsThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _ioPacketsThreadPool.allowCoreThreadTimeOut(true);

    _generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE, Config.GENERAL_PACKET_THREAD_CORE_SIZE * 2, 120L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("Normal Packet Pool", 7));

    _generalPacketsThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _generalPacketsThreadPool.allowCoreThreadTimeOut(true);

    _generalThreadPool = new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE + 2, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("General Pool", 10));

    _generalThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _generalThreadPool.allowCoreThreadTimeOut(true);

    _aiThreadPool = new ThreadPoolExecutor(1, Config.AI_MAX_THREAD, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue());

    _aiThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _aiThreadPool.allowCoreThreadTimeOut(true);

    _aiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.AI_MAX_THREAD, new PriorityThreadFactory("AISTPool", 6));
    _aiScheduledThreadPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
    _aiScheduledThreadPool.allowCoreThreadTimeOut(true);

    scheduleGeneralAtFixedRate(new PurgeTask(), 600000L, 300000L);
  }

  public static long validateDelay(long delay)
  {
    if (delay < 0L)
    {
      delay = 1L;
    }
    else if (delay > 4611686018427L)
    {
      delay = 4611686018427L;
    }
    return delay;
  }

  public ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
  {
    try
    {
      delay = validateDelay(delay);
      return _effectsScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
    }
    catch (RejectedExecutionException e) {
      if (!isShutdown())
      {
        _log.warning("EffectThreadPool: Failed schedule task!");
        Thread.dumpStack();
      }
    }
    return null;
  }

  public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable r, long initial, long delay)
  {
    try
    {
      delay = validateDelay(delay);
      initial = validateDelay(initial);
      return _effectsScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
    }
    catch (RejectedExecutionException e)
    {
      if (!isShutdown())
      {
        _log.warning("EffectThreadPool: Failed schedule task at fixed rate!");
        Thread.dumpStack();
      }
    }
    return null;
  }

  public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
  {
    try
    {
      delay = validateDelay(delay);
      return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
    }
    catch (RejectedExecutionException e)
    {
      if (!isShutdown())
      {
        _log.warning("GeneralThreadPool: Failed schedule task!");
        Thread.dumpStack();
      }
    }
    return null;
  }

  public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable r, long initial, long delay)
  {
    try
    {
      delay = validateDelay(delay);
      initial = validateDelay(initial);
      return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
    }
    catch (RejectedExecutionException e)
    {
      if (!isShutdown())
      {
        _log.warning("GeneralThreadPool: Failed schedule task at fixed rate!");
        Thread.dumpStack();
      }
    }
    return null;
  }

  public ScheduledFuture<?> scheduleAi(Runnable r, long delay)
  {
    try
    {
      delay = validateDelay(delay);
      return _aiScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
    }
    catch (RejectedExecutionException e)
    {
      if (!isShutdown())
      {
        _log.warning("GeneralThreadPool: Failed schedule task at fixed rate!");
        Thread.dumpStack();
      }
    }
    return null;
  }

  public ScheduledFuture<?> scheduleAiAtFixedRate(Runnable r, long initial, long delay)
  {
    try
    {
      delay = validateDelay(delay);
      initial = validateDelay(initial);
      return _aiScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
    }
    catch (RejectedExecutionException e)
    {
      if (!isShutdown())
      {
        _log.warning("GeneralThreadPool: Failed schedule task at fixed rate!");
        Thread.dumpStack();
      }
    }
    return null;
  }

  public void executePacket(ReceivablePacket<L2GameClient> pkt)
  {
    _generalPacketsThreadPool.execute(pkt);
  }

  public void executeIOPacket(ReceivablePacket<L2GameClient> pkt)
  {
    _ioPacketsThreadPool.execute(pkt);
  }

  public void executeTask(Runnable r)
  {
    _generalThreadPool.execute(r);
  }

  public void executeAi(Runnable r)
  {
    _aiThreadPool.execute(r);
  }

  public String[] getStats()
  {
    return new String[] { "STP:", " + Effects:", " |- ActiveThreads:   " + _effectsScheduledThreadPool.getActiveCount(), " |- getCorePoolSize: " + _effectsScheduledThreadPool.getCorePoolSize(), " |- PoolSize:        " + _effectsScheduledThreadPool.getPoolSize(), " |- MaximumPoolSize: " + _effectsScheduledThreadPool.getMaximumPoolSize(), " |- CompletedTasks:  " + _effectsScheduledThreadPool.getCompletedTaskCount(), " |- ScheduledTasks:  " + (_effectsScheduledThreadPool.getTaskCount() - _effectsScheduledThreadPool.getCompletedTaskCount()), " | -------", " + General:", " |- ActiveThreads:   " + _generalScheduledThreadPool.getActiveCount(), " |- getCorePoolSize: " + _generalScheduledThreadPool.getCorePoolSize(), " |- PoolSize:        " + _generalScheduledThreadPool.getPoolSize(), " |- MaximumPoolSize: " + _generalScheduledThreadPool.getMaximumPoolSize(), " |- CompletedTasks:  " + _generalScheduledThreadPool.getCompletedTaskCount(), " |- ScheduledTasks:  " + (_generalScheduledThreadPool.getTaskCount() - _generalScheduledThreadPool.getCompletedTaskCount()), " | -------", " + AI:", " |- ActiveThreads:   " + _aiScheduledThreadPool.getActiveCount(), " |- getCorePoolSize: " + _aiScheduledThreadPool.getCorePoolSize(), " |- PoolSize:        " + _aiScheduledThreadPool.getPoolSize(), " |- MaximumPoolSize: " + _aiScheduledThreadPool.getMaximumPoolSize(), " |- CompletedTasks:  " + _aiScheduledThreadPool.getCompletedTaskCount(), " |- ScheduledTasks:  " + (_aiScheduledThreadPool.getTaskCount() - _aiScheduledThreadPool.getCompletedTaskCount()), "TP:", " + Packets:", " |- ActiveThreads:   " + _generalPacketsThreadPool.getActiveCount(), " |- getCorePoolSize: " + _generalPacketsThreadPool.getCorePoolSize(), " |- MaximumPoolSize: " + _generalPacketsThreadPool.getMaximumPoolSize(), " |- LargestPoolSize: " + _generalPacketsThreadPool.getLargestPoolSize(), " |- PoolSize:        " + _generalPacketsThreadPool.getPoolSize(), " |- CompletedTasks:  " + _generalPacketsThreadPool.getCompletedTaskCount(), " |- QueuedTasks:     " + _generalPacketsThreadPool.getQueue().size(), " | -------", " + I/O Packets:", " |- ActiveThreads:   " + _ioPacketsThreadPool.getActiveCount(), " |- getCorePoolSize: " + _ioPacketsThreadPool.getCorePoolSize(), " |- MaximumPoolSize: " + _ioPacketsThreadPool.getMaximumPoolSize(), " |- LargestPoolSize: " + _ioPacketsThreadPool.getLargestPoolSize(), " |- PoolSize:        " + _ioPacketsThreadPool.getPoolSize(), " |- CompletedTasks:  " + _ioPacketsThreadPool.getCompletedTaskCount(), " |- QueuedTasks:     " + _ioPacketsThreadPool.getQueue().size(), " | -------", " + General Tasks:", " |- ActiveThreads:   " + _generalThreadPool.getActiveCount(), " |- getCorePoolSize: " + _generalThreadPool.getCorePoolSize(), " |- MaximumPoolSize: " + _generalThreadPool.getMaximumPoolSize(), " |- LargestPoolSize: " + _generalThreadPool.getLargestPoolSize(), " |- PoolSize:        " + _generalThreadPool.getPoolSize(), " |- CompletedTasks:  " + _generalThreadPool.getCompletedTaskCount(), " |- QueuedTasks:     " + _generalThreadPool.getQueue().size(), " | -------", " + AI:", " |- Not Done" };
  }

  public void shutdown()
  {
    _shutdown = true;
    try
    {
      _effectsScheduledThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _generalScheduledThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _generalPacketsThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _ioPacketsThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _generalThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _aiThreadPool.awaitTermination(1L, TimeUnit.SECONDS);
      _effectsScheduledThreadPool.shutdown();
      _generalScheduledThreadPool.shutdown();
      _generalPacketsThreadPool.shutdown();
      _ioPacketsThreadPool.shutdown();
      _generalThreadPool.shutdown();
      _aiThreadPool.shutdown();
      _log.info("All ThreadPools are now stoped");
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public boolean isShutdown()
  {
    return _shutdown;
  }

  public void purge()
  {
    _effectsScheduledThreadPool.purge();
    _generalScheduledThreadPool.purge();
    _aiScheduledThreadPool.purge();
    _ioPacketsThreadPool.purge();
    _generalPacketsThreadPool.purge();
    _generalThreadPool.purge();
    _aiThreadPool.purge();
  }

  public String getPacketStats()
  {
    TextBuilder tb = new TextBuilder();
    ThreadFactory tf = _generalPacketsThreadPool.getThreadFactory();
    if ((tf instanceof PriorityThreadFactory))
    {
      tb.append("General Packet Thread Pool:\r\n");
      tb.append("Tasks in the queue: " + _generalPacketsThreadPool.getQueue().size() + "\r\n");
      tb.append("Showing threads stack trace:\r\n");
      PriorityThreadFactory ptf = (PriorityThreadFactory)tf;
      int count = ptf.getGroup().activeCount();
      Thread[] threads = new Thread[count + 2];
      ptf.getGroup().enumerate(threads);
      tb.append("There should be " + count + " Threads\r\n");
      for (Thread t : threads)
      {
        if (t == null)
          continue;
        tb.append(t.getName() + "\r\n");
        for (StackTraceElement ste : t.getStackTrace())
        {
          tb.append(ste.toString());
          tb.append("\r\n");
        }
      }
    }
    tb.append("Packet Tp stack traces printed.\r\n");
    return tb.toString();
  }

  public String getIOPacketStats()
  {
    TextBuilder tb = new TextBuilder();
    ThreadFactory tf = _ioPacketsThreadPool.getThreadFactory();
    if ((tf instanceof PriorityThreadFactory))
    {
      tb.append("I/O Packet Thread Pool:\r\n");
      tb.append("Tasks in the queue: " + _ioPacketsThreadPool.getQueue().size() + "\r\n");
      tb.append("Showing threads stack trace:\r\n");
      PriorityThreadFactory ptf = (PriorityThreadFactory)tf;
      int count = ptf.getGroup().activeCount();
      Thread[] threads = new Thread[count + 2];
      ptf.getGroup().enumerate(threads);
      tb.append("There should be " + count + " Threads\r\n");
      for (Thread t : threads)
      {
        if (t == null)
          continue;
        tb.append(t.getName() + "\r\n");
        for (StackTraceElement ste : t.getStackTrace())
        {
          tb.append(ste.toString());
          tb.append("\r\n");
        }
      }
    }
    tb.append("Packet Tp stack traces printed.\r\n");
    return tb.toString();
  }

  public String getGeneralStats()
  {
    TextBuilder tb = new TextBuilder();
    ThreadFactory tf = _generalThreadPool.getThreadFactory();
    if ((tf instanceof PriorityThreadFactory))
    {
      tb.append("General Thread Pool:\r\n");
      tb.append("Tasks in the queue: " + _generalThreadPool.getQueue().size() + "\r\n");
      tb.append("Showing threads stack trace:\r\n");
      PriorityThreadFactory ptf = (PriorityThreadFactory)tf;
      int count = ptf.getGroup().activeCount();
      Thread[] threads = new Thread[count + 2];
      ptf.getGroup().enumerate(threads);
      tb.append("There should be " + count + " Threads\r\n");
      for (Thread t : threads)
      {
        if (t == null)
          continue;
        tb.append(t.getName() + "\r\n");
        for (StackTraceElement ste : t.getStackTrace())
        {
          tb.append(ste.toString());
          tb.append("\r\n");
        }
      }
    }
    tb.append("Packet Tp stack traces printed.\r\n");
    return tb.toString();
  }
  protected class PurgeTask implements Runnable {
    protected PurgeTask() {
    }

    public void run() {
      _effectsScheduledThreadPool.purge();
      _generalScheduledThreadPool.purge();
      _aiScheduledThreadPool.purge();
      _ioPacketsThreadPool.purge();
      _generalPacketsThreadPool.purge();
      _generalThreadPool.purge();
      _aiThreadPool.purge();
    }
  }

  private class PriorityThreadFactory
    implements ThreadFactory
  {
    private int _prio;
    private String _name;
    private AtomicInteger _threadNumber = new AtomicInteger(1);
    private ThreadGroup _group;

    public PriorityThreadFactory(String name, int prio)
    {
      _prio = prio;
      _name = name;
      _group = new ThreadGroup(_name);
    }

    public Thread newThread(Runnable r)
    {
      Thread t = new Thread(_group, r);
      t.setName(_name + "-" + _threadNumber.getAndIncrement());
      t.setPriority(_prio);
      return t;
    }

    public ThreadGroup getGroup()
    {
      return _group;
    }
  }
}