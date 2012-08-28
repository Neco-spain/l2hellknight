package l2p.gameserver.geodata;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;
import gnu.trove.TIntObjectHashMap;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.commons.text.StrTable;
import l2p.gameserver.Config;
import l2p.gameserver.utils.Location;

public class PathFindBuffers
{
  public static final int MIN_MAP_SIZE = 64;
  public static final int STEP_MAP_SIZE = 32;
  public static final int MAX_MAP_SIZE = 512;
  private static TIntObjectHashMap<PathFindBuffer[]> buffers = new TIntObjectHashMap();
  private static int[] sizes = new int[0];
  private static Lock lock = new ReentrantLock();

  private static PathFindBuffer create(int mapSize)
  {
    lock.lock();
    try
    {
      PathFindBuffer[] buff = (PathFindBuffer[])buffers.get(mapSize);
      PathFindBuffer buffer;
      if (buff != null)
      {
        PathFindBuffer buffer;
        buff = (PathFindBuffer[])l2p.commons.lang.ArrayUtils.add(buff, buffer = new PathFindBuffer(mapSize));
      }
      else
      {
        PathFindBuffer[] tmp48_45 = new PathFindBuffer[1];
         tmp58_55 = new PathFindBuffer(mapSize); buffer = tmp58_55; tmp48_45[0] = tmp58_55; buff = tmp48_45;
        sizes = org.apache.commons.lang3.ArrayUtils.add(sizes, mapSize);
        Arrays.sort(sizes);
      }
      buffers.put(mapSize, buff);
      buffer.inUse = true;
      PathFindBuffer localPathFindBuffer1 = buffer;
      return localPathFindBuffer1; } finally { lock.unlock(); } throw localObject;
  }

  private static PathFindBuffer get(int mapSize)
  {
    lock.lock();
    try
    {
      PathFindBuffer[] buff = (PathFindBuffer[])buffers.get(mapSize);
      for (PathFindBuffer buffer : buff) {
        if (buffer.inUse)
          continue;
        buffer.inUse = true;
        PathFindBuffer localPathFindBuffer1 = buffer;
        return localPathFindBuffer1;
      }
      ??? = null;
      return ???; } finally { lock.unlock(); } throw localObject;
  }

  public static PathFindBuffer alloc(int mapSize)
  {
    if (mapSize > 512)
      return null;
    mapSize += 32;
    if (mapSize < 64) {
      mapSize = 64;
    }
    PathFindBuffer buffer = null;
    for (int i = 0; i < sizes.length; i++) {
      if (sizes[i] < mapSize)
        continue;
      mapSize = sizes[i];
      buffer = get(mapSize);
      break;
    }

    if (buffer == null)
    {
      for (int size = 64; size < 512; size += 32) {
        if (size < mapSize)
          continue;
        mapSize = size;
        buffer = create(mapSize);
        break;
      }
    }

    return buffer;
  }

  public static void recycle(PathFindBuffer buffer)
  {
    lock.lock();
    try
    {
      inUse = false;
    }
    finally
    {
      lock.unlock();
    }
  }

