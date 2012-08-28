package l2m.gameserver.geodata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2m.gameserver.Config;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.ExShowTrace;
import l2m.gameserver.utils.Location;

public class GeoMove
{
  private static List<Location> findPath(int x, int y, int z, Location target, GameObject obj, boolean showTrace, int geoIndex)
  {
    if (Math.abs(z - target.z) > 256) {
      return Collections.emptyList();
    }
    z = GeoEngine.getHeight(x, y, z, geoIndex);
    target.z = GeoEngine.getHeight(target, geoIndex);

    PathFind n = new PathFind(x, y, z, target.x, target.y, target.z, obj, geoIndex);

    if ((n.getPath() == null) || (n.getPath().isEmpty())) {
      return Collections.emptyList();
    }
    List targetRecorder = new ArrayList(n.getPath().size() + 2);

    targetRecorder.add(new Location(x, y, z));

    for (Location p : n.getPath()) {
      targetRecorder.add(p.geo2world());
    }

    targetRecorder.add(target);

    if (Config.PATH_CLEAN) {
      pathClean(targetRecorder, geoIndex);
    }
    if ((showTrace) && (obj.isPlayer()) && (((Player)obj).getVarB("trace")))
    {
      Player player = (Player)obj;
      ExShowTrace trace = new ExShowTrace();
      int i = 0;
      for (Location loc : targetRecorder)
      {
        i++;
        if ((i == 1) || (i == targetRecorder.size()))
          continue;
        trace.addTrace(loc.x, loc.y, loc.z + 15, 30000);
      }
      player.sendPacket(trace);
    }

    return targetRecorder;
  }

  public static List<List<Location>> findMovePath(int x, int y, int z, Location target, GameObject obj, boolean showTrace, int geoIndex)
  {
    return getNodePath(findPath(x, y, z, target, obj, showTrace, geoIndex), geoIndex);
  }

  private static List<List<Location>> getNodePath(List<Location> path, int geoIndex)
  {
    int size = path.size();
    if (size <= 1)
      return Collections.emptyList();
    List result = new ArrayList(size);
    for (int i = 1; i < size; i++)
    {
      Location p2 = (Location)path.get(i);
      Location p1 = (Location)path.get(i - 1);
      List moveList = GeoEngine.MoveList(p1.x, p1.y, p1.z, p2.x, p2.y, geoIndex, true);
      if (moveList == null)
        return Collections.emptyList();
      if (!moveList.isEmpty())
        result.add(moveList);
    }
    return result;
  }

  public static List<Location> constructMoveList(Location begin, Location end)
  {
    begin.world2geo();
    end.world2geo();

    int diff_x = end.x - x; int diff_y = end.y - y; int diff_z = end.z - z;
    int dx = Math.abs(diff_x); int dy = Math.abs(diff_y); int dz = Math.abs(diff_z);
    float steps = Math.max(Math.max(dx, dy), dz);
    if (steps == 0.0F) {
      return Collections.emptyList();
    }
    float step_x = diff_x / steps; float step_y = diff_y / steps; float step_z = diff_z / steps;
    float next_x = x; float next_y = y; float next_z = z;

    List result = new ArrayList((int)steps + 1);
    result.add(new Location(x, y, z));

    for (int i = 0; i < steps; i++)
    {
      next_x += step_x;
      next_y += step_y;
      next_z += step_z;

      result.add(new Location((int)(next_x + 0.5F), (int)(next_y + 0.5F), (int)(next_z + 0.5F)));
    }

    return result;
  }

  private static void pathClean(List<Location> path, int geoIndex)
  {
    int size = path.size();
    if (size > 2) {
      for (int i = 2; i < size; i++)
      {
        Location p3 = (Location)path.get(i);
        Location p2 = (Location)path.get(i - 1);
        Location p1 = (Location)path.get(i - 2);
        if ((!p1.equals(p2)) && (!p3.equals(p2)) && (!IsPointInLine(p1, p2, p3)))
          continue;
        path.remove(i - 1);
        size--;
        i = Math.max(2, i - 2);
      }
    }

    int current = 0;

    while (current < path.size() - 2)
    {
      Location one = (Location)path.get(current);
      int sub = current + 2;
      while (sub < path.size())
      {
        Location two = (Location)path.get(sub);
        if ((one.equals(two)) || (GeoEngine.canMoveWithCollision(one.x, one.y, one.z, two.x, two.y, two.z, geoIndex)))
          while (current + 1 < sub)
          {
            path.remove(current + 1);
            sub--;
          }
        sub++;
      }
      current++;
    }
  }

  private static boolean IsPointInLine(Location p1, Location p2, Location p3)
  {
    if (((x == p3.x) && (p3.x == p2.x)) || ((y == p3.y) && (p3.y == p2.y))) {
      return true;
    }

    return (x - p2.x) * (y - p2.y) == (p2.x - p3.x) * (p2.y - p3.y);
  }
}