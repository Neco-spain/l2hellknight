package net.sf.l2j.gameserver;

import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.pathfinding.GeoNode;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.log.AbstractLogger;

public class GeoData
{
  public static final Logger _log = AbstractLogger.getLogger("GeoData");
  private static GeoData _instance;

  public static GeoData getInstance()
  {
    return _instance;
  }

  public static void init() {
    if (Config.GEODATA == 0) {
      _instance = new GeoData();
    } else {
      GeoEngine.init();
      _instance = GeoEngine.getInstance();
    }
  }

  public short getType(int x, int y) {
    return 0;
  }

  public short getHeight(int x, int y, int z) {
    return (short)z;
  }

  public short getSpawnHeight(int x, int y, int zmin, int zmax) {
    return (short)zmin;
  }

  public String geoPosition(int x, int y) {
    return "";
  }

  public boolean canSeeTarget(L2Object cha, L2Object target) {
    return Math.abs(target.getZ() - cha.getZ()) < 1000;
  }

  public boolean canSeeTarget(L2Object cha, Point3D worldPosition) {
    return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
  }

  public boolean canSeeTarget(L2Object cha, L2Object target, boolean attack) {
    return Math.abs(target.getZ() - cha.getZ()) < 1000;
  }

  public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz) {
    return Math.abs(z - tz) < 1000;
  }

  public Location moveCheckAttack(int x, int y, int z, int tx, int ty, int tz) {
    return new Location(tx, ty, tz);
  }

  public boolean canSeeTargetDebug(L2PcInstance gm, L2Object target) {
    return true;
  }

  public short getNSWE(int x, int y, int z) {
    return 15;
  }

  public Location moveCheck(int x, int y, int z, int tx, int ty, int tz) {
    return new Location(tx, ty, tz);
  }

  public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz) {
    return true;
  }

  public void addGeoDataBug(L2PcInstance gm, String comment) {
  }

  public static void unloadGeodata(byte rx, byte ry) {
  }

  public static boolean loadGeodataFile(byte rx, byte ry) {
    return false;
  }

  public boolean hasGeo(int x, int y) {
    return false;
  }

  public FastTable<GeoNode> getNeighbors(GeoNode n) {
    return null;
  }
}