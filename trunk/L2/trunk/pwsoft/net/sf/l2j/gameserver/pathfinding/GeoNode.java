package net.sf.l2j.gameserver.pathfinding;

import javolution.util.FastTable;

public class GeoNode
{
  private final AbstractNodeLoc _loc;
  private final int _neighborsIdx;
  private FastTable<GeoNode> _neighbors;
  private GeoNode _parent;
  private short _cost;

  public GeoNode(AbstractNodeLoc Loc, int Neighbors_idx)
  {
    _loc = Loc;
    _neighborsIdx = Neighbors_idx;
  }

  public void setParent(GeoNode p) {
    _parent = p;
  }

  public void setCost(int cost) {
    _cost = (short)cost;
  }

  public void attachNeighbors() {
    _neighbors = (_loc != null ? PathFinding.getInstance().readNeighbors(this, _neighborsIdx) : null);
  }

  public FastTable<GeoNode> getNeighbors() {
    return _neighbors;
  }

  public GeoNode getParent() {
    return _parent;
  }

  public AbstractNodeLoc getLoc() {
    return _loc;
  }

  public short getCost() {
    return _cost;
  }

  public boolean equals(Object arg0) {
    return ((arg0 instanceof GeoNode)) && (_loc.getX() == ((GeoNode)arg0).getLoc().getX()) && (_loc.getY() == ((GeoNode)arg0).getLoc().getY()) && (_loc.getZ() == ((GeoNode)arg0).getLoc().getZ());
  }

  public int getX() {
    return _loc.getX();
  }

  public int getY() {
    return _loc.getY();
  }

  public int getZ() {
    return _loc.getZ();
  }
}