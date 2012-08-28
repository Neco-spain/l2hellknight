package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public final class ConditionHasSkill extends Condition
{
  private final Integer _id;
  private final int _level;

  public ConditionHasSkill(Integer id, int level)
  {
    _id = id;
    _level = level;
  }

  protected boolean testImpl(Env env)
  {
    if (env.skill == null)
      return false;
    return env.character.getSkillLevel(_id) >= _level;
  }
}