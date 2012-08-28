package l2p.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.residence.Residence;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

public final class ResidenceHolder extends AbstractHolder
{
  private static ResidenceHolder _instance = new ResidenceHolder();

  private IntObjectMap<Residence> _residences = new TreeIntObjectMap();

  private Map<Class, List<Residence>> _fastResidencesByType = new HashMap(4);

  public static ResidenceHolder getInstance()
  {
    return _instance;
  }

  public void addResidence(Residence r)
  {
    _residences.put(r.getId(), r);
  }

  public <R extends Residence> R getResidence(int id)
  {
    return (Residence)_residences.get(id);
  }

  public <R extends Residence> R getResidence(Class<R> type, int id)
  {
    Residence r = getResidence(id);
    if ((r == null) || (r.getClass() != type)) {
      return null;
    }
    return r;
  }

  public <R extends Residence> List<R> getResidenceList(Class<R> t)
  {
    return (List)_fastResidencesByType.get(t);
  }

  public Collection<Residence> getResidences()
  {
    return _residences.values();
  }

  public <R extends Residence> R getResidenceByObject(Class<? extends Residence> type, GameObject object)
  {
    return getResidenceByCoord(type, object.getX(), object.getY(), object.getZ(), object.getReflection());
  }

  public <R extends Residence> R getResidenceByCoord(Class<R> type, int x, int y, int z, Reflection ref)
  {
    Collection residences = type == null ? getResidences() : getResidenceList(type);
    for (Residence residence : residences)
    {
      if (residence.checkIfInZone(x, y, z, ref))
        return residence;
    }
    return null;
  }

  public <R extends Residence> R findNearestResidence(Class<R> clazz, int x, int y, int z, Reflection ref, int offset)
  {
    Residence residence = getResidenceByCoord(clazz, x, y, z, ref);
    double closestDistance;
    if (residence == null)
    {
      closestDistance = offset;

      for (Residence r : getResidenceList(clazz))
      {
        double distance = r.getZone().findDistanceToZone(x, y, z, false);
        if (closestDistance > distance)
        {
          closestDistance = distance;
          residence = r;
        }
      }
    }
    return residence;
  }

  public void callInit()
  {
    for (Residence r : getResidences())
      r.init();
  }

  private void buildFastLook()
  {
    for (Residence residence : _residences.values())
    {
      List list = (List)_fastResidencesByType.get(residence.getClass());
      if (list == null)
        _fastResidencesByType.put(residence.getClass(), list = new ArrayList());
      list.add(residence);
    }
  }

  public void log()
  {
    buildFastLook();
    info("total size: " + _residences.size());
    for (Map.Entry entry : _fastResidencesByType.entrySet())
      info(" - load " + ((List)entry.getValue()).size() + " " + ((Class)entry.getKey()).getSimpleName().toLowerCase() + "(s).");
  }

  public int size()
  {
    return 0;
  }

  public void clear()
  {
    _residences.clear();
    _fastResidencesByType.clear();
  }
}