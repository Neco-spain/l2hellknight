package net.sf.l2j.gameserver.geodata;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.util.Point3D;

public class GeoEngine extends GeoData
{
  private static final Logger _log = Logger.getLogger(GeoData.class.getName());
  private static final Map<Short, MappedByteBuffer> _geodata = new FastMap();
  private static final Map<Short, IntBuffer> _geodataIndex = new FastMap();
  private static BufferedOutputStream _geoBugsOut;
  private final FastMap<Integer, FastMap<Long, Byte>> _doorGeodata = new FastMap();

  public static GeoEngine getInstance()
  {
    return SingletonHolder._instance;
  }

  private GeoEngine()
  {
    nInitGeodata();
  }

  public short getType(int x, int y)
  {
    return nGetType(x - -131072 >> 4, y - -262144 >> 4);
  }

  public short getHeight(int x, int y, int z)
  {
    return nGetHeight(x - -131072 >> 4, y - -262144 >> 4, z);
  }

  public short getSpawnHeight(int x, int y, int zmin, int zmax, int spawnid)
  {
    return nGetSpawnHeight(x - -131072 >> 4, y - -262144 >> 4, zmin, zmax, spawnid);
  }

  public String geoPosition(int x, int y)
  {
    int gx = x - -131072 >> 4;
    int gy = y - -262144 >> 4;
    return "bx: " + getBlock(gx) + " by: " + getBlock(gy) + " cx: " + getCell(gx) + " cy: " + getCell(gy) + "  region offset: " + getRegionOffset(gx, gy);
  }

