package net.sf.l2j.gameserver.geodata.pathfind;


public class GeoNodeLoc extends AbstractNodeLoc
{
  private final short _x;
  private final short _y;
  private final short _z;

  public GeoNodeLoc(short x, short y, short z)
  {
    this._x = x;
    this._y = y;
    this._z = z;
  }

  public int getX()
  {
    return -131072 + this._x * 128 + 48;
  }

  public int getY()
  {
    return -262144 + this._y * 128 + 48;
  }

  public short getZ()
  {
    return this._z;
  }

  public void setZ(short z)
  {
  }

  public int getNodeX()
  {
    return this._x;
  }

  public int getNodeY()
  {
    return this._y;
  }

  @SuppressWarnings("unused")
public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + this._x;
    result = 31 * result + this._y;
    result = 31 * result + this._z;
    return result;
  }

  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof GeoNodeLoc)) return false;
    GeoNodeLoc other = (GeoNodeLoc)obj;
    if (this._x != other._x) return false;
    if (this._y != other._y) return false;
    return this._z == other._z;
  }
}