  public static StrTable getStats()
  {
    StrTable table = new StrTable("PathFind Buffers Stats");
    lock.lock();
    try
    {
      long totalUses = 0L; long totalPlayable = 0L; long totalTime = 0L;
      int index = 0;

      for (int size : sizes)
      {
        index++;
        int count = 0;
        long uses = 0L;
        long playable = 0L;
        long itrs = 0L;
        long success = 0L;
        long overtime = 0L;
        long time = 0L;
        for (PathFindBuffer buff : (PathFindBuffer[])buffers.get(size))
        {
          count++;
          uses += buff.totalUses;
          playable += buff.playableUses;
          success += buff.successUses;
          overtime += buff.overtimeUses;
          time += buff.totalTime / 1000000L;
          itrs += buff.totalItr;
        }

        totalUses += uses;
        totalPlayable += playable;
        totalTime += time;

        table.set(index, "Size", size);
        table.set(index, "Count", count);
        table.set(index, "Uses (success%)", new StringBuilder().append(uses).append("(").append(String.format("%2.2f", new Object[] { Double.valueOf(uses > 0L ? success * 100.0D / uses : 0.0D) })).append("%)").toString());
        table.set(index, "Uses, playble", new StringBuilder().append(playable).append("(").append(String.format("%2.2f", new Object[] { Double.valueOf(uses > 0L ? playable * 100.0D / uses : 0.0D) })).append("%)").toString());
        table.set(index, "Uses, overtime", new StringBuilder().append(overtime).append("(").append(String.format("%2.2f", new Object[] { Double.valueOf(uses > 0L ? overtime * 100.0D / uses : 0.0D) })).append("%)").toString());
        table.set(index, "Iter., avg", uses > 0L ? itrs / uses : 0L);
        table.set(index, "Time, avg (ms)", String.format("%1.3f", new Object[] { Double.valueOf(uses > 0L ? time / uses : 0.0D) }));
      }

      table.addTitle(new StringBuilder().append("Uses, total / playable  : ").append(totalUses).append(" / ").append(totalPlayable).toString());
      table.addTitle(new StringBuilder().append("Uses, total time / avg (ms) : ").append(totalTime).append(" / ").append(String.format("%1.3f", new Object[] { Double.valueOf(totalUses > 0L ? totalTime / totalUses : 0.0D) })).toString());
    }
    finally
    {
      lock.unlock();
    }

    return table;
  }

  static
  {
    TIntIntHashMap config = new TIntIntHashMap();

    for (String e : Config.PATHFIND_BUFFERS.split(";"))
    {
      String[] k;
      if ((!e.isEmpty()) && ((k = e.split("x")).length == 2))
        config.put(Integer.valueOf(k[1]).intValue(), Integer.valueOf(k[0]).intValue());
    }
    TIntIntIterator itr = config.iterator();

    while (itr.hasNext())
    {
      itr.advance();
      int size = itr.key();
      int count = itr.value();

      PathFindBuffer[] buff = new PathFindBuffer[count];
      for (int i = 0; i < count; i++) {
        buff[i] = new PathFindBuffer(size);
      }
      buffers.put(size, buff);
    }

    sizes = config.keys();
    Arrays.sort(sizes);
  }

  public static class GeoNode
    implements Comparable<GeoNode>
  {
    public static final int NONE = 0;
    public static final int OPENED = 1;
    public static final int CLOSED = -1;
    public int x;
    public int y;
    public short z;
    public short nswe;
    public float totalCost;
    public float costFromStart;
    public float costToEnd;
    public int state;
    public GeoNode parent;

    public GeoNode()
    {
      nswe = -1;
    }

    public GeoNode set(int x, int y, short z)
    {
      this.x = x;
      this.y = y;
      this.z = z;
      return this;
    }

    public boolean isSet()
    {
      return nswe != -1;
    }

    public void free()
    {
      nswe = -1;
      costFromStart = 0.0F;
      totalCost = 0.0F;
      costToEnd = 0.0F;
      parent = null;
      state = 0;
    }

    public Location getLoc()
    {
      return new Location(x, y, z);
    }

    public String toString()
    {
      return "[" + x + "," + y + "," + z + "] f: " + totalCost;
    }

    public int compareTo(GeoNode o)
    {
      if (totalCost > o.totalCost)
        return 1;
      if (totalCost < o.totalCost)
        return -1;
      return 0;
    }
  }

  public static class PathFindBuffer
  {
    final int mapSize;
    final PathFindBuffers.GeoNode[][] nodes;
    final Queue<PathFindBuffers.GeoNode> open;
    int offsetX;
    int offsetY;
    boolean inUse;
    long totalUses;
    long successUses;
    long overtimeUses;
    long playableUses;
    long totalTime;
    long totalItr;

    public PathFindBuffer(int mapSize)
    {
      open = new PriorityQueue(mapSize);
      this.mapSize = mapSize;
      nodes = new PathFindBuffers.GeoNode[mapSize][mapSize];
      for (int i = 0; i < nodes.length; i++)
        for (int j = 0; j < nodes[i].length; j++)
          nodes[i][j] = new PathFindBuffers.GeoNode();
    }

    public void free()
    {
      open.clear();
      for (int i = 0; i < nodes.length; i++)
        for (int j = 0; j < nodes[i].length; j++)
          nodes[i][j].free();
    }
  }
}