package l2m.gameserver.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameObjectArray<E extends GameObject>
  implements Iterable<E>
{
  private static final Logger _log = LoggerFactory.getLogger(GameObjectArray.class);
  public final String name;
  public final int resizeStep;
  public final int initCapacity;
  private final List<Integer> freeIndexes;
  private E[] elementData;
  private int size = 0; private int real_size = 0;

  public GameObjectArray(String _name, int initialCapacity, int _resizeStep)
  {
    name = _name;
    resizeStep = _resizeStep;
    initCapacity = initialCapacity;

    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Capacity (" + name + "): " + initialCapacity);
    if (resizeStep < 1) {
      throw new IllegalArgumentException("Illegal resize step (" + name + "): " + resizeStep);
    }
    freeIndexes = new ArrayList(resizeStep);
    elementData = ((GameObject[])new GameObject[initialCapacity]);
  }

  public int size()
  {
    return size;
  }

  public int getRealSize()
  {
    return real_size;
  }

  public int capacity()
  {
    return elementData.length;
  }

  public synchronized int add(E e)
  {
    Integer freeIndex = null;

    if (freeIndexes.size() > 0) {
      freeIndex = (Integer)freeIndexes.remove(freeIndexes.size() - 1);
    }
    if (freeIndex != null)
    {
      real_size += 1;
      elementData[freeIndex.intValue()] = e;
      return freeIndex.intValue();
    }

    if (elementData.length <= size)
    {
      int newCapacity = elementData.length + resizeStep;
      _log.warn("Object array [" + name + "] resized: " + elementData.length + " -> " + newCapacity);
      elementData = ((GameObject[])Arrays.copyOf(elementData, newCapacity));
    }
    elementData[(size++)] = e;
    real_size += 1;
    return size - 1;
  }

  public synchronized E remove(int index, int expectedObjId)
  {
    if (index >= size) {
      return null;
    }
    GameObject old = elementData[index];
    if ((old == null) || (old.getObjectId() != expectedObjId)) {
      return null;
    }
    elementData[index] = null;
    real_size -= 1;
    if (index == size - 1)
      size -= 1;
    else
      freeIndexes.add(Integer.valueOf(index));
    return old;
  }

  public E get(int index)
  {
    return index >= size ? null : elementData[index];
  }

  public E findByObjectId(int objId)
  {
    if (objId <= 0) {
      return null;
    }
    for (int i = 0; i < size; i++)
    {
      GameObject o = elementData[i];
      if ((o != null) && (o.getObjectId() == objId))
        return o;
    }
    return null;
  }

  public E findByName(String s)
  {
    if (s == null) {
      return null;
    }
    for (int i = 0; i < size; i++)
    {
      GameObject o = elementData[i];
      if ((o != null) && (s.equalsIgnoreCase(o.getName())))
        return o;
    }
    return null;
  }

  public List<E> findAllByName(String s)
  {
    if (s == null) {
      return null;
    }
    List result = new ArrayList();

    for (int i = 0; i < size; i++)
    {
      GameObject o = elementData[i];
      if ((o != null) && (s.equalsIgnoreCase(o.getName())))
        result.add(o);
    }
    return result;
  }

  public List<E> getAll()
  {
    return getAll(new ArrayList(size));
  }

  public List<E> getAll(List<E> list)
  {
    for (int i = 0; i < size; i++)
    {
      GameObject o = elementData[i];
      if (o != null)
        list.add(o);
    }
    return list;
  }

  private int indexOf(E o)
  {
    if (o == null) {
      return -1;
    }
    for (int i = 0; i < size; i++) {
      if (o.equals(elementData[i]))
        return i;
    }
    return -1;
  }

  public boolean contains(E o)
  {
    return indexOf(o) > -1;
  }

  public synchronized void clear()
  {
    elementData = ((GameObject[])new GameObject[0]);
    size = 0;
    real_size = 0;
  }

  public Iterator<E> iterator()
  {
    return new Itr();
  }

  class Itr implements Iterator<E>
  {
    private int cursor = 0;
    private E _next;

    Itr()
    {
    }

    public boolean hasNext() {
      while (cursor < size)
        if ((this._next = elementData[(cursor++)]) != null)
          return true;
      return false;
    }

    public E next()
    {
      GameObject result = _next;
      _next = null;
      if (result == null)
        throw new NoSuchElementException();
      return result;
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
}