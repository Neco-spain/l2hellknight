package l2p.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import l2p.commons.geometry.Point3D;
import l2p.commons.geometry.Shape;
import l2p.commons.util.Rnd;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.templates.spawn.SpawnRange;
import l2p.gameserver.utils.Location;

public class Territory
  implements Shape, SpawnRange
{
  protected final Point3D max = new Point3D();
  protected final Point3D min = new Point3D();

  private final List<Shape> include = new ArrayList(1);
  private final List<Shape> exclude = new ArrayList(1);

  public Territory add(Shape shape)
  {
    if (include.isEmpty())
    {
      max.x = shape.getXmax();
      max.y = shape.getYmax();
      max.z = shape.getZmax();
      min.x = shape.getXmin();
      min.y = shape.getYmin();
      min.z = shape.getZmin();
    }
    else
    {
      max.x = Math.max(max.x, shape.getXmax());
      max.y = Math.max(max.y, shape.getYmax());
      max.z = Math.max(max.z, shape.getZmax());
      min.x = Math.min(min.x, shape.getXmin());
      min.y = Math.min(min.y, shape.getYmin());
      min.z = Math.min(min.z, shape.getZmin());
    }

    include.add(shape);
    return this;
  }

  public Territory addBanned(Shape shape)
  {
    exclude.add(shape);
    return this;
  }

  public List<Shape> getTerritories()
  {
    return include;
  }

  public List<Shape> getBannedTerritories()
  {
    return exclude;
  }

  public boolean isInside(int x, int y)
  {
    for (int i = 0; i < include.size(); i++)
    {
      Shape shape = (SpawnRange)include.get(i);
      if (shape.isInside(x, y))
        return !isExcluded(x, y);
    }
    return false;
  }

  public boolean isInside(int x, int y, int z)
  {
    if ((x < min.x) || (x > max.x) || (y < min.y) || (y > max.y) || (z < min.z) || (z > max.z)) {
      return false;
    }

    for (int i = 0; i < include.size(); i++)
    {
      Shape shape = (SpawnRange)include.get(i);
      if (shape.isInside(x, y, z))
        return !isExcluded(x, y, z);
    }
    return false;
  }

  public boolean isExcluded(int x, int y)
  {
    for (int i = 0; i < exclude.size(); i++)
    {
      Shape shape = (SpawnRange)exclude.get(i);
      if (shape.isInside(x, y))
        return true;
    }
    return false;
  }

  public boolean isExcluded(int x, int y, int z)
  {
    for (int i = 0; i < exclude.size(); i++)
    {
      Shape shape = (SpawnRange)exclude.get(i);
      if (shape.isInside(x, y, z))
        return true;
    }
    return false;
  }

  public int getXmax()
  {
    return max.x;
  }

  public int getXmin()
  {
    return min.x;
  }

  public int getYmax()
  {
    return max.y;
  }

  public int getYmin()
  {
    return min.y;
  }

  public int getZmax()
  {
    return max.z;
  }

  public int getZmin()
  {
    return min.z;
  }

  public static Location getRandomLoc(Territory territory)
  {
    return getRandomLoc(territory, 0);
  }

  public static Location getRandomLoc(Territory territory, int geoIndex)
  {
    Location pos = new Location();

    List territories = territory.getTerritories();

    label307: for (int i = 0; i < 100; i++)
    {
      Shape shape = (SpawnRange)territories.get(Rnd.get(territories.size()));

      pos.x = Rnd.get(shape.getXmin(), shape.getXmax());
      pos.y = Rnd.get(shape.getYmin(), shape.getYmax());
      pos.z = (shape.getZmin() + (shape.getZmax() - shape.getZmin()) / 2);

      if (!territory.isInside(pos.x, pos.y)) {
        continue;
      }
      int tempz = GeoEngine.getHeight(pos, geoIndex);
      if (shape.getZmin() != shape.getZmax() ? 
        (tempz < shape.getZmin()) && (tempz > shape.getZmax()) : 
        (tempz < shape.getZmin() - 200) || (tempz > shape.getZmin() + 200)) {
        continue;
      }
      pos.z = tempz;

      int geoX = pos.x - World.MAP_MIN_X >> 4;
      int geoY = pos.y - World.MAP_MIN_Y >> 4;

      int x = geoX - 1;
      while (true) { if (x <= geoX + 1) {
          for (int y = geoY - 1; y <= geoY + 1; y++)
            if (GeoEngine.NgetNSWE(x, y, tempz, geoIndex) != 15)
              break label307;
          x++; continue;
        }

        return pos;
      }
    }

    return pos;
  }

  public Location getRandomLoc(int geoIndex)
  {
    return getRandomLoc(this, geoIndex);
  }
}