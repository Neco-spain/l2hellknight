package l2m.commons.collections;

import java.util.Iterator;
import java.util.List;

public class JoinedIterator<E>
  implements Iterator<E>
{
  private Iterator<E>[] _iterators;
  private int _currentIteratorIndex;
  private Iterator<E> _currentIterator;
  private Iterator<E> _lastUsedIterator;

  public JoinedIterator(List<Iterator<E>> iterators)
  {
    this((Iterator[])iterators.toArray(new Iterator[iterators.size()]));
  }

  public JoinedIterator(Iterator[] iterators)
  {
    if (iterators == null)
      throw new NullPointerException("Unexpected NULL iterators argument");
    _iterators = iterators;
  }

  public boolean hasNext()
  {
    updateCurrentIterator();
    return _currentIterator.hasNext();
  }

  public E next()
  {
    updateCurrentIterator();
    return _currentIterator.next();
  }

  public void remove()
  {
    updateCurrentIterator();
    _lastUsedIterator.remove();
  }

  protected void updateCurrentIterator()
  {
    if (_currentIterator == null)
    {
      if (_iterators.length == 0)
        _currentIterator = EmptyIterator.getInstance();
      else {
        _currentIterator = _iterators[0];
      }

      _lastUsedIterator = _currentIterator;
    }

    while ((!_currentIterator.hasNext()) && (_currentIteratorIndex < _iterators.length - 1))
    {
      _currentIteratorIndex += 1;
      _currentIterator = _iterators[_currentIteratorIndex];
    }
  }
}