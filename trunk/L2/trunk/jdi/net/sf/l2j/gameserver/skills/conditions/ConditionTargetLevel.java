package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetLevel extends Condition
{
  private final int _level;

  public ConditionTargetLevel(int level)
  {
    _level = level;
  }

  public boolean testImpl(Env env)
  {
    if (env.target == null)
      return false;
    return env.target.getLevel() >= _level;
  }
}