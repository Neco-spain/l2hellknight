package net.sf.l2j.gameserver.geodata.pathfind;

public class GeoNodeLoc extends AbstractNodeLoc
{
  private final short _x;
  private final short _y;
  private final short _z;

  public GeoNodeLoc(short x, short y, short z)
  {
    _x = x;
    _y = y;
    _z = z;
  }

  public int getX()
  {
    return -131072 + _x * 128 + 48;
  }

  public int getY()
  {
    return -262144 + _y * 128 + 48;
  }

  public short getZ()
  {
    return _z;
  }

  public void setZ(short z)
  {
  }

  public int getNodeX()
  {
    return _x;
  }

  public int getNodeY()
  {
    return _y;
  }

  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + _x;
    result = 31 * result + _y;
    result = 31 * result + _z;
    return result;
  }

  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof GeoNodeLoc)) return false;
    GeoNodeLoc other = (GeoNodeLoc)obj;
    if (_x != other._x) return false;
    if (_y != other._y) return false;
    return _z == other._z;
  }
}