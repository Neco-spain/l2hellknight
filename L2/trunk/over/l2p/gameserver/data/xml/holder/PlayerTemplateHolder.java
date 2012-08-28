package l2p.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.ClassType;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.base.Sex;
import l2p.gameserver.templates.player.PlayerTemplate;

public final class PlayerTemplateHolder extends AbstractHolder
{
  private static final PlayerTemplateHolder _instance = new PlayerTemplateHolder();

  private static final TIntObjectHashMap<TIntObjectHashMap<TIntObjectHashMap<PlayerTemplate>>> _templates = new TIntObjectHashMap();

  private static int _templates_count = 0;

  public static PlayerTemplateHolder getInstance()
  {
    return _instance;
  }

  public void addPlayerTemplate(Race race, ClassType type, Sex sex, PlayerTemplate template)
  {
    if (_templates.get(race.ordinal()) == null) {
      _templates.put(race.ordinal(), new TIntObjectHashMap());
    }
    if (((TIntObjectHashMap)_templates.get(race.ordinal())).get(type.getMainType().ordinal()) == null) {
      ((TIntObjectHashMap)_templates.get(race.ordinal())).put(type.getMainType().ordinal(), new TIntObjectHashMap());
    }
    ((TIntObjectHashMap)((TIntObjectHashMap)_templates.get(race.ordinal())).get(type.getMainType().ordinal())).put(sex.ordinal(), template);
    _templates_count += 1;
  }

  public PlayerTemplate getPlayerTemplate(ClassId classId, Sex sex)
  {
    Race race = classId.getRace();
    ClassType type = classId.getType();
    if ((_templates.get(race.ordinal()) != null) && 
      (((TIntObjectHashMap)_templates.get(race.ordinal())).get(type.getMainType().ordinal()) != null))
      return (PlayerTemplate)((TIntObjectHashMap)((TIntObjectHashMap)_templates.get(race.ordinal())).get(type.getMainType().ordinal())).get(sex.ordinal());
    return null;
  }

  public int size()
  {
    return _templates_count;
  }

  public void clear()
  {
    _templates.clear();
  }
}