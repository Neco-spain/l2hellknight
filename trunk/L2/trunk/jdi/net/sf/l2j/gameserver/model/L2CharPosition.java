package net.sf.l2j.gameserver.model;

public final class L2CharPosition
{
  public final int x;
  public final int y;
  public final int z;
  public final int heading;

  public L2CharPosition(int pX, int pY, int pZ, int pHeading)
  {
    x = pX;
    y = pY;
    z = pZ;
    heading = pHeading;
  }

  public L2CharPosition(Location loc) {
    x = loc.getX();
    y = loc.getY();
    z = loc.getZ();
    heading = loc.getHeading();
  }
}