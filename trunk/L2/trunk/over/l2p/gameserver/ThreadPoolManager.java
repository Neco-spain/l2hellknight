package l2p.gameserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import l2p.commons.threading.LoggingRejectedExecutionHandler;
import l2p.commons.threading.PriorityThreadFactory;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.threading.RunnableStatsWrapper;

public class ThreadPoolManager
{
  private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(9223372036854775807L - System.nanoTime()) / 2L;

  private static final ThreadPoolManager _instance = new ThreadPoolManager();
  private final ScheduledThreadPoolExecutor _scheduledExecutor;
  private final ThreadPoolExecutor _executor;
  private boolean _shutdown;

  public static ThreadPoolManager getInstance()
  {
    return _instance;
  }

  private ThreadPoolManager()
  {
    _scheduledExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("ScheduledThreadPool", 5), new LoggingRejectedExecutionHandler());
    _executor = new ThreadPoolExecutor(Config.EXECUTOR_THREAD_POOL_SIZE, 2147483647, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("ThreadPoolExecutor", 5), new LoggingRejectedExecutionHandler());

    scheduleAtFixedRate(new RunnableImpl()
    {
      public void runImpl()
      {
        _scheduledExecutor.purge();
        _executor.purge();
      }
    }
    , 300000L, 300000L);
  }

  private long validate(long delay)
  {
    return Math.max(0L, Math.min(MAX_DELAY, delay));
  }

  public boolean isShutdown()
  {
    return _shutdown;
  }

  public Runnable wrap(Runnable r)
  {
    return Config.ENABLE_RUNNABLE_STATS ? RunnableStatsWrapper.wrap(r) : r;
  }

  public ScheduledFuture<?> schedule(Runnable r, long delay)
  {
    return _scheduledExecutor.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
  }

  public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
  {
    return _scheduledExecutor.scheduleAtFixedRate(wrap(r), validate(initial), validate(delay), TimeUnit.MILLISECONDS);
  }

  public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay)
  {
    return _scheduledExecutor.scheduleWithFixedDelay(wrap(r), validate(initial), validate(delay), TimeUnit.MILLISECONDS);
  }

  public void execute(Runnable r)
  {
    _executor.execute(wrap(r));
  }

  public void shutdown() throws InterruptedException
  {
    _shutdown = true;
    try
    {
      _scheduledExecutor.shutdown();
      _scheduledExecutor.awaitTermination(10L, TimeUnit.SECONDS);
    }
    finally
    {
      _executor.shutdown();
      _executor.awaitTermination(1L, TimeUnit.MINUTES);
    }
  }

  public CharSequence getStats()
  {
    StringBuilder list = new StringBuilder();

    list.append("ScheduledThreadPool\n");
    list.append("=================================================\n");
    list.append("\tgetActiveCount: ...... ").append(_scheduledExecutor.getActiveCount()).append("\n");
    list.append("\tgetCorePoolSize: ..... ").append(_scheduledExecutor.getCorePoolSize()).append("\n");
    list.append("\tgetPoolSize: ......... ").append(_scheduledExecutor.getPoolSize()).append("\n");
    list.append("\tgetLargestPoolSize: .. ").append(_scheduledExecutor.getLargestPoolSize()).append("\n");
    list.append("\tgetMaximumPoolSize: .. ").append(_scheduledExecutor.getMaximumPoolSize()).append("\n");
    list.append("\tgetCompletedTaskCount: ").append(_scheduledExecutor.getCompletedTaskCount()).append("\n");
    list.append("\tgetQueuedTaskCount: .. ").append(_scheduledExecutor.getQueue().size()).append("\n");
    list.append("\tgetTaskCount: ........ ").append(_scheduledExecutor.getTaskCount()).append("\n");
    list.append("ThreadPoolExecutor\n");
    list.append("=================================================\n");
    list.append("\tgetActiveCount: ...... ").append(_executor.getActiveCount()).append("\n");
    list.append("\tgetCorePoolSize: ..... ").append(_executor.getCorePoolSize()).append("\n");
    list.append("\tgetPoolSize: ......... ").append(_executor.getPoolSize()).append("\n");
    list.append("\tgetLargestPoolSize: .. ").append(_executor.getLargestPoolSize()).append("\n");
    list.append("\tgetMaximumPoolSize: .. ").append(_executor.getMaximumPoolSize()).append("\n");
    list.append("\tgetCompletedTaskCount: ").append(_executor.getCompletedTaskCount()).append("\n");
    list.append("\tgetQueuedTaskCount: .. ").append(_executor.getQueue().size()).append("\n");
    list.append("\tgetTaskCount: ........ ").append(_executor.getTaskCount()).append("\n");

    return list;
  }
}