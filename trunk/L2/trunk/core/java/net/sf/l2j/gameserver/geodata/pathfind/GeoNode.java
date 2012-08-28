package net.sf.l2j.gameserver.geodata.pathfind;


public class GeoNode extends AbstractNode
{
  private final int _neighborsIdx;
  private short _cost;
  private GeoNode[] _neighbors;

  public GeoNode(AbstractNodeLoc Loc, int Neighbors_idx)
  {
    super(Loc);
    this._neighborsIdx = Neighbors_idx;
  }

  public short getCost()
  {
    return this._cost;
  }

  public void setCost(int cost)
  {
    this._cost = (short)cost;
  }

  public GeoNode[] getNeighbors()
  {
    return this._neighbors;
  }

  public void attachNeighbors()
  {
    if (getLoc() == null) this._neighbors = null; else
      this._neighbors = GeoPathFinding.getInstance().readNeighbors(this, this._neighborsIdx);
  }
}