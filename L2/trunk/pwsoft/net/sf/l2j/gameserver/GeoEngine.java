package net.sf.l2j.gameserver;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Scanner;
import java.util.logging.Logger;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.GeoNode;
import net.sf.l2j.gameserver.pathfinding.PathFinding;
import net.sf.l2j.gameserver.pathfinding.cellnodes.CellPathFinding;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Point3D;

public class GeoEngine extends GeoData
{
  private static GeoEngine _instance;
  public static final byte BLOCKTYPE_FLAT = 0;
  public static final byte BLOCKTYPE_COMPLEX = 1;
  public static final byte BLOCKTYPE_MULTILEVEL = 2;
  public static final int BlocksInMap = 65536;
  private static final byte _e = 1;
  private static final byte _w = 2;
  private static final byte _s = 4;
  private static final byte _n = 8;
  private static FastMap<Short, MappedByteBuffer> _geodata = new FastMap().shared("GeoEngine._geodata");
  private static FastMap<Short, IntBuffer> _geodataIndex = new FastMap().shared("GeoEngine._geodataIndex");

  public static GeoEngine getInstance() {
    return _instance;
  }

  public static void init() {
    _instance = new GeoEngine();
    switch (Config.GEO_TYPE) {
    case 1:
      loadL2J();
      break;
    case 2:
      loadOFF();
    }
  }

  public short getType(int x, int y)
  {
    return nGetType(x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4);
  }

  public short getHeight(int x, int y, int z)
  {
    return nGetHeight(x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4, (short)z);
  }

  public short getSpawnHeight(int x, int y, int zmin, int zmax)
  {
    return nGetSpawnHeight(x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4, zmin, zmax);
  }

  public String geoPosition(int x, int y)
  {
    int gx = x - Config.MAP_MIN_X >> 4;
    int gy = y - Config.MAP_MIN_Y >> 4;
    return "bx: " + getBlock(gx) + " by: " + getBlock(gy) + " cx: " + getCell(gx) + " cy: " + getCell(gy) + "  region offset: " + getRegionOffset(gx, gy);
  }

