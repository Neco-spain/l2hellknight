package net.sf.l2j.gameserver.pathfinding.cellnodes;

import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.GeoEngine;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.GeoNode;
import net.sf.l2j.gameserver.pathfinding.PathFinding;

public class CellPathFinding extends PathFinding
{
  private static CellPathFinding _instance;

  public static CellPathFinding getInstance()
  {
    return _instance;
  }

  public static void init() {
    _instance = new CellPathFinding();
    _instance.load();
  }

  private void load()
  {
  }

  public boolean pathNodesExist(short regionoffset)
  {
    return false;
  }

  public FastTable<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz)
  {
    return (GeoEngine.getInstance().hasGeo(x, y)) && (GeoEngine.getInstance().hasGeo(tx, ty)) ? searchByClosest(readNode(x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4, GeoEngine.getInstance().getHeight(x, y, z)), readNode(tx - Config.MAP_MIN_X >> 4, ty - Config.MAP_MIN_Y >> 4, GeoEngine.getInstance().getHeight(tx, ty, tz))) : null;
  }

  public FastTable<GeoNode> readNeighbors(GeoNode n, int idx)
  {
    return GeoData.getInstance().getNeighbors(n);
  }

  public GeoNode readNode(int gx, int gy, short z) {
    return new GeoNode(new CellNode(gx, gy, z), 0);
  }
}