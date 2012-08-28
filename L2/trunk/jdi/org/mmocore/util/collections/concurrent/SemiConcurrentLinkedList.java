package org.mmocore.util.collections.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SemiConcurrentLinkedList<E>
  implements Iterable<E>
{
  private volatile SemiConcurrentLinkedList<E>.Node<E> _start = new Node();
  private volatile SemiConcurrentLinkedList<E>.Node<E> _end = new Node();
  private int _size;

  public SemiConcurrentLinkedList()
  {
    clear();
  }

  public void addLast(E elem)
  {
    Node oldEndNode = _end;

    Node.access$002(oldEndNode, elem);

    Node newEndNode = new Node(oldEndNode, null, null);

    oldEndNode.setNext(newEndNode);

    _end = newEndNode;

    _size += 1;
  }

  public void remove(SemiConcurrentLinkedList<E>.Node<E> node)
  {
    Node previous = node.getPrevious();
    Node next = node.getNext();

    previous.setNext(next);

    next.setPrevious(previous);

    _size -= 1;
  }

  public int size()
  {
    return _size;
  }

  public final boolean isEmpty()
  {
    return _size == 0;
  }

  public void clear()
  {
    _start.setNext(_end);
    _end.setPrevious(_start);
  }

  public SemiConcurrentLinkedList<E>.Node<E> getStart()
  {
    return _start;
  }

  public SemiConcurrentLinkedList<E>.Node<E> getEnd()
  {
    return _end;
  }

  public Iterator<E> iterator()
  {
    return new SemiConcurrentIterator(_start);
  }

  public final class Node<T>
  {
    private SemiConcurrentLinkedList<E>.Node<T> _previous;
    private SemiConcurrentLinkedList<E>.Node<T> _next;
    private T _value;

    protected Node()
    {
    }

    protected Node()
    {
      _value = value;
    }

    protected Node(SemiConcurrentLinkedList<E>.Node<T> previous, T next)
    {
      _previous = previous;
      _next = next;
      _value = value;
    }

    public SemiConcurrentLinkedList<E>.Node<T> getNext()
    {
      return _next;
    }

    protected void setNext(SemiConcurrentLinkedList<E>.Node<T> node)
    {
      _next = node;
    }

    public SemiConcurrentLinkedList<E>.Node<T> getPrevious()
    {
      return _previous;
    }

    protected void setPrevious(SemiConcurrentLinkedList<E>.Node<T> node)
    {
      _previous = node;
    }

    public T getValue()
    {
      return _value;
    }
  }

  public class SemiConcurrentIterator
    implements Iterator<E>
  {
    private SemiConcurrentLinkedList<E>.Node<E> _current;

    protected SemiConcurrentIterator()
    {
      _current = start;
    }

    public boolean hasNext()
    {
      return _current.getNext() != _end;
    }

    public E next()
    {
      _current = _current.getNext();
      if (_current == _end)
      {
        throw new NoSuchElementException();
      }
      return _current.getValue();
    }

    public void remove()
    {
      remove(_current);
      _current = _current.getPrevious();
    }
  }
}