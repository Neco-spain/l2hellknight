package l2p.gameserver.geodata;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import l2p.gameserver.Config;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.utils.Location;

public class PathFind
{
  private int geoIndex = 0;
  private PathFindBuffers.PathFindBuffer buff;
  private List<Location> path;
  private final short[] hNSWE = new short[2];
  private final Location startPoint;
  private final Location endPoint;
  private PathFindBuffers.GeoNode startNode;
  private PathFindBuffers.GeoNode endNode;
  private PathFindBuffers.GeoNode currentNode;

  public PathFind(int x, int y, int z, int destX, int destY, int destZ, GameObject obj, int geoIndex)
  {
    this.geoIndex = geoIndex;

    startPoint = (Config.PATHFIND_BOOST == 0 ? new Location(x, y, z) : GeoEngine.moveCheckWithCollision(x, y, z, destX, destY, true, geoIndex));
    endPoint = ((Config.PATHFIND_BOOST != 2) || (Math.abs(destZ - z) > 200) ? new Location(destX, destY, destZ) : GeoEngine.moveCheckBackwardWithCollision(destX, destY, destZ, startPoint.x, startPoint.y, true, geoIndex));

    startPoint.world2geo();
    endPoint.world2geo();

    startPoint.z = GeoEngine.NgetHeight(startPoint.x, startPoint.y, startPoint.z, geoIndex);
    endPoint.z = GeoEngine.NgetHeight(endPoint.x, endPoint.y, endPoint.z, geoIndex);

    int xdiff = Math.abs(endPoint.x - startPoint.x);
    int ydiff = Math.abs(endPoint.y - startPoint.y);

    if ((xdiff == 0) && (ydiff == 0))
    {
      if (Math.abs(endPoint.z - startPoint.z) < 32)
      {
        path = new ArrayList();
        path.add(0, startPoint);
      }
      return;
    }

    int mapSize = 2 * Math.max(xdiff, ydiff);

    if ((this.buff = PathFindBuffers.alloc(mapSize)) != null)
    {
      buff.offsetX = (startPoint.x - buff.mapSize / 2);
      buff.offsetY = (startPoint.y - buff.mapSize / 2);

      buff.totalUses += 1L;
      if (obj.isPlayable()) {
        buff.playableUses += 1L;
      }
      findPath();

      buff.free();

      PathFindBuffers.recycle(buff);
    }
  }

  private List<Location> findPath()
  {
    startNode = buff.nodes[(startPoint.x - buff.offsetX)][(startPoint.y - buff.offsetY)].set(startPoint.x, startPoint.y, (short)startPoint.z);

    GeoEngine.NgetHeightAndNSWE(startPoint.x, startPoint.y, (short)startPoint.z, hNSWE, geoIndex);
    startNode.z = hNSWE[0];
    startNode.nswe = hNSWE[1];
    startNode.costFromStart = 0.0F;
    startNode.state = 1;
    startNode.parent = null;

    endNode = buff.nodes[(endPoint.x - buff.offsetX)][(endPoint.y - buff.offsetY)].set(endPoint.x, endPoint.y, (short)endPoint.z);

    startNode.costToEnd = pathCostEstimate(startNode);
    startNode.totalCost = (startNode.costFromStart + startNode.costToEnd);

    buff.open.add(startNode);

    long nanos = System.nanoTime();
    long searhTime = 0L;
    int itr = 0;

    while (((searhTime = System.nanoTime() - nanos) < Config.PATHFIND_MAX_TIME) && ((this.currentNode = (PathFindBuffers.GeoNode)buff.open.poll()) != null))
    {
      itr++;
      if ((currentNode.x == endPoint.x) && (currentNode.y == endPoint.y) && (Math.abs(currentNode.z - endPoint.z) < 64))
      {
        path = tracePath(currentNode);
        break;
      }

      handleNode(currentNode);
      currentNode.state = -1;
    }

    buff.totalTime += searhTime;
    buff.totalItr += itr;
    if (path != null)
      buff.successUses += 1L;
    else if (searhTime > Config.PATHFIND_MAX_TIME) {
      buff.overtimeUses += 1L;
    }
    return path;
  }

  private List<Location> tracePath(PathFindBuffers.GeoNode f)
  {
    List locations = new ArrayList();
    do
    {
      locations.add(0, f.getLoc());
      f = f.parent;
    }while (f.parent != null);
    return locations;
  }

