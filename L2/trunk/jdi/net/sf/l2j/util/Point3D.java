package net.sf.l2j.util;

import java.io.Serializable;

public class Point3D
  implements Serializable
{
  private static final long serialVersionUID = 4638345252031872576L;
  private volatile int _x;
  private volatile int _y;
  private volatile int _z;

  public Point3D(int pX, int pY, int pZ)
  {
    _x = pX;
    _y = pY;
    _z = pZ;
  }

  public Point3D(int pX, int pY)
  {
    _x = pX;
    _y = pY;
    _z = 0;
  }

  public Point3D(Point3D worldPosition)
  {
    synchronized (worldPosition)
    {
      _x = worldPosition._x;
      _y = worldPosition._y;
      _z = worldPosition._z;
    }
  }

  public synchronized void setTo(Point3D point)
  {
    synchronized (point)
    {
      _x = point._x;
      _y = point._y;
      _z = point._z;
    }
  }

  public String toString()
  {
    return "(" + _x + ", " + _y + ", " + _z + ")";
  }

  public int hashCode()
  {
    return _x ^ _y ^ _z;
  }

  public synchronized boolean equals(Object o)
  {
    if ((o instanceof Point3D))
    {
      Point3D point3D = (Point3D)o;
      boolean ret;
      synchronized (point3D)
      {
        ret = (point3D._x == _x) && (point3D._y == _y) && (point3D._z == _z);
      }
      return ret;
    }
    return false;
  }

  public synchronized boolean equals(int pX, int pY, int pZ)
  {
    return (_x == pX) && (_y == pY) && (_z == pZ);
  }

  public synchronized long distanceSquaredTo(Point3D point)
  {
    long dx;
    long dy;
    synchronized (point)
    {
      dx = _x - point._x;
      dy = _y - point._y;
    }
    return dx * dx + dy * dy;
  }

  public static long distanceSquared(Point3D point1, Point3D point2)
  {
    long dx;
    long dy;
    synchronized (point1)
    {
      synchronized (point2)
      {
        dx = _x - point2._x;
        dy = _y - point2._y;
      }
    }
    return dx * dx + dy * dy;
  }

  public static boolean distanceLessThan(Point3D point1, Point3D point2, double distance)
  {
    return distanceSquared(point1, point2) < distance * distance;
  }

  public int getX()
  {
    return _x;
  }

  public synchronized void setX(int pX)
  {
    _x = pX;
  }

  public int getY()
  {
    return _y;
  }

  public synchronized void setY(int pY)
  {
    _y = pY;
  }

  public int getZ()
  {
    return _z;
  }

  public synchronized void setZ(int pZ)
  {
    _z = pZ;
  }

  public synchronized void setXYZ(int pX, int pY, int pZ)
  {
    _x = pX;
    _y = pY;
    _z = pZ;
  }
}