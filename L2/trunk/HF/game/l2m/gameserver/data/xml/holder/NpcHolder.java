package l2m.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2p.commons.data.xml.AbstractHolder;
import l2p.commons.lang.ArrayUtils;
import l2m.gameserver.templates.npc.NpcTemplate;

public final class NpcHolder extends AbstractHolder
{
  private static final NpcHolder _instance = new NpcHolder();

  private TIntObjectHashMap<NpcTemplate> _npcs = new TIntObjectHashMap(20000);
  private TIntObjectHashMap<List<NpcTemplate>> _npcsByLevel;
  private NpcTemplate[] _allTemplates;
  private Map<String, NpcTemplate> _npcsNames;

  public static NpcHolder getInstance()
  {
    return _instance;
  }

  public void addTemplate(NpcTemplate template)
  {
    _npcs.put(template.npcId, template);
  }

  public NpcTemplate getTemplate(int id)
  {
    NpcTemplate npc = (NpcTemplate)ArrayUtils.valid(_allTemplates, id);
    if (npc == null)
    {
      warn("Npc is not found with " + id);
      return null;
    }
    return _allTemplates[id];
  }

  public NpcTemplate getTemplateByName(String name)
  {
    return (NpcTemplate)_npcsNames.get(name.toLowerCase());
  }

  public List<NpcTemplate> getAllOfLevel(int lvl)
  {
    return (List)_npcsByLevel.get(lvl);
  }

  public NpcTemplate[] getAll()
  {
    return (NpcTemplate[])_npcs.getValues(new NpcTemplate[_npcs.size()]);
  }

  private void buildFastLookupTable()
  {
    _npcsByLevel = new TIntObjectHashMap();
    _npcsNames = new HashMap();

    int highestId = 0;
    for (int id : _npcs.keys()) {
      if (id > highestId)
        highestId = id;
    }
    _allTemplates = new NpcTemplate[highestId + 1];
    for (TIntObjectIterator iterator = _npcs.iterator(); iterator.hasNext(); )
    {
      iterator.advance();
      int npcId = iterator.key();
      NpcTemplate npc = (NpcTemplate)iterator.value();

      _allTemplates[npcId] = npc;
      List byLevel;
      if ((byLevel = (List)_npcsByLevel.get(npc.level)) == null)
        _npcsByLevel.put(npcId, byLevel = new ArrayList());
      byLevel.add(npc);

      _npcsNames.put(npc.name.toLowerCase(), npc);
    }
  }

  protected void process()
  {
    buildFastLookupTable();
  }

  public int size()
  {
    return _npcs.size();
  }

  public void clear()
  {
    _npcsNames.clear();
    _npcs.clear();
  }
}