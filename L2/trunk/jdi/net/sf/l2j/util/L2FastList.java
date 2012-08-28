package net.sf.l2j.util;

import javolution.util.FastList;
import javolution.util.FastList.Node;

public class L2FastList<T> extends FastList<T>
{
  static final long serialVersionUID = 1L;

  public final boolean forEach(I2ForEach<T> func, boolean sync)
  {
    if (sync)
      synchronized (this) { return forEachP(func);
      }
    return forEachP(func);
  }

  private boolean forEachP(I2ForEach<T> func) {
    FastList.Node e = head(); for (FastList.Node end = tail(); (e = func.getNext(e)) != end; )
      if (!func.ForEach(e.getValue())) return false;
    return true;
  }

  public static abstract interface I2ForEach<T>
  {
    public abstract boolean ForEach(T paramT);

    public abstract FastList.Node<T> getNext(FastList.Node<T> paramNode);
  }
}