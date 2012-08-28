package l2p.gameserver.utils;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;

public class PositionUtils
{
  private static final int MAX_ANGLE = 360;
  private static final double FRONT_MAX_ANGLE = 100.0D;
  private static final double BACK_MAX_ANGLE = 40.0D;

  public static TargetDirection getDirectionTo(Creature target, Creature attacker)
  {
    if ((target == null) || (attacker == null))
      return TargetDirection.NONE;
    if (isBehind(target, attacker))
      return TargetDirection.BEHIND;
    if (isInFrontOf(target, attacker))
      return TargetDirection.FRONT;
    return TargetDirection.SIDE;
  }

  public static boolean isInFrontOf(Creature target, Creature attacker)
  {
    if (target == null) {
      return false;
    }

    double angleTarget = calculateAngleFrom(target, attacker);
    double angleChar = convertHeadingToDegree(target.getHeading());
    double angleDiff = angleChar - angleTarget;
    if (angleDiff <= -260.0D)
      angleDiff += 360.0D;
    if (angleDiff >= 260.0D) {
      angleDiff -= 360.0D;
    }
    return Math.abs(angleDiff) <= 100.0D;
  }

  public static boolean isBehind(Creature target, Creature attacker)
  {
    if (target == null) {
      return false;
    }

    double angleChar = calculateAngleFrom(attacker, target);
    double angleTarget = convertHeadingToDegree(target.getHeading());
    double angleDiff = angleChar - angleTarget;
    if (angleDiff <= -320.0D)
      angleDiff += 360.0D;
    if (angleDiff >= 320.0D) {
      angleDiff -= 360.0D;
    }
    return Math.abs(angleDiff) <= 40.0D;
  }

  public static boolean isFacing(Creature attacker, GameObject target, int maxAngle)
  {
    if (target == null)
      return false;
    double maxAngleDiff = maxAngle / 2;
    double angleTarget = calculateAngleFrom(attacker, target);
    double angleChar = convertHeadingToDegree(attacker.getHeading());
    double angleDiff = angleChar - angleTarget;
    if (angleDiff <= -360.0D + maxAngleDiff) angleDiff += 360.0D;
    if (angleDiff >= 360.0D - maxAngleDiff) angleDiff -= 360.0D;

    return Math.abs(angleDiff) <= maxAngleDiff;
  }

  public static int calculateHeadingFrom(GameObject obj1, GameObject obj2)
  {
    return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
  }

  public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
  {
    double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
    if (angleTarget < 0.0D)
      angleTarget = 360.0D + angleTarget;
    return (int)(angleTarget * 182.04444444399999D);
  }

  public static double calculateAngleFrom(GameObject obj1, GameObject obj2)
  {
    return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
  }

  public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
  {
    double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
    if (angleTarget < 0.0D)
      angleTarget = 360.0D + angleTarget;
    return angleTarget;
  }

  public static boolean checkIfInRange(int range, int x1, int y1, int x2, int y2)
  {
    return checkIfInRange(range, x1, y1, 0, x2, y2, 0, false);
  }

  public static boolean checkIfInRange(int range, int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
  {
    long dx = x1 - x2;
    long dy = y1 - y2;

    if (includeZAxis)
    {
      long dz = z1 - z2;
      return dx * dx + dy * dy + dz * dz <= range * range;
    }
    return dx * dx + dy * dy <= range * range;
  }

  public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis)
  {
    if ((obj1 == null) || (obj2 == null))
      return false;
    return checkIfInRange(range, obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
  }

  public static double convertHeadingToDegree(int heading)
  {
    return heading / 182.04444444399999D;
  }

  public static double convertHeadingToRadian(int heading)
  {
    return Math.toRadians(convertHeadingToDegree(heading) - 90.0D);
  }

  public static int convertDegreeToClientHeading(double degree)
  {
    if (degree < 0.0D)
      degree = 360.0D + degree;
    return (int)(degree * 182.04444444399999D);
  }

  public static double calculateDistance(int x1, int y1, int z1, int x2, int y2)
  {
    return calculateDistance(x1, y1, 0, x2, y2, 0, false);
  }

  public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
  {
    long dx = x1 - x2;
    long dy = y1 - y2;

    if (includeZAxis)
    {
      long dz = z1 - z2;
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    return Math.sqrt(dx * dx + dy * dy);
  }

  public static double calculateDistance(GameObject obj1, GameObject obj2, boolean includeZAxis)
  {
    if ((obj1 == null) || (obj2 == null))
      return 2147483647.0D;
    return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
  }

  public static double getDistance(GameObject a1, GameObject a2)
  {
    return getDistance(a1.getX(), a2.getY(), a2.getX(), a2.getY());
  }

  public static double getDistance(Location loc1, Location loc2)
  {
    return getDistance(loc1.getX(), loc1.getY(), loc2.getX(), loc2.getY());
  }

  public static double getDistance(int x1, int y1, int x2, int y2)
  {
    return Math.hypot(x1 - x2, y1 - y2);
  }

  public static int getHeadingTo(GameObject actor, GameObject target)
  {
    if ((actor == null) || (target == null) || (target == actor))
      return -1;
    return getHeadingTo(actor.getLoc(), target.getLoc());
  }

  public static int getHeadingTo(Location actor, Location target)
  {
    if ((actor == null) || (target == null) || (target.equals(actor))) {
      return -1;
    }
    int dx = target.x - x;
    int dy = target.y - y;
    int heading = target.h - (int)(Math.atan2(-dy, -dx) * 10430.378350470453D + 32768.0D);

    if (heading < 0)
      heading = heading + 1 + 2147483647 & 0xFFFF;
    else if (heading > 65535) {
      heading &= 65535;
    }
    return heading;
  }

  public static enum TargetDirection
  {
    NONE, 
    FRONT, 
    SIDE, 
    BEHIND;
  }
}