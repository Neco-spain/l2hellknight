package l2m.commons.util.concurrent.locks;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class ReentrantReadWriteLock
{
  private static final AtomicIntegerFieldUpdater<ReentrantReadWriteLock> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(ReentrantReadWriteLock.class, "state");
  static final int SHARED_SHIFT = 16;
  static final int SHARED_UNIT = 65536;
  static final int MAX_COUNT = 65535;
  static final int EXCLUSIVE_MASK = 65535;
  transient ThreadLocalHoldCounter readHolds;
  transient HoldCounter cachedHoldCounter;
  private Thread owner;
  private volatile int state;

  static int sharedCount(int c)
  {
    return c >>> 16;
  }

  static int exclusiveCount(int c)
  {
    return c & 0xFFFF;
  }

  public ReentrantReadWriteLock()
  {
    readHolds = new ThreadLocalHoldCounter();
    setState(0);
  }

  private final int getState()
  {
    return state;
  }

  private void setState(int newState)
  {
    state = newState;
  }

  private boolean compareAndSetState(int expect, int update)
  {
    return stateUpdater.compareAndSet(this, expect, update);
  }

  private Thread getExclusiveOwnerThread()
  {
    return owner;
  }

  private void setExclusiveOwnerThread(Thread thread)
  {
    owner = thread;
  }

  public void writeLock()
  {
    Thread current = Thread.currentThread();
    while (true)
    {
      int c = getState();
      int w = exclusiveCount(c);
      if (c != 0)
      {
        if ((w == 0) || (current != getExclusiveOwnerThread()))
          continue;
        if (w + exclusiveCount(1) > 65535)
          throw new Error("Maximum lock count exceeded");
      }
      if (compareAndSetState(c, c + 1))
      {
        setExclusiveOwnerThread(current);
        return;
      }
    }
  }

  public boolean tryWriteLock()
  {
    Thread current = Thread.currentThread();
    int c = getState();
    if (c != 0)
    {
      int w = exclusiveCount(c);
      if ((w == 0) || (current != getExclusiveOwnerThread()))
        return false;
      if (w == 65535)
        throw new Error("Maximum lock count exceeded");
    }
    if (!compareAndSetState(c, c + 1))
      return false;
    setExclusiveOwnerThread(current);
    return true;
  }

  final boolean tryReadLock()
  {
    Thread current = Thread.currentThread();
    int c = getState();
    int w = exclusiveCount(c);
    if ((w != 0) && (getExclusiveOwnerThread() != current))
      return false;
    if (sharedCount(c) == 65535)
      throw new Error("Maximum lock count exceeded");
    if (compareAndSetState(c, c + 65536))
    {
      HoldCounter rh = cachedHoldCounter;
      if ((rh == null) || (rh.tid != current.getId()))
        cachedHoldCounter = (rh = (HoldCounter)readHolds.get());
      rh.count += 1;
      return true;
    }
    return false;
  }

  public void readLock()
  {
    Thread current = Thread.currentThread();
    HoldCounter rh = cachedHoldCounter;
    if ((rh == null) || (rh.tid != current.getId()))
      rh = (HoldCounter)readHolds.get();
    while (true)
    {
      int c = getState();
      int w = exclusiveCount(c);
      if ((w != 0) && (getExclusiveOwnerThread() != current))
        continue;
      if (sharedCount(c) == 65535)
        throw new Error("Maximum lock count exceeded");
      if (compareAndSetState(c, c + 65536))
      {
        cachedHoldCounter = rh;
        rh.count += 1;
        return;
      }
    }
  }

  public void writeUnlock()
  {
    int nextc = getState() - 1;
    if (Thread.currentThread() != getExclusiveOwnerThread())
      throw new IllegalMonitorStateException();
    if (exclusiveCount(nextc) == 0)
    {
      setExclusiveOwnerThread(null);
      setState(nextc);
      return;
    }

    setState(nextc);
  }

  public void readUnlock()
  {
    HoldCounter rh = cachedHoldCounter;
    Thread current = Thread.currentThread();
    if ((rh == null) || (rh.tid != current.getId()))
      rh = (HoldCounter)readHolds.get();
    if (rh.tryDecrement() <= 0)
      throw new IllegalMonitorStateException();
    while (true)
    {
      int c = getState();
      int nextc = c - 65536;
      if (compareAndSetState(c, nextc))
        return;
    }
  }

  static final class ThreadLocalHoldCounter extends ThreadLocal<ReentrantReadWriteLock.HoldCounter>
  {
    public ReentrantReadWriteLock.HoldCounter initialValue()
    {
      return new ReentrantReadWriteLock.HoldCounter();
    }
  }

  static final class HoldCounter
  {
    int count;
    final long tid = Thread.currentThread().getId();

    int tryDecrement()
    {
      int c = count;
      if (c > 0)
        count = (c - 1);
      return c;
    }
  }
}