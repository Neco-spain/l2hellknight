package net.sf.l2j.gameserver.geodata.pathfind;

public abstract class AbstractNode
{
  private AbstractNodeLoc _loc;
  private AbstractNode _parent;

  public AbstractNode(AbstractNodeLoc loc)
  {
    this._loc = loc;
  }

  public void setParent(AbstractNode p)
  {
    this._parent = p;
  }

  public AbstractNode getParent()
  {
    return this._parent;
  }

  public AbstractNodeLoc getLoc()
  {
    return this._loc;
  }

  public void setLoc(AbstractNodeLoc l)
  {
    this._loc = l;
  }

  @SuppressWarnings("unused")
public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (this._loc == null ? 0 : this._loc.hashCode());
    return result;
  }

  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof AbstractNode)) return false;
    AbstractNode other = (AbstractNode)obj;
    if (this._loc == null)
    {
      if (other._loc != null) return false;
    }
    else if (!this._loc.equals(other._loc)) return false;
    return true;
  }
}