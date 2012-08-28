package net.sf.l2j.gameserver.instancemanager;

import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.zone.type.L2FishingZone;
import net.sf.l2j.gameserver.model.zone.type.L2WaterZone;

public class FishingZoneManager
{
  protected static final Logger _log = Logger.getLogger(FishingZoneManager.class.getName());
  private static FishingZoneManager _instance;
  private FastList<L2FishingZone> _fishingZones;
  private FastList<L2WaterZone> _waterZones;

  public static final FishingZoneManager getInstance()
  {
    if (_instance == null)
    {
      _log.info("Initializing FishingZoneManager");
      _instance = new FishingZoneManager();
    }
    return _instance;
  }

  public void addFishingZone(L2FishingZone fishingZone)
  {
    if (_fishingZones == null) {
      _fishingZones = new FastList();
    }
    _fishingZones.add(fishingZone);
  }

  public void addWaterZone(L2WaterZone waterZone) {
    if (_waterZones == null) {
      _waterZones = new FastList();
    }
    _waterZones.add(waterZone);
  }

  public final L2FishingZone isInsideFishingZone(int x, int y, int z)
  {
    for (L2FishingZone temp : _fishingZones)
      if (temp.isInsideZone(x, y, temp.getWaterZ() - 10)) return temp;
    return null;
  }

  public final L2WaterZone isInsideWaterZone(int x, int y, int z) {
    for (L2WaterZone temp : _waterZones)
      if (temp.isInsideZone(x, y, temp.getWaterZ())) return temp;
    return null;
  }
}