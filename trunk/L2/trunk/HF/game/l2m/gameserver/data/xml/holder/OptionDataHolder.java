package l2m.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.OptionDataTemplate;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public final class OptionDataHolder extends AbstractHolder
{
  private static final OptionDataHolder _instance = new OptionDataHolder();

  private IntObjectMap<OptionDataTemplate> _templates = new HashIntObjectMap();

  public static OptionDataHolder getInstance()
  {
    return _instance;
  }

  public void addTemplate(OptionDataTemplate template)
  {
    _templates.put(template.getId(), template);
  }

  public OptionDataTemplate getTemplate(int id)
  {
    return (OptionDataTemplate)_templates.get(id);
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