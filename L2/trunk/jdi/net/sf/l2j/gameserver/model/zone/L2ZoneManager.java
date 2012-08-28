package net.sf.l2j.gameserver.model.zone;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;

public class L2ZoneManager
{
  private FastList<L2ZoneType> _zones;

  public L2ZoneManager()
  {
    _zones = new FastList();
  }

  public void registerNewZone(L2ZoneType zone)
  {
    _zones.add(zone);
  }

  public FastList<L2ZoneType> getZones()
  {
    return _zones;
  }

  public void unregisterZone(L2ZoneType zone)
  {
    _zones.remove(zone);
  }

  public void revalidateZones(L2Character character)
  {
    for (L2ZoneType e : _zones)
    {
      if (e != null) e.revalidateInZone(character);
    }
  }

  public void removeCharacter(L2Character character)
  {
    for (L2ZoneType e : _zones)
    {
      if (e != null) e.removeCharacter(character);
    }
  }

  public void onDeath(L2Character character)
  {
    for (L2ZoneType e : _zones)
    {
      if (e != null) e.onDieInside(character);
    }
  }

  public void onRevive(L2Character character)
  {
    for (L2ZoneType e : _zones)
    {
      if (e != null) e.onReviveInside(character);
    }
  }
}