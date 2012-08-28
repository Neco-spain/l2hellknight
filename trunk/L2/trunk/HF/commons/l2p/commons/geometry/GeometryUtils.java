package l2m.commons.geometry;

public class GeometryUtils
{
  public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d)
  {
    return checkIfLinesIntersects(a, b, c, d, null);
  }

  public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r)
  {
    if (((x == b.x) && (y == b.y)) || ((c.x == d.x) && (c.y == d.y))) {
      return false;
    }

    double Bx = b.x - x;
    double By = b.y - y;
    double Cx = c.x - x;
    double Cy = c.y - y;
    double Dx = d.x - x;
    double Dy = d.y - y;

    double distAB = Math.sqrt(Bx * Bx + By * By);

    double theCos = Bx / distAB;
    double theSin = By / distAB;
    double newX = Cx * theCos + Cy * theSin;
    Cy = (int)(Cy * theCos - Cx * theSin);
    Cx = newX;
    newX = Dx * theCos + Dy * theSin;
    Dy = (int)(Dy * theCos - Dx * theSin);
    Dx = newX;

    if (Cy == Dy) {
      return false;
    }

    double ABpos = Dx + (Cx - Dx) * Dy / (Dy - Cy);

    if (r != null)
    {
      r.x = (int)(x + ABpos * theCos);
      r.y = (int)(y + ABpos * theSin);
    }

    return true;
  }

  public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d)
  {
    return checkIfLineSegementsIntersects(a, b, c, d, null);
  }

  public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r)
  {
    if (((x == b.x) && (y == b.y)) || ((c.x == d.x) && (c.y == d.y))) {
      return false;
    }

    if (((x == c.x) && (y == c.y)) || ((b.x == c.x) && (b.y == c.y)) || ((x == d.x) && (y == d.y)) || ((b.x == d.x) && (b.y == d.y))) {
      return false;
    }

    double Bx = b.x - x;
    double By = b.y - y;
    double Cx = c.x - x;
    double Cy = c.y - y;
    double Dx = d.x - x;
    double Dy = d.y - y;

    double distAB = Math.sqrt(Bx * Bx + By * By);

    double theCos = Bx / distAB;
    double theSin = By / distAB;
    double newX = Cx * theCos + Cy * theSin;
    Cy = (int)(Cy * theCos - Cx * theSin);
    Cx = newX;
    newX = Dx * theCos + Dy * theSin;
    Dy = (int)(Dy * theCos - Dx * theSin);
    Dx = newX;

    if (((Cy < 0.0D) && (Dy < 0.0D)) || ((Cy >= 0.0D) && (Dy >= 0.0D))) {
      return false;
    }

    double ABpos = Dx + (Cx - Dx) * Dy / (Dy - Cy);

    if ((ABpos < 0.0D) || (ABpos > distAB)) {
      return false;
    }

    if (r != null)
    {
      r.x = (int)(x + ABpos * theCos);
      r.y = (int)(y + ABpos * theSin);
    }

    return true;
  }
}