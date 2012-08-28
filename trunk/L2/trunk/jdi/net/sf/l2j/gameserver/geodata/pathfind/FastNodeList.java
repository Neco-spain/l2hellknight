package net.sf.l2j.gameserver.geodata.pathfind;

public class FastNodeList
{
  private AbstractNode[] _list;
  private int _size;

  public FastNodeList(int size)
  {
    _list = new AbstractNode[size];
  }

  public void add(AbstractNode n)
  {
    _list[(_size++)] = n;
  }

  public boolean contains(AbstractNode n)
  {
    for (int i = 0; i < _size; i++)
      if (_list[i].equals(n)) return true;
    return false;
  }

  public boolean containsRev(AbstractNode n)
  {
    for (int i = _size - 1; i >= 0; i--)
      if (_list[i].equals(n)) return true;
    return false;
  }
}