package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionWithSkill extends Condition
{
  private final boolean _skill;

  public ConditionWithSkill(boolean skill)
  {
    _skill = skill;
  }

  public boolean testImpl(Env env)
  {
    return (_skill) || (env.skill == null);
  }
}