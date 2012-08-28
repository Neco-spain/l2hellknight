package l2m.gameserver.instancemanager;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Territory;
import l2m.gameserver.model.World;
import l2m.gameserver.templates.mapregion.RegionData;
import l2m.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;

public class MapRegionManager extends AbstractHolder
{
  private static final MapRegionManager _instance = new MapRegionManager();

  private RegionData[][][] map = new RegionData[World.WORLD_SIZE_X][World.WORLD_SIZE_Y][0];

  public static MapRegionManager getInstance()
  {
    return _instance;
  }

  private int regionX(int x)
  {
    return x - World.MAP_MIN_X >> 15;
  }

  private int regionY(int y)
  {
    return y - World.MAP_MIN_Y >> 15;
  }

  public void addRegionData(RegionData rd)
  {
    for (int x = regionX(rd.getTerritory().getXmin()); x <= regionX(rd.getTerritory().getXmax()); x++)
      for (int y = regionY(rd.getTerritory().getYmin()); y <= regionY(rd.getTerritory().getYmax()); y++)
      {
        map[x][y] = ((RegionData[])ArrayUtils.add(map[x][y], rd));
      }
  }

  public <T extends RegionData> T getRegionData(Class<T> clazz, GameObject o)
  {
    return getRegionData(clazz, o.getX(), o.getY(), o.getZ());
  }

  public <T extends RegionData> T getRegionData(Class<T> clazz, Location loc)
  {
    return getRegionData(clazz, loc.getX(), loc.getY(), loc.getZ());
  }

  public <T extends RegionData> T getRegionData(Class<T> clazz, int x, int y, int z)
  {
    for (RegionData rd : map[regionX(x)][regionY(y)])
    {
      if (rd.getClass() != clazz)
        continue;
      if (rd.getTerritory().isInside(x, y, z)) {
        return rd;
      }
    }
    return null;
  }

  public int size()
  {
    return World.WORLD_SIZE_X * World.WORLD_SIZE_Y;
  }

  public void clear()
  {
  }
}