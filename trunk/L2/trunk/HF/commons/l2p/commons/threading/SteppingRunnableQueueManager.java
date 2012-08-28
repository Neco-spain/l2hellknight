package l2m.commons.threading;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import l2m.commons.collections.LazyArrayList;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SteppingRunnableQueueManager
  implements Runnable
{
  private static final Logger _log = LoggerFactory.getLogger(SteppingRunnableQueueManager.class);
  protected final long tickPerStepInMillis;
  private final List<SteppingScheduledFuture<?>> queue = new CopyOnWriteArrayList();
  private final AtomicBoolean isRunning = new AtomicBoolean();

  public SteppingRunnableQueueManager(long tickPerStepInMillis)
  {
    this.tickPerStepInMillis = tickPerStepInMillis;
  }

  public SteppingScheduledFuture<?> schedule(Runnable r, long delay)
  {
    return schedule(r, delay, delay, false);
  }

  public SteppingScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
  {
    return schedule(r, initial, delay, true);
  }

  private SteppingScheduledFuture<?> schedule(Runnable r, long initial, long delay, boolean isPeriodic)
  {
    long initialStepping = getStepping(initial);
    long stepping = getStepping(delay);
    SteppingScheduledFuture sr;
    queue.add(sr = new SteppingScheduledFuture(r, initialStepping, stepping, isPeriodic));

    return sr;
  }

  private long getStepping(long delay)
  {
    delay = Math.max(0L, delay);
    return delay < tickPerStepInMillis ? 1L : delay % tickPerStepInMillis > tickPerStepInMillis / 2L ? delay / tickPerStepInMillis + 1L : delay / tickPerStepInMillis;
  }

  public void run()
  {
    if (!isRunning.compareAndSet(false, true))
    {
      _log.warn(new StringBuilder().append("Slow running queue, managed by ").append(this).append(", queue size : ").append(queue.size()).append("!").toString());
      return;
    }

    try
    {
      if (queue.isEmpty())
        return;
      for (SteppingScheduledFuture sr : queue)
        if (!sr.isDone())
          sr.run();
    }
    finally
    {
      isRunning.set(false);
    }
  }

  public void purge()
  {
    LazyArrayList purge = LazyArrayList.newInstance();

    for (SteppingScheduledFuture sr : queue) {
      if (sr.isDone())
        purge.add(sr);
    }
    queue.removeAll(purge);

    LazyArrayList.recycle(purge);
  }

  public CharSequence getStats()
  {
    StringBuilder list = new StringBuilder();

    Map stats = new TreeMap();
    int total = 0;
    int done = 0;

    for (SteppingScheduledFuture sr : queue)
    {
      if (sr.isDone())
      {
        done++;
        continue;
      }
      total++;
      MutableLong count = (MutableLong)stats.get(sr.r.getClass().getName());
      if (count == null)
        stats.put(sr.r.getClass().getName(), count = new MutableLong(1L));
      else {
        count.increment();
      }
    }
    for (Map.Entry e : stats.entrySet()) {
      list.append("\t").append((String)e.getKey()).append(" : ").append(((MutableLong)e.getValue()).longValue()).append("\n");
    }
    list.append("Scheduled: ....... ").append(total).append("\n");
    list.append("Done/Cancelled: .. ").append(done).append("\n");

    return list;
  }

  public class SteppingScheduledFuture<V>
    implements RunnableScheduledFuture<V>
  {
    private final Runnable r;
    private final long stepping;
    private final boolean isPeriodic;
    private long step;
    private boolean isCancelled;

    public SteppingScheduledFuture(Runnable r, long initial, long stepping, boolean isPeriodic)
    {
      this.r = r;
      step = initial;
      this.stepping = stepping;
      this.isPeriodic = isPeriodic;
    }

    public void run()
    {
      if (--step == 0L)
        try
        {
          r.run();
        }
        catch (Exception e)
        {
          SteppingRunnableQueueManager._log.error("Exception in a Runnable execution:", e);
        }
        finally
        {
          if (isPeriodic)
            step = stepping;
        }
    }

    public boolean isDone()
    {
      return (isCancelled) || ((!isPeriodic) && (step == 0L));
    }

    public boolean isCancelled()
    {
      return isCancelled;
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
      return this.isCancelled = 1;
    }

    public V get()
      throws InterruptedException, ExecutionException
    {
      return null;
    }

    public V get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException
    {
      return null;
    }

    public long getDelay(TimeUnit unit)
    {
      return unit.convert(step * tickPerStepInMillis, TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed o)
    {
      return 0;
    }

    public boolean isPeriodic()
    {
      return isPeriodic;
    }
  }
}