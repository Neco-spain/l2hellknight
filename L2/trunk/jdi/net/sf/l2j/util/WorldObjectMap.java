package net.sf.l2j.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Object;

public class WorldObjectMap<T extends L2Object> extends L2ObjectMap<T>
{
  Map<Integer, T> _objectMap = new FastMap().setShared(true);

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
    if (obj != null)
      _objectMap.put(Integer.valueOf(obj.getObjectId()), obj);
  }

  public void remove(T obj)
  {
    if (obj != null)
      _objectMap.remove(Integer.valueOf(obj.getObjectId()));
  }

  public T get(int id)
  {
    return (L2Object)_objectMap.get(Integer.valueOf(id));
  }

  public boolean contains(T obj)
  {
    if (obj == null)
      return false;
    return _objectMap.get(Integer.valueOf(obj.getObjectId())) != null;
  }

  public Iterator<T> iterator()
  {
    return _objectMap.values().iterator();
  }
}