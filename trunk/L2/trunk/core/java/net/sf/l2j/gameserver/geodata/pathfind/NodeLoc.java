package net.sf.l2j.gameserver.geodata.pathfind;

import net.sf.l2j.gameserver.geodata.GeoData;

public class NodeLoc extends AbstractNodeLoc
{
  private int _x;
  private int _y;
  private short _geoHeightAndNSWE;

  public NodeLoc(int x, int y, short z)
  {
    this._x = x;
    this._y = y;
    this._geoHeightAndNSWE = GeoData.getInstance().getHeightAndNSWE(x, y, z);
  }

  public void set(int x, int y, short z)
  {
    this._x = x;
    this._y = y;
    this._geoHeightAndNSWE = GeoData.getInstance().getHeightAndNSWE(x, y, z);
  }

  public short getNSWE()
  {
    return (short)(this._geoHeightAndNSWE & 0xF);
  }

  public int getX()
  {
    return (this._x << 4) + -131072;
  }

  public int getY()
  {
    return (this._y << 4) + -262144;
  }

  public short getZ()
  {
    short height = (short)(this._geoHeightAndNSWE & 0xFFF0);
    return (short)(height >> 1);
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
    result = 31 * result + this._geoHeightAndNSWE;
    return result;
  }

  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof NodeLoc)) return false;
    NodeLoc other = (NodeLoc)obj;
    if (this._x != other._x) return false;
    if (this._y != other._y) return false;
    return this._geoHeightAndNSWE == other._geoHeightAndNSWE;
  }
}