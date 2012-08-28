package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;

public final class ConditionUsingSkill extends Condition
{
  private final int _skillId;

  public ConditionUsingSkill(int skillId)
  {
    _skillId = skillId;
  }

  public boolean testImpl(Env env)
  {
    if (env.skill == null)
      return false;
    return env.skill.getId() == _skillId;
  }
}