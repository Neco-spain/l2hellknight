package l2m.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.CubicTemplate;

public final class CubicHolder extends AbstractHolder
{
  private static CubicHolder _instance = new CubicHolder();
  private final TIntObjectHashMap<CubicTemplate> _cubics = new TIntObjectHashMap(10);

  public static CubicHolder getInstance()
  {
    return _instance;
  }

  public void addCubicTemplate(CubicTemplate template)
  {
    _cubics.put(hash(template.getId(), template.getLevel()), template);
  }

  public CubicTemplate getTemplate(int id, int level)
  {
    return (CubicTemplate)_cubics.get(hash(id, level));
  }

  public int hash(int id, int level)
  {
    return id * 10000 + level;
  }

  public int size()
  {
    return _cubics.size();
  }

  public void clear()
  {
    _cubics.clear();
  }
}