package l2m.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.model.instances.StaticObjectInstance;
import l2m.gameserver.templates.StaticObjectTemplate;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public final class StaticObjectHolder extends AbstractHolder
{
  private static final StaticObjectHolder _instance = new StaticObjectHolder();

  private IntObjectMap<StaticObjectTemplate> _templates = new HashIntObjectMap();
  private IntObjectMap<StaticObjectInstance> _spawned = new HashIntObjectMap();

  public static StaticObjectHolder getInstance()
  {
    return _instance;
  }

  public void addTemplate(StaticObjectTemplate template)
  {
    _templates.put(template.getUId(), template);
  }

  public StaticObjectTemplate getTemplate(int id)
  {
    return (StaticObjectTemplate)_templates.get(id);
  }

  public void spawnAll()
  {
    for (StaticObjectTemplate template : _templates.values())
      if (template.isSpawn())
      {
        StaticObjectInstance obj = template.newInstance();

        _spawned.put(template.getUId(), obj);
      }
    info("spawned: " + _spawned.size() + " static object(s).");
  }

  public StaticObjectInstance getObject(int id)
  {
    return (StaticObjectInstance)_spawned.get(id);
  }

  public int size()
  {
    return _templates.size();
  }

  public void clear()
  {
    _templates.clear();
  }
}