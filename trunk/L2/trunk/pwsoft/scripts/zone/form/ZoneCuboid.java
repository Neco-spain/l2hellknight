package scripts.zone.form;

import scripts.zone.L2ZoneForm;

public class ZoneCuboid extends L2ZoneForm
{
  private int _x1;
  private int _x2;
  private int _y1;
  private int _y2;
  private int _z1;
  private int _z2;

  public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2)
  {
    _x1 = x1;
    _x2 = x2;
    if (_x1 > _x2)
    {
      _x1 = x2; _x2 = x1;
    }

    _y1 = y1;
    _y2 = y2;
    if (_y1 > _y2)
    {
      _y1 = y2; _y2 = y1;
    }

    _z1 = z1;
    _z2 = z2;
    if (_z1 > _z2)
    {
      _z1 = z2; _z2 = z1;
    }
  }

  public boolean isInsideZone(int x, int y, int z)
  {
    return (x >= _x1) && (x <= _x2) && (y >= _y1) && (y <= _y2) && (z >= _z1) && (z <= _z2);
  }

  public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
  {
    if (isInsideZone(ax1, ay1, _z2 - 1)) return true;
    if (isInsideZone(ax1, ay2, _z2 - 1)) return true;
    if (isInsideZone(ax2, ay1, _z2 - 1)) return true;
    if (isInsideZone(ax2, ay2, _z2 - 1)) return true;

    if ((_x1 > ax1) && (_x1 < ax2) && (_y1 > ay1) && (_y1 < ay2)) return true;
    if ((_x1 > ax1) && (_x1 < ax2) && (_y2 > ay1) && (_y2 < ay2)) return true;
    if ((_x2 > ax1) && (_x2 < ax2) && (_y1 > ay1) && (_y1 < ay2)) return true;
    if ((_x2 > ax1) && (_x2 < ax2) && (_y2 > ay1) && (_y2 < ay2)) return true;

    if (lineIntersectsLine(_x1, _y1, _x2, _y1, ax1, ay1, ax1, ay2)) return true;
    if (lineIntersectsLine(_x1, _y1, _x2, _y1, ax2, ay1, ax2, ay2)) return true;
    if (lineIntersectsLine(_x1, _y2, _x2, _y2, ax1, ay1, ax1, ay2)) return true;
    if (lineIntersectsLine(_x1, _y2, _x2, _y2, ax2, ay1, ax2, ay2)) return true;

    if (lineIntersectsLine(_x1, _y1, _x1, _y2, ax1, ay1, ax2, ay1)) return true;
    if (lineIntersectsLine(_x1, _y1, _x1, _y2, ax1, ay2, ax2, ay2)) return true;
    if (lineIntersectsLine(_x2, _y1, _x2, _y2, ax1, ay1, ax2, ay1)) return true;
    return lineIntersectsLine(_x2, _y1, _x2, _y2, ax1, ay2, ax2, ay2);
  }

  public double getDistanceToZone(int x, int y)
  {
    double shortestDist = Math.pow(_x1 - x, 2.0D) + Math.pow(_y1 - y, 2.0D);

    double test = Math.pow(_x1 - x, 2.0D) + Math.pow(_y2 - y, 2.0D);
    if (test < shortestDist) shortestDist = test;

    test = Math.pow(_x2 - x, 2.0D) + Math.pow(_y1 - y, 2.0D);
    if (test < shortestDist) shortestDist = test;

    test = Math.pow(_x2 - x, 2.0D) + Math.pow(_y2 - y, 2.0D);
    if (test < shortestDist) shortestDist = test;

    return Math.sqrt(shortestDist);
  }

  public int getLowZ()
  {
    return _z1;
  }

  public int getHighZ()
  {
    return _z2;
  }
}