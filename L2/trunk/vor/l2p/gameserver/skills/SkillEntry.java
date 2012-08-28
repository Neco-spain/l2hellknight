package l2p.gameserver.skills;

import java.util.AbstractMap.SimpleImmutableEntry;
import l2p.gameserver.model.Skill;

public class SkillEntry extends AbstractMap.SimpleImmutableEntry<SkillEntryType, Skill>
{
  private boolean _disabled;

  public SkillEntry(SkillEntryType key, Skill value)
  {
    super(key, value);
  }

  public boolean isDisabled()
  {
    return _disabled;
  }

  public void setDisabled(boolean disabled)
  {
    _disabled = disabled;
  }
}