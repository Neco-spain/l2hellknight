package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;

public class EffectCombatPointHealOverTime extends Effect
{
  public EffectCombatPointHealOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean onActionTime()
  {
    if (_effected.isHealBlocked()) {
      return true;
    }
    double addToCp = Math.max(0.0D, Math.min(calc(), _effected.calcStat(Stats.CP_LIMIT, null, null) * _effected.getMaxCp() / 100.0D - _effected.getCurrentCp()));
    if (addToCp > 0.0D) {
      _effected.setCurrentCp(_effected.getCurrentCp() + addToCp);
    }
    return true;
  }
}