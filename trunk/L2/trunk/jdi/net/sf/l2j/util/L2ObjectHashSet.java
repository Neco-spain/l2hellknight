package net.sf.l2j.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;

public final class L2ObjectHashSet<T extends L2Object> extends L2ObjectSet<T>
  implements Iterable<T>
{
  private static final boolean TRACE = false;
  private static final boolean DEBUG = false;
  private static final int[] PRIMES;
  private T[] _table;
  private int[] _collisions;
  private int _count;

  private static int getPrime(int min)
  {
    for (int i = 0; i < PRIMES.length; i++)
    {
      if (PRIMES[i] >= min)
        return PRIMES[i];
    }
    throw new OutOfMemoryError();
  }

  public L2ObjectHashSet()
  {
    int size = PRIMES[0];
    _table = ((L2Object[])new L2Object[size]);
    _collisions = new int[size + 31 >> 5];
  }

  public int size()
  {
    return _count;
  }

  public boolean isEmpty()
  {
    return _count == 0;
  }

  public synchronized void clear()
  {
    int size = PRIMES[0];
    _table = ((L2Object[])new L2Object[size]);
    _collisions = new int[size + 31 >> 5];
    _count = 0;
  }

  private void check()
  {
  }

  public synchronized void put(T obj)
  {
    if (obj == null)
      return;
    if (contains(obj))
      return;
    if (_count >= _table.length / 2)
      expand();
    int hashcode = obj.getObjectId();
    if ((Config.ASSERT) && (!$assertionsDisabled) && (hashcode <= 0)) throw new AssertionError();
    int seed = hashcode;
    int incr = 1 + ((seed >> 5) + 1) % (_table.length - 1);
    int ntry = 0;
    int slot = -1;
    do
    {
      int pos = seed % _table.length & 0x7FFFFFFF;
      if (_table[pos] == null)
      {
        if (slot < 0)
          slot = pos;
        if ((_collisions[(pos >> 5)] & 1 << (pos & 0x1F)) == 0)
        {
          _table[slot] = obj;
          _count += 1;

          return;
        }

      }
      else
      {
        if (_table[pos] == obj) {
          return;
        }
        if ((Config.ASSERT) && (!$assertionsDisabled) && (obj.getObjectId() == _table[pos].getObjectId())) throw new AssertionError();

        if ((slot >= 0) && ((_collisions[(pos >> 5)] & 1 << (pos & 0x1F)) == 0))
        {
          _table[slot] = obj;
          _count += 1;

          return;
        }

      }

      _collisions[(pos >> 5)] |= 1 << (pos & 0x1F);

      seed += incr;
      ntry++; } while (ntry < _table.length);

    throw new IllegalStateException();
  }

  public synchronized void remove(T obj)
  {
    if (obj == null)
      return;
    if (!contains(obj))
      return;
    int hashcode = obj.getObjectId();
    if ((Config.ASSERT) && (!$assertionsDisabled) && (hashcode <= 0)) throw new AssertionError();
    int seed = hashcode;
    int incr = 1 + ((seed >> 5) + 1) % (_table.length - 1);
    int ntry = 0;
    do
    {
      int pos = seed % _table.length & 0x7FFFFFFF;
      if (_table[pos] == obj)
      {
        _table[pos] = null;
        _count -= 1;

        return;
      }

      if ((_table[pos] == null) && ((_collisions[(pos >> 5)] & 1 << (pos & 0x1F)) == 0))
      {
        return;
      }

      seed += incr;
      ntry++; } while (ntry < _table.length);

    throw new IllegalStateException();
  }

  public boolean contains(T obj)
  {
    int size = _table.length;
    if (size <= 11)
    {
      for (int i = 0; i < size; i++)
      {
        if (_table[i] == obj)
          return true;
      }
      return false;
    }
    int hashcode = obj.getObjectId();
    if ((Config.ASSERT) && (!$assertionsDisabled) && (hashcode <= 0)) throw new AssertionError();
    int seed = hashcode;
    int incr = 1 + ((seed >> 5) + 1) % (size - 1);
    int ntry = 0;
    do
    {
      int pos = seed % size & 0x7FFFFFFF;
      if (_table[pos] == obj) {
        return true;
      }
      if ((_table[pos] == null) && ((_collisions[(pos >> 5)] & 1 << (pos & 0x1F)) == 0)) {
        return false;
      }

      seed += incr;
      ntry++; } while (ntry < size);
    return false;
  }

  private void expand()
  {
    int newSize = getPrime(_table.length + 1);
    L2Object[] newTable = new L2Object[newSize];
    int[] newCollisions = new int[newSize + 31 >> 5];

    for (int i = 0; i < _table.length; i++)
    {
      L2Object obj = _table[i];
      if (obj == null)
        continue;
      int hashcode = obj.getObjectId();
      int seed = hashcode;
      int incr = 1 + ((seed >> 5) + 1) % (newSize - 1);
      int ntry = 0;
      while (true)
      {
        int pos = seed % newSize & 0x7FFFFFFF;
        if (newTable[pos] == null)
        {
          newTable[pos] = obj;
        }
        else
        {
          newCollisions[(pos >> 5)] |= 1 << (pos & 0x1F);

          seed += incr;
          ntry++; if (ntry >= newSize)
            throw new IllegalStateException(); 
        }
      }
    }
    _table = ((L2Object[])newTable);
    _collisions = newCollisions;
  }

  public Iterator<T> iterator()
  {
    return new Itr(_table);
  }

  static
  {
    PRIMES = new int[] { 5, 7, 11, 17, 23, 29, 37, 47, 59, 71, 89, 107, 131, 163, 197, 239, 293, 353, 431, 521, 631, 761, 919, 1103, 1327, 1597, 1931, 2333, 2801, 3371, 4049, 4861, 5839, 7013, 8419, 10103, 12143, 14591, 17519, 21023, 25229, 30293, 36353, 43627, 52361, 62851, 75431, 90523, 108631, 130363, 156437, 187751, 225307, 270371, 324449, 389357, 467237, 560689, 672827, 807403, 968897, 1162687, 1395263, 1674319, 2009191, 2411033, 2893249, 3471899, 4166287, 4999559, 5999471, 7199369 };
  }

  class Itr
    implements Iterator<T>
  {
    private final T[] _array;
    private int _nextIdx;
    private T _nextObj;
    private T _lastRet;

    Itr()
    {
      _array = pArray;
      for (; _nextIdx < _array.length; _nextIdx += 1)
      {
        _nextObj = _array[_nextIdx];
        if (_nextObj != null)
          return;
      }
    }

    public boolean hasNext() {
      return _nextObj != null;
    }

    public T next() {
      if (_nextObj == null)
        throw new NoSuchElementException();
      _lastRet = _nextObj;
      for (_nextIdx += 1; _nextIdx < _array.length; _nextIdx += 1)
      {
        _nextObj = _array[_nextIdx];
        if (_nextObj != null)
          break;
      }
      if (_nextIdx >= _array.length)
        _nextObj = null;
      return _lastRet;
    }

    public void remove() {
      if (_lastRet == null)
        throw new IllegalStateException();
      remove(_lastRet);
    }
  }
}