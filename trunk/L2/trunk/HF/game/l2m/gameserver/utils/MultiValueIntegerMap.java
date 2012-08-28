package l2m.gameserver.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiValueIntegerMap
{
  private Map<Integer, List<Integer>> map;

  public MultiValueIntegerMap()
  {
    map = new ConcurrentHashMap();
  }

  public Set<Integer> keySet()
  {
    return map.keySet();
  }

  public Collection<List<Integer>> values()
  {
    return map.values();
  }

  public List<Integer> allValues()
  {
    List result = new ArrayList();
    for (Map.Entry entry : map.entrySet())
      result.addAll((Collection)entry.getValue());
    return result;
  }

  public Set<Map.Entry<Integer, List<Integer>>> entrySet()
  {
    return map.entrySet();
  }

  public List<Integer> remove(Integer key)
  {
    return (List)map.remove(key);
  }

  public List<Integer> get(Integer key)
  {
    return (List)map.get(key);
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
    List valuesForKey = (List)map.get(key);
    if (valuesForKey == null)
      return null;
    boolean removed = valuesForKey.remove(value);
    if (!removed)
      return null;
    if (valuesForKey.isEmpty())
      remove(key);
    return value;
  }

  public Integer removeValue(Integer value)
  {
    List toRemove = new ArrayList(1);
    for (Map.Entry entry : map.entrySet())
    {
      ((List)entry.getValue()).remove(value);
      if (((List)entry.getValue()).isEmpty())
        toRemove.add(entry.getKey());
    }
    for (Integer key : toRemove)
      remove(key);
    return value;
  }

  public Integer put(Integer key, Integer value)
  {
    List coll = (List)map.get(key);
    if (coll == null)
    {
      coll = new CopyOnWriteArrayList();
      map.put(key, coll);
    }
    coll.add(value);
    return value;
  }

  public boolean containsValue(Integer value)
  {
    for (Map.Entry entry : map.entrySet())
      if (((List)entry.getValue()).contains(value))
        return true;
    return false;
  }

  public boolean containsValue(Integer key, Integer value)
  {
    List coll = (List)map.get(key);
    if (coll == null)
      return false;
    return coll.contains(value);
  }

  public int size(Integer key)
  {
    List coll = (List)map.get(key);
    if (coll == null)
      return 0;
    return coll.size();
  }

  public boolean putAll(Integer key, Collection<? extends Integer> values)
  {
    if ((values == null) || (values.size() == 0))
      return false;
    boolean result = false;
    List coll = (List)map.get(key);
    if (coll == null)
    {
      coll = new CopyOnWriteArrayList();
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
      total += ((List)entry.getValue()).size();
    return total;
  }
}