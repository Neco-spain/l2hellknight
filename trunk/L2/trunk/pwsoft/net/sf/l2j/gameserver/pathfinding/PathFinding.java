package net.sf.l2j.gameserver.pathfinding;

import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.pathfinding.cellnodes.CellPathFinding;
import net.sf.l2j.gameserver.pathfinding.tools.CellNodeMap;
import net.sf.l2j.util.log.AbstractLogger;

public abstract class PathFinding
{
  public static final Logger _log = AbstractLogger.getLogger("PathFinding");
  private static PathFinding _instance;

  public static PathFinding getInstance()
  {
    return _instance;
  }

  public static void init() {
    CellPathFinding.init();
    _instance = CellPathFinding.getInstance(); } 
  public abstract boolean pathNodesExist(short paramShort);

  public abstract FastTable<AbstractNodeLoc> findPath(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);

  public abstract FastTable<GeoNode> readNeighbors(GeoNode paramGeoNode, int paramInt);

  public FastTable<AbstractNodeLoc> search(GeoNode start, GeoNode end) { FastTable visited = new FastTable();
    FastTable to_visit = new FastTable();
    to_visit.add(start);
    int i = 0;

    GeoNode n = null;
    GeoNode node = null;
    while (i < 800) {
      try {
        node = (GeoNode)to_visit.remove(0);
      } catch (Exception var12) {
        return null;
      }

      if (node.equals(end)) {
        n = null;
        return constructPath(node);
      }

      i++;
      visited.add(node);
      node.attachNeighbors();
      FastTable neighbors = node.getNeighbors();
      if ((neighbors != null) && (!neighbors.isEmpty())) {
        int k = 0; for (int l = neighbors.size(); k < l; k++) {
          n = (GeoNode)neighbors.get(k);
          if (n == null)
          {
            continue;
          }
          if ((!visited.contains(n)) && (!to_visit.contains(n))) {
            n.setParent(node);
            to_visit.add(n);
          }
        }
      }
    }
    n = null;
    node = null;
    return null; }

  protected FastTable<AbstractNodeLoc> searchByClosest(GeoNode start, GeoNode end)
  {
    CellNodeMap known = new CellNodeMap();
    FastTable to_visit = new FastTable();
    to_visit.add(start);
    known.add(start);

    int i = 0;
    GeoNode n = null;
    GeoNode node = null;
    while (i < 3500) {
      try {
        node = (GeoNode)to_visit.remove(0);
      } catch (Exception var14) {
        n = null;
        node = null;
        return null;
      }

      i++;
      node.attachNeighbors();
      if (node.equals(end)) {
        n = null;
        return constructPath(node);
      }

      FastTable neighbors = node.getNeighbors();
      if ((neighbors != null) && (!neighbors.isEmpty())) {
        int k = 0; for (int l = neighbors.size(); k < l; k++) {
          n = (GeoNode)neighbors.get(k);
          if (n == null)
          {
            continue;
          }
          if (!known.contains(n)) {
            boolean added = false;
            n.setParent(node);
            n.setCost(square(end.getLoc().getNodeX() - n.getLoc().getNodeX()) + square(end.getLoc().getNodeY() - n.getLoc().getNodeY()) + squareHalfOriginal(end.getLoc().getZ() - n.getLoc().getZ()));

            for (int index = 0; index < to_visit.size(); index++) {
              if (((GeoNode)to_visit.get(index)).getCost() > n.getCost()) {
                to_visit.add(index, n);
                added = true;
                break;
              }
            }

            if (!added) {
              to_visit.addLast(n);
            }

            known.add(n);
          }
        }
      }
    }
    n = null;
    node = null;
    return null;
  }

  public FastTable<AbstractNodeLoc> constructPath(GeoNode node) {
    FastTable path = new FastTable();
    int previousdirectionx = -1000;

    for (int previousdirectiony = -1000; node.getParent() != null; node = node.getParent())
    {
      int directiony;
      int directionx;
      int directiony;
      if ((node.getParent().getParent() != null) && (Math.abs(node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX()) == 1) && (Math.abs(node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY()) == 1)) {
        int directionx = node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX();
        directiony = node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY();
      } else {
        directionx = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
        directiony = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
      }

      if ((directionx != previousdirectionx) || (directiony != previousdirectiony)) {
        previousdirectionx = directionx;
        previousdirectiony = directiony;
        path.add(0, node.getLoc());
      }
    }
    node = null;

    if (path.size() > 4) {
      GeoData geo = GeoData.getInstance();
      FastTable valueList = new FastTable();
      int i = 0; for (int k = path.size(); i < k - 3; i += 3) {
        AbstractNodeLoc anl = (AbstractNodeLoc)path.get(i);
        if (anl == null)
        {
          continue;
        }
        AbstractNodeLoc anl2 = (AbstractNodeLoc)path.get(i + 3);
        if (anl2 == null)
        {
          continue;
        }
        if (geo.canMoveFromToTarget(anl.getX(), anl.getY(), anl.getZ(), anl2.getX(), anl2.getY(), anl2.getZ())) {
          valueList.add(Integer.valueOf(i + 1));
          valueList.add(Integer.valueOf(i + 2));
        }
      }

      for (int index = valueList.size() - 1; index >= 0; index--) {
        path.remove(valueList.get(index));
      }
      valueList.clear();
      valueList = null;
    }
    return path;
  }

  protected short getNodePos(int geo_pos) {
    return (short)(geo_pos >> 3);
  }

  protected short getNodeBlock(int node_pos) {
    return (short)(node_pos % 256);
  }

  protected byte getRegionX(int node_pos) {
    return (byte)((node_pos >> 8) + 15);
  }

  protected byte getRegionY(int node_pos) {
    return (byte)((node_pos >> 8) + 10);
  }

  protected short getRegionOffset(byte rx, byte ry) {
    return (short)((rx << 5) + ry);
  }

  public int calculateWorldX(short node_x) {
    return Config.MAP_MIN_X + node_x * 128 + 48;
  }

  public int calculateWorldY(short node_y) {
    return Config.MAP_MIN_Y + node_y * 128 + 48;
  }

  public static int square(int num) {
    return num * num;
  }

  public static long square(long num) {
    return num * num;
  }

  public static double square(double num) {
    return num * num;
  }

  public static int squareHalfOriginal(int num) {
    return num / 2 * num;
  }

  public static long squareHalfOriginal(long num) {
    return num / 2L * num;
  }
}