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
          ExclusiveTask.this.onElapsed();
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
    this._returnIfAlreadyRunning = returnIfAlreadyRunning;
  }

  protected ExclusiveTask()
  {
    this(false);
  }

  public synchronized boolean isScheduled()
  {
    return this._future != null;
  }

  public final synchronized void cancel()
  {
    if (this._future != null)
    {
      this._future.cancel(false);
      this._future = null;
    }
  }

  public final synchronized void schedule(long delay)
  {
    cancel();

    this._future = ThreadPoolManager.getInstance().scheduleEffect(this._runnable, delay);
  }

  public final synchronized void execute()
  {
    ThreadPoolManager.getInstance().executeTask(this._runnable);
  }

  public final synchronized void scheduleAtFixedRate(long delay, long period)
  {
    cancel();

    this._future = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this._runnable, delay, period);
  }

  protected abstract void onElapsed();

  private synchronized boolean tryLock()
  {
    if (this._returnIfAlreadyRunning) {
      return !this._isRunning;
    }
    this._currentThread = Thread.currentThread();
    while (true)
    {
      try
      {
        notifyAll();

        if (this._currentThread != Thread.currentThread()) {
          return false;
        }
        if (!this._isRunning) {
          return true;
        }
        wait();

        continue; } catch (InterruptedException e) {
      }
    }
  }

  private synchronized void unlock() {
    this._isRunning = false;
  }
}