package net.sf.l2j.gameserver.geodata.pathfind;

public class GeoNode extends AbstractNode
{
  private final int _neighborsIdx;
  private short _cost;
  private GeoNode[] _neighbors;

  public GeoNode(AbstractNodeLoc Loc, int Neighbors_idx)
  {
    super(Loc);
    _neighborsIdx = Neighbors_idx;
  }

  public short getCost()
  {
    return _cost;
  }

  public void setCost(int cost)
  {
    _cost = (short)cost;
  }

  public GeoNode[] getNeighbors()
  {
    return _neighbors;
  }

  public void attachNeighbors()
  {
    if (getLoc() == null) _neighbors = null; else
      _neighbors = GeoPathFinding.getInstance().readNeighbors(this, _neighborsIdx);
  }
}