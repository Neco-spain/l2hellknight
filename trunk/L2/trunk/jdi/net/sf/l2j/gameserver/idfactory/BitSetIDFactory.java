package net.sf.l2j.gameserver.idfactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.util.PrimeFinder;

public class BitSetIDFactory extends IdFactory
{
  private static Logger _log = Logger.getLogger(BitSetIDFactory.class.getName());
  private BitSet _freeIds;
  private AtomicInteger _freeIdCount;
  private AtomicInteger _nextFreeId;

  protected BitSetIDFactory()
  {
    ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BitSetCapacityCheck(), 30000L, 30000L);
    initialize();
    _log.info("IDFactory: " + _freeIds.size() + " id's available.");
  }

  public synchronized void initialize()
  {
    try
    {
      _freeIds = new BitSet(PrimeFinder.nextPrime(100000));
      _freeIds.clear();
      _freeIdCount = new AtomicInteger(1879048191);

      for (int usedObjectId : extractUsedObjectIDTable())
      {
        int objectID = usedObjectId - 268435456;
        if (objectID < 0)
        {
          _log.warning("Object ID " + usedObjectId + " in DB is less than minimum ID of " + 268435456);
        }
        else {
          _freeIds.set(usedObjectId - 268435456);
          _freeIdCount.decrementAndGet();
        }
      }
      _nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
      _initialized = true;
    }
    catch (Exception e)
    {
      _initialized = false;
      _log.severe("BitSet ID Factory could not be initialized correctly");
      e.printStackTrace();
    }
  }

  public synchronized void releaseId(int objectID)
  {
    if (objectID - 268435456 > -1)
    {
      _freeIds.clear(objectID - 268435456);
      _freeIdCount.incrementAndGet();
    } else {
      _log.warning("BitSet ID Factory: release objectID " + objectID + " failed (< " + 268435456 + ")");
    }
  }

  public synchronized int getNextId()
  {
    int newID = _nextFreeId.get();
    _freeIds.set(newID);
    _freeIdCount.decrementAndGet();

    int nextFree = _freeIds.nextClearBit(newID);

    if (nextFree < 0)
    {
      nextFree = _freeIds.nextClearBit(0);
    }
    if (nextFree < 0)
    {
      if (_freeIds.size() < 1879048191)
      {
        increaseBitSetCapacity();
      }
      else
      {
        throw new NullPointerException("Ran out of valid Id's.");
      }
    }

    _nextFreeId.set(nextFree);

    return newID + 268435456;
  }

  public synchronized int size()
  {
    return _freeIdCount.get();
  }

  protected synchronized int usedIdCount()
  {
    return size() - 268435456;
  }

  protected synchronized boolean reachingBitSetCapacity()
  {
    return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > _freeIds.size();
  }

  protected synchronized void increaseBitSetCapacity()
  {
    BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
    newBitSet.or(_freeIds);
    _freeIds = newBitSet;
  }

  public class BitSetCapacityCheck
    implements Runnable
  {
    public BitSetCapacityCheck()
    {
    }

    public void run()
    {
      if (reachingBitSetCapacity())
      {
        increaseBitSetCapacity();
      }
    }
  }
}