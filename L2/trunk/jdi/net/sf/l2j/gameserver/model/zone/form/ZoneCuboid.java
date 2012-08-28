package net.sf.l2j.gameserver.model.zone.form;

import java.awt.Rectangle;
import net.sf.l2j.gameserver.model.zone.L2ZoneForm;

public class ZoneCuboid extends L2ZoneForm
{
  private int _z1;
  private int _z2;
  Rectangle _r;

  public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2)
  {
    int _x1 = Math.min(x1, x2);
    int _x2 = Math.max(x1, x2);
    int _y1 = Math.min(y1, y2);
    int _y2 = Math.max(y1, y2);

    _z1 = Math.min(z1, z2);
    _z2 = Math.max(z1, z2);

    _r = new Rectangle(_x1, _y1, _x2 - _x1, _y2 - _y1);
  }

  public boolean isInsideZone(int x, int y, int z)
  {
    return (_r.contains(x, y)) && (z >= _z1) && (z <= _z2);
  }

  public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
  {
    return _r.intersects(Math.min(ax1, ax2), Math.min(ay1, ay2), Math.abs(ax2 - ax1), Math.abs(ay2 - ay1));
  }

  public double getDistanceToZone(int x, int y)
  {
    int _x1 = _r.x;
    int _x2 = _r.x + _r.width;
    int _y1 = _r.y;
    int _y2 = _r.y + _r.height;

    double shortestDist = Math.pow(_x1 - x, 2.0D) + Math.pow(_y1 - y, 2.0D);

    double test = Math.pow(_x1 - x, 2.0D) + Math.pow(_y2 - y, 2.0D);
    if (test < shortestDist) {
      shortestDist = test;
    }
    test = Math.pow(_x2 - x, 2.0D) + Math.pow(_y1 - y, 2.0D);
    if (test < shortestDist) {
      shortestDist = test;
    }
    test = Math.pow(_x2 - x, 2.0D) + Math.pow(_y2 - y, 2.0D);
    if (test < shortestDist) {
      shortestDist = test;
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