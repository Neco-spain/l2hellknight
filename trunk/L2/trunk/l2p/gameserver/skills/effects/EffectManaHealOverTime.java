package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class EffectManaHealOverTime extends Effect
{
  private final boolean _ignoreMpEff;

  public EffectManaHealOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
    _ignoreMpEff = template.getParam().getBool("ignoreMpEff", false);
  }

  public boolean onActionTime()
  {
    if (_effected.isHealBlocked()) {
      return true;
    }
    double mp = calc();
    double newMp = mp * (!_ignoreMpEff ? _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0D, _effector, getSkill()) : 100.0D) / 100.0D;
    double addToMp = Math.max(0.0D, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100.0D - _effected.getCurrentMp()));

    if (addToMp > 0.0D) {
      _effected.setCurrentMp(_effected.getCurrentMp() + addToMp);
    }
    return true;
  }
}