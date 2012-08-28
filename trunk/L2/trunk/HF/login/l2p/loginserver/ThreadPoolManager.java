package l2m.loginserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import l2m.commons.threading.RunnableImpl;

public class ThreadPoolManager
{
  private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(9223372036854775807L - System.nanoTime()) / 2L;

  private static final ThreadPoolManager _instance = new ThreadPoolManager();

  private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue());

  public static final ThreadPoolManager getInstance()
  {
    return _instance;
  }

  private ThreadPoolManager()
  {
    scheduleAtFixedRate(new RunnableImpl()
    {
      public void runImpl()
      {
        executor.purge();
        scheduledExecutor.purge();
      }
    }
    , 600000L, 600000L);
  }

  private final long validate(long delay)
  {
    return Math.max(0L, Math.min(MAX_DELAY, delay));
  }

  public void execute(Runnable r)
  {
    executor.execute(r);
  }

  public ScheduledFuture<?> schedule(Runnable r, long delay)
  {
    return scheduledExecutor.schedule(r, validate(delay), TimeUnit.MILLISECONDS);
  }

  public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
  {
    return scheduledExecutor.scheduleAtFixedRate(r, validate(initial), validate(delay), TimeUnit.MILLISECONDS);
  }
}