  public boolean canSeeTarget(L2Object cha, Point3D target)
  {
    return cha.getZ() >= target.getZ() ? canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()) : DoorTable.getInstance().checkIfDoorsBetween(cha.getX(), cha.getY(), cha.getZ(), target.getX(), target.getY(), target.getZ()) ? false : canSeeTarget(target.getX(), target.getY(), target.getZ(), cha.getX(), cha.getY(), cha.getZ());
  }

  public boolean canSeeTarget(L2Object cha, L2Object target)
  {
    if ((cha == null) || (target == null)) {
      return false;
    }

    int z = cha.getZ() + 45;
    if (cha.isL2SiegeGuard()) {
      z += 30;
    }
    int z2 = target.getZ() + 45;

    if (target.isL2Door()) {
      return true;
    }

    if (DoorTable.getInstance().checkIfDoorsBetween(cha, cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2)) {
      return false;
    }

    if (target.isL2SiegeGuard()) {
      z2 += 30;
    }
    if (cha.getZ() >= target.getZ()) {
      return canSeeTarget(cha.getX(), cha.getY(), z, target.getX(), target.getY(), z2);
    }

    return canSeeTarget(target.getX(), target.getY(), z2, cha.getX(), cha.getY(), z);
  }

  public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
  {
    return canSee(x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4, z, tx - Config.MAP_MIN_X >> 4, ty - Config.MAP_MIN_Y >> 4, tz);
  }

  public Location moveCheckAttack(int x, int y, int z, int tx, int ty, int tz)
  {
    return moveCheck(new Location(x, y, z), new Location(tx, ty, tz), x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4, z, tx - Config.MAP_MIN_X >> 4, ty - Config.MAP_MIN_Y >> 4, tz);
  }

  public short getNSWE(int x, int y, int z)
  {
    return nGetNSWE(x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4, z);
  }

  public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz)
  {
    Location destiny = moveCheck(x, y, z, tx, ty, tz);
    return (destiny.getX() == tx) && (destiny.getY() == ty) && (destiny.getZ() == tz);
  }

  public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
  {
    if (DoorTable.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz)) {
      return new Location(x, y, z);
    }

    return moveCheck(new Location(x, y, z), new Location(tx, ty, tz), x - Config.MAP_MIN_X >> 4, y - Config.MAP_MIN_Y >> 4, z, tx - Config.MAP_MIN_X >> 4, ty - Config.MAP_MIN_Y >> 4, tz);
  }

  public void addGeoDataBug(L2PcInstance gm, String comment)
  {
  }

  public boolean hasGeo(int x, int y)
  {
    int gx = x - Config.MAP_MIN_X >> 4;
    int gy = y - Config.MAP_MIN_Y >> 4;
    short region = getRegionOffset(gx, gy);
    return _geodata.get(Short.valueOf(region)) != null;
  }

  private static boolean canSee(int x, int y, double z, int tx, int ty, int tz) {
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
      if (dz * dz > 22500.0D) {
        short region = getRegionOffset(x, y);

        if (_geodata.get(Short.valueOf(region)) != null) {
          return false;
        }
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

      for (int i = 0; i < dx; i++) {
        x = next_x;
        y = next_y;
        if (d > 0) {
          d += delta_B;
          next_x += inc_x;
          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz)) {
            return false;
          }
          next_y += inc_y;
          z += inc_z_directiony;

          if (!nLOS(next_x, y, (int)z, 0, inc_y, inc_z_directiony, tz))
            return false;
        }
        else {
          d += delta_A;
          next_x += inc_x;

          z += inc_z_directionx;
          if (!nLOS(x, y, (int)z, inc_x, 0, inc_z_directionx, tz))
            return false;
        }
      }
    }
    else {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++) {
        x = next_x;
        y = next_y;
        if (d > 0) {
          d += delta_B;
          next_y += inc_y;
          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz)) {
            return false;
          }
          next_x += inc_x;
          z += inc_z_directionx;

          if (!nLOS(x, next_y, (int)z, inc_x, 0, inc_z_directionx, tz))
            return false;
        }
        else {
          d += delta_A;
          next_y += inc_y;

          z += inc_z_directiony;
          if (!nLOS(x, y, (int)z, 0, inc_y, inc_z_directiony, tz)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static Location moveCheck(Location startpoint, Location destiny, int x, int y, double z, int tx, int ty, int tz) {
    int dx = tx - x;
    int dy = ty - y;
    int distance2 = dx * dx + dy * dy;

    if (distance2 == 0) {
      return destiny;
    }
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
    double tempz = z;

    if (dx >= dy)
    {
      int delta_A = 2 * dy;
      int d = delta_A - dx;
      int delta_B = delta_A - 2 * dx;

      for (int i = 0; i < dx; i++) {
        x = next_x;
        y = next_y;
        if (d > 0) {
          d += delta_B;
          next_x += inc_x;
          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D) {
            return new Location((x << 4) + Config.MAP_MIN_X, (y << 4) + Config.MAP_MIN_Y, (int)z);
          }

          z = tempz;
          next_y += inc_y;

          tempz = nCanMoveNext(next_x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D) {
            return new Location((x << 4) + Config.MAP_MIN_X, (y << 4) + Config.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        } else {
          d += delta_A;
          next_x += inc_x;

          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D) {
            return new Location((x << 4) + Config.MAP_MIN_X, (y << 4) + Config.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        }
      }
    } else {
      int delta_A = 2 * dx;
      int d = delta_A - dy;
      int delta_B = delta_A - 2 * dy;
      for (int i = 0; i < dy; i++) {
        x = next_x;
        y = next_y;
        if (d > 0) {
          d += delta_B;
          next_y += inc_y;
          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D) {
            return new Location((x << 4) + Config.MAP_MIN_X, (y << 4) + Config.MAP_MIN_Y, (int)z);
          }

          z = tempz;
          next_x += inc_x;

          tempz = nCanMoveNext(x, next_y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D) {
            return new Location((x << 4) + Config.MAP_MIN_X, (y << 4) + Config.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        } else {
          d += delta_A;
          next_y += inc_y;

          tempz = nCanMoveNext(x, y, (int)z, next_x, next_y, tz);
          if (tempz == 4.9E-324D) {
            return new Location((x << 4) + Config.MAP_MIN_X, (y << 4) + Config.MAP_MIN_Y, (int)z);
          }

          z = tempz;
        }
      }
    }
    if (z == startpoint.getZ())
    {
      return destiny;
    }

    return new Location(destiny.getX(), destiny.getY(), (int)z);
  }

  private static byte sign(int x) {
    return (byte)(x >= 0 ? 1 : -1);
  }

  static short makeShort(byte b1, byte b0) {
    return (short)(b1 << 8 | b0 & 0xFF);
  }

  private static short getRegionOffset(int x, int y) {
    return (short)(((x >> 11) + 15 << 5) + (y >> 11) + 10);
  }

  private static int getBlock(int geo_pos) {
    return (geo_pos >> 3) % 256;
  }

  private static int getCell(int geo_pos) {
    return geo_pos % 8;
  }

  private static short nGetType(int x, int y) {
    short region = getRegionOffset(x, y);
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    return geo == null ? 0 : (short)geo.get(_geodataIndex.get(Short.valueOf(region)) == null ? ((getBlock(x) << 8) + getBlock(y)) * 3 : ((IntBuffer)_geodataIndex.get(Short.valueOf(region))).get((getBlock(x) << 8) + getBlock(y)));
  }

  private static int getIndex(int blockX, int blockY, IntBuffer idx) {
    if (idx == null) {
      return ((blockX << 8) + blockY) * 3;
    }

    return idx.get((blockX << 8) + blockY);
  }

  private static short nGetHeight(int geox, int geoy, short z) {
    short region = getRegionOffset(geox, geoy);

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null) {
      return z;
    }

    int index = getIndex(getBlock(geox), getBlock(geoy), (IntBuffer)_geodataIndex.get(Short.valueOf(region)));

    index++;

    switch (geo.get(index - 1)) {
    case 0:
      return geo.getShort(index);
    case 1:
      cellX = getCell(geox);
      cellY = getCell(geoy);
      index += ((cellX << 3) + cellY << 1);
      height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      return height;
    }
    int cellX = getCell(geox);
    int cellY = getCell(geoy);
    int offset = (cellX << 3) + cellY;
    while (offset > 0) {
      byte lc = geo.get(index);
      index += (lc << 1) + 1;
      offset--;
    }
    byte layers = geo.get(index);
    index++;
    short height = -1;
    if ((layers <= 0) || (layers > 125)) {
      _log.warning("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
      return z;
    }
    short temph = -32768;
    while (layers > 0) {
      height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if ((z - temph) * (z - temph) > (z - height) * (z - height)) {
        temph = height;
      }
      layers = (byte)(layers - 1);
      index += 2;
    }
    return temph;
  }

  private static short nGetUpperHeight(int geox, int geoy, int z)
  {
    short region = getRegionOffset(geox, geoy);

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null) {
      return (short)z;
    }

    int index = getIndex(getBlock(geox), getBlock(geoy), (IntBuffer)_geodataIndex.get(Short.valueOf(region)));

    index++;
    short height = -1;
    switch (geo.get(index - 1)) {
    case 0:
      return geo.getShort(index);
    case 1:
      cellX = getCell(geox);
      cellY = getCell(geoy);
      index += ((cellX << 3) + cellY << 1);
      height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      return height;
    }
    int cellX = getCell(geox);
    int cellY = getCell(geoy);
    int offset = (cellX << 3) + cellY;
    while (offset > 0) {
      byte lc = geo.get(index);
      index += (lc << 1) + 1;
      offset--;
    }
    byte layers = geo.get(index);
    index++;
    height = -1;
    if ((layers <= 0) || (layers > 125)) {
      _log.warning("Broken geofile (case1), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
      return (short)z;
    }
    short temph = 32767;
    while (layers > 0)
    {
      height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if (height < z) {
        return temph;
      }
      temph = height;
      layers = (byte)(layers - 1);
      index += 2;
    }
    return temph;
  }

  private static short nGetSpawnHeight(int geox, int geoy, int zmin, int zmax)
  {
    short region = getRegionOffset(geox, geoy);

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null) {
      return (short)zmin;
    }

    short temph = -32768;
    int index = getIndex(getBlock(geox), getBlock(geoy), (IntBuffer)_geodataIndex.get(Short.valueOf(region)));

    index++;
    int cellX;
    int cellY;
    short height;
    switch (geo.get(index - 1)) {
    case 0:
      temph = geo.getShort(index);
      break;
    case 1:
      cellX = getCell(geox);
      cellY = getCell(geoy);
      index += ((cellX << 3) + cellY << 1);
      height = geo.getShort(index);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      temph = height;
      break;
    default:
      cellX = getCell(geox);
      cellY = getCell(geoy);
      int offset = (cellX << 3) + cellY;
      while (offset > 0) {
        byte lc = geo.get(index);
        index += (lc << 1) + 1;
        offset--;
      }

      byte layers = geo.get(index);
      index++;
      if ((layers <= 0) || (layers > 125)) {
        _log.warning("Broken geofile (case2), region: " + region + " - invalid layer count: " + layers + " at: " + geox + " " + geoy);
        return (short)zmin;
      }
      while (layers > 0) {
        height = geo.getShort(index);
        height = (short)(height & 0xFFF0);
        height = (short)(height >> 1);
        if ((zmin - temph) * (zmin - temph) > (zmin - height) * (zmin - height)) {
          temph = height;
        }
        layers = (byte)(layers - 1);
        index += 2;
      }
      if ((temph <= zmax + 200) && (temph >= zmin - 200)) break;
      return (short)zmin;
    }

    if ((temph > zmax + 1000) || (temph < zmin - 1000)) {
      return (short)zmin;
    }
    return temph;
  }

  private static double nCanMoveNext(int x, int y, int z, int tx, int ty, int tz)
  {
    short region = getRegionOffset(x, y);

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null) {
      if (Config.DEBUG) {
        _log.warning("Geo Region - Region Offset: " + region + " dosnt exist!!");
      }
      return z;
    }

    short NSWE = 0;
    int index = getIndex(getBlock(x), getBlock(y), (IntBuffer)_geodataIndex.get(Short.valueOf(region)));

    index++;

    switch (geo.get(index - 1)) {
    case 0:
      return geo.getShort(index);
    case 1:
      cellX = getCell(x);
      cellY = getCell(y);
      index += ((cellX << 3) + cellY << 1);
      height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);
      if (checkNSWE(NSWE, x, y, tx, ty)) {
        return height;
      }
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
    short height = -1;
    if ((layers <= 0) || (layers > 125)) {
      _log.warning("Broken geofile (case3), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
      return z;
    }
    short tempz = -32768;
    while (layers > 0) {
      height = geo.getShort(index);
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
    if (checkNSWE(NSWE, x, y, tx, ty)) {
      return tempz;
    }
    return 4.9E-324D;
  }

  private static boolean nLOS(int x, int y, int z, int inc_x, int inc_y, double inc_z, int tz)
  {
    short region = getRegionOffset(x, y);

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null) {
      return true;
    }

    short NSWE = 0;
    int index = getIndex(getBlock(x), getBlock(y), (IntBuffer)_geodataIndex.get(Short.valueOf(region)));

    index++;
    int cellX;
    int cellY;
    switch (geo.get(index - 1)) {
    case 0:
      short height1 = geo.getShort(index);
      if (z > height1) {
        return z + inc_z > height1;
      }
      return z + inc_z < height1;
    case 1:
      cellX = getCell(x);
      cellY = getCell(y);
      index += ((cellX << 3) + cellY << 1);
      short height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
      height = (short)(height & 0xFFF0);
      height = (short)(height >> 1);

      return (checkNSWE(NSWE, x, y, x + inc_x, y + inc_y)) || 
        (z >= nGetUpperHeight(x + inc_x, y + inc_y, height));
    case 2:
      cellX = getCell(x);
      cellY = getCell(y);

      int offset = (cellX << 3) + cellY;
      while (offset > 0)
      {
        byte lc = geo.get(index);
        index += (lc << 1) + 1;
        offset--;
      }
      byte layers = geo.get(index);

      index++;
      short tempZ = -1;
      if ((layers <= 0) || (layers > 125)) {
        _log.warning("Broken geofile (case4), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
        return false;
      }
      short upperHeight = 32767;
      short lowerHeight = -32768;
      byte temp_layers = layers;
      boolean highestlayer = true;
      while (temp_layers > 0)
      {
        tempZ = geo.getShort(index);
        tempZ = (short)(tempZ & 0xFFF0);
        tempZ = (short)(tempZ >> 1);

        if (z > tempZ) {
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

      if ((z - upperHeight < -10) && (z - upperHeight > inc_z - 20.0D) && (z - lowerHeight > 40)) {
        return false;
      }

      if (!highestlayer)
      {
        if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
        {
          if (z < nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight)) {
            return false;
          }
        }
        return true;
      }
      if (!checkNSWE(NSWE, x, y, x + inc_x, y + inc_y))
      {
        if (z < nGetUpperHeight(x + inc_x, y + inc_y, lowerHeight)) {
          return false;
        }
      }
      return true;
    }
    return true;
  }

  private short nGetNSWE(int x, int y, int z)
  {
    short region = getRegionOffset(x, y);

    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null) {
      return 15;
    }

    short NSWE = 0;
    int index = getIndex(getBlock(x), getBlock(y), (IntBuffer)_geodataIndex.get(Short.valueOf(region)));

    index++;
    int cellX;
    int cellY;
    short height;
    switch (geo.get(index - 1)) {
    case 0:
      return 15;
    case 1:
      cellX = getCell(x);
      cellY = getCell(y);
      index += ((cellX << 3) + cellY << 1);
      height = geo.getShort(index);
      NSWE = (short)(height & 0xF);
      break;
    case 2:
      cellX = getCell(x);
      cellY = getCell(y);
      int offset = (cellX << 3) + cellY;
      while (offset > 0) {
        byte lc = geo.get(index);
        index += (lc << 1) + 1;
        offset--;
      }
      byte layers = geo.get(index);
      index++;
      height = -1;
      if ((layers <= 0) || (layers > 125)) {
        _log.warning("Broken geofile (case5), region: " + region + " - invalid layer count: " + layers + " at: " + x + " " + y);
        return 15;
      }
      short tempz = -32768;
      while (layers > 0) {
        height = geo.getShort(index);
        height = (short)(height & 0xFFF0);
        height = (short)(height >> 1);

        if ((z - tempz) * (z - tempz) > (z - height) * (z - height)) {
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

  public FastTable<GeoNode> getNeighbors(GeoNode n)
  {
    int x = n.getLoc().getNodeX();
    int y = n.getLoc().getNodeY();
    short region = getRegionOffset(x, y);
    ByteBuffer geo = (ByteBuffer)_geodata.get(Short.valueOf(region));
    if (geo == null) {
      return null;
    }
    byte parentdirection = 0;
    if (n.getParent() != null) {
      if (n.getParent().getLoc().getNodeX() > x)
        parentdirection = 1;
      else if (n.getParent().getLoc().getNodeX() < x) {
        parentdirection = -1;
      }

      if (n.getParent().getLoc().getNodeY() > y)
        parentdirection = 2;
      else if (n.getParent().getLoc().getNodeY() < y) {
        parentdirection = -2;
      }
    }

    short NSWE = 0;
    int index = getIndex(getBlock(x), getBlock(y), (IntBuffer)_geodataIndex.get(Short.valueOf(region)));

    FastTable neighbors = new FastTable(4);
    byte type = geo.get(index);
    index++;
    short layers;
    switch (type) {
    case 0:
      layers = geo.getShort(index);
      n.getLoc().setZ(layers);
      if (parentdirection != 1) {
        neighbors.add(CellPathFinding.getInstance().readNode(x + 1, y, layers));
      }

      if (parentdirection != 2) {
        neighbors.add(CellPathFinding.getInstance().readNode(x, y + 1, layers));
      }

      if (parentdirection != -2) {
        neighbors.add(CellPathFinding.getInstance().readNode(x, y - 1, layers));
      }

      if (parentdirection == -1) break;
      neighbors.add(CellPathFinding.getInstance().readNode(x - 1, y, layers)); break;
    case 1:
      index += ((getCell(x) << 3) + getCell(y) << 1);
      layers = geo.getShort(index);
      NSWE = (short)(layers & 0xF);
      layers = (short)(layers & 0xFFF0);
      layers = (short)(layers >> 1);
      n.getLoc().setZ(layers);
      if ((NSWE != 15) && (parentdirection != 0)) {
        return null;
      }

      if ((parentdirection != 1) && (checkNSWE(NSWE, x, y, x + 1, y))) {
        neighbors.add(CellPathFinding.getInstance().readNode(x + 1, y, layers));
      }

      if ((parentdirection != 2) && (checkNSWE(NSWE, x, y, x, y + 1))) {
        neighbors.add(CellPathFinding.getInstance().readNode(x, y + 1, layers));
      }

      if ((parentdirection != -2) && (checkNSWE(NSWE, x, y, x, y - 1))) {
        neighbors.add(CellPathFinding.getInstance().readNode(x, y - 1, layers));
      }

      if ((parentdirection == -1) || (!checkNSWE(NSWE, x, y, x - 1, y))) break;
      neighbors.add(CellPathFinding.getInstance().readNode(x - 1, y, layers)); break;
    case 2:
      for (int var17 = (getCell(x) << 3) + getCell(y); var17 > 0; var17--) {
        index += (geo.get(index) << 1) + 1;
      }

      byte var18 = geo.get(index);
      index++;
      if ((var18 <= 0) || (var18 > 125)) {
        _log.warning("Broken geofile (case5), region: " + region + " - invalid layer count: " + var18 + " at: " + x + " " + y);
        return null;
      }

      short tempz = -32768;
      for (short z = n.getLoc().getZ(); var18 > 0; var18 = (byte)(var18 - 1)) {
        short height = geo.getShort(index);
        height = (short)(height & 0xFFF0);
        height = (short)(height >> 1);
        if ((z - tempz) * (z - tempz) > (z - height) * (z - height)) {
          tempz = height;
          NSWE = (short)geo.get(index);
          NSWE = (short)(NSWE & 0xF);
        }
        index += 2;
      }

      n.getLoc().setZ(tempz);
      if ((NSWE != 15) && (parentdirection != 0)) {
        return null;
      }

      if ((parentdirection != 1) && (checkNSWE(NSWE, x, y, x + 1, y))) {
        neighbors.add(CellPathFinding.getInstance().readNode(x + 1, y, tempz));
      }

      if ((parentdirection != 2) && (checkNSWE(NSWE, x, y, x, y + 1))) {
        neighbors.add(CellPathFinding.getInstance().readNode(x, y + 1, tempz));
      }

      if ((parentdirection != -2) && (checkNSWE(NSWE, x, y, x, y - 1))) {
        neighbors.add(CellPathFinding.getInstance().readNode(x, y - 1, tempz));
      }

      if ((parentdirection == -1) || (!checkNSWE(NSWE, x, y, x - 1, y))) break;
      neighbors.add(CellPathFinding.getInstance().readNode(x - 1, y, tempz));
    }

    return neighbors;
  }

  private static boolean checkNSWE(short NSWE, int x, int y, int tx, int ty)
  {
    if (NSWE == 15) {
      return true;
    }
    if (tx > x)
    {
      if ((NSWE & 0x1) == 0)
        return false;
    }
    else if (tx < x)
    {
      if ((NSWE & 0x2) == 0) {
        return false;
      }
    }
    if (ty > y)
    {
      if ((NSWE & 0x4) == 0)
        return false;
    }
    else if (ty < y)
    {
      if ((NSWE & 0x8) == 0) {
        return false;
      }
    }
    return true;
  }

  private static void loadL2J() {
    long start = System.currentTimeMillis();
    File f = new File(Config.GEO_L2J_PATH);
    if ((f.exists()) && (f.isDirectory())) {
      File[] e = f.listFiles();
      int len$ = e.length;

      for (int i$ = 0; i$ < len$; i$++) {
        File q = e[i$];
        if ((!q.isHidden()) && (!q.isDirectory()) && (q.getName().endsWith(".l2j"))) {
          loadL2JGeo(q, Config.GEO_SHOW_LOAD);
        }
      }

      long time = System.currentTimeMillis() - start;
      _log.info("GeoData: loaded, time: " + time + " ms.");
      if (Config.GEODATA == 2)
        PathFinding.init();
    }
    else {
      _log.warning("GeoData [ERROR]: Failed to Load: " + Config.GEO_L2J_PATH + "\n");
    }
  }

  public static boolean loadL2JGeo(File quad, boolean log) {
    if (log) {
      _log.info("Loading " + quad.getName().toUpperCase() + "... [" + Util.formatAdena((int)(quad.length() / 1024L)) + "]KB");
    }

    Scanner scanner = new Scanner(quad.getName());
    scanner.useDelimiter("([_|\\.]){1}");
    int rx = scanner.nextInt();
    int ry = scanner.nextInt();
    scanner.close();
    short regionoffset = (short)((rx << 5) + ry);
    FileChannel roChannel = null;
    int index = 0; int block = 0; int flor = 0;
    try
    {
      roChannel = new RandomAccessFile(quad, "r").getChannel();
      int size = (int)roChannel.size();
      MappedByteBuffer geo;
      MappedByteBuffer geo;
      if (Config.FORCE_GEODATA)
      {
        geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, size).load();
      }
      else geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, size);

      geo.order(ByteOrder.LITTLE_ENDIAN);

      if (size >= 196608)
      {
        indexs = IntBuffer.allocate(65536);
        while (block < 65536) {
          byte type = geo.get(index);
          indexs.put(block, index);
          block++;
          index++;
          switch (type) {
          case 0:
            index += 2;
            break;
          case 1:
            index += 128;
            break;
          case 2:
            for (int b = 0; b < 64; b++) {
              byte layers = geo.get(index);
              index += (layers << 1) + 1;
              if (layers > flor) {
                flor = layers;
              }
            }
          }
        }

        _geodataIndex.put(Short.valueOf(regionoffset), indexs);
      }
      _geodata.put(Short.valueOf(regionoffset), geo);
    } catch (Exception e) {
      _log.warning("GeoData [ERROR]: Failed to Load: " + quad.getName().toUpperCase() + "\n");
      e.printStackTrace();
      IntBuffer indexs = 0;
      return indexs;
    }
    finally
    {
      try
      {
        if (roChannel != null)
          roChannel.close();
      }
      catch (Exception ignored)
      {
      }
    }
    return true;
  }

  private static void loadOFF() {
    long start = System.currentTimeMillis();
    File f = new File(Config.GEO_OFF_PATH);
    if ((f.exists()) && (f.isDirectory())) {
      File[] e = f.listFiles();
      int len$ = e.length;

      for (int i$ = 0; i$ < len$; i$++) {
        File q = e[i$];
        if ((!q.isHidden()) && (!q.isDirectory()) && (q.getName().endsWith("conv.dat"))) {
          loadOffGeo(q, Config.GEO_SHOW_LOAD);
        }
      }

      long time = System.currentTimeMillis() - start;
      _log.info("GeoData: loaded, time: " + time + " ms.");
      if (Config.GEODATA == 2)
        PathFinding.getInstance();
    }
    else {
      _log.warning("GeoData [ERROR]: Failed to Load: " + Config.GEO_OFF_PATH + "\n");
    }
  }

  public static boolean loadOffGeo(File quad, boolean log) {
    if (log) {
      _log.info("Loading " + quad.getName().toUpperCase() + "... [" + Util.formatAdena((int)(quad.length() / 1024L)) + "]KB");
    }

    String name = quad.getName();
    name = name.replaceAll("_conv", "");
    Scanner scanner = new Scanner(name);
    scanner.useDelimiter("([_|\\.]){1}");
    int rx = scanner.nextInt();
    int ry = scanner.nextInt();
    scanner.close();
    short regionoffset = (short)((rx << 5) + ry);
    FileChannel roChannel = null;
    int block = 0; int flor = 0;
    int index = 18;
    try
    {
      roChannel = new RandomAccessFile(quad, "r").getChannel();
      int size = (int)roChannel.size();
      MappedByteBuffer geo;
      MappedByteBuffer geo;
      if (Config.FORCE_GEODATA)
      {
        geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, size).load();
      }
      else geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0L, size);

      geo.order(ByteOrder.LITTLE_ENDIAN);

      if (size >= 393234)
      {
        indexs = IntBuffer.allocate(65536);
        while (block < 65536) {
          short type = makeShort(geo.get(index + 1), geo.get(index));
          index += 2;

          indexs.put(block, index);
          if (type == 0)
            index += 4;
          else if (type == 64)
            index += 128;
          else {
            for (int b = 0; b < 64; b++) {
              byte layers = (byte)makeShort(geo.get(index + 1), geo.get(index));
              index += 2;
              for (int i = 0; i < layers << 1; i++) {
                if (layers > flor) {
                  flor = layers;
                }
              }
            }
          }
          block++;
        }
        _geodataIndex.put(Short.valueOf(regionoffset), indexs);
      }
      _geodata.put(Short.valueOf(regionoffset), geo);
    } catch (Exception e) {
      _log.warning("GeoData [ERROR]: Failed to Load: " + quad.getName().toUpperCase() + "\n");
      e.printStackTrace();
      IntBuffer indexs = 0;
      return indexs;
    }
    finally
    {
      try
      {
        if (roChannel != null)
          roChannel.close();
      }
      catch (Exception ignored)
      {
      }
    }
    return true;
  }

  public static void unloadGeodata(byte rx, byte ry) {
    short regionoffset = (short)((rx << 5) + ry);
    _geodataIndex.remove(Short.valueOf(regionoffset));
    _geodata.remove(Short.valueOf(regionoffset));
  }
}