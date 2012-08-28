package net.sf.l2j.gameserver.geodata.pathfind;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.util.StringUtil;

public class CellPathFinding extends PathFinding
{
  private static final Logger _log = Logger.getLogger(CellPathFinding.class.getName());
  private BufferInfo[] _allBuffers;
  private int _findSuccess = 0;
  private int _findFails = 0;
  private int _postFilterUses = 0;
  private int _postFilterPlayableUses = 0;
  private int _postFilterPasses = 0;
  private long _postFilterElapsed = 0L;

  private FastList<L2ItemInstance> _debugItems = null;

  public static CellPathFinding getInstance()
  {
    return SingletonHolder._instance;
  }

  private CellPathFinding()
  {
    try
    {
      String[] array = Config.PATHFIND_BUFFERS.split(";");

      _allBuffers = new BufferInfo[array.length];

      for (int i = 0; i < array.length; i++)
      {
        String buf = array[i];
        String[] args = buf.split("x");
        if (args.length != 2) throw new Exception(new StringBuilder().append("Invalid buffer definition: ").append(buf).toString());

        _allBuffers[i] = new BufferInfo(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
      }
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("CellPathFinding: Problem during buffer init: ").append(e.getMessage()).toString());
      throw new Error("CellPathFinding: load aborted");
    }
  }

  public boolean pathNodesExist(short regionoffset)
  {
    return false;
  }

