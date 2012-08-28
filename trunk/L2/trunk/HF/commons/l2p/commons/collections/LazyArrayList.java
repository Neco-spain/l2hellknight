package l2m.commons.collections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class LazyArrayList<E>
  implements List<E>, RandomAccess, Cloneable, Serializable
{
  private static final long serialVersionUID = 8683452581122892189L;
  private static final int POOL_SIZE = Integer.parseInt(System.getProperty("lazyarraylist.poolsize", "-1"));
  private static final ObjectPool POOL = new GenericObjectPool(new PoolableLazyArrayListFactory(null), POOL_SIZE, 2, 0L, -1);
  private static final int L = 8;
  private static final int H = 1024;
  protected transient Object[] elementData;
  protected transient int size = 0;
  protected transient int capacity = 8;

  public static <E> LazyArrayList<E> newInstance()
  {
    try
    {
      return (LazyArrayList)POOL.borrowObject();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return new LazyArrayList();
  }

  public static <E> void recycle(LazyArrayList<E> obj)
  {
    try
    {
      POOL.returnObject(obj);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public LazyArrayList(int initialCapacity)
  {
    if (initialCapacity < 1024) {
      while (capacity < initialCapacity)
        capacity <<= 1;
    }
    capacity = initialCapacity;
  }

  public LazyArrayList()
  {
    this(8);
  }

  public boolean add(E element)
  {
    ensureCapacity(size + 1);
    elementData[(size++)] = element;

    return true;
  }

  public E set(int index, E element)
  {
    Object e = null;
    if ((index >= 0) && (index < size))
    {
      e = elementData[index];
      elementData[index] = element;
    }
    return e;
  }

  public void add(int index, E element)
  {
    if ((index >= 0) && (index < size))
    {
      ensureCapacity(size + 1);
      System.arraycopy(elementData, index, elementData, index + 1, size - index);
      elementData[index] = element;
      size += 1;
    }
  }

  public boolean addAll(int index, Collection<? extends E> c)
  {
    if ((c == null) || (c.isEmpty())) {
      return false;
    }
    if ((index >= 0) && (index < size))
    {
      Object[] a = c.toArray();
      int numNew = a.length;
      ensureCapacity(size + numNew);

      int numMoved = size - index;
      if (numMoved > 0)
        System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
      System.arraycopy(a, 0, elementData, index, numNew);
      size += numNew;

      return true;
    }

    return false;
  }

  protected void ensureCapacity(int newSize)
  {
    if (newSize > capacity)
    {
      if (newSize < 1024) {
        while (capacity < newSize)
          capacity <<= 1;
      }
      while (capacity < newSize) {
        capacity = (capacity * 3 / 2);
      }
      Object[] elementDataResized = new Object[capacity];
      if (elementData != null)
        System.arraycopy(elementData, 0, elementDataResized, 0, size);
      elementData = elementDataResized;
    }
    else if (elementData == null) {
      elementData = new Object[capacity];
    }
  }

  public E remove(int index)
  {
    Object e = null;
    if ((index >= 0) && (index < size))
    {
      size -= 1;
      e = elementData[index];
      elementData[index] = elementData[size];
      elementData[size] = null;

      trim();
    }
    return e;
  }

  public boolean remove(Object o)
  {
    if (size == 0) {
      return false;
    }
    int index = -1;
    for (int i = 0; i < size; i++) {
      if (elementData[i] != o)
        continue;
      index = i;
      break;
    }

    if (index == -1) {
      return false;
    }
    size -= 1;
    elementData[index] = elementData[size];
    elementData[size] = null;

    trim();

    return true;
  }

  public boolean contains(Object o)
  {
    if (size == 0) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (elementData[i] == o)
        return true;
    }
    return false;
  }

  public int indexOf(Object o)
  {
    if (size == 0) {
      return -1;
    }
    int index = -1;
    for (int i = 0; i < size; i++) {
      if (elementData[i] != o)
        continue;
      index = i;
      break;
    }

    return index;
  }

  public int lastIndexOf(Object o)
  {
    if (size == 0) {
      return -1;
    }
    int index = -1;
    for (int i = 0; i < size; i++) {
      if (elementData[i] == o)
        index = i;
    }
    return index;
  }

  protected void trim()
  {
  }

  public E get(int index)
  {
    if ((size > 0) && (index >= 0) && (index < size)) {
      return elementData[index];
    }
    return null;
  }

  public Object clone()
  {
    LazyArrayList clone = new LazyArrayList();
    if (size > 0)
    {
      clone.capacity = capacity;
      clone.elementData = new Object[elementData.length];
      System.arraycopy(elementData, 0, clone.elementData, 0, size);
    }
    return clone;
  }

  public void clear()
  {
    if (size == 0) {
      return;
    }
    for (int i = 0; i < size; i++) {
      elementData[i] = null;
    }
    size = 0;
    trim();
  }

  public int size()
  {
    return size;
  }

  public boolean isEmpty()
  {
    return size == 0;
  }

  public int capacity()
  {
    return capacity;
  }

  public boolean addAll(Collection<? extends E> c)
  {
    if ((c == null) || (c.isEmpty()))
      return false;
    Object[] a = c.toArray();
    int numNew = a.length;
    ensureCapacity(size + numNew);
    System.arraycopy(a, 0, elementData, size, numNew);
    size += numNew;
    return true;
  }

  public boolean containsAll(Collection<?> c)
  {
    if (c == null)
      return false;
    if (c.isEmpty())
      return true;
    Iterator e = c.iterator();
    while (e.hasNext())
      if (!contains(e.next()))
        return false;
    return true;
  }

  public boolean retainAll(Collection<?> c)
  {
    if (c == null)
      return false;
    boolean modified = false;
    Iterator e = iterator();
    while (e.hasNext()) {
      if (c.contains(e.next()))
        continue;
      e.remove();
      modified = true;
    }
    return modified;
  }

  public boolean removeAll(Collection<?> c)
  {
    if ((c == null) || (c.isEmpty()))
      return false;
    boolean modified = false;
    Iterator e = iterator();
    while (e.hasNext()) {
      if (!c.contains(e.next()))
        continue;
      e.remove();
      modified = true;
    }
    return modified;
  }

  public Object[] toArray()
  {
    Object[] r = new Object[size];
    if (size > 0)
      System.arraycopy(elementData, 0, r, 0, size);
    return r;
  }

  public <T> T[] toArray(T[] a)
  {
    Object[] r = a.length >= size ? a : (Object[])(Object[])Array.newInstance(a.getClass().getComponentType(), size);
    if (size > 0)
      System.arraycopy(elementData, 0, r, 0, size);
    if (r.length > size)
      r[size] = null;
    return r;
  }

  public Iterator<E> iterator()
  {
    return new LazyItr(null);
  }

  public ListIterator<E> listIterator()
  {
    return new LazyListItr(0);
  }

  public ListIterator<E> listIterator(int index)
  {
    return new LazyListItr(index);
  }

  public String toString()
  {
    if (size == 0) {
      return "[]";
    }
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < size; i++)
    {
      Object e = elementData[i];
      sb.append(e == this ? "this" : e);

      if (i == size - 1)
        sb.append(']').toString();
      else
        sb.append(", ");
    }
    return sb.toString();
  }

  public List<E> subList(int fromIndex, int toIndex)
  {
    throw new UnsupportedOperationException();
  }

  private class LazyListItr extends LazyArrayList<E>.LazyItr
    implements ListIterator<E>
  {
    LazyListItr(int index)
    {
      super(null);
      cursor = index;
    }

    public boolean hasPrevious()
    {
      return cursor > 0;
    }

    public E previous()
    {
      int i = cursor - 1;
      Object previous = get(i);
      lastRet = (this.cursor = i);
      return previous;
    }

    public int nextIndex()
    {
      return cursor;
    }

    public int previousIndex()
    {
      return cursor - 1;
    }

    public void set(E e)
    {
      if (lastRet == -1)
        throw new IllegalStateException();
      set(lastRet, e);
    }

    public void add(E e)
    {
      add(cursor++, e);
      lastRet = -1;
    }
  }

  private class LazyItr
    implements Iterator<E>
  {
    int cursor = 0;
    int lastRet = -1;

    private LazyItr() {
    }
    public boolean hasNext() {
      return cursor < size();
    }

    public E next()
    {
      Object next = get(cursor);
      lastRet = (cursor++);
      return next;
    }

    public void remove()
    {
      if (lastRet == -1)
        throw new IllegalStateException();
      remove(lastRet);
      if (lastRet < cursor)
        cursor -= 1;
      lastRet = -1;
    }
  }

  private static class PoolableLazyArrayListFactory
    implements PoolableObjectFactory
  {
    public Object makeObject()
      throws Exception
    {
      return new LazyArrayList();
    }

    public void destroyObject(Object obj)
      throws Exception
    {
      ((LazyArrayList)obj).clear();
    }

    public boolean validateObject(Object obj)
    {
      return true;
    }

    public void activateObject(Object obj)
      throws Exception
    {
    }

    public void passivateObject(Object obj)
      throws Exception
    {
      ((LazyArrayList)obj).clear();
    }
  }
}