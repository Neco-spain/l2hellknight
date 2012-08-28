package scripts.zone.form;

import scripts.zone.L2ZoneForm;

public class ZoneCylinder extends L2ZoneForm
{
  private int _x;
  private int _y;
  private int _z1;
  private int _z2;
  private int _rad;
  private int _radS;

  public ZoneCylinder(int x, int y, int z1, int z2, int rad)
  {
    _x = x;
    _y = y;
    _z1 = z1;
    _z2 = z2;
    _rad = rad;
    _radS = (rad * rad);
  }

  public boolean isInsideZone(int x, int y, int z)
  {
    return (Math.pow(_x - x, 2.0D) + Math.pow(_y - y, 2.0D) <= _radS) && (z >= _z1) && (z <= _z2);
  }

  public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
  {
    if ((_x > ax1) && (_x < ax2) && (_y > ay1) && (_y < ay2)) return true;

    if (Math.pow(ax1 - _x, 2.0D) + Math.pow(ay1 - _y, 2.0D) < _radS) return true;
    if (Math.pow(ax1 - _x, 2.0D) + Math.pow(ay2 - _y, 2.0D) < _radS) return true;
    if (Math.pow(ax2 - _x, 2.0D) + Math.pow(ay1 - _y, 2.0D) < _radS) return true;
    if (Math.pow(ax2 - _x, 2.0D) + Math.pow(ay2 - _y, 2.0D) < _radS) return true;

    if ((_x > ax1) && (_x < ax2))
    {
      if (Math.abs(_y - ay2) < _rad) return true;
      if (Math.abs(_y - ay1) < _rad) return true;
    }
    if ((_y > ay1) && (_y < ay2))
    {
      if (Math.abs(_x - ax2) < _rad) return true;
      if (Math.abs(_x - ax1) < _rad) return true;
    }

    return false;
  }

  public double getDistanceToZone(int x, int y)
  {
    return Math.sqrt(Math.pow(_x - x, 2.0D) + Math.pow(_y - y, 2.0D)) - _rad;
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