  private void handleNode(PathFindBuffers.GeoNode node)
  {
    int clX = node.x;
    int clY = node.y;
    short clZ = node.z;

    getHeightAndNSWE(clX, clY, clZ);
    short NSWE = hNSWE[1];

    if (Config.PATHFIND_DIAGONAL)
    {
      if (((NSWE & 0x4) == 4) && ((NSWE & 0x1) == 1))
      {
        getHeightAndNSWE(clX + 1, clY, clZ);
        if ((hNSWE[1] & 0x4) == 4)
        {
          getHeightAndNSWE(clX, clY + 1, clZ);
          if ((hNSWE[1] & 0x1) == 1)
          {
            handleNeighbour(clX + 1, clY + 1, node, true);
          }
        }

      }

      if (((NSWE & 0x4) == 4) && ((NSWE & 0x2) == 2))
      {
        getHeightAndNSWE(clX - 1, clY, clZ);
        if ((hNSWE[1] & 0x4) == 4)
        {
          getHeightAndNSWE(clX, clY + 1, clZ);
          if ((hNSWE[1] & 0x2) == 2)
          {
            handleNeighbour(clX - 1, clY + 1, node, true);
          }
        }

      }

      if (((NSWE & 0x8) == 8) && ((NSWE & 0x1) == 1))
      {
        getHeightAndNSWE(clX + 1, clY, clZ);
        if ((hNSWE[1] & 0x8) == 8)
        {
          getHeightAndNSWE(clX, clY - 1, clZ);
          if ((hNSWE[1] & 0x1) == 1)
          {
            handleNeighbour(clX + 1, clY - 1, node, true);
          }
        }

      }

      if (((NSWE & 0x8) == 8) && ((NSWE & 0x2) == 2))
      {
        getHeightAndNSWE(clX - 1, clY, clZ);
        if ((hNSWE[1] & 0x8) == 8)
        {
          getHeightAndNSWE(clX, clY - 1, clZ);
          if ((hNSWE[1] & 0x2) == 2)
          {
            handleNeighbour(clX - 1, clY - 1, node, true);
          }
        }
      }

    }

    if ((NSWE & 0x1) == 1)
    {
      handleNeighbour(clX + 1, clY, node, false);
    }

    if ((NSWE & 0x2) == 2)
    {
      handleNeighbour(clX - 1, clY, node, false);
    }

    if ((NSWE & 0x4) == 4)
    {
      handleNeighbour(clX, clY + 1, node, false);
    }

    if ((NSWE & 0x8) == 8)
    {
      handleNeighbour(clX, clY - 1, node, false);
    }
  }

  private float pathCostEstimate(PathFindBuffers.GeoNode n)
  {
    int diffx = endNode.x - n.x;
    int diffy = endNode.y - n.y;
    int diffz = endNode.z - n.z;

    return (float)Math.sqrt(diffx * diffx + diffy * diffy + diffz * diffz / 256);
  }

  private float traverseCost(PathFindBuffers.GeoNode from, PathFindBuffers.GeoNode n, boolean d)
  {
    if ((n.nswe != 15) || (Math.abs(n.z - from.z) > 16)) {
      return 3.0F;
    }

    getHeightAndNSWE(n.x + 1, n.y, n.z);
    if ((hNSWE[1] != 15) || (Math.abs(n.z - hNSWE[0]) > 16)) return 2.0F;

    getHeightAndNSWE(n.x - 1, n.y, n.z);
    if ((hNSWE[1] != 15) || (Math.abs(n.z - hNSWE[0]) > 16)) return 2.0F;

    getHeightAndNSWE(n.x, n.y + 1, n.z);
    if ((hNSWE[1] != 15) || (Math.abs(n.z - hNSWE[0]) > 16)) return 2.0F;

    getHeightAndNSWE(n.x, n.y - 1, n.z);
    if ((hNSWE[1] != 15) || (Math.abs(n.z - hNSWE[0]) > 16)) return 2.0F;

    return d ? 1.414F : 1.0F;
  }

  private void handleNeighbour(int x, int y, PathFindBuffers.GeoNode from, boolean d)
  {
    int nX = x - buff.offsetX; int nY = y - buff.offsetY;
    if ((nX >= buff.mapSize) || (nX < 0) || (nY >= buff.mapSize) || (nY < 0)) {
      return;
    }
    PathFindBuffers.GeoNode n = buff.nodes[nX][nY];

    if (!n.isSet())
    {
      n = n.set(x, y, from.z);
      GeoEngine.NgetHeightAndNSWE(x, y, from.z, hNSWE, geoIndex);
      n.z = hNSWE[0];
      n.nswe = hNSWE[1];
    }

    int height = Math.abs(n.z - from.z);
    if ((height > Config.PATHFIND_MAX_Z_DIFF) || (n.nswe == 0)) {
      return;
    }
    float newCost = from.costFromStart + traverseCost(from, n, d);
    if ((n.state == 1) || (n.state == -1))
    {
      if (n.costFromStart <= newCost) {
        return;
      }
    }
    if (n.state == 0) {
      n.costToEnd = pathCostEstimate(n);
    }
    n.parent = from;
    n.costFromStart = newCost;
    n.totalCost = (n.costFromStart + n.costToEnd);

    if (n.state == 1) {
      return;
    }
    n.state = 1;
    buff.open.add(n);
  }

  private void getHeightAndNSWE(int x, int y, short z)
  {
    int nX = x - buff.offsetX; int nY = y - buff.offsetY;
    if ((nX >= buff.mapSize) || (nX < 0) || (nY >= buff.mapSize) || (nY < 0))
    {
      hNSWE[1] = 0;
      return;
    }

    PathFindBuffers.GeoNode n = buff.nodes[nX][nY];
    if (!n.isSet())
    {
      n = n.set(x, y, z);
      GeoEngine.NgetHeightAndNSWE(x, y, z, hNSWE, geoIndex);
      n.z = hNSWE[0];
      n.nswe = hNSWE[1];
    }
    else
    {
      hNSWE[0] = n.z;
      hNSWE[1] = n.nswe;
    }
  }

  public List<Location> getPath()
  {
    return path;
  }
}