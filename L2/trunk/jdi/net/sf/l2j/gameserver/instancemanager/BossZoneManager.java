package net.sf.l2j.gameserver.instancemanager;

import java.io.PrintStream;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.zone.type.L2CustomZone;

public class BossZoneManager
{
  private static BossZoneManager _instance;
  private FastList<L2CustomZone> _zones;

  public static final BossZoneManager getInstance()
  {
    if (_instance == null)
    {
      System.out.println("Initializing BossZoneManager");
      _instance = new BossZoneManager();
    }
    return _instance;
  }

  public void addZone(L2CustomZone zone)
  {
    if (_zones == null)
    {
      _zones = new FastList();
    }
    _zones.add(zone);
  }

  public final L2CustomZone getZone(L2Character character)
  {
    if (_zones != null)
      for (L2CustomZone temp : _zones)
      {
        if (temp.isCharacterInZone(character))
        {
          return temp;
        }
      }
    return null;
  }

  public final L2CustomZone getZone(int x, int y, int z)
  {
    if (_zones != null)
      for (L2CustomZone temp : _zones)
      {
        if (temp.isInsideZone(x, y, z))
        {
          return temp;
        }
      }
    return null;
  }

  public boolean checkIfInZone(String zoneType, L2Object obj)
  {
    L2CustomZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
    if (temp == null)
    {
      return false;
    }
    return temp.getZoneName().equalsIgnoreCase(zoneType);
  }
}