  public boolean canSeeTarget(L2Object cha, Point3D target)
  {
    if (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()))
      return false;
    if (cha.getZ() >= target.getZ())
      return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ());
    return canSeeTarget(target.getX(), target.getY(), target.getZ(), cha.getX(), cha.getY(), cha.getZ());
  }

  public boolean canSeeTarget(L2Object cha, L2Object target)
  {
    if ((cha == null) || (target == null)) return false;

    int z = cha.getZ() + 45;
    if ((cha instanceof L2SiegeGuardInstance))
      z += 30;
    int z2 = target.getZ() + 45;
    if ((!(target instanceof L2DoorInstance)) && (DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2)))
      return false;
    if ((target instanceof L2DoorInstance)) return true;
    if ((target instanceof L2SiegeGuardInstance))
      z2 += 30;
    if (cha.getZ() >= target.getZ())
      return canSeeTarget(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2);
    return canSeeTarget(target.getX(), target.getY(), z2, cha.getX(), cha.getY(), z);
  }

  public boolean canSeeTargetDebug(L2PcInstance gm, L2Object target)
  {
    int z = gm.getZ() + 45;
    int z2 = target.getZ() + 45;
    if ((target instanceof L2DoorInstance))
    {
      gm.sendMessage("door always true");
      return true;
    }

    if (gm.getZ() >= target.getZ()) {
      return canSeeDebug(gm, gm.getX() - -131072 >> 4, gm.getY() - -262144 >> 4, z, target.getX() - -131072 >> 4, target.getY() - -262144 >> 4, z2);
    }
    return canSeeDebug(gm, target.getX() - -131072 >> 4, target.getY() - -262144 >> 4, z2, gm.getX() - -131072 >> 4, gm.getY() - -262144 >> 4, z);
  }

  public short getNSWE(int x, int y, int z)
  {
    return nGetNSWE(x - -131072 >> 4, y - -262144 >> 4, z);
  }

  public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz)
  {
    Location destiny = moveCheck(x, y, z, tx, ty, tz);
    return (destiny.getX() == tx) && (destiny.getY() == ty) && (destiny.getZ() == tz);
  }

  public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
  {
    Location startpoint = new Location(x, y, z);
    if (DoorTable.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz)) return startpoint;

    Location destiny = new Location(tx, ty, tz);
    return moveCheck(startpoint, destiny, x - -131072 >> 4, y - -262144 >> 4, z, tx - -131072 >> 4, ty - -262144 >> 4, tz);
  }

  public void addGeoDataBug(L2PcInstance gm, String comment)
  {
    int gx = gm.getX() - -131072 >> 4;
    int gy = gm.getY() - -262144 >> 4;
    int bx = getBlock(gx);
    int by = getBlock(gy);
    int cx = getCell(gx);
    int cy = getCell(gy);
    int rx = (gx >> 11) + 16;
    int ry = (gy >> 11) + 10;
    String out = rx + ";" + ry + ";" + bx + ";" + by + ";" + cx + ";" + cy + ";" + gm.getZ() + ";" + comment + "\n";
    try
    {
      _geoBugsOut.write(out.getBytes());
      _geoBugsOut.flush();
      gm.sendMessage("GeoData bug saved!");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      gm.sendMessage("GeoData bug save Failed!");
    }
  }

  public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
  {
    return canSee(x - -131072 >> 4, y - -262144 >> 4, z, tx - -131072 >> 4, ty - -262144 >> 4, tz);
  }

  public boolean hasGeo(int x, int y)
  {
    int gx = x - -131072 >> 4;
    int gy = y - -262144 >> 4;
    short region = getRegionOffset(gx, gy);
    return _geodata.get(Short.valueOf(region)) != null;
  }

  private static boolean canSee(int x, int y, double z, int tx, int ty, int tz)
  {
    int dx = tx - x;
    int dy = ty - y;
    double dz = tz - z;
    int distance2 = dx * dx + dy * dy;

    if (distance2 > 90000)
    {
      return false;
    }

    if (distance2 < 82)
    {
      if (dz * dz > 22500.0D)
      {
        short region = getRegionOffset(x, y);

        if (_geodata.get(Short.valueOf(region)) != null) return false;
      }
      return true;
    }

    int inc_x = sign(dx);
    int inc_y = sign(dy);
    dx = Math.abs(dx);
    dy = Math.abs(dy);
    double inc_z_directionx = dz * dx / distance2;
    double inc_z_directiony = dz * dy / distance2;

    int next_x = x;
    int next_y = y;

    if (dx >= dy)
    {
      int delta_A = 2 * dy;
      int d = delta_A - dx;
      int delta_B = delta_A - 2 * dx;

      for (int i = 0; i < dx; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_x += inc_x;
          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, false)) return false;
          next_y += inc_y;
          z += inc_z_directiony;

          if (!nLOS(next_x, y, (int)z, 0, inc_y, inc_z_directiony, tz, false)) return false;
        }
        else
        {
          d += delta_A;
          next_x += inc_x;

          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, false)) return false;
        }
      }
    }
    else
    {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_y += inc_y;
          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, false)) return false;
          next_x += inc_x;
          z += inc_z_directionx;

          if (!nLOS(x, next_y, (int)z, inc_x, 0, inc_z_directionx, tz, false)) return false;
        }
        else
        {
          d += delta_A;
          next_y += inc_y;

          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, false)) return false;
        }
      }
    }
    return true;
  }

  private static boolean canSeeDebug(L2PcInstance gm, int x, int y, double z, int tx, int ty, int tz)
  {
    int dx = tx - x;
    int dy = ty - y;
    double dz = tz - z;
    int distance2 = dx * dx + dy * dy;

    if (distance2 > 90000)
    {
      gm.sendMessage("dist > 300");
      return false;
    }

    if (distance2 < 82)
    {
      if (dz * dz > 22500.0D)
      {
        short region = getRegionOffset(x, y);

        if (_geodata.get(Short.valueOf(region)) != null) return false;
      }
      return true;
    }

    int inc_x = sign(dx);
    int inc_y = sign(dy);
    dx = Math.abs(dx);
    dy = Math.abs(dy);
    double inc_z_directionx = dz * dx / distance2;
    double inc_z_directiony = dz * dy / distance2;

    gm.sendMessage("Los: from X: " + x + "Y: " + y + "--->> X: " + tx + " Y: " + ty);

    int next_x = x;
    int next_y = y;

    if (dx >= dy)
    {
      int delta_A = 2 * dy;
      int d = delta_A - dx;
      int delta_B = delta_A - 2 * dx;

      for (int i = 0; i < dx; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_x += inc_x;
          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, true)) return false;
          next_y += inc_y;
          z += inc_z_directiony;

          if (!nLOS(next_x, y, (int)z, 0, inc_y, inc_z_directiony, tz, true)) return false;
        }
        else
        {
          d += delta_A;
          next_x += inc_x;

          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz, true)) return false;
        }
      }
    }
    else
    {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_y += inc_y;
          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, true)) return false;
          next_x += inc_x;
          z += inc_z_directionx;

          if (!nLOS(x, next_y, (int)z, inc_x, 0, inc_z_directionx, tz, true)) return false;
        }
        else
        {
          d += delta_A;
          next_y += inc_y;

          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz, true)) return false;
        }
      }
    }
    return true;
  }

  private static Location moveCheck(Location startpoint, Location destiny, int x, int y, double z, int tx, int ty, int tz)
  {
    int dx = tx - x;
    int dy = ty - y;
    int distance2 = dx * dx + dy * dy;

    if (distance2 == 0) return destiny;
    if (distance2 > 36100)
    {
      double divider = Math.sqrt(30000.0D / distance2);
      tx = x + (int)(divider * dx);
      ty = y + (int)(divider * dy);
      int dz = tz - startpoint.getZ();
      tz = startpoint.getZ() + (int)(divider * dz);
      dx = tx - x;
      dy = ty - y;
    }

    int inc_x = sign(dx);
    int inc_y = sign(dy);
    dx = Math.abs(dx);
    dy = Math.abs(dy);

    int next_x = x;
    int next_y = y;

    if (dx >= dy)
    {
      int delta_A = 2 * dy;
      int d = delta_A - dx;
      int delta_B = delta_A - 2 * dx;

      for (int i = 0; i < dx; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_x += inc_x;
          double tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D)
            return new Location((x << 4) + -131072, (y << 4) + -262144, (int)z);
          z = tempz;
          next_y += inc_y;

          tempz = nCanMoveNext(next_x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D)
            return new Location((x << 4) + -131072, (y << 4) + -262144, (int)z);
          z = tempz;
        }
        else
        {
          d += delta_A;
          next_x += inc_x;

          double tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D)
            return new Location((x << 4) + -131072, (y << 4) + -262144, (int)z);
          z = tempz;
        }
      }
    }
    else
    {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++)
      {
        x = next_x;
        y = next_y;
        if (d > 0)
        {
          d += delta_B;
          next_y += inc_y;
          double tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D)
            return new Location((x << 4) + -131072, (y << 4) + -262144, (int)z);
          z = tempz;
          next_x += inc_x;

          tempz = nCanMoveNext(x, next_y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D)
            return new Location((x << 4) + -131072, (y << 4) + -262144, (int)z);
          z = tempz;
        }
        else
        {
          d += delta_A;
          next_y += inc_y;

          double tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D)
            return new Location((x << 4) + -131072, (y << 4) + -262144, (int)z);
          z = tempz;
        }
      }
    }
    if (z == startpoint.getZ())
      return destiny;
    return new Location(destiny.getX(), destiny.getY(), (int)z);
  }

  private static byte sign(int x)
  {
    if (x >= 0) return 1;
    return -1;
  }

  private static void nInitGeodata()
  {
    LineNumberReader lnr;
    try {
      _log.info("Geo Engine: - Loading Geodata...");
      File Data = new File("./data/geodata/geo_index.txt");
      if (!Data.exists()) return;

      lnr = new LineNumberReader(new BufferedReader(new FileReader(Data)));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new Error("Failed to Load geo_index File.");
    }
    try
    {
      String line;
      while ((line = lnr.readLine()) != null)
      {
        if (line.trim().length() != 0) {
          StringTokenizer st = new StringTokenizer(line, "_");
          byte rx = Byte.parseByte(st.nextToken());
          byte ry = Byte.parseByte(st.nextToken());
          loadGeodataFile(rx, ry);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error("Failed to Read geo_index File.");
    }
    finally
    {
      try
      {
        lnr.close();
      }
      catch (Exception e)
      {
      }
    }
    try
    {
      File geo_bugs = new File("./data/geodata/geo_bugs.txt");

      _geoBugsOut = new BufferedOutputStream(new FileOutputStream(geo_bugs, true));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new Error("Failed to Load geo_bugs.txt File.");
    }
  }

  public static void unloadGeodata(byte rx, byte ry)
  {
    short regionoffset = (short)((rx << 5) + ry);
    _geodataIndex.remove(Short.valueOf(regionoffset));
    _geodata.remove(Short.valueOf(regionoffset));
  }

  public static boolean loadGeodataFile(byte rx, byte ry)
  {
    String fname = "./data/geodata/" + rx + "_" + ry + ".l2j";
    short regionoffset = (short)((rx << 5) + ry);
    _log.info("Geo Engine: - Loading: " + fname + " -> region offset: " + regionoffset + "X: " + rx + " Y: " + ry);
    File Geo = new File(fname);
    int index = 0; int block = 0; int flor = 0;
    FileChannel roChannel = null;
    try
    {
      roChannel = new RandomAccessFile(Geo, "r").getChannel();
      int size = (int)roChannel.size();
      MappedByteBuffer geo;
      MappedByteBuffer geo;
      if (Config.FORCE_GEODATA)
      {
        geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, size).load();
      } else geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, size);
      geo.order(ByteOrder.LITTLE_ENDIAN);

      if (size > 196608)
      {
        indexs = IntBuffer.allocate(65536);
        while (block < 65536)
        {
          byte type = geo.get(index);
          indexs.put(block, index);
          block++;
          index++;
          if (type == 0) index += 2;
          else if (type == 1) index += 128;
          else
          {
            for (int b = 0; b < 64; b++)
            {
              byte layers = geo.get(index);
              index += (layers << 1) + 1;
              if (layers <= flor) continue; flor = layers;
            }
          }
        }
        _geodataIndex.put(Short.valueOf(regionoffset), indexs);
      }
      _geodata.put(Short.valueOf(regionoffset), geo);

      _log.info("Geo Engine: - Max Layers: " + flor + " Size: " + size + " Loaded: " + index);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      _log.warning("Failed to Load GeoFile at block: " + block + "\n");
      boolean indexs = false;
      IntBuffer indexs = indexs;
      return indexs;
    }
    finally
    {
      try
      {
        roChannel.close();
      }
      catch (Exception e)
      {
      }
    }
    return true;
  }

  private static short getRegionOffset(int x, int y)
  {
    int rx = x >> 11;
    int ry = y >> 11;
    return (short)((rx + 16 << 5) + (ry + 10));
  }

  private static int getBlock(int geo_pos)
  {
    return (geo_pos >> 3) % 256;
  }

  private static int getCell(int geo_pos)
  {
    return geo_pos % 8;
  }

  private static short nGetType(int x, int y)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);

    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return 0;
    }
    return (short)geo.get(index);
  }

  private static short nGetHeight(int geox, int geoy, int z)
  {
    short region = getRegionOffset(geox, geoy);
    int blockX = getBlock(geox);
    int blockY = getBlock(geoy);

    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return (short)z;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
      return geo.getShort(index);
    if (type == 1)
    {
      int cellX = getCell(geox);
      int cellY = getCell(geoy);
      index += ((cellX << 3) + cellY << 1);
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      return height;
    }

    int cellX = getCell(geox);
    int cellY = getCell(geoy);
    int offset = (cellX << 3) + cellY;
    while (offset > 0)
    {
      byte lc = geo.get(index);
      index += (lc << 1) + 1;
      offset--;
    }
    byte layers = geo.get(index);
    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
      return (short)z;
    }
    short temph = -32768;
    while (layers > 0)
    {
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if ((z - temph) * (z - temph) > (z - height) * (z - height)) temph = height;
      layers = (byte)(layers - 1);
      index += 2;
    }
    return temph;
  }

  private static short nGetUpperHeight(int geox, int geoy, int z)
  {
    short region = getRegionOffset(geox, geoy);
    int blockX = getBlock(geox);
    int blockY = getBlock(geoy);

    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return (short)z;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
      return geo.getShort(index);
    if (type == 1)
    {
      int cellX = getCell(geox);
      int cellY = getCell(geoy);
      index += ((cellX << 3) + cellY << 1);
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      return height;
    }

    int cellX = getCell(geox);
    int cellY = getCell(geoy);
    int offset = (cellX << 3) + cellY;
    while (offset > 0)
    {
      byte lc = geo.get(index);
      index += (lc << 1) + 1;
      offset--;
    }
    byte layers = geo.get(index);
    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
      return (short)z;
    }
    short temph = 32767;
    while (layers > 0)
    {
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if (height < z) return temph;
      temph = height;
      layers = (byte)(layers - 1);
      index += 2;
    }
    return temph;
  }

  private static short nGetSpawnHeight(int geox, int geoy, int zmin, int zmax, int spawnid)
  {
    short region = getRegionOffset(geox, geoy);
    int blockX = getBlock(geox);
    int blockY = getBlock(geoy);

    short temph = -32768;
    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return (short)zmin;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0) {
      temph = geo.getShort(index);
    } else if (type == 1)
    {
      int cellX = getCell(geox);
      int cellY = getCell(geoy);
      index += ((cellX << 3) + cellY << 1);
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      temph = height;
    }
    else
    {
      int cellX = getCell(geox);
      int cellY = getCell(geoy);

      int offset = (cellX << 3) + cellY;
      while (offset > 0)
      {
        byte lc = geo.get(index);
        index += (lc << 1) + 1;
        offset--;
      }

      byte layers = geo.get(index);
      index++;
      if ((layers <= 0) || (layers > 125))
      {
        _log.warning("Broken geofile (case2), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
        return (short)zmin;
      }
      while (layers > 0)
      {
        short height = geo.getShort(index);
        height = (short)(height & 0xFFF0);
        height = (short)(height >> 1);
        if ((zmin - temph) * (zmin - temph) > (zmin - height) * (zmin - height)) temph = height;
        layers = (byte)(layers - 1);
        index += 2;
      }
      if ((temph > zmax + 200) || (temph < zmin - 200))
      {
        if (Config.DEBUG)
          _log.warning("SpawnHeight Error - Couldnt find correct layer to spawn NPC - GeoData or Spawnlist Bug!: zmin: " + zmin + " zmax: " + zmax + " value: " + temph + " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
        return (short)zmin;
      }
    }
    if ((temph > zmax + 1000) || (temph < zmin - 1000))
    {
      if (Config.DEBUG)
        _log.warning("SpawnHeight Error - Spawnlist z value is wrong or GeoData error: zmin: " + zmin + " zmax: " + zmax + " value: " + temph + " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
      return (short)zmin;
    }
    return temph;
  }

  private static double nCanMoveNext(int x, int y, int z, int tx, int ty, int tz)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);

    short NSWE = 0;

    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return z;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
      return geo.getShort(index);
    if (type == 1)
    {
      int cellX = getCell(x);
      int cellY = getCell(y);
      index += ((cellX << 3) + cellY << 1);
      short height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if (checkNSWE(NSWE, x, y, tx, ty)) return height;
      return 4.9E-324D;
    }

    int cellX = getCell(x);
    int cellY = getCell(y);
    int offset = (cellX << 3) + cellY;
    while (offset > 0)
    {
      byte lc = geo.get(index);
      index += (lc << 1) + 1;
      offset--;
    }
    byte layers = geo.get(index);

    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case3), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
      return z;
    }
    short tempz = -32768;
    while (layers > 0)
    {
      short height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);

      if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
      {
        tempz = height;
        NSWE = geo.getShort(index);
        NSWE = (short)(NSWE & 0xF);
      }
      layers = (byte)(layers - 1);
      index += 2;
    }
    if (checkNSWE(NSWE, x, y, tx, ty)) return tempz;
    return 4.9E-324D;
  }

  private static boolean nLOS(int x, int y, int z, int inc_x, int inc_y, double inc_z, int tz, boolean debug)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);

    short NSWE = 0;

    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return true;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
    {
      short height = geo.getShort(index);
      if (debug) _log.warning("flatheight:" + height);
      if (z > height) return inc_z > height;
      return inc_z < height;
    }
    if (type == 1)
    {
      int cellX = getCell(x);
      int cellY = getCell(y);
      index += ((cellX << 3) + cellY << 1);
      short height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
      {
        if (debug) _log.warning("height:" + height + " z" + z);
        return z >= nGetUpperHeight(x + inc_x, y + inc_y, height);
      }
      return true;
    }

    int cellX = getCell(x);
    int cellY = getCell(y);

    int offset = (cellX << 3) + cellY;
    while (offset > 0)
    {
      byte lc = geo.get(index);
      index += (lc << 1) + 1;
      offset--;
    }
    byte layers = geo.get(index);

    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case4), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
      return false;
    }
    short upperHeight = 32767;
    short lowerHeight = -32768;
    byte temp_layers = layers;
    boolean highestlayer = false;
    while (temp_layers > 0)
    {
      short tempZ = geo.getShort(index);
      tempZ = (short)(tempZ & 0xFFF0);
      tempZ = (short)(tempZ >> 1);

      if (z > tempZ)
      {
        lowerHeight = tempZ;
        NSWE = geo.getShort(index);
        NSWE = (short)(NSWE & 0xF);
        break;
      }

      highestlayer = false;
      upperHeight = tempZ;

      temp_layers = (byte)(temp_layers - 1);
      index += 2;
    }
    if (debug) {
      _log.warning("z:" + z + " x: " + cellX + " y:" + cellY + " la " + layers + " lo:" + lowerHeight + " up:" + upperHeight);
    }

    if ((z - upperHeight < -10) && (z - upperHeight > inc_z - 20.0D) && (z - lowerHeight > 40))
    {
      if (debug) _log.warning("false, incz" + inc_z);
      return false;
    }

    if (!highestlayer)
    {
      if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
      {
        if (debug) {
          _log.warning("block and next in x" + inc_x + " y" + inc_y + " is:" + nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight));
        }
        return z >= nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight);
      }
      return true;
    }
    return (checkNSWE(NSWE, x, y, x + inc_x, y + inc_y)) || (z >= nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight));
  }

  private static short nGetNSWE(int x, int y, int z)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);

    short NSWE = 0;

    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return 15;
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
      return 15;
    if (type == 1)
    {
      int cellX = getCell(x);
      int cellY = getCell(y);
      index += ((cellX << 3) + cellY << 1);
      short height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
    }
    else
    {
      int cellX = getCell(x);
      int cellY = getCell(y);
      int offset = (cellX << 3) + cellY;
      while (offset > 0)
      {
        byte lc = geo.get(index);
        index += (lc << 1) + 1;
        offset--;
      }
      byte layers = geo.get(index);
      index++;

      if ((layers <= 0) || (layers > 125))
      {
        _log.warning("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
        return 15;
      }
      short tempz = -32768;
      while (layers > 0)
      {
        short height = geo.getShort(index);
        height = (short)(height & 0xFFF0);
        height = (short)(height >> 1);

        if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
        {
          tempz = height;
          NSWE = (short)geo.get(index);
          NSWE = (short)(NSWE & 0xF);
        }
        layers = (byte)(layers - 1);
        index += 2;
      }
    }
    return NSWE;
  }

  public short getHeightAndNSWE(int x, int y, int z)
  {
    short region = getRegionOffset(x, y);
    int blockX = getBlock(x);
    int blockY = getBlock(y);

    IntBuffer idx = (IntBuffer)_geodataIndex.get(Short.valueOf(region));
    int index;
    int index;
    if (idx == null) index = ((blockX << 8) + blockY) * 3;
    else {
      index = idx.get((blockX << 8) + blockY);
    }
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null)
    {
      if (Config.DEBUG) _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      return (short)(z << 1 | 0xF);
    }

    byte type = geo.get(index);
    index++;
    if (type == 0)
      return (short)(geo.getShort(index) << 1 | 0xF);
    if (type == 1)
    {
      int cellX = getCell(x);
      int cellY = getCell(y);
      index += ((cellX << 3) + cellY << 1);
      return geo.getShort(index);
    }

    int cellX = getCell(x);
    int cellY = getCell(y);
    int offset = (cellX << 3) + cellY;
    while (offset > 0)
    {
      byte lc = geo.get(index);
      index += (lc << 1) + 1;
      offset--;
    }
    byte layers = geo.get(index);
    index++;

    if ((layers <= 0) || (layers > 125))
    {
      _log.warning("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
      return (short)(z << 1 | 0xF);
    }
    short temph = -32768;
    short result = 0;
    while (layers > 0)
    {
      short block = geo.getShort(index);
      short height = (short)(block & 0xFFF0);
      height = (short)(height >> 1);
      if ((z - temph) * (z - temph) > (z - height) * (z - height))
      {
        temph = height;
        result = block;
      }
      layers = (byte)(layers - 1);
      index += 2;
    }
    return result;
  }

  private static boolean checkNSWE(short NSWE, int x, int y, int tx, int ty)
  {
    if (NSWE == 15) return true;
    if (tx > x)
    {
      if ((NSWE & 0x1) == 0) return false;
    }
    else if (tx < x)
    {
      if ((NSWE & 0x2) == 0) return false;
    }
    if (ty > y)
    {
      if ((NSWE & 0x4) == 0) return false;
    }
    else if (ty < y)
    {
      if ((NSWE & 0x8) == 0) return false;
    }
    return true;
  }

  public void setDoorGeodataOpen(L2DoorInstance door, boolean i)
  {
    FastMap doorGeodata = (FastMap)_doorGeodata.get(Integer.valueOf(door.getDoorId()));

    if (doorGeodata == null)
      _doorGeodata.put(Integer.valueOf(door.getDoorId()), doorGeodata = new FastMap());
  }

  public void initDoorGeodata(L2DoorInstance door)
  {
    int minX = door.getXMin() - -131072 >> 4;
    int maxX = door.getXMax() - -131072 >> 4;
    int minY = door.getYMin() - -262144 >> 4;
    int maxY = door.getYMax() - -262144 >> 4;
    int z = door.getZMin();

    FastMap doorGeodata = (FastMap)_doorGeodata.get(Integer.valueOf(door.getDoorId()));

    for (int geoX = minX; geoX <= maxX; geoX++)
    {
      for (int geoY = minY; geoY <= maxY; geoY++)
      {
        if (!isCellNearDoor(geoX, geoY, minX, maxX, minY, maxY))
          continue;
        short region = getRegionOffset(geoX, geoY);

        short NSWE = 0;
        int index;
        int neededIndex = index = getIndex(geoX, geoY, region);
        ByteBuffer regionGeo = (ByteBuffer)_geodata.get(Short.valueOf(region));
        if (regionGeo == null)
        {
          _log.info("GeoEngine: Door: " + door.getDoorId() + " has no geodata!");
        }
        else
        {
          byte type = regionGeo.get(index++);

          byte layers = 0;

          short tempz = 0;
          int cellX;
          int cellY;
          short height;
          switch (type)
          {
          case 1:
            cellX = getCell(geoX);
            cellY = getCell(geoY);
            index += ((cellX << 3) + cellY << 1);
            NSWE = (short)(regionGeo.getShort(index) & 0xF);
            neededIndex = index;
            break;
          case 2:
            cellX = getCell(geoX);
            cellY = getCell(geoY);
            int offset = (cellX << 3) + cellY;
            while (offset > 0)
            {
              byte lc = regionGeo.get(index);
              index += (lc << 1) + 1;
              offset--;
            }
            layers = regionGeo.get(index++);
            height = -1;
            if ((layers <= 0) || (layers > 125))
            {
              _log.warning("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + geoX + " " + geoY);
              continue;
            }
            tempz = -32768;
          default:
            while (layers > 0)
            {
              height = regionGeo.getShort(index);
              height = (short)(height & 0xFFF0);
              height = (short)(height >> 1);

              if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
              {
                tempz = height;
                NSWE = (short)regionGeo.get(index);
                NSWE = (short)(NSWE & 0xF);
                neededIndex = index;
              }
              layers = (byte)(layers - 1);
              index += 2;
            }
          }
          if (doorGeodata == null) {
            _doorGeodata.put(Integer.valueOf(door.getDoorId()), doorGeodata = new FastMap());
          }
          doorGeodata.put(Long.valueOf(region << 32 | neededIndex), Byte.valueOf((byte)NSWE));
        }
      }

    }

    setDoorGeodataOpen(door, true);
  }

  private boolean isCellNearDoor(int geoX, int geoY, int minX, int maxX, int minY, int maxY)
  {
    for (int ax = geoX; ax < geoX + 16; ax++) {
      for (int ay = geoY; ay < geoY + 16; ay++)
        if ((geoX >= minX) && (geoX <= maxX) && (geoY >= minY) && (geoY <= maxY)) return true;
    }
    return false;
  }

  private int getIndex(int x, int y, short region)
  {
    int blockX = getBlock(x);
    int blockY = getBlock(y);

    IntBuffer tmp = (IntBuffer)_geodataIndex.get(Short.valueOf(region));

    if (tmp == null) return ((blockX << 8) + blockY) * 3;

    return tmp.get((blockX << 8) + blockY);
  }

  private static class SingletonHolder
  {
    static final GeoEngine _instance = new GeoEngine(null);
  }
}