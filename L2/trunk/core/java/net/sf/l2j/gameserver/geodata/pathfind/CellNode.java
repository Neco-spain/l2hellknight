package net.sf.l2j.gameserver.geodata.pathfind;


public class CellNode extends AbstractNode
{
  private CellNode _next = null;
  private boolean _isInUse = true;
  private float _cost = -1000.0F;

  public CellNode(AbstractNodeLoc loc)
  {
    super(loc);
  }

  public boolean isInUse()
  {
    return this._isInUse;
  }

  public void setInUse()
  {
    this._isInUse = true;
  }

  public CellNode getNext()
  {
    return this._next;
  }

  public void setNext(CellNode next)
  {
    this._next = next;
  }

  public float getCost()
  {
    return this._cost;
  }

  public void setCost(double cost)
  {
    this._cost = (float)cost;
  }

  public void free()
  {
    setParent(null);
    this._cost = -1000.0F;
    this._isInUse = false;
    this._next = null;
  }
}