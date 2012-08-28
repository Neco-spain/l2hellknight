package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;

public class ConditionSkillStats extends Condition
{
  private final Stats _stat;

  public ConditionSkillStats(Stats stat)
  {
    _stat = stat;
  }

  public boolean testImpl(Env env)
  {
    if (env.skill == null)
      return false;
    return env.skill.getStat() == _stat;
  }
}