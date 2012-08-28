package l2m.commons.util;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

public class TroveUtils
{
  private static final TIntObjectHashMap EMPTY_INT_OBJECT_MAP = new TIntObjectHashMapEmpty();
  public static final TIntArrayList EMPTY_INT_ARRAY_LIST = new TIntArrayListEmpty();

  public static <V> TIntObjectHashMap<V> emptyIntObjectMap()
  {
    return EMPTY_INT_OBJECT_MAP;
  }

  private static class TIntArrayListEmpty extends TIntArrayList
  {
    static final long serialVersionUID = 1L;

    TIntArrayListEmpty()
    {
      super();
    }

    public void add(int val)
    {
      throw new UnsupportedOperationException();
    }
  }

  private static class TIntObjectHashMapEmpty<V> extends TIntObjectHashMap<V>
  {
    static final long serialVersionUID = 1L;

    TIntObjectHashMapEmpty()
    {
      super();
    }

    public V put(int key, V value)
    {
      throw new UnsupportedOperationException();
    }

    public V putIfAbsent(int key, V value)
    {
      throw new UnsupportedOperationException();
    }
  }
}