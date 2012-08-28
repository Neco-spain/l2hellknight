package l2m.commons.geometry;

import l2m.commons.lang.ArrayUtils;

public class Polygon extends AbstractShape
{
  protected Point2D[] points = Point2D.EMPTY_ARRAY;

  public Polygon add(int x, int y)
  {
    add(new Point2D(x, y));
    return this;
  }

  public Polygon add(Point2D p)
  {
    if (points.length == 0)
    {
      min.y = p.y;
      min.x = p.x;
      max.x = p.x;
      max.y = p.y;
    }
    else
    {
      min.y = Math.min(min.y, p.y);
      min.x = Math.min(min.x, p.x);
      max.x = Math.max(max.x, p.x);
      max.y = Math.max(max.y, p.y);
    }
    points = ((Point2D[])ArrayUtils.add(points, p));
    return this;
  }

  public Polygon setZmax(int z)
  {
    max.z = z;
    return this;
  }

  public Polygon setZmin(int z)
  {
    min.z = z;
    return this;
  }

  public boolean isInside(int x, int y)
  {
    if ((x < min.x) || (x > max.x) || (y < min.y) || (y > max.y)) {
      return false;
    }
    int hits = 0;
    int npoints = points.length;
    Point2D last = points[(npoints - 1)];

    for (int i = 0; i < npoints; i++)
    {
      Point2D cur = points[i];

      if (cur.y != last.y)
      {
        int leftx;
        int leftx;
        if (cur.x < last.x)
        {
          if (x >= last.x) {
            break label314;
          }
          leftx = cur.x;
        }
        else
        {
          if (x >= cur.x) {
            break label314;
          }
          leftx = last.x;
        }
        double test2;
        double test1;
        double test2;
        if (cur.y < last.y)
        {
          if ((y < cur.y) || (y >= last.y)) {
            break label314;
          }
          if (x < leftx)
          {
            hits++;
            break label314;
          }
          double test1 = x - cur.x;
          test2 = y - cur.y;
        }
        else
        {
          if ((y < last.y) || (y >= cur.y)) {
            break label314;
          }
          if (x < leftx)
          {
            hits++;
            break label314;
          }
          test1 = x - last.x;
          test2 = y - last.y;
        }

        if (test1 < test2 / (last.y - cur.y) * (last.x - cur.x))
        {
          hits++;
        }
      }
      label314: last = cur;
    }

    return (hits & 0x1) != 0;
  }

  public boolean validate()
  {
    if (points.length < 3) {
      return false;
    }

    if (points.length > 3)
    {
      for (int i = 1; i < points.length; i++)
      {
        int ii = i + 1 < points.length ? i + 1 : 0;

        for (int n = i; n < points.length; n++) {
          if (Math.abs(n - i) <= 1)
            continue;
          int nn = n + 1 < points.length ? n + 1 : 0;
          if (GeometryUtils.checkIfLineSegementsIntersects(points[i], points[ii], points[n], points[nn])) return false;
        }
      }
    }
    return true;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < points.length; i++)
    {
      sb.append(points[i]);
      if (i < points.length - 1)
        sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }
}