package l2m.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.DoorTemplate;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public final class DoorHolder extends AbstractHolder
{
  private static final DoorHolder _instance = new DoorHolder();

  private IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap();

  public static DoorHolder getInstance()
  {
    return _instance;
  }

  public void addTemplate(DoorTemplate door)
  {
    _doors.put(door.getNpcId(), door);
  }

  public DoorTemplate getTemplate(int doorId)
  {
    return (DoorTemplate)_doors.get(doorId);
  }

  public IntObjectMap<DoorTemplate> getDoors()
  {
    return _doors;
  }

  public int size()
  {
    return _doors.size();
  }

  public void clear()
  {
    _doors.clear();
  }
}