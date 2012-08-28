package net.sf.l2j.util;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

public class L2FastMap<K, V> extends FastMap<K, V>
{
  static final long serialVersionUID = 1L;

  public final boolean ForEach(I2ForEach<K, V> func, boolean sync)
  {
    if (sync)
      synchronized (this) { return forEachP(func);
      }
    return forEachP(func);
  }

  private boolean forEachP(I2ForEach<K, V> func) {
    FastMap.Entry e = head(); for (FastMap.Entry end = tail(); (e = func.getNext(e)) != end; )
      if (!func.forEach(e.getKey(), e.getValue())) return false;
    return true;
  }

  public static abstract interface I2ForEach<K, V>
  {
    public abstract boolean forEach(K paramK, V paramV);

    public abstract FastMap.Entry<K, V> getNext(FastMap.Entry<K, V> paramEntry);
  }
}