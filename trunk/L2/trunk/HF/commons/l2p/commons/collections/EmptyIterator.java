package l2m.commons.collections;

import java.util.Iterator;

public class EmptyIterator<E>
  implements Iterator<E>
{
  private static final Iterator INSTANCE = new EmptyIterator();

  public static <E> Iterator<E> getInstance()
  {
    return INSTANCE;
  }

  public boolean hasNext()
  {
    return false;
  }

  public E next()
  {
    throw new UnsupportedOperationException();
  }

  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}