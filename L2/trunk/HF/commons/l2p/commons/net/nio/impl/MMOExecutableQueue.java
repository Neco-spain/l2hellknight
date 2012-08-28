package l2m.commons.net.nio.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class MMOExecutableQueue<T extends MMOClient>
  implements Queue<ReceivablePacket<T>>, Runnable
{
  private static final int NONE = 0;
  private static final int QUEUED = 1;
  private static final int RUNNING = 2;
  private final IMMOExecutor<T> _executor;
  private final Queue<ReceivablePacket<T>> _queue;
  private AtomicInteger _state = new AtomicInteger(0);

  public MMOExecutableQueue(IMMOExecutor<T> executor)
  {
    _executor = executor;
    _queue = new ArrayDeque();
  }

  public void run()
  {
    while (_state.compareAndSet(1, 2))
      try
      {
        while (true)
        {
          Runnable t = poll();
          if (t == null) {
            break;
          }
          t.run();
        }
      }
      finally
      {
        _state.compareAndSet(2, 0);
      }
  }

  public int size()
  {
    return _queue.size();
  }

  public boolean isEmpty()
  {
    return _queue.isEmpty();
  }

  public boolean contains(Object o)
  {
    throw new UnsupportedOperationException();
  }

  public Iterator<ReceivablePacket<T>> iterator()
  {
    throw new UnsupportedOperationException();
  }

  public Object[] toArray()
  {
    throw new UnsupportedOperationException();
  }

  public <E> E[] toArray(E[] a)
  {
    throw new UnsupportedOperationException();
  }

  public boolean remove(Object o)
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAll(Collection<?> c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(Collection<? extends ReceivablePacket<T>> c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(Collection<?> c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll(Collection<?> c)
  {
    throw new UnsupportedOperationException();
  }

  public void clear()
  {
    synchronized (_queue)
    {
      _queue.clear();
    }
  }

  public boolean add(ReceivablePacket<T> e)
  {
    synchronized (_queue)
    {
      if (!_queue.add(e)) {
        return false;
      }
    }
    if (_state.getAndSet(1) == 0) {
      _executor.execute(this);
    }
    return true;
  }

  public boolean offer(ReceivablePacket<T> e)
  {
    synchronized (_queue)
    {
      return _queue.offer(e);
    }
  }

  public ReceivablePacket<T> remove()
  {
    synchronized (_queue)
    {
      return (ReceivablePacket)_queue.remove();
    }
  }

  public ReceivablePacket<T> poll()
  {
    synchronized (_queue)
    {
      return (ReceivablePacket)_queue.poll();
    }
  }

  public ReceivablePacket<T> element()
  {
    synchronized (_queue)
    {
      return (ReceivablePacket)_queue.element();
    }
  }

  public ReceivablePacket<T> peek()
  {
    synchronized (_queue)
    {
      return (ReceivablePacket)_queue.peek();
    }
  }
}