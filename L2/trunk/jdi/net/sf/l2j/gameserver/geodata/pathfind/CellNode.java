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
    return _isInUse;
  }

  public void setInUse()
  {
    _isInUse = true;
  }

  public CellNode getNext()
  {
    return _next;
  }

  public void setNext(CellNode next)
  {
    _next = next;
  }

  public float getCost()
  {
    return _cost;
  }

  public void setCost(double cost)
  {
    _cost = (float)cost;
  }

  public void free()
  {
    setParent(null);
    _cost = -1000.0F;
    _isInUse = false;
    _next = null;
  }
}