package net.sf.l2j.gameserver.geodata.pathfind;

public abstract class AbstractNode
{
  private AbstractNodeLoc _loc;
  private AbstractNode _parent;

  public AbstractNode(AbstractNodeLoc loc)
  {
    _loc = loc;
  }

  public void setParent(AbstractNode p)
  {
    _parent = p;
  }

  public AbstractNode getParent()
  {
    return _parent;
  }

  public AbstractNodeLoc getLoc()
  {
    return _loc;
  }

  public void setLoc(AbstractNodeLoc l)
  {
    _loc = l;
  }

  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (_loc == null ? 0 : _loc.hashCode());
    return result;
  }

  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof AbstractNode)) return false;
    AbstractNode other = (AbstractNode)obj;
    if (_loc == null)
    {
      if (other._loc != null) return false;
    }
    else if (!_loc.equals(other._loc)) return false;
    return true;
  }
}