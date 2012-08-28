package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Skill;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.stats.Env;

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