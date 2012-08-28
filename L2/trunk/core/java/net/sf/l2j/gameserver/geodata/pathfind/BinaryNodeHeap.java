package net.sf.l2j.gameserver.geodata.pathfind;


public class BinaryNodeHeap
{
  private final GeoNode[] _list;
  private int _size;

  public BinaryNodeHeap(int size)
  {
    this._list = new GeoNode[size + 1];
    this._size = 0;
  }

  public void add(GeoNode n)
  {
    this._size += 1;
    int pos = this._size;
    this._list[pos] = n;
    while (pos != 1)
    {
      int p2 = pos / 2;
      if (this._list[pos].getCost() > this._list[p2].getCost())
        break;
      GeoNode temp = this._list[p2];
      this._list[p2] = this._list[pos];
      this._list[pos] = temp;
      pos = p2;
    }
  }

  public GeoNode removeFirst()
  {
    GeoNode first = this._list[1];
    this._list[1] = this._list[this._size];
    this._list[this._size] = null;
    this._size -= 1;
    int pos = 1;
    while (true)
    {
      int cpos = pos;
      int dblcpos = cpos * 2;
      if (dblcpos + 1 <= this._size)
      {
        if (this._list[cpos].getCost() >= this._list[dblcpos].getCost()) pos = dblcpos;
        if (this._list[pos].getCost() >= this._list[(dblcpos + 1)].getCost()) pos = dblcpos + 1;
      }
      else if (dblcpos <= this._size)
      {
        if (this._list[cpos].getCost() >= this._list[dblcpos].getCost()) pos = dblcpos;
      }

      if (cpos == pos)
        break;
      GeoNode temp = this._list[cpos];
      this._list[cpos] = this._list[pos];
      this._list[pos] = temp;
    }

    return first;
  }

  public boolean contains(GeoNode n)
  {
    if (this._size == 0) return false;
    for (int i = 1; i <= this._size; i++)
    {
      if (this._list[i].equals(n)) return true;
    }
    return false;
  }

  public boolean isEmpty()
  {
    return this._size == 0;
  }
}