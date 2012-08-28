package l2p.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import l2p.commons.data.xml.AbstractHolder;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.gameserver.model.Player;
import l2p.gameserver.templates.InstantZone;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class InstantZoneHolder extends AbstractHolder
{
  private static final InstantZoneHolder _instance = new InstantZoneHolder();
  private IntObjectMap<InstantZone> _zones = new HashIntObjectMap();

  public static InstantZoneHolder getInstance()
  {
    return _instance;
  }

  public void addInstantZone(InstantZone zone)
  {
    _zones.put(zone.getId(), zone);
  }

  public InstantZone getInstantZone(int id)
  {
    return (InstantZone)_zones.get(id);
  }

  private SchedulingPattern getResetReuseById(int id)
  {
    InstantZone zone = getInstantZone(id);
    return zone == null ? null : zone.getResetReuse();
  }

  public int getMinutesToNextEntrance(int id, Player player)
  {
    SchedulingPattern resetReuse = getResetReuseById(id);
    if (resetReuse == null) {
      return 0;
    }
    Long time = null;
    if ((getSharedReuseInstanceIds(id) != null) && (!getSharedReuseInstanceIds(id).isEmpty()))
    {
      List reuses = new ArrayList();
      for (Iterator i$ = getSharedReuseInstanceIds(id).iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
        if (player.getInstanceReuse(i) != null)
          reuses.add(player.getInstanceReuse(i)); }
      if (!reuses.isEmpty())
      {
        Collections.sort(reuses);
        time = (Long)reuses.get(reuses.size() - 1);
      }
    }
    else {
      time = player.getInstanceReuse(id);
    }if (time == null)
      return 0;
    return (int)Math.max((resetReuse.next(time.longValue()) - System.currentTimeMillis()) / 60000L, 0L);
  }

  public List<Integer> getSharedReuseInstanceIds(int id)
  {
    if (getInstantZone(id).getSharedReuseGroup() < 1)
      return null;
    List sharedInstanceIds = new ArrayList();
    for (InstantZone iz : _zones.values())
      if ((iz.getSharedReuseGroup() > 0) && (getInstantZone(id).getSharedReuseGroup() > 0) && (iz.getSharedReuseGroup() == getInstantZone(id).getSharedReuseGroup()))
        sharedInstanceIds.add(Integer.valueOf(iz.getId()));
    return sharedInstanceIds;
  }

  public List<Integer> getSharedReuseInstanceIdsByGroup(int groupId)
  {
    if (groupId < 1)
      return null;
    List sharedInstanceIds = new ArrayList();
    for (InstantZone iz : _zones.values())
      if ((iz.getSharedReuseGroup() > 0) && (iz.getSharedReuseGroup() == groupId))
        sharedInstanceIds.add(Integer.valueOf(iz.getId()));
    return sharedInstanceIds;
  }

  public int size()
  {
    return _zones.size();
  }

  public void clear()
  {
    _zones.clear();
  }
}