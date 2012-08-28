package net.sf.l2j.gameserver.pathfinding.cellnodes;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;

public class CellNode extends AbstractNodeLoc
{
  private final int _x;
  private final int _y;
  private short _z;

  public CellNode(int x, int y, short z)
  {
    _x = x;
    _y = y;
    _z = z;
  }

  public int getX()
  {
    return (_x << 4) + Config.MAP_MIN_X;
  }

  public int getY()
  {
    return (_y << 4) + Config.MAP_MIN_Y;
  }

  public short getZ()
  {
    return _z;
  }

  public void setZ(short z)
  {
    _z = z;
  }

  public int getNodeX()
  {
    return _x;
  }

  public int getNodeY()
  {
    return _y;
  }
}