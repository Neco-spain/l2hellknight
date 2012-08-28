package l2m.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.spawn.SpawnTemplate;

public final class SpawnHolder extends AbstractHolder
{
  private static final SpawnHolder _instance = new SpawnHolder();

  private Map<String, List<SpawnTemplate>> _spawns = new HashMap();

  public static SpawnHolder getInstance()
  {
    return _instance;
  }

  public void addSpawn(String group, SpawnTemplate spawn)
  {
    List spawns = (List)_spawns.get(group);
    if (spawns == null)
      _spawns.put(group, spawns = new ArrayList());
    spawns.add(spawn);
  }

  public List<SpawnTemplate> getSpawn(String name)
  {
    List template = (List)_spawns.get(name);
    return template == null ? Collections.emptyList() : template;
  }

  public int size()
  {
    int i = 0;
    for (List l : _spawns.values()) {
      i += l.size();
    }
    return i;
  }

  public void clear()
  {
    _spawns.clear();
  }

  public Map<String, List<SpawnTemplate>> getSpawns()
  {
    return _spawns;
  }
}