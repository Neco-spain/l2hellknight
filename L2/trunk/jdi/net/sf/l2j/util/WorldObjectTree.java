package net.sf.l2j.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.sf.l2j.gameserver.model.L2Object;

public class WorldObjectTree<T extends L2Object> extends L2ObjectMap<T>
{
  private final TreeMap<Integer, T> _objectMap = new TreeMap();
  private final ReentrantReadWriteLock _rwl = new ReentrantReadWriteLock();
  private final Lock _r = _rwl.readLock();
  private final Lock _w = _rwl.writeLock();

  public int size()
  {
    _r.lock();
    try {
      int i = _objectMap.size();
      return i; } finally { _r.unlock(); } throw localObject;
  }

  public boolean isEmpty()
  {
    _r.lock();
    try {
      boolean bool = _objectMap.isEmpty();
      return bool; } finally { _r.unlock(); } throw localObject;
  }

  public void clear()
  {
    _w.lock();
    try {
      _objectMap.clear();
    } finally {
      _w.unlock();
    }
  }

  public void put(T obj)
  {
    if (obj != null) {
      _w.lock();
      try {
        _objectMap.put(Integer.valueOf(obj.getObjectId()), obj);
      } finally {
        _w.unlock();
      }
    }
  }

  public void remove(T obj)
  {
    if (obj != null) {
      _w.lock();
      try {
        _objectMap.remove(Integer.valueOf(obj.getObjectId()));
      } finally {
        _w.unlock();
      }
    }
  }

  public T get(int id)
  {
    _r.lock();
    try {
      L2Object localL2Object = (L2Object)_objectMap.get(Integer.valueOf(id));
      return localL2Object; } finally { _r.unlock(); } throw localObject;
  }

  public boolean contains(T obj)
  {
    if (obj == null) return false;
    _r.lock();
    try {
      boolean bool = _objectMap.containsValue(obj);
      return bool; } finally { _r.unlock(); } throw localObject;
  }

  public Iterator<T> iterator()
  {
    _r.lock();
    try {
      Iterator localIterator = _objectMap.values().iterator();
      return localIterator; } finally { _r.unlock(); } throw localObject;
  }
}