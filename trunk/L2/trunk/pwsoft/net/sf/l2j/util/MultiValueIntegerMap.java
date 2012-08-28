package net.sf.l2j.util;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;

public class MultiValueIntegerMap
{
  private FastMap<Integer, FastList<Integer>> map;

  public MultiValueIntegerMap()
  {
    map = new FastMap(10).setShared(true);
  }

  public Set<Integer> keySet()
  {
    return map.keySet();
  }

  public Collection<FastList<Integer>> values()
  {
    return map.values();
  }

  public FastList<Integer> allValues()
  {
    FastList result = new FastList(10);
    for (Map.Entry entry : map.entrySet())
      result.addAll((Collection)entry.getValue());
    return result;
  }

  public Set<Map.Entry<Integer, FastList<Integer>>> entrySet()
  {
    return map.entrySet();
  }

  public FastList<Integer> remove(Integer key)
  {
    return (FastList)map.remove(key);
  }

  public FastList<Integer> get(Integer key)
  {
    return (FastList)map.get(key);
  }

  public boolean containsKey(Integer key)
  {
    return map.containsKey(key);
  }

  public void clear()
  {
    map.clear();
  }

  public int size()
  {
    return map.size();
  }

  public boolean isEmpty()
  {
    return map.isEmpty();
  }

  public Integer remove(Integer key, Integer value)
  {
    FastList valuesForKey = (FastList)map.get(key);
    if (valuesForKey == null) return null;
    boolean removed = valuesForKey.remove(value);
    if (!removed) return null;
    if (valuesForKey.isEmpty())
      remove(key);
    return value;
  }

  public Integer removeValue(Integer value)
  {
    FastList toRemove = new FastList(1);
    for (Map.Entry entry : map.entrySet())
    {
      ((FastList)entry.getValue()).remove(value);
      if (((FastList)entry.getValue()).isEmpty())
        toRemove.add(entry.getKey());
    }
    for (Integer key : toRemove)
      remove(key);
    return value;
  }

  public Integer put(Integer key, Integer value)
  {
    FastList coll = (FastList)map.get(key);
    if (coll == null)
    {
      coll = new FastList(1);
      map.put(key, coll);
    }
    coll.add(value);
    return value;
  }

  public boolean containsValue(Integer value)
  {
    for (Map.Entry entry : map.entrySet())
      if (((FastList)entry.getValue()).contains(value))
        return true;
    return false;
  }

  public boolean containsValue(Integer key, Integer value)
  {
    FastList coll = (FastList)map.get(key);
    if (coll == null) return false;
    return coll.contains(value);
  }

  public int size(Integer key)
  {
    FastList coll = (FastList)map.get(key);
    if (coll == null) return 0;
    return coll.size();
  }

  public boolean putAll(Integer key, Collection<? extends Integer> values)
  {
    if ((values == null) || (values.size() == 0)) return false;
    boolean result = false;
    FastList coll = (FastList)map.get(key);
    if (coll == null)
    {
      coll = new FastList(values.size());
      coll.addAll(values);
      if (coll.size() > 0)
      {
        map.put(key, coll);
        result = true;
      }
    }
    else {
      result = coll.addAll(values);
    }return result;
  }

  public int totalSize()
  {
    int total = 0;
    for (Map.Entry entry : map.entrySet())
      total += ((FastList)entry.getValue()).size();
    return total;
  }
}