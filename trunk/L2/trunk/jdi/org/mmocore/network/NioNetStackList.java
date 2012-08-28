package org.mmocore.network;

public final class NioNetStackList<E>
{
  private final NioNetStackList<E>.NioNetStackNode _start = new NioNetStackNode(null);

  private final NioNetStackList<E>.NioNetStackNodeBuf _buf = new NioNetStackNodeBuf();

  private NioNetStackList<E>.NioNetStackNode _end = new NioNetStackNode(null);

  public NioNetStackList()
  {
    clear();
  }

  public final void addLast(E elem)
  {
    NioNetStackNode newEndNode = _buf.removeFirst();
    NioNetStackNode.access$102(_end, elem);
    NioNetStackNode.access$202(_end, newEndNode);
    _end = newEndNode;
  }

  public final E removeFirst()
  {
    NioNetStackNode old = _start._next;
    Object value = old._value;
    NioNetStackNode.access$202(_start, old._next);
    _buf.addLast(old);
    return value;
  }

  public final boolean isEmpty()
  {
    return _start._next == _end;
  }

  public final void clear()
  {
    NioNetStackNode.access$202(_start, _end);
  }

  private final class NioNetStackNodeBuf
  {
    private final NioNetStackList<E>.NioNetStackNode _start = new NioNetStackList.NioNetStackNode(NioNetStackList.this, null);

    private NioNetStackList<E>.NioNetStackNode _end = new NioNetStackList.NioNetStackNode(NioNetStackList.this, null);

    NioNetStackNodeBuf()
    {
      NioNetStackList.NioNetStackNode.access$202(_start, _end);
    }

    final void addLast(NioNetStackList<E>.NioNetStackNode node)
    {
      NioNetStackList.NioNetStackNode.access$202(node, null);
      NioNetStackList.NioNetStackNode.access$102(node, null);
      NioNetStackList.NioNetStackNode.access$202(_end, node);
      _end = node;
    }

    final NioNetStackList<E>.NioNetStackNode removeFirst()
    {
      if (NioNetStackList.NioNetStackNode.access$200(_start) == _end) {
        return new NioNetStackList.NioNetStackNode(NioNetStackList.this, null);
      }
      NioNetStackList.NioNetStackNode old = NioNetStackList.NioNetStackNode.access$200(_start);
      NioNetStackList.NioNetStackNode.access$202(_start, NioNetStackList.NioNetStackNode.access$200(old));
      return old;
    }
  }

  private final class NioNetStackNode
  {
    private NioNetStackList<E>.NioNetStackNode _next;
    private E _value;

    private NioNetStackNode()
    {
    }
  }
}