package net.sf.l2j.gameserver.model.zone.form;

import java.awt.Polygon;
import net.sf.l2j.gameserver.model.zone.L2ZoneForm;

public class ZoneNPoly extends L2ZoneForm
{
  private Polygon _p;
  private int _z1;
  private int _z2;

  public ZoneNPoly(int[] x, int[] y, int z1, int z2)
  {
    _p = new Polygon(x, y, x.length);
    _z1 = Math.min(z1, z2);
    _z2 = Math.max(z1, z2);
  }

  public boolean isInsideZone(int x, int y, int z)
  {
    return (_p.contains(x, y)) && (z >= _z1) && (z <= _z2);
  }

  public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
  {
    return _p.intersects(Math.min(ax1, ax2), Math.min(ay1, ay2), Math.abs(ax2 - ax1), Math.abs(ay2 - ay1));
  }

  public double getDistanceToZone(int x, int y)
  {
    int[] _x = _p.xpoints;
    int[] _y = _p.ypoints;

    double shortestDist = Math.pow(_x[0] - x, 2.0D) + Math.pow(_y[0] - y, 2.0D);

    for (int i = 1; i < _p.npoints; i++)
    {
      double test = Math.pow(_x[i] - x, 2.0D) + Math.pow(_y[i] - y, 2.0D);
      if (test < shortestDist) {
        shortestDist = test;
      }
    }
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