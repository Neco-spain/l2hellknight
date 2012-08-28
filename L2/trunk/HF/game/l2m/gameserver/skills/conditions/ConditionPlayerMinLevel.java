package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionPlayerMinLevel extends Condition
{
  private final int _level;

  public ConditionPlayerMinLevel(int level)
  {
    _level = level;
  }

  protected boolean testImpl(Env env)
  {
    return env.character.getLevel() >= _level;
  }
}