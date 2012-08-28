package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.EffectType;
import l2m.gameserver.skills.Env;

public class ConditionPlayerHasBuff extends Condition
{
  private final EffectType _effectType;
  private final int _level;

  public ConditionPlayerHasBuff(EffectType effectType, int level)
  {
    _effectType = effectType;
    _level = level;
  }

  protected boolean testImpl(Env env)
  {
    Creature character = env.character;
    if (character == null)
      return false;
    Effect effect = character.getEffectList().getEffectByType(_effectType);
    if (effect == null) {
      return false;
    }
    return (_level == -1) || (effect.getSkill().getLevel() >= _level);
  }
}