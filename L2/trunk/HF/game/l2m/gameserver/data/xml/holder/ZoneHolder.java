package l2m.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.Map;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.ZoneTemplate;

public class ZoneHolder extends AbstractHolder
{
  private static final ZoneHolder _instance = new ZoneHolder();

  private final Map<String, ZoneTemplate> _zones = new HashMap();

  public static ZoneHolder getInstance()
  {
    return _instance;
  }

  public void addTemplate(ZoneTemplate zone)
  {
    _zones.put(zone.getName(), zone);
  }

  public ZoneTemplate getTemplate(String name)
  {
    return (ZoneTemplate)_zones.get(name);
  }

  public Map<String, ZoneTemplate> getZones()
  {
    return _zones;
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