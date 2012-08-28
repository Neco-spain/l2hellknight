package l2m.gameserver.geodata;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.model.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoOptimizer
{
  private static final Logger log = LoggerFactory.getLogger(GeoOptimizer.class);
  public static int[][][] checkSums;
  private static final byte version = 1;

  public static BlockLink[] loadBlockMatches(String fileName)
  {
    File f = new File(Config.DATAPACK_ROOT, fileName);
    if (!f.exists())
      return null;
    try
    {
      FileChannel roChannel = new RandomAccessFile(f, "r").getChannel();

      int count = (int)((roChannel.size() - 1L) / 6L);
      ByteBuffer buffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, roChannel.size());
      roChannel.close();
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      if (buffer.get() != 1) {
        return null;
      }
      BlockLink[] links = new BlockLink[count];
      for (int i = 0; i < links.length; i++) {
        links[i] = new BlockLink(buffer.getShort(), buffer.get(), buffer.get(), buffer.getShort());
      }
      return links;
    }
    catch (Exception e)
    {
      log.error("", e);
    }return null;
  }

  public static class BlockLink
  {
    public final int blockIndex;
    public final int linkBlockIndex;
    public final byte linkMapX;
    public final byte linkMapY;

    public BlockLink(short _blockIndex, byte _linkMapX, byte _linkMapY, short _linkBlockIndex)
    {
      blockIndex = (_blockIndex & 0xFFFF);
      linkMapX = _linkMapX;
      linkMapY = _linkMapY;
      linkBlockIndex = (_linkBlockIndex & 0xFFFF);
    }

    public BlockLink(int _blockIndex, byte _linkMapX, byte _linkMapY, int _linkBlockIndex)
    {
      blockIndex = (_blockIndex & 0xFFFF);
      linkMapX = _linkMapX;
      linkMapY = _linkMapY;
      linkBlockIndex = (_linkBlockIndex & 0xFFFF);
    }
  }

  public static class CheckSumLoader extends RunnableImpl
  {
    private final int geoX;
    private final int geoY;
    private final int rx;
    private final int ry;
    private final byte[][][] region;
    private final String fileName;

    public CheckSumLoader(int _geoX, int _geoY, byte[][][] _region)
    {
      geoX = _geoX;
      geoY = _geoY;
      rx = (geoX + Config.GEO_X_FIRST);
      ry = (_geoY + Config.GEO_Y_FIRST);
      region = _region;
      fileName = ("geodata/checksum/" + rx + "_" + ry + ".crc");
    }

    private boolean loadFromFile()
    {
      File GeoCrc = new File(Config.DATAPACK_ROOT, fileName);
      if (!GeoCrc.exists())
        return false;
      try
      {
        FileChannel roChannel = new RandomAccessFile(GeoCrc, "r").getChannel();
        if (roChannel.size() != 262144L)
        {
          roChannel.close();
          return false;
        }

        ByteBuffer buffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, roChannel.size());
        roChannel.close();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int[] _checkSums = new int[65536];
        for (int i = 0; i < 65536; i++)
          _checkSums[i] = buffer.getInt();
        GeoOptimizer.checkSums[geoX][geoY] = _checkSums;
        return true;
      }
      catch (Exception e)
      {
        GeoOptimizer.log.error("", e);
      }return false;
    }

    private void saveToFile()
    {
      GeoOptimizer.log.info("Saving checksums to: " + fileName);
      try
      {
        File f = new File(Config.DATAPACK_ROOT, fileName);
        if (f.exists())
          f.delete();
        FileChannel wChannel = new RandomAccessFile(f, "rw").getChannel();
        ByteBuffer buffer = wChannel.map(FileChannel.MapMode.READ_WRITE, 0L, 262144L);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int[] _checkSums = GeoOptimizer.checkSums[geoX][geoY];
        for (int i = 0; i < 65536; i++)
          buffer.putInt(_checkSums[i]);
        wChannel.close();
      }
      catch (Exception e)
      {
        GeoOptimizer.log.error("", e);
      }
    }

    private void gen()
    {
      GeoOptimizer.log.info("Generating checksums for " + rx + "_" + ry);
      int[] _checkSums = new int[65536];
      CRC32 crc32 = new CRC32();
      for (int i = 0; i < 65536; i++)
      {
        crc32.update(region[i][0]);
        _checkSums[i] = (int)(crc32.getValue() ^ 0xFFFFFFFF);
        crc32.reset();
      }
      GeoOptimizer.checkSums[geoX][geoY] = _checkSums;
    }

    public void runImpl()
      throws Exception
    {
      if (!loadFromFile())
      {
        gen();
        saveToFile();
      }
    }
  }

  public static class GeoBlocksMatchFinder extends RunnableImpl
  {
    private final int geoX;
    private final int geoY;
    private final int rx;
    private final int ry;
    private final int maxScanRegions;
    private final String fileName;

    public GeoBlocksMatchFinder(int _geoX, int _geoY, int _maxScanRegions)
    {
      geoX = _geoX;
      geoY = _geoY;
      rx = (geoX + Config.GEO_X_FIRST);
      ry = (geoY + Config.GEO_Y_FIRST);
      maxScanRegions = _maxScanRegions;
      fileName = ("geodata/matches/" + rx + "_" + ry + ".matches");
    }

    private boolean exists()
    {
      return new File(Config.DATAPACK_ROOT, fileName).exists();
    }

    private void saveToFile(GeoOptimizer.BlockLink[] links)
    {
      GeoOptimizer.log.info("Saving matches to: " + fileName);
      try
      {
        File f = new File(Config.DATAPACK_ROOT, fileName);
        if (f.exists())
          f.delete();
        FileChannel wChannel = new RandomAccessFile(f, "rw").getChannel();
        ByteBuffer buffer = wChannel.map(FileChannel.MapMode.READ_WRITE, 0L, links.length * 6 + 1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(1);
        for (int i = 0; i < links.length; i++)
        {
          buffer.putShort((short)links[i].blockIndex);
          buffer.put(links[i].linkMapX);
          buffer.put(links[i].linkMapY);
          buffer.putShort((short)links[i].linkBlockIndex);
        }
        wChannel.close();
      }
      catch (Exception e)
      {
        GeoOptimizer.log.error("", e);
      }
    }

    private void calcMatches(int[] curr_checkSums, int mapX, int mapY, List<GeoOptimizer.BlockLink> putlinks, boolean[] notready)
    {
      int[] next_checkSums = GeoOptimizer.checkSums[mapX][mapY];
      if (next_checkSums == null) {
        return;
      }

      for (int blockIdx = 0; blockIdx < 65536; blockIdx++) {
        if (notready[blockIdx] == 0)
          continue;
        int startIdx2 = next_checkSums == curr_checkSums ? blockIdx + 1 : 0;
        for (int blockIdx2 = startIdx2; blockIdx2 < 65536; blockIdx2++) {
          if ((curr_checkSums[blockIdx] != next_checkSums[blockIdx2]) || 
            (!GeoEngine.compareGeoBlocks(geoX, geoY, blockIdx, mapX, mapY, blockIdx2)))
            continue;
          putlinks.add(new GeoOptimizer.BlockLink(blockIdx, (byte)mapX, (byte)mapY, blockIdx2));
          notready[blockIdx] = false;
          break;
        }
      }
    }

    private GeoOptimizer.BlockLink[] gen()
    {
      GeoOptimizer.log.info("Searching matches for " + rx + "_" + ry);
      long started = System.currentTimeMillis();

      boolean[] notready = new boolean[65536];
      for (int i = 0; i < 65536; i++) {
        notready[i] = true;
      }
      List links = new ArrayList();
      int[] _checkSums = GeoOptimizer.checkSums[geoX][geoY];

      int n = 0;
      for (int mapX = geoX; mapX < World.WORLD_SIZE_X; mapX++)
      {
        int startgeoY = mapX == geoX ? geoY : 0;
        for (int mapY = startgeoY; mapY < World.WORLD_SIZE_Y; mapY++)
        {
          calcMatches(_checkSums, mapX, mapY, links, notready);
          n++;
          if ((maxScanRegions > 0) && (maxScanRegions == n)) {
            return (GeoOptimizer.BlockLink[])links.toArray(new GeoOptimizer.BlockLink[links.size()]);
          }
        }
      }
      started = System.currentTimeMillis() - started;
      GeoOptimizer.log.info("Founded " + links.size() + " matches for " + rx + "_" + ry + " in " + (float)started / 1000.0F + "s");
      return (GeoOptimizer.BlockLink[])links.toArray(new GeoOptimizer.BlockLink[links.size()]);
    }

    public void runImpl()
      throws Exception
    {
      if (!exists())
      {
        GeoOptimizer.BlockLink[] links = gen();
        saveToFile(links);
      }
    }
  }
}