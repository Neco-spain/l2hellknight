package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.EffectType;
import l2m.gameserver.skills.Env;

public final class ConditionTargetHasBuff extends Condition
{
  private final EffectType _effectType;
  private final int _level;

  public ConditionTargetHasBuff(EffectType effectType, int level)
  {
    _effectType = effectType;
    _level = level;
  }

  protected boolean testImpl(Env env)
  {
    Creature target = env.target;
    if (target == null)
      return false;
    Effect effect = target.getEffectList().getEffectByType(_effectType);
    if (effect == null) {
      return false;
    }
    return (_level == -1) || (effect.getSkill().getLevel() >= _level);
  }
}