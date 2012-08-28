package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.Env;

public class ConditionUsingSkill extends Condition
{
  private int _id;

  public ConditionUsingSkill(int id)
  {
    _id = id;
  }

  protected boolean testImpl(Env env)
  {
    if (env.skill == null) {
      return false;
    }
    return env.skill.getId() == _id;
  }
}