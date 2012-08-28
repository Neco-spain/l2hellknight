package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public final class ConditionTargetHasForbiddenSkill extends Condition
{
  private final int _skillId;

  public ConditionTargetHasForbiddenSkill(int skillId)
  {
    _skillId = skillId;
  }

  protected boolean testImpl(Env env)
  {
    Creature target = env.target;
    if (!target.isPlayable())
      return false;
    return target.getSkillLevel(Integer.valueOf(_skillId)) <= 0;
  }
}