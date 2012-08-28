package net.sf.l2j.gameserver.taskmanager;

import java.util.concurrent.Future;
import net.sf.l2j.gameserver.ThreadPoolManager;

public abstract class ExclusiveTask
{
  private final boolean _returnIfAlreadyRunning;
  private Future<?> _future;
  private boolean _isRunning;
  private Thread _currentThread;
  private final Runnable _runnable = new Runnable()
  {
    public void run()
    {
      if (ExclusiveTask.this.tryLock())
      {
        try
        {
          onElapsed();
        }
        finally
        {
          ExclusiveTask.this.unlock();
        }
      }
    }
  };

  protected ExclusiveTask(boolean returnIfAlreadyRunning)
  {
    _returnIfAlreadyRunning = returnIfAlreadyRunning;
  }

  protected ExclusiveTask()
  {
    this(false);
  }

  public synchronized boolean isScheduled()
  {
    return _future != null;
  }

  public final synchronized void cancel()
  {
    if (_future != null)
    {
      _future.cancel(false);
      _future = null;
    }
  }

  public final synchronized void schedule(long delay)
  {
    cancel();

    _future = ThreadPoolManager.getInstance().scheduleEffect(_runnable, delay);
  }

  public final synchronized void execute()
  {
    ThreadPoolManager.getInstance().executeTask(_runnable);
  }

  public final synchronized void scheduleAtFixedRate(long delay, long period)
  {
    cancel();

    _future = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(_runnable, delay, period);
  }

  protected abstract void onElapsed();

  private synchronized boolean tryLock() {
    if (_returnIfAlreadyRunning) {
      return !_isRunning;
    }
    _currentThread = Thread.currentThread();
    while (true)
    {
      try
      {
        notifyAll();

        if (_currentThread != Thread.currentThread()) {
          return false;
        }
        if (!_isRunning) {
          return true;
        }
        wait();

        continue; } catch (InterruptedException e) {
      }
    }
  }

  private synchronized void unlock() {
    _isRunning = false;
  }
}