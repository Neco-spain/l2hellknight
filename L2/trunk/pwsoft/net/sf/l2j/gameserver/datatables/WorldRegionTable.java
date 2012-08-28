package net.sf.l2j.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.util.log.AbstractLogger;

public class WorldRegionTable
{
  private static Logger _log = AbstractLogger.getLogger(WorldRegionTable.class.getName());
  private static WorldRegionTable _instance;
  private static FastMap<Integer, FastList<Region>> _regions = new FastMap().shared("WorldRegionTable._regions");

  public static WorldRegionTable getInstance()
  {
    return _instance;
  }

  public static void init() {
    _instance = new WorldRegionTable();
    load();
  }

  private static void load()
  {
    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./data/world_regions.txt");
      if (!Data.exists()) {
        return;
      }
      fr = new FileReader(Data);
      br = new BufferedReader(fr);
      lnr = new LineNumberReader(br);
      FastList wrs = new FastList();
      String line;
      while ((line = lnr.readLine()) != null) {
        if ((line.trim().length() == 0) || (line.startsWith("#"))) {
          continue;
        }
        String[] wr = line.split("=");
        int zoneId = Integer.valueOf(wr[0]).intValue();
        String[] crds = wr[1].split(";");

        for (String xy : crds) {
          String[] fXy = xy.split(",");
          wrs.add(new Region(Integer.valueOf(fXy[0]).intValue(), Integer.valueOf(fXy[1]).intValue()));
        }

        _regions.put(Integer.valueOf(zoneId), wrs);
      }
    }
    catch (Exception e1) {
      e.printStackTrace();
    } finally {
      try {
        if (fr != null) {
          fr.close();
        }
        if (br != null) {
          br.close();
        }
        if (lnr != null)
          lnr.close();
      }
      catch (Exception e1) {
      }
    }
    _log.info("WorldRegionTable: cached " + _regions.size() + " regions.");
  }

  public FastMap<Integer, FastList<Region>> getRegions() {
    return _regions;
  }

  public void clearRegions() {
    _regions.clear();
  }
  public static class Region {
    public int x;
    public int y;

    Region(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
}