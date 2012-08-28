package net.sf.l2j.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Object;

public class WorldObjectSet<T extends L2Object> extends L2ObjectSet<T>
{
  private Map<Integer, T> _objectMap;

  public WorldObjectSet()
  {
    _objectMap = new FastMap().setShared(true);
  }

  public int size()
  {
    return _objectMap.size();
  }

  public boolean isEmpty()
  {
    return _objectMap.isEmpty();
  }

  public void clear()
  {
    _objectMap.clear();
  }

  public void put(T obj)
  {
    _objectMap.put(Integer.valueOf(obj.getObjectId()), obj);
  }

  public void remove(T obj)
  {
    _objectMap.remove(Integer.valueOf(obj.getObjectId()));
  }

  public boolean contains(T obj)
  {
    return _objectMap.containsKey(Integer.valueOf(obj.getObjectId()));
  }

  public Iterator<T> iterator()
  {
    return _objectMap.values().iterator();
  }
}