  public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, boolean playable)
  {
    int gx = x - -131072 >> 4;
    int gy = y - -262144 >> 4;
    if (!GeoData.getInstance().hasGeo(x, y)) return null;
    short gz = GeoData.getInstance().getHeight(x, y, z);
    int gtx = tx - -131072 >> 4;
    int gty = ty - -262144 >> 4;
    if (!GeoData.getInstance().hasGeo(tx, ty)) return null;
    short gtz = GeoData.getInstance().getHeight(tx, ty, tz);
    CellNodeBuffer buffer = alloc(64 + 2 * Math.max(Math.abs(gx - gtx), Math.abs(gy - gty)), playable);
    if (buffer == null) return null;

    boolean debug = (playable) && (Config.DEBUG_PATH);

    if (debug)
    {
      if (_debugItems == null) { _debugItems = new FastList();
      } else
      {
        for (L2ItemInstance item : _debugItems)
        {
          if (item != null) {
            item.decayMe();
          }
        }
        _debugItems.clear();
      }
    }

    FastList path = null;
    try
    {
      CellNode result = buffer.findPath(gx, gy, gz, gtx, gty, gtz);
      Iterator i$;
      if (debug)
      {
        for (i$ = buffer.debugPath().iterator(); i$.hasNext(); ) { n = (CellNode)i$.next();

          if (n.getCost() < 0.0F)
            dropDebugItem(1831, (int)(-n.getCost() * 10.0F), n.getLoc());
          else {
            dropDebugItem(57, (int)(n.getCost() * 10.0F), n.getLoc());
          }
        }
      }
      if (result == null)
      {
        _findFails += 1;

        List list = null;
        n = list;
        return n;
      }
      path = constructPath(result);
    }
    catch (Exception e)
    {
      e.printStackTrace();

      List list = null;
      CellNode n = list;
      return n; } finally { buffer.free();
    }

    if ((path.size() < 3) || (Config.MAX_POSTFILTER_PASSES <= 0))
    {
      _findSuccess += 1;
      return path;
    }

    long timeStamp = System.currentTimeMillis();
    _postFilterUses += 1;
    if (playable) _postFilterPlayableUses += 1; int pass = 0;
    boolean remove;
    ListIterator middlePoint;
    do { pass++;
      _postFilterPasses += 1;

      remove = false;
      middlePoint = path.listIterator();
      ListIterator endPoint = path.listIterator(1);
      AbstractNodeLoc locEnd = null;
      int currentX = x;
      int currentY = y;
      int currentZ = z;

      while (endPoint.hasNext())
      {
        locEnd = (AbstractNodeLoc)endPoint.next();
        AbstractNodeLoc locMiddle = (AbstractNodeLoc)middlePoint.next();
        if (GeoData.getInstance().canMoveFromToTarget(currentX, currentY, currentZ, locEnd.getX(), locEnd.getY(), locEnd.getZ()))
        {
          middlePoint.remove();
          remove = true;
          if (!debug) continue; dropDebugItem(735, 1, locMiddle); continue;
        }

        currentX = locMiddle.getX();
        currentY = locMiddle.getY();
        currentZ = locMiddle.getZ();
      }

    }

    while ((playable) && (remove) && (path.size() > 2) && (pass < Config.MAX_POSTFILTER_PASSES));

    if (debug)
    {
      middlePoint = path.listIterator();
      while (middlePoint.hasNext())
      {
        AbstractNodeLoc locMiddle = (AbstractNodeLoc)middlePoint.next();
        dropDebugItem(65, 1, locMiddle);
      }
    }

    _findSuccess += 1;
    _postFilterElapsed += System.currentTimeMillis() - timeStamp;
    return path;
  }

  private FastList<AbstractNodeLoc> constructPath(AbstractNode node)
  {
    FastList path = new FastList();
    int previousDirectionX = -2147483648;
    int previousDirectionY = -2147483648;

    while (node.getParent() != null)
    {
      int directionX = 0;
      int directionY;
      int directionY;
      if ((!Config.ADVANCED_DIAGONAL_STRATEGY) && (node.getParent().getParent() != null))
      {
        int tmpX = node.getLoc().getNodeX() - node.getParent().getParent().getLoc().getNodeX();
        int tmpY = node.getLoc().getNodeY() - node.getParent().getParent().getLoc().getNodeY();
        int directionY;
        if (Math.abs(tmpX) == Math.abs(tmpY))
        {
          directionY = tmpY;
        }
        else
        {
          directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
        }
      }
      else
      {
        directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
        directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
      }

      if ((directionX != previousDirectionX) || (directionY != previousDirectionY))
      {
        previousDirectionX = directionX;
        previousDirectionY = directionY;

        path.addFirst(node.getLoc());
        node.setLoc(null);
      }

      node = node.getParent();
    }

    return path;
  }

  private CellNodeBuffer alloc(int size, boolean playable)
  {
    CellNodeBuffer current = null;
    for (BufferInfo i : _allBuffers)
    {
      if (i.mapSize < size)
        continue;
      for (CellNodeBuffer buf : i.bufs)
      {
        if (buf.lock())
        {
          i.uses += 1;
          if (playable) i.playableUses += 1;
          i.elapsed += buf.getElapsedTime();
          current = buf;
          break;
        }
      }
      if (current != null) {
        break;
      }
      current = new CellNodeBuffer(i.mapSize);
      current.lock();
      if (i.bufs.size() < i.count)
      {
        i.bufs.add(current);
        i.uses += 1;
        if (!playable) break; i.playableUses += 1; break;
      }

      i.overflows += 1;
      if (!playable) continue; i.playableOverflows += 1;
    }

    return current;
  }

  private void dropDebugItem(int itemId, int num, AbstractNodeLoc loc)
  {
    L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
    item.setCount(num);
    item.spawnMe(loc.getX(), loc.getY(), loc.getZ());
    _debugItems.add(item);
  }

  public String[] getStat()
  {
    String[] result = new String[_allBuffers.length + 1];
    for (int i = 0; i < _allBuffers.length; i++) {
      result[i] = _allBuffers[i].toString();
    }
    StringBuilder stat = new StringBuilder(100);
    StringUtil.append(stat, new String[] { "LOS postfilter uses:", String.valueOf(_postFilterUses), "/", String.valueOf(_postFilterPlayableUses) });
    if (_postFilterUses > 0)
      StringUtil.append(stat, new String[] { " total/avg(ms):", String.valueOf(_postFilterElapsed), "/", String.format("%1.2f", new Object[] { Double.valueOf(_postFilterElapsed / _postFilterUses) }), " passes total/avg:", String.valueOf(_postFilterPasses), "/", String.format("%1.1f", new Object[] { Double.valueOf(_postFilterPasses / _postFilterUses) }), "\r\n" });
    StringUtil.append(stat, new String[] { "Pathfind success/fail:", String.valueOf(_findSuccess), "/", String.valueOf(_findFails) });
    result[(result.length - 1)] = stat.toString();

    return result;
  }

  private static final class BufferInfo
  {
    final int mapSize;
    final int count;
    ArrayList<CellNodeBuffer> bufs;
    int uses = 0;
    int playableUses = 0;
    int overflows = 0;
    int playableOverflows = 0;
    long elapsed = 0L;

    public BufferInfo(int size, int cnt)
    {
      mapSize = size;
      count = cnt;
      bufs = new ArrayList(count);
    }

    public String toString()
    {
      StringBuilder stat = new StringBuilder(100);
      StringUtil.append(stat, new String[] { String.valueOf(mapSize), "x", String.valueOf(mapSize), " num:", String.valueOf(bufs.size()), "/", String.valueOf(count), " uses:", String.valueOf(uses), "/", String.valueOf(playableUses) });
      if (uses > 0) {
        StringUtil.append(stat, new String[] { " total/avg(ms):", String.valueOf(elapsed), "/", String.format("%1.2f", new Object[] { Double.valueOf(elapsed / uses) }) });
      }
      StringUtil.append(stat, new String[] { " ovf:", String.valueOf(overflows), "/", String.valueOf(playableOverflows) });

      return stat.toString();
    }
  }

  private static class SingletonHolder
  {
    protected static final CellPathFinding _instance = new CellPathFinding(null);
  }
}