package net.sf.l2j.gameserver.cache;

import javolution.context.ObjectFactory;
import javolution.lang.Reusable;
import javolution.util.FastCollection;
import javolution.util.FastCollection.Record;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

public class FastMRUCache<K, V> extends FastCollection
  implements Reusable
{
  private static final long serialVersionUID = 1L;
  private static final int DEFAULT_CAPACITY = 50;
  private static final int DEFAULT_FORGET_TIME = 300000;
  private FastMap<K, FastMRUCache<K, V>.CacheNode> _cache = new FastMap().setKeyComparator(FastComparator.DIRECT);
  private FastMap<K, V> _map;
  private FastList<K> _mruList = new FastList();
  private int _cacheSize;
  private int _forgetTime;
  private static final ObjectFactory FACTORY = new ObjectFactory()
  {
    public Object create()
    {
      return new FastMRUCache();
    }

    public void cleanup(Object obj)
    {
      ((FastMRUCache)obj).reset();
    }
  };

  public static FastMRUCache newInstance()
  {
    return (FastMRUCache)FACTORY.object();
  }

  public FastMRUCache()
  {
    this(new FastMap(), 50, 300000);
  }

  public FastMRUCache(FastMap<K, V> map)
  {
    this(map, 50, 300000);
  }

  public FastMRUCache(FastMap<K, V> map, int max)
  {
    this(map, max, 300000);
  }

  public FastMRUCache(FastMap<K, V> map, int max, int forgetTime)
  {
    _map = map;
    _cacheSize = max;
    _forgetTime = forgetTime;
    _map.setKeyComparator(FastComparator.DIRECT);
  }

  public synchronized void reset()
  {
    _map.reset();
    _cache.reset();
    _mruList.reset();
    _map.setKeyComparator(FastComparator.DIRECT);
    _cache.setKeyComparator(FastComparator.DIRECT);
  }

  public synchronized V get(K key)
  {
    Object result;
    if (!_cache.containsKey(key))
    {
      if (_mruList.size() >= _cacheSize)
      {
        _cache.remove(_mruList.getLast());
        _mruList.removeLast();
      }

      Object result = _map.get(key);

      _cache.put(key, new CacheNode(result));
      _mruList.addFirst(key);
    }
    else
    {
      CacheNode current = (CacheNode)_cache.get(key);

      if (current._lastModified + _forgetTime <= System.currentTimeMillis())
      {
        current._lastModified = System.currentTimeMillis();
        current._node = _map.get(key);
        _cache.put(key, current);
      }

      _mruList.remove(key);
      _mruList.addFirst(key);

      result = current._node;
    }

    return result;
  }

  public synchronized boolean remove(Object key)
  {
    _cache.remove(key);
    _mruList.remove(key);
    return _map.remove(key) == key;
  }

  public FastMap<K, V> getContentMap()
  {
    return _map;
  }

  public int size()
  {
    return _mruList.size();
  }

  public int capacity()
  {
    return _cacheSize;
  }

  public int getForgetTime()
  {
    return _forgetTime;
  }

  public synchronized void clear()
  {
    _cache.clear();
    _mruList.clear();
    _map.clear();
  }

  public final FastCollection.Record head()
  {
    return _mruList.head();
  }

  public final FastCollection.Record tail()
  {
    return _mruList.tail();
  }

  public final Object valueOf(FastCollection.Record record)
  {
    return ((FastMap.Entry)record).getKey();
  }

  public final void delete(FastCollection.Record record)
  {
    remove(((FastMap.Entry)record).getKey());
  }

  class CacheNode
  {
    long _lastModified;
    V _node;

    public CacheNode()
    {
      _lastModified = System.currentTimeMillis();
      _node = object;
    }

    public boolean equals(Object object)
    {
      return _node == object;
    }
  }
}