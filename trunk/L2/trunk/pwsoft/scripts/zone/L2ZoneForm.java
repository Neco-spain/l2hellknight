package scripts.zone;

public abstract class L2ZoneForm
{
  public abstract boolean isInsideZone(int paramInt1, int paramInt2, int paramInt3);

  public abstract boolean intersectsRectangle(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public abstract double getDistanceToZone(int paramInt1, int paramInt2);

  public abstract int getLowZ();

  public abstract int getHighZ();

  protected boolean lineIntersectsLine(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2)
  {
    int s1 = sameSide(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2);
    int s2 = sameSide(bx1, by1, bx2, by2, ax1, ay1, ax2, ay1);

    return (s1 <= 0) && (s2 <= 0);
  }

  protected int sameSide(double x0, double y0, double x1, double y1, double px0, double py0, double px1, double py1)
  {
    int sameSide = 0;

    double dx = x1 - x0;
    double dy = y1 - y0;
    double dx1 = px0 - x0;
    double dy1 = py0 - y0;
    double dx2 = px1 - x1;
    double dy2 = py1 - y1;

    double c1 = dx * dy1 - dy * dx1;
    double c2 = dx * dy2 - dy * dx2;

    if ((c1 != 0.0D) && (c2 != 0.0D))
      sameSide = (c1 < 0.0D ? 1 : 0) != (c2 < 0.0D ? 1 : 0) ? -1 : 1;
    else if ((dx == 0.0D) && (dx1 == 0.0D) && (dx2 == 0.0D))
      sameSide = (!isBetween(y0, y1, py0)) && (!isBetween(y0, y1, py1)) ? 1 : 0;
    else if ((dy == 0.0D) && (dy1 == 0.0D) && (dy2 == 0.0D)) {
      sameSide = (!isBetween(x0, x1, px0)) && (!isBetween(x0, x1, px1)) ? 1 : 0;
    }
    return sameSide;
  }

  protected boolean isBetween(double a, double b, double c)
  {
    return (c >= a) && (c <= b);
  }
}