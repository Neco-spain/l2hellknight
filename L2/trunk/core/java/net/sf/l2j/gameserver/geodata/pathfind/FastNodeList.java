package net.sf.l2j.gameserver.geodata.pathfind;


public class FastNodeList
{
  private AbstractNode[] _list;
  private int _size;

  public FastNodeList(int size)
  {
    this._list = new AbstractNode[size];
  }

  public void add(AbstractNode n)
  {
    this._list[(this._size++)] = n;
  }

  public boolean contains(AbstractNode n)
  {
    for (int i = 0; i < this._size; i++)
      if (this._list[i].equals(n)) return true;
    return false;
  }

  public boolean containsRev(AbstractNode n)
  {
    for (int i = this._size - 1; i >= 0; i--)
      if (this._list[i].equals(n)) return true;
    return false;